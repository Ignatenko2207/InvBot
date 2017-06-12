package com.amazoninvestorclub.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazoninvestorclub.DAO.AccountDAO;
import com.amazoninvestorclub.domain.Account;

public class AccountsListBean {
	
	private static Logger log = Logger.getLogger(AccountsListBean.class.getName());
	public ArrayList<Account> accountsList = getAccountsList();
	
	public ArrayList<Account> getAccountsList() {
		try {
			accountsList = AccountDAO.getAccounts();
			Collections.sort(accountsList, new Comparator<Account>() {
				public int compare(Account A1, Account A2) {
					return A1.login.compareTo(A2.login);
				  	}
				});
		} catch (Exception e) {
			log.log(Level.WARNING, "Method getAccountsList. Accounts wasn't found");
			e.printStackTrace();
			return accountsList;
		}
		return accountsList;
	}
	
	public ArrayList<Account> getAccountsListByFilter(String filter) {
		try {
			accountsList = AccountDAO.getAccountsByFilter(filter);
			Collections.sort(accountsList, new Comparator<Account>() {
				public int compare(Account A1, Account A2) {
					
					return A1.login.compareTo(A2.login);
				  	}
				});
		} catch (Exception e) {
			log.log(Level.WARNING, "Method getAccountsListByFilter. Accounts wasn't found");
			e.printStackTrace();
			return accountsList;
		}
		return accountsList;
	}
		
}
