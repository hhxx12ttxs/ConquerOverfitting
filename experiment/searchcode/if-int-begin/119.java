/**
 * Copyright (c) 2008-2009 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *
 * $Id$
 */
package org.obeonetwork.dsl.overview.presentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.ui.MarkerHelper;
import org.eclipse.emf.common.ui.ViewerPane;
import org.eclipse.emf.common.ui.editor.ProblemEditorPart;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.obeonetwork.dsl.overview.OverviewModelDef;
import org.obeonetwork.dsl.overview.OverviewModelDefRegistry;

/**
 * This is an example of a Overview model editor. <!-- begin-user-doc -->
 * 
 * @implements ITabbedPropertySheetPageContributor <!-- end-user-doc -->
 * @generated NOT
 */
public abstract class OverviewEditor extends MultiPageEditorPart implements
		IEditingDomainProvider, ISelectionProvider, IMenuListener,
		IViewerProvider, IGotoMarker, ITabbedPropertySheetPageContributor {

	public static final String PROPERTIES_CONTRIBUTOR = "fr.obeo.dsl.viewpoint.diagram";

	/**
	 * This keeps track of the editing domain that is used to track all changes
	 * to the model. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AdapterFactoryEditingDomain editingDomain;

	/**
	 * This is the one adapter factory used for providing views of the model.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AdapterFactory adapterFactory;

	/**
	 * This is the content outline page. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected IContentOutlinePage contentOutlinePage;

	/**
	 * This is a kludge... <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected IStatusLineManager contentOutlineStatusLineManager;

	/**
	 * This is the content outline page's viewer. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer contentOutlineViewer;

	/**
	 * This is the property sheet page. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated NOT
	 */
	protected TabbedPropertySheetPage propertySheetPage;

	/**
	 * This is the viewer that shadows the selection in the content outline. The
	 * parent relation must be correctly defined for this to work. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer selectionViewer;

	/**
	 * This inverts the roll of parent and child in the content provider and
	 * show parents as a tree. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer parentViewer;

	/**
	 * This shows how a tree view works. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer treeViewer;

	/**
	 * This shows how a list view works. A list viewer doesn't support icons.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ListViewer listViewer;

	/**
	 * This shows how a table view works. A table can be used as a list with
	 * icons. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected TableViewer tableViewer;

	/**
	 * This shows how a tree view with columns works. <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer treeViewerWithColumns;

	/**
	 * This keeps track of the active viewer pane, in the book. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ViewerPane currentViewerPane;

	/**
	 * This keeps track of the active content viewer, which may be either one of
	 * the viewers in the pages or the content outline viewer. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Viewer currentViewer;

	/**
	 * This listens to which ever viewer is active. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected ISelectionChangedListener selectionChangedListener;

	/**
	 * This keeps track of all the
	 * {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are
	 * listening to this editor. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

	/**
	 * This keeps track of the selection of the editor as a whole. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ISelection editorSelection = StructuredSelection.EMPTY;

	/**
	 * The MarkerHelper is responsible for creating workspace resource markers
	 * presented in Eclipse's Problems View. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected MarkerHelper markerHelper = new EditUIMarkerHelper();

	/**
	 * This listens for when the outline becomes active <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected IPartListener partListener = new IPartListener() {
		@Override
		public void partActivated(final IWorkbenchPart p) {
			if (p instanceof ContentOutline) {
				if (((ContentOutline) p).getCurrentPage() == OverviewEditor.this.contentOutlinePage) {
					OverviewEditor.this.getActionBarContributor()
							.setActiveEditor(OverviewEditor.this);

					OverviewEditor.this
							.setCurrentViewer(OverviewEditor.this.contentOutlineViewer);
				}
			} else if (p instanceof PropertySheet) {
				if (((PropertySheet) p).getCurrentPage() == OverviewEditor.this.propertySheetPage) {
					OverviewEditor.this.getActionBarContributor()
							.setActiveEditor(OverviewEditor.this);
					OverviewEditor.this.handleActivate();
				}
			} else if (p == OverviewEditor.this) {
				OverviewEditor.this.handleActivate();
			}
		}

		@Override
		public void partBroughtToTop(final IWorkbenchPart p) {
			// Ignore.
		}

		@Override
		public void partClosed(final IWorkbenchPart p) {
			// Ignore.
		}

		@Override
		public void partDeactivated(final IWorkbenchPart p) {
			// Ignore.
		}

		@Override
		public void partOpened(final IWorkbenchPart p) {
			// Ignore.
		}
	};

	/**
	 * Resources that have been removed since last activation. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Collection<Resource> removedResources = new ArrayList<Resource>();

	/**
	 * Resources that have been changed since last activation. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Collection<Resource> changedResources = new ArrayList<Resource>();

	/**
	 * Resources that have been saved. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	protected Collection<Resource> savedResources = new ArrayList<Resource>();

	/**
	 * Map to store the diagnostic associated with a resource. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();

	/**
	 * Controls whether the problem indication should be updated. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected boolean updateProblemIndication = true;

	/**
	 * Adapter used to update the problem indication when resources are demanded
	 * loaded. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected EContentAdapter problemIndicationAdapter = new EContentAdapter() {
		@Override
		public void notifyChanged(final Notification notification) {
			if (notification.getNotifier() instanceof Resource) {
				switch (notification.getFeatureID(Resource.class)) {
				case Resource.RESOURCE__IS_LOADED:
				case Resource.RESOURCE__ERRORS:
				case Resource.RESOURCE__WARNINGS: {
					final Resource resource = (Resource) notification
							.getNotifier();
					final Diagnostic diagnostic = OverviewEditor.this
							.analyzeResourceProblems(resource, null);
					if (diagnostic.getSeverity() != Diagnostic.OK) {
						OverviewEditor.this.resourceToDiagnosticMap.put(
								resource, diagnostic);
					} else {
						OverviewEditor.this.resourceToDiagnosticMap
								.remove(resource);
					}

					if (OverviewEditor.this.updateProblemIndication) {
						OverviewEditor.this.getSite().getShell().getDisplay()
								.asyncExec(new Runnable() {
									@Override
									public void run() {
										OverviewEditor.this
												.updateProblemIndication();
									}
								});
					}
					break;
				}
				}
			} else {
				super.notifyChanged(notification);
			}
		}

		@Override
		protected void setTarget(final Resource target) {
			this.basicSetTarget(target);
		}

		@Override
		protected void unsetTarget(final Resource target) {
			this.basicUnsetTarget(target);
		}
	};

	/**
	 * This listens for workspace changes. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(final IResourceChangeEvent event) {
			// Only listening to these.
			// if (event.getType() == IResourceDelta.POST_CHANGE)
			{
				final IResourceDelta delta = event.getDelta();
				try {
					class ResourceDeltaVisitor implements IResourceDeltaVisitor {
						protected ResourceSet resourceSet = OverviewEditor.this.editingDomain
								.getResourceSet();
						protected Collection<Resource> changedResources = new ArrayList<Resource>();
						protected Collection<Resource> removedResources = new ArrayList<Resource>();

						@Override
						public boolean visit(final IResourceDelta delta) {
							if ((delta.getFlags() != IResourceDelta.MARKERS)
									&& (delta.getResource().getType() == IResource.FILE)) {
								if ((delta.getKind() & (IResourceDelta.CHANGED | IResourceDelta.REMOVED)) != 0) {
									final Resource resource = this.resourceSet
											.getResource(URI.createURI(delta
													.getFullPath().toString()),
													false);
									if (resource != null) {
										if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
											this.removedResources.add(resource);
										} else if (!OverviewEditor.this.savedResources
												.remove(resource)) {
											this.changedResources.add(resource);
										}
									}
								}
							}

							return true;
						}

						public Collection<Resource> getChangedResources() {
							return this.changedResources;
						}

						public Collection<Resource> getRemovedResources() {
							return this.removedResources;
						}
					}

					final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
					delta.accept(visitor);

					if (!visitor.getRemovedResources().isEmpty()) {
						OverviewEditor.this.removedResources.addAll(visitor
								.getRemovedResources());
						if (!OverviewEditor.this.isDirty()) {
							OverviewEditor.this.getSite().getShell()
									.getDisplay().asyncExec(new Runnable() {
										@Override
										public void run() {
											OverviewEditor.this
													.getSite()
													.getPage()
													.closeEditor(
															OverviewEditor.this,
															false);
											OverviewEditor.this.dispose();
										}
									});
						}
					}

					if (!visitor.getChangedResources().isEmpty()) {
						OverviewEditor.this.changedResources.addAll(visitor
								.getChangedResources());
						if (OverviewEditor.this.getSite().getPage()
								.getActiveEditor() == OverviewEditor.this) {
							OverviewEditor.this.getSite().getShell()
									.getDisplay().asyncExec(new Runnable() {
										@Override
										public void run() {
											OverviewEditor.this
													.handleActivate();
										}
									});
						}
					}
				} catch (final CoreException exception) {
					OverviewEditorPlugin.INSTANCE.log(exception);
				}
			}
		}
	};

	/**
	 * Handles activation of the editor or it's associated views. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void handleActivate() {
		// Recompute the read only state.
		//
		if (this.editingDomain.getResourceToReadOnlyMap() != null) {
			this.editingDomain.getResourceToReadOnlyMap().clear();

			// Refresh any actions that may become enabled or disabled.
			//
			this.setSelection(this.getSelection());
		}

		if (!this.removedResources.isEmpty()) {
			if (this.handleDirtyConflict()) {
				this.getSite().getPage()
						.closeEditor(OverviewEditor.this, false);
				OverviewEditor.this.dispose();
			} else {
				this.removedResources.clear();
				this.changedResources.clear();
				this.savedResources.clear();
			}
		} else if (!this.changedResources.isEmpty()) {
			this.changedResources.removeAll(this.savedResources);
			this.handleChangedResources();
			this.changedResources.clear();
			this.savedResources.clear();
		}
	}

	/**
	 * Handles what to do with changed resources on activation. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void handleChangedResources() {
		if (!this.changedResources.isEmpty()
				&& (!this.isDirty() || this.handleDirtyConflict())) {
			this.editingDomain.getCommandStack().flush();

			this.updateProblemIndication = false;
			for (final Resource resource : this.changedResources) {
				if (resource.isLoaded()) {
					resource.unload();
					try {
						resource.load(Collections.EMPTY_MAP);
					} catch (final IOException exception) {
						if (!this.resourceToDiagnosticMap.containsKey(resource)) {
							this.resourceToDiagnosticMap.put(resource, this
									.analyzeResourceProblems(resource,
											exception));
						}
					}
				}
			}
			this.updateProblemIndication = true;
			this.updateProblemIndication();
		}
	}

	/**
	 * Updates the problems indication with the information described in the
	 * specified diagnostic. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void updateProblemIndication() {
		if (this.updateProblemIndication) {
			final BasicDiagnostic diagnostic = new BasicDiagnostic(
					Diagnostic.OK, "org.obeonetwork.dsl.overview.editor", 0,
					null, new Object[] { this.editingDomain.getResourceSet() });
			for (final Diagnostic childDiagnostic : this.resourceToDiagnosticMap
					.values()) {
				if (childDiagnostic.getSeverity() != Diagnostic.OK) {
					diagnostic.add(childDiagnostic);
				}
			}

			int lastEditorPage = this.getPageCount() - 1;
			if ((lastEditorPage >= 0)
					&& (this.getEditor(lastEditorPage) instanceof ProblemEditorPart)) {
				((ProblemEditorPart) this.getEditor(lastEditorPage))
						.setDiagnostic(diagnostic);
				if (diagnostic.getSeverity() != Diagnostic.OK) {
					this.setActivePage(lastEditorPage);
				}
			} else if (diagnostic.getSeverity() != Diagnostic.OK) {
				final ProblemEditorPart problemEditorPart = new ProblemEditorPart();
				problemEditorPart.setDiagnostic(diagnostic);
				problemEditorPart.setMarkerHelper(this.markerHelper);
				try {
					this.addPage(++lastEditorPage, problemEditorPart,
							this.getEditorInput());
					this.setPageText(lastEditorPage,
							problemEditorPart.getPartName());
					this.setActivePage(lastEditorPage);
					this.showTabs();
				} catch (final PartInitException exception) {
					OverviewEditorPlugin.INSTANCE.log(exception);
				}
			}

			if (this.markerHelper.hasMarkers(this.editingDomain
					.getResourceSet())) {
				this.markerHelper.deleteMarkers(this.editingDomain
						.getResourceSet());
				if (diagnostic.getSeverity() != Diagnostic.OK) {
					try {
						this.markerHelper.createMarkers(diagnostic);
					} catch (final CoreException exception) {
						OverviewEditorPlugin.INSTANCE.log(exception);
					}
				}
			}
		}
	}

	/**
	 * Shows a dialog that asks if conflicting changes should be discarded. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected boolean handleDirtyConflict() {
		return MessageDialog.openQuestion(this.getSite().getShell(),
				getString("_UI_FileConflict_label"),
				getString("_WARN_FileConflict"));
	}

	/**
	 * This creates a model editor. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	public OverviewEditor() {
		super();
		this.initializeEditingDomain();
	}

	abstract protected String getOverviewModelDefID();

	/**
	 * This sets up the editing domain for the model editor. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	protected void initializeEditingDomain() {
		// Create an adapter factory that yields item providers.

		// Load the overviewModelDef with this ID
		OverviewModelDef overviewModelDef = null;
		for (final OverviewModelDef ovvMDef : OverviewModelDefRegistry
				.getOverviewModelDefs()) {
			if (this.getOverviewModelDefID().equals(ovvMDef.getID())) {
				overviewModelDef = ovvMDef;
				break;
			}
		}
		this.adapterFactory = overviewModelDef.getAdapterFactory();

		// Create the command stack that will notify this editor as commands are
		// executed.
		//
		final BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to
		// be the selection of the viewer with focus.
		//
		commandStack.addCommandStackListener(new CommandStackListener() {
			@Override
			public void commandStackChanged(final EventObject event) {
				OverviewEditor.this.getContainer().getDisplay()
						.asyncExec(new Runnable() {
							@Override
							public void run() {
								OverviewEditor.this
										.firePropertyChange(IEditorPart.PROP_DIRTY);

								// Try to select the affected objects.
								//
								final Command mostRecentCommand = ((CommandStack) event
										.getSource()).getMostRecentCommand();
								if (mostRecentCommand != null) {
									OverviewEditor.this
											.setSelectionToViewer(mostRecentCommand
													.getAffectedObjects());
								}
								if ((OverviewEditor.this.propertySheetPage != null)
										&& !OverviewEditor.this.propertySheetPage
												.getControl().isDisposed()
										&& (OverviewEditor.this.propertySheetPage
												.getCurrentTab() != null)) {
									OverviewEditor.this.propertySheetPage
											.refresh();
								}
							}
						});
			}
		});

		// Create the editing domain with a special command stack.
		//
		this.editingDomain = new AdapterFactoryEditingDomain(
				this.adapterFactory, commandStack,
				new HashMap<Resource, Boolean>());
	}

	/**
	 * This is here for the listener to be able to call it. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected void firePropertyChange(final int action) {
		super.firePropertyChange(action);
	}

	/**
	 * This sets the selection into whichever viewer is active. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setSelectionToViewer(final Collection<?> collection) {
		final Collection<?> theSelection = collection;
		// Make sure it's okay.
		//
		if ((theSelection != null) && !theSelection.isEmpty()) {
			// I don't know if this should be run this deferred
			// because we might have to give the editor a chance to process the
			// viewer update events
			// and hence to update the views first.
			//
			//
			final Runnable runnable = new Runnable() {
				@Override
				public void run() {
					// Try to select the items in the current content viewer of
					// the editor.
					//
					if (OverviewEditor.this.currentViewer != null) {
						OverviewEditor.this.currentViewer
								.setSelection(new StructuredSelection(
										theSelection.toArray()), true);
					}
				}
			};
			runnable.run();
		}
	}

	/**
	 * This returns the editing domain as required by the
	 * {@link IEditingDomainProvider} interface. This is important for
	 * implementing the static methods of {@link AdapterFactoryEditingDomain}
	 * and for supporting {@link org.eclipse.emf.edit.ui.action.CommandAction}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public EditingDomain getEditingDomain() {
		return this.editingDomain;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public class ReverseAdapterFactoryContentProvider extends
			AdapterFactoryContentProvider {
		/**
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		public ReverseAdapterFactoryContentProvider(
				final AdapterFactory adapterFactory) {
			super(adapterFactory);
		}

		/**
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		@Override
		public Object[] getElements(final Object object) {
			final Object parent = super.getParent(object);
			return (parent == null ? Collections.EMPTY_SET : Collections
					.singleton(parent)).toArray();
		}

		/**
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		@Override
		public Object[] getChildren(final Object object) {
			final Object parent = super.getParent(object);
			return (parent == null ? Collections.EMPTY_SET : Collections
					.singleton(parent)).toArray();
		}

		/**
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		@Override
		public boolean hasChildren(final Object object) {
			final Object parent = super.getParent(object);
			return parent != null;
		}

		/**
		 * <!-- begin-user-doc --> <!-- end-user-doc -->
		 * 
		 * @generated
		 */
		@Override
		public Object getParent(final Object object) {
			return null;
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setCurrentViewerPane(final ViewerPane viewerPane) {
		if (this.currentViewerPane != viewerPane) {
			if (this.currentViewerPane != null) {
				this.currentViewerPane.showFocus(false);
			}
			this.currentViewerPane = viewerPane;
		}
		this.setCurrentViewer(this.currentViewerPane.getViewer());
	}

	/**
	 * This makes sure that one content viewer, either for the current page or
	 * the outline view, if it has focus, is the current one. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setCurrentViewer(final Viewer viewer) {
		// If it is changing...
		//
		if (this.currentViewer != viewer) {
			if (this.selectionChangedListener == null) {
				// Create the listener on demand.
				//
				this.selectionChangedListener = new ISelectionChangedListener() {
					// This just notifies those things that are affected by the
					// section.
					//
					@Override
					public void selectionChanged(
							final SelectionChangedEvent selectionChangedEvent) {
						OverviewEditor.this.setSelection(selectionChangedEvent
								.getSelection());
					}
				};
			}

			// Stop listening to the old one.
			//
			if (this.currentViewer != null) {
				this.currentViewer
						.removeSelectionChangedListener(this.selectionChangedListener);
			}

			// Start listening to the new one.
			//
			if (viewer != null) {
				viewer.addSelectionChangedListener(this.selectionChangedListener);
			}

			// Remember it.
			//
			this.currentViewer = viewer;

			// Set the editors selection based on the current viewer's
			// selection.
			//
			this.setSelection(this.currentViewer == null ? StructuredSelection.EMPTY
					: this.currentViewer.getSelection());
		}
	}

	/**
	 * This returns the viewer as required by the {@link IViewerProvider}
	 * interface. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Viewer getViewer() {
		return this.currentViewer;
	}

	/**
	 * This creates a context menu for the viewer and adds a listener as well
	 * registering the menu for extension. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected void createContextMenuFor(final StructuredViewer viewer) {
		final MenuManager contextMenu = new MenuManager("#PopUp");
		contextMenu.add(new Separator("additions"));
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this);
		final Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		this.getSite().registerContextMenu(contextMenu,
				new UnwrappingSelectionProvider(viewer));

		final int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		final Transfer[] transfers = new Transfer[] { LocalTransfer
				.getInstance() };
		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(
				viewer));
		viewer.addDropSupport(dndOperations, transfers,
				new EditingDomainViewerDropAdapter(this.editingDomain, viewer));
	}

	/**
	 * This is the method called to load a resource into the editing domain's
	 * resource set based on the editor's input. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public void createModel() {
		final URI resourceURI = EditUIUtil.getURI(this.getEditorInput());
		Exception exception = null;
		Resource resource = null;
		try {
			// Load the resource through the editing domain.
			//
			resource = this.editingDomain.getResourceSet().getResource(
					resourceURI, true);
		} catch (final Exception e) {
			exception = e;
			resource = this.editingDomain.getResourceSet().getResource(
					resourceURI, false);
		}

		final Diagnostic diagnostic = this.analyzeResourceProblems(resource,
				exception);
		if (diagnostic.getSeverity() != Diagnostic.OK) {
			this.resourceToDiagnosticMap.put(resource,
					this.analyzeResourceProblems(resource, exception));
		}
		this.editingDomain.getResourceSet().eAdapters()
				.add(this.problemIndicationAdapter);
	}

	/**
	 * Returns a diagnostic describing the errors and warnings listed in the
	 * resource and the specified exception (if any). <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Diagnostic analyzeResourceProblems(final Resource resource,
			final Exception exception) {
		if (!resource.getErrors().isEmpty()
				|| !resource.getWarnings().isEmpty()) {
			final BasicDiagnostic basicDiagnostic = new BasicDiagnostic(
					Diagnostic.ERROR,
					"org.obeonetwork.dsl.overview.editor",
					0,
					getString("_UI_CreateModelError_message", resource.getURI()),
					new Object[] { exception == null ? (Object) resource
							: exception });
			basicDiagnostic.merge(EcoreUtil.computeDiagnostic(resource, true));
			return basicDiagnostic;
		} else if (exception != null) {
			return new BasicDiagnostic(Diagnostic.ERROR,
					"org.obeonetwork.dsl.overview.editor", 0, getString(
							"_UI_CreateModelError_message", resource.getURI()),
					new Object[] { exception });
		} else {
			return Diagnostic.OK_INSTANCE;
		}
	}

	/**
	 * This is the method used by the framework to install your own controls.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void createPages() {
		// Creates the model from the editor input
		//
		this.createModel();

		// Only creates the other pages if there is something that can be edited
		//
		if (!this.getEditingDomain().getResourceSet().getResources().isEmpty()
				&& !(this.getEditingDomain().getResourceSet().getResources()
						.get(0)).getContents().isEmpty()) {
			// Create a page for the selection tree view.
			//
			{
				final ViewerPane viewerPane = new ViewerPane(this.getSite()
						.getPage(), OverviewEditor.this) {
					@Override
					public Viewer createViewer(final Composite composite) {
						final Tree tree = new Tree(composite, SWT.MULTI);
						final TreeViewer newTreeViewer = new TreeViewer(tree);
						return newTreeViewer;
					}

					@Override
					public void requestActivation() {
						super.requestActivation();
						OverviewEditor.this.setCurrentViewerPane(this);
					}
				};
				viewerPane.createControl(this.getContainer());

				this.selectionViewer = (TreeViewer) viewerPane.getViewer();
				this.selectionViewer
						.setContentProvider(new AdapterFactoryContentProvider(
								this.adapterFactory));

				this.selectionViewer
						.setLabelProvider(new AdapterFactoryLabelProvider(
								this.adapterFactory));
				this.selectionViewer.setInput(this.editingDomain
						.getResourceSet());
				this.selectionViewer.setSelection(
						new StructuredSelection(this.editingDomain
								.getResourceSet().getResources().get(0)), true);
				viewerPane.setTitle(this.editingDomain.getResourceSet());

				new AdapterFactoryTreeEditor(this.selectionViewer.getTree(),
						this.adapterFactory);

				this.createContextMenuFor(this.selectionViewer);
				final int pageIndex = this.addPage(viewerPane.getControl());
				this.setPageText(pageIndex,
						getString("_UI_SelectionPage_label"));
			}

			// Create a page for the parent tree view.
			//
			// {
			// ViewerPane viewerPane =
			// new ViewerPane(getSite().getPage(), OverviewEditor.this) {
			// @Override
			// public Viewer createViewer(Composite composite) {
			// Tree tree = new Tree(composite, SWT.MULTI);
			// TreeViewer newTreeViewer = new TreeViewer(tree);
			// return newTreeViewer;
			// }
			// @Override
			// public void requestActivation() {
			// super.requestActivation();
			// setCurrentViewerPane(this);
			// }
			// };
			// viewerPane.createControl(getContainer());
			//
			// parentViewer = (TreeViewer)viewerPane.getViewer();
			// parentViewer.setAutoExpandLevel(30);
			// parentViewer.setContentProvider(new
			// ReverseAdapterFactoryContentProvider(adapterFactory));
			// parentViewer.setLabelProvider(new
			// AdapterFactoryLabelProvider(adapterFactory));
			//
			// createContextMenuFor(parentViewer);
			// int pageIndex = addPage(viewerPane.getControl());
			// setPageText(pageIndex, getString("_UI_ParentPage_label"));
			// }

			// This is the page for the list viewer
			//
			// {
			// ViewerPane viewerPane =
			// new ViewerPane(getSite().getPage(), OverviewEditor.this) {
			// @Override
			// public Viewer createViewer(Composite composite) {
			// return new ListViewer(composite);
			// }
			// @Override
			// public void requestActivation() {
			// super.requestActivation();
			// setCurrentViewerPane(this);
			// }
			// };
			// viewerPane.createControl(getContainer());
			// listViewer = (ListViewer)viewerPane.getViewer();
			// listViewer.setContentProvider(new
			// AdapterFactoryContentProvider(adapterFactory));
			// listViewer.setLabelProvider(new
			// AdapterFactoryLabelProvider(adapterFactory));
			//
			// createContextMenuFor(listViewer);
			// int pageIndex = addPage(viewerPane.getControl());
			// setPageText(pageIndex, getString("_UI_ListPage_label"));
			// }

			// This is the page for the tree viewer
			//
			// {
			// ViewerPane viewerPane =
			// new ViewerPane(getSite().getPage(), OverviewEditor.this) {
			// @Override
			// public Viewer createViewer(Composite composite) {
			// return new TreeViewer(composite);
			// }
			// @Override
			// public void requestActivation() {
			// super.requestActivation();
			// setCurrentViewerPane(this);
			// }
			// };
			// viewerPane.createControl(getContainer());
			// treeViewer = (TreeViewer)viewerPane.getViewer();
			// treeViewer.setContentProvider(new
			// AdapterFactoryContentProvider(adapterFactory));
			// treeViewer.setLabelProvider(new
			// AdapterFactoryLabelProvider(adapterFactory));
			//
			// new AdapterFactoryTreeEditor(treeViewer.getTree(),
			// adapterFactory);
			//
			// createContextMenuFor(treeViewer);
			// int pageIndex = addPage(viewerPane.getControl());
			// setPageText(pageIndex, getString("_UI_TreePage_label"));
			// }

			// This is the page for the table viewer.
			//
			// {
			// ViewerPane viewerPane =
			// new ViewerPane(getSite().getPage(), OverviewEditor.this) {
			// @Override
			// public Viewer createViewer(Composite composite) {
			// return new TableViewer(composite);
			// }
			// @Override
			// public void requestActivation() {
			// super.requestActivation();
			// setCurrentViewerPane(this);
			// }
			// };
			// viewerPane.createControl(getContainer());
			// tableViewer = (TableViewer)viewerPane.getViewer();
			//
			// Table table = tableViewer.getTable();
			// TableLayout layout = new TableLayout();
			// table.setLayout(layout);
			// table.setHeaderVisible(true);
			// table.setLinesVisible(true);
			//
			// TableColumn objectColumn = new TableColumn(table, SWT.NONE);
			// layout.addColumnData(new ColumnWeightData(3, 100, true));
			// objectColumn.setText(getString("_UI_ObjectColumn_label"));
			// objectColumn.setResizable(true);
			//
			// TableColumn selfColumn = new TableColumn(table, SWT.NONE);
			// layout.addColumnData(new ColumnWeightData(2, 100, true));
			// selfColumn.setText(getString("_UI_SelfColumn_label"));
			// selfColumn.setResizable(true);
			//
			// tableViewer.setColumnProperties(new String [] {"a", "b"});
			// tableViewer.setContentProvider(new
			// AdapterFactoryContentProvider(adapterFactory));
			// tableViewer.setLabelProvider(new
			// AdapterFactoryLabelProvider(adapterFactory));
			//
			// createContextMenuFor(tableViewer);
			// int pageIndex = addPage(viewerPane.getControl());
			// setPageText(pageIndex, getString("_UI_TablePage_label"));
			// }

			// This is the page for the table tree viewer.
			//
			// {
			// ViewerPane viewerPane =
			// new ViewerPane(getSite().getPage(), OverviewEditor.this) {
			// @Override
			// public Viewer createViewer(Composite composite) {
			// return new TreeViewer(composite);
			// }
			// @Override
			// public void requestActivation() {
			// super.requestActivation();
			// setCurrentViewerPane(this);
			// }
			// };
			// viewerPane.createControl(getContainer());
			//
			// treeViewerWithColumns = (TreeViewer)viewerPane.getViewer();
			//
			// Tree tree = treeViewerWithColumns.getTree();
			// tree.setLayoutData(new FillLayout());
			// tree.setHeaderVisible(true);
			// tree.setLinesVisible(true);
			//
			// TreeColumn objectColumn = new TreeColumn(tree, SWT.NONE);
			// objectColumn.setText(getString("_UI_ObjectColumn_label"));
			// objectColumn.setResizable(true);
			// objectColumn.setWidth(250);
			//
			// TreeColumn selfColumn = new TreeColumn(tree, SWT.NONE);
			// selfColumn.setText(getString("_UI_SelfColumn_label"));
			// selfColumn.setResizable(true);
			// selfColumn.setWidth(200);
			//
			// treeViewerWithColumns.setColumnProperties(new String [] {"a",
			// "b"});
			// treeViewerWithColumns.setContentProvider(new
			// AdapterFactoryContentProvider(adapterFactory));
			// treeViewerWithColumns.setLabelProvider(new
			// AdapterFactoryLabelProvider(adapterFactory));
			//
			// createContextMenuFor(treeViewerWithColumns);
			// int pageIndex = addPage(viewerPane.getControl());
			// setPageText(pageIndex,
			// getString("_UI_TreeWithColumnsPage_label"));
			// }

			this.getSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					OverviewEditor.this.setActivePage(0);
				}
			});
		}

		// Ensures that this editor will only display the page's tab
		// area if there are more than one page
		//
		this.getContainer().addControlListener(new ControlAdapter() {
			boolean guard = false;

			@Override
			public void controlResized(final ControlEvent event) {
				if (!this.guard) {
					this.guard = true;
					OverviewEditor.this.hideTabs();
					this.guard = false;
				}
			}
		});

		this.getSite().getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				OverviewEditor.this.updateProblemIndication();
			}
		});
	}

	/**
	 * If there is just one page in the multi-page editor part, this hides the
	 * single tab at the bottom. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void hideTabs() {
		if (this.getPageCount() <= 1) {
			this.setPageText(0, "");
			if (this.getContainer() instanceof CTabFolder) {
				((CTabFolder) this.getContainer()).setTabHeight(1);
				final Point point = this.getContainer().getSize();
				this.getContainer().setSize(point.x, point.y + 6);
			}
		}
	}

	/**
	 * If there is more than one page in the multi-page editor part, this shows
	 * the tabs at the bottom. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void showTabs() {
		if (this.getPageCount() > 1) {
			this.setPageText(0, getString("_UI_SelectionPage_label"));
			if (this.getContainer() instanceof CTabFolder) {
				((CTabFolder) this.getContainer()).setTabHeight(SWT.DEFAULT);
				final Point point = this.getContainer().getSize();
				this.getContainer().setSize(point.x, point.y - 6);
			}
		}
	}

	/**
	 * This is used to track the active viewer. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected void pageChange(final int pageIndex) {
		super.pageChange(pageIndex);

		if (this.contentOutlinePage != null) {
			this.handleContentOutlineSelection(this.contentOutlinePage
					.getSelection());
		}
	}

	/**
	 * This is how the framework determines which interfaces we implement. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(final Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return this.showOutlineView() ? this.getContentOutlinePage() : null;
		} else if (key.equals(IPropertySheetPage.class)) {
			return this.getPropertySheetPage();
		} else if (key.equals(IGotoMarker.class)) {
			return this;
		} else {
			return super.getAdapter(key);
		}
	}

	/**
	 * This accesses a cached version of the content outliner. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public IContentOutlinePage getContentOutlinePage() {
		if (this.contentOutlinePage == null) {
			// The content outline is just a tree.
			//
			class MyContentOutlinePage extends ContentOutlinePage {
				@Override
				public void createControl(final Composite parent) {
					super.createControl(parent);
					OverviewEditor.this.contentOutlineViewer = this
							.getTreeViewer();
					OverviewEditor.this.contentOutlineViewer
							.addSelectionChangedListener(this);

					// Set up the tree viewer.
					//
					OverviewEditor.this.contentOutlineViewer
							.setContentProvider(new AdapterFactoryContentProvider(
									OverviewEditor.this.adapterFactory));
					OverviewEditor.this.contentOutlineViewer
							.setLabelProvider(new AdapterFactoryLabelProvider(
									OverviewEditor.this.adapterFactory));
					OverviewEditor.this.contentOutlineViewer
							.setInput(OverviewEditor.this.editingDomain
									.getResourceSet());

					// Make sure our popups work.
					//
					OverviewEditor.this
							.createContextMenuFor(OverviewEditor.this.contentOutlineViewer);

					if (!OverviewEditor.this.editingDomain.getResourceSet()
							.getResources().isEmpty()) {
						// Select the root object in the view.
						//
						OverviewEditor.this.contentOutlineViewer.setSelection(
								new StructuredSelection(
										OverviewEditor.this.editingDomain
												.getResourceSet()
												.getResources().get(0)), true);
					}
				}

				@Override
				public void makeContributions(final IMenuManager menuManager,
						final IToolBarManager toolBarManager,
						final IStatusLineManager statusLineManager) {
					super.makeContributions(menuManager, toolBarManager,
							statusLineManager);
					OverviewEditor.this.contentOutlineStatusLineManager = statusLineManager;
				}

				@Override
				public void setActionBars(final IActionBars actionBars) {
					super.setActionBars(actionBars);
					OverviewEditor.this.getActionBarContributor()
							.shareGlobalActions(this, actionBars);
				}
			}

			this.contentOutlinePage = new MyContentOutlinePage();

			// Listen to selection so that we can handle it is a special way.
			//
			this.contentOutlinePage
					.addSelectionChangedListener(new ISelectionChangedListener() {
						// This ensures that we handle selections correctly.
						//
						@Override
						public void selectionChanged(
								final SelectionChangedEvent event) {
							OverviewEditor.this
									.handleContentOutlineSelection(event
											.getSelection());
						}
					});
		}

		return this.contentOutlinePage;
	}

	/**
	 * This accesses a cached version of the property sheet. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	public IPropertySheetPage getPropertySheetPage() {
		if (this.propertySheetPage == null) {
			this.propertySheetPage = new TabbedPropertySheetPage(
					OverviewEditor.this) {
				@Override
				public void setActionBars(final IActionBars actionBars) {
					super.setActionBars(actionBars);
					OverviewEditor.this.getActionBarContributor()
							.shareGlobalActions(this, actionBars);
				}
			};
		}
		return this.propertySheetPage;
	}

	/**
	 * This deals with how we want selection in the outliner to affect the other
	 * views. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void handleContentOutlineSelection(final ISelection selection) {
		if ((this.currentViewerPane != null) && !selection.isEmpty()
				&& (selection instanceof IStructuredSelection)) {
			final Iterator<?> selectedElements = ((IStructuredSelection) selection)
					.iterator();
			if (selectedElements.hasNext()) {
				// Get the first selected element.
				//
				final Object selectedElement = selectedElements.next();

				// If it's the selection viewer, then we want it to select the
				// same selection as this selection.
				//
				if (this.currentViewerPane.getViewer() == this.selectionViewer) {
					final ArrayList<Object> selectionList = new ArrayList<Object>();
					selectionList.add(selectedElement);
					while (selectedElements.hasNext()) {
						selectionList.add(selectedElements.next());
					}

					// Set the selection to the widget.
					//
					this.selectionViewer.setSelection(new StructuredSelection(
							selectionList));
				} else {
					// Set the input to the widget.
					//
					if (this.currentViewerPane.getViewer().getInput() != selectedElement) {
						this.currentViewerPane.getViewer().setInput(
								selectedElement);
						this.currentViewerPane.setTitle(selectedElement);
					}
				}
			}
		}
	}

	/**
	 * This is for implementing {@link IEditorPart} and simply tests the command
	 * stack. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean isDirty() {
		return ((BasicCommandStack) this.editingDomain.getCommandStack())
				.isSaveNeeded();
	}

	/**
	 * This is for implementing {@link IEditorPart} and simply saves the model
	 * file. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void doSave(final IProgressMonitor progressMonitor) {
		// Save only resources that have actually changed.
		//
		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
				Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);

		// Do the work within an operation because this is a long running
		// activity that modifies the workbench.
		//
		final WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			// This is the method that gets invoked when the operation runs.
			//
			@Override
			public void execute(final IProgressMonitor monitor) {
				// Save the resources to the file system.
				//
				boolean first = true;
				for (final Resource resource : OverviewEditor.this.editingDomain
						.getResourceSet().getResources()) {
					if ((first || !resource.getContents().isEmpty() || OverviewEditor.this
							.isPersisted(resource))
							&& !OverviewEditor.this.editingDomain
									.isReadOnly(resource)) {
						try {
							OverviewEditor.this.savedResources.add(resource);
							resource.save(saveOptions);
						} catch (final Exception exception) {
							OverviewEditor.this.resourceToDiagnosticMap.put(
									resource, OverviewEditor.this
											.analyzeResourceProblems(resource,
													exception));
						}
						first = false;
					}
				}
			}
		};

		this.updateProblemIndication = false;
		try {
			// This runs the options, and shows progress.
			//
			new ProgressMonitorDialog(this.getSite().getShell()).run(true,
					false, operation);

			// Refresh the necessary state.
			//
			((BasicCommandStack) this.editingDomain.getCommandStack())
					.saveIsDone();
			this.firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (final Exception exception) {
			// Something went wrong that shouldn't.
			//
			OverviewEditorPlugin.INSTANCE.log(exception);
		}
		this.updateProblemIndication = true;
		this.updateProblemIndication();
	}

	/**
	 * This returns whether something has been persisted to the URI of the
	 * specified resource. The implementation uses the URI converter from the
	 * editor's resource set to try to open an input stream. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected boolean isPersisted(final Resource resource) {
		boolean result = false;
		try {
			final InputStream stream = this.editingDomain.getResourceSet()
					.getURIConverter().createInputStream(resource.getURI());
			if (stream != null) {
				result = true;
				stream.close();
			}
		} catch (final IOException e) {
			// Ignore
		}
		return result;
	}

	/**
	 * This always returns true because it is not currently supported. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * This also changes the editor's input. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void doSaveAs() {
		final SaveAsDialog saveAsDialog = new SaveAsDialog(this.getSite()
				.getShell());
		saveAsDialog.open();
		final IPath path = saveAsDialog.getResult();
		if (path != null) {
			final IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(path);
			if (file != null) {
				this.doSaveAs(URI.createPlatformResourceURI(file.getFullPath()
						.toString(), true), new FileEditorInput(file));
			}
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void doSaveAs(final URI uri, final IEditorInput editorInput) {
		(this.editingDomain.getResourceSet().getResources().get(0)).setURI(uri);
		this.setInputWithNotify(editorInput);
		this.setPartName(editorInput.getName());
		final IProgressMonitor progressMonitor = this.getActionBars()
				.getStatusLineManager() != null ? this.getActionBars()
				.getStatusLineManager().getProgressMonitor()
				: new NullProgressMonitor();
		this.doSave(progressMonitor);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void gotoMarker(final IMarker marker) {
		try {
			if (marker.getType().equals(EValidator.MARKER)) {
				final String uriAttribute = marker.getAttribute(
						EValidator.URI_ATTRIBUTE, null);
				if (uriAttribute != null) {
					final URI uri = URI.createURI(uriAttribute);
					final EObject eObject = this.editingDomain.getResourceSet()
							.getEObject(uri, true);
					if (eObject != null) {
						this.setSelectionToViewer(Collections
								.singleton(this.editingDomain
										.getWrapper(eObject)));
					}
				}
			}
		} catch (final CoreException exception) {
			OverviewEditorPlugin.INSTANCE.log(exception);
		}
	}

	/**
	 * This is called during startup. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	@Override
	public void init(final IEditorSite site, final IEditorInput editorInput) {
		this.setSite(site);
		this.setInputWithNotify(editorInput);
		this.setPartName(editorInput.getName());
		site.setSelectionProvider(this);
		site.getPage().addPartListener(this.partListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				this.resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setFocus() {
		if (this.currentViewerPane != null) {
			this.currentViewerPane.setFocus();
		} else {
			this.getControl(this.getActivePage()).setFocus();
		}
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void addSelectionChangedListener(
			final ISelectionChangedListener listener) {
		this.selectionChangedListeners.add(listener);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void removeSelectionChangedListener(
			final ISelectionChangedListener listener) {
		this.selectionChangedListeners.remove(listener);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to
	 * return this editor's overall selection. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public ISelection getSelection() {
		return this.editorSelection;
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to
	 * set this editor's overall selection. Calling this result will notify the
	 * listeners. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void setSelection(final ISelection selection) {
		this.editorSelection = selection;

		for (final ISelectionChangedListener listener : this.selectionChangedListeners) {
			listener.selectionChanged(new SelectionChangedEvent(this, selection));
		}
		this.setStatusLineManager(selection);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setStatusLineManager(final ISelection selection) {
		final IStatusLineManager statusLineManager = (this.currentViewer != null)
				&& (this.currentViewer == this.contentOutlineViewer) ? this.contentOutlineStatusLineManager
				: this.getActionBars().getStatusLineManager();

		if (statusLineManager != null) {
			if (selection instanceof IStructuredSelection) {
				final Collection<?> collection = ((IStructuredSelection) selection)
						.toList();
				switch (collection.size()) {
				case 0: {
					statusLineManager
							.setMessage(getString("_UI_NoObjectSelected"));
					break;
				}
				case 1: {
					final String text = new AdapterFactoryItemDelegator(
							this.adapterFactory).getText(collection.iterator()
							.next());
					statusLineManager.setMessage(getString(
							"_UI_SingleObjectSelected", text));
					break;
				}
				default: {
					statusLineManager.setMessage(getString(
							"_UI_MultiObjectSelected",
							Integer.toString(collection.size())));
					break;
				}
				}
			} else {
				statusLineManager.setMessage("");
			}
		}
	}

	/**
	 * This looks up a string in the plugin's plugin.properties file. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static String getString(final String key) {
		return OverviewEditorPlugin.INSTANCE.getString(key);
	}

	/**
	 * This looks up a string in plugin.properties, making a substitution. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	private static String getString(final String key, final Object s1) {
		return OverviewEditorPlugin.INSTANCE
				.getString(key, new Object[] { s1 });
	}

	/**
	 * This implements {@link org.eclipse.jface.action.IMenuListener} to help
	 * fill the context menus with contributions from the Edit menu. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void menuAboutToShow(final IMenuManager menuManager) {
		((IMenuListener) this.getEditorSite().getActionBarContributor())
				.menuAboutToShow(menuManager);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EditingDomainActionBarContributor getActionBarContributor() {
		return (EditingDomainActionBarContributor) this.getEditorSite()
				.getActionBarContributor();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public IActionBars getActionBars() {
		return this.getActionBarContributor().getActionBars();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public AdapterFactory getAdapterFactory() {
		return this.adapterFactory;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void dispose() {
		this.updateProblemIndication = false;

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
				this.resourceChangeListener);

		this.getSite().getPage().removePartListener(this.partListener);

		if (this.adapterFactory instanceof ComposedAdapterFactory) {
			((ComposedAdapterFactory) this.adapterFactory).dispose();
		}

		if (this.getActionBarContributor().getActiveEditor() == this) {
			this.getActionBarContributor().setActiveEditor(null);
		}

		if (this.propertySheetPage != null) {
			this.propertySheetPage.dispose();
		}

		if (this.contentOutlinePage != null) {
			this.contentOutlinePage.dispose();
		}

		super.dispose();
	}

	/**
	 * Returns whether the outline view should be presented to the user. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected boolean showOutlineView() {
		return true;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
	 * @generated NOT
	 */
	@Override
	public String getContributorId() {
		return PROPERTIES_CONTRIBUTOR;
	}
}

