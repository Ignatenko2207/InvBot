package com.amazoninvestorclub.domain;

import java.text.DecimalFormat;

public class RankParser {

	public String rankAsString;
	
	public String getPrice(int rank){
		if(rank<1){
			return "0.0";
		}
		double rankAsDouble = (double) rank/10;
		String rankAsString = new DecimalFormat("###.#").format(rankAsDouble);
		return rankAsString;
	}
}
