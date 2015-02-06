/**
 * CollaborationAndReview.java
 * @author Vagisha Sharma
 * Mar 27, 2009
 * @version 1.0
 */
package org.yeastrc.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 */
public class CollaborationAndReview implements ComparableCollaboration{

    private Collaboration collaboration;
    private List<ProjectReviewer> reviewers;
    
    public CollaborationAndReview() {
        reviewers = new ArrayList<ProjectReviewer>();
    }

    public Collaboration getCollaboration() {
        return collaboration;
    }

    public void setCollaboration(Collaboration collaboration) {
        this.collaboration = collaboration;
    }

    public List<ProjectReviewer> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<ProjectReviewer> reviewers) {
        this.reviewers = reviewers;
    }
    
    public String getReviewerNames() {
        String names = "";
        for(ProjectReviewer reviewer: reviewers) {
            names += ", "+reviewer.getResearcher().getLastName();
        }
        if(names.length() > 0)
            names = names.substring(2); // remove the first comma and space
        return names;
    }
    
    public Researcher getPI() {
        return collaboration.getPI();
    }
    
    public java.util.Date getLastChange() {
        return collaboration.getLastChange();
    }

    public int getID() {
        return collaboration.getID();
    }

    public Date getSubmitDate() {
        return collaboration.getSubmitDate();
    }

    public String getTitle() {
        return collaboration.getTitle();
    }

    public CollaborationStatus getCollaborationStatus() {
        return collaboration.getCollaborationStatus();
    }
}
