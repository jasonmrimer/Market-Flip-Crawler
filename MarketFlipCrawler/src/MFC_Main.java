/*
 * Main will eventually run the threads of the site crawler and the database crawler.
 * Jason M. Rimer, 20151009
 */
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.jsoup.nodes.Document;
import marketflip.MF_Product;
import marketflip.MF_SourceCode;
import mfc_analyzer.MFC_SourceCodeAnalyzerManager;
import mfc_netcrawler.MFC_NetCrawlerManager;
import mfc_dbcrawler.MFC_DatabaseCrawlerManager;
public class MFC_Main {
	/*
	 * This main method only serves to start the program then
	 * branch into subclasses & methods with more freedom to operate
	 * threads and diverge into a database crawler and internet crawler
	 */

	public static void main(String args[]) throws SQLException{
		// Create pipelines for inter-thread communication
		BlockingQueue<MF_Product> bqMFProduct = new ArrayBlockingQueue<MF_Product>(MFC_DatabaseCrawlerManager.MFC_MAX_DB_QUEUE_COUNT);
		BlockingQueue<Document> bqMFSourceCode = new ArrayBlockingQueue<Document>(MFC_SourceCodeAnalyzerManager.MFC_MAX_ANALYZER_QUEUE_COUNT);
		// Create threads to run simultaneous sections of the application
		(new Thread(new MFC_SourceCodeAnalyzerManager(bqMFSourceCode, bqMFProduct))).start();	//takes sourcecode and returns product
		(new Thread(new MFC_DatabaseCrawlerManager(bqMFProduct))).start();						//takes product and updates database
		(new Thread(new MFC_NetCrawlerManager(bqMFSourceCode))).start();						//delivers sourcecode for analyzing
 	}
}
