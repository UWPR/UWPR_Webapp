/**
 * InstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jun 13, 2011
 */
package org.uwpr.instrumentlog;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
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

        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = "INSERT INTO " +
                "instrumentUsage (projectID, instrumentID, instrumentOperatorId, instrumentRateID, startDate, endDate,enteredBy, dateEntered, updatedBy, notes) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";

        try
        {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, block.getProjectID());
            stmt.setInt(2, block.getInstrumentID());
            stmt.setInt(3, block.getInstrumentOperatorId());
            stmt.setInt(4, block.getInstrumentRateID());
            stmt.setTimestamp(5, new Timestamp(block.getStartDate().getTime()));
            stmt.setTimestamp(6, new Timestamp(block.getEndDate().getTime()));
            stmt.setInt(7, block.getResearcherID());

            block.setDateCreated(new java.util.Date(System.currentTimeMillis()));
            stmt.setTimestamp(8, new Timestamp(block.getDateCreated().getTime()));

            if(block.getUpdaterResearcherID() != 0)
            {
                stmt.setInt(9, block.getUpdaterResearcherID());
            }
            else
            {
                stmt.setNull(9, Types.INTEGER);
            }

            stmt.setString(10, block.getNotes());

            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if(rs.next())
            {
                block.setID(rs.getInt(1));
            }
            else
            {
                throw new SQLException("Error inserting row in instrumentBlock table. No auto-generated ID returned.");
            }

        } finally {

            if(stmt != null) try {stmt.close();} catch(SQLException e){}
            if(rs != null) try {rs.close();} catch(SQLException e){}
        }
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

	public void updateBlocksDates(List<? extends UsageBlockBase> blocks) throws Exception {

		if (blocks == null || blocks.size() == 0)
			return;

		log.info("Updating usage blocks on instrument: " + blocks.get(0).getInstrumentID());


		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		StringBuilder sql = new StringBuilder("Update instrumentUsage SET");
		sql.append(" startDate = ?");
		sql.append(", endDate = ?");
		sql.append(", updatedBy = ?");
		sql.append(" WHERE id = ?");
		try {

			conn = getConnection();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(sql.toString());

			for(UsageBlockBase block: blocks)
			{
				stmt.setTimestamp(1, new Timestamp(block.getStartDate().getTime()));
				stmt.setTimestamp(2, new Timestamp(block.getEndDate().getTime()));
				stmt.setInt(3, block.getUpdaterResearcherID());
				stmt.setInt(4, block.getID());
				stmt.executeUpdate();
			}
			conn.commit();

		} finally {

			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
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
}
