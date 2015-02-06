/**
 * 
 */
package org.uwpr.htpasswd;

/**
 * @author Mike
 *
 */
public class HTPasswdUtils {

	/** Location of username / password file for apache access control **/
	public static final String PASSWORD_FILE = "/net/pr/vol1/ProteomicsResource/pr_passwords";
	//public static final String PASSWORD_FILE = "/nfs/vol1/ProteomicsResource/pr_passwords";
	//public static final String PASSWORD_FILE = "/home/mriffle/pr_passwords";
	
	/** Location of the htpasswd executable for manging the password file **/
	public static final String HTPASSWD_PROGRAM = "/usr/bin/htpasswd";
	
}
