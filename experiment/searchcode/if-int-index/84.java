/**
 * Copyright (C) 2010 Mycila <mathieu.carbou@gmail.com>
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

package com.mycila.jdbc.query;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Insert {

    private final Mapper mapper = new Mapper();
    private final Request request = new Request() {
        @Override
        PreparedStatement getStatement() {
            return Insert.this.statement;
        }
    };
    PreparedStatement statement;
    private final String table;
    private Connection connection;
    private final Map<String, Param> inserts = new LinkedHashMap<String, Param>();

    Insert(Connection connection, String table) {
        this.table = table;
        this.connection = connection;
    }

    public int[] executeBatch() throws SqlException {
        return exec();
    }

    public int execute() throws SqlException {
        return executeBatch()[0];
    }

    public Results executeAndReturns(String... columnNames) throws SqlException {
        exec(columnNames);
        return Results.build(this, mapper);
    }

    int[] exec(String... columnNames) {
        try {
            boolean batchMode = statement != null;
            if (!batchMode) {
                buildStatement(columnNames);
                setParameters();
            } else if (!this.inserts.isEmpty()) {
                addBatch();
            }
            statement.setFetchSize(1);
            if (batchMode) {
                return statement.executeBatch();
            } else {
                return new int[]{statement.executeUpdate()};
            }
        } catch (SQLException e) {
            JdbcUtils.closeStatement(statement);
            throw new SqlException(e.getMessage(), e);
        }
    }

    public Insert addBatch() throws SqlException {
        try {
            if (statement == null) {
                buildStatement();
            }
            setParameters();
            statement.addBatch();
            this.inserts.clear();
            return this;
        } catch (SQLException e) {
            JdbcUtils.closeStatement(statement);
            throw new SqlException(e.getMessage(), e);
        }
    }

    void setParameters() {
        int i = 1;
        for (Param param : this.inserts.values())
            param.set(i++);
    }

    void buildStatement(String... columnNames) throws SQLException {
        StringBuilder sb = new StringBuilder("INSERT INTO ").append(table).append(" (");
        for (String column : inserts.keySet()) {
            sb.append(column).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") VALUES (?");
        for (int i = 1; i < inserts.size(); i++)
            sb.append(",?");
        sb.append(")");
        statement = columnNames == null || columnNames.length == 0 ?
            connection.prepareStatement(sb.toString()) :
            connection.prepareStatement(sb.toString(), columnNames);
    }

    public final Insert setNull(String column) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setNull(index);
            }
        });
        return this;
    }

    public final Insert setBoolean(String column, final boolean x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setBoolean(index, x);
            }
        });
        return this;
    }

    public final Insert setByte(String column, final byte x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setByte(index, x);
            }
        });
        return this;
    }

    public final Insert setShort(String column, final short x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setShort(index, x);
            }
        });
        return this;
    }

    public final Insert setInt(String column, final int x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setInt(index, x);
            }
        });
        return this;
    }

    public final Insert setLong(String column, final long x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setLong(index, x);
            }
        });
        return this;
    }

    public final Insert setFloat(String column, final float x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setFloat(index, x);
            }
        });
        return this;
    }

    public final Insert setDouble(String column, final double x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setDouble(index, x);
            }
        });
        return this;
    }

    public final Insert setBigDecimal(String column, final BigDecimal x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setBigDecimal(index, x);
            }
        });
        return this;
    }

    public final Insert setString(String column, final String x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setString(index, x);
            }
        });
        return this;
    }

    public final Insert setBytes(String column, final byte[] x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setBytes(index, x);
            }
        });
        return this;
    }

    public final Insert setDate(String column, final Date x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setDate(index, x);
            }
        });
        return this;
    }

    public final Insert setTime(String column, final Time x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setTime(index, x);
            }
        });
        return this;
    }

    public final Insert setTimestamp(String column, Date time) {
        return setTimestamp(column, time.getTime());
    }

    public final Insert setTimestamp(String column, long time) {
        return setTimestamp(column, new Timestamp(time));
    }

    public final Insert setTimestamp(String column, final Timestamp x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setTimestamp(index, x);
            }
        });
        return this;
    }

    public final Insert setObject(String column, final Object x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setObject(index, x);
            }
        });
        return this;
    }

    public Insert setObjects(String column, final Object[] x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setObjects(index, x);
            }
        });
        return this;
    }

    public final Insert setBlob(String column, final Blob x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setBlob(index, x);
            }
        });
        return this;
    }

    public final Insert setClob(String column, final Clob x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setClob(index, x);
            }
        });
        return this;
    }

    public final Insert setURL(String column, final URL x) {
        inserts.put(column, new Param() {
            @Override
            public void set(int index) {
                request.setURL(index, x);
            }
        });
        return this;
    }

    private static interface Param {
        void set(int index);
    }

}

