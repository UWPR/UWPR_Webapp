<%@ page import="org.yeastrc.project.Researcher" %>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="memberList">
  <logic:forward name="manageGroupMembers" />
</logic:notPresent>
 
<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<link rel="stylesheet" href="css/kendo-ui-core/styles/kendo.common.min.css"/>
<link rel="stylesheet" href="css/kendo-ui-core/styles/kendo.fiori.min.css"/>
<script src="js/kendo-ui-core/jquery.min.js"></script>
<script src="js/kendo-ui-core/kendo.ui.core.min.js"></script>


<script type="text/javascript">

    $(document).ready(function() {
        var researcherSelector = $("#researcherSelector").kendoMultiSelect(
                {
                    autoClose:false,
                    filter:"contains"

                }
        ).data("kendoMultiSelect");

        $("#manageGroupMembersForm").submit(function()
        {
            var researcherIds = researcherSelector.value();
            // console.log("selected IDs: " + researcherIds);
            for(var i = 0; i < researcherIds.length; i += 1)
            {
                $(this).append('<input type="hidden" name="selectedResearcher[' + i + '].ID" value="' + researcherIds[i] + '"/>');
            }
            return true;
        });
    });

</script>


<yrcwww:contentbox title="Manage Group Members">

<center>
<div style="width:60%" align="left">
 <P>Listed below are members of the group: <B><bean:write name="groupName" scope="request"/></B>.

 <P><b>Current members:</b><BR>

  <TABLE BORDER="0" WIDTH="80%" class="striped">

   <logic:iterate id="researcher" name="memberList">

    <TR>
     <TD WIDTH="50%"><bean:write name="researcher" property="firstName"/>
     				 <bean:write name="researcher" property="lastName"/></TD>
     <TD WIDTH="50%">
      <jsp:useBean id="params" class="java.util.HashMap"/>
	   <%
	    params.put( "action", "delete");
	    params.put( "groupName", request.getAttribute("groupName"));
	    params.put( "researcherID", new Integer(((org.yeastrc.project.Researcher)(researcher)).getID()) );
       %>
       <html:link href="/pr/manageGroupMembers.do" name="params">Remove from Group</html:link>
     </TD>
    </TR>

   </logic:iterate>

  </TABLE>
  <logic:empty name="memberList">
   <B>No Members</B>
  </logic:empty>

  <HR>


    <html:form action="manageGroupMembers" method="POST" styleId="manageGroupMembersForm">
        <html:hidden name="manageGroupMembersForm" property="groupName"/>
        <input type="hidden" name="action" value="add">

        <div>
        <label for="researcherSelector" style="font-weight:bold;">Add a new member to this group:</label>
        <select name="researchers" id="researcherSelector" multiple="multiple" data-placeholder="Select one or more researchers...">
            <logic:iterate id="selectedResearcher" indexId="index" name="researchers">
               <option value="<%=String.valueOf(((Researcher)selectedResearcher).getID())%>">
                   <bean:write name="selectedResearcher" property="listing"/>
               </option>
            </logic:iterate>
        </select>
        </div>
        <div style="margin-top:15px;">
            <input type="submit" class="k-button" value="Add to group">
        </div>
   </html:form>

   
</div>
</center>
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>