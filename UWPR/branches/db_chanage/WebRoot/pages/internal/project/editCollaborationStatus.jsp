<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="editCollaborationStatusForm">
	<logic:forward name="editCollaborationStatus" />
</logic:notPresent>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<script src="/pr/js/jquery-1.5.min.js"></script>

<script>

$(document).ready(function() {
	
	if($("input:radio[name=statusShortName]:checked").val() != 'R' ) {
   		$("#rejectionReasons_div").hide();
   	}
   	if($("input:radio[name=statusShortName]:checked").val() != 'V' ) {
   		$("#emailComments_tr").hide();
   	}
   	
   	$("input:radio[name=statusShortName]").click(function() { 
   		var showRejectionReasons = false;
   		var showEmailComments = false;
   		if($(this).val() == 'R') {
   			showRejectionReasons = true;
   		}
   		if($(this).val() == 'V') {
   			showEmailComments = true;
   		}
   		if(showRejectionReasons)	$("#rejectionReasons_div").show();
   		else                        $("#rejectionReasons_div").hide();
   		if(showEmailComments)	$("#emailComments_tr").show();
   		else                        $("#emailComments_tr").hide();
   	});
    
});

function addRejectionReason() {
	var winHeight = 500;
	var winWidth = 700;
	var doc = "/pr/newRejectionCause.do";
	window.open(doc, "REJECT_CAUSE_WINDOW", "width=" + winWidth + ",height=" + winHeight + ",status=no,resizable=yes,scrollbars=yes");
}

function updateRejectionReasons(id, cause) {
	$('#rejectionList tr:last').after('<tr><td style="font-size: 8pt;"><input type="checkbox" value="'+id+'" name="reviewerRejectionCauseIds" /> '+cause+'</td></tr>');
}

function onCancel() {
	document.location = "/pr/viewProject.do?ID="+<bean:write name="editCollaborationStatusForm" property="ID"/>;
}
</script>


<yrcwww:contentbox title="Update Collaboration Status">
<!-- This should be visible only to users with admin privileges -->
<yrcwww:member group="administrators">

<html:form action="saveCollaborationStatus" method="post" styleId="form1" >
	<CENTER>
	<TABLE CELLPADDING="no" CELLSPACING="0">

	<tr>
		<TD WIDTH="25%" VALIGN="top"><b>Project ID: </b></TD>
		<TD WIDTH="75%" VALIGN="top"><b>
			<html:hidden name="editCollaborationStatusForm" property="ID"/>
			<html:link href="/pr/viewProject.do" 
			           paramId="ID" 
			           paramName="editCollaborationStatusForm" paramProperty="ID">
			<bean:write name="editCollaborationStatusForm" property="ID" />
			</html:link>
		</b></TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top"><b>Title: </b></TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationStatusForm" property="title"/>
			<bean:write name="editCollaborationStatusForm" property="title"/>
		</TD>
	</tr>
	
	<logic:present name="editCollaborationReviewForm" property="dateAccepted">  
   <TR>
   		<TD WIDTH="25%" VALIGN="top" style="padding-top:10px">Date Accepted:</TD>
    	<TD WIDTH="75%" VALIGN="top" style="padding-top:10px">
    		<html:hidden name="editCollaborationReviewForm" property="dateAccepted"/>
    		<bean:write name="editCollaborationReviewForm" property="dateAccepted" />
    	</TD>
   </TR>
   </logic:present>
   
	<tr><td colspan="2" style="padding-top:20"><hr></td></tr>
	
	<TR>
    <TD WIDTH="25%" VALIGN="top" style="padding-top:10px">Collaboration status:</TD>
    <TD WIDTH="75%" VALIGN="top" style="padding-top:10px">	
     <logic:present name="collabStatusOptions">
     	<logic:iterate name="collabStatusOptions" id="option" indexId="index"
     				   type="org.yeastrc.project.CollaborationStatus">
     		<%String optionShortName = option.getShortName();%>
     		<NOBR>
     		<html:radio name="editCollaborationStatusForm" property="statusShortName" 
     				value="<%=optionShortName %>" 
     				styleId="<%=optionShortName %>"/><bean:write name="option" property="longName" />
     		</NOBR>
     		<logic:equal name="index" value="2">
     			<br>
     		</logic:equal>
     	</logic:iterate>
     </logic:present>
    </TD>
   </TR>
   
   <TR style="visibility: none;">
		<td width="25%"></td>
   		<TD width="75%" VALIGN="top" style="padding-top:10px; padding-bottom:10px;" align="center">
   		<div id="rejectionReasons_div">
   		<span style="font-weight: bold; font-size: 8pt;">Rejection Reasons (select at least one)</span> 
   		<br>
   		<table style="border: 1px solid gray;" id="rejectionList">
   			<logic:iterate name="collabRejectionReasons" id="cause" type="org.yeastrc.project.CollaborationRejectionCause">
   				<%String causeId = String.valueOf(cause.getId()); %>
   				<tr><td style="font-size: 8pt;">
   				<html:multibox name="editCollaborationStatusForm" property="rejectionCauses" value="<%=causeId %>" />
   				<bean:write name="cause" property="cause" />
   				</td></tr>
   			</logic:iterate>
   		</table>
   		<a href="javascript:addRejectionReason()" style="margin-bottom:10"><b>Add</b></a>
   		</div>
   		</TD>
   </TR>
   
   <TR style="visibility: none;" id="emailComments_tr">
		<td width="25%"></td>
   		<TD width="75%" VALIGN="top" style="padding-top:10px; padding-bottom:10px;" align="center">
   			Email Comments:<br><font color="red">(Included in e-mail to investigators)</font><br>
   			<html:textarea property="emailComments" rows="5" cols="50"></html:textarea>
   		</TD>
   </TR>
   
   <tr><td colspan="2" align="center" style="padding:10px;">
    	<html:submit styleClass="button">Save</html:submit>
    	 <input type="button" class="button" onclick="onCancel();" value="Cancel"/>
    </td></tr>
	</TABLE>
	</CENTER>
	</html:form>
</yrcwww:member>
</yrcwww:contentbox>


<!-- ============================================================================ -->
   	
<%@ include file="/includes/footer.jsp" %>