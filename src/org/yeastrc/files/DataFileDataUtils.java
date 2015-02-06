package org.yeastrc.files;

import java.util.HashMap;
import java.util.Map;

public class DataFileDataUtils {

	public static String PROJECT = "project";
		
	private static Map<String, String> typeLocationMap;
	public static Map<String, String> getTypeLocationMap() {
		
		if( typeLocationMap == null ) {
			typeLocationMap = new HashMap<String,String>();
			typeLocationMap.put( "project", "projectFiles" );
		}
		
		return typeLocationMap;
	}

	private static Map<String, String> typeColumnMap;
	public static Map<String, String> getTypeColumnMap() {
		
		if( typeColumnMap == null ) {
			typeColumnMap = new HashMap<String,String>();
			typeColumnMap.put( "project", "project_id" );
		}
		
		return typeColumnMap;
	}
	
	
}
