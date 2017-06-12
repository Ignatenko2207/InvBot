package com.amazoninvestorclub.moveThreads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.firefox.FirefoxProfile;
//import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import com.amazoninvestorclub.DAO.AccountDAO;
import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ItemDAO;
import com.amazoninvestorclub.DAO.KeyDAO;
import com.amazoninvestorclub.DAO.KeyStatDAO;
import com.amazoninvestorclub.DAO.ProxiesDAO;
import com.amazoninvestorclub.beans.RankParser;
import com.amazoninvestorclub.domain.Account;
import com.amazoninvestorclub.domain.Item;
import com.amazoninvestorclub.domain.KeyWord;
import com.amazoninvestorclub.domain.ProxyForUse;
import com.amazoninvestorclub.domain.WaitTimer;

import ru.stqa.selenium.factory.WebDriverFactory;

public class MoveItemByKey extends Thread {

	private static Logger log = Logger.getLogger(MoveItemByKey.class.getName());
	WaitTimer timer = new WaitTimer();
	private Item itemToMove;
	private KeyWord keyToMove;
	private String profile;

	public MoveItemByKey(Item item, KeyWord key, String profile) {
		this.itemToMove = item;
		this.keyToMove = key;
		this.profile = profile;
	}

	@Override
	public void run() {

		log.log(Level.INFO, "Try to find stat data for item " + itemToMove.asin + " by key " + keyToMove.key);

		boolean itemNotFound = true;
		itemNotFound = setStatDataByAllDeps(itemToMove.name, itemToMove.asin, keyToMove.key);
		if (itemNotFound) {
			itemNotFound = setStatDataByGroup(itemToMove.name, itemToMove.asin, itemToMove.group, keyToMove.key);
		}
		if (itemNotFound) {
			itemNotFound = setStatDataByFilter(itemToMove.name, itemToMove.asin, itemToMove.group, keyToMove.key);
		}
		if (itemNotFound) {
			log.log(Level.INFO, "Method setStatDataForKey. Item link with filters wasn't found in 3 attempts.");
			// set stat data if item not found (NF)
			KeyWord keyForNF = new KeyWord();
			String filter = "NOT FOUND";
			try {
				keyForNF = KeyDAO.getKey(keyToMove.key, itemToMove.asin);
				timer.waitSeconds(3);
				KeyDAO.editKey(keyForNF.key, itemToMove.asin, keyForNF.key, keyForNF.addInDay, keyForNF.lastAdd,
						keyForNF.itemLink);
				KeyStatDAO.editStatData(keyForNF.key, itemToMove.asin, keyForNF.lastAdd, 0, 0, filter);
			} catch (DAOException e) {
				e.printStackTrace();
			}

			return;
		}

		
		timer.waitSeconds(getRandomNumber(10, 30));
		for (int i = 0; i < 100; i++) {
			int pause = getRandomNumber(120, 300); // set pause between mooving
			
			KeyWord checkedKeyWordToMove = new KeyWord();
			try {
				checkedKeyWordToMove = KeyDAO.getKey(keyToMove.key, itemToMove.asin);
			} catch (DAOException e) {
				e.printStackTrace();
			}

			if (checkedKeyWordToMove.addInDay > 0) {
				log.log(Level.INFO, "Item " + itemToMove.asin + ". Try to move item.");
				// check link and try to add item
				if (linkIsCorrect(checkedKeyWordToMove.itemLink)) {
					Account account = getAccountToMove();
					try {
						AccountDAO.editAccount(account.login, account.password, account.login, account.password, 1);
					} catch (DAOException e2) {
						e2.printStackTrace();
					}

					log.log(Level.INFO, "--------------------------------\nItem link for item " + itemToMove.asin
							+ " with key-words " + checkedKeyWordToMove.key + " \n" + checkedKeyWordToMove.itemLink);

					log.log(Level.INFO, "Account " + account.login + " was choosen for move item " + itemToMove.asin
							+ " by key-words " + checkedKeyWordToMove.key);

					// add item to cart
					log.log(Level.INFO, "Item link for item " + itemToMove.asin + " with key-words "
							+ checkedKeyWordToMove.key + " was found. Try to move item.");
					timer.waitSeconds(5);

					boolean itemAdded = moveItemOnAmazon(checkedKeyWordToMove.key, checkedKeyWordToMove.itemLink, itemToMove.name,
							itemToMove.asin, account);

					if (itemAdded) {
						KeyWord addedKey = new KeyWord();
						try {
							KeyStatDAO.addItemToCart(checkedKeyWordToMove.key, checkedKeyWordToMove.lastAdd,
									itemToMove.asin);
							addedKey = KeyDAO.getKey(checkedKeyWordToMove.key, itemToMove.asin);
						} catch (DAOException e1) {
							e1.printStackTrace();
						}
						// set info about adding
						try {
							KeyDAO.editKey(addedKey.key, itemToMove.asin, addedKey.key, (addedKey.addInDay - 1),
									addedKey.lastAdd, addedKey.itemLink);
						} catch (DAOException e) {
							e.printStackTrace();
						}
					} else {
						i--;
					}

				}
				// wait some time between adding
				timer.waitSeconds(pause);
			}

		}

	}

