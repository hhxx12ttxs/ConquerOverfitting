/*
    VLDocking Framework 2.1
    Copyright VLSOLUTIONS, 2004-2006

    email : info@vlsolutions.com
------------------------------------------------------------------------
This software is distributed under the CeCILL license, a GNU GPL-compatible
license adapted to french law.
French and English license headers are provided at the begining of
the source files of this software application.
------------------------------------------------------------------------
LICENCE CeCILL (FRENCH VERSION).
------------------------------------------------------------------------
Ce logiciel est un programme informatique servant ? améliorer les interfaces
homme-machine d'applications Java basées sur Swing, en leur apportant un
ensemble de fonctions relatives au dockage des composants.

Ce logiciel est régi par la licence CeCILL soumise au droit français et
respectant les principes de diffusion des logiciels libres. Vous pouvez
utiliser, modifier et/ou redistribuer ce programme sous les conditions
de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
sur le site "http://www.cecill.info".

En contrepartie de l'accessibilité au code source et des droits de copie,
de modification et de redistribution accordés par cette licence, il n'est
offert aux utilisateurs qu'une garantie limitée.  Pour les m?mes raisons,
seule une responsabilité restreinte p?se sur l'auteur du programme,  le
titulaire des droits patrimoniaux et les concédants successifs.

A cet égard  l'attention de l'utilisateur est attirée sur les risques
associés au chargement,  ? l'utilisation,  ? la modification et/ou au
développement et ? la reproduction du logiciel par l'utilisateur étant
donné sa spécificité de logiciel libre, qui peut le rendre complexe ?
manipuler et qui le réserve donc ? des développeurs et des professionnels
avertis possédant  des  connaissances  informatiques approfondies.  Les
utilisateurs sont donc invités ? charger  et  tester  l'adéquation  du
logiciel ? leurs besoins dans des conditions permettant d'assurer la
sécurité de leurs syst?mes et ou de leurs données et, plus généralement,
? l'utiliser et l'exploiter dans les m?mes conditions de sécurité.

Le fait que vous puissiez accéder ? cet en-t?te signifie que vous avez
pris connaissance de la licence CeCILL, et que vous en avez accepté les
termes.

------------------------------------------------------------------------
CeCILL License (ENGLISH VERSION)
------------------------------------------------------------------------

This software is a computer program whose purpose is to enhance Human-Computer
Interfaces written in Java with the Swing framework, providing them a set of
functions related to component docking.

This software is governed by the CeCILL  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

*/


package com.vlsolutions.swing.docking.ui;

import com.vlsolutions.swing.docking.AutoHideExpandPanel;
import com.vlsolutions.swing.docking.CompoundDockingPanel;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.DockViewTitleBar;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.DockingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.PanelUI;


/** A UI for the {@link com.vlsolutions.swing.docking.DockViewTitleBar}.
 *
 * @author Lilian Chamontin, VLSolutions
 * @since 2.0
 * @update 2006/12/01 Lilian Chamontin : added client property support for dockKey, and react to visibleTitleBar
 * @update 2007/01/08 Lilian Chamontin : added support for autohide/dock disabled when hidden
 */
public class DockViewTitleBarUI extends PanelUI implements PropertyChangeListener {
  
  /* hack to use custom painting except on mac os (ugly opacity effects)  */
  private static boolean useCustomPaint = System.getProperty("os.name").
      toLowerCase().indexOf("mac os") < 0;
  
  private static Color panelColor = UIManager.getColor("Panel.background");
  private static Color highlight = UIManager.getColor("controlLtHighlight");
  private static Color shadow = UIManager.getColor("controlShadow");
  
  private static Icon closeIcon = UIManager.getIcon("DockViewTitleBar.close");
  private static Icon closeIconRollover = UIManager.getIcon("DockViewTitleBar.close.rollover");
  private static Icon closeIconPressed = UIManager.getIcon("DockViewTitleBar.close.pressed");
  
  private static Icon maximizeIcon = UIManager.getIcon("DockViewTitleBar.maximize");
  private static Icon maximizeIconRollover = UIManager.getIcon("DockViewTitleBar.maximize.rollover");
  private static Icon maximizeIconPressed = UIManager.getIcon("DockViewTitleBar.maximize.pressed");
  
