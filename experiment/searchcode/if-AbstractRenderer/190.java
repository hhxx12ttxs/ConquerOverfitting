package de.nielsjaeckel.confluence.plugins.universalnavigation.renderer;

import com.atlassian.confluence.util.velocity.VelocityUtils;
import de.nielsjaeckel.confluence.plugins.universalnavigation.api.*;
import de.nielsjaeckel.confluence.plugins.universalnavigation.service.NavigationVisibilityHelper;
import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HtmlListRenderer extends AbstractRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(HtmlListRenderer.class);
    public static final String RENDERER_TEMPLATE = "templates/renderer/htmllistrenderer.vm";

    private Navigation navigation;
    private ApplicationIntegrationService applicationIntegrationService;

    @Override
    public CharSequence renderNavigation(Navigation navigation, NodeContext nodeContext, String style) {

        this.navigation = navigation;

        // sanitize style
        if (style == null) {
            style = "";
        }

        // remove invisible nodes
        NavigationVisibilityHelper nvh = new NavigationVisibilityHelper(nodeContext);
        nvh.removeInvisibleNodes(navigation);

        // render template
        VelocityContext velocityContext = getVelocityContext(navigation.getRootNode());
        velocityContext.put("style", style);
        String out = VelocityUtils.getRenderedTemplate(RENDERER_TEMPLATE, velocityContext);

        return out;
    }

    public CharSequence renderChildNodes(NavigationNode node) {

        StringBuffer out = new StringBuffer();


        List<NavigationNode> childNodes = node.getChildNodes();

        if (childNodes.size() > 0) {

            out.append("<ul>");

            // render all nodes
            for (NavigationNode childNode : childNodes) {

                out.append(getRenderedNode(childNode));

                // no recursion here
                // the called template must recurse itself by calling renderer.renderChildNodes(navigation, node)
            }

            out.append("</ul>");
        }

        return out;
    }

    private CharSequence getRenderedNode(NavigationNode node) {

        CharSequence output = "";
        try {
            // get the template
            String template = node.getNodeType().getRenderTemplate();

            if (template != null) {

                // render template
                VelocityContext velocityContext = getVelocityContext(node);
                output = VelocityUtils.getRenderedContent(template, velocityContext);
            }
        }
        catch (Exception e) {

            LOG.error(String.format("Could not render node of type %s in navigation with id %s.", node.getNodeType().getId(), navigation.getId()), e);
            output = renderNodeError(node);
        }

        return output;
    }

    private VelocityContext getVelocityContext(NavigationNode node) {

        VelocityContext velocityContext = applicationIntegrationService.getDefaultVelocityContext();
        velocityContext.put("navigation", navigation);
        velocityContext.put("node", node);
        velocityContext.put("renderer", this);

        return velocityContext;
    }

    private CharSequence renderNodeError(NavigationNode node) {

        return String.format("Error rendering node of type %s.", node.getNodeType().getId());
    }

    public void setApplicationIntegrationService(ApplicationIntegrationService applicationIntegrationService) {
        this.applicationIntegrationService = applicationIntegrationService;
    }
}
