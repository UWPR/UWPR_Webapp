<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.apache.logging.log4j.LogManager,org.apache.logging.log4j.Logger" %>
<%--
  Generic error page, reached from the <error-page> entries in web.xml for an uncaught exception or
  a 500.  The failure detail is logged on the server.  Nothing about it, in particular any SQL a
  SQLException carries, reaches the browser.  Kept self-contained so it cannot itself throw and turn
  into an error loop.
--%>
<%
    Throwable failure = (Throwable) request.getAttribute("javax.servlet.error.exception");
    Object failedUri  = request.getAttribute("javax.servlet.error.request_uri");
    Object statusCode = request.getAttribute("javax.servlet.error.status_code");

    // A short reference the user can quote.  It is logged beside the detail, and the log's own
    // timestamp matches it, so a report can be traced to the failure.
    String reference = Long.toHexString(System.currentTimeMillis());

    Logger errorLog = LogManager.getLogger("org.uwpr.www.ErrorPage");
    errorLog.error("Unhandled error [" + reference + "] on " + failedUri + " (status " + statusCode + ")", failure);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
 <link rel="stylesheet" type="text/css" href="/pr/css/global.css"/>
 <title>UWPR - Something went wrong</title>
</head>
<body>
 <div style="max-width: 600px; margin: 40px auto; padding: 0 20px;">
  <h2>Something went wrong</h2>
  <p>An error on the server stopped this request from completing. Please try again.</p>
  <p>If it keeps happening, email the UWPR administrators and quote the reference below so they can
     find the matching log entry.</p>
  <p><b>Reference:</b> <%= reference %></p>
  <p><a href="/pr/">Return to the home page</a></p>
 </div>
</body>
</html>
