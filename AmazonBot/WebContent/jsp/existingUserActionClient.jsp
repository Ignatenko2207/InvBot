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
				<div class="table-content">
					<table>
						<div class="content" id="main-box">
							<div id="wrong">
								<h2>User with this password exists in DataBase!</h2>
								<h2>User <%=session.getValue("userName") %> was not saved!</h2>
							</div>
								<div id="authorisation">
								<div id="form">
								<form action="UserActionServlet">
								<div id="input-name">					
									<label for="username" id="label-name">Name:</label><input id="input-name" size="24" type="text" name="userName" required value="<%=session.getValue("userName") %>" placeholder="Input name">
								</div>
								<div id="input-pass">	
									<label for="password" id="label-pass">Password:</label><input  id="input-pass" size="24" type="password" name="userPassword" required value="<%=session.getValue("userPassword") %>" placeholder="Input password">
								</div>
								<input type="hidden" name="action" value="add">
								<input type="hidden" name="actionBtn" value="<%=session.getValue("btn") %>">
								<input type="hidden" name="oldName" value="<%=session.getValue("userName") %>">
								<input type="hidden" name="oldPass" value="<%=session.getValue("userPassword") %>">								
								<div id="botton" >
									<input type="submit" class="btn-class" value="<%=session.getValue("btn") %>">
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