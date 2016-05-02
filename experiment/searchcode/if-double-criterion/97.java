package nl.spikey.orm.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nl.spikey.orm.Configuration;
import nl.spikey.orm.IdObject;
import nl.spikey.orm.Session;
import nl.spikey.orm.annotations.Column;
import nl.spikey.orm.annotations.Entity;
import nl.spikey.orm.annotations.Id;
import nl.spikey.orm.annotations.Index;
import nl.spikey.orm.annotations.MappedSuperclass;
import nl.spikey.orm.criteria.Criteria;
import nl.spikey.orm.criteria.Criterion;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class Orm
{
	private static final String DTYPE = "dtype";

	private static final String NULL = "null";

	private Configuration configuration;

	public Orm(Configuration configuration)
	{
		this.configuration = configuration;
	}

	public Cursor selectQuery(Criteria criteria)
	{
		return selectQuery(criteria, null); // is all columns
	}

	public Cursor selectQuery(Criteria criteria, String[] columns)
	{
		Class< ? extends IdObject> clazz = criteria.getClzz();
		if (!clazz.isAnnotationPresent(Entity.class))
			return null;
		String table = getTableName(clazz);

		String selection = "";
		List<String> selectionArgs = new ArrayList<String>();
		selection = extractWhereClause(criteria, selectionArgs);

		String groupBy = null;
		String having = null;
		String orderBy = criteria.getOrderBy();
		String limit = criteria.getLimitString();

		return getSession().query(table, columns, selection, selectionArgs.toArray(new String[0]),
			groupBy, having, orderBy, limit);
	}

	public int count(Criteria criteria)
	{
		String query = "SELECT count(*) FROM ";
		query += getTableName(criteria.getClzz());

		List<String> selectionArgs = new ArrayList<String>();
		query += " WHERE " + extractWhereClause(criteria, selectionArgs);

		Cursor cursor = getSession().rawQuery(query, selectionArgs.toArray(new String[0]));
		int result = 0;
		if (cursor != null && cursor.moveToFirst())
		{
			result = cursor.getInt(0);
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		getSession().close();
		return result;
	}

	public <T extends IdObject> List<T> list(Criteria criteria)
	{
		Cursor cursor = selectQuery(criteria);

		List<T> resultList = new ArrayList<T>();
		if (cursor != null && cursor.moveToFirst())
		{
			do
			{
				T object = fillObject(cursor);
				resultList.add(object);
			}
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		getSession().close();

		return resultList;
	}

	public <T extends IdObject> T unique(Criteria criteria) throws NotUniqueException
	{
		int count = count(criteria);
		if (count > 1)
			throw new NotUniqueException("Criteria resulted in " + count + " results.");

		Cursor cursor = selectQuery(criteria);

		List<T> resultList = new ArrayList<T>();
		if (cursor != null && cursor.moveToFirst())
		{
			do
			{
				T object = fillObject(cursor);
				resultList.add(object);
			}
			while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed())
			cursor.close();
		getSession().close();

		if (resultList.size() == 1)
			return resultList.get(0);
		return null;
	}

	/**
	 * Drops table with tableName if exists
	 */
	public void dropTable(String tableName)
	{
		String query = "DROP TABLE IF EXISTS " + tableName;
		getSession().execSQL(query);
	}

	/**
	 * Creates tables with as table name the first subclass annotated with @Entity of an @MappedSuperClass
	 * annotated class. Or the first subclass of IdObject if no @MappedSupperClass
	 * annotation is present.
	 * 
	 * It will only include all the columns of the entityClasses and there super classes
	 * all columns of the subclasses of entityClasses will have to be added manually using
	 * the addCollumnFor method.
	 */
	public void createTable(Class< ? extends IdObject> clazz)
	{
		String query = "CREATE TABLE " + getTableName(clazz) + "(" + DTYPE + " TEXT";
		List<Field> fields = new ArrayList<Field>();
		for (Class< ? > clzz : getSubClasses(clazz))
		{
			for (Field field : getTableColumnFields(clzz))
				if (!fields.contains(field))
					fields.add(field);
		}

		for (Field field : fields)
		{
			query += ',';
			query += generateColumnNameAndConstraint(field);
			if (field.isAnnotationPresent(Id.class))
				query += " PRIMARY KEY";
		}
		query += ")";
		getSession().execSQL(query);
	}

	public void createIndex(Class< ? extends IdObject> clazz)
	{
		List<Field> fields = new ArrayList<Field>();
		List<String> indexeQueries = new ArrayList<String>();
		for (Class< ? > clzz : getSubClasses(clazz))
		{
			for (Field field : getTableColumnFields(clzz))
				if (!fields.contains(field))
					fields.add(field);
		}

		for (Field field : fields)
		{
			if (field.isAnnotationPresent(Index.class))
			{
				StringBuilder indexQuery = new StringBuilder("CREATE ");
				if (field.getAnnotation(Index.class).unique())
					indexQuery.append("UNIQUE ");
				indexQuery.append("INDEX ");
				if (field.getAnnotation(Index.class).name() != null
					&& field.getAnnotation(Index.class).name().length() > 0)
					indexQuery.append(field.getAnnotation(Index.class).name());
				else
					indexQuery.append(getTableName(clazz) + "_" + getColumnName(field) + "_index");
				indexQuery.append(" ON " + getTableName(clazz) + "(" + getColumnName(field) + ")");
				indexeQueries.add(indexQuery.toString());
			}
		}
		for (String indexQuery : indexeQueries)
		{
			getSession().execSQL(indexQuery);
		}
	}

	public void dropIndex(Class< ? extends IdObject> clazz)
	{
		List<Field> fields = new ArrayList<Field>();
		List<String> indexeQueries = new ArrayList<String>();
		for (Class< ? > clzz : getSubClasses(clazz))
		{
			for (Field field : getTableColumnFields(clzz))
				if (!fields.contains(field))
					fields.add(field);
		}

		for (Field field : fields)
		{
			if (field.isAnnotationPresent(Index.class))
			{
				StringBuilder indexQuery = new StringBuilder("DROP ");
				indexQuery.append("INDEX IF EXISTS ");
				if (field.getAnnotation(Index.class).name() != null
					&& field.getAnnotation(Index.class).name().length() > 0)
					indexQuery.append(field.getAnnotation(Index.class).name());
				else
					indexQuery.append(getTableName(clazz) + "_" + getColumnName(field) + "_index");
				indexeQueries.add(indexQuery.toString());
			}
		}
		for (String indexQuery : indexeQueries)
		{
			getSession().execSQL(indexQuery);
		}
	}

	public void dropAndRecreateTable(Class< ? extends IdObject> clazz)
	{
		dropTable(getTableName(clazz));
		createTable(clazz);
	}

	private String addColumnForQuery(Class< ? extends IdObject> clazz, String propertyName)
	{
		String query = "ALTER TABLE " + getTableName(clazz) + " ADD COLUMN ";

		for (Field field : getTableColumnFields(clazz))
		{
			if (getColumnName(field).equals(propertyName))
			{
				query += generateColumnNameAndConstraint(field);
			}
		}
		return query;
	}

	public void addColumnFor(Class< ? extends IdObject> clazz, String propertyName,
			String defaultValue)
	{
		String query = addColumnForQuery(clazz, propertyName);
		if (defaultValue != null && !defaultValue.isEmpty())
			query += " DEFAULT (" + defaultValue + ")";

		getSession().execSQL(query);
	}

	public void addColumnFor(Class< ? extends IdObject> clazz, String propertyName)
	{
		getSession().execSQL(addColumnForQuery(clazz, propertyName));
	}

	// LOW: [ORM] misschien nog een rename column?

	/**
	 * This function deletes the object from the database. Keep in mind that objects
	 * referencing to this object will have to be deleted first, although this will not be
	 * checked unless you have enabled foreign keys (default disabled)
	 */
	public <T extends IdObject> boolean deleteObject(T object)
	{
		if (object == null || getSession() == null)
			return false;
		String whereClause = "";
		for (Field field : getTableColumnFields(object.getClass()))
		{
			if (field.isAnnotationPresent(Id.class))
				whereClause = getColumnName(field) + "=?";
		}
		int result =
			getSession().delete(getTableName(object.getClass()), whereClause,
				new String[] {object.getId().toString()});

		return (result == 1);
	}

	public void beginTransaction()
	{
		getSession().beginTransaction();
	}

	public void commit()
	{
		getSession().commit();
	}

	public void rollback()
	{
		getSession().rollback();
	}

	/**
	 * This function will save the object in the database which will then generate a id
	 * for the object.
	 * 
	 * Keep in mind that only this object will be saved if the object have references to
	 * other objects which are not jet saved you will have to save these objects first
	 * otherwise other wise it will result in a ObjectNotSavedException because the
	 * referenced object is not saved (has no id).
	 */
	public <T extends IdObject> boolean saveObject(T object)
	{
		if (object == null || getSession() == null)
			return false;
		Long result =
			getSession().insert(getTableName(object.getClass()), null,
				getContentValuesForObject(object));
		if (result != null)
			object.setId(result);

		return (result != null);
	}

	/**
	 * This function will update all the properties of this object in the database
	 * 
	 * Keep in mind that only this object will be updated if the object have references to
	 * other objects which are not jet saved you will have to save these objects first
	 * otherwise other wise it will result in a ObjectNotSavedException because the
	 * referenced object is not saved (has no id).
	 */
	public <T extends IdObject> boolean updateObject(T object)
	{
		if (object == null || getSession() == null)
			return false;
		String whereClause = "";
		for (Field field : getTableColumnFields(object.getClass()))
		{
			if (field.isAnnotationPresent(Id.class))
				whereClause = getColumnName(field) + "=?";
		}
		int result =
			getSession().update(getTableName(object.getClass()), getContentValuesForObject(object),
				whereClause, new String[] {object.getId().toString()});

		return (result == 1);
	}

	/**
	 * If the object has no id the object will be saved as a new object in the database,
	 * however when the object does have an id the object will be updated
	 * 
	 * Keep in mind that only this object will be updated/saved if the object have
	 * references to other objects which are not jet saved you will have to save these
	 * objects first otherwise other wise it will result in a ObjectNotSavedException
	 * because the referenced object is not saved (has no id).
	 */
	public <T extends IdObject> boolean saveOrUpdateObject(T object)
	{
		if (object == null || getSession() == null)
			return false;
		if (object.getId() != null && object.getId().longValue() > 0)
			return updateObject(object);
		else
			return saveObject(object);
	}

	public int getDatabaseVersion()
	{
		if (getSession() != null)
			return getSession().getVersion();
		return 0;
	}

	private Session getSession()
	{
		if (configuration != null)
			return configuration.getSession();
		return null;
	}

	private List<Class< ? >> getSubClasses(Class< ? > clazz)
	{
		List<Class< ? >> result = new ArrayList<Class< ? >>();
		result.add(clazz);

		for (Class< ? > clzz : configuration.getClasses())
		{
			if (clazz.equals(clzz.getSuperclass()))
			{
				result.addAll(getSubClasses(clzz));
			}
		}

		return result;
	}

	private <T extends IdObject> T fillObject(Cursor cursor)
	{
		T object = getNewInstanceOf(cursor.getString(cursor.getColumnIndex(DTYPE)));

		Class< ? > clzz = object.getClass();
		while (clzz.isAnnotationPresent(MappedSuperclass.class)
			|| clzz.isAnnotationPresent(nl.spikey.orm.annotations.Entity.class))
		{
			Field[] fields = clzz.getDeclaredFields();

			for (Field field : fields)
			{
				if (field.isAnnotationPresent(Column.class))
				{
					boolean flag = field.isAccessible();
					field.setAccessible(true);

					Class< ? > typeClass = field.getType();

					try
					{
						if (typeClass.isPrimitive())
						{
							if (typeClass.equals(long.class))
								field.setLong(object,
									cursor.getLong(cursor.getColumnIndex(getColumnName(field))));
							else if (typeClass.equals(int.class))
								field.setInt(object,
									cursor.getInt(cursor.getColumnIndex(getColumnName(field))));
							else if (typeClass.equals(double.class))
								field.setDouble(object,
									cursor.getDouble(cursor.getColumnIndex(getColumnName(field))));
							else if (typeClass.equals(float.class))
								field.setFloat(object,
									cursor.getFloat(cursor.getColumnIndex(getColumnName(field))));
						}
						else
						{
							if (typeClass.isEnum())
							{
								String enumConst =
									cursor.getString(cursor.getColumnIndex(getColumnName(field)));
								if (enumConst != null && !isNull(enumConst))
								{
									@SuppressWarnings({"unchecked", "rawtypes"})
									Enum< ? > enumValue =
										Enum.valueOf((Class< ? extends Enum>) typeClass, enumConst);
									field.set(object, enumValue);
								}
							}
							else if (typeClass.equals(Long.class))
							{
								if (typeClass.equals(Long.class))
								{
									String stringValue =
										cursor.getString(cursor
											.getColumnIndex(getColumnName(field)));
									if (!isNull(stringValue))
										field.set(object, Long.valueOf(stringValue));
								}
							}
							else if (typeClass.equals(Integer.class))
							{
								if (typeClass.equals(Integer.class))
								{
									String stringValue =
										cursor.getString(cursor
											.getColumnIndex(getColumnName(field)));
									if (!isNull(stringValue))
										field.set(object, Integer.valueOf(stringValue));
								}
							}
							else if (typeClass.equals(Double.class))
							{
								if (typeClass.equals(Double.class))
								{
									String stringValue =
										cursor.getString(cursor
											.getColumnIndex(getColumnName(field)));
									if (!isNull(stringValue))
										field.set(object, Double.valueOf(stringValue));
								}
							}
							else if (typeClass.equals(Float.class))
							{
								if (typeClass.equals(Float.class))
								{
									String stringValue =
										cursor.getString(cursor
											.getColumnIndex(getColumnName(field)));
									if (!isNull(stringValue))
										field.set(object, Float.valueOf(stringValue));
								}
							}

							else if (typeClass.equals(String.class))
							{
								field.set(object,
									cursor.getString(cursor.getColumnIndex(getColumnName(field))));
							}
							else if (typeClass.equals(Boolean.class)
								|| typeClass.equals(boolean.class))
							{
								Short boolVar =
									cursor.getShort(cursor.getColumnIndex(getColumnName(field)));
								if (boolVar != null)
									field.setBoolean(object, boolVar == 0 ? false : true);
							}
							else if (typeClass.equals(byte[].class))
							{
								byte[] byteVar =
									cursor.getBlob(cursor.getColumnIndex(getColumnName(field)));
								if (byteVar != null && byteVar.length > 0)
									field.set(object, byteVar);
							}
							else if (IdObject.class.isAssignableFrom(typeClass))
							{
								String id =
									cursor.getString(cursor.getColumnIndex(getColumnName(field)));
								if (!isNull(id))
								{
									@SuppressWarnings("unchecked")
									Class< ? extends IdObject> explicitClass =
										(Class< ? extends IdObject>) typeClass;
									IdObject entity;
									if (!Modifier.isAbstract(explicitClass.getModifiers()))
									{
										entity = explicitClass.newInstance();
										entity.setId(Long.valueOf(id));
										entity.setNeedUpdate(true);
									}
									else
										entity = uniqueLazyObject(explicitClass, Long.valueOf(id));
									field.set(object, entity);
								}
							}
						}
					}
					catch (IllegalAccessException accEx)
					{
						Log.d(
							Orm.class.getSimpleName() + "("
								+ accEx.getStackTrace()[3].getLineNumber() + ")",
							"illegal access: " + accEx.getMessage());
					}
					catch (IllegalArgumentException argEx)
					{
						Log.d(
							Orm.class.getSimpleName() + "("
								+ argEx.getStackTrace()[3].getLineNumber() + ")",
							"illegal arguments: " + argEx.getMessage());
					}
					catch (InstantiationException instEx)
					{
						Log.d(Orm.class.getSimpleName(),
							"can't creat new instance: " + instEx.getMessage());
					}
					field.setAccessible(flag);
				}
			}

			clzz = clzz.getSuperclass();
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	private <T extends IdObject> T getNewInstanceOf(String className)
	{
		Class< ? > clazz = configuration.getClassMap().get(className);
		if (clazz != null)
		{
			try
			{
				return (T) clazz.newInstance();
			}
			catch (IllegalAccessException accEx)
			{
				Log.d(Orm.class.getSimpleName() + "(" + accEx.getStackTrace()[3].getLineNumber()
					+ ")", "illegal access: " + accEx.getMessage());
			}
			catch (InstantiationException instEx)
			{
				Log.d(Orm.class.getSimpleName(), "can't creat new instance: " + instEx.getMessage());
			}
		}
		else
		{
			Log.d(Orm.class.getSimpleName(), "Class \"" + className + "\" is not registred.");
		}
		return null;
	}

	/**
	 * based on {@link: http://www.sqlite.org/datatype3.html#affinity} and {@link:
	 * http://www.sqlite.org/lang_createtable.html}
	 */
	private String generateColumnNameAndConstraint(Field field) throws UnknownColumnTypeException
	{
		String query = getColumnName(field);
		Class< ? > typeClass = field.getType();
		if (typeClass.equals(Long.class) || typeClass.equals(long.class))
			query += " INTEGER";
		else if (typeClass.equals(Integer.class) || typeClass.equals(int.class))
			query += " INTEGER";
		else if (typeClass.equals(Double.class) || typeClass.equals(double.class))
			query += " REAL";
		else if (typeClass.equals(String.class))
			query += " TEXT";
		else if (typeClass.equals(Boolean.class) || typeClass.equals(boolean.class))
			query += " INTEGER";
		else if (typeClass.equals(byte[].class))
			query += " BLOB";
		else if (typeClass.isEnum())
			query += " TEXT";
		else if (IdObject.class.isAssignableFrom(typeClass))
		{
			query += " INTEGER";
			// TODO [ORM] ff uitzoeken hoe we die isForeignLKeyEnabled het beste aan
			// kunnen pakken.
			if (configuration.isForeignKeyEnabled())
				query += " REFERENCES " + getTableName(field.getDeclaringClass());
		}
		else
		{
			throw new UnknownColumnTypeException("Field " + getColumnName(field)
				+ " has @Column annotation, but is not supported as database ColumnType");
		}
		if (!field.getAnnotation(Column.class).nullable())
			query += " NOT NULL";
		if (field.getAnnotation(Column.class).unique())
			query += " UNIQUE";
		return query;
	}

	private static String getColumnName(Field field)
	{
		if (field.getAnnotation(Column.class).name().length() > 0)
			return field.getAnnotation(Column.class).name();
		return field.getName();
	}

	private static String getTableName(Class< ? > clazz)
	{
		Class< ? > tableClass = getTableClass(clazz);
		if (tableClass.getAnnotation(Entity.class).name().length() > 0)
			return tableClass.getAnnotation(Entity.class).name();
		return tableClass.getSimpleName();
	}

	private static Class< ? > getTableClass(Class< ? > clazz)
	{
		if (clazz.getSuperclass().isAnnotationPresent(Entity.class))
			return getTableClass(clazz.getSuperclass());
		return clazz;
	}

	private static boolean isNull(String enumConst)
	{
		if (enumConst == null
			|| enumConst.toLowerCase(Locale.ENGLISH).equals(NULL.toLowerCase(Locale.ENGLISH)))
			return true;
		return false;
	}

	private List<Field> getTableColumnFields(Class< ? > clazz)
	{
		List<Field> properties = new ArrayList<Field>();
		while (clazz.isAnnotationPresent(MappedSuperclass.class)
			|| clazz.isAnnotationPresent(Entity.class))
		{
			Field[] fields = clazz.getDeclaredFields();

			for (Field field : fields)
			{
				if (field.isAnnotationPresent(Column.class))
				{
					if (!properties.contains(field))
						properties.add(field);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return properties;
	}

	private <T extends IdObject> ContentValues getContentValuesForObject(T object)
	{
		ContentValues values = new ContentValues();
		values.put(DTYPE, object.getClass().getSimpleName());
		for (Field field : getTableColumnFields(object.getClass()))
		{
			if (field.isAnnotationPresent(Id.class))
				continue;

			boolean flag = field.isAccessible();
			field.setAccessible(true);

			try
			{
				Class< ? > typeClass = field.getType();
				if (typeClass.equals(long.class))
					values.put(getColumnName(field), field.getLong(object));
				else if (typeClass.equals(int.class))
					values.put(getColumnName(field), field.getInt(object));
				else if (typeClass.equals(double.class))
					values.put(getColumnName(field), field.getDouble(object));
				else if (typeClass.equals(boolean.class))
					values.put(getColumnName(field), field.getBoolean(object));
				else if (typeClass.isEnum())
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), field.get(object).toString());
				}
				else if (typeClass.equals(Long.class))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), (Long) field.get(object));
				}
				else if (typeClass.equals(Integer.class))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), (Integer) field.get(object));
				}
				else if (typeClass.equals(Double.class))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), (Double) field.get(object));
				}
				else if (typeClass.equals(String.class))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), field.get(object).toString());
				}
				else if (typeClass.equals(Boolean.class))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), (Boolean) field.get(object));
				}
				else if (typeClass.equals(byte[].class))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
						values.put(getColumnName(field), (byte[]) field.get(object));
				}
				else if (IdObject.class.isAssignableFrom(typeClass))
				{
					if (field.get(object) == null)
						values.putNull(getColumnName(field));
					else
					{
						IdObject entity = (IdObject) field.get(object);
						if (entity.getId() == null)
							throw new ObjectNotSavedException("object reference not saved");
						values.put(getColumnName(field), entity.getId());
					}
				}
			}
			catch (IllegalAccessException accEx)
			{
				Log.d(Orm.class.getSimpleName() + "(" + accEx.getStackTrace()[3].getLineNumber()
					+ ")", "illegal access: " + accEx.getMessage());
			}
			catch (IllegalArgumentException argEx)
			{
				Log.d(Orm.class.getSimpleName() + "(" + argEx.getStackTrace()[3].getLineNumber()
					+ ")", "illegal arguments: " + argEx.getMessage());
			}

			field.setAccessible(flag);
		}
		return values;
	}

	// -- LOW: [ORM] maybe replace the following code by proxies.

	private <T extends IdObject> T uniqueLazyObject(Class<T> clzz, long id)
	{
		Criteria criteria = new Criteria(clzz);
		criteria.addEquals(getColumnName(getIdColumnField(clzz)), id);
		Cursor cursor = selectQuery(criteria, new String[] {DTYPE});

		List<T> resultList = new ArrayList<T>();
		if (cursor != null && cursor.moveToFirst())
		{
			do
			{
				T object = getNewInstanceOf(cursor.getString(cursor.getColumnIndex(DTYPE)));
				// This might look a bit tricky, but if everything is right, this
				// is the only object to return because its filtered by database id.
				object.setId(id);
				object.setNeedUpdate(true);
				resultList.add(object);
			}
			while (cursor.moveToNext());

			cursor.close();
		}
		getSession().close();

		if (resultList.size() == 1)
			return resultList.get(0);
		return null;
	}

	private Field getIdColumnField(Class< ? > clazz)
	{
		while (clazz.isAnnotationPresent(MappedSuperclass.class)
			|| clazz.isAnnotationPresent(Entity.class))
		{
			Field[] fields = clazz.getDeclaredFields();

			for (Field field : fields)
			{
				if (field.isAnnotationPresent(Id.class))
				{
					return field;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	/**
	 * Returns the complete where clause (excluding 'WHERE') based on the criteria given
	 * 
	 * @param criteria
	 *            Criteria from what the where clause will be extracted.
	 * @param selectionArgs
	 *            A List to which the needed arguments will be added.
	 * @return A String containing the where clause based on the criteria.
	 */
	private String extractWhereClause(Criteria criteria, List<String> selectionArgs)
	{
		Class< ? extends IdObject> clazz = criteria.getClzz();
		if (!clazz.isAnnotationPresent(Entity.class))
			return null;
		String table = getTableName(clazz);
		String entityName = clazz.getSimpleName();
		if (clazz.getAnnotation(Entity.class).name().length() > 0)
			entityName = clazz.getAnnotation(Entity.class).name();

		String selection = "";
		// check if clazz is a subclass of the table class
		if (!table.equals(entityName))
		{
			// clazz and table are not the same, so clazz is a subclass of table, if clazz
			// has more subclasses than add these classes to DTYPE query.
			for (Class< ? > dtype : getSubClasses(clazz))
			{
				// als clazz nog subclasses heeft deze toevoegen aan dtype selection
				selection += DTYPE + "=? OR ";
				selectionArgs.add(dtype.getSimpleName());
			}
			selection = selection.substring(0, selection.length() - 4);
			selection += " AND ";
		}

		for (Criterion criterion : criteria.getCriterion())
		{
			selection += criterion.getExpression() + " AND ";
			if (criterion.getArguments() != null && !criterion.getArguments().isEmpty())
				selectionArgs.addAll(criterion.getArguments());
		}
		if (selection.length() > 4)
			selection = selection.substring(0, selection.length() - 5);
		return selection;
	}

}

