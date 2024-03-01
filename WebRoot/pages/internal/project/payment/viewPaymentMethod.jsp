<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.math.RoundingMode" %>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>


<logic:empty name="paymentMethod">
  <logic:forward name="viewPaymentMethod" />
</logic:empty>

<script type="text/javascript">
function editPaymentMethod(paymentMethodId, projectId) {
	document.location.href="/pr/editPaymentMethod.do?paymentMethodId="+paymentMethodId+"&projectId="+projectId;
}
function deletePaymentMethod(paymentMethodId, projectId) {
	if(confirm("Are you sure you want to delete this payment method?")) {
		document.location.href="/pr/deletePaymentMethod.do?paymentMethodId="+paymentMethodId+"&projectId="+projectId;
	}
}
function backToProject(projectId) {
	document.location.href="/pr/viewProject.do?ID="+projectId;
}
</script>
   			
<yrcwww:contentbox title="View Payment Method">

    <div align="center">
    <div style="margin:10px">
        <table>
            <tbody>
                <logic:notEmpty name="paymentMethod" property="ponumber">
                    <tr>
                        <td>PO Amount: </td>
                        <td>
                            $<bean:write name="paymentMethod" property="poAmount"/>
                        </td>
                    </tr>
                </logic:notEmpty>
                <tr>
                    <td>Total used: </td>
                    <td>$<bean:write name="paymentMethodUsage" property="totalCostFormatted"/></td>
                </tr>
                <tr>
                    <td>Invoiced: </td>
                    <td>$<bean:write name="paymentMethodUsage" property="invoicedCostFormatted"/></td>
                </tr>
                <logic:notEmpty name="paymentMethod" property="ponumber">
                    <tr>
                        <td>Balance: </td>
                        <bean:define id="payment" name="paymentMethod" type="org.yeastrc.project.payment.PaymentMethod"/>
						<bean:define id="paymentUsage" name="paymentMethodUsage" type="org.yeastrc.project.payment.PaymentMethodUsage"/>
                        <%
                            BigDecimal available = payment.getPoAmount() != null ? payment.getPoAmount() : new BigDecimal("0");
                            BigDecimal used = paymentUsage.getTotalCost() != null ? paymentUsage.getTotalCost() : BigDecimal.ZERO;
                            BigDecimal balance = available.subtract(used);
                            if(balance.doubleValue() < 0.0) balance = new BigDecimal("0");
                            balance = balance.setScale(2, RoundingMode.HALF_UP);
                        %>
                        <td>$<%=balance.toString()%></td>
                    </tr>
                </logic:notEmpty>
            </tbody>
        </table>
    </div>

