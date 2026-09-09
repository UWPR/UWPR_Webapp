<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<!-- Make sure we have our Collections defined, if not, go get them -->
<logic:notPresent name="activeProjects" scope="request">
	<logic:forward name="standardHome"/>
</logic:notPresent>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/errors.jsp" %>

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<script type='text/javascript' src='/pr/js/jquery.tablesorter.min.js'></script>

<style>
	/* Sort arrows, copied from css/tablesorter.css but scoped to table.striped.
	    - the tablesorter class is not used here, it forces 8pt text and hides the row stripes
	    - image paths are absolute because this block is inlined, not in css/ */
	table.striped thead tr .header {
		background-image: url(/pr/images/tablesorter/bg.gif);
		background-repeat: no-repeat;
		background-position: center right;
		cursor: pointer;
	}
	table.striped thead tr .headerSortUp {
		background-image: url(/pr/images/tablesorter/asc.gif);
	}
	table.striped thead tr .headerSortDown {
		background-image: url(/pr/images/tablesorter/desc.gif);
	}
	table.striped thead tr .headerSortDown, table.striped thead tr .headerSortUp {
		background-color: #B0C4DE;
	}
</style>

<script>
// jQuery here is 1.5, which predates .prop().  Use attr()/removeAttr() -- .prop() does
// nothing and the buttons never enable.

// Enable the button only while a checkbox in its table is ticked.
function updateArchiveButton(tableId, buttonId) {
	var anyChecked = $("#" + tableId + " tbody input.projectCheck:checked").length > 0;
	if (anyChecked) {
		$("#" + buttonId).removeAttr("disabled");
	}
	else {
		$("#" + buttonId).attr("disabled", "disabled");
	}
}

$(document).ready(function() {

	// Keep these indexes in step with the header rows below.  0-based:
	//   0 checkbox, 1 View, 2 ID, 3 Title, 4 Type, 5 Submit Date, 6 Status, 7 overdue marker
	var sorterOptions = {
		sortList: [[2,1]],          // newest first, matching the order from the database
		headers: {
			0: { sorter: false },
			1: { sorter: false },
			7: { sorter: false }
		}
	};

	$("#your_projects_table").tablesorter(sorterOptions);
	$("#archived_projects_table").tablesorter(sorterOptions);

	// Delegated handlers, so they survive tablesorter reordering the rows.
	$("#your_projects_table").delegate("input.projectCheck", "click", function() {
		updateArchiveButton("your_projects_table", "archive_selected_button");
	});
	$("#archived_projects_table").delegate("input.projectCheck", "click", function() {
		updateArchiveButton("archived_projects_table", "unarchive_selected_button");
	});

	// Links rather than a header checkbox -- a checkbox in a tablesorter header also sorts.
	$("#select_all_active").click(function() {
		$("#your_projects_table tbody input.projectCheck").attr("checked", "checked");
		updateArchiveButton("your_projects_table", "archive_selected_button");
		return false;
	});
	$("#select_none_active").click(function() {
		$("#your_projects_table tbody input.projectCheck").removeAttr("checked");
		updateArchiveButton("your_projects_table", "archive_selected_button");
		return false;
	});
	$("#select_all_archived").click(function() {
		$("#archived_projects_table tbody input.projectCheck").attr("checked", "checked");
		updateArchiveButton("archived_projects_table", "unarchive_selected_button");
		return false;
	});
	$("#select_none_archived").click(function() {
		$("#archived_projects_table tbody input.projectCheck").removeAttr("checked");
		updateArchiveButton("archived_projects_table", "unarchive_selected_button");
		return false;
	});

	// Archived list starts collapsed, since the point is to keep it out of the way.
	$("#toggle_archived").click(function() {
		var section = $("#archived_projects_section");
		section.toggle();
		$(this).text(section.is(":visible") ? "hide" : "show");
		return false;
	});

	// Initial state, in case the browser restored ticked boxes on a back-navigation.
	updateArchiveButton("your_projects_table", "archive_selected_button");
	updateArchiveButton("archived_projects_table", "unarchive_selected_button");
});
</script>

