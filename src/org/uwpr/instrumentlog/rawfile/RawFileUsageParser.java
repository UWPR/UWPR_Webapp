/**
 * RawFileUsageParser.java
 * @author Vagisha Sharma
 * Jan 13, 2009
 * @version 1.0
 */
package org.uwpr.instrumentlog.rawfile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 
 */
public class RawFileUsageParser {

    private static final Logger log = LogManager.getLogger(RawFileUsageParser.class.getName());
    
    private static final RawFileUsageParser parser = new RawFileUsageParser();
    
    // Example: Fri Aug  7 00:00:03 PDT 2009
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
    
    private RawFileUsageParser() {}
    
    public static RawFileUsageParser instance() {
        return parser;
    }
    
    public List<ProjectRawFileUsage> parse(String filePath) throws Exception  {
        List<ProjectRawFileUsage> usageList = new ArrayList<ProjectRawFileUsage>();
        
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader(new FileReader(filePath));
            
            boolean start = false;
            String line = reader.readLine();
            // First line is the date
            ProjectRawFileUsageUtils.setLastDateParsed(getDate(line.trim(), filePath));
            
            while((line = reader.readLine()) != null) {
                
                if(line.startsWith("year")) {
                    start = true;
                    continue;
                }
                if(start) {
                    line = line.trim();
                    if(line.length() == 0) // this must be the end of useful information
                        break;
                    String[] tokens = line.split("\\s+");
                    if(tokens.length != 6) {
                        throw new Exception("Require 6 columns.\n\t"+line);
                    }
                    ProjectRawFileUsage usage = new ProjectRawFileUsage();
                    try {
                        usage.setProjectID(Integer.parseInt(tokens[1]));
                        usage.setRawFileCount(Integer.parseInt(tokens[3]));
                        usage.setRawFileSize(Float.parseFloat(tokens[4]));
                        usage.setDataDirectory(tokens[5]);
                    }
                    catch (NumberFormatException e) {
                        throw new Exception("Error parsing numbers.\n\t"+line);
                    }
                    usageList.add(usage);
                }
            }
        }
        catch(IOException e) {
           throw e;
        }
        finally {
            if(reader != null) try {
                reader.close();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return usageList;
    }

    private Date getDate(String line, String filePath) {
        try {
            return dateFormat.parse(line);
        }
        catch (ParseException e) {
            log.error("Error parsing date in file: "+filePath, e);
            return null;
        }
    }
}
