/**
 * SaveCollaborationExtension.java
 * @author Vagisha Sharma
 * Apr 8, 2009
 * @version 1.0
 */
package org.yeastrc.www.project;

import org.apache.log4j.Logger;
import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Collaboration;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;
import org.yeastrc.project.Technology;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 */
public class SaveCollaborationExtensionAction extends Action {

 private static final Logger log = Logger.getLogger(SaveNewCollaborationAction.class);
    
    public ActionForward execute( ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response )
    throws Exception {
        
        // The form elements we're after
        int parentProjectId;
        String title = null;
        int pi = 0;
        String[] groups = null;
        String[] fundingTypes = null;
        String[] federalFundingTypes = null;
        String projectAbstract = null;
        String publicAbstract = null;
        String scientificQuestion = null;
        String progress = null;
        //String keywords = null;
        String publications = null;
        String comments;
        String instrumentTimeExpl;
        boolean sendEmail;
        boolean isTech;
        String grantNumber = null;
        String grantAmount = null;
        String foundationName = null;

        // UWPR stuff
        int ltqRunsRequested = 0;
        int ltq_etdRunsRequested = 0;
        int ltq_orbitrapRunsRequested = 0;
        int ltq_ftRunsRequested = 0;
        int tsq_accessRunsRequested = 0;
        int tsq_vantageRunsRequested = 0;
        
        String[] fragmentationTypes = null;
        
        boolean databaseSearchRequested = false;
        boolean massSpecExpertiseRequested = true;
        
        // !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
        String extensionReasons;
        
        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.login.notloggedin"));
            saveErrors( request, errors );
            return mapping.findForward("authenticate");
        }


        // We're saving!
        parentProjectId = ((EditCollaborationForm)form).getParentProjectID();
        title = ((EditCollaborationForm)(form)).getTitle();
        pi = ((EditCollaborationForm)(form)).getPI();
        groups = ((EditCollaborationForm)(form)).getGroups();
        fundingTypes = ((EditCollaborationForm)(form)).getFundingTypes();
        federalFundingTypes = ((EditCollaborationForm)(form)).getFederalFundingTypes();
        projectAbstract = ((EditCollaborationForm)(form)).getAbstract();
        publicAbstract = ((EditCollaborationForm)(form)).getPublicAbstract();
        scientificQuestion = ((EditCollaborationForm)(form)).getScientificQuestion();
        //keywords = ((EditCollaborationForm)(form)).getKeywords();
        progress = ((EditCollaborationForm)(form)).getProgress();
        publications = ((EditCollaborationForm)(form)).getPublications();
        comments = ((EditCollaborationForm)(form)).getComments();
        instrumentTimeExpl = ((EditCollaborationForm)(form)).getInstrumentTimeExpl();
        sendEmail = ((EditCollaborationForm)(form)).getSendEmail();
        isTech = ((EditCollaborationForm)(form)).getIsTech();
        foundationName = ((EditCollaborationForm)(form)).getFoundationName();
        grantNumber = ((EditCollaborationForm)(form)).getGrantNumber();
        grantAmount = ((EditCollaborationForm)(form)).getGrantAmount();
        
        ltqRunsRequested = ((EditCollaborationForm)(form)).getLtqRunsRequested();
        ltq_etdRunsRequested = ((EditCollaborationForm)(form)).getLtq_etdRunsRequested();
        ltq_orbitrapRunsRequested = ((EditCollaborationForm)(form)).getLtq_orbitrapRunsRequested();
        ltq_ftRunsRequested = ((EditCollaborationForm)(form)).getLtq_ftRunsRequested();
        tsq_accessRunsRequested = ((EditCollaborationForm)(form)).getTsq_accessRunsRequested();
        tsq_vantageRunsRequested = ((EditCollaborationForm)(form)).getTsq_vantageRunsRequested();
        fragmentationTypes = ((EditCollaborationForm)(form)).getFragmentationTypes();
        databaseSearchRequested = ((EditCollaborationForm)(form)).isDatabaseSearchRequested();
        massSpecExpertiseRequested = ((EditCollaborationForm)(form)).isMassSpecExpertiseRequested();
        
        // !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
        extensionReasons = ((EditCollaborationForm)(form)).getExtensionReasons();
        
        // Set blank items to null
        if (title.equals("")) title = null;
        if (projectAbstract.equals("")) projectAbstract = null;
        //if (keywords.equals("")) keywords = null;
        //if (progress.equals("")) progress = null;
        //if (publications.equals("")) publications = null;
        if (comments.equals("")) comments = null;
        if (instrumentTimeExpl.equals("")) instrumentTimeExpl = null;

