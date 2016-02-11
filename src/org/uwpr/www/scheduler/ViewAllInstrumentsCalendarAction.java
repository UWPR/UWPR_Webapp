/**
 * ViewAllInstrumentsAvailabilityAction.java
 * @author Vagisha Sharma
 * May 21, 2009
 * @version 1.0
 */
package org.uwpr.www.scheduler;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.*;
import org.uwpr.instrumentlog.InstrumentColors;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.yeastrc.project.*;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 
 */
public class ViewAllInstrumentsCalendarAction extends Action {

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception 
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception{
        
        // User making this request
        User user = UserUtils.getUser(request);
        if (user == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.login.notloggedin"));
            saveErrors( request, errors );
            return mapping.findForward("authenticate");
        }
        
        // get a list of ms instruments
        List <MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
        Collections.sort(instruments, new Comparator<MsInstrument>() {
            public int compare(MsInstrument o1, MsInstrument o2) {
                return o1.getID() > o2.getID() ? 1 : (o1.getID() == o2.getID() ? 0 : -1);
            }});
        
        List<InstrumentNameColor> instrumentNameColors = new ArrayList<InstrumentNameColor>(instruments.size());
        
        for (MsInstrument instrument: instruments) {
        	
        	InstrumentNameColor item = new InstrumentNameColor();
        	item.setInstrumentId(instrument.getID());
        	item.setName(instrument.getName());
        	
        	item.setActive(instrument.isActive());
            String color = instrument.getColor();
        	item.setColor(StringUtils.isBlank(color) ? InstrumentColors.getColor(instrument.getID()) : color);
        	instrumentNameColors.add(item);
        }
        request.setAttribute("instruments", instrumentNameColors);
        
        Groups groupMan = Groups.getInstance();

        // Put a list of projects to be displayed in a drop down list
        ProjectsSearcher projSearcher = new ProjectsSearcher();
        projSearcher.addType(new BilledProject().getShortType()); // billed projects
        if(!groupMan.isMember(user.getResearcher().getID(), "administrators"))
        {
            // If the user is not an admin, include only those projects to which the
            // user has access.
            projSearcher.setResearcher(user.getResearcher());
        }
        List <Project> billedProjects = projSearcher.search();
        List<Project> projects = new ArrayList<Project>();
        projects.addAll(billedProjects);

        if (groupMan.isMember(user.getResearcher().getID(), "administrators"))
        {
            // Add subsidized projects only if the user making this request is an admin.
            projSearcher = new ProjectsSearcher();
            projSearcher.addType(new Collaboration().getShortType()); // subsidized projects
            projSearcher.addStatusType(CollaborationStatus.ACCEPTED); // list accepted projects only
            List<Project> subsidizedProjects = projSearcher.search();
            projects.addAll(subsidizedProjects);
        }


        Collections.sort(projects, new ProjectPIComparator());
        request.setAttribute("projects", projects);


        
        if(request.getParameter("popup") != null)
        {
        	return mapping.findForward("Popup");
        }
        return mapping.findForward("Success");
    }
    
    public static final class InstrumentNameColor {
    	
    	private int instrumentId;
    	private String name;
    	private boolean active;
    	private String color;
    	
    	
		public int getInstrumentId() {
			return instrumentId;
		}
		public void setInstrumentId(int instrumentId) {
			this.instrumentId = instrumentId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color = color;
		}
		public boolean isActive() {
			return active;
		}
		public void setActive(boolean active) {
			this.active = active;
		}
    }
}
