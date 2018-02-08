<%@page import="org.uwpr.chart.google.DataSet"%>
<%@page import="org.uwpr.chart.google.GoogleApiChartCreator"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/tablesorter.css">
<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.tablesorter.min.js'></script>

<script>

$(document).ready(function() { 
	// extend the default setting to always include the zebra widget. 
    $.tablesorter.defaults.widgets = ['zebra']; 
    // extend the default setting to always sort, DESC, on the first (ID) column
    // column indexes are 0-based.
    $.tablesorter.defaults.sortList = [[0,1]];
    $("#blocklist_table").tablesorter({
    	headers: { 
            // assign the seventh column (we start counting zero) 
            6: { 
                // disable it by setting the property sorter to false 
                sorter: false 
            } 
        } 
    }); 
});

function confirmDelete(timeBlockId) {
   if(confirm("Are you sure you want to delete this entry?")) {
   		document.location.href="/pr/deleteTimeBlock.do?timeBlockId=" + timeBlockId;
    	return 1;
   }
}

function addTimeBlock() {
	document.location.href="/pr/viewTimeBlockForm.do";
}

</script>

<yrcwww:contentbox title="Time Blocks">
<center>

<table border="0" cellpadding="7" id="blocklist_table" class="tablesorter">
	<thead>
		<tr>
			<th><span style="padding:0 10 0 0;">ID</span></th>
			<th><span style="padding:0 10 0 0;">Name</span></th>
			<th><span style="padding:0 10 0 0;"># Hours</span></th>
			<th><span style="padding:0 10 0 0;">Start Time</span></th>
			<th><span style="padding:0 10 0 0;">End Time</span></th>
			<th><span style="padding:0 10 0 0;">Created</span></th>
			<th></th>
		</tr>
	</thead>
	
	<tbody>
	
		<logic:iterate name="timeBlocks" id="block">
		
			<tr>
				<td><bean:write name="block" property="id"/></td>
				<td><bean:write name="block" property="name"/></td>
				<td><bean:write name="block" property="numHours"/></td>
				<td><bean:write name="block" property="startTimeString"/></td>
				<td><bean:write name="block" property="endTimeString"/></td>
				<td><bean:write name="block" property="createDateString"/></td>
				<td>
					<a href="#" onclick="confirmDelete('<bean:write name="block" property="id" />'); return false;">
						<span style="color:red">[Delete]</span>
					</a>
				</td>
			</tr>
		</logic:iterate>
	</tbody>
	
</table>

<div style="margin:20px">
	<input type="button" value="Add New Block" onclick="addTimeBlock();"/>
</div>

</center>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>