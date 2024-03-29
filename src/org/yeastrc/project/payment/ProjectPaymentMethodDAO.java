/**
 * ProjectPaymentMethodDAO.java
 * @author Vagisha Sharma
 * May 20, 2011
 */
package org.yeastrc.project.payment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 */
public class ProjectPaymentMethodDAO {

	private ProjectPaymentMethodDAO() {}
	
	private static ProjectPaymentMethodDAO instance = new ProjectPaymentMethodDAO();
	
	private static final Logger log = LogManager.getLogger(ProjectPaymentMethodDAO.class);
	
	public static ProjectPaymentMethodDAO getInstance() {
		return instance;
	}
	
	public List<PaymentMethod> getPaymentMethods(int projectId) throws SQLException {
		
		List<Integer> paymentMethodIds = new ArrayList<Integer>();
		String sql = "SELECT paymentMethodID FROM projectPaymentMethod WHERE projectID="+projectId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				paymentMethodIds.add(rs.getInt("paymentMethodID"));
			}
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		List<PaymentMethod> paymentMethodList = new ArrayList<PaymentMethod>(paymentMethodIds.size());
		
		PaymentMethodDAO pmdao = PaymentMethodDAO.getInstance();
		for(Integer paymentMethodId: paymentMethodIds) {
			
			PaymentMethod pm = pmdao.getPaymentMethod(paymentMethodId);
			if(pm != null) {
				paymentMethodList.add(pm);
			}
		}
		
		return paymentMethodList;
	}
	
	public List<PaymentMethod> getCurrentPaymentMethods(int projectId) throws SQLException {
		
		List<Integer> paymentMethodIds = new ArrayList<Integer>();
		String sql = "SELECT paymentMethodID FROM projectPaymentMethod WHERE projectID="+projectId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next()) {
				paymentMethodIds.add(rs.getInt("paymentMethodID"));
			}
			
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		List<PaymentMethod> paymentMethodList = new ArrayList<PaymentMethod>(paymentMethodIds.size());
		
		PaymentMethodDAO pmdao = PaymentMethodDAO.getInstance();
		for(Integer paymentMethodId: paymentMethodIds) {
			
			PaymentMethod pm = pmdao.getPaymentMethod(paymentMethodId);
			if(pm != null && pm.isCurrent()) {
				paymentMethodList.add(pm);
			}
		}
		
		return paymentMethodList;
	}
	
	public void savePaymentMethod(int projectId, PaymentMethod paymentMethod) throws SQLException {

		Connection conn = null;

		String sql = "INSERT INTO projectPaymentMethod (projectID, paymentMethodID) VALUES (?,?)";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			conn.setAutoCommit(false);

			// First save the payment method
			int paymentMethodId = PaymentMethodDAO.getInstance().savePaymentMethod(conn, paymentMethod);

			// Now create an entry in the bridge table
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, projectId);
			stmt.setInt(2, paymentMethodId);
			
			int numRowsInserted = stmt.executeUpdate();
			if(numRowsInserted == 0) {
				throw new SQLException("Creating project payment method failed, no rows affected.");
			}

			conn.commit();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	public void deletePaymentMethod(int paymentMethodId) throws SQLException {
		
		// first delete the payment method
		PaymentMethodDAO.getInstance().deletePaymentMethod(paymentMethodId);
		
		// now delete the entry in the bridge table
		// NOTE: there is a trigger on paymentMethod that will 
		//       delete all entries in projectPaymentMethod that have this paymentMethodId
		// unlinkProjectPaymentMethod(paymentMethodId, 0);
		
	}

	public void unlinkProjectPaymentMethod(int paymentMethodId, int projectId) throws SQLException {
		
		if(paymentMethodId == 0 && projectId == 0) {
			log.error("paymentMethodId and projectId are both 0 in unlinkProjectPaymentMethod. Skipping...");
			return;
		}
		
		String sql = "DELETE FROM projectPaymentMethod WHERE ";
		if(paymentMethodId != 0) {
			sql += "paymentMethodID="+paymentMethodId;
		}
		if(paymentMethodId != 0 && projectId != 0)
			sql += " AND ";
		if(projectId != 0) {
			sql += " projectID="+projectId;
		}
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
            stmt.executeUpdate(sql);
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }
}
