package mfc_netcrawler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * MFC_NetCrawler is the producer class for the NetCrawlerManager. NetCrawler serves up
 * new websites for the Manager for to designate a thread. By giving each website a thread,
 * the Manager can process issues and delays for which a straighthrough crawler would stall.
 * NetCrawler can limit how deep it crawls or can process infinitely. 
 * @author Atlas
 *
 * TODO Note from site: Hey, nice post. It's worth mentioning in crawling you should parse the domain's robots.txt first and create a URL exclusion set to make sure you don't anger any webmasters ;)
 */
public class MFC_NetCrawler implements Callable<MFC_NetCrawler> {
	private final int			MAX_SITE_DEPTH	=	10;
	private Document 			siteDoc;	// Use to pass to SourceCode Analyzer through pipeline
	private Collection 			URLs 			=	 new ArrayList<String>();
	
	public MFC_NetCrawler(String startURL) {
		try {
			Connection.Response response = Jsoup.connect(startURL).ignoreContentType(true).execute();
//			System.out.println("type: " + response.contentType());
			if (response.contentType().startsWith("text/") || response.contentType().equals("application/xml") || response.contentType().equals("application/xhtml+xml")){
				siteDoc = Jsoup.connect(startURL).get();
				Elements links = siteDoc.select("a[href]");
				for (Element link: links) {
					Connection.Response URLresponse = Jsoup.connect(link.attr("abs:href")).ignoreContentType(true).execute();
//					System.out.println("type: " + URLresponse.contentType());
					if (URLresponse.contentType().startsWith("text/") || URLresponse.contentType().equals("application/xml") || URLresponse.contentType().equals("application/xhtml+xml")){
						URLs.add(link.attr("abs:href"));
					}
				}
			}
			else siteDoc = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Upon completion, this method will return a future to the its executor. In particular,
	 * the source code of the website as a JSoup Document.
	 */
	@Override
	public MFC_NetCrawler call() throws Exception {
		// TODO Auto-generated method stub - these obviously needs to be much more robust^_^
		// Test when started & finished *test worked
//		Date startDate = new Date();
//		Thread.sleep((new Random()).nextInt(5000));
//		Date endDate = new Date();
		return this;
//		return "http://website.com/" + startDate + " finished " + endDate + " <UPC>{" + (int)((new Random()).nextFloat()*100000000) + "}";
	}
	
	/**
	 * Getters: used to pass the source doc to the sourcecodanalyzer and the URLs to iterate through
	 * @return
	 */
	public Document getSiteDoc() {
		return siteDoc;
	}

	public Collection getURLs() {
		return URLs;
	}
}
