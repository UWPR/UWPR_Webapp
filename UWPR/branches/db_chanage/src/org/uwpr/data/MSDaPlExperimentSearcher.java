/**
 * 
 */
package org.uwpr.data;

import org.uwpr.AppProperties;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vsharma
 *
 */
public class MSDaPlExperimentSearcher {


	/**
     * Gets a list of the all the MSDaPl experiments associated with the given project as DataURI objects.
     * @param project project
     * @return a list of MSDaPl experiments for this project as DataURI objects
     * @throws Exception
     */
	public static List<DataURI> searchByProject( Project project ) throws Exception
    {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<DataURI> dataList = new ArrayList<DataURI>();

		try {

			StringBuilder sql = new StringBuilder()
                    .append("SELECT exp.* FROM tblProjectExperiment AS pe INNER JOIN ")
                    .append(DBConnectionManager.MSDATA).append(".msExperiment AS exp ")
                    .append(" ON (exp.id = pe.experimentId) ")
                    .append(" WHERE pe.projectID = ?");
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement( sql.toString() );
			stmt.setInt( 1, project.getID() );
			rs = stmt.executeQuery();
			
			while (rs.next()) {
                int experimentId = rs.getInt("id");
                DataURI dataUri = new DataURI();
                dataUri.setId(0); // No database ID, since this is an entry from the msData.msExperiment table.
                dataUri.setProject(project);
                dataUri.setLastChange(rs.getDate("lastUpdate"));
                dataUri.setUri(String.format("%s/viewProject.do?ID=%s#Expt%s", AppProperties.getWebMsdaplBaseuri(),
                                             project.getID(), experimentId));
                dataUri.setComments(String.format("MSDaPl data; experiment ID %s", experimentId));
                dataList.add(dataUri);
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
