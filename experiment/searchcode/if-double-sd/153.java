package org.cremag.utils.file.genomic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.log4j.Logger;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BigWigIterator;
import org.broad.igv.bbfile.WigItem;
import org.cremag.genomeStore.QuerySet;
import org.cremag.genomic.BedItem;
import org.cremag.genomic.Coverage;
import org.cremag.utils.GenomeStoreFileUtils;
import org.cremag.utils.plot.ColorBrewer;
import org.cremag.utils.plot.PlotProperties;
import org.cremag.utils.stats.StatisticalResult;

public class BigWig extends GenomicTrack {
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(BigWig.class);
	
	BBFileReader reader;
	
	public BigWig (BBFileReader reader) {
		this.id = GenomeStoreFileUtils.getFileName(reader.getBBFilePath());
		this.reader = reader;
		this.trackType = TrackType.BIGWIG;
	}
	
	public Coverage getCoverage(BedItem bedItem) {
		Coverage coverage = new Coverage(bedItem);
		
		try {
		BigWigIterator bigWigIterator = reader.getBigWigIterator(bedItem.getChromosome(), bedItem.getStart(), 
				bedItem.getChromosome(), bedItem.getEnd(), false);
		
		while(bigWigIterator.hasNext()) {
			try {
				WigItem item = bigWigIterator.next();
				for(int i = item.getStartBase(); i < item.getEndBase(); i++)
					if(i >= bedItem.getStart() && i < bedItem.getEnd())
						coverage.put(i, item.getWigValue());
			} catch (Exception e) {logger.warn("Coverage compute warning minor problem");} 
		}
		} catch(Exception e) {logger.warn("Coverage compute warning major problem");}
		return coverage;
	}
	
	@Override
	public void plot(PlotProperties plot, Graphics2D image, BedItem range) {
		Coverage coverage = this.getCoverage(range);
		coverage.plotTrack(plot, image);
	}

	public List<Float> getCoverageSummariesForBackgroundBedItems(QuerySet querySet) {
		List <Float> coverages = new ArrayList <Float> ();
		for (BedItem bedItem : querySet.getBedItems()) {
			float sum = this.getCoverage(bedItem).getSum() / (new Float(bedItem.getLength()));
			coverages.add(sum);
		}
		Collections.sort(coverages);
		return coverages;
	}
	
	public Map<Integer, Float> getCoverageHistogramForBackground (QuerySet querySet, int range) {
		int len = 2 * range + 1;
		Map <Integer, Float> histogram = new TreeMap <Integer, Float> ();
		for (int i = 0; i < len; i++)
			histogram.put(i, 0f);
		for (BedItem bedItem : querySet.getBedItems()) {
			//logger.info("QuerySet histogram: " + bedItem);
			BedItem rangeBed = new BedItem(bedItem.getChromosome(), bedItem.getCenter() - range, bedItem.getCenter() + range);
			Coverage coverage = this.getCoverage(bedItem);
			for (int i = 0; i < len; i++) {
				float value = histogram.get(i);
				histogram.put(i, value + coverage.getValueForPosition(i + rangeBed.getStart()));
			}
		}
		for (int i = 0; i < len; i++) {
			float value = histogram.get(i);
			histogram.put(i, value / (float) querySet.size());
		}
		
		//logger.info("NAME: " + background.getName());
		//logger.info("SIZE: " + ((float) background.size()));
		
		return histogram;
	}
	
