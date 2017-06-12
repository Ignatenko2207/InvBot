package com.amazoninvestorclub.servlets;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.amazoninvestorclub.DAO.AccountDAO;
import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.beans.ActionAmazonBean;
//import com.amazoninvestorclub.beans.GetInfoItemAmazonBean;
import com.amazoninvestorclub.beans.MoveItemAmazonBean;
import com.amazoninvestorclub.domain.Account;
import com.amazoninvestorclub.domain.WaitTimer;



@SuppressWarnings("serial")
@WebServlet("/accountAction")
public class AccountActionServlet extends HttpServlet {
	
	private static Logger log = Logger.getLogger(AccountActionServlet.class.getName());
	MoveItemAmazonBean moveItem = new MoveItemAmazonBean();
    public AccountActionServlet() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
    	req.setCharacterEncoding("UTF-8");
    	resp.setCharacterEncoding("UTF-8");
    	
    	String accountName = (String) req.getParameter("accountName");
    	String accountPass = (String) req.getParameter("accountPassword");
    	String action = (String) req.getParameter("action");
    	String actionBtn = (String) req.getParameter("actionBtn");
    	String accountUsed = (String) req.getParameter("accountUsed");
    	
    	if(accountUsed==null || accountUsed.equals("0")){
    		accountUsed="0";
    	} else{
    		accountUsed = (String) req.getParameter("accountUsed");
    	}
    	int accUsed = Integer.valueOf(accountUsed);
    	
    	if (action.equals("edit")) {
    		HttpSession session = req.getSession();
    		session.setAttribute("title", "Edit account : "+accountName);
    		session.setAttribute("accountName", accountName);
    		session.setAttribute("accountPassword", accountPass);
    		session.setAttribute("btn", "Edit account");
    		session.setAttribute("accountUsed", accountUsed);
    		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/accountActionClient.jsp");
    		dispatcher.forward(req, resp);
		} else
		if (action.equals("delete")) {
			try {
				AccountDAO.deleteAccount(accountName, accountPass);
				resp.sendRedirect("/AmazonBot/jsp/infoaccount.jsp?used=1&unused=1");
			} catch (DAOException e) {
				e.printStackTrace();
				log.log(Level.WARNING, "Method doGet -> delete. Account wasn't deleted.");
			}
		} else 
		if (action.equals("create")){
	    	HttpSession session = req.getSession();
	    	session.setAttribute("title", "Create account");
	    	session.setAttribute("accountName", "");
	    	session.setAttribute("accountPassword", "");
	    	session.setAttribute("btn", "Create account");
	    	session.setAttribute("accountUsed", accountUsed);
	    	
	    	RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/accountActionClient.jsp");
	    	dispatcher.forward(req, resp);
		} else
		if (action.equals("add")&&actionBtn.equals("Create account")){
			try {
				boolean accountExistsInDB = checkAccountInDB(accountName, accountPass);				
				if(accountExistsInDB){
					req.setAttribute("accountName", accountName);
					req.setAttribute("accountPassword", accountPass);
					req.setAttribute("accountUsed", accountUsed);
					RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/redirectAccountActionClient.jsp");
			    	dispatcher.forward(req, resp);
				} else{
					AccountDAO.create(accountName, accountPass, accUsed);
					resp.sendRedirect("/AmazonBot/jsp/infoaccount.jsp?used=1&unused=1");
				}
				
			} catch (DAOException e) {
				e.printStackTrace();
				log.log(Level.WARNING, "Method doGet -> add + Create account. Account wasn't created.");
			}
		} else
			if (action.equals("add")&&actionBtn.equals("Save account")){
				try {
					boolean accountExistsInDB = checkAccountInDB(accountName, accountPass);				
					if(accountExistsInDB){
						req.setAttribute("accountName", accountName);
						req.setAttribute("accountPassword", accountPass);
						req.setAttribute("accountUsed", accountUsed);
						RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/redirectAccountActionClient.jsp");
				    	dispatcher.forward(req, resp);
					} else{
						AccountDAO.create(accountName, accountPass, accUsed);
						resp.sendRedirect("/AmazonBot/jsp/infoaccount.jsp?used=1&unused=1");
					}
					
				} catch (DAOException e) {
					log.log(Level.WARNING, "Method doGet -> add + Save account. Account wasn't saved.");
					e.printStackTrace();
				}
			} else
    	if (action.equals("add")&&actionBtn.equals("Edit account")){
    		String oldName = (String) req.getParameter("oldName");
        	String oldPass = (String) req.getParameter("oldPass");
        	
    		try {
    			boolean accountExistsInDB = checkAccountInDB(accountName, accountPass);				
				if(accountExistsInDB){
					req.setAttribute("accountName", accountName);
					req.setAttribute("accountPassword", accountPass);
					req.setAttribute("accountUsed", accountUsed);
					RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/redirectAccountActionClient.jsp");
					dispatcher.forward(req, resp);
				} else{
					AccountDAO.editAccount(oldName, oldPass, accountName, accountPass, accUsed);
					resp.sendRedirect("/AmazonBot/jsp/infoaccount.jsp?used=1&unused=1");
				}
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> add + Edit account. Account wasn't edited.");
				e.printStackTrace();
			}
		} else
		if(action.equals("createOnAmazon")){
			ActionAmazonBean newAcc = new ActionAmazonBean();
			
			log.log(Level.INFO, "Method doGet -> createOnAmazon. Try to create account. Other treads are interupted.");
			/*
			if(getInfoItem.isAlive()){
				try {
					getInfoItem.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			*/
			if(moveItem.isAlive()){
				try {
					moveItem.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			new WaitTimer().waitSeconds(5);
			Account account = newAcc.createNewAccount();
			HttpSession session = req.getSession();
	    	session.setAttribute("title", "Save account");
	    	session.setAttribute("btn", "Save account");
	    	session.setAttribute("accountUsed", accountUsed);
	    	session.setAttribute("accountName", account.login);
	    	session.setAttribute("accountPassword", account.password);
	    	/*
       		if(getInfoItem.isAlive()){
       			log.log(Level.INFO, "Method doGet -> createOnAmazon. GetInfo thread is continued");
				getInfoItem.notify();
			}
			*/
			if(moveItem.isAlive()){
				log.log(Level.INFO, "Method doGet -> createOnAmazon. MoveItem thread is continued");
				moveItem.notify();
			}
	    	RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/accountActionClient.jsp");
	    	dispatcher.forward(req, resp);
		}
	}

    private boolean checkAccountInDB(String accountName, String accountPass) {
    	boolean accountExists = false;
    	try {
			Account accountInDB = AccountDAO.getAccount(accountName, accountPass);
			if (accountInDB.login==null||accountInDB.equals("missmatch")) {
				accountExists=false;
				return accountExists;
			}
			if (accountInDB.login.equals(accountName)&&accountInDB.password.equals(accountPass)) {
				accountExists=true;
				return accountExists;
			}
		} catch (DAOException e) {
			log.log(Level.WARNING, "Method checkAccountInDB. Account wasn't found.");
			return accountExists;
		}
    	return accountExists;
	}

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException  {

    	doGet(req, resp);
    }

}
