package com.amazoninvestorclub.domain;

import java.text.DecimalFormat;

public class PriceParser {
	
	private String priceAsString;
	private int intPrice;
	
	public String getPrice(int price){
		String priceAsString = "";
		double priceAsDouble = (double)price/100;
		priceAsString = new DecimalFormat("$###.##").format(priceAsDouble);
		return priceAsString;
	}
	
	public int getIntPrice(String strPrice){
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
		int intPrice = 0;
		if(strPrice.contains(",")||strPrice.contains(".")){
			intPrice = Integer.valueOf(checkedPrice);
		} else{
			intPrice = Integer.valueOf(checkedPrice)*100;
		}
		return intPrice;
	}
	
	
}
