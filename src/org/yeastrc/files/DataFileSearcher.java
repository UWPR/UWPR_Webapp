package org.yeastrc.files;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mike
 *
 * Find all DataFiles for a given Object (e.g., Project or MSRun)
 *
 */
public class DataFileSearcher {

	/*
	 * Set up as a singleton
	 */
	private static final DataFileSearcher INSTANCE = new DataFileSearcher();
	private DataFileSearcher() { }

	/**
	 * Get an instance of this class
	 * @return
	 */
	public static DataFileSearcher getInstance() { return INSTANCE; }
	
	/**
	 * Get all DataFiles associated with the given project
	 * @param p
	 * @return
	 * @throws Exception
	 */
	public List<DataFile> getDataFiles( Project p ) throws Exception {
		return getDataFiles( DataFileDataUtils.PROJECT, p.getID() );
	}
	
	/**
	 * Get all datafiles associated with this type of object (given the supplied object's ID)
	 * @param type
	 * @param typeID
	 * @return A List of DataFiles associated with the parameters sorted by upload date, an empty List if none are found
	 * @throws Exception
	 */
	private List<DataFile> getDataFiles( String type, int typeID ) throws Exception {

		List<DataFile> datafiles = new ArrayList<DataFile>();
		if( typeID == 0 ) return datafiles;
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			
			String sql = "SELECT file_id FROM ";
			sql += DataFileDataUtils.getTypeLocationMap().get( type ) + " WHERE ";
			sql += DataFileDataUtils.getTypeColumnMap().get( type ) + " = ? ORDER BY file_id";
			
			conn = DBConnectionManager.getPrConnection();
			
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, typeID );
			
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				datafiles.add( DataFileFactory.getInstance().getDataFile( rs.getInt( "file_id" ) ) );
			}
			
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
		
		
		return datafiles;
	}
	
}
