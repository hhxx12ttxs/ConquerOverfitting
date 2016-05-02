
package FI.realitymodeler.sql;

import FI.realitymodeler.common.*;
import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import jdbm.*;
import jdbm.helper.*;
import jdbm.recman.*;

/** This is a rudimentary sql server which does not support views.<br>
    Following statements are available:<br>
    CREATE [UNIQUE] INDEX <i>index-name</i> ON <i>table-name</i><br> {<i>column-name</i>}, ...)<br>
    <br>
    CREATE TABLE <i>table-name</i><br> {<i>column-name</i> <i>data-type</i>}, ...)<br>
    <br>
    <i>data-type</i> is one of the following:<br>
    CHARACTER(<i>max-length</i>)<br>
    VARCHARACTER(<i>max-length</i>)<br>
    LONGVARCHAR[(<i>max-length</i>[, <i>max-path-length</i>])]<br>
    NUMERIC(<i>precision</i>[, <i>scale</i>])<br>
    DECIMAL(<i>precision</i>[, <i>scale</i>])<br>
    BIT<br>
    TINYINT<br>
    SMALLINT<br>
    INTEGER<br>
    BIGINT<br>
    REAL<br>
    FLOAT<br>
    DOUBLE<br>
    BINARY<br>
    VARBINARY<br>
    LONGVARBINARY[(<i>max-length</i>[, <i>max-path-length</i>])]<br>
    DATE<br>
    TIME<br>
    TIMESTAMP<br>
    <br>
    DELETE FROM <i>table-name</i><br>
    [WHERE <i>search-condition</i>]<br>
    <br>
    INSERT INTO <i>table-name</i> {<i>column-name</i>}, ...)]<br> {VALUES {<i>literal</i> | ?}, ...) | <i>select-statement</i>}<br>
    <br>
    SELECT [ALL | DISTINCT] <i>select-list</i><br>
    FROM {<i>table-name</i> [<i>correlation-name</i>], ...<br>
    [WHERE <i>search-condition</i>]<br>
    [GROUP BY {<i>column-name</i> | <i>column-number</i>}, ...]<br>
    [HAVING <i>search-condition</i>]<br> {INTERSECT | MINUS | UNION [ALL]} <i>select-statement</i>]<br>
    [[ORDER BY {{<i>column-name</i> | <i>column-number</i>} [ASC | DESC]}, ...] |<br>
    [FOR UPDATE OF {<i>column-name</i>}, ...]]<br>
    <br>
    <i>select-list</i> is:<br> { {<i>expression</i> | <i>table-name</i>.* | <i>correlation-name</i>.*}, ...}<br>
    <br>
    UPDATE <i>table-name</i><br>
    SET {<i>column-name</i> = <i>expression</i>}, ...<br>
    [WHERE <i>search-condition</i>]<br>
    <br>
    <i>search-condition</i> can be constructed from following expressions:<br>
    [NOT] EXISTS <i>select-statement</i><br>
    [NOT] (<i>column-name</i> | <i>literal-string</i>) LIKE <i>literal-string</i><br>
    [NOT] <i>expression</i> BETWEEN <i>expression</i> AND <i>expression</i><br>
    [NOT] <i>expression</i> IN <i>select-statement</i><br>
    [NOT] <i>expression</i> IS [NOT] NULL */
public class Database {
    static final int MAX_LENGTH = 0;
    static final int OFFSET = 0;
    static final int LENGTH = 1;
    static final int PRECISION = 1;
    static final int SCALE = 2;
    static final int TYPE = 3;
    static final int COLUMN_INDEX = 4;
    static final int pathLength = 256;
    static final String delims = "\t ";
    static final String whites = "\t\n\r ";
    static final String compChars = "=<>,";
    static final String operChars = "+-*/";
    static final String realChars = ".DEFdef";
    static final String duplKeys = "Duplicate keys";
    static final Character ADD = new Character('+'), SUB = new Character('-'), MUL = new Character('*'), DIV = new Character('/');
    static final Integer COLUMN = new Integer(0), REFERENCE = new Integer(1), PARAMETER = new Integer(2);
    static final Integer SELECT = new Integer(-1), INSERT = new Integer(0), DELETE = new Integer(1),
        CREATE = new Integer(2), UNIQUE = new Integer(3), INDEX = new Integer(4), TABLE = new Integer(5);
    static final Integer UPDATE = new Integer(0), DISTINCT = new Integer(1), WHERE = new Integer(2),
        INTERSECT = new Integer(3), MINUS = new Integer(4), UNION = new Integer(5);
    static final Integer AVG = new Integer(0), COUNT = new Integer(1),
        MAX = new Integer(2), MIN = new Integer(3), SUM = new Integer(4);
    static final Integer ALL = new Integer(-1), NOT = new Integer(0),
        AND = new Integer(1), OR = new Integer(2), BETWEEN = new Integer(3),
        EQUAL = new Integer(4), UNEQUAL = new Integer(5),
        GREATER = new Integer(6), LESS = new Integer(7),
        GREATER_OR_EQUAL = new Integer(8), LESS_OR_EQUAL = new Integer(9),
        EXISTS = new Integer(10), IN = new Integer(11), LIKE = new Integer(12),
        IS = new Integer(13), NULL = new Integer(14), SOME = new Integer(15),
        ANY = new Integer(16);

    static Map<String, Database> instances = new HashMap<String, Database>();

    private DataFile blockFile;
    private File dir;
    private Map<String, Table> tables = new HashMap<String, Table>();
    private RecordManager recordManager;
    private RWControl rwControl = new RWControl();
    private String name;
    private int blockSize = 64;

    class ColumnDef {
        int offset;
        int length;
        int scale;
        int type;
        int columnIndex;

        public String toString() {
            return "{ColumnDef: offset=" + offset + ",length=" + length + ",scale=" + scale + ",type=" + type + "}";
        }

    }

    class Column {
        IndexFile indexFile;
        String columnName;
        ColumnDef columnDef;

        public String toString() {
            return "{Column: indexFile=" + indexFile + ",columnName=" + columnName + "}";
        }

    }

    class Table {
        DataFile dataFile;
        Map<String, Column> columns;
        Map<String, Index> indices;
        Index index;
        String tableName;
        Vector<Column> columnVector;
        boolean needsBlockFile = false;

        public String toString() {
            return "{Table: dataFile=" + dataFile + ",columns=" + columns + ",indices=" + indices + ",index=" + index + ",tableName=" + tableName + ",columnVector=" + columnVector + ",needsBlockFile=" + needsBlockFile + "}";
        }

    }

    class Index {
        IndexFile indexFile;
        Map<Column, ColumnDef> columnTable;
        Vector<ColumnDef> columnDefs;

        public String toString() {
            return "{Index: indexFile=" + indexFile + ",columnTable=" + columnTable + ",columnDefs=" + columnDefs + "}";
        }

    }

    class From {
        Table table;
        IndexFile indexFile;
        boolean descending = false;

        public String toString() {
            return "{From: table=" + table + ",indexFile=" + indexFile + ",descending=" + descending + "}";
        }

    }

    /** Returns old instance of database if it exists or new one if not. All
        operations with database will now be properly synchronized. */
    public static synchronized Database instance(String name)
        throws IOException {
        Database database = instances.get(name);
        if (database == null) instances.put(name, database = new Database(name));
        return database;
    }

    @SuppressWarnings("fallthrough")
    public Database(String name)
        throws IllegalArgumentException, IOException {
        dir = new File(this.name = name);
        if (!dir.exists() && !dir.mkdir()) throw new IOException();
        File recordManagerFile = new File(dir, "index");
        recordManager = RecordManagerFactory.createRecordManager(recordManagerFile.getPath());
        File file = new File(dir, "tables.info");
        if (!file.exists()) return;
        DataInputStream tablesInfoIn = new DataInputStream(new FileInputStream(file));
        for (int numberOfTables = tablesInfoIn.readInt(); numberOfTables > 0; numberOfTables--) {
            String tableName = tablesInfoIn.readUTF();
            Map<String, Column> columns = new HashMap<String, Column>();
            Map<String, Index> indices = new HashMap<String, Index>();
            Vector<Column> columnVector = new Vector<Column>();
            DataInputStream tableInfoIn = new DataInputStream(new FileInputStream(new File(dir, tableName + ".info")));
            int numberOfColumns = tableInfoIn.readInt(), recordLength = 0;
            for (int columnIndex = 0; columnIndex < numberOfColumns; columnIndex++) {
                Column column = new Column();
                String columnName = tableInfoIn.readUTF();
                ColumnDef columnDef = new ColumnDef();
                columnDef.offset = tableInfoIn.readInt();
                columnDef.length = tableInfoIn.readInt();
                columnDef.scale = tableInfoIn.readInt();
                columnDef.type = tableInfoIn.readInt();
                columnDef.columnIndex = columnIndex;
                column.columnName = columnName;
                column.columnDef = columnDef;
                columns.put(columnName, column);
                columnVector.addElement(column);
                recordLength += fieldLength(columnDef, true);
            }
            tableInfoIn.close();
            Table table = new Table();
            file = new File(dir, tableName + ".data");
            table.dataFile = new DataFile(file, recordLength);
            table.tableName = tableName;
            table.columns = columns;
            table.indices = indices;
            table.columnVector = columnVector;
            tables.put(tableName, table);
            file = new File(dir, tableName + "_indices.info");
            if (!file.exists()) continue;
            Index index = null;
            DataInputStream indicesInfoIn = new DataInputStream(new FileInputStream(file));
            int numberOfIndices = indicesInfoIn.readInt();
            IndexFile indexFiles[] = new IndexFile[numberOfIndices];
            for (int i = 0; i < numberOfIndices; i++) {
                String indexName = indicesInfoIn.readUTF();
                DataInputStream indexInfoIn = new DataInputStream(new FileInputStream(new File(dir, tableName + "_" + indexName + "_index.info")));
                int pageRef = indexInfoIn.readInt();
                boolean unique = indexInfoIn.readBoolean();
                int length = 0;
                String firstName = null;
                Vector<ColumnDef> columnDefs = new Vector<ColumnDef>();
                Map<Column, ColumnDef> columnTable = new HashMap<Column, ColumnDef>();
                int numberOfKeyFields = indexInfoIn.readInt();
                DataItem dataItems[] = new DataItem[numberOfKeyFields];
                boolean hasLength = false, needsBlockFile = false;
                for (int keyFieldIndex = 0; keyFieldIndex < numberOfKeyFields; keyFieldIndex++) {
                    String columnName = indexInfoIn.readUTF();
                    if (firstName == null) firstName = columnName;
                    Column column = columns.get(columnName);
                    ColumnDef columnDef = column.columnDef;
                    columnDefs.addElement(columnDef);
                    columnTable.put(column, columnDef);
                    dataItems[keyFieldIndex] = new DataItem(columnDef.offset, fieldLength(columnDef, false));
                    switch (columnDef.type) {
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        if (columnDef.type == Types.VARCHAR ||
                            File.separatorChar == '\\') dataItems[keyFieldIndex].caseType = DataItem.CHAR;
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        if (keyFieldIndex < numberOfKeyFields - 1) throw new IllegalArgumentException("Variable length key field is not the last");
                        needsBlockFile = true;
                    case Types.CHAR:
                    case Types.BINARY:
                        if (columnDef.type == Types.CHAR) dataItems[keyFieldIndex].caseType = DataItem.CHAR;
                        if (keyFieldIndex == numberOfKeyFields - 1) hasLength = true;
                        else dataItems[keyFieldIndex].offset += 4;
                    }
                }
                indexInfoIn.close();
                index = new Index();
                if (needsBlockFile && blockFile == null) {
                    file = new File(dir, "data.block");
                    blockFile = new DataFile(file, blockSize);
                }
                columns.get(firstName).indexFile = index.indexFile = indexFiles[i] =
                    new IndexFile(tableName + "_" + indexName + ".index", recordManager, dataItems, !unique, hasLength, table.dataFile, needsBlockFile ? blockFile : null);
                index.columnDefs = columnDefs;
                index.columnTable = columnTable;
                indices.put(indexName, index);
                if (table.index == null) table.index = index;
                if (needsBlockFile) table.needsBlockFile = true;
            }
            indicesInfoIn.close();
            for (int i = 0; i < numberOfIndices; i++) {
                IndexContext xcontext = indexFiles[i].openIndex(true, table.needsBlockFile ? blockFile : null);
                try {
                    indexFiles[i].recover(xcontext);
                } finally {
                    indexFiles[i].close(xcontext);
                }
            }
        }
        tablesInfoIn.close();
    }

    public File getDir() {
        return dir;
    }

    public String getName() {
        return name;
    }

    public boolean created() {
        return !tables.isEmpty();
    }

    public int tableLength(String tableName) {
        return tables.get(tableName).dataFile.fileLen();
    }

