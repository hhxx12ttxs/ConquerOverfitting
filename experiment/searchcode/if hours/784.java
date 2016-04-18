package com.example.mobile.viewer;

import com.example.mobile.MobileUI;
import com.example.mobile.data.MealOptionDeadline;
import com.vaadin.data.util.sqlcontainer.connection.JDBCConnectionPool;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;

@SuppressWarnings("serial")
public class HourView extends HorizontalLayout {
	
	private Label hoursLabel;
	private Button plus;
	private Button minus;
	private Integer hours;
	
	private MobileUI ui = ((MobileUI) UI.getCurrent());
	private JDBCConnectionPool connectionPool = ui.getConnectionPool();
	private MealOptionDeadline mealoptiondeadline;

	
	
	public HourView(MealOptionDeadline selected){
		mealoptiondeadline = selected;
		hours = mealoptiondeadline.getChour();
		hoursLabel = new Label(displayhours(hours));
		//hoursLabel.setContentMode(ContentMode.HTML);
		plus = new Button("+");
		minus = new Button("-");
		
		addComponent(hoursLabel);
		addComponent(plus);
		addComponent(minus);
		
		
		plus.addClickListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (hours < 23) {
					hours = hours + 1;
				} else {
					hours = 0;
				}
				hoursLabel.setValue(displayhours(hours));
				ui.updateMealOptionDeadlineHours(connectionPool, mealoptiondeadline, hours);
				
			}});

		minus.addClickListener(new ClickListener(){

			@Override
			public void buttonClick(ClickEvent event) {
				// TODO Auto-generated method stub
				if (hours > 0) {
					hours = hours - 1;
				} else {
					hours = 23;
				}
				hoursLabel.setValue(displayhours(hours));
				ui.updateMealOptionDeadlineHours(connectionPool, mealoptiondeadline, hours);
				
			}});
	}
		
		private String displayhours(Integer hours) {
			// TODO Auto-generated method stub
			String display = "";
			if (hours >=0 && hours <= 9){
				//display = "<center>" + "0" + hours.toString() + "</center>";
				display = "0" + hours.toString();
			} else {
				//display =  "<center>" + hours.toString() + "</center>";
				display =  hours.toString();
			}
			return display;
		}
	
	

}

