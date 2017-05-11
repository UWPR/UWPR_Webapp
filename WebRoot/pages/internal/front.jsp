<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<!-- Make sure we have our Collections defined, if not, go get them --> 
<logic:notPresent name="userProjects" scope="request">
	<logic:forward name="standardHome"/>
</logic:notPresent>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<yrcwww:contentbox title="Welcome">

<P>Welcome to the internal Proteomics Resource web site.</P>

<logic:present name="showReminder">
	<!--
	<div id="acknowledgementReminder" style="color:red;font-weight:bold;margin-top:20px;margin-bottom:20px;">
		Please acknowledge the University of Washington's proteomics resource in your publications.
	</div>
	-->
</logic:present>

	<p>
From here, you can view and update your abstract information, request new collaborations and retrieve your proteomics data. 
To initiate a new project you need to submit a short project description and billing information.
</p>

<ul>
		<li>
			<b><html:link href="/pr/newBilledProject.do">Start new collaboration</html:link></b>
		</li>
		<li>
			<a href="/pr/costcenter_resources/UWPR_Current_Rates.xlsx">Current instrument rates</a>
		</li>
		<li>
			<a href="/pr/costcenter_resources/UWPR_FAQ_Instrument_scheduling.pdf">Billing FAQ and instructions for scheduling instrument time</a>
		</li>
		<li>
			<html:link href="../admin/costcenter/paymentInformation.jsp">Payment information</html:link>
		</li>
        <li>
            <a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">Sample Submission Form</a>
        </li>
</ul>

<p>
UW members will be able to schedule instrument time immediately after submitting both project details and billing information. 
Please make sure to coordinate with UWPR personnel to get access to the facility and schedule training time. 
All collaborators must undergo the appropriate training at the beginning and during their scheduled instrument time. 
There is a 30 minute setup time required at the beginning of your scheduled instrument time for UWPR personnel to get the instrument ready.
</p>

<p>
Non UW collaborators will have to coordinate sample analysis with UWPR personnel. 
Contact Priska von Haller (priska@uw.edu) for more details. The <b><a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">Sample Submission Form</a></b>
can be downloaded <a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">here</a>.
</p>

<p>
The UWPR reserves the right to cancel a collaboration at any time for any reason without notice. All fees are subject to change. 
</p>

<p>
The UW Proteomics Resource should be acknowledged as follows in publications: 
<b>This work is supported in part by the University of Washington's Proteomics Resource (UWPR95794).</b> 
Please report significant progress and publications for each project.  
This can be done by navigating to the appropriate project page, clicking on the "Edit Project" link and
filling out the fields labeled as "Progress" and "Publications". 
</p>

<div style="color:red; margin-bottom:20px">
Please NOTE: We are not equipped or licensed to handle either biohazards or radioisotopes and, 
consequently, will NOT work with either pathogenic or radioactive materials.
</div>



<!-- SHOW ALL PROJECTS, FOR WHICH THIS USER IS LISTED AS A RESEARCHER -->
<yrcwww:contentbox title="Your Projects" innerBox="true">

<logic:notEmpty name="userProjects" scope="request">
 <TABLE BORDER="0" WIDTH="100%" class="striped">
 
 <thead>
   <TH>&nbsp;</TH>
   <TH><U>ID</U></TH>
   <TH><U>Title</U></TH>
   <TH><U>Type</U></TH>
   <TH><U>Submit Date</U></TH>
   <TH><U>Collaboration<br>Status</U></TH>
   <TH>&nbsp;</TH>
</thead>

<tbody>
<logic:iterate id="project" name="userProjects" scope="request">
  
  <TR>
  <TD valign="top">
   <NOBR>
    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
   </NOBR>
  </TD>
  <TD valign="top"><bean:write name="project" property="ID"/></TD>
  <TD valign="top"><bean:write name="project" property="title"/></TD>
  <TD valign="top">
  	<logic:equal name="project" property="shortType" value="C">
  		UWPR Supported
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Billed
  	</logic:equal>
  </TD>
  <TD valign="top"><bean:write name="project" property="submitDate"/></TD>
  <TD valign="top">
  	<logic:equal name="project" property="shortType" value="C">
  		<bean:write name="project" property="collaborationStatus"/>
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Active
  	</logic:equal>
  </TD>
  <TD>
  	<logic:equal name="project" property="shortType" value="C">
	  	<logic:equal name="project" property="progressReportOverdue" value="true">
	  		<span style="color: red; font-weight: bold; font-size: 8pt;">Report Overdue</span>
	  	</logic:equal>
  	</logic:equal>
  </TD>
  </TR>
