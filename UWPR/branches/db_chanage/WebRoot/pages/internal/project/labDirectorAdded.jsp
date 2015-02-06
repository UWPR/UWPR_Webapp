<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<yrcwww:contentbox title="Lab Director Added">
	<logic:present name="labDirName">
	<P align="center"><B><bean:write name="labDirName"/> has been added as a Lab Director.</B>
	</P>
	</logic:present>
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>