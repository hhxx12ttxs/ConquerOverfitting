import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
public void stateChanged(ChangeEvent e)
{
JTabbedPane pane = (JTabbedPane) e.getSource();
if(pane.getSelectedComponent() instanceof ChartPanel)

