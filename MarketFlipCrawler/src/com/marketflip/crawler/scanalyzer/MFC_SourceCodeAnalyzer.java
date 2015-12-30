<<<<<<< HEAD
package com.marketflip.crawler.scanalyzer;
/*
 * SourceCodeAnalyzer runs as a Callable in order to be trackable as a Future
 * in the threads managed by SourceCodeAnalysisManager. The object methods parse
 * source code in search of pertinent product data in hope to turn that code into
 * a useable MF_Product object. That MF_Product object will 
 */

import java.util.concurrent.Callable;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.marketflip.shared.products.MF_Product;

public class MFC_SourceCodeAnalyzer implements Callable<MF_Product> {
	private Document siteDoc;	// Future object with more advanced possibilites of XML, JSON, etc.
	
	public MFC_SourceCodeAnalyzer(Document document) {
		this.siteDoc = document;
	}
	
	@Override
	public MF_Product call() throws Exception {
		return analyzeCode();
	}
	
	private MF_Product analyzeCode() {
		if (hasUPC()){
			MF_Product product = new MF_Product();
			String UPC = siteDoc.select("meta[property=og:upc").attr("content");
			String title = siteDoc.select("meta[property=og:title").attr("content");
			String description = siteDoc.select("div.about-item-preview-text").html();
			System.out.println("about to return MF_Product");
			product.setUPC(UPC);
			product.setName(title);
			product.setDescription(description);
			return product;
		}
		else return null;
	}
	private boolean hasUPC() {
		// TODO Auto-generated method stub
		return !siteDoc.select("meta[property=og:upc").attr("content").isEmpty();
	}
	
	@Override
	public String toString() {
		String stringRepresentation;
		stringRepresentation = "MFC_SourceCodeAnalyzer object with document title: " + siteDoc.title();
		return stringRepresentation;
	}

}
=======
package com.marketflip.crawler.scanalyzer;
/*
 * SourceCodeAnalyzer runs as a Callable in order to be trackable as a Future
 * in the threads managed by SourceCodeAnalysisManager. The object methods parse
 * source code in search of pertinent product data in hope to turn that code into
 * a useable MF_Product object. That MF_Product object will 
 */

import java.util.concurrent.Callable;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.marketflip.shared.products.MF_Product;

public class MFC_SourceCodeAnalyzer implements Callable<MF_Product> {
	private Document siteDoc;	// Future object with more advanced possibilites of XML, JSON, etc.
	
	public MFC_SourceCodeAnalyzer(Document document) {
		this.siteDoc = document;
	}
	
	@Override
	public MF_Product call() throws Exception {
		return analyzeCode();
	}
	
	private MF_Product analyzeCode() {
		if (hasUPC()){
			MF_Product product = new MF_Product();
			String UPC = siteDoc.select("meta[property=og:upc").attr("content");
			String title = siteDoc.select("meta[property=og:title").attr("content");
			String description = siteDoc.select("div.about-item-preview-text").html();
			System.out.println("about to return MF_Product");
			product.setUPC(UPC);
			product.setName(title);
			product.setDescription(description);
			return product;
		}
		else return null;
	}
	private boolean hasUPC() {
		// TODO Auto-generated method stub
		return !siteDoc.select("meta[property=og:upc").attr("content").isEmpty();
	}
	
	@Override
	public String toString() {
		String stringRepresentation;
		stringRepresentation = "MFC_SourceCodeAnalyzer object with document title: " + siteDoc.title();
		return stringRepresentation;
	}

}
>>>>>>> refs/heads/TestingFocus_V2
