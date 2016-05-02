package com.example.vaadindemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.example.vaadindemo.domain.Task;
import com.example.vaadindemo.service.StorageService;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;

public class TaskTreeTable extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	protected static final String TITLE_PROPERTY = "Title";
	protected static final String DESCRIPTION_PROPERTY = "Description";
	protected static final String DATE_PROPERTY = "Deadline";

	Object allTasks;
	Object importantUrgentTasks;
	Object importantNonUrgentTasks;
	Object unImportantUrgentTasks;
	Object unImportantNonUrgentTasks;

	protected TreeTable treetable;
	StorageService ss = new StorageService();
	String[] tab;

	public TaskTreeTable() {
		setWidth("100%");

		treetable = new TreeTable();
		treetable.setStyleName(Reindeer.TABLE_BORDERLESS);
		treetable.setWidth("100%");
		treetable.setSelectable(true);
		treetable.setMultiSelect(false);

		treetable.addListener(new ItemClickEvent.ItemClickListener() {

			private static final long serialVersionUID = 1L;

			public void itemClick(ItemClickEvent event) {
				Item clicked = event.getItem();
				final Object itemId = event.getItemId();
				Property title = clicked.getItemProperty("Title");
				Property description = clicked.getItemProperty("Description");
				Property deadline = clicked.getItemProperty("Deadline");
				Date dead = (Date) deadline.getValue();

				final Task toAdd = new Task(Long.parseLong("0"), title
						.toString(), dead, description.toString(), false, false);
				if (event.isDoubleClick()) {
					final Window formTask = new Window("Delete Task");
					formTask.setModal(true);
					formTask.setWidth("275px");

					Label richText = new Label(
							"<h1>Do you want to delete this Task?</h1>"
									+ "<h4>Task </h4> " + title.toString()
									+ "will be <b>irretrievably lost</b> </h4>");
					richText.setContentMode(Label.CONTENT_XHTML);

					VerticalLayout vl = new VerticalLayout();
					Button usunButton = new Button("Delete");
					usunButton.addListener(new Button.ClickListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(ClickEvent event) {
							removeTask(itemId);
							Window mainWindow = formTask.getParent();
							mainWindow.removeWindow(formTask);

						}
					});

					vl.addComponent(richText);
					vl.addComponent(usunButton);

					formTask.addComponent(vl);
					getApplication().getMainWindow().addWindow(formTask);
				} else {

					Form taskForm = new Form();

					final BeanItem<Task> taskItem = new BeanItem<Task>(toAdd);
					taskForm.setItemDataSource(taskItem);
					taskForm.setVisibleItemProperties(Arrays
							.asList(new String[] { "title", "description",
									"date", "important", "urgent" }));
					final Window formTask = new Window("Edit Task");
					formTask.setModal(true);
					formTask.setWidth("275px");

					VerticalLayout vl = new VerticalLayout();
					Button dodajButton = new Button("Edit");
					dodajButton.addListener(new Button.ClickListener() {

						private static final long serialVersionUID = 1L;

						@Override
						public void buttonClick(ClickEvent event) {
							removeTask(itemId);
							addTask(toAdd);
							Window mainWindow = formTask.getParent();
							mainWindow.removeWindow(formTask);

						}
					});

					vl.addComponent(taskForm);
					vl.addComponent(dodajButton);

					formTask.addComponent(vl);
					getApplication().getMainWindow().addWindow(formTask);
				}
			}
		});

		addComponent(treetable);

		treetable.addContainerProperty(TITLE_PROPERTY, String.class, "");
		treetable.addContainerProperty(DESCRIPTION_PROPERTY, String.class, "");
		treetable.addContainerProperty(DATE_PROPERTY, Date.class, null);
		List<Task> listTask = ss.getTaskList();

		allTasks = treetable.addItem(new Object[] { "All Tasks", "", null },
				"100");
		importantUrgentTasks = treetable.addItem(new Object[] {
				"Important Urgent", "", null }, "101");
		importantNonUrgentTasks = treetable.addItem(new Object[] {
				"Important Non-Urgent", "", null }, "102");
		unImportantUrgentTasks = treetable.addItem(new Object[] {
				"Unimportant Urgent", "", null }, "103");
		unImportantNonUrgentTasks = treetable.addItem(new Object[] {
				"Unimportant Non-Urgent", "", null }, "104");

		for (Task t : listTask) {
			Object task = treetable.addItem(
					new Object[] { t.getTitle(), t.getDescription(),
							t.getDate() }, null);
			treetable.setParent(task, allTasks);
			treetable.setChildrenAllowed(task, false);
		}

		listTask = ss.getTaskSelectedList(" Pilne Ważne");

		for (Task t : listTask) {
			Object task = treetable.addItem(
					new Object[] { t.getTitle(), t.getDescription(),
							t.getDate() }, null);
			treetable.setParent(task, importantUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		}

		listTask = ss.getTaskSelectedList(" Niepilne Ważne");

		for (Task t : listTask) {
			Object task = treetable.addItem(
					new Object[] { t.getTitle(), t.getDescription(),
							t.getDate() }, null);
			treetable.setParent(task, unImportantUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		}

		listTask = ss.getTaskSelectedList(" Pilne Nieważne");

		for (Task t : listTask) {
			Object task = treetable.addItem(
					new Object[] { t.getTitle(), t.getDescription(),
							t.getDate() }, null);
			treetable.setParent(task, importantNonUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		}

		listTask = ss.getTaskSelectedList(" Niepilne Nieważne");

		for (Task t : listTask) {
			Object task = treetable.addItem(
					new Object[] { t.getTitle(), t.getDescription(),
							t.getDate() }, null);
			treetable.setParent(task, unImportantNonUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		}

		treetable.setCollapsed(allTasks, false);
		treetable.setImmediate(true);

	}

	public StorageService getSs() {
		return ss;
	}

	public void setSs(StorageService ss) {
		this.ss = ss;
	}

	public void addTask(Task t) {

		Object task1 = treetable.addItem(
				new Object[] { t.getTitle(), t.getDescription(), t.getDate() },
				null);
		treetable.setParent(task1, allTasks);
		treetable.setChildrenAllowed(task1, false);
		Object task = treetable.addItem(
				new Object[] { t.getTitle(), t.getDescription(), t.getDate() },
				null);
		if (t.getImportant() && t.getUrgent()) {
			treetable.setParent(task, importantUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		} else if (t.getImportant() && !t.getUrgent()) {
			treetable.setParent(task, importantNonUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		} else if (!t.getImportant() && t.getUrgent()) {
			treetable.setParent(task, unImportantUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		} else {
			treetable.setParent(task, unImportantNonUrgentTasks);
			treetable.setChildrenAllowed(task, false);
		}
	}

	public void removeTask(Object itemId) {

		HierarchicalContainer container = (HierarchicalContainer) treetable
				.getContainerDataSource();
		Item it = container.getItem(itemId);
		
		List<Object> ids = new ArrayList<Object>();

			for (Iterator<?> it1 = container.getItemIds().iterator(); it1
					.hasNext();) {
				Object o1 = it1.next();
				Item o = container.getItem(o1);

				if (o.getItemProperty("Title").toString().equals(it.getItemProperty("Title").toString())) {
					ids.add(o1);
				}
			}
		
		for (Object o : ids) {
			treetable.removeItem(o);
		}

	}

	public List<String> getTitles() {
		HierarchicalContainer container = (HierarchicalContainer) treetable
				.getContainerDataSource();
		Collection<?> col = container.getChildren("100");
		Iterator<?> i = col.iterator();
		List<String> titles = new ArrayList<String>();
		while (i.hasNext()) {
			Object id = container.getIdByIndex(Integer.parseInt(i.next()
					.toString()));
			Item itemek = container.getItem(id);

			String title = itemek.getItemProperty("Title").toString();

			if (title.equals(container.getItem("101").getItemProperty("Title")
					.toString())
					|| title.equals(container.getItem("102")
							.getItemProperty("Title").toString())
					|| title.equals(container.getItem("103")
							.getItemProperty("Title").toString())
					|| title.equals(container.getItem("104")
							.getItemProperty("Title").toString())) {
				continue;
			} else {
				titles.add(title);
			}
		}

		col = container.getChildren("101");
		i = col.iterator();
		while (i.hasNext()) {
			Object id = container.getIdByIndex(Integer.parseInt(i.next()
					.toString()));
			Item itemek = container.getItem(id);

			String title = itemek.getItemProperty("Title").toString();
			titles.add(title);
		}

		col = container.getChildren("102");
		i = col.iterator();
		while (i.hasNext()) {
			Object id = container.getIdByIndex(Integer.parseInt(i.next()
					.toString()));
			Item itemek = container.getItem(id);

			String title = itemek.getItemProperty("Title").toString();
			titles.add(title);
		}

		col = container.getChildren("103");
		i = col.iterator();
		while (i.hasNext()) {
			Object id = container.getIdByIndex(Integer.parseInt(i.next()
					.toString()));
			Item itemek = container.getItem(id);

			String title = itemek.getItemProperty("Title").toString();
			titles.add(title);
		}

		col = container.getChildren("104");
		i = col.iterator();
		while (i.hasNext()) {
			Object id = container.getIdByIndex(Integer.parseInt(i.next()
					.toString()));
			Item itemek = container.getItem(id);

			String title = itemek.getItemProperty("Title").toString();
			titles.add(title);
		}
		return titles;
	}

	public void setTasksToDelete(String[] tab) {
		this.tab = tab;
	}

	public void removeFromTab() {
		HierarchicalContainer container = (HierarchicalContainer) treetable
				.getContainerDataSource();
		List<Object> ids = new ArrayList<Object>();

		for (int i = 0; i < tab.length; i++) {
			for (Iterator<?> it = container.getItemIds().iterator(); it
					.hasNext();) {
				Object o1 = it.next();
				Item o = container.getItem(o1);

				if (o.getItemProperty("Title").toString().equals(tab[i])) {
					ids.add(o1);
				}
			}
		}
		for (Object o : ids) {
			treetable.removeItem(o);
		}
	}

}

