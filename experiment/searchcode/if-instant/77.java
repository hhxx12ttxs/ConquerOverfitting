package org.dftproject.lineagelinkage.ui.pages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dftproject.genesis.data.genealogy.GenealogyConstants;
import org.dftproject.genesis.data.genealogy.GenealogyUtils;
import org.dftproject.genesis.data.genealogy.IEvent;
import org.dftproject.genesis.data.genealogy.IInstant;
import org.dftproject.genesis.data.genealogy.IName;
import org.dftproject.genesis.data.genealogy.IPerson;
import org.dftproject.genesis.data.genealogy.IPlace;
import org.dftproject.genesis.data.genealogy.IRole;
import org.dftproject.genesis.data.genealogy.Sex;
import org.dftproject.genesis.ui.figures.FigureCanvasEx;
import org.dftproject.genesis.ui.figures.PageContainer;
import org.dftproject.genesis.ui.figures.dualtree.DualTreeFigure;
import org.dftproject.genesis.ui.figures.dualtree.ISelectableFigure;
import org.dftproject.genesis.ui.figures.dualtree.ImprovedWalkersLayout;
import org.dftproject.genesis.ui.figures.zoompan.ZoomPanContainer;
import org.dftproject.genesis.ui.pages.AbstractPagePart;
import org.dftproject.genesis.ui.pages.IPageInput;
import org.dftproject.genesis.ui.pages.IPageSite;
import org.dftproject.genesis.ui.pages.PageInitException;
import org.dftproject.genesis.ui.pages.pedigree.tree.PedigreeAnimator;
import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutListener;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class MergePage extends AbstractPagePart {

	private static final Log log = LogFactory.getLog(MergePage.class);

	public static final String ID = MergePage.class.getName();

	private final Color borderColor = new Color(null, 192, 192, 192);

	private FigureCanvasEx canvas;
	private ZoomPanContainer zoomPanContainer;
	private DualTreeFigure pedigree;

	private static final int numThreads = 5;
	private ThreadPoolExecutor threadPool;

	private final Set<Pair> alreadyVisited = Collections.synchronizedSet(new HashSet<Pair>());

	private boolean updateScheduled;
	private final Queue<MergePair> updateQueue = new LinkedList<MergePair>();
	private final Runnable uiRunnable = new Runnable() {

		public void run() {
			updateScheduled = false;
			update();
		}

	};

	public void init(IPageSite site, IPageInput pageInput) throws PageInitException {
		if (!(pageInput instanceof MergePageInput))
			throw new PageInitException("input must be a " + MergePageInput.class.getName());
		this.setSite(site);
		this.setInput(pageInput);

		threadPool = new ThreadPoolExecutor(numThreads, numThreads, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public MergePageInput getMergePageInput() {
		return (MergePageInput) getInput();
	}

	@Override
	public void createPartControl(Composite parent) {
		createContents(parent);
	}

	protected void createContents(Composite parent) {
		// Create the main Draw2D canvas

		canvas = new FigureCanvasEx(parent);
		canvas.getViewport().setContentsTracksWidth(true);
		canvas.getViewport().setContentsTracksHeight(true);

		// Create the root figure

		PageContainer container = new PageContainer();
		container.setLayoutManager(new StackLayout());
		container.setBorder(new MarginBorder(10));
		canvas.setContents(container);

		// Create the zoom/pan container for the pedigree

		zoomPanContainer = new ZoomPanContainer();
		zoomPanContainer.setBorder(new LineBorder(borderColor, 1));
		container.add(zoomPanContainer);

		// Create the pedigree

		pedigree = new DualTreeFigure();
		pedigree.setLayoutManager(new ImprovedWalkersLayout(40, 20));
		pedigree.addLayoutListener(PedigreeAnimator.getDefault());
		pedigree.addLayoutListener(new LayoutListener.Stub() {

			@Override
			public void postLayout(IFigure container) {
				zoomPanContainer.maintainFocus();
			}

		});
		zoomPanContainer.getContents().add(pedigree);

		// Handle arrow keys

		canvas.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (pedigree.getSelectedNode() == null)
					return;

				switch (e.keyCode) {
				case SWT.ARROW_LEFT:
					ISelectableFigure left = getLeft(pedigree.getSelectedNode());
					if (left != null) {
						left.setSelected(true);
						zoomPanContainer.smoothScrollTo(zoomPanContainer.getFigureLocation(left));
					}
					break;
				case SWT.ARROW_RIGHT:
					ISelectableFigure right = getRight(pedigree.getSelectedNode());
					if (right != null) {
						right.setSelected(true);
						zoomPanContainer.smoothScrollTo(zoomPanContainer.getFigureLocation(right));
					}
					break;
				case SWT.ARROW_UP:
					ISelectableFigure up = getUp(pedigree.getSelectedNode());
					if (up != null) {
						up.setSelected(true);
						zoomPanContainer.smoothScrollTo(zoomPanContainer.getFigureLocation(up));
					}
					break;
				case SWT.ARROW_DOWN:
					ISelectableFigure down = getDown(pedigree.getSelectedNode());
					if (down != null) {
						down.setSelected(true);
						zoomPanContainer.smoothScrollTo(zoomPanContainer.getFigureLocation(down));
					}
					break;
				}
			}

		});

		loadPedigree();
	}

	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	protected void loadPedigree() {
		final IPerson primary = getMergePageInput().getPerson();
		final IPerson duplicate = getMergePageInput().getDuplicate();

		threadPool.execute(new Runnable() {

			public void run() {
				addMergePair(new MergePair(createPersonData(primary), createPersonData(duplicate)));
			}

		});
	}

	protected void scheduleUIUpdate(MergePair item) {
		synchronized (updateQueue) {
			updateQueue.add(item);
			if (!updateScheduled) {
				updateScheduled = true;
				Display.getDefault().asyncExec(uiRunnable);
			}
		}
	}

	protected void update() {
		synchronized (updateQueue) {
			Animation.markBegin();

			MergePair mergePair;
			while ((mergePair = updateQueue.poll()) != null) {
				createMergeNode(mergePair);
			}

			Animation.run();
		}
	}

	protected void addMergePair(final MergePair mergePair) {
		scheduleUIUpdate(mergePair);

		threadPool.execute(new Runnable() {

			public void run() {
				addParents(mergePair, true);
				addParents(mergePair, false);
			}

		});
	}

	protected void addParents(final MergePair mergePair, final boolean paternal) {
		if (mergePair == null)
			return;

		PersonData primary = mergePair.getPrimary();
		PersonData duplicate = mergePair.getDuplicate();

		if (primary != null || duplicate != null) {
			IPerson primaryParent = null;
			if (primary != null)
				primaryParent = paternal ? GenealogyUtils.getFather(primary.getPerson()) : GenealogyUtils.getMother(primary.getPerson());
			IPerson duplicateParent = null;
			if (duplicate != null)
				duplicateParent = paternal ? GenealogyUtils.getFather(duplicate.getPerson()) : GenealogyUtils.getMother(duplicate.getPerson());

			if (primaryParent != null || duplicateParent != null) {
				if (alreadyVisited.add(new Pair(primaryParent, duplicateParent))) {
					MergePair parentMergePair = new MergePair(createPersonData(primaryParent), createPersonData(duplicateParent));
					parentMergePair.setChild(mergePair, paternal);
					addMergePair(parentMergePair);
				} else {
					log.info("pair already seen");
				}
			}
		}
	}

	protected PersonData createPersonData(IPerson person) {
		if (person == null)
			return null;

		Sex sex = GenealogyUtils.getSex(person);

		String fullName = null;
		IName name = GenealogyUtils.getName(person);
		if (name != null)
			fullName = GenealogyUtils.stringFromName(name);

		List<EventData> events = new LinkedList<EventData>();

		for (IRole role : person.getRoles(GenealogyConstants.Child)) {
			IEvent event = role.getEvent();
			if (event != null) {
				IInstant instant = GenealogyUtils.getDate(event);
				IPlace place = null;// GenealogyUtils.getPlace(event);

				if (instant != null || place != null) {
					String strDate = GenealogyUtils.stringFromInstant(instant);
					String strPlace = place == null ? null : place.toString();
					events.add(new EventData("b.", strDate, strPlace));
				}
			}
		}
		for (IRole role : person.getRoles(GenealogyConstants.Deceased)) {
			IEvent event = role.getEvent();
			if (event != null) {
				IInstant instant = GenealogyUtils.getDate(event);
				IPlace place = null;// GenealogyUtils.getPlace(event);

				if (instant != null || place != null) {
					String strDate = GenealogyUtils.stringFromInstant(instant);
					String strPlace = place == null ? null : place.toString();
					events.add(new EventData("d.", strDate, strPlace));
				}
			}
		}

		return new PersonData(sex, fullName, events.toArray(new EventData[0]), person);
	}

	protected void createMergeNode(MergePair mergePair) {
		if (mergePair.getPrimary() == null && mergePair.getDuplicate() == null)
			return;

		final MergeNode figure = new MergeNode(mergePair.getPrimary(), mergePair.getDuplicate());
		figure.addPropertyChangeListener(ISelectableFigure.SELECTION_PROPERTY, new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {
				if (Boolean.TRUE.equals(event.getNewValue()))
					zoomPanContainer.setFocus(figure);
			}

		});

		if (mergePair.getChild() != null) {
			IFigure childFigure = mergePair.getChild().getFigure();
			if (mergePair.isPaternal()) {
				pedigree.addAncestor(childFigure, figure, 0);
			} else {
				pedigree.addAncestor(childFigure, figure, 1);
			}
		} else {
			pedigree.setRoot(figure);
			figure.setSelected(true);
		}

		mergePair.setFigure(figure);
	}

	protected ISelectableFigure getLeft(IFigure figure) {
		IFigure[] descendants = pedigree.getDescendants(figure);
		if (descendants.length == 0 || descendants[0] == null)
			return null;
		return (ISelectableFigure) descendants[0];
	}

	protected ISelectableFigure getRight(IFigure figure) {
		ISelectableFigure nearest = null;

		int y = figure.getBounds().getCenter().y;
		IFigure[] ancestors = pedigree.getAncestors(figure);
		for (IFigure ancestor : ancestors) {
			if (!(ancestor instanceof ISelectableFigure))
				continue;
			if (nearest == null)
				nearest = (ISelectableFigure) ancestor;
			else if (Math.abs(nearest.getBounds().getCenter().y - y) > Math.abs(ancestor.getBounds().getCenter().y - y))
				nearest = (ISelectableFigure) ancestor;
		}

		return nearest;
	}

	protected ISelectableFigure getUp(IFigure figure) {
		// Get this figure's child

		IFigure[] descendants = pedigree.getDescendants(figure);
		if (descendants.length == 0 || descendants[0] == null)
			return null;

		// Get the ancestor index of this figure

		int index;
		IFigure[] ancestors = pedigree.getAncestors(descendants[0]);
		for (index = 0; index < ancestors.length; index++) {
			if (ancestors[index] == figure)
				break;
		}

		// Return the ancestor with the next highest index

		if (index > 0) {
			for (int i = index - 1; index >= 0; index--) {
				if (ancestors[i] != null)
					return (ISelectableFigure) ancestors[i];
			}
		}

		// Try moving down a generation

		IFigure up = descendants[0];
		while ((up = getUp(up)) != null) {
			ancestors = pedigree.getAncestors(up);
			for (int i = ancestors.length - 1; i >= 0; i--) {
				if (ancestors[i] != null)
					return (ISelectableFigure) ancestors[i];
			}
		}

		return null;
	}

	protected ISelectableFigure getDown(IFigure figure) {
		// Get this figure's child

		IFigure[] descendants = pedigree.getDescendants(figure);
		if (descendants.length == 0 || descendants[0] == null)
			return null;

		// Get the ancestor index of this figure

		int index;
		IFigure[] ancestors = pedigree.getAncestors(descendants[0]);
		for (index = ancestors.length - 1; index >= 0; index--) {
			if (ancestors[index] == figure)
				break;
		}

		// Return the ancestor with the next highest index

		if (index < ancestors.length - 1) {
			for (int i = index + 1; index < ancestors.length; index++) {
				if (ancestors[i] != null)
					return (ISelectableFigure) ancestors[i];
			}
		}

		// Try moving down a generation

		IFigure down = descendants[0];
		while ((down = getDown(down)) != null) {
			ancestors = pedigree.getAncestors(down);
			for (int i = 0; i < ancestors.length; i++) {
				if (ancestors[i] != null)
					return (ISelectableFigure) ancestors[i];
			}
		}

		return null;
	}

	protected class MergePair {

		private final PersonData primary;
		private final PersonData duplicate;
		private MergePair child;
		private boolean paternal;
		private IFigure figure;

		public MergePair(PersonData primary, PersonData duplicate) {
			this.primary = primary;
			this.duplicate = duplicate;
		}

		public PersonData getPrimary() {
			return primary;
		}

		public PersonData getDuplicate() {
			return duplicate;
		}

		public MergePair getChild() {
			return child;
		}

		public boolean isPaternal() {
			return paternal;
		}

		public void setChild(MergePair child, boolean paternal) {
			this.child = child;
			this.paternal = paternal;
		}

		public IFigure getFigure() {
			return figure;
		}

		public void setFigure(IFigure figure) {
			this.figure = figure;
		}

	}

	protected class Pair {

		private final IPerson primary;
		private final IPerson duplicate;

		public Pair(IPerson primary, IPerson duplicate) {
			this.primary = primary;
			this.duplicate = duplicate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((duplicate == null) ? 0 : duplicate.hashCode());
			result = prime * result + ((primary == null) ? 0 : primary.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Pair other = (Pair) obj;
			if (duplicate == null) {
				if (other.duplicate != null)
					return false;
			} else if (!duplicate.equals(other.duplicate))
				return false;
			if (primary == null) {
				if (other.primary != null)
					return false;
			} else if (!primary.equals(other.primary))
				return false;
			return true;
		}

	}

}

