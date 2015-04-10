/**
 * 
 */
package org.uwpr.data;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Mike
 *
 */
public class DataURISearcher {

	private DataURISearcher() { }
	
	/**
	 * Get an instance of this class
	 * @return
	 */
	public static DataURISearcher getInstance() {
		return new DataURISearcher();
	}
	
	/**
	 * Get all DataURI rows associated with a specific project, from the database
	 * @param project The project, to which all DataURIs must be associated
	 * @return
	 * @throws Exception
	 */
	public List<DataURI> searchByProject( Project project ) throws Exception {
		return this.searchByProjectID( project.getID() );
	}
	
	/**
	 * Get all DataURI rows associated with a specific project id, from the database
	 * @param id The project id, to which all dataURIs must be associated
	 * @return
	 * @throws Exception
	 */
	public List<DataURI> searchByProjectID( int id ) throws Exception {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<DataURI> dataList = new ArrayList<DataURI>();

		try {

			String sql = "SELECT id FROM externalDataLocations WHERE projectID = ? ORDER BY lastChange";
			conn = DBConnectionManager.getPrConnection();
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, id );
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				try {
					dataList.add( DataURIFactory.getInstance().getDataURI( rs.getInt( 1 ) ) ); 
				} catch (Exception ignored) {}
			}

			rs.close(); rs = null;
			stmt.close(); stmt = null;
			conn.close(); conn = null;

		} finally {
				
			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException ignored) {}
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException ignored) {}
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException ignored) {}
			}
		}
		
		return dataList;
	}
}
