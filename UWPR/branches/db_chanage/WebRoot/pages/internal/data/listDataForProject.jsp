


<yrcwww:contentbox title="External Links to Data">

	<logic:notEmpty name="externalLinkData" scope="request">
	
		<center>
		<table width="80%" class="striped">
			
			<tr>
			
				<yrcwww:member group="administrators"><th width="10%" valign="bottom" style="font-size:10pt;">&nbsp;</th></yrcwww:member>
				<th width="25%" valign="bottom" style="font-size:10pt;"><span style="text-decoration:underline;">Data Link</span><br><span style="font-size:8pt;">(Opens New Window)</span></th>
				<th width="25%" valign="bottom" style="font-size:10pt;"><span style="text-decoration:underline;">Upload Date</span></th>
				<th valign="bottom" style="font-size:10pt;"><span style="text-decoration:underline;">Comments<span></th>
			
			</tr>
			
			<logic:iterate name="externalLinkData" scope="request" id="link">
			
				<tr>
				
					<yrcwww:member group="administrators">
						<td align="center" valign="top" width="10%">
							<div style="margin-bottom:10px;"><a id="editLink" onclick="showExternalDataEditForm(<bean:write name="link" property="id"/>); return false" href="">Edit</a></div>
							<div><a href="/pr/deleteExternalDataLink.do?id=<bean:write name="link" property="id" />"><span style="font-size:8pt;color:#FF0000;">[Delete]</span></a></div>
						</td>
					</yrcwww:member>
					
					<td align="center" valign="top" width="25%"><a target="data_window" href="<bean:write name="link" property="uri" />">View Data</a></td>
					
					<td align="center"valign="top" width="25%"><bean:write name="link" property="lastChange" /></td>

					<td align="left"valign="top"><bean:write name="link" property="comments" /></td>
				
				</tr>
			
				<!-- BEGIN Form for editing external data information -->
				<yrcwww:member group="administrators">
				<tr>
					<td colspan="5">
						<div id="<bean:write name="link" property="id" />" style="display: none; color: #000000;">
							<yrcwww:contentbox>
								<html:form action="updateExternalDataLink" method="post">
								<input type="hidden" name="projectID" value="<bean:write name="project" property="ID" />">
								<input type="hidden" name="dataID" value="<bean:write name="link" property="id" />">
								<center>
									<div style="color: #000000; margin-bottom: 20px;">Edit external data link</div>
									<table>
										<tr>
											<td>Data URI:</td>
											<td><input type="text" name="uri" value="<bean:write name="link" property="uri" />" size="50" maxlength="2000"/></td>
										</tr>
					
										<tr>
											<td valign="top">Comments:</td>
											<td valign="top"><input type="textarea" name="comments" rows="10" cols="50" size="50" maxlength="2000" value="<bean:write name="link" property="comments" />" /></td>
										</tr>
									</table>
									<input type="button" value="Cancel" onClick="hideExternalDataEditForm(<bean:write name="link" property="id" />)">
									<input type="submit" value="Update">
								</center>
								</html:form>
							</yrcwww:contentbox>
						</div>
					</td>
				</tr>
				</yrcwww:member>
				<!-- END Form for editing external data information -->
				
			</logic:iterate>
		</table>
		</center>
	
	
	</logic:notEmpty><logic:empty name="externalLinkData" scope="request">
	
		<div style="font-size:12pt;color:#000000;margin-top:10px;margin-bottom:10px;">
			<center>
				No external data associated with project.
			</center>
		</div>
	
	</logic:empty>



	<yrcwww:member group="any">

		<center>
			<input id="showUploadFormButton" type="button" style="font-size:10pt;font-weight:bold;margin-top:20px;" value="+ Add External Data Link" onClick="showExternalDataUploadForm()">
		</center>
		
		

		<div id="externalLinkUploadForm" style="display:none;color:#000000;">
			<yrcwww:contentbox>
			
			<center>
				<div style="color:#000000;margin-bottom:20px;">Fill out the form below to link this project to external data:</div>
			</center>
			
			<html:form action="saveExternalDataLink" method="post"><center>
				<input type="hidden" name="projectID" value="<bean:write name="project" property="ID"  />">

				<table>
					<tr>
						<td>Data URI:</td>
						<td><html:text property="uri" size="50" maxlength="2000" /></td>
					</tr>
					
					<tr>
						<td valign="top">Comments:</td>
						<td valign="top"><html:textarea property="comments" rows="10" cols="50" /></td>
					</tr>
				</table>
			
				<input type="button" style="font-size:10pt;font-weight:bold;margin-top:20px;" value="- Cancel Upload" onClick="hideExternalDataUploadForm()">
				<input type="submit" style="font-size:10pt;font-weight:bold;margin-top:20px;" value="Submit Link">



			</center></html:form>
			</yrcwww:contentbox>
		</div>
	
		
		<script language="javascript">

			var uploadBox = document.all? document.all["externalLinkUploadForm"] : document.getElementById? document.getElementById("externalLinkUploadForm") : "";
			var uploadButton = document.all? document.all["showUploadFormButton"] : document.getElementById? document.getElementById("showUploadFormButton") : "";
			
			function showExternalDataUploadForm() {
				uploadBox.style.display = "inline";
				showUploadFormButton.style.display = "none";
			
			}
			
			function hideExternalDataUploadForm() {
				uploadBox.style.display = "none";
				showUploadFormButton.style.display = "inline";
			}
			
			function showExternalDataEditForm(editFormId) {
				var editBox = document.all? document.all[editFormId] : document.getElementById? document.getElementById(editFormId) : "";
				editBox.style.display = "inline";
			}
			
			function hideExternalDataEditForm(editFormId) {
				var editBox = document.getElementById(editFormId);
				editBox.style.display = "none";
			}

		</script>
		
		
	</yrcwww:member>
	



</yrcwww:contentbox>
