package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.amazoninvestorclub.domain.Account;

public class AccountDAO {

	private static Logger log = Logger.getLogger(AccountDAO.class.getName());

	public static ArrayList<Account> getAccounts() throws DAOException {

		ArrayList<Account> accounts = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT login, password, used FROM accounts";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getAccounts. Connection to DB is not established.");
				return accounts;
			}
			try {

				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					log.log(Level.INFO, "Method getAccounts. ResultSet was NULL. Accounts list is empty.");
					return accounts;
				} else {
					while (rSet.next()) {
						Account account = new Account();
						account.setLogin(rSet.getString(1));
						account.setPassword(rSet.getString(2));
						account.setUsed(rSet.getInt(3));

						accounts.add(account);
					}
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getAccounts. SQL request isn't correct. Accounts list is empty.");
				return accounts;
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
						"Method getAccounts. Finally block isn't correct. Accounts list is empty. Connections could be unclose!!!");
			}
		}
		return accounts;
	}

	public static ArrayList<Account> getAccountsForCartAdd() throws DAOException {
		ArrayList<Account> accounts = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT login, password, used FROM accounts WHERE used = '0'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getAccountsForCartAdd. Connection is not established.");
				return accounts;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					log.log(Level.INFO, "Method getAccountsForCartAdd. ResultSet was NULL. Accounts list is empty.");
					return accounts;
				} else {
					while (rSet.next()) {
						Account account = new Account();
						account.setLogin(rSet.getString(1));
						account.setPassword(rSet.getString(2));
						account.setUsed(rSet.getInt(3));
						accounts.add(account);
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getAccountsForCartAdd. SQL request isn't correct.");
				return accounts;
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
						"Method getAccountsForCartAdd. Finally block isn't correct. Accounts list is empty. Connections could be unclose!!!");
			}
		}
		return accounts;
	}

	public static ArrayList<Account> getAccountsForWLAdd() throws DAOException {
		ArrayList<Account> accounts = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT login, password, used FROM accounts WHERE used > '0' AND used < '3'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getAccountsForWLAdd. Connection is not established.");
				return accounts;
			}
			try {

				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					log.log(Level.INFO, "Method getAccountsForWLAdd. ResultSet was NULL. Accounts list is empty.");
					return accounts;
				} else {
					while (rSet.next()) {
						Account account = new Account();
						account.setLogin(rSet.getString(1));
						account.setPassword(rSet.getString(2));
						account.setUsed(rSet.getInt(3));
						accounts.add(account);
					}
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getAccountsForWLAdd. SQL request isn't correct.");
				return accounts;
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
						"Method getAccountsForWLAdd. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return accounts;
	}

	public static ArrayList<Account> getAccountsByFilter(String filter) throws DAOException {
		ArrayList<Account> accounts = new ArrayList<>();

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "";
		if (filter.equals("used")) {
			sql = "SELECT login, password, used FROM accounts WHERE used != '0'";
		}
		if (filter.equals("unused")) {
			sql = "SELECT login, password, used FROM accounts WHERE used = '0'";
		}

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getAccountsByFilter. Connection is not established.");
				return accounts;
			}
			try {

				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					log.log(Level.INFO, "Method getAccountsByFilter. ResultSet was NULL. Accounts list is empty.");
				} else {
					while (rSet.next()) {
						Account account = new Account();
						account.setLogin(rSet.getString(1));
						account.setPassword(rSet.getString(2));
						account.setUsed(rSet.getInt(3));
						accounts.add(account);
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getAccountsByFilter. SQL request isn't correct.");
				return accounts;
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
						"Method getAccountsByFilter. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return accounts;
	}

	public static void create(String login, String password, int used) throws DAOException {
		Account accountInDB = getAccount(login, password);
		if (accountInDB.login != null) {
			if (accountInDB.login.equals(login) && accountInDB.password.equals(password)) {
				log.log(Level.INFO,
						"Method create. Account " + login + " wasn't created! This account already exists!");
				return;
			}
		}
		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "INSERT INTO accounts (login, password, used) VALUES (?,?,?)";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method create. Connection is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1, login);
				statement.setString(2, password);
				statement.setInt(3, used);
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

	public static Account getAccount(String login, String password) throws DAOException {

		Account accountInDB = new Account();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM accounts";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getAccount. Connection to get is not established!");
				return accountInDB;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					accountInDB.login = "missmatch";
					accountInDB.password = "missmatch";
				} else {
					while (rSet.next()) {
						if (rSet.getString(2).equals(login) && rSet.getString(3).equals(password)) {
							accountInDB.login = rSet.getString(2);
							accountInDB.password = rSet.getString(3);
							accountInDB.used = rSet.getInt(4);
						}
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getAccount. SQL request isn't correct.");
				return accountInDB;
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
						"Method getAccount. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return accountInDB;
	}

	public static void editAccount(String login, String password, String newLogin, String newPassword, int newUsed)
			throws DAOException {

		Account accountInDB = getAccount(login, password);
		if (accountInDB.login.equals("mismatch") || accountInDB.password.equals("mismatch")) {
			log.log(Level.INFO, "Account " + login + " or " + password + " doesn't match!");
			return;
		}

		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;

		String sql1 = "SELECT * " + "FROM accounts " + "WHERE login='" + login + "' AND password='" + password + "'";

		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method editAccount. Connection is not established.");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while (rSet1.next()) {
					if (rSet1.getString(2).equals(login) && rSet1.getString(3).equals(password)) {
						int accountID = rSet1.getInt(1);

						String sql2 = "UPDATE accounts " + "SET  login='" + newLogin + "', " + "password='"
								+ newPassword + "', " + "used='" + newUsed + "' " + "WHERE id='" + accountID + "'";

						connection2 = ConnectionToDB.getConnectionToDB();
						if (connection2 == null) {
							log.log(Level.SEVERE, "Method editAccount. Connection for update is established.");
							return;
						}
						statement2 = connection2.prepareStatement(sql2);
						statement2.executeUpdate();

					}
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method editAccount. SQL request isn't correct.");
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
				log.log(Level.WARNING,
						"Method editAccount. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

	public static void deleteAccount(String login, String password) throws DAOException {

		Account accountInDB = getAccount(login, password);
		if (accountInDB != null) {
			if (accountInDB.login.equals("mismatch") || accountInDB.password.equals("mismatch")) {
				log.log(Level.INFO, "Account " + login + " with password " + password + " doesn't match!");
				return;
			}
		}
		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;

		String sql1 = "SELECT * " + "FROM accounts " + "WHERE login='" + login + "' AND password='" + password + "'";

		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method deleteAccount. Connection1 is not established.");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while (rSet1.next()) {
					if (rSet1.getString(2).equals(login)) {
						if (rSet1.getString(3).equals(password)) {
							int accountID = rSet1.getInt(1);

							String sql2 = "DELETE FROM accounts WHERE id='" + accountID + "'";

							connection2 = ConnectionToDB.getConnectionToDB();
							if (connection2 == null) {
								log.log(Level.SEVERE, "Method deleteAccount. Connection2 is not established.");
								return;
							}
							statement2 = connection2.prepareStatement(sql2);
							statement2.executeUpdate();
						}
					}
				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method deleteAccount. SQL request isn't correct.");
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
				log.log(Level.WARNING,
						"Method deleteAccount. SQL request isn't correct. Connections could be unclose!!!");
			}
		}
	}
}
