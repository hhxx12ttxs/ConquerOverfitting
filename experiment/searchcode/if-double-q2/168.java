package uk.ac.ebi.pride.chart.graphics.implementation.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.json.JSONException;
import uk.ac.ebi.pride.chart.controller.PMDQuartilesFileReader;
import uk.ac.ebi.pride.chart.graphics.implementation.PrideChart;
import uk.ac.ebi.pride.chart.graphics.implementation.data.*;
import uk.ac.ebi.pride.chart.graphics.interfaces.PrideChartSpectraOptions;
import uk.ac.ebi.pride.chart.graphics.interfaces.PrideChartQuartiles;
import uk.ac.ebi.pride.chart.model.implementation.ExperimentSummaryData;
import uk.ac.ebi.pride.chart.model.implementation.MSDataArray;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>From a spectral chartData structure of a PRIDE experiment and a file of theoretical chartData, create a chart comparing the distribution of precursor masses of a pride experiment and the theoretical distribution.</p>
 *
 * @author Antonio Fabregat
 * Date: 15-jul-2010
 * Time: 11:24:46
 */
public class PreMSvsTheMSChartSpectra extends PrideChart implements PrideChartSpectraOptions, PrideChartQuartiles {
    /**
     * Defines the width of the bars in order to calculate the frequency of each mass range
     */
    private static final double BAR_WIDTH = 100; //90;

    /**
     * <p> Creates an instance of this PreMSvsTheMSChartSpectra object, setting all fields as per description below.</p>
     *
     * @param summaryData spectral chartData structure containing all Spectrums of a PRIDE experiment
     */
    public PreMSvsTheMSChartSpectra(ExperimentSummaryData summaryData) {
        super(summaryData);
    }

    /**
     * <p> Creates an instance of this PreMSvsTheMSChartSpectra object, setting all fields as per description below.</p>
     *
     * @param jsonData a Pride Chart Json Data object containing the chart values
     */
    public PreMSvsTheMSChartSpectra(String jsonData) {
        super(jsonData);
    }

    @Override
    protected boolean isDataConsistent(ExperimentSummaryData summaryData) {
        if (!summaryData.getState().isPrecursorChargesLoaded())
            recordError("The experiment does not contain precursor charge values");

        if (!summaryData.getState().isPrecursorMassesLoaded())
            recordError("The experiment does not contain precursor mass values");

        return isValid();
    }

    @Override
    protected void initializeTypes() {
        this.supportedTypes.add(DataSeriesType.ALL_SPECTRA);
        this.supportedTypes.add(DataSeriesType.IDENTIFIED_SPECTRA);
        this.supportedTypes.add(DataSeriesType.UNIDENTIFIED_SPECTRA);

        this.visibleTypes.add(DataSeriesType.ALL_SPECTRA);
    }

    @Override
    protected void initializeQuartiles(){
        this.supportedQuartiles.add(QuartilesType.NONE);
        this.supportedQuartiles.add(QuartilesType.PRIDE);
        this.supportedQuartiles.add(QuartilesType.HUMAN);
        this.supportedQuartiles.add(QuartilesType.MOUSE);

        this.visibleQuartiles = QuartilesType.NONE;
    }

