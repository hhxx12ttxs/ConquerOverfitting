package at.ait.dme.yumaJS.client.annotation.impl.seajax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import at.ait.dme.yumaJS.client.YUMA;
import at.ait.dme.yumaJS.client.annotation.Annotatable;
import at.ait.dme.yumaJS.client.annotation.Annotation;
import at.ait.dme.yumaJS.client.annotation.impl.seajax.api.SeadragonMouseHandler;
import at.ait.dme.yumaJS.client.annotation.impl.seajax.api.SeadragonPoint;
import at.ait.dme.yumaJS.client.annotation.impl.seajax.api.SeadragonViewer;
import at.ait.dme.yumaJS.client.annotation.ui.CompoundOverlay;
import at.ait.dme.yumaJS.client.annotation.ui.AnnotationWidget.AnnotationWidgetEditHandler;
import at.ait.dme.yumaJS.client.annotation.ui.edit.BoundingBox;
import at.ait.dme.yumaJS.client.annotation.ui.edit.Range;
import at.ait.dme.yumaJS.client.init.InitParams;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * An implementation of {@link Annotatable} for an HTML DIV holding a 
 * Seadragon AJAX viewer instance.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@Export
@ExportPackage("YUMA")
public class SeajaxAnnotationLayer extends Annotatable implements Exportable {
	
	private static final int DEFAULT_FRAGMENT_LEFT = 30;
	private static final int DEFAULT_FRAGMENT_TOP = 30;
	private static final int DEFAULT_SIZE = 60;
	
	private static final String MEDIATYPE = "IMAGE";
	
	private static String objectURI;
	
	private HTML parentDiv;
	
	private AbsolutePanel editingLayer;
	
	private SeadragonViewer viewer;
	
	// { annotationID -> overlay }
	private HashMap<String, CompoundOverlay> overlays = 
		new HashMap<String, CompoundOverlay>();
	
	private int annotationCtr = 0;
	
	public SeajaxAnnotationLayer(String id, JavaScriptObject deepZoomViewer) {
		this(id, deepZoomViewer, null);
	}
	
	public SeajaxAnnotationLayer(String id, JavaScriptObject deepZoomViewer, InitParams params) {
		super(params);
		
		Element el = DOM.getElementById(id);
		if (el == null)
			YUMA.fatalError("Error: no element with id '" + id + "' found on this page");
		
		if (!el.getTagName().toLowerCase().equals("div"))
			YUMA.fatalError("Error: you can only create a DeepZoomCanvas on a <div> element");
		
		if (deepZoomViewer == null) 
			YUMA.fatalError("Error: Seadragon viewer not found (not initialized yet?)");
		
		parentDiv = HTML.wrap(el);
		
		editingLayer = new AbsolutePanel();
		editingLayer.setStyleName("deepzoom-canvas");
		editingLayer.getElement().getStyle().setOverflow(Overflow.VISIBLE);
		editingLayer.setPixelSize(parentDiv.getOffsetWidth(), parentDiv.getOffsetHeight());		
		RootPanel.get().insert(editingLayer, parentDiv.getAbsoluteLeft(), parentDiv.getAbsoluteTop(), 0);
		
		viewer = new SeadragonViewer(deepZoomViewer);
		viewer.addMouseHandler(new SeadragonMouseHandler() {
			public void onMouseOver() {
				parentDiv.addStyleName("hover");
				parentDiv.removeStyleName("no-hover");
			}
			
			public void onMouseOut() {
				parentDiv.removeStyleName("hover");
				parentDiv.addStyleName("no-hover");
			}
		});
		
		objectURI = viewer.getObjectURI();
		
		if (getServerURL() != null)
			fetchAnnotations(getServerURL());
	}
	
	@Override
	public String getObjectURI() {
		return objectURI;
	}
	
	@Override
	public String getMediaType() {
		return MEDIATYPE;
	}
	
	@Override
	public String toFragment(BoundingBox bbox, Range range) {
		// BoundingBoxes are in viewport coordinates
		SeadragonPoint viewportBottomLeft = SeadragonPoint.create(bbox.getX(), bbox.getY() + bbox.getHeight());
		SeadragonPoint viewportTopRight = SeadragonPoint.create(bbox.getX() + bbox.getWidth(), bbox.getY());
		
		// Step 1 - convert coords to normalized Seajax World coordinates (where x = {0,1})
		SeadragonPoint seajaxBottomLeft = viewer.pointFromPixel(viewportBottomLeft);
		SeadragonPoint seajaxTopRight = viewer.pointFromPixel(viewportTopRight);
		
		// Step 2 - convert coords to true image pixel coordinates
		SeadragonPoint imgBottomLeft = viewer.toImageCoordinates(seajaxBottomLeft);
		SeadragonPoint imgTopRight = viewer.toImageCoordinates(seajaxTopRight);
		
		return "xywh=pixel:" 
			+ (int) imgBottomLeft.getX() + "," 
			+ (int) imgTopRight.getY() + "," 
			+ (int) (imgTopRight.getX() - imgBottomLeft.getX()) + ","
			+ (int) (imgBottomLeft.getY() - imgTopRight.getY());
	}

	@Override
	public Range toRange(String fragment) {
		// Zoomable images don't support range fragments
		return null;
	}

