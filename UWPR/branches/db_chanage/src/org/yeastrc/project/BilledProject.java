/**
 * BilledProject.java
 * @author Vagisha Sharma
 * May 20, 2011
 */
package org.yeastrc.project;

import org.apache.commons.lang.StringUtils;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.ProjectPaymentMethodDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 */
public class BilledProject extends Project {

	// The set of groups to which this project belongs
	private Set<String> groups;
	private List<PaymentMethod> paymentMethods;
	private boolean isBlocked = false;
	
	
	public List<PaymentMethod> getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	/**
	 * Use this method to populate this object with data from the datbase.
	 * @param id The experiment ID to load.
	 * @throws InvalidIDException If this ID is not valid (or not found)
	 * @throws SQLException If there is a problem interracting with the database
	 */
	public void load(int id) throws InvalidIDException, SQLException {

		// Load Project data first.
		super.load(id);

		// Get our connection to the database.
		Connection conn = DBConnectionManager.getPrConnection();
		Statement stmt = null;
		ResultSet rs = null;

		try{
			stmt = conn.createStatement();

			// Our SQL statement
			String sqlStr = "SELECT * FROM tblBilledProject WHERE projectID = " + id;

			// Our results
			rs = stmt.executeQuery(sqlStr);

			// No rows returned.
			if( !rs.next() ) {
				throw new InvalidIDException("Loading Billed Project failed due to invalid Project ID.");
			}

			// Populate the object from this row.

			String tmpStr;
			tmpStr = rs.getString("collGroups");
			if(tmpStr == null || tmpStr.equals("")) {
				this.groups = null;
			} else {
				this.groups = new HashSet<String>();
				String[] types = StringUtils.split(tmpStr, ",");
				for (int i = 0; i < types.length; i++) {
					this.groups.add(types[i]);
				}
			}
			// affiliation
//			this.affiliation = Affiliation.forName(rs.getString("affiliation"));
			
			this.paymentMethods = ProjectPaymentMethodDAO.getInstance().getPaymentMethods(this.id);
			
			this.isBlocked = rs.getBoolean("blocked");
            
		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	/**
	 * Use this method to save this object's data to the database.
	 * These objects should generally represent a single row of data in a table.
	 * This will update the row if this object represents a current row of data, or it will
	 * insert a new row if this data is not in the database (that is, if there is no ID in the object).
	 * @throws InvalidIDException If there is an ID set in this object, but it is not in the database.
	 * @throws SQLException If there is a problem interracting with the database
	 */
	public void save() throws InvalidIDException, SQLException {
		
		// Whether or not this is a new project being added to the database
		boolean newProject;

		if (super.id == 0) { newProject = true; }
		else { newProject = false; }

		// Call save in the Project class first.  This will save general project data.
		super.save();

		// Get our connection to the database.
		Connection conn = DBConnectionManager.getPrConnection();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			// Get our updatable result set
			String sqlStr = "SELECT * FROM tblBilledProject WHERE projectID = " + super.id;
			rs = stmt.executeQuery(sqlStr);

			// See if we're updating a row or adding a new row.
			if (newProject == false) {

				// Make sure this row is actually in the database.  This shouldn't ever happen.
				if (!rs.next()) {
					throw new InvalidIDException("ID was set in BilledProject, but not found in database on save()");
				}

				// Make sure the result set is set up w/ current values from this object
				if (this.groups == null) { rs.updateNull("collGroups"); }
				else { rs.updateString("collGroups", StringUtils.join(this.groups.toArray(), ",")); }
				
//				if (this.affiliation == null) {rs.updateNull("affiliation");}
//				else rs.updateString("affiliation", this.affiliation.name());
				
				rs.updateBoolean("blocked", this.isBlocked);

				// Update the row
				rs.updateRow();

			} else {
				// We're adding a new row.

				rs.moveToInsertRow();

				rs.updateInt("projectID", super.id);

				if (this.groups == null) { rs.updateNull("collGroups"); }
				else { rs.updateString("collGroups", StringUtils.join(this.groups.toArray(), ",")); }
				
//				if (this.affiliation == null) {rs.updateNull("affiliation");}
//				else rs.updateString("affiliation", this.affiliation.name());
				
				rs.updateBoolean("blocked", this.isBlocked);
			
				rs.insertRow();
			}
		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	
	public void delete() throws InvalidIDException, SQLException {

		// Delete the general project entry first
		super.delete();

		// Get our connection to the database.
		Connection conn = DBConnectionManager.getPrConnection();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			// Our SQL statement
			String sqlStr = "SELECT projectID FROM tblBilledProject WHERE projectID = " + super.id;

			// Our results
			rs = stmt.executeQuery(sqlStr);

			// No rows returned.
			if( !rs.next() ) {
				throw new InvalidIDException("Attempted to delete a Billed Project not found in the database.");
			}

			// Delete the result row.
			rs.deleteRow();		

		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		// Delete the payment methods for this project
        ProjectPaymentMethodDAO.getInstance().unlinkProjectPaymentMethod(0, this.id);

		// re-initialize the id
        super.id = 0;
	}
	
	
	/**
	 * Removes a group from the set of groups to which this project belongs.  If the group
	 * isn't in the set, nothing happens.
	 * @param group The group to remove for this project.
	 */
	public void removeGroup(String group) {
		if (this.groups == null) { return; }
		if (group == null) { return; }
		this.groups.remove(group);
		
		// If the groups set is now empty, just set it to null
		if (this.groups.isEmpty()) { this.groups = null; }
	}

	/**
	 * Removes all the groups from the set of groups to which this project belongs.  Use with caution!
	 */
	public void clearGroups() {
		this.groups = null;
	}
	
	/**
	 * Will return a Set of group names, to which this project belongs.  All groups names will be contained
	 * in the GROUPS array in the Projects class.
	 * @return A set of groups in the YRC, with which this project is affiliated.  Returns null if there are no groups.
	 */
	public Set<String> getGroups() { return this.groups; }
	
	/**
	 * Set a group to which this project belongs.
	 * This will add to a set of groups, so each consecutive call adds another group
	 * to the Set.  All groups passed in here should be present in Projects.GROUPS array.
	 * Ideally one would use the static variables in Projects for setting this in the form of:
	 * setGroup(Projects.YATES)
	 * @param group The group to add for this project.
	 * @throws InvalidIDException if group is an invalid group as defined by Projects.GROUPS
	 */
	public void setGroup(String group) throws InvalidIDException {
		if (group == null) { return; }

		if (Arrays.binarySearch(Projects.GROUPS, group) < 0) {
			throw new InvalidIDException("Attempt to insert an invalid group.");
		}

		if (this.groups == null) { this.groups = new HashSet<String>(); }
	
		this.groups.add(group);
	}
	
	/**
	 * Will return the unabbreviated name for the type of the project.  At the time of this writing, 
	 * it will be one of the following:
	 * "Collaboration", "BilledProject"
	 * @return The unabbreviated name for the type of the project.
	 */
	public String getLongType() { return "BilledProject"; }
	
	/**
	 * Will return the abbreviated name for the type of the project.  At the time of this writing,
	 * it will be one of the following: "C", "B"
	 * @return The abbreviated name for the type of the project.
	 */
	public String getShortType() { return "B"; }

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

}
