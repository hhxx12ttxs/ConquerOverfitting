package com.weanticipate.harvester;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.weanticipate.harvester.EventSource.BaseEvent;
import com.weanticipate.resources.media.MediaProvider;
import com.weanticipate.resources.web.WebFetcher;

public class GruvrLiveMusicEventSource implements EventSource, Iterator<BaseEvent> {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final SimpleDateFormat shortDateParser = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat longDateParser = new SimpleDateFormat(
			"EEEE, MMMM dd, yyyy 'at' h:mm a");

	private static final Pattern cityFeedPattern = Pattern
			.compile("\"(http://gruvr.com/feed/\\?geo=.+?)\"");

	/*
	 * 
	 * <span class="checkout_event_datetime">
	 * Sunday, March 11, 2012 at 8:00 PM
	 * </span>
	 */
	private static final Pattern buyPagePattern = Pattern
			.compile("<span class=\"checkout_event_datetime\">\\s+?(.+?)\\s+?</span>");

	/*
	 * 
	 * javascript:tnet(1794451,1250882453,1,127.00,94764 ,7082683,127,'' );
	 */
	private static final Pattern ticketsPagePattern = Pattern
			.compile("javascript:tnet\\((.+?),(.+?),(.+?),(.+?),(.+?),(.+?),(.+?),");

	/*
	 * <h3>Genres</h3>
	 * <p><a
	 * href="/explore/genre/poprock-d20"><strong>Pop/Rock</strong></a></p>
	 */
	private static final Pattern genresPattern = Pattern
			.compile("explore/genre/.+?<strong>(.+?)</strong>");

	/*
	 * 
	 * <h3>Styles</h3>
	 * <ul class="left-sidebar-list">
	 * <li><a href="/explore/style/heavy-metal-d655">Heavy Metal</a></li>
	 * <li><a href="/explore/style/alternative-metal-d2697">Alternative
	 * Metal</a></li>
	 * <li><a href="/explore/style/post-grunge-d2771">Post-Grunge</a></li>
	 * <li><a href="/explore/style/rap-metal-d2931">Rap-Metal</a></li>
	 * </ul>
	 */
	private static final Pattern stylesPattern = Pattern
			.compile("<li><a href=\"/explore/style/.+?\">(.+?)</a></li>");

	private static final LinkedHashMap<String, Set<String>> statesToProcess = Maps
			.newLinkedHashMap();

	private static List<String> statesList = Lists.newArrayList("CA", "NY", "IL", "AZ", "OR", "VA",
			"GA", "FL", "NJ", "AL", "AK", "AR", "CO", "CT", "DE", "HI", "ID", "IN", "IA", "KS",
			"KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NM",
			"NC", "ND", "OH", "OK", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT", "WA", "WV",
			"WI", "WY");

	private static final Set<String> statesSet = Sets.newHashSet(statesList);

	static {
		// prioritize the states to process
		// but add them in reverse order so that the higher
		// priority states will appear as 'newer'
		statesList = Lists.reverse(statesList);
		for (String state : statesList) {
			statesToProcess.put(state, new HashSet<String>());
		}
	}

	private class CityEventSource extends RSSEventSource {

		public CityEventSource(String cityFeedUrl) {
			super(web, cityFeedUrl);
		}

		@Override
		public boolean requireLocation() {
			return GruvrLiveMusicEventSource.this.requireLocation();
		}

		@Override
		public String getName() {
			return GruvrLiveMusicEventSource.this.getName();
		}

		@Override
		public String finalizeUrl(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeUrl(baseEvent);
		}

		@Override
		public LinkedHashSet<String> finalizeTags(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeTags(baseEvent);
		}

		@Override
		public String finalizeName(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeName(baseEvent);
		}

		@Override
		public ArrayNode finalizeMedia(BaseEvent baseEvent, Collection<String> tags) {
			return GruvrLiveMusicEventSource.this.finalizeMedia(baseEvent, tags);
		}

		@Override
		public ObjectNode finalizeLocation(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeLocation(baseEvent);
		}

