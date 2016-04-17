 package com.htsoft.core.util;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 public class FunctionsUtil
   implements Serializable
 {
   public static char[] crchar = { '\r', '\n' };
   public static String crlf = new String(crchar);
   public static char cr = '\r';
   public static char lf = '\n';
   public static char[] quchar = { '"' };
   public static char[] quBlank = { '"', '"' };
   public static String quote = new String(quchar);
   public static String blankQuote = new String(quBlank);
   public static String fs = File.separator;
   private static char[] hex40 = null;
 
   static { hex40 = new char[4000];
     for (int i = 0; i < 4000; i++)
       hex40[i] = '@';
   }
 
   public static String chopstr(String line, int maxlen)
   {
     StringTokenizer strtoken = new StringTokenizer(line, " ", true);
     int curlen = 0;
     boolean firstoken = true;
     String retline = "";
 
     while (strtoken.hasMoreTokens()) {
       String token = strtoken.nextToken();
       curlen += token.length();
       if ((curlen <= maxlen | firstoken)) {
         retline = retline + token;
       } else {
         retline = retline + crlf + token;
         curlen = token.length();
       }
       if (firstoken)
         firstoken = false;
     }
     return retline;
   }
 
   public static String firstToUpperCase(String string) {
     String post = string.substring(1, string.length());
     char first = string.toUpperCase().charAt(0);
     return first + post;
   }
 
   public static void copyAppend(String fromFile, String toFile)
     throws IOException
   {
     PrintWriter out = new PrintWriter(new FileWriter(toFile, true));
     BufferedReader in = new BufferedReader(new FileReader(fromFile));
     String lineIn;
     while ((lineIn = in.readLine()) != null)
     {
//       String lineIn;
       out.println(lineIn);
     }out.close();
     in.close();
   }
 
   public static String d2x(String numstr, int precision, int scale)
   {
     String retstr = "";
     String signstr = "";
     String pstr = "";
     String sstr = "";
     boolean beginscale = false;
 
     if (numstr.indexOf("-") >= 0)
       signstr = "D";
     else
       signstr = "C";
     int strlen = numstr.length();
 
     for (int i = 0; i < strlen; i++) {
       char onechar = numstr.charAt(i);
       if ((onechar == '+') || (onechar == '-'))
         continue;
       if (onechar == '.')
         beginscale = true;
       else if (beginscale)
         sstr = sstr + onechar;
       else
         pstr = pstr + onechar;
     }
     int diflen = precision - scale;
     if (pstr.length() < diflen) {
       pstr = Pad(pstr, '0', -1 * diflen);
     }
     if (precision % 2 == 0)
       pstr = "0" + pstr;
     if (sstr.length() < scale)
       sstr = Pad(sstr, '0', scale);
     retstr = s2x(pstr + sstr + signstr);
     return retstr;
   }
 
   public static int exp(int in, int power)
   {
     int retval = in;
 
     if (power > 0) {
       for (int i = 1; i < power; i++)
         retval *= in;
     }
     else {
       retval = 1;
     }
     return retval;
   }
 
   public static String getArg(String[] arg, int argNum)
   {
     return (argNum >= 0) && (argNum < arg.length) ? arg[argNum]
       .toUpperCase() : "";
   }
 
   public static String getArg(String[] arg, int argNum, boolean lastArg)
   {
     String retstr = "";
 
     retstr = getArg(arg, argNum);
     if ((lastArg) && (argNum >= 0))
       for (int i = argNum + 1; i < arg.length; i++)
         retstr = retstr + " " + arg[i];
     return retstr.toUpperCase();
   }
 
   public static String getFmtString(String varName, String fmt)
   {
     int numDigits = 0;
     int numDecimals = 0;
 
     StringTokenizer tline = new StringTokenizer(fmt, " (),");
     String tokstr = "";
     boolean isNumDigits = true;
 
     while (tline.hasMoreTokens()) {
       tokstr = tline.nextToken().trim();
       try {
         Integer num = new Integer(tokstr);
         if (isNumDigits) {
           numDigits = num.intValue();
           isNumDigits = false;
         } else {
           numDecimals = num.intValue();
         }
       } catch (Exception localException) {
       }
     }
     return getFmtString(varName, numDigits, numDecimals);
   }
 
   public static String getFmtString(String varName, int numDigits, int numDecimals)
   {
     int numSig = numDigits - numDecimals;
     StringBuffer retStr = new StringBuffer("");
     int numThree = 0;
     for (int c = 0; c < numSig; c++) {
       if (numThree == 3) {
         retStr.insert(0, ",");
         numThree = 0;
       }
       if (c == 0)
         retStr.insert(0, "0");
       else
         retStr.insert(0, "#");
       numThree++;
     }
 
     for (int c = 0; c < numDecimals; c++) {
       if (c == 0)
         retStr.append(".0");
       else {
         retStr.append("0");
       }
     }
     return "   private static String " + varName + "Fmt " + " = " + 
       quote + retStr.toString() + quote + ";";
   }
 
   public static String getFmtString(String varName, String digits, String decimals)
   {
     int numDigits = new Integer(digits).intValue();
     int numDecimals = new Integer(decimals).intValue();
     return getFmtString(varName, numDigits, numDecimals);
   }
 
   public static String getOpt(String options, String searchStr) {
     String retstr = "";
     int i = options.indexOf(searchStr);
     if (i >= 0) {
       int e = options.length();
       int s = i + searchStr.length();
       int temp = options.indexOf(' ', s);
       if ((temp >= 0) && (temp < e))
         e = temp;
       temp = options.indexOf('/', s);
       if ((temp >= 0) && (temp < e))
         e = temp;
       retstr = options.substring(s, e);
     }
     return retstr;
   }
 
   public static String getParameter(String parmStr, String searchStr, String separator)
   {
     String result = "";
     if ((searchStr == null) || (parmStr == null)) {
       return result;
     }
     StringTokenizer tline = new StringTokenizer(parmStr, separator);
     while (tline.hasMoreTokens()) {
       String value = tline.nextToken().trim();
       if (value.equals(searchStr)) {
         if (!tline.hasMoreTokens()) break;
         result = tline.nextToken().trim();
         break;
       }
     }
     return result;
   }
 
   public static String getScale(String fmt)
   {
     StringBuffer retStr = new StringBuffer("");
     int numDigits = 0;
     int numDecimals = 0;
 
     StringTokenizer tline = new StringTokenizer(fmt, " (),");
     String tokstr = "";
     boolean isNumDigits = true;
 
     while (tline.hasMoreTokens()) {
       tokstr = tline.nextToken().trim();
       try {
         Integer num = new Integer(tokstr);
         if (isNumDigits) {
           numDigits = num.intValue();
           isNumDigits = false;
         } else {
           numDecimals = num.intValue();
         }
       } catch (Exception localException) {
       }
     }
     Integer numDec = new Integer(numDecimals);
     return numDec.toString();
   }
 
   public static void globalChange(String target, String fromText, String toText, boolean includeSubDir)
     throws Exception
   {
     File targetFile = new File(target);
     if (targetFile.isFile()) {
       searchReplace(target, fromText, toText, "");
     } else if (targetFile.isDirectory()) {
       String[] fl = targetFile.list();
       for (int i = 0; i < fl.length; i++) {
         File subfile = new File(targetFile, fl[i]);
         if ((subfile.isFile()) || (includeSubDir))
           globalChange(subfile.getAbsolutePath(), fromText, toText, 
             includeSubDir);
       }
     } else {
       throw new Exception("Error in reading " + target);
     }
   }
 
   public static int hexval(char in)
   {
     int retval = in;
     if (retval < 58)
       retval = in & 0xF;
     else
       retval = (in & 0xF) + '\t';
     return retval;
   }
 
   public static String i2x(String numstr, int outlen)
   {
     Integer integval = new Integer(numstr);
     int intval = integval.intValue();
     String wrkstr = Integer.toHexString(intval);
     wrkstr = wrkstr.toUpperCase();
     int strlen = wrkstr.length();
     int intlen = outlen * 2;
     if (strlen < intlen)
       wrkstr = Pad(wrkstr, '0', -1 * intlen);
     else if (strlen > intlen)
       wrkstr = wrkstr.substring(strlen - intlen);
     String retstr = s2x(wrkstr);
     return retstr;
   }
 
   public static void searchReplace(String infilename, String srchstr, String replstr, String parmstr)
   {
     String ctlparms = parmstr.toUpperCase();
     boolean delsw = ctlparms.indexOf("/D") >= 0;
     boolean addsw = ctlparms.indexOf("/A") >= 0;
     try
     {
       File chgfile = new File(infilename);
       String basefilename = infilename;
 
       if (basefilename.indexOf(".") > 0) {
         StringTokenizer tline = new StringTokenizer(basefilename, ".");
         basefilename = tline.nextToken().trim();
       }
 
       String bkpfilename = basefilename + ".BAK";
       File bakfile = new File(bkpfilename);
 
       if (infilename.equals(bkpfilename)) {
         return;
       }
 
       if (bakfile.exists()) {
         bakfile.delete();
       }
       chgfile.renameTo(bakfile);
 
       PrintWriter out = new PrintWriter(new FileWriter(infilename));
 
       BufferedReader in = new BufferedReader(new FileReader(bkpfilename));
       String line = in.readLine();
       int numchg = 0;
       while (line != null) {
         int srchpos = line.indexOf(srchstr);
         if (srchpos >= 0) {
           numchg++;
           if (delsw)
             line = replstr;
           else {
             line = replaceString(line, srchstr, replstr);
           }
         }
         out.println(line);
         line = in.readLine();
       }
       if ((addsw) && (numchg == 0) && (!replstr.equals(""))) {
         out.println(replstr);
       }
 
       in.close();
       out.close();
     }
     catch (Exception e) {
       System.out.println("Error in jsrchrepl  --> " + crlf + 
         e.toString());
       e.printStackTrace();
       System.exit(12);
     }
   }
 
   public static void logMsg(String msg, String logFile) throws IOException
   {
     PrintWriter out = new PrintWriter(new FileWriter(logFile, true));
     out.println(msg);
     out.close();
   }
 
   public static String makeBusName(String newStr)
   {
     StringBuffer retstr = new StringBuffer("");
     String specialCharacters = " _/.,#'%-";
     String numericCharacters = "0123456789";
     int strlen = newStr.length();
     char[] onechar = new char[1];
     boolean nextUpper = true;
 
     for (int i = 0; i < strlen; i++) {
       onechar[0] = newStr.charAt(i);
       String charString = new String(onechar);
       if (specialCharacters.indexOf(charString) >= 0) {
         if (charString.equals("'"))
           nextUpper = false;
         else
           nextUpper = true;
         retstr.append(charString);
       }
       else if (nextUpper) {
         retstr.append(charString.toUpperCase());
         nextUpper = false;
       } else {
         retstr.append(charString.toLowerCase());
       }
 
     }
 
     if (numericCharacters.indexOf(String.valueOf(retstr.charAt(0))) >= 0) {
       retstr = retstr.insert(0, 'X');
     }
     return retstr.toString();
   }
 
   public static String makeClassName(String newStr)
   {
     return makeFirstLetterUpperCase(makeVarName(newStr));
   }
 
   public static String makeFirstLetterLowerCase(String newStr)
   {
     if (newStr.length() == 0) {
       return newStr;
     }
     char[] oneChar = new char[1];
     oneChar[0] = newStr.charAt(0);
     String firstChar = new String(oneChar);
     return firstChar.toLowerCase() + newStr.substring(1);
   }
 
   public static String makeFirstLetterUpperCase(String newStr)
   {
     if (newStr.length() == 0) {
       return newStr;
     }
     char[] oneChar = new char[1];
     oneChar[0] = newStr.charAt(0);
     String firstChar = new String(oneChar);
     return firstChar.toUpperCase() + newStr.substring(1);
   }
 
   public static String makeVarName(String newStr)
   {
     boolean useCaseSensitive = true;
 
     StringBuffer retstr = new StringBuffer("");
     String specialCharacters = " _/.,#'%-";
     String numericCharacters = "0123456789";
     int strlen = newStr.length();
     char[] onechar = new char[1];
     boolean nextUpper = false;
     boolean firstChar = true;
 
     for (int i = 0; i < strlen; i++) {
       onechar[0] = newStr.charAt(i);
       String charString = new String(onechar);
       if (!useCaseSensitive) charString = charString.toLowerCase();
       if (specialCharacters.indexOf(charString) >= 0) {
         if (charString.equals("'"))
           nextUpper = false;
         else
           nextUpper = true;
       }
       else if (nextUpper) {
         if (!firstChar)
           retstr.append(charString.toUpperCase());
         else
           retstr.append(charString);
         firstChar = false;
         nextUpper = false;
       } else {
         retstr.append(charString);
         firstChar = false;
       }
 
     }
 
     if (numericCharacters.indexOf(String.valueOf(retstr.charAt(0))) >= 0) {
       retstr = retstr.insert(0, 'x');
     }
     return retstr.toString();
   }
 
   public static String ovly(String line, String ovlystr, int ovlypos, int ovlylen)
   {
     int ovlystrlen = ovlystr.length();
     int actlen = ovlylen;
     int targlen = line.length();
     int templen = 0;
     String retline = line;
 
     if (actlen == 0)
       actlen = ovlystr.length();
     if (actlen == -1) {
       actlen = -1 * ovlystr.length();
     }
 
     if (actlen >= 0)
       templen = actlen + ovlypos - 1;
     if (templen > targlen) {
       targlen = templen;
     }
     char[] newlinechar = new char[targlen];
     newlinechar = line.toCharArray();
 
     for (int i = 0; i < ovlystrlen; i++) {
       if (actlen > 0) {
         int j = ovlypos + i - 1;
         if (((j >= 0 ? 1 : 0) & (j < targlen ? 1 : 0)) != 0)
           newlinechar[j] = ovlystr.charAt(i);
       } else if (actlen < 0) {
         int k = ovlystrlen - i - 1;
         int j = ovlypos - 1 - i;
         if (((j >= 0 ? 1 : 0) & (j < targlen ? 1 : 0)) != 0)
           newlinechar[j] = ovlystr.charAt(k);
       }
       retline = new String(newlinechar);
     }
     return retline;
   }
 
   public static String Pad(String line, char padchar, int len)
   {
     int templen = len;
     if (templen < 0)
       templen *= -1;
     int numchar = templen - line.length();
     String retline = line;
     if (numchar > 0) {
       for (int i = 0; i < numchar; i++)
         if (len >= 0)
           retline = retline + padchar;
         else
           retline = padchar + retline;
     }
     else {
       retline = line.substring(0, templen);
     }
     return retline;
   }
 
   public static String PadHex40(String line, int len) {
     int currlen = line.length();
     int diff = len - currlen;
     int maxlen = hex40.length;
     if ((diff <= maxlen) && (diff > 0)) {
       return line + new String(hex40, 0, diff);
     }
 
     char padChar = '@';
     return Pad(line, padChar, len);
   }
 
   public static String Padn(String line, char padchar, int len)
   {
     int actualLen = len;
     if (len < 0)
       actualLen = len * -1;
     if (line.length() > actualLen) {
       return line;
     }
     return Pad(line, padchar, len);
   }
 
   public static final Hashtable parseStringToHashtable(String aQueryString, String delimiter)
   {
     Hashtable result = new Hashtable();
 
     if (aQueryString == null) {
       return result;
     }
     StringTokenizer tline = new StringTokenizer(aQueryString, delimiter);
     while (tline.hasMoreTokens()) {
       String tokstr = tline.nextToken().trim();
       if (!tokstr.equals("")) {
         result.put(tokstr, "");
       }
     }
     return result;
   }
 
   public static boolean patternMatch(String str, String pattern)
   {
     if (pattern.equals("*")) {
       return true;
     }
 
     if (pattern.startsWith("*")) {
       String p1 = pattern.substring(1);
       for (int i = 0; i < str.length(); i++)
         if (patternMatch(str.substring(i), p1))
           return true;
       return false;
     }
 
     int t = pattern.indexOf("*");
 
     if (t < 0)
     {
       return str.equals(pattern);
     }
 
     String p2 = pattern.substring(0, t);
     return (str.startsWith(p2)) && (patternMatch(str.substring(t), 
       pattern.substring(t)));
   }
 
   public static String replaceString(String in, String from, String to)
   {
     int i = in.indexOf(from);
     if ((i < 0) || (from.length() == 0)) {
       return in;
     }
     return in.substring(0, i) + to + 
       replaceString(in.substring(i + from.length()), from, to);
   }
 
   public static String s2x(String str)
   {
     String retstr = "";
     int strlen = str.length();
 
     for (int i = 0; i < strlen; i++) {
       int hibyte = hexval(str.charAt(i)) * 16;
       i++;
       int lobyte;
//       int lobyte;
       if (i >= strlen)
         lobyte = 0;
       else
         lobyte = hexval(str.charAt(i));
       retstr = retstr + (char)(hibyte + lobyte);
     }
     return retstr;
   }
 
   public static String getCurrentDate() {
     Date aDate = new Date();
     return aDate.toString();
   }
 
   public static void sortStringArray(String[] srcArray)
   {
     Collator c = Collator.getInstance();
     c.setStrength(0);
     int arrayLength = srcArray.length;
     for (int i = arrayLength; i > 1; i--)
       for (int j = 0; j < i - 1; j++)
         if (c.compare(srcArray[j], srcArray[(j + 1)]) > 0) {
           String temp = srcArray[j];
           srcArray[j] = srcArray[(j + 1)];
           srcArray[(j + 1)] = temp;
         }
   }
 
   public static String stripspecial(String newStr)
   {
     StringBuffer retstr = new StringBuffer("");
     String specialCharacters = " _/.,#'-%";
     int strlen = newStr.length();
     char[] onechar = new char[1];
     for (int i = 0; i < strlen; i++) {
       onechar[0] = newStr.charAt(i);
       String charString = new String(onechar);
       if (specialCharacters.indexOf(charString) >= 0) {
         continue;
       }
       retstr.append(charString);
     }
 
     return retstr.toString();
   }
 
   public static String stripzero(String numstr)
   {
     String retstr = "";
     boolean firstnz = true;
     int strlen = numstr.length();
 
     for (int i = 0; i < strlen; i++) {
       char onechar = numstr.charAt(i);
       if ((firstnz) && (onechar == '0')) {
         continue;
       }
       retstr = retstr + onechar;
       firstnz = false;
     }
 
     if (retstr.equals(""))
       retstr = "0";
     return retstr;
   }
 
   public static String x2d(ByteArrayInputStream inbytes, int precision, int scale)
   {
     String retstr = "";
     String negsign = "";
     String lastchar = "";
     int onemore = precision % 2;
     if (onemore == 1)
       onemore = 0;
     else {
       onemore = 1;
     }
     int neglen = -1 * (precision + scale);
     int diflen = precision - scale;
     String wrkstr = x2s(inbytes);
     int wrklen = wrkstr.length();
     lastchar = wrkstr.substring(wrklen - 1);
     if (lastchar.equals("D"))
       negsign = "-";
     wrkstr = wrkstr.substring(0, wrklen - 1);
     if (diflen > 0)
       retstr = wrkstr.substring(0, diflen + onemore);
     if (scale > 0)
       retstr = retstr + "." + wrkstr.substring(diflen + onemore);
     retstr = negsign + stripzero(retstr);
     return retstr;
   }
 
   public static String x2d(String numstr, int precision, int scale)
   {
     String retstr = "";
     String negsign = "";
     String lastchar = "";
     int onemore = precision % 2;
     if (onemore == 1)
       onemore = 0;
     else {
       onemore = 1;
     }
     int neglen = -1 * (precision + scale);
     int diflen = precision - scale;
     String wrkstr = x2s(numstr);
     int wrklen = wrkstr.length();
     lastchar = wrkstr.substring(wrklen - 1);
     if (lastchar.equals("D"))
       negsign = "-";
     wrkstr = wrkstr.substring(0, wrklen - 1);
     if (diflen > 0)
       retstr = wrkstr.substring(0, diflen + onemore);
     if (scale > 0)
       retstr = retstr + "." + wrkstr.substring(diflen + onemore);
     retstr = negsign + stripzero(retstr);
     return retstr;
   }
 
   public static String x2i(ByteArrayInputStream inbytes, boolean smallint)
   {
     String retstr = "";
     try {
       String wrkstr = x2s(inbytes);
       int strlen = wrkstr.length();
       int tempval = 0;
       long retval = 0L;
       for (int i = 0; i < strlen; i++) {
         int k = strlen - i - 1;
         int onebyte = hexval(wrkstr.charAt(k));
         tempval = onebyte * exp(16, i);
         retval += tempval;
       }
       if (smallint) {
         if (retval > 32767L)
           retval -= 65536L;
       }
       else if (retval > 2147483647L) {
         retval -= 4294967296L;
       }
       Long integval = new Long(retval);
       retstr = integval.toString();
     }
     catch (Exception e) {
       return "Error: " + e;
     }
     return retstr;
   }
 
   public static String x2i(String numstr, boolean smallint)
   {
     String retstr = "";
     try {
       String wrkstr = x2s(numstr);
       int strlen = wrkstr.length();
       int tempval = 0;
       long retval = 0L;
       for (int i = 0; i < strlen; i++) {
         int k = strlen - i - 1;
         int onebyte = hexval(wrkstr.charAt(k));
         tempval = onebyte * exp(16, i);
         retval += tempval;
       }
       if (smallint) {
         if (retval > 32767L)
           retval -= 65536L;
       }
       else if (retval > 2147483647L) {
         retval -= 4294967296L;
       }
       Long integval = new Long(retval);
       retstr = integval.toString();
     }
     catch (Exception e) {
       return "Error: " + e;
     }
     return retstr;
   }
 
   public static String x2s(ByteArrayInputStream inbytes)
   {
     String retstr = "";
     int strlen = inbytes.available();
     int targlen = strlen * 2;
     String tempstr = "";
     for (int i = 0; i < strlen; i++) {
       int intval = inbytes.read();
 
       tempstr = Integer.toHexString(intval);
       if (tempstr.length() == 1)
         retstr = retstr + "0" + tempstr;
       else
         retstr = retstr + tempstr;
     }
     return retstr.toUpperCase();
   }
 
   public static Properties extractProperties(Properties props, String mask)
   {
     Properties results = new Properties();
     Enumeration enum1 = props.keys();
     while (enum1.hasMoreElements()) {
       String aKey = (String)enum1.nextElement();
       if ((aKey != null) && (aKey.indexOf(mask) >= 0)) {
         Object aValue = props.getProperty(aKey);
         results.put(aKey, aValue);
       }
     }
     return results;
   }
 
   public static List extractPropertyValues(Properties props)
   {
     List results = new ArrayList();
 
     Enumeration enum1 = props.elements();
     while (enum1.hasMoreElements()) {
       results.add(enum1.nextElement());
     }
     return results;
   }
 
   public static void createDir(File inFile) {
     if (inFile.exists()) {
       return;
     }
     File inDir = new File(inFile.getParent());
     if (!inDir.isDirectory())
       inDir.mkdirs();
   }
 
   public static boolean hasMask(String inString, String mask) {
     boolean hasSubstring = inString.toUpperCase().indexOf(
       mask.toUpperCase()) >= 0;
     return hasSubstring;
   }
 
   public static String makeLowerCase(String aString) {
     return aString.toLowerCase();
   }
 
   public static String makeUpperCase(String aString) {
     return aString.toUpperCase();
   }
 
   public static String x2s(String hexstr)
   {
     String retstr = "";
     int strlen = hexstr.length();
     int targlen = strlen * 2;
     String tempstr = "";
     for (int i = 0; i < strlen; i++) {
       int intval = hexstr.charAt(i);
       tempstr = Integer.toHexString(intval);
       if (tempstr.length() == 1)
         retstr = retstr + "0" + tempstr;
       else
         retstr = retstr + tempstr;
     }
     return retstr.toUpperCase();
   }
 
   public static String quote(String aString) {
     return quote + aString + quote;
   }
 }


