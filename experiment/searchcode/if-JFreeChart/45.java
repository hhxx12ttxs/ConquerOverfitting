package app;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.CategoryTableXYDataset;


public class Main {
	
	 public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException{
		 
		 CmdLineParser parser = new CmdLineParser();
		 Option scdWeightOpt = parser.addDoubleOption("scd");
		 Option cldWeightOpt = parser.addDoubleOption("cld");
		 Option ehdWeightOpt = parser.addDoubleOption("ehd");
		 Option numResultsOpt = parser.addDoubleOption("nRes");
		 Option minResFilterOpt = parser.addDoubleOption("nFilter");
		 Option task1Opt = parser.addBooleanOption("task1");
		 Option task2Opt = parser.addBooleanOption("task2");
		 Option indexOption = parser.addBooleanOption("createIndex");
		 
		 try {
			parser.parse(args);
		 }catch (IllegalOptionValueException e) {
			printUsage();
			System.exit(-1);
		 }catch (UnknownOptionException e) {
			printUsage();
			System.exit(-1);
		 }
		 
		 int numberOfResults = (Integer) parser.getOptionValue(numResultsOpt, 25);
		 int filter = (Integer) parser.getOptionValue(minResFilterOpt, 0);
		 
		 if(parser.getOptionValue(indexOption)!=null){
			 Indexing indexing = new Indexing("index", "ucid.v2-png");
			 indexing.createIndex();
		 }
		 
		 if(parser.getOptionValue(task1Opt)!=null){
			 System.out.println("Task1");
			 getPerformanceTask1(1f, 1f, 1f, numberOfResults, filter);
			 getPerformanceTask1(1f, 1f, 0f, numberOfResults, filter);
			 getPerformanceTask1(0f, 0f, 1f, numberOfResults, filter);
			 getPerformanceTask1(1f, 1f, 0.5f, numberOfResults, filter);
			 getPerformanceTask1(0.25f, 0.25f, 1f, numberOfResults, filter);
		 }else if(parser.getOptionValue(task2Opt)!=null){
			 System.out.println("Task2");
			 getPerformanceTask2(numberOfResults, filter);			 
		 }else{
			 System.out.println("Task1 custom");
			 float scdWeight = (Float) parser.getOptionValue(scdWeightOpt,1f);
			 float cldWeight = (Float) parser.getOptionValue(cldWeightOpt,1f);
			 float ehdWeight = (Float) parser.getOptionValue(ehdWeightOpt,1f);
			 getPerformanceTask1(scdWeight, cldWeight, ehdWeight, numberOfResults, filter);
		 }
		 
		 
	 }

	private static void printUsage() {
		// TODO Auto-generated method stub
		
	}

	private static void getPerformanceTask1(float weightSCD, float weightCLD, float weightEHD, int numberOfResults, int filter) throws CorruptIndexException, IOException {
		Search search = new Search(weightSCD, weightCLD, weightEHD, numberOfResults+1);
		GTFParser parser = new GTFParser();
		Map<String, List<String>> gtf = parser.parseAndFilterGroundTruthFile(filter);
		double[][] precisionValues = new double[gtf.entrySet().size()][numberOfResults];
		double[][] recallValues = new double[gtf.entrySet().size()][numberOfResults];
		double[][] averagePrecisionValues = new double[gtf.entrySet().size()][numberOfResults];
		int queryIndex = 0;
		for(Entry<String, List<String>> entry : gtf.entrySet()){
			List<SearchResult> currentResult = search.search(new File("ucid.v2-png/" + entry.getKey()));
			Stats stats = new Stats(currentResult, entry.getValue());
			for(int k=1; k<=numberOfResults; k++){
				precisionValues[queryIndex][k-1] = stats.getPrecision(k);
				recallValues[queryIndex][k-1] = stats.getRecall(k);
				averagePrecisionValues[queryIndex][k-1] = stats.getAveragePrecision(k);
			}
			queryIndex++;
			System.out.println(queryIndex);
		}
		
		CategoryTableXYDataset meanPrecisionDataset = new CategoryTableXYDataset();
		
		for(int k=0; k<precisionValues[0].length; k++){
			double precisionSum = 0;
			double recallSum = 0;
			double averagePrecisionSum = 0;
			for(int j=0; j<precisionValues.length; j++){
				precisionSum += precisionValues[j][k];
				recallSum += recallValues[j][k];
				averagePrecisionSum += averagePrecisionValues[j][k];
			}
			meanPrecisionDataset.add(k+1, precisionSum/(double)precisionValues.length, "MeanPrecision");
			meanPrecisionDataset.add(k+1, recallSum/(double)recallValues.length, "MeanRecall");
			meanPrecisionDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValues.length, "MeanAveragePrecision");
		}
		
