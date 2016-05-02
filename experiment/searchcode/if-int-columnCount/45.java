package Gui;

import Gui.Primitive.Field;
import Gui.ChangeHistory.History;
import Gui.ChangeHistory.HistoryAction;
import Gui.ChangeHistory.HistoryElementChanged;
import Gui.ChangeHistory.HistoryIteration;
import Gui.Primitive.PrimitiveEntity;
import Gui.Primitive.PrimitiveLine;
import Gui.Utils.MySQLType;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Таблица атрибутов
 *
 * @author Danila
 */
public class AttributeTableModel extends AbstractTableModel {

    /**
     * Текущая редактируемая сщуность
     */
    private PrimitiveEntity curEnt;
    /**
     * Дата таблицы
     */
    private List<Object> data;
    /**
     * Список уникаьлных идентификаторов атрибутов
     */
    private List<Integer> dataID;
    /**
     * Количество строк
     */
    private int rowCount;
    /**
     * Количество колонок
     */
    private int columnCount;
    /**
     * Имена колонок
     */
    private String[] columnName = {"PK", "Name", "Type", "Length"};
    /**
     * Родительский виджет, в котором происходит отрисовка элементов диаграммы
     */
    private DrawJPanel parent;

    /**
     * Инициализация новой таблицы атрибутов
     */
    public AttributeTableModel(DrawJPanel parent) {
        this.parent = parent;
        rowCount = 0;
        columnCount = columnName.length;
        data = new ArrayList<Object>();
        dataID = new ArrayList<Integer>();
    }

    /**
     *
     */
    public void checkField() {
        if (this.curEnt != null) {
            List<Field> fieldList = this.curEnt.getCopyFieldList();
            int size = fieldList.size();
            // Изменилась размерность
            if (size != rowCount) {
                setEntity(curEnt);
            }
        }
    }

    /**
     *
     * @param parent
     */
    public void setParent(DrawJPanel parent) {
        this.parent = parent;
    }

    /**
     * Установка текущей редактируемой сущности
     *
     * @param curEnt Редактируемая сущность
     */
    public void setEntity(PrimitiveEntity curEnt) {
        clear();
        this.curEnt = curEnt;
        data.clear();
        dataID.clear();

        List<Field> fieldList = curEnt.getCopyFieldList();
        for (Field field : fieldList) {
            addField(field);
        }
    }

    /**
     * Добавление атрибута в таблица
     *
     * @param field
     */
    private void addField(Field field) {
        dataID.add(field.getID());
        data.add(field.isPrimaryKey());
        data.add(field.getName());
        data.add(field.getType());
        data.add(field.getLength1());
        rowCount++;

    }

    /**
     *
     * @param numRow
     */
    public Field getField(int numRow) {
        return curEnt.getField(numRow);
    }

    /**
     * Добавление строки
     */
    public void addRow() {
        Field field = new Field(" ", false);
        addField(field);
        curEnt.addField(field);
        parent.repaint();
        History.historyListBack.add(new HistoryIteration(HistoryAction.Edit));
        History.historyListBack.get(History.historyListBack.size()-1).addChange(HistoryElementChanged.AtrAdd, field, curEnt.getID());
        History.historyListFwd.clear();
    }

    /**
     * Удаление строки с заданным номером
     *
     * @param numRow Номер строки, которую нужно удалить
     */
    public void removeRow(int numRow) {
        int start = numRow * columnCount;
        int end = numRow * columnCount + columnCount;
        for (int i = start; i < end; i++) {
            // Удаляем одну и туже позицию, т.к. происходит сдвиг
            data.remove(start);
        }
        History.historyListBack.add(new HistoryIteration(HistoryAction.Edit));
        History.historyListBack.get(History.historyListBack.size()-1).addChange(HistoryElementChanged.AtrRemove, curEnt.getField(numRow), curEnt.getID());
        History.historyListFwd.clear();
        dataID.remove(numRow);
        curEnt.removeField(numRow);
        rowCount--;
    }

    /**
     * Очистка таблицы
     */
    public void clear() {
        data.clear();
        dataID.clear();
        rowCount = 0;
    }

    /**
     * Обновление строки
     *
     * @param numRow Номер строки, в которой нужно обновить отображемые данные
     */
    public void updateRow(int numRow) {
        List<Field> fieldList = curEnt.getCopyFieldList();
        Field field = fieldList.get(numRow);
        setValueAt(field.isPrimaryKey(), numRow, 0);
        setValueAt(field.getName(), numRow, 1);
        setValueAt(field.getType(), numRow, 2);
        setValueAt(field.getLength1(), numRow, 3);
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnName.length;
    }

    @Override
    public String getColumnName(int i) {
        return columnName[i];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex * columnCount + columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int colIndex) {
        Field field = curEnt.getCopyFieldList().get(rowIndex);
        if (field.isForeignKey()) {
            if (colIndex == 2 || colIndex == 3) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(rowIndex * columnCount + columnIndex < data.size() ){
            data.remove(rowIndex * columnCount + columnIndex);
            data.add(rowIndex * columnCount + columnIndex, aValue);            
            List<Field> fieldList = curEnt.getCopyFieldList();
            Field curField = fieldList.get(rowIndex);            
            Object o = this.getValueAt(rowIndex, columnIndex);
            boolean isChanged=false;
            switch(columnIndex){
                case 0:
                    if(curField.isPrimaryKey()!=(Boolean)o)
                        isChanged=true;
                    break;
                case 1:
                    if(!curField.getName().equals((String)o))
                        isChanged=true;
                    break;
                case 2:
                    if(!curField.getType().equals((String)o))
                        isChanged=true;
                    break;
                case 3:
                    if(curField.getLength1()!=(Integer)o)
                        isChanged=true;
                    break;
            }
            if(isChanged){
                if(!History.isProcess){
                    History.historyListFwd.clear();
                    History.historyListBack.add(new HistoryIteration(HistoryAction.Edit));
                    History.historyListBack.get(History.historyListBack.size()-1).addChange(HistoryElementChanged.AtrChange, curField.clone(), curEnt.getID());
                }

                switch (columnIndex) {
                    case 0: {
                        Boolean bool = (Boolean) o;
                        curField.setPrimaryKey(bool);
                        break;
                    }
                    case 1: {
                        String str = (String) o;
                        curField.setName(str);
                        break;
                    }
                    case 2: {
                        String str = (String) o;
                        curField.setType(str);
                        break;
                    }
                    case 3: {
                        int num = (Integer) o;
                        curField.setLength1(num);
                        break;
                    }
                }
            }
            parent.repaint();
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
