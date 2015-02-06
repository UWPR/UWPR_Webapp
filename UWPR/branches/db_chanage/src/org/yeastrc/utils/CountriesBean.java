/* CountriesBean.java
 * Created on Mar 4, 2004
 */
package org.yeastrc.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A singleton class containing a list of countries
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 4, 2004
 *
 */
public class CountriesBean {

	// The CountriesBean single instance we're returning
	private static final CountriesBean INSTANCE = new CountriesBean();
	
	// The name of the text file containing the country list
	private static final String COUNTRY_FILE = "countries.txt";
	
	// The list of countries
	private List<CountryBean> countryList;

	/**
	 * Get the instance of this bean.
	 * @return A CountriesBean object, populated with CountryBeans
	 */
	public static CountriesBean getInstance() {
		return INSTANCE;
	}

	// Our private constructor, to ensure this stays a singleton
	private CountriesBean() {
		this.countryList = new ArrayList<CountryBean>();
		this.loadCountries();
	}
	
	// Load the states
	private void loadCountries() {
		
		// Read in the Countries file and populate the state list
		try {
			InputStream is = getClass().getResourceAsStream(CountriesBean.COUNTRY_FILE);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line;
			
			while ((line = in.readLine()) != null) {
				if (line.startsWith("#")) continue;
				
				String[] vals = line.split("\t");
				if (vals.length != 2) continue;
				
				CountryBean newCountry = new CountryBean();
				newCountry.setName(vals[1]);
				newCountry.setCode(vals[0]);
				
				this.countryList.add(newCountry);
			}
			
			in.close();
		}
		catch (Exception e) { ; }
	}
	
	/**
	 * Will return the list of countries as a list of Country objects
	 * @return A list of Countries
	 */
	public List<CountryBean> getCountries() {
		return countryList;
	}

}
