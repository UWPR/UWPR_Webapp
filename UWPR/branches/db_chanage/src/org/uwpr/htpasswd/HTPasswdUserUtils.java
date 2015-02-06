/**
 * 
 */
package org.uwpr.htpasswd;

/**
 * @author Mike
 *
 */
public class HTPasswdUserUtils {

	// private constuctor
	private HTPasswdUserUtils() { }
	
	/**
	 * Get an instance of this class
	 * @return
	 */
	public static HTPasswdUserUtils getInstance() {
		return new HTPasswdUserUtils();
	}
	
	/**
	 * Attempts to delete the supplied username from the username/password file
	 * If the user does not exist in the file, nothing happens
	 * @param user
	 * @throws Exception If there is a problem.
	 */
	public void removeUser( String user ) throws Exception {
		Runtime.getRuntime().exec( HTPasswdUtils.HTPASSWD_PROGRAM + " -D" + HTPasswdUtils.PASSWORD_FILE + " " + user ); 
	}
	
	/**
	 * Adds the specified user to the password file.  If the user already exists in the file,
	 * this password will replace the old password
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	public void addUser( String user, String password ) throws Exception {
		this.removeUser( user );
		Runtime.getRuntime().exec( HTPasswdUtils.HTPASSWD_PROGRAM + " -b " + HTPasswdUtils.PASSWORD_FILE + " " + user + " " + password );
	}

	
	
}