<logic:notEmpty name="notices" scope="request">
<yrcwww:contentbox title="Notices">
	<logic:iterate id="notice" name="notices">
		<div style="margin: 6px 0; padding: 8px 12px; border-left: 4px solid #d9822b; background-color: #fff7e6;">
			<div style="white-space: pre-wrap;"><bean:write name="notice" property="message"/></div>
			<div style="font-size: 8pt; color: #888; margin-top: 4px;">
				Posted on
				<bean:write name="notice" property="startDateString"/>
			</div>
		</div>
	</logic:iterate>
</yrcwww:contentbox>
</logic:notEmpty>

<yrcwww:contentbox title="Welcome">

<P>Welcome to the internal Proteomics Resource web site.</P>

<logic:present name="showReminder">
	<!--
	<div id="acknowledgementReminder" style="color:red;font-weight:bold;margin-top:20px;margin-bottom:20px;">
		Please acknowledge the University of Washington's proteomics resource in your publications.
	</div>
	-->
</logic:present>

	<p>
From here, you can view and update your abstract information, request new collaborations and retrieve your proteomics data. 
To initiate a new project you need to submit a short project description and billing information.
</p>

<ul>
		<li>
			<b><html:link href="/pr/newBilledProject.do">Start new collaboration</html:link></b>
		</li>
		<li>
			<a href="/pr/costcenter_resources/UWPR_Current_Rates.pdf">Current instrument rates</a>
		</li>
		<li>
			<a href="/pr/costcenter_resources/UWPR_FAQ_Instrument_scheduling.pdf">Billing FAQ and instructions for scheduling instrument time</a>
		</li>
		<li>
			<html:link href="../admin/costcenter/paymentInformation.jsp">Payment information</html:link>
		</li>
        <li>
            <a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">Sample Submission Form</a>
        </li>
</ul>

<p>
UW members will be able to schedule instrument time immediately after submitting both project details and billing information. 
Please make sure to coordinate with UWPR personnel to get access to the facility and schedule training time. 
All collaborators must undergo the appropriate training at the beginning and during their scheduled instrument time. 
There is a 30 minute setup time required at the beginning of your scheduled instrument time for UWPR personnel to get the instrument ready.
</p>

<p>
	UW collaborators interested in fee for service analyses will have to coordinate sample analysis with UWPR personnel.
	Contact Michael Hoopmann (hoopmann@uw.edu) for more details. The <b><a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">Sample Submission Form</a></b>
can be downloaded <a href="/pr/costcenter_resources/UWPR sample submission form.xlsx">here</a>.
</p>

<p>
The UWPR reserves the right to cancel a collaboration at any time for any reason without notice. All fees are subject to change. 
</p>

<p>
The UW Proteomics Resource should be acknowledged as follows in publications: 
<b>This work is supported in part by the University of Washington's Proteomics Resource (UWPR95794).</b>
</p>

<div style="color:red; margin-bottom:20px">
Please NOTE: We are not equipped or licensed to handle either biohazards or radioisotopes and, 
consequently, will NOT work with either pathogenic or radioactive materials.
</div>



<!-- SHOW ALL PROJECTS, FOR WHICH THIS USER IS LISTED AS A RESEARCHER -->
<yrcwww:contentbox title="Your Projects" innerBox="true">

<logic:notEmpty name="activeProjects" scope="request">
<form action="/pr/archiveProjects.do" method="post">
<input type="hidden" name="archived" value="true"/>

 <TABLE BORDER="0" WIDTH="100%" id="your_projects_table" class="striped">

 <thead>
  <TR>
   <TH>&nbsp;</TH>
   <TH>&nbsp;</TH>
   <TH><U>ID</U></TH>
   <TH><U>Title</U></TH>
   <TH><U>Type</U></TH>
   <TH><U>Submit Date</U></TH>
   <TH><U>Collaboration<br>Status</U></TH>
   <TH>&nbsp;</TH>
  </TR>
</thead>

