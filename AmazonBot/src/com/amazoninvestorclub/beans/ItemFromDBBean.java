package com.amazoninvestorclub.beans;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ItemActionDAO;
import com.amazoninvestorclub.DAO.ItemDAO;
import com.amazoninvestorclub.DAO.KeyDAO;
import com.amazoninvestorclub.DAO.KeyStatDAO;
import com.amazoninvestorclub.domain.Item;
import com.amazoninvestorclub.domain.ItemAction;
import com.amazoninvestorclub.domain.KeyStat;
import com.amazoninvestorclub.domain.KeyWord;

public class ItemFromDBBean {

	private static Logger log = Logger.getLogger(ItemFromDBBean.class.getName());
	private Item item = new Item();

	public Item getItem(String itemName, String itemAsin) {
		try {
			item = ItemDAO.getItemByAsin(itemName, itemAsin);
		} catch (DAOException e) {
			log.log(Level.WARNING, "Item " + itemName + " wasn't found.");
			e.printStackTrace();
		}
		return item;
	}

	public ArrayList<ItemAction> getActionsOfItem(String itemAsin) {
		ArrayList<ItemAction> itemActions = new ArrayList<>();
		try {
			itemActions = ItemActionDAO.getItemActionsByAsin(itemAsin);
		} catch (DAOException e) {
			log.log(Level.WARNING, "Getting item's action has Exception.");
			e.printStackTrace();
		}
		return itemActions;

	}

	public ArrayList<KeyWord> getKeysForItem(String itemAsin) {
		ArrayList<KeyWord> keys = new ArrayList<>();
		try {
			keys = KeyDAO.getKeysForItem(itemAsin);
		} catch (DAOException e) {
			log.log(Level.WARNING, "Getting item's keys has Exception.");
			e.printStackTrace();
		}
		return keys;
	}

	public ArrayList<KeyStat> getStatDataByKey(String key, String itemAsin) {
		ArrayList<KeyStat> statData = new ArrayList<>();
		try {
			statData = KeyStatDAO.getStatDataByKey(key, itemAsin);
		} catch (DAOException e) {
			log.log(Level.WARNING, "Getting key's stat data has Exception.");
			e.printStackTrace();
		}
		return statData;
	}
	
	public ArrayList<KeyStat> getStatDataForItem(String itemAsin) {
		ArrayList<KeyStat> statData = new ArrayList<>();
		
		try {
			statData = KeyStatDAO.getAllStatDataForItem(itemAsin);
		} catch (DAOException e) {
			e.printStackTrace();
		}
		
		System.out.println("----------------------------");
		KeyStat tempKS = new KeyStat();
		for (int i = 0; i < (statData.size()-1); i++) {
			for (int j = 0; j < (statData.size()-1); j++) {
				if (statData.get(j).addTime < statData.get(j+1).addTime) {
					tempKS = statData.get(j);
					statData.set(j, statData.get(j+1));
					statData.set((j+1), tempKS);
				}
			}
		}
		
		return statData;
	}
}
