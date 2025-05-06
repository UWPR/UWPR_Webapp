/*
 * CycleUtils.java
 *
 * Created November 11, 2003
 *
 * Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.user;

import java.security.SecureRandom;
import java.sql.*;
import org.yeastrc.data.*;
import org.yeastrc.db.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.*;


/**
 * A set of static methods performing operations related to users
 * of the web site.
 */
public class UserUtils {

	/**
	 * Get a populated User object corresponding to a username.
	 * @param username The username to test
	 * @return The User object corresponding to that username.
	 * @throws NoSuchUserException if that username does not exist.
	 * @throws SQLException if a database error was encountered.
	 */
	public static User getUser(String username) throws NoSuchUserException, SQLException {
		// The User to return
		User theUser;
		
		// Make sure the username isn't null
		if (username == null) { throw new NoSuchUserException("got null for username in UserUtils.getUser"); }

		// Get our connection to the database.
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.prepareStatement("SELECT researcherID FROM tblUsers WHERE username = ?");
			stmt.setString(1, username);

			rs = stmt.executeQuery();
		
			// No rows returned.
			if( !rs.next() ) {
				throw new NoSuchUserException("Username not found.");
			}

			theUser = new User();

			try {
				theUser.load(rs.getInt("researcherID"));
			} catch(InvalidIDException e) {
				throw new NoSuchUserException("Somehow, we got an invalid ID (" + rs.getInt("researcherID") + ") after we got the ID from the username...  This can't be good.");
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}

		return theUser;
	}

	/**
	 * Get a populated User object from the request passed in.
	 * @param The request object to check for the user
	 * @return The user object, or null if no user object was found
	 */
	public static User getUser(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session == null) { return null; }

		return (User)(session.getAttribute("user"));
	}


	/**
	 * Determine whether or a not a User with the supplied username exists
	 * @param username The username to test
	 * @return true if the user exists, false if not
	 * @throws SQLException if a database error was encountered
	 */
	public static boolean userExists(String username) throws SQLException {
		boolean returnVal = false;
		
		if (username == null || username.equals("") ) { return false; }
	   
		// Get our connection to the database.
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		

		try {
			stmt = conn.prepareStatement("SELECT researcherID FROM tblUsers WHERE username = ?");
			stmt.setString(1, username);

			rs = stmt.executeQuery();
		
			// No rows returned.
			if( !rs.next() ) {
				returnVal = false;
			} else {
				returnVal = true;
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}

		return returnVal;
	}

	/**
	 * Determine whether or a not a User with the supplied researcherID exists
	 * @param researcherID The researcherID to test
	 * @return true if the user exists, false if not
	 * @throws SQLException if a database error was encountered
	 */
	public static boolean userExists(int researcherID) throws SQLException {
		boolean returnVal = false;
	   
		// Get our connection to the database.
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement("SELECT researcherID FROM tblUsers WHERE researcherID = ?");
			stmt.setInt(1, researcherID);

			rs = stmt.executeQuery();
		
			// No rows returned.
			if( !rs.next() ) {
				returnVal = false;
			} else {
				returnVal = true;
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}

		return returnVal;
	}


	/**
	 * Determine whether or a not a Researcher with the supplied email exists
	 * @param email The email to test
	 * @return The researcher ID of the researcher if it exists, -1 if it doesn't
	 * @throws SQLException if a database error was encountered
	 */
	public static int emailExists(String email) throws SQLException {
		int returnVal = -1;
		
		if (email == null || email.equals("") ) { return -1; }
	   
		// Get our connection to the database.
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.prepareStatement("SELECT researcherID FROM tblResearchers WHERE researcherEmail = ?");
			stmt.setString(1, email);

			rs = stmt.executeQuery();
		
			// No rows returned.
			if( !rs.next() ) {
				returnVal = -1;
			} else {
				returnVal = rs.getInt("researcherID");
			}
			
			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
		}
		finally {

			// Always make sure result sets and statements are closed,
			// and the connection is returned to the pool
			if (rs != null) {
				try { rs.close(); } catch (SQLException e) { ; }
				rs = null;
			}
			if (stmt != null) {
				try { stmt.close(); } catch (SQLException e) { ; }
				stmt = null;
			}
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) { ; }
				conn = null;
			}
		}

		return returnVal;
	
	}

    private static Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }



	private static final List<Character> LOWERCASE = "abcdefghijklmnopqrstuvwxyz".chars().mapToObj(c -> (char)c).collect(Collectors.toList());
	private static final List<Character> UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".chars().mapToObj(c -> (char)c).collect(Collectors.toList());
	private static final List<Character> DIGITS = "0123456789".chars().mapToObj(c -> (char)c).collect(Collectors.toList());
	private static final List<Character> SYMBOLS = "!@#$%^&*+=?".chars().mapToObj(c -> (char)c).collect(Collectors.toList());

	private static final List<Character> ALL_CHARS = new ArrayList<Character>() {{
		addAll(LOWERCASE);
		addAll(UPPERCASE);
		addAll(DIGITS);
		addAll(SYMBOLS);
	}};

	private static final int PASSWORD_LEN = 12;

	public static String generatePassword()
	{
		SecureRandom random = new SecureRandom();

		List<Character> passwordChars = new ArrayList<>(PASSWORD_LEN);

		// Ensure that there is at least one character from each character category.
		addRandomChar(LOWERCASE, passwordChars, random);
		addRandomChar(UPPERCASE, passwordChars, random);
		addRandomChar(DIGITS, passwordChars, random);
		addRandomChar(SYMBOLS, passwordChars, random);

		// Create a copy of ALL_CHARS for this password generation
		List<Character> allChars = new ArrayList<>(ALL_CHARS);
		// Shuffle it to ensure randomness for this particular password
		Collections.shuffle(allChars, random);

		// Add more characters until we are at the desired password length
		while (passwordChars.size() < PASSWORD_LEN)
		{
			addRandomChar(allChars, passwordChars, random);
		}

		// Final shuffle to randomize character positions
		Collections.shuffle(passwordChars, random);

		StringBuilder password = new StringBuilder(PASSWORD_LEN);
		for (Character c : passwordChars) {
			password.append(c);
		}
		return password.toString();
	}

	/**
	 * Adds a random character from the given category to the password
	 */
	private static void addRandomChar(List<Character> category, List<Character> passwordChars, SecureRandom random)
	{
		int randomIdx = random.nextInt(category.size());
		passwordChars.add(category.get(randomIdx));
	}
}