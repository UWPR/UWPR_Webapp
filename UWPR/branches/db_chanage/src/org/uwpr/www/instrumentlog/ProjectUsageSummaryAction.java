/**
 * ProjectUsageSummaryAction.java
 * @author Vagisha Sharma
 * Jan 13, 2009
 * @version 1.0
 */
package org.uwpr.www.instrumentlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.uwpr.instrumentlog.ProjectInstrumentUsage;
import org.uwpr.instrumentlog.ProjectInstrumentUsageDAO;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsage;
import org.uwpr.instrumentlog.rawfile.ProjectRawFileUsageUtils;
import org.uwpr.instrumentlog.rawfile.ProjectUsageSummary;
import org.yeastrc.project.CollaborationStatus;
import org.yeastrc.project.Project;
import org.yeastrc.project.ProjectsSearcher;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * 
 */
public class ProjectUsageSummaryAction extends Action {

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

        // Restrict access to administrators
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }
        
        ProjectsSearcher ps = new ProjectsSearcher();
        ps.addStatusType(CollaborationStatus.ACCEPTED);
        ps.addStatusType(CollaborationStatus.COMPLETE);
        ps.addStatusType(CollaborationStatus.EXPIRED);
        List <Project> projects = ps.search();
        Collections.sort(projects, new Comparator<Project>() {
            public int compare(Project o1, Project o2) {
                return Integer.valueOf(o2.getID()).compareTo(o1.getID());
            }});
        
        
        List<ProjectUsageSummary> usageList = new ArrayList<ProjectUsageSummary>(projects.size());
        
        List<ProjectInstrumentUsage> projInstrUsage = ProjectInstrumentUsageDAO.getInstance().getAllProjectInstrumentUsage(null, null);
        // sort by projectID
        Collections.sort(projInstrUsage, new Comparator<ProjectInstrumentUsage>() {
            public int compare(ProjectInstrumentUsage o1, ProjectInstrumentUsage o2) {
                return Integer.valueOf(o1.getProjectID()).compareTo(o2.getProjectID());
            }});
        
        
        for(Project project: projects) {
            ProjectUsageSummary summary = new ProjectUsageSummary();
            summary.setProject(project);
            
            ProjectRawFileUsage rawFileUsage = ProjectRawFileUsageUtils.instance().loadUsage(project.getID());
            if(rawFileUsage == null)
                rawFileUsage = new ProjectRawFileUsage();
            summary.setRawFileUsage(rawFileUsage);
            
            List<ProjectInstrumentUsage> projUsage = getProjectInstrumentUsage(project.getID(), projInstrUsage);
            // sort by instrument ID
            Collections.sort(projUsage, new Comparator<ProjectInstrumentUsage>() {
                public int compare(ProjectInstrumentUsage o1,
                        ProjectInstrumentUsage o2) {
                    return Integer.valueOf(o1.getInstrumentID()).compareTo(o2.getInstrumentID());
                }});
            summary.setInstrumentUsage(projUsage);
            usageList.add(summary);
        }
        
        request.setAttribute("projectUsageList", usageList);
        
        List<MsInstrument> instruments = MsInstrumentUtils.instance().getMsInstruments();
        Collections.sort(instruments, new Comparator<MsInstrument>() {
            public int compare(MsInstrument o1, MsInstrument o2) {
                return Integer.valueOf(o1.getID()).compareTo(o2.getID());
            }});
        List<String> instrumentNames = new ArrayList<String>(instruments.size());
        for(MsInstrument instrument: instruments) {
            if(instrument.getNameOnly().equals("LTQ"))
                instrumentNames.add("LTQ");
            else if (instrument.getNameOnly().equals("LTQ-ETD"))
                instrumentNames.add("ETD");
            else if (instrument.getNameOnly().equals("LTQ-Orbitrap-1"))
                instrumentNames.add("Orbi1");
            else if (instrument.getNameOnly().equals("LTQ-Orbitrap-2"))
                instrumentNames.add("Orbi2");
            else if (instrument.getNameOnly().equals("LTQ-FT"))
                instrumentNames.add("FT");
            else if (instrument.getNameOnly().equals("TSQ-Access"))
                instrumentNames.add("TSQ-A");
            else if (instrument.getNameOnly().equals("TSQ-Vantage"))
                instrumentNames.add("TSQ-V");
            else if (instrument.getNameOnly().equals("nanoMate"))
                instrumentNames.add("nMate");
            else if (instrument.getNameOnly().equals("Agilent HPLC"))
                instrumentNames.add("HPLC");
            else if (instrument.getNameOnly().equals("Q"))
                instrumentNames.add("Q");
        }
        request.setAttribute("instruments", instruments);
        request.setAttribute("instrumentNames", instrumentNames);
        
        if(ProjectRawFileUsageUtils.getLastDateParsed() != null) {
            request.setAttribute("dateUpdated", ProjectRawFileUsageUtils.getLastDateParsed());
        }
        
        return mapping.findForward("Success");   
    }
    
    private List<ProjectInstrumentUsage> getProjectInstrumentUsage(int projectId, List<ProjectInstrumentUsage> sortedUsage) {
        List<ProjectInstrumentUsage> usageList = new ArrayList<ProjectInstrumentUsage>();
        for(ProjectInstrumentUsage usage: sortedUsage) {
            if(usage.getProjectID() == projectId)
                usageList.add(usage);
            if(usage.getProjectID() > projectId)
                break;
        }
        return usageList;
    }
}
