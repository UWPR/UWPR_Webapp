
<%@page import="org.yeastrc.www.project.EditCollaborationForm"%>
<%@page import="org.yeastrc.project.CollaborationStatus"%>
<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="editCollaborationForm">
	<logic:forward name="editProject" />
</logic:notPresent>
 

<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<SCRIPT LANGUAGE="javascript">
	function openAXISWindow(type) {
	 var AXISI_WIN, AXISII_WIN;
	 var doc = "/pr/AXIS.do?ID=<bean:write name="editCollaborationForm" property="ID"/>&type=" + type;

	 if(type == "I") {
		AXISI_WIN = window.open(doc, "AXISI_WIN",
									  "width=850,height=550,status=no,resizable=yes,scrollbars");
	 } else if(type == "II") {
		AXISI_WIN = window.open(doc, "AXISII_WIN",
									  "width=850,height=550,status=no,resizable=yes,scrollbars");
	 }
	}
	
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
	
	function showAvailability() {
		var url = "/pr/viewAllInstrumentCalendar.do?popup=true";
		window.open(url, "Instrument_Calendar", "width=900,height=900,status=no,resizable=yes,scrollbars=yes");
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


<yrcwww:contentbox title="Edit Collaboration Details">

<%
	boolean disabledFull = !((EditCollaborationForm)request.getAttribute("editCollaborationForm")).isFullEditable();
	boolean disabledPart = !((EditCollaborationForm)request.getAttribute("editCollaborationForm")).isPartEditable();
%>

<div style="margin:20px 0 20px 0;font-weight:bold;">
Fields followed by * are required.
</div>

 <CENTER>
  <html:form action="saveCollaboration" method="post" styleId="form1">
  <html:hidden name="editCollaborationForm" property="ID"/>
  <html:hidden name="editCollaborationForm" property="fullEditable" />
  <html:hidden name="editCollaborationForm" property="partEditable" />
  <TABLE CELLPADDING="no" CELLSPACING="0" >
  
  <tr>
  	<TD WIDTH="25%" VALIGN="top"><B>Project ID:</B></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<html:hidden name="editCollaborationForm" property="ID"/>
    	<B><bean:write name="editCollaborationForm" property="ID"/></B>
    </TD>
  </tr>
  
   <TR>
    <TD WIDTH="25%" VALIGN="top"><B>Submit Date:</B></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<html:hidden name="editCollaborationForm" property="submitDate" />
    	<B><bean:write name="editCollaborationForm" property="submitDate"/></B>
    </TD>
   </TR>

  <tr><td colspan="2"><hr width="100%"></td></tr>

   <TR id="piRow">
    <TD WIDTH="25%" VALIGN="top">Lab Director*</TD>
    <TD WIDTH="75%" VALIGN="top">
    
   		<html:select property="PI" disabled="<%=disabledFull %>">
   			<html:option value="0">None</html:option>
			<html:options collection="labDirectors" property="ID" labelProperty="listing"/>
   		</html:select>
   		<logic:equal name="editCollaborationForm" property="fullEditable" value="false">
   			<html:hidden property="PI" />
   		</logic:equal>
        <logic:equal name="editCollaborationForm" property="fullEditable" value="true">
            <div style="font-size:8pt;margin-bottom:10px;">
            If your Lab Director is not listed in the menu above <br>click on
            <html:link action="addLabDirector"><b>Add Lab Director</b></html:link>.
            </div>
         </logic:equal>
    </TD>
   </TR>

  <logic:iterate id="researcher" property="researcherList" name="editCollaborationForm" indexId="cnt">
  <tr id="researcherRow_<%=cnt%>" >
      <TD WIDTH="25%" VALIGN="top">Researcher:</TD>
      <td WIDTH="25%" VALIGN="top">
          <html:select name="researcher" property="ID" indexed="true" disabled="<%=disabledFull%>">
              <html:option value="0">None</html:option>
          <html:options collection="researchers" property="ID" labelProperty="listing"/>
          </html:select>
          <logic:equal name="editCollaborationForm" property="fullEditable" value="false">
              <html:hidden name="researcher" property="ID" indexed="true" />
          </logic:equal>
          <logic:equal name="editCollaborationForm" property="fullEditable" value="true">
              <a href="javascript:confirmRemoveResearcher('<%=(cnt)%>')" style="color:red; font-size:8pt;">[Remove]</a>
          </logic:equal>
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
     <logic:equal name="editCollaborationForm" property="fullEditable" value="true">
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
   	</logic:equal>
   	<logic:equal name="editCollaborationForm" property="fullEditable" value="false">
   		<logic:iterate name="editCollaborationForm" property="groups" id="group"><bean:write name="group" />&nbsp;</logic:iterate>
   		<div style="visibility: hidden;">
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
     	</div>
   	</logic:equal>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Title*</TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="title" size="60" maxlength="80"  disabled="<%=disabledFull%>"/>
    <logic:equal name="editCollaborationForm" property="fullEditable" value="false">
   			<html:hidden property="title" />
   	</logic:equal>
    </TD>
    
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Scientific Question<br><span style="font-size:8pt;color:red;">Using 1 to 2 sentences, succinctly describe the scientific question that will be addressed by this proposal.</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="scientificQuestion" rows="5" cols="50"  disabled="<%=disabledFull%>"/>
    <logic:equal name="editCollaborationForm" property="fullEditable" value="false">
   		<html:hidden property="scientificQuestion" />
   	</logic:equal>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Abstract*<br><span style="font-size:8pt;color:red;">Short summary of the project (limit 500 words).</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="abstract" rows="10" cols="50"  disabled="<%=disabledFull%>"/>
    <logic:equal name="editCollaborationForm" property="fullEditable" value="false">
   		<html:hidden property="abstract" />
   	</logic:equal>	
    </TD>
   </TR>

	<logic:present name="editCollaborationForm" property="extensionReasons">
  	<TR>
  		<TD valign="top" width="25%">Project Extension Reason(s)*</TD>
  		<TD WIDTH="75%" VALIGN="top"><html:textarea property="extensionReasons" rows="10" cols="50"  disabled="<%=disabledFull%>"/>
		<logic:equal name="editCollaborationForm" property="fullEditable" value="false">
				<html:hidden property="extensionReasons" />
		</logic:equal>
  	</TR>
  </logic:present>
	
   <TR>
    <TD WIDTH="25%" VALIGN="top">Progress<br><span style="font-size:8pt;color:red;">Highlight scientific progress enabled by the UWPR collaboration.</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="progress" rows="7" cols="50"/>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Publications<br><font style="font-size:8pt;color:red;">ONLY publications resulting<br>from this collaboration</font></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="publications" rows="5" cols="50"/></TD>
   </TR>
   	
	<TR>
    <TD WIDTH="25%" VALIGN="top">Comments</TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="comments" rows="5" cols="50"/></TD>
   </TR>
   
	<tr><td colspan="2"><hr width="100%"></td></tr>
	<tr>
		<td colspan="2" align="center"><span style="font-size:12pt;">Additional information (for anticipated workload scheduling):</span><br><br></td>
	</tr>


	<tr>
		<td width="25%" valign="top">Approx. # of requested runs*</td>
		<td width="75%" valign="top">
			<table>
				<tr>
					<td><html:text property="ltqRunsRequested" size="3" maxlength="6"  disabled="<%=disabledPart%>"/>
					<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   						<html:hidden property="ltqRunsRequested" />
   					</logic:equal>	
					</td>
					<td>LTQ</td>
					
					<td><html:text property="ltq_ftRunsRequested" size="3" maxlength="6"  disabled="<%=disabledPart%>"/>
					<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   						<html:hidden property="ltq_ftRunsRequested" />
   					</logic:equal>	
					</td>
					<td>LTQ-FT</td>
					
				</tr>
				<tr>
					<td><html:text property="ltq_etdRunsRequested" size="3" maxlength="6"  disabled="<%=disabledPart%>"/>
					<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   						<html:hidden property="ltq_etdRunsRequested" />
   					</logic:equal>	
					</td>
					<td>LTQ-ETD</td>

					<td><html:text property="ltq_orbitrapRunsRequested" size="3" maxlength="6"  disabled="<%=disabledPart%>"/>
					<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   						<html:hidden property="ltq_orbitrapRunsRequested" />
   					</logic:equal>	
					</td>
					<td>LTQ-Orbitrap</td>
					
				</tr>
				<tr>
					<td><html:text property="tsq_accessRunsRequested" size="3" maxlength="6"  disabled="<%=disabledPart%>"/>
					<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   						<html:hidden property="tsq_accessRunsRequested" />
   					</logic:equal>	
					</td>
					<td>TSQ-Access</td>	
					
					<td><html:text property="tsq_vantageRunsRequested" size="3" maxlength="6"  disabled="<%=disabledPart%>"/>
					<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   						<html:hidden property="tsq_vantageRunsRequested" />
   					</logic:equal>	
					</td>
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
			<html:textarea property="instrumentTimeExpl" rows="5" cols="50" disabled="<%=disabledPart%>"/>
			<logic:equal name="editCollaborationForm" property="partEditable" value="false">
				<html:hidden property="instrumentTimeExpl" />
			</logic:equal>
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
			<html:hidden property="notPending"/>
		</td>
	
	</tr>
	
	<tr>
		<logic:equal name="editCollaborationForm" property="partEditable" value="true">
		<td width="25%" valign="top">Fragmentation type, check all that apply</td>
		</logic:equal>
		
		<logic:equal name="editCollaborationForm" property="partEditable" value="false">
		<td width="25%" valign="top">Fragmentation type(s):</td>
		</logic:equal>
		
		<td width="75%" valign="top">
			<table>
				<logic:equal name="editCollaborationForm" property="partEditable" value="true">
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
				</logic:equal>	
				
				<logic:equal name="editCollaborationForm" property="partEditable" value="false">
				<tr>
					<td>
						<logic:iterate name="editCollaborationForm" property="fragmentationTypes" id="ft"> <bean:write name="ft" /> &nbsp;</logic:iterate>
						<div style="visibility: hidden;">
							<html:multibox property="fragmentationTypes" value="CID" />
							<html:multibox property="fragmentationTypes" value="ETD" />
							<html:multibox property="fragmentationTypes" value="HCD" />
							<html:multibox property="fragmentationTypes" value="PQD" />
							<html:multibox property="fragmentationTypes" value="ECD" />
							<html:multibox property="fragmentationTypes" value="IRMPD" />
						</div>
					</td>
				</tr>
				</logic:equal>	
				
				
			</table>
		</td>
	</tr>
	
	<tr>
		<td width="25%" valign="top" style="padding-top:10px">Mass Spec. analysis by UWPR personnel?
		<br><span style="font-size:8pt;color:red;">
		Select "No" only if you have trained mass spectrometrists who will perform the analysis at our facility.
		</span>
		</td>
		<td width="75%" valign="top" style="padding-top:10px">
			<html:radio property="massSpecExpertiseRequested" value="true" disabled="<%=disabledPart%>"/>Yes
			<html:radio property="massSpecExpertiseRequested" value="false"  disabled="<%=disabledPart%>"/>No
			<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   				<html:hidden property="massSpecExpertiseRequested" />
   			</logic:equal>	
		</td>
	</tr>
	
	<tr>
		<td width="25%" valign="top" style="padding-top:10px">Database search performed at UWPR?</td>
		<td width="75%" valign="top" style="padding-top:10px">
			<html:radio property="databaseSearchRequested" value="true" disabled="<%=disabledPart%>"/>Yes
			<html:radio property="databaseSearchRequested" value="false"  disabled="<%=disabledPart%>"/>No
			<logic:equal name="editCollaborationForm" property="partEditable" value="false">
   				<html:hidden property="databaseSearchRequested" />
   			</logic:equal>
		</td>
	</tr>
   	
	<tr><td colspan="2"><hr width="100%"></td></tr>
	

  </TABLE>

 <P><NOBR>
 <html:submit>Save</html:submit>
 <input type="button" onclick="document.location='/pr/viewProject.do?ID=<bean:write name='editCollaborationForm' property='ID'/>';" value="Cancel" />
 </NOBR>
 <br>
 <!-- If the project's status is REVISE show a checkbox for sending email to reviewers. -->
 <logic:present name="statusIsRevise">
 	<html:checkbox property="emailReviewer">Notify Reviewers</html:checkbox>
 </logic:present>
 </P>
  </html:form>


 </CENTER>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>