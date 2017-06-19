package automationFramework;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetReports {
	
	public static final int MAXCONCURRENTTHREADS = 6;

	public static void main(String[] args) {
		
		String listOfPrintersPath = "//cfs/it_general/units/ss/ITT2/_s/MFD/Reports/MasterReports_Spreadsheet_Archive/Master_Reports.xlsx";
		
		File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "Reports-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth());
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
        }
                
        String downloadBasePath = workingFolder.getAbsolutePath();

		
		ReportGatherer gatherer;
		try {
			gatherer = new ReportGatherer(listOfPrintersPath, downloadBasePath, 4, 6, 7);
		
			int totalMFDs = gatherer.length();
			
			int startMFD = 1;
	        final BlockingQueue<Integer> queue = new ArrayBlockingQueue<Integer>(totalMFDs);
	        for(int currentMFD = startMFD; currentMFD <= totalMFDs; currentMFD++){
	        	queue.add(currentMFD);
	        }
		
	        CountDownLatch latch = new CountDownLatch(totalMFDs);
	        ExecutorService pool = Executors.newFixedThreadPool(MAXCONCURRENTTHREADS);
	        for(final int mfd : queue){
	            pool.execute(new Runnable(){
	                public void run() {
	                    gatherer.retrieveReportByIndex(mfd);
	                    latch.countDown();
	                }
	            });
	        }
        
        
	        pool.shutdown();
	        latch.await();
	        pool.awaitTermination(1, TimeUnit.MINUTES);
        
        }
    	catch (InterruptedException e) {
			System.out.println("ERROR ExecutorService/CountDownLatch failed.");
		}
		catch (Exception e) {
			System.out.println("ERROR Could not initialize gatherer");
		}
		

		System.out.println("Retrieval complete.");
	}	
}
