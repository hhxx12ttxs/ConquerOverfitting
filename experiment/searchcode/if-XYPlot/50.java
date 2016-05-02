/*
 * Gasdroid 
 * Copyright (C) 2012  Andrea Antonello (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gasdroide;

import static java.lang.Math.abs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartRenderingInfo;
import org.afree.chart.ChartTouchEvent;
import org.afree.chart.ChartTouchListener;
import org.afree.chart.annotations.XYAnnotation;
import org.afree.chart.axis.ValueAxis;
import org.afree.chart.plot.Marker;
import org.afree.chart.plot.Plot;
import org.afree.chart.plot.PlotRenderingInfo;
import org.afree.chart.plot.ValueMarker;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.afree.data.xy.XYSeries;
import org.afree.data.xy.XYSeriesCollection;
import org.afree.graphics.SolidColor;
import org.afree.graphics.geom.RectShape;
import org.afree.ui.Layer;
import org.afree.ui.RectangleInsets;
import org.gasdroide.database.DaoData;
import org.gasdroide.database.Data;
import org.gasdroide.database.DatabaseManager;
import org.gasdroide.database.Dataset;
import org.gasdroide.device.AD6B11Device;
import org.gasdroide.device.DeviceUtilities;
import org.gasdroide.device.DummyValueDevice;
import org.gasdroide.device.IValueDevice;
import org.gasdroide.preferences.PreferencesActivity;
import org.gasdroide.utils.LeastSquaresInterpolator;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import eu.geopaparazzi.library.bluetooth.BluetoothManager;
import eu.geopaparazzi.library.bluetooth.IBluetoothIOHandler;
import eu.geopaparazzi.library.chart.XYChartView;
import eu.geopaparazzi.library.gps.GpsLocation;
import eu.geopaparazzi.library.gps.GpsManager;
import eu.geopaparazzi.library.util.DynamicDoubleArray;
import eu.geopaparazzi.library.util.FileUtilities;
import eu.geopaparazzi.library.util.LibraryConstants;
import eu.geopaparazzi.library.util.ResourcesManager;
import eu.geopaparazzi.library.util.TextRunnable;
import eu.geopaparazzi.library.util.Utilities;
import eu.geopaparazzi.library.util.debug.Debug;
import eu.geopaparazzi.library.util.debug.Logger;

/**
 * The main Gasdroide activity with the gui.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class GasdroidActivity extends Activity implements ChartTouchListener {

    private static final String ENABLE_CHART_SELECTION = "Enable Selection Mode";
    private static final String DISABLE_CHART_SELECTION = "Disable Selection Mode";
    private static final int MENU_TOGGLE_CHART_SELECTION = Menu.FIRST;
    private static final int MENU_SETTINGS = 2;
    private static final int MENU_BLUETOOTHPREFS = 3;
    private static final int MENU_EXPORT = 4;
    private static final int MENU_ABOUT = 5;

    private DynamicDoubleArray xDataList = new DynamicDoubleArray(100);
    private DynamicDoubleArray yDataList = new DynamicDoubleArray(100);
    private List<double[]> touchedPoints = new ArrayList<double[]>();

    private boolean isRecording = false;

    private TextView infoText;
    private XYChartView chartView;
    private AlertDialog loadDatasetDialog;
    private SimpleDateFormat formatter = LibraryConstants.TIME_FORMATTER;

    private List<Marker> markers = new ArrayList<Marker>();
    private XYAnnotation slopeAnnotation = null;
    private GpsManager gpsManager;
    private XYSeriesCollection datasetXY;
    private boolean isInSelectionMode = false;

    private IBluetoothIOHandler bluetoothDevice;

    private boolean useRaw;
    private String uomX;
    private String uomY;
    private TextView infoTitleTextView;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ResourcesManager.getInstance(this);

        // activate gps recording
        gpsManager = GpsManager.getInstance(this);

        /*
         * the buttons
         */
        final int recordButtonId = R.id.recordbutton;
        final ImageButton recordButton = (ImageButton) findViewById(recordButtonId);
        recordButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick( View v ) {
                if (!BluetoothManager.INSTANCE.isIOReady()) {
                    Utilities.messageDialog(GasdroidActivity.this, org.gasdroide.R.string.no_bluetooth_connection,
                            new Runnable(){
                                public void run() {
                                    /*
                                     * want to connect to the bt?
                                     */
                                    runOnUiThread(new Runnable(){
                                        public void run() {
                                            doDeviceSelection();
                                        }
                                    });
                                }
                            });
                } else {
                    if (BluetoothManager.INSTANCE.isIOReady()) {
                        if (!isRecording) {
                            isRecording = true;
                            Utilities.toast(GasdroidActivity.this, org.gasdroide.R.string.start_recording, Toast.LENGTH_SHORT);
                            recordButton.setImageResource(R.drawable.stoprecording);
                            resetData();

                            // start reading
                            new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        recordData();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            // start chart refreshing
                            new Thread(new Runnable(){
                                public void run() {
                                    try {
                                        chartData();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else {
                            isRecording = false;
                            Utilities.toast(GasdroidActivity.this, org.gasdroide.R.string.stop_recording, Toast.LENGTH_SHORT);
                            recordButton.setImageResource(R.drawable.record);
                            // connectedBluetoothDevice.close();
                        }
                    } else {
                        Utilities.messageDialog(GasdroidActivity.this, org.gasdroide.R.string.device_not_ready, null);
                    }
                }
            }

        });

        final int saveButtonId = R.id.savebutton;
        ImageButton saveButton = (ImageButton) findViewById(saveButtonId);
        saveButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick( View v ) {
                if (xDataList != null && xDataList.size() > 1) {

                    final double[] p1 = new double[]{Double.NaN, Double.NaN};
                    final double[] p2 = new double[]{Double.NaN, Double.NaN};
                    fillSlopePoints(p1, p2);
                    Utilities.inputMessageDialog(GasdroidActivity.this, getString(org.gasdroide.R.string.dataset_name),
                            getString(org.gasdroide.R.string.enter_dataset_title), getNewDatasetName(), new TextRunnable(){
                                public void run() {
                                    try {
                                        GpsLocation location = gpsManager.getLocation();
                                        Double lat = null;
                                        Double lon = null;
                                        Double altim = null;
                                        if (location != null) {
                                            lat = location.getLatitude();
                                            lon = location.getLongitude();
                                            altim = location.getAltitude();
                                        }

                                        double[] inP1 = Double.isNaN(p1[0]) ? null : p1;
                                        double[] inP2 = Double.isNaN(p2[0]) ? null : p2;
                                        DaoData.addDataset(GasdroidActivity.this, theTextToRunOn, lon, lat, altim, inP1, inP2,
                                                xDataList, yDataList);
                                        runOnUiThread(new Runnable(){
                                            public void run() {
                                                Toast.makeText(GasdroidActivity.this, org.gasdroide.R.string.dataset_saved,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        resetData();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                } else {
                    Utilities.messageDialog(GasdroidActivity.this, org.gasdroide.R.string.no_data_to_save_available, null);
                }
            }
        });

        final int loadButtonId = R.id.loadbutton;
        ImageButton loadButton = (ImageButton) findViewById(loadButtonId);
        loadButton.setOnClickListener(new Button.OnClickListener(){
            public void onClick( View v ) {
                resetData();
                loadData();
            }
        });

        chartView = (XYChartView) findViewById(R.id.chartview);
        chartView.addChartTouchListener(this);

        infoTitleTextView = (TextView) findViewById(R.id.infotextviewlabel);
        infoText = (TextView) findViewById(R.id.infotextview);

        try {
            DatabaseManager.getInstance().getDatabase(this);
        } catch (IOException e) {
            Logger.e(this, e.getLocalizedMessage(), e);
            e.printStackTrace();
            Utilities.toast(this, org.gasdroide.R.string.an_error_occurred_while_opening_the_database, Toast.LENGTH_LONG);
        }
    }

    private void resetData() {
        xDataList.clearForSameSizeReuse();
        yDataList.clearForSameSizeReuse();
        touchedPoints.clear();
        runOnUiThread(new Runnable(){
            public void run() {
                removeMarkers();
                chartView.setDataset(null, "", "", "");
                infoText.setText("");
            }
        });
    }

    private void recordData() throws IOException {
        if (bluetoothDevice == null) {
            runOnUiThread(new Runnable(){
                public void run() {
                    doDeviceSelection();
                }
            });
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        useRaw = preferences.getBoolean(DeviceUtilities.PREF_KEY_USERAW, true);

        IValueDevice valueDevice = (IValueDevice) bluetoothDevice;
        checkUOM();

        int samplingInterval = Integer.parseInt(preferences.getString(DeviceUtilities.PREF_KEY_SAMPLING_INTERVAL, "250")); //$NON-NLS-1$

        int maxReadingPerSecond = valueDevice.getMaxReadingPerSecond();
        double readingsPerSecond = 1000.0 / samplingInterval;
        double newSamplingInterval = samplingInterval;
        if (readingsPerSecond > maxReadingPerSecond) {
            readingsPerSecond = maxReadingPerSecond;
            newSamplingInterval = 1000.0 / readingsPerSecond;
            Utilities.toast(this, getString(org.gasdroide.R.string.sampling_interval_is_too_small) + (int) newSamplingInterval,
                    Toast.LENGTH_LONG);
        }

        long t1 = SystemClock.uptimeMillis();
        final long[] timeStamp = new long[1];
        while( isRecording ) {
            double value;
            if (useRaw) {
                value = valueDevice.getRawValue(timeStamp);
            } else {
                value = valueDevice.getValue(timeStamp);
            }
            if (Debug.D) {
                Logger.d(this, "RECORD: " + value); //$NON-NLS-1$
            }

            xDataList.add((float) (timeStamp[0] - t1));
            yDataList.add(value);

            SystemClock.sleep((long) newSamplingInterval);
        }

    }
    private void chartData() throws IOException {
        final String newDatasetName = getNewDatasetName();
        while( isRecording ) {
            runOnUiThread(new Runnable(){

                public void run() {
                    int size = xDataList.size();
                    if (size > 1) {
                        if (Debug.D) {
                            Logger.d(this, "CHART: " + xDataList.get(size - 1) + "/" + yDataList.get(size - 1)); //$NON-NLS-1$ //$NON-NLS-2$
                        }

                        checkUOM();

                        datasetXY = chartView.createDatasetFromXY(xDataList, yDataList, getString(org.gasdroide.R.string.data));
                        StringBuilder sbY = new StringBuilder();
                        sbY.append(getString(org.gasdroide.R.string.ylabel));
                        sbY.append(" [");
                        sbY.append(uomY);
                        sbY.append("]");
                        StringBuilder sbX = new StringBuilder();
                        sbX.append(getString(org.gasdroide.R.string.xlabel));
                        sbX.append(" [");
                        sbX.append(uomX);
                        sbX.append("]");
                        chartView.setDataset(datasetXY, null, sbX.toString(), sbY.toString());
                        chartView.invalidate();
                    }
                }
            });
            SystemClock.sleep(1000);
        }
    }

    private void checkUOM() {
        IValueDevice valueDevice = (IValueDevice) bluetoothDevice;
        if (useRaw) {
            uomX = valueDevice.getXRawValueUOM();
            uomY = valueDevice.getYRawValueUOM();
        } else {
            uomX = valueDevice.getXValueUOM();
            uomY = valueDevice.getYValueUOM();
        }
    }

    @Override
    public void finish() {
        if (BluetoothManager.INSTANCE.isEnabled()) {
            try {
                BluetoothManager.INSTANCE.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.finish();
    }

    public boolean onPrepareOptionsMenu( Menu menu ) {
        menu.clear();

        if (isInSelectionMode) {
            menu.add(Menu.NONE, MENU_TOGGLE_CHART_SELECTION, 0, DISABLE_CHART_SELECTION).setIcon(android.R.drawable.ic_menu_crop);
        } else {
            menu.add(Menu.NONE, MENU_TOGGLE_CHART_SELECTION, 0, ENABLE_CHART_SELECTION).setIcon(android.R.drawable.ic_menu_crop);
        }
        menu.add(Menu.NONE, MENU_SETTINGS, 1, R.string.preferences).setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(Menu.NONE, MENU_BLUETOOTHPREFS, 2, org.gasdroide.R.string.bluetooth_device_choice).setIcon(
                R.drawable.ic_volume_bluetooth_in_call);
        menu.add(Menu.NONE, MENU_EXPORT, 3, "Export to file").setIcon(android.R.drawable.ic_menu_save);
        menu.add(Menu.NONE, MENU_ABOUT, 4, "About").setIcon(android.R.drawable.ic_menu_info_details);

        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onMenuItemSelected( int featureId, MenuItem item ) {
        switch( item.getItemId() ) {
        case MENU_TOGGLE_CHART_SELECTION:
            isInSelectionMode = !isInSelectionMode;
            return true;
        case MENU_BLUETOOTHPREFS:
            runOnUiThread(new Runnable(){
                public void run() {
                    doDeviceSelection();
                }
            });
            return true;
        case MENU_SETTINGS:
            Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
            startActivity(preferencesIntent);
            return true;
        case MENU_EXPORT:
            try {
                exportData();
            } catch (IOException e) {
                e.printStackTrace();
                Utilities.messageDialog(GasdroidActivity.this, org.gasdroide.R.string.error_export, null);
            }
            return true;
        case MENU_ABOUT:
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);

            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void doDeviceSelection() {
        final List<String> devices = new ArrayList<String>();
        Set<BluetoothDevice> bondedDevices = BluetoothManager.INSTANCE.getBondedDevices();
        for( BluetoothDevice bluetoothDevice : bondedDevices ) {
            devices.add(bluetoothDevice.getName());
        }
        final String dummyDevice = "Dummy Device";
        devices.add(dummyDevice);
        String[] items = devices.toArray(new String[0]);

        new AlertDialog.Builder(this).setSingleChoiceItems(items, 0, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

                    public void onClick( DialogInterface dialog, int whichButton ) {
                        dialog.dismiss();
                        try {
                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            String deviceName = devices.get(selectedPosition);
                            if (deviceName.equals(dummyDevice)) {
                                bluetoothDevice = new DummyValueDevice();
                                BluetoothManager.INSTANCE.makeDummy();
                            } else {
                                BluetoothDevice realBluetoothDevice = BluetoothManager.INSTANCE
                                        .getBluetoothDeviceByName(deviceName);
                                if (realBluetoothDevice != null) {
                                    BluetoothManager.INSTANCE.setBluetoothDevice(realBluetoothDevice, true);
                                    bluetoothDevice = new AD6B11Device(GasdroidActivity.this);
                                    BluetoothManager.INSTANCE.initializeIBluetoothDeviceInternal(bluetoothDevice);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).show();
    }

    private void loadData() {
        try {
            final List<Dataset> datasetsList = DaoData.getDatasetsList(GasdroidActivity.this);
            if (datasetsList.size() < 1) {
                Utilities
                        .messageDialog(GasdroidActivity.this, org.gasdroide.R.string.no_datasets_available_in_the_database, null);
                return;
            }

            final String[] datasetArray = new String[datasetsList.size()];
            for( int i = 0; i < datasetArray.length; i++ ) {
                datasetArray[i] = datasetsList.get(i).getTitle();
            }

            final DialogInterface.OnMultiChoiceClickListener dialogListener = new DialogInterface.OnMultiChoiceClickListener(){

                @Override
                public void onClick( DialogInterface dialog, int which, boolean isChecked ) {
                    try {
                        final Dataset dataset = datasetsList.get(which);
                        double[] p1 = dataset.getP1();
                        double[] p2 = dataset.getP2();
                        if (p1 != null) {
                            touchedPoints.add(p1);
                        }
                        if (p2 != null) {
                            touchedPoints.add(p2);
                        }

                        Data data4Dataset = DaoData.getData4Dataset(GasdroidActivity.this, dataset);
                        xDataList = data4Dataset.getTimeArray();
                        yDataList = data4Dataset.getValuesArray();
                        loadDatasetDialog.dismiss();
                        if (xDataList.size() > 1) {
                            datasetXY = chartView.createDatasetFromXY(xDataList, yDataList,
                                    getString(org.gasdroide.R.string.data));
                            chartView.setDataset(datasetXY, dataset.getTitle(), getString(org.gasdroide.R.string.xlabel),
                                    getString(org.gasdroide.R.string.ylabel));

                            addMarkersFromSlopePoint();
                            AFreeChart chart = chartView.getChart();
                            Plot plot = chart.getPlot();
                            if (plot instanceof XYPlot) {
                                XYPlot xyPlot = (XYPlot) plot;
                                addSlopeLine(xyPlot);
                            }
                            chartView.invalidate();

                            printInfo(true);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            final boolean[] checkedValues = new boolean[datasetArray.length];
            for( int i = 0; i < datasetArray.length; i++ )
                checkedValues[i] = false;

            AlertDialog.Builder builder = new AlertDialog.Builder(GasdroidActivity.this);
            builder.setTitle(org.gasdroide.R.string.select_dataset_to_load);
            builder.setMultiChoiceItems(datasetArray, checkedValues, dialogListener);
            loadDatasetDialog = builder.create();
            loadDatasetDialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the info to the info view.
     * 
     * @param isLoaded if the dataset is loaded from db, which means that for now we do not know the UOM.
     */
    private void printInfo( boolean isLoaded ) {
        if (touchedPoints.size() > 0) {
            StringBuilder sb = new StringBuilder();
            if (touchedPoints.size() == 1) {
                // double[] p1 = touchedPoints.get(0);
                //                sb.append(getString(org.gasdroide.R.string.first_point)).append("\n"); //$NON-NLS-1$
                //                sb.append(getString(org.gasdroide.R.string.x)).append((float) p1[0]).append("\n"); //$NON-NLS-1$
                //                sb.append(getString(org.gasdroide.R.string.y)).append((float) p1[1]).append("\n"); //$NON-NLS-1$
            } else {

                if (bluetoothDevice instanceof IValueDevice) {
                    IValueDevice valueDevice = (IValueDevice) bluetoothDevice;
                    StringBuilder sbTitle = new StringBuilder();
                    sbTitle.append("dC/dt");
                    if (!isLoaded) {
                        sbTitle.append(" [");
                        if (useRaw) {
                            sbTitle.append(valueDevice.getYRawValueUOM());
                            sbTitle.append("/");
                            sbTitle.append(valueDevice.getXRawValueUOM().substring(1)); // TODO
                                                                                        // remove
                                                                                        // .substring(1)
                                                                                        // if using
                                                                                        // milliseconds
                        } else {
                            sbTitle.append(valueDevice.getYValueUOM());
                            sbTitle.append("/");
                            sbTitle.append(valueDevice.getXValueUOM().substring(1)); // TODO remove
                                                                                     // .substring(1)
                                                                                     // if using
                                                                                     // milliseconds
                        }
                        sbTitle.append("]");
                    }
                    infoTitleTextView.setText(sbTitle);
                }

                double[] p1 = touchedPoints.get(0);
                double[] p2 = touchedPoints.get(1);

                double[] slopeRegressionArray = extractSlopeRegression(p1, p2);

                sb.append(getString(org.gasdroide.R.string.slope)).append("\n").append(slopeRegressionArray[0]).append("\n"); //$NON-NLS-1$
                sb.append(getString(org.gasdroide.R.string.regression)).append("\n").append(slopeRegressionArray[1]).append("\n"); //$NON-NLS-1$

            }

            infoText.setText(sb.toString());
        }
    }

    private double[] extractSlopeRegression( double[] p1, double[] p2 ) {
        double[] slopeRegressionArray = new double[2];

        float firstX = (float) p1[0];
        float firstY = (float) p1[1];
        float secondX = (float) p2[0];
        float secondY = (float) p2[1];
        if (firstX > secondX) {
            // invert
            firstX = (float) p2[0];
            firstY = (float) p2[1];
            secondX = (float) p1[0];
            secondY = (float) p1[1];
        }

        //                sb.append(getString(org.gasdroide.R.string.first_point)).append("\n"); //$NON-NLS-1$
        //                sb.append(getString(org.gasdroide.R.string.x)).append(firstX).append("\n"); //$NON-NLS-1$
        //                sb.append(getString(org.gasdroide.R.string.y)).append(firstY).append("\n"); //$NON-NLS-1$
        //                sb.append(getString(org.gasdroide.R.string.second_point)).append("\n"); //$NON-NLS-1$
        //                sb.append(getString(org.gasdroide.R.string.x)).append(secondX).append("\n"); //$NON-NLS-1$
        //                sb.append(getString(org.gasdroide.R.string.y)).append(secondY).append("\n"); //$NON-NLS-1$
        // calc slope
        float slope = (secondY - firstY) / (secondX - firstX);
        float regressionSlope = calculateRegression(firstX, secondX);

        // TODO remove if using milliseconds
        slopeRegressionArray[0] = slope * 1000;
        slopeRegressionArray[1] = regressionSlope * 1000;
        // TODO remove if using milliseconds
        return slopeRegressionArray;
    }

    private float calculateRegression( float firstX, float secondX ) {
        /*
         * do the regression
         */
        List<Double> xList = new ArrayList<Double>();
        List<Double> yList = new ArrayList<Double>();
        for( int i = 0; i < xDataList.size(); i++ ) {
            double x = xDataList.get(i);
            if (x >= firstX && x <= secondX) {
                double y = yDataList.get(i);
                xList.add(x);
                yList.add(y);
            }
        }
        LeastSquaresInterpolator interpolator = new LeastSquaresInterpolator(xList, yList);
        float regressionSlope = (float) interpolator.getA1();
        return regressionSlope;
    }

    private void fillSlopePoints( double[] p1, double[] p2 ) {
        if (touchedPoints.size() == 0) {
            return;
        }
        double[] tmpP1 = touchedPoints.get(0);
        float firstX = (float) tmpP1[0];
        float firstY = (float) tmpP1[1];
        if (touchedPoints.size() == 2) {
            double[] tmpP2 = touchedPoints.get(1);
            float secondX = (float) tmpP2[0];
            float secondY = (float) tmpP2[1];
            if (firstX > secondX) {
                // invert
                firstX = (float) tmpP2[0];
                firstY = (float) tmpP2[1];
                secondX = (float) tmpP1[0];
                secondY = (float) tmpP1[1];
            }
            p2[0] = secondX;
            p2[1] = secondY;
        }
        p1[0] = firstX;
        p1[1] = firstY;
    }

    public void chartTouched( ChartTouchEvent event ) {
        if (isInSelectionMode) {
            AFreeChart chart = event.getChart();
            MotionEvent motionEvent = event.getTrigger();
            ChartRenderingInfo info = chartView.getChartRenderingInfo();

            int x = (int) (motionEvent.getX() / chartView.getScaleX());
            int y = (int) (motionEvent.getY() / chartView.getScaleY());

            Plot plot = chart.getPlot();
            if (plot instanceof XYPlot) {
                double xValue = -1;
                double yValue = -1;
                XYPlot xyPlot = (XYPlot) plot;
                PlotRenderingInfo plotInfo = info.getPlotInfo();
                RectShape dataArea = plotInfo.getDataArea();
                if (dataArea.contains(x, y)) {
                    ValueAxis xaxis = xyPlot.getDomainAxis();
                    if (xaxis != null) {
                        xValue = xaxis.java2DToValue(x, plotInfo.getDataArea(), xyPlot.getDomainAxisEdge());
                    }

                    int size = this.xDataList.size();
                    double[] xArray = xDataList.getInternalArray();
                    double[] yArray = yDataList.getInternalArray();
                    int index = -1;
                    for( int i = 0; i < size - 1; i++ ) {
                        if (xValue >= xArray[i] && xValue < xArray[i + 1]) {
                            index = abs(xValue - xArray[i]) < abs(xValue - xArray[i + 1]) ? i : i + 1;
                            // if (Debug.D)
                            // Logger.d(this, "Found " + xValue + " between " + xArray[i] + " and "
                            // + xArray[i + 1]);
                            break;
                        }
                    }
                    if (index < 0) {
                        return;
                    }
                    xValue = xArray[index];
                    yValue = yArray[index];
                }

                if (markers.size() == 2) {

                    double[] p1 = touchedPoints.get(0);
                    double[] p2 = touchedPoints.get(1);

                    int toRemove = abs(p1[0] - xValue) < abs(p2[0] - xValue) ? 0 : 1;

                    Marker remove = markers.remove(toRemove);
                    touchedPoints.remove(toRemove);
                    xyPlot.removeDomainMarker(remove, Layer.BACKGROUND);
                }
                Marker marker = new ValueMarker(xValue, R.color.green, 1.0f);
                marker.setLabelOffset(new RectangleInsets(2, 5, 2, 5));
                xyPlot.addDomainMarker(marker, Layer.BACKGROUND);
                markers.add(marker);
                touchedPoints.add(new double[]{xValue, yValue});

                if (slopeAnnotation != null) {
                    xyPlot.removeAnnotation(slopeAnnotation);
                    slopeAnnotation = null;
                }
                addSlopeLine(xyPlot);

                chartView.invalidate();
                printInfo(false);
            }
        }
    }

    private void addSlopeLine( XYPlot xyPlot ) {
        if (touchedPoints.size() == 2) {
            double[] p1 = touchedPoints.get(0);
            double[] p2 = touchedPoints.get(1);

            List< ? > series = datasetXY.getSeries();
            int seriesIndex = 1;
            if (series.size() > 1) {
                datasetXY.removeSeries(seriesIndex);
            }
            XYSeries xyS = new XYSeries("slope", true, true); //$NON-NLS-1$
            xyS.add(p1[0], p1[1]);
            xyS.add(p2[0], p2[1]);
            datasetXY.addSeries(xyS);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            xyPlot.setRenderer(seriesIndex, renderer);

            renderer.setSeriesPaintType(seriesIndex, new SolidColor(Color.BLUE));
            renderer.setSeriesStroke(seriesIndex, 5.0f);
        }
    }

    private void removeMarkers() {
        AFreeChart chart = chartView.getChart();
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            for( Marker marker : markers ) {
                xyPlot.removeDomainMarker(marker, Layer.BACKGROUND);
            }
            // chartView.invalidate();
            markers.clear();
        }
    }

    private void addMarkersFromSlopePoint() {
        AFreeChart chart = chartView.getChart();
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;

            for( double[] p : touchedPoints ) {
                Marker marker = new ValueMarker(p[0], R.color.green, 1.0f);
                marker.setLabelOffset(new RectangleInsets(2, 5, 2, 5));
                xyPlot.addDomainMarker(marker, Layer.BACKGROUND);
                markers.add(marker);
            }
        }
    }

    private String getNewDatasetName() {
        String uomAdd = "";
        if (uomX != null && uomY != null) {
            uomAdd = " x[" + uomX + "] y[" + uomY + "]";
        }
        return getString(org.gasdroide.R.string.new_dataset) + formatter.format(new Date()) + uomAdd;
    }

    private void exportData() throws IOException {
        final List<Dataset> datasetsList = DaoData.getDatasetsList(GasdroidActivity.this);
        if (datasetsList.size() < 1) {
            Utilities.messageDialog(GasdroidActivity.this, org.gasdroide.R.string.no_datasets_available_in_the_database, null);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID,TITLE,LON,LAT,SLOPE,REGRESSION\n");
        for( Dataset dataset : datasetsList ) {
            resetData();

            long id = dataset.getId();
            String title = dataset.getTitle();
            double lon = dataset.getLon();
            double lat = dataset.getLat();

            double[] p1 = dataset.getP1();
            double[] p2 = dataset.getP2();
            if (p1 == null || p2 == null) {
                continue;
            }

            Data data4Dataset = DaoData.getData4Dataset(GasdroidActivity.this, dataset);
            xDataList = data4Dataset.getTimeArray();
            yDataList = data4Dataset.getValuesArray();

            double[] slopeRegression = extractSlopeRegression(p1, p2);

            sb.append(id).append(",");
            sb.append(title).append(",");
            sb.append(lon).append(",");
            sb.append(lat).append(",");
            sb.append(slopeRegression[0]).append(",");
            sb.append(slopeRegression[1]).append("\n");
        }

        ResourcesManager resourcesManager = ResourcesManager.getInstance(this);
        File applicationDir = resourcesManager.getApplicationDir();
        File exportFile = new File(applicationDir, "gasdroide_export.csv");
        FileUtilities.writefile(sb.toString(), exportFile);

    }
}
