<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>

<script>
    $(function() {
        $(".datepicker").datepicker();
    });
</script>

<yrcwww:contentbox title="Notice">
<center>
<html:form action="saveNotice" method="POST">

<html:hidden property="idString"/>

<table border="0" cellpadding="7">
    <tbody>
        <tr>
            <td>Start Date:</td>
            <td>
                <html:text property="startDateString" styleClass="datepicker"/>
                <span style="font-size:8pt;">e.g. 04/29/2026</span>
            </td>
        </tr>
        <tr>
            <td>End Date:</td>
            <td>
                <html:text property="endDateString" styleClass="datepicker"/>
            </td>
        </tr>
        <tr>
            <td valign="top">Message:</td>
            <td>
                <html:textarea property="message" rows="5" cols="60"/>
                <br/>
                <span style="font-size:8pt;">Plain text. HTML will be escaped on display.</span>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <html:submit>Save</html:submit>
                <input type="button" onclick="document.location='/pr/manageNotices.do';" value="Cancel"/>
            </td>
        </tr>
    </tbody>
</table>

</html:form>
</center>
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>
