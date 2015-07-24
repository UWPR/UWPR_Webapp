<%@page import="org.uwpr.instrumentlog.MsInstrumentUsage"%>
<%
	String instrumentName = "";
	MsInstrumentUsage instrumentUsage = (MsInstrumentUsage)instrument;
	if(instrumentUsage != null)
	{
		instrumentName = instrumentUsage.getInstrumentName();
	}
	
%>
<yrcwww:contentbox title="<%=instrumentName %>" innerBox="true">

	<div style="text-align:left;font-weight:bold;">
		Usage: <bean:write name="instrument" property="numHoursUsed"/> / <bean:write name="instrument" property="hoursInRange"/> hours (<bean:write name="instrument" property="percentUsed"/>%)
	</div>
	<div style="text-align:left;font-weight:bold;">
	Top Projects:
	</div>	
	<table align="center" cellpadding="3" width="100%" class="striped">
		<thead>
			<tr>
			<th><b><font style="font-size:9pt;">ID</font></b></th>
			<th><b><font style="font-size:9pt;">Title</font></b></th>
			<th><b><font style="font-size:9pt;">PI</font></b></th>
			<th><b><font style="font-size:9pt;">Usage</font></b></th>
			</tr>
		</thead>
		
		<tbody>
		<logic:iterate name="instrument" property="topProjects" id="projectUsage">
			<tr>
				<td>
					<html:link href="/pr/viewProject.do" paramId="ID" paramName="projectUsage" paramProperty="projectID">
						<bean:write name="projectUsage" property="projectID" />
					</html:link>
				</td>
				<td><bean:write name="projectUsage" property="projectTitle" /></td>
				<td><html:link href="/pr/viewResearcher.do" paramId="id" paramName="projectUsage" paramProperty="PIID"><bean:write name="projectUsage" property="projectPI" /></html:link></td>
				<td><bean:write name="projectUsage" property="numHoursUsed"/> hours<br>(<bean:write name="projectUsage" property="percentUsed"/>%)</td>
			</tr>
		</logic:iterate>
		</tbody>
	</table>
	
	<div style="font-weight:bold;">
	
		<html:link href="/pr/instrumentUsageSummary.do" paramId="instrumentID" paramName="instrument" paramProperty="instrumentID" >Details</html:link>
			&nbsp;
			&nbsp;
			
		<html:link href="/pr/viewAllInstrumentCalendar.do" >View Calendar</html:link>
            &nbsp;
            &nbsp;
        <html:link href="/pr/viewTimeScheduledForInstrument.do" paramId="instrumentId" paramName="instrument" paramProperty="instrumentID">List Usage</html:link>

	</div>

</yrcwww:contentbox>
<br>