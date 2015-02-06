/**
 * 
 */
package org.uwpr.data;

/**
 * @author Mike
 *
 */
public class URIUtils {

	/**
	 * Get the parent directory of a uri string.  eg:  a uri of http://foo.com/my/sharona/ would return http://foo.com/my/
	 * @param uri
	 * @return
	 */
	public static String getParentDirectory( String uri ) {
		if ( uri == null) return null;															// yeah, can't be null
		if ( !uri.startsWith( "http://" ) && !uri.startsWith( "https://" ) ) return null;		// not a valid URI according to my rules
		
		// chop off the trailing /
		if ( uri.endsWith( "/" ) )
			uri = uri.substring(0, uri.length() - 1 );
		
		// http://foo.com would split into { "http:", "", "foo.com" }, which means if we're at the top most directory, the split would contain 3 fields/
		String[] fields = uri.split( "/" );
		if (fields.length <= 3) return null;
		
		StringBuffer newuri = new StringBuffer();
		for (int i = 0; i < fields.length - 2; i++) {
			newuri.append( fields[i] + "/" );
		}

		return newuri.toString();
	}
	
}
