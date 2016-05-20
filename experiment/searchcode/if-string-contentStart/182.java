package dev.manda.nu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MandaAppActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void searchWiki(View view) {
		System.out.println("wiki");
		EditText wikiSearch = (EditText) findViewById(R.id.wikiSearchText);
		try {
			String searchResult = executeSearch(wikiSearch.getText().toString());
			Intent intent = new Intent(this, WikiActivity.class);
			String information = getPageContext(searchResult, intent);
			intent.putExtra("INFORMATION", information);
			startActivity(intent);
		} catch (Exception e) {
			// TODO make toast
		}
	}

	public void yakatanaRecipe(View view) {
		System.out.println("yakatana");
		Intent intent = new Intent(this, YakatanaActivity.class);
		startActivity(intent);
	}

	public void songList(View view) {
		System.out.println("songs");
		Intent intent = new Intent(this, SongActivity.class);
		startActivity(intent);
	}

	public void imageGallery(View view) {
		System.out.println("images");
	}

	private String executeSearch(String searchWord)
			throws Exception {
		BufferedReader in = null;
		String page = "";
		// searchWord = searchWord.replaceAll("ĺ", "%C3%A5");
		// searchWord = searchWord.replaceAll("ä", "%C3%A4");
		// searchWord = searchWord.replaceAll("ö", "%C3%B6");
		// searchWord = searchWord.replaceAll("Ĺ", "%C3%A5");
		// searchWord = searchWord.replaceAll("Ä", "%C3%A5");
		// searchWord = searchWord.replaceAll("Ö", "%C3%A5");
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI("http://manda.nu/wiki/index.php/Special:S%C3%B6k?search=" + searchWord + "&go=G%C3%A5+till"));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			page = sb.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return page;
	}

	private String getPageContext(String searchResult, Intent intent) {
		String content = "";
		int contentStart = searchResult.indexOf("<!-- start content -->");
		int contentEnd = searchResult.indexOf("<!-- end content -->");
		searchResult = searchResult.substring(contentStart, contentEnd);
		if (searchResult.contains("Kategori:Arbetare")) {
			content = parseArbetare(searchResult, intent);
		} else {
			// TODO parsa annat
		}
		return content;
	}

	private String parseArbetare(String searchResult, Intent intent) {
		String arbetareContent = getFaktaruta(searchResult);
		arbetareContent += "\nBeskrivning:\n" + getDescription(searchResult);
		String imageURL = getImageURL(searchResult);
		if (imageURL != "") {
			intent.putExtra("IMAGE", imageURL);
		}
		return arbetareContent;
	}

	private String getImageURL(String searchResult) {
		String imageURL = "";
		int startImage = searchResult.indexOf("<div class=\"thumbinner\"");
		if (startImage != -1) {
			int endImage = searchResult.indexOf("</a>", startImage);
			imageURL = searchResult.substring(startImage, endImage);
			Pattern p = Pattern.compile("src=\".*?\"");
			Matcher matcher = p.matcher(imageURL);
			if (matcher.find()) {
				imageURL = matcher.group();
				imageURL = "http://manda.nu" + imageURL.substring(5, imageURL.length() - 1);
			} else {
				imageURL = "";
			}
		}
		return imageURL;
	}

	private String getDescription(String searchResult) {
		int descriptionStart = searchResult.indexOf("</table>");
		int descriptionEnd = searchResult.indexOf("<div class=\"", descriptionStart);
		String description = searchResult.substring(descriptionStart, descriptionEnd);
		descriptionEnd = description.lastIndexOf("</p>");
		description = description.substring(0, descriptionEnd);
		description = formatDescription(description);
		description = description.trim();
		return description;
	}

	private String formatDescription(String description) {
		description = description.replaceAll("\n", "");
		description = description.replaceAll("</p>", "\n");
		description = description.replaceAll("<.*?>", "");
		description = description.replaceAll("\\.", ". ");
		description = description.replaceAll("\\.  ", ". ");
		description = description.replaceAll("\\?", "? ");
		description = description.replaceAll("\\?  ", "? ");
		description = description.replaceAll("!", "! ");
		description = description.replaceAll("!  ", "! ");
		return description;
	}

	private String getFaktaruta(String searchResult) {
		String arbetareContent = "";
		int factStart = searchResult.indexOf("<table>");
		int factEnd = searchResult.indexOf("</table>");
		String fact = searchResult.substring(factStart, factEnd);
		factStart = fact.indexOf("Namn");
		fact = fact.substring(factStart);
		fact = fact.replaceAll("<\\S*>", "");
		fact = fact.replaceAll("[\n]", "");
		String[] split = fact.split("[\\s]");
		for (int i = 0; i < split.length; i++) {
			if (split[i].getBytes().length != 0) {
				if (split[i].equals("Namn")) {
					arbetareContent = split[i] + ":";
				} else if (split[i].equals("Generation") || split[i].equals("Kallas") || split[i].equals("Ĺrgĺng") || split[i].equals("Läs(er/te)")) {
					arbetareContent += "\n" + split[i] + ":";
				} else {
					arbetareContent += " " + split[i];
				}
			}
		}
		arbetareContent += "\n";
		return arbetareContent;
	}
}

