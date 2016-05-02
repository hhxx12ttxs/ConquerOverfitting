/*
<<<<<<< HEAD
 * java-gnome, a UI library for writing GTK and GNOME programs from Java!
 *
 * Copyright Â 2006-2010 Operational Dynamics Consulting, Pty Ltd
 *
 * The code in this file, and the program it is a part of, is made available
 * to you by its authors as open source software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License version
 * 2 ("GPL") as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
 *
 * You should have received a copy of the GPL along with this program. If not,
 * see http://www.gnu.org/licenses/. The authors of this program may be
 * contacted through http://java-gnome.sourceforge.net/.
 *
 * Linking this library statically or dynamically with other modules is making
 * a combined work based on this library. Thus, the terms and conditions of
 * the GPL cover the whole combination. As a special exception (the
 * "Claspath Exception"), the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module. An independent
 * module is a module which is not derived from or based on this library. If
 * you modify this library, you may extend the Classpath Exception to your
 * version of the library, but you are not obligated to do so. If you do not
 * wish to do so, delete this exception statement from your version.
 */
package org.gnome.glib;

import org.freedesktop.bindings.Constant;
import org.freedesktop.bindings.Debug;
import org.freedesktop.bindings.Pointer;
import org.gnome.gdk.Pixbuf;
import org.gnome.pango.FontDescription;

/**
 * A generic value that can be passed as a parameter to or returned from a
 * method or function on an underlying entity in the GLib library and those
 * built on it. Value is only used in setting or getting Object properties,
 * and in the TreeModel system.
 * 
 * <p>
 * <b>For use by bindings hackers only.</b><br>
 * As with other classes in <code>org.gnome.glib</code>, this is
 * implementation machinery and should not be needed by anyone developing
 * applications with java-gnome.
 * 
 * <p>
 * Ironically, Values are <i>not</i> actually type safe; if you happen to
 * create one to hold Strings, and then call the getEnum() method on it, your
 * program will explode (this is somewhat to the contrary of the spirit of
 * java-gnome). These are therefore only for use from within strongly typed
 * methods exposing a safe and sane public API. You've been warned.
 * 
 * <p>
 * <i>Complementing the object oriented system supplied by the GLib library is
 * a set of foundation elements, <code>GType</code> and <code>GValue</code>,
 * the latter being defined as "a polymorphic type that can hold values of any
 * other type", which isn't much help, really.</i>
 * 
 * <p>
 * <i>Since instances of Java classes are their own identity, we do not need
 * to directly represent <code>GType</code> and <code>GValue</code> as
 * separate classes. We implement <code>GType</code> as a characteristic that
 * any</i> <code>Value</code> <i>or</i> <code>Object</code> <i>has.</i>
 * 
 * @author Andrew Cowie
 * @since 4.0.0
 */
public class Value extends Pointer
{
    /*
     * The second argument is a hack to create a different overload of
     * <init>(long). What a pain in the ass, but whatever.
     */
    protected Value(long pointer, boolean proxy) {
        super(pointer);
        if (Debug.MEMORY_MANAGEMENT) {
            System.err.println("Value.<init>(long)\t\t" + this.toString());
            System.err.flush();
        }
    }

    /**
     * By design, we are the owner of all the GValues because we allocate the
     * memory for them in (for example) the native implementation of
     * {@link GValue#createValue(int)}. So, call <code>g_slice_free()</code>
     * on our pointer to free that memory.
     */
    protected void release() {
        if (Debug.MEMORY_MANAGEMENT) {
            System.err.println("Value.release()\t\t\t" + this.toString());
        }
        GValue.free(this);
    }

    /**
     * Create an empty Value without initializing it's type. For use in
     * methods like TreeModel's
     * {@link org.gnome.gtk.TreeModel#getValue(org.gnome.gtk.TreeIter, org.gnome.gtk.DataColumnString)
     * getValue()} family, which use a blank Value internally.
     * 
     * Not public API!
     */
    protected Value() {
        this(GValue.createValue(), true);
    }

    /**
     * Create a Value containing a String. Not public API!
     */
    protected Value(String value) {
        this(GValue.createValue(value), true);
    }

    protected String getString() {
        return GValue.getString(this);
    }

    /**
     * Create a Value containing an <code>int</code>. Not public API!
     */
    protected Value(int value) {
        this(GValue.createValue(value), true);
    }

    protected int getInteger() {
        return GValue.getInteger(this);
    }

    /**
     * Create a Value containing a <code>boolean</code>. Not public API!
     */
    protected Value(boolean value) {
        this(GValue.createValue(value), true);
    }

