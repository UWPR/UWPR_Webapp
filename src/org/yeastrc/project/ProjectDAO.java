/**
 * ProjectDAO.java
 * @author Vagisha Sharma
 * Apr 8, 2009
 * @version 1.0
 */
package org.yeastrc.project;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ProjectDAO {

    private static ProjectDAO instance;
    
    private static final Logger log = Logger.getLogger(ProjectDAO.class.getName());
    
    private ProjectDAO() {}
    
    public static ProjectDAO instance() {
        if(instance == null)
            instance = new ProjectDAO();
        
        return instance;
    }
    
    /**
     * If the project represented by the projectId is extended return the 
     * id of the extended project
     * @param parentProjectId
     * @return
     * @throws SQLException 
     */
    public int getChildProjectId(int parentProjectId) throws SQLException {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            stmt = conn.createStatement();

            // Our SQL statement
            String sqlStr = "SELECT projectID FROM tblProjects WHERE parentProjectID = "+parentProjectId;

            // Our results
            rs = stmt.executeQuery(sqlStr);

            // results
            if(rs.next()) {
                return (rs.getInt("projectID"));
            }
        }
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
        return 0;
    }

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }

    /**
     * If the project represented by the projectId is an extension project
     * get a list of all of its parent projects.
     * @param childProjectId
     * @return
     * @throws SQLException 
     */
    public List<Integer> getAncestors(int childProjectId) throws SQLException {
        
        // Get our connection to the database.
        Connection conn = null; 

        List<Integer> ancestors = new ArrayList<Integer>();
        try{
            conn = getConnection();
            
            int maxAncestors = 5;
            int i = 0;
            int currChild = childProjectId;
            while(i < maxAncestors) {
                int parentId = getAncestorId(currChild, conn);
                if(parentId == 0)
                    break;
                
                ancestors.add(parentId);
                currChild = parentId;
                i++;
            }
        }
        finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { ; }
                conn = null;
            }
        }
        
        return ancestors;
    }
    
    private int getAncestorId(int childProjectId, Connection conn) {
        
        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement();

            // Our SQL statement
            String sqlStr = "SELECT parentProjectID FROM tblProjects WHERE projectID = "+childProjectId;

            // Our results
            rs = stmt.executeQuery(sqlStr);

            // results
            if(rs.next()) {
                return (rs.getInt("parentProjectID"));
            }
        }
        catch(SQLException e) { e.printStackTrace(); }
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
        }
        return 0;
        
    }
}
