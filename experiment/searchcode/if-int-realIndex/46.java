/* Listbox.java

	Purpose:

	Description:

	History:
		Wed Jun 15 17:25:00     2005, Created by tomyeh

Copyright (C) 2005 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zul;

import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Comparator;

import static org.zkoss.lang.Generics.cast;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Exceptions;
import org.zkoss.lang.Library;
import org.zkoss.lang.Objects;
import org.zkoss.lang.Strings;
import org.zkoss.io.Serializables;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.au.AuRequests;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CloneableEventListener;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.ext.render.Cropper;
import org.zkoss.zk.ui.util.ComponentCloneListener;
import org.zkoss.zul.event.DataLoadingEvent;
import org.zkoss.zul.event.ListDataEvent;
import org.zkoss.zul.event.ListDataListener;
import org.zkoss.zul.event.PageSizeEvent;
import org.zkoss.zul.event.PagingEvent;
import org.zkoss.zul.event.ZulEvents;
import org.zkoss.zul.ext.Paginal;
import org.zkoss.zul.ext.Selectable;
import org.zkoss.zul.ext.Sortable;
import org.zkoss.zul.impl.DataLoader;
import org.zkoss.zul.impl.GroupsListModel;
import org.zkoss.zul.impl.ListboxDataLoader;
import org.zkoss.zul.impl.MeshElement;
import org.zkoss.zul.impl.Padding;
import org.zkoss.zul.impl.Utils;
import org.zkoss.zul.impl.XulElement;

/**
 * A listbox.
 *
 * <p>
 * Event:
 * <ol>
 * <li>{@link org.zkoss.zk.ui.event.SelectEvent} is sent when user changes the
 * selection.</li>
 * <li>onAfterRender is sent when the model's data has been rendered.(since 5.0.4)</li>
 * </ol>
 *
 * <p>
 * See <a href="package-summary.html">Specification</a>.
 * </p>
 *
 * <p>
 * Besides creating {@link Listitem} programmingly, you could assign a data
 * model (a {@link ListModel} or {@link GroupsModel} instance) to a listbox via
 * {@link #setModel(ListModel)} or {@link #setModel(GroupsModel)} and then the
 * listbox will retrieve data via {@link ListModel#getElementAt} when necessary.
 *
 * <p>
 * Besides assign a list model, you could assign a renderer (a
 * {@link ListitemRenderer} instance) to a listbox, such that the listbox will
 * use this renderer to render the data returned by
 * {@link ListModel#getElementAt}. If not assigned, the default renderer, which
 * assumes a label per list item, is used. In other words, the default renderer
 * adds a label to a Listitem by calling toString against the object returned by
 * {@link ListModel#getElementAt}</p>
 *
 * <p>To retrieve what are selected in Listbox with a {@link Selectable}
 * {@link ListModel}, you shall use {@link Selectable#getSelection} to get what
 * is currently selected object in {@link ListModel} rather than using
 * {@link Listbox#getSelectedItems}. That is, you shall operate on the item of
 * the {@link ListModel} rather than on the {@link Listitem} of the {@link Listbox}
 * if you use the {@link Selectable} {@link ListModel}.</p>
 *
 * <pre><code>
 * Set selection = ((Selectable)getModel()).getSelection();
 * </code></pre>
 * 
 * <p>[Since 6.0.0] If a model is set, whether the listbox allows
 * the multiple selection depends on {@link Selectable#setMultiple}.
 * In other words, the application shall not access listbox directly if
 * a model is assigned. Rather, the application shall access the model
 * directly.
 * 
 * <p>
 * There are two ways to handle long content: scrolling and paging. If
 * {@link #getMold} is "default", scrolling is used if {@link #setHeight} is
 * called and too much content to display. If {@link #getMold} is "paging",
 * paging is used if two or more pages are required. To control the number of
 * items to display in a page, use {@link #setPageSize}.
 *
 * <p>
 * If paging is used, the page controller is either created automatically or
 * assigned explicitly by {@link #setPaginal}. The paging controller specified
 * explicitly by {@link #setPaginal} is called the external page controller. It
 * is useful if you want to put the paging controller at different location
 * (other than as a child component), or you want to use the same controller to
 * control multiple listboxes.
 *
 * <p>
 * Default {@link #getZclass}: z-listbox.(since 3.5.0)
 *
 * <p>
 * To have a list box without stripping, you can specify a non-existent style
 * class to {@link #setOddRowSclass}.
 *
 * <h3>Clustering and Serialization</h3>
 *
 * <p>
 * When used in a clustering environment, you have to make
 * {@link ListitemRenderer} ({@link #setItemRenderer}) and {@link ListModel} (
 * {@link #setModel}) either serializable or re-assign them when
 * {@link #sessionDidActivate} is called.
 *
 * <h3>Render on Demand (rod)</h3>
 * [ZK EE]
 * [Since 5.0.0]
 *
 * <p>For huge data, you can turn on Listbox's ROD to request ZK engine to load from
 * {@link ListModel} only the required data chunk and create only the required
 * {@link Listitem}s in memory and render only the required DOM elements in
 * browser. So it saves both the memory and the processing time in both server
 * and browser for huge data. If you don't use the {@link ListModel} with the
 * Listbox, turn on the ROD will still have ZK engine to render only a chunk of
 * DOM elements in browser so it at least saves the memory and processing time
 * in browser. Note that ROD works only if the Listbox is configured to has a
 * limited "view port" height. That is, either the Listbox is in the "paging"
 * mold or you have to {@link #setHeight(String)},{@link #setVflex(String)},
 * or {@link #setRows(int)} of the Listbox to make ROD works.</p>
 *
 * <p>You can turn on/off ROD for all Listboxes in the application or only
 * for a specific Listbox. To turn on ROD for all Listboxes in the application,
 * you have to specify the Library Property "org.zkoss.zul.listbox.rod" to
 * "true" in WEB-INF/zk.xml. If you did not specify the Library Property,
 * default is false.</p>
 *
 * <pre><code>
 *	<library-property>
 *		<name>org.zkoss.zul.listbox.rod</name>
 *		<value>true</value>
 *	</library-property>
 * </code></pre>
 *
 * <p>To turn on ROD for a specific Listbox, you have to specify the Listbox's
 * attribute map with key "org.zkoss.zul.listbox.rod" to true. That is, for
 * example, if in a zul file, you shall specify &lt;custom-attributes> of the
 * Listbox like this:</p>
 *
 * <pre><code>
 *	<listbox ...>
 *    <custom-attributes org.zkoss.zul.listbox.rod="true"/>
 *  </listbox>
 * </code></pre>
 *
 * <p>You can mix the Library Property and &lt;custom-attributes> ways together.
 * The &lt;custom-attributes> way always takes higher priority. So you
 * can turn OFF ROD in general and turn ON only some specific Listbox component.
 * Or you can turn ON ROD in general and turn OFF only some specific Listbox
 * component.</P>
 *
 * <p>Since only partial {@link Listitem}s are created and rendered in the
 * Listbox if you turn the ROD on, there will be some limitations on accessing
 * {@link Listitem}s. For example, if you call
 * <pre><code>
 * Listitem itemAt100 = (Listitem) getItemAtIndex(100);
 * </code></pre>
 * <p>The {@link Listitem} in index 100 is not necessary created yet if it is
 * not in the current "view port" and you will get "null" instead.</p>
 *
 * <p>And it is generally a bad idea to "cache" the created {@link Listitem}
 * in your application if you turn the ROD on because Listitems might be removed
 * later. Basically, you shall operate on the item of the {@link ListModel}
 * rather than on the {@link Listitem} of the {@link Listbox} if you use the
 * {@link ListModel} and ROD.</p>
 *
 * <h3>Custom Attributes</h3>
 * <dl>
 * <dt>org.zkoss.zul.listbox.rightSelect</dt>
 * <dd>Specifies whether the selection shall be toggled when user right clicks on
 * item, if the checkmark ({@link #isCheckmark}) is enabled.</br>
 * Notice that you could specify this attribute in any of its ancestor's attributes.
 * It will be inherited.</dd>
 * <dt>org.zkoss.zul.listbox.rod</dt>
 * <dd>Specifies whether to enable ROD (render-on-demand).</br>
 * Notice that you could specify this attribute in any of its ancestor's attributes.
 * It will be inherited.</dd>
 * <dt>org.zkoss.zul.listbox.autoSort</dt>.(since 5.0.7) 
 * <dd>Specifies whether to sort the model when the following cases:</br>
 * <ol>
 * <li>{@link #setModel} is called and {@link Listheader#setSortDirection} is set.</li>
 * <li>{@link Listheader#setSortDirection} is called.</li>
 * <li>Model receives {@link ListDataEvent} and {@link Listheader#setSortDirection} is set.</li>
 * </ol>
 * If you want to ignore sort when receiving {@link ListDataEvent}, 
 * you can specifies the value as "ignore.change".</br>
 * Notice that you could specify this attribute in any of its ancestor's attributes.
 * It will be inherited.</dd>
 * </dl>
 * <dt>org.zkoss.zul.listbox.groupSelect</dt>
 * <dd>Specifies whether Listgroups under this Listbox are selectable. Notice that 
 * you could specify this attribute in any of its ancestor's attributes. It will 
 * be inherited. Default value is false.</dd>
 * 
 * <dt>org.zkoss.zul.listbox.preloadSize</dt>.(since 5.0.8) 
 * <dd>Specifies the number of items to preload when receiving
 * the rendering request from the client.
 * <p>It is used only if live data ({@link #setModel(ListModel)} and
 * not paging ({@link #getPagingChild}).</dd>
 * 
 * <dt>org.zkoss.zul.listbox.initRodSize</dt>.(since 5.0.8) 
 * <dd>Specifies the number of items rendered when the Listbox first render.
 * <p>
 * It is used only if live data ({@link #setModel(ListModel)} and not paging
 * ({@link #getPagingChild}).</dd>
 * 
 * @author tomyeh
 * @see ListModel
 * @see ListitemRenderer
 * @see ListitemRendererExt
 */
