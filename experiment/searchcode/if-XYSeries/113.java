import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class TestHarness extends ApplicationFrame {

    private XYSeries original;
    private XYSeries simplified;

    private JCheckBox mode;
    private JSlider slider;

    private Data data;
    boolean isDataLoaded = false;

    private XYPlot plot;

    private void loadDataFromFile(String filename) {
        FileInputStream file = null;
        Scanner scanner = null;
        isDataLoaded = false;
        try {
            file = new FileInputStream(filename);
            scanner = new Scanner(file);
            scanner.useLocale(Locale.US);

            data = new Data();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss.SSS");
            while (scanner.hasNext()) {
                String time;
                scanner.next("\\d+");
                time = scanner.next("[\\d\\.]+");
                scanner.next("\\w+");
                double latDeg = scanner.nextDouble();
                latDeg += scanner.nextDouble() / 60.0;
                latDeg += scanner.nextDouble() / 3600.0;
                String lat = scanner.next("\\w+");
                if ("S".equals(lat) || "s".equals(lat)) {
                    latDeg = -latDeg;
                }
                double longDeg = scanner.nextDouble();
                longDeg += scanner.nextDouble() / 60.0;
                longDeg += scanner.nextDouble() / 3600.0;
                lat = scanner.next("\\w+");
                if ("W".equals(lat) || "w".equals(lat)) {
                    longDeg = -longDeg;
                }
                double course = scanner.nextDouble();
                data.add(latDeg, longDeg, dateFormat.parse(time).getTime() / 1000.0f, course);
            }
            data.pack();
            isDataLoaded = true;
        } catch (ParseException ex) {
            // ignore
        } catch (IOException ex) {
            // ignore;
        } finally {
            try {
                if (scanner != null) {
                    scanner.close();
                }
                if (file != null) {
                    file.close();
                }
            } catch (IOException ignore) {
                //ignore
            }
        }
        mode.setEnabled(isDataLoaded);
        slider.setEnabled(isDataLoaded);
        if (isDataLoaded) {
            slider.setValue(Math.min(slider.getValue(), data.getLat().length));
            slider.setMaximum(data.getLat().length);
            simplify();
        }
    }

    private void simplify() {
        if (! isDataLoaded) {
            return;
        }
        original.clear();
        simplified.clear();
        if (mode.isSelected()) {
            int[] points = PathUtils.simplifyTimedData(data.getTime(), data.getCourse(), slider.getValue());
            for (int i = 0; i < data.getTime().length; i++) {
                original.add(data.getTime()[i], data.getCourse()[i], false);
            }
            for (int point : points) {
                simplified.add(data.getTime()[point], data.getCourse()[point], false);
            }
            original.add(data.getTime()[data.getTime().length - 1], data.getCourse()[data.getTime().length - 1], true);
        } else {
            int[] points = PathUtils.simplifyGeoPath(data.getLat(), data.getLon(), slider.getValue());
            for (int i = 0; i < data.getLat().length; i++) {
                original.add(
                        Math.toRadians(data.getLon()[i]) * PathUtils.EARTH_RADIUS,
                        Math.toRadians(data.getLat()[i]) * PathUtils.EARTH_RADIUS,
                        false
                );
            }
            for (int point : points) {
                simplified.add(
                        Math.toRadians(data.getLon()[point]) * PathUtils.EARTH_RADIUS,
                        Math.toRadians(data.getLat()[point]) * PathUtils.EARTH_RADIUS,
                        false
                );
            }
            original.add(
                    Math.toRadians(data.getLon()[data.getLon().length - 1]) * PathUtils.EARTH_RADIUS,
                    Math.toRadians(data.getLat()[data.getLat().length - 1]) * PathUtils.EARTH_RADIUS,
                    true
            );
        }
        plot.getRangeAxis().setRange(original.getMinY(), original.getMaxY());
        plot.getDomainAxis().setRange(original.getMinX(), original.getMaxX());
    }

    public TestHarness() {
        super("Test-harness");
        original = new XYSeries("Original", false);
        simplified = new XYSeries("Simplified", false);
        final XYSeriesCollection data = new XYSeriesCollection();
        data.addSeries(original);
        data.addSeries(simplified);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Chart",
                "X",
                "Y",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        plot = (XYPlot) chart.getPlot();
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesPaint(1, Color.BLACK);
        renderer.setSeriesStroke(1, new BasicStroke(3));
        plot.setRenderer(renderer);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        final JButton button = new JButton("Open");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser dialog = new JFileChooser();
                int value = dialog.showOpenDialog(TestHarness.this);
                if (value == JFileChooser.APPROVE_OPTION) {
                    loadDataFromFile(dialog.getSelectedFile().getAbsolutePath());
                }
            }
        });

        mode = new JCheckBox("Time/Course mode");
        mode.setEnabled(false);
        mode.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                simplify();
            }
        });

        final JLabel pointCount = new JLabel();
        slider = new JSlider(JSlider.HORIZONTAL, 2, 30, 30);
        slider.setEnabled(false);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                pointCount.setText(slider.getValue() + "");
                simplify();
            }
        });
        wrapper.add(button);
        wrapper.add(new Label(" "));
        wrapper.add(mode);
        wrapper.setBorder(new EmptyBorder(0, 0, 0, 5));
        topPanel.add(wrapper, BorderLayout.WEST);
        topPanel.add(slider, BorderLayout.CENTER);
        topPanel.add(pointCount, BorderLayout.EAST);
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, chartPanel);
        setContentPane(pane);
    }

    public static void main(final String[] args) {
        final TestHarness test = new TestHarness();
        test.pack();
        RefineryUtilities.centerFrameOnScreen(test);
        test.setVisible(true);

    }

    private static class Data {
        private double[] lat;
        private double[] lon;
        private double[] time;
        private double[] course;
        private int index = 0;

        public Data() {
            lat = new double[1000];
            lon = new double[1000];
            time = new double[1000];
            course = new double[1000];
        }

        public void add(double lat, double lon, double time, double course) {
            if (index >= this.lat.length) {
                this.lat = Arrays.copyOf(this.lat, this.lat.length + 1000);
                this.lon = Arrays.copyOf(this.lon, this.lat.length + 1000);
                this.time = Arrays.copyOf(this.time, this.lat.length + 1000);
                this.course = Arrays.copyOf(this.course, this.lat.length + 1000);
            }
            this.lat[index] = lat;
            this.lon[index] = lon;
            this.time[index] = time;
            this.course[index] = course;
            index++;
        }

        public void pack() {
            this.lat = Arrays.copyOf(this.lat, index);
            this.lon = Arrays.copyOf(this.lon, index);
            this.time = Arrays.copyOf(this.time, index);
            this.course = Arrays.copyOf(this.course, index);
        }

        public double[] getCourse() {
            return course;
        }

        public double[] getLat() {
            return lat;
        }

        public double[] getTime() {
            return time;
        }

        public double[] getLon() {
            return lon;
        }
    }
}

