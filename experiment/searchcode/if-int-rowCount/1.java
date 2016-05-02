package ru.etu.astamir.model;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import ru.etu.astamir.compression.Border;
import ru.etu.astamir.compression.BorderPart;
import ru.etu.astamir.geom.common.java.Direction;
import ru.etu.astamir.geom.common.java.Orientation;
import ru.etu.astamir.geom.common.java.Point;
import ru.etu.astamir.geom.common.java.Polygon;
import ru.etu.astamir.model.contacts.Contact;
import ru.etu.astamir.model.exceptions.UnexpectedException;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Виртуальная сетка. Отличается от обычной тем, что у нее может изменятся шаг и размер
 * в зависимости от каких-то событий.
 */
public class VirtualGrid implements Drawable, Grid {
    private TopologyLayer layer;

    /**
     * Шаг сетки.
     */
    private double step = 15.0;


    /**
     * Видимость сетки на рисунке. Имеет значение только при отрисовке.
     */
    private boolean gridVisible;

    /**
     * Элементы топологии.
     */
    private List<List<TopologyElement>> elements = Lists.newArrayList();

    /**
     * Максимальное количество элементов в столбце.
     */
    private int maxRowCount = Integer.MAX_VALUE;

    /**
     * Максимальное кол-во колонок.
     */
    private int maxColumnCount = Integer.MAX_VALUE;


    public VirtualGrid(List<List<TopologyElement>> elements, double step, boolean gridVisible) {
        this.step = step;
        this.elements = Lists.newArrayList(elements);
        this.gridVisible = gridVisible;
    }

    public VirtualGrid(int columnCount, double step) {
        this.step = step;
        this.elements = Lists.newArrayListWithCapacity(columnCount);
    }

    public VirtualGrid(int columnCount) {
        this(columnCount, 15.0);
    }           
    

