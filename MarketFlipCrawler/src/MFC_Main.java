/*
 * Main will eventually run the threads of the site crawler and the database crawler.
 * Jason M. Rimer, 20151009
 */
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import marketflip.MF_Product;
import marketflip.MF_SourceCode;
import mfc_analyzer.MFC_SourceCodeAnalyzer;
import mfc_netcrawler.MFC_NetCrawler;
import mfc_dbcrawler.MFC_DatabaseCrawler;

public class MFC_Main {
	/*
	 * This main method only serves to start the program then
	 * branch into subclasses & methods with more freedom to operate
	 * threads and diverge into a database crawler and internet crawler
	 */
	private static int MFC_MAX_DB_QUEUE_COUNT = 3;
	public static void main(String args[]){
		// Create pipelines for inter-thread communication
		BlockingQueue<MF_Product> bqMFProduct = new ArrayBlockingQueue<MF_Product>(MFC_MAX_DB_QUEUE_COUNT);
		BlockingQueue<MF_SourceCode> bqMFSourceCode = new ArrayBlockingQueue<MF_SourceCode>(MFC_MAX_DB_QUEUE_COUNT);
		// Create threads to run simultaneous sections of the application
		(new Thread(new MFC_SourceCodeAnalyzer(bqMFSourceCode, bqMFProduct))).start();	//takes sourcecode and returns product
		(new Thread(new MFC_DatabaseCrawler(bqMFProduct))).start();						//takes product and updates database
		(new Thread(new MFC_NetCrawler(bqMFSourceCode))).start();						//delivers sourcecode for analyzing
	}
}
