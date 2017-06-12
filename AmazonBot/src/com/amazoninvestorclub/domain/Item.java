package com.amazoninvestorclub.domain;

public class Item {

	public String name;
	public String asin;
	public int price;
	public String imgSource;
	public String keyWord;
	public String group;
	public long sellDate; //time in millis
	public int ranking;
	public int maxInCart;
	public int nowInCart;
	public long lastAdding; //time in millis
	public int position;
	public int page;
	public String move;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAsin() {
		return asin;
	}
	public void setAsin(String asin) {
		this.asin = asin;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getImgSource() {
		return imgSource;
	}
	public void setImgSource(String imgSource) {
		this.imgSource = imgSource;
	}
	public String getKeyWord() {
		return keyWord;
	}
	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public long getSellDate() {
		return sellDate;
	}
	public void setSellDate(long sellDate) {
		this.sellDate = sellDate;
	}
	public int getRanking() {
		return ranking;
	}
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	public int getMaxInCart() {
		return maxInCart;
	}
	public void setMaxInCart(int maxInCart) {
		this.maxInCart = maxInCart;
	}
	public int getNowInCart() {
		return nowInCart;
	}
	public void setNowInCart(int nowInCart) {
		this.nowInCart = nowInCart;
	}
	public long getLastAdding() {
		return lastAdding;
	}
	public void setLastAdding(long lastAdding) {
		this.lastAdding = lastAdding;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public String getMove() {
		return move;
	}
	public void setMove(String move) {
		this.move = move;
	}	
}
