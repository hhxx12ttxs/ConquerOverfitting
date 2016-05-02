package com.multunus;


public class FeedItem {

	private String title;
	private String content;
	private String contentURL;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContentURL() {
		return contentURL;
	}
	public void setContentURL(String link) {
		this.contentURL = link;
	}
	public FeedItem copy() {
		FeedItem feedItem = new FeedItem();
		feedItem.setTitle(title);
		feedItem.setContent(content);
		feedItem.setContentURL(contentURL);
		return feedItem;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Title: ");
		sb.append(title);
		sb.append('\n');
		sb.append("Link: ");
		sb.append(contentURL);
		sb.append('\n');
		sb.append("Content: ");
		sb.append(content);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((contentURL == null) ? 0 : contentURL.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeedItem other = (FeedItem) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (contentURL == null) {
			if (other.contentURL != null)
				return false;
		} else if (!contentURL.equals(other.contentURL))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
}

