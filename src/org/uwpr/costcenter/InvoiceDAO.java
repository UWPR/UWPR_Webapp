/**
 * InvoiceInstrumentUsageDAO.java
 * @author Vagisha Sharma
 * Jul 16, 2011
 */
package org.uwpr.costcenter;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */
public class InvoiceDAO {

	private static final Logger log = Logger.getLogger(InvoiceDAO.class);
	
	private static InvoiceDAO instance = new InvoiceDAO();
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	public static InvoiceDAO getInstance() {
		return instance;
	}
	
	public void save(Invoice invoice) throws SQLException {
		
		String sql = "INSERT INTO invoice (billStartDate, billEndDate, createdBy) VALUES (?,?,?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setTimestamp(1, new Timestamp(invoice.getBillStartDate().getTime()));
			stmt.setTimestamp(2, new Timestamp(invoice.getBillEndDate().getTime()));
			stmt.setInt(3, invoice.getCreatedBy());
			stmt.executeUpdate();
			
			rs = stmt.getGeneratedKeys(); 

			if ( rs != null && rs.next() ) {
				invoice.setId( rs.getInt(1) );
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	public Invoice getInvoice (Date startDate, Date endDate) throws SQLException {
		
		String sql = "SELECT * FROM invoice WHERE billStartDate='"+dateFormat.format(startDate)+"'"
		+ "AND billEndDate='"+dateFormat.format(endDate)+"'";
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				Invoice invoice = new Invoice();
				invoice.setId(rs.getInt("id"));
				invoice.setCreateDate(rs.getTimestamp("createDate"));
				invoice.setBillStartDate(rs.getTimestamp("billStartDate"));
				invoice.setBillEndDate(rs.getTimestamp("billEndDate"));
				invoice.setCreatedBy(rs.getInt("createdBy"));
				return invoice;
			}
			else {
				log.error("No entry found in table invoice for startDate: "+startDate.toString()+" and endDate: "+endDate.toString());
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return null;
	}
	
	public void delete(Invoice invoice) throws SQLException {
		
		String sql = "DELETE FROM invoice WHERE id="+invoice.getId();
		Connection conn = null;
		Statement stmt = null;
		
		try {
			conn = DBConnectionManager.getMainDbConnection();
			stmt = conn.createStatement();
			stmt.execute(sql);
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
}
