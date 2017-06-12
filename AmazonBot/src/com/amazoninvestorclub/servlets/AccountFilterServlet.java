package com.amazoninvestorclub.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
@WebServlet("/accountFilter")
public class AccountFilterServlet extends HttpServlet {
	
    public AccountFilterServlet() {
        super();
        
    }

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		HttpSession session = req.getSession();
    	if(req.getParameter("used")!=null){
    		session.setAttribute("used", "1");
    	} else{
    		session.setAttribute("used", "0");
    	}
    	if(req.getParameter("unused")!=null){
    		session.setAttribute("unused", "1");
    	} else{
    		session.setAttribute("unused", "0");
    	}
    	resp.sendRedirect("/AmazonBot/jsp/infoaccount.jsp");
    	    	
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
