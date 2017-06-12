package com.amazoninvestorclub.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {
	
	public String dateAsString;
	public String dateAndTimeAsString;
	
	public String getDateAsString(long timeInMillis){
		if(timeInMillis==0){
			dateAsString = "--/--/----";
			return dateAsString;
		}
		Date date = new Date();
		date.setTime(timeInMillis);
		SimpleDateFormat formating = new SimpleDateFormat("dd/MM/YYYY");
		dateAsString = formating.format(date);		
		return dateAsString;
	}	
	
	public String getDateAndTimeAsString(long timeInMillis){
		if(timeInMillis==0){
			dateAndTimeAsString = "--/--/----";
			return dateAndTimeAsString;
		}
		Date date = new Date();
		date.setTime(timeInMillis);
		SimpleDateFormat formating = new SimpleDateFormat("HH:mm dd/MM/YYYY");
		dateAsString = formating.format(date);		
		return dateAsString;
	}	
}