		JFreeChart meanPrecisionChart = ChartFactory.createXYLineChart(null, "K", "Measures", meanPrecisionDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(meanPrecisionChart, numberOfResults);
		//showChart(meanPrecisionChart);
		ImageIO.write(meanPrecisionChart.createBufferedImage(600, 450), "png", new File("charts/Task1_"+weightSCD+"-"+weightCLD+"-"+weightEHD+".png"));
	}
	
	private static void showChart(JFreeChart chart){
		ChartFrame meanPrecisionChartFrame = new ChartFrame("Precision-Recall Chart", chart);
		meanPrecisionChartFrame.setBounds(0, 0, 500, 500);
		meanPrecisionChartFrame.setVisible(true);
		meanPrecisionChartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private static void setChartProperties(JFreeChart chart, int numberOfResults){
		chart.getXYPlot().getDomainAxis().setRange(1, numberOfResults);
		chart.getXYPlot().getRangeAxis().setRange(0, 0.7001);
		chart.getXYPlot().getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		TickUnits tickUnits = new TickUnits();
		tickUnits.add(new NumberTickUnit(0.1d));
		chart.getXYPlot().getRangeAxis().setStandardTickUnits(tickUnits);
		chart.getXYPlot().getDomainAxis().setTickLabelFont(new Font("sansserif",Font.BOLD,25));
		chart.getXYPlot().getRangeAxis().setTickLabelFont(new Font("sansserif",Font.BOLD,25));
		chart.getXYPlot().getDomainAxis().setLabelFont(new Font("sansserif",Font.BOLD,25));
		chart.getXYPlot().getRangeAxis().setLabelFont(new Font("sansserif",Font.BOLD,25));
		chart.getLegend().setItemFont(new Font("sansserif",Font.ITALIC,18));
		
		chart.getXYPlot().setBackgroundPaint(Color.white);
		chart.getXYPlot().setDomainGridlinePaint(Color.lightGray);
		chart.getXYPlot().setRangeGridlinePaint(Color.lightGray);
		chart.getXYPlot().setOutlineStroke(new BasicStroke(0f));
		chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(3));
		chart.getXYPlot().getRenderer().setSeriesStroke(1, new BasicStroke(3));
		chart.getXYPlot().getRenderer().setSeriesStroke(2, new BasicStroke(3));
	}
	