<tbody>
<logic:iterate id="project" name="activeProjects" scope="request">

  <TR>
  <TD valign="top" align="center">
   <input type="checkbox" class="projectCheck" name="projectIds" value="<bean:write name="project" property="ID"/>"/>
  </TD>
  <TD valign="top">
   <NOBR>
    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
   </NOBR>
  </TD>
  <TD valign="top"><bean:write name="project" property="ID"/></TD>
  <TD valign="top"><bean:write name="project" property="title"/></TD>
  <TD valign="top">
  	<logic:equal name="project" property="shortType" value="C">
  		UWPR Supported
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Billed
  	</logic:equal>
  </TD>
  <TD valign="top"><bean:write name="project" property="submitDate"/></TD>
  <TD valign="top">
  	<logic:equal name="project" property="shortType" value="C">
  		<bean:write name="project" property="collaborationStatus"/>
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Active
  	</logic:equal>
  </TD>
  <TD>
  	<logic:equal name="project" property="shortType" value="C">
	  	<logic:equal name="project" property="progressReportOverdue" value="true">
	  		<span style="color: red; font-weight: bold; font-size: 8pt;">Report Overdue</span>
	  	</logic:equal>
  	</logic:equal>
  </TD>
  </TR>
</logic:iterate>
</tbody>
 </TABLE>

 <div style="margin-top: 8px;">
  <input type="submit" id="archive_selected_button" value="Archive Selected" disabled="disabled"/>
  &nbsp;&nbsp;
  <span style="font-size: 8pt;">
   <a href="#" id="select_all_active">select all</a> /
   <a href="#" id="select_none_active">none</a>
  </span>
  &nbsp;&nbsp;
  <span style="font-size: 8pt; color: #666;">Archiving moves a project to the Archived list.  Nothing is deleted.</span>
 </div>

</form>
</logic:notEmpty>

<logic:empty name="activeProjects" scope="request">
	<logic:empty name="archivedProjects" scope="request">
	<p>You have not yet submitted an abstract for collaboration.
	<br/>
	For a billed project <html:link href="/pr/newBilledProject.do">click here</html:link>.
	</p>
	</logic:empty>
	<logic:notEmpty name="archivedProjects" scope="request">
	<p>All of your projects are archived.  See the Archived Projects list.</p>
	</logic:notEmpty>
</logic:empty>

</yrcwww:contentbox>

<!-- ARCHIVED PROJECTS -- completed work, kept out of the main list above -->
<logic:notEmpty name="archivedProjects" scope="request">

<div style="margin: 20px;"></div>

<yrcwww:contentbox title="Archived Projects" innerBox="true">

<div style="margin-bottom: 8px;">
 <a href="#" id="toggle_archived" style="font-weight: bold;">show</a>
 <span style="font-size: 8pt; color: #666;">
  &nbsp;(<bean:size id="archivedCount" name="archivedProjects"/><bean:write name="archivedCount"/> archived)
 </span>
</div>

<div id="archived_projects_section" style="display: none;">
<form action="/pr/archiveProjects.do" method="post">
<input type="hidden" name="archived" value="false"/>

 <TABLE BORDER="0" WIDTH="100%" id="archived_projects_table" class="striped">

 <thead>
  <TR>
   <TH>&nbsp;</TH>
   <TH>&nbsp;</TH>
   <TH><U>ID</U></TH>
   <TH><U>Title</U></TH>
   <TH><U>Type</U></TH>
   <TH><U>Submit Date</U></TH>
   <TH><U>Collaboration<br>Status</U></TH>
   <TH>&nbsp;</TH>
  </TR>
</thead>

<tbody>
<logic:iterate id="project" name="archivedProjects" scope="request">

  <TR>
  <TD valign="top" align="center">
   <input type="checkbox" class="projectCheck" name="projectIds" value="<bean:write name="project" property="ID"/>"/>
  </TD>
  <TD valign="top">
   <NOBR>
    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
   </NOBR>
  </TD>
  <TD valign="top"><bean:write name="project" property="ID"/></TD>
  <TD valign="top"><bean:write name="project" property="title"/></TD>
  <TD valign="top">
  	<logic:equal name="project" property="shortType" value="C">
  		UWPR Supported
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Billed
  	</logic:equal>
  </TD>
  <TD valign="top"><bean:write name="project" property="submitDate"/></TD>
  <TD valign="top">
  	<logic:equal name="project" property="shortType" value="C">
  		<bean:write name="project" property="collaborationStatus"/>
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Active
  	</logic:equal>
  </TD>
  <TD>&nbsp;</TD>
  </TR>
