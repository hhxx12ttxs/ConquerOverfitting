package org.synthful.smartgwt.client.widgets;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.synthful.smartgwt.client.UIMasquerade;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.core.BaseClass;
import com.smartgwt.client.core.DataClass;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.OperationBinding;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.SerializationContext;
import com.smartgwt.client.data.XmlNamespaces;
import com.smartgwt.client.data.events.HandleErrorHandler;
import com.smartgwt.client.types.CriteriaPolicy;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.EnumTranslateStrategy;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.RPCTransport;
import com.smartgwt.client.types.TextMatchStyle;
import com.smartgwt.client.types.ValueEnum;

public class UIDataSource
extends Widget
implements UIMasquerade<DataSource>, HasWidgets {

	final protected DataSource datasource = new DataSource();
	
	@Override
	public void add(Widget w) {
		if (w instanceof UIDataSourceField){
			addField(((UIDataSourceField)w).getSmartObject());
		}
	}

	@Override
	public void clear() {
	}

	@Override
	public Iterator<Widget> iterator() {
		List<Widget> fields = new ArrayList<Widget>(datasource.getFields().length);
		for (DataSourceField field : datasource.getFields()) {
			fields.add(new UIDataSourceField(field));
		}
		return fields.iterator();
	}

	@Override
	public boolean remove(Widget w) {
		return false;
	}

	@Override
	public DataSource getSmartObject() {
		return this.datasource;
	}

	public String getDataSrcID() {
		return datasource.getID();
	}

	public void setDataSrcID(String id) {
		datasource.setID(id);
	}

	public String getScClassName() {
		return datasource.getScClassName();
	}

	public void setScClassName(String scClassName) {
		datasource.setScClassName(scClassName);
	}

	public int hashCode() {
		return datasource.hashCode();
	}

	public JavaScriptObject getConfig() {
		return datasource.getConfig();
	}

	public JavaScriptObject getOrCreateJsObj() {
		return datasource.getOrCreateJsObj();
	}

	public void destroy() {
		datasource.destroy();
	}

	public boolean equals(Object obj) {
		return datasource.equals(obj);
	}

	public JavaScriptObject create() {
		return datasource.create();
	}

	public void setAddGlobalId(Boolean addGlobalId)
			throws IllegalStateException {
		datasource.setAddGlobalId(addGlobalId);
	}

	public Boolean getAddGlobalId() {
		return datasource.getAddGlobalId();
	}

	public void setAutoCacheAllData(Boolean autoCacheAllData) {
		datasource.setAutoCacheAllData(autoCacheAllData);
	}

	public Boolean getAutoCacheAllData() {
		return datasource.getAutoCacheAllData();
	}

	public void setAutoConvertRelativeDates(Boolean autoConvertRelativeDates)
			throws IllegalStateException {
		datasource.setAutoConvertRelativeDates(autoConvertRelativeDates);
	}

	public Boolean getAutoConvertRelativeDates() {
		return datasource.getAutoConvertRelativeDates();
	}

	public String toString() {
		return datasource.toString();
	}

//	public void setAutoDeriveSchema(Boolean autoDeriveSchema)
//			throws IllegalStateException {
//		datasource.setAutoDeriveSchema(autoDeriveSchema);
//	}
//
//	public Boolean getAutoDeriveSchema() {
//		return datasource.getAutoDeriveSchema();
//	}

	public void setAutoDeriveTitles(Boolean autoDeriveTitles)
			throws IllegalStateException {
		datasource.setAutoDeriveTitles(autoDeriveTitles);
	}

	public Boolean getAutoDeriveTitles() {
		return datasource.getAutoDeriveTitles();
	}

	public void setProperty(String property, String value) {
		datasource.setProperty(property, value);
	}

	public void setProperty(String property, boolean value) {
		datasource.setProperty(property, value);
	}

	public void setProperty(String property, double value) {
		datasource.setProperty(property, value);
	}

	public void setProperty(String property, JavaScriptObject value) {
		datasource.setProperty(property, value);
	}

	public void fireEvent(GwtEvent<?> event) {
		datasource.fireEvent(event);
	}

	public int getHandlerCount(Type<?> type) {
		return datasource.getHandlerCount(type);
	}

	public void setCacheAllData(Boolean cacheAllData) {
		datasource.setCacheAllData(cacheAllData);
	}

	public Boolean getCacheAllData() {
		return datasource.getCacheAllData();
	}


	public Record[] getCacheData() {
		return datasource.getCacheData();
	}

	public void setCacheMaxAge(int cacheMaxAge) {
		datasource.setCacheMaxAge(cacheMaxAge);
	}

	public int getCacheMaxAge() {
		return datasource.getCacheMaxAge();
	}

	public void setCallbackParam(String callbackParam)
			throws IllegalStateException {
		datasource.setCallbackParam(callbackParam);
	}

	public String getCallbackParam() {
		return datasource.getCallbackParam();
	}

	public void setCanMultiSort(Boolean canMultiSort)
			throws IllegalStateException {
		datasource.setCanMultiSort(canMultiSort);
	}

	public Boolean getCanMultiSort() {
		return datasource.getCanMultiSort();
	}

	public void setChildrenField(String childrenField)
			throws IllegalStateException {
		datasource.setChildrenField(childrenField);
	}

	public String getChildrenField() {
		return datasource.getChildrenField();
	}

	public void setClientOnly(Boolean clientOnly) throws IllegalStateException {
		datasource.setClientOnly(clientOnly);
	}

	public Boolean getClientOnly() {
		return datasource.getClientOnly();
	}

	public void setCriteriaPolicy(CriteriaPolicy criteriaPolicy) {
		datasource.setCriteriaPolicy(criteriaPolicy);
	}

	public CriteriaPolicy getCriteriaPolicy() {
		return datasource.getCriteriaPolicy();
	}

	public void setDataFormat(DSDataFormat dataFormat)
			throws IllegalStateException {
		datasource.setDataFormat(dataFormat);
	}

	public DSDataFormat getDataFormat() {
		return datasource.getDataFormat();
	}

	public void setDataTransport(RPCTransport dataTransport)
			throws IllegalStateException {
		datasource.setDataTransport(dataTransport);
	}

	public RPCTransport getDataTransport() {
		return datasource.getDataTransport();
	}

	public void setDataURL(String dataURL) throws IllegalStateException {
		datasource.setDataURL(dataURL);
	}

	public String getDataURL() {
		return datasource.getDataURL();
	}

	public void setDropExtraFields(Boolean dropExtraFields)
			throws IllegalStateException {
		datasource.setDropExtraFields(dropExtraFields);
	}

	public Boolean getDropExtraFields() {
		return datasource.getDropExtraFields();
	}

	public void setEnumConstantProperty(String enumConstantProperty)
			throws IllegalStateException {
		datasource.setEnumConstantProperty(enumConstantProperty);
	}

	public void setEnumOrdinalProperty(String enumOrdinalProperty)
			throws IllegalStateException {
		datasource.setEnumOrdinalProperty(enumOrdinalProperty);
	}

	public void setEnumTranslateStrategy(
			EnumTranslateStrategy enumTranslateStrategy)
			throws IllegalStateException {
		datasource.setEnumTranslateStrategy(enumTranslateStrategy);
	}

	public void setIconField(String iconField) throws IllegalStateException {
		datasource.setIconField(iconField);
	}

	public String getIconField() {
		return datasource.getIconField();
	}

	public void setJsonPrefix(String jsonPrefix) throws IllegalStateException {
		datasource.setJsonPrefix(jsonPrefix);
	}

	public String getJsonPrefix() {
		return datasource.getJsonPrefix();
	}

	public void setJsonSuffix(String jsonSuffix) throws IllegalStateException {
		datasource.setJsonSuffix(jsonSuffix);
	}

	public String getJsonSuffix() {
		return datasource.getJsonSuffix();
	}

	public void setPluralTitle(String pluralTitle) throws IllegalStateException {
		datasource.setPluralTitle(pluralTitle);
	}

	public String getPluralTitle() {
		return datasource.getPluralTitle();
	}

	public void setPreventHTTPCaching(Boolean preventHTTPCaching)
			throws IllegalStateException {
		datasource.setPreventHTTPCaching(preventHTTPCaching);
	}

	public Boolean getPreventHTTPCaching() {
		return datasource.getPreventHTTPCaching();
	}

	public void setQualifyColumnNames(Boolean qualifyColumnNames)
			throws IllegalStateException {
		datasource.setQualifyColumnNames(qualifyColumnNames);
	}

	public Boolean getQualifyColumnNames() {
		return datasource.getQualifyColumnNames();
	}

	public void setRecordXPath(String recordXPath) throws IllegalStateException {
		datasource.setRecordXPath(recordXPath);
	}

	public String getRecordXPath() {
		return datasource.getRecordXPath();
	}

	public void setRequiredMessage(String requiredMessage) {
		datasource.setRequiredMessage(requiredMessage);
	}

	public String getRequiredMessage() {
		return datasource.getRequiredMessage();
	}

	public void setResultBatchSize(int resultBatchSize) {
		datasource.setResultBatchSize(resultBatchSize);
	}

	public int getResultBatchSize() {
		return datasource.getResultBatchSize();
	}

	public String getSchemaNamespace() throws IllegalStateException {
		return datasource.getSchemaNamespace();
	}

	public void setSendExtraFields(Boolean sendExtraFields)
			throws IllegalStateException {
		datasource.setSendExtraFields(sendExtraFields);
	}

	public Boolean getSendExtraFields() {
		return datasource.getSendExtraFields();
	}

//	public void setServerConstructor(String serverConstructor)
//			throws IllegalStateException {
//		datasource.setServerConstructor(serverConstructor);
//	}
//
//	public String getServerConstructor() {
//		return datasource.getServerConstructor();
//	}

	public void setServiceNamespace(String serviceNamespace)
			throws IllegalStateException {
		datasource.setServiceNamespace(serviceNamespace);
	}

	public String getServiceNamespace() {
		return datasource.getServiceNamespace();
	}

	public void setShowLocalFieldsOnly(Boolean showLocalFieldsOnly)
			throws IllegalStateException {
		datasource.setShowLocalFieldsOnly(showLocalFieldsOnly);
	}

	public Boolean getShowLocalFieldsOnly() {
		return datasource.getShowLocalFieldsOnly();
	}

	public void setShowPrompt(Boolean showPrompt) {
		datasource.setShowPrompt(showPrompt);
	}

	public Boolean getShowPrompt() {
		return datasource.getShowPrompt();
	}

	public void setStrictSQLFiltering(Boolean strictSQLFiltering)
			throws IllegalStateException {
		datasource.setStrictSQLFiltering(strictSQLFiltering);
	}

	public Boolean getStrictSQLFiltering() {
		return datasource.getStrictSQLFiltering();
	}

	public void setTagName(String tagName) throws IllegalStateException {
		datasource.setTagName(tagName);
	}

	public String getTagName() {
		return datasource.getTagName();
	}

	public void setTitle(String title) {
		datasource.setTitle(title);
	}

	public String getTitle() {
		return datasource.getTitle();
	}

	public void setTitleField(String titleField) throws IllegalStateException {
		datasource.setTitleField(titleField);
	}

	public String getTitleField() {
		return datasource.getTitleField();
	}

	public void setUseFlatFields(Boolean useFlatFields)
			throws IllegalStateException {
		datasource.setUseFlatFields(useFlatFields);
	}

	public Boolean getUseFlatFields() {
		return datasource.getUseFlatFields();
	}

	public void setUseHttpProxy(Boolean useHttpProxy)
			throws IllegalStateException {
		datasource.setUseHttpProxy(useHttpProxy);
	}

	public Boolean getUseHttpProxy() {
		return datasource.getUseHttpProxy();
	}

	public void setUseLocalValidators(Boolean useLocalValidators) {
		datasource.setUseLocalValidators(useLocalValidators);
	}

	public Boolean getUseLocalValidators() {
		return datasource.getUseLocalValidators();
	}

	public void setUseParentFieldOrder(Boolean useParentFieldOrder)
			throws IllegalStateException {
		datasource.setUseParentFieldOrder(useParentFieldOrder);
	}

	public Boolean getUseParentFieldOrder() {
		return datasource.getUseParentFieldOrder();
	}

	public void setUseTestDataFetch(Boolean useTestDataFetch) {
		datasource.setUseTestDataFetch(useTestDataFetch);
	}

	public Boolean getUseTestDataFetch() {
		return datasource.getUseTestDataFetch();
	}

	public void setValidateRelatedRecords(Boolean validateRelatedRecords)
			throws IllegalStateException {
		datasource.setValidateRelatedRecords(validateRelatedRecords);
	}

	public Boolean getValidateRelatedRecords() {
		return datasource.getValidateRelatedRecords();
	}

	public Criteria convertRelativeDates(Criteria criteria) {
		return datasource.convertRelativeDates(criteria);
	}

	public Criteria convertRelativeDates(Criteria criteria,
			String timezoneOffset, int firstDayOfWeek) {
		return datasource.convertRelativeDates(criteria, timezoneOffset,
				firstDayOfWeek);
	}

	public DataSourceField getField(String fieldName) {
		return datasource.getField(fieldName);
	}

	public void getLegalChildTags() {
		datasource.getLegalChildTags();
	}

	public DataSourceField getPrimaryKeyField() {
		return datasource.getPrimaryKeyField();
	}

	public String getPrimaryKeyFieldName() {
		return datasource.getPrimaryKeyFieldName();
	}

	public HandlerRegistration addHandleErrorHandler(HandleErrorHandler handler) {
		return datasource.addHandleErrorHandler(handler);
	}

	public Boolean hasAllData() {
		return datasource.hasAllData();
	}

	public void invalidateCache() {
		datasource.invalidateCache();
	}

	public void performCustomOperation(String operationId, Record data,
			DSCallback callback, DSRequest requestProperties) {
		datasource.performCustomOperation(operationId, data, callback,
				requestProperties);
	}

	public void supportsTextMatchStyle(TextMatchStyle textMatchStyle) {
		datasource.supportsTextMatchStyle(textMatchStyle);
	}

	public void updateCaches(DSResponse dsResponse) {
		datasource.updateCaches(dsResponse);
	}

	public void updateCaches(DSResponse dsResponse, DSRequest dsRequest) {
		datasource.updateCaches(dsResponse, dsRequest);
	}

	public void setDataProtocol(DSProtocol dataProtocol)
			throws IllegalStateException {
		datasource.setDataProtocol(dataProtocol);
	}

	public void setDefaultParams(Map<?, ?> defaultParams) {
		datasource.setDefaultParams(defaultParams);
	}

	public Map<?, ?> getDefaultParams() {
		return datasource.getDefaultParams();
	}

	public DSProtocol getDataProtocol() {
		return datasource.getDataProtocol();
	}

	public boolean isCreated() {
		return datasource.isCreated();
	}

	public JavaScriptObject getJsObj() {
		return datasource.getJsObj();
	}

	public void setRequestProperties(DSRequest requestProperties)
			throws IllegalStateException {
		datasource.setRequestProperties(requestProperties);
	}

	public void setInheritsFrom(DataSource inheritsFrom)
			throws IllegalStateException {
		datasource.setInheritsFrom(inheritsFrom);
	}

	public void setFields(DataSourceField... fields)
			throws IllegalStateException {
		datasource.setFields(fields);
	}

	public void addField(DataSourceField field) throws IllegalStateException {
		datasource.addField(field);
	}

	public DataSourceField[] getFields() {
		return datasource.getFields();
	}

	public Object getDisplayValue(String fieldName, Object value) {
		return datasource.getDisplayValue(fieldName, value);
	}

	public void setTestData(DataClass[] testData) {
		datasource.setTestData(testData);
	}

	public DataClass[] getTestData() {
		return datasource.getTestData();
	}

	public void setOperationBindings(OperationBinding... operationBindings)
			throws IllegalStateException {
		datasource.setOperationBindings(operationBindings);
	}

	public void setRecordName(String recordName) throws IllegalStateException {
		datasource.setRecordName(recordName);
	}

	public void setXmlNamespaces(XmlNamespaces xmlNamespaces)
			throws IllegalStateException {
		datasource.setXmlNamespaces(xmlNamespaces);
	}

	public void fetchData(Criteria criteria, DSCallback callback) {
		datasource.fetchData(criteria, callback);
	}

	public void fetchData(Criteria criteria, DSCallback callback,
			DSRequest requestProperties) {
		datasource.fetchData(criteria, callback, requestProperties);
	}


	public void filterData(Criteria criteria, DSCallback callback) {
		datasource.filterData(criteria, callback);
	}

	public void filterData(Criteria criteria, DSCallback callback,
			DSRequest requestProperties) {
		datasource.filterData(criteria, callback, requestProperties);
	}

	public void addData(Record record) {
		datasource.addData(record);
	}

	public void addData(Record record, DSCallback callback) {
		datasource.addData(record, callback);
	}

	public void addData(Record record, DSCallback callback,
			DSRequest requestProperties) {
		datasource.addData(record, callback, requestProperties);
	}

	public void updateData(Record record) {
		datasource.updateData(record);
	}

	public void updateData(Record record, DSCallback callback) {
		datasource.updateData(record, callback);
	}

	public void updateData(Record record, DSCallback callback,
			DSRequest requestProperties) {
		datasource.updateData(record, callback, requestProperties);
	}

	public void removeData(Record record) {
		datasource.removeData(record);
	}

	public void removeData(Record record, DSCallback callback) {
		datasource.removeData(record, callback);
	}

	public void removeData(Record record, DSCallback callback,
			DSRequest requestProperties) {
		datasource.removeData(record, callback, requestProperties);
	}

	public String xmlSerialize(JavaScriptObject data) {
		return datasource.xmlSerialize(data);
	}

	public String xmlSerialize(JavaScriptObject data, SerializationContext flags) {
		return datasource.xmlSerialize(data, flags);
	}

	public void processResponse(String requestId, DSResponse responseProperties) {
		datasource.processResponse(requestId, responseProperties);
	}

	public Record[] recordsFromXML(Object elements) {
		return datasource.recordsFromXML(elements);
	}

	public String[] getFieldNames() {
		return datasource.getFieldNames();
	}

	public String[] getFieldNames(boolean excludeHidden) {
		return datasource.getFieldNames(excludeHidden);
	}

	public void downloadFile(Record data) {
		datasource.downloadFile(data);
	}

	public void downloadFile(Record data, String fieldName,
			DSRequest requestProperties) {
		datasource.downloadFile(data, fieldName, requestProperties);
	}

	public void viewFile(Record data) {
		datasource.viewFile(data);
	}

	public void viewFile(Record data, String fieldName,
			DSRequest requestProperties) {
		datasource.viewFile(data, fieldName, requestProperties);
	}

	public void exportClientData(Object[] data, DSRequest requestProperties) {
		datasource.exportClientData(data, requestProperties);
	}

	public String getID() {
		return datasource.getID();
	}

	public String getAttribute(String attribute) {
		return datasource.getAttribute(attribute);
	}

	public String getAttributeAsString(String property) {
		return datasource.getAttributeAsString(property);
	}

	public Date getAttributeAsDate(String property) {
		return datasource.getAttributeAsDate(property);
	}

	public Integer getAttributeAsInt(String property) {
		return datasource.getAttributeAsInt(property);
	}

	public Double getAttributeAsDouble(String property) {
		return datasource.getAttributeAsDouble(property);
	}

	public Element getAttributeAsElement(String property) {
		return datasource.getAttributeAsElement(property);
	}

	public JavaScriptObject getAttributeAsJavaScriptObject(String property) {
		return datasource.getAttributeAsJavaScriptObject(property);
	}

	public Float getAttributeAsFloat(String property) {
		return datasource.getAttributeAsFloat(property);
	}

	public Boolean getAttributeAsBoolean(String property) {
		return datasource.getAttributeAsBoolean(property);
	}

	public Map getAttributeAsMap(String property) {
		return datasource.getAttributeAsMap(property);
	}

	public void setAttribute(String attribute, String value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, Boolean value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, Map value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, int[] value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, BaseClass[] value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, DataClass[] value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setCacheData(Record... cacheData) {
		datasource.setCacheData(cacheData);
	}

	public void setAttribute(String attribute, double value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, int value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, Date value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, ValueEnum[] value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, DataClass value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, JavaScriptObject value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, String[] value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setAttribute(String attribute, Element value,
			boolean allowPostCreate) {
		datasource.setAttribute(attribute, value, allowPostCreate);
	}

	public void setDataField(String dataField) throws IllegalStateException {
		datasource.setDataField(dataField);
	}

	public String getDataField() {
		return datasource.getDataField();
	}

	public void setDescriptionField(String descriptionField)
			throws IllegalStateException {
		datasource.setDescriptionField(descriptionField);
	}

	public String getDescriptionField() {
		return datasource.getDescriptionField();
	}

	public void setGlobalNamespaces(Map globalNamespaces) {
		datasource.setGlobalNamespaces(globalNamespaces);
	}

	public Map getGlobalNamespaces() {
		return datasource.getGlobalNamespaces();
	}

	public void setInfoField(String infoField) throws IllegalStateException {
		datasource.setInfoField(infoField);
	}

	public String getInfoField() {
		return datasource.getInfoField();
	}

	public void setInheritsFrom(String inheritsFrom)
			throws IllegalStateException {
		datasource.setInheritsFrom(inheritsFrom);
	}

	public String getInheritsFrom() {
		return datasource.getInheritsFrom();
	}

	public OperationBinding[] getOperationBindings() {
		return datasource.getOperationBindings();
	}

	public DSRequest getRequestProperties() {
		return datasource.getRequestProperties();
	}

	public void setResultSetClass(String resultSetClass)
			throws IllegalStateException {
		datasource.setResultSetClass(resultSetClass);
	}

	public String getResultSetClass() {
		return datasource.getResultSetClass();
	}

	public void setUseOfflineStorage(Boolean useOfflineStorage) {
		datasource.setUseOfflineStorage(useOfflineStorage);
	}

	public Boolean getUseOfflineStorage() {
		return datasource.getUseOfflineStorage();
	}

	public void setWillHandleAdvancedCriteria(Boolean willHandleAdvancedCriteria) {
		datasource.setWillHandleAdvancedCriteria(willHandleAdvancedCriteria);
	}

	public Boolean getWillHandleAdvancedCriteria() {
		return datasource.getWillHandleAdvancedCriteria();
	}

	public void exportData() {
		datasource.exportData();
	}


	public DataSourceField getFieldForDataPath(String dataPath) {
		return datasource.getFieldForDataPath(dataPath);
	}

	public Boolean supportsAdvancedCriteria() {
		return datasource.supportsAdvancedCriteria();
	}

	public void validateData(Record values) {
		datasource.validateData(values);
	}

	public void validateData(Record values, DSCallback callback,
			DSRequest requestProperties) {
		datasource.validateData(values, callback, requestProperties);
	}

	public void setID(String id) {
		datasource.setID(id);
	}

	public Record[] copyRecords(Record[] records) {
		return datasource.copyRecords(records);
	}

	public Record copyRecord(Record record) {
		return datasource.copyRecord(record);
	}

	public String xmlSerialize(Record data, SerializationContext flags) {
		return datasource.xmlSerialize(data, flags);
	}

	public String xmlSerialize(Record[] data, SerializationContext flags) {
		return datasource.xmlSerialize(data, flags);
	}

	public String xmlSerialize(Map data, SerializationContext flags) {
		return datasource.xmlSerialize(data, flags);
	}


	public void setTypeOperators(FieldType fieldType, OperatorId[] operators) {
		datasource.setTypeOperators(fieldType, operators);
	}

	public OperatorId[] getTypeOperators(FieldType fieldType) {
		return datasource.getTypeOperators(fieldType);
	}

	public OperatorId[] getFieldOperators(String fieldName) {
		return datasource.getFieldOperators(fieldName);
	}

	public OperatorId[] getFieldOperators(DataSourceField field) {
		return datasource.getFieldOperators(field);
	}

	public void setDropUnknownCriteria(Boolean dropUnknownCriteria)
			throws IllegalStateException {
		datasource.setDropUnknownCriteria(dropUnknownCriteria);
	}

	public Boolean getDropUnknownCriteria() {
		return datasource.getDropUnknownCriteria();
	}

	public int compareDates(Date date1, Date date2, String fieldName) {
		return datasource.compareDates(date1, date2, fieldName);
	}

	public void exportData(Criteria criteria, DSRequest requestProperties,
			DSCallback callback) {
		datasource.exportData(criteria, requestProperties, callback);
	}

	public String getFileURL(Record data) {
		return datasource.getFileURL(data);
	}

	public String getFileURL(Record data, String fieldName) {
		return datasource.getFileURL(data, fieldName);
	}

	public String getFileURL(Record data, String fieldName,
			DSRequest requestProperties) {
		return datasource.getFileURL(data, fieldName, requestProperties);
	}

	public Criteria splitCriteria(Criteria criteria, String[] fields) {
		return datasource.splitCriteria(criteria, fields);
	}
		

}

