package com.amazoninvestorclub.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ItemActionDAO;
import com.amazoninvestorclub.DAO.ItemDAO;
import com.amazoninvestorclub.DAO.KeyDAO;
import com.amazoninvestorclub.DAO.MoveActionDAO;
import com.amazoninvestorclub.beans.MoveItemAmazonBean;
import com.amazoninvestorclub.beans.PageAmazonParser;
import com.amazoninvestorclub.beans.PriceParser;
import com.amazoninvestorclub.beans.RankParser;
import com.amazoninvestorclub.domain.Item;
import com.amazoninvestorclub.domain.KeyWord;
import com.amazoninvestorclub.domain.WaitTimer;

@SuppressWarnings("serial")
@WebServlet("/itemAction")
public class ItemActionServlet extends HttpServlet {


	MoveItemAmazonBean moveItem = new MoveItemAmazonBean();
	private static Logger log = Logger.getLogger(ItemActionServlet.class.getName());
    public ItemActionServlet() {
        super();
        
    }

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//req.setCharacterEncoding("UTF-8");
       	//resp.setCharacterEncoding("UTF-8");
       	
       	String itemName = (String) req.getParameter("itemName");
       	String itemAsin = (String) req.getParameter("itemAsin");
       	String action = (String) req.getParameter("action");
       	
       	if(action.equals("create")){
       		HttpSession session = req.getSession();
       		session.setAttribute("action", "create");
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/itemActionClient.jsp");
    		dispatcher.forward(req, resp);
       	}
       	
       	if(action.equals("checkLink")){
       		String itemLink = req.getParameter("link-item");
       		
       		Item itemFromLink = new Item();
       		PageAmazonParser parser = new PageAmazonParser();
       		
       		/// should stop all threads
       		
			log.log(Level.INFO, "Method doGet -> checkLink.");
					
			new WaitTimer().waitSeconds(5);
			
       		itemFromLink = parser.getItemFromLink(itemLink);
       		String nameLink = itemFromLink.name;
       		String asinLink = itemFromLink.asin;
       		String priceLink = new PriceParser().getPriceString(itemFromLink.price);
       		String imgLink = itemFromLink.imgSource;
       		String groupLink = itemFromLink.group;
       		
       		if(moveItem.isAlive()){
				log.log(Level.INFO, "Method doGet -> checkLink. Getting item's info is finished. MoveItem thread is continued.");
				moveItem.notify();
			}

       		HttpSession session = req.getSession();
       		session.setAttribute("action", "createByLink");
       		session.setAttribute("link", itemLink);
       		session.setAttribute("nameLink", nameLink);
       		session.setAttribute("asinLink", asinLink);
       		session.setAttribute("imgLink", imgLink);
       		session.setAttribute("priceLink", priceLink);
       		session.setAttribute("groupLink", groupLink);
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/itemActionClient.jsp");
    		dispatcher.forward(req, resp);
       	}
       	
