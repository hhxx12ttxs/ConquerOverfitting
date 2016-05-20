/**
 * Copyright 2011 FeedDreamwork SIG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.feeddreamwork.feed;

import java.util.*;

import org.feeddreamwork.*;
import org.w3c.dom.*;

public class Rss20FeedBuilder extends FeedBuilder {
	public static final String TARGET_TYPE = "RSS 2.0";
	
	public Rss20FeedBuilder(Feed source, String feedId) {
		super(source, feedId);
	}

	@Override
	public String addUpdatedTime(String source, Date updatedTime) {
		Utils.throwIfNullOrEmpty(source);
		Utils.throwIfNull(updatedTime);
		return source.replace(FeedConstant.UPDATED_TIME_PLACEHOLDER,
				DateUtils.formatDateAsRFC2822(updatedTime));
	}

	@Override
	public String getMIMEType() {
		return "application/rss+xml";
	}

	@Override
	protected Element buildRoot() {
		Element rss = this.feedDocument.createElement("rss");
		rss.setAttribute("version", "2.0");
		rss.setAttribute("xmlns:atom", FeedConstant.ATOM_10_NAMESPACE);
		this.feedDocument.appendChild(rss);

		Element channel = this.feedDocument.createElement("channel");
		rss.appendChild(channel);
		return channel;
	}

	@Override
	protected void buildMetadata(Element root) {
		if (!Utils.isNullOrEmpty(this.source.getHubLink()))
			appendLinkElement(root, "hub", this.source.getHubLink());
		appendLinkElement(root, "self", this.getFeedAddress());
		XMLUtils.appendSimpleElement(root, "title", this.source.getTitle());
		XMLUtils.appendSimpleElement(root, "description",
				this.source.getDescription());
		XMLUtils.appendSimpleElement(root, "link", this.source.getLink());
		if (!Utils.isNullOrEmpty(this.source.getImage())) {
			Element imageNode = this.feedDocument.createElement("image");
			XMLUtils.appendSimpleElement(imageNode, "title",
					this.source.getTitle());
			XMLUtils.appendSimpleElement(imageNode, "link",
					this.source.getLink());
			XMLUtils.appendSimpleElement(imageNode, "url",
					this.source.getImage());
			root.appendChild(imageNode);
		}
		if (!Utils.isNullOrEmpty(source.getLanguage()))
			XMLUtils.appendSimpleElement(root, "language",
					this.source.getLanguage());

		XMLUtils.appendSimpleElement(root, "lastBuildDate",
				FeedConstant.UPDATED_TIME_PLACEHOLDER);
		XMLUtils.appendSimpleElement(root, "generator", this.getGeneratorInfo());
	}

	@Override
	protected Element buildEntry(Element root, Entry entry) {
		Element entryNode = this.feedDocument.createElement("item");
		root.appendChild(entryNode);

		XMLUtils.appendSimpleElement(entryNode, "title", entry.getTitle());
		XMLUtils.appendSimpleElement(entryNode, "link", entry.getLink());
		XMLUtils.appendSimpleElement(entryNode, "description",
				entry.getContent());
		XMLUtils.appendSimpleElement(entryNode, "author", entry.getAuthor());
		XMLUtils.appendSimpleElement(entryNode, "pubDate",
				DateUtils.formatDateAsRFC2822(entry.getUpdatedTime()));

		XMLUtils.appendSimpleElement(entryNode, "guid", entry.getLink());
		return entryNode;
	}

	private static Element appendLinkElement(Element node, String rel,
			String href) {
		Element result = XMLUtils.appendSimpleElement(node, "atom:link", null);
		result.setAttribute("rel", rel);
		result.setAttribute("href", href);
		return result;
	}
}

