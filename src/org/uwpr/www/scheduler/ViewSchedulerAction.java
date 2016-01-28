/**
 * ViewScheduler.java
 * @author Vagisha Sharma
 * May 23, 2011
 */
package org.uwpr.www.scheduler;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.costcenter.InstrumentRate;
import org.uwpr.costcenter.InstrumentRateDAO;
import org.uwpr.costcenter.RateType;
import org.uwpr.costcenter.RateTypeDAO;
import org.uwpr.costcenter.TimeBlock;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.scheduler.ProjectInstrumentTimeApprover;
import org.yeastrc.project.*;
import org.yeastrc.project.payment.PaymentMethod;
import org.yeastrc.project.payment.ProjectPaymentMethodDAO;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class ViewSchedulerAction extends Action {

	private static final Logger log = Logger.getLogger(ViewSchedulerAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}
		
        
        // we need a projectID
        int projectId = 0;
        try {
        	projectId = Integer.parseInt(request.getParameter("projectId"));
        }
        catch(NumberFormatException e) {
        	projectId = 0;
        }
        if(projectId == 0) {
        	ActionErrors errors = new ActionErrors();
            errors.add("scheduler", new ActionMessage("error.scheduler.invalidid", "Invalid projectID in request"));
            saveErrors( request, errors );
            return mapping.findForward("standardHome");
        }
        
        // we need an instrumentID
        int instrumentId = 0;
        try {
        	instrumentId = Integer.parseInt(request.getParameter("instrumentId"));
        }
        catch(NumberFormatException e) {
        	instrumentId = 0;
        }
        // get a list of ms instruments
        List <MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
        Collections.sort(instruments, new Comparator<MsInstrument>() {
            public int compare(MsInstrument o1, MsInstrument o2) {
                return o1.getID() > o2.getID() ? 1 : (o1.getID() == o2.getID() ? 0 : -1);
        }});
        
        MsInstrument instrument = null;
        for(MsInstrument instr: instruments) {
        	if(instr.getID() == instrumentId) {
        		instrument = instr;
        		break;
        	}
        }
        if(instrument == null) {
        	log.error("No instrument found for ID: "+instrumentId);
        	// select the first "active" instrument
        	for(MsInstrument instr: instruments) {
        		if(!instr.isActive())
        			continue;
        		instrumentId = instr.getID();
        		instrument = instr;
        		break;
        	}
        }
        
        // Make sure the user has access to the project
        Project project = null;
        try {
        	project = ProjectFactory.getProject(projectId);
        	if(!project.checkAccess(user.getResearcher())) {
        		ActionErrors errors = new ActionErrors();
        		errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess","User does not have access to schedule instrument time for project."));
        		saveErrors( request, errors );
        		ActionForward fwd = mapping.findForward("Failure");
    			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
            	return newFwd;

        	}
        }
        catch(Exception e) {
        	ActionErrors errors = new ActionErrors();
			errors.add("scheduler", new ActionMessage("error.scheduler.load","Error loading project to check access."));
			saveErrors( request, errors );
			log.error("Error checking access to project ID: "+projectId, e);
			ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        	return newFwd;

        }
        
        // If this is a UWPR supported project non-admin users should not be able to schedule time. They have to go through Priska
        Groups groupMan = Groups.getInstance();
    	
        if(project instanceof Collaboration) {
        	if(!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
        		ActionErrors errors = new ActionErrors();
        		errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess","User does not have access to schedule instrument time for project."));
        		saveErrors( request, errors );
        		ActionForward fwd = mapping.findForward("Failure");
    			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
            	return newFwd;
        	}
        }
        
        // If this a Fee for Service project non-admin users should not be able to schedule time, since they are not the
        // ones running the mass-spec
        if(project.isMassSpecExpertiseRequested())
        {
        	if(!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
        		ActionErrors errors = new ActionErrors();
        		errors.add("scheduler", new ActionMessage("error.scheduler.invalidaccess","Users cannot schedule instrument time for Fee-for-Service projects."));
        		saveErrors( request, errors );
        		ActionForward fwd = mapping.findForward("Failure");
    			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
            	return newFwd;
        	}
        }
        
        
        // Get the Time blocks the user will be able to select from
        RateType rateType = null;
        if(project instanceof BilledProject) {
        	Affiliation affiliation = ((BilledProject)project).getAffiliation();
        	rateType = RateTypeDAO.getInstance().getRateTypeForAffiliation(affiliation, project.isMassSpecExpertiseRequested());
        }
        else if(project instanceof Collaboration) {
        	rateType = RateTypeDAO.getInstance().getRateForUwprSupportedProjects();
        }
        if(rateType == null) {
        	
        	ActionErrors errors = new ActionErrors();
			errors.add("scheduler", new ActionMessage("error.scheduler.load","Error getting rate type."));
			saveErrors( request, errors );
			ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        	return newFwd;
        }
        List<InstrumentRate> rates = InstrumentRateDAO.getInstance().getInstrumentCurrentRates(instrumentId, rateType.getId());
        List<TimeBlock> timeBlocks = new ArrayList<TimeBlock>(rates.size());
        for(InstrumentRate rate: rates) {
        	TimeBlock block = rate.getTimeBlock();
        	timeBlocks.add(block);
        }
        if(timeBlocks.size() == 0) {
        	ActionErrors errors = new ActionErrors();
			errors.add("scheduler", new ActionMessage("error.scheduler.load","No time blocks were found for instrument: "+instrument.getName()+
					" and rateType: "+rateType.getName()));
			saveErrors( request, errors );
			ActionForward fwd = mapping.findForward("Failure");
			ActionForward newFwd = new ActionForward(fwd.getPath()+"?ID="+projectId, fwd.getRedirect());
        	return newFwd;
        }
        // sort the blocks by numHours
        Collections.sort(timeBlocks, new Comparator<TimeBlock>() {
			@Override
			public int compare(TimeBlock o1, TimeBlock o2) {
				return Integer.valueOf(o1.getNumHours()).compareTo(o2.getNumHours());
			}
		});
        
        // Get the time blocks for which this instrument has a rate
        List<InstrumentRate> instrumentRates = InstrumentRateDAO.getInstance().getAllCurrentRatesForInstrument(instrumentId);
        
        
        Set<Integer> instrumentTimeBlockIds = new HashSet<Integer>();
        for(InstrumentRate rate: instrumentRates) {
        	instrumentTimeBlockIds.add(rate.getTimeBlock().getId());
        }
        
        Iterator<TimeBlock> iter = timeBlocks.iterator();
        while(iter.hasNext()) {
        	TimeBlock block = iter.next();
        	if(!instrumentTimeBlockIds.contains(block.getId()))
        		iter.remove();
        	if(block.getNumHours() == 0) // time blocks associated with old scheduled instrument time
        		iter.remove();
        }

        request.setAttribute("instruments", instruments); // set in the request to display as a drop down menu
		// Get a list of verified instrument operators;
		List<Researcher> allInstrumentOperators = Groups.getInstance().getMembers(Groups.INSTRUMENT_OPERATOR);
		// Put a list of instrument operators listed on the project.
		List<Researcher> projectInstrumentOperators = project.getInstrumentOperators(allInstrumentOperators);
		List<Map<String, Object>> instrumentOperators = new ArrayList<Map<String, Object>>(projectInstrumentOperators.size());
		for(Researcher researcher: projectInstrumentOperators)
		{
			Map<String, Object> values = new HashMap<String, Object>(3);
			values.put("ID", researcher.getID());
			values.put("fullName", researcher.getFullName());
			values.put("timeRemaining", ProjectInstrumentTimeApprover.getRemainingInstrumentTimeForOperator(researcher));
			instrumentOperators.add(values);
		}
		request.setAttribute("instrumentOperators", instrumentOperators);
        request.setAttribute("projectId", projectId);
        request.setAttribute("instrumentId", instrumentId);
        request.setAttribute("timeBlocks", timeBlocks);
        
        if(project instanceof BilledProject) {
	        // get a list of payment methods for this project
	        List<PaymentMethod> paymentMethods = ProjectPaymentMethodDAO.getInstance().getCurrentPaymentMethods(projectId);
	        request.setAttribute("paymentMethods", paymentMethods);
        }
        
        // If the user making the request is an admin put a list of projects 
        // to be displayed in a drop down list
        ProjectsSearcher projSearcher = new ProjectsSearcher();
        projSearcher.addType(new BilledProject().getShortType()); // billed projects
        if(!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
        	projSearcher.setResearcher(user.getResearcher());
        }
        List <Project> billedProjects = projSearcher.search();

        projSearcher = new ProjectsSearcher();
        projSearcher.addType(new Collaboration().getShortType()); // subsidized projects
        projSearcher.addStatusType(CollaborationStatus.ACCEPTED); // list accepted projects only
        if(!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
        	projSearcher.setResearcher(user.getResearcher());
        }
        List<Project> subsidizedProjects = projSearcher.search();

        List<Project> projects = new ArrayList<Project>();
        projects.addAll(billedProjects);
        projects.addAll(subsidizedProjects);

        Collections.sort(projects, new ProjectPIComparator());
        request.setAttribute("projects", projects);
        
        // If the "scheduler_year" and "scheduler_month" attributes were set in the 
        // EditProjectInstrumentTimeAction action, pass the values on be set in the calendar
        if(request.getSession().getAttribute("scheduler_year") != null) {
        	request.setAttribute("year", request.getSession().getAttribute("scheduler_year"));
        	request.getSession().setAttribute("scheduler_year", null);
        }
        if(request.getSession().getAttribute("scheduler_month") != null) {
        	request.setAttribute("month", request.getSession().getAttribute("scheduler_month"));
        	request.getSession().setAttribute("scheduler_month", null);
        }
        
        if(project instanceof BilledProject)
        	return mapping.findForward("BilledProjectScheduler");
        else
        	return mapping.findForward("SubsidizedProjectScheduler");
	}
}
