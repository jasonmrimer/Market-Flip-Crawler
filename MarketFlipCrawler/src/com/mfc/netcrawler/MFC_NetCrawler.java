package com.mfc.netcrawler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.appengine.repackaged.org.codehaus.jackson.map.RuntimeJsonMappingException;

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

	private final int			MAX_SITE_DEPTH	= 1;
	private Document			siteDoc;
	private Collection<String>	URLs;
	private MFC_WebsiteDAO		database;
	private String				startURLBeforeHash;
	private String				startURLAfterHash;

	public MFC_NetCrawler() {
		this.URLs = new ArrayList<String>();
		this.siteDoc = null;
	}

	/**
	 * This is the common interface to create the class that will be used
	 * in the real program when connecting to a website database and having an
	 * actual website URL.
	 * 
	 * @param database
	 * @param startURL
	 */
	public MFC_NetCrawler(MFC_WebsiteDAO database, String startURL) {
		this(startURL);
		this.database = database;
	}

	/**
	 * This constructor is a mock object that operates outside of the useful system
	 * wherein it is only given a local file path rather than a database connection and
	 * an URL from the Internet (i.e., designed only for testing)
	 * 
	 * @param localFilePath
	 */
	public MFC_NetCrawler(String localFilePath) {
		this();
		/*
		 * Uses the SHA-256 hashing algorithm to return a unique string for each URL.
		 * The database stores each hashed string as the identifier for the URL to ensure
		 * it accounts for all URLs regardless of length
		 */
		this.startURLBeforeHash = localFilePath;
		this.startURLAfterHash = DigestUtils.sha256Hex(localFilePath);
	}

	/**
	 * Upon completion, this method will return a future to the its executor. In
	 * particular, the source code of the website as a JSoup Document.
	 */
	@Override
	public MFC_NetCrawler call() {
		runJSoup();
		return this;
	}

	/**
	 * This method uses several JSoup tools to extract the site document,
	 * links array, and content type to facilitate analysis and crawling.
	 * It also checks the database to see if the program already examined the
	 * website to be processed.
	 */
	private void runJSoup() {
		createJSDocument();
		collectLinks();
		catalogVisit();
	}

	/**
	 * This method catalogs the visit to a website by inserting the
	 * URL into a website database and may evolve to include the date, time, etc.
	 */
	private void catalogVisit() {
		// Insert into database to catalog visit
		if (database != null) {
			database.insertURLToWebsiteTable(startURLAfterHash);
		}
	}

	/**
	 * This method iterates through all the links in an HTML Doc and
	 * adds the absolute references to the object's collection.
	 */
	private void collectLinks() {
		// fetch the array of links
		Elements links = siteDoc.select("a[href]");
		// iterate each link inside the link array from the siteDoc
		for (Element link : links) {
			/*
			 * Use a separate method to handle each link in order to catch errors specific to each
			 * link rather than the overlapping URLs throughout the runJSoup method
			 */
			URLs.add(link.attr("abs:href"));
		}
	}

	/**
	 * The purpose of this method is to test the URL or path to a website or local document
	 * and determine whether JSoup can properly extract a Document as well as catch all errors
	 * the process throws.
	 */
	private void createJSDocument() {
		try {
			/*
			 * From: http://jsoup.org/cookbook/input/load-document-from-url
			 * [Jsoup.connect(StringURL)] only suports web URLs (http and https protocols); if you
			 * need to load
			 * from a file, use the parse(File in, String charsetName) method instead.
			 */
			if (startURLBeforeHash.startsWith("http://")) {
				/*
				 * TODO The tester limits the type of websites that the code
				 * attempts to pull from in order to limit errors. We need to
				 * expand this to a more elaborate testing scheme because this
				 * is more like a "duct tape" solution for our temporary
				 * testing.
				 */
				// TODO ignoreContentType is part of the bad solution but gets
				// what we need to examine:
				Connection jsoupCon = Jsoup.connect(startURLBeforeHash).timeout(10000);
				String contentType = new String(
						jsoupCon.ignoreContentType(true).execute().contentType());
				if (contentType.startsWith("text/") || contentType.startsWith("application/xml")
						|| contentType.startsWith("application/xhtml+xml")) {
					/*
					 * This iteration is the heart of "crawling" as it takes all
					 * the links out of a website's Document and queues each
					 * link for a visit by a Future in the NetCrawlerManager
					 */
					this.siteDoc = Jsoup.connect(startURLBeforeHash).timeout(10000).get();
				}
			}
			// This block includes local files that are HTML docs (e.g., for mock objects)
			else if (startURLBeforeHash.endsWith(".html")) {
				siteDoc = Jsoup.parse(new File(startURLBeforeHash), "UTF-8");
			}
			else siteDoc = null;
		}
		catch (SSLHandshakeException e) {
			System.err.println("SSLHandshakeException for: " + startURLBeforeHash);
		}
		catch (MalformedURLException | HttpStatusException e) {
			// TODO handle exception SSL the issue by getting the proper
			// certifications for HTTPS websites:
			// https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle exception Malformed by filtering missing URL parts:
			// https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle httpstatusexception seemingly from 404 not founds
			System.err.println("MFC_NetCrawler non-IO exception  for: " + startURLBeforeHash);
			e.printStackTrace();
		}
		catch (SocketTimeoutException e) {
			System.err.println("SocketTimeoutException for: " + startURLBeforeHash);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("MFC_NetCrawler IO exception for " + startURLBeforeHash);
		}
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

	public Collection<String> getURLs() {
		return URLs;
	}

	public MFC_WebsiteDAO getDatabase() {
		return database;
	}

	public String getStartURL() {
		return startURLBeforeHash;
	}
}