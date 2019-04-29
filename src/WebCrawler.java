import java.io.IOException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Implementation of a WebCrawler as a Runnable type.
 *
 */
public class WebCrawler implements Runnable {

	private String name;
	private int urlsLimit;
	private LinkedBlockingQueue<String[]> sharedUrlPool;
	private Set<String> urlsVisited;
	
	private String element[];
	private String url;
	private String layer;
	private Document doc;
	private Elements links;
	
	private WebCrawlerListener listener;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param SharedUrlPool
	 * @param urlsVisited
	 * @param urlsLimit
	 */
	public WebCrawler(String name,
					  LinkedBlockingQueue<String[]> SharedUrlPool,
					  Set<String> urlsVisited,
				      int urlsLimit) {
		
		this.name = name;
		this.sharedUrlPool = SharedUrlPool;
		this.urlsVisited = urlsVisited;
		this.urlsLimit = urlsLimit;
	}

	/**
	 * Main thread body
	 */
	@Override
	public void run() {
		
		while (urlsVisited.size() < urlsLimit) {
			try {
				element = sharedUrlPool.take(); // wait here if no elements are in queue
				url = element[0];
				layer = element[1];

			} catch (InterruptedException e) {
				e.getMessage();
			}

			// ensure connection to url is valid and that it hasn't been scraped already
			if (checkUrl(url)) {
				System.out.println(urlsVisited.size() + " " + name + " scraping " + url + " at layer: " + layer);
				
				int newLayer = Integer.parseInt(layer) + 1;
				for (Element link : links) {
					String element[] = { link.attr("abs:href"), ("" + newLayer) };
					 // exclude duplicate links on same page
					if (!urlsVisited.contains(element[0])) {
						// add to tail of shared queue
						sharedUrlPool.offer(element);
					}
				}
			}
		}
		//fire event that urlLimit is reached
		listener.urlLimitReached();
	}

	/**
	 * Check the passed url.
	 * 
	 * @param url
	 * @return
	 */
	public boolean checkUrl(String url) {
		boolean flag = false;
		if (connectToUrl(url) && urlsVisited.add(url) && urlsVisited.size() <= urlsLimit) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * Check if connection to url is established and store html doc with elements
	 * in corresponding object.
	 * 
	 * @param url
	 * @return
	 */
	public boolean connectToUrl(String url) {
		boolean success;
		if (url != null) {
			try {
				doc = Jsoup.connect(url).get();
				links = doc.select("a[href]");
				success = true;
			} catch (IOException e) {
				e.getMessage();
				success = false;
			} catch (IllegalArgumentException e) {
				System.out.println("skipping a malformed url");
				success = true;
			}
		} else {
			success = false;
		}
		return success;
	}

	public void setListener(WebCrawlerListener listener) {
		this.listener = listener;
	}
}