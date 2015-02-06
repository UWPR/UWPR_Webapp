/**
 * RateTypeDAO.java
 * @author Vagisha Sharma
 * Apr 29, 2011
 */
package org.uwpr.costcenter;

import org.apache.log4j.Logger;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Affiliation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RateTypeDAO {

	private RateTypeDAO() {}
	
	private static final Logger log = Logger.getLogger(RateTypeDAO.class);
	
	private static RateTypeDAO instance = new RateTypeDAO();
	
	public static RateTypeDAO getInstance() {
		return instance;
	}
	
	public RateType getRateType (int rateTypeId) throws SQLException {
		
		String sql = "SELECT * FROM rateType WHERE id="+rateTypeId;
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			conn = DBConnectionManager.getConnection("pr");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {
				RateType rateType = new RateType();
				rateType.setId(rateTypeId);
				rateType.setName(rs.getString("name"));
				rateType.setDescription(rs.getString("description"));
				return rateType;
			}
			else {
				log.error("No entry found in table rateType for id: "+rateTypeId);
				return null;
			}
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
			if(stmt != null) try {stmt.close();} catch(SQLException e){}
			if(rs != null) try {rs.close();} catch(SQLException e){}
		}
	}
	
	public List<RateType> getAllRateTypes() throws SQLException {
		
		Connection conn = null;

		try {
			conn = DBConnectionManager.getConnection("pr");
            return getAllRateTypes(conn);
		}
		finally {
			if(conn != null) try {conn.close();} catch(SQLException e){}
		}
	}

    public List<RateType> getAllRateTypes(Connection conn) throws SQLException {

    		String sql = "SELECT * FROM rateType";
    		Statement stmt = null;
    		ResultSet rs = null;

    		try {
    			stmt = conn.createStatement();
    			rs = stmt.executeQuery(sql);

    			List<RateType> rateTypes = new ArrayList<RateType>();
    			while(rs.next()) {
    				RateType rateType = new RateType();
    				rateType.setId(rs.getInt("id"));
    				rateType.setName(rs.getString("name"));
    				rateType.setDescription(rs.getString("description"));
    				rateTypes.add(rateType);
    			}
    			return rateTypes;
    		}
    		finally {
    			if(stmt != null) try {stmt.close();} catch(SQLException e){}
    			if(rs != null) try {rs.close();} catch(SQLException e){}
    		}
    	}
	
	public RateType getRateTypeForAffiliation(Affiliation affiliation, boolean feeForService) throws SQLException {
		
		String rateTypeName = affiliation.name();
		if(feeForService) {
			rateTypeName += "_FFS";
		}
		List<RateType> rateTypes = getAllRateTypes();
        for(RateType rateType: rateTypes) {
        	if(rateType.getName().equals(rateTypeName)) {
        		return rateType;
        	}
        }
        return null;
	}
	
	public RateType getRateForUwprSupportedProjects() throws SQLException {
		
		
		return RateTypeDAO.getInstance().getRateTypeForAffiliation(Affiliation.UW, false);
		
//		List<RateType> rateTypes = getAllRateTypes();
//        for(RateType rateType: rateTypes) {
//        	if(rateType.getName().equals("FREE")) {
//        		return rateType;
//        	}
//        }
//        return null;
	}
}
