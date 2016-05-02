package org.auroraide.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.auroraide.client.menu.MenuComposite;
import org.auroraide.client.toolbar.ToolbarComposite;
import org.auroraide.client.ui.codePressEditor.CodePressEditorWidget;
import org.auroraide.client.ui.editorTabPanel.EditorTabPanel;
import org.auroraide.client.ui.newDialog.NewProjectDialog;
import org.auroraide.client.ui.simpleSortableTable.SortableTable;
import org.auroraide.client.ui.treeItem.ClassTreeItem;
import org.auroraide.client.ui.treeItem.PackageTreeItem;
import org.auroraide.client.ui.treeItem.ProjectTreeItem;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class auroraide implements EntryPoint {

	private VerticalSplitPanel workPanel;
	private TabPanel infoPanel;
	private HTML tasks;
	private EditorTabPanel editorPanel;
	private Tree packageTree;
	private SortableTable problemsTable;
	private HTML console;
	private CodePressEditorWidget codePressEditor;
	private NewProjectDialog newProject;
	private List elementEvent = new ArrayList();
	private FlexTable eventHistory;
	private TreeItem yourlynTreeItem;
	private ToggleButton focusToggleButton;
	private ToggleButton traceToggleButton;
	private boolean activateYourlyn = true;
	private ToggleButton activateToggleButton;
	private String compiler = "eclipse";
	private MenuComposite menu;

	public void onModuleLoad() {

		RootPanel rootPanel = RootPanel.get();
		newProject = new NewProjectDialog();
		codePressEditor = new CodePressEditorWidget();
		editorPanel = new EditorTabPanel();
		packageTree = new Tree();
		packageTree.addTreeListener(new TreeListener() {

			public void onTreeItemSelected(TreeItem item) {
				if (activateYourlyn) {
					int event = elementEvent.size() + 1;
					String kind = "selection";
					String target = item.getText().trim();
					int scaling = 1;
					elementEvent.add(new Event(event, kind, target, scaling));
				}
			}

			public void onTreeItemStateChanged(TreeItem item) {
				// TODO Auto-generated method stub

			}
		});

		infoPanel = new TabPanel();
		buildNewProjectDialog();
		buildMenuBar(rootPanel);
		buildToolBar(rootPanel);

		final HorizontalSplitPanel mainPanel = new HorizontalSplitPanel();
		rootPanel.add(mainPanel);
		mainPanel.setSize("98%", "86%");
		mainPanel.setSplitPosition("20%");

		final StackPanel explorerPanel = new StackPanel();
		mainPanel.setLeftWidget(explorerPanel);
		explorerPanel.setSize("100%", "100%");

		explorerPanel.add(packageTree,
				"<img src=\"icon/package.gif\"/> Package Explorer", true);

		packageTree.addTreeListener(new TreeListener() {
			public void onTreeItemSelected(final TreeItem item) {
				if (item instanceof ClassTreeItem) {
					String classContent = ((ClassUnit) ((ClassTreeItem) item)
							.getUserObject()).classContent;
					String className = ((ClassUnit) ((ClassTreeItem) item)
							.getUserObject()).className;
					if (editorPanel.getWidgetCount() > 0)
						editorPanel.remove(0);
					editorPanel.add(codePressEditor,
							"<img src=\"icon/jcu_obj.gif\"/> " + className
									+ ".java", true);
					editorPanel.selectTab(0);
					// Window.alert(classContent);
					codePressEditor.setCode(classContent);

				}
			}

			public void onTreeItemStateChanged(final TreeItem item) {
			}
		});

		FileBuilder.Util.getInstance().getFiles(new FilesUpdater());

		final VerticalPanel yourlynPanel = new VerticalPanel();
		explorerPanel.add(yourlynPanel,
				"<img src=\"icon/focus.gif\"/> Yourlyn", true);
		yourlynPanel.setWidth("100%");

		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		yourlynPanel.add(horizontalPanel);
		horizontalPanel.setWidth("100%");
		horizontalPanel.setStyleName("gwt-RichTextToolbar");
		focusToggleButton = new ToggleButton("Focus", "Focus");
		horizontalPanel.add(focusToggleButton);
		focusToggleButton.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				if (focusToggleButton.isDown()) {

					HashMap map = new HashMap();

					for (int i = 0; i < elementEvent.size(); i++) {
						Event temp = (Event) elementEvent.get(i);
						if (map.containsKey(temp.getTarget()))
							continue;
						float doi = DOI(temp.getTarget(), elementEvent);
						map.put(temp.getTarget(), Float.toString(doi));
						TreeItem item = new TreeItem(temp.getTarget() + "("
								+ map.get(temp.getTarget()) + ")");
						yourlynTreeItem.addItem(item);
						yourlynTreeItem.setVisible(true);
					}

					/*
					 * Map.Entry[] entries = (Map.Entry[])
					 * map.entrySet().toArray( new Map.Entry[0]);
					 * Arrays.sort(entries, ENTRY_VALUE_COMPARATOR);
					 * 
					 * for (Iterator iter = map.keySet().iterator(); iter
					 * .hasNext();) {
					 * 
					 * String next = iter.next().toString(); TreeItem item = new
					 * TreeItem(next + ":" + map.get(next));
					 * yourlynTreeItem.addItem(item);
					 * yourlynTreeItem.setVisible(true); }
					 */

				} else {
					yourlynTreeItem.setVisible(false);
					yourlynTreeItem.removeItems();

				}
			}
		});

		traceToggleButton = new ToggleButton("Trace", "Trace");
		horizontalPanel.add(traceToggleButton);
		traceToggleButton.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				if (traceToggleButton.isDown()) {
					eventHistory.setText(0, 0, "Event");
					eventHistory.setText(0, 1, "Kind");
					eventHistory.setText(0, 2, "Target(s)");
					for (int i = 0; i < elementEvent.size(); i++) {
						Event event = (Event) elementEvent.get(i);
						eventHistory.setText(i + 1, 0, String.valueOf(event
								.getEvent()));
						eventHistory.setText(i + 1, 1, event.getKind());
						eventHistory.setText(i + 1, 2, event.getTarget());
					}
				} else {
					int num = eventHistory.getRowCount();
					for (int i = 0; i < num; i++)
						eventHistory.removeRow(0);
				}
			}
		});

		activateToggleButton = new ToggleButton("Activate", "Activate");
		activateToggleButton.setDown(true);
		horizontalPanel.add(activateToggleButton);
		activateToggleButton.addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				if (activateToggleButton.isDown()) {
					activateYourlyn = true;
				} else {
					activateYourlyn = false;
					elementEvent.clear();
				}
			}
		});

		Tree yourlynTree = new Tree();
		yourlynTreeItem = yourlynTree.addItem("DOI Tree");
		yourlynTreeItem.setVisible(false);
		yourlynPanel.add(yourlynTree);

		eventHistory = new FlexTable();
		yourlynPanel.add(eventHistory);
		eventHistory.setWidth("100%");
		eventHistory.setStylePrimaryName("gwt-FlexTable");

		workPanel = new VerticalSplitPanel();
		workPanel.setSize("100%", "100%");
		workPanel.setSplitPosition("75%");
		mainPanel.setRightWidget(workPanel);

		editorPanel.setSize("100%", "100%");

		workPanel.setTopWidget(editorPanel);

		codePressEditor.setFocus(true);
		codePressEditor.setWidth("100%");
		codePressEditor.setVisibleLines(20);
		editorPanel.add(codePressEditor,
				"<img src=\"icon/jcu_obj.gif\"/> New Editor", true);

		workPanel.setBottomWidget(infoPanel);
		infoPanel.setWidth("100%");

		tasks = new HTML("Ready to roll!", true);
		infoPanel.add(tasks, "<img src=\"icon/tasks_tsk.gif\"/> Tasks", true);

		problemsTable = new SortableTable();
		buildProblemsTable();
		infoPanel.add(problemsTable,
				"<img src=\"icon/problems_view.gif\"/> Problems", true);

		console = new HTML();
		infoPanel.add(console, "<img src=\"icon/console_view.gif\"/> Console",
				true);

		// explorerPanel.sselectTab(0);
		infoPanel.selectTab(0);
		editorPanel.selectTab(0);

	}

	private void buildNewProjectDialog() {
		newProject.getCancelButton().addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				newProject.hide();

			}

		});
		newProject.getFinishButton().addClickListener(new ClickListener() {

			private ClassUnit classUnit;

			public void onClick(Widget sender) {
				String projectName = newProject.getProjectName().getText()
						.trim();
				String packageName = newProject.getPackageName().getText()
						.trim();
				String className = newProject.getClassName().getText().trim();
				classUnit = new ClassUnit();
				classUnit.className = className;
				classUnit.packageName = packageName;
				classUnit.projectName = projectName;

				if (newProject.getType().equalsIgnoreCase("project")
						&& projectName.length() > 0) {

					FileBuilder.Util.getInstance().createFile(classUnit,
							"project", new AsyncCallback() {

								public void onFailure(Throwable caught) {
									Window
											.alert("Creating "
													+ classUnit.projectName
													+ " fails!");

								}

								public void onSuccess(Object result) {
									if (result.toString().equalsIgnoreCase(
											"false"))
										return;
									packageTree.addItem(new ProjectTreeItem(
											classUnit.projectName));

								}
							});

				} else if (newProject.getType().equalsIgnoreCase("package")) {
					if (packageTree.getSelectedItem() instanceof ProjectTreeItem
							&& packageName.length() > 0)

						FileBuilder.Util.getInstance().createFile(classUnit,
								"package", new AsyncCallback() {

									public void onFailure(Throwable caught) {
										Window.alert("Creating "
												+ classUnit.packageName
												+ " fails!");

									}

									public void onSuccess(Object result) {
										if (result.toString().equalsIgnoreCase(
												"false"))
											return;
										packageTree.getSelectedItem().addItem(
												new PackageTreeItem(
														classUnit.packageName));

									}
								});

				} else if (newProject.getType().equalsIgnoreCase("class")) {

					if (packageTree.getSelectedItem() instanceof PackageTreeItem
							&& className.length() > 0)
						FileBuilder.Util.getInstance().createFile(classUnit,
								"class", new AsyncCallback() {

									public void onFailure(Throwable caught) {
										Window.alert("Creating "
												+ classUnit.className
												+ " fails!");

									}

									public void onSuccess(Object result) {
										if (result.toString().equalsIgnoreCase(
												"false"))
											return;
										ClassTreeItem classItem = new ClassTreeItem(
												classUnit.className);
										classItem.setUserObject(classUnit);
										packageTree.getSelectedItem().addItem(
												classItem);

									}
								});

				}
				newProject.hide();

			}

		});
	}

	private void buildToolBar(RootPanel rootPanel) {

		/*
		 * final HorizontalPanel toolBarlPanel = new HorizontalPanel();
		 * 
		 * toolBarlPanel.setStyleName("gwt-RichTextToolbar"); //
		 * toolBarlPanel.setSize("100%", "3%"); toolBarlPanel.setHeight("3%");
		 * toolBarlPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		 * toolBarlPanel
		 * .setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		 * 
		 * final PushButton newPrj_pushButton = new PushButton(new Image(
		 * "icon/new_wiz1.gif"), new Image("icon/new_wiz2.gif"));
		 * toolBarlPanel.add(newPrj_pushButton);
		 * newPrj_pushButton.addClickListener(new ClickListener() {
		 * 
		 * public void onClick(Widget sender) { newProject.setType("project");
		 * newProject.show(); }
		 * 
		 * });
		 * 
		 * final PushButton newJP_pushButton = new PushButton(new Image(
		 * "icon/newjprj_wiz1.gif"), new Image("icon/newjprj_wiz.gif"));
		 * toolBarlPanel.add(newJP_pushButton);
		 * newJP_pushButton.addClickListener(new ClickListener() {
		 * 
		 * public void onClick(Widget sender) { newProject.setType("project");
		 * newProject.show(); }
		 * 
		 * });
		 * 
		 * final PushButton newP_pushButton = new PushButton(new Image(
		 * "icon/newpack_wiz1.gif"), new Image("icon/newpack_wiz.gif"));
		 * toolBarlPanel.add(newP_pushButton);
		 * newP_pushButton.addClickListener(new ClickListener() { public void
		 * onClick(final Widget sender) { if (packageTree.getSelectedItem()
		 * instanceof ProjectTreeItem) { newProject.setType("package");
		 * newProject.show(); String projectName = packageTree.getSelectedItem()
		 * .getText().trim(); newProject.getProjectName().setText(projectName); } }
		 * });
		 * 
		 * final PushButton newClass_pushButton = new PushButton(new Image(
		 * "icon/newclass_wiz1.gif"), new Image("icon/newclass_wiz.gif"));
		 * toolBarlPanel.add(newClass_pushButton);
		 * newClass_pushButton.addClickListener(new ClickListener() { public
		 * void onClick(final Widget sender) { if (packageTree.getSelectedItem()
		 * instanceof PackageTreeItem) { newProject.setType("class");
		 * newProject.show(); String packageName = packageTree.getSelectedItem()
		 * .getText().trim(); String projectName = packageTree.getSelectedItem()
		 * .getParentItem().getText().trim();
		 * newProject.getProjectName().setText(projectName);
		 * newProject.getPackageName().setText(packageName); } } });
		 * 
		 * final PushButton save_pushButton = new PushButton(new Image(
		 * "icon/save_edit1.gif"), new Image("icon/save_edit.gif"));
		 * toolBarlPanel.add(save_pushButton);
		 * save_pushButton.addClickListener(new ClickListener() { public void
		 * onClick(final Widget sender) { if (packageTree.getSelectedItem()
		 * instanceof ClassTreeItem) { ((ClassUnit)
		 * packageTree.getSelectedItem().getUserObject()).classContent =
		 * codePressEditor .getCode(); } } });
		 * 
		 * final PushButton delete_pushButton = new PushButton(new Image(
		 * "icon/delete_config1.gif"), new Image("icon/delete_config.gif"));
		 * toolBarlPanel.add(delete_pushButton);
		 * delete_pushButton.addClickListener(new ClickListener() {
		 * 
		 * public void onClick(Widget sender) {
		 * 
		 * boolean delete = Window.confirm("Are you sure to delete " +
		 * packageTree.getSelectedItem().getText().trim() + "?"); if (delete) { //
		 * if(packageTree.getSelectedItem() instanceof // ClassTreeItem)
		 * editorPanel.remove(0);
		 * 
		 * packageTree.getSelectedItem().remove(); } }
		 * 
		 * });
		 * 
		 * final PushButton debug_pushButton = new PushButton(new Image(
		 * "icon/debug_exc1.gif"), new Image("icon/debug_exc.gif"));
		 * toolBarlPanel.add(debug_pushButton);
		 * debug_pushButton.addClickListener(new ClickListener() { public void
		 * onClick(final Widget sender) { Window.alert("I can't debug now. I am
		 * still learning it!"); } });
		 * 
		 * final PushButton compile_pushButton = new PushButton(new Image(
		 * "icon/redo_edit1.gif"), new Image("icon/redo_edit.gif"));
		 * toolBarlPanel.add(compile_pushButton);
		 * compile_pushButton.addClickListener(new ClickListener() { public void
		 * onClick(final Widget sender) { infoPanel.selectTab(0);
		 * tasks.setText("Compiling at " + new Date()); if
		 * (codePressEditor.getCode() != null &&
		 * codePressEditor.getCode().length() > 0) {
		 * 
		 * for (int i = 1; i < problemsTable.getRowCount(); i++)
		 * problemsTable.removeRow(i); if (packageTree.getSelectedItem()
		 * instanceof ClassTreeItem) { ClassUnit claaUnit = (ClassUnit)
		 * ((ClassTreeItem) packageTree .getSelectedItem()).getUserObject();
		 * claaUnit.classContent = codePressEditor.getCode();
		 * CompileCode.Util.getInstance().getCompilingResult( claaUnit, new
		 * ProblemsTreeUpdater()); } else Window.alert("Choose java file to
		 * compile"); } } });
		 * 
		 * final PushButton run_pushButton = new PushButton(new Image(
		 * "icon/run_exc1.gif"), new Image("icon/run_exc.gif"));
		 * run_pushButton.addClickListener(new ClickListener() {
		 * 
		 * public void onClick(Widget sender) { if (codePressEditor.getCode() !=
		 * null && codePressEditor.getCode().length() > 0) { if
		 * (packageTree.getSelectedItem() instanceof ClassTreeItem) {
		 * 
		 * ClassUnit classUnit = (ClassUnit) ((ClassTreeItem) packageTree
		 * .getSelectedItem()).getUserObject(); classUnit.classContent =
		 * codePressEditor.getCode();
		 * RunProgram.Util.getInstance().runClass(classUnit, new RunClass());
		 * tasks.setText("Runing " + classUnit.className + ".java at " + new
		 * Date()); infoPanel.selectTab(2); }
		 * 
		 * else Window.alert("Choose java file to run"); } } });
		 * 
		 * toolBarlPanel.add(run_pushButton);
		 */
		ToolbarComposite toolbar = new ToolbarComposite(packageTree,
				newProject, editorPanel, codePressEditor);
		toolbar.getCompile_pushButton().addClickListener(new ClickListener() {
			public void onClick(final Widget sender) {
				compile();
				if (activateYourlyn) {
					if (packageTree.getSelectedItem() instanceof ClassTreeItem) {

						int event = elementEvent.size() + 1;
						String kind = "compile";
						String target = packageTree.getSelectedItem().getText()
								.trim();
						int scaling = 2;
						elementEvent
								.add(new Event(event, kind, target, scaling));
					}
				}
			}
		});
		toolbar.getRun_pushButton().addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				runProgram();
				if (activateYourlyn) {
					if (packageTree.getSelectedItem() instanceof ClassTreeItem) {
						int event = elementEvent.size() + 1;
						String kind = "run";
						String target = packageTree.getSelectedItem().getText()
								.trim();
						int scaling = 3;
						elementEvent
								.add(new Event(event, kind, target, scaling));
					}
				}
			}
		});

		toolbar.getDown_pushButton().addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				if (packageTree.getSelectedItem() instanceof ClassTreeItem) {
					ClassUnit classUnit = (ClassUnit) packageTree
							.getSelectedItem().getUserObject();
					String name = classUnit.className;
					String project = classUnit.projectName;
					String pkg = classUnit.packageName;
					//Window.alert(name+project+pkg);
					Window
							.open("FileDownloadServlet?name=" + name
									+ "&project=" + project + "&pkg=" + pkg,
									"_blank",
									"menubar=0,resizable=0,toolbar=0,location=0,width=100,height=100");
				} else
					Window.alert("Select a class file first!");
			}

		});

		rootPanel.add(toolbar);
	}

	private void compile() {
		if (codePressEditor.getCode() != null
				&& codePressEditor.getCode().length() > 0) {
			// save first
			boolean cont = Window.confirm("Save file before compiling?");
			if (!cont)
				return;

			if (packageTree.getSelectedItem() instanceof ClassTreeItem) {
				((ClassUnit) packageTree.getSelectedItem().getUserObject()).classContent = codePressEditor
						.getCode();

				ClassUnit classUnit = (ClassUnit) packageTree.getSelectedItem()
						.getUserObject();
				FileBuilder.Util.getInstance().modifyFile(classUnit, "class",
						new AsyncCallback() {

							public void onFailure(Throwable caught) {
								Window.alert("Saving Fails!");

							}

							public void onSuccess(Object result) {
								if (result.toString().equalsIgnoreCase("false"))
									Window.alert("Saving Fails!");

							}
						});

			}
			// compile
			for (int i = 1; i < problemsTable.getRowCount(); i++)
				problemsTable.removeRow(i);
			if (packageTree.getSelectedItem() instanceof ClassTreeItem) {

				ClassUnit claaUnit = (ClassUnit) ((ClassTreeItem) packageTree
						.getSelectedItem()).getUserObject();
				infoPanel.selectTab(0);
				tasks.setText("Compiling " + claaUnit.className + ".java at "
						+ new Date());

				claaUnit.classContent = codePressEditor.getCode();
				CompileCode.Util.getInstance().getCompilingResult(compiler,
						claaUnit, new ProblemsTreeUpdater());
			} else
				Window.alert("Choose java file to compile");

		}

	}

	private void runProgram() {
		if (codePressEditor.getCode() != null
				&& codePressEditor.getCode().length() > 0) {
			// save first
			boolean cont = Window.confirm("Save file before runing?");
			if (!cont)
				return;

			if (packageTree.getSelectedItem() instanceof ClassTreeItem) {
				((ClassUnit) packageTree.getSelectedItem().getUserObject()).classContent = codePressEditor
						.getCode();

				ClassUnit classUnit = (ClassUnit) packageTree.getSelectedItem()
						.getUserObject();
				FileBuilder.Util.getInstance().modifyFile(classUnit, "class",
						new AsyncCallback() {

							public void onFailure(Throwable caught) {
								Window.alert("Saving Fails!");

							}

							public void onSuccess(Object result) {
								if (result.toString().equalsIgnoreCase("false"))
									Window.alert("Saving Fails!");

							}
						});

			}
			// run program
			for (int i = 1; i < problemsTable.getRowCount(); i++)
				problemsTable.removeRow(i);
			if (packageTree.getSelectedItem() instanceof ClassTreeItem) {

				ClassUnit classUnit = (ClassUnit) ((ClassTreeItem) packageTree
						.getSelectedItem()).getUserObject();
				infoPanel.selectTab(0);
				tasks.setText("Runing " + classUnit.className + ".java at "
						+ new Date());
				classUnit.classContent = codePressEditor.getCode();
				RunProgram.Util.getInstance().runClass(compiler, classUnit,
						new RunClass());
				infoPanel.selectTab(2);
			}

			else
				Window.alert("Choose java file to run");
		}
	}

	private void buildMenuBar(RootPanel rootPanel) {

		menu = new MenuComposite(packageTree, newProject, editorPanel);
		menu.getCompile_runMenu().setCommand(new Command() {

			public void execute() {
				compile();
			}
		});

		menu.getRun_runMenu().setCommand(new Command() {

			public void execute() {
				runProgram();
			}
		});

		menu.getEcj_compilerMenu().setCommand(new Command() {

			public void execute() {
				if (!menu.getEcj_compilerMenu().isToggle()) {
					menu.getEcj_compilerMenu().toggle();
					menu.getJdk_compilerMenu().toggle();
				}
				compiler = "eclipse";

			}

		});

		menu.getJdk_compilerMenu().setCommand(new Command() {

			public void execute() {
				if (menu.getJdk_compilerMenu().isToggle()) {
					menu.getEcj_compilerMenu().toggle();
					menu.getJdk_compilerMenu().toggle();
				}
				compiler = "jsr199";

			}

		});

		rootPanel.add(menu);
	}

	private void buildProblemsTable() {

		problemsTable.setStyleName("sortableTable");
		problemsTable.setBorderWidth(0);
		problemsTable.setCellPadding(5);
		problemsTable.setCellSpacing(1);

		problemsTable.addColumnHeader("Type", 0);
		problemsTable.addColumnHeader("Description", 1);
		problemsTable.addColumnHeader("Resource", 2);
		problemsTable.addColumnHeader("path", 3);
		problemsTable.addColumnHeader("Location", 4);

		RowFormatter rowFormatter = problemsTable.getRowFormatter();
		rowFormatter.setStyleName(0, "tableHeader2");

		// Set the Styles for the Data Rows and Columns
		CellFormatter cellFormatter = problemsTable.getCellFormatter();
		// Set the styles for the headers
		for (int colIndex = 0; colIndex < 5; colIndex++) {
			cellFormatter.setStyleName(0, colIndex, "headerStyle");
			cellFormatter.setAlignment(0, colIndex,
					HasHorizontalAlignment.ALIGN_CENTER,
					HasVerticalAlignment.ALIGN_MIDDLE);
		}

	}

	private float DECAY(int fromEvent, int toEvent) {
		if (toEvent < fromEvent)
			return 0;
		return (float) ((toEvent - fromEvent) * 0.1);
	}

	float DOI(String element, List elementHistory) {
		List elementEvents = new ArrayList();
		float interest = 0;
		float totalDecay = 0;
		for (int i = 0; i < elementHistory.size(); i++) {
			Event event = (Event) elementHistory.get(i);
			if (element.equalsIgnoreCase(event.getTarget())) {
				elementEvents.add(event);
			}
		}

		Event decayStart = (Event) elementEvents.get(0);

		for (int i = 0; i < elementEvents.size(); i++) {
			Event temp = (Event) elementEvents.get(i);
			interest += temp.getScaling();
			float currDecay = DECAY(decayStart.getEvent(), temp.getEvent());

			if (interest < currDecay) {
				decayStart = temp;
				interest = temp.getScaling();
			}

		}

		totalDecay = DECAY(decayStart.getEvent(), elementHistory.size());

		return (interest - totalDecay);

	}

	class ProblemsTreeUpdater implements AsyncCallback {
		public void onFailure(Throwable caught) {
			tasks.setText("Compiler crash!");
		}

		public void onSuccess(Object result) {
			ProblemUnit[] problems = (ProblemUnit[]) result;
			if (problems == null || problems.length == 0) {
				ClassUnit classUnit = (ClassUnit) ((ClassTreeItem) packageTree
						.getSelectedItem()).getUserObject();
				tasks.setText(tasks.getText()
						+ "\nCompile complete!\nGenerating "
						+ classUnit.className + ".class!");
				return;
			}

			tasks.setText(tasks.getText() + "\n" + "Compile complete!\n"
					+ problems.length + " problem(s) found.");
			infoPanel.selectTab(1);
			RowFormatter rowFormatter = problemsTable.getRowFormatter();
			for (int i = 0; i < problems.length; i++) {

				problemsTable.setValue(i + 1, 0, problems[i].kind);
				problemsTable.setValue(i + 1, 1, problems[i].description);
				problemsTable.setValue(i + 1, 2, problems[i].resource);
				problemsTable.setValue(i + 1, 3, problems[i].path);
				problemsTable.setValue(i + 1, 4, new Integer(
						problems[i].location));
				if ((i + 1) % 2 == 0) {
					rowFormatter.setStyleName(i + 1, "customRowStyle");
				} else {
					rowFormatter.setStyleName(i + 1, "tableRow");
				}
				CellFormatter cellFormatter = problemsTable.getCellFormatter();
				for (int colIndex = 0; colIndex < 5; colIndex++) {
					cellFormatter.setStyleName(i + 1, colIndex, "customFont");
					cellFormatter.setAlignment(i + 1, colIndex,
							HasHorizontalAlignment.ALIGN_CENTER,
							HasVerticalAlignment.ALIGN_MIDDLE);
				}
			}

		}
	}

	class FilesUpdater implements AsyncCallback {

		public void onFailure(Throwable caught) {
			System.out.println(caught);

		}

		public void onSuccess(Object result) {
			ClassUnit[] classUnits = (ClassUnit[]) result;
			if (classUnits == null)
				return;
			for (int i = 0; i < classUnits.length; i++) {
				ClassUnit classUnit = classUnits[i];

				ProjectTreeItem projectName = null;
				if (classUnit.projectName != null
						&& classUnit.projectName.length() > 0) {
					for (int temp = 0; temp < packageTree.getItemCount(); temp++) {
						if (classUnit.projectName.equalsIgnoreCase(packageTree
								.getItem(temp).getText().trim())) {
							projectName = (ProjectTreeItem) packageTree
									.getItem(temp);
							break;
						}
					}
					if (projectName == null) {
						projectName = new ProjectTreeItem(classUnit.projectName);
					}
					packageTree.addItem(projectName);
				}

				PackageTreeItem packageTreeItem = null;
				if (classUnit.packageName != null
						&& classUnit.packageName.length() > 0) {
					for (int temp = 0; temp < projectName.getChildCount(); temp++) {
						if (classUnit.packageName.equalsIgnoreCase(projectName
								.getChild(temp).getText().trim())) {
							packageTreeItem = (PackageTreeItem) projectName
									.getChild(temp);
							break;
						}
					}
					if (packageTreeItem == null) {
						packageTreeItem = new PackageTreeItem(
								classUnit.packageName);
					}
					projectName.addItem(packageTreeItem);
				}

				if (classUnit.className != null
						&& classUnit.className.length() > 0) {
					ClassTreeItem classTreeItem = new ClassTreeItem(
							classUnit.className);
					classTreeItem.setUserObject(classUnit);
					packageTreeItem.addItem(classTreeItem);
				}

			}

		}

	}

	class RunClass implements AsyncCallback {

		public void onFailure(Throwable caught) {
			console.setText("Run crash");

		}

		public void onSuccess(Object result) {
			String consoleString = (String) result;
			console.setText(consoleString);

		}

	}

	static class Event {
		private int event;
		private String kind;
		private String target;
		private int scaling;

		public int getEvent() {
			return event;
		}

		public void setEvent(int event) {
			this.event = event;
		}

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public int getScaling() {
			return scaling;
		}

		public void setScaling(int scaling) {
			this.scaling = scaling;
		}

		public Event(int event, String kind, String target, int scaling) {
			this.event = event;
			this.kind = kind;
			this.target = target;
			this.scaling = scaling;
		}
	}

}

