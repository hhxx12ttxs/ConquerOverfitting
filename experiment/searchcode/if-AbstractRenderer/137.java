package com.tunelib.client.managed.ui;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.Renderer;

import com.tunelib.client.managed.request.ArtistProxy;
import com.tunelib.client.scaffold.ui.MobileProxyListView;
import com.tunelib.client.scaffold.ScaffoldMobileApp;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link MobileProxyListView} specialized to {@link ArtistKey} values.
 */
public class ArtistMobileListView extends MobileProxyListView<ArtistProxy> {

	/**
	 * The renderer used to render cells.
	 */
	private static class CellRenderer extends
			AbstractSafeHtmlRenderer<ArtistProxy> {
		private final String dateStyle = ScaffoldMobileApp.getMobileListResources().cellListStyle().dateProp();
		private final String secondaryStyle = ScaffoldMobileApp.getMobileListResources().cellListStyle().secondaryProp();
		
		private final Renderer<java.lang.String> primaryRenderer = new AbstractRenderer<java.lang.String>() {
        public String render(java.lang.String obj) {
          return obj == null ? "" : String.valueOf(obj);
        }
      };
		private final Renderer<java.lang.Long> secondaryRenderer = new AbstractRenderer<java.lang.Long>() {
        public String render(java.lang.Long obj) {
          return obj == null ? "" : String.valueOf(obj);
        }
      };

		@Override
		public SafeHtml render(ArtistProxy value) {
			if (value == null) {
				return SafeHtmlUtils.EMPTY_SAFE_HTML;
			}

			// Primary property.
			SafeHtmlBuilder sb = new SafeHtmlBuilder();
			if (value.getName() != null) {
				sb.appendEscaped(primaryRenderer.render(value.getName()));
			}

			// Secondary property.
			sb.appendHtmlConstant("<div style=\"position:relative;\">");
			sb.appendHtmlConstant("<div class=\"" + secondaryStyle + "\">");
			if (value.getId() != null) {
				sb.appendEscaped(secondaryRenderer.render(value.getId()));
			}
			sb.appendHtmlConstant("</div>");

			// Date property.
			sb.appendHtmlConstant("<div class=\"" + dateStyle + "\">");
			
			sb.appendHtmlConstant("</div>");
			sb.appendHtmlConstant("</div>");

			return sb.toSafeHtml();
		}
	}

	private static ArtistMobileListView instance;

	private final Set<String> paths = new HashSet<String>();

	public static ArtistMobileListView instance() {
		if (instance == null) {
			instance = new ArtistMobileListView();
		}

		return instance;
	}

	public ArtistMobileListView() {
		super("New Artist", new CellRenderer());
		
		paths.add("name");
		paths.add("id");
	}

	public String[] getPaths() {
		return paths.toArray(new String[paths.size()]);
	}
}

