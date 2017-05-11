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