        // Load our project
        Project project;
        if (isTech){
            project = new Technology();
        } else {
            project = new Collaboration();
        }

        // If this is an extension project, the parentProjectID should be > 0
        if(parentProjectId > 0)
            project.setParentProjectID(parentProjectId);
        
        
        // Set this project in the request, as a bean to be displayed on the view
        //request.setAttribute("project", project);
        
        // Set up our researchers
        Researcher oPI = null;
        try
        {
            if (pi != 0) {
                oPI = new Researcher();
                oPI.load(pi);
            }
            List<Researcher> rList = ((EditProjectForm)(form)).getResearcherList();
            List<Researcher> projResearchers = new ArrayList<Researcher>();
            for(Researcher r: rList) {
                if(r != null && r.getID() > 0) {
                    r.load(r.getID());
                    projResearchers.add(r);
                }
            }
            project.setResearchers(projResearchers);
        }
        catch (InvalidIDException iie) {

            // Couldn't load the researcher.
            ActionErrors errors = new ActionErrors();
            errors.add("project", new ActionMessage("error.project.invalidresearcher"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }


        // Set up the funding types
        project.clearFundingTypes();
        
        if (fundingTypes != null) {
            if (fundingTypes.length > 0) {
                for (int i = 0; i < fundingTypes.length; i++) {
                    project.setFundingType(fundingTypes[i]);
                }
            }
        }
        
        // Set up the federal funding types
        project.clearFederalFundingTypes();
        
        if (federalFundingTypes != null) {
            if (federalFundingTypes.length > 0) {
                for (int i = 0; i < federalFundingTypes.length; i++) {
                    project.setFederalFundingType(federalFundingTypes[i]);
                }
            }
        }

        // Set up the groups
        project.clearGroups();
        
        if (groups != null) {
            if (groups.length > 0) {
                for (int i = 0; i < groups.length; i++) {
                    try { project.setGroup(groups[i]); }
                    catch (InvalidIDException iie) {
                    
                        // Somehow got an invalid group...
                        ActionErrors errors = new ActionErrors();
                        errors.add("project", new ActionMessage("error.project.invalidgroup"));
                        saveErrors( request, errors );
                        return mapping.findForward("Failure");                  
                    }
                }
            }
        }

        // Set all of the new values in the project
        project.setTitle(title);
        project.setPI(oPI);
        project.setAbstract(projectAbstract);
        project.setScientificQuestion(scientificQuestion);
        project.setPublicAbstract(publicAbstract);
        //project.setKeywords(keywords);
        project.setProgress(progress);
        project.setPublications(publications);
        project.setComments(comments);
        project.setInstrumentTimeExpl(instrumentTimeExpl);
        project.setGrantAmount( grantAmount );
        project.setGrantNumber( grantNumber );
        project.setFoundationName( foundationName );
        
        project.setLtqRunsRequested(ltqRunsRequested);
        project.setLtq_etdRunsRequested(ltq_etdRunsRequested);
        project.setLtq_orbitrapRunsRequested(ltq_orbitrapRunsRequested);
        project.setLtq_ftRunsRequested(ltq_ftRunsRequested);
        project.setTsq_accessRunsRequested(tsq_accessRunsRequested);
        project.setTsq_vantageRunsRequested(tsq_vantageRunsRequested);
        project.setDatabaseSearchRequested(databaseSearchRequested);
        project.setMassSpecExpertiseRequested(massSpecExpertiseRequested);
        
        if (fragmentationTypes != null) {
            for ( String fragmentationType : fragmentationTypes ) {
                if (fragmentationType.equals( "CID" ) )
                    project.setCidFragmentation( true );
                else if (fragmentationType.equals( "PQD" ) )
                    project.setPqdFragmentation( true );
                else if (fragmentationType.equals( "HCD" ) )
                    project.setHcdFragmentation( true );
                else if (fragmentationType.equals( "ETD" ) )
                    project.setEtdFragmentation( true );
                else if (fragmentationType.equals( "ECD" ) )
                    project.setEcdFragmentation( true );
                else if (fragmentationType.equals( "IRMPD" ) )
                    project.setIrmpdFragmentation( true );
            }
        }
        
        // !!!!!!!!!!!!!!!!!!!!! PROJECT EXTENSION REASON !!!!!!!!!!!!!!!!!!!!!!!
        project.setExtensionReasons(extensionReasons);
        
        
        // Send email to the groups they're collaboration with
        if (sendEmail) {
            try {
                // set the SMTP host property value
                Properties properties = System.getProperties();
                properties.put("mail.smtp.host", "localhost");
            
                // create a JavaMail session
                javax.mail.Session mSession = javax.mail.Session.getInstance(properties, null);
            
                // create a new MIME message
                MimeMessage message = new MimeMessage(mSession);
            
                // set the from address
                Address fromAddress = new InternetAddress(((Researcher)(user.getResearcher())).getEmail());
                message.setFrom(fromAddress);
            
                // set the to address by assembling a comma delimited list of addresses associated with the groups selected
                String emailStr = "";
                MessageResources mr = getResources(request);
                for (int i = 0; i < groups.length; i++) {
                    if (i > 0) { emailStr = emailStr + ","; }
                    
                    emailStr = emailStr + mr.getMessage("email.groups." + groups[i]);
                }
                
                Address[] toAddress = InternetAddress.parse(emailStr);
                message.setRecipients(Message.RecipientType.TO, toAddress);
            
                // set the subject
                message.setSubject("New Collaboration Request");
            
                // set the message body
                String text = ((Researcher)(user.getResearcher())).getFirstName() + " ";
                text += ((Researcher)(user.getResearcher())).getLastName() + " ";
                text += "has requested a new collaboration with your group.  Replying to this email should reply directly to the researcher.\n\n";
                text += "Details:\n\n";
                
                if (oPI != null)
                    text += "PI: " + oPI.getListing() + "\n\n";
    
                //text += "Groups: " + project.getGroupsString() + "\n\n";
                text += "Title: " + project.getTitle() + "\n\n";
                text += "Abstract: " + project.getAbstract() + "\n\n";
                text += "Scientific Question: " + project.getScientificQuestion() + "\n\n";

                text += "MS Runs requested:\n";
                text += "\tLTQ:\t" + ltqRunsRequested + "\n";
                text += "\tLTQ-ETD:\t" + ltq_etdRunsRequested + "\n";
                text += "\tLTQ-Orbitrap:\t" + ltq_orbitrapRunsRequested + "\n";
                text += "\tLTQ-FT:\t" + ltq_ftRunsRequested + "\n";
                text += "\tTSQ-Access:\t" + tsq_accessRunsRequested + "\n";
                text += "\tTSQ-Vantage:\t" + tsq_vantageRunsRequested + "\n\n";
                
                text += "Fragmentation type(s) requested: ";
                if (fragmentationTypes == null) 
                    text+= "\n\n";
                else
                    text += org.apache.commons.lang.StringUtils.join( fragmentationTypes, ", " ) + "\n\n";

                text += "Database searched at UWPR: " + databaseSearchRequested + "\n\n";               
                
                text += "Comments: " + project.getComments() + "\n\n";
            
                message.setText(text);
            
                // send the message
                Transport.send(message);
            
            }
            catch (Exception e) { log.error("Error sending email to collaboration group.", e); }
        }
        
        
        // Save the project
        project.save();
        
        // ASSIGN reviewer(s)
        if (isTech){
            // We don't have tech project for UWPR.  isTech should never be true.
        } else {
            // Assign a reviewer to the project
            try {
                ReviewerAssignmentUtils.assignReviewer((Collaboration) project);
            }
            catch(ReviewerAssignmentException e) {
                log.error("ERROR assigning reviewers to project: "+project.getID()+"\n", e);
                // could not assign reviewers to project
                /*ActionErrors errors = new ActionErrors();
                errors.add("project", new ActionMessage("error.project.noreviewer"));
                saveErrors( request, errors );
                return mapping.findForward("Failure");*/ 
            }
        }
        
        
        // Send signup confirmation to researcher
        NewProjectUtils.sendEmailConfirmation(user.getResearcher(), project, getResources(request));

        if (isTech){
            // We don't have tech project for UWPR.  isTech should never be true.
        } else {
            // Send an email to the reviewer
            try {
                ReviewerEmailUtils.emailReviewer((Collaboration) project);
            }
            catch (Exception e) {
                log.error("Error emailing reviewers for project: "+project.getID(), e);
            }
        }
        
        
        // Go!
        ActionForward success = mapping.findForward("Success") ;
        success = new ActionForward( success.getPath() + "?ID=" + project.getID(), success.getRedirect() ) ;
        return success ;
    }
}