		@Override
		public String finalizeDescription(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeDescription(baseEvent);
		}

		@Override
		public Calendar finalizeDate(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeDate(baseEvent);
		}

		@Override
		public String finalizeCategory(BaseEvent baseEvent) {
			return GruvrLiveMusicEventSource.this.finalizeCategory(baseEvent);
		}
	}

	private final LinkedHashSet<String> baseTags = new LinkedHashSet<String>();
	private final WebFetcher web;
	private final MediaProvider mediaProvider;
	private final String name;
	private final String gruvrBaseUrl;
	private Iterator<String> cityFeedUrls;
	private Iterator<BaseEvent> currentCityFeed;
	private int currentCityEventCount;
	BaseEvent eventToReturnNext;
	private final Set<String> eventGUIDsAlreadyProcessed = Sets.newHashSet();

	public GruvrLiveMusicEventSource(WebFetcher web, MediaProvider mediaProvider,
			String gruvrBaseUrl, String... tags) {
		this.web = web;
		this.mediaProvider = mediaProvider;
		this.gruvrBaseUrl = gruvrBaseUrl;
		this.name = "Gruvr Main Live Event Source - " + gruvrBaseUrl;
		for (String tag : tags) {
			baseTags.add(tag.toLowerCase().trim());
		}
	}