    public int fieldLength(ColumnDef columnDef, boolean prefix)
        throws IllegalArgumentException {
        switch (columnDef.type) {
        case Types.BIT:
        case Types.TINYINT:
            return 1;
        case Types.SMALLINT:
            return 2;
        case Types.INTEGER:
        case Types.REAL:
        case Types.FLOAT:
        case Types.REF:
            return 4;
        case Types.BIGINT:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.DATE:
        case Types.TIME:
            return 8;
        case Types.TIMESTAMP:
            return 12;
        case Types.CHAR:
            return prefix ? 4 + columnDef.length * 2 : columnDef.length * 2;
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.LONGVARBINARY:
            return columnDef.length == 0 ? 0 : (prefix ? 4 + columnDef.length * 2 : columnDef.length * 2) + 4;
        case Types.BINARY:
            return prefix ? 4 + columnDef.length : columnDef.length;
        case Types.VARBINARY:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.BLOB:
        case Types.CLOB:
            return (prefix ? 4 + columnDef.length : columnDef.length) + 4;
        }
        throw new IllegalArgumentException("Unknown column type");
    }

    public void close()
        throws IOException {
        recordManager.close();
    }

    public int findColumn(String columnName)
        throws IllegalArgumentException {
        int i;
        if ((i = columnName.indexOf('.')) != -1) {
            String tableName = columnName.substring(0, i).trim();
            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("No table name " + tableName + " found");
            columnName = columnName.substring(i + 1).trim();
            Column column;
            if ((column = table.columns.get(columnName)) != null) return column.columnDef.columnIndex;
            throw new IllegalArgumentException("No column name " + columnName + " found");
        }
        columnName = columnName.trim();
        Iterator<Table> tableIter = tables.values().iterator();
        while (tableIter.hasNext()) {
            Table table = tableIter.next();
            Column column;
            if ((column = table.columns.get(columnName)) != null) return column.columnDef.columnIndex;
        }
        throw new IllegalArgumentException("No column name " + columnName + " found");
    }

    public void setColumnElement(Object columnElement[], String element, Map<String, Table> selectTables)
        throws IllegalArgumentException {
        columnElement[0] = COLUMN;
        if (selectTables == null) {
            columnElement[1] = element;
            return;
        }
        int i;
        if ((i = element.indexOf('.')) != -1) {
            Table table = selectTables.get(element.substring(0, i));
            columnElement[1] = table.columns.get(element.substring(i + 1));
            columnElement[2] = table;
            return;
        }
        Iterator<Table> tableIter = selectTables.values().iterator();
        while (tableIter.hasNext()) {
            Table table = tableIter.next();
            Column column;
            if ((column = table.columns.get(element)) != null) {
                columnElement[1] = column;
                columnElement[2] = table;
                return;
            }
        }
        throw new IllegalArgumentException("No element " + element + " found");
    }

    public void setReferenceElement(Object referenceElement[], String element, Map<String, Table> selectTables) {
        referenceElement[0] = REFERENCE;
        if (selectTables == null) {
            referenceElement[1] = element;
            return;
        }
        if (element.equals("#")) referenceElement[1] = selectTables.values().iterator().next();
        else if (element.startsWith("#")) referenceElement[1] = selectTables.get(element.substring(1));
    }

    public void setElements(Vector<?> expression, Map<String, Table> selectTables)
        throws IllegalArgumentException {
        for (int n = 0; n < expression.size(); n++)
            if (expression.elementAt(n) instanceof Vector)
                setElements((Vector<?>)expression.elementAt(n), selectTables);
            else if (expression.elementAt(n) instanceof Object[] &&
                     ((Object[])expression.elementAt(n))[1] instanceof String)
                if (((Object[])expression.elementAt(n))[0] == COLUMN)
                    setColumnElement((Object[])expression.elementAt(n),
                                     (String)((Object[])expression.elementAt(n))[1], selectTables);
                else if (((Object[])expression.elementAt(n))[0] == REFERENCE)
                    setReferenceElement((Object[])expression.elementAt(n),
                                        (String)((Object[])expression.elementAt(n))[1], selectTables);
    }

    public int literalString(Vector<Object> vector, String command, int i)
        throws IllegalArgumentException {
        StringBuffer literalBuf = new StringBuffer();
        while (++i < command.length()) {
            if (command.charAt(i) == '\'')
                if (i + 1 < command.length() && command.charAt(i + 1) == '\'') i++;
                else break;
            else if (command.charAt(i) == '\\')
                if (i + 1 < command.length()) i++;
                else throw new IllegalArgumentException("Invalid literal string in command " + command);
            literalBuf.append(command.charAt(i));
        }
        if (i == command.length() || command.charAt(i) != '\'') throw new IllegalArgumentException("Invalid literal string in command " + command);
        vector.addElement(new String(literalBuf.toString()));
        return ++i;
    }

    public void literalNumber(Vector<Object> vector, String string) {
        int k;
        for (k = 0; k < realChars.length() && string.indexOf(realChars.charAt(k)) == -1; k++);
        if (k == realChars.length()) vector.addElement(Long.valueOf(string));
        else vector.addElement(Double.valueOf(string));
    }

    public Object evaluate(Vector<?> expression, Map<?, ?> values) {
        if (expression.size() == 1) {
            Object o = expression.firstElement();
            if (o instanceof Vector) return evaluate((Vector<?>)o, values);
            if (o instanceof Object[]) return values.get(((Object[])o)[1]);
            return o;
        }
        Number result = new Long(0);
        int i = 0;
        while (i < expression.size()) {
            int j = expression.indexOf(MUL, i);
            int k = expression.indexOf(DIV, i);
            Number value1;
            if (j != -1 || k != -1) {
                if (j == -1 || k < j) j = k;
                k = j - 1;
                Object o = expression.elementAt(j - 1);
                if (o instanceof Vector) value1 = (Number)evaluate((Vector<?>)o, values);
                else if (o instanceof Object[]) value1 = (Number)values.get(((Object[])o)[1]);
                else value1 = (Number)o;
                do {
                    o = expression.elementAt(j + 1);
                    Number value2;
                    if (o instanceof Vector) value2 = (Number)evaluate((Vector<?>)o, values);
                    else if (o instanceof Object[]) value2 =
                                                        (Number)values.get(((Object[])o)[1]);
                    else value2 = (Number)o;
                    if (value1 instanceof BigDecimal || value2 instanceof BigDecimal)
                        if (expression.elementAt(j) == MUL)
                            value1 = value1 instanceof BigDecimal ?
                                ((BigDecimal)value1).multiply(new BigDecimal(value2.doubleValue())) :
                                new BigDecimal(value1.doubleValue()).multiply((BigDecimal)value2);
                        else value1 = value1 instanceof BigDecimal ?
                                 ((BigDecimal)value1).divide(new BigDecimal(value2.doubleValue()), BigDecimal.ROUND_UP) :
                                 new BigDecimal(value1.doubleValue()).divide((BigDecimal)value2, BigDecimal.ROUND_UP);
                    else if (value1 instanceof Double || value2 instanceof Double ||
                             value1 instanceof Float || value2 instanceof Float)
                        if (expression.elementAt(j) == MUL)
                            value1 = new Double(value1.doubleValue() * value2.doubleValue());
                        else value1 = new Double(value1.doubleValue() / value2.doubleValue());
                    else if (expression.elementAt(j) == MUL)
                        value1 = new Long(value1.longValue() * value2.longValue());
                    else value1 = new Long(value1.longValue() / value2.longValue());
                    if (j == expression.size() - 1) break;
                    j += 2;
                } while (j < expression.size() && (expression.elementAt(j) == MUL ||
                                                   expression.elementAt(j) == DIV));
            } else {
                k = (j = expression.size()) - 1;
                Object o = expression.elementAt(k);
                if (o instanceof Vector) value1 = (Number)evaluate((Vector<?>)o, values);
                else if (o instanceof Object[]) value1 = (Number)values.get(((Object[])o)[1]);
                else value1 = (Number)o;
            } while (--k > i) {
                Object o = expression.elementAt(k - 1);
                Number value2;
                if (o instanceof Vector) value2 = (Number)evaluate((Vector<?>)o, values);
                else if (o instanceof Object[]) value2 = (Number)values.get(((Object[])o)[1]);
                else value2 = (Number)o;
                if (value1 instanceof BigDecimal || value2 instanceof BigDecimal)
                    if (expression.elementAt(k) == ADD)
                        value1 = value1 instanceof BigDecimal ?
                            ((BigDecimal)value1).add(new BigDecimal(value2.doubleValue())) :
                            new BigDecimal(value1.doubleValue()).add((BigDecimal)value2);
                    else value1 = value1 instanceof BigDecimal ?
                             ((BigDecimal)value1).subtract(new BigDecimal(value2.doubleValue())) :
                             new BigDecimal(value1.doubleValue()).subtract((BigDecimal)value2);
                else if (value1 instanceof Double || value2 instanceof Double)
                    if (expression.elementAt(k) == ADD)
                        value1 = new Double(value1.doubleValue() + value2.doubleValue());
                    else value1 = new Double(value1.doubleValue() - value2.doubleValue());
                else if (expression.elementAt(k) == ADD)
                    value1 = new Long(value1.longValue() + value2.longValue());
                else value1 = new Long(value1.longValue() - value2.longValue());
                k--;
            }
            if (result instanceof BigDecimal || value1 instanceof BigDecimal)
                if (expression.elementAt(i) == ADD)
                    result = result instanceof BigDecimal ?
                        ((BigDecimal)result).add(new BigDecimal(value1.doubleValue())) :
                        new BigDecimal(result.doubleValue()).add((BigDecimal)value1);
                else result = result instanceof BigDecimal ?
                         ((BigDecimal)result).subtract(new BigDecimal(value1.doubleValue())) :
                         new BigDecimal(result.doubleValue()).subtract((BigDecimal)value1);
            else if (result instanceof Double || value1 instanceof Double)
                if (expression.elementAt(i) == ADD)
                    result = new Double(result.doubleValue() + value1.doubleValue());
                else result = new Double(result.doubleValue() - value1.doubleValue());
            else if (expression.elementAt(i) == ADD)
                result = new Long(result.longValue() + value1.longValue());
            else result = new Long(result.longValue() - value1.longValue());
            i = j;
        }
        return result;
    }

