package com.n0tice.api.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.n0tice.api.client.exceptions.AuthorisationException;
import com.n0tice.api.client.exceptions.BadRequestException;
import com.n0tice.api.client.exceptions.HttpFetchException;
import com.n0tice.api.client.exceptions.MissingCredentialsExeception;
import com.n0tice.api.client.exceptions.N0ticeException;
import com.n0tice.api.client.exceptions.NotAllowedException;
import com.n0tice.api.client.exceptions.NotFoundException;
import com.n0tice.api.client.exceptions.ParsingException;
import com.n0tice.api.client.model.AccessToken;
import com.n0tice.api.client.model.Consumer;
import com.n0tice.api.client.model.Content;
import com.n0tice.api.client.model.FlagType;
import com.n0tice.api.client.model.GeoCodingResolution;
import com.n0tice.api.client.model.Group;
import com.n0tice.api.client.model.HistoryItem;
import com.n0tice.api.client.model.ImageSize;
import com.n0tice.api.client.model.MediaFile;
import com.n0tice.api.client.model.MediaInfo;
import com.n0tice.api.client.model.MediaType;
import com.n0tice.api.client.model.ModerationAction;
import com.n0tice.api.client.model.ModerationComplaint;
import com.n0tice.api.client.model.ModerationComplaintType;
import com.n0tice.api.client.model.ModerationState;
import com.n0tice.api.client.model.NewUserResponse;
import com.n0tice.api.client.model.Noticeboard;
import com.n0tice.api.client.model.NoticeboardResultSet;
import com.n0tice.api.client.model.NoticeboardSearchQuery;
import com.n0tice.api.client.model.Repost;
import com.n0tice.api.client.model.ResultSet;
import com.n0tice.api.client.model.SearchQuery;
import com.n0tice.api.client.model.Tag;
import com.n0tice.api.client.model.TagSet;
import com.n0tice.api.client.model.Update;
import com.n0tice.api.client.model.User;
import com.n0tice.api.client.model.VideoAttachment;
import com.n0tice.api.client.model.Vote;
import com.n0tice.api.client.oauth.N0ticeOauthApi;
import com.n0tice.api.client.parsers.ConsumerParser;
import com.n0tice.api.client.parsers.ExifParser;
import com.n0tice.api.client.parsers.HistoryParser;
import com.n0tice.api.client.parsers.MediaInfoParser;
import com.n0tice.api.client.parsers.ModerationComplaintParser;
import com.n0tice.api.client.parsers.NoticeboardParser;
import com.n0tice.api.client.parsers.SearchParser;
import com.n0tice.api.client.parsers.UserParser;
import com.n0tice.api.client.urls.SearchUrlBuilder;
import com.n0tice.api.client.urls.UrlBuilder;
import com.n0tice.api.client.util.HttpFetcher;

public class N0ticeApi {
	
	private static final Logger log = Logger.getLogger(N0ticeApi.class);
	
	private static final String ACTION = "action";
	private static final String BIO = "bio";
	private static final String BODY = "body";
	private static final String COLOUR = "colour";
	private static final String CONSUMER_KEY = "consumerkey";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CREATE = "create";
	private static final String DATE = "date";
	private static final String DESCRIPTION = "description";
	private static final String DISPLAY_NAME = "displayName";
	private static final String EMAIL = "email";
	private static final String HEADLINE = "headline";
	private static final String IMAGE = "image";
	private static final String MODERATION_USER = "moderationUser";
	private static final String NAME = "name";
	private static final String NOTES = "notes";
	private static final String NOTICEBOARD = "noticeboard";
	private static final String OK = "ok";
	private static final String PASSWORD = "password";
	private static final String TOKEN = "token";
	private static final String USERNAME = "username";
	private static final String REALNAME = "realName";

	private static final int HTTP_OK = 200;
	private static final int HTTP_BAD_REQUEST = 400;
	private static final int HTTP_UNAUTHORISED = 401;
	private static final int HTTP_FORBIDDEN = 403;
	private static final int HTTP_NOT_FOUND = 404;
	
	private static final String UTF_8 = "UTF-8";
	private static final String COMMA = ",";
	
	private static final DateTimeFormatter ZULE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
	
	private final UrlBuilder urlBuilder;
	private final SearchUrlBuilder searchUrlBuilder;
	private final HttpFetcher httpFetcher;
	private final SearchParser searchParser;
	private final UserParser userParser;
	private final NoticeboardParser noticeboardParser;
	private final ModerationComplaintParser moderationComplaintParser;
	private final HistoryParser historyParser;
	private final ExifParser exifParser;
	private final MediaInfoParser mediaInfoParser;
	private final ConsumerParser consumerParser;

	private OAuthService service;
	private Token scribeAccessToken;
	
