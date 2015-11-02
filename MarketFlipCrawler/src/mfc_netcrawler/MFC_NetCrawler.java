package mfc_netcrawler;

import java.io.IOException;
import java.net.MalformedURLException;
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
	private final int	MAX_SITE_DEPTH	=	10;
	private Document	siteDoc; // Use to pass to SourceCode Analyzer through pipeline
	private Collection	URLs			=	new ArrayList<String>();

	public MFC_NetCrawler(String startURL) { 			
		
		try {
			if (startURL.startsWith("http://")) {
				String contentType = new String(Jsoup.connect(startURL).ignoreContentType(true).execute().contentType());
				if (contentType.startsWith("text/") || contentType.startsWith("application/xml") || 
						contentType.startsWith("application/xhtml+xml")){
					siteDoc = Jsoup.connect(startURL).get();
					Elements links = siteDoc.select("a[href]");
					for (Element link: links) {
						String linkContentType = new String(Jsoup.connect(startURL).ignoreContentType(true).execute().contentType());
						if ((linkContentType.startsWith("text/") || linkContentType.startsWith("application/xml") || 
								linkContentType.startsWith("application/xhtml+xml")) && link.attr("abs:href").startsWith("http://")){
							// TODO only accept hhtp:// for now to speed crawling due to errors
							URLs.add(link.attr("abs:href"));
						}
					}
				}
				else siteDoc = null;
			}
		} catch (SSLHandshakeException | MalformedURLException | HttpStatusException e) {
			// TODO handle exception SSL the issue by getting the proper certifications for HTTPS websites: https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle exception Malformed by filtering missing URL parts: https://confluence.atlassian.com/display/KB/Unable+to+Connect+to+SSL+Services+due+to+PKIX+Path+Building+Failed
			// TODO handle httpstatusexception seemingly from 404 not founds
			System.err.println("MFC_NetCrawler non-IO exception to JSoup connection:");
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("MFC_NetCrawler IO exception to JSoup connection:");
//			e.printStackTrace();
		}
	}

	/**
	 * Upon completion, this method will return a future to the its executor. In
	 * particular, the source code of the website as a JSoup Document.
	 */
	@Override
	public MFC_NetCrawler call() throws Exception {
		// TODO Auto-generated method stub - these obviously needs to be much more robust^_^
		return this;
	}

	/**
	 * Getters: used to pass the source doc to the sourcecodanalyzer and the
	 * URLs to iterate through
	 * 
	 * @return
	 */
	public Document getSiteDoc() {
		return siteDoc;
	}

	public Collection getURLs() {
		return URLs;
	}
}
