package mfc_netcrawler;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;
public class MFC_NetCrawler implements Callable<String> {

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub - these obviously needs to be much more robust^_^
		// Test when started & finished
		Date startDate = new Date();
		Thread.sleep((new Random()).nextInt(5000));
		Date endDate = new Date();
		return "http://website.com/" + startDate + " finished " + endDate + " <UPC>{" + (int)((new Random()).nextFloat()*100000000) + "}";
	}

}
