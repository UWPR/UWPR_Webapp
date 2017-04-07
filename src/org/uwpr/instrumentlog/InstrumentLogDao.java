/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.util.Collections;
import java.util.List;

/**
 * 
 */
public class InstrumentLogDao
{
	private static final InstrumentLogDao instance = new InstrumentLogDao();

	private static enum ACTION
	{
		ADDED, EDITED, DELETED, PURGED
	};

	private InstrumentLogDao() {}
	
	public static InstrumentLogDao getInstance() {
		return instance;
	}

	static final Connection getConnection() throws SQLException
	{
		return DBConnectionManager.getMainDbConnection();
	}

	public void logSignupAdded(Connection conn, List<? extends UsageBlockBase> usageBlocks, int researcherId, String message) throws SQLException
	{
		logSignupAction(conn, usageBlocks, researcherId, message, ACTION.ADDED);
	}

	public void logSignupEdited(Connection conn, UsageBlockBase block, int researcherId, String message) throws SQLException
	{
		logSignupAction(conn, Collections.singletonList(block), researcherId, message, ACTION.EDITED);
	}

	public void logSignupDeleted(Connection conn, List<? extends UsageBlockBase> usageBlocks, int researcherId) throws SQLException
	{
		logSignupAction(conn, usageBlocks, researcherId, null, ACTION.DELETED);
	}

	public void logSignupPurged(Connection conn, List<? extends UsageBlockBase> usageBlocks, int researcherId, String message) throws SQLException
	{
		logSignupAction(conn, usageBlocks, researcherId, message, ACTION.PURGED);
	}

	private void logSignupAction(Connection conn, List<? extends UsageBlockBase> usageBlocks, int researcherId, String message, ACTION action) throws SQLException
	{
		String sql = "INSERT INTO instrumentLog (projectID, instrumentID, blockId, userId, action, log) VALUES(?,?,?,?,?,?)";

		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			for(UsageBlockBase block: usageBlocks)
			{
				stmt.setInt(1, block.getProjectID());
				stmt.setInt(2, block.getInstrumentID());
				stmt.setInt(3, block.getID());
				stmt.setInt(4, researcherId);
				stmt.setString(5, action.name());
				if(message == null)
				{
					stmt.setNull(6, Types.VARCHAR);
				}
				else {
					stmt.setString(6, message);
				}
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
