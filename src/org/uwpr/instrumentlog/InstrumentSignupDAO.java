/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.log4j.Logger;
import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.costcenter.InstrumentRateDAO;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.PaymentMethodDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class InstrumentSignupDAO
{
	private static final InstrumentSignupDAO instance = new InstrumentSignupDAO();

	private static final Logger log = Logger.getLogger(InstrumentSignupDAO.class);

	private static final String BLOCK_SQL = "INSERT INTO instrumentSignupBlock (instrumentSignupId, instrumentRateId, startDate, endDate) VALUES (?,?,?,?)";
	private static final String PAYMENT_SQL = "INSERT INTO instrumentSignupPayment (instrumentSignupId, paymentMethodId, percentPayment) VALUES (?,?,?)";

	private InstrumentSignupDAO() {}
	
	public static InstrumentSignupDAO getInstance() {
		return instance;
	}

	static final Connection getConnection() throws SQLException
	{
		return DBConnectionManager.getMainDbConnection();
	}

    public void save(Connection conn, InstrumentSignup signup) throws Exception
    {
        if (signup == null)
            return;

        log.info(String.format("Saving signup block for project: %d, instrument: %d", signup.getProjectID(), signup.getInstrumentID()));

        PreparedStatement signupStmt = null;
		PreparedStatement blockStmt = null;
		PreparedStatement paymentStmt = null;
		ResultSet rs = null;

		String signupSql = "INSERT INTO instrumentSignup (projectId, instrumentId, startDate, endDate, createdBy) VALUES (?,?,?,?,?)";

        try {

			signupStmt = conn.prepareStatement(signupSql, Statement.RETURN_GENERATED_KEYS);
			signupStmt.setInt(1, signup.getProjectID());
			signupStmt.setInt(2, signup.getInstrumentID());
			signupStmt.setTimestamp(3, makeTimestamp(signup.getStartDate()));
			signupStmt.setTimestamp(4, makeTimestamp(signup.getEndDate()));
			signupStmt.setInt(5, signup.getCreatedBy());
			int insertCount = signupStmt.executeUpdate();
			if(insertCount == 0)
			{
				throw new SQLException("Failed to insert row in instrumentSignup table.");
			}

			rs = signupStmt.getGeneratedKeys();
			if(rs.next())
			{
				signup.setId(rs.getInt(1));
			}
			else
			{
				throw new SQLException("Error inserting row in instrumentSignup table. No auto-generated ID returned.");
			}

			blockStmt = insertBlocks(conn, signup);

			paymentStmt = insertPayments(conn, signup);

        } finally {

            if(signupStmt != null) try {signupStmt.close();} catch(SQLException ognored){}
			if(blockStmt != null) try {blockStmt.close();} catch(SQLException ognored){}
			if(paymentStmt != null) try {paymentStmt.close();} catch(SQLException ognored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }

	private PreparedStatement insertPayments(Connection conn, InstrumentSignup signup) throws SQLException
	{
		PreparedStatement paymentStmt;
		paymentStmt = conn.prepareStatement(PAYMENT_SQL);
		for(SignupPayment payment: signup.getPayments())
        {
            payment.setInstrumentSignupId(signup.getId());
            paymentStmt.setInt(1, payment.getInstrumentSignupId());
            paymentStmt.setInt(2, payment.getPaymentMethod().getId());
            paymentStmt.setBigDecimal(3, payment.getPercent());
            paymentStmt.executeUpdate();
        }
		return paymentStmt;
	}

	private PreparedStatement insertBlocks(Connection conn, InstrumentSignup signup) throws SQLException
	{
		PreparedStatement blockStmt;
		blockStmt = conn.prepareStatement(BLOCK_SQL);
		for(SignupBlock blk: signup.getBlocks())
        {
            blk.setInstrumentSignupId(signup.getId());
            blockStmt.setInt(1, blk.getInstrumentSignupId());
            blockStmt.setInt(2, blk.getInstrumentRateId());
            blockStmt.setTimestamp(3, makeTimestamp(blk.getStartDate()));
            blockStmt.setTimestamp(4, makeTimestamp(blk.getEndDate()));
            blockStmt.executeUpdate();
        }
		return blockStmt;
	}

	public void updateSignup(Connection conn, InstrumentSignup signup, boolean updateBlocks, boolean updatePayments) throws Exception
	{
		if (signup == null)
			return;

		log.info(String.format("Updating signup block for project: %d, instrument: %d", signup.getProjectID(), signup.getInstrumentID()));


		if(updateBlocks)
		{
			// Delete all blocks
			deleteSignupBlocks(conn, signup);
		}
		if(updatePayments)
		{
			deletePayments(conn, signup);
		}

		PreparedStatement signupStmt = null;
		PreparedStatement blockStmt = null;
		PreparedStatement paymentStmt = null;

		String signupSql = "UPDATE instrumentSignup SET projectId = ?, instrumentId = ?, startDate = ?, endDate = ? WHERE id=?";

		try {

			signupStmt = conn.prepareStatement(signupSql);
			signupStmt.setInt(1, signup.getProjectID());
			signupStmt.setInt(2, signup.getInstrumentID());
			signupStmt.setTimestamp(3, makeTimestamp(signup.getStartDate()));
			signupStmt.setTimestamp(4, makeTimestamp(signup.getEndDate()));
			signupStmt.setInt(5, signup.getId());
			int updateCount = signupStmt.executeUpdate();
			if(updateCount == 0)
			{
				throw new SQLException("Failed to update row in instrumentSignup table for id " + signup.getBlocks());
			}

			if(updateBlocks)
			{
				blockStmt = insertBlocks(conn, signup);
			}

			if(updatePayments)
			{
				paymentStmt = insertPayments(conn, signup);
			}

		} finally {

			if(signupStmt != null) try {signupStmt.close();} catch(SQLException ognored){}
			if(blockStmt != null) try {blockStmt.close();} catch(SQLException ognored){}
			if(paymentStmt != null) try {paymentStmt.close();} catch(SQLException ognored){}
		}
	}

	public <T extends InstrumentSignupGeneric> void deleteSignup(Connection conn, List<T> signupList) throws Exception {

		if (signupList == null || signupList.size() == 0)
			return;
		PreparedStatement stmt = null;

		String deleteSql = "DELETE FROM instrumentSignup WHERE id=?";

		try
		{
			for(InstrumentSignupGeneric signup: signupList)
			{
				checkIsSignupInvoiced(conn, signup);

				log.info("Deleting signup: " + signup.toString());

				// NOTE: There is a trigger on instrumentSignup that will delete all rows in the
				//       instrumentSignupBlock and  instrumentSignupPayment tables where instrumentSignupId is equal to
				//       the given signup id
				stmt = conn.prepareStatement(deleteSql);
				stmt.setInt(1, signup.getId());
				stmt.executeUpdate();
			}
		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

	private void deleteSignupBlocks(Connection conn, InstrumentSignupGeneric signup) throws Exception
	{
		PreparedStatement stmt = null;

		String deleteSql = "DELETE FROM instrumentSignupBlock WHERE instrumentSignupId=?";

		log.info("Deleting signup blocks for signupId: " + signup.toString());

		try
		{
			checkIsSignupInvoiced(conn, signup);

			stmt = conn.prepareStatement(deleteSql);
			stmt.setInt(1, signup.getId());
			stmt.executeUpdate();

		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

	private void checkIsSignupInvoiced(Connection conn, InstrumentSignupGeneric signup) throws Exception
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String getInvoicedBlocksSql = "SELECT count(*) FROM invoiceSignupBlock iv INNER JOIN instrumentSignupBlock isb ON (isb.id = iv.instrumentSignupBlockId) "
				+ " INNER JOIN instrumentSignup i ON (isb.instrumentSignupId = i.id) WHERE i.id=?";

		log.info("Looking for invoiced signup blocks for signup: " + signup.getId());

		try {
			// If any of the existing blocks have already been invoiced throw an error
			stmt = conn.prepareStatement(getInvoicedBlocksSql);
			stmt.setInt(1, signup.getId());
			rs = stmt.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				if (count > 0) {
					throw new Exception(count + " signup blocks have already been invoiced for signup " + signup.toString());
				}
			}
		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	private void deletePayments(Connection conn, InstrumentSignupGeneric signup) throws Exception
	{
		PreparedStatement stmt = null;

		String deleteSql = "DELETE FROM instrumentSignupPayment WHERE instrumentSignupId=?";

		log.info("Deleting payment methods for signupId: " + signup.toString());

		try
		{
			stmt = conn.prepareStatement(deleteSql);
			stmt.setInt(1, signup.getId());
			stmt.executeUpdate();

		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

	public List<InstrumentSignup> getExistingSignup(java.util.Date startDate, java.util.Date endDate) throws SQLException
	{
		String sql = "SELECT * FROM instrumentSignup WHERE startDate < ? AND endDate > ? ORDER BY startDate ASC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<InstrumentSignup> existingSignup = new ArrayList<InstrumentSignup>();

		try
		{
			conn = getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setTimestamp(1, new java.sql.Timestamp(endDate.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(startDate.getTime()));
			rs = stmt.executeQuery();

			while(rs.next())
			{
				InstrumentSignup su = new InstrumentSignup();
				su.setId(rs.getInt("Id"));
				su.setInstrumentID(rs.getInt("instrumentId"));
				su.setProjectId(rs.getInt("projectId"));
				su.setStartDate(makeDate(rs.getTimestamp("startDate")));
				su.setEndDate(makeDate(rs.getTimestamp("endDate")));
				su.setDateCreated(makeDate(rs.getTimestamp("created")));
				su.setCreatedBy(rs.getInt("createdBy"));
				existingSignup.add(su);
			}
		}
		finally
		{
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}

		return existingSignup;
	}

	private List<InstrumentSignupWithRate> getSignup(int projectId, int instrumentId, java.util.Date startDate, java.util.Date endDate, boolean complete) throws SQLException {

		List<InstrumentSignupWithRate> signupList = new ArrayList<InstrumentSignupWithRate>();

		if(projectId <= 0)
		{
			log.error("Invalid projectId " + projectId + " in request.");
			return signupList;
		}

		StringBuilder sql = new StringBuilder("SELECT * FROM instrumentSignup WHERE projectId = ?");
		if(instrumentId > 0)
		{
			sql.append(" AND instrumentId = ?");
		}
		if(startDate != null)
		{
			sql.append(" AND startDate >= ?");
		}
		if(endDate != null)
		{
			sql.append(" AND endDate <= ?");
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = getConnection();
			stmt = conn.prepareStatement(sql.toString());
			stmt.setInt(1, projectId);
			if(instrumentId > 0)
			{
				stmt.setInt(2, instrumentId);
			}
			if(startDate != null)
			{
				stmt.setTimestamp(1, makeTimestamp(startDate));
			}
			if(endDate != null)
			{
				stmt.setTimestamp(2, makeTimestamp(endDate));
			}

			rs = stmt.executeQuery();

			while(rs.next())
			{
				InstrumentSignupWithRate su = new InstrumentSignupWithRate();
				su.setId(rs.getInt("Id"));
				su.setInstrumentID(rs.getInt("instrumentId"));
				su.setProjectId(rs.getInt("projectId"));
				su.setStartDate(makeDate(rs.getTimestamp("startDate")));
				su.setEndDate(makeDate(rs.getTimestamp("endDate")));
				su.setDateCreated(makeDate(rs.getTimestamp("created")));
				su.setCreatedBy(rs.getInt("createdBy"));
				signupList.add(su);
			}

			if(complete)
			{
				for(InstrumentSignupWithRate signup: signupList)
				{
					signup.setBlocks(getSignupBlocks(conn, signup.getId(), new SignupBlockFactory.SignupBlockWithRateCreator()));
					signup.setPayments(getPaymentMethods(conn, signup.getId()));
				}
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		return signupList;
	}

	public List<InstrumentSignupWithRate> getSignup(SignupFilter filter) throws SQLException
	{
		return getSignup(filter.getProjectId(), filter.getInstrumentId(), filter.getStartDate(), filter.getEndDate(), true);
	}

	private <T extends SignupBlock> List<T> getSignupBlocks(Connection conn, int instrumentSignupId, SignupBlockFactory<T> blockCreator) throws SQLException {

		List<T> blocks = new ArrayList<T>();

		String sql = "SELECT * FROM instrumentSignupBlock WHERE instrumentSignupId = " + instrumentSignupId;

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				T block = blockCreator.create();
				block.setId(rs.getInt("Id"));
				block.setInstrumentSignupId(rs.getInt("instrumentSignupId"));
				block.setInstrumentRateId(rs.getInt("instrumentRateId"));
				block.setStartDate(makeDate(rs.getTimestamp("startDate")));
				block.setEndDate(makeDate(rs.getTimestamp("endDate")));
				blocks.add(block);

				// block.setPayments(getPaymentMethods(conn, block.getId()));
			}
		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		return blocks;
	}

	private List<SignupBlockWithRate> getSignupBlocksWithRate(Connection conn, int instrumentSignupId) throws SQLException {

		List<SignupBlockWithRate> blocks = getSignupBlocks(conn, instrumentSignupId, new SignupBlockFactory.SignupBlockWithRateCreator());

		Map<Integer, InstrumentRate> rates = new HashMap<Integer, InstrumentRate>();
		InstrumentRateDAO rateDao = InstrumentRateDAO.getInstance();
		for(SignupBlockWithRate block: blocks)
		{
			int instrumentRateId = block.getInstrumentRateId();
			InstrumentRate rate = rates.get(instrumentRateId);
			if(rate == null)
			{
				rate = rateDao.getInstrumentRate(instrumentRateId);
				rates.put(instrumentRateId, rate);
			}
			block.setRate(rate);
		}

		return blocks;
	}

	private List<SignupPayment> getPaymentMethods(Connection conn, int instrumentSignupId) throws SQLException {

		List<SignupPayment> payments = new ArrayList<SignupPayment>();

		String sql = "SELECT * FROM instrumentSignupPayment WHERE instrumentSignupId = " + instrumentSignupId;

		Statement stmt = null;
		ResultSet rs = null;

		PaymentMethodDAO pmDao = PaymentMethodDAO.getInstance();

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);

			while(rs.next())
			{
				SignupPayment payment = new SignupPayment();
				payment.setInstrumentSignupId(rs.getInt("instrumentSignupId"));
				payment.setPercent(rs.getBigDecimal("percentPayment"));

				int pmId = rs.getInt("paymentMethodID");
				PaymentMethod pm = pmDao.getPaymentMethod(pmId);
				if(pm == null) {
					throw new SQLException("No payment method found for ID: "+pmId);
				}
				payment.setPaymentMethod(pm);

				payments.add(payment);
			}
		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}

		return payments;
	}

	public void logInstrumentSignUp(Connection conn, List<? extends UsageBlockBase> usageBlocks) throws SQLException
	{
		String sql = "INSERT INTO instrumentSignupLog (projectId, instrumentId, startDate, endDate, createdBy) VALUES(?,?,?,?,?)";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			for(UsageBlockBase block: usageBlocks)
			{
				stmt.setInt(1, block.getProjectID());
				stmt.setInt(2, block.getInstrumentID());
				stmt.setTimestamp(3, makeTimestamp(block.getStartDate()));
				stmt.setTimestamp(4, makeTimestamp(block.getEndDate()));
				stmt.setInt(5, block.getResearcherID());
			}
			stmt.executeUpdate();
		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

	private java.util.Date makeDate(Timestamp timestamp)
	{
		return new java.util.Date(timestamp.getTime());
	}

	private java.sql.Timestamp makeTimestamp(java.util.Date date)
	{
		return new Timestamp(date.getTime());
	}
}
