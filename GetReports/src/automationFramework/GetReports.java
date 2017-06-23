package automationFramework;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetReports {
	
	// The maximum number of browser windows this application will be allowed to run concurrently
	public static final int MAXCONCURRENTTHREADS = 6;

	public static void main(String[] args) {
		
		String listOfPrintersFileFullPath = "//cfs/it_general/units/ss/ITT2/_s/MFD/Reports/MasterReports_Spreadsheet_Archive/Master_Reports.xlsx";
		
		File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "Reports-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth());
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
        }
        String downloadBasePath = workingFolder.getAbsolutePath();

		
		ReportGatherer gatherer;
		try {
			gatherer = new ReportGatherer(listOfPrintersFileFullPath, downloadBasePath, 4, 6, 7);
			
			int totalMFDs = 0;;
			BlockingQueue<Integer> queue;
			if(gatherer.hasRetries()){
				ArrayList<String> list = gatherer.getRetryList();
				totalMFDs = list.size();
				queue = new ArrayBlockingQueue<Integer>(totalMFDs);
				for(int i = 0; i < list.size(); i++){
					queue.add(Integer.parseInt(list.get(i)));
				}
				
			}
			else{
				int startMFD = 1;
				totalMFDs = gatherer.getFileLength();
				queue = new ArrayBlockingQueue<Integer>(totalMFDs);
		        for(int currentMFD = startMFD; currentMFD <= totalMFDs; currentMFD++){
		        	queue.add(currentMFD);
		        }
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
	        pool.awaitTermination(30, TimeUnit.SECONDS);
			gatherer.close();
        
        }
    	catch (InterruptedException e) {
			System.out.println("ERROR ExecutorService/CountDownLatch failed.");
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR Could not initialize gatherer");
		}
		
		System.out.println("Retrieval complete.");
	}
	
	
}