    public VirtualGrid() {
        this(5, 15.0);
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    /**
     * Добавление элемента в колонку.
     *
     * @param element Некоторый элемент топологии
     * @param columnIndex индекс колонки, в конец которой добавляем элемент.
     * @return false, если не получилось добавить элемент.
     */
    public boolean addElementToColumn(TopologyElement element, int columnIndex) {
        if (columnIndex >= columnCount()) {
            ensureColumnCount(columnIndex + 1);
        }

        List<TopologyElement> column = getColumn(columnIndex);
        if (column.size() == maxRowCount) {
            return false;
        }
        element.setCoordinates(columnIndex, column.size());
        return column.add(element);
    }

    public boolean addElementToRow(TopologyElement element, int rowIndex) {
        if (rowIndex >= rowCount()) {
            if (!addEmptyRow()) {
                return false;
            }
        }

        ensureColumnCount(rowIndex + 1);
        return setElementAt(indexOfLastNotEmptyElementInRow(rowIndex), rowIndex, element);
    }

    int indexOfLastNotEmptyElementInRow(int rowIndex) {
        if (rowIndex >= rowCount()) {
            return -1;
        }

        final List<TopologyElement> row = getRow(rowIndex);
        for (int i = row.size() - 1; i >= 0; i--) {
            if (!row.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Удостоверение, что нам хватает колонок. Грубо говоря, добавляем
     * |[новое число колонок] - [текущее число колонок]| пустых колонок.
     * @param newColumnCount Новое число колонок. Столько колонок должно стать
     *                       после работы метода.
     * @see #columnCount()
     */
    private void ensureColumnCount(int newColumnCount) {
        int columnsToAdd = newColumnCount - columnCount();
        for (int i = 0; i < columnsToAdd; i++) {
            addEmptyColumn();
        }
    }

    /**
     * Добавление пустой колонки.
     */
    private void addEmptyColumn() {
        List<TopologyElement> column = Lists.newArrayListWithCapacity(rowCount());
        elements.add(column);
    }

    /**
     * Добавление пустой колонки по указанному индексу.
     * Все колонки начиная с указанного индекса сдвигаются вправо
     * на одну позицию, координаты элементов обновляются.
     *
     * @param index Индекс пустой колонки.
     */
    public void insertEmptyColumn(int index) {
        Preconditions.checkArgument(index >= 0);

        if (index >= columnCount()) {
            ensureColumnCount(index + 1);
            return;
        }
        List<TopologyElement> column = Lists.newArrayListWithCapacity(rowCount());
        elements.add(index, column);
        ensureCoordinates();
    }

    // TODO test
    public void shiftGrid(Direction direction) {
        // either insert empty column or remove first column and add empty to the end;
        switch (direction) {
            case RIGHT: {
                insertEmptyColumn(0);
            } break;
            case LEFT: {
                removeColumn(0);
                addEmptyColumn();
            } break;
            case UP: {
                insertEmptyRow(0);
            } break;
            case DOWN: {
                removeRow(0);
                addEmptyRow();
            } break;

            default: throw new UnexpectedException();
        }


    }

    boolean addEmptyRow() {
        return addRow(Collections.nCopies(columnCount(), EmptyElement.create(layer)));
    }



    boolean insertEmptyRow(int rowIndex) {
        if (rowCount() + 1 < maxRowCount) {
            int columnIndex = 0;
            for (int i = 0; i < elements.size();i++/*List<TopologyElement> column : elements*/) {
                insertElement(columnIndex, rowIndex, EmptyElement.create(layer));
                columnIndex++;
            }

            return true;
        }

        return false;
    }



    /**
     * Добавляет элемент в конец таблицы. Если в последней колонке нет места,
     * создается новая. Добавление элемента ограничивается параметром
     * {@link #maxRowCount}
     *
     * @param element Элемент, который мы хотим добавить.
     * @return true, если удалось добавить элемент.
     */
    public boolean addElement(TopologyElement element) {
        if (elements.isEmpty()) {
            return addElementToColumn(element, 0);
        }

        final int columnCount = columnCount();
        List<TopologyElement> lastColumn = elements.get(columnCount - 1);

        if (lastColumn.size() < maxRowCount) {
            element.setCoordinates(columnCount - 1, lastColumn.size());
            return lastColumn.add(element);
        }

        return addElementToColumn(element, columnCount);
    }

    /**
     * Добавляет элемент в заданную ячейку сетки, если в ней не содержится другого элемента. В колонке
     * элементы до rowIndex заполняются пустыми элементами. Этот метод не стоит вызывать напрямую, нужно
     * пользоваться методом {@link #setElementAt(int, int, TopologyElement)}
     *
     * @param columnIndex Индекс колонки
     * @param rowIndex Индекс строки.
     * @param element Элемент, который нужно добавить.
     * 
     * @return true, если получилось добавить элемент по заданным координатам, иначе false.
     * @see #setElementAt(int, int, TopologyElement)
     *
     */
    private boolean addElementAt(int columnIndex, int rowIndex, TopologyElement element) {
        // we are absolutely sure that we don't have element with this coordinates
        // we have to find working column
        ensureColumnCount(columnIndex + 1); // we have to ensure its existance.
        List<TopologyElement> column = getColumn(columnIndex);
        
        // now we have to fill all empty spots with empty elements

        if (rowIndex >= column.size()) {
            TopologyElement emptyElement = EmptyElement.create(layer);
            for (int i = column.size(); i < rowIndex; i++) {
                emptyElement.setCoordinates(columnIndex, i);
                column.add(emptyElement);
            }

            element.setCoordinates(columnIndex, rowIndex);
            return column.add(element);
        }

        return false;
    }

    /**
     * Добавление колонки в правый конец сетки. Все элементы с индексом
     * больше чем {@link #maxRowCount} не учитываются.
     * @param column список элементов
     */
    public void addColumn(List<? extends TopologyElement> column) {
        int lastIndex = column.size() - 1;
        int lastAllowedIndex = maxRowCount - 1;
        if (lastIndex >= 0) { // we have some elements in the column
            elements.add(Lists.newArrayList(column.subList(0,
                    (lastAllowedIndex > lastIndex ? lastIndex : lastAllowedIndex) + 1))); // we have to trim the column according to maxRowCount
            ensureCoordinates(); // also we have to update elements' coordinates,
            // although we only have to set actual coordinates to the give column.
        } else { // means we got an empty column
            addEmptyColumn(); // so we add an empty column
        }
    }

    /**
     * Добавление строки элементов в сетку. Мы сможем добавить строку только
     * если нам это позволит параметр максимального кол-ва строк {@link #maxRowCount}.
     *
     * @param row Строка элементов.
     * @return true, если получилось добавить строку элементов, false, если
     * у нас уже максимальное кол-во строк.
     */
    public boolean addRow(Collection<? extends TopologyElement> row) {
        int columnCount = columnCount();
        int rowCount = rowCount();
        if (rowCount + 1 <= maxRowCount) { // we actually can add another row.
            if (columnCount < row.size()) {
                ensureColumnCount(row.size());
            } // making sure, we have enough columns to work with

            int index = 0;
            for (TopologyElement element : row) {
                setElementAt(index, rowCount, element); // in case we also have to add empty elements.
                index++;
            }

            return true;
        }

        return false;
    }

    public boolean insertRow(Collection<? extends TopologyElement> row, int rowIndex) {
        if (insertEmptyRow(rowIndex)) {
            int index = 0;
            for (TopologyElement element : row) {
                setElementAt(index, rowIndex, element);
                index++;
            }
        }

        return true;
    }

    public boolean insertColumn(Collection<? extends TopologyElement> column, int index) {
        if (column.size() <= maxRowCount) {
            insertEmptyColumn(index);
            int k = 0;
            for (TopologyElement element : column) {
                setElementAt(index, k, element);
                k++;
            }
        }

        return false;
    }

    public void insertGrid(VirtualGrid grid, int columnIndex, int rowIndex) {
        for (List<TopologyElement> column : grid.getColumns()) {
            int rowI = rowIndex;
            for (TopologyElement element : column) {
                setElementAt(columnIndex, rowI, element);
                rowI++;
            }

            columnIndex++;
        }

        ensureCoordinates();
    }

    /**
     * Задать элемент с конкретными координатами. Если элемент с такими координатами уже есть в сетке,
     * мы просто заменяем его на переданный, иначе мы создаем все условия, чтобы переданный элемент
     * оказался в сетке с требуемыми координатами, а именно добавляем необходимые колонки и заполняем
     * все элементы перед ним пустыми.
     *
     * @param columnIndex индекс колонки.
     * @param rowIndex индекс строки.
     * @param newElement сам элемент.
     * @return true, если получилось добавить элемент с заданными координатами.
     */
    public boolean setElementAt(int columnIndex, int rowIndex, TopologyElement newElement) {
        if (getElement(columnIndex, rowIndex).isPresent()) { // if we already have an element with such coordinates, we replace it.
            newElement.setCoordinates(columnIndex, rowIndex); // setting actual coordinates to the element.
            elements.get(columnIndex).set(rowIndex, newElement);
            return true;
        }

        return addElementAt(columnIndex, rowIndex, newElement); // we were unable to find existing element, so we have to add one
    }

    /**
     * Удаление элемента, или просто замена его пустым. Для полноценного
     * удаления элемента со сдвигом нужно вызвать {@link #removeEmptyElements()}
     * @param columnIndex
     * @param rowIndex
     */    
    @Override
    public void removeElementAt(int columnIndex, int rowIndex) {
        setElementAt(columnIndex, rowIndex, EmptyElement.create(layer));
    }

    /**
     * Удаляем целый ряд, если в колонке нету элементов заданной строки,
     * заполняем пустыми. Этот метод не полностью удаляет элементы, если
     * нужно удалить их полностью следует затем вызвать метод {@link #removeEmptyElements()}
     *
     * @param rowIndex индекс строки, которую нужно удалить.
     */
    public void removeRow(int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, columnCount());
        for (int i = 0; i < columnCount(); i++) {
            removeElementAt(i, rowIndex);
        }
    }

    /**
     * Удаление колонки со здвигом влево, а так же обновление координат элементов.
     * @param columnIndex индекс колонки, которую хотим удалить.
     */
    public void removeColumn(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, columnCount());
        elements.remove(columnIndex);
        ensureCoordinates();
    }

    /**
     * Получение колонки по заданному индексу.
     * @param columnIndex индекс колонки, которую хотим получить.
     * @return колонка по заданному индексу.
     */
    public List<TopologyElement> getColumn(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, elements.size());
        return elements.get(columnIndex);
    }

    /**
     * Получение колонки, как отображения интекса строки в элемент.
     * Все пустые элементы игнорируются.
     *
     * @param columnIndex индекс колонки, которую хотим получить.
     * @return Отображение колонки без пустых элементов.
     */
    public Map<Integer, TopologyElement> columnMap(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, columnCount());
        List<TopologyElement> column = getColumn(columnIndex);
        if (!column.isEmpty()) {
            Map<Integer, TopologyElement> columnMap = Maps.newHashMap();
            for (int i = 0; i < column.size(); i++) {
                TopologyElement element = column.get(i);
                if (!element.isEmpty()) {
                    columnMap.put(i, element);
                }
            }

            return columnMap;
        }
        
        return Maps.newHashMap();
    }

