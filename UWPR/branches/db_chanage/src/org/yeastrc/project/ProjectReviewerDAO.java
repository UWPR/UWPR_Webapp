/**
 * ProjectReviewerDAO.java
 * @author Vagisha Sharma
 * Mar 26, 2009
 * @version 1.0
 */
package org.yeastrc.project;

import org.yeastrc.data.InvalidIDException;
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
public class ProjectReviewerDAO {

   private static final ProjectReviewerDAO instance = new ProjectReviewerDAO();
   
   private ProjectReviewerDAO() {}
   
   public static ProjectReviewerDAO instance() {
       return instance;
   }
   
   public void saveProjectReviewer(ProjectReviewer reviewer) throws SQLException {
       // Get our connection to the database.
       Connection conn = getConnection();
       Statement stmt = null;
       ResultSet rs = null;
       try {
           
           stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
           String sqlStr = "SELECT * FROM projectReviewer WHERE projectID = " + reviewer.getProjectId()+
                           " AND researcherID = "+reviewer.getResearcher().getID();
           
           rs = stmt.executeQuery(sqlStr);
           boolean exists = rs.next();
           
           if(!exists) {
               rs.moveToInsertRow();
           }
           rs.updateInt("projectID", reviewer.getProjectId());
           rs.updateInt("researcherID", reviewer.getResearcher().getID());
           if(reviewer.getRecommendedStatus() != null) {
               rs.updateString("recommendedStatus", reviewer.getRecommendedStatus().getShortName());
           }
           rs.updateString("comments", reviewer.getComment());
           rs.updateString("emailComment", reviewer.getEmailComment());
           
           if(exists)
               rs.updateRow();
           else
               rs.insertRow();
           
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
       
       if(reviewer.getRejectionCauses().size() > 0) {
           
           RejectionCauseDAO rcDao = RejectionCauseDAO.instance();
           rcDao.saveProjectRejectionCauses(reviewer.getProjectId(), 
                   reviewer.getResearcher().getID(), 
                   reviewer.getRejectionCauseIds());
       }
   }
   
   public List<Integer> getProjectReviewerIds(int projectId) throws SQLException {
       
       // Get our connection to the database.
       Connection conn = getConnection();
       Statement stmt = null;
       ResultSet rs = null;
       
       try {
           String sql = "SELECT researcherID from projectReviewer WHERE projectID="+projectId+" ORDER BY researcherID";
           
           stmt = conn.createStatement();
           rs = stmt.executeQuery(sql);
           
           List<Integer> reviewers = new ArrayList<Integer>();
           while(rs.next()) {
               reviewers.add(rs.getInt("researcherID"));
           }
           return reviewers;
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
   
   public List<ProjectReviewer> getProjectReviewers(int projectId) throws SQLException {
       
       // Get our connection to the database.
       Connection conn = getConnection();
       Statement stmt = null;
       ResultSet rs = null;
       
       try {
           String sql = "SELECT * from projectReviewer WHERE projectID="+projectId+" ORDER BY researcherID";
           
           stmt = conn.createStatement();
           rs = stmt.executeQuery(sql);
           
           List<ProjectReviewer> reviewers = new ArrayList<ProjectReviewer>();
           while(rs.next()) {
               Researcher researcher = new Researcher();
               try {
                   researcher.load(rs.getInt("researcherID"));
               }
               catch (InvalidIDException e) {
                   continue;
               }
               ProjectReviewer reviewer = new ProjectReviewer();
               reviewer.setProjectId(projectId);
               reviewer.setResearcher(researcher);
               reviewer.setRecommendedStatus(CollaborationStatus.statusForString(rs.getString("recommendedStatus")));
               reviewer.setComment(rs.getString("comments"));
               reviewer.setEmailComment(rs.getString("emailComment"));
               reviewers.add(reviewer);
               
               // If the reviewer recommended to reject this project, get the reasons
               if(reviewer.getRecommendedStatus() == CollaborationStatus.REJECTED) {
                   RejectionCauseDAO rcDao = RejectionCauseDAO.instance();
                   List<CollaborationRejectionCause> rejectionCauses = 
                       rcDao.getProjectReviewerRejectionCauses(reviewer.getProjectId(), reviewer.getResearcher().getID());
                   reviewer.setRejectionCauses(rejectionCauses);
               }
           }
           return reviewers;
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
   
   public int getLastProjectReviewedBy(int researcherId) throws SQLException{
       
       // Get our connection to the database.
       Connection conn = getConnection();
       Statement stmt = null;
       ResultSet rs = null;
       
       try {
           String sql = "SELECT projectID "+
                        "FROM projectReviewer "+
                        "WHERE researcherID =  "+researcherId+
                        " ORDER BY projectID DESC LIMIT 1";
           
           stmt = conn.createStatement();
           rs = stmt.executeQuery(sql);
           
           if(rs.next()) {
               return rs.getInt("projectID");
           }
           else
               return 0;
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

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getPrConnection();
    }

    public int getLastProjectReviewedBy(int researcherId1, int researcherId2) throws SQLException{
       
       // Get our connection to the database.
       Connection conn = getConnection();
       Statement stmt = null;
       ResultSet rs = null;
       
       try {
           String sql = "SELECT pr1.projectID AS projectID "+
                        "FROM projectReviewer AS pr1, projectReviewer AS pr2 "+
                        "WHERE pr1.projectID = pr2.projectID  "+
                        " AND pr1.researcherID = "+researcherId1+
                        " AND pr2.researcherID = "+researcherId2+
                        " ORDER BY projectID DESC LIMIT 1";
           
           stmt = conn.createStatement();
           rs = stmt.executeQuery(sql);
           
           if(rs.next()) {
               return rs.getInt("projectID");
           }
           else
               return 0;
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
   
   public void deleteProjectReviewers(int projectId) throws SQLException {
       // Get our connection to the database.
       Connection conn = null;
       Statement stmt = null;
       
       try {
           conn = getConnection();
           stmt = conn.createStatement();
           String sqlStr = "DELETE FROM projectReviewer WHERE projectID = "+projectId;
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
}