public class Listbox extends MeshElement {
	private static final long serialVersionUID = 2009111111L;
	public static final String LOADING_MODEL = "org.zkoss.zul.loadingModel";
	public static final String SYNCING_MODEL = "org.zkoss.zul.syncingModel";

	private static final Log log = Log.lookup(Listbox.class);
	private static final String ATTR_ON_INIT_RENDER_POSTED = "org.zkoss.zul.onInitLaterPosted";
	private static final String ATTR_ON_PAGING_INIT_RENDERER_POSTED = "org.zkoss.zul.onPagingInitPosted";
	private static final int INIT_LIMIT = 50;

	private transient DataLoader _dataLoader;
	private transient List<Listitem> _items;
	private transient List<int[]> _groupsInfo;
	private transient List<Listgroup> _groups;
	/** A list of selected items. */
	private transient Set<Listitem> _selItems;
	/** A readonly copy of {@link #_selItems}. */
	private transient Set<Listitem> _roSelItems;
	private int _maxlength;
	private int _rows, _jsel = -1;
	private transient Listhead _listhead;
	private transient Listfoot _listfoot;
	private transient Frozen _frozen;
	private transient ListModel<?> _model;
	private transient ListitemRenderer<?> _renderer;
	private transient ListDataListener _dataListener;
	private transient Collection<Component> _heads;
	private int _hdcnt;
	private String _innerWidth = "100%";
	/** The name. */
	private String _name;
	/** The paging controller, used only if mold = "paging". */
	private transient Paginal _pgi;
	private transient boolean _isReplacingItem;
	private transient int _focusIndex = -1;

	/**
	 * The paging controller, used only if mold = "paging" and user doesn't
	 * assign a controller via {@link #setPaginal}. If exists, it is the last
	 * child
	 */
	private transient Paging _paging;
	private EventListener<PagingEvent> _pgListener;
	private EventListener<Event> _pgImpListener, _modelInitListener;
	/** The style class of the odd row. */
	private String _scOddRow = null;
	private int _tabindex;
	/** the # of rows to preload. */
	private int _preloadsz = 50;
	/** maintain the number of the visible item in Paging mold. */
	private int _visibleItemCount;
	private int _currentTop = 0; // since 5.0.0 scroll position
	private int _currentLeft = 0;
	private int _topPad; // since 5.0.0 top padding
	private String _nonselTags; //since 5.0.5 for non-selectable tags
	
	private int _anchorTop = 0 ; //since ZK 5.0.11 , 6.0.0 anchor position
	private int _anchorLeft = 0 ; 
	
	private boolean _multiple;
	private boolean _disabled, _checkmark;
	private boolean _renderAll; //since 5.0.0

	private transient boolean _rod;
	/** whether to ignore ListDataEvent.SELECTION_CHANGED */
	private transient boolean _ignoreDataSelectionEvent;
	private String _emptyMessage;
	
	static {
		addClientEvent(Listbox.class, Events.ON_RENDER, CE_DUPLICATE_IGNORE
				| CE_IMPORTANT | CE_NON_DEFERRABLE);
		addClientEvent(Listbox.class, "onInnerWidth", CE_DUPLICATE_IGNORE
				| CE_IMPORTANT);
		
		//ZK-925 We can't use CE_DUPLICATE_IGNORE in "onSelect" event since we need to sync the status when multiple select in ROD.
		addClientEvent(Listbox.class, Events.ON_SELECT, CE_IMPORTANT);
		addClientEvent(Listbox.class, Events.ON_FOCUS, CE_DUPLICATE_IGNORE);
		addClientEvent(Listbox.class, Events.ON_BLUR, CE_DUPLICATE_IGNORE);
		addClientEvent(Listbox.class, "onScrollPos", CE_DUPLICATE_IGNORE | CE_IMPORTANT); // since 5.0.0
		addClientEvent(Listbox.class, "onTopPad", CE_DUPLICATE_IGNORE); // since
		// 5.0.0
		addClientEvent(Listbox.class, "onDataLoading", CE_DUPLICATE_IGNORE
				| CE_IMPORTANT | CE_NON_DEFERRABLE); // since 5.0.0
		addClientEvent(Listbox.class, ZulEvents.ON_PAGE_SIZE, CE_DUPLICATE_IGNORE|CE_IMPORTANT|CE_NON_DEFERRABLE); //since 5.0.2
		
		// since 6.0.0, F60-ZK-715
		addClientEvent(Listbox.class, "onAcrossPage", 
				CE_DUPLICATE_IGNORE | CE_IMPORTANT | CE_NON_DEFERRABLE);
		
		// since 6.0.0/5.0.11, B50-ZK-798
		addClientEvent(Listbox.class, "onAnchorPos", CE_DUPLICATE_IGNORE | CE_IMPORTANT);
	}

	public Listbox() {
		init();
	}

	private void init() {
		_items = new AbstractSequentialList<Listitem>() {
			public ListIterator<Listitem> listIterator(int index) {
				return new ItemIter(index);
			}

			public Listitem get(int j) {
				final Component o = Listbox.this.getChildren().get(j + _hdcnt);
				if (o instanceof Listitem)
					return (Listitem)o;
				throw new IndexOutOfBoundsException("Wrong index: " + j);
			}

			public int size() {
				int sz = getChildren().size() - _hdcnt;
				if (_listfoot != null)
					--sz;
				if (_paging != null)
					--sz;
				if (_frozen != null)
					--sz;
				return sz;
			}

			/**
			 * override for Listgroup
			 *
			 * @since 3.5.1
			 */
			protected void removeRange(int fromIndex, int toIndex) {
				ListIterator it = listIterator(toIndex);
				for (int n = toIndex - fromIndex; --n >= 0 && it.hasPrevious();) {
					it.previous();
					it.remove();
				}
			}
			
			/**
			 * Override to remove unnecessary Listitem re-indexing (when ROD is on, clear() is called frequently). 
			 */
			public void clear() {
				final boolean oldFlag = setReplacingItem(true);
				try {
					super.clear();
				} finally {
					setReplacingItem(oldFlag);
				}
			}
		};
		_selItems = new LinkedHashSet<Listitem>(4);
		_roSelItems = Collections.unmodifiableSet(_selItems);

		_heads = new AbstractCollection<Component>() {
			public int size() {
				return _hdcnt;
			}

			public Iterator<Component> iterator() {
				return new Iter();
			}
		};
		_groupsInfo = new LinkedList<int[]>();
		_groups = new AbstractList<Listgroup>() {
			public int size() {
				return getGroupCount();
			}

			public Iterator<Listgroup> iterator() {
				return new IterGroups();
			}

			public Listgroup get(int index) {
				return (Listgroup)getItemAtIndex(_groupsInfo.get(index)[0]);
			}
		};
	}

	private int getRealIndex(int index) {
		final int offset = _model != null ? getDataLoader().getOffset() : 0;
		return index - (offset < 0 ? 0 : offset);
	}

	public List<Component> getChildren() {
		return new Children();
	}

	protected class Children extends AbstractComponent.Children {
		protected void removeRange(int fromIndex, int toIndex) {
			ListIterator<Component> it = listIterator(toIndex);
			for (int n = toIndex - fromIndex; --n >= 0 && it.hasPrevious();) {
				it.previous();
				it.remove();
			}
		}
	};

