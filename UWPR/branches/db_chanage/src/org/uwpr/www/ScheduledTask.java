package org.uwpr.www;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.uwpr.instrumentlog.DateUtils;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsage;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsageUtils;
import org.uwpr.instrumentlog.rawfile.ProjectUsageUpdateErrorEmailer;
import org.uwpr.instrumentlog.rawfile.RawFileUsageParser;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.db.DBConnectionManager;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.ExemptResearchers;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectReviewer;
import org.yeastrc.project.ProjectReviewerDAO;
import org.yeastrc.project.ProjectsSearcher;
import org.yeastrc.project.Researcher;
import org.yeastrc.www.project.CollaborationStatusChangeEmailer;
import org.yeastrc.www.project.ReviewerEmailUtils;

public class ScheduledTask implements ServletContextListener {

    private static final Logger log = Logger.getLogger(ScheduledTask.class);
    
    private volatile boolean notiferRunning = false;
    private Timer timer;

    private boolean reviewReminderSent = false;
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        timer.cancel();
        while(notiferRunning) {
            try {
                log.debug("Wating for ProjectStatusChecker to finish");
                wait(1000);
            }
            catch (InterruptedException e) {
                log.error("", e);
                break;
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("INITIALIZED");
        timer =  new Timer();
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR, 1);
        date.set(Calendar.MINUTE, 00);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.AM_PM, Calendar.AM);
        
       
        // Schedule to run once a day
        timer.schedule(
          new ScheduledTimerTask(sce.getServletContext()),
          date.getTime(),
          1000 * 60 * 60 * 24 // * 7
        );
        
    }
    
    final class ScheduledTimerTask extends TimerTask{
     
        private final ServletContext context;
        
        public ScheduledTimerTask(ServletContext context) {
            this.context = context;
        }
        
        @Override
        public void run() {
            notiferRunning = true;
            log.info("RUNNING ScheduledTask ........");
            
            
            // Update project usage (# raw files, disk space used etc.)
            updateProjectUsage();
            
            // update project status and send emails
            updateProjectStatusAndEmail();
            
            // send reminders to reviewers for pending reviews
            sendPendingReviewReminder();
            
            log.info("ScheduledTask done.........");
            notiferRunning = false;
            
        }
        
        private void updateProjectUsage() {
        	
        	
        	// look for the appropriate  property  in scheduledTask.properties
            InputStream is = context.getResourceAsStream("/WEB-INF/classes/scheduledTask.properties");
            if(is == null)
                return;
            Properties props = new Properties();
            try {
                props.load(is);
            }
            catch (IOException e) {
                log.info("Error reading scheduledTask.properties file", e);
                return;
            }
            try {is.close();} catch (IOException e){}
            
            String filePath = props.getProperty("file.location.rawcounts");
            if(filePath == null || filePath.trim().length() == 0) {
                log.error("No value found for property: file.location.rawcounts");
                ProjectUsageUpdateErrorEmailer.getInstance().sendEmail("Could not find path to rawcounts file.  Missing property file.location.rawcounts");
                return;
            }
            
            log.info("Parsing file: "+filePath);
            // String filePath = "/var/www/html/internal/rawcount.tsv";
            List<ProjectRawFileUsage> usageList = null;
            
            try {
                usageList = RawFileUsageParser.instance().parse(filePath);
            }
            catch (Exception e) {
            	
            	ProjectUsageUpdateErrorEmailer.getInstance().sendEmail(e.getMessage());
                log.error("Error parsing file: "+filePath, e);
            }
            
            if(usageList == null)    return;
            
            try {
                ProjectRawFileUsageUtils.instance().saveUsage(usageList);
            }
            catch (Exception e) {
               log.error("Error updating raw file usage", e);
            }
            
        }
        
        private void updateProjectStatusAndEmail() {
            ProjectsSearcher ps = new ProjectsSearcher();
            ps.addStatusType(CollaborationStatus.ACCEPTED);
            ps.addStatusType(CollaborationStatus.COMPLETE);
            ps.addStatusType(CollaborationStatus.EXPIRED);
            
            List<Project> projects = null;
            try {
                projects = ps.search();
            }
            catch (SQLException e) {
                log.error("!!!Error searching for projects", e);
                return;
            }
            
            // remove projects exempt from progress reports etc.
            projects = removeExempt(projects);
            
            // update project status if required
            updateProjectStatus(projects);
            
            // send progress report overdue reminders
            try {
                sendReminderForOverdueProjects(projects);
            }
            catch (SQLException e) {
                log.error("Could not send reminders for overdue projects");
            }
        }
        
        private List<Project> removeExempt(List<Project> projects) {
            Iterator<Project> iter = projects.iterator();
            while(iter.hasNext()) {
                Project proj = iter.next();
                Researcher pi = proj.getPI();
                if(pi == null) {
                    log.error("PROJECT PI IS NULL: "+proj.getID());
                }
                if(ExemptResearchers.contains(pi.getID()))
                    iter.remove();
            }
            return projects;
        }

        private void sendReminderForOverdueProjects(List<Project> projects) throws SQLException {
            
            log.info("SENDING OVERDUE REPORT REMINDERS");
            if(projects != null && projects.size() > 0) {
                Iterator<Project> iter = projects.iterator();
                while(iter.hasNext()) {
                    Project proj = iter.next();
                    if(proj instanceof Collaboration) {
                        Collaboration c = (Collaboration) proj;
                        
                        if(c.isPending() || c.isRejected())
                            continue;
                        
                        if(c.getDateAccepted() == null)
                            continue;
                        
                        // if we already have a valid report return false
                        if(c.hasValidProgressReport())
                            continue;
                        
                        
                        // calculate the number of days since this project was accepted
                        java.util.Date date = new java.util.Date();
                        int numDays = (int) ((date.getTime() - c.getDateAccepted().getTime()) / (1000*60*60*24));
                        // If this project has been in the ACCEPTED/COMPLETED state for > 300 days send a reminder
                        if(numDays > 300) {
                            
                            // if a reminder has already been sent for this project
                            // don't send another one
                            if(reminderSent(c))
                                continue;
                            
                            else {
                                log.info("Project "+c.getID()+" has overdue report. Sending reminder.");
                                CollaborationStatusChangeEmailer.sendReportReminderEmail(c);
                                setReminderSent(c);
                            }
                        }
                    }
                }
            }
        }
        
        private void setReminderSent(Collaboration c) throws SQLException {
         // Get our connection to the database.
            Connection conn = DBConnectionManager.getConnection("pr");
            Statement stmt = null;
            
            try {
                stmt = conn.createStatement();
                String sqlStr = "INSERT INTO projectReportReminder (projectID) VALUES ("+c.getID()+")";
                stmt.executeUpdate(sqlStr);
                
            }
            catch(SQLException e) { 
                log.error("Could not create entry in table projectReportReminder for projectID: "+c.getID(), e);
                throw e;
            }
            finally {

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

        private boolean reminderSent(Collaboration c) throws SQLException {
            
            // Get our connection to the database.
            Connection conn = DBConnectionManager.getConnection("pr");
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                
                String sqlStr = "SELECT * FROM projectReportReminder WHERE projectID = " + c.getID();
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sqlStr);
                
                if(rs.next())   return true;
                else            return false;
                
            }
            catch(SQLException e) { 
                log.error("Could not query projectReportReminder table for projectID: "+c.getID(), e);
                throw e;
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
        }

        private void updateProjectStatus(List<Project> projects) {
            markProjectsComplete(projects);
            expireProjects(projects);
        }

        private void markProjectsComplete(List<Project> projects) {
            
            log.info("MARKING PROJECTS COMPLETE");
            for(Project project: projects) {
                if(project instanceof Collaboration) {
                    Collaboration c = (Collaboration) project;
                    
                    // The terminal status of a project is either COMPLETE or EXPIRED
                    // if the project is already in a terminal state continue;
                    if(!(c.getCollaborationStatus() == CollaborationStatus.ACCEPTED))
                        continue;
                    
                    // get the number of runs requested
                    int numRequested = c.getTotalRunsRequested();
                    if(numRequested == 0)
                        continue; 
                    
                    // get the number of raw files generated 
                    ProjectRawFileUsage rawFileUsage = null;
                    try {
                        rawFileUsage = ProjectRawFileUsageUtils.instance().loadUsage(project.getID());
                    }
                    catch (SQLException e) {
                        log.error("Error looking up raw file usage for project: "+project.getID(), e);
                        continue;
                    }
                    if(rawFileUsage == null)
                        rawFileUsage = new ProjectRawFileUsage();
                    int numRawFiles = rawFileUsage.getRawFileCount();
                    if(numRawFiles == 0)
                        continue;
                    
                    // mark projects complete if num_raw files >= num_requested + 2*sqrt(num_requested)
                    boolean exceeded = numRawFiles >= numRequested + 2 * Math.sqrt(numRequested);
                    
                    
                    if(exceeded){
                        
                        log.info("Project "+c.getID()+" will be marked COMPLETE");
                        c.setCollaborationStatus(CollaborationStatus.COMPLETE);
                        try {
                            c.save();
                            // send email to researchers that their project has been maeked as complete
                            CollaborationStatusChangeEmailer.sendCollaborationCompletedEmail(c);
                        }
                        catch (InvalidIDException e) {
                            log.error("Error updating project status to COMPLETE", e);
                        }
                        catch (SQLException e) {
                            log.error("Error updating project status to COMPLETE", e);
                        }
                    }
                }
            }
        }
        
        private void expireProjects(List<Project> projects) {
            
            log.info("MARKING PROJECTS EXPIRED");
            Date today = new Date();
            for(Project project: projects) {
                if(project instanceof Collaboration) {
                    Collaboration c = (Collaboration) project;
                    
                    // The terminal status of a project is either COMPLETE or EXPIRED
                    // if the project is already in a terminal state continue;
                    if(!(c.getCollaborationStatus() == CollaborationStatus.ACCEPTED))
                        continue;
                    
                    Date dateAccepted = c.getDateAccepted();
                    // don't know what to do with a null date
                    if(dateAccepted == null)
                        continue;
                    
                    
                    if(expireProject(dateAccepted, today)) {
                    
                    // calculate the number of days since this project was accepted.
                    // int numDaysAccepted = (int) ((today.getTime() - dateAccepted.getTime()) / (1000*60*60*24));
                    
                    // If the project has been accepted for a year and not been marked as COMPLETE
                    // set the status to expired
                    // if(numDaysAccepted > 365){
                        
                        log.info("Project "+c.getID()+" will be marked EXPIRED");
                        c.setCollaborationStatus(CollaborationStatus.EXPIRED);
                        try {
                            c.save();
                            // send email to researchers that their project has expired
                            CollaborationStatusChangeEmailer.sendCollaborationExpiredEmail(c);
                        }
                        catch (InvalidIDException e) {
                            log.error("Error updating project status to EXPIRED", e);
                        }
                        catch (SQLException e) {
                            log.error("Error updating project status to EXPIRED", e);
                        }
                    }
                }
            }
        }
    
        final boolean expireProject(Date dateAccepted, Date today) {
        	
        	Calendar calendar = Calendar.getInstance();
        	calendar.setTime(dateAccepted);
            calendar.add(Calendar.YEAR, 1); // add a year to the accepted date
            Date expiryDate = calendar.getTime();
            return !(expiryDate.after(today));
		}

		private void sendPendingReviewReminder() {

            
            // this should only run on Mondays
            if(DateUtils.getCurrentDay() != Calendar.MONDAY) {
                log.info("Not the day to send review reminders!");
                reviewReminderSent = false;
                return;
            }
            // If this is running more than once a day -- don't sent reminders again.
            // Should never really happen.....
            else if(reviewReminderSent){
                log.info("Review reminders have already been sent today");
                return;
            }
            
            
            // look for the appropriate  property  in scheduledTask.properties
            InputStream is = context.getResourceAsStream("/WEB-INF/classes/scheduledTask.properties");
            if(is == null)
                return;
            Properties props = new Properties();
            try {
                props.load(is);
            }
            catch (IOException e) {
                log.info("Error reading scheduledTask.properties file", e);
                return;
            }
            try {is.close();} catch (IOException e){}
            
            String val = props.getProperty("email.reminder.send");
            if(val != null && !Boolean.valueOf(val)) {
                log.info("email.reminder.send is set to FALSE");
                return;
            }
            
            
            log.info("Looking for pending projects to send reminder emails to reviewers");
            
            // get a list of pending projects
            ProjectsSearcher ps = new ProjectsSearcher();
            ps.addStatusType(CollaborationStatus.PENDING);
            ps.addStatusType(CollaborationStatus.REVISE_PENDING);
            
            List<Project> projects = null;
            try {
                projects = ps.search();
            }
            catch (SQLException e) {
                log.error("!!!Error searching for pending projects", e);
                return;
            }
            
            if(projects.size() == 0)
                return;
            
            
            ProjectReviewerDAO revDao = ProjectReviewerDAO.instance();
            
            Date current = new Date();
            long DAY = 1000 * 60 * 60 * 24;
            
            for(Project project: projects) {
                
            	if(!(project instanceof Collaboration))
            		continue;
            	
                // if it has been less than 7 days since this project was submitted skip over it
                Date submitted = ((Collaboration)project).getSubmitDate();
                if(((current.getTime() - submitted.getTime()) / DAY) < 7) {
                    log.info("Project "+project.getID()+" was submitted less than 7 days ago ... skipping");
                    continue;
                }
                
                // get the reviewers for the project
                List<ProjectReviewer> reviewers = null;
                try {
                    reviewers = revDao.getProjectReviewers(project.getID());
                }
                catch (SQLException e) {
                    log.error("!!!Error getting reviewers for projects", e);
                }
                
                if(reviewers != null) {
                	// The project could be in pending status if there is a review conflict
                	if(isConflictStatus(reviewers)) {
                		ReviewerEmailUtils.sendReviewConflictReminderEmail((Collaboration)project, reviewers);
                	}
                	// If there is not review conflict it means one or more reviewers have not 
                	// submitted their review.
                	else {
                		for(ProjectReviewer reviewer: reviewers) {
                			if(!reviewer.isReviewSubmitted()) {
                				ReviewerEmailUtils.sendReviewReminderEmail((Collaboration)project, reviewer);
                			}
                		}
                	}
                }
            }
            
            reviewReminderSent = true;
        }

		private boolean isConflictStatus(List<ProjectReviewer> reviewers) {
			CollaborationStatus status = null;
			for(ProjectReviewer reviewer: reviewers) {
    			if(!reviewer.isReviewSubmitted()) 
    				return false;
    			if(status == null)
    				status = reviewer.getRecommendedStatus();
    			
    			if(status != reviewer.getRecommendedStatus())
    				return true;
    		}
			return false;
		}
    }

}
