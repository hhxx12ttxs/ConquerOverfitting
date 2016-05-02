/*******************************************************************************
 * Copyright (c) 2013 Max G??bel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Max G??bel - initial API and implementation
 ******************************************************************************/
package at.tuwien.prip.annotator.views.anno;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import at.tuwien.prip.annotator.Activator;
import at.tuwien.prip.annotator.DocWrapUIUtils;
import at.tuwien.prip.annotator.control.DocumentController;
import at.tuwien.prip.annotator.control.DocumentUpdate;
import at.tuwien.prip.annotator.control.DocumentUpdate.UpdateType;
import at.tuwien.prip.annotator.control.IModelChangedListener;
import at.tuwien.prip.annotator.control.ModelChangedEvent;
import at.tuwien.prip.annotator.editors.WrapperEditor;
import at.tuwien.prip.annotator.editors.annotator.DocWrapEditor;
import at.tuwien.prip.annotator.utils.ModelUtils2;
import at.tuwien.prip.annotator.utils.benchmark.DiademBenchmarkEngine;
import at.tuwien.prip.annotator.views.BenchmarkEditorInput;
import at.tuwien.prip.common.datastructures.HashMapList;
import at.tuwien.prip.common.datastructures.MapList;
import at.tuwien.prip.common.log.ErrorDump;
import at.tuwien.prip.common.utils.ListUtils;
import at.tuwien.prip.model.document.segments.CharSegment;
import at.tuwien.prip.model.document.segments.GenericSegment;
import at.tuwien.prip.model.document.segments.OpTuple;
import at.tuwien.prip.model.document.segments.TextSegment;
import at.tuwien.prip.model.document.segments.fragments.TextFragment;
import at.tuwien.prip.model.graph.DocEdge;
import at.tuwien.prip.model.graph.DocNode;
import at.tuwien.prip.model.graph.EdgeConstants;
import at.tuwien.prip.model.graph.ISegmentGraph;
import at.tuwien.prip.model.project.annotation.Annotation;
import at.tuwien.prip.model.project.annotation.AnnotationPage;
import at.tuwien.prip.model.project.annotation.AnnotationType;
import at.tuwien.prip.model.project.annotation.PdfInstructionContainer;
import at.tuwien.prip.model.project.annotation.TableAnnotation;
import at.tuwien.prip.model.project.annotation.TableCellContainer;
import at.tuwien.prip.model.project.document.DocumentModel;
import at.tuwien.prip.model.project.document.benchmark.Benchmark;
import at.tuwien.prip.model.project.document.benchmark.BenchmarkDocument;
import at.tuwien.prip.model.project.document.benchmark.PdfBenchmarkDocument;
import at.tuwien.prip.model.project.document.pdf.PdfDocumentPage;
import at.tuwien.prip.model.project.selection.AbstractSelection;
import at.tuwien.prip.model.project.selection.ExtractionResult;
import at.tuwien.prip.model.project.selection.LabelSelection;
import at.tuwien.prip.model.project.selection.MultiPageSelection;
import at.tuwien.prip.model.project.selection.PDFInstruction;
import at.tuwien.prip.model.project.selection.PdfSelection;
import at.tuwien.prip.model.project.selection.RegionSelection;
import at.tuwien.prip.model.project.selection.SinglePageSelection;
import at.tuwien.prip.model.project.selection.TableCell;
import at.tuwien.prip.model.project.selection.TableSelection;
import at.tuwien.prip.model.project.selection.TextSelection;
import at.tuwien.prip.model.utils.DocGraphUtils;

/**
 * AnnotationView.java
 * 
 * 
 * 
 * @author: Max Goebel <mcgoebel@gmail.com>
 * @date: Mar 16, 2011
 */
