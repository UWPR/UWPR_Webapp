package org.uwpr.www;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsage;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsageUtils;
import org.uwpr.instrumentlog.rawfile.ProjectUsageUpdateErrorEmailer;
import org.uwpr.instrumentlog.rawfile.RawFileUsageParser;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ScheduledTask implements ServletContextListener {

    private static final Logger log = LogManager.getLogger(ScheduledTask.class);
    
    private volatile boolean notiferRunning = false;
    private Timer timer;

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
                log.error("No value found for property: file.location.rawcounts. Skipping parsing raw file usage data...");
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
    }

}
