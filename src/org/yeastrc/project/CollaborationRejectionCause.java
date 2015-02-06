/**
 * CollaborationRejectionCause.java
 * @author Vagisha Sharma
 * Feb 19, 2009
 * @version 1.0
 */
package org.yeastrc.project;

/**
 * 
 */
public class CollaborationRejectionCause {

    private int id;
    private String cause;
    private String description;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getCause() {
        return cause;
    }
    public void setCause(String cause) {
        this.cause = cause;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
