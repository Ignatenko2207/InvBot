package com.amazoninvestorclub.domain;

public class ItemAction {
	public String itemName;
	public String itemAsin;
	public String accountName;
	public String action;
	public long actionTime;
	
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getItemAsin() {
		return itemAsin;
	}
	public void setItemAsin(String iatemAsin) {
		this.itemAsin = iatemAsin;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public long getActionTime() {
		return actionTime;
	}
	public void setActionTime(long actionTime) {
		this.actionTime = actionTime;
	}
	
	
}
