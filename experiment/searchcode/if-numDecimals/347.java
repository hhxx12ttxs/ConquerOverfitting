package code.generator.common;

/**
 * Created by IntelliJ IDEA.
 * User: hongbing.zhang
 * Date: 12-8-16
 * Time: 下午5:10
 * To change this template use File | Settings | File Templates.
 */
import code.generator.db.SqlColumn;
import code.generator.db.SqlTable;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

// Referenced classes of package code.generator.common:
//			ApplicationObject, InputStreamMonitor, StringUtil, ApplicationProperties,
//			TemplateProcessor, PackageNameResolver, ListHashtable

public class Functions extends code.generator.common.ApplicationObject
{

	public static char crchar[] = {
		'\r', '\n'
	};
	public static String crlf;
	public static char cr = '\r';
	public static char lf = '\n';
	public static char quchar[] = {
		'"'
	};
	public static char quBlank[] = {
		'"', '"'
	};
	public static String quote;
	public static String blankQuote;
	public static String fs;
	private static char hex40[];

	public Functions()
	{
	}

	public static String chopstr(String line, int maxlen)
	{
		StringTokenizer strtoken = new StringTokenizer(line, " ", true);
		int curlen = 0;
		boolean firstoken = true;
		String retline = "";
		while (strtoken.hasMoreTokens())
		{
			String token = strtoken.nextToken();
			curlen += token.length();
			if ((curlen <= maxlen) | firstoken)
			{
				retline = retline + token;
			} else
			{
				retline = retline + crlf + token;
				curlen = token.length();
			}
			if (firstoken)
				firstoken = false;
		}
		return retline;
	}

	public static String firstToUpperCase(String string)
	{
		String post = string.substring(1, string.length());

		String first = String.valueOf(string.charAt(0)).toUpperCase();
		return first + post;
	}

