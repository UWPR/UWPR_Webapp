
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:empty name="project">
  <logic:forward name="viewProject" />
</logic:empty>
 
<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<script type="text/javascript" src="/pr/js/jquery-1.5.min.js"></script>
<script type="text/javascript" src="/pr/js/jquery.tools.min.js" ></script>
<script type="text/javascript" src="/pr/js/files.js" ></script>
<script>
	$(document).ready(function() { showFiles( 'project', <bean:write name="project" property="ID" />);});
</script>
<style>
	div.tooltip
	{
		background: #333;
		background: rgba(0,0,0,.5);
		color: #fff;
		border-radius: 5px;
		padding: 5px 10px;
	}
</style>
<yrcwww:contentbox title="View Project Details">

<SCRIPT LANGUAGE="JavaScript">
 function confirmDelete(ID) {
    if(confirm("Are you sure you want to delete this project?")) {
       if(confirm("Are you ABSOLUTELY sure you want to delete this project?")) {
          document.location.href="/pr/deleteProject.do?ID=" + ID;
          return 1;
       }
    }
 }
</SCRIPT>

 <CENTER>
 <TABLE CELLPADDING="no" CELLSPACING="0" class="striped">
  
  <tr>
   <TD valign="top" width="25%">ID:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="ID"/></TD>
  </tr>

   <!--  ANCESTORS of this project, if any -->
  <logic:notEmpty name="ancestorProjects">
  <tr>
  	<TD valign="top" width="25%"><b>Parent Project(s):</b></TD>
  	<TD valign="top" width="75%">
  		<logic:iterate name="ancestorProjects" id="ancestorId">
  			<html:link action="viewProject.do" paramId="ID" paramName="ancestorId" >
  				<bean:write name="ancestorId"/>
  			</html:link>
  			&nbsp; &nbsp;
  		</logic:iterate>
  	</TD>
  	</tr>
  </logic:notEmpty>
  
  <!--  Child project, if any -->
  <logic:notEqual name="childProjectId" value="0">
  <tr>
  	<TD valign="top" width="25%"><b>Extension Project:</b></TD>
  	<TD valign="top" width="75%">
  		<html:link action="viewProject.do" paramId="ID" paramName="childProjectId" >
  			<bean:write name="childProjectId"/>
  		</html:link>
  	</TD>
  	</tr>
  </logic:notEqual>
  
  
  <tr>
   <TD valign="top" width="25%">Affiliation:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="affiliation.name"/></TD>
  </tr>
  
  <tr>
   <TD valign="top" width="25%">Title:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="title"/></TD>
  </tr>

  <tr>
   <TD valign="top" width="25%">Collaborating with:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="groupsString"/></TD>
  </tr>

  
   
  <!-- List the Researchers here: -->
  <%@ include file="researcherList.jsp" %>

  <tr>
   <TD valign="top" width="25%">Scientific Question:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="scientificQuestion" filter="false"/></TD>
  </tr>

  <tr>
   <TD valign="top" width="25%">Abstract:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="abstractAsHTML" filter="false"/></TD>
  </tr>


  <tr>
   <TD valign="top" width="25%">Progress/Results:</TD>
   <TD valign="top" width="75%">
   		<bean:write name="project" property="progressAsHTML" filter="false"/>
   </TD>
  </tr>

	<logic:notEmpty name="project" property="publications">
	  <tr>
	   <TD valign="top" width="25%">Publications:</TD>
	   <TD valign="top" width="75%"><bean:write name="project" property="publicationsAsHTML" filter="false"/></TD>
	  </tr>
	</logic:notEmpty>

	<logic:notEmpty name="project" property="comments">
  		<tr>
   		<TD valign="top" width="25%">Comments:</TD>
   		<TD valign="top" width="75%"><bean:write name="project" property="commentsAsHTML" filter="false"/></TD>
  		</tr>
	</logic:notEmpty>

	<tr>
		<td colspan="2" valign="top">
		<div style="text-decoration:underline; margin-bottom:10px;">PaymentMethods:</div>
			<logic:notEmpty name="project" property="paymentMethods">
				<table cellspacing="0" cellpadding="0" border="1">
                <thead>
                    <th style="font-size: smaller;">Type</th>
                    <th>Number</th>
					<th>Name</th>
                    <th>Current</th>
					<th>Expires</th>
					<th>Setup</th>
					<th>Signup</th>
					<th>Instrument</th>
                    <th>Total Cost</th>
                    <th>Invoiced</th>
                    <th>&nbsp;</th>
                </thead>
				<tbody>
				<logic:iterate name="project" property="paymentMethods" id="paymentMethod">
					<tr>
					<logic:notEmpty name="paymentMethod" property="uwbudgetNumber">
						<td style="padding:3px;">UW Budget Number</td>
						<td style="padding:3px;font-weight:bold"><bean:write name="paymentMethod" property="uwbudgetNumber"/></td>
					</logic:notEmpty>
					<logic:notEmpty name="paymentMethod" property="ponumber">
						<td style="padding:3px;">PO Number</td>
						<td style="padding:3px;">
                            <span style="font-weight: bold;">
                                <bean:write name="paymentMethod" property="ponumber"/>
                            </span>
                            <logic:greaterThan name="paymentMethod" property="poAmount" value="0">
                                &nbsp;<nobr>($<bean:write name="paymentMethod" property="poAmount"/> )</nobr>
                            </logic:greaterThan>
                        </td>
					</logic:notEmpty>
					<td style="padding:3px">
						<bean:write name="paymentMethod" property="name50Chars" />
					</td>
					<td style="padding:3px">
						<logic:equal name="paymentMethod" property="current" value="true">
							<span style="color:green">Yes</span>
						</logic:equal>
						<logic:equal name="paymentMethod" property="current" value="false">
							&nbsp;
						</logic:equal>
					</td>
					<td style="padding:3px">
						<nobr><bean:write name="paymentMethod" property="budgetExpirationDate" /></nobr>
					</td>
					<td style="padding:3px">
						$<bean:write name="paymentMethod" property="setupCost" />
					</td>
					<td style="padding:3px">
						$<bean:write name="paymentMethod" property="signupCost" />
					</td>
					<td style="padding:3px">
						$<bean:write name="paymentMethod" property="instrumentCost" />
					</td>
                    <td style="padding:3px">
                        $<bean:write name="paymentMethod" property="totalCost" />
                    </td>
                    <td style="padding:3px">
                        $<bean:write name="paymentMethod" property="invoicedCost" />
                    </td>
					<td style="padding:3px">
						<nobr>
						<a href='/pr/viewPaymentMethod.do?projectId=<bean:write name="project" property="ID"/>&paymentMethodId=<bean:write name="paymentMethod" property="id"/>'><img src="<%=request.getContextPath()%>/images/view.png" title="View" width="20" height="20"/></a>
						&nbsp;&nbsp;
						<a href='/pr/copyPaymentMethod.do?projectId=<bean:write name="project" property="ID"/>&paymentMethodId=<bean:write name="paymentMethod" property="id"/>'><img src="<%=request.getContextPath()%>/images/copy.png" title="Copy" width="20" height="20"/>
						</a>
						</nobr>
					</td>
					</tr>
				</logic:iterate>
				</tbody>
				</table>
				<div style="margin:10px 0px 10px 0px; text-align:left;font-weight:bold;">
					<html:link action="newPaymentMethod.do" paramId="projectId" paramName="project" paramProperty="ID">
					[Add New Payment Method]
					</html:link>
					<logic:equal name="project" property="massSpecExpertiseRequested" value="false">
						&nbsp; &nbsp;
						<html:link action="viewScheduler.do" paramId="projectId" paramName="project" paramProperty="ID">
						[Schedule Instrument Time]
						</html:link>
					</logic:equal>
					<logic:equal name="project" property="massSpecExpertiseRequested" value="true">
						<yrcwww:member group="administrators">
							&nbsp; &nbsp;
							<html:link action="viewScheduler.do" paramId="projectId" paramName="project" paramProperty="ID">
							[Schedule Instrument Time]
							</html:link>
						</yrcwww:member>
					</logic:equal>
				</div>
				
				<div style="margin:5px 0px 15px 0px">
					<html:link action="viewTimeScheduledForProject" paramId="projectId" paramName="project" paramProperty="ID">
					<b>[Instrument time scheduled for project]</b>
					</html:link>
				</div>
				
			</logic:notEmpty>
			<logic:empty name="project" property="paymentMethods">
				<div style="color:red;margin:10px 0px 10px 0px;">
					There are no payment methods associated with this project.
					<br/>
					In order to schedule instrument time you must have at least one payment method.
					<br/>  
					Click <html:link action="newPaymentMethod.do" paramId="projectId" paramName="project" paramProperty="ID">here</html:link>
					to add a payment method for this project.
				</div>
			</logic:empty>

			<ul>
				<li style="padding:3px;"><a href="/pr/costcenter_resources/UWPR_FAQ_Instrument_scheduling.pdf">Billing FAQ and instructions for scheduling instrument time</a></li>
				<li style="padding:3px;"><a href="/pr/costcenter_resources/UWPR_Current_Rates.xlsx">Download current instrument rates</a></li>
				<li style="padding:3px;"><html:link href="pages/admin/costcenter/paymentInformation.jsp">Payment information</html:link></li>
			</ul>
			
		</td>
	</tr>
	
	
	<tr>
		<td width="25%" valign="top">Mass Spec. analysis by UWPR personnel?</td>
		<td width="75%" valign="top"><bean:write name="project" property="massSpecExpertiseRequested" /></td>
	</tr>
	
	<tr>
		<td width="25%" valign="top">Database search at UWPR?</td>
		<td width="75%" valign="top"><bean:write name="project" property="databaseSearchRequested" /></td>
	</tr>
	
  
	
  <tr>
   <TD valign="top" width="25%">Submit Date:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="submitDate"/></TD>
  </tr>

  <tr>
   <TD valign="top" width="25%">Last Updated:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="lastChange"/></TD>
  </tr>
  
  <yrcwww:member group="administrators">
  	<tr>
   	<TD valign="top" width="25%">Status:</TD>
   	<TD valign="top" width="75%">
   		<logic:equal name="project" property="blocked" value="false">
   			ACTIVE
  		</logic:equal>
   		<logic:equal name="project" property="blocked" value="true">
   			BLOCKED
  		</logic:equal>
   	</TD>
  	</tr>
  </yrcwww:member>
  
  <tr>
   <TD valign="top" width="25%">Files:</TD>
   <TD valign="top" width="75%">
   	<div id="files"><span>Loading files from database...</span></div>
   </TD>
  </tr>
  
  
 </TABLE>


   
 <div style="margin-top:20px">
 <html:link action="/editProject.do" paramId="ID" paramName="project" paramProperty="ID"><B>[EDIT PROJECT]</B></html:link>
 
 <yrcwww:member group="administrators">
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <a href="#" onclick="confirmDelete('<bean:write name="project" property="ID"/>'); return false;"><B>[DELETE PROJECT]</B></a>
 </yrcwww:member>

 <div style="margin-top:20px;font-weight:bold; font-size: larger;">
     <a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">Sample Submission Form</a>
 </div>
 
</div>
</CENTER>
</yrcwww:contentbox>

   
<br><br>

  <!-- List the external links to data here: -->
  <%@ include file="/pages/internal/data/listDataForProject.jsp" %>


<yrcwww:member group="administrators">
<br/><br/>
<yrcwww:contentbox title="Project Status">
  <div style="width:100%;" align="center">
  <logic:equal name="project" property="blocked" value="false">
  	<html:link href="/pr/toggleBilledProjectStatus.do" paramId="projectId" paramName="project" paramProperty="ID"><B>[BLOCK PROJECT]</B></html:link>
  </logic:equal>
   <logic:equal name="project" property="blocked" value="true">
  	<html:link href="/pr/toggleBilledProjectStatus.do" paramId="projectId" paramName="project" paramProperty="ID"><B>[UNBLOCK PROJECT]</B></html:link>
  </logic:equal>
  <br/>
  </div>
</yrcwww:contentbox>
</yrcwww:member>

<%@ include file="/includes/footer.jsp" %>