<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%
response.setHeader("Pragma", "No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
response.addHeader("Cache-control", "no-store"); // tell proxy not to cache
response.addHeader("Cache-control", "max-age=0"); // stale right away
%>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<yrcwww:notmember group="any">
 <logic:forward name="standardHome" />
</yrcwww:notmember>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">


<html>
<head>
 <yrcwww:title />

	<link href="http://fonts.googleapis.com/css?family=Arvo:400,700" rel="stylesheet" type="text/css" />
 	<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/default.css?v=01.28.18">
 	<!--[if lte IE 7]><link href="default_ie6.css" rel="stylesheet" type="text/css" /><![endif]-->
 	
	<link REL="stylesheet" TYPE="text/css" HREF="/pr/css/calendar.css" />
	<link rel="stylesheet" type="text/css" href="yui/build/fonts/fonts-min.css" />
	<link rel="stylesheet" type="text/css" href="yui/build/calendar/assets/calendar.css" />

</head>

<body>

<div id="wrapper"> <!-- Will be closed in footer.jsp -->

	
	<div id="wrapper-bgtop"> <!-- Will be closed in footer.jsp -->
	
	
	<div id="header" class="container">
		<div id="logo">
			<img src="/pr/images/UWPR_Logo/UWPR_logo_TimesNewRoman_fade.png" class="center" style="vertical-align: bottom; margin-top: 2px;"/>
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
	  <ul>
	  		<yrcwww:authenticated>
			     <li>
			         <html:link href="/pr/pages/internal/front.jsp"><span>Home</span></html:link>
			     </li>
			     <li>
			     	<html:link href="/pr/editInformation.do"><span>Account</span></html:link>
			     </li>
			     <yrcwww:member group="administrator">
				     <li>
				         <html:link forward="adminHome"><span>ADMIN</span></html:link>
				     </li>
			     </yrcwww:member>
			     <li>
		         	<html:link href="/pr/pages/about/front.jsp"><span>About</span></html:link>
		     	 </li>
			     <li>
	     			<html:link href="/pr/logout.do"><span>Logout</span></html:link>
	     		 </li>
		     </yrcwww:authenticated>
		     
		     <yrcwww:notauthenticated>
		     	<li>
		         	<html:link href="/pr/pages/about/front.jsp"><span>About</span></html:link>
		     	</li>
		     	<li>
		         	<html:link href="/pr/pages/internal/front.jsp">Login</html:link>
		     	</li>
   			 </yrcwww:notauthenticated>
		     
		</ul>
	</div>

	<div id="submenu">
		<logic:equal name="dir" scope="request" value="search">
		    <html:link href="/pr/editInformation.do">Information</html:link>
		    &nbsp;&nbsp;.&nbsp;&nbsp;
		    <html:link href="/pr/editPassword.do">Password</html:link>
		    &nbsp;&nbsp;.&nbsp;&nbsp;
		    <html:link href="/pr/editUsername.do">Username</html:link>
	   </logic:equal>
	   <logic:equal name="dir" scope="request" value="internal">
			<html:link href="/pr/newResearcher.do">Add Researcher</html:link>
	   </logic:equal>
	   <logic:equal name="dir" scope="request" value="project">
	        <html:link href="/pr/newResearcher.do">Add Researcher</html:link>
	   </logic:equal>
	   <logic:equal name="dir" scope="request" value="admin">
	        <html:link href="/pr/newResearcher.do">Add Researcher</html:link>
	   </logic:equal>
	</div>
	
	
	<div id="page"> <!-- Will be closed in footer.jsp -->
	
	
<table BORDER="0" WIDTH="100%" CELLPADDING="0" CELLSPACING="0">

 <tr>

  <td WIDTH="478" VALIGN="BOTTOM" COLSPAN="2">
   <nobr><img SRC="/pr/images/left-top-round.gif" WIDTH="15" HEIGHT="15"><img SRC="/pr/images/uwpr_logo.png" WIDTH="222" HEIGHT="44" ALT="YRC LOGO"><img SRC="/pr/images/double-side-round.gif" WIDTH="15" HEIGHT="15">
   <html:link forward="adminSearch"><IMG SRC="/pr/images/tabs/tab-top-admin_search.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
   <html:link href="/pr/uploadRedirect.do"><IMG SRC="/pr/images/tabs/tab-top-admin_upload.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
   <yrcwww:member group="administrators">
   <html:link href="/pr/manageGroups.do"><IMG SRC="/pr/images/tabs/tab-top-admin_groups.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
   </yrcwww:member>
   <html:link forward="standardHome"><IMG SRC="/pr/images/tabs/tab-top-standard.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
   <yrcwww:member group="administrators">
   <html:link action="allInstrumentUsageSummary"><IMG SRC="/pr/images/tabs/tab-top-ms-log.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
   <html:link action="costCenterHome"><IMG SRC="/pr/images/tabs/tab-top-cost-center.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link></yrcwww:member>
   <html:link href="/pr/logout.do"><IMG SRC="/pr/images/tabs/tab-top-logout.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link></nobr></td>

  <td WIDTH="100%" style="text-align:right;">
   <yrcwww:authenticated>
    <jsp:useBean id="user" class="org.yeastrc.www.user.User" scope="session"/>
    <FONT STYLE="font-size:8pt;">You are: <yrcwww:user attribute="username"/> (<yrcwww:user attribute="firstname"/> <yrcwww:user attribute="lastname"/>)<BR>
    <bean:write name="user" property="researcher.organization"/></FONT>
   </yrcwww:authenticated>
   <yrcwww:notauthenticated>
    Not logged in.&nbsp;&nbsp;
   </yrcwww:notauthenticated>
  </td>
 </tr>

 <tr BGCOLOR="#808080">
  <td VALIGN="CENTER" WIDTH="236" BGCOLOR="#808080" COLSPAN="2"><nobr><img SRC="/pr/images/left-bottom-round.gif" WIDTH="15" HEIGHT="20"><img SRC="/pr/images/title-text.gif" WIDTH="221" HEIGHT="20"></nobr></td>

  <td BGCOLOR="#808080" style="text-align:right;" WIDTH="100%">
   <img SRC="/pr/images/right-round.gif" WIDTH="15" HEIGHT="20"></td>

 </tr>

 <tr BGCOLOR="#FFFFFF">

  <td BGCOLOR="#FFFFFF" COLSPAN="3" ALIGN="LEFT" VALIGN="top"><NOBR>&nbsp;&nbsp;&nbsp;
   <!--<IMG SRC="/pr/images/tabs/tab-bottom-yrc_administration.png" WIDTH="200" HEIGHT="15" BORDER="0">-->
   <logic:equal name="dir" scope="request" value="search">
    <html:link href="/pr/pages/admin/search/searchProjects.jsp">
    <IMG SRC="/pr/images/tabs/tab-bottom-admin_search_projects.png" WIDTH="100" HEIGHT="15" BORDER="0">
    </html:link>
   </logic:equal>
   <logic:equal name="dir" scope="request" value="upload">
		<yrcwww:member group="MacCoss">
			<html:link href="/pr/uploadMacCossFormAction.do"><IMG SRC="/pr/images/tabs/tab-bottom-upload-maccoss.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
			<html:link href="/pr/listUploadJobs.do"><IMG SRC="/pr/images/tabs/tab-bottom-list-jobs.png" WIDTH="100" HEIGHT="15" BORDER="0"></html:link>
		</yrcwww:member>
   </logic:equal>

  </NOBR></td>


 </tr>

</table>

   <yrcwww:authenticated><yrcwww:history/></yrcwww:authenticated>

<br>