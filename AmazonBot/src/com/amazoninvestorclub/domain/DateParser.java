package com.amazoninvestorclub.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser {
	
	public String dateAsString;
	
	public String getDateAsString(long TimeInMillis){
		Date date = new Date();
		date.setTime(TimeInMillis);
		SimpleDateFormat formating = new SimpleDateFormat("YYYY/MM/DD");
		dateAsString = formating.format(date);		
		return dateAsString;
	}
}
