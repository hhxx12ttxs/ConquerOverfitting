package de.nielsjaeckel.confluence.plugins.universalnavigation.renderer;


import de.nielsjaeckel.confluence.plugins.universalnavigation.api.*;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InspectRenderer extends AbstractRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(InspectRenderer.class);

    private Navigation navigation;

    private static final List<String> internalProperties = new ArrayList<String>();
    static {

        internalProperties.add("childNodes");
        internalProperties.add("class");
        internalProperties.add("nodeType");
        internalProperties.add("parentNode");
    }

    @Override
    public CharSequence renderNavigation(Navigation navigation, NodeContext nodeContext, String style) {

        this.navigation = navigation;

        StringBuffer out = new StringBuffer();
        out.append(String.format("<div class=\"universal-navigation navigation-%s renderer-inspect\">", navigation.getId()));
        out.append(renderChildNodes(navigation.getRootNode()));
        out.append("</div>");

        return out;
    }

    public CharSequence renderChildNodes(NavigationNode node) {

        StringBuffer out = new StringBuffer();
        List<NavigationNode> childNodes = node.getChildNodes();
        if (childNodes.size() > 0) {

            out.append("<ul>");

            // render all nodes
            for (NavigationNode childNode : childNodes) {

                out.append(String.format("<li class=\"type-%s\">", childNode.getNodeType().getId()));
                out.append(String.format("<span class=\"type\" title=\"%s\">%s:</span> ", childNode.getClass().getName(), childNode.getNodeType().getId()));

                try {
                    Map<String, String> properties = BeanUtils.describe(childNode);
                    out.append("[");

                    boolean first = true;
                    for (Map.Entry entry : properties.entrySet()) {

                        // do not render internal properties
                        if (internalProperties.contains(entry.getKey())) {
                            continue;
                        }

                        if (first) {
                            first = false;
                        }
                        else {
                            out.append(", ");
                        }

                        out.append(entry.getKey());
                        out.append("=");
                        out.append(entry.getValue()); // getValue() may return null
                    }

                    out.append("]");

                } catch (Exception e) {

                    out.append("<span class=\"error\">ERROR: " + e.getMessage() + "</span>");
                    LOG.warn("Error while accessing bean properties of navigation node of type " + childNode.getClass().getName(), e);
                }


                // recursion (depth-first)
                out.append(renderChildNodes(childNode));
                out.append("</li>");
            }

            out.append("</ul>");
        }

        return out;
    }

    public void setApplicationIntegrationService(ApplicationIntegrationService applicationIntegrationService) {
        // not needed here
    }
}