       	if(action.equals("edit")){
       		HttpSession session = req.getSession();
       		session.setAttribute("action", "edit");
       		session.setAttribute("name", itemName);
       		session.setAttribute("asin", itemAsin);
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/itemActionClient.jsp");
    		dispatcher.forward(req, resp);
       	}
       	if(action.equals("delete")){
       		try {
				ItemDAO.deleteItem(itemName, itemAsin);
				// delete keys and actions
				
				KeyDAO.deleteKeys(itemAsin);
				ItemActionDAO.deleteActionsWithItem(itemAsin);
				
				resp.sendRedirect("/AmazonBot/jsp/infoitem.jsp");
			} catch (DAOException e) {
				e.printStackTrace();
				log.log(Level.WARNING, "Method doGet -> delete. Item wasn't deleted.");
			}
		}
       	if(action.equals("info")){
       		HttpSession session = req.getSession();
       		session.setAttribute("itemName", itemName);
       		session.setAttribute("itemAsin", itemAsin);
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/detailedInfoItem.jsp");
    		dispatcher.forward(req, resp);
       	}
       	if(action.equals("addItem")){
       		
       		String btn = (String) req.getParameter("btn");
       		if(btn.equals("createNew")){
       		//new fields to DB
       			String newName = (String) req.getParameter("item-name");
       			String newAsin = (String) req.getParameter("item-asin");
       			String priceAsString = (String) req.getParameter("item-price");
       			PriceParser priceParser = new PriceParser();
       			int newPrice = priceParser.getPriceInteger(priceAsString);       			
       			//img check
       			String newImg = (String) req.getParameter("item-image");
       			if(newImg.isEmpty()||newImg.equals("null")){
       				newImg = "\\AmazonBot\\images\\noPhotoEn.png";
       			}
       			//group check
       			String newGroup = (String) req.getParameter("item-group");
       			if(newGroup.isEmpty()||newGroup.equals("null")){
       				newGroup = "no group";
       			}
       			String newKeyword = (String) req.getParameter("item-keyword");
       			String MaxInCartAsString = (String) req.getParameter("item-maxincart");
       			int newMaxInCarts = Integer.valueOf(MaxInCartAsString);
       			//position check
       			String positionAsString = (String) req.getParameter("item-position");
       			if(positionAsString.isEmpty()||positionAsString.equals("null")){
       				positionAsString = "0";
       			}
       			int newPosition = Integer.valueOf(positionAsString);
       			//page check
       			String pageAsString = (String) req.getParameter("item-page");
       			if(pageAsString.isEmpty()||pageAsString.equals("null")){
       				pageAsString = "0";
       			}
       			int newPage = Integer.valueOf(pageAsString);
       			//rank check
       			String rankAsString = (String) req.getParameter("item-rank");
       			if(rankAsString.isEmpty()||rankAsString.equals("null")){
       				rankAsString = "0";
       			}
       			RankParser rankParser = new RankParser();
       			int newRank = rankParser.getRankInteger(rankAsString);
       			
       			int nowInCarts = 0;
       			long lastAdding = 0;
       			String move = "off";
       			long sellDate = System.currentTimeMillis();
       			
       			Item checkedItem = new Item();
   				try {	
   					checkedItem = ItemDAO.getItemByAsin(newName, newAsin);
   				} catch (DAOException e1) {
   					log.log(Level.WARNING, "Method doGet -> addItem. Checked item "+checkedItem+" wasn't found.");
   					e1.printStackTrace();
   				} 
   				
   				if(checkedItem.name==null||checkedItem.name.equals("missmatch")){
   					try {
   						ItemDAO.create(newName, newAsin, newPrice, newImg, newKeyword, newGroup, sellDate, newRank, newMaxInCarts, nowInCarts, lastAdding, newPosition, newPage, move);
   						KeyDAO.create(newKeyword, 50, 0, newAsin, "");
   						resp.sendRedirect("/AmazonBot/jsp/infoitem.jsp");
   	       			} catch (DAOException e) {
   	       				log.log(Level.WARNING, "Method doGet -> addItem. Item wasn't created.");
   						e.printStackTrace();
   					}
   				} else{
   					HttpSession session = req.getSession();
           			session.setAttribute("action", "create");
           	    	session.setAttribute("name", newName);
           	    	session.setAttribute("asin", newAsin);
           	    	session.setAttribute("newName", newName);
           	    	RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/existingItemActionClient.jsp");
           	    	dispatcher.forward(req, resp);
   				}
       		}
       		
       		if(btn.equals("editItem")){
       			String editedItemName = (String) req.getParameter("oldItemName");
       			String editedItemAsin = (String) req.getParameter("oldItemAsin");
       			Item editedItem = new Item();
       			try {
       				editedItem = ItemDAO.getItemByAsin(editedItemName, editedItemAsin);
				} catch (DAOException e) {
					log.log(Level.WARNING, "Method doGet -> editItem. Edited item  wasn't found.");
				}
       			int nowIn = editedItem.nowInCart;
       			long sellDate = editedItem.sellDate;
       			long lastAdd = editedItem.lastAdding;
       			String move = editedItem.move;
       			
       			//new fields to DB
       			String newName = (String) req.getParameter("item-name");
       			String newAsin = (String) req.getParameter("item-asin");
       			String priceAsString = (String) req.getParameter("item-price");
       			PriceParser priceParser = new PriceParser();
       			int newPrice = priceParser.getPriceInteger(priceAsString);       			
       			//img check
       			String newImg = (String) req.getParameter("item-image");
       			if(newImg.isEmpty()||newImg.equals("null")){ //!!!!!!!!!!!!!!!!!
       				newImg = "\\AmazonBot\\images\\noPhotoEn.png";
       			}
       			//group check
       			String newGroup = (String) req.getParameter("item-group");
       			if(newGroup.isEmpty()||newGroup.equals("null")){ //!!!!!!!!!!!!!!!!!
       				newGroup = "no group";
       			}
       			String newKeyword = (String) req.getParameter("item-keyword");
       			String MaxInCartAsString = (String) req.getParameter("item-maxincart");
       			int newMaxInCarts = Integer.valueOf(MaxInCartAsString);
       			//position check
       			String positionAsString = (String) req.getParameter("item-position");
       			if(positionAsString.isEmpty()||positionAsString.equals("null")){
       				positionAsString = "0";
       			}
       			int newPosition = Integer.valueOf(positionAsString);
       			//page check
       			String pageAsString = (String) req.getParameter("item-page");
       			if(pageAsString==null || pageAsString.isEmpty()){ 
       				pageAsString = "0";
       			}
       			int newPage = Integer.valueOf(pageAsString);
       			//rank check
       			String rankAsString = (String) req.getParameter("item-rank");
       			if(rankAsString==null || rankAsString.isEmpty()){
       				rankAsString = "0";
       			}
       			RankParser rankParser = new RankParser();
       			int newRank = rankParser.getRankInteger(rankAsString);
       			
       			//check for the same item in DB
   				Item checkedItem = new Item();
   				try {
   					checkedItem = ItemDAO.getItemByAsin(newName, newAsin);
   				} catch (DAOException e1) {
   					log.log(Level.WARNING, "Method doGet -> addItem. Checked item "+checkedItem+" wasn't found.");
   					e1.printStackTrace();
   				}
       			
       			if(newAsin.equals(checkedItem.asin)){
       				if(newAsin.equals(editedItemAsin)){
       					try {
       						ItemDAO.editItem(editedItemName, editedItemAsin, newName, newAsin, newImg, newKeyword, newGroup, newMaxInCarts, nowIn, newPosition, newPage, move, sellDate, lastAdd, newPrice, newRank);
           					if(checkedItem.keyWord!=newKeyword){
           						KeyDAO.create(newKeyword, 50, 0, editedItemAsin, "");
           					}
       						resp.sendRedirect("/AmazonBot/jsp/infoitem.jsp");
               			} catch (DAOException e) {
               				log.log(Level.WARNING, "Method doGet -> addItem. Item "+editedItemName+" wasn't updated.");
        					e.printStackTrace();
        				}
       				} else{
       					HttpSession session = req.getSession();
               			session.setAttribute("action", "edit");
               	    	session.setAttribute("name", editedItemName);
               	    	session.setAttribute("asin", editedItemAsin);
               	    	session.setAttribute("newName", newName);
               	    	RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/existingItemActionClient.jsp");
               	    	dispatcher.forward(req, resp);
       				}
       			}else {
       				try {
       					ItemDAO.editItem(editedItemName, editedItemAsin, newName, newAsin, newImg, newKeyword, newGroup, newMaxInCarts, nowIn, newPosition, newPage, move, sellDate, lastAdd, newPrice, newRank);
       					resp.sendRedirect("/AmazonBot/jsp/infoitem.jsp");
           			} catch (DAOException e) {
           				log.log(Level.WARNING, "Method doGet -> addItem. Item "+editedItemName+" wasn't updated.");
    					e.printStackTrace();
    				}
       			}
       		}
       	}
       	
