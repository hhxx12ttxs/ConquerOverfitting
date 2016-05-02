package plugins.adufour.activemeshes.shape;

import icy.canvas.IcyCanvas;
import icy.file.xls.XlsManager;
import icy.gui.component.sequence.SequenceChooser;
import icy.gui.util.GuiUtil;
import icy.image.IcyBufferedImage;
import icy.main.Icy;
import icy.math.ArrayMath;
import icy.painter.PainterAdapter;
import icy.sequence.Sequence;
import icy.type.DataType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import plugins.adufour.activemeshes.mesh.Mesh;
import plugins.adufour.activemeshes.mesh.Vertex;
import plugins.adufour.activemeshes.shape.quickhull3d.QuickHull3D;
import plugins.fab.trackmanager.PluginTrackManagerProcessor;
import plugins.fab.trackmanager.TrackSegment;
import plugins.nchenouard.spot.Detection;

public class TrackProcessorMeshAnalysis extends PluginTrackManagerProcessor
{
	private enum Descriptors
	{
		None, Perimeter, Volume, Roundness, RadiiVar, Convexity, Speed_over_Curvature, Confinement
	}
	
	private JComboBox		jComboDescriptors		= new JComboBox(Descriptors.values());
	
	private JButton			jButtonSaveToXLS		= new JButton("Mesh->XLS");
	
	private JButton			jButtonSaveToVTK		= new JButton("Mesh->VTK");
	
	private JButton			jButtonSaveToOFF		= new JButton("Mesh->OFF");
	
	private JButton			jButtonSaveShapeToXLS	= new JButton("Shape->XLS");
	
	private JButton			jButtonExportBinary		= new JButton("Binary volume");
	
	private SequenceChooser	jComboSequences			= new SequenceChooser();
	
	private JPanel			chartPanel				= new JPanel();
	
	private double			tScale;
	
