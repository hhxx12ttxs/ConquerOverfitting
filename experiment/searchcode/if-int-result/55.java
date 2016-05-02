/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
// -*- java -*-
// $Id: ObjectAttr.java 1258 2009-08-07 12:02:50Z vic $
// $Name:  $

package ru.adv.db.config;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.adv.db.adapter.DBAdapter;
import ru.adv.db.adapter.DBAdapterException;
import ru.adv.db.adapter.Types;
import ru.adv.db.base.DBCastException;
import ru.adv.db.base.DBValue;
import ru.adv.db.base.MCast;
import ru.adv.util.BadBooleanException;
import ru.adv.util.ClassCreator;
import ru.adv.util.ErrorCodeException;
import ru.adv.util.StringParser;
import ru.adv.util.UniqId;
import ru.adv.util.XMLObject;

/**
 * ????????????? attr ?? ???????????? ??? ???? ??????
 * 
 * @version $Revision: 1.51 $
 */
public class ObjectAttr implements XMLObject {
	
    private static final String DEFAULT_ATTR_NAME = "default";
	public static final String SEQUENCE_NONE       = "none";
    public static final String SEQUENCE_DEFAULT    = DEFAULT_ATTR_NAME;
    public static final String CALCULATE_ATTR_NAME = "calculate";

    private int                type;
    private ConfigObject       object;
    private Class<?>           calculatedClass;
    private String             formatIn;
    private String             formatOut;
    private String             typeName;
    private String             className;
    private String             label;
    private String             form;
    private String             treePresence;
    private String             occurs;
    private String             inputFilter;
    private String             outputFilter;
    private String             defaultValue;
    private Object             defaultDbValue;
    private Object             defaultJdbcValue;
    private String             treeObject;
    private String             mime;
    private String             vid;
    private String             inline;
    private boolean            index;
    private String             defaultSortCS;
    private boolean            unique;
    private boolean            readonly;
    private boolean            system;
    private String             vidPriority;
    private String             title;
    private String             id;
    private String             sequence;
    private String             calculatedClassName;
    private boolean            isForeign;
    private String             foreignName;
    private String             dbt;
    private int                length;
    private String             precision;
    private String             decimalPlaces;
    private boolean            isVid;
    private boolean            isSelfVid;
    private boolean            isInline;
    private boolean            defaultSortIngnoreCase;
    private String             description;
    private boolean            fullTextSearch;

    /**
     * 
     * @param elem
     * @param object
     */
    protected ObjectAttr(Element elem, ConfigObject object) throws DBAdapterException, DBConfigException {
        id = getString(elem, "id");
        type = DBAdapter.getTypeByDBT(elem.getAttribute("dbt"));
        this.object = object;
        dbt = getString(elem, "dbt");
        className = getString(elem, "class");
        typeName = getString(elem, "type");
        label = getString(elem, "label");
        form = getString(elem, "form");
        treePresence = getString(elem, "tree-presence");
        occurs = getString(elem, "occurs");
        inputFilter = getString(elem, "input-filter");
        outputFilter = getString(elem, "output-filter");
        treeObject = getString(elem, "tree-object");
        mime = getString(elem, "mime");
        inline = getString(elem, "inline");
        /*_isInline = (_inline != null && _inline.equals("yes")) ? true : false; */
        isInline = (inline != null && inline.equals("no")) ? false : true; //new version
        
        vid = getString(elem, "vid");
        isVid = (vid != null && vid.equals("self")) ? true : toBoolean(vid);
        isSelfVid = (vid != null && vid.equals("self")) ? true : false;
        
        index = getBoolean(elem, "index");
        // false if "default-sort-cs" not present OR attr is marked as default
        // sort case sensitive
        // true if attr default-sort-cs="no"
        defaultSortCS = getString(elem, "default-sort-cs");
        defaultSortIngnoreCase = defaultSortCS == null || defaultSortCS.equals("yes") ? false : defaultSortCS.equals("no") ? true : false;
        unique = getBoolean(elem, "unique");
        readonly = getBoolean(elem, "readonly");
        system = getBoolean(elem, "system");
        vidPriority = getString(elem, "vid-priority");
        title = getString(elem, "title");
        description = getString(elem, "description");
        formatIn = getString(elem, "format-in");
        formatOut = getString(elem, "format-out");
        sequence = getString(elem, "sequence");
        calculatedClassName = getString(elem, CALCULATE_ATTR_NAME);
        foreignName = getString(elem, "foreign");
        isForeign = foreignName != null;
        length = 0;
        if (getType() == Types.STRING || getType() == Types.STRING) {
            try {
                length = Integer.parseInt(getString(elem, "length"));
            } catch (NumberFormatException e) {
                length = 0;
            }
        }
        precision = getString(elem, "precision");
        decimalPlaces = getString(elem, "decimal-places");
        checkName();
        loadCalculationClass();
        defaultValue = getString(elem, DEFAULT_ATTR_NAME);
        if (defaultValue==null && isFile()) {
        	defaultValue = "false";
        }
        if ( defaultValue != null ) {
        	defaultDbValue = checkDefaultValue( defaultValue );
        	defaultJdbcValue =  getDBConfig().getDBAdapter().custToJdbcValue(createDBValue(defaultDbValue));
        }
        fullTextSearch = "yes".equals(getString(elem, "full-text-search"));
        
        initCalculatedProperties();
        
    }

