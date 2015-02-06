/**
 * ComparableProject.java
 * @author Vagisha Sharma
 * Mar 27, 2009
 * @version 1.0
 */
package org.yeastrc.project;

/**
 * 
 */
public interface ComparableProject {

    public abstract Researcher getPI();
    
    public abstract int getID();
    
    public abstract java.util.Date getSubmitDate();
    
    public abstract java.util.Date getLastChange();
    
    public abstract String getTitle();
   
}
