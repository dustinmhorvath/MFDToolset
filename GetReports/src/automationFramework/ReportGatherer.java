package automationFramework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.xmlbeans.impl.common.Mutex;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ReportGatherer {
	
	// Used for webDriver timeout intervals, in seconds
	private static int LONGWAIT = 20;
	private static int SHORTWAIT = 3;
	
	private String pathToLogFile;
	private String logFileName = "log.txt";
	private String downloadBaseFilepath;
	private HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
	private ChromeOptions options = new ChromeOptions();
	private DesiredCapabilities cap = DesiredCapabilities.chrome();
	private ExcelReader reader;
	private int length;
	private int ipCol;
	private int pwCol;
	private int nameCol;
	private LoggingTool logger;
	private Mutex webDriverLock;
	FileAlterationMonitor monitor;
	
	/**
	    * Constructs a ReportGatherer and opens the excel file provided
	    * @param String Absolute path, including filename, of the excel sheet
	    * @param String The root filepath to which reports will be downloaded
	    * @param Integer The 0-indexed column containing printer IPs
	    * @param Integer The 0-indexed column containing printer passwords
	    * @param Integer The 0-indexed column containing printer names
	    * @throws Exception 
	    */
	public ReportGatherer(String mfdList, String basePath, int ipColumnIndex, int pwColumnIndex, int printerColumnIndex) throws Exception  {
		ipCol = ipColumnIndex;
		pwCol = pwColumnIndex;
		nameCol = printerColumnIndex;
		downloadBaseFilepath = basePath;
		pathToLogFile = basePath + "/log";
		monitor = new FileAlterationMonitor();
		webDriverLock = new Mutex();
				
		monitor.start();
		
		System.setProperty("webdriver.chrome.driver", "tools/chromedriver.exe");
		try {
			reader = new ExcelReader(mfdList);
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException("Couldn't find filelist");
		} catch (IOException e) {
			throw new IOException("Error opening filelist");
		}
		length = reader.getRows();
		try{
			logger = new LoggingTool(pathToLogFile, logFileName);
			logger.logInfPrint("Initialized LoggingTool.");
		}
		catch(IOException e){
			throw new IOException("ERROR Couldn't create log file.");
		}
		
		
	}
	
	public void close(){
		try {
			monitor.stop();
		} catch (Exception e) {
			logger.logErrPrint("Monitor failed to shutdown");
		}
		logger.close();
	}
	
	/**
	    * Loads, logs in, navigates, downloads, and logs out from a single MFD, provided via its row index within the excel sheet
	    * @param index the row index of the MFD within the excel sheet, starting at index 0, which is row B.
	    */
	public void retrieveReportByIndex(int index){
		String printerName = reader.getValueAt(index, nameCol);
		Context context;
		boolean success = false;
		
		synchronized(webDriverLock){
			context = getContext(index, printerName);
			logger.logInfPrint(Thread.currentThread().toString() + " started");
			logger.logInfPrint(context.ipAddress + " " + context.printerName + " started");
		}
		
		if(preliminary_mfdLoad(context) == 0) {
			if(loginPage_mfdLogin(context) == 0) {
				if(adminSystemPage_mfdImportExport(context) == 0){
					if(importExport_mfdCounterMenu(context) == 0){
						if(counterExportMenu_mfdUserCounterSelection(context) == 0){
							if(downloadScreen_mfdDownloadButton(context) == 0){
								success = true;
							}
						}
					}
					if(importExport_mfdCounterMenu(context) == 0){
						if(counterExportMenu_mfdAccountCounterSelection(context) == 0){
							if(downloadScreen_mfdDownloadButton(context) == 0){
								success = true;
							}
						}
					}
				}
				mfdLogout(context);
			}
		}
		
		destroyContext(context);
		if(!success){
			logger.logFailPrint(String.valueOf(index));
		}
	}
	
	/**
	    * Loads, logs in, navigates, downloads, and logs out from a single MFD, provided via its printer name in the excel sheet.
	    * @param name Printer name of the MFD within the excel sheet.
	    */
	public void retrieveReportByPrinterName(String name){
		int index = retrievePrinterIndexByName(name);
		if(index >=0){
			retrieveReportByIndex(index);
		}
		else return;
	}
	
	/**
	    * Returns the row index of a printer when given a printer name. Returns -1 on an error.
	    * @param name Printer name of the MFD within the excel sheet.
	    * @return int Index of the printer if found, or -1 if not.
	    */
	public int retrievePrinterIndexByName(String name){
		for(int i = 0; i < length; i++){
			if(name.equalsIgnoreCase(reader.getValueAt(i, nameCol))){
				return i;
			}
		}
		return -1;
	}
	
	/**
	    * Creates a download subdirectory, and a ChromeDriver webdriver
	    * @param printerName name of the printer, which will be used to create a subdirectory by its name
	    * @return Context containing the tools needed for navigating printer menus.
	    */
	private Context getContext(int index, String printerName){
		
		chromePrefs.put("profile.default_content_settings.popups", 0);
		
		try{
			File currentFolder = new File(downloadBaseFilepath);
	        File workingFolder = new File(currentFolder, printerName);
	        if (!workingFolder.exists()) {
	            workingFolder.mkdir();
	        }
		}
		catch(Exception e){
			logger.logErrPrint("couldn't create a context");
		}
		
	    chromePrefs.put("download.default_directory", downloadBaseFilepath +  "/" + printerName);
	    
	    HashMap<String, Object> chromeOptionsMap = new HashMap<String, Object>();
	    options.setExperimentalOption("prefs", chromePrefs);
	    options.addArguments("--test-type");
	    
	    cap.setCapability(ChromeOptions.CAPABILITY, chromeOptionsMap);
	    cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
	    cap.setCapability(ChromeOptions.CAPABILITY, options);
		
		WebDriver webDriver = new ChromeDriver(cap);
		WebDriverWait waitLong = new WebDriverWait(webDriver, LONGWAIT);
		WebDriverWait waitShort = new WebDriverWait(webDriver, SHORTWAIT);
		
		String ip = reader.getValueAt(index, ipCol);
		String pw = reader.getValueAt(index, pwCol);
		
		Context context = new Context(webDriver, waitShort, waitLong, ip, pw, printerName);
		return context;
			
	}
	
	/**
	    * Destroys the webdriver given a context
	    */
	private void destroyContext(Context context){
		context.webDriver.close();
		context.webDriver.quit();
	}
	
	/**
	    *  Loads the login page privided an ip address
	    *  @param context A context containing relevant printer information
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int preliminary_mfdLoad(Context context){
		try{
			context.webDriver.navigate().to("http://" + context.ipAddress + "/wcd/top.xml");
		}
		
		/*try{
			WebElement error_message_element = context.webDriver.findElement(By.xpath("//*[@id=\"main-message\"]/div[2]"));
			
			if(error_message_element.getAttribute("jscontent") == "errorCode"){
				logger.logErrPrint(context.ipAddress + " couldn't load login page");
				return 1;
			}
		}
		catch(NoSuchElementException e){
			// Means we didn't hit an error page
			System.out.print("");
		}*/
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " couldn't load login page");
			e.printStackTrace();
			return 1;
		}
		
		return 0;
	}
	
	/**
	    * Logs into the printer system page as an administrator
	    *  @param context A context containing relevant printer information
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int loginPage_mfdLogin(Context context){
		try{
			WebElement adminButton = context.webDriver.findElement(By.id("Admin"));
			adminButton.click();
			
			context.waitLong.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"LP0LOG\"]/table[1]/tbody/tr/td/input"))).sendKeys(Keys.RETURN);
			
			context.waitLong.until(ExpectedConditions.elementToBeClickable(By.id("R_ADM1"))).click();
			WebElement passwordField = context.webDriver.findElement(By.id("Admin_Pass"));
			passwordField.sendKeys(context.pwString);
			passwordField.sendKeys(Keys.RETURN);
			
			/*PublicTools.sleep();
			
			if(!context.waitShort.getCurrentUrl().toLowerCase().contains("a_system") ){
				logger.logErrPrint(context.ipAddress + " couldn't reach system page");
				return 1;
			}*/
			
		
		}
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " failed to load page");
			return 1;
		}
		
		logger.logInfPrint(context.ipAddress + " logged in");
		return 0;
		
	}
	
	/**
	    *  Selects the import/export submenu
	    *  @param context A context containing relevant printer information
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int adminSystemPage_mfdImportExport(Context context){
		try{
			context.waitLong.until(ExpectedConditions.elementToBeClickable(By.linkText("Import/Export"))).click();	
		}
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " failed to find import/export");
			return 1;
		}
		
		return 0;
		
	}
	
	/**
	    *  Modify the number of seconds before timeout on any driver.waitLong() 
	    *  private method calls. Used for operations such as initial page load and login, and defaults to 20 seconds. 
	    *  Does not take effect until a new context is created.
	    *  @param seconds Number of seconds to be used as the timeout on any LONGWAIT driver actions.
	    */
	public void setWaitLong(int seconds){
		if(seconds > 0){
			LONGWAIT = seconds;
		}
	}
	
	/**
	    *  Modify the number of seconds before timeout on any driver.waitShort()
	    *  private method calls. Used for operations such as navigating counter menus, and other clicks
	    *  that typically respond and load quickly. Default to 3 seconds. Does not take effect until a new context is created.
	    *  @param seconds Number of seconds to be used as the timeout on any SHORTWAIT driver actions.
	    *  
	    */
	public void setWaitShort(int seconds){
		SHORTWAIT = seconds;
	}
	
	/**
	    *  Selects the 'Counter' export option and clicks 'export'.
	    *  @param index The row index of the MFD within the excel sheet, starting at index 0, which is row B. Used for finding the ip for logging.
	    *  @return Returns 0 on success, nonzero on an error.
	    */
	private int importExport_mfdCounterMenu(Context context){
		// Click on "counter"
		try{
			context.waitLong.until(ExpectedConditions.elementToBeClickable(By.id("R_SEL3"))).click();
			
			// Click on "Ok"
			try{
				context.waitLong.until(ExpectedConditions.elementToBeClickable(By.id("ExportButton"))).click();
			}
			catch(Exception e){
				logger.logErrPrint(context.ipAddress + " failed to select OK");
				return 1;
			}
		}
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " failed to select counter");
			return 1;
		}
		
		
		return 0;
	}
	
	/**
	    *  Selects the 'User Counter' export option and clicks 'ok'
	    *  @param context A context containing relevant printer information
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int counterExportMenu_mfdUserCounterSelection(Context context){
		// Click on "User Counter"
		try{
			context.waitShort.until(ExpectedConditions.elementToBeClickable(By.id("R_SEL_C2Export"))).click();
			
			// Click on "Ok"
			try{
				context.waitShort.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"AS_CNL_EXExport\"]/div[2]/input[1]"))).click();
			}
			catch(Exception e){
				logger.logErrPrint(context.ipAddress + " failed to select OK");
				return 1;
			}
			
		}
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " failed to find User Counter");
			return 1;
		}
		
		logger.logInfPrint(context.ipAddress + " downloading user counter");
		return 0;
	}
	
	/**
	    *  Selects the 'Account Counter' export option and clicks 'ok'
	    *  @param context A context containing relevant printer information
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int counterExportMenu_mfdAccountCounterSelection(Context context){
		// Click on "User Counter"
		try{
			context.waitShort.until(ExpectedConditions.elementToBeClickable(By.id("R_SEL_C3Export"))).click();
			
			// Click on "Ok"
			try{
				context.waitShort.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"AS_CNL_EXExport\"]/div[2]/input[1]"))).click();
			}
			catch(Exception e){
				logger.logErrPrint(context.ipAddress + " failed to select OK");
				return 1;
			}
			
		}
		catch(Exception e){
			logger.logInfPrint(context.ipAddress + " failed to find Account Counter");
			return 1;
		}
		
		logger.logInfPrint(context.ipAddress + " downloading user counter");
		return 0;
	}
	
	
	
	/**
	    *  Hits the download button, downloads the file, waits, then hits the back button.
	    *  @param context A context containing relevant printer information.
	    *  @return int returns 0 on success, nonzero on an error.
	    */
	private int downloadScreen_mfdDownloadButton(Context context){
		final Boolean[] flag = new Boolean[1];
		flag[0] = false;

		FileAlterationObserver observer = new FileAlterationObserver(downloadBaseFilepath + "/" + context.printerName);
		FileAlterationListener listener = new FileAlterationListenerAdaptor() {
		    public void onFileChange(File file) {
		    	if(file.getName().endsWith(".txt")){
		    		flag[0] = true;
		    	}
		    }
		};
		
		observer.addListener(listener);
		monitor.addObserver(observer);
		
		try{		    
			context.waitLong.until(ExpectedConditions.elementToBeClickable(By.id("btnEXE"))).click();
		}
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " failed to find download button");
			return 1;
		}
		
		int interval = 1;
		int attempts = 20;
		for(int i = 0; i < attempts; i++){
			if(flag[0]){
				break;
			}
			else{
				try {
					Thread.sleep(interval * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		monitor.removeObserver(observer);
		observer.removeListener(listener);
		
		try{
			context.waitLong.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"btnOK\"]"))).click();
			return 0;
		}
		catch(Exception e1){
			try{
				context.waitLong.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"downloadbtnOK\"]"))).click();
			}
			catch(Exception e2){
				logger.logErrPrint(context.ipAddress + " failed to find back button");
				return 1;
			}
		}
				
		return 1;
	}
	
	/**
	    *  Logs out of the current printer.
	    *  @param context A context containing relevant printer information.
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int mfdLogout(Context context){
		try{
			context.waitShort.until(ExpectedConditions.elementToBeClickable(By.id("ALogout_Button"))).click();
			context.waitShort.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"AS_LO\"]/div[2]/input[1]"))).click();
		}
		catch(Exception e){
			logger.logErrPrint(context.ipAddress + " couldn't log out");
			return 1;
		}
		
		logger.logInfPrint(context.ipAddress + " logged out");
		return 0;
	}

	public int length(){
		return length;
	}

}
