package com.sfeir.wolfengine.server.servlet.renderer.rome;

import com.sfeir.wolfengine.server.servlet.renderer.impl.AbstractRenderer;
import com.sfeir.wolfengine.server.servlet.renderer.Produces;
import com.sfeir.wolfengine.server.servlet.model.PostModel;
import com.sfeir.wolfengine.server.entity.blogengine.BlogPost;
import com.sfeir.wolfengine.server.entity.datamanagement.Link;
import com.sun.syndication.feed.synd.*;
//import com.sun.syndication.feed.module.base.CustomTag;
//import com.sun.syndication.feed.module.base.CustomTagImpl;
import com.sun.syndication.io.SyndFeedOutput;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * User: cappelle.f
 * Date: 2 juil. 2009
 * Time: 15:21:49
 */
@Produces(value = PostModel.class, format = "rss2")
public class RomeRssRendererProvider extends AbstractRenderer<PostModel>
{

    public static final String RSS_TITLE = "WolfEngine";
    public static final String RSS_DESC = "Feed created by WolfEngine and propulsed by Google App Engine";
    private static final String MIME_TYPE = "application/xml; charset=UTF-8";

    @Override
    protected void renderInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        response.setCharacterEncoding("utf-8");
        response.setContentType(MIME_TYPE);
        response.addHeader("Content-Disposition", "inline; filename=wolfengine.xml");
        try
        {
//            String feedAsString = null;
            String hostUrl = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf(request.getRequestURI()));
//			feedAsString = getCachedFeed(request);
            SyndFeed feed = getFeed(hostUrl);
//			if (feedAsString == null){
            feed.setFeedType("rss_2.0");
//				RSSFeedWriter writer = new RSSFeedWriter();
//				writer.setRssfeed(feed);
//				writer.write(hostUrl);
//				feedAsString = writer.serialize();
            SyndFeedOutput sfo = new SyndFeedOutput();
            sfo.output(feed, response.getWriter());
//				cachedFeed(request, feedAsString);
//			}
//            response.getWriter().write(feedAsString);
        } catch (Exception e)
        {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

    private SyndFeed getFeed(String link)
    {
        SyndFeed feed = new SyndFeedImpl();
        feed.setTitle(RSS_TITLE);
        feed.setLink(link);
        feed.setDescription(RSS_DESC);
        feed.setLanguage("en");
//        feed.set
        feed.setEntries(getSyndEntriesRepresentation(link));
        return feed;
    }

    // TODO : implement a Rome Module for Â?commentsÂ? element in each entry.
    protected List<SyndEntry> getSyndEntriesRepresentation(String link)
    {
        List<SyndEntry> entries = new ArrayList<SyndEntry>();
        for (BlogPost blogPost : this.getModel().getPosts())
        {
            SyndEntry entry = new SyndEntryImpl();
            String guid = link + "/post/" + blogPost.getUrl();
            if (blogPost.getTitle() != null)
                entry.setTitle(blogPost.getTitle());
            else
                entry.setTitle("Untitled post");
            entry.setLink(guid);
//            CommentAPIModule commentModule = new CommentAPIModuleImpl();
//            commentModule.setComment(guid + "/comment");
//            entry.setModules(Arrays.asList(commentModule));
            if (blogPost.getAuthors() != null && !blogPost.getAuthors().isEmpty())
                entry.setAuthor(blogPost.getAuthors().get(0).getLogin());
            else
                entry.setAuthor("Unknown author");
//			entry.setGuid(guid);
            // add links to the contents
//			String content = blogPost.getContent() + linksToString(blogPost.getLinks());
            SyndContent content = new SyndContentImpl();
            content.setType("text/html");
            content.setValue(blogPost.getContent() + linksToString(blogPost.getLinks()));
            entry.setDescription(content);
            entry.setPublishedDate(blogPost.getCreationDate());
//			entry.setPubDate(blogPost.getCreationDate());
//			entry.setCommentUrl(guid+"#comments");

            entries.add(entry);
        }
        return entries;
    }

    private String linksToString(List<Link> list)
    {
        StringBuilder result = new StringBuilder();
        result.append("<div><br></div>");
        for (Link link : list)
        {
            result.append("<div><a href=\"/post/link/" + link.getIdAsString()
                    + "\">" + link.getLabel()
                    + "</a></div>");
        }
        return result.toString();
    }
}

