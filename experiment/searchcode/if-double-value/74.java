<<<<<<< HEAD
/*
 * This file is part of JGAP.
 *
 * JGAP offers a dual license model containing the LGPL as well as the MPL.
 *
 * For licensing information please see the file license.txt included with JGAP
 * or have a look at the top of class org.jgap.Chromosome which representatively
 * includes the JGAP license policy applicable for any file delivered with JGAP.
 */
package org.jgap.gp.function;

import org.jgap.*;
import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.util.*;

/**
 * The if-then construct.
 *
 * @author Klaus Meffert
 * @since 3.0
 */
public class If
    extends CommandGene
implements ICloneable {
  /** String containing the CVS revision. Read out via reflection!*/
  private final static String CVS_REVISION = "$Revision: 1.7 $";

  public If(final GPConfiguration a_conf, Class a_returnType)
      throws InvalidConfigurationException {
    super(a_conf, 2, a_returnType);
  }

  public String toString() {
    return "if(&1) then (&2)";
  }

  public boolean execute_boolean(ProgramChromosome c, int n, Object[] args) {
    boolean x = c.execute_boolean(n, 0, args);
    boolean value = false;
    if (x) {
      value = c.execute_boolean(n, 1, args);
    }
    return value;
  }

  public int execute_int(ProgramChromosome c, int n, Object[] args) {
    int x = c.execute_int(n, 0, args);
    int value = 0;
    if (x >= 0) {
      value = c.execute_int(n, 1, args);
    }
    return value;
  }

  public long execute_long(ProgramChromosome c, int n, Object[] args) {
    long x = c.execute_long(n, 0, args);
    long value = 0;
    if (x >= 0) {
      value = c.execute_long(n, 1, args);
    }
    return value;
  }

  public float execute_float(ProgramChromosome c, int n, Object[] args) {
    float x = c.execute_float(n, 0, args);
    float value = 0;
    if (x >= 0) {
      value = c.execute_float(n, 1, args);
    }
    return value;
  }

  public double execute_double(ProgramChromosome c, int n, Object[] args) {
    double x = c.execute_double(n, 0, args);
    double value = 0;
    if (x >= 0) {
      value = c.execute_double(n, 1, args);
    }
    return value;
  }

  public void execute_void(ProgramChromosome c, int n, Object[] args) {
    int x = c.execute_int(n, 0, args);/**@todo add option for type of first child to constructor*/
    if (x >= 0) {
      c.execute_void(n, 1, args);
    }
  }

  /**
   * Clones the object. Simple and straight forward implementation here.
   *
   * @return cloned instance of this object
   *
   * @author Klaus Meffert
   * @since 3.4
   */
  public Object clone() {
    try {
      If result = new If(getGPConfiguration(), getReturnType());
      return result;
    } catch (Exception ex) {
      throw new CloneException(ex);
    }
  }
}
=======
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ala.spatial.analysis.index;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Some value correction while parsing strings.
 *
 * Permitted units:
 * mm, cm, km
 *
 * Permitted types (in m):
 * depth, >= 0
 * altitude, <= 10,000,000
 * year, > 1600 and <= current
 * distance, >= 0 and <= 20,000,000
 *
 * If invalid value, default to Double.NaN, Float.NaN, Integer.MIN_VALUE.
 *
 * @author Adam
 */
public class ValueCorrection {

    final static String[] unitsList = {"mm", "cm", "m", "km"};
    final static double[] unitsListScale = {0.001, 0.01, 1, 1000};
    final static String[] typesList = {"depth", "altitude", "year", "distance"};
    final static double[] typesListMin = {0, Double.NEGATIVE_INFINITY, 1000, 0};
    final static double[] typesListMax = {Double.POSITIVE_INFINITY, 10000000, java.util.Calendar.getInstance().get(java.util.Calendar.YEAR), Double.POSITIVE_INFINITY};

    static public int correctToInt(String targetUnits, String validationType, String stringValue) {
        int i = scaleInt(targetUnits, stringValue);
        i = correct(i, validationType);
        return i;
    }

    static public double correctToDouble(String targetUnits, String validationType, String stringValue) {
        double d = scaleDouble(targetUnits, stringValue);
        d = correct(d, validationType);
        return d;
    }

    /**
     * Attempt to read an Integer to the target units.
     *
     * test for < #
     * test for < # units
     * test for # - #
     * test for # units - #
     * test for # units - # units
     * test for # - # units
     * 
     * @param targetUnits
     * @param validationType
     * @param stringValue
     * @return
     */
    static int scaleInt(String targetUnits, String stringValue) {
        int vi = Integer.MIN_VALUE;

        //test zero length
        if (stringValue.length() == 0) {
            return vi;
        }

        //attempt parse
        try {
            return Integer.parseInt(stringValue);
        } catch (Exception e) {
        }

        stringValue = clean(stringValue);

        //test for 'units' at end
        int unitsPos = getUnitsPos(stringValue);
        double multiplier = getUnitsMultiplier(unitsPos, targetUnits);

        //fix stringValue for unitsPos >= 0
        if (unitsPos >= 0) {
            stringValue = stringValue.substring(0, stringValue.length() - unitsList[unitsPos].length());
        }

        if (stringValue.length() > 0 && stringValue.charAt(0) == '<') {
            //test for < #
            //test for < # units
            stringValue = stringValue.substring(1);
        } else {
            int dashPos = stringValue.indexOf('-', 1);
            if (dashPos > 0) {
                //test for # - #
                //test for # units - #
                //test for # units - # units
                //test for # - # units
                stringValue = stringValue.substring(dashPos + 1);
            }
        }

        //parse remaining stringValue as int and apply multiplier
        try {
            vi = (int) (Integer.parseInt(stringValue) * multiplier);
        } catch (Exception e) {
        }

        //failed 2nd parse as int, try as double and apply multiplier
        if (vi == Integer.MIN_VALUE) {
            try {
                vi = (int) (Double.parseDouble(stringValue) * multiplier);
            } catch (Exception e) {
            }
        }

        return vi;
    }

