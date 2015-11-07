package mfc_netcrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;

import javax.net.ssl.SSLHandshakeException;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * MFC_NetCrawler is the producer class for the NetCrawlerManager. NetCrawler
 * serves up new websites for the Manager for to designate a thread. By giving
 * each website a thread, the Manager can process issues and delays for which a
 * straighthrough crawler would stall. NetCrawler can limit how deep it crawls
 * or can process infinitely.
 * 
 * @author Atlas
 *
 *         TODO Note from site: Hey, nice post. It's worth mentioning in
 *         crawling you should parse the domain's robots.txt first and create a
 *         URL exclusion set to make sure you don't anger any webmasters ;)
 */
public class MFC_NetCrawler implements Callable<MFC_NetCrawler> {
	private final int			MAX_SITE_DEPTH	=	10;
	private Document			siteDoc; // Use to pass to SourceCode Analyzer through pipeline
	private Collection<String>	URLs			=	new ArrayList<String>();
	private MFC_TempDB			database;
	private String				startURL;
	private ResultSet			resultSet;
	private int callCount = 0;
	
//	public MFC_NetCrawler(String startURL) { 			
//		this.startURL = startURL;
//	
//	}

	public MFC_NetCrawler(MFC_TempDB database, String startURL) {
		// TODO Auto-generated constructor stub
		this.database = database;
		this.startURL = startURL;
	}

	public void runJSoup(){
		try {
			boolean isURLRecorded = database.isRecorded(startURL);
			resultSet = database.getURLResultSet(startURL);
			// Test whether in database
			if (database.isRecorded(startURL)){	// only check the resultset with first() *do not move cursor
				System.out.println("from netcrawler: already in db");
			}
			else {
				if (startURL.startsWith("http://")) {
					String contentType = new String(Jsoup.connect(startURL).ignoreContentType(true).execute().contentType());
					if (contentType.startsWith("text/") || contentType.startsWith("application/xml") || 
							contentType.startsWith("application/xhtml+xml")){
						System.out.println("netcrawler 64, type: " + contentType);
						siteDoc = Jsoup.connect(startURL).get();
						Elements links = siteDoc.select("a[href]");
						for (Element link : links) {
//							ResultSet linkResultSet = database.getURLResultSet(link.attr("abs:href")); 
							if (database.isRecorded(link.attr("abs:href"))) {	// move cursor to row and use resultSet
//								String url = linkResultSet.getString(1);	// TODO get more generic argument to assure dynamic code
								System.out.println("netcrawler 69, new link, " + link.attr("abs:href") + ", already in db ");
							}
							else {
								System.out.println("not in db: " + link.attr("abs:href"));
//								database.insertURLToWebsiteTable(link.attr("abs:href"));	// putting it in the database here is bad - only insert WHEN completing a crawl
								String linkContentType = new String(Jsoup.connect(startURL).ignoreContentType(true).execute().contentType());
								if ((linkContentType.startsWith("text/") || linkContentType.startsWith("application/xml") || 
										linkContentType.startsWith("application/xhtml+xml")) && link.attr("abs:href").startsWith("http://")){
									// TODO only accept hhtp:// for now to speed crawling due to errors
									URLs.add(link.attr("abs:href"));
									System.out.println("added to array: " + link.attr("abs:href"));
								}
							}
						}
					}
					else siteDoc = null;
					database.insertURLToWebsiteTable(startURL);	// insert to db after crawl complete
				}
				resultSet.close();
			}
		} catch (SSLHandshakeException | MalformedURLException | HttpStatusException e) {
			// TODO handle exception SSL the issue by getting the proper certifications for HTTPS websites: https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle exception Malformed by filtering missing URL parts: https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle httpstatusexception seemingly from 404 not founds
			System.err.println("MFC_NetCrawler non-IO exception to JSoup connection:");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("MFC_NetCrawler IO exception to JSoup connection:");
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.err.println("MFC_NetCrawler SQL exception to JSoup connection:");
			e.printStackTrace();
		}
	}
	/**
	 * Upon completion, this method will return a future to the its executor. In
	 * particular, the source code of the website as a JSoup Document.
	 */
	@Override
	public MFC_NetCrawler call() throws Exception {
		// TODO Auto-generated method stub - these obviously needs to be much more robust^_^
		runJSoup();
		return this;
	}

	/**
	 * Getters: used to pass the source doc to the sourcecodanalyzer and the
	 * URLs to iterate through in the manager
	 * 
	 * @return
	 */
	public Document getSiteDoc() {
		return siteDoc;
	}

	public Collection getURLs() {
		return URLs;
	}

	public MFC_TempDB getDatabase() {
		return database;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public String getStartURL() {
		return startURL;
	}
}
