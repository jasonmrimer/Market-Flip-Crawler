import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MFC_DatabaseCrawler implements Runnable{
//	private Executors executor = new ExecutorService();
	
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
