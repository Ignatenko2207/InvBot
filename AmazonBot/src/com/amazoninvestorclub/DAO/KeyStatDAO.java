package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.domain.KeyStat;

public class KeyStatDAO {

	private static Logger log = Logger.getLogger(KeyStatDAO.class.getName());

	public static void create(String key, long addTime, int page, int position, int addAmount, String filterUsed,
			String itemAsin) throws DAOException {

		KeyStat keyStat = getKeyStat(key, addTime, itemAsin);
		if (keyStat.key != null) {
			if (keyStat.key.equals(key) && keyStat.addTime == addTime) {
				log.log(Level.INFO, "Method create. Key stat data for key " + keyStat.key
						+ " wasn't created! This addTime already exists!");
				return;
			}
		}

		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "INSERT INTO key_stat (key, time_add, page, position, added, filter, asin) VALUES (?,?,?,?,?,?,?)";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method create. Connection is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1, key);
				statement.setLong(2, addTime);
				statement.setInt(3, page);
				statement.setInt(4, position);
				statement.setInt(5, addAmount);
				statement.setString(6, filterUsed);
				statement.setString(7, itemAsin);
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

	public static KeyStat getKeyStat(String key, long addTime, String itemAsin) throws DAOException {

		KeyStat keyStat = new KeyStat();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM key_stat WHERE key='" + key + "' AND asin ='" + itemAsin + "'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKeyStat. Connection to get is not established!");
				keyStat.key = "missmatch";
				return keyStat;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					keyStat.key = "";
				} else {
					while (rSet.next()) {
						if (rSet.getLong(2) == addTime) {
							keyStat = new KeyStat();
							keyStat.key = rSet.getString(1);
							keyStat.addTime = rSet.getLong(2);
							keyStat.page = rSet.getInt(3);
							keyStat.position = rSet.getInt(4);
							keyStat.addAmount = rSet.getInt(5);
							keyStat.filterUsed = rSet.getString(6);
							keyStat.itemAsin = rSet.getString(8);
						}
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKeyStat. SQL request isn't correct.");
				return keyStat;
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
						"Method getKeyStat. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return keyStat;
	}
	
	public static ArrayList<KeyStat> getStatDataByKey(String key, String itemAsin) throws DAOException {
		ArrayList<KeyStat> statData = new ArrayList<>();
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM key_stat WHERE key='" + key + "' AND asin ='" + itemAsin + "'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKeyStat. Connection to get is not established!");
				return statData;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					return statData;
				} else {
					while (rSet.next()) {
						KeyStat keyStat = new KeyStat();
						keyStat.key = rSet.getString(1);
						keyStat.addTime = rSet.getLong(2);
						keyStat.page = rSet.getInt(3);
						keyStat.position = rSet.getInt(4);
						keyStat.addAmount = rSet.getInt(5);
						keyStat.filterUsed = rSet.getString(6);
						keyStat.itemAsin = rSet.getString(8);
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKeyStat. SQL request isn't correct.");
				return statData;
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
						"Method getKeyStat. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return statData;
	}

	public static void addItemToCart(String key, long addTime, String itemAsin) throws DAOException {

		KeyStat keyStat = getKeyStat(key, addTime, itemAsin);
		if (keyStat.key != null) {
			if (!keyStat.key.equals(key) && keyStat.addTime != addTime) {
				log.log(Level.INFO, "Method addItemToCart. Key stat data for key " + keyStat.key
						+ " wasn't edited! This addTime doesn't exist!");
				return;
			}
		}
		
		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;
		
		String sql1 = "SELECT * FROM key_stat WHERE key='" + key + "' AND asin ='" + itemAsin + "'";
		
		

		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method addItemToCart. Connection to get is not established!");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet = statement1.executeQuery();
				if (rSet.wasNull()) {
					return;
				} else {
					while (rSet.next()) {
						if (rSet.getLong(2) == addTime) {
							int id = rSet.getInt(7);
							String sql2 = "UPDATE key_stat SET added='" + (keyStat.addAmount+1) + "' WHERE ks_id='" + id + "'";
							connection2 = ConnectionToDB.getConnectionToDB();
							statement2 = connection2.prepareStatement(sql2);
							statement2.executeUpdate();
						}
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method addItemToCart. SQL request isn't correct.");
				return;
			}
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (statement1 != null) {
					statement1.close();
				}
				if (statement2 != null) {
					statement2.close();
				}
				if (connection1 != null) {
					connection1.close();
				}
				if (connection2 != null) {
					connection2.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method addItemToCart. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static void editStatData(String key, String itemAsin, long addTime, int page, int position,
			String filter) throws DAOException {
	
		KeyStat keyStat = getKeyStat(key, addTime, itemAsin);
		if (keyStat.key != null) {
			if (!keyStat.key.equals(key) && keyStat.addTime != addTime) {
				log.log(Level.INFO, "Method addItemToCart. Key stat data for key " + keyStat.key
						+ " wasn't edited! This addTime doesn't exist!");
				return;
			}
		}
		
		
		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;
		
		String sql1 = "SELECT * FROM key_stat WHERE key='" + key + "' AND asin ='" + itemAsin + "'";
		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method addItemToCart. Connection to get is not established!");
				keyStat.key = "";
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet = statement1.executeQuery();
				if (rSet.wasNull()) {
					keyStat.key = "missmatch";
				} else {
					while (rSet.next()) {
						if (rSet.getLong(2) == addTime) {
							int id = rSet.getInt(7);
							String sql2 = "UPDATE key_stat SET "
									+ "page='" + page + "', "
									+ "position='" + position + "', "
									+ "filter='" + filter + "'"
									+ " WHERE ks_id='" + id + "'";
							connection2 = ConnectionToDB.getConnectionToDB();
							statement2 = connection2.prepareStatement(sql2);
							statement2.executeUpdate();
						}
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method addItemToCart. SQL request isn't correct.");
				return;
			}
		} finally {
			try {
				if (rSet != null) {
					rSet.close();
				}
				if (statement1 != null) {
					statement1.close();
				}
				if (statement2 != null) {
					statement2.close();
				}
				if (connection1 != null) {
					connection1.close();
				}
				if (connection2 != null) {
					connection2.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE,
						"Method addItemToCart. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static ArrayList<KeyStat> getAllStatDataForItem(String itemAsin) throws DAOException {
		ArrayList<KeyStat> statData = new ArrayList<>();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;
		String sql = "SELECT * FROM key_stat WHERE asin ='" + itemAsin + "'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKeyStat. Connection to get is not established!");
				return statData;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					return statData;
				} else {
					while (rSet.next()) {
						KeyStat keyStat = new KeyStat();
						keyStat = new KeyStat();
						keyStat.key = rSet.getString(1);
						keyStat.addTime = rSet.getLong(2);
						keyStat.page = rSet.getInt(3);
						keyStat.position = rSet.getInt(4);
						keyStat.addAmount = rSet.getInt(5);
						keyStat.filterUsed = rSet.getString(6);
						keyStat.itemAsin = rSet.getString(8);
						statData.add(keyStat);
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKeyStat. SQL request isn't correct.");
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
						"Method getKeyStat. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return statData;
		
	}
}
