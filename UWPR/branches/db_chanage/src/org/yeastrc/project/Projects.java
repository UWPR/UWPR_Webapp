/*
 * Projects.java
 *
 * Created November 15, 2003
 *
 * Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.project;

import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.www.user.Groups;

import java.sql.*;
import java.util.*;


/**
 * This class is to provide a set of static methods and variable definitions that
 * will provide utility functionality for projects.
 *
 * @version 2003-11-19
 * @author Michael Riffle <mriffle@u.washington.edu>
 */
public class Projects {
	/**
	 * These are the valid GROUPS in the YRC.  All calls to setGroup/addGroup in Project
	 * will have to be present int this array, or no guarantees are made as to method behavior!
	 * This array MUST BE SORTED alphabetically, for binary search purposes
	 */
	public static final String[] GROUPS = {"Bruce", "Core", "Goodlett","Heinecke", "Hoofnagle", "Informatics", "MacCoss", "Ong", "Villen", "Wolf-Yadlin", "von_Haller"};
	public static final String[] GROUPS_LONG = {"Bruce", "UWPR Core Members", "Goodlett", "Heinecke","Hoofnagle", "Informatics", "MacCoss", "Ong", "Villen", "Wolf-Yadlin", "von Haller"};
	
	/** The definition for groups */
	public static final String MACCOSS = "MacCoss";
	public static final String BRUCE = "Bruce";
	public static final String VILLEN = "Villen";
	public static final String GOODLETT = "Goodlett";
	public static final String INFORMATICS = "Informatics";
	public static final String HEINECKE = "Heinecke";
	public static final String VON_HALLER = "von_Haller";
	public static final String HOOFNAGLE = "Hoofnagle";
	public static final String WOLF_YADLIN = "Wolf-Yadlin";
	public static final String ONG = "Ong";
	public static final String CORE = "Core";
	
	/** The definition for a Collaboration **/
	public static final String COLLABORATION = "C";
	
	/** The definition for a Billed Project **/
	public static final String BILLED_PROJECT = "B";
	
	
	/**
	 * Returns all NEW projects submitted to this YRC member's group(s)
	 * @param r The researcher of the YRC member
	 * @return A list of new projects (within the last 30 days) for this member's groups, null if this is not a YRC member
	 */
	public static List getNewProjectsForYRCMember(Researcher r) throws SQLException {
		int researcherID = r.getID();
		Groups gm = Groups.getInstance();

		// return null if they're not in a YRC group
		if (!gm.isInAGroup(researcherID)) return null;

		ProjectsSearcher ps = new ProjectsSearcher();
		//ps.setResearcher(r);

		// Set the start date of the search to 1 month ago
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		ps.setStartDate(cal.getTime());

		// Set the groups to search
		List groups = gm.getGroups();
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			String group = (String)(iter.next());
			if (gm.isMember(researcherID, group)) {
				ps.addGroup(group);		
			}
		}

