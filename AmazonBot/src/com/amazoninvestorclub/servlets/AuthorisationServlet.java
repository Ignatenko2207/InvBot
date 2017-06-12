package com.amazoninvestorclub.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.UserDAO;
import com.amazoninvestorclub.beans.AccountCreator;
import com.amazoninvestorclub.beans.MoveItemAmazonBean;
import com.amazoninvestorclub.domain.User;

@SuppressWarnings("serial")
@WebServlet("/authorisation")
public class AuthorisationServlet extends HttpServlet{
	
	
	private static Logger log = Logger.getLogger(AuthorisationServlet.class.getName());
	MoveItemAmazonBean moveItem = new MoveItemAmazonBean();
	AccountCreator creator = new AccountCreator();
	
	
	public AuthorisationServlet() {
        super();
       
    }
	
	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
	
		super.doPost(req, resp);
    }
	
	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException  {

    	req.setCharacterEncoding("UTF-8");
    	resp.setCharacterEncoding("UTF-8");
    	
    	String userName = (String) req.getParameter("username");
    	String userPass = (String) req.getParameter("password");
    	
    	try {
    		Class.forName("org.postgresql.Driver");
    		ArrayList<User> usersList = UserDAO.getUsers();
    		boolean userFound = false;
    		for (User userInList : usersList) {
    			if(userInList.name.equals(userName) && userInList.password.equals(userPass)){
    				userFound = true;
    			}
			}
			
			if(userFound){
				if(moveItem.isAlive()==false){
					moveItem.start();
				}
				/*
				if(creator.isAlive()==false){
					creator.setDaemon(true);
					creator.start();
				}
				*/
				resp.sendRedirect("jsp/infouser.jsp");
			} else{
				resp.sendRedirect("wrongindex.jsp");
			}
		} catch (DAOException | ClassNotFoundException e1) {
			e1.printStackTrace();
			log.log(Level.SEVERE, "Authorisation has Exception. Redirect to Authorisation page to try again.");
			resp.sendRedirect("wrongindex.jsp");
		}
    }
}
