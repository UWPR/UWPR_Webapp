/**
 * 
 */
package org.yeastrc.www.project;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.yeastrc.project.Projects;
import org.yeastrc.project.Researcher;
import org.yeastrc.utils.CountriesBean;
import org.yeastrc.utils.StatesBean;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

/**
 * AddLabDirectorAction.java
 * @author Vagisha Sharma
 * Jul 19, 2010
 * 
 */
public class AddLabDirectorAction extends Action {


	public ActionForward execute( ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response )
	throws Exception {
		
		// User making this request
		User user = UserUtils.getUser(request);
		if (user == null) {
			ActionErrors errors = new ActionErrors();
			errors.add("username", new ActionMessage("error.login.notloggedin"));
			saveErrors( request, errors );
			return mapping.findForward("authenticate");
		}

		// The Researcher we're editing (is the one logged in)
		Researcher researcher = user.getResearcher();

		
		// Create our ActionForm
		AddLabDirectorForm newForm = new AddLabDirectorForm();
		request.setAttribute("addLabDirectorForm", newForm);
		
		newForm.setDepartment(researcher.getDepartment());
		newForm.setOrganization(researcher.getOrganization());
		newForm.setState(researcher.getState());
		newForm.setZipCode(researcher.getZipCode());
		newForm.setCountry(researcher.getCountry());

		HttpSession session = request.getSession();
		
		// Save our states bean
		StatesBean sb = StatesBean.getInstance();
		session.setAttribute("states", sb.getStates());

		// Save our countries bean
		CountriesBean cb = CountriesBean.getInstance();
		session.setAttribute("countries", cb.getCountries());

		// List of existing researchers.
		Collection researchers = Projects.getAllResearchers();
        session.setAttribute("researchers", researchers);
        
		// Go!
		return mapping.findForward("Success");

	}
}
