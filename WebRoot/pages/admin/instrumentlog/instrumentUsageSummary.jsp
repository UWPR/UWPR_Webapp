
<%@page import="org.uwpr.instrumentlog.MsInstrumentUsage"%>
<%@page import="org.uwpr.chart.google.DataSet"%>
<%@page import="org.uwpr.chart.google.GoogleApiChartCreator"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/calendar.css" />
<link rel="stylesheet" type="text/css" href="yui/build/fonts/fonts-min.css" />
<link rel="stylesheet" type="text/css" href="yui/build/calendar/assets/calendar.css" />

<%
	MsInstrumentUsage instrumentUsage = (MsInstrumentUsage)session.getAttribute("usage");
	String instrumentName = "Instrument Usage";
	if(instrumentUsage != null)
	{
		instrumentName = instrumentUsage.getInstrumentName();
	}
 %>
<yrcwww:contentbox title="<%=instrumentName %>">
<!-- ================================================================================================ -->
<!-- BEGIN Date Range form -->
<center>
<html:form action="instrumentUsageSummary" method="post">

<div  class="yui-skin-sam" style="width:850px;border: 1px solid #DDDDDD;padding: 10px;margin:0px">
	<input type="hidden" name="instrumentID" value="<bean:write name="usage" property="instrumentID"/>" />
	<b>Start Date&nbsp;&nbsp;</b>
	<label>M: </label> <html:text property="startDateMonth" styleId="startDateMonth" styleClass="month" />
    <label>D:</label> <html:text property="startDateDay" styleId="startDateDay" styleClass="day" />
    <label>Y: </label> <html:text property="startDateYear" styleId="startDateYear" styleClass="year" />
    <span id="cal1"></span>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <b>End Date&nbsp;&nbsp;</b>
	<label>M: </label> <html:text property="endDateMonth" styleId="endDateMonth" styleClass="month" />
    <label>D:</label> <html:text property="endDateDay" styleId="endDateDay" styleClass="day" />
    <label>Y: </label> <html:text property="endDateYear" styleId="endDateYear" styleClass="year" />
    <span id="cal2"></span>
    <br><br>
    <input type="submit" value="Update" onclick="submitForm();return false;">
    
</div>
</html:form>
</center>
<!-- END Date Range form -->
<!-- ================================================================================================ -->

<!-- ================================================================================================ -->
<!-- BEGIN Summary Charts -->
<center>
<div style="width:800px;background-color:#FFFFFF;border: 1px solid #DDDDDD;padding:10px;margin:0px">
<logic:present name="piStats">
<%
	DataSet piStats = (DataSet)(session.getAttribute("piStats"));
	String piStatsChartURL = GoogleApiChartCreator.getPieChartURL(piStats, 330, 160);
%>
<img src="<%=piStatsChartURL%>" align="top" alt="Usage(%) by PI" />
</logic:present>
<logic:present name="monthlyStats">
<% 
	DataSet monthlyStats = (DataSet)(session.getAttribute("monthlyStats"));
	String monthsStatsChartURL = GoogleApiChartCreator.getLineChartURL(monthlyStats, 450, 200);
%>
<img src="<%=monthsStatsChartURL%>" align="top" alt="Usage(%) by Month" />
</logic:present>
</div>
</center>
<!-- END Summary Charts -->
<!-- ================================================================================================ -->

<br><br>

