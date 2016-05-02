package com.softaria.windows.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.softaria.windows.factories.BadUrlException;
import com.softaria.windows.factories.CloseConfirmation;
import com.softaria.windows.factories.ViewFactory;
import com.softaria.windows.model.AbsoluteSizeCalculator;
import com.softaria.windows.model.CascadePositionCalculator;
import com.softaria.windows.model.Perspective;
import com.softaria.windows.model.PositionCalculator;
import com.softaria.windows.model.SizeCalculator;
import com.softaria.windows.model.View;
import com.softaria.windows.model.ViewUrl;
import com.softaria.windows.widgets.Frame;
import com.softaria.windows.widgets.FrameActivationListener;
import com.softaria.windows.widgets.FrameButtonPressedListener;
import com.softaria.windows.widgets.ResizeListenerRegistrator;

/**
 * Manages {@link View}s. Does not deal with {@link Perspective} or
 * {@link PerspectiveManager} {@link View}s can belong to different
 * {@link AbsolutePanel}. Used exclusively by {@link WindowsManager}
 * 
 * @author Roman M Kuzmin (roman@softaria.com)
 * 
 */
class ViewManager {

  /**
   * Internal structure which holds together {@link View}, corresponding
   * {@link Frame} and parent's panel of this Frame
   * 
   * @author Roman M Kuzmin (roman@softaria.com)
   * 
   */
  private class ViewAndFrame {
    public View view;
    public Frame frame;
    public AbsolutePanel panel;

    public ViewAndFrame(View view, Frame frame, AbsolutePanel panel) {
      super();
      this.view = view;
      this.frame = frame;
      this.panel = panel;
    }

  }

  private ViewFactory viewFactory;
  private MaximizedFramesResizeManager maxFrameResizeManager;

  private Frame activeFrame;

  private HashMap<ViewUrl, ViewAndFrame> openedFrames = new HashMap<ViewUrl, ViewAndFrame>();

  // Let perspective have its own widgets "below" views
  private int zIndex = 1000;

  /**
   * 
   * @param viewFactory
   *          - factory for creating {@link View}s from {@link ViewUrl}s
   * @param maxFrameResizeManager
   */
  public ViewManager(ViewFactory viewFactory, MaximizedFramesResizeManager maxFrameResizeManager) {
    super();
    this.viewFactory = viewFactory;
    this.maxFrameResizeManager = maxFrameResizeManager;

  }

  /**
   * creates or (if already exists) activates View. Called exlusively by
   * {@link WindowsManager}
   * 
   * Most parameters are used only when we are creating new View. When
   * activating existing View they are ignored
   * 
   * @param url
   * @param target
   *          - parent for newly constructed Frame (ignored when we are
   *          activating existing view)
   * @param activationListener
   *          - listener that will be called when user clicks on Frame (ignored
   *          when we are activating existing view)
   * @param closeListener
   *          - listener that will be called when user clicks on X button of
   *          {@link Frame} (ignored when we are activating existing view)
   * @param position
   *          - calculator for calculating position of Frame (ignored when we
   *          are activating existing view)
   * @param size
   *          - calculator for calculating size of Frame ((ignored when we are
   *          activating existing view)
   * @param resizeManager
   *          - required by Frame to be notified about browser's window
   *          resizing. When {@link Frame} is maximized it must know about this
   *          to throw resize event to its listeners
   * @param helpManager
   *          - used to construct {@link HelpWriter} and pass it to
   *          {@link ViewFactory}
   * 
   * @throws BadUrlException
   *           when {@link ViewFactory} does so
   */
  void openView(ViewUrl url, AbsolutePanel target, FrameActivationListener activationListener,
      FrameButtonPressedListener closeListener, PositionCalculator position, SizeCalculator size,
      HelpManager helpManager, boolean neverMaximize) throws BadUrlException {

    ViewAndFrame vf = openedFrames.get(url);

    if (vf == null) {

      ResizeListenerRegistrator frlr = new ResizeListenerRegistrator();

      HelpWriter helpWriter = new HelpWriter(url, helpManager);

      View view = viewFactory.create(url, new CloseListenerRegistratorImpl(url), frlr, helpWriter);

      if (size == null) {
        size = view.getSizeCalculator();
        if (size == null) {
          size = new AbsoluteSizeCalculator(100, 100);
        }
      }

      if (position == null) {
        position = view.getPositionCalculator();
        if (position == null) {
          position = getCascadePositionCalculator(target);
        }
      }

      boolean needMaximize = true;

      if ((neverMaximize) || (view.isFlagSet(View.NEVER_MAXIMIZE))) {
        needMaximize = false;
      } else {
        Set<ViewUrl> openedViews = getOpenedViews(target);
        if (!openedViews.isEmpty()) {
          for (ViewUrl openedViewUrl : openedViews) {
            ViewAndFrame openedVF = openedFrames.get(openedViewUrl);
            if (!openedVF.frame.isMaximized()) {
              needMaximize = false;
              break;
            }
          }
        }
      }

      Frame frame = new Frame(target, view.getName(), view.getContent(), position, size,
          closeListener, frlr);

      frame.addActivationListener(activationListener);

      vf = new ViewAndFrame(view, frame, target);

      openedFrames.put(url, vf);

      if (needMaximize) {
        maximizeView(url);
      }

    }

    setActiveFrame(url);
  }

