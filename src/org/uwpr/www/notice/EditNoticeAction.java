package org.uwpr.www.notice;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.uwpr.notice.Notice;
import org.uwpr.notice.NoticeDAO;
import org.yeastrc.www.user.Groups;
import org.yeastrc.www.user.User;
import org.yeastrc.www.user.UserUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EditNoticeAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {

        User user = UserUtils.getUser(request);
        if (user == null) {
            ActionErrors errors = new ActionErrors();
            errors.add("username", new ActionMessage("error.login.notloggedin"));
            saveErrors(request, errors);
            return mapping.findForward("authenticate");
        }

        Groups groupMan = Groups.getInstance();
        if (!groupMan.isMember(user.getResearcher().getID(), "administrators")) {
            ActionErrors errors = new ActionErrors();
            errors.add("access", new ActionMessage("error.access.invalidgroup"));
            saveErrors(request, errors);
            return mapping.findForward("Failure");
        }

        NoticeForm noticeForm = (NoticeForm) form;

        // If an id parameter is present, populate the form for editing.
        // Absent param => create new. Present but empty/malformed => error.
        String idParam = request.getParameter("id");
        if (idParam != null) {
            int id;
            try {
                id = Integer.parseInt(idParam);
            }
            catch (NumberFormatException e) {
                ActionErrors errors = new ActionErrors();
                errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "Invalid notice id: " + idParam));
                saveErrors(request, errors);
                request.setAttribute("notices", NoticeDAO.getInstance().getAllNotices());
                return mapping.findForward("NotFound");
            }

            Notice notice = NoticeDAO.getInstance().getNotice(id);
            if (notice == null) {
                ActionErrors errors = new ActionErrors();
                errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "Notice not found: id=" + id));
                saveErrors(request, errors);
                request.setAttribute("notices", NoticeDAO.getInstance().getAllNotices());
                return mapping.findForward("NotFound");
            }

            noticeForm.setId(notice.getId());
            noticeForm.setStartDate(notice.getStartDate());
            noticeForm.setEndDate(notice.getEndDate());
            noticeForm.setMessage(notice.getMessage());
        }

        return mapping.findForward("Success");
    }
}
