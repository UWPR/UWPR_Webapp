/**
 * 
 */
package org.uwpr.data;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.ProjectFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Mike
 *
 */
public class DataURIFactory {

	// private constructor
	private DataURIFactory() { }
	
	/**
	 * Get an instance of this class
	 * @return
	 */
	public static DataURIFactory getInstance() {
		return new DataURIFactory();
	}
	
	/**
	 * Gets the DataURI object out of the database that matches
	 * the uri passed in.  This does not have to be an exact match.  We consider all sub
	 * directories of a particular URI entry to haveve the same permissions as the parent URI.
	 * Therefor, a search will be made for the specific URI then, if none is found, move up to
	 * parent directory, then grandparent directory and so on.  If no match is ever found, null is returned.
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public DataURI getDataURI( String uri ) throws Exception {
		
		DataURI dataURI = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			// get our connection
			conn = getConnection();
			
			String sql = "SELECT id FROM externalDataLocations WHERE uri = ?";
			stmt = conn.prepareStatement( sql );
			stmt.setString( 1,  uri );
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				dataURI = getDataURI ( rs.getInt( 1 ) );
			}
			
			rs.close(); rs = null;
			
			// didn't find it
			while (dataURI == null) {
				uri = URIUtils.getParentDirectory( uri );
				if (uri == null) break;
				
				stmt.setString( 1, uri );
				rs = stmt.executeQuery();
				
				if (rs.next())
					dataURI = getDataURI( rs.getInt( 1 ) );
				
				rs.close(); rs = null;
			}
			
			
			//rs.close(); rs = null;
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
		
		return dataURI;
	}
	
	
	/**
	 * Get a DataURI object out of the database
	 * @param id The id of the desired DataURI object
	 * @return The DataURI object, or null if not found
	 * @throws Exception
	 */
	public DataURI getDataURI( int id ) throws Exception {
		
		DataURI dataURI = null;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
		
			String sql = "SELECT * FROM externalDataLocations WHERE id = ?";
			conn = getConnection();
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, id );
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				dataURI = new DataURI();
				dataURI.setId( id );
				dataURI.setUri( rs.getString( "uri" ) );
				dataURI.setComments( rs.getString( "comments" ) );
				dataURI.setLastChange( rs.getDate( "lastChange" ) );
				dataURI.setProject( ProjectFactory.getProject( rs.getInt( "projectID" ) ) );
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
		
		return dataURI;
	}

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getPrConnection();
    }


}
