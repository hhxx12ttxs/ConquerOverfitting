package com.myvdm.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.myvdm.client.request.DocumentProxy;
import com.myvdm.client.request.DocumentTypeProxy;
import com.myvdm.client.request.FieldProxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author razvan
 */
public class DocumentListView extends AbstractProxyListView<DocumentProxy> {

    interface Binder extends UiBinder<HTMLPanel, DocumentListView> {
    }

    private static final Binder BINDER = GWT.create(Binder.class);

    private static DocumentListView instance;

    protected Set<String> paths = new HashSet<String>();

    @UiField
    CellTable<DocumentProxy> table;

    @UiField
    Button newButton;
    
    // TODO why do you have to qualify name for the 2nd type parameter?
    @UiField(provided = true)
    ValueListBox<DocumentTypeProxy> type = new ValueListBox<DocumentTypeProxy>(DocumentTypeProxyRenderer.instance(),
            new com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider<DocumentTypeProxy>());

    public DocumentListView() {
        super.init(BINDER.createAndBindUi(this), table, newButton);
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        init();
    }

    public void init() {

        type.addValueChangeHandler(new ValueChangeHandler<DocumentTypeProxy>() {

            @Override
            public void onValueChange(ValueChangeEvent<DocumentTypeProxy> event) {
                getDelegate().typeChanged(event.getValue());
            }
        });
                
        
        paths.add("name");
        table.addColumn(new TextColumn<DocumentProxy>() {
            Renderer<String> renderer = new AbstractRenderer<java.lang.String>() {
                @Override
                public String render(String obj) {
                    return obj == null ? "" : String.valueOf(obj);
                }
            };

            @Override
            public String getValue(DocumentProxy object) {
                return renderer.render(object.getName());
            }
            // TODO i18n    
        }, "Nume");

        paths.add("type");
        table.addColumn(new TextColumn<DocumentProxy>() {
            Renderer<String> renderer = new AbstractRenderer<java.lang.String>() {
                @Override
                public String render(String obj) {
                    return obj == null ? "" : String.valueOf(obj);
                }
            };

            @Override
            public String getValue(DocumentProxy proxy) {
                // TODO make an abstraction
                // TODO throws null since I do not bind the type on the client
                // create a generic 'null' object :) or see Guava
                return renderer.render(proxy.getType() == null ? "NULL" : String.valueOf(proxy.getType().getName()));
            }
            // TODO i18n
        }, "Tip");

        paths.add("fields");
        table.addColumn(new TextColumn<DocumentProxy>() {
            // TODO
            // CollectionRenderer<FieldProxy, FieldProxyRenderer, Set<FieldProxy>> renderer;
            Renderer<List<FieldProxy>> renderer = CollectionRenderer.of(FieldProxyRenderer.instance());

            @Override
            public String getValue(DocumentProxy proxy) {
                return renderer.render(proxy.getFields());
            }
        }, "Fields");

    }

    @Override
    public void setDocumentTypePickerValues(Collection<DocumentTypeProxy> values) {
        type.setAcceptableValues(values);
    }

    @Override
    public String[] getPaths() {
        return paths.toArray(new String[paths.size()]);
    }

    public static DocumentListView instance() {
        if (instance == null) {
            instance = new DocumentListView();
        }
        return instance;
    }
}

