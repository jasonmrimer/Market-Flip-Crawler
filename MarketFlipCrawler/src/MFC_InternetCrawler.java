
public class MFC_InternetCrawler implements Runnable {

	@Override
	public void run() {
		/*
		 * The netCrawler will constantly run, crawling the Internet for pages with
		 * products for sale. Each time it discovers a product, it will pass it to 
		 * the dbCrawler for processing.
		 */
		while (true) {
			System.out.println("net");
		}
	}
}
