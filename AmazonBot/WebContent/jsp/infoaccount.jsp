<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="com.amazoninvestorclub.beans.AccountsListBean"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.text.*" %>
<%@ page import="com.amazoninvestorclub.domain.Account" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Amazon Investor Club infolistst</title>
</head>
<body>
	<style>
	   <%@include file='/css/liststyle2.css' %>
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
				<li><a class="active" href="infoaccount.jsp">Accounts</a></li>
				<li><a href="infouser.jsp">Users</a></li>
			</ul>
			<br>
		
			<div class="tab-content" class="table-tab">
				<div class="table-header">
					<a href="/AmazonBot/servlets/AccountActionServlet?accountUsed=0&action=create" class="tab-btn">Add account</a>
				</div>
				<br>
				<div class="table-content">
					<table>
						<tr class="tr-header">
							<th class="col-login">Login</th>
							<th>Password</th>
							<th>Used</th>
							<th>Edit/Delete</th>
						</tr>
						<tr class="tr-content">
						<jsp:useBean id="accountDB" scope="request" class="com.amazoninvestorclub.beans.AccountsListBean" />
						<% 
						List<Account> accountsList = new ArrayList();
						if(session.getValue("used").equals("1")&&session.getValue("unused").equals("1")){
							accountsList = accountDB.getAccountsList();
						}
						if(session.getValue("used").equals("1")&&session.getValue("unused").equals("0")){
							accountsList = accountDB.getAccountsListByFilter("used");
						}
						if(session.getValue("used").equals("0")&&session.getValue("unused").equals("1")){
							accountsList = accountDB.getAccountsListByFilter("unused");
						}
						if(accountsList.isEmpty()==false){
							for(Account account: accountsList)	{
								%>
								<tr class="tr-content">
									<td><%=account.login %></td>
									<td><%=account.password %></td>
									<td><%=account.used %></td>
									<td class="row-btn">
										<div class="link-edit">
											<text>_______</text><a href="/AmazonBot/servlets/AccountActionServlet?accountName=<%=account.login %>&accountPassword=<%=account.password %>&accountUsed=<%=account.used %>&action=edit" class="btn-link"><img src="\AmazonBot\images\edit.png" alt="edit"></a>
		    							</div>
		     							<div class="link-delete">
											<a href="/AmazonBot/servlets/AccountActionServlet?accountName=<%=account.login %>&accountPassword=<%=account.password %>&accountUsed=<%=account.used %>&action=delete" class="btn-link"><img src="\AmazonBot\images\delete.png" alt="delete"></a><text>_______</text>
										</div>		
									</td>
								</tr>	
							<%}
						}
						 %>
					</table>
				</div>
				<br>
				<div class="table-footer">
					<form action="/AmazonBot/servlets/AccountFilterServlet">
						<%
						if(session.getValue("used").equals("1")){
							%>
							<label><input type="checkbox" name="used" checked/>Used</label>
							<%
						} else{
							%>
							<label><input type="checkbox" name="used" />Used</label>
							<%
						}
						if(session.getValue("unused").equals("1")){
							%>
							<label><input type="checkbox" name="unused" checked/>Unused</label>
							<%
						} else{
							%>
							<label><input type="checkbox" name="unused" />Unused</label>
						<%}%>
						<input type="submit" id="filter-btn" value="Filter"/>
					</form>
				</div>
			</div>
			
		</div>
	</div>
</body>
</html>