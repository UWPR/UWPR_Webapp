<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<link rel='stylesheet' type='text/css' href='/pr/css/fullcalendar.css' />

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="paymentMethods">
    <logic:forward name="viewEditBlockPaymentForm" />
</logic:notPresent>

<script type="text/javascript">

var lastPaymentMethodIndex = 0;
<logic:present name="editBlockPaymentForm" property="paymentPercentList">
    <bean:size name="editBlockPaymentForm" property="paymentPercentList" id="paymentMethods_size"/>
        lastPaymentMethodIndex = <bean:write name="paymentMethods_size" />
    // alert(lastPaymentMethodIndex);
</logic:present>

var paymentMethodList = ""; // <option value='0'>Select</option>";
<logic:present name="paymentMethods">
    <logic:iterate name="paymentMethods" id="paymentMethod">
        var id = <bean:write name="paymentMethod" property="id"/>;
        var name = "<bean:write name='paymentMethod' property='displayString' />"
        paymentMethodList += "<option value='"+id+"'>"+name+"</option>";
    </logic:iterate>
</logic:present>

$(document).ready(function(){

    if(lastPaymentMethodIndex > 1)
    {
        $("#addPaymentMethodLink").hide();
    }
});

function removePaymentMethod(rowIdx) {
	// alert("removing payment method at index "+rowIdx);
    $("#paymentMethodRow_"+rowIdx+" select").val(0);
	$("#paymentMethodRow_"+rowIdx).remove();
    lastPaymentMethodIndex--;
    if(lastPaymentMethodIndex == 1)
    {
        $("#addPaymentMethodLink").show();
    }
}

function addPaymentMethod() {

	// alert("last payment method: "+lastPaymentMethodIndex);
	var newRow = "<tr id='paymentMethodRow_"+lastPaymentMethodIndex+"'>";
	newRow += "<td><b>PaymentMethod: </b></td>";
	newRow += "<td>";
	newRow += "<select name='paymentPercentItem["+lastPaymentMethodIndex+"].paymentMethodId'>";
	newRow += paymentMethodList;
	newRow += "</select>";
    newRow += "&nbsp;&nbsp;";
    newRow += "<input type='text' name='paymentPercentItem["+lastPaymentMethodIndex+"].paymentPercent' size='3' maxlength='3' value='0'/>%";
	newRow += " <a href='javascript:removePaymentMethod("+lastPaymentMethodIndex+")' style='color:red; font-size:8pt;'>[Remove]</a>";
	newRow += "</td>";
	newRow +="</tr>";

    $("#paymentMethodRow_"+(lastPaymentMethodIndex-1)).after(newRow);

    lastPaymentMethodIndex++;

    if(lastPaymentMethodIndex == 2)
    {
        $("#addPaymentMethodLink").hide();
    }
    else
    {
        $("#addPaymentMethodLink").show();
    }
}

</script>


<yrcwww:contentbox title="Change Payment Method">
<center>
<html:form name="editBlockPaymentForm" type="org.uwpr.www.scheduler.EditBlockPaymentMethodForm" action="editBlockPaymentMethod" method="POST">
<bean:define name="editBlockPaymentForm" id="editForm" type="org.uwpr.www.scheduler.EditBlockPaymentMethodForm" />

<html:hidden name="editBlockPaymentForm" property="usageBlockIdsToEdit"/>

<table>
<tbody>
<tr>
	<td><b>Project:</b></td>
	<td>
		<yrcwww:projectLink projectId='<%=editForm.getProjectId()%>'/>
		<html:hidden name="editBlockPaymentForm" property="projectId"/>
	</td>
	<td></td>
</tr>
<tr>
	<td><b>Instrument: </b></td>
	<td>
		<bean:write name="editBlockPaymentForm" property="instrumentName"/>
		<html:hidden name="editBlockPaymentForm" property="instrumentName"/>
	</td>
	<td></td>
</tr>

<tr>
	<td><b>Start:</b></td>
	<td>
        <html:hidden name="editBlockPaymentForm" property="startDate"/>
        <html:hidden name="editBlockPaymentForm" property="startTime"/>
        <bean:write name="editBlockPaymentForm" property="startDate"/> &nbsp;
        <bean:write name="editBlockPaymentForm" property="startTime"/>
	</td>
</tr>
<tr>
	<td><b>End:</b></td>
	<td>
        <html:hidden name="editBlockPaymentForm" property="endDate"/>
        <html:hidden name="editBlockPaymentForm" property="endTime"/>
        <bean:write name="editBlockPaymentForm" property="endDate"/> &nbsp;
        <bean:write name="editBlockPaymentForm" property="endTime"/>
	</td>
</tr>


<logic:present name="editBlockPaymentForm" property="paymentPercentList">
<logic:iterate id="paymentPercentItem" name="editBlockPaymentForm" property="paymentPercentList"
               type="org.uwpr.www.scheduler.EditBlockPaymentMethodForm.PaymentPercent" indexId="idx">
    <!-- index is 0-based -->
    <tr id="paymentMethodRow_<%=idx%>">
        <td><b>Payment Method:</b> </td>
        <td>
            <html:hidden name="paymentPercentItem" property="label" indexed="true"/>
            <html:select name="paymentPercentItem" property="paymentMethodId" indexed="true">
                <html:optionsCollection name="paymentMethods" value="id" label="displayString"/>
            </html:select>
            &nbsp;
            <html:text name="paymentPercentItem" property="paymentPercent" indexed="true" size="3" maxlength="3"/>%
            <logic:greaterThan name="idx" value="0">
                <a href='javascript:removePaymentMethod(<%=idx%>)' style='color:red; font-size:8pt;'>[Remove]</a>
            </logic:greaterThan>
        </td>
    </tr>
</logic:iterate>
</logic:present>
<tr>
    <td colspan="2" align="center"><a href="javascript:addPaymentMethod()"><span id="addPaymentMethodLink">Add Payment Method</span></a></td>
</tr>

<tr>
	<td colspan="2" align="center" style="padding-top:20px;">
		<html:submit>Update</html:submit>
		<input type="button" onclick="document.location='/pr/viewScheduler.do?projectId=<bean:write name='editBlockPaymentForm' property='projectId'/>&instrumentId=<bean:write name='editBlockPaymentForm' property='instrumentId'/>'" value="Cancel"/>
	</td>
</tr>
</tbody>
</table>
</html:form>
</center>
</yrcwww:contentbox>
<%@ include file="/includes/footer.jsp"%>