package org.yeastrc.files;

import org.yeastrc.db.DBConnectionManager;

import java.sql.*;

public class DataFileSaver {

	public static DataFileSaver getInstance() { return new DataFileSaver(); }
	private DataFileSaver() { }
	
	/**
	 * Save the supplied DataFile to the database. If the id of the DataFile is 0, then
	 * it is assumed to not already be in the database. If it is not 0, then its record
	 * is updated.
	 * @param datafile
	 * @type the type of data this file is associated with
	 * @tid the ID associated with the type, such as a runID associated with a MS run
	 * @throws Exception
	 */
	public void saveDataFile( DataFile datafile, String type, int tid ) throws Exception {
		
		// Get our connection to the database.
		Connection conn = DBConnectionManager.getPrConnection();
		Statement stmt = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			boolean isNew = false;
			
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			// Get our updatable result set
			String sqlStr = "SELECT id, uploadedBy, filesize, filename, description, mimetype FROM files WHERE id = " + datafile.getId();
			rs = stmt.executeQuery(sqlStr);

			// See if we're updating a row or adding a new row.
			if (datafile.getId() == 0) {
				isNew = true;
				rs.moveToInsertRow();
			}
			else {
				if( !rs.next() )
					throw new Exception( "ID was set for DataFile, but not found in database when attempting to save." );
			}
				
			rs.updateInt( "uploadedBy", datafile.getUploader().getID() );
			rs.updateInt( "filesize", datafile.getFilesize() );
			rs.updateString( "filename", datafile.getFilename() );
			rs.updateString( "description", datafile.getDescription() );
			rs.updateString( "mimetype", datafile.getMimetype() );
			
			if( datafile.getId() == 0 ) {
				rs.insertRow();
				rs.last();
				datafile.setId( rs.getInt( "id" ) );
			} else
				rs.updateRow();
				
			// Close up shop
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			
			// make sure it's in the correct association table in the database
			if( isNew ) {
				String table = DataFileDataUtils.getTypeLocationMap().get( type );
				String column = DataFileDataUtils.getTypeColumnMap().get( type );
				sqlStr = "INSERT INTO " + table + " (file_id, " + column + ") VALUES(?,?)";
				
				pstmt = conn.prepareStatement( sqlStr );
				pstmt.setInt( 1, datafile.getId() );
				pstmt.setInt( 2, tid );
				pstmt.execute();
				
				pstmt.close(); pstmt = null;
			}
			
			
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
			if (pstmt != null) {
				try { pstmt.close(); } catch (SQLException e) { ; }
				pstmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}
	}
			
}
