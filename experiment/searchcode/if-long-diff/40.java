package at.ac.tuwien.qse.group07.cocolounge.gui.tasks;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import at.ac.tuwien.qse.group07.cocolounge.core.entity.Project;
import at.ac.tuwien.qse.group07.cocolounge.core.entity.Task;
import at.ac.tuwien.qse.group07.cocolounge.core.service.EmployeeService;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Date;
import java.lang.Math;

public class MyTasksPanel extends Panel {

	private static final long serialVersionUID = 1348657368160460903L;
	
	@SpringBean
	private EmployeeService employeeService;
	
	public MyTasksPanel(String id) {
		super(id);
		
		final Date today = new Date();
		
		//TODO: create links instead of labels
		add(new ListView("tasklist", employeeService.getAssignedTasks()) {
			protected void populateItem(ListItem item) {
		        Task task = (Task) item.getModelObject();
		        item.add(new Label("taskname", task.getName()));
		        
		        Long diff = today.getTime() - task.getDuedate().getTime();
		        diff /= 60000;
		        if (Math.abs(diff) <= 60) {
		        	item.add(new Label("taskduedate", diff.toString()+"m"));
		        } else if (Math.abs(diff) <= 60*60) {
		        	diff /= 60;
		        	item.add(new Label("taskduedate", diff.toString()+"h"));
		        } else {
		        	diff /= (24*60);
		        	item.add(new Label("taskduedate", diff.toString()+"d"));
		        }
		    }
		});
	}
	
}

