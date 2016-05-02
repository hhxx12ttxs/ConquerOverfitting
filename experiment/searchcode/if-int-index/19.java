/**
 * Copyright (C) 2008 Mathieu Carbou <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycila.testing.plugin.db;

import com.mycila.testing.plugin.db.api.SqlData;
import com.mycila.testing.plugin.db.api.SqlRow;

import java.util.AbstractList;
import java.util.List;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class SqlRowImpl implements SqlRow {

    private final SqlResultsImpl results;
    private final int index;
    private final List<SqlData> columns;
    private String rowStr;

    SqlRowImpl(SqlResultsImpl results, int index) {
        this.results = results;
        this.index = index;
        this.columns = new AbstractList<SqlData>() {
            @Override
            public SqlData get(int index) {
                return column(index);
            }

            @Override
            public int size() {
                return columnCount();
            }
        };
    }

    public int index() {
        return index;
    }

    public int columnCount() {
        return results.columnCount();
    }

    public SqlData column(int index) {
        return results.data(this.index, index);
    }

    public SqlData column(String name) {
        return results.column(name).row(index);
    }

    public List<SqlData> columns() {
        return columns;
    }

    @Override
    public String toString() {
        if (rowStr == null) {
            StringBuilder sb = results.addSeparator(results.addHeader(results.addSeparator(new StringBuilder())));
            results.addEntry(sb.append("|"), index + 1, 5).append("|");
            for (SqlData data : columns) {
                results.addEntry(sb, data, data.header().displaySize()).append("|");
            }
            rowStr = results.addSeparator(sb.append("\n")).toString();
        }
        return rowStr;
    }

}

