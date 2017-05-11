
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:empty name="projectAndReview">
  <logic:forward name="viewProject" />
</logic:empty>
 
<bean:define name="projectAndReview" property="collaboration" id="project" type="org.yeastrc.project.Collaboration"/>


<%@ include file="/includes/header.jsp" %>

<script type="text/javascript" src="/pr/js/jquery-1.5.min.js"></script>
<script type="text/javascript" src="/pr/js/jquery.tools.min.js" ></script>
<script type="text/javascript" src="/pr/js/files.js" ></script>
<script>
	$(document).ready(function() { showFiles( 'project', <bean:write name="project" property="ID" />, '<%= (String)session.getId() %>');});
</script>

<%@ include file="/includes/errors.jsp" %>


<yrcwww:contentbox title="View Collaboration Details">

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
   <TD valign="top" width="25%">Title:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="title"/></TD>
  </tr>

  <tr>
   <TD valign="top" width="25%">Collaborating with:</TD>
   <TD valign="top" width="75%"><bean:write name="project" property="groupsString"/></TD>
  </tr>

  <tr>
   <TD valign="top" width="25%">Collaboration status:</TD>
   <TD valign="top" width="75%"><b><bean:write name="project" property="collaborationStatus" /></b></TD>
  </tr>
  
  <logic:present name="rawFileUsage">
   <tr>
   <TD valign="top" width="25%"># RAW files: </TD>
   <TD valign="top" width="75%">
   	<%int totalRunsRequested = project.getTotalRunsRequested(); %>
   	<logic:greaterThan name="rawFileUsage" property="rawFileCount" value='<%=String.valueOf(totalRunsRequested)%>'>
   		<font color="red">
   	</logic:greaterThan>
   		<b><bean:write name="rawFileUsage" property="rawFileCount" /></b>
   <logic:greaterThan name="rawFileUsage" property="rawFileCount" value='<%=String.valueOf(totalRunsRequested)%>'>
   		</font>
   	</logic:greaterThan>
   </TD>
  </tr>
  </logic:present>
  
    <logic:present name="project" property="dateAccepted">  
	  	<tr>
	   	<TD valign="top" width="25%">Date Accepted:</TD>
	   	<TD valign="top" width="75%"><bean:write name="project" property="dateAccepted" /></TD>
	  	</tr>
  	</logic:present>
  
   
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


  <!-- EXTENSION REASONS -->
  <logic:present name="project" property="extensionReasons">
  	<tr>
  		<TD valign="top" width="25%">Project Extension Reason(s):</TD>
  		<TD valign="top" width="75%"><bean:write name="project" property="extensionReasonsAsHTML" filter="false"/></TD>
  	</tr>
  </logic:present>
  
  
  <tr>
   <TD valign="top" width="25%">Progress/Results:</TD>
   <TD valign="top" width="75%">
   <logic:equal name="project" property="progressReportOverdue" value="true">
  		<span style="color: red; font-weight: bold;">Report Overdue</span>
   </logic:equal>
   <logic:equal name="project" property="progressReportOverdue" value="false">
   		<bean:write name="project" property="progressAsHTML" filter="false"/>
   </logic:equal>
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
		<td width="25%" valign="top">Approx. # of requested runs:</td>
		<td width="75%" valign="top">
			<table width="50%">
				<tr>
					<td>LTQ:</td>
					<td><bean:write name="project" property="ltqRunsRequested" /></td>
					<td>LTQ-FT:</td>
					<td><bean:write name="project" property="ltq_ftRunsRequested" /></td>
				</tr>
				<tr>
					<td>LTQ-ETD:</td>
					<td><bean:write name="project" property="ltq_etdRunsRequested" /></td>
					<td>LTQ-Orbitrap:</td>
					<td><bean:write name="project" property="ltq_orbitrapRunsRequested" /></td>
				</tr>
				<tr>
					<td>TSQ-Access:</td>
					<td><bean:write name="project" property="tsq_accessRunsRequested" /></td>
					<td>TSQ-Vantage:</td>
					<td><bean:write name="project" property="tsq_vantageRunsRequested" /></td>
				</tr>
				
				 <logic:equal name="project" property="accepted" value="true">
				 <yrcwww:member group="administrators">
					<tr>
						<td colspan="4" align="center">
							<div style="margin-bottom: 10px;">
								<html:link action="viewScheduler.do" paramId="projectId" paramName="project" paramProperty="ID">
								[Schedule Instrument Time]
								</html:link>
							</div>
						</td>
					</tr>
				</yrcwww:member>
				</logic:equal>
	
			</table>
		</td>
	</tr>
	
	<tr>
		<TD valign="top" width="25%">Instrument time justification:</TD>
   		<TD valign="top" width="75%"><bean:write name="project" property="instrumentTimeExplAsHTML" filter="false"/></TD>
	
	</tr>
	
	
	<tr>
		<td width="25%" valign="top">Fragmentation types:</td>
		<td width="75%" valign="top"><bean:write name="project" property="fragmentationTypesString" /></td>
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

  <tr>
   <TD valign="top" width="25%">Files:</TD>
   <TD valign="top" width="75%">
   	<div id="files"><span>Loading files from database...</span></div>
   </TD>
  </tr>

 </TABLE>


   
 <div style="margin-top:15px;">

 <yrcwww:member group="administrators">
  <a href="#" onclick="confirmDelete('<bean:write name="project" property="ID"/>'); return false;"><B>[DELETE PROJECT]</B></a>
 </yrcwww:member>
 </div>

 <div style="margin-top: 20px;font-weight:bold; font-size: larger;">
     <a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">Sample Submission Form</a>
 </div>

 </CENTER>
