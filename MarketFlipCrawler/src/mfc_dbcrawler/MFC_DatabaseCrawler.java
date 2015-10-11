package mfc_dbcrawler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import marketflip.MF_Product;

public class MFC_DatabaseCrawler implements Runnable{
	private BlockingQueue<MF_Product> bqMFProduct;
	private ExecutorService executor;
	
	// Construct with Executor and pipeline
	public MFC_DatabaseCrawler(BlockingQueue<MF_Product> bqMFProduct) {
		this.bqMFProduct = bqMFProduct;
	}
	
	@Override
	public void run() {
		/*
		 *  While running, the dbCrawler will take requests from the netCrawler and serve
		 *  results from the Market Flip database
		 */
		while (true) {
			try {
				System.out.println("db");
				Thread.sleep(1000);
			} catch (InterruptedException e){
				
			}
		}
		
	}

}
