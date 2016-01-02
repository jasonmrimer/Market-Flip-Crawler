package com.marketflip.crawler.netcrawler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jsoup.nodes.Document;
import org.omg.CORBA.PUBLIC_MEMBER;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.marketflip.crawler.scanalyzer.MFC_SourceCodeAnalyzerManager;

/**
 * @author Jason Rimer
 *         MFC_NetCrawler will continuously manage crawling the web in search of
 *         websites that contain products for sale. When it finds a website,
 *         it will send all of the website's data to an analyzer (e.g. MFC_SourceCodeAnalyzer) for
 *         heavy duty analytics to potentially turn the discovered data into useable input to
 *         the database. The logic for the handoff is to keep NetCrawler light and quick, bringing
 *         in
 *         as much data as possible while other classes handle more time/resource-intensive
 *         processes.
 */
public class MFC_NetCrawlerManager implements Runnable {

	private final int							MFC_MAX_THREAD_COUNT	= 100;										// limit thread number based on what our system architecture can handle
	private final int							MFC_MAX_SITE_VISITS		= 4;										// limit sites to prevent infinite crawl
	private ArrayList<Future<MFC_NetCrawler>>	futuresArray			= new ArrayList<Future<MFC_NetCrawler>>();	// contain all of the callables passed to the executor
	private BlockingQueue<Document>				bqMFSourceCode;														// open communication TO SourceCodeAnalyzer
	private ExecutorService						executor;
	private MFC_NetCrawler						netCrawler;
	private String								startURL;															// =	"http://www.walmart.com/ip/New-Super-Mario-Bros.-Wii/11991871";
	private ArrayList<String>					URLs					= new ArrayList<String>();
	private MFC_WebsiteDAO						database;
	private int									sitesVisited			= 0;
	private boolean								isMock					= false;
	private int									futuresCount;														// used to test how many futures the manager creates
	private int siteLimit;

