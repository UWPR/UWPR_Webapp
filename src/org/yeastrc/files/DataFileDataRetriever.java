package org.yeastrc.files;

import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataFileDataRetriever {

	public static DataFileDataRetriever getInstance() { return new DataFileDataRetriever(); }
	private DataFileDataRetriever() { }
	
	public byte[] getDataFileData( DataFile datafile ) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		byte[] datafiledata = null;
		
		try {
			
			conn = DBConnectionManager.getPrConnection();

			String sql = "SELECT data FROM files WHERE id = ?";
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, datafile.getId() );
			rs = stmt.executeQuery();
			
			if ( !rs.next() )
				throw new Exception( "Invalid file ID on DataFileDataRetriever.getDataFileData  DataFile ID: " + datafile.getId() );
			
			datafiledata = rs.getBytes( "data" );
			
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			conn.close(); conn = null;
			
		} finally {
			
			if (rs != null) {
				try { rs.close(); rs = null; } catch (Exception e) { ; }
			}

			if (stmt != null) {
				try { stmt.close(); stmt = null; } catch (Exception e) { ; }
			}
			
			if (conn != null) {
				try { conn.close(); conn = null; } catch (Exception e) { ; }
			}			
		}
		
		return datafiledata;
	}
	
	
}