    /**
     * Private processor of the chartData of the masses loaded with DBAccessController in the constructor
     */
    @Override
    protected void processData(ExperimentSummaryData summaryData) {
        MSDataArray msIdentifiedArrayExp = new MSDataArray(summaryData.getMassVecExp(true));
        int idBars;
        try{
            idBars = (int) Math.ceil(msIdentifiedArrayExp.getMaxValue() / BAR_WIDTH);
        }catch(Exception e){
            idBars = 0;
        }

        MSDataArray msUnidentifiedArrayExp = new MSDataArray(summaryData.getMassVecExp(false));
        int unidBars;
        try{
            unidBars = (int) Math.ceil(msUnidentifiedArrayExp.getMaxValue() / BAR_WIDTH);
        }catch(Exception e){
            unidBars = 0;
        }

        //ToDo: find a better way for knowing when there is not precursor mass chartData in the experiment
        //This is 1 because there is not precursor mass chartData in the experiment
        if (idBars <= 1 && unidBars <=1) {
            recordError("The experiment does not contain precursor mass data");
            intermediateData = new IntermediateData(getErrorMessages());
            return;
        }

        double[] idExpData = new double[idBars];
        double[] unidExpData = new double[unidBars];

        float idTotalFreqExpData = 0;
        for (int j = 0; j < msIdentifiedArrayExp.size(); j++) {
            double value = msIdentifiedArrayExp.getAt(j);
            int range = (int) Math.floor((value) / BAR_WIDTH);
            idExpData[range]++;
            idTotalFreqExpData++;
        }

        float unidTotalFreqExpData = 0;
        for (int j = 0; j < msUnidentifiedArrayExp.size(); j++) {
            double value = msUnidentifiedArrayExp.getAt(j);
            int range = (int) Math.floor((value) / BAR_WIDTH);
            unidExpData[range]++;
            unidTotalFreqExpData++;
        }

        List<SeriesPair<Double,Double>> identified = new ArrayList<SeriesPair<Double,Double>>();
        for (int j = 0; j < idExpData.length; j++) {
            //Normalization of the frequencies
            double key = j * BAR_WIDTH;
            double value = idExpData[j] / idTotalFreqExpData;
            identified.add(new SeriesPair<Double, Double>(key, value));
        }

        List<SeriesPair<Double,Double>> unidentified = new ArrayList<SeriesPair<Double,Double>>();
        for (int j = 0; j < unidExpData.length; j++) {
            //Normalization of the frequencies
            double key = j * BAR_WIDTH;
            double value = unidExpData[j] / unidTotalFreqExpData;
            unidentified.add(new SeriesPair<Double, Double>(key, value));
        }

        intermediateData = new IntermediateData();
        DataSeries idSeries =
                new DataSeries<Double, Double>(
                        DataSeriesType.IDENTIFIED_SPECTRA,
                        DataSeriesType.IDENTIFIED_SPECTRA.getType(),
                        identified);
        intermediateData.addPrideChartSerie(idSeries);

        DataSeries unidSeries =
                new DataSeries<Double, Double>(
                        DataSeriesType.UNIDENTIFIED_SPECTRA,
                        DataSeriesType.UNIDENTIFIED_SPECTRA.getType(),
                        unidentified);
        intermediateData.addPrideChartSerie(unidSeries);

        try {
            intermediateData.setVariable("idenFreq", idTotalFreqExpData);
            intermediateData.setVariable("unidenFreq", unidTotalFreqExpData);
        } catch (JSONException e) {/*Nothing here*/}
    }

    private DataSeries<Double, Double> getAllSpectraSeries(List<DataSeries<Double,Double>> seriesList,
                                                           double idenFreq, double unidenFreq){
        DataSeries<Double,Double> identified = seriesList.get(0);
        DataSeries<Double,Double> unidentified = seriesList.get(1);

        List<SeriesPair<Double,Double>> idenSeries = identified.getSeriesValues(Double.class, Double.class);
        List<SeriesPair<Double,Double>> unidenSeries = unidentified.getSeriesValues(Double.class, Double.class);

        double max = Math.max(idenSeries.size(), unidenSeries.size());
        List<SeriesPair<Double,Double>> allSpectraSeries = new ArrayList<SeriesPair<Double,Double>>();
        for (int j = 0; j < max; j++) {
            double iden = j < idenSeries.size() ? idenSeries.get(j).getY() : 0.0;
            double uniden = j < unidenSeries.size() ? unidenSeries.get(j).getY() : 0.0;

            double key = j < idenSeries.size() ?  idenSeries.get(j).getX() : unidenSeries.get(j).getX();
            double value = (iden * idenFreq + uniden * unidenFreq) / (idenFreq + unidenFreq);
            allSpectraSeries.add(new SeriesPair<Double, Double>(key, value));
        }

        return new DataSeries<Double, Double>(
                        DataSeriesType.ALL_SPECTRA,
                        DataSeriesType.ALL_SPECTRA.getType(),
                        allSpectraSeries);
    }

    private List<XYSeries> getTheoreticalSeries(int bars, String reference, String fileName){
        if(fileName==null || fileName.isEmpty()) return new ArrayList<XYSeries>();
        
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName);
        PMDQuartilesFileReader pmdQuartiles = new PMDQuartilesFileReader(is);

        List<XYSeries> series = new ArrayList<XYSeries>();
        XYSeries q1 = new XYSeries("Quartiles"); //Q1
        XYSeries q2 = new XYSeries(reference); //Q2
        XYSeries q3 = new XYSeries("Quartiles"); //Q3
        for (int j = 0; j < bars; j++) {
            double key = j * BAR_WIDTH;
            try{
                q1.add(key, pmdQuartiles.getQ1Values().get(j));
                q2.add(key, pmdQuartiles.getQ2Values().get(j));
                q3.add(key, pmdQuartiles.getQ3Values().get(j));
            }catch(IndexOutOfBoundsException e){
                q1.add(key, 0.0);
                q2.add(key, 0.0);
                q3.add(key, 0.0);
            }
        }
        series.add(q1);
        series.add(q2);
        series.add(q3);

        try {
            is.close();
        } catch (IOException e) {
            recordError(e.getMessage());
        }

