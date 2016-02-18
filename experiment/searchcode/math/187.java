package org.crap4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CPStringReader {


	  private byte[] b;
	  private String className;
	  private String superName;
	  private String[] strings;
	  
	  public CPStringReader(InputStream is) throws IOException {
	    this(readClass(is));
	  }
	  
	  public CPStringReader(byte[] b) {
	    this.b = b;
	    
	    int index = 8;
	    
	    int cpEntriesCount = readUnsignedShort(index);
	    int[] cpOffsets = new int[cpEntriesCount];
	    index += 2;
	    
	    List classAndStringsIndexes = new ArrayList();
	    
	    // Parse the constant pool.
	    
	    int max = 0;
	    
	    for (int i = 1; i < cpEntriesCount; i++) {
	      int cpTag = b[index];
	      int size = 0;
	      
	      cpOffsets[i] = index + 1;
	      
	      switch(cpTag) {
	      case INT:
	      case FLOAT:
	      case FIELD:
	      case NAME_TYPE:
	      case METH:
	      case IMETH:
	        size = 5;
	        break;
	      case LONG:
	      case DOUBLE:
	        size = 9;
	        i++;
	        break;
	      case UTF8:
	        classAndStringsIndexes.add(new Integer(i));
	        size = 3 + readUnsignedShort(index + 1);
	        if (size > max) {
	            max = size;
	        }
	        break;
	      case CLASS:
	      case STR:
	      default:
	        size = 3;
	        break;
	      }
	      
	      index += size;
	    }
	    
	    char[] stringBuf = new char[max];
	    
	    strings = new String[classAndStringsIndexes.size()];                               
	    
	    for (int i = 0; i < strings.length; i++) {
	      strings[i] = readUTF8(cpOffsets[((Integer)classAndStringsIndexes.get(i)).intValue()], stringBuf);
	    }
	    
	    className = readUTF8(cpOffsets[readUnsignedShort(cpOffsets[readUnsignedShort(index + 2)])], stringBuf);
	    int v = cpOffsets[readUnsignedShort(index + 4)];
	    superName = v == 0 ? null : readUTF8(cpOffsets[readUnsignedShort(v)], stringBuf);
	  }
	    
	  public String getClassName() {
	    return className.replace('/', '.');
	  }
	  
	  public String getSuperClassName() {
	    return superName.replace('/', '.');
	  }
	  
	  public String[] getConstantPoolStrings() {
	    return strings;
	  }
	  
	  /**
	   * Reads the bytecode of a class.
	   * 
	   * @param is an input stream from which to read the class.
	   * @return the bytecode read from the given input stream.
	   * @throws IOException if a problem occurs during reading.
	   */
	  private static byte[] readClass(final InputStream is) throws IOException {
	      if (is == null) {
	          throw new IOException("Class not found");
	      }
	      byte[] b = new byte[is.available()];
	      int len = 0;
	      while (true) {
	          int n = is.read(b, len, b.length - len);
	          if (n == -1) {
	              if (len < b.length) {
	                  byte[] c = new byte[len];
	                  System.arraycopy(b, 0, c, 0, len);
	                  b = c;
	              }
	              return b;
	          }
	          len += n;
	          if (len == b.length) {
	              byte[] c = new byte[b.length + 1000];
	              System.arraycopy(b, 0, c, 0, len);
	              b = c;
	          }
	      }
	  }
	  
	  /**
	   * Reads an unsigned short value in {@link #b b}. <i>This method is
	   * intended for {@link Attribute} sub classes, and is normally not needed by
	   * class generators or adapters.</i>
	   * 
	   * @param index the start index of the value to be read in {@link #b b}.
	   * @return the read value.
	   */
	  private int readUnsignedShort(final int index) {
	      byte[] b = this.b;
	      return ((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF);
	  }

	  /**
	   * Reads an UTF8 string constant pool item in {@link #b b}. <i>This method
	   * is intended for {@link Attribute} sub classes, and is normally not needed
	   * by class generators or adapters.</i>
	   * 
	   * @param index the start index of an unsigned short value in {@link #b b},
	   *        whose value is the index of an UTF8 constant pool item.
	   * @param buf buffer to be used to read the item. This buffer must be
	   *        sufficiently large. It is not automatically resized.
	   * @return the String corresponding to the specified UTF8 item.
	   */
	  
	  private String readUTF8(int index, final char[] buf) {
	    byte[] b = this.b;
	    int length = readUnsignedShort(index);
	    return readUTF(index + 2, length, buf);
	  }
	//  public String readUTF8(int index, final char[] buf) {
//	      int item = readUnsignedShort(index);
//	      String s = strings[item];
//	      if (s != null) {
//	          return s;
//	      }
//	      index = items[item];
//	      return strings[item] = readUTF(index + 2, readUnsignedShort(index), buf);
	//  }

	  /**
	   * Reads UTF8 string in {@link #b b}.
	   * 
	   * @param index start offset of the UTF8 string to be read.
	   * @param utfLen length of the UTF8 string to be read.
	   * @param buf buffer to be used to read the string. This buffer must be
	   *        sufficiently large. It is not automatically resized.
	   * @return the String corresponding to the specified UTF8 string.
	   */
	  private String readUTF(int index, final int utfLen, final char[] buf) {
	      int endIndex = index + utfLen;
	      byte[] b = this.b;
	      int strLen = 0;
	      int c, d, e;
	      while (index < endIndex) {
	          c = b[index++] & 0xFF;
	          switch (c >> 4) {
	              case 0:
	              case 1:
	              case 2:
	              case 3:
	              case 4:
	              case 5:
	              case 6:
	              case 7:
	                  // 0xxxxxxx
	                  buf[strLen++] = (char) c;
	                  break;
	              case 12:
	              case 13:
	                  // 110x xxxx 10xx xxxx
	                  d = b[index++];
	                  buf[strLen++] = (char) (((c & 0x1F) << 6) | (d & 0x3F));
	                  break;
	              default:
	                  // 1110 xxxx 10xx xxxx 10xx xxxx
	                  d = b[index++];
	                  e = b[index++];
	                  buf[strLen++] = (char) (((c & 0x0F) << 12)
	                          | ((d & 0x3F) << 6) | (e & 0x3F));
	                  break;
	          }
	      }
	      return new String(buf, 0, strLen);
	  }



	  /**
	   * The type of CONSTANT_Class constant pool items.
	   */
	  final static int CLASS = 7;

	  /**
	   * The type of CONSTANT_Fieldref constant pool items.
	   */
	  final static int FIELD = 9;

	  /**
	   * The type of CONSTANT_Methodref constant pool items.
	   */
	  final static int METH = 10;

	  /**
	   * The type of CONSTANT_InterfaceMethodref constant pool items.
	   */
	  final static int IMETH = 11;

	  /**
	   * The type of CONSTANT_String constant pool items.
	   */
	  final static int STR = 8;

	  /**
	   * The type of CONSTANT_Integer constant pool items.
	   */
	  final static int INT = 3;

	  /**
	   * The type of CONSTANT_Float constant pool items.
	   */
	  final static int FLOAT = 4;

	  /**
	   * The type of CONSTANT_Long constant pool items.
	   */
	  final static int LONG = 5;

	  /**
	   * The type of CONSTANT_Double constant pool items.
	   */
	  final static int DOUBLE = 6;

	  /**
	   * The type of CONSTANT_NameAndType constant pool items.
	   */
	  final static int NAME_TYPE = 12;

	  /**
	   * The type of CONSTANT_Utf8 constant pool items.
	   */
	  final static int UTF8 = 1;
	  
	}

