package com.amazoninvestorclub.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.amazoninvestorclub.domain.Item;
import com.amazoninvestorclub.domain.WaitTimer;

public class PageAmazonParser {

	private static Logger log = Logger.getLogger(PageAmazonParser.class.getName());
	// Income link to parse it
	String incomeLink;

	// Item fields to get info from Amazon
	String itemName;
	String itemAsin;
	String itemPrice;
	String itemImg;
	String itemGroup;
	int itemPos;
	int itemPage;
	int itemRank;

	// Links to do some actions on Amazon
	String startLink = "https://www.amazon.com";
	String regAccountLink;
	String logedAccountLink;
	String loginLink;
	String actionLinkByAccount;
	String logoutLink;
	WaitTimer timer = new WaitTimer();
	int attemptToGetItem = 0;

	public Item getItemFromLink(String link) {
		Item itemFromLink = new Item();
		itemFromLink.name = "";
		itemFromLink.asin = "";
		itemFromLink.price = 0;
		itemFromLink.imgSource = "";
		itemFromLink.group = "";

		try {
			Document document = Jsoup.connect(link).get();
			timer.waitSeconds(10);
			if (document.toString().contains("Amazon.com, Inc. or its affiliates") == false) {
				attemptToGetItem++;
				if (attemptToGetItem < 10) {
					getItemFromLink(link);
				} else {
					return itemFromLink;
				}
			}

			// get item name
			if (document.toString().contains("productTitle")) {
				Element element = document.getElementById("productTitle");
				String nameLink = element.text();
				itemFromLink.name = nameLink;
			}

			// get item asin
			if (document.toString().contains("ASIN")) {
				Element element = document.getElementById("ASIN");
				String asinLink = element.attr("value");
				itemFromLink.asin = asinLink;
			}

			// get item price
			itemPrice = "";
			boolean priceNotFound = true;
			if (document.toString().contains("priceblock_saleprice")&&priceNotFound) {
				Element element = document.getElementById("priceblock_saleprice");
				String priceLink = element.text();
				if (priceLink.contains(",") || priceLink.contains(".")) {
					itemPrice = priceLink;
				} else{
					priceLink += "00";
					itemPrice = priceLink;
				}
				
				priceNotFound = false;
			}
			if (document.toString().contains("priceblock_ourprice")&&priceNotFound) {
				Element element = document.getElementById("priceblock_ourprice");
				String priceLink = element.text();
				if (priceLink.contains(",") || priceLink.contains(".")) {
					itemPrice = priceLink;
				} else{
					priceLink += "00";
					itemPrice = priceLink;
				}
				priceNotFound = false;
			}
			if (document.toString().contains("priceblock_dealprice")&&priceNotFound) {
				Element element = document.getElementById("priceblock_dealprice");
				String priceLink = element.text();
				if (priceLink.contains(",") || priceLink.contains(".")) {
					itemPrice = priceLink;
				} else{
					priceLink += "00";
					itemPrice = priceLink;
				}
				priceNotFound = false;
			}
			if (document.toString().contains("olp_feature_div")&&priceNotFound) {
				Element priceDiv =  document.getElementById("olp_feature_div");
				List<Element> aPriceElements = priceDiv.getElementsByTag("a");
				if (!aPriceElements.isEmpty()) {
					Element priseElement = aPriceElements.get(0);
					String priceDiscription = priseElement.text();
					char[] symbolsInPriceDisc = priceDiscription.toCharArray();
					boolean priceNotSet = true;
					String priceLink = "";
					for (char symbol : symbolsInPriceDisc) {

						if (!priceNotFound && priceNotSet) {
							if (!Character.isLetter(symbol)) {
								priceLink += String.valueOf(symbol);
							} else {
								priceNotSet = false;
							}
						}
						if (symbol == '$' && priceNotFound) {
							priceNotFound = false;
							priceLink = "$";
						}

					}
					if (priceLink.contains(",") || priceLink.contains(".")) {
						itemPrice = priceLink;
					} else{
						priceLink += "00";
						itemPrice = priceLink;
					}
				}
			}
			if (!itemPrice.equals("")) {
				itemFromLink.price = new PriceParser().getPriceInteger(itemPrice);
			} else {
				itemFromLink.price = 0;
			}

			// get item imgSource
			ArrayList<String> imgSources = new ArrayList<>();
			Elements liElements = document.getElementsByAttributeValueStarting("class", "a-spacing-small item");
			liElements.forEach(liElement -> {
				Elements imgElements = liElement.getElementsByTag("img");
				imgElements.forEach(imgElement -> {
					String imgSourse = imgElement.attr("src");
					imgSources.add(imgSourse);
				});
			});
			if (imgSources.isEmpty() == false) {
				itemFromLink.imgSource = imgSources.get(0);
			} else {
				itemFromLink.imgSource = "";
			}

			// get item group
			itemGroup = "";
			ArrayList<String> groups = new ArrayList<>();
			if (document.toString().contains("wayfinding-breadcrumbs_feature_div")) {
				Element element = document.getElementById("wayfinding-breadcrumbs_feature_div");
				Elements groupLinks = element.getElementsByAttributeValue("class", "a-link-normal a-color-tertiary");
				groupLinks.forEach(groupLink -> {
					groups.add(groupLink.text());
				});
				for (int i = 0; i < groups.size(); i++) {
					itemGroup += groups.get(i);
					if (i != (groups.size() - 1)) {
						itemGroup += " > ";
					}
				}
			} else {
				if (document.toString().contains("Back to results")) {
					Element element = document.getElementById("brand");
					String groupLink = element.text();
					itemGroup = groupLink;
				}
			}
			itemFromLink.group = itemGroup;

		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getNameByLink. Getting item page has Exception.");
			return itemFromLink;
		}

		return itemFromLink;

	}

