
<%@page import="org.yeastrc.www.project.EditBilledProjectForm"%><%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>

<yrcwww:notauthenticated>
 <logic:forward name="authenticate" />
</yrcwww:notauthenticated>

<logic:notPresent name="editBilledProjectForm">
	<logic:forward name="editProject" />
</logic:notPresent>
 

<%@ include file="/includes/header.jsp" %>

<%@ include file="/includes/errors.jsp" %>

<script type='text/javascript' src='/pr/js/jquery-1.5.min.js'></script>
<SCRIPT LANGUAGE="javascript">
	function openAXISWindow(type) {
	 var AXISI_WIN, AXISII_WIN;
	 var doc = "/pr/AXIS.do?ID=<bean:write name="editBilledProjectForm" property="ID"/>&type=" + type;

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
	
	$(function() {
		updateMassSpecExpertiseRadio();
	});
	
	function updateMassSpecExpertiseRadio() {
	
		// If the Affiliation selector is disabled we will not be updating the 
		// radio buttons.
		if($("#affiliation").attr('disabled')) {
			return;
		}
		
		var selected = $("#affiliation option:selected").text();		
		// alert("affiliation selected is " + selected);
		
		var $radios = $('input:radio[name=massSpecExpertiseRequested]');
		if(selected != "None" && selected != "University of Washington") {
		
	        $radios.filter('[value=true]').attr('checked', true);
	        $radios.attr("disabled", true);
	        $("#massSpecExpertiseRequested_hidden").attr("disabled", false); 
	        $("#massSpecExpertiseRequested_hidden").val(true);
		}
		else {
			//$radios.filter('[value=false]').attr('checked', true);
			$radios.attr("disabled", false);
	        $("#massSpecExpertiseRequested_hidden").attr("disabled", true); 
	        $("#massSpecExpertiseRequested_hidden").val(false);
		}
	}

    <bean:size name="editBilledProjectForm" property="researcherList" id="researchers_size"/>
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


<yrcwww:contentbox title="Edit Project Details">


<div style="margin:20px 0 20px 0;font-weight:bold;">
Fields followed by * are required.
</div>

 <CENTER>
  <html:form action="saveBilledProject" method="post" styleId="form1">
  <TABLE CELLPADDING="no" CELLSPACING="0">
  
  <tr>
  	<TD WIDTH="25%" VALIGN="top"><B>Project ID:</B></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<html:hidden name="editBilledProjectForm" property="ID"/>
    	<B><bean:write name="editBilledProjectForm" property="ID"/></B>
    </TD>
  </tr>
  
   <TR>
    <TD WIDTH="25%" VALIGN="top"><B>Submit Date:</B></TD>
    <TD WIDTH="75%" VALIGN="top">
    	<html:hidden name="editBilledProjectForm" property="submitDate" />
    	<B><bean:write name="editBilledProjectForm" property="submitDate"/></B>
    </TD>
   </TR>

	<tr>
		<td width="25%" valign="top"><b>Affiliation*</b></td>
		<td width="75%" valign="top">
			<logic:equal name="editBilledProjectForm" property="editable" value="true">
				<html:select property="affiliationName" styleId="affiliation" onchange="updateMassSpecExpertiseRadio()">
					<html:option value="0">None</html:option>
					<html:options collection="affiliationTypes" property="name" labelProperty="longName"/>
				</html:select>
			</logic:equal>
			<logic:equal name="editBilledProjectForm" property="editable" value="false">
                <html:hidden name="editBilledProjectForm"  property="affiliationName"/>
				<html:select property="affiliationName" styleId="affiliation" disabled="true">
					<html:option value="0">None</html:option>
					<html:options collection="affiliationTypes" property="name" labelProperty="longName"/>
				</html:select>
			</logic:equal>
		</td>
	
	</tr>
	
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

  <logic:iterate id="researcher" property="researcherList" name="editBilledProjectForm" indexId="cnt">
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
    <TD WIDTH="25%" VALIGN="top">Title*</TD>
    <TD WIDTH="75%" VALIGN="top"><html:text property="title" size="60" maxlength="80"/>
    </TD>
    
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Scientific Question<br><span style="font-size:8pt;color:red;">Using 1 to 2 sentences, succinctly describe the scientific question that will be addressed by this proposal.</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="scientificQuestion" rows="5" cols="50"/>
    </TD>
   </TR>

   <TR>
    <TD WIDTH="25%" VALIGN="top">Abstract*<br><span style="font-size:8pt;color:red;">Short summary of the project (limit 500 words).</span></TD>
    <TD WIDTH="75%" VALIGN="top"><html:textarea property="abstract" rows="10" cols="50"/>
    	
    </TD>
   </TR>

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
   
	<tr>
		<td width="25%" valign="top" style="padding-top:10px">Mass Spec. analysis by UWPR personnel?
		</td>
		<td width="75%" valign="top" style="padding-top:10px">
		
			<logic:equal name="editBilledProjectForm" property="editable" value="true">
				<html:radio property="massSpecExpertiseRequested" value="true" />Yes
				<html:radio property="massSpecExpertiseRequested" value="false" />No
				<html:hidden property="massSpecExpertiseRequested" styleId="massSpecExpertiseRequested_hidden"/>
			</logic:equal>
			<logic:equal name="editBilledProjectForm" property="editable" value="false">
				<html:radio property="massSpecExpertiseRequested" value="true" disabled="true"/>Yes
				<html:radio property="massSpecExpertiseRequested" value="false" disabled="true"/>No
			</logic:equal>
			<span style="color:red; font-weight:bold; font-size: 8pt;">(Additional labor rates will apply if "Yes" is selected)</span>
		</td>
	</tr>
	
	<tr>
		<td width="25%" valign="top" style="padding-top:10px">Database search performed at UWPR?</td>
		<td width="75%" valign="top" style="padding-top:10px">
			<html:radio property="databaseSearchRequested" value="true" />Yes
			<html:radio property="databaseSearchRequested" value="false" />No
		</td>
	</tr>
   	
	<tr><td colspan="2"><hr width="75%"></td></tr>
	

  </TABLE>

 <P><NOBR>
	<html:submit>Save</html:submit>
	<input type="button" onclick="document.location='/pr/viewProject.do?ID=<bean:write name='editBilledProjectForm' property='ID'/>';" value="Cancel" />
 </NOBR>
 </P>
 <br>

</html:form>


 </CENTER>

</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>