</yrcwww:contentbox>

<!-- DISPLAY REVIEWER INFO ONLY FOR REVIEWERS AND ADMINISTRATORS TO SEE -->
<yrcwww:member group="Reviewers">
<br><br>
<yrcwww:contentbox title="Collaboration Review">
<center>

<logic:iterate name="projectAndReview" property="reviewers" id="reviewer">
	<div style="background-color:#FFFFFF;  width:90%; margin-top:20; margin-bottom:10; padding:5;" align="center" >
	<table CELLPADDING="no" CELLSPACING="0" width="98%">
	<tr>
		<TD WIDTH="25%" VALIGN="top"><b>Reviewer:</b></TD>
		<TD WIDTH="75%" VALIGN="top">
			<b><bean:write name="reviewer" property="researcher.firstName" /> <bean:write name="reviewer" property="researcher.lastName"/></b>
		</TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top">Status Recommended:</TD>
		<TD WIDTH="75%" VALIGN="top"><bean:write name="reviewer" property="recommendedStatus" /></TD>
	</tr>
	<logic:present name="reviewer" property="rejectionCauseString">
		<TD WIDTH="25%" VALIGN="top">Rejection Cause(s):</TD>
		<TD WIDTH="75%" VALIGN="top">
			<logic:iterate name="reviewer" property="rejectionCauses" id="cause" indexId="cnt">
				
				<%=(cnt+1) %>. <bean:write name="cause" property="cause" /><br>
			</logic:iterate>
		</TD>
	</logic:present>
 		<tr>
  			<TD valign="top" width="25%" style="padding-top:10px;">Comments:<br>
  			<span style="color:red; font-size:8pt;">(NOT seen by investigators)</span>
  			</TD>
  			<TD valign="top" width="75%"><bean:write name="reviewer" property="reviewerCommentsAsHTML" filter="false"/></TD>
 		</tr>
 		<tr>
  			<TD valign="top" width="25%" style="padding-top:10px;">Email Comments:<br>
  			<span style="color:red; font-size:8pt;">(Included in e-mail to investigators)</span>
  			</TD>
  			<TD valign="top" width="75%"><bean:write name="reviewer" property="reviewerEmailCommentsAsHTML" filter="false"/></TD>
 		</tr>
 </table>
</div>
</logic:iterate>
 
</yrcwww:contentbox>
</yrcwww:member>

   
<br><br>

  <!-- List the external links to data here: -->
  <%@ include file="/pages/internal/data/listDataForProject.jsp" %>


<%@ include file="/includes/footer.jsp" %>