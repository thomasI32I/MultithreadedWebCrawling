import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * 
 */
public class CrawlPool {

	private final int URLS_LIMIT = 1000;
	//default
	private int crawlerThreads = 5;
	
	LinkedBlockingQueue<String[]> sharedUrlPool;
	Set<String> urlsVisited;
	final ExecutorService executor;
	//first element will be empty
	WebCrawler[] crawlers;

	/**
	 * Constructor
	 */
	public CrawlPool() {
		
		crawlers = new WebCrawler[crawlerThreads + 1];
		sharedUrlPool = new LinkedBlockingQueue<String[]>(URLS_LIMIT);
		urlsVisited = Collections.synchronizedSet(new HashSet<>(URLS_LIMIT));
		executor = Executors.newFixedThreadPool(crawlerThreads);
	}
	
	/**
	 * Constructor
	 * 
	 * @param crawlerThreads Number of crawler threads to use.
	 */
	public CrawlPool(int crawlerThreads) {
		
		this.crawlerThreads = crawlerThreads;
		crawlers = new WebCrawler[this.crawlerThreads + 1];
		sharedUrlPool = new LinkedBlockingQueue<String[]>(URLS_LIMIT);
		urlsVisited = Collections.synchronizedSet(new HashSet<>(URLS_LIMIT));
		executor = Executors.newFixedThreadPool(this.crawlerThreads);
	}
	
	/**
	 * Method to start crawling process at the root url.
	 * @param url The root url to start from.
	 */
	public void startCrawlingAtRoot(String url) {
		insertRoot(url);
		initCrawlers();
		startCrawlers();
	}
	
	/**
	 * Insert root url into pool.
	 * 
	 * @param url
	 * @return
	 */
	private boolean insertRoot(String url) {
		
		String element[] = { url, "1" };
		if (Utils.connectToUrl(url)) {
			sharedUrlPool.offer(element);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Create the WebCrawler instances.
	 */
	private void initCrawlers() {
		for (int count = 1; count <= crawlerThreads; count++) {
			
			crawlers[count] = new WebCrawler("[ Crawler " + count + " ]", sharedUrlPool, urlsVisited, URLS_LIMIT);
			crawlers[count].setListener(new WebCrawlerListener() {
				@Override
				public void urlLimitReached() {
					shutdownPool();
				}
			});
		}
	}
	
	/**
	 * Execute the WebCrawler instances in new threads.
	 */
	private void startCrawlers() {
		for (int count = 1; count <= crawlerThreads; count++) {
			executor.execute(crawlers[count]);
		}
	}
	
	/**
	 * shutdown thread pool
	 */
	private void shutdownPool() {
		executor.shutdown();
	}
}