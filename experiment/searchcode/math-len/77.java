//#preprocessor

//---------------------------------------------------------------------------------
//
//  Little Color Management System
//
// Permission is hereby granted, free of charge, to any person obtaining 
// a copy of this software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the Software 
// is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in 
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
// THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//---------------------------------------------------------------------------------
//
//@Author Vinnie Simonetti
package littlecms.internal.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import littlecms.internal.LCMSResource;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.util.Arrays;
import net.rim.device.api.util.StringUtilities;

//#ifdef CMS_INTERNAL_ACCESS & DEBUG
public
//#endif
final class PrintUtility
{
	/* 
	 * This mostly follows the specification mentioned http://pubs.opengroup.org/onlinepubs/009695399/functions/printf.html and http://pubs.opengroup.org/onlinepubs/009695399/functions/scanf.html
	 * 
	 * Formats:
	 *	p: Bit of inside information, it prints out the position the pointer is currently at. A new VirtualPointer will be 0000:0000. Moving 16 bytes in will print
	 *		out 0000:000F.
	 */
	
	public static int output(PrintStream out, int max, final String format, Object[] args, Object[][] formatCache, int cacheIndex)
	{
		if(max == 0)
        {
			//Regardless of if there is a format or not, nothing is going to get returned.
        	return 0;
        }
		//Cache the format if possible
		Object[] formats;
		if(formatCache != null && formatCache.length > 0 && cacheIndex >= 0 && cacheIndex < formatCache.length && formatCache[cacheIndex] != null)
		{
			formats = formatCache[cacheIndex];
		}
		else
		{
			formats = breakFormat(format, null);
			if(formatCache != null && formatCache.length > 0 && cacheIndex >= 0 && cacheIndex < formatCache.length)
			{
				formatCache[cacheIndex] = formats;
			}
		}
        if (max < 0)
        {
            max = Integer.MAX_VALUE;
        }
        int count = 0;
        int len = formats.length;
        int argLen = args == null ? 0 : args.length;
        int elLen;
        int argPos = 0;
        //Go through all the parts of the format
        for(int i = 0; i < len; i++)
        {
	        Object obj = formats[i];
	        String str = null;
	        if(obj instanceof String)
	        {
	        	//If it's a String then it's easy
		        str = (String)obj;
	        }
	        else
	        {
	        	//Have to actually format the args into a String
		        FormatElement form = (FormatElement)obj;
                if (form == null)
                {
                	throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                }
                //Does the format take any arguments as width or precision?
		        int req = form.requires();
		        if(req > 0)
		        {
			        Long one;
			        Long two = null;
			        if(argPos + req < argLen)
			        {
				        one = getAsLong(args[argPos++]);
				        if(req == 2)
				        {
					        two = getAsLong(args[argPos++]);
				        }
				        form.setInputValue(one, two);
			        }
			        else
			        {
			        	//O no, not enough args
				        argPos += req; //Do this so that it only prints the format out
			        }
		        }
                if (form.getFormat().endsWith("n"))
                {
                	//Have a numeric arg (gets the length of what has been written out so far)
                    ((int[])args[argPos++])[0] = count;
                }
                else
                {
                	//Ok, time to process..
                    if (argPos >= argLen && !form.hasArgLocation())
                    {
                    	//..Or not. Not enough args, return the format
                        str = form.getFormat();
                    }
                    else
                    {
                    	//If the format takes an argument then pass it in; it could be relative or absolute.
                        str = form.format(form.takesArg() ? (form.hasArgLocation() ? args[form.argLocation()] : args[argPos++]) : null);
                    }
                }
	        }
	        //If the String isn't null then print it to the PrintStream
            if (str != null)
            {
                elLen = str.length();
                if (elLen + count > max)
                {
                	//The maximum char-count will be exceeded so truncate the String before writing it and stop execution
                    out.print(str.substring(0, max - count));
                    count = max;
                    break;
                }
                else
                {
                	//Print out the whole String
                    out.print(str);
                    count += elLen;
                }
            }
        }
        if(count < max)
        {
        	out.print('\0');
        }
        //Reset the cache values so that if the cache is reused it doesn't contain the same values used in this current operation
        if(formatCache != null && formatCache.length > 0)
		{
        	for(int i = 0; i < len; i++)
        	{
        		if(formats[i] instanceof FormatElement)
        		{
        			((FormatElement)formats[i]).reset();
        		}
        	}
		}
        return count;
	}
	