    public DBConfig getDBConfig() {
        return object != null ? object.getDBConfig() : null;
    }

    public boolean isFullTextSearch() {
        return fullTextSearch;
    }

    private boolean getBoolean(Element element, String key) throws DBConfigException {
        String string = getString(element, key);
        boolean result = false;
        if (string != null) {
            try {
                result = StringParser.toBoolean(string);
            } catch (BadBooleanException e) {
                throw new InvalidBooleanValueException(e, this, key, string);
            }
        }
        return result;
    }

    private boolean toBoolean(String string) {
        boolean result = false;
        if (string != null) {
            try {
                result = StringParser.toBoolean(string);
            } catch (BadBooleanException e) {
                result = false;
            }
        }
        return result;
    }

    private String getString(Element element, String key) {
        String result = null;
        if (element.hasAttribute(key)) {
            result = element.getAttribute(key);
        }
        return result;
    }

    /**
     * Get attribute name.
     */
    public String getName() {
        return id;
    }

    public String getSQLName() {
        return sqlName;
    }

    public String getObjectName() {
        return object.getName();
    }

    public String getSequence() {
        return sequence;
    }

    /**
     * ?????????? ???????? ???????? calculated ??? null
     * 
     * @return
     */
    public String getCalculatedClassName() {
        return calculatedClassName;
    }

    /**
     * ?????????? ???????? ???????? calculated ??? null
     * 
     * @return
     */
    public Class<?> getCalculatedClass() {
    	if (calculatedClass==null) {
    		calculatedClass = ClassCreator.forName(getCalculatedClassName());
    	}
        return calculatedClass;
    }

    /**
     * ???????? ????????? calculated class
     * 
     * @throws DBConfigException
     */
    private void loadCalculationClass() throws DBConfigException {
        if (isCalculated()) {
            try {
                calculatedClass = ClassCreator.forName(getCalculatedClassName());
                ClassCreator.newInstance(calculatedClass);
            } catch (ErrorCodeException e) {
            	if ( ! this.object.getDBConfig().isSoftChecks() ) {
                    throw new DBConfigException(e);
            	}
            }
        }
    }

    /**
     * ???????? ?? ??????? ??????????
     * 
     * @return
     */
    public boolean isCalculated() {
        return getCalculatedClassName() != null || type == Types.CALCULATED;
    }

    /**
     * check for string type.
     * 
     * @return true if dbt=DB.DBT_STRING or DB.DBT_TEXT
     * @see ru.adv.db.adapter.DBAdapter
     */
    public boolean isString() {
    	return isString;
    }

