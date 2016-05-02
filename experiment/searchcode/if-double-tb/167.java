/*
 * @(#)UMLApplicationModel.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw and all its
 * contributors. All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the 
 * license agreement you entered into with the copyright holders. For details
 * see accompanying license terms.
 */
package us.groupa.uml;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.jhotdraw.app.action.edit.RedoAction;
import org.jhotdraw.app.action.edit.UndoAction;
import org.jhotdraw.app.action.view.ToggleViewPropertyAction;
import org.jhotdraw.app.action.view.ViewPropertyAction;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.TextAreaCreationTool;
import org.jhotdraw.draw.tool.ConnectionTool;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.jhotdraw.app.*;
import org.jhotdraw.app.action.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.action.*;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.gui.JFileURIChooser;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.gui.filechooser.ExtensionFileFilter;
import org.jhotdraw.util.*;

import us.groupa.uml.figures.*;
import us.groupa.uml.figures.RelationshipEntity.Relationship;

/**
 * UMLApplicationModel.
 * 
 * @author Werner Randelshofer.
 * @version $Id: UMLApplicationModel.java 717 2010-11-21 12:30:57Z rawcoder $
 */

/*
 * A derivation of the Pert PertApplicationModel, refactored, many changes made and marked within code.
 *
 * Changes made by: Jason Ford, Emily Hughes, Steven Goodman
 *
 */

public class UMLApplicationModel extends DefaultApplicationModel {

    private final static double[] scaleFactors = {5, 4, 3, 2, 1.5, 1.25, 1, 0.75, 0.5, 0.25, 0.10};

    private static class ToolButtonListener implements ItemListener {

        private Tool tool;
        private DrawingEditor editor;

        public ToolButtonListener(Tool t, DrawingEditor editor) {
            this.tool = t;
            this.editor = editor;
        }

        @Override
        public void itemStateChanged(ItemEvent evt) {
            if (evt.getStateChange() == ItemEvent.SELECTED) {
                editor.setTool(tool);
            }
        }
    }
    /**
     * This editor is shared by all views.
     */
    private DefaultDrawingEditor sharedEditor;
    private HashMap<String, Action> actions;

    /** Creates a new instance. */
    public UMLApplicationModel() {
    }

    @Override
    public ActionMap createActionMap(Application a, @Nullable View v) {
        ActionMap m = super.createActionMap(a, v);
        ResourceBundleUtil drawLabels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        AbstractAction aa;

        m.put(ExportSourceAction.ID, new ExportSourceAction(a, v));
        m.put("view.toggleGrid", aa = new ToggleViewPropertyAction(a, v, UMLView.GRID_VISIBLE_PROPERTY));
        drawLabels.configureAction(aa, "view.toggleGrid");
        for (double sf : scaleFactors) {
            m.put((int) (sf * 100) + "%",
                    aa = new ViewPropertyAction(a, v, DrawingView.SCALE_FACTOR_PROPERTY, Double.TYPE, new Double(sf)));
            aa.putValue(Action.NAME, (int) (sf * 100) + " %");

        }
        return m;
    }

    public DefaultDrawingEditor getSharedEditor() {
        if (sharedEditor == null) {
            sharedEditor = new DefaultDrawingEditor();
        }
        return sharedEditor;
    }

    @Override
    public void initView(Application a, @Nullable View p) {
        if (a.isSharingToolsAmongViews()) {
            ((UMLView) p).setEditor(getSharedEditor());
        }
    }

    private void addCreationButtonsTo(JToolBar tb, final DrawingEditor editor) {
        // AttributeKeys for the entity sets
        HashMap<AttributeKey, Object> attributes;

        ResourceBundleUtil labels = ResourceBundleUtil.getBundle("us.groupa.uml.Labels");
        ResourceBundleUtil drawLabels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");

        ButtonFactory.addSelectionToolTo(tb, editor);
        tb.addSeparator();
        
        /*
         * Code below this point has been modified from the original PertApplicationModel
         */ 

        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.FILL_COLOR, new Color(0xF6ED93));
        attributes.put(AttributeKeys.STROKE_COLOR, Color.black);
        attributes.put(AttributeKeys.TEXT_COLOR, Color.black);
        ButtonFactory.addToolTo(tb, editor, new CreationTool(new ClassFigure(), attributes), "edit.createClass", labels);

        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
        attributes.put(AttributeKeys.END_DECORATION, new ArrowTip(.35,12,11.3,false,true,true));
        ButtonFactory.addToolTo(tb, editor, new ConnectionTool(new InheritanceFigure(), attributes), "edit.createInheritance", labels);
        
        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
        ButtonFactory.addToolTo(tb, editor, new ConnectionTool(new DependencyFigure(), attributes), "edit.createDependency", labels);
        
        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
        ButtonFactory.addToolTo(tb, editor, new ConnectionTool(new AssociationFigure(Relationship.Unidirectional), attributes), "edit.createUnidirectional", labels);
        
        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
        ButtonFactory.addToolTo(tb, editor, new ConnectionTool(new AssociationFigure(Relationship.UnidirectionalAggregation), attributes), "edit.createUnidirectionalAggregation", labels);
        
        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
        ButtonFactory.addToolTo(tb, editor, new ConnectionTool(new AssociationFigure(Relationship.Bidirectional), attributes), "edit.createBidirectional", labels);
        
