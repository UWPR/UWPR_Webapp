<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<logic:notPresent name="projectsSearchSize" scope="session">
  <logic:redirect href="/pr/pages/admin/search/searchProjects.jsp" />
</logic:notPresent>

<%@ include file="/includes/header.jsp" %>


<%@ include file="/includes/errors.jsp" %>


<yrcwww:contentbox title="Search Projects Results">

<logic:equal name="projectsSearchSize" scope="session" value="0">
<B>Found 0 matches to your query...</B>
</logic:equal>

<!-- List Billed Projects -->
<logic:notEmpty name="billedProjects" scope="session">

<yrcwww:contentbox title="Billed Projects" innerBox="true">
<table width="100%" cellpadding="2" cellspacing="0" border="0" class="striped">
  
  <thead>
  <tr>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=id">ID</html:link></b></th>
  <th valign="top" align="center"><b><html:link href="/pr/sortProjectSearch.do?sortby=pi"><font style="font-size:8pt;">Lab Director</font></html:link></b></th>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=title">Title</html:link></b></th>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=submit"><font style="font-size:8pt;">Submit Date</font></html:link></b></th>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=change"><font style="font-size:8pt;">Changed</font></html:link></b></th>
  </tr>
  </thead>
 
 <tbody>
<logic:iterate id="project" name="billedProjects" scope="session">
 <tr>
  <TD valign="top" width="5%">
   <NOBR>
    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">
     <bean:write name="project" property="ID"/></html:link>
   </NOBR>
  </TD>
  <TD valign="top" width="13%">
   <logic:present name="project" property="PI"><bean:write name="project" property="PI.lastName"/></logic:present>
  </TD>
  <TD valign="top" width="43%"><bean:write name="project" property="title"/></TD>
  <TD valign="top" width="12%"><bean:write name="project" property="submitDate"/></TD>
  <TD valign="top" width="12%"><bean:write name="project" property="lastChange"/></TD>
 </tr>
</logic:iterate>
</tbody>
</table>
</yrcwww:contentbox>
<br/><br/>
</logic:notEmpty>




<!-- List UWPR Supported Projects -->
<logic:notEmpty name="subsidizedProjects" scope="session">
<yrcwww:contentbox title="UWPR Supported Projects" innerBox="true">
<table width="100%" cellpadding="2" cellspacing="0" border="0" class="striped">
 <thead>
 <tr>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=id">ID</html:link></b></th>
  <th valign="top" align="center"><b><html:link href="/pr/sortProjectSearch.do?sortby=pi"><font style="font-size:8pt;">Lab Director</font></html:link></b></th>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=title">Title</html:link></b></th>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=submit"><font style="font-size:8pt;">Submit Date</font></html:link></b></th>
  <th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=change"><font style="font-size:8pt;">Changed</font></html:link></b></th>
  <th valign="top" align="center"><b><html:link href="/pr/sortProjectSearch.do?sortby=status"><font style="font-size:8pt;">Collaboration Status</font></html:link></b></th>
  <th valign="top" align="center"><b><font style="font-size:8pt;">Report Submitted</font></b></th>
  <yrcwww:member group="administrators">
  	<th valign="top"><b><html:link href="/pr/sortProjectSearch.do?sortby=reviewer"><font style="font-size:8pt;">Reviewer(s)</font></html:link></b></th>
  	<th valign="top"></th>
  </yrcwww:member>
 </tr>
 </thead>
 
 <tbody>
<logic:iterate id="project" name="subsidizedProjects" scope="session">
 <tr>
  <TD valign="top" width="5%">
   <NOBR>
    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="collaboration.ID">
     <bean:write name="project" property="collaboration.ID"/></html:link>
   </NOBR>
  </TD>
  <TD valign="top" width="13%">
   <logic:present name="project" property="collaboration.PI"><bean:write name="project" property="collaboration.PI.lastName"/></logic:present>
  </TD>
  <TD valign="top" width="43%"><bean:write name="project" property="collaboration.title"/></TD>
  <TD valign="top" width="12%"><bean:write name="project" property="collaboration.submitDate"/></TD>
  <TD valign="top" width="12%"><bean:write name="project" property="collaboration.lastChange"/></TD>
  <logic:present name="project" property="collaboration.collaborationStatus">
  	<TD valign="top" width="12%"><bean:write name="project" property="collaboration.collaborationStatus"/></TD>
  </logic:present>
  <logic:equal name="project" property="collaboration.progressReportValid" value="true">
  		<TD valign="top" width="12%">Y</TD>
  </logic:equal>
  <logic:equal name="project" property="collaboration.progressReportValid" value="false">
  		<TD valign="top" width="12%">
  		<logic:equal name="project" property="collaboration.progressReportOverdue" value="true"><font color="red"></logic:equal>
  		N
  		<logic:equal name="project" property="collaboration.progressReportOverdue" value="true"></font></logic:equal>
  		</TD>
  </logic:equal>
  
  <TD valign="top" width="12%">
  <yrcwww:member group="administrators">
  	<logic:present name="project" property="reviewers">

  		<logic:iterate name="project" property="reviewers" id="reviewer">
  			<bean:write name="reviewer" property="researcher.lastName" />
  			<br>
  		</logic:iterate>
  	</logic:present>
  </yrcwww:member>
  </TD>
  <TD valign="top" width="12%">
  <yrcwww:member group="administrators">
  	<logic:present name="project" property="reviewers">
  		<logic:iterate name="project" property="reviewers" id="reviewer">
  			<logic:present name="reviewer" property="recommendedStatusShortName" >
  				<span style="color: red;">(<bean:write name="reviewer" property="recommendedStatusShortName" />)</span>
  			</logic:present>
  			<br>
  		</logic:iterate>
  	</logic:present>
  </yrcwww:member>
  </TD>
  
 </tr>
</logic:iterate>
</tbody>
</table>
</yrcwww:contentbox>
</logic:notEmpty>



</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>