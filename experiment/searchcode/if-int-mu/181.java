package com.mplify.enums;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2008, M-PLIFY S.A.
 *                     21, rue Glesener
 *                     L-1631 LUXEMBOURG
 *
 * All rights reserved.
 *******************************************************************************
 *******************************************************************************
 * A simple tri-state enumerative value: can be 'true', 'false' or 'mu'
 * (i.e. 'unknown'). This is one of the few implementation that does not use
 * a Map for reverse lookup.
 * 
 * 2004.10.04 - Created & simplified. Replaces old m3p.utils.Troolean
 * 2004.12.06 - Added obtain(Integer). Remade in best idiom.
 * 2005.12.06 - Class is now Serializable as it is used for the AlarmTILT client
 * 2013.01.26 - Added valueOf() methods 
 * 2013.02.01 - Added asBoolean() to be used by Groovy
 * *****************************************************************************/

public class Troolean implements Serializable, EnumerativeTypeUsingInteger {

    private final static String CLASS = Troolean.class.getName();

    // private final static Logger LOGGER_obtain = LoggerFactory.getLogger(CLASS + ".obtain");

    private final static long serialVersionUID = 0x275fd07b77429d8dL; // OK

    /*
     * Members. The Troolean is identified by 'value', which is also what is used in the database
     */

    private final int value;
    private final String name;

    /*
     * These are the possible instance of this class; they form a quasi-enumeration
     * These are public and used by client code.
     */

    public static final Troolean TRUE;
    public static final Troolean FALSE;
    public static final Troolean MU;

    /*
     * An list of the values, public but immutable 
     */

    public final static List<Troolean> LIST;

    /*
     * The values hashed by their Integer id, private
     */

    private final static Map<Integer, Troolean> MAP = new HashMap<Integer, Troolean>();
    private final static Map<String, Troolean> REVERSE_MAP = new HashMap<String, Troolean>();

    /*
     * Static construction takes care to assign unique (and constant) numeric identifiers
     * to the various instances. They must be stable because they are used in the database. 
     * At the same time, the values are assigned to the 'list', which will then be
     * made immutable and assigned to LIST.
     */

    static {
        ArrayList<Troolean> list = new ArrayList<Troolean>();
        list.add(FALSE = new Troolean( 0, "false"));
        list.add(TRUE = new Troolean( 1, "true"));
        list.add(MU = new Troolean( 2, "mu"));
        //
        // hash the values into the private hash, too, and put them into the ARRAY
        // should we check for duplicates here? naahh...
        //
        for (Troolean tr : list) {
            MAP.put(new Integer(tr.value), tr);
            REVERSE_MAP.put(tr.toString().trim().toLowerCase(), tr); // key is the lowercased short string
        }
        // saturday night special: "-1" is *also* "mu"
        MAP.put(new Integer(-1), MU);
        LIST = Collections.unmodifiableList(list);
    }

    /**
     * The constructor, can only called by this class. The class defines
     * the possible cases using the constructor and that's it. Constructor is PRIVATE! 
     */

    private Troolean(int x, String name) {
        this.value = x;
        this.name = name;
    }

    /**
     * Transform this value into a string. 
     */

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Is a value valid?
     */

    public static boolean isValid(int x) {
        return (MAP.containsKey(new Integer(x)));
    }

    /**
     * Comparison compares the underlying values
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // quick guess
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Troolean)) {
            return false;
        }
        Troolean other = (Troolean) obj;
        return (other.value == this.value);
    }

    /**
     * Get the actual underlying value (use sparingly, only for store actually)
     */

    @Override
    public int getValue() {
        return value;
    }

    /**
     * Return a value instance given an int.
     * Throws 'IllegalArgumentException' if there is no instance corresponding to 'x'
     */

    public static Troolean obtain(int x) {
        Troolean res = MAP.get(new Integer(x));
        if (res == null) {
            throw new IllegalArgumentException("Nothing corresponding to value '" + x + "' exists");
        } else {
            return res;
        }
    }

    /**
     * Obtain from an Integer. Do not pass (null)
     */

    public static Troolean obtain(Integer x) {
        if (x == null) {
            throw new IllegalArgumentException("The passed Integer is (null)");
        } else {
            return obtain(x.intValue());
        }
    }

    /**
     * Obtain a Troolean from the passed String 'x', which is lowercased and trimmed
     * before being compared to the 'short text'. In case no match is found, the
     * 'defaultValue' is returned. That value can be (null).
     * You may also want to throw an IllegalArgumentException, set 'throwIfNoMatch' to
     * true.
     * This method can grok 'null' as 'x'.
     */

    public static Troolean obtain(String xIn, Troolean defaultValue, boolean throwIfNoMatch) {
        String x = xIn;
        if (x != null) {
            x = x.toLowerCase().trim(); // values is lowercase!
        }
        Troolean smc = REVERSE_MAP.get(x);
        if (smc == null) {
            if (throwIfNoMatch) {
                throw new IllegalArgumentException("The passed String, '" + x + "' could not be transformed into a " + CLASS);
            } else {
                return defaultValue;
            }
        } else {
            return smc;
        }
    }

    /**
     * Wanna hash this...
     */

    @Override
    public int hashCode() {
        return value;
    }

    /**
     * Make string where possibly values are comma-separted. Used in 'usage' strings
     */

    public static String toUsageString() {
        StringBuffer buf = new StringBuffer();
        boolean appendComma = false;
        for (Troolean element : Troolean.LIST) {
            if (appendComma) {
                buf.append(",");
            }
            buf.append("'");
            buf.append(element.toString());
            buf.append("'");
            appendComma = true;
        }
        return buf.toString();
    }

    /**
     * Transform to a Logical NOT. MU is transformed into MU
     */

    public Troolean toNot() {
        // fastest comparison?
        if (this.value == TRUE.value) {
            return Troolean.FALSE;
        } else if (this.value == FALSE.value) {
            return Troolean.TRUE;
        } else {
            return Troolean.MU;
        }
    }

    /**
     * Transform to a Boolean. MU is transformed into 'null'
     */

    public Boolean toBoolean() {
        // fastest comparison?
        if (this.value == TRUE.value) {
            return Boolean.TRUE;
        } else if (this.value == FALSE.value) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

    /**
     * Get the boolean value. Throws if MU
     */

    public boolean booleanValue() {
        // fastest comparison?
        if (this.value == TRUE.value) {
            return true;
        } else if (this.value == FALSE.value) {
            return false;
        } else {
            throw new IllegalStateException("The value is currently " + this);
        }
    }

    /**
     * Used by groovy
     */

    public boolean asBoolean() {
        return booleanValue();
    }

    /**
     * Map from a boolean
     */

    public static Troolean valueOf(boolean x) {
        return (x ? Troolean.TRUE : Troolean.FALSE);
    }

    /**
     * Map from a Boolean. "null" is remapped to MU
     */

    public static Troolean valueOf(Boolean x) {
        if (x == null) {
            return Troolean.MU;
        } else {
            return (x.booleanValue() ? Troolean.TRUE : Troolean.FALSE);
        }
    }

}

