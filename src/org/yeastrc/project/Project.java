/*
 * Project.java
 *
 * Created October 15, 2003
 *
 * Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.project;

import org.apache.commons.lang.StringUtils;
import org.uwpr.costcenter.RateType;
import org.uwpr.costcenter.RateTypeDAO;
import org.yeastrc.data.IData;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.www.project.WordCounter;
import org.yeastrc.www.user.Groups;

import java.sql.*;
import java.util.*;


/**
 * At the core of the YRC informatics platform is the Project.  All data of all kind in
 * the platform point back to a project.  Projects are created when scientists request
 * associations with the YRC in the form of collaborations, plasmid dissemination, training
 * or what have you.  As such, it will have dates, collaborators and other administrative
 * information associated with it, which will allow us to determine permissions on data and
 * so on.
 * <P>This class holds methods for setting and getting all administrative information associated
 * with a project in the database.
 * <P>This class is actually abstract, as projects can, at the time of this writing, be one of
 * 4 different types of projects.  Each of these 4 types has a concrete implementation of Project,
 * which are Collaboration, Dissemination, Technology, Training
 *
 * @version 2003-03-23
 * @author Michael Riffle <mriffle@u.washington.edu>
 */
public abstract class Project implements Comparable, IData, ComparableProject {	

	/**
	 * Instantiate a new project.
	 */
	public Project() {		
		this.id = 0;
		this.NCRRID = 0;
		
		this.generalFunding = null;
		this.federalFunding = null;
		//this.federalFundingOther = null;

		this.PI = null;
        this.researchers = new ArrayList<Researcher>();

		this.submitDate = null;
		this.lastChange = null;
		
		this.title = null;
		this.projectAbstract = null;
		this.publicAbstract = null;
		this.progress = null;
		this.keywords = null;
		this.publications = null;
		this.comments = null;
		this.instrumentTimeExpl = null;
		
		this.BTA = (float)0.0;
		
		this.axisI = null;
		this.axisII = null;
		
		this.extensionReasons = null;
	}

	/**
	 * Determine whether or not a Researcher has READ access to this project, that is
	 * are they affiliated with the project OR are they members of groups that the 
	 * project is associated with.
	 * @param researcher The Researcher to check
	 * @return true if they have access, false if not
	 */
	public boolean checkReadAccess(Researcher researcher) {		
		if (researcher == null) { return false; }

        Groups groupsMan = Groups.getInstance();

        // Admins have access to all projects.
        if (groupsMan.isMember(researcher.getID(), "administrators"))
        {
            return true;
        }

        // User has access if he/she is the project PI
        if(this.PI != null && this.PI.equals(researcher))
        {
            return true;
        }

        // If the user is listed as a researcher on the project they have read access
        for(Researcher r: this.researchers)
        {
            if(r != null && r.equals(researcher))
                return true;
        }

		
		// If the user is a member of one of the groups associated with the project
	    // they have read access
		String[] tGroups = this.getGroupsArray();
		if(tGroups == null)
			return false;
		
		for (int i = 0; i < tGroups.length; i++) {
			if (groupsMan.isMember(researcher.getID(), tGroups[i]))
				return true;
		}
		
//		// If the researcher is in a group within the YRC, then they should have read access to the project
//		if (groupsMan.isInAGroup(researcher.getID()))
//			return true;
		
		// Access -DENIED-
		return false;
	}
	
	
	/**
	 * Determine whether or not a Researcher has WRITE access to this project, that is
	 * are they affiliated with the project.
	 * @param researcher The Researcher to check
	 * @return true if they have access, false if not
	 */
	public boolean checkAccess(Researcher researcher) {		
		if (researcher == null) { return false; }

        Groups groupsMan = Groups.getInstance();

        // Admins have access to all projects.
        if (groupsMan.isMember(researcher.getID(), "administrators"))
            return true;

        // User has write access if he/she is the project PI
        if(this.PI != null && this.PI.equals(researcher))
            return true;

        // If the user is listed as a researcher on the project they have write access
        for(Researcher r: this.researchers) {
            if(r != null && r.equals(researcher))
                return true;
        }

        // Access -DENIED-
        return false;
	}


	/**
	 * Will return a Set of group names, to which this project belongs.  All groups names will be contained
	 * in the GROUPS array in the Projects class.
	 * @return A set of groups in the YRC, with which this project is affiliated.
	 */
	public abstract Set<String> getGroups();
	
	
	/**
	 * Will return the unabbreviated name for the type of the project.  At the time of this writing, 
	 * it will be one of the following:
	 * "Collaboration", "Technology", "Training" or "Dissemination"
	 * @return The unabbreviated name for the type of the project.
	 */
	public abstract String getLongType();
	
	/**
	 * Will return the abbreviated name for the type of the project.  At the time of this writing,
	 * it will be one of the following: "C", "B"
	 * @return The abbreviated name for the type of the project.
	 */
	public abstract String getShortType();

	/**
	 * Set a group to which this project belongs.
	 * This will add to a set of groups, so each consecutive call adds another group
	 * to the Set.  All groups passed in here should be present in Projects.GROUPS array.
	 * Ideally one would use the static variables in Projects for setting this in the form of:
	 * setGroup(Projects.YATES)
	 * @param group The group to add for this project.
	 * @throws InvalidIDException if group is an invalid group.
	 */
	public abstract void setGroup(String group) throws InvalidIDException;

	/**
	 * Removes a group from the set of groups to which this project belongs.  If the group
	 * isn't in the set, nothing happens.
	 * @param group The group to remove for this project.
	 */
	public abstract void removeGroup(String group);
	
	/**
	 * Removes all the groups from the set of groups to which this project belongs.  Use with caution!
	 */
	public abstract void clearGroups();

	/**
	 * Will return a String, listing the groups to which this project belongs.
	 * @return A String list of all the groups to which this project belongs.
	 */
	public String getGroupsString() { return Projects.getGroupsString(this.getGroups()); }	

