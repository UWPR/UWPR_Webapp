/**
 * 
 */
package org.uwpr.instrumentlog;

import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.PaymentMethodDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InstrumentUsagePaymentDAO.java
 * @author Vagisha Sharma
 * Jun 2, 2011
 * 
 */
public class InstrumentUsagePaymentDAO {

	private static final InstrumentUsagePaymentDAO instance = new InstrumentUsagePaymentDAO();
	
	private InstrumentUsagePaymentDAO () {}
	
	public static InstrumentUsagePaymentDAO getInstance() {
		return instance;
	}
	
	public List<InstrumentUsagePayment> getPaymentsForUsage(int instrumentUsageId) throws SQLException {
		
		return getPaymentsForUsage(instrumentUsageId, 0);
	}

    public List<InstrumentUsagePayment> getPaymentsForUsage(int instrumentUsageId, int paymentMethodId) throws SQLException {

    		Connection conn = null;
    		Statement stmt = null;
    		ResultSet rs = null;


            String sql = "SELECT * FROM instrumentUsagePayment WHERE instrumentUsageID="+instrumentUsageId;
            if(paymentMethodId != 0)
            {
                sql += " AND paymentMethodID="+paymentMethodId;
            }

            PaymentMethodDAO pmDao = PaymentMethodDAO.getInstance();

            List <InstrumentUsagePayment> payments = new ArrayList<InstrumentUsagePayment>();

            try {
            	conn = getConnection();
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                while (rs.next()) {

                	InstrumentUsagePayment payment = new InstrumentUsagePayment();
                	payment.setInstrumentUsageId(instrumentUsageId);
            	    int pmId = rs.getInt("paymentMethodID");
                	payment.setPercent(rs.getBigDecimal("percentPayment"));
                	payments.add(payment);

                	PaymentMethod pm = pmDao.getPaymentMethod(pmId);
                	if(pm == null) {
                		throw new SQLException("No payment method found for ID: "+pmId);
                	}
                	payment.setPaymentMethod(pm);

                }
                return payments;

            } catch (SQLException e) {
                e.printStackTrace();
            }

            finally {
                // Always make sure result sets and statements are closed,
            	if(conn != null) try {conn.close();} catch(SQLException e){}
    			if(stmt != null) try {stmt.close();} catch(SQLException e){}
    			if(rs != null) try {rs.close();} catch(SQLException e){}
            }
            return null;
    	}
	
	public void savePayment(InstrumentUsagePayment payment) throws SQLException {
		
		String sql = "INSERT INTO instrumentUsagePayment (instrumentUsageID, paymentMethodID, percentPayment) VALUES (?,?,?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		
		try {
			conn = getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, payment.getInstrumentUsageId());
			stmt.setInt(2, payment.getPaymentMethod().getId());
			stmt.setBigDecimal(3, payment.getPercent());
			
			stmt.executeUpdate();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}

    private Connection getConnection() throws SQLException {
        return DBConnectionManager.getMainDbConnection();
    }

    public boolean hasInstrumentUsageForPayment(int paymentMethodId) throws SQLException {
		
		String sql = "SELECT count(*) FROM instrumentUsagePayment WHERE paymentMethodID = "+paymentMethodId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				int count = rs.getInt(1);
                return count != 0;
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
		
		return false;
	}

	public void deletePaymentsForUsage (int instrumentUsageId) throws SQLException {

		String sql = "DELETE FROM instrumentUsagePayment where instrumentUsageID="+instrumentUsageId;
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.executeUpdate();
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
		}
	}
}