	public void plot(PlotProperties plot, Graphics2D image, List <QuerySet> querySets) {

		/* number of breaks in the image */
		
		int numberOfColors = 10;
		int sd = 2;
		int legendSize = 40;
		
		image.setFont(new Font("serif", Font.PLAIN, 14));
		FontMetrics metrics = image.getFontMetrics(image.getFont());
		
		/* calculate thresholds */
		List <Double> thresholds = new ArrayList <Double> ();
		StatisticalSummary stats = querySets.get(0).getStatisticalSummaryForTrack(this.getId());
		double zero = stats.getMean() - new Double(sd) * stats.getStandardDeviation();
		thresholds.add(zero);
		for(int i = 1; i < numberOfColors - 2; i++)
			thresholds.add(zero + i * 2 * sd * stats.getStandardDeviation() / new Double(numberOfColors - 2));
		thresholds.add(stats.getMean() + sd * stats.getStandardDeviation());
		
		/* count thresholds above zero, negative ones are absent, but thresholds can occur because of mean - sd */
		int thresholdsAboveZero = thresholds.size();
		for(double threshold : thresholds)
			if (threshold < 0) thresholdsAboveZero--;
		
		List <Color> palette = ColorBrewer.getDChipColors(numberOfColors);
		
		int counter = 0;
		for(QuerySet querySet : querySets) {
			
			/* position and width */
			int width = (int) (0.9 * (plot.getWidth() - legendSize) / querySets.size());
			int x = (int) (plot.getX() + 0.1 * width + legendSize + counter * (plot.getWidth() - legendSize) / querySets.size());
			
			/* labelling plot */
			image.setPaint(ColorBrewer.GREY_90);
			int stringHeight = metrics.getHeight();
			
			String message = querySet.getName();
			int stringWidth = metrics.stringWidth(message);
			image.drawString(message, x + width / 2 - stringWidth / 2, plot.getY() - 2);
			
			/* statistics */
			message = StatisticalResult.roundToSignificantDigits(querySets.get(0).tTest(querySet, this).getPValue(),2);
			stringWidth = metrics.stringWidth(message);
			image.drawString(message, x + width / 2 - stringWidth / 2, plot.getY() + plot.getHeight() + stringHeight + 1);
			
			/* drawing stripes */
			int numberOfStripes = querySet.getBedItems().size();
			int height = (int) (plot.getHeight() / numberOfStripes);
			if (height < 3) height = 3;
			List <Float> values = this.getCoverageSummariesForBackgroundBedItems(querySet);
			int verticalCounter = 0;
			for (Float value : values) {
				image.setPaint(palette.get(numberOfColors - 1));
				for (int i = numberOfColors - 2; i >= 0; i--) {
					//logger.trace("I: " + i);
					//logger.trace("THRE: " + thresholds.get(i));
					//logger.trace("PALE: " + palette.get(i));
					if(value < thresholds.get(i)) image.setPaint(palette.get(i));
				}
				int y = plot.getY() + plot.getHeight() - (int) (verticalCounter * plot.getHeight() / numberOfStripes);
				image.fillRect(x, y - height - 1, width, height + 1);
				verticalCounter++;
			}
			counter++;
		}
		
		for(int i = numberOfColors; i >= numberOfColors - thresholdsAboveZero; i--) {
			//logger.info("I:" + i);
			try {
				int y = plot.getY() + plot.getHeight() - (i + 1 - numberOfColors + thresholdsAboveZero) * plot.getHeight() / (thresholdsAboveZero + 1) - 1;
				//logger.info("Y:" + y);
				image.setPaint(ColorBrewer.GREY_90);
				if (i != numberOfColors) {
					image.drawLine(plot.getX() + legendSize / 2, y, plot.getX() + 3 * legendSize / 4, y);
					//logger.info(thresholds.get(i - 1));
					//logger.info(new Double((new Double(1000 * thresholds.get(i - 1))).intValue()) / 1000);
					String message = "" + (new Double((new Double(100 * thresholds.get(i - 1))).intValue()) / 100);
					int stringWidth = metrics.stringWidth(message);
					int stringHeight = metrics.getHeight();
					//logger.info("Message " + message);
					if (i % 2 == 0)
					image.drawString(message, plot.getX() + legendSize / 2 - 1 - stringWidth, y + stringHeight / 2 - 2);
				}
				
				image.setPaint(palette.get(i - 1));
				image.fillRect(plot.getX() + 3 * legendSize / 4, y, legendSize / 5, plot.getHeight() / (thresholdsAboveZero + 1) + 1);
			} catch (Exception e) { /* don't draw last square */ } 
		}
	}

