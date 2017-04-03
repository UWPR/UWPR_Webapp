/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 
 */
public class InstrumentSignupDAO
{
	private static final InstrumentSignupDAO instance = new InstrumentSignupDAO();

	private static final Logger log = Logger.getLogger(InstrumentSignupDAO.class);

	private static final String BLOCK_SQL = "INSERT INTO instrumentSignupBlock (projectID, instrumentID, instrumentRateId, startDate, endDate, createdBy) VALUES (?,?,?,?,?,?)";
	private static final String PAYMENT_SQL = "INSERT INTO instrumentSignupPayment (instrumentSignupBlockId, paymentMethodId, percentPayment) VALUES (?,?,?)";

	private InstrumentSignupDAO() {}
	
	public static InstrumentSignupDAO getInstance() {
		return instance;
	}

	static final Connection getConnection() throws SQLException
	{
		return DBConnectionManager.getMainDbConnection();
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

	private java.sql.Timestamp makeTimestamp(java.util.Date date)
	{
		return new Timestamp(date.getTime());
	}
}
