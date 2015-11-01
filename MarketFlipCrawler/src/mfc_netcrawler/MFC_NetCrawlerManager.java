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

import org.jsoup.nodes.Document;

import marketflip.MF_SourceCode;
import mfc_analyzer.MFC_SourceCodeAnalyzerManager;

/**
 * @author Jason Rimer
 * MFC_NetCrawler will continuously manage crawling the web in search of 
 * websites that contain products for sale. When it finds a website,
 * it will send all of the website's data to an analyzer (e.g. MFC_SourceCodeAnalyzer) for
 * heavy duty analytics to potentially turn the discovered data into useable input to
 * the database. The logic for the handoff is to keep NetCrawler light and quick, bringing in
 * as much data as possible while other classes handle more time/resource-intensive processes.
 */
public class MFC_NetCrawlerManager implements Runnable {
	private final int 							MFC_MAX_THREAD_COUNT	=	3;									// limit thread number based on what our system architecture can handle
	private final int 							MFC_MAX_SITE_VISITS		=	100;								// limit sites to prevent infinite crawl
	private ArrayList<Future<MFC_NetCrawler>>	futuresArray			=	new ArrayList<Future<MFC_NetCrawler>>();	// contain all of the callables passed to the executor
	private BlockingQueue<Document>				bqMFSourceCode;													// open communication TO SourceCodeAnalyzer
	private ExecutorService 					executor;
	private MFC_NetCrawler						netCrawler;
	private String								startURL				=	"http://jsoup.org";
	private ArrayList<String>					URLs					=	new ArrayList<String>();
	
	// Construct with open pipeline TO SourceCodeAnalyzer
	public MFC_NetCrawlerManager(BlockingQueue<Document> bqMFSourceCode) {
		this.bqMFSourceCode = bqMFSourceCode;
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT);	 
		netCrawler = new MFC_NetCrawler(startURL);
		// TODO move to JUnit Test
		//		System.out.println("net constructed");
	}
	
	/**
	 * The netCrawler will constantly run, crawling the Internet for pages with
	 * products for sale. Each time it discovers a product, it will pass it to
	 * the dbCrawler for processing.
	 */
	@Override
	public void run() {
		// TODO Start the first crawler pointed at a specific website
//		Future<MFC_NetCrawler> startFuture = executor.submit(netCrawler);
//		try {
//			MFC_NetCrawler tempFuture = startFuture.get();
			URLs.addAll(netCrawler.getURLs());
			if(netCrawler.getSiteDoc() != null) bqMFSourceCode.add(netCrawler.getSiteDoc());
//		} catch (ExecutionException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        int sitesVisited = 1;

		// Create a Callable to be used for Futures; help found at: http://www.journaldev.com/1090/java-callable-future-example
		while (sitesVisited < MFC_MAX_SITE_VISITS) {		// Constantly seek websites for processing
        	// Fills ArrayList to ensure the program always runs max threads allowable
        	for (int futuresCount = futuresArray.size(); futuresCount < MFC_MAX_THREAD_COUNT; futuresCount++){
                // Submit Callable tasks to be executed by thread pool and return Future to be analyzed for completion.
        		if (!URLs.isEmpty()){
	        		Future<MFC_NetCrawler> future = executor.submit(new MFC_NetCrawler(URLs.remove(0)));
	        		futuresArray.add(future);	//add future to list for tracking
        		}
        		else break;
        	}
        	// Iterate through Futures to remove any completed Callables in order to refill the thread pool and keep it full
        	for (int futureIndex = futuresArray.size() - 1; futureIndex > -1; futureIndex--){
    			try {
        			if (futuresArray.get(futureIndex).isDone() && bqMFSourceCode.size() < MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT) {
        				// TODO move to JUnit Test: System.out.println(futuresArray.get(futureIndex).get());
        				bqMFSourceCode.add(futuresArray.get(futureIndex).get().getSiteDoc());
        				URLs.addAll(futuresArray.get(futureIndex).get().getURLs());
        				System.out.println(futuresArray.get(futureIndex).get().getSiteDoc().baseUri());
        				futuresArray.remove(futureIndex);
        				sitesVisited++;
        				// TODO move to JUnit Test: System.out.println("removed, NC size: " + futuresArray.size());
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

	public MFC_NetCrawler getNetCrawler() {
		return netCrawler;
	}
	
}
