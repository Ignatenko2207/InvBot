<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="com.amazoninvestorclub.domain.KeyStat"%>
<%@page import="com.amazoninvestorclub.domain.KeyWord"%>
<%@page import="com.amazoninvestorclub.domain.ItemAction"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*, java.text.*"%>
<%@ page import="com.amazoninvestorclub.domain.Item"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Amazon Investor Club info</title>
</head>
<body>
	<jsp:useBean id="itemFromDB" scope="request"
		class="com.amazoninvestorclub.beans.ItemFromDBBean" />
	<jsp:useBean id="RankParser" scope="request"
		class="com.amazoninvestorclub.beans.RankParser" />
	<jsp:useBean id="PriceParser" scope="request"
		class="com.amazoninvestorclub.beans.PriceParser" />
	<jsp:useBean id="DateParser" scope="request"
		class="com.amazoninvestorclub.beans.DateParser" />

	<style>
<%@include file='/css/liststyle1.css' %>
</style>

	<div id="header">
		<div class="content">
			<h2>
				<a href="/AmazonBot/jsp/infoitem.jsp" id="logo">Back to item's info</a>
			</h2>
		</div>
	</div>
	<div class="clear"></div>

	<br>
	<br>
	<div class="content">
		<div class="table">
			<ul class="nav-tabs">
				<li><a class="active"
					href="/AmazonBot/servlets/ItemActionServlet?itemName=<%=session.getValue("itemName")%>&itemAsin=<%=session.getValue("itemAsin")%>&action=info">Item:
						<%=session.getValue("itemAsin")%></a></li>
			</ul>
			<br>

			<div class="tab-content" class="table-tab">
				<br>
				<div class="table-content">
					<table id="table">
						<tr class="tr-header">

							<th width="170px">Name</th>
							<th width="120px">ASIN</th>
							<th width="50px">Image</th>
							<th width="60px">Price</th>
							<th width="200px">Group</th>
							<th width="200px">Key word</th>
							<th width="60px">In carts</th>
							<th width="80px">Sell date</th>
							<th width="80px">Last add</th>
							<th width="60px">Position</th>
							<th width="60px">Page</th>
							<th width="60px">Ranking</th>

						</tr>

						<%
							String itemName = (String) session.getValue("itemName");
							String itemAsin = (String) session.getValue("itemAsin");
							Item item = itemFromDB.getItem(itemName, itemAsin);
							ArrayList<KeyWord> keys = itemFromDB.getKeysForItem(itemAsin);
						%>
						<tr class="tr-content">
							<td><%=item.name%></td>
							<td><%=item.asin%></td>
							<td><img src="<%=item.imgSource%>"></td>
							<td><%=PriceParser.getPriceString(item.price)%></td>
							<td><%=item.group%></td>
							<td><%=item.keyWord%></td>
							<td><%=item.maxInCart%>/<%=item.nowInCart%></td>
							<td><%=DateParser.getDateAsString(item.sellDate)%></td>
							<td><%=DateParser.getDateAsString(item.lastAdding)%></td>
							<td><%=item.position%></td>
							<td><%=item.page%></td>
							<td><%=RankParser.getRankString(item.ranking)%></td>
						</tr>
					</table>
				</div>
				<div id="key-info-div">
					<table id="key-info">
						<form>
							<input type="hidden" name="itemName" value="<%=item.name%>">
							<input type="hidden" name="itemAsin" value="<%=item.asin%>">
							<input type="hidden" name="action" value="createKey"> <input
								id="createKey"
								formaction="/AmazonBot/servlets/ItemActionServlet" type="submit"
								value="Create new key-word">
						</form>

						<tr class="tr-header">

							<th width="120px">Key-word</th>
							<th width="60px">Max add</th>
							<th width="80px">Last add</th>
							<th width="80px">Edit/delete</th>
						</tr>

						<%
							for (KeyWord key : keys) {
						%>
						<tr class="tr-content">
							<td><%=key.key%></td>
							<td><%=key.addInDay%></td>
							<td><%=DateParser.getDateAndTimeAsString(key.lastAdd)%></td>
							<td class="row-btn">
								<div class="link-edit">
									<text>_</text>
									<a
										href="/AmazonBot/servlets/ItemActionServlet?key=<%=key.key%>&itemName=<%=item.name%>&itemAsin=<%=item.asin%>&action=editKey"
										class="btn-link"><img src="\AmazonBot\images\edit.png"
										alt="edit"></a>
								</div>

								<div class="actionItem">
									<text>_</text>
									<a
										href="/AmazonBot/servlets/ItemActionServlet?key=<%=key.key%>&itemName=<%=item.name%>&itemAsin=<%=item.asin%>&action=deleteKey"
										class="btn-link"><img src="\AmazonBot\images\delete.png"
										alt="delete"></a>
								</div>
							</td>
						</tr>

						<%
							}
						%>

					</table>
				</div>
				<br>
				<div class="table-content">
					<div id="act-content">
						<table id="table2">
							<tr class="tr-header">
								<th width="120px">Key-word</th>
								<th width="110px">Action time</th>
								<th width="50px">Added</th>
								<th width="50px">Page</th>
								<th width="50px">Pos.</th>
								<th width="60px">Filter</th>
							</tr>
				<%
					
						ArrayList<KeyStat> keyStatData = itemFromDB.getStatDataForItem(itemAsin);
						
						
						if(!keyStatData.isEmpty()){
							
							
							for(KeyStat statKey: keyStatData){
								%>
								<tr class="tr-content">
									<td><%=statKey.key%></td>
									<td><%=DateParser.getDateAndTimeAsString(statKey.addTime)%></td>
									<td><%=statKey.addAmount%></td>
									<td><%=statKey.page%></td>
									<td><%=statKey.position%></td>
									<td><%=statKey.filterUsed%></td>
								</tr>
								<%
							}
							
						}
					
				%>
				

						</table>

					</div>
				</div>




				</br>
				<div class="table-footer"></div>
			</div>

		</div>
	</div>
</body>
</html>