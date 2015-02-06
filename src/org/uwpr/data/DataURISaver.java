/**
 * 
 */
package org.uwpr.data;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.yeastrc.db.DBConnectionManager;

/**
 * @author Mike
 *
 */
public class DataURISaver {

	// private constructor
	private DataURISaver() { }

	/**
	 * Get an instance of this class
	 * @return
	 */
	public static DataURISaver getInstance() {
		return new DataURISaver();
	}
	
	public void save( DataURI dataURI ) throws Exception {
		if (dataURI == null)
			return;
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			
			boolean newData = false;
			if (dataURI.getId() == 0) newData = true;
			
			String sql = "SELECT * FROM externalDataLocations WHERE id = " + dataURI.getId();
			
			conn = DBConnectionManager.getConnection("pr");
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery( sql );
			
			if (!newData) {
				if (!rs.next())
					throw new Exception( "DataURI had id, but was not in database.  Aborting save." );
			} else {
				rs.moveToInsertRow();
			}
			
			rs.updateString( "uri", dataURI.getUri() );
			rs.updateString( "comments", dataURI.getComments() );
			rs.updateInt( "projectID", dataURI.getProject().getID() );
			
			if (!newData) {
				rs.updateRow();
			} else {
				rs.insertRow();
				rs.last();
				dataURI.setId( rs.getInt( "id" ) );
			}
			
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			conn.close(); conn = null;

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
	}
	
}