	public String getRegAccountLink(String incomeURL) {
		regAccountLink = "";
		ArrayList<String> regAccountLinks = new ArrayList<>();
		try {
			Document document = Jsoup.connect(incomeURL).get();
			timer.waitSeconds(10);

			if (document.toString().contains("Hello. Sign in")) {
				if (document.toString().contains("gw-sign-in-bottom")) {
					Element divRegElement = document.getElementById("gw-sign-in-bottom");
					Elements aElements = divRegElement.getElementsByAttributeValue("class", "a-link-normal");
					aElements.forEach(aElement -> {
						String hrefAttr = aElement.attr("href");
						regAccountLinks.add(hrefAttr);
					});
				}
				if (regAccountLinks.isEmpty() == false) {
					regAccountLink = regAccountLinks.get(0);
				}
			} else {
				logoutLink = getLogoutLink("");
				if (logoutLink.equals("") == false) {
					try {
						document = Jsoup.connect(logoutLink).get();
						timer.waitSeconds(10);
						if (document.toString().contains("Create your Amazon account")) {
							Element aElement = document.getElementById("createAccountSubmit");
							regAccountLink = aElement.attr("href");
						}
						if (document.toString().contains("Hello. Sign in")) {
							if (document.toString().contains("gw-sign-in-bottom")) {
								Element divRegElement = document.getElementById("gw-sign-in-bottom");
								Elements aElements = divRegElement.getElementsByAttributeValue("class",
										"a-link-normal");
								aElements.forEach(aElement -> {
									String hrefAttr = aElement.attr("href");
									regAccountLinks.add(hrefAttr);
								});
							}
							if (regAccountLinks.isEmpty() == false) {
								regAccountLink = regAccountLinks.get(0);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.log(Level.WARNING, "Method getRegAccountLink. Getting logged-in page has Exception.");
						return regAccountLink;
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getRegAccountLink. Getting logged-out page has Exception.");
			return regAccountLink;
		}
		return regAccountLink;
	}

	public String getLogoutLink(String logedLink) {

		logoutLink = "";
		try {
			Document document = Jsoup.connect(logedAccountLink).get();
			timer.waitSeconds(10);
			if (document.toString().contains("Sign Out")) {
				Element divOutElement = document.getElementById("nav-tools");
				Elements aElements = divOutElement.getElementsByTag("a");
				if (aElements.isEmpty() == false) {
					aElements.forEach(aElement -> {
						if (aElement.text().contains("Sign Out")) {
							logoutLink = startLink + aElement.attr("href");
						}
					});
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getLogoutLink. Getting logged-in page has Exception.");
			return logoutLink;
		}
		return logoutLink;
	}

	// methods to get info about Item
	public String getNameByLink(String link) {
		itemName = "";
		try {
			Document document = Jsoup.connect(link).get();
			timer.waitSeconds(10);
			if (document.toString().contains("productTitle")) {
				Element element = document.getElementById("productTitle");
				String nameLink = element.text();
				itemName = nameLink;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getNameByLink. Getting item page has Exception.");
			return itemName;
		}
		return itemName;
	}

	public String getAsinByLink(String link) {
		itemAsin = "";
		try {
			Document document = Jsoup.connect(link).get();
			timer.waitSeconds(10);
			if (document.toString().contains("ASIN")) {
				Element element = document.getElementById("ASIN");
				String asinLink = element.attr("value");
				itemAsin = asinLink;
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Method getAsinByLink. Getting item page has Exception.");
			e.printStackTrace();
			return itemName;
		}
		return itemAsin;
	}

	public String getPriceByLink(String link) {
		itemPrice = "";
		try {
			Document document = Jsoup.connect(link).get();
			timer.waitSeconds(10);
			if (document.toString().contains("priceblock_ourprice")) {
				Element element = document.getElementById("priceblock_ourprice");
				String priceLink = element.text();
				itemPrice = priceLink;
			}
			if (document.toString().contains("priceblock_dealprice")) {
				Element element = document.getElementById("priceblock_dealprice");
				String priceLink = element.text();
				itemPrice = priceLink;
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getPriceByLink. Getting item page has Exception.");
			return itemPrice;
		}
		return itemPrice;
	}

	public String getImgByLink(String link) {
		ArrayList<String> imgSources = new ArrayList<>();
		itemImg = "";
		try {
			Document document = Jsoup.connect(link).get();
			timer.waitSeconds(10);
			Elements liElements = document.getElementsByAttributeValueStarting("class", "a-spacing-small item");
			liElements.forEach(liElement -> {
				Elements imgElements = liElement.getElementsByTag("img");
				imgElements.forEach(imgElement -> {
					String imgSourse = imgElement.attr("src");
					imgSources.add(imgSourse);
				});
			});
			if (imgSources.isEmpty() == false) {
				itemImg = imgSources.get(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getImgByLink. Getting item page has Exception.");
			return itemPrice;
		}
		return itemImg;
	}

	public String getGroupByLink(String link) {
		itemGroup = "";
		ArrayList<String> groups = new ArrayList<>();
		try {
			Document document = Jsoup.connect(link).get();
			timer.waitSeconds(10);
			if (document.toString().contains("wayfinding-breadcrumbs_feature_div")) {
				Element element = document.getElementById("wayfinding-breadcrumbs_feature_div");
				Elements groupLinks = element.getElementsByAttributeValue("class", "a-link-normal a-color-tertiary");
				groupLinks.forEach(groupLink -> {
					groups.add(groupLink.text());
				});

				for (int i = 0; i < groups.size(); i++) {
					itemGroup += groups.get(i);
					if (i != (groups.size() - 1)) {
						itemGroup += " > ";
					}
				}
			} else {
				if (document.toString().contains("Back to results")) {
					Element element = document.getElementById("brand");
					String groupLink = element.text();
					itemGroup = groupLink;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			log.log(Level.WARNING, "Method getGroupByLink. Getting item page has Exception.");
			return itemGroup;
		}
		return itemGroup;
	}

}
