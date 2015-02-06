/**
 * 
 */
package org.yeastrc.www.project;

import java.sql.SQLException;
import java.util.List;

import org.uwpr.instrumentlog.ProjectInstrumentUsageDAO;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.ProjectReviewer;
import org.yeastrc.project.ProjectReviewerDAO;

/**
 * ProjectEditableChecker.java
 * @author Vagisha Sharma
 * Jul 16, 2010
 * 
 */
public class ProjectEditableChecker {

	public static final int NOT_EDITABLE = 1;
	public static final int FULL_EDITABLE = 2;
	public static final int PART_EDITABLE = 3;
	
	private ProjectEditableChecker () {}
	
	public static int getEditStatus(Collaboration project) throws SQLException {
		
        CollaborationStatus projectStatus = ((Collaboration)project).getCollaborationStatus();
        
        // If the project is REVISE only instrument usage fields 
    	// (in addition to comments) are editable
    	if(projectStatus == CollaborationStatus.REVISE) {
    		return PART_EDITABLE;
    	}
    	
    	else if (projectStatus == CollaborationStatus.REVISE_PENDING) {
    		return NOT_EDITABLE;
    	}
        
        else if (projectStatus == CollaborationStatus.PENDING) {
        	
        	List<ProjectReviewer> reviewers = ProjectReviewerDAO.instance().getProjectReviewers(project.getID());
        	
        	// If the project is PENDING and none of the reviewers have submitted reviews
        	// the project is fully editable, otherwise it is not editable
        	boolean reviewed = false;
        	for(ProjectReviewer reviewer: reviewers) {
        		if(reviewer.isReviewSubmitted()) {
        			reviewed = true;
        			break;
        		}
        	}
        	if(reviewed) {
        		return NOT_EDITABLE;
        	}
        	else {
        		return FULL_EDITABLE;
        	}
        }
        else { // NOT (PENDING, REVISE or REVISE_PENDING)
        	return NOT_EDITABLE; // Only the comments, publications and progress fields are editable
        }
	}
	
	public static int getEditStatus(BilledProject project) throws SQLException {
		
		// If any instrument time has been scheduled for this project the Affiliation and "Mass Spec. Analysis performed by..." fields
		// should not be editable
		int blockCount = ProjectInstrumentUsageDAO.getInstance().getUsageBlockCountForProject(project.getID());
		if(blockCount > 0) {
			return PART_EDITABLE;
		}
		else {
			return FULL_EDITABLE;
		}
	}
	
}
