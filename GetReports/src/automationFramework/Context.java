package automationFramework;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Context {
	public WebDriver webDriver;
	public String ipAddress;
	public String pwString;
	public String printerName;
	public WebDriverWait waitShort;
	public WebDriverWait waitLong;
	
	public Context(WebDriver webDriver, WebDriverWait waitShort, WebDriverWait waitLong,String ipAddress, String pwString, String printerName){
		this.ipAddress = ipAddress;
		this.pwString = pwString;
		this.printerName = printerName;
		this.webDriver = webDriver;
		this.waitShort = waitShort;
		this.waitLong = waitLong;
	}
}
