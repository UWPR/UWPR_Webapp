/**
 * 
 */
package org.uwpr.htpasswd;

import org.apache.commons.lang.StringUtils;
import org.uwpr.data.DataURI;
import org.uwpr.data.DataURISearcher;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.User;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
/**
 * @author Mike
 *
 */
public class HTAccessFileUtils {

	// private constructor
	private HTAccessFileUtils() { }
	
	/**
	 * Removes the htaccess file in for the given URL (which must be a URL pointing to a local path)
	 * @param url
	 * @throws Exception
	 */
	public void removeHTAccessFile( URL url ) throws Exception {
		if (!url.getHost().equals( "proteomicsresource.washington.edu" ) )
			throw new Exception( "Only know how to write out htaccess files for proteomicsresource.washington.edu." );
		
		if (!url.getPath().startsWith( "/net/pr/vol1" ) )
			throw new Exception( "Only know about URL paths that start with \"/net/pr/vol1\"" );
		
		
		this.removeHTAccessFile( url.getPath() );
	}
	
	/**
	 * Removes the htaccess file in the given directory
	 * @param directory
	 * @throws Exception
	 */
	public void removeHTAccessFile( String directory ) throws Exception {
		
		// make sure we're working with the directory
		File file = new File( directory );
		if (!file.isDirectory())
			file = file.getParentFile();	
		
		File htaccess = new File( file, ".htaccess" );
		htaccess.delete();
	}
	
	/**
	 * Get an instance of this class
	 * @return
	 */
	public static HTAccessFileUtils getInstance() {
		return new HTAccessFileUtils();
	}
	
	
	/**
	 * Writes/replaces .htaccess files for all external data links associated with this project
	 * @param project
	 * @throws Exception
	 */
	public void refreshAllHTAccessFiles( Project project ) throws Exception {
		
		if (project == null)
			throw new Exception( "project is null." );
		
		// go through an write a new .htaccess file for all external data URLs for this project
		for ( DataURI uri : DataURISearcher.getInstance().searchByProject( project ) ) {
		
			// encapsulating in a try block, because I don't want to abort if one of the
			// directories no longer exists
			try {
				writeHTAccessFile( project, new URL( uri.getUri() ) );
			} catch (Exception e) { ; }
		}
				
	}
	
	/**
	 * Write the .htaccess file out for the supplied URL, providing access to all researchers in the
	 * supplied project
	 * @param project
	 * @param url
	 * @throws Exception
	 */
	public void writeHTAccessFile( Project project, URL url ) throws Exception {
		
		if (!url.getHost().equals( "proteomicsresource.washington.edu" ) )
			throw new Exception( "Only know how to write out htaccess files for proteomicsresource.washington.edu." );
		
		if (!url.getPath().startsWith( "/net/pr/vol1" ) )
			throw new Exception( "Only know about URL paths that start with \"/net/pr/vol1\"\n  This one is: " + url.getPath() );
		
		this.writeHTAccessFile( project, url.getPath() );
	}
	
	/**
	 * Write the .htaccess file out to the supplied directory, allowing all the researchers on the
	 * supplied project access to the directory  Will replace any current .htaccess file there
	 * @param project
	 * @param directory
	 * @throws Exception
	 */
	public void writeHTAccessFile( Project project, String directory ) throws Exception {
		
		if (directory == null)
			throw new Exception( "got a null directory" );
		
		if (directory.length() < 1)
			throw new Exception( "got an empty directory" );
		
		/*
		 * Format of .htaccess file:
		 * AuthUserFile /home/cc_test/public_html/.htpasswd
		 * AuthName ByPassword
		 * AuthType Basic
		 *
		 * require user john peter ben
		 */
		
		// make sure we're working with the directory
		File file = new File( directory );
		if (!file.isDirectory())
			file = file.getParentFile();	
		
		File htaccess = new File( file, ".htaccess" );
		if (htaccess.exists()) htaccess.delete();
		
		FileWriter fw = new FileWriter( htaccess );
		fw.write( "AuthType Basic\n" );
		fw.write( "AuthName \"UWPR Data Server\"\n" );
		fw.write( "AuthUserFile " + HTPasswdUtils.PASSWORD_FILE + "\n" );
		
		Collection<String> usernames = new HashSet<String>();
		try {
			User user = new User();
			user.load( project.getPI().getID() );
			usernames.add( user.getUsername() );
			user = null;
		} catch (Exception e ) { ; }

        for(Researcher researcher: project.getResearchers())
        {
            try
            {
                User user = new User();
                user.load( researcher.getID() );
                usernames.add( user.getUsername() );
            } catch (Exception ignored ) { ; }
        }

		usernames.add( "engj" );
		usernames.add( "priska");
		usernames.add( "mriffle" );
		usernames.add( "vsharma" );

		if ( usernames.size() > 0 )
			fw.write( "require user " + StringUtils.join( usernames.iterator(), " " ) + "\n" );
		else
			fw.write( "require user no_one_in_project\n" );
		
		fw.close();
	}
}