	private static void getPerformanceTask2(int numberOfResults, int filter) throws CorruptIndexException, IOException{
		GTFParser parser = new GTFParser();
		Map<String, List<String>> gtf = parser.parseAndFilterGroundTruthFile(filter);
		
		Search searchSCD = new Search(1f, 0f, 0f, numberOfResults+1);
		Search searchCLD = new Search(0f, 1f, 0f, numberOfResults+1);
		Search searchEHD = new Search(0f, 0f, 1f, numberOfResults+1);
		
		CategoryTableXYDataset bordaDataset = new CategoryTableXYDataset();
		CategoryTableXYDataset rankProductDataset = new CategoryTableXYDataset();
		CategoryTableXYDataset invertedRankPositionDataset = new CategoryTableXYDataset();
		
		CategoryTableXYDataset precisionDataset = new CategoryTableXYDataset();
		CategoryTableXYDataset recallDataset = new CategoryTableXYDataset();
		CategoryTableXYDataset averagePrecisionDataset = new CategoryTableXYDataset();
		
		
		double[][] precisionValuesBORDA = new double[gtf.entrySet().size()][numberOfResults];
		double[][] recallValuesBORDA = new double[gtf.entrySet().size()][numberOfResults];
		double[][] averagePrecisionValuesBORDA = new double[gtf.entrySet().size()][numberOfResults];
		
		double[][] precisionValuesRankProduct = new double[gtf.entrySet().size()][numberOfResults];
		double[][] recallValuesRankProduct = new double[gtf.entrySet().size()][numberOfResults];
		double[][] averagePrecisionValuesRankProduct = new double[gtf.entrySet().size()][numberOfResults];
		
		double[][] precisionValuesInvertedRankPosition = new double[gtf.entrySet().size()][numberOfResults];
		double[][] recallValuesInvertedRankPosition = new double[gtf.entrySet().size()][numberOfResults];
		double[][] averagePrecisionValuesInvertedRankPosition = new double[gtf.entrySet().size()][numberOfResults];
		
		int queryIndex=0;
		for(Entry<String, List<String>> entry : gtf.entrySet()){
			String query = entry.getKey();
			
			List<SearchResult> currentResultSCD = searchSCD.search(new File("ucid.v2-png/" + query));
			List<SearchResult> currentResultCLD = searchCLD.search(new File("ucid.v2-png/" + query));
			List<SearchResult> currentResultEHD = searchEHD.search(new File("ucid.v2-png/" + query));

			RankFusion rankFusion = new RankFusion(currentResultSCD, currentResultCLD, currentResultEHD);
			List<SearchResult> mergedWithBorda = rankFusion.mergeWithBORDACount();
			Stats statsBORDA = new Stats(mergedWithBorda, entry.getValue());
			List<SearchResult> mergedWithRankProduct = rankFusion.mergeWithRankProduct();
			Stats statsRankProduct = new Stats(mergedWithRankProduct, entry.getValue());
			List<SearchResult> mergedWithInvertedRankPosition = rankFusion.mergeWithInvertedRankPosition();
			Stats statsInvertedRankPosition = new Stats(mergedWithInvertedRankPosition, entry.getValue());
			
			for(int k=1; k<=numberOfResults; k++){
				precisionValuesBORDA[queryIndex][k-1] = statsBORDA.getPrecision(k);
				recallValuesBORDA[queryIndex][k-1] = statsBORDA.getRecall(k);
				averagePrecisionValuesBORDA[queryIndex][k-1] = statsBORDA.getAveragePrecision(k);
				precisionValuesRankProduct[queryIndex][k-1] = statsRankProduct.getPrecision(k);
				recallValuesRankProduct[queryIndex][k-1] = statsRankProduct.getRecall(k);
				averagePrecisionValuesRankProduct[queryIndex][k-1] = statsRankProduct.getAveragePrecision(k);
				precisionValuesInvertedRankPosition[queryIndex][k-1] = statsInvertedRankPosition.getPrecision(k);
				recallValuesInvertedRankPosition[queryIndex][k-1] = statsInvertedRankPosition.getRecall(k);
				averagePrecisionValuesInvertedRankPosition[queryIndex][k-1] = statsInvertedRankPosition.getAveragePrecision(k);
			}
			
			queryIndex++;
			System.out.println(queryIndex);
		}
		
		for(int k=0; k<precisionValuesBORDA[0].length; k++){
			double precisionSum = 0;
			double recallSum = 0;
			double averagePrecisionSum = 0;
			for(int j=0; j<precisionValuesBORDA.length; j++){
				precisionSum += precisionValuesBORDA[j][k];
				recallSum += recallValuesBORDA[j][k];
				averagePrecisionSum += averagePrecisionValuesBORDA[j][k];
			}
			bordaDataset.add(k+1, precisionSum/(double)precisionValuesBORDA.length, "MeanPrecision");
			bordaDataset.add(k+1, recallSum/(double)recallValuesBORDA.length, "MeanRecall");
			bordaDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValuesBORDA.length, "MeanAveragePrecision");
			precisionDataset.add(k+1, precisionSum/(double)precisionValuesBORDA.length, "BORDAPrecision");
			recallDataset.add(k+1, recallSum/(double)recallValuesBORDA.length, "BORDARecall");
			averagePrecisionDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValuesBORDA.length, "BORDAAveragePrecision");
		}
		
		for(int k=0; k<precisionValuesRankProduct[0].length; k++){
			double precisionSum = 0;
			double recallSum = 0;
			double averagePrecisionSum = 0;
			for(int j=0; j<precisionValuesRankProduct.length; j++){
				precisionSum += precisionValuesRankProduct[j][k];
				recallSum += recallValuesRankProduct[j][k];
				averagePrecisionSum += averagePrecisionValuesRankProduct[j][k];
			}
			rankProductDataset.add(k+1, precisionSum/(double)precisionValuesRankProduct.length, "MeanPrecision");
			rankProductDataset.add(k+1, recallSum/(double)recallValuesRankProduct.length, "MeanRecall");
			rankProductDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValuesRankProduct.length, "MeanAveragePrecision");
			precisionDataset.add(k+1, precisionSum/(double)precisionValuesRankProduct.length, "RankProductPrecision");
			recallDataset.add(k+1, recallSum/(double)recallValuesRankProduct.length, "RankProductRecall");
			averagePrecisionDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValuesRankProduct.length, "RankProductAveragePrecision");
		}
		
		for(int k=0; k<precisionValuesInvertedRankPosition[0].length; k++){
			double precisionSum = 0;
			double recallSum = 0;
			double averagePrecisionSum = 0;
			for(int j=0; j<precisionValuesInvertedRankPosition.length; j++){
				precisionSum += precisionValuesInvertedRankPosition[j][k];
				recallSum += recallValuesInvertedRankPosition[j][k];
				averagePrecisionSum += averagePrecisionValuesInvertedRankPosition[j][k];
			}
			invertedRankPositionDataset.add(k+1, precisionSum/(double)precisionValuesInvertedRankPosition.length, "MeanPrecision");
			invertedRankPositionDataset.add(k+1, recallSum/(double)recallValuesInvertedRankPosition.length, "MeanRecall");
			invertedRankPositionDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValuesInvertedRankPosition.length, "MeanAveragePrecision");
			precisionDataset.add(k+1, precisionSum/(double)precisionValuesInvertedRankPosition.length, "InvertedRankPositionPrecision");
			recallDataset.add(k+1, recallSum/(double)recallValuesInvertedRankPosition.length, "InvertedRankPositionRecall");
			averagePrecisionDataset.add(k+1, averagePrecisionSum/(double)averagePrecisionValuesInvertedRankPosition.length, "InvertedRankPositionAveragePrecision");
		}
		
		JFreeChart bordaChart = ChartFactory.createXYLineChart(null, "K", "Measures", bordaDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(bordaChart, numberOfResults);
		//showChart(bordaChart);
		ImageIO.write(bordaChart.createBufferedImage(600, 450), "png", new File("charts/Task2_BORDA.png"));

		JFreeChart rankProductChart = ChartFactory.createXYLineChart(null, "K", "Measures", rankProductDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(rankProductChart, numberOfResults);
		//showChart(rankProductChart);
		ImageIO.write(rankProductChart.createBufferedImage(600, 450), "png", new File("charts/Task2_RankProduct.png"));
		
		JFreeChart invertedRankPositionChart = ChartFactory.createXYLineChart(null, "K", "Measures", invertedRankPositionDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(invertedRankPositionChart, numberOfResults);
		//showChart(invertedRankPositionChart);
		ImageIO.write(invertedRankPositionChart.createBufferedImage(600, 450), "png", new File("charts/Task2_InvertedRankPosition.png"));
		
		JFreeChart precisionChart = ChartFactory.createXYLineChart(null, "K", "Precision", precisionDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(precisionChart, numberOfResults);
		//showChart(precisionChart);
		ImageIO.write(precisionChart.createBufferedImage(600, 450), "png", new File("charts/Task2_Precision.png"));
		
		JFreeChart recallChart = ChartFactory.createXYLineChart(null, "K", "Recall", recallDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(recallChart, numberOfResults);
		//showChart(recallChart);
		ImageIO.write(recallChart.createBufferedImage(600, 450), "png", new File("charts/Task2_Recall.png"));
		
		JFreeChart averagePrecisionChart = ChartFactory.createXYLineChart(null, "K", "Average Precision", averagePrecisionDataset, PlotOrientation.VERTICAL, true, true, false);
		setChartProperties(averagePrecisionChart, numberOfResults);
		//showChart(averagePrecisionChart);
		ImageIO.write(averagePrecisionChart.createBufferedImage(600, 450), "png", new File("charts/Task2_AveragePrecision.png"));
		
	}
}

