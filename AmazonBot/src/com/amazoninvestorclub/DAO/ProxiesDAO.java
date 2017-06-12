package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.amazoninvestorclub.domain.ProxyForUse;

public class ProxiesDAO {

	private static Logger log = Logger.getLogger(ProxiesDAO.class.getName());
	
	public static void create(String proxy, int port) throws DAOException {

		ProxyForUse proxyForUse = getProxyForUse(proxy, port);
		if (proxyForUse.getProxy() != null) {
			if (proxyForUse.getProxy().equals(proxy) && proxyForUse.getPort() == port) {
				log.log(Level.INFO,
						"Method create. Proxy " + proxyForUse.getProxy() + " wasn't created! This proxy already exists!");
				return;
			}
		}
		Connection connection = null;
		PreparedStatement statement = null;

		String sql = "INSERT INTO proxies (proxy, port) VALUES (?,?)";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method create. Connection is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1, proxy);
				statement.setInt(2, port);
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

	public static ProxyForUse getProxyForUse(String proxy, int port) throws DAOException {

		ProxyForUse proxyForUse = new ProxyForUse();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM proxies WHERE proxy='" + proxy + "'";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method getKeyStat. Connection to get is not established!");
				proxyForUse.setProxy("missmatch");
				return proxyForUse;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					proxyForUse.setProxy("missmatch");
				} else {
					while (rSet.next()) {
						if (rSet.getInt(2) == port) {
							proxyForUse.setProxy(rSet.getString(1));
							proxyForUse.setPort(rSet.getInt(2));
							proxyForUse.setId(rSet.getInt(3));
						}
					}
				}

			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getKeyStat. SQL request isn't correct.");
				return proxyForUse;
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
		return proxyForUse;
	}

	
	
	
	public ArrayList<ProxyForUse> getAllProxies() throws DAOException{
		
		ArrayList<ProxyForUse> allProxies = new ArrayList<>();
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;

		String sql = "SELECT * FROM proxies";

		try {
			try {
				connection = ConnectionToDB.getConnectionToDB();
			} catch (DAOException e1) {
				e1.printStackTrace();
			}
			if (connection == null) {
				log.log(Level.SEVERE, "Method getAllProxies. Connection to get is not established!");
				return allProxies;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if (rSet.wasNull()) {
					log.log(Level.INFO, "Method getAllProxies. There are NO proxies in DB.");
					return allProxies;
				} else {
					while (rSet.next()) {
						ProxyForUse proxyToPref = new ProxyForUse();
						proxyToPref.setProxy(rSet.getString(1));
						proxyToPref.setPort(rSet.getInt(2));
						
						allProxies.add(proxyToPref);
					}
				}
				
				log.log(Level.INFO, "Method getAllProxies. There are " + allProxies.size() + " proxies in DB.");
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method getAllProxies. SQL request isn't correct.");
				return allProxies;
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
		return allProxies;
	}

	public static void deleteProxy(String proxy, int port) throws DAOException {

		ProxyForUse proxyForUse = new ProxyForUse();
		if (proxyForUse.getProxy() != null) {
			if (proxyForUse.getProxy().equals("missmatch")) {
				log.log(Level.INFO, "Method deleteProxy. Proxy " + proxy + " doesn't exist!");
				return;
			}
		}

		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;

		String sql1 = "SELECT * FROM proxies WHERE proxy ='" + proxy + "'";

		try {
			connection1 = ConnectionToDB.getConnectionToDB();
			if (connection1 == null) {
				log.log(Level.SEVERE, "Method deleteProxy. Connection1 is not established.");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while (rSet1.next()) {
					if (rSet1.getInt(2) == port) {
						int proxyID = rSet1.getInt(3);
						String sql2 = "DELETE FROM proxies WHERE id='" + proxyID + "'";
						connection2 = ConnectionToDB.getConnectionToDB();
						if (connection2 == null) {
							log.log(Level.SEVERE, "Method deleteProxy. Connection2 is not established.");
							return;
						}
						statement2 = connection2.prepareStatement(sql2);
						statement2.executeUpdate();
					} else {
						log.log(Level.INFO, "Method deleteProxy. Proxy " + proxy + " does not match.");
						return;
					}

				}
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method deleteProxy. SQL request isn't correct.");
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
				log.log(Level.SEVERE,
						"Method deleteProxy. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}
	
	public static void deleteAllProxies() throws DAOException {

		Connection connection = null;
		PreparedStatement statement = null;
		
		String sql = "DELETE FROM proxies";

		try {
			connection = ConnectionToDB.getConnectionToDB();
			if (connection == null) {
				log.log(Level.SEVERE, "Method deleteAllProxies. Connection is not established.");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.executeUpdate();
			} catch (SQLException e) {
				log.log(Level.WARNING, "Method deleteAllProxies. SQL request isn't correct.");
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
						"Method deleteProxy. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}

}
