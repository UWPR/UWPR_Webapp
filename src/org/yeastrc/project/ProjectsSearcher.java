/* ProjectsSearch.java
 * Created on Mar 31, 2004
 */
package org.yeastrc.project;

import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Provides a set of methods for setting search parameters, and retrieving
 * search results based on those parameters.
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 31, 2004
 *
 */
public class ProjectsSearcher {

	/**
	 * Get a new ProjectSearch
	 */
	public ProjectsSearcher() {
		this.searchTokens = new HashSet<String>();
		this.types = new HashSet<String>();
		this.groups = new HashSet<String>();
		this.statusTypes = new HashSet<CollaborationStatus>();
	}

	public List<Project> search() throws SQLException {
		ArrayList<Project> retList = new ArrayList<Project>();
		
		// Get our connection to the database.
		Connection conn = DBConnectionManager.getMainDbConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			boolean haveConstraint = false;

			// One entry per ? in sqlStr, in the order the placeholders appear.
			List<Object> params = new ArrayList<Object>();

            String sqlStr = "SELECT DISTINCT P.projectID, P.projectSubmitDate ";
            sqlStr += "FROM tblProjects AS P ";
            sqlStr += "LEFT OUTER JOIN projectResearcher AS PR ON P.projectID = PR.projectID ";
            sqlStr += "LEFT OUTER JOIN tblResearchers AS RPI ON RPI.researcherID = P.projectPI ";
            sqlStr += "LEFT OUTER JOIN tblResearchers AS R ON R.researcherID = PR.researcherID";

			// We have a project type constraint
			if (this.types.size() > 0) {
				sqlStr += " WHERE";
				haveConstraint = true;

				sqlStr += " (";

				Iterator<String> iter = this.types.iterator();
				sqlStr += "P.projectType = ?";
				params.add(iter.next());

				while (iter.hasNext()) {
					sqlStr += " OR P.projectType = ?";
					params.add(iter.next());
				}

				sqlStr += ")";
			}

			// We have search tokens.  A project has to match every one of them.
			if (this.searchTokens.size() > 0) {
				if (haveConstraint) {
					sqlStr += " AND";
				} else {
					sqlStr += " WHERE";
					haveConstraint = true;
				}

				Iterator<String> iter = this.searchTokens.iterator();

				sqlStr += " (";
				sqlStr += tokenConstraint(iter.next(), params);

				while (iter.hasNext()) {
					sqlStr += " AND " + tokenConstraint(iter.next(), params);
				}

				sqlStr += ")";
			}

			// Start date constraint
			if (this.startDate != null) {
				if (haveConstraint) { sqlStr += " AND"; }
				else {
					sqlStr += " WHERE";
					haveConstraint = true;
				}

				sqlStr += " P.projectSubmitDate >= ?";
				params.add(new java.sql.Date(this.startDate.getTime()));
			}

			// End date constraint
			if (this.endDate != null) {
				if (haveConstraint) { sqlStr += " AND"; }
				else {
					sqlStr += " WHERE";
					haveConstraint = true;
				}

				sqlStr += " P.projectSubmitDate <= ?";
				params.add(new java.sql.Date(this.endDate.getTime()));
			}

			// Archived constraint
			if (this.excludeArchived) {
				if (haveConstraint) { sqlStr += " AND"; }
				else {
					sqlStr += " WHERE";
					haveConstraint = true;
				}

				sqlStr += " P.archived = 0";
			}

			sqlStr += " ORDER BY P.projectSubmitDate";

			stmt = conn.prepareStatement(sqlStr);
			for (int i = 0; i < params.size(); i++) {
				stmt.setObject(i + 1, params.get(i));
			}
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				int projectID = rs.getInt("projectID");
				Project p;
				try { p = ProjectFactory.getProject(projectID); }
				catch (InvalidIDException e) { continue; }
				
				// Make sure this project belongs to one of the specified groups, if not skip it.
				if (this.groups != null && !this.groups.isEmpty()) {
					boolean foundGroup = false;
					String[] tgroups = p.getGroupsArray();
					
					if(tgroups != null && tgroups.length > 0) {
					
						for (int i = 0; i < tgroups.length; i++) {
							if (this.groups.contains(tgroups[i])) {
								foundGroup = true;
								break;
							}
						}
					}
					if (!foundGroup) { continue; }
				}
				
				// User wants to filter on collaboration status (only for projects of type Collaboration)
				if(this.statusTypes.size() > 0) {
				    if(p instanceof Collaboration) {
				        CollaborationStatus projStatus = ((Collaboration)p).getCollaborationStatus();
				        if(!statusTypes.contains(projStatus))
				            continue;
				    }
				    else {
				    	continue; // not a Collaboration project
				    }
				}
				
				// Don't add this project to the list if the Researcher doesn't have access
				if (this.researcher != null) {
					boolean hasAccess = this.requireWriteAccess ? p.checkAccess(this.researcher)
					                                            : p.checkReadAccess(this.researcher);
					if (!hasAccess) {
						continue;
					}
				}

				retList.add(p);
			}

			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
		}
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
		
		
		return retList;
	}

	/**
	 * The columns one search word is matched against.
	 */
	private static final String[] TOKEN_COLUMNS = {
			"RPI.researcherLastName", "RPI.researcherFirstName",
			"R.researcherLastName", "R.researcherFirstName",
			"P.projectAbstract", "P.publicAbstract", "P.projectKeywords",
			"P.projectProgress", "P.scientificQuestion", "P.projectTitle"
	};

	/**
	 * A parenthesised OR over TOKEN_COLUMNS for one search word.  A word that parses as an
	 * integer also matches that project ID.
	 *
	 * Adds one entry to params for each ? it returns, in the same order.
	 */
	private String tokenConstraint(String tok, List<Object> params) {

		StringBuilder constraint = new StringBuilder("(");

		for (int i = 0; i < TOKEN_COLUMNS.length; i++) {
			if (i > 0) { constraint.append(" OR "); }
			constraint.append(TOKEN_COLUMNS[i]).append(" LIKE ?");
			params.add("%" + tok + "%");
		}

		try {
			int projectId = Integer.parseInt(tok);
			constraint.append(" OR P.projectID = ?");
			params.add(projectId);
		}
		catch (NumberFormatException ignored) {}

		constraint.append(")");
		return constraint.toString();
	}

	/**
	 * Add a new word to use for searching
	 * @param phrase The phrase to add
	 */
	public void addSearchToken(String phrase) {
		if (phrase == null) { return; }

		this.searchTokens.add(phrase);
	}
	
	/**
	 * Add a project type to search by.
	 * @param type
	 */
	public void addType(String type) {
		if (type == null) return;
		this.types.add(type);
	}

	/**
	 * Add a YRC group to search by
	 * @param group
	 */
	public void addGroup(String group) {
		if (group == null) return;
		this.groups.add(group);
	}
	
	/**
	 * Add a CollaborationStatus type to search by
	 * @param status
	 */
	public void addStatusType(CollaborationStatus status) {
	    if(statusTypes == null)
	        return;
	    this.statusTypes.add(status);
	}
	
	/**
	 * Leave archived projects out of the result.
	 */
	public void setExcludeArchived(boolean excludeArchived) {
		this.excludeArchived = excludeArchived;
	}

	/**
	 * The researcher must be an administrator, the PI, or one of the project researchers
	 * to have write access.
	 */
	public void setRequireWriteAccess(boolean requireWriteAccess) {
		this.requireWriteAccess = requireWriteAccess;
	}

	/** Set the researcher to use as the basis for checking access to the projects
	 *  returned.  If this researcher doesn't have access to a project, it won't be
	 *  in the returned list.
	 *
	 * @param researcher The researcher
	 */
	public void setResearcher(Researcher researcher) {
		this.researcher = researcher;
	}
	
	/**
	 * Set the earliest submission date for returned projects.
	 * Defaults to the beginning of time.
	 * @param date
	 */
	public void setStartDate(Date date) {
		this.startDate = date;
	}
	
	/**
	 * Set the latest submission date for returned projects.
	 * Defaults to the end of time.
	 * @param date
	 */
	public void setEndDate(Date date) {
		this.endDate = date;
	}

	// The Strings we are using to conduct the search
	private Set<String> searchTokens;
	
	// The project types to include in the results
	private Set<String> types;
	
	// The groups to which the projects belong to include in the results
	private Set<String> groups;
	
	// The researcher requesting the list, or just the researcher who's access privledges are taken into account when making the list
	private Researcher researcher;
	
	// The earliest submission date of included projects (project submitted before this date are not returned)
	private Date startDate;
	
	// The latest submission date of included project (project submitted after this date are not returned)
	private Date endDate;
	
	// The collaboration status of the projects to include in the result
	private Set<CollaborationStatus> statusTypes;

	private boolean excludeArchived = false;
	private boolean requireWriteAccess = false;

}
