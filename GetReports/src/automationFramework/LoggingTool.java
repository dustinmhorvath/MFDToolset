package automationFramework;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class LoggingTool {
	private String logFileWithPath;
	public LoggingTool(String logFileWithPath){
		this.logFileWithPath = logFileWithPath;
	}
	/**
	    * Writes a string to the log file and copies output to stdout, using a standard INF format
	    * @param logLine the string to be written to the log
	    */
	public void logInfPrint(String logLine){
		String line = "INF " + logLine;
		logPrint(line);
	}
	
	/**
	    * Writes a string to the log file and copies output to stdout, using a standard ERR format
	    * @param logLine the string to be written to the log
	    */
	public void logErrPrint(String logLine){
		String line = "ERR " + logLine;
		logPrint(line);
	}
	
	/**
	    * Writes a string to the log file and copies output to stdout, using a standard format
	    * @param logLine the string to be written to the log
	    */
	private void logPrint(String logLine){
		String line = getLogPrefix() + logLine;
		System.out.println(line);
		writeToLogFile(line);
	}

	/**
	    * Writes a string to the log file in a standard format
	    * @param logLine the string to the be written to the log. Will be combined with a prefix so that all log entries are standardized 
	    *  @return int returns 0 on success, nonzero on an error
	    */
	private int writeToLogFile(String logLine){
		try{
		    PrintWriter writer = new PrintWriter(logFileWithPath, "UTF-8");
		    writer.println(logLine);
		    writer.close();
		    
		    return 0;
		} catch (IOException e) {
			System.out.println("Write to log file " + logFileWithPath + " failed");
			return 1;
		}
	}
	
	/**
	    * Helper function the standardizes log format
	    * @return String a string containing the prefix concatenated before log entries
	    */
	private String getLogPrefix(){
		return LocalDateTime.now() + "  : ";
	}
}