	/**
	 * Use this method to save this object's data to the database.
	 * These objects should generally represent a single row of data in a table.
	 * This will update the row if this object represents a current row of data, or it will
	 * insert a new row if this data is not in the database (that is, if there is no ID in the object).
	 * @throws InvalidIDException If there is an ID set in this object, but it is not in the database.
	 * @throws SQLException If there is a problem interracting with the database
	 */
	public void save() throws InvalidIDException, SQLException {

		// Get our connection to the database.
		Connection conn = getConnection();
		Statement stmt = null;
		ResultSet rs = null;	

		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			// Get our updatable result set
			String sqlStr = "SELECT * FROM tblProjects WHERE projectID = " + this.id;
			rs = stmt.executeQuery(sqlStr);

			// See if we're updating a row or adding a new row.
			if (this.id > 0) {

				// Make sure this row is actually in the database.  This shouldn't ever happen.
				if (!rs.next()) {
					throw new InvalidIDException("ID was set in a Project, but not found in database on save()");
				}

				// Make sure the result set is set up w/ current values from this object
				rs.updateString("projectType", getShortType());
				
				if (this.title == null) { rs.updateNull("projectTitle"); }
				else { rs.updateString("projectTitle", this.title); }

				/*
				 * Update our researchers.  The value for the researcher ID will be taken from
				 * the respective researcher object and set in the Project table.  If there is no
				 * respective researcher object (it didn't exist when the Project object was loaded, and non were created)
				 * it will be set to 0.  This should be a self-correcting system if Researchers are
				 * deleted from the system causing Projects to contain invalid researcher IDs.
				 */
				if (this.PI != null) {
					rs.updateInt("projectPI", this.PI.getID());
				} else { rs.updateNull("projectPI"); }

				if (this.generalFunding == null) { rs.updateNull("projectFundingTypes"); }
				else { rs.updateString("projectFundingTypes", StringUtils.join(this.generalFunding.toArray(), ",")); }
				
				if (this.federalFunding == null) { rs.updateNull("projectFundingFederal"); }
				else { rs.updateString("projectFundingFederal", StringUtils.join(this.federalFunding.toArray(), ",")); }
				
				if (this.projectAbstract == null) { rs.updateNull("projectAbstract"); }
				else { rs.updateString("projectAbstract", this.projectAbstract); }

				if (this.publicAbstract == null) { rs.updateNull("publicAbstract"); }
				else { rs.updateString("publicAbstract", this.publicAbstract); }
				
				if (this.progress == null) { rs.updateNull("projectProgress"); }
				else { rs.updateString("projectProgress", this.progress); }
				
				if (this.progressLastChange == null) { rs.updateNull("progressLastChange"); }
				else { rs.updateDate("progressLastChange", this.progressLastChange); }

				if (this.keywords == null) { rs.updateNull("projectKeywords"); }
				else { rs.updateString("projectKeywords", this.keywords); }

				if (this.publications == null) { rs.updateNull("projectPublications"); }
				else { rs.updateString("projectPublications", this.publications); }

				if (this.comments == null) { rs.updateNull("projectComments"); }
				else { rs.updateString("projectComments", this.comments); }
				
				if(this.instrumentTimeExpl == null) { rs.updateNull("instrumentTimeExpl"); }
				else { rs.updateString("instrumentTimeExpl", this.instrumentTimeExpl); }

				rs.updateFloat("projectBTA", this.BTA);
				
				if (this.axisI == null) { rs.updateNull("projectAxisI"); }
				else { rs.updateString("projectAxisI", this.axisI); }

				if (this.axisII == null) { rs.updateNull("projectAxisII"); }
				else { rs.updateString("projectAxisII", this.axisII); }
				
				if (this.NCRRID == 0) { rs.updateNull("NCRR_ID"); }
				else { rs.updateInt("NCRR_ID", this.NCRRID); }
				
				if (this.foundationName == null) rs.updateNull( "foundationName" );
				else rs.updateString( "foundationName", this.foundationName );
				
				if (this.grantNumber == null) rs.updateNull( "grantNumber" );
				else rs.updateString( "grantNumber", this.grantNumber );
				
				if (this.grantAmount == null) rs.updateNull( "grantAmount" );
				else rs.updateString( "grantAmount", this.grantAmount );
				
				// proteomics resource stuff:				
				rs.updateInt( "ltqRunsRequested", this.ltqRunsRequested );
				rs.updateInt( "ltq_etdRunsRequested", this.ltq_etdRunsRequested );
				rs.updateInt( "ltq_orbitrapRunsRequested", this.ltq_orbitrapRunsRequested );
				rs.updateInt( "ltq_ftRunsRequested", this.ltq_ftRunsRequested );
				rs.updateInt( "tsq_accessRunsRequested", this.tsq_accessRunsRequested );
				rs.updateInt( "tsq_vantageRunsRequested", this.tsq_vantageRunsRequested);
				
				if (this.cidFragmentation) rs.updateString( "cidFragmentation", "T" );
				else rs.updateString( "cidFragmentation", "F" );
				
				if (this.pqdFragmentation) rs.updateString( "pqdFragmentation", "T" );
				else rs.updateString( "pqdFragmentation", "F" );
				
				if (this.hcdFragmentation) rs.updateString( "hcdFragmentation", "T" );
				else rs.updateString( "hcdFragmentation", "F" );
				
				if (this.etdFragmentation) rs.updateString( "etdFragmentation", "T" );
				else rs.updateString( "etdFragmentation", "F" );

				if (this.ecdFragmentation) rs.updateString( "ecdFragmentation", "T" );
				else rs.updateString( "ecdFragmentation", "F" );
				
				if (this.irmpdFragmentation) rs.updateString( "irmpdFragmentation", "T" );
				else rs.updateString( "irmpdFragmentation", "F" );

				if (this.databaseSearchRequested) rs.updateString( "databaseSearchRequested",  "T" );
				else rs.updateString( "databaseSearchRequested", "F" );
				
				if (this.massSpecExpertiseRequested) rs.updateString( "massSpecExpertiseRequested",  "T" );
                else rs.updateString( "massSpecExpertiseRequested", "F" );
				
				if (this.scientificQuestion == null) rs.updateNull( "scientificQuestion" );
				else rs.updateString( "scientificQuestion", this.scientificQuestion );
				
				// !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
				if (this.extensionReasons == null) { rs.updateNull("extensionReasons"); }
                else { rs.updateString("extensionReasons", this.extensionReasons); }
				
				
				// Make sure the lastChange is updated correctly
				java.util.Date uDate = new java.util.Date();
				this.lastChange = new java.sql.Date(uDate.getTime());
				rs.updateDate("lastChange", this.lastChange);

                // affiliation
                if (this.affiliation == null) {rs.updateNull("affiliation");}
                else rs.updateString("affiliation", this.affiliation.name());

				// Update the row
				rs.updateRow();

			} else {
				// We're adding a new row.

				rs.moveToInsertRow();

				// Does this project have a parent project?
                if(this.parentProjectId > 0) {
                    rs.updateInt("parentProjectID", this.parentProjectId);
                }
                
				rs.updateString("projectType", getShortType());

				java.util.Date uDate = new java.util.Date();
				this.submitDate = new java.sql.Date(uDate.getTime());
				
				rs.updateDate("projectSubmitDate", this.submitDate);
				
				if (this.title == null) { rs.updateNull("projectTitle"); }
				else { rs.updateString("projectTitle", this.title); }

				/*
				 * Update our researchers.  The value for the researcher ID will be taken from
				 * the respective researcher object and set in the Project table.  If there is no
				 * respective researcher object (it didn't exist when the Project object was loaded, and non were created)
				 * it will be set to 0.  This should be a self-correcting system if Researchers are
				 * deleted from the system causing Projects to contain invalid researcher IDs.
				 */
				if (this.PI != null) {
					rs.updateInt("projectPI", this.PI.getID());
				} else { rs.updateNull("projectPI"); }

				if (this.generalFunding == null) { rs.updateNull("projectFundingTypes"); }
				else { rs.updateString("projectFundingTypes", StringUtils.join(this.generalFunding.toArray(), ",")); }
				
				if (this.federalFunding == null) { rs.updateNull("projectFundingFederal"); }
				else { rs.updateString("projectFundingFederal", StringUtils.join(this.federalFunding.toArray(), ",")); }
				
				if (this.projectAbstract == null) { rs.updateNull("projectAbstract"); }
				else { rs.updateString("projectAbstract", this.projectAbstract); }
				
				if (this.publicAbstract == null) { rs.updateNull("publicAbstract"); }
				else { rs.updateString("publicAbstract", this.publicAbstract); }
				
				if (this.progress == null) { rs.updateNull("projectProgress"); }
				else { rs.updateString("projectProgress", this.progress); }

				if (this.progressLastChange == null) { rs.updateNull("progressLastChange"); }
				else { rs.updateDate("progressLastChange", this.progressLastChange); }

				if (this.keywords == null) { rs.updateNull("projectKeywords"); }
				else { rs.updateString("projectKeywords", this.keywords); }

				if (this.publications == null) { rs.updateNull("projectPublications"); }
				else { rs.updateString("projectPublications", this.publications); }

				if (this.comments == null) { rs.updateNull("projectComments"); }
				else { rs.updateString("projectComments", this.comments); }
				
				if(this.instrumentTimeExpl == null) { rs.updateNull("instrumentTimeExpl"); }
				else { rs.updateString("instrumentTimeExpl", this.instrumentTimeExpl); }

				rs.updateFloat("projectBTA", this.BTA);
				
				if (this.axisI == null) { rs.updateNull("projectAxisI"); }
				else { rs.updateString("projectAxisI", this.axisI); }

				if (this.axisII == null) { rs.updateNull("projectAxisII"); }
				else { rs.updateString("projectAxisII", this.axisII); }
				
				if (this.NCRRID == 0) { rs.updateNull("NCRR_ID"); }
				else { rs.updateInt("NCRR_ID", this.NCRRID); }

				if (this.foundationName == null) rs.updateNull( "foundationName" );
				else rs.updateString( "foundationName", this.foundationName );
				
				if (this.grantNumber == null) rs.updateNull( "grantNumber" );
				else rs.updateString( "grantNumber", this.grantNumber );
				
				if (this.grantAmount == null) rs.updateNull( "grantAmount" );
				else rs.updateString( "grantAmount", this.grantAmount );
				
				// proteomics resource stuff:				
				rs.updateInt( "ltqRunsRequested", this.ltqRunsRequested );
				rs.updateInt( "ltq_etdRunsRequested", this.ltq_etdRunsRequested );
				rs.updateInt( "ltq_orbitrapRunsRequested", this.ltq_orbitrapRunsRequested );
				rs.updateInt( "ltq_ftRunsRequested", this.ltq_ftRunsRequested );
				rs.updateInt( "tsq_accessRunsRequested", this.tsq_accessRunsRequested );
                rs.updateInt( "tsq_vantageRunsRequested", this.tsq_vantageRunsRequested);
				
				if (this.cidFragmentation) rs.updateString( "cidFragmentation", "T" );
				else rs.updateString( "cidFragmentation", "F" );
				
				if (this.pqdFragmentation) rs.updateString( "pqdFragmentation", "T" );
				else rs.updateString( "pqdFragmentation", "F" );
				
				if (this.hcdFragmentation) rs.updateString( "hcdFragmentation", "T" );
				else rs.updateString( "hcdFragmentation", "F" );
				
				if (this.etdFragmentation) rs.updateString( "etdFragmentation", "T" );
				else rs.updateString( "etdFragmentation", "F" );

				if (this.ecdFragmentation) rs.updateString( "ecdFragmentation", "T" );
				else rs.updateString( "ecdFragmentation", "F" );
				
				if (this.irmpdFragmentation) rs.updateString( "irmpdFragmentation", "T" );
				else rs.updateString( "irmpdFragmentation", "F" );

				if (this.massSpecExpertiseRequested) rs.updateString( "massSpecExpertiseRequested",  "T" );
                else rs.updateString( "massSpecExpertiseRequested", "F" );
				
				if (this.databaseSearchRequested) rs.updateString( "databaseSearchRequested",  "T" );
				else rs.updateString( "databaseSearchRequested", "F" );
				
				if (this.scientificQuestion == null) rs.updateNull( "scientificQuestion" );
				else rs.updateString( "scientificQuestion", this.scientificQuestion );
				
				
				// !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
                if (this.extensionReasons == null) { rs.updateNull("extensionReasons"); }
                else { rs.updateString("extensionReasons", this.extensionReasons); }
                

                // affiliation
                if (this.affiliation == null) {rs.updateNull("affiliation");}
                else rs.updateString("affiliation", this.affiliation.name());


				rs.insertRow();

				// Get the ID generated for this item from the database, and set expID
				rs.last();
				this.id = rs.getInt("projectID");
				
				try {
					this.lastChange = rs.getDate("lastChange");
				} catch (Exception e) { ; }
			}

			// save the project researchers
            List<Integer> researcherIds = new ArrayList<Integer>(this.getResearchers().size());
            for(Researcher r: this.getResearchers())
            {
                if(r != null)
                {
                    researcherIds.add(r.getID());
                }
            }
            saveResearchersForProject(this.getID(), researcherIds);
		}
		catch(SQLException e) { throw e; }
		catch(InvalidIDException e) { throw e; }
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
	}


	/**
	 * Use this method to populate this object with data from the datbase.
	 * @param id The experiment ID to load.
	 * @throws InvalidIDException If this ID is not valid (or not found)
	 * @throws SQLException If there is a problem interracting with the database
	 */
	public void load(int id) throws InvalidIDException, SQLException {

		// Get our connection to the database.
		Connection conn = getConnection();
		Statement stmt = null;
		ResultSet rs = null;	

		try{
			stmt = conn.createStatement();

			// Our SQL statement
			String sqlStr = "SELECT * FROM tblProjects WHERE projectID = " + id;

			// Our results
			rs = stmt.executeQuery(sqlStr);

			// No rows returned.
			if( !rs.next() ) {
				throw new InvalidIDException("Load failed due to invalid Project ID.");
			}

			// Populate the object from this row.
			this.id = rs.getInt("projectID");
			this.parentProjectId = rs.getInt("parentProjectID");
			this.submitDate = rs.getDate("projectSubmitDate");
			this.title = rs.getString("projectTitle");
			
			/*
			 * Populate the researchers associated with this project.  If a problem is
			 * encountered loading a researcher, we will catch that here and set the object
			 * to null, instead of throwing back an exception.  This enables invalid entries
			 * to be entered into the database for researcher IDs (or for researchers to somehow
			 * be deleted from the database) in the Project table without
			 * blowing up project display pages.
			 */
			int tmpID;
			tmpID = rs.getInt("projectPI");
			if (tmpID != 0) {
				this.PI = new Researcher();
				try { this.PI.load(tmpID); }
				catch (InvalidIDException e) { this.PI = null; }
			}

			// Set up the project general funding types.
			String tmpStr;
			tmpStr = rs.getString("projectFundingTypes");
			if(tmpStr == null || tmpStr.equals("")) {
				this.generalFunding = null;
			} else {
				this.generalFunding = new HashSet();
				Object[] types = StringUtils.split(tmpStr, ",");
				for (int i = 0; i < types.length; i++) {
					this.generalFunding.add(types[i]);
				}
			}

			// Set up the project federal funding types.
			tmpStr = rs.getString("projectFundingFederal");
			if(tmpStr == null || tmpStr.equals("")) {
				this.federalFunding = null;
			} else {
				this.federalFunding = new HashSet();
				Object[] types = StringUtils.split(tmpStr, ",");
				for (int i = 0; i < types.length; i++) {
					this.federalFunding.add(types[i]);
				}
			}
			
			// Set up the rest of the object variables
			this.projectAbstract = rs.getString("projectAbstract");
			this.publicAbstract = rs.getString("publicAbstract");
			this.progress = rs.getString("projectProgress");
			this.keywords = rs.getString("projectKeywords");
			this.publications = rs.getString("projectPublications");
			this.comments = rs.getString("projectComments");
			this.instrumentTimeExpl = rs.getString("instrumentTimeExpl");
			this.BTA = rs.getFloat("projectBTA");
			this.axisI = rs.getString("projectAxisI");
			this.axisII = rs.getString("projectAxisII");
			this.lastChange = rs.getDate("lastChange");
			this.progressLastChange = rs.getDate("progressLastChange");
			this.NCRRID = rs.getInt("NCRR_ID");
			this.foundationName = rs.getString( "foundationName" );
			this.grantAmount = rs.getString( "grantAmount" );
			this.grantNumber = rs.getString( "grantNumber" );
			
			// proteomics resource stuff
			this.ltqRunsRequested = rs.getInt( "ltqRunsRequested" );
			this.ltq_etdRunsRequested = rs.getInt( "ltq_etdRunsRequested" );
			this.ltq_orbitrapRunsRequested = rs.getInt( "ltq_orbitrapRunsRequested" );
			this.ltq_ftRunsRequested = rs.getInt( "ltq_ftRunsRequested" );
			this.tsq_accessRunsRequested = rs.getInt( "tsq_accessRunsRequested" );
			this.tsq_vantageRunsRequested = rs.getInt("tsq_vantageRunsRequested");
			this.scientificQuestion = rs.getString( "scientificQuestion" );
			
			
			// !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
            this.extensionReasons = rs.getString("extensionReasons");
            
            
			if (rs.getString( "cidFragmentation" ) != null && rs.getString( "cidFragmentation" ).equals( "T" ) )
				this.cidFragmentation = true;
			else
				this.cidFragmentation = false;
			
			if (rs.getString( "pqdFragmentation" ) != null && rs.getString( "pqdFragmentation" ).equals( "T" ) )
				this.pqdFragmentation = true;
			else
				this.pqdFragmentation = false;			
			
			if (rs.getString( "hcdFragmentation" ) != null && rs.getString( "hcdFragmentation" ).equals( "T" ) )
				this.hcdFragmentation = true;
			else
				this.hcdFragmentation = false;
			
			if (rs.getString( "etdFragmentation" ) != null && rs.getString( "etdFragmentation" ).equals( "T" ) )
				this.etdFragmentation = true;
			else
				this.etdFragmentation = false;

			if (rs.getString( "ecdFragmentation" ) != null && rs.getString( "ecdFragmentation" ).equals( "T" ) )
				this.ecdFragmentation = true;
			else
				this.ecdFragmentation = false;

			if (rs.getString( "irmpdFragmentation" ) != null && rs.getString( "irmpdFragmentation" ).equals( "T" ) )
				this.irmpdFragmentation = true;
			else
				this.irmpdFragmentation = false;
			
			if (rs.getString( "databaseSearchRequested" ) != null && rs.getString( "databaseSearchRequested" ).equals( "T" ) )
				this.databaseSearchRequested = true;
			else
				this.databaseSearchRequested = false;
			
			if (rs.getString( "massSpecExpertiseRequested" ) != null && rs.getString( "massSpecExpertiseRequested" ).equals( "T" ) )
                this.massSpecExpertiseRequested = true;
            else
                this.massSpecExpertiseRequested = false;
			
			// affiliation
            this.affiliation = Affiliation.forName(rs.getString("affiliation"));

			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
		}
		catch(SQLException e) { throw e; }
		catch(InvalidIDException e) { throw e; }
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

        // set the researchers for this project
        this.setResearchers(getResearchersForProject(this.getID()));
	}

    private List<Researcher> getResearchersForProject(int projectId) throws SQLException
    {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {

            String sql = "SELECT * FROM projectResearcher WHERE projectID = "+projectId;

            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            List<Researcher> researchers = new ArrayList<Researcher>();
            while (rs.next()) {
                int tmpId = (rs.getInt("researcherID"));
                Researcher r = new Researcher();
                try {r.load(tmpId);}
                catch(InvalidIDException e) { r = null;}
                if(r != null)
                    researchers.add(r);
            }
            return researchers;

        } finally {

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
    }

    private void saveResearchersForProject(int projectId, List<Integer> researcherIds) throws SQLException
    {

        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();


            // delete old entries
            String sqlStr = "DELETE FROM projectResearcher WHERE projectID = " + projectId;
            stmt.executeUpdate(sqlStr);
            stmt.close();

            // add new ones
            stmt = conn.createStatement();
            Set<Integer> uniqIds = new HashSet<Integer>(researcherIds);
            if(uniqIds.size() == 0)
                return;
            sqlStr = "INSERT INTO projectResearcher (projectID, researcherID) VALUES ";
            for(Integer id: uniqIds) {
                sqlStr += "("+projectId+","+id+"),";
            }
            sqlStr = sqlStr.substring(0, sqlStr.length() - 1); // remove last comma
            stmt.executeUpdate(sqlStr);

        } finally {

            // Always make sure result sets and statements are closed,
            // and the connection is returned to the pool
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { ; }
                stmt = null;
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { ; }
                conn = null;
            }
        }
    }

    private Connection getConnection() throws SQLException
    {
        return DBConnectionManager.getMainDbConnection();
    }

    /**
	 * Use this method to delete the data underlying this object from the database.
	 * Doing so will delete the row from the table corresponding to this object, and
	 * will remove the ID value from the object (since it represents the primary key)
	 * in the database.  This will cause subsequent calls to save() on the object to
	 * insert a new row into the database and generate a new ID.
	 * This will also call delete() on instantiated IData objects for all rows in the
	 * database which are dependent on this row.  For example, calling delete() on a
	 * MS Run objects would call delete() on all Run Result objects, which would then
	 * call delete() on all dependent Peptide objects for those results.
	 * Pre: object is populated with a valid ID.
	 * @throws SQLException if there is a problem working with the database.
	 * @throws InvalidIDException if the ID isn't set in this object, or if the ID isn't
	 * valid (that is, not found in the database).
	 */
	public void delete() throws InvalidIDException, SQLException {

		// Get our connection to the database.
		Connection conn = getConnection();
		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			// Our SQL statement
			String sqlStr = "SELECT projectID FROM tblProjects WHERE projectID = " + this.id;

			// Our results
			rs = stmt.executeQuery(sqlStr);

			// No rows returned.
			if( !rs.next() ) {
				throw new InvalidIDException("Attempted to delete a Project not found in the database.");
			}

			// Delete the result row.
			rs.deleteRow();		

			rs.close();
			rs = null;
			
			stmt.close();
			stmt = null;
			
			conn.close();
			conn = null;
			
			// Set the ID to 0 in the subclass of Project in delete() there.
			// this.id = 0;
		}
		catch(SQLException e) { throw e; }
		catch(InvalidIDException e) { throw e; }
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
	}


	/**
	 * For Comparable.  Does a comparison of project IDs.
	 * @param o The project to compare this one to.
	 */
	public int compareTo(Object o) {
		Project p = (Project)o;

		if (this.id > p.getID()) { return 1; }
		if (this.id < p.getID()) { return -1; }
		return 0;
	}


	// SET METHODS
	
	/**
	 * set the PI
	 * @param PI The PI for the project.
	 */
	public void setPI(Researcher PI) { this.PI = PI; }

    public void setResearchers(List<Researcher> researchers)
    {
        this.researchers = researchers;
    }
	
	
	/**
	 * Set the title for the project.
	 * @param title The project title.
	 */
	public void setTitle(String title) { this.title = title; }
	
	/**
	 * Sets the funding type for this project.  There are no restrictions on what can be
	 * set as a funding type, but the database may have limitations.  At the time of writing
	 * this the valid types are: 'FEDERAL','FOUNDATION','INDUSTRY','PROFASSOC','LOCGOV','OTHER'
	 * The types are stored as a set, so multiple calls to this function will add types to the
	 * set.  To clear the set, call clearFundingTypes()
	 * @param funds The type of funding to add to this project.
	 */
	public void setFundingType(String funds) {
		if (this.generalFunding == null) { this.generalFunding = new HashSet(); }
		this.generalFunding.add(funds);
	}
	
	/**
	 * Clears all funding types from this Project
	 */
	public void clearFundingTypes() {
		this.generalFunding = null;
	}
	
	/**
	 * Sets the federal funding type for this project.  There are no restrictions on what can be
	 * set as a federal funding type, but the database may have limitationis.  At the time of writing
	 * the valid types are: 'NASA','NIH','NSF','DOD','DOE','OTHER','NIST','DVA'
	 * The types are stored as a set, so multiple calls to this function will add types to the
	 * set.  To clear the set, call clearFederalFundingTypes()
	 * @param funds The type of federal funding to add to this project.
	 */
	public void setFederalFundingType(String funds) {
		if (this.federalFunding == null) { this.federalFunding = new HashSet(); }
		this.federalFunding.add(funds);
	}
	
	/**
	 * Clears all federal funding types from this project
	 */
	public void clearFederalFundingTypes() {
		this.federalFunding = null;
	}

	/**
	 * Set the text for the abstract for this project
	 * @param arg The text for the abstract of this project
	 */
	public void setAbstract(String arg) { this.projectAbstract = arg; }
	
	/**
	 * Set the text for the PUBLIC abstract for this project
	 * @param arg The text for the PUBLIC abstract for this project
	 */
	public void setPublicAbstract(String arg) { this.publicAbstract = arg; }
	
	/**
	 * Set the text for a description of the progress for this project.  Note, this will
	 * override the currently text for the progress.  To add, use addProgress()
	 * @param arg the text for the progress
	 */
	public void setProgress(String arg) {
		if (this.progress != null && this.progress.equals(arg)) { return; }

		// The progress is being changed.
		this.progress = arg;
		
		java.util.Date tDate = new java.util.Date();
		this.progressLastChange = new java.sql.Date(tDate.getTime());
	}
	
	/**
	 * Add to the progress text for this project.  The text will be added, preceded by
	 * two newlines (\n\n) to the existing text for this project.
	 * @param arg the text to add to the progress
	 */
	public void addProgress(String arg) {
		if (this.progress == null) { this.setProgress(arg); }
		else { this.progress = this.progress + "\n\n" + arg; }
		
		java.util.Date tDate = new java.util.Date();
		this.progressLastChange = new java.sql.Date(tDate.getTime());
	}
	
	/**
	 * Set the string for the keywords for this project.
	 * As of yet, there is no format to the list.  Comma delimited, space delimited,
	 * tab delimited are all acceptable.  This may change in the future.
	 * @param arg The list of keywords to apply to this project.
	 */
	public void setKeywords(String arg) { this.keywords = arg; }
	
	/**
	 * Set the value for publications for this project.  This is a list of
	 * references or publications attached to this project.  It is a single
	 * descriptive String.
	 * @param arg The text for the publication list.
	 */
	public void setPublications(String arg) { this.publications = arg; }
	
	/**
	 * Set the value for any comments associated to this project.
	 * This will override the current value for comments.
	 * @param arg The comments for this project.
	 */
	public void setComments(String arg) { this.comments = arg; }
	
	/**
	 * Set the BTA for this project.  This value is used in the annual NCRR
	 * progress report.
	 * @param bta The BTA for this project.
	 */
	public void setBTA(float bta) { this.BTA = bta; }
	
	/**
	 * Set the AXIS I codes for this project, as a single String.  This value is
	 * used in the annual NCRR progress report
	 * @param arg the AXIS I code string
	 */
	public void setAxisI(String arg) { this.axisI = arg; }
	
	/**
	 * Set the AXIS II codes for this project, as a single String.  This value is
	 * used in the annual NCRR progress report
	 * @param arg the AXIS II code string
	 */
	public void setAxisII(String arg) { this.axisII = arg; }
	
	/**
	 * Set the NCRR ID for this project, which should be its value in the NCRR database
	 * @param arg the NCRR ID
	 */
	public void setNCRRID(int arg) { this.NCRRID = arg; }
	
	// GET METHODS
	/**
	 * Get the Project ID number
	 * @return the project ID number
	 */
	public int getID() { return this.id; }

	/**
	 * Get the PI for this project
	 * @return the project PI
	 */
	public Researcher getPI() { return this.PI; }

    public List<Researcher> getResearchers()
    {
        return this.researchers;
    }

	public List<Researcher> getInstrumentOperators(List<Researcher> allInstrumentOperators)
	{
		Set<Integer> instrumentOperatorIds = new HashSet<Integer>(allInstrumentOperators.size());
		for(Researcher operator: allInstrumentOperators)
		{
			instrumentOperatorIds.add(operator.getID());
		}
		// Filter the instrument operator list to the users listed on the project
		List<Researcher> projectResearchers = getResearchers();
		Set<Researcher> projectInstrumentOperators = new HashSet<Researcher>(projectResearchers.size());
		for(Researcher researcher: projectResearchers)
		{
			if(instrumentOperatorIds.contains(researcher.getID()))
			{
				projectInstrumentOperators.add(researcher);
			}
		}
		Researcher pi = getPI();
		if(pi != null && instrumentOperatorIds.contains(pi.getID()))
		{
			projectInstrumentOperators.add(pi);
		}
		return new ArrayList<>(projectInstrumentOperators);
	}
	/**
	 * Returns the project submit date as a String
	 * @return the project submit date in string form
	 */
	public java.util.Date getSubmitDate() { return this.submitDate; }
	
	/**
	 * Returns the project title.
	 * @return the project title.
	 */
	public String getTitle() {
		if (this.title == null || this.title.equals(""))
			return this.getLongType() + " Project";

		return this.title;
	}
	
	/**
	 * Returns a comma+space, sorted list of the general funding types for the project.
	 * @return a comma+space, sorted list of general funding types for the project
	 */
	public String getFundingTypes() {
		if (this.generalFunding == null) { return "None"; }
		
		Object[] types = this.generalFunding.toArray();
		Arrays.sort(types);
		
		return StringUtils.join(types, ", ");
	}

	/**
	 * Get the actual array of Strings representing the funding types
	 * @return an array of strings
	 */
	public String[] getFundingTypesArray() {
		if (this.generalFunding == null) { return null; }
		
		Object[] tmp = new String[0];		
		String[] types = (String[])(this.generalFunding.toArray(tmp));
		
		return types;
	}

	
	/**
	 * Get the actual array of Strings representing the funding types
	 * @return an array of strings
	 */
	public String[] getFragmentationTypesArray() {
		
		ArrayList<String> types = new ArrayList<String>();
		if (this.cidFragmentation) types.add( "CID" );
		if (this.pqdFragmentation) types.add( "PQD" );
		if (this.hcdFragmentation) types.add( "HCD" );
		if (this.etdFragmentation) types.add( "ETD" );
		if (this.ecdFragmentation) types.add( "ECD" );
		if (this.irmpdFragmentation) types.add( "IRMPD" );

		Object[] tmp = new String[0];		
		String[] retTypes = (String[])(types.toArray(tmp));
		
		return retTypes;
	}
	
	/**
	 * Get a string representation of the requested fragmentation types
	 * @return
	 */
	public String getFragmentationTypesString() {
		return org.apache.commons.lang.StringUtils.join( this.getFragmentationTypesArray(), ", " );
	}
	
	
	/**
	 * Returns a comma+space, sorted list of the federal funding types for the project.
	 * @return a comma+space, sorted list of federal funding types for the project
	 */
	public String getFederalFundingTypes() {
		if (this.federalFunding == null) { return "None"; }
	
		Object[] types = this.federalFunding.toArray();
		Arrays.sort(types);
		
		return StringUtils.join(types, ", ");
	}

	/**
	 * Get the actual array of Strings representing the federal funding types
	 * @return an array of strings
	 */
	public String[] getFederalFundingTypesArray() {
		if (this.federalFunding == null) { return null; }

		Object[] tmp = new String[0];		
		String[] types = (String[])(this.federalFunding.toArray(tmp));

		return types;
	}


	/**
	 * Get the actual array of Strings representing the YRC groups (short form)
	 * @return an array of strings
	 */
	public String[] getGroupsArray() {
		if (this.getGroups() == null) { return null; }
		
		Object[] tmp = new String[0];		
		String[] groups = (String[])(this.getGroups().toArray(tmp));
		
		return groups;
	}


	///**
	// * Returns the value of the "other" federal funding, which may have been entered by the user
	// * if their federal funding source wasn't on the list provided to them.
	// * @return the "other" federal funding source
	// */
	//public String getFederalFundingOther() { return this.federalFundingOther; }
	
	/**
	 * Returns the abstract for the project
	 * @return The abstract (text)
	 */
	public String getAbstract() { return this.projectAbstract; }
	
	/**
	 * Returns the public abstract for the project
	 * @return the public abstract
	 */
	public String getPublicAbstract() { return this.publicAbstract; }
	
	/**
	 * Returns the abstract in HTML form
	 * @return The abstract in HTML form
	 */
	public String getAbstractAsHTML() {
		return org.yeastrc.utils.HTML.convertToHTML(this.projectAbstract);
	}
	
	public String getPublicAbstractAsHTML() {
		return org.yeastrc.utils.HTML.convertToHTML(this.publicAbstract);
	}

	/**
	 * Returns the progress for the project
	 * @return The progress (Text)
	 */
	public String getProgress() { return this.progress; }
	
	/**
	 * Get the progress as HTML
	 * @return the progress as HTML
	 */
	public String getProgressAsHTML() {
		return org.yeastrc.utils.HTML.convertToHTML(this.progress);
	}
	
	public boolean isProgressReportValid() {
	    return hasValidProgressReport();
	}
	
	public boolean hasValidProgressReport() {
	    return progress != null && WordCounter.count(progress) >= 20;
	}
	
	/**
	 * Returns the keywords for the project, however they were entered.
	 * @return The keywords.
	 */
	public String getKeywords() { return this.keywords; }
	
	/**
	 * Returns the publications associated with this project.
	 * @return The publications
	 */
	public String getPublications() { return this.publications; }
	
	/**
	 * Get the publications as HTML
	 * @return the publications as HTML
	 */
	public String getPublicationsAsHTML() {
		return org.yeastrc.utils.HTML.convertToHTML(this.getPublications());
	}
	
	/**
	 * Returns the comments associated with this project.
	 * @return The comments
	 */
	public String getComments() { return this.comments; }
	
	/**
	 * Returns the comments as HTML
	 * @return The comments as HTML
	 */
	public String getCommentsAsHTML() {
		return org.yeastrc.utils.HTML.convertToHTML(this.getComments());
	}
	
	/**
	 * Returns the BTA entered for this project (used for NCRR report)
	 * @return The BTA
	 */
	public float getBTA() { return this.BTA; }
	
	/**
	 * Returns the Axis I string entered for this project. (used for NCRR report)
	 * @return the Axis I
	 */
	public String getAxisI() { return this.axisI; }
	
	/**
	 * Returns the Axis II string entered for this project. (used for NCRR report)
	 * @return the Axis II
	 */
	public String getAxisII() { return this.axisII; }

	/**
	 * Returns the last changed date (when the project was last modified in the database)
	 * @return The date of the last change to this project.
	 */
	public java.util.Date getLastChange() { return this.lastChange; }

	/**
	 * Returns the date the progress was last modified and saved
	 * @return The date of the last progress change to this project.
	 */
	public java.util.Date getProgressLastChange() { return this.progressLastChange; }

	/**
	 * Returns the NCRR ID for this project, from the NCRR database
	 * @return The NCRR ID
	 */
	public int getNCRRID() { return this.NCRRID; }

	/**
	 * Get a label to use to identify this project.
	 * @return A label used to identify this project, to a human being
	 */
	public String getLabel() {
		String label;

		label = String.valueOf(this.id);

		if (this.PI != null) {
			label += " - " + this.PI.getLastName();
		}
		
		label += " - " + this.getTitle();
		
		// Truncate if necessary
		if (label.length() > 70) {
			label = label.substring(0,66) + "...";
		}

		return label;
	}


	/**
	 * A string representation of a Project
	 */
	public String toString() {
		return "Project #" + this.id + " - " + this.title;
	}


	
	/**
	 * @return Returns the foundationName.
	 */
	public String getFoundationName() {
		return foundationName;
	}
	/**
	 * @param foundationName The foundationName to set.
	 */
	public void setFoundationName(String foundationName) {
		this.foundationName = foundationName;
	}
	/**
	 * @return Returns the grantAmount.
	 */
	public String getGrantAmount() {
		return grantAmount;
	}
	/**
	 * @param grantAmount The grantAmount to set.
	 */
	public void setGrantAmount(String grantAmount) {
		this.grantAmount = grantAmount;
	}
	/**
	 * @return Returns the grantNumber.
	 */
	public String getGrantNumber() {
		return grantNumber;
	}
	/**
	 * @param grantNumber The grantNumber to set.
	 */
	public void setGrantNumber(String grantNumber) {
		this.grantNumber = grantNumber;
	}
	
	
	// instance variables

	// The id this Project has in the database
	protected int id;
	
	// The id of the parent project(if any)
	private int parentProjectId;
	
	// The submit date of the project (actually a time stamp of it's creation)
	private java.sql.Date submitDate;
	
	// funding sources (general)
	private HashSet generalFunding;
	
	// federal funding sources
	private HashSet federalFunding;

	// "other" federal funding source(s)
	//private String federalFundingOther;

	// the Principle Investigator
	private Researcher PI;
	
	// researchers
	private List<Researcher> researchers;
	
	// Title of the project
	private String title;
	
	// Abstract for the project
	private String projectAbstract;
	
	// Public Abstract for the project
	private String publicAbstract;
	
	// A description of the progress of the project
	private String progress;
	
	// Keywords associated with the project, a comma delimited string
	private String keywords;
	
	// A list of publications (free text) associated with the project
	private String publications;
	
	// Any comments entered for this project
	private String comments;
	
	// Justification for requested instrument time
	private String instrumentTimeExpl;
	
	// Whether or not this project is new (will be T or F, this isn't used)
	//private String isNew;

	
	// !!!!!!!!!!!!! EXTENSION PROJECT REASONS !!!!!!!!!!!!!!!!!
	private String extensionReasons;
	// !!!!!!!!!!!!! EXTENSION PROJECT REASONS !!!!!!!!!!!!!!!!!
	
	// The BTA number associated with this project, used for annual report
	private float BTA;
	
	// The AXIS codes (as a string) associated with this project
	private String axisI;
	private String axisII;
	
	// When the project last changed (a timestamp from the database)
	private java.sql.Date lastChange;
	
	// When the progress was last changed
	private java.sql.Date progressLastChange;
	
	// The NCRR ID for this project
	private int NCRRID;

	// The name of the foundation that funded (if applicable)
	private String foundationName;
	
	// The grant number and ammount funding this project
	private String grantNumber;
	private String grantAmount;
	
	// proteomics resource-specific items
	private int ltqRunsRequested;
	private int ltq_etdRunsRequested;
	private int ltq_orbitrapRunsRequested;
	private int ltq_ftRunsRequested;
	private int tsq_accessRunsRequested;
	private int tsq_vantageRunsRequested;
	
	private boolean cidFragmentation;
	private boolean pqdFragmentation;
	private boolean hcdFragmentation;
	private boolean etdFragmentation;
	private boolean ecdFragmentation;
	private boolean irmpdFragmentation;
	
	private boolean databaseSearchRequested;
	private boolean massSpecExpertiseRequested;
	private String scientificQuestion;

    private Affiliation affiliation;

    public Affiliation getAffiliation() {
    		return affiliation;
    	}

    public void setAffiliation(Affiliation affiliation) {
    		this.affiliation = affiliation;
    	}

	/**
	 * @return the scientificQuestion
	 */
	public String getScientificQuestion() {
		return scientificQuestion;
	}

	/**
	 * @param scientificQuestion the scientificQuestion to set
	 */
	public void setScientificQuestion(String scientificQuestion) {
		this.scientificQuestion = scientificQuestion;
	}

	/**
	 * @return the cidFragmentation
	 */
	public boolean isCidFragmentation() {
		return cidFragmentation;
	}

	/**
	 * @param cidFragmentation the cidFragmentation to set
	 */
	public void setCidFragmentation(boolean cidFragmentation) {
		this.cidFragmentation = cidFragmentation;
	}

	/**
	 * @return the massSpecExpertiseRequested
	 */
	public boolean isMassSpecExpertiseRequested() {
		return massSpecExpertiseRequested;
	}

	/**
	 * @param massSpecExpertiseRequested the massSpecExpertiseRequested to set
	 */
	public void setMassSpecExpertiseRequested(boolean massSpecExpertiseRequested) {
		this.massSpecExpertiseRequested = massSpecExpertiseRequested;
	}
	
	/**
     * @return the databaseSearchRequested
     */
    public boolean isDatabaseSearchRequested() {
        return databaseSearchRequested;
    }

    /**
     * @param databaseSearchRequested the databaseSearchRequested to set
     */
    public void setDatabaseSearchRequested(boolean databaseSearchRequested) {
        this.databaseSearchRequested = databaseSearchRequested;
    }

	/**
	 * @return the ecdFragmentation
	 */
	public boolean isEcdFragmentation() {
		return ecdFragmentation;
	}

	/**
	 * @param ecdFragmentation the ecdFragmentation to set
	 */
	public void setEcdFragmentation(boolean ecdFragmentation) {
		this.ecdFragmentation = ecdFragmentation;
	}

	/**
	 * @return the etdFragmentation
	 */
	public boolean isEtdFragmentation() {
		return etdFragmentation;
	}

	/**
	 * @param etdFragmentation the etdFragmentation to set
	 */
	public void setEtdFragmentation(boolean etdFragmentation) {
		this.etdFragmentation = etdFragmentation;
	}

	/**
	 * @return the hcdFragmentation
	 */
	public boolean isHcdFragmentation() {
		return hcdFragmentation;
	}

	/**
	 * @param hcdFragmentation the hcdFragmentation to set
	 */
	public void setHcdFragmentation(boolean hcdFragmentation) {
		this.hcdFragmentation = hcdFragmentation;
	}

	/**
	 * @return the irmpdFragmentation
	 */
	public boolean isIrmpdFragmentation() {
		return irmpdFragmentation;
	}

	/**
	 * @param irmpdFragmentation the irmpdFragmentation to set
	 */
	public void setIrmpdFragmentation(boolean irmpdFragmentation) {
		this.irmpdFragmentation = irmpdFragmentation;
	}

	/**
	 * @return the ltq_etdRunsRequested
	 */
	public int getLtq_etdRunsRequested() {
		return ltq_etdRunsRequested;
	}

	/**
	 * @param ltq_etdRunsRequested the ltq_etdRunsRequested to set
	 */
	public void setLtq_etdRunsRequested(int ltq_etdRunsRequested) {
		this.ltq_etdRunsRequested = ltq_etdRunsRequested;
	}

	/**
	 * @return the ltq_ftRunsRequested
	 */
	public int getLtq_ftRunsRequested() {
		return ltq_ftRunsRequested;
	}

	/**
	 * @param ltq_ftRunsRequested the ltq_ftRunsRequested to set
	 */
	public void setLtq_ftRunsRequested(int ltq_ftRunsRequested) {
		this.ltq_ftRunsRequested = ltq_ftRunsRequested;
	}

	/**
	 * @return the ltq_orbitrapRunsRequested
	 */
	public int getLtq_orbitrapRunsRequested() {
		return ltq_orbitrapRunsRequested;
	}

	/**
	 * @param ltq_orbitrapRunsRequested the ltq_orbitrapRunsRequested to set
	 */
	public void setLtq_orbitrapRunsRequested(int ltq_orbitrapRunsRequested) {
		this.ltq_orbitrapRunsRequested = ltq_orbitrapRunsRequested;
	}

	/**
	 * @return the ltqRunsRequested
	 */
	public int getLtqRunsRequested() {
		return ltqRunsRequested;
	}

	/**
	 * @param ltqRunsRequested the ltqRunsRequested to set
	 */
	public void setLtqRunsRequested(int ltqRunsRequested) {
		this.ltqRunsRequested = ltqRunsRequested;
	}

	
	/**
	 * Returns the total number of requested runs
	 */
	public int getTotalRunsRequested() {
	    return ltqRunsRequested + ltq_etdRunsRequested + ltq_ftRunsRequested +
	    ltq_orbitrapRunsRequested + tsq_accessRunsRequested + tsq_vantageRunsRequested;
	}
	
	/**
	 * @return the pqdFragmentation
	 */
	public boolean isPqdFragmentation() {
		return pqdFragmentation;
	}

	/**
	 * @param pqdFragmentation the pqdFragmentation to set
	 */
	public void setPqdFragmentation(boolean pqdFragmentation) {
		this.pqdFragmentation = pqdFragmentation;
	}

	/**
	 * @return the tsq_accessRunsRequested
	 */
	public int getTsq_accessRunsRequested() {
		return tsq_accessRunsRequested;
	}

	/**
	 * @param tsq_accessRunsRequested the tsq_accessRunsRequested to set
	 */
	public void setTsq_accessRunsRequested(int tsq_accessRunsRequested) {
		this.tsq_accessRunsRequested = tsq_accessRunsRequested;
	}
	
	/**
     * @return the tsq_vantageRunsRequested
     */
    public int getTsq_vantageRunsRequested() {
        return tsq_vantageRunsRequested;
    }

    /**
     * @param tsq_vantageRunsRequested the tsq_vantageRunsRequested to set
     */
    public void setTsq_vantageRunsRequested(int tsq_vantageRunsRequested) {
        this.tsq_vantageRunsRequested = tsq_vantageRunsRequested;
    }

    public int getParentProjectID() {
        return parentProjectId;
    }

    public void setParentProjectID(int parentProjectId) {
        this.parentProjectId = parentProjectId;
    }

    public String getInstrumentTimeExpl() {
		return instrumentTimeExpl;
	}

	public void setInstrumentTimeExpl(String instrumentTimeExpl) {
		this.instrumentTimeExpl = instrumentTimeExpl;
	}
	
	public String getInstrumentTimeExplAsHTML() {
        return org.yeastrc.utils.HTML.convertToHTML(this.instrumentTimeExpl);
    }

	public String getExtensionReasons() {
        return extensionReasons;
    }

    public void setExtensionReasons(String extensionReasons) {
        this.extensionReasons = extensionReasons;
    }
    
    public String getExtensionReasonsAsHTML() {
        return org.yeastrc.utils.HTML.convertToHTML(this.extensionReasons);
    }

	public RateType getRateType() throws SQLException
	{
		if(this instanceof BilledProject) {
			return RateTypeDAO.getInstance().getRateForBilledProjects(this);
		}
		else if(this instanceof Collaboration) {
			return RateTypeDAO.getInstance().getRateForUwprSupportedProjects();
		}
		return null;
	}

}