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
	
	/**
	 * Encapsulates all the information needed to perform operations with an MFD.
	 * @param webDriver The webdriver operating this particular printer. Required so that download locations can be controlled.
	 * @param waitShort The duration to wait for "shorter" actions, like navigating tables and lists. Typically the ones which can be counted on to complete in 1-2 seconds.
	 * @param waitLong The duration to wait for "longer" actions, like loading webpaged or logins. Typically the ones which might take up to 10 or 15 seconds to complete.
	 * @param ipAddress Address of the printer's web interface.
	 * @param pwString String containing the admin login password for the printer.
	 * @param printerName The name of the printer in question. Used for logging and for storing any downloaded files.
	 */
	public Context(WebDriver webDriver, WebDriverWait waitShort, WebDriverWait waitLong, String ipAddress, String pwString, String printerName){
		this.ipAddress = ipAddress;
		this.pwString = pwString;
		this.printerName = printerName;
		this.webDriver = webDriver;
		this.waitShort = waitShort;
		this.waitLong = waitLong;
		
	}
}
