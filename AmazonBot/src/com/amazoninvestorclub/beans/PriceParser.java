package com.amazoninvestorclub.beans;

import java.text.DecimalFormat;

public class PriceParser {
	
	public String priceAsString;
	public int priceAsInteger;
	
	public String getPriceString(int price){
		String priceAsString = "$";
		double priceAsDouble = (double)price/100;
		priceAsString = new DecimalFormat("$###.##").format(priceAsDouble);
		return priceAsString;
	}
	
	public int getPriceInteger(String strPrice){
		String checkedPrice = "";
		boolean stingIsEmpty = true;
		char symbolsInPrice[] = strPrice.toCharArray();
		for(Character symbol: symbolsInPrice){
			if(Character.isDigit(symbol)){
				if(Character.getNumericValue(symbol)==0){
					if(stingIsEmpty){
						checkedPrice = ""; 
					} else{
						checkedPrice += symbol.toString(); 
						stingIsEmpty = false;
					}
				}else{
					checkedPrice += symbol.toString(); 
					stingIsEmpty = false;
				}
			}
		}
		if(checkedPrice.isEmpty()){
			return 0;
		}
		int priceAsInteger = 0;
		if(strPrice.contains(",")||strPrice.contains(".")){
			priceAsInteger = Integer.valueOf(checkedPrice);
		} else{
			priceAsInteger = Integer.valueOf(checkedPrice)*100;
		}
		return priceAsInteger;
	}
}
