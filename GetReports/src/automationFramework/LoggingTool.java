package automationFramework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class LoggingTool implements AutoCloseable {
	private String logName = "log.txt";
	private String logPath;
	private String logFileWithPath;
	private String retryLogName = "retry.txt";
	private String failureLogFileWithPath;
	private PrintWriter logWriter;
	private PrintWriter failWriter;
	
	/**
	 * Instantiates a LoggingTool. Attempts to create a FileWriter and open a new file in the directory provided.
	 * @param logFilepath Absolute or relative path to the directory to be used for logging.
	 * @throws IOException Errors if it fails to write on the directory provided, typically due to write permission on the target path.
	 */
	public LoggingTool(String logFilepath) throws IOException {
		this.logPath = logFilepath;
		this.logFileWithPath = logFilepath + "/" + new Date().getTime() + "-" + logName;
		this.failureLogFileWithPath = logFilepath + "/" + new Date().getTime() + "-" + retryLogName;
		
		File folder = new File(logFilepath);
        if (!folder.exists()) {
        	folder.mkdir();
	    }
        FileWriter fwl = new FileWriter( this.logFileWithPath , true);
		logWriter = new PrintWriter(fwl);
		logInfPrint("LoggingTool started.");
	}
	
	/**
	 * Explicitly closes FileWriter objects. Should be called by java AutoCloseable implicitly.
	 */
	public void close(){
		if(logWriter != null){
			logWriter.close();
		}
		if(failWriter != null){
			failWriter.close();
		}
	}
	
	/**
	    * Writes a string to the log file and copies output to stdout, using a standard INF format
	    * @param logLine the string to be written to the log
	    */
	public void logInfPrint(String logLine) {
		String line = "INF " + logLine;
		try {
			logPrint(line);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("Failed to write to log file");
		}
	}
	
	/**
	    * Writes a string to the log file and copies output to stdout, using a standard ERR format.
	    * @param logLine The string to be written to the log.
	    */
	public void logErrPrint(String logLine) {
		String line = "ERR " + logLine;
		try {
			logPrint(line);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			System.out.println("Failed to write to log file");
		}
	}
	
	/**
	 * Writes a string to the retry log. This string is implementation-agnostic, but is typically a name or index. It is written only to file, and not copied to stdout.
	 * @param string The string to be written to the retry file.
	 */
	public void logFailRetryPrint(String string) {
		try {
			writeToFailLogFile(string);
		} catch (IOException e) {
			System.out.println("Failed to write to retry log");
		}
	}
	
	/**
	    * Writes a string to the log file and copies output to stdout, using a standard format
	    * @param logLine the string to be written to the log
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	    */
	private void logPrint(String logLine) throws FileNotFoundException, UnsupportedEncodingException{
		String line = getLogPrefix() + logLine;
		System.out.println(line);
		writeToLogFile(line);
	}

	/**
	 * Writes a string to the log file in a standard format.
	 * @param logLine The string to be written to the log. Will be combined with a prefix so that all log entries are standardized. 
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	    */
	private void writeToLogFile(String logLine) throws FileNotFoundException, UnsupportedEncodingException{
	    logWriter.println(logLine);
		logWriter.flush();
	}
	
	/**
	 * Writes a string to the retry log file in a standard format.
	 * @param logLine The string to be written to the log. Will be combined with a prefix so that all log entries are standardized. 
	 * @throws IOException
	 */
	private void writeToFailLogFile(String logLine) throws IOException{
		if(failWriter == null){
			FileWriter fwf = new FileWriter( this.failureLogFileWithPath , true);
			failWriter = new PrintWriter(fwf);
			logInfPrint("Initialized failWriter");
		}
		
	    failWriter.println(logLine);
		failWriter.flush();
	}
	
	/**
	    * Helper function the standardizes log format
	    * @return String containing the prefix concatenated before log entries
	    */
	private String getLogPrefix(){
		return LocalDateTime.now() + "  : ";
	}
	
	/**
	 * Automatically looks for the most recent retry file and returns its contents. Throws an exception if a retry
	 *  file cannot be found.
	 * @return Returns an ArrayList containing the newline-delimited strings in the retry file. 
	 * @throws FileNotFoundException Throws an exception if a file cannot be found, or if some other error prevents the retry
	 *  file from being read.
	 */
	public ArrayList<String> getLatestRetry() throws FileNotFoundException   {
		File folder = new File(logPath);
		File[] listOfAllLogFiles = folder.listFiles();
		ArrayList<String> listOfAllRetryFiles = new ArrayList<String>();
		
		for(int i = 0; i < listOfAllLogFiles.length; i++){
		    String filename = listOfAllLogFiles[i].getName();
		    if(filename.endsWith(retryLogName)){
		    	listOfAllRetryFiles.add(filename);
		    }
		}
		
		long latest = 0;
		String latestRetryFileString = null;
		for(int i = 0; i < listOfAllRetryFiles.size(); i++){
			long timestamp = Long.parseLong(listOfAllRetryFiles.get(i).substring(0, 13));
			if(timestamp > latest){
				latest = timestamp;
				latestRetryFileString = listOfAllRetryFiles.get(i);
			}
		}
		
		Scanner scanner = new Scanner(new File(logPath + "/" + latestRetryFileString));
		ArrayList<String> retryValues = new ArrayList<String>();
		while(scanner.hasNextInt()){
			String val = scanner.next().trim();
		    if(val.length() > 0){
		    	retryValues.add(val);
		    }
		}
		
		scanner.close();
		
		return retryValues;
	}

	
}
