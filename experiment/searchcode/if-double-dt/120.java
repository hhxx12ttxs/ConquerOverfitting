package com.el.oers.web.report;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.isf.commons.DateUtil;
import org.isf.commons.bean.Type;
import org.isf.commons.data.Column;
import org.isf.commons.data.Header;
import org.isf.commons.data.Tuple;
import org.primefaces.component.columngroup.ColumnGroup;
import org.primefaces.component.row.Row;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;

import com.el.oers.report.StockALQOverviewBuilderBean;

@Named("stockALQOverview")
@RequestScoped
public class StockALQOverview implements Serializable {

	private static final int VIEWED_WEEKS = 3;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6272862058385630962L;
	
	private Header header;
	private ColumnGroup columnGroup;
	
	private static final String[][] ALQ_OVERVIEW_DEFS = {{"DE", "LZM", "ALQ DE LZM"},{"DE", "KZM", "ALQ DE KZM"},{"DK", "%", "ALQ DK"},{"PL", "%", "ALQ PL"}};
	private LineChartModel alqOverviewChart;
	
	@Inject
	private StockALQOverviewBuilderBean builder;
	
	@PostConstruct
	public void initial() throws Exception {
		header = new Header();
		
		Column c = new Column("Type", Type.STRING);
		c.setRowSpan(2);
		header.addColumn(c);
		
		Date[] dates = getDates();
		for (int i=0;i<dates.length;i++) {
			Date d = dates[i];
			if (i%7 == 0) {
				Calendar cal = DateUtil.getISOCalendar();
				cal.setTime(d);
				c = new Column("KW "+cal.get(Calendar.WEEK_OF_YEAR), Type.DOUBLE);
				c.setColumnSpan(7);
				header.addColumn(c);
			}
			Column sub = new Column(DateUtil.toString("dd.MM.", d), Type.DOUBLE);
			c.addColumn(sub);
		}
		
		setColumnGroup(createColumnGroup());
		
		createCharts();
	}
	
	protected void createCharts() throws Exception {
		alqOverviewChart = createALQOverviewModel();
		
	}
	
	protected LineChartModel createALQOverviewModel() throws Exception {
		LineChartModel model = new LineChartModel();
		model.setTitle("T채gliches ALQ Reporting nach L채nder");
		model.setLegendPosition("e");
		Axis yaxis = model.getAxis(AxisType.Y);
		yaxis.setLabel("Auslastung in %");
		yaxis.setMin(50.0d);
		yaxis.setMax(100.0d);
		
		Date[] dates = getDates();
		
		DateAxis xaxis = new DateAxis();
		xaxis.setMax(DateUtil.toString("yyyy-MM-dd", dates[dates.length-1]));
		xaxis.setTickFormat("%#d.%#m.");
		
		model.getAxes().put(AxisType.X, xaxis);
		
		for (String[] vals : ALQ_OVERVIEW_DEFS) {
			List<Tuple> l = builder.build(vals[0], vals[1], dates);
			ChartSeries series = new ChartSeries();
			series.setLabel(vals[2]);
			
			for (int i=0;i<dates.length;i++) {
				Tuple t = l.get(0);
				Double d = (Double)t.getData(i+1);
				if (d.doubleValue() == 0.0) continue;
				series.set(DateUtil.toString("yyyy-MM-dd", dates[i]), d);
			}
			
			model.addSeries(series);
		}
		
		return model;
	}
	
	public LineChartModel getALQOverviewChart() { return alqOverviewChart; }
	
	public Header getHeader() { return header; }
	
	public String[] getColumnNames() {
		Date[] dates = getDates();
		String[] ret = new String[dates.length+1];
		ret[0] = "Type";
		for (int i=0;i<dates.length;i++)
			ret[i+1] = DateUtil.toString("dd.MM.", dates[i]);
		return ret;		
	}
	
	public ColumnGroup getColumnGroup() { return columnGroup; }
	public void setColumnGroup(ColumnGroup columnGroup) { this.columnGroup = columnGroup; }
	
	protected ColumnGroup createColumnGroup() {
		ColumnGroup grp = new ColumnGroup();
		grp.setType("header");
		
		int cnt = getHeader().levelCount();
		for (int i=1;i<=cnt;i++) {
			Row row = new Row();
			
			for (Column c : getHeader().getColumnsFromLevel(i)) {
				org.primefaces.component.column.Column col = new org.primefaces.component.column.Column();
				col.setHeaderText(c.getDisplayName());
				if (!c.hasColumns())  {
					col.setRowspan(cnt-i+1);
					col.setStyle("min-width: 80px;");
				} else col.setColspan(c.columnSize());
				
				row.getChildren().add(col);
			}
			
			grp.getChildren().add(row);
		}
		
		return grp;
	}
	
	public Date[] getDates() { return DateUtil.getLastWeeks(VIEWED_WEEKS-1); }
	
		
	public List<DisplayTuple> getData(String country, String devision) throws Exception {
		List<Tuple> l = builder.build(country, devision, getDates());	
		List<DisplayTuple> ret = new ArrayList<DisplayTuple>();
		
		for (int i=0;i<l.size();i++) {
			Tuple t = l.get(i);
			DisplayTuple dt = new DisplayTuple();
			String bstyle = (i%3 == 0) ? "font-weight: bold; " : "";
			for (int j=0;j<t.count();j++) {
				String lstyle = bstyle;
				if (j==0 && i%3 > 0) {
					dt.addData(t.getData(j));
					lstyle += "width: 100px; text-align: right; ";
				} else if (j==0) {
					dt.addData(t.getData(j));
					lstyle += "width: 100px; ";
				} else if (i<3) {
					DecimalFormat df = new DecimalFormat("#,##0.0");
					dt.addData(df.format((Double)t.getData(j))+"%");
					lstyle += "text-align: right; ";
				} else {
					DecimalFormat df = new DecimalFormat("#,##0");
					dt.addData(df.format((Double)t.getData(j)));
					lstyle += "text-align: right; ";
				}
				dt.setStyle(j, lstyle);
			}
			ret.add(dt);
		}
		
		return ret;
	}
	
}

