package org.seventyeight.web.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seventyeight.cache.SessionCache;
import org.seventyeight.database.mongodb.MongoDatabaseStrategy;
import org.seventyeight.database.mongodb.MongoDocument;
import org.seventyeight.markup.HtmlGenerator;
import org.seventyeight.markup.SimpleParser;
import org.seventyeight.web.Core;
import org.seventyeight.web.nodes.Conversation;

/**
 * @author cwolfgang
 */
public class Comment extends AbstractNode<Comment> {
	
    private static Logger logger = LogManager.getLogger( Comment.class );

    private static SimpleParser textParser = new SimpleParser( new HtmlGenerator() );

    public static final String TITLE_FIELD = "title";
    public static final String TEXT_FIELD = "text";
    public static final String USER_FIELD = "owner";
    public static final String DATE_FIELD = "date";
    
    /** The conversation this comment is attached to */
    public static final String CONVERSATION_FIELD = "conversation";
    
    /** The immediate comment or top level conversation */
    public static final String PARENT_FIELD = "parent";

    public static final String COMMENTS_COLLECTION = "comments";
    
    private static SessionCache commentCache = new SessionCache( new MongoDatabaseStrategy( COMMENTS_COLLECTION ) );
    static {
    	commentCache.setAutoFlush(true);
    }

    public Comment(Core core, Node parent, MongoDocument document) {
        super(core, parent, document);
    }

    @Override
    public void updateNode( JsonObject jsonData ) {
        //String text = request.getValue( "comment", "" );
        String text = jsonData.getAsJsonPrimitive( "comment" ).getAsString();
        setText( text );
    }

    protected void setTextParserVersion( String version ) {
        document.set( "textParserVersion", version );
    }

    public String getText( TextType type ) {
        MongoDocument texts = document.getSubDocument( TEXT_FIELD, null );
        if( texts == null || texts.isNull() ) {
            return "";
        } else {
            return texts.get( type.name(), "" );
        }
    }

    /**
     * Get the HTML version of the text
     */
    public String getText() {
        return getText( TextType.html );
    }

    public String getMarkUp() {
        return getText( TextType.markUp );
    }

    public String getCommentParent() {
    	return document.get("parent");
    }

    /**
     * Assuming the text field is created
     */
    public void setText( String text, TextType type ) {
        logger.debug( "Setting text for " + this );

        MongoDocument texts = document.getSubDocument( TEXT_FIELD, null );
        if( texts != null && !texts.isNull() ) {
            texts.set( type.name(), text );
        } else {
            throw new IllegalStateException( TEXT_FIELD + " field was not found!" );
        }
    }

    public Comment setText(String text) {
        MongoDocument texts = document.getSubDocument( TEXT_FIELD, null );
        if( texts == null ) {
            logger.debug( "Creating text field" );
            texts = new MongoDocument();
            document.set( TEXT_FIELD, texts );
        }

        StringBuilder output = textParser.parse( text );

        // Set the version of the parser and generator
        String version = textParser.getVersion() + ":" + textParser.getGeneratorVersion();
        setTextParserVersion( version );

        setText( text, TextType.markUp );
        setText( output.toString(), TextType.html );

        return this;
    }
    
    /*
    public String getRootIdentifier() {
    	return document.get(ROOT_FIELD, "");
    }
    */
    
    public void setConversation(Conversation conversation) {
    	document.set("conversation", conversation.getIdentifier());
    }
    
    public void setConversationId(String id) {
    	document.set("conversation", id);
    }
    
    public void setConversationParent(String identifier) {
    	document.set("parent", identifier);
    }
    
    public String getConversationId() {
    	return document.get("conversation", "");
    }
    
    public List<String> getAncestors() {
    	return document.getObjectList2("ancestors");
    }
    
    public static MongoDocument getComment(String id) {
    	return commentCache.get(id);
    }

    
    /*
    public static List<Comment> getCommentsByUser(User user, int offset, int number, Node parent) {
        MongoDBQuery query = new MongoDBQuery().is( "type", "comment" ).is( "owner", user.getIdentifier() );
        MongoDocument sort = new MongoDocument().set( "created", 1 );
        List<MongoDocument> docs = MongoDBCollection.get( Core.NODES_COLLECTION_NAME ).find( query, offset, number, sort );

        List<Comment> comments = new ArrayList<Comment>( docs.size() );

        for(MongoDocument d : docs) {
            Comment c = new Comment(parent, d);
            comments.add( Core.getInstance().getTemplateManager().getRenderer( request ).renderObject( c, "view.vm" ) );
        }

        return comments;
    }
    */

    @Override
	public void save() {
    	logger.debug("Saving {}", this);
		commentCache.save(this.getDocument(), getIdentifier());
	}
    
	public static class CommentDescriptor extends NodeDescriptor<Comment> {

        public CommentDescriptor( Node parent ) {
            super( parent );
        }

        @Override
        public String getDisplayName() {
            return "Comment";
        }

        @Override
        public String getType() {
            return "comment";
        }
        
        @Override
		public String getCollectionName() {
			return Comment.COMMENTS_COLLECTION;
		}

		@Override
        public Comment newInstance( CoreRequest request, Node parent ) throws ItemInstantiationException {
            Comment comment = super.newInstance( request, parent );

            if(parent instanceof PersistedNode) {
                comment.getDocument().set( CONVERSATION_FIELD, ((AbstractNode)parent).getIdentifier() );
                //comment.getDocument().set( PARENT_FIELD, ((AbstractNode)parent).getIdentifier() );
                comment.getDocument().set( PARENT_FIELD, request.getValue("parent") );
                
                Comment commentParent = new Comment(request.getCore(), parent, getComment((String) request.getValue("parent")));
                List<String> ancestors = new ArrayList<String>(commentParent.getAncestors());
                logger.debug("Parent ancestors: {}", ancestors);
                ancestors.add(commentParent.getIdentifier());
                comment.getDocument().set("ancestors", ancestors);
            }

            return comment;
        }
    }
}