	public static int fscanf(final InputStream file, final String format, Object[] argptr, Object[][] formatCache, int cacheIndex)
    {
		//First thing to do is get the actual formated data
		byte[] bytes = null;
		try
		{
			bytes = IOUtilities.streamToBytes(file);
		}
		catch(IOException ioe)
		{
		}
		if(bytes == null)
		{
			//No data, no processing
			return 0;
		}
		//Now process the format
		boolean doValidate = true;
		Object[] formats;
		//..and possibly cache it
		if(formatCache != null && formatCache.length > 0 && cacheIndex >= 0 && cacheIndex < formatCache.length && formatCache[cacheIndex] != null)
		{
			doValidate = false;
			formats = formatCache[cacheIndex];
		}
		else
		{
			formats = breakFormat(format, null);
			if(formatCache != null && formatCache.length > 0 && cacheIndex >= 0 && cacheIndex < formatCache.length)
			{
				formatCache[cacheIndex] = formats;
			}
		}
		//Now get the string data
		String str = null;
		try
		{
			str = new String(bytes, "UTF-8"); //Get string in UTF-8 format, this will allow standard ASCII all the way to international languages to be processed.
		}
		catch(UnsupportedEncodingException ioe)
		{
		}
		if(str == null)
		{
			//No string, no processing
			return 0;
		}
        char[] chars = str.toCharArray();
        int slen = chars.length;
        int len = formats.length;
        int argLen = argptr.length;
        int[] tempVals = new int[2]; //index 0 is "str pos", index 1 is "arg pos"
        //Simplify formats even more, this will not be cached because if the cache was used on output it would create an invalid formatted element
        for (int i = 0; i < len; i++)
        {
            Object obj = formats[i];
            if (obj instanceof String)
            {
            	//If the format is a String then it could possibly get simplified to speed up actual execution (this isn't writing out the format so we don't need to go through the format if we don't need to)
                String tmp = (String)obj;
                tmp = tmp.trim(); //Trim the String
                if (tmp.length() == 0)
                {
                	//Hmm, seems the String was only whitespace so remove it from the processing list
                    Object[] nForms = new Object[len - 1];
                    System.arraycopy(formats, 0, nForms, 0, i);
                    System.arraycopy(formats, i + 1, nForms, i, len - (i + 1));
                    len = nForms.length;
                    i--;
                    formats = nForms;
                }
                else
                {
                	//Ok we still have String content, lets see if we can make heads-or-tails of it
                    Vector nEl = null;
                    int l = tmp.length();
                    for (int k = 0; k < l; k++)
                    {
                        if (isWhiteSpace(tmp.charAt(k)))
                        {
                        	//Found some whitespace, remove it so we are left only with actual String content
                            if (nEl == null)
                            {
                                nEl = new Vector();
                            }
                            nEl.addElement(tmp.substring(0, k).trim());
                            tmp = tmp.substring(k).trim();
                            k = 0;
                            l = tmp.length();
                        }
                    }
                    if (nEl != null)
                    {
                        //Get forgotten element
                        nEl.addElement(tmp);
                        //Copy formats into a temporary array
                        int nElSize;
                        Object[] nForms = new Object[len + (nElSize = nEl.size()) - 1];
                        System.arraycopy(formats, 0, nForms, 0, i);
                        //Copy the cleaned up String to a temporary array
                        Object[] tObj = new Object[nElSize];
                        nEl.copyInto(tObj);
                        //Copy the new String (clean) into the new formats
                        System.arraycopy(tObj, 0, nForms, i, nElSize);
                        //Copy any formats that might have been overwritten to a new position in the new formats
                        System.arraycopy(formats, i + 1, nForms, i + nElSize, len - (i + 1));
                        //Adjust format length for processing
                        len = nForms.length;
                        //Adjust index position in relation to the new String
                        i += nElSize - 1;
                        //Finally replace the formats
                        formats = nForms;
                    }
                    else
                    {
                    	//Replace the format String with the trimmed String
                        formats[i] = tmp;
                    }
                }
            }
        }
        //Process str
        for (int i = 0; i < len && tempVals[0] < slen; i++)
        {
            //Skip Whitespace
            while (isWhiteSpace(chars[tempVals[0]]))
            {
                tempVals[0]++;
            }
            if (tempVals[0] >= slen)
            {
                break;
            }
            
            //Process elements
            Object obj = formats[i];
            if (obj instanceof String)
            {
                String tmp = (String)obj;
                if (str.indexOf(tmp, tempVals[0]) != tempVals[0])
                {
                    break;
                }
                tempVals[0] += tmp.length();
            }
            else
            {
                FormatElement form = (FormatElement)obj;
                if (form == null)
                {
                	throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                }
                if(doValidate)
                {
                	//If this isn't a precached formatter it should get validated
                	if(!validateUnformatter(form.getFormat()))
                	{
                		//Invalid format, return
                		if(formatCache != null && formatCache.length > 0)
                		{
                			//If there is a cache, remove it since the formatting is not valid
                			formatCache[cacheIndex] = null;
                		}
                		break;
                	}
                }
                //Get the current argument position and String position, "unformat" it, and compare the new positions
                int sp = tempVals[0];
                int ap = tempVals[1];
                form.unformat(str, argptr, tempVals);
                if (sp == tempVals[0] && ap == tempVals[1])
                {
                    //This means something went wrong, no changes to the input String or arguments occurred
                    break;
                }
                //count += (tempVals[1] - ap); //This is simply the number of arguments read
                if(tempVals[1] >= argLen)
                {
                	break; //Reached the max number of args that can be parsed, no need to keep processing
                }
            }
        }
        //Reset the formats so they can be reused
        if(formatCache != null && formatCache.length > 0)
		{
        	for(int i = 0; i < len; i++)
        	{
        		if(formats[i] instanceof FormatElement)
        		{
        			((FormatElement)formats[i]).reset();
        		}
        	}
		}
        return tempVals[1];
    }
	
	private static boolean validateUnformatter(String form)
	{
		int len = form.length();
		int section = 0; //Section: 0-"ignore char", 1-width, 2-modifiers, 3-type, (-1)-done
		for(int i = 1; i < len; i++) //First char is always %
		{
			char c = form.charAt(i);
			if (FULL_FORMAT.indexOf(c) >= 0) //Valid identifiers
			{
				switch(section)
				{
					case 0:
						if(c != '*')
						{
							//Not a 'ignore char' so go to next section
							i--;
						}
						section++;
						break;
					case 1:
						if(SCANF_SPECIFIERS.indexOf(c) >= 0)
						{
							//Found specifier, end
							return (i + 1) == len;
						}
						else if(SCANF_WIDTH.indexOf(c) >= 0)
						{
							//Continue execution, eventually it should get to the next component in the format.
						}
						else if(SCANF_LENGTH.indexOf(c) >= 0)
						{
							//Go to next section for checking
							section++;
						}
						else
						{
							section = -1; //Invalid
						}
						break;
					case 2:
						if(SCANF_SPECIFIERS.indexOf(c) >= 0)
						{
							//Found specifier, end
							return (i + 1) == len;
						}
						else if(SCANF_LENGTH.indexOf(c) >= 0)
						{
							//Continue execution, eventually it should get to a specifier or an invalid number
						}
						else
						{
							section = -1; //Invalid
						}
						break;
				}
			}
			else
			{
				section = -1;
			}
			if(section < 0)
			{
				break;
			}
		}
		return false;
	}
	
	private static boolean isWhiteSpace(char c)
    {
//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
		return net.rim.device.api.util.CharacterUtilities.isWhitespace(c);
//#else
        switch (c)
        {
            case ' ':
            case '\n':
            case '\r':
            case '\t':
            case '\0':
                return true;
            default:
                return false;
        }
//#endif
    }
	
	//Helper function so arguments can be converted to a Long
	private static Long getAsLong(Object arg)
	{
		if(arg instanceof Long)
		{
			return (Long)arg;
		}
		long l;
		if(arg instanceof Byte)
		{
			l = ((Byte)arg).byteValue() & 0xFF;
		}
		else if(arg instanceof Short)
		{
			l = ((Short)arg).shortValue() & 0xFFFF;
		}
		else if(arg instanceof Integer)
		{
			l = ((Integer)arg).longValue() & 0xFFFFFFFFL;
		}
		else
		{
			l = -1; //Invalid (-1 is the default value for width and precision which is where this function will get used)
		}
		return new Long(l);
	}
	
	private static final long SPECIFIERS_UID = 0x318AE6E8A41FCF70L;
	private static final long FLAGS_UID = 0x94E998EE54BD00EL;
	private static final long WIDTH_PRECISION_UID = 0xFF217872C5D4CDDL;
	private static final long LENGTH_UID = 0x9EF53E8BFE248140L;
	private static final long FULL_FORMAT_UID = 0x3024E7CCD507CBE0L;
	
	private static final long SCANF_SPECIFIERS_UID = 0x3C0F209819016600L;
	private static final long SCANF_WIDTH_UID = 0x3889827523F0B67AL;
	private static final long SCANF_LENGTH_UID = 0x50EC7C6D6EC9B264L;
	private static final long SCANF_FULL_FORMAT_UID = 0xAE2D09322C4157CFL;
	
	private static final char THOUS_SEP = '\'';
	
	private static char DECIMAL;
	
	private static String SPECIFIERS;
	private static String FLAGS;
	private static String WIDTH_PRECISION;
	private static String LENGTH;
	private static String FULL_FORMAT;
	
	private static String SCANF_SPECIFIERS;
	private static String SCANF_WIDTH;
	private static String SCANF_LENGTH;
	private static String SCANF_FULL_FORMAT;
	
	static
	{
		String temp = (String)Utility.singletonStorageGet(SPECIFIERS_UID);
		DECIMAL = Double.toString(1.1).charAt(1);
		if(temp == null)
		{
			SPECIFIERS = "cspdieEfFgGouxXn";
			FLAGS = "-+ #" + THOUS_SEP + '0';
			WIDTH_PRECISION = "123456789*0"; //Zero is added at end so that when FULL_FORMAT is generated there isn't two zeros in the format. It wouldn't cause an error but it would be one more char to check that isn't needed.
			LENGTH = "hlLzjt";
			FULL_FORMAT = FLAGS + WIDTH_PRECISION.substring(0, 9) + '.' + LENGTH + SPECIFIERS;
			Utility.singletonStorageSet(SPECIFIERS_UID, SPECIFIERS);
			Utility.singletonStorageSet(FLAGS_UID, FLAGS);
			Utility.singletonStorageSet(WIDTH_PRECISION_UID, WIDTH_PRECISION);
			Utility.singletonStorageSet(LENGTH_UID, LENGTH);
			Utility.singletonStorageSet(FULL_FORMAT_UID, FULL_FORMAT);
			
			SCANF_SPECIFIERS = SPECIFIERS.substring(0, SPECIFIERS.length() - 2);
			SCANF_WIDTH = WIDTH_PRECISION.substring(0, WIDTH_PRECISION.length() - 2);
			SCANF_LENGTH = LENGTH.substring(0, LENGTH.length() - 3);
			SCANF_FULL_FORMAT = SCANF_WIDTH + SCANF_LENGTH + SCANF_SPECIFIERS + '*';
			Utility.singletonStorageSet(SCANF_SPECIFIERS_UID, SCANF_SPECIFIERS);
			Utility.singletonStorageSet(SCANF_WIDTH_UID, SCANF_WIDTH);
			Utility.singletonStorageSet(SCANF_LENGTH_UID, SCANF_LENGTH);
			Utility.singletonStorageSet(SCANF_FULL_FORMAT_UID, SCANF_FULL_FORMAT);
		}
		else
		{
			SPECIFIERS = temp;
			FLAGS = (String)Utility.singletonStorageGet(FLAGS_UID);
			WIDTH_PRECISION = (String)Utility.singletonStorageGet(WIDTH_PRECISION_UID);
			LENGTH = (String)Utility.singletonStorageGet(LENGTH_UID);
			FULL_FORMAT = (String)Utility.singletonStorageGet(FULL_FORMAT_UID);
			
			SCANF_SPECIFIERS = (String)Utility.singletonStorageGet(SCANF_SPECIFIERS_UID);
			SCANF_WIDTH = (String)Utility.singletonStorageGet(SCANF_WIDTH_UID);
			SCANF_LENGTH = (String)Utility.singletonStorageGet(SCANF_LENGTH_UID);
			SCANF_FULL_FORMAT = (String)Utility.singletonStorageGet(SCANF_FULL_FORMAT_UID);
		}
	}
	
	//Takes the format String, breaks it up into format elements and String literals. If just the format is passed in along with the args argument then the broken down format will be passed into the args argument.
	public static Object[] breakFormat(final String format, String[][] args)
	{
		StringBuffer bu = new StringBuffer();
        Vector parts = new Vector();
        int len = format.length();
        boolean inFormat = false;
        int section = -1;
        Vector argList = args == null ? null : new Vector(6);
        for (int i = 0; i < len; i++)
        {
            char c = format.charAt(i);
            if (inFormat)
            {
                //First remove any arg location parameter
                int argPosIdPos = format.indexOf('$', i);
                if (argPosIdPos >= 0)
                {
                    //Not very efficient but works well
                    String sub = format.substring(i, argPosIdPos);
                    int inLen = sub.length();
                    int k;
                    for (k = 0; k < inLen; k++)
                    {
                        if (!Character.isDigit(sub.charAt(k)))
                        {
                            break;
                        }
                    }
                    if (k == inLen)
                    {
                        if (argList != null)
                        {
                            argList.addElement(sub);
                        }
                        else
                        {
                            bu.append(sub);
                            bu.append('$');
                        }
                        i += inLen;
                        continue;
                    }
                }
                if (FULL_FORMAT.indexOf(c) >= 0) //Valid identifiers
                {
                    bu.append(c);
                    switch (section)
                    {
                        case -1: //Bad format
                        	throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                        case 0: //General (everything is possible)
                            if (SPECIFIERS.indexOf(c) >= 0)
                            {
                                //Found the end, exit
                                section = -1;
                                String str = bu.toString();
                                if (argList != null)
                                {
                                	for(int j = 0; j < 4; j++)
                                	{
                                		argList.addElement(null);
                                	}
                                    argList.addElement(str.substring(1));
                                }
                                else
                                {
                                    //If we don't do this it will become redundant and we will get a stack overflow
                                    parts.addElement(FormatElement.getFormatter(str));
                                }
                                bu.setLength(0);
                                inFormat = false;
                            }
                            else if (FLAGS.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                    argList.addElement(bu.toString().substring(1));
                                }
                                section++; //Found flag section, now to check for next section
                            }
                            else if (WIDTH_PRECISION.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                    argList.addElement(null);
                                    argList.addElement(bu.toString().substring(1));
                                }
                                section += 2; //Found width section, now to check for next section
                            }
                            else if (c == '.')
                            {
                            	//Precision is prefixed with a decimal, make sure that there is more to the format then just a decimal at the end.
                                if (i + 1 < len)
                                {
                                    if (WIDTH_PRECISION.indexOf(format.charAt(i + 1)) >= 0)
                                    {
                                        if (argList != null)
                                        {
                                        	for(int j = 0; j < 2; j++)
                                        	{
                                        		argList.addElement(null);
                                        	}
                                            argList.addElement(bu.toString().substring(1));
                                        }
                                        section += 3; //Found precision section, now to check for next section
                                    }
                                    else if (SPECIFIERS.indexOf(format.charAt(i + 1)) < 0)
                                    {
                                        throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                                    }
                                }
                                else
                                {
                                    throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                                }
                            }
                            else if (LENGTH.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                	for(int j = 0; j < 3; j++)
                                	{
                                		argList.addElement(null);
                                	}
                                    argList.addElement(bu.toString().substring(1));
                                }
                                section += 4; //Found length section, now to check for next section
                            }
                            else
                            {
                                throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                            }
                            break;
                        case 1: //Flags
                            if (SPECIFIERS.indexOf(c) >= 0)
                            {
                                //Found the end, exit
                                section = -1;
                                String str = bu.toString();
                                if (argList != null)
                                {
                                	for(int j = 0; j < 3; j++)
                                	{
                                		argList.addElement(null);
                                	}
                                    argList.addElement(c + "");
                                }
                                else
                                {
                                    //If we don't do this it will become redundent and we will get a stack overflow
                                    parts.addElement(FormatElement.getFormatter(str));
                                }
                                bu.setLength(0);
                                inFormat = false;
                            }
                            else if (FLAGS.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                	argList.setElementAt(((String)argList.elementAt(0)) + c, 0);
                                }
                                continue; //Still looking at flag values
                            }
                            else if (WIDTH_PRECISION.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                	argList.addElement(c + "");
                                }
                                section++; //Found width section, now to check for next section
                            }
                            else if (c == '.')
                            {
                            	//Precision is prefixed with a decimal, make sure that there is more to the format then just a decimal at the end.
                                if (i + 1 < len)
                                {
                                    if (WIDTH_PRECISION.indexOf(format.charAt(i + 1)) >= 0)
                                    {
                                        if (argList != null)
                                        {
                                            argList.addElement(null);
                                            argList.addElement(c + "");
                                        }
                                        section += 2; //Found precision section, now to check for next section
                                    }
                                    else if (SPECIFIERS.indexOf(format.charAt(i + 1)) < 0)
                                    {
                                        throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                                    }
                                }
                                else
                                {
                                    throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                                }
                            }
                            else if (LENGTH.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                	for(int j = 0; j < 2; j++)
                                	{
                                		argList.addElement(null);
                                	}
                                    argList.addElement(c + "");
                                }
                                section += 3; //Found length section, now to check for next section
                            }
                            else
                            {
                                throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                            }
                            break;
                        case 2: //Width
                            if (SPECIFIERS.indexOf(c) >= 0)
                            {
                                //Found the end, exit
                                section = -1;
                                String str = bu.toString();
                                if (argList != null)
                                {
                                	for(int j = 0; j < 2; j++)
                                	{
                                		argList.addElement(null);
                                	}
                                    argList.addElement(c + "");
                                }
                                else
                                {
                                    //If we don't do this it will become redundent and we will get a stack overflow
                                    parts.addElement(FormatElement.getFormatter(str));
                                }
                                bu.setLength(0);
                                inFormat = false;
                            }
                            else if (WIDTH_PRECISION.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                    argList.setElementAt(((String)argList.elementAt(1)) + c, 1);
                                }
                                continue; //Still looking at width values
                            }
                            else if (c == '.')
                            {
                            	//Precision is prefixed with a decimal, make sure that there is more to the format then just a decimal at the end.
                                if (i + 1 < len)
                                {
                                    if (WIDTH_PRECISION.indexOf(format.charAt(i + 1)) >= 0)
                                    {
                                        if (argList != null)
                                        {
                                            argList.addElement(c + "");
                                        }
                                        section++; //Found precision section, now to check for next section
                                    }
                                    else if (SPECIFIERS.indexOf(c) < 0)
                                    {
                                        throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                                    }
                                }
                                else
                                {
                                    throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                                }
                            }
                            else if (LENGTH.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                    argList.addElement(null);
                                    argList.addElement(c + "");
                                }
                                section += 2; //Found length section, now to check for next section
                            }
                            else
                            {
                                throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                            }
                            break;
                        case 3: //Precision
                            if (SPECIFIERS.indexOf(c) >= 0)
                            {
                                //Found the end, exit
                                section = -1;
                                String str = bu.toString();
                                if (argList != null)
                                {
                                    argList.addElement(null);
                                    argList.addElement(c + "");
                                }
                                else
                                {
                                    //If we don't do this it will become redundent and we will get a stack overflow
                                    parts.addElement(FormatElement.getFormatter(str));
                                }
                                bu.setLength(0);
                                inFormat = false;
                            }
                            else if (WIDTH_PRECISION.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                    argList.setElementAt(((String)argList.elementAt(2)) + c, 2);
                                }
                                continue; //Still looking at precision values
                            }
                            else if (LENGTH.indexOf(c) >= 0)
                            {
                                if (argList != null)
                                {
                                    argList.addElement(c + "");
                                }
                                section++; //Found length section, now to check for next section
                            }
                            else
                            {
                                throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                            }
                            break;
                        case 4: //Length
                            if (SPECIFIERS.indexOf(c) >= 0)
                            {
                                //Found the end, exit
                                section = -1;
                                String str = bu.toString();
                                if (argList != null)
                                {
                                    argList.addElement(c + "");
                                }
                                else
                                {
                                    //If we don't do this it will become redundant and we will get a stack overflow
                                    parts.addElement(FormatElement.getFormatter(str));
                                }
                                bu.setLength(0);
                                inFormat = false;
                            }
                            else
                            {
                                throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                            }
                            break;
                    }
                }
                //If args isn't null then copy the broken up components into the argument
                if (!inFormat && argList != null)
                {
                	String[] argListAr = new String[argList.size()];
                	argList.copyInto(argListAr);
                    args[0] = argListAr;
                }
            }
            else
            {
            	//Look for a format element
                if (c == '%')
                {
                	//Found one
                    if (i + 1 < len)
                    {
                        if (format.charAt(i + 1) == '%')
                        {
                            i++;
                            bu.append('%');
                        }
                        else
                        {
                            inFormat = true;
                            if (bu.length() > 0)
                            {
                                parts.addElement(bu.toString());
                                bu.setLength(0);
                            }
                            bu.append('%');
                            section = 0; //Used to determine what part of the format is being checked
                        }
                    }
                    else
                    {
                        throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                    }
                }
                else
                {
                    bu.append(c);
                }
            }
        }
        //If anything is left over then process it
        if (bu.length() > 0)
        {
            if (inFormat)
            {
                parts.addElement(FormatElement.getFormatter(bu.toString()));
            }
            else
            {
                parts.addElement(bu.toString());
            }
        }
        Object[] partsAr = new Object[parts.size()];
        parts.copyInto(partsAr);
        return partsAr;
	}
	
	public static abstract class FormatElement
	{
		protected String format;
		
        protected FormatElement(String format)
        {
            this.format = format;
        }
        
        public abstract String format(Object obj);
        
        public abstract void unformat(String value, Object[] refO, int[] vals);
        
        public String getFormat()
        {
            return format;
        }
        
        public abstract void setInputValue(Long one, Long two);
        
        public abstract boolean takesArg();
        
        public abstract int requires();
        
        public abstract int argLocation();
        
        public abstract String getNullParamOutput();
        
        public abstract void reset();
        
        public boolean hasArgLocation()
        {
            return argLocation() >= 0;
        }
        
        public static FormatElement getFormatter(String form)
        {
            if (form.charAt(0) != '%')
            {
                return null;
            }
            switch (form.charAt(form.length() - 1))
            {
            	case 'C':
            	case 'S':
                case 'c': //Character
                case 's': //String
                    return new StringFormatElement(form);
                case 'd':
                case 'i': //Signed decimal integer
                case 'o': //Signed octal
                case 'x':
                case 'X': //Unsigned hexadecimal integer
                case 'u': //Unsigned decimal integer
                    return new IntFormatElement(form);
                //case 'a':
                //case 'A':
                case 'e':
                case 'E': //Scientific notation
                case 'g':
                case 'G': //Takes the smaller output of 'f' and 'e'/'E'
                case 'f':
                case 'F': //Decimal floating point
                    return new FloatFormatElement(form);
                case 'p': //Pointer address
                    return new PointerFormatElement(form);
            }
	        return new GenericFormatElement(form);
        }
        
        protected void argError(String formatter, String defValue, Class element)
        {
        	System.err.println(formatter + Utility.LCMS_Resources.getString(LCMSResource.PRINTUTIL_UNK_ARG) + defValue + ". Arg:" + element);
        }
        
        public String ToString()
        {
            return format;
        }
	}
	
	private static abstract class GeneralFormatElement extends FormatElement
    {
        private boolean arg, lengthDoubleSize;
        protected String flags;
        protected char length, type;
        protected int width, precision, requiresInput, argPos;
        
        public GeneralFormatElement(String format)
        {
        	super(format);
        	reset();
            this.arg = true; //Not sure why this should be included but could be useful in the future or depending on implementation.
            parseFormat();
        }
        
        public void reset()
        {
        	this.precision = -1;
            this.width = -1;
        }
        
        private void parseFormat()
        {
            String[][] parts = new String[1][];
            PrintUtility.breakFormat(this.format, parts);
            String[] elements = parts[0];
            int pos = 0;
            if (elements.length == 6)
            {
                pos++;
                argPos = Integer.parseInt(elements[0]) - 1;
            }
            else
            {
                argPos = -1;
            }
            if (elements[pos++] != null)
            {
                //Flags
                this.flags = elements[pos - 1];
            }
            if (elements[pos++] != null)
            {
                //Width
                String el = elements[pos - 1];
                int loc;
                if ((loc = el.indexOf('*')) >= 0)
                {
                    requiresInput = 1;
                }
                if (el.length() > loc + 1)
                {
                    width = Integer.parseInt(loc >= 0 ? el.substring(loc + 1) : el);
                }
            }
            if (elements[pos++] != null)
            {
                //Precision
                String el = elements[pos - 1];
                if (el.indexOf('*') >= 0)
                {
                    requiresInput++;
                    if (requiresInput == 1)
                    {
                        //No first element, need to make sure only second element is retrieved
                        requiresInput |= 1 << 2;
                    }
                }
                else
                {
                    precision = Integer.parseInt(el.substring(1));
                }
            }
            if (elements[pos++] != null)
            {
                //Length
                String el = elements[pos - 1];
                char c1 = el.charAt(0);
                if (el.length() > 1)
                {
                	char c2 = el.charAt(1);
                    if (c1 != c2 || (c1 != 'h' || c1 != 'l'))
                    {
                    	throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                    }
                    this.lengthDoubleSize = true;
                }
                this.length = c1;
            }
            type = elements[pos].charAt(0);
        }

        public String format(Object obj)
        {
            String str = inFormat(obj);
            if (flags != null)
            {
                if (flags.indexOf('-') >= 0)
                {
                	/*
                    if (flags.indexOf('0') >= 0)
                    {
                    	throw new IllegalArgumentException(Utility.LCMS_Resources.getString(LCMSResource.BAD_STRING_FORMAT));
                    }
                    */
                    //Left align
//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
                	str = StringUtilities.pad(str, ' ', width, false);
//#else
                    if (str.length() < width)
                    {
                    	char[] chars = new char[width - str.length()];
                    	Arrays.fill(chars, ' ');
                        str += new String(chars);
                    }
//#endif
                }
                else if (this.precision == -1 && flags.indexOf('0') >= 0 && SPECIFIERS.indexOf(this.type) > 2)
                {
                	//Pad with zeros (for everything but char, string, and pointer) when precision not specified
//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
                	str = StringUtilities.pad(str, '0', width, true);
//#else
                    if (str.length() < width)
                    {
                    	char[] chars = new char[width - str.length()];
                    	Arrays.fill(chars, '0');
                        str = new String(chars) + str;
                    }
//#endif
                }
            }
//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
            str = StringUtilities.pad(str, ' ', width, true);
//#else
            if (str.length() < width)
            {
            	//Right align
            	char[] chars = new char[width - str.length()];
            	Arrays.fill(chars, ' ');
                str = new String(chars) + str;
            }
//#endif
            return str;
        }
        
        public abstract String inFormat(Object obj);

        public void setInputValue(Long one, Long two)
        {
            if (one != null)
            {
                if ((requiresInput & (1 << 2)) != 0)
                {
                    precision = (int)one.longValue();
                }
                else
                {
                    width = (int)one.longValue();
                }
            }
            if (two != null)
            {
                precision = (int)two.longValue();
            }
        }

        public boolean takesArg()
        {
            return arg;
        }

        public int requires()
        {
            return requiresInput & 3;
        }

        public int argLocation()
        {
            return argPos;
        }
    }
	
	private static class GenericFormatElement extends GeneralFormatElement
    {
        public GenericFormatElement(String format)
        {
        	super(format);
        }

        public String inFormat(Object obj)
        {
        	argError("GenericFormat", "$format", obj.getClass());
            return this.format;
        }
        
        public String getNullParamOutput()
        {
        	return inFormat(null);
        }

        public void unformat(String value, Object[] refO, int[] vals)
        {
            throw new UnsupportedOperationException();
        }
    }
	
	private static class StringFormatElement extends GeneralFormatElement
    {
        public StringFormatElement(String format)
        {
        	super(format);
        }

        public String inFormat(Object obj)
        {
            boolean charType = this.type == 'c';
            String str = null;
            if (obj instanceof String)
            {
                str = (String)obj;
            }
            else if (obj instanceof StringBuffer)
            {
                str = ((StringBuffer)obj).toString();
                int len = Utility.strlen(str);
                if(len != str.length())
                {
                	str = str.substring(0, len);
                }
            }
            if (str == null)
            {
                if (obj instanceof char[])
                {
                    if (this.length == 'l')
                    {
                        str = new String((char[])obj, 0, Utility.strlen((char[])obj));
                    }
                    else
                    {
                        char[] chars = (char[])obj;
                        int len;
                        byte[] nBytes = new byte[len = charType ? 1 : Utility.strlen(chars)];
                        for (int i = 0; i < len; i++)
                        {
                            nBytes[i] = (byte)chars[i];
                        }
                        str = new String(nBytes);
                    }
                }
                else if (obj instanceof Character)
                {
                	char c = ((Character)obj).charValue();
                	if(this.length != 'l')
                	{
                		c = (char)(c & 0xFF);
                	}
                    str = c + "";
                }
                else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long)
                {
                    long val;
                    int mask = this.length == 'l' ? 0xFFFF : 0xFF;
                    if (obj instanceof Byte)
                    {
                        val = ((Byte)obj).byteValue() & mask;
                    }
                    else if (obj instanceof Short)
                    {
                        val = ((Short)obj).shortValue() & mask;
                    }
                    else if (obj instanceof Integer)
                    {
                        val = ((Integer)obj).intValue() & mask;
                    }
                    else
                    {
                        val = ((Long)obj).longValue() & mask;
                    }
                    str = ((char)val) + "";
                }
                else if (obj instanceof VirtualPointer)
                {
                	VirtualPointer.TypeProcessor proc = ((VirtualPointer)obj).getProcessor();
                	if(charType)
                	{
	                	char c = this.length != 'l' ? ((char)proc.readInt8()) : proc.readChar();
	                	str = c + "";
                	}
                	else
                	{
                		str = proc.readString();
                	}
                }
                else
                {
                	argError("StringFormat", "obj.toString()", obj.getClass());
                    str = obj.toString(); //This will return ASCII
                }
            }
            else if (this.length != 'l')
            {
                char[] chars = str.toCharArray();
                int len;
                byte[] nBytes = new byte[len = charType ? 1 : chars.length];
                for (int i = 0; i < len; i++)
                {
                    nBytes[i] = (byte)chars[i];
                }
                str = new String(nBytes);
            }
            if (charType)
            {
                if (str.length() > 1)
                {
                    str = str.substring(0, 1);
                }
            }
            else if (this.precision >= 0 && this.type != 'c')
            {
                if (str.length() > this.precision)
                {
                    str = str.substring(0, this.precision);
                }
            }
            return str;
        }
        
        public String getNullParamOutput()
        {
        	return this.type == 'c' ? "\0" : "(null)";
        }

        public void unformat(String value, Object[] refO, int[] vals)
        {
        	int w = this.width;
            if (w < 0)
            {
                w = 1;
            }
            int len = value.length();
            int org = vals[0];
            if (this.type == 'c')
            {
                char[] items = new char[w];
                value.getChars(org, org + w, items, 0);
                vals[0] += w;
                int t = vals[1] + w;
                if (this.requiresInput == 1)
                {
                    return;
                }
                for (int i = vals[1], e = 0; i < t; i++, e++)
                {
                    vals[1]++;
                    Object obj = refO[i];
                    if (obj == null || !obj.getClass().isArray())
                    {
                    	if(obj != null)
                    	{
                    		if(!(obj instanceof VirtualPointer || obj instanceof StringBuffer))
                    		{
                    			return;
                    		}
                    	}
                		else
                		{
                			return;
                		}
                    }
                    if (obj instanceof char[])
                    {
                        ((char[])obj)[0] = items[e];
                    }
                    else if(obj instanceof byte[])
                    {
                    	((byte[])obj)[0] = (byte)items[e];
                    }
                    else if(obj instanceof short[])
                    {
                    	((short[])obj)[0] = (short)items[e];
                    }
                    else if(obj instanceof int[])
                    {
                    	((int[])obj)[0] = items[e];
                    }
                    else if(obj instanceof long[])
                    {
                    	((long[])obj)[0] = items[e];
                    }
                    else if(obj instanceof StringBuffer)
                    {
                    	StringBuffer buf = (StringBuffer)obj;
                    	if(buf.length() == buf.capacity())
                    	{
                    		buf.setCharAt(0, items[e]);
                    	}
                    	else
                    	{
                    		buf.append(items[e]);
                    	}
                    }
                    else if(obj instanceof VirtualPointer)
                    {
                    	VirtualPointer.TypeProcessor proc = ((VirtualPointer)obj).getProcessor();
                    	proc.write(items[e]);
                    }
                    else
                    {
                    	argError("StringFormat", "null", obj.getClass());
                    }
                }
            }
            else
            {
                for (w = 0; w < len; w++)
                {
                    if (PrintUtility.isWhiteSpace(value.charAt(org + w)))
                    {
                        break;
                    }
                }
                if (this.width < w && this.width != -1)
                {
                    w = this.width;
                }
                vals[0] += w;
                if (this.requiresInput == 1)
                {
                    return; //Skip argument
                }
                Object obj = refO[vals[1]];
                vals[1]++;
                if (obj == null || !obj.getClass().isArray())
                {
                	if(obj != null)
                	{
                		if(!(obj instanceof VirtualPointer || obj instanceof StringBuffer))
                		{
                			return;
                		}
                	}
            		else
            		{
            			return;
            		}
                }
                String sVal = value.substring(org, org + w);
                if (obj instanceof String[])
                {
                    ((String[])refO)[0] = sVal;
                }
                else if (obj instanceof char[])
                {
                    System.arraycopy(sVal.toCharArray(), 0, (char[])obj, 0, w);
                }
                else if(obj instanceof byte[])
                {
                	System.arraycopy(sVal.getBytes(), 0, (byte[])obj, 0, w);
                }
                else if(obj instanceof short[])
                {
                	short[] sh = (short[])obj;
                	char[] ch = sVal.toCharArray();
                	for(int i = 0; i < w; i++)
                	{
                		sh[i] = (short)ch[i];
                	}
                }
                else if(obj instanceof StringBuffer)
                {
                	StringBuffer buf = (StringBuffer)obj;
                	if(buf.length() == buf.capacity())
                	{
                		Utility.strncpy(buf, sVal, w);
                	}
                	else
                	{
                		buf.append(sVal);
                	}
                }
                else if(obj instanceof VirtualPointer)
                {
                	((VirtualPointer)obj).getProcessor().write(sVal);
                }
                else
                {
                	argError("StringFormat", "null", obj.getClass());
                }
            }
        }
    }
	
	private static class IntFormatElement extends GeneralFormatElement
    {
		private static LargeNumber[] MAX;
		
        private boolean signed;
        private boolean basicType;

        public IntFormatElement(String format)
        {
        	super(format);
            switch (this.type)
            {
                default:
                case 'd':
                case 'i':
                    //Signed decimal integer
                    signed = true;
                    basicType = true;
                    break;
                case 'o':
                    //Signed octal
                    signed = true;
                    basicType = false;
                    break;
                case 'x':
                case 'X':
                    //Unsigned hexadecimal integer
                    signed = false;
                    basicType = false;
                    break;
                case 'u':
                    //Unsigned decimal integer
                    signed = false;
                    basicType = true;
                    break;
            }
        }

        public String inFormat(Object obj)
        {
            StringBuffer bu = new StringBuffer();
            long value = 0;
            int bCount = 1;
            switch(this.length)
        	{
        		default:
        			if(obj instanceof Byte)
                	{
                		value = ((Byte)obj).byteValue();
                		bCount = 1;
                	}
                	else if(obj instanceof Short)
                	{
                		value = ((Short)obj).shortValue();
                		bCount = 2;
                	}
                	else if(obj instanceof Integer)
                	{
                		value = ((Integer)obj).intValue();
                		bCount = 4;
                	}
                	else if(obj instanceof Long)
                	{
                		value = ((Long)obj).longValue();
                		bCount = 8;
                	}
                	else if(obj instanceof Float)
                	{
                		value = Float.floatToIntBits(((Float)obj).floatValue());
                		bCount = 4;
                	}
                	else if(obj instanceof Double)
                	{
                		value = Double.doubleToLongBits(((Double)obj).doubleValue());
                		bCount = 8;
                	}
                	else if(obj instanceof VirtualPointer)
                	{
                		value = ((VirtualPointer)obj).getProcessor().readInt32();
                		bCount = 4;
                	}
                	else
                	{
                		argError("IntFormat", "0", obj.getClass());
                		value = 0;
                	}
        			break;
        		case 'h':
        			if(super.lengthDoubleSize)
        			{
        				value = ((Character)obj).charValue();
        				bCount = 2;
        			}
        			else
        			{
        				value = ((Short)obj).shortValue();
        				bCount = 2;
        			}
        			break;
        		case 'z':
        		case 'j':
        		case 't':
        			value = ((Integer)obj).intValue();
        			bCount = 4;
        			break;
        		case 'l':
        			if(super.lengthDoubleSize)
        			{
        				value = ((Long)obj).longValue();
        				bCount = 8;
        			}
        			else
        			{
        				value = ((Integer)obj).longValue();
        				bCount = 4;
        			}
        			break;
        	}
            if (this.flags != null)
            {
                if (value >= 0)
                {
                    if (flags.indexOf(' ') >= 0)
                    {
                        bu.append(' ');
                    }
                    else if (flags.indexOf('+') >= 0)
                    {
                        bu.append('+');
                    }
                }
            }
            String str;
            if (signed)
            {
                if (basicType)
                {
                	str = thousandsSep(flags, Long.toString(value));
//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
                	bu.append(StringUtilities.pad(str, '0', this.length, true));
//#else
                    if (str.length() < this.length)
                    {
                    	char[] chars = new char[this.length - str.length()];
                    	Arrays.fill(chars, '0');
                        bu.append(chars);
                    }
                    bu.append(str);
//#endif
                }
                else
                {
                	//It's actually an unsigned print out
                	if(value < 0)
                	{
                		str = negNumberToUnsigned(bCount, value).toString(8);
                	}
                	else
                	{
                		str = Long.toString(value, 8);
                	}
                    if (flags != null && flags.indexOf('#') >= 0 && value != 0)
                    {
                        bu.append('0');
                    }
//#ifndef BlackBerrySDK4.5.0 | BlackBerrySDK4.6.0 | BlackBerrySDK4.6.1 | BlackBerrySDK4.7.0 | BlackBerrySDK4.7.1 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0
                    bu.append(StringUtilities.pad(str, '0', this.length - bu.length(), true));
//#else
                    if (str.length() + bu.length() < this.length)
                    {
                    	char[] chars = new char[(this.length + bu.length()) - str.length()];
                    	Arrays.fill(chars, '0');
                        bu.append(chars);
                    }
                    bu.appen
