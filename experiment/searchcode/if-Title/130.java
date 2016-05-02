package com.weanticipate.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import twitter4j.auth.AccessToken;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.weanticipate.exceptions.InvalidFieldValueException;
import com.weanticipate.exceptions.MissingFieldException;
import com.weanticipate.exceptions.UnknownLocationException;
import com.weanticipate.resources.geo.GeonamesAPI;
import com.weanticipate.resources.mongodb.MongoDB;
import com.weanticipate.resources.mongodb.MongoDBCollections;
import com.weanticipate.resources.mongodb.MongoDBCursor;
import com.weanticipate.utils.DateUtils;
import com.weanticipate.utils.JsonUtils;

@Singleton
@ThreadSafe
public final class EventService {

	private static ImmutableSet<String> neutralWords = new ImmutableSet.Builder<String>().add("a")
			.add("an").add("and").add("be").add("is").add("of").add("the").add("to").build();

	private static enum Visibility {
		PUBLIC, PRIVATE
	}

	private static final int MAX_RESULT_SET_TAGS = 500;
	private static final int MAX_RESULT_SET = 25; // if this is going to change,
													// be certain to change
													// web_ui Listings.java also
	private static final long NEAREST_TRIGGER_THRESHOLD = 60000 * 10;

	private static final long FURTHEST_TRIGGER_THRESHOLD = 1000l * 60l * 60l * 24l * 366l * 2l;
	public static final String _ID = "_id";
	public static final String EVENT_ID = "event-id";
	public static final String ID_PREFIX = "e_";
	public static final String OWNER_ID = "owner-id";
	public static final String OWNER_USERNAME = "owner-username";
	public static final String SUMMARY = "summary";
	public static final String DESCRIPTION = "description";
	public static final String URL = "url";
	public static final String TIMEZONE = "timezone";
	public static final String CREATED_TS = "created-ts";
	public static final String VISIBILITY = "visibility";
	public static final String CANCELED = "canceled";
	public static final String TAGS = "tags";
	public static final String CATEGORY = "category";
	public static final String MEDIA = "media";
	public static final String TRIGGER_TIME = "trigger-time";
	public static final String TIME_REMAINING_IN_MINUTES = "time-remaining-in-mins";
	public static final String CALCULATED_TRIGGER_TIME = "calculated-trigger-time";
	public static final String DATETIME_LABELS = "datetime-labels";
	public static final String OWNER_TZ = "owner-tz";
	public static final String RELATIVE_TZ = "relative-tz";
	public static final String FOLLOWERS = "followers";
	public static final String COMMENT_EMAIL_SUBSCRIBERS = "comment-email-subscribers";
	public static final String DETAIL_CHANGE_SUBSCRIBERS = "detail-change-subscribers";
	public static final String POST_TWEET = "post-tweet";
	public static final String TRIGGER_ID_PREFIX = "t_";
	public static final String SLICING_ID = "slicing-id";
	public static final String FOLLOWER_ID = "follower-id";
	public static final String FOLLOWER_USERNAME = "follower-username";
	public static final String IS_EVENT_FOLLOWER = "is-following-event";
	public static final String EMAIL_ADDRESS = "address";
	public static final String LOCATION = "location";
	public static final String LOCATION_ADDRESS = "address";
	public static final String LOCATION_CENTERPOINT = "center";
	public static final String LOCATION_MARKERPOINT = "marker";
	public static final String LOCATION_MARKERPOINT_LNGLAT_COORDS = "marker-coordinates";
	public static final String EVENT_LOCATION_COORDINATES = "location.marker-coordinates";
	public static final String LOCATION_ZOOM = "zoom-level";
	public static final String LOCATION_DESCRIPTION = "description";
	public static final String TWITTER_ID = "twitter-id";
	public static final String TWITTER_ACCESS_TOKEN = "access-token";
	public static final String TWITTER_ACCESS_TOKEN_SECRET = "access-token-secret";
	public static final String USERNAME = "username";
	public static final String USER_ID = "user-id";

	public static final String FACEBOOK_INVITE_REQUEST = "_id";
	public static final String FACEBOOK_INVITE_REQUEST_EVENT = "event-id";
	public static final String FACEBOOK_INVITE_REQUEST_USERS = "user-ids";

	public static final String FOLLOWER_COUNT = "follower-count";
	public final static DBObject DONOTINCLUDE_FOLLOWERS = new BasicDBObject(FOLLOWERS,
			MongoDB.DO_NOT_INCLUDE).append(COMMENT_EMAIL_SUBSCRIBERS, MongoDB.DO_NOT_INCLUDE)
			.append(DETAIL_CHANGE_SUBSCRIBERS, MongoDB.DO_NOT_INCLUDE);
	public static final DBObject SORTBY_MOSTANTICIPATED = new BasicDBObject(FOLLOWER_COUNT,
			MongoDB.DESC);
	public static final DBObject SORTBY_NEWEST = new BasicDBObject(CREATED_TS, MongoDB.DESC);

	public static final DBObject SORTBY_SOONEST = new BasicDBObject(CALCULATED_TRIGGER_TIME,
			MongoDB.ASC);

	private static List<DBObject> eventCategories;

