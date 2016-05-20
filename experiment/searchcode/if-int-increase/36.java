package org.gbif.ecat.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;

public class StringUtils {

  static final int LINNEAN_YEAR = 1751;
  static final String CONS = "BCDFGHJKLMNPQRSTVWXYZ";
  private static final Pattern OCT = Pattern.compile("^[0-7]+$");
  private static final Pattern HEX = Pattern.compile("^[0-9abcdefABCDEF]+$");

  static final String VOC = "AEIOU";
  static Random rnd = new Random();

  /**
   * Returns an empty string or the trimmed lower case version of any input, but never NULL.
   */
  public static String emptyLowerCase(String x) {
    if (x == null) {
      return "";
    }
    return x.trim().toLowerCase();
  }

  public static String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  public static String increase(String x) {
    if (x == null) {
      return null;
    }
    if (x.equals("")) {
      return x;
    }
    char lastChar = x.charAt(x.length() - 1);
    // if the last char is a z increase the next char to the left instead
    if (lastChar == 'z' && x.length() > 1) {
      return increase(x.substring(0, x.length() - 1)) + "a";
    } else if (lastChar == 'Z' && x.length() > 1) {
      return increase(x.substring(0, x.length() - 1)) + "A";
    } else {
      lastChar++;
      return x.substring(0, x.length() - 1) + lastChar;
    }
  }

  public static String joinIfNotNull(String delimiter, Object... values) {
    StringBuilder sb = new StringBuilder();
    boolean comma = false;
    for (Object obj : values) {
      if (obj != null) {
        if (comma) {
          sb.append(delimiter);
        } else {
          comma = true;
        }
        sb.append(obj);
      }
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    System.out.println(increase("Carla"));
    System.out.println(increase("Holz"));
    System.out.println(increase("Atz"));
    // System.out.println(StringUtils.toStringBuilder(false,new Long(32),new
    // Long(32),"Hallo",null,null,"Pia ",new Boolean(true)));
    // System.out.println(StringUtils.toStringBuilder(true,"Age",new
    // Long(32),"Size",new
    // Long(32),"Hi","Hallo","La",null,"Lo",null,"Name","Pia ","Cool",new
    // Boolean(true)));
  }

  public static Boolean parseBoolean(String x) {
    x = org.apache.commons.lang3.StringUtils.trimToEmpty(x).toLowerCase();
    if (x.equals("true") || x.equals("t") || x.equals("1") || x.equals("yes") || x.equals("y")) {
      return true;
    }
    if (x.equals("false") || x.equals("f") || x.equals("0") || x.equals("no") || x.equals("n")) {
      return false;
    }
    return null;
  }

  public static String randomSpecies() {
    return WordUtils.capitalize(randomString(rnd.nextInt(9) + 3)) + " " + randomString(rnd.nextInt(11) + 4)
      .toLowerCase();
  }

  /**
   * Creates a random string in upper case of given length with purely latin characters only.
   * Vocals are used much more frequently than consonants
   *
   * @return a random string in upper case
   */
  public static String randomString(int len) {
    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      if (rnd.nextInt(3) > 1) {
        sb.append(CONS.charAt(rnd.nextInt(CONS.length())));
      } else {
        sb.append(VOC.charAt(rnd.nextInt(VOC.length())));
      }
    }

    return sb.toString();
  }

  public static String randomSpeciesYear() {
    int maxYear = Calendar.getInstance().get(Calendar.YEAR);
    return String.valueOf(LINNEAN_YEAR + rnd.nextInt(maxYear - LINNEAN_YEAR + 1));
  }

  /**
   * simple integer parsing method that does not throw any exception but
   * returns null instead
   *
   * @return the parsed integer or null
   */
  public static Integer toInteger(String x) {
    try {
      return Integer.valueOf(x);
    } catch (NumberFormatException e) {

    }
    return null;
  }

  public static String toStringBuilder(boolean withLabels, Object... prop) {
    if (withLabels) {
      List<String> result = new ArrayList<String>();
      int idx = 0;
      while (idx + 1 < prop.length) {
        String attr = (prop[idx] == null ? "?" : prop[idx].toString()) + ":" + (prop[idx + 1] == null ? "---"
          : prop[idx + 1].toString());
        // System.out.println(attr);
        result.add(attr);
        idx++;
        idx++;
      }
      return org.apache.commons.lang3.StringUtils.join(result, " ");
    } else {
      List<String> result = new ArrayList<String>();
      for (Object p : prop) {
        result.add(p == null ? "---" : p.toString());
      }
      return org.apache.commons.lang3.StringUtils.join(result, " ");
    }
  }

