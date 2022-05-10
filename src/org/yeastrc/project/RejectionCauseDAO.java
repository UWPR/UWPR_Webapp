/**
 * RejectionCauseFinder.java
 * @author Vagisha Sharma
 * Feb 19, 2009
 * @version 1.0
 */
package org.yeastrc.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class RejectionCauseDAO {

    private static final RejectionCauseDAO instance = new RejectionCauseDAO();
    
    private static final Logger log = LogManager.getLogger(RejectionCauseDAO.class.getName());
    
    private RejectionCauseDAO() {}
    
    public static RejectionCauseDAO instance() {
        return instance;
    }
    
    /**
     * Returns a list of all rejection causes found in the database
     * @return
     * @throws SQLException 
     */
    public List<CollaborationRejectionCause> findAll() {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            stmt = conn.createStatement();

            // Our SQL statement
            String sqlStr = "SELECT * FROM collaborationRejectionCause";

            // Our results
            rs = stmt.executeQuery(sqlStr);

            // results
            List<CollaborationRejectionCause> list = new ArrayList<CollaborationRejectionCause>();
            while(rs.next()) {
                CollaborationRejectionCause cause = new CollaborationRejectionCause();
                cause.setId(rs.getInt("id"));
                cause.setCause(rs.getString("cause"));
                cause.setDescription(rs.getString("description"));
                list.add(cause);
            }
            return list;
        }
        catch(SQLException e) { log.error("Error finding all collaboration rejection causes", e); }
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
        return new ArrayList<CollaborationRejectionCause>(0);
    }
    
    public CollaborationRejectionCause find(int causeId) {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            stmt = conn.createStatement();

            // Our SQL statement
            String sqlStr = "SELECT * FROM collaborationRejectionCause WHERE id = "+causeId;

            // Our results
            rs = stmt.executeQuery(sqlStr);

            // results
            if(rs.next()) {
                CollaborationRejectionCause cause = new CollaborationRejectionCause();
                cause.setId(rs.getInt("id"));
                cause.setCause(rs.getString("cause"));
                cause.setDescription(rs.getString("description"));
                return cause;
            }
        }
        catch(SQLException e) { log.error("Error finding collaboration rejection cause with ID: "+causeId, e); }
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
        return null;
    }
    
    /**
     * Returs a list of rejection causes for the project (if any).
     * @param projectId
     * @return
     */
    public List<Integer> findProjectRejectionCauseIds(int projectId) {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            // Our SQL statement
            String sqlStr = "SELECT DISTINCT(rc.causeID) FROM collaborationRejected AS rc, collaborationRejectionCause AS crc "+
            "WHERE rc.projectID = "+projectId+" AND rc.causeID = crc.id";

            stmt = conn.createStatement();
            stmt.executeQuery(sqlStr);
            
            // Our results
            rs = stmt.executeQuery(sqlStr);
            List<Integer> list = new ArrayList<Integer>();
            while(rs.next()) {
                list.add(rs.getInt("causeID"));
            }
            return list;
        }
        catch(SQLException e) { log.error("Error finding rejection cause IDs for project ID: "+projectId, e); }
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
        return new ArrayList<Integer>(0);
    }
    
    public List<CollaborationRejectionCause> findProjectRejectionCauseList(int projectId) {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            // Our SQL statement
            String sqlStr = "SELECT * FROM collaborationRejected AS rc, collaborationRejectionCause AS crc "+
            "WHERE rc.projectID = "+projectId+" AND rc.causeID = crc.id GROUP BY rc.causeID";

            stmt = conn.createStatement();
            stmt.executeQuery(sqlStr);
            
            // Our results
            rs = stmt.executeQuery(sqlStr);
            List<CollaborationRejectionCause> list = new ArrayList<CollaborationRejectionCause>();
            while(rs.next()) {
                CollaborationRejectionCause cause = new CollaborationRejectionCause();
                cause.setId(rs.getInt("causeID"));
                cause.setCause(rs.getString("cause"));
                cause.setDescription(rs.getString("description"));
                list.add(cause);
            }
            return list;
        }
        catch(SQLException e) { log.error("Error finding rejection causes for project ID: "+projectId, e); }
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
        return new ArrayList<CollaborationRejectionCause>(0);
    }
    
    public List<Integer> getProjectReviewerRejectionCauseIds(int projectId, int reviewerId) {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        List<Integer> list = new ArrayList<Integer>();
        try{
            conn = DBConnectionManager.getMainDbConnection();
            // Our SQL statement
            String sqlStr = "SELECT causeID FROM collaborationRejected AS rc "+
            "WHERE projectID = "+projectId+" AND researcherID = "+reviewerId;

            stmt = conn.createStatement();
            stmt.executeQuery(sqlStr);
            
            // Our results
            rs = stmt.executeQuery(sqlStr);
            while(rs.next()) {
                list.add(rs.getInt("causeID"));
            }
        }
        catch(SQLException e) { log.error("Error finding rejection cause IDs for project ID: "+projectId+" and reviewerID: "+reviewerId, e); }
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
        return list;
    }
    
    
    public List<CollaborationRejectionCause> getProjectReviewerRejectionCauses (int projectId, int reviewerId) {
        
        // Get our connection to the database.
        Connection conn = null; 
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn = getConnection();
            // Our SQL statement
            String sqlStr = "SELECT * FROM collaborationRejected AS rc, collaborationRejectionCause AS crc "+
            "WHERE rc.projectID = "+projectId+" AND rc.researcherID = "+reviewerId+" AND rc.causeID = crc.id";

            stmt = conn.createStatement();
            stmt.executeQuery(sqlStr);
            
            // Our results
            rs = stmt.executeQuery(sqlStr);
            List<CollaborationRejectionCause> list = new ArrayList<CollaborationRejectionCause>();
            while(rs.next()) {
                CollaborationRejectionCause cause = new CollaborationRejectionCause();
                cause.setId(rs.getInt("causeID"));
                cause.setCause(rs.getString("cause"));
                cause.setDescription(rs.getString("description"));
                list.add(cause);
            }
            return list;
        }
        catch(SQLException e) { log.error("Error finding rejection causes for project ID: "+projectId+" and reviewerID: "+reviewerId, e); }
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
        return new ArrayList<CollaborationRejectionCause>(0);
    }
    
    
    public void saveProjectRejectionCauses(int projectId, int researcherId, List<Integer> causeIds) throws SQLException {
     
        log.info("Saving rejection causes for project: "+projectId);
        
        // now save
        // Get our connection to the database.
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = getConnection();
            
            // FIRST delete any existing rejection causes
            deleteReviewerProjectRejectionCauses(projectId, researcherId, conn);
            
            if(causeIds == null || causeIds.size() == 0) {
                log.warn("Rejection cause list is either null or empty for projectID: "+projectId);
                return;
            }
            
            stmt = conn.createStatement();
            String sqlStr = "INSERT INTO collaborationRejected (projectID, researcherID, causeID) VALUES ";
            for(Integer causeId: causeIds) {
                sqlStr += "("+projectId+", "+researcherId+", "+causeId+"),";
            }
            sqlStr = sqlStr.substring(0, sqlStr.length() - 1); // remove last comma;
            
            stmt.executeUpdate(sqlStr);
        }
        finally {

            // Always make sure result sets and statements are closed,
            // and the connection is returned to the pool
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

    public void deleteProjectRejectionCauses(int projectId) throws SQLException {
        
        // Get our connection to the database.
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            String sqlStr = "DELETE FROM collaborationRejected WHERE projectID = "+projectId;
            stmt.executeUpdate(sqlStr);
        }
        finally {

            // Always make sure result sets and statements are closed,
            // and the connection is returned to the pool
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
    
    private void deleteReviewerProjectRejectionCauses(int projectId, int reviewerId, Connection conn) throws SQLException {
        
        // Get our connection to the database.
        Statement stmt = null;
        
        try {
            stmt = conn.createStatement();
            String sqlStr = "DELETE FROM collaborationRejected WHERE projectID = "+projectId+" AND researcherID="+reviewerId;
            stmt.executeUpdate(sqlStr);
        }
        finally {

            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { ; }
                stmt = null;
            }
        }
    }
    
    public int addNewRejectionCause(CollaborationRejectionCause cause) throws SQLException {
     // Get our connection to the database.
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;    
        
        try {
            conn = getConnection();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sqlStr = "SELECT * FROM collaborationRejectionCause WHERE cause=\""+cause.getCause()+"\" AND description=\""+cause.getDescription()+"\"";
            rs = stmt.executeQuery(sqlStr);
            if(rs.next()) {
                return rs.getInt("id");
            }
            else {
                rs.moveToInsertRow();
                rs.updateString("cause", cause.getCause());
                rs.updateString("description", cause.getDescription());
                rs.insertRow();
                rs.last();
                return rs.getInt("id");
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
    }
}
