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
	private final int MAX_SITE_DEPTH = 10;
	private Document siteDoc; // Use to pass to SourceCode Analyzer through
								// pipeline
	private Collection<String> URLs = new ArrayList<String>();
	private MFC_TempDB database;
	private String startURL;
	private ResultSet resultSet;
	private int callCount = 0;

	public MFC_NetCrawler(MFC_TempDB database, String startURL) {
		// TODO Auto-generated constructor stub
		this.database = database;
		this.startURL = startURL;
	}

	public void runJSoup(){
		try {
			/*
			 * If not recorded & a proper website, crawl it.  
			 * 
			 * former: System.out.println("from netcrawler: already in db");	
			 * // TODO Junit to ensure it properly finds already recorded URLs
			 */
			if (!database.isRecorded(startURL) && startURL.startsWith("http://")){	
				/*
				 * TODO The tester limits the type of websites that the code attempts to pull from 
				 * in order to limit errors. We need to expand this to a more elaborate testing
				 * scheme because this is more like a "duct tape" solution for our temporary testing.
				 */
				// TODO ignoreContentType it part of the bad solution but gets what we need to examine:
				String contentType = new String(Jsoup.connect(startURL).ignoreContentType(true).execute().contentType());	
				if (contentType.startsWith("text/") || contentType.startsWith("application/xml") || 
						contentType.startsWith("application/xhtml+xml")){
					/*
					 * This iteration is the heart of "crawling" as it takes all the links
					 * out of a website's Document and queues each link for a visit by a 
					 * Future in the NetCrawlerManager
					 */
					siteDoc = Jsoup.connect(startURL).get();	// fetch the site document that contains HTML-tagged data from JSoup 
					Elements links = siteDoc.select("a[href]");	// fetch the array of links
					for (Element link : links) {				// iterate each link inside the link array from the siteDoc
						if (!database.isRecorded(link.attr("abs:href"))) {	// move cursor to row and use resultSet
							String linkContentType = new String(Jsoup.connect(startURL).ignoreContentType(true).execute().contentType());
							if ((linkContentType.startsWith("text/") || linkContentType.startsWith("application/xml") || 
									linkContentType.startsWith("application/xhtml+xml")) && link.attr("abs:href").startsWith("http://")){
								// TODO only accept http:// for now to speed crawling due to errors
								URLs.add(link.attr("abs:href"));
							}
						}
					}
				}
				else siteDoc = null;
				database.insertURLToWebsiteTable(startURL);	// insert to db after crawl complete
			}
		} catch (SSLHandshakeException | MalformedURLException | HttpStatusException e) {
			// TODO handle exception SSL the issue by getting the proper
			// certifications for HTTPS websites:
			// https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle exception Malformed by filtering missing URL parts:
			// https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle httpstatusexception seemingly from 404 not founds
			System.err.println("MFC_NetCrawler non-IO exception to JSoup connection:");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("MFC_NetCrawler IO exception to JSoup connection:");
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
