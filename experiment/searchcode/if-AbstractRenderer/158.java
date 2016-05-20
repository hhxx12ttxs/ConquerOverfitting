package com.sfeir.wolfengine.server.servlet.renderer.freemarker;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sfeir.wolfengine.server.entity.blogengine.BlogPost;
import com.sfeir.wolfengine.server.entity.datamanagement.Tag;
import com.sfeir.wolfengine.server.entity.datamanagement.Comment;
import com.sfeir.wolfengine.server.servlet.model.PostModel;
import com.sfeir.wolfengine.server.servlet.renderer.Produces;
import com.sfeir.wolfengine.server.servlet.renderer.Renderer;
import com.sfeir.wolfengine.server.servlet.renderer.impl.AbstractRenderer;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.*;

@Produces(value = PostModel.class, format = "html")
public class FreemarkerRendererProvider extends AbstractRenderer<PostModel> implements Renderer<PostModel>
{

    private static Configuration config;

    static
    {
        // StringTemplateLoader loader = new StringTemplateLoader();
        // loader.putTemplate("main", "Hello ${user}");
        ClassTemplateLoader loader = new ClassTemplateLoader(FreemarkerRendererProvider.class, "/templates");

        config = new Configuration();
        config.setDefaultEncoding("utf-8");
        config.setTemplateLoader(loader);
    }

    private String tagUrl;

    @Override
    protected void renderInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        response.setCharacterEncoding("utf-8");
        PrintWriter pw = response.getWriter();
        pw.println("<html>");
        HashMap<String, Object> root = new HashMap<String, Object>();
        if (getModel().getSetupInfo() != null && getModel().getSetupInfo().getName() != null
                && !getModel().getSetupInfo().getName().equals(""))
            root.put("headertitle", getModel().getSetupInfo().getName());
        else
            root.put("headertitle", "BlogEngine : The enginist Blog");
        Template header = config.getTemplate("header.ftl");
        header.process(root, pw);
        pw.println("<body class=\"dc-home\"><div id=\"page\">");

        Template top = config.getTemplate("top.ftl");
        top.process(root, pw);

        pw.println("<div id=\"wrapper\"><div id=\"main\"><div id=\"content\">");

        renderContent(getModel(), pw);

        pw.println("</div></div><div id=\"sidebar\">");
        root.put("post", getModel());
        root.put("buildTags", new BuildTagsMethod());
        Template sidebar = config.getTemplate("sidebar.ftl");
        sidebar.process(root, pw);
        pw.println("</div></div>");

