/*
 * This file is part of seadams Utils.
 *
 * Copyright (c) 2008-2011 Sam Adams <seadams@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.seadams.util.io;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

public class ClassPath {

    private final Class<?> clazz;

    public ClassPath(final Class<?> c) {
        this.clazz = c;
    }

    public URL getClassUrl() {
        return clazz.getResource(clazz.getSimpleName() + ".class");
    }

    public boolean inJar() {
        URL u = getClassUrl();
        return u.toString().startsWith("jar:file:/");
    }

    public File getJarFile() {
        URL u = getClassUrl();
        String s = u.toString();
        if (!s.startsWith("jar:file:/")) {
            return null;
        }
        int i = s.toLowerCase().indexOf(".jar!");
        String sf = s.substring(10, i);

        // Decode url-encoded characters - %20 = <space> etc...
        try {
            sf = URLDecoder.decode(sf, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            // Decode manually
            int indx;
            while ((indx = sf.indexOf('%')) > -1) {
                if (sf.length() > indx + 1) {
                    String lccfp = sf.toLowerCase();
                    char ch = lccfp.charAt(indx + 1);
                    char cl = lccfp.charAt(indx + 2);
                    int ih = "0123456789abcdef".indexOf(ch);
                    int il = "0123456789abcdef".indexOf(cl);
                    if (ih > -1 && il > -1) {
                        sf = sf.substring(0, indx)
                                + ((char) (16 * ih + il))
                                + sf.substring(indx + 3);
                    }
                }
            }
        }

        return new File(sf);
    }


    public static void main(String[] args) {
        Class<?> clazz = ArrayList.class;
        ClassPath cp = new ClassPath(clazz);
        System.out.println(cp.getClassUrl());
        System.out.println(cp.inJar());
        System.out.println(cp.getJarFile().getAbsolutePath());
    }

}

