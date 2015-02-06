package org.yeastrc.project;

import java.util.ArrayList;
import java.util.List;

public class ProjectReviewer {

    private Researcher researcher;
    private int projectId;
    private CollaborationStatus recommendedStatus;
    private String comment;
    private String emailComment;
    
    // If the reviewer recommended to reject the project what were the causes
    private List<CollaborationRejectionCause> rejectionCauses = new ArrayList<CollaborationRejectionCause>();
    
    public ProjectReviewer() {
        rejectionCauses = new ArrayList<CollaborationRejectionCause>();
    }
    
    public Researcher getResearcher() {
        return researcher;
    }
    public void setResearcher(Researcher researcher) {
        this.researcher = researcher;
    }
    public int getProjectId() {
        return projectId;
    }
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    public CollaborationStatus getRecommendedStatus() {
        return recommendedStatus;
    }
    public String getRecommendedStatusShortName() {
        if(recommendedStatus == null)
            return null;
        return recommendedStatus.getShortName();
    }
    public void setRecommendedStatus(CollaborationStatus recommendedStatus) {
        this.recommendedStatus = recommendedStatus;
    }
    public void setRecommendedStatusShortName(String shortName) {
        this.recommendedStatus = CollaborationStatus.statusForString(shortName);
    }
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getReviewerCommentsAsHTML() {
        return org.yeastrc.utils.HTML.convertToHTML(this.getComment());
    }
    public String getEmailComment() {
        return emailComment;
    }

    public void setEmailComment(String emailComment) {
        this.emailComment = emailComment;
    }
    public String getReviewerEmailCommentsAsHTML() {
        return org.yeastrc.utils.HTML.convertToHTML(this.getEmailComment());
    }
    /**
     * Returns the list of reason if the reviewer rejected the project
     * @return
     */
    public List<CollaborationRejectionCause> getRejectionCauses() {
        return rejectionCauses;
    }

    /**
     * Set the list of reasons if the reviewer rejected the project.
     * @param rejectionCauseIds
     */
    public void setRejectionCauses(List<CollaborationRejectionCause> rejectionCauses) {
        this.rejectionCauses = rejectionCauses;
    }
    
    
    public List<Integer>getRejectionCauseIds() {
        if(rejectionCauses == null)
            return new ArrayList<Integer>(0);
        List<Integer> causeIds = new ArrayList<Integer>(rejectionCauses.size());
        for(CollaborationRejectionCause cause: rejectionCauses) {
            causeIds.add(cause.getId());
        }
        return causeIds;
    }
    
    public String getRejectionCauseString() {
        if(rejectionCauses.size() == 0)
            return null;
        String causeList = "";
        for(CollaborationRejectionCause cause: rejectionCauses) {
            causeList += ", "+cause.getCause();
        }
        if(causeList.length() > 0)
            causeList = causeList.substring(2);
        return causeList;
    }
    
    public boolean isReviewSubmitted() {
    	return (recommendedStatus == CollaborationStatus.ACCEPTED ||
    			recommendedStatus == CollaborationStatus.REJECTED ||
    			recommendedStatus == CollaborationStatus.REVISE);
    }
}
