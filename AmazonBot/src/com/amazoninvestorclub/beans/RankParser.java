package com.amazoninvestorclub.beans;

import java.text.DecimalFormat;

public class RankParser {

	public String rankAsString;
	public int rankAsInteger;
	
	public String getRankString(int rank){
		if(rank<1){
			return "0.0";
		}
		double rankAsDouble = (double) rank/10;
		String rankAsString = new DecimalFormat("###.#").format(rankAsDouble);
		return rankAsString;
	}
	public int getRankInteger(String rank){
		boolean stingIsEmpty = true;
		rankAsString = "";
		char symbolsInRank[] = rank.toCharArray();
		for(Character symbol: symbolsInRank){
			if(Character.isDigit(symbol)){
				if(Character.getNumericValue(symbol)==0){
					if(stingIsEmpty){
						rankAsString = ""; 
					} else{
						rankAsString += symbol.toString(); 
						stingIsEmpty = false;
					}
				}else{
					rankAsString += symbol.toString(); 
					stingIsEmpty = false;
				}
			}
		}
		if(rankAsString.isEmpty()){
			rankAsInteger = 0;
			return rankAsInteger;
		}
		if(rank.contains(".")||rank.contains(",")){
			rankAsInteger = Integer.valueOf(rankAsString);
		} else{
			rankAsInteger = Integer.valueOf(rankAsString);
			rankAsInteger*=10;
		}
		
		return rankAsInteger;
	}
		
}
