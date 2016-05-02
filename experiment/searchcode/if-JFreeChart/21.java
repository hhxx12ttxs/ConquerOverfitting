

import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.*;
import java.awt.Dimension;
import java.awt.GridLayout;

public class Chart extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int[] value;
	int total;
	
//	 Constructors
	public Chart(int[] value,int total){
		this.value = value;
		this.total = total;
		initGUI();
	}
	
	private void initGUI(){		
		this.setLayout(new GridLayout(1, 1));
		JPanel jpanel = createDemoPanel();
        jpanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(jpanel);		
	}
	
	private static JFreeChart createChart(XYDataset xydataset)
    {
        JFreeChart jfreechart = ChartFactory.createXYLineChart("Baccarat", "Hand", "Loss/Profit", xydataset, PlotOrientation.VERTICAL, true, true, false);        
        return jfreechart;
    }
	
	private XYDataset createDataset()
    {
		XYSeries series = new XYSeries("Profit/Loss at Hand");
		int total_profit = 0;
        for(int i=0; i<value.length;i++){
        	if (value[i]==-1) break;
        	total_profit+=value[i];
        	series.add(i, total_profit);
        }
        XYDataset xyDataset = new XYSeriesCollection(series);
        return xyDataset;
    }
	
	public JPanel createDemoPanel()
    {
        JFreeChart jfreechart = createChart(createDataset());
        ChartPanel chartPanel = new ChartPanel(jfreechart);
        chartPanel.getChartRenderingInfo().setEntityCollection(null);
        return chartPanel;
    }
	
}

