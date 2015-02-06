
<%@page import="org.yeastrc.project.CollaborationStatus"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="editCollaborationReviewForm">
	<logic:forward name="editCollaborationReview" />
</logic:notPresent>


<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script>
$(document).ready(function() {
	
	if($("input:radio[name=recommendedStatusShortName]:checked").val() != 'R' ) {
   		$("#rejectionReasons_div").hide();
   	}
   	
   	$("input:radio[name=recommendedStatusShortName]").click(function() { 
   		var showRejectionReasons = false;
   		if($(this).val() == 'R') {
   			showRejectionReasons = true;
   		}
   		if(showRejectionReasons)	$("#rejectionReasons_div").show();
   		else                        $("#rejectionReasons_div").hide();
   	});
    
});

function addRejectionReason() {
	var winHeight = 500
	var winWidth = 700;
	var doc = "/pr/newRejectionCause.do";
	window.open(doc, "REJECT_CAUSE_WINDOW", "width=" + winWidth + ",height=" + winHeight + ",status=no,resizable=yes,scrollbars=yes");
}

function updateRejectionReasons(id, cause) {
	$('#rejectionList tr:last').after('<tr><td style="font-size: 8pt;"><input type="checkbox" value="'+id+'" name="reviewerRejectionCauseIds" /> '+cause+'</td></tr>');
}

function onCancel(projectID) {
	document.location = "/pr/viewProject.do?ID="+projectID;
}
function onSave() {
	if(confirm("Are you sure you want to submit this review?  This action may trigger an email to the investigator(s).")) {
          document.forms["reviewForm"].submit();
    }
    else {
    	return false;
    }
}
function onSaveDraft() {
	
	document.forms["reviewForm"].elements["draft"].value="true";
	document.forms["reviewForm"].submit();
}
	
</script>

<yrcwww:contentbox title="Review Collaboration Request">
<!-- ============================================================================ -->
<!-- This should be visible only to users with admin privileges -->
<yrcwww:member group="administrators">

