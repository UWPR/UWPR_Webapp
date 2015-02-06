/**
 * ProjectRawFileUsage.java
 * @author Vagisha Sharma
 * Jan 13, 2009
 * @version 1.0
 */
package org.uwpr.instrumentlog.rawfile;

/**
 * 
 */
public class ProjectRawFileUsage {

    private int projectID;
    private int rawFileCount;
    private float rawFileSize;
    private String dataDirectory;
    
    public int getProjectID() {
        return projectID;
    }
    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }
    public int getRawFileCount() {
        return rawFileCount;
    }
    public void setRawFileCount(int rawFileCount) {
        this.rawFileCount = rawFileCount;
    }
    public float getRawFileSize() {
        return rawFileSize;
    }
    public void setRawFileSize(float rawFileSize) {
        this.rawFileSize = rawFileSize;
    }
    public String getDataDirectory() {
        return dataDirectory;
    }
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }
}
