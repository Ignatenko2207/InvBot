package com.amazoninvestorclub.domain;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WaitTimer {
	
	private static Logger log = Logger.getLogger(WaitTimer.class.getName());
	
	public void waitAction(){
		int minValue = 150;
		int maxValue = 240;
		int seconds = minValue + (int)(Math.random() * ((maxValue - minValue) + 1));
		try {
			Thread.sleep(seconds*1000);
        }  catch (Exception e) {
        	e.printStackTrace();
        	log.log(Level.SEVERE, "Method waitAction. Thread didn't sleep.");
		}
	}
	
	public void waitGetAction(){
		int minValue = 90;
		int maxValue = 120;
		int seconds = minValue + (int)(Math.random() * ((maxValue - minValue) + 1));
		try {
			Thread.sleep(seconds*1000);
        }  catch (Exception e) {
        	e.printStackTrace();
        	log.log(Level.SEVERE, "Method waitGetAction. Thread didn't sleep.");
		}
	}
	
	public void waitSeconds(int seconds){
		try {
			Thread.sleep(seconds*1000);
        }  catch (Exception e) {
        	log.log(Level.SEVERE, "Method waitSeconds. Thread didn't sleep.");
			e.printStackTrace();
		}
	}

}
