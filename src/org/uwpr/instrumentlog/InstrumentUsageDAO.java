/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.costcenter.*;
import org.uwpr.scheduler.UsageBlockPaymentInformation;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;

import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 
 */
public class InstrumentUsageDAO {

	private static final InstrumentUsageDAO instance = new InstrumentUsageDAO();
	
	private static final Logger log = LogManager.getLogger(InstrumentUsageDAO.class);

	private static final String dateUpdateMsg = "Date changed from {0} - {1} to  {2} - {3}";
	private static final String deletedByEditAction = "Deleted by edit action";
	private static final String addedByEditAction = "Added by edit action";

	private InstrumentUsageDAO () {}
	
	public static InstrumentUsageDAO getInstance() {
		return instance;
	}

	private void save(Connection conn, List<? extends UsageBlockBase> blocks, String message) throws Exception
	{
		if (blocks == null || blocks.size() == 0)
			return;

		log.info("Saving usage blocks");

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		PreparedStatement stmt = null;
		ResultSet rs = null;

		String sql = "INSERT INTO " +
				"instrumentUsage (projectID, instrumentID, instrumentOperatorId, instrumentRateID, startDate, endDate,enteredBy, dateEntered, updatedBy, notes, deleted, setupBlock) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

		try
		{
			for(UsageBlockBase block: blocks) {
				stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

				stmt.setInt(1, block.getProjectID());
				stmt.setInt(2, block.getInstrumentID());
				stmt.setInt(3, block.getInstrumentOperatorId());
				stmt.setInt(4, block.getInstrumentRateID());
				stmt.setTimestamp(5, makeTimestamp(block.getStartDate()));
				stmt.setTimestamp(6, makeTimestamp(block.getEndDate()));
				stmt.setInt(7, block.getResearcherID());

				block.setDateCreated(new java.util.Date(System.currentTimeMillis()));
				stmt.setTimestamp(8, makeTimestamp(block.getDateCreated()));

				if (block.getUpdaterResearcherID() != 0) {
					stmt.setInt(9, block.getUpdaterResearcherID());
				} else {
					stmt.setNull(9, Types.INTEGER);
				}

				stmt.setString(10, block.getNotes());

				stmt.setBoolean(11, block.isDeleted());

				stmt.setBoolean(12, block.isSetupBlock());

				stmt.executeUpdate();
				rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					block.setID(rs.getInt(1));
				} else {
					throw new SQLException("Error inserting row in instrumentBlock table. No auto-generated ID returned.");
				}

				logDao.logSignupAdded(conn, Collections.singletonList(block), block.getResearcherID(), message);
			}

		} finally {

			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	public void updateBlocksDates(Connection conn, List<? extends UsageBlockBase> blocks, String message) throws SQLException
	{
		updateBlocksDates(conn, blocks, Collections.singletonList(message));
	}

	public void updateBlocksDates(Connection conn, List<? extends UsageBlockBase> blocks, List<String> logMessages) throws SQLException
	{

		if (blocks == null || blocks.size() == 0)
			return;

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		PreparedStatement stmt = null;
		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
		sql.append(" startDate = ?");
		sql.append(", endDate = ?");
		sql.append(", instrumentRateID = ?");
		sql.append(", updatedBy = ?");
		sql.append(" WHERE id = ?");

		try {

			stmt = conn.prepareStatement(sql.toString());

			int i = 0;
			for(UsageBlockBase block: blocks)
			{
				String message = logMessages.size() == 1 ? logMessages.get(0) : logMessages.get(i++);

				logDao.logSignupEdited(conn, block, block.getUpdaterResearcherID(), message);

				stmt.setTimestamp(1, makeTimestamp(block.getStartDate()));
				stmt.setTimestamp(2, makeTimestamp(block.getEndDate()));
				stmt.setInt(3, block.getInstrumentRateID());
				stmt.setInt(4, block.getUpdaterResearcherID());
				stmt.setInt(5, block.getID());
				stmt.executeUpdate();
			}

		} finally {

			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

    public void updateBlocksProject(Connection conn, List<? extends UsageBlockBase> blocks, int newProjectId) throws Exception {

        if (blocks == null || blocks.size() == 0)
            return;

        log.info("Updating usage blocks (project) on instrument: " + blocks.get(0).getInstrumentID());

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		PreparedStatement stmt = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
        sql.append(" projectID = ?");
        sql.append(", updatedBy = ?");
        sql.append(" WHERE id = ?");
        try {

            stmt = conn.prepareStatement(sql.toString());

            for(UsageBlockBase block: blocks)
            {
                stmt.setInt(1, newProjectId);
                stmt.setInt(2, block.getUpdaterResearcherID());
                stmt.setInt(3, block.getID());
                stmt.executeUpdate();

				logDao.logSignupEdited(conn, block, block.getUpdaterResearcherID(),
						"Changed project from " + block.getProjectID() + " to " + newProjectId);
            }

        } finally {

            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
        }
    }

	public void updateBlocksInstrumentOperator(List<? extends UsageBlockBase> blocks, int newInstrumentOperator) throws Exception {

		if (blocks == null || blocks.size() == 0)
			return;

		log.info("Updating usage blocks (instrument operator) on instrument: " + blocks.get(0).getInstrumentID());

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
		sql.append(" instrumentOperatorId = ?");
		sql.append(", updatedBy = ?");
		sql.append(" WHERE id = ?");
		try {

			conn = getConnection();
			stmt = conn.prepareStatement(sql.toString());

			for(UsageBlockBase block: blocks)
			{
				stmt.setInt(1, newInstrumentOperator);
				stmt.setInt(2, block.getUpdaterResearcherID());
				stmt.setInt(3, block.getID());
				stmt.executeUpdate();

				logDao.logSignupEdited(conn, block, block.getUpdaterResearcherID(),
						"Changed instrument operator from " + block.getInstrumentOperatorId() + " to " + newInstrumentOperator);
			}

		} finally {

			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}

	public void deletedByEditAction(Connection conn, List<UsageBlockBase> usageBlocks, Researcher researcher) throws SQLException
	{
		delete(conn, usageBlocks, researcher, deletedByEditAction);
	}

	private int updateSetupStatusForDeletedBlocks(Connection conn, List<UsageBlockBase> deletedBlocks, Researcher researcher) throws SQLException {

		if (deletedBlocks == null || deletedBlocks.size() == 0)
			return 0;

		log.info("Updating setup blocks, if any.");

		List<Integer> blockIds = new ArrayList<>();
		for (UsageBlockBase block : deletedBlocks) {
			if (!block.isSetupBlock()) {
				continue;
			}

			// If there is an adjacent block make it the setup block
			UsageBlockBase adjBlock = UsageBlockBaseDAO.getUsageBlockStartsAt(block.getProjectID(), block.getInstrumentID(), block.getEndDate());
			if (adjBlock != null) {
				blockIds.add(adjBlock.getID());
			}
		}
		if (blockIds.size() == 0)
		{
			log.info("No blocks found to update.");
			return 0;
		}

		return makeSetupBlocks(conn, researcher, blockIds);
	}

	public int makeSetupBlocks(Connection conn, Researcher researcher, List<Integer> blockIds) throws SQLException
	{
		return updateSetupBlocks(conn, researcher, blockIds, Boolean.TRUE);
	}

	public int removeSetupFlag(Connection conn, Researcher researcher, List<Integer> blockIds) throws SQLException
	{
		return updateSetupBlocks(conn, researcher, blockIds, Boolean.FALSE);
	}


	private int updateSetupBlocks(Connection conn, Researcher researcher, List<Integer> blockIds, boolean makeSetup) throws SQLException
	{
		StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
		sql.append(" setupBlock = ?, ");
		sql.append(" updatedBy = ? ");
		sql.append(" WHERE id in (").append(StringUtils.join(blockIds, ",")).append(") ");

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try
		{
			stmt = conn.prepareStatement(sql.toString());
			stmt.setBoolean(1, makeSetup);
			stmt.setInt(2, researcher.getID());

			int numUpdated = stmt.executeUpdate();

			return numUpdated;

		} finally {

			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	static final Connection getConnection() throws SQLException
	{
        return DBConnectionManager.getMainDbConnection();
    }

    public boolean hasInstrumentUsageForInstrumentRate(int instrumentRateId) throws SQLException {
		
		String sql = "SELECT count(*) FROM instrumentUsage WHERE instrumentRateID = "+instrumentRateId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				int count = rs.getInt(1);
				if(count == 0)
					return false;
				else
					return true;
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return false;
	}

	public int getUsageBlockCountForProject(int projectId) throws SQLException {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String sql = "SELECT COUNT(*) FROM instrumentUsage WHERE projectID="+projectId;

		//System.out.println(sql);

		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if(rs.next())
				return rs.getInt(1);
			else
				return 0;
		}

		finally {
			// Always make sure result sets and statements are closed,
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	public void purge(UsageBlockBase block, Researcher researcher) throws SQLException {

		// NOTE: There is a trigger on instrumentUsage table that will 
		//       delete all entries in the instrumentUsagePayment where instrumentUsageID is equal to 
		//       the given usageId
		Connection conn = null;

		try {

			conn = getConnection();
			conn.setAutoCommit(false);
			delete(conn, Collections.singletonList(block), researcher, null);
			conn.commit();;
			
		} finally {
				if (conn != null) {
					try { conn.close(); } catch (SQLException ignored) { ; }
				}
		}
	}

	private void delete(Connection conn, List<UsageBlockBase> blocks, Researcher researcher, String message) throws SQLException {

		if(blocks == null || blocks.size() == 0)
		{
			return;
		}
		// NOTE: There is a trigger on instrumentUsage table that will
		//       delete all entries in the instrumentUsagePayment where instrumentUsageID is equal to
		//       the given usageId
		PreparedStatement stmt = null;
		String sql = "DELETE FROM instrumentUsage WHERE id=?";

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		try {
			stmt = conn.prepareStatement( sql );

			for(UsageBlockBase block: blocks)
			{
				log.info("Deleting usage block ID "+block.getID());
				stmt.setInt(1, block.getID());
				stmt.executeUpdate();

				message = message == null ? "" : message + ": ";
				logDao.logSignupPurged(conn, block, researcher.getID(), message + block.toString());
			}

		} finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException ignored) { ; }
			}
		}
	}

	public String saveUsageBlocks(
			Connection conn,
			List<? extends UsageBlockBase> usageBlocks,
			UsageBlockPaymentInformation paymentInfo)
	{
		return saveUsageBlocksForBilledProject(conn, usageBlocks, paymentInfo, null);
	}

	public String saveUsageBlocksByEditAction(
			Connection conn,
			List<? extends UsageBlockBase> usageBlocks,
			UsageBlockPaymentInformation paymentInfo)
	{
		return saveUsageBlocksForBilledProject(conn, usageBlocks, paymentInfo, addedByEditAction);
	}

	private String saveUsageBlocksForBilledProject(
			Connection conn,
			List<? extends UsageBlockBase> usageBlocks,
			UsageBlockPaymentInformation paymentInfo, String logMessage) {

		if(usageBlocks == null || usageBlocks.size() == 0)
		{
			return null;
		}

		List<UsageBlock> blocksWithPayment = new ArrayList<>(usageBlocks.size());
		for(UsageBlockBase blk: usageBlocks)
		{
			UsageBlock uBlk = new UsageBlock();
			blk.copyTo(uBlk);
			blocksWithPayment.add(uBlk);

			List<InstrumentUsagePayment> payments = new ArrayList<>();
			uBlk.setPayments(payments);
			for (int i = 0; i < paymentInfo.getCount(); i++)
			{
				PaymentMethod pm = paymentInfo.getPaymentMethod(i);
				BigDecimal perc = paymentInfo.getPercent(i);

				InstrumentUsagePayment usagePayment = new InstrumentUsagePayment();
				usagePayment.setPaymentMethod(pm);
				usagePayment.setPercent(perc);
				payments.add(usagePayment);
			}
		}

		String message = saveUsageBlocks(conn, blocksWithPayment, logMessage);
		int i;
		for(i = 0; i < blocksWithPayment.size(); i++)
		{
			UsageBlockBase blkBase = usageBlocks.get(i); // These are in the same order as blocksWithPayment.
			UsageBlock blk = blocksWithPayment.get(i);
			blkBase.setID(blk.getID());
		}
		return message;
	}

	public String saveUsageBlocks(Connection conn,  List<UsageBlock> blocksWithPayment, String logMessage)
	{
		if(blocksWithPayment == null || blocksWithPayment.size() == 0)
		{
			return null;
		}

		InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();

		try
		{
			// Blocks are in order
			for (UsageBlock block : blocksWithPayment)
			{
				log.info("Saving usage block: " + block.toString());

				// save to the instrumentUsage table
				InstrumentUsageDAO.getInstance().save(conn, Collections.singletonList(block), logMessage);


				for (InstrumentUsagePayment iup: block.getPayments())
				{
					iup.setInstrumentUsageId(block.getID());
					iupDao.savePayment(conn, iup);
				}
			}
		}
		catch(Exception e)
		{
			log.error("Error saving usage blocks", e);
			return "There was an error saving usage block. Error was: " + e.getMessage();
		}

		return null;
	}

	private String getUpdateMessage(UsageBlockBase block, Date newStartDate, Date newEndDate)
	{
		return MessageFormat.format(dateUpdateMsg,
				TimeUtils.format(block.getStartDate()),
				TimeUtils.format(block.getEndDate()),
				TimeUtils.format(newStartDate),
				TimeUtils.format(newEndDate));
	}

	private String getUpdateMessage(Date startDate, Date endDate, UsageBlockBase newBlock)
	{
		return MessageFormat.format(dateUpdateMsg,
				TimeUtils.format(startDate),
				TimeUtils.format(endDate),
				TimeUtils.format(newBlock.getStartDate()),
				TimeUtils.format(newBlock.getEndDate()));
	}

	private java.sql.Timestamp makeTimestamp(java.util.Date date)
	{
		return new Timestamp(date.getTime());
	}
}
