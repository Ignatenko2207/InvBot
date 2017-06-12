package com.amazoninvestorclub.DAO;

import java.sql.Connection;
import java.sql.DriverManager;


public class ConnectionToDB {
	
	static final String DBURL= "jdbc:postgresql://localhost:5432/AmazonExpert";
	static final String DBUser = "postgres";
	static final String DBUserPassword= "248842";
		
	public static Connection getConnectionToDB() throws DAOException{
		Connection connection = null;
		try{
			connection = DriverManager.getConnection(DBURL, DBUser, DBUserPassword);
			if(connection!=null){
				return connection;
			}else{
				throw new DAOException("Connection to DB is not established!");
			}
		}catch(Exception e){
			throw new DAOException("Connection to DB is not established!");				
		}
	}
}