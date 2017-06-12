package com.amazoninvestorclub.beans;

import java.io.BufferedReader;
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

import com.amazoninvestorclub.DAO.AccountDAO;
import com.amazoninvestorclub.DAO.DAOException;
import com.amazoninvestorclub.DAO.ProxiesDAO;
import com.amazoninvestorclub.domain.Account;
import com.amazoninvestorclub.domain.ProxyForUse;
import com.amazoninvestorclub.domain.WaitTimer;

import ru.stqa.selenium.factory.WebDriverFactory;

public class AccountCreator extends Thread {

	private static Logger log = Logger.getLogger(AccountCreator.class.getName());
	WaitTimer timer = new WaitTimer();
	ProxiesDAO prDAO = new ProxiesDAO();
	
	@Override
	public void run() {
		log.log(Level.INFO, "Thread AccountCreator have started");
		timer.waitSeconds(20);
		while (true) {
			Account account = createNewAccount();
			try {
				AccountDAO.create(account.login, account.password, 0);
			} catch (DAOException e) {
				e.printStackTrace();
			}
			timer.waitSeconds(getRandomNumber(300, 600));
		}

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
			WebDriver userDriver = getProxyFirefoxDriver();

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

						userDriver.quit();
						log.log(Level.INFO, "Method createNewAccount. Account " + account.login
								+ " was registered and sent to other method.");
						return account;
					} else {
						log.log(Level.INFO, "Method createNewAccount. Account was not registered. Trying again.");

						userDriver.quit();
						timer.waitGetAction();
						createNewAccount();
					}
				} else {
					log.log(Level.INFO, "Method createNewAccount. Start page isn't loged out. Trying again.");

					userDriver.quit();
					timer.waitGetAction();
					createNewAccount();
				}
			} catch (Exception e) {
				log.log(Level.WARNING, "Method createNewAccount. Start page didn't load. Trying again.");

				userDriver.quit();
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

	private WebDriver getProxyFirefoxDriver() {
		System.setProperty("webdriver.gecko.driver", "C:\\java\\selenium\\geckodriver.exe");

		ProfilesIni WSP = new ProfilesIni();
		FirefoxProfile profile = WSP.getProfile("amazon");
		
		// set proxy
		ArrayList<ProxyForUse> proxies = getAllProxies();
		
		int rndIndex = getRandomIndex(proxies.size());
		
		ProxyForUse proxyForUse = proxies.get(rndIndex);
		
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
				getProxyFirefoxDriver();
			}

		} catch (Exception e) {
			log.log(Level.INFO, "Method getNewFirefoxDriver. Web driver wasn't created.");
			driver.quit();
			getProxyFirefoxDriver();
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

	public int getRandomNumber(int min, int max) {
		int number = (int) (min + (Math.random() * (max - min)));
		return number;
	}

}
