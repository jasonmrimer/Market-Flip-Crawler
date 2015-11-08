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

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;

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
	private final int 							MFC_MAX_THREAD_COUNT	=	100;									// limit thread number based on what our system architecture can handle
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
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT);	// create executor with thread limit
		database = new MFC_TempDB();	// create connection to database for website storage
		URLs.add(startURL);				// place the first URL in the queue to begin crawling
	}
	
	/**
	 * The netCrawler will constantly run, crawling the Internet for pages with
	 * products for sale. Each time it discovers a product, it will pass it to
	 * the dbCrawler for processing.
	 */
	@Override
	public void run() {
		Long startTime = System.currentTimeMillis();
		/*
		 * The loop will continually create, check, and complete Callables known as Futures.
		 * The futures will run as a thread in the executor and manage NetCrawler objects. 
		 * The NetCrawler objects will disect website Documents to get code, links, and descriptions for the SourceCodeAnalyzer.
		 * This Manager will send the completed Document to the Analyzer and close the futures.
		 * 
		 * Some initial understanding found from help found at: http://www.journaldev.com/1090/java-callable-future-example
		 */
		while (sitesVisited < MFC_MAX_SITE_VISITS) {		// Constantly seek websites for processing
			// Fills ArrayList to ensure the program always runs max threads allowable
        	fillFuturesArray();
			
        	// Iterate through Futures to remove any completed Callables in order to refill the thread pool and keep it full
        	manageFutures();
        	
        }
		executor.shutdown();	// close executor to release assets
		Long elapsedTime = startTime - System.currentTimeMillis();	// get elapsed time for the netcrawler
		System.out.println("elapsed time: " + elapsedTime + " ms");
	}
	
	private void fillFuturesArray() {
		for (int futuresCount = futuresArray.size(); futuresCount < MFC_MAX_THREAD_COUNT; futuresCount++){
            // Submit Callable tasks to be executed by thread pool and return Future to be analyzed for completion.
    		if (!URLs.isEmpty()){ // check that a URL is ready to execute
//        		String futureURL = URLs.remove(0);	// remove the first URL (FIFO) to submit as NetCrawler Future
//    			System.out.println("submitting future: " + futureURL);	// TODO Move to JUnit
//        		MFC_NetCrawler futureNetCrawler = new MFC_NetCrawler(database, futureURL);	// create the NetCrawler to submit as Future
//    			Future<MFC_NetCrawler> future;	// create a Future with a returned NetCrawler
//        		future = executor.submit(futureNetCrawler);	// submit Future/start thread
//        		futuresArray.add(future);	// add future to list for tracking
        		// TRIAL: receiving error with duplicate threads likely due to concurrent db updates
    			// and thread creations. moving isRecorded test to Manager despite time lag 
    			if (!database.isRecorded(DigestUtils.sha256Hex(URLs.get(0)))) {
        			futuresArray.add(executor.submit(new MFC_NetCrawler(database, URLs.remove(0))));
        		} else URLs.remove(0);
    		}
    		else break;
    	}		
	}

	private void manageFutures() {
		for (int futureIndex = 0; futureIndex < futuresArray.size(); futureIndex++) {	// FIFO
			try {
    			if (futuresArray.get(futureIndex).isDone() && bqMFSourceCode.size() < MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT) {
    				// TODO move to JUnit Test: 
					MFC_NetCrawler completedNetCrawler = futuresArray.get(futureIndex).get();	// retrieve completed NetCrawler from Future
//    				System.out.println("Completed NetCrawler: " + completedNetCrawler.getStartURL()); // TODO move to JUnit
    				if (completedNetCrawler.getSiteDoc() != null) {
    					bqMFSourceCode.add(completedNetCrawler.getSiteDoc());
        				URLs.addAll(completedNetCrawler.getURLs());
        				// TODO check URLs returned
    				}
    				futuresArray.remove(futureIndex);
					System.out.println("Site visited: " + completedNetCrawler.getStartURL());	// TODO returns blank
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
	 * Getters; specifically used for JUnit Testing as no other classes require methods or
	 * variables from this manager
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