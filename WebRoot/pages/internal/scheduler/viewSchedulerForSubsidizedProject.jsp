<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<logic:notPresent name="instruments">
  <logic:forward name="viewScheduler" />
</logic:notPresent>


<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<link rel='stylesheet' type='text/css' href='/pr/css/fullcalendar.css' />

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>
<script type='text/javascript' src='/pr/js/fullcalendar.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.qtip-1.0.0-rc3.min.js'></script>
<script type='text/javascript' src='/pr/js/uwpr.scheduler.js'></script>

<style type='text/css'>

	#calendar {
		width: 900px;
		margin: 0 auto;
		}
		
	#scheduledTimeDiv table th
	{
		font-size:8pt;
		font-weight:bold;
	}
	#scheduledTimeDiv table td
	{
		font-size:8pt;
		font-weight:bold;
	}

</style>

<script type='text/javascript'>


$(document).ready(function() {

	
	initCalendar();
	// select the correct instrument in the drop-down menu
	$("#instrumentSelector").val(<bean:write name="instrumentId"/>);
	
	// select the correct project in the drop-down menu
	// project selector is displayed only for site admins.
	$("#projectSelector").val(<bean:write name="projectId"/>);
	
});

function getEventSourceUrl() {
	return "/pr/instrumentUsageBlocks.do";
}
function getProjectUrl(projectId) {
	return "/pr/viewProject.do?ID="+projectId;
}
function getDeleteTimeBlockUrl() {
	return "/pr/deleteInstrumentTimeAjax.do";
}
function getEditTimeBlockUrl() {
	return "/pr/viewEditInstrumentTimeForm.do";
}
function getProjectId() {
	return <bean:write name="projectId"/>;
}
function getInstrumentId() {
	return <bean:write name="instrumentId"/>;
}
function getStartTimes() {

	<yrcwww:member group="administrators">
	
		return getAllTimes(10); // select 10am
	
	</yrcwww:member>
	
	<yrcwww:notmember group="administrators">
		var stimes = [{ value: 10,display:  "10:00 am", selected:true },
	   				  { value: 14,display: "2:00 pm", selected:false }
	   			      ];
	   return stimes;
	</yrcwww:notmember>
	
}

function getEndTimes() {

	<yrcwww:member group="administrators">
	
		return getAllTimes(18); // select 6pm
	
	</yrcwww:member>
	
	<yrcwww:notmember group="administrators">
		var stimes = [{ value: 10,display:  "10:00 am", selected:false },
	   				  { value: 14,display: "2:00 pm", selected:false },
	   			      { value: 18,display: "6:00 pm", selected:true }
	   			      ];
	   return stimes;
	</yrcwww:notmember>
}

function getAllTimes(select) {

	var times = [];
	
	times[0] = { value: 0,display:  "12:00 am", selected:false };
	for(var i = 1; i <= 11; i++) {
		if(i == select) {
			times[i] = { value: i,display:  i+":00 am", selected:true };
		}
		else {
			times[i] = { value: i,display:  i+":00 am", selected:false };
		}
	}
	times[12] = { value: 12,display:  "12:00 pm", selected:false };
	for(var i = 13; i <= 23; i++) {
		if(i == select) {
			times[i] = { value: i,display:  (i-12)+":00 pm", selected:true };
		}
		else {
			times[i] = { value: i,display:  (i-12)+":00 pm", selected:false };
		}
	}
	
	return times;
}

function getRequestInformation() {
	
	var information = {};
	information.requestUrl = "/pr/requestInstrumentTimeAjax.do";
	information.projectId = getProjectId();
	information.instrumentId = getInstrumentId();
	information.hasPaymentMethodInfo = false;
       						
     return information;		
}

function initCalendar() {

	var date = new Date();
	// var d = date.getDate();
	var m = date.getMonth();
	var y = date.getFullYear();
	
	$('#calendar').uwpr_scheduler({
			instrumentId: getInstrumentId(),
			projectId: getProjectId(),
			eventJSONSourceUrl: getEventSourceUrl(),
			onAddEventSuccessFn: null,
			eventDeleteUrl: getDeleteTimeBlockUrl(),
			eventEditUrl:getEditTimeBlockUrl(),
			onDeleteSuccessFn: null,
			projectLinkUrlFn: getProjectUrl,
			requestInformationFn: getRequestInformation,
			startTimes: getStartTimes(),
			endTimes: getEndTimes(),
			canAddEvents: true,
			<logic:present name="year">
				year: <bean:write name="year"/>,
				month: <bean:write name="month"/>
			</logic:present>
			<logic:notPresent name="year">
				year: y,
				month: m
			</logic:notPresent>
			
	});
}

function refreshCalendar() {

	$('#calendar').uwpr_scheduler('refresh');
}

function switchInstrument(projectId) {
	var instrumentId = $("#instrumentSelector :selected").val();
	//alert("instrumentID: "+instrumentId+"\nURL:"+"/pr/viewScheduler.do?instrumentId="+instrumentId+"&projectId="+projectId);
	document.location.href="/pr/viewScheduler.do?instrumentId="+instrumentId+"&projectId="+projectId;
}

function switchProject() {
	var projectId = $("#projectSelector :selected").val();
	var instrumentId = $("#instrumentSelector :selected").val();
	// alert("instrumentID: "+instrumentId+"\nURL:"+"/pr/viewScheduler.do?instrumentId="+instrumentId+"&projectId="+projectId);
	document.location.href="/pr/viewScheduler.do?instrumentId="+instrumentId+"&projectId="+projectId;
}



</script>

<yrcwww:contentbox title="Schedule Instrument Time">
<center>

<div class="ui-state-default">

<div style="margin:5px 0 0 0; text-align:center;">
Project ID: <bean:write name="projectId"/>
<span style="text-decoration:underline; font-size:8pt;">
	<html:link action="viewProject.do" paramId="ID" paramName="projectId"> (back to project)</html:link>
</span>
</div>
<!-- Instrument Selector -->
<div style="margin:20px 0 20px 0; text-align:center;">
Select Instrument:
<select id="instrumentSelector" onchange='switchInstrument(<bean:write name="projectId" />)'>
<logic:iterate name="instruments" id="instrument">
	<logic:equal name="instrument" property="active" value="true">
		<option value='<bean:write name="instrument" property="ID" />'><bean:write name="instrument" property="name" /></option>
	</logic:equal>
</logic:iterate>

</select>
</div>

<!-- Project selector -->
<logic:present name="projects">
	<div style="margin:20px 0 20px 0; text-align:center;">
	Select Project:
	<select id="projectSelector" onchange='switchProject()'>
		<logic:iterate name="projects" id="project">
			<option value='<bean:write name="project" property="ID" />'><bean:write name="project" property="label" /></option>
		</logic:iterate>
	</select>
	</div>
</logic:present>

</div>

<div style="margin:10px 0 10px 0; text-align:center;font-weight:bold;color:red;font-size:8pt">
NOTE: If you are scheduling instrument time over a weekend please ensure that you have access to the mass spec. facility. 
</div>

<!-- 
<div style="margin:10px 0 10px 0; text-align:center;">
<a href="#" onclick="refreshCalendar(); return false;" style="color:red;font-size:8pt;text-decoration:underline;">[Refresh Calendar]</a>
</div>
-->

<!-- Calendar  -->
<div id='calendar' style="width:850px;"></div>

</center>
</yrcwww:contentbox>


<%@ include file="/includes/footer.jsp"%>