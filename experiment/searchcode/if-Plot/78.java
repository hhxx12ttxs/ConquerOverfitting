import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import cnuphys.ced.event.plot.ReconstructionPlotGrid;
* Access to the PlotManager singleton
*
* @return the PlotManager singleton
*/
public static PlotManager getInstance() {
if (instance == null) {