	public N0ticeApi(String apiUrl) {
		this.urlBuilder = new UrlBuilder(apiUrl);
		this.searchUrlBuilder = new SearchUrlBuilder(apiUrl);
		this.httpFetcher = new HttpFetcher();
		this.searchParser = new SearchParser();
		this.userParser = new UserParser();
		this.noticeboardParser = new NoticeboardParser();
		this.historyParser = new HistoryParser();
		this.moderationComplaintParser = new ModerationComplaintParser();
		this.exifParser = new ExifParser();
		this.mediaInfoParser = new MediaInfoParser();
		this.consumerParser = new ConsumerParser();
	}
	
	public N0ticeApi(String apiUrl, String consumerKey, String consumerSecret, AccessToken accessToken) {
		this.urlBuilder = new UrlBuilder(apiUrl);
		this.searchUrlBuilder = new SearchUrlBuilder(apiUrl);
		this.httpFetcher = new HttpFetcher();
		this.searchParser = new SearchParser();
		this.userParser = new UserParser();
		this.noticeboardParser = new NoticeboardParser();
		this.historyParser = new HistoryParser();
		this.moderationComplaintParser = new ModerationComplaintParser();
		this.exifParser = new ExifParser();
		this.mediaInfoParser = new MediaInfoParser();
		this.consumerParser = new ConsumerParser();
		
		service = new ServiceBuilder().provider(new N0ticeOauthApi(apiUrl))
			.apiKey(consumerKey)
			.apiSecret(consumerSecret)
			.build();
		scribeAccessToken = new Token(accessToken.getToken(), accessToken.getSecret());
	}
	
	public Content get(String id) throws N0ticeException {
		return searchParser.parseContent(httpFetcher.fetchContent(urlBuilder.get(id), UTF_8));
	}
	