  /**
   * Unescapes various unicode escapes if existing:
   * java unicode escape, four hexadecimal digits
   * \ uhhhh
   * octal escape
   * \nnn
   * The octal value nnn, where nnn stands for 1 to 3 digits between 0 and 7. For example, the code for the ASCII
   * ESC (escape) character is \033.
   * hexadecimal escape
   * \xhh...
   * The hexadecimal value hh, where hh stands for a sequence of hexadecimal digits (09, and either AF or
   * af).Like the same construct in ISO C, the escape sequence continues until the first nonhexadecimal digit is
   * seen.
   * However, using more than two hexadecimal digits produces undefined results. (The \x escape sequence is not
   * allowed
   * in POSIX awk.)
   *
   * @param text string potentially containing unicode escape chars
   *
   * @return the unescaped string
   */
  public static String unescapeUnicodeChars(String text) {
    if (text == null) {
      return null;
    }
    // replace unicode, hexadecimal or octal character encodings by iterating over the chars once
    //
    // java unicode escape, four hexadecimal digits
    // \ uhhhh
    //
    // octal escape
    // \nnn
    // The octal value nnn, where nnn stands for 1 to 3 digits between 0 and 7. For example, the code for the ASCII
    // ESC (escape) character is \033.
    //
    // hexadecimal escape
    // \xhh...
    // The hexadecimal value hh, where hh stands for a sequence of hexadecimal digits (09, and either AF or
    // af).
    // Like the same construct in ISO C, the escape sequence continues until the first nonhexadecimal digit is seen.
    // However, using more than two hexadecimal digits produces undefined results. (The \x escape sequence is not allowed
    // in POSIX awk.)
    int i = 0, len = text.length();
    char c;
    StringBuffer sb = new StringBuffer(len);
    while (i < len) {
      c = text.charAt(i++);
      if (c == '\\') {
        if (i < len) {
          c = text.charAt(i++);
          try {
            if (c == 'u' && text.length() >= i + 4) {
              // make sure we have only hexadecimals
              String hex = text.substring(i, i + 4);
              if (HEX.matcher(hex).find()) {
                c = (char) Integer.parseInt(hex, 16);
                i += 4;
              } else {
                throw new NumberFormatException("No hex value: " + hex);
              }
            } else if (c == 'n' && text.length() >= i + 2) {
              // make sure we have only 0-7 digits
              String oct = text.substring(i, i + 2);
              if (OCT.matcher(oct).find()) {
                c = (char) Integer.parseInt(oct, 8);
                i += 2;
              } else {
                throw new NumberFormatException("No octal value: " + oct);
              }
            } else if (c == 'x' && text.length() >= i + 2) {
              // make sure we have only hexadecimals
              String hex = text.substring(i, i + 2);
              if (HEX.matcher(hex).find()) {
                c = (char) Integer.parseInt(hex, 16);
                i += 2;
              } else {
                throw new NumberFormatException("No hex value: " + hex);
              }
            } else if (c == 'r' || c == 'n' || c == 't') {
              // escaped newline or tab. Replace with simple space
              c = ' ';
            } else {
              throw new NumberFormatException("No char escape");
            }
          } catch (NumberFormatException e) {
            // keep original characters including \ if escape sequence was invalid
            // but replace \n with space instead
            if (c == 'n') {
              c = ' ';
            } else {
              c = '\\';
              i--;
            }
          }
        }
      } // fall through: \ escapes itself, quotes any character but u
      sb.append(c);
    }
    return sb.toString();
  }

  public static Map<String, String> upper(Map<String, String> map) {
    Map<String, String> upperMap = new HashMap<String, String>();
    for (String k : map.keySet()) {
      String v = map.get(k);
      if (v != null) {
        v = v.trim().toUpperCase();
      }
      upperMap.put(k.toUpperCase(), v);
    }
    return upperMap;
  }
}

