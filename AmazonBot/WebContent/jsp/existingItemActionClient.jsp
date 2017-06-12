<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.text.*" %>
<%@ page import="com.amazoninvestorclub.domain.Item" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>
	<%
	if((session.getValue("action").equals("edit"))){
	%>
		Edit item
	<%
	} else{
	%>
		Create item
	<%
	}
	%>
	
</title>
</head>
<body>
	<style>
	   <%@include file='/css/createitemstyle.css' %>
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
				<%
				if(session.getValue("action").equals("create")){
				%>
					<li><a class="active"  href="">Create new item</a></li>
				<%
				} else{
				%>
					<li><a class="active"  href="">Edit item: <%=session.getValue("name") %></a></li>
				<%	
				}
				%>
			</ul>
			<br>
			<div class="tab-content" class="table-tab">
				<div class="table-content">
					<table>
						<div class="content" id="main-box">
							<%
							if(session.getValue("action").equals("create")){
							%>
							<div class="table-header">
								<form action="">
									<input type="text" name="link-item" id="input-link" placeholder="Paste link to fast creation of item" size="124">
									<input type="submit" class="input-btn"" value="Check link">
								</form>
							</div>
							<div id="form">
								<form action="">
									<div id="authorisation">
										<div class="input-item" id="left-side-info">
											<input type="text" required name="item-name" class="input-field" size="50" placeholder="Input name of item"></br>
											<input type="text" required name="item-asin" class="input-field" size="50" placeholder="Input ASIN of item"></br>
											<input type="text" required name="item-price" class="input-field" size="50" placeholder="Input price of item"></br>
											<input type="text" required name="item-image" class="input-field" size="50" placeholder="Input link to image of item"></br>
											<input type="text" required name="item-group" class="input-field" size="50" placeholder="Input group of item (if you know)"></br>
											<input type="hidden" name="action" value="addItem">
											<input type="submit" class="btn-class"" value="Create item">
											</br>
										</div>
											<div class="input-item" id="right-side-info">
											<input type="text" required name="item-keyword" class="input-field" size="50" placeholder="Input keyword of item"></br>
											<input type="text" required name="item-maxincart" class="input-field" size="50" placeholder="Input maximal numbers of items to add into carts"></br>
											<input type="text" required name="item-position" class="input-field" size="50" placeholder="Input position of item (if you know)"></br>
											<input type="text" required name="item-page" class="input-field" size="50" placeholder="Input page of item (if you know)"></br>
											<input type="text" required name="item-rank" class="input-field" size="50" placeholder="Input rank of item (if you know)"></br>
											</br>
										</div>
									<div  >
									
								</form>	
								</div>
								
							</div>
							<%
							}
							if(session.getValue("action").equals("createByLink")){
							%>
							<div class="table-header">
								<form action="">!!!!!!!!Put some servlet
									<input type="text" name="link-item" id="input-link" placeholder="Paste link to fast creation of item" size="124">
									<input type="submit" class="input-btn"" value="Check link">
								</form>
							</div>
							<div id="form">
								<form action="">!!!!!!!!Put some servlet
									<div id="authorisation">
										<div class="input-item" id="left-side-info">
											<input type="text" required name="item-name" class="input-field" size="50" placeholder="Input name of item"></br>
											<input type="text" required name="item-asin" class="input-field" size="50" placeholder="Input ASIN of item"></br>
											<input type="text" required name="item-price" class="input-field" size="50" placeholder="Input price of item"></br>
											<input type="text" required name="item-image" class="input-field" size="50" placeholder="Input link to image of item"></br>
											<input type="text" required name="item-group" class="input-field" size="50" placeholder="Input group of item (if you know)"></br>
											<input type="hidden" name="action" value="addItem">
											<input type="submit" class="btn-class"" value="Create item">
											</br>
										</div>
											<div class="input-item" id="right-side-info">
											<input type="text" required name="item-keyword" class="input-field" size="50" placeholder="Input keyword of item"></br>
											<input type="text" required name="item-maxincart" class="input-field" size="50" placeholder="Input maximal numbers of items to add into carts"></br>
											<input type="text" required name="item-position" class="input-field" size="50" placeholder="Input position of item (if you know)"></br>
											<input type="text" required name="item-page" class="input-field" size="50" placeholder="Input page of item (if you know)"></br>
											<input type="text" required name="item-rank" class="input-field" size="50" placeholder="Input rank of item (if you know)"></br>
											</br>
										</div>
									<div  >
								</form>	
								</div>
							</div>
							<%	
							}
							if(session.getValue("action").equals("edit")){
							%>
							<div id="form">
								<form action="/AmazonBot/servlets/ItemActionServlet" >
									<div id="authorisation">
										<div class="input-item" id="left-side-info">
											
											<jsp:useBean id="itemFromDB" scope="request" class="com.amazoninvestorclub.beans.ItemFromDBBean" />
											<jsp:useBean id="rankAsString" scope="request" class="com.amazoninvestorclub.beans.RankParser" />
											<jsp:useBean id="priceAsString" scope="request" class="com.amazoninvestorclub.beans.PriceParser" />
											<%
											String name = (String) session.getValue("name");
											String asin = (String) session.getValue("asin");
											Item item = itemFromDB.getItem(name, asin);
											String rank = rankAsString.getRankString(item.ranking);
											String price = priceAsString.getPriceString(item.price);
											%>
											<input type="text" required name="item-name" class="input-field" size="50" placeholder="Input name of item" value="<%=item.name%>"></br>
											<input type="text" required name="item-asin" class="input-field" size="50" placeholder="Input ASIN of item" value="<%=item.asin%>"></br>
											<input type="text" required name="item-price" class="input-field" size="50" placeholder="Input price of item" value="<%=price%>"></br>
											<input type="text" required name="item-image" class="input-field" size="50" placeholder="Input link to image of item" value="<%=item.imgSource%>"></br>
											<input type="text" required name="item-group" class="input-field" size="50" placeholder="Input group of item (if you know)" value="<%=item.group%>"></br>
											<input type="hidden" name="oldItemName" value="<%=item.name%>">
											<input type="hidden" name="oldItemAsin" value="<%=item.asin%>">
											<input type="hidden" name="action" value="addItem">
											<input type="hidden" name="btn" value="editItem">
											<input type="submit" class="btn-class"" value="Edit item">
											</br>
										</div>
											<div class="input-item" id="right-side-info">
											<input type="text" required name="item-keyword" class="input-field" size="50" placeholder="Input keyword of item" value="<%=item.keyWord%>"></br>
											<input type="text" required name="item-maxincart" class="input-field" size="50" placeholder="Input maximal numbers of items to add into carts" value="<%=item.maxInCart%>"></br>
											<input type="text" required name="item-position" class="input-field" size="50" placeholder="Input position of item (if you know)" value="<%=item.position%>"></br>
											<input type="text" required name="item-page" class="input-field" size="50" placeholder="Input page of item (if you know)" value="<%=item.page%>"></br>
											<input type="text" required name="item-rank" class="input-field" size="50" placeholder="Input rank of item (if you know)" value="<%=rank%>"></br>
											</br>
										</div>
									<div  >
									
								</form>	
								</div>
								
							</div>
							<%	
							}
							%>
							<div id="wrong">
								<h2>Item with this ASIN exists in DataBase! Item <%=session.getValue("newName") %> was not saved!</h2>
							</div>
						</div>
						
					</table>
				</div>
			</div>
			
		</div>
	</div>
</body>
</html>