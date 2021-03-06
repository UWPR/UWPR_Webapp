/* Groups.java
 * Created on Mar 23, 2004
 */
package org.yeastrc.www.user;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Researcher;

import java.sql.*;
import java.util.*;

/**
 * Singleton class providing access and methods for YRC group information.
 * 
 * @author Michael Riffle <mriffle@alumni.washington.edu>
 * @version 1.0, Mar 23, 2004
 *
 */
public class Groups {

	// Our single instance of this class
	private static final Groups INSTANCE = new Groups();

	// Our HashMap of groups
	private HashMap <String, List<Integer>> groups;

    public static final String INSTRUMENT_OPERATOR = "Instrument Operators";
    public static final String ADMIN = "administrators";
	public static final String NOTIFY = "Notify";

	// Our constructor
	private Groups() {
		this.groups = new HashMap();

		try { this.loadGroups(); }
		catch (Exception e) { ; }
	}

	// Load the groups from the database.
	private void loadGroups() throws SQLException {
		
		// Get our connection to the database.
		Connection conn = getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery("SELECT g.groupName AS groupName, gm.researcherID AS researcherID FROM tblYRCGroups AS g LEFT OUTER JOIN tblYRCGroupMembers AS gm ON g.groupID = gm.groupID ORDER BY g.groupName, gm.researcherID");

			while (rs.next()) {
				String groupName = rs.getString("groupName");
				int researcherID = rs.getInt("researcherID");
				ArrayList rList;
				
				// Get our ArrayList of members of this group, so far
				if (groups.get(groupName) == null) {
					rList = new ArrayList();
					groups.put(groupName, rList);
				} else {
					rList = (ArrayList)(groups.get(groupName));
				}

				// Add this member to the group
				rList.add(new Integer(researcherID));
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
	}
	
	/**
	 * Gets the groupID for the supplied group name
	 * @param groupName the name of the group
	 * @return The id of the group, 0 if not found
	 */
	private int getGroupID(String groupName) throws SQLException {
		int groupID = 0;
		if (groupName == null) return 0;
		
		// Get our connection to the database.
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			String sqlStr = "SELECT groupID FROM tblYRCGroups WHERE groupName = ?";
			
			conn = getConnection();
			stmt = conn.prepareStatement(sqlStr);
			stmt.setString(1, groupName);
			
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				groupID = rs.getInt("groupID");
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;

		} finally {

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

		return groupID;

	}
	
	/**
	 * @return The singleton instance
	 */
	public static Groups getInstance() { return INSTANCE; }


	/**
	 * Reload the groups list from the database
	 */
	public void reloadGroups() throws SQLException {
		this.groups = new HashMap();

		this.loadGroups();
	}

	/**
	 * Test to see if the given researcher ID is a member of the given group.
	 * @param rID The researcher ID
	 * @param gName The group name
	 */
	public boolean isMember(int rID, String gName) {
		ArrayList members;
		if (gName == null) { return false; }
		
		members = (ArrayList)(this.groups.get(gName));
		if (members == null) { return false; }
		
		return members.contains(new Integer(rID));
	}
	
	/**
	 * Checks to see if the supplied researcher ID belongs to ANY YRC group.
	 * If it does, then it's YRC personell, if not it's a normal researcher
	 * @param rID The researcher ID
	 * @return true if it is in a YRC group, false if not
	 */
	public boolean isInAGroup(int rID) {
		
		// Look through each group check for membership
		List groupList = this.getGroups();
		Iterator iter = groupList.iterator();
		while (iter.hasNext()) {
			String groupName = (String)(iter.next());
			if (this.isMember(rID, groupName)) return true;
		}
		
		return false;
	}

    public boolean isAdministrator(Researcher researcher)
    {
        return isMember(researcher.getID(), ADMIN);
    }
	
	public List<String> getGroupsFor(List<Integer> researcherIds) throws SQLException {

	    reloadGroups();
	    
	    if(researcherIds.size() == 0)
	        return new ArrayList<String>(0);
	    
        // Get our connection to the database.
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        List<String> groupNames = new ArrayList<String>();
        
        try {
            StringBuilder buf = new StringBuilder();
            buf.append("SELECT DISTINCT g.groupName FROM tblYRCGroups AS g, tblYRCGroupMembers AS m WHERE g.groupID = m.groupID AND m.researcherID in (");
            for(int researcherId: researcherIds)
               buf.append(researcherId+",");
            buf.deleteCharAt(buf.length() - 1); // delete last comma
            buf.append(")");
            
            String sqlStr = buf.toString();
            
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlStr);
            
            while (rs.next()) {
                String grpName = rs.getString("groupName");
                if(grpName != null)
                    groupNames.add(grpName);
            }
            
            rs.close();
            rs = null;
            
            stmt.close();
            stmt = null;
            
            conn.close();
            conn = null;

        } finally {

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

        return groupNames;
	    
	}
	/**
	 * List all of the groups.
	 * @return An ordered list of the groups by name
	 */
	public List getGroups() {
		Set groupSet = this.groups.keySet();
		ArrayList groupNames = new ArrayList(groupSet);
		Collections.sort(groupNames);
		
		return groupNames;
	}

	/**
	 * Returns a sorted list of Researcher objects, sorted by last name, belonging to the supplied group.
	 * @param groupName The name of the group.
	 * @return A sorted list of Researcher objects.
	 */
	public List <Researcher> getMembers(String groupName) {
		ArrayList retList = new ArrayList<Researcher>();
			
		if (groupName == null) { return retList; }
		
		List<Integer> members = this.groups.get(groupName);
		if (members == null) { return retList; }
		
		Iterator<Integer> iter = members.iterator();
		while (iter.hasNext()) {
			Integer resID = iter.next();
			
			Researcher res = new Researcher();
			try {
				res.load(resID.intValue());
			} catch(Exception e) {
				continue;
			}
			
			retList.add(res);
		}
		
		Collections.sort(retList);
		return retList;
	}

	/**
	 * Add the given researcherID to the given group name
	 * @param groupName
	 * @param researcherID
	 * @throws SQLException
	 */
	public void addToGroup(String groupName, int researcherID) throws SQLException
    {
        addToGroup(groupName, Collections.singletonList(researcherID));
    }

    public void addToGroup(String groupName, List<Integer> researcherIds) throws SQLException {
		int groupID = 0;
		
		if (groupName == null) return; // Invalid group name
        // Get the groupID value for this group from the table
        groupID = this.getGroupID(groupName);
        if (groupID == 0) return; // Group does not exist!

        List<Researcher> members = this.getMembers(groupName);
		if (members == null) return;

        List<Integer> memberIds = new ArrayList<Integer>(members.size());
        for(Researcher member: members)
        {
            memberIds.add(member.getID());
        }

        List<Integer> toAdd = new ArrayList<Integer>();
        for(Integer researcherId: researcherIds)
        {
            if (!memberIds.contains(researcherId))
            {
                // Add researcher only if he/she is not already a member of the group.
                toAdd.add(researcherId);
            }
        }

		// Get our connection to the database.
		Connection conn = null;
		PreparedStatement stmt = null;

		String sqlStr = "INSERT INTO tblYRCGroupMembers (groupID, researcherID) VALUES (?, ?)";
		try {
            conn = getConnection();
            conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sqlStr);
            for(Integer researcherID: toAdd)
            {
                stmt.setInt(1, groupID);
                stmt.setInt(2, researcherID);

                stmt.executeUpdate();
            }
			conn.commit();

		} finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException ignored) { ; }
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException ignored) { ; }
			}
		}
		
		// Refresh the group list.
		this.reloadGroups();
	}

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }

    /**
	 * Remove the given researcherID from the given group name
	 * @param groupName
	 * @param researcherID
	 * @throws SQLException
	 */
	public void removeFromGroup(String groupName, int researcherID) throws SQLException {
		int groupID = 0;
		
		if (groupName == null) return;
		
		// Invalid group name
		List members = this.getMembers(groupName);
		if (members == null) return;
		
		// Researcher is already in that group
		if (members.contains(new Integer(researcherID))) return;
	
		// Remove this researcher from this group in the database
		
		// Get the groupID value for this group from the table
		groupID = this.getGroupID(groupName);
		if (groupID == 0) return;		

		// Get our connection to the database.
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			String sqlStr = "DELETE FROM tblYRCGroupMembers WHERE groupID = ? AND researcherID = ?";
			
			conn = getConnection();
			stmt = conn.prepareStatement(sqlStr);
			stmt.setInt(1, groupID);
			stmt.setInt(2, researcherID);
			
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;

		} finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
		
		// Refresh the groups list
		this.reloadGroups();
	}

}
