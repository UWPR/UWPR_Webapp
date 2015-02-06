/* StateBean.java
 * Created on Mar 4, 2004
 */
package org.yeastrc.utils;

/**
 * Holds data/methods for setting/getting information about a US state
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 4, 2004
 *
 */
public class StateBean {

	// The state name
	private String name;
	
	// The state code
	private String code;

	/**
	 * Set the name of the state
	 * @param arg the name of the state
	 */
	public void setName(String arg) { this.name = arg; }
	
	/**
	 * Set the state code
	 * @param arg the state code
	 */
	public void setCode(String arg) { this.code = arg; }
	
	/**
	 * Get the name of the state
	 * @return the name of the state
	 */
	public String getName() { return this.name; }
	
	/**
	 * Get the state code
	 * @return the 2 letter code for the state
	 */
	public String getCode() { return this.code; }

}
