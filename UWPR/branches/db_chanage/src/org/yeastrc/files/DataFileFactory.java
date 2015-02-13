package org.yeastrc.files;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Researcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Get DataFiles!
 * @author Mike
 *
 */
public class DataFileFactory {

	private static final DataFileFactory INSTANCE = new DataFileFactory();
	public static DataFileFactory getInstance() { return INSTANCE; }
	private DataFileFactory() { }
	
	/**
	 * Get the requested data file
	 * @param id
	 * @return The DataFile
	 * @throws Exception
	 */
	public DataFile getDataFile( int id ) throws Exception {
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		DataFile datafile = null;
		
		try {
			
			conn = DBConnectionManager.getPrConnection();

			String sql = "SELECT id, filename, description, filesize, mimetype, uploadDate, uploadedBy FROM files WHERE id = ?";
			stmt = conn.prepareStatement( sql );
			stmt.setInt( 1, id );
			rs = stmt.executeQuery();
			
			if ( !rs.next() )
				throw new Exception( "Invalid file ID on DataFileFactory.getDataFile.  DataFile ID: " + id );
			
			datafile = new DataFile();

			datafile.setId( rs.getInt( "id" ) );
			datafile.setFilename( rs.getString( "filename" ) );
			datafile.setDescription( rs.getString( "description" ) );
			datafile.setFilesize( rs.getInt( "filesize" ) );
			datafile.setMimetype( rs.getString( "mimetype" ) );
			datafile.setTimestamp( (java.util.Date)rs.getDate( "uploadDate" ) );

			if( rs.getInt( "uploadedBy" ) != 0 ) {
				Researcher r = new Researcher();
				r.load( rs.getInt( "uploadedBy" ) );
			
				datafile.setUploader( r );
				r = null;
			}
			
			
			rs.close(); rs = null;
			stmt.close(); stmt = null;
			
			// set the type and typeID
			for( String loc : DataFileDataUtils.getTypeLocationMap().keySet() ) {
				sql = "SELECT * FROM " + DataFileDataUtils.getTypeLocationMap().get( loc ) + " WHERE file_id = ?";
				stmt = conn.prepareStatement( sql );
				stmt.setInt(1, id );
				rs = stmt.executeQuery();
				
				if ( rs.next() ) {
					datafile.setType( loc );
					datafile.setTypeID( rs.getInt( DataFileDataUtils.getTypeColumnMap().get( loc ) ) );
				}
				
				rs.close(); rs = null;
				stmt.close(); stmt = null;
			}
			
			if( datafile.getType() == null )
				throw new Exception( "Could not get type (e.g., yatesRun or project) for the datafile." );
			
			
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
		
		
		return datafile;
	}
	
}
