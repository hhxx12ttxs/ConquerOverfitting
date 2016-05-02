/*
 *  Copyright (C) 2001 David Hoag
 *  ObjectWave Corporation
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  For a full copy of the license see:
 *  http://www.opensource.org/licenses/lgpl-license.html
 */
package com.objectwave.persist.file;
import com.objectwave.logging.MessageLog;
import com.objectwave.persist.*;
import com.objectwave.persist.objectConstruction.ProcessResultSet;
import java.io.*;
/**
 *  Build the DomainObjects from the byte array from the file.
 *
 * @author  Dave Hoag
 * @version  $Id: ObjectBuilder.java,v 2.2 2001/11/02 16:07:56 dave_hoag Exp $
 */
public class ObjectBuilder
{
	/**
	 */
	public static ObjectFormatter defaultFormatter;
	static boolean verbose = System.getProperty("ow.persistVerbose", null) != null;
	static ObjectBuilder defaultInstance;
	final static String MY_NULL = "MyNullValueAndPlaceHolder";
	ProcessResultSet resultEngine;
	/**
	 */
	public ObjectBuilder()
	{
		resultEngine = ProcessResultSet.getInstance("RDB");
	}
	/**
	 *  Gets the Instance attribute of the ObjectBuilder class
	 *
	 * @return  The Instance value
	 */
	public static synchronized ObjectBuilder getInstance()
	{
		if(defaultInstance == null)
		{
			defaultInstance = new ObjectBuilder();
		}
		return defaultInstance;
	}
	/**
	 *  A utility method that simplifies code.
	 *  Users of this broker are expected to use an RDBPersistence implementation.
	 *
	 * @param  object
	 * @return  The RDBAdapter value
	 */
	protected final static RDBPersistence getRDBAdapter(final Persistence object)
	{
		if(object.usesAdapter())
		{
			return (RDBPersistence) object.getAdapter();
		}
		else
		{
			return (RDBPersistence) object;
		}
	}
	/**
	 * @param  pkeyData The new PrimaryKeyValues value
	 * @param  result The new PrimaryKeyValues value
	 * @param  pResult The new PrimaryKeyValues value
	 */
	protected void setPrimaryKeyValues(final Object pkeyData, Persistence result, RDBPersistence pResult)
	{
		AttributeTypeColumn[] pkAtc = pResult.getPrimaryKeyDescriptions();
		if(pkAtc.length == 1)
		{
			Object pkeyValue = null;
			if(Persistence.class.isAssignableFrom(pkAtc[0].getField().getType()))
			{
				//No need to set it, return
				return;
			}
			else
			{
				pkeyValue = defaultFormatter.convertType(pkAtc[0], pkeyData);
				pkAtc[0].setValue(result, pkeyValue);
			}
			pResult.setPrimaryKeyField(pkeyValue);
		}
		else
		{
			java.util.StringTokenizer st = new java.util.StringTokenizer(String.valueOf(pkeyData), "|");
			for(int i = 0; i < pkAtc.length; ++i)
			{
				Object pkeyValue = defaultFormatter.convertType(pkAtc[i], st.nextToken());
				pkAtc[i].setValue(result, pkeyValue);
			}
		}

	}
	/**
	 *  Convert the data in the object to a byte [] of data.
	 *
	 * @param  pObj
	 * @return  The byte array that is the data found in the provided object
	 */
	public byte[] getSaveData(final RDBPersistence pObj)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		AttributeTypeColumn[] cols = pObj.getAttributeDescriptions();
		Persistence obj = pObj.getPersistentObject();
		try
		{
			for(int i = 0; i < cols.length; ++i)
			{
				if(cols[i].getField().getType().isPrimitive())
				{
					writePrimitive(dos, cols[i], obj);
				}
				else
						if(cols[i].getField().getType().getName().equals("[B"))
				{
					byte[] byteArray = (byte[]) cols[i].getValue(obj);

					dos.writeInt(byteArray.length);
					dos.write(byteArray);
				}
				else
				{
					dos.writeUTF(defaultFormatter.formatValue(cols[i].getValue(obj)));
				}
			}
			/*
			 *  cols = pObj.getPrimaryKeyDescriptions();
			 *  for(int i = 0; i < cols.length; ++i)
			 *  {
			 *  if(cols[i].getField().getType().isPrimitive())
			 *  {
			 *  writePrimitive(dos, cols[i], obj);
			 *  }
			 *  else
			 *  if(cols[i].getField().getType().getName().equals("[B"))
			 *  {
			 *  byte[] byteArray = (byte[]) cols[i].getValue(obj);
			 *  dos.writeInt(byteArray.length);
			 *  dos.write(byteArray);
			 *  }
			 *  else
			 *  {
			 *  dos.writeUTF(defaultFormatter.formatValue(cols[i].getValue(obj)));
			 *  }
			 *  }
			 */
			cols = pObj.getForeignKeyDescriptions();
			for(int i = 0; i < cols.length; ++i)
			{
				RDBPersistence reference = null;
				Persistence fkData = (Persistence) cols[i].getValue(obj);
				if(fkData != null)
				{
					reference = getRDBAdapter(fkData);
				}
				writeReference(dos, reference, pObj, fkData, cols[i]);
			}
			cols = pObj.getInstanceLinkDescriptions();
			for(int i = 0; i < cols.length; i++)
			{
				//Get a list of those with column names

				RDBPersistence reference = null;
				if(cols[i].getColumnName() != null)
				{
					Persistence fkData = (Persistence) cols[i].getValue(obj);
					if(fkData != null)
					{
						reference = getRDBAdapter(fkData);
					}
					writeReference(dos, reference, pObj, fkData, cols[i]);
				}
			}
			dos.flush();
		}
		catch(IOException ex)
		{
			//This should NEVER happen
			System.err.println(ex);
			ex.printStackTrace();
		}
		return bos.toByteArray();
	}
	/**
	 * @param  one
	 * @param  two
	 * @return  The Match value
	 */
	protected boolean isMatch(RDBPersistence one, RDBPersistence two)
	{
		if(one == null || two == null)
		{
			return false;
		}
		Object[] oneKeys = one.getPrimaryKeyFields();
		Object[] twoKeys = two.getPrimaryKeyFields();
		if(oneKeys.length != twoKeys.length)
		{
			return false;
		}
		for(int i = 0; i < oneKeys.length; i++)
		{

			if(oneKeys[i] == null || twoKeys[i] == null || (!oneKeys[i].equals(twoKeys[i])))
			{
				return false;
			}
		}
		return true;
	}
	/**
	 *  Build the Object from the byte []
	 *
	 * @param  queryObject
	 * @param  data
	 * @param  pkeyData
	 * @return
	 * @exception  InstantiationException
	 * @exception  IllegalAccessException
	 */
	public Persistence buildObject(Persistence queryObject, byte[] data, Object pkeyData) throws InstantiationException, IllegalAccessException
	{

		final RDBPersistence pObj = getRDBAdapter(queryObject);
		final Persistence result = pObj.getInstance(null, null);
		final RDBPersistence pResult = getRDBAdapter(result);

		MessageLog.debug(this, "Building a " + result + " pkeyData " + pkeyData);
		setPrimaryKeyValues(pkeyData, result, pResult);

		final ByteArrayInputStream bos = new ByteArrayInputStream(data);
		final DataInputStream dos = new DataInputStream(bos);
		AttributeTypeColumn[] cols = pObj.getAttributeDescriptions();

		try
		{
			for(int i = 0; i < cols.length; ++i)
			{
				if(cols[i].getField().getType().isPrimitive())
				{
					readPrimitive(dos, cols[i], result);
				}
				else
						if(cols[i].getField().getType().getName().equals("[B"))
				{
					if(data.length > 0)
					{
						int arrayLength = dos.readInt();
						byte[] byteArray = new byte[arrayLength];
						for(int j = 0; j < arrayLength; j++)
						{
							byteArray[j] = dos.readByte();
						}
						cols[i].setValue(result, byteArray);
					}
				}
				else
				{
					final String value = dos.readUTF();
					cols[i].setValue(result, defaultFormatter.convertType(cols[i], value));
				}
			}
			/*
			 *  cols = pObj.getPrimaryKeyDescriptions();
			 *  for(int i = 0; i < cols.length; ++i)
			 *  {
			 *  if(cols[i].getField().getType().isPrimitive())
			 *  {
			 *  readPrimitive(dos, cols[i], result);
			 *  }
			 *  else
			 *  if(cols[i].getField().getType().getName().equals("[B"))
			 *  {
			 *  if(data.length > 0)
			 *  {
			 *  int arrayLength = dos.readInt();
			 *  byte[] byteArray = new byte[arrayLength];
			 *  for(int j = 0; j < arrayLength; j++)
			 *  {
			 *  byteArray[j] = dos.readByte();
			 *  }
			 *  cols[i].setValue(result, byteArray);
			 *  }
			 *  }
			 *  else
			 *  {
			 *  final String value = dos.readUTF();
			 *  cols[i].setValue(result, defaultFormatter.convertType(cols[i], value));
			 *  }
			 *  }
			 */
			cols = pObj.getForeignKeyDescriptions();
			for(int i = 0; i < cols.length; ++i)
			{
				readReference(dos.readUTF(), cols[i], result, pResult, queryObject);
			}

			cols = pObj.getInstanceLinkDescriptions();
			for(int i = 0; i < cols.length; i++)
			{
				//Get a list of those with column names

				if(cols[i].getColumnName() != null)
				{
					readReference(dos.readUTF(), cols[i], result, pObj, queryObject);
				}
				else
				{
					/*
					 *  We have to build the proxy. Not sure if the instance link is valid at this time
					 */
					Persistence instanceLinkData = null;
					Persistence queryInstanceValue = (Persistence) cols[i].getValue(queryObject);
					if(queryInstanceValue != null)
					{
						RDBPersistence pQueryInstanceValue = getRDBAdapter(queryInstanceValue);
						if(pQueryInstanceValue.isRetrievedFromDatabase())
						{
							AttributeTypeColumn linkCol = pQueryInstanceValue.instanceLinkJoinColumn(pResult);
							Persistence linkData = (Persistence) linkCol.getValue(queryInstanceValue);
							//If queryInstance fk points to result, then set him as my instance link val
							if(isMatch(getRDBAdapter(linkData), pResult))
							{
								instanceLinkData = queryInstanceValue;
							}
						}
					}
					if(instanceLinkData == null)
					{
						Object primaryKey = null;
						RDBPersistence refObject = null;
						instanceLinkData = resultEngine.buildProxy(cols[i], primaryKey, pResult, refObject);
					}
					cols[i].setValue(result, instanceLinkData);
				}
			}
			resultEngine.buildCollectionProxies(result, null, pObj, null);
		}
		catch(IOException ex)
		{
			//This should NEVER happen
			System.err.println(ex);
			ex.printStackTrace();
		}
		return result;
	}
	/**
	 *  Utility method that does the work of other methods.
	 *  Get the actual databaseIdentifier that refers to the other Persistent object.
	 *  Modifies the Object array passed as fkData to contain the databaseIdentifiers and not the acutal Persistent object.
	 *
	 * @param  col
	 * @param  obj
	 * @param  pObj
	 * @param  queryObject
	 * @param  pkey
	 * @exception  IOException
	 * @exception  InstantiationException
	 * @exception  IllegalAccessException
	 */
	protected void readReference(String pkey, final AttributeTypeColumn col, final Persistence obj, final RDBPersistence pObj, Persistence queryObject) throws IOException, InstantiationException, IllegalAccessException
	{
		Object joinData = null;
		Persistence fkData = null;
		if(!pkey.equals(MY_NULL))
		{
			//Not a NULL value
			Persistence queryInstanceValue = (Persistence) col.getValue(queryObject);

			if(queryInstanceValue != null)
			{
				RDBPersistence pQueryInstanceValue = getRDBAdapter(queryInstanceValue);
				if(pQueryInstanceValue.isRetrievedFromDatabase())
				{
					AttributeTypeColumn pColumn = pQueryInstanceValue.getPrimaryAttributeDescription();
					Object pkeyValue = defaultFormatter.convertType(pColumn, pkey);
					if(pkeyValue.equals(pQueryInstanceValue.getPrimaryKeyField()))
					{
						fkData = queryInstanceValue;
						//The query object data is the same as the proxy that would be created
					}
				}
			}
			if(fkData == null)
			{
				java.util.StringTokenizer st = new java.util.StringTokenizer(pkey, "|");
				java.util.ArrayList list = new java.util.ArrayList();
				while(st.hasMoreTokens())
				{
					String token = st.nextToken();
					list.add(token);
				}
				fkData = resultEngine.buildFkProxy(col, list.toArray(), obj, pObj, null);
			}
		}
		col.setValue(obj, fkData);
	}
	/**
	 *  Utility method that does the work of other methods.
	 *  Get the actual databaseIdentifier that refers to the other Persistent object.
	 *  Modifies the Object array passed as fkData to contain the databaseIdentifiers and not the acutal Persistent object.
	 *
	 * @param  dos
	 * @param  pFkData
	 * @param  pObj
	 * @param  fkData
	 * @param  fkCol
	 * @exception  IOException
	 */
	protected void writeReference(final DataOutputStream dos, RDBPersistence pFkData, final RDBPersistence pObj, final Persistence fkData, final AttributeTypeColumn fkCol) throws IOException
	{
		StringBuffer sb = new StringBuffer();
		if(pFkData != null)
		{
			if(pFkData.isRetrievedFromDatabase() || pFkData.isProxy())
			{
				//AttributeTypeColumn joinColumn = pFkData.foreignKeyJoinColumn(pObj, fkCol.getJoinOn());
				//joinData = joinColumn.getValue(fkData);
				JoinField[] joinFields = fkCol.getJoinFields();
				for(int i = 0; i < joinFields.length; i++)
				{
					JoinField joinField = joinFields[i];
					AttributeTypeColumn joinColumn = pFkData.foreignKeyJoinColumn(pObj, joinField.getJoinField());
					Object field = joinColumn.getValue(fkData);
					sb.append(field);
					//if has more values, add a separator char
					if(i < (joinFields.length - 1))
					{
						sb.append('|');
					}
				}
			}
		}
		if(sb.length() == 0)
		{
			dos.writeUTF(MY_NULL);
		}
		else
		{
			dos.writeUTF(sb.toString());
		}
	}
	/**
	 *  Write the primitive value to the output stream.
	 *
	 * @param  dos
	 * @param  col
	 * @param  obj
	 * @exception  IOException
	 */
	protected void writePrimitive(final DataOutputStream dos, final AttributeTypeColumn col, final Persistence obj) throws IOException
	{
		final Class c = col.getField().getType();
		//Once the setAccessible support is added, this can be optimized
		if(byte.class == c)
		{
			dos.writeByte(((Byte) col.getValue(obj)).byteValue());
		}
//            dos.writeByte(col.getField().getByte(obj));
		else
				if(short.class == c)
		{
			dos.writeShort(((Short) col.getValue(obj)).shortValue());
		}
//            dos.writeShort(col.getField().getShort(obj));
		else
				if(int.class == c)
		{
			dos.writeInt(((Integer) col.getValue(obj)).intValue());
		}
//            dos.writeInt(col.getField().getInt(obj));
		else
				if(long.class == c)
		{
			dos.writeLong(((Long) col.getValue(obj)).longValue());
		}
//            dos.writeLong(col.getField().getLong(obj));
		else
				if(float.class == c)
		{
			dos.writeFloat(((Float) col.getValue(obj)).floatValue());
		}
//            dos.writeFloat(col.getField().getFloat(obj));
		else
				if(double.class == c)
		{
			dos.writeDouble(((Double) col.getValue(obj)).doubleValue());
		}
//            dos.writeDouble(col.getField().getDouble(obj));
		else
				if(char.class == c)
		{
			dos.writeChar(((Character) col.getValue(obj)).charValue());
		}
//            dos.writeChar(col.getField().getChar(obj));
		else
				if(boolean.class == c)
		{
			dos.writeBoolean(((Boolean) col.getValue(obj)).booleanValue());
		}
//            dos.writeBoolean(col.getField().getBoolean(obj));
	}
	/**
	 *  Read the primitive values from the input stream.
	 *
	 * @param  dos
	 * @param  col
	 * @param  obj
	 * @exception  IOException
	 */
	protected void readPrimitive(final DataInputStream dos, final AttributeTypeColumn col, final Persistence obj) throws IOException
	{
		final Class c = col.getField().getType();
		//Once the setAccessible support is added, this can be optimized
		if(byte.class == c)
		{
			byte res = dos.readByte();
			col.setValue(obj, new Byte(res));
		}
		else
				if(short.class == c)
		{
			short res = dos.readShort();
			col.setValue(obj, new Short(res));
		}
		else
				if(int.class == c)
		{
			int res = dos.readInt();
			col.setValue(obj, new Integer(res));
		}
		else
				if(long.class == c)
		{
			long res = dos.readLong();
			col.setValue(obj, new Long(res));
		}
		else
				if(float.class == c)
		{
			float res = dos.readFloat();
			col.setValue(obj, new Float(res));
		}
		else
				if(double.class == c)
		{
			double res = dos.readDouble();
			col.setValue(obj, new Double(res));
		}
		else
				if(char.class == c)
		{
			char res = dos.readChar();
			col.setValue(obj, new Character(res));
		}
		else
				if(boolean.class == c)
		{
			boolean res = dos.readBoolean();
			col.setValue(obj, new Boolean(res));
		}
	}
	static
	{
		try
		{
			defaultFormatter = new FileObjectFormatter();
		}
		catch(Throwable t)
		{
			System.err.println("Exception loading ObjectBuilder class " + t);
			t.printStackTrace();
		}
	}
}

