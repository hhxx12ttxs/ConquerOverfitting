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
package com.objectwave.persist.broker;
import com.objectwave.logging.MessageLog;
import com.objectwave.persist.AttributeTypeColumn;
import com.objectwave.persist.Persistence;
import com.objectwave.persist.QueryException;
import com.objectwave.persist.RDBPersistence;
import java.util.ArrayList;
import java.util.List;
/**
 *  Figure out the order that the persistent objects should be saved.
 *
 * @author  dhoag
 * @version  $Id: SaveObjectsStrategy.java,v 1.1 2001/07/05 13:22:55 dave_hoag Exp $
 */
public class SaveObjectsStrategy
{
	/**
	 *  Constructor for the SaveObjectsStrategy object
	 */
	public SaveObjectsStrategy()
	{
	}
	/**
	 *  A utility method that simplifies code.
	 *
	 * @param  object
	 * @return  The RDBAdapter value
	 */
	public final RDBPersistence getRDBAdapter(final Persistence object)
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
	 *  Save all of the objects in the objs collection. This will attempt to save
	 *  objects in an order that makes sense to a relational database.
	 *
	 * @param  objs The peristent objects to save.
	 * @exception  QueryException
	 */
	public void saveObjects(final ArrayList objs) throws QueryException
	{
		final ArrayList undoList = new ArrayList(objs.size());

		int depth = 0;
		try
		{
			depth = saveObjects(objs, undoList, 0, 1);
		}
		catch(QueryException e)
		{
			clearIsRetrievedFromDatabase(undoList);
			throw e;
		}
		catch(RuntimeException exception)
		{
			clearIsRetrievedFromDatabase(undoList);
			throw exception;
		}
		MessageLog.debug(this, " saveObjects recursive depth of " + depth + " " + Thread.currentThread());
	}
	/**
	 *  Do an identity compare to determine if the List contains the object.
	 *  Created because most Lists do an 'equals' comparison, not an identity
	 *  compare.
	 *
	 * @param  obj java.lang.Object The object for which we are looking
	 * @param  aList The list being searched.
	 * @return  boolean true if the object is in the provided list.
	 * @author  Dave Hoag
	 */
	public boolean contains(final Object obj, final List aList)
	{
		int size = aList.size();

		for(int i = 0; i < size; ++i)
		{
			if(obj == aList.get(i))
			{
				return true;
			}
		}
		return false;
	}
	/**
	 *  We may have several objects to save. This attempts to save them in some
	 *  'order' that makes sense for a relational database.
	 *
	 * @param  objs The peristent objects to save. This list should shrink with
	 *      every recursive call
	 * @param  undoList Those new objects that have been inserted into the database
	 *      will be added to this list.
	 * @param  sizeOfLaterList
	 * @param  depth
	 * @return
	 * @exception  QueryException
	 * @see  #save
	 * @author  Dave Hoag
	 */
	protected int saveObjects(final List objs, final ArrayList undoList, final int sizeOfLaterList, final int depth) throws QueryException
	{
		final int origSize = objs.size();

		ArrayList laterList;
		if(sizeOfLaterList == 0)
		{
			laterList = new ArrayList(origSize);
		}
		else
		{
			laterList = new ArrayList(sizeOfLaterList);
		}

		for(int j = 0; j < origSize; j++)
		{
			final Persistence obj = (Persistence) objs.get(j);
			final RDBPersistence pObj = getRDBAdapter(obj);

			final AttributeTypeColumn[] cols = pObj.getForeignKeyTypes(null);

			if((cols.length == 0) || (!unsavedForeignKeys(cols, obj)))
			{
				if(!obj.isRetrievedFromDatabase())
				{
					undoList.add(obj);
				}
				obj.save();

				if(needsToBeSavedAgain(obj, pObj, objs, laterList))
				{
					laterList.add(obj);
				}
			}
			else
			{
// I do not think I really care if it's already in the list. It shouldn't be, and no biggie if it is
//			if(! contains(obj, laterList))

				laterList.add(obj);
			}
		}
		//End for j loop
		return finishSaving(laterList, undoList, sizeOfLaterList, depth);
	}
	/**
	 *  Determines if in the collection of persistent objects if there exists an
	 *  object that is neither transient nor 'saved'/persistent.
	 *
	 * @param  fkColumns
	 * @param  sourceObj
	 * @return  boolean true if there are unsaved foreign keys in the list of data.
	 */
	protected boolean unsavedForeignKeys(final AttributeTypeColumn[] fkColumns, final Persistence sourceObj)
	{
		for(int i = 0; i < fkColumns.length; i++)
		{
			Persistence fkData = (Persistence) fkColumns[i].getValue(sourceObj);
			if(fkData != null)
			{
				if(!(fkData.isTransient() || fkData.isRetrievedFromDatabase()))
				{
					return true;
				}
			}
		}
		return false;
	}
	/**
	 *  Finish up the work necessary to 'saveObjects'. This may result in a
	 *  recursive call back to saveObjects.
	 *
	 * @param  laterList
	 * @param  undoList
	 * @param  originalSize
	 * @param  depth
	 * @return
	 * @exception  QueryException
	 */
	private final int finishSaving(final ArrayList laterList, final ArrayList undoList, final int originalSize, final int depth) throws QueryException
	{
		int newSize = laterList.size();
		if(newSize == originalSize)
		{
			//The later list did not change! Just save objects in a random order

			for(int i = 0; i < newSize; ++i)
			{
				Persistence obj = (Persistence) laterList.get(i);
				if(!obj.isRetrievedFromDatabase())
				{
					undoList.add(obj);
				}
				obj.save();
			}
		}
		else
				if(newSize > 0)
		{
			//There is more to save. Recurse and save the remaining objects.

			return saveObjects(laterList, undoList, newSize, depth + 1);
		}
		return depth;
	}
	/**
	 *  Check all of the instance link values in the obj parameter. If any instance
	 *  link value is declared to have a column name, and that instance link value
	 *  has not been saved to the database, then I'll need to be saved a second
	 *  time.
	 *
	 * @param  obj Persistence The persistent object that may need to be saved
	 *      twice.
	 * @param  originalSaveList The list of objects that were sent to this broker
	 *      to save.
	 * @param  pObj
	 * @param  laterList
	 * @return  true if the obj parameter to be added to the laterList.
	 */
	private final boolean needsToBeSavedAgain(final Persistence obj, final RDBPersistence pObj, final List originalSaveList, final ArrayList laterList)
	{
		final AttributeTypeColumn[] cols = pObj.getInstanceLinkTypes(null);

		for(int i = 0; i < cols.length; i++)
		{
			Persistence instanceLinkData = (Persistence) cols[i].getValue(obj);
			if((cols[i].getColumnName() != null) &&
			//If I have a column name.
					(instanceLinkData != null) &&
					(!instanceLinkData.isRetrievedFromDatabase()) &&
			//And the data for that column is not saved
					(!instanceLinkData.isTransient()) &&
					(contains(instanceLinkData, originalSaveList)))
			{
				//Our list of objects will eventually save this instance link object

				if(!contains(obj, laterList))
				{
					//If this is not already in the later list, be sure to save it again
					return true;
				}
			}
		}
		return false;
	}
	/**
	 *  When we save a new object in the database, we mark it as retreived from the
	 *  database. When we are saving multiple objects within a transaction, some
	 *  objects will be marked before others. If an object fails to save during the
	 *  transaction, the whole transaction is aborted. The isRetrievedFromDatabase
	 *  flag must be restored to false for those objects which have already been
	 *  marked to be true.
	 *
	 * @param  undoList List of those objects that have been marked as retreived
	 *      that were initialy not retrieved
	 */
	private final void clearIsRetrievedFromDatabase(final ArrayList undoList)
	{
		MessageLog.warn(this, "SaveObjects failed with an exception. Setting isRetrievedFromDatabase to false.:" + Thread.currentThread());
		int size = undoList.size();
		for(int i = 0; i < size; i++)
		{
			((Persistence) undoList.get(i)).setRetrievedFromDatabase(false);
		}
	}
}

