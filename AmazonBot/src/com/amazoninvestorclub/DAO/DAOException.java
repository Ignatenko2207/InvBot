package com.amazoninvestorclub.DAO;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class DAOException extends Exception{
	
	public DAOException(String exception){
		Logger log = Logger.getLogger(DAOException.class.getName());
		log.log(Level.SEVERE, exception);
	}
}
