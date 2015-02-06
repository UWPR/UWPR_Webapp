<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="editCollaborationForm" scope="request">
 <logic:forward name="extendCollaboration"/>
</logic:notPresent>

<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>
<logic:present name="saved" scope="request">
 <P align="center"><B>Your collaboration request was successfully received.</B>
</logic:present>

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<SCRIPT TYPE="text/javascript">
	function popup(mylink) {
		if (!window.focus)return true;
		var href;
		if (typeof(mylink) == 'string')
   			href=mylink;
		else
   			href=mylink.href;
		window.open(href, 'UWPR_example', 'width=500,height=400,scrollbars=yes');
		return false;
	}

    <bean:size name="editCollaborationForm" property="researcherList" id="researchers_size"/>
    var lastResearcherIndex = <bean:write name="researchers_size" />
    var researcherList = "<option value='0'>None</option>";
    <logic:iterate name="researchers" id="researcher">
        var id = <bean:write name="researcher" property="ID"/>;
        var name = '<bean:write name="researcher" property="listing" />';
        researcherList += "<option value='"+id+"'>"+name+"</option>";
    </logic:iterate>

    function removeResearcher(rowIdx)
    {
        //alert("removing researcher at index "+rowIdx);
        $("#researcherRow_"+rowIdx).hide();
        $("#researcherRow_"+rowIdx+" select").val(0);
    }

    function confirmRemoveResearcher(rowIdx) {
        if(confirm("Are you sure you want to remove this researcher from the project?"))
        {
            removeResearcher(rowIdx);
        }
    }

    function addResearcher()
    {

        // alert("last researcher: "+lastResearcherIndex);
        var newRow = "<tr id='researcherRow_"+lastResearcherIndex+"'>";
        newRow += "<td width='25%' valign='top'>Researcher: </td>";
        newRow += "<td width='25%' valign='top'>";
        newRow += "<select name='researcher["+lastResearcherIndex+"].ID'>";
        newRow += researcherList;
        newRow += "</select>";
        newRow += " <a href='javascript:confirmRemoveResearcher("+lastResearcherIndex+")' style='color:red; font-size:8pt;'>[Remove]</a>";
        newRow += "</td>";
        newRow +="</tr>";
        if(lastResearcherIndex == 0) {
            $("#piRow").after(newRow);
        }
        else {
            $("#researcherRow_"+(lastResearcherIndex-1)).after(newRow);
        }
        lastResearcherIndex++;
    }
</SCRIPT>

<yrcwww:contentbox title="Extend Collaboration">

<P>To extend an existing collaboration with the UWPR, please fill out / update the form below and enter the number of anticipated runs needed for the extension.
The appropriate members of the UWPR will automatically be notified of your request, and should follow-up with you shortly.

<P><B>NOTE:</B>  If an individual you are listing as a researcher is not currently in the database, you must add them to the database first.
Go <html:link href="/pr/newResearcher.do">here</html:link> to add a new researcher to our database.

 <CENTER>

  <P><html:form action="saveCollaborationExtension" method="post">
  	
  <TABLE CELLPADDING="no" CELLSPACING="0">

<!--
   <yrcwww:member group="any">
   <TR>
    <TD WIDTH="100%" VALIGN="top" COLSPAN="2"><html:checkbox property="isTech"/> Check here if this <b>is a Technology Development project</b>.<BR><BR></TD>
   </TR>
   </yrcwww:member>
