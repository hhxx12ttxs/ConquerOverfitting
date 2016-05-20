/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 huliqing, huliqing.cn@gmail.com
 *
 * This file is part of QBlog.
 * QBlog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QBlog is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with QBlog.  If not, see <http://www.gnu.org/licenses/>.
 *
 * ?????QBlog?????
 * ?????????????????????????????.
 * QBlog????????????????????????????????
 * ????????????????????LGPL3????????????.
 * ??LGPL????????COPYING?COPYING.LESSER???
 * ????QBlog????????LGPL??????
 * ??????????? http://www.gnu.org/licenses/ ???
 *
 * - Author: Huliqing
 * - Contact: huliqing.cn@gmail.com
 * - License: GNU Lesser General Public License (LGPL)
 * - Blog and source code availability: http://www.huliqing.name/
 */

package name.huliqing.qblog.backup.converter;

import name.huliqing.qblog.backup.TypeConverter;

/**
 *
 * @author huliqing
 */
public class DoubleConverter implements TypeConverter{

    public Object getAsObject(String strValue) {
        if (strValue == null || "".equals(strValue))
            return null;
        Double result = null;
        try {
            result = Double.parseDouble(strValue);
        } catch (NumberFormatException nfe) {
            return null;
        }
        return result;
    }

    public String getAsString(Object objValue) {
        if (objValue == null)
            return "";
        return objValue.toString();
    }

}

