<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>
<logic:notPresent name="overdueProjects" scope="request">
 <logic:forward name="newCollaboration"/>
</logic:notPresent>

<yrcwww:contentbox title="Collaboration Request Denied">
<div style="color: red;">
You are temporarily blocked from requesting a collaboration with the UWPR because one or more projects
listing you as a researcher are overdue for a progress report. The UWPR requires a progress report to 
be submitted within 12 months of a project being accepted.  This report should highlight the 
scientific progress enabled by the collaboration. 
<br><br>

A progress report is due for the projects listed below. You can submit your report for a project by 
clicking on "Submit Report".
<br><br>

You may request another collaboration with the UWPR once you have submitted reports for all projects
listed below. 
</div>
<br><br>

<yrcwww:contentbox title="Your Projects" >

<logic:notEmpty name="overdueProjects" scope="request">
 <TABLE BORDER="0" WIDTH="100%">
  <TR>
   <TD>&nbsp;</TD>
   <TD><U>ID</U></TD>
   <TD><U>Title</U></TD>
   <TD><U>Submit Date</U></TD>
   <TD><U>Collaboration<br>Status</U></TD>
   <TD></TD>
  </TR>

<logic:iterate id="project" name="overdueProjects" scope="request">
  <logic:equal name="project" property="progressReportOverdue" value="true">
  	<TR style="">
  </logic:equal>
  <logic:equal name="project" property="progressReportOverdue" value="false">
  	<TR>
  </logic:equal>
  <TD valign="top">
   <NOBR>
    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
   </NOBR>
  </TD>
  <TD valign="top"><bean:write name="project" property="ID"/></TD>
  <TD valign="top"><bean:write name="project" property="title"/></TD>
  <TD valign="top"><bean:write name="project" property="submitDate"/></TD>
  <TD valign="top"><bean:write name="project" property="collaborationStatus"/></TD>
  <TD>
  	<span style="font-size: 8pt;">
    <html:link href="/pr/editProject.do" paramId="ID" paramName="project" paramProperty="ID">Submit<br>Report</html:link>
    </span>
  </TD>
 </TR>
</logic:iterate>

 </TABLE>
</logic:notEmpty>

</yrcwww:contentbox>


<CENTER>
</CENTER>

</yrcwww:contentbox>
<%@ include file="/includes/footer.jsp" %>