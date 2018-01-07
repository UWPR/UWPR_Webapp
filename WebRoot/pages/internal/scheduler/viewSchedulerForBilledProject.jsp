<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<logic:notPresent name="paymentMethods">
  <logic:forward name="viewScheduler" />
</logic:notPresent>


<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<link rel='stylesheet' type='text/css' href='/pr/css/fullcalendar.css' />

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>
<script type='text/javascript' src='/pr/js/fullcalendar.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.qtip-1.0.0-rc3.min.js'></script>
<script type='text/javascript' src='/pr/js/uwpr.scheduler.js?v=05.02.17'></script>

<style type='text/css'>

	#calendar {
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
		font-weight:normal;
	}
	div.cal_selector
	{
		margin:15px 0 15px 0;
		text-align:left;
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

	$('select#instrumentOperatorSelector').change(function()
	{
		var selected = $('select#instrumentOperatorSelector :selected').val();
		$('.operatorTimeRemaining').hide();
		var id = "operator_" +selected;
		// alert("set id to " + selected);
		$('#' + id).show();
	});

	// If we have only one instrument operator for this project select it now
	// We will have 2 <option> elements if there is only 1 instrument operator.
	// alert($('select#instrumentOperatorSelector option').length);
	if($('select#instrumentOperatorSelector option').length == 2) {

		$('select#instrumentOperatorSelector option:last').attr("selected", "selected");
		$('select#instrumentOperatorSelector').change();
	}

	// If we have only one payment method for this project select it now
	// We will have 2 <option> elements if there is only 1 payment method
	//alert($('select#paymentMethodSelector_1 option').length);
	if($('select#paymentMethodSelector_1 option').length == 2) {
		
		$('select#paymentMethodSelector_1 option:last').attr("selected", "selected");
	}
	// in case this is visible hide it and reset percent of first payment method to 100%
	hideSecondPaymentMethod();
	
	
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
function getEditBlockDetailsUrl() {
    return "/pr/viewEditBlockDetailsForm.do";
}
function getProjectId() {
	return <bean:write name="projectId"/>;
}
function getInstrumentId() {
	return <bean:write name="instrumentId"/>;
}

function getRequestInformation() {
	var information = {};
	information.requestUrl = "/pr/requestInstrumentTimeAjax.do";
	information.projectId = getProjectId();
	information.instrumentId = getInstrumentId();
	information.instrumentOperatorId = $("#instrumentOperatorSelector :selected").val();
	information.hasPaymentMethodInfo = true;
	information.paymentMethodId1 = $("#paymentMethodSelector_1 :selected").val();
	information.paymentMethod1Perc = $("#paymentMethodPercent_1").val();
    // check if there is a second payment method
	information.paymentMethodId2 = $("#paymentMethodSelector_2 :selected").val();
	information.paymentMethod2Perc = $("#paymentMethodPercent_2").val();


	if (information.instrumentOperatorId == 0) {
		information.errorMessage = "Please select an instrument operator.";
		return information;
	}

    if (information.paymentMethodId1 == 0) {
    	information.errorMessage = "Please select a payment method.";
    	return information;
    }
     			
	if (information.paymentMethodId1 === information.paymentMethodId2) {
		information.errorMessage = "Selected payments methods are the same.";
		return information;
	}
	
	if (information.paymentMethodId2 !== 0 && information.paymentMethod2Perc === 0.0) {
		information.errorMessage = "Percent entered for the second payment method should be greater than 0.";
		return information;
	}
       						
     return information;		
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

function initCalendar() {

	var date = new Date();
	// var d = date.getDate();
	var m = date.getMonth();
	var y = date.getFullYear();
	
	$('#calendar').uwpr_scheduler({
			instrumentId: getInstrumentId(),
			projectId: getProjectId(),
			eventJSONSourceUrl: getEventSourceUrl(),
			onAddEventSuccessFn: addToScheduledTimeTable,
			eventDeleteUrl: getDeleteTimeBlockUrl(),
			eventEditUrl: getEditTimeBlockUrl(),
            eventEditBlockDetailsUrl: getEditBlockDetailsUrl(),
			onDeleteSuccessFn: deleteFromScheduledTimeTable,
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



function deleteFromScheduledTimeTable(usageBlockIds) {
	
	for(var i = 0; i < usageBlockIds.length; i++) {
	
		var usageBlockId = usageBlockIds[i];
		
		//alert("I am deleting: "+usageBlockId);
		
		var row = $("#usage_block_"+usageBlockId);
		if(row == undefined || row.length == 0) {
			//alert("You are removing a time block that was not added in the current session. \"Time Scheduled\" table will not be updated.");
		}
		else {
			//alert("removing from table");
			var fee = $("#fee_"+usageBlockId).text();
			deleteCost(fee.replace("$",""));
			row.remove();
		}
	}
}

function addToScheduledTimeTable (jsonobj) {

	
	$("#scheduledTimeDiv").show();
	var blocks = jsonobj.blocks;
	
	var totalcost = 0.0;
	for (var i = 0; i < blocks.length; i++) {
	
		// alert(block.id);
		var block = blocks[i];
		var row = "<tr id='usage_block_"+block.id+"'>";
		row += "<td style='display:none;'>"+block.id+"</td>";
		row += "<td class='ui-state-default'>"+block.start_date+"</td>";
		row += "<td class='ui-state-default'>"+block.end_date+"</td>";
		row += "<td class='ui-state-default' id='fee_"+block.id+"'>$"+block.fee+"</td>";
		row += "</tr>";
		$("#scheduledTimeDiv table > tbody:last").append(row);
		
		totalcost += parseFloat(block.fee);
		// alert(block.fee+" total: "+totalcost);
		//console.log("adding usage "+$("#scheduledTimeDiv table > tbody:last"));
	}
	
	addCost(totalcost);
}

function addCost(addThis) {

	var currentCost = $("#totalCost").text();
	//alert(currentCost);
	var newCost = parseFloat(currentCost) + parseFloat(addThis);
	
	$("#totalCost").text((newCost).toFixed(2));
	
}

function deleteCost(deleteThis) {

	var currentCost = $("#totalCost").text();
	//console.log(deleteThis);
	var newCost = parseFloat(currentCost) - parseFloat(deleteThis);
	
	$("#totalCost").text((newCost).toFixed(2));
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


function showSecondPaymentMethod() {

	// show the second payment row
	$("#secondPaymentMethodRow").show();
	// hide the link to add a second payment
	$("#addPaymentMethodLinkRow").hide();
	
	// make the percent field for the first payment method editable
	$("#paymentMethodPercent_1").removeAttr("disabled"); 
	
}

function hideSecondPaymentMethod() {
	$("#paymentMethodPercent_1").val(100); // reset percent for payment method 1 to 100%
	$("#paymentMethodPercent_2").val(0); // set the percent for payment method 2 to 0%
	
	// for payment method 1 make the percent field not editable
	$("#paymentMethodPercent_1").attr("disabled", "disabled");
	
	$("#paymentMethodSelector_2").val(0); // deselect the payment method
	
	// hide the second payment row
	$("#secondPaymentMethodRow").hide();
	// show the link to add a second payment
	$("#addPaymentMethodLinkRow").show();
}

function updatePercent() {
	
	if($("#paymentMethodPercent_1").val() == "")
		return;
	var percent1 = parseFloat($("#paymentMethodPercent_1").val());
	if(isNaN(percent1)) {
		alert("Invalid number entered in the percent field. Please enter a number between 0 and 100");
	}
	if(percent1 > 100.0)
		percent1 = 100.0;
	if(percent1 < 0.0)
		percent1 = 0;
	var percent2 = 100.0 - percent1;
	$("#paymentMethodPercent_2").val(Math.round(percent2*100.0)/100.0);
	$("#paymentMethodPercent_1").val(Math.round(percent1*100.0)/100.0);
}

</script>

<yrcwww:contentbox title="Schedule Instrument Time">
<center>

<div class="ui-state-default" style="padding:10px;">

<div style="margin:5px 0 0 0; text-align:center;">
Project ID: <bean:write name="projectId"/>
<span style="text-decoration:underline; font-size:8pt;">
	<html:link action="viewProject.do" paramId="ID" paramName="projectId"> (back to project)</html:link>
</span>
</div>


<!-- Project selector -->
<logic:present name="projects">
	<div class="cal_selector">
	Select project:
	<select id="projectSelector" onchange='switchProject()'>
		<logic:iterate name="projects" id="project">
			<option value='<bean:write name="project" property="ID" />'><bean:write name="project" property="label" /></option>
		</logic:iterate>
	</select>
	</div>
</logic:present>

<!-- Instrument Selector -->
<div class="cal_selector">
	Select instrument:
	<select id="instrumentSelector" onchange='switchInstrument(<bean:write name="projectId" />)'>
		<logic:iterate name="instruments" id="instrument">
			<logic:equal name="instrument" property="active" value="true">
				<option value='<bean:write name="instrument" property="ID" />'><bean:write name="instrument" property="name" /></option>
			</logic:equal>
		</logic:iterate>

	</select>
</div>

<!-- Instrument Operator Selector -->
<div class="cal_selector">
	Select instrument operator:
	<select id="instrumentOperatorSelector">
		<option value='0'>Select</option>
		<logic:iterate name="instrumentOperators" id="instrumentOperator">
			<option value='<bean:write name="instrumentOperator" property="ID" />'>
				<bean:write name="instrumentOperator" property="fullName" />
			</option>
		</logic:iterate>
	</select>
	&nbsp;
	<logic:iterate name="instrumentOperators" id="instrumentOperator">
		<span class="operatorTimeRemaining" id="operator_<bean:write name='instrumentOperator' property='ID'/>" style="display:none; font-size:8pt; color:red">
			<bean:write name="instrumentOperator" property="timeRemaining" /> hrs remaining
			&nbsp;
			<html:link action="viewTimeScheduledForOperator.do" paramId="instrumentOperatorId" paramName="instrumentOperator" paramProperty="ID">[View Details]</html:link>
		</span>
	</logic:iterate>

	<logic:empty name="instrumentOperators">
		<br>
		<span style="color:red;font-size:8pt;">
			There are no mass spec. instrument operators listed on this project.
			Please contact us to add researchers on this project to the list of verified instrument operators.
		</span>
	</logic:empty>
</div>

<!-- Payment Selector -->
<div class="cal_selector">
Select payment method(s):
<!-- User is allowed to use up to two payment methods -->
<table>
<tr>
<td>
UW Budget # / PO Number:
<select id="paymentMethodSelector_1">
<option value='0'>Select</option>
<logic:iterate name="paymentMethods" id="paymentMethod">
	<option value='<bean:write name="paymentMethod" property="id" />'><bean:write name="paymentMethod" property="displayString" /></option>
</logic:iterate>
</select>
</td>
<td>
<input id="paymentMethodPercent_1" type="text" value="100" size="3" maxlength="3" onkeyup="updatePercent()" disabled="disabled" />%
</td>
<td></td>
</tr>

<tr id="addPaymentMethodLinkRow">
	<td colspan="3" align="left">
		<a href="#" onclick="showSecondPaymentMethod(); return false;" style="color:red;font-size:8pt;text-decoration:underline;">[Add a payment method]</a>
	</td>
</tr>

<tr id="secondPaymentMethodRow" style="display:none;">
<td>
UW Budget # / PO Number:
<select id="paymentMethodSelector_2">
<option value='0'>Select</option>
<logic:iterate name="paymentMethods" id="paymentMethod">
	<option value='<bean:write name="paymentMethod" property="id" />'><bean:write name="paymentMethod" property="displayString" /></option>
</logic:iterate>
</select>
</td>
<td>
<input id="paymentMethodPercent_2" type="text" value="0" disabled="disabled" size="3" maxlength="3"/>%
</td>
<td>
	<a href="#" onclick="hideSecondPaymentMethod(); return false;" style="color:red;font-size:8pt;text-decoration:underline;">[Remove]</a>
</td>
</tr>

</table>
</div>
</div>

<div style="margin:10px 0 10px 0; text-align:center;font-weight:bold;color:red;font-size:8pt">
NOTE: If you are scheduling instrument time over a weekend please ensure that you have access to the mass spec. facility. 
</div>
<div style="margin:10px 0 10px 0; text-align:center;font-weight:bold;color:red;font-size:8pt">
Instrument time cannot be deleted less that 48 hours prior to the scheduled start. 
</div>

<!--  
<div style="margin:10px 0 10px 0; text-align:center;">
<a href="#" onclick="refreshCalendar(); return false;" style="color:red;font-size:8pt;text-decoration:underline;">[Refresh Calendar]</a>
</div>
-->

<!-- Calendar and Scheduled Time table -->
<table style="width:850px;">
<tr>
<td style="vertical-align: top;">
<div id='calendar' align="left"></div>
</td>
<td style="width:200px;vertical-align:top;background: none repeat scroll 0 0 transparent;padding:5px;" class="ui-state-default">
	<div id="scheduledTimeDiv">
		<nobr>Time Scheduled:</nobr>
		<table>
			<thead>
				<tr>
					<th style="display:none;">ID</th>
					<th class="ui-state-default">Start</th>
					<th class="ui-state-default">End</th>
					<th class="ui-state-default">Cost</th>
				</tr>
			</thead>
			<tbody style="color:black;">
			</tbody>
		</table>
		<div style="margin-top:10px;">
			<nobr>Total Cost: <span style="color:red;">$</span><span id="totalCost" style="color:red;">0</span></nobr>
		</div>
		<div style="margin-top:10px;color:black;font-size:8pt;font-weight:normal;">
			Click <html:link action="viewTimeScheduledForProject" paramId="projectId" paramName="projectId"><b>here</b></html:link> to view <b>all</b> the time scheduled for this project.
		</div>
		<div style="margin-top:10px;color:black;font-size:8pt;font-weight:normal;">
			<b><a href="/pr/costcenter_resources/UWPR_FAQ_Instrument_scheduling.pdf">View</a></b> the billing FAQ and instructions for scheduling instrument time.
		</div>
		
		<div style="margin-top:10px;color:black;font-size:8pt;font-weight:normal;">
			<a href="/pr/costcenter_resources/UWPR_Current_Rates.xlsx"><b>View</b></a> the current rates for instruments.
		</div>
		
	</div>
</td>
</tr>
</table>
</center>
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>