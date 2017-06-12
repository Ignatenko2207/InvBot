package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.domain.KeyWord;

public class KeyDAO {

	private static Logger log = Logger.getLogger(KeyDAO.class.getName());

	public static ArrayList<KeyWord> getKeys() throws DAOException {

		ArrayList<KeyWord> keys = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM keywords";

		try {

			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKeys. Connection is established.");
				return keys;
			}
			try {

				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				while (rSet.next()) {
					KeyWord key = new KeyWord();
					key.key = rSet.getString(1);
					key.addInDay = rSet.getInt(2);
					key.lastAdd = rSet.getLong(3);
					key.itemAsin = rSet.getString(4);
					key.itemLink = rSet.getString(5);
					keys.add(key);
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKeys. SQL request isn't correct.");
				return keys;
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
				log.log(Level.SEVERE, "Method getKeys. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return keys;
	}

	public static ArrayList<KeyWord> getKeysForItem(String itemAsin) throws DAOException {
		ArrayList<KeyWord> keys = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM keywords WHERE itemasin = '" + itemAsin + "'";

		try {

			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKeys. Connection is established.");
				return keys;
			}
			try {

				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				while (rSet.next()) {
					KeyWord key = new KeyWord();
					key.key = rSet.getString(1);
					key.addInDay = rSet.getInt(2);
					key.lastAdd = rSet.getLong(3);
					key.itemAsin = rSet.getString(4);
					key.itemLink = rSet.getString(5);
					keys.add(key);
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKeys. SQL request isn't correct.");
				return keys;
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
				log.log(Level.SEVERE, "Method getKeys. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return keys;
	}

	public static synchronized void create(String key, int addInDay, long lastAdd, String itemAsin, String itemLink)
			throws DAOException {

		KeyWord keyInDB = getKey(key, itemAsin);
		if (keyInDB != null) {
			if (!keyInDB.key.equals("missmatch")) {
				log.log(Level.INFO, "Method create. The same key exists in DB!");
				return;
			}

		}

		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "INSERT INTO keywords (key, dayadition, lastadd, itemasin, itemlink) VALUES (?,?,?,?,?)";
		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method create. Connection is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1, key);
				statement.setInt(2, addInDay);
				statement.setLong(3, lastAdd);
				statement.setString(4, itemAsin);
				statement.setString(5, itemLink);
				statement.executeUpdate();
				log.log(Level.INFO, "Method create. Key was created.");
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

	public static synchronized KeyWord getKey(String keyToFind, String itemAsin) throws DAOException {

		KeyWord key = new KeyWord();
		key.key = "missmatch";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM keywords";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKey. Connection is not established!");
				return key;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				while (rSet.next()) {

					if (rSet.getString(1).equals(keyToFind) && rSet.getString(4).equals(itemAsin)) {
						key.key = rSet.getString(1);
						key.addInDay = rSet.getInt(2);
						key.lastAdd = rSet.getLong(3);
						key.itemAsin = rSet.getString(4);
						key.itemLink = rSet.getString(5);

					}
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKey. SQL request isn't correct.");
				return key;
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
				log.log(Level.SEVERE, "Method getKey. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return key;
	}

	public static synchronized void editKey(String keyToEdit, String itemAsin, String newKey, int newAddInDay, long newLastAdd,
			String newItemLink) throws DAOException {

		KeyWord keyInDB = getKey(keyToEdit, itemAsin);
		if (keyInDB.key.equals("mismatch")) {
			log.log(Level.INFO, "Method editKey. Key " + keyToEdit + " doesn't match!");
			return;
		}

		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;

		String sql1 = "SELECT * FROM keywords WHERE key='" + keyToEdit + "' AND itemasin='"+itemAsin+"'";

		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method editKey. Connection1 to edit is not established!");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while (rSet1.next()) {
					if (rSet1.getString(4).equals(itemAsin)) {
						log.log(Level.INFO, "Method editKey. Key " + keyToEdit + " has found.");
						int id = (rSet1.getInt("id"));

						String sql2 = "UPDATE keywords " + "SET  key='" + newKey + "', " + "lastadd = '" + newLastAdd
								+ "', " + "dayadition ='" + newAddInDay + "'," + "itemlink = '" + newItemLink + "' "
								+ "WHERE id='" + id + "'";

						connection2 = ConnectionToDB.getConnectionToDB();
						if (connection2 == null) {
							log.log(Level.SEVERE, "Method editKey. Connection2 to edit is not established!");
							return;
						}
						statement2 = connection2.prepareStatement(sql2);
						statement2.executeUpdate();
						log.log(Level.INFO, "Method editKey. key " + keyToEdit + " was edited.");
					} 
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method editUser. SQL request isn't correct");
				return;
			}
		} finally {
			try {
				if (statement2 != null) {
					statement1.close();
				}
				if (connection2 != null) {
					connection1.close();
				}
				if (rSet1 != null) {
					rSet1.close();
				}
				if (statement1 != null) {
					statement1.close();
				}
				if (connection1 != null) {
					connection1.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method editUser. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static synchronized void deleteKey(String key, String itemAsin) throws DAOException {

		KeyWord keyInDB = getKey(key, itemAsin);
		if (keyInDB.key.equals("mismatch")) {
			log.log(Level.INFO, "Method deleteKey. Key " + key + " doesn't match!");
			return;
		}

		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;

		String sql1 = "SELECT * FROM keywords WHERE key='" + key + "' AND itemasin='"+itemAsin+"'";

		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method deleteKey. Connection1 to edit is not established!");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while (rSet1.next()) {
					if (rSet1.getString(4).equals(itemAsin)) {
						log.log(Level.INFO, "Method deleteKey. Key " + key + " has found.");
						String sql2 = "DELETE FROM keywords WHERE key='" + key + "' AND itemasin='" + itemAsin + "'";
						connection2 = ConnectionToDB.getConnectionToDB();
						if (connection2 == null) {
							log.log(Level.SEVERE, "Method deleteKey. Connection2 to edit is not established!");
							return;
						}
						statement2 = connection2.prepareStatement(sql2);
						statement2.executeUpdate();
					} 
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method editUser. SQL request isn't correct");
				return;
			}
		} finally {
			try {
				if (statement2 != null) {
					statement1.close();
				}
				if (connection2 != null) {
					connection1.close();
				}
				if (rSet1 != null) {
					rSet1.close();
				}
				if (statement1 != null) {
					statement1.close();
				}
				if (connection1 != null) {
					connection1.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method deleteKey. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static void deleteKeys(String itemAsin) throws DAOException {

		Connection connection = null;
		Statement statement = null;

		String sql = "DELETE FROM keywords WHERE itemasin='" + itemAsin + "'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method deleteKey. Connection1 to edit is not established!");
				return;
			}
			statement = connection.prepareStatement(sql);
			statement.executeUpdate(sql);

		} catch (SQLException e) {
			log.log(Level.WARNING, "Method editUser. SQL request isn't correct");
			return;
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method deleteKey. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}
}
