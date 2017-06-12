<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=session.getValue("title") %></title>
</head>
<body>
	<style>
	   <%@include file='/css/createstyle.css' %>
	</style>
	<div id="header">
		<div class="content">
			<h2><a href="http://amazoninvestorclub.com" target="_blank" id="logo">Welcome to Amazon Investor Club</a></h2>
		</div>
	</div>
	<br>
	<br>
	<div class="content">
		<div class="table">
			<ul class="nav-tabs">
				<li><a class="active"  href=""><%=session.getValue("title") %></a></li>
			</ul>
			<br>
			<div class="tab-content" class="table-tab">
				<div  class="table-content">
					<table>
						<div class="content" id="main-box">
							<div id="authorisation">
								<div id="form">
								<form action="ItemActionServlet">
								<div id="input-key">					
									<label for="keyname"  id="label-key">Key:</label>
									<input id="keyname" required size="24" type="text" name="key" required 
									<%
									if(session.getValue("title").toString().contains("Edit")){
										%> value="<%=session.getValue("key") %>"<%
									}
									%> placeholder="Input key">
								</div>
								<div id="input-count">	
									<label for="count" id="label-count">Adding in day:</label>
									<input  id="count" required size="24" type="text" name="count" placeholder="Input amount of adding">
								</div>
								<input type="hidden" name="oldKey" value="<%=session.getValue("key") %>">
								<input type="hidden" name="action" value="<%=session.getValue("title") %>Key">
								<input type="hidden" name="itemAsin" value="<%=session.getValue("itemAsin") %>">
								<div id="botton" >
									<input type="submit" class="btn-class" value="<%=session.getValue("title") %> key">
								</div>
								</form>
							</div>
						</div>
					</table>
				</div>
			</div>
			
		</div>
	</div>
</body>
</html>