<logic:present name="usage" scope="session">

	<yrcwww:contentbox title="Usage Details" innerBox="true">
	<div style="font-weight:bold;">
		Usage: <bean:write name="usage" property="numHoursUsed"/> / <bean:write name="usage" property="hoursInRange"/> hours (<bean:write name="usage" property="percentUsed"/>%)
	</div>
	<table align="center" cellpadding="3" width="100%" class="striped">
		<thead>
		<tr>
			<th><b><font style="font-size:9pt;"><html:link href="/pr/sortInstrumentUsage.do?sortby=ProjectID">ID</html:link></font></b></th>
			<th><b><font style="font-size:9pt;"><html:link href="/pr/sortInstrumentUsage.do?sortby=Title">Title</html:link></font></b></th>
			<th><b><font style="font-size:9pt;"><html:link href="/pr/sortInstrumentUsage.do?sortby=PI">PI</html:link></font></b></th>
			<th><b><font style="font-size:9pt;"><html:link href="/pr/sortInstrumentUsage.do?sortby=Usage">Usage</html:link></font></b></th>
			<th><b><font style="font-size:9pt;">Dates</font></b></th>
		</tr>
		</thead>
		
		<tbody>		
		<logic:iterate name="usage" property="projectUsageList" id="projectUsage">
		<tr>
			<td><html:link href="/pr/viewProject.do" paramId="ID" paramName="projectUsage" paramProperty="projectID">
				<bean:write name="projectUsage" property="projectID" />
			</html:link></td>
			<td><bean:write name="projectUsage" property="projectTitle" /></td>
			<td><html:link href="/pr/viewResearcher.do" paramId="id" paramName="projectUsage" paramProperty="PIID"><bean:write name="projectUsage" property="projectPI" /></html:link></td>
			<td><bean:write name="projectUsage" property="numHoursUsed"/> hours<br>(<bean:write name="projectUsage" property="percentUsed"/>%)</td>
			<td>
			<logic:iterate name="projectUsage" property="allUsageBlocks" id="usageBlock">
				<a href="/pr/viewInstrumentUsageForm.do?usageID=<bean:write name="usageBlock" property="ID" />">
				<font style="font-size:8pt;"><bean:write name="usageBlock" property="startDateFormated" /> - <bean:write name="usageBlock" property="endDateFormated" /></font></a>
				<br>
			</logic:iterate>
			</td>
		</tr>
		</logic:iterate>
		</tbody>
	</table>
	
	<div style="font-weight:bold;font-size:9pt;text-align:center;">
		<html:link href="/pr/viewAllInstrumentCalendar.do" >View Calendar</html:link>
        &nbsp;
        &nbsp;
        <a href="/pr/viewTimeScheduledForInstrument.do?instrumentId=<%=instrumentUsage.getInstrumentID()%>">List Usage</a>
	</div>
</yrcwww:contentbox>
</logic:present>

</yrcwww:contentbox>

<script type="text/javascript">

function submitForm() {
	
	// validate the numbers entered in the date fields
	var startDate = parseInt(document.dateRangeForm.startDateDay.value);
	var startMonth = parseInt(document.dateRangeForm.startDateMonth.value);
	var startYear = parseInt(document.dateRangeForm.startDateYear.value);
	if(!validateInt(startDate, 1,31)) {
		alert("Invalid Start Date (day): "+startMonth+"/"+startDate+"/"+startYear);
		return false;
	}
	if(!validateInt(startMonth, 1, 12)) {
		alert("Invalid Start Date (month): "+startMonth+"/"+startDate+"/"+startYear);
		return false;
	}
	if(!validateInt(startYear, 1900, 3000)) {
		alert("Invalid Start Date (year): "+startMonth+"/"+startDate+"/"+startYear);
		return false;
	}
	
	var endDate = parseInt(document.dateRangeForm.endDateDay.value);
	var endMonth = parseInt(document.dateRangeForm.endDateMonth.value);
	var endYear = parseInt(document.dateRangeForm.endDateYear.value);
	if(!validateInt(endDate, 1,31)) {
		alert("Invalid End Date (day): "+endMonth+"/"+endDate+"/"+endYear);
		return false;
	}
	if(!validateInt(endMonth, 1, 12)) {
		alert("Invalid End Date (month): "+endMonth+"/"+endDate+"/"+endYear);
		return false;
	}
	if(!validateInt(endYear, 1900, 3000)) {
		alert("Invalid End Date (year): "+endMonth+"/"+endDate+"/"+endYear);
		return false;
	}
	
	// make sure start date is less that the end date
	var valid = true;
	if(endYear < startYear) valid = false;
	else if(endYear == startYear) {
		if(endMonth < startMonth) {
			valid = false;
		}
		
		else if(endMonth == startMonth) {
			if(endDate < startDate)	{
				valid = false;
			}
		}
	}
	if(!valid) {
		alert("Start Date cannot be later than End Date");
		return false;
	}
	
	document.dateRangeForm.submit();
}


function validateInt(value, min, max) {
	var intVal = parseInt(value);
	var valid = true;
	if(isNaN(intVal))						valid = false;
	if(valid && intVal < min)				valid = false;
	if(max && (valid && intVal > max))		valid = false;
	
	return valid;
}

</script>

<script type="text/javascript" src="yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="yui/build/dom/dom-min.js"></script>
<script type="text/javascript" src="yui/build/event/event-min.js"></script>
<script type="text/javascript" src="yui/build/calendar/calendar-min.js"></script>
<script type="text/javascript" src="yui/build/container/container_core-min.js"></script>
<script type="text/javascript" src="yui/build/element/element-beta-min.js"></script>
<script type="text/javascript" src="yui/build/button/button-min.js"></script>

<script type="text/javascript">

