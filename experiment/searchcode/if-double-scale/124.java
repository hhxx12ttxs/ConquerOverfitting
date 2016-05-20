package bestelsnel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.CellRendererPane;
import javax.swing.JTable;
import javax.swing.RepaintManager;

/**
 * Provides printing capabilities to the rest of the application
 */
public class PrintUtilities implements Printable
{
	private Component componentToBePrinted;
	private String name;
	private CellRendererPane renderer;

	/**
	 * Creates an instance of PrintUtilities and calls <code>print();</code>
	 * @param c The component to be printed
	 * @param name The name to be used for the print job
	 * @param renderer The CellRendererPane used to render <code>c</code>
	 * @return The used instance of PrintUtilities.
	 */
	public static PrintUtilities printComponent(Component c, String name, CellRendererPane renderer)
	{
		PrintUtilities printUtilities = new PrintUtilities(c, name, renderer);
		printUtilities.print();

		return printUtilities;
	}

	private PrintUtilities(Component componentToBePrinted, String name, CellRendererPane renderer)
	{
		this.componentToBePrinted = componentToBePrinted;
		this.name = name;
		this.renderer = renderer;
	}

	/**
	 * Start the printing process, shows a print dialog.
	 */
	public void print()
	{
		// Gets a printer job and sets it up
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this);
		printJob.setJobName(this.name);

		// Opens the printDialog
		if (printJob.printDialog())
		{ // Only proceed if the user choses to print
			try
			{
				// Off to the printer
				printJob.print();
			}
			catch (PrinterException pe)
			{
				Logger.getLogger(PrintUtilities.class.getName()).log(Level.SEVERE, null, pe);
			}
		}
	}

	/**
	 * Makes the printing happen, calls either printTable or printDefault depending
	 * on the component that needs to be printed
	 * @param g
	 * @param pageFormat
	 * @param pageIndex
	 * @return 
	 */
	@Override
	public int print(Graphics g, PageFormat pageFormat, int pageIndex)
	{
		int retval = 0;

		if (componentToBePrinted instanceof JTable)
		{
			retval = printTable(g, pageFormat, pageIndex);
		}
		else
		{
			retval = printDefault(g, pageFormat, pageIndex);
		}

//		// Draw a logo
//		Graphics2D g2d = (Graphics2D)g;
//        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
//		g2d.setColor(Color.black);
//		String text = "BestelSnel";
//		Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(text, g);
//		g2d.drawString("BestelSnel", (float)(pageFormat.getImageableWidth() - bounds.getWidth()), (float)bounds.getHeight());
//		
		return retval;
	}

	/**
	 * Prints the table to g
	 * @param g
	 * @param pageFormat
	 * @param pageIndex
	 * @return 
	 */
	private int printTable(Graphics g, PageFormat pageFormat, int pageIndex)
	{
		JTable tableView = (JTable) componentToBePrinted;

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.black);
		int fontHeight = g2.getFontMetrics().getHeight();
		int fontDesent = g2.getFontMetrics().getDescent();

		//leave room for page number
		double pageHeight =
			   pageFormat.getImageableHeight() - fontHeight;
		double pageWidth =
			   pageFormat.getImageableWidth();
		double tableWidth = (double) tableView.getColumnModel().getTotalColumnWidth();
		double scale = 1;
		if (tableWidth >= pageWidth)
		{
			scale = pageWidth / tableWidth;
		}

		double headerHeightOnPage =
			   tableView.getTableHeader().getHeight() * scale;
		double tableWidthOnPage = tableWidth * scale;

		double oneRowHeight = (tableView.getRowHeight()
							   + tableView.getRowMargin()) * scale;
		int numRowsOnAPage =
			(int) ((pageHeight - headerHeightOnPage)
				   / oneRowHeight);
		double pageHeightForTable = oneRowHeight
									* numRowsOnAPage;
		int totalNumPages =
			(int) Math.ceil(((double) tableView.getRowCount())
							/ numRowsOnAPage);
		if (pageIndex >= totalNumPages)
		{
			return NO_SUCH_PAGE;
		}

		g2.translate(pageFormat.getImageableX(),
					 pageFormat.getImageableY());
		//bottom center
		g2.drawString("Page: " + (pageIndex + 1),
					  (int) pageWidth / 2 - 35, (int) (pageHeight
													   + fontHeight - fontDesent));

		g2.translate(0f, headerHeightOnPage);
		g2.translate(0f, -pageIndex * pageHeightForTable);

		//If this piece of the table is smaller 
		//than the size available,
		//clip to the appropriate bounds.
		if (pageIndex + 1 == totalNumPages)
		{
			int lastRowPrinted =
				numRowsOnAPage * pageIndex;
			int numRowsLeft =
				tableView.getRowCount()
				- lastRowPrinted;
			g2.setClip(0,
					   (int) (pageHeightForTable * pageIndex),
					   (int) Math.ceil(tableWidthOnPage),
					   (int) Math.ceil(oneRowHeight
									   * numRowsLeft));
		} //else clip to the entire area available.
		else
		{
			g2.setClip(0,
					   (int) (pageHeightForTable * pageIndex),
					   (int) Math.ceil(tableWidthOnPage),
					   (int) Math.ceil(pageHeightForTable));
		}

		g2.scale(scale, scale);
		tableView.paint(g2);
		g2.scale(1 / scale, 1 / scale);
		g2.translate(0f, pageIndex * pageHeightForTable);
		g2.translate(0f, -headerHeightOnPage);
		g2.setClip(0, 0,
				   (int) Math.ceil(tableWidthOnPage),
				   (int) Math.ceil(headerHeightOnPage));
		g2.scale(scale, scale);
		tableView.getTableHeader().paint(g2);
		//paint header at top

		return Printable.PAGE_EXISTS;
	}

	/**
	 * Draws component to g
	 * @param g
	 * @param pageFormat
	 * @param pageIndex
	 * @return 
	 */
	private int printDefault(Graphics g, PageFormat pageFormat, int pageIndex)
	{

		if (pageIndex > 0)
		{
			return NO_SUCH_PAGE;
		}
		else
		{
			disableDoubleBuffering(componentToBePrinted);
			renderer.paintComponent(g, componentToBePrinted, null, (int) pageFormat.getImageableX(), (int) pageFormat.getImageableY(), (int) pageFormat.getImageableWidth(), (int) pageFormat.getImageableHeight(), true);
			enableDoubleBuffering(componentToBePrinted);
			return PAGE_EXISTS;
		}
	}

	private static void disableDoubleBuffering(Component c)
	{
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	private static void enableDoubleBuffering(Component c)
	{
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}
}