	/**
	 * Initializes _dataListener and register the listener to the model
	 */
	private void initDataListener() {
		if (_dataListener == null)
			_dataListener = new ListDataListener() {
				public void onChange(ListDataEvent event) {
					onListDataChange(event);
				}
			};

		_model.addListDataListener(_dataListener);
	}

	/**
	 * @deprecated since 5.0.0, use {@link #setSizedByContent}(!fixedLayout)
	 *             instead
	 * @param fixedLayout
	 *            true to outline this listbox by browser
	 */
	public void setFixedLayout(boolean fixedLayout) {
		setSizedByContent(!fixedLayout);
	}

	/**
	 * @deprecated since 5.0.0, use !{@link #isSizedByContent} instead
	 */
	public boolean isFixedLayout() {
		return !isSizedByContent();
	}

	/**
	 * Returns {@link Listhead} belonging to this listbox, or null if no list
	 * headers at all.
	 */
	public Listhead getListhead() {
		return _listhead;
	}

	/**
	 * Returns {@link Listfoot} belonging to this listbox, or null if no list
	 * footers at all.
	 */
	public Listfoot getListfoot() {
		return _listfoot;
	}

	/**
	 * Returns the frozen child.
	 *
	 * @since 5.0.0
	 */
	public Frozen getFrozen() {
		return _frozen;
	}

	/**
	 * Returns a collection of heads, including {@link #getListhead} and
	 * auxiliary heads ({@link Auxhead}) (never null).
	 *
	 * @since 3.0.0
	 */
	public Collection<Component> getHeads() {
		return _heads;
	}

	/**
	 * Returns whether the HTML's select tag is used.
	 */
	/* package */ boolean inSelectMold() {
		return "select".equals(getMold());
	}

	/**
	 * Returns whether the check mark shall be displayed in front of each item.
	 * <p>
	 * Default: false.
	 */
	public boolean isCheckmark() {
		return _checkmark;
	}

	/**
	 * Sets whether the check mark shall be displayed in front of each item.
	 * <p>
	 * The check mark is a checkbox if {@link #isMultiple} returns true. It is a
	 * radio button if {@link #isMultiple} returns false.
	 */
	public void setCheckmark(boolean checkmark) {
		if (_checkmark != checkmark) {
			_checkmark = checkmark;
			smartUpdate("checkmark", checkmark);
		}
	}

	/**
	 * Sets the inner width of this component. The inner width is the width of
	 * the inner table. By default, it is 100%. That is, it is the same as the
	 * width of this component. However, it is changed when the user is sizing
	 * the column's width.
	 *
	 * <p>
	 * Application developers rarely call this method, unless they want to
	 * preserve the widths of sizable columns changed by the user. To preserve
	 * the widths, the developer have to store the widths of all columns and the
	 * inner width ({@link #getInnerWidth}), and then restore them when
	 * re-creating this component.
	 *
	 * @param innerWidth
	 *            the inner width. If null, "100%" is assumed.
	 * @since 3.0.0
	 */
	public void setInnerWidth(String innerWidth) {
		if (innerWidth == null)
			innerWidth = "100%";
		if (!_innerWidth.equals(innerWidth)) {
			_innerWidth = innerWidth;
			smartUpdate("innerWidth", innerWidth);
		}
	}

	/**
	 * Returns the inner width of this component. The inner width is the width
	 * of the inner table.
	 * <p>
	 * Default: "100%"
	 *
	 * @see #setInnerWidth
	 * @since 3.0.0
	 */
	public String getInnerWidth() {
		return _innerWidth;
	}

	/**
	 * Returns whether to grow and shrink vertical to fit their given space, so
	 * called vertial flexibility.
	 *
	 * <p>
	 * Note: this attribute is ignored if {@link #setRows} is specified
	 *
	 * <p>
	 * Default: false.
	 */
	public boolean isVflex() {
		final String vflex = getVflex();
		if ("true".equals(vflex) || "min".equals(vflex)) {
			return true;
		}
		if (Strings.isBlank(vflex) || "false".equals(vflex)) {
			return false;
		}
		return Integer.parseInt(vflex) > 0;
	}

	/**
	 * Sets whether to grow and shrink vertical to fit their given space, so
	 * called vertial flexibility.
	 *
	 * <p>
	 * Note: this attribute is ignored if {@link #setRows} is specified
	 */
	public void setVflex(boolean vflex) {
		if (isVflex() != vflex) {
			setVflex(String.valueOf(vflex));
		}
	}

	/**
	 * Returns whether it is disabled.
	 * <p>
	 * Default: false.
	 */
	public boolean isDisabled() {
		return _disabled;
	}

	/**
	 * Sets whether it is disabled.
	 * <p>Note that it is only applied when mold is "select".
	 */
	public void setDisabled(boolean disabled) {
		if (_disabled != disabled) {
			_disabled = disabled;
			smartUpdate("disabled", _disabled);
		}
	}

	/**
	 * Returns the tab order of this component.
	 * <p>
	 * Currently, only the "select" mold supports this property.
	 * <p>
	 * Default: 0 (means the same as browser's default).
	 */
	public int getTabindex() {
		return _tabindex;
	}

	/**
	 * Sets the tab order of this component.
	 * <p>
	 * Currently, only the "select" mold supports this property.
	 */
	public void setTabindex(int tabindex) throws WrongValueException {
		if (_tabindex != tabindex) {
			_tabindex = tabindex;
			smartUpdate("tabindex", tabindex);
		}
	}

	/**
	 * Returns the rows. Zero means no limitation.
	 * <p>
	 * Default: 0.
	 */
	public int getRows() {
		return _rows;
	}

	/**
	 * Sets the rows.
	 * <p>
	 * Note: if both {@link #setHeight} is specified with non-empty,
	 * {@link #setRows} is ignored
	 */
	public void setRows(int rows) throws WrongValueException {
		if (rows < 0)
			throw new WrongValueException("Illegal rows: " + rows);

		if (_rows != rows) {
			_rows = rows;
			smartUpdate("rows", _rows);
		}
	}

	/**
	 * Returns the seltype.
	 * <p>
	 * Default: "single".
	 */
	public String getSeltype() {
		return _multiple ? "multiple" : "single";
	}

	/**
	 * Sets the seltype.
	 * Allowed values:single,multiple
	 * 
	 */
	public void setSeltype(String seltype) throws WrongValueException {
		if ("single".equals(seltype))
			setMultiple(false);
		else if ("multiple".equals(seltype))
			setMultiple(true);
		else
			throw new WrongValueException("Unknown seltype: " + seltype);
	}

	/**
	 * Returns whether multiple selections are allowed.
	 * <p>
	 * Default: false.
	 */
	public boolean isMultiple() {
		return _multiple;
	}

	/**
	 * Sets whether multiple selections are allowed.
	 * <p>Notice that, if a model is assigned, it will change the model's
	 * state (by {@link Selectable#setMultiple}).
	 */
	public void setMultiple(boolean multiple) {
		if (_multiple != multiple) {
			_multiple = multiple;
			if (!_multiple && _selItems.size() > 1) {
				final Listitem item = getSelectedItem();
				for (Iterator<Listitem> it = _selItems.iterator(); it.hasNext();) {
					final Listitem li = it.next();
					if (li != item) {
						li.setSelectedDirectly(false);
						it.remove();
					}
				}
				// No need to update selId because multiple will do the job at
				// client
			}
			if (_model != null)
				((Selectable)_model).setMultiple(multiple);
			smartUpdate("multiple", _multiple);
		}
	}

	/**
	 * Returns the maximal length of each item's label.
	 * <p>
	 * It is meaningful only for the select mold.
	 */
	public int getMaxlength() {
		return _maxlength;
	}

	/**
	 * Sets the maximal length of each item's label.
	 * <p>
	 * It is meaningful only for the select mold.
	 */
	public void setMaxlength(int maxlength) {
		if (maxlength < 0)
			maxlength = 0;
		if (_maxlength != maxlength) {
			_maxlength = maxlength;
			smartUpdate("maxlength", maxlength);
		}
	}

	/**
	 * Returns the name of this component.
	 * <p>
	 * Default: null.
	 * <p>
	 * The name is used only to work with "legacy" Web application that handles
	 * user's request by servlets. It works only with HTTP/HTML-based browsers.
	 * It doesn't work with other kind of clients.
	 * <p>
	 * Don't use this method if your application is purely based on ZK's
	 * event-driven model.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of this component.
	 * <p>
	 * The name is used only to work with "legacy" Web application that handles
	 * user's request by servlets. It works only with HTTP/HTML-based browsers.
	 * It doesn't work with other kind of clients.
	 * <p>
	 * Don't use this method if your application is purely based on ZK's
	 * event-driven model.
	 *
	 * @param name
	 *            the name of this component.
	 */
	public void setName(String name) {
		if (name != null && name.length() == 0)
			name = null;
		if (!Objects.equals(_name, name)) {
			_name = name;
			smartUpdate("name", name);
		}
	}