  private static Icon restoreIcon = UIManager.getIcon("DockViewTitleBar.restore");
  private static Icon restoreIconRollover = UIManager.getIcon("DockViewTitleBar.restore.rollover");
  private static Icon restoreIconPressed = UIManager.getIcon("DockViewTitleBar.restore.pressed");
  
  private static Icon hideIcon = UIManager.getIcon("DockViewTitleBar.hide");
  private static Icon hideIconRollover = UIManager.getIcon("DockViewTitleBar.hide.rollover");
  private static Icon hideIconPressed = UIManager.getIcon("DockViewTitleBar.hide.pressed");
  
  private static Icon dockIcon = UIManager.getIcon("DockViewTitleBar.dock");
  private static Icon dockIconRollover = UIManager.getIcon("DockViewTitleBar.dock.rollover");
  private static Icon dockIconPressed = UIManager.getIcon("DockViewTitleBar.dock.pressed");
  
  private static Icon floatIcon = UIManager.getIcon("DockViewTitleBar.float");
  private static Icon floatIconRollover = UIManager.getIcon("DockViewTitleBar.float.rollover");
  private static Icon floatIconPressed = UIManager.getIcon("DockViewTitleBar.float.pressed");
  
  private static Icon attachIcon = UIManager.getIcon("DockViewTitleBar.attach");
  private static Icon attachIconRollover = UIManager.getIcon("DockViewTitleBar.attach.rollover");
  private static Icon attachIconPressed = UIManager.getIcon("DockViewTitleBar.attach.pressed");
  
  // label resources taken from BasicInternalFrameUI...
  private static final String CLOSE_TEXT = UIManager.getString("DockViewTitleBar.closeButtonText");
  private static final String ICONIFY_TEXT = UIManager.getString("DockViewTitleBar.minimizeButtonText");
  private static final String RESTORE_TEXT = UIManager.getString("DockViewTitleBar.restoreButtonText");
  private static final String MAXIMIZE_TEXT = UIManager.getString("DockViewTitleBar.maximizeButtonText");
  private static final String FLOAT_TEXT = UIManager.getString("DockViewTitleBar.floatButtonText");
  private static final String ATTACH_TEXT = UIManager.getString("DockViewTitleBar.attachButtonText");
  
  private static Color selectedTitleColor = UIManager.getColor("InternalFrame.activeTitleBackground");
  private static Color selectedTextColor = UIManager.getColor("InternalFrame.activeTitleForeground");
  private static Color notSelectedTitleColor = UIManager.getColor("InternalFrame.inactiveTitleBackground");
  private static Color notSelectedTextColor = UIManager.getColor("InternalFrame.inactiveTitleForeground");
  
  // flags to hide/show buttons in the title bar (they are always visible in the contextual menu, but might
  // take too much space on the titles (for example a minimum set could be hide/float/close
  //  as maximize is accessed by double click)
  private boolean isCloseButtonDisplayed = UIManager.getBoolean("DockViewTitleBar.isCloseButtonDisplayed");
  private boolean isHideButtonDisplayed = UIManager.getBoolean("DockViewTitleBar.isHideButtonDisplayed");
  private boolean isDockButtonDisplayed = UIManager.getBoolean("DockViewTitleBar.isDockButtonDisplayed");
  private boolean isMaximizeButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isMaximizeButtonDisplayed");
  private boolean isRestoreButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isRestoreButtonDisplayed");
  private boolean isFloatButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isFloatButtonDisplayed");
  private boolean isAttachButtonDisplayed= UIManager.getBoolean("DockViewTitleBar.isAttachButtonDisplayed");
  
  protected DockViewTitleBar titleBar;
  
