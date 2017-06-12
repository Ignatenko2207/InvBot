package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.amazoninvestorclub.domain.ItemAction;

public class ItemActionDAO {
	
	private static Logger log = Logger.getLogger(ItemActionDAO.class.getName());
	
	public static void addAction(String itemName, String itemAsin, String accName, String action) throws DAOException{
		Connection connection = null;
		PreparedStatement statement = null;
		long actionTime = System.currentTimeMillis();
		
		String sql = "INSERT INTO items_accounts (item_name, item_asin, account_name, action, action_time) VALUES (?,?,?,?,?)";
		
		connection = ConnectionToDB.getConnectionToDB();
		if(connection == null){
			log.log(Level.SEVERE, "Method addAction. Connection is not established!");
			return;
		}
		try {
			statement = connection.prepareStatement(sql);
			statement.setString(1, itemName);
			statement.setString(2, itemAsin);
			statement.setString(3, accName);
			statement.setString(4, action);
			statement.setLong(5, actionTime);
			statement.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.WARNING, "Method addAction. SQL request isn't correct.");
			return;
		} finally{
			try {
				if(statement!=null){
		    	statement.close();
				}
				if(connection!=null){
		    	connection.close();
				}
			}catch (SQLException e) {
			log.log(Level.SEVERE, "Method addAction.Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}
	
	public static ArrayList<ItemAction> getItemActionsByAsin (String itemAsin) throws DAOException{
		ArrayList<ItemAction> itemActions = new ArrayList<>();
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;
		
		String sql = "SELECT *"
				+ "FROM items_accounts "
				+ "WHERE item_asin='"+itemAsin+"'";
		try{
			connection = ConnectionToDB.getConnectionToDB();
			if(connection == null){
				log.log(Level.SEVERE, "Method getItemActionsByAsin. Connection is not established!");
				return itemActions;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				
				while(rSet.next()){
					ItemAction itemAction = new ItemAction();
					itemAction.itemName = rSet.getString(2);
					itemAction.itemAsin = rSet.getString(3);
					itemAction.accountName = rSet.getString(4);
					itemAction.action = rSet.getString(5);
					itemAction.actionTime = rSet.getLong(6);
					itemActions.add(itemAction);
				}
			}
			catch (SQLException e) {
				log.log(Level.WARNING, "Method getItemActionsByAsin. SQL request isn't correct. ");
				return itemActions;
			}
		}
		finally{
			try {
				if(rSet!=null){
					rSet.close();
				}
			    if(statement!=null){
			    	statement.close();
			    }
			    if(connection!=null){
			    	connection.close();
			    }
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method getItemActionsByAsin. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return itemActions;
	}
	
	public static void deleteActionsWithItem(String itemAsin) throws DAOException{
		Connection connection = null;
		PreparedStatement statement = null;
				
		String sql = "DELETE FROM items_accounts WHERE item_asin='"+itemAsin+"'";
		
		connection = ConnectionToDB.getConnectionToDB();
		if(connection != null){
			log.log(Level.SEVERE, "Method deleteActionsWithItem. Connection is not established!");
			return;
		}
		try {
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.WARNING, "Method deleteActionsWithItem. SQL request isn't correct.");
			return;
		}
		finally {
			try {
				if(statement!=null){
			    	statement.close();
			    }
			    if(connection!=null){
			    	connection.close();
			    }
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method deleteActionsWithItem. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}
}
