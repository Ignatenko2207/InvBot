<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Amazon Investor Club authorisation</title>
</head>
<body>
	<style>
	   <%@include file='/css/style.css' %>
	</style>
	<div id="header">
		<div class="content">
			<h2><a href="http://amazoninvestorclub.com" target="_blank" id="logo">Welcome to Amazon Investor Club</a></h2>
		</div>
	</div>
	<div class="clear"></div>
	<div class="interrupter"></div>
	<div class="content" id="main-box">
	<div id="wrong">
				<h2>User with this password does not exist!</h2>
				<h2>Access denied!!!</h2>
		</div>
		<div id="authorisation">
		
			<form action="authorisation" method="post" enctype="application/x-www-form-urlencoded">
			<div id="form">
				<div id="input-name">					
					<label for="username" id="label-name">Name:</label><input id="input-name" size="24" type="text" name="username" placeholder="Your name">
				</div>
				<div id="input-pass">	
					<label for="password" id="label-pass">Password:</label><input  id="input-pass" size="24" type="password" name="password" placeholder="Your password">
				</div>
				<div id="botton" >
					<input type="submit" class="btn-class" value="Submit">
				</div>
			</div>
			</form>
			
		</div>
		
	</div>
</body>
</html>