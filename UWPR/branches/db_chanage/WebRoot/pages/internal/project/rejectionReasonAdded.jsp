<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/global.css">

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<%@ include file="/includes/errors.jsp" %>

<script type="text/javascript">
function updateEditCollabPage(id, cause, description) {
	window.opener.updateRejectionReasons(id, cause, description);
	window.close();
}

</script>
<yrcwww:contentbox title="Saved!">

 <CENTER>
  <table>
  	<tr>
  		<td>Reason:</td>
  		<td>
  			<bean:write name = "rejectionReason" property="cause" />
  		</td>
  	</tr>
  	<tr>
  		<td>Description:</td>
  		<td><bean:write name = "rejectionReason" property="description" /><br></td>
  	</tr>
  </table>
  <br>
  <input type="button" value="Done" 
  		onclick="updateEditCollabPage(<bean:write name = "rejectionReason" property="id" />, '<bean:write name = "rejectionReason" property="cause" />', '<bean:write name = "rejectionReason" property="description" />')"/>
 </CENTER>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>