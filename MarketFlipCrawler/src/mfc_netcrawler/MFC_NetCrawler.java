package mfc_netcrawler;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import marketflip.MF_SourceCode;
/*
 * MFC_NetCrawler will continuously manage crawling the web in search of 
 * websites that contain products for sale. When it finds a website,
 * it will send all of the website's data to an analyzer (e.g. MFC_SourceCodeAnalyzer) for
 * heavy duty analytics to potentially turn the discovered data into useable input to
 * the database. The logic for the handoff is to keep NetCrawler light and quick, bringing in
 * as much data as possible while other classes handle more time/resource-intensive processes.  
 */
public class MFC_NetCrawler implements Runnable {
	private static int MFC_MAX_THREAD_COUNT = 3;	//limit thread number based on what our system architecture can handle
	private ArrayList<Future<String>> futuresArray = new ArrayList<Future<String>>();	//contain all of the callables passed to the executor
	private BlockingQueue<MF_SourceCode> bqMFSourceCode;	//open communication TO SourceCodeAnalyzer
	private ExecutorService executor;

	// Construct with open pipeline TO SourceCodeAnalyzer
	public MFC_NetCrawler(BlockingQueue<MF_SourceCode> bqMFSourceCode) {
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT);	 
		System.out.println("net constructed");
	}
	
	/*
	 * The netCrawler will constantly run, crawling the Internet for pages with
	 * products for sale. Each time it discovers a product, it will pass it to 
	 * the dbCrawler for processing.
	 */
	@Override
	public void run() {
		// Create a Callable to be used for Futures; help found at: http://www.journaldev.com/1090/java-callable-future-example
        while (true) {		// Constantly seek websites for processing
        	// Fills ArrayList to ensure the program always runs max threads allowable
        	for (int futuresCount = futuresArray.size(); futuresCount < MFC_MAX_THREAD_COUNT; futuresCount++){
                // Submit Callable tasks to be executed by thread pool and return Future to be analyzed for completion.
        		Future<String> future = executor.submit(new MFC_WebsiteFinder());
        		futuresArray.add(future);	//add future to list for tracking
        	}
        	// Iterate through Futures to remove any completed Callables in order to refill the thread pool and keep it full
        	for (int futureIndex = futuresArray.size() - 1; futureIndex > -1; futureIndex--){
    			try {
        			if (futuresArray.get(futureIndex).isDone()) {
        				System.out.println(futuresArray.get(futureIndex).get());
        				futuresArray.remove(futureIndex);
        				System.out.println("removed, size: " + futuresArray.size());
        			}
        		} catch (InterruptedException e) {
    				e.printStackTrace();
				} catch (ExecutionException e) {
    				e.printStackTrace();
    			} catch (ConcurrentModificationException e) {
    				e.printStackTrace();
    			}
    		}
        }
	}
}
