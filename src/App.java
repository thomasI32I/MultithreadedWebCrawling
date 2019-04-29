/**
 * 
 *
 */
public class App {

	public static void main(String[] args) {
		
		CrawlPool pool = new CrawlPool(50);
		pool.startCrawlingAtRoot("https://www.kicker.de");
	}
}
