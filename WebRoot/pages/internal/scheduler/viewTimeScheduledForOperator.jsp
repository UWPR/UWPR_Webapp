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
            // assign the seventh column (we start counting zero) 
            10: {
                // disable it by setting the property sorter to false 
                sorter: false 
            } 
        } 
    });
    
    // $("#totalCost").text(getTotalCost());
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

function deleteTimeBlock(usageBlockId, projectId) {

	if(confirm("Are you sure you want to delete this time block?")) {
          document.location.href="/pr/deleteInstrumentTime.do?usageBlockId=" + usageBlockId+"&projectId="+projectId;
          return true;
    }
}

</script>

<logic:notPresent name="instruments">
	<logic:forward name="viewTimeScheduledForOperator"/>
</logic:notPresent>

<yrcwww:contentbox title="Scheduled Instrument Time for Instrument Operator">
<center>

<logic:present name="noInstrumentTimeScheduled">
    <div style="margin:20px;">
        There is no instrument time scheduled for instrument operator <bean:write name="instrumentOperator" property="researcher.fullName"/>.
    </div>
</logic:present>


<logic:notPresent name="noInstrumentTimeScheduled">

<div style="margin:20px; text-align:left; align:center">
<html:form action="viewTimeScheduledForOperator" method="POST">
    <html:hidden property="instrumentOperatorId" styleId="instrumentOperatorId"/> <!-- This comes from the form -->
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
        </tr>
        <tr>
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
            <td>
                Project:
            </td>
            <td colspan="3">
                <html:select property="projectId" styleId="projectId">
                    <html:option value="0">ALL</html:option>
                    <html:optionsCollection name="projects" value="ID" label="title"/>
                </html:select>
            </td>
        </tr>

        <tr>
            <td align="left">Start date:</td>
            <td>
                <html:text property="startDateString" styleClass="datepicker" styleId="startDate"></html:text>
                <span style="font-size:8pt;">e.g. 04/29/2011</span>
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
        No scheduled instrument time was found for <bean:write name="instrumentOperator" property="researcher.fullName"/>
        with the selected filtering criteria.
    </div>
</logic:empty>


<logic:notEmpty name="usageBlocks">
<div style="font-weight:bold; text-alignment:center;margin:10px;">
	Total cost: $<span style="color:red;"><bean:write name="totalCost"/></span>
    &nbsp;
    Total hours: <span style="color:red;"><bean:write name="totalHours"/></span>
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
                                    <a href="/pr/viewPaymentMethod.do?paymentMethodId=<bean:write name='payment' property='paymentMethod.id'/>&projectId=<bean:write name='usageBlock' property='projectID'/>">
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
					<a href="#" onclick='deleteTimeBlock(<bean:write name="usageBlock" property="ID" />, <bean:write name="usageBlock" property="projectID" />)'>[Delete]</a>
                    <yrcwww:member group="administrator">
                        <br>
                        <a href="#" onclick='deleteTimeBlock(<bean:write name="usageBlock" property="ID" />, <bean:write name="usageBlock" property="projectID" />)'>[Purge]</a>
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