	/** Sets a list of HTML tag names that shall <i>not</i> cause the list item
	 * being selected if they are clicked.
	 * <p>Default: null (it means button, input, textarea and a). If you want
	 * to select no matter which tag is clicked, please specify an empty string.
	 * @param tags a list of HTML tag names that will <i>not</i> cause the list item
	 * being selected if clicked. Specify null to use the default and "" to
	 * indicate none.
	 * @since 5.0.5
	 */
	public void setNonselectableTags(String tags) {
		if (!Objects.equals(_nonselTags, tags)) {
			_nonselTags = tags;
			smartUpdate("nonselectableTags", tags);
		}
	}
	/** Returns a list of HTML tag names that shall <i>not</i> cause the list item
	 * being selected if they are clicked.
	 * <p>Refer to {@link #setNonselectableTags} for details.
	 * @since 5.0.5
	 */
	public String getNonselectableTags() {
		return _nonselTags;
	}

	/**
	 * Returns a live list of all {@link Listitem}. By live we mean you can add
	 * or remove them directly with the List interface. In other words, you
	 * could add or remove an item by manipulating the returned list directly.
	 */
	public List<Listitem> getItems() {
		return _items;
	}

	/**
	 * Returns the number of items.
	 */
	public int getItemCount() {
		return _items.size();
	}

	/**
	 * Returns the item at the specified index.
	 *
	 * <p>
	 * Note: if live data is used ({@link #getModel} is not null), the returned
	 * item might NOT be loaded yet. To ensure it is loaded, you have to invoke
	 * {@link #renderItem}.
	 */
	public Listitem getItemAtIndex(int index) {
		final int realindex = getRealIndex(index);
		return realindex < 0 || realindex >= _items.size() ? null: _items.get(realindex);
	}

	/**
	 * Returns the index of the specified item, or -1 if not found.
	 */
	public int getIndexOfItem(Listitem item) {
		return item == null ? -1 : item.getIndex();
	}

	/**
	 * Returns the index of the selected item (-1 if no one is selected).
	 */
	public int getSelectedIndex() {
		return _jsel;
	}

	/* package */boolean isLoadingModel() {
		return getAttribute(LOADING_MODEL) != null;
	}

	/**
	 * Deselects all of the currently selected items and selects the item with
	 * the given index.
	 */
	public void setSelectedIndex(int jsel) {
		final int isz = _items.size();
		final int tsz = _model != null ? _model.getSize() : isz;
		if (jsel >= tsz)
			throw new UiException("Out of bound: " + jsel + " while size="
					+ tsz);

		if (jsel < -1)
			jsel = -1;
		if (jsel < 0) { // unselect all
			clearSelection();
		} else if (jsel != _jsel || (_multiple && _selItems.size() > 1) || !_selItems.contains(getItemAtIndex(_jsel))) {
			for (Listitem item: _selItems) {
				item.setSelectedDirectly(false);
			}
			_selItems.clear();
			_jsel = jsel;
			Listitem item = getItemAtIndex(_jsel);

			if (item == null) { // to be selected item is not there
				if (inPagingMold()) {
					final int offset = _jsel - _jsel % getPageSize();
					final int limit = getPageSize();
					getDataLoader().syncModel(offset, limit); // force reloading
				} else {
					smartUpdate("selInView_", _jsel);
				}
			} else {
				item.setSelectedDirectly(true);
				_selItems.add(item);
			}

			if (inSelectMold()) {
				smartUpdate("selectedIndex", _jsel);
			} else if (item != null)
				smartUpdate("selectedItem", item);
			// Bug 1734950: don't count on index (since it may change)
			// On the other hand, it is OK with select-mold since
			// it invalidates if items are added or removed
		}

		if (_jsel >= 0 && inPagingMold()) {
			final Listitem item = getItemAtIndex(_jsel);
			int size = getDataLoader().getOffset();
			for (Iterator it = new VisibleChildrenIterator(true); it.hasNext(); size++)
				if (item.equals(it.next()))
					break;

			final int pg = size / getPageSize();
			if (pg != getActivePage())
				setActivePage(pg);
		}
	}

	/**
	 * Deselects all of the currently selected items and selects the given item.
	 * <p>
	 * It is the same as {@link #setSelectedItem}.
	 *
	 * @param item
	 *            the item to select. If null, all items are deselected.
	 */
	public void selectItem(Listitem item) {
		if (item == null) {
			setSelectedIndex(-1);
		} else {
			if (item.getParent() != this)
				throw new UiException("Not a child: " + item);
			if (_multiple || !item.isSelected())
				setSelectedIndex(item.getIndex());
		}
	}

	/**
	 * Selects the given item, without deselecting any other items that are
	 * already selected..
	 *
	 * <p>Notice that if you assign a model to a listbox ({@link #setModel}),
	 * you shall not invoke this method directly. Rather, use {@link Selectable}
	 * instead.
	 */
	public void addItemToSelection(Listitem item) {
		if (item.getParent() != this)
			throw new UiException("Not a child: " + item);

		if (!item.isSelected()) {
			if (!_multiple) {
				selectItem(item);
			} else {
				if (item.getIndex() < _jsel || _jsel < 0) {
					_jsel = item.getIndex();
					// ZK-866
					// update the change of selected index
					if (inSelectMold()) {
						smartUpdate("selectedIndex", _jsel);
					} else if (item != null)
						smartUpdate("selectedItem", item);
				}
				item.setSelectedDirectly(true);
				_selItems.add(item);
				if (inSelectMold()) {
					item.smartUpdate("selected", true);
				} else {
					smartUpdateSelection();
				}
			}
		}
	}

	/**
	 * Deselects the given item without deselecting other items.
	 *
	 * <p>Notice that if you assign a model to a listbox ({@link #setModel}),
	 * you shall not invoke this method directly. Rather, use {@link Selectable}
	 * instead.
	 */
	public void removeItemFromSelection(Listitem item) {
		if (item.getParent() != this)
			throw new UiException("Not a child: " + item);

		if (item.isSelected()) {
			if (!_multiple) {
				clearSelection();
			} else {
				item.setSelectedDirectly(false);
				_selItems.remove(item);
				fixSelectedIndex(0);
				if (inSelectMold()) {
					item.smartUpdate("selected", false);
				} else {
					smartUpdateSelection();
				}
			}
		}
	}

	/**
	 * Note: we have to update all selection at once, since addItemToSelection
	 * and removeItemFromSelection might be called interchangeably.
	 */
	private void smartUpdateSelection() {
		final StringBuffer sb = new StringBuffer(80);
		for (Listitem item: _selItems) {
			if (sb.length() > 0)
				sb.append(',');
			sb.append(item.getUuid());
		}
		smartUpdate("chgSel", sb.toString());
	}

	/**
	 * If the specified item is selected, it is deselected. If it is not
	 * selected, it is selected. Other items in the list box that are selected
	 * are not affected, and retain their selected state.
	 */
	public void toggleItemSelection(Listitem item) {
		if (item.isSelected())
			removeItemFromSelection(item);
		else
			addItemToSelection(item);
	}

	/**
	 * Clears the selection.
	 */
	public void clearSelection() {
		if (!_selItems.isEmpty()) {
			for (Listitem item: _selItems) {
				item.setSelectedDirectly(false);
			}
			_selItems.clear();
			_jsel = -1;
			if (inSelectMold())
				smartUpdate("selectedIndex", -1);
			else
				smartUpdate("selectedItem", null);
			// Bug 1734950: don't count on index (since it may change)
		}
	}

	/**
	 * Selects all items.
	 */
	public void selectAll() {
		if (!_multiple)
			throw new UiException("Appliable only to the multiple seltype: "
					+ this);

		if (_items.size() != _selItems.size()) {
			for (Listitem item: _items) {
				_selItems.add(item);
				item.setSelectedDirectly(true);
			}
			_jsel = _items.isEmpty() ? -1 : 0;
			smartUpdate("selectAll", true);
		}
	}

	/**
	 * Returns the selected item.
	 *
	 * <p>
	 * Note: if live data is used ({@link #getModel} is not null), the returned
	 * item might NOT be loaded yet. To ensure it is loaded, you have to invoke
	 * {@link #renderItem}.
	 */
	public Listitem getSelectedItem() {
		return _jsel >= 0 ? _jsel > 0 && _selItems.size() == 1 ? // optimize for performance
			_selItems.iterator().next(): getItemAtIndex(_jsel) : null;
	}

