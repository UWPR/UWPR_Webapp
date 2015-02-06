/*
 * DataProjectSearcher.java
 * Michael Riffle <mriffle@u.washington.edu>
 * Apr 10, 2008
 */
package org.uwpr.www.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectFactory;

/**
 * @author Michael Riffle <mriffle@u.washington.edu>
 * @date Apr 10, 2008
 * Description of class here.
 */
public class DataProjectSearcher {

	// prevent direct instantiaion of DataProjectSearcher
	private DataProjectSearcher() {
	}

	/**
	 * get an instance of DataProjectSearcher
	 * @return an instance of DataProjectSearcher
	 */
	public static DataProjectSearcher getInstance() {
		return new DataProjectSearcher();
	}
	
	public Set<Project> getProjectsWithExternalData() throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		Set<Project> pSet = new HashSet<Project>();

		try {

			String sql = "SELECT DISTINCT projectID FROM externalDataLocations";
			conn = DBConnectionManager.getConnection( "pr" );
			stmt = conn.prepareStatement( sql );
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				try {
					pSet.add( ProjectFactory.getProject( rs.getInt( 1 ) ) );
				} catch (Exception e) { ; }
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
		
		return pSet;
	}
}
