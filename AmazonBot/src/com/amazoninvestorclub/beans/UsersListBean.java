package com.amazoninvestorclub.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.UserDAO;
import com.amazoninvestorclub.domain.User;

public class UsersListBean {
	
	private static Logger log = Logger.getLogger(UsersListBean.class.getName());
	public ArrayList<User> usersList = getUsersList();
	
	public ArrayList<User> getUsersList() {
		try {
			usersList = UserDAO.getUsers();
			Collections.sort(usersList, new Comparator<User>() {
				public int compare(User U1, User U2) {
					return U1.name.compareTo(U2.name);
				  	}
				});
		} catch (DAOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getUsersList. Users wasn't found");
			return usersList;
		}
		return usersList;
	}
		
}
