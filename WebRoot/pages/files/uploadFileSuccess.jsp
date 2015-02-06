<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<!--  TODO: auto update parent file listing when page loads -->

<html>
	<head>
		<title>Upload File</title>
		<script type="text/javascript" src="/pr/js/jquery-1.5.min.js"></script>

		<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/global.css">
	</head>

	<script>
		// update the file list in the parent window
		$(document).ready(function() { window.opener.showFiles(); });
	</script>

	<body>	
		<yrcwww:contentbox title="Upload Successful!">
		
			Your file was successfully uploaded!
			
			<br/><br/><a href="javascript:window.close()">Close Window</a>
		
		</yrcwww:contentbox>
	
	</body>
</html>