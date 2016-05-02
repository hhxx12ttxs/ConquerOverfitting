package pl.edu.agh.stockwatch.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import pl.edu.agh.stockwatch.model.Quote;
import pl.edu.agh.stockwatch.model.Quotes;

public class GraphPlotter {

	public static JFreeChart quotesPlot(Quotes quotes) {
		return createChart(createDataset(quotes), quotes.getStock());
	}
	
	public static BufferedImage quotesPlot(Quotes quotes, int width, int height) throws IOException {
	
        /* INIT */
		
		
		
        BufferedImage image = new BufferedImage(width, height, 
        		BufferedImage.TYPE_INT_RGB);
        
        JFreeChart jfreechart = createChart(createDataset(quotes), quotes.getStock());
        image = jfreechart.createBufferedImage(width, height);
		
        //ChartUtilities.writeChartAsJPEG(out, chart, width, height)
        
        
        return image;
        
        //saveToFile( image, new File( filename ) );
	}
	
    private static XYDataset createDataset(Quotes quotes)
    {
	
	TimeSeries qSeries = new TimeSeries("quoteSeries");
	TimeSeriesCollection dataset = new TimeSeriesCollection();
    Calendar c = Calendar.getInstance();
    Quotes tsInput = quotes;
    
    
	for(Quote q: tsInput.getQuotes()) {
		c.setTime(q.getDate());
		Month mnt = new Month((c.get(Calendar.MONTH))+1, c.get(Calendar.YEAR));
		//Day d = new Day(c.get(Calendar.DAY_OF_MONTH), (c.get(Calendar.MONTH))+1, c.get(Calendar.YEAR));
		//System.out.println(mnt);
		qSeries.addOrUpdate(mnt, q.getClose());        		
	
	dataset.addSeries(qSeries);
	}
	return dataset;
	}
    

    public static JFreeChart createChart(XYDataset dataset, String stock)
    {
            JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(stock+ " ", "Date", "Index", dataset, false, true, false);
            jfreechart.setBackgroundPaint(Color.white);
          
          //ChartPanel panel;
            
            Plot plot = jfreechart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
          
            jfreechart.setBackgroundPaint(new java.awt.Color(245,245,245));
            
            XYPlot xyplot = (XYPlot)jfreechart.getPlot();
            xyplot.setInsets(new RectangleInsets(5D, 5D, 5D, 20D));
            xyplot.setBackgroundPaint(Color.lightGray);
            xyplot.setAxisOffset(new RectangleInsets(5D, 5D, 5D, 5D));
            xyplot.setDomainGridlinePaint(Color.white);
            xyplot.setRangeGridlinePaint(Color.white);
            NumberAxis numberaxis = (NumberAxis)xyplot.getRangeAxis();
            numberaxis.setAutoRangeIncludesZero(false);
            numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            return jfreechart;
    }
	
	public static void saveToFile( BufferedImage img, File file ) throws IOException {

        ImageWriter writer = null;
        java.util.Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
        if( iter.hasNext() ){

            writer = (ImageWriter)iter.next();

        }

        ImageOutputStream ios = ImageIO.createImageOutputStream( file );
        writer.setOutput(ios);

        ImageWriteParam param = new JPEGImageWriteParam( java.util.Locale.getDefault() );
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;

        param.setCompressionQuality(0.98f);

        writer.write(null, new IIOImage( img, null, null ), param);
    }
}

