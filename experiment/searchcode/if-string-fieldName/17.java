/**
 * Copyright 2011-2012 Joonas Keturi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vaadin.objectview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.wsdl.WSDLException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.ClassResource;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormFieldFactory;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.LegacyWindow;
import com.vaadin.ui.Link;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.Reindeer;

import org.apache.axis2.AxisFault;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.util.ClassLoaderRepository;
import org.apache.bcel.util.Repository;

import org.vaadin.common.Support;

import org.xml.sax.SAXException;

/**
 * <pre>
 * User interface
 *
 * <i>type</i> get<i>FieldName</i>()
 * void set<i>FieldName</i>(<i>type</i> <i>FieldName</i>)
 * void edit<i>FieldName</i>(<i>type</i> <i>FieldName</i>)
 * void validate<i>FieldName</i>(Object <i>FieldName</i>)
 * boolean disabled<i>FieldName</i>()
 * boolean invisible<i>FieldName</i>()

 * If <i>FieldName</i> is prefixed with word Form, field is not used in tables.
 * If <i>FieldName</i> is prefixed with word Optional, field is not required.
 * If <i>FieldName</i> starts with words Text, Title or MainTitle,
 * field is not input field, but returns translatable text.
 * If <i>FieldName</i> starts with word Group and is a collection type
 * or has value list, it is shown as separate selections.
 * If <i>FieldName</i> ends with word Password and is a string,
 * it is shown as password field with covered characters.
 * If <i>FieldName</i> ends with word ApplicationWindow and is an object,
 * it is a native browser window.
 * If <i>FieldName</i> ends only with word Window and is an object,
 * it is a subwindow inside main window.
 * Windows can be opened giving their path name
 * in list of component names to be updated in other methods.
 * If <i>FieldName</i> is prefixed with word Volatile, it is not
 * stored in database if persistence is used.
 * If <i>FieldName</i> is prefixed with word Storage or Model, it is only
 * stored in database when persisted or used only in web services
 * and not shown in user interface.
 * Prefix words must appear in order storage, link, form, optional, volatile.
 * If field has no set method, it is read only field.
 * If type is a collection, it must be in return type of get-method a separate class
 * extended from collection type and having element class
 * as type parameter. In set- and edit-methods this type must be a plain collection
 * type with type parameter. For example:
 *
 * public static class SomeSet extends HashSet&lt;String&gt; {
 * }
 *
 * SomeSet getSomeSet()
 * void setSomeSet(Set&lt;String&gt; someSet)
 * void editSomeSet(Set&lt;String&gt; someSet)
 *
 * type :=
 * boolean                    : check box
 * extends java.util.Calendar : calendar with date and time
 * extends java.util.Date     : calendar with date and time
 * java.sql.Date              : calendar with date without time
 * char                       : string with length of one
 * byte, short, integer, long : integer text field
 * float, double              : floating point text field
 * java.math.BigDecimal       : decimal number text field
 * java.math.BigInteger       : integer text field
 * enumeration                : selection box or group with values of enumeration
 * String                     : text field
 * List, array                : table
 * Set                        : multiple selection box or group
 *
 * Methods without get- or set-verbs, represent actions, and are shown as buttons
 * in the user interface. For example
 *
 * public String[] action() {...
 *
 * Action methods return array of strings. First string can be path name of component
 * to navigate after action. Path name of component is a dot separated list of
 * simple class names (without package names) and field names with small first letter
 * from first component to target component.
 * Other strings may be path names of components which should be updated after action.
 * First string can also be null, and no navigation will occur. Return value
 * of method can be null also, and no navigation will occur, and no fields will
 * be updated.
 * If field is a table, and the field name starts with word Form, it is
 * sequence of forms.
 * If the field name starts with word Link, it is a sequence of links.
 * If the field name starts with word Tab, it is a sequence of tabs in tab sheet.
 * Otherwise it is a table of element type whose class is given
 * in the type parameter or in the array member type.
 * Every class which represents a tab, may contain method 'start':
 *
 * public String[] start()
 *
 * When user navigates to this tab or when the tab is created, this method is
 * always called. It can return array of strings containing path names of components
 * which should be updated in user interface. It can return also null, if update is
 * not needed. This method is not mandatory.
 *
 * <i>type</i>[] values<i>FieldName</i>()
 * values of selection box or group without enumeration type
 *
 * <i>type</i> getLink<i>FieldName</i>()
 *   link type with methods:
 * String navigate()
 *   returns path name of component to navigate.
 * String getSource()
 *   returns URL to navigate
 * String getTarget()
 *   returns window target name where to open source URL
 * String getName()
 *   returns name of link which can be translated
 *
 * <i>MenuList</i> get<i>FieldName</i>Menu()
 *   menu with methods:
 * String getName()
 *   returns name of menu item which can be translated
 * String getIcon()
 *   returns URL for menu item icon if it contains colon, otherwise path from class loader.
 *   This method is optional or it can return null
 * <i>MenuList</i> getMenu()
 *   returns menu item list. This is menu list below menu bar, or sub menu item list
 *   This method is optional or it can return null
 * String[] execute()
 *   executes menu item action. Return list operates as in action methods.
 *
 * String icon<i>FieldName</i>()
 *   returns URL for button icon if it contains colon, otherwise path from class loader
 *
 * If getter-method returns object, which is in package com.vaadin or org.vaadin,
 * it must inherit class com.vaadin.ui.AbstractComponent, and it is added
 * to user interface as such.
 * With this feature it is possible for example to add Embedded-components like images to user interface,
 * and any custom component, like some add-on component.
 * 
 * boolean disabled()
 *  If form object is disabled.
 * boolean invisible()
 *  If form object is invisible.
 * ObjectView.LayoutType layoutType()
 *  Layout type of form object.
 * boolean sizeFull()
 *  Form object will use full window size.
 *
 * Methods with field name locale handles locale changes.
 * public void setLocale(final String locale) {
 * public String getLocale() {
 * public String[] editLocale(final String locale) {
 * public String[] valuesLocale() {
 * Locale values must be of format language_country_variant (two last elements optional)
 *
 * If object has method of signature:
 * public void themes(final String[] themeValues) {
 * This method receives available theme names.
 *
 * Methods with field name theme handles theme changes.
 * public void setTheme(final String theme) {
 * public String getTheme() {
 * public String[] editTheme(final String theme) {
 * public String[] valuesTheme() {
 *
 * Persistence
 *
 * These methods must be defined in main class if persistence is required:
 *
 * import org.vaadin.objectview.ObjectView.StorageType;
 *
 * public StorageType storageType() {
 *      return StorageType.TDB; // gives type of storage, resource description framework database
 * }
 *
 * public String storageDirectory() {
 *     return new File(System.getProperty("user.home"), "storage").getPath(); // gives directory of database files
 * }
 *
 * These methods can be defined in any user interface class for persistence:
 *
 * public String id()
 * public <i>StorageObjectType</i> getStartSearch()
 * public void setStartSearch(final <i>StorageObjectType</i> <i>name</i>)
 * public <i>StorageObjectType</i> get<i>FieldName</i>StoreSearch()
 * public void set<i>FieldName</i>StoreSearch(<i>StorageObjectType</i>)
 * public <i>StorageObjectType</i> get<i>FieldName</i>Store()
 * public String[] <i>FieldName</i>Store()
 * public <i>StorageObjectType</i> get<i>FieldName</i>Search()
 * public void set<i>FieldName</i>Search(final <i>StorageObjectType</i> <i>FieldName</i>)
 * public String[] <i>FieldName</i>Search()
 * public <i>StorageObjectType</i> get<i>FieldName</i>Remove()
 * public String[] <i>FieldName</i>Remove()
 *
 * id-method can return resource identifier for object. If this method is missing
 * identifier is same as class name, and there will be only one instance in database.
 * <i>StorageObjectType</i> must be a class defined separately and implementing
 * java.util.List and having element class as type parameter, or it must be
 * a class having getter- and setter-methods for different types of element lists.
.* For example:
 * 
 * public static class StorageList extends ArrayList&lt;<i>ObjectType</i>&gt; {
 * }
 *
 * public static class StorageObjectType {
 *
 * StorageList getStorageList()
 * void setStorageList(StorageList storageList)
 *
 * }
 *
 * Accessor-methods marked with Search-word must be return object of this type which is
 * used as search parameters for database. Every object in list is used separately
 * as search parameters making conjunction of all non-null fields. Result of
 * these searches are added to list of the same type. Different object types can be searched
 * when search-method returns object containing different list types.
 * Search-method with Start-word is called when user interface object is first
 * created, and afterwards every time when user interface object is selected as one
 * tab in tab sheet.
 * Other search-methods are called when button is pressed connected to method whose name starts with
 * same field name as in search-method, like:
 *
 * public String[] <i>FieldName</i>Search()
 *
 * Also they are called when field with same name as in search-method is changed. For example:
 *
 * public SomeList get<i>FieldName</i>()
 *
 * Results of search-methods are returned to calling object with corresponding set-method.
 * When button method has corresponding get-method marked with Store-word, it is called
 * to get fields to be stored in database.
 * When button method has corresponding get-method marked with Remove-word, it is called
 * to get fields to be removed from database.
 *
 * Web services
 *
 * This method must be defined in main class if web services are required:
 *
 * public String namespace() {
 *      return "http://dummy.org"; // gives namespace for web services
 * }
 *
 * These methods can be defined in any user interface class for web services:
 *
 * public <i>WebRequestObjectType</i> get<i>FieldName</i>Request()
 * public void set<i>FieldName</i>Request(final <i>WebRequestObjectType</i> <i>FieldName</i>)
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>Response()
 * public void set<i>FieldName</i>Response(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>RetrieveSearch()
 * public void set<i>FieldName</i>RetrieveSearch(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public void <i>FieldName</i>Retrieve()
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>UpdateSearch()
 * public void set<i>FieldName</i>UpdateSearch(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public void <i>FieldName</i>Update()
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>AddSearch()
 * public void set<i>FieldName</i>AddSearch(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public void <i>FieldName</i>Add()
 * public void <i>FieldName</i>Delete()
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>ClientRetrieveSearch()
 * public void set<i>FieldName</i>ClientRetrieveSearch(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public void <i>FieldName</i>ClientRetrieve()
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>ClientUpdateSearch()
 * public void set<i>FieldName</i>ClientUpdateSearch(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public void <i>FieldName</i>]ClientUpdate()
 * public <i>WebRequestObjectListType</i> get<i>FieldName</i>ClientAddSearch()
 * public void set<i>FieldName</i>ClientAddSearch(final <i>WebRequestObjectListType</i> <i>FieldName</i>)
 * public void <i>FieldName</i>ClientAdd()
 * public void <i>FieldName</i>ClientDelete()
 * public String client<i>FieldName</i>Action()
 * public String client<i>FieldName</i>LocationURI()
 * public String client<i>FieldName</i>Username()
 * public String client<i>FieldName</i>Password()
 * public String client<i>FieldName</i>MessageType()
 * public String client<i>FieldName</i>RequestType()
 * public boolean client<i>FieldName</i>Plain()
 *
 * All methods starting with word 'client' are reserved.
 *
 * Storage- and web client operations are triggered by button clicks,
 * and they can be also called by the following method from class ObjectViewApplication:
 *
 * public String[] callExecutor(final String namePath)
 *
 * Name path to executor method of button is given in namePath.
 *
 * </pre>
 */
public class ObjectView extends LegacyWindow {
    private final static long serialVersionUID = 0L;

    public static enum LayoutType {
        CSS_LAYOUT(CssLayout.class),
            FORM_LAYOUT(FormLayout.class),
            GRID_LAYOUT(GridLayout.class),
            HORIZONTAL_LAYOUT(HorizontalLayout.class),
            VERTICAL_LAYOUT(VerticalLayout.class);

        private final Class<? extends AbstractLayout> type;

        LayoutType(final Class<? extends AbstractLayout> type) {
            this.type = type;
        }
        
        public Class<? extends AbstractLayout> getType() {
            return type;
        }

    };

    abstract static class ObjectViewConverter<MODEL>
        implements Converter<String, MODEL> {

        public abstract MODEL convertToModel(final String value,
                                             final Locale locale);

        public String convertToPresentation(final MODEL value,
                                            final Locale locale) {
            return value != null ? String.valueOf(value) : "";
        }

        public abstract Class<MODEL> getModelType();

        public Class<String> getPresentationType() {
            return String.class;
        }

    }

    public static enum StorageType {TDB};

