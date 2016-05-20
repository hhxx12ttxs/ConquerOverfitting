package org.itx.jbalance.equeue.gwt.client.reports;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.itx.jbalance.equeue.gwt.client.ContentWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;

public class Reporta5_5Widget extends ContentWidget implements Reporta5_1Presenter.Display {

	interface Binder extends UiBinder<Widget, Reporta5_5Widget> {

	}

	@UiField(provided=true)
	ValueListBox<Integer>yearFrom;
	
	@UiField(provided=true)
	ValueListBox<Integer>yearTo;


	@Override
	public void initialize() {
		
		AbstractRenderer<Integer> renderer = new AbstractRenderer<Integer>() {
	    	@Override
			public String render(Integer object) {
	    		if(object==null)
	    			return "--";
				return object+"";
			}
	    	
	    	
		};
		yearFrom=new ValueListBox<Integer>(renderer);
		yearTo=new ValueListBox<Integer>(renderer);
			
			Date date = new Date();
			Collection<Integer> years=new ArrayList<Integer>();
			@SuppressWarnings("deprecation")
			int currentYear = date.getYear();
			for(int y=currentYear+1900;y>currentYear+1900-8;y--){
				years.add(y);
			}
			
			yearFrom.setAcceptableValues(years);
			yearFrom.setValue(years.iterator().next());
			
			yearTo.setAcceptableValues(years);
			yearTo.setValue(years.iterator().next());
		
		Binder uiBinder = GWT.create(Binder.class);
		initWidget(uiBinder.createAndBindUi(this));
		
	}

	
	@UiHandler("generate")
	void generate(ClickEvent event){
		Window.open("equeue/report?type=queueByYear" +
				"&yearFrom="+yearFrom.getValue()+
				"&yearTo="  +yearTo.getValue()		, "blank_", null);
	}
	
	

}

