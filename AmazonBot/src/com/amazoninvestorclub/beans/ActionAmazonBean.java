package com.amazoninvestorclub.beans;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ItemDAO;
import com.amazoninvestorclub.DAO.KeyDAO;
import com.amazoninvestorclub.DAO.KeyStatDAO;
import com.amazoninvestorclub.DAO.ProxiesDAO;
import com.amazoninvestorclub.domain.Account;
import com.amazoninvestorclub.domain.Item;
import com.amazoninvestorclub.domain.KeyWord;
import com.amazoninvestorclub.domain.ProxyForUse;
import com.amazoninvestorclub.domain.WaitTimer;
import com.amazoninvestorclub.moveThreads.MoveItemByKey;

import ru.stqa.selenium.factory.WebDriverFactory;

public class ActionAmazonBean {

	private static Logger log = Logger.getLogger(ActionAmazonBean.class.getName());
	public WaitTimer timer = new WaitTimer();

	@SuppressWarnings("deprecation")
	public void moveItems(ArrayList<Item> itemsForAdd) {
		
		
		ArrayList<Thread> threads = new ArrayList<>();
		int threadsAmount = 12; // max value of paralel threads
		int keysAmount = threadsAmount / itemsForAdd.size(); // amount of keys
																// to move
		if (keysAmount > 8) {
			keysAmount = 8;
		}
		if (keysAmount == 0) {
			keysAmount = 1;
		}

		log.log(Level.INFO, "Can move " + keysAmount + " keys for every item.");
		int countOfThreads = 0;
		for (Item item : itemsForAdd) {
			
			ArrayList<KeyWord> keysToMove = new ArrayList<>();
			ArrayList<KeyWord> checkedKeysToMove = new ArrayList<>();
			try {
				keysToMove = KeyDAO.getKeysForItem(item.asin);
			} catch (DAOException e) {
				e.printStackTrace();
			}
			for (KeyWord keyInList : keysToMove) {
				if ((System.currentTimeMillis() - keyInList.lastAdd) > (1000 * 60 * 60 * 24)
						&& checkedKeysToMove.size() <= keysAmount) {
					checkedKeysToMove.add(keyInList);
				}
			}
			if (checkedKeysToMove.isEmpty()) {
				log.log(Level.INFO, "Item " + item.asin + " List of keys is empty.");
				continue;
			}
			
			item.lastAdding = System.currentTimeMillis();
			try {
				ItemDAO.editItem(item.name, item.asin, item.name, item.asin, item.imgSource,
						item.keyWord, item.group, item.maxInCart, item.nowInCart, item.position,
						item.page, item.move, item.sellDate, item.lastAdding, item.price,
						item.ranking);
			} catch (DAOException e) {
				e.printStackTrace();
			}
			long timeAdd = item.lastAdding;
			int count = 0;
			for (KeyWord keyWord : checkedKeysToMove) {

				if (count < keysAmount && countOfThreads < threadsAmount) {
					if (keyWord.lastAdd == 0) {
						keyWord.addInDay = getRandomNumber(47, 50);
					} else {
						keyWord.addInDay = getRandomNumber(45, 50);
					}
					
					
					try {
						KeyDAO.editKey(keyWord.key, item.asin, keyWord.key, keyWord.addInDay, timeAdd,
								keyWord.itemLink);
						timer.waitSeconds(3);
						
					} catch (DAOException e) {
						e.printStackTrace();
					}
					try {
						KeyStatDAO.create(keyWord.key, timeAdd, 0, 0, 0, "", item.asin);
						timer.waitSeconds(3);
					} catch (DAOException e) {
						e.printStackTrace();
					}

					// set list of threads
					String profile = "Profile " + (countOfThreads + 1);
					Thread nextThread = getFreeThread(item, keyWord, profile);

					if (nextThread != null && !nextThread.isAlive()) {
						threads.add(nextThread);
						log.log(Level.INFO, "Thread " + nextThread.getName() + " started for item " + item.asin
								+ " and key " + keyWord.key);
						nextThread.start();
						count++;
						countOfThreads++;
						timer.waitSeconds(getRandomNumber(45, 60));
					} else {
						log.log(Level.INFO, "All threads are busy!!! Try again in 12 hours.");
						return;

					}
				}

			}
		}
		log.log(Level.INFO, "---------------------------------------\nAll threads started!!!\n" + "There are "
				+ threads.size() + " threads now. Wait for 12 hours to start new.");
		timer.waitSeconds(60 * 60 * 12); // wait 12h to kill all threads
		WebDriverFactory.dismissAll();
		timer.waitSeconds(10);
		for (Thread thread : threads) {
			if (thread.isAlive() || thread.isInterrupted()) {
				thread.stop();
			}
		}
		// claen system before new threads start
		File tempFile = new File("C:\\tomcat\\temp");
		deleteFile(tempFile);
		return;
	}

