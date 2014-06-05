package org.seventyeight.web.music;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seventyeight.database.mongodb.MongoDBCollection;
import org.seventyeight.database.mongodb.MongoDBQuery;
import org.seventyeight.database.mongodb.MongoDocument;
import org.seventyeight.web.Core;
import org.seventyeight.web.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cwolfgang
 */
public class Festival extends Resource<Festival> {

    private static Logger logger = LogManager.getLogger( Festival.class );

    public Festival( Node parent, MongoDocument document ) {
        super( parent, document );
    }

    @Override
    public void updateNode( CoreRequest request, JsonObject jsonData ) {
        if(jsonData != null) {
            JsonElement venueElement = jsonData.get( "venue" );
            if(venueElement.isJsonNull()) {
                throw new IllegalArgumentException( "Venue is not provided" );
            }

            document.set( "venue", venueElement.getAsString() );
        }
    }


    public String getVenueId() {
        return document.get( "venue", null );
    }

    public void addEvent(Event event) {
        event.setAsPartOf( this );
    }

    public Venue getVenue() throws NotFoundException, ItemInstantiationException {
        String id = getVenueId();
        if(id != null) {
            return Core.getInstance().getNodeById( this, id );
        } else {
            return null;
        }
    }

    public List<Event> getEvents() throws ItemInstantiationException {
        logger.debug( "Getting events for {}", this );

        MongoDBQuery query = new MongoDBQuery().is( "partOf", getIdentifier() );
        List<MongoDocument> docs = MongoDBCollection.get( Core.NODES_COLLECTION_NAME ).find( query );
        List<Event> events = new ArrayList<Event>( docs.size() );
        for(MongoDocument doc : docs) {
            Event event = (Event) Core.getInstance().getNode( this, doc );
            events.add( event );
        }

        return events;
    }

    public static class FestivalDescriptor extends NodeDescriptor<Festival> {

        @Override
        public String getType() {
            return "festival";
        }

        @Override
        public String getDisplayName() {
            return "Festival";
        }
    }
}