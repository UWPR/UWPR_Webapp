<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<link rel='stylesheet' type='text/css' href='/pr/css/jquery_ui/ui-lightness/jquery-ui-1.8.12.custom.css' />
<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery-ui-1.8.12.custom.min.js'></script>
<script>
	$(document).ready(function() {$(".datepicker").datepicker();});
</script>

<yrcwww:contentbox title="Add New Payment Method">
<center>
<html:form action="saveNewPaymentMethod.do" method="POST">
<html:hidden name="paymentMethodForm" property="worktagAllowed"/>
<html:hidden name="paymentMethodForm" property="uwbudgetAllowed"/>
<html:hidden name="paymentMethodForm" property="ponumberAllowed"/>

<table border="0" cellpadding="7" class="striped">

	<tr>
	<tr>
		<td><b>Project ID:</b></td>
		<td>
			<b><bean:write name="paymentMethodForm" property="projectId"/></b>
			<html:hidden property="projectId"/>
		</td>
	</tr>

	<logic:equal name="paymentMethodForm" property="worktagAllowed" value="true">
	<tr>
		<td><bold>Worktag <span style="color:red;">(required)</span>:</bold></td>
		<td>
			<html:text  property="worktag"/> <span style="font-size:10px;">format: [GR|GF|PG|CC|SAG]#####. Example: GF101001</span>
		</td>
	</tr>
	<tr>
		<td>
			Worktag Name:
		</td>
		<td><html:text  property="paymentMethodName" size="40" /></td>
	</tr>
	<tr>
		<td>Resource Worktag:</td>
		<td>
			<html:text  property="resourceWorktag"/> <span style="font-size:10px;">format: RS######. <span style="color:red;">Required for CC worktags</span></span>
		</td>
	</tr>
	<tr>
		<td>Resource Worktag Description:</td>
		<td>
			<html:text  property="resourceWorktagDescr"/>
		</td>
	</tr>
	<tr>
		<td>Assignee Worktag:</td>
		<td>
			<html:text  property="assigneeWorktag"/> <span style="font-size:10px;">format: AS######</span>
		</td>
	</tr>
	<tr>
		<td>Assignee Worktag Description:</td>
		<td>
			<html:text  property="assigneeWorktagDescr"/>
		</td>
	</tr>
	<tr>
		<td>Activity Worktag:</td>
		<td>
			<html:text  property="activityWorktag"/> <span style="font-size:10px;">format: AC######</span>
		</td>
	</tr>
	<tr>
		<td>Activity Worktag Description:</td>
		<td>
			<html:text  property="activityWorktagDescr"/>
		</td>
	</tr>
	<tr>
		<td><bold>Expiration Date: <span style="color:red;">(required)</span></bold></td>
		<td><html:text  property="budgetExpirationDateStr" styleClass="datepicker"/> <span style="font-size:10px;">format: MM/dd/yyyy</span></td>
	</tr>
	</logic:equal>

	<logic:equal name="paymentMethodForm" property="uwbudgetAllowed" value="true">
	<tr>
		<td>UW Budget Number:</td>
		<td>
			<html:text  property="uwBudgetNumber"/> <span style="font-size:10px;">format: 00-0000</span>
		</td>
	</tr>
	<tr>
		<td>
			Budget Name:
		</td>
		<td><html:text  property="paymentMethodName" size="40" /></td>
	</tr>
	<tr>
		<td><bold>Expiration Date: <span style="color:red;">(required)</span></bold></td>
		<td><html:text  property="budgetExpirationDateStr" styleClass="datepicker"/> <span style="font-size:10px;">format: MM/dd/yyyy</span></td>
	</tr>
	</logic:equal>

	<logic:equal name="paymentMethodForm" property="ponumberAllowed" value="true">
	<tr>
		<td>PO Number:</td>
		<td>
			<html:text  property="poNumber"/>
			<br/>
			<span style="font-weight:bold;">
				<html:link href="pages/admin/costcenter/paymentInformation.jsp">Payment Information</html:link>
			</span>
		</td>
	</tr>
	<tr>
		<td>Amount:</td>
		<td><html:text property="poAmount"/></td>
	</tr>
	<tr>
		<td>
			Payment Method Name:
		</td>
		<td><html:text  property="paymentMethodName" size="40" /></td>
	</tr>
	</logic:equal>

   <tr>
   		<td>Federal Funding:</td>
   		<td colspan="4">
   			<html:checkbox property="federalFunding"></html:checkbox>
   			<br/>
   			<span style="color:red; font-size:10px;">
   				Please check this box if the chosen payment method is federally funded.
   			</span>
   		</td>
   </tr>
   <tr>
   		<td colspan="5" style="color:red;font-size:10pt;">
   			Please provide contact information of the person responsible for accounting and billing.
   		</td>
   </tr>
   <tr>
   		<td>First Name:</td>
   		<td colspan="4">
   			<html:text  property="contactFirstName" size="40" />
   		</td>
   	</tr>
   	<tr>
   		<td>Last Name:</td>
   		<td colspan="4">
   			<html:text  property="contactLastName" size="40" />
   		</td>
   	</tr>
   	<tr>
   		<td>Email:</td>
   		<td colspan="4">
   			<html:text  property="contactEmail" size="40" />
   		</td>
   	</tr>
   	<tr>
   		<td>Phone:</td>
   		<td colspan="4">
   			<html:text  property="contactPhone"/>
   		</td>
   	</tr>
   	<tr>
   		<td>Organization:</td>
   		<td colspan="4">
   			<html:text  property="organization" size="40"/>
   		</td>
   	</tr>
   	<tr>
   		<td>Address Line 1:</td>
   		<td colspan="4">
   			<html:text  property="addressLine1" size="80"/>
   		</td>
   	</tr>
   	<tr>
   		<td>Address Line 2:</td>
   		<td colspan="4">
   			<html:text  property="addressLine2" size="80"/>
   		</td>
   	</tr>
   	<tr>
   		<td>City:</td>
   		<td colspan="4">
   			<html:text  property="city"/>
   		</td>
   	</tr>
   	<tr>
   		<td>State:</td>
   		<td colspan="4">
   			<html:select property="state">
	     		<html:option value="No">If in US, choose state:</html:option>
	     		<html:options collection="states" property="code" labelProperty="name"/>
	    	</html:select>
   		</td>
   	</tr>
   	<tr>
   		<td>Zip/Postal Code:</td>
   		<td colspan="4">
   			<html:text property="zip" size="20" maxlength="255"/>
   		</td>
   	</tr>
   	<tr>
   		<td>Country:</td>
   		<td colspan="4">
   			<html:select property="country">
	     		<html:options collection="countries" property="code" labelProperty="name"/>
	    	</html:select>
   		</td>
   	</tr>
   	
   <tr>
   		<td colspan="6" align="center">
   			<html:submit>Save</html:submit>
   			<input type="button" onclick="document.location='/pr/viewProject.do?ID=<bean:write name='paymentMethodForm' property='projectId'/>'" value="Cancel"/>
   		</td>
   		
   </tr>
   </tbody>
   
</table>

</html:form>

</center>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>