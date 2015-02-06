<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/global.css">

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<%@ include file="/includes/errors.jsp" %>

<yrcwww:contentbox title="Add Collaboration Rejection Reason">

 <CENTER>
  <html:form action="addRejectionCause" method="post" styleId="form1">
  <table>
  	<tr>
  		<td>Reason:</td>
  		<td><html:text name = "rejectionCauseForm" property="cause" /></td>
  	</tr>
  	<tr>
  		<td>Description:</td>
  		<td><html:text name = "rejectionCauseForm" property="description" /><br><span style="font-size: 8pt">(Text to be included in rejection emails.)</span></td>
  	</tr>
  </table>
  <br>
  	<html:submit styleClass="button">Save</html:submit>
  	<input type="button" onclick="window.close(); return false;" value="Cancel" class="button"/>
  </html:form>
 </CENTER>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>