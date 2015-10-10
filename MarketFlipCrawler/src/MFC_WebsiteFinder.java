import java.util.Random;
import java.util.concurrent.*;
public class MFC_WebsiteFinder implements Callable<String> {

	@Override
	public String call() throws Exception {
		// TODO Auto-generated method stub - these obviously needs to be much more robust^_^
		Thread.sleep(500);
		return "http://website.com/" + (new Random()).nextInt(1000000);
	}

}
