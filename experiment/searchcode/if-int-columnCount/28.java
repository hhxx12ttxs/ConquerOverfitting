package Gui;

import Gui.Primitive.Field;
import Gui.Primitive.PrimitiveEntity;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Danila
 */
public class IndexTableModel extends AbstractTableModel {

    /**
     * Данные таблицы
     */
    public List<Object> data;
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
    private String[] columnName = {"Name", "Attributes"};
    /**
     * Текущая сущность, для которой редактируются индексы
     */
    private PrimitiveEntity curEnt;

    /**
     *
     */
    public IndexTableModel() {
        data = new ArrayList<Object>();
        columnCount = columnName.length;
        rowCount = 0;
    }

    /**
     * Установка новой сущности, для которой будут редактироваться индексы
     *
     * @param entity
     */
    public void setEntity(PrimitiveEntity entity) {
        clear();
        curEnt = entity;
        Iterator<List<Integer>> iter = entity.getCompoundIndexListCopy().iterator();
        List<String> stringList = entity.getIndexNameList();
        int i = 0;
        while (iter.hasNext()) {
            addIndex(iter.next(), stringList.get(i));
            rowCount++;
            i++;
        }
        fireTableDataChanged();
    }

    /**
     * Вспомогательная функция
     *
     * @param list
     */
    private void addIndex(List<Integer> list, String indexName) {
        data.add(indexName);
        data.add(getIndexStr(list));
    }

    /**
     * Вспомогательная функция
     *
     * @param list
     * @return
     */
    private String getIndexStr(List<Integer> list) {
        StringBuilder tmpStr = new StringBuilder();
        for (Integer integer : list) {
            tmpStr.append(curEnt.getFieldWithID(integer).getName());
            tmpStr.append(";");
        }
        return tmpStr.toString();
    }

    /**
     * Добавление новой пустой страки к таблице
     */
    public void addRow() {
        data.add(new String());
        data.add(new String());
        curEnt.addCompoundIndex(new ArrayList<Integer>());
        curEnt.addIndexName(new String());
        curEnt.addUniq(false);
        rowCount++;
        fireTableDataChanged();
    }

    /**
     * Обновление строки в таблице
     *
     * @param numRow Номер строки, которую нужно обновить
     */
    public void updateRow(int numRow) {
        setValueAt(curEnt.getIndexName(numRow), numRow, 0);
        setValueAt(getIndexStr(curEnt.getCompoundIndexList(numRow)), numRow, 1);
    }

    /**
     * Удаление из таблицы строки
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
        curEnt.removeCompoundIndex(numRow);
        rowCount--;
    }

    /**
     * Очистка таблица
     */
    public void clear() {
        data.clear();
        rowCount = 0;
    }

    /**
     *
     * @return
     */
    public String getName(int numRow) {
        return curEnt.getIndexName(numRow);
    }

    /**
     *
     * @param numRow
     * @return
     */
    public List<Integer> getIndexList(int numRow) {
        return curEnt.getCompoundIndexList(numRow);
    }

    /**
     *
     * @return
     */
    public List<Field> getFieldList() {
        return curEnt.getCopyFieldList();
    }

    /**
     *
     * @return
     */
    public PrimitiveEntity getEntity() {
        return this.curEnt;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return columnCount;
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data.remove(rowIndex * columnCount + columnIndex);
        data.add(rowIndex * columnCount + columnIndex, aValue);

        switch (columnIndex) {
            case 0: {
                curEnt.removeIndexName(rowIndex);
                curEnt.addIndexName((String) aValue, rowIndex);
                break;
            }
            case 1: {
                // Не востребовано
                break;
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int colIndex) {
        if (colIndex == 1) {
            return false;
        }
        return true;
    }
}