    /**
     * check for boolean type.
     * 
     * @return true if DB.BOOLEAN || DB.FILE
     * @see ru.adv.db.adapter.DBAdapter
     */
    public boolean isBoolean() {
        return isBoolean;
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    /**
     * check for dbt FILE.
     * 
     * @return true if DB.FILE
     * @see ru.adv.db.adapter.DBAdapter
     */
    public boolean isFile() {
        return getType() == DBAdapter.FILE;
    }

    /**
     * true if attr i foreign
     * 
     * @return true if attr id foreign
     */
    public boolean isForeign() {
        return isForeign;
    }

    /**
     * ???????? ???????? <code>tree-object</code> ??????????? ?
     * <code>tree-object=="tag"</code>
     */
    public boolean isTagLayout() {
        return isTagLayout;
    }
    private boolean calculateIsTagLayout() {
        String inTree = getTreeObject();
        return (inTree != null) ? inTree.equals("tag") : isForeign();
    }

    public String getForeignObjectName() {
        return foreignName;
    }

    /**
     * ?????????? ??????? ???????? ? {@link ConfigObject} ??? -1 ??? ??????
     * @return
     */
    public int indexInObject() {
    	if (indexInObject==null) {
    		// Lazy initialization because ConfigObject must be initaialized before 
    		indexInObject = this.object.getAttributeNames().indexOf(getName());
    	}
    	return indexInObject;
    }

    public ConfigObject getForeignObject() throws DBConfigException {
    	if (isForeign() && foreignObject==null) {
    		synchronized (this) {
        		// Lazy initialization because the all ConfigObjects must be initialized before
        		foreignObject = getDBConfig().getConfigObject(getForeignObjectName());
			}
    	}
    	return foreignObject;
    }

    /**
     * true if attr can be NULL; DBT == (boolean|file)=> false (NOT NULL)
     * isSetRequired() => false (NOT NULL) isForeign() => false (NOT NULL)
     * getName().equals(id) => false (NOT NULL)
     * 
     * @return true if attr can be NULL
     */
    public boolean isNullable() {
    	return isNullable;
    }

    /**
     * Get string type constant from {@link ru.adv.db.adapter.Types}
     */
    public String getDBT() {
        return dbt;
    }

    /**
     * Get type constant from {@link ru.adv.db.adapter.Types}
     */
    public int getType() {
        return type;
    }

    public String getSQLType() {
        return sqlType;
    }

    /**
     * true if attr is isForeign() OR occurs="required". ??????? ? ???????
     * ???????? createRequiredProcedure !
     * 
     * @return true if attr can be NULL
     */
    public boolean isRequired() {
    	return isRequired;
    }

    /**
     * ?????????? ???????? ???????? default ??? null
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }
    
    /**
     * ??????????? ? ??????????? ???????? ???????? default ??? null
     */
    public Object getDefaultDbValue() {
        return defaultDbValue;
    }
    
    /**
     * ??????????? ? ??????????? ???????? ???????? default ??? null
     * ??? prepared statement
     */
    public Object getDefaultJdbcValue() {
        return defaultJdbcValue;
    }

    /**
     * true if attr has id == 'tree'
     */
    public boolean isTree() {
    	return isTree;
    }

    /**
     * true if attr must be unique
     * 
     * @return true if attr must be unique
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * true if attr must be indexed
     * 
     * @return true if attr must be indexed
     */
    public boolean isIndex() {
        return index;
    }

    /**
     * true if attr is for system use
     * 
     * @return true if attr is for system use
     */
    public boolean isSystem() {
        return system;
    }

    /**
     * true if attr is vid
     * 
     * @return true if attr is vid
     */
    public boolean isVid() {
        return isVid;
    }
    
    public boolean isInline(){
    	return isInline;
    }

    /**
     * true ???? vid=="self"
     * 
     * @return vid=="self"
     */
    public boolean isSelfVid() {
        return isSelfVid;
    }

    /**
     * false if "default-sort-cs" not present OR attr is marked as default sort
     * case sensitive true if attr default-sort-cs="no"
     * 
     * @return true if attr default-sort-cs="no"
     */
    public boolean isDefaultSortIgnoreCase() {
        return defaultSortIngnoreCase;
    }

    /**
     * true if attr is default for selected from base
     * 
     * @return true if attr is default for selected from base
     */
    public boolean isDefaultSelected() {
    	return defaultSelected;
    }
    
    private boolean calculateIsDefaultSelected() {
        String treePresents = getTreePresence();
        if (getName().equals("id"))
            return true;
        if (getName().equals(ConfigObject.TREE_ATTR_NAME))
            return true;
        if (treePresents != null && treePresents.equals("always"))
            return true;
        if (isVid() && !isForeign() && !(treePresents != null && treePresents.equals("optional"))) {
            return true;
        }
        return false;
    }

    public boolean compareDBSpecificAttributes(ObjectAttr objectAttr) {
        if (dbt != null ? !dbt.equals(objectAttr.dbt) : objectAttr.dbt != null)
            return false;
        if (occurs != null ? !occurs.equals(objectAttr.occurs) : objectAttr.occurs != null)
            return false;
        if (defaultValue != null ? !defaultValue.equals(objectAttr.defaultValue) : objectAttr.defaultValue != null)
            return false;
        if (system != objectAttr.system)
            return false;
        if (length != objectAttr.length)
            return false;
        if (foreignName != null ? !foreignName.equals(objectAttr.foreignName) : objectAttr.foreignName != null)
            return false;
        if (sequence != null ? !sequence.equals(objectAttr.sequence) : objectAttr.sequence != null)
            return false;
        return true;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ObjectAttr))
            return false;

