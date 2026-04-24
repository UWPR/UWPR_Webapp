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
import java.sql.SQLException;

public class SaveNoticeAction extends Action {

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

        Notice notice = new Notice();
        try {
            notice.setStartDate(noticeForm.getStartDate());
            notice.setEndDate(noticeForm.getEndDate());
        }
        catch (Exception e) {
            // Validate() should have caught this; defensive fallback.
            ActionErrors errors = new ActionErrors();
            errors.add("notice", new ActionMessage("error.costcenter.invaliddata", "Invalid date: " + e.getMessage()));
            saveErrors(request, errors);
            return mapping.findForward("Failure");
        }
        String msg = noticeForm.getMessage();
        notice.setMessage(msg == null ? "" : msg.trim());

        try {
            int existingId = noticeForm.getId();
            if (existingId > 0) {
                notice.setId(existingId);
                int affected = NoticeDAO.getInstance().updateNotice(notice);
                if (affected == 0) {
                    ActionErrors errors = new ActionErrors();
                    errors.add("notice", new ActionMessage("error.costcenter.save", "Notice no longer exists (id=" + existingId + "). It may have been deleted by another user."));
                    saveErrors(request, errors);
                    request.setAttribute("notices", NoticeDAO.getInstance().getAllNotices());
                    return mapping.findForward("NotFound");
                }
            }
            else {
                notice.setCreatedBy(user.getResearcher().getID());
                NoticeDAO.getInstance().saveNotice(notice);
            }
        }
        catch (SQLException e) {
            ActionErrors errors = new ActionErrors();
            errors.add("notice", new ActionMessage("error.costcenter.save", e.getMessage()));
            saveErrors(request, errors);
            return mapping.findForward("Failure");
        }

        return mapping.findForward("Success");
    }
}
