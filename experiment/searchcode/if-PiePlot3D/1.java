import javax.swing.Timer;
import org.jfree.chart.plot.PiePlot3D;

class Rotator extends Timer implements ActionListener
angle = angle + 1;
if(angle == 360)
angle = 0;
}

private PiePlot3D plot;
private int angle;
}

