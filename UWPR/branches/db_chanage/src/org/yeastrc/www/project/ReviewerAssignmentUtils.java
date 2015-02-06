package org.yeastrc.www.project;

import org.apache.log4j.Logger;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.ProjectReviewer;
import org.yeastrc.project.ProjectReviewerDAO;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.user.Groups;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ReviewerAssignmentUtils {

    private static final Logger log = Logger.getLogger(ReviewerAssignmentUtils.class);
    
    private ReviewerAssignmentUtils(){}

    private static List<Researcher> reviewers;
    
    private static class ReviewerOrder implements Comparable<ReviewerOrder>{
        private Researcher reviewer;
        private int lastReviewedProjectId;
        
        public int compareTo(ReviewerOrder o) {
        	if(o.lastReviewedProjectId == this.lastReviewedProjectId) {
        		// randomly select one
        		if(Math.random() > 0.5)
        			return 1;
        		else
        			return -1;
        	}
        	else
        		return Integer.valueOf(this.lastReviewedProjectId).compareTo(o.lastReviewedProjectId);
        }
        
        public String toString() {
            return reviewer.getLastName()+" "+lastReviewedProjectId;
        }
    }
    
    private static boolean reviewersChanged(List<Integer> currentReviewers) {
        if(reviewers == null) return true;
        if(reviewers.size() != currentReviewers.size())   return true;
        
        List<Integer> myIds = new ArrayList<Integer>(reviewers.size());
        for(Researcher r: reviewers) {
            myIds.add(r.getID());
        }
        for(Integer reviewer: currentReviewers) 
            if(!myIds.contains(reviewer))        return true;
        return false;
    }
    
    private static void getReviewers(List<Integer> currentReviewerIds) {
        if(reviewers == null) {
            reviewers = new ArrayList<Researcher>();
        }
        reviewers.clear();
        for(Integer id: currentReviewerIds) {
            Researcher r = new Researcher();
            try {
                r.load(id);
            }
            catch (InvalidIDException e) {
                log.warn("Researcher not found with ID: "+id);
                continue;
            }
            catch (SQLException e) {
               log.warn("Error getting researcher with ID: "+id);
               continue;
            }
            reviewers.add(r);
        }
    }
        
    public static List<Researcher> assignReviewer(Collaboration collaboration) throws ReviewerAssignmentException {

        List<Researcher> reviewers = getReviewerAssignment(collaboration);
        if(reviewers.size() == 0) {
        	log.fatal("Error getting reviewer assignments for collaboration: "+collaboration.getID());
            throw new ReviewerAssignmentException("Error getting reviewer assignments for collaboration: "+collaboration.getID());
        }
        
        ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
        for(Researcher researcher: reviewers) {
            ProjectReviewer reviewer = new ProjectReviewer();
            reviewer.setProjectId(collaboration.getID());
            reviewer.setResearcher(researcher);
            try {
                prDao.saveProjectReviewer(reviewer);
            }
            catch (SQLException e) {
                throw new ReviewerAssignmentException("Error saving project reviewer. ProjectID: "+reviewer.getProjectId()+
                        " ResearcherID: "+reviewer.getResearcher().getID(), e);
            }
            log.info("Assigned reviewer: "+researcher.getFirstName()+" "+researcher.getLastName()+" to collaboration: "+collaboration.getTitle());
            
        }
        return reviewers;
    }

    private static List<Researcher> getReviewerAssignment(Collaboration collaboration) {
    	
    	// Get the LRU order of current reviewers
    	List<ReviewerOrder> revOrderList = getReviewerLruOrder();
        if(revOrderList == null)
        	return new ArrayList<Researcher>(0);
        
    	// If this is an extension project assign one of the original reviewers
//    	if(collaboration.getParentProjectID() > 0) {
//    		List<Researcher> extReviewers = getExtensionProjectReviewers(collaboration, revOrderList);
//    		// If no reviewers were found for the parent project we will assign new reviewers.
//    		if(extReviewers != null && extReviewers.size() > 0)
//    			return extReviewers;
//    	}
    	
    	// This is a new project we will assign two reviewers.
        List<String> projectGroups;
        try {
            projectGroups = getProjectResearcherGroups(collaboration);
        }
        catch (SQLException e1) {
            log.error("Error getting project researcher IDs");
            return new ArrayList<Researcher>(0);
        }

        ReviewerOrder assigned1 = getReviewer(revOrderList, projectGroups, -1, collaboration);
        if(assigned1 == null) {
        	log.error("First reviewer is null or project: "+collaboration.getID());
        	try {
                ReviewerEmailUtils.emailVsharmaNoReviewersFound(collaboration);
            }
            catch(Exception e) {log.error("Error sending mail to vsharma; No reviewer pair found");}
        	return new ArrayList<Researcher>(0);
        }
        ReviewerOrder assigned2 = getReviewer(revOrderList, projectGroups, assigned1.reviewer.getID(), collaboration);
        if(assigned2 == null) {
        	log.error("Second reviewer is null or project: "+collaboration.getID());
        	try {
                ReviewerEmailUtils.emailVsharmaNoReviewersFound(collaboration);
            }
            catch(Exception e) {log.error("Error sending mail to vsharma; No reviewer pair found");}
        	return new ArrayList<Researcher>(0);
        }
        
        List<Researcher> reviewers = new ArrayList<Researcher>(2);
        reviewers.add(assigned1.reviewer);
        reviewers.add(assigned2.reviewer);
        return reviewers;
        
    }

	private static List<ReviewerOrder> getReviewerLruOrder() {
		
		ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
    	
    	// refresh the list of current reviewers.
    	List<Integer> reviewerIds = null;
        try {
            reviewerIds = getReviewerIds();
        }
        catch (SQLException e) {
            log.error("Error getting reviewer IDs.", e);
            return null;
        }

        if (reviewerIds.size() == 0)
            return null;
        
        if(reviewersChanged(reviewerIds)) {
        	getReviewers(reviewerIds);
        }
        
        // order the reviewers by last reviewed project ID
        List<ReviewerOrder> revOrderList = new ArrayList<ReviewerOrder>(reviewerIds.size());
        
        for(Researcher reviewer: reviewers) {
        	ReviewerOrder revOrder = new ReviewerOrder();
        	revOrder.reviewer = reviewer;
        	
        	// set the last reviewed projectId
        	int projectId = 0;
        	try {
                projectId = prDao.getLastProjectReviewedBy(reviewer.getID());
                revOrder.lastReviewedProjectId = projectId;
            }
            catch(SQLException e) {
                log.error("Error getting last project assigned reviewer ID: "+reviewer.getID(), e);
                return null;
            }
            revOrderList.add(revOrder);
        }
        
        Collections.sort(revOrderList);
        for(ReviewerOrder rev: revOrderList) {
            log.info(rev.toString());
        }
		return revOrderList;
	}

/*
 * Commented out since we are no longer assigning one of the original reviewers to extension projects.
 */
//	private static List<Researcher> getExtensionProjectReviewers(Collaboration collaboration, List<ReviewerOrder> revOrderList) {
//		
//		List<Integer> parentReviewerIds = getParentProjectReviewers(collaboration);
//	
//		if(parentReviewerIds == null || parentReviewerIds.size() == 0) {
//			log.warn("NO Reviewers found for parent project: "+collaboration.getParentProjectID());
//			return null;
//			// If no reviewers were found for the parent project it is possible that this is an old project
//			// that had no reviewers. We will assign new reviewers.
//		}
//		else {
//			
//			for(ReviewerOrder reviewer: revOrderList) {
//				if(parentReviewerIds.contains(reviewer.reviewer.getID())) {
//					// assign a single reviewer
//					List<Researcher> researchers = new ArrayList<Researcher>(1);
//					researchers.add(reviewer.reviewer);
//					return researchers;
//				}
//			}
//		}
//		
//		// We couldn't assign a reviewer.  It is possible that the parent project reviewers are no longer in the reviewer pool.
//		log.warn("Reviewers of parent project are no longer in the reviewer pool. Parent project: "+collaboration.getID());
//		return null;
//	}
    
	/*
	 * Commented out since we are no longer assigning one of the original reviewers to extension projects.
	 */
//    private static List<Integer> getParentProjectReviewers(
//			Collaboration collaboration) {
//		
//    	int parentProjectId = collaboration.getParentProjectID();
//    	
//    	// Load our project
//		Collaboration project;
//		
//		try {
//			project = (Collaboration)(ProjectFactory.getProject(parentProjectId));
//		} catch (InvalidProjectTypeException e) {
//			log.error("Invalid project type (ID "+parentProjectId+"): "+e.getMessage(), e);
//			return new ArrayList<Integer>(0);
//		} catch (SQLException e) {
//			log.error("Error loading project (ID "+parentProjectId+"): "+e.getMessage(), e);
//			return new ArrayList<Integer>(0);
//		} catch (InvalidIDException e) {
//			log.error("Invalid project ID (ID "+parentProjectId+"): "+e.getMessage(), e);
//			return new ArrayList<Integer>(0);
//		}
//		
//		if(project == null) {
//			log.error("No project found (ID "+parentProjectId+")");
//			return new ArrayList<Integer>(0);
//		}
//		
//		// Get the reviewers for this project
//		ProjectReviewerDAO prDao = ProjectReviewerDAO.instance();
//		List<ProjectReviewer> reviewers = null;
//		try {
//			reviewers = prDao.getProjectReviewers(parentProjectId);
//		} catch (SQLException e) {
//			log.error("Error loading reviewers for project (ID "+parentProjectId+"): "+e.getMessage(), e);
//			return new ArrayList<Integer>(0);
//		}
//		
//		if(reviewers == null || reviewers.size() == 0) {
//			log.error("No reviewers found for project (ID "+parentProjectId+")");
//			return new ArrayList<Integer>(0);
//		}
//		List<Integer> researchers = new ArrayList<Integer>(reviewers.size());
//		for(ProjectReviewer reviewer: reviewers) {
//			researchers.add(reviewer.getResearcher().getID());
//		}
//		
//		return researchers;
//	}

	private static ReviewerOrder getReviewer(List<ReviewerOrder> orderList, List<String> projectGroups, int excludeReasearcherId, Collaboration collaboration) {
    	
    	for(ReviewerOrder reviewer: orderList) {
            try {
                if(isReviewerInProjectGroup(reviewer.reviewer.getID(), projectGroups))
                    continue;
                else if(reviewer.reviewer.getID() == excludeReasearcherId)
                	continue;
                else {
                   log.info("Found reviewer: "+reviewer.toString()+" with NO conflicts for project: "+collaboration.getID());
                   return reviewer;
                }
            }
            catch (SQLException e) {
                log.error("Error determining if reviewer is in the same group(s) as the project researhers");
                return null;
            }
        }
    	
    	// We did not find a reviewer that does not belong to any of the groups that the researchers 
        // of this project are members of.  We will return the least recently used reviewer at this point. 
    	
    	for(ReviewerOrder reviewer: orderList) {
    		if(reviewer.reviewer.getID() == excludeReasearcherId)
            	continue; // we have already assigned this reviewer
    		
    		log.info("WARNING: assigning reviewer "+reviewer.toString()+" with conflicts for project: "+collaboration.getID());
    		return reviewer;
    	}
    	
    	log.error("ERROR: Could not assign a reviewer!!!");
    	return null;
    }
    

    private static List<String> getProjectResearcherGroups(Collaboration collaboration) throws SQLException {
        Researcher pi = collaboration.getPI();
        List<Researcher> researchers = collaboration.getResearchers();

        List<Integer> projectMembers = new ArrayList<Integer>(4);
        if(pi != null) projectMembers.add(pi.getID());
        for(Researcher researcher: researchers)
        {
            projectMembers.add(researcher.getID());
        }

        Groups groups = Groups.getInstance();
        // Get the groups of the researchers for this project
        List<String> projectMemberGroups = groups.getGroupsFor(projectMembers);
        
        // Get the groups the researchers are collaborating with
        Set<String> collabGroups = collaboration.getGroups();
        for(String cg: collabGroups) {
            if(!projectMemberGroups.contains(cg)) 
                projectMemberGroups.add(cg);
        }
        
        return projectMemberGroups;
    }

    private static boolean isReviewerInProjectGroup(int reviewerId, List<String> projectGroups) throws SQLException {

        Groups groups = Groups.getInstance();
        for(String grpName: projectGroups) {
            if(grpName.equalsIgnoreCase("Core") 
                    || grpName.equals("Reviewers") 
                    || grpName.equalsIgnoreCase("administrators") 
                    || grpName.equalsIgnoreCase("von Haller")
                    || grpName.equalsIgnoreCase("Lab Directors")
                    )
                continue;

            if(groups.isMember(reviewerId, grpName))
                return true;
        }
        return false;
    }

    private static List<Integer> getReviewerIds() throws SQLException {
        // Get our connection to the database.
        Connection conn = DBConnectionManager.getConnection("pr");  
        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();

            // Our SQL statement
            String sqlStr = "SELECT gm.researcherID FROM tblYRCGroups as g, tblYRCGroupMembers AS gm, tblResearchers AS r "+
            "WHERE g.groupID=gm.groupID AND gm.researcherID= r.researcherID AND g.groupName = \"Reviewers\" "+
            "ORDER BY gm.researcherID";

            // Our results
            rs = stmt.executeQuery(sqlStr);

            List<Integer> reviewers = new ArrayList<Integer>();
            while(rs.next()) {
                Integer id = rs.getInt("researcherID");
                if (id != null)
                    reviewers.add(id);
            }

            // No reviewers found
            if(reviewers.size() == 0) {
                log.warn("No project reviewers found!!");
                return new ArrayList<Integer>(0);
            }

            return reviewers;
        }
        catch(SQLException e) { throw e; }
        finally {

            // Always make sure result sets and statements are closed,
            // and the connection is returned to the pool
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { ; }
                rs = null;
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { ; }
                stmt = null;
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { ; }
                conn = null;
            }
        }
    }
    
}
