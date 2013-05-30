package org.seventyeight.web.model;

import org.apache.log4j.Logger;
import org.seventyeight.database.mongodb.MongoDBCollection;
import org.seventyeight.database.mongodb.MongoDBQuery;
import org.seventyeight.database.mongodb.MongoDocument;
import org.seventyeight.web.Core;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @author cwolfgang
 */
public abstract class NodeDescriptor<T extends Describable<T>> extends Descriptor<T> implements Node {

    private static Logger logger = Logger.getLogger( NodeDescriptor.class );

    @Override
    public Node getParent() {
        return Core.getInstance();
    }

    @Override
    public T newInstance( String title ) throws ItemInstantiationException {
        logger.debug( "New instance of " + getType() + " with title " + title + "(" + allowIdenticalNaming() + ")" );
        if( !allowIdenticalNaming() ) {
            if( titleExists( title, getType() ) ) {
                throw new ItemInstantiationException( "Multiple instances of " + getType() + " with the same title is not allowed." );
            }
        }

        T node = createNode();

        node.getDocument().set( "type", getType() );
        node.getDocument().set( "title", title );

        return node;
    }

    private boolean titleExists( String title, String type ) {
        MongoDocument doc = MongoDBCollection.get( Core.NODE_COLLECTION_NAME ).findOne( new MongoDBQuery().is( Core.NAME_FIELD, title ) );
        return !doc.isNull();
    }

    protected T createNode( ) throws ItemInstantiationException {
        logger.debug( "Creating " + clazz.getName() );

        MongoDBCollection collection = MongoDBCollection.get( getCollectionName() );
        MongoDocument document = new MongoDocument();

        T instance = null;
        try {
            Constructor<T> c = clazz.getConstructor( Node.class, MongoDocument.class );
            instance = c.newInstance( this, document );
        } catch( Exception e ) {
            throw new ItemInstantiationException( "Unable to instantiate " + clazz.getName(), e );
        }

        document.set( "class", clazz.getName() );
        //collection.save( document );

        return instance;
    }

    public String getCollectionName() {
        return Core.NODE_COLLECTION_NAME;
    }

    public abstract String getType();

    /**
     * Determine whether to allow identical names or not.<br />
     * Default is true.
     */
    public boolean allowIdenticalNaming() {
        return true;
    }

    @Override
    public String getMainTemplate() {
        return "org/seventyeight/web/main.vm";
    }
}