  private PositionCalculator getCascadePositionCalculator(AbsolutePanel target) {

    if (activeFrame != null) {
      if (activeFrame.getContainer().equals(target)) {
        return new CascadePositionCalculator(activeFrame);
      }
    }

    for (ViewUrl viewUrl : openedFrames.keySet()) {
      ViewAndFrame vaf = openedFrames.get(viewUrl);
      if (vaf.panel.equals(target)) {
        return new CascadePositionCalculator(vaf.frame);
      }
    }

    return new CascadePositionCalculator(null);
  }

  private void setActiveFrame(ViewUrl url) {

    ViewAndFrame vf = openedFrames.get(url);

    if (vf != null) {
      // new frame exists
      if (activeFrame != null) {
        // old frame exists
        if (activeFrame.equals(vf.frame)) {
          // do nothing - old frame is our
          return;
        } else {
          // set old frame inactive
          activeFrame.setSelectionColorScheme(false);
          activeFrame = null;
        }
      }

      // Set new frame active (inspite of existing of old frame):
      activeFrame = vf.frame;
      activeFrame.setSelectionColorScheme(true);
      // ATTENTION: This (commented) way breaks events and makes widgets
      // inside
      // frame unclickable!
      // activeFrame.removeFromParent();
      // vf.panel.add(activeFrame, left, top);

      // So, now we will just increase global zIndex (which may be up to 2
      // billions at least)
      DOM.setIntStyleAttribute(activeFrame.getElement(), "zIndex", zIndex++);

    } else {
      // no new frame:
      if (activeFrame != null) {
        // set old frame inactive
        activeFrame.setSelectionColorScheme(false);
        activeFrame = null;

      }
    }

  }

  /**
   * Closes view if it is opened. Used only by {@link WindowsManager}
   * 
   * @param url
   */
  void closeView(ViewUrl url) {
    ViewAndFrame vf = openedFrames.get(url);

    if (vf != null) {
      if (isActiveFrame(vf.frame)) {
        setActiveFrame(null);
      }
      vf.frame.close();
      openedFrames.remove(url);

      // Notify view that it was closed
      CloseListenerRegistratorImpl.notifyViewClosed(url);
    }
  }

  private boolean isActiveFrame(Frame frame) {

    return frame == activeFrame;
  }

  /**
   * To be called by {@link WindowsManager}
   * 
   * @return set of opened views (located in all panels)
   */
  Set<ViewUrl> getOpenedViews() {

    return openedFrames.keySet();

  }

  /**
   * 
   * @param parent
   * @return set of opened views that belong to the same parent panel
   */
  Set<ViewUrl> getOpenedViews(AbsolutePanel parent) {

    Set<ViewUrl> ret = new HashSet<ViewUrl>();

    for (ViewUrl viewUrl : openedFrames.keySet()) {
      ViewAndFrame vaf = openedFrames.get(viewUrl);
      if (vaf.panel.equals(parent)) {
        ret.add(viewUrl);
      }
    }

    return ret;
  }

  /**
   * Initiates closing process. To be called by {@link WindowsManager}
   * 
   * @param url
   * @param closeConfirmation
   */
  void tryClose(ViewUrl url, CloseConfirmation closeConfirmation) {
    CloseListenerRegistratorImpl.tryClose(url, closeConfirmation);

  }

  public void minimizeView(ViewUrl viewUrl) {
    ViewAndFrame vf = openedFrames.get(viewUrl);

    if (vf != null) {
      maxFrameResizeManager.removeFrame(vf.frame);
      vf.frame.minimize();
    }

  }

  public void maximizeView(ViewUrl viewUrl) {
    ViewAndFrame vf = openedFrames.get(viewUrl);

    if (vf != null) {
      vf.frame.maximize();
      maxFrameResizeManager.addFrame(vf.frame);
    }

  }

}

