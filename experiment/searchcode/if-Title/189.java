/*
 * Copyright (C) 2011, EADS France
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package xowl.gmi.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Laurent WOUTERS
 */
public class Libraries {
    private Controller controller;
    private Map<xowl.gmi.model.cvssl.Symbol, String> symbolToLib;
    private Map<xowl.gmi.model.ast.ASTNode, LibraryElement> astToTBE;
    private Map<xowl.gmi.model.ast.ASTNode, String> astToLib;
    private Map<xowl.gmi.model.ast.ASTNode, xowl.interpreter.adapter.ObjectPropertyObserver> astToObserver;
    private xowl.gmi.view.Libraries view;
    
    public xowl.gmi.view.CollapsePanel getView() { return view; }
    
    public Libraries(Controller controller, xowl.gmi.model.cvssl.ViewDefinition def) {
        this.controller = controller;
        this.symbolToLib = new HashMap<>();
        this.astToTBE = new HashMap<>();
        this.astToLib = new HashMap<>();
        this.astToObserver = new HashMap<>();
        this.view = new xowl.gmi.view.Libraries(controller.getLocale());
        
        List<xowl.gmi.model.cvssl.Library> libraries = new ArrayList<>(def.getAllLibraries());
        Collections.sort(libraries, new Comparator<xowl.gmi.model.cvssl.Library>() {
            public int compare(xowl.gmi.model.cvssl.Library o1, xowl.gmi.model.cvssl.Library o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
        for (xowl.gmi.model.cvssl.Library lib : libraries) {
            String title = lib.getDescription();
            xowl.gmi.model.cvssl.Symbol symbol = lib.getSymbol();
            this.symbolToLib.put(symbol, title);
            this.view.addLibray(title);
        }
        
        for (xowl.gmi.model.ast.ASTNode node : xowl.gmi.model.ast.ASTNode.getAll(controller.getRepository())) {
            xowl.gmi.model.cvssl.Symbol symbol = node.getSymbol();
            if (symbol == null) watch(node);
            else insert(node);
        }
        
        xowl.gmi.model.ast.ASTNode.addObserver(controller.getRepository(), new xowl.interpreter.adapter.ClassObserver<xowl.gmi.model.ast.ASTNode>() {
            @Override public void onNewInstance(xowl.gmi.model.ast.ASTNode node) {
                if (node.getOntology() != Libraries.this.controller.getOntologyAST())
                    return;
                xowl.gmi.model.cvssl.Symbol symbol = node.getSymbol();
                if (symbol == null) watch(node);
                else insert(node);
            }
            @Override public void onDestroyInstance(xowl.gmi.model.ast.ASTNode node) {
                if (node.getOntology() != Libraries.this.controller.getOntologyAST())
                    return;
                retract(node);
            }
        });
    }
    
    private void watch(final xowl.gmi.model.ast.ASTNode node) {
        xowl.interpreter.adapter.ObjectPropertyObserver observer = new xowl.interpreter.adapter.ObjectPropertyObserver() {
            @Override public void onAdd(xowl.interpreter.adapter.ProxyObject elem) {
                javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable() {
                    @Override public void run() { onSymbolFound(node); }
                });
            }
            @Override public void onRemove(xowl.interpreter.adapter.ProxyObject elem) { }
        };
        astToObserver.put(node, observer);
        node.addPropertyObserver("symbol", observer);
    }
    private void onSymbolFound(xowl.gmi.model.ast.ASTNode node) {
        node.removePropertyObserver(astToObserver.get(node));
        astToObserver.remove(node);
        insert(node);
    }
    
    private void insert(xowl.gmi.model.ast.ASTNode node) {
        String title = symbolToLib.get(node.getSymbol());
        if (title == null)
            return;
        LibraryElement widget = new LibraryElement(node, controller.getStyle());
        astToTBE.put(node, widget);
        astToLib.put(node, title);
        view.addToolboxElement(title, widget);
    }
    private void retract(xowl.gmi.model.ast.ASTNode node) {
        LibraryElement tbe = astToTBE.get(node);
        String title = astToLib.get(node);
        if (tbe == null || title == null)
            return;
        tbe.onRemove();
        view.removeToolboxElement(title, tbe);
        astToTBE.remove(node);
        astToLib.remove(node);
    }
}