	public List<HistoryItem> getHistory(String id) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.getHistory(id));
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == 200) {
			return historyParser.parse(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Map<String, Object> getMeta(String id) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id) + "/meta");
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == 200) {
			return searchParser.parseKeyValueObjectMap(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public List<ModerationComplaint> getModerationComplaints(String id) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id) + "/flags");
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == 200) {
			return moderationComplaintParser.parse(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public void closeModerationComplaint(String contentId, int flagId, String moderationUser) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.closeModerationComplaint(contentId, flagId));
		addBodyParameter(request, MODERATION_USER, moderationUser);
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == 200) {
			getResponseBody(response);
			return;
		}
		
		throw handleExceptions(response);		
	}
	
	public void closeModerationComplaint(String contentId, int flagId) throws N0ticeException {
		closeModerationComplaint(contentId, flagId, null);
	}
	
	public List<Repost> getReposts(String id) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id) + "/reposts");
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return searchParser.parseReposts(getResponseBody(response));
		}
		
		throw handleExceptions(response);		
	}
	
	public Update getUpdate(String id) throws N0ticeException {
		return searchParser.parseUpdate(httpFetcher.fetchContent(urlBuilder.get(id), UTF_8));
	}
	
	public Content authedGet(String id) throws N0ticeException {		
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id));
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return searchParser.parseContent(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public String history() throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get("history"));
		oauthSignRequest(request);

		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return getResponseBody(response);
		}
		
		throw handleExceptions(response);
	}
	
	public Map<String, Map<String, String>> imageExif(String id) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get("image/" + id + "/exif"));
		oauthSignRequest(request);

		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return exifParser.parse(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public MediaInfo videoMediaInfo(String id) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id + "/mediainfo"));
		oauthSignRequest(request);

		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return mediaInfoParser.parse(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public ResultSet search(SearchQuery searchQuery) throws N0ticeException {
		return searchParser.parseSearchResults(httpFetcher.fetchContent(searchUrlBuilder.toUrl(searchQuery), UTF_8));
	}
	
	public NoticeboardResultSet searchNoticeboards(NoticeboardSearchQuery noticeboardSearchQuery) throws N0ticeException {
		return noticeboardParser.parseNoticeboardSearchResults(httpFetcher.fetchContent(searchUrlBuilder.toUrl(noticeboardSearchQuery), UTF_8));
	}
	
	public User user(String username) throws N0ticeException {
		return userParser.parseUserProfile(httpFetcher.fetchContent(urlBuilder.userProfile(username), UTF_8));
	}
	
	public Map<String, Object> contactUser(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.userProfile(username) + "/contact");
		oauthSignRequest(request);
		
		Response response = request.send();
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseKeyValueObjectMap(getResponseBody(response));
		}
		
		throw handleExceptions(response);		
	}
	
	public List<User> followedUsers(String username) throws N0ticeException {
		return userParser.parseUserProfiles(httpFetcher.fetchContent(urlBuilder.userFollowedUsers(username), UTF_8));
	}
	
	public List<User> noticeboardAdmins(String domain) throws NotFoundException, ParsingException, HttpFetchException {
		return userParser.parseUserProfiles(httpFetcher.fetchContent(urlBuilder.noticeboardAdmins(domain), UTF_8));
	}
	
	public List<Noticeboard> followedNoticeboards(String username) throws N0ticeException {
		return noticeboardParser.parseNoticeboards(httpFetcher.fetchContent(urlBuilder.userFollowedNoticeboards(username), UTF_8));
	}
	
	public List<Noticeboard> noticeboards(String username) throws N0ticeException {
		return noticeboardParser.parseNoticeboards(httpFetcher.fetchContent(urlBuilder.userNoticeboards(username), UTF_8));
	}
	
	public Noticeboard noticeBoard(String noticeboard) throws N0ticeException {
		return noticeboardParser.parseNoticeboardResult(httpFetcher.fetchContent(urlBuilder.noticeBoard(noticeboard), UTF_8));
	}
	
	public User verify() throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.verify());
		oauthSignRequest(request);
		
		Response response = request.send();
		if (response.getCode() == HTTP_OK) {
	    	return userParser.parseUserProfile(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Content postReport(String headline, Double latitude, Double longitude, String body, String link, MediaFile image, VideoAttachment video, String noticeboard) throws N0ticeException {
		return postReport(headline, null, latitude, longitude, body, link, image, video, noticeboard, null, Lists.<String>newArrayList());		
	}
	
	public Content postReport(String headline, Double latitude, Double longitude, String body, String link, MediaFile image, VideoAttachment video, String noticeboard, DateTime date, List<String> tags) throws N0ticeException {
		return postReport(headline, null, latitude, longitude, body, link, image, video, noticeboard, date, tags);
	}
	
	public Noticeboard createNoticeboard(String name, String description, boolean moderated, Date endDate, Date embargoDate, Set<MediaType> supportedMediaTypes, String group, 
			MediaFile cover, boolean featured, boolean isolated) throws N0ticeException {
		return postNewNoticeboard(null, name, description, moderated, endDate, embargoDate, supportedMediaTypes, group, cover, featured, isolated, null, null);
	}
	
	public Noticeboard createNoticeboard(String domain, String name, String description, boolean moderated, Date endDate, Date embargoDate, Set<MediaType> supportedMediaTypes, String group, MediaFile cover, boolean featured, boolean isolated) throws N0ticeException {
		return postNewNoticeboard(domain, name, description, moderated, endDate, embargoDate, supportedMediaTypes, group, cover, featured, isolated, null, null);
	}
	
	public Noticeboard createNoticeboard(String domain, String name, String description, boolean moderated, Date endDate, Date embargoDate,
			Set<MediaType> supportedMediaTypes, String group, MediaFile cover, boolean featured, boolean isolated,
			GeoCodingResolution geoCodingResolution, List<String> tags) throws N0ticeException {
		return postNewNoticeboard(domain, name, description, moderated, endDate, embargoDate, supportedMediaTypes, group, cover, featured, isolated, geoCodingResolution, tags);
	}
	
	public Noticeboard editNoticeboard(String domain, String name, String description, Boolean moderated, Boolean featured, MediaFile cover, Date endDate, Date embargoDate, Set<MediaType> supportedMediaTypes, String group, Boolean isolated, GeoCodingResolution geoCodingResolution, List<String> tags) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.noticeBoard(domain));
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		addEntityPartParameter(entity, NAME, name);
		addEntityPartParameter(entity, DESCRIPTION, description);
		addEntityPartParameter(entity, "moderated", moderated != null ? Boolean.toString(moderated) : null);
		addEntityPartParameter(entity, "featured", featured != null ? Boolean.toString(featured) : null);
		addEntityPartParameter(entity, "isolated", isolated != null ? Boolean.toString(isolated) : null);
		addEntityPartParameter(entity, "geoCodingResolution", geoCodingResolution != null ? geoCodingResolution.toString() : null);
		
		if (cover != null) {
			entity.addPart("cover", new ByteArrayBody(cover.getData(), cover.getFilename()));
		}
		if (endDate != null) {
			addEntityPartParameter(entity, "endDate", ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(endDate)));
		}
		if (embargoDate != null) {
			addEntityPartParameter(entity, "embargoDate", ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(embargoDate)));
		}
		if (supportedMediaTypes != null) {
			addEntityPartParameter(entity, "supportedMediaTypes", formatSupportMediaTypes(supportedMediaTypes));
		}
		if (group != null) {
			addEntityPartParameter(entity, "group", group);
		}
		if (tags != null) {
			addEntityPartParameter(entity, "tags", formatTags(tags));
		}
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
	    	return noticeboardParser.parseNoticeboardResult(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
 	
	public void closeNoticeboard(String domain) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.closeNoticeboard(domain));
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return;
		}
		
		throw handleExceptions(response);
	}
	
	public void deleteNoticeboard(String domain) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.DELETE, urlBuilder.noticeBoard(domain));
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return;
		}
		
		throw handleExceptions(response);
	}
	
	public void deleteUser(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.DELETE, urlBuilder.userProfile(username));
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return;
		}
		
		throw handleExceptions(response);
	}
	
	public TagSet createTagSet(String name, String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.newTagSet(username));
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		addStringPart(entity, NAME, name);
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTagSet(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public List<TagSet> tagSets(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.user(username) + "/tagsets");
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTagSets(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	
	public TagSet tagSet(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id));
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTagSet(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Tag createTag(Tag tag) throws N0ticeException, JsonProcessingException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(tag.getTagSet().getId()));		
		request.addBodyParameter("tag", new ObjectMapper().writeValueAsString(tag));	// TODO can't work out how to make Scribe and Spring o
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTag(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Tag tag(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id));
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTag(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Tag editTag(Tag tag) throws N0ticeException, JsonProcessingException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(tag.getId()));		
		request.addBodyParameter("tag", new ObjectMapper().writeValueAsString(tag));	// TODO can't work out how to make Scribe and Spring o
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTag(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
		
	public List<Tag> tags(String tagSet) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(tagSet) + "/tags");
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTags(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public List<Tag> tags(String tagSet, String q) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(tagSet) + "/tags");
		request.addQuerystringParameter("q", q);
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseTags(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Group createGroup(String name, String colour) throws N0ticeException {
		return createGroup(name, colour, null);
	}
	
	public Group createGroup(String name, String colour, String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.newGroup());
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		addEntityPartParameter(entity, NAME, name);
		addEntityPartParameter(entity, COLOUR, colour);
		addEntityPartParameter(entity, USERNAME, username);
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseGroup(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Group group(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id));
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseGroup(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public List<Group> groups(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.user(username) + "/groups");

		Response response = request.send();

		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseGroups(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Group updateGroup(String id, String name, String colour) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id));
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		if (!Strings.isNullOrEmpty(name)) {
			addStringPart(entity, NAME, name);
		}
		if (!Strings.isNullOrEmpty(colour)) {
			addStringPart(entity, COLOUR, colour);
		}
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseGroup(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Content postReport(String headline, String placeName, Double latitude, Double longitude, String body, String link, 
			MediaFile image, VideoAttachment video, String noticeboard, DateTime date, List<String> tags) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.newReport());
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		if (headline != null) {
			addStringPart(entity, HEADLINE, headline);
		}
		if (noticeboard != null) {
			addStringPart(entity, NOTICEBOARD, noticeboard);
		}
		if (latitude != null && longitude != null) {			
			addStringPart(entity, "latitude", Double.toString(latitude));
			addStringPart(entity, "longitude", Double.toString(longitude));						
			if (placeName != null) {
				addStringPart(entity, "placename", placeName);
			}
		}
		if (tags != null) {
			final String tagsValue = formatTags(tags);
			addEntityPartParameter(entity, "tags", tagsValue);
		}
				
		populateUpdateFields(body, link, image, video, entity);
		
		if (date != null) {
			addStringPart(entity, DATE, date.toString(ZULE_TIME_FORMAT));
		}
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseContent(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public ResultSet authedSearch(SearchQuery searchQuery) throws N0ticeException {				
		OAuthRequest request = createOauthRequest(Verb.GET, searchUrlBuilder.toUrl(searchQuery));
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseSearchResults(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Content postReportUpdate(String reportId, String body, String link, MediaFile image, VideoAttachment video) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(reportId)  + "/update/new");
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		populateUpdateFields(body, link, image, video, entity);

		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseContent(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public void deleteUpdate(String updateId) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.DELETE, urlBuilder.get(updateId));
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return;
		}
		
		throw handleExceptions(response);
	}
	
	public boolean voteInteresting(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id) + "/vote/interesting");	
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}

		throw handleExceptions(response);
	}
	
	public boolean removeInterestingVote(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.DELETE, urlBuilder.get(id) + "/vote/interesting");	
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}

		throw handleExceptions(response);
	}
		
	public boolean flagAsInappropriate(String id, ModerationComplaintType type, String notes, String email, String moderationUser) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id) + "/flag");
		if (type != null) {
			addBodyParameter(request, "type", type.toString());
		}
		if (notes != null) {
			addBodyParameter(request, NOTES, notes);
		}
		if (email != null) {
			addBodyParameter(request, EMAIL, email);
		}
		addBodyParameter(request, MODERATION_USER, moderationUser);
		if (scribeAccessToken != null) {
			oauthSignRequest(request);
		}
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}
		
		throw handleExceptions(response);
	}
	
	public boolean flagAsInappropriate(String id, ModerationComplaintType type, String notes, String email) throws N0ticeException {
		return flagAsInappropriate(id, type, notes, email, null);
	}
	
	public boolean repost(String id, String noticeboard) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id) + "/repost");
		addBodyParameter(request, NOTICEBOARD, noticeboard);
		oauthSignRequest(request);

		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}

		throw handleExceptions(response);
	}
	
	public List<ModerationAction> moderationActions(String username) throws N0ticeException {
		return moderationComplaintParser.parseModerationActions(httpFetcher.fetchContent(urlBuilder.moderationActions(username), UTF_8));
	}
	
	public List<ModerationState> moderationStates(String username) throws N0ticeException {
		return moderationComplaintParser.parseModerationStates(httpFetcher.fetchContent(urlBuilder.moderationStates(username), UTF_8));
	}
	
	public List<FlagType> flagTypes() throws N0ticeException {
		return moderationComplaintParser.parseFlagTypes(httpFetcher.fetchContent(urlBuilder.flagTypes(), UTF_8));
	}
	
	public boolean moderate(String id, String notes, String action, String moderationUser) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id) + "/moderate");
		addBodyParameter(request, ACTION, action);
		addBodyParameter(request, NOTES, notes);
		addBodyParameter(request, MODERATION_USER, moderationUser);
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}

		throw handleExceptions(response);
	}
	
	public boolean moderate(String id, String notes, String action) throws N0ticeException {
		return moderate(id, notes, action, null);
	}
	
	public boolean notify(String id, String message, String moderationUser) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id) + "/notify");
		addBodyParameter(request, "message", message);
		addBodyParameter(request, MODERATION_USER, moderationUser);
		oauthSignRequest(request);
		
		final Response response = request.send();		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}

		throw handleExceptions(response);
	}
	
	public List<Vote> interestingVotes(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.interestingVotesUrl(id));	
			
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			return searchParser.parseVotes(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public List<Vote> interestingVotes(String id, String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.interestingVotesUrl(id, username));	
			
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			return searchParser.parseVotes(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Content updateReport(String id, String headline, String body, String noticeboard) throws N0ticeException {	
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id));	
		addBodyParameter(request, HEADLINE, headline);
		addBodyParameter(request, BODY, body);
		addBodyParameter(request, NOTICEBOARD, noticeboard);
		oauthSignRequest(request);
		
		Response response = request.send();
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseContent(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public boolean followUser(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.user(username) + "/follow");	
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}
		
		throw handleExceptions(response);
	}
	
	public boolean unfollowUser(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.user(username) + "/unfollow");	
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}
		
		throw handleExceptions(response);
	}
	
	public boolean followNoticeboard(String noticeboard) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.noticeBoard(noticeboard) + "/follow");	
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}
		
		throw handleExceptions(response);
	}
	
	public boolean unfollowNoticeboard(String noticeboard) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.noticeBoard(noticeboard) + "/unfollow");	
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
	    	return true;
		}
		
		throw handleExceptions(response);
	}
	
	public Consumer createConsumer(String username, String name, String description) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.consumers());		
		addBodyParameter(request, USERNAME, username);
		addBodyParameter(request, NAME, name);
		addBodyParameter(request, DESCRIPTION, description);
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return consumerParser.parseConsumer(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public List<Consumer> getConsumers() throws N0ticeException {		
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.consumers());		
		oauthSignRequest(request);
				
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return consumerParser.parseConsumers(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public NewUserResponse createUser(String consumerKey, String username, String password, String email) throws N0ticeException {
		return createUser(consumerKey, username, password, email, null, null, null);
	}
	
	public NewUserResponse createUser(String consumerKey, String username, String password, String email, String twitterAccessToken, String twitterAccessSecret, String facebookAccessToken) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.newUser());		
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, USERNAME, username);
		addBodyParameter(request, PASSWORD, password);
		addBodyParameter(request, EMAIL, email);
		addBodyParameter(request, "twitterAccessToken", twitterAccessToken);
		addBodyParameter(request, "twitterAccessSecret", twitterAccessSecret);
		addBodyParameter(request, "facebookAccessToken", facebookAccessToken);
		
		final Response response = request.send();

		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseNewUserResponse(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public AccessToken authUser(String consumerKey, String username, String password, String consumerSecret) throws N0ticeException {
		log.debug("Attempting to auth user with username and password: " + username);
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, USERNAME, username);
		addBodyParameter(request, PASSWORD, password);

		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
	
	public AccessToken authGigyaUser(String consumerKey, String uid, String consumerSecret) throws N0ticeException {
		log.debug("Attempting to auth Gigya user: " + consumerKey + ", " + uid);
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, "gigyaUID", uid);

		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
	
	public AccessToken authGuardianUser(String consumerKey, String token, String consumerSecret) throws N0ticeException {
		log.debug("Attempting to auth guardian user: " + consumerKey + ", " + token);
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, "guardianToken", token);

		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
	
	public AccessToken authBritannicaCookie(String consumerKey, String cookie, String consumerSecret) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, "britannicaCookie", cookie);
		
		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
	
	public AccessToken authGuardianCookie(String consumerKey, String cookie, String consumerSecret) throws N0ticeException {
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, "guardianCookie", cookie);
		
		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
		
	public AccessToken authTwitterUser(String consumerKey, String twitterAccessToken, String twitterAccessSecret, String consumerSecret) throws N0ticeException {
		return authTwitterUser(consumerKey, twitterAccessToken, twitterAccessSecret, consumerSecret, true);
	}
	
	public AccessToken authTwitterUser(String consumerKey, String twitterAccessToken, String twitterAccessSecret, String consumerSecret, boolean createAccount) throws N0ticeException {
		log.debug("Attempting to auth twitter user using twitter token: " + twitterAccessToken);
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, "twitterAccessToken", twitterAccessToken);
		addBodyParameter(request, "twitterAccessSecret", twitterAccessSecret);
		addBodyParameter(request, CREATE, Boolean.toString(createAccount));

		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
	
	public AccessToken authFacebookUser(String consumerKey, String facebookToken, String consumerSecret) throws N0ticeException {
		return authFacebookUser(consumerKey, facebookToken, consumerSecret, true);
	}
	
	public AccessToken authFacebookUser(String consumerKey, String facebookToken, String consumerSecret, boolean createAccount) throws N0ticeException {
		log.debug("Attempting to auth facebook user using facebook token: " + facebookToken);
		final OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.auth());
		addBodyParameter(request, CONSUMER_KEY, consumerKey);
		addBodyParameter(request, "facebookAccessToken", facebookToken);
		addBodyParameter(request, CREATE, Boolean.toString(createAccount));

		manuallySignRequest(consumerSecret, request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {		
			return new UserParser().parseAuthUserResponse(getResponseBody(response)).getAccessToken();
		}
		
		throw handleExceptions(response);
	}
	
	public User updateUserDetails(String username, String displayName, String bio, MediaFile image, VideoAttachment preroll, String realName) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.user(username));		
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		if (displayName != null) {
			addStringPart(entity, DISPLAY_NAME, displayName);
		}
		if (bio != null) {
			addStringPart(entity, BIO, bio);
		}
		if (realName != null) {
			addStringPart(entity, REALNAME, realName);
		}
		if (image != null) {
			entity.addPart(IMAGE, new ByteArrayBody(image.getData(), image.getFilename()));
		}
		if (preroll != null) {
			entity.addPart("preroll", new InputStreamBody(preroll.getData(), preroll.getFilename()));			
		}
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();

		if (response.getCode() == HTTP_OK) {
			return new UserParser().parseUserProfile(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public boolean deleteReport(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.DELETE, urlBuilder.get(id));	
		oauthSignRequest(request);
		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
			getResponseBody(response);
			return true;
		}
		
		throw handleExceptions(response);
	}
	
	public InputStream downloadOriginal(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id) + "/original");	
		oauthSignRequest(request);

		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return response.getStream();
		}
		
		throw handleExceptions(response);
	}
	
	public InputStream downloadVideoPreview(String id, String type) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.get(id) + "/preview" + (type != null ? "?type=" + type : ""));		
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			// TODO need to capture Content-Length so that it can be relayed to the final end user.
			return response.getStream();
		}
		
		throw handleExceptions(response);
	}
	
	public void updateVideoPreview(String id) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.get(id) + "/preview");
		oauthSignRequest(request);
		
		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return;
		}
		
		throw handleExceptions(response);
	}
	
	public InputStream previewImage(String id, ImageSize size) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.getPreviewImageUrl(id, size));	
		oauthSignRequest(request);

		final Response response = request.send();
		if (response.getCode() == HTTP_OK) {
			return response.getStream();
		}
		
		throw handleExceptions(response);
	}
	
	public Map<String, String> getWhitelabelSites() throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.getWhitelabelSitesUrl());		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseKeyValueMap(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public Map<String, String> getWhitelabelSettings(String username) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.GET, urlBuilder.getWhitelabelSettingsUrl(username));		
		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseKeyValueMap(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	public boolean requestPasswordReset(String email, String returnUrl) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.reset() + "/request");
		request.addBodyParameter(EMAIL, email);
		request.addBodyParameter("returnUrl", returnUrl);

		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseString(getResponseBody(response)).equals(OK);
		}
		
		throw handleExceptions(response);
	}
	
	public boolean confirmResetPasswordToken(String email, String token) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.reset() + "/confirm");
		request.addBodyParameter(EMAIL, email);
		request.addBodyParameter(TOKEN, token);

		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseString(getResponseBody(response)).equals(OK);
		}
		
		throw handleExceptions(response);
	}
	
	public boolean resetPassword(String email, String token, String newPassword) throws N0ticeException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.reset() + "/action");
		request.addBodyParameter(EMAIL, email);
		request.addBodyParameter(TOKEN, token);
		request.addBodyParameter("newPassword", newPassword);

		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseString(getResponseBody(response)).equals(OK);
		}
		
		throw handleExceptions(response);
	}
	
	public Map<String, String> setWhitelabelSettings(String username, Map<String, String> settings, MediaFile header, MediaFile masthead) throws N0ticeException, JsonProcessingException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.getWhitelabelSettingsUrl(username));
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		addStringPart(entity, "settings", new ObjectMapper().writeValueAsString(settings));	// TODO should be a RESTful body POST
		
		if (header != null) {
			entity.addPart("headerImage", new ByteArrayBody(header.getData(), header.getFilename()));
		}
		if (masthead != null) {
			entity.addPart("mastheadImage", new ByteArrayBody(masthead.getData(), masthead.getFilename()));
		}
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);

		final Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return searchParser.parseKeyValueMap(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}
	
	private void populateUpdateFields(String body, String link, MediaFile image, VideoAttachment video, MultipartEntity entity) throws N0ticeException {
		if (body != null) {
			try {
				entity.addPart(BODY, new StringBody(body, Charset.forName(UTF_8)));
			} catch (UnsupportedEncodingException e) {
				throw new N0ticeException();
			}
		}
		if (link != null) {
			try {
				entity.addPart("link", new StringBody(link, Charset.forName(UTF_8)));
			} catch (UnsupportedEncodingException e) {
				throw new N0ticeException();
			}
		}
		if (image != null) {
			entity.addPart(IMAGE, new ByteArrayBody(image.getData(), image.getFilename()));
		}
		if (video != null) {
			entity.addPart("video", new InputStreamBody(video.getData(), video.getFilename()));			
		}
	}
	
	private Noticeboard postNewNoticeboard(String domain, String name,
			String description, boolean moderated, 
			Date endDate, Date embargoDate,
			Set<MediaType> supportedMediaTypes, String group, MediaFile cover, boolean featured, boolean isolated, GeoCodingResolution geoCodingResolution, List<String> tags)
			throws N0ticeException, MissingCredentialsExeception,
			ParsingException {
		OAuthRequest request = createOauthRequest(Verb.POST, urlBuilder.newNoticeboard());
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		if (domain != null) {
			addEntityPartParameter(entity, "domain", domain);
		}
		addEntityPartParameter(entity, NAME, name);
		addEntityPartParameter(entity, DESCRIPTION, description);
		addEntityPartParameter(entity, "moderated", Boolean.toString(moderated));
		addEntityPartParameter(entity, "featured", Boolean.toString(featured));
		addEntityPartParameter(entity, "isolated", Boolean.toString(isolated));
		addEntityPartParameter(entity, "geoCodingResolution", geoCodingResolution != null ? geoCodingResolution.toString() : null);
		
		if (endDate != null) {
			addEntityPartParameter(entity, "endDate", ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(endDate)));
		}
		if (embargoDate != null) {
			addEntityPartParameter(entity, "embargoDate", ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(embargoDate)));
		}
		if (cover != null) {
			entity.addPart("cover", new ByteArrayBody(cover.getData(), cover.getFilename()));
		}
		if (supportedMediaTypes != null) {
			addEntityPartParameter(entity, "supportedMediaTypes", formatSupportMediaTypes(supportedMediaTypes));
		}
		if (group != null) {
			addEntityPartParameter(entity, "group", group);
		}
		if (tags != null) {
			addEntityPartParameter(entity, "tags", formatTags(tags));
		}
		
		request.addHeader(CONTENT_TYPE, entity.getContentType().getValue());
		addMultipartEntity(request, entity);
		oauthSignRequest(request);
		
		Response response = request.send();
		
		if (response.getCode() == HTTP_OK) {
	    	return noticeboardParser.parseNoticeboardResult(getResponseBody(response));
		}
		
		throw handleExceptions(response);
	}

	private String formatSupportMediaTypes(Set<MediaType> supportedMediaTypes) {
		StringBuilder supportedMediaTypesValue = new StringBuilder();
		Iterator<MediaType> supportedMediaTypesIterator = supportedMediaTypes.iterator();
		while(supportedMediaTypesIterator.hasNext()) {
			supportedMediaTypesValue.append(supportedMediaTypesIterator.next());
			if (supportedMediaTypesIterator.hasNext()) {
				supportedMediaTypesValue.append(COMMA);
			}
		}
		return supportedMediaTypesValue.toString();
	}
	
	private String formatTags(List<String> tags) {
		StringBuilder supportedMediaTypesValue = new StringBuilder();
		Iterator<String> supportedMediaTypesIterator = tags.iterator();
		while(supportedMediaTypesIterator.hasNext()) {
			supportedMediaTypesValue.append(supportedMediaTypesIterator.next());
			if (supportedMediaTypesIterator.hasNext()) {
				supportedMediaTypesValue.append(COMMA);
			}
		}
		return supportedMediaTypesValue.toString();
	}
	
	private byte[] extractMultpartBytes(MultipartEntity entity) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		entity.writeTo(byteArrayOutputStream);			
		byte[] byteArray = byteArrayOutputStream.toByteArray();
		return byteArray;
	}
	
	private void addBodyParameter(OAuthRequest request, String parameter, String value) {
		if (value != null) {
			request.addBodyParameter(parameter, value);
		}
	}
	
	private void addEntityPartParameter(MultipartEntity entity, String parameter, String value) {
		if (value != null) {
			try {
				entity.addPart(parameter, new StringBody(value, Charset.forName(UTF_8)));
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void addMultipartEntity(OAuthRequest request, MultipartEntity entity) throws N0ticeException {
		try {
			request.addPayload(extractMultpartBytes(entity));
		} catch (IOException e) {			
			e.printStackTrace();
			throw new N0ticeException();
		}
	}
	
	private void oauthSignRequest(OAuthRequest request) throws MissingCredentialsExeception {
		if (scribeAccessToken != null) {
			service.signRequest(scribeAccessToken, request);
			return;
		}
		throw new MissingCredentialsExeception();
	}

	private void manuallySignRequest(String consumerSecret, OAuthRequest request) throws N0ticeException {
		// Manually sign this request using the consumer secret rather than the access key/access secret.
		addBodyParameter(request, "oauth_signature_method", "HMAC-SHA1");
		addBodyParameter(request, "oauth_version", "1.0");
		addBodyParameter(request, "oauth_timestamp", Long.toString(DateTimeUtils.currentTimeMillis()));
		final String effectiveUrl = request.getCompleteUrl() + "?" + request.getBodyContents();
		addBodyParameter(request, "oauth_signature", sign(effectiveUrl, consumerSecret));
	}
	
	private String sign(String effectiveUrl, String secret) throws N0ticeException {
	    SecretKeySpec key;
		try {
			key = new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(key);
			byte[] bytes = mac.doFinal(effectiveUrl.getBytes(UTF_8));
			return new String(Base64.encodeBase64(bytes)).replace("\r\n", "");
			
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			throw new N0ticeException();
		} catch (NoSuchAlgorithmException e) {
			log.error(e);
			throw new N0ticeException();
		} catch (InvalidKeyException e) {
			log.error(e);
			throw new N0ticeException();
		}
	}
	
	private void addStringPart(MultipartEntity entity, String parameter, String value) throws N0ticeException {
		try {
			entity.addPart(parameter, new StringBody(value, Charset.forName(UTF_8)));
		} catch (UnsupportedEncodingException e) {
			log.error(e);
			throw new N0ticeException();
		}
	}
    
    private OAuthRequest createOauthRequest(Verb verb, String url) {
        OAuthRequest request = new OAuthRequest(verb, url);
        request.setConnectTimeout(15, TimeUnit.SECONDS);
        request.setReadTimeout(30, TimeUnit.SECONDS);
        return request;
    }
        
    private String getResponseBody(Response response) {
    	InputStream inputStream = response.getStream();
    	if (inputStream == null) {
    		return null;
    	}
    	
    	try {
			final List<String> lines = IOUtils.readLines(inputStream);
			IOUtils.closeQuietly(inputStream);			
			return Joiner.on("\n").join(lines);

    	} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    private N0ticeException handleExceptions(Response response) {
		final String responseBody = getResponseBody(response);
		ObjectMapper objectMapper = new ObjectMapper();
		log.error("Exception during n0tice api call: " + response.getCode() + ", " + responseBody);
		if (response.getCode() == HTTP_NOT_FOUND) {
			return new NotFoundException("Not found");
		}
		try {
			if (response.getCode() == HTTP_FORBIDDEN) {
				return new NotAllowedException(responseBody != null ? objectMapper.readValue(responseBody, String.class) : "");
			}		
			if (response.getCode() == HTTP_UNAUTHORISED) {
				return new AuthorisationException(responseBody != null ? objectMapper.readValue(responseBody, String.class) : "");
			}
			if (response.getCode() == HTTP_BAD_REQUEST) {
				return new BadRequestException(responseBody != null ? objectMapper.readValue(responseBody, String.class) : "");
			}
			
		} catch (JsonParseException e) {
			return new N0ticeException(responseBody);
		} catch (JsonMappingException e) {
			return new N0ticeException(responseBody);
		} catch (IOException e) {
			return new N0ticeException(responseBody);
		}
		
		return new N0ticeException(responseBody);
	}
    
}

