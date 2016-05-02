/*
 * Copyright (c) 2010 Tonic Solutions LLC.
 *
 * http://www.nimbits.com
 *
 *
 * Licensed under the GNU GENERAL PUBLIC LICENSE, Version 3.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.nimbits.client.panels;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;
import com.nimbits.client.exception.NimbitsException;
import com.nimbits.client.icons.Icons;
import com.nimbits.client.model.Const;
import com.nimbits.client.model.GxtPointModel;
import com.nimbits.client.model.common.CommonFactoryLocator;
import com.nimbits.client.model.point.Point;
import com.nimbits.client.model.point.PointName;
import com.nimbits.client.model.timespan.InvalidTimespanException;
import com.nimbits.client.model.timespan.Timespan;
import com.nimbits.client.model.timespan.TimespanModel;
import com.nimbits.client.model.timespan.TimespanServiceClientImpl;
import com.nimbits.client.model.value.Value;
import com.nimbits.client.service.datapoints.PointService;
import com.nimbits.client.service.datapoints.PointServiceAsync;
import com.nimbits.client.service.recordedvalues.RecordedValueService;
import com.nimbits.client.service.recordedvalues.RecordedValueServiceAsync;

import java.util.*;

public class AnnotatedTimeLinePanel extends NavigationEventProvider {
    private final DateTimeFormat fmt = DateTimeFormat.getFormat(Const.FORMAT_DATE_TIME);

    private AnnotatedTimeLine line;
    private ContentPanel mainPanel;
    private DataTable dataTable = null;

    private final Map<PointName, Point> points = new HashMap<PointName, Point>();
    private final TextField endDateSelector = new TextField();
    private final TextField startDateSelector = new TextField();
    private Timespan timespan;
    private boolean headerVisible;
    private final String name;
    private boolean selected;


    public AnnotatedTimeLinePanel(boolean showHeader, String name) {
        this.headerVisible = showHeader;
        this.name = name;
    }

    public boolean containsPoint(Point point) {
        return points.containsKey(point.getName());
    }

    public void addValue(Point point, Value value) {
        if (timespan != null) {
            final Date end = (timespan.getEnd().getTime() > value.getTimestamp().getTime()) ? value.getTimestamp() : timespan.getEnd();
            final Date start = (timespan.getStart().getTime() < value.getTimestamp().getTime()) ? value.getTimestamp() : timespan.getStart();
            this.timespan = new TimespanModel(end, start);
            startDateSelector.setValue(fmt.format(this.timespan.getStart()));
            endDateSelector.setValue(fmt.format(this.timespan.getEnd()));
        }


        addPointDataToTable(point, Arrays.asList(value));
        GWT.log("timespan is null: " + (timespan == null));
        drawChart();
    }

    private void addPointDataToTable(Point p, List<Value> values) {
        int PointColumn;


        boolean found = false;

        removePointDataFromTable(CommonFactoryLocator.getInstance().createPointName(Const.DEFAULT_EMPTY_COL));

        int r = dataTable.getNumberOfColumns();
        int CurrentRow = dataTable.getNumberOfRows();
        PointColumn = dataTable.getNumberOfColumns();

        for (int i = 0; i < r; i++) {
            String s = dataTable.getColumnLabel(i);
            if (s.equals(p.getName().getValue())) {
                PointColumn = i;
                found = true;
                break;
            }
        }


        if (!found) {
            dataTable.addColumn(ColumnType.NUMBER, p.getName().getValue());
            dataTable.addColumn(ColumnType.STRING, "title" + r);
            dataTable.addColumn(ColumnType.STRING, "text" + r);
        }

        if (values != null) {
            for (Value v : values) {
                dataTable.addRow();
                dataTable.setValue(CurrentRow, 0, v.getTimestamp());
                dataTable.setValue(CurrentRow, PointColumn, v.getValue());

                String note = v.getNote();
                String name = p.getName().getValue();

                if (note == null || v.getNote().trim().length() == 0) {
                    note = null;//"undefined";
                    name = null;//"undefined";
                }

                //note = null;
                dataTable.setValue(CurrentRow, PointColumn + 2, note);
                dataTable.setValue(CurrentRow, PointColumn + 1, name);

                CurrentRow++;
            }
        }
    }

    private void removePointDataFromTable(PointName pointName) {
        int r = dataTable.getNumberOfColumns();
        for (int i = 0; i < r; i++) {
            String s = dataTable.getColumnLabel(i);
            if (s.equals(pointName.getValue())) {
                dataTable.removeColumns(i, i + 2);
                break;
            }
        }
    }

    private void drawChart() {
        layout();
        line.draw(dataTable, createOptions());
    }

//    public List<Point> getPoints() {
//        return points;
//    }

    @Override
    protected void onRender(final Element parent, final int index) {
        super.onRender(parent, index);

        mainPanel = new ContentPanel();
        mainPanel.setBodyBorder(true);
        mainPanel.setHeaderVisible(headerVisible);

        mainPanel.setFrame(true);
        mainPanel.setTopComponent(toolbar());
        //   mainPanel.setLayout(new FillLayout());
        mainPanel.setHeight(400);
        if (headerVisible) {
            mainPanel.getHeader().addTool(
                    maximizeToolbarButton());
            mainPanel.getHeader().addTool(
                    closeToolbarButton());
        }
        setDropTarget(mainPanel);
        add(mainPanel);
        initChart();
        //  layout(true);
    }

    private ToolButton maximizeToolbarButton() {
        return new ToolButton("x-tool-maximize",
                new SelectionListener<IconButtonEvent>() {
                    boolean isMax;

                    @Override
                    public void componentSelected(final IconButtonEvent ce) {
                        final Window window = new Window();

                        final AnnotatedTimeLinePanel panel = new AnnotatedTimeLinePanel(false, name);

                        // panel.hideHeader();
                        window.add(panel);
                        window.setWidth(800);
                        window.setHeight(800);

                        window.show();
                        panel.resize(770, 790);
                        for (final PointName pointName : points.keySet()) {
                            panel.addPoint(points.get(pointName));
                        }

                    }
                });
    }

    private ToolButton closeToolbarButton() {
        return new ToolButton("x-tool-close",
                new SelectionListener<IconButtonEvent>() {
                    boolean isMax;

                    @Override
                    public void componentSelected(final IconButtonEvent ce) {
                        notifyChartRemovedListener(name);
                    }
                });
    }


    private ToolBar toolbar() {
        final ToolBar toolBar = new ToolBar();


        final Button startDateMenu = new Button();
        startDateMenu.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.calendar()));


        startDateSelector.setSelectOnFocus(false);
        if (timespan != null) {
            startDateSelector.setValue(fmt.format(timespan.getStart()));
        }
        startDateSelector.setToolTip("Start Date");

        startDateSelector.addListener(Events.KeyPress, new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == 13) {
                    refreshChart();
                }
            }
        });


        if (timespan != null) {
            endDateSelector.setValue(fmt.format(timespan.getEnd()));
        }
        endDateSelector.setSelectOnFocus(false);
        endDateSelector.setToolTip("End Date");
        endDateSelector.addListener(Events.KeyPress, new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent be) {
                if (be.getKeyCode() == 13) {
                    refreshChart();
                }
            }
        });


        final Button refresh = new Button();
        refresh.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.refresh2()));


        toolBar.add(startDateSelector);
        // toolBar.add(startDateMenu);
        //  toolBar.add(new SeparatorToolItem());

        toolBar.add(endDateSelector);
        //   toolBar.add(new SeparatorToolItem());

        refresh.addListener(Events.OnClick, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(final BaseEvent be) {
                refreshChart();
            }
        });

        toolBar.add(refresh);

        toolBar.add(new SeparatorToolItem());

        final NumberField min = new NumberField();
        final NumberField max = new NumberField();
        Label minY = new Label("MinY:");
        Label maxY = new Label("MaxY:");
        min.setWidth(30);
        max.setWidth(30);

        min.setValue(0);
        max.setValue(100);

        toolBar.add(minY);
        toolBar.add(min);
        toolBar.add(maxY);
        toolBar.add(max);


        Button refreshRange = new Button();
        refreshRange.setIcon(AbstractImagePrototype.create(Icons.INSTANCE.refresh2()));
        refreshRange.addListener(Events.OnClick, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                Options options = Options.create();
                options.setDisplayAnnotations(true);
                options.setWindowMode(WindowMode.OPAQUE);
                options.setAllowRedraw(true);
                options.setDisplayRangeSelector(true);
                options.setMin(min.getValue().intValue());
                options.setMax(max.getValue().intValue());
                line.draw(dataTable, options);
            }
        });
        toolBar.add(refreshRange);


        return toolBar;
    }

    private void refreshChart() {
        try {
            timespan = TimespanServiceClientImpl.createTimespan(startDateSelector.getValue().toString(), endDateSelector.getValue().toString());
            if (line != null && timespan != null) {
                line.setVisibleChartRange(timespan.getStart(), timespan.getEnd());
                //setTimespan(timespan);
                //startDateSelector.setValue(result.getStart());
                // endDateSelector.setValue(result.getEnd());
                dataTable = DataTable.create();
                dataTable.addColumn(ColumnType.DATETIME, "Date");

                for (PointName pointName : points.keySet()) {
                    addPointToChart(points.get(pointName));
                }
            }

            //      }
            //   });
        } catch (InvalidTimespanException e) {
            GWT.log(e.getMessage(), e);
        }
    }

    public void addPoint(Point point) {
        addPointToChart(point);
    }

    private void addPointToChart(final Point point) {
        if (!points.containsKey(point.getName())) {
            points.put(point.getName(), point);
        }

//            ArrayList<Point> l = new ArrayList<Point>();
        //     l.add(p);
        final int start = 0;
        final int end = 1000;

        if (timespan == null) {
            loadValuesThatExist(point);
        } else {
            loadDataSegment(point, start, end);
        }
    }

    private void loadValuesThatExist(final Point p) {
        final RecordedValueServiceAsync dataService = GWT.create(RecordedValueService.class);
        // final MessageBox box = MessageBox.wait("Progress",
        //        "Loading " + p.getName().getValue() + " values ", "Loading...");
        //box.show();

        dataService.getTopDataSeries(p, 100, new Date(), new AsyncCallback<List<Value>>() {
            @Override
            public void onFailure(Throwable caught) {
                //       box.close();
            }

            @Override
            public void onSuccess(final List<Value> result) {
                Value oldest, newest;

                if (result.size() > 0) {
                    oldest = result.get(result.size() - 1);


                    newest = result.get(0);


                    timespan = new TimespanModel(oldest.getTimestamp(), newest.getTimestamp());
                    setTimespan(timespan);
                }


                addPointDataToTable(p, result);

                drawChart();


                // box.close();
            }
        });
    }

//
//    public void resizePanel(int h, int w) {
//        mainPanel.setWidth(w);
//        mainPanel.setHeight(h);
//        initChart( );
//        doLayout();
//    }

    public void setTimespan(Timespan ts) {
        this.timespan = ts;
        this.startDateSelector.setValue(fmt.format(ts.getStart()));
        this.endDateSelector.setValue(fmt.format(ts.getEnd()));
    }

    private void loadDataSegment(final Point p, final int start, final int end) {
        final RecordedValueServiceAsync dataService = GWT.create(RecordedValueService.class);
        final MessageBox box = MessageBox.wait("Progress",
                "Loading " + p.getName().getValue() + " values " + start + " to " + end, "Loading...");
        box.show();
        //   Timespan timespan = new TimespanModel(startDate, endDate);
        dataService.getPieceOfDataSegment(p, timespan, start, end, new AsyncCallback<List<Value>>() {
            @Override
            public void onFailure(final Throwable caught) {
                box.close();
            }

            @Override
            public void onSuccess(final List<Value> result) {
                addPointDataToTable(p, result);
                if (result.size() > 0) {
                    loadDataSegment(p, end + 1, end + 1000);
                } else {
                    drawChart();
                }

                box.close();
            }
        });
    }

    void resize(final int h, final int w) {
        mainPanel.setHeight(h);
        mainPanel.setWidth(w);
        line.setHeight("100%");
        line.setWidth("100%");
    }

//    public void hideHeader() {
////        mainPanel.setHeaderVisible(false);
//    }

    private void setDropTarget(final Component container) {
        //    DropTarget target = new DropTarget(container) {
        new DropTarget(container) {
            @Override
            protected void onDragDrop(final DNDEvent event) {
                super.onDragDrop(event);
                List<TreeStoreModel> t = event.getData();

                for (final TreeStoreModel a : t) {
                    final GxtPointModel p = (GxtPointModel) a.getModel();

                    final PointServiceAsync pointService = GWT.create(PointService.class);
                    try {
                        pointService.getPointByID(p.getId(), new AsyncCallback<Point>() {
                            @Override
                            public void onFailure(final Throwable throwable) {

                            }

                            @Override
                            public void onSuccess(final Point point) {
                                points.put(point.getName(), point);
                                addPointToChart(point);
                            }
                        });
                    } catch (NimbitsException e) {
                        GWT.log(e.getMessage());
                    }
                }
            }
        };
    }

    public void initChart() {
        Runnable onLoadCallback = new Runnable() {
            @Override
            public void run() {
                if (line != null) {
                    mainPanel.remove(line);
                    line = null;
                }

                dataTable = DataTable.create();
                dataTable.addColumn(ColumnType.DATETIME, Const.WORD_DATE);
//                line = new AnnotatedTimeLine(dataTable, createOptions(), w + "px",
//                        (h - heightMod) + "px");
                line = new AnnotatedTimeLine(dataTable, createOptions(), "100%", "100%");
                //  	line.setVisibleChartRange(startDate, endDate);
                mainPanel.add(line);

                addEmptyDataToTable();
                //emptyPanel.setLayout(new FillLayout());
                //  mainPanel.add(h);
                layout();
                if (points != null && points.size() > 0) {
                    refreshChart();
                }
            }
        };

        VisualizationUtils.loadVisualizationApi(onLoadCallback,
                AnnotatedTimeLine.PACKAGE);
    }

    private Options createOptions() {
        Options options = Options.create();
        options.setDisplayAnnotations(true);
        options.setWindowMode(WindowMode.OPAQUE);
        options.setAllowRedraw(true);
        options.setDisplayRangeSelector(true);

        //options.setDisplayAnnotationsFilter(arg0)
        return options;
    }

    private void addEmptyDataToTable() {
        dataTable.addColumn(ColumnType.NUMBER, Const.DEFAULT_EMPTY_COL);
        dataTable.addColumn(ColumnType.STRING, "title0");
        dataTable.addColumn(ColumnType.STRING, "text0");
    }

    public void removePoint(Point p) {
        removePointDataFromTable(p.getName());
        if (points.containsKey(p.getName())) {
            points.remove(p.getName());
        }
        if (points.size() == 0) {
            removePointDataFromTable(CommonFactoryLocator.getInstance().createPointName(Const.DEFAULT_EMPTY_COL));
            addEmptyDataToTable();
        }

        drawChart();
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setBorders(selected);

    }
}

