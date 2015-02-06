/**
 * WordCounter.java
 * @author Vagisha Sharma
 * Apr 8, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import java.util.regex.Pattern;


/**
 * 
 */
public class WordCounter {

	private static final Pattern wordPattern = Pattern.compile("[\\s]+");
	
    public static int count(String text) {
    	
    	if(text == null)
    		return 0;
    	
    	text = text.trim();
    	if(text.length() == 0)
    		return 0;
    	
    	// remove all full-stops, commas and semi-colons
    	text = text.replaceAll("[,|;|\\.]", "");
    	//System.out.println("'"+text+"'");
    	
    	if(text.length() == 0)
    		return 0;
    	
    	String[] tokens = wordPattern.split(text.trim());
    	return tokens.length;
    }
    
    public static void main(String[] args) {
        String  text = ", , , ... 1234;";
        System.out.println(WordCounter.count(text));
        
        text = "Here is some progres's. Here is some progress. Here is some progress.  Here is some progress. Here is some;";
        System.out.println(WordCounter.count(text));
        
        text = ".";
        System.out.println(WordCounter.count(text));
        
        text = "";
        System.out.println(WordCounter.count(text));
    }
}