    static Converter characterConverter
        = new ObjectView.ObjectViewConverter<Character>() {
        private final static long serialVersionUID = 0L;

        public Character convertToModel(final String value,
                                        final Locale locale) {
            if (value.length() > 1) {
                throw new Converter.ConversionException();
            }
            return "".equals(value) ? null : value.charAt(0);
        }

        public Class<Character> getModelType() {
            return Character.class;
        }
    };
    static Converter byteConverter
        = new ObjectView.ObjectViewConverter<Byte>() {
        private final static long serialVersionUID = 0L;

        public Byte convertToModel(final String value,
                                   final Locale locale) {
            return "".equals(value) ? null : Byte.valueOf(value);
        }

        public Class<Byte> getModelType() {
            return Byte.class;
        }
    };
    static Converter shortConverter
        = new ObjectView.ObjectViewConverter<Short>() {
        private final static long serialVersionUID = 0L;

        public Short convertToModel(final String value,
                                    final Locale locale) {
            return "".equals(value) ? null : Short.valueOf(value);
        }

        public Class<Short> getModelType() {
            return Short.class;
        }
    };
    static Converter integerConverter
        = new ObjectView.ObjectViewConverter<Integer>() {
        private final static long serialVersionUID = 0L;

        public Integer convertToModel(final String value,
                                      final Locale locale) {
            return "".equals(value) ? null : Integer.valueOf(value);
        }

        public Class<Integer> getModelType() {
            return Integer.class;
        }
    };
    static Converter longConverter
        = new ObjectView.ObjectViewConverter<Long>() {
        private final static long serialVersionUID = 0L;

        public Long convertToModel(final String value,
                                   final Locale locale) {
            return "".equals(value) ? null : Long.valueOf(value);
        }

        public Class<Long> getModelType() {
            return Long.class;
        }
    };
    static Converter floatConverter
        = new ObjectView.ObjectViewConverter<Float>() {
        private final static long serialVersionUID = 0L;

        public Float convertToModel(final String value,
                                    final Locale locale) {
            return "".equals(value) ? null : Float.valueOf(value);
        }

        public Class<Float> getModelType() {
            return Float.class;
        }
    };
    static Converter doubleConverter
        = new ObjectView.ObjectViewConverter<Double>() {
        private final static long serialVersionUID = 0L;

        public Double convertToModel(final String value,
                                     final Locale locale) {
            return "".equals(value) ? null : Double.valueOf(value);
        }

        public Class<Double> getModelType() {
            return Double.class;
        }
    };
    static Converter bigDecimalConverter
        = new ObjectView.ObjectViewConverter<BigDecimal>() {
        private final static long serialVersionUID = 0L;

        public BigDecimal convertToModel(final String value,
                                         final Locale locale) {
            return "".equals(value) ? null : new BigDecimal(value);
        }

        public Class<BigDecimal> getModelType() {
            return BigDecimal.class;
        }
    };
    static Converter bigIntegerConverter
        = new ObjectView.ObjectViewConverter<BigInteger>() {
        private final static long serialVersionUID = 0L;

        public BigInteger convertToModel(final String value,
                                         final Locale locale) {
            return "".equals(value) ? null : new BigInteger(value);
        }

        public Class<BigInteger> getModelType() {
            return BigInteger.class;
        }
    };

    protected final Map<String, ObjectView.FieldInfo> executorFieldInfoMap = new HashMap<String, ObjectView.FieldInfo>();
    protected final Map<String, AbstractComponent> componentMap = new HashMap<String, AbstractComponent>();
    protected final Map<String, AbstractSelect> selectMap = new HashMap<String, AbstractSelect>();
    protected final Map<String, TabSheet> tabSheetMap = new HashMap<String, TabSheet>();
    protected final Map<String, MenuBar> menuBarMap = new HashMap<String, MenuBar>();

    protected Map<String, Object> defaultValues = null;
    protected Set<String> disabledSet = null;
    protected Set<String> invisibleSet = null;
    protected Locale locale = null;
    protected Locale defaultLocale = null;
    protected ResourceBundle resourceBundle = null;
    protected String bundleName = null;
    protected ObjectViewWebService webService = null;
    protected ObjectViewWebClient webClient = null;
    protected ObjectViewStorage storage = null;

    public static class FieldInfo {
        public Class type = null;
        public Class elementType = null;
        public Method accessor = null;
        public Method mutator = null;
        public Method executor = null;
        public Method editor = null;
        public Method validator = null;
        public Method disabled = null;
        public Method invisible = null;
        public String name = null;
        public String namePath = null;
        public String namespace = null;
        public boolean hasDefault = false;
        public boolean isArray = false;
        public boolean isList = false;
        public boolean isSimple = false;
        public boolean isEditable = false;
        public boolean isForm = false;
        public boolean isLink = false;
        public boolean isMenu = false;
        public boolean isCustom = false;
        public boolean isOptional = false;
        public boolean isVolatile = false;
        public boolean isLocale = false;
        public boolean isTheme = false;
        public boolean isRequest = false;
        public boolean isResponse = false;
        public boolean isProtected = false;
        public boolean isError = false;
        public boolean isClient = false;
        public boolean isAttribute = false;
        public int minLength = 0;
        public int maxLength = 0;
        public String pattern = null;
        public List<String> values = null;
        public ObjectView.FieldInfo requestFieldInfo = null;
        public ObjectView.FieldInfo responseFieldInfo = null;
        public List<ObjectView.FieldInfo> fieldInfoList = null;
        public Map<String, ObjectView.FieldInfo> fieldInfoMap = null;
        public Map<String, ObjectView.FieldInfo> formFieldInfoMap = null;
        public com.hp.hpl.jena.rdf.model.Property property = null;
        public Object object = null;

        public String toString() {
            return Support.toString(this);
        }

    }

    public static class TableData implements Serializable {
        private final static long serialVersionUID = 0L;

        protected Map<String, ObjectView.FieldInfo> fieldInfoMap = null;
        protected Table table = null;
        protected List<Object> dataList = null;
        protected Map<Object, Object> dataMap = null;
        protected Class elementType = null;
        protected String namePath = null;
        protected boolean isSingle = false;

        public String toString() {
            return Support.toString(this);
        }

    }

    class ObjectViewForm extends Form {
        private final static long serialVersionUID = 0L;

        @Override
        protected void attachField(Object propertyId,
                                   Field field) {
            if (field instanceof Form && field.isVisible()) {
                final Form form = (Form)field;
                if (form.getItemDataSource() != null) {
                    final Collection itemPropertyIds = form.getItemPropertyIds();
                    if (itemPropertyIds != null) {
                        for (final Object itemPropertyId : itemPropertyIds) {
                            super.attachField(itemPropertyId, form.getField(itemPropertyId));
                        }
                        return;
                    }
                }
            }
            super.attachField(propertyId, field);
        }

    }

    class CollectionItem implements Item {
        private final static long serialVersionUID = 0L;

        private final Collection<?> collection;

        CollectionItem(final Collection<?> collection) {
            this.collection = collection;
        }

        public Property getItemProperty(Object id) {
            return new ObjectProperty(id);
        }

        public Collection<?> getItemPropertyIds() {
            return collection;
        }

        public boolean addItemProperty(Object id,
                                       Property property) {
            return true;
        }

        public boolean removeItemProperty(Object id) {
            return true;
        }

    }

    class ObjectViewCommand implements MenuBar.Command {
        private final static long serialVersionUID = 0L;

        Object item = null;

        ObjectViewCommand(final Object item) {
            this.item = item;
        }

        public void menuSelected(MenuBar.MenuItem selectedItem) {
            navigate(item, "execute");
        }

    }

    class ObjectViewFormFieldFactory implements FormFieldFactory {
        private final static long serialVersionUID = 0L;

        private final Repository repository;
        private final String name;
        private final String namePath;
        private final List<ObjectView.FieldInfo> fieldInfoList;

        ObjectViewFormFieldFactory(final Repository repository,
                                   final String name,
                                   final String namePath,
                                   final List<ObjectView.FieldInfo> fieldInfoList) {
            this.repository = repository;
            this.name = name;
            this.namePath = namePath;
            this.fieldInfoList = fieldInfoList;
        }

        public Field createField(final Item item,
                                 final Object propertyId,
                                 final Component uiContext) {
            Exception exception = null;
            try {
                final ObjectView.FieldInfo fieldInfo = new ObjectView.FieldInfo();
                fieldInfo.name = name;
                fieldInfo.namePath = namePath;
                final List<ObjectView.FieldInfo> componentFieldInfoList = new ArrayList<ObjectView.FieldInfo>();
                final Field field
                    = (Field)makeForm(repository, namePath, propertyId, null, null, null,
                                      componentFieldInfoList, null, null, null, false, false, false);
                fieldInfo.fieldInfoList = componentFieldInfoList;
                checkNamespace(propertyId, "", fieldInfo);
                if (fieldInfoList != null) {
                    fieldInfoList.add(fieldInfo);
                }
                return field;
            } catch (final ClassNotFoundException classNotFoundException) {
                exception = classNotFoundException;
            } catch (final IOException ioException) {
                exception = ioException;
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InstantiationException instantiationException) {
                exception = instantiationException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            } catch (final NoSuchMethodException noSuchMethodException) {
                exception = noSuchMethodException;
            } catch (final SAXException saxException) {
                exception = saxException;
            } catch (final TransformerException transformerException) {
                exception = transformerException;
            } catch (final URISyntaxException uriSyntaxException) {
                exception = uriSyntaxException;
            } catch (final WSDLException wsdlException) {
                exception = wsdlException;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            return null;
        }

    }

    class ObjectViewTableFieldFactory implements TableFieldFactory {
        private final static long serialVersionUID = 0L;

        private final Repository repository;
        private final String namePath;
        private final TableData tableData;

        ObjectViewTableFieldFactory(final Repository repository,
                                    final String namePath,
                                    final TableData tableData) {
            this.repository = repository;
            this.namePath = namePath;
            this.tableData = tableData;
        }

        public Field createField(final Container container,
                                 final Object itemId,
                                 final Object propertyId,
                                 final Component uiContext) {
            final ObjectView.FieldInfo fieldInfo = tableData.fieldInfoMap.get((String)propertyId);
            final Property property = container.getContainerProperty(itemId, propertyId);
            final Class<?> type = property.getType();
            Object object = tableData.dataMap.get(itemId);
            if (object == null) {
                Exception exception = null;
                try {
                    object = tableData.elementType.newInstance();
                    putTableItemData(tableData, itemId, object);
                } catch (final IllegalAccessException illegalAccessException) {
                    exception = illegalAccessException;
                } catch (final InvocationTargetException invocationTargetException) {
                    exception = invocationTargetException;
                } catch (final InstantiationException instantiationException) {
                    exception = instantiationException;
                }
                if (exception != null) {
                    throw new RuntimeException(exception);
                }
            }
            Field field = null;
            Exception exception = null;
            try {
                field = (Field)makeComponent(repository, null, (String)propertyId, null, type, object, null,
                                             fieldInfo, null, null, false, false);
            } catch (final ClassNotFoundException classNotFoundException) {
                exception = classNotFoundException;
            } catch (final IOException ioException) {
                exception = ioException;
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InstantiationException instantiationException) {
                exception = instantiationException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            } catch (final NoSuchMethodException noSuchMethodException) {
                exception = noSuchMethodException;
            } catch (final SAXException saxException) {
                exception = saxException;
            } catch (final TransformerException transformerException) {
                exception = transformerException;
            } catch (final URISyntaxException uriSyntaxException) {
                exception = uriSyntaxException;
            } catch (final WSDLException wsdlException) {
                exception = wsdlException;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            return field;
        }

    }

    class ObjectViewValidator extends AbstractStringValidator {
        private final static long serialVersionUID = 0L;

        private final Object object;
        private final String validatorName;

        ObjectViewValidator(final Object object,
                            final Method validator) {
            super("");
            this.object = object;
            this.validatorName = validator.getName();
        }

        @Override
        public boolean isValidValue(String value) {
            String errorMessage = null;
            final Method validator = ObjectView.getMethod(object.getClass(), validatorName);
            Exception exception = null;
            try {
                errorMessage = (String)validator.invoke(object, value);
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            if (errorMessage != null) {
                setErrorMessage(getString(errorMessage));
                return false;
            }
            return true;
        }

    }

    class EditorListener implements Property.ValueChangeListener {
        private final static long serialVersionUID = 0L;

        private final Object object;
        private final String editorName;
        private final String searchName;
        private final boolean isLocale;
        private final boolean isTheme;
        private final Class type;
        private final Map<String, ObjectView.FieldInfo> fieldInfoMap;

        EditorListener(final Object object,
                       final Method editor,
                       final String searchName,
                       final boolean isLocale,
                       final boolean isTheme,
                       final Map<String, ObjectView.FieldInfo> fieldInfoMap) {
            this.object = object;
            this.editorName = editor.getName();
            this.searchName = searchName;
            this.isLocale = isLocale;
            this.isTheme = isTheme;
            this.type = editor.getParameterTypes()[0];
            this.fieldInfoMap = fieldInfoMap;
        }

        public void searchData()
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
            if (fieldInfoMap != null) {
                storage.searchData(object, searchName, fieldInfoMap);
            }
        }