	public MFC_NetCrawlerManager() {
		database = new MFC_WebsiteDAO(); // create connection to database for website storage
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT); // create executor with thread limit
	}

	public MFC_NetCrawlerManager(BlockingQueue<Document> bqJSoupDoc) {
		this();
		this.bqMFSourceCode = bqJSoupDoc;
		this.siteLimit = MFC_MAX_SITE_VISITS;
	}

	public MFC_NetCrawlerManager(BlockingQueue<Document> bqJSoupDoc, String startPath) {
		this(bqJSoupDoc);
		this.startURL = startPath;
		URLs.add(startURL); // place the first URL in the queue to begin crawling
		System.out.println("netcrawlmngr for " + startURL);
	}

	/**
	 * The purpose of this constructor is to create a mock object that sets the database to null in
	 * order to run quick test and exclude any calls to the database inside netCrawler objects or
	 * this object.
	 *
	 * @param bqJSoupDoc
	 * @param startPath
	 * @param isMock
	 */
	public MFC_NetCrawlerManager(BlockingQueue<Document> bqJSoupDoc, String startPath,
			boolean isMock) {
		this.bqMFSourceCode = bqJSoupDoc;
		this.startURL = startPath;
		this.isMock = isMock;
		database = null;
		executor = Executors.newFixedThreadPool(MFC_MAX_THREAD_COUNT); // create executor with thread limit
		URLs.add(startURL); // place the first URL in the queue to begin crawling
		futuresCount = 0;
	}

	public MFC_NetCrawlerManager(BlockingQueue<Document> bq, String startURLString, int siteLimit) {
		this(bq, startURLString);
		this.siteLimit = siteLimit;
	}

	/**
	 * The netCrawler will constantly run, crawling the Internet for pages with
	 * products for sale. Each time it discovers a product, it will pass it to
	 * the dbCrawler for processing.
	 */
	@Override
	public void run() {
		System.out.println("running");
		/*
		 * The loop will continually create, check, and complete Callables known as Futures.
		 * The futures will run as a thread in the executor and manage NetCrawler objects.
		 * The NetCrawler objects will disect website Documents to get code, links, and descriptions
		 * for the SourceCodeAnalyzer.
		 * This Manager will send the completed Document to the Analyzer and close the futures.
		 * It complets when it reaches the max site limit or all futures are complete while the URL
		 * list is empty (essentially, found a deadend).
		 * 
		 * Some initial understanding found from help found at:
		 * http://www.journaldev.com/1090/java-callable-future-example
		 */
		System.out.println((sitesVisited < siteLimit));
		System.out.println((!futuresArray.isEmpty() && !URLs.isEmpty()));
		while ((sitesVisited < siteLimit)
				|| (!futuresArray.isEmpty() && !URLs.isEmpty())) { // Constantly seek websites for processing
			// Fills ArrayList to ensure the program always runs max threads allowable
			fillFuturesArray();

			// Iterate through Futures to remove any completed Callables in order to refill the thread pool and keep it full
			manageFutures();

		}
		executor.shutdown(); // close executor to release assets
		try {
			if (database != null) {
				database.con.close();
			}
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fillFuturesArray() {
		for (int futuresCount = futuresArray
				.size(); futuresCount < MFC_MAX_THREAD_COUNT; futuresCount++) {
			// Submit Callable tasks to be executed by thread pool and return Future to be analyzed for completion.
			if (!URLs.isEmpty()) { // check that a URL is ready to execute
				//        		String futureURL = URLs.remove(0);	// remove the first URL (FIFO) to submit as NetCrawler Future
				//    			System.out.println("submitting future: " + futureURL);	// TODO Move to JUnit
				//        		MFC_NetCrawler futureNetCrawler = new MFC_NetCrawler(database, futureURL);	// create the NetCrawler to submit as Future
				//    			Future<MFC_NetCrawler> future;	// create a Future with a returned NetCrawler
				//        		future = executor.submit(futureNetCrawler);	// submit Future/start thread
				//        		futuresArray.add(future);	// add future to list for tracking
				// TRIAL: receiving error with duplicate threads likely due to concurrent db updates
				// and thread creations. moving isRecorded test to Manager despite time lag 
				// Mock block for testing without the database
				System.out.println("inside netmngr 3");

				if (isMock) {
					// Using the mock object, disregard the database connection for website URLs and run mock NetCrawlers
					futuresArray.add(executor.submit(new MFC_NetCrawler(URLs.remove(0))));
					futuresCount++;
					System.out.println("mock ncmngr futures++: " + futuresCount);
					break;
				}
				// Actual programming block
				else {
					System.out.println("inside netmngr 4");

					if (!database.isRecorded(DigestUtils.sha256Hex(URLs.get(0)))) {
						System.out.println("inside netmngr 5");

						futuresArray
								.add(executor.submit(new MFC_NetCrawler(database, URLs.remove(0))));
					}
					else URLs.remove(0);
				}
			}
			else break;
		}
	}

	private void manageFutures() {
		for (int futureIndex = 0; futureIndex < futuresArray.size(); futureIndex++) { // FIFO
			try {
				if (futuresArray.get(futureIndex).isDone() && bqMFSourceCode
						.size() < MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT) {
					// TODO move to JUnit Test: 
					MFC_NetCrawler completedNetCrawler = futuresArray.get(futureIndex).get(); // retrieve completed NetCrawler from Future
					//    				System.out.println("Completed NetCrawler: " + completedNetCrawler.getStartURL()); // TODO move to JUnit
					if (completedNetCrawler.getSiteDoc() != null) {
						System.out.println(
								"Adding to SCM from NCM: " + completedNetCrawler.getStartURL());
						bqMFSourceCode.add(completedNetCrawler.getSiteDoc());
						URLs.addAll(completedNetCrawler.getURLs());
						// TODO check URLs returned
					}
					futuresArray.remove(futureIndex);
					System.out.println("Site visited: " + completedNetCrawler.getStartURL()); // TODO returns blank
					sitesVisited++;
					// TODO move to JUnit Test: System.out.println("removed, NC size: " + futuresArray.size());
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (ExecutionException e) {
				e.printStackTrace();
			}
			catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String toString() {
		String toString;
		toString = "MFC_NetCrawlerManager object with BlockingQueue size: "
				+ (bqMFSourceCode.size() + bqMFSourceCode.remainingCapacity());
		return toString;
	}

	/**
	 * Getters; specifically used for JUnit Testing as no other classes require methods or
	 * variables from this manager
	 * 
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

	public MFC_WebsiteDAO getDatabase() {
		return database;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public int getFuturesCount() {
		return futuresCount;
	}

	public int getSitesVisited() {
		return sitesVisited;
	}

	public ArrayList<String> getURLs() {
		return URLs;
	}

}