       	if(action.equals("move")){
       		Item item=new Item();
       		try {
				item = ItemDAO.getItemByAsin(itemName, itemAsin);
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> move. Item "+item+" wasn't found.");
				e.printStackTrace();
			}
       		String flag;
       		if(item.move.equals("off")){
       			flag = "up";
       		} else{
       			flag="off";
       		}
       		try {
				ItemDAO.changeMoveFlag(itemName, itemAsin, flag);
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> move. Item "+item+" wasn't updated.");
				e.printStackTrace();
			}
       		resp.sendRedirect("/AmazonBot/jsp/infoitem.jsp");
       	}
       	
       	if(action.equals("main-move")){
       		
       		String flag;
       		String lastAction="";
			try {
				lastAction = MoveActionDAO.getLastAction();
			} catch (DAOException e1) {
				log.log(Level.WARNING, "Method doGet -> main-move. Last action wasn't found.");
				e1.printStackTrace();
			}
       		
       		if(lastAction.equals("off")){
       			flag = "on";
       		} else{
       			flag="off";
       		}
       		try {
       			MoveActionDAO.addAction(flag);
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> main-move. Last action wasn't saved.");
				e.printStackTrace();
			}
       		resp.sendRedirect("/AmazonBot/jsp/infoitem.jsp");
       	}
       	
