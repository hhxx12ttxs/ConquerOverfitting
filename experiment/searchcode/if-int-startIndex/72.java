/**************************************************************************
 * Store.java is part of Touch4j 4.0. Copyright 2012 Emitrom LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 **************************************************************************/
package com.emitrom.touch4j.client.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emitrom.touch4j.client.core.Ext;
import com.emitrom.touch4j.client.core.Function;
import com.emitrom.touch4j.client.core.JsObject;
import com.emitrom.touch4j.client.core.JsoHelper;
import com.emitrom.touch4j.client.core.config.StoreEvents;
import com.emitrom.touch4j.client.data.handlers.BeforeLoadHandler;
import com.emitrom.touch4j.client.data.handlers.BeforePrefetchHandler;
import com.emitrom.touch4j.client.data.handlers.ClearHandler;
import com.emitrom.touch4j.client.data.handlers.DataAddedHandler;
import com.emitrom.touch4j.client.data.handlers.DataChangedHandler;
import com.emitrom.touch4j.client.data.handlers.EachCallBack;
import com.emitrom.touch4j.client.data.handlers.FilterHandler;
import com.emitrom.touch4j.client.data.handlers.RemoveHandler;
import com.emitrom.touch4j.client.data.handlers.UpdateHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;

/**
 * The Store class encapsulates a client side cache of Model objects. Stores
 * load data via a Proxy, and also provide functions for sorting, filtering and
 * querying the model instances contained within it.
 * 
 * @See <a
 *      href="http://docs.sencha.com/touch/2-0/#!/api/Ext.data.Store">Ext.data
 *      .Store</a>
 * 
 */
public class Store extends JsObject {

    private final static String ADD = StoreEvents.ADD.getValue();
    private final static String BEFORE_LOAD = StoreEvents.BEFORE_LOAD.getValue();
    private final static String BEFORE_PREFETCH = StoreEvents.BEFORE_PREFETCH.getValue();
    private final static String CLEAR = StoreEvents.CLEAR.getValue();
    private final static String DATACHANGED = StoreEvents.DATACHANGED.getValue();
    private final static String REMOVE = StoreEvents.REMOVE.getValue();
    private final static String UPDATE = StoreEvents.UPDATE.getValue();

    protected final static String model = "emitrom-model-";
    protected static int modelRegistry = 0;
    protected String modelName;
    List<BaseModel> elements = new ArrayList<BaseModel>();

    protected Store() {
        create();
    }

    protected Store(JavaScriptObject obj) {
        super(obj);
    }

    protected static Store instance(JavaScriptObject obj) {
        return new Store(obj);
    }

    /**
     * This constructor allows a Store to be created; it MUST contain at least
     * one element, since the Store's fields will be derived from the first
     * element in the Store.
     * 
     * @param data
     */
    public Store(List<? extends BaseModel> data) {
        assert !data.isEmpty() : "BaseModel list cannot be empty.";
        Set<String> fields = new HashSet<String>(data.get(0).getFields());
        init(data, fields);
    }

    /**
     * This constructor allows a Store to be created without elements. However,
     * the list of fields must be specified.
     * 
     * @param data
     * @param fields
     */
    public Store(List<? extends BaseModel> data, String... fields) {
        assert fields.length != 0 : "Fields list cannot be empty.";
        init(data, new HashSet<String>(Arrays.asList(fields)));
    }

    /**
     * Initializes this Store with the given BaseModel objects and the given
     * fields.
     * 
     * @param data
     * @param fields
     */
    protected void init(List<? extends BaseModel> data, Set<String> fields) {
        modelRegistry++;
        modelName = model + modelRegistry;
        this.elements.addAll(data);

        Ext.defineModel(modelName, fields);
        JavaScriptObject[] storeData = new JavaScriptObject[data.size()];

        for (int i = 0; i < data.size(); i++) {
            storeData[i] = data.get(i).getJsObj();
        }

        create(modelName, JsoHelper.arrayConvert(storeData));
    }

    /**
     * Adds Model instance to the Store.
     * 
     * @param value
     */
    public void add(BaseModel... values) {
        addAll(Arrays.asList(values));
    }

    public void addAll(List<? extends BaseModel> data) {
        JavaScriptObject[] storeData = new JavaScriptObject[data.size()];
        for (int i = 0; i < data.size(); i++) {
            storeData[i] = data.get(i).getJsObj();
        }
        add(JsoHelper.arrayConvert(storeData));
    }

    /**
     * Gets the average value in the store.
     * 
     * @param field
     * @param grouped
     * @return
     */
    public native JavaScriptObject average(String field) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.average(field);
    }-*/;

    /**
     * Revert to a view of the Record cache with no filtering applied.
     * 
     * @param suppressEvent
     */
    public native void clearFilter(boolean suppressEvent) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso.clearFilter(suppressEvent);
    }-*/;

    /**
     * Revert to a view of the Record cache with no filtering applied.
     * 
     * @param suppressEvent
     */
    public native void clearFilter() /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso.clearFilter();
    }-*/;

    /**
     * Filters the loaded set of records by a given set of filters.
     * 
     * @param suppressEvent
     */
    public native void filter(String filters) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso.filter(filters);
    }-*/;

    /**
     * Filters the loaded set of records by a given set of filters.
     * 
     * @param suppressEvent
     */
    public native void filter(String filters, String value) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso.filter(filters, value);
    }-*/;

    /**
     * Filters the loaded set of records by a given set of filters.
     * 
     * @param suppressEvent
     */
    public native void filter(String filters, String value, boolean anyMatch) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso.filter(filters, value, anyMatch);
    }-*/;

    /**
     * Filters the loaded set of records by a given set of filters.
     * 
     * @param suppressEvent
     */
    public native void filter(String filters, String value, boolean anyMatch, boolean caseInsensitive) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso.filter(filters, value, anyMatch, caseInsensitive);
    }-*/;

    public native void filterBy(FilterHandler handler) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		jso
				.filterBy(function(record) {
					var model = @com.emitrom.touch4j.client.data.BaseModel::new(Lcom/google/gwt/core/client/JavaScriptObject;)(record);
					return handler.@com.emitrom.touch4j.client.data.handlers.FilterHandler::onFilter(Lcom/emitrom/touch4j/client/data/BaseModel;)(model);
				});
    }-*/;

    /**
     * Calls the specified function for each of the Records in the cache.
     * Returning false aborts and exits the iteration.
     * 
     * @param callback
     */
    public native boolean each(EachCallBack callback)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso
				.each(function(record) {
					var model = @com.emitrom.touch4j.client.data.BaseModel::new(Lcom/google/gwt/core/client/JavaScriptObject;)(record)
					callback.@com.emitrom.touch4j.client.data.handlers.EachCallBack::onEachRecord(Lcom/emitrom/touch4j/client/data/BaseModel;)(model);
				});
    }-*/;

    public native int count()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.count();
    }-*/;

    /**
     * Collects unique values for a particular dataIndex from this store.
     * 
     * @param dataIndex
     * @param allowNull
     * @param bypassFilter
     * @return
     */
    public native JsArray<JavaScriptObject> collect(String dataIndex, boolean allowNull, boolean bypassFilter) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.collect(dataIndex, allowNull, bypassFilter);
    }-*/;

    public BaseModel getAt(int index) {
        JavaScriptObject obj = _getAt(index);
        BaseModel model = elements.get(index);
        model.update(obj);
        return model;
    }

    private native JavaScriptObject _getAt(int index)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getAt(index);
		//return @com.emitrom.touch4j.client.core.BaseModel::new(Lcom/google/gwt/core/client/JavaScriptObject;)(value);
    }-*/;

    /**
     * The Collection that holds this store's local cache of records
     * 
     * @param data
     */
    public void setData(List<? extends BaseModel> data) {
        JavaScriptObject[] storeData = new JavaScriptObject[data.size()];
        for (int i = 0; i < data.size(); i++) {
            storeData[i] = data.get(i).getJsObj();
        }
        _setData(JsoHelper.arrayConvert(storeData));
        elements.clear();
        elements.addAll(data);
    }

    /**
     * 
     * @return, The page that the Store has most recently loaded.
     */
    private native void _setData(JavaScriptObject obj)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.setData(obj);
    }-*/;

    /**
     * 
     * @return, The page that the Store has most recently loaded.
     */
    public native int getCurrentPage()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.currentPage;
    }-*/;

    public native void setCurrentPage(int value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.currentPage = value;
    }-*/;

    /**
     * The default sort direction to use if one is not specified (defaults to
     * SortDirection.ASC)
     * 
     * @param sortDirection
     */
    public void setDefaultSortDirection(SortDirection sortDirection) {
        setDefaultSortDirection(sortDirection.getValue());
    }

    public SortDirection getDefaultSortDirection() {
        return SortDirection.valueOf(_getDefaultSortDirection());
    }

    /**
     * The direction in which sorting should be applied when grouping. Defaults
     * to SortDirection.ASC - the other supported value is SortDirection.DESC
     * 
     * @param direction
     */
    public void setGroupDirection(SortDirection direction) {
        setGroupDir(direction.getValue());
    }

    public SortDirection getGroupDirection() {
        return SortDirection.valueOf(_getGroupDir());
    }

    /**
     * The (optional) field by which to group data in the store. Internally,
     * grouping is very similar to sorting - the groupField and groupDir are
     * injected as the first sorter (see sort). Stores support a single level of
     * grouping, and groups can be fetched via the getGroups method.
     * 
     * @param value
     */
    public native void setGroupField(String value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.groupField = value;
    }-*/;

    public native String getGroupField()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.groupField;
    }-*/;

    /**
     * Finds the index of the first matching Record in this store by a specific
     * field value.
     * 
     * @param fieldName
     * @param value
     * @param startIndex
     * @return
     */
    public native int findExact(String fieldName, Object value, int startIndex) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.findExact(fieldName, value, startIndex);
    }-*/;

    /**
     * fieldName : String The name of the Record field to test. value :
     * String/RegExp Either a string that the field value should begin with, or
     * a RegExp to test against the field. startIndex : Number (optional) The
     * index to start searching at anyMatch : Boolean (optional) True to match
     * any part of the string, not just the beginning caseSensitive : Boolean
     * (optional) True for case sensitive comparison exactMatch : Boolean
     * (optional) True to force exact match (^ and $ characters added to the
     * regex). Defaults to false.
     * 
     * @param <T>
     * @param fieldName
     * @param value
     * @param startIndex
     * @param anyMatch
     * @param caseSensitive
     * @param exactMatch
     * @return
     */
    public native <T extends BaseModel> T findRecord(String fieldName, String value, int startIndex, boolean anyMatch,
                    boolean caseSensitive, boolean exactMatch) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.findExact(fieldName, value, startIndex, anyMatch,
				caseSensitive, exactMatch);
    }-*/;

    /**
     * Convenience function for getting the first model instance in the store
     * 
     * @param <T>
     * @param grouped
     * @return
     */
    public native <T extends BaseModel> T first(boolean grouped) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.first(grouped);
    }-*/;

    /**
     * Get the Record with the specified id.
     * 
     * @param <T>
     * @param id
     * @return
     */
    public native <T extends BaseModel> T getById(String id) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getById(id);
    }-*/;

    /**
     * Returns the string to group on for a given model instance. The default
     * implementation of this method returns the model's groupField, but this
     * can be overridden to group by an arbitrary string.
     * 
     * @param instance
     * @return
     */
    public native String getGroupString(BaseModel instance) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getGroupString(instance);
    }-*/;

    /**
     * Returns an array containing the result of applying grouping to the
     * records in this store. See groupField, groupDir and getGroupString.
     * 
     * @param groupName
     * @return
     */
    public native JsArray<JavaScriptObject> getGroups(String groupName) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getGroups(groupName);
    }-*/;

    /**
     * Returns a range of Records between specified indices.
     * 
     * @param <T>
     * @param startIndex
     * @param endIndex
     * @return
     */
    public native <T extends BaseModel> T getRange(int startIndex, int endIndex) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getRange(startIndex, endIndex);
    }-*/;

    /**
     * Group data in the store
     * 
     * @param groupers
     * @param direction
     */
    public native void group(String groupers, String direction) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.group(groupers, direction);
    }-*/;

    /**
     * Group data in the store
     * 
     * @param groupers
     * @param direction
     */
    public native void group(JsArray<JavaScriptObject> groupers, String direction) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.group(groupers, direction);
    }-*/;

    /**
     * Get the index within the cache of the passed Record.
     * 
     * @param model
     * @return
     */
    public int indexOf(BaseModel model) {
        return elements.indexOf(model);
    }

    private native int _indexOf(BaseModel model) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.indexOf(model);
    }-*/;

    /**
     * Get the index within the cache of the Record with the passed id.
     * 
     * @param id
     * @return
     */
    public native int indexOfId(String id) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.indexOfId(id);
    }-*/;

    /**
     * Get the index within the entire dataset. From 0 to the totalCount.
     * 
     * @param model
     * @return
     */
    public native int indexOfTotal(BaseModel model) /*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.indexOfTotal(model);
    }-*/;

    /**
     * Flag denoting that this object is sortable. Always true.
     * 
     * @return
     */
    public native boolean isSortable()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.isSortable;
    }-*/;

    /**
     * True if the Store has already been destroyed. If this is true, the
     * reference to Store should be deleted as it will not function correctly
     * any more.
     * 
     * @return
     */
    public native boolean isDestroyed()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.isDestroyed;
    }-*/;

    /**
     * The sorter currently applied to this Store
     * 
     * @param value
     */
    public native void setSorter(String value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.sorters = value;
    }-*/;

    /**
     * Appends an event handler to this object.
     * 
     * @param callback
     */
    public native void addListener(String eventName, Function callback)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						eventName,
						function() {
							callback.@com.emitrom.touch4j.client.core.Function::execute()();
						});
    }-*/;

    /**
     * Clear any groupers in the store
     */
    public native void clearGrouping()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.clearGrouping();
    }-*/;

    /**
     * Removes all listeners for this object.
     */
    public native void clearListeners()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.clearListeners();
    }-*/;

    /**
     * Gets the number of cached records.
     */
    public native int getCount()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getCount();
    }-*/;

    /**
     * A configuration object for this Store's grouper.
     * 
     * @param handler
     * @return
     */

    public native String setGrouper(Grouper grouper)/*-{
		var j = this.@com.emitrom.touch4j.client.core.JsObject::getJsObj()();
		j
				.setGrouper(function(record) {
					var model = @com.emitrom.touch4j.client.data.BaseModel::new(Lcom/google/gwt/core/client/JavaScriptObject;)(record);
					return grouper.@com.emitrom.touch4j.client.data.Grouper::onGroup(Lcom/emitrom/touch4j/client/data/BaseModel;)(model);
				});

    }-*/;

    /**
     * Returns the total number of Model instances that the Proxy indicates
     * exist. This will usually differ from getCount when using paging -
     * getCount returns the number of records loaded into the Store at the
     * moment, getTotalCount returns the number of records that could be loaded
     * into the Store if the Store contained all data
     */
    public native int getTotalCount()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getTotalCount();
    }-*/;

    /**
     * Determines the page from a record index
     * 
     * @param index
     * @return
     */
    public native int getPageFromRecordIndex(int index)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.getPageFromRecordIndex();
    }-*/;

    /**
     * Checks to see if this object has any listeners for a specified event
     * 
     * @param index
     * @return
     */
    public native boolean hasListener(int index)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.hasListener();
    }-*/;

    /**
     * Convenience function for getting the last model instance in the store
     * 
     * @param <T>
     * @return
     */
    public native <T extends BaseModel> T last()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		var value = jso.last();
		return @com.emitrom.touch4j.client.data.BaseModel::new(Lcom/google/gwt/core/client/JavaScriptObject;)(value);
    }-*/;

    /**
     * Convenience function for getting the last model instance in the store
     * 
     * @param <T>
     * @return
     */
    public native <T extends BaseModel> T last(boolean grouped)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		var value = jso.last(grouped);
		return @com.emitrom.touch4j.client.data.BaseModel::new(Lcom/google/gwt/core/client/JavaScriptObject;)(value);
    }-*/;

    /**
     * Loads the next 'page' in the current data set
     */
    public native void nextPage()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.nextPage();
    }-*/;

    public native void nextPage(Object options)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.nextPage(options);
    }-*/;

    /**
     * Loads the next 'page' in the current data set
     */
    public native void previousPage()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.previousPage();
    }-*/;

    public native void previousPage(Object options)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.previousPage(options);
    }-*/;

    /**
     * The colletection of sorters currently applied to this Store
     * 
     * @param value
     */
    public void setSorters(List<String> sorters) {
        JsArrayString values = JsArrayString.createArray().cast();
        for (String s : sorters) {
            values.push(s);
        }
        _setSorters(values);
    }

    /**
     * Inserts Model instances into the Store at the given index and fires the
     * add event.
     * 
     * @param index
     * @param models
     */
    private native void _insertNative(int index, List<BaseModel> models)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		var values = @com.emitrom.touch4j.client.data.BaseModel::fromList(Ljava/util/List;)(models);
		jso.insert(index, values);
    }-*/;

    /**
     * Insert a given model at the given position
     * 
     * @param index
     * @param model
     */
    public void insert(int index, BaseModel model) {
        List<BaseModel> models = new ArrayList<BaseModel>();
        models.add(model);
        _insertNative(index, models);
        this.elements.add(index, model);

    }

    /**
     * Removes the given record from the Store, firing the 'remove' event for
     * each instance that is removed, plus a single 'datachanged' event after
     * removal.
     * 
     * @param value
     */
    public void remove(BaseModel value) {
        remove(value.getJsObj());
        elements.remove(value);
    }

    /**
     * Removes the given record from the Store, firing the 'remove' event for
     * each instance that is removed, plus a single 'datachanged' event after
     * removal.
     * 
     * @param data
     */
    public void remove(List<? extends BaseModel> data) {
        elements.removeAll(data);
        removeNative(data);
    }

    private void removeNative(List<? extends BaseModel> data) {
        JavaScriptObject[] storeData = new JavaScriptObject[data.size()];
        for (int i = 0; i < data.size(); i++) {
            storeData[i] = data.get(i).getJsObj();
        }
        remove(JsoHelper.arrayConvert(storeData));

    }

    public void removeAll() {
        elements.clear();
        removeAllNative();
    }

    private native void removeAllNative()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.removeAll();
    }-*/;

    public void removeAt(int index) {
        elements.remove(index);
        removeAtNative(index);
    }

    private native void removeAtNative(int index)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.removeAt(index);
    }-*/;

    public native void resumeEvents()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.resumentEvents();
    }-*/;

    public void removeAll(boolean silent) {
        elements.clear();
        removeAllNative(silent);
    }

    private native void removeAllNative(boolean silent)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.removeAll(silent);
    }-*/;

    /**
     * Loads an array of data straight into the Store
     * 
     * @param data
     *            , Array of data to load
     * @param append
     *            , True to add the records to the existing records in the
     *            store, false to remove the old ones first
     */
    public void loadData(List<? extends BaseModel> data, boolean append) {
        if (append == false) {
            elements.clear();
        }
        elements.addAll(data);
        loadDataNative(data, append);
    }

    private void loadDataNative(List<? extends BaseModel> data, boolean append) {
        JavaScriptObject[] storeData = new JavaScriptObject[data.size()];
        for (int i = 0; i < data.size(); i++) {
            storeData[i] = data.get(i).getJsObj();
        }
        loadData(JsoHelper.arrayConvert(storeData), append);

    }

    /**
     * Loads a given 'page' of data by setting the start and limit values
     * appropriately. Internally this just causes a normal load operation,
     * passing in calculated 'start' and 'limit' params
     * 
     * @param page
     * @param options
     */
    public native void loadPage(int page, Object options)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.loadPage(page, options);
    }-*/;

    public native void addDataAddedHandler(DataAddedHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						@com.emitrom.touch4j.client.data.Store::ADD,
						function(store, data) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.DataAddedHandler::onAdd(Lcom/emitrom/touch4j/client/data/Store;Lcom/google/gwt/core/client/JavaScriptObject;)(s, data);
						});
    }-*/;

    public native void addBeforeLoadHandler(BeforeLoadHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						@com.emitrom.touch4j.client.data.Store::BEFORE_LOAD,
						function(store) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.BeforeLoadHandler::onBeforeLoad(Lcom/emitrom/touch4j/client/data/Store;)(s);
						});
    }-*/;

    public native void addBeforePrefetch(BeforePrefetchHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						@com.emitrom.touch4j.client.data.Store::BEFORE_PREFETCH,
						function(store) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.BeforePrefetchHandler::onBeforePrefetch(Lcom/emitrom/touch4j/client/data/Store;)(s);
						});
    }-*/;

    public native void addClearHandler(ClearHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						@com.emitrom.touch4j.client.data.Store::CLEAR,
						function(store) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.ClearHandler::onClear(Lcom/emitrom/touch4j/client/data/Store;)(s);
						});
    }-*/;

    public native void addDataChangedHandler(DataChangedHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						@com.emitrom.touch4j.client.data.Store::DATACHANGED,
						function(store) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.DataChangedHandler::onDataChanged(Lcom/emitrom/touch4j/client/data/Store;)(s);
						});
    }-*/;

    public native void addRemoveHandler(RemoveHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						"remove",
						function(store) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.RemoveHandler::onRemove(Lcom/emitrom/touch4j/client/data/Store;)(s);
						});
    }-*/;

    public native void addUpdateHandler(UpdateHandler handler)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso
				.addListener(
						@com.emitrom.touch4j.client.data.Store::UPDATE,
						function(store) {
							var s = @com.emitrom.touch4j.client.data.Store::instance(Lcom/google/gwt/core/client/JavaScriptObject;)(store);
							handler.@com.emitrom.touch4j.client.data.handlers.UpdateHandler::onUpdate(Lcom/emitrom/touch4j/client/data/Store;)(s);
						});
    }-*/;

    private native void _setSorters(JavaScriptObject values)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.sorters = values;
    }-*/;

    private native void create(String modelName, JavaScriptObject values)/*-{
		this.@com.emitrom.touch4j.client.core.JsObject::jsObj = new $wnd.Ext.data.Store(
				{
					model : modelName,
					data : values
				});
    }-*/;

    private native void create()/*-{
		this.@com.emitrom.touch4j.client.core.JsObject::jsObj = new $wnd.Ext.data.Store(
				{
					model : "Emitrom"
				});
    }-*/;

    private native void setDefaultSortDirection(String value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.defaultSortDirection = value;
    }-*/;

    private native String _getDefaultSortDirection()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.defaultSortDirection;
    }-*/;

    private native void setGroupDir(String value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.groupDir = value;
    }-*/;

    private native String _getGroupDir()/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		return jso.groupDir;
    }-*/;

    private native void add(JavaScriptObject value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.add(value);
    }-*/;

    private native void remove(JavaScriptObject value)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.remove(value);
    }-*/;

    private native void loadData(JavaScriptObject values, boolean append)/*-{
		var jso = this.@com.emitrom.touch4j.client.core.JsObject::jsObj;
		jso.loadData(values, append);
    }-*/;

}

