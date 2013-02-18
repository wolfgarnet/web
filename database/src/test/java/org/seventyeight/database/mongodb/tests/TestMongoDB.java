package org.seventyeight.database.mongodb.tests;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.junit.Rule;
import org.junit.Test;
import org.seventyeight.database.mongodb.MongoDBCollection;
import org.seventyeight.database.mongodb.MongoDBRule;
import org.seventyeight.database.mongodb.MongoDocument;

import java.util.List;

/**
 * @author cwolfgang
 *         Date: 16-02-13
 *         Time: 21:12
 */
public class TestMongoDB {

    public static final String COLLECTION_NAME = "webtest";

    @Rule
    public MongoDBRule env = new MongoDBRule( "testdb" );

    @Test
    public void test1() {
        MongoDBCollection collection = env.getDatabase().createCollection( COLLECTION_NAME );

        MongoDocument d = new MongoDocument();
        d.set( "snade", "made" );
        collection.add( d );

        collection.listDocuments();

        env.getDatabase().getCollections();
    }


    @Test
    public void test2() {
        MongoDBCollection collection = env.getDatabase().createCollection( COLLECTION_NAME );

        MongoDocument d1 = new MongoDocument();
        MongoDocument d2 = new MongoDocument();

        d1.set( "CONTENT", "YEAH" );
        d2.set( "CHILD1", d1 );
        d2.set( "CHILD1", d1 );

        collection.add( d2 );

        collection.show();
    }

    @Test
    public void test3() {
        MongoDBCollection collection = env.getDatabase().createCollection( COLLECTION_NAME + "1" );

        MongoDocument parent = new MongoDocument();
        parent.setList( "childs" );

        MongoDocument c1 = new MongoDocument();
        c1.set( "val1", 1 );
        c1.set( "val2", 2 );

        parent.addToList( "childs", c1 );

        c1.set( "val1", 111 );
        parent.addToList( "childs", c1 );

        collection.add( parent );

        collection.show();

        List<MongoDocument> docs = parent.getList( "childs" );
        for( MongoDocument doc : docs ) {
            System.out.println( "DOC: " + doc);
        }
    }
}
