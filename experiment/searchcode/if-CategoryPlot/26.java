//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormTableModel;
import org.nuclos.client.ui.collect.SubFormFilter.SubFormRowFilter;
import org.nuclos.client.ui.collect.component.AbstractCollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.common.E;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collect.collectable.searchcondition.CollectableLikeCondition;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;


/**
 * Panel for displaying the logbook.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class HistorySliderPanel extends JPanel implements ChangeListener, ChartMouseListener {

	private static class HighlightedBarRenderer extends BarRenderer {
		private int highlightRow = -1;
		private int highlightColumn = -1;

		public void setHighlightedItem(int i, int j) {
			if (highlightRow == i && highlightColumn == j) {
				return;
			} else {
				highlightRow = i;
				highlightColumn = j;
				notifyListeners(new RendererChangeEvent(this));
				return;
			}
		}

		public Paint getItemOutlinePaint(int i, int j) {
			if (i == highlightRow && j == highlightColumn)
				return Color.yellow;
			else
				return super.getItemOutlinePaint(i, j);
		}
	}
	
	private static class FormatedGregorianCalendar extends GregorianCalendar {
		private int type;
		public FormatedGregorianCalendar(int type) {
			super();
			this.type = type;
			
		}
		public FormatedGregorianCalendar(int type, Calendar c) {
			this(type);
			this.setTimeInMillis(c.getTimeInMillis());
			
		}
		public static GregorianCalendar getInstance(int type, Calendar c) {
			return new FormatedGregorianCalendar(type, c);
		}
		@Override
		public String toString() {
			switch (type) {
			case Calendar.YEAR:
				return CollectableFieldFormat.getInstance(Date.class).format("yyyy", this.getTime());
			case Calendar.MONTH:
				return CollectableFieldFormat.getInstance(Date.class).format("MM/yyyy", this.getTime());
			case Calendar.DATE:
				return CollectableFieldFormat.getInstance(Date.class).format("dd.MM.yyyy", this.getTime());
			case Calendar.HOUR:
				return CollectableFieldFormat.getInstance(Date.class).format("HH:mm", this.getTime());
			case Calendar.MINUTE:
				return CollectableFieldFormat.getInstance(Date.class).format("mm:ss", this.getTime());
			default:
				return super.toString();
			}
		}
	}

	private final SubForm subform;
	
	private final JScrollBar scroller;
	private SlidingCategoryDataset dataset;
	private HighlightedBarRenderer renderer;
	
	private String title;
	private ChartPanel chartpanel;
	
	private Collection<EntityObjectVO<Long>> collHistoryEntries;
	private Map<Calendar, Collection<EntityObjectVO<Long>>> collYearHistoryEntries;
	private Map<Calendar, Collection<EntityObjectVO<Long>>> collMonthHistoryEntries;
	private Map<Calendar, Collection<EntityObjectVO<Long>>> collDayHistoryEntries;
	private Map<Calendar, Collection<EntityObjectVO<Long>>> collHourHistoryEntries;
	private Map<Calendar, Collection<EntityObjectVO<Long>>> collMinuteHistoryEntries;

	private int iCurrentType = Calendar.YEAR;
	private Calendar iCurrentItem = null;
	
	private static final int FACTOR = 100;
	private static final int MINYEARS = 4;

	private Object[] createDataset(Collection<EntityObjectVO<Long>> col, Integer type, Calendar current) {
		int maxValue = FACTOR;
		int maxValueColumn = -1;
		final DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();

		Map<Calendar, Collection<EntityObjectVO<Long>>> categories = Collections.emptyMap();
		switch (type) {
		case Calendar.YEAR:
			if (collYearHistoryEntries == null)
				collYearHistoryEntries = getYearCategories(col);
			categories = collYearHistoryEntries;
			break;
		case Calendar.MONTH:
			categories = getMonthCategories(current, col);
			collMonthHistoryEntries = categories;
			break;
		case Calendar.DATE:
			categories = getDayCategories(current, col);
			collDayHistoryEntries = categories;
			break;
		case Calendar.HOUR:
			categories = getHourCategories(current, col);
			collHourHistoryEntries = categories;
			break;
		case Calendar.MINUTE:
			categories = getMinuteCategories(current, col);
			collMinuteHistoryEntries = categories;
			break;
		default:
			break;
		}
		
		final List<Calendar> calKeys = new ArrayList<Calendar>(categories.keySet());
		Collections.sort(calKeys, new Comparator<Calendar>() {
			@Override
			public int compare(Calendar o1, Calendar o2) {
				return LangUtils.compare(o1.getTime(), o2.getTime());
			}
		});
		
		int i = 0;
		for (Calendar c : calKeys) {
			i++;
			final Collection<EntityObjectVO<Long>> values = categories.get(c);
			
			int maxValueNew = Math.max(maxValue, values.size() * FACTOR);
			if (maxValue != maxValueNew)
				maxValueColumn = i;
			maxValue = maxValueNew;
			defaultcategorydataset.addValue(values.size() * FACTOR, type, FormatedGregorianCalendar.getInstance(type, c));
		}
		
		return new Object[]{defaultcategorydataset, maxValue, maxValueColumn};
	}
	
	private Map<Calendar, Collection<EntityObjectVO<Long>>> getYearCategories(Collection<EntityObjectVO<Long>> col) {
		final Map<Calendar, Collection<EntityObjectVO<Long>>> mpCategories = new HashMap<Calendar, Collection<EntityObjectVO<Long>>>();
		
		List<EntityObjectVO<Long>> data = new ArrayList<EntityObjectVO<Long>>(col);
		Collections.sort(data, new Comparator<EntityObjectVO<Long>>() {
			@Override
			public int compare(EntityObjectVO<Long> o1, EntityObjectVO<Long> o2) {
				return LangUtils.compare(o1.getFieldValue(E.HISTORY.validuntil.getUID()), o2.getFieldValue(E.HISTORY.validuntil.getUID()));
			}
		});
		
		// add at least MINYEARS years.
		final Calendar current = Calendar.getInstance();
		for (int i = 0; i < MINYEARS; i++) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, (current.get(Calendar.YEAR) - MINYEARS + i));
			normalizeCalendar(c, Calendar.YEAR);
			mpCategories.put(c , new ArrayList<EntityObjectVO<Long>>());
		}
		
		for (EntityObjectVO<Long> eo : data) {
			final Calendar c = Calendar.getInstance();
			c.setTime(((InternalTimestamp)eo.getFieldValue(E.HISTORY.validuntil.getUID())));
			normalizeCalendar(c, Calendar.YEAR);
			if (!mpCategories.containsKey(c)) {
				mpCategories.put(c, new ArrayList<EntityObjectVO<Long>>());
			}
			mpCategories.get(c).add(eo);
		}

		return mpCategories;
	}

	private Map<Calendar, Collection<EntityObjectVO<Long>>> getMonthCategories(Calendar current, Collection<EntityObjectVO<Long>> col) {
		final Map<Calendar, Collection<EntityObjectVO<Long>>> mpCategories = new HashMap<Calendar, Collection<EntityObjectVO<Long>>>();
		
		List<EntityObjectVO<Long>> data = new ArrayList<EntityObjectVO<Long>>(col);
		Collections.sort(data, new Comparator<EntityObjectVO<Long>>() {
			@Override
			public int compare(EntityObjectVO<Long> o1, EntityObjectVO<Long> o2) {
				return LangUtils.compare(o1.getFieldValue(E.HISTORY.validuntil.getUID()), o2.getFieldValue(E.HISTORY.validuntil.getUID()));
			}
		});
		
		// add at least 12 month.
		for (int i = 0; i < 12; i++) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, current.get(Calendar.YEAR));
			c.set(Calendar.MONTH, i);
			normalizeCalendar(c, Calendar.MONTH);
			mpCategories.put(c , new ArrayList<EntityObjectVO<Long>>());
		}
		
		for (EntityObjectVO<Long> eo : data) {
			final Calendar c = Calendar.getInstance();
			c.setTime(((InternalTimestamp)eo.getFieldValue(E.HISTORY.validuntil.getUID())));
			normalizeCalendar(c, Calendar.MONTH);
			if (!mpCategories.containsKey(c)) {
				mpCategories.put(c, new ArrayList<EntityObjectVO<Long>>());
			}
			mpCategories.get(c).add(eo);
		}

		return mpCategories;
	}
	
	private Map<Calendar, Collection<EntityObjectVO<Long>>> getDayCategories(Calendar current, Collection<EntityObjectVO<Long>> col) {
		final Map<Calendar, Collection<EntityObjectVO<Long>>> mpCategories = new HashMap<Calendar, Collection<EntityObjectVO<Long>>>();
		
		List<EntityObjectVO<Long>> data = new ArrayList<EntityObjectVO<Long>>(col);
		Collections.sort(data, new Comparator<EntityObjectVO<Long>>() {
			@Override
			public int compare(EntityObjectVO<Long> o1, EntityObjectVO<Long> o2) {
				return LangUtils.compare(o1.getFieldValue(E.HISTORY.validuntil.getUID()), o2.getFieldValue(E.HISTORY.validuntil.getUID()));
			}
		});
		
		// add at least x (30 or 31) day of month.
		Calendar tmp = Calendar.getInstance();
		tmp.setTimeInMillis(current.getTimeInMillis());
	    tmp.set(Calendar.DATE, tmp.getActualMaximum(Calendar.DATE));
		for (int i = 0; i < tmp.get(Calendar.DATE); i++) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, current.get(Calendar.YEAR));
			c.set(Calendar.MONTH, current.get(Calendar.MONTH));
			c.set(Calendar.DATE, i + 1);
			normalizeCalendar(c, Calendar.DATE);
			mpCategories.put(c , new ArrayList<EntityObjectVO<Long>>());
		}
		
		for (EntityObjectVO<Long> eo : data) {
			final Calendar c = Calendar.getInstance();
			c.setTime((((InternalTimestamp)eo.getFieldValue(E.HISTORY.validuntil.getUID()))));
			normalizeCalendar(c, Calendar.DATE);
			if (!mpCategories.containsKey(c)) {
				mpCategories.put(c, new ArrayList<EntityObjectVO<Long>>());
			}
			mpCategories.get(c).add(eo);
		}

		return mpCategories;
	}
	
	private Map<Calendar, Collection<EntityObjectVO<Long>>> getHourCategories(Calendar current, Collection<EntityObjectVO<Long>> col) {
		final Map<Calendar, Collection<EntityObjectVO<Long>>> mpCategories = new HashMap<Calendar, Collection<EntityObjectVO<Long>>>();
		
		List<EntityObjectVO<Long>> data = new ArrayList<EntityObjectVO<Long>>(col);
		Collections.sort(data, new Comparator<EntityObjectVO<Long>>() {
			@Override
			public int compare(EntityObjectVO<Long> o1, EntityObjectVO<Long> o2) {
				return LangUtils.compare(o1.getFieldValue(E.HISTORY.validuntil.getUID()), o2.getFieldValue(E.HISTORY.validuntil.getUID()));
			}
		});
		
		// add at least 24 hours.
		for (int i = 0; i < 24; i++) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, current.get(Calendar.YEAR));
			c.set(Calendar.MONTH, current.get(Calendar.MONTH));
			c.set(Calendar.DATE, current.get(Calendar.DATE));
			c.set(Calendar.HOUR_OF_DAY, i);
			normalizeCalendar(c, Calendar.HOUR);
			mpCategories.put(c , new ArrayList<EntityObjectVO<Long>>());
		}
		
		for (EntityObjectVO<Long> eo : data) {
			final Calendar c = Calendar.getInstance();
			c.setTime((((InternalTimestamp)eo.getFieldValue(E.HISTORY.validuntil.getUID()))));
			normalizeCalendar(c, Calendar.HOUR);
			if (!mpCategories.containsKey(c)) {
				mpCategories.put(c, new ArrayList<EntityObjectVO<Long>>());
			}
			mpCategories.get(c).add(eo);
		}

		return mpCategories;
	}

	private Map<Calendar, Collection<EntityObjectVO<Long>>> getMinuteCategories(Calendar current, Collection<EntityObjectVO<Long>> col) {
		final Map<Calendar, Collection<EntityObjectVO<Long>>> mpCategories = new HashMap<Calendar, Collection<EntityObjectVO<Long>>>();
		
		List<EntityObjectVO<Long>> data = new ArrayList<EntityObjectVO<Long>>(col);
		Collections.sort(data, new Comparator<EntityObjectVO<Long>>() {
			@Override
			public int compare(EntityObjectVO<Long> o1, EntityObjectVO<Long> o2) {
				return LangUtils.compare(o1.getFieldValue(E.HISTORY.validuntil.getUID()), o2.getFieldValue(E.HISTORY.validuntil.getUID()));
			}
		});
		
		// add at least 60 minutes.
		for (int i = 0; i < 60; i++) {
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, current.get(Calendar.YEAR));
			c.set(Calendar.MONTH, current.get(Calendar.MONTH));
			c.set(Calendar.DATE, current.get(Calendar.DATE));
			c.set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY));
			c.set(Calendar.MINUTE, i);
			normalizeCalendar(c, Calendar.MINUTE);
			mpCategories.put(c , new ArrayList<EntityObjectVO<Long>>());
		}
		
		for (EntityObjectVO<Long> eo : data) {
			final Calendar c = Calendar.getInstance();
			c.setTime((((InternalTimestamp)eo.getFieldValue(E.HISTORY.validuntil.getUID()))));
			normalizeCalendar(c, Calendar.MINUTE);
			if (!mpCategories.containsKey(c)) {
				mpCategories.put(c, new ArrayList<EntityObjectVO<Long>>());
			}
			mpCategories.get(c).add(eo);
		}

		return mpCategories;
	}

	private JFreeChart createChart(CategoryDataset categorydataset, int maxValue)	{
		final JFreeChart jfreechart = ChartFactory.createBarChart(this.title, "", "", categorydataset, PlotOrientation.VERTICAL, false, false, false);
		final CategoryPlot categoryplot = (CategoryPlot)jfreechart.getPlot();
		
		final CategoryAxis categoryaxis = categoryplot.getDomainAxis();
		categoryaxis.setMaximumCategoryLabelWidthRatio(0.8F);
		categoryaxis.setLowerMargin(0.02D);
		categoryaxis.setUpperMargin(0.02D);
		final NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
		numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		numberaxis.setRange(0.0D, maxValue * 1.1);
		numberaxis.setVisible(false);
		categoryplot.setRenderer(renderer);
		
		final GradientPaint gradientpaint = new GradientPaint(0.0F, 0.0F, Color.blue, 0.0F, 0.0F, new Color(0, 0, 64));
		renderer.setSeriesPaint(0, gradientpaint);
		
		final Font font = jfreechart.getTitle().getFont().deriveFont((float)16.0).deriveFont(Font.PLAIN);
		jfreechart.getTitle().setFont(font);
		
		return jfreechart;
	}

	public void stateChanged(ChangeEvent changeevent) {
		try {
			dataset.setFirstCategoryIndex(scroller.getValue());
		} catch (Exception e) {
			// ignore.
		}
	}
    
    private JFreeChart createCategoryChart(Collection<EntityObjectVO<Long>> col, int type, Calendar c) {
    	final Object[] ds = createDataset(col, type, c);
    	final int colCount = ((CategoryDataset)ds[0]).getColumnCount();

		scroller.setMaximum(colCount - 10);
		dataset = new SlidingCategoryDataset((CategoryDataset)ds[0], 0, Math.min(12, colCount));

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scroller.setValue((int)(((Integer)ds[2])*0.7));
			}
		});
		return createChart(dataset, (Integer)ds[1]);
    }		
    
    private void setHistorySliderEntries(int type, Calendar c) {
    	iCurrentType = type;
    	iCurrentItem = c;

		switch (type) {
		case Calendar.YEAR:
			chartpanel.setChart(createCategoryChart(
					collYearHistoryEntries.get(c), Calendar.MONTH, c));
			break;
		case Calendar.MONTH:
			chartpanel.setChart(createCategoryChart(
					collMonthHistoryEntries.get(c), Calendar.DATE, c));
			break;
		case Calendar.DATE:
			chartpanel.setChart(createCategoryChart(
					collDayHistoryEntries.get(c), Calendar.HOUR, c));
			break;
		case Calendar.HOUR:
			chartpanel.setChart(createCategoryChart(
					collHourHistoryEntries.get(c), Calendar.MINUTE, c));
			break;
		case Calendar.MINUTE:
			chartpanel.setChart(createCategoryChart(
					collMinuteHistoryEntries.get(c), Calendar.SECOND, c));
			break;
		default:
			chartpanel.setChart(createCategoryChart(collHistoryEntries, Calendar.YEAR, null));
			break;
		}

		chartpanel.setDomainZoomable(false);
		chartpanel.setRangeZoomable(false);
    }
    
    private void setFixedRowFilter(int type, Calendar c) {
	    final ArrayList<RowFilter<TableModel, Integer>> filters = new ArrayList<RowFilter<TableModel,Integer>>();
		
	    final SubFormTableModel tblmdl = subform.getSubformTable().getSubFormModel();
	    final CollectableEntityField cef = tblmdl.getCollectableEntityField(tblmdl.findColumnByFieldUid(E.HISTORY.validuntil.getUID()));
	    final CollectableComponent clctcomp = CollectableComponentFactory.getInstance().newCollectableComponent(cef, null, true);

		final String dateSeparator = SpringLocaleDelegate.getInstance().getLocale().getLanguage().equals(Locale.ENGLISH.getLanguage()) ? "/" : ".";

	    switch (type) {
		case Calendar.YEAR:
			((AbstractCollectableComponent)clctcomp).setSearchCondition(
					new CollectableLikeCondition(cef, "*" + dateSeparator + c.get(Calendar.YEAR) + " "));
	        break;
		case Calendar.MONTH:
			((AbstractCollectableComponent)clctcomp).setSearchCondition(
					new CollectableLikeCondition(cef, "*" + dateSeparator + (c.get(Calendar.MONTH) + 1) + dateSeparator + c.get(Calendar.YEAR) + " "));
			break;
		case Calendar.DATE:
			((AbstractCollectableComponent)clctcomp).setSearchCondition(
					new CollectableLikeCondition(cef, c.get(Calendar.DATE) + dateSeparator + (c.get(Calendar.MONTH) + 1) + dateSeparator + c.get(Calendar.YEAR) + " "));
			break;
		case Calendar.HOUR:
			((AbstractCollectableComponent)clctcomp).setSearchCondition(
					new CollectableLikeCondition(cef, 
							c.get(Calendar.DATE) + dateSeparator + (c.get(Calendar.MONTH) + 1)
							+ dateSeparator + c.get(Calendar.YEAR) + " "
									+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY) : "" + c.get(Calendar.HOUR_OF_DAY))  + ":"));
			break;
		case Calendar.MINUTE:
			((AbstractCollectableComponent)clctcomp).setSearchCondition(
					new CollectableLikeCondition(cef, 
							c.get(Calendar.DATE) + dateSeparator + (c.get(Calendar.MONTH) + 1)
							+ dateSeparator + c.get(Calendar.YEAR) + " "
									+ (c.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + c.get(Calendar.HOUR_OF_DAY) : "" + c.get(Calendar.HOUR_OF_DAY)) + ":"
										+ (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : "" + c.get(Calendar.MINUTE)) + ":"));
			break;
		default:
			//chartpanel.setChart(createCategoryChart(collHistoryEntries, Calendar.YEAR));
			break;
		}
	    filters.add(new SubFormRowFilter(cef, clctcomp));
	      
        subform.getSubFormFilter().clearFixedRowFilter();
        for (RowFilter<TableModel, Integer> rowFilter : filters) {
	        subform.getSubFormFilter().addFixedRowFilter(rowFilter);
        }
        subform.getSubFormFilter().doFiltering();

    }
    
	public void chartMouseClicked(ChartMouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e.getTrigger()))
			return;
		
		final ChartEntity chartentity = e.getEntity();
		if (chartentity != null) {
			CategoryItemEntity item = null;
			if (chartentity instanceof CategoryItemEntity)
				item = (CategoryItemEntity)chartentity;
				
			if (item != null) {
				final Integer type = (Integer)item.getRowKey();
				final Calendar c = (Calendar)item.getColumnKey();
				if (type != Calendar.MINUTE) { // last entry
					if (e.getTrigger().getClickCount() == 2) {
						setHistorySliderEntries(type, c);
					}
				}
				setFixedRowFilter(type, c); // just single click.
				
				final CategoryDataset categorydataset = item.getDataset();
				renderer.setHighlightedItem(categorydataset.getRowIndex(item.getRowKey()), categorydataset.getColumnIndex(item.getColumnKey()));
			} else {
				// nothing selected.
		        subform.getSubFormFilter().clearFixedRowFilter();
		        subform.getSubFormFilter().doFiltering();
		        
				renderer.setHighlightedItem(-1, -1);

				if (e.getTrigger().getClickCount() == 2) {
					switch(iCurrentType) {
					case Calendar.YEAR:
						setHistorySliderEntries(-1, null);
						break;
					case Calendar.MONTH:
						normalizeCalendar(iCurrentItem, Calendar.YEAR);
						setHistorySliderEntries(Calendar.YEAR, iCurrentItem);
						break;
					case Calendar.DATE:
						normalizeCalendar(iCurrentItem, Calendar.MONTH);
						setHistorySliderEntries(Calendar.MONTH, iCurrentItem);
						break;
					case Calendar.HOUR:
						normalizeCalendar(iCurrentItem, Calendar.DATE);
						setHistorySliderEntries(Calendar.DATE, iCurrentItem);
						break;
					case Calendar.MINUTE:
						normalizeCalendar(iCurrentItem, Calendar.HOUR);
						setHistorySliderEntries(Calendar.HOUR, iCurrentItem);
						break;
					}
				}

				if (iCurrentType != -1 && iCurrentItem != null)
					setFixedRowFilter(iCurrentType, iCurrentItem);
			}
		}
	}
	
	private void normalizeCalendar(Calendar c, int type) {
		switch (type) {
		case Calendar.YEAR:
			c.set(c.get(Calendar.YEAR), 0, 1, 0, 0, 0);
			c.set(Calendar.MILLISECOND, 0);
			break;
		case Calendar.MONTH:
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0);
			c.set(Calendar.MILLISECOND, 0);
			break;
		case Calendar.DATE:
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), 0, 0, 0);
			c.set(Calendar.MILLISECOND, 0);
			break;
		case Calendar.HOUR:
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), c.get(Calendar.HOUR_OF_DAY), 0, 0);
			c.set(Calendar.MILLISECOND, 0);
			break;
		case Calendar.MINUTE:
			c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), 0);
			c.set(Calendar.MILLISECOND, 0);
			break;
		}
	}

	public void chartMouseMoved(ChartMouseEvent e) {
		/*final ChartEntity chartentity = e.getEntity();
		if (!(chartentity instanceof CategoryItemEntity)) {
			renderer.setHighlightedItem(-1, -1);
			return;
		}
		
		final CategoryItemEntity categoryitementity = (CategoryItemEntity)chartentity;
		final CategoryDataset categorydataset = categoryitementity.getDataset();
		renderer.setHighlightedItem(categorydataset.getRowIndex(categoryitementity.getRowKey()), categorydataset.getColumnIndex(categoryitementity.getColumnKey()));*/
	}
        
    public HistorySliderPanel(final SubForm subform) {
		super(new BorderLayout());
		
		this.subform = subform;
		
		scroller = new JScrollBar(0, 0, 1, 0, 10);

		renderer = new HighlightedBarRenderer();
		renderer.setDrawBarOutline(true);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				setPreferredSize(new Dimension(getWidth(), (int)(getParent().getHeight() * 0.25)));
				removeComponentListener(this);
			}
		});
		setPreferredSize(new Dimension(getWidth(), 0));
    }
    
    private final JPanel createControlPanel() {
    	final JPanel panel = new JPanel();
    	panel.setOpaque(false);
    	
    	final JButton btnOverview = new JButton();
    	btnOverview.setOpaque(false);
    	btnOverview.setAction(new AbstractAction(
    			SpringLocaleDelegate.getInstance().getMessage("LogbookController.14", "?&#x153;bersicht")) {
			@Override
			public void actionPerformed(ActionEvent e) {
				// nothing selected.
		        subform.getSubFormFilter().clearFixedRowFilter();
		        subform.getSubFormFilter().doFiltering();
		        
				renderer.setHighlightedItem(-1, -1);

				setHistorySliderEntries(-1, null);
			}
		});
    	
    	final JButton btnUp = new JButton();
    	btnUp.setOpaque(false);
    	btnUp.setAction(new AbstractAction("", Icons.getInstance().getSortingUp()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				// nothing selected.
		        subform.getSubFormFilter().clearFixedRowFilter();
		        subform.getSubFormFilter().doFiltering();
		        
				renderer.setHighlightedItem(-1, -1);
				
				switch(iCurrentType) {
				case Calendar.YEAR:
					setHistorySliderEntries(-1, null);
					break;
				case Calendar.MONTH:
					normalizeCalendar(iCurrentItem, Calendar.YEAR);
					setHistorySliderEntries(Calendar.YEAR, iCurrentItem);
					break;
				case Calendar.DATE:
					normalizeCalendar(iCurrentItem, Calendar.MONTH);
					setHistorySliderEntries(Calendar.MONTH, iCurrentItem);
					break;
				case Calendar.HOUR:
					normalizeCalendar(iCurrentItem, Calendar.DATE);
					setHistorySliderEntries(Calendar.DATE, iCurrentItem);
					break;
				case Calendar.MINUTE:
					normalizeCalendar(iCurrentItem, Calendar.HOUR);
					setHistorySliderEntries(Calendar.HOUR, iCurrentItem);
					break;
				}
			}
		});

    	final JButton btnDown = new JButton(Icons.getInstance().getSortingDown());
    	btnDown.setOpaque(false);

    	panel.add(btnOverview);
    	panel.add(btnUp);
    	//panel.add(btnDown);
    	return panel;
    }
    
    public final void init(String title, Collection<EntityObjectVO<Long>> collHistoryEntries) {
	
    	this.title = title;
    	
    	this.collHistoryEntries = collHistoryEntries;
		
		chartpanel = new ChartPanel(createCategoryChart(collHistoryEntries, Calendar.YEAR, null)); // year as default
		// disable popup.
		chartpanel.setPopupMenu(null);
		

		final JPanel pnlControl = createControlPanel();  
		
		final JPanel panel = new JPanel();
	    panel.setLayout (new BorderLayout() {
	    	 @Override
	    	public void layoutContainer(Container target) {
	    		 super.layoutContainer(target);
	    	     
	    	     Dimension size = pnlControl.getPreferredSize();
	    	     pnlControl.setBounds(10, -5, size.width, size.height);
	    	}
	    });
	    panel.add(pnlControl, BorderLayout.CENTER);
		panel.add(chartpanel, BorderLayout.CENTER);
		
		add(panel, BorderLayout.CENTER);
		
		chartpanel.addChartMouseListener(this);
		scroller.getModel().addChangeListener(this);
		
		final JPanel jpanel = new JPanel(new BorderLayout());
		jpanel.add(scroller);
		jpanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		jpanel.setBackground(Color.white);
		add(jpanel, BorderLayout.SOUTH);
    }

}