public class AnnotationView extends ViewPart 
implements 
ISelectionProvider,
ISelectionChangedListener, 
IModelChangedListener 
{

	public final int EXPAND_LEVEL = 3;

	public static String ID = "at.tuwien.prip.annotator.views.annotation";

	private TreeViewer annoViewer;

	private TreeViewer gtViewer;

	private PdfBenchmarkDocument document = null;

	//	boolean isDebugMode = false;

	// private Action saveToBenchmarkAction;
	private Action deleteItemAction;
	// private Action clearAction;
	//	private Button switchDebugModeButton;
	private Button createTableButton;
	private Button addResultButton;
	private Button addGTButton;
	private Button clearResultButton;
	private Button clearGTButton;
	private Button saveAnnotationButton;
	private Button findTableCellsMagicButton;

	private Text colText, rowText, colSpanText, rowSpanText, xDistanceThreshold, yDistanceThreshold;
	private Label colLabel, rowLabel, colSpanLabel, rowSpanLabel;
	private Button colMinusButton, colPlusButton, rowMinusButton, rowPlusButton;
	private Button guessButton;

	Composite colPanel;
	TabFolder cellFolder;

	private AnnotationType annotationType = AnnotationType.TABLE;

	private Combo combo1, combo2;

	/* needs saving? */
	boolean dirty = false;

	/**
	 * Constructor.
	 */
	public AnnotationView() {
		WrapperEditor we = DocWrapUIUtils.getWrapperEditor();
		if (we != null) {
			we.registerForSelection(this);
		}
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		Activator.modelControl.addModelChangedListener(this);

		/* BEGIN CONTENT */

		final TabFolder tabFolder = new TabFolder(parent, SWT.BORDER);

		/* TAB 1: Annotations */
		TabItem annoTab = new TabItem(tabFolder, SWT.NONE);
		annoTab.setText("Results");

		Composite annoPanel = new Composite(tabFolder, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		annoPanel.setLayout(layout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.heightHint = 400;
		annoPanel.setLayoutData(data);

		/* add a control panel at top */
		createAnnotationControlPanel(annoPanel);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);

		/* add the viewer at bottom */
		Composite viewerPanel = new Composite(annoPanel, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 10);
		viewerPanel.setLayout(new FillLayout());
		viewerPanel.setLayoutData(gd);
		viewerPanel.setEnabled(true);

		/* create a new tree viewer */
		annoViewer = createAnnoTreeViewer(viewerPanel);
		annoViewer.setAutoExpandLevel(EXPAND_LEVEL);
		annoViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionEnablement();

				TreeSelection tsel = (TreeSelection) event.getSelection();
				Object top = tsel.getFirstElement();
				if (top instanceof AbstractSelection)
				{
					DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
					int pageNum = editor.getCurrentPage();

					RegionSelection region = findRegionFromSelection((AbstractSelection) top, pageNum);
					if (region!=null)
					{
						editor.highlightBox(region.getBounds());
					}
					else
					{
						editor.highlightBox(null);
					}
					
					if (top instanceof RegionSelection)
					{
						combo2.setEnabled(true);
						combo2.select(1);
					}
					else if (top instanceof TableSelection)
					{
						combo2.setEnabled(true);
						combo2.select(0);
					}
					else
					{
						combo2.setEnabled(false);
					}
				}
			}
		});

		annoTab.setControl(annoPanel);

		/* TAB 2: Ground Truth */
		TabItem gtTab = new TabItem(tabFolder, SWT.NONE);
		gtTab.setText("Ground Truth");

		Composite gtPanel = new Composite(tabFolder, SWT.NONE);
		layout = new GridLayout(1, true);
		gtPanel.setLayout(layout);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.heightHint = 800;
		gtPanel.setLayoutData(data);

		/* create a control panel at the top */
		createGtControlPanel(gtPanel);
		// GridData gd2 = new GridData(SWT.FILL, SWT.TOP, true, false);

		/* add the viewer at bottom */
		Composite gtViewerPanel = new Composite(gtPanel, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 10);
		gtViewerPanel.setLayout(new FillLayout());
		gtViewerPanel.setLayoutData(gd);
		gtViewerPanel.setEnabled(true);

		/* create a new tree viewer */
		gtViewer = createGtTreeViewer(gtViewerPanel);
		gtViewer.setAutoExpandLevel(EXPAND_LEVEL);
		gtViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionEnablement();

				//set highlight
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				Object top = selection.getFirstElement();
				if (top instanceof AbstractSelection)
				{
					DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
					int pageNum = editor.getCurrentPage();

					RegionSelection region = findRegionFromSelection((AbstractSelection) top, pageNum);
					if (region!=null)
					{
						editor.highlightBox(region.getBounds());
					}
					else
					{
						editor.highlightBox(null);
					}
				}
			}
		});

		gtTab.setControl(gtPanel);

		createActions();
		createContextMenu();
	}

	/**
	 * Create the ground truth viewer.
	 * 
	 * @param parent
	 * @return
	 */
	private TreeViewer createGtTreeViewer(Composite parent) {
		final TreeViewer treeViewer = new TreeViewer(parent, SWT.BORDER);
		treeViewer.setContentProvider(new AnnotationViewContentProviderGT());
		treeViewer.setLabelProvider(new AnnotationViewLabelProvider());
		return treeViewer;
	}

	/**
	 * Create the annotation viewer.
	 * 
	 * @param parent
	 * @return
	 */
	private TreeViewer createAnnoTreeViewer(Composite parent) {
		final TreeViewer treeViewer = new TreeViewer(parent, SWT.BORDER);
		treeViewer.setContentProvider(new AnnotationViewContentProvider());
		treeViewer.setLabelProvider(new AnnotationViewLabelProvider());

		return treeViewer;
	}

	/**
	 * 
	 * @param selection
	 * @param pageNum
	 * @return
	 */
	private RegionSelection findRegionFromSelection (AbstractSelection selection, int pageNum)
	{
		RegionSelection result = null;

		if (selection instanceof RegionSelection)
		{
			RegionSelection region = (RegionSelection) selection;
			if (region.getPageNum()==pageNum)
			{
				result = region;
			}
		}
		else if (selection instanceof SinglePageSelection)
		{
			SinglePageSelection spSel = (SinglePageSelection) selection;
			if (spSel.getItems().size()==0)
			{
				if (spSel.getBounds()!=null)
				{
					result = new RegionSelection();
					result.setBounds(spSel.getBounds());
				}
			}
			else
			{
				for (AbstractSelection sel : spSel.getItems())
				{
					if (sel instanceof RegionSelection)
					{
						RegionSelection region = (RegionSelection) sel;
						if (region.getPageNum()==pageNum)
						{
							result = (RegionSelection) sel;
							break;
						}
					}
				}
			}
		}
		else if (selection instanceof MultiPageSelection)
		{
			MultiPageSelection mpSel = (MultiPageSelection) selection;
			for (AnnotationPage page : mpSel.getPages())
			{
				if (page.getPageNum()==pageNum)
				{
					for (AbstractSelection sel : page.getItems())
					{
						if (sel instanceof RegionSelection)
						{
							result = (RegionSelection) sel;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createGtControlPanel(final Composite parent)
	{
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(6, false);
		panel.setLayout(layout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.END;
		panel.setLayoutData(data);

		// /////////////////////////////////////////////////////////////
		// ROW 1
		//
		/* create a clear annotation button */
		Image clearImage = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_CLEAR);
		clearGTButton = new Button(panel, SWT.PUSH);
		clearGTButton.setImage(clearImage);
		clearGTButton.setToolTipText("Clear current ground truth");
		clearGTButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				document.getGroundTruth().clear();
				gtViewer.setInput(document);
				gtViewer.setAutoExpandLevel(EXPAND_LEVEL);

				dirty = false;
				updateActionEnablement();

				//refresh canvas
				DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
				editor.highlightBox(null);
				editor.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = 1;
		data.verticalAlignment = SWT.END;
		clearGTButton.setLayoutData(data);
		clearGTButton.setEnabled(false);

		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = 1;
		data.verticalAlignment = SWT.END;
		addGTButton = new Button(panel, SWT.NONE);
		addGTButton.setToolTipText("Add ground truth");
		ImageDescriptor desc = Activator.getImageDescriptor("/icons/eclipseUI/obj16/add_obj.gif");
		addGTButton.setImage(desc.createImage());
		addGTButton.setLayoutData(data);
		addGTButton.setEnabled(false);
		addGTButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				// From a view you get the site which allow to get the service
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand("at.tuwien.prip.annotator.commands.addGroundTruth", null);
				} catch (Exception ex) {
					throw new RuntimeException("at.tuwien.prip.annotator.commands.addGroundTruth not found");
					// Give message
				}
				updateActionEnablement();

				WrapperEditor we = (WrapperEditor) Activator.getActiveEditor();
				DocumentModel model = we.documentControl.getDocModel();
				DocumentUpdate update = new DocumentUpdate();
				update.setType(UpdateType.DOCUMENT_CHANGED);
				update.setUpdate(model);
				we.documentControl.setDocumentUpdate(update);

			} 
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		panel.pack();
		return panel;
	}

	/**
	 * Create a control panel for the annotation type.
	 * 
	 * @param parent
	 * @return
	 */
	private Composite createAnnotationControlPanel(final Composite parent) 
	{
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		panel.setLayout(layout);
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		panel.setLayoutData(data);

		// /////////////////////////////////////////////////////////////
		// ROW 1
		//
		// add a create new table button
		createTableButton = new Button(panel, SWT.NONE);
		createTableButton.setText("Create new table");
		createTableButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				/* create a new benchmark document */
				BenchmarkDocument benchDoc = null;
				IEditorPart editor = DocWrapUIUtils.getActiveEditor();
				if (editor instanceof WrapperEditor) {
					WrapperEditor we = (WrapperEditor) editor;
					IEditorInput input = we.getEditorInput();
					if (input instanceof BenchmarkEditorInput) {
						BenchmarkEditorInput bei = (BenchmarkEditorInput) input;
						benchDoc = bei.getBenchmarkDocument();
					}
				}
				if (benchDoc == null) {
					MessageDialog.openError(parent.getShell(), "Error",
							"No benchmark document open");
					return;
				}

				AnnotationPage page = null;
				int pageNum = DocumentController.docModel.getPageNum();

				if (benchDoc instanceof PdfBenchmarkDocument) {
					page = findAnnotationPageInBenchmarkDocument(
							(PdfBenchmarkDocument) benchDoc, pageNum);
				}
				if (page == null) {
					// create a new annotation page
					page = new AnnotationPage();
					page.setPageNum(pageNum);
				}

				/* create a new table selection */
				TableSelection ts = new TableSelection();
				int id = getCounter(benchDoc, "table");
				ts.setId(id);
				ts.getPages().add(page);

				/* create a new table annotation */
				TableAnnotation ann = (TableAnnotation) ModelUtils2
						.findNamedAnnotation(benchDoc, AnnotationType.TABLE);
				if (ann == null) {
					ann = new TableAnnotation("");
					benchDoc.getAnnotations().add(ann);
				}
				ann.getItems().add(ts);

				annoViewer.setInput(benchDoc);
				
				if (ts!=null)
				{
					TreePath path = new TreePath(new Object[]{ts});
					TreeSelection treeSel = new TreeSelection(path);
					annoViewer.setSelection(treeSel);
				}
				
				//				annoViewer.refresh();

				dirty = true;
				updateActionEnablement();
			}
		});

		data = new GridData();
		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = false;
		createTableButton.setLayoutData(data);
		createTableButton.setEnabled(false);

		combo2 = new Combo(panel, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo2.setEnabled(false);
		combo2.removeAll();
		combo2.add("Region");
		combo2.add("Cell");
		combo2.select(0);
		combo2.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateActionEnablement();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/////////////////////////////////////////////////////////////////
		// ADD TAB FOLDER FOR CELLS
		cellFolder = new TabFolder(panel, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalSpan = 2;
		gridData.grabExcessVerticalSpace = false;
		gridData.heightHint = 90;
		cellFolder.setLayout(new GridLayout());
		cellFolder.setLayoutData(gridData);
		cellFolder.setVisible(false);
		
		TabItem magicTab = new TabItem(cellFolder, SWT.NONE);
		magicTab.setText("Automatic");
		
		Composite magicPanel = new Composite(cellFolder, SWT.BORDER);
		magicPanel.setLayout(new GridLayout(3,false));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		magicPanel.setLayoutData(gridData);
		
		guessButton = new Button(magicPanel, SWT.CHECK);
		guessButton.setSelection(true);
		gridData = new GridData();
		gridData.widthHint = 24;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;

		Label guessLabel = new Label(magicPanel, SWT.NONE);
		guessLabel.setText("guess column/row index");

		/* create a clear annotation button */
		ImageDescriptor wandImage = Activator.getImageDescriptor("/icons/annotator/obj16/wand.png");
		findTableCellsMagicButton = new Button(magicPanel, SWT.PUSH);
		findTableCellsMagicButton.setImage(wandImage.createImage());
		findTableCellsMagicButton.setToolTipText("Automatically find table cells");
		findTableCellsMagicButton.addSelectionListener(new SelectionListener() 
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				StructuredSelection ss = (StructuredSelection) getSelection();
				RegionSelection region = (RegionSelection) ss.getFirstElement();
				region.getCellContainer().getCells().clear(); //clear first

				findAllTableCells(region);

				annoViewer.refresh();

				dirty = false;
				updateActionEnablement();

				//refresh canvas
				DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
				editor.highlightBox(null);
				editor.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		data = new GridData();
		data.verticalAlignment = SWT.END;
		data.verticalSpan = 3;
		findTableCellsMagicButton.setLayoutData(data);
		findTableCellsMagicButton.setEnabled(false);
		
		xDistanceThreshold = new Text(magicPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 24;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		xDistanceThreshold.setTabs(2);
		xDistanceThreshold.setLayoutData(gridData);
		xDistanceThreshold.setTextLimit(3);
		xDistanceThreshold.setSize(30, 20);
		xDistanceThreshold.setText("5");
		xDistanceThreshold.setEditable(true);
		xDistanceThreshold.setEnabled(true);

		Label distLabel = new Label(magicPanel, SWT.NONE);	
		distLabel.setText("X Treshold");

		yDistanceThreshold = new Text(magicPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 24;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		yDistanceThreshold.setTabs(2);
		yDistanceThreshold.setLayoutData(gridData);
		yDistanceThreshold.setTextLimit(3);
		yDistanceThreshold.setSize(30, 20);
		yDistanceThreshold.setText("5");
		yDistanceThreshold.setEditable(true);
		yDistanceThreshold.setEnabled(true);

		Label distYLabel = new Label(magicPanel, SWT.NONE);	
		distYLabel.setText("Y Treshold");	

		magicTab.setControl(magicPanel);
		
		TabItem manualTab = new TabItem(cellFolder, SWT.NONE);
		manualTab.setText("Manual");
		
		colPanel = new Composite(cellFolder, SWT.BORDER);
		colPanel.setLayout(new GridLayout(6, false));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 60;
		colPanel.setData(gridData);

		colLabel = new Label(colPanel, SWT.NONE);
		colLabel.setText("Column:");

		colMinusButton = new Button(colPanel, SWT.PUSH);
		colMinusButton.setText("-");
		gridData = new GridData();
		gridData.widthHint = 21;
		gridData.heightHint = 21;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		colMinusButton.setLayoutData(gridData);

		colText = new Text(colPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 24;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		colText.setTabs(2);
		colText.setLayoutData(gridData);
		colText.setTextLimit(3);
		colText.setSize(30, 20);
		colText.setText("0");
		colText.setEditable(true);
		colText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

		colMinusButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int xoff = 0;
				String xoffText = colText.getText();
				if (xoffText.length()>0)
				{
					xoff = Integer.parseInt(colText.getText());
				}

				colText.setText(xoff - 1 + "");
			}
		});

		colPlusButton = new Button(colPanel, SWT.PUSH);
		colPlusButton.setText("+");
		gridData = new GridData();
		gridData.widthHint = 21;
		gridData.heightHint = 21;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		colPlusButton.setLayoutData(gridData);

		colPlusButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int xoff = 0;
				String xoffText = colText.getText();
				if (xoffText.length()>0)
				{
					xoff = Integer.parseInt(colText.getText());
				}
				colText.setText(xoff + 1 + "");
			}
		});

		colSpanLabel = new Label(colPanel, SWT.NONE);
		colSpanLabel.setText("Col Span:");

		colSpanText = new Text(colPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 14;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		colSpanText.setTabs(2);
		colSpanText.setLayoutData(gridData);
		colSpanText.setTextLimit(2);
		colSpanText.setSize(30, 20);
		colSpanText.setText("1");
		colSpanText.setEditable(true);
		colSpanText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

		rowLabel = new Label(colPanel, SWT.NONE);
		rowLabel.setText("Row:");

		rowMinusButton = new Button(colPanel, SWT.PUSH);
		rowMinusButton.setText("-");
		gridData = new GridData();
		gridData.widthHint = 21;
		gridData.heightHint = 21;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		rowMinusButton.setLayoutData(gridData);

		rowText = new Text(colPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 24;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		rowText.setTabs(2);
		rowText.setLayoutData(gridData);
		rowText.setTextLimit(3);
		rowText.setSize(30, 20);
		rowText.setText("0");
		rowText.setEditable(true);
		rowText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

		rowMinusButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int xoff = 0;
				String xoffText = rowText.getText();
				if (xoffText.length()>0)
				{
					xoff = Integer.parseInt(rowText.getText());
				}

				rowText.setText(xoff - 1 + "");
			}
		});

		rowPlusButton = new Button(colPanel, SWT.PUSH);
		rowPlusButton.setText("+");
		gridData = new GridData();
		gridData.widthHint = 21;
		gridData.heightHint = 21;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		rowPlusButton.setLayoutData(gridData);

		rowPlusButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				int xoff = 0;
				String xoffText = rowText.getText();
				if (xoffText.length()>0)
				{
					xoff = Integer.parseInt(rowText.getText());
				}
				rowText.setText(xoff + 1 + "");
			}
		});

		rowSpanLabel = new Label(colPanel, SWT.NONE);
		rowSpanLabel.setText("Row Span:");

		rowSpanText = new Text(colPanel, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 14;
		gridData.heightHint = 12;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		rowSpanText.setTabs(2);
		rowSpanText.setLayoutData(gridData);
		rowSpanText.setTextLimit(2);
		rowSpanText.setSize(30, 20);
		rowSpanText.setText("1");
		rowSpanText.setEditable(true);
		rowSpanText.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});
		
		manualTab.setControl(colPanel);
		
		/////////////////////////////////////////////////////////////////////////
		//ROW 2
		Composite buttonPanel = new Composite(panel, SWT.NONE);
		buttonPanel.setLayout(new GridLayout(4,true));
		data = new GridData();
		data.horizontalAlignment = SWT.RIGHT;
		buttonPanel.setLayoutData(data);

		/* create a clear annotation button */
		Image clearImage = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_CLEAR);
		clearResultButton = new Button(buttonPanel, SWT.PUSH);
		clearResultButton.setImage(clearImage);
		clearResultButton.setToolTipText("Clear all current annotations");
		clearResultButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				document.getAnnotations().clear();
				annoViewer.setInput(document);
				annoViewer.setAutoExpandLevel(EXPAND_LEVEL);

				dirty = false;
				updateActionEnablement();

				//refresh canvas
				DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
				editor.highlightBox(null);
				editor.refresh();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = 1;
		data.verticalAlignment = SWT.END;
		clearResultButton.setLayoutData(data);
		clearResultButton.setEnabled(false);

		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.horizontalSpan = 1;
		data.verticalAlignment = SWT.END;
		addResultButton = new Button(buttonPanel, SWT.NONE);
		addResultButton.setToolTipText("Add result");
		ImageDescriptor desc = Activator.getImageDescriptor("/icons/eclipseUI/obj16/add_obj.gif");
		addResultButton.setImage(desc.createImage());
		addResultButton.setLayoutData(data);
		addResultButton.setEnabled(false);
		addResultButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {


				// From a view you get the site which allow to get the service
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand("at.tuwien.prip.annotator.commands.addAnnotation", null);
				} catch (Exception ex) {
					throw new RuntimeException("at.tuwien.prip.annotator.commands.addAnnotation not found");
					// Give message
				}
			} 
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		/* create a save annotation button */
		Image saveImage = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_SAVE_EDIT);
		saveAnnotationButton = new Button(buttonPanel, SWT.PUSH);
		saveAnnotationButton.setImage(saveImage);
		saveAnnotationButton.setToolTipText("Save current annotation");
		saveAnnotationButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// open dialog
				String fileName = document.getUri();
				fileName = fileName.replaceFirst("\\.pdf", ".xml");
				String[] parts = fileName.split("/");

				FileDialog dialog = new FileDialog(getSite().getShell(),
						SWT.SAVE);
				dialog.setFilterNames(new String[] { "XML Files",
				"All Files (*.*)" });
				dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
				// dialog.setFilterPath(BenchmarkEngine.getBenchmarkRootDirectory());
				dialog.setFileName("bm_" + parts[parts.length - 1]);// "datafile.xml");

				fileName = null;
				fileName = dialog.open();
				ErrorDump.debug(this, "Saving to " + fileName);

				if (fileName != null) {
					if (new java.io.File(fileName).exists()) {
						MessageBox messageBox = new MessageBox(getSite()
								.getShell(), SWT.ICON_WARNING | SWT.OK
								| SWT.CANCEL);

						messageBox.setText("Warning");
						messageBox.setMessage("File exists. Overwrite?");
						int buttonID = messageBox.open();
						switch (buttonID) {
						case SWT.OK:
							// saves changes ...
							break;
						case SWT.CANCEL:
							return;
						}
					}

					//fix row and cols
					if (guessButton.getSelection())
					{
						for (Annotation ann : document.getAnnotations())
						{
							if (ann instanceof TableAnnotation)
							{
								TableAnnotation tableAnn = (TableAnnotation) ann;
								for (AbstractSelection sel : tableAnn.getItems())
								{
									if (sel instanceof TableSelection)
									{
										TableSelection tsel = (TableSelection) sel;
										for (AnnotationPage page : tsel.getPages())
										{
											for (AbstractSelection as : page.getItems())
											{
												if (as instanceof RegionSelection)
												{
													RegionSelection region = (RegionSelection) as;
													guessRowColIndex(region);
												}
											}
										}
									}
								}
							}
						}
					}

					// OK, save to file
					DiademBenchmarkEngine.writeTableBenchmark(
							document,
							fileName);

					ErrorDump
					.debug(this, fileName + " written successfully...");
					dirty = false;
				}

				updateActionEnablement();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		data = new GridData();
		data.grabExcessHorizontalSpace = false;
		data.verticalAlignment = SWT.END;
		saveAnnotationButton.setLayoutData(data);
		saveAnnotationButton.setEnabled(false);

		panel.pack();
		return panel;
	}

	private void createActions()
	{
		deleteItemAction = new Action("Delete") {
			public void run() {
				deleteItem();
			}
		};
		Image deleteImage = PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_ETOOL_DELETE);
		deleteItemAction.setImageDescriptor(ImageDescriptor
				.createFromImage(deleteImage));
	}

	/**
	 * 
	 * @param table
	 */
	private void guessRowColIndex (RegionSelection region)
	{
		List<Cluster> clusters = new ArrayList<AnnotationView.Cluster>();
		for (TableCell n : region.getCellContainer().getCells())
		{
			clusters.add(new Cluster(n));
		}
		
		//find column and row centers
		List<Cluster> columnClusters = new ArrayList<Cluster>();
		for (Cluster n : clusters)
		{
			Rectangle bounds = n.bounds;
			int x1 = bounds.x;
			int x2 = bounds.x + bounds.width;
			if (columnClusters.size()==0)
			{
				columnClusters.add(n);
			}
			else
			{
				Cluster col = null;
				boolean remove = false;
				Iterator<Cluster> colit = columnClusters.iterator();
				while( colit.hasNext() )
				{
					col = colit.next();
					int colX1 = col.bounds.x;
					int colX2 = col.bounds.x + col.bounds.width;
					if ((colX1<=x1 && x1<=colX2) || (x1<=colX1 && colX1<=x2))
					{
						//merge
						columnClusters.add(new Cluster(col, n));
						remove = true;
						break;
					}
				}
				if (remove)
				{
					columnClusters.remove(col);
				}
				else 
				{
					columnClusters.add(n);
				}
			}	
		}

		//find column and row centers
		List<Cluster> rowClusters = new ArrayList<Cluster>();
		for (Cluster n : clusters)
		{
			Rectangle bounds = n.bounds;
			int y1 = bounds.y;
			int y2 = bounds.y + bounds.height;
			if (rowClusters.size()==0)
			{
				rowClusters.add(n);
			}
			else
			{
				Cluster row = null;
				boolean remove = false;
				Iterator<Cluster> rowit = rowClusters.iterator();
				while( rowit.hasNext() )
				{
					row = rowit.next();
					int rowY1 = row.bounds.y;
					int rowY2 = row.bounds.y + row.bounds.height;
					if ((rowY1<=y1 && y1<=rowY2) || (y1<=rowY1 && rowY1<=y2))
					{
						//merge
						rowClusters.add(new Cluster(row, n));
						remove = true;
						break;
					}
				}
				if (remove)
				{
					rowClusters.remove(row);
				}
				else 
				{
					rowClusters.add(n);
				}
			}	
		}
		
		Collections.sort(columnClusters, new Comparator<Cluster>() 
				{
			@Override
			public int compare(Cluster arg0, Cluster arg1) 
			{
				if ( arg0.bounds.x<arg1.bounds.x )
					return -1;
				else if ( arg0.bounds.x>arg1.bounds.x )
					return 1;

				return 0;
			}
				});
		for (TableCell cell : region.getCellContainer().getCells())
		{
			Rectangle bounds = cell.getBounds();
			int startCol = 0;
			for (Cluster col : columnClusters)
			{
				if (!col.bounds.contains(bounds))
				{
					startCol++;
				}
				else
					break;
			}

			Collections.sort(rowClusters, new Comparator<Cluster>() 
					{
				@Override
				public int compare(Cluster arg0, Cluster arg1) 
				{
					if ( arg0.bounds.y<arg1.bounds.y )
						return 1;
					else if ( arg0.bounds.y>arg1.bounds.y )
						return -1;

					return 0;
				}
					});
			int startRow = 0;
			for (Cluster row : rowClusters)
			{
				if (!row.bounds.contains(bounds))
				{
					startRow++;
				}
				else
					break;
			}
			
			cell.setStartCol(startCol);
			cell.setStartRow(startRow);
		}

	}

	/**
	 * 
	 */
	private void updateActionEnablement() 
	{
		IStructuredSelection sel = (IStructuredSelection) annoViewer
				.getSelection();
		deleteItemAction.setEnabled(sel.size() > 0);

		if (combo1!=null)
		{
			combo1.setEnabled(document!=null);
		}

		if (combo2!=null)
		{
			combo2.setEnabled(document!=null);
		}
		if (createTableButton!=null)
		{
			createTableButton.setEnabled(document!=null);
		}


		if (document != null) {
			//if (dirty) {

			if (findTableCellsMagicButton!=null)
			{
				findTableCellsMagicButton.setEnabled(false);
				cellFolder.setVisible(false);
				xDistanceThreshold.setEnabled(false);
				String item2 = combo2.getItem(combo2.getSelectionIndex());
				if (sel!=null && 
						sel.getFirstElement() instanceof RegionSelection)
				{
					findTableCellsMagicButton.setEnabled(true);
					xDistanceThreshold.setEnabled(true);
					cellFolder.setVisible(true);
				}
				if ("cell".equalsIgnoreCase(item2))
				{
					colLabel.setVisible(true);
					colPlusButton.setVisible(true);
					colMinusButton.setVisible(true);
					colText.setVisible(true);
					colSpanLabel.setVisible(true);
					colSpanText.setVisible(true);

					rowLabel.setVisible(true);
					rowPlusButton.setVisible(true);
					rowMinusButton.setVisible(true);
					rowText.setVisible(true);
					rowSpanLabel.setVisible(true);
					rowSpanText.setVisible(true);

					colPanel.setVisible(true);
				}
			}

			saveAnnotationButton.setEnabled(true);
			if (document.getAnnotations().size() > 0) {
				clearResultButton.setEnabled(true);
			} else {
				clearResultButton.setEnabled(false);
			}
			if (document.getGroundTruth().size() > 0) {
				addGTButton.setEnabled(false);
				clearGTButton.setEnabled(true);
			} else {
				addGTButton.setEnabled(true);
				clearGTButton.setEnabled(false);
			}
			addResultButton.setEnabled(true);
		} else {
			saveAnnotationButton.setEnabled(false);
			clearResultButton.setEnabled(false);
			addResultButton.setEnabled(false);
			addGTButton.setEnabled(false);
		}
	}

	/**
	 * 
	 */
	public void deleteItem() 
	{
		BenchmarkDocument benchDoc = (BenchmarkDocument) annoViewer.getInput();
		TreeSelection tsel = null;
		Object parent = null;

		IStructuredSelection sel = (IStructuredSelection) annoViewer
				.getSelection();
		if (sel instanceof TreeSelection) {
			tsel = (TreeSelection) sel;
			TreePath paths = tsel.getPathsFor(tsel.getFirstElement())[0];
			parent = paths.getParentPath().getLastSegment();
		}

		Iterator<?> iter = sel.iterator();
		while (iter.hasNext()) 
		{
			Object obj = iter.next();
			if (obj instanceof Annotation) {
				benchDoc.getAnnotations().remove(obj);
			} else if (obj instanceof AnnotationPage) {
				if (parent instanceof Annotation) {
					((Annotation) parent).getPages().remove(obj);
				} else if (parent instanceof TableSelection) {
					((TableSelection) parent).getPages().remove(obj);
				}
			} else if (obj instanceof ExtractionResult) {
				if (parent instanceof AnnotationPage) {
					((AnnotationPage) parent).getItems().remove(obj);
				}
			} else if (obj instanceof PdfSelection) {
				if (parent instanceof ExtractionResult) {
					// ((ExtractionResult)parent).getItems().remove(obj);
				} else if (parent instanceof LabelSelection) {
					// ((LabelSelection)parent).setSelection(null);
				}
			}
			else if (obj instanceof TableCell) 
			{
				if (parent instanceof TableCellContainer) {
					TableCellContainer cont = (TableCellContainer) parent;
					cont.getCells().remove(obj);
				}
			} 
			else if (obj instanceof AbstractSelection) 
			{
				if (parent instanceof AnnotationPage) 
				{
					((AnnotationPage) parent).getItems().remove(obj);
				}
				else if (parent instanceof Annotation)
				{
					Annotation ann = (Annotation) parent;
					ann.getItems().remove(obj);
				}
				else if (parent instanceof SinglePageSelection)
				{
					SinglePageSelection selection = (SinglePageSelection) parent;
					selection.getItems().remove(obj);
				}
				else if (parent instanceof MultiPageSelection)
				{
					MultiPageSelection selection = (MultiPageSelection) parent;
					for (AnnotationPage page : selection.getPages())
					{
						page.getItems().remove(obj);
					}
				}
			} 
			else if (obj instanceof RegionSelection) 
			{
				((AnnotationPage) parent).getItems().remove(obj);
			}
			else if (obj instanceof RegionSelection) 
			{
				if (parent instanceof AnnotationPage) {
					((AnnotationPage) parent).getItems().remove(obj);
				}
			}
			else if (obj instanceof TableSelection) 
			{
				if (parent instanceof TableAnnotation) {
					((TableAnnotation) parent).getTables().remove(obj);
				}
			}
		}

		WrapperEditor we = (WrapperEditor) Activator.getActiveEditor();
		DocumentModel model = we.documentControl.getDocModel();
		DocumentUpdate update = new DocumentUpdate();
		update.setType(UpdateType.DOCUMENT_CHANGED);
		update.setUpdate(model);
		we.documentControl.setDocumentUpdate(update);

		DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
		if (editor!=null)
		{
			editor.highlightBox(null);
		}

		annoViewer.refresh();
		dirty = true;
		updateActionEnablement();
	}

	/**
	 * 
	 */
	private void createContextMenu() {
		// Create menu manager.
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});

		// Create menu.
		annoViewer.getControl().setMenu(
				menuMgr.createContextMenu(annoViewer.getControl()));

		// Register menu for extension.
		getSite().registerContextMenu(menuMgr, annoViewer);
	}

	/**
	 * 
	 * @param mgr
	 */
	private void fillContextMenu(IMenuManager mgr) {
		// mgr.add(togglAnnotationType);
		// mgr.add(assignClassLabelAction);
		mgr.add(deleteItemAction);
	}

	@Override
	public void setFocus() {
		annoViewer.getControl().setFocus();
	}

	/**
	 * Set the benchmark document to be displayed.
	 * 
	 * If a previous benchmark document exists, the algorithm tries to merge the
	 * two benchmark documents.
	 * 
	 * @param benchDoc
	 * @param replace
	 */
	public void setBenchmarkDocument(BenchmarkDocument document,
			boolean replace, boolean force) {
		this.document = (PdfBenchmarkDocument) document;

		// check if we have to merge annotations...
		if (annoViewer.getInput() == null || replace) {
			// benchmark.getDocuments().clear();
			// benchmark.getDocuments().add(benchDoc);
			annoViewer.setInput(document);
			annoViewer.setAutoExpandLevel(EXPAND_LEVEL);
			return;
		}

		dirty = true;
		annoViewer.refresh();
		annoViewer.setAutoExpandLevel(EXPAND_LEVEL);
		updateActionEnablement();
	}

	private int getCounter(AbstractSelection selection, String type)
	{
		int counter = 0;
		if ("table".equalsIgnoreCase(type))
		{
			if (selection instanceof MultiPageSelection)
			{

			}
		}

		if ("cell".equalsIgnoreCase(type))
		{
			if (selection instanceof RegionSelection)
			{
				return ((RegionSelection) selection).getCellContainer().getCells().size();
			}
		}

		return counter;
	}

	/**
	 * 
	 * @param bdoc
	 * @param type
	 * @return
	 */
	private int getCounter(BenchmarkDocument bdoc, String type) {
		int counter = 0;
		List<Annotation> annos = bdoc.getAnnotations();
		for (Annotation ann : annos) {
			if (ann instanceof TableAnnotation) {
				if ("table".equals(type)) {
					counter = 0; // reset table counter
				}
				TableAnnotation tann = (TableAnnotation) ann;
				List<TableSelection> selections = tann.getTables();
				for (TableSelection tsel : selections) {

					if ("table".equals(type)) {
						counter++;
					} else {
						List<AnnotationPage> pages = tsel.getPages();
						for (AnnotationPage page : pages) {
							if ("region".equals(type)) {
								counter = 0; // reset region counter
							}
							List<AbstractSelection> regions = page.getItems();
							for (AbstractSelection selection : regions) {
								if ("cell".equals(type)) {
									counter = 0; // reset cell counter
								}

								if ("region".equals(type)) {
									counter++;
								} else {
									RegionSelection region = (RegionSelection) selection;
									List<TableCell> cells = region
											.getCellContainer().getCells();
									for (TableCell cell : cells) {
										cell.getBounds();
										if ("cell".equals(type)) {
											counter++;
										}
									}
								}
							}
						}

					}
				}
			}
		}
		return counter;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (annoViewer != null) {
			annoViewer.addSelectionChangedListener(listener);
		}
	}

	@Override
	public ISelection getSelection() {
		return annoViewer.getSelection();
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		if (annoViewer != null) {
			annoViewer.removeSelectionChangedListener(listener);
		}
	}

	@Override
	public void setSelection(ISelection selection) {

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) 
	{
		if (event.getSelection() != null
				&& event.getSelection() instanceof IStructuredSelection)
		{
			IStructuredSelection selection = 
					(IStructuredSelection) event.getSelection();
			Object obj = selection.getFirstElement();
			List<OpTuple> pdfOperators = new ArrayList<OpTuple>();
			List<AbstractSelection> sels = new ArrayList<AbstractSelection>();
			Rectangle bounds = null;

			// get current benchmark item
			PdfBenchmarkDocument document = null;
			IEditorPart editor = DocWrapUIUtils.getActiveEditor();
			if (!(editor instanceof WrapperEditor))
			{
				ErrorDump.debug(this, "Cannot parse editor");
			}

			WrapperEditor we = (WrapperEditor) editor;
			document = (PdfBenchmarkDocument) we.getActiveDocument();

			if (document != null)
			{
				int pageNum = we.getCurrentDocumentPageNum();
				PdfDocumentPage page = document.getPage(pageNum);
				ISegmentGraph dg = page.getGraph();
				if (dg == null) {
					return;
				}

				Object parent = null;

				if (obj instanceof org.eclipse.swt.graphics.Rectangle) 
				{
					org.eclipse.swt.graphics.Rectangle rect = (org.eclipse.swt.graphics.Rectangle) obj;
					Rectangle rectangle = new Rectangle(
							rect.x,
							rect.y,
							rect.width, 
							rect.height);

					if (getSelection() != null) 
					{
						// extract sub graph
						rectangle = DocGraphUtils.yFlipRectangle(rectangle, dg);

						//get subgraph
						ISegmentGraph sub = DocGraphUtils.getDocumentSubgraphUnderRectangle(dg,	rectangle, false);
						bounds = sub.getDimensions();

						MapList<OpTuple, TextSegment> op2gsMap = new HashMapList<OpTuple, TextSegment>();
						for (DocNode node : sub.getNodes())
						{
							GenericSegment gs = sub.getNodeSegHash().get(node);
							List<OpTuple> ops = gs.getOperators();
							if (gs instanceof CharSegment) 
							{
								OpTuple op = ((CharSegment) gs).getSourceOp();
								pdfOperators.add(op);
								op2gsMap.putmore(op, (CharSegment) gs);
							}
							else if (gs instanceof TextFragment) 
							{
								if (ops != null && ops.size() == 1)
								{
									OpTuple op = ops.get(0);
									pdfOperators.add(op);
									op2gsMap.putmore(op, (TextFragment) gs);
								} 
							}
							else if (gs instanceof TextSegment)
							{
								if (ops != null && ops.size() == 1) 
								{
									OpTuple op = ops.get(0);
									pdfOperators.add(op);
									op2gsMap.putmore(op, (TextSegment) gs);
								}
							}
						}
						ListUtils.unique(pdfOperators);
						if (pdfOperators.size() == 0)
						{
							MessageDialog
							.openInformation(combo2.getShell(),
									"Information",
									"No valid selection (no corresponding PDF operators selected).");
							return;
						}

						PdfSelection pdfSel = new PdfSelection();
						for (OpTuple opTuple : pdfOperators) 
						{
							PDFInstruction idx = new PDFInstruction(
									opTuple.getOpIndex(), 
									opTuple.getArgIndex());
							Rectangle region = null;
							StringBuffer txt = new StringBuffer();
							for (TextSegment ts : op2gsMap.get(opTuple))
							{

								txt.append(ts.getText());
								if (region == null) {
									region = ts.getBoundingRectangle();
								} else {
									region = region.union(ts
											.getBoundingRectangle());
								}
							}
							String tmp = DocGraphUtils.getTextUnderRegion(region, sub);
							idx.setText(tmp);//txt.toString()
							idx.setBounds(region);
							pdfSel.getInstructions().add(idx);
						}
						pdfSel.setBounds(bounds);

						//compute text
						//						DocGraphUtils.flipReverseDocGraph(dg);
						String text = DocGraphUtils.getTextUnderRegion(rectangle, dg);
						pdfSel.setText(text);
						sels.add(pdfSel);

						// determine parent
						ITreeSelection ts = (ITreeSelection) getSelection();
						Object o = ts.getFirstElement();
						int type = combo2.getSelectionIndex();
						if (type == 0) { // region
							if (o instanceof TableSelection) {
								parent = o;
							} else {
								MessageDialog
								.openInformation(combo2.getShell(),
										"Information",
										"No parent selected. Please select a parent 'Table' in the Annotation View.");
							}
						} else if (type == 1) { // cell
							if (o instanceof RegionSelection) {
								parent = o;
							} else {
								MessageDialog
								.openInformation(combo2.getShell(),
										"Information",
										"No parent selected. Please select a parent 'Region' in the Annotation View.");
							}
						}
					}
				}

				/* create a new annotation page */
				AnnotationPage annoPage = new AnnotationPage();
				annoPage.setPageNum(pageNum);

				/* table annotation */
				if (annotationType == AnnotationType.TABLE) 
				{
					if (parent != null) 
					{
						if (parent instanceof TableSelection)
						{
							int id = getCounter(document, "region");

							AnnotationPage p = findPageFromTableSelection(
									(TableSelection) parent, pageNum);
							if (p == null) {
								p = new AnnotationPage();
								p.setPageNum(pageNum);
								((TableSelection) parent).getPages().add(p);
							}

							RegionSelection regionSelection = new RegionSelection();
							regionSelection.setId(id);
							regionSelection.setPageNum(pageNum);
							regionSelection.setBounds(bounds);
							
							for (AbstractSelection sel : sels)
							{
								if (sel instanceof PdfSelection)
								{	
									regionSelection.getInstructionContainer().addAll(
											((PdfSelection) sel).getInstructions());

									TextSelection tsel = new TextSelection();
									tsel.setText(((PdfSelection) sel).getText());
									tsel.setBounds(sel.getBounds());
									regionSelection.setText(tsel);
								}
								else if (sel instanceof TextSelection) 
								{
									regionSelection
									.setText((TextSelection) sel);
								}
							}
							p.getItems().add(regionSelection);

							((TableSelection) parent).setBounds(bounds);
							annoViewer.refresh();
						} 
						else if (parent instanceof RegionSelection)
						{
							int id = getCounter((AbstractSelection) parent, "cell");

							RegionSelection reg = (RegionSelection) parent;
							TableCell cellSelection = new TableCell(id);
							int colStart = Integer.parseInt(colText.getText());
							cellSelection.setStartCol(colStart);
							int colSpan = Integer.parseInt(colSpanText.getText());
							if (colSpan>1)
							{
								cellSelection.setEndCol(colStart + colSpan-1);
							}

							int rowStart = Integer.parseInt(rowText.getText());
							cellSelection.setStartRow(rowStart);
							int rowSpan = Integer.parseInt(rowSpanText.getText());
							if (rowSpan>1)
							{
								cellSelection.setEndRow(rowStart + rowSpan-1);
							}

							cellSelection.setBounds(bounds);
							for (AbstractSelection sel : sels) {
								if (sel instanceof PdfSelection) {
									cellSelection.getInstructions().addAll(
											((PdfSelection) sel)
											.getInstructions());
									cellSelection
									.setContent(((PdfSelection) sel)
											.getText());
								} else if (sel instanceof TextSelection) {
									cellSelection
									.setContent(((TextSelection) sel)
											.getText());
								}
							}
							reg.getCellContainer().add(cellSelection);
							annoViewer.refresh();
						}
					}
				}

//				//search for existing annotations.
//				Annotation ann = ModelUtils2.findNamedAnnotation(document,
//						annotationType);
//				if (ann == null) 
//				{
//					ann = new TableAnnotation(document.getUri());
//					document.getAnnotations().add(ann);
//				}
//				if (ann != null) 
//				{
//					
////					ann.getPages().add(annoPage);
//					setBenchmarkDocument(document, false, false);
//				}
//				document = (PdfBenchmarkDocument) we.getActiveDocument();
//				System.out.println();
			}
//			annoViewer.setInput(document);
		}
		dirty = true;
		updateActionEnablement();

		//refresh canvas
		DocWrapEditor docWrapEditor = DocWrapUIUtils.getDocWrapEditor();
		docWrapEditor.refresh();
	}

	class Cluster 
	{
		List<Object> nodes;

		Rectangle bounds;

		public Cluster(DocNode node)
		{
			this.nodes = new ArrayList<Object>();
			nodes.add(node);

			this.bounds = node.getBoundingBox().getBounds();
		}

		public Cluster(TableCell cell)
		{
			this.nodes = new ArrayList<Object>();
			nodes.add(cell);

			this.bounds = cell.getBounds();
		}

		public Cluster(Cluster a, Cluster b)
		{
			//merge two clusters
			this.nodes = new ArrayList<Object>();
			if (a!=null)
			{
				this.nodes.addAll(a.nodes);
			}

			if (b!=null)
			{
				for (Object bn : b.nodes)
				{
					if (!this.nodes.contains(bn))
					{
						this.nodes.add(bn);
					}
				}			
			}

			if (a!=null && b!=null)
			{
				this.bounds = a.bounds.union(b.bounds);
			}
		}

		@Override
		public boolean equals(Object obj) 
		{
			if (obj instanceof Cluster)
			{
				Cluster other = (Cluster) obj;

				return (this.nodes.equals(other.nodes));
//				if (this.nodes.size()==other.nodes.size())
//				{
//					List<Object> disjoint = new ArrayList<Object>(this.nodes);
//					disjoint.removeAll(other.nodes);
//					return disjoint.isEmpty();
//				}
			}
			return false;
		}

	}

	/**
	 * 
	 * @param region
	 */
	private void findAllTableCells(final RegionSelection region) 
	{
		Rectangle rect = region.getBounds();

		//flip
		ISegmentGraph dg = DocumentController.docModel.getDocumentGraph();
		rect = DocGraphUtils.yFlipRectangle(rect, dg);
		ISegmentGraph sub =
				DocGraphUtils.getDocumentSubgraphUnderRectangle(dg, rect, true);

		List<TableCell> cells = new ArrayList<TableCell>();


		Map<Object,Cluster> clusterList = new HashMap<Object, Cluster>();
		for (DocNode n : sub.getNodes())
		{
			clusterList.put(n, new Cluster(n));
		}

		for (DocNode n : sub.getNodes())
		{
			List<DocEdge> edges = sub.getEdgesFrom(n);
			if (edges!=null) 
			{
				for (DocEdge e : edges) 
				{
					DocNode left = null, right = null;

					if (e.getTo().getSegX1()<e.getFrom().getSegX1())
					{
						left = e.getTo();
						right = e.getFrom();
					}
					else
					{
						right = e.getTo();
						left = e.getFrom();
					}

					if (e.getRelation().equals(EdgeConstants.ADJ_RIGHT)) 
					{
						float dist = Math.abs(left.getSegX2()-right.getSegX1());
						int xthreshold = Integer.parseInt(xDistanceThreshold.getText());
						if (dist<xthreshold) 
						{
							clusterList.get(n);

							//merge clusters
							Cluster a = clusterList.get(left);
							Cluster b = clusterList.get(right);

							if (a==null)
								a = new Cluster(left);

							if (b==null)
								b = new Cluster(right);

							Cluster c = new Cluster(a,b);


							for (Object an : a.nodes)
							{
								clusterList.put(an, c);
							}

							for (Object bn : b.nodes)
							{
								clusterList.put(bn, c);
							}
						}
						break;
					}
					else if (e.getRelation().equals(EdgeConstants.ADJ_BELOW)) 
					{
						float dist = Math.abs(left.getSegY2()-right.getSegY1());
						int ythreshold = Integer.parseInt(yDistanceThreshold.getText());
						if (dist<ythreshold) 
						{
							clusterList.get(n);

							//merge clusters
							Cluster a = clusterList.get(left);
							Cluster b = clusterList.get(right);

							if (a==null)
								a = new Cluster(left);

							if (b==null)
								b = new Cluster(right);

							Cluster c = new Cluster(a,b);


							for (Object an : a.nodes)
							{
								clusterList.put(an, c);
							}

							for (Object bn : b.nodes)
							{
								clusterList.put(bn, c);
							}
						}
						break;
					}
				}
			}
		}

		Collection<Cluster> clusters = clusterList.values();
		ListUtils.unique(clusters);

		//find column centers
		List<Cluster> columnClusters = new ArrayList<Cluster>();
		for (Cluster n : clusters)
		{
			Rectangle bounds = n.bounds;
			int x1 = bounds.x;
			int x2 = bounds.x + bounds.width;
			if (columnClusters.size()==0)
			{
				columnClusters.add(n);
			}
			else
			{
				Cluster col = null;
				boolean remove = false;
				Iterator<Cluster> colit = columnClusters.iterator();
				while( colit.hasNext() )
				{
					col = colit.next();
					int colX1 = col.bounds.x;
					int colX2 = col.bounds.x + col.bounds.width;
					if ((colX1<=x1 && x1<=colX2) || (x1<=colX1 && colX1<=x2))
					{
						//merge
						columnClusters.add(new Cluster(col, n));
						remove = true;
						break;
					}
				}
				if (remove)
				{
					columnClusters.remove(col);
				}
				else 
				{
					columnClusters.add(n);
				}
			}	
		}

		//find row centers
		int ythreshold = Integer.parseInt(yDistanceThreshold.getText());
		List<Cluster> rowClusters = new ArrayList<Cluster>();
		for (Cluster n : clusters)
		{
			Rectangle bounds = n.bounds;
			int y1 = bounds.y;
			int y2 = bounds.y + bounds.height;
			if (rowClusters.size()==0)
			{
				rowClusters.add(n);
			}
			else
			{
				Cluster row = null;
				boolean remove = false;
				Iterator<Cluster> rowit = rowClusters.iterator();
				while( rowit.hasNext() )
				{
					row = rowit.next();
					int rowY1 = row.bounds.y;
					int rowY2 = row.bounds.y + row.bounds.height;
					if ((rowY1<=y1-ythreshold && y1-ythreshold<=rowY2) || 
							(y1<=rowY1-ythreshold && rowY1-ythreshold<=y2))
					{
						//merge
						rowClusters.add(new Cluster(row, n));
						remove = true;
						break;
					}
				}
				if (remove)
				{
					rowClusters.remove(row);
				}
				else 
				{
					rowClusters.add(n);
				}
			}	
		}

		int idInc = 0;

		for (Cluster cluster : clusters)
		{
			Rectangle bounds = null;
			StringBuffer sb = new StringBuffer();
			PdfInstructionContainer container = new PdfInstructionContainer();

			for (Object obj : cluster.nodes)
			{
				if (obj instanceof DocNode)
				{
					DocNode node = (DocNode) obj;

					//set bounds
					if (bounds==null)
					{
						bounds = node.getBoundingBox().getBounds();
					}
					else
					{
						bounds = bounds.union(node.getBoundingBox().getBounds());
					}		

					sb.append(node.getSegText());

					//add PDF instructions
					GenericSegment gs = dg.getNodeSegHash().get(node);
					if (gs!=null)
					{
						List<OpTuple> tuples = gs.getOperators();
						for (OpTuple tuple : tuples)
						{
							PDFInstruction instr = new PDFInstruction(
									tuple.getOpIndex(), tuple.getArgIndex());
							container.add(instr);
						}
					}			
				}

			}

			Collections.sort(columnClusters, new Comparator<Cluster>() 
					{
				@Override
				public int compare(Cluster arg0, Cluster arg1) 
				{
					if ( arg0.bounds.x<arg1.bounds.x )
						return -1;
					else if ( arg0.bounds.x>arg1.bounds.x )
						return 1;

					return 0;
				}
					});
			int startCol = 0;
			for (Cluster col : columnClusters)
			{
				if (!col.bounds.contains(bounds))
				{
					startCol++;
				}
				else
					break;
			}

			Collections.sort(rowClusters, new Comparator<Cluster>() 
					{
				@Override
				public int compare(Cluster arg0, Cluster arg1) 
				{
					if ( arg0.bounds.y<arg1.bounds.y )
						return 1;
					else if ( arg0.bounds.y>arg1.bounds.y )
						return -1;

					return 0;
				}
					});
			int startRow = 0;
			for (Cluster row : rowClusters)
			{
				if (!row.bounds.contains(bounds))
				{
					startRow++;
				}
				else
					break;
			}

			//create new cell
			TableCell cell = new TableCell(idInc);
			cell.setBounds(bounds);

			String txt = "";
			//			String txt = DocGraphUtils.getTextUnderRegion(bounds, sub);
			txt = sb.toString();
			cell.setContent(txt);
			cell.setInstructions(container);

			RegionSelection reg = new RegionSelection();
			reg.setPageNum( DocumentController.docModel.getPageNum());
			reg.setBounds(bounds);
			TextSelection text = new TextSelection();
			text.setText(sb.toString());
			cell.getItems().add(text);
			cell.getItems().add(reg);
			cell.setStartCol(startCol);
			cell.setStartRow(startRow);
			cells.add(cell);

			idInc++;
		}

		MapList<Integer,TableCell> cellMap = new HashMapList<Integer, TableCell>();
		for (TableCell cell : cells)
		{
			cellMap.putmore(cell.getBounds().y, cell);
		}
		
		List<Integer> keys = new ArrayList<Integer>(cellMap.keySet());
		Collections.sort(keys);
		Collections.reverse(keys);
		
		List<TableCell> sortedCells = new ArrayList<TableCell>();
		int id = 0;
		int rowIndex = 0;
		for (int key : keys)
		{
			List<TableCell> row = cellMap.get(key);
			Collections.sort(row, new Comparator<TableCell>() {

				@Override
				public int compare(TableCell o1, TableCell o2) {
					if (o1.getBounds().x<o2.getBounds().x)
						return -1;
					else if (o1.getBounds().x>o2.getBounds().x)
						return 1;
					return 0;
				}
			});
			
			int colIndex = 0;
			for (TableCell cell : row)
			{
				cell.setStartCol(colIndex);
				cell.setStartRow(rowIndex);
				cell.setId(id);
				sortedCells.add(cell);
				
				colIndex++;
				id ++;
			}
			rowIndex++;
		}
		
//		//sort cell top to botton
//		Collections.sort(cells, new Comparator<TableCell>() 
//				{
//
//			@Override
//			public int compare(TableCell o1, TableCell o2) {
//				if (o1.getBounds().y<o2.getBounds().y)
//					return 1;
//				else if (o1.getBounds().y>o2.getBounds().y)
//					return -1;
//				else {
//					if (o1.getBounds().x<o2.getBounds().x)
//						return 1;
//					else if (o1.getBounds().x>o2.getBounds().x)
//						return -1;
//				}
//				return 0;
//			}
//		});
		
		region.setCellContainer(new TableCellContainer());
		region.getCellContainer().addAll(sortedCells);

		annoViewer.refresh();				
	}

	/**
	 * 
	 */
	public AnnotationPage findPageFromTableSelection(TableSelection tsel,
			int pageNum) {
		List<AnnotationPage> pages = tsel.getPages();
		for (AnnotationPage page : pages) {
			if (page.getPageNum() == pageNum) {
				return page;
			}
		}

		return null;
	}

	/**
	 * 
	 * @param b
	 * @param uri
	 * @return
	 */
	public BenchmarkDocument findBenchDoc(Benchmark b, String uri) {
		List<BenchmarkDocument> docs = b.getDocuments();
		for (BenchmarkDocument doc : docs) {
			if (doc.getUri().equals(uri)) {
				return doc;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param document
	 * @param pageNum
	 * @return
	 */
	public AnnotationPage findAnnotationPageInBenchmarkDocument(
			PdfBenchmarkDocument document, int pageNum) {
		for (AnnotationPage page : document.getAnnotationPages()) {
			if (page.getPageNum() == pageNum) {
				return page;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param b
	 * @param sel
	 * @return
	 */
	public BenchmarkDocument findBenchDocContaining(Benchmark b,
			AnnotationPage page) {
		List<BenchmarkDocument> docs = b.getDocuments();
		for (BenchmarkDocument doc : docs) {
			List<Annotation> annos = doc.getAnnotations();
			for (Annotation ann : annos) {
				if (ann instanceof TableAnnotation) {
					TableAnnotation tann = (TableAnnotation) ann;
					List<TableSelection> selections = tann.getTables();
					for (TableSelection table : selections) {
						List<AnnotationPage> pages = table.getPages();
						for (AnnotationPage p : pages) {
							if (p.equals(page)) {
								return doc;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param document
	 */
	public void setInput(BenchmarkDocument document) 
	{
		this.document = (PdfBenchmarkDocument) document;

		this.annoViewer.setInput(document);
		this.gtViewer.setInput(document);

		updateActionEnablement();

		//refresh canvas
		DocWrapEditor editor = DocWrapUIUtils.getDocWrapEditor();
		if (editor!=null)
		{
			editor.refresh();
		}
	}

	@Override
	public void modelChanged(ModelChangedEvent event) {

	}

}

