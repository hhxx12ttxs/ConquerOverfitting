package com.paramount.client.module.inquiry.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.paramount.client.module.form.ParamountDefaultAddFormView;
import com.paramount.client.module.util.POCComponentFactory;
import com.paramount.client.module.util.POCDataUtils;
import com.paramount.shared.ParamountConstants;

public class ParamountFormUtils {
	
	public static void createFormHeader(String formTitle,FlexTable layout) {
		FlexTable headerTable = new FlexTable();
		FlexCellFormatter cellFormatter = headerTable.getFlexCellFormatter();
	    // Add a title to the form
		headerTable.setWidget(0, 0, new Label(formTitle));
		headerTable.setWidth("100%");
	    layout.getFlexCellFormatter().setColSpan(0, 0, 4);
	    layout.setWidget(0, 0, headerTable);
	}
	
	public static FlexTable createActionsHeader(String formTitle,FlexTable layout) {
		FlexTable headerTable = new FlexTable();
		FlexCellFormatter cellFormatter = headerTable.getFlexCellFormatter();
	    // Add a title to the form
		headerTable.setWidget(0, 1, new Label(formTitle));
		FlexTable actionsTable = new FlexTable();
		
		headerTable.setWidget(0, 2, actionsTable);
		headerTable.setWidth("100%");
	    cellFormatter.setStylePrimaryName(0, 0, "poc-form-heading");
	    layout.getFlexCellFormatter().setColSpan(0, 0, 4);
	    layout.setWidget(0, 0, headerTable);
	    return actionsTable;
	}
	