<table border="0" cellpadding="7" class="striped">

	<tbody>
	<tr>
		<td><b>Project ID:</b></td>
		<td>
			<b><html:link action="viewProject.do" paramId="ID" paramName="projectId"><bean:write name="projectId"/></html:link></b>
		</td>
	</tr>

	<logic:notEmpty name="paymentMethod" property="worktag">
		<tr>
			<td>Worktag:</td>
			<td>
				<bean:write name="paymentMethod"  property="worktag"/>
			</td>
		</tr>
		<tr>
			<td>Worktag Name:</td>
			<td>
				<bean:write name="paymentMethod"  property="paymentMethodName"/>
			</td>
		</tr>
		<tr>
			<td>Resource worktag:</td>
			<td>
				<bean:write name="paymentMethod"  property="resourceWorktag"/>
			</td>
		</tr>
		<tr>
			<td>Resource worktag description:</td>
			<td>
				<bean:write name="paymentMethod"  property="resourceWorktagDescr"/>
			</td>
		</tr>
		<tr>
			<td>Assignee worktag:</td>
			<td>
				<bean:write name="paymentMethod" property="assigneeWorktag"/>
			</td>
		</tr>
		<tr>
			<td>Assignee worktag description:</td>
			<td>
				<bean:write name="paymentMethod"  property="assigneeWorktagDescr"/>
			</td>
		</tr>
		<tr>
			<td>Activity worktag:</td>
			<td>
				<bean:write name="paymentMethod" property="activityWorktag"/>
			</td>
		</tr>
		<tr>
			<td>Activity worktag description:</td>
			<td>
				<bean:write name="paymentMethod"  property="activityWorktagDescr"/>
			</td>
		</tr>
		<tr>
			<td>Expiration Date:</td>
			<td>
				<bean:write name="paymentMethod"  property="budgetExpirationDate"/>
			</td>
		</tr>
	</logic:notEmpty>

	<logic:notEmpty name="paymentMethod" property="uwbudgetNumber">
		<tr>
	    	<td>UW Budget Number:</td>
	    	<td>
	    		<bean:write name="paymentMethod"  property="uwbudgetNumber"/>
	    	</td>
	   </tr>
		<tr>
			<td>Budget Name:</td>
			<td>
				<bean:write name="paymentMethod"  property="paymentMethodName" />
			</td>
		</tr>
		<tr>
			<td>Expiration Date:</td>
			<td>
				<bean:write name="paymentMethod"  property="budgetExpirationDate"/>
			</td>
		</tr>
   </logic:notEmpty>
   
   <logic:notEmpty name="paymentMethod" property="ponumber">
		<tr>
	    	<td>PO Number:</td>
	    	<td>
	    		<bean:write name="paymentMethod"  property="ponumber"/>
	    	</td>
	   </tr>
       <tr>
            <td>PO Amount:</td>
            <td>
                <bean:write name="paymentMethod"  property="poAmount"/>
            </td>
       </tr>
	   <tr>
		   <td>PO Name:</td>
		   <td>
			   <bean:write name="paymentMethod"  property="paymentMethodName" />
		   </td>
	   </tr>
   </logic:notEmpty>

   <tr>
	   	<td>Federal Funding:</td>
	   	<td>
	   		<bean:write name="paymentMethod"  property="federalFunding"/>
	   	</td>
	</tr>
    <tr>
   		<td>Current:</td>
   		<td>
   			<bean:write name="paymentMethod"  property="current"/>
   		</td>
   </tr>
   
   <tr>
   		<td colspan="2" style="font-weight:bold; text-align:left;">
   			Contact details of the person responsible for accounting and billing.
   		</td>
   </tr>
   <tr>
   		<td>First Name:</td>
   		<td>
   			<bean:write name="paymentMethod"  property="contactFirstName" />
   		</td>
   	</tr>
   	<tr>
   		<td>Last Name:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="contactLastName" />
   		</td>
   	</tr>
   	<tr>
   		<td>Email:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="contactEmail" />
   		</td>
   	</tr>
   	<tr>
   		<td>Phone:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="contactPhone"/>
   		</td>
   	</tr>
   	<tr>
   		<td>Organization:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="organization" />
   		</td>
   	</tr>
   	<tr>
   		<td>Address Line 1:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="addressLine1" />
   		</td>
   	</tr>
   	<tr>
   		<td>Address Line 2:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="addressLine2" />
   		</td>
   	</tr>
   	<tr>
   		<td>City:</td>
   		<td >
   			<bean:write name="paymentMethod"  property="city"/>
   		</td>
   	</tr>
   	<tr>
   		<td>State:</td>
   		<td >
   			<bean:write name="paymentMethod" property="state" />
   		</td>
   	</tr>
   	<tr>
   		<td>Zip/Postal Code:</td>
   		<td >
   			<bean:write name="paymentMethod" property="zip" />
   		</td>
   	</tr>
   	<tr>
   		<td>Country:</td>
   		<td >
   			<bean:write name="paymentMethod" property="country" />
   		</td>
   	</tr>
   	
   	<tr>
   		<td colspan="2" style="text-align:center;">

   			<input onclick='editPaymentMethod(<bean:write name="paymentMethod" property="id"/>, <bean:write name="projectId"/>)' type="button" value="Edit"/>
   			<input onclick='deletePaymentMethod(<bean:write name="paymentMethod" property="id"/>, <bean:write name="projectId"/>)' type="button" value="Delete"/>
   			<input onclick='backToProject(<bean:write name="projectId"/>)' type="button" value="Back to Project"/>
   			
   		</td>
   	</tr>
   	
   </tbody>
   
</table>
</div>
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp"%>