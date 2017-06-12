package com.amazoninvestorclub.beans;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ItemDAO;
import com.amazoninvestorclub.DAO.MoveActionDAO;
import com.amazoninvestorclub.domain.Item;
import com.amazoninvestorclub.domain.WaitTimer;

public class MoveItemAmazonBean extends Thread {

	private static Logger log = Logger.getLogger(MoveItemAmazonBean.class.getName());
	WaitTimer timer = new WaitTimer();
	ActionAmazonBean actionAmazon = new ActionAmazonBean();

	@Override
	public void run() {
		log.log(Level.INFO, "Thread is started.");
		timer.waitSeconds(30);
		while (true) {
			String moveFlag = "";
			try {
				moveFlag = MoveActionDAO.getLastAction();
			} catch (DAOException e1) {
				e1.printStackTrace();
				log.log(Level.WARNING, "Getting move flag has Exception.");
				continue;
			}
			if (moveFlag.equals("on")) {
				ArrayList<Item> items = new ArrayList<>();
				try {
					items = ItemDAO.getItemsForMoove();
				} catch (DAOException e) {
					log.log(Level.WARNING, "Getting items for move has Exception.");
					e.printStackTrace();
					continue;
				}

				ArrayList<Item> itemsForAddChecked = new ArrayList<>();

				for (Item item : items) {
					if (item.maxInCart > item.nowInCart && itemsForAddChecked.size() < 10) { // 10
																								// items
																								// to
																								// move
																								// is
																								// max
																								// value
						if (item.lastAdding == 0) {
							item.sellDate = System.currentTimeMillis();
							item.lastAdding = System.currentTimeMillis();
							try {
								ItemDAO.editItem(item.name, item.asin, item.name, item.asin, item.imgSource,
										item.keyWord, item.group, item.maxInCart, item.nowInCart, item.position,
										item.page, item.move, item.sellDate, item.lastAdding, item.price, item.ranking);
							} catch (DAOException e) {
								e.printStackTrace();
							}
							itemsForAddChecked.add(item);
						} else {
							if ((System.currentTimeMillis() - item.sellDate) < (1000 * 60 * 60 * 24)
									|| (System.currentTimeMillis() - item.sellDate) > (1000 * 60 * 60 * 48)) {
								if ((System.currentTimeMillis() - item.lastAdding) > (1000 * 60 * 60 * 12)) {
//									item.lastAdding = System.currentTimeMillis();
//									try {
//										ItemDAO.editItem(item.name, item.asin, item.name, item.asin, item.imgSource,
//												item.keyWord, item.group, item.maxInCart, item.nowInCart, item.position,
//												item.page, item.move, item.sellDate, item.lastAdding, item.price,
//												item.ranking);
//									} catch (DAOException e) {
//										e.printStackTrace();
//									}

									itemsForAddChecked.add(item);
								}
							}
						}
					}
				}

				if (itemsForAddChecked == null || itemsForAddChecked.isEmpty()) {
					log.log(Level.INFO, "In 12 hours check for new items.");
					timer.waitSeconds((60 * 60 * 12) + (60*15)); 		// wait 12
																		// hours
																		// for
																		// other
																		// threads
				} else {
					log.log(Level.INFO, "There was " + itemsForAddChecked.size() + " items found. Try to move them.");

					actionAmazon.moveItems(itemsForAddChecked);
				}
			} else {
				log.log(Level.INFO, "Move flag is \"off\". Check it in 10 minutes.");
				timer.waitSeconds(10 * 60);
			}

		}
	}
}
