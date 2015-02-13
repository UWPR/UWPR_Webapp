/**
 * ProjectRawFileUsageUtils.java
 * @author Vagisha Sharma
 * Jan 13, 2009
 * @version 1.0
 */
package org.uwpr.instrumentlog.rawfile;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * 
 */
public class ProjectRawFileUsageUtils {

    private static final Logger log = Logger.getLogger(ProjectRawFileUsageUtils.class);
    private static final ProjectRawFileUsageUtils instance = new ProjectRawFileUsageUtils();
    
    private static Date lastDateParsed;
    
    private ProjectRawFileUsageUtils() {}
    
    public static ProjectRawFileUsageUtils instance() {
        return instance;
    }
    
    public static void setLastDateParsed(Date date) {
        lastDateParsed = date;
    }
    
    public static Date getLastDateParsed() {
        return lastDateParsed;
    }
    
    public void saveUsage(List<ProjectRawFileUsage> usageList) throws Exception {
        for(ProjectRawFileUsage usage: usageList)
            saveUsage(usage);
    }
    
    public void saveUsage(ProjectRawFileUsage usage) throws Exception {

        // Get our connection to the database.
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // Get our updatable result set
            String sqlStr = "SELECT * FROM projectRawDataSummary WHERE projectID = " + usage.getProjectID();
            rs = stmt.executeQuery(sqlStr);

            if(rs.next()) {
                rs.updateInt("rawFileCount", usage.getRawFileCount());
                rs.updateFloat("rawFileSize", usage.getRawFileSize());
                rs.updateString("dataDirectory", usage.getDataDirectory());
                // Update the row
                rs.updateRow();
            }
            else {
                // We're adding a new row.
                rs.moveToInsertRow();
                rs.updateInt("projectID", usage.getProjectID());
                rs.updateInt("rawFileCount", usage.getRawFileCount());
                rs.updateFloat("rawFileSize", usage.getRawFileSize());
                rs.updateString("dataDirectory", usage.getDataDirectory());
                rs.insertRow();

            }
        }
        catch(SQLException e) { throw e; }
        finally {
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
    }

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getPrConnection();
    }

    public ProjectRawFileUsage loadUsage(int projectId) throws SQLException {
        
        // Get our connection to the database.
        Connection conn = getConnection();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            String sqlStr = "SELECT * FROM projectRawDataSummary WHERE projectID = " +projectId;
            rs = stmt.executeQuery(sqlStr);

            if(rs.next()) {
                ProjectRawFileUsage usage = new ProjectRawFileUsage();
                usage.setProjectID(projectId);
                usage.setRawFileCount(rs.getInt("rawFileCount"));
                usage.setRawFileSize(rs.getFloat("rawFileSize"));
                usage.setDataDirectory(rs.getString("dataDirectory"));
                return usage;
            }
            else {
               log.error("No raw file usage information found for projectID: "+projectId);
               return null;
            }
        }
        catch(SQLException e) { throw e; }
        finally {
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
    }
}