    /**
     * Получение строки, как отображения интекса столбца в элемент. Все
     * пустые элементы игнорируются.
     *
     * @param rowIndex индекс строки, которую хотим получить.
     * @return Отображение строки без пустых элементов.
     */
    public Map<Integer, TopologyElement> rowMap(int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, rowCount());
        List<TopologyElement> row = getRow(rowIndex);
        if (!row.isEmpty()) {
            Map<Integer, TopologyElement> rowMap = Maps.newHashMap();
            for (int i = 0; i < row.size(); i++) {
                TopologyElement element = row.get(i);
                if (!element.isEmpty()) {
                    rowMap.put(i, element);
                }
            }

            return rowMap;
        }

        return Maps.newHashMap();
    }

    /**
     * Получение сетки как отображения координат в элементы, чтобы
     * для получения элемента по координатам не приходилось каждый раз
     * пробегать весь список элементов.
     *
     * @return Отображение координат в элементы.
     */
    public Map<Point, TopologyElement> getElementPointMap() {
        ensureCoordinates();
        Map<Point, TopologyElement> elementMap = Maps.newHashMap();
        for (TopologyElement element : getAllElements()) {
            if (!element.isEmpty()) {
                elementMap.put(element.getCoordinates(), element);
            }
        }

        return elementMap;
    }

    /**
     * Получение сетки как отображения индекса колонок в отображение индексов строк.
     * Все пустые элементы игнорируются.
     *
     * @return Отображение координат в элементы.
     */
    public Map<Integer, Map<Integer, TopologyElement>> getElementMap() {
        ensureCoordinates();
        Map<Integer, Map<Integer, TopologyElement>> map = Maps.newHashMap();
        for (int i = 0; i < columnCount(); i++) {
            List<TopologyElement> column = elements.get(i);
            if (!column.isEmpty()) {
                Map<Integer, TopologyElement> columnMap = columnMap(i);
                map.put(i, columnMap);
            }
        }

        return map;
    }


    /**
     * Получение строки по индексу.
     *
     * @param rowIndex индекс строки, которую мы хотим получить.
     * @return копия строки с заданным идексом.
     */
    public List<TopologyElement> getRow(int rowIndex) {
        Preconditions.checkElementIndex(rowIndex, rowCount());
        List<TopologyElement> row = Lists.newArrayListWithCapacity(columnCount());
        for (List<TopologyElement> column : elements) {
            if (rowIndex < column.size()) {
                row.add(column.get(rowIndex));
            } else {
                row.add(EmptyElement.create(layer));
            }
        }

        return row;
    }

    // TODO test
    public boolean insertElement(int columnIndex, int rowIndex, TopologyElement element) {
        ensureColumnCount(columnIndex);
        List<TopologyElement> column = getColumn(columnIndex);
        if (column.size() + 1 <= maxRowCount) {
            column.add(rowIndex, element);
            ensureCoordinates();
            removeEmptyColumns();

            return true;
        }

        removeEmptyColumns(); // we still might have some unwanted empty columns from ensureColumnCount
        return false;
    }


    @Override
    public List<List<TopologyElement>> getColumns() {
        return Lists.newArrayList(elements);
    }

    @Override
    public List<List<TopologyElement>> getRows() {
        List<List<TopologyElement>> rows = Lists.newArrayList();
        final int rowCount = rowCount();
        for (int i = 0; i < rowCount; i++) {
            rows.add(getRow(i));
        }

        return rows;
    }

    @Override
    public List<List<TopologyElement>> walk(Direction direction) {
        switch (direction) {
            case LEFT: return getColumns();
            case RIGHT: return Lists.reverse(getColumns());
            case DOWN: return getRows();
            case UP: return Lists.reverse(getRows());
            default: throw new UnexpectedException();
        }
    }

    /**
     * Пытается найти элемент с заданными координатами.
     *
     * @param columnIndex Координата columnIndex элемента
     * @param rowIndex Координата rowIndex элемента
     * @return Элемент с координатами (columnIndex, rowIndex) или null, если на этом месте ничего нету.
     */
    @Override
    public Optional<TopologyElement> getElement(final int columnIndex, final int rowIndex) {
        if (columnIndex >= columnCount()) {
            return Optional.absent();
        }
        
        List<TopologyElement> column = elements.get(columnIndex);
        
        if (rowIndex < column.size()) {
            return Optional.of(column.get(rowIndex));
        } // trying to get the element by coordinates.

        return Optional.absent();
    }

    /**
     * Получение всех элементов сетки, как единого списка. Итератор будет построен
     * на основе колонок, то есть сначала будут пройдены все элементы первой колонки потом второй и т.д.
     *
     * @return список всех элементов сетки.
     */
    public List<TopologyElement> getAllElements() {
        return Lists.newArrayList(Iterables.filter(Iterables.concat(elements), new Predicate<TopologyElement>() {
            @Override
            public boolean apply(TopologyElement input) {
                return !input.isEmpty();
            }
        }));
    }

    /**
     * Устанавливает всем элементам координаты в соответствии
     * с их положением в сетке.
     */
    public void ensureCoordinates() {
        for (int i = 0; i < columnCount(); i++) {
            for (int j = 0; j < elements.get(i).size(); j++) {
                TopologyElement element = elements.get(i).get(j);
                element.setCoordinates(i, j);
            }
        }
    }

    /**
     * Удаляет пустые элементы из сетки, а так же обновляет координаты оставшихся.
     * Если в ходе удаления в какой-то колонке заканчиваются элементы, то колонка удаляется.
     */
    public void removeEmptyElements() {
        for (Iterator<List<TopologyElement>> columnIterator = elements.iterator(); columnIterator.hasNext();) {
            List<TopologyElement> column = columnIterator.next();
            for (Iterator<TopologyElement> i = column.iterator(); i.hasNext();) {
                TopologyElement element = i.next();
                if (element.isEmpty()) {
                    i.remove();
                }
            }

            if (column.isEmpty()) {
                columnIterator.remove();
            }
        }

        ensureCoordinates();
    }

    /**
     * Удаляет полностью пустые колонки,
     * то есть колонки состоящие полностью из пустых элементов.
     */
    public void removeEmptyColumns() {
        for (Iterator<List<TopologyElement>> it = elements.iterator(); it.hasNext();) {
            final List<TopologyElement> column = it.next();
            if (Iterables.all(column, new Predicate<TopologyElement>() {
                @Override
                public boolean apply(TopologyElement input) {
                    return input.isEmpty();
                }
            })) {
                it.remove();
            }
        }
    }

    /**
     * Удаляет все пустые элементы с конца столбца. Как только встречается
     * непустой элемент, процесс останавливается.
     * @param columnIndex индекс колонки.
     */
    private void removeLastEmptyElements(int columnIndex) {
        Preconditions.checkElementIndex(columnIndex, columnCount());
        List<TopologyElement> column = elements.get(columnIndex);
        for (ListIterator<TopologyElement> i = column.listIterator(column.size() - 1);i.hasPrevious();) {
            TopologyElement elem = i.previous();
            if (elem.isEmpty()) {
                i.remove();
            } else {
                break;
            }
        }
    }
    
    public void removeLastEmptyElements() {
        for (int columnIndex = 0; columnIndex < columnCount(); columnIndex++) {
            removeLastEmptyElements(columnIndex);
        }
    }

    /**
     * Присваивает элементам виртальные координаты на основе их
     * реальных координат.
     */
    // TODO
    public void reorderByNaturalCoordinates() {
        throw new UnexpectedException("not implemented yet");
    }

    /**
     * Перестраивает элементы в сетке в соответствии с их координатами.
     */
    // TODO
    public void reorderByElementsCoordinates() {
        throw new UnexpectedException("not implemented yet");
    }
    
    // TODO look through
    public Border getBorder(final Direction direction, int index, BorderPart... additionalParts) {
        index = direction.isUpOrRight() ? index : index + 1;
        Border border = new Border(direction.getOrthogonalDirection().toOrientation());
        ListIterator<List<TopologyElement>> target = direction.isLeftOrRight() ? getColumns().listIterator(index) : getRows().listIterator(index);
        Predicate<ListIterator<List<TopologyElement>>> counter = new Predicate<ListIterator<List<TopologyElement>>>() {
            @Override
            public boolean apply(ListIterator<List<TopologyElement>> input) {
                return direction.isUpOrRight() ? input.hasNext() : input.hasPrevious();
            }
        };
        Function<ListIterator<List<TopologyElement>>, List<TopologyElement>> next = new Function<ListIterator<List<TopologyElement>>, List<TopologyElement>>() {
            @Override
            public List<TopologyElement> apply(ListIterator<List<TopologyElement>> input) {
                return direction.isUpOrRight() ? input.next() : input.previous();
            }
        };

        for (;counter.apply(target);) {
            List<TopologyElement> column = next.apply(target);
            List<BorderPart> borderParts = Lists.newArrayList();
            for (TopologyElement element : column) {
                borderParts.addAll(BorderPart.of(element));
            }
            border.overlay(borderParts, direction);
        }
        
        border.overlay(Lists.newArrayList(additionalParts), direction);

        return border;
    }

    // TODO look through
    public Border getBorder(Direction direction) {
        int columnCount = columnCount();
        int rowCount = rowCount();

        if (direction.isLeftOrRight() && columnCount == 0) {
            return Border.emptyBorder(Orientation.VERTICAL);
        }

        if (direction.isUpOrDown() && rowCount == 0) {
            return Border.emptyBorder(Orientation.HORIZONTAL);
        }

        return this.getBorder(direction, direction.isUpOrRight() ? 0 :
                (direction.isLeftOrRight() ? columnCount - 1 : rowCount - 1));
    }

    // TODO look through
    public Border getBorderWithOffset(Direction direction, int dec) {
        int columnCount = columnCount();
        int rowCount = rowCount();

        if (direction.isLeftOrRight() && columnCount == 0) {
            return Border.emptyBorder(Orientation.VERTICAL);
        }

        if (direction.isUpOrDown() && rowCount == 0) {
            return Border.emptyBorder(Orientation.HORIZONTAL);
        }

        return this.getBorder(direction, direction.isUpOrRight() ? dec :
                (direction.isLeftOrRight() ? columnCount - dec - 1 : rowCount - dec - 1));
    }



    /**
     * Получить число колонок.
     * 
     * @return число колонок сетки.
     */
    public int columnCount() {
        return elements.size();
    }

    /**
     * Кол-во строк, число элементов в самой большой колонке.
     * Функция ищет максимальную колоноку, поэтому не стоит вызывать
     * слишком часто.
     * @return Максимальное кол-во строк.
     */
    public int rowCount() {
        return elements.isEmpty() ? 0 : Collections.max(elements, new Comparator<List<TopologyElement>>() {
            @Override
            public int compare(List<TopologyElement> o1, List<TopologyElement> o2) {
                return Ints.compare(o1.size(), o2.size());
            }
        }).size();
    }


    public boolean isGridVisible() {
        return gridVisible;
    }

    public void setGridVisible(boolean gridVisible) {
        this.gridVisible = gridVisible;
    }

    public void setMaxRowCount(int maxRowCount) {
        this.maxRowCount = maxRowCount;
        // remove all above rows ?
    }

    @Override
    public void draw(Graphics2D g) {
        if (gridVisible) {
            // отобразить сетку
        }

        //ensureCoordinates();
        for (TopologyElement element : getAllElements()) {
            element.draw(g);
            Polygon bounds = element.getBounds();
            if (bounds != null) {
                if (bounds.vertices().size() > 0) {
                    Point center = bounds.getCenter();
                   // g.drawString(element.getCoordinates().toString(), center.intX(), center.intY());
                }
            }
        }

    }
    
    @Override
    public String toString() {
        removeLastEmptyElements();
        StringBuilder builder = new StringBuilder();
        for (List<TopologyElement> column : Lists.reverse(getRows())) {
            builder.append("[").append(Joiner.on("],[").join(Lists.transform(column, new Function<TopologyElement, String>() {
                @Override
                public String apply(TopologyElement input) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(input.getClass().getSimpleName()).append(input.getCoordinates());
                    return builder.toString();
                }
            }))).append("]\n");
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        VirtualGrid grid = new VirtualGrid();
        Contact contact = new Contact(Point.of(0,0), Material.METALL);
        grid.setElementAt(0, 0, contact);
        grid.setElementAt(1, 0, contact);
        grid.setElementAt(0, 1, contact);
        
        VirtualGrid g = new VirtualGrid();
        Bus bus = new Bus(null, Point.of(0, 0), Material.METALL, 0);
        g.setMaxRowCount(3);
        for (int i = 0; i < 9; i++) {
            g.addElement(bus);            
        }
        

        System.out.println(grid);
        System.out.println("================");
        System.out.println(g);
        System.out.println("================");
        grid.insertGrid(g, 1,1);
        System.out.println(grid);

        /*for (int i = 0; i < 50; i++) {
            grid.addElementToColumn(new Random().nextBoolean() ? EmptyElement.create(null) : new Contact(null,
                    new Point(0, 0), 0, null, null, null), new Random().nextInt(10));
        }

        for (int i = 0; i < grid.rowCount(); i++) {
            grid.removeRow(i);
        }*/


    }
}