       	if(action.equals("createKey")){
       		HttpSession session = req.getSession();
       		session.setAttribute("action", "create");
       		session.setAttribute("itemAsin", itemAsin);
       		session.setAttribute("title", "Create");
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/keyActionClient.jsp");
    		dispatcher.forward(req, resp);
       	}
         
		if(action.equals("editKey")){
			HttpSession session = req.getSession();
       		session.setAttribute("action", "edit");
       		session.setAttribute("itemAsin", itemAsin);
       		session.setAttribute("title", "Edit");
       		session.setAttribute("key", req.getParameter("key"));
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/keyActionClient.jsp");
    		dispatcher.forward(req, resp);
			
			
		}
		
		if(action.equals("deleteKey")){
			HttpSession session = req.getSession();
			String key = (String) req.getParameter("key");
			itemAsin = req.getParameter("itemAsin");
			itemName = req.getParameter("itemName");
			try {
       			KeyDAO.deleteKey(key, itemAsin);
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> deleteKey. Last action wasn't saved.");
				e.printStackTrace();
			}
			session.setAttribute("itemName", itemName);
       		session.setAttribute("itemAsin", itemAsin);
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/detailedInfoItem.jsp");
    		dispatcher.forward(req, resp);
		}
		
		if(action.equals("CreateKey")){
			HttpSession session = req.getSession();
			String key = (String) req.getParameter("key");
			String addInDayAsString = (String) req.getParameter("count");
			int count = Integer.valueOf(addInDayAsString);
			try {
       			KeyDAO.create(key, count, 0, itemAsin, "");;
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> addKeye. Last action wasn't saved.");
				e.printStackTrace();
			}
			session.setAttribute("itemName", itemName);
       		session.setAttribute("itemAsin", itemAsin);
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/detailedInfoItem.jsp");
    		dispatcher.forward(req, resp);
		}
		
		if(action.equals("EditKey")){
			HttpSession session = req.getSession();
			String key = (String) req.getParameter("key");
			String oldKey = (String) req.getParameter("oldKey");
			String addInDayAsString = (String) req.getParameter("count");
			itemAsin = (String) req.getParameter("itemAsin");
			int count = Integer.valueOf(addInDayAsString);
			KeyWord editedKey = new KeyWord();
			try {
				editedKey = KeyDAO.getKey(oldKey, itemAsin);
       			KeyDAO.editKey(oldKey, itemAsin, key, count, editedKey.lastAdd, editedKey.itemLink);
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> addKeye. Last action wasn't saved.");
				e.printStackTrace();
			}
			session.setAttribute("itemName", itemName);
       		session.setAttribute("itemAsin", itemAsin);
       		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/detailedInfoItem.jsp");
    		dispatcher.forward(req, resp);
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
