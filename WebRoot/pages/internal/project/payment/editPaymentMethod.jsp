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
    function backToProject() {
	   document.location='/pr/viewProject.do?ID='+<bean:write name="paymentMethodForm" property="projectId"/>
    }
</script>

<yrcwww:contentbox title="Edit Payment Method">
<center>

<logic:equal name="paymentMethodForm" property="editable" value="false">
	<div style="font-size:8pt; font-weight:bold; color:red; margin:10px 0 10px 0;">
	
		This payment method is already in use. You may not change the worktag / budget number / PO number at this time.
	</div>
</logic:equal>


<html:form action="savePaymentMethod.do" method="POST">

<html:hidden name="paymentMethodForm" property="worktagAllowed"/>
<html:hidden name="paymentMethodForm" property="uwbudgetAllowed"/>
<html:hidden name="paymentMethodForm" property="ponumberAllowed"/>

<logic:equal name="paymentMethodForm" property="worktagAllowed" value="true">
	<bean:define id="paymentMethodFormTitle">Worktag</bean:define>
</logic:equal>
<logic:equal name="paymentMethodForm" property="uwbudgetAllowed" value="true">
	<bean:define id="paymentMethodFormTitle">Budget Name</bean:define>
</logic:equal>
<logic:equal name="paymentMethodForm" property="ponumberAllowed" value="true">
	<bean:define id="paymentMethodFormTitle">PO Name</bean:define>
</logic:equal>

<table border="0" cellpadding="7" class="striped">

	<tr>
		<td><b>Project ID:</b></td>
		<td>
			<b><bean:write name="paymentMethodForm" property="projectId"/></b>
			<html:hidden property="projectId"/>
			<html:hidden property="paymentMethodId"/>
		</td>
	</tr>

		<logic:notEmpty name="paymentMethodForm" property="worktag">
			<tr>
				<td><bold>Worktag <span style="color:red;">(required)</span>:</bold></td>
				<td>
				<logic:equal name="paymentMethodForm" property="editable" value="true">
					<html:text  property="worktag"/> <span style="font-size:10px;">format: [GR|GF|PG|CC|SAG]#####. Example: GF101001</span>
				</logic:equal>
				<logic:equal name="paymentMethodForm" property="editable" value="false">
					<bean:write name="paymentMethodForm" property="worktag" />
					<html:hidden  name="paymentMethodForm" property="worktag" />
				</logic:equal>
				</td>
			</tr>
		<tr>
			<td>Worktag Name:</td>
			<td>
				<html:text  property="paymentMethodName"/>
			</td>
		</tr>
		<tr>
			<td>Resource Worktag:</td>
			<td>
				<html:text  property="resourceWorktag"/> <span style="font-size:10px;">format: RS######. <span style="color:red;">Required for PG, CC and some SAG, GR worktags</span></span>
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
		</logic:notEmpty>


		<logic:notEmpty name="paymentMethodForm" property="uwBudgetNumber">
		<tr>
			<td>UW Budget Number:</td>
			<td>
			    <logic:equal name="paymentMethodForm" property="editable" value="true">
					<html:text  property="uwBudgetNumber" /> <span style="font-size:10px;">format: 00-0000</span>
				</logic:equal>
			    <logic:equal name="paymentMethodForm" property="editable" value="false">
					<bean:write name="paymentMethodForm" property="uwBudgetNumber" />
					<html:hidden  name="paymentMethodForm" property="uwBudgetNumber" />
				</logic:equal>
			</td>
		</tr>

		<tr>
			<td>Budget Name:</td>
			<td colspan="4">
				<html:text  property="paymentMethodName" size="40" />
			</td>
		</tr>
		</logic:notEmpty>

	    <logic:notEmpty name="paymentMethodForm" property="poNumber">
		<tr>
			<td>PO Number:</td>
			<td>
				<logic:equal name="paymentMethodForm" property="editable" value="true">
					<html:text  property="poNumber"/>
				</logic:equal>
				<logic:equal name="paymentMethodForm" property="editable" value="false">
					<bean:write name="paymentMethodForm" property="poNumber" />
					<html:hidden  name="paymentMethodForm" property="poNumber" />
				</logic:equal>
				<br/>
				<span style="font-weight:bold;">
					<html:link href="pages/admin/costcenter/paymentInformation.jsp">Payment Information</html:link>
				</span>
			</td>
		</tr>
		<tr>
			<td>Amount: </td>
			<td>
				<logic:equal name="paymentMethodForm" property="editable" value="true">
					<html:text property="poAmount"/>
				</logic:equal>
				<logic:equal name="paymentMethodForm" property="editable" value="false">
					<bean:write name="paymentMethodForm" property="poAmount" />
					<html:hidden  name="paymentMethodForm" property="poAmount" />
				</logic:equal>
			</td>
		</tr>
		<tr>
			<td>PO Name:</td>
			<td colspan="4">
				<html:text  property="paymentMethodName" size="40" />
			</td>
		</tr>
		</logic:notEmpty>


	<logic:equal name="paymentMethodForm" property="ponumberAllowed" value="false">
	<tr>
		<td>Expiration Date:<span style="color:red;">(required)</span></td>
		<td><html:text  property="budgetExpirationDateStr" styleClass="datepicker"/> <span style="font-size:10px;">format: MM/dd/yyyy</span></td>
	</tr>
	</logic:equal>

	<tr>
		<td>Federal Funding:</td>
		<td colspan="4">
			<html:checkbox property="federalFunding" disabled="false"></html:checkbox>
			<br/>
		</td>
   </tr>

   <tr>
   		<td>Current:</td>
   		<td>
  			<html:radio property="current" value="true">YES</html:radio>
   			<html:radio property="current" value="false">NO</html:radio>
   		</td>
   </tr>
   
   <tr>
   		<td colspan="5" style="color:red;font-size:10pt;">
   			Contact information of the person responsible for accounting and billing.
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
   			<input type="button" onclick="backToProject(); return false;" value="Cancel"/>
   		</td>
   		
   </tr>
   </tbody>
   
</table>

</html:form>

</center>


</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>