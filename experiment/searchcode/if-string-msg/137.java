package com.myfitnesspal.qa.foundation;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.myfitnesspal.qa.foundation.log.TestSummaryCollector;
import com.myfitnesspal.qa.foundation.log.TestSummaryHelper;
import com.myfitnesspal.qa.foundation.util.Screenshot;
import com.myfitnesspal.qa.pages.Page;
import com.myfitnesspal.qa.utils.LogicUtils;
import com.myfitnesspal.qa.utils.Messager;
import com.myfitnesspal.qa.utils.R;
import com.thoughtworks.selenium.Selenium;

public class DriverHelper {

	protected static final Logger LOGGER = Logger.getLogger(DriverHelper.class);

	private static final long MAX_TIMEOUT = R.CONFIG.getLong("impl.timeout");

	private static final long RETRY_TIME = R.CONFIG.getLong("retry.timeout");

	private long timer;

	private static Wait<WebDriver> wait;

	protected TestSummaryHelper summary;

	protected WebDriver driver;

	public static boolean isChrome = false;

	public static boolean isFirefox = false;

	public static boolean isIE = false;

	public DriverHelper() {
		initBrowser();
	}

	public DriverHelper(WebDriver driver) {
		this.driver = driver;
		initSummary(driver);
		initBrowser();
	}

	protected void initSummary(WebDriver driver) {
		summary = new TestSummaryHelper(driver);
	}

