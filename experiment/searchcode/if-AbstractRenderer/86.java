package org.itx.jbalance.equeue.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueListBox;

public class DateChooserComponent extends FlowPanel
	implements HasValue<Date>{
	

	Renderer<Integer> intRenderer=  new AbstractRenderer<Integer>() {
    	@Override
		public String render(Integer object) {
			return object+"";
		}
	};
	
	Renderer<Integer> monthRenderer=  new AbstractRenderer<Integer>() {
		private String[] names={"???","???","???","???","???","????","????","??????","????????","???????","??????","???????"};
    	@Override
		public String render(Integer m) {
    		if(m==null)
    			return "";
			return names[m-1];
		}
	};
	
	
	private ValueListBox<Integer> yearListBox=new ValueListBox<Integer>(intRenderer);
	private ValueListBox<Integer> monthListBox=new ValueListBox<Integer>(monthRenderer);
	private ValueListBox<Integer> dayListBox=new ValueListBox<Integer>(intRenderer);
	
	
	
	
	public DateChooserComponent (){
		super();
		Date date = new Date();
		Collection<Integer> years=new ArrayList<Integer>();
		int currentYear = date.getYear();
		for(int y=currentYear+1900;y>currentYear+1900-30;y--){
			years.add(y);
		}
		yearListBox.setAcceptableValues(years);
		yearListBox.setValue(currentYear+1900);
		Collection<Integer> monthes=new ArrayList<Integer>();
		for(int m=1;m<=12;m++){
			monthes.add(m);
		}
		monthListBox.setAcceptableValues(monthes);
		monthListBox.setValue(date.getMonth()+1);
		Collection<Integer> days=new ArrayList<Integer>();
		for(int m=1;m<=31;m++){
			days.add(m);
		}
		dayListBox.setAcceptableValues(days);
		dayListBox.setValue(date.getDate());
		
		
		add(dayListBox);
		add(monthListBox);
		add(yearListBox);
	}
	

	

	public void setEnable(Boolean e){
		if(e==null)
			return;
		DOM.setElementPropertyBoolean(yearListBox.getElement(), "disabled", !e);
		DOM.setElementPropertyBoolean(monthListBox.getElement(), "disabled", !e);
		DOM.setElementPropertyBoolean(dayListBox.getElement(), "disabled", !e);
	}

//	List<ValueChangeHandler<Date>> valueChangeHandlers=new ArrayList<ValueChangeHandler<Date>>();
	
	 
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Date> handler) {
		
		return null;//addHandler(handler ,new GwtEvent.Type<Date>());
	}

	@Override
	public Date getValue() {
		Date date = new Date(yearListBox.getValue()-1900,monthListBox.getValue()-1,dayListBox.getValue());
		
		return date;
	}

	@Override
	public void setValue(Date value) {
		if(value==null)
			value = new Date();
		yearListBox.setValue(value.getYear()+1900);
		monthListBox.setValue(value.getMonth()+1);
		dayListBox.setValue(value.getDate());
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		setValue( value);
		
	}

	
}

