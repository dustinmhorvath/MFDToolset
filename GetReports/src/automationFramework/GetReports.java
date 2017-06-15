package automationFramework;

import java.io.File;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class GetReports {
	

	public static void main(String[] args) {
		String envPath = "//cfs/apps/pcc/MFDAutomationEnv/";
		
		String listOfPrintersPath = "//cfs/it_general/units/ss/ITT2/_s/MFD/Reports/MasterReports_Spreadsheet_Archive/Master_Reports.xlsx";
		
		File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "Reports-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth());
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
        }
                
        String downloadBasePath = workingFolder.getAbsolutePath();
        ReportGatherer gatherer = new ReportGatherer(envPath, listOfPrintersPath, downloadBasePath, 4, 6, 7);
		
        new Thread(() -> {
        	gatherer.retrieveReportByPrinterName("D6608");
        }).start();
        
        Queue<Thread> queue = new LinkedList<Thread>();
        //gatherer.length()

		for(int currentMFD = 1; currentMFD < 6; currentMFD++){	
			final int index = currentMFD;
			
			Thread thread = new Thread(() -> {
	        	gatherer.retrieveReportByIndex(index);
			});
			
			queue.offer(thread);
	        thread.start();
	        while(queue.size() > 2){
	        	PublicTools.sleep(2000);
	        	for (Thread t : queue) {
	        		if(!t.isAlive()){
	        			queue.remove(t);
	        		}
	        	}
	        	
	        }
			
			
		}

		System.out.println("Retrieval complete.");
	}
	
}
