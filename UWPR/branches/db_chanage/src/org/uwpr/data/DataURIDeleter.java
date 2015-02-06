/**
 * 
 */
package org.uwpr.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.yeastrc.db.DBConnectionManager;

/**
 * @author Mike
 *
 */
public class DataURIDeleter {

	// private constructor
	private DataURIDeleter() { }
	
	/**
	 * Get an instance of this class
	 * @return
	 */
	public static DataURIDeleter getInstance() {
		return new DataURIDeleter();
	}

	public void delete( DataURI dataURI ) throws Exception {
		if (dataURI == null)
			return;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			
			String sql = "DELETE FROM externalDataLocations WHERE id = ?";
			
			conn = DBConnectionManager.getConnection("pr");
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, dataURI.getId() );
			stmt.executeUpdate();
			
			// reset its ID to 0, since it's no longer in the database
			dataURI.setId( 0 );
			
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
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
	}
	
}