    /**
     * Attempt to read an Integer to the target units.
     *
     * test for < #
     * test for < # units
     * test for # - #
     * test for # units - #
     * test for # units - # units
     * test for # - # units
     *
     * @param targetUnits
     * @param validationType
     * @param stringValue
     * @return
     */
    static double scaleDouble(String targetUnits, String stringValue) {
        double vd = Double.NaN;

        //test zero length
        if (stringValue.length() == 0) {
            return vd;
        }

        //attempt parse
        try {
            return Double.parseDouble(stringValue);
        } catch (Exception e) {
        }

        stringValue = clean(stringValue);

        //test for 'units' at end
        int unitsPos = getUnitsPos(stringValue);
        double multiplier = getUnitsMultiplier(unitsPos, targetUnits);

        //fix stringValue for unitsPos >= 0
        if (unitsPos >= 0) {
            stringValue = stringValue.substring(0, stringValue.length() - unitsList[unitsPos].length());
        }

        if (stringValue.length() > 0 && stringValue.charAt(0) == '<') {
            //test for < #
            //test for < # units
            stringValue = stringValue.substring(1);
        } else {
            int dashPos = stringValue.indexOf('-', 1);
            if (dashPos > 0) {
                //test for # - #
                //test for # units - #
                //test for # units - # units
                //test for # - # units
                stringValue = stringValue.substring(dashPos + 1);
            }
        }

        //parse remaining stringValue as double and apply multiplier
        try {
            vd = Double.parseDouble(stringValue) * multiplier;
        } catch (Exception e) {
        }

        return vd;
    }

    private static String clean(String value) {
        //remove spaces
        value = value.replace(" ", "");

        //remove trailing non-unit or number characters
        int i;
        boolean remove = true;
        while (remove && value.length() > 0) {
            char end = value.charAt(value.length() - 1);
            if (end <= '9' && end >= '0') {
                break;
            } else {
//                remove = true;
//                for(i=0;i<unitsList.length;i++) {
//                    if(end == unitsList[i].charAt(unitsList[i].length()-1)) {
//                        remove = false;
//                        break;
//                    }
//                }
//                if(remove) {
//                    value = value.substring(0, value.length() - 1);
//                }

                //only permit removal of '.'
                if (end == '.') {
                    value = value.substring(0, value.length() - 1);
                    remove = true;
                } else {
                    remove = false;
                }
            }
        }

        return value.trim().toLowerCase();
    }

    private static int getUnitsPos(String stringValue) {
        int pos = -1;
        if (stringValue.length() > 0) {
            for (int i = 0; i < unitsList.length; i++) {
                if (stringValue.endsWith(unitsList[i])) {
                    char c = stringValue.charAt(stringValue.length() - unitsList[i].length() - 1);
                    if (c <= '9' && c >= '0') {
                        pos = i;
                        break;
                    }
                }
            }
        }
        return pos;
    }

    private static double getUnitsMultiplier(int unitsPos, String targetUnits) {
        if (unitsPos < 0 || targetUnits == null) {
            return 1;
        }
        double multiplier = 1;
        for (int i = 0; i < unitsList.length; i++) {
            if (targetUnits.equals(unitsList[i])) {
                multiplier = unitsListScale[unitsPos] / unitsListScale[i];
            }
        }
        return multiplier;
    }

    public static void main(String[] args) {
        String filename = "d:\\list.csv";

        try {
            BufferedReader r = new BufferedReader(new FileReader(filename));
            String s;
            System.out.println("original value,corrected double,corrected int");
            while ((s = r.readLine()) != null) {
                double d = correctToDouble("m", "distance", s);
                int i = correctToInt("m", "distance", s);

                System.out.println(s + "," + ((Double.isNaN(d)) ? "" : d) + "," + ((i == Integer.MIN_VALUE) ? "" : i));
            }
            r.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int correct(int v, String validationType) {
        if (validationType != null && v != Integer.MIN_VALUE) {
            for (int i = 0; i < typesList.length; i++) {
                if (typesList[i].equals(validationType)) {
                    //test
                    if (v < typesListMin[i] || v > typesListMax[i]) {
                        v = Integer.MIN_VALUE;
                    }
                    break;
                }
            }
        }
        return v;
    }

    private static double correct(double v, String validationType) {
        if (validationType != null && !Double.isNaN(v)) {
            for (int i = 0; i < typesList.length; i++) {
                if (typesList[i].equals(validationType)) {
                    //test
                    if (v < typesListMin[i] || v > typesListMax[i]) {
                        v = Double.NaN;
                    }
                    break;
                }
            }
        }
        return v;
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

