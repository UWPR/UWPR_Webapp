package org.uwpr.notice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoticeDAO {

    private NoticeDAO() {}

    private static final Logger log = LogManager.getLogger(NoticeDAO.class);

    private static NoticeDAO instance = new NoticeDAO();

    public static NoticeDAO getInstance() {
        return instance;
    }

    public Notice getNotice(int noticeId) throws SQLException {
        String sql = "SELECT * FROM notice WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, noticeId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return makeNotice(rs);
            }
            log.error("No entry found in table notice for id: " + noticeId);
            return null;
        }
        finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    public List<Notice> getAllNotices() throws SQLException {
        String sql = "SELECT * FROM notice ORDER BY startDate DESC, id DESC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            List<Notice> notices = new ArrayList<Notice>();
            while (rs.next()) {
                notices.add(makeNotice(rs));
            }
            return notices;
        }
        finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    public List<Notice> getActiveNotices(Date asOf) throws SQLException {
        String sql = "SELECT * FROM notice WHERE startDate <= ? AND endDate >= ? ORDER BY startDate ASC, id ASC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            java.sql.Date day = new java.sql.Date(asOf.getTime());
            stmt.setDate(1, day);
            stmt.setDate(2, day);
            rs = stmt.executeQuery();
            List<Notice> notices = new ArrayList<Notice>();
            while (rs.next()) {
                notices.add(makeNotice(rs));
            }
            return notices;
        }
        finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) {}
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    public void saveNotice(Notice notice) throws SQLException {
        String sql = "INSERT INTO notice (startDate, endDate, message, createdBy, createDate) VALUES (?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(notice.getStartDate().getTime()));
            stmt.setDate(2, new java.sql.Date(notice.getEndDate().getTime()));
            stmt.setString(3, notice.getMessage());
            stmt.setInt(4, notice.getCreatedBy());
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    public int updateNotice(Notice notice) throws SQLException {
        String sql = "UPDATE notice SET startDate=?, endDate=?, message=? WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(notice.getStartDate().getTime()));
            stmt.setDate(2, new java.sql.Date(notice.getEndDate().getTime()));
            stmt.setString(3, notice.getMessage());
            stmt.setInt(4, notice.getId());
            return stmt.executeUpdate();
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    public void deleteNotice(int noticeId) throws SQLException {
        String sql = "DELETE FROM notice WHERE id=?";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, noticeId);
            stmt.executeUpdate();
        }
        finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) {}
            if (conn != null) try { conn.close(); } catch (SQLException e) {}
        }
    }

    private Notice makeNotice(ResultSet rs) throws SQLException {
        Notice n = new Notice();
        n.setId(rs.getInt("id"));
        n.setStartDate(rs.getDate("startDate"));
        n.setEndDate(rs.getDate("endDate"));
        n.setMessage(rs.getString("message"));
        n.setCreatedBy(rs.getInt("createdBy"));
        n.setCreateDate(rs.getTimestamp("createDate"));
        return n;
    }

    private Connection getConnection() throws SQLException {
        return DBConnectionManager.getMainDbConnection();
    }
}
