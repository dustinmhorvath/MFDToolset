package automationFramework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

public class LoggingTool  {
	private String logFileWithPath;
	public LoggingTool(String logFilepath, String logFileWithPath) throws IOException{
		this.logFileWithPath = logFilepath + "/" + logFileWithPath;
		
		File folder = new File(logFilepath);
        if (!folder.exists()) {
        	folder.mkdir();
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
	    * Writes a string to the log file and copies output to stdout, using a standard ERR format
	    * @param logLine the string to be written to the log
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
	    * Writes a string to the log file in a standard format
	    * @param logLine the string to the be written to the log. Will be combined with a prefix so that all log entries are standardized 
	    *  @return int returns 0 on success, nonzero on an error
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	    */
	private void writeToLogFile(String logLine) throws FileNotFoundException, UnsupportedEncodingException{
		
	    PrintWriter writer = new PrintWriter(logFileWithPath, "UTF-8");
	    writer.println(logLine);
	    writer.close();
		    
	}
	
	/**
	    * Helper function the standardizes log format
	    * @return String containing the prefix concatenated before log entries
	    */
	private String getLogPrefix(){
		return LocalDateTime.now() + "  : ";
	}
}