        Template footer = config.getTemplate("footer.ftl");
        footer.setEncoding("utf-8");
        footer.process(new SimpleHash(), pw);
        pw.println("</div></body></html>");
    }

    private void renderContent(PostModel model, PrintWriter pw) throws IOException, TemplateException
    {
        if (model.getViewMode().equals(PostModel.PostViewModeEnum.LIST_MODE)
                || model.getViewMode().equals(PostModel.PostViewModeEnum.CATEGORY)
                || model.getViewMode().equals(PostModel.PostViewModeEnum.TAG))
        {

            // Add the content of the posts list to the string Builder
            writeListOfPosts(model.getPosts(), pw);

            // Add the pager according to the number of
            // posts
            writePagination(model, pw);

        } else if (model.getViewMode().equals(PostModel.PostViewModeEnum.DETAILED_MODE))
        {
            // Get the post and add its html representation
            BlogPost blogPostToDisplay = model.getPosts().get(0);
            writeDetailedPost(blogPostToDisplay, pw, false, true);
        } else if (model.getViewMode().equals(PostModel.PostViewModeEnum.TAGS))
        {
            writeTagsCloud(model, pw);
        } else
            throw new IllegalArgumentException("Bad view mode : " + model.getViewMode());
    }

    private void writePagination(PostModel model, PrintWriter pw)
    {
        // Ecriture de la pagination
        pw.append("<p class=\"pagination\">");
        if (!model.isOnLastPage())
        {
            pw.append("<a href=\"" + request.getRequestURI() + "?p=");
            pw.print(model.getActualPage() + 1);
            pw.append("\" class=\"prev\">&#171; prev</a> - ");
        }
        pw.append("page " + model.getActualPage() + " de ");
        pw.print(model.getNbPages());
        if (!model.isOnFirstPage())
        {
            pw.append(" - <a href=\"" + request.getRequestURI() + "?p=");
            pw.print(model.getActualPage() - 1);
            pw.append("\" class=\"next\">next &#187;</a>");
        }
        pw.append("</p>");
    }

    private void writeTagsCloud(PostModel model, PrintWriter pw)
    {
        List<Tag> tags = model.getTags();
        if (tags.size() > 5)
            tags = tags.subList(0, 5);
        long max = 100;
        if (tags != null && !tags.isEmpty() && tags.get(0).getOccurency() != null)
            max = tags.get(0).getOccurency();

        Collections.shuffle(tags, new Random());
        // Template tagcloud = config.getTemplate("tagcloud.ftl");
        // Map<String, Object> rootMap = new HashMap<String, Object>();
        // tagcloud.process(rootMap, pw);
        pw.append("<div id=\"content-info\">");
        pw.append("<h2>Tags</h2>");
        pw.append("</div>");
        pw.append("<div class=\"content-inner\">\n" + "\t\n\t<ul class=\"tags\">\n");
        Long tagOccurency = 0l;
        for (Tag tag : tags)
        {
            if (tag.getOccurency() != null)
                tagOccurency = ((tag.getOccurency().intValue() * 100) / max);
            else
                tagOccurency = 0l;
            pw.append("<li><a href=\"" + tagUrl + "/" + tag.getName() + "\" class=\"tag" + getTagRate(tagOccurency)
                    + "\">" + tag.getName() + "</a></li>");
        }
        pw.append("</ul>");
        pw.append("</div>");
    }

    private String getTagRate(Long tagOccurency)
    {
        String occurencyString = tagOccurency.toString().charAt(0) + "";
        if (tagOccurency < 10 && tagOccurency > 0)
            return "10";
        for (int i = 0; i < tagOccurency.toString().length() - 1; i++)
            occurencyString += 0;
        return occurencyString;
    }

    private void writeListOfPosts(List<BlogPost> liste, PrintWriter pw) throws IOException, TemplateException
    {
        for (BlogPost blogPost : liste)
        {
            writeDetailedPost(blogPost, pw, true, false);
        }
    }

    // TODO categoryURL
    private void writeDetailedPost(BlogPost result, PrintWriter pw, boolean headerView, boolean first)
            throws IOException, TemplateException
    {

        HashMap<String, Object> root = new HashMap<String, Object>();
        if (first)
            root.put("class", "post odd first");
        else
            root.put("class", "post odd ");
        if (result.getCreationDate() != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEEEE dd MMMM yyyy");
            SimpleDateFormat sdfWithHour = new SimpleDateFormat("EEEEEE dd MMMM yyyy hh:mm");
            root.put("date", sdf.format(result.getCreationDate()));
            root.put("datehours", sdfWithHour.format(result.getCreationDate()));
        } else
        {
            root.put("date", "Unknown date");
            root.put("datehours", "Unknown date");
        }
        // TODO no authors
        if (result.getAuthors() != null && !result.getAuthors().isEmpty())
            root.put("author", result.getAuthors().get(0).getLogin());

        String content = null;
        if (headerView)
            content = result.getHeader();
        if (content == null || "".equals(content.trim()))
            content = result.getContent();
        if (content == null)
            content = "No content";
        root.put("content", content);

        root.put("post", result);
        Template template = config.getTemplate("postdetail.ftl");
        template.process(root, pw);

        if (headerView)
        {
            if (result.getComments() != null && !result.getComments().isEmpty())
            {
                if (result.getComments().size() == 1)
                {
                    pw.append("<a href=\"/post/");
                    pw.append(result.getUrl());
                    pw
                            .append("#comments\" class=\"comment_count\">1 comment</a>");
                } else
                {
                    pw.append("<a href=\"/post/");
                    pw.append(result.getUrl());
                    pw
                            .append("#comments\" class=\"comment_count\">");
                    pw.append(result.getComments().size()
                            + " comments</a>");
                }
            } else
            {
                pw.append("<a href=\"/post/");
                pw.append(result.getUrl());
                pw
                        .append("#comments\" class=\"comment_count\">no comment</a>");
            }
//            pw.append("</p>\n\t</div>");
//            pw.append("\n\t</div>");
        } else
        {
//            pw.append("</p>\n\t</div>");
//            pw.append("\n\t</div>");
            // On affiche la liste des commentaires
            if (result.getComments() != null && !result.getComments().isEmpty())
            {
                writePostComments(result.getComments(), pw);
            }

            // On affiche le formulaire permettant de poster un commentaire
            writeCommentsForm(result, pw);
        }
    }

    private void writeCommentsForm(BlogPost blogPost, PrintWriter pw) throws IOException, TemplateException
    {
        Boolean areCommentsAllowed = true;
        PostModel associatedModel = getModel();
        if (associatedModel != null && associatedModel.getSetupInfo() != null && associatedModel.getSetupInfo().getCommentsAllowedTime() != null)
        {
            areCommentsAllowed = blogPost.areCommentsAllowed(associatedModel.getSetupInfo().getCommentsAllowedTime());
        } else
        {
            areCommentsAllowed = blogPost.areCommentsAllowed(null);
        }
        if (areCommentsAllowed)
        {
            User user = UserServiceFactory.getUserService().getCurrentUser();
            SimpleHash rootMap = new SimpleHash();
            rootMap.put("user", user);
            rootMap.put("post", blogPost);
            Template commentform = config.getTemplate("commentform.ftl");
            commentform.process(rootMap, pw);
        }
    }

    private void writePostComments(List<Comment> comments, PrintWriter pw) throws IOException, TemplateException
    {

        SimpleHash rootMap = new SimpleHash();
        rootMap.put("comments", comments);
        rootMap.put("replaceLinks", new ReplaceLinksMethod());
        Template commentlist = config.getTemplate("commentlist.ftl");
        commentlist.process(rootMap, pw);
//		stringBuilder.append("<div id=\"comments\">");
//		stringBuilder.append("<h3>Commentaires</h3><dl>");
//		Integer commentNumber = 1;
//		for (Comment comment : comments) {
//			stringBuilder.append("<dt id=\"" + comment.getId()
//					+ "\" class=\" odd first\">");
//			stringBuilder.append("<a href=\"#" + comment.getId()
//					+ "\" class=\"comment-number\">" + commentNumber + ".</a>");
//			stringBuilder.append(sdfWithHour.format(comment.getDate())
//					+ " par ");
//			String href = comment.getHref();
//			if (href != null && !href.isEmpty()) {
//			    if (href.startsWith("http://"))
//			        href = "http://" + href;
//			    stringBuilder.append("<a href=\"" + href
//					+ "\" rel=\"nofollow\">" + comment.getPseudo()
//					+ "</a>");
//			}
//			else {
//			    stringBuilder.append(comment.getPseudo());
//			}
//			stringBuilder.append("</dt><dd class=\" odd first\">");
//			stringBuilder.append(replaceLinks(comment.getComment().getValue()));
//			stringBuilder.append("</dd>");
//			commentNumber++;
//		}
//		stringBuilder.append("</dl></div>");
    }

    public class BuildTagsMethod implements TemplateMethodModelEx
    {

        private String tagUrl = "/tag";

        @Override
        public TemplateModel exec(List args) throws TemplateModelException
        {
            if (1 != args.size())
                throw new TemplateModelException("I want my tags !");
            List<Tag> tags = ((SimpleSequence) args.get(0)).toList();
            long max = 100;
            if (tags != null && !tags.isEmpty() && tags.get(0).getOccurency() != null)
                max = tags.get(0).getOccurency();

            Long tagOccurency = 0l;
            Integer compteur = 0;
            String result = "";
            ListIterator<Tag> iterator = tags.listIterator();
            while (iterator.hasNext() && compteur < 20)
            {
                Tag tag = iterator.next();
                compteur++;
                if (tag.getOccurency() != null)
                    tagOccurency = ((tag.getOccurency().intValue() * 100) / max);
                else
                    tagOccurency = 0l;
                result += "\t\t<li><a href=\"" + tagUrl + "/" + tag.getName() + "\" class=\"tag"
                        + getTagRate(tagOccurency) + "\">" + tag.getName() + "</a> </li>\n";
            }
            return new SimpleScalar(result);
        }

        private String getTagRate(Long tagOccurency)
        {
            String occurencyString = tagOccurency.toString().charAt(0) + "";
            if (tagOccurency < 10 && tagOccurency > 0)
                return "10";
            for (int i = 0; i < tagOccurency.toString().length() - 1; i++)
                occurencyString += 0;
            return occurencyString;
        }
    }

    public class ReplaceLinksMethod implements TemplateMethodModel
    {
        @Override
        public String exec(List args) throws TemplateModelException
        {
            if (1 != args.size())
                throw new TemplateModelException("I want my tags !");
            String text = (String) args.get(0);
            return text
                    .replaceAll("(http://\\S*)", "<a href=\"$1\" rel=\"nofollow\">$1</a>")
                    .replaceAll("\\n", "<br/>")
                    .replaceAll("(\\S*)@(\\S*)", "<a href=\"#\" onclick=\"window.location=[\'$2\',\'@\',\'$1\',\'://\',\'to\', \'mail\'].reverse().join(\'\'); return false;\" rel=\"nofollow\">$1[&#64;]$2</a>");
        }
    }


}

