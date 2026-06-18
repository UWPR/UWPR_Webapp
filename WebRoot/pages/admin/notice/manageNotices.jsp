<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>

<script>
    function confirmDelete(noticeId) {
        if (confirm("Are you sure you want to delete this notice?")) {
            document.location.href = "/pr/deleteNotice.do?id=" + noticeId;
            return 1;
        }
    }

    function addNotice() {
        document.location.href = "/pr/editNotice.do";
    }
</script>

<yrcwww:contentbox title="Home Page Notices">
<center>

<logic:empty name="notices">
    <p style="margin: 20px;">No notices have been posted.</p>
</logic:empty>

<logic:notEmpty name="notices">
<table border="0" cellpadding="7" class="striped">
    <thead>
        <tr>
            <th><span style="padding:0 10 0 0;">ID</span></th>
            <th><span style="padding:0 10 0 0;">Start Date</span></th>
            <th><span style="padding:0 10 0 0;">End Date</span></th>
            <th><span style="padding:0 10 0 0;">Message</span></th>
            <th></th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        <logic:iterate name="notices" id="notice">
            <tr>
                <td><bean:write name="notice" property="id"/></td>
                <td><bean:write name="notice" property="startDateString"/></td>
                <td><bean:write name="notice" property="endDateString"/></td>
                <td><bean:write name="notice" property="message"/></td>
                <td>
                    <a href="/pr/editNotice.do?id=<bean:write name="notice" property="id"/>">[Edit]</a>
                </td>
                <td>
                    <a href="#" onclick="confirmDelete('<bean:write name="notice" property="id"/>'); return false;">
                        <span style="color:red">[Delete]</span>
                    </a>
                </td>
            </tr>
        </logic:iterate>
    </tbody>
</table>
</logic:notEmpty>

<div style="margin:20px">
    <input type="button" value="Add New Notice" onclick="addNotice();"/>
</div>

</center>
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>