  /* This ancestor listener is required as buttons may change according to container hierarchy.
   * The first example is when a dockable is added to a floating + compund dockable, the attach
   * button (which usually becomes visible as the dockable is in the floating state) has to be hidden
   * (tech choice : we don't want to allow attaching a single child of a compound dockable)
   *
   */
  private AncestorListener ancestorListener = new AncestorListener() {
    public void ancestorAdded(AncestorEvent event) {
      configureButtons(titleBar);
    }
    public void ancestorMoved(AncestorEvent event) {
    }
    public void ancestorRemoved(AncestorEvent event) {
    }
  };
  
  public DockViewTitleBarUI(DockViewTitleBar tb) {
    this.titleBar = tb;
  }
  
  public static ComponentUI createUI(JComponent c) {
    return new DockViewTitleBarUI((DockViewTitleBar) c);
  }
  
  public void installUI(JComponent c) {
    super.installUI(c);
    installTitleBorder(c);
    titleBar.addPropertyChangeListener(this);
    titleBar.addAncestorListener(ancestorListener);
    installButtons();
    installLabel();
    Dockable d = titleBar.getDockable();
    if (d != null){
      titleBar.getDockable().getDockKey().addPropertyChangeListener(this);
      configureButtons(titleBar);
    }
    c.setCursor(Cursor.getDefaultCursor()); // needs this to avoid artifacts with floating dockable borders.
    
  }
  
  public void uninstallUI(JComponent c){
    //System.out.println("UNISTALL UI");
    super.uninstallUI(c);
    uninstallTitleBorder(c);
    titleBar.removePropertyChangeListener(this);
    titleBar.removeAncestorListener(ancestorListener);
    Dockable d = titleBar.getDockable();
    if (d != null){
      d.getDockKey().removePropertyChangeListener(this);
    } 
  }
  
  /**  Installs default on the titlebar label */
  protected void installLabel(){
    JLabel titleLabel = titleBar.getTitleLabel();
    Font f = UIManager.getFont("DockViewTitleBar.titleFont");
    titleLabel.setFont(f);
    titleLabel.setForeground(notSelectedTextColor);
    titleBar.setBackground(notSelectedTitleColor);
    
    // adjust minimum size because of BoxLayout usage (doesn't allow resize under minimum size)
    titleLabel.setMinimumSize(new Dimension(30, titleLabel.getPreferredSize().height));
  }
  
  /** Installs the default background of the title bar */
  protected void installBackground(){
    titleBar.setBackground(notSelectedTitleColor);
  }
  
  /** configure the title bar buttons */
  protected void installButtons(){
    JButton closeButton = titleBar.getCloseButton();
    JButton dockButton = titleBar.getHideOrDockButton();
    JButton maximizeButton = titleBar.getMaximizeOrRestoreButton();
    JButton floatButton = titleBar.getFloatButton();
    
    Insets buttonMargin = new Insets(0, 2, 0, 2); //todo put this in UI
    closeButton.setMargin(buttonMargin);
    dockButton.setMargin(buttonMargin);
    maximizeButton.setMargin(buttonMargin);
    floatButton.setMargin(buttonMargin);
    
    dockButton.setRolloverEnabled(true);
    dockButton.setBorderPainted(false);
    dockButton.setFocusable(false);
    dockButton.setContentAreaFilled(false);
    
    closeButton.setRolloverEnabled(true);
    closeButton.setBorderPainted(false);
    closeButton.setFocusable(false);
    closeButton.setContentAreaFilled(false);
    
    maximizeButton.setRolloverEnabled(true);
    maximizeButton.setBorderPainted(false);
    maximizeButton.setFocusable(false);
    maximizeButton.setContentAreaFilled(false);
    
    floatButton.setRolloverEnabled(true);
    floatButton.setBorderPainted(false);
    floatButton.setFocusable(false);
    floatButton.setContentAreaFilled(false);
    
  }
  
