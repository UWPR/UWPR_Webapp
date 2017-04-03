/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.uwpr.costcenter.*;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Researcher;

import java.sql.*;
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
	
	private InstrumentUsageDAO () {}
	
	public static InstrumentUsageDAO getInstance() {
		return instance;
	}

    public void save(Connection conn, UsageBlockBase block) throws Exception
    {
        if (block == null)
            return;

        log.info("Saving usage block on instrument: "+block.getInstrumentID());

        save(conn, Collections.singletonList(block));
    }

	public void save(UsageBlockBase block) throws Exception
    {
        Connection conn = null;
        try
        {
            conn = getConnection();
            save(conn, block);
        }
        finally {

            if(conn != null) try {conn.close();} catch(SQLException e){}
        }
	}

	private void save(Connection conn, List<UsageBlockBase> blocks) throws Exception
	{
		if (blocks == null || blocks.size() == 0)
			return;

		log.info("Saving usage blocks");

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
			}

		} finally {

			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	private void updateBlocksDates(Connection conn, List<? extends UsageBlockBase> blocks) throws Exception {

		if (blocks == null || blocks.size() == 0)
			return;

		log.info("Updating usage blocks on instrument: " + blocks.get(0).getInstrumentID());


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

			for(UsageBlockBase block: blocks)
			{
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

	public void updateBlocksDates(List<? extends UsageBlockBase> blocks) throws Exception {

		Connection conn = null;

		try {

			conn = getConnection();
			conn.setAutoCommit(false);
			updateBlocksDates(conn, blocks);
			conn.commit();

		} finally {

			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}

    public void updateBlocksProjectAndOperator(Connection conn, List<? extends UsageBlockBase> blocks) throws Exception {

        if (blocks == null || blocks.size() == 0)
            return;

        log.info("Updating usage blocks (project and instrument operator) on instrument: " + blocks.get(0).getInstrumentID());

		PreparedStatement stmt = null;
        ResultSet rs = null;

        StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
        sql.append(" projectID = ?");
        sql.append(", instrumentOperatorId = ?");
        sql.append(", updatedBy = ?");
        sql.append(" WHERE id = ?");
        try {

            stmt = conn.prepareStatement(sql.toString());

            for(UsageBlockBase block: blocks)
            {
                stmt.setInt(1, block.getProjectID());
                stmt.setInt(2, block.getInstrumentOperatorId());
                stmt.setInt(3, block.getUpdaterResearcherID());
                stmt.setInt(4, block.getID());
                stmt.executeUpdate();
            }

        } finally {

            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
        }
    }

	public void markDeleted(List<Integer> usageBlockIds, Researcher user) throws Exception {

		if (usageBlockIds == null || usageBlockIds.size() == 0)
			return;

		log.info("Marking blocks as deleted.");

		Connection conn = null;

		try
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			markDeleted(conn, usageBlockIds, user);
			conn.commit();

		} finally {

			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}

	public int markDeleted(Connection conn, List<Integer> usageBlockIds, Researcher researcher) throws SQLException {

		if (usageBlockIds == null || usageBlockIds.size() == 0)
			return 0;

		log.info("Marking blocks as deleted.");

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
	
	public void delete(int usageId) throws SQLException {
		
		
		// NOTE: There is a trigger on instrumentUsage table that will 
		//       delete all entries in the instrumentUsagePayment where instrumentUsageID is equal to 
		//       the given usageId
		
		log.info("Deleting usage block ID "+usageId);
		Connection conn = null;
		Statement stmt = null;
		
		try {
			
			String sql = "DELETE FROM instrumentUsage WHERE id="+usageId;
			// System.out.println(sql);
			conn = getConnection();
			stmt = conn.createStatement();
			stmt.execute(sql);
			
			//InstrumentUsagePaymentDAO.getInstance().deletePaymentsForUsage(usageId);
			
		} finally {

				// Always make sure result sets and statements are closed,
				// and the connection is returned to the pool
				if (stmt != null) {
					try { stmt.close(); } catch (SQLException ignored) { ; }
				}
				if (conn != null) {
					try { conn.close(); } catch (SQLException ignored) { ; }
				}
		}
	}

	public void delete(List<Integer> usageIds) throws SQLException {

		// NOTE: There is a trigger on instrumentUsage table that will
		//       delete all entries in the instrumentUsagePayment where instrumentUsageID is equal to
		//       the given usageId

		Connection conn = null;

		try
		{
			conn = getConnection();
			conn.setAutoCommit(false);
			delete(conn, usageIds);
			conn.commit();

		} finally
		{

			if (conn != null) {
				try { conn.close(); } catch (SQLException ignored) { ; }
			}
		}
	}

	public void delete(Connection conn, List<Integer> usageIds) throws SQLException {

		// NOTE: There is a trigger on instrumentUsage table that will
		//       delete all entries in the instrumentUsagePayment where instrumentUsageID is equal to
		//       the given usageId

		PreparedStatement stmt = null;
		String sql = "DELETE FROM instrumentUsage WHERE id=?";

		try {
			stmt = conn.prepareStatement( sql );

			for(Integer usageId: usageIds)
			{
				stmt.setInt(1, usageId);
				stmt.executeUpdate();
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

		List<Integer> toDelete = new ArrayList<>();
		List<UsageBlockBase> toUpdate = new ArrayList<UsageBlockBase>();
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
				toDelete.add(block.getID());
			}

			else if(sStartDate.before(startDate))
			{
				if(block.getEndDate().after(endDate))
				{
					Date blockEndDate = block.getEndDate();
					// Create a new block
					UsageBlock newBlock = new UsageBlock();
					block.copyTo(newBlock);
					newBlock.setID(0);
					newBlock.setStartDate(endDate);
					newBlock.setEndDate(blockEndDate);
					newBlock.setDeleted(true);
					newBlock.setUpdaterResearcherID(researcher.getID());
					updateRateId(conn, rateType, newBlock);
					newBlocks.add(newBlock);
					newBlock.setPayments(iupDao.getPaymentsForUsage(block.getID()));
				}

				block.setEndDate(startDate);
				block.setUpdaterResearcherID(researcher.getID());
				// Get the new rateId
				updateRateId(conn, rateType, block);
				toUpdate.add(block);
			}

			else if(sEndDate.after(endDate))
			{
				block.setStartDate(endDate);
				// Get the new rateId
				updateRateId(conn, rateType, block);
				toUpdate.add(block);
			}
			else
			{
				// We should not be here!!
				throw new Exception("Cannot handle existing overlapping signup: " + sStartDate + " to " + sEndDate
						+ ". Requested signup was from " + startDate + " to " + endDate);
			}
		}

		delete(conn, toDelete);
		updateBlocksDates(conn, toUpdate);
		for(UsageBlock block: newBlocks)
		{
			save(conn, block);
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

	private java.sql.Timestamp makeTimestamp(java.util.Date date)
	{
		return new Timestamp(date.getTime());
	}
}