-->

  	<tr>
  	<TD WIDTH="25%" VALIGN="top"><B>Parent Project ID:</B></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<html:hidden name="editCollaborationForm" property="parentProjectID"/>
    	<html:link action="viewProject.do" paramId="ID" 
    	paramName="editCollaborationForm" paramProperty="parentProjectID">
    	<B><bean:write name="editCollaborationForm" property="parentProjectID"/></B>
    	</html:link>
    </TD>
  	</tr>
  	
   <TR id="piRow">
    <TD WIDTH="25%" VALIGN="top">Lab Director:</TD>
    <TD WIDTH="75%" VALIGN="top">
    
    	<html:select property="PI">
    		<html:option value="0">None</html:option>
			<html:options collection="labDirectors" property="ID" labelProperty="listing"/>
    	</html:select>
    	<div style="font-size:8pt;margin-bottom:10px;">
    	If your Lab Director is not listed in the menu above <br>click on 
    	<html:link action="addLabDirector"><b>Add Lab Director</b></html:link>.
    	</div>
    </TD>
   </TR>


    <logic:iterate id="researcher" property="researcherList" name="editCollaborationForm" indexId="cnt">
    <tr id="researcherRow_<%=cnt%>" >
        <TD WIDTH="25%" VALIGN="top">Researcher:</TD>
        <td WIDTH="25%" VALIGN="top">
            <html:select name="researcher" property="ID" indexed="true">
                <html:option value="0">None</html:option>
            <html:options collection="researchers" property="ID" labelProperty="listing"/>
            </html:select>
            <a href="javascript:confirmRemoveResearcher('<%=(cnt)%>')" style="color:red; font-size:8pt;">[Remove]</a>
        </td>
    </tr>
    </logic:iterate>

    <tr>
        <td colspan="2" align="center"><a href="javascript:addResearcher()">Add Researcher</a></td>
    </tr>


	<tr><td colspan="2"><hr width="100%"></td></tr>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Collaborating with:</TD>
    <TD WIDTH="75%" VALIGN="top">	
     <html:multibox property="groups" value="Bruce"/>Bruce
     <html:multibox property="groups" value="Goodlett"/>Goodlett
     <html:multibox property="groups" value="Heinecke"/>Heinecke
     <html:multibox property="groups" value="Hoofnagle"/>Hoofnagle
     <br/>
     <html:multibox property="groups" value="MacCoss"/>MacCoss
     <html:multibox property="groups" value="Ong"/>Ong
     <html:multibox property="groups" value="Villen"/>Villen
     <html:multibox property="groups" value="Wolf-Yadlin"/>Wolf-Yadlin
     <br/>
     <html:multibox property="groups" value="Informatics"/>Informatics
     <html:multibox property="groups" value="von_Haller"/>von Haller
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Project Title:</TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="title" size="60" maxlength="80"/></TD>
   </TR>
   
   <TR>
    <TD WIDTH="25%" VALIGN="top">Scientific Question:<br><span style="font-size:8pt;color:red;">Using 1 to 2 sentences, succinctly describe the scientific question that will be addressed by this proposal.</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="scientificQuestion" rows="5" cols="50"/></TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Abstract:<br><span style="font-size:8pt;color:red;">Short summary of the project (limit 500 words).</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="abstract" rows="10" cols="50"/></TD>
   </TR>

	<TR>
	
	<!-- PROJECT EXTENSION REASONS -->
	<TR>
    	<TD WIDTH="25%" VALIGN="top">Project Extension Reason(s):</TD>
    	<TD WIDTH="75%" VALIGN="top"><html:textarea property="extensionReasons" rows="10" cols="50"/></TD>
   </TR>
   
	<TR>
    	<TD WIDTH="25%" VALIGN="top">Comments:</TD>
    	<TD WIDTH="75%" VALIGN="top"><html:textarea property="comments" rows="5" cols="50"/></TD>
   	</TR>
<!--
   <TR>
    <TD WIDTH="25%" VALIGN="top">Public Abstract:<br><font style="font-size:8pt;color:red;">To appear in NIH CRISP database.</font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="publicAbstract" rows="7" cols="50"/></TD>
   </TR>
-->

	<tr><td colspan="2"><hr width="100%"></td></tr>
	<tr>
		<td colspan="2" style="text-align:center;"><span style="font-size:12pt;">Additional information (for anticipated workload scheduling):</span><br><br></td>
	</tr>

	<tr>
		<td width="25%" valign="top">Approx. # of requested runs:</td>
		<td width="75%" valign="top">
			<table>
				<tr>
					<td><html:text property="ltqRunsRequested" size="3" maxlength="6" /></td>
					<td>LTQ</td>
					
					<td><html:text property="ltq_ftRunsRequested" size="3" maxlength="6" /></td>
					<td>LTQ-FT</td>
					
				</tr>
				<tr>
					<td><html:text property="ltq_etdRunsRequested" size="3" maxlength="6" /></td>
					<td>LTQ-ETD</td>

					<td><html:text property="ltq_orbitrapRunsRequested" size="3" maxlength="6" /></td>
					<td>LTQ-Orbitrap</td>
				</tr>
				<tr>
					<td><html:text property="tsq_accessRunsRequested" size="3" maxlength="6" /></td>
					<td>TSQ-Access</td>
					
					<td><html:text property="tsq_vantageRunsRequested" size="3" maxlength="6" /></td>
					<td>TSQ-Vantage</td>
				</tr>
			</table>
		</td>
	</tr>

	<tr>
		<td colspan="2" align="center"><div style="margin-bottom: 10px;"><a href="javascript:showAvailability()">View MS Instrument Availability</a></div></td>
	</tr>
	
	<tr>
		<td width="25%" valign="top">Instrument time justification:</td>
		<td width="75%" valign="top">
			<html:textarea property="instrumentTimeExpl" rows="5" cols="50"/>
			<br/>
			<div style="color:red; font-weight:bold; font-size:8pt;">
			Please provide an explanation for the number of runs and instrument <br/>
			type requested. Click <a href="pages/internal/project/instrTimeExamples.jsp" onclick="return popup(this);">here</a> 
			for some examples. <br/>
			A new project is more likely to be reviewed in a timely manner <br>if the number of requested runs is 50 or less.
			</div>
			<div style="font-size:8pt; margin-bottom:10px;">
			If you are NOT requesting instrument time please enter "None" in the text box above.
			</div>
		</td>
	
	</tr>
	
	<tr>
		<td width="25%" valign="top">Fragmentation type, check all that apply:</td>
		<td width="75%" valign="top">
			<table>
				<tr>
					<td><html:multibox property="fragmentationTypes" value="CID" />CID</td>
					<td><html:multibox property="fragmentationTypes" value="ETD" />ETD</td>
					<td><html:multibox property="fragmentationTypes" value="HCD" />HCD</td>
				</tr>
				<tr>
					<td><html:multibox property="fragmentationTypes" value="PQD" />PQD</td>
					<td><html:multibox property="fragmentationTypes" value="ECD" />ECD</td>
					<td><html:multibox property="fragmentationTypes" value="IRMPD" />IRMPD</td>
				</tr>
			</table>
		</td>
	</tr>


	<tr>
		<td width="25%" valign="top" style="padding-top:10px">Mass Spec. analysis by UWPR personnel?
		<br><span style="font-size:8pt;color:red;">
		Select "No" only if you have trained mass spectrometrists who will perform the analysis at our facility.
		</span>
		</td>
		<td width="75%" valign="top" style="padding-top:10px"><html:radio property="massSpecExpertiseRequested" value="true" />Yes
					     <html:radio property="massSpecExpertiseRequested" value="false" />No
		</td>
	</tr>
	
	<tr>
		<td width="25%" valign="top" style="padding-top:10px">Database search performed at UWPR?</td>
		<td width="75%" valign="top" style="padding-top:10px"><html:radio property="databaseSearchRequested" value="true" />Yes
					     <html:radio property="databaseSearchRequested" value="false" />No
		</td>
	</tr>

	<tr><td colspan="2"><hr width="100%"></td></tr>

  </TABLE>

 <P><NOBR>
 <html:submit value="Request/Save Collaboration"/>
 <input type="button" class="button" onclick="onCancel(<bean:write name="editCollaborationForm" property="parentProjectID"/>);" value="Cancel"/>
 
 </NOBR>
 
  </html:form>
  
 </CENTER>
</yrcwww:contentbox>

<script type="text/javascript">
	function showAvailability() {
		var url = "/pr/viewAllInstrumentCalendar.do?popup=true";
		window.open(url, "Instrument_Calendar", "width=900,height=900,status=no,resizable=yes,scrollbars=yes");
	}
	function onCancel(projectID) {
		document.location = "/pr/viewProject.do?ID="+projectID;
	}
</script>

<%@ include file="/includes/footer.jsp" %>