        public void valueChange(Property.ValueChangeEvent event) {
            final Property property = event.getProperty();
            final Object value = property.getValue();
            Exception exception = null;
            try {
                final Method editor = object.getClass().getMethod(editorName, type);
                final String[] namePaths = (String[])editor.invoke(object, value);
                if (storage != null) {
                    searchData();
                }
                if (namePaths != null) {
                    for (final String namePath : namePaths) {
                        updateComponent(namePath);
                    }
                }
                if (isLocale) {
                    final StringTokenizer tokens = new StringTokenizer((String)value, "_");
                    String language = null, country = null, variant = null;
                    if (tokens.hasMoreTokens()) {
                        language = tokens.nextToken();
                        if (tokens.hasMoreTokens()) {
                            country = tokens.nextToken();
                            if (tokens.hasMoreTokens()) {
                                variant = tokens.nextToken();
                            }
                        }
                    }
                    Locale locale = null;
                    if (variant != null) {
                        locale = new Locale(language, country, variant);
                    } else if (country != null) {
                        locale = new Locale(language, country);
                    } else if (language != null) {
                        locale = new Locale(language);
                    }
                    if (locale != null) {
                        ObjectView.this.locale = locale;
                        getObjectViewApplication().setContextLocale(locale);
                        ObjectView.this.requestRepaintAll();
                    }
                }
                if (isTheme) {
                    getApplication().setTheme((String)value);
                }
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InstantiationException instantiationException) {
                exception = instantiationException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            } catch (final NoSuchMethodException noSuchMethodException) {
                exception = noSuchMethodException;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
        }

    }

    class NavigatorListener implements Button.ClickListener {
        private final static long serialVersionUID = 0L;

        private final Object object;
        private final String navigatorName;

        NavigatorListener(final Object object,
                          final Method navigator) {
            this.object = object;
            this.navigatorName = navigator.getName();
        }

        public void buttonClick(final Button.ClickEvent clickEvent) {
            navigate(object, navigatorName);
        }

    }

    class StarterListener implements TabSheet.SelectedTabChangeListener {
        private final static long serialVersionUID = 0L;

        public void selectedTabChange(final TabSheet.SelectedTabChangeEvent selectedTabChangeEvent) {
            final TabSheet tabSheet = selectedTabChangeEvent.getTabSheet();
            final Component tab = tabSheet.getSelectedTab();
            invokeStarter((AbstractComponent)tab);
        }

    }

    class CalendarMethodProperty extends MethodProperty<Calendar> {
        private final static long serialVersionUID = 0L;

        CalendarMethodProperty(final Object instance,
                               final ObjectView.FieldInfo fieldInfo) {
            super(Calendar.class, instance, fieldInfo.accessor, fieldInfo.mutator);
        }

        public Calendar getValue() {
            return super.getValue();
        }

        public void setValue(Calendar newValue) {
            super.setValue(newValue);
        }

    }

    class StringProperty extends ObjectProperty {
        private final static long serialVersionUID = 0L;

        final String name;

        StringProperty(final String name) {
            super("");
            this.name = name;
        }

        @Override
        public Object getValue() {
            return getString(name);
        }

    }

    class ObjectViewPropertyFormatter extends PropertyFormatter {
        private final static long serialVersionUID = 0L;

        private final Class type;
        private final Object object;
        private final String argumentsMethodName;

        ObjectViewPropertyFormatter(final Property propertyDataSource,
                                    final Class type,
                                    final Object object,
                                    final Method argumentsMethod) {
            this.type = type;
            this.object = object;
            this.argumentsMethodName = argumentsMethod != null ? argumentsMethod.getName() : null;
            setPropertyDataSource(propertyDataSource);
        }

        @Override
        public Class getType() {
            return type;
        }

        @Override
        public String getValue() {
            return getPropertyDataSource().getValue() != null ? getPropertyDataSource().getValue().toString() : "";
        }

        @Override
        public String format(Object value) {
            if (String.class.equals(type)) {
                Object[] arguments = null;
                if (argumentsMethodName != null) {
                    final Method argumentsMethod = ObjectView.getMethod(object.getClass(), argumentsMethodName);
                    Exception exception = null;
                    try {
                        arguments = (Object[])argumentsMethod.invoke(object);
                    } catch (final IllegalAccessException illegalAccessException) {
                        exception = illegalAccessException;
                    } catch (final InvocationTargetException invocationTargetException) {
                        exception = invocationTargetException;
                    }
                    if (exception != null) {
                        throw new RuntimeException(exception);
                    }
                }
                return getText((String)value, arguments);
            }
            return value.toString();
        }
            
        @Override
        public Object parse(String formattedValue) {
            if (Character.class.equals(type)) {
                return formattedValue.length() > 0 ? formattedValue.charAt(0) : null;
            }
            return formattedValue;
        }

        @Override
        public void setValue(Object newValue) {
            if (newValue instanceof String) {
                getPropertyDataSource().setValue(parse((String)newValue));
            } else {
                getPropertyDataSource().setValue(newValue);
            }
        }

    }

    class FormClickListener implements Button.ClickListener {
        private final static long serialVersionUID = 0L;

        final List<TableData> tableDataList = new ArrayList<TableData>();
        final Map<Button, ObjectView.FieldInfo> executorFieldInfoMap = new HashMap<Button, ObjectView.FieldInfo>();
        final Map<String, ObjectView.FieldInfo> fieldInfoMap;
        final Object object;

        FormClickListener(final Map<String, ObjectView.FieldInfo> fieldInfoMap,
                          final Object object) {
            this.fieldInfoMap = fieldInfoMap;
            this.object = object;
        }

        public void getData()
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
            for (final TableData tableData : tableDataList) {
                getTableData(tableData);
            }
        }

        public void putData()
            throws IllegalAccessException, InvocationTargetException {
            for (final TableData tableData : tableDataList) {
                putTableData(tableData);
            }
        }

        public void buttonClick(final Button.ClickEvent clickEvent) {
            final Button button = clickEvent.getButton();
            Exception exception = null;
            try {
                getData();
                final ObjectView.FieldInfo fieldInfo = executorFieldInfoMap.get(button);
                final String[] namePaths = callExecutor(object, fieldInfo, fieldInfoMap);
                if (namePaths != null) {
                    final int length = namePaths.length;
                    if (length > 0) {
                        String namePath = namePaths[0];
                        if (namePath != null) {
                            final AbstractComponent component = getComponent(namePath);
                            final TabSheet tabSheet = (TabSheet)getContainerComponent(namePath);
                            tabSheet.setSelectedTab(component);
                            invokeStarter(component);
                        }
                        for (int index = 1; index < length; index++) {
                            updateComponent(namePaths[index]);
                        }
                    }
                }
            } catch (final AxisFault axisFault) {
                exception = axisFault;
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InstantiationException instantiationException) {
                exception = instantiationException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            } catch (final MalformedURLException malformedURLException) {
                exception = malformedURLException;
            } catch (final NoSuchMethodException noSuchMethodException) {
                exception = noSuchMethodException;
            } catch (final XMLStreamException xmlStreamException) {
                exception = xmlStreamException;
            } catch (ObjectViewWebService.ConversionException conversionException) {
                exception = conversionException;
            }
            if (exception != null) {
                exception.printStackTrace();
                Notification.show("Error invoking service",
                                  Notification.TYPE_ERROR_MESSAGE);
            }
        }

    }

    class TableClickListener extends TableData
        implements Button.ClickListener {
        private final static long serialVersionUID = 0L;

        @Override
        public void buttonClick(final Button.ClickEvent clickEvent) {
            final Button button = clickEvent.getButton();
            if ("Remove".equals(button.getCaption())) {
                final Object itemId = table.getValue();
                if (itemId != null) {
                    table.removeItem(itemId);
                    final Object object = dataMap.remove(itemId);
                    dataList.remove(object);
                }
            } else {
                final Object itemId = table.addItem();
                Exception exception = null;
                try {
                    getTableItemData(this, itemId);
                } catch (final IllegalAccessException illegalAccessException) {
                    exception = illegalAccessException;
                } catch (final InstantiationException instantiationException) {
                    exception = instantiationException;
                } catch (final InvocationTargetException invocationTargetException) {
                    exception = invocationTargetException;
                }
                if (exception != null) {
                    throw new RuntimeException(exception);
                }
            }
        }

    }

    public static Method getMethod(final Class type,
                                   final String name) {
        try {
            return type.getMethod(name);
        } catch (final NoSuchMethodException noSuchMethodException) {}
        return null;
    }