	public static int createRows(LinkedHashMap<Object, Object> valueMap,FlexTable layout) {
		int columncounter=0;
		int rowCounter=2;
		
		Set<Entry<Object, Object>> entries = valueMap.entrySet();
		for(Entry<Object, Object> entry: entries) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			layout.getColumnFormatter().setWidth(columncounter, "33.3%");
			populateCol1(key, value,rowCounter,columncounter%3,layout);
			
			columncounter++;
			if(columncounter%3==0) rowCounter++;
		}
		return rowCounter;
	}
	
	public static void populateCol1(Object col1, Object col2, int rowIndex, int colIndex,FlexTable layout) {
		
		FlexTable formTable = new FlexTable();
		formTable.setCellSpacing(6);
		Widget widget;
		if(col1 instanceof String) {
		widget = new Label((String)col1);
		widget.setStyleName("form-label");
		} else {
			widget = (Widget)col1;
		}
		formTable.setWidget(0,0,widget);
		
		if(col2 instanceof String) {
			widget = new Label((String)col2);
			widget.setStyleName("form-label-text");
		} else {
			widget = (Widget)col2;
		}
		
		formTable.setWidget(0,1,widget);
		layout.setWidget(rowIndex, colIndex, formTable);

	}
	
	public static  void renderViewTable(
			LinkedHashMap<Object, LinkedHashMap<Object, Object>> result, FlexTable layout, ArrayList<DisclosurePanel> panels) {
		LinkedHashMap<Object, Object> valueMap = POCDataUtils.createValueMap(result);
		
		int rowsCount = ParamountFormUtils.createRows(valueMap,layout);
		
		FlexCellFormatter formatter = layout.getFlexCellFormatter();
		formatter.setColSpan(++rowsCount, 0, 4);
		formatter.setColSpan(rowsCount, 0, 4);
		layout.setHTML(rowsCount, 0, "");
		
		formatter.setStylePrimaryName(rowsCount, 0, "poc-bottom-border");		   

		ArrayList<DisclosurePanel> textAreaPanels = POCDataUtils.getTextAreaPanels(result);
		for(DisclosurePanel panel: textAreaPanels) {
			layout.setWidget(++rowsCount, 0, panel);
			formatter.setColSpan(rowsCount, 0, 3);
			}
		
		formatter.setStylePrimaryName(rowsCount, 0, "poc-bottom-border");		   
		
		if(panels!=null) {
		for(DisclosurePanel panel: panels) {
			layout.setWidget(++rowsCount, 0, panel);
			formatter.setColSpan(rowsCount, 0, 3);
			}
		}
	}

	public static LinkedHashMap<Object, Object> createEmptyValueMap(LinkedHashMap<Object, LinkedHashMap<Object, Object>> entityMap) {
		LinkedHashMap<Object, Object> valueMap = new LinkedHashMap<Object, Object>();
		Set<Entry<Object, LinkedHashMap<Object, Object>>> entrySet = entityMap.entrySet();
		for(Entry<Object, LinkedHashMap<Object, Object>> entry:entrySet) {
			LinkedHashMap<Object, Object> value = entry.getValue();
			Widget displayWidget = null;  
			
			if(!ParamountConstants.YES.equals(value.get(ParamountConstants.FORM_VISIBLE))) {
				continue;
			}
			
			if(!ParamountConstants.YES.equals(value.get(ParamountConstants.IS_FORM_EDITABLE))) {
				Object entityValue = value.get(ParamountConstants.ENTITY_VALUE);			
				if(entityValue!=null) {
					displayWidget = new Label(entityValue.toString());
					valueMap.put(value.get(ParamountConstants.FORM_DISPLAY_NAME), displayWidget);
					value.put(ParamountConstants.DISPLAY_WIDGET, displayWidget);
					continue;
				}
			}
			
			
				String displayType = value.get(ParamountConstants.FORM_DISPLAY_TYPE).toString();
				if(ParamountConstants.HTML_TYPES.LISTBOX.toString().equals(displayType)) {
					Object mapObject = value.get(ParamountConstants.LIST_VALUES);
					displayWidget = POCComponentFactory.createListBox((LinkedHashMap<String, String>)mapObject);
				}
				
				if(ParamountConstants.HTML_TYPES.TEXTAREA.toString().equals(displayType)) {
					continue;
				}
				
				if(ParamountConstants.HTML_TYPES.LABEL.toString().equals(displayType)) {
					displayWidget = new TextBox();
				}

				if(ParamountConstants.HTML_TYPES.ANCHOR.toString().equals(displayType)) {
					displayWidget = new Label("INQ01");
				}
				
				valueMap.put(value.get(ParamountConstants.FORM_DISPLAY_NAME), displayWidget);
				value.put(ParamountConstants.DISPLAY_WIDGET, displayWidget);
			
		}
		
		return valueMap;
	}

	
	public static LinkedHashMap<Object, Object> createEditValueMap(LinkedHashMap<Object, LinkedHashMap<Object, Object>> entityMap) {
		LinkedHashMap<Object, Object> valueMap = new LinkedHashMap<Object, Object>();
		Set<Entry<Object, LinkedHashMap<Object, Object>>> entrySet = entityMap.entrySet();
		for(Entry<Object, LinkedHashMap<Object, Object>> entry:entrySet) {
			LinkedHashMap<Object, Object> value = entry.getValue();
			Widget displayWidget = null;  
			
			if(!ParamountConstants.YES.equals(value.get(ParamountConstants.FORM_VISIBLE))) {
				continue;
			}
			
			if(!ParamountConstants.YES.equals(value.get(ParamountConstants.IS_FORM_EDITABLE))) {
				Object entityValue = value.get(ParamountConstants.ENTITY_VALUE);			
				if(entityValue!=null) {
					displayWidget = new Label(entityValue.toString());
					valueMap.put(value.get(ParamountConstants.FORM_DISPLAY_NAME), displayWidget);
					value.put(ParamountConstants.DISPLAY_WIDGET, displayWidget);
					continue;
				}
			}
			
			
			Object entityValue = value.get(ParamountConstants.ENTITY_VALUE);
				String displayType = value.get(ParamountConstants.FORM_DISPLAY_TYPE).toString();
				if(ParamountConstants.HTML_TYPES.LISTBOX.toString().equals(displayType)) {
					Object mapObject = value.get(ParamountConstants.LIST_VALUES);
					ListBox listBox = POCComponentFactory.createListBox((LinkedHashMap<String, String>)mapObject);
					displayWidget = listBox;
					if(entityValue!=null) {
					int count = listBox.getItemCount();
					for(int i=0;i<count;i++) {
						if(listBox.getItemText(i).equals(entityValue.toString())) {
							listBox.setSelectedIndex(i);
							}
						}
					}
				}
				
				if(ParamountConstants.HTML_TYPES.TEXTAREA.toString().equals(displayType)) {
					continue;
				}
				
				if(ParamountConstants.HTML_TYPES.LABEL.toString().equals(displayType)) {
					TextBox textBox = new TextBox();
					displayWidget = textBox;
					if(entityValue!=null) textBox.setText(entityValue.toString());
				}

				if(ParamountConstants.HTML_TYPES.ANCHOR.toString().equals(displayType)) {
					displayWidget = new Label("INQ01");
				}
				
				valueMap.put(value.get(ParamountConstants.FORM_DISPLAY_NAME), displayWidget);
				value.put(ParamountConstants.DISPLAY_WIDGET, displayWidget);
			
		}
		
		return valueMap;
	}


	/**
	 * This function will return the table values for the id for which this class is created.
	 */
	

	public static FlexTable createFormActionsTable(ParamountDefaultAddFormView form) {
		FlexTable actionsTable = new FlexTable();
		Button saveButton = POCComponentFactory.createEditButton("Save");
		attachSaveEvent(saveButton, form);
		Button cancelButton = POCComponentFactory.createEditButton("Cancel");
		attachCancelEvent(cancelButton, form);
		actionsTable.setWidget(0, 0, saveButton);
		actionsTable.setWidget(0, 1, cancelButton);
		return actionsTable;
	}
	
	private static void attachSaveEvent(Button button,final ParamountDefaultAddFormView form) {
		button.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				form.saveEntity();				
			}
		});
	}
	
	private static void attachCancelEvent(Button button,final ParamountDefaultAddFormView form) {
		button.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				form.cancel();		
			}
		});
	}
}

