import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class MFC_InternetCrawler implements Runnable {
	private ArrayList<MFC_WebsiteFinder> callablesArray = new ArrayList<MFC_WebsiteFinder>();	//contain all of the callables passed to the executor
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
        //submit Callable tasks to be executed by thread pool
        while (true) {
        	for (int callableCount = callablesArray.size(); callableCount <= MFC_MAX_THREAD_COUNT; callableCount++){
        		callablesArray.add(new MFC_WebsiteFinder());
        	}
        	try {
				String completedCallable = executor.invokeAny(callablesArray);
				System.out.println(completedCallable);
				callablesArray.remove(completedCallable);
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
			}
        	
        }
//		Future<String> future = executor.submit(new MFC_WebsiteFinder());
//		// Constantly seek websites for processing
//		while (true) {
//			for (int count = 0; count < MFC_MAX_THREAD_COUNT; count++){
//				FutureTask<String> future = new FutureTask<String>(new Callable<String>() {
//					public String call() {
//						return findWebsite();
//			    }});
//				executor.execute(future);
//			}
//			try {
//				System.out.println("net");
//				System.out.println(executor.invokeAll(tasks));
//				Thread.sleep(500);
//			} 
//			catch (InterruptedException e){} 
//			catch (ExecutionException e) {}
//		}
	}
	/*
	 * This method will crawl until it finds a new website with products then
	 * pass that website to an analyzer
	 */
	
	private String findWebsite() {
//		return "http://website.com/" + count;
		return "http://website.com/" + (new Random()).nextInt(1000000);
	}
}
