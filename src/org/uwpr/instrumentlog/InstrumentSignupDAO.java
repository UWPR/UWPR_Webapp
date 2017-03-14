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

import java.sql.*;
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

	private InstrumentSignupDAO() {}
	
	public static InstrumentSignupDAO getInstance() {
		return instance;
	}

	static final Connection getConnection() throws SQLException
	{
		return DBConnectionManager.getMainDbConnection();
	}

    public void save(Connection conn, InstrumentSignup signup, int researcherId) throws Exception
    {
        if (signup == null)
            return;

        log.info(String.format("Saving signup block for project: %d, instrument: %d", signup.getProjectID(), signup.getInstrumentID()));

        PreparedStatement signupStmt = null;
		PreparedStatement blockStmt = null;
		PreparedStatement paymentStmt = null;
		ResultSet rs = null;

		String signupSql = "INSERT INTO instrumentSignup (projectId, instrumentId, startDate, endDate, createdBy) VALUES (?,?,?,?,?)";
		String blockSql = "INSERT INTO instrumentSignupBlock (instrumentSignupId, instrumentRateId, startDate, endDate) VALUES (?,?,?,?)";
		String paymentSql = "INSERT INTO instrumentSignupPayment (instrumentSignupId, paymentMethodId, percentPayment) VALUES (?,?,?)";

        try {

			signupStmt = conn.prepareStatement(signupSql, Statement.RETURN_GENERATED_KEYS);
			signupStmt.setInt(1, signup.getProjectID());
			signupStmt.setInt(2, signup.getInstrumentID());
			signupStmt.setTimestamp(3, new java.sql.Timestamp(signup.getStartDate().getTime()));
			signupStmt.setTimestamp(4, new java.sql.Timestamp(signup.getEndDate().getTime()));
			signupStmt.setInt(5, researcherId);
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

			blockStmt = conn.prepareStatement(blockSql);
			for(SignupBlock blk: signup.getBlocks())
			{
				blk.setInstrumentSignupId(signup.getId());
				blockStmt.setInt(1, blk.getInstrumentSignupId());
				blockStmt.setInt(2, blk.getInstrumentRateId());
				blockStmt.setTimestamp(3, new java.sql.Timestamp(blk.getStartDate().getTime()));
				blockStmt.setTimestamp(4, new java.sql.Timestamp(blk.getEndDate().getTime()));
				blockStmt.executeUpdate();
			}

			paymentStmt = conn.prepareStatement(paymentSql);
			for(SignupPayment payment: signup.getPayments())
			{
				payment.setInstrumentSignupId(signup.getId());
				paymentStmt.setInt(1, payment.getInstrumentSignupId());
				paymentStmt.setInt(2, payment.getPaymentMethod().getId());
				paymentStmt.setBigDecimal(3, payment.getPercent());
				paymentStmt.executeUpdate();
			}

        } finally {

            if(signupStmt != null) try {signupStmt.close();} catch(SQLException ognored){}
			if(blockStmt != null) try {blockStmt.close();} catch(SQLException ognored){}
			if(paymentStmt != null) try {paymentStmt.close();} catch(SQLException ognored){}
			if(rs != null) try {rs.close();} catch(SQLException ignored){}
        }
    }

	public void deleteSignup(Connection conn, List<InstrumentSignupGeneric> signupList) throws Exception {

		if (signupList == null || signupList.size() == 0)
			return;
		PreparedStatement stmt = null;

		String deleteSql = "DELETE FROM instrumentSignup WHERE id=?";

		try
		{
			for(InstrumentSignupGeneric signup: signupList)
			{
				// NOTE: There is a trigger on instrumentSignup that will delete all rows in the
				//       instrumentSignupBlock and  instrumentSignupPayment tables where instrumentSignupId is equal to
				//       the given signup id
				log.info("Deleting signup: " + signup.toString());
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

    public List<InstrumentSignupGeneric> getExistingSignup(InstrumentSignupGeneric signup) throws SQLException
	{
		return getExistingSignup(signup.getStartDate(), signup.getEndDate());
	}

	public List<InstrumentSignupGeneric> getExistingSignup(java.util.Date startDate, java.util.Date endDate) throws SQLException
	{
		String sql = "SELECT * FROM instrumentSignup WHERE startDate < ? AND endDate > ? ORDER BY startDate ASC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<InstrumentSignupGeneric> existingSignup = new ArrayList<InstrumentSignupGeneric>();

		try
		{
			conn = getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setTimestamp(1, new java.sql.Timestamp(endDate.getTime()));
			stmt.setTimestamp(2, new java.sql.Timestamp(startDate.getTime()));
			rs = stmt.executeQuery();

			while(rs.next())
			{
				InstrumentSignupGeneric su = new InstrumentSignupGeneric();
				su.setId(rs.getInt("Id"));
				su.setInstrumentID(rs.getInt("instrumentId"));
				su.setProjectId(rs.getInt("projectId"));
				su.setStartDate(rs.getTimestamp("startDate"));
				su.setEndDate(rs.getTimestamp("endDate"));
				su.setDateCreated(rs.getTimestamp("created"));
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

	private List<InstrumentSignupWithRate> getSignup(int projectId, int instrumentId, Date startDate, Date endDate, boolean complete) throws SQLException {

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
				stmt.setTimestamp(1, new java.sql.Timestamp(startDate.getTime()));
			}
			if(endDate != null)
			{
				stmt.setTimestamp(2, new java.sql.Timestamp(endDate.getTime()));
			}

			rs = stmt.executeQuery();

			while(rs.next())
			{
				InstrumentSignupWithRate su = new InstrumentSignupWithRate();
				su.setId(rs.getInt("Id"));
				su.setInstrumentID(rs.getInt("instrumentId"));
				su.setProjectId(rs.getInt("projectId"));
				su.setStartDate(rs.getTimestamp("startDate"));
				su.setEndDate(rs.getTimestamp("endDate"));
				su.setDateCreated(rs.getTimestamp("created"));
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
		java.sql.Date startDate = null;
		if(filter.getStartDate() != null)
		{
			startDate = new java.sql.Date(filter.getStartDate().getTime());
		}

		java.sql.Date endDate = null;
		if(filter.getEndDate() != null)
		{
			endDate = new java.sql.Date(filter.getEndDate().getTime());
		}
		return getSignup(filter.getProjectId(), filter.getInstrumentId(), startDate, endDate, true);
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
				block.setStartDate(rs.getTimestamp("startDate"));
				block.setEndDate(rs.getTimestamp("endDate"));
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
				stmt.setTimestamp(3, new java.sql.Timestamp(block.getStartDate().getTime()));
				stmt.setTimestamp(4, new java.sql.Timestamp(block.getEndDate().getTime()));
				stmt.setInt(5, block.getResearcherID());
			}
			stmt.executeUpdate();
		}
		finally
		{
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
}