	public void type(String controlInfo, final WebElement control, String text) {
		String msg;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return control.isDisplayed();
				}
			});
			control.clear();
			control.sendKeys(text);
			msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, controlInfo);
			summary.write(msg);
		} catch (Exception e) {
			msg = Messager.KEYS_NOT_SEND_TO_ELEMENT.error(text, controlInfo);
			summary.write(msg);
			Assert.fail(msg);
		}
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public void openURL(String relURL) {
		relURL = relURL.contains("http:") ? relURL : ConfigArgs.getURL() + relURL;
		driver.get(relURL);
		String msg = Messager.OPEN_URL.info(relURL);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public boolean isUrlAsExpected(String expected) {
		expected = expected.contains("http:") ? expected : ConfigArgs.getURL() + expected;
		if (LogicUtils.isURLEqual(expected, driver.getCurrentUrl())) {
			summary.write(Messager.EXPECTED_URL.info(driver.getCurrentUrl()));
			return true;
		} else {
			Messager.UNEXPECTED_URL.error(expected, driver.getCurrentUrl());
			return false;
		}
	}

	public void pause(long timeout) {
		try {
			Thread.sleep(timeout * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void click(String controlInfo, WebElement control) {
		// ff sometimes need press enter
		clickSafe(controlInfo, control, true);
		String msg = Messager.ELEMENT_CLICKED.info(controlInfo);
		summary.write(msg);
		try {
			TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}
	}

	private void clickSafe(String controlInfo, WebElement element, boolean startTimer) {

		if (startTimer) {
			timer = System.currentTimeMillis();
		}
		try {
			Thread.sleep(RETRY_TIME);
			element.click();
		} catch (Exception e) {
			if (System.currentTimeMillis() - timer < MAX_TIMEOUT * 1000) {
				clickSafe(controlInfo, element, false);
			} else {
				Assert.fail(Messager.ELEMENT_NOT_CLICKED.error(controlInfo));
			}
		}
	}

	public void pressEnter(String controlInfo, WebElement control) {
		pressEnterSafe(controlInfo, control, true);
		String msg = Messager.ELEMENT_CLICKED.info(controlInfo);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public void pressEnterOrClickInFF(String controlInfo, WebElement control) {
		if (isFirefox) {
			clickSafe(controlInfo, control, true);
		} else {
			pressEnterSafe(controlInfo, control, true);
		}
		String msg = Messager.ELEMENT_CLICKED.info(controlInfo);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public void clickOPressEnterInFF(String controlInfo, WebElement control) {
		if (isFirefox) {
			pressEnterSafe(controlInfo, control, true);
		} else {
			clickSafe(controlInfo, control, true);
		}
		String msg = Messager.ELEMENT_CLICKED.info(controlInfo);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	private void pressEnterSafe(String controlInfo, WebElement element, boolean startTimer) {

		if (startTimer) {
			timer = System.currentTimeMillis();
		}
		try {
			Thread.sleep(RETRY_TIME);
			element.sendKeys(Keys.ENTER);
		} catch (Exception e) {
			if (System.currentTimeMillis() - timer < MAX_TIMEOUT * 1000) {
				pressEnterSafe(controlInfo, element, false);
			} else {
				Assert.fail(Messager.ELEMENT_NOT_CLICKED.error(controlInfo));
			}
		}
	}

	public boolean isTitleAsExpected(final String title) {
		boolean result;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return title.equals(driver.getTitle());
				}
			});
			result = true;
			summary.write(Messager.TITLE_CORERECT.info(driver.getCurrentUrl(), title));
		} catch (Exception e) {
			result = false;
			summary.write(Messager.TITLE_NOT_CORERECT.error(driver.getCurrentUrl(), title, driver.getTitle()));
		}
		return result;
	}

	public boolean isTitleAsExpectedPattern(String url, String pattern) {
		boolean result;
		String actual = driver.getTitle();
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(actual);
		if (m.find()) {
			summary.write(Messager.TITLE_CORERECT.info(driver.getCurrentUrl(), actual));
			result = true;
		} else {
			summary.write(Messager.TITLE_DOES_NOT_MATCH_TO_PATTERN.error(driver.getCurrentUrl(), pattern, actual));
			result = false;
		}
		return result;
	}

	public boolean isElementPresent(String controlInfo, final WebElement element) {
		boolean result;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return element.isDisplayed();
				}
			});
			result = true;
			summary.write(Messager.ELEMENT_PRESENT.info(controlInfo));
		} catch (Exception e) {
			result = false;
			summary.write(Messager.ELEMENT_NOT_PRESENT.error(controlInfo));
		}
		return result;
	}

	public boolean isElementPresent(String controlInfo, final WebElement element, long maxWait) {
		boolean result;
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		wait = new WebDriverWait(driver, maxWait, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return element.isDisplayed();
				}
			});
			result = true;
		} catch (Exception e) {
			result = false;
		}
		driver.manage().timeouts().implicitlyWait(MAX_TIMEOUT, TimeUnit.SECONDS);
		return result;
	}

	public boolean isElementWithTextPresent(String controlInfo, final WebElement element, final String text) {
		boolean result;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return element.isDisplayed() && element.getText().contains(text);
				}
			});
			result = true;
			summary.write(Messager.ELEMENT_WITH_TEXT_PRESENT.info(controlInfo, text));
		} catch (Exception e) {
			result = false;
			summary.write(Messager.ELEMENT_WITH_TEXT_NOT_PRESENT.error(controlInfo, text));
		}
		return result;
	}

	public boolean isElementNotPresent(String controlInfo, final WebElement element) {
		boolean result;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return element == null || !element.isDisplayed();
				}
			});
			result = true;
		} catch (Exception e) {
			result = false;
			summary.write(Messager.UNEXPECTED_ELEMENT_PRESENT.error(controlInfo));
		}
		return result;
	}

	public boolean isPageOpened(final Page page) {
		boolean result;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return LogicUtils.isURLEqual(page.getPageURL(), driver.getCurrentUrl());
				}
			});
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public void navigateBack() {
		driver.navigate().back();
		summary.write(Messager.BACK.info());
	}

	public void setElementText(String controlInfo, String frame, String id, String text) {
		((JavascriptExecutor) driver).executeScript(String.format(
				"document.getElementById('%s').contentWindow.document.getElementById('%s').innerHTML='%s'", frame, id,
				text));
		String msg = Messager.KEYS_SEND_TO_ELEMENT.info(text, controlInfo);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public void select(String controlInfo, WebElement select, String selectText) {
		Select s = new Select(select);
		s.selectByVisibleText(selectText);
		String msg = Messager.SELECT_TEXT.info(selectText, controlInfo);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public void select(String controlInfo, WebElement select, int index) {
		Select s = new Select(select);
		s.selectByIndex(index);
		String msg = Messager.SELECT_INDEX.info(String.valueOf(index), controlInfo);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public void hover(String xpathLocator, String elementName) {
		Selenium seleniumDriver = new WebDriverBackedSelenium(driver, driver.getCurrentUrl());
		seleniumDriver.mouseOver(String.format("xpath=%s", xpathLocator));
		String msg = Messager.HOVER_IMG.info(elementName);
		summary.write(msg);
		TestSummaryCollector.pushScreenStep(Screenshot.take(driver), msg);
	}

	public boolean isElementNotNull(String elementName, By by) {
		String msg;
		boolean res;
		try {
			driver.findElement(by);
			res = true;
			msg = Messager.ELEMENT_PRESENT.info(elementName);
		} catch (Exception e) {
			res = false;
			msg = Messager.ELEMENT_NOT_PRESENT.error(elementName);
		}
		summary.write(msg);
		return (res);
	}

	public boolean isElementNull(String elementName, By by) {
		boolean res = driver.findElements(by).size() == 0;
		String msg;
		if (res) {
			msg = Messager.ELEMENT_NOT_PRESENT_PASS.info(elementName);
		} else {
			msg = Messager.UNEXPECTED_ELEMENT_PRESENT.error(elementName);
		}
		summary.write(msg);
		return (res);
	}

	public WebElement getElement(By by) {
		return driver.findElement(by);
	}

	public WebElement getVisibleElement(final By by) {
		WebElement result = null;
		wait = new WebDriverWait(driver, MAX_TIMEOUT, RETRY_TIME);
		try {
			wait.until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver dr) {
					return ((RemoteWebElement) dr.findElement(by)).isDisplayed() != false;
				}
			});
			result = driver.findElement(by);
		} catch (Exception e) {
			// none
		}
		return result;
	}

	public void sielentAlert() {
		if (!(driver instanceof HtmlUnitDriver)) {
			((JavascriptExecutor) driver).executeScript("window.alert = function(msg) { return true; }");
			((JavascriptExecutor) driver).executeScript("window.confirm = function(msg) { return true; }");
			((JavascriptExecutor) driver).executeScript("window.prompt = function(msg) { return true; }");
		}
	}

	private void initBrowser() {
		String browser = ConfigArgs.getBrowser();
		if (R.CONFIG.get("firefox.browser").equals(browser)) {
			isFirefox = true;
		} else if (R.CONFIG.get("ie.browser").equals(browser)) {
			isIE = true;
		} else {
			isChrome = true;
		}
	}
	
	protected double getDouble(String num)
	{
		return Double.valueOf(num.replace(",", ""));
	}
}

