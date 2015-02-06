/* CountryBean.java
 * Created on Mar 4, 2004
 */
package org.yeastrc.utils;

/**
 * Holds data/methods for setting/getting information about a country
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 4, 2004
 *
 */
public class CountryBean {

	// The state name
	private String name;
	
	// The state code
	private String code;

	/**
	 * Set the name of the country
	 * @param arg the name of the country
	 */
	public void setName(String arg) { this.name = arg; }
	
	/**
	 * Set the country code
	 * @param arg the country code
	 */
	public void setCode(String arg) { this.code = arg; }
	
	/**
	 * Get the name of the country
	 * @return the name of the country
	 */
	public String getName() { return this.name; }
	
	/**
	 * Get the country code
	 * @return the 2 letter ISO code for the country
	 */
	public String getCode() { return this.code; }

}