	/**
	 * Deselects all of the currently selected items and selects the given item.
	 * <p>
	 * It is the same as {@link #selectItem}.
	 */
	public void setSelectedItem(Listitem item) {
		selectItem(item);
	}

	/**
	 * Selects the given listitems.
	 *
	 * @since 3.6.0
	 */
	public void setSelectedItems(Set listItems) {
		if (!isMultiple())
			throw new WrongValueException(
					"Listbox must allow multiple selections.");
		for (Iterator it = listItems.iterator(); it.hasNext();) {
			addItemToSelection((Listitem) it.next());
		}
	}

	/**
	 * Returns all selected items.
	 *
	 * <p>
	 * Note: if live data is used ({@link #getModel} is not null), the returned
	 * item might NOT be loaded yet. To ensure it is loaded, you have to invoke
	 * {@link #renderItem}.
	 */
	public Set<Listitem> getSelectedItems() {
		return _roSelItems;
	}

	/**
	 * Returns the number of items being selected.
	 */
	public int getSelectedCount() {
		return _selItems.size();
	}

	/**
	 * Appends an item.
	 *
	 * <p>
	 * Note: if live data is used ({@link #getModel} is not null), the returned
	 * item might NOT be loaded yet. To ensure it is loaded, you have to invoke
	 * {@link #renderItem}.
	 */
	public Listitem appendItem(String label, String value) {
		final Listitem item = new Listitem(label, value);
		item.applyProperties();
		item.setParent(this);
		return item;
	}

	/**
	 * Removes the child item in the list box at the given index.
	 *
	 * <p>
	 * Note: if live data is used ({@link #getModel} is not null), the returned
	 * item might NOT be loaded yet. To ensure it is loaded, you have to invoke
	 * {@link #renderItem}.
	 *
	 * @return the removed item.
	 */
	public Listitem removeItemAt(int index) {
		final Listitem item = getItemAtIndex(index);
		removeChild(item);
		return item;
	}

	// --Paging--//
	/**
	 * Returns the paging controller, or null if not available. Note: the paging
	 * controller is used only if {@link #getMold} is "paging".
	 *
	 * <p>
	 * If mold is "paging", this method never returns null, because a child
	 * paging controller is created automcatically (if not specified by
	 * developers with {@link #setPaginal}).
	 *
	 * <p>
	 * If a paging controller is specified (either by {@link #setPaginal}, or by
	 * {@link #setMold} with "paging"), the listbox will rely on the paging
	 * controller to handle long-content instead of scrolling.
	 */
	public Paginal getPaginal() {
		return _pgi;
	}

	/*
	 * Specifies the paging controller. Note: the paging controller is used only
	 * if {@link #getMold} is "paging".
	 *
	 * <p>It is OK, though without any effect, to specify a paging controller
	 * even if mold is not "paging".
	 *
	 * @param pgi the paging controller. If null and {@link #getMold} is
	 * "paging", a paging controller is created automatically as a child
	 * component (see {@link #getPagingChild}).
	 */
	public void setPaginal(Paginal pgi) {
		if (!Objects.equals(pgi, _pgi)) {
			final Paginal old = _pgi;
			_pgi = pgi; // assign before detach paging, since removeChild
			// assumes it

			if (inPagingMold()) {
				if (old != null)
					removePagingListener(old);
				if (_pgi == null) {
					if (_paging != null)
						_pgi = _paging;
					else
						newInternalPaging();
				} else { // _pgi != null
					if (_pgi != _paging) {
						if (_paging != null)
							_paging.detach();
						_pgi.setTotalSize(getDataLoader().getTotalSize());
						addPagingListener(_pgi);
						if (_pgi instanceof Component)
							smartUpdate("paginal", _pgi);
					}
				}
			}
		}
	}

	/**
	 * Creates the internal paging component.
	 */
	private void newInternalPaging() {
//		assert inPagingMold() : "paging mold only";
//		assert (_paging == null && _pgi == null);

		final Paging paging = new Paging();
		paging.setAutohide(true);
		paging.setDetailed(true);
		paging.applyProperties();
		paging.setTotalSize(getDataLoader().getTotalSize());
		paging.setParent(this);
		if (_pgi != null)
			addPagingListener(_pgi);
	}

	private class PGListener implements SerializableEventListener<PagingEvent>,
			CloneableEventListener<PagingEvent> {
		public void onEvent(PagingEvent event) {
			Events.postEvent(new PagingEvent(event.getName(),
				Listbox.this, event.getPageable(), event.getActivePage()));
		}

		@Override
		public Object willClone(Component comp) {
			return null; // skip to clone
		}
	}
	private class PGImpListener implements SerializableEventListener<Event>,
			CloneableEventListener<Event> {
		public void onEvent(Event event) {
			if (_model != null && inPagingMold()) {
				final Paginal pgi = getPaginal();
				int pgsz = pgi.getPageSize();
				final int ofs = pgi.getActivePage() * pgsz;
				if (_rod) {
					getDataLoader().syncModel(ofs, pgsz);
				}
				postOnPagingInitRender();
			}
			invalidate();
		}

		@Override
		public Object willClone(Component comp) {
			return null; // skip to clone
		}
	}

	/** Adds the event listener for the onPaging event. */
	private void addPagingListener(Paginal pgi) {
		if (_pgListener == null)
			_pgListener = new PGListener();
		pgi.addEventListener(ZulEvents.ON_PAGING, _pgListener);

		if (_pgImpListener == null)
			_pgImpListener = new PGImpListener();
		
		pgi.addEventListener("onPagingImpl", _pgImpListener);
	}

	/** Removes the event listener for the onPaging event. */
	private void removePagingListener(Paginal pgi) {
		pgi.removeEventListener(ZulEvents.ON_PAGING, _pgListener);
		pgi.removeEventListener("onPagingImpl", _pgImpListener);
	}

	/**
	 * Returns the child paging controller that is created automatically, or
	 * null if mold is not "paging", or the controller is specified externally
	 * by {@link #setPaginal}.
	 *
	 * @since 3.0.7
	 */
	public Paging getPagingChild() {
		return _paging;
	}

	protected Paginal pgi() {
		if (_pgi == null)
			throw new IllegalStateException("Available only the paging mold");
		return _pgi;
	}

	/**
	 * Sets the active page in which the specified item is. The active page will
	 * become the page that contains the specified item.
	 *
	 * @param item
	 *            the item to show. If the item is null or doesn't belong to
	 *            this listbox, nothing happens.
	 * @since 3.0.4
	 * @see #setActivePage(int)
	 */
	public void setActivePage(Listitem item) {
		if (item != null && item.getParent() == this) {
			final int pg = item.getIndex() / getPageSize();
			if (pg != getActivePage())
				setActivePage(pg);
		}
	}

	/**
	 * Returns whether this listbox is in the paging mold.
	 */
	/* package */boolean inPagingMold() {
		return "paging".equals(getMold());
	}

	/**
	 * Returns the number of visible descendant {@link Listitem}.
	 *
	 * @since 3.5.1
	 */
	public int getVisibleItemCount() {
		return _visibleItemCount;
	}

	/* package */void addVisibleItemCount(int count) {
		if (count != 0) {
			_visibleItemCount += count;
			if (inPagingMold()) {
				final Paginal pgi = getPaginal();
				pgi.setTotalSize(getDataLoader().getTotalSize());
				invalidate(); // the set of visible items might change
			} else if (((Cropper) getDataLoader()).isCropper()) {
				getDataLoader().updateModelInfo();
			} else {
				smartUpdate("visibleItemCount", _visibleItemCount);
			}
		}
	}

	/**
	 * Returns the style class for the odd rows.
	 * <p>
	 * Default: {@link #getZclass()}-odd. (since 3.5.0)
	 *
	 * @since 3.0.0
	 */
	public String getOddRowSclass() {
		return _scOddRow == null ? getZclass() + "-odd" : _scOddRow;
	}

	/**
	 * Sets the style class for the odd rows. If the style class doesn't exist,
	 * the striping effect disappears. You can provide different effects by
	 * providing the proper style classes.
	 *
	 * @since 3.0.0
	 */
	public void setOddRowSclass(String scls) {
		if (scls != null && scls.length() == 0)
			scls = null;
		if (!Objects.equals(_scOddRow, scls)) {
			_scOddRow = scls;
			smartUpdate("oddRowSclass", scls);
		}
	}

	/**
	 * Returns the number of listgroup
	 *
	 * @since 3.5.0
	 */
	public int getGroupCount() {
		return _groupsInfo.size();
	}

	/**
	 * Returns a list of all {@link Listgroup}.
	 *
	 * @since 3.5.0
	 */
	public List<Listgroup> getGroups() {
		return _groups;
	}