  /** Listen to property changes in the DockKey or the title bar  */
  public void propertyChange(PropertyChangeEvent e){
    String pName = e.getPropertyName();
    //System.out.println("property change " + pName);
    if (pName.equals("dockable")){
      Dockable old = (Dockable)e.getOldValue();
      if (old != null){
        old.getDockKey().removePropertyChangeListener(this);
      }
      Dockable newDockable = ((Dockable)e.getNewValue());
      if (newDockable != null){
        configureButtons(titleBar);
        newDockable.getDockKey().addPropertyChangeListener(this);
      }
    } else if (pName.equals(DockKey.PROPERTY_AUTOHIDEABLE)){
      boolean isAutoHideable = ((Boolean)e.getNewValue()).booleanValue();
      boolean isMaximized = titleBar.getDockable()
      .getDockKey().getDockableState() == DockableState.STATE_MAXIMIZED;
      if (isAutoHideable){
        if (! isMaximized){
          titleBar.getHideOrDockButton().setVisible(true);
          configureHideButton(titleBar.getHideOrDockButton());
        }
      } else {
        titleBar.getHideOrDockButton().setVisible(false);
      }
    } else if (pName.equals(DockKey.PROPERTY_MAXIMIZABLE)){
      boolean isMaximizeable = ((Boolean)e.getNewValue()).booleanValue();
      boolean isHidden = titleBar.getDockable()
      .getDockKey().getDockableState() == DockableState.STATE_HIDDEN;
      if (isMaximizeable){
        if (! isHidden){
          titleBar.getMaximizeOrRestoreButton().setVisible(true);
          configureMaximizeButton(titleBar.getMaximizeOrRestoreButton());
        }
      } else {
        titleBar.getMaximizeOrRestoreButton().setVisible(false);
      }
    } else if (pName.equals(DockKey.PROPERTY_CLOSEABLE)) {
      boolean isCloseable = ((Boolean)e.getNewValue()).booleanValue();
      boolean isMaximized = titleBar.getDockable()
      .getDockKey().getDockableState() == DockableState.STATE_MAXIMIZED;
      if (isCloseable){
        if (! isMaximized){
          titleBar.getCloseButton().setVisible(true);
          configureCloseButton(titleBar.getCloseButton());
        }
      } else {
        titleBar.getCloseButton().setVisible(false);
      }
    } else if (pName.equals(DockKey.PROPERTY_FLOATABLE)) {
      boolean isFloatable = ((Boolean)e.getNewValue()).booleanValue();
      boolean isMaximized = titleBar.getDockable()
      .getDockKey().getDockableState() == DockableState.STATE_MAXIMIZED;
      if (isFloatable){
        if (! isMaximized){
          titleBar.getFloatButton().setVisible(true);
          configureFloatButton(titleBar.getFloatButton());
        }
      } else {
        titleBar.getFloatButton().setVisible(false);
      }
    } else if (pName.equals(DockKey.PROPERTY_DOCKABLE_STATE)){
      configureButtons(titleBar);
    } else if (pName.equals("active")){
      boolean isActive = ((Boolean)e.getNewValue()).booleanValue();
      if (isActive) {
        titleBar.getTitleLabel().setForeground(selectedTextColor);
        titleBar.setBackground(selectedTitleColor);
      } else {
        titleBar.getTitleLabel().setForeground(notSelectedTextColor);
        titleBar.setBackground(notSelectedTitleColor);
      }
      titleBar.repaint();
    } else if (pName.equals("titlebar.notification")){
      boolean notification = ((Boolean)e.getNewValue()).booleanValue();
      if (notification) {
        titleBar.setBackground(UIManager.getColor("DockingDesktop.notificationColor"));
        titleBar.setOpaque(true);
      } else {
        if (titleBar.isActive()) {
          titleBar.setBackground(selectedTitleColor);
        } else {
          titleBar.setBackground(notSelectedTitleColor);
        }
        titleBar.setOpaque(false);
      }
      titleBar.repaint();
    } else if (pName.equals(DockKey.PROPERTY_NAME)){
      titleBar.repaint();
    } else if (pName.equals("clientProperty.visibleTitleBar")){ // 2006/12/01
      boolean v = Boolean.TRUE.equals(e.getNewValue());
      titleBar.setVisible(v);
    }
  }
  
