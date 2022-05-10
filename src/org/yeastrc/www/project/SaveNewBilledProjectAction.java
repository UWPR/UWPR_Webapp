/*
 *
 * Created on February 5, 2004
 *
 * Created by Michael Riffle <mriffle@u.washington.edu>
 *
 */

package org.yeastrc.www.project;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;
import org.yeastrc.data.InvalidIDException;
import org.yeastrc.project.Affiliation;
import org.yeastrc.project.BilledProject;
import org.yeastrc.project.Project;
import org.yeastrc.project.Researcher;
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
 * Controller class for saving a new billed project.
 */
public class SaveNewBilledProjectAction extends Action {

    private static final Logger log = LogManager.getLogger(SaveNewBilledProjectAction.class);
    
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
		String scientificQuestion = null;
		String progress = null;
		String publications = null;
		String comments;

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
		
		Affiliation affiliation = null;
		
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}


		// We're saving!
		parentProjectId = ((EditBilledProjectForm)form).getParentProjectID();
		title = ((EditBilledProjectForm)(form)).getTitle();
		pi = ((EditBilledProjectForm)(form)).getPI();
		groups = ((EditBilledProjectForm)(form)).getGroups();
		fundingTypes = ((EditBilledProjectForm)(form)).getFundingTypes();
		federalFundingTypes = ((EditBilledProjectForm)(form)).getFederalFundingTypes();
		projectAbstract = ((EditBilledProjectForm)(form)).getAbstract();
		scientificQuestion = ((EditBilledProjectForm)(form)).getScientificQuestion();
		progress = ((EditBilledProjectForm)(form)).getProgress();
		publications = ((EditBilledProjectForm)(form)).getPublications();
		comments = ((EditBilledProjectForm)(form)).getComments();
		
		ltqRunsRequested = ((EditBilledProjectForm)(form)).getLtqRunsRequested();
		ltq_etdRunsRequested = ((EditBilledProjectForm)(form)).getLtq_etdRunsRequested();
		ltq_orbitrapRunsRequested = ((EditBilledProjectForm)(form)).getLtq_orbitrapRunsRequested();
		ltq_ftRunsRequested = ((EditBilledProjectForm)(form)).getLtq_ftRunsRequested();
		tsq_accessRunsRequested = ((EditBilledProjectForm)(form)).getTsq_accessRunsRequested();
	    tsq_vantageRunsRequested = ((EditBilledProjectForm)(form)).getTsq_vantageRunsRequested();
		fragmentationTypes = ((EditBilledProjectForm)(form)).getFragmentationTypes();
		databaseSearchRequested = ((EditBilledProjectForm)(form)).isDatabaseSearchRequested();
		massSpecExpertiseRequested = ((EditBilledProjectForm)(form)).isMassSpecExpertiseRequested();
		
		affiliation = ((EditBilledProjectForm)form).getAffiliation();
		
		// Set blank items to null
		if (title.equals("")) title = null;
		if (projectAbstract.equals("")) projectAbstract = null;
		if (comments.equals("")) comments = null;

		// Load our project
		Project project = new BilledProject();
		

		// If this is an extension project, the parentProjectID should be > 0
		if(parentProjectId > 0)
		    project.setParentProjectID(parentProjectId);
		
		
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
		project.setProgress(progress);
		project.setPublications(publications);
		project.setComments(comments);
		
		project.setLtqRunsRequested(ltqRunsRequested);
		project.setLtq_etdRunsRequested(ltq_etdRunsRequested);
		project.setLtq_orbitrapRunsRequested(ltq_orbitrapRunsRequested);
		project.setLtq_ftRunsRequested(ltq_ftRunsRequested);
		project.setTsq_accessRunsRequested(tsq_accessRunsRequested);
		project.setTsq_vantageRunsRequested(tsq_vantageRunsRequested);
		project.setDatabaseSearchRequested(databaseSearchRequested);
		if(affiliation != Affiliation.UW) {
			// project.setMassSpecExpertiseRequested(massSpecExpertiseRequested);
			// This should be set correctly in the form, but just in case...
			project.setMassSpecExpertiseRequested(true);
		}
		else {
			project.setMassSpecExpertiseRequested(massSpecExpertiseRequested);
		}
		
		
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
		
		((BilledProject)project).setAffiliation(affiliation);
		
        // Save the project
        project.save();
		
		// Send email to the groups they're collaboration with
		if (project.getGroups() != null && project.getGroups().size() > 0) {
			emailCollaborators(request, (BilledProject) project, user);
		}
		
		// Send signup confirmation to researcher
		// NewProjectUtils.sendEmailConfirmation(user.getResearcher(), project, getResources(request));

		
		// Go!
		ActionForward success = mapping.findForward("Success") ;
		success = new ActionForward( success.getPath() + "?ID=" + project.getID(), success.getRedirect() ) ;
		return success ;
	}

    private void emailCollaborators(HttpServletRequest request, BilledProject billedProject, User user) {
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
        	String[] groups = billedProject.getGroupsArray();
        	for (int i = 0; i < groups.length; i++) {
        		if (i > 0) { emailStr = emailStr + ","; }
        		
        		emailStr = emailStr + mr.getMessage("email.groups." + groups[i]);
        	}
        	
        	Address[] toAddress = InternetAddress.parse(emailStr);
        	//System.out.println(emailStr);
        	message.setRecipients(Message.RecipientType.TO, toAddress);
        
        	// set the subject
        	message.setSubject("UWPR - New Collaboration (Billable Project) Request");
        
        	// set the message body
        	String text = ((Researcher)(user.getResearcher())).getFirstName() + " ";
        	text += ((Researcher)(user.getResearcher())).getLastName() + " ";
        	text += "has requested a new collaboration with your group.  Replying to this email should reply directly to the researcher.\n\n";
        	text += "Details:\n\n";
        	

        	
        	if (billedProject.getPI() != null)
        		text += "PI: " + billedProject.getPI().getListing() + "\n\n";

        	text += "Title: " + billedProject.getTitle() + "\n\n";
        	text += "Abstract: " + billedProject.getAbstract() + "\n\n";
        	text += "Scientific Question: " + billedProject.getScientificQuestion() + "\n\n";
        	text += "Database searched at UWPR: " + billedProject.isDatabaseSearchRequested() + "\n\n";
        	text += "Mass Spec. analysis by UWPR personnel: " + billedProject.isMassSpecExpertiseRequested() + "\n\n";
        	
        	text += "Comments: " + billedProject.getComments() + "\n\n";
        
        	//System.out.println(text);
        	
        	message.setText(text);
        
        	// send the message
        	Transport.send(message);
        
        }
        catch (Exception e) { log.error("Error sending email to collaboration group.", e); }
    }
	
}