	public static void copyAppend(String fromFile, String toFile)
		throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(toFile, true));
		BufferedReader in = new BufferedReader(new FileReader(fromFile));
		String lineIn;
		while ((lineIn = in.readLine()) != null)
			out.println(lineIn);
		out.close();
		in.close();
	}

	public static void copyfile(File fileFrom, File fileTo, boolean createdir)
		throws FileNotFoundException, IOException
	{
		if (!fileFrom.canRead())
			throw new IOException("Cannot read from file " + fileFrom.getPath());
		if (createdir)
		{
			File todir = new File(fileTo.getParent());
			if (!todir.isDirectory())
				todir.mkdirs();
		}
		if (code.generator.common.Functions.execproc("filecopy " + fileFrom.getPath() + " " + fileTo.getPath(), true) != 0)
			throw new IOException("Failed to copy to file " + fileTo.getPath());
		else
			return;
	}

	public static void copyfile(String fileFrom, String fileTo, boolean createdir)
		throws FileNotFoundException, IOException
	{
		code.generator.common.Functions.copyfile(new File(fileFrom), new File(fileTo), createdir);
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
		for (int i = 0; i < strlen; i++)
		{
			char onechar = numstr.charAt(i);
			if (onechar != '+' && onechar != '-')
				if (onechar == '.')
					beginscale = true;
				else
				if (beginscale)
					sstr = sstr + onechar;
				else
					pstr = pstr + onechar;
		}

		int diflen = precision - scale;
		if (pstr.length() < diflen)
			pstr = code.generator.common.Functions.Pad(pstr, '0', -1 * diflen);
		if (precision % 2 == 0)
			pstr = "0" + pstr;
		if (sstr.length() < scale)
			sstr = code.generator.common.Functions.Pad(sstr, '0', scale);
		retstr = code.generator.common.Functions.s2x(pstr + sstr + signstr);
		return retstr;
	}

	public static int execproc(String cmd, boolean waitfor)
	{
		int retcode = 0;
		try
		{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(cmd);
			new InputStreamMonitor(p.getErrorStream());
			new InputStreamMonitor(p.getInputStream());
			if (waitfor)
				retcode = p.waitFor();
		}
		catch (Exception e)
		{
			System.out.println("Error in executing " + cmd + " -->" + e);
			System.exit(1);
		}
		return retcode;
	}

	public static int exp(int in, int power)
	{
		int retval = in;
		if (power > 0)
		{
			for (int i = 1; i < power; i++)
				retval *= in;

		} else
		{
			retval = 1;
		}
		return retval;
	}

	public static String getArg(String arg[], int argNum)
	{
		return argNum < 0 || argNum >= arg.length ? "" : arg[argNum].toUpperCase();
	}

	public static String getArg(String arg[], int argNum, boolean lastArg)
	{
		String retstr = "";
		retstr = code.generator.common.Functions.getArg(arg, argNum);
		if (lastArg && argNum >= 0)
		{
			for (int i = argNum + 1; i < arg.length; i++)
				retstr = retstr + " " + arg[i];

		}
		return retstr.toUpperCase();
	}

	public static String getFmtString(String varName, String fmt)
	{
		int numDigits = 0;
		int numDecimals = 0;
		StringTokenizer tline = new StringTokenizer(fmt, " (),");
		String tokstr = "";
		boolean isNumDigits = true;
		while (tline.hasMoreTokens())
		{
			tokstr = tline.nextToken().trim();
			try
			{
				Integer num = new Integer(tokstr);
				if (isNumDigits)
				{
					numDigits = num.intValue();
					isNumDigits = false;
				} else
				{
					numDecimals = num.intValue();
				}
			}
			catch (Exception exception) { }
		}
		return code.generator.common.Functions.getFmtString(varName, numDigits, numDecimals);
	}

	public static String getFmtString(String varName, int numDigits, int numDecimals)
	{
		int numSig = numDigits - numDecimals;
		StringBuffer retStr = new StringBuffer("");
		int numThree = 0;
		for (int c = 0; c < numSig; c++)
		{
			if (numThree == 3)
			{
				retStr.insert(0, ",");
				numThree = 0;
			}
			if (c == 0)
				retStr.insert(0, "0");
			else
				retStr.insert(0, "#");
			numThree++;
		}

		for (int c = 0; c < numDecimals; c++)
			if (c == 0)
				retStr.append(".0");
			else
				retStr.append("0");

		return "   private static String " + varName + "Fmt " + " = " + quote + retStr.toString() + quote + ";";
	}

	public static String getFmtString(String varName, String digits, String decimals)
	{
		int numDigits = (new Integer(digits)).intValue();
		int numDecimals = (new Integer(decimals)).intValue();
		return code.generator.common.Functions.getFmtString(varName, numDigits, numDecimals);
	}

	public static String getOpt(String options, String searchStr)
	{
		String retstr = "";
		int i = options.indexOf(searchStr);
		if (i >= 0)
		{
			int e = options.length();
			int s = i + searchStr.length();
			int temp = options.indexOf(' ', s);
			if (temp >= 0 && temp < e)
				e = temp;
			temp = options.indexOf('/', s);
			if (temp >= 0 && temp < e)
				e = temp;
			retstr = options.substring(s, e);
		}
		return retstr;
	}

	public static String getParameter(String parmStr, String searchStr, String separator)
	{
		String result = "";
		if (searchStr == null || parmStr == null)
			return result;
		for (StringTokenizer tline = new StringTokenizer(parmStr, separator); tline.hasMoreTokens();)
		{
			String value = tline.nextToken().trim();
			if (value.equals(searchStr))
			{
				if (tline.hasMoreTokens())
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
		while (tline.hasMoreTokens())
		{
			tokstr = tline.nextToken().trim();
			try
			{
				Integer num = new Integer(tokstr);
				if (isNumDigits)
				{
					numDigits = num.intValue();
					isNumDigits = false;
				} else
				{
					numDecimals = num.intValue();
				}
			}
			catch (Exception exception) { }
		}
		Integer numDec = new Integer(numDecimals);
		return numDec.toString();
	}

	public static void globalChange(String target, String fromText, String toText, boolean includeSubDir)
		throws Exception
	{
		File targetFile = new File(target);
		if (targetFile.isFile())
			code.generator.common.Functions.searchReplace(target, fromText, toText, "");
		else
		if (targetFile.isDirectory())
		{
			String fl[] = targetFile.list();
			for (int i = 0; i < fl.length; i++)
			{
				File subfile = new File(targetFile, fl[i]);
				if (subfile.isFile() || includeSubDir)
					code.generator.common.Functions.globalChange(subfile.getAbsolutePath(), fromText, toText, includeSubDir);
			}

		} else
		{
			throw new Exception("Error in reading " + target);
		}
	}

	public static int hexval(char in)
	{
		int retval = in;
		if (retval < 58)
			retval = in & 0xf;
		else
			retval = (in & 0xf) + 9;
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
			wrkstr = code.generator.common.Functions.Pad(wrkstr, '0', -1 * intlen);
		else
		if (strlen > intlen)
			wrkstr = wrkstr.substring(strlen - intlen);
		String retstr = code.generator.common.Functions.s2x(wrkstr);
		return retstr;
	}

	public static void searchReplace(String infilename, String srchstr, String replstr, String parmstr)
	{
		boolean delsw;
		boolean addsw;
		String ctlparms = parmstr.toUpperCase();
		delsw = ctlparms.indexOf("/D") >= 0;
		addsw = ctlparms.indexOf("/A") >= 0;
		File chgfile;
		String bkpfilename;
		File bakfile;
		chgfile = new File(infilename);
		String basefilename = infilename;
		if (basefilename.indexOf(".") > 0)
		{
			StringTokenizer tline = new StringTokenizer(basefilename, ".");
			basefilename = tline.nextToken().trim();
		}
		bkpfilename = basefilename + ".BAK";
		bakfile = new File(bkpfilename);
		if (infilename.equals(bkpfilename))
			return;
		try
		{
			if (bakfile.exists())
				bakfile.delete();
			chgfile.renameTo(bakfile);
			PrintWriter out = new PrintWriter(new FileWriter(infilename));
			BufferedReader in = new BufferedReader(new FileReader(bkpfilename));
			String line = in.readLine();
			int numchg = 0;
			for (; line != null; line = in.readLine())
			{
				int srchpos = line.indexOf(srchstr);
				if (srchpos >= 0)
				{
					numchg++;
					if (delsw)
						line = replstr;
					else
						line = code.generator.common.Functions.replaceString(line, srchstr, replstr);
				}
				out.println(line);
			}

			if (addsw && numchg == 0 && !replstr.equals(""))
				out.println(replstr);
			in.close();
			out.close();
		}
		catch (Exception e)
		{
			System.out.println("Error in jsrchrepl  --> " + crlf + e.toString());
			e.printStackTrace();
			System.exit(12);
		}
		return;
	}

	public static void logMsg(String msg, String logFile)
		throws IOException
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
		char onechar[] = new char[1];
		boolean nextUpper = true;
		for (int i = 0; i < strlen; i++)
		{
			onechar[0] = newStr.charAt(i);
			String charString = new String(onechar);
			if (specialCharacters.indexOf(charString) >= 0)
			{
				if (charString.equals("'"))
					nextUpper = false;
				else
					nextUpper = true;
				retstr.append(charString);
			} else
			if (nextUpper)
			{
				retstr.append(charString.toUpperCase());
				nextUpper = false;
			} else
			{
				retstr.append(charString.toLowerCase());
			}
		}

		if (numericCharacters.indexOf(String.valueOf(retstr.charAt(0))) >= 0)
			retstr = retstr.insert(0, 'X');
		return retstr.toString();
	}

	public static String makeClassName(String newStr)
	{
		return code.generator.common.Functions.makeFirstLetterUpperCase(code.generator.common.Functions.makeVarName(newStr));
	}

	public static String makeFirstLetterLowerCase(String newStr)
	{
		if (newStr.length() == 0)
		{
			return newStr;
		} else
		{
			char oneChar[] = new char[1];
			oneChar[0] = newStr.charAt(0);
			String firstChar = new String(oneChar);
			return firstChar.toLowerCase() + newStr.substring(1);
		}
	}

	public static String makeFirstLetterUpperCase(String newStr)
	{
		if (newStr.length() == 0)
		{
			return newStr;
		} else
		{
			char oneChar[] = new char[1];
			oneChar[0] = newStr.charAt(0);
			String firstChar = new String(oneChar);
			return firstChar.toUpperCase() + newStr.substring(1);
		}
	}

	public static String makeVarName(String newStr)
	{
		StringBuffer retstr = new StringBuffer("");
		String specialCharacters = " _/.,#'%-";
		String numericCharacters = "0123456789";
		int strlen = newStr.length();
		char onechar[] = new char[1];
		boolean nextUpper = false;
		boolean firstChar = true;
		for (int i = 0; i < strlen; i++)
		{
			onechar[0] = newStr.charAt(i);
			String charString = new String(onechar);
			if (specialCharacters.indexOf(charString) >= 0)
			{
				if (charString.equals("'"))
					nextUpper = false;
				else
					nextUpper = true;
			} else
			if (nextUpper)
			{
				if (!firstChar)
					retstr.append(charString.toUpperCase());
				else
					retstr.append(charString.toLowerCase());
				firstChar = false;
				nextUpper = false;
			} else
			{
				retstr.append(charString.toLowerCase());
				firstChar = false;
			}
		}

		if (numericCharacters.indexOf(String.valueOf(retstr.charAt(0))) >= 0)
			retstr = retstr.insert(0, 'x');
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
		if (actlen == -1)
			actlen = -1 * ovlystr.length();
		if (actlen >= 0)
			templen = (actlen + ovlypos) - 1;
		if (templen > targlen)
			targlen = templen;
		char newlinechar[] = new char[targlen];
		newlinechar = line.toCharArray();
		for (int i = 0; i < ovlystrlen; i++)
		{
			if (actlen > 0)
			{
				int j = (ovlypos + i) - 1;
				if ((j >= 0) & (j < targlen))
					newlinechar[j] = ovlystr.charAt(i);
			} else
			if (actlen < 0)
			{
				int k = ovlystrlen - i - 1;
				int j = ovlypos - 1 - i;
				if ((j >= 0) & (j < targlen))
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
		if (numchar > 0)
		{
			for (int i = 0; i < numchar; i++)
				if (len >= 0)
					retline = retline + padchar;
				else
					retline = padchar + retline;

		} else
		{
			retline = line.substring(0, templen);
		}
		return retline;
	}

	public static String PadHex40(String line, int len)
	{
		int currlen = line.length();
		int diff = len - currlen;
		int maxlen = hex40.length;
		if (diff <= maxlen && diff > 0)
		{
			return line + new String(hex40, 0, diff);
		} else
		{
			char padChar = '@';
			return code.generator.common.Functions.Pad(line, padChar, len);
		}
	}

	public static String Padn(String line, char padchar, int len)
	{
		int actualLen = len;
		if (len < 0)
			actualLen = len * -1;
		if (line.length() > actualLen)
			return line;
		else
			return code.generator.common.Functions.Pad(line, padchar, len);
	}

	public static final Hashtable parseStringToHashtable(String aQueryString, String delimiter)
	{
		Hashtable result = new Hashtable();
		if (aQueryString == null)
			return result;
		for (StringTokenizer tline = new StringTokenizer(aQueryString, delimiter); tline.hasMoreTokens();)
		{
			String tokstr = tline.nextToken().trim();
			if (!tokstr.equals(""))
				result.put(tokstr, "");
		}

		return result;
	}

	public static boolean patternMatch(String str, String pattern)
	{
		if (pattern.equals("*"))
			return true;
		if (pattern.startsWith("*"))
		{
			String p1 = pattern.substring(1);
			for (int i = 0; i < str.length(); i++)
				if (code.generator.common.Functions.patternMatch(str.substring(i), p1))
					return true;

			return false;
		}
		int t = pattern.indexOf("*");
		if (t < 0)
			return str.equals(pattern);
		String p2 = pattern.substring(0, t);
		return str.startsWith(p2) && code.generator.common.Functions.patternMatch(str.substring(t), pattern.substring(t));
	}

	public static String replaceString(String in, String from, String to)
	{
		int i = in.indexOf(from);
		if (i < 0 || from.length() == 0)
			return in;
		else
			return in.substring(0, i) + to + code.generator.common.Functions.replaceString(in.substring(i + from.length()), from, to);
	}

	public static String s2x(String str)
	{
		String retstr = "";
		int strlen = str.length();
		for (int i = 0; i < strlen; i++)
		{
			int hibyte = code.generator.common.Functions.hexval(str.charAt(i)) * 16;
			int lobyte;
			if (++i >= strlen)
				lobyte = 0;
			else
				lobyte = code.generator.common.Functions.hexval(str.charAt(i));
			retstr = retstr + (char)(hibyte + lobyte);
		}

		return retstr;
	}

	public static String getCurrentDate()
	{
		Date aDate = new Date();
		return aDate.toString();
	}

	public static void sortStringArray(String srcArray[])
	{
		Collator c = Collator.getInstance();
		c.setStrength(0);
		int arrayLength = srcArray.length;
		for (int i = arrayLength; i > 1; i--)
		{
			for (int j = 0; j < i - 1; j++)
				if (c.compare(srcArray[j], srcArray[j + 1]) > 0)
				{
					String temp = srcArray[j];
					srcArray[j] = srcArray[j + 1];
					srcArray[j + 1] = temp;
				}

		}

	}

	public static String stripspecial(String newStr)
	{
		StringBuffer retstr = new StringBuffer("");
		String specialCharacters = " _/.,#'-%";
		int strlen = newStr.length();
		char onechar[] = new char[1];
		for (int i = 0; i < strlen; i++)
		{
			onechar[0] = newStr.charAt(i);
			String charString = new String(onechar);
			if (specialCharacters.indexOf(charString) < 0)
				retstr.append(charString);
		}

		return retstr.toString();
	}

	public static String stripzero(String numstr)
	{
		String retstr = "";
		boolean firstnz = true;
		int strlen = numstr.length();
		for (int i = 0; i < strlen; i++)
		{
			char onechar = numstr.charAt(i);
			if (!firstnz || onechar != '0')
			{
				retstr = retstr + onechar;
				firstnz = false;
			}
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
		else
			onemore = 1;
		int neglen = -1 * (precision + scale);
		int diflen = precision - scale;
		String wrkstr = code.generator.common.Functions.x2s(inbytes);
		int wrklen = wrkstr.length();
		lastchar = wrkstr.substring(wrklen - 1);
		if (lastchar.equals("D"))
			negsign = "-";
		wrkstr = wrkstr.substring(0, wrklen - 1);
		if (diflen > 0)
			retstr = wrkstr.substring(0, diflen + onemore);
		if (scale > 0)
			retstr = retstr + "." + wrkstr.substring(diflen + onemore);
		retstr = negsign + code.generator.common.Functions.stripzero(retstr);
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
		else
			onemore = 1;
		int neglen = -1 * (precision + scale);
		int diflen = precision - scale;
		String wrkstr = code.generator.common.Functions.x2s(numstr);
		int wrklen = wrkstr.length();
		lastchar = wrkstr.substring(wrklen - 1);
		if (lastchar.equals("D"))
			negsign = "-";
		wrkstr = wrkstr.substring(0, wrklen - 1);
		if (diflen > 0)
			retstr = wrkstr.substring(0, diflen + onemore);
		if (scale > 0)
			retstr = retstr + "." + wrkstr.substring(diflen + onemore);
		retstr = negsign + code.generator.common.Functions.stripzero(retstr);
		return retstr;
	}

	public static String x2i(ByteArrayInputStream inbytes, boolean smallint)
	{
		String retstr = "";
		try
		{
			String wrkstr = code.generator.common.Functions.x2s(inbytes);
			int strlen = wrkstr.length();
			int tempval = 0;
			long retval = 0L;
			for (int i = 0; i < strlen; i++)
			{
				int k = strlen - i - 1;
				int onebyte = code.generator.common.Functions.hexval(wrkstr.charAt(k));
				tempval = onebyte * code.generator.common.Functions.exp(16, i);
				retval += tempval;
			}

			if (smallint)
			{
				if (retval > 32767L)
					retval -= 0x10000L;
			} else
			if (retval > 0x7fffffffL)
				retval -= 0x100000000L;
			Long integval = new Long(retval);
			retstr = integval.toString();
		}
		catch (Exception e)
		{
			return "Error: " + e;
		}
		return retstr;
	}

	public static String x2i(String numstr, boolean smallint)
	{
		String retstr = "";
		try
		{
			String wrkstr = code.generator.common.Functions.x2s(numstr);
			int strlen = wrkstr.length();
			int tempval = 0;
			long retval = 0L;
			for (int i = 0; i < strlen; i++)
			{
				int k = strlen - i - 1;
				int onebyte = code.generator.common.Functions.hexval(wrkstr.charAt(k));
				tempval = onebyte * code.generator.common.Functions.exp(16, i);
				retval += tempval;
			}

			if (smallint)
			{
				if (retval > 32767L)
					retval -= 0x10000L;
			} else
			if (retval > 0x7fffffffL)
				retval -= 0x100000000L;
			Long integval = new Long(retval);
			retstr = integval.toString();
		}
		catch (Exception e)
		{
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
		for (int i = 0; i < strlen; i++)
		{
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
		for (Enumeration enumtmp = props.keys(); enumtmp.hasMoreElements();)
		{
			String aKey = (String)enumtmp.nextElement();
			if (aKey != null && aKey.indexOf(mask) >= 0)
			{
				Object aValue = props.getProperty(aKey);
				results.put(aKey, aValue);
			}
		}

		return results;
	}

	public static List extractPropertyValues(Properties props)
	{
		List results = new ArrayList();
		for (Enumeration enumtmp = props.elements(); enumtmp.hasMoreElements(); results.add(enumtmp.nextElement()));
		return results;
	}

	public static void createDir(File inFile)
	{
		if (inFile.exists())
			return;
		File inDir = new File(inFile.getParent());
		if (!inDir.isDirectory())
			inDir.mkdirs();
	}

	public static boolean hasMask(String inString, String mask)
	{
		boolean hasSubstring = inString.toUpperCase().indexOf(mask.toUpperCase()) >= 0;
		return hasSubstring;
	}

	public static String makeLowerCase(String aString)
	{
		return aString.toLowerCase();
	}

	public static String makeUpperCase(String aString)
	{
		return aString.toUpperCase();
	}

	public static String x2s(String hexstr)
	{
		String retstr = "";
		int strlen = hexstr.length();
		int targlen = strlen * 2;
		String tempstr = "";
		for (int i = 0; i < strlen; i++)
		{
			int intval = hexstr.charAt(i);
			tempstr = Integer.toHexString(intval);
			if (tempstr.length() == 1)
				retstr = retstr + "0" + tempstr;
			else
				retstr = retstr + tempstr;
		}

		return retstr.toUpperCase();
	}

	public static String makeSampleData(String attType, String colname, Integer colsizeNum, Short coltypeNum, String testcase, String incQuote)
	{
		SimpleDateFormat dtFormat = new SimpleDateFormat("");
		dtFormat.setLenient(false);
		dtFormat.applyPattern("yyyy-MM-dd");
		return code.generator.common.Functions.makeSampleDataWithDatePattern(dtFormat, attType, colname, colsizeNum, coltypeNum, testcase, incQuote);
	}

	public static String makeSampleDataWithInputDatePattern(String attType, String colname, Integer colsizeNum, Short coltypeNum, String testcase, String incQuote)
	{
		SimpleDateFormat dtFormat = new SimpleDateFormat("");
		dtFormat.setLenient(false);
		dtFormat.applyPattern("MM/dd/yy");
		return code.generator.common.Functions.makeSampleDataWithDatePattern(dtFormat, attType, colname, colsizeNum, coltypeNum, testcase, incQuote);
	}

	public static String makeSampleDataPK(code.generator.db.SqlTable aTable, String pattern, String testcase)
	{
		int numKeys = aTable.getPrimaryKeys().size();
		String className = code.generator.common.Functions.makeClassName(aTable.getEntityName());
		String pkey = "new " + className + "PK (";
		String comma = "";
		for (int i = 0; i < numKeys; i++)
		{
			code.generator.db.SqlColumn col = aTable.getPrimaryKey(i);
			Short coltype = new Short(col.getColtype());
			Integer colsize = new Integer(col.getColsize());
			String coldata = code.generator.common.Functions.makeSampleData(pattern, col.getAttType(), col.getColname(), colsize, coltype, testcase, "Y");
			pkey = pkey + comma + coldata;
			comma = ",";
		}

		pkey = pkey + ")";
		return pkey;
	}

	public static String makeSampleData(String pattern, String attType, String colname, Integer colsizeNum, Short coltypeNum, String testcase, String incQuote)
	{
		SimpleDateFormat dtFormat = new SimpleDateFormat("");
		dtFormat.setLenient(false);
		dtFormat.applyPattern(pattern);
		return code.generator.common.Functions.makeSampleDataWithDatePattern(dtFormat, attType, colname, colsizeNum, coltypeNum, testcase, incQuote);
	}

	private static String makeSampleDataWithDatePattern(SimpleDateFormat dtFormat, String attType, String colname, Integer colsizeNum, Short coltypeNum, String testcase, String incQuote)
	{
		String sampleData = "";
		int colsize = colsizeNum.intValue();
		Short coltype = coltypeNum.shortValue();
		boolean includeQuotes = incQuote.equalsIgnoreCase("Y");
		String s = "";
		long lng = 0L;
		if (includeQuotes)
			s = quote;
		try
		{
			lng = (new Long(testcase)).longValue();
		}
		catch (Exception e)
		{
			lng = 0L;
		}
		String longString = "(new Long(" + testcase + ")).longValue()";
		SimpleDateFormat tsFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'000'");
		SimpleDateFormat timeFormat = new SimpleDateFormat("");
		timeFormat.setLenient(false);
		timeFormat.applyPattern("hh:mm:ss");
		if (code.generator.common.Functions.hasMask(attType, "Integer"))
		{
			if (includeQuotes)
				sampleData = "new Integer(" + testcase + ")";
			else
				sampleData = (new StringBuffer(String.valueOf(testcase))).toString();
		} else
		if (code.generator.common.Functions.hasMask(attType, "BigDecimal"))
		{
			if (includeQuotes)
				sampleData = "new BigDecimal(" + testcase + ".0 )";
			else
				sampleData = testcase + ".0";
		} else
		if (code.generator.common.Functions.hasMask(attType, "Short"))
		{
			if (includeQuotes)
				sampleData = "(new Short((new Long(" + testcase + ")).shortValue()))";
			else
				sampleData = (new StringBuffer(String.valueOf(testcase))).toString();
		} else
		if (code.generator.common.Functions.hasMask(attType, "Long"))
		{
			if (includeQuotes)
				sampleData = "new Long(" + testcase + ")";
			else
				sampleData = (new StringBuffer(String.valueOf(testcase))).toString();
		} else
		if (code.generator.common.Functions.hasMask(attType, "Double"))
		{
			if (includeQuotes)
				sampleData = "new Double(" + testcase + ")";
			else
				sampleData = (new StringBuffer(String.valueOf(testcase))).toString();
		} else
		if (code.generator.common.Functions.hasMask(attType, "Float"))
		{
			if (includeQuotes)
				sampleData = "new Float(" + testcase + ")";
			else
				sampleData = (new StringBuffer(String.valueOf(testcase))).toString();
		} else
		if (code.generator.common.Functions.hasMask(attType, "String"))
		{
			if (colsize > 1)
				sampleData = code.generator.common.StringUtil.makeVarName(colname);
			else
				sampleData = "";
			if (colsize > 1 && sampleData.length() > colsize - 1)
				sampleData = sampleData.substring(0, colsize - 1);
			sampleData = s + sampleData + testcase + s;
		} else
		if (code.generator.common.Functions.hasMask(attType, "Date"))
		{
			if (includeQuotes)
				sampleData = "(new java.util.Date(" + longString + "))";
			else
				sampleData = dtFormat.format(new Date(lng));
		} else
		if (code.generator.common.Functions.hasMask(attType, "Timestamp"))
		{
			if (includeQuotes)
				sampleData = "(new java.sql.Timestamp(" + longString + "))";
			else
				sampleData = tsFormatter.format(new Timestamp(lng));
		} else
		if (code.generator.common.Functions.hasMask(attType, "Time"))
		{
			if (includeQuotes)
				sampleData = "(new java.sql.Time(" + longString + "))";
			else
				sampleData = timeFormat.format(new Time(lng));
		} else
		{
			sampleData = (new StringBuffer(String.valueOf(testcase))).toString();
		}
		return sampleData;
	}

	public static String quote(String aString)
	{
		return quote + aString + quote;
	}

	public static code.generator.common.ListHashtable getResolvedProperties(code.generator.db.SqlTable sqlTable)
	{
		String FS = File.separator;
		String tableName = sqlTable.getTable();
		String srcName = code.generator.common.Functions.makeClassName(sqlTable.getEntityName());
		String lowercaseSrcName = code.generator.common.Functions.makeFirstLetterLowerCase(srcName);
		String entityName = code.generator.common.Functions.makeClassName(sqlTable.getEntityName());
		String defaultPackagePrefix = code.generator.common.ApplicationProperties.getProperty("packagePrefix");
		String defaultPackageLocation = code.generator.common.Functions.replaceString(defaultPackagePrefix, ".", FS);
		String packageMod = code.generator.common.TemplateProcessor.getPackageResolver().getPropertyValue(tableName);
		String packageModot = "";
		String packageModLocation = "";
		if (!packageMod.equals(""))
		{
			packageModot = "." + packageMod;
			packageModLocation = code.generator.common.Functions.replaceString(packageMod, ".", FS) + FS;
		}
		String packageLocationFS = code.generator.common.Functions.replaceString(defaultPackagePrefix, ".", "/");
		code.generator.common.ListHashtable reqProperties = new ListHashtable();
		reqProperties.put("srcName", srcName);
		reqProperties.put("entityName", entityName);
		reqProperties.put("packageLocation", defaultPackageLocation);
		reqProperties.put("packageLocationFS", packageLocationFS);
		reqProperties.put("packageModLocation", packageModLocation);
		reqProperties.put("packageMod", packageMod);
		reqProperties.put("packageModot", packageModot);
		return reqProperties;
	}

	static
	{
		crlf = new String(crchar);
		quote = new String(quchar);
		blankQuote = new String(quBlank);
		fs = File.separator;
		hex40 = null;
		hex40 = new char[4000];
		for (int i = 0; i < 4000; i++)
			hex40[i] = '@';

	}
}

