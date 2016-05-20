/*
 * Copyright 2009-2010 Nanjing RedOrange ltd (http://www.red-orange.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package redora.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static util class for handling SQL strings. Typically a parameter (?) in the SQL statement
 * is replaced with the given value.
 * Besides escaping the ' character, also
 *
 * @author Nanjing RedOrange (www.red-orange.cn)
 */
public class SQLParameter {

    final static Pattern PARAM = Pattern.compile("\\?");
    public final static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
    public final static SimpleDateFormat yyyyMMddHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @NotNull
    public static String prepareNull(@NotNull String sql) {
        return replaceFirst(sql, "null", 0);
    }

    @NotNull
    public static String prepareDirty(@NotNull String sql, @NotNull String param) {
        //If the server is set to ANSI_QUOTES SQL mode (or just ANSI mode) you should be safe.
        //https://www.owasp.org/index.php/SQL_Injection_Prevention_Cheat_Sheet#MySQL_Escaping
        return prepare(sql, param.replace("'", "''").replace("\\", "\\\\"));
    }

    @NotNull
    public static String prepare(@NotNull String sql, @Nullable Object param) {
        if (param == null) {
            return prepareNull(sql);
        } else if (param instanceof Long) {
            return prepare(sql, ((Long) param).longValue());
        } else if (param instanceof String) {
            return prepareDirty(sql, (String) param);
        } else if (param instanceof Integer) {
            return prepare(sql, ((Integer) param).intValue());
        } else if (param instanceof Date) {
            return prepare(sql, (Date) param);
        } else if (param instanceof Double) {
            return prepare(sql, ((Double) param).doubleValue());
        } else if (param instanceof Boolean) {
            return prepare(sql, ((Boolean) param).booleanValue());
        }
        throw new IllegalArgumentException("Unsupported type " + param.getClass() + " for param " + param);
    }

    @NotNull
    public static String prepare(@NotNull String sql, double param) {
        return replaceFirst(sql, String.valueOf(param), 0);
    }

    @NotNull
    public static String prepare(@NotNull String sql, boolean param) {
        return replaceFirst(sql, param ? "1" : "0", 0);
    }

    @NotNull
    public static String prepareTime(@NotNull String sql, @NotNull Date param) {
        return replaceFirst(sql, "'" + yyyyMMddHHMMSS.format(param) + "'", 0);
    }

    @NotNull
    public static String prepare(@NotNull String sql, @NotNull Date param) {
        return replaceFirst(sql, "'" + yyyyMMdd.format(param) + "'", 0);
    }

    @NotNull
    public static String prepare(@NotNull String sql, @NotNull String param) {
        return replaceFirst(sql, "'" + param.replace("\\$", "\\$") + "'", 0);
    }

    @NotNull
    public static String prepare(@NotNull String sql, long param) {
        return replaceFirst(sql, String.valueOf(param), 0);
    }

    @NotNull
    public static String prepare(@NotNull String sql, int param) {
        return replaceFirst(sql, String.valueOf(param), 0);
    }

    @NotNull
    public static String prepare(@NotNull String sql, Set<Long> param) {
        StringBuilder ids = new StringBuilder();
        char comma = ' ';
        for (Long id : param) {
            ids.append(comma).append(id);
            comma = ',';
        }
        return replaceFirst(sql, ids.toString(), 0);
    }

    @NotNull
    static String replaceFirst(@NotNull String sql, @NotNull String param, int start) {
        Matcher m = PARAM.matcher(sql);
        if (m.find(start)) {
            //avoid to replace ? params within the string ' ' delimiters
            if (sql.substring(0, m.start() + 1).split("'").length % 2 != 0) {
                StringBuffer retVal = new StringBuffer();
                m.appendReplacement(retVal, Matcher.quoteReplacement(param));
                m.appendTail(retVal);
                return retVal.toString();
            } else {
                return replaceFirst(sql, param, m.start() + 1);
            }
        }
        return sql;
    }
}