YAHOO.util.Event.onDOMReady(function () {
		function onButtonClick1() {
            /*
                 Create an empty body element for the Overlay instance in order 
                 to reserve space to render the Calendar instance into.
            */
            oCalendarMenu1.setBody("&#32;");
            oCalendarMenu1.body.id = "calendarcontainer1";
            // Render the Overlay instance into the Button's parent element
            oCalendarMenu1.render(this.get("container"));
            // Align the Overlay to the Button instance
            oCalendarMenu1.align();
            /*
                 Create a Calendar instance and render it into the body 
                 element of the Overlay.
            */
            var oCalendar = new YAHOO.widget.Calendar("buttoncalendar1", oCalendarMenu1.body.id);
            oCalendar.render();
            /* 
                Subscribe to the Calendar instance's "changePage" event to 
                keep the Overlay visible when either the previous or next page
                controls are clicked.
            */
            oCalendar.changePageEvent.subscribe(function () {
                window.setTimeout(function () {
                    oCalendarMenu1.show();
                }, 0);
            });
            /*
                Subscribe to the Calendar instance's "select" event to 
                update the month, day, year form fields when the user
                selects a date.
            */
            oCalendar.selectEvent.subscribe(function (p_sType, p_aArgs) {
                var aDate;
                if (p_aArgs) {
                    aDate = p_aArgs[0][0];
                    YAHOO.util.Dom.get("startDateMonth").value = aDate[1];
                    YAHOO.util.Dom.get("startDateDay").value = aDate[2];
                    YAHOO.util.Dom.get("startDateYear").value = aDate[0];
                }
                oCalendarMenu1.hide();
            });
            /*
                 Unsubscribe from the "click" event so that this code is 
                 only executed once
            */
            this.unsubscribe("click", onButtonClick1);
        }

		function onButtonClick2() {
            /*
                 Create an empty body element for the Overlay instance in order 
                 to reserve space to render the Calendar instance into.
            */
            oCalendarMenu2.setBody("&#32;");
            oCalendarMenu2.body.id = "calendarcontainer2";
            // Render the Overlay instance into the Button's parent element
            oCalendarMenu2.render(this.get("container"));
            // Align the Overlay to the Button instance
            oCalendarMenu2.align();
            /*
                 Create a Calendar instance and render it into the body 
                 element of the Overlay.
            */
            var oCalendar = new YAHOO.widget.Calendar("buttoncalendar2", oCalendarMenu2.body.id);
            oCalendar.render();
            /* 
                Subscribe to the Calendar instance's "changePage" event to 
                keep the Overlay visible when either the previous or next page
                controls are clicked.
            */
            oCalendar.changePageEvent.subscribe(function () {
                window.setTimeout(function () {
                    oCalendarMenu2.show();
                }, 0);
            });
            /*
                Subscribe to the Calendar instance's "select" event to 
                update the month, day, year form fields when the user
                selects a date.
            */
            oCalendar.selectEvent.subscribe(function (p_sType, p_aArgs) {
                var aDate;
                if (p_aArgs) {
                    aDate = p_aArgs[0][0];
                    YAHOO.util.Dom.get("endDateMonth").value = aDate[1];
                    YAHOO.util.Dom.get("endDateDay").value = aDate[2];
                    YAHOO.util.Dom.get("endDateYear").value = aDate[0];
                }
                oCalendarMenu2.hide();
            });
            /*
                 Unsubscribe from the "click" event so that this code is 
                 only executed once
            */
            this.unsubscribe("click", onButtonClick2);
        }

        // Create an Overlay instance to house the Calendar instance
        var oCalendarMenu1 = new YAHOO.widget.Overlay("calendarmenu1");
        var oCalendarMenu2 = new YAHOO.widget.Overlay("calendarmenu2");

        // Create a Button instance of type "menu"
        var oButton1 = new YAHOO.widget.Button({ 
                                            type: "menu", 
                                            id: "calendarpicker1", 
                                            label: "", 
                                            menu: oCalendarMenu1, 
                                            container: "cal1" });
                                            
        var oButton2 = new YAHOO.widget.Button({ 
                                            type: "menu", 
                                            id: "calendarpicker2", 
                                            label: "", 
                                            menu: oCalendarMenu2, 
                                            container: "cal2" });
        /*
            Add a "click" event listener that will render the Overlay, and 
            instantiate the Calendar the first time the Button instance is 
            clicked.
        */
        oButton1.on("click", onButtonClick1);
        oButton2.on("click", onButtonClick2);
    
    });

</script>
<%@ include file="/includes/footer.jsp"%>