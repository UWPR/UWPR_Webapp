<logic:messagesPresent message="false">

<div id="wide-content" style="margin-bottom: 20px;">

	<div class="box-style1">
	
		<div class="title">
		
			<h2><span style="color:red">Errors Encountered</span></h2>
			
		</div>
		
		<div class="content" style="text-align:center;">
			<div style="color:red; width:60%; text-align:left;margin: 0 auto;">
				<UL>
	     		<html:messages id="messages">
	       			<LI>
	         			<bean:write name="messages" filter="false"/>
	       			</LI>
	     		</html:messages>
	   			</UL>
   			</div>
		</div>
		
	</div>
	
</div>
</logic:messagesPresent>