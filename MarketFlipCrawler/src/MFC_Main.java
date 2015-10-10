/*
 * Main will eventually run the threads of the site crawler and the database crawler.
 * Jason M. Rimer, 20151009
 */

public class MFC_Main {
	/*
	 * This main method only serves to start the program then
	 * branch into subclasses & methods with more freedom to operate
	 * threads and diverge into a database crawler and internet crawler
	 */
	public static void main(String args[]){
//		MFC_DatabaseCrawler dbCrawler = new MFC_DatabaseCrawler();
//		MFC_InternetCrawler netCrawler = new MFC_InternetCrawler();
		(new Thread(new MFC_DatabaseCrawler())).start();
		(new Thread(new MFC_InternetCrawler())).start();
	}
}