</logic:iterate>
</tbody>
 </TABLE>
 
</logic:notEmpty>

<logic:empty name="userProjects" scope="request">
	<p>You have not yet submitted an abstract for collaboration.
	<br/>  
	For a billed project <html:link href="/pr/newBilledProject.do">click here</html:link>.
	</p>
</logic:empty>

</yrcwww:contentbox>

<div style="margin: 20px;"></div>

<!-- SHOW ANY RECENT SUBMISSIONS TO THIS USER'S GROUP --> 
<yrcwww:member group="any">
	<yrcwww:contentbox title="Recent Submissions" innerBox="true">
	 	<logic:notEmpty name="newProjects" scope="request">
		 <p>Below are projects submitted by researchers to your group(s) within the last month.
	 
		 <p><table border="0" width="100%" class="striped">
		  <thead>
		  <tr>
		   <th>&nbsp;</th>
		   <th><u>ID</u></th>
		   <th><u>Lab Director</u></th>
		   <th><u>Title</u></th>
		   <th><u>Submit Date</u></th>
		  </tr>
		  </thead>
	 
	 	<tbody>
		<logic:iterate id="project" name="newProjects" scope="request">
		 <TR>
		  <TD valign="top">
		   <NOBR>
		    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
		   </NOBR>
		  </TD>
		  <TD valign="top"><bean:write name="project" property="ID"/></TD>
		  <TD valign="top"><bean:write name="project" property="PI.lastName"/></TD>
		  <TD valign="top"><bean:write name="project" property="title"/></TD>
		  <TD valign="top"><bean:write name="project" property="submitDate"/></TD>
		 </TR>
		</logic:iterate>
		</tbody>
		
		</table>
   		</logic:notEmpty>
   		<logic:empty name="newProjects" scope="request">
   		 <p>There have been no projects submitted to your group in the last month.
   		</logic:empty>
	</yrcwww:contentbox>
</yrcwww:member>


<!-- SHOW ALL PROJECTS, FOR WHICH THIS USER IS LISTED AS A REVIEWER --> 
<yrcwww:member group="Reviewers">

<logic:present name="reviewAssignments" scope="request">

<div style="margin: 20px;"></div>
<yrcwww:contentbox title="Projects Assigned to Review" innerBox="true">
 <TABLE BORDER="0" WIDTH="100%">
 
 <logic:empty name="reviewAssignments">
 	<div align="center"> You do not have any projects to review!</div>
 </logic:empty>
 
 <logic:notEmpty name="reviewAssignments">
  <thead>
  <tr>
	<th>&nbsp;</th>
	<th><u>ID</u></th>
	<th><u>Lab Director</u></th>
	<th><u>Title</u></th>
	<th><u>Submit Date</u></th>
	<th><u>Collaboration<br>Status</u></th>
 </tr>
 </thead>
 
 <tbody>	
	<logic:iterate id="project" name="reviewAssignments" scope="request">
 	<TR>
	<TD valign="top">
		<NOBR>
	    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
	   </NOBR>
	</TD>
  	<TD valign="top"><bean:write name="project" property="ID"/></TD>
  	<TD valign="top"><bean:write name="project" property="PI.lastName"/></TD>
  	<TD valign="top"><bean:write name="project" property="title"/></TD>
  	<TD valign="top"><bean:write name="project" property="submitDate"/></TD>
  	<logic:equal name="project" property="shortType" value="C">
  		<TD valign="top"><bean:write name="project" property="collaborationStatus" /></TD>
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Active
  	</logic:equal>
  	
	</TR>
</logic:iterate>
 </logic:notEmpty>
 </tbody>
 </TABLE>
</yrcwww:contentbox> 
</logic:present>  

</yrcwww:member>


</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>