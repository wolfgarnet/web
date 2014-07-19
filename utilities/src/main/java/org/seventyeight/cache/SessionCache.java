package org.seventyeight.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author cwolfgang
 */
public class SessionCache {

    private static Logger logger = LogManager.getLogger( SessionCache.class );

    private static Map<String, Object> cache;
    private DBStrategy dbStrategy;

    private static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SessionCache( int maxSize, DBStrategy dbStrategy ) {
        this.dbStrategy = dbStrategy;
        this.cache = Collections.synchronizedMap(new LruCache<String, Object>( maxSize ));
    }

    public void save(Object obj, String id) {
        logger.debug( "[CACHE] saving {}", id );
        Object record = dbStrategy.save( obj, id );
        lock.writeLock().lock();
        try {
            cache.put( id, record );
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <TYPE> TYPE get(String id) {
        lock.readLock().lock();
        try {
            if(cache.containsKey( id )) {
                logger.debug( "[CACHE] retrieved {}", id );
                return (TYPE) dbStrategy.deserialize( cache.get( id ) );
            } else {
                logger.debug( "[CACHE] missed {}", id );
                lock.readLock().unlock();
                TYPE o = resolve( id );
                lock.readLock().lock();
                return o;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private <TYPE> TYPE resolve(String id) {
        logger.debug( "[CACHE] resolving {}", id );
        Object record = dbStrategy.get( id );
        if(record != null) {
            Object object = dbStrategy.deserialize( record );
            // Put the record in the cache
            lock.writeLock().lock();
            try {
                cache.put( id, record );
            } finally {
                lock.writeLock().unlock();
            }

            return (TYPE) object;
        } else {
            logger.debug( "[CACHE] could not resolve {}", id );
            return null;
        }
    }
}
