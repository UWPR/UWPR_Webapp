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
	$(document).ready(function() {$(".datepicker").datepicker();});
</script>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>


<yrcwww:contentbox title="Edit Instrument Time">
<center>
<html:form name="editInstrumentTimeForm" type="org.uwpr.www.scheduler.EditProjectInstrumentTimeForm" action="editInstrumentTime" method="POST">
<bean:define name="editInstrumentTimeForm" id="editForm" type="org.uwpr.www.scheduler.EditProjectInstrumentTimeForm" />

<html:hidden name="editInstrumentTimeForm" property="usageBlockIdsToEdit"/>

<table>
<tbody>
<tr>
	<td><b>Project:</b></td>
	<td>
		<yrcwww:projectLink projectId='<%=editForm.getProjectId()%>'/>
		<html:hidden name="editInstrumentTimeForm" property="projectId"/>
	</td>
	<td></td>
</tr>
<tr>
	<td><b>Instrument: </b></td>
	<td>
		<bean:write name="editInstrumentTimeForm" property="instrumentName"/>
		<html:hidden name="editInstrumentTimeForm" property="instrumentId"/>
		<html:hidden name="editInstrumentTimeForm" property="instrumentName"/>
	</td>
	<td></td>
</tr>
<tr>
	<td><b>Instrument operator: </b></td>
	<td>
		<html:select name="editInstrumentTimeForm" property="instrumentOperatorId">
			<html:options collection="instrumentOperators" property="ID" labelProperty="listing"/>
		</html:select>
	</td>
	<td></td>
</tr>
<tr>
	<td><b>Created By: </b></td>
	<td>
		<yrcwww:researcherLink researcherId="<%=editForm.getCreatorId()%>"/>
		<html:hidden name="editInstrumentTimeForm" property="creatorId" />
	</td>
	<td></td>
</tr>
<tr>
	<td><b>Created On: </b></td>
	<td>
		<bean:write name="editInstrumentTimeForm" property="createDate" />
		<html:hidden name="editInstrumentTimeForm" property="createDate" />
	</td>
	<td></td>
</tr>


<logic:notEqual name="editInstrumentTimeForm" property="updaterId" value="0">
<tr>
	<td><b>Updated By: </b></td>
	<td>
		<yrcwww:researcherLink researcherId="<%=editForm.getUpdaterId()%>"/>
		<html:hidden name="editInstrumentTimeForm" property="updaterId" />
	</td>
	<td></td>
</tr>
<tr>
	<td><b>Updated On: </b></td>
	<td>
		<bean:write name="editInstrumentTimeForm" property="updateDate" />
		<html:hidden name="editInstrumentTimeForm" property="updateDate" />
	</td>
	<td></td>
</tr>
</logic:notEqual>


<tr>
	<td><b>Start:</b></td>
	<td>
		<html:text name="editInstrumentTimeForm" property="startDate" styleClass="datepicker"/>
		&nbsp;
		<html:select name="editInstrumentTimeForm" property="startTime">
			<html:optionsCollection name="startTimeOptions" value="value" label="display"/>
		</html:select>
	</td>
</tr>
<tr>
	<td><b>End:</b></td>
	<td>
		<html:text name="editInstrumentTimeForm" property="endDate" styleClass="datepicker"/>
		&nbsp;
		<html:select name="editInstrumentTimeForm" property="endTime">
			<html:optionsCollection name="endTimeOptions" value="value" label="display"/>
		</html:select>
	</td>
</tr>
<tr>
	<td colspan="2" align="center" style="padding-top:20px;">
		<html:submit>Update</html:submit>
		<input type="button" onclick="document.location='/pr/viewScheduler.do?projectId=<bean:write name='editInstrumentTimeForm' property='projectId'/>&instrumentId=<bean:write name='editInstrumentTimeForm' property='instrumentId'/>'" value="Cancel"/>
	</td>
</tr>
</tbody>
</table>
</html:form>
</center>
</yrcwww:contentbox>
<%@ include file="/includes/footer.jsp"%>