    public static Method getMethod(final Method methods[],
                                   final String name) {
        for (final Method method : methods) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public void setup(final Object object,
                      final Locale locale,
                      final ServletContext servletContext)
        throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException,
               InvocationTargetException, NoSuchMethodException, SAXException, TransformerException,
               URISyntaxException, WSDLException {
        final Class objectClass = object.getClass();
        final Method storageTypeMethod = ObjectView.getMethod(objectClass, "storageType");
        defaultLocale = locale;
        if (storageTypeMethod != null) {
            final ObjectView.StorageType storageType = (ObjectView.StorageType)storageTypeMethod.invoke(object);
            if (ObjectView.StorageType.TDB.equals(storageType)) {
                final Method storageDirectoryMethod = ObjectView.getMethod(objectClass, "storageDirectory");
                final String storageDirectory = (String)storageDirectoryMethod.invoke(object);
                storage = new ObjectViewStorage(storageDirectory);
            }
        }
        final String name = objectClass.getSimpleName();
        bundleName = objectClass.getPackage().getName() + ".resources." + name;
        checkLocale();
        componentMap.put(name, this);
        final Method defaultValuesMethod = ObjectView.getMethod(objectClass, "defaultValues");
        if (defaultValuesMethod != null) {
            defaultValues = (Map<String, Object>)defaultValuesMethod.invoke(object);
        }
        final Method disabledSetMethod = ObjectView.getMethod(objectClass, "disabledSet");
        if (disabledSetMethod != null) {
            disabledSet = (Set<String>)disabledSetMethod.invoke(object);
        }
        final Method invisibleSetMethod = ObjectView.getMethod(objectClass, "invisibleSet");
        if (invisibleSetMethod != null) {
            invisibleSet = (Set<String>)invisibleSetMethod.invoke(object);
        }
        final Repository repository = new ClassLoaderRepository(getClass().getClassLoader());
        final List<ObjectView.FieldInfo> fieldInfoList = new ArrayList<ObjectView.FieldInfo>();
        final Map<String, ObjectView.FieldInfo> fieldInfoMap = new HashMap<String, ObjectView.FieldInfo>();
        makeForm(repository, name, object, object.getClass(), null, null, fieldInfoList, fieldInfoMap, null,
                 getContent(), false, false, false);
        checkComponents();
        final Method namespaceMethod = ObjectView.getMethod(objectClass, "namespace");
        if (namespaceMethod != null) {
            final String namespace = (String)namespaceMethod.invoke(object);
            if (namespace != null) {
                webService = ObjectViewWebService.getObjectViewWebService(servletContext, object, namespace, name,
                                                                          fieldInfoList);
                webClient = new ObjectViewWebClient(webService);
            }
        }
    }

    public void shutdown() {
        if (storage != null) {
            storage.close();
        }
    }

    public ObjectViewApplication getObjectViewApplication() {
        return (ObjectViewApplication)getApplication();
    }

    public void checkNamespace(final Object object,
                               final String name,
                               final ObjectView.FieldInfo fieldInfo)
        throws IllegalAccessException, InvocationTargetException {
        final Method namespaceMethod = ObjectView.getMethod(object.getClass(), "namespace" + name);
        if (namespaceMethod != null) {
            fieldInfo.namespace = (String)namespaceMethod.invoke(object);
        }
    }

    public String[] callExecutor(final String namePath)
        throws AxisFault, IllegalAccessException, InstantiationException, InvocationTargetException,
               MalformedURLException, NoSuchMethodException, XMLStreamException,
               ObjectViewWebService.ConversionException {
        final ObjectView.FieldInfo fieldInfo = executorFieldInfoMap.get(namePath);
        return callExecutor(fieldInfo.object, fieldInfo, fieldInfo.fieldInfoMap);
    }

    protected String[] callExecutor(final Object object,
                                    final ObjectView.FieldInfo fieldInfo,
                                    final Map<String, ObjectView.FieldInfo> fieldInfoMap)
        throws AxisFault, IllegalAccessException, InstantiationException, InvocationTargetException,
               MalformedURLException, NoSuchMethodException, XMLStreamException,
               ObjectViewWebService.ConversionException {
        final Method executor = fieldInfo.executor;
        if (storage != null
            && ("store".equals(executor.getName()) || executor.getName().endsWith("Store")
                || "remove".equals(executor.getName()) || executor.getName().endsWith("Remove"))) {
            storage.searchData(object, executor.getName() + "Search", fieldInfoMap);
        }
        if (fieldInfo.isClient) {
            webClient.invokeService(fieldInfo);
            ObjectView.this.requestRepaintAll();
        }
        final String[] namePaths = (String[])executor.invoke(object);
        if (storage != null) {
            if ("store".equals(executor.getName()) || executor.getName().endsWith("Store")) {
                storage.storeData(object, executor.getName(), fieldInfoMap, false);
            } else if ("search".equals(executor.getName()) || executor.getName().endsWith("Search")) {
                storage.searchData(object, executor.getName(), fieldInfoMap);
            } else if ("remove".equals(executor.getName()) || executor.getName().endsWith("Remove")) {
                storage.storeData(object, executor.getName(), fieldInfoMap, true);
            }
        }
        return namePaths;
    }

    protected void navigate(final Object object,
                            final String navigatorName) {
        final Method navigator = ObjectView.getMethod(object.getClass(), navigatorName);
        Exception exception = null;
        try {
            final String[] namePaths = (String[])navigator.invoke(object);
            for (int index = 1; index < namePaths.length; index++) {
                updateComponent(namePaths[index]);
            }
            final String namePath = namePaths[0];
            if (namePath != null) {
                AbstractComponent component = (AbstractComponent)getComponent(namePath);
                if (component instanceof Window) {
                    updateComponent(component);
                    component = (AbstractComponentContainer)((Window)component).getContent();
                } else {
                    final TabSheet tabSheet = (TabSheet)getContainerComponent(namePath);
                    tabSheet.setSelectedTab(component);
                }
                invokeStarter(component);
            }
        } catch (final IllegalAccessException illegalAccessException) {
            exception = illegalAccessException;
        } catch (final InstantiationException instantiationException) {
            exception = instantiationException;
        } catch (final InvocationTargetException invocationTargetException) {
            exception = invocationTargetException;
        } catch (final NoSuchMethodException noSuchMethodException) {
            exception = noSuchMethodException;
        }
        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }

    protected SimpleDateFormat getDateFormat(Class type) {
        String pattern = null;
        if (java.sql.Date.class.equals(type)) {
            pattern = "yyyy-MM-dd";
        } else if (java.sql.Time.class.equals(type)) {
            pattern = "HH:mm:ss";
        } else {
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        return new SimpleDateFormat(pattern, locale);
    }

    protected AbstractComponent getContainerComponent(final String namePath) {
        final int dot = namePath.lastIndexOf('.');
        return getComponent(dot != -1 ? namePath.substring(0, dot) : namePath);
    }

    protected AbstractComponent getComponent(final String namePath) {
        final AbstractComponent component = componentMap.get(namePath);
        if (component == null) {
            throw new RuntimeException("Component with name path '" + namePath + "' not found");
        }
        return component;
    }

    protected void checkLocale() {
        if (defaultLocale != null) {
            locale = defaultLocale;
        } else {
            final UI ui = UI.getCurrent();
            locale = ui.getLocale();
        }
        try {
            resourceBundle = ResourceBundle.getBundle(bundleName, locale);
        } catch (final MissingResourceException missingResourceException) {}
    }

    protected void checkMenuItems(final Iterator<MenuBar.MenuItem> menuItemIterator) {
        while (menuItemIterator.hasNext()) {
            final MenuBar.MenuItem menuItem = menuItemIterator.next();
            menuItem.setText(getString(menuItem.getDescription()));
            final List<MenuBar.MenuItem> children = menuItem.getChildren();
            if (children != null) {
                checkMenuItems(children.iterator());
            }
        }
    }

    protected void checkComponents()
        throws IllegalAccessException, InvocationTargetException {
        final Iterator<TabSheet> tabSheetIterator = tabSheetMap.values().iterator();
        while (tabSheetIterator.hasNext()) {
            final TabSheet tabSheet = tabSheetIterator.next();
            final Iterator<Component> componentIterator = tabSheet.getComponentIterator();
            while (componentIterator.hasNext()) {
                final AbstractComponent component = (AbstractComponent)componentIterator.next();
                final Object object = component.getData();
                final String name = object.getClass().getSimpleName();
                final TabSheet.Tab tab = tabSheet.getTab(component);
                tab.setCaption(getString(name));
            }
        }
        final Iterator<MenuBar> menuBarIterator = menuBarMap.values().iterator();
        while (menuBarIterator.hasNext()) {
            final MenuBar menuBar = menuBarIterator.next();
            checkMenuItems(menuBar.getItems().iterator());
        }
        final Iterator<Map.Entry<String, AbstractSelect>> selectEntryIterator = selectMap.entrySet().iterator();
        while (selectEntryIterator.hasNext()) {
            final Map.Entry<String, AbstractSelect> selectEntry = selectEntryIterator.next();
            String namePath = selectEntry.getKey();
            updateSelect(selectEntry.getValue(), namePath);
        }
        final Iterator<AbstractComponent> componentIterator = componentMap.values().iterator();
        while (componentIterator.hasNext()) {
            final AbstractComponent component = componentIterator.next();
            component.setCaption(component.getCaption());
        }
    }

    protected void getTableData(final TableData tableData)
        throws IllegalAccessException, InstantiationException, InvocationTargetException {
        if (!tableData.table.isEditable()) {
            return;
        }
        final Collection itemIds = tableData.table.getItemIds();
        for (final Object itemId : itemIds) {
            getTableItemData(tableData, itemId);
        }
    }

    protected void getTableItemData(final TableData tableData,
                                    final Object itemId)
        throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final Collection containerPropertyIds = tableData.table.getContainerPropertyIds();
        final Item item = tableData.table.getItem(itemId);
        if (tableData.isSingle) {
            final Object propertyId = containerPropertyIds.iterator().next();
            final Object value = item.getItemProperty(propertyId).getValue(),
                oldValue = tableData.dataMap.get(itemId);
            if (oldValue != null) {
                int index = tableData.dataList.indexOf(oldValue);
                tableData.dataList.set(index, value);
            } else {
                tableData.dataList.add(value);
            }
            tableData.dataMap.put(itemId, value);
        } else {
            Object value = tableData.dataMap.get(itemId);
            if (value == null) {
                value = tableData.elementType.newInstance();
                tableData.dataList.add(value);
                tableData.dataMap.put(itemId, value);
            }
            for (final Object aPropertyId : containerPropertyIds) {
                final ObjectView.FieldInfo fieldInfo = tableData.fieldInfoMap.get((String)aPropertyId);
                final Method mutator = fieldInfo.mutator;
                Object propertyValue = item.getItemProperty(aPropertyId).getValue();
                if ((fieldInfo.type.equals(Character.class) || fieldInfo.type.equals(Character.TYPE))
                    && propertyValue instanceof String) {
                    propertyValue = ((String)propertyValue).length() > 0 ? ((String)propertyValue).charAt(0) : null;
                }
                mutator.invoke(value, propertyValue);
            }
        }
    }

    protected void putTableData(final TableData tableData)
        throws IllegalAccessException, InvocationTargetException {
        tableData.dataMap.clear();
        tableData.table.removeAllItems();
        for (final Object value : tableData.dataList) {
            final Object itemId = tableData.table.addItem();
            putTableItemData(tableData, itemId, value);
            tableData.dataMap.put(itemId, value);
        }
    }

    protected void putTableItemData(final TableData tableData,
                                    final Object itemId,
                                    Object value)
        throws IllegalAccessException, InvocationTargetException {
        final Collection containerPropertyIds = tableData.table.getContainerPropertyIds();
        final Item item = tableData.table.getItem(itemId);
        if (tableData.isSingle) {
            Object propertyId = containerPropertyIds.iterator().next();
            final Property property = item.getItemProperty(propertyId);
            if (value instanceof Character) {
                value = value.toString();
            }
            property.setValue(value);
        } else {
            for (final Object aPropertyId : containerPropertyIds) {
                final Method accessor = tableData.fieldInfoMap.get((String)aPropertyId).accessor;
                Object aValue = accessor.invoke(value);
                if (aValue != null) {
                    final Property property = item.getItemProperty(aPropertyId);
                    if (aValue instanceof Character) {
                        aValue = aValue.toString();
                    }
                    property.setValue(aValue);
                }
            }
        }
    }

    protected void updateComponent(String namePath)
        throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        AbstractComponent component = componentMap.get(namePath);
        if (component == null && namePath.endsWith(".values")) {
            namePath = namePath.substring(0, namePath.length() - ".values".length());
            component = getComponent(namePath);
            if (component != null) {
                updateSelect((AbstractSelect)component, namePath);
            }
        }
        if (component == null) {
            throw new RuntimeException("Component with name path '" + namePath + "' not found for update");
        }
        updateComponent(component);
    }

    protected void updateComponent(final AbstractComponent component)
        throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        if (storage != null) {
            final Collection<?> listeners = component.getListeners(Property.ValueChangeEvent.class);
            final Iterator<?> listenerItems = listeners.iterator();
            while (listenerItems.hasNext()) {
                final Object listener = listenerItems.next();
                if (listener instanceof ObjectView.EditorListener) {
                    ((ObjectView.EditorListener)listener).searchData();
                }
            }
        }
        if (component instanceof UI) {
            if (component != this) {
                final UI ui = (UI)component;
                final Page page = new Page(ui);
                //page.open(page.getLocation().toString(), page.getName());
            }
        } else if (component instanceof Window) {
            if (component != this) {
                final Collection<Window> windows = UI.getCurrent().getWindows();
                if (!windows.contains(component)) {
                    UI.getCurrent().addWindow((Window)component);
                }
            }
        } else if (component instanceof AbstractSelect) {
            final Property property = ((AbstractField)component).getPropertyDataSource();
            if (property != null) {
                final Object value = property.getValue();
                if (((AbstractSelect)component).isMultiSelect()) {
                    for (final Object aValue : (Set)value) {
                        ((AbstractSelect)component).select(aValue);
                    }
                } else {
                    ((AbstractSelect)component).select(value);
                }
            }
        } else if (component instanceof AbstractField) {
            final Property property = ((AbstractField)component).getPropertyDataSource();
            if (property instanceof MethodProperty) {
                ((MethodProperty)property).fireValueChange();
            } else {
                component.requestRepaint();
            }
            if (component instanceof Form) {
                final Collection<?> itemPropertyIds = ((Form)component).getItemPropertyIds();
                for (final Object itemPropertyId : itemPropertyIds) {
                    final Field field = ((Form)component).getField(itemPropertyId);
                    if (field instanceof AbstractComponent) {
                        updateComponent((AbstractComponent)field);
                    }
                }
            }
        } else if (component instanceof Layout) {
            if (component.getParent() instanceof TabSheet) {
                final TabSheet tabSheet = (TabSheet)component.getParent();
                final TabSheet.Tab tab = tabSheet.getTab(component);
                tab.setEnabled(component.isEnabled());
                tab.setVisible(component.isVisible());
            } else if (component.getData() instanceof TableData) {
                final TableData tableData = (TableData)component.getData();
                getTableData(tableData);
                putTableData(tableData);
            }
        }
        if (component instanceof ComponentContainer) {
            final Iterator<Component> components = ((ComponentContainer)component).getComponentIterator();
            while (components.hasNext()) {
                final Component aComponent = components.next();
                if (aComponent instanceof AbstractComponent) {
                    updateComponent((AbstractComponent)aComponent);
                }
            }
            ((ComponentContainer)component).requestRepaintAll();
        } else {
            component.requestRepaint();
        }
    }

    protected void invokeStarter(final AbstractComponent layout) {
        final Object object = layout.getData();
        final Method starter = ObjectView.getMethod(object.getClass(), "start");
        if (starter != null) {
            Exception exception = null;
            try {
                if (storage != null) {
                    final Form form = (Form)((ComponentContainer)layout).getComponentIterator().next();
                    final Map<String, ObjectView.FieldInfo> fieldInfoMap = (Map<String, ObjectView.FieldInfo>)form.getData();
                    storage.searchData(object, "startSearch", fieldInfoMap);
                }
                String[] names  = (String[])starter.invoke(object);
                for (final String name : names) {
                    updateComponent(name);
                }
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InstantiationException instantiationException) {
                exception = instantiationException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            } catch (final NoSuchMethodException noSuchMethodException) {
                exception = noSuchMethodException;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
        }
    }

    protected AbstractComponentContainer makeLayout(final Repository repository,
                                                    final String namePath,
                                                    final Object object,
                                                    final TabSheet tabSheet,
                                                    final List<ObjectView.FieldInfo> fieldInfoList,
                                                    final Map<String, ObjectView.FieldInfo> fieldInfoMap)
        throws ClassNotFoundException, IOException, IllegalAccessException, InstantiationException,
               InvocationTargetException, NoSuchMethodException, SAXException, TransformerException,
               URISyntaxException, WSDLException {
        final Class<?> objectType = object.getClass();
        final Method objectDisabled = ObjectView.getMethod(objectType, "disabled"),
            objectInvisible = ObjectView.getMethod(objectType, "invisible");
        final HorizontalLayout layout = new HorizontalLayout() {
                private final static long serialVersionUID = 0L;

                @Override
                public boolean isEnabled() {
                    return checkFlag(disabledSet, namePath, object, objectDisabled, super.isEnabled());
                }

                @Override
                public boolean isVisible() {
                    return checkFlag(invisibleSet, namePath, object, objectInvisible, super.isVisible());
                }

            };
        layout.setHeight(-1, Sizeable.Unit.POINTS);
        layout.setData(object);
        final Boolean sizeFull = (Boolean)callMethod(object, "sizeFull");
        if (sizeFull != null && sizeFull.booleanValue()) {
            layout.setSizeFull();
        }
        final String name = object.getClass().getSimpleName();
        final Form form
            = makeForm(repository, namePath, object, null, null, null, fieldInfoList, fieldInfoMap, null, null,
                       false, false, false);
        layout.addComponent(form);
        componentMap.put(namePath, layout);
        if (tabSheet == null) {
            return layout;
        }
        final TabSheet.Tab tab = tabSheet.addTab(layout);
        tab.setCaption(getString(name));
        return layout;
    }

    protected boolean hasWord(final String name,
                              final String word,
                              final boolean only,
                              final boolean ends) {
        final int wordLength = word.length();
        return name.length() > wordLength
            && (name.startsWith(word) || name.startsWith(Character.toUpperCase(word.charAt(0)) + word.substring(1)))
            && (Character.isUpperCase(name.charAt(wordLength)) || !Character.isLetter(name.charAt(wordLength)))
            || only && name.equals(word)
            || ends && name.endsWith(Character.toUpperCase(word.charAt(0)) + word.substring(1));
    }

    protected boolean isText(final String name) {
        return hasWord(name, "text", false, false)
            || hasWord(name, "title", true, false)
            || hasWord(name, "mainTitle", true, false);
    }

    protected String getString(final String name) {
        if (resourceBundle == null) {
            return name;
        }
        try {
            return resourceBundle.getString(name);
        } catch (final MissingResourceException missingResourceException) {
            return name;
        }
    }

    protected String getText(final String name,
                             final Object... arguments) {
        final String pattern = getString(name);
        final MessageFormat messageFormat = new MessageFormat(name, locale);
        return messageFormat.format(pattern, arguments);
    }

    protected boolean checkFlag(final Set<String> set,
                                final String namePath,
                                final Object object,
                                final Method method,
                                boolean flagValue) {
        if (!flagValue) {
            return flagValue;
        }
        if (set != null) {
            flagValue = !set.contains(namePath);
        }
        if (method != null) {
            Exception exception = null;
            try {
                final Boolean result = (Boolean)method.invoke(object);
                return flagValue && !result;
            } catch (final IllegalAccessException illegalAccessException) {
                exception = illegalAccessException;
            } catch (final InvocationTargetException invocationTargetException) {
                exception = invocationTargetException;
            }
            if (exception != null) {
                throw new RuntimeException(exception);
            }
        }
        return flagValue;
    }

    protected Class getTypeParameter(final Repository repository,
                                     final Class type,
                                     final Object object,
                                     final String namePath)
        throws ClassNotFoundException {
        try {
            final JavaClass javaClass = repository.loadClass(type);
            final ConstantPool constantPool = javaClass.getConstantPool();
            final Constant[] constants = constantPool.getConstantPool();
            for (int index = 0; index < constants.length; index++) {
                final Constant constant = constants[index];
                if (constant instanceof ConstantUtf8) {
                    final String name = ((ConstantUtf8)constant).getBytes();
                    if ("Signature".equals(name)) {
                        final Constant nextConstant = constants[index + 1];
                        String className = null;
                        if (nextConstant instanceof ConstantClass) {
                            className = ((ConstantClass)nextConstant).getBytes(constantPool).replace('/', '.');
                        } else {
                            final String signature = ((ConstantUtf8)nextConstant).getBytes(),
                                signatureString = Utility.signatureToString(signature);
                            String searchString = "<L";
                            int signatureIndex = signatureString.indexOf(searchString);
                            if (signatureIndex == -1) {
                                searchString = "<[L";
                                signatureIndex = signatureString.indexOf(searchString);
                                className = signatureString.substring(signatureIndex + searchString.length());
                                return Array.newInstance(Class.forName(className), 0).getClass();
                            } else {
                                className = signatureString.substring(signatureIndex + searchString.length());
                            }
                        }
                        return Class.forName(className);
                    }
                }
            }
        } catch (final ClassFormatException exception) {
            throw new ClassFormatException(exception + " from " + object.getClass() + ": " + namePath);
        } catch (final ClassNotFoundException exception) {
            throw new ClassNotFoundException(exception + " from " + object.getClass() + ": " + namePath, exception);
        }
        return null;
    }

    protected Object callMethod(final Object object,
                                final String name)
        throws IllegalAccessException, InvocationTargetException {
        final Method method = ObjectView.getMethod(object.getClass(), name);
        if (method != null) {
            return method.invoke(object);
        }
        return null;
    }

    protected void setSelectValues(final AbstractSelect select,
                                   final Object values,
                                   final Set<?> selectedItemIds) {
        if (values.getClass().isArray()) {
            final int length = Array.getLength(values);
            for (int index = 0; index < length; index++) {
                final Object aValue = Array.get(values, index);
                select.addItem(aValue);
                if (selectedItemIds != null) {
                    if (selectedItemIds.contains(aValue)) {
                        select.select(aValue);
                    }
                }
                String caption = getString(aValue.toString());
                if (caption == null) {
                    caption = aValue.toString();
                }
                select.setItemCaption(aValue, caption);
            }
        } else {
            final List valuesList = (List)values;
            final Iterator valueItems = valuesList.iterator();
            while (valueItems.hasNext()) {
                final Object aValue = valueItems.next();
                select.addItem(aValue);
                if (selectedItemIds != null) {
                    if (selectedItemIds.contains(aValue)) {
                        select.select(aValue);
                    }
                }
                String caption = getString(aValue.toString());
                if (caption == null) {
                    caption = aValue.toString();
                }
                select.setItemCaption(aValue, caption);
            }
        }
    }

    protected void updateSelect(final AbstractSelect select,
                                final String namePath)
        throws IllegalAccessException, InvocationTargetException {
        final Object object = select.getData();
        Object values = null;
        if (object.getClass().isArray()) {
            values = object;
        } else {
            final String name = namePath.substring(namePath.lastIndexOf('.') + 1);
            final Method valuesMethod
                = ObjectView.getMethod(object.getClass(),
                            "values" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            if (valuesMethod != null) {
                values = valuesMethod.invoke(object);
            }
        }
        if (values != null) {
            final Set<Object> selectedItemIds = new HashSet<Object>();
            final Iterator<?> itemIds = select.getItemIds().iterator();
            while (itemIds.hasNext()) {
                Object itemId = itemIds.next();
                if (select.isSelected(itemId)) {
                    selectedItemIds.add(itemId);
                }
            }
            select.removeAllItems();
            setSelectValues(select, values, selectedItemIds);
        }
    }

    protected Object findComponent(final Repository repository,
                                   final Class type,
                                   final Object object,
                                   Object value,
                                   final Class[] componentTypeValue,
                                   final AbstractComponent[] componentValue,
                                   final String name,
                                   final String namePath,
                                   final ObjectView.FieldInfo fieldInfo,
                                   MethodProperty[] propertyValue)
        throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        AbstractValidator validator = null;
        boolean typeDisabled = false;
        Method valuesMethod = null;
        if (object != null) {
            valuesMethod = ObjectView.getMethod(object.getClass(),
                                     "values" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
        }
        if (value == null && defaultValues != null) {
            value = defaultValues.get(namePath);
            if (value != null) {
                fieldInfo.hasDefault = true;
            }
        }
        Converter aConverter = null;
        String aConversionError = null;
        final boolean makeValidator = componentValue != null && fieldInfo.validator == null && fieldInfo.isEditable;
        if (type.isEnum() || Set.class.isAssignableFrom(type) || valuesMethod != null) {
            Class componentType = null;
            final boolean isMultiSelect = Set.class.isAssignableFrom(type);
            if (isMultiSelect) {
                if (componentTypeValue != null) {
                    componentTypeValue[0] = Set.class;
                }
                if (value == null) {
                    value = type.newInstance();
                }
                componentType = getTypeParameter(repository, type, object, fieldInfo.namePath);
            } else {
                componentType = type;
            }
            final Object enumConstants = componentType.getEnumConstants();
            Object values = enumConstants;
            if (values == null) {
                if (valuesMethod != null) {
                    values = valuesMethod.invoke(object);
                }
            }
            if (values != null) {
                if (!isMultiSelect) {
                    if (value == null) {
                        if (values.getClass().isArray()) {
                            final int length = Array.getLength(values);
                            if (length > 0) {
                                value = Array.get(values, 0);
                            }
                        } else {
                            final List valuesList = (List)values;
                            if (!valuesList.isEmpty()) {
                                value = valuesList.get(0);
                            }
                        }
                    }
                }
            }
            if (componentValue != null) {
                AbstractSelect select = null;
                final boolean isGroup = hasWord(name, "group", false, false);
                if (isGroup) {
                    select = new OptionGroup() {
                            private final static long serialVersionUID = 0L;

                            @Override
                            public String getCaption() {
                                return getString(name);
                            }

                            @Override
                            public boolean isEnabled() {
                                return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                            }

                            @Override
                            public boolean isVisible() {
                                return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());

                            }

                        };
                    select.setMultiSelect(isMultiSelect);
                } else if (isMultiSelect) {
                    select = new TwinColSelect() {
                            private final static long serialVersionUID = 0L;

                            @Override
                            public String getCaption() {
                                return getString(name);
                            }

                            @Override
                            public boolean isEnabled() {
                                return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                            }

                            @Override
                            public boolean isVisible() {
                                return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());

                            }

                        };
                } else {
                    select = new ComboBox() {
                            private final static long serialVersionUID = 0L;

                            @Override
                            public String getCaption() {
                                return getString(name);
                            }

                            @Override
                            public boolean isEnabled() {
                                return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                            }

                            @Override
                            public boolean isVisible() {
                                return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());

                            }

                        };
                }
                select.setData(enumConstants != null ? enumConstants : object);
                if (values != null) {
                    Set selectedItemIds = null;
                    if (value instanceof Set) {
                        selectedItemIds = (Set)value;
                    } else {
                        selectedItemIds = new HashSet();
                        selectedItemIds.add(value);
                    }
                    setSelectValues(select, values, selectedItemIds);
                }
                if (fieldInfo.isLocale || fieldInfo.isTheme) {
                    select.setNullSelectionAllowed(false);
                }
                componentValue[0] = select;
                if (!fieldInfo.isLocale) {
                    selectMap.put(fieldInfo.namePath, select);
                }
            }
        } else if (Boolean.class.equals(type) || Boolean.TYPE.equals(type)) {
            if (componentValue != null) {
                componentValue[0] = new CheckBox() {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public String getCaption() {
                            return getString(name);
                        }

                        @Override
                        public boolean isEnabled() {
                            return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                        }

                        @Override
                        public boolean isVisible() {
                            return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                        }

                    };
                if (propertyValue != null) {
                    propertyValue[0] = new MethodProperty<Boolean>(Boolean.class, object, fieldInfo.accessor, fieldInfo.mutator);
                }
            }
            if (value == null) {
                value = Boolean.FALSE;
            }
        } else if (Calendar.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type)) {
            if (componentValue != null) {
                final DateField dateField
                    = new DateField() {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public String getCaption() {
                            return getString(name);
                        }

                        @Override
                        public boolean isEnabled() {
                            return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                        }

                        @Override
                        public boolean isVisible() {
                            return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                        }

                    };
                if (java.sql.Date.class.equals(type)) {
                    dateField.setResolution(DateField.RESOLUTION_DAY);
                    if (value == null) {
                        value = new java.sql.Date(System.currentTimeMillis());
                    }
                }
                componentValue[0] = dateField;
                if (propertyValue != null) {
                    if (Calendar.class.isAssignableFrom(type)) {
                        propertyValue[0] = new CalendarMethodProperty(object, fieldInfo);
                    } else {
                        propertyValue[0] = new MethodProperty<Boolean>(Boolean.class, object, fieldInfo.accessor, fieldInfo.mutator);
                    }
                }
            }
        } else if (Character.class.equals(type) || Character.TYPE.equals(type)) {
            if (value == null) {
                value = '0';
            }
            if (makeValidator) {
                aConverter = ObjectView.characterConverter;
                aConversionError = "Invalid character value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Character>(Character.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (Byte.class.equals(type) || Byte.TYPE.equals(type)) {
            if (value == null) {
                value = (byte)0;
            }
            if (makeValidator) {
                aConverter = ObjectView.byteConverter;
                aConversionError = "Invalid byte value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Byte>(Byte.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (Short.class.equals(type) || Short.TYPE.equals(type)) {
            if (value == null) {
                value = (short)0;
            }
            if (makeValidator) {
                aConverter = ObjectView.shortConverter;
                aConversionError = "Invalid short value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Short>(Short.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (Integer.class.equals(type) || Integer.TYPE.equals(type)) {
            if (value == null) {
                value = 0;
            }
            if (makeValidator) {
                aConverter = ObjectView.integerConverter;
                aConversionError = "Invalid integer value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Integer>(Integer.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (Long.class.equals(type) || Long.TYPE.equals(type)) {
            if (value == null) {
                value = 0L;
            }
            if (makeValidator) {
                aConverter = ObjectView.longConverter;
                aConversionError = "Invalid long value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Long>(Long.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (Float.class.equals(type) || Float.TYPE.equals(type)) {
            if (value == null) {
                value = 0.0f;
            }
            if (makeValidator) {
                aConverter = ObjectView.floatConverter;
                aConversionError = "Invalid float value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Float>(Float.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (Double.class.equals(type) || Double.TYPE.equals(type)) {
            if (value == null) {
                value = 0.0;
            }
            if (makeValidator) {
                aConverter = ObjectView.doubleConverter;
                aConversionError = "Invalid double value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<Double>(Double.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (BigDecimal.class.equals(type)) {
            if (value == null) {
                value = BigDecimal.valueOf(0L);
            }
            if (makeValidator) {
                aConverter = ObjectView.bigDecimalConverter;
                aConversionError = "Invalid big decimal value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<BigDecimal>(BigDecimal.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (BigInteger.class.equals(type)) {
            if (value == null) {
                value = BigInteger.valueOf(0L);
            }
            if (makeValidator) {
                aConverter = ObjectView.bigIntegerConverter;
                aConversionError = "Invalid big integer value";
            }
            if (propertyValue != null) {
                propertyValue[0] = new MethodProperty<BigInteger>(BigInteger.class, object, fieldInfo.accessor, fieldInfo.mutator);
            }
        } else if (fieldInfo.isLink) {
            if (value == null) {
                value = object;
            }
            if (componentValue != null) {
                Object componentObject = null;
                if (fieldInfo.accessor != null) {
                    componentObject = fieldInfo.accessor.invoke(object);
                } else {
                    componentObject = object;
                }
                final String linkName = (String)callMethod(componentObject, "getName");
                final Method navigator = ObjectView.getMethod(type, "navigate");
                AbstractComponent linkComponent = null;
                if (navigator != null) {
                    final Button button = new Button() {
                            private final static long serialVersionUID = 0L;

                            @Override
                            public String getCaption() {
                                if (linkName != null) {
                                    return getString(linkName);
                                }
                                return null;
                            }

                            @Override
                            public boolean isEnabled() {
                                return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                            }

                            @Override
                            public boolean isVisible() {
                                return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                            }

                        };
                    button.addListener(new NavigatorListener(componentObject, navigator));
                    button.setStyleName(BaseTheme.BUTTON_LINK);
                    linkComponent = button;
                    typeDisabled = true;
                } else {
                    final Link link = new Link() {
                            private final static long serialVersionUID = 0L;

                            @Override
                            public String getCaption() {
                                if (linkName != null) {
                                    return getString(linkName);
                                }
                                return null;
                            }

                            @Override
                            public boolean isEnabled() {
                                return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                            }

                            @Override
                            public boolean isVisible() {
                                return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                            }

                        };
                    final String source = (String)callMethod(componentObject, "getSource");
                    if (source != null) {
                        link.setResource(new ExternalResource(source));
                    }
                    final String targetName = (String)callMethod(componentObject, "getTarget");
                    if (targetName != null) {
                        link.setTargetName(targetName);
                    }
                    linkComponent = link;
                }
                componentValue[0] = linkComponent;
            }
        } else if (!type.isPrimitive()
                   && (type.getPackage().getName().startsWith("com.vaadin.")
                       || type.getPackage().getName().startsWith("org.vaadin."))) {
            Object componentObject = null;
            if (fieldInfo.accessor != null) {
                componentObject = fieldInfo.accessor.invoke(object);
            } else {
                componentObject = object;
            }
            if (value == null) {
                value = componentObject;
            }
            if (componentValue != null) {
                componentValue[0] = (AbstractComponent)componentObject;
            }
            fieldInfo.isCustom = true;
            fieldInfo.isEditable = true;
            typeDisabled = true;
        }
        if (componentValue != null) {
            if (componentValue[0] == null) {
                if (isText(name)) {
                    final Label label = new Label() {
                            private final static long serialVersionUID = 0L;

                            public boolean isVisible() {
                                return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                            }

                        };
                    if (hasWord(name, "mainTitle", true, false)) {
                        label.setStyleName(Reindeer.LABEL_H1);
                    } else if (hasWord(name, "title", true, false)) {
                        label.setStyleName(Reindeer.LABEL_H2);
                    }
                    componentValue[0] = label;
                } else {
                    final Converter converter = aConverter;
                    final String conversionError = aConversionError;
                    final AbstractTextField textField = hasWord(name, "password", true, true)
                        ? new PasswordField() {
                                private final static long serialVersionUID = 0L;

                                @Override
                                public String getCaption() {
                                    return getString(name);
                                }

                                @Override
                                public boolean isEnabled() {
                                    return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                                }

                                @Override
                                public boolean isVisible() {
                                    return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                                }

                            }
                        : new TextField() {
                                private final static long serialVersionUID = 0L;

                                @Override
                                public String getCaption() {
                                    return getString(name);
                                }

                                @Override
                                public boolean isEnabled() {
                                    return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                                }

                                @Override
                                public boolean isVisible() {
                                    return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                                }

                                @Override
                                public Converter getConverter() {
                                    if (converter != null) {
                                        return converter;
                                    }
                                    return super.getConverter();
                                }

                                @Override
                                public String getConversionError() {
                                    if (conversionError != null) {
                                        return getString(conversionError);
                                    }
                                    return super.getConversionError();
                                }

                            };
                    textField.setNullRepresentation("");
                    if (fieldInfo.validator != null) {
                        validator = new ObjectViewValidator(object, fieldInfo.validator);
                    }
                    if (validator != null) {
                        textField.addValidator(validator);
                    }
                    componentValue[0] = textField;
                }
                if (propertyValue != null && propertyValue[0] == null) {
                    propertyValue[0] = new MethodProperty<String>(String.class, object, fieldInfo.accessor, fieldInfo.mutator);
                }
            }
        }
        if (typeDisabled) {
            return null;
        }
        if (value == null) {
            try {
                value = type.newInstance();
            } catch (final IllegalAccessException illegalAccessException) {
                try {
                    Method newInstance = type.getMethod("getInstance");
                    value = newInstance.invoke(null);
                } catch (final NoSuchMethodException noSuchMethodException) {}
            }
        }
        if (componentTypeValue != null) {
            if (componentTypeValue[0] == null) {
                if (value != null) {
                    componentTypeValue[0] = value.getClass();
                } else {
                    componentTypeValue[0] = type;
                }
            }
        }
        return value;
    }

    protected Form makeForm(final String namePath,
                            final Object object,
                            final Class<?> objectType) {
        final Method objectDisabled = ObjectView.getMethod(objectType, "disabled"),
            objectInvisible = ObjectView.getMethod(objectType, "invisible");
        final Form form = new ObjectViewForm() {
                private final static long serialVersionUID = 0L;

                @Override
                public boolean isEnabled() {
                    return checkFlag(disabledSet, namePath, object, objectDisabled, super.isEnabled());
                }

                @Override
                public boolean isVisible() {
                    return checkFlag(invisibleSet, namePath, object, objectInvisible, super.isVisible());
                }

            };
        form.setImmediate(true);
        //form.setWriteThrough(false);
        return form;
    }

    protected void checkServiceFieldInfo(final String name,
                                         final Map<String, ObjectView.FieldInfo> fieldInfoMap,
                                         final ObjectView.FieldInfo fieldInfo) {
        final ObjectView.FieldInfo serviceFieldInfo = fieldInfoMap.get(name);
        if (serviceFieldInfo != null) {
            if (fieldInfo.isRequest) {
                serviceFieldInfo.requestFieldInfo = fieldInfo;
            } else {
                serviceFieldInfo.responseFieldInfo = fieldInfo;
            }
        }
    }

    protected <T> T getModelParameter(final Class objectType,
                                      final Object object,
                                      final String name)
        throws IllegalAccessException, InvocationTargetException {
        final Method method = object != null ? ObjectView.getMethod(objectType, "model" + name) : null;
        return method != null ? (T)method.invoke(object) : null;
    }

    protected Form makeForm(final Repository repository,
                            final String namePath,
                            final Object object,
                            Class<?> objectType,
                            final List<String> nameList,
                            final IndexedContainer indexedContainer,
                            final List<ObjectView.FieldInfo> fieldInfoList,
                            final Map<String, ObjectView.FieldInfo> fieldInfoMap,
                            final Map<String, ObjectView.FieldInfo> formFieldInfoMap,
                            final ComponentContainer container,
                            final boolean inModelMethod,
                            final boolean modelOnly,
                            boolean isReadOnly)
        throws ClassNotFoundException, IOException, IllegalAccessException,
               InstantiationException, InvocationTargetException, NoSuchMethodException,
               SAXException, TransformerException, URISyntaxException, WSDLException {
        String namePathBase = namePath;
        if (!"".equals(namePathBase)) {
            namePathBase += ".";
        }
        if (objectType == null) {
            objectType = object.getClass();
        }
        Form form = null;
        FormClickListener formClickListener = null;
        if (indexedContainer == null && container == null && !modelOnly) {
            form = makeForm(namePath, object, objectType);
            formClickListener = new FormClickListener(fieldInfoMap, object);
            final Method layoutTypeMethod = ObjectView.getMethod(objectType, "layoutType");
            if (layoutTypeMethod != null) {
                final ObjectView.LayoutType layoutType = (ObjectView.LayoutType)layoutTypeMethod.invoke(object);
                if (layoutType != null) {
                    final AbstractLayout layout = layoutType.getType().newInstance();
                    form.setLayout(layout);
                }
            }
        }
        final Method methods[] = objectType.getMethods();
        final JavaClass javaClass = repository.loadClass(objectType);
        final org.apache.bcel.classfile.Method[] classMethods = javaClass.getMethods();
        for (final org.apache.bcel.classfile.Method classMethod : classMethods) {
            if (!classMethod.isPublic()) {
                continue;
            }
            final String methodName = classMethod.getName();
            if (defaultLocale == null && "themes".equals(methodName)) {
                final Method themes = objectType.getMethod(methodName, String[].class);
                themes.invoke(object, new Object[] {getObjectViewApplication().getThemes()});
            }
            if (methodName.startsWith("<")
                || classMethod.getArgumentTypes().length > 0
                || "navigate".equals(methodName) || "start".equals(methodName)
                || "layoutType".equals(methodName) || "sizeFull".equals(methodName)
                || "storageType".equals(methodName) || "storageDirectory".equals(methodName)
                || "defaultValues".equals(methodName)
                || "disabledSet".equals(methodName) || "invisibleSet".equals(methodName)
                || "toString".equals(methodName)
                || hasWord(methodName, "model", false, false)
                || hasWord(methodName, "namespace", true, false)
                || hasWord(methodName, "client", false, false)
                || hasWord(methodName, "set", false, false)
                || hasWord(methodName, "edit", false, false)
                || hasWord(methodName, "id", true, false)
                || hasWord(methodName, "disabled", true, false) || hasWord(methodName, "invisible", true, false)
                || hasWord(methodName, "values", false, false) || hasWord(methodName, "icon", false, false)
                || hasWord(methodName, "arguments", false, false)) {
                continue;
            }
            final Method method = objectType.getMethod(methodName);
            Class type = method.getReturnType();
            final ObjectView.FieldInfo fieldInfo = new ObjectView.FieldInfo();
            ObjectViewWebModel objectViewWebModel = null;
            String baseName = null, fieldName = null, name = null;
            boolean isModel = false, isModelMethod = false;
            if (type != null && hasWord(methodName, "get", false, false)) {
                isModelMethod = methodName.endsWith("Store") || methodName.endsWith("Search")
                    || methodName.endsWith("Remove");
                fieldName = methodName.substring(3);
                if (object != null) {
                    checkNamespace(object, fieldName, fieldInfo);
                }
                name = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                if (hasWord(methodName, "model", false, true)) {
                    final String modelName = fieldName.substring(0, fieldName.length() - "Model".length());
                    objectViewWebModel
                        = ObjectViewWebModel.getObjectViewWebModel(getObjectViewApplication(),
                                                                   (String)getModelParameter(objectType, object, modelName + "WsdlURI"),
                                                                   (String)getModelParameter(objectType, object, modelName + "WebServiceName"),
                                                                   (String)getModelParameter(objectType, object, modelName + "WebServicePort"),
                                                                   (String)getModelParameter(objectType, object, modelName + "WebOperationName"));
                    type = objectViewWebModel.getModelClass();
                }
                fieldInfo.type = type;
                fieldInfo.isRequest = fieldName.endsWith("Request");
                fieldInfo.isResponse = fieldName.endsWith("Response");
                if (fieldInfo.isRequest || fieldInfo.isResponse) {
                    isModelMethod |= true;
                    final String serviceName = fieldInfo.isRequest
                        ? name.substring(0, name.length() - "Request".length())
                        : name.substring(0, name.length() - "Response".length());
                    checkServiceFieldInfo(serviceName + "Retrieve", fieldInfoMap, fieldInfo);
                    checkServiceFieldInfo(serviceName + "Update", fieldInfoMap, fieldInfo);
                    checkServiceFieldInfo(serviceName + "Add", fieldInfoMap, fieldInfo);
                    checkServiceFieldInfo(serviceName + "Delete", fieldInfoMap, fieldInfo);
                    checkServiceFieldInfo(serviceName + "Call", fieldInfoMap, fieldInfo);
                    baseName = serviceName;
                }
                fieldInfo.isArray = type.isArray();
                fieldInfo.isList = List.class.isAssignableFrom(type);
                fieldInfo.isLink = !type.isPrimitive() && hasWord(name, "link", true, false);
                int nameIndex = fieldInfo.isLink ? "link".length() : 0;
                fieldInfo.isForm = hasWord(name.substring(nameIndex), "form", false, false);
                if (fieldInfo.isForm) {
                    nameIndex += "form".length();
                }
                fieldInfo.isOptional = hasWord(name.substring(nameIndex), "optional", false, false);
                if (fieldInfo.isOptional) {
                    nameIndex += "optional".length();
                }
                if (hasWord(name.substring(nameIndex), "readOnly", false, false)) {
                    isReadOnly = true;
                    nameIndex += "readOnly".length();
                }
                fieldInfo.isSimple = isSimple(name, type, object);
                fieldInfo.isLocale = "locale".equals(name);
                fieldInfo.isTheme = "theme".equals(name);
                Class parameterType = null;
                if (objectViewWebModel != null) {
                    parameterType = Object.class;
                } else if (Set.class.isAssignableFrom(type)) {
                    parameterType = Set.class;
                } else {
                    parameterType = type;
                }
                try {
                    fieldInfo.mutator = objectType.getMethod("set" + fieldName, parameterType);
                    fieldInfo.isEditable = !isReadOnly && !fieldInfo.isResponse;
                } catch (final NoSuchMethodException ex) {}
                try {
                    fieldInfo.editor = objectType.getMethod("edit" + fieldName, parameterType);
                } catch (final NoSuchMethodException ex) {
                    fieldInfo.editor = ObjectView.getMethod(methods, "edit" + fieldName);
                }
                fieldInfo.isVolatile = isModelMethod || hasWord(name.substring(nameIndex), "volatile", false, false) || isText(name) || !fieldInfo.isEditable;
                if (fieldInfo.isEditable) {
                    try {
                        fieldInfo.validator = objectType.getMethod("validate" + fieldName, Object.class);
                    } catch (final NoSuchMethodException ex) {}
                }
                fieldInfo.accessor = method;
                if (fieldInfoMap != null && !isText(name)) {
                    fieldInfoMap.put(name, fieldInfo);
                }
                if (hasWord(name, "storage", false, false) || hasWord(name, "model", false, false)) {
                    isModel = true;
                } else if (!isText(name) && !fieldInfo.isForm) {
                    if (indexedContainer != null && !isModelMethod && !modelOnly
                        && fieldInfo.isSimple) {
                        final Class[] componentTypeValue = new Class[1];
                        Object defaultValue
                            = findComponent(repository, type, null, null, componentTypeValue,
                                            null, name, namePathBase + name, fieldInfo, null);
                        if (Character.class.equals(componentTypeValue[0])
                            || Character.TYPE.equals(componentTypeValue[0])) {
                            componentTypeValue[0] = String.class;
                            if (defaultValue != null) {
                                defaultValue = defaultValue.toString();
                            }
                        }
                        indexedContainer.addContainerProperty(name, componentTypeValue[0], defaultValue);
                        if (nameList != null) {
                            nameList.add(name);
                        }
                    }
                }
                fieldInfo.isAttribute = name.endsWith("Attribute");
            } else {
                fieldName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
                name = methodName;
                fieldInfo.executor = method;
                if (methodName.endsWith("Retrieve") || methodName.endsWith("Update")
                    || methodName.endsWith("Add") || methodName.endsWith("Delete")
                    || methodName.endsWith("Call")) {
                    final String direction = methodName.endsWith("ClientRetrieve")
                        || methodName.endsWith("ClientUpdate") || methodName.endsWith("ClientAdd")
                        || methodName.endsWith("ClientDelete") || methodName.endsWith("ClientCall") ? "Client" : "";
                    if ("".equals(direction)) {
                        isModelMethod |= true;
                    }
                    final String serviceName = methodName.endsWith("Retrieve")
                        ? name.substring(0, name.length() - direction.length() - "Retrieve".length())
                        : methodName.endsWith("Update")
                        ? name.substring(0, name.length() - direction.length() - "Update".length())
                        : methodName.endsWith("Add")
                        ? name.substring(0, name.length() - direction.length() - "Add".length())
                        : methodName.endsWith("Delete")
                        ? name.substring(0, name.length() - direction.length() - "Delete".length())
                        : name.substring(0, name.length() - direction.length() - "Call".length());
                    final ObjectView.FieldInfo requestFieldInfo = fieldInfoMap.get(serviceName + "Request");
                    if (requestFieldInfo != null) {
                        fieldInfo.requestFieldInfo = requestFieldInfo;
                    }
                    final ObjectView.FieldInfo responseFieldInfo = fieldInfoMap.get(serviceName + "Response");
                    if (responseFieldInfo != null) {
                        fieldInfo.responseFieldInfo = responseFieldInfo;
                    }
                    fieldInfo.isClient = "Client".equals(direction);
                }
            }
            fieldInfo.object = object;
            fieldInfo.name = baseName != null ? baseName : name;
            fieldInfo.namePath = namePathBase + name;
            fieldInfo.disabled = ObjectView.getMethod(objectType, "disabled" + fieldName);
            fieldInfo.invisible = ObjectView.getMethod(objectType, "invisible" + fieldName);
            fieldInfo.formFieldInfoMap = formFieldInfoMap;
            if (storage != null) {
                fieldInfo.property = storage.makeProperty(name, objectType);
            }
            if (fieldInfoList != null) {
                fieldInfoList.add(fieldInfo);
            }
            if ((!fieldInfo.isVolatile || indexedContainer == null)
                && (!inModelMethod || !isModelMethod)) {
                final AbstractComponent component
                    = (AbstractComponent)makeComponent(repository, form, name, namePathBase + name, type, object,
                                                       formClickListener, fieldInfo, fieldInfoMap, formFieldInfoMap,
                                                       inModelMethod || isModelMethod,
                                                       isModel || modelOnly || isModelMethod
                                                       || indexedContainer != null);
                if (component != null) {
                    if (container != null) {
                        componentMap.put(namePathBase + name, component);
                        container.addComponent(component);
                        continue;
                    }
                    if (component instanceof Field) {
                        form.addField(name, (Field)component);
                    } else {
                        form.getLayout().addComponent(component);
                    }
                }
            }
        }
        if (formClickListener != null) {
            formClickListener.putData();
        }
        if (form != null) {
            componentMap.put(namePath, form);
            form.setData(fieldInfoMap);
        }
        return form;
    }

    protected Layout makeTable(final Repository repository,
                               final Form form,
                               final String name,
                               final String namePath,
                               final Object object,
                               final IndexedContainer indexedContainer,
                               final Class elementType,
                               final List<String> nameList,
                               final List<Object> objectList,
                               final FormClickListener formClickListener,
                               final ObjectView.FieldInfo fieldInfo,
                               final boolean isSingle)
        throws IllegalAccessException, InvocationTargetException {
        final VerticalLayout verticalLayout = new VerticalLayout() {
                private final static long serialVersionUID = 0L;

                @Override
                public boolean isVisible() {
                    return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                }

            };
        verticalLayout.setHeight(-1, Sizeable.Unit.POINTS);
        final Table table = new Table() {
                private final static long serialVersionUID = 0L;

                @Override
                public String getColumnHeader(Object propertyId) {
                    return getString(name + "." + propertyId);
                }

                @Override
                public boolean isEnabled() {
                    return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                }

                @Override
                protected String formatPropertyValue(Object rowId, Object colId, Property property) {
                    final Class type = property.getType();
                    if (Date.class.isAssignableFrom(type)) {
                        return getDateFormat(type).format(property.getValue());
                    }
                    return super.formatPropertyValue(rowId, colId, property);
                }

            };
        final Label label = new Label(new StringProperty(name));
        verticalLayout.addComponent(label);
        table.setContainerDataSource(indexedContainer);
        table.setSizeFull();
        table.setVisibleColumns(nameList.toArray(new Object[nameList.size()]));
        TableData tableData = null;
        if (fieldInfo.isEditable) {
            table.setEditable(true);
            table.setSelectable(true);
            table.setImmediate(true);
            final TableClickListener tableClickListener
                = new TableClickListener();
            final HorizontalLayout horizontalLayout
                = new HorizontalLayout();
            horizontalLayout.setHeight(-1, Sizeable.Unit.POINTS);
            final Button addButton = new Button("", tableClickListener) {
                    private final static long serialVersionUID = 0L;

                    @Override
                    public String getCaption() {
                        return getString("add");
                    }

                },
                removeButton = new Button("", tableClickListener) {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public String getCaption() {
                            return getString("remove");
                        }
                    };
            horizontalLayout.addComponent(addButton);
            horizontalLayout.addComponent(removeButton);
            verticalLayout.addComponent(horizontalLayout);
            tableData = tableClickListener;
            table.setTableFieldFactory(new ObjectViewTableFieldFactory(repository, namePath, tableData));
        } else {
            tableData = new TableData();
        }
        formClickListener.tableDataList.add(tableData);
        tableData.table = table;
        tableData.dataList = objectList;
        tableData.dataMap = new HashMap<Object, Object>();
        tableData.elementType = elementType;
        tableData.isSingle = isSingle;
        tableData.fieldInfoMap = fieldInfo.fieldInfoMap;
        tableData.namePath = namePath;
        verticalLayout.setData(tableData);
        verticalLayout.addComponent(table);
        componentMap.put(namePath, verticalLayout);
        return verticalLayout;
    }

    protected boolean isSimple(final String name,
                               final Class type,
                               final Object object) {
        if (type.isPrimitive() || type.isEnum() || Set.class.isAssignableFrom(type)
            || !type.isPrimitive() && hasWord(name, "link", true, false)) {
            return true;
        }
        if (object != null) {
            final Method valuesMethod
                = ObjectView.getMethod(object.getClass(), "values" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            if (valuesMethod != null) {
                return true;
            }
        }
        final Package typePackage = type.getPackage();
        if (typePackage == null) {
            return false;
        }
        final String packageName = typePackage.getName();
        return packageName.startsWith("java.") || packageName.startsWith("javax.")
            || packageName.startsWith("com.vaadin.") || packageName.startsWith("org.vaadin.");
    }

    protected Resource getResource(final String name) {
        if (name.indexOf(":") != -1) {
            return new ExternalResource(name);
        }
        return new ClassResource(UI.getCurrent().getClass(), name);
    }

    protected void addMenu(final MenuBar menuBar,
                           final MenuBar.MenuItem parentMenuItem,
                           final List<?> list)
        throws IllegalAccessException, InvocationTargetException {
        for (Object item : list) {
            final Class itemType = item.getClass();
            final String itemName = (String)callMethod(item, "getName");
            final Method iconMethod = ObjectView.getMethod(itemType, "getIcon");
            Resource icon = null;
            if (iconMethod != null) {
                final String iconName = (String)iconMethod.invoke(item);
                icon = getResource(iconName);
            }
            final MenuBar.Command command = new ObjectViewCommand(item);
            final String caption = getString(itemName);
            final MenuBar.MenuItem menuItem
                = parentMenuItem != null ? parentMenuItem.addItem(caption, icon, command)
                : menuBar.addItem(caption, icon, command);
            menuItem.setDescription(itemName);
            final Method menuMethod = ObjectView.getMethod(itemType, "getMenu");
            if (menuMethod != null) {
                final List itemList = (List)menuMethod.invoke(item);
                if (itemList != null) {
                    addMenu(null, menuItem, itemList);
                }
            }
        }
    }

    protected Component makeComponent(final Repository repository,
                                      final Form form,
                                      final String name,
                                      final String namePath,
                                      final Class type,
                                      final Object object,
                                      final FormClickListener formClickListener,
                                      final ObjectView.FieldInfo fieldInfo,
                                      final Map<String, ObjectView.FieldInfo> fieldInfoMap,
                                      final Map<String, ObjectView.FieldInfo> formFieldInfoMap,
                                      final boolean inModelMethod,
                                      final boolean modelOnly)
        throws ClassNotFoundException, IOException, IllegalAccessException,
               InstantiationException, InvocationTargetException, NoSuchMethodException,
               SAXException, TransformerException, URISyntaxException, WSDLException {
        final Class<?> objectType = object != null ? object.getClass() : null;
        if (fieldInfo.executor != null) {
            fieldInfo.fieldInfoMap = fieldInfoMap;
            executorFieldInfoMap.put(namePath, fieldInfo);
            if (modelOnly) {
                return null;
            }
            final Method iconMethod
                = ObjectView.getMethod(objectType, "icon" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            final Button button = new Button("commit") {
                    private final static long serialVersionUID = 0L;

                    @Override
                    public String getCaption() {
                        if (iconMethod == null) {
                            return getString(name);
                        }
                        return null;
                    }

                    @Override
                    public boolean isEnabled() {
                        return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                    }

                    @Override
                    public boolean isVisible() {
                        return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                    }

                };
            if (iconMethod != null) {
                final String resourceName = (String)iconMethod.invoke(object);
                button.setIcon(getResource(resourceName));
                button.setStyleName(BaseTheme.BUTTON_LINK);
            }
            button.addListener(formClickListener);
            formClickListener.executorFieldInfoMap.put(button, fieldInfo);
            if (namePath != null) {
                componentMap.put(namePath, (AbstractComponent)button);
            }
            form.getLayout().addComponent(button);
            button.setData(fieldInfoMap);
            return null;
        }
        if (!fieldInfo.isList && isSimple(name, type, object)) {
            if (modelOnly) {
                return null;
            }
            final Class[] componentTypeValue = new Class[1];
            final AbstractComponent[] componentValue = new AbstractComponent[1];
            Object value = null;
            if (fieldInfo.accessor != null && object != null) {
                value = fieldInfo.accessor.invoke(object);
            }
            final MethodProperty[] propertyValue = new MethodProperty[1];
            final Object defaultValue
                = findComponent(repository, type, object, value, componentTypeValue, componentValue,
                                name, namePath, fieldInfo, propertyValue);
            final AbstractComponent component = componentValue[0];
            Class componentType = componentTypeValue[0];
            AbstractField field = null;
            if ((componentType != null || fieldInfo.isCustom)
                && component instanceof Field) {
                field = (AbstractField)component;
                if (!fieldInfo.isEditable) {
                    field.setReadOnly(true);
                }
            }
            if (componentType != null) {
                if (fieldInfo.isLocale) {
                    if (defaultLocale == null && fieldInfo.mutator != null && value == null) {
                        final Locale locale = UI.getCurrent().getLocale();
                        fieldInfo.mutator.invoke(object, locale.toString());
                    }
                } else if (fieldInfo.isTheme) {
                    if (defaultLocale == null && fieldInfo.mutator != null && value == null) {
                        String theme = UI.getCurrent().getTheme();
                        if (theme == null) {
                            theme = "reindeer";
                        }
                        fieldInfo.mutator.invoke(object, theme);
                    }
                } else if (fieldInfo.hasDefault || !fieldInfo.isOptional) {
                    if (fieldInfo.mutator != null && value == null) {
                        fieldInfo.mutator.invoke(object, defaultValue);
                    }
                    if (field != null) {
                        field.setRequired(fieldInfo.isEditable);
                    }
                }
                if (fieldInfo.accessor == null) {
                    throw new RuntimeException("Accessor not found in " + name);
                }
                component.setImmediate(true);
                if (component instanceof Property.Viewer) {
                    if (Calendar.class.isAssignableFrom(componentType)) {
                        componentType = Date.class;
                    }
                    if (Character.class.equals(componentType) || isText(name)) {
                        final Method argumentsMethod
                            = ObjectView.getMethod(object.getClass(), "arguments"
                                        + Character.toUpperCase(name.charAt(0)) + name.substring(1));
                        ((Property.Viewer)component).setPropertyDataSource(new ObjectViewPropertyFormatter(propertyValue[0], componentType, object,
                                                                                                           argumentsMethod));
                    } else {
                        if (Date.class.isAssignableFrom(componentType)) {
                            ((DateField)component).setDateFormat(getDateFormat(componentType).toLocalizedPattern());
                        }
                        ((Property.Viewer)component).setPropertyDataSource(propertyValue[0]);
                    }
                }
            }
            if (fieldInfo.isEditable) {
                if (field != null) {
                    if (fieldInfo.editor != null && field instanceof Property.ValueChangeNotifier) {
                        ((Property.ValueChangeNotifier)field).addListener(new EditorListener(object, fieldInfo.editor, name + "Search", fieldInfo.isLocale, fieldInfo.isTheme, fieldInfoMap));
                    }
                }
            }
            if (namePath != null) {
                componentMap.put(namePath, component);
            }
            if (form != null && component instanceof Button) {
                form.getLayout().addComponent(component);
                return null;
            }
            return component;
        }
        Object value = null;
        if (fieldInfo.accessor != null && object != null) {
            value = fieldInfo.accessor.invoke(object);
        }
        if (value == null) {
            try {
                value = type.newInstance();
            } catch (final InstantiationException instantiationException) {
                throw new RuntimeException(namePath, instantiationException);
            }
            if (fieldInfo.mutator != null) {
                fieldInfo.mutator.invoke(object, value);
            }
        }
        if (fieldInfo.isArray || fieldInfo.isList) {
            if (fieldInfo.isArray) {
                final List list = new ArrayList();
                final int length = Array.getLength(value);
                for (int index = 0; index < length; index++) {
                    list.add(Array.get(value, index));
                }
                value = list;
            }
            if (hasWord(name, "form", false, false)) {
                Form listForm = null;
                if (!modelOnly) {
                    listForm = makeForm(namePath, object, objectType);
                    final List list = (List)value;
                    final List<ObjectView.FieldInfo> fieldInfoList = new ArrayList<ObjectView.FieldInfo>();
                    listForm.setFormFieldFactory(new ObjectViewFormFieldFactory(repository, name, namePath, fieldInfoList));
                    listForm.setItemDataSource(new CollectionItem(list));
                    form.addField(name, listForm);
                    fieldInfo.fieldInfoList = fieldInfoList;
                }
                if (namePath != null) {
                    if (listForm != null) {
                        componentMap.put(namePath, listForm);
                    }
                    if (storage != null) {
                        fieldInfo.property = storage.makeProperty(name, objectType);
                    }
                }
                return null;
            }
            if (hasWord(name, "tab", true, false)) {
                String namePathBase = namePath;
                if (!"".equals(namePathBase)) {
                    namePathBase += ".";
                }
                AbstractComponent component = null;
                TabSheet tabSheet = null;
                if (!modelOnly) {
                    tabSheet = new TabSheet();
                    final List list = (List)value;
                    final List<ObjectView.FieldInfo> fieldInfoList = new ArrayList<ObjectView.FieldInfo>();
                    for (Object item : list) {
                        final String itemName = item.getClass().getSimpleName();
                        final List<ObjectView.FieldInfo> componentFieldInfoList = new ArrayList<ObjectView.FieldInfo>();
                        final Map<String, ObjectView.FieldInfo> componentFieldInfoMap = new HashMap<String, ObjectView.FieldInfo>();
                        final AbstractComponent layout
                            = makeLayout(repository, namePathBase + itemName, item, tabSheet,
                                         componentFieldInfoList, componentFieldInfoMap);
                        final ObjectView.FieldInfo componentFieldInfo = new ObjectView.FieldInfo();
                        componentFieldInfo.name = name;
                        componentFieldInfo.namePath = namePath;
                        componentFieldInfo.fieldInfoList = componentFieldInfoList;
                        componentFieldInfo.fieldInfoMap = componentFieldInfoMap;
                        checkNamespace(item, "", componentFieldInfo);
                        fieldInfoList.add(componentFieldInfo);
                        if (component == null) {
                            component = layout;
                        }
                    }
                    fieldInfo.fieldInfoList = fieldInfoList;
                    tabSheet.addListener(new StarterListener());
                }
                if (namePath != null) {
                    if (tabSheet != null) {
                        componentMap.put(namePath, tabSheet);
                        tabSheetMap.put(namePath, tabSheet);
                    }
                    if (storage != null) {
                        fieldInfo.property = storage.makeProperty(name, objectType);
                    }
                }
                if (component != null) {
                    invokeStarter(component);
                }
                return tabSheet;
            }
            if (hasWord(name, "link", true, false)) {
                if (modelOnly) {
                    return null;
                }
                final List list = (List)value;
                final AbstractComponent[] componentValue = new AbstractComponent[1];
                fieldInfo.accessor = null;
                for (Object item : list) {
                    findComponent(repository, item.getClass(), item, null, null, componentValue,
                                  name, namePath, fieldInfo, null);
                    final AbstractComponent component = componentValue[0];
                    if (component instanceof Field) {
                        form.addField(name, (Field)component);
                    } else {
                        form.getLayout().addComponent(component);
                    }
                }
                return null;
            }
            if (hasWord(name, "menu", true, true)) {
                fieldInfo.isMenu = true;
                if (modelOnly) {
                    return null;
                }
                if (value != null) {
                    final MenuBar menuBar = new MenuBar();
                    menuBar.setAutoOpen(true);
                    addMenu(menuBar, null, (List<?>)value);
                    form.getLayout().addComponent(menuBar);
                    menuBarMap.put(namePath, menuBar);
                }
                return null;
            }
            final List<String> nameList = new ArrayList<String>();
            Class elementType = null;
            if (fieldInfo.isList) {
                elementType = getTypeParameter(repository, type, object, fieldInfo.namePath);
            } else {
                elementType = type.getComponentType();
            }
            fieldInfo.elementType = elementType;
            final IndexedContainer indexedContainer = new IndexedContainer();
            if (isSimple(name, elementType, object)) {
                nameList.add(name);
                if (modelOnly) {
                    return null;
                }
                Object defaultValue
                    = findComponent(repository, elementType, null, null, null, null, name, namePath, fieldInfo, null);
                if (defaultValue instanceof Character) {
                    defaultValue = defaultValue.toString();
                }
                indexedContainer.addContainerProperty(name, defaultValue.getClass(), defaultValue);
                final Map<String, ObjectView.FieldInfo> componentFieldInfoMap
                    = new HashMap<String, ObjectView.FieldInfo>();
                componentFieldInfoMap.put(name, fieldInfo);
                fieldInfo.fieldInfoMap = componentFieldInfoMap;
                return makeTable(repository, form, name, namePath, object, indexedContainer, elementType, nameList,
                                 (List<Object>)value, formClickListener, fieldInfo, true);
            }
            final List<ObjectView.FieldInfo> componentFieldInfoList = new ArrayList<ObjectView.FieldInfo>();
            final Map<String, ObjectView.FieldInfo> componentFieldInfoMap
                = new HashMap<String, ObjectView.FieldInfo>();
            makeForm(repository, namePath, null, elementType, nameList, indexedContainer,
                     componentFieldInfoList, componentFieldInfoMap, null, null, inModelMethod, modelOnly,
                     !fieldInfo.isEditable);
            fieldInfo.fieldInfoList = componentFieldInfoList;
            fieldInfo.fieldInfoMap = componentFieldInfoMap;
            if (modelOnly) {
                return null;
            }
            return makeTable(repository, form, name, namePath, object, indexedContainer, elementType, nameList,
                             (List<Object>)value, formClickListener, fieldInfo, false);
        }
        if (hasWord(name, "layout", true, false)) {
            AbstractComponent component = null;
            if (!modelOnly) {
                final List<ObjectView.FieldInfo> componentFieldInfoList = new ArrayList<ObjectView.FieldInfo>();
                final Map<String, ObjectView.FieldInfo> componentFieldInfoMap = new HashMap<String, ObjectView.FieldInfo>();
                component = makeLayout(repository, namePath, value, null,
                                       componentFieldInfoList, componentFieldInfoMap);
                fieldInfo.fieldInfoList = componentFieldInfoList;
                fieldInfo.fieldInfoMap = componentFieldInfoMap;
            }
            if (namePath != null) {
                if (component != null) {
                    componentMap.put(namePath, component);
                }
                if (storage != null) {
                    fieldInfo.property = storage.makeProperty(name, objectType);
                }
            }
            invokeStarter(component);
            return component;
        }
        if (hasWord(name, "window", true, true)) {
            AbstractComponent component = null;
            if (!modelOnly) {
                final List<ObjectView.FieldInfo> componentFieldInfoList
                    = new ArrayList<ObjectView.FieldInfo>();
                final Map<String, ObjectView.FieldInfo> componentFieldInfoMap
                    = new HashMap<String, ObjectView.FieldInfo>();
                final AbstractComponentContainer layout
                    = makeLayout(repository, namePath, value, null,
                                 componentFieldInfoList, componentFieldInfoMap);
                fieldInfo.fieldInfoList = componentFieldInfoList;
                fieldInfo.fieldInfoMap = componentFieldInfoMap;
                final LegacyWindow window = new LegacyWindow(null, layout) {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public String getCaption() {
                            return getString(name);
                        }

                        @Override
                        public boolean isEnabled() {
                            return checkFlag(disabledSet, namePath, object, fieldInfo.disabled, super.isEnabled());
                        }

                        @Override
                        public boolean isVisible() {
                            return checkFlag(invisibleSet, namePath, object, fieldInfo.invisible, super.isVisible());
                        }

                    };
                if (defaultLocale == null && hasWord(name, "applicationWindow", true, true)) {
                    getApplication().addWindow(window);
                }
                component = window;
                invokeStarter(layout);
            }
            if (namePath != null) {
                if (component != null) {
                    componentMap.put(namePath, component);
                }
                if (storage != null) {
                    fieldInfo.property = storage.makeProperty(name, objectType);
                }
            }
            return null;
        }
        final List<ObjectView.FieldInfo> componentFieldInfoList = new ArrayList<ObjectView.FieldInfo>();
        final Map<String, ObjectView.FieldInfo> componentFieldInfoMap = new HashMap<String, ObjectView.FieldInfo>();
        final Form newForm
            = makeForm(repository, namePath, value, fieldInfo.type, null, null,
                       componentFieldInfoList, componentFieldInfoMap, fieldInfoMap, null, inModelMethod, modelOnly,
                       !fieldInfo.isEditable);
        fieldInfo.fieldInfoList = componentFieldInfoList;
        fieldInfo.fieldInfoMap = componentFieldInfoMap;
        fieldInfo.formFieldInfoMap = formFieldInfoMap;
        return newForm;
    }

}

