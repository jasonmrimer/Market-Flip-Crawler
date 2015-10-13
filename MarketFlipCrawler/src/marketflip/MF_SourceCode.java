package marketflip;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

/*
 * This will become a passable object either comprised of XML, JSON, or
 * straight code from a webpage. The object will pass between sections of
 * the software as necessary (e.g. the MFC_NetCrawler will grab and instantiate an
 * instnace of MFSourceCode then send that object to the MFCSourceCodeAnalyzer to
 * sort the information as necessary into a more useable MFProduct object).
 */
public class MF_SourceCode {
	private String strSourceCode;
	
	public MF_SourceCode() {
		// TODO Auto-generated constructor stub
	}
	// Construct with the source code to analyze and the blocking queues to use/alter
	public MF_SourceCode(String strSourceCode) {
		// TODO Auto-generated constructor stub
		this.strSourceCode = strSourceCode;
	}
	
	@Override
	public String toString(){
		return strSourceCode;
	}
}