  /** Update the buttons to track state changes (for example, the maximize button can become "restore"
   * when the view is maximized.
   */
  protected void configureButtons(DockViewTitleBar tb){
    DockKey key = tb.getDockable().getDockKey();
    int state = key.getDockableState();
    JButton closeButton = titleBar.getCloseButton();
    JButton maxBtn = titleBar.getMaximizeOrRestoreButton();
    JButton hideBtn = titleBar.getHideOrDockButton();
    JButton floatBtn = titleBar.getFloatButton();
    
    switch (state){
      case DockableState.STATE_DOCKED:
        if (key.isCloseEnabled() && isCloseButtonDisplayed){
          closeButton.setVisible(true);
          configureCloseButton(closeButton);
        } else {
          closeButton.setVisible(false);
        }
        if (key.isMaximizeEnabled() && isMaximizeButtonDisplayed){
          maxBtn.setVisible(true);
          configureMaximizeButton(maxBtn);
        } else {
          maxBtn.setVisible(false);
        }
        
        boolean isChildOfMaximizedContainer = false;
        DockingDesktop desk = titleBar.getDesktop();
        if (desk != null){
          Dockable max = desk.getMaximizedDockable();
          if (max != null && max.getComponent().getParent().isAncestorOf(titleBar)){
            isChildOfMaximizedContainer = true;
          }
        }
        
        
        if (key.isAutoHideEnabled() && isHideButtonDisplayed && ! isChildOfMaximizedContainer){
          hideBtn.setVisible(true);
          configureHideButton(hideBtn);
        } else {
          hideBtn.setVisible(false);
        }
        if (key.isFloatEnabled() && isFloatButtonDisplayed && !isChildOfMaximizedContainer){
          floatBtn.setVisible(true);
          configureFloatButton(floatBtn);
        } else {
          floatBtn.setVisible(false);
        }
        
        titleBar.revalidate();
        break;
      case DockableState.STATE_HIDDEN:
        if (key.isCloseEnabled() && isCloseButtonDisplayed){
          closeButton.setVisible(true);
          configureCloseButton(closeButton);
        } else {
          closeButton.setVisible(false);
        }
        // maximize not allowed when in autohide mode
        maxBtn.setVisible(false);
        
        boolean isChildOfCompound = DockingUtilities.isChildOfCompoundDockable(tb.getDockable());
        
        // idem for float
        if (key.isFloatEnabled() && isFloatButtonDisplayed && !isChildOfCompound){
          floatBtn.setVisible(true);
          configureFloatButton(floatBtn);
        } else {
          floatBtn.setVisible(false);
        }
        // hide becomes dock
        if (isChildOfCompound){
          //tb.getParent().getParent() instanceof AutoHideExpandPanel
          // V2.1 : only when not nested
          hideBtn.setVisible(false);
        } else {
          if (isDockButtonDisplayed 
              && key.isAutoHideEnabled()){ // 2007/01/08
            hideBtn.setVisible(true);
            configureDockButton(hideBtn);
          } else {
            hideBtn.setVisible(false);
          }
        }
        titleBar.revalidate();
        break;
      case DockableState.STATE_MAXIMIZED:
        closeButton.setVisible(false);
        floatBtn.setVisible(false);
        // maxBtn becomes restore
        if (isRestoreButtonDisplayed){
          configureRestoreButton(maxBtn);
          maxBtn.setVisible(true);
        } else {
          maxBtn.setVisible(false);
        }
        hideBtn.setVisible(false);
        titleBar.revalidate();
        break;
      case DockableState.STATE_FLOATING:
        closeButton.setVisible(false);
        maxBtn.setVisible(false);
        hideBtn.setVisible(false);
        if (DockingUtilities.isChildOfCompoundDockable(tb.getDockable())){
          // cannot attach a compound dockable directly
          floatBtn.setVisible(false);
        } else {
          floatBtn.setVisible(isAttachButtonDisplayed);
          configureAttachButton(floatBtn);
        }
        break;
      default :
        // not interesting
    }
    
  }
  
