/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.uwpr.costcenter.*;
import org.uwpr.scheduler.UsageBlockPaymentInformation;
import org.uwpr.www.util.TimeUtils;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.payment.PaymentMethod;

import java.math.BigDecimal;
import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 
 */
public class InstrumentUsageDAO {

	private static final InstrumentUsageDAO instance = new InstrumentUsageDAO();
	
	private static final Logger log = Logger.getLogger(InstrumentUsageDAO.class);

	private static final String dateUpdateMsg = "Date changed from {0} - {1} to  {2} - {3}";
	private static final String adjustingSignup = "Adjusting Signup";
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
				"instrumentUsage (projectID, instrumentID, instrumentOperatorId, instrumentRateID, startDate, endDate,enteredBy, dateEntered, updatedBy, notes, deleted) " +
				"VALUES(?,?,?,?,?,?,?,?,?,?,?)";

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

	public void updateBlocksDates(List<? extends UsageBlockBase> blocks, List<String> logMessages) throws Exception {

		Connection conn = null;

		try {

			conn = getConnection();
			conn.setAutoCommit(false);
			updateBlocksDates(conn, blocks, logMessages);
			conn.commit();

		} finally {

			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}

	private void updateBlocksDates(Connection conn, List<? extends UsageBlockBase> blocks, List<String> logMessages) throws Exception {

		if (blocks == null || blocks.size() == 0)
			return;

		log.info("Updating usage blocks on instrument: " + blocks.get(0).getInstrumentID());

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		boolean queryBlock = false;
		if(logMessages == null)
		{
			queryBlock = true;
		}
		PreparedStatement stmt = null;
		PreparedStatement selectStmt = null;

		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
		sql.append(" startDate = ?");
		sql.append(", endDate = ?");
		sql.append(", instrumentRateID = ?");
		sql.append(", updatedBy = ?");
		sql.append(" WHERE id = ?");

		String selectSql = "SELECT * FROM instrumentUsage WHERE id=?";
		try {

			stmt = conn.prepareStatement(sql.toString());
			if(queryBlock)
			{
				selectStmt = conn.prepareStatement(selectSql);
			}

			int i = 0;
			for(UsageBlockBase block: blocks)
			{
				String message = null;
				if(queryBlock) {
					// Get the existing database block
					selectStmt.setInt(1, block.getID());
					rs = selectStmt.executeQuery();
					if (!rs.next()) {
						throw new SQLException("Could not find existing block with id " + block.getID());
					}
					Date originalStartDate = rs.getTimestamp("startDate");
					Date originalEndDate = rs.getTimestamp("endDate");

					message = getUpdateMessage(originalStartDate, originalEndDate, block);
				}
				else
				{
					message = logMessages.get(i++);
				}
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
			if(selectStmt != null) try {selectStmt.close();} catch(SQLException e){}
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
                stmt.setInt(3, block.getUpdaterResearcherID());
                stmt.setInt(4, block.getID());
                stmt.executeUpdate();

				logDao.logSignupEdited(conn, block, block.getUpdaterResearcherID(),
						"Changed project from " + block.getProjectID() + " to " + newProjectId);
            }

        } finally {

            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
        }
    }

	public void markDeleted(List<UsageBlockBase> usageBlocks, Researcher user) throws Exception {

		if (usageBlocks == null || usageBlocks.size() == 0)
			return;

		log.info("Marking blocks as deleted.");

		Connection conn = null;
		try
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			markDeleted(conn, usageBlocks, user);
			conn.commit();

		} finally {

			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}

	public int markDeleted(Connection conn, List<UsageBlockBase> usageBlocks, Researcher researcher) throws SQLException
	{
		return markDeleted(conn, usageBlocks, researcher, null);
	}

	public int markDeletedByEditAction(Connection conn, List<UsageBlockBase> usageBlocks, Researcher researcher) throws SQLException
	{
		return markDeleted(conn, usageBlocks, researcher, deletedByEditAction);
	}

	private int markDeleted(Connection conn, List<UsageBlockBase> usageBlocks, Researcher researcher, String message) throws SQLException {

		if (usageBlocks == null || usageBlocks.size() == 0)
			return 0;

		InstrumentLogDao logDao = InstrumentLogDao.getInstance();

		log.info("Marking blocks as deleted.");

		List<Integer> usageBlockIds = new ArrayList<>(usageBlocks.size());
		for(UsageBlockBase block: usageBlocks)
		{
			usageBlockIds.add(block.getID());
		}
		PreparedStatement stmt = null;
		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
		sql.append(" deleted = ?, ");
		sql.append(" updatedBy = ? ");
		sql.append(" WHERE id in (").append(StringUtils.join(usageBlockIds, ",")).append(") ");
		try
		{
			stmt = conn.prepareStatement(sql.toString());
			stmt.setBoolean(1, Boolean.TRUE);
			stmt.setInt(2, researcher.getID());

			int numUpdated = stmt.executeUpdate();

			logDao.logSignupDeleted(conn, usageBlocks, researcher.getID());

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

				logDao.logSignupPurged(conn, block, researcher.getID(), message + ": " + block.toString());
			}

		} finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException ignored) { ; }
			}
		}
	}

	public void deleteOrAdjustSignupBlocks(Connection conn, Researcher researcher, int projectId, int instrumentId, RateType rateType, Date startDate, Date endDate) throws Exception
	{
		List<UsageBlockBase> signupBlocks = UsageBlockBaseDAO.getSignupBlocks(conn, projectId, instrumentId, startDate, endDate);

		if(signupBlocks.size() == 0)
			return;

		List<UsageBlockBase> toDelete = new ArrayList<>();
		List<UsageBlockBase> toUpdate = new ArrayList<>();
		List<String> updateMsgs = new ArrayList<>();
		List<UsageBlock> newBlocks = new ArrayList<>();

		InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();

		for(UsageBlockBase block: signupBlocks)
		{
			Date sStartDate = block.getStartDate();
			Date sEndDate = block.getEndDate();

			if((sStartDate.equals(startDate) || sStartDate.after(startDate)) &&
					(sEndDate.equals(endDate) || sEndDate.before(endDate)))
			{
				// delete this signup (it is contained in the new signup request)
				toDelete.add(block);
			}

			else if(sStartDate.before(startDate))
			{
				if(sEndDate.after(endDate))
				{
					// Create a new block
					UsageBlock newBlock = new UsageBlock();
					block.copyTo(newBlock);
					newBlock.setID(0);
					newBlock.setStartDate(endDate);
					newBlock.setEndDate(sEndDate);
					newBlock.setDeleted(true);
					newBlock.setUpdaterResearcherID(researcher.getID());
					updateRateId(conn, rateType, newBlock);
					newBlocks.add(newBlock);
					newBlock.setPayments(iupDao.getPaymentsForUsage(block.getID()));
				}
				if(!sEndDate.equals(startDate))
				{
					// Update the block end date only if it is overlapping
					String updateMsg = getUpdateMessage(block, block.getStartDate(), startDate);
					block.setEndDate(startDate);
					block.setUpdaterResearcherID(researcher.getID());
					// Get the new rateId
					updateRateId(conn, rateType, block);
					toUpdate.add(block);
					updateMsgs.add(adjustingSignup + ": " + updateMsg);
				}
			}

			else if(sEndDate.after(endDate))
			{
				if(!sStartDate.equals(endDate))
				{
					// Update the block end date only if it is overlapping
					String updateMsg = getUpdateMessage(block, endDate, block.getEndDate());
					block.setStartDate(endDate);
					// Get the new rateId
					updateRateId(conn, rateType, block);
					toUpdate.add(block);
					updateMsgs.add(adjustingSignup + ": " + updateMsg);
				}
			}
			else
			{
				// We should not be here!!
				throw new Exception("Cannot handle existing overlapping signup: " + sStartDate + " to " + sEndDate
						+ ". Requested signup was from " + startDate + " to " + endDate);
			}
		}

		delete(conn, toDelete, researcher, adjustingSignup);
		updateBlocksDates(conn, toUpdate, updateMsgs);
		save(conn, newBlocks, adjustingSignup);
		for(UsageBlock block: newBlocks)
		{
			for(InstrumentUsagePayment payment: block.getPayments())
			{
				payment.setInstrumentUsageId(block.getID());
				iupDao.savePayment(conn, payment);
			}

		}
	}

	private void updateRateId(Connection conn, RateType rateType, UsageBlockBase block) throws Exception
	{
		TimeBlock timeBlock = TimeBlockDAO.getInstance().getTimeBlock(conn, block.getHours());
		if(timeBlock == null)
        {
            throw new Exception("Could not find a time block for numHours: " + block.getHours() + ". Attempting to adjust signup block: " + block.toString());
        }
		InstrumentRate rate = InstrumentRateDAO.getInstance().getInstrumentCurrentRate(block.getInstrumentID(), timeBlock.getId(), rateType.getId());
		if(rate == null)
		{
			throw new Exception("Could not find a rate for timeBlock: " + timeBlock.toString() + ". Attempting to adjust signup block: " + block.toString());
		}
		block.setInstrumentRateID(rate.getId());
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

		InstrumentUsagePaymentDAO iupDao = InstrumentUsagePaymentDAO.getInstance();

		if(usageBlocks == null || usageBlocks.size() == 0)
		{
			return null;
		}

		try
		{
			// Blocks are in order
			for (UsageBlockBase block : usageBlocks)
			{
				log.info("Saving usage block: " + block.toString());

				// save to the instrumentUsage table
				InstrumentUsageDAO.getInstance().save(conn, Collections.singletonList(block), logMessage);

				for (int i = 0; i < paymentInfo.getCount(); i++)
				{

					PaymentMethod pm = paymentInfo.getPaymentMethod(i);
					BigDecimal perc = paymentInfo.getPercent(i);

					InstrumentUsagePayment usagePayment = new InstrumentUsagePayment();
					usagePayment.setInstrumentUsageId(block.getID());
					usagePayment.setPaymentMethod(pm);
					usagePayment.setPercent(perc);
					iupDao.savePayment(conn, usagePayment);
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
