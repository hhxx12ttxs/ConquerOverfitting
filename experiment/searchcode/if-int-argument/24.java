/*
 * Copyright (C) 2013 JAFS.es
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.jafs.jaiberdroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;

/**
 * Class that implements an SQLite query.
 * @author  Jose Antonio Fuentes Santiago
 * @version 0.5
 */
public final class Query {
	/**
	 * Types of queries.
	 * @author  Jose Antonio Fuentes Santiago
	 * @version 0.5
	 */
	public enum Type {
		/** Select query. */
		SELECT,
		/** Insert query. */
		INSERT,
		/** Update query. */
		UPDATE,
		/** Delete query. */
		DELETE
	}


	/** List of arguments in condition. */
	protected final List<String> args = new ArrayList<String>();

	/** Table name for the query. */
	protected Entity entity;
	/** Values to insert or update. */
	protected ContentValues values;
	/** Condition that must comply the query. */
	protected String condition;
	/** Sets if the query is transaccional. The query is executed between a transaction. */
	protected boolean transactional;
	/** Type of query. */
	protected Type type;
	/** Object with data of entity. */
	protected Object object;


	/**
	 * Private constructor for inheritance.
	 */
	protected Query() {
	}


	/**
	 * Constructor for queries without values.
	 * @param type       Type of query.
	 * @param classType  Class of entity.
	 */
	@SuppressWarnings("rawtypes")
	public Query(final Type type, final Class classType) {
		this.entity = JaiberdroidInstance.getEntityManager().getEntity(classType);
		this.type = type;
	}


	/**
	 * Constructor for queries with values.
	 * @param type    Type of query.
	 * @param object  Object that contains data of query.
	 */
	public Query(final Type type, final Object object) {
		this.entity = JaiberdroidInstance.getEntityManager().getEntity(object.getClass());
		this.type = type;
		this.object = object;
	}


	/**
	 * Creates an insert query.
	 * @param  object  Object with data to insert.
	 * @return Query   Query generated.
	 * @throws JaiberdroidException 
	 */
	public static Query createInsert(final Object object) throws JaiberdroidException {
		final Query query = new Query(Type.INSERT, object);

		query.setTransactional(true);
		query.setValues(getValues(query, false, (List<String>) null));

		return query;
	}


	/**
	 * Creates an update query.
	 * @param  object  Object with data to update.
	 * @return Query   Query generated.
	 * @throws JaiberdroidException 
	 */
	public static Query createUpdate(final Object object) throws JaiberdroidException {
		final Query query = new Query(Type.UPDATE, object);

		query.addArg(JaiberdroidReflection.executeGetMethod(JaiberdroidReflection.GET_ID, object));
		query.setCondition(JaiberdroidSql._ID + " = ?");
		query.setTransactional(true);
		query.setValues(getValues(query, false, (List<String>) null));

		return query;
	}


	/**
	 * Creates a delete query.
	 * @param  type  Type of entity.
	 * @param  id    Id of elemento to delete. If id is -1, get the remove all query.
	 * @return Query generated.
	 * @throws JaiberdroidException 
	 */
	@SuppressWarnings("rawtypes")
	public static Query createDelete(final Class type, final int id) throws JaiberdroidException {
		final Query query = new Query(Type.DELETE, type);

		if (-1 != id) {
			query.addArg(id);
			query.setCondition(JaiberdroidSql._ID + " = ?");
		}
		query.setTransactional(true);

		return query;
	}


	/**
	 * Get a content values object for received query.
	 * @param  query   Query that contains the data.
	 * @param  id      Boolean value that indicated if content values stores also the id.
	 * @return ContentValues object with generated data.
	 * @throws JaiberdroidException 
	 */
	public static ContentValues getValues(final Query query, final boolean id) throws JaiberdroidException {
		return getValues(query, id, (List<String>) null);
	}


	/**
	 * Get a content values object for received query, filtering the fields indicated in filter.
	 * @param  query   Query that contains the data.
	 * @param  id      Boolean value that indicated if content values stores also the id.
	 * @param  filter  Strings array that contains the names of fields that must be ignored.
	 * @return ContentValues object with generated data.
	 * @throws JaiberdroidException 
	 */
	public static ContentValues getValues(final Query query, final boolean id, final String[] filter)
										throws JaiberdroidException {
		if (null != filter && filter.length > 0) {
			return getValues(query, id, Arrays.asList(filter));
		} else {
			return getValues(query, id, (List<String>) null);
		}
	}


	/**
	 * Get a content values object for received query.
	 * @param  query   Query that contains the data.
	 * @param  id      Boolean value that indicated if content values stores also the id.
	 * @param  filter  List of strings that contains the names of fields that must be ignored.
	 * @return ContentValues object with generated data.
	 * @throws JaiberdroidException 
	 */
	@SuppressWarnings("deprecation")
	protected static ContentValues getValues(final Query query, final boolean id, final List<String> filter) throws JaiberdroidException {
		final ContentValues values = new ContentValues();
		final Object object = query.getObject();
		String name;
		Object data;

		try {
			// Get al the fields for entity.
			for (final Field field : query.getEntity().getFields().getFields().values()) {
				// We can control if the field is ID or is in filter.
				if ((id || !JaiberdroidSql._ID.equals(field.getName()))
						&& (null == filter || !filter.contains(field.getName()))) {
					// Executes the method to obtain the value.
					name = JaiberdroidReflection.getMethodGet(field.getName(), field.getType());
					data = JaiberdroidReflection.executeGetMethod(name, object);

					// Checks if the value is ok.
					if (null == data) {
						if (field.isNull()) {
							values.putNull(field.getName());
						} else {
							throw new JaiberdroidException("Field " + field.getName() + " in table "
														+ query.getEntity().getTableName()
														+ " can't be null");
						}
					} else if (FieldTypes.DATE.equals(field.getType())) {
						values.put(field.getName(), Long.toString(Date.parse((String) data) / 1000));
					} else {
						values.put(field.getName(), data.toString());
					}
				}
			}
		} catch (final SecurityException e) {
			throw new JaiberdroidException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new JaiberdroidException(e.getMessage());
		}

		return values;
	}


	/**
	 * Gets the entity of the query.
	 * @return Entity object of the query.
	 */
	final Entity getEntity() {
		return entity;
	}


	/**
	 * Gets a field array.
	 * @return Array of fields of current entity query.
	 */
	public String[] getFields() {
		return entity.getFields().getFieldsArray();
	}


	/**
	 * Gets the condition for action.
	 * @return String with condition for action.
	 */
	public final String getCondition() {
		return condition;
	}


	/**
	 * Sets the condition for action.
	 * @param  condition  String with condition for action.
	 */
	public final void setCondition(final String condition) {
		this.condition = condition;
	}


	/**
	 * Gets the values to set.
	 * @return Object with a set of values to set.
	 */
	public final ContentValues getValues() {
		return values;
	}


	/**
	 * Sets the values to set.
	 * @param  values  Object with a set of values to set.
	 */
	public final void setValues(final ContentValues values) {
		this.values = values;
	}


	/**
	 * Sets the values to set.
	 * @param  name   Name of value.
	 * @param  value  String with the value to add.
	 */
	public final void addValue(final String name, final String value) {
		if (null == values) {
			values = new ContentValues();
		}
		values.put(name, value);
	}


	/**
	 * Sets the values to set.
	 * @param  name   Name of value.
	 * @param  value  Integer with the value to add.
	 */
	public final void addValue(final String name, final int value) {
		if (null == values) {
			values = new ContentValues();
		}
		values.put(name, value);
	}


	/**
	 * Sets the values to set.
	 * @param  name   Name of value.
	 * @param  value  Float with the value to add.
	 */
	public final void addValue(final String name, final float value) {
		if (null == values) {
			values = new ContentValues();
		}
		values.put(name, value);
	}


	/**
	 * Sets the values to set.
	 * @param  name   Name of value.
	 * @param  value  Double with the value to add.
	 */
	public final void addValue(final String name, final double value) {
		if (null == values) {
			values = new ContentValues();
		}
		values.put(name, value);
	}


	/**
	 * Sets the values to set.
	 * @param  name  Name of the null value to add.
	 */
	public final void addNull(final String name) {
		if (null == values) {
			values = new ContentValues();
		}
		values.putNull(name);
	}


	/**
	 * Gets the arguments of condition.
	 * @return String array with arguments for condition.
	 */
	public final String[] getArgsArray() {
		String[] argsArray = null;

		if (!args.isEmpty()) {
			argsArray = new String[args.size()];
			int i = 0;
			for (final String argument : args) {
				argsArray[i] = argument;
				++i;
			}
		}

		return argsArray;
	}


	/**
	 * Gets the arguments of condition.
	 * @return String array with arguments for condition.
	 */
	public final List<String> getArgs() {
		return args;
	}


	/**
	 * Adds a string argument.
	 * @param  argument  String argument to add.
	 */
	public final void addArg(final String argument) {
		args.add(argument);
	}


	/**
	 * Adds an integer argument.
	 * @param  argument  Integer argument to add.
	 */
	public final void addArg(final int argument) {
		addArg(Integer.toString(argument));
	}


	/**
	 * Adds a float argument.
	 * @param  argument  Float argument to add.
	 */
	public final void addArg(final float argument) {
		addArg(Float.toString(argument));
	}


	/**
	 * Adds an double argument.
	 * @param  argument  Double argument to add.
	 */
	public final void addArg(final double argument) {
		addArg(Double.toString(argument));
	}


	/**
	 * Sets the arguments of condition.
	 * @param  args  String array with arguments for condition.
	 */
	public final void setArgs(final List<String> args) {
		this.args.clear();

		if (null != args) {
			for (final String argument : args) {
				this.args.add(argument);
			}
		}
	}


	/**
	 * Clears the arguments list.
	 */
	public final void clearArgs() {
		args.clear();
	}


	/**
	 * Gets if query is transactional.
	 * @return Boolean value that indicates if query is transactional.
	 */
	public final boolean isTransactional() {
		return transactional;
	}


	/**
	 * Sets if query is transactional.
	 * @param  transactional  Boolean value that indicates if query is transactional.
	 */
	public final void setTransactional(final boolean transactional) {
		this.transactional = transactional;
	}


	/**
	 * Gets type of query.
	 * @return Type of query.
	 */
	public final Type getType() {
		return type;
	}


	/**
	 * Sets type of query.
	 * @param  type  Type of query.
	 */
	public final void setType(final Type type) {
		this.type = type;
	}


	/**
	 * Gets the current object.
	 * @return Object that contains query values.
	 */
	public final Object getObject() {
		return object;
	}


	/**
	 * Sets the current object.
	 * @param  object  Object that contains query values.
	 */
	public final void setObject(final Object object) {
		this.object = object;
	}


	/**
	 * Gets the id of current object if exists.
	 * @return Integer with id of current object. 0 if the object hasn't id.
	 */
	public final int getId() {
		int id = 0;

		if (null != values && values.containsKey(JaiberdroidSql._ID)) {
			id = values.getAsInteger(JaiberdroidSql._ID);
		}

		return id;
	}


	/**
	 * Gets a string with content of the current Query.
	 * @return String with content of the current Query.
	 */
	@Override
	public String toString() {
		final StringBuilder objBuilder = new StringBuilder();

		objBuilder.append("table->");
		objBuilder.append(entity.getTableName());
		objBuilder.append("; condition->");
		objBuilder.append(condition);
		objBuilder.append("; values{");
		objBuilder.append(values);
		objBuilder.append("}; args->");
		objBuilder.append(args);
		objBuilder.append("; transactional->");
		objBuilder.append(transactional);
		objBuilder.append("; type->");
		objBuilder.append(type);

		return objBuilder.toString();
	}
}

