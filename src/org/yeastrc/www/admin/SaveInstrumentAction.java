package org.yeastrc.www.admin;

import org.apache.struts.action.*;
import org.uwpr.instrumentlog.MsInstrument;
import org.uwpr.instrumentlog.MsInstrumentUtils;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by vsharma on 2/9/2016.
 */
public class SaveInstrumentAction extends Action
{
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

        // Restrict access to administrators
        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors( request, errors );
            return mapping.findForward("Failure");
        }

        AddInstrumentForm myForm = (AddInstrumentForm) form;
        MsInstrumentUtils instrUtils = MsInstrumentUtils.instance();
        String color = myForm.getColor();
        if(color != null && color.startsWith("#"))
        {
            color = color.substring(1); // Remove # from
        }

        MsInstrument instrument;
        // updating an existing instrument
        if(myForm.getId() > 0)
        {
            instrument = MsInstrumentUtils.instance().getMsInstrument(myForm.getId());
            if (instrument == null)
            {
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR,
                        new ActionMessage("error.general.invalid.id", "instrument: " + myForm.getId() +
                                ". No instrument found with this ID."));
                saveErrors(request, errors);
                return mapping.findForward("Failure");
            }
        }
        else
        {
            instrument = new MsInstrument();
        }
        instrument.setName(myForm.getName());
        instrument.setDescription(myForm.getDescription());
        instrument.setColor(color);
        instrument.setActive(myForm.isActive());
        instrument.setMassSpec(myForm.isMassSpec());
        instrUtils.saveInstrument(instrument);

        // Kick it to the view page
        return mapping.findForward("Success");
    }
}
