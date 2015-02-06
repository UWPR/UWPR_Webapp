<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<%
	response.sendRedirect("http://proteomicsresource.washington.edu/");
	// response.sendRedirect("http://localhost:8090/index.php");
 %>
 

<%@ include file="/includes/footer.jsp" %>