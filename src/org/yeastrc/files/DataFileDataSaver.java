package org.yeastrc.files;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.yeastrc.db.DBConnectionManager;
import java.io.*;

public class DataFileDataSaver {

	public static DataFileDataSaver getInstance() { return new DataFileDataSaver(); }
	private DataFileDataSaver() { }
	
	/**
	 * Save the supplied data to the supplied datafile in the database. NOTE: The given DataFile
	 * must already be saved to the database, or this will not work
	 * @param datafile
	 * @param data
	 * @throws Exception
	 */
	public void saveData( DataFile datafile, byte[] data ) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			
			conn = DBConnectionManager.getConnection( "pr" );

			String sql = "UPDATE files SET data = ? WHERE id = ?";
			
			stmt = conn.prepareStatement( sql );
			stmt.setBytes( 1, data );
			stmt.setInt( 2, datafile.getId() );

			stmt.executeQuery();
			
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
		} finally {

			if (stmt != null) {
				try { stmt.close(); stmt = null; } catch (Exception e) { ; }
			}
			
			if (conn != null) {
				try { conn.close(); conn = null; } catch (Exception e) { ; }
			}			
		}
		
	}

	/**
	 * Save the data to the database using the supplied InputStream
	 * @param datafile
	 * @param is
	 * @throws Exception
	 */
	public void saveData( DataFile datafile, InputStream is ) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			
			conn = DBConnectionManager.getConnection( "pr" );

			String sql = "UPDATE files SET data = ? WHERE id = ?";
			
			stmt = conn.prepareStatement( sql );
			stmt.setBinaryStream( 1, is, datafile.getFilesize() );
			stmt.setInt( 2, datafile.getId() );

			stmt.execute();
			
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
		} finally {

			if (stmt != null) {
				try { stmt.close(); stmt = null; } catch (Exception e) { ; }
			}
			
			if (conn != null) {
				try { conn.close(); conn = null; } catch (Exception e) { ; }
			}			
		}
		
	}
	
}