    protected boolean getBoolean() {
        return GValue.getBoolean(this);
    }

    /**
     * Create a Value containing a <code>float</code>. Not public API!
     */
    protected Value(float value) {
        this(GValue.createValue(value), true);
    }

    protected Value(double value) {
        this(GValue.createValue(value), true);
    }

    protected Value(Pixbuf pixbuf) {
        this(GValue.createValue(pixbuf), true);
    }

    protected Value(Object obj) {
        this(GValue.createValue(obj), true);
    }

    protected Value(FontDescription desc) {
        this(GValue.createValue(desc), true);
    }

    /*
     * Another one that's only really here for unit tests.
     */
    protected Object getObject() {
        return GValue.getObject(this);
    }

    protected Pixbuf getPixbuf() {
        return GValue.getPixbuf(this);
    }

    protected float getFloat() {
        return GValue.getFloat(this);
    }

    protected double getDouble() {
        return GValue.getDouble(this);
    }

    protected Value(long value) {
        this(GValue.createValue(value), true);
    }

    protected Value(Constant value) {
        this(GValue.createValue(value), true);
    }

    protected long getLong() {
        return GValue.getLong(this);
    }

    protected Constant getEnum() {
        return GValue.getEnum(this);
=======
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ala.spatial.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Adam
 */
public class Facet implements Serializable {

    /**
     * Parse a facet created by webportal:
     *
     * Classification legend queries
     *
     * (1) <field>:"<value>" OR <field>:"<value>"
     * (2) -<field>:*
     * (3) -(<field>:* AND -<field>:"<value>" AND -<field>:"<value>")
     *
     * (4) <field>:[<min> TO <max>] OR <field>:[<min> TO <max>]
     * (5) -<field>:[* TO *]
     * (6) -(<field>:[* TO *] AND -<field>:[<min> TO <max>] AND -<field>:[<min> TO <max>])
     *
     *
     * Environmental Envelope queries
     *
     * (7) <field>:[<min> TO <max>] AND <field>:[<min> TO <max>]
     *
     *
     * Scatterplot queries
     *
     * (8) <field>:[<min> TO <max>] AND <field>:[<min> TO <max>]
     * (9) -(<field>:[* TO *] AND <field>:[* TO *])
     * (10) -(-(<field>:[<min> TO <max>] AND <field>:[<min> TO <max>]) AND <field>:[* TO *] AND <field>:[* TO *])
     *
     *
     * (11) -(<field>:[<min> TO <max>] AND <field>:[<min> TO <max>])
     * (12) <field>:[* TO *] AND <field>:[* TO *]
     * (13) (-<field>:[<min> TO <max>] OR -<field>:[<min> TO <max>]) AND <field>:[* TO *] AND <field>:[* TO *]
     *
     *
     * @param fq facet to parse as String
     * @param isInteger flag for range queries.  Want to report an inclusive range
     * @return
     */
    public static Facet parseFacet(String fq) {
        if (fq == null || fq.length() < 3) {
            return null;

        }

        //tests
        boolean hasAnd = fq.contains(" AND ");
        boolean hasOr = fq.contains(" OR ");

        if (fq.startsWith("-(-(")) {
            // (10) -(-(<field>:[<min> TO <max>] AND <field>:[<min> TO <max>]) AND <field>:[* TO *] AND <field>:[* TO *])
            int p = fq.indexOf(')');

            //reverse sign to convert first inner AND into OR
            Facet[] orPart = parseTerms(" AND ", fq.substring(4, p), true);
            Facet[] andPart = parseTerms(" AND ", fq.substring(p + 6, fq.length() - 1), false);

            return new Facet(fq, orPart, andPart, null);
        } else if (fq.startsWith("-(") && !fq.endsWith(")") && !hasOr) {
            //(13) -(<field>:[<min> TO <max>] AND <field>:[<min> TO <max>]) AND <field>:[* TO *] AND <field>:[* TO *]
            int p = fq.indexOf(')');

            //reverse sign to convert first inner AND into OR
            Facet[] orPart = parseTerms(" AND ", fq.substring(2, p), true);
            Facet[] andPart = parseTerms(" AND ", fq.substring(p + 6, fq.length() - 1), false);
            return new Facet(fq, orPart, andPart, null);
        } else {//if((hasAnd != hasOr) || (!hasAnd && !hasOr)) {
            //(1) (2) (3) (4) (5) (6) (7) (8) (9) (11) (12)
            boolean invert = fq.charAt(0) == '-' && fq.charAt(1) == '(';
            String s = invert ? fq.substring(2, fq.length() - 1) : fq;
            Facet[] f = parseTerms((hasAnd ? " AND " : " OR "), s, invert);

            if (f.length == 1) {
                return f[0];
            } else {
                if (invert) {
                    return new Facet(fq, (hasAnd ? f : null), (hasOr ? f : null), null);
                } else {
                    return new Facet(fq, (hasOr ? f : null), (hasAnd ? f : null), null);
                }
            }
        }

        //return null;
    }

    static Facet[] parseTerms(String separator, String fq, boolean invert) {
        String[] terms = fq.split(separator);
        Facet[] facets = new Facet[terms.length];
        for (int i = 0; i < terms.length; i++) {
            String ff = terms[i];
            int offset = ff.startsWith("-") ? 1 : 0;
            String f = ff.substring(offset, ff.indexOf(':'));
            String v = ff.substring(ff.indexOf(':') + 1);
            if (v.charAt(0) == '\"' || v.charAt(0) == '*' || !v.toUpperCase().contains(" TO ")) {
                //value
                if (v.charAt(0) == '\"') {
                    v = v.substring(1, v.length() - 1);
                }
                facets[i] = new Facet(f, v, invert != (offset == 0));
            } else {
                //range
                String[] n = v.toUpperCase().substring(1, v.length() - 1).split(" TO ");
                /*
                 * double[] d = {n[0].equals("*") ? Double.NEGATIVE_INFINITY : Double.parseDouble(n[0]),
                n[1].equals("*") ? Double.POSITIVE_INFINITY : Double.parseDouble(n[1])};
                facets[i] = new Facet(f, d[0], d[1], invert != (offset == 0));
                 *
                 */
                facets[i] = new Facet(f, n[0], n[1], invert != (offset == 0));
            }
        }

        return facets;
    }
    String field;
    String value;
    String[] valueArray;
    String parameter;
    double min;
    double max;
    boolean includeRange;
    Facet[] orInAndTerms;
    Facet[] andTerms;
    Facet[] orTerms;

    public Facet(String field, String value, boolean includeRange) {
        this.field = field;
        this.value = value;
        this.includeRange = includeRange;
        this.parameter = null;
        this.min = Double.NaN;
        this.max = Double.NaN;
        this.valueArray = null;
        if (this.value != null) {
            this.valueArray = new String[]{this.value};
        }
    }

    public Facet(String field, double min, double max, boolean includeRange) {
        this.field = field;
        this.min = min;
        this.max = max;
        this.includeRange = includeRange;

        String strMin = Double.isInfinite(min) ? "*" : (min == (int) min) ? String.format("%d", (int) min) : String.valueOf(min);
        String strMax = Double.isInfinite(max) ? "*" : (max == (int) max) ? String.format("%d", (int) max) : String.valueOf(max);

        this.value = "[" + strMin + " TO " + strMax + "]";

        this.valueArray = null;

        this.parameter = (includeRange ? "" : "-") + this.field + ":" + this.value;
    }

    public Facet(String field, String strMin, String strMax, boolean includeRange) {
        this.field = field;

        if(field.equals("occurrence_year")) {
            strMin = strMin.replace("-12-31T00:00:00Z", "").replace("-01-01T00:00:00Z", "");
            strMax = strMax.replace("-12-31T00:00:00Z", "").replace("-01-01T00:00:00Z", "");
        }
        double[] d = {strMin.equals("*") ? Double.NEGATIVE_INFINITY : Double.parseDouble(strMin),
            strMax.equals("*") ? Double.POSITIVE_INFINITY : Double.parseDouble(strMax)};
        this.min = d[0];
        this.max = d[1];
        this.includeRange = includeRange;

        if(field.equals("occurrence_year")) {
            this.value = "[" + strMin + "-01-01T00:00:00Z TO " + strMax + "-12-31T00:00:00Z]";
        } else {
            this.value = "[" + strMin + " TO " + strMax + "]";
        }

        this.valueArray = null;

        this.parameter = (includeRange ? "" : "-") + this.field + ":" + this.value;
    }

    public Facet(String fq, Facet[] orInAndTerms, Facet[] andTerms, Facet[] orTerms) {
        //make toString work
        parameter = fq;

        //make isValid and getFields work
        this.orInAndTerms = orInAndTerms;
        this.andTerms = andTerms;
        this.orTerms = orTerms;
    }

    @Override
    public String toString() {
        String facet = "";
        if (parameter == null) {
            if ((value.startsWith("\"") && value.endsWith("\"")) || value.equals("*")) {
                facet = (includeRange ? "" : "-") + field + ":" + value;
            } else {
                facet = (includeRange ? "" : "-") + field + ":\"" + value + "\"";
            }

            if (field.equals("occurrence_year")) {
                facet = facet.replace(" TO ","-01-01T00:00:00Z TO ").replace("]","-12-31T00:00:00Z]");
            } else if (field.equals("occurrence_year_decade") || field.equals("decade")) {
                if(value.contains("before")) {
                    facet = (includeRange ? "" : "-") + field + ":[* TO 1849-12-31T00:00:00Z]";
                } else {
                    String yr = value.replace("\"","");
                    yr = yr.substring(0, yr.length()-1);
                    facet = (includeRange ? "" : "-") + field + ":[" + yr + "0-01-01T00:00:00Z TO " + yr + "9-12-31T00:00:00Z]";
                }
                facet = facet.replace("_decade","");
            }
        } else {
            facet = parameter;

            if(!facet.contains("-01-01T00:00:00Z")
                    && !facet.contains("-12-31T00:00:00Z")
                    && facet.contains("occurrence_year")) {
                if (field.equals("occurrence_year")) {
                    parameter = parameter.replace(" TO ","-01-01T00:00:00Z TO ").replace("]","-12-31T00:00:00Z]");
                } else if (field.equals("occurrence_year_decade") || field.equals("decade")) {
                    //TODO: make this work
                }
            }
        }

        return facet;
    }

    public String[] getFields() {
        Set<String> fieldSet = new HashSet<String>();
        if (field != null) {
            fieldSet.add(field);
        }
        if (orInAndTerms != null) {
            for (Facet f : orInAndTerms) {
                for (String s : f.getFields()) {
                    fieldSet.add(s);
                }
            }
        }
        if (andTerms != null) {
            for (Facet f : andTerms) {
                for (String s : f.getFields()) {
                    fieldSet.add(s);
                }
            }
        }
        if (orTerms != null) {
            for (Facet f : orTerms) {
                for (String s : f.getFields()) {
                    fieldSet.add(s);
                }
            }
        }

        String[] fields = new String[fieldSet.size()];
        fieldSet.toArray(fields);
        return fields;
    }

    public boolean isValid(String v) {
        if (getType() == 1) {
            for (int i = 0; i < valueArray.length; i++) {
                if (valueArray[i].equals(v) || (v.length() != 0 && valueArray[i].equals("*"))) {
                    return includeRange;
                }
            }
            return !includeRange;
        } else if (getType() == 0) {
            try {
                double d = Double.parseDouble(v);
                boolean inside = d >= min && d <= max;
                return includeRange ? inside : !inside;
            } catch (Exception e) {
            }
        } else {
            boolean state = true;
            if (orInAndTerms != null) {
                state = sumTermTests(orInAndTerms, v) > 0;
            }
            if (andTerms != null) {
                if (!state) {
                    //state = false;
                } else {
                    state = sumTermTests(andTerms, v) == andTerms.length;
                }
            }
            if (orTerms != null) {
                if (state) {
                    return true;
                } else {
                    return sumTermTests(orTerms, v) > 0;
                }
            } else {
                return state;
            }
        }

        return !includeRange;
    }

    public boolean isValid(double d) {
        if (getType() == 1) {
            String v = String.valueOf(d);
            if (Double.isNaN(d)) {
                v = "";
            }
            for (int i = 0; i < valueArray.length; i++) {
                if (valueArray[i].equals(v) || (v.length() != 0 && valueArray[i].equals("*"))) {
                    return includeRange;
                }
            }

            return !includeRange;
        } else if (getType() == 0) {
            try {
                boolean inside = d >= min && d <= max;
                return includeRange ? inside : !inside;
            } catch (Exception e) {
            }
        } else {
            boolean state = true;
            if (orInAndTerms != null) {
                state = sumTermTests(orInAndTerms, d) > 0;
            }
            if (andTerms != null) {
                if (!state) {
                    //state = false;
                } else {
                    state = sumTermTests(andTerms, d) == andTerms.length;
                }
            }
            if (orTerms != null) {
                if (state) {
                    return true;
                } else {
                    return sumTermTests(orTerms, d) > 0;
                }
            } else {
                return state;
            }
        }

        return !includeRange;
    }

    /**
     * Facet type
     * 0 = numeric
     * 1 = string
     * 2 = group
     *
     * @return
     */
    public int getType() {
        if (orInAndTerms != null || andTerms != null || orTerms != null) {
            return 2;
        } else if (valueArray != null) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean isValid(List<QueryField> fields, int record) {
        if (getType() == 2) {
            boolean state = true;
            if (orInAndTerms != null) {
                state = sumTermTests(orInAndTerms, fields, record) > 0;
            }
            if (andTerms != null) {
                if (!state) {
                    //state = false;
                } else {
                    state = sumTermTests(andTerms, fields, record) == andTerms.length;
                }
            }
            if (orTerms != null) {
                if (state) {
                    return true;
                } else {
                    return sumTermTests(orTerms, fields, record) > 0;
                }
            } else {
                return state;
            }
        } else {
            for (QueryField qf : fields) {
                if (qf.getName().equals(field)) {
                    if (getType() == 1) {
                        return isValid(qf.getAsString(record));
                    } else {    //type == 0
                        switch (qf.getFieldType()) {
                            case DOUBLE:
                                return isValid(qf.getDouble(record));
                            case FLOAT:
                                return isValid(qf.getFloat(record));
                            case LONG:
                                return isValid((double) qf.getLong(record));
                            case INT:
                                return isValid(qf.getInt(record));
                            default:
                                return isValid(qf.getAsString(record));
                        }
                    }
                }
            }
            //if field not found, treat as outside of any specified range
            return !includeRange;
        }
    }

    private int sumTermTests(Facet[] andTerms, List<QueryField> fields, int record) {
        int sum = 0;
        for (int i = 0; andTerms != null && i < andTerms.length; i++) {
            if (andTerms[i].getType() == 2) {
                if (andTerms[i].isValid(fields, record)) {
                    sum++;
                }
            } else {
                if (andTerms[i].isValid(fields, record)) {
                    sum++;
                }
            }
        }
        return sum;
    }

    private int sumTermTests(Facet[] andTerms, String value) {
        int sum = 0;
        for (int i = 0; andTerms != null && i < andTerms.length; i++) {
            if (andTerms[i].getType() == 2) {
                if (andTerms[i].isValid(value)) {
                    sum++;
                }
            } else {
                if (andTerms[i].isValid(value)) {
                    sum++;
                }
            }
        }
        return sum;
    }

    private int sumTermTests(Facet[] andTerms, double value) {
        int sum = 0;
        for (int i = 0; andTerms != null && i < andTerms.length; i++) {
            if (andTerms[i].getType() == 2) {
                if (andTerms[i].isValid(value)) {
                    sum++;
                }
            } else {
                if (andTerms[i].isValid(value)) {
                    sum++;
                }
            }
        }
        return sum;
    }

    public double getMin() {
        if (getType() == 0) {
            return min;
        } else if (getType() == 1) {
            return Float.NaN;
        } else {
            double min = Double.POSITIVE_INFINITY;
            if (orInAndTerms != null) {
                for (int i = 0; i < orInAndTerms.length; i++) {
                    double newMin = orInAndTerms[i].getMin();
                    if (orInAndTerms[i].includeRange && newMin < min) {
                        min = newMin;
                    }
                }
            }
            if (andTerms != null) {
                for (int i = 0; i < andTerms.length; i++) {
                    double newMin = andTerms[i].getMin();
                    if (andTerms[i].includeRange && newMin < min) {
                        min = newMin;
                    }
                }
            }
            if (orTerms != null) {
                for (int i = 0; i < orTerms.length; i++) {
                    double newMin = orTerms[i].getMin();
                    if (orTerms[i].includeRange && newMin < min) {
                        min = newMin;
                    }
                }
            }
            return min;
        }
    }

    public double getMax() {
        if (getType() == 0) {
            return max;
        } else if (getType() == 1) {
            return Float.NaN;
        } else {
            double max = Double.NEGATIVE_INFINITY;
            if (orInAndTerms != null) {
                for (int i = 0; i < orInAndTerms.length; i++) {
                    double newMax = orInAndTerms[i].getMax();
                    if (orInAndTerms[i].includeRange && newMax > max) {
                        max = newMax;
                    }
                }
            }
            if (andTerms != null) {
                for (int i = 0; i < andTerms.length; i++) {
                    double newMax = andTerms[i].getMax();
                    if (andTerms[i].includeRange && newMax > max) {
                        max = newMax;
                    }
                }
            }
            if (orTerms != null) {
                for (int i = 0; i < orTerms.length; i++) {
                    double newMax = orTerms[i].getMax();
                    if (orTerms[i].includeRange && newMax > max) {
                        max = newMax;
                    }
                }
            }
            return max;
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