	static {
		eventCategories = Lists.newLinkedList();

		// live performances
		BasicDBObject category = createCategory("live_performances", "Live Performances", true,
				eventCategories);
		List<DBObject> subCategories = Lists.newLinkedList();
		category.append("sub-categories", subCategories);
		createCategory("live_performances.music_concerts", "Music Concerts", true, subCategories);
		createCategory("live_performances.comedy_shows", "Comedy Shows", true, subCategories);
		createCategory("live_performances.cultural_events", "Cultural Events", true, subCategories);
		createCategory("live_performances.other", "Other Performances", true, subCategories);

		// conferences
		category = createCategory("conventions", "Conferences and Conventions", true,
				eventCategories);
		subCategories = Lists.newLinkedList();
		category.append("sub-categories", subCategories);
		createCategory("conventions.comicbook", "Comic Book Conventions", true, subCategories);
		createCategory("conventions.scifi", "SciFi Conventions", true, subCategories);
		createCategory("conventions.anime", "Anime Conventions", true, subCategories);
		createCategory("conventions.gaming", "Gaming Conventions", true, subCategories);
		createCategory("conventions.signings", "Signings or Appearances", true, subCategories);
		createCategory("conventions.business", "Business Conferences", true, subCategories);
		createCategory("conventions.tech", "Technology Conferences", true, subCategories);
		createCategory("conventions.tech", "Collectibles Conventions", true, subCategories);
		createCategory("conventions.other", "Other Events", true, subCategories);

		// sports
		category = createCategory("sports", "Sports", true, eventCategories);
		subCategories = Lists.newLinkedList();
		category.append("sub-categories", subCategories);
		createCategory("sports.nba", "NBA Games", true, subCategories);
		createCategory("sports.mlb", "MLB Games", true, subCategories);
		createCategory("sports.pga", "PGA Tournaments", true, subCategories);
		// createCategory("sports.nfl", "NFL Games", true, subCategories);
		// createCategory("sports.ncaa_basketball", "NCAA Basketball Games",
		// true,
		// subCategories);
		// createCategory("sports.ncaa_football", "NCAA Football Games", true,
		// subCategories);
		createCategory("sports.nascar", "NASCAR Races", true, subCategories);
		createCategory("sports.indy", "Indy Races", true, subCategories);
		createCategory("sports.other", "Other Sport Events", true, subCategories);

		// reserved lists
		createCategory("upcoming_movies", "Upcoming Movies", false, eventCategories);
		createCategory("product_launches", "Product Lauches", false, eventCategories);
		createCategory("upcoming_videogames", "Upcoming Video Games", false, eventCategories);

		// other unreserved lists
		createCategory("astronomy", "Astronomical Events", true, eventCategories);
		createCategory("rumored_events", "Speculated or Rumored Events", true, eventCategories);
		createCategory("personal_events", "Personal Events", true, eventCategories, true);
		createCategory("misc", "Everything Else", true, eventCategories);
	}

	private static BasicDBObject createCategory(String id, String label, boolean canAdd,
			List<DBObject> parent, boolean isDefault) {
		BasicDBObject category = new BasicDBObject("_id", id);
		category.put("can-add", canAdd);
		category.put("label", label);
		if (isDefault) {
			category.put("default", isDefault);
		}
		parent.add(category);
		return category;
	}

	private static BasicDBObject createCategory(String id, String label, boolean canAdd,
			List<DBObject> parent) {
		return createCategory(id, label, canAdd, parent, false);
	}

