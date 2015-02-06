package org.yeastrc.db;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class DBConnectionManager {

	public static Connection getConnection(String db) throws SQLException {
		try {
			Context ctx = new InitialContext();	
			DataSource ds;
			Connection conn;

			if (db.equals("pr")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/pr"); }
			else if (db.equals("sgd")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/sgd"); }
			else if (db.equals("sgd_static_200409")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/sgd_static_200409"); }
			else if (db.equals("go")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/go"); }
			else if (db.equals("go_xref")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/go_xref"); }
			else if (db.equals("wormbase")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/wormbase"); }
			else if (db.equals("nrseq")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/nrseq"); }
			else if (db.equals("scop")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/scop"); }
			else if (db.equals("pdr")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/pdr"); }
			else if (db.equals("hgnc")) { ds = (DataSource)ctx.lookup("java:comp/env/jdbc/hgnc"); }
			
			else { throw new SQLException("Invalid database name passed into DBConnectionManager."); }

			if (ds != null) {
				conn = ds.getConnection();
				if (conn != null) { return conn; }
				else { throw new SQLException("Got a null connection..."); }
			}

			throw new SQLException("Got a null DataSource...");
		} catch (NamingException ne) {
			throw new SQLException("Naming exception: " + ne.getMessage() + "(" + db + ")" );
		}
	}
}