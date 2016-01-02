package com.marketflip.crawler.main;

/*
 * Main will eventually run the threads of the site crawler and the database crawler.
 * Jason M. Rimer, 20151009
 */
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.nodes.Document;

import com.marketflip.crawler.dbcrawler.MFC_DatabaseCrawlerManager;
import com.marketflip.crawler.netcrawler.MFC_NetCrawlerManager;
import com.marketflip.crawler.scanalyzer.MFC_SourceCodeAnalyzerManager;
import com.marketflip.shared.products.MF_Product;

/**
 * This main method only serves to start the program then
 * branch into subclasses & methods with more freedom to operate
 * threads and diverge into a database crawler and internet crawler
 */
public class MFC_Main {

	private static ExecutorService executor;

	public static void main(String args[]) throws Exception {
		executor = Executors.newCachedThreadPool();
		if (args.length == 0) {
			// Create pipelines for inter-thread communication
			BlockingQueue<MF_Product> bqMFProduct = new ArrayBlockingQueue<MF_Product>(
					MFC_DatabaseCrawlerManager.MFC_MAX_DB_QUEUE_COUNT);
			BlockingQueue<Document> bqMFSourceCode = new ArrayBlockingQueue<Document>(
					MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT);
			// Create threads to run simultaneous sections of the application
			(new Thread(new MFC_SourceCodeAnalyzerManager(bqMFSourceCode, bqMFProduct, 0))).start(); //takes sourcecode and returns product
			(new Thread(new MFC_DatabaseCrawlerManager(bqMFProduct, "testing"))).start(); //takes product and updates database
			(new Thread(new MFC_NetCrawlerManager(bqMFSourceCode))).start(); //delivers sourcecode for analyzing
		}
		else {
			String startURLPath;
			int siteLimit, productLimit, docLimit;
			BlockingQueue<MF_Product> bqMFProduct;
			BlockingQueue<Document> bqMFSourceCode;
			MFC_SourceCodeAnalyzerManager scaMngr;
			MFC_DatabaseCrawlerManager dbcMngr;
			MFC_NetCrawlerManager netMngr;
			Thread scaThread, dbcThread, netThread;
			startURLPath = args[0];
			siteLimit = Integer.valueOf(args[1]);
			productLimit = Integer.valueOf(args[2]);
			docLimit = Integer.valueOf(args[3]);
			// Create pipelines for inter-thread communication
			bqMFProduct = new ArrayBlockingQueue<MF_Product>(
					MFC_DatabaseCrawlerManager.MFC_MAX_DB_QUEUE_COUNT);
			bqMFSourceCode = new ArrayBlockingQueue<Document>(
					MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT);
			// Create threads to run simultaneous sections of the application
			dbcMngr = new MFC_DatabaseCrawlerManager(bqMFProduct, "testing", productLimit);
			netMngr = new MFC_NetCrawlerManager(bqMFSourceCode, startURLPath, siteLimit);
			scaMngr = new MFC_SourceCodeAnalyzerManager(bqMFSourceCode, bqMFProduct, docLimit);
			dbcThread = new Thread(dbcMngr);
			netThread = new Thread(netMngr); //takes product and updates database
			scaThread = new Thread(scaMngr); //takes sourcecode and returns product
			executor.execute(dbcThread);
			executor.execute(netMngr);
			executor.execute(scaMngr);
		}
	}

	public ExecutorService getExecutor() {
		return executor;
	}
}
