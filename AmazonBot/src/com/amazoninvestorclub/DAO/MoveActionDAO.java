package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoveActionDAO {

	private static Logger log = Logger.getLogger(MoveActionDAO.class.getName());
	
	public static void addAction(String action) throws DAOException{
		Connection connection = null;
		PreparedStatement statement = null;
		long actionTime = System.currentTimeMillis();
		
		String sql = "INSERT INTO move_btn (move_value, set_time) VALUES (?,?)";
		
		connection = ConnectionToDB.getConnectionToDB();
		if(connection == null){
			log.log(Level.SEVERE, "Method addAction. Connection is not established!");
			return;
		}
		try {
			statement = connection.prepareStatement(sql);
			statement.setString(1, action);
			statement.setLong(2, actionTime);
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
				log.log(Level.SEVERE, "Method addAction. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}
	
	public static String getLastAction() throws DAOException{
		
		String action = "unknown";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;
		
		String sql = "SELECT move_value FROM move_btn WHERE set_time=(SELECT MAX(set_time) FROM move_btn)";
		
		try{
			connection = ConnectionToDB.getConnectionToDB();
			if(connection == null){
				log.log(Level.SEVERE, "Method getLastAction. Connection to get is not established!");
				return action;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				if(rSet.wasNull()){
					action = "unknown";
				} else{
					while(rSet.next()){
						action = rSet.getString(1);
					}
				}
				
			}
			catch (SQLException e) {
				log.log(Level.WARNING, "Method getLastAction. SQL request isn't correct.");
				return action;
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
				log.log(Level.SEVERE, "Method getLastAction. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return action;
	}
}
