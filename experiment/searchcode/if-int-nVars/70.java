/*
 *  Fingy. Educational programming environment for children.
 *  Copyright (C) 2010.  Zhlobich Andrei <mailto:anjensan@informatics.by>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.informatics.jcx.fingy.ide;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

import by.informatics.jcx.fingy.lang.BoolValue;
import by.informatics.jcx.fingy.lang.FloatValue;
import by.informatics.jcx.fingy.lang.IntValue;
import by.informatics.jcx.fingy.lang.NullValue;
import by.informatics.jcx.fingy.lang.StringValue;
import by.informatics.jcx.fingy.lang.Value;
import by.informatics.jcx.fingy.lang.ValueType;
import by.informatics.jcx.fingy.lang.Variable;
import by.informatics.jcx.fingy.lang.VariableChangeListener;

import javax.swing.table.AbstractTableModel;

public class VariableTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 0L;

    private static final String[] COLUMN_NAME = new String[] { "Variable",
            "Value", "Type" };
    private static final int NAME_COLUMN_ID = 0;
    private static final int VALUE_COLUMN_ID = 1;
    private static final int TYPE_COLUMN_ID = 2;
    private static final ValueType[] FORCED_TYPES = new ValueType[] {
            ValueType.ARRAY, ValueType.INTERNAL, ValueType.OBJECT };

    private List<Variable> variables = new ArrayList<Variable>();
    private final RunServer runServer;
    private Map<Variable, Object> updatedVariables = 
            new IdentityHashMap<Variable, Object>();

    private VariableChangeListener variableListener = 
            new VariableChangeListener() {
        @Override
        public void variableChanged(Variable variable) {
            fireVariableChanged(variable);
        }
    };

    public VariableTableModel(RunServer runServer) {
        this.runServer = runServer;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAME.length;
    }

    @Override
    public int getRowCount() {
        if (isEmpty()) {
            return 0;
        } else {
            return getVariables().size();
        }
    }

    private boolean isEmpty() {
        return getVariables() == null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Variable v = getVariable(rowIndex);
        switch (columnIndex) {
            case NAME_COLUMN_ID:
                return v.getName();
            case VALUE_COLUMN_ID:
                return v.getValue();
            case TYPE_COLUMN_ID:
                return v.getValue().getType().getTypeName();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAME[column];
    }

    @Override
    public java.lang.Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == VALUE_COLUMN_ID;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != VALUE_COLUMN_ID) {
            throw new IllegalArgumentException("Invalid columnt index");
        }
        Variable v = getVariable(rowIndex);
        setVariableValue(v, aValue);
    }

    private void setVariableValue(Variable var, Object aValue) {
        ValueType type = var.getValue().getType();
        if (type != ValueType.ARRAY && type != ValueType.OBJECT) {
            var.setValue(createValueForType(type, aValue.toString()));
        } else {
            System.out.printf("value change ignored\n");
        }
    }

    private Variable getVariable(int row) {
        return variables.get(row);
    }

    private List<Variable> getVariables() {
        return variables;
    }

    private int getRowByVariable(Variable variable) {
        return variables.indexOf(variable);
    }

    public void update() {
        List<Collection<Variable>> vars = runServer.getVariables();
        HashSet<Variable> nVars = joinVarsList(vars);
        // remove old variables
        Iterator<Variable> it = variables.iterator();
        while (it.hasNext()) {
            Variable v = it.next();
            if (!nVars.contains(v)) {
                removeVariable(v);
                it.remove();
            }
        }
        // add new variables
        nVars.removeAll(variables);
        for (Variable v : nVars) {
            variables.add(v);
            addVariable(v);
        }
        updateForcedVariables();
    }

    private void updateForcedVariables() {
        for (Variable v : variables) {
            if (isForcedVariable(v) && !updatedVariables.containsKey(v)) {
                fireVariableChanged(v);
            }
        }
        updatedVariables.clear();
    }

    private void removeVariable(Variable variable) {
        int row = getRowByVariable(variable);
        variable.removeListener(variableListener);
        fireTableRowsDeleted(row, row);
    }

    private void addVariable(Variable variable) {
        variable.addListener(variableListener);
        int row = getRowByVariable(variable);
        fireTableRowsInserted(row, row);
    }

    private HashSet<Variable> joinVarsList(List<Collection<Variable>> variables) {
        HashSet<Variable> result = new HashSet<Variable>();
        if (variables.size() > 0) {
        	result.addAll(variables.get(variables.size() - 1));
        }
        return result;
    }

    private void fireVariableChanged(Variable variable) {
        int row = getRowByVariable(variable);
        updatedVariables.put(variable, null);
        fireTableRowsUpdated(row, row);
    }

    private static final boolean isForcedVariable(Variable variable) {
        ValueType type = variable.getValue().getType();
        for (int i = 0; i < FORCED_TYPES.length; ++i) {
            if (type == FORCED_TYPES[i]) {
                return true;
            }
        }
        return false;
    }

    private static Value createValueForType(ValueType type, String value) {
        Value v = NullValue.NULL;
        switch (type) {
            case BOOL:
                v = BoolValue.create(Boolean.parseBoolean(value));
                break;
            case INT:
                v = IntValue.create(Integer.parseInt(value));
                break;
            case FLOAT:
                v = FloatValue.create(Double.parseDouble(value));
                break;
            case STRING:
                v = StringValue.create(value);
                break;
        }
        return v;
    }
}

