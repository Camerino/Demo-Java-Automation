package com.orasi.bluesource.titlesPage;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import ru.yandex.qatools.allure.annotations.Step;

import com.orasi.api.restServices.blueSource.BlueSource;
import com.orasi.api.restServices.blueSource.titles.Title;
import com.orasi.core.interfaces.Element;
import com.orasi.core.interfaces.Label;
import com.orasi.core.interfaces.Link;
import com.orasi.core.interfaces.Webtable;
import com.orasi.core.interfaces.impl.ElementImpl;
import com.orasi.core.interfaces.impl.internal.ElementFactory;
import com.orasi.exception.automation.ElementNotVisibleException;
import com.orasi.utils.AlertHandler;
import com.orasi.utils.OrasiDriver;
import com.orasi.utils.TestEnvironment;
import com.orasi.utils.TestReporter;

public class ListingTitlesPage {
    	private OrasiDriver driver = null;
	
	//All the page elements
	@FindBy(linkText = "New Title")	private Link lnkNewTitle;	
	@FindBy(xpath = "//h1[text() = 'Listing titles']") private Label lblTitle;
	@FindBy(css = ".alert-success.alert-dismissable") private Label lblSuccessMsg;	
	@FindBy(className = "table") private Webtable tabTitles;
	
	private By editIcon = By.cssSelector("div:nth-child(1) > a:nth-child(1)");
	private By deleteIcon = By.cssSelector("div:nth-child(1) > a:nth-child(2)");
	
	// *********************
	// ** Build page area **
	// *********************
	public ListingTitlesPage(OrasiDriver driver){
	    this.driver = driver;
	    ElementFactory.initElements(driver, this);
	}	
	
	public boolean pageLoaded(){
	    return driver.page().pageLoaded(this.getClass(), lnkNewTitle);
	}

	// *****************************************
	// ***Page Interactions ***
	// *****************************************

	@Step("Click the \"New Title\" link")
	public void clickNewTitle(){
	    lnkNewTitle.click();
	}
		
	public boolean isTitleHeaderDisplayed(){
	    return lblTitle.isDisplayed();
	}
	
	@Step("Click the \"Edit Title\" icon on the row for title \"{0}\"")
	public void clickModifyTitle(String title){	    
		driver.findElement(By.xpath("//table/tbody/tr/td[contains(text(),'"+title+"')]/div/a/span[contains(@class,'glyphicon-pencil')]")).click();
	}
	
	@Step("An alert should appear for confirmation")
	public boolean isSuccessMsgDisplayed() {
		boolean displayed;
		try {
			displayed = lblSuccessMsg.syncVisible();
			return displayed;

		} catch (ElementNotVisibleException e){
			return false;
		}
	   
	}
	
	@Step("The title \"{0}\" should be found on the Titles table")
	public boolean searchTableByTitle(String title){
	    if(getTitleRowPosition(title) > 0) return true;
	    return false;
	}
	
	@Step("Delete the title from the table")
	public void deleteTitle(String title, String browser){
		//8/15/2016 Safari driver does not currently handle modal alerts.  This is a work around to accept the alert
		// see issue in github for details: https://github.com/seleniumhq/selenium-google-code-issue-archive/issues/3862
		if (browser.equalsIgnoreCase("safari")){
			driver.executeJavaScript("confirm = function(message){return true;};");
			driver.executeJavaScript("alert = function(message){return true;};");
			driver.executeJavaScript("prompt = function(message){return true;}");
			driver.findElement(By.xpath("//table/tbody/tr/td[contains(text(),'"+title+"')]/div/a/span[contains(@class,'glyphicon-trash')]")).click();
		}
		else {
			driver.findElement(By.xpath("//table/tbody/tr/td[contains(text(),'"+title+"')]/div/a/span[contains(@class,'glyphicon-trash')]")).click();
			AlertHandler.handleAllAlerts(driver, 2);
		}
	}

	public void ensureNoExistingTitle(String title){
	    if (searchTableByTitle(title)){
		BlueSource blueSource = new BlueSource("Company.admin");
		List<Title>  titles = blueSource.titles().getAllTitles();
		Title tempTitle = null;
		Iterator<Title> titleIterator = titles.iterator();
		while (titleIterator.hasNext()) {
		    tempTitle = titleIterator.next();
		    if(tempTitle.getName().equals(title)) blueSource.titles().deleteTitle(tempTitle);
		}
		TestReporter.log("The title of \"" + title + "\" previously existed. Deleting previous title");
		
	    }
	}
	
	private WebElement getTitleRowElement(String title){
	    int titleRow = getTitleRowPosition(title);
	    return tabTitles.getCell( titleRow, 1);
	}
	
	private int getTitleRowPosition(String title){
	    return tabTitles.getRowWithCellText(title, 1,1,false);
	}
}
