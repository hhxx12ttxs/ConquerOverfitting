/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)Utils.java 
 *
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package com.sun.stc.common.utils;

import java.util.*;
import java.text.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.image.*;
import java.applet.*;

/** Assorted static utility routines.
 * 
 * <P>
 * Whenever I come up with a static routine that might be of general use,
 * I put it here.  So far the class includes:
 * <UL>
 * <LI> some string routines that were left out of java.lang.String
 * <LI> some filename manipulating routines
 * <LI> a color-spec parser
 * <LI> a full-precision replacement for Double.toString()
 * <LI> a fixed version of java.io.InputStream's byte-array read routine
 * <LI> a standard broken-image icon
 * <LI> a thick-line drawing routine
 * </UL>
 * and lots more.
 * <P>
 */
public class Utils
{
   /* ********************************************************* */
   /* PUBLIC STATIC METHODS                                     */
   /* ********************************************************* */

      public static boolean equalsStringOrNull(String s1, String s2)
      {
         boolean equal = true;
         if (s1 != null)
         {
           equal = (s2 != null) && s1.equals( s2 );
         }
         else
         {
           equal = (s2 == null);
         } // if
         return equal;
      }


    public static String removeBlankLines(String in)
    {
       String out = new String(in);
       if (in != null)
       {
          out = "";
          StringReader sr = new StringReader(in);
          BufferedReader br = new BufferedReader(sr);
          String s="";
          while (s!=null)
          {
            try
            {
              s = br.readLine();
            }
            catch (Exception e)
            {
              s = null;
            }
            if (s!=null)
            {
              s = s.trim();
              if (!s.equals(""))
              {
                out += s + "\n";
              } // if
            } // if
          } // while
          try
          {
            br.close();
          }
          catch (Exception e)
          {
          }
       } 
       return out;
    }

    public static String getLocaleString()
    {
      String locale_name = getDefaultLocale().toString();
      return locale_name; 
    }

    public static Locale getDefaultLocale()
    {
      MessageFormat mf = new MessageFormat("");
      Locale l = mf.getLocale();
      return l;
    }


    /** <p> Remove quotes from around a string */
    public static String removeQuotes(String in)
    {
       String out = in.trim();
       if (isQuoted(in))
       {
         out=out.substring(1,out.length()-1);
       } // if
       return out;
    }

    public static boolean isQuoted(String in)
    {
       boolean quoted = false;
       if (in != null)
       {
         quoted =  (in.length() > 1) &&
          (in.charAt(0) == '\"') && (in.charAt( in.length() - 1 ) == '\"');
       }
       return quoted;
    }

      public static final int findEndingQuote(String s)
      {
        int pos=-1;
        if (s.length() > 1)
        {
          for (int i=1; (pos==-1) && (i<s.length()); i++)
          {
            if ((s.charAt(i) ==  '\"') &&  // found unescaped "
                (s.charAt(i-1) != '\\'))
            {
              pos=i;
            } // if
          } // for
        } // if
        return pos;
      }

    public static String addQuotes(String in)
    {
       String out = "";
       if ((in != null))
       {
         if (!isQuoted(in))
         { 
           out = "\"" + in + "\""; 
         } 
       }
       return out;
    }
    /** <p> Create an array with the same items as in this vector */
    public static final Object[] copyVectorToArray(Vector vec)
    {
      Object[] o = new Object[ vec.size() ] ;
      vec.copyInto( o );
      return o;
    }

    /**
     * <p>
     * Given a vector print it out as follows.
     * <code>  
     *    <label>: item1, item2, item3 ... itemN
     *             itemO, itemP, itemQ ... itemZ
     * </code>
     * Where no long is longer than maxChar
     * <p>
     * NOTE: this is only useful for items that can be represented
     * as a simple string.
     * <p>
     * @param label is the label to prepend the list
     * @param vec is a vector of simple strings
     * @param max_width is the maximum characters to allow
     * <p>
     * @return the wrapped string. 
     */
     public static final String getVectorWrappedString(
       String label,
       Vector vec,
       int max_width)
     {
       int len;
       int indent;
       String out=label;
       String pad;

       len = label.length();
       indent = len;
       char[] p = new char[ indent ];
       for (int i=0; i<p.length; i++)
       {
         p[i]=' ';
       }
       pad = new String(p);
       for (int i=0; i<vec.size(); i++)
       {
          String s = vec.elementAt(i).toString();
          /* needs to be wrapped ? */
          int sep_space = (i==0) ? 0 : 2;  
          if ((len != indent) && ((len+s.length()+sep_space) > max_width))
          {
             out += "\n";
             out += pad + s;
             len = indent + s.length();
          }
          else
          {
             if (i!=0)
             {
               out += ", " + s;
               len += s.length() + 2;
             }
             else
             {
               out += s;
               len += s.length();
             }
          }
       }
       return out;
     }
       

     public static final String getVectorListString(Vector vec )
     {
       return getVectorString(vec,", ");
     }

     public static final String getVectorString(Vector vec, String sep)
     {
       String s="";
       if ((vec!=null) && (vec.size() > 0))
       {
         for (int i=0; i<vec.size(); i++)
         {
           if (i!=0)
           {
             s += sep;
           } // if
           s += vec.elementAt(i);
         } // for
       }
       else
       {
         s="<none>";
       } // if
       return s;
     }

    /** <p> Append the items in the array to this vector */
    public static final void addArrayToVector(Vector vec, Object[] o)
    {
      if ((vec != null) && (o != null))
      {
        for (int i=0;i<o.length;i++)
        {
          vec.addElement(o[i]);
        } // for
      } // if
    }


     public static final void appendVector(Vector vec, Vector append)
     {
       if ((vec != null) && (append != null))
       {
          for (int i=0; i<append.size(); i++)
          {
            vec.addElement( append.elementAt(i) );
          } // for
       } // if
     }

      public static Vector getVecConcatenated(Vector first, Vector second)
      {
	Vector result = new Vector(0);
	
	if (first.size() == 0)
	{
	  result = (Vector)second.clone();
	}
	else if (second.size() == 0)
	{
	  result = (Vector)first.clone();
	}
	else
	{
	  int second_size = second.size();
	  result = (Vector) first.clone();
	  for (int i = 0; i < second_size; i++)
	  {
	    result.addElement(second.elementAt(i));
	  }
	}
	return result;
      }

    /** <p> Append the items not already in the array to this vector */
    public static void addArrayUniqueToVector(Vector vec, Object[] o)
    {
      if ((vec != null) && (o != null))
      {
        for (int i=0;i<o.length;i++)
        {
          addUniqueToVector(vec,o[i]);
        } // for
      } // if
   }

     public static final void appendVectorUniqueToVector(Vector vec,
        Vector append)
     {
       if ((vec != null) && (append != null))
       {
          for (int i=0; i<append.size(); i++)
          {
            addUniqueToVector(vec, append.elementAt(i) );
          } // for
       } // if
     }

    /** <p> Add an item to the vector if not already in the vector */
    public static void addUniqueToVector(Vector vec, Object o)
    {
       if ((vec != null) && (o != null))
       {
         if (!vec.contains( o ))
         {
          vec.addElement( o );
         }
       }
    }

    public static String copyString(String src)
    {
      String dest = null;
      if (src != null)
      {
        dest = new String(src);
      }
      return  dest;
    }

     /** 
      * <p> 
      * Does this string represent a 0.
      * <p>
      * Representations of 0 include "0","-0","000000...000","-0000...0000"
      */
     public static boolean isZero(String value)
      {
        boolean zero=true;
        if (value.charAt(0)=='-')
        {
          value=value.substring(1);
        }
        for (int i=0;zero&&(i<value.length());i++)
        {
          zero=value.charAt(i)=='0';
        }
        return zero;
      }


    /**
     * <p>
     * Is this an absolute path.
     * <p>
     * @param filename the filename path to check
     * <p>
     * @return true if the path is absolute; false if the path is relative
     */
    public static final boolean isPathAbsolute(String filename)
    {
      boolean isabsolute = false;

      if ((filename!=null) &&  (filename.length() > 0)) 
      {
        if (System.getProperty("os.name").startsWith("Win"))
        {
           isabsolute=isWinPathAbsolute(filename);
        }
        else // Unix
        {
          isabsolute=isPathAbsolute(filename,"/");
        }
      } 
      return isabsolute;   
    }

    private final static boolean isWinPathAbsolute(String filename)
    {
      boolean isabsolute=false;
      if (startsWithDrive(filename))
      {
        isabsolute=isPathAbsolute(filename.substring(2),"\\/"); 
      }
      else
      {
        isabsolute=isPathAbsolute(filename,"\\/");
      }
      return isabsolute;
   }

   private final static boolean isPathAbsolute(String filename,String sep)
   {
    boolean isabsolute=false;
    // the first character is a path seperator
    if (sep.indexOf(filename.charAt(0)) != -1)
      isabsolute = true;
    return isabsolute;
   }

   public final static boolean equals(Object o1, Object o2)
   {
     boolean equals = false;
     equals = (o1 == o2);  // same object
     if (!equals)
     {
       if ((o1 != null) &&
           (o2 != null))
       {
         equals = o1.equals(o2);
       }
     } // if
     return equals;
   }

    /**
     * <p>
     * Does the path start with a drive letter?
     * <p>
     * @param filename the path to check
     * <p>
     * @return true if starts with a drive letter; otherwise false
     */
    public final static boolean startsWithDrive(String filename)
    {
      boolean starts=false;
      starts=((filename.length() > 2) && (filename.charAt(1)==':') &&
       (((filename.charAt(0) >= 'A') && (filename.charAt(0) <= 'Z') ||
         (filename.charAt(0) >= 'a') && (filename.charAt(0) <= 'z'))));
      return starts;
    }

    /* STC enhancement */
    /**
     * <p>
     * Given the current directory and a filename return the fullpath of the
     * the file.
     * <p>
     * For example 
     * <ul> 
     * <li>getFullPath("/home/user1","my.file") would return 
     *     "/home/user1/my.file"
     * <li>getFullPath("/home/user1","/home/user2/tmp/my.file") would return
     *     "/home/user2/tmp/my.file"
     * <li>getFullPath("/home/user1","c:/home/user3/my.file") on Windows would
     *     return "c:/home/user3/my.file" on Unix would return 
     *     "/home/user1/c:/home/user3/my.file"
     * </ul>
     * <p>
     * @param cur_dir the current directory that relative paths sould be
     *        relative to
     * @param filename the absolute or relative filename.
     * <p>
     * @return the full path
     */
    public final static String getFullPath(String cur_dir, String filename)
    {
      String fn=filename;
      // if the last character of the cur_dir is a seperator strip it
      char last_char=cur_dir.charAt(cur_dir.length()-1);
      if (System.getProperty("os.name").startsWith("Win"))
      {  
        if ((last_char=='\\')||(last_char=='/'))
          cur_dir=cur_dir.substring(0,cur_dir.length()-1);
      }
      else // unix
      {
        if (last_char=='/')
          cur_dir=cur_dir.substring(0,cur_dir.length()-1);
      }
      // get full path based on the system we are on
      if (System.getProperty("os.name").startsWith("Win"))
      {
        if (startsWithDrive(filename)&&!isPathAbsolute(filename))
        {
          if (startsWithDrive(cur_dir))
          { cur_dir=cur_dir.substring(2); }
          fn=filename.substring(0,2) + cur_dir + "\\" + 
               filename.substring(2);
        }
        else if (!isPathAbsolute(filename))
        {
            fn=cur_dir + "\\" + filename;
        }
        // make all paths look like unix paths
        fn=fn.replace('\\','/');
      }
      else // Unix
      {
        if (!isPathAbsolute(filename))
        { fn=cur_dir+"/"+filename; }
      }
      return fn;
    }

    /* STC enhancement */
    /**
     * <p>
     * Given a Date object create a string in the following format: 
     * <p>
     * YYYYMMDDhhmmss 
     * <p>
     * @param d the date object to represent
     * <p>
     * @return the date in the correct format.
     */
    public static String makeDateString(Date d)
    {
     GregorianCalendar cal=new GregorianCalendar();
     if (d!=null)
     {
       cal.setTime(d);
     }
     int year=cal.get(Calendar.YEAR);
     int month=cal.get(Calendar.MONTH);
     String MM =(month < 10)? "0"+String.valueOf(month) : String.valueOf(month)
;
     int day=cal.get(Calendar.DATE);
     String DD=(day < 10)?  "0" + String.valueOf(day) : String.valueOf(day);
     int hour=cal.get(Calendar.HOUR_OF_DAY);
     String hh=(hour < 10)?  "0" + String.valueOf(hour) : String.valueOf(hour);
     int min=cal.get(Calendar.MINUTE);
     String mm=(min < 10)?  "0" + String.valueOf(min) : String.valueOf(min);
     int sec=cal.get(Calendar.SECOND);
     String ss=(sec < 10)?  "0" + String.valueOf(sec) : String.valueOf(sec);
     String date_time= String.valueOf(year)+
                       MM +
                       DD +
                       hh +
                       mm +
                       ss;
     return date_time;
    }

    /**
      * <p>
      * Given a string in the format of YYYYMMDDhhmmss create a Date  
      * object.
      * <p> 
      * @param date_time a string in the format of YYYMMDDhhmmss
      * <p> 
      * @return the Date object represented by the string.
      */
    public static GregorianCalendar getDateFromDateString(String date_time)
    {
     int year=0;
     int month=0;
     int day=0;
     int hour=0;
     int min=0;
     int sec=0;
     try 
     {
       // YYYYMMDDhhmmss
       // ^  ^
       year=Integer.parseInt(date_time.substring(0,4));
     } catch (Exception e){}
     try 
     {
       // YYYYMMDDhhmmss
       //     ^^
       month=Integer.parseInt(date_time.substring(4,6));
     } catch (Exception e){}
     try 
     {
       // YYYYMMDDhhmmss
       //       ^^
       day=Integer.parseInt(date_time.substring(6,8));
     } catch (Exception e){}
     try 
     {
       // YYYYMMDDhhmmss
       //         ^^
       hour=Integer.parseInt(date_time.substring(8,10));
     } catch (Exception e){}
     try 
     {
       // YYYYMMDDhhmmss
       //           ^^
       min=Integer.parseInt(date_time.substring(10,12));
     } catch (Exception e){}
     try 
     {
       // YYYYMMDDhhmmss
       //             ^^
       sec=Integer.parseInt(date_time.substring(12));
     } catch (Exception e){}
     GregorianCalendar d=new GregorianCalendar(year,month-1,day,hour,min,sec);
     return d;
    }

    /* STC enhancement */
    /**
     * <p>
     * Shorten a the fullpath of a file to fit into <var>len</var> characters.
     * <p>
     * This method will preserve the basename and extention and shorten the
     * directory information as needed placeing "..." in parts of the directory
     * specification in order to shorten the string.
     * <p>
     * This is useful to display the filename as part of a title or in a
     * fixed length label.
     * <p>
     * @param full_path the full path of the file.
     * @param len the number of characters to shorten the full path to
     * <p>
     * @return the shortened string.
     */
    public static String makeAbbreviatedPath(String full_path,int len)
    {
     String abv="";
     String fn=getBasename(full_path);
     if (full_path.length()<=len)
     {
      abv=full_path;
     }
     else if (fn.length()>=(len-3))
     {
      abv=fn;
     }
     else
     {
      int len_in=len;
      len=(len - 5 - fn.length());
      if (len < 0)
      {
        abv=".../"+fn;
        if ((len_in-fn.length()) >= 5) 
        { abv="/.../"+fn; }
        else if ((len_in-fn.length()) >= 4)
        { abv=".../"+fn; }
        else if ((len_in-fn.length()) >= 3)
        { abv="..."+fn; }
        else
        { abv=fn; }
      }
      else
      {
        String dir=getDirname(full_path);
        String tmp=dir.substring(0,len);
        if (tmp.equals(dir))
        {
          abv=full_path;
        }
        else
        {
          // if the last word of our string was cut off get rid of it
          int last=tmp.lastIndexOf('/');
          if (last > -1)
          {
            String last_part=dir.substring(last);
            StringTokenizer st=new StringTokenizer(last_part,"/");
            if (st.hasMoreTokens())
            {
               String last_word=st.nextToken();
               String last_tmp_word=tmp.substring(last+1);
               if (!last_word.equals(last_tmp_word))
               {
                 tmp=tmp.substring(0,last);
               }
               abv=tmp+"/.../"+fn;
            }
            else // should never come here but just in case
            {
             abv=tmp+".../"+fn;
            } 
          }
          else
          {
            abv=full_path;
          }
        } 
      }
     }
     return abv;
    }

    /* STC enhancement */
    /**
      * <p>
      * Get the directory portion of a filename.
      * <p>
      * The filename is given as: 
      * <var>directory</var>/<var>basename</var>.<var>extension</var>
      * <p>
      * @param file the full filename
      * <p>
      * @return the directory portion of the filename
      */
    public static String getDirname(String file)
    {
      String dir=".";
      int end=file.lastIndexOf(File.separator);
      if (System.getProperty("os.name").startsWith("Win"))
      {
        end = Math.max(end, file.lastIndexOf("/"));
      }
      if (end > -1)
      {
        dir=file.substring(0,end);
      }
      return dir;
    }

    /* STC enhancement */
    /**
      * <p>
      * Get the basename portion of a filename.  The basename is
      * the basename.extension without the directory.
      * <p>
      * The filename is given as: 
      * <var>directory</var>/<var>basename</var>.<var>extension</var>
      * <p>
      * @param file the full filename
      * <p>
      * @return the basename portion of the filename
      */
    public static String getBasename(String file)
    {
      String name=file;
      int end=file.lastIndexOf(File.separator);
      if (System.getProperty("os.name").startsWith("Win"))
      {
         int nt_end = file.lastIndexOf("/");
         end = Math.max(nt_end, end);
      }
      if (end > -1)
      {
        name=file.substring(end+1);
      }
      return name;
    }

    /* STC enhancement */
    /**
      * <p>
      * Get the basename portion of a filename without the extension.  
      * <p>
      * The filename is given as: 
      * <var>directory</var>/<var>basename</var>.<var>extension</var>
      * <p>
      * @param file the full filename
      * <p>
      * @return the basename portion of the filename
      */
    public static String getBasenameNoExt(String file)
    {
      String name=getBasename(file);
      int end=name.lastIndexOf(".");
      if (end > -1)
      {
        name=name.substring(0,end);
      }
      return name;
    }

    public static String getExtension(String file)
    {
      String ext=""; 
      if (file != null)
      {
         int pos = file.lastIndexOf(".");
         if (pos > 0) 
         {
            ext = file.substring( pos+1 );
         }
      }
      return ext;
    }
    
    /* STC enhancement */
   /**
    * <p>
    * Calculates the average of an array of integers
    * <p>
    * @param wid the array of integers to average
    * <p>
    * @return the average as an integer
    */
   public static int averageInts(int[] wid)
   {
     int ave=0;
     int total=0;
     for (int i=0;i<wid.length;i++)
     {
       total+=wid[i];
     }
     if (wid.length > 0)
     { ave = total / wid.length; }
     return ave;
   }


    /* STC enhancement */
    /** 
     * <p>
     * Make sure that a string is <var>cols</var> characters long.
     * <p>
     * if the string is too long clip it otherwise add blanks after
     * the end of a string to make it <var>cols</var> characters long.
     * <p>
     * @param s the string to pad/clip
     * @param how long we want the string
     * <p>
     * @return the string of <var>cols</var> length.
     */
    public static String padString(String s, int cols)
    {
    
      String r=s;
      if (s.length() > cols)
      {
       r = clipString(s,cols);
      }
      else if (s.length() < cols)
      {
       int diff=cols-s.length();
       for (int i=0;i<diff;i++)
       {  r+=" "; }
      }
      return r;
    }

    /* STC enhancement */
    /**
      * <p>
      * Center a string within a certain number of columns.
      * <p>
      * @param s the string to center
      * @param cols the number of columns to center the string in.
      * <p>
      * @return the centered string.
      */
    public static String centerString(String s, int cols)
    {
      String r=s;
      if (s.length() > cols)
      {
       r = clipString(s,cols);
      } 
      else if (s.length() < cols)
      {
       int diff=cols-s.length();
       int pre=diff/2;
       int post=diff - pre;
       r="";
       for (int i=0;i<pre;i++)
       {  r+=" "; }
       r+=s;
       for (int i=0;i<post;i++)
       {  r+=" "; }
      }
      return r;
    }

    /**
     * <p>
     * If needed clip the string to a predefined length.
     * <p>
     * @param s the string to clip
     * @param cols the length to clip it to
     * <p>
     * @return the clipped string. 
     */
    public static String clipString(String s, int cols)
    {
      String r=s;
      if (s.length() > cols)
         r=s.substring(0,cols);
      return r;  
    }

    /**
     * <p>
     * Word wrap the string such that it fits in <var>width</var> columns.
     * <p>  
     * Words are split using a \n.  \n are treated as paragraph breaks.
     * <p>
     * Calls wordWrap(in,width," ","/","+");
     * <p>
     * @param in the string to wrap
     * @param width the maximum number of characters in a line
     * <p>
     * @return the wrapped string. 
     */
    public static String wordWrap(String in, int width)
    { return wordWrap(in,width," ","/","+"); }



    /* STC enhancement */
    /**
     * <p>
     * Word wrap the string such that it fits in <var>width</var> columns.
     * <p>  
     * Words are split using a \n.  
     * <p>  
     * \n is treated as a paragraph break.
     * <p>
     * @param in the string to wrap
     * <p>
     * @param width the maximum number of characters in a line
     * <p>
     * @param break_chars the characters that are treated as white space.
     *        These characters indicate the end of a word.
     * <p>
     * @param cont_char a character that acts as a hyphen (-) for a string.
     *        A string may be broken on this character but if it is the
     *        <var>line_cont</var> character will be appended to the broken  
     *        line.  A typical value for cont_char is "\"
     * <p>
     * @param line_cont the character to append to the end of a line 
     *        if a cont_char allowed the string to be broken.  This symbols
     *        indicates that the value in this string continues on the next
     *        line.
     * <p>
     * @return the wrapped string. 
     */
    public static String wordWrap(String in, int width,String break_chars,
        String cont_char,String line_cont)
    {
      String out="";
      String word="";
      int cur_len=0;
      int start=0;
      for (int i=0;i<in.length();i++)
      {
        if (break_chars.indexOf(in.charAt(i))!=-1)
        {
          int char_off=0;
          if (!in.substring(i,i+1).equals(" "))
             char_off++;
          word=in.substring(start,i+char_off);
          if (word.length() >= width)
          {
            if (word.lastIndexOf(cont_char)!=-1)
            { 
             out+="\n";
             int len=2;
             int pre=0;
             while(word.length()+pre > width)
             {
              String tmp=word.substring(0,width-len);
              tmp=tmp.substring(0,tmp.lastIndexOf(cont_char)+1);
              out+=tmp+" "+line_cont+"\n    ";
              len=6;
              pre=4;
              word=word.substring(tmp.length());
              if (word.lastIndexOf(cont_char) == -1)
              {
                 out+=word;
                 word="";
              }
             }
             out+=word+" ";
             cur_len=out.length();
            }
            else
            {
              out+="\n"+word;   
              cur_len=word.length();
            }
            start=i+1;
          }
          else
          {
            if (word.length() + cur_len+1 <= width)
            {
              if (cur_len == 0)
              {
                out+=word; 
              }
              else
              {
                out+=" "+word;
              }
              cur_len+=word.length()+1;
              start=i+1;
            }
            else
            {
              out+="\n"+word;
              cur_len=word.length();
              start=i+1;
            }
          }
        }
        else if (in.charAt(i) == '\n')
        {
          word=in.substring(start,i);
          if (word.length() >= width)
          {
            out+="\n"+word;   
            cur_len=word.length();
          }
          else
          {
            if (word.length() + cur_len+1 <= width)
            {
              if (cur_len == 0)
              {
                out+=word; 
              }
              else
              {
                out+=" "+word;
              }
              cur_len+=word.length()+1;
            }
            else
            {
              out+="\n"+word;
              cur_len=word.length();
            }
          }
          out+="\n \n";
          cur_len=0;
          start=i+1;
        }
      }
      if (start < in.length() )
      {
        word=in.substring(start,in.length());
        if (cur_len>0)
        { 
          if (cur_len+word.length() > width)
          {
            out+="\n";
          }
          else
          {
            out+=" "; 
          }
        }
        if (word.length() >= width)
        {
            if (word.lastIndexOf(cont_char)!=-1)
            {
             out+="\n";
             int len=1;
             int pre=0;
             while(word.length()+pre > width)
             {
              String tmp=word.substring(0,width-len);
              tmp=tmp.substring(0,tmp.lastIndexOf(cont_char)+1);
              tmp=tmp.substring(0,tmp.lastIndexOf(cont_char)+1);
              out+=tmp+" "+line_cont+"\n    ";
              len=6;
              pre=4;
              word=word.substring(tmp.length());
              if (word.lastIndexOf(cont_char) == -1)
              {
                 out+=word;
                 word="";
              }
             }
            }
        }
        out+=word;
      }
      return out;
    }


    /* STC enhancement */
    /**
     * <p>
     * Is this string in the following array? 
     * <p>
     * <strong>NOTE:</strong>Search is case sensitive.
     * <p>
     * @param cur the string to look for
     * @param array the array of strings to look through
     * <p>
     * @return true if the string is in the array; false otherwise
     */ 
    public static boolean stringInArray(String cur, String[] array)
    { 
     boolean found=false;
     if ((cur!=null)&&(array!=null))
     {
       for (int i=0;!found&&(i<array.length);i++)
       {
         if (array[i]!=null)
         { found=array[i].equals(cur); }
       }
     }
     return found;
    }

    /**
     * <p>
     * Assign up to out.length items from the input array to the
     * output array.
     * <p>
     * @param in the array of input items
     * @param out the output array
     * <p>
     * @return the output array
     */ 
    public static Object[] copyArray(Object[] in, Object[] out)
    {
      for (int i=0;(i<in.length)&&(i<out.length);i++)
      {
         out[i]=in[i]; 
      }
      return out;
    }

    /**
     * <p>
     * Print all the objects in the array to standard out.
     * <p>
     * @param array the array to print
     */
    public static void printArray(Object[] array)
    {
      System.out.println( getArrayString( array, null ));
    }

    public static String getArrayString(Object[] array)
    {
      return getArrayString(array,",");
    }

    public static String getArrayString(Object[] array, String sep)
    {
      String s="";
      for (int i=0;i<array.length;i++) 
      {
        if (sep == null)
        {
          s += "[" + i + "] = ";
        }
        else if (i!=0)
        {
          s += sep;
        } // if
        s += array[i];
      } // for
      return s;
    }

    /// Returns a date string formatted in Unix ls style - if it's within
    // six months of now, Mmm dd hh:ss, else Mmm dd  yyyy.
    /* REMOVED in order to get rid of circular dependency that
       this code causes between Fmt && Utils
    public static String lsDateStr( Date date )
        {
        long dateTime = date.getTime();
	if ( dateTime == -1L )
	    return "------------";
        long nowTime = (new Date()).getTime();
        String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        String part1 = months[date.getMonth()] + Fmt.fmt( date.getDate(), 3 );
        if ( Math.abs( nowTime - dateTime ) < 183L * 24L * 60L * 60L * 1000L )
            return part1 + Fmt.fmt( date.getHours(), 3 ) + ":" +
                Fmt.fmt( date.getMinutes(), 2, Fmt.ZF );
        else
            return part1 + Fmt.fmt( date.getYear() + 1900, 6 );
        }
     */


    /** 
     * <p>
     * Returns the length of the initial segment of <var>str</var> 
     * which consists entirely of characters from <var>charSet</var>.
     */
    public static int strSpan( String str, String charSet )
	{
	int i;
	for ( i = 0; i < str.length(); ++i )
	    if ( charSet.indexOf( str.charAt( i ) ) == -1 )
		break;
	return i;
	}

    /**
     * <p>
     *  Returns the length of the initial segment of <var>str</var> 
     *  which consists entirely of characters NOT from <var>charSet</var>.
     */
    public static int strCSpan( String str, String charSet )
    {
	int i;
	for ( i = 0; i < str.length(); ++i )
	    if ( charSet.indexOf( str.charAt( i ) ) != -1 )
		break;
	return i;
    }

    /**
     * <p>
     * Checks whether a string matches a given wildcard pattern.
     * Only does ? and *, and multiple patterns separated by |.
     * <p>
     * @param pattern the pattern to match
     * @param string does this string match the pattern?
     * <p>
     * @return true if the string matches the pattern; false otherwise.
     */
    public static boolean match( String pattern, String string )
	{
	for ( int p = 0; ; ++p )
	    {
	    for ( int s = 0; ; ++p, ++s )
		{
		boolean sEnd = ( s >= string.length() );
		boolean pEnd = ( p >= pattern.length() ||
				 pattern.charAt( p ) == '|' );
		if ( sEnd && pEnd )
		    return true;
		if ( sEnd || pEnd )
		    break;
		if ( pattern.charAt( p ) == '?' )
		    continue;
		if ( pattern.charAt( p ) == '*' )
		    {
		    int i;
		    ++p;
		    for ( i = string.length(); i >= s; --i )
			if ( match(
			       pattern.substring( p ),
			       string.substring( i ) ) )  /* not quite right */
			    return true;
		    break;
		    }
		if ( pattern.charAt( p ) != string.charAt( s ) )
		    break;
		}
	    p = pattern.indexOf( '|', p );
	    if ( p == -1 )
		return false;
	    }
	}

    /**
     * <p>
     * Finds the maximum length of a string that matches a given wildcard
     * pattern.  Only does ? and *, and multiple patterns separated by |.
     */
    public static int matchSpan( String pattern, String string )
	{
	// !!!
	return 0;
	}

    /** 
      * <p>
      * Returns the length of the initial segment of <var>str1</var> that 
      * equals <var>str2</var>.
      */
    public static int sameSpan( String str1, String str2 )
	{
	int i;
	for ( i = 0;
	      i < str1.length() && i < str2.length() &&
		str1.charAt( i ) == str2.charAt( i );
	      ++i )
	    ;
	return i;
	}

    /**
     * <p>
     * Returns the number of times the given character appears in the string.
     */
    public static int charCount( String str, char c )
	{
	int n = 0;
	for ( int i = 0; i < str.length(); ++i )
	    if ( str.charAt( i ) == c )
		++n;
	return n;
	}


    /**
     * <p>
     * Turns a String into an array of Strings, by using StringTokenizer
     * to split it up at whitespace.
     */
    public static String[] splitStr( String str )
	{
	StringTokenizer st = new StringTokenizer( str );
	int n = st.countTokens();
	String[] strs = new String[n];
	for ( int i = 0; i < n; ++i )
	    strs[i] = st.nextToken();
	return strs;
	}

    /**
     * <p>
     * Turns an array of Strings into a single String, with the components
     * separated by spaces.
     */
    public static String flattenStrarr( String[] strs )
	{
	StringBuffer sb = new StringBuffer();
	for ( int i = 0; i < strs.length; ++i )
	    {
	    if ( i > 0 )
		sb.append( ' ' );
	    sb.append( strs[i] );
	    }
	return sb.toString();
	}

    /**
     * <p> 
     * Sorts an array of Strings using a bubblesort.
     * <p>
     * It is recommended that StringQSort be used for any but
     * really short string arrays.
     * <p> 
     * Java currently has no general sort function.  Sorting Strings is
     * common enough that it's worth making a special case.
     */
    public static void sortStrings( String[] strings )
	{
	// Just does a bubblesort.
	for ( int i = 0; i < strings.length - 1; ++i )
	    {
	    for ( int j = i + 1; j < strings.length; ++j )
		{
		if ( strings[i].compareTo( strings[j] ) > 0 )
		    {
		    String t = strings[i];
		    strings[i] = strings[j];
		    strings[j] = t;
		    }
		}
	    }
	}


    /**
     * <p>
     * Returns the number a raised to the power of <var>b</var>.  Long version
     * of Math.pow().  
     * <p>
     * @exception java.lang.ArithmeticException if <var>b</var> is negative.
     */
    public static long pow( long a, long b )
	throws ArithmeticException
	{
	if ( b < 0 )
	    throw new ArithmeticException();
	long r = 1;
	while ( b != 0 )
	    {
	    if ( odd( b ) )
		r *= a;
	    b >>>= 1;
	    a *= a;
	    }
	return r;
	}


    /**
     * <p>
     * Improved version of Double.toString(), returns up to 16 digits.
     */
    public static String doubleToString( double d )
	{
	// As of JDK 1.0, Double.toString() is a native method that just
	// does an sprintf with a %g.  At least on some systems, this returns
	// only six decimal places.  This replacement version gives the full
	// sixteen digits.

	// Handle special numbers first, to avoid complications.
	if ( Double.isNaN( d ) )
	    return "NaN";
	if ( d == Double.NEGATIVE_INFINITY )
	    return "-Inf";
	if ( d == Double.POSITIVE_INFINITY )
	    return "Inf";

	// Grab the sign, and then make the number positive for simplicity.
	boolean negative = false;
	if ( d < 0.0D )
	    {
	    negative = true;
	    d = -d;
	    }

	// Get the native version of the unsigned value, as a template.
	String unsStr = Double.toString( d );

	// Dissect out the exponent.
	String mantStr, expStr;
	int exp;
	int eInd = unsStr.indexOf( 'e' );
	if ( eInd == -1 )
	    {
	    mantStr = unsStr;
	    expStr = "";
	    exp = 0;
	    }
	else
	    {
	    mantStr = unsStr.substring( 0, eInd );
	    expStr = unsStr.substring( eInd + 1 );
	    if ( expStr.startsWith( "+" ) )
		exp = Integer.parseInt( expStr.substring( 1 ) );
	    else
		exp = Integer.parseInt( expStr );
	    }

	// Dissect out the number part.
	String numStr;
	int dotInd = mantStr.indexOf( '.' );
	if ( dotInd == -1 )
	    numStr = mantStr;
	else
	    numStr = mantStr.substring( 0, dotInd );
	long num;
	if ( numStr.length() == 0 )
	    num = 0;
	else
	    num = Integer.parseInt( numStr );

	// Build the new mantissa.
	StringBuffer newMantBuf = new StringBuffer( numStr + "." );
	double p = Math.pow( 10, exp );
	double frac = d - num * p;
	String digits = "0123456789";
	int nDigits = 16 - numStr.length();	// about 16 digits in a double
	for ( int i = 0; i < nDigits; ++i )
	    {
	    p /= 10.0D;
	    int dig = (int) ( frac / p );
	    if ( dig < 0 ) dig = 0;
	    if ( dig > 9 ) dig = 9;
	    newMantBuf.append( digits.charAt( dig ) );
	    frac -= dig * p;
	    }

	if ( (int) ( frac / p + 0.5D ) == 1 )
	    {
	    // Round up.
	    boolean roundMore = true;
	    for ( int i = newMantBuf.length() - 1; i >= 0; --i )
		{
		int dig = digits.indexOf( newMantBuf.charAt( i ) );
		if ( dig == -1 )
		    continue;
		++dig;
		if ( dig == 10 )
		    {
		    newMantBuf.setCharAt( i, '0' );
		    continue;
		    }
		newMantBuf.setCharAt( i, digits.charAt( dig ) );
		roundMore = false;
		break;
		}
	    if ( roundMore )
		{
		// If this happens, we need to prepend a 1.  But I haven't
		// found a test case yet, so I'm leaving it out for now.
		// But if you get this message, please let me know!
		newMantBuf.append( "ROUNDMORE" );
		}
	    }

	// Chop any trailing zeros.
	int len = newMantBuf.length();
	while ( newMantBuf.charAt( len - 1 ) == '0' )
	    newMantBuf.setLength( --len );
	// And chop a trailing dot, if any.
	if ( newMantBuf.charAt( len - 1 ) == '.' )
	    newMantBuf.setLength( --len );

	// Done.
	return ( negative ? "-" : "" ) +
	       newMantBuf +
	       ( expStr.length() != 0 ? ( "e" + expStr ) : "" );
	}

   /** 
    * <p> 
    * An array-to-String routine.  Handles arrays of arbitrary
    * type, including nested arrays.    
    * <p>
    * Sample output:
    * <BLOCKQUOTE><CODE><PRE>
    * byte[]:    { (byte)0, (byte)1, (byte)2 }
    * char[]:    { '0', '1', '2' }
    * short[]:   { (short)0, (short)1, (short)2 }
    * int[]:     { 0, 1, 2 }
    * long[]:    { 0L, 1L, 2L }
    * float[]:   { 0F, 1F, 2F }
    * double[]:  { 0D, 1D, 2D }
    * String[]:  { "0", "1", "2" }
    * int[][]:   { { 0, 1, 2 }, { 3, 4, 5 } }
    * </PRE></CODE></BLOCKQUOTE>
    * <p>
    * @param o usually an array of some type. Example: char[] a={'0','1','2'};
    *        arrayToString(a);
    * <p>
    * @return the string representing the array.
    */
    public static String arrayToString( Object o )
	{
	if ( o == null )
	    return "null";
	String cl = o.getClass().getName();
	if ( ! cl.startsWith( "[" ) )
	    // It's not an array; just call its toString method.
	    return o.toString();
	StringBuffer sb = new StringBuffer( "{ " );
	if ( o instanceof byte[] )
	    {
	    byte[] ba = (byte[]) o;
	    for ( int i = 0; i < ba.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( "(byte)" );
		sb.append( ba[i] );
		}
	    }
	else if ( o instanceof char[] )
	    {
	    char[] ca = (char[]) o;
	    for ( int i = 0; i < ca.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( "'" );
		sb.append( ca[i] );
		sb.append( "'" );
		}
	    }
	else if ( o instanceof short[] )
	    {
	    short[] sa = (short[]) o;
	    for ( int i = 0; i < sa.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( "(short)" );
		sb.append( sa[i] );
		}
	    }
	else if ( o instanceof int[] )
	    {
	    int[] ia = (int[]) o;
	    for ( int i = 0; i < ia.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( ia[i] );
		}
	    }
	else if ( o instanceof long[] )
	    {
	    long[] la = (long[]) o;
	    for ( int i = 0; i < la.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( la[i] );
		sb.append( "L" );
		}
	    }
	else if ( o instanceof float[] )
	    {
	    float[] fa = (float[]) o;
	    for ( int i = 0; i < fa.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( fa[i] );
		sb.append( "F" );
		}
	    }
	else if ( o instanceof double[] )
	    {
	    double[] da = (double[]) o;
	    for ( int i = 0; i < da.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( da[i] );
		sb.append( "D" );
		}
	    }
	else if ( o instanceof String )
	    {
	    // Special-case Strings so we can surround them with quotes.
	    String[] sa = (String[]) o;
	    for ( int i = 0; i < sa.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( "\"" );
		sb.append( sa[i] );
		sb.append( "\"" );
		}
	    }
	else if ( cl.startsWith( "[L" ) )
	    {
	    // Some random class.
	    Object[] oa = (Object[]) o;
	    for ( int i = 0; i < oa.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( oa[i] );
		}
	    }
	else if ( cl.startsWith( "[[" ) )
	    {
	    // Nested arrays.
	    Object[] aa = (Object[]) o;
	    for ( int i = 0; i < aa.length; ++i )
		{
		if ( i > 0 ) sb.append( ", " );
		sb.append( arrayToString( aa[i] ) );
		}
	    }
	else
	    sb.append( "(unknown array type)" );
	sb.append( " }" );
	return sb.toString();
	}

    /**
     * <p>
     * Check if an object extends a given class or one of its superclasses.
     * <p>
     * Use this method as an instanceof that works on Class objects at 
     * runtime, instead of type descriptors at compile time.
     */
    public static boolean instanceOf( Object o, Class cl )
	{
	// Null check.
	if ( o == null || cl == null )
	    return false;
	Class ocl = o.getClass();
	// Check if they are the same class.
	if ( ocl.equals( cl ) )
	    return true;
	// If the class is not itself an interface, then check its interfaces.
	if ( ! cl.isInterface() )
	    {
	    Class ifs[] = cl.getInterfaces();
	    for ( int i = 0; i < ifs.length; ++i )
		if ( instanceOf( o, ifs[i] ) )
		    return true;
	    }
	// And check supeclasses.
	Class scl = cl.getSuperclass();
	if ( scl != null )
	    if ( instanceOf( o, scl ) )
		return true;
	// Guess not.
	return false;
	}


    private final static int bWidth = 20;
    private final static int bHeight = 19;
    private static int[] bPixels = {	// color model is AARRGGBB
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0x00ffffff,
	0x00ffffff, 0x00ffffff, 0x00ffffff, 0x00ffffff, 0x00ffffff,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,
	0x00ffffff, 0x00ffffff, 0x00ffffff, 0x00ffffff, 0x00ffffff,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xffffffff,
	0xff000000, 0x00ffffff, 0x00ffffff, 0x00ffffff, 0x00ffffff,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000,
	0xff00ff00, 0xff00ff00, 0xff00ff00, 0xff00ff00, 0xff000000,
	0xff000000, 0xffff00ff, 0xffff00ff, 0xff000000, 0xffffffff,
	0xffffffff, 0xff000000, 0x00ffffff, 0x00ffffff, 0x00ffffff,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff00ff00,
	0xff00ff00, 0xff000000, 0xff000000, 0xff00ff00, 0xff00ff00,
	0xff000000, 0xff000000, 0xffff00ff, 0xff000000, 0xffffffff,
	0xffffffff, 0xffffffff, 0xff000000, 0x00ffffff, 0x00ffffff,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff00ff00,
	0xff000000, 0xff000000, 0xffff00ff, 0xff000000, 0xff00ff00,
	0xff00ff00, 0xff000000, 0xffff00ff, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0x00ffffff,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000,
	0xff000000, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff00ff00,
	0xff00ff00, 0xff000000, 0xffff00ff, 0xffff00ff, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xff000000, 0xff00ff00, 0xff00ff00,
	0xff000000, 0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xff000000, 0xff00ff00, 0xff00ff00, 0xff000000,
	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xff000000, 0xff00ff00, 0xff00ff00, 0xff000000, 0xff000000,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xff000000, 0xff00ff00, 0xff000000, 0xff000000, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xff000000, 0xff000000, 0xff000000, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xff000000, 0xff000000, 0xff000000, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xff000000, 0xff00ff00, 0xff00ff00, 0xff000000, 0xff000000,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xff000000, 0xff00ff00, 0xff000000, 0xff000000, 0xff000000,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xff000000, 0xff000000, 0xff000000, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff, 0xffff00ff,
	0xffff00ff, 0xffff00ff, 0xffff00ff, 0xff000000, 0xff000000,

	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,

	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	0xff000000, 0xff000000, 0xff000000, 0xff000000, 0xff000000,
	};
    private static Image bImage = null;

    /**
     * <p>
     * Draw a broken-image image.
     */
    public static void brokenImage( Graphics graphics, Component comp )
	{
	if ( bImage == null )
	    bImage = comp.createImage(
		new MemoryImageSource( bWidth, bHeight, bPixels, 0, bWidth ) );
	Dimension d = comp.getSize();
	graphics.setColor( comp.getBackground() );
	graphics.fillRect( 0, 0, d.width, d.height );
	graphics.drawImage(
	    bImage, ( d.width - bWidth ) / 2, ( d.height - bHeight ) / 2,
	    null );
	}

    /**
     * <p>
     * Parse a color string into a Color.  The color can be specified
     * by name as one of:
     * <BLOCKQUOTE>
     * black blue cyan darkGray gray green lightGray
     * magenta orange pink red white yellow
     * </BLOCKQUOTE>
     * Or, as an #rrggbb hex number, like in Netscape.
     */
    public static Color parseColor( String str )
	{
	if ( str.startsWith( "#" ) )
	    {
	    try
		{
		int h = Integer.parseInt( str.substring( 1 ), 16 );
		return new Color(
		    ( h >>> 16 ) & 0xff, ( h >>> 8 ) & 0xff, h & 0xff );
		}
	    catch ( NumberFormatException e )
		{
		return null;
		}
	    }
	Color color;
	color = parseNamedColor( str );
	if ( color != null )
	    return color;
	if ( str.substring( 0, 4 ).equalsIgnoreCase( "dark" ) )
	    {
	    color = parseNamedColor( str.substring( 4 ) );
	    if ( color != null )
		return color.darker();
	    }
	if ( str.substring( 0, 5 ).equalsIgnoreCase( "light" ) )
	    {
	    color = parseNamedColor( str.substring( 5 ) );
	    if ( color != null )
		return color.brighter();
	    }
	if ( str.substring( 0, 6 ).equalsIgnoreCase( "bright" ) )
	    {
	    color = parseNamedColor( str.substring( 6 ) );
	    if ( color != null )
		return color.brighter();
	    }
	return null;
	}

    private static Color parseNamedColor( String str )
	{
	if ( str.equalsIgnoreCase( "black" ) )
	    return Color.black;
	if ( str.equalsIgnoreCase( "blue" ) )
	    return Color.blue;
	if ( str.equalsIgnoreCase( "cyan" ) )
	    return Color.cyan;
	if ( str.equalsIgnoreCase( "darkGray" ) )
	    return Color.darkGray;
	if ( str.equalsIgnoreCase( "gray" ) )
	    return Color.gray;
	if ( str.equalsIgnoreCase( "green" ) )
	    return Color.green;
	if ( str.equalsIgnoreCase( "lightGray" ) )
	    return Color.lightGray;
	if ( str.equalsIgnoreCase( "magenta" ) )
	    return Color.magenta;
	if ( str.equalsIgnoreCase( "orange" ) )
	    return Color.orange;
	if ( str.equalsIgnoreCase( "pink" ) )
	    return Color.pink;
	if ( str.equalsIgnoreCase( "red" ) )
	    return Color.red;
	if ( str.equalsIgnoreCase( "white" ) )
	    return Color.white;
	if ( str.equalsIgnoreCase( "yellow" ) )
	    return Color.yellow;
	return null;
	}


    /**
     * <p>
     * Handles the standard applet background parameter, BGCOLOR.  
     * <p>
     * Call as:
     * <BLOCKQUOTE>
     * Acme.Utils.handleBgcolor( this );
     * </BLOCKQUOTE>
     * at the start of your init() method.
     */
    public static void handleBgcolor( Applet applet )
	{
	String param = applet.getParameter( "bgcolor" );
	if ( param != null )
	    {
	    Color color = parseColor( param );
	    if ( color != null )
		applet.setBackground( color );
	    }
	}


    /**
     * <p>
     * Test is a number is even.
     */
    public static boolean even( long n )
	{
	return ( n & 1 ) == 0;
	}

    /**
     * <P>
     * Test is a number is odd.
     */
    public static boolean odd( long n )
	{
	return ( n & 1 ) != 0;
	}


    /** <p> Count the number of 1-bits in a byte.  */
    public static int countOnes( byte n )
	{
	return countOnes( n & 0xffL );
	}

    /** <p> Count the number of 1-bits in an int. */
    public static int countOnes( int n )
	{
	return countOnes( n & 0xffffffffL );
	}

    /** <p> Count the number of 1-bits in a long. */
    public static int countOnes( long n )
	{
	// There are faster ways to do this, all the way up to looking
	// up bytes in a 256-element table.  But this is not too bad.
	int count = 0;
	while ( n != 0 )
	    {
	    if ( odd( n ) )
		++count;
	    n >>>= 1;
	    }
	return count;
	}


    /**
     * <p>
     * A fixed version of java.io.InputStream.read(byte[], int, int).  The
     * standard version catches and ignores IOExceptions from below.
     * This version sends them on to the caller.
     */
    public static int fixedRead( InputStream in, byte[] b, int off, int len ) throws IOException
        {
        if ( len <= 0 )
            return 0;
        int c = in.read();
        if ( c == -1 )
            return -1;
        if ( b != null )
            b[off] = (byte) c;
        int i;
        for ( i = 1; i < len ; ++i )
            {
            c = in.read();
            if ( c == -1 )
                break;
            if ( b != null )
                b[off + i] = (byte) c;
            }
        return i;
        }


    private static final int SPLINE_THRESH = 3;

    /** <p> Draw a three-point spline. */
    public static void drawSpline( Graphics graphics, int x1, int y1, int x2, int y2, int x3, int y3 )
	{
	int xa, ya, xb, yb, xc, yc, xp, yp;

	xa = ( x1 + x2 ) / 2;
	ya = ( y1 + y2 ) / 2;
	xc = ( x2 + x3 ) / 2;
	yc = ( y2 + y3 ) / 2;
	xb = ( xa + xc ) / 2;
	yb = ( ya + yc ) / 2;

	xp = ( x1 + xb ) / 2;
	yp = ( y1 + yb ) / 2;
	if ( Math.abs( xa - xp ) + Math.abs( ya - yp ) > SPLINE_THRESH )
	    drawSpline( graphics, x1, y1, xa, ya, xb, yb );
	else
	    graphics.drawLine( x1, y1, xb, yb );

	xp = ( x3 + xb ) / 2;
	yp = ( y3 + yb ) / 2;
	if ( Math.abs( xc - xp ) + Math.abs( yc - yp ) > SPLINE_THRESH )
	    drawSpline( graphics, xb, yb, xc, yc, x3, y3 );
	else
	    graphics.drawLine( xb, yb, x3, y3 );
	}


    private static final int DDA_SCALE = 8192;

    /** <p> Draw a thick line. */
    public static void drawThickLine( Graphics graphics, int x1, int y1, int x2, int y2, int linewidth )
	{
	// Draw the starting point filled.
	graphics.fillOval(
	    x1 - linewidth / 2, y1 - linewidth / 2, linewidth, linewidth );

	// Short-circuit zero-length lines.
	if ( x1 == x2 && y1 == y2 )
	    return;

	/* Draw, using a simple DDA. */
	if ( Math.abs( x2 - x1 ) > Math.abs( y2 - y1 ) )
	    {
	    // Loop over X domain.
	    int dy, srow;
	    int dx, col, row, prevrow;

	    if ( x2 > x1 )
		dx = 1;
	    else
		dx = -1;
	    dy = ( y2 - y1 ) * DDA_SCALE / Math.abs( x2 - x1 );
	    prevrow = row = y1;
	    srow = row * DDA_SCALE + DDA_SCALE / 2;
	    col = x1;
	    for (;;)
		{
		if ( row != prevrow )
		    {
		    graphics.drawOval(
			col - linewidth / 2, prevrow - linewidth / 2,
			linewidth, linewidth );
		    prevrow = row;
		    }
		graphics.drawOval(
		    col - linewidth / 2, row - linewidth / 2,
		    linewidth, linewidth );
		if ( col == x2 )
		    break;
		srow += dy;
		row = srow / DDA_SCALE;
		col += dx;
		}
	    }
	else
	    {
	    // Loop over Y domain.
	    int dx, scol;
	    int dy, col, row, prevcol;

	    if ( y2 > y1 )
		dy = 1;
	    else
		dy = -1;
	    dx = ( x2 - x1 ) * DDA_SCALE / Math.abs( y2 - y1 );
	    row = y1;
	    prevcol = col = x1;
	    scol = col * DDA_SCALE + DDA_SCALE / 2;
	    for ( ; ; )
		{
		if ( col != prevcol )
		    {
		    graphics.drawOval(
			prevcol - linewidth / 2, row - linewidth / 2,
			linewidth, linewidth );
		    prevcol = col;
		    }
		graphics.drawOval(
		    col - linewidth / 2, row - linewidth / 2,
		    linewidth, linewidth );
		if ( row == y2 )
		    break;
		row += dy;
		scol += dx;
		col = scol / DDA_SCALE;
		}
	    }
	}


    /**
     * <p>
     * Make a URL with no ref part and no query string.  Also, if it's
     * a directory then make sure there's a trailing slash.
     * <p>
     * @exception java.net.MalformedURLException If the url is not legal.
     */
    public static URL plainUrl( URL context, String urlStr )
	throws MalformedURLException
	{
	URL url = new URL( context, urlStr );
	String fileStr = url.getFile();
	int i = fileStr.indexOf( '?' );
	if ( i != -1 )
	    fileStr = fileStr.substring( 0, i );
	url = new URL(
	    url.getProtocol(), url.getHost(), url.getPort(), fileStr );
	if ( ( ! fileStr.endsWith( "/" ) ) &&
	     urlStrIsDir( url.toExternalForm() ) )
	    {
	    fileStr = fileStr + "/";
	    url = new URL(
		url.getProtocol(), url.getHost(), url.getPort(), fileStr );
	    }
	return url;
	}

    /**
     * <p> 
     * Make a URL with no ref part and no query string.  Also, if it's
     * a directory then make sure there's a trailing slash.
     * <p>
     * @exception java.net.MalformedURLException If the url is not legal.
     */
    public static URL plainUrl( String urlStr )
	throws MalformedURLException
	{
        return plainUrl( null, urlStr );
        }

    /**
     * <p>
     * Figure out the base URL for a given URL.  What this means is
     * if the URL points to a directory, you get that directory; if the
     * URL points to a file, you get the directory the file is in.
     */
    public static String baseUrlStr( String urlStr )
	{
	if ( urlStr.endsWith( "/" ) )
	    return urlStr;
	if ( urlStrIsDir( urlStr ) )
	    return urlStr + "/";
	return urlStr.substring( 0, urlStr.lastIndexOf( '/' ) + 1 );
	}

    /** <p> Makes sure if a URL is a directory, it ends with a slash. */
    public static String fixDirUrlStr( String urlStr )
	{
	if ( urlStr.endsWith( "/" ) )
	    return urlStr;
	if ( urlStrIsDir( urlStr ) )
	    return urlStr + "/";
	return urlStr;
	}

    /**
     * <p>
     * Figures out whether a URL points to a directory or not.
     * <p>
     * Web servers are lenient and accept directory-URLs without
     * the trailing slash.  What they actually do is return a
     * redirect to the same URL with the trailing slash appended.
     * <p>
     * Unfortunately, Java doesn't let us see that such a redirect
     * happened.  Instead we have to figure out it's a directory
     * indirectly and heuristically.
     */
    public static boolean urlStrIsDir( String urlStr )
	{
	// If it ends with a slash, it's probably a directory.
	if ( urlStr.endsWith( "/" ) )
	    return true;

	// If the last component has a dot, it's probably not a directory.
	int lastSlash = urlStr.lastIndexOf( '/' );
	int lastPeriod = urlStr.lastIndexOf( '.' );
	if ( lastPeriod != -1 && ( lastSlash == -1 || lastPeriod > lastSlash ) )
	    return false;

	// Otherwise, append a slash and try to connect.  This is
	// fairly expensive.
	String urlStrWithSlash = urlStr + "/";
	try
	    {
	    URL url = new URL( urlStrWithSlash );
	    InputStream f = url.openStream();
	    f.close();
	    // Worked fine - it's probably a directory.
	    return true;
	    }
	catch ( Exception e )
	    {
	    // Got an error - must not be a directory.
	    return false;
	    }
	}


    /** <p> Figures out whether a URL is absolute or not. */
    public static boolean urlStrIsAbsolute( String urlStr )
	{
	if ( urlStr.startsWith( "/" ) || urlStr.indexOf( ":/" ) != -1 )
	    return true;
	// Should handle :8000/ and such too.
	return false;
	}

    /** 
      * <p> 
      * Returns an equivalent URL string that is guaranteed to be absolute.
      * <p>
      * @exception java.net.MalformedURLException if url is not legal.
      */
    public static String absoluteUrlStr( String urlStr, URL contextUrl ) throws MalformedURLException
	{
	URL url = new URL( contextUrl, urlStr );
	return url.toExternalForm();
	}


    /** <p> Check if an array contains a given element. */
    public static boolean arraycontains( Object[] array, Object element )
	{
	for ( int i = 0; i < array.length; ++i )
	    if ( array[i].equals( element ) )
		return true;
	return false;
	}


    /**
     * <p>
     * Run a program on the host Unix system.
     * <p>
     * This routine runs the specified command, waits for it to
     * finish, and returns the exit status.
     * <p>
     * This is like the Unix system() routine.  Unlike the Unix version,
     * though, stdout and stderr get thrown away unless you redirect them.
     */

    public static int system( String cmd )
    {
     return system(cmd, true);
    }

    public static int runCmd( String cmd )
    {
      return runCmd(cmd, true);
    }

    public static int runCmd( String cmd, boolean wait)
    {
	try
	    {
	    Runtime runtime = Runtime.getRuntime();
	    Process process = runtime.exec( cmd );
	    int rc = 0;
            if (wait)
            {
             rc = process.waitFor();
            }
            return rc;
	    }
	catch ( IOException e )
	    {
	    return -1;
	    }
	catch ( InterruptedException e )
	    {
	    return -1;
            }
    }

    public static int system( String cmd , boolean wait)
	{
	try
	    {
	    Runtime runtime = Runtime.getRuntime();
	    String[] shCmd = new String[3];
	    shCmd[0] = "/bin/sh";
	    shCmd[1] = "-c";
	    shCmd[2] = cmd;
	    Process process = runtime.exec( shCmd );
	    int rc = 0;
            if (wait)
            {
             rc = process.waitFor();
            }
            return rc;
	    }
	catch ( IOException e )
	    {
	    return -1;
	    }
	catch ( InterruptedException e )
	    {
	    return -1;
	    }
	}

    /**
     * <p>
     * Run a program on the host Unix system, and capture the output.
     * <P>
     * This routine runs the specified command, and returns an InputStream
     * for reading the output of the program.
     */
    public static InputStream popen( String cmd )
	{
	try
	    {
	    Runtime runtime = Runtime.getRuntime();
	    String[] shCmd = new String[3];
	    shCmd[0] = "/bin/sh";
	    shCmd[1] = "-c";
	    shCmd[2] = cmd;
	    Process process = runtime.exec( shCmd );
	    return process.getInputStream();
	    }
	catch ( IOException e )
	    {
	    return null;
	    }
	}


    /** <p> Dump out the current call stack. */
    public static void dumpStack( PrintStream p )
	{
	(new Exception()).printStackTrace( p );
	}

    /** <p> Dump out the current call stack onto System.err. */
    public static void dumpStack()
	{
	(new Exception()).printStackTrace();
	}

    public static String getHostName()
    {
       String host_name = "localhost";
       try
       {
         host_name = InetAddress.getLocalHost().getHostName();
         if (host_name.indexOf(".") > 0)
         {
            host_name = host_name.substring(0,host_name.indexOf("."));
         }
       }
       catch (Exception e)
       {
       }
       return host_name;
    }

    public static boolean isValidJavaClassName(String classname)
    {
	boolean ok = true;
	String valChars = "abcdefghijklmnopqrstuvwxzyABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
	String valNums = "0123456789";
	char charValue = '0';
	if ((classname != null) && (classname.length() > 0))
	{
		for (int i = 0; i < classname.length(); i++)
		{
			charValue = classname.charAt(i);
			if (valChars.indexOf(charValue, 0) == -1)
			{
				//not found so it is not valid
				ok = false;
				break;
			}
		}
		if (ok == true)
		{
			char firstChar = classname.charAt(0);
			String val = String.valueOf(firstChar);
			if (valNums.indexOf(val, 0) > -1)
			{
				// cannot start with a number
				ok = false;
			}
		}
	}
	return ok;
    }

    public static void main(String[] args)
    {
      System.out.println("Locale=" + getLocaleString());
      String sent="";
      for (int i=0;i<args.length;i++)
      {
       sent+=args[i];
       if (i<args.length-1)
       {
        sent+=" "; 
       }
      }
      String out=wordWrap(sent,40);
      System.out.println(out);
      String test="This is a test of a message. This should wrap and" +
                  " also have a paragraph break.\nSee this is paragraph two.";
      System.out.println(wordWrap(test,40));
      String test1="/home/andrea/work/new/datagate/foo, /home/andrea/work/new/datagate/bar, "+
                  "/home/andrea/work/new/datagate/dgSend, /home/andrea/work/new/datagate/dgRecv";
      System.out.println(wordWrap(test1,55));
             
      System.out.println("dirname(/home/andrea/foo.doc): "+
              getDirname("/home/andrea/foo.doc")); 
      System.out.println("basename(/home/andrea/foo.doc): "+
              getBasename("/home/andrea/foo.doc")); 
      System.out.println("basenameNoExt(/home/andrea/foo.doc): "+
              getBasenameNoExt("/home/andrea/foo.doc")); 
      System.out.println("dirname(foo.doc): "+
              getDirname("foo.doc")); 

      System.out.println("center(Andrea,20)=<"+centerString("Andrea",20)+">");
      System.out.println("center(Andrea,21)=<"+centerString("Andrea",21)+">");
      System.out.println("center(Andrea,6)=<"+centerString("Andrea",6)+">");
      System.out.println("center(Andrea,3)=<"+centerString("Andrea",3)+">");
      System.out.println("pad(Andrea,10)=<"+padString("Andrea",10)+">");
      System.out.println("pad(Andrea,3)=<"+padString("Andrea",3)+">");

      System.out.println("wordWrap(/home/datagate/data/file-001.dat,20)="+
                    wordWrap("/home/datagate/data/file-001.dat",20)); 
      System.out.println("wordWrap(data/ScSerialHL7In.seq,20)"+
                    wordWrap("data/ScSerialHL7In.seq",20));                        
      System.out.println(
              "makeAbbreviatedPath(/home/andrea/DataGate/src/Java/my.cfg,30)=\n"+
               makeAbbreviatedPath("/home/andrea/DataGate/src/Java/my.cfg",30));
      System.out.println(
              "makeAbbreviatedPath(/home/gev/DataGate/src/Java/my.cfg,30)=\n"+
               makeAbbreviatedPath("/home/gev/DataGate/src/Java/my.cfg",30));
      System.out.println(
              "makeAbbreviatedPath(/home/gev/DataGate/src/Java/my.cfg,10)=\n"+
               makeAbbreviatedPath("/home/gev/DataGate/src/Java/my.cfg",10));
      System.out.println(
              "makeAbbreviatedPath(/home/gev/DataGate/src/Java/my.cfg,50)=\n"+
               makeAbbreviatedPath("/home/gev/DataGate/src/Java/my.cfg",50));

      System.out.println("dateStringCur:"+makeDateString(new Date())); 
      GregorianCalendar d=getDateFromDateString("19701201100000");
      GregorianCalendar d2=getDateFromDateString("19701202100000");
      Date dtmp = d.getTime();
      Date d2tmp = d2.getTime();
      System.out.println("24hours= "+(24*60*60*1000)+" milliseconds"); 
      System.out.println("d2-d:="+(d2tmp.getTime()-dtmp.getTime()));
   
      System.out.println("fullPath(/home/andrea/full/path.out)="+ 
        getFullPath("/home/bill","/home/andrea/full/path.out"));
      System.out.println("fullPath(relative/path.out)="+ 
        getFullPath("/home/bill/","relative/path.out"));
      System.out.println("fullPath(a:/home/andrea/full/path.out)"+ 
          getFullPath("c:/home/bill","a:/home/andrea/full/path.out"));
      if (System.getProperty("os.name").startsWith("Win"))
      {
        System.out.println("fullPath(a:\\home\\andrea\\full\\path.out)="+ 
          getFullPath("c:/home/bill","a:\\home\\andrea\\full\\path.out"));
        System.out.println("fullPath(Z:/home/andrea/full/path.out)="+ 
          getFullPath("c:/home/bill","Z:/home/andrea/full/path.out"));
        System.out.println("fullPath(a:relative/path.out)="+ 
          getFullPath("c:/home/bill","a:relative/path.out"));
        System.out.println("fullPath(relative/path.out)="+ 
          getFullPath("c:/home/bill","relative/path.out"));
        System.out.println("fullPath(Z:relative/path.out)="+ 
          getFullPath("Z:/home/bill","relative/path.out"));
        System.out.println("fullPath(^:relative/path.out)="+ 
          getFullPath("/home/bill","^:relative/path.out"));
        System.out.println("fullPath(foo:relative/path.out)="+ 
          getFullPath("\\home\\bill","foo:relative\\path.out"));
      }

      System.out.println("isZero(-0)="+isZero("-0"));
      System.out.println("isZero(0)="+isZero("0"));
      System.out.println("isZero(00000)="+isZero("000000"));
      System.out.println("isZero(-00000)="+isZero("-000000"));
      System.out.println("isZero(-00003)="+isZero("-000003"));
      System.out.println("isZero(5)="+isZero("5"));

 
      int zero=1;
      try {
         zero=Integer.parseInt("-0");
         System.out.println("-0="+zero);
      } catch (Exception e){}
   }
 }