  /** installs the icons and tooltip suitable for a close button */
  protected void configureCloseButton(JButton btn){
    btn.setIcon(closeIcon);
    btn.setRolloverIcon(closeIconRollover);
    btn.setPressedIcon(closeIconPressed);
    btn.setRolloverSelectedIcon(closeIconRollover);
    btn.setToolTipText(CLOSE_TEXT);
  }
  
  
  /** installs the icons and tooltip suitable for a maximize button */
  protected void configureMaximizeButton(JButton btn){
    btn.setIcon(maximizeIcon);
    btn.setRolloverIcon(maximizeIconRollover);
    btn.setPressedIcon(maximizeIconPressed);
    btn.setRolloverSelectedIcon(maximizeIconRollover);
    btn.setToolTipText(MAXIMIZE_TEXT);
  }
  
  /** installs the icons and tooltip suitable for a restore button.
   */
  protected void configureRestoreButton(JButton btn){
    btn.setIcon(restoreIcon);
    btn.setRolloverIcon(restoreIconRollover);
    btn.setPressedIcon(restoreIconPressed);
    btn.setRolloverSelectedIcon(restoreIconRollover);
    btn.setToolTipText(RESTORE_TEXT);
  }
  
  /** installs the icons and tooltip suitable for a hide button */
  protected void configureHideButton(JButton btn){
    btn.setIcon(hideIcon);
    btn.setRolloverIcon(hideIconRollover);
    btn.setPressedIcon(hideIconPressed);
    btn.setRolloverSelectedIcon(hideIconRollover);
    btn.setToolTipText(ICONIFY_TEXT);
  }
  
  /** installs the icons and tooltip suitable for a dock button */
  protected void configureDockButton(JButton btn){
    btn.setIcon(dockIcon);
    btn.setRolloverIcon(dockIconRollover);
    btn.setPressedIcon(dockIconPressed);
    btn.setRolloverSelectedIcon(dockIconRollover);
    btn.setToolTipText(RESTORE_TEXT);
  }
  
  /** installs the icons and tooltip suitable for a float button */
  protected void configureFloatButton(JButton btn){
    btn.setIcon(floatIcon);
    btn.setRolloverIcon(floatIconRollover);
    btn.setPressedIcon(floatIconPressed);
    btn.setRolloverSelectedIcon(floatIconRollover);
    btn.setToolTipText(FLOAT_TEXT);
  }
  
  /** installs the icons and tooltip suitable for an attach button */
  protected void configureAttachButton(JButton btn){
    btn.setIcon(attachIcon);
    btn.setRolloverIcon(attachIconRollover);
    btn.setPressedIcon(attachIconPressed);
    btn.setRolloverSelectedIcon(attachIconRollover);
    btn.setToolTipText(ATTACH_TEXT);
  }
  
  /** installs the border of the title bar */
  protected void installTitleBorder(JComponent c){
    Border b = UIManager.getBorder("DockViewTitleBar.border");
    c.setBorder(b);
    
  }
  
  /** uninstalls the icons and tooltip suitable for a close button */
  protected void uninstallTitleBorder(JComponent c){
    c.setBorder(null);
  }
  
  /**  Custom title bar painting : uses a gradient from the background color
   * to the control highlight color.
   */
  public void paint(Graphics g, JComponent c){
    DockViewTitleBar tb = (DockViewTitleBar)c;
    if (useCustomPaint) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(panelColor);
      g2.fillRect(0, 0, tb.getWidth(), tb.getHeight()); // emptyborder doesnt repaint
      
      Insets i = tb.getInsets();
      g2.setColor(tb.getBackground());
      Rectangle r = tb.getTitleLabel().getBounds();
      int w = r.x + r.width;
      g2.fillRect(i.left, i.top, w,
          tb.getHeight() - i.top - i.bottom);
      // gradient paint after the label text (to ensure readeability)
      if (tb.isActive()) {
        g2.setPaint(new GradientPaint(i.left + w, 0, tb.getBackground(),
            tb.getWidth(), 0,
            highlight));
      } else {
        g2.setPaint(new GradientPaint(i.left + w, 0, tb.getBackground(),
            tb.getWidth(), 0,
            highlight)); //panelColor));
      }
      g2.fillRect(i.left + w, i.top,
          tb.getWidth() - w - i.left - i.right,
          tb.getHeight() - i.top - i.bottom);
      
      g2.dispose();
    }
    super.paint(g, c);
    
    
  }
  
}

