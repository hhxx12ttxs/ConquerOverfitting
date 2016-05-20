package org.coode.cardinality.model;

import org.coode.cardinality.prefs.CardiPrefs;
import org.coode.cardinality.prefs.CardinalityProperties;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Nick Drummond<br>
 * nick.drummond@cs.manchester.ac.uk<br>
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Aug 30, 2006<br><br>
 * <p/>
 */
public class CardinalityTableModel extends AbstractTableModel {

    public static final int COL_PROP = 0;
    public static final int COL_MIN = 1;
    public static final int COL_MAX = 2;
    public static final int COL_FILLER = 3;
    public static final int COL_CLOSED = 4;
    public static final int COL_COUNT = 5;

    private static final Class[] COL_CLASSES = {OWLObjectProperty.class,
                                                Integer.class,
                                                Integer.class,
                                                OWLObject.class,
                                                Boolean.class};

    private static String[] colNames;

    private OWLModelManager mngr;

    private CardinalityRowFactory rowFactory;

    private OWLOntologyChangeListener cl = new OWLOntologyChangeListener() {
        public void ontologiesChanged(List<? extends OWLOntologyChange> list) throws OWLException {
            handleOntologiesChanged(list);
        }
    };

    public CardinalityTableModel(OWLModelManager mngr) {
        super();
        this.mngr = mngr;
        rowFactory = new CardinalityRowFactory(mngr);
        rowFactory.setShowInherited(CardiPrefs.getInstance().getBoolean(CardiPrefs.OPT_SHOW_INHERITED_RESTRS, true));
        mngr.addOntologyChangeListener(cl);
    }

    public void setSubject(OWLClass subject) {
        rowFactory.setSubject(subject);
        fireTableDataChanged();
    }

    public OWLClass getSubject() {
        return rowFactory.getSubject();
    }

    public void removeRows(Set<CardinalityRow> rows) {
        List<OWLOntologyChange> changes = rowFactory.removeRestrictions(rows);
        mngr.applyChanges(changes);
    }

    public CardinalityRow getRow(int row) {
        return rowFactory.getRows().get(row);
    }

    public int getRow(CardinalityRow restr) {
        return rowFactory.getRows().indexOf(restr);
    }

    public int getRow(OWLRestriction restr) {
        List<CardinalityRow> rows = rowFactory.getRows();
        for (int i=0; i<rows.size(); i++) {
            if (rows.get(i).contains(restr)) {
                return i;
            }
        }
        return -1;
    }

/////////////////////////////////// TableModel implementation

    public Class getColumnClass(int columnIndex) {
        return COL_CLASSES[columnIndex];
    }

    public String getColumnName(int column) {
        if (colNames == null) {
            Properties props = CardinalityProperties.getInstance();
            colNames = new String[COL_COUNT];
            for (int i = 0; i < COL_COUNT; i++) {
                colNames[i] = props.getProperty("col.name." + i, "unnamed");
            }
        }
        return colNames[column];
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        CardinalityRow restr = rowFactory.getRows().get(rowIndex);
        switch (columnIndex) {
            case COL_MIN:
                int min = restr.getMin();
                value = (min < 0) ? "" : Integer.toString(min);
                break;
            case COL_MAX:
                int max = restr.getMax();
                value = (max < 0) ? "" : Integer.toString(max);
                break;
            case COL_PROP:
                value = restr.getProperty();
                break;
            case COL_FILLER:
                value = restr.getFiller();
                break;
            case COL_CLOSED:
                value = restr.isClosed();
                break;
        }

        return value;
    }

    public int getColumnCount() {
        return COL_COUNT;
    }

    public int getRowCount() {
        return rowFactory.getRows().size();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        boolean result = true;
        if (rowFactory.getRows().get(rowIndex).isReadOnly()) {
            result = false;
        }
        else {
            switch (columnIndex) {
                case COL_MIN: // fallthrough
                case COL_MAX:
                    // cannot edit the min, max of 1 if this is a hasValue restriction
                    if (getRow(rowIndex).getFiller() instanceof OWLIndividual ||
                            getRow(rowIndex).getFiller() instanceof OWLLiteral){
                        result = false;
                    }
                    break;
                case COL_CLOSED:
                    OWLProperty prop = (OWLProperty) getValueAt(rowIndex, CardinalityTableModel.COL_PROP);
                    if (prop instanceof OWLDataProperty ||
                        getRow(rowIndex).getMax() == 0) {
                        result = false;
                    }
                    break;
            }
        }
        return result;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        CardinalityRow restr = rowFactory.getRows().get(rowIndex);
        switch (columnIndex) {
            case COL_MIN:
                try {
                    restr.setMin(Integer.parseInt((String) aValue));
                }
                catch (NumberFormatException e) {
                    restr.setMin(CardinalityRow.NO_VALUE);
                }
                break;
            case COL_MAX:
                try {
                    restr.setMax(Integer.parseInt((String) aValue));
                }
                catch (NumberFormatException e) {
                    restr.setMax(CardinalityRow.NO_VALUE);
                }
                break;
            case COL_PROP:
                restr.setProperty((OWLProperty) aValue);
                break;
            case COL_FILLER:
                restr.setFiller((OWLObject) aValue);
                break;
            case COL_CLOSED:
                restr.setClosed((Boolean) aValue);
                break;
        }

        changes.addAll(restr.getChanges());

        mngr.applyChanges(changes);
    }

    private void handleOntologiesChanged(List<? extends OWLOntologyChange> list) {
        boolean reload = false;
        for (OWLOntologyChange ontologyChange : list){
            OWLClassExpression subclass = null;
            OWLClassExpression superclass = null;
            OWLAxiom axiom = ontologyChange.getAxiom();
            if (axiom instanceof OWLSubClassOfAxiom) {
                subclass = ((OWLSubClassOfAxiom) axiom).getSubClass();
                superclass = ((OWLSubClassOfAxiom) axiom).getSuperClass();
            }

            if (subclass != null) {
                if ((subclass.equals(getSubject())) ||
                    (mngr.getOWLHierarchyManager().getOWLClassHierarchyProvider().getAncestors(getSubject()).contains(subclass))) {
                    if (superclass instanceof OWLClass ||
                        superclass instanceof OWLRestriction ||
                        superclass instanceof OWLObjectComplementOf) {
                        reload = true;
                    }
                }
            }
        }
        if (reload){
            rowFactory.reload();
            fireTableDataChanged();
        }
    }

//////////////////////////////////////////////////////////


    public void dispose() {
        mngr.removeOntologyChangeListener(cl);
    }
}

