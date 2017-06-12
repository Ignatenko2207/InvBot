<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.text.*" %>
<%@ page import="com.amazoninvestorclub.domain.Item" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Amazon Investor Club infolist</title>
</head>
<body>
	<style>
	   <%@include file='/css/liststyle1.css' %>
	</style>

	<div id="header">
		<div class="content">
			<h2><a href="http://amazoninvestorclub.com"  id="logo">Welcome to Amazon Investor Club</a></h2>
		</div>
	</div>
	<div class="clear"></div>
	
	<br>
	<br>
	<div class="content">
		<div class="table">
			<ul class="nav-tabs">
				<li><a class="active" href="infoitem.jsp">Items</a></li>
				<li><a href="/AmazonBot/servlets/AccountFilterServlet?unused=on">Accounts</a></li>
				<li><a href="infouser.jsp">Users</a></li>
			</ul>
			<br>
		
			<div class="tab-content" class="table-tab">
				<div class="table-header">
					<a href="/AmazonBot/servlets/ItemActionServlet?action=create" class="tab-btn">Add item</a>
					<jsp:useBean id="itemsDB" scope="request" class="com.amazoninvestorclub.beans.ItemsListBean" />
					<jsp:useBean id="RankParser" scope="request" class="com.amazoninvestorclub.beans.RankParser" />
					
					<%
						String button = itemsDB.getMoveAction();
					%>
					<form>
						<input type="hidden" name="action" value="main-move">
						<button class="main-moove-btn" formaction="/AmazonBot/servlets/ItemActionServlet" id="main-btn-<%=button %>">Move items on Amazon: <%=button %></button>
					</form>
				</div>
				<br>
				<div class="table-content">
					<table id="table">
						<tr class="tr-header">
							<th width="70px">Move up</th>
							<th width="220px">Name</th>
							<th width="120px">ASIN</th>
							<th width="50px">Image</th>
							<th width="150px">Key word</th>
							<th width="60px">In carts</th>
							<th width="60px">Position</th>
							<th width="60px">Page</th>
							<th width="60px">Ranking</th>
							<th width="80px">Edit/Delete</th>
						</tr>
						
						<%
							List<Item> itemsList = itemsDB.getItemsList();
							
							for (Item item: itemsList) {
						%>
						<tr class="tr-content">
							<td>
							<form>
							<input type="hidden" name="itemName" value="<%=item.name %>">
							<input type="hidden" name="itemAsin" value="<%=item.asin %>">
							<input type="hidden" name="action" value="move">
							<button class="moove-btn" formaction="/AmazonBot/servlets/ItemActionServlet" id="btn-<%=item.move %>"><%=item.move %></button></td>
							</form>
							</td>
							<td class="item-link" ><a title="Click to see more info" href="/AmazonBot/servlets/ItemActionServlet?itemName=<%=item.name %>&itemAsin=<%=item.asin %>&action=info"><%=item.name %></a>
							<td><%=item.asin %></td>
							<td><img src="<%=item.imgSource %>"></td>
							<td><%=item.keyWord %></td>
							<td><%=item.nowInCart %> of <%=item.maxInCart %></td>
							<td><%=item.position %></td>
							<td><%=item.page %></td>
							<td><%=RankParser.getRankString(item.ranking) %></td> 
							<td class="row-btn">
								<div class="link-edit">
									<text>_</text><a href="/AmazonBot/servlets/ItemActionServlet?itemName=<%=item.name %>&itemAsin=<%=item.asin %>&action=edit" class="btn-link"><img src="\AmazonBot\images\edit.png" alt="edit"></a>
    							</div>
     							
     							<div class="actionItem">
									<text>_</text><a href="/AmazonBot/servlets/ItemActionServlet?itemName=<%=item.name %>&itemAsin=<%=item.asin %>&action=delete" class="btn-link"><img src="\AmazonBot\images\delete.png" alt="delete"></a>
								</div>
							</td>
						</tr>	
						<%}	%>
					</table>
				</div>
				<div class="table-footer">
				</div>
			</div>
			
		</div>
	</div>
</body>
</html>