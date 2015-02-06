	<logic:notEmpty name="project" property="PI">
		<bean:define id="pi" name="project" property="PI" />

		<tr>
			<TD valign="top" width="25%">Lab Director:</TD>
			<TD valign="top" width="75%">
			<html:link href="/pr/viewResearcher.do" paramId="id" paramName="pi" paramProperty="ID">
			    <bean:write name="pi" property="firstName"/> <bean:write name="pi" property="lastName"/>, <bean:write name="pi" property="degree"/></html:link>
			</TD>
		</tr>
	</logic:notEmpty>

    <logic:iterate name="project" property="researchers" id="researcher">
        <tr>
            <TD valign="top" width="25%">Researcher :</TD>
            <TD valign="top" width="75%">
            <html:link action="viewResearcher.do" paramId="id" paramName="researcher" paramProperty="ID">
                <bean:write name="researcher" property="firstName"/> <bean:write name="researcher" property="lastName"/>
                <logic:notEmpty name="researcher" property="degree">
                , <bean:write name="researcher" property="degree"/>
                </logic:notEmpty>
            </html:link>
            </TD>
        </tr>
    </logic:iterate>

    <logic:notEmpty name="project" property="PI">
        <tr>
            <TD valign="top" width="25%">Organization:</TD>
            <TD valign="top" width="75%"><bean:write name="pi" property="organization"/>
        </tr>
    </logic:notEmpty>
	
