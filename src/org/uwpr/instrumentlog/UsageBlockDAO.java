package org.uwpr.instrumentlog;

import org.apache.commons.lang.StringUtils;
import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.costcenter.InstrumentRateDAO;
import org.uwpr.costcenter.TimeBlock;
import org.uwpr.costcenter.TimeBlockDAO;
import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vsharma on 1/25/2016.
 */
public class UsageBlockDAO
{
    /**
     * Returns a list of usage blocks for the given instrument ID that have their start OR end dates .
     * within the given date range.
     * If trim is true, blocks that have either their start or end dates outside of the range are trimmed.
     * @param instrumentID
     * @param startDate
     * @param endDate
     * @param trim
     * @return
     * @throws SQLException
     */
    public static List<UsageBlock> getUsageBlocksForInstrument(int instrumentID, java.util.Date startDate, java.util.Date endDate,
                                                        boolean trim) throws SQLException
    {
       return getUsageBlocksForInstrument(instrumentID, startDate, endDate, trim,
               false); // Do not include signup-only blocks
    }

    /**
     * Returns a list of usage blocks for the given instrument ID that have their start OR end dates .
     * within the given date range.
     * If trim is true, blocks that have either their start or end dates outside of the range are trimmed.
     * @param instrumentID
     * @param startDate
     * @param endDate
     * @param trim
     * @return
     * @throws SQLException
     */
    public static List<UsageBlock> getUsageBlocksForInstrument(int instrumentID, java.util.Date startDate, java.util.Date endDate,
                                                               boolean trim, boolean includeDeleted) throws SQLException
    {
        // Get our connection to the database.
        Connection conn = null;
        try
        {
            conn = InstrumentUsageDAO.getConnection();
            return getUsageBlocksForInstrument(instrumentID, startDate, endDate, trim, null,
                    includeDeleted,
                    conn);

        }
        finally
        {
            // Make sure the connection is returned to the pool
            if (conn != null)
            {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    static List<UsageBlock> getUsageBlocksForInstrument(int instrumentID,
                                                 java.util.Date startDate, java.util.Date endDate, boolean trim,
                                                 String sortBy, boolean includeDeleted, Connection conn) throws SQLException
    {
        if (conn == null)
            return null;
        UsageBlockFilter filter = new UsageBlockFilter();
        filter.setContainedInRange(false);
        filter.setInstrumentId(instrumentID);
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setSortColumn(sortBy);
        filter.setTrimToFit(trim);
        if(includeDeleted)
        {
            filter.setBlockType(UsageBlockBaseFilter.BlockType.ALL);
        }
        return getUsageBlocks(filter);
    }

    private static UsageBlock makeUsageBlock(ResultSet rs) throws SQLException {
        UsageBlock blk = new UsageBlock();
        blk.setID(rs.getInt("id"));
        blk.setResearcherID(rs.getInt("enteredBy"));
        blk.setUpdaterResearcherID(rs.getInt("updatedBy"));
        blk.setInstrumentID(rs.getInt("instrumentID"));
        blk.setInstrumentOperatorId(rs.getInt("instrumentOperatorId"));
        blk.setOperatorName(rs.getString("operatorName"));
        blk.setInstrumentRateID(rs.getInt("instrumentRateID"));
        blk.setInstrumentName(rs.getString("name"));
        blk.setProjectID(rs.getInt("projectID"));
        blk.setProjectTitle(rs.getString("projectTitle"));
        blk.setPIID(rs.getInt("projectPI"));
        blk.setProjectPI(rs.getString("projectPiName"));
        blk.setStartDate(rs.getTimestamp("startDate"));
        blk.setEndDate(rs.getTimestamp("endDate"));
        blk.setDateCreated(rs.getTimestamp("dateEntered"));
        blk.setDateChanged(rs.getTimestamp("lastChanged"));
        blk.setNotes(rs.getString("notes"));
        blk.setInvoiceDate(rs.getTimestamp("invoiceDate"));
        blk.setDeleted(rs.getBoolean("deleted"));
        blk.setSetupBlock(rs.getBoolean("setupBlock"));

        return blk;
    }

    public static List<UsageBlock> getUsageBlocks(UsageBlockFilter filter) throws SQLException
    {
        return getUsageBlocks(filter, true);
    }

    public static List<UsageBlock> getUsageBlocks(UsageBlockFilter filter, boolean getPaymentMethods) throws SQLException
    {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder buf = new StringBuilder();

        buf.append("SELECT insUsg.*, " +
                        "ins.name, "
                        + "proj.projectTitle, proj.projectPI, " +
                        "r.researcherLastName AS projectPiName, operator.researcherLastName AS operatorName " +
                        ", invoice.createDate AS invoiceDate "
        );
        buf.append(" FROM " + DBConnectionManager.getInstrumentsTableSQL() + " AS ins ");
        buf.append(" INNER JOIN instrumentUsage AS insUsg ON ins.id=insUsg.instrumentID ");
        buf.append(" INNER JOIN tblProjects AS proj ON proj.projectID=insUsg.projectID ");
        buf.append(" INNER JOIN tblResearchers AS r ON r.researcherID=proj.projectPI ");
        buf.append(" LEFT OUTER JOIN tblResearchers AS operator ON operator.researcherID=insUsg.instrumentOperatorId ");
        buf.append(" LEFT OUTER JOIN ( invoice, invoiceInstrumentUsage as invBlk )");
        buf.append(" ON ( invBlk.instrumentUsageID = insUsg.id AND invoice.id=invBlk.invoiceID )");

        String joiner = " WHERE ";

        if(filter.getBlockType() != UsageBlockBaseFilter.BlockType.ALL)
        {
            buf.append(joiner);
            buf.append(" deleted = ").append((filter.getBlockType() == UsageBlockBaseFilter.BlockType.SIGNUP_ONLY) ? 1 : 0);
            joiner = " AND ";
        }

        if(filter.getProjectId() != 0)
        {
            buf.append(joiner);
            buf.append(" insUsg.projectID=").append(filter.getProjectId());
            joiner=" AND ";
        }
        if(filter.getInstrumentId() != 0)
        {
            buf.append(joiner);
            buf.append(" ins.id = ").append(filter.getInstrumentId());
            joiner=" AND ";
        }
        if(filter.getInstrumentOperatorId() != 0)
        {
            buf.append(joiner);
            buf.append(" insUsg.instrumentOperatorId = ").append(filter.getInstrumentOperatorId());
            joiner=" AND ";
        }
        if(filter.isContainedInRange())
        {
            if(filter.getStartDate() != null)
            {
                buf.append(joiner);
                buf.append(" startDate >= '").append(UsageBlockBaseDAO.dateFormat.format(filter.getStartDate())).append("'");
                joiner = " AND ";
            }
            if(filter.getEndDate() != null)
            {
                buf.append(joiner);
                buf.append(" endDate <= '").append(UsageBlockBaseDAO.dateFormat.format(filter.getEndDate())).append("'");
            }
        }
        else
        {
            if(filter.getEndDate() != null)
            {
                buf.append(joiner);
                buf.append(" startDate < '").append(UsageBlockBaseDAO.dateFormat.format(filter.getEndDate())).append("'");
                joiner = " AND ";
            }
            if(filter.getStartDate() != null)
            {
                buf.append(joiner);
                buf.append(" endDate > '").append(UsageBlockBaseDAO.dateFormat.format(filter.getStartDate())).append("'");
            }
        }

        if (!StringUtils.isBlank(filter.getSortColumn()))
        {
            buf.append(" ORDER BY " + StringUtils.trim(filter.getSortColumn()));
        }

        String sql = buf.toString();
        //System.out.println(sql);

        List <UsageBlock> usageBlks = new ArrayList<UsageBlock>();

        InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();
        try {
            conn = InstrumentUsageDAO.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {

                UsageBlock newBlk = makeUsageBlock(rs);

                if(getPaymentMethods) {
                    // Get all payment methods used for this block if there is no payment method in the filter.
                    List<InstrumentUsagePayment> payments = iupDao.getPaymentsForUsage(newBlk.getID(), filter.getPaymentMethodId());
                    if (payments == null || payments.isEmpty())
                        continue;

                    newBlk.setPayments(payments);
                }

                // Get the instrument rate
                InstrumentRateDAO rateDao = InstrumentRateDAO.getInstance();
                InstrumentRate rate = rateDao.getInstrumentRate(newBlk.getInstrumentRateID());
                if(rate == null)
                    throw new SQLException("No instrument rate found for ID: "+newBlk.getInstrumentRateID());
                newBlk.setRate(rate);

                // Trim blocks if required
                if(filter.isTrimToFit())
                {
                    UsageBlockBaseDAO.truncateBlock(newBlk, filter.getStartDate(), filter.getEndDate());
                }
                usageBlks.add(newBlk);
            }
            return usageBlks;
        }

        finally {
            // Always make sure result sets and statements are closed,
            if(conn != null) try {conn.close();} catch(SQLException e){}
            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
        }
    }

    public static List<UsageBlock> getHourlyUsageBlocksForPaymentMethod(int paymentMethodId) throws SQLException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        StringBuilder buf = new StringBuilder();

        buf.append("SELECT insUsg.*, " +
                "ins.name, "
                + "proj.projectTitle, proj.projectPI, " +
                "r.researcherLastName AS projectPiName, operator.researcherLastName AS operatorName " +
                ", invoice.createDate AS invoiceDate "
        );
        buf.append(" FROM " + DBConnectionManager.getInstrumentsTableSQL() + " AS ins ");
        buf.append(" INNER JOIN instrumentUsage AS insUsg ON ins.id=insUsg.instrumentID ");
        buf.append(" INNER JOIN tblProjects AS proj ON proj.projectID=insUsg.projectID ");
        buf.append(" INNER JOIN tblResearchers AS r ON r.researcherID=proj.projectPI ");
        buf.append(" INNER JOIN instrumentUsagePayment AS iup ON insUsg.Id = iup.InstrumentUsageId ");
        buf.append(" INNER JOIN instrumentRate AS ir ON insUsg.instrumentRateID = ir.Id" );
        buf.append(" LEFT OUTER JOIN tblResearchers AS operator ON operator.researcherID=insUsg.instrumentOperatorId ");
        buf.append(" LEFT OUTER JOIN ( invoice, invoiceInstrumentUsage as invBlk )");
        buf.append(" ON ( invBlk.instrumentUsageID = insUsg.id AND invoice.id=invBlk.invoiceID )");

        buf.append(" WHERE iup.paymentMethodId = ").append(paymentMethodId);
        buf.append(" AND ir.blockID = ").append(" (SELECT id FROM timeBlock WHERE name='").append(TimeBlock.HOURLY).append("')"); // LIMIT to hourly blocks

        String sql = buf.toString();
        //System.out.println(sql);

        List <UsageBlock> usageBlks = new ArrayList<>();

        try {
            conn = InstrumentUsageDAO.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while (rs.next()) {

                UsageBlock newBlk = makeUsageBlock(rs);

                // Get the instrument rate
                InstrumentRateDAO rateDao = InstrumentRateDAO.getInstance();
                InstrumentRate rate = rateDao.getInstrumentRate(newBlk.getInstrumentRateID());
                if(rate == null)
                    throw new SQLException("No instrument rate found for ID: "+newBlk.getInstrumentRateID());
                newBlk.setRate(rate);
                if(rate.getTimeBlock().getName().equals(TimeBlock.HOURLY)) {
                    usageBlks.add(newBlk);
                }
            }
            return usageBlks;
        }

        finally {
            // Always make sure result sets and statements are closed,
            if(conn != null) try {conn.close();} catch(SQLException e){}
            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
        }
    }
}
