package org.yeastrc.files;

import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DataFileDeleter {

	private DataFileDeleter() { }
	private static final DataFileDeleter INSTANCE = new DataFileDeleter();

	/**
	 * Get the instance of this class
	 * @return
	 */
	public static DataFileDeleter getInstance() { return INSTANCE; }

	/**
	 * Delete the supplied datafile from the database
	 * @param datafile
	 * @throws Exception
	 */
	public void deleteDataFile( DataFile datafile ) throws Exception {
		
		if( datafile == null ) return;
		if( datafile.getId() == 0 ) return;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			
			String sql = "DELETE FROM files WHERE id = ?";
			conn = DBConnectionManager.getPrConnection();
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, datafile.getId() );
			stmt.executeUpdate();
			stmt.close(); stmt = null;
			
			for( String t : DataFileDataUtils.getTypeLocationMap().values() ) {
				sql = "DELETE FROM " + t + " WHERE file_id = ?";
				stmt = conn.prepareStatement( sql );
				stmt.setInt( 1, datafile.getId() );
				stmt.executeUpdate();
				stmt.close(); stmt = null;
			}
			
		} finally {

			if (stmt != null) {
				try { stmt.close(); stmt = null; } catch (Exception e) { ; }
			}
			
			if (conn != null) {
				try { conn.close(); conn = null; } catch (Exception e) { ; }
			}			
		}
		
		// no longer in the database
		datafile.setId( 0 );
		
		return;
	}	
}
