package Control.LOOK;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import View.DiskOptimizationApp;

import Control.Reader.Reader;

public class Look {
	static Reader r;
	public static int sortedSequence[];
	
	public Look(int current, int previous,int rawSequence[],Reader r){
		this.r=r;
		int direction=0;
		int lookSequence[] = rawSequence;
		//finding the direction of the arrow..
		if(previous>current){direction=0; };  // going towards zero
		if(previous<current){direction=1;}  // going away from zero
		
		//sorting the sequence in order
		Arrays.sort(lookSequence);
		//sorts it in LOOK methods
		LOOK(lookSequence,direction,r.getCurrent());
		
		DiskOptimizationApp.jPanel_leftContent= null;
		DiskOptimizationApp.jPanel_leftContent = createChart();
	}
	
	public void LOOK(int rawSequence[],int direction,int current){
		sortedSequence=new int[rawSequence.length];
				
		if(direction==0)//going towards zero direction
		{
			//finding where the first point will be from
			int startIndex = 1010;
			for(int i=0;i<rawSequence.length;i++){
				if(current<rawSequence[i]){startIndex=i; break;}
				
			}
			
			//sorting into LOOK method
			int filledIndex=0;
			for(int i=startIndex-1;i>0;i--){
				sortedSequence[filledIndex]=rawSequence[i];
				filledIndex++;
			}
			for(int i=startIndex;i<rawSequence.length;i++){
				sortedSequence[i]=rawSequence[i];
			}
		}
		
		if(direction==1)//going away from zero direction
		{
			//finding where the first point will be from
			int startIndex = 1010;
			for(int i=0;i<rawSequence.length;i++){
				if(current<rawSequence[i]){startIndex=i; break;}
				
			}
			//sorting into LOOK method
			int filledIndex=0;
			for(int i=startIndex;i<rawSequence.length;i++){
				sortedSequence[filledIndex]=rawSequence[i];
				filledIndex++;
			}
			for(int i=startIndex-1;i>=0;i--){
				sortedSequence[filledIndex]=rawSequence[i];
				filledIndex++;
			}
		}
		
		output(sortedSequence);
	}
	
	public void output(int sortedSequence[]){
		String sequence="";
		String working1="";
		String working2="";
		int total=0;
		sequence+=r.getCurrent();
		int previous=r.getCurrent();
		
		for(int i=0; i<sortedSequence.length;i++)
		{
			int current=sortedSequence[i];
			sequence+=","+current;
			int d= Math.abs(previous-current);
			
			working1+="|"+previous+"-"+current+"|+";
			working2+=d+" + ";
			total+=d;
			previous=current;
		}
		DiskOptimizationApp.getJTextArea().setText("Method\t: LOOK"+'\n'+"-----------------\n"+"Order of Access\t: "+sequence+"\n"+"Total Distance\t= "+working1.substring(0,working1.length()-1)+"\n"+"              \t= "+working2.substring(0,working2.length()-2)+"\n"+"              \t= "+total+'\n');
		System.out.println("Method\t: LOOK"+'\n'+"-----------------");
		System.out.println("Order of Access\t: "+sequence);
		System.out.println("Total Distance\t= "+working1.substring(0,working1.length()-1));
		System.out.println("              \t= "+working2.substring(0,working2.length()-2));
		System.out.println("              \t= "+total+'\n');
		
	}

	  private static CategoryDataset createDataset()
	    {
		  	DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();
	        defaultcategorydataset.addValue(r.getCurrent(), "Classes", ""+0);
	        
	        for(int i=0; i<sortedSequence.length;i++){
	        	 
	        	   defaultcategorydataset.addValue(sortedSequence[i], "Classes", (i+1)+"");
	        }
	        return defaultcategorydataset;
	    }

	    private static JFreeChart createChart(CategoryDataset categorydataset)
	    {
	        JFreeChart jfreechart = ChartFactory.createLineChart("LOOK", null, "", categorydataset, PlotOrientation.HORIZONTAL, false, true, false);
	        jfreechart.addSubtitle(new TextTitle("By The Fantastic 4"));
	        
	        TextTitle texttitle = new TextTitle("Authors: A Ameenudeen,Adeel Ateeque,Lee Kai Quan,Shahrikin Alias");
	        texttitle.setFont(new Font("SansSerif", 0, 10));
	        texttitle.setPosition(RectangleEdge.BOTTOM);
	        texttitle.setHorizontalAlignment(HorizontalAlignment.RIGHT);
	        jfreechart.addSubtitle(texttitle);
	        
	        jfreechart.setBackgroundPaint(Color.white);
	        CategoryPlot categoryplot = (CategoryPlot)jfreechart.getPlot();
	        categoryplot.setBackgroundPaint(Color.lightGray);
	        categoryplot.setRangeGridlinesVisible(false);
	        java.net.URL url = (Look.class).getClassLoader().getResource("OnBridge11small.png");
	        if(url != null)
	        {
	            ImageIcon imageicon = new ImageIcon(url);
	            jfreechart.setBackgroundImage(imageicon.getImage());
	            categoryplot.setBackgroundPaint(new Color(0, 0, 0, 0));
	        }
	        NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
	        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        LineAndShapeRenderer lineandshaperenderer = (LineAndShapeRenderer)categoryplot.getRenderer();
	        lineandshaperenderer.setShapesVisible(true);
	        lineandshaperenderer.setDrawOutlines(true);
	        lineandshaperenderer.setUseFillPaint(true);
	        lineandshaperenderer.setBaseFillPaint(Color.white);
	        lineandshaperenderer.setSeriesStroke(0, new BasicStroke(3F));
	        lineandshaperenderer.setSeriesOutlineStroke(0, new BasicStroke(2.0F));
	        lineandshaperenderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-5D, -5D, 10D, 10D));
	        return jfreechart;
	    }

	    public static JPanel createChart()
	    {
	        JFreeChart jfreechart = createChart(createDataset());
	        return new ChartPanel(jfreechart);
	    }
}