	public void histogram(PlotProperties plot, Graphics2D image, List <QuerySet> querySets, int range) {

		/* number of breaks in the image */
		
		int legendSize = 40;
		
		Font font = new Font("Sans", Font.PLAIN, 16);
		image.setFont(font);
		
		/* GraphicsEnvironment graphicsEnvironment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
		for(String str : graphicsEnvironment.getAvailableFontFamilyNames()) {System.out.println(str);}; */
		
		
		FontMetrics metrics = image.getFontMetrics(font);
		
		int histogramBrakes = plot.getWidth() - legendSize;
		
		/* getData */
		Map <String, Map <Integer, Float>> data = new TreeMap <String, Map <Integer, Float>>(); 
		for(QuerySet querySet : querySets) {
			data.put(querySet.getName(), this.getCoverageHistogramForBackground(querySet, range));
		}
		
		/* getMax */
		float max = 0;
		for(Map <Integer, Float> histogram : data.values()) {
			for(float value : histogram.values())
				if(value > max) max = value;
		}
		
		List <Color> palette = ColorBrewer.getGraphPalette();
		
		/* plot */
		int colorCounter = 0;
		
		image.setStroke(new BasicStroke(2));
		
		
		for(Entry <String, Map <Integer, Float>> dataEntry : data.entrySet()) {
			
			image.setStroke(new BasicStroke(1));
			
			String name = dataEntry.getKey();
			Map <Integer, Float> histogram = dataEntry.getValue();
			
			int previousX = plot.getX() + legendSize;
			int previousY = plot.getY() + plot.getHeight() - new Float(plot.getHeight() * histogram.get(0) / max).intValue();
			int maxY = previousY;
			
			image.setPaint(palette.get(colorCounter++ % 8));
			float scale = (float) histogramBrakes / (float) histogram.size(); 
			for(int i = 0; i < histogram.size(); i++) {
				int x = plot.getX() + legendSize + (int) (i * scale);
				int y = plot.getY() + plot.getHeight() - new Float(plot.getHeight() * histogram.get(i) / max).intValue();
				if (y > maxY) maxY = y;
				//System.out.println(i + "\tX:" + previousX + "\t" + x + "\tY:" + previousY + "\t" + y + "\t" + maxY);
				if (x > previousX) {
					image.drawLine(previousX, previousY, x, maxY);
					previousX = x;
					previousY = maxY;
					maxY = 0;
				}
			}
			//System.out.println("Brakes: " + histogramBrakes + " " + plot.getX() + " \t" + plot.getWidth());
			//System.out.println(histogram.size());
			//System.out.println(scale);
			/* labelling plot */
			image.setStroke(new BasicStroke(4));
			int stringHeight = metrics.getHeight();
			int x = plot.getX() + legendSize;
			int y = plot.getY() + new Double(stringHeight * (colorCounter - 1) * 0.9).intValue();
			image.drawLine(x, y, x + 40, y);
			
			image.setPaint(ColorBrewer.GREY_80);
			String message = name;
			image.drawString(message, x + 47, y + 5);
			
		}
		
		/* draw scale */
		image.setStroke(new BasicStroke(1));
		image.setPaint(ColorBrewer.GREY_80);
		
		image.drawLine(plot.getX() + legendSize - 20, plot.getY(), plot.getX() + legendSize - 10, plot.getY());
		image.drawLine(plot.getX() + legendSize - 10, plot.getY(), plot.getX() + legendSize - 10, plot.getY() + plot.getHeight());
		image.drawLine(plot.getX() + legendSize - 20, plot.getY() + plot.getHeight(), plot.getX() + legendSize - 10, plot.getY() + plot.getHeight());
		
		
		
		String message = "" + (new Float(new Float(max * 100).intValue()) / 100);
		int stringWidth = metrics.stringWidth(message);
		int stringHeight = metrics.getHeight();
		image.drawString(message, plot.getX() + legendSize - 20 - stringWidth, plot.getY() + stringHeight / 2 - 3);
		
		image.rotate(Math.toRadians(-90), plot.getX() + legendSize - 20 - stringWidth, plot.getY() + plot.getHeight() / 2);
		image.drawString("Average coverage", plot.getX() + legendSize - 80 - stringWidth, plot.getY() + plot.getHeight() - 50);
		image.rotate(Math.toRadians(90), plot.getX() + legendSize - 20 - stringWidth, plot.getY() + plot.getHeight() / 2);
		
		
		float quarter = (float) (plot.getWidth() - legendSize) / 4;
		image.drawLine(plot.getX() + legendSize, plot.getY() + plot.getHeight() + 10, plot.getX() + legendSize, plot.getY() + plot.getHeight() + 20);
		image.drawLine(plot.getX() + legendSize, plot.getY() + plot.getHeight() + 10, plot.getX() + plot.getWidth(), plot.getY() + plot.getHeight() + 10);
		image.drawLine(plot.getX() + plot.getWidth(), plot.getY() + plot.getHeight() + 10, plot.getX() + plot.getWidth(), plot.getY() + plot.getHeight() + 20);
		image.drawLine(plot.getX() + legendSize + (int) (quarter * 1), plot.getY() + plot.getHeight() + 10, plot.getX() + legendSize + (int) (quarter * 1), plot.getY() + plot.getHeight() + 20);
		image.drawLine(plot.getX() + legendSize + (int) (quarter * 2), plot.getY() + plot.getHeight() + 10, plot.getX() + legendSize + (int) (quarter * 2), plot.getY() + plot.getHeight() + 20);
		image.drawLine(plot.getX() + legendSize + (int) (quarter * 3), plot.getY() + plot.getHeight() + 10, plot.getX() + legendSize + (int) (quarter * 3), plot.getY() + plot.getHeight() + 20);

		
		message = "Distance to center of genomic range (bp)";
		stringWidth = metrics.stringWidth(message);
		stringHeight = metrics.getHeight();
		image.drawString(message, plot.getX() + legendSize + 50, plot.getY() + plot.getHeight() + 55);
		message = "-1000";
		image.drawString(message, plot.getX() + legendSize -20, plot.getY() + plot.getHeight() + 35);
		message = "-500";
		image.drawString(message, plot.getX() + legendSize + (int) (quarter * 1) -18, plot.getY() + plot.getHeight() + 35);
		message = "0";
		image.drawString(message, plot.getX() + legendSize + (int) (quarter * 2) -4, plot.getY() + plot.getHeight() + 35);
		message = "500";
		image.drawString(message, plot.getX() + legendSize + (int) (quarter * 3) -14, plot.getY() + plot.getHeight() + 35);
		message = "1000";
		image.drawString(message, plot.getX() + legendSize + (int) (quarter * 4) - 20, plot.getY() + plot.getHeight() + 35);
	}
	
}

