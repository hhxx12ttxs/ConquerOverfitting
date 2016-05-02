/**

    Copyright (c) 1999-2001 by Robert J Colquhoun, All Rights Reserved


    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

*/

package org.maverickdbms.basic.string;


import java.math.BigDecimal;

import org.maverickdbms.basic.mvArray;
import org.maverickdbms.basic.mvConstants;
import org.maverickdbms.basic.mvConstantString;
import org.maverickdbms.basic.mvException;
import org.maverickdbms.basic.Factory;
import org.maverickdbms.basic.File;
import org.maverickdbms.basic.List;
import org.maverickdbms.basic.OSFile;
import org.maverickdbms.basic.Program;
import org.maverickdbms.basic.SequentialFile;
import org.maverickdbms.basic.mvString;

public class BasicString extends mvString {      

    private final static int DEFAULT_ARRAY_SIZE = 10;
        
    private final static char[] EMPTY_STRING = new char[0];
    private final static long _ZERO = doubleToLongBits(0.0D);
    private static boolean hasRawLongBitsFlag = true;

    private final static int TYPE_BASIC_STRING = 0;
    private final static int TYPE_INDEX_AM_STRING = 1;
    private final static int TYPE_INDEX_VM_STRING = 2;
    private final static int TYPE_INDEX_SM_STRING = 3;
    private final static int TYPE_JAVA_DOUBLE = 4;
    private final static int TYPE_JAVA_LONG = 5;
    private final static int TYPE_JAVA_STRING = 6;
    private final static int TYPE_MV_ARRAY = 7;
    private final static int TYPE_MV_FILE = 8;
    private final static int TYPE_MV_LIST = 9;
    private final static int TYPE_MV_OSFILE = 10;
    private final static int TYPE_MV_PROGRAM = 11;
    private final static int TYPE_MV_SEQFILE = 12;

    private final static char[] DELIMITERS = { 0, mvConstants.AM, mvConstants.VM, mvConstants.SM, 0, 0 };

    private Object data = EMPTY_STRING;
    private int type;
    private long value;     
    
    public BasicString(Factory f) { super(f); }

    public mvString append(char c) {
        switch (type) {
            case TYPE_BASIC_STRING:
                char[] buffer = (char[])data;
                if (value >= ((char[])data).length) {
                    buffer = resizeBuffer((int)value + 1);
                }        
                buffer[(int)value++] = c;    
                break;
            case TYPE_JAVA_DOUBLE:
                String s1 = doubleToString();                        
                value = s1.length();
                data = new char[2 * (int)value];
                type = TYPE_BASIC_STRING;
                s1.getChars(0, (int)value, ((char[])data), 0);            
                ((char[])data)[(int)value++] = c;    
                break;
            case TYPE_JAVA_LONG:
                String s2 = Long.toString(value, 10);
                value = s2.length();
                data = new char[2 * (int)value];
                type = TYPE_BASIC_STRING;
                s2.getChars(0, (int)value, ((char[])data), 0);            
                ((char[])data)[(int)value++] = c;    
                break;
            case TYPE_JAVA_STRING:
                String s3 = (String)data;                        
                value = s3.length();
                data = new char[2 * (int)value];
                type = TYPE_BASIC_STRING;
                s3.getChars(0, (int)value, ((char[])data), 0);            
                ((char[])data)[(int)value++] = c;    
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                BasicString[] array = (BasicString[])data;
                if (value == 0) {
                    array[(int)value++] = (BasicString)factory.getString();
                }
                if (c == DELIMITERS[type]) { 
                    array = resizeArray((int)value + 1);
                    array[(int)value++] = (BasicString)factory.getString();
                } else {
                    array[(int)value - 1].append(c);
                }    
                break;
        }
        return this;
    } 

    public mvString append(String s) {        
        int len = s.length();
        switch (type) {
            case TYPE_BASIC_STRING:
                char[] buffer = (char[])data;
                if (len > buffer.length - value) {
                    buffer = resizeBuffer(len + (int)value);
                }
                s.getChars(0, len, buffer, (int)value);
                value += len;    
                break;
            case TYPE_JAVA_DOUBLE:
                String s1 = doubleToString();                        
                value = s1.length();
                data = new char[(int)value + len];
                s1.getChars(0, (int)value , ((char[])data), 0);            
                s.getChars(0, len, (char[])data, (int)value);        
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_LONG:
                String s2 = Long.toString(value, 10);
                value = s2.length();
                data = new char[(int)value + len];
                s2.getChars(0, (int)value , ((char[])data), 0);            
                s.getChars(0, len, (char[])data, (int)value);        
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_STRING:
                String s3 = (String)data;                        
                value = s3.length();
                data = new char[(int)value + len];
                s3.getChars(0, (int)value, ((char[])data), 0);            
                s.getChars(0, len, (char[])data, (int)value);        
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                for (int i = 0; i < len; i++) {
                    append(s.charAt(i));
                }     
                break;
        }
        return this;
    } 

    public mvString append(mvConstantString mvs) {
        int len = mvs.length();
        switch (type) {
            case TYPE_BASIC_STRING:
                char[] buffer = (char[])data;
                if (len > buffer.length - (int)value) {
                    buffer = resizeBuffer(len + (int)value);
                }        
                mvs.getChars(0, len, buffer, (int)value);
                value += len;    
                break;
            case TYPE_JAVA_DOUBLE:
                String s1 = doubleToString();                        
                value = s1.length();
                data = new char[(int)value + len];
                s1.getChars(0, (int)value, ((char[])data), 0);            
                mvs.getChars(0, len, (char[])data, (int)value);        
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_LONG:
                String s2 = Long.toString(value, 10);
                value = s2.length();
                data = new char[(int)value + len];
                s2.getChars(0, (int)value, ((char[])data), 0);            
                mvs.getChars(0, len, (char[])data, (int)value);        
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_STRING:
                String s3 = (String)data;                        
                value = s3.length();
                data = new char[(int)value + len];
                s3.getChars(0, (int)value, ((char[])data), 0);            
                mvs.getChars(0, len, (char[])data, (int)value);        
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                for (int i = 0; i < len; i++) {
                    append(mvs.charAt(i));
                }     
                break;
        }
        return this;
    } 

    public mvString append(mvConstantString mvs, int start, int len) {
        switch (type) {
            case TYPE_BASIC_STRING:
                char[] buffer = (char[])data;
                if (len > buffer.length - value) {
                    buffer = resizeBuffer(len + (int)value);
                }
                mvs.getChars(start, start + len, buffer, (int)value);
                value += len;    
                break;
            case TYPE_JAVA_DOUBLE:
                String s1 = doubleToString();                        
                value = s1.length();
                data = new char[(int)value + len];
                s1.getChars(0, (int)value, ((char[])data), 0);            
                mvs.getChars(start, start + len, (char[])data, (int)value); 
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_LONG:
                String s2 = Long.toString(value, 10);
                value = s2.length();
                data = new char[(int)value + len];
                s2.getChars(0, (int)value, ((char[])data), 0);            
                mvs.getChars(start, start + len, (char[])data, (int)value); 
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_STRING:
                String s3 = (String)data;                        
                value = s3.length();
                data = new char[(int)value + len];
                s3.getChars(0, (int)value, ((char[])data), 0);            
                mvs.getChars(start, start + len, (char[])data, (int)value);
                value += len;    
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                for (int i = start; i < start + len; i++) {
                    append(mvs.charAt(i));
                }     
                break;
        }
        return this;
    }
    
    public mvConstantString ASSIGNED() {
        if (data == EMPTY_STRING && type == TYPE_BASIC_STRING) {
            return ZERO;
        }
        return ONE;
    }

    /**
    * Returns the character at the specified index.
    * @return the character.
    */
    public char charAt(int index) {
        switch (type) {
            case TYPE_BASIC_STRING:
                return ((char[])data)[index];
            case TYPE_JAVA_DOUBLE:
                //assume if they want a character index they are treating as string
                char[] arr = doubleToString().toCharArray();                
                data = arr;
                value = arr.length;
                type = TYPE_BASIC_STRING;
                return arr[index];
            case TYPE_JAVA_LONG:
                //assume if they want a character index they are treating as string
                char[] arr2 = Long.toString(value, 10).toCharArray();
                data = arr2;
                value = arr2.length;
                type = TYPE_BASIC_STRING;
                return arr2[index];
            case TYPE_JAVA_STRING:
                return ((String)data).charAt(index);
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                BasicString[] array = (BasicString[])data;
                for (int i = 0; i < value; i++) {
                    int len = array[i].length();
                    if (index < len) {
                        return array[i].charAt(index);
                    }
                    index -= len;
                    if (index-- == 0 && i < value - 1) return DELIMITERS[type];
                }
                throw new ArrayIndexOutOfBoundsException();        
        }                
        //shouldn't happen...
        throw new ArrayIndexOutOfBoundsException();        
    }
          
    public void clear() {
        switch (type) {
        case TYPE_INDEX_AM_STRING:
        case TYPE_INDEX_VM_STRING:
        case TYPE_INDEX_SM_STRING:
            BasicString[] array = (BasicString[])data;
            for (int i = 0; i < value; i++) {            
                factory.putString(array[i]);
                array[i] = null;
            }
            break;
        }
        value = 0;
        data = EMPTY_STRING;
        type = TYPE_BASIC_STRING;
    }
            
    public int compareTo(mvConstantString a) {
        switch (type) {
            case TYPE_BASIC_STRING:
                int alen = a.length();
                int min = (value < alen) ? (int)value : alen;
                char[] buffer = (char[])data;
                for (int i = 0; i < min; i++) {
                    int diff = buffer[i] - a.charAt(i);
                    if (diff != 0) return diff;
                }
                return (int)(value - alen);
            default:
                return toString().compareTo(a.toString());
        }
    }

    private boolean containsDelimiter() {
        switch (type) {
            case TYPE_BASIC_STRING:
                if (value >= 0) {
                    char[] buffer = (char[])data;
                    for (int i = 0; i < value; i++) {                
                        if (buffer[i] >= mvConstants.TM && buffer[i] <= mvConstants.RM) return true;
                    }
                }       
                return false;   
            case TYPE_JAVA_DOUBLE:
            case TYPE_JAVA_LONG:                   
               return false;
            case TYPE_JAVA_STRING:
                if (value >= 0) {
                    String s = (String)data;
                    for (int i = 0; i < s.length(); i++) {
                        char c = s.charAt(i);
                        if (c >= mvConstants.TM && c <= mvConstants.RM) return true;
                    }
                }       
                return false;                
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                if (value > 1) return true;
                BasicString[] array = (BasicString[])data;
                if (value == 1) return array[0].containsDelimiter();
                return false;
        }                                 
        return false; //should never be reached
    }
   
    private void convertToMultivalue(int newtype) {
        switch (type) {
        case TYPE_BASIC_STRING:
            {
                char[] buff= (char[])data;
                int len = (int)value;
                value = 0;
                BasicString[] array = resizeArray(DEFAULT_ARRAY_SIZE);
                int start = 0;
                for (int i = 0; i < len; i++) {
                    if (buff[i] == DELIMITERS[newtype]) {
                        array[(int)value] = (BasicString)factory.getString();
                        array[(int)value++].set(buff, start, i - start);
                        if (value >= array.length) {
                            array = resizeArray((int)value + 1);
                        }
                        start = i + 1;
                    }
                }
                if (start < len) {
                    array[(int)value] = (BasicString)factory.getString();
                    array[(int)value++].set(buff, start, len - start);
                }
            }
            break;
        case TYPE_JAVA_DOUBLE:
            {
                BasicString[] arr = new BasicString[1];
                arr[0] = (BasicString)factory.getString();
                arr[0].set(Double.longBitsToDouble(value));
                data = arr;
                value = 1;
            }
            break;
        case TYPE_JAVA_LONG:                   
            {
                BasicString[] arr = new BasicString[1];
                arr[0] = (BasicString)factory.getString();
                arr[0].set(value);
                data = arr;
                value = 1;
            }
            break;
        case TYPE_JAVA_STRING:
            {
                String s = (String)data;
                int len = s.length();
                value = 0;
                BasicString[] array = resizeArray(DEFAULT_ARRAY_SIZE);
                int start = 0;
                for (int i = 0; i < len; i++) {
                    if (s.charAt(i) == DELIMITERS[newtype]) {
                        array[(int)value] = (BasicString)factory.getString();
                        array[(int)value++].set(s.substring(start, i));
                        if (value >= array.length) {
                            array = resizeArray((int)value + 1);
                        }
                        start = i + 1;
                    }
                }
                if (start < len) {
                    array[(int)value] = (BasicString)factory.getString();
                    array[(int)value++].set((start > 0) ? s.substring(start) : s);
                }
            }
            break;
        }
        type = newtype;
    }

    public mvString DCOUNT(mvString result, mvConstantString delim) { 
        char c = delim.charAt(0);
        switch (type) {
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING: 
            case TYPE_INDEX_SM_STRING:
                if (c == DELIMITERS[type])  {
                    result.set(value);
                } else {
                    BasicString[] array = (BasicString[])data;
                    int len = (value > 0) ? 1 : 0;
                    for (int i = 0; i < value; i++) {
                        array[i].DCOUNT(result, delim);
                        int tmp = result.intValue();
                        if (tmp > 1) {
                            len += tmp - 1;
                        }				
                    }
                    result.set(len);
                }
                return result;
            default:
                return super.DCOUNT(result, delim);
        }
    }

    /**
    * Deletes an attribute, value or subvalue from a mvString, the corresponding
    * delimiter is also removed
    * @param attrib attribute to delete, if value is zero the entire attribute is deleted
    * @param val value to delete, if subval is zero the entire value is deleted
    * @param subval subvalue to delete.
    * @return the mvString
    */        
    public mvString DELETE(mvConstantString attrib, mvConstantString val, mvConstantString subval) {        
        if (type != TYPE_INDEX_AM_STRING) {
            convertToMultivalue(TYPE_INDEX_AM_STRING);
        }
        int a = attrib.intValue();
        int v = val.intValue();        
        BasicString item = this;
        int index = (a > 0) ? a - 1 : 0;            
        if (v > 0) {
            item = ((BasicString[])item.data)[index];
            if (item.type != TYPE_INDEX_VM_STRING) {
                item.convertToMultivalue(TYPE_INDEX_VM_STRING);
            }
            index = v - 1;     
            int s = subval.intValue();
            if (s > 0) {
                item = ((BasicString[])item.data)[index];
                if (item.type != TYPE_INDEX_SM_STRING) {
                    item.convertToMultivalue(TYPE_INDEX_SM_STRING);
                }
                index = s - 1;                
            }        
        }
        //factory.putString(item.array[index]);
        if (item.value - index - 1 > 0) {
			//if < 50 % used create smaller array
			BasicString[] old = (BasicString[])item.data;
            if ((int)item.value < (old.length >> 1)) {
                item.data = new BasicString[(int)item.value];
                System.arraycopy(old, 0, item.data, 0, index);
                System.arraycopy(old, index + 1, item.data, index, (int)item.value - index - 1);
            } else {
                System.arraycopy(item.data, index + 1, item.data, index, (int)item.value - index - 1);
			}
        }
        item.value--;
        return this;
    }
    
    private String doubleToString() {
        return factory.getNumberFormatter().format(Double.longBitsToDouble(value));
    }

    /**
    * Determines whether the object is equal to the string
    * @param o the object to be compared
    */   
    public boolean equals(Object o) {
        switch (type) {
            case TYPE_BASIC_STRING:
                if (o instanceof mvConstantString && value == ((mvConstantString)o).length()) {
                    mvConstantString mvcs = (mvConstantString)o;
                    char[] buffer = ((char[])data);
                    for (int i = 0; i < value; i++) {
                        if (buffer[i] != mvcs.charAt(i)) return false;
                    }
                    return true;
                }
                return false;
            case TYPE_JAVA_DOUBLE:
                if (o instanceof mvConstantString && ((mvConstantString)o).isNumeric()) {
                    double d = ((mvConstantString)o).getDouble();
                    return (value == doubleToLongBits(d));
                }
                return false;
            case TYPE_JAVA_LONG:
                if (o instanceof mvConstantString && ((mvConstantString)o).isIntegral()) {
                    return (value == ((mvConstantString)o).longValue());
                }
                return false;
            case TYPE_JAVA_STRING:
                return ((String)data).equals(o.toString());
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                if (o instanceof mvConstantString) {
                    BasicString[] arr = (BasicString[])data;
                    mvConstantString o2 = (mvConstantString)o;
                    if (o2 instanceof BasicString) {
                        BasicString o3 = (BasicString)o2;
                        if (type == o3.type) {
                            if (value != o3.value) {
                                return false;
                            }
                            BasicString[] arr2 = (BasicString[])o3.data;
                            for (int i = 0; i < value; i++ ) {
                                if (!arr[i].equals(arr2[i])) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    } 
                    if (value == 0) {
                        return (o2.length() == 0);
                    } else if (value == 1) {
                        return arr[0].equals(o);
                    } else if (value > 1) {
                        int olen = o2.length();
                        int index = 0;
                        int alen = arr[0].length();
                        char delim = DELIMITERS[type];
                        for (int j = 0; j < alen; j++) {
                            if (index >= olen
                                    || arr[0].charAt(j) != o2.charAt(index++)) {
                                return false;
                            }
                        }
                        for (int i = 1; i < value; i++) {
                            if (index >= olen || delim != o2.charAt(index++)) {
                                return false;
                            }
                            alen = arr[i].length();
                            for (int j = 0; j < alen; j++) {
                                if (index >= olen
                                        || arr[i].charAt(j) != o2.charAt(index++)) {
                                    return false;
                                }
                            }
                        }
                        return (index == olen);
                    }
                }
        }
        //should not be reached
        return false;
    }
  
    public mvConstantString EXTRACT(mvConstantString attrib, mvConstantString val, mvConstantString subval) {        
        if (type != TYPE_INDEX_AM_STRING) {
            convertToMultivalue(TYPE_INDEX_AM_STRING);
        }
        int a = attrib.intValue();
        if (value < a) {
            return EMPTY;
        }
        int v = val.intValue();
        int index = (a > 0) ? a - 1 : 0;
        BasicString result = ((BasicString[])data)[index];        
        if (v > 0 && result != null) {
            if (result.type != TYPE_INDEX_VM_STRING) {
                result.convertToMultivalue(TYPE_INDEX_VM_STRING);
            }
            if (result.value < v) {
                return EMPTY;
            }
            result = ((BasicString[])result.data)[v - 1];
            int s = subval.intValue();
            if (s > 0 && result != null) {
                if (result.type != TYPE_INDEX_SM_STRING) {
                    result.convertToMultivalue(TYPE_INDEX_SM_STRING);
                }
                if (result.value < s) {
                    return EMPTY;
                }
                result = ((BasicString[])result.data)[s - 1];
            }        
        }
        return (result != null) ? result : EMPTY;
    }
    
    public int findLength(int start, int len, char delim) {
        //System.err.println("findLength '" + toString() + "' start = " + start + " len = " + len + " delim = " + delim);
        switch (type) {
            case TYPE_JAVA_DOUBLE:
                data = doubleToString().toCharArray();
                value = ((char[])data).length;
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_LONG:
                //XXX could do thiswithout Long.toString
                data = Long.toString(value, 10).toCharArray();
                value = ((char[])data).length;
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_STRING:
                data = ((String)data).toCharArray();
                value = ((char[])data).length;
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                {
                    BasicString[] array = (BasicString[])data;
                    //find start
                    int index = 0;
                    while (index < value) {
                        int len2 = array[index].length();
                        if (len2 > start) {
                            if (delim == DELIMITERS[type]) {
                                return len2 - start + 1;
                            } else {
                                int val = array[index++].findLength(start, len2, delim);
                                int len3 = val;
                                while (index < value
                                        && len3 + start < len
                                        && val == len2 - start) {
                                    start = 0;
                                    val = array[index++].findLength(start, len2, delim);
                                    len3 += val + 1;
                                }
                                return len3;
                            }
                        }
                        start -= len2 + 1;
                        index++;
                    }
                    return 0;
                }
        }
        char[] buffer = (char[])data;
        int end = start;
        while (end < len && buffer[end++] != delim);
        return end - start;
    }

    private int findposition(int index) {
        int val = 0;
        BasicString[] array = (BasicString[])data;
        if (index > value) index = (int)value;
        for (int i = 0; i < index; i++) {
            val += array[i].length();
        }
        if (index > 0) val += index - 1; //delimeters
        return val;
    }

    public int findPosition(int a, int v, int s) {
        if (type != TYPE_INDEX_AM_STRING) {
            convertToMultivalue(TYPE_INDEX_AM_STRING);
        }
        BasicString[] array = (BasicString[])data;
        int index = findposition(a - 1);
        if (a <= value && v > 0) {
            if (array[a - 1].type != TYPE_INDEX_VM_STRING) {
                array[a - 1].convertToMultivalue(TYPE_INDEX_VM_STRING);
            }
            index += array[a - 1].findposition(v - 1);
            BasicString[] array2 = (BasicString[])array[a - 1].data;
            if (v <= array[a - 1].value && s > 0) {
                if (array2[v - 1].type != TYPE_INDEX_SM_STRING) {
                    array2[v - 1].convertToMultivalue(TYPE_INDEX_SM_STRING);
                }
                index += array2[v - 1].findposition(s - 1);
            }
        }
        return index;
    }
    
    public int indexOf(char c, int start) {
        switch (type) {
            case TYPE_BASIC_STRING:
                char[] buffer = ((char[])data);    
                for (int i = start; i < value; i++) {
                    if (buffer[i] == c) {
                        return i;
                    }
                }
                break;
            case TYPE_JAVA_DOUBLE:
            case TYPE_JAVA_LONG:
                return toString().indexOf(c, start);
            case TYPE_JAVA_STRING:
                return ((String)data).indexOf(c, start);
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                int pos = 0;
                BasicString[] array = (BasicString[])data;
                if (c == DELIMITERS[type]) {
                    //special case
                    for (int i = 0; i < value; i++) {
                        int len = array[i].length();
                        if (pos + len < start) {
                            pos += len + 1;
                        } else {
                            return pos + len;
                        }
                    }
                    
                }
                for (int i = 0; i < value; i++) {
                    int len = array[i].length();
                    if (pos + len < start) {
                        pos += len + 1;
                    } else {
                        int val = array[i].indexOf(c, start - pos);
                        if (val > 0) return val + pos;
                        else pos += len + 1;
                        start = pos;
                    }
                }
                break;
        }        
        return -1;
    }

    private int insert(int index, BasicString insert) {
        BasicString[] array = (BasicString[])data;
        index = (index > 0) ? index - 1 : (int)value;
        if (index >= value) {
            if (index >= array.length) array = resizeArray(index + 1);
            while (index > value) {
                array[(int)value++] = (BasicString)factory.getString();
            }
        } else {
            if (value >= array.length) array = resizeArray((int)value + 1);
            System.arraycopy(array, index, array, index + 1, (int)value - index);
        }
        array[index] = insert;
        value++;
        return index;
    }

    public mvString INSERT(mvConstantString attrib, mvConstantString val, mvConstantString subval, mvConstantString insert) {
        if (type != TYPE_INDEX_AM_STRING) {
            convertToMultivalue(TYPE_INDEX_AM_STRING);
        }
        int a = attrib.intValue();
        int v = val.intValue();            

        BasicString[] array = (BasicString[])data;
        BasicString ins = (BasicString)factory.getString();
        ins.set(insert);

        if (v != 0) {
            if (a < 0 || a > value) {
                a = insert(a, (BasicString)factory.getString());
            } else {
                a--;
            }
            if (array[a].type != TYPE_INDEX_VM_STRING) {
                array[a].convertToMultivalue(TYPE_INDEX_VM_STRING);
            }
            int s = subval.intValue();
            if (s != 0) {
                BasicString[] array2 = (BasicString[])array[a].data;
                if (v < 0 || v > array[a].value) {
                    v = array[a].insert(v, (BasicString)factory.getString());
                } else {
                    v--;
                }
                if (array2[v].type != TYPE_INDEX_SM_STRING) {
                    array2[v].convertToMultivalue(TYPE_INDEX_SM_STRING);
                }
                array2[v].insert(s, ins);
            } else {
                array[a].insert(v, ins);
            }
        } else {
            //a != 0
            insert(a, ins);
        }
        //XXX insert containsDelimiters????
        return this;
    }

    public mvArray getArray() {
        if (type != TYPE_MV_ARRAY) {
            data = factory.getArray();
            type = TYPE_MV_ARRAY;
        }
        return (mvArray)data;
    }


    public BigDecimal getBigDecimal() {
        switch (type) {
        case TYPE_BASIC_STRING:
            return new BigDecimal(new String((char[])data, 0 ,(int)value));
        case TYPE_JAVA_DOUBLE:
            return new BigDecimal(Double.longBitsToDouble(value));
        case TYPE_JAVA_LONG:
            return new BigDecimal(value);
        case TYPE_JAVA_STRING:
            return new BigDecimal((String)data);
        case TYPE_INDEX_AM_STRING:
        case TYPE_INDEX_VM_STRING:
        case TYPE_INDEX_SM_STRING:
            return new BigDecimal(toString());
        }
        //should not reach here
        return null;
    }
    
    /**
    * Copies the specified index from the string into the destination character array.
    * @param srcstart starting postion in the source string.
    * @param srcend ending index + 1 of characters to be copied from source.
    * @param dest the destination character array
    * @param deststart starting position in the destination array.
    */    
    public void getChars(int srcstart, int srcend, char[] dest, int deststart) {
        switch (type) {
            case TYPE_BASIC_STRING:                        
                System.arraycopy(data, srcstart, dest, deststart, srcend - srcstart);
                break;
            case TYPE_JAVA_DOUBLE:                        
                doubleToString().getChars(srcstart, srcend, dest, deststart);
                break;
            case TYPE_JAVA_LONG:                        
                {
                    long val = value;
                    int index = 1;
                    while ((val /= 10) != 0) { index++; }
                    if (value < 0) {
                        if (srcstart == 0 && srcend > srcstart) {
                            dest[deststart++] = '-';
                            srcend--;
                        }
                        val = -value;
                    } else {
                        val = value;
                    }
                    //work right to left
                    while (index > srcend) {
                        val /= 10;
                        index--;
                    }
                    while (index > srcstart) {
                        dest[deststart + index - srcstart - 1] = (char)('0' + (val % 10));
                        val /= 10;
                        index--;
                    }
                }
                break;
            case TYPE_JAVA_STRING:
                ((String)data).getChars(srcstart, srcend, dest, deststart);                
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                BasicString[] array = (BasicString[])data;
                int index = 0;
                int pos1 = 0;
                int pos2 = (value > 0) ? array[index].length() : 0;
                while (pos2 < srcstart) {
                    pos1 = pos2;
                    pos2 += array[++index].length() + 1;
                }
                while (pos2 < srcend) {
                    if (srcstart > pos1) {
                        if (index > 0) pos1++;
                        array[index].getChars(srcstart - pos1, pos2 - pos1, dest, deststart);
                        deststart += pos2 - srcstart;
                    } else {
                        if (index  > 0) {
                            dest[deststart++] = DELIMITERS[type];
                            pos1++;
                        }
                        array[index].getChars(0, pos2 - pos1, dest, deststart);
                        deststart += pos2 - pos1;
                    }
                    pos1 = pos2;
                    pos2 += array[++index].length() + 1;
                }

                if (srcstart > pos1) {
                    if (index > 0) pos1++;
                    array[index].getChars(srcstart - pos1, srcend - pos1, dest, deststart);
                } else {
                    if (index  > 0) {
                        dest[deststart++] = DELIMITERS[type]; 
                        pos1++;
                    }
                    if (srcend > pos1) { //make sure we have something to copy
                        array[index].getChars(0, srcend - pos1, dest, deststart);
                    }
                }
                break;
        }        
    }
    
    public double getDouble() {
        switch (type) {
            case TYPE_JAVA_DOUBLE:
                return Double.longBitsToDouble(value);
            case TYPE_JAVA_LONG:
                return (double)value;
            default:
                //assume if it is wanted as a number it will be wanted
                //again so cache it
                String s = toString();
                if (s.length() > 0) {
                    try {
                        value = doubleToLongBits(Double.valueOf(s).doubleValue());
                    } catch (NumberFormatException nfe) {
                        //System.err.println("string is non-numeric, zero assumed!");
                        value =  _ZERO;
                    }
                } else {
                    value = _ZERO;
                }
                type = TYPE_JAVA_DOUBLE;
                return Double.longBitsToDouble(value);
        }
    }                  
           
    public Program getProgram() throws mvException {
        switch (type) {
            case TYPE_MV_PROGRAM:
                return (Program)data;
            default:
                if (length() > 0) {
                    data = factory.getProgram(this);
                    type = TYPE_MV_PROGRAM;
                    return (Program)data;
                } else {
                    //XXX runtime error!
                    throw new mvException(0, "Cannot resolve program!");
                }
        }
    }

    public File getFile() {
        if (type != TYPE_MV_FILE) {
            //XXX runtime error!
            return null;
        }
        return (File)data;
    }

    public List getList() {
        if (type != TYPE_MV_LIST) {
            //XXX runtime error!
            return null;
        }
        return (List)data;
    }

    public OSFile getOSFile() {
        if (type != TYPE_MV_OSFILE) {
            data = factory.getOSFile();
            type = TYPE_MV_OSFILE;
        }
        return (OSFile)data;
    }

    public SequentialFile getSequentialFile() {
        if (type != TYPE_MV_SEQFILE) {
            data = factory.getSequentialFile();
            type = TYPE_MV_SEQFILE;
        }
        return (SequentialFile)data;
    }

    private static long doubleToLongBits(double d) {
        //works out whether doubleToRawBits is available
        try {
            return (hasRawLongBitsFlag) ? Double.doubleToRawLongBits(d) : Double.doubleToLongBits(d);
        } catch (NoSuchMethodError nsme) {
            hasRawLongBitsFlag = false;
            return Double.doubleToLongBits(d);
        }
    }

    public int intValue() {
        switch (type) {
        case TYPE_BASIC_STRING:
				//note for 0 length strings will return 0
                char[] buffer = (char[])data;
				int len = (int)value;
				type = TYPE_JAVA_LONG;
				value = 0;
				if (buffer[0] == '-') {
                    for (int i = 1; i < len; i++) {
						value *= 10;
						value -= Character.digit(buffer[i], 10);
                    }
				} else {
                    for (int i = 0; i < len; i++) {
                        if (!Character.isDigit(buffer[i])) break;
						value *= 10;
						value += Character.digit(buffer[i], 10);
                    }
				}
                return (int)value;
            case TYPE_JAVA_DOUBLE:
				double d = Double.longBitsToDouble(value);
				if (d == (double)((long)d)) {
					type = TYPE_JAVA_LONG;
					value = (long)d;
					return (int)value;
				}
			    return (int)d;
            case TYPE_JAVA_LONG:
                return (int)value;
            case TYPE_JAVA_STRING:
				//note for 0 length strings will return 0
                char[] buffer2 = ((String)data).toCharArray();
				type = TYPE_JAVA_LONG;
				value = 0;
				if (buffer2[0] == '-') {
                    for (int i = 1; i < buffer2.length; i++) {
						value *= 10;
						value -= Character.digit(buffer2[i], 10);
                    }
				} else {
                    for (int i = 0; i < buffer2.length; i++) {
						value *= 10;
						value += Character.digit(buffer2[i], 10);
                    }
				}
                return (int)value;
        case TYPE_INDEX_AM_STRING:
        case TYPE_INDEX_VM_STRING:
        case TYPE_INDEX_SM_STRING:
			//XXX should throw exception if value > 1
			if (value == 1) {
                value = ((BasicString[])data)[0].longValue();
			} else {
			    value = 0;
			}
            type = TYPE_JAVA_LONG;
            return (int)value;
        }
        //should not reach here
        return 0;
    }    
    
    public boolean isIntegral() {
        switch(type) {
            case TYPE_BASIC_STRING:
				if (value == 0) return false;
                char[] buffer = (char[])data;
				if (buffer[0] == '-') {
				    long val = 0;
                    for (int i = 1; i < value; i++) {
						char c = buffer[i];
                        if (!Character.isDigit(c)) {
                            return false;
                        }
						val *= 10;
						val -= Character.digit(c, 10);
                    }
				    type = TYPE_JAVA_LONG;
			        value = val;
				} else {
				    long val = 0;
                    for (int i = 0; i < value; i++) {
						char c = buffer[i];
                        if (!Character.isDigit(c)) {
                            return false;
                        }
						val *= 10;
						val += Character.digit(c, 10);
                    }
				    type = TYPE_JAVA_LONG;
			        value = val;
				}
                return true;
            case TYPE_JAVA_DOUBLE:
				double d = Double.longBitsToDouble(value);
				if (d == (double)((long)d)) {
					type = TYPE_JAVA_LONG;
					value = (long)d;
					return true;
				}
                return false;
            case TYPE_JAVA_LONG:
                return true;
            case TYPE_JAVA_STRING:
			{
                String s = (String)data;
				long val = 0;
				if (s.length() == 0) return false;
				if (s.charAt(0) == '-') {
                    for (int i = 1; i < s.length(); i++) {
                        char c = s.charAt(i);
                        if (!Character.isDigit(c)) {
                            return false;
                        }
						val *= 10;
						val -= Character.digit(c, 10);
                    }        
				} else {
                    for (int i = 0; i < s.length(); i++) {
                        char c = s.charAt(i);
                        if (!Character.isDigit(c)) {
                            return false;
                        }
						val *= 10;
						val += Character.digit(c, 10);
                    }        
				}
				type = TYPE_JAVA_LONG;
				value = val;
                return true;
				}
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                return (value == 1 && ((BasicString[])data)[0].isIntegral());
        }
        return false;        
    }

    public boolean isNumeric() {
        switch(type) {
            case TYPE_BASIC_STRING:
                char[] buffer = (char[])data;
                int index = (value > 0 && buffer[0] == '-') ? 1 : 0;
                boolean hasDecimal = false;
                while (index < value) {
                    char c = buffer[index++];
                    if (!hasDecimal && c == '.') {
                        hasDecimal = true;
                    } else if (!Character.isDigit(c)) {
                        return false;
                    }
                }
                return true;
            case TYPE_JAVA_DOUBLE:
            case TYPE_JAVA_LONG:
                return true;
            case TYPE_JAVA_STRING:
                String s = (String)data;
                int index2 = (value > 0 && s.charAt(0) == '-') ? 1 : 0;
                boolean hasDecimal2 = false;
                while (index2 < s.length()) {
                    char c = s.charAt(index2++);
                    if (!hasDecimal2 && c == '.') {
                        hasDecimal2 = true;
                    } else if (!Character.isDigit(c)) {
                        return false;
                    }
                }
                return true;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                return (value == 1 && ((BasicString[])data)[0].isNumeric());
        }
        return false;        
    }

    /**
    * Returns the length of the string
    * @return the length    
    */     
    public int length() {
        switch (type) {
            case TYPE_BASIC_STRING:
                return (int)value;
            case TYPE_JAVA_DOUBLE:
                return doubleToString().length();
            case TYPE_JAVA_LONG:
				int len0 = (value < 0) ? 2 : 1;
				long val = value / 10;
				while (val != 0) {
	                val /= 10;
                    len0++;
                }
                return len0;
            case TYPE_JAVA_STRING:
                return ((String)data).length();
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                int len = 0;
                BasicString[] array = (BasicString[])data;
                for (int i = 0; i < value; i++) {
                    len += array[i].length();
                    if (i < value - 1) len++;
                }
                return len;
        }        
        //should not reach here
        return 0;
    }
   
    //Note this will truncate
    public long longValue() {
        switch (type) {
			case TYPE_BASIC_STRING:
				//note for 0 length strings will return 0
                char[] buffer = (char[])data;
				int len = (int)value;
				type = TYPE_JAVA_LONG;
				value = 0;
				if (buffer[0] == '-') {
                    for (int i = 1; i < len; i++) {
						value *= 10;
						value -= Character.digit(buffer[i], 10);
                    }
				} else {
                    for (int i = 0; i < len; i++) {
						value *= 10;
						value += Character.digit(buffer[i], 10);
                    }
				}
                return value;
            case TYPE_JAVA_DOUBLE:
				double d = Double.longBitsToDouble(value);
				if (d == (double)((long)d)) {
					type = TYPE_JAVA_LONG;
					value = (long)d;
					return value;
				}
			    return (long)d;
            case TYPE_JAVA_LONG:
                return value;
			case TYPE_JAVA_STRING:
                char[] buffer2 = ((String)data).toCharArray();
				type = TYPE_JAVA_LONG;
				value = 0;
				if (buffer2[0] == '-') {
                    for (int i = 1; i < buffer2.length; i++) {
						value *= 10;
						value -= Character.digit(buffer2[i], 10);
                    }
				} else {
                    for (int i = 0; i < buffer2.length; i++) {
						value *= 10;
						value += Character.digit(buffer2[i], 10);
                    }
				}
                return value;
        case TYPE_INDEX_AM_STRING:
        case TYPE_INDEX_VM_STRING:
        case TYPE_INDEX_SM_STRING:
			//XXX should throw exception if value > 1
			if (value == 1) {
                value = ((BasicString[])data)[0].longValue();
			} else {
			    value = 0;
			}
            type = TYPE_JAVA_LONG;
            return value;
        }
        //should not reach here
        return 0L;
    }    
    
    public mvConstantString REMOVE(mvString colpos, mvString delim) {
        
        //1 check of type MV_ARRAY
        if (type != TYPE_INDEX_AM_STRING) {
            convertToMultivalue(TYPE_INDEX_AM_STRING);
        }
        BasicString[] array = (BasicString[])data;
        int len = (int)value;
        //2 ensure length has 4 spare spots for hidden params
        if (value + 4 >= array.length
                || array[len] == null
                || array[len + 1] == null
                || array[len + 2] == null
                || array[len + 3] == null) {
            array = resizeArray(len + 4);
            array[len] = (BasicString)factory.getString();
            array[len].set(1);
            array[len + 1] = (BasicString)factory.getString();
            array[len + 1].set(0);
            array[len + 2] = (BasicString)factory.getString();
            array[len + 2].set(0);
            array[len + 3] = (BasicString)factory.getString();
            array[len + 3].set(0);
        }
        //3 check for hidden params colpos and delim after length
        BasicString colpos2 = array[len];
        int index = array[len + 1].intValue();
        int index2 = array[len + 2].intValue();
        int index3 = array[len + 3].intValue();
        //4 compare colpos if != reset
		if (colpos != null && !colpos.equals(colpos2)) {
			int cp = colpos.intValue();
			if (cp == 0) {
				cp = 1;
			}
			index = 0;
			index2 = 0;
			index3 = 0;
			//XXX only roughly sets colpos(no account of VM or SM
			int count = 0;
			for (int i = 0; i < len; i++) {
				int al = array[i].length();
				if (count + al > cp) { 
					index = i;
					break;
				}
				count += al;
			}
			colpos.set(count);
			colpos2.set(count);
            if (cp != 0 && cp != 1) {
                System.err.println("XXX REMOVE need to reset colpos!");
            }
		}
        //5 look at stored param to find current index
        //6 dereference and call next
        BasicString result = null;
        if (index < len) {
            result = array[index];
            if (result.type != TYPE_INDEX_VM_STRING) {
                result.convertToMultivalue(TYPE_INDEX_VM_STRING);
            }
            int len2 = (int)result.value;
            if (index2 < len2) {
                array = (BasicString[])result.data;
                result = array[index2];
                if (result.type != TYPE_INDEX_SM_STRING) {
                    result.convertToMultivalue(TYPE_INDEX_SM_STRING);
                }
                int len3 = (int)result.value;
                if (index3 < len3) {
                    array = (BasicString[])result.data;
                    result = array[index3];
                    index3++;
                }
                if (index3 == len3) {
                    index2++;
                    index3 = 0;
                }
            }
            if (index3 == 0 && index2 == len2) {
                index++;
                index2=0;
            }
            //column position
			if (colpos != null) {
				int newlen = colpos2.intValue() +  result.length() + 1;
				colpos.set(newlen);
				colpos2.set(newlen);
			}
        }
        //delim
        if (index3 > 0) {
            delim.set(4);
        } else if (index2 > 0) {
            delim.set(3);
        } else if (index < len) {
            delim.set(2);
        } else {
            delim.set(0);
        }
        array = (BasicString[])data;
        array[len + 1].set(index);
        array[len + 2].set(index2);
        array[len + 3].set(index3);
        return (result != null) ? result : EMPTY;
    }

    public void replace(mvConstantString mvs, int start, int offset, int len) {
        switch (type) {
            case TYPE_JAVA_DOUBLE:                          
                data = doubleToString().toCharArray();            
                value = ((char[])data).length;
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_LONG:                          
                data = Long.toString(value, 10).toCharArray();            
                value = ((char[])data).length;
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_JAVA_STRING:
                data = ((String)data).toCharArray();
                value = ((char[])data).length;
                type = TYPE_BASIC_STRING;
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                //XXX this implementation has really poor performance
                for (int i = 0; i < len; i++) {
                    setCharAt(offset + i, mvs.charAt(start + i));
                }
                return;
        } 
        //potentially need to resize buffer if offset + len > length
        if (offset + len > value) {
            resizeBuffer(offset + len);
            value = offset + len;
        }
        mvs.getChars(start, start + len, (char[])data, offset);     
    }
    
    /**
    * Replaces the specified string at the specified attribute, value and subvalue mark.
    * @param attrib attribute position
    * @param val value mark position
    * @param subval subvalue mark postion
    * @param replace mvString to be replaced
    * @return result string containing the replaced entry
    */
    public mvString REPLACE(mvConstantString attrib, mvConstantString val, mvConstantString subval, mvConstantString replace) {                

        int a = attrib.intValue();

        BasicString item = this;
        if (type != TYPE_INDEX_AM_STRING) {
            convertToMultivalue(TYPE_INDEX_AM_STRING);
        }
        BasicString[] array = (BasicString[])data;
        int index = (a > 0) ? a - 1 : (int)item.value;

        int v = val.intValue();            
        if (v != 0) {
            if (index >= value) {
                if (index >= array.length) array = resizeArray(index + 1);
                while (index >= value) {
                    array[(int)value++] = (BasicString)factory.getString();
                }
            }
            item = array[index];
            if (item.type != TYPE_INDEX_VM_STRING) {
                item.convertToMultivalue(TYPE_INDEX_VM_STRING);
            }
            array = (BasicString[])item.data;
            index = (v > 0) ? v - 1 : (int)item.value;

            int s = subval.intValue();
            if (s != 0) {
                if (index >= item.value) {
                    if (index >= array.length) item.resizeArray(index + 1);
                    while (index >= item.value) {
                        array[(int)item.value++] = (BasicString)factory.getString();
                    }
                }
                item = array[index];
                if (item.type != TYPE_INDEX_SM_STRING) {
                    item.convertToMultivalue(TYPE_INDEX_SM_STRING);
                }
                array = (BasicString[])item.data;
                index = (s > 0) ? s - 1 : (int)item.value;
            }
        }
        if (index >= item.value) {
            if (index >= array.length) array = item.resizeArray(index + 1);
            while (index >= item.value) {
                array[(int)item.value++] = (BasicString)factory.getString();
            }
        }
        array[index].set(replace);
        //XXX replace containsDelimiters????
        return this;
    }
    
    
    /**
    * Ensures the string has specified capacity.    
    * Preserves current contents
    */               
    private char[] resizeBuffer(int len) {
        if (type != TYPE_BASIC_STRING) {
            data = new char[len];
            type = TYPE_BASIC_STRING;
            return (char[])data;
        }
        char[] tmp = (char[])data;
        if (value >= 0 && len > tmp.length) {
            int newsize = 1;
            while (newsize < len && (newsize + (newsize >> 1)) < len){
                newsize <<= 1;            
            }
            if (newsize < len) {
                newsize += newsize >> 1;
            }
            data = new char[newsize];
            System.arraycopy(tmp, 0, data, 0, (int)value);            
            tmp = (char[])data;
        }
        return tmp;
    }
    
    private BasicString[] resizeArray(int len) {
        switch (type) {
            case TYPE_BASIC_STRING:
            case TYPE_JAVA_STRING:
            case TYPE_JAVA_DOUBLE:
            case TYPE_JAVA_LONG:
                //XXX loses current info needs convertToMultivalue
                data = new BasicString[len];
                type = TYPE_INDEX_AM_STRING;
                return (BasicString[])data;
        }
        BasicString[] tmp = (BasicString[])data;
        if (len > tmp.length) {
            int newsize = 1;
            while (newsize < len && (newsize + (newsize >> 1)) < len){
                newsize <<= 1;            
            }
            if (newsize < len) {
                newsize += newsize >> 1;
            }
            data = new BasicString[newsize];
            System.arraycopy(tmp, 0, data, 0, (int)value);            
            tmp = (BasicString[])data;
        }
        return tmp;
    } 

    /**
    * Returns a select list from the current string.
    * If the string is of type File, then the select
    * will occur against the database.  If the string is of
    * any other type then the resulting list consists of the
    * attributes from the string.
    * @param result the variable to hold the List
    * @param f factory
    */
    public void SELECT(Program program, mvString result, Factory f) throws mvException {
        if (type == TYPE_MV_FILE) {
            ((File)data).SELECT(program, result, factory.getKey());
        } else {
            super.SELECT(program, result, f);
        }
    }

    public void set(char[] c, int start, int len) {
        value = 0;
        resizeBuffer(len);
        System.arraycopy(c, start, data, 0, len);
        value = len;
        type = TYPE_BASIC_STRING;
    }
    
    public void set(mvConstantString mvs) {
        if (mvs == this)  {
            //reset REMOVE values
            if (type == TYPE_INDEX_AM_STRING) {
                BasicString[] array = (BasicString[])data;
                int index= (int)value;
                if (index + 4 <= array.length) {
                    array[index] = null;
                    array[index + 1] = null;
                    array[index + 2] = null;
                    array[index + 3] = null;
                }
            }
            return;
        } else if (mvs instanceof BasicString) {
            BasicString s = (BasicString)mvs;
            while ((s.type == TYPE_INDEX_AM_STRING
			        || s.type == TYPE_INDEX_VM_STRING
			        || s.type == TYPE_INDEX_SM_STRING)
				&& s.value == 1) {
				s = ((BasicString[])s.data)[0];  //recurse
			}
            switch (s.type) {
                case TYPE_INDEX_AM_STRING:
                case TYPE_INDEX_VM_STRING:
                case TYPE_INDEX_SM_STRING:
                    s = (BasicString)mvs;
                    type = s.type;
                    value = s.value;
                    BasicString[] sarr = (BasicString[])s.data;
                    BasicString[] arr = new BasicString[(int)value];
                    for (int i = 0; i < value; i++) {
                        arr[i] = (BasicString)factory.getString();
                        arr[i].set(sarr[i]);
                    }
                    data = arr;
                    return;
                case TYPE_JAVA_DOUBLE:
                case TYPE_JAVA_LONG:
                case TYPE_JAVA_STRING:
                    type = s.type;
                    value = s.value;
                    data = s.data;
                    return;
            }
        } else if (mvs.isNumeric()) {
            if (mvs.isIntegral()) {
                if (mvs.charAt(0) == '0') {
                    // catch values with leading zeroes
                    type = TYPE_JAVA_STRING;
                    data = mvs.toString();
                } else {
                    type = TYPE_JAVA_LONG;
                    value = mvs.longValue();
                }
                return;
            } else {
                type = TYPE_JAVA_DOUBLE;
                value = doubleToLongBits(mvs.getDouble());
                return;
            }
        } else if (mvs instanceof org.maverickdbms.basic.string.JavaString) {
                type = TYPE_JAVA_STRING;
                data = mvs.toString();
                return;
        }
        value = 0;
        int newlen = mvs.length();
        mvs.getChars(0, newlen, resizeBuffer(newlen), 0);        
        value = newlen;    
        type = TYPE_BASIC_STRING;
    } 
    
    public void set(mvConstantString mvs, int start, int len) {
        //bounds check mvs to make sure length does not exceed string
        //...behaviour needed by basic [] operator
        int mvslen = mvs.length();
        if (start < 0) {
            start = 0;
        }
        if (start > mvslen) {
            start = mvslen;
        }
        if (mvslen < start + len) {
            len = mvslen - start;
        }
        if (mvs == this) {
            switch (type) {
            case TYPE_JAVA_STRING:
                data = ((String)data).substring(start, start + len);
                break;
            case TYPE_JAVA_DOUBLE:
                data = doubleToString().substring(start, start + len);
                type = TYPE_JAVA_STRING;
                break;
            case TYPE_JAVA_LONG:
                data = Long.toString(value, 10).substring(start, start + len);
                type = TYPE_JAVA_STRING;
                break;
            case TYPE_INDEX_AM_STRING:
            case TYPE_INDEX_VM_STRING:
            case TYPE_INDEX_SM_STRING:
                //XXX this code is pretty dodgy and has had no testing...
                BasicString[] array = (BasicString[])data;
                int index = 0;
                int pos1 = 0;
                int pos2 = array[index].length();
                while (pos2 < start) {
                    pos1 = pos2;
                    pos2 += array[index++].length() + 1;
                }
                if (index > 0) {
                    System.arraycopy(array, index, array, 0, (int)value - index);
                    index = 0;
                }
                if (start > pos1) {
                    array[index].set(array[index], start - pos1, pos2 - start);
                    pos1 = 0;
                    pos2 = pos2 - start;
                }
                while (pos2 < start + len) {
                    pos1 = pos2;
                    pos2 += array[index++].length() + 1;
                }
                value = index;
                array[index].setLength(pos2 - start - len);
                break;
            case TYPE_BASIC_STRING:
                System.arraycopy(data, start, data, 0, len);
                value = len;
                break;
            }
        } else {
            value = 0;
            mvs.getChars(start, start + len, resizeBuffer(len), 0);        
            value = len;    
            type = TYPE_BASIC_STRING;
        }
    }
    
    public void set(String s) {        
        type = TYPE_JAVA_STRING;
        data = s;                   
    }  

    
    public void set(BigDecimal num) {
        char[] c = num.toString().toCharArray();
        value = 0;
        System.arraycopy(c, 0, resizeBuffer(c.length), 0, c.length);
        value = c.length;
        type = TYPE_BASIC_STRING;
    }
    
    public void set(char c) {
        resizeBuffer(1)[0] = c;
        value = 1;
        type = TYPE_BASIC_STRING;
    }
    
    public void set(double num) {
        value = doubleToLongBits(num);
        type = TYPE_JAVA_DOUBLE;
    }
    
    public void set(int num) {     
        value = num;
        type = TYPE_JAVA_LONG;
    }
    
    public void set(long num) {
        value = num;
        type = TYPE_JAVA_LONG;
    }

    public void setCharAt(int index, char c) {
        switch (type) {
            case TYPE_BASIC_STRING:                          
                char[] buffer = (char[])data;
                if (value <= index) {
                    buffer = resizeBuffer(index + 1);
                    value = index + 1; //XXX need to do something about fill chars
             
