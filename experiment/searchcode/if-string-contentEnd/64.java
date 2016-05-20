import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * This class is a basic HTML parser made to be able to take in a url string,
 * and then expose the data you want, how you want, with none of the bullshit of
 * other parsers trying to 'look elegant'.
 * 
 * @author Matthew Boyer
 * 
 */
public class HtmlParser
{
	// TODO remove variable assignments and remove all static assignments, make
	// project a library
	private static String urlToParse = "http://crossfit.com";
	private String webPageAsString;
	private static boolean loggingOn = true;
	private static StringBuilder pageHTMLString = new StringBuilder();

	/**
	 * Constructor. Takes in the string url you want to parse and if you want
	 * debug logging to be turned on or off.
	 * 
	 * @param urlToParse
	 * @param loggingOn
	 */
	public HtmlParser(String urlToParse, boolean loggingOn)
	{
		this.urlToParse = urlToParse;
		this.loggingOn = loggingOn;
	}

	public static void main(String... args)
	{
		openURLOnThread();
		getContentsByTagAndClass("div", "blogbody");
	}

	/**
	 * Allows you to pull string contents out by looking for a tag and it's css
	 * class
	 * 
	 * @param tag
	 *            - string tag value, ex 'p', 'a', 'span'
	 * @param cssClass
	 *            - string css class value
	 */
	public static String getContentsByTagAndClass(final String tag,
			final String cssClass)
	{
		String contents = "";
		String startTag = "<" + tag;
		String endTag = "</" + tag;
		ArrayList<String> contentFound = new ArrayList<String>();
		String contentStartTag = "<p>";
		String contentEndTag = "</p>";

		if(pageHTMLString.length() > 0 || pageHTMLString != null)
		{
			int firstStartTagIndex = pageHTMLString.indexOf(cssClass);
			int firstEndTagIndex = pageHTMLString.indexOf(startTag,
					firstStartTagIndex);

			String test = pageHTMLString.substring(firstStartTagIndex,
					firstEndTagIndex);

			int nextStartIndex = pageHTMLString.indexOf(cssClass,
					firstEndTagIndex);
			int nextEndIndex = pageHTMLString.indexOf(startTag, nextStartIndex);

			String test2 = pageHTMLString.substring(nextStartIndex,
					nextEndIndex);
			int contentStart = test2.indexOf(contentStartTag);
			int contentEnd = test2.indexOf(contentEndTag);
			// TODO check this the next few days and make sure it gets the
			// current day's wod data correctly
			String desiredContent = test2.substring(contentStart, contentEnd);
			System.out.println(desiredContent);
		}
		else
		{
			return null;// our pageHTMLString is empty and we can't do anything
		}

		return contents;
	}

	/**
	 * Allows you to pull string contents out by looking for a tag and it's css
	 * id.
	 * 
	 * @param tag
	 *            - string tag value, ex 'p', 'a', 'span'
	 * @param cssID
	 *            - string css id value
	 */
	public String getContentsByTagAndID(final String tag, final String cssID)
	{
		String contents = null;

		return contents;
	}

	/**
	 * Opens the url on a thread and returns the entire page as a string. It
	 * waits until thread completes before returning.
	 * 
	 * @return
	 */
	public static StringBuilder openURLOnThread()
	{
		Thread t = new Thread(new Runnable() {
			public void run()
			{
				try
				{
					URL url = new URL(urlToParse);
					URLConnection yc = url.openConnection();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(yc.getInputStream()));
					String inputLine;
					while((inputLine = in.readLine()) != null)
					{
						pageHTMLString.append(inputLine);
						pageHTMLString.append("\n");
					}
					in.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		t.start();
		while(t.isAlive())
		{
			// do nothing
		}
		if(loggingOn)
		{
			// System.out.println(pageHTMLString);
		}
		return pageHTMLString;
	}

	/**
	 * Opens the url on calling thread and returns the entire page as a string.
	 * 
	 * @return
	 */
	public static StringBuilder openURLOffThread()
	{
		try
		{
			URL url = new URL(urlToParse);
			URLConnection yc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream()));
			String inputLine;
			while((inputLine = in.readLine()) != null)
			{
				pageHTMLString.append(inputLine);
				pageHTMLString.append("\n");
			}
			in.close();
			if(loggingOn)
			{
				System.out.println(pageHTMLString);
			}
			return pageHTMLString;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}

