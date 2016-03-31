<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.etouch.beans.DocusignResults" %>
<%@ page import="com.etouch.model.StoreData" %>

<html>
<head>
    <link type="text/css" rel="stylesheet" href="/css/main.css"/>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>
</head>

<body>
<%@ include file="top.jsp" %>
<br/>
<div class="container" style="width: 1500px;">
	<div class="panel panel-default">
	  	<!-- Default panel contents -->
	  	<div class="panel-heading">Docusign Status</div>
	
	  	<div class="row">
		  <div class="col-md-3">Header</div>
		  <div class="col-md-1">Status</div>
		  <div class="col-md-1">Date</div>
		  <div class="col-md-5">Full Json</div>
		  <div class="col-md-2">Document</div>
		</div>
		<%
		StoreData sd = new StoreData();
		for(DocusignResults item: sd.get()){
			out.println("<div class='row'>");
			out.println("<div class='col-md-3'><pre>"+item.getHeader()+"</pre></div>");
			out.println("<div class='col-md-1'>"+item.getStatus()+"</div>");
			out.println("<div class='col-md-1'>"+item.getDate()+"</div>");
			out.println("<div class='col-md-5'><pre>"+item.getFullJson()+"</pre></div>");
			out.println("<div class='col-md-2'><pre>"+item.getDocument()+"</pre></div>");
			out.println("</div>");
		}
		
		%>
	</div>

	<div class="panel panel-default">
	<div class="panel-heading">Post to Workbench</div>
	<div class="panel-body">
	<form action="/etouchdocusign/WorkerServlet" method="post">
	    <div><textarea name="content" rows="3" cols="60"></textarea></div>
	    <div><input type="submit" value="Post to docusign"/></div>
	</form>
	</div>
	</div>
	
	<div class="panel panel-default">
	<div class="panel-heading">post to Docusign connect</div>
	<form action="/etouchdocusign/docusignconnect" method="post">
	    <div><textarea name="content" rows="3" cols="60"></textarea></div>
	    <div><input type="submit" value="Post to docusign"/></div>
	</form>
	</div>
</div>
</body>
</html>