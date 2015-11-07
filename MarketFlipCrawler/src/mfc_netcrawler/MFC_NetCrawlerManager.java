package mfc_netcrawler;
import java.net.MalformedURLException;
import java.sql.SQLException;
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

import javax.net.ssl.SSLHandshakeException;

import org.jsoup.HttpStatusException;
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
	private MFC_TempDB							database;
	private int 								sitesVisited			=	0;
	// Construct with open pipeline TO SourceCodeAnalyzer
	public MFC_NetCrawlerManager(BlockingQueue<Document> bqMFSourceCode) throws SQLException {
		this.bqMFSourceCode = bqMFSourceCode;
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT);
		database = new MFC_TempDB();
		URLs.add(startURL);
		// TODO move to JUnit Test
	}
	
	/**
	 * The netCrawler will constantly run, crawling the Internet for pages with
	 * products for sale. Each time it discovers a product, it will pass it to
	 * the dbCrawler for processing.
	 */
	@Override
	public void run() {
		Long startTime = System.currentTimeMillis();
		// Start the first crawler pointed at a specific website
//		startFirstCrawler();

		// Create a Callable to be used for Futures; help found at: http://www.journaldev.com/1090/java-callable-future-example
		while (sitesVisited < MFC_MAX_SITE_VISITS) {		// Constantly seek websites for processing
        	
			// Fills ArrayList to ensure the program always runs max threads allowable
        	fillFuturesArray();
			
        	// Iterate through Futures to remove any completed Callables in order to refill the thread pool and keep it full
        	manageCallables();
        	
        }
		
		executor.shutdown();
		Long elapsedTime = startTime - System.currentTimeMillis();
		System.out.println("elapsed time: " + elapsedTime + " ms");
	}
	
	private void fillFuturesArray() {
		for (int futuresCount = futuresArray.size(); futuresCount < MFC_MAX_THREAD_COUNT; futuresCount++){
            // Submit Callable tasks to be executed by thread pool and return Future to be analyzed for completion.
    		if (!URLs.isEmpty()){ // check that a URL is ready to execute
        		String futureURL = URLs.remove(0);	// remove the first URL (FIFO) to submit as NetCrawler Future
    			System.out.println("submitting future: " + futureURL);	// TODO Move to JUnit
        		MFC_NetCrawler futureNetCrawler = new MFC_NetCrawler(database, futureURL);	// create the NetCrawler to submit as Future
    			Future<MFC_NetCrawler> future;	// create a Future with a returned NetCrawler
        		future = executor.submit(futureNetCrawler);	// submit Future/start thread
        		futuresArray.add(future);	// add future to list for tracking
    		}
    		else break;
    	}		
	}

	private void manageCallables() {
//		for (int futureIndex = futuresArray.size() - 1; futureIndex > -1; futureIndex--){	// LIFO - switch to FIFO
		for (int futureIndex = 0; futureIndex < futuresArray.size(); futureIndex++) {	// FIFO
			try {
    			if (futuresArray.get(futureIndex).isDone() && bqMFSourceCode.size() < MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT) {
    				// TODO move to JUnit Test: 
					MFC_NetCrawler completedNetCrawler = futuresArray.get(futureIndex).get();	// retrieve completed NetCrawler from Future
    				System.out.println("Completed NetCrawler: " + completedNetCrawler.getStartURL()); // TODO move to JUnit
    				if (completedNetCrawler.getSiteDoc() != null) {
    					bqMFSourceCode.add(completedNetCrawler.getSiteDoc());
    					System.out.println("Site visited: " + completedNetCrawler.getSiteDoc().attr("abs:href"));	// TODO returns blank
//        				System.out.println(futuresArray.get(futureIndex).get().getSiteDoc().baseUri());
        				URLs.addAll(completedNetCrawler.getURLs());
        				// TODO check URLs returned
    				}
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
	/**
	 * Getters
	 * @return
	 */
	public MFC_NetCrawler getNetCrawler() {
		return netCrawler;
	}

	public ArrayList<Future<MFC_NetCrawler>> getFuturesArray() {
		return futuresArray;
	}

	public BlockingQueue<Document> getBqMFSourceCode() {
		return bqMFSourceCode;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public ArrayList<String> getURLs() {
		return URLs;
	}

	public MFC_TempDB getDatabase() {
		return database;
	}
	
}
//private void startFirstCrawler() {
//netCrawler = new MFC_NetCrawler(database, startURL); // create first netCrawler object
//
////netCrawler.runJSoup(); // initiate all of the jsoup methods for the URL
//URLs.addAll(netCrawler.getURLs()); // add first few URLs to crawling list
//if (netCrawler.getSiteDoc() != null) {
//	bqMFSourceCode.add(netCrawler.getSiteDoc());
//}
//sitesVisited = 1;		
//}