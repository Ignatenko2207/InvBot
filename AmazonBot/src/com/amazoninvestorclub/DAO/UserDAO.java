package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.amazoninvestorclub.domain.User;

public class UserDAO {
	
	private static Logger log = Logger.getLogger(UserDAO.class.getName());
	
	public static ArrayList<User> getUsers() throws DAOException{
		
		ArrayList<User> users = new ArrayList<>();
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;
		
		String sql = "SELECT login, password FROM users";
		
		try{
			
			connection = ConnectionToDB.getConnectionToDB();
			if(connection == null){
				log.log(Level.SEVERE, "Method getUsers. Connection is established.");
				return users;
			}
			try {
				
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				while(rSet.next()){
					User user = new User();
					user.name=rSet.getString(1);
					user.password=rSet.getString(2);
					users.add(user);
				}
			}
			catch (SQLException e) {
				log.log(Level.WARNING, "Method getUsers. SQL request isn't correct.");
				return users;
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
				log.log(Level.SEVERE, "Method getUsers. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return users;
	}
	
	public static void create(String login, String password) throws DAOException{
		
		Connection connection = null;
		PreparedStatement statement = null;
		String sql = "INSERT INTO users (login, password) VALUES (?,?)";
		try{
			connection = ConnectionToDB.getConnectionToDB();
			if(connection == null){
				log.log(Level.SEVERE, "Method create. Connection is not established!");
				return;
			}
			try {
				statement = connection.prepareStatement(sql);
				statement.setString(1, login);
				statement.setString(2, password);
				statement.executeUpdate();
			}catch (SQLException e) {
				log.log(Level.WARNING, "Method create. SQL request isn't correct.");
				return;
			}
		}finally{
			try {
			    if(statement!=null){
			    	statement.close();
			    }
			    if(connection!=null){
			    	connection.close();
			    }
			}catch (SQLException e) {
				log.log(Level.SEVERE, "Method create. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
	}
	
	public static User getUser(String login, String password) throws DAOException{
		
		User user = new User();
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet rSet = null;
		
		String sql = "SELECT * FROM users";
		
		try{
			connection = ConnectionToDB.getConnectionToDB();
			if(connection == null){
				log.log(Level.SEVERE, "Method getUser. Connection is not established!");
				return user;
			}
			try {
				statement = connection.prepareStatement(sql);
				rSet = statement.executeQuery();
				while(rSet.next()){
					
					if(rSet.getString(2).equals(login) && rSet.getString(3).equals(password)){
						user.name = rSet.getString(2);
						user.password = rSet.getString(3);
					}  else{
						user.name = "missmatch";
					}
				}
			}
			catch (SQLException e) {
				log.log(Level.WARNING, "Method getUser. SQL request isn't correct.");
				return user;
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
				log.log(Level.SEVERE, "Method getUser. Finally block isn't correct. Connections could be unclose!!!");
			}
		}
		return user;
	}

	public static void editUser(String login, String password, String newLogin, String newPassword) throws DAOException{
		
		User userInDB = getUser(login, password);
		if (userInDB.name.equals("mismatch")||userInDB.password.equals("mismatch")) {
			log.log(Level.INFO, "Method editUser. User "+login+" or "+password+" doesn't match!");
			return;
		}
		
		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;
		
		String sql1 = "SELECT * "
				+ "FROM users "
				+ "WHERE login='"+login+"' AND password='"+password+"'";
		
		try{
			connection1 = ConnectionToDB.getConnectionToDB();
			if(connection1 == null){
				log.log(Level.SEVERE, "Method editUser. Connection1 to edit is not established!");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while(rSet1.next()){
					if(rSet1.getString(2).equals(login)){
						if(rSet1.getString(3).equals(password)){
							log.log(Level.INFO, "User "+login+" has found.");
							int userID = rSet1.getInt(1);

							String sql2 = "UPDATE users "
									+ "SET  login='"+newLogin+"', "
									+ "password='"+newPassword+"' "
									+ "WHERE id='"+userID+"'";
							
							connection2 = ConnectionToDB.getConnectionToDB();
							if(connection2 == null){
								log.log(Level.SEVERE, "Method editUser. Connection2 to edit is not established!");
								return;
							}
							statement2 = connection2.prepareStatement(sql2);
							statement2.executeUpdate();
						} else {
							log.log(Level.INFO, "Method editUser. Password of user "+login+" does not match.");
							return;
						}
					} else {
						log.log(Level.INFO, "Method editUser. User "+login+" does not exist.");
						return;
					}
				}
			}catch (SQLException e) {
				log.log(Level.WARNING, "Method editUser. SQL request isn't correct");
				return;
			}
		}finally{
			try {
				if(statement2!=null){
					statement1.close();
				}
			    if(connection2!=null){
			    	connection1.close();
			    }
				if(rSet1!=null){
					rSet1.close();
				}
			    if(statement1!=null){
			    	statement1.close();
			    }
			    if(connection1!=null){
			    	connection1.close();
			    }
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method editUser. Finally block isn't correct. Connections could be unclose!!!");
			}
		}		
	}
	
	public static void deleteUser(String login, String password) throws DAOException{
		
		User userInDB = getUser(login, password);
		if(userInDB.name!=null){
			if (userInDB.name.equals("missmatch")||userInDB.password.equals("missmatch")) {
				log.log(Level.INFO, "Method deleteUser. User "+login+" with password "+password+" doesn't match!");
				return;
			}
		}
		
		Connection connection1 = null;
		PreparedStatement statement1 = null;
		ResultSet rSet1 = null;
		Connection connection2 = null;
		PreparedStatement statement2 = null;
		
		String sql1 = "SELECT * FROM users WHERE login='"+login+"' AND password='"+password+"'";
		
		try{
			connection1 = ConnectionToDB.getConnectionToDB();
			if(connection1 == null){
				log.log(Level.SEVERE, "Method deleteUser. Connection1 is not established.");
				return;
			}
			try {
				statement1 = connection1.prepareStatement(sql1);
				rSet1 = statement1.executeQuery();
				while(rSet1.next()){
					if(rSet1.getString(2).equals(login) && rSet1.getString(3).equals(password)){
							int userID = rSet1.getInt(1);

							String sql2 = "DELETE FROM users WHERE id='"+userID+"'";
							
							connection2 = ConnectionToDB.getConnectionToDB();
							if(connection2 == null){
								log.log(Level.SEVERE, "Method deleteUser. Connection2 is not established.");
								return;
							}
							statement2 = connection2.prepareStatement(sql2);
							statement2.executeUpdate();
					} else {
						log.log(Level.INFO, "Method deleteUser. User "+login+" does not exist.");
						return;
					}
				}
			}catch (SQLException e) {
				log.log(Level.WARNING, "Method deleteUser. SQL request isn't correct.");
				return;
			}
		}finally{
			try {
				if(statement2!=null){
					statement1.close();
				}
			    if(connection2!=null){
			    	connection1.close();
			    }
				if(rSet1!=null){
					rSet1.close();
				}
			    if(statement1!=null){
			    	statement1.close();
			    }
			    if(connection1!=null){
			    	connection1.close();
			    }
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Method deleteUser. Finally block isn't correct. Connections could be unclose!!!");
			}
		}				
	}
}
