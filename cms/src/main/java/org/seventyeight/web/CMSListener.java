package org.seventyeight.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.seventyeight.database.DatabaseException;

import javax.servlet.annotation.WebListener;
import java.io.File;

/**
 * @author cwolfgang
 */
@WebListener
public class CMSListener extends DatabaseContextListener<CMSCore> {

    private static Logger logger = LogManager.getLogger( CMSListener.class );

    public CMSListener() {
        //extraTemplatePaths.add( "WEB-INF/classes/templates" );
    }

    @Override
    protected void install() throws DatabaseException {
    }

    @Override
    public CMSCore getCore( File path, String dbname ) throws CoreException {
        return new CMSCore( path, dbname );
    }
}