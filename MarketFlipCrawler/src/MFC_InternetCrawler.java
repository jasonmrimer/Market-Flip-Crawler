import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MFC_InternetCrawler implements Runnable {
//	private ArrayList<MFC_WebsiteFinder> callablesArray = new ArrayList<MFC_WebsiteFinder>();	//contain all of the callables passed to the executor
	private ArrayList<Future<String>> futuresArray = new ArrayList<Future<String>>();	//contain all of the callables passed to the executor
	private ExecutorService executor;
	private static int MFC_MAX_THREAD_COUNT = 3;	//limit thread number based on what our system architecture can handle
	
	public MFC_InternetCrawler() {
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
        // Submit Callable tasks to be executed by thread pool
        while (true) {		// Constantly seek websites for processing
        	// Fills ArrayList to ensure the program always runs max threads allowable
        	for (int futuresCount = futuresArray.size(); futuresCount < MFC_MAX_THREAD_COUNT; futuresCount++){
        		Future<String> future = executor.submit(new MFC_WebsiteFinder());
        		futuresArray.add(future);
        	}
        	for (int futureIndex = futuresArray.size() - 1; futureIndex > -1; futureIndex--){
    			try {
        			if (futuresArray.get(futureIndex).isDone()) {
        				System.out.println(futuresArray.get(futureIndex).get());
        				futuresArray.remove(futureIndex);
        				System.out.println("removed, size: " + futuresArray.size());
        			}
        		}
				catch (InterruptedException e) {
    				e.printStackTrace();
				} 
    			catch (ExecutionException e) {
    				e.printStackTrace();
    			}
    			catch (ConcurrentModificationException e){
    				e.printStackTrace();
    			}
    		}
//    		for (Future<String> future : futuresArray){
//    			try {
//        			if (future.isDone()) {
//        				System.out.println(future.get());
//        				if (futuresArray.remove(future)) System.out.println("removed, size: " + futuresArray.size());
//        			}
//        		}
//				catch (InterruptedException e) {
//				} 
//    			catch (ExecutionException e) {
//				}
//    			catch (ConcurrentModificationException e){
//    				e.printStackTrace();
//    			}
//    		}
        	
        }
	}
}