	public TrackProcessorMeshAnalysis()
	{
		super.setName("Mesh processing");
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		panel.add(Box.createVerticalStrut(5));
		
		JPanel panelExport = GuiUtil.createLineBoxPanel(jButtonSaveToXLS, Box.createHorizontalStrut(5), jButtonSaveToVTK, Box.createHorizontalStrut(5), jButtonSaveToOFF, Box.createHorizontalStrut(5),
				jButtonSaveShapeToXLS, Box.createHorizontalStrut(5), jButtonExportBinary);
		panelExport.setBorder(new TitledBorder("Export"));
		panel.add(panelExport);
		
		panel.add(Box.createVerticalStrut(5));
		
		panel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(10), new JLabel("Paint on:"), Box.createHorizontalStrut(10), jComboSequences, Box.createHorizontalStrut(10)));
		
		panel.add(Box.createVerticalStrut(5));
		
		panel.add(GuiUtil.createLineBoxPanel(Box.createHorizontalStrut(10), new JLabel("Plot descriptor:"), Box.createHorizontalStrut(10), jComboDescriptors, Box.createHorizontalStrut(10)));
		
		jComboDescriptors.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Compute();
			}
		});
		jButtonSaveToXLS.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						exportMeshToXLS();
					}
				}.run();
			}
		});
		jButtonSaveToVTK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						exportMeshToVTK();
					}
				}.run();
			}
		});
		jButtonSaveToOFF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						exportMeshToOFF();
					}
				}.run();
			}
		});
		jButtonSaveShapeToXLS.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread()
				{
					public void run()
					{
						saveShapeToXLS();
					}
				}.run();
			}
		});
		jButtonExportBinary.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				new Thread()
				{
					public void run()
					{
						Icy.getMainInterface().addSequence(rasterizeContours(jComboSequences.getSelectedSequence()));
					}
				}.run();
			}
		});
		
		// chartPanel.setPreferredSize(new Dimension(500, 200));
		
		panel.add(chartPanel);
	}
	
	@Override
	public void Close()
	{
	}
	
	@Override
	public synchronized void Compute()
	{
		chartPanel.removeAll();
		
		if (!super.isEnabled()) return;
		
		if (trackPool.getDisplaySequence() == null) return;
		
		tScale = trackPool.getDisplaySequence().getTimeInterval();
		
		int dim = 3; // FIXME
		
		ChartPanel chart = null;
		
		Descriptors descriptor = (Descriptors) jComboDescriptors.getSelectedItem();
		
		switch (descriptor)
		{
			case None:
				return;
				
			case Perimeter:
				double[][] dim1 = computeDimension(1);
				chart = createChartPanel(dim1, (dim == 2) ? "Perimeter" : "Surface", "Time (sec.)", "\u03BC" + (dim == 2 ? "m" : "m\u00B2"));
			break;
			
			case Volume:
				double[][] dim2 = computeDimension(2);
				chart = createChartPanel(dim2, (dim == 2) ? "Area" : "Volume", "Time (sec.)", "\u03BC" + (dim == 2 ? "m\u00B2" : "m\u00B3"));
			break;
			
			case Roundness:
				double[][] roundness = computeRoundness();
				chart = createChartPanel(roundness, "Roundness", "Time (sec.)", "%");
			break;
			
			case RadiiVar:
				double[][] radiivar = computeRadiiVar();
				chart = createChartPanel(radiivar, "Radii Variance", "Time (sec.)", "Var.");
			break;
			
			case Convexity:
				double[][] convexHullDiff = computeConvexity();
				chart = createChartPanel(convexHullDiff, "Convexity", "Time (sec.)", "%");
			break;
			
			case Speed_over_Curvature:
				computeSpeedOverCurvature();
			// chart = createChartPanel(speedOverCurvature, "Speed/Curvature", "Speed",
			// "Curvature");
			break;
			
			case Confinement:
				
				computeConfinement();
			
			break;
			
			default:
				System.out.println("Measure " + descriptor.toString() + " not implemented yet");
				return;
		}
		jComboDescriptors.setSelectedItem(Descriptors.None);
		
		if (chart != null)
		{
			// replace default chart colors by detection colors (taken from t=0)
			XYItemRenderer renderer = ((XYPlot) chart.getChart().getPlot()).getRenderer();
			for (TrackSegment ts : trackPool.getTrackSegmentList())
				renderer.setSeriesPaint(trackPool.getTrackIndex(ts), ts.getFirstDetection().getColor());
			
			chartPanel.add(chart);
		}
		// FIXME super.setPanelHeight(355);
		super.panel.updateUI();
		
		jComboSequences.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final Sequence s = jComboSequences.getSelectedSequence();
				
				s.addPainter(new PainterAdapter()
				{
					@Override
					public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
					{
						for (Detection det : trackPool.getAllDetection())
						{
							if (canvas.getPositionT() != det.getT() || canvas.getPositionZ() != (int) det.getZ()) continue;
							
							int i = trackPool.getTrackIndex(trackPool.getTrackSegmentWithDetection(det));
							
							int x = (int) det.getX();
							int y = (int) det.getY();
							g.setColor(Color.orange);
							g.setFont(g.getFont().deriveFont(9f));
							g.drawString("" + i, x - 6, y - 6);
							g.drawOval((int) x - 3, (int) y - 3, 6, 6);
						}
					}
				});
			}
			
		});
	}
	
	private void saveShapeToXLS()
	{
		JFileChooser jfc = new JFileChooser();
		
		if (jfc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
		
		XlsManager xlsManager;
		try
		{
			xlsManager = new XlsManager(jfc.getSelectedFile());
			
			int dim = 3; // FIXME
			
			saveToXls(xlsManager, (dim == 1) ? "Perimeter" : "Surface", computeDimension(1));
			saveToXls(xlsManager, (dim == 1) ? "Area" : "Volume", computeDimension(2));
			saveToXls(xlsManager, "Roundness", computeRoundness());
			saveToXls(xlsManager, "Convexity", computeConvexity());
			
			xlsManager.SaveAndClose();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void exportMeshToXLS()
	{
		JFileChooser jfc = new JFileChooser();
		
		if (jfc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
		
		XlsManager xlsManager;
		try
		{
			xlsManager = new XlsManager(jfc.getSelectedFile());
			
			int cpt = 1;
			for (Detection det : trackPool.getAllDetection())
			{
				Mesh c3d = (Mesh) det;
				
				xlsManager.createNewPage("Object " + (cpt++));
				xlsManager.setLabel(0, 0, "X");
				xlsManager.setLabel(1, 0, "Y");
				xlsManager.setLabel(2, 0, "Z");
				xlsManager.setLabel(3, 0, "NX");
				xlsManager.setLabel(4, 0, "NY");
				xlsManager.setLabel(5, 0, "NZ");
				
				int n = c3d.vertices.size();
				
				for (int i = 0, row = 1; i < n; i++)
				{
					Vertex v = c3d.vertices.get(i);
					
					if (v == null) continue;
					
					xlsManager.setNumber(0, row, v.position.x);
					xlsManager.setNumber(1, row, v.position.y);
					xlsManager.setNumber(2, row, v.position.z);
					
					xlsManager.setNumber(3, row, v.normal.x);
					xlsManager.setNumber(4, row, v.normal.y);
					xlsManager.setNumber(5, row, v.normal.z);
					
					row++;
				}
			}
			
			xlsManager.SaveAndClose();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void exportMeshToVTK()
	{
		JFileChooser jfc = new JFileChooser();
		
		if (jfc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
		
		File f = jfc.getSelectedFile();
		
		int cpt = 0;
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			String filePrefix = "_#" + (cpt < 1000 ? "0" : "");
			filePrefix += (cpt < 100 ? "0" : "");
			filePrefix += (cpt < 10 ? "0" : "") + cpt;
			
			for (Detection det : ts.getDetectionList())
			{
				int t = det.getT();
				String fileName = filePrefix + "_T" + (t < 10 ? "000" : t < 100 ? "00" : t < 1000 ? "0" : "") + t + ".vtk";
				PrintStream ps;
				try
				{
					ps = new PrintStream(f.getAbsolutePath() + fileName);
					if (det.getDetectionType() == Detection.DETECTIONTYPE_VIRTUAL_DETECTION)
					{
						ps.close();
					}
					else
					{
						Mesh c3d = (Mesh) det;
						c3d.exportToVTK(ps);
					}
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
			
			cpt++;
		}
	}
	
	private void exportMeshToOFF()
	{
		JFileChooser jfc = new JFileChooser();
		
		if (jfc.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
		
		File f = jfc.getSelectedFile();
		
		int cpt = 1;
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			String filePrefix = "_#" + (cpt < 10 ? "0" : "") + cpt;
			
			for (Detection det : ts.getDetectionList())
			{
				String fileName = filePrefix + "_T" + (det.getT() < 10 ? "0" : "") + det.getT() + ".off";
				PrintStream ps;
				try
				{
					ps = new PrintStream(f.getAbsolutePath() + fileName);
					Mesh c3d = (Mesh) det;
					c3d.exportToOFF(ps);
				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
			
			cpt++;
		}
	}
	
	private ChartPanel createChartPanel(double[][] array, String title, String xLabel, String yLabel)
	{
		XYSeriesCollection plot = new XYSeriesCollection();
		
		for (int track = 0; track < array.length; track++)
		{
			XYSeries series = new XYSeries(track);
			
			double[] row = array[track];
			
			for (int frame = 0; frame < row.length; frame++)
			{
				double value = row[frame];
				
				if (value != 0) series.add(frame * tScale, value);
			}
			
			plot.addSeries(series);
		}
		
		JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, plot, PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips
				false // urls
				);
		
		return new ChartPanel(chart, 500, 300, 500, 300, 500, 300, false, false, true, true, true, true);
	}
	
	private void saveToXls(XlsManager xlsManager, String pageTitle, double[][] array)
	{
		xlsManager.createNewPage(pageTitle);
		
		xlsManager.setLabel(0, 0, "Track \\ Time");
		
		// time labels
		for (int t = 0; t < trackPool.getDisplaySequence().getSizeT(); t++)
			xlsManager.setNumber(t + 1, 0, t * tScale);
		
		// indexes + data
		for (int i = 0; i < trackPool.getTrackSegmentList().size(); i++)
		{
			xlsManager.setNumber(0, i + 1, i);
			double[] row = array[i];
			for (int j = 0; j < row.length; j++)
				if (row[j] != 0) xlsManager.setNumber(j + 1, i + 1, row[j]);
		}
	}
	
	private double[][] computeDimension(int order)
	{
		double[][] result = new double[trackPool.getTrackSegmentList().size()][trackPool.getDisplaySequence().getSizeT()];
		
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			if (!(ts.getFirstDetection() instanceof Mesh)) continue;
			
			int trackIndex = trackPool.getTrackIndex(ts);
			
			for (Detection det : ts.getDetectionList())
			{
				if (!(det instanceof Mesh)) continue;
				
				double value = ((Mesh) det).getDimension(order);
				result[trackIndex][det.getT()] = value;
			}
		}
		
		return result;
	}
	
	/**
	 * Computes the roundness of all objects over time. The roundness is computed as the ratio
	 * between the smallest and biggest distance between the mass center and the mesh vertices,
	 * expressed as a percentage
	 * 
	 * @return
	 */
	private double[][] computeRoundness()
	{
		double[][] result = new double[trackPool.getTrackSegmentList().size()][trackPool.getDisplaySequence().getSizeT()];
		
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			int trackIndex = trackPool.getTrackIndex(ts);
			
			for (Detection det : ts.getDetectionList())
			{
				if (!(det instanceof Mesh)) continue;
				
				Mesh contour = (Mesh) det;
				double min = contour.getMinDistanceTo(contour.getMassCenter());
				double max = contour.getMaxDistanceToCenter();
				
				result[trackIndex][det.getT()] = (min / max) * 100;
			}
		}
		
		return result;
	}
	
	/**
	 * Computes the coefficient of variation of shape for all objects over time. This coefficient is
	 * given as the variance of all distances between the mass center and the vertices, normalized
	 * over the average mesh radius
	 * 
	 * @return
	 */
	private double[][] computeRadiiVar()
	{
		double[][] result = new double[trackPool.getTrackSegmentList().size()][trackPool.getDisplaySequence().getSizeT()];
		
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			int trackIndex = trackPool.getTrackIndex(ts);
			
			for (Detection det : ts.getDetectionList())
			{
				if (!(det instanceof Mesh)) continue;
				
				Mesh contour = (Mesh) det;
				int n = (int) contour.getDimension(0);
				double[] distances = new double[n];
				int cpt = 0;
				
				Point3d center = contour.getMassCenter();
				
				for (Point3d p : contour)
				{
					if (p == null) continue;
					distances[cpt++] = p.distance(center);
				}
				
				result[trackIndex][det.getT()] = ArrayMath.var(distances, true) / ArrayMath.mean(distances);
			}
		}
		
		return result;
	}
	
	/**
	 * Measures the convexity of all objects over time. The convexity is computed as the difference
	 * between the object and its convex hull, expressed as a percentage of the convex hull. Hence,
	 * the value is 1 if the object is convex, and lower than 1 otherwise
	 * 
	 * @return
	 */
	private double[][] computeConvexity()
	{
		double[][] result = new double[trackPool.getTrackSegmentList().size()][trackPool.getDisplaySequence().getSizeT()];
		
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			int trackIndex = trackPool.getTrackIndex(ts);
			
			for (Detection det : ts.getDetectionList())
			{
				if (!(det instanceof Mesh)) continue;
				
				Mesh contour = (Mesh) det;
				
				Point3d[] points = new Point3d[(int) contour.getDimension(0)];
				int i = 0;
				for (Point3d p : contour)
					if (p != null) points[i++] = p;
				
				QuickHull3D q3d = new QuickHull3D(points);
				int[][] hullFaces = q3d.getFaces();
				Point3d[] hullPoints = q3d.getVertices();
				double hullVolume = 0;
				
				Vector3d v12 = new Vector3d();
				Vector3d v13 = new Vector3d();
				Vector3d cross = new Vector3d();
				
				for (int[] face : hullFaces)
				{
					Point3d v1 = hullPoints[face[0]];
					Point3d v2 = hullPoints[face[1]];
					Point3d v3 = hullPoints[face[2]];
					
					v12.sub(v2, v1);
					v13.sub(v3, v1);
					cross.cross(v12, v13);
					
					double surf = cross.length() * 0.5f;
					
					cross.normalize();
					hullVolume += surf * cross.x * (v1.x + v2.x + v3.x);
				}
				
				result[trackIndex][det.getT()] = 100 * contour.getDimension(2) / hullVolume;
			}
		}
		
		return result;
	}
	
	private Sequence rasterizeContours(Sequence s)
	{
		final Sequence sBin = new Sequence();
		sBin.setName(s.getName() + "_bin");
		sBin.setPixelSizeX(s.getPixelSizeX());
		sBin.setPixelSizeY(s.getPixelSizeY());
		sBin.setPixelSizeZ(s.getPixelSizeZ());
		
		ExecutorService service = Executors.newCachedThreadPool();
		
		ArrayList<Detection> detections = trackPool.getAllDetection();
		
		// retrieve the maximum t (no need to create empty stuff if the process was interrupted)
		
		int maxT = -1;
		for (Detection det : detections)
			if (det.getT() > maxT) maxT = det.getT();
		
		for (int t = 0; t <= maxT; t++)
		{
			for (int z = 0; z < s.getSizeZ(); z++)
				sBin.setImage(t, z, new IcyBufferedImage(s.getSizeX(), s.getSizeY(), 1, DataType.USHORT));
			
			short cpt = 1;
			
			for (final Detection det : detections)
				if (det.getT() == t && det instanceof Mesh) ((Mesh) det).rasterize(sBin, cpt++, service);
		}
		
		service.shutdownNow();
		
		return sBin;
	}
	
	private void computeSpeedOverCurvature()
	{
		JFileChooser jfc = new JFileChooser();
		if (jfc.showSaveDialog(getPanel()) == JFileChooser.APPROVE_OPTION)
		{
			XlsManager xls;
			try
			{
				xls = new XlsManager(jfc.getSelectedFile());
				
				for (TrackSegment ts : trackPool.getTrackSegmentList())
				{
					int trackIndex = trackPool.getTrackIndex(ts);
					xls.createNewPage("Cell " + trackIndex);
					// System.out.println("Cell " + trackIndex);
					
					ArrayList<Detection> dets = ts.getDetectionList();
					
					int col = 0;
					
					for (int d = 1; d < dets.size(); d++, col += 2)
					{
						int row = 0;
						
						xls.setLabel(col, 0, "dK [" + (d - 1) + "-" + d + "]");
						xls.setLabel(col + 1, 0, "dV [" + (d - 1) + "-" + d + "]");
						// System.out.println("Interval " + (d - 1) + "-" + d + ":");
						Mesh prevCt = (Mesh) dets.get(d - 1);
						Mesh currCt = ((Mesh) dets.get(d));
												
						Vector3d globalDisplacement = new Vector3d(currCt.getMassCenter());
						globalDisplacement.sub(prevCt.getMassCenter());
						
						// register prevCt towards currCt
						for (Point3d prevPt : prevCt)
							if (prevPt != null) prevPt.add(globalDisplacement);
						
						Point3d currPt = new Point3d();
						
						for (Point3d prevPt : prevCt)
						{
							if (prevPt == null) continue;
							
							row++;
							
							currCt.getMinDistanceTo(prevPt, currPt);
							
							double dV = currPt.distance(prevPt) / trackPool.getDisplaySequence().getTimeInterval();
							double dK = (prevCt.getCurvature(prevPt) - currCt.getCurvature(currPt)) / trackPool.getDisplaySequence().getTimeInterval();
							
							xls.setNumber(col, row, dK);
							xls.setNumber(col + 1, row, dV);
							// System.out.println(dK + "\t" + dV);
						}
						// System.out.println();
						// System.out.println();
						// System.out.println();
					}
					
					// System.out.println();
				}
				
				xls.SaveAndClose();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void computeConfinement()
	{
		for (TrackSegment ts : trackPool.getTrackSegmentList())
		{
			int trackIndex = trackPool.getTrackIndex(ts);
			
			Point3d minBB = new Point3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point3d maxBB = new Point3d();
			
			for (Detection det : ts.getDetectionList())
			{
				double x = det.getX(), y = det.getY(), z = det.getZ();
				
				if (x < minBB.x) minBB.x = x;
				if (x > maxBB.x) maxBB.x = x;
				
				if (y < minBB.y) minBB.y = y;
				if (y > maxBB.y) maxBB.y = y;
				
				if (z < minBB.z) minBB.z = z;
				if (z > maxBB.z) maxBB.z = z;
			}
			
			minBB.x *= trackPool.getDisplaySequence().getPixelSizeX();
			minBB.y *= trackPool.getDisplaySequence().getPixelSizeY();
			minBB.z *= trackPool.getDisplaySequence().getPixelSizeZ();
			
			maxBB.x *= trackPool.getDisplaySequence().getPixelSizeX();
			maxBB.y *= trackPool.getDisplaySequence().getPixelSizeY();
			maxBB.z *= trackPool.getDisplaySequence().getPixelSizeZ();
			
			System.out.println("Track Segment #" + trackIndex + ": ");
			System.out.println("  min BB = " + minBB.toString());
			System.out.println("  max BB = " + maxBB.toString());
			
			maxBB.sub(minBB);
			
			System.out.println("  confinement = " + (maxBB.x * maxBB.y * maxBB.z));
		}
	}
	
	@Override
	public void displaySequenceChanged()
	{
		
	}
}