	public void deleteFile(File file) {

		if (!file.exists()) {
			return;
		}
		File[] files = file.listFiles();
		if (files.length != 0) {
			for (File fileIn : files) {
				if (fileIn.isDirectory()) {
					for (File fileToDel : fileIn.listFiles()) {
						fileToDel.delete();
					}
					fileIn.delete();
				} else {
					fileIn.delete();
				}
			}
		}
	}

	private Thread getFreeThread(Item item, KeyWord key, String profile) {
		Thread freeThread = new MoveItemByKey(item, key, profile);
		freeThread.setName("moveItemByKey-" + key.key);
		return freeThread;
	}

	public Account createNewAccount() {
		Account account = new Account();
		// get random account
		String accountName = getRandomAccName();
		String accountEmail = getRandomAccEmail(accountName);
		String accountPassword = "246813579";

		log.log(Level.INFO, "Method createNewAccount. Try to register account " + accountEmail);
		int atemptToCreate = 0;
		boolean accountIsNotCreated = true;
		while (accountIsNotCreated) {
			if (atemptToCreate > 3) {
				log.log(Level.INFO, "Method createNewAccount. Account " + accountEmail
						+ " wasn't registered in 3 atempts. Try it with other one in 3 minutes.");
				timer.waitSeconds(60);
				createNewAccount();
			}
			atemptToCreate++;
			log.log(Level.INFO,
					"Method createNewAccount. Try to register account " + accountEmail + " Atempt: " + atemptToCreate);
			WebDriver userDriver = getNewProxyFirefoxDriver();

			try {
				String startPageHTML = userDriver.getPageSource();
				timer.waitSeconds(10);
				if (startPageHTML.contains("Hello. Sign in")) {

					WebElement regLinkElement = userDriver.findElement(By.id("nav-flyout-ya-signin"));
					WebElement regLinkElementA = regLinkElement.findElement(By.tagName("a"));
					String loginlink = regLinkElementA.getAttribute("href");
					if (!loginlink.contains("https://www.amazon.com")) {
						loginlink = "https://www.amazon.com" + loginlink;
					}
					userDriver.get(loginlink);
					timer.waitSeconds(getRandomNumber(10, 20));
					WebElement newAccSubmit = userDriver.findElement(By.id("createAccountSubmit"));
					String reglink = newAccSubmit.getAttribute("href");
					if (!reglink.contains("https://www.amazon.com")) {
						reglink = "https://www.amazon.com" + loginlink;
					}
					userDriver.get(reglink);
					timer.waitSeconds(getRandomNumber(10, 20)); // timer.waitGetAction();
					WebElement formElement = userDriver.findElement(By.id("ap_register_form"));
					WebElement nameElement = userDriver.findElement(By.id("ap_customer_name"));
					nameElement.sendKeys(accountName);
					WebElement emailElement = userDriver.findElement(By.id("ap_email"));
					emailElement.sendKeys(accountEmail);
					WebElement passwordElement = userDriver.findElement(By.id("ap_password"));
					passwordElement.sendKeys(accountPassword);
					WebElement checkPasswordElement = userDriver.findElement(By.id("ap_password_check"));
					checkPasswordElement.sendKeys(accountPassword);
					timer.waitAction();
					formElement.submit();
					log.log(Level.INFO, "Method createNewAccount. Try to check registered account " + accountEmail);
					timer.waitSeconds(getRandomNumber(10, 20));
					userDriver.get("https://www.amazon.com");
					timer.waitSeconds(getRandomNumber(10, 20));
					// check for new account
					if (userDriver.getPageSource().contains("Hello, " + accountName)) {
						accountIsNotCreated = false;
						account.login = accountEmail;
						account.password = accountPassword;
						account.used = 0;

						WebDriverFactory.dismissDriver(userDriver);
						log.log(Level.INFO, "Method createNewAccount. Account " + account.login
								+ " was registered and sent to other method.");
						return account;
					} else {
						log.log(Level.INFO, "Method createNewAccount. Account was not registered. Trying again.");

						WebDriverFactory.dismissDriver(userDriver);
						timer.waitGetAction();
						createNewAccount();
					}
				} else {
					log.log(Level.INFO, "Method createNewAccount. Start page isn't loged out. Trying again.");

					WebDriverFactory.dismissDriver(userDriver);
					timer.waitGetAction();
					createNewAccount();
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Method createNewAccount. Start page didn't load. Trying again.");

				WebDriverFactory.dismissDriver(userDriver);
				timer.waitGetAction();
				createNewAccount();
			}
		}

		if (account.login == null || account.login.isEmpty()) {
			log.log(Level.INFO, "Method createNewAccount. Account login is - " + account.login);
			createNewAccount();
		}
		return account;
	}

	private int getRandomIndex(int size) {
		int index;
		index = (int) (Math.random() * (size - 1));
		// check that index is correct
		if (index > (size - 1)) {
			index = size - 1;
		}
		return index;
	}

	private String getRandomAccEmail(String accountName) {
		int randomDomainIndex;
		// random case for domain choose
		randomDomainIndex = 1 + (int) (Math.random() * (6 - 1));
		String randomEmail = accountName;
		switch (randomDomainIndex) {
		case 1:
			randomEmail += "@gmail.com";
			break;
		case 2:
			randomEmail += "@mail.com";
			break;
		case 3:
			randomEmail += "@hotmail.com";
			break;
		case 4:
			randomEmail += "@bigmir.net";
			break;
		case 5:
			randomEmail += "@zoho.eu";
			break;
		default:
			randomEmail += "@gmail.com";
			break;
		}
		return randomEmail;
	}

	private String getRandomAccName() {
		String randomName = getRandomName();

		String allSymbols = "0123456789";
		char[] symbols = allSymbols.toCharArray();

		int randomLength = getRandomNumber(7, 12);
		for (int i = 0; i < randomLength; i++) {
			int index = (int) (Math.random() * symbols.length);
			randomName += Character.toString(symbols[index]);
		}
		return randomName;
	}

	private String getRandomName() {
		String name = "";
		ArrayList<String> names = new ArrayList<>();
		names.add("Alex");
		names.add("Ali");
		names.add("Artur");
		names.add("Alla");
		names.add("Allan");
		names.add("Anna");
		names.add("Azur");
		names.add("Archi");
		names.add("Boris");
		names.add("Bogdan");
		names.add("Borek");
		names.add("Egor");
		names.add("Elena");
		names.add("Alina");
		names.add("Eva");
		names.add("Elsa");
		names.add("Ekaterina");
		names.add("Georg");
		names.add("Garik");
		names.add("Gustaf");
		names.add("Evgen");
		names.add("Leonid");
		names.add("Michael");
		names.add("Marina");
		names.add("Peter");
		names.add("Mirabella");
		names.add("Maria");
		names.add("Maks");
		names.add("Maksim");
		names.add("Anastasia");
		names.add("Nastya");
		names.add("Nikita");
		names.add("Taras");
		names.add("Teodor");
		names.add("Timofey");
		names.add("Serg");
		names.add("Sergio");
		names.add("Oleg");
		names.add("Olga");
		names.add("Andrey");
		names.add("Victor");
		names.add("Victoria");
		names.add("Vitaliy");
		names.add("Vitalina");
		names.add("Vlad");
		names.add("Valeria");
		names.add("Valeriy");
		names.add("Ivan");
		names.add("Roman");
		names.add("Yana");
		names.add("Yarik");
		names.add("Yaroslav");
		names.add("Svetlana");
		names.add("Nataly");
		names.add("Yan");
		name = names.get(getRandomIndex(names.size()));
		return name;
	}

	private WebDriver getNewProxyFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", "C:\\java\\selenium\\geckodriver.exe");

		ProfilesIni WSP = new ProfilesIni();
		FirefoxProfile profile = WSP.getProfile("amazon");

		// set proxy
		ArrayList<ProxyForUse> proxies = getAllProxies();

		ProxyForUse proxyForUse = proxies.get(getRandomIndex(proxies.size()));

		profile.setPreference("network.proxy.type", 1);
		profile.setPreference("network.proxy.http", proxyForUse.getProxy());
		profile.setPreference("network.proxy.http_port", proxyForUse.getPort());

		try {
			ProxiesDAO.deleteProxy(proxyForUse.getProxy(), proxyForUse.getPort());
		} catch (DAOException e1) {
			e1.printStackTrace();
		}
		// set other preferences
		profile.setPreference("permissions.default.image", 2);

		DesiredCapabilities firefox = DesiredCapabilities.firefox();
		firefox.setCapability(FirefoxDriver.PROFILE, profile);

		timer.waitSeconds(10);
		WebDriver driver = WebDriverFactory.getDriver(firefox);
		driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
		try {

			timer.waitSeconds(15);
			driver.get("https://www.amazon.com");

			timer.waitSeconds(getRandomNumber(5, 10)); // it was 5-10 sec before

			// check driver
			String currentPage = driver.getPageSource();
			if (currentPage.contains("nav-flyout-ya-newCust")) {
				return driver;
			} else {
				log.log(Level.INFO, "Method getNewFirefoxDriver. Web driver wasn't created. Try again");
				timer.waitSeconds(getRandomNumber(180, 300)); // it was 5-10 sec
																// before
				getNewProxyFirefoxDriver();
			}

		} catch (Exception e) {
			log.log(Level.INFO, "Method getNewFirefoxDriver. Web driver wasn't created.");
			WebDriverFactory.dismissDriver(driver);
			getNewProxyFirefoxDriver();
		}

		return driver;
	}

	private ArrayList<ProxyForUse> getAllProxies() {
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

	public static int getRandomNumber(int min, int max) {
		int number = (int) (min + (Math.random() * (max - min)));
		return number;
	}
}