    public int expression(Vector<Object> expression, String command, int i, boolean sub, Map<String, Table> selectTables, int parameterNumber[])
        throws IllegalArgumentException {
        boolean nonConstants = false;
        for (;;) {
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (i == command.length())
                if (sub) throw new IllegalArgumentException("Invalid expression in command " + command);
                else break;
            if (command.charAt(i) == ')') {
                if (sub) i++;
                break;
            }
            if ("+-*/".indexOf(command.charAt(i)) != -1) {
                if (expression.isEmpty() && "*/".indexOf(command.charAt(i)) != -1)
                    throw new IllegalArgumentException("Invalid expression in command " + command);
                expression.addElement(new Character(command.charAt(i)));
                if (++i == command.length()) throw new IllegalArgumentException("Invalid expression in command " + command);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            } else if (!expression.isEmpty()) break;
            if (command.charAt(i) == '(') {
                Vector<Object> expression2 = new Vector<Object>();
                i = expression(expression2, command, ++i, true, selectTables, parameterNumber);
                if (expression2.size() != 1 || !(expression2.firstElement() instanceof Number)) {
                    expression.addElement(expression2);
                    nonConstants = true;
                } else expression.addElement(expression2.firstElement());
                continue;
            }
            if (command.charAt(i) == '\'') {
                i = literalString(expression, command, i);
                nonConstants = true;
                continue;
            }
            int j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   compChars.indexOf(command.charAt(i)) == -1 &&
                   (i == 0 || operChars.indexOf(command.charAt(i)) == -1)) i++;
            if (j == i)
                if (expression.isEmpty() ||
                    expression.elementAt(expression.size() - 1) instanceof Character)
                    throw new IllegalArgumentException("Invalid expression in command " + command);
                else break;
            String string = command.substring(j, i);
            if (Character.isDigit(string.charAt(0)) || "+-.".indexOf(string.charAt(0)) != -1) {
                literalNumber(expression, string);
                continue;
            }
            nonConstants = true;
            if ((j = string.indexOf('(')) != -1) {
                Integer function = null;
                if (string.regionMatches(true, 0, "AVG", 0, j)) function = AVG;
                else if (string.regionMatches(true, 0, "COUNT", 0, j)) function = COUNT;
                else if (string.regionMatches(true, 0, "MAX", 0, j)) function = MAX;
                else if (string.regionMatches(true, 0, "MIN", 0, j)) function = MIN;
                else if (string.regionMatches(true, 0, "SUM", 0, j)) function = SUM;
                else throw new IllegalArgumentException("Unknown function in command " + command);
                expression.addElement(function);
                i = ++j;
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                       command.charAt(i) != ')') i++;
                if (i == command.length() || command.charAt(i) == ')') throw new IllegalArgumentException("Invalid expression in command " + command);
                int l = i - j;
                if (command.regionMatches(true, j, "DISTINCT", 0, l)) {
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1 && command.charAt(i) != ')') i++;
                    Object columnElement[] = new Object[3];
                    setColumnElement(columnElement, command.substring(j, i), selectTables);
                    expression.addElement(columnElement);
                } else if (function == COUNT) {
                    if (!command.regionMatches(j, "*", 0, 1)) throw new IllegalArgumentException("Invalid expression in command " + command);
                    expression.addElement(new Character('*'));
                } else {
                    if (!command.regionMatches(true, j, "ALL", 0, l)) i = j;
                    Vector<Object> expression2 = new Vector<Object>();
                    i = expression(expression2, command, i, false, selectTables,
                                   parameterNumber);
                    expression.addElement(expression2);
                    continue;
                } while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length() || command.charAt(i) != ')') throw new IllegalArgumentException("Invalid expression in command " + command);
                i++;
            } else if (string.equals("?")) {
                Object parameterElement[] = new Object[2];
                parameterElement[0] = PARAMETER;
                parameterElement[1] = new Integer(++parameterNumber[0]);
                expression.addElement(parameterElement);
            } else if (string.startsWith("#")) {
                Object tableElement[] = new Object[2];
                setReferenceElement(tableElement, string, selectTables);
                expression.addElement(tableElement);
            } else {
                Object columnElement[] = new Object[3];
                setColumnElement(columnElement, string, selectTables);
                expression.addElement(columnElement);
            }
        }
        if (!nonConstants) {
            Object result = evaluate(expression, null);
            expression.removeAllElements();
            expression.addElement(result);
        }
        return i;
    }

    public int writeContent(DataOutputStream dout, ColumnDef columnDef, Object literal)
        throws IOException {
        int length = -1;
        switch (columnDef.type) {
        case Types.BIT:
        case Types.TINYINT:
            dout.writeByte(((Number)literal).intValue());
            break;
        case Types.SMALLINT:
            dout.writeShort(((Number)literal).intValue());
            break;
        case Types.INTEGER:
            dout.writeInt(((Number)literal).intValue());
            break;
        case Types.BIGINT:
        case Types.DATE:
        case Types.TIME:
            dout.writeLong(((Number)literal).longValue());
            break;
        case Types.REAL:
        case Types.FLOAT:
            dout.writeFloat(((Number)literal).floatValue());
            break;
        case Types.DOUBLE:
            dout.writeDouble(((Number)literal).doubleValue());
            break;
        case Types.NUMERIC:
        case Types.DECIMAL: {
            byte b[] = new BigDecimal(new BigInteger((String)literal), columnDef.scale).toBigInteger().toByteArray();
            dout.writeInt(b.length);
            dout.write(b);
            break;
        }
        case Types.TIMESTAMP: {
            Timestamp t = Timestamp.valueOf((String)literal);
            dout.writeLong(t.getTime());
            dout.writeInt(t.getNanos());
            break;
        }
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.LONGVARBINARY:
            if (literal instanceof String)
                dout.writeChars(((String)literal).substring(0,
                                                            length = columnDef.type == Types.CHAR &&
                                                            ((String)literal).length() > columnDef.length ?
                                                            columnDef.length : ((String)literal).length()));
            else {
                length = columnDef.type == Types.CHAR &&
                    ((byte[])literal).length > columnDef.length ?
                    columnDef.length : ((byte[])literal).length;
                for (int i = 0; i < length; i++) dout.writeChar(((byte[])literal)[i] & 0xff);
            }
            if (columnDef.type != Types.CHAR) length = -1;
            length *= 2;
            break;
        case Types.BINARY:
        case Types.VARBINARY:
            if (literal instanceof byte[])
                dout.write((byte[])literal, 0,
                           length = columnDef.type == Types.BINARY &&
                           ((byte[])literal).length > columnDef.length ?
                           columnDef.length : ((byte[])literal).length);
            else dout.writeBytes(((String)literal).substring(0,
                                                             length = columnDef.type == Types.BINARY &&
                                                             ((String)literal).length() > columnDef.length ?
                                                             columnDef.length : ((String)literal).length()));
            if (columnDef.type != Types.BINARY) length = -2;
            break;
        }
        return length;
    }

    void writeLargeObject(InputStream in, String name)
        throws IOException {
        FileOutputStream fout = new FileOutputStream(new File(dir, name));
        int n;
        byte b[] = new byte[Support.bufferLength];
        while ((n = in.read(b)) > 0) fout.write(b, 0, n);
        fout.close();
    }

    void writeData(IndexContext xcontext, DataFile dataFile, int dataRef, byte b[], int length, ColumnDef columnDef)
        throws IOException {
        dataFile.seekFile(xcontext.rawDataFile, dataRef, columnDef.offset);
        if (length == -2) {
            int blockRef = xcontext.rawDataFile.readInt(), nextBlockRef = blockRef, offset = columnDef.length;
            xcontext.rawDataFile.writeInt(b.length);
            xcontext.rawDataFile.write(b, 0, Math.min(b.length, columnDef.length));
            while (offset < b.length && nextBlockRef != 0) {
                blockRef = nextBlockRef;
                blockFile.seekFile(xcontext.rawBlockFile, blockRef);
                nextBlockRef = xcontext.rawBlockFile.readInt();
                int n = Math.min(b.length - offset, blockFile.recordLength - 4);
                xcontext.rawBlockFile.write(b, offset, n);
                offset += n;
            }
            if (nextBlockRef != 0) {
                if (blockRef == nextBlockRef) {
                    dataFile.seekFile(xcontext.rawDataFile, blockRef);
                    xcontext.rawDataFile.writeInt(0);
                } else {
                    blockFile.seekFile(xcontext.rawBlockFile, blockRef);
                    xcontext.rawBlockFile.writeInt(0);
                } while (nextBlockRef != 0) {
                    blockRef = nextBlockRef;
                    blockFile.seekFile(xcontext.rawBlockFile, blockRef);
                    nextBlockRef = xcontext.rawBlockFile.readInt();
                    blockFile.deleteData(xcontext.rawBlockFile, blockRef);
                }
            }
            else
                while (offset < b.length) {
                    int newBlockRef = blockFile.newData(xcontext.rawBlockFile);
                    if (blockRef == nextBlockRef) {
                        dataFile.seekFile(xcontext.rawDataFile, blockRef);
                        xcontext.rawDataFile.writeInt(newBlockRef);
                    } else {
                        blockFile.seekFile(xcontext.rawBlockFile, blockRef);
                        xcontext.rawBlockFile.writeInt(newBlockRef);
                    }
                    blockRef = newBlockRef;
                    blockFile.seekFile(xcontext.rawBlockFile, blockRef, 4);
                    int n = Math.min(b.length - offset, blockFile.recordLength - 4);
                    xcontext.rawBlockFile.write(b, offset, n);
                    offset += n;
                }
        } else {
            if (length != -1) xcontext.rawDataFile.writeInt(length);
            xcontext.rawDataFile.write(b);
        }
    }

    public int searchCondition(Vector<Object> searchCondition, String command, int i, boolean sub, Map<String, Table> selectTables, int parameterNumber[])
        throws IllegalArgumentException, IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        for (;;) {
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (i == command.length()) break;
            if (command.charAt(i) == ')') {
                if (sub) i++;
                return i;
            }
            int j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   compChars.indexOf(command.charAt(i)) == -1) i++;
            int l = i - j;
            if (!searchCondition.isEmpty()) {
                if (command.regionMatches(true, j, "AND", 0, l))
                    searchCondition.addElement(AND);
                else if (command.regionMatches(true, j, "OR", 0, l))
                    searchCondition.addElement(OR);
                else return j;
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                       compChars.indexOf(command.charAt(i)) == -1) i++;
                l = i - j;
            }
            if (command.regionMatches(true, j, "NOT", 0, l)) {
                searchCondition.addElement(NOT);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                       compChars.indexOf(command.charAt(i)) == -1) i++;
                l = i - j;
            }
            if (command.charAt(j) == '(') {
                Vector<Object> searchCondition2 = new Vector<Object>();
                i = searchCondition(searchCondition2, command, ++j, true, selectTables, parameterNumber);
                searchCondition.addElement(searchCondition2);
                continue;
            }
            if (command.regionMatches(true, j, "EXISTS", 0, l)) {
                searchCondition.addElement(EXISTS);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length() || command.charAt(i) != '(') throw new IllegalArgumentException("Invalid exists-clause in command " + command);
                i++;
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                if (!command.regionMatches(true, j, "SELECT", 0, i - j)) throw new IllegalArgumentException("Invalid exists-clause in command " + command);
                searchCondition.addElement(SELECT);
                Vector<Object> selectVector = new Vector<Object>();
                i = select(selectVector, command, i, true, parameterNumber);
                searchCondition.addElement(selectVector);
                continue;
            } while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            int k = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   compChars.indexOf(command.charAt(i)) == -1) i++;
            int m = i - k;
            boolean not = false;
            if (command.regionMatches(true, k, "NOT", 0, m)) {
                not = true;
                while (i < command.length() &&
                       whites.indexOf(command.charAt(i)) != -1) i++;
                k = i;
                while (i < command.length() &&
                       whites.indexOf(command.charAt(i)) == -1) i++;
                m = i - k;
            }
            if (command.regionMatches(true, k, "LIKE", 0, m)) {
                Object columnElement[] = new Object[3];
                setColumnElement(columnElement, command.substring(j, j + l), selectTables);
                searchCondition.addElement(columnElement);
                if (not) searchCondition.addElement(NOT);
                searchCondition.addElement(LIKE);
                while (i < command.length() &&
                       whites.indexOf(command.charAt(i)) != -1) i++;
                if (command.charAt(i) == '?') {
                    Object parameterElement[] = new Object[2];
                    parameterElement[0] = PARAMETER;
                    parameterElement[1] = new Integer(++parameterNumber[0]);
                    searchCondition.addElement(parameterElement);
                    i++;
                } else if (command.charAt(i) != '\'') throw new IllegalArgumentException("Invalid like-clause in command " + command);
                else i = literalString(searchCondition, command, i);
                continue;
            }
            Vector<Object> expression = new Vector<Object>();
            i = expression(expression, command, j, false, selectTables, parameterNumber);
            searchCondition.addElement(expression);
            if (i == command.length()) throw new IllegalArgumentException("Invalid expression in command " + command);
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   compChars.indexOf(command.charAt(i)) == -1) i++;
            l = i - j;
            if (i == command.length()) throw new IllegalArgumentException("Invalid expression in command " + command);
            if (compChars.indexOf(command.charAt(i)) != -1) {
                j = i;
                while (compChars.indexOf(command.charAt(++i)) != -1);
                l = i - j;
            } else if (command.regionMatches(true, j, "NOT", 0, l)) {
                searchCondition.addElement(NOT);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length()) throw new IllegalArgumentException("Invalid expression in command " + command);
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                       compChars.indexOf(command.charAt(i)) == -1) i++;
                l = i - j;
            }
            if (command.regionMatches(true, j, "BETWEEN", 0, l)) {
                searchCondition.addElement(BETWEEN);
                expression = new Vector<Object>();
                i = expression(expression, command, i, false, selectTables, parameterNumber);
                searchCondition.addElement(expression);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                if (!command.regionMatches(true, j, "AND", 0, i - j)) throw new IllegalArgumentException("Invalid between-clause in command " + command);
                expression = new Vector<Object>();
                i = expression(expression, command, i, false, selectTables, parameterNumber);
                searchCondition.addElement(expression);
                continue;
            }
            if (command.regionMatches(true, j, "IN", 0, l)) {
                searchCondition.addElement(IN);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (command.charAt(i) == '(') {
                    i++;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1) i++;
                    if (command.regionMatches(true, j, "SELECT", 0, i - j)) {
                        searchCondition.addElement(SELECT);
                        Vector<Object> selectVector = new Vector<Object>();
                        i = select(selectVector, command, i, true, parameterNumber);
                        searchCondition.addElement(selectVector);
                        continue;
                    }
                    Vector<Object> inList = new Vector<Object>();
                    i = j;
                    while (i < command.length() && command.charAt(i) != ')') {
                        if (command.charAt(i) != '\'') {
                            j = i;
                            while (i < command.length() &&
                                   whites.indexOf(command.charAt(i)) == -1) i++;
                            String string = command.substring(j, i);
                            if (string.equals("?")) {
                                Object parameterElement[] = new Object[2];
                                parameterElement[0] = PARAMETER;
                                parameterElement[1] =
                                    new Integer(++parameterNumber[0]);
                                inList.addElement(parameterElement);
                            } else if (Character.isDigit(string.charAt(0)) ||
                                       "+-.".indexOf(string.charAt(0)) != -1)
                                literalNumber(inList, string);
                            else throw new IllegalArgumentException("Invalid in-clause in command " + command);
                        } else i = literalString(inList, command, i);
                        while (i < command.length() &&
                               whites.indexOf(command.charAt(i)) != -1) i++;
                    }
                    if (i == command.length() || command.charAt(i) != ')')
                        throw new IllegalArgumentException("Invalid in-clause in command " + command);
                    continue;
                }
                expression = new Vector<Object>();
                i = expression(expression, command, i, false, selectTables, parameterNumber);
                searchCondition.addElement(expression);
                continue;
            }
            if (command.regionMatches(true, j, "IS", 0, l)) {
                searchCondition.addElement(IS);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                l = i - j;
                if (command.regionMatches(true, j, "NOT", 0, l)) {
                    searchCondition.addElement(NOT);
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1) i++;
                    l = i - j;
                }
                if (!command.regionMatches(true, j, "NULL", 0, l)) throw new IllegalArgumentException("Invalid is-clause in command " + command);
                continue;
            }
            Integer comparision;
            if (command.regionMatches(j, "=", 0, 1)) comparision = EQUAL;
            else if (command.regionMatches(j, "<>", 0, 2)) comparision = UNEQUAL;
            else if (command.regionMatches(j, ">", 0, 2)) comparision = GREATER;
            else if (command.regionMatches(j, "<", 0, 2)) comparision = LESS;
            else if (command.regionMatches(j, ">=", 0, 2)) comparision = GREATER_OR_EQUAL;
            else if (command.regionMatches(j, "<=", 0, 2)) comparision = LESS_OR_EQUAL;
            else throw new IllegalArgumentException("Invalid comparision-operator in command " + command);
            searchCondition.addElement(comparision);
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            l = i - j;
            Integer attribute = null;
            if (command.regionMatches(true, j, "SOME", 0, l)) attribute = SOME;
            else if (command.regionMatches(true, j, "ANY", 0, l)) attribute = ANY;
            else if (command.regionMatches(true, j, "ALL", 0, l)) attribute = ALL;
            if (attribute != null) {
                searchCondition.addElement(attribute);
                while (i < command.length() &&
                       whites.indexOf(command.charAt(i)) != -1) i++;
            } else i = j;
            if (command.charAt(i) == '(') {
                i++;
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                if (!command.regionMatches(true, j, "SELECT", 0, i - j)) throw new IllegalArgumentException("Invalid sub-select in command " + command);
                searchCondition.addElement(SELECT);
                Vector<Object> selectVector = new Vector<Object>();
                i = select(selectVector, command, i, true, parameterNumber);
                searchCondition.addElement(selectVector);
                continue;
            } else if (attribute != null) throw new IllegalArgumentException("Invalid expression in command " + command);
            expression = new Vector<Object>();
            i = expression(expression, command, i, false, selectTables, parameterNumber);
            searchCondition.addElement(expression);
        }
        if (sub) throw new IllegalArgumentException("Invalid expression in command " + command);
        return i;
    }

    public int select(Vector<Object> selectVector, String command, int i, boolean sub, int parameterNumber[])
        throws IllegalArgumentException, IOException {
        while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
        int j = i;
        while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
        int l = i - j;
        if (command.regionMatches(true, j, "DISTINCT", 0, l)) selectVector.addElement(DISTINCT);
        else if (!command.regionMatches(true, j, "ALL", 0, l)) i = j;
        Vector<Object> selectList = new Vector<Object>();
        while (i < command.length()) {
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   command.charAt(i) != ',') i++;
            if (j == i) throw new IllegalArgumentException("Invalid command " + command);
            if (command.charAt(i - 1) != '*') {
                Vector<Object> expression = new Vector<Object>();
                i = expression(expression, command, j, false, null, parameterNumber);
                selectList.addElement(expression);
            } else selectList.addElement(command.substring(j, i));
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (i == command.length() || command.charAt(i) != ',') break;
            i++;
        }
        selectVector.addElement(selectList);
        j = i;
        while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
        if (!command.regionMatches(true, j, "FROM", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
        Map<String, Table> selectTables = new HashMap<String, Table>();
        Vector<From> fromVector = new Vector<From>();
        for (;;) {
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (i == command.length()) break;
            if (command.charAt(i) == ')') {
                if (sub) i++;
                return i;
            }
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   command.charAt(i) != ')' && command.charAt(i) != ',') i++;
            String tableName = command.substring(j, i);
            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("No table name " + tableName + " found in command " + command);
            selectTables.put(tableName, table);
            From from = new From();
            from.table = table;
            fromVector.addElement(from);
            while (i < command.length() && delims.indexOf(command.charAt(i)) != -1) i++;
            if (i == command.length()) break;
            if (command.charAt(i) == ')') {
                if (sub) i++;
                return i;
            }
            if (command.charAt(i) == ',') {
                i++;
                continue;
            }
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                   command.charAt(i) != ')' && command.charAt(i) != ',') i++;
            selectTables.put(command.substring(j, i), table);
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (i == command.length()) break;
            if (command.charAt(i) == ')') {
                if (sub) i++;
                return i;
            }
            if (command.charAt(i) != ',') break;
            i++;
        }
        selectVector.addElement(fromVector);
        int k;
        for (int n = 0; n < selectList.size(); n++)
            if (selectList.elementAt(n) instanceof Vector)
                setElements((Vector<?>)selectList.elementAt(n), selectTables);
            else if (((String)selectList.elementAt(n)).equals("*")) selectList.setElementAt(null, n);
            else if ((k = ((String)selectList.elementAt(n)).indexOf('.')) != -1) {
                String tableName = ((String)selectList.elementAt(n)).substring(0, k);
                Table table = selectTables.get(tableName);
                if (table == null) throw new IllegalArgumentException("No table name " + tableName + " found in command " + command);
                selectList.setElementAt(table, n);
            } else throw new IllegalArgumentException("Invalid expression in command " + command);
        boolean where = false;
        while (i < command.length()) {
            if (command.charAt(i) == ')') {
                if (sub) i++;
                return i;
            }
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            l = i - j;
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (command.regionMatches(true, j, "WHERE", 0, l)) {
                if (where) throw new IllegalArgumentException("Invalid where-clause in command " + command);
                where = true;
                selectVector.addElement(WHERE);
                Vector<Object> searchCondition = new Vector<Object>();
                i = searchCondition(searchCondition, command, i, false, selectTables, parameterNumber);
                selectVector.addElement(searchCondition);
                continue;
            }
            if (command.regionMatches(true, j, "ORDER", 0, l)) {
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                if (!command.regionMatches(true, j, "BY", 0, i - j)) throw new IllegalArgumentException("Invalid order-clause in command " + command);
                Object columnElement[] = new Object[3];
                while (i < command.length()) {
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) break;
                    if (command.charAt(i) == ')') {
                        if (sub) i++;
                        return i;
                    }
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1 && command.charAt(i) != ')' &&
                           command.charAt(i) != ',') i++;
                    setColumnElement(columnElement, command.substring(j, i), selectTables);
                    int n;
                    From from = null;
                    for (n = 0; n < fromVector.size(); n++)
                        if ((from = fromVector.elementAt(n)).table == columnElement[2]) {
                            from.indexFile = ((Column)columnElement[1]).indexFile;
                            break;
                        }
                    if (n == fromVector.size()) throw new IllegalArgumentException("Invalid order-clause in command " + command);
                    while (i < command.length() &&
                           delims.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) break;
                    if (command.charAt(i) == ')') {
                        if (sub) i++;
                        return i;
                    }
                    if (command.charAt(i) == ',') {
                        i++;
                        continue;
                    }
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1 &&
                           command.charAt(i) != ')' && command.charAt(i) != ',') i++;
                    l = i - j;
                    if (command.regionMatches(true, j, "DESC", 0, l)) from.descending = true;
                    else if (!command.regionMatches(true, j, "ASC", 0, l)) throw new IllegalArgumentException("Invalid order-clause in command " + command);
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) break;
                    if (command.charAt(i) == ')') {
                        if (sub) i++;
                        return i;
                    }
                    if (command.charAt(i) != ',') break;
                    i++;
                }
                continue;
            }
            Integer operator;
            if (command.regionMatches(true, j, "INTERSECT", 0, l)) operator = INTERSECT;
            else if (command.regionMatches(true, j, "MINUS", 0, l)) operator = MINUS;
            else if (command.regionMatches(true, j, "UNION", 0, l)) operator = UNION;
            else throw new IllegalArgumentException("Invalid set operator in command " + command);
            selectVector.addElement(operator);
            if (operator == UNION) {
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                if (command.regionMatches(true, j, "ALL", 0, i - j)) {
                    selectVector.addElement(ALL);
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                } else i = j;
            }
            if (i == command.length()) throw new IllegalArgumentException("Invalid expression in command " + command);
            boolean sub2;
            if (command.charAt(i) == '(') {
                sub2 = true;
                i++;
            } else sub2 = false;
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            if (!command.regionMatches(true, j, "SELECT", 0, i - j)) throw new IllegalArgumentException("Invalid expression in command " + command);
            Vector<Object> selectVector2 = new Vector<Object>();
            i = select(selectVector2, command, i, sub2, parameterNumber);
            selectVector.addElement(selectVector2);
        }
        if (sub) throw new IllegalArgumentException("Invalid expression in command " + command);
        return i;
    }

    public boolean satisfies(Vector<?> searchCondition, Map<?, ?> values)
        throws IllegalArgumentException {
        boolean result = true;
        for (int n = 0; n < searchCondition.size(); n++) {
            Integer op = null;
            boolean not = false, result1 = false;
            if (searchCondition.elementAt(n) instanceof Integer) {
                if (searchCondition.elementAt(n) != NOT) {
                    op = (Integer)searchCondition.elementAt(n);
                    n++;
                }
                if (searchCondition.elementAt(n) == NOT) {
                    not = true;
                    n++;
                }
            }
            Integer op1;
            if (searchCondition.elementAt(n) instanceof Vector &&
                (n == 0 || searchCondition.elementAt(n - 1) instanceof Integer &&
                 (op1 = (Integer)searchCondition.elementAt(n - 1)).intValue() >= NOT.intValue() &&
                 op1.intValue() <= OR.intValue()) && (n == searchCondition.size() - 1 ||
                                                      searchCondition.elementAt(n + 1) instanceof Integer &&
                                                      (op1 = (Integer)searchCondition.elementAt(n + 1)).intValue() >= AND.intValue() &&
                                                      op1.intValue() <= OR.intValue()))
                result1 = satisfies((Vector<?>)searchCondition.elementAt(n), values);
            else if (n + 1 < searchCondition.size() &&
                     searchCondition.elementAt(n + 1) instanceof Integer)
                if (n + 2 < searchCondition.size() &&
                    (op1 = (Integer)searchCondition.elementAt(n + 1)).intValue() >= EQUAL.intValue() &&
                    op1.intValue() <= LESS_OR_EQUAL.intValue()) {
                    Object value = evaluate((Vector<?>)searchCondition.elementAt(n), values),
                        value1 = evaluate((Vector<?>)searchCondition.elementAt(n + 2), values);
                    if (op1 == EQUAL)
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() == ((Number)value1).doubleValue();
                        else result1 = Support.compare(value, value1) == 0;
                    else if (op1 == UNEQUAL)
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() != ((Number)value1).doubleValue();
                        else result1 = Support.compare(value, value1) != 0;
                    else if (op1 == GREATER)
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() > ((Number)value1).doubleValue();
                        else result1 = Support.compare(value, value1) > 0;
                    else if (op1 == LESS)
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() < ((Number)value1).doubleValue();
                        else result1 = Support.compare(value, value1) < 0;
                    else if (op1 == GREATER_OR_EQUAL)
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() >= ((Number)value1).doubleValue();
                        else result1 = Support.compare(value, value1) >= 0;
                    else if (op1 == LESS_OR_EQUAL)
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() <= ((Number)value1).doubleValue();
                        else result1 = Support.compare(value, value1) <= 0;
                } else {
                    boolean not1 = (Integer)searchCondition.elementAt(n + 1) == NOT;
                    if (n + (not1 ? 4 : 3) < searchCondition.size() &&
                        searchCondition.elementAt(n + (not1 ? 2 : 1)) instanceof Integer &&
                        searchCondition.elementAt(n + (not1 ? 2 : 1)) == BETWEEN) {
                        Object value = evaluate((Vector<?>)searchCondition.elementAt(n), values),
                            value1 = evaluate((Vector<?>)searchCondition.elementAt(n + (not1 ? 3 : 2)), values),
                            value2 = evaluate((Vector<?>)searchCondition.elementAt(n + (not1 ? 4 : 3)), values);
                        if (value instanceof Number)
                            result1 = ((Number)value).doubleValue() >= ((Number)value1).doubleValue() &&
                                ((Number)value).doubleValue() <= ((Number)value2).doubleValue();
                        else result1 = Support.compare(value, value1) >= 0 &&
                                 Support.compare(value, value2) <= 0;
                        if (not1) result1 = !result1;
                        n += not1 ? 4 : 3;
                    } else if (n + (not1 ? 3 : 2) < searchCondition.size() &&
                               searchCondition.elementAt(n + (not1 ? 2 : 1)) instanceof Integer &&
                               (Integer)searchCondition.elementAt(n + (not1 ? 2 : 1)) == LIKE) {
                        Object value = values.get(((Object[])searchCondition.elementAt(n))[1]);
                        String pattern = searchCondition.elementAt(n + (not1 ? 3 : 2)) instanceof Object[] ?
                            (String)values.get(((Object[])searchCondition.elementAt(n + (not1 ? 3 : 2)))[1]) :
                            (String)searchCondition.elementAt(n + (not1 ? 3 : 2));
                        RegexpPool pool = new RegexpPool();
                        try {
                            pool.add(pattern, Boolean.TRUE);
                        } catch (RegexException ex) {
                            throw new IllegalArgumentException(Support.stackTrace(ex));
                        }
                        result1 = pool.match(value instanceof byte[] ? new String((byte[])value).trim() : (String)value) != null;
                        if (not1) result1 = !result1;
                        n += not1 ? 3 : 2;
                    } else continue;
                } else continue;
            if (not) result1 = !result1;
            if (op != null)
                if (op == AND) result &= result1;
                else result |= result1;
            else result = result1;
        }
        return result;
    }

    public boolean passes(Vector<?> resultVector, Map<?, ?> values)
        throws IllegalArgumentException {
        boolean result;
        int n = resultVector.indexOf(WHERE);
        if (n != -1) result = satisfies((Vector<?>)resultVector.elementAt(n + 1), values);
        else result = true;
        n = resultVector.indexOf(INTERSECT);
        if (n != -1) return result && passes((Vector<?>)resultVector.elementAt(n + 1), values);
        n = resultVector.indexOf(MINUS);
        if (n != -1) return result && !passes((Vector<?>)resultVector.elementAt(n + 1), values);
        n = resultVector.indexOf(UNION);
        if (n != -1) {
            if (resultVector.elementAt(n + 1) instanceof Integer) n++;
            return result || passes((Vector<?>)resultVector.elementAt(n + 1), values);
        }
        return result;
    }

    public void readContents(IndexContext xcontext, Table table, Map<Object, Object> values, int dataRef)
        throws IOException {
        DataFile dataFile = table.dataFile;
        values.put(table, new Long(dataRef));
        Iterator<Column> columnIter = table.columnVector.iterator();
        while (columnIter.hasNext()) {
            Column column = columnIter.next();
            ColumnDef columnDef = column.columnDef;
            dataFile.seekFile(xcontext.rawDataFile, dataRef, columnDef.offset);
            switch (columnDef.type) {
            case Types.BIT:
            case Types.TINYINT:
                values.put(column, new Integer(xcontext.rawDataFile.readByte()));
                break;
            case Types.SMALLINT:
                values.put(column, new Integer(xcontext.rawDataFile.readShort()));
                break;
            case Types.INTEGER:
                values.put(column, new Integer(xcontext.rawDataFile.readInt()));
                break;
            case Types.BIGINT:
            case Types.DATE:
            case Types.TIME:
                values.put(column, new Long(xcontext.rawDataFile.readLong()));
                break;
            case Types.REAL:
            case Types.FLOAT:
                values.put(column, new Float(xcontext.rawDataFile.readFloat()));
                break;
            case Types.DOUBLE:
                values.put(column, new Double(xcontext.rawDataFile.readDouble()));
                break;
            case Types.NUMERIC:
            case Types.DECIMAL: {
                byte b[] = new byte[xcontext.rawDataFile.readInt()];
                xcontext.rawDataFile.readFully(b);
                values.put(column, new BigDecimal(new BigInteger(b), columnDef.scale));
                break;
            }
            case Types.TIMESTAMP: {
                java.util.Date d = new java.util.Date(xcontext.rawDataFile.readLong());
                Timestamp t = new Timestamp(d.getTime());
                t.setNanos(xcontext.rawDataFile.readInt());
                values.put(column, t.toString());
                break;
            }
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGVARBINARY: {
                if (columnDef.length == 0) {
                    String value[] = new String[1];
                    value[0] = table.tableName + "_" + column.columnName + "_" + dataRef;
                    values.put(column, value);
                    break;
                }
                int blockRef = columnDef.type == Types.CHAR ? 0 : xcontext.rawDataFile.readInt(),
                    length = xcontext.rawDataFile.readInt() / 2,
                    blockLength = blockFile != null ? (blockFile.recordLength - 4) / 2 : 0;
                StringBuffer sb = new StringBuffer(length);
                for (int n = Math.min(length, columnDef.length); n > 0; n--)
                    sb.append(xcontext.rawDataFile.readChar());
                length -= columnDef.length;
                while (blockRef != 0) {
                    blockFile.seekFile(xcontext.rawBlockFile, blockRef);
                    blockRef = xcontext.rawBlockFile.readInt();
                    for (int n = Math.min(length, blockLength); n > 0; n--)
                        sb.append(xcontext.rawBlockFile.readChar());
                    length -= blockLength;
                }
                if (columnDef.type == Types.LONGVARCHAR || columnDef.type == Types.LONGVARBINARY) {
                    String value[] = new String[1];
                    value[0] = sb.toString();
                    values.put(column, value);
                } else values.put(column, sb.toString());
                break;
            }
            case Types.BINARY:
            case Types.VARBINARY: {
                int blockRef = columnDef.type == Types.BINARY ? 0 : xcontext.rawDataFile.readInt();
                byte b[] = new byte[xcontext.rawDataFile.readInt()];
                int n = Math.min(b.length, columnDef.length), offset = columnDef.length;
                xcontext.rawDataFile.readFully(b, 0, n);
                while (blockRef != 0) {
                    blockFile.seekFile(xcontext.rawBlockFile, blockRef);
                    blockRef = xcontext.rawBlockFile.readInt();
                    n = Math.min(b.length - offset, blockFile.recordLength - 4);
                    xcontext.rawBlockFile.readFully(b, offset, n);
                    offset += blockFile.recordLength - 4;
                }
                values.put(column, b);
                break;
            }
            }
        }
    }

    public int directRead(Table fromTable, Vector<From> fromVector, Vector<?> searchCondition, Map<Object, Object> context, Map<Object, Object> values)
        throws IllegalArgumentException, InterruptedException, InterruptedException, IOException {
        boolean empty;
        if (context.isEmpty()) {
            int n;
            for (n = 0; n < searchCondition.size(); n++)
                if (searchCondition.elementAt(n) instanceof Vector) {
                    Vector<?> v = (Vector<?>)searchCondition.elementAt(n), v1;
                    if (v.size() != 1 || !(v.firstElement() instanceof Object[]) ||
                        !(searchCondition.elementAt(n + 1) instanceof Integer) ||
                        searchCondition.elementAt(n + 1) != EQUAL ||
                        (v1 = (Vector<?>)searchCondition.elementAt(n + 2)).size() != 1) break;
                    Object o;
                    if (((Object[])v.firstElement())[0] == COLUMN) {
                        if (v1.firstElement() instanceof Number || v1.firstElement() instanceof String)
                            o = v1.firstElement();
                        else if (v1.firstElement() instanceof Object[] &&
                                 ((Object[])v1.firstElement())[0] == PARAMETER)
                            o = values.get(((Object[])v1.firstElement())[1]);
                        else break;
                        Object a[] = new Object[4];
                        a[0] = new IndexItem();
                        a[1] = o;
                        a[2] = ((Object[])v.firstElement())[1];
                        a[3] = new HashMap();
                        context.put(((Object[])v.firstElement())[2], a);
                    } else if (((Object[])v.firstElement())[0] == REFERENCE) {
                        if (v1.firstElement() instanceof Number) o = v1.firstElement();
                        else if (v1.firstElement() instanceof Object[] &&
                                 ((Object[])v1.firstElement())[0] == PARAMETER)
                            o = values.get(((Object[])v1.firstElement())[1]);
                        else break;
                        Table table = (Table)((Object[])v.firstElement())[1];
                        if (context.containsKey(table)) throw new IllegalArgumentException("Invalid command");
                        Object a[] = new Object[2];
                        a[0] = new IndexItem();
                        a[1] = o;
                        context.put(table, a);
                    } else break;
                    n += 2;
                } else if (!(searchCondition.elementAt(n) instanceof Integer) ||
                           searchCondition.elementAt(n) != AND) break;
            if (n < searchCondition.size()) {
                context.clear();
                return -1;
            }
            if (fromVector != null) {
                Iterator<From> fromIter = fromVector.iterator();
                while (fromIter.hasNext())
                    if (!context.containsKey(fromIter.next().table)) return -1;
            }
            empty = true;
        } else if (!(context.values().iterator().next() instanceof Object[])) return -1;
        else empty = false;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        int n1;
        if (fromVector != null) n1 = fromVector.size();
        else n1 = 1;
        do
            for (int n = empty ? 0 : n1 - 1; n < n1; n++) {
                Table table;
                if (fromVector != null) table = fromVector.elementAt(n).table;
                else table = fromTable;
                Object a[] = (Object[])context.get(table);
                DataFile dataFile = table.dataFile;
                rwControl.startRead();
                try {
                    IndexContext xcontext = table.index.indexFile.openIndex(false, table.needsBlockFile ? blockFile : null);
                    try {
                        IndexItem xitem = (IndexItem)a[0];
                        if (a.length == 4) {
                            Column column = (Column)a[2];
                            Map<Integer, Boolean> h = (Map<Integer, Boolean>)a[3];
                            IndexFile indexFile = column.indexFile;
                            if (xitem.dataRef != 0) {
                                byte key[] = xitem.key;
                                if ((!indexFile.locateItem(xcontext, xitem) || !indexFile.nextItem(xcontext, xitem)) &&
                                    (!indexFile.searchItem(xcontext, xitem) || Support.compare(xitem.key, key, 0, 0, key.length) != 0)) {
                                    h.clear();
                                    xitem.dataRef = 0;
                                    if (n == 0) return 0;
                                    n -= 2;
                                    continue;
                                }
                            } else {
                                writeContent(dout, column.columnDef, a[1]);
                                byte key[] = xitem.key = bout.toByteArray();
                                bout.reset();
                                if (!indexFile.searchItem(xcontext, xitem) || key.length == 0 ||
                                    Support.compare(xitem.key, key, 0, 0, key.length) != 0) return 0;
                            }
                            Integer dr = new Integer(xitem.dataRef);
                            if (h.containsKey(dr)) {
                                n--;
                                continue;
                            }
                            h.put(dr, Boolean.TRUE);
                        } else {
                            if (xitem.dataRef != 0) {
                                xitem.dataRef = 0;
                                if (n == 0) return 0;
                                n -= 2;
                                continue;
                            }
                            xitem.dataRef = ((Number)a[1]).intValue();
                        }
                        readContents(xcontext, table, values, xitem.dataRef);
                    } finally {
                        table.index.indexFile.close(xcontext);
                    }
                } finally {
                    rwControl.stopRead();
                }
            } while (!satisfies(searchCondition, values));
        return 1;
    }

    public Vector<?> move(Vector<?> selectVector, Map<Object, Object> context, Map<Object, Object> values, int number)
        throws IllegalArgumentException, InterruptedException, IOException {
        int n;
        boolean backward, distinct;
        if (number < 0) {
            backward = true;
            number = -number;
        } else backward = false;
        if (selectVector.firstElement() == DISTINCT) {
            distinct = true;
            n = 1;
        } else {
            distinct = false;
            n = 0;
        }
        Vector<?> selectList = (Vector<?>)selectVector.elementAt(n++);
        Vector<From> fromVector = (Vector<From>)selectVector.elementAt(n++);
        n = selectVector.indexOf(WHERE, n);
        Vector<?> searchCondition;
        if (n != -1) searchCondition = (Vector<?>)selectVector.elementAt(++n);
        else searchCondition = null;
        if (number > 0 && searchCondition != null &&
            (n = directRead(null, fromVector, searchCondition, context, values)) != -1) {
            if (n == 0) return null;
            while (--number > 0 && n == 1)
                n = directRead(null, fromVector, searchCondition, context, values);
        }
        else
            while (number > 0) {
                do
                    for (n = context.isEmpty() ? 0 : fromVector.size() - 1; n < fromVector.size(); n++) {
                        From from = fromVector.elementAt(n);
                        Table table = from.table;
                        IndexFile indexFile = from.indexFile;
                        if (indexFile == null) indexFile = table.index.indexFile;
                        Object object = context.get(table);
                        IndexItem xitem = object instanceof IndexItem ? (IndexItem)object : null;
                        rwControl.startRead();
                        try {
                            IndexContext xcontext = indexFile.openIndex(false, table.needsBlockFile ? blockFile : null);
                            try {
                                if (xitem == null) {
                                    context.put(table, xitem = new IndexItem());
                                    xcontext.clear();
                                } else {
                                    if (xitem.dataRef == 0) xcontext.clear();
                                    else if (!indexFile.locateItem(xcontext, xitem) &&
                                             !indexFile.searchItem(xcontext, xitem)) return null;
                                }
                                if (from.descending ^ backward ?
                                    indexFile.prevItem(xcontext, xitem) :
                                    indexFile.nextItem(xcontext, xitem)) {
                                    readContents(xcontext, table, values, xitem.dataRef);
                                    continue;
                                }
                                xitem.dataRef = 0;
                                if (n == 0) return null;
                                n -= 2;
                            } finally {
                                indexFile.close(xcontext);
                            }
                        } finally {
                            rwControl.stopRead();
                        }
                    } while (!passes(selectVector, values));
                number--;
            }
        Vector<Object> row = new Vector<Object>();
        Iterator<?> selectListIter = selectList.iterator();
        while (selectListIter.hasNext()) {
            Object selectListElement = selectListIter.next();
            if (selectListElement == null) {
                Iterator<From> fromIter = fromVector.iterator();
                while (fromIter.hasNext()) {
                    Iterator columnIter = fromIter.next().table.columnVector.iterator();
                    while (columnIter.hasNext()) row.addElement(values.get(columnIter.next()));
                }
            } else if (selectListElement instanceof Object[]) {
                Iterator<Column> columnIter = ((Vector<Column>)((Object[])selectListElement)[3]).iterator();
                while (columnIter.hasNext()) row.addElement(values.get(columnIter.next()));
            } else row.addElement(evaluate((Vector<?>)selectListElement, values));
        }
        return row;
    }

    public Vector<?> prepare(String command)
        throws IllegalArgumentException, IOException {
        Vector<Object> commandVector = new Vector<Object>();
        int i = 0, j = 0;
        while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
        j = i;
        while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
        int l = i - j;
        while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
        int parameterNumber[] = new int[1];
        if (command.regionMatches(true, j, "SELECT", 0, l)) {
            commandVector.addElement(SELECT);
            Vector<Object> selectVector = new Vector<Object>();
            select(selectVector, command, i, false, parameterNumber);
            commandVector.addElement(selectVector);
        } else if (command.regionMatches(true, j, "UPDATE", 0, l)) {
            commandVector.addElement(UPDATE);
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            String tableName = command.substring(j, i);
            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("No table name " + tableName + " found in command " + command);
            Map<String, Column> columns = table.columns;
            Map<String, Index> indices = table.indices;
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            if (!command.regionMatches(true, j, "SET", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
            commandVector.addElement(table);
            Map<String, Table> selectTables = new HashMap<String, Table>();
            selectTables.put(tableName, table);
            Map<Column, Vector<Object>> setTable = new HashMap<Column, Vector<Object>>();
            Map<Index, Index> indexTable = new HashMap<Index, Index>();
            while (i < command.length()) {
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1 &&
                       command.charAt(i) != '=') i++;
                String columnName = command.substring(j, i);
                Column column = columns.get(columnName);
                if (column == null) throw new IllegalArgumentException("No column name " + columnName + " found in command " + command);
                ColumnDef columnDef = column.columnDef;
                Iterator<Index> indexIter = indices.values().iterator();
                while (indexIter.hasNext()) {
                    Index index = indexIter.next();
                    if (index.columnTable.containsKey(column)) indexTable.put(index, index);
                } while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length() || command.charAt(i) != '=') throw new IllegalArgumentException("Invalid command " + command);
                Vector<Object> expression = new Vector<Object>();
                i = expression(expression, command, ++i, false, selectTables, parameterNumber);
                setTable.put(column, expression);
                if (i == command.length() || command.charAt(i) != ',') break;
                i++;
            }
            commandVector.addElement(setTable);
            commandVector.addElement(indexTable);
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            if (i > j) {
                if (!command.regionMatches(true, j, "WHERE", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
                Vector<Object> searchCondition = new Vector<Object>();
                i = searchCondition(searchCondition, command, i, false, selectTables, parameterNumber);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i < command.length()) throw new IllegalArgumentException("Invalid command " + command);
                commandVector.addElement(WHERE);
                commandVector.addElement(searchCondition);
            }
        } else if (command.regionMatches(true, j, "INSERT", 0, l)) {
            commandVector.addElement(INSERT);
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            if (!command.regionMatches(true, j, "INTO", 0, i - j)) throw new IllegalArgumentException("Invalid insert-command " + command);
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            String tableName = command.substring(j, i);
            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("No table name " + tableName + " found in command " + command);
            commandVector.addElement(table);
            Map<String, Column> columns = table.columns;
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            Vector<Column> columnVector = null;
            if (command.charAt(i) == '(') {
                i++;
                columnVector = new Vector<Column>();
                while (i < command.length()) {
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1 && command.charAt(i) != ')' &&
                           command.charAt(i) != ',') i++;
                    columnVector.addElement(columns.get(command.substring(j, i)));
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) throw new IllegalArgumentException("Invalid insert-command " + command);
                    if (command.charAt(i) == ')') break;
                    if (command.charAt(i++) != ',') throw new IllegalArgumentException("Invalid insert-command " + command);
                }
                if (i++ == command.length()) throw new IllegalArgumentException("Invalid insert-command " + command);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            }
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            l = i - j;
            if (command.regionMatches(true, j, "SELECT", 0, l)) {
                commandVector.addElement(SELECT);
                Vector<Object> selectVector = new Vector<Object>();
                select(selectVector, command, i, false, parameterNumber);
                commandVector.addElement(selectVector);
                if (columnVector != null) commandVector.addElement(columnVector);
            } else {
                if (!command.regionMatches(true, j, "VALUES", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length() || command.charAt(i++) != '(') throw new IllegalArgumentException("Invalid command " + command);
                if (columnVector == null) columnVector = table.columnVector;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                Vector<Object> vector = new Vector<Object>();
                Map<Column, Object> valueTable = new HashMap<Column, Object>();
                Iterator<Column> columnIter = columnVector.iterator();
                while (i < command.length()) {
                    Column column = columnIter.next();
                    ColumnDef columnDef = column.columnDef;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) throw new IllegalArgumentException("Invalid command " + command);
                    String string;
                    if (command.charAt(i) == '\'') {
                        i = literalString(vector, command, i);
                        string = (String)vector.firstElement();
                        vector.removeAllElements();
                    } else {
                        j = i;
                        while (i < command.length() &&
                               whites.indexOf(command.charAt(i)) == -1 &&
                               command.charAt(i) != ')' && command.charAt(i) != ',') i++;
                        string = command.substring(j, i);
                        if (string.equals("?")) {
                            valueTable.put(column, new Integer(++parameterNumber[0]));
                            string = null;
                        }
                    }
                    if (string != null) {
                        int len = -1, pad = 0;
                        switch (columnDef.type) {
                        case Types.BIT:
                        case Types.TINYINT:
                            dout.writeByte(Integer.parseInt(string));
                            break;
                        case Types.SMALLINT:
                            dout.writeShort(Integer.parseInt(string));
                            break;
                        case Types.INTEGER:
                            dout.writeInt(Integer.parseInt(string));
                            break;
                        case Types.BIGINT:
                            dout.writeLong(Long.parseLong(string));
                            break;
                        case Types.REAL:
                        case Types.FLOAT:
                            dout.writeFloat(Float.valueOf(string).floatValue());
                            break;
                        case Types.DOUBLE:
                            dout.writeDouble(Double.valueOf(string).doubleValue());
                            break;
                        case Types.NUMERIC:
                        case Types.DECIMAL: {
                            byte b[] = new BigDecimal(new BigInteger(string), columnDef.scale).toBigInteger().toByteArray();
                            dout.writeInt(b.length);
                            dout.write(b);
                            break;
                        }
                        case Types.CHAR:
                        case Types.VARCHAR:
                            if (string.length() > columnDef.length) {
                                dout.writeChars(string.substring(0, columnDef.length));
                                len = columnDef.length;
                            } else {
                                dout.writeChars(string);
                                len = string.length();
                            }
                            pad = (columnDef.length - len) * 2;
                            break;
                        case Types.DATE:
                            dout.writeLong(java.sql.Date.valueOf(string).getTime());
                            break;
                        case Types.TIME:
                            dout.writeLong(java.sql.Time.valueOf(string).getTime());
                            break;
                        case Types.TIMESTAMP: {
                            Timestamp t = Timestamp.valueOf(string);
                            dout.writeLong(t.getTime());
                            dout.writeInt(t.getNanos());
                            break;
                        }
                        case Types.BINARY:
                        case Types.VARBINARY:
                            if (string.length() > columnDef.length) {
                                dout.writeBytes(string.substring(0, columnDef.length));
                                len = columnDef.length;
                            } else {
                                dout.writeBytes(string);
                                len = string.length();
                            }
                            pad = columnDef.length - len;
                            break;
                        case Types.LONGVARCHAR:
                        case Types.LONGVARBINARY:
                            if (columnDef.scale > 0) {
                                if (string.length() > columnDef.scale) {
                                    dout.writeChars(string.substring(0, columnDef.scale));
                                    len = columnDef.scale;
                                } else {
                                    dout.writeChars(string);
                                    len = string.length();
                                }
                                pad = (columnDef.scale - len) * 2;
                            }
                            break;
                        }
                        if (pad > 0) bout.write(new byte[pad]);
                        Object a[] = new Object[2];
                        a[0] = new Integer(len);
                        a[1] = bout.toByteArray();
                        bout.reset();
                        valueTable.put(column, a);
                    } while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) throw new IllegalArgumentException("Invalid command " + command);
                    if (command.charAt(i) == ')') break;
                    if (command.charAt(i++) != ',') throw new IllegalArgumentException("Invalid command " + command);
                }
                columnIter = table.columnVector.iterator();
                while (columnIter.hasNext()) {
                    Column column = columnIter.next();
                    ColumnDef columnDef = column.columnDef;
                    if (!valueTable.containsKey(column)) {
                        Object a[] = new Object[2];
                        switch (columnDef.type) {
                        case Types.CHAR:
                        case Types.VARCHAR:
                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARCHAR:
                        case Types.LONGVARBINARY:
                            a[0] = new Integer(0);
                            break;
                        default:
                            a[0] = new Integer(-1);
                            break;
                        }
                        a[1] = new byte[fieldLength(columnDef, false)];
                        valueTable.put(column, a);
                    }
                }
                commandVector.addElement(valueTable);
            }
        } else if (command.regionMatches(true, j, "DELETE", 0, l)) {
            commandVector.addElement(DELETE);
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            if (!command.regionMatches(true, j, "FROM", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            String tableName = command.substring(j, i);
            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("Invalid command " + command);
            commandVector.addElement(table);
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            if (i > j) {
                if (!command.regionMatches(true, j, "WHERE", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
                Map<String, Table> selectTables = new HashMap<String, Table>();
                selectTables.put(tableName, table);
                Vector<Object> searchCondition = new Vector<Object>();
                i = searchCondition(searchCondition, command, i, false, selectTables, parameterNumber);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i < command.length()) throw new IllegalArgumentException("Invalid command " + command);
                commandVector.addElement(WHERE);
                commandVector.addElement(searchCondition);
            }
        } else if (command.regionMatches(true, j, "CREATE", 0, l)) {
            commandVector.addElement(CREATE);
            j = i;
            while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
            l = i - j;
            boolean unique;
            if (command.regionMatches(true, j, "UNIQUE", 0, l)) {
                commandVector.addElement(UNIQUE);
                unique = true;
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                l = i - j;
            } else unique = false;
            while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
            if (command.regionMatches(true, j, "INDEX", 0, l)) {
                commandVector.addElement(INDEX);
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                commandVector.addElement(command.substring(j, i));
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                if (!command.regionMatches(true, j, "ON", 0, i - j)) throw new IllegalArgumentException("Invalid command " + command);
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                String tableName = command.substring(j, i);
                Table table = tables.get(tableName);
                if (table == null) throw new IllegalArgumentException("No table name " + tableName + " found in command " + command);
                Map<String, Column> columns = table.columns;
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length() || command.charAt(i++) != '(') throw new IllegalArgumentException("Invalid command " + command);
                Vector<ColumnDef> columnDefs = new Vector<ColumnDef>();
                Vector<String> columnNames = new Vector<String>();
                Map<Column, ColumnDef> columnTable = new HashMap<Column, ColumnDef>();
                while (i < command.length()) {
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1 && command.charAt(i) != ')' &&
                           command.charAt(i) != ',') i++;
                    String columnName = command.substring(j, i);
                    columnNames.addElement(columnName);
                    Column column = columns.get(columnName);
                    ColumnDef columnDef = column.columnDef;
                    columnDefs.addElement(columnDef);
                    columnTable.put(column, columnDef);
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) throw new IllegalArgumentException("Invalid command " + command);
                    if (command.charAt(i) == ')') break;
                    if (command.charAt(i++) != ',') throw new IllegalArgumentException("Invalid command " + command);
                }
                commandVector.addElement(tableName);
                commandVector.addElement(columnDefs);
                commandVector.addElement(columnNames);
                commandVector.addElement(columnTable);
            } else if (unique) throw new IllegalArgumentException("Invalid command " + command);
            else if (command.regionMatches(true, j, "TABLE", 0, l)) {
                commandVector.addElement(TABLE);
                j = i;
                while (i < command.length() && whites.indexOf(command.charAt(i)) == -1) i++;
                commandVector.addElement(command.substring(j, i));
                while (i < command.length() && whites.indexOf(command.charAt(i)) != -1) i++;
                if (i == command.length() || command.charAt(i++) != '(') throw new IllegalArgumentException("Invalid command " + command);
                int offset = 0;
                Map<String, Column> columns = new HashMap<String, Column>();
                Vector<Column> columnVector = new Vector<Column>();
                Vector<String> columnNames = new Vector<String>();
                while (i < command.length()) {
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1) i++;
                    String columnName = command.substring(j, i);
                    columnNames.addElement(columnName);
                    ColumnDef columnDef = new ColumnDef();
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    boolean pno = true;
                    j = i;
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) == -1 &&
                           (pno = command.charAt(i) != '(') && command.charAt(i) != ')' &&
                           command.charAt(i) != ',') i++;
                    columnDef.offset = offset;
                    columnDef.length = -1;
                    columnDef.scale = -1;
                    columnDef.columnIndex = columnVector.size();
                    l = i - j;
                    if (!pno) {
                        int k = command.indexOf(')', i);
                        if (k == -1) throw new IllegalArgumentException("Invalid command " + command);
                        String s = command.substring(i + 1, k);
                        i = k + 1;
                        k = s.indexOf(',');
                        if (k != -1) {
                            columnDef.length = Integer.parseInt(s.substring(0, k));
                            columnDef.scale = Integer.parseInt(s.substring(k + 1));
                        } else columnDef.length = Integer.parseInt(s);
                    }
                    if (command.regionMatches(true, j, "CHARACTER", 0, l)) {
                        columnDef.type = Types.CHAR;
                        if (columnDef.length == -1) throw new IllegalArgumentException("Invalid command " + command);
                        offset += 4 + columnDef.length * 2;
                    } else if (command.regionMatches(true, j, "VARCHARACTER", 0, l)) {
                        columnDef.type = Types.VARCHAR;
                        if (columnDef.length == -1) throw new IllegalArgumentException("Invalid command " + command);
                        offset += 4 + columnDef.length * 2 + 4;
                    } else if (command.regionMatches(true, j, "LONGVARCHAR", 0, l)) {
                        columnDef.type = Types.LONGVARCHAR;
                        offset += 4 + pathLength * 2 + 4;
                    } else if (command.regionMatches(true, j, "NUMERIC", 0, l)) {
                        columnDef.type = Types.NUMERIC;
                        if (columnDef.length == -1) throw new IllegalArgumentException("Invalid command " + command);
                        offset += 4 + columnDef.length;
                    } else if (command.regionMatches(true, j, "DECIMAL", 0, l)) {
                        columnDef.type = Types.DECIMAL;
                        if (columnDef.length == -1) throw new IllegalArgumentException("Invalid command " + command);
                        offset += 4 + columnDef.length;
                    } else if (command.regionMatches(true, j, "BIT", 0, l)) {
                        columnDef.type = Types.BIT;
                        offset++;
                    } else if (command.regionMatches(true, j, "TINYINT", 0, l)) {
                        columnDef.type = Types.TINYINT;
                        offset++;
                    } else if (command.regionMatches(true, j, "SMALLINT", 0, l)) {
                        columnDef.type = Types.SMALLINT;
                        offset += 2;
                    } else if (command.regionMatches(true, j, "INTEGER", 0, l)) {
                        columnDef.type = Types.INTEGER;
                        offset += 4;
                    } else if (command.regionMatches(true, j, "BIGINT", 0, l)) {
                        columnDef.type = Types.BIGINT;
                        offset += 8;
                    } else if (command.regionMatches(true, j, "REAL", 0, l)) {
                        columnDef.type = Types.REAL;
                        offset += 4;
                    } else if (command.regionMatches(true, j, "FLOAT", 0, l)) {
                        columnDef.type = Types.FLOAT;
                        offset += 4;
                    } else if (command.regionMatches(true, j, "DOUBLE", 0, l)) {
                        columnDef.type = Types.DOUBLE;
                        offset += 8;
                    } else if (command.regionMatches(true, j, "BINARY", 0, l)) {
                        columnDef.type = Types.BINARY;
                        if (columnDef.length == -1) throw new IllegalArgumentException("Invalid command " + command);
                        offset += 4 + columnDef.length;
                    } else if (command.regionMatches(true, j, "VARBINARY", 0, l)) {
                        columnDef.type = Types.VARBINARY;
                        if (columnDef.length == -1) throw new IllegalArgumentException("Invalid command " + command);
                        offset += 4 + columnDef.length + 4;
                    } else if (command.regionMatches(true, j, "LONGVARBINARY", 0, l)) {
                        columnDef.type = Types.LONGVARBINARY;
                        offset += 4 + pathLength * 2 + 4;
                    } else if (command.regionMatches(true, j, "DATE", 0, l)) {
                        columnDef.type = Types.DATE;
                        offset += 8;
                    } else if (command.regionMatches(true, j, "TIME", 0, l)) {
                        columnDef.type = Types.TIME;
                        offset += 8;
                    } else if (command.regionMatches(true, j, "TIMESTAMP", 0, l)) {
                        columnDef.type = Types.TIMESTAMP;
                        offset += 12;
                    }
                    if (columnDef.length == -1) columnDef.length = fieldLength(columnDef, false);
                    Column column = new Column();
                    column.columnName = columnName;
                    column.columnDef = columnDef;
                    columns.put(columnName, column);
                    columnVector.addElement(column);
                    while (i < command.length() &&
                           whites.indexOf(command.charAt(i)) != -1) i++;
                    if (i == command.length()) throw new IllegalArgumentException("Invalid command " + command);
                    if (command.charAt(i) == ')') break;
                    if (command.charAt(i++) != ',') throw new IllegalArgumentException("Invalid command " + command);
                }
                commandVector.addElement(columns);
                commandVector.addElement(columnVector);
                commandVector.addElement(columnNames);
                commandVector.addElement(new Integer(offset));
            }
        } else throw new IllegalArgumentException("Invalid command " + command);
        return commandVector;
    }

    boolean deleteKeys(Map<String, Index> indices, int dataRef, boolean needsBlockFile)
        throws IOException {
        boolean ok = true;
        IndexItem xitem = new IndexItem();
        Iterator<Index> indexIter = indices.values().iterator();
        while (indexIter.hasNext()) {
            Index index = indexIter.next();
            IndexFile indexFile = index.indexFile;
            IndexContext xcontext = indexFile.openIndex(true, needsBlockFile ? blockFile : null);
            try {
                xitem.dataRef = dataRef;
                xitem.key = indexFile.buildKey(xcontext, indexFile.dataItems, dataRef);
                if (!indexFile.deleteItem(xcontext, xitem)) ok = false;
                indexFile.update(xcontext);
            } finally {
                indexFile.close(xcontext);
            }
        }
        return ok;
    }

    boolean addKeys(Map<String, Index> indexTable, int dataRef, boolean needsBlockFile)
        throws IOException {
        IndexItem xitem = new IndexItem();
        Iterator<Index> indexIter = indexTable.values().iterator();
        while (indexIter.hasNext()) {
            Index index = indexIter.next();
            IndexFile indexFile = index.indexFile;
            IndexContext xcontext = indexFile.openIndex(true, needsBlockFile ? blockFile : null);
            try {
                xitem.dataRef = dataRef;
                xitem.key = indexFile.buildKey(xcontext, indexFile.dataItems, dataRef);
                if (!indexFile.addItem(xcontext, xitem)) {
                    deleteKeys(indexTable, dataRef, needsBlockFile);
                    indexFile.update(xcontext);
                    return false;
                }
                indexFile.update(xcontext);
            } finally {
                indexFile.close(xcontext);
            }
        }
        return true;
    }

    @SuppressWarnings("fallthrough")
    public Vector<?> perform(Vector<?> commandVector, Map<Object, Object> values, boolean readOnly)
        throws IllegalArgumentException, InterruptedException, IOException {
        Object token = commandVector.firstElement();
        if (token == SELECT) return commandVector;
        if (readOnly) throw new IOException("Connection is allowed to read only");
        if (token == UPDATE) {
            Table table = (Table)commandVector.elementAt(1);
            Map<Column, Vector<?>> setTable = (Map<Column, Vector<?>>)commandVector.elementAt(2);
            Map<String, Index> indexTable = (Map<String, Index>)commandVector.elementAt(3);
            int n = commandVector.indexOf(WHERE, 4);
            Vector<?> searchCondition;
            if (n != -1) searchCondition = (Vector<?>)commandVector.elementAt(n + 1);
            else searchCondition = null;
            DataFile dataFile = table.dataFile;
            Map<String, Column> columns = table.columns;
            Vector<Column> columnVector = table.columnVector;
            Map<Object, Object> context = new HashMap<Object, Object>();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            Vector<Object> resultVector = new Vector<Object>();
            resultVector.addElement(UPDATE);
            Vector<Integer> refs = new Vector<Integer>();
            resultVector.addElement(refs);
            IndexItem xitem;
            boolean direct;
            if (searchCondition != null &&
                (n = directRead(table, null, searchCondition, context, values)) != -1) {
                if (n == 0) return resultVector;
                xitem = (IndexItem)((Object[])context.get(table))[0];
                direct = true;
            } else {
                xitem = new IndexItem();
                xitem.dataRef = 0;
                direct = false;
            }
            for (;;) {
                rwControl.startWrite();
                try {
                    IndexContext xcontext = table.index.indexFile.openIndex(true, table.needsBlockFile ? blockFile : null);
                    try {
                        if (!direct) {
                            if (++xitem.dataRef >= dataFile.fileLen()) break;
                            dataFile.seekStatus(xcontext.rawDataFile, xitem.dataRef);
                            if (xcontext.rawDataFile.readInt() != 0) continue;
                            readContents(xcontext, table, values, xitem.dataRef);
                            if (searchCondition != null &&
                                !satisfies(searchCondition, values)) continue;
                        }
                        refs.addElement(new Integer(xitem.dataRef));
                        if (!deleteKeys(indexTable, xitem.dataRef, table.needsBlockFile))
                            throw new IOException("Failed to delete keys");
                        Iterator<Map.Entry<Column, Vector<?>>> setTableIter = setTable.entrySet().iterator();
                        while (setTableIter.hasNext()) {
                            Map.Entry<Column, Vector<?>> entry = setTableIter.next();
                            Column column = entry.getKey();
                            Object value = evaluate(entry.getValue(), values);
                            if (value instanceof InputStream) {
                                writeLargeObject((InputStream)value,
                                                 table.tableName + "_" + column.columnName + "_" + xitem.dataRef);
                                value = "";
                            }
                            ColumnDef columnDef = column.columnDef;
                            int length = writeContent(dout, columnDef, value);
                            byte b[] = bout.toByteArray();
                            bout.reset();
                            writeData(xcontext, dataFile, xitem.dataRef, b, length, columnDef);
                        }
                        if (!addKeys(indexTable, xitem.dataRef, table.needsBlockFile))
                            throw new IllegalArgumentException(duplKeys);
                    } finally {
                        table.index.indexFile.close(xcontext);
                    }
                } finally {
                    rwControl.stopWrite();
                }
                if (direct && directRead(table, null, searchCondition, context, values) != 1) break;
            }
            return resultVector;
        }
        if (token == INSERT) {
            Table table = (Table)commandVector.elementAt(1);
            if (table == null) throw new IllegalArgumentException("No table found");
            DataFile dataFile = table.dataFile;
            Map<String, Column> columns = table.columns;
            Map<String, Index> indices = table.indices;
            Vector<Object> resultVector = new Vector<Object>();
            resultVector.addElement(INSERT);
            Vector<Integer> refs = new Vector<Integer>();
            resultVector.addElement(refs);
            if (commandVector.elementAt(2) == SELECT) {
                Vector<?> selectVector = (Vector<?>)commandVector.elementAt(3);
                Vector<?> columnVector;
                if (commandVector.size() > 4)
                    columnVector = (Vector<?>)commandVector.elementAt(4);
                else columnVector = table.columnVector;
                Map<Object, Object> context = new HashMap<Object, Object>();
                Vector<?> row;
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                while ((row = move(selectVector, context, values, 1)) != null) {
                    rwControl.startWrite();
                    try {
                        IndexContext xcontext = table.index.indexFile.openIndex(true, table.needsBlockFile ? blockFile : null);
                        try {
                            int dataRef = dataFile.newData(xcontext.rawDataFile);
                            refs.addElement(new Integer(dataRef));
                            Iterator<?> columnIter = columnVector.iterator();
                            Iterator<?> rowIter = row.iterator();
                            while (columnIter.hasNext()) {
                                ColumnDef columnDef = ((Column)columnIter.next()).columnDef;
                                int length = writeContent(dout, columnDef, rowIter.next());
                                byte b[] = bout.toByteArray();
                                bout.reset();
                                writeData(xcontext, dataFile, dataRef, b, length, columnDef);
                            }
                            if (!addKeys(indices, dataRef, table.needsBlockFile)) {
                                dataFile.deleteData(xcontext.rawDataFile, dataRef);
                                throw new IllegalArgumentException(duplKeys);
                            }
                        } finally {
                            table.index.indexFile.close(xcontext);
                        }
                    } finally {
                        rwControl.stopWrite();
                    }
                }
                return resultVector;
            }
            HashMap valueTable = (HashMap)commandVector.elementAt(2);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            rwControl.startWrite();
            try {
                IndexContext xcontext = table.index.indexFile.openIndex(true, table.needsBlockFile ? blockFile : null);
                try {
                    int dataRef = dataFile.newData(xcontext.rawDataFile);
                    refs.addElement(new Integer(dataRef));
                    Iterator valueIter = valueTable.entrySet().iterator();
                    while (valueIter.hasNext()) {
                        Map.Entry entry = (Map.Entry)valueIter.next();
                        Column column = (Column)entry.getKey();
                        ColumnDef columnDef = column.columnDef;
                        Object value = entry.getValue();
                        int length;
                        byte b[];
                        if (value instanceof Integer) {
                            value = values.get(value);
                            if (value instanceof InputStream) {
                                writeLargeObject((InputStream)value,
                                                 table.tableName + "_" + column.columnName + "_" + dataRef);
                                value = "";
                            }
                            length = writeContent(dout, columnDef, value);
                            b = bout.toByteArray();
                            bout.reset();
                        } else {
                            length = ((Integer)((Object[])value)[0]).intValue();
                            b = (byte[])((Object[])value)[1];
                        }
                        writeData(xcontext, dataFile, dataRef, b, length, columnDef);
                    }
                    if (!addKeys(indices, dataRef, table.needsBlockFile)) {
                        dataFile.deleteData(xcontext.rawDataFile, dataRef);
                        throw new IllegalArgumentException(duplKeys);
                    }
                } finally {
                    table.index.indexFile.close(xcontext);
                }
            } finally {
                rwControl.stopWrite();
            }
            return resultVector;
        }
        if (token == DELETE) {
            Table table = (Table)commandVector.elementAt(1);
            int n = commandVector.indexOf(WHERE, 2);
            Vector<?> searchCondition;
            if (n != -1) searchCondition = (Vector<?>)commandVector.elementAt(n + 1);
            else searchCondition = null;
            DataFile dataFile = table.dataFile;
            Map<String, Column> columns = table.columns;
            Map<String, Index> indices = table.indices;
            Vector<Column> columnVector = table.columnVector;
            Map<Object, Object> context = new HashMap<Object, Object>();
            Vector<Object> resultVector = new Vector<Object>();
            resultVector.addElement(DELETE);
            Vector<Integer> refs = new Vector<Integer>();
            resultVector.addElement(refs);
            IndexItem xitem;
            boolean direct;
            if (searchCondition != null &&
                (n = directRead(table, null, searchCondition, context, values)) != -1) {
                if (n == 0) return resultVector;
                xitem = (IndexItem)((Object[])context.get(table))[0];
                direct = true;
            } else {
                xitem = new IndexItem();
                xitem.dataRef = 0;
                direct = false;
            }
            for (;;) {
                rwControl.startWrite();
                try {
                    IndexContext xcontext = table.index.indexFile.openIndex(true, table.needsBlockFile ? blockFile : null);
                    try {
                        if (!direct) {
                            if (++xitem.dataRef >= dataFile.fileLen()) break;
                            dataFile.seekStatus(xcontext.rawDataFile, xitem.dataRef);
                            if (xcontext.rawDataFile.readInt() != 0) continue;
                            readContents(xcontext, table, values, xitem.dataRef);
                            if (searchCondition != null &&
                                !satisfies(searchCondition, values)) continue;
                        }
                        refs.addElement(new Integer(xitem.dataRef));
                        Iterator<Column> columnIter = columnVector.iterator();
                        while (columnIter.hasNext()) {
                            Column column = columnIter.next();
                            ColumnDef columnDef = column.columnDef;
                            Object value = values.get(column);
                            switch (columnDef.type) {
                            case Types.LONGVARCHAR:
                            case Types.LONGVARBINARY:
                                new File(dir, (String)value).delete();
                                break;
                            }
                        }
                        if (!deleteKeys(indices, xitem.dataRef, table.needsBlockFile))
                            throw new IOException("Failed to delete keys");
                        dataFile.deleteData(xcontext.rawDataFile, xitem.dataRef);
                    } finally {
                        table.index.indexFile.close(xcontext);
                    }
                } finally {
                    rwControl.stopWrite();
                }
                if (direct && directRead(table, null, searchCondition, context, values) != 1) break;
            }
            return resultVector;
        }
        if (token == CREATE) {
            int n;
            boolean unique;
            if (commandVector.elementAt(1) == UNIQUE) {
                unique = true;
                n = 2;
            } else {
                unique = false;
                n = 1;
            }
            if (commandVector.elementAt(n) == INDEX) {
                String indexName = (String)commandVector.elementAt(n + 1);
                String tableName = (String)commandVector.elementAt(n + 2);
                Vector<ColumnDef> columnDefs = (Vector<ColumnDef>)commandVector.elementAt(n + 3);
                Vector<String> columnNames = (Vector<String>)commandVector.elementAt(n + 4);
                Map<Column, ColumnDef> columnTable = (Map<Column, ColumnDef>)commandVector.elementAt(n + 5);
                Table table = tables.get(tableName);
                Map<String, Column> columns = table.columns;
                Map<String, Index> indices = table.indices;
                int numberOfKeyFields = columnDefs.size();
                DataItem dataItems[] = new DataItem[numberOfKeyFields];
                boolean hasLength = false, needsBlockFile = false;
                for (int keyFieldIndex = 0; keyFieldIndex < numberOfKeyFields; keyFieldIndex++) {
                    ColumnDef columnDef = columnDefs.elementAt(keyFieldIndex);
                    dataItems[keyFieldIndex] = new DataItem(columnDef.offset, fieldLength(columnDef, false));
                    switch (columnDef.type) {
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                        if (columnDef.type == Types.VARCHAR ||
                            File.separatorChar == '\\') dataItems[keyFieldIndex].caseType = DataItem.CHAR;
                    case Types.VARBINARY:
                    case Types.LONGVARBINARY:
                        if (keyFieldIndex < numberOfKeyFields - 1) throw new IllegalArgumentException("Variable length key field is not the last");
                        needsBlockFile = true;
                    case Types.CHAR:
                    case Types.BINARY:
                        if (columnDef.type == Types.CHAR) dataItems[keyFieldIndex].caseType = DataItem.CHAR;
                        if (keyFieldIndex == numberOfKeyFields - 1) hasLength = true;
                        else dataItems[keyFieldIndex].offset += 4;
                    }
                }
                Index index = new Index();
                columns.get(columnNames.firstElement()).indexFile = index.indexFile =
                    new IndexFile(tableName + "_" + indexName + ".index", recordManager, dataItems, !unique, hasLength, table.dataFile, needsBlockFile ? blockFile : null);
                index.columnDefs = columnDefs;
                index.columnTable = columnTable;
                indices.put(indexName, index);
                if (table.index == null) table.index = index;
                if (needsBlockFile) table.needsBlockFile = true;
                DataOutputStream indexInfoOut = new DataOutputStream(new FileOutputStream(new File(dir, tableName + "_" + indexName + "_index.info")));
                indexInfoOut.writeInt(0);
                indexInfoOut.writeBoolean(unique);
                indexInfoOut.writeInt(columnNames.size());
                Iterator columnNameIter = columnNames.iterator();
                while (columnNameIter.hasNext()) indexInfoOut.writeUTF((String)columnNameIter.next());
                indexInfoOut.close();
                DataOutputStream indicesInfoOut = new DataOutputStream(new FileOutputStream(new File(dir, tableName + "_indices.info")));
                indicesInfoOut.writeInt(indices.size());
                Iterator indexIter = indices.keySet().iterator();
                while (indexIter.hasNext()) indicesInfoOut.writeUTF((String)indexIter.next());
                indicesInfoOut.close();
            } else if (!unique && commandVector.elementAt(1) == TABLE) {
                String tableName = (String)commandVector.elementAt(2);
                Map<String, Column> columns = (Map<String, Column>)commandVector.elementAt(3);
                Vector<Column> columnVector = (Vector<Column>)commandVector.elementAt(4);
                Vector<String> columnNames = (Vector<String>)commandVector.elementAt(5);
                int recordLength = ((Integer)commandVector.elementAt(6)).intValue();
                Table table = new Table();
                File file = new File(dir, tableName + ".data");
                table.dataFile = new DataFile(file, recordLength);
                table.tableName = tableName;
                table.columns = columns;
                table.indices = new HashMap<String, Index>();
                table.columnVector = columnVector;
                tables.put(tableName, table);
                DataOutputStream tableInfoOut = new DataOutputStream(new FileOutputStream(new File(dir, tableName + ".info")));
                tableInfoOut.writeInt(columnNames.size());
                Iterator columnNameIter = columnNames.iterator();
                while (columnNameIter.hasNext()) {
                    String columnName = (String)columnNameIter.next();
                    tableInfoOut.writeUTF(columnName);
                    ColumnDef columnDef = columns.get(columnName).columnDef;
                    tableInfoOut.writeInt(columnDef.offset);
                    tableInfoOut.writeInt(columnDef.length);
                    tableInfoOut.writeInt(columnDef.scale);
                    tableInfoOut.writeInt(columnDef.type);
                }
                tableInfoOut.close();
                DataOutputStream tablesInfoOut = new DataOutputStream(new FileOutputStream(new File(dir, "tables.info")));
                tablesInfoOut.writeInt(tables.size());
                Iterator tableIter = tables.keySet().iterator();
                while (tableIter.hasNext()) tablesInfoOut.writeUTF((String)tableIter.next());
                tablesInfoOut.close();
            } else throw new IllegalArgumentException("Invalid command");
        }
        return null;
    }

    public Vector<?> perform(String command, Map<Object, Object> values, boolean readOnly)
        throws IllegalArgumentException, InterruptedException, IOException {
        return perform(prepare(command), values, readOnly);
    }

    public Vector<?> perform(Object statement, Map<Object, Object> values, boolean readOnly)
        throws IllegalArgumentException, InterruptedException, IOException {
        if (statement instanceof Vector) return perform((Vector<?>)statement, values, readOnly);
        return perform((String)statement, values, readOnly);
    }

    public static void main(String argv[])
        throws IllegalArgumentException, IOException, SQLException {
        DatabaseConnection c = new DatabaseConnection("test");
        Statement s = c.createStatement();
        s.execute("CREATE TABLE TEST (FIELD1 CHAR(25), FIELD2 INTEGER)");
        s.execute("CREATE INDEX TEST ON TEST (FIELD1, FIELD2)");
        s.execute("INSERT INTO TEST (FIELD1, FIELD2) VALUES ('tset', 123)");
        s.execute("INSERT INTO TEST (FIELD1, FIELD2) VALUES ('tset', 123)");
        s.execute("UPDATE TEST SET FIELD1='test'\nWHERE FIELD1='tset'");
        //      s.execute("INSERT INTO TEST (FIELD1, FIELD2) VALUES ('reverse', 321)");
        PreparedStatement p = c.prepareStatement("SELECT * FROM TEST\nWHERE #=?");
        ResultSet r = s.executeQuery("SELECT ALL #, * FROM TEST");
        do {
            System.out.println("reference="+r.getInt(1));
            System.out.println("field1="+r.getString(2));
            System.out.println("field2="+r.getInt(3));
        } while (r.next());
        //      s.execute("DELETE FROM TEST\nWHERE #=1");
        //      s.execute("DELETE FROM TEST\nWHERE FIELD1='test'");
    }

}

