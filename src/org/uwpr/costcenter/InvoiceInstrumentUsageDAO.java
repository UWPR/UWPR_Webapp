/**
 * InvoiceInstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jul 16, 2011
 */
package org.uwpr.costcenter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class InvoiceInstrumentUsageDAO {

	private static final Logger log = LogManager.getLogger(InvoiceInstrumentUsageDAO.class);
	
	private static InvoiceInstrumentUsageDAO instance = new InvoiceInstrumentUsageDAO();
	
	public static InvoiceInstrumentUsageDAO getInstance() {
		return instance;
	}
	
	public void saveBlocks(Connection conn, List<InvoiceInstrumentUsage> invoiceBlocks) throws SQLException {
		
		String sql = "INSERT INTO invoiceInstrumentUsage (invoiceID, instrumentUsageID) VALUES (?,?)";
		PreparedStatement stmt = null;
		
		try {

			stmt = conn.prepareStatement(sql);
			for(InvoiceInstrumentUsage invoiceBlock: invoiceBlocks) {

				stmt.setInt(1, invoiceBlock.getInvoiceId());
				stmt.setInt(2, invoiceBlock.getInstrumentUsageId());
				stmt.executeUpdate();
			}
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
	
	/**
	 * True if the block has any invoiceInstrumentUsage row.  Any row means the block is invoiced -- a live
	 * invoice, or an orphan the pre-deploy cleanup was meant to remove -- so callers refuse to edit or
	 * delete it either way.
	 */
	public boolean isBlockInvoiced (int instrumentUsageId) throws SQLException {

		String sql = "SELECT 1 FROM invoiceInstrumentUsage WHERE instrumentUsageID = ? LIMIT 1";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, instrumentUsageId);
			rs = stmt.executeQuery();
			return rs.next();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

	/**
	 * Returns every invoiceInstrumentUsage row for the block.  blockExported uses this to skip a block
	 * already on the invoice being built (a re-export) and to refuse one linked to any other invoice,
	 * whether that invoice still exists or is an orphan left by a deleted one.
	 */
	public List<InvoiceInstrumentUsage> getAllInvoiceRowsForUsage (int instrumentUsageId) throws SQLException {

		String sql = "SELECT id, invoiceID, instrumentUsageID FROM invoiceInstrumentUsage WHERE instrumentUsageID = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<InvoiceInstrumentUsage> rows = new ArrayList<>();

		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, instrumentUsageId);
			rs = stmt.executeQuery();
			while(rs.next()) {
				InvoiceInstrumentUsage row = new InvoiceInstrumentUsage();
				row.setId(rs.getInt("id"));
				row.setInvoiceId(rs.getInt("invoiceID"));
				row.setInstrumentUsageId(rs.getInt("instrumentUsageID"));
				rows.add(row);
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		return rows;
	}

	public void deleteBlocksForInvoice (Connection conn, int invoiceId) throws SQLException {

		String sql = "DELETE FROM invoiceInstrumentUsage WHERE invoiceID = ?";
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, invoiceId);
			stmt.executeUpdate();
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

	public void deleteBlocksForUsage (Connection conn, int instrumentUsageId) throws SQLException {

		String sql = "DELETE FROM invoiceInstrumentUsage WHERE instrumentUsageID = ?";
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, instrumentUsageId);
			stmt.executeUpdate();
		}
		finally {
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
}
