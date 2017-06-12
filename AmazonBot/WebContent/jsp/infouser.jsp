<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.text.*" %>
<%@ page import="com.amazoninvestorclub.domain.User" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Amazon Investor Club infolist</title>
</head>
<body>
	<style>
	   <%@ include file='/css/liststyle3.css' %>
	</style>
	<div id="header">
		<div class="content">
			<h2><a href="http://amazoninvestorclub.com" target="_blank" id="logo">Welcome to Amazon Investor Club</a></h2>
		</div>
	</div>
	<div class="clear"></div>
	<br>
	<br>
	<div class="content">
		<div class="table">
			<ul class="nav-tabs">
				<li><a href="infoitem.jsp">Items</a></li>
				<li><a href="/AmazonBot/servlets/AccountFilterServlet?unused=on">Accounts</a></li>
				<li><a  class="active" href="infouser.jsp">Users</a></li>
			</ul>
			<br>
			<div class="tab-content" class="table-tab">
				<div class="table-header">
					<a href="/AmazonBot/servlets/UserActionServlet?action=create" class="tab-btn">Add user</a>
				</div>
				<br>
				<div class="table-content">
					<table>
						<tr class="tr-header">
							<th class="col-login">Name</th>
							<th>Password</th>
							<th>Edit/Delete</th>
						</tr>
						<jsp:useBean id="usersDB" scope="request" class="com.amazoninvestorclub.beans.UsersListBean" />
						<% 
						List<User> usersList = usersDB.usersList;
							for(User user: usersList)	{
						%>
						<tr class="tr-content">
							<td><%=user.name %></td>
							<td><%=user.password %></td>
							<td class="row-btn">
								<div class="link-edit">
									<text>___________</text><a href="/AmazonBot/servlets/UserActionServlet?userName=<%=user.name %>&userPassword=<%=user.password %>&action=edit" class="btn-link"><img src="\AmazonBot\images\edit.png" alt="edit"></a>
    							</div>
     							<div class="link-delete">
									<a href="/AmazonBot/servlets/UserActionServlet?userName=<%=user.name %>&userPassword=<%=user.password %>&action=delete" class="btn-link"><img src="\AmazonBot\images\delete.png" alt="delete"></a><text>___________</text>
								</div>					
							</td>
						</tr>	
						<%}	%>													
					</table>
				</div>
			</div>
			
		</div>
	</div>
</body>
</html>