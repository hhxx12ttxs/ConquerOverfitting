package at.ac.tuwien.qse.group07.cocolounge.gui;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import at.ac.tuwien.qse.group07.cocolounge.core.entity.Event;
import at.ac.tuwien.qse.group07.cocolounge.core.entity.Project;
import at.ac.tuwien.qse.group07.cocolounge.core.entity.Task;
import at.ac.tuwien.qse.group07.cocolounge.core.service.EmployeeService;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Date;
import java.lang.Math;

public class MyEventsPanel extends Panel {

	private static final long serialVersionUID = 1348657368160460903L;
	
	@SpringBean
	private EmployeeService employeeService;
	
	public MyEventsPanel(String id) {
		super(id);
		
		final Date today = new Date();
		
		//TODO: create links instead of labels
		add(new ListView("eventlist", employeeService.getUpcomingEvents()) {
			protected void populateItem(ListItem item) {
		        Event event = (Event) item.getModelObject();
		        item.add(new Label("eventname", event.getName()));
		        
		        // TODO: remove the abs, it is redundant when "only upcoming"-checks are enabled
		        Long diff = Math.abs(today.getTime() - event.getStartdate().getTime());
		        diff /= 60000;
		        if (diff <= 60) {
		        	item.add(new Label("eventstartdate", diff.toString()+"m"));
		        } else if (diff <= 60*60) {
		        	diff /= 60;
		        	item.add(new Label("eventstartdate", diff.toString()+"h"));
		        } else {
		        	diff /= (24*60);
		        	item.add(new Label("eventstartdate", diff.toString()+"d"));
		        }
		    }
		});
	}
	
}