	@Override
	public Iterator<BaseEvent> extractEvents() {
		try {
			// Set<String> feedUrls = Sets.newHashSet();
			String mainWebPageContent = web.fetchContent(gruvrBaseUrl);
			// need to extract all links that point to regional areas,
			// for example: href="http://gruvr.com/feed/?geo=zurich"
			Matcher matcher = cityFeedPattern.matcher(mainWebPageContent);
			while (matcher.find()) {
				String feedUrl = matcher.group(1);
				String state = getUSAState(feedUrl);
				if (state != null) {
					System.out.println(feedUrl);

					Set<String> feedUrlsForState = statesToProcess.get(state);
					if (feedUrlsForState != null) {
						feedUrlsForState.add(feedUrl);
					}
				}
			}
			// now build one comprehensive feed list
			List<String> cityFeedUrlList = Lists.newLinkedList();
			for (String state : statesToProcess.keySet()) {
				Set<String> cityFeedUrlSet = statesToProcess.get(state);
				cityFeedUrlList.addAll(cityFeedUrlSet);
				cityFeedUrlSet.clear(); // just to free up some memory
			}
			cityFeedUrls = cityFeedUrlList.iterator();
			System.out.println("done finding " + cityFeedUrlList.size() + " US feeds");
			return this;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	private String getUSAState(String feedUrl) {
		String city = feedUrl.substring(feedUrl.indexOf('=') + 1);
		String[] possibleCityState = normalizeCityName(city);
		if (possibleCityState == null) {
			return null;
		}

		String state = possibleCityState[1].toUpperCase();

		if (state.length() != 2) {
			return null;
		}

		if (statesSet.contains(state)) {
			return state;
		}
		return null;
	}

	private String[] normalizeCityName(String city) {
		int lastDash = city.lastIndexOf('-');
		if (lastDash < 0) {
			return null;
		}

		String[] cityParts = city.split("-");
		city = "";
		for (int i = 0; i < cityParts.length - 1; i++) {
			city += cityParts[i];
			if (i < cityParts.length - 2) {
				city += " ";
			}
		}
		String[] possibleCityState = new String[2];
		possibleCityState[0] = city;
		possibleCityState[1] = cityParts[cityParts.length - 1];
		return possibleCityState;
	}

	@Override
	public String finalizeName(BaseEvent baseEvent) {
		String title = RSSEventSource.getCData("title", baseEvent);
		if (title == null) {
			return null;
		}
		String[] titleParts = title.split(":");
		if ((titleParts == null) || (titleParts.length < 2)) {
			return null;
		}
		title = titleParts[1];
		titleParts = title.split(" at ");
		if ((titleParts == null) || (titleParts.length != 2)) {
			return null;
		}
		return titleParts[0].trim();
	}

	@Override
	public String finalizeDescription(BaseEvent baseEvent) {
		return null;
	}

	@Override
	public ObjectNode finalizeLocation(BaseEvent baseEvent) {
		String title = RSSEventSource.getCData("title", baseEvent);
		if (title == null) {
			return null;
		}
		String[] titleParts = title.split(":");
		if ((titleParts == null) || (titleParts.length < 2)) {
			return null;
		}
		title = titleParts[1];
		titleParts = title.split(" at ");
		if ((titleParts == null) || (titleParts.length != 2)) {
			return null;
		}
		String locationName = titleParts[1].trim();
		if (locationName.length() == 0) {
			return null;
		}
		ObjectNode location = mapper.createObjectNode();
		location.put("address", locationName);
		String georssPoint = RSSEventSource.getElementText("georss:point", baseEvent);
		if (georssPoint != null) {
			String[] coords = georssPoint.split(" ");
			if (coords.length == 2) {
				String lat = coords[0].trim();
				String lng = coords[1].trim();
				if ((lat != null) && (lng != null)) {
					location.put("center", lat + "," + lng);
					location.put("marker", lat + "," + lng);
				}
			}
		}
		return location;
	}

	@Override
	public String finalizeUrl(BaseEvent baseEvent) {
		return null;
	}

	@Override
	public ArrayNode finalizeMedia(BaseEvent baseEvent, Collection<String> tags) {
		final ArrayNode mediaNode = mapper.createArrayNode();

		String name = finalizeName(baseEvent);
		if (name == null) {
			return mediaNode;
		}
		String search = name + " live";
		mediaProvider.fetchImages(search, mediaNode, 2);
		mediaProvider.fetchVideos(search, mediaNode, 4);
		return mediaNode;
	}

	@Override
	public String finalizeCategory(BaseEvent baseEvent) {
		LinkedHashSet<String> tags = finalizeTags(baseEvent);
		if (tags.contains("music")) {
			return "live_performances.music_concerts";
		}
		for (String tag : tags) {
			if (tag.contains("comedy")) {
				return "live_performances.comedy_shows";
			}
		}
		return "live_performances.music_concerts";
	}

	@Override
	public LinkedHashSet<String> finalizeTags(BaseEvent baseEvent) {
		LinkedHashSet<String> tags = new LinkedHashSet<String>();
		tags.addAll(baseTags);

		/*
		 * Extract genre from allmusic.com hml, such as:
		 * <h3>Genres</h3>
		 * <p><a
		 * href="/explore/genre/poprock-d20"><strong>Pop/Rock</strong></a></p>
		 */
		String name = finalizeName(baseEvent);
		if (name == null) {
			return null;
		}
		try {
			name = URLEncoder.encode(name.toLowerCase(), "UTF-8");
			String artistContent = web
					.fetchContent("http://www.allmusic.com/search/artist/" + name);
			Matcher matcher = genresPattern.matcher(artistContent);
			int max = 2;
			int total = 0;
			int count = 0;
			while (matcher.find() && (count++ < max)) {
				String genres = matcher.group(1).trim().toLowerCase();
				tags.add(genres);
				total++;

			}
			matcher = stylesPattern.matcher(artistContent);
			count = 0;
			while (matcher.find() && (count++ < max)) {
				String styles = matcher.group(1).trim().toLowerCase();
				tags.add(styles);
				total++;
			}
			if ((total > 0)) {
				boolean foundNonMusic = false;
				for (String tag : tags) {
					if (tag.contains("comedy") || tag.contains("spoken")) {
						foundNonMusic = true;
						break;
					}
				}
				if (!foundNonMusic) {
					tags.add("music");
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return tags;
	}

	@Override
	public Calendar finalizeDate(BaseEvent baseEvent) {
		String description = RSSEventSource.getCData("description", baseEvent);
		if (description == null) {
			return null;
		}
		String[] descriptionParts = description.split(" on ");
		if ((descriptionParts == null) || (descriptionParts.length < 2)) {
			return null;
		}
		String dateStr = descriptionParts[descriptionParts.length - 1];
		Date date;
		try {
			date = shortDateParser.parse(dateStr);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			Calendar refinedCal = tryExtractFinerTime(baseEvent);
			if (refinedCal != null) {
				return refinedCal;
			}
			return cal;
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Calendar tryExtractFinerTime(BaseEvent baseEvent) {
		String link = RSSEventSource.getElementText("link", baseEvent);
		if (link == null) {
			return null;
		}
		if (!link.startsWith("http://tickets.gruvr.com")) {
			return null;
		}

		try {
			String ticketsPageContent = web.fetchContent(link);
			if (ticketsPageContent != null) {
				Matcher matcher = ticketsPagePattern.matcher(ticketsPageContent);
				if (matcher.find()) {
					String field1 = matcher.group(1).trim();
					String field2 = matcher.group(2).trim();
					String field3 = matcher.group(3).trim();
					String field4 = matcher.group(4).trim();
					String field5 = matcher.group(5).trim();
					matcher.group(6);
					matcher.group(7);
					String buyPageUrl = "https://tickettransaction2.com/Checkout.aspx?"
							+ "brokerid=2977" + "&sitenumber=0" + "&tgid=" + field2 + "&evtid="
							+ field1 + "&price=" + field4 + "&treq=" + field3 + "&SessionId="
							+ field5;

					String buyPage = web.fetchContent(buyPageUrl);
					if (buyPage != null) {

						matcher = buyPagePattern.matcher(buyPage);
						if (matcher.find()) {
							String actualDateTimeStr = matcher.group(1).trim();
							if (!actualDateTimeStr.contains("at TBA")) {
								Date actualDateTime = longDateParser.parse(actualDateTimeStr);
								Calendar cal = Calendar.getInstance();
								cal.setTime(actualDateTime);
								return cal;
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean requireLocation() {
		return true;
	}

	@Override
	public boolean hasNext() {
		if (eventToReturnNext != null) {
			return true;
		}
		if (cityFeedUrls == null) {
			return false;
		}
		if ((currentCityFeed == null) && !cityFeedUrls.hasNext()) {
			return false;
		}
		if ((currentCityFeed != null) && currentCityFeed.hasNext()) {
			eventToReturnNext = currentCityFeed.next();
			return true;
		}
		if (!cityFeedUrls.hasNext()) {
			return false;
		}
		while (true) {
			if (!cityFeedUrls.hasNext()) {
				return false;
			}
			String cityFeedUrl = cityFeedUrls.next();
			System.out.println("Done with previous, found " + currentCityEventCount);
			System.out.println("GRUVR starting city feed " + cityFeedUrl);
			RSSEventSource cityFeed = new CityEventSource(cityFeedUrl);
			try {
				currentCityFeed = cityFeed.extractEvents();
				currentCityEventCount = 0;
			}
			catch (Exception e) {
				e.printStackTrace();
				currentCityFeed = null;
			}
			if (currentCityFeed == null) {
				continue;
			}
			if (!currentCityFeed.hasNext()) {
				continue;
			}
			eventToReturnNext = null;
			while ((eventToReturnNext == null) && currentCityFeed.hasNext()) {
				BaseEvent nextEvent = currentCityFeed.next();
				if (nextEvent == null) {
					continue;
				}
				String guid = RSSEventSource.getElementText("guid", nextEvent);
				if (guid != null) {
					if (eventGUIDsAlreadyProcessed.contains(guid)) {
						System.out.print('.');
						continue;
					}
					eventGUIDsAlreadyProcessed.add(guid);
					eventToReturnNext = nextEvent;
					return true;
				}
			}
			return hasNext();
		}
	}

	@Override
	public BaseEvent next() {
		if (!hasNext()) {
			return null;
		}
		currentCityEventCount++;
		BaseEvent ret = eventToReturnNext;
		eventToReturnNext = null;
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove is not supported");
	}
}