	private static <K extends Comparable<? super K>, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {
		SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
				new Comparator<Map.Entry<K, V>>() {
					@Override
					public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
						K key1 = e1.getKey();
						K key2 = e2.getKey();
						int valueCompare = e2.getValue().compareTo(e1.getValue());
						if (valueCompare == 0) {
							return key1.compareTo(key2);
						}
						else {
							return valueCompare;
						}
					}
				});
		sortedEntries.addAll(map.entrySet());
		return sortedEntries;
	}

	private final MongoDB mongo;
	private final GeonamesAPI geoService;

	@Inject
	public EventService(MongoDB mongo, GeonamesAPI geoService) {
		this.mongo = mongo;
		this.geoService = geoService;
		ensureIndexes();
	}

	private void ensureIndexes() {
		mongo.ensureIndex(MongoDBCollections.EVENTS, OWNER_ID);
		mongo.ensureIndex(MongoDBCollections.EVENTS, TAGS);
		mongo.ensureIndex(MongoDBCollections.EVENTS, CATEGORY);
		mongo.ensureIndex(MongoDBCollections.EVENTS, CALCULATED_TRIGGER_TIME);
		mongo.ensureIndex(MongoDBCollections.EVENTS, FOLLOWER_COUNT);
		mongo.ensure2DIndex(MongoDBCollections.EVENTS, EVENT_LOCATION_COORDINATES);

		mongo.ensureIndex(MongoDBCollections.FACEBOOK_INVITE_REQUESTS, FACEBOOK_INVITE_REQUEST);
	}

	public List<DBObject> getCategories() {
		return eventCategories;
	}

	/**
	 * This service method assumes that the received DBObject has been
	 * authorized to be created for the provided username
	 * 
	 * @param twitter
	 * @param email
	 * @param eventBson
	 *            the binary json object representing the parameters of the new
	 *            event
	 * @return the same event object, but populated with an ID
	 * @throws InvalidFieldValueException
	 * @throws MissingFieldException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public BasicDBObject createEvent(BasicDBObject newEventSubmission, String ownerTimezone,
			String userId, String username, String email, AccessToken twitterAccessToken,
			BasicDBObject userNotificationSettings) throws MissingFieldException,
			InvalidFieldValueException, IOException {
		if (newEventSubmission == null) {
			return null;
		}

		// create the new event object
		String eventId = JsonUtils.generateUniqueId(ID_PREFIX);
		BasicDBObject newEvent = new BasicDBObject();
		newEvent.put(OWNER_ID, userId);
		newEvent.put(OWNER_USERNAME, username);
		newEvent.put(_ID, eventId);
		newEvent.put(CREATED_TS, DateUtils.nowString());
		newEvent.put(CANCELED, false);

		// determine the timezone in which to create the event
		String timezone = null;
		Object providedLocationObject = newEventSubmission.get(LOCATION);
		BasicDBObject providedLocationJson = null;
		if (providedLocationObject != null) {
			providedLocationJson = new BasicDBObject((Map) providedLocationObject);
		}
		if (providedLocationJson != null) {
			String locationName = providedLocationJson.getString(LOCATION_ADDRESS);
			BasicDBObject locationJson = new BasicDBObject(LOCATION_ADDRESS, locationName);
			String locationDescription = providedLocationJson.getString(LOCATION_DESCRIPTION);
			if (locationDescription != null) {
				locationJson.append(LOCATION_DESCRIPTION, locationDescription);
			}
			String centerPoint = providedLocationJson.getString(LOCATION_CENTERPOINT);
			if (centerPoint != null) {
				locationJson.append(LOCATION_CENTERPOINT, centerPoint);
			}
			String markerPoint = providedLocationJson.getString(LOCATION_MARKERPOINT);
			if (markerPoint != null) {
				locationJson.append(LOCATION_MARKERPOINT, markerPoint);
				String[] coords = markerPoint.split("\\,");
				double lat = Double.parseDouble(coords[0]);
				double lng = Double.parseDouble(coords[1]);
				List<Double> coordsToStore = new ArrayList<Double>();

				// do NOT reverse these, Mongo wants x,y, which is lng/lat
				coordsToStore.add(lng);
				coordsToStore.add(lat);

				locationJson.append(LOCATION_MARKERPOINT_LNGLAT_COORDS, coordsToStore);
				try {
					System.out.println("latLong for event: " + markerPoint);
					DateTimeZone locationTimezone = geoService.getTimezoneForLatLng(lat, lng);
					if (locationTimezone != null) {
						timezone = locationTimezone.getID();
					}

				}
				catch (UnknownLocationException e) {
					timezone = null;
				}
			}
			Integer zoomLevel = (Integer) providedLocationJson.get(LOCATION_ZOOM);
			if (zoomLevel != null) {
				locationJson.append(LOCATION_ZOOM, zoomLevel);
			}
			newEvent.put(LOCATION, locationJson);
			if (timezone == null) {
				try {
					DateTimeZone locationTimezone = geoService.getTimezoneForLocation(locationName);
					if (locationTimezone != null) {
						timezone = locationTimezone.getID();
					}
				}
				catch (UnknownLocationException e) {
					timezone = null;
				}
			}
		}
		if (timezone == null) {
			String timezoneNameProvided = newEventSubmission.getString(TIMEZONE);
			if (timezoneNameProvided != null) {
				DateTimeZone timezoneProvided = DateTimeZone.forID(timezoneNameProvided);
				if (timezoneProvided != null) {
					timezone = timezoneProvided.getID();
				}
				else {
					System.out.println("EventService.createEvent() timezone was provided, "
							+ "but doesn't resolve to a timezoneID: " + timezoneNameProvided);
				}
			}
		}
		if (timezone == null) {
			timezone = ownerTimezone;
		}
		newEvent.put(TIMEZONE, timezone);

		JsonUtils.copyRequiredStringField(newEventSubmission, newEvent, SUMMARY, 140);
		JsonUtils.copyRequiredStringField(newEventSubmission, newEvent, CATEGORY, 140);
		copyVisibility(newEventSubmission, newEvent);
		copyTriggerTime(newEventSubmission, newEvent);

		// persist the event
		mongo.insert(MongoDBCollections.EVENTS, newEvent);

		return newEvent;
	}

	public void addEventFollower(String eventId, String followerId, String followerUsername,
			String followerEmail, BasicDBObject userNotificationSettings) throws IOException {
		boolean subscribeToCommentEmail = userNotificationSettings == null ? true
				: userNotificationSettings.getBoolean(
						UserService.SETTING_FOLLOWEDEVENTS_NEWCOMMENT_FLAG, true);
		boolean subscribeToEventDetailChanges = userNotificationSettings == null ? true
				: userNotificationSettings.getBoolean(
						UserService.SETTING_FOLLOWEDEVENTS_MODIFIED_FLAG, true);
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		BasicDBObject updates = new BasicDBObject();
		// this code assumes the user has already been verified to not follow
		// the event already
		BasicDBObject addToSetFields = new BasicDBObject(FOLLOWERS, new BasicDBObject(USER_ID,
				followerId).append(USERNAME, followerUsername));
		updates.put(MongoDB.CMD_ADD_TO_SET, addToSetFields);
		updates.put(MongoDB.CMD_INCREMENT, new BasicDBObject(FOLLOWER_COUNT, 1));
		if (subscribeToCommentEmail) {
			addToSetFields.put(COMMENT_EMAIL_SUBSCRIBERS, followerEmail);
		}
		if (subscribeToEventDetailChanges) {
			addToSetFields.put(DETAIL_CHANGE_SUBSCRIBERS, followerEmail);
		}
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, updates);
	}

	public void addEventMediaItem(String eventId, String eventOwnerEmail,
			BasicDBObject newMediaItemJson, String username) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}

		// TODO need to validate media items being added
		// TODO should copy the known fields into a new object instead, so that
		// no trash can pollute the db
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_PUSH, new BasicDBObject(MEDIA, newMediaItemJson));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void changeEventDescription(String eventId, String newDescription) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		// TODO need to validate, similar to the createEvent method
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_SET, new BasicDBObject(DESCRIPTION, newDescription));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void changeEventCategory(String eventId, String newCategory) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		// TODO need to validate, similar to the createEvent method
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_SET, new BasicDBObject(CATEGORY, newCategory));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void changeEventLocation(String eventId, BasicDBObject locationJson) throws IOException,
			InvalidFieldValueException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		// TODO need to validate location entry
		BasicDBObject update = new BasicDBObject();
		if (!locationJson.containsField(LOCATION)) {
			throw new InvalidFieldValueException(LOCATION, "a location field is required");
		}
		// TODO should copy the known fields into a new object instead, so that
		// no trash can pollute the db
		update.put(MongoDB.CMD_SET, locationJson);
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void changeEventTags(String eventId, List<String> tags) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		if (tags == null) {
			tags = new ArrayList<String>();
		}
		List<String> lowercasedTags = new ArrayList<String>();
		for (String tag : tags) {
			lowercasedTags.add(tag.toLowerCase().trim());
		}
		// TODO need to validate, similar to the createEvent method
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_SET, new BasicDBObject(TAGS, lowercasedTags));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void changeEventTitle(String eventId, String newTitle) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		int followers = event.containsField(FOLLOWER_COUNT) ? event.getInt(FOLLOWER_COUNT) : 0;
		if (followers > 0) {
			throw new IllegalStateException("cannot change the title with active followers");
		}
		// TODO need to validate, similar to the createEvent method
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_SET, new BasicDBObject(SUMMARY, newTitle));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void changeEventUrl(String eventId, String newUrl) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		// TODO need to validate, similar to the createEvent method
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_SET, new BasicDBObject(URL, newUrl));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public void deleteEvent(String eventId) {
		mongo.deleteOne(MongoDBCollections.EVENTS, eventId);
	}

	public void deleteEventLocation(String eventId) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		// TODO need to validate location entry
		BasicDBObject update = new BasicDBObject();
		update.put(MongoDB.CMD_UNSET, new BasicDBObject(LOCATION, MongoDB.INCLUDE));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, update);
	}

	public String extractEventIdFromEvent(DBObject event) {
		return event.get(_ID).toString();
	}

	public String extractEventOwnerIdFromEvent(DBObject event) {
		return event.get(OWNER_ID).toString();
	}

	public String extractEventOwnerIdFromTriggerObject(DBObject eventToTrigger) {
		return eventToTrigger.get(OWNER_ID).toString();
	}

	public String extractEventOwnerUsernameFromEvent(DBObject event) {
		return event.get(OWNER_USERNAME).toString();
	}

	public String extractEventSummaryFromEvent(DBObject event) {
		return event.get(SUMMARY).toString();
	}

	public String extractEventSummaryFromTriggerObject(DBObject eventToTrigger) {
		return eventToTrigger.get(SUMMARY).toString();
	}

	public BasicDBObject fetchEvent(String id, DateTimeZone relativeTimezone,
			boolean isFollowingEvent) throws IOException {
		DBObject event = mongo.findOne(MongoDBCollections.EVENTS, id, DONOTINCLUDE_FOLLOWERS);
		if (event == null) {
			return null;
		}
		BasicDBObject eventToReturn = new BasicDBObject();
		eventToReturn.putAll(event);

		if (relativeTimezone != null) {
			String triggerTime = eventToReturn.getString(TRIGGER_TIME);
			BasicDBObject datetimeLabel = DateUtils.createLabels(triggerTime, relativeTimezone);
			DBObject datetimeLabels = (DBObject) eventToReturn.get(DATETIME_LABELS);
			datetimeLabels.put(RELATIVE_TZ, datetimeLabel);
		}
		if (isFollowingEvent) {
			eventToReturn.put(IS_EVENT_FOLLOWER, true);
		}
		addTimeRemaining(eventToReturn, System.currentTimeMillis());
		return eventToReturn;
	}

	@SuppressWarnings("unchecked")
	public List<String> fetchEventCommentSubscribers(String id) {
		DBObject event = mongo.findOne(MongoDBCollections.EVENTS, id, new BasicDBObject(
				COMMENT_EMAIL_SUBSCRIBERS, MongoDB.INCLUDE));
		if (event == null) {
			return ImmutableList.of();
		}
		List<String> subscribers = (List<String>) event.get(COMMENT_EMAIL_SUBSCRIBERS);
		if (subscribers != null) {
			return subscribers;
		}
		return Lists.newArrayList();
	}

	@SuppressWarnings("unchecked")
	private List<String> fetchEventDetailChangeSubscribers(String id) {
		DBObject event = mongo.findOne(MongoDBCollections.EVENTS, id, new BasicDBObject(
				DETAIL_CHANGE_SUBSCRIBERS, MongoDB.INCLUDE));
		if (event == null) {
			return ImmutableList.of();
		}
		List<String> subscribers = (List<String>) event.get(DETAIL_CHANGE_SUBSCRIBERS);
		if (subscribers != null) {
			return subscribers;
		}
		return Lists.newArrayList();
	}

	@SuppressWarnings("unchecked")
	public List<DBObject> fetchEventFollowers(String eventId) {
		DBObject event = mongo.findOne(MongoDBCollections.EVENTS, eventId, new BasicDBObject(
				FOLLOWERS, MongoDB.INCLUDE));
		if (event == null) {
			return ImmutableList.of();
		}
		List<DBObject> followers = (List<DBObject>) event.get(FOLLOWERS);
		if (followers != null) {
			return followers;
		}
		return Lists.newArrayList();
	}

	public List<DBObject> fetchEvents(List<String> eventIds, int start, boolean includePastEvents,
			DateTimeZone relativeTimezone, List<String> eventsFollowed) {
		long now = System.currentTimeMillis();
		QueryBuilder query = QueryBuilder.start(VISIBILITY).is(
				Visibility.PUBLIC.name().toLowerCase());
		if (!includePastEvents) {
			query = query.and(CALCULATED_TRIGGER_TIME).greaterThan(now);
		}
		query = query.and(_ID).in(eventIds);
		if (!includePastEvents) {
			query = query.and(CALCULATED_TRIGGER_TIME).greaterThan(now);
		}

		DBObject eventsToFetch = new BasicDBObject(query.get().toMap());

		return fetchPaginatedEvents(eventsToFetch, 0, SORTBY_SOONEST, relativeTimezone, true,
				eventsFollowed);
	}

	public List<DBObject> fetchMostAnticipatedEvents(int start, String category, String tag,
			String term, String title, DateTimeZone relativeTimezone, List<String> eventsFollowed) {
		return fetchPaginatedEvents(getPublicEventQuery(), start, SORTBY_MOSTANTICIPATED, category,
				tag, term, title, relativeTimezone, true, eventsFollowed);
	}

	public List<DBObject> fetchNewestEvents(int start, String category, String tag, String term,
			String title, DateTimeZone relativeTimezone, List<String> eventsFollowed) {
		return fetchPaginatedEvents(getPublicEventQuery(), start, SORTBY_NEWEST, category, tag,
				term, title, relativeTimezone, true, eventsFollowed);
	}

	public List<DBObject> fetchSoonestEvents(int start, String category, String tag, String term,
			String title, DateTimeZone relativeTimezone, List<String> eventsFollowed) {
		return fetchPaginatedEvents(getPublicEventQuery(), start, SORTBY_SOONEST, category, tag,
				term, title, relativeTimezone, true, eventsFollowed);
	}

	public List<DBObject> fetchNearestEvents(int start, double lat, double lng, String category,
			String tag, String term, String title, DateTimeZone relativeTimezone,
			List<String> eventsFollowed) {
		return fetchPaginatedEvents(getPublicEventQuery(lat, lng), start, null, category, tag,
				term, title, relativeTimezone, true, eventsFollowed);
	}

	public List<DBObject> fetchMostAnticipatedTags(String category) {
		// include events that occurred within 24 hours
		long yesterday = System.currentTimeMillis() - (1000 * 60 * 60 * 24);
		QueryBuilder whereBuilder = QueryBuilder.start(VISIBILITY)
				.is(Visibility.PUBLIC.name().toLowerCase()).and(CALCULATED_TRIGGER_TIME)
				.greaterThan(yesterday);
		if ((category != null) && (category.length() > 0)) {
			Pattern categoryQueryPattern = Pattern.compile(Pattern.quote(category.toLowerCase()
					.trim()));
			whereBuilder = whereBuilder.and(CATEGORY).regex(categoryQueryPattern);
		}
		DBObject where = whereBuilder.get();
		BasicDBObject get = new BasicDBObject(TAGS, MongoDB.INCLUDE).append(FOLLOWER_COUNT,
				MongoDB.INCLUDE).append(OWNER_ID, MongoDB.INCLUDE);
		MongoDBCursor results = mongo.findMany(MongoDBCollections.EVENTS, where, get).sort(
				SORTBY_MOSTANTICIPATED);

		Map<String, Integer> tagFollowers = Maps.newHashMap();

		while ((tagFollowers.size() < MAX_RESULT_SET_TAGS) && results.hasNext()) {
			DBObject event = results.next();

			@SuppressWarnings("unchecked")
			Collection<String> eventTags = (Collection<String>) event.get(TAGS);
			if (eventTags != null) {
				Integer followers = (Integer) event.get(FOLLOWER_COUNT);
				int eventFollowerCount = followers == null ? 0 : followers.intValue();
				if (!event.get(OWNER_ID).equals("u_srynYBY")) {
					// don't increment followers if this was
					// created by WeAnticiapte
					eventFollowerCount++;
				}
				for (String tag : eventTags) {
					Integer tagFollowerCount = tagFollowers.get(tag);
					if (tagFollowerCount == null) {
						tagFollowers.put(tag.toLowerCase(), eventFollowerCount);
					}
					else {
						tagFollowers.put(tag.toLowerCase(), tagFollowerCount + eventFollowerCount);
					}
				}
			}
		}
		SortedSet<Entry<String, Integer>> tags = entriesSortedByValues(tagFollowers);
		List<DBObject> tagsToReturn = Lists.newArrayListWithCapacity(tags.size());
		for (Entry<String, Integer> entry : tags) {
			BasicDBObject tagEntry = new BasicDBObject(entry.getKey(), entry.getValue());
			tagsToReturn.add(tagEntry);
		}
		return tagsToReturn;
	}

	public List<DBObject> fetchMostEventsTags(int start, String decodedTerm) {
		return null;
	}

	public List<DBObject> fetchUserEvents(String userId, int start, boolean includePastEvents,
			DateTimeZone relativeTimezone) {
		long now = System.currentTimeMillis();
		QueryBuilder query = QueryBuilder.start(OWNER_ID).is(userId);
		if (!includePastEvents) {
			query = query.and(CALCULATED_TRIGGER_TIME).greaterThan(now);
		}
		DBObject eventsToFetch = new BasicDBObject(query.get().toMap());

		return fetchPaginatedEvents(eventsToFetch, start, SORTBY_SOONEST, relativeTimezone, true,
				null);
	}

	public List<DBObject> fetchUserEvents(String userId, Interval interval) {
		long startTime = System.currentTimeMillis();
		long endTime = interval.getEnd().plusDays(1).getMillis();
		DBObject eventsToFetch = new BasicDBObject(QueryBuilder.start(OWNER_ID).is(userId)
				.and(CALCULATED_TRIGGER_TIME).lessThan(endTime).and(CALCULATED_TRIGGER_TIME)
				.greaterThanEquals(startTime).get().toMap());
		Iterator<DBObject> results = mongo.findMany(MongoDBCollections.EVENTS, eventsToFetch,
				DONOTINCLUDE_FOLLOWERS);
		return Lists.newArrayList(results);
	}

	public List<String> fetchCommentListenerSubscribers(String eventId, String ownerEmail,
			String commenterEmail) {
		List<String> eventCommentSubscriberEmails = fetchEventCommentSubscribers(eventId);
		if (ownerEmail != null) {
			eventCommentSubscriberEmails.add(ownerEmail);
		}
		if (commenterEmail != null) {
			eventCommentSubscriberEmails.remove(commenterEmail);
			// TODO: its crap that we get a list here rather than a set:(
		}
		return eventCommentSubscriberEmails;
	}

	public List<String> fetchEventDetailChangeSubscribers(String eventId, String eventOwnerEmail) {
		List<String> eventDetailChangeSubscribers = fetchEventDetailChangeSubscribers(eventId);
		if (eventOwnerEmail != null) {
			eventDetailChangeSubscribers.add(eventOwnerEmail);
		}
		return eventDetailChangeSubscribers;
	}

	public void removeEventFollower(String eventId, String followerId, String followerUsername,
			String email) throws IOException {
		BasicDBObject event = fetchEvent(eventId, null, false);
		if (event == null) {
			return;
		}
		BasicDBObject updates = new BasicDBObject();
		// this code assumes the user has already been verified to follow the
		// event already
		BasicDBObject pullFields = new BasicDBObject();
		updates.put(MongoDB.CMD_PULL, pullFields);
		pullFields.put(FOLLOWERS,
				new BasicDBObject(USER_ID, followerId).append(USERNAME, followerUsername));
		pullFields.put(COMMENT_EMAIL_SUBSCRIBERS, email);
		pullFields.put(DETAIL_CHANGE_SUBSCRIBERS, email);
		updates.put(MongoDB.CMD_INCREMENT, new BasicDBObject(FOLLOWER_COUNT, -1));
		mongo.updateOne(MongoDBCollections.EVENTS, eventId, updates);
		QueryBuilder.start(EVENT_ID).is(eventId).and(FOLLOWER_ID).is(followerId).get();
	}

	public void settingsChanged(String userId, String email, BasicDBObject changedSettings,
			List<String> eventsFollowed) {
		if (changedSettings.containsField(UserService.SETTING_FOLLOWEDEVENTS_NEWCOMMENT_FLAG)) {
			updateSettingsForFollowedEventsNewComment(email, changedSettings, eventsFollowed);
		}
		if (changedSettings.containsField(UserService.SETTING_FOLLOWEDEVENTS_MODIFIED_FLAG)) {
			updateSettingsForFollowedEventsDetailChanges(email, changedSettings, eventsFollowed);
		}
	}

	private void updateSettingsForFollowedEventsDetailChanges(String email,
			BasicDBObject changedSettings, List<String> eventsFollowed) {
		boolean doNotify = changedSettings
				.getBoolean(UserService.SETTING_FOLLOWEDEVENTS_MODIFIED_FLAG);
		for (String eventId : eventsFollowed) {
			BasicDBObject updates = new BasicDBObject();
			if (doNotify) {
				updates.put(MongoDB.CMD_ADD_TO_SET, new BasicDBObject(DETAIL_CHANGE_SUBSCRIBERS,
						email));
			}
			else {
				updates.put(MongoDB.CMD_PULL, new BasicDBObject(DETAIL_CHANGE_SUBSCRIBERS, email));
			}
			mongo.updateOne(MongoDBCollections.EVENTS, eventId, updates);
		}
	}

	private void updateSettingsForFollowedEventsNewComment(String email,
			BasicDBObject changedSettings, List<String> eventsFollowed) {
		boolean doNotify = changedSettings
				.getBoolean(UserService.SETTING_FOLLOWEDEVENTS_NEWCOMMENT_FLAG);
		for (String eventId : eventsFollowed) {
			BasicDBObject updates = new BasicDBObject();
			if (doNotify) {
				updates.put(MongoDB.CMD_ADD_TO_SET, new BasicDBObject(COMMENT_EMAIL_SUBSCRIBERS,
						email));
			}
			else {
				updates.put(MongoDB.CMD_PULL, new BasicDBObject(COMMENT_EMAIL_SUBSCRIBERS, email));
			}
			mongo.updateOne(MongoDBCollections.EVENTS, eventId, updates);
		}
	}

	private void addTimeRemaining(BasicDBObject event, long now) {
		if (!event.getBoolean(CANCELED)) {
			long triggerTime = event.getLong(CALCULATED_TRIGGER_TIME);
			long timeLeft = triggerTime - System.currentTimeMillis();
			if (timeLeft < 0) {
				timeLeft = -1;
			}
			else {
				timeLeft = timeLeft / 60000;
			}
			event.put(TIME_REMAINING_IN_MINUTES, timeLeft);
		}
	}

	private void copyTriggerTime(BasicDBObject newEventSubmission, BasicDBObject toEvent)
			throws MissingFieldException, InvalidFieldValueException {
		String timezone = toEvent.getString(TIMEZONE);
		String fieldValue = newEventSubmission.getString(TRIGGER_TIME) + "." + timezone;
		System.out.println("newEventSubmssion triggerTime of new event: " + fieldValue);
		if (Strings.isNullOrEmpty(fieldValue)) {
			throw new MissingFieldException(TRIGGER_TIME);
		}
		try {
			long triggerTimeInMillis = DateUtils.calculateMillis(fieldValue);
			long millisUntilTrigger = triggerTimeInMillis - System.currentTimeMillis();
			System.out.println("millisUntilTrigger: " + millisUntilTrigger + ", now: "
					+ System.currentTimeMillis());
			if (millisUntilTrigger < NEAREST_TRIGGER_THRESHOLD) {
				throw new InvalidFieldValueException(TRIGGER_TIME,
						"An event cannot be scheduled to occur sooner than "
								+ NEAREST_TRIGGER_THRESHOLD / 1000 + " seconds from now");
			}
			if (millisUntilTrigger > FURTHEST_TRIGGER_THRESHOLD) {
				throw new InvalidFieldValueException(TRIGGER_TIME,
						"An event cannot be scheduled to occur later than 2 years from now");
			}
			BasicDBObject datetimeLabel = DateUtils.createLabels(fieldValue);
			BasicDBObject datetimeLabels = new BasicDBObject();
			toEvent.put(DATETIME_LABELS, datetimeLabels);
			datetimeLabels.put(OWNER_TZ, datetimeLabel);
			toEvent.put(TRIGGER_TIME, fieldValue);

			toEvent.put(CALCULATED_TRIGGER_TIME, triggerTimeInMillis);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			throw new InvalidFieldValueException(TRIGGER_TIME, TRIGGER_TIME
					+ " must be of the form '" + DateUtils.DATETIME_PATTERN);
		}
		catch (IllegalArgumentException e) {
			throw new InvalidFieldValueException(TRIGGER_TIME, TRIGGER_TIME
					+ " has an invalid value. " + e.getMessage());
		}
	}

	private void copyVisibility(BasicDBObject newEventSubmission, BasicDBObject toEvent)
			throws MissingFieldException, InvalidFieldValueException {
		String fieldValue = newEventSubmission.getString(VISIBILITY);
		if (Strings.isNullOrEmpty(fieldValue)) {
			fieldValue = Visibility.PUBLIC.name().toLowerCase();
		}
		try {
			if (Visibility.valueOf(fieldValue.toUpperCase()) == null) {
				throw new InvalidFieldValueException(VISIBILITY, fieldValue,
						enumToString(Visibility.values()));
			}
		}
		catch (IllegalArgumentException e) {
			throw new InvalidFieldValueException(VISIBILITY, fieldValue,
					enumToString(Visibility.values()));
		}
		toEvent.put(VISIBILITY, fieldValue);
	}

	private ImmutableList<String> enumToString(Object[] enumOptions) {
		List<String> options = Lists.newLinkedList();
		for (Object option : enumOptions) {
			options.add(option.toString().toLowerCase());
		}
		return ImmutableList.copyOf(options);
	}

	private List<DBObject> fetchPaginatedEvents(DBObject query, int page, DBObject sortBy,
			DateTimeZone relativeTimezone, boolean useLimit, List<String> eventsFollowed) {

		MongoDBCursor results = mongo.findMany(MongoDBCollections.EVENTS, query,
				DONOTINCLUDE_FOLLOWERS);
		if (sortBy != null) {
			results = results.sort(sortBy);
		}
		if (useLimit) {
			results = results.skip(page * MAX_RESULT_SET).limit(MAX_RESULT_SET);
		}
		List<DBObject> events = Lists.newArrayListWithCapacity(MAX_RESULT_SET);
		Set<String> eventsFollowedSet = Sets.newHashSet();
		if (eventsFollowed != null) {
			eventsFollowedSet.addAll(eventsFollowed);
		}
		long now = System.currentTimeMillis();
		while (results.hasNext()) {
			BasicDBObject event = new BasicDBObject();
			event.putAll(results.next());
			if (relativeTimezone != null) {
				String triggerTime = event.getString(TRIGGER_TIME);
				BasicDBObject datetimeLabel = DateUtils.createLabels(triggerTime, relativeTimezone);
				DBObject datetimeLabels = (DBObject) event.get(DATETIME_LABELS);
				datetimeLabels.put(RELATIVE_TZ, datetimeLabel);
			}
			String eventId = extractEventIdFromEvent(event);
			boolean isFollowingEvent = eventsFollowedSet.contains(eventId);
			event.put(IS_EVENT_FOLLOWER, isFollowingEvent);
			addTimeRemaining(event, now);
			events.add(event);
		}
		return events;

	}

	private List<DBObject> fetchPaginatedEvents(QueryBuilder queryBuilder, int start,
			DBObject sortBy, String category, String tag, String searchString, String title,
			DateTimeZone relativeTimezone, boolean useLimit, List<String> eventsFollowed) {

		if (searchString != null) {
			List<String> terms = Lists.newLinkedList(Arrays.asList(searchString.split(" ")));
			Iterator<String> iter = terms.iterator();
			while (iter.hasNext()) {
				String term = iter.next();
				if (neutralWords.contains(term)) {
					iter.remove();
				}
				else {
					queryBuilder = queryBuilder.and(SUMMARY).regex(
							Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE));
				}
			}
		}
		if (category != null) {
			Pattern categoryQueryPattern = Pattern.compile(Pattern.quote(category.toLowerCase()
					.trim()));
			queryBuilder = queryBuilder.and(CATEGORY).regex(categoryQueryPattern);
		}
		if (tag != null) {
			queryBuilder = queryBuilder.and(TAGS).is(tag.toLowerCase().trim());
		}
		if (title != null) {
			queryBuilder = queryBuilder.and(SUMMARY).is(title);
		}
		// DBObject ownerQuery =
		// QueryBuilder.start(OWNER_USERNAME).is(searchString).get();

		// orQueries.add(ownerQuery);
		DBObject query = queryBuilder.get();
		System.out.println("EventService QUERY: " + query);
		return fetchPaginatedEvents(query, start, sortBy, relativeTimezone, useLimit,
				eventsFollowed);
	}

	private QueryBuilder getPublicEventQuery() {
		// include events that occurred within 24 hours
		long yesterday = System.currentTimeMillis() - (1000 * 60 * 60 * 24);
		return QueryBuilder.start(VISIBILITY).is(Visibility.PUBLIC.name().toLowerCase())
				.and(CALCULATED_TRIGGER_TIME).greaterThan(yesterday);
	}

	private QueryBuilder getPublicEventQuery(double lat, double lng) {
		return getPublicEventQuery().and(EVENT_LOCATION_COORDINATES).near(lng, lat);
	}

	public void storeInviteRequest(String inviteRequestId, String eventId, String facebookUserId) {

		DBObject request = fetchEventRequest(inviteRequestId);
		if (request == null) {
			request = new BasicDBObject(FACEBOOK_INVITE_REQUEST, inviteRequestId);
			request.put(FACEBOOK_INVITE_REQUEST_EVENT, eventId);
			List<String> facebookUserIds = Lists.newArrayList();
			facebookUserIds.add(facebookUserId);
			request.put(FACEBOOK_INVITE_REQUEST_USERS, facebookUserIds);
			mongo.insert(MongoDBCollections.FACEBOOK_INVITE_REQUESTS, request);
		}
		else {
			DBObject update = new BasicDBObject(MongoDB.CMD_ADD_TO_SET, new BasicDBObject(
					FACEBOOK_INVITE_REQUEST_USERS, facebookUserId));
			mongo.updateOne(MongoDBCollections.FACEBOOK_INVITE_REQUESTS, inviteRequestId, update);
		}

	}

	public DBObject fetchEventRequest(String inviteRequestId) {
		return mongo.findOne(MongoDBCollections.FACEBOOK_INVITE_REQUESTS, inviteRequestId);
	}
}

