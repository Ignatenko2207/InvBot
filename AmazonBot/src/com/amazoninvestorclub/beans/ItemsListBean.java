package com.amazoninvestorclub.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ItemDAO;
import com.amazoninvestorclub.DAO.MoveActionDAO;
import com.amazoninvestorclub.domain.Item;

public class ItemsListBean {
	
	private static Logger log = Logger.getLogger(ItemsListBean.class.getName());
	public ArrayList<Item> itemsList = getItemsList();
	String moveAction;
	
	public ArrayList<Item> getItemsList() {
		try {
			itemsList = ItemDAO.getItems();
			Collections.sort(itemsList, new Comparator<Item>() {
				public int compare(Item I1, Item I2) {
					return I1.name.compareTo(I2.name);
				  	}
				});
		} catch (DAOException e) {
			e.printStackTrace();
		}
		return itemsList;
	}
	
	public String getMoveAction(){
		
		try {
			moveAction = MoveActionDAO.getLastAction();
		} catch (DAOException e) {
			log.log(Level.WARNING, "Method getMoveAction has Exception.");
			e.printStackTrace();
		}
		if(moveAction==null || moveAction.equals("")){
			log.log(Level.WARNING, "Last move action wasn't found. Will returned \"off\".");
			moveAction = "off";
		}
		return moveAction;
	}
		
}
