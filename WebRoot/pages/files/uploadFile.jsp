<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<html>
	<head>

		<title>Upload File</title>

		<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/global.css">

	</head>

	<body>
		<%@ include file="/includes/errors.jsp" %>
	
		<yrcwww:contentbox title="Upload File">
		
		Select a file from your computer to upload:
		
			<html:form action="uploadFile" method="post" enctype="multipart/form-data">


				<html:hidden property="type"/>
				<html:hidden property="id"/>
				
				<html:file property="dataFile" />
			
				<html:submit />
			
			</html:form>
		</yrcwww:contentbox>
	
	</body>
</html>