<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="editCollaborationForm" scope="request">
 <logic:forward name="newCollaboration"/>
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
	
<yrcwww:contentbox title="Submit Abstract for Collaboration">

<P>To request a new collaboration with the UWPR, please fill out the form below.
The appropriate members of the UWPR will automatically be notified of your request, and should follow-up with you shortly.

<div style="color:red">
Please note that abstracts submitted for UWPR supported instrument time will undergo critical review.
The review process can take upward of one month.   
A detailed justification will be necessary for approval of instrument time. 
All UWPR supported projects require an active collaboration with one of the UWPR PIs to ensure optimal experimental design.
Please contact us for more information.
<br/>
<b>NOTE: </b> There is a limit of 50 RAW files or 5 days for each subsidized project and five projects per PI per year.
</div>

<P>If an individual you are listing as a researcher is not currently in the database, you must add them to the database first.
Go <html:link href="/pr/newResearcher.do">here</html:link> to add a new researcher to our database.

<div style="margin:20px 0 20px 0;font-weight:bold;">
Fields followed by * are required.
</div>

 <CENTER>

  <P><html:form action="saveNewCollaboration" method="post">
  	
  <TABLE CELLPADDING="no" CELLSPACING="0">


   <tr><td colspan="2"><hr width="100%"></td></tr>
   <TR id="piRow">
    <TD WIDTH="25%" VALIGN="top">Lab Director*</TD>
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
    <TD WIDTH="25%" VALIGN="top">Collaborating with*</TD>
    <TD WIDTH="75%" VALIGN="top" style="padding-bottom: 10px;">	
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
     <br>
     <span style="font-size:8pt; font-weight:bold;color:red;">Abstract should have been discussed with and approved by the selected collaborator(s).</span>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Project Title*</TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="title" size="60" maxlength="80"/></TD>
   </TR>
   
   <TR>
    <TD WIDTH="25%" VALIGN="top">Scientific Question<br><span style="font-size:8pt;color:red;">Using 1 to 2 sentences, succinctly describe the scientific question that will be addressed by this proposal.</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="scientificQuestion" rows="5" cols="50"/></TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Abstract*<br><span style="font-size:8pt;color:red;">Short summary of the project (limit 500 words).</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="abstract" rows="10" cols="50"/></TD>
   </TR>

	<TR>
    	<TD WIDTH="25%" VALIGN="top">Comments</TD>
    	<TD WIDTH="75%" VALIGN="top"><html:textarea property="comments" rows="5" cols="50"/></TD>
   	</TR>
<!--
   <TR>
    <TD WIDTH="25%" VALIGN="top">Public Abstract:<br><font style="font-size:8pt;color:red;">To appear in NIH CRISP database.</font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="publicAbstract" rows="7" cols="50"/></TD>
   </TR>
-->

	<tr><td colspan="2"><hr width="75%"></td></tr>
	<tr>
		<td colspan="2" style="text-align:center;"><span style="font-size:12pt;">Additional information (for anticipated workload scheduling):</span><br><br></td>
	</tr>

	<tr>
		<td width="25%" valign="top">Approx. # of requested runs*</td>
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
		<td width="25%" valign="top">Instrument time justification*</td>
		<td width="75%" valign="top">
			<html:textarea property="instrumentTimeExpl" rows="5" cols="50"/>
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
		<td width="25%" valign="top">Fragmentation type, check all that apply</td>
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



	<!--
   <TR>
    <TD WIDTH="25%" VALIGN="top">Progress:</TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="progress" rows="7" cols="50"/></TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Publications:<br><font style="font-size:8pt;color:red;">ONLY publications resulting<br>from this collaboration</font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="publications" rows="5" cols="50"/></TD>
   </TR>
   
	-->


<!--
	<tr><td colspan="2"><hr width="75%"></td></tr>

	<tr>
		<td colspan="2" align="left"><p><b>The following funding information is used by us when filing our annual report
										with NCRR and NIH, and is used to derive summary statistics only.</b><br><br>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Funding Sources:<br>
     <font style="font-size:8pt;color:red;">For <b>this</b> project only.<br><br></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<NOBR><html:multibox property="fundingTypes" value="FEDERAL"/>U.S. Federal</NOBR>
    	<NOBR><html:multibox property="fundingTypes" value="FOUNDATION"/>Foundation</NOBR>
    	<NOBR><html:multibox property="fundingTypes" value="INDUSTRY"/>Industry</NOBR>
    	<NOBR><html:multibox property="fundingTypes" value="PROFASSOC"/>Prof. Assoc.</NOBR>
    	<NOBR><html:multibox property="fundingTypes" value="LOCGOV"/>Local Gov.</NOBR>
    	<NOBR><html:multibox property="fundingTypes" value="OTHER"/>Other <font style="font-size:8pt;">(includes non-US gov't)</font></NOBR>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Federal Funding Sources:<br>
     <font style="font-size:8pt;color:red;">Only for <b>U.S.</b> Federal funding.<br><br></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<NOBR><html:multibox property="federalFundingTypes" value="NASA"/>NASA</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="NIH"/>NIH</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="NSF"/>NSF</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="DOE"/>DOE</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="DOD"/>DOD</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="NIST"/>NIST</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="DVA"/>DVA</NOBR>
    	<NOBR><html:multibox property="federalFundingTypes" value="OTHER"/>Other</NOBR>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Grant number:<br><font style="font-size:8pt;color:red;">(Only for federal funding)</font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="grantNumber" size="20" maxlength="255"/></TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Annual Funds:<br><font style="font-size:8pt;color:red;"></font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="grantAmount" size="10" maxlength="255"/></TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Funding source name:<br><font style="font-size:8pt;color:red;">(If NOT U.S. federal)</font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="foundationName" size="60" maxlength="80"/></TD>
   </TR>
-->

	<tr><td colspan="2"><hr width="75%"></td></tr>

<!--  
   <TR>
    <TD COLSPAN="2">
     <html:multibox property="sendEmail" value="false"/> Check here if you do <B>NOT</B> want an email sent to the groups in the
     UWPR with whom you are requesting a collaboration.  This should only be done if you are absolutely sure they are already
     aware of this collaboration.
    </TD>
   </TR>
-->
  </TABLE>

 <P><NOBR>
 <html:submit value="Request/Save Collaboration"/>
 </NOBR>
 
  </html:form>
  
 </CENTER>
</yrcwww:contentbox>

<script type="text/javascript">
	function showAvailability() {
		var url = "/pr/viewAllInstrumentCalendar.do?popup=true";
		window.open(url, "Instrument_Calendar", "width=900,height=900,status=no,resizable=yes,scrollbars=yes");
	}
</script>

<%@ include file="/includes/footer.jsp" %>