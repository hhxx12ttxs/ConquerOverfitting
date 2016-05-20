package com.tunelib.client.managed.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;

import com.tunelib.client.scaffold.place.AbstractProxyListView;
import com.tunelib.client.managed.request.ArtistProxy;


import java.util.HashSet;
import java.util.Set;

/**
 * {@link AbstractProxyListView} specialized to {@link ArtistKey}} values.
 */
public class ArtistListView extends AbstractProxyListView<ArtistProxy> {
	interface Binder extends UiBinder<HTMLPanel, ArtistListView> {
	}

	private static final Binder BINDER = GWT.create(Binder.class);
  
  private static ArtistListView instance;

	@UiField CellTable<ArtistProxy> table;
	@UiField Button newButton;
  
  private Set<String> paths = new HashSet<String>();

  public static ArtistListView instance() {
    if (instance == null) {
      instance = new ArtistListView();
    }
    
    return instance;
  }
  
	public ArtistListView() {
		init(BINDER.createAndBindUi(this), table, newButton);
		table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
    
    paths.add("id");
    table.addColumn(new TextColumn<ArtistProxy>() {
      Renderer<java.lang.Long> renderer = new AbstractRenderer<java.lang.Long>() {
        public String render(java.lang.Long obj) {
          return obj == null ? "" : String.valueOf(obj);
        }
      };
      
      @Override
      public String getValue(ArtistProxy object) {
        return renderer.render(object.getId());
      }
    }, "Id");
    paths.add("version");
    table.addColumn(new TextColumn<ArtistProxy>() {
      Renderer<java.lang.Integer> renderer = new AbstractRenderer<java.lang.Integer>() {
        public String render(java.lang.Integer obj) {
          return obj == null ? "" : String.valueOf(obj);
        }
      };
      
      @Override
      public String getValue(ArtistProxy object) {
        return renderer.render(object.getVersion());
      }
    }, "Version");
    paths.add("name");
    table.addColumn(new TextColumn<ArtistProxy>() {
      Renderer<java.lang.String> renderer = new AbstractRenderer<java.lang.String>() {
        public String render(java.lang.String obj) {
          return obj == null ? "" : String.valueOf(obj);
        }
      };
      
      @Override
      public String getValue(ArtistProxy object) {
        return renderer.render(object.getName());
      }
    }, "Name");
 	}

  public String[] getPaths() {
    return paths.toArray(new String[paths.size()]);
  }
}