<html:form action="saveCollaborationReview" method="post" styleId="reviewForm">
	<CENTER>
	<html:hidden name="editCollaborationReviewForm" property="draft" styleId="draft" />
	
	<TABLE CELLPADDING="no" CELLSPACING="0">
	
	<tr>
		<TD WIDTH="25%" VALIGN="top"><b>Project ID: </b></TD>
		<TD WIDTH="75%" VALIGN="top"><b>
			<html:hidden name="editCollaborationReviewForm" property="ID"/>
			<html:link href="/pr/viewProject.do" 
			           paramId="ID" 
			           paramName="editCollaborationReviewForm" paramProperty="ID">
			<bean:write name="editCollaborationReviewForm" property="ID" />
			</html:link>
		</b></TD>
	</tr>
	
	<!--  ANCESTORS of this project, if any -->
	  <logic:notEqual name="editCollaborationReviewForm" property="parentProject" value="0">
	  <tr>
	  	<TD valign="top" width="25%"><b>Parent Project:</b></TD>
	  	<TD valign="top" width="75%">
	  		<html:hidden name="editCollaborationReviewForm" property="parentProject"/>
	  		<html:link href="/pr/viewProject.do" 
			           paramId="ID" 
			           paramName="editCollaborationReviewForm" paramProperty="parentProject">
			<bean:write name="editCollaborationReviewForm" property="parentProject"/>
			</html:link>
	  	</TD>
	  	</tr>
	  </logic:notEqual>
	  
	<tr>
		<TD WIDTH="25%" VALIGN="top"><b>Title: </b></TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="title"/>
			<bean:write name="editCollaborationReviewForm" property="title"/>
		</TD>
	</tr>
	
	
	
	<tr><td colspan="2" style="padding-top:20"><hr></td></tr>
	<tr><td colspan="2" style="padding-bottom:20"><hr></td></tr>
	<!-- ============================================================================================== -->
	<!-- REVIEWER 1 -->
	<!-- ============================================================================================== -->
	<logic:notEqual name="editCollaborationReviewForm" property="reviewerId" value="0">
	<tr>
		<TD WIDTH="25%" VALIGN="top"><b>Reviewer:</b></TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="reviewerId"/>
			<html:hidden name="editCollaborationReviewForm" property="reviewerName"/>
			<b><bean:write name="editCollaborationReviewForm" property="reviewerName"/></b></TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top">Recommend:</TD>
		<TD WIDTH="75%" VALIGN="top">
			<NOBR>
	     		<html:radio name="editCollaborationReviewForm" property="recommendedStatusShortName" 
	     				value="<%=CollaborationStatus.ACCEPTED.getShortName() %>" 
	     				styleId="<%=CollaborationStatus.ACCEPTED.getShortName() %>"/>
	     				<%=CollaborationStatus.ACCEPTED.getAltName() %>
	     	</NOBR>
	     	<NOBR>
	     		<html:radio name="editCollaborationReviewForm" property="recommendedStatusShortName" 
	     				value="<%=CollaborationStatus.REVISE.getShortName() %>" 
	     				styleId="<%=CollaborationStatus.REVISE.getShortName() %>"/>
	     				<%=CollaborationStatus.REVISE.getAltName() %>
	     	</NOBR>
	     	<NOBR>
	     		<html:radio name="editCollaborationReviewForm" property="recommendedStatusShortName" 
	     				value="<%=CollaborationStatus.REJECTED.getShortName() %>" 
	     				styleId="<%=CollaborationStatus.REJECTED.getShortName() %>"/>
	     				<%=CollaborationStatus.REJECTED.getAltName() %>
	     	</NOBR>
     	</TD>
	</tr>
	
	<TR style="visibility: hidden;">
   		<TD width="75%" colspan="2" VALIGN="top" style="padding-top:10px; padding-bottom:10px;text-align:center;">
   		<div id="rejectionReasons_div">
   		<span style="font-weight: bold; font-size: 8pt;">Rejection Reasons</span>
   		<br>
   		<span style="color:#FF0000;font-size:8pt;">Please select <b>at least one</b> reason. <br>Selected reasons will be included in the e-mail to the investigator(s).</span> 
   		<br>
   		<table style="border: 1px solid gray;" id="rejectionList">
   			<logic:iterate name="collabRejectionReasons" id="cause" type="org.yeastrc.project.CollaborationRejectionCause">
   				<%String causeId = String.valueOf(cause.getId()); %>
   				<tr><td style="font-size: 8pt;">
   				<html:multibox name="editCollaborationReviewForm" property="reviewerRejectionCauseIds" value="<%=causeId %>" />
   				<bean:write name="cause" property="cause" />
   				</td></tr>
   			</logic:iterate>
   		</table>
   		<a href="javascript:addRejectionReason()" style="margin-bottom:10"><b>Add</b></a>
   		</div>
   		</TD>
   </TR>
	
	<tr>
		<TD valign="top" width="25%">Comments:<br>
		<span style="color:red; font-size:10pt;">(NOT seen by investigators)</span>
		</TD>
		<TD valign="top" width="75%">
			<html:textarea name="editCollaborationReviewForm" property="reviewerComments" rows="5" cols="50" />
		</TD>
	</tr>
	
	<tr>
		<TD valign="top" width="25%">Email Comments:<br>
		<span style="color:red; font-size:10pt;">(Included in e-mail to investigators)</span>
		</TD>
		<TD valign="top" width="75%">
			<html:textarea name="editCollaborationReviewForm" property="emailComments" rows="5" cols="50" />
		</TD>
	</tr>
	
	<tr><td colspan="2" align="center" style="padding:10px;">
    	<input type="button" class="button" onclick="onSave();" value="Submit Review"/>
    	<input type="button" class="button" onclick="onSaveDraft();" value="Save Comments"/>
    	<input type="button" class="button" onclick="onCancel(<bean:write name="editCollaborationReviewForm" property="ID"/>);" value="Cancel"/>
    	<br>
    	<span style="font-size:8pt; color:red; margin-top:15px;">Click on "Save Comments" to save your comments. This will NOT trigger an email to the investigator(s).</span>
    </td></tr>
	
	
  	</logic:notEqual>
	<tr><td colspan="2" ><hr></td></tr>
	<tr><td colspan="2" style="padding-bottom:20"><hr></td></tr>
	
	
	
	
	
	<tr>
		<TD WIDTH="25%" VALIGN="top">Scientific question: </TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="scientificQuestion"/>
			<bean:write name="editCollaborationReviewForm" property="scientificQuestion"/>
		</TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top">Abstract: </TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="abstract"/>
			<bean:write name="editCollaborationReviewForm" property="abstract"/>
		</TD>
	</tr>

	<!-- EXTENSION REASONS -->
  <logic:present name="editCollaborationReviewForm" property="extensionReasons">
  	<tr>
  		<TD valign="top" width="25%">Project Extension Reason(s):</TD>
  		<TD valign="top" width="75%">
  			<html:hidden name="editCollaborationReviewForm" property="extensionReasons" />
  			<bean:write name="editCollaborationReviewForm" property="extensionReasons" filter="false"/>
  		</TD>
  	</tr>
  </logic:present>


	<tr>
		<TD WIDTH="25%" VALIGN="top">Lab Director: </TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="PI"/>
			<bean:write name="editCollaborationReviewForm" property="PI"/>
		</TD>
	</tr>

    <logic:iterate id="researcher" property="researcherList" name="editCollaborationReviewForm" indexId="cnt">
        <tr>
            <TD valign="top" width="25%">Researcher :</TD>
            <TD valign="top" width="75%">
                <bean:write name="researcher"/>
            </TD>
        </tr>
    </logic:iterate>

	<tr>
		<TD WIDTH="25%" VALIGN="top"># Runs requested: </TD>
		<TD WIDTH="75%" VALIGN="top">
			<table width="50%">
				<tr>
					<td>LTQ:<td>
					<td>
						<html:hidden name="editCollaborationReviewForm" property="ltqRunsRequested" />
						<bean:write name="editCollaborationReviewForm" property="ltqRunsRequested" />
					</td>
					<td width="10%">&nbsp;</td>
					<td>LTQ-FT:<td>
					<td>
						<html:hidden name="editCollaborationReviewForm" property="ltq_ftRunsRequested" />
						<bean:write name="editCollaborationReviewForm" property="ltq_ftRunsRequested" />
					</td>
				</tr>
				<tr>
					<td>LTQ-ETD:<td>
					<td>
						<html:hidden name="editCollaborationReviewForm" property="ltq_etdRunsRequested" />
						<bean:write name="editCollaborationReviewForm" property="ltq_etdRunsRequested" />
					</td>
					<td width="10%">&nbsp;</td>
					<td>LTQ-Orbitrap:<td>
					<td><bean:write name="editCollaborationReviewForm" property="ltq_orbitrapRunsRequested" /></td>

				</tr>
				<tr>
					<td>TSQ-Access:<td>
					<td>
						<html:hidden name="editCollaborationReviewForm" property="tsq_accessRunsRequested" />
						<bean:write name="editCollaborationReviewForm" property="tsq_accessRunsRequested" />
					</td>
					<td width="10%">&nbsp;</td>
					<td>TSQ-Vantage:<td>
					<td>
						<html:hidden name="editCollaborationReviewForm" property="tsq_vantageRunsRequested" />
						<bean:write name="editCollaborationReviewForm" property="tsq_vantageRunsRequested" />
					</td>
				</tr>
			</table>
		</TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top">Fragmentation types: </TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="fragmentationTypes"/>
			<bean:write name="editCollaborationReviewForm" property="fragmentationTypes"/>
		</TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top">Mass Spec. analysis by UWPR personnel? </TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="massSpecExpertiseRequested"/>
			<bean:write name="editCollaborationReviewForm" property="massSpecExpertiseRequested"/>
		</TD>
	</tr>
	<tr>
		<TD WIDTH="25%" VALIGN="top">Database search at UWPR? </TD>
		<TD WIDTH="75%" VALIGN="top">
			<html:hidden name="editCollaborationReviewForm" property="databaseSearchRequested"/>
			<bean:write name="editCollaborationReviewForm" property="databaseSearchRequested"/>
		</TD>
	</tr>

	</TABLE>
	</CENTER>
	</html:form>
</yrcwww:member>
</yrcwww:contentbox>


<!-- ============================================================================ -->
   	
<%@ include file="/includes/footer.jsp" %>