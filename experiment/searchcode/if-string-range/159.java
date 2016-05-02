package mosaic.ui.editors.pages.table;

import java.util.ArrayList;
import java.util.StringTokenizer;

import mosaic.ui.dialogs.FileToDeployDialog;
import mosaic.ui.dialogs.ProgramToRunDialog;
import mosaic.ui.editors.pages.ResourcesModel;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;

import eu.mosaic.application.ComputeApplicationResourceClass;
import eu.mosaic.application.IApplicationResourceClass;
import eu.mosaic.deployment.FileToDeploy;
import eu.mosaic.resources.ResourceType;
import eu.mosaic.util.IWithIdentity;
import eu.mosaic.util.IntValueRange;

public class CustomTableViewer<ElementType extends IWithIdentity> {
//	private static final int ITEM_COUNT = 10;
	private IModel<ElementType> model;
	private Composite parent;
	private Composite composite;
	
	private TableViewer resourceViewer;
	private TableViewer fileViewer;
	private TableViewer runViewer;
	
	private Button addButton;
	private Button deleteButton;
	private Button upButton;
	private Button downButton;
	
	private Text ports;
	
	private IActionsPerformer<ElementType> actionsPerformer;
	
	private DeployedFilesListener dflistener;
	private RunListener runlistener;
	
	public static final String URL1 = "Local URL";
	public static final String URL2 = "Remote URL";	
	public static final String[] DEPLOYED_FILES_PROPS = {URL1, URL2};
	
	public CustomTableViewer(IModel<ElementType> model, IActionsPerformer<ElementType> actionsPerformer, 
			Composite parent, ResourceType type) {
		super();
		this.model = model;
		this.parent = parent;
		this.actionsPerformer = actionsPerformer;
		createContent(type);
	}
	
	private void createContent(final ResourceType type) {
		composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		composite.setLayout(layout);
		//add buttons
		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		GridLayout buttonsLayout = new GridLayout(4, false);
		buttonsComposite.setLayout(buttonsLayout);
		
		addButton = new Button(buttonsComposite, SWT.BORDER);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {			
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Add pressed");
				if (type == null)
					actionsPerformer.performAdd();
				else 
					((IResourceActionsPerformer<IApplicationResourceClass>) actionsPerformer).performAdd(type);
				refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub			
			}
		});
		
		deleteButton = new Button(buttonsComposite, SWT.BORDER);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new SelectionListener() {			
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Delete pressed");
				actionsPerformer.performDelete((ElementType)((IStructuredSelection) resourceViewer.getSelection()).getFirstElement());
				refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});		
		
		upButton = new Button(buttonsComposite, SWT.BORDER);
		upButton.setText("Up");
		upButton.addSelectionListener(new SelectionListener() {			
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Up pressed");			
				if (resourceViewer.getSelection() instanceof IStructuredSelection) { 
					ElementType element = (ElementType)((IStructuredSelection) resourceViewer.getSelection()).getFirstElement();
					if (type == null)
						actionsPerformer.performMoveUp(element);
					else
						((IResourceActionsPerformer<IApplicationResourceClass>) actionsPerformer).performMoveUp((IApplicationResourceClass) element, type);
					refresh();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
		downButton = new Button(buttonsComposite, SWT.BORDER);
		downButton.setText("Down");
		downButton.addSelectionListener(new SelectionListener() {			
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Down pressed");
				if (resourceViewer.getSelection() instanceof IStructuredSelection) {
					ElementType element = (ElementType)((IStructuredSelection) resourceViewer.getSelection()).getFirstElement();
					if (type == null)
						actionsPerformer.performMoveDown(element);
					else
						((IResourceActionsPerformer<IApplicationResourceClass>) actionsPerformer).performMoveDown((IApplicationResourceClass) element, type);
					refresh();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}				
		});
			
		Table table = new Table(composite, 
				SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		// table.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true));
		//add the table viewer
		resourceViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
				
		resourceViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void doubleClick(DoubleClickEvent event) {
				actionsPerformer.performModify((ElementType)((IStructuredSelection) resourceViewer.getSelection())
						.getFirstElement());
				refresh();
			}
		});
		
		//table.getTable().setItemCount(ITEM_COUNT);
		//add table columns
		TableColumnInfo[] columnsDescriptions = model.getColumnsDescriptions();
		ColumnLabelProvider[] columnsLabelProvider = model.getColumnsLabelProvider();
		int i = 0;
		for (TableColumnInfo columnInfo : columnsDescriptions) {
			// 
			TableViewerColumn col = createTableViewerColumn(resourceViewer, columnInfo.getName(), columnInfo.getWidth(), i);
			col.setLabelProvider(columnsLabelProvider[i++]);
		}
		
		if (type != null)
			resourceViewer.setContentProvider(((ResourcesModel) model).getContentProvider(type));
		else
			resourceViewer.setContentProvider(model.getContentProvider());
		//table.getTable().setItemCount(ITEM_COUNT);
		resourceViewer.setInput(model.getInput());
		resourceViewer.getControl().setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 4, 1));
		
		if (type != null) {
			switch (type) {
			case ANY:
				break;
			case COMPUTE: {
				ResourcesModel rmodel = (ResourcesModel) model;
				Group cmp1 = new Group(composite, SWT.NONE);
				cmp1.setText("Deployed files");
				cmp1.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, true, 2, 1));
				cmp1.setLayout(new GridLayout(2, false));
				createDeployedFileViewer(cmp1, rmodel);
				
				Group cmp2 = new Group(composite, SWT.NONE);
				cmp2.setText("Programs to run");
				cmp2.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, true, 2, 1));
				cmp2.setLayout(new GridLayout(2, false));	
				createProgramsToRunViewer(cmp2, rmodel);
				
				new Label(composite, SWT.NONE).setText("Ports:");
				ports = new Text(composite, SWT.BORDER);
				ports.setLayoutData(createLayoutData());
				resourceViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						ComputeApplicationResourceClass rc = (ComputeApplicationResourceClass) getResourceSelection();
						if (rc != null && rc.getOpenPorts() != null)
							ports.setText(IntValListToString(rc.getOpenPorts()));	
					}
				});
				
				ports.addModifyListener(new ModifyListener() {
					@Override
					public void modifyText(ModifyEvent e) {
						try {
							ComputeApplicationResourceClass resource = (ComputeApplicationResourceClass) getResourceSelection();
							if (resource != null) {
								resource.setOpenPorts(stringToIntValList(ports.getText()));
							}
						} catch (ClassCastException ex) { }
					}
				});
			    }			
				break;
				
			case STORAGE:
			case NETWORK:
			case VOLUME:
			case MAP_REDUCE:
			}
		}
	}
	
	public IApplicationResourceClass getResourceSelection() {
		if (resourceViewer == null) 
			return null;
		ISelection sel = resourceViewer.getSelection();
		IApplicationResourceClass result = getTfromSelection(sel);
		return result;
	}
	
	private GridData createLayoutData() {
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.minimumWidth = 250;
		return layoutData;
	}
	
	public void refresh() {
		resourceViewer.refresh();
	}
	
	private TableViewerColumn createTableViewerColumn(TableViewer viewer, String title, Integer bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		if (bound != null) 
			column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public Composite getComposite(){
		return composite;
	}
	
	public void inputChanged() {
		resourceViewer.setInput(model.getInput());
	}
	
	public void exportSelection(IWorkbenchPartSite site){
		//export selection
		site.setSelectionProvider(resourceViewer);

	}
	
	private String IntValListToString(java.util.List<IntValueRange> list) {
		if (list == null)
			return "";
		StringBuffer arr = new StringBuffer("");
		for (IntValueRange ivr : list) {
			arr.append(ivr.getMin());
			if(ivr.getMin() < ivr.getMax()) {
				arr.append('-');
				arr.append(ivr.getMax());
				arr.append(',');
			}
		};
		if(arr.length() > 0)
			arr.setLength(arr.length() - 1);
		return arr.toString();
	}
	
	private java.util.List<IntValueRange> stringToIntValList(String portsStr) {
		java.util.List<IntValueRange> result = new ArrayList<IntValueRange>();
		StringTokenizer portRanges= new StringTokenizer(portsStr, ",");
		int pos, min, max;
		String range;
		while (portRanges.hasMoreTokens()) {
			range = portRanges.nextToken();
			pos = range.indexOf('-');
			try {
				if (pos == -1) {
					min = Integer.parseInt(range);
					result.add(new IntValueRange(min, min));
				} else {
					min = Integer.parseInt(range.substring(0, pos));
					max = Integer.parseInt(range.substring(pos+1));
					result.add(new IntValueRange(min, max));
				}
			} catch (NumberFormatException e) {
				System.out.println("Unexpected range spec: " + range);
				// ignore
			}
		}
		return result;
	}
		
	private void createDeployedFileViewer(Composite parent, ResourcesModel rmodel) {
		Table fileTable =  new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		fileViewer = new TableViewer(fileTable);
		fileTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
		fileTable.setHeaderVisible(true);
		fileTable.setLinesVisible(true);
		
		dflistener = new DeployedFilesListener();
		resourceViewer.addSelectionChangedListener(dflistener);
		
		TableColumnInfo[] fileColDescriptions = rmodel.getFileColumnsDescriptions();
		ColumnLabelProvider[] fileColumnsLabelProvider = rmodel.getFDColumnsLabelProvider();
		// getColumnsLabelProvider();
		int i = 0;
		for (TableColumnInfo columnInfo : fileColDescriptions) {
			TableViewerColumn col = createTableViewerColumn(fileViewer, columnInfo.getName(), columnInfo.getWidth(), i);
			col.setLabelProvider(fileColumnsLabelProvider[i++]);
		}
		fileViewer.setContentProvider(ArrayContentProvider.getInstance());
		// rmodel.getDeployedFileContentProvider(null));
		fileViewer.setColumnProperties(DEPLOYED_FILES_PROPS);
		fileViewer.setCellModifier(new DeployedFileModifier(fileViewer));
		CellEditor[] fdEditors = new CellEditor[2];
		fdEditors[0] = new TextCellEditor(fileTable);
		fdEditors[1] = new TextCellEditor(fileTable);
	    fileViewer.setCellEditors(fdEditors);
	    
		Button add = new Button(parent, SWT.NONE);
	    Button del = new Button(parent, SWT.NONE);
	    add.setText("Add"); 
	    del.setText("Delete");
	    new Label(parent, SWT.NONE);
	    new Label(parent, SWT.NONE);
	    
	    add.addSelectionListener(new SelectionListener() {			
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		dflistener.performAdd();
	    	}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) { }
	    });
	    
	    del.addSelectionListener(new SelectionListener() {			
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		FileToDeploy ftd = getTfromSelection(fileViewer.getSelection());
	    		if (ftd != null) {
	    			dflistener.performDelete(ftd);
	    		}
	    	}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) { }
	    });
	    parent.pack();
	}
	
	private void createProgramsToRunViewer(Composite parent, ResourcesModel rmodel) {
		Table runTable =  new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		runViewer = new TableViewer(runTable);
		runTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		runTable.setHeaderVisible(true);
		runTable.setLinesVisible(true);
	
		runlistener = new RunListener();
		resourceViewer.addSelectionChangedListener(runlistener);
		
		TableColumnInfo[] runColDescriptions = rmodel.getProgramColumnsDescriptions();
		ColumnLabelProvider[] runColumnsLabelProvider = rmodel.getProgramsToRunLabelProvider();

		int i = 0;
		for (TableColumnInfo columnInfo : runColDescriptions) {
			// 
			TableViewerColumn col = createTableViewerColumn(runViewer, columnInfo.getName(), columnInfo.getWidth(), i);
			col.setLabelProvider(runColumnsLabelProvider[i++]);
		}
		runViewer.setContentProvider(ArrayContentProvider.getInstance());
		runViewer.setColumnProperties(new String[] {"Name"});
	    
	    Button add = new Button(parent, SWT.NONE);
	    Button del = new Button(parent, SWT.NONE);
	    add.setText("Add"); 
	    del.setText("Delete");
	    
	    add.addSelectionListener(new SelectionListener() {			
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		runlistener.performAdd();
	    	}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) { }
	    });
	    
	    del.addSelectionListener(new SelectionListener() {			
	    	@Override
	    	public void widgetSelected(SelectionEvent e) {
	    		String prog = getTfromSelection(runViewer.getSelection());
	    		if (prog != null) {
	    			runlistener.performDelete(prog);
	    		}
	    	}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) { }
	    });
	    parent.pack();
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getTfromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.size() == 1) {
				try {
					return ((T) sel.getFirstElement());
				} catch (ClassCastException e) {
					//
				}
			}
		}
		return null;
	}

	
//	public Object getSelection(){
//		table.getSelection();
//	}
	
	class RunListener implements ISelectionChangedListener {
		public ComputeApplicationResourceClass compResource = null;
		String[] empty = new String[0];
		
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			try {
				compResource = getTfromSelection(event.getSelection());
				if (compResource != null && compResource.getProgramsToRun() != null) {
					runViewer.setInput(compResource.getProgramsToRun());
				}  else
					runViewer.setInput(empty);
			} catch (ClassCastException ex) {
				System.out.println("No job for runViewer");
			}
		}	
		
		public void performDelete(String program) {
			ComputeApplicationResourceClass cmp = compResource;
			if (cmp != null) 
				cmp.getProgramsToRun().remove(program);			
			runViewer.refresh();
		}

		public void performAdd() {
			ComputeApplicationResourceClass cmp = compResource;
			if (cmp != null) {
				ProgramToRunDialog dlg = new ProgramToRunDialog(runViewer.getTable().getShell(), cmp);
				dlg.open();
			}
			runViewer.refresh();
		}
	}
	
	class DeployedFilesListener implements ISelectionChangedListener {
		public ComputeApplicationResourceClass compResource = null;
		FileToDeploy[] empty = new FileToDeploy[0];
		public DeployedFilesListener() { }

		public void performDelete(FileToDeploy ftd) {
			ComputeApplicationResourceClass cmp = compResource;
			if (cmp != null && cmp.getFilesToDeploy() != null) {
				cmp.getFilesToDeploy().remove(ftd);
			}
			fileViewer.refresh();
		}

		public void performAdd() {
			ComputeApplicationResourceClass cmp = compResource;
			if (cmp != null) {
				FileToDeployDialog dlg = new FileToDeployDialog(fileViewer.getTable().getShell(), cmp);
				dlg.open();
			}
			fileViewer.refresh();
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			try {
				compResource = getTfromSelection(event.getSelection());
				if (compResource != null) {
					fileViewer.setInput(compResource.getFilesToDeploy());
				} else
					fileViewer.setInput(empty);
			} catch (ClassCastException ex) {
				System.out.println("No job for fileViewer");
			}
		}		
	}		
}


class DeployedFileModifier implements ICellModifier {
	private Viewer viewer;

	public DeployedFileModifier(Viewer viewer) {
		this.viewer = viewer;
	}

	public boolean canModify(Object element, String property) {
		// Allow editing of everything
		return true;
	}
	public Object getValue(Object element, String property) {
		FileToDeploy fd = (FileToDeploy) element;
		if (CustomTableViewer.URL1.equals(property))
			return fd.getLocalURL();
		if (CustomTableViewer.URL2.equals(property))
			return fd.getRemotePath();
		// Shouldn't get here
		System.out.println("GOT null");
		return null;
	}

	public void modify(Object element, String property, Object value) {
		// element can be passed as an Item
		if (element instanceof Item) element = ((Item) element).getData();

		FileToDeploy fd = (FileToDeploy) element;
		if (CustomTableViewer.URL1.equals(property)) {
			fd.setLocalURL((String) value);
		} else if (CustomTableViewer.URL2.equals(property)) {
			fd.setRemoteURL((String) value);
		}

		// Force the viewer to refresh
		viewer.refresh();
	}
}

