import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class Utils {
	
	@SuppressWarnings("unused")
	private static Document doc;
	
	/**
	 * Check if connection to root url is correct
	 * 
	 * @param url
	 * @return
	 */
	public static boolean connectToUrl(String url) {
		boolean success;
		if (url != null) {
			try {
				doc = Jsoup.connect(url).get();
				success = true;
			} catch (Exception e) {
				success = false;
			}
		} else {
			success = false;
		}
		return success;
	}
}