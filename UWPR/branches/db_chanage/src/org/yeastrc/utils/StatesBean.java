/* StatesBean.java
 * Created on Mar 4, 2004
 */
package org.yeastrc.utils;

import java.util.*;
import java.io.*;

/**
 * A singleton class for holding a list of US states
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 4, 2004
 *
 */
public class StatesBean {
	
	// The StatesBean single instance we're returning
	private static final StatesBean INSTANCE = new StatesBean();
	
	// The name of the text file containing the state list
	private static final String STATE_FILE = "states.txt";
	
	// The list of states
	private List<StateBean> stateList;

	/**
	 * Get the instance of this bean.
	 * @return A StatesBean object, populated with StateBeans
	 */
	public static StatesBean getInstance() {
		return INSTANCE;
	}

	// Our private constructor, to ensure this stays a singleton
	private StatesBean() {
		this.stateList = new ArrayList<StateBean>();
		this.loadStates();
	}
	
	// Load the states
	private void loadStates() {
		
		// Read in the States file and populate the state list
		try {
			InputStream is = getClass().getResourceAsStream(StatesBean.STATE_FILE);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line;
			
			while ((line = in.readLine()) != null) {
				if (line.startsWith("#")) continue;
				
				String[] vals = line.split("\t");
				if (vals.length != 2) continue;
				
				StateBean newState = new org.yeastrc.utils.StateBean();
				newState.setName(vals[0]);
				newState.setCode(vals[1]);
				
				this.stateList.add(newState);
			}
			
			in.close();
		}
		catch (Exception e) { ; }
	}
	
	/**
	 * Will return the list of states as a list of State objects
	 * @return A list of States
	 */
	public List<StateBean> getStates() {
		return stateList;
	}

}
