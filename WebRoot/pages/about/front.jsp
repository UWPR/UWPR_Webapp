<%@ taglib uri="/WEB-INF/yrc-www.tld" prefix="yrcwww" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>


<%@ include file="/includes/header.jsp" %>

<yrcwww:contentbox title="UW Proteomics Resource">

		<p>
		The University of Washington's Proteomics Resource (UWPR) is a proteomics facility whose mission is 
		to advance proteomic technologies and apply these technologies to significant biological problems. 
		The UWPR will perform experiments motivated by UWPR members as well as collaborate on projects with the larger UW community.
		</p>

		<p>
		The UWPR is operated as a Cost Center through which institutional users (academic, non-profit and corporate) contribute 
		to the upkeep of the resource by means of a recharge system. Rates are determined annually and are based on the operating 
		costs and individual instrument usage. All rates are reviewed and approved by the University of Washington Administration 
		and are subject to change. The UWPR will bill for its services on a monthly basis. 
		</p>
		
		<p>
		The UWPR will be supporting a limited number of pilot projects by granting UW members access to resources at no charge. 
		The intent is to support small scale projects (max. 5 days of instrument time), 
		e.g. to produce preliminary data for a grant proposal. 
		All UWPR supported projects require an active collaboration with one of the UWPR PIs to ensure optimal experimental design. 
		Please note that abstracts submitted for UWPR supported instrument time will undergo critical review and a 
		detailed justification will be necessary for approval of instrument time. 
		The review process can take upward of one month. Please contact us for more information. 
		If your request gets approved, contact UWPR personnel to schedule instrument time for you.
		<b>NOTE:</b> There is a limit of 50 RAW files or 5 days (whichever happens first) for each subsidized project 
		and five projects per PI per year. Click 
		<html:link href="/pr/newCollaboration.do">here</html:link>
		to start a 
		<span title="The UWPR supports a limited number of small scale projects that grant access to the resources at no charge.  These projects require collaboration with one of the UWPR PI's and undergo critical review for approval. Once approved, instrument time can be scheduled only by UWPR personnel.">
		<nobr><b><html:link href="/pr/newCollaboration.do">UWPR supported project</html:link></b></nobr>.
		</span>
		</p>

		<p>
		The UWPR is located at the UW's <a href='http://depts.washington.edu/somslu/'>South Lake Union campus</a>
		and provides access to state of the art instrumentation and computational support for Resource members and their collaborators. 
		The UWPR is governed by a Resource Advisory group that is currently composed of Bob Waterson, Mike MacCoss, Dave Goodlett, 
		Jay Heinecke, Trisha Davis, Stan Fields, Jim Bruce, Judit Villen, Alejandro Wolf-Yadlin, Shao-En Ong, and Andy Hoofnagle. 
		</p>

		<p>
		Click <a href="<%=AppProperties.getHost()%>/collab.php">here</a> for more information on how to collaborate.
		</p>
		
		<p>
			For resources available at the UWPR click <a href="<%=AppProperties.getHost()%>/resources.php">here</a>.
		</p>
		<ul>
			<li>
				<b><html:link href="/pr/newBilledProject.do">Start new collaboration</html:link></b>
			</li>
			<li>
				<a href="/pr/costcenter_resources/UWPR_Current_Rates.xlsx">Current instrument rates</a>
			</li>
			<li>
				<a href="/pr/costcenter_resources/UWPR_FAQ_Instrument_scheduling.pdf">Billing FAQ and instructions for scheduling instrument time</a>
			</li>
			<li>
				<html:link href="../admin/costcenter/paymentInformation.jsp">Payment information</html:link>
			</li>
		</ul>
	
		
</yrcwww:contentbox>

<%@ include file="/includes/footer.jsp" %>