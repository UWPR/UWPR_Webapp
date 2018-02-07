<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/tablesorter.css">
<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.tablesorter.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>


<script>

$(document).ready(function() {

    $( ".datepicker" ).datepicker();
	// extend the default setting to always include the zebra widget. 
    $.tablesorter.defaults.widgets = ['zebra']; 
    // extend the default setting to always sort on the second (Instrument) column 
    // column indexes are 0-based.
    $.tablesorter.defaults.sortList = [[1,0]]; 
    $("#blocklist_table").tablesorter({
    	headers: { 
            // assign the third column (we start counting zero) 
            3: {
                // disable it by setting the property sorter to false 
                sorter: false 
            }, 
            // assign the eleventh column (we start counting zero) 
            11: {
                // disable it by setting the property sorter to false 
                sorter: false 
            } 
        } 
    });
}); 

function submitForm()
{
    var projectId = $("#projectId").val();
    var instrumentId = $("#instrumentId").val();
    var paymentMethodId = $("#paymentMethodId").val();
    var instrumentOperatorId = $("#instrumentOperatorId").val();
    var startDate = $("#startDate").val();
    var endDate = $("#endDate").val();

    var url = location.pathname;
    url += "?projectId=" + projectId +
            "&instrumentId=" + instrumentId +
            "&paymentMethodId=" + paymentMethodId +
            "&instrumentOperatorId=" + instrumentOperatorId +
            "&startDate=" + startDate + "&endDate=" + endDate;

    // alert("Changing URL to " + url);
    window.location.href = url;
}

function markBlockDeleted(usageBlockId, projectId) {

	if(confirm("Are you sure you want to delete this time block?")) {
          document.location.href="/pr/deleteInstrumentTime.do?usageBlockId=" + usageBlockId+"&projectId="+projectId;
          return true;
    }
}

function deleteTimeBlock(usageBlockId, projectId) {

    if(confirm("Are you sure you want to delete this time block and the sign-up?")) {
        document.location.href="/pr/deleteInstrumentTime.do?deleteSignup=true&usageBlockId=" + usageBlockId+"&projectId="+projectId;
        return true;
    }
}

   function exportBillingInformation(summarized)
   {
       var startDate = $("#startDate").val();
       var endDate = $("#endDate").val();
       if(!startDate || !endDate)
       {
            alert("Please enter start and end dates.");
           return false;
       }
       var url = "/pr/exportProjectBillingInformation.do?projectId=" + $("#projectId").val() + "&startDate=" + startDate + "&endDate="+endDate;
       if(summarized)
       {
           url += "&summarize=true";
       }
       document.location.href = url;
       return false;
   }

</script>

<logic:notPresent name="instruments">
	<logic:forward name="viewTimeScheduledForProject"/>
</logic:notPresent>

<yrcwww:contentbox title="Scheduled Instrument Time for Project">
<center>

<logic:present name="noInstrumentTimeScheduled">
    <div style="margin:20px;">
        There is no instrument time scheduled for project ID <bean:write name="project" property="ID"/>.
        <br/>
        Click <html:link action="viewScheduler.do" paramId="projectId" paramName="project" paramProperty="ID">here</html:link> to schedule time for this project.
        </div>
</logic:present>


<logic:notPresent name="noInstrumentTimeScheduled">


<div style="font-weight:bold; text-alignment:center;">
    Project ID: <bean:write name="project" property="ID"/>
    <html:link action="viewProject.do" paramName="project" paramProperty="ID" paramId="ID" >
        <span style="font-size:8pt;">(back to project)</span>
    </html:link>
    <br/>
    <bean:write name="project" property="title"/>
</div>

<div style="margin:20px; text-align:left; align:center">
<html:form action="viewTimeScheduledForProject" method="POST">
    <html:hidden property="projectId" styleId="projectId"/>
    <table align="center">
        <tr>
            <td>
                Instrument:
            </td>
            <td>
                <html:select property="instrumentId" styleId="instrumentId">
                    <html:option value="0">ALL</html:option>
                    <html:optionsCollection name="instruments" value="ID" label="name"/>
                </html:select>
            </td>
            <td>
                Payment method:
            </td>
            <td>
                <html:select property="paymentMethodId" styleId="paymentMethodId">
                    <html:option value="0">ALL</html:option>
                    <html:optionsCollection name="paymentMethods" value="id" label="displayString"/>
                </html:select>
            </td>
        </tr>

        <tr>
            <td align="left">Start date:</td>
            <td>
                <html:text property="startDateString" styleClass="datepicker" styleId="startDate"></html:text>
                <span style="font-size:8pt;">e.g. 04/29/2011</span>
            </td>
            <td>
                Instrument operator:
            </td>
            <td>
                <html:select property="instrumentOperatorId" styleId="instrumentOperatorId">
                    <html:option value="0">ALL</html:option>
                    <html:optionsCollection name="instrumentOperators" value="ID" label="fullName"/>
                </html:select>
            </td>
        </tr>
        <tr>
            <td align="left">End date:</td>
            <td>
                <html:text property="endDateString" styleClass="datepicker" styleId="endDate"></html:text>
                <span style="font-size:8pt;">e.g. 04/29/2011</span>
            </td>
        </tr>
        <tr>
            <td colspan="4" align="center">
                <input type="submit" value="Update" onclick="submitForm();return false;">
            </td>
        </tr>
    </table>
