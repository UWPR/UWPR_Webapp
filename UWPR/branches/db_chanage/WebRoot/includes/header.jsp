<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
response.addHeader("Cache-control", "no-store"); // tell proxy not to cache
response.addHeader("Cache-control", "max-age=0"); // stale right away
%>

<!DOCTYPE htm PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
<head>
 <yrcwww:title />
 
 <link href="http://fonts.googleapis.com/css?family=Arvo:400,700" rel="stylesheet" type="text/css" />
 <link REL="stylesheet" TYPE="text/css" HREF="/pr/css/default.css">
 <!--[if lte IE 7]><link href="default_ie6.css" rel="stylesheet" type="text/css" /><![endif]-->

</head>

<body>

<div id="wrapper"> <!-- Will be closed in footer.jsp -->

	
	<div id="wrapper-bgtop"> <!-- Will be closed in footer.jsp -->
	
	<div id="header" class="container">
		<div id="logo">
			<a href="http://proteomicsresource.washington.edu/"><img src="/pr/images/UWPR_Logo/UWPR_logo_TimesNewRoman_fade.png" class="center" style="vertical-align: bottom; margin-top: 2px;"/></a>
		</div>
		<div id="search">
			<yrcwww:authenticated>
    			<jsp:useBean id="user" class="org.yeastrc.www.user.User" scope="session"/>
    			<yrcwww:user attribute="username"/> 
    			(<yrcwww:user attribute="firstname"/> <yrcwww:user attribute="lastname"/>)
    			<br/>
    			<bean:write name="user" property="researcher.organization"/>
   			</yrcwww:authenticated>
		   <yrcwww:notauthenticated>
		    	Not logged in.&nbsp;&nbsp;
		   </yrcwww:notauthenticated>
		</div>
	</div>
	
	<div id="menu" class="container">
	
	  <%
	  	String dir = (String)request.getAttribute("dir");
	  	String homeMenuClass = "";
	  	if(dir != null && (dir.equals("internal") || dir.equals("project")))
	  	{
	  		homeMenuClass = "active";
	  	}
	  	String accountMenuClass = "";
	  	if(dir != null && dir.equals("account"))
	  	{
	  		accountMenuClass = "active";
	  	}
	  	String adminMenuClass = "";
	  	if(dir != null && (dir.equals("admin")
	  	                   || dir.equals("search")
	  	                   || dir.equals("instrumentlog")
	  	                   || dir.equals("costcenter")))
	  	{
	  		adminMenuClass = "active";
	  	}
	  	String aboutMenuClass = "";
	  	if(dir != null && dir.equals("about"))
	  	{
	  		aboutMenuClass = "active";
	  	}
	  	
	   %>
	  <ul>
	  		<yrcwww:authenticated>
			     <li class="<%=homeMenuClass %>">
			         <html:link href="/pr/pages/internal/front.jsp"><span>Home</span></html:link>
			     </li>
			     <li class="<%=accountMenuClass %>">
			     	<html:link href="/pr/editInformation.do"><span>Account</span></html:link>
			     </li>
			     <yrcwww:member group="administrator">
				     <li class="<%=adminMenuClass %>">
				         <html:link href="/pr/pages/admin/search/searchProjects.jsp"><span>ADMIN</span></html:link>
				     </li>
			     </yrcwww:member>
			     <li class="<%=aboutMenuClass %>">
		         	<html:link href="/pr/pages/about/front.jsp"><span>About</span></html:link>
		     	 </li>
			     <li>
	     			<html:link href="/pr/logout.do"><span>Logout</span></html:link>
	     		 </li>
		     </yrcwww:authenticated>
		     
		     <yrcwww:notauthenticated>
		     	<li class="<%=aboutMenuClass %>">
		         	<html:link href="/pr/pages/about/front.jsp"><span>About</span></html:link>
		     	</li>
		     	<li>
		         	<html:link href="/pr/pages/internal/front.jsp">Login</html:link>
		     	</li>
   			 </yrcwww:notauthenticated>
		     
		</ul>
	</div>

	<div id="submenu">
		<logic:equal name="dir" scope="request" value="account">
		    <html:link href="/pr/editInformation.do">Information</html:link>
		    &nbsp;&nbsp;.&nbsp;&nbsp;
		    <html:link href="/pr/editPassword.do">Password</html:link>
		    &nbsp;&nbsp;.&nbsp;&nbsp;
		    <html:link href="/pr/editUsername.do">Username</html:link>
	   </logic:equal>
	   <logic:equal name="dir" scope="request" value="internal">
		    <html:link href="/pr/newResearcher.do">Add Researcher</html:link>
            &nbsp;&nbsp;.&nbsp;&nbsp;
            <html:link href="/pr/viewAllInstrumentCalendar.do">Instrument Calendar</html:link>
	   </logic:equal>
	   <logic:equal name="dir" scope="request" value="project">
	        <html:link href="/pr/newResearcher.do">Add Researcher</html:link>
	   </logic:equal>
	   
	   <yrcwww:member group="administrator">
	   	   <logic:equal name="dir" scope="request" value="admin">
		   		<%@ include file="/includes/admin_submenu.jsp" %>     
		   </logic:equal>
		   <logic:equal name="dir" scope="request" value="search">
		   		<%@ include file="/includes/admin_submenu.jsp" %>     
		   </logic:equal>
		   
		   <logic:equal name="dir" scope="request" value="costcenter">
		   		<%@ include file="/includes/admin_submenu.jsp" %>    
		   </logic:equal>
		   <logic:equal name="dir" scope="request" value="instrumentlog">
		   		<%@ include file="/includes/admin_submenu.jsp" %>    
		   </logic:equal>
	   </yrcwww:member>
	</div>
	
	<div id="breadcrumb">
		<yrcwww:authenticated><yrcwww:history/></yrcwww:authenticated>
	</div>
	
	<div id="page"> <!-- Will be closed in footer.jsp -->