		List projects = ps.search();
		return projects;
	}
	
	/**
	 * Get all of the projects for which the supplied researcher ID is associated as a researcher
	 * @param researcherID The researcher ID to use
	 * @return An list of populated Project objects
	 */
	public static ArrayList getProjectsByResearcher(int researcherID) throws SQLException, InvalidProjectTypeException {
		ArrayList retList = new ArrayList();

		// Get our connection to the database.
		Connection conn = DBConnectionManager.getConnection("pr");
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try{

            // Our SQL statement
            String sqlStr = "SELECT DISTINCT  p.projectID , projectType "+
            "FROM tblProjects AS p " +
            "LEFT OUTER JOIN projectResearcher AS pr "+
            "ON p.projectID = pr.projectID "+
            "WHERE (p.projectPI = ? OR pr.researcherID = ?) "+
            "ORDER BY p.projectID";

            stmt = conn.prepareStatement(sqlStr);
            stmt.setInt(1, researcherID);
            stmt.setInt(2, researcherID);

			// Our results
			rs = stmt.executeQuery();

			// Iterate over list and populate our return list
			while (rs.next()) {
				int projectID = rs.getInt("projectID");
				String type = rs.getString("projectType");
				Project proj;
				
				if (type.equals(Projects.COLLABORATION)) {
					proj = new Collaboration();
				} else if (type.equals(Projects.BILLED_PROJECT)) {
					proj = new BilledProject();
				} else {
					throw new InvalidProjectTypeException("Type wasn't C or B...");
				}
				
				try {
					proj.load(projectID);
				} catch(InvalidIDException iie) {
					continue;
				}
				
				retList.add(proj);
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
     * Get all of the projects for which the supplied researcher ID is associated as a researcher
     * @param researcherID The researcher ID to use
     * @return An list of populated Project objects
     */
    public static List<Collaboration> getCollaborationsByReviewer(int researcherID) throws SQLException {
        List<Collaboration> retList = new ArrayList<Collaboration>();

        // Get our connection to the database.
        Connection conn = DBConnectionManager.getConnection("pr");
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try{
            // Our SQL statement
            String sqlStr =  "SELECT DISTINCT projectID FROM projectReviewer WHERE ";
                   sqlStr += "researcherID = ? ";
                   sqlStr += "ORDER BY projectID DESC";
            
            stmt = conn.prepareStatement(sqlStr);
            stmt.setInt(1, researcherID);

            // Our results
            rs = stmt.executeQuery();

            // Iterate over list and populate our return list
            while (rs.next()) {
                int projectID = rs.getInt("projectID");
                
                Collaboration collaboration = new Collaboration();
                
                try {
                    collaboration.load(projectID);
                } catch(InvalidIDException iie) {
                    continue;
                }
                
                retList.add(collaboration);
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
	 * Simply takes a Set of groups and returns a comma delimited
	 * listed of those groups.
	 * @param groups a Set of groups, as defined in this document (e.g. Projects.TWOHYBRID)
	 * @return A string consisting of a long hand, comma delimited version of that Set
	 */
	public static String getGroupsString(Set groups) {
		if (groups == null) return "None";
		if (groups.size() < 1) return "None";
		
		boolean pastFirst = false;
		String retString = "";
		
		Iterator iter = groups.iterator();
		while (iter.hasNext()) {
			String group = (String)(iter.next());

			// Add a comma before all but the first item
			if (pastFirst) retString += ", ";
			pastFirst = true;
			
			// Get the long version of the group name, and add it to the list
			int indexOfGroup = Arrays.binarySearch(Projects.GROUPS, group);
			if (indexOfGroup >= 0)
				retString += Projects.GROUPS_LONG[indexOfGroup];
			else
				retString += group;		
		}

		return retString;

	}


	/**
	 * Simply return an ArrayList of all the researchers in the database (as Researcher objects)
	 * @return A list of all the Researchers in the database
	 */
	public static ArrayList getAllResearchers() throws SQLException, InvalidIDException {
		ArrayList retList = new ArrayList();

		// Get our connection to the database.
		Connection conn = DBConnectionManager.getConnection("pr");
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();

			// Our SQL statement
			String sqlStr =  "SELECT researcherID, researcherFirstName, researcherLastName, researcherDegree, researcherEmail, researcherOrganization FROM tblResearchers ORDER BY researcherLastName";

			// Our results
			rs = stmt.executeQuery(sqlStr);

			// Iterate over list and populate our return list
			while (rs.next()) {
				Researcher researcher = new Researcher();
				researcher.setID(rs.getInt("researcherID"));
				researcher.setFirstName(rs.getString("researcherFirstName"));
				researcher.setLastName(rs.getString("researcherLastName"));
				researcher.setDegree(rs.getString("researcherDegree"));
				researcher.setEmail(rs.getString("researcherEmail"));
				researcher.setOrganization(rs.getString("researcherOrganization"));
				
				retList.add(researcher);
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
	 * Return a list of all the researchers in the database that are also "Lab Directors"
	 * @return A list of all the Researchers in the database
	 */
	public static List<Researcher> getAllLabDirectors() throws SQLException, InvalidIDException {
		List<Researcher> retList = new ArrayList<Researcher>();

		// Get our connection to the database.
		Connection conn = DBConnectionManager.getConnection("pr");
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();

			// Our SQL statement
			String sqlStr =  "SELECT r.researcherID, researcherFirstName, researcherLastName, researcherDegree, researcherEmail, researcherOrganization "+
			"FROM tblResearchers AS r, tblYRCGroupMembers AS m, tblYRCGroups AS g "+
			"WHERE g.groupName=\"Lab Directors\" "+
			"AND g.groupID = m.groupID "+
			"AND m.researcherID = r.researcherID "+
			"ORDER BY researcherLastName";

			// Our results
			rs = stmt.executeQuery(sqlStr);

			// Iterate over list and populate our return list
			while (rs.next()) {
				Researcher researcher = new Researcher();
				researcher.setID(rs.getInt("researcherID"));
				researcher.setFirstName(rs.getString("researcherFirstName"));
				researcher.setLastName(rs.getString("researcherLastName"));
				researcher.setDegree(rs.getString("researcherDegree"));
				researcher.setEmail(rs.getString("researcherEmail"));
				researcher.setOrganization(rs.getString("researcherOrganization"));
				
				retList.add(researcher);
			}
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
}