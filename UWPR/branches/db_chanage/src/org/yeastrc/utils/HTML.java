/* HTML.java
 * Created on Mar 16, 2004
 */
package org.yeastrc.utils;

/**
 * Add one sentence class summary here.
 * Add class description here
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 16, 2004
 *
 */
public class HTML {
	
	/**
	 * Purpose is to take a string and convert it so that it's suitable for display on
	 * an HTML pages.  This will essentially make line breaks appear as they
	 * are in the actual string.
	 * @param arg The string to convert
	 * @return The converted string
	 */
	public static String convertToHTML(String arg) {
		String retStr = null;
		if (arg == null) return retStr;
		
		retStr = arg;

		// Replace < and >
		retStr = retStr.replaceAll("<", "&lt;");
		retStr = retStr.replaceAll(">", "&gt;");
		
		// Replace newlines with <BR>
		retStr = retStr.replaceAll("\r\n", "<BR>");		// PC version
		retStr = retStr.replaceAll("\n", "<BR>");		// UNIX version
		retStr = retStr.replaceAll("\r", "<BR>");		// MAC version

		retStr = retStr.replaceAll("\\\\r\\\\n", "<BR>");
		retStr = retStr.replaceAll("\\\\n", "<BR>");
		retStr = retStr.replaceAll("\\\\r", "<BR>");
		
		return retStr;
	}


}