</logic:iterate>
</tbody>
 </TABLE>

 <div style="margin-top: 8px;">
  <input type="submit" id="unarchive_selected_button" value="Unarchive Selected" disabled="disabled"/>
  &nbsp;&nbsp;
  <span style="font-size: 8pt;">
   <a href="#" id="select_all_archived">select all</a> /
   <a href="#" id="select_none_archived">none</a>
  </span>
 </div>

</form>
</div>

</yrcwww:contentbox>
</logic:notEmpty>

<div style="margin: 20px;"></div>

<!-- SHOW ANY RECENT SUBMISSIONS TO THIS USER'S GROUP --> 
<yrcwww:member group="any">
	<yrcwww:contentbox title="Recent Submissions" innerBox="true">
	 	<logic:notEmpty name="newProjects" scope="request">
		 <p>Below are projects submitted by researchers to your group(s) within the last month.
	 
		 <p><table border="0" width="100%" class="striped">
		  <thead>
		  <tr>
		   <th>&nbsp;</th>
		   <th><u>ID</u></th>
		   <th><u>Lab Director</u></th>
		   <th><u>Title</u></th>
		   <th><u>Submit Date</u></th>
		  </tr>
		  </thead>
	 
	 	<tbody>
		<logic:iterate id="project" name="newProjects" scope="request">
		 <TR>
		  <TD valign="top">
		   <NOBR>
		    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
		   </NOBR>
		  </TD>
		  <TD valign="top"><bean:write name="project" property="ID"/></TD>
		  <TD valign="top"><bean:write name="project" property="PI.lastName"/></TD>
		  <TD valign="top"><bean:write name="project" property="title"/></TD>
		  <TD valign="top"><bean:write name="project" property="submitDate"/></TD>
		 </TR>
		</logic:iterate>
		</tbody>
		
		</table>
   		</logic:notEmpty>
   		<logic:empty name="newProjects" scope="request">
   		 <p>There have been no projects submitted to your group in the last month.
   		</logic:empty>
	</yrcwww:contentbox>
</yrcwww:member>


<!-- SHOW ALL PROJECTS, FOR WHICH THIS USER IS LISTED AS A REVIEWER --> 
<yrcwww:member group="Reviewers">

<logic:present name="reviewAssignments" scope="request">

<div style="margin: 20px;"></div>
<yrcwww:contentbox title="Projects Assigned to Review" innerBox="true">
 <TABLE BORDER="0" WIDTH="100%">
 
 <logic:empty name="reviewAssignments">
 	<div align="center"> You do not have any projects to review!</div>
 </logic:empty>
 
 <logic:notEmpty name="reviewAssignments">
  <thead>
  <tr>
	<th>&nbsp;</th>
	<th><u>ID</u></th>
	<th><u>Lab Director</u></th>
	<th><u>Title</u></th>
	<th><u>Submit Date</u></th>
	<th><u>Collaboration<br>Status</u></th>
 </tr>
 </thead>
 
 <tbody>	
	<logic:iterate id="project" name="reviewAssignments" scope="request">
 	<TR>
	<TD valign="top">
		<NOBR>
	    <html:link href="/pr/viewProject.do" paramId="ID" paramName="project" paramProperty="ID">View</html:link>
	   </NOBR>
	</TD>
  	<TD valign="top"><bean:write name="project" property="ID"/></TD>
  	<TD valign="top"><bean:write name="project" property="PI.lastName"/></TD>
  	<TD valign="top"><bean:write name="project" property="title"/></TD>
  	<TD valign="top"><bean:write name="project" property="submitDate"/></TD>
  	<logic:equal name="project" property="shortType" value="C">
  		<TD valign="top"><bean:write name="project" property="collaborationStatus" /></TD>
  	</logic:equal>
  	<logic:equal name="project" property="shortType" value="B">
  		Active
  	</logic:equal>
  	
	</TR>
</logic:iterate>
 </logic:notEmpty>
 </tbody>
 </TABLE>
</yrcwww:contentbox> 
</logic:present>  

</yrcwww:member>


</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>