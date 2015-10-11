package mfc_analyzer;

import java.util.concurrent.BlockingQueue;

import marketflip.MF_Product;
import marketflip.MF_SourceCode;

public class MFC_SourceCodeAnalyzer implements Runnable {
	private BlockingQueue<MF_SourceCode> bqMFSourceCode;	//open communication FROM NetCrawler
	private BlockingQueue<MF_Product> bqMFProduct;			//open communication TO DBCrawler

	// Construct with open pipelines
	public MFC_SourceCodeAnalyzer(BlockingQueue<MF_SourceCode> bqMFSourceCode, BlockingQueue<MF_Product> bqMFProduct) {
		this.bqMFSourceCode = bqMFSourceCode; 
		this.bqMFProduct = bqMFProduct;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