	public boolean setStatDataByAllDeps(String itemName, String itemAsin, String keyWord) {
		boolean itemNotFound = true;
		boolean groupNotFound = true;

		String itemLink = "";
		String searchLink = "";
		WebDriver userDriver = getProxyChromeDriver();
		log.log(Level.INFO, "Method setStatDataByAllDeps. Try to get new search for item.");

		timer.waitSeconds(3);
		WebElement searchInput = userDriver.findElement(By.id("twotabsearchtextbox"));
		searchInput.sendKeys(keyWord);

		WebElement searchForm = userDriver.findElement(By.name("site-search"));
		timer.waitSeconds(getRandomNumber(4, 6));
		searchForm.submit();
		timer.waitSeconds(getRandomNumber(4, 6));
		searchLink = userDriver.getCurrentUrl();
		userDriver.get(searchLink);
		timer.waitSeconds(getRandomNumber(4, 6));

		// try to find group

		boolean refPresent;
		try {
			userDriver.findElement(By.id("refinements"));
			refPresent = true;
		} catch (NoSuchElementException e) {
			refPresent = false;
		}

		if (refPresent) {
			WebElement refinementsBlock = userDriver.findElement(By.id("refinements"));
			List<WebElement> liElements = refinementsBlock.findElements(By.tagName("li"));
			boolean searchLinkNotFound = true;
			for (int i = 0; i < liElements.size() && i < 5; i++) {
				WebElement liElement = liElements.get(i);
				// check for present element
				if (searchLinkNotFound) {
					boolean present;
					try {
						liElement.findElement(By.tagName("a"));
						present = true;
					} catch (NoSuchElementException e) {
						present = false;
						continue;
					}
					if (present) {
						WebElement aElement = liElement.findElement(By.tagName("a"));

						if (aElement.getText().contains("Any Category")) {
							searchLinkNotFound = false;
							groupNotFound = false;
							searchLink = aElement.getAttribute("href");
							if (!searchLink.contains("https://www.amazon.com")) {
								searchLink = "https://www.amazon.com" + searchLink;
							}
							userDriver.get(searchLink);
							timer.waitSeconds(3);
						}

					}
				}

			}
		}

		if (groupNotFound) {
			if (userDriver.getPageSource().contains("searchDropdownBox")) {

				boolean SSDPpresent;
				try {
					userDriver.findElement(By.id("searchDropdownBox"));
					SSDPpresent = true;
				} catch (NoSuchElementException e) {
					SSDPpresent = false;
				}

				if (SSDPpresent) {
					WebElement searchSelectList = userDriver.findElement(By.id("searchDropdownBox"));
					Select selectList = new Select(searchSelectList);
					selectList.selectByVisibleText("All Departments");
					groupNotFound = false;

				}
			}

			searchForm = userDriver.findElement(By.name("site-search"));
			timer.waitSeconds(3);
			searchForm.submit();
			timer.waitSeconds(3);
			searchLink = userDriver.getCurrentUrl();
			userDriver.get(searchLink);
			timer.waitSeconds(3);
		}

		searchLink = userDriver.getCurrentUrl();

		// find last page
		int lastPage = 0;

		if (userDriver.getPageSource().contains("id=\"pagn\"")) {
			if (userDriver.getPageSource().contains("pagnDisabled")) {
				WebElement pagesElements = userDriver.findElement(By.id("pagn"));
				WebElement maxPageElement = pagesElements.findElement(By.className("pagnDisabled"));
				String lastPageStr = maxPageElement.getText();
				lastPage = Integer.valueOf(lastPageStr);
				log.log(Level.INFO, "Method setStatDataByAllDeps. Last page was found. It is " + lastPage + " page.");
			} else {
				WebElement pagesElements = userDriver.findElement(By.id("pagn"));
				List<WebElement> pagesLinkElements = pagesElements.findElements(By.className("pagnLink"));
				if (!pagesLinkElements.isEmpty()) {
					WebElement lastPageElement = pagesLinkElements.get((pagesLinkElements.size() - 1));
					WebElement lastPageLink = lastPageElement.findElement(By.tagName("a"));
					String lastPageStr = lastPageLink.getText();
					lastPage = Integer.valueOf(lastPageStr);
				} else {
					lastPage = 1;
				}

				log.log(Level.INFO, "Method setStatDataByAllDeps. Last page was found. It is " + lastPage + " page.");

			}
		}

		if (lastPage == 0) {
			lastPage = 1;
		}
		int page = 0;
		int position = 0;
		while (itemNotFound) {

			if (!userDriver.getPageSource().contains("twotabsearchtextbox")) {
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(180, 300));

				userDriver = getProxyChromeDriver();
				userDriver.get(searchLink);
			}
			
			try {
				boolean searchItemListNotFound = true;
				String resultListID = "";
				
				try {
					userDriver.findElement(By.id("s-results-list-atf"));
					searchItemListNotFound = false;
					resultListID = "s-results-list-atf";
				} catch (NoSuchElementException e) {
					searchItemListNotFound = true;
					resultListID = "";
				}
				
				if(searchItemListNotFound){
					try {
						userDriver.findElement(By.id("mainResults"));
						searchItemListNotFound = false;
						resultListID = "mainResults";
					} catch (NoSuchElementException e) {
						searchItemListNotFound = true;
						resultListID = "";
					}				
				}
				
				if(searchItemListNotFound){
					try {
						userDriver.findElement(By.id("resultsCol"));
						searchItemListNotFound = false;
						resultListID = "resultsCol";
					} catch (NoSuchElementException e) {
						searchItemListNotFound = true;
						resultListID = "";
					}				
				}
				
				if(resultListID.isEmpty()){
					userDriver.quit();
					return itemNotFound;
				}
				
				WebElement foundItemsList = userDriver.findElement(By.id(resultListID));
				List<WebElement> foundElements = foundItemsList.findElements(By.tagName("li"));
				if (!foundElements.isEmpty()) {
					page++;
					// log.log(Level.INFO, "Method setStatDataForKey. Try to find
					// item " + itemAsin + " on " + page + " page.");
					for (WebElement foundElement : foundElements) {
						String elementAsin = foundElement.getAttribute("data-asin");
						if (elementAsin != null) {
							position++;
							if (elementAsin.contains(itemAsin)) {
								if(foundElement.getText().contains("Sponsored")){
									continue;
								}
								List<WebElement> aElements = new ArrayList<>();
								aElements = foundElement.findElements(By.tagName("a"));
								itemLink = aElements.get(0).getAttribute("href");
								if (!itemLink.contains("https://www.amazon.com")) {
									itemLink = "https://www.amazon.com" + itemLink;
								}

								log.log(Level.INFO, "Method setStatDataByAllDeps. Item was found on " + page + " page, "
										+ position + " position.\n" + "Link for item: " + itemLink);
								// set item info to DB
								KeyWord keyForSearch = new KeyWord();
								try {
									keyForSearch = KeyDAO.getKey(keyWord, itemAsin);
									timer.waitSeconds(3);
									KeyDAO.editKey(keyForSearch.key, itemAsin, keyForSearch.key, keyForSearch.addInDay,
											keyForSearch.lastAdd, itemLink);
								} catch (DAOException e1) {
									e1.printStackTrace();
								}

								String filter = "not used";

								Item foundItem = new Item();
								try {
									KeyStatDAO.editStatData(keyForSearch.key, itemAsin, keyForSearch.lastAdd, page,
											position, filter);
									foundItem = ItemDAO.getItemByAsin(itemName, itemAsin);
								} catch (DAOException e1) {
									e1.printStackTrace();
								}
								itemNotFound = false;
								if (foundItem.keyWord.equals(keyWord)) {
									log.log(Level.INFO,
											"Method setStatDataByAllDeps. This key word is main for item . Set stat data for item "
													+ itemAsin);
									timer.waitSeconds(3);
									userDriver.navigate().to(itemLink);
									if (userDriver.getPageSource().contains("acrPopover")) {
										WebElement rankElement = userDriver.findElement(By.id("acrPopover"));
										String textInRanking = rankElement.getAttribute("title");
										int itemRank = foundItem.ranking;
										if (textInRanking != null) {
											String rankString = Character.toString(textInRanking.charAt(0))
													+ Character.toString(textInRanking.charAt(1))
													+ Character.toString(textInRanking.charAt(2));
											itemRank = new RankParser().getRankInteger(rankString);
											log.log(Level.INFO, "Method setStatDataForKey. Item " + itemAsin
													+ " was found. Try to save data to DB");
										}

										try {
											ItemDAO.setStatData(itemName, itemAsin, position, page, itemRank);
										} catch (DAOException e) {
											e.printStackTrace();
										}
									}
								}
								
								log.log(Level.INFO,
										"-----------------------------------------\nItem link for key was found! Try to return to main method to move item");
								try {
									userDriver.quit();
								} catch (Exception e) {
									log.log(Level.INFO,
											"-----------------------------------------\nDriver was closed with exception.");
									return itemNotFound;
								}
								return itemNotFound;
							}
						}
					}
				} else {
					log.log(Level.INFO, "-----------------------------------------\nList of items is empty.");
					try {
						userDriver.quit();
					} catch (Exception e) {
						log.log(Level.INFO, "-----------------------------------------\nDriver was closed with exception.");
						return itemNotFound;
					}
					return itemNotFound;
				}

				if (itemNotFound) {
					if (page < lastPage) {
						if (page == 1) {
							if (userDriver.getPageSource().contains("pagnNextLink")) {
								WebElement nextPageEl = userDriver.findElement(By.id("pagnNextLink"));
								searchLink = nextPageEl.getAttribute("href");
								if (!searchLink.contains("https://www.amazon.com")) {
									searchLink = "https://www.amazon.com" + searchLink;
								}
								userDriver.get(searchLink);
								timer.waitSeconds(getRandomNumber(3, 5)); // timer
							} else {
								log.log(Level.INFO,
										"-----------------------------------------\nFirst page don't have next button.");

								try {
									userDriver.quit();
								} catch (Exception e) {
									log.log(Level.INFO,
											"-----------------------------------------\nDriver was closed with exception.");
									return itemNotFound;
								}
								return itemNotFound;

							}
						} else {
							searchLink = getNextSearchPage(userDriver.getCurrentUrl(), page + 1);
							if (!searchLink.contains("https://www.amazon.com")) {
								searchLink = "https://www.amazon.com" + searchLink;
							}
							userDriver.get(searchLink);
							timer.waitSeconds(getRandomNumber(3, 5)); // timer
						}
					} else {
						
						log.log(Level.INFO, "Method setStatDataByAllDeps. It's the last page.");
						try {
							userDriver.quit();
						} catch (Exception e) {
							log.log(Level.INFO,
									"-----------------------------------------\nDriver was closed with exception.");
							return itemNotFound;
						}
						return itemNotFound;
					}
				}
			} catch (Exception e) {
				log.log(Level.INFO, "Method setStatDataByAllDeps. Method has exception");
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(180, 300));
				userDriver = getProxyChromeDriver();
				userDriver.get(searchLink);
				continue;
			}
		}
		return itemNotFound;
	}

	public boolean setStatDataByGroup(String itemName, String itemAsin, String itemGroup, String keyWord) {
		boolean itemNotFound = true;
		String mainGroup = getMainGroup(itemGroup);
		boolean groupNotFound = true;

		String itemLink = "";
		String searchLink = "";
		WebDriver userDriver = getProxyChromeDriver();
		log.log(Level.INFO, "Method setStatDataByGroup. Try to get new search for item.");

		timer.waitSeconds(3);
		WebElement searchInput = userDriver.findElement(By.id("twotabsearchtextbox"));
		searchInput.sendKeys(keyWord);

		WebElement searchForm = userDriver.findElement(By.name("site-search"));
		timer.waitSeconds(getRandomNumber(4, 6));
		searchForm.submit();
		timer.waitSeconds(getRandomNumber(4, 6));
		searchLink = userDriver.getCurrentUrl();
		userDriver.get(searchLink);
		timer.waitSeconds(getRandomNumber(4, 6));

		// try to find group

		boolean refPresent;
		try {
			userDriver.findElement(By.id("refinements"));
			refPresent = true;
		} catch (NoSuchElementException e) {
			refPresent = false;
		}

		// try to find group
		if (refPresent) {
			WebElement refinementsBlock = userDriver.findElement(By.id("refinements"));
			List<WebElement> liElements = refinementsBlock.findElements(By.tagName("li"));
			boolean searchLinkNotFound = true;
			for (int i = 0; i < liElements.size() && i < 5; i++) {
				WebElement liElement = liElements.get(i);
				// check for present element
				if (searchLinkNotFound) {
					boolean present;
					try {
						liElement.findElement(By.tagName("a"));
						present = true;
					} catch (NoSuchElementException e) {
						present = false;
						continue;
					}
					if (present) {
						WebElement aElement = liElement.findElement(By.tagName("a"));

						if (itemGroup.contains(aElement.getText())) {
							searchLinkNotFound = false;
							groupNotFound = false;
							searchLink = aElement.getAttribute("href");
							if (!searchLink.contains("https://www.amazon.com")) {
								searchLink = "https://www.amazon.com" + searchLink;
							}
							userDriver.get(searchLink);
							timer.waitSeconds(3);
						}

					}
				}

			}
		}

		if (groupNotFound) {
			if (userDriver.getPageSource().contains("searchDropdownBox")) {

				boolean SSDPpresent;
				try {
					userDriver.findElement(By.id("searchDropdownBox"));
					SSDPpresent = true;
				} catch (NoSuchElementException e) {
					SSDPpresent = false;
				}

				if (SSDPpresent) {
					WebElement searchSelectList = userDriver.findElement(By.id("searchDropdownBox"));
					Select selectList = new Select(searchSelectList);
					selectList.selectByVisibleText(mainGroup);
					groupNotFound = false;

				}
			}

			searchForm = userDriver.findElement(By.name("site-search"));
			timer.waitSeconds(3);
			searchForm.submit();
			timer.waitSeconds(3);
			searchLink = userDriver.getCurrentUrl();
			userDriver.get(searchLink);
			timer.waitSeconds(3);
		}

		searchLink = userDriver.getCurrentUrl();

		if (searchLink.isEmpty()) {
			log.log(Level.INFO, "-----------------------------------------\nSearch link is empty.");
			try {
				userDriver.quit();
			} catch (Exception e) {
				log.log(Level.INFO, "-----------------------------------------\nDriver was closed with exception.");
				return itemNotFound;
			}
			return itemNotFound;
		}
		userDriver.get(searchLink);
		timer.waitSeconds(getRandomNumber(5, 10));
		// find last page
		int lastPage = 0;

		if (userDriver.getPageSource().contains("id=\"pagn\"")) {
			if (userDriver.getPageSource().contains("pagnDisabled")) {
				WebElement pagesElements = userDriver.findElement(By.id("pagn"));
				WebElement maxPageElement = pagesElements.findElement(By.className("pagnDisabled"));
				String lastPageStr = maxPageElement.getText();
				lastPage = Integer.valueOf(lastPageStr);
				log.log(Level.INFO, "Method setStatDataByGroup. Last page was found. It is " + lastPage + " page.");
			} else {
				WebElement pagesElements = userDriver.findElement(By.id("pagn"));
				List<WebElement> pagesLinkElements = pagesElements.findElements(By.className("pagnLink"));
				if (!pagesLinkElements.isEmpty()) {
					WebElement lastPageElement = pagesLinkElements.get((pagesLinkElements.size() - 1));
					WebElement lastPageLink = lastPageElement.findElement(By.tagName("a"));
					String lastPageStr = lastPageLink.getText();
					lastPage = Integer.valueOf(lastPageStr);
				} else {
					lastPage = 1;
				}

				log.log(Level.INFO, "Method setStatDataByGroup. Last page was found. It is " + lastPage + " page.");

			}
		}

		if (lastPage == 0) {
			lastPage = 1;
		}
		int page = 0;
		int position = 0;
		while (itemNotFound) {
			if (!userDriver.getPageSource().contains("twotabsearchtextbox")) {
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(180, 300));

				userDriver = getProxyChromeDriver();
				userDriver.get(searchLink);
			}

			try {
				boolean searchItemListNotFound = true;
				String resultListID = "";
				
				try {
					userDriver.findElement(By.id("s-results-list-atf"));
					searchItemListNotFound = false;
					resultListID = "s-results-list-atf";
				} catch (NoSuchElementException e) {
					searchItemListNotFound = true;
					resultListID = "";
				}
				
				if(searchItemListNotFound){
					try {
						userDriver.findElement(By.id("mainResults"));
						searchItemListNotFound = false;
						resultListID = "mainResults";
					} catch (NoSuchElementException e) {
						searchItemListNotFound = true;
						resultListID = "";
					}				
				}
				
				if(searchItemListNotFound){
					try {
						userDriver.findElement(By.id("resultsCol"));
						searchItemListNotFound = false;
						resultListID = "resultsCol";
					} catch (NoSuchElementException e) {
						searchItemListNotFound = true;
						resultListID = "";
					}				
				}
				
				if(resultListID.isEmpty()){
					userDriver.quit();
					return itemNotFound;
				}
				

				WebElement foundItemsList = userDriver.findElement(By.id(resultListID));
				List<WebElement> foundElements = foundItemsList.findElements(By.tagName("li"));
				if (!foundElements.isEmpty()) {
					page++;
					for (WebElement foundElement : foundElements) {
						String elementAsin = foundElement.getAttribute("data-asin");
						if (elementAsin != null) {
							position++;
							if (elementAsin.contains(itemAsin)) {
								if(foundElement.getText().contains("Sponsored")){
									continue;
								}
								
								List<WebElement> aElements = new ArrayList<>();
								aElements = foundElement.findElements(By.tagName("a"));
								itemLink = aElements.get(0).getAttribute("href");
								if (!itemLink.contains("https://www.amazon.com")) {
									itemLink = "https://www.amazon.com" + itemLink;
								}

								log.log(Level.INFO, "Method setStatDataForKey. Item was found on " + page + " page, "
										+ position + " position.\n" + "Link for item: " + itemLink);
								// set item info to DB
								KeyWord keyForSearch = new KeyWord();
								try {
									keyForSearch = KeyDAO.getKey(keyWord, itemAsin);
									timer.waitSeconds(3);
									KeyDAO.editKey(keyForSearch.key, itemAsin, keyForSearch.key, keyForSearch.addInDay,
											keyForSearch.lastAdd, itemLink);
								} catch (DAOException e1) {
									e1.printStackTrace();
								}

								String filter = "in group";

								Item foundItem = new Item();
								try {
									KeyStatDAO.editStatData(keyForSearch.key, itemAsin, keyForSearch.lastAdd, page,
											position, filter);
									foundItem = ItemDAO.getItemByAsin(itemName, itemAsin);
								} catch (DAOException e1) {
									e1.printStackTrace();
								}
								itemNotFound = false;
								if (foundItem.keyWord.equals(keyWord)) {
									log.log(Level.INFO,
											"Method setStatDataByGroup. This key word is main for item . Set stat data for item "
													+ itemAsin);
									timer.waitSeconds(3);
									userDriver.navigate().to(itemLink);
									if (userDriver.getPageSource().contains("acrPopover")) {
										WebElement rankElement = userDriver.findElement(By.id("acrPopover"));
										String textInRanking = rankElement.getAttribute("title");
										int itemRank = foundItem.ranking;
										if (textInRanking != null) {
											String rankString = Character.toString(textInRanking.charAt(0))
													+ Character.toString(textInRanking.charAt(1))
													+ Character.toString(textInRanking.charAt(2));
											itemRank = new RankParser().getRankInteger(rankString);
											log.log(Level.INFO, "Method setStatDataByGroup. Item " + itemAsin
													+ " was found. Try to save data to DB");
										}

										try {
											ItemDAO.setStatData(itemName, itemAsin, position, page, itemRank);
										} catch (DAOException e) {
											e.printStackTrace();
										}
									}
								}
								
								log.log(Level.INFO,
										"-----------------------------------------\nItem link for key was found! Try to return to main method to move item");
								try {
									userDriver.quit();
								} catch (Exception e) {
									log.log(Level.INFO,
											"-----------------------------------------\nDriver was closed with exception.");
									return itemNotFound;
								}
								return itemNotFound;
							}
						}
					}
				} else {
					log.log(Level.INFO, "-----------------------------------------\nList of items is empty.");
					try {
						userDriver.quit();
					} catch (Exception e) {
						log.log(Level.INFO, "-----------------------------------------\nDriver was closed with exception.");
						return itemNotFound;
					}
					return itemNotFound;
				}

				if (itemNotFound) {
					if (page < lastPage) {
						if (page == 1) {
							if (userDriver.getPageSource().contains("pagnNextLink")) {
								WebElement nextPageEl = userDriver.findElement(By.id("pagnNextLink"));
								searchLink = nextPageEl.getAttribute("href");
								if (!searchLink.contains("https://www.amazon.com")) {
									searchLink = "https://www.amazon.com" + searchLink;
								}
								userDriver.get(searchLink);
								timer.waitSeconds(getRandomNumber(3, 5)); // timer
							} else {
								log.log(Level.INFO,
										"-----------------------------------------\nFirst page don't have next button.");

								try {
									userDriver.quit();
								} catch (Exception e) {
									log.log(Level.INFO,
											"-----------------------------------------\nDriver was closed with exception.");
									return itemNotFound;
								}
								return itemNotFound;

							}
						} else {
							searchLink = getNextSearchPage(userDriver.getCurrentUrl(), page + 1);
							if (!searchLink.contains("https://www.amazon.com")) {
								searchLink = "https://www.amazon.com" + searchLink;
							}
							userDriver.get(searchLink);
							timer.waitSeconds(getRandomNumber(3, 5)); // timer
						}
					} else {
						
						log.log(Level.INFO, "Method setStatDataByGroup. It's the last page.");
						try {
							userDriver.quit();
						} catch (Exception e) {
							log.log(Level.INFO,
									"-----------------------------------------\nDriver was closed with exception.");
							return itemNotFound;
						}
						return itemNotFound;
					}
				}
			} catch (Exception e) {
				log.log(Level.INFO, "Method setStatDataByGroup. Method has exception");
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(180, 300));
				userDriver = getProxyChromeDriver();
				userDriver.get(searchLink);
				continue;
			}
		}
		return itemNotFound;
	}

	public boolean setStatDataByFilter(String itemName, String itemAsin, String itemGroup, String keyWord) {
		boolean itemNotFound = true;

		String itemLink = "";
		String searchLink = getSearchLinkByFilter("", itemName, itemAsin, keyWord, itemGroup);
		if (searchLink.isEmpty()) {
			return itemNotFound;
		}
		timer.waitSeconds(getRandomNumber(5, 10));
		WebDriver userDriver = getProxyChromeDriver();
		log.log(Level.INFO, "Method setStatDataByFilter. Try to get new search for item.");
		userDriver.get(searchLink);
		timer.waitSeconds(getRandomNumber(5, 10));

		// find last page
		int lastPage = 0;

		if (userDriver.getPageSource().contains("id=\"pagn\"")) {
			if (userDriver.getPageSource().contains("pagnDisabled")) {
				WebElement pagesElements = userDriver.findElement(By.id("pagn"));
				WebElement maxPageElement = pagesElements.findElement(By.className("pagnDisabled"));
				String lastPageStr = maxPageElement.getText();
				lastPage = Integer.valueOf(lastPageStr);
				log.log(Level.INFO, "Method setStatDataByFilter. Last page was found. It is " + lastPage + " page.");
			} else {
				WebElement pagesElements = userDriver.findElement(By.id("pagn"));
				List<WebElement> pagesLinkElements = pagesElements.findElements(By.className("pagnLink"));
				if (!pagesLinkElements.isEmpty()) {
					WebElement lastPageElement = pagesLinkElements.get((pagesLinkElements.size() - 1));
					WebElement lastPageLink = lastPageElement.findElement(By.tagName("a"));
					String lastPageStr = lastPageLink.getText();
					lastPage = Integer.valueOf(lastPageStr);
				} else {
					lastPage = 1;
				}

				log.log(Level.INFO, "Method setStatDataByFilter. Last page was found. It is " + lastPage + " page.");

			}
		}

		if (lastPage == 0) {
			lastPage = 1;
		}
		int page = 0;
		int position = 0;
		while (itemNotFound) {

			if (!userDriver.getPageSource().contains("twotabsearchtextbox")) {
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(180, 300));

				userDriver = getProxyChromeDriver();
				userDriver.get(searchLink);
			}

			try {
				boolean searchItemListNotFound = true;
				String resultListID = "";
				
				try {
					userDriver.findElement(By.id("s-results-list-atf"));
					searchItemListNotFound = false;
					resultListID = "s-results-list-atf";
				} catch (NoSuchElementException e) {
					searchItemListNotFound = true;
					resultListID = "";
				}
				
				if(searchItemListNotFound){
					try {
						userDriver.findElement(By.id("mainResults"));
						searchItemListNotFound = false;
						resultListID = "mainResults";
					} catch (NoSuchElementException e) {
						searchItemListNotFound = true;
						resultListID = "";
					}				
				}
				
				if(searchItemListNotFound){
					try {
						userDriver.findElement(By.id("resultsCol"));
						searchItemListNotFound = false;
						resultListID = "resultsCol";
					} catch (NoSuchElementException e) {
						searchItemListNotFound = true;
						resultListID = "";
					}				
				}
				
				if(resultListID.isEmpty()){
					userDriver.quit();
					return itemNotFound;
				}
				

				WebElement foundItemsList = userDriver.findElement(By.id(resultListID));
				List<WebElement> foundElements = foundItemsList.findElements(By.tagName("li"));
				if (!foundElements.isEmpty()) {
					page++;
					for (WebElement foundElement : foundElements) {
						String elementAsin = foundElement.getAttribute("data-asin");
						if (elementAsin != null) {
							position++;
							if (elementAsin.contains(itemAsin)) {
								if(foundElement.getText().contains("Sponsored")){
									continue;
								}
								List<WebElement> aElements = new ArrayList<>();
								aElements = foundElement.findElements(By.tagName("a"));
								itemLink = aElements.get(0).getAttribute("href");
								if (!itemLink.contains("https://www.amazon.com")) {
									itemLink = "https://www.amazon.com" + itemLink;
								}

								log.log(Level.INFO, "Method setStatDataForKey. Item was found on " + page + " page, "
										+ position + " position.\n" + "Link for item: " + itemLink);
								// set item info to DB
								KeyWord keyForSearch = new KeyWord();
								try {
									keyForSearch = KeyDAO.getKey(keyWord, itemAsin);
									timer.waitSeconds(3);
									KeyDAO.editKey(keyForSearch.key, itemAsin, keyForSearch.key, keyForSearch.addInDay,
											keyForSearch.lastAdd, itemLink);
								} catch (DAOException e1) {
									e1.printStackTrace();
								}

								String filter = "by price";

								Item foundItem = new Item();
								try {
									KeyStatDAO.editStatData(keyForSearch.key, itemAsin, keyForSearch.lastAdd, page,
											position, filter);
									foundItem = ItemDAO.getItemByAsin(itemName, itemAsin);
								} catch (DAOException e1) {
									e1.printStackTrace();
								}
								itemNotFound = false;
								if (foundItem.keyWord.equals(keyWord)) {
									log.log(Level.INFO,
											"Method setStatDataForKey. This key word is main for item . Set stat data for item "
													+ itemAsin);
									timer.waitSeconds(3);
									userDriver.navigate().to(itemLink);
									if (userDriver.getPageSource().contains("acrPopover")) {
										WebElement rankElement = userDriver.findElement(By.id("acrPopover"));
										String textInRanking = rankElement.getAttribute("title");
										int itemRank = foundItem.ranking;
										if (textInRanking != null) {
											String rankString = Character.toString(textInRanking.charAt(0))
													+ Character.toString(textInRanking.charAt(1))
													+ Character.toString(textInRanking.charAt(2));
											itemRank = new RankParser().getRankInteger(rankString);
											log.log(Level.INFO, "Method setStatDataForKey. Item " + itemAsin
													+ " was found. Try to save data to DB");
										}

										try {
											ItemDAO.setStatData(itemName, itemAsin, position, page, itemRank);
										} catch (DAOException e) {
											e.printStackTrace();
										}
									}
								}
								
								log.log(Level.INFO,
										"-----------------------------------------\nItem link for key was found! Try to return to main method to move item");
								try {
									userDriver.quit();
								} catch (Exception e) {
									log.log(Level.INFO,
											"-----------------------------------------\nDriver was closed with exception.");
									return itemNotFound;
								}
								return itemNotFound;
							}
						}
					}
				} else {
					log.log(Level.INFO, "-----------------------------------------\nList of items is empty.");
					try {
						userDriver.quit();
					} catch (Exception e) {
						log.log(Level.INFO, "-----------------------------------------\nDriver was closed with exception.");
						return itemNotFound;
					}
					return itemNotFound;
				}

				if (itemNotFound) {
					if (page < lastPage) {
						if (page == 1) {
							if (userDriver.getPageSource().contains("pagnNextLink")) {
								WebElement nextPageEl = userDriver.findElement(By.id("pagnNextLink"));
								searchLink = nextPageEl.getAttribute("href");
								if (!searchLink.contains("https://www.amazon.com")) {
									searchLink = "https://www.amazon.com" + searchLink;
								}
								userDriver.get(searchLink);
								timer.waitSeconds(getRandomNumber(3, 5)); // timer
							} else {
								log.log(Level.INFO,
										"-----------------------------------------\nFirst page don't have next button.");

								try {
									userDriver.quit();
								} catch (Exception e) {
									log.log(Level.INFO,
											"-----------------------------------------\nDriver was closed with exception.");
									return itemNotFound;
								}
								return itemNotFound;

							}
						} else {
							searchLink = getNextSearchPage(userDriver.getCurrentUrl(), page + 1);
							if (!searchLink.contains("https://www.amazon.com")) {
								searchLink = "https://www.amazon.com" + searchLink;
							}
							userDriver.get(searchLink);
							timer.waitSeconds(getRandomNumber(3, 5)); // timer
						}
					} else {
						
						log.log(Level.INFO, "Method setStatDataForKey. It's the last page.");
						try {
							userDriver.quit();
						} catch (Exception e) {
							log.log(Level.INFO,
									"-----------------------------------------\nDriver was closed with exception.");
							return itemNotFound;
						}
						return itemNotFound;
					}
				}
			} catch (Exception e) {
				log.log(Level.INFO, "Method setStatDataByFilter. Method has exception");
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(180, 300));
				userDriver = getProxyChromeDriver();
				userDriver.get(searchLink);
				continue;
			}
		}
		return itemNotFound;
	}

	public boolean moveItemOnAmazon(String keyWord, String itemLink, String itemName, String itemAsin, Account account) {

		boolean itemAdded = false;
		// try to login
		WebDriver userDriver = getProxyChromeDriver();
		// check captcha
		if (!userDriver.getPageSource().contains("searchDropdownBox")) {
			try {
				userDriver.quit();
			} catch (Exception e) {
				userDriver.quit();
				timer.waitSeconds(getRandomNumber(30, 60));
				return itemAdded;
			}
			timer.waitSeconds(getRandomNumber(30, 60));
			return itemAdded;
		}
		timer.waitSeconds(getRandomNumber(5, 10));
		String startPageHTML = userDriver.getPageSource();
		timer.waitSeconds(getRandomNumber(5, 10));

		// check page isn't logined by any account
		if (startPageHTML.contains("Hello. Sign in")) {

			log.log(Level.INFO,
					"Method moveItemOnAmazon. Try to login " + account.login + " to move item by key " + keyToMove.key);

			WebElement regLinkElement = userDriver.findElement(By.id("nav-flyout-ya-signin"));
			WebElement regLinkAElement = regLinkElement.findElement(By.tagName("a"));
			String loginlink = regLinkAElement.getAttribute("href");
			if (!loginlink.contains("https://www.amazon.com")) {
				loginlink = "https://www.amazon.com" + loginlink;
			}
			userDriver.get(loginlink);
			timer.waitSeconds(getRandomNumber(7, 12));
			WebElement formElement = userDriver.findElement(By.name("signIn"));
			WebElement inputLogin = userDriver.findElement(By.id("ap_email"));
			inputLogin.sendKeys(account.login);
			WebElement inputPass = userDriver.findElement(By.id("ap_password"));
			inputPass.sendKeys(account.password);
			timer.waitSeconds(getRandomNumber(30, 45));
			formElement.submit();
			timer.waitSeconds(getRandomNumber(7, 10));
			userDriver.get("https://www.amazon.com");
			timer.waitSeconds(getRandomNumber(4, 6));

			String currentPage = userDriver.getPageSource();

			if (currentPage.contains("Hello, ")) {
				log.log(Level.INFO, "Method moveItemOnAmazon. Account " + account.login + " was logged in.");

			} else {
				log.log(Level.INFO,
						"Method moveItemOnAmazon. Account " + account.login + " wasn't logined. Try again.");
				userDriver.quit();
				timer.waitSeconds(60);
				return itemAdded;
			}
			
			// try to find qid argument
			String qid = "";
			
			timer.waitSeconds(getRandomNumber(4, 6));
			WebElement searchInput = userDriver.findElement(By.id("twotabsearchtextbox"));
			searchInput.sendKeys(keyWord);

			WebElement searchForm = userDriver.findElement(By.name("site-search"));
			timer.waitSeconds(getRandomNumber(4, 6));
			searchForm.submit();
			timer.waitSeconds(getRandomNumber(4, 6));
			String searchLink = userDriver.getCurrentUrl();
			userDriver.get(searchLink);
			timer.waitSeconds(getRandomNumber(4, 6));
			
			boolean refPresent;
			try {
				userDriver.findElement(By.id("refinements"));
				refPresent = true;
			} catch (NoSuchElementException e) {
				refPresent = false;
			}

			// try to find qid in link
			if (refPresent) {
				WebElement refinementsBlock = userDriver.findElement(By.id("refinements"));
				List<WebElement> liElements = refinementsBlock.findElements(By.tagName("li"));
				boolean qidNotFound = true;
				for (int i = 0; i < liElements.size() && i < 5; i++) {
					// check for present element
					if (qidNotFound) {
						WebElement liElement = liElements.get(i);
						boolean aPresent;
						try {
							liElement.findElement(By.tagName("a"));
							aPresent = true;
						} catch (NoSuchElementException e) {
							aPresent = false;
							continue;
						}
						if (aPresent) {
							WebElement aElement = liElement.findElement(By.tagName("a"));
							
							String linkWithQid = aElement.getAttribute("href");
							if(linkWithQid.contains("qid=")){
								qidNotFound = false;
								String[] splitedLink = linkWithQid.split("&");
								for (String splitedElement : splitedLink) {
									if(splitedElement.contains("qid=")){
										qid = splitedElement;
									}
								}
								
							}
						}
					}
				}
			}
			
			if(qid.isEmpty()){
				
			}
			
			
			if(qid.isEmpty()){
				log.log(Level.INFO, "Method moveItemOnAmazon. QID was not found. Try again.");
				userDriver.quit();
				timer.waitSeconds(10);
				return itemAdded;
			}
			
			// change itemLink
			itemLink = changedItemLink(itemLink, qid);
						
			userDriver.get(itemLink);
			timer.waitSeconds(getRandomNumber(3, 5));

		} else {
			log.log(Level.INFO, "Method moveItemOnAmazon. Account " + account.login
					+ " cannot be logined. Page has been already used.");
			userDriver.quit();
			timer.waitSeconds(10);
			return itemAdded;
		}
		
		// add to WL
		try {
			log.log(Level.INFO, "Method moveItemOnAmazon. Try to add item " + itemAsin + " to WL");
			String itemPage = userDriver.getPageSource();
			timer.waitSeconds(getRandomNumber(5, 10));
			String parentHandle = userDriver.getWindowHandle();

			if (itemPage.contains("id=\"wishListDropDown\"")) {
				if (itemPage.contains("add-to-wishlist-button")) {
					WebElement addToListBtn = userDriver.findElement(By.id("add-to-wishlist-button"));
					addToListBtn.click();
					timer.waitSeconds(getRandomNumber(7, 10));
					for (String childHandle : userDriver.getWindowHandles()) {
						if (!childHandle.equals(parentHandle)) {
							userDriver.switchTo().window(childHandle);
						}
					}
					itemPage = userDriver.getPageSource();

					if (itemPage.contains("atwl-dd-ul")) {
						WebElement ulListEl = userDriver.findElement(By.id("atwl-dd-ul"));
						timer.waitSeconds(getRandomNumber(7, 10));
						List<WebElement> liElements = ulListEl.findElements(By.tagName("li"));
						timer.waitSeconds(getRandomNumber(7, 10));
						boolean itemNotAddedToWL = true;
						for (WebElement liElement : liElements) {
							WebElement aElement = liElement.findElement(By.tagName("a"));

							if (aElement.getText().contains("Wish") && itemNotAddedToWL) {
								itemNotAddedToWL = false;
								aElement.click();
								log.log(Level.INFO, "Method moveItemOnAmazon. Item " + itemName + " was added to WL.");
								timer.waitSeconds(getRandomNumber(7, 10));
							}
						}

					}
					timer.waitSeconds(getRandomNumber(7, 10));
					userDriver.switchTo().window(parentHandle);
				}
			} else {
				if (userDriver.getPageSource().contains("add-to-wishlist-button-submit")) {
					WebElement addToListBtn = userDriver.findElement(By.id("add-to-wishlist-button-submit"));
					addToListBtn.click();
					timer.waitSeconds(getRandomNumber(7, 10));
					for (String childHandle : userDriver.getWindowHandles()) {
						if (!childHandle.equals(parentHandle)) {
							userDriver.switchTo().window(childHandle);
						}
					}
					itemPage = userDriver.getPageSource();
					if (itemPage.contains("WLNEW_newwl_section")) {
						WebElement wishSection = userDriver.findElement(By.id("WLNEW_newwl_section"));
						wishSection.click();
						timer.waitSeconds(getRandomNumber(7, 10));
						if (userDriver.getPageSource().contains("WLNEW_valid_submit")) {
							WebElement wishElementSubmit = userDriver.findElement(By.id("WLNEW_valid_submit"));
							wishElementSubmit.click();
							log.log(Level.INFO, "Method moveItemOnAmazon. Item " + itemName + " was added to WL.");
						}
					}
				}

				timer.waitSeconds(getRandomNumber(7, 10));
				userDriver.switchTo().window(parentHandle);
			}

			log.log(Level.INFO, "Method moveItemOnAmazon. Action \"add to wishList\" was saved to DB");
			
			timer.waitSeconds(getRandomNumber(10, 15));

			// add to cart
			userDriver.get(itemLink);
			log.log(Level.INFO, "Method moveItemOnAmazon. Try to add item " + itemAsin + " to cart");
			timer.waitSeconds(getRandomNumber(7, 12));
			
			
			boolean addToCartPresent;
			try {
				userDriver.findElement(By.id("add-to-cart-button"));
				addToCartPresent = true;
			} catch (NoSuchElementException e) {
				addToCartPresent = false;
			}
			
			if (addToCartPresent) {
				WebElement addToCartBtn = userDriver.findElement(By.id("add-to-cart-button"));
				addToCartBtn.submit();
				itemAdded = true;
				
				timer.waitSeconds(getRandomNumber(7, 12));
				
				userDriver.quit();

			} 
			
			boolean buyOptionsPresent;
			try {
				userDriver.findElement(By.id("buybox-see-all-buying-choices-announce"));
				buyOptionsPresent = true;
			} catch (NoSuchElementException e) {
				buyOptionsPresent = false;
			}
			
			if(buyOptionsPresent){
				WebElement buyOptionBtn = userDriver.findElement(By.id("buybox-see-all-buying-choices-announce"));
				String buyOptionLink = buyOptionBtn.getAttribute("href");
				if (!buyOptionLink.contains("https://www.amazon.com")) {
					buyOptionLink = "https://www.amazon.com" + buyOptionLink;
				}
				userDriver.get(buyOptionLink);
				timer.waitSeconds(getRandomNumber(7, 12)); // timer
				List<WebElement> addToCartElements = userDriver.findElements(By.className("a-button-input"));
				
				for (WebElement addToCartBtn : addToCartElements) {
					if(!itemAdded){
						String valAttr = addToCartBtn.getAttribute("value");
						if(valAttr.equals("Add to cart")){
							addToCartBtn.click();
							itemAdded = true;
							timer.waitSeconds(getRandomNumber(5, 10));
							
							userDriver.quit();
						}
					}
				}
			}
			
			if(itemAdded){
				log.log(Level.INFO, "Method moveItemOnAmazon. Item " + itemName + " was added to cart.");
				try {
					//ItemActionDAO.addAction(itemName, itemAsin, account.login, "add to cart");
					ItemDAO.setAddingToCart(itemName, itemAsin);
					AccountDAO.deleteAccount(account.login, account.password);
					log.log(Level.INFO, "Method moveItemOnAmazon. Action \"add to cart\" was saved to DB.");
				} catch (DAOException e) {
					log.log(Level.INFO, "Method moveItemOnAmazon. Item was added to cart, but action by account "
							+ account.login + " was not saved to DB");
					e.printStackTrace();
				}
			}
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "Method moveItemOnAmazon has exception.");
			userDriver.quit();
			return itemAdded;
		}

		return itemAdded;
	}

	private synchronized String changedItemLink(String itemLink, String qid) {
		String changedLink = "";
		String[] splitedLink = itemLink.split("&");
		for (int i=0; i<splitedLink.length; i++) {
			String splitedElement = splitedLink[i];
			if(splitedElement.contains("qid=")){
				changedLink += qid;
			} else{
				changedLink += splitedElement;
			}
			
			if(i<(splitedLink.length-1)){
				changedLink += "&";
			}
		}
		
		
		
		
		return changedLink;
	}

	public synchronized void deleteFile(File file) {

		if (!file.exists()) {
			return;
		}
		File[] files = file.listFiles();
		if (files.length != 0) {
			for (File fileIn : files) {
				if (fileIn.isDirectory()) {
					for (File f : fileIn.listFiles()) {
						if (f.isDirectory()) {
							deleteFile(f);
							f.delete();
						} else {
							f.delete();
						}
					}
				}
				fileIn.delete();
			}
		}
	}

	private synchronized Account getAccountToMove() {
		boolean accountNotFound = true;
		Account accountForAdd = new Account();
		while (accountNotFound) {

			ArrayList<Account> accountsForAdd = new ArrayList<>();
			try {
				accountsForAdd = AccountDAO.getAccountsForCartAdd();
				log.log(Level.INFO, accountsForAdd.size() + " accounts were choosen for adding to cart.");
			} catch (DAOException e) {
				log.log(Level.INFO, "Getting accountsForCart has Exception. Try with new account");
				e.printStackTrace();
				continue;
			}

			// read account from file

			if (accountsForAdd.size() == 0) {
				try {
					BufferedReader reader = new BufferedReader(new FileReader("C:\\java\\AccToDB.txt"));
					String line;
					while ((line = reader.readLine()) != null) {
						String[] splitedLine = line.split(" ");
						String accLogin = splitedLine[0];
						String accPass = splitedLine[1];
						AccountDAO.create(accLogin, accPass, 0);
					}
					reader.close();
					accountsForAdd = AccountDAO.getAccountsForCartAdd();
				} catch (NumberFormatException | IOException | DAOException e) {
					e.printStackTrace();
				}
			}
			int accIndex = getRandomIndex(accountsForAdd.size());
			accountForAdd = accountsForAdd.get(accIndex);
			log.log(Level.INFO, "AccountFC " + accountForAdd.login + " was choosen from list.");
			accountNotFound = false;

		}
		log.log(Level.INFO, "AccountFC " + accountForAdd.login + " was found.");
		return accountForAdd;
	}

	public WebDriver getProxyChromeDriver() {
		System.setProperty("webdriver.chrome.driver", "C:\\java\\selenium\\chromedriver.exe");

		// delete data from pc
		String pcName = System.getProperty("user.name");
		String profileDirForDel = "C:\\Users\\" + pcName + "\\AppData\\Local\\Google\\Chrome\\" + profile;
		File profDir = new File(profileDirForDel);
		deleteFile(profDir);

		// copy new files to profile dir
		String newProfileDir = "C:\\Users\\" + pcName + "\\AppData\\Local\\Google\\Chrome\\newProf";
		File newProfDir = new File(newProfileDir);
		try {
			copyDirectory(newProfDir, profDir);
		} catch (IOException e1) {

			e1.printStackTrace();
		}

		DesiredCapabilities chrome = DesiredCapabilities.chrome();

		// set Profile
		String profileDir = "C:\\Users\\" + pcName + "\\AppData\\Local\\Google\\Chrome\\" + profile;
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");
		options.addArguments("user-data-dir=" + profileDir);

		chrome.setCapability(ChromeOptions.CAPABILITY, options);
		// set Proxy

		ArrayList<ProxyForUse> proxies = getAllProxies();
		int rndIndex = getRandomIndex(proxies.size());
		ProxyForUse proxyForUse = proxies.get(rndIndex);
		String rndProxy = proxyForUse.getProxy() + ":" + proxyForUse.getPort();
		options.addArguments("--proxy-server=http://" + rndProxy);
		chrome.setCapability(ChromeOptions.CAPABILITY, options);

		// delete proxy from BD
		try {
			ProxiesDAO.deleteProxy(proxyForUse.getProxy(), proxyForUse.getPort());
		} catch (DAOException e1) {
			e1.printStackTrace();
		}

		WebDriver driver = WebDriverFactory.getDriver(chrome);
		driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
		try {
			timer.waitSeconds(10);

			driver.get("https://www.amazon.com");

			timer.waitSeconds(getRandomNumber(10, 15)); // it was 10-15 sec
														// before

			// check driver
			String currentPage = driver.getPageSource();
			if (currentPage.contains("twotabsearchtextbox")) {
				return driver;
			} else {
				driver.quit();
				log.log(Level.INFO, "Method getNewFirefoxDriver. Web driver wasn't created. Try again");
				timer.waitSeconds(getRandomNumber(300, 600)); // it was 5-10
																// min
																// before
				driver = getProxyChromeDriver();

			}

		} catch (Exception e) {
			log.log(Level.INFO, "Method getNewFirefoxDriver. Web driver wasn't created.");
			driver.quit();
			timer.waitSeconds(getRandomNumber(600, 900)); // it was 10-15
			// min
			// before
			driver = getProxyChromeDriver();

		}
		return driver;
	}

	public synchronized void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public synchronized ArrayList<ProxyForUse> getAllProxies() {
		ArrayList<ProxyForUse> proxies = new ArrayList<>();
		ProxiesDAO prxDAO = new ProxiesDAO();
		try {
			proxies = prxDAO.getAllProxies();
		} catch (DAOException e1) {
			e1.printStackTrace();
		}
		if (proxies.isEmpty()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader("C:\\java\\proxy_http_ip.txt"));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] splitedLine = line.split(":");
					String proxyAsString = splitedLine[0];
					String portAsString = splitedLine[1];
					int portAsInt = Integer.valueOf(portAsString);
					ProxiesDAO.create(proxyAsString, portAsInt);
				}
				reader.close();
				proxies = prxDAO.getAllProxies();
			} catch (NumberFormatException | IOException | DAOException e) {
				e.printStackTrace();
			}
		}
		return proxies;
	}

	public String getSearchLinkByFilter(String searchLink, String itemName, String itemAsin, String keyWord,
			String itemGroup) {

		boolean groupNotFound = true;
		String mainGroup = getMainGroup(itemGroup);

		log.log(Level.INFO, "Method getSearchLinkByFilter. Try to get new search for item by filter");
		WebDriver userDriver = getProxyChromeDriver();
		try {

			log.log(Level.INFO, "Method setStatDataForKey. Try to get new search for item.");

			timer.waitSeconds(3);
			WebElement searchInput = userDriver.findElement(By.id("twotabsearchtextbox"));
			searchInput.sendKeys(keyWord);

			WebElement searchForm = userDriver.findElement(By.name("site-search"));
			timer.waitSeconds(getRandomNumber(4, 6));
			searchForm.submit();
			timer.waitSeconds(getRandomNumber(4, 6));
			searchLink = userDriver.getCurrentUrl();
			userDriver.get(searchLink);
			timer.waitSeconds(getRandomNumber(4, 6));

			// try to find group

			boolean refPresent;
			try {
				userDriver.findElement(By.id("refinements"));
				refPresent = true;
			} catch (NoSuchElementException e) {
				refPresent = false;
			}

			// try to find group
			if (refPresent) {
				WebElement refinementsBlock = userDriver.findElement(By.id("refinements"));
				List<WebElement> liElements = refinementsBlock.findElements(By.tagName("li"));
				boolean searchLinkNotFound = true;
				for (int i = 0; i < liElements.size() && i < 5; i++) {
					WebElement liElement = liElements.get(i);
					// check for present element
					if (searchLinkNotFound) {
						boolean present;
						try {
							liElement.findElement(By.tagName("a"));
							present = true;
						} catch (NoSuchElementException e) {
							present = false;
							continue;
						}
						if (present) {
							WebElement aElement = liElement.findElement(By.tagName("a"));

							if (itemGroup.contains(aElement.getText())) {
								searchLinkNotFound = false;
								groupNotFound = false;
								searchLink = aElement.getAttribute("href");
								if (!searchLink.contains("https://www.amazon.com")) {
									searchLink = "https://www.amazon.com" + searchLink;
								}
								userDriver.get(searchLink);
								timer.waitSeconds(3);
							}

						}
					}

				}
			}

			if (groupNotFound) {
				if (userDriver.getPageSource().contains("searchDropdownBox")) {

					boolean SSDPpresent;
					try {
						userDriver.findElement(By.id("searchDropdownBox"));
						SSDPpresent = true;
					} catch (NoSuchElementException e) {
						SSDPpresent = false;
					}

					if (SSDPpresent) {
						WebElement searchSelectList = userDriver.findElement(By.id("searchDropdownBox"));
						Select selectList = new Select(searchSelectList);
						selectList.selectByVisibleText(mainGroup);
						groupNotFound = false;

					}
				}

				searchForm = userDriver.findElement(By.name("site-search"));
				timer.waitSeconds(3);
				searchForm.submit();
				timer.waitSeconds(3);
				searchLink = userDriver.getCurrentUrl();
				userDriver.get(searchLink);
				timer.waitSeconds(3);
			}

			searchLink = userDriver.getCurrentUrl();

			// try to set price
			Item item = new Item();
			try {
				item = ItemDAO.getItemByAsin(itemName, itemAsin);
			} catch (DAOException e) {
				e.printStackTrace();
			}
			// set min and max walue for search
			int lowPriceInt = (int)(item.price / 100);// - 1;
			if (lowPriceInt < 0) {
				lowPriceInt = 0;
			}
			int highPriceInt = (item.price / 100) + 1;
			WebElement lowPriceIn = userDriver.findElement(By.id("low-price"));
			lowPriceIn.sendKeys(String.valueOf(lowPriceInt));
			WebElement highPriceIn = userDriver.findElement(By.id("high-price"));
			highPriceIn.sendKeys(String.valueOf(highPriceInt));
			timer.waitSeconds(getRandomNumber(5, 10));
			WebElement goBtn = userDriver.findElement(By.className("leftNavGoBtn"));
			goBtn.click();
			timer.waitSeconds(getRandomNumber(5, 10));
			searchLink = userDriver.getCurrentUrl();

		} catch (Exception e) {
			userDriver.quit();
			return "";
		}
		userDriver.quit();
		return searchLink;
	}

	public String getNextSearchPage(String navUrl, int pageToBeIn) {
		String nextPage = "";
		if (!navUrl.contains("page=")) {
			log.log(Level.INFO, "Method getNextSearchPage. Source page cannot be changed.");
			return nextPage;
		}

		char[] strElements = navUrl.toCharArray();
		ArrayList<Character> charElements = new ArrayList<>();
		for (char chElement : strElements) {
			charElements.add(chElement);
		}
		char[] intElements = String.valueOf(pageToBeIn).toCharArray();
		int repIndex = -1;
		for (int i = 0; i < charElements.size(); i++) {
			if (charElements.get(i) == '=' && charElements.get(i - 1) == 'e' && charElements.get(i - 2) == 'g'
					&& charElements.get(i - 3) == 'a' && charElements.get(i - 4) == 'p') {
				repIndex = i + 1;
			}
			if (repIndex == i) {
				if (Character.isDigit(charElements.get(i))) {
					charElements.remove(i);
					i--;
				}
			}
		}
		for (int i = 0; i < intElements.length; i++) {
			charElements.add(repIndex, intElements[i]);
			repIndex++;
		}
		for (char chToStr : charElements) {
			if (Character.isDigit(chToStr)) {
				nextPage += Character.getNumericValue(chToStr);
			} else {
				nextPage += String.valueOf(chToStr);
			}
		}
		return nextPage;
	}

	public String getMainGroup(String group) {
		String mainGroup = "";
		char[] symbols = group.toCharArray();
		for (int i = 0; i < symbols.length; i++) {
			String iSymbol = Character.toString(symbols[i]);
			String nextSymbol = "";
			if (i != (symbols.length - 1)) {
				nextSymbol = Character.toString(symbols[i + 1]);
			}
			if (iSymbol.equals(" ") && nextSymbol.equals(">")) {
				return mainGroup;
			} else {
				mainGroup += iSymbol;
			}

		}
		return mainGroup;
	}

	public int getRandomIndex(int size) {
		int index;
		index = (int) (Math.random() * size);
		// check that index is correct
		if (index > (size - 1)) {
			index = size - 1;
		}
		return index;
	}

	public boolean linkIsCorrect(String itemLink) {
		boolean linkIsCorrect = false;
		if (!itemLink.isEmpty()) {
			linkIsCorrect = true;
			;
		}
		return linkIsCorrect;
	}

	public int getRandomNumber(int min, int max) {
		int number = (int) (min + (Math.random() * (max - min)));
		return number;
	}
}
