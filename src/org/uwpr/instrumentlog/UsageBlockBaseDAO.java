package org.uwpr.instrumentlog;

import org.apache.commons.lang.StringUtils;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vsharma on 1/25/2016.
 */
public class UsageBlockBaseDAO
{
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

    public static UsageBlockBase getUsageBlockBase(int usageID)
    {
        // Get our connection to the database.
        Connection conn = null;
        try
        {
            conn = InstrumentUsageDAO.getConnection();
            return getUsageBlockBase(usageID, conn);

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Make sure the connection is returned to the pool
            if (conn != null)
            {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
        return null;
    }

    private static UsageBlockBase getUsageBlockBase(int usageID, Connection conn)
    {
        if (conn == null)
            return null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT * from instrumentUsage WHERE id="+usageID;
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            if (rs.next())
            {
                return makeUsageBlockBase(rs);
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        finally
        {
            // Always make sure result sets and statements are closed,
            if (rs != null)
            {
                try { rs.close(); } catch (SQLException ignored) {}
            }
            if (stmt != null)
            {
                try { stmt.close(); } catch (SQLException ignored) {}
            }
        }

        return null;
    }

    public static void truncateBlock(UsageBlockBase newBlk, java.util.Date startDate, java.util.Date endDate)
    {
        if(startDate != null)
        {
            Date start = newBlk.getStartDate();
            start = start.after(startDate) ? start : startDate; // DateUtils.defaultStartTime(startDate);
            newBlk.setStartDate(start);
        }
        if(endDate != null)
        {
            Date end = newBlk.getEndDate();
            end = end.before(endDate) ? end : endDate; // DateUtils.defaultEndTime(endDate);
            newBlk.setEndDate(end);
        }

    }

    private static UsageBlockBase makeUsageBlockBase(ResultSet rs) throws SQLException
    {
        UsageBlockBase blk = new UsageBlockBase();
        blk.setID(rs.getInt("id"));
        blk.setResearcherID(rs.getInt("enteredBy"));
        blk.setUpdaterResearcherID(rs.getInt("updatedBy"));
        blk.setInstrumentID(rs.getInt("instrumentID"));
        blk.setInstrumentOperatorId(rs.getInt("instrumentOperatorId"));
        blk.setInstrumentRateID(rs.getInt("instrumentRateID"));
        blk.setProjectID(rs.getInt("projectID"));
        blk.setStartDate(rs.getTimestamp("startDate"));
        blk.setEndDate(rs.getTimestamp("endDate"));
        blk.setDateCreated(rs.getTimestamp("dateEntered"));
        blk.setDateChanged(rs.getTimestamp("lastChanged"));
        blk.setNotes(rs.getString("notes"));
        blk.setDeleted(rs.getBoolean("deleted"));
        blk.setSetupBlock(rs.getBoolean("setupBlock"));
        return blk;
    }

    /**
     * Returns a list of usage blocks for the given instrument operator.
     * If containedInRange is true, only blocks that start AND end in the given time range are returned.
     * If startDate or endDate are null they are ignored.
     * @throws SQLException
     */
    public static List<UsageBlockBase> getUsageBlocksForInstrumentOperator(int instrumentOperatorId, Date startDate, Date endDate,
                                                                           boolean containedInRange) throws SQLException
    {
        if(instrumentOperatorId <= 0)
        {
            throw new SQLException("Invalid instrumentOperatorId: " + instrumentOperatorId);
        }
        UsageBlockBaseFilter filter = new UsageBlockBaseFilter();
        filter.setInstrumentOperatorId(instrumentOperatorId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setContainedInRange(containedInRange);
        return getUsageBlocks(filter);
    }

    /**
     * Returns a list of usage blocks for the given project
     * If containedInRange is true, only blocks that start AND end in the given time range are returned.
     * If startDate or endDate are null they are ignored.
     * @throws SQLException
     */
    public static List<UsageBlockBase> getUsageBlocksForProject(int projectId, Date startDate, Date endDate,
                                                                boolean containedInRange) throws SQLException
    {
        if(projectId <= 0)
        {
            throw new SQLException("Invalid projectId: " + projectId);
        }
        UsageBlockBaseFilter filter = new UsageBlockBaseFilter();
        filter.setProjectId(projectId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setContainedInRange(containedInRange);
        return getUsageBlocks(filter);
    }

    public static List<UsageBlockBase> getUsageBlocksForBilling(int projectId, Date startDate, Date endDate) throws SQLException
    {
        if(projectId <= 0)
        {
            throw new SQLException("Invalid projectId: " + projectId);
        }
        UsageBlockBaseFilter filter = new UsageBlockBaseFilter();
        filter.setProjectId(projectId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setContainedInRange(false);
        return getUsageBlocks(filter);
    }

    /**
     * Returns a list of usage blocks for the given instrument rate.
     * If containedInRange is true, only blocks that start AND end in the given time range are returned.
     * If startDate or endDate are null they are ignored.
     * @throws SQLException
     */
    public static List<UsageBlockBase> getUsageBlocksForInstrumentRate(int instrumentRateId, Date startDate, Date endDate,
                                                                           boolean containedInRange) throws SQLException
    {
        if(instrumentRateId <= 0)
        {
            throw new SQLException("Invalid instrumentRateId: " + instrumentRateId);
        }
        UsageBlockBaseFilter filter = new UsageBlockBaseFilter();
        filter.setInstrumentRateId(instrumentRateId);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setContainedInRange(containedInRange);
        return getUsageBlocks(filter);
    }

    private static List<UsageBlockBase> getUsageBlocks(UsageBlockBaseFilter filter) throws SQLException
    {
        Connection conn = null;

        try {
            conn = InstrumentUsageDAO.getConnection();
            return getUsageBlocks(conn, filter);
        }
        finally {
            // Always make sure result sets and statements are closed,
            if(conn != null) try {conn.close();} catch(SQLException ignored){}
        }
    }

    private static List<UsageBlockBase> getUsageBlocks(Connection conn, UsageBlockBaseFilter filter) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;

        String sql = makeSql(filter);

        List <UsageBlockBase> usageBlks = new ArrayList<UsageBlockBase>();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {

                UsageBlockBase newBlk = makeUsageBlockBase(rs);
                usageBlks.add(newBlk);

            }
            return usageBlks;
        }

        finally {
            // Always make sure result sets and statements are closed,
            if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
            if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }

    public static boolean hasUsageBlockEndsAt(int projectId, int instrumentId, Date endDate) throws SQLException {

        Connection conn = null;

        try {
            conn = InstrumentUsageDAO.getConnection();
            return hasUsageBlockEndsAt(conn, projectId, instrumentId, endDate);

        }
        finally {
            // Always make sure result sets and statements are closed,
            if(conn != null) try {conn.close();} catch(SQLException ignored){}
        }
    }

    public static boolean hasUsageBlockEndsAt(Connection conn, int projectId, int instrumentId, Date endDate) throws SQLException {

        return getUsageBlockEndsAt(conn, projectId, instrumentId, endDate) != null;
    }

    public static UsageBlockBase getUsageBlockEndsAt(int projectId, int instrumentId, Date endDate) throws SQLException {

        Connection conn = null;

        try {
            conn = InstrumentUsageDAO.getConnection();
            return getUsageBlockEndsAt(conn, projectId, instrumentId, endDate);

        }
        finally {
            // Always make sure result sets and statements are closed,
            if(conn != null) try {conn.close();} catch(SQLException ignored){}
        }
    }

    public static UsageBlockBase getUsageBlockEndsAt(Connection conn, int projectId, int instrumentId, Date endDate) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder("SELECT * FROM instrumentUsage WHERE projectID = ");
        sql.append(projectId);
        sql.append(" AND instrumentID= ").append(instrumentId);
        sql.append(" AND deleted = 0");
        sql.append(" AND endDate = '").append(dateFormat.format(endDate)).append("'");
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());
            if (rs.next()) {
                return makeUsageBlockBase(rs);
            }
            return null;

        }
        finally {
            // Always make sure result sets and statements are closed,
            if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
            if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }

    public static UsageBlockBase getUsageBlockStartsAt(int projectId, int instrumentId, Date startDate) throws SQLException {

        Connection conn = null;
        try {
            conn = InstrumentUsageDAO.getConnection();
            return getUsageBlockStartsAt(conn, projectId, instrumentId, startDate);
        }
        finally {
            // Always make sure result sets and statements are closed,
            if(conn != null) try {conn.close();} catch(SQLException ignored){}
        }
    }

    public static UsageBlockBase getUsageBlockStartsAt(Connection conn, int projectId, int instrumentId, Date startDate) throws SQLException {

        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder("SELECT * FROM instrumentUsage WHERE projectID = ");
        sql.append(projectId);
        sql.append(" AND instrumentID= ").append(instrumentId);
        sql.append(" AND deleted = 0");
        sql.append(" AND startDate = '").append(dateFormat.format(startDate)).append("'");
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql.toString());
            if (rs.next()) {
                return makeUsageBlockBase(rs);
            }
            return null;

        }
        finally {
            // Always make sure result sets and statements are closed,
            if(stmt != null) try {stmt.close();} catch(SQLException ignored){}
            if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }

    static String makeSql(UsageBlockBaseFilter filter)
    {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT * FROM instrumentUsage");
        String joiner = " WHERE ";

        if(filter.getProjectId() != 0)
        {
            buf.append(joiner);
            buf.append(" projectID = ").append(filter.getProjectId());
            joiner = " AND ";
        }
        if(filter.getInstrumentId() != 0)
        {
            buf.append(joiner);
            buf.append(" instrumentID = ").append(filter.getInstrumentId());
            joiner = " AND ";
        }
        if(filter.getInstrumentRateId() != 0)
        {
            buf.append(joiner);
            buf.append(" instrumentRateID = ").append(filter.getInstrumentRateId());
            joiner = " AND ";
        }
        if(filter.getInstrumentOperatorId() != 0)
        {
            buf.append(joiner);
            buf.append(" instrumentOperatorId = ").append(filter.getInstrumentOperatorId());
            joiner = " AND ";
        }
        if(filter.isContainedInRange())
        {
            if(filter.getStartDate() != null)
            {
                buf.append(joiner);
                buf.append(" startDate >= '").append(dateFormat.format(filter.getStartDate())).append("'");
                joiner = " AND ";
            }
            if(filter.getEndDate() != null)
            {
                buf.append(joiner);
                buf.append(" endDate <= '").append(dateFormat.format(filter.getEndDate())).append("'");
            }
        }
        else
        {
            if(filter.getEndDate() != null)
            {
                buf.append(joiner);
                buf.append(" startDate < '").append(dateFormat.format(filter.getEndDate())).append("'");
                joiner = " AND ";
            }
            if(filter.getStartDate() != null)
            {
                buf.append(joiner);
                buf.append(" endDate > '").append(dateFormat.format(filter.getStartDate())).append("'");
            }
        }

        if (!StringUtils.isBlank(filter.getSortColumn()))
        {
            buf.append(" ORDER BY " + StringUtils.trim(filter.getSortColumn()));
        }

        return buf.toString();
    }
}