        attributes = new HashMap<AttributeKey, Object>();
        attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
        ButtonFactory.addToolTo(tb, editor, new ConnectionTool(new AssociationFigure(Relationship.BidirectionalAggregation), attributes), "edit.createBidirectionalAggregation", labels);
        /*
         * End changed code
         */
        
        tb.addSeparator();
        ButtonFactory.addToolTo(tb, editor, new TextAreaCreationTool(new TextAreaFigure()), "edit.createTextArea", drawLabels);
    }

    /**
     * Creates toolbars for the application.
     * This class always returns an empty list. Subclasses may return other
     * values.
     */
    @Override
    public java.util.List<JToolBar> createToolBars(Application a, @Nullable View pr) {
        ResourceBundleUtil drawLabels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
        UMLView p = (UMLView) pr;

        DrawingEditor editor;
        if (p == null) {
            editor = getSharedEditor();
        } else {
            editor = p.getEditor();
        }

        LinkedList<JToolBar> list = new LinkedList<JToolBar>();
        JToolBar tb;
        tb = new JToolBar();
        addCreationButtonsTo(tb, editor);
        tb.setName(drawLabels.getString("window.drawToolBar.title"));
        list.add(tb);
        return list;
    }

    /** Creates the MenuBuilder. */
    @Override
    protected MenuBuilder createMenuBuilder() {
        return new DefaultMenuBuilder() {
        	
        	@Override
        	public void addUndoItems(JMenu m, Application app, @Nullable View v) {
        		//no undo please
        	}
            @Override
            public void addOtherViewItems(JMenu m, Application app, @Nullable View v) {
                ActionMap am = app.getActionMap(v);
                JCheckBoxMenuItem cbmi;
                cbmi = new JCheckBoxMenuItem(am.get("view.toggleGrid"));
                ActionUtil.configureJCheckBoxMenuItem(cbmi, am.get("view.toggleGrid"));
                m.add(cbmi);
                JMenu m2 = new JMenu("Zoom");
                for (double sf : scaleFactors) {
                    String id = (int) (sf * 100) + "%";
            cbmi = new JCheckBoxMenuItem(am.get(id));
            ActionUtil.configureJCheckBoxMenuItem(cbmi, am.get(id));
            m2.add(cbmi);
                }
                m.add(m2);
            }
            
            /*
             * Code below this point has been modified from the original PertApplicationModel
             */
            
            public void addOtherFileItems(JMenu m, Application app, @Nullable View v) {
            	ActionMap am = app.getActionMap(v);
                if (v instanceof UMLView){															
                	final UMLView uv = (UMLView) v;
                    JMenuItem jmiObserver;
                    jmiObserver = new JMenuItem("Create Observer Diagram");
                    jmiObserver.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		uv.createObserverDiagram();
                    	}
                    });
                    m.add(jmiObserver);
                }

                if (v instanceof UMLView){																// v is instanceof UMLView, not DefaultDrawingView
                	final UMLView uv = (UMLView) v;
                    JMenuItem jmiAssociation;
                    jmiAssociation = new JMenuItem("Create First Demo Diagram");
                    jmiAssociation.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		uv.createAssociationDemoDiagram();
                    	}

                    });
                    m.add(jmiAssociation);                    
                }
            
                if (v instanceof UMLView){																// v is instanceof UMLView, not DefaultDrawingView
                	final UMLView uv = (UMLView) v;
                    JMenuItem jmiDemo;
                    jmiDemo = new JMenuItem("Create Second Demo Diagram");
                    jmiDemo.addActionListener(new ActionListener() {
                    	public void actionPerformed(ActionEvent e) {
                    		uv.createOtherDemoDiagram();
                    	}

                    });
                    m.add(jmiDemo);                    
                }
            }
            
            /*
             * End changed code
             */
        };
    }

    @Override
    public URIChooser createOpenChooser(Application a, @Nullable View v) {
        JFileURIChooser c = new JFileURIChooser();
        c.addChoosableFileFilter(new ExtensionFileFilter("UML Diagram", "xml"));
        return c;
    }

    @Override
    public URIChooser createSaveChooser(Application a, @Nullable View v) {
        JFileURIChooser c = new JFileURIChooser();
        c.addChoosableFileFilter(new ExtensionFileFilter("UML Diagram", "xml"));
        return c;
    }
    
    @Override
    public URIChooser createExportChooser(Application a, @Nullable View v) {
    	JFileURIChooser c = new JFileURIChooser();
    	c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return c;
    }
}