	/**
	 * Returns whether listgroup exists.
	 *
	 * @since 3.5.0
	 */
	public boolean hasGroup() {
		return !_groupsInfo.isEmpty();
	}

	/** Sets true to avoid unnecessary Listitem re-indexing when render template.
	 * @param b true to skip
	 * @return original true/false status
	 * @see Renderer#render
	 */
	/* package */boolean setReplacingItem(boolean b) {
		final boolean old = _isReplacingItem;
		if (_model != null) // B60-ZK-898: only apply when model is used.
			_isReplacingItem = b;
		return old;
	}
	
	/* package */void fixItemIndices(int j, int to, boolean infront) {
		int realj = getRealIndex(j);
		if (realj < 0) {
			realj = 0;
		}
		if (realj < _items.size()) {
			final int beginning = j;
			for (Iterator<Listitem> it = _items.listIterator(realj); it.hasNext()
					&& (to < 0 || j <= to); ++j) {
				Listitem o = it.next();
				o.setIndexDirectly(j);

				if (_isReplacingItem) //@see Renderer#render
					break; //set only the first Listitem, skip handling GroupInfo
				
				// if beginning is a group, we don't need to change its groupInfo,
				// because
				// it is not reliable when infront is true.
				if ((!infront || beginning != j) && o instanceof Listgroup) {
					int[] g = getLastGroupsInfoAt(j + (infront ? -1 : 1));
					if (g != null) {
						g[0] = j;
						if (g[2] != -1)
							g[2] += (infront ? 1 : -1);
					}
				}
			}
		}
	}

	/* package */Listgroup getListgroupAt(int index) {
		if (_groupsInfo.isEmpty())
			return null;
		final int[] g = getGroupsInfoAt(index);
		if (g != null) {
			return (Listgroup) getItemAtIndex(g[0]);
		}
		return null;
	}

	/**
	 * Returns the group index which matches with the ListModel index.
	 *
	 * @param index
	 *            the list item index
	 * @return the associated group index of the list item index.
	 */
	/* package */int getGroupIndex(int index) {
		int j = 0, gindex = -1;
		int[] g = null;
		for (Iterator<int[]> it = _groupsInfo.iterator(); it.hasNext(); ++j) {
			g = it.next();
			if (index == g[0])
				gindex = j;
			else if (index < g[0])
				break;
		}
		return gindex != -1 ? gindex :
			g != null && index < (g[0]+g[1]) ? (j-1) :
			g != null && index == (g[0]+g[1]) && g[2] == -1 ? (j-1) : gindex;
	}

	/* package */int[] getGroupsInfoAt(int index) {
		return getGroupsInfoAt(index, false);
	}

	/**
	 * Returns the last groups info which matches with the same index. Because
	 * dynamically maintain the index of the groups will occur the same index at
	 * the same time in the loop.
	 */
	/* package */int[] getLastGroupsInfoAt(int index) {
		int[] rg = null;
		for (int[] g: _groupsInfo) {
			if (index == g[0])
				rg = g;
			else if (index < g[0])
				break;
		}
		return rg;
	}

	/**
	 * Returns an int array that it has two length, one is an index of
	 * listgroup, and the other is the number of items of listgroup(inclusive).
	 */
	/* package */int[] getGroupsInfoAt(int index, boolean isListgroup) {
		for (int[] g: _groupsInfo) {
			if (isListgroup) {
				if (index == g[0])
					return g;
			} else if ((index > g[0] && index <= g[0] + g[1]))
				return g;
		}
		return null;
	}

	public void beforeChildAdded(Component newChild, Component refChild) {
		if (newChild instanceof Listitem) {
			if (newChild instanceof Listgroup && inSelectMold())
				throw new UnsupportedOperationException(
						"Unsupported Listgroup in Select mold!");
			if (newChild instanceof Listgroupfoot) {
				if (!hasGroup())
					throw new UiException(
							"Listgroupfoot cannot exist alone, you have to add a Listgroup first");
				if (refChild == null) {
					if (getLastChild() instanceof Listgroupfoot)
						throw new UiException(
								"Only one Listgroupfoot is allowed per Listgroup");
				}
			}
		} else if (newChild instanceof Listhead) {
			if (_listhead != null && _listhead != newChild)
				throw new UiException("Only one listhead is allowed: " + this);
		} else if (newChild instanceof Frozen) {
			if (_frozen != null && _frozen != newChild)
				throw new UiException("Only one frozen child is allowed: "
						+ this);
			if (inSelectMold())
				log.warning("Mold select ignores frozen");
		} else if (newChild instanceof Listfoot) {
			if (_listfoot != null && _listfoot != newChild)
				throw new UiException("Only one listfoot is allowed: " + this);
			if (inSelectMold())
				log.warning("Mold select ignores listfoot");
		} else if (newChild instanceof Paging) {
			if (_paging != null && _paging != newChild)
				throw new UiException("Only one paging is allowed: " + this);
			if (_pgi != null)
				throw new UiException(
						"External paging cannot coexist with child paging");
			if (!inPagingMold())
				throw new UiException(
						"The child paging is allowed only in the paging mold");
		} else if (!(newChild instanceof Auxhead)) {
			throw new UiException("Unsupported child for Listbox: " + newChild);
		}
		super.beforeChildAdded(newChild, refChild);
	}

	private boolean hasGroupsModel() {
		return _model instanceof GroupsListModel;
	}

	public boolean insertBefore(Component newChild, Component refChild) {
		if (newChild instanceof Listitem) {
			final boolean isReorder = newChild.getParent() == this;
			//bug #3051305: Active Page not update when drag & drop item to the end
			if (isReorder) {
				checkInvalidateForMoved((Listitem)newChild, true);
			}
			fixGroupsInfoBeforeInsert(newChild, refChild, isReorder);
			// first: listhead or auxhead
			// last two: listfoot and paging
			if (refChild != null && refChild.getParent() != this)
				refChild = null; // Bug 1649625: it becomes the last child
			if (refChild != null
					&& (refChild == _listhead || refChild instanceof Auxhead))
				refChild = getChildren().size() > _hdcnt ? getChildren()
						.get(_hdcnt)
						: null;

			refChild = fixRefChildBeforeFoot(refChild);
			final Listitem newItem = (Listitem) newChild;
			final int jfrom = newItem.getParent() == this ? newItem.getIndex()
					: -1;

			if (super.insertBefore(newChild, refChild)) {
				// Maintain _items
				final int jto = refChild instanceof Listitem ? ((Listitem) refChild)
						.getIndex()
						: -1, fixFrom = jfrom < 0 || (jto >= 0 && jfrom > jto) ? jto
						: jfrom;
				// jfrom < 0: use jto
				// jto < 0: use jfrom
				// otherwise: use min(jfrom, jto)
				if (fixFrom < 0)
					newItem.setIndexDirectly(_items.size() - 1
							+ getDataLoader().getOffset());
				else
					fixItemIndices(fixFrom,
							jfrom >= 0 && jto >= 0 ? jfrom > jto ? jfrom : jto
									: -1, !isReorder);

				// Maintain selected
				final int newIndex = newItem.getIndex();
				if (newItem.isSelected()) {
					if (_jsel < 0) {
						_jsel = newIndex;
						_selItems.add(newItem);
					} else if (_multiple) {
						if (_jsel > newIndex) {
							_jsel = newIndex;
						}
						_selItems.add(newItem);
					} else { // deselect
						newItem.setSelectedDirectly(false);
					}
				} else {
					if (jfrom < 0) { // no existent child
						if (!isLoadingModel() && _jsel >= newIndex)
							++_jsel;
					} else if (_jsel >= 0) { // any selected
						if (jfrom > _jsel) { // from below
							if (jto >= 0 && jto <= _jsel)
								++_jsel;
						} else { // from above
							if (jto < 0 || jto > _jsel)
								--_jsel;
						}
					}
				}

				fixGroupsInfoAfterInsert(newItem);
				
				//bug #3049167: Totalsize increase when drag & drop in paging Listbox/Grid
				if (!isReorder) { //if reorder, not an insert
					afterInsert(newChild);
				}

				return true;
			} // insert
		} else if (newChild instanceof Listhead) {
			final boolean added = _listhead == null;
			refChild = fixRefChildForHeader(refChild);
			if (super.insertBefore(newChild, refChild)) {
				_listhead = (Listhead) newChild;
				if (added)
					++_hdcnt; // it may be moved, not inserted
				return true;
			}
		} else if (newChild instanceof Auxhead) {
			final boolean added = newChild.getParent() != this;
			refChild = fixRefChildForHeader(refChild);
			if (super.insertBefore(newChild, refChild)) {
				if (added)
					++_hdcnt; // it may be moved, not inserted
				return true;
			}
		} else if (newChild instanceof Frozen) {
			refChild = _paging; // the last two: listfoot and paging
			if (super.insertBefore(newChild, refChild)) {
				_frozen = (Frozen) newChild;
				return true;
			}
		} else if (newChild instanceof Listfoot) {
			// the last two: listfoot and paging
			if (_frozen != null)
				refChild = _frozen;
			else
				refChild = _paging;
			if (super.insertBefore(newChild, refChild)) {
				_listfoot = (Listfoot) newChild;
				return true;
			}
		} else if (newChild instanceof Paging) {
			refChild = null; // the last: paging
			if (super.insertBefore(newChild, refChild)) {
				_pgi = _paging = (Paging) newChild;
				return true;
			}
		} else {
			return super.insertBefore(newChild, refChild);
			// impossible but to make it more extensible
		}
		return false;
	}

