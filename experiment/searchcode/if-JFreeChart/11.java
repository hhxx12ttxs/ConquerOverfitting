import java.awt.datatransfer.*;
import java.io.*;
import org.jfree.chart.JFreeChart;

public class PDFChartTransferable
implements Transferable
{

final DataFlavor pdfFlavor;
private JFreeChart chart;
private int width;

