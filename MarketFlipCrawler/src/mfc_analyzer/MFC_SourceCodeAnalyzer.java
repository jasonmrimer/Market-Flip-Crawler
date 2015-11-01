package mfc_analyzer;
/*
 * SourceCodeAnalyzer runs as a Callable in order to be trackable as a Future
 * in the threads managed by SourceCodeAnalysisManager. The object methods parse
 * source code in search of pertinent product data in hope to turn that code into
 * a useable MF_Product object. That MF_Product object will 
 */

import java.util.concurrent.Callable;

import org.jsoup.nodes.Document;

import marketflip.MF_Product;
import marketflip.MF_SourceCode;

public class MFC_SourceCodeAnalyzer implements Callable<MF_Product> {
	private Document sourceCode;	// Future object with more advanced possibilites of XML, JSON, etc.
	private String strSourceCode;		// Simplifed to String for testing
	
	public MFC_SourceCodeAnalyzer(Document document) {
		this.sourceCode = document;
		this.strSourceCode = document.toString();
	}
	@Override
	public MF_Product call() throws Exception {
		return analyzeCode();
	}
	private MF_Product analyzeCode() {
		if (strSourceCode.indexOf("<UPC>") > 0) {
			return new MF_Product("UPC: " + strSourceCode.substring(strSourceCode.indexOf("<UPC>{") + 6,
					strSourceCode.indexOf("}", strSourceCode.indexOf("<UPC>"))));	// Assumes nomenclature: <UPC>{12345678}
		}
		else return new MF_Product("not a product");
	}

}