	private Component fixRefChildForHeader(Component refChild) {
		if (refChild != null && refChild.getParent() != this)
			refChild = null;

		// try the first listitem
		if (refChild == null
				|| (refChild != _listhead && !(refChild instanceof Auxhead)))
			refChild = getChildren().size() > _hdcnt ? getChildren()
					.get(_hdcnt)
					: null;

		// try listfoot or paging if no listem
		refChild = fixRefChildBeforeFoot(refChild);
		return refChild;
	}

	private Component fixRefChildBeforeFoot(Component refChild) {
		if (refChild == null) {
			if (_listfoot != null)
				refChild = _listfoot;
			else if (_frozen != null)
				refChild = _frozen;
			else
				refChild = _paging;
		} else if (refChild == _paging) {
			if (_listfoot != null)
				refChild = _listfoot;
			else if (_frozen != null)
				refChild = _frozen;
		}
		return refChild;
	}

	/**
	 * If the child is a listgroup, its listgroupfoot will be removed at the
	 * same time.
	 */
	public boolean removeChild(Component child) {
		if (_paging == child && _pgi == child && inPagingMold())
			throw new IllegalStateException(
					"The paging component cannot be removed manually. It is removed automatically when changing the mold");
		// Feature 1906110: prevent developers from removing it accidently

		if (child instanceof Listitem && child.getParent() == this)
			beforeRemove(child);

		if (!super.removeChild(child))
			return false;

		if (_listhead == child) {
			_listhead = null;
			--_hdcnt;
		} else if (_listfoot == child) {
			_listfoot = null;
		} else if (_frozen == child) {
			_frozen = null;
		} else if (child instanceof Listitem) {
			// maintain items
			final Listitem item = (Listitem) child;
			final int index = item.getIndex();
			item.setIndexDirectly(-1); // mark

			// Maintain selected
			if (item.isSelected()) {
				_selItems.remove(item);
				if (_jsel == index) {
					fixSelectedIndex(index);
				}
			} else {
				if (!isLoadingModel() && _jsel >= index) {
					--_jsel;
				}
			}
			fixGroupsInfoAfterRemove(child, index);
		} else if (_paging == child) {
			_paging = null;
			if (_pgi == child)
				_pgi = null;
		} else if (child instanceof Auxhead) {
			--_hdcnt;
		}

		if (((Cropper) getDataLoader()).isCropper()) {
			getDataLoader().updateModelInfo();
		}

		return true;
	}

	/**
	 * Callback if a list item has been inserted.
	 * <p>
	 * Note: it won't be called if other kind of child is inserted.
	 * <p>
	 * When this method is called, the index is correct.
	 * <p>
	 * Default: invalidate if it is the paging mold and it affects the view of
	 * the active page.
	 *
	 * @since 3.0.5
	 */
	protected void afterInsert(Component comp) {
		if (_isReplacingItem) //@see Renderer#render
			return; //called by #insertBefore(), skip handling GroupInfo
		
		updateVisibleCount((Listitem) comp, false);
		checkInvalidateForMoved((Listitem) comp, false);
	}

	/**
	 * Callback if a list item will be removed (not removed yet). Note: it won't
	 * be called if other kind of child is removed.
	 * <p>
	 * Default: invalidate if it is the paging mold and it affects the view of
	 * the active page.
	 *
	 * @since 3.0.5
	 */
	protected void beforeRemove(Component comp) {
		if (_isReplacingItem) //@see Renderer#render
			return; //called by #removeChild(), skip handling GroupInfo
		
		updateVisibleCount((Listitem) comp, true);
		checkInvalidateForMoved((Listitem) comp, true);
	}

	/**
	 * Update the number of the visible item before it is removed or after it is
	 * added.
	 */
	private void updateVisibleCount(Listitem item, boolean isRemove) {
		if (item instanceof Listgroup || item.isVisible()) {
			final Listgroup g = getListgroupAt(item.getIndex());

			// We shall update the number of the visible item in the following
			// cases.
			// 1) If the item is a type of Listgroupfoot, it is always shown.
			// 2) If the item is a type of Listgroup, it is always shown.
			// 3) If the item doesn't belong to any group.
			// 4) If the group of the item is open.
			if (item instanceof Listgroupfoot || item instanceof Listgroup
					|| g == null || g.isOpen())
				addVisibleItemCount(isRemove ? -1 : 1);

			if (item instanceof Listgroup) {
				final Listgroup group = (Listgroup) item;

				// If the previous group exists, we shall update the number of
				// the visible item from the number of the visible item of the
				// current group.
				if (item.getPreviousSibling() instanceof Listitem) {
					final Listitem preRow = (Listitem) item
							.getPreviousSibling();
					if (preRow == null) {
						if (!group.isOpen()) {
							addVisibleItemCount(isRemove ? group
									.getVisibleItemCount() : -group
									.getVisibleItemCount());
						}
					} else {
						final Listgroup preGroup = preRow instanceof Listgroup ? (Listgroup) preRow
								: getListgroupAt(preRow.getIndex());
						if (preGroup != null) {
							if (!preGroup.isOpen() && group.isOpen())
								addVisibleItemCount(isRemove ? -group
										.getVisibleItemCount() : group
										.getVisibleItemCount());
							else if (preGroup.isOpen() && !group.isOpen())
								addVisibleItemCount(isRemove ? group
										.getVisibleItemCount() : -group
										.getVisibleItemCount());
						} else {
							if (!group.isOpen())
								addVisibleItemCount(isRemove ? group
										.getVisibleItemCount() : -group
										.getVisibleItemCount());
						}
					}
				} else if (!group.isOpen()) {
					addVisibleItemCount(isRemove ? group.getVisibleItemCount()
							: -group.getVisibleItemCount());
				}
			}
		}
		if (inPagingMold())
			getPaginal().setTotalSize(getDataLoader().getTotalSize());
	}

	/**
	 * Checks whether to invalidate, when a child has been added or or will be
	 * removed.
	 *
	 * @param bRemove
	 *            if child will be removed
	 */
	private void checkInvalidateForMoved(Listitem child, boolean bRemove) {
		// No need to invalidate if
		// 1) act == last and child in act
		// 2) act != last and child after act
		// Except removing last elem which in act and act has only one elem
		if (inPagingMold() && !isInvalidated()) {
			final int j = child.getIndex(), pgsz = getPageSize(), n = (getActivePage() + 1)
					* pgsz;
			if (j >= n)
				return; // case 2

			final int cnt = getItems().size(), n2 = n - pgsz;
			if (j >= n2 && cnt <= n && (!bRemove || cnt > n2 + 1))
				return; // case 1

			invalidate();
		}
	}

	/**
	 * An iterator used by visible children.
	 */
	private class VisibleChildrenIterator implements Iterator {
		private final ListIterator _it = getItems().listIterator();
		private int _count = 0;
		private boolean _isBeginning = false;

		private VisibleChildrenIterator() {
		}

		private VisibleChildrenIterator(boolean isBeginning) {
			_isBeginning = isBeginning;
		}

		public boolean hasNext() {
			if (!inPagingMold())
				return _it.hasNext();

			if (!_isBeginning && _count >= getPaginal().getPageSize()) {
				return false;
			}

			if (_count == 0 && !_isBeginning) {
				final Paginal pgi = getPaginal();
				int begin = pgi.getActivePage() * pgi.getPageSize();
				for (int i = 0; i < begin && _it.hasNext();) {
					getVisibleRow((Listitem) _it.next());
					i++;
				}
			}
			return _it.hasNext();
		}

		private Listitem getVisibleRow(Listitem item) {
			if (item instanceof Listgroup) {
				final Listgroup g = (Listgroup) item;
				if (!g.isOpen()) {
					for (int j = 0, len = g.getItemCount(); j < len
							&& _it.hasNext(); j++)
						_it.next();
				}
			}
			while (!item.isVisible() && _it.hasNext())
				item = (Listitem) _it.next();
			return item;
		}

