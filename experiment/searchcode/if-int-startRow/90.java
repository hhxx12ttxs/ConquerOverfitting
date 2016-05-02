package com.netfever.site.dynovisz.web.spring.data;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

public class SolrQuery {
	public enum SortedBy {
		VIDEO_DATE,
		NUMBER_OF_VIEW;
	}
	
	private static final int NUMBER_PER_ROW = 12;
	private static final Preferences PREF_SOLR_NODE = Preferences.systemRoot().node("com/netfever/dynovisz/solr"); 
	private static final String QUERY_FMT = PREF_SOLR_NODE.get("query", "NO_QUERY_LOADED_CHECK_PREFERENCES_FILES");
	private static final String QUERY_TOKEN = "${QUERY}";
	private static final String SORTBY_TOKEN = "${SORTBY}";
	private static final String START_TOKEN = "${START}";
	private static final String NUMBER_PER_ROW_TOKEN = "${NUMBER_PER_ROW}";
	
	private final SimpleDateFormat solrDateFmt = new SimpleDateFormat("yyyy-MM-dd"); 
	
	private String query;
	private boolean withcomments; 
	private String category; 
	private String language;
	private Date startDate; 
	private Date endDate;
	private SortedBy sortedBy;
	private int startRow;
	private int rowCount = NUMBER_PER_ROW;
	
	private String quotedString(final String value) {
		return "\"" + value + "\"";
	}
	
	private static final String encodeUrl(final String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (this.withcomments) {
			sb.append("withcomments:");
		} else {
			sb.append("withoutcomments:");			
		}
			
		if (query.length() == 0) {
			sb.append("*");
		} else {
			sb.append(quotedString(query));
		}
		
		// category
		if (category != null) {
			sb.append(String.format(" AND categories:%s", quotedString(category)));
		}
		
		// language
		if (language != null) {
			sb.append(String.format(" AND language:%s", language));
		}
		
		// videodate
		if (startDate != null) {
			sb.append(
				String.format(
					" AND videodate:[%sT00:00:00Z TO %sT00:00:00Z]", 
					solrDateFmt.format(startDate), 
					solrDateFmt.format(endDate)));
		}
		
		String res = QUERY_FMT.replace(QUERY_TOKEN, "(" + encodeUrl(sb.toString()) + ")");
		
		// sorting
		if (sortedBy == SortedBy.VIDEO_DATE) {
			res = res.replace(SORTBY_TOKEN, "videodate");			
		} else {
			res = res.replace(SORTBY_TOKEN, "numberOfView");						
		}
		
		res = res.replace(START_TOKEN, Integer.toString(startRow));
		
		res = res.replace(NUMBER_PER_ROW_TOKEN, Integer.toString(NUMBER_PER_ROW));
		
		res = PREF_SOLR_NODE.get("rooturl", "") + res;
		
		return res;
	}

	public String getQuery() {
		return this.query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public boolean isWithcomments() {
		return this.withcomments;
	}

	public void setWithcomments(boolean withcomments) {
		this.withcomments = withcomments;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public SortedBy getSortedBy() {
		return this.sortedBy;
	}

	public void setSortedBy(SortedBy sortedBy) {
		this.sortedBy = sortedBy;
	}

	public int getStartRow() {
		return this.startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public int getRowCount() {
		return this.rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	
	public int getPage() {
		return (this.startRow / this.rowCount) + 1;
	}

	public void setPage(int page) {
		this.startRow = (page - 1) * this.rowCount;
	}
}