        return series;
    }

    /**
     * Private method to create the chart taking the chartData returned by processMasses method
     */
    @Override
    protected void setChart() {
        //Necessary because the y axis upper bound was not being correctly set automatically
        double yAxisUpperBound = 0.0;
        
        SeriesManager<Double,Double> sf = new SeriesManager<Double,Double>(intermediateData);
        List<DataSeries<Double,Double>> seriesList = sf.getSeries();

        //Adds the 'All Spectra' series at the end in the local variable 'seriesList'
        //**NOTE** The intermediateData is NOT modified
        double idenFreq = intermediateData.getDouble("idenFreq");
        double unidenFreq = intermediateData.getDouble("unidenFreq");
        seriesList.add(getAllSpectraSeries(seriesList, idenFreq, unidenFreq));

        int bars = 0;
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (DataSeries<Double, Double> series : seriesList) {
            DataSeriesType seriesType = series.getType();
            if(!visibleTypes.contains(seriesType)) continue;

            List<SeriesPair<Double, Double>> values = series.getSeriesValues(Double.class, Double.class);
            XYSeries expSerie = new XYSeries(series.getIdentifier());
            for (SeriesPair<Double, Double> value : values) {
                yAxisUpperBound = Math.max(yAxisUpperBound, value.getY());
                expSerie.add(value.getX(), value.getY());
            }
            dataset.addSeries(expSerie);
            bars = Math.max(bars, values.size());
        }

        QuartilesType quartilesType = getVisibleQuartile();
        String fileName = quartilesType.getFileName();
        if (fileName!=null) {    
            for (XYSeries xySeries : getTheoreticalSeries(bars, quartilesType.getType(), fileName)) {
                yAxisUpperBound = Math.max(yAxisUpperBound, xySeries.getMaxY());
                dataset.addSeries(xySeries);
            }
        }

        chart = null;
        chart = ChartFactory.createXYLineChart(
                getChartTitle(),            // chart title
                "Mass (Daltons)",           // x axis label
                "Relative Frequency",       // y axis label
                dataset,                    // chartData
                PlotOrientation.VERTICAL,
                true,                       // include legend
                true,                       // tooltips
                false                       // urls
        );
        chart.addSubtitle(new TextTitle(getChartSubTitle()));

        XYSplineRenderer sr = new XYSplineRenderer();
        chart.getXYPlot().setRenderer(sr);

        XYPlot plot = (XYPlot) chart.getPlot();
        //SETTING THE UPPER BOUND AND MARGIN MANUALLY
        plot.getRangeAxis().setUpperBound(yAxisUpperBound * ( 1 + CHART_UPPER_MARGIN/2 ));
        plot.setBackgroundAlpha(0f);
        plot.setDomainGridlinePaint(Color.red);
        plot.setRangeGridlinePaint(Color.blue);

        int series = dataset.getSeries().size();
        if(fileName!=null){
            plot.getRenderer().setSeriesPaint(series-3, quartilesType.getBoundsColor());
            plot.getRenderer().setSeriesPaint(series-2, quartilesType.getMiddleColor());
            plot.getRenderer().setSeriesPaint(series-1, quartilesType.getBoundsColor());
        }


        for(int i=0; i<series; i++){
            sr.setSeriesShapesVisible(i, false);
            sr.setSeriesVisibleInLegend(i, true);
        }

        if( series > 3 )
            sr.setSeriesVisibleInLegend(series-3, false);
    }

    @Override
    public String getChartTitle() {
        return "Distribution of Precursor Ion Masses";
    }

    @Override
    public String getChartShortTitle() {
        return "Precursor Ion Masses";
    }

    @Override
    public String getChartSubTitle() {
        return "per PRIDE experiment";
    }

    @Override
    public void setSpectraTypeVisibility(DataSeriesType type, boolean visible) {
        super.setTypeVisibility(type, visible);
    }

    @Override
    public boolean isSpectraMultipleChoice() {
        return false;
    }

    @Override
    public boolean isSpectraSeriesEmpty(DataSeriesType type) {
        SeriesManager<Double,Double> sf = new SeriesManager<Double,Double>(intermediateData);
        List<DataSeries<Double,Double>> seriesList = sf.getSeries();

        //Adds the 'All Spectra' series at the end in the local variable 'seriesList'
        //**NOTE** The intermediateData is NOT modified
        double idenFreq = intermediateData.getDouble("idenFreq");
        double unidenFreq = intermediateData.getDouble("unidenFreq");
        seriesList.add(getAllSpectraSeries(seriesList, idenFreq, unidenFreq));

        for (DataSeries<Double,Double> series : seriesList) {
            if(series.getType()==type)
                return series.isEmpty(Double.class, Double.class);
        }
        return true;
    }

    @Override
    public void setQuartileVisibility(QuartilesType type, boolean visible) {
        super.setQuartileVisibility(type, visible);
    }

    @Override
    public boolean isMultipleQuartile() {
        return false;
    }

    @Override
    public boolean isQuartileEmpty(QuartilesType type) {
        return false;
    }
}