		public Object next() {
			if (!inPagingMold())
				return _it.next();
			_count++;
			final Listitem item = (Listitem) _it.next();
			return _it.hasNext() ? getVisibleRow(item) : item;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Fix the selected index, _jsel, assuming there are no selected one before
	 * (and excludes) j-the item.
	 */
	private void fixSelectedIndex(int j) {
		if (!_selItems.isEmpty()) {
			int realj = getRealIndex(j);
			if (realj < 0)
				realj = 0;
			if (realj < _items.size()) {
				for (Iterator<Listitem> it = _items.listIterator(realj); it.hasNext(); ++j) {
					final Listitem item = it.next();
					if (item.isSelected()) {
						_jsel = j;
						return;
					}
				}
			}
		}
		_jsel = -1;
	}

	private void fixGroupsInfoBeforeInsert(Component newChild, Component refChild, boolean isReorder) {
		if (_isReplacingItem) //@see Renderer#render
			return; //called by #insertBefore(), skip handling GroupInfo
		
		if (newChild instanceof Listgroupfoot) {
			if (refChild == null) {
				if (isReorder) {
					final int idx = ((Listgroupfoot) newChild).getIndex();
					final int[] ginfo = getGroupsInfoAt(idx);
					if (ginfo != null) {
						ginfo[1]--;
						ginfo[2] = -1;
					}
				}
				final int[] g = _groupsInfo.get(getGroupCount() - 1);

				g[2] = getItems().get(getItems().size() - 1)
						.getIndex();
			} else if (refChild instanceof Listitem) {
				final int idx = ((Listitem) refChild).getIndex();
				final int[] g = getGroupsInfoAt(idx);
				if (g == null)
					throw new UiException(
							"Listgroupfoot cannot exist alone, you have to add a Listgroup first");
				if (g[2] != -1)
					throw new UiException(
							"Only one Listgroupfoot is allowed per Listgroup");
				if (idx != (g[0] + g[1]))
					throw new UiException(
							"Listgroupfoot must be placed after the last Row of the Listgroup");
				g[2] = idx - 1;
				if (isReorder) {
					final int nindex = ((Listgroupfoot) newChild)
							.getIndex();
					final int[] ginfo = getGroupsInfoAt(nindex);
					if (ginfo != null) {
						ginfo[1]--;
						ginfo[2] = -1;
					}
				}
			} else {
				final Component preRefChild = refChild.getPreviousSibling();
				if (preRefChild instanceof Listitem) {
					final int idx = ((Listitem) preRefChild).getIndex();
					//bug 2936019: Execption when Listbox insertBefore() group + groupfoot
					final int[] g = getGroupsInfoAt(idx, preRefChild instanceof Listgroup);
					if (g == null)
						throw new UiException(
								"Listgroupfoot cannot exist alone, you have to add a Listgroup first");
					if (g[2] != -1)
						throw new UiException(
								"Only one Listgroupfoot is allowed per Listgroup");
					if (idx + 1 != (g[0] + g[1]))
						throw new UiException(
								"Listgroupfoot must be placed after the last Row of the Listgroup");
					g[2] = idx;
					if (isReorder) {
						final int nindex = ((Listgroupfoot) newChild)
								.getIndex();
						final int[] ginfo = getGroupsInfoAt(nindex);
						if (ginfo != null) {
							ginfo[1]--;
							ginfo[2] = -1;
						}
					}
				}
			}
		}
	}

	private void fixGroupsInfoAfterInsert(Listitem newItem) {
		if (_isReplacingItem) //@see Renderer#render
			return; //called by #insertBefore(), skip handling GroupInfo
		
		if (newItem instanceof Listgroup) {
			Listgroup lg = (Listgroup) newItem;
			if (_groupsInfo.isEmpty())
				_groupsInfo.add(new int[] { lg.getIndex(),
						getItemCount() - lg.getIndex(), -1 });
			else {
				int idx = 0;
				int[] prev = null, next = null;
				for (int[] g: _groupsInfo) {
					if (g[0] <= lg.getIndex()) {
						prev = g;
						idx++;
					} else {
						next = g;
						break;
					}
				}
				if (prev != null) {
					int index = lg.getIndex(), leng = index - prev[0], size = prev[1]
							- leng + 1;
					prev[1] = leng;
					_groupsInfo.add(idx, new int[] { index, size,
						size > 1 && prev[2] >= index ? prev[2] + 1 : -1 });
					if (size > 1 && prev[2] > index)
						prev[2] = -1; // reset listgroupfoot
				} else if (next != null) {
					_groupsInfo.add(idx, new int[] { lg.getIndex(),
							next[0] - lg.getIndex(), -1 });
				}
			}
		} else if (!_groupsInfo.isEmpty()) {
			int index = newItem.getIndex();
			final int[] g = getGroupsInfoAt(index);
			if (g != null) {
				g[1]++;
				if (g[2] != -1
						&& (g[2] >= index || newItem instanceof Listgroupfoot))
					g[2] = g[0] + g[1] - 1;
			}
		}
	}

	private void fixGroupsInfoAfterRemove(Component child, int index) {
		if (!_isReplacingItem) {//@see Renderer#render
			//called by #removeChild(), handling GroupInfo if !isReplcingItem
			if (child instanceof Listgroup) {
				int[] prev = null, remove = null;
				for (int[] g: _groupsInfo) {
					if (g[0] == index) {
						remove = g;
						break;
					}
					prev = g;
				}
				if (prev != null && remove != null) {
					prev[1] += remove[1] - 1;
				}
				fixItemIndices(index, -1, false);
				if (remove != null) {
					_groupsInfo.remove(remove);
					final int idx = remove[2];
					if (idx != -1) {
						final int realIndex = getRealIndex(idx) - 1;
						if (realIndex >= 0 && realIndex < getItemCount())
							removeChild(getChildren().get(realIndex));
					}
				}
			} else if (!_groupsInfo.isEmpty()) {
				final int[] g = getGroupsInfoAt(index);
				if (g != null) {
					g[1]--;
					if (g[2] != -1)
						g[2]--;
					fixItemIndices(index, -1, false);
				} else
					fixItemIndices(index, -1, false);
	
				if (child instanceof Listgroupfoot) {
					final int[] g1 = getGroupsInfoAt(index);
					if (g1 != null)
						g1[2] = -1;
				}
			} else
				fixItemIndices(index, -1);
		}

		if (hasGroupsModel() && getItemCount() <= 0) { // remove to empty,
			// reset _groupsInfo
			_groupsInfo = new LinkedList<int[]>();
		}
		//bug 3057288
		//getDataLoader().updateModelInfo(); //itemsInvalidate after really removed
		//return true;
	}
	
	/**
	 * Fix Childitem._index since j-th item.
	 *
	 * @param j
	 *            the start index (inclusion)
	 * @param to
	 *            the end index (inclusion). If -1, up to the end.
	 */
	private void fixItemIndices(int j, int to) {
		int realj = getRealIndex(j);
		if (realj < 0)
			realj = 0;
		if (realj < _items.size()) {
			for (Iterator<Listitem> it = _items.listIterator(realj); it.hasNext()
					&& (to < 0 || j <= to); ++j)
				it.next().setIndexDirectly(j);
		}
	}

	// -- ListModel dependent codes --//
	/**
	 * Returns the model associated with this list box, or null if this list box
	 * is not associated with any list data model.
	 *
	 * <p>
	 * Note: if {@link #setModel(GroupsModel)} was called with a groups model,
	 * this method returns an instance of {@link ListModel} encapsulating it.
	 *
	 * @see #setModel(ListModel)
	 * @see #setModel(GroupsModel)
	 */
	@SuppressWarnings("unchecked")
	public <T> ListModel<T> getModel() {
		return (ListModel)_model;
	}

	/**
	 * Returns the list model associated with this list box, or null if this
	 * list box is associated with a {@link GroupsModel} or not associated with
	 * any list data model.
	 *
	 * @since 3.5.0
	 * @see #setModel(ListModel)
	 */
	@SuppressWarnings("unchecked")
	public <T> ListModel<T> getListModel() {
		return _model instanceof GroupsListModel ? null : (ListModel)_model;
	}

	/**
	 * Returns the groups model associated with this list box, or null if this
	 * list box is associated with a {@link ListModel} or not associated with
	 * any list data model.
	 *
	 * @since 3.5.0
	 * @see #setModel(GroupsModel)
	 */
	@SuppressWarnings("unchecked")
	public <D, G, F> GroupsModel<D, G, F> getGroupsModel() {
		return _model instanceof GroupsListModel ? ((GroupsListModel) _model)
				.getGroupsModel() : null;
	}

	/**
	 * Sets the lis
