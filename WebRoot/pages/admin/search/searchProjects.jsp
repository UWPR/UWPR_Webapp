
<%@page import="org.yeastrc.project.CollaborationStatus"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<%@ include file="/includes/header.jsp" %>


<%@ include file="/includes/errors.jsp" %>

<yrcwww:contentbox title="Search Projects Form">

<P>To search the projects in the UWPR database, enter your search terms below.  Only results containing all of the terms
you entered will be returned.  The researcher names on the project are also searched.
</P>
<html:form action="searchProjects" method="POST">

 <CENTER>
 
 <html:text property="searchString" size="50"/>
 
 <P>Limit your search to:<BR></P>
  
  <P><U>Project Type:</U><BR>
  <html:checkbox property="types" value="C"/>UWPR Supported Projects
  <html:checkbox property="types" value="B"/>Billed Projects
  </P>
  

 <P><U>Project Status (<span style="font-size:8pt;">UWPR supported projects only</span>):</U><BR>
 <%for(CollaborationStatus status: CollaborationStatus.getDisplayStatusList()) {%>
	<html:checkbox property="collaborationStatus" value="<%=status.getShortName() %>" /><%=status.getLongName() %>
 <%} %>
 </P>

 
 <P ALIGN="center"><html:submit value="Search Projects"/>
 <html:submit property="showAll" value="Show All" >Show All Projects</html:submit>
</P>

</CENTER>
</html:form>

</yrcwww:contentbox>


<%@ include file="/includes/footer.jsp" %>