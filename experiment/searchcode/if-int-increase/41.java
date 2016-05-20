package com.objectwave.classFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A specific attribute info.
 * @see		ClassFile
 */
public class CodeAttributeInfo extends AttributeInfo
{
	AttributeInfo [] otherAttributes;
	byte [] exceptionTable;
	static final int lookupSwitchOpCode = 171;
	static final int tableSwitchOpCode = 170;
	static final int invokeInterfaceOpCode = 185;

	ConstantPoolInfo [] caughtTypes;
	/**
	 */
	protected void fixUpConstants(ClassFile target, ConstantPoolInfo [] originalPool) throws Exception
	{
		super.fixUpConstants(target, originalPool);
		if(caughtTypes != null)
		{
			for(int i = 0; i < caughtTypes.length; ++i)
			{
				caughtTypes [i] = target.recursiveAdd(caughtTypes [i]);
			}
		}
		if(otherAttributes != null)
		{
			for(int i = 0; i < otherAttributes.length; ++i)
			{
				otherAttributes[i].fixUpConstants(target, originalPool);
			}
		}
		fixUpCode(target, originalPool);
    }
    /**
     * If this method is copied into another class, it may become necessary to fix up all references
     * to the constants in the original pool.
     */
	protected void fixUpCode(ClassFile target, ConstantPoolInfo [] originalPool) throws Exception
	{
		int len = getInt(data, 4);
		byte [] twoBytes = new byte [2];
		for(int i = 8; i - 8 < len; ++i)
		{
		    int idx = ((int)0xff & data[i]);
		    if(idx < opCodeTable.length && idx > -1)
		    {
		        if(shouldGetConstantFromOne(idx))
		        {
		            int barIdx = opCodeTable[idx].lastIndexOf('_');
		            int varIdx = -1;
		            if(barIdx < 0)
		            {
		                twoBytes [0] = 0;
		                twoBytes [1] = data[++i];
		                varIdx = indexFromBytes(twoBytes);

		                ConstantPoolInfo info = originalPool [varIdx];
		                ConstantPoolInfo cp = target.recursiveAdd(info);

		                varIdx = cp.indexOf(cp, target.constantPool);

		                twoBytes = bytesFromIndex((short)varIdx);
		                data [i] = twoBytes[1];
			        }
		        }
		        if(shouldGetConstant(idx))
		        {
		            int varIdx = -1;
		            twoBytes [0] = data[++i];
		            twoBytes [1] = data[++i];

		            varIdx = indexFromBytes(twoBytes);
		            ConstantPoolInfo info = originalPool [varIdx];
		            ConstantPoolInfo cp = target.recursiveAdd(info);
		            varIdx = cp.indexOf(cp, target.constantPool);
		            twoBytes = bytesFromIndex((short)varIdx);
		            data [i - 2] = twoBytes[0];
		            data [i - 1] = twoBytes[1];
		        }
		    }
		}
	}
    /**
	 */
	public CodeAttributeInfo()
	{
	}
	public void adjustExceptionTable(short increase)
	{
		ConstantPoolInfo [] result;
		if(exceptionTable != null && exceptionTable.length > 0)
		{
System.out.println("Adjusting exception table!");
			result = new ConstantPoolInfo [ exceptionTable.length / 8 ];
			for(int i = 0; i < result.length; ++i)
			{
				int il = (i * 8) + 6;
int start_pc = il - 6;
int end_pc = il - 4;
int handler_pc = il - 2;

			    byte [] twoBytes = new byte [2];

twoBytes [0] = exceptionTable[ start_pc ];
twoBytes [1] = exceptionTable[ start_pc + 1];
short idx = indexFromBytes(twoBytes);
System.out.println("Changing " + idx + " to " + (idx + increase));
twoBytes = bytesFromIndex((short)(idx + increase));
exceptionTable[ start_pc ] = twoBytes [0];
exceptionTable[ start_pc + 1] = twoBytes [1];

twoBytes [0] = exceptionTable[ end_pc ];
twoBytes [1] = exceptionTable[ end_pc + 1];
idx = indexFromBytes(twoBytes);
twoBytes = bytesFromIndex((short)(idx + increase));
exceptionTable[ end_pc ] = twoBytes [0];
exceptionTable[ end_pc + 1] = twoBytes [1];

twoBytes [0] = exceptionTable[ handler_pc ];
twoBytes [1] = exceptionTable[ handler_pc + 1];
idx = indexFromBytes(twoBytes);
twoBytes = bytesFromIndex((short)(idx + increase));
exceptionTable[ handler_pc ] = twoBytes [0];
exceptionTable[ handler_pc + 1] = twoBytes [1];

			}
		}
	}
	/**
	 * Null values in the resulting array are legal. They represent 'finally' catch blocks.
	 * @return All of the caught types declared in the exception table.
	 */
	public ConstantPoolInfo [] getCatchTypes(ConstantPoolInfo [] pool)
	{
		ConstantPoolInfo [] result;
		if(exceptionTable.length > 0)
		{
			result = new ConstantPoolInfo [ exceptionTable.length / 8 ];
			for(int i = 0; i < result.length; ++i)
			{
				int idx = (i * 8) + 6;
int start_pc = idx - 6;
int end_pc = idx - 4;
int handler_pc = idx - 2;

			    byte [] twoBytes = new byte [2];

twoBytes [0] = exceptionTable[ start_pc ];
twoBytes [1] = exceptionTable[ start_pc + 1];
//System.out.println("Start_pc " + indexFromBytes(twoBytes));
twoBytes [0] = exceptionTable[ end_pc ];
twoBytes [1] = exceptionTable[ end_pc + 1];
//System.out.println("end_pc " + indexFromBytes(twoBytes));
twoBytes [0] = exceptionTable[ handler_pc ];
twoBytes [1] = exceptionTable[ handler_pc + 1];
//System.out.println("handler_pc " + indexFromBytes(twoBytes));

		    	twoBytes [0] = exceptionTable[ idx ];
		    	twoBytes [1] = exceptionTable[ idx + 1];
            	int constPoolIdx = indexFromBytes(twoBytes);
            	if(constPoolIdx != 0)
            	{
            		result [i ] = pool[constPoolIdx ];
            	}
			}
		}
		else
		{
			result = new ConstantPoolInfo [ 0 ];
		}
		return result;
	}
	/**
	 */
	public CodeAttributeInfo(ConstantPoolInfo newName, byte newData[])
	{
	    super(newName, newData);
	}
	/**
	Code_attribute {
            u2 attribute_name_index;
            u4 attribute_length;
            u2 max_stack;
            u2 max_locals;
            u4 code_length;
            u1 code[code_length];
            u2 exception_table_length;
            {
                u2 start_pc;
                u2 end_pc;
                u2  handler_pc;
                u2  catch_type;
            }
            exception_table[exception_table_length];

            u2 attributes_count;
            attribute_info attributes[attributes_count];
	 */
	public boolean read(DataInputStream di, ConstantPoolInfo pool[]) throws IOException
	{
		int len;
        //The name should already be set!
        if(name == null)
        {
    		name = pool[di.readShort()];
    	}
		len = di.readInt();
		data = new byte[len];
//		len  = di.read(data);
//		if (len != data.length)
//		{
//			System.out.println("DATA does not equal length");
//			return false;
//		}
		di.readFully(data);

		readCodeAttributes(pool);
		return true;
	}
	/**
	 * Optional code attributes. These include a LocalVariableTable and the  LineNumberTable.
	 */
	protected void readCodeAttributes(ConstantPoolInfo pool[]) throws IOException
	{
	    int len = data.length;
		int actualCodeLen = getInt(data, 4);
		final int headerAndCode = actualCodeLen + 8;
		int dif = len - headerAndCode;  //Len is the total length of data - the amount for code.

		if(dif > 0) //X remains for exception table and local variables.
		{
		    byte [] twoBytes = new byte [2];
		    twoBytes [0] = data[ headerAndCode + 0];
		    twoBytes [1] = data[ headerAndCode + 1];
            int addlCount = indexFromBytes(twoBytes);
            int exceptionTableData = (addlCount * 8) + 2; //The number of bytes for both the table and size field
            if(exceptionTableData > 2)
            {
                exceptionTable = new byte [exceptionTableData - 2]; //The size field is not used
                System.arraycopy(data, headerAndCode + 2, exceptionTable, 0, exceptionTableData - 2);
                caughtTypes = getCatchTypes(pool);
            }
            dif = dif - exceptionTableData;
            if(dif > 0)
            {
		        byte [] bytes = new byte[dif - 2];

		        twoBytes [0] = data[ headerAndCode + exceptionTableData + 0];
		        twoBytes [1] = data[ headerAndCode + exceptionTableData + 1];
                addlCount = indexFromBytes(twoBytes);
		        System.arraycopy(data, headerAndCode + exceptionTableData + 2 ,bytes,0, dif - 2);

                java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(bytes);
		        DataInputStream din = new DataInputStream(bin);

		        otherAttributes = new AttributeInfo [addlCount] ;
		        for(int i = 0; i < addlCount; ++i)
		        {
    		        AttributeInfo addl = AttributeInfo.readAttributeInfo(din, pool);
		            otherAttributes[i] = addl;
		            if(addl != null)
		            {
//		                System.out.println("Created addl one " + addl);
		            }
		            else
		            {
		                System.out.println("Failed optional method attribute!");
		            }
		        }
		    }
		}
	}
	/**
	 * Write the bytes to the output stream.
	 * @param dos The DataOutputStream upon which this is writing
	 * @param pool The constant pool in which to index.
	 */
	public void write(DataOutputStream dos, ConstantPoolInfo pool[]) throws IOException, Exception
	{
		dos.writeShort(ConstantPoolInfo.indexOf(name, pool));
		dos.writeInt(data.length);
//The original code would write all of the bytes in the data
//		dos.write(data, 0, data.length);

	    int len = data.length;
		int actualCodeLen = getInt(data, 4);
		final int headerAndCode = actualCodeLen + 8;

		dos.write(data, 0, headerAndCode + 2); //The length of exception table adds 2

		byte [] twoBytes = new byte [2];
		twoBytes [0] = data[ headerAndCode + 0];
		twoBytes [1] = data[ headerAndCode + 1];
        int addlCount = indexFromBytes(twoBytes);


		if(caughtTypes == null || caughtTypes.length ==0 || addlCount !=  caughtTypes.length)
		{
			dos.write(data, headerAndCode + 2, (addlCount * 8));
		}
		else
		{
        	for(int i = 0; i < addlCount; ++i)
        	{
//        		dos.write(data, headerAndCode + 2 + (i * 8), 6);
				dos.write(exceptionTable, (i * 8), 6);
        		short idx = caughtTypes[i].indexOf(caughtTypes[i], pool);
        		twoBytes = bytesFromIndex(idx);
        		dos.write(twoBytes);
        	}
        }
        if(otherAttributes != null )
        {
	        twoBytes = bytesFromIndex((short)otherAttributes.length);
	        dos.write(twoBytes);
        	for(int i = 0; i < otherAttributes.length; ++i)
        	{
        		otherAttributes[i].write(dos, pool);
        	}
        }
        else
        {
			twoBytes = bytesFromIndex((short)0);
	        dos.write(twoBytes);
        }
	}
	/**
	 * If there is a local var table, find the local var at the index.
	 */
	public String getLocalVar(int idx)
	{
		try
		{
			if(otherAttributes == null) return "";
			LocalVariableAttributeInfo locals = null;
			for(int i = 0; i < otherAttributes.length; ++i)
			{
				if(otherAttributes[i] instanceof LocalVariableAttributeInfo)
				{
					locals = (LocalVariableAttributeInfo)otherAttributes[i];
					break;
				}
			}
			if(locals == null) return "";
			return locals.getLocalVarName(idx);
		}
		catch(Throwable t)
		{
			return "";
		}
	}
	/**
	 */
	public String toString(ConstantPoolInfo pool[])
	{
		StringBuffer x = new StringBuffer();
		String type = name.toString();
		if(caughtTypes != null)
		{
			x.append(String.valueOf(caughtTypes.length) + " type(s) caught by blocks\n");
			for(int i = 0; i < caughtTypes.length; ++i)
			{
				if(caughtTypes[i] == null)
				{
					x.append("finally\n");
				}
				else
				{
					x.append(caughtTypes[i].toString() + '\n');
				}
			}
		}
		try
		{
			displayCode(x, pool);
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			System.out.println(x);
			throw ex;
		}
		return x.toString();
    }
    /**
     */
	public void addCalledMethods(ArrayList result, ConstantPoolInfo  pool[])
	{
		byte [] twoBytes = new byte [2];
		int len = getInt(data, 4);
		for(int i = 8; i - 8 < len; ++i)
		{
		    int idx = ((int)0xff & data[i]);
		    if(idx < opCodeTable.length && idx > -1)
		    {
		        if(idx == lookupSwitchOpCode || idx == tableSwitchOpCode )
		        {
		            i = handleLookupSwitch(idx, data, i, new StringBuffer());
		        }
		        if(idx == invokeInterfaceOpCode)
		        {
		            twoBytes [0] = data[++i];
		            twoBytes [1] = data[++i];
		            result.add(pool[indexFromBytes(twoBytes)]);
		            ++i;
		            i++;
		        }
		        if(shouldGetConstantFromOne(idx))
		        {
		            int barIdx = opCodeTable[idx].lastIndexOf('_');
		            int varIdx = -1;
		            if(barIdx < 0)
		            {
		                twoBytes [0] = 0;
		                twoBytes [1] = data[++i];
		                varIdx = indexFromBytes(twoBytes);
			        }
			        else
			        {
			            try
			            {
			            	varIdx = new Integer(opCodeTable[idx].substring(barIdx + 1).trim()).intValue();
			            }
			            catch (Exception t) { }
			        }
			        if(varIdx > -1)
					{
			            if(pool[varIdx].type == ConstantPoolInfo.METHODREF)
			            {
			                result.add(pool[varIdx]);
			            }
			        }
		        }
		        if(shouldGetConstant(idx))
		        {
		            twoBytes [0] = data[++i];
		            twoBytes [1] = data[++i];
		            int aIdx = indexFromBytes(twoBytes);

			        if(pool[aIdx].type == ConstantPoolInfo.METHODREF)
			        {
			            result.add(pool[aIdx]);
			        }

		        }
		        if(localVarAccess(idx))
		        {
		            int barIdx = opCodeTable[idx].lastIndexOf('_');
		            if(barIdx < 0)
		            {
		            	++i;
			        }
		        }
                if(hasFourByteDatum(idx))
                {
                    i++;
                    i++;
                    i++;
                    i++;
                }
		        if(hasTwoByteDatum(idx))
		        {
		            twoBytes [0] = data[++i];
		            twoBytes [1] = data[++i];
		        }
		        if(hasOneByteDatum(idx))
		        {
		            twoBytes [1] = data[++i];
		        }
		    }
		    else
		    {
		        throw new RuntimeException ("Failed to correctly process bytes!!! idx in not in allowed range.");
//		        	System.out.println("Failed to correctly process bytes!!!");
		    }
		}
	}
	/**
	 * Only called by the MethodInfo class if the System parameter of ow.showAttributes is set to non-null.
	 * @param buff The buffer upon which information is being written.
	 * @param pool ConstantPoolInfo [] The constant pool containing strings, classes, etc...
	 */
	protected void displayCode(final StringBuffer buff, ConstantPoolInfo pool[])
	{
		buff.append("\tCode <"+data.length+" bytes>");
		if(exceptionTable != null)
		{
			buff.append("\tExceptionTable <"+exceptionTable.length+" bytes>");
		}
		java.io.StringWriter sw = new java.io.StringWriter();
		try
		{
		    if(System.getProperty("ow.hexDump") != null)
		    {
    		    hexDump(data, data.length, sw);
		        buff.append(sw.toString());
		    }
    		buff.append("\n");
		    byte [] twoBytes = new byte [2];
		    twoBytes [0] = data[0];
		    twoBytes [1] = data[1];
		    buff.append("\tmax_stack " + indexFromBytes(twoBytes) + "\n");
		    twoBytes [0] = data[2];
		    twoBytes [1] = data[3];
		    buff.append("\tmax_locals " + indexFromBytes(twoBytes) + "\n");
		    int len = getInt(data, 4);

		    for(int i = 8; i - 8 < len; ++i)
		    {
		        buff.append("\t" + ((int)0xff & data[i]) + " ");
		        int idx = ((int)0xff & data[i]);
		        if(idx < opCodeTable.length && idx > -1)
		        {
		            buff.append(opCodeTable[idx]);
		            if(idx == lookupSwitchOpCode || idx == tableSwitchOpCode )
		            {
		            	i = handleLookupSwitch(idx, data, i, buff);
		            }
		            if(idx == invokeInterfaceOpCode)
		            {
    		            buff.append(" ");
		                twoBytes [0] = data[++i];
		                twoBytes [1] = data[++i];
		                buff.append(pool[indexFromBytes(twoBytes)] );
		                twoBytes [0] = 0;
		                twoBytes [1] = data[++i];
		                int nArgs = indexFromBytes(twoBytes);
		                i++;
		            }
		            if(shouldGetConstantFromOne(idx))
		            {
		            	int barIdx = opCodeTable[idx].lastIndexOf('_');
		            	int varIdx = -1;
		            	if(barIdx < 0)
		            	{
		                	twoBytes [0] = 0;
		                	twoBytes [1] = data[++i];
		                	varIdx = indexFromBytes(twoBytes);
			            }
			            else
			            {
			            	try
			            	{
			            		varIdx = new Integer(opCodeTable[idx].substring(barIdx + 1).trim()).intValue();
			            	}
			            	catch (Throwable t) { buff.append(t.toString());}
			            }
			            if(varIdx > -1)
						{
    		            	buff.append(" ");
			                buff.append(pool[varIdx] );
			            }
		            }
		            if(shouldGetConstant(idx))
		            {
    		            buff.append(" ");
		                twoBytes [0] = data[++i];
		                twoBytes [1] = data[++i];
		                buff.append(pool[indexFromBytes(twoBytes)] );
		            }
		            if(localVarAccess(idx))
		            {
		            	int barIdx = opCodeTable[idx].lastIndexOf('_');
		            	int varIdx = -1;
		            	if(barIdx < 0)
		            	{
		                	buff.append(" localVar ");
			                varIdx = data[++i];
			            }
			            else
			            {
			            	try
			            	{
			            		varIdx = new Integer(opCodeTable[idx].substring(barIdx + 1).trim()).intValue();
			            	}
			            	catch (Throwable t) { buff.append(t.toString());}
			            }
//			            buff.append(" varIdx : " + varIdx);
			            if(varIdx > -1)
			            {
				            buff.append(' ');
			            	buff.append(getLocalVar(varIdx));
			            }
		            }
		            if(hasFourByteDatum(idx))
		            {
    		            buff.append(' ');
                        int ival = getInt(data, ++i);
                        i = i + 3;
		                buff.append( ival );
		            }

		            if(hasTwoByteDatum(idx))
		            {
    		            buff.append(' ');
		                twoBytes [0] = data[++i];
		                twoBytes [1] = data[++i];
		                buff.append( indexFromBytes(twoBytes) );
		            }
		            if(hasOneByteDatum(idx))
		            {
    		            buff.append(" ");
		                twoBytes [0] = 0;
		                twoBytes [1] = data[++i];
		                buff.append( indexFromBytes(twoBytes) );
		            }
		            buff.append("\n");
		        }
		        else
		        {
		            buff.append("###FAILURE##\n");
		        }
		    }
		}
		catch (IOException e) { e.printStackTrace(); }

	}
	/**
	 * The lookupswith opcode has a variable length of data. This method
	 * will deal with that accordingly.
	 *
	 * @return The index of the last data element read. It is up to the user of this method
	 * to increment accordingly.
	 */
	protected int handleLookupSwitch(int opCode, byte [] data, int idx, StringBuffer buff)
	{
		while((++idx % 4) != 0) //Skip padded zeros
		{
//			System.out.print("pad ");
		}
		buff.append('\n');
		int defaultByte = getInt(data, idx);

		idx += 4;
		int lowByte = getInt(data, idx);
		idx += 4;

		int count = lowByte;
		if(opCode == tableSwitchOpCode)
		{
			int highByte = getInt(data, idx);
			idx += 4;
			count = highByte - lowByte + 1;
		}

		int value;
		for(int i = 0; i < count; ++i)
		{
			value = getInt(data, idx);
			buff.append("key " + value);
			idx += 4;
			value = getInt(data, idx);
			buff.append(" value " + value);
			buff.append('\n');
			idx += 4;
		}

		return idx - 1;
	}
    /**
    *  This method will print a hex dump of the given byte array to the given
    *  output stream.  Each line of the output will be 2-digit hex numbers,
    *  separated by single spaces, followed by the characters corresponding to
    *  those hex numbers, or a '.' if the given character is unprintable.  Each of
    *  these numbers will correspond to a byte of the byte array.
    *
    *  @author Steve Sinclair
    *  @param bytes the byte array to write
    *  @param writer the destination for the output.
    *  @exception java.io.IOException thrown if there's an error writing strings to the writer.
    */
    public static void hexDump(final byte[] bytes, int read, final java.io.Writer writer)
	    throws java.io.IOException
    {
	    final int width = 16;

	    for (int i=0; i < read; i += width)
	    {
		    int limit = (i+width > read) ? read - i : width;
		    int j;
		    StringBuffer literals = new StringBuffer(width);
		    StringBuffer hex = new StringBuffer(width*3);
		    for (j=0; j < limit; ++j)
		    {
			    int aByte = bytes[i+j];
			    if (aByte < 0)
				    aByte = 0xff + aByte + 1;
			    if (aByte < 0x10)
				    hex.append('0');
			    hex.append(Integer.toHexString(aByte));
			    hex.append(' ');
			    if (aByte >= 32 && aByte < 128)
				    literals.append((char)aByte);
			    else
				    literals.append('.'); // unprintable
		    }
		    for (/*use current j value*/; j < width; ++j)
		    {
			    literals.append(" ");
			    hex.append("-- ");
		    }
		    hex.append(' ');
		    hex.append(literals); // use hex to build the line
		    hex.append('\n');
		    writer.write(hex.toString()); // write the line.
	    }
    }
	boolean localVarAccess(int idx)
	{
	    for(int i = 0; i < localLookup.length; ++i)
	    {
	        if(localLookup[i] == idx) return true;
	    }
	    return false;
	}
	boolean shouldGetConstantFromOne(int idx)
	{
	    for(int i = 0; i < constLookupOne.length; ++i)
	    {
	        if(constLookupOne[i] == idx) return true;
	    }
	    return false;
	}
	boolean shouldGetConstant(int idx)
	{
	    for(int i = 0; i < constLookup.length; ++i)
	    {
	        if(constLookup[i] == idx) return true;
	    }
	    return false;
	}
    boolean hasFourByteDatum(final int idx)
	{
	    for(int i = 0; i < fourByteDatum.length; ++i)
	    {
	        if(fourByteDatum[i] == idx) return true;
	    }
	    return false;
	}
	boolean hasTwoByteDatum(final int idx)
	{
	    for(int i = 0; i < twoByteDatum.length; ++i)
	    {
	        if(twoByteDatum[i] == idx) return true;
	    }
	    return false;
	}
	boolean hasOneByteDatum(final int idx)
	{
	    for(int i = 0; i < oneByteDatum.length; ++i)
	    {
	        if(oneByteDatum[i] == idx) return true;
	    }
	    return false;
	}
	int [] localLookup = {21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78 };
	int [] constLookupOne = { 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 18 };
	int [] constLookup = { 19, 20, 178, 179, 180, 181, 182, 183, 184, 187, 189 , 192, 193 };
	int [] oneByteDatum = { 16 , 188 };
	int [] twoByteDatum = { 17, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166,167 , 198 , 199};
    int [] fourByteDatum = { 200 };
	String [] opCodeTable = {
        /*0 (0x00) */ "nop ",
        /*1 (0x01) */ "aconst_null ",
        /*2 (0x02) */ "iconst_m1 ",
        /*3 (0x03) */ "iconst_0 ",
        /*4 (0x04) */ "iconst_1 ",
        /*5 (0x05) */ "iconst_2 ",
        /*6 (0x06) */ "iconst_3 ",
        /*7 (0x07) */ "iconst_4 ",
        /*8 (0x08) */ "iconst_5 ",
        /*9 (0x09) */ "lconst_0 ",
        /*10 (0x0a) */ "lconst_1 ",
        /*11 (0x0b) */ "fconst_0 ",
        /*12 (0x0c) */ "fconst_1 ",
        /*13 (0x0d) */ "fconst_2 ",
        /*14 (0x0e) */ "dconst_0 ",
        /*15 (0x0f) */ "dconst_1 ",
        /*16 (0x10) */ "bipush ",
        /*17 (0x11) */ "sipush ",
        /*18 (0x12) */ "ldc ",
        /*19 (0x13) */ "ldc_w ",
        /*20 (0x14) */ "ldc2_w ",
        /*21 (0x15) */ "iload ",
        /*22 (0x16) */ "lload ",
        /*23 (0x17) */ "fload ",
        /*24 (0x18) */ "dload ",
        /*25 (0x19) */ "aload ",
        /*26 (0x1a) */ "iload_0 ",
        /*27 (0x1b) */ "iload_1 ",
        /*28 (0x1c) */ "iload_2 ",
        /*29 (0x1d) */ "iload_3 ",
        /*30 (0x1e) */ "lload_0 ",
        /*31 (0x1f) */ "lload_1 ",
        /*32 (0x20) */ "lload_2 ",
        /*33 (0x21) */ "lload_3 ",
        /*34 (0x22) */ "fload_0 ",
        /*35 (0x23) */ "fload_1 ",
        /*36 (0x24) */ "fload_2 ",
        /*37 (0x25) */ "fload_3 ",
        /*38 (0x26) */ "dload_0 ",
        /*39 (0x27) */ "dload_1 ",
        /*40 (0x28) */ "dload_2 ",
        /*41 (0x29) */ "dload_3 ",
        /*42 (0x2a) */ "aload_0 ",
        /*43 (0x2b) */ "aload_1 ",
        /*44 (0x2c) */ "aload_2 ",
        /*45 (0x2d) */ "aload_3 ",
        /*46 (0x2e) */ "iaload ",
        /*47 (0x2f) */ "laload ",
        /*48 (0x30) */ "faload ",
        /*49 (0x31) */ "daload ",
        /*50 (0x32) */ "aaload ",
        /*51 (0x33) */ "baload ",
        /*52 (0x34) */ "caload ",
        /*53 (0x35) */ "saload ",
        /*54 (0x36) */ "istore ",
        /*55 (0x37) */ "lstore ",
        /*56 (0x38) */ "fstore ",
        /*57 (0x39) */ "dstore ",
        /*58 (0x3a) */ "astore ",
        /*59 (0x3b) */ "istore_0 ",
        /*60 (0x3c) */ "istore_1 ",
        /*61 (0x3d) */ "istore_2 ",
        /*62 (0x3e) */ "istore_3 ",
        /*63 (0x3f) */ "lstore_0 ",
        /*64 (0x40) */ "lstore_1 ",
        /*65 (0x41) */ "lstore_2 ",
        /*66 (0x42) */ "lstore_3 ",
        /*67 (0x43) */ "fstore_0 ",
        /*68 (0x44) */ "fstore_1 ",
        /*69 (0x45) */ "fstore_2 ",
        /*70 (0x46) */ "fstore_3 ",
        /*71 (0x47) */ "dstore_0 ",
        /*72 (0x48) */ "dstore_1 ",
        /*73 (0x49) */ "dstore_2 ",
        /*74 (0x4a) */ "dstore_3 ",
        /*75 (0x4b) */ "astore_0 ",
        /*76 (0x4c) */ "astore_1 ",
        /*77 (0x4d) */ "astore_2 ",
        /*78 (0x4e) */ "astore_3 ",
        /*79 (0x4f) */ "iastore ",
        /*80 (0x50) */ "lastore ",
        /*81 (0x51) */ "fastore ",
        /*82 (0x52) */ "dastore ",
        /*83 (0x53) */ "aastore ",
        /*84 (0x54) */ "bastore ",
        /*85 (0x55) */ "castore ",
        /*86 (0x56) */ "sastore ",
        /*87 (0x57) */ "pop ",
        /*88 (0x58) */ "pop2 ",
        /*89 (0x59) */ "dup ",
        /*90 (0x5a) */ "dup_x1 ",
        /*91 (0x5b) */ "dup_x2 ",
        /*92 (0x5c) */ "dup2 ",
        /*93 (0x5d) */ "dup2_x1 ",
        /*94 (0x5e) */ "dup2_x2 ",
        /*95 (0x5f) */ "swap ",
        /*96 (0x60) */ "iadd ",
        /*97 (0x61) */ "ladd ",
        /*98 (0x62) */ "fadd ",
        /*99 (0x63) */ "dadd ",
        /*100 (0x64) */ "isub ",
        /*101 (0x65) */ "lsub ",
        /*102 (0x66) */ "fsub ",
        /*103 (0x67) */ "dsub ",
        /*104 (0x68) */ "imul ",
        /*105 (0x69) */ "lmul ",
        /*106 (0x6a) */ "fmul ",
        /*107 (0x6b) */ "dmul ",
        /*108 (0x6c) */ "idiv ",
        /*109 (0x6d) */ "ldiv ",
        /*100 (0x6e) */ "fdiv ",
        /*111 (0x6f) */ "ddiv ",
        /*112 (0x70) */ "irem ",
        /*113 (0x71) */ "lrem ",
        /*114 (0x72) */ "frem ",
        /*115 (0x73) */ "drem ",
        /*116 (0x74) */ "ineg ",
        /*117 (0x75) */ "lneg ",
        /*118 (0x76) */ "fneg ",
        /*119 (0x77) */ "dneg ",
        /*120 (0x78) */ "ishl ",
        /*121 (0x79) */ "lshl ",
        /*122 (0x7a) */ "ishr ",
        /*123 (0x7b) */ "lshr ",
        /*124 (0x7c) */ "iushr ",
        /*125 (0x7d) */ "lushr ",
        /*126 (0x7e) */ "iand ",
        /*127 (0x7f) */ "land ",
        /*128 (0x80) */ "ior ",
        /*129 (0x81) */ "lor ",
        /*130 (0x82) */ "ixor ",
        /*131 (0x83) */ "lxor ",
        /*132 (0x84) */ "iinc ",
        /*133 (0x85) */ "i2l ",
        /*134 (0x86) */ "i2f ",
        /*135 (0x87) */ "i2d ",
        /*136 (0x88) */ "l2i ",
        /*137 (0x89) */ "l2f ",
        /*138 (0x8a) */ "l2d ",
        /*139 (0x8b) */ "f2i ",
        /*140 (0x8c) */ "f2l ",
        /*141 (0x8d) */ "f2d ",
        /*142 (0x8e) */ "d2i ",
        /*143 (0x8f) */ "d2l ",
        /*144 (0x90) */ "d2f ",
        /*145 (0x91) */ "i2b ",
        /*146 (0x92) */ "i2c ",
        /*147 (0x93) */ "i2s ",
        /*148 (0x94) */ "lcmp ",
        /*149 (0x95) */ "fcmpl ",
        /*150 (0x96) */ "fcmpg ",
        /*151 (0x97) */ "dcmpl ",
        /*152 (0x98) */ "dcmpg ",
        /*153 (0x99) */ "ifeq ",
        /*154 (0x9a) */ "ifne ",
        /*155 (0x9b) */ "iflt ",
        /*156 (0x9c) */ "ifge ",
        /*157 (0x9d) */ "ifgt ",
        /*158 (0x9e) */ "ifle ",
        /*159 (0x9f) */ "if_icmpeq ",
        /*160 (0xa0) */ "if_icmpne ",
        /*161 (0xa1) */ "if_icmplt ",
        /*162 (0xa2) */ "if_icmpge ",
        /*163 (0xa3) */ "if_icmpgt ",
        /*164 (0xa4) */ "if_icmple ",
        /*165 (0xa5) */ "if_acmpeq ",
        /*166 (0xa6) */ "if_acmpne ",
        /*167 (0xa7) */ "goto ",
        /*168 (0xa8) */ "jsr ",
        /*169 (0xa9) */ "ret ",
        /*170 (0xaa) */ "tableswitch ",
        /*171 (0xab) */ "lookupswitch ",
        /*172 (0xac) */ "ireturn ",
        /*173 (0xad) */ "lreturn ",
        /*174 (0xae) */ "freturn ",
        /*175 (0xaf) */ "dreturn ",
        /*176 (0xb0) */ "areturn ",
        /*177 (0xb1) */ "return ",
        /*178 (0xb2) */ "getstatic ",
        /*179 (0xb3) */ "putstatic ",
        /*180 (0xb4) */ "getfield ",
        /*181 (0xb5) */ "putfield ",
        /*182 (0xb6) */ "invokevirtual ",
        /*183 (0xb7) */ "invokespecial ",
        /*184 (0xb8) */ "invokestatic ",
        /*185 (0xb9) */ "invokeinterface ",
        /*186 (0xba) */ "xxxunusedxxx ",
        /*187 (0xbb) */ "new ",
        /*188 (0xbc) */ "newarray ",
        /*189 (0xbd) */ "anewarray ",
        /*190 (0xbe) */ "arraylength ",
        /*191 (0xbf) */ "athrow ",
        /*192 (0xc0) */ "checkcast ",
        /*193 (0xc1) */ "instanceof ",
        /*194 (0xc2) */ "monitorenter ",
        /*195 (0xc3) */ "monitorexit ",
        /*196 (0xc4) */ "wide ",
        /*197 (0xc5) */ "multianewarray ",
        /*198 (0xc6) */ "ifnull ",
        /*199 (0xc7) */ "ifnonnull ",
        /*200 (0xc8) */ "goto_w ",
        /*201 (0xc9) */ "jsr_w ",

        //_quick opcodes:
        /*202 (0xca) */ "breakpoint ",

        /*203 (0xcb) */ "ldc_quick ",
        /*204 (0xcc) */ "ldc_w_quick ",
        /*205 (0xcd) */ "ldc2_w_quick ",
        /*206 (0xce) */ "getfield_quick ",
        /*207 (0xcf) */ "putfield_quick ",
        /*208 (0xd0) */ "getfield2_quick ",
        /*209 (0xd1) */ "putfield2_quick ",
        /*210 (0xd2) */ "getstatic_quick ",
        /*211 (0xd3) */ "putstatic_quick ",
        /*212 (0xd4) */ "getstatic2_quick ",
        /*213 (0xd5) */ "putstatic2_quick ",
        /*214 (0xd6) */ "invokevirtual_quick ",
        /*215 (0xd7) */ "invokenonvirtual_quick ",
        /*216 (0xd8) */ "invokesuper_quick ",
        /*217 (0xd9) */ "invokestatic_quick ",
        /*218 (0xda) */ "invokeinterface_quick ",
        /*219 (0xdb) */ "invokevirtualobject_quick ",
        /*221 (0xdd) */ "new_quick ",
        /*222 (0xde) */ "anewarray_quick ",
        /*223 (0xdf) */ "multianewarray_quick ",
        /*224 (0xe0) */ "checkcast_quick ",
        /*225 (0xe1) */ "instanceof_quick ",
        /*226 (0xe2) */ "invokevirtual_quick_w ",
        /*227 (0xe3) */ "getfield_quick_w ",
        /*228 (0xe4) */ "putfield_quick_w ",

        //Reserved opcodes:
        /*229 (0xe4) */ "", "", "",
        /*232 (0xe4) */ "", "", "",
        /*235 (0xe4) */ "", "", "",
        /*238 (0xe4) */ "", "", "",
        /*241 (0xe4) */ "", "", "",
        /*243 (0xe4) */ "", "", "",
        /*246 (0xe4) */ "", "", "",
        /*249 (0xe4) */ "", "", "",
		/*252 */"",
		/*253 */"",
        /*254 (0xfe) */ "impdep1 ",
        /*255 (0xff) */ "impdep2 "
        };
}
