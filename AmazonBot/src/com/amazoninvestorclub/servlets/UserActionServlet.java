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

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.UserDAO;
import com.amazoninvestorclub.domain.User;

@SuppressWarnings("serial")
@WebServlet("/actionUser")
public class UserActionServlet extends HttpServlet {
	 
	private static Logger log = Logger.getLogger(UserActionServlet.class.getName());
	
    public UserActionServlet() {
        super();
        
    }

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
    	req.setCharacterEncoding("UTF-8");
    	resp.setCharacterEncoding("UTF-8");
    	
    	String userName = (String) req.getParameter("userName");
    	String userPass = (String) req.getParameter("userPassword");
    	String action = (String) req.getParameter("action");
    	String actionBtn = (String) req.getParameter("actionBtn");
    	
    	if (action.equals("edit")) {
    		HttpSession session = req.getSession();
    		session.setAttribute("title", "Edit user : "+userName);
    		session.setAttribute("userName", userName);
    		session.setAttribute("userPassword", userPass);
    		session.setAttribute("btn", "Edit user");
    		RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/userActionClient.jsp");
    		dispatcher.forward(req, resp);
		} else
		if (action.equals("delete")) {
			try {
				UserDAO.deleteUser(userName, userPass);
				resp.sendRedirect("/AmazonBot/jsp/infouser.jsp");
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> delete. User wasn't deleted.");
				e.printStackTrace();
			}
		} else 
		if (action.equals("create")){
	    	HttpSession session = req.getSession();
	    	session.setAttribute("title", "Create user");
	    	session.setAttribute("userName", "");
	    	session.setAttribute("userPassword", "");
	    	session.setAttribute("btn", "Create user");
	    	RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/userActionClient.jsp");
	    	dispatcher.forward(req, resp);
		} else
		if (action.equals("add")&&actionBtn.equals("Create user")){
			try {
				boolean userExistsInDB = checkUserInDB(userName, userPass);				
				if(userExistsInDB){
					req.setAttribute("userPassword", userName);
					req.setAttribute("userPassword", userPass);
					RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/existingUserActionClient.jsp");
			    	dispatcher.forward(req, resp);
				} else{
					UserDAO.create(userName, userPass);
					resp.sendRedirect("/AmazonBot/jsp/infouser.jsp");
				}
				
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> add+Create user. User wasn't created.");
				e.printStackTrace();
			}
		}
    	if (action.equals("add")&&actionBtn.equals("Edit user")){
    		String oldName = (String) req.getParameter("oldName");
        	String oldPass = (String) req.getParameter("oldPass");
    		try {
    			boolean userExistsInDB = checkUserInDB(userName, userPass);				
				if(userExistsInDB){
					req.setAttribute("userName", userName);
					req.setAttribute("userPassword", userPass);
					RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/existingUserActionClient.jsp");
					dispatcher.forward(req, resp);
				} else{
					UserDAO.editUser(oldName, oldPass, userName, userPass);
					resp.sendRedirect("/AmazonBot/jsp/infouser.jsp");
				}
			} catch (DAOException e) {
				log.log(Level.WARNING, "Method doGet -> add+Create user. User wasn't edited.");
				e.printStackTrace();
			}
		}
    	
	}

    private boolean checkUserInDB(String userName, String userPass) {
    	boolean userExists = false;
    	try {
			User userInDB = UserDAO.getUser(userName, userPass);
			if (userInDB.name.equals(userName)&&userInDB.password.equals(userPass)) {
				userExists=true;
			}
		} catch (DAOException e) {
			log.log(Level.WARNING, "Method checkUserInDB. User wasn't found.");
			e.printStackTrace();
		}
    	return userExists;
	}

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException  {

    	doGet(req, resp);
    }
}
