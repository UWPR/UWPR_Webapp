<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>

<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<link rel='stylesheet' type='text/css' href='/pr/css/fullcalendar.css' />
<link rel='stylesheet' type='text/css' href='/pr/css/global.css' />

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>
<script type='text/javascript' src='/pr/js/fullcalendar.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.qtip-1.0.0-rc3.min.js'></script>
<script type='text/javascript' src='/pr/js/uwpr.scheduler.js?v=06.29.18'></script>

<style type='text/css'>

	#calendar {
		width: 650px;
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

var timeBlocks = {};

$(document).ready(function() {

	//timeBlocks = getTimeBlocks();
	
	initCalendar();
	
	$("#cb_select_toggle").click(function() {
		
		var text = $(this).text();
		if(text == '[Deselect All]') {
			$(this).text('[Select Al]l');
			$(".instrument_cb").attr('checked', '');
		}
		else {
			$(this).text('[Deselect All]');
			$(".instrument_cb").attr('checked', 'checked');
		}
	});
});

var currentEventSourceUrl;

function getEventSourceUrl() {

	var instruments = getSelectedInstrumentList();
	return "/pr/allInstrumentUsageBlocks.do?instruments="+instruments;
}

function getProjectUrl(projectId) {
	return "/pr/viewProject.do?ID="+projectId;
}
function getDeleteTimeBlockUrl() {
	return "/pr/deleteInstrumentTimeAjax.do";
}

function initCalendar() {

	var date = new Date();
	var d = date.getDate();
	var m = date.getMonth();
	var y = date.getFullYear();
	
	$('#calendar').uwpr_scheduler({
			instrumentId: 0,
			projectId: 0,
			eventJSONSourceUrl: getEventSourceUrl(),
			onAddEventSuccessFn: null,
			eventDeleteUrl: getDeleteTimeBlockUrl(),
			onDeleteSuccessFn: null,
			projectLinkUrlFn: getProjectUrl,
			requestInformationFn: null,
			timeBlocks: null,
			canAddEvents: false
	});
	
	currentEventSourceUrl = getEventSourceUrl();
}

function refreshCalendar() {

	var newEventSourceUrl = getEventSourceUrl();
	var arr = new Array();
	arr[0] = currentEventSourceUrl;
	arr[1] = newEventSourceUrl;
	$('#calendar').uwpr_scheduler('updateEventSource', arr);
	currentEventSourceUrl = newEventSourceUrl;
	// $('#calendar').uwpr_scheduler('refresh');
}

function getSelectedInstrumentList() {
	
	var instruments = "";
	$(".instrument_cb:checked").each(function() {
		instruments += $(this).attr('id');
		instruments += ",";
	});
	// alert(instruments);
	return instruments;
}

function goToScheduler() {
	var projectId = $("#projectSelector :selected").val();
	var instrumentId = $("#instrumentSelector :selected").val();
	// alert("instrumentID: "+instrumentId+"\nURL:"+"/pr/viewScheduler.do?instrumentId="+instrumentId+"&projectId="+projectId);
	document.location.href="/pr/viewScheduler.do?instrumentId="+instrumentId+"&projectId="+projectId;
}



</script>
</head>

<body>
<center>
<table>

	<tbody>
		<tr>
			<td width="15%" valign="top">
				<table style="padding-top:50px;">
					<logic:iterate name="instruments" id="instrument">
						<logic:equal name="instrument" property="active" value="true">
						<tr>
							<td>
								<input type="checkbox" class="instrument_cb" id='<bean:write name="instrument" property="instrumentId" />' checked="checked"/>
							</td>
							<td>
								<span style="width:20px; height:20px; background-color:#<bean:write name='instrument' property='color'/>;" >&nbsp;&nbsp;&nbsp;</span>
							</td>
							<td style="font-size:8pt;">
								<bean:write name="instrument" property="name"/>
							</td>
						</tr>
						</logic:equal>
					</logic:iterate>
					<tr><td colspan="3"><hr/></td></tr>
					<logic:iterate name="instruments" id="instrument">
						<logic:equal name="instrument" property="active" value="false">
						<tr>
							<td>
								<input type="checkbox" class="instrument_cb" id='<bean:write name="instrument" property="instrumentId" />' checked="checked"/>
							</td>
							<td>
								<span style="width:20px; height:20px; background-color:#<bean:write name='instrument' property='color'/>;" >&nbsp;&nbsp;&nbsp;</span>
							</td>
							<td style="font-size:8pt;">
								<bean:write name="instrument" property="name"/>
							</td>
						</tr>
						</logic:equal>
					</logic:iterate>
				</table>
				
				<div id="cb_select_toggle" style="margin:10px 0 10px 0; text-align:left;font-size:8pt;text-decoration:underline;cursor:pointer;">[Deselect All]</div>
				
				<div style="margin:10px 0 10px 0; text-align:left;">
					<span style="color:red;font-size:8pt;text-decoration:underline;cursor:pointer;"onclick="refreshCalendar()">[Refresh Calendar]</span>
				</div>
			</td>
			
			<td width="85%">
				<!-- Calendar  -->
				<div id='calendar'"></div>
			</td>
		</tr>
	</tbody>
</table>

</center>
</body>
</html>