	@Override
	public BoundingBox toBoundingBox(String fragment) {
		if (fragment.startsWith("xywh=pixel:")) {
			fragment = fragment.substring(11);
		} else if (fragment.startsWith("xywh=")) {
			fragment = fragment.substring(5);
		} else {
			return null;
		}
		
		String[] xywh = fragment.split(",");
		if (xywh.length != 4)
			return null;

		int x = Integer.parseInt(xywh[0]);
		int y = Integer.parseInt(xywh[1]);
		int w = Integer.parseInt(xywh[2]);
		int h = Integer.parseInt(xywh[3]);

		// Fragments are in image pixel coordinates		
		SeadragonPoint imgBottomLeft = SeadragonPoint.create(x, y + h);
		SeadragonPoint imgTopRight = SeadragonPoint.create(x + w, y);
		
		// Step 1 - convert to Seajax World coordinates
		SeadragonPoint seajaxBottomLeft = viewer.toWorldCoordinates(imgBottomLeft);
		SeadragonPoint seajaxTopRight = viewer.toWorldCoordinates(imgTopRight);
		
		// Step 2 - convert to viewport coordinates
		SeadragonPoint viewportBottomLeft = viewer.pixelFromPoint(seajaxBottomLeft);
		SeadragonPoint viewportTopRight = viewer.pixelFromPoint(seajaxTopRight);
		
		return BoundingBox.create(
				(int) viewportBottomLeft.getX(), 
				(int) viewportTopRight.getY(),
				(int) (viewportTopRight.getX() - viewportBottomLeft.getX()),
				(int) (viewportBottomLeft.getY() - viewportTopRight.getY()));
	}
		
	@Override
	protected void onWindowResize(int width, int height) {
		RootPanel.get().setWidgetPosition(editingLayer, parentDiv.getAbsoluteLeft(), parentDiv.getAbsoluteTop());
	}
	
	@Override
	public void addAnnotation(Annotation annotation) {
		// Note: it is NOT possible to use the annotation itself as a key
		// in the HashMap, because their hashCodes change in the async event
		// handlers. That's why we use annotation's unique IDs to track them.
		// In order to track new annotations (which don't have IDs assigned yet)
		// and annotations in server-less mode, we'll assign a temporary ID here.
		// Don't really like this solution but it seems to be the only viable one.
		// (I'm open for suggestions, though!)
		if (annotation.getID() == null)
			annotation.setID("unassigned-" + Integer.toString(annotationCtr++));
		
		if (annotation.getIsReplyTo() == null) {
			// Add new overlay
			final CompoundOverlay overlay = (getRepliesEnabled()) ? 
					new CommentListSeajaxOverlay(annotation, viewer, editingLayer, this) :
					new SingleSeajaxAnnotationOverlay(annotation, viewer, editingLayer, this);

			overlays.put(annotation.getID(), overlay);
			redraw();
		} else {
			// Reply - ignore if replies are not enabled!
			if (getInitParams().isRepliesEnabled()) {
				CommentListSeajaxOverlay overlay = (CommentListSeajaxOverlay) overlays.get(annotation.getIsReplyTo());
				if (overlay != null)
					overlay.addToList(annotation);
			}
		}
	}

	@Override
	public void removeAnnotation(Annotation a) {
		// TODO code duplication with ImageAnnotationLayer -> refactor
		CompoundOverlay overlay = overlays.get(a.getID());
		if (overlay != null) {
			// No-reply mode, or reply mode + root annotation
			overlay.destroy();
			overlays.remove(a.getID());
		} else if (getRepliesEnabled() && (a.getIsReplyTo() != null)) {
			overlay = overlays.get(a.getIsReplyTo());
			if (overlay != null)
				overlay.removeAnnotation(a.getID()); 
		}
	}
	
	@Override
	public void updateAnnotation(String id, Annotation updated) {
		// TODO code duplication with ImageAnnotationLayer -> refactor
		CompoundOverlay overlay = overlays.get(id);
		if (overlay != null) {
			// No-reply mode, or reply mode + root annotation
			overlay.updateAnnotation(id, updated);
			overlays.remove(id);
			overlays.put(updated.getID(), overlay);
			redraw();
		} else if (getRepliesEnabled() && (updated.getIsReplyTo() != null)) { 
			// Reply mode + reply annotation
			overlay = overlays.get(updated.getIsReplyTo());
			if (overlay != null) {
				overlay.updateAnnotation(id, updated);
			}
		}
	}
	
	private void redraw() {
		// Re-assign z-indexes 
		// TODO code duplication with OpenLayerAnnotationLayer -> remove!
		ArrayList<CompoundOverlay> sortedOverlays = new ArrayList<CompoundOverlay>();
		for (String id : overlays.keySet()) {
			sortedOverlays.add(overlays.get(id));
		}
		Collections.sort(sortedOverlays);
				 
		int zIndex = 9010;
		for (CompoundOverlay overlay : sortedOverlays) {
			overlay.setZIndex(zIndex);
			zIndex++;
		}
	}
	
	private String createEmptyFragment() {
		return toFragment(BoundingBox.create(DEFAULT_FRAGMENT_LEFT, DEFAULT_FRAGMENT_TOP, 
				DEFAULT_SIZE, DEFAULT_SIZE), null);
	}
	
	public void createNewAnnotation() {
		final Annotation empty = createEmptyAnnotation();
		empty.setFragment(createEmptyFragment());
		addAnnotation(empty);
		
		final CompoundOverlay overlay = overlays.get(empty.getID());
		
		// It's a new annotation - we'll listen to the first save/cancel
		overlay.addAnnotationWidgetEditHandler(empty, new AnnotationWidgetEditHandler() {
			public void onStartEditing() { }
			
			public void onSave(Annotation a) { 
				// If save, just remove the handler
				final AnnotationWidgetEditHandler self = this;
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					public void execute() {
						overlay.removeAnnotationWidgetEditHandler(empty, self);	
					}
				});
			}
			
			public void onCancel() {
				// If cancel, we'll remove the annotation from the GUI
				removeAnnotation(empty);
			}
		});
		overlay.edit(empty);	
	}
	
}

