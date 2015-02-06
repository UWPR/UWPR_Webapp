
var type;
var id;
var sessionId;

function showFiles(t, i, sid) {

	if (typeof(t) != "undefined") { type = t; }
	if (typeof(i) != "undefined") { id = i; }
	if (typeof(sid) != "undefined") { sessionId = sid; }

	var service_url = "http://proteomicsresource.washington.edu/pr/service/filesSearch.do;jsessionid=" + sessionId + "?";
	//var service_url = "http://localhost:8080/pr/service/filesSearch.do?";
	
	service_url += "type=" + type;
	service_url += "&id=" + id;
	service_url += "&jsoncallback=?";
		
	$('#files').empty();
	var con = "<div style=\"font-weight:bold;\">Loading file list from database...</div>";
	$(con).appendTo( "#files");
	
	$.getJSON(service_url,
    function(data){
		
		if( data.data && data.data.length > 0 ) {
		
			$('#files').empty();
	
			// create table to display files
			var test = "<table border=\"0\">";
			
			$.each(data.data, function(i,item){
	
				test += "<tr>\n";
				
				test += "<td valign=\"top\"><a class=\"fileLink\" title=\"Uploaded by " + item.uploaderName + " on " + ( item.uploadDate + '') + "\" href=\"/pr/downloadFile.do?id=" + item.id + "\">" + item.filename + "</a></td>";				
				test += "<td valign=\"top\">" + getSizeString( item.filesize ) + "</td>";				
				test += "<td valign=\"top\"><a href=\"javascript:deleteFile(" + item.id + ")\"><span style=\"color:red;\">[Delete]</a></td>";				

				test += "</tr>";
		
			});
			
			
			test += "</table>";
					    	
			$(test).appendTo( '#files' );
		
		} else {
			
			$('#files').empty();
			
			var test = "<span>No files found.</span>";
			$(test).appendTo( '#files' );
			
		}
		
		var test = "<div><button style=\"font-size:80%;background-color:black;color:white;font-weight:bold;text-decoration:none;margin-top:10px;border-width:2px;border-color:white;\" onClick=\"javascript:fileUploadPopup( '" + type + "', " + id + ")\">Upload File</button></div>";
		$(test).appendTo( '#files' );
		
		// add tooltips to file links
		$("[title]").tooltip();
	});
	
}

function fileUploadPopup() {
	var winHeight = 300
	var winWidth = 500;
	var doc = "/pr/uploadFileForm.do?type=" + type + "&id=" + id;
	window.open(doc, "FILE_UPLOAD_WINDOW", "width=" + winWidth + ",height=" + winHeight + ",status=no,resizable=yes,scrollbars=yes");
}

function deleteFile( fid ) {
	
	if (typeof(fid) == "undefined") { return; }

	if( confirm( "Are you sure you want to delete this file?" ) ) {
		var service_url = "http://proteomicsresource.washington.edu/pr/service/deleteFile.do;jsessionid=" + sessionId + "?";
		//var service_url = "http://localhost:8080/pr/service/deleteFile.do?";
		
		service_url += "id=" + fid;
		service_url += "&jsoncallback=?";
		
		// delete the file via web service
		$.getJSON( service_url, function(data){
		
			// update the list of files on the page
			showFiles();
		} );
	}

}

function getSizeString( nStr ) {

	var suffix;
	if( nStr < 1000 ) {
		suffix = "B";
	} else if( nStr < 1000000 ) {
		nStr /= 1000;
		nStr = Math.round( nStr );
		suffix = "kB";
	} else {
		nStr /= 1000000;
		nStr = Math.round( nStr );
		suffix = "MB";
	}
	
	nStr += '';
	x = nStr.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2 + ' ' + suffix;
}