        final ObjectAttr objectAttr = (ObjectAttr) o;

        if (defaultSortCS != objectAttr.defaultSortCS)
            return false;
        if (index != objectAttr.index)
            return false;
        if (length != objectAttr.length)
            return false;
        if (readonly != objectAttr.readonly)
            return false;
        if (system != objectAttr.system)
            return false;
        if (type != objectAttr.type)
            return false;
        if (unique != objectAttr.unique)
            return false;
        if (calculatedClassName != null ? !calculatedClassName.equals(objectAttr.calculatedClassName) : objectAttr.calculatedClassName != null)
            return false;
        if (className != null ? !className.equals(objectAttr.className) : objectAttr.className != null)
            return false;
        if (dbt != null ? !dbt.equals(objectAttr.dbt) : objectAttr.dbt != null)
            return false;
        if (defaultValue != null ? !defaultValue.equals(objectAttr.defaultValue) : objectAttr.defaultValue != null)
            return false;
        if (foreignName != null ? !foreignName.equals(objectAttr.foreignName) : objectAttr.foreignName != null)
            return false;
        if (form != null ? !form.equals(objectAttr.form) : objectAttr.form != null)
            return false;
        if (formatIn != null ? !formatIn.equals(objectAttr.formatIn) : objectAttr.formatIn != null)
            return false;
        if (formatOut != null ? !formatOut.equals(objectAttr.formatOut) : objectAttr.formatOut != null)
            return false;
        if (id != null ? !id.equals(objectAttr.id) : objectAttr.id != null)
            return false;
        if (inputFilter != null ? !inputFilter.equals(objectAttr.inputFilter) : objectAttr.inputFilter != null)
            return false;
        if (label != null ? !label.equals(objectAttr.label) : objectAttr.label != null)
            return false;
        if (mime != null ? !mime.equals(objectAttr.mime) : objectAttr.mime != null)
            return false;
        if (occurs != null ? !occurs.equals(objectAttr.occurs) : objectAttr.occurs != null)
            return false;
        if (outputFilter != null ? !outputFilter.equals(objectAttr.outputFilter) : objectAttr.outputFilter != null)
            return false;
        if (sequence != null ? !sequence.equals(objectAttr.sequence) : objectAttr.sequence != null)
            return false;
        if (title != null ? !title.equals(objectAttr.title) : objectAttr.title != null)
            return false;
        if (treeObject != null ? !treeObject.equals(objectAttr.treeObject) : objectAttr.treeObject != null)
            return false;
        if (treePresence != null ? !treePresence.equals(objectAttr.treePresence) : objectAttr.treePresence != null)
            return false;
        if (typeName != null ? !typeName.equals(objectAttr.typeName) : objectAttr.typeName != null)
            return false;
        if (vid != null ? !vid.equals(objectAttr.vid) : objectAttr.vid != null)
            return false;
        if (vidPriority != null ? !vidPriority.equals(objectAttr.vidPriority) : objectAttr.vidPriority != null)
            return false;
        if (inline != null ? !inline.equals(objectAttr.inline) : objectAttr.inline != null){
        	return false;
        }
        return true;
    }

    public int hashCode() {
        int result;
        result = type;
        result = 29 * result + (formatIn != null ? formatIn.hashCode() : 0);
        result = 29 * result + (formatOut != null ? formatOut.hashCode() : 0);
        result = 29 * result + (typeName != null ? typeName.hashCode() : 0);
        result = 29 * result + (className != null ? className.hashCode() : 0);
        result = 29 * result + (label != null ? label.hashCode() : 0);
        result = 29 * result + (form != null ? form.hashCode() : 0);
        result = 29 * result + (treePresence != null ? treePresence.hashCode() : 0);
        result = 29 * result + (occurs != null ? occurs.hashCode() : 0);
        result = 29 * result + (inputFilter != null ? inputFilter.hashCode() : 0);
        result = 29 * result + (outputFilter != null ? outputFilter.hashCode() : 0);
        result = 29 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 29 * result + (treeObject != null ? treeObject.hashCode() : 0);
        result = 29 * result + (mime != null ? mime.hashCode() : 0);
        result = 29 * result + (vid != null ? vid.hashCode() : 0);
        result = 29 * result + (inline != null ? inline.hashCode() : 0);
        result = 29 * result + (index ? 1 : 0);
        result = 29 * result + (defaultSortCS != null ? defaultSortCS.hashCode() : 0);
        result = 29 * result + (unique ? 1 : 0);
        result = 29 * result + (readonly ? 1 : 0);
        result = 29 * result + (system ? 1 : 0);
        result = 29 * result + (vidPriority != null ? vidPriority.hashCode() : 0);
        result = 29 * result + (title != null ? title.hashCode() : 0);
        result = 29 * result + (id != null ? id.hashCode() : 0);
        result = 29 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 29 * result + (calculatedClassName != null ? calculatedClassName.hashCode() : 0);
        result = 29 * result + (foreignName != null ? foreignName.hashCode() : 0);
        result = 29 * result + (dbt != null ? dbt.hashCode() : 0);
        result = 29 * result + length;
        return result;
    }

    /**
     * true if attr is occurs="required". ??????? ? ??????? ????????
     * createRequiredProcedure ! ?????? ????? ?? ????????????!
     * 
     * @return true if attr length mast be > 0
     */
    private boolean isSetRequired() {
        if (getOccurs() != null) {
            return getOccurs().equals("required");
        }
        return false;
    }

    /**
     * checks if specified value conforms to type of this attribute
     * @return customized checked value.
     */
    public Object checkDefaultValue(Object value) throws DBConfigException {
        try {

            if (value == null || value.toString().length() == 0) {
                throw new EmptyDefaultValueException("Default value for attribute '" + getName() + "' is empty !", this);
            }

            // ???????? ?????????? ???????? ? ????
            DBValue dbValue = createDBValue(value);

            switch (getType()) {
                case DBAdapter.STRING: // flow down
                case DBAdapter.TEXT:
                    if (MCast.toString(value).length() == 0 && isSetRequired()) {
                        throw new EmptyDefaultValueException("Required attribute '" + getName() + "' has empty default value!", this);
                    }
                    break;
                case DBAdapter.BOOLEAN:
                    if (isSetRequired() && !MCast.toBoolean(value).booleanValue()) {
                        throw new InvalidDefaultValueException("Required boolean attribute '" + getName() + "' has 'false' default value!", this, value);
                    }
                    break;
                case DBAdapter.FILE:
                    if (MCast.toBoolean(value).booleanValue()) {
                        throw new InvalidDefaultValueException("Default value for file type attribute '" + getName() + "' must be always 'false'!", this, value);
                    }
                    break;
            }
            return dbValue.get();
            
        } catch (DBCastException e) {
            throw new InvalidDefaultValueException("Invalid default value for " + getObjectName() + "_" + getName() + ": " + e.getMessage(), this, value);
        }
    }

    public boolean isReadonly() {
        return readonly;
    }

    private void checkName() throws DBConfigException {
        if (getDBConfig() == null) {
            DBConfig.isPermitNameForIdentify(getName(), null);
        } else {
            getDBConfig().isPermitNameForIdentify(getName());
        }
        if (isNameTooLong()) {
            throw new AttributeIdTooLongException("Invalid attribute identificator '" + getName() + "': maximum length may be " + getDBConfig().getDBAdapter().getMaxAttributeNameLength(), getObjectName(), getName(), getDBConfig().getId());
        }
    }

    private boolean isNameTooLong() {
        return getDBConfig() != null && getName().length() > getDBConfig().getDBAdapter().getMaxAttributeNameLength();
    }

    public Element toXML(Document doc) {
        Element result = doc.createElement("attr");
        result.setAttribute("id", getName());
        result.setAttribute("dbt", getDBT());
        if (getTypeName() != null) {
            result.setAttribute("type", getTypeName());
        }
        if (getAttrClass() != null) {
            result.setAttribute("class", getAttrClass());
        }
        if (getLabel() != null) {
            result.setAttribute("label", getLabel());
        }
        if (getForm() != null) {
            result.setAttribute("form", getForm());
        }
        if (getTreePresence() != null) {
            result.setAttribute("tree-presence", getTreePresence());
        }
        if (getOccurs() != null) {
            result.setAttribute("occurs", getOccurs());
        }
        if (getInputFilterName() != null) {
            result.setAttribute("input-filter", getInputFilterName());
        }
        if (getOutputFilterName() != null) {
            result.setAttribute("output-filter", getOutputFilterName());
        }
        if (getDefaultValue() != null) {
            result.setAttribute(DEFAULT_ATTR_NAME, getDefaultValue());
        }
        if ( getDefaultDbValue() != null ) {
            result.setAttribute(DEFAULT_ATTR_NAME, getDefaultDbValue().toString());
        }
        
        if (getTreeObject() != null) {
            result.setAttribute("tree-object", getTreeObject());
        }
        if (getMIME() != null) {
            result.setAttribute("mime", getMIME());
        }
        if (getVID() != null) {
            result.setAttribute("vid", getVID());
        }
        if (getInline() != null) {
        	result.setAttribute("inline", getInline());
        }
        if (getIndex())
            result.setAttribute("index", "yes");
        if (getDefaultSortCS() != null)
            result.setAttribute("default-sort-cs", getDefaultSortCS());
        if (getUnique())
            result.setAttribute("unique", "yes");
        if (getReadonly())
            result.setAttribute("readonly", "yes");
        if (getSystem())
            result.setAttribute("system", "yes");
        if (getLength() > 0) {
            result.setAttribute("length", Integer.toString(getLength()));
        }
        if (getForeignObjectName() != null) {
            result.setAttribute("foreign", getForeignObjectName());
        }
        if (getVIDPriority() != null) {
            result.setAttribute("vid-priority", getVIDPriority());
        }
        if (getTitle() != null) {
            result.setAttribute("title", getTitle());
        }
        if (getSequence() != null) {
            result.setAttribute("sequence", getSequence());
        }
        if (getCalculatedClassName() != null) {
            result.setAttribute("calculate", getCalculatedClassName());
        }
        if (getFormatIn() != null) {
            result.setAttribute("format-in", getFormatIn());
        }
        if (getFormatOut() != null) {
            result.setAttribute("format-out", getFormatOut());
        }
        if (getDescription() != null) {
            result.setAttribute("description", getDescription());
        }
        return result;
    }

    private String getTitle() {
        return title;
    }

    private String getDescription() {
        return description;
    }

    public String getVIDPriority() {
        return vidPriority;
    }

    private boolean getSystem() {
        return system;
    }

    private boolean getReadonly() {
        return readonly;
    }

    private boolean getUnique() {
        return unique;
    }

    private String getDefaultSortCS() {
        return defaultSortCS;
    }

    private boolean getIndex() {
        return index;
    }

    private String getVID() {
        return vid;
    }
    
    private String getInline(){
    	return inline;
    }

    public String getMIME() {
        return mime;
    }

    private String getTreeObject() {
        return treeObject;
    }

    public String getInputFilterName() {
        return inputFilter;
    }

    public String getOutputFilterName() {
        return outputFilter;
    }

    private String getOccurs() {
        return occurs;
    }

    private String getTreePresence() {
        return treePresence;
    }

    private String getForm() {
        return form;
    }

    private String getLabel() {
        return label;
    }

    private String getAttrClass() {
        return className;
    }

    private String getTypeName() {
        return typeName;
    }

    public String toString() {
    	return toStringValue;
    }

    public DBValue createDBValue(Object content) throws DBCastException {
        return DBValue.createInstance(getType(), content);
    }

    public DBValue createDBValue(ResultSet rs, int pos) throws DBCastException, SQLException {
        return DBValue.createInstance(getType(), rs, pos);
    }

    public Object generateDefautValue() {
        Object result = null;
        switch (type) {
            case Types.STRING:
            case Types.TEXT:
                String value;
                if (isUnique()) {
                    value = UniqId.getUUID();
                } else {
                    value = getName();
                }
                if (getLength() < value.length()) {
                    value = value.substring(0, getLength());
                }
                result = value;
                break;
            case Types.DATE:
                result = new Date(0);
                break;
            case Types.DOUBLE:
                result = new Double(1);
                break;
            case Types.FLOAT:
            case Types.NUMERIC:
                result = new Float(1);
                break;

            case Types.BOOLEAN:
            case Types.FILE:
            case Types.INT:
                result = new Integer(1);
                break;
            case Types.LONG:
                result = new Long(1);
                break;
            case Types.SHORTINT:
                result = new Short((short) 1);
                break;

            case Types.TIMESTAMP:
                result = new Timestamp(0);
                break;
        }
        return result;
    }

    public int getLength() {
        return length;
    }

    public boolean isAutoGenerated() {
        return autoGenerated; 
    }

    public String getFormatIn() {
        return formatIn;
    }

    public String getFormatOut() {
        return formatOut;
    }

    public String getPrecision() {
        return precision;
    }

    public String getDecimalPlaces() {
        return decimalPlaces;
    }

    void setFormatIn(String formatIn) {
        if (this.formatIn == null && formatIn != null) {
            this.formatIn = formatIn;
        }
    }

    void setFormatOut(String formatOut) {
        if (this.formatOut == null && formatOut != null) {
            this.formatOut = formatOut;
        }
    }

    public String getAttrStoragePath() {
        return attrStoragePath;
    }

    public ConfigObject getConfigObject() {
        return object;
    }
    
    /*
     *  calculate and cached properties
     */
    private String attrStoragePath;
    private ConfigObject foreignObject;
    private String sqlName;
    private String sqlType;
    private Integer indexInObject = null;
    private boolean autoGenerated;
    private boolean isBoolean;
    private boolean isDateTime;
    private boolean defaultSelected;
    private boolean isNullable;
    private boolean isRequired;
    private boolean isString;
    private boolean isTagLayout;
    private boolean isTree;
    private String toStringValue;
    
    private void initCalculatedProperties() {
    	toStringValue = getObjectName() + "_" + getName();
    	isTree = getName().equals(ConfigObject.TREE_ATTR_NAME);
    	attrStoragePath = object.getRealName() + "/" + getName();
        sqlName = getDBConfig().getDBAdapter().getSQLIdentifier(this);
        
        if (!isCalculated()) {
        	sqlType = getDBConfig().getDBAdapter().getSQLType(this);
        }
        
        autoGenerated = getName().equals("id") || getName().equals("objversion");
        isBoolean = getType() == DBAdapter.BOOLEAN || getType() == DBAdapter.FILE;
        isDateTime = getType() == DBAdapter.DATE || getType() == DBAdapter.TIMESTAMP;
        defaultSelected = calculateIsDefaultSelected();
        
        isNullable = !( isSetRequired() || isBoolean() || isForeign() || getName().equals("id") );
        isRequired = isSetRequired() || isForeign();
        isString = DBAdapter.isStringType(getDBT());
        isTagLayout = calculateIsTagLayout();
    }

    
}

