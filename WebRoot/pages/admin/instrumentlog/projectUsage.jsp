
<%@page import="org.uwpr.instrumentlog.ProjectInstrumentUsage"%>
<%@page import="org.yeastrc.project.Collaboration"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<logic:empty name="projectUsageList">
  <logic:forward name="projectUsageList" />
</logic:empty>


<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/tablesorter.css">
<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.tablesorter.min.js'></script>

<script>
$(document).ready(function() { 
	// extend the default setting to always include the zebra widget. 
    $.tablesorter.defaults.widgets = ['zebra']; 
    // extend the default setting to always sort on the second (Instrument) column 
    // column indexes are 0-based.
    $.tablesorter.defaults.sortList = [[0,1]]; 
    $("#usageList").tablesorter({
    	headers: { 
            
        }
    }); 
});

</script>


</div> <!-- End of div #page so that we can have a wide table-->
<yrcwww:contentbox title="Project Usage">
		
	<logic:present name="dateUpdated">
	<div>
		<b>Last Updated: <bean:write name="dateUpdated"/> </b>
	</div>
	</logic:present>

	<table id="usageList" class="tablesorter" cellspacing="2" cellpadding="2" id="mytable" width="98%">
		<thead>
			<th class="sort-int header" style="font-size: 9pt;"><span style="margin-right: 20px;">ID</span></th>
			<th class="sort-alpha header" style="font-size: 9pt;" align="left"><span style="margin-right: 20px;">Lab<br>Director</span></th>
			<th class="sort-date header" style="font-size: 9pt;" align="left"><span style="margin-right: 20px;">Date<br>Accepted</span></th>
			<th class="sort-alpha header" style="font-size: 9pt;" align="left"><span style="margin-right: 20px;">Status</span></th>
			<logic:iterate name="instrumentNames" id="instrumentName">
				<th class="sort-int header" style="font-size: 8pt;" align="left">
					<span style="margin-right: 10px;"># Hours<br>on<br><bean:write name="instrumentName" /></span>
				</th>
			</logic:iterate>
			<th class="sort-int header" style="font-size: 9pt;" align="left"><span style="margin-right: 25px;"># Runs<br>Req.</span></th>
			<th class="sort-int header" style="font-size: 9pt;" align="left"><span style="margin-right: 25px;"># Raw<br>Files</span></th>
			<th class="sort-float header" style="font-size: 9pt;" align="left"><span style="margin-right: 15px;">Size(GB)</span></th>
			<th class="sort-alpha header" style="font-size: 9pt;"><span style="margin-right: 20px;">Directory</span></th>
		</thead>
		<tbody>
			<logic:iterate name="projectUsageList" id="projectUsage" type="org.uwpr.instrumentlog.rawfile.ProjectUsageSummary">
				<tr>
					<td><a href="/pr/viewProject.do?ID=<bean:write name="projectUsage" property="project.ID" />"><bean:write name="projectUsage" property="project.ID" /></a></td>
					<td><bean:write name="projectUsage" property="project.PI.lastName" /></td>
					<td>
						<logic:present name="projectUsage" property="project.collaborationStatus">
							<bean:write name="projectUsage" property="project.dateAccepted" />		
						</logic:present>
					</td>
					<td>
						<logic:present name="projectUsage" property="project.collaborationStatus">
							<bean:write name="projectUsage" property="project.collaborationStatus.shortName" />
						</logic:present>
					</td>
					<logic:iterate name="instruments" id="instrument" type="org.uwpr.instrumentlog.MsInstrument">
						<%ProjectInstrumentUsage usage = projectUsage.getUsageForInstrument(instrument.getID()); %>
						<td>
						<%if(usage != null) { %>
							<%=usage.getSumHoursUsed() %>
						<%} else {%>
							-
						<%} %>
						</td>
					</logic:iterate>
					<td>
						<logic:present name="projectUsage" property="project.collaborationStatus">
							<bean:write name="projectUsage" property="project.totalRunsRequested" />
						</logic:present>
					</td>
					<td><bean:write name="projectUsage" property="rawFileUsage.rawFileCount" /></td>
					<td><bean:write name="projectUsage" property="rawFileUsage.rawFileSize" /></td>
					<td><bean:write name="projectUsage" property="rawFileUsage.dataDirectory" /></td>
				</tr>
			</logic:iterate>
		</tbody>
	</table>
</yrcwww:contentbox>

<div> <!-- For ending div in footer.jsp -->
<%@ include file="/includes/footer.jsp"%>
