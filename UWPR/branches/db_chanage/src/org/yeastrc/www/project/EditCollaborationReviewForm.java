/**
 * EditCollaborationReviewForm.java
 * @author Vagisha Sharma
 * Mar 31, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.CollaborationStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class EditCollaborationReviewForm  extends ActionForm {

    private int ID; // projectId
    private String title;
    
    private int reviewerId;
    private String reviewerName;
    private CollaborationStatus recommend;
    private String[] rejectCauses;
    private String comments;
    private String emailComments;

    private String collaborationGroups;
    private String piName;
    private List<String> researchers = new ArrayList<String>();
    private String scientificQuestion;
    private String projectAbstract;
    
    private int ltqRunsRequested = 0;
    private int ltq_etdRunsRequested = 0;
    private int ltq_orbitrapRunsRequested = 0;
    private int ltq_ftRunsRequested = 0;
    private int tsq_accessRunsRequested = 0;
    private int tsq_vantageRunsRequested = 0;
    
    private String fragmentationTypes;
    
    private boolean databaseSearchRequested;
    private boolean massSpecExpertiseRequested = true;
    
    private String extensionReason = null;
    private int parentProject = 0;
    
    private boolean draft = false;


    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();
        
        // validate only if this is not a draft
        if(!draft) {
            // Make sure at least one status is selected
            if(recommend == null) {
                errors.add("project", new ActionMessage("error.project.noStatus"));
            }
            // If the status recommended by the reviewer is "Reject" make sure 
            // at least one rejection reason is selected
            if(recommend != null && recommend == CollaborationStatus.REJECTED) {
                if(rejectCauses == null || rejectCauses.length == 0) {
                    errors.add("project", new ActionMessage("error.project.noRejectionCause"));
                }
            }   
            // If the status recommended by the reviewer is "Revise" make sure that 
            // comments have been entered
            if(recommend != null && recommend == CollaborationStatus.REVISE) {
                if(emailComments == null || emailComments.length() == 0) {
                    errors.add("project", new ActionMessage("error.project.noEmailComments"));
                }
            }
        }
        
        return errors;
    }

    
    public String[] getReviewerRejectionCauseIds() {
        
        return this.rejectCauses;
    }
    
    public void setReviewerRejectionCauseIds(String[] causeList) {
        this.rejectCauses = causeList;
    }
    
    /** Gets the short name for the status of this collaboration */
    public String getRecommendedStatusShortName() {
        if (recommend != null)
            return recommend.getShortName();
        else
            return "";
    }
    
    public CollaborationStatus getRecommendedStatus() {
        return this.recommend;
    }
    
    /** Set the status for this collaboration based on the given short name for the status*/
    public void setRecommendedStatusShortName(String shortName) {
        this.recommend = CollaborationStatus.statusForString(shortName);
    }
    
    /** Set the status for this collaboration */
    public void setRecommendedStatus(CollaborationStatus status) {
        this.recommend = status;
    }
    
    public int getReviewerId() {
        return reviewerId;
    }


    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerComments() {
        return comments;
    }

    public void setReviewerComments(String comments) {
        this.comments = comments;
    }
    
    public String getEmailComments() {
        return emailComments;
    }
    public void setEmailComments(String emailComments) {
        this.emailComments = emailComments;
    }


    public void setID(int id) {
        this.ID = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getID() {
        return this.ID;
    }
    public String getTitle() {
        return this.title;
    }
    
    /** Set the PI ID */
    public void setPI(String piName) { this.piName = piName; }
    
    public String getPI() { return this.piName; }
    
    /** Set the abstract */
    public void setAbstract(String arg) { this.projectAbstract = arg; }
    
    public String getAbstract() { return this.projectAbstract; }


    public String getCollaborationGroups() {
        return collaborationGroups;
    }
    public void setCollaborationGroups(String collaborationGroups) {
        this.collaborationGroups = collaborationGroups;
    }


    //----------------------------------------------------------------
    // Researchers
    //----------------------------------------------------------------
    public String getResearcher(int index) {
        //System.out.println("Getting researcher id at index: "+index);
        while(index >= researchers.size())
            researchers.add(new String());
        return researchers.get(index);
    }

    public List<String> getResearcherList() {
        //System.out.println("Getting researcher list");
        List<String> rList = new ArrayList<String>();
        for(String r: researchers) {
            if(!StringUtils.isBlank(r))
                rList.add(r);
        }
        return rList;
    }

    public void setResearcherList(List<String> researchers) {
        //System.out.println("Setting researcher");
        this.researchers = researchers;
    }

    public String getScientificQuestion() {
        return scientificQuestion;
    }
    public void setScientificQuestion(String scientificQuestion) {
        this.scientificQuestion = scientificQuestion;
    }
    
    
    public int getLtq_etdRunsRequested() {
        return ltq_etdRunsRequested;
    }
    public void setLtq_etdRunsRequested(int ltq_etdRunsRequested) {
        this.ltq_etdRunsRequested = ltq_etdRunsRequested;
    }

    public int getLtq_ftRunsRequested() {
        return ltq_ftRunsRequested;
    }
    public void setLtq_ftRunsRequested(int ltq_ftRunsRequested) {
        this.ltq_ftRunsRequested = ltq_ftRunsRequested;
    }

    public int getLtq_orbitrapRunsRequested() {
        return ltq_orbitrapRunsRequested;
    }
    public void setLtq_orbitrapRunsRequested(int ltq_orbitrapRunsRequested) {
        this.ltq_orbitrapRunsRequested = ltq_orbitrapRunsRequested;
    }

    public int getLtqRunsRequested() {
        return ltqRunsRequested;
    }
    public void setLtqRunsRequested(int ltqRunsRequested) {
        this.ltqRunsRequested = ltqRunsRequested;
    }

    public int getTsq_accessRunsRequested() {
        return tsq_accessRunsRequested;
    }
    public void setTsq_accessRunsRequested(int tsq_accessRunsRequested) {
        this.tsq_accessRunsRequested = tsq_accessRunsRequested;
    }
    
    public int getTsq_vantageRunsRequested() {
        return tsq_accessRunsRequested;
    }
    public void setTsq_vantageRunsRequested(int tsq_accessRunsRequested) {
        this.tsq_accessRunsRequested = tsq_accessRunsRequested;
    }
    
    public String getFragmentationTypes() {
        return fragmentationTypes;
    }
    public void setFragmentationTypes(String fragmentationTypes) {
        this.fragmentationTypes = fragmentationTypes;
    }
    
    public boolean isDatabaseSearchRequested() {
        return databaseSearchRequested;
    }
    public void setDatabaseSearchRequested(boolean databaseSearchRequested) {
        this.databaseSearchRequested = databaseSearchRequested;
    }
    
    public boolean isMassSpecExpertiseRequested() {
        return massSpecExpertiseRequested;
    }
    public void setMassSpecExpertiseRequested(boolean massSpecExpertiseRequested) {
        this.massSpecExpertiseRequested = massSpecExpertiseRequested;
    }
    
    public String getExtensionReasons() {
        return extensionReason;
    }
    public void setExtensionReasons(String extensionReason) {
        this.extensionReason = extensionReason;
    }

    public int getParentProject() {
        return parentProject;
    }
    public void setParentProject(int parentProject) {
        this.parentProject = parentProject;
    }
    public boolean isDraft() {
        return draft;
    }
    public void setDraft(boolean isDraft) {
        this.draft = isDraft;
    }
}
