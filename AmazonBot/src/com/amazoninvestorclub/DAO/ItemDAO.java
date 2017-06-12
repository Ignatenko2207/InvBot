package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.amazoninvestorclub.domain.Item;

public class ItemDAO {

	private static Logger log = Logger.getLogger(ItemDAO.class.getName());

	public static ArrayList<Item> getItems() throws DAOException {

		ArrayList<Item> items = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM items";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getItems. Connection is not established!");
				return items;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();

				while (rSet.next()) {
					Item item = new Item();
					item.name = rSet.getString(2);
					item.asin = rSet.getString(3);
					item.imgSource = rSet.getString(4);
					item.keyWord = rSet.getString(5);
					item.group = rSet.getString(6);
					item.maxInCart = rSet.getInt(7);
					item.nowInCart = rSet.getInt(8);
					item.position = rSet.getInt(9);
					item.page = rSet.getInt(10);
					item.sellDate = rSet.getLong(11);
					item.lastAdding = rSet.getLong(12);
					item.price = rSet.getInt(13);
					item.ranking = rSet.getInt(14);
					item.move = rSet.getString(15);
					items.add(item);
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getItems. SQL request isn't correct.");
				return items;
			}
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method getItems. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return items;
	}

	public static ArrayList<Item> getItemsForMoove() throws DAOException {
		ArrayList<Item> checkedItems = new ArrayList<>();
		ArrayList<Item> items = new ArrayList<>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;
		String sql = "SELECT * FROM items WHERE move='up'";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getItemsForMoove. Connection is not established!");
				return items;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				while (rSet.next()) {
					Item item = new Item();
					item.name = rSet.getString(2);
					item.asin = rSet.getString(3);
					item.imgSource = rSet.getString(4);
					item.keyWord = rSet.getString(5);
					item.group = rSet.getString(6);
					item.maxInCart = rSet.getInt(7);
					item.nowInCart = rSet.getInt(8);
					item.position = rSet.getInt(9);
					item.page = rSet.getInt(10);
					item.sellDate = rSet.getLong(11);
					item.lastAdding = rSet.getLong(12);
					item.price = rSet.getInt(13);
					item.ranking = rSet.getInt(14);
					item.move = rSet.getString(15);
					items.add(item);
				}
				

				
				for (Item currentItem : items) {
					if (currentItem.lastAdding == 0) {
						currentItem.sellDate = System.currentTimeMillis();
						editItem(currentItem.name, currentItem.asin, currentItem.name, currentItem.asin,
								currentItem.imgSource, currentItem.keyWord, currentItem.group, currentItem.maxInCart,
								currentItem.nowInCart, currentItem.position, currentItem.page, currentItem.move,
								currentItem.sellDate, 0, currentItem.price, currentItem.ranking);
					}
					checkedItems.add(currentItem);
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getItemsForMoove. SQL request isn't correct.");
				return items;
			}
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method getItemsForMoove. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return checkedItems;
	}

	public static synchronized void create(String name, String asin, int price, String imgSource, String keyWord, String group,
			long sellDate, int ranking, int maxInCart, int nowInCart, long lastAdding, int position, int page,
			String move) throws DAOException {

		Item itemInDB = getItemByAsin(name, asin);
		if (itemInDB.name != null) {
			if (itemInDB.name.equals(name) && itemInDB.asin.equals(asin)) {
				log.log(Level.INFO, "Method create. Item " + name + " wasn't created! This item already exists!");
				return;
			}
		}

		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "INSERT INTO items (name, asin, img_source, key_word, item_group, max_in_cart, now_in_cart, "
				+ "position, page, sell_date, last_adding, price, ranking, move) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method create. Connection is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1, name);
				statement.setString(2, asin);
				statement.setString(3, imgSource);
				statement.setString(4, keyWord);
				statement.setString(5, group);
				statement.setInt(6, maxInCart);
				statement.setInt(7, nowInCart);
				statement.setInt(8, position);
				statement.setInt(9, page);
				statement.setLong(10, sellDate);
				statement.setLong(11, lastAdding);
				statement.setInt(12, price);
				statement.setInt(13, ranking);
				statement.setString(14, move);
				statement.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method create. SQL request isn't correct.");
				return;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method create. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static synchronized Item getItemByAsin(String name, String asin) throws DAOException {
		Item item = new Item();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM items";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getItemByAsin. Connection is not established!");
				return item;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();

				if (rSet.wasNull()) {
					item.name = "missmatch";
					item.asin = "missmatch";
				} else {
					while (rSet.next()) {
						if (rSet.getString(3) == null) {
							item.name = "missmatch";
							item.asin = "missmatch";
							return item;
						}

						if (rSet.getString(3).equals(asin)) {
							item.name = rSet.getString(2);
							item.asin = rSet.getString(3);
							item.imgSource = rSet.getString(4);
							item.keyWord = rSet.getString(5);
							item.group = rSet.getString(6);
							item.maxInCart = rSet.getInt(7);
							item.nowInCart = rSet.getInt(8);
							item.position = rSet.getInt(9);
							item.page = rSet.getInt(10);
							item.sellDate = rSet.getLong(11);
							item.lastAdding = rSet.getLong(12);
							item.price = rSet.getInt(13);
							item.ranking = rSet.getInt(14);
							item.move = rSet.getString(15);
							return item;
						} else {
							item.name = "missmatch";
							item.asin = "missmatch";
						}
					}
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getItemByAsin. SQL request isn't correct.");
				return item;
			}
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method getItemByAsin. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return item;
	}

	public static synchronized void setAddingToCart(String name, String asin) throws DAOException {
		Item itemInDB = getItemByAsin(name, asin);
		if (itemInDB.name == null) {
			log.log(Level.INFO,
					"Method setAddingToCart. Item " + name + " whith asin " + asin + " doesn't exist in DB");
			return;
		}

		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "UPDATE items SET now_in_cart='" + (itemInDB.nowInCart + 1) + "' WHERE asin='" + asin + "'";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method setAddingToCart. Connection is not established!");
				return;
			}

			try {
				statement = connection.prepareStatement(sql);
				if (itemInDB.asin.equals(asin)) {
					statement.executeUpdate();
				} else {
					log.log(Level.INFO, "Method setAddingToCart. Asin does not match.");
					return;
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method setAddingToCart. SQL request isn't correct");
				return;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method setAddingToCart. Finally block isn't correct. Connections could be unclose!!!");
			}
		}

	}

	public static synchronized void editItem(String name, String asin, String newName, String newAsin, String newImgSource,
			String newKeyWord, String newGroup, int newMaxInCart, int newNowInCart, int newPosition, int newPage,
			String newMove, long newSellDate, long newLastAdding, int newPrice, int newRanking) throws DAOException {

		Item itemInDB = getItemByAsin(name, asin);
		if (itemInDB.name == null) {
			log.log(Level.INFO, "Method editItem. Item " + name + " whith asin " + asin + " doesn't exist in DB");
			return;
		}

		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "UPDATE items " + "SET  name='" + newName + "', " + "asin='" + newAsin + "', " + "img_source='"
				+ newImgSource + "', " + "key_word='" + newKeyWord + "', " + "item_group='" + newGroup + "', "
				+ "max_in_cart='" + newMaxInCart + "', " + "now_in_cart='" + newNowInCart + "', " + "position='"
				+ newPosition + "', " + "page='" + newPage + "', " + "move='" + newMove + "', " + "sell_date='"
				+ newSellDate + "', " + "last_adding='" + newLastAdding + "', " + "price='" + newPrice + "', "
				+ "ranking='" + newRanking + "' " + "WHERE asin='" + asin + "'";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method editItem. Connection is not established!");
				return;
			}

			try {
				statement = connection.prepareStatement(sql);
				if (itemInDB.asin.equals(asin)) {
					statement.executeUpdate();
				} else {
					log.log(Level.INFO, "Method editItem. Asin does not match.");
					return;
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method editItem. SQL request isn't correct");
				return;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method editItem. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static synchronized void setStatData(String name, String asin, int newPosition, int newPage, int newRanking)
			throws DAOException {

		Item itemInDB = getItemByAsin(name, asin);
		if (itemInDB.name == null) {
			log.log(Level.INFO, "Method setStatData. Item " + name + " whith asin " + asin + " doesn't exist in DB");
			return;
		}

		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "UPDATE items " + "SET  position='" + newPosition + "', " + "page='" + newPage + "', "
				+ "ranking='" + newRanking + "' " + "WHERE asin='" + asin + "'";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method setStatData. Connection is not established!");
				return;
			}

			try {
				statement = connection.prepareStatement(sql);
				if (itemInDB.asin.equals(asin)) {
					statement.executeUpdate();
				} else {
					log.log(Level.INFO, "Method setStatData. Asin does not match.");
					return;
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method setStatData. SQL request isn't correct");
				return;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method setStatData. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static synchronized void deleteItem(String name, String asin) throws DAOException {

		Item itemInDB = getItemByAsin(name, asin);

		if (itemInDB.name == null) {
			log.log(Level.INFO, "Method deleteItem. Item " + name + " whith asin " + asin + " doesn't exist in DB");
			return;
		}

		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "DELETE FROM items WHERE asin='" + itemInDB.asin + "'";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method deleteItem. Connection to get is not established!");
				return;
			}

			try {
				statement = connection.prepareStatement(sql);
				if (itemInDB.asin.equals(asin)) {
					statement.executeUpdate();
				} else {
					log.log(Level.INFO, "Method deleteItem. Asin in DB does not match with " + asin + ".");
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method deleteItem. SQL request isn't correct.");
				return;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method deleteItem. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static synchronized void changeMoveFlag(String name, String asin, String flag) throws DAOException {
		Item itemInDB = getItemByAsin(name, asin);

		if (itemInDB.name == null) {
			log.log(Level.INFO, "Method changeMoveFlag. Item " + name + " whith asin " + asin + " doesn't exist in DB");
			return;
		}
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "UPDATE items " + "SET  move='" + flag + "' " + "WHERE asin='" + asin + "'";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method changeMoveFlag. Connection to get is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method changeMoveFlag. SQL request isn't correct.");
				return;
			}
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method changeMoveFlag. Finally block isn't correct. Connections could be unclose!!!");
			}
		}

	}
}
