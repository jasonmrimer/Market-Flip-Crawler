package mfc_dbcrawler;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import marketflip.MF_Product;
import mfc_analyzer.MFC_SourceCodeAnalyzer;

public class MFC_DatabaseCrawlerManager implements Runnable{
	public final static int MFC_MAX_DB_QUEUE_COUNT = 3;	//limit queue number based on what our system architecture can handle
	private final int MFC_MAX_THREAD_COUNT = 10;	//limit thread number based on what our system architecture can handle
	private ArrayList<Future> futuresArray = new ArrayList<Future>();	//contain all of the callables passed to the executor
	private BlockingQueue<MF_Product> bqMFProduct;
	private ExecutorService executor;
	
	// Construct with Executor and pipeline
	public MFC_DatabaseCrawlerManager(BlockingQueue<MF_Product> bqMFProduct) {
		this.bqMFProduct = bqMFProduct;
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT);
	}
	
	@Override
	public void run() {
		/*
		 *  While running, the dbCrawler will take requests from the netCrawler and serve
		 *  results from the Market Flip database
		 */
		// Create a Callable to be used for Futures; help found at: http://www.journaldev.com/1090/java-callable-future-example
        while (true) {		// Constantly seek websites for processing
        	if (!bqMFProduct.isEmpty() && futuresArray.size() < MFC_MAX_THREAD_COUNT){	// only run the queue if it contains items 
	        	// Fills ArrayList to ensure the program always runs max threads allowable
        		/* It may be true that the thread is full but the queue has entries; thereby, we must block
        		 * the queue and wait until the executor gains a spot. I created futuresArray to help count the components
        		 * inside the executor as the executor itself cannot return such a value
        		 */
	        	// Continually empty the queue until the executor reaches capacity
        		while (!bqMFProduct.isEmpty()){
	        		if (futuresArray.size() < MFC_MAX_THREAD_COUNT) {	// Check if the executor can begin another thread
		        		Future<Boolean> future = executor.submit(new MFC_DatabaseCrawler(bqMFProduct.remove()));	// begin thread & remove from queue
		        		futuresArray.add(future);	//add future to list for tracking
	        		}
	        		else break;
	        	}
	        	// Iterate through Futures to remove any completed Callables in order to refill the thread pool and keep it full
	        	for (int futureIndex = futuresArray.size() - 1; futureIndex > -1; futureIndex--){
	    			try {
	        			if (futuresArray.get(futureIndex).isDone()) {
	        				futuresArray.remove(futureIndex);
	        				System.out.println("removed, DBCM array size: " + futuresArray.size());
	        			}
	        		} catch (ConcurrentModificationException e) {
	    				e.printStackTrace();
	    			}
	    		}
        	}
        }
		
	}

}