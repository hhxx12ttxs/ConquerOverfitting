package com.weanticipate.harvester;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDateTime;

import com.weanticipate.resources.geo.GeonamesAPI;
import com.weanticipate.resources.media.MediaProvider;
import com.weanticipate.resources.web.WebFetcher;

public class SportsCalendarEventSource extends CSVEventSource implements EventSource {

	private static final ObjectMapper mapper = new ObjectMapper();
	private final LinkedHashSet<String> baseTags = new LinkedHashSet<String>();
	private final MediaProvider mediaProvider;
	private final String category;
	private final GeonamesAPI geo;

	public SportsCalendarEventSource(WebFetcher web, MediaProvider mediaProvider, GeonamesAPI geo,
			String icalUrl, String category, String leagueTag, String sportTag, String... otherTags) {
		super(web, icalUrl);
		this.geo = geo;
		this.mediaProvider = mediaProvider;
		this.category = category;
		baseTags.add(leagueTag);
		baseTags.add(sportTag);
		for (String calTag : otherTags) {
			baseTags.add(calTag);
		}
	}

	@Override
	public LinkedHashSet<String> finalizeTags(BaseEvent baseEvent) {
		LinkedHashSet<String> eventTags = new LinkedHashSet<String>();
		eventTags.addAll(baseTags);

		String[] teams = baseEvent.getName().split("@");
		if (teams.length == 2) {
			String team1 = teams[0];
			String team2 = teams[1];
			if (team1.contains(":")) {
				team1 = team1.substring(team1.indexOf(':') + 1);
			}
			team1 = team1.trim().toLowerCase();
			team2 = team2.trim().toLowerCase();
			eventTags.add(team1);
			eventTags.add(team2);
		}

		if (baseEvent.getLocation() != null) {
			String[] locationParts = baseEvent.getLocation().get("address").getTextValue()
					.split("@");
			if (locationParts.length == 2) {
				String stadium = locationParts[0].trim();
				if (stadium.length() > 0) {
					eventTags.add(stadium);
				}
			}
		}
		return eventTags;
	}

	@Override
	public String getName() {
		return "Sports" + baseTags;
	}

	@Override
	public String finalizeName(BaseEvent baseEvent) {
		String name = baseEvent.getName();
		int index = name.indexOf(':');
		if (index > 0) {
			name = name.substring(index + 1).trim();
		}

		if (name.contains("[TV:") && name.trim().endsWith("]")) {
			name = name.substring(0, name.indexOf("[TV:")).trim();
		}
		return name;
	}

	@Override
	public String finalizeDescription(BaseEvent baseEvent) {
		return null;
	}

	@Override
	public ObjectNode finalizeLocation(BaseEvent baseEvent) {
		return baseEvent.getLocation();
	}

	@Override
	public String finalizeUrl(BaseEvent baseEvent) {
		return null;
	}

	@Override
	public ArrayNode finalizeMedia(BaseEvent baseEvent, Collection<String> tags) {
		LinkedHashSet<String> actualTags = finalizeTags(baseEvent);
		if (actualTags.size() == baseTags.size() + 2) {
			// this way we know the last two tags are the team names
			String[] eventTags = actualTags.toArray(new String[actualTags.size()]);
			String leagueTag = eventTags[0];
			String team1 = eventTags[actualTags.size() - 2];
			String team2 = eventTags[actualTags.size() - 1];
			ArrayNode media = mapper.createArrayNode();
			mediaProvider.fetchImages(leagueTag + " " + team1, media, 3);
			mediaProvider.fetchImages(leagueTag + " " + team2, media, 3);
			return media;
		}
		else {
			String[] eventTags = actualTags.toArray(new String[actualTags.size()]);
			String leagueTag = eventTags[0];

			ArrayNode media = mapper.createArrayNode();
			String location = baseEvent.getLocation().get("address").getTextValue();
			mediaProvider.fetchImages(leagueTag + " " + location, media, 3);
			return media;
		}

	}

	@Override
	public Calendar finalizeDate(BaseEvent baseEvent) {
		Calendar time = baseEvent.getEventTime();
		time.getTimeZone();
		if (time.before(Calendar.getInstance())) {
			return time;
		}
		if (time.get(Calendar.HOUR_OF_DAY) != 0) {
			ObjectNode location = finalizeLocation(baseEvent);
			String address = location.get("address").getTextValue();
			if (address.contains("-")) {
				address = address.substring(address.indexOf("-") + 1).trim();
			}
			try {
				DateTimeZone locationZone = geo.getTimezoneForLocation(address);
				if (locationZone != null) {
					// System.out.println("timezone for " + address + " is " +
					// locationZone.getID());
					// System.out.println("old time': " + time.getTime());
					Instant instant = new Instant(time.getTimeInMillis());
					LocalDateTime localDateTime = new LocalDateTime(instant,
							DateTimeZone.getDefault());
					// System.out.println("old time: " + localDateTime);

					DateTime srcDateTime = localDateTime.toDateTime(DateTimeZone.getDefault());
					// System.out.println("src time: " + srcDateTime);
					DateTime dstDateTime = srcDateTime.withZone(locationZone);
					// System.out.println("dst time: " + dstDateTime);
					LocalDateTime resultLocalDateTime = dstDateTime.toLocalDateTime();
					// System.out.println("res time: " + resultLocalDateTime);
					DateTime resultDateTime = resultLocalDateTime.toDateTime();
					// System.out.println("res time: " + resultDateTime);

					Calendar newTime = resultDateTime.toGregorianCalendar();
					// newTime.setTimeZone(locationZone.toTimeZone());
					// System.out.println("new datetime: " + newTime.getTime());
					return newTime;

				}
			}
			catch (Exception e) {
				return null;
			}
		}
		return baseEvent.getEventTime();
	}

	@Override
	public String finalizeCategory(BaseEvent baseEvent) {
		return category;
	}

	@Override
	public boolean requireLocation() {
		return true;
	}
}