</html:form>
</div>

<logic:empty name="usageBlocks">
    <div style="margin:20px;">
        No scheduled instrument time was found for project ID
        <bean:write name="project" property="ID"/>
        with the selected criteria.
    </div>
</logic:empty>


<logic:notEmpty name="usageBlocks">
<div style="font-weight:bold; text-alignment:center;margin:10px;">
	Total cost: $<span style="color:red;"><bean:write name="totalCost"/></span>
    &nbsp;
    Setup cost: $<span style="color:red;"><bean:write name="setupCost"/></span>
    &nbsp;
    Sign-up cost: $<span style="color:red;"><bean:write name="signupCost"/></span>
    &nbsp;
    Instrument cost: $<span style="color:red;"><bean:write name="instrumentCost"/></span>
    <br>
    Instrument hours: <span style="color:red;"><bean:write name="instrumentHours"/></span>
    &nbsp;
    sign-up hours: <span style="color:red;"><bean:write name="signupHours"/></span>
</div>
    <div style="margin-top:10px;margin-bottom:10px;">
        Export billing information: <a href="" onclick="return exportBillingInformation()">[Detailed]</a>&nbsp;<a href="" onclick="return exportBillingInformation(true)">[Summarized]</a>
    </div>
<div style="font-weight:bold; text-alignment:center; font-size:8pt">
	<html:link action="viewScheduler.do" paramName="project" paramProperty="ID" paramId="projectId">
		[Schedule Time for Project]
	</html:link>
</div>

<table id="blocklist_table" class="tablesorter" border="0" cellpadding="7">
	<thead>
		<tr>
			<th class="scheduler">ID</th>
			<th class="scheduler">Instrument</th>
            <th class="scheduler">Operator</th>
			<th class="scheduler">Payment<br/>Method(s)</th>
			<th class="scheduler">Start</th>
			<th class="scheduler">End</th>
            <th class="scheduler">Setup</th>
            <th class="scheduler">SignUp</th>
            <th class="scheduler">Instrument</th>
			<th class="scheduler">Total</th>
			<th class="scheduler">Billed</th>
			<th class="scheduler"></th>
		</tr>
	</thead>
	
	<tbody>
	
		<logic:iterate name="usageBlocks" id="usageBlock">
		
			<tr>
				<td><bean:write name="usageBlock" property="ID"/></td>
				<td><bean:write name="usageBlock" property="instrumentName"/></td>
                <td><bean:write name="usageBlock" property="operatorName"/></td>
				<td>
					<logic:notEmpty name="usageBlock" property="payments">
						<ul>
						<logic:iterate name="usageBlock" property="payments" id="payment">
							<li>
								<nobr>
								<a href="/pr/viewPaymentMethod.do?paymentMethodId=<bean:write name='payment' property='paymentMethod.id'/>&projectId=<bean:write name='project' property='ID'/>">
								<bean:write name="payment" property="paymentMethod.shortDisplayString" /></a>
								&nbsp;
								<bean:write name="payment" property="percent" />%
								</nobr>
							</li>
						</logic:iterate>
						</ul>
					</logic:notEmpty>
				</td>
				<td><nobr><bean:write name="usageBlock" property="startDateFormated"/></nobr></td>
				<td><nobr><bean:write name="usageBlock" property="endDateFormated"/></nobr></td>
                <td align="right"><span class="costColumn"><bean:write name="usageBlock" property="setupCost"/></span></td>
                <td align="right"><span class="costColumn"><bean:write name="usageBlock" property="signupCost"/></span></td>
                <td align="right"><span class="costColumn"><bean:write name="usageBlock" property="instrumentCost"/></span></td>
				<td align="right"><span class="costColumn"><bean:write name="usageBlock" property="totalCost"/></span></td>
				<td>
					<logic:empty name="usageBlock" property="invoiceDate">
					-
					</logic:empty>
					<logic:notEmpty name="usageBlock" property="invoiceDate">
						<bean:write name="usageBlock" property="invoiceDateFormatted"/>
					</logic:notEmpty>
				</td>
				<td style="font-size:10pt;color:red">
                    <logic:equal name="usageBlock" property="deleted" value="false">
					  <a href="#" onclick='markBlockDeleted(<bean:write name="usageBlock" property="ID" />, <bean:write name="project" property="ID" />)'>[Delete]</a>
                    </logic:equal>
                    <a href='viewEditBlockDetailsForm.do?projectId=<bean:write name="project" property="ID" />&instrumentId=<bean:write name="usageBlock" property="instrumentID" />&usageBlockIds=<bean:write name="usageBlock" property="ID" />'>[Edit]</a>
                    <yrcwww:member group="administrator">
                        <br>
                        <a href="#" onclick='deleteTimeBlock(<bean:write name="usageBlock" property="ID" />, <bean:write name="project" property="ID" />)'>[Purge]</a>
                    </yrcwww:member>
				</td>
				
			</tr>
		</logic:iterate>
	</tbody>
	
</table>
</logic:notEmpty>

</center>
</logic:notPresent>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>