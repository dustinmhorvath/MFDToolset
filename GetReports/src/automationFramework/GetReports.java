package automationFramework;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GetReports {
	
	public static final int MAXCONCURRENTTHREADS = 2;

	public static void main(String[] args) {
		
		String listOfPrintersPath = "//cfs/it_general/units/ss/ITT2/_s/MFD/Reports/MasterReports_Spreadsheet_Archive/Master_Reports.xlsx";
		
		File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "Reports-" + LocalDateTime.now().getMonthValue() + "-" + LocalDateTime.now().getDayOfMonth());
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
        }
                
        String downloadBasePath = workingFolder.getAbsolutePath();
        ReportGatherer gatherer = new ReportGatherer(listOfPrintersPath, downloadBasePath, 4, 6, 7);
		
        /*
        //gatherer.length()
        int startMFD = 1;
        int totalMFDs = 1;//gatherer.length();
        final ArrayList<Integer> list = new ArrayList<Integer>();
        for(int currentMFD = startMFD; currentMFD <= totalMFDs; currentMFD++){
			list.add(currentMFD);
        }
		
        ExecutorService pool = Executors.newFixedThreadPool(MAXCONCURRENTTHREADS);
        for(final int mfd : list){
            pool.execute(new Runnable(){
                public void run() {
                    gatherer.retrieveReportByIndex(mfd);
                }
            });
        }
        
        pool.shutdown();
        try {
			pool.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	     	

		System.out.println("Retrieval complete.");
	}	
}
