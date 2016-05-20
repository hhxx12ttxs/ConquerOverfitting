// IMPORT COMMANDS

package jiggle.app;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jiggle.AbstractForceLaw;
import jiggle.Cell;
import jiggle.ConjugateGradients;
import jiggle.Edge;
import jiggle.ForceDirectedOptimizationProcedure;
import jiggle.ForceLaw;
import jiggle.ForceModel;
import jiggle.Graph;
import jiggle.InverseSquareVertexVertexRepulsionLaw;
import jiggle.QuadraticSpringLaw;
import jiggle.Vertex;
import jiggle.relo.HardPointedForceLaw;
import jiggle.relo.OriginAttractionLaw;
import jiggle.relo.PointedEdge;
import jiggle.relo.Subgraph;
import jiggle.relo.SubgraphForceLaw;

import org.eclipse.draw2d.geometry.Rectangle;

// JIGGLEFRAME CLASS

public class JiggleFrame extends Frame {
    // generated value for no structural changes
    private static final long serialVersionUID = 2686344419166502503L;

    int frameWidth = 800, frameHeight = 600;
    
    GraphGenerator generator = null;
    SettingsDlg settingsDlg = null;

    // execution data
    public ForceDirectedOptimizationProcedure optimizationProcedure;
    public Graph graph = null;

    // debug data
    public Map<ForceLaw,Color> dbgForceLaws = new HashMap<ForceLaw,Color>();
    public Map<Cell,String> dbgStrMap = new HashMap<Cell,String>();
    
    boolean isIterating = false, iteratingNow = false;

    int imageWidth = -1, imageHeight = -1;
    Image offScreenImage = null;
    
    public final int updateDelay = 1000;

    private double zoomFactor = 1;
    
    public int userTranslateX = 0;
    public int userTranslateY = 0;

    
    public JiggleFrame() {
        super("Jiggle");
    }
    
    public void init() {
        setLayout(null);
        this.addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent arg0) {
                running = false;
            }
            public void windowActivated(WindowEvent arg0) {}
            public void windowClosed(WindowEvent arg0) {}
            public void windowDeactivated(WindowEvent arg0) {}
            public void windowDeiconified(WindowEvent arg0) {}
            public void windowIconified(WindowEvent arg0) {}
            public void windowOpened(WindowEvent arg0) {}
           });
        setBackground(Color.white);
        //setFont(new Font("TimesRoman", Font.BOLD, 16));

        generator = new GraphGenerator(this, frameWidth / 2, frameHeight / 2);
        settingsDlg = new SettingsDlg(this, frameWidth / 2, frameHeight / 2);

        Menu menu;
        MenuItem mi;

        menu = new Menu("Commands");

        mi = new MenuItem("Generate Graph");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                isIterating = false;
                while (iteratingNow)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                generator.setVisible(true);
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Run");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (graph == null) {
                    return;
                }
                isIterating = false;
                while (iteratingNow) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
                isIterating = true;
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Step");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (graph == null) {
                    return;
                }
                isIterating = false;
                while (iteratingNow)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                isIterating = true;
                iteratingNow = true;
                optimizationProcedure.improveGraph();
                iteratingNow = false;
                isIterating = false;
                dumpGraphPoints();
                repaint();
            }});
        mi.setShortcut(new MenuShortcut(KeyEvent.VK_SPACE));
        menu.add(mi);
        
        mi = new MenuItem("Step x10");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (graph == null) {
                    return;
                }
                isIterating = false;
                while (iteratingNow)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                isIterating = true;
                iteratingNow = true;
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                optimizationProcedure.improveGraph();
                iteratingNow = false;
                isIterating = false;
                dumpGraphPoints();
                repaint();
            }});
        mi.setShortcut(new MenuShortcut(KeyEvent.VK_SPACE, true));
        menu.add(mi);
        
        mi = new MenuItem("Stop");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.err.println(((MenuItem)arg0.getSource()).getLabel());
                isIterating = false;
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Dump Points");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dumpGraphPoints();
            }
        });
        mi.setShortcut(new MenuShortcut(KeyEvent.VK_P));
        menu.add(mi);
        
        mi = new MenuItem("Dump Forces");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dumpGraphForces();
            }
        });
        mi.setShortcut(new MenuShortcut(KeyEvent.VK_F));
        menu.add(mi);
        
        mi = new MenuItem("Scramble");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.err.println(((MenuItem)arg0.getSource()).getLabel());
                boolean oldIteratingVal = isIterating;
                isIterating = false;
                while (iteratingNow)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                scramble();
                isIterating = oldIteratingVal;
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Zoom Out");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.err.println(((MenuItem)arg0.getSource()).getLabel());
                zoomFactor *= 2;
                offScreenImage = null;
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Zoom In");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.err.println(((MenuItem)arg0.getSource()).getLabel());
                zoomFactor /= 2;
                offScreenImage = null;
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Settings");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                isIterating = false;
                while (iteratingNow)
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                settingsDlg.setVisible(true);
        		if (settingsDlg.okClicked) {
                    setGraph(null);
            	}
                repaint();
            }});
        menu.add(mi);
        
        mi = new MenuItem("Debug");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	execDebug();
            }});
        menu.add(mi);

        mi = new MenuItem("Quit");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                running = false;
            }});
        menu.add(mi);
        
        MenuBar menuBar = new MenuBar();
        menuBar.add(menu);
        setMenuBar(menuBar);
        
        setSize(frameWidth, frameHeight);

        
        this.addMouseMotionListener(new MouseMotionListener() {
        	Point mouseDragPos = null;
			public void mouseDragged(MouseEvent me) {
				if (mouseDragPos != null) {
					Point oldPos = mouseDragPos;
					Point newPos = me.getPoint();
					userTranslateX += (newPos.x - oldPos.x) * zoomFactor;
					userTranslateY += (newPos.y - oldPos.y) * zoomFactor;
				}
				mouseDragPos = me.getPoint();
			}
			public void mouseMoved(MouseEvent arg0) {
				mouseDragPos = null;
			}
			});
        

        graph = defaultGraph();
        if (graph != null) {
            setGraph(graph);
	        scramble();
        }
        
        setVisible(true);
    }
    
    private void dumpGraphPoints() {
        if (!testVerticesIntegrity()) {
            return;
        }
        
        int n = graph.getNumberOfVertices();
        int d = graph.getDimensions();
        double negativeGradient[][] = new double[n][d];
        
        // get cumulative gradient
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                negativeGradient[i][j] = 0;
            }
            graph.vertices.get(i).gradientNdx = i;
        }
        for (Iterator<ForceLaw> flit = dbgForceLaws.keySet().iterator(); flit.hasNext();) {
            flit.next().apply(negativeGradient);
        }

        dumpGraphPoints(graph, negativeGradient);
        
        double tension = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < d; j++) {
                tension += Math.abs(negativeGradient[i][j]);
            }
        }
        System.err.println("tension: " + tension);
        
    }
    private boolean testVerticesIntegrity() {
        int n = graph.getNumberOfVertices();
        int d = graph.getDimensions();
        
        boolean graphIntegrity = true;
        
        for (int i = 0; i < n; i++) {
            if (!(graph.vertices.get(i) instanceof Vertex))
                continue;
            
            Vertex v = (Vertex) graph.vertices.get(i);
            double coords[] = v.getCoords();
            double size[] = v.getSize();
            double min[] = v.getMin();
            double max[] = v.getMax();
            double integrityErr = 0;
            for (int j = 0; j < d; j++) {
                integrityErr += size[j] - (max[j] - min[j]);
                integrityErr += min[j] - (coords[j] - size[j]/2);
            }
            if (integrityErr > 0) {
                if (integrityErr < 1e-5) {
                    System.err.println("Integrity failed: [" + i + "] Err: " + integrityErr + " continuing...");
                } else {
                    System.err.println("Integrity failed: [" + i + "] Err: " + integrityErr);
                    graphIntegrity = false;
                }
            }
            
        }
        return graphIntegrity;
        
    }
    private void dumpGraphPoints(Graph g, double[][] negativeGradient) {
        for (int i = 0; i < g.getNumberOfVertices(); i++) {
            Cell v = g.vertices.get(i);
            if (v instanceof Subgraph) {
                double coords[] = ((Subgraph)v).getBorderCoords();
                double size[] = ((Subgraph)v).getBorderSize();
                System.err.println("sg: " + 
                        "[" + v.gradientNdx + "] " +
                        (int) (coords[0]-size[0]/2) + "," + 
                        (int) (coords[1]-size[1]/2) + " - " + 
                        (int) (coords[0]+size[0]/2) + "," + 
                        (int) (coords[1]+size[1]/2) + " (x" + 
                        (int) size[0] + "," + 
                        (int) size[1] + ")      ==> " +
                        negativeGradient[v.gradientNdx][0] + "," +
                        negativeGradient[v.gradientNdx][1]
            	);
                System.err.print("  : " + "[" + v.gradientNdx + "] - ");
                dumpGraphChildren((Subgraph) v);
            } else {
                double coords[] = v.getCoords();
                double size[] = v.getSize();
                String cellStr = "";
                if (v.data != null) {
                    cellStr = v.data.toString();
                }
                System.err.println(
                        "[" + v.gradientNdx + "] " +
                        (int) (coords[0]-size[0]/2) + "," + 
                        (int) (coords[1]-size[1]/2) + " - " + 
                        (int) (coords[0]+size[0]/2) + "," + 
                        (int) (coords[1]+size[1]/2) + " (x" + 
                        (int) size[0] + "," + 
                        (int) size[1] + ")      ==> " +
                        negativeGradient[v.gradientNdx][0] + "," +
                        negativeGradient[v.gradientNdx][1] + "  " +
                        cellStr
                );
            }
        }
    }
    private void dumpGraphChildren(Subgraph g) {
        for (int i = 0; i < g.getNumberOfVertices(); i++) {
            Cell v = g.vertices.get(i);
            if (v instanceof Subgraph) {
                System.err.print("sg: " + "[" + v.gradientNdx + "] ");
            } else {
                System.err.print("[" + v.gradientNdx + "] ");
            }
        }
        System.err.println();
    }

    private void dumpGraphForces() {
        int n = graph.getNumberOfVertices();
        int d = graph.getDimensions();
        
        Map<ForceLaw,Object> gradientMap = new HashMap<ForceLaw,Object> (dbgForceLaws);
        
        for (Iterator<ForceLaw> flit = gradientMap.keySet().iterator(); flit.hasNext();) {
            double negativeGradient[][] = new double[n][d];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    negativeGradient[i][j] = 0;
                }
                graph.vertices.get(i).gradientNdx = i;
            }

            ForceLaw curFL = flit.next();
            boolean oldDebugState = curFL.printDebugInfo;
            curFL.printDebugInfo = true;
            curFL.apply(negativeGradient);
            curFL.printDebugInfo = oldDebugState;
            gradientMap.put(curFL, negativeGradient);
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (Iterator flit = gradientMap.keySet().iterator(); flit.hasNext();) {
                ForceLaw curFL = (ForceLaw) flit.next();
                double[][] negativeGradient = (double[][]) gradientMap.get(curFL);
                
                Cell v = graph.vertices.get(i);
                //double coords[] = v.getCoords();
                //double size[] = v.getSize();
                System.err.println(   
                        "[" + v.gradientNdx + "] " +
                        negativeGradient[i][0] + "," + 
                        negativeGradient[i][1] + "\t ==>" +  
                        curFL.getClass()
                );
                
            }
        }

        
    }
    
    @SuppressWarnings("unused")
	private String spc(int cnt) {
        String s = "";
        for (int i = 0; i < cnt; i++) {
            s += " ";
        }
        return s;
    }

    // TODO remove the while(running) loop from here, use a timer or something
    public boolean running = true;
    public void run() {
        
        long time = System.currentTimeMillis() / updateDelay;
        while (running) {

            Thread.yield();

            if (System.currentTimeMillis() / updateDelay > time) {
                repaint();
                time = System.currentTimeMillis() / updateDelay;
            }
            
            if (isIterating) {
                //long stTime = System.currentTimeMillis();
                iteratingNow = true;
                optimizationProcedure.improveGraph();
                iteratingNow = false;
                //System.err.println(" [" + (System.currentTimeMillis() - stTime) + " ms ]");                
            } else {
                try {
                    Thread.sleep(updateDelay);
                } catch (InterruptedException e) {
                }
            }
            

        }
    }
    
    // the graph that jiggle is started with
    private Graph defaultGraph() {
        // return null;
        //return scenario0();
        //return scenario1();
        //return scenario2();
        //return scenario4();

        //return scenario3();			// sub-graph testing scenario
        //return scenario3pp();
        //return scenario5();		// pointed forces testing scenario
        
        return scenarioRelo();
    }
    
    private Graph scenarioRelo() {
        
        Graph g = new Graph();
        Vertex v;
        Vertex v2;

        Subgraph sg0;
        sg0 = new Subgraph(g);
        sg0.data = "sg0";
		g.insertVertex(sg0);
        
        Subgraph sg; 
        sg = new Subgraph(g);
        sg.data = "sg";
		g.insertVertex(sg);
		sg0.insertVertex(sg);

        v = g.insertVertex();
		sg0.insertVertex(v);
		
        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v = g.insertVertex();
		sg.insertVertex(v);

        v2 = g.insertVertex();
		sg.insertVertex(v2);
		
		/*Edge e = */g.insertEdge(v, v2);

        v = g.insertVertex();
		/*e = */g.insertEdge(v, v2);

        v2 = g.insertVertex();
		/*e = */g.insertEdge(v, v2);

		v = g.insertVertex();
		sg.insertVertex(v);

		/*e = */g.insertEdge(v, v2);

		return g;
    }

    @SuppressWarnings("unused")
	private Graph scenario0() {
        Graph g = new Graph();
		Vertex grid [] [] = new Vertex [8] [8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				grid [i] [j] = g.insertVertex ();
			}
		}
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8 - 1; j++) {
				g.insertEdge (grid [i] [j], grid [i] [j+1]);
				g.insertEdge (grid [j] [i], grid [j+1] [i]);
			}
		}
		return g;
    }
    
    // does not work, nodes explode away. solving this should be easy
    @SuppressWarnings("unused")
	private Graph scenario1() {
        Graph g = new Graph();
		Vertex grid [] [] = new Vertex [8] [8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				grid [i] [j] = g.insertVertex ();
			}
		}
		return g;
    }

    @SuppressWarnings("unused")
	private Graph scenario2() {
        Graph g = new Graph();
		Vertex grid [] [] = new Vertex [8] [8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				grid [i] [j] = g.insertVertex ();
			}
		}
		// horizontal edges
		for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8 - 1; c++) {
                Edge e = new PointedEdge (g, grid[r][c], grid[r][c + 1], 0, true);
                g.insertEdge(e);
            }
        }
		// vertical edges
		for (int r = 0; r < 8 - 1; r++) {
            for (int c = 0; c < 8; c++) {
                Edge e = new PointedEdge(g, grid[r][c], grid[r + 1][c], 1, true);
                g.insertEdge(e);
            }
        }
		return g;
    }

    @SuppressWarnings("unused")
	private Graph scenario3() {
        Vertex[][] grid = null;
        Vertex[] chain = null;

        Graph g = new Graph();
        grid = new Vertex[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                grid[i][j] = g.insertVertex();
            }
        }
        // horizontal edges
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8 - 1; c++) {
                Edge e = new PointedEdge(g, grid[r][c], grid[r][c + 1], false);
                g.insertEdge(e);
            }
        }
        // vertical edges
        for (int r = 0; r < 8 - 1; r++) {
            for (int c = 0; c < 8; c++) {
                Edge e = new PointedEdge(g, grid[r][c], grid[r + 1][c], true);
                g.insertEdge(e);
            }
        }
		
        Subgraph sg = new Subgraph(g);
		g.insertVertex(sg);

		sg.insertVertex(grid[3][3]);
		sg.insertVertex(grid[3][4]);
		sg.insertVertex(grid[3][5]);
		sg.insertVertex(grid[4][3]);
		sg.insertVertex(grid[4][4]);
		sg.insertVertex(grid[4][5]);
		sg.insertVertex(grid[4][6]);
		sg.insertVertex(grid[5][3]);
		sg.insertVertex(grid[5][4]);
		sg.insertVertex(grid[5][5]);
		sg.insertVertex(grid[6][3]);
		sg.insertVertex(grid[6][4]);
		sg.insertVertex(grid[6][5]);
		sg.insertVertex(grid[6][6]);
		
        // add a horizontal chain
        chain = new Vertex[5];
        for (int i = 0; i < chain.length; i++) {
        	chain[i] = g.insertVertex();
        }
        for (int c = 0; c < chain.length - 1; c++) {
            Edge e = new PointedEdge(g, chain[c+1], chain[c], false);
            g.insertEdge(e);
        }
        Edge e = new PointedEdge(g, chain[0], grid[0][0], false);
        g.insertEdge(e);

		dbgStrMap.put(grid[0][0], "00");
		dbgStrMap.put(grid[0][7], "07");
		dbgStrMap.put(grid[7][0], "70");
		dbgStrMap.put(grid[7][7], "77");
		
		dbgStrMap.put(chain[0], "Right");
		dbgStrMap.put(chain[chain.length-1], "Left");
		
		return g;
    }
    
    @SuppressWarnings("unused")
	private Graph scenario3pp() {
        Vertex[][] grid = null;

        Graph g = new Graph();
        grid = new Vertex[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                grid[i][j] = g.insertVertex();
            }
        }
        // horizontal edges
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8 - 1; c++) {
                Edge e = new PointedEdge(g, grid[r][c], grid[r][c + 1], false);
                g.insertEdge(e);
            }
        }
        // vertical edges
        for (int r = 0; r < 8 - 1; r++) {
            for (int c = 0; c < 8; c++) {
                Edge e = new PointedEdge(g, grid[r][c], grid[r + 1][c], true);
                g.insertEdge(e);
            }
        }
		
        Subgraph sg = new Subgraph(g);
		g.insertVertex(sg);

		sg.insertVertex(grid[3][3]);
		sg.insertVertex(grid[3][4]);
		sg.insertVertex(grid[3][5]);
		sg.insertVertex(grid[4][3]);
		sg.insertVertex(grid[4][4]);
		sg.insertVertex(grid[4][5]);
		sg.insertVertex(grid[4][6]);
		sg.insertVertex(grid[5][3]);
		sg.insertVertex(grid[5][4]);
		sg.insertVertex(grid[5][5]);
		sg.insertVertex(grid[6][3]);
		sg.insertVertex(grid[6][4]);
		sg.insertVertex(grid[6][5]);
		sg.insertVertex(grid[6][6]);
		
        // add a horizontal chain
        Vertex[] chain = null;
        chain = new Vertex[5];
        for (int i = 0; i < chain.length; i++) {
        	chain[i] = g.insertVertex();
        }
        for (int c = 0; c < chain.length - 1; c++) {
            Edge e = new PointedEdge(g, chain[c+1], chain[c], false);
            g.insertEdge(e);
        }
		dbgStrMap.put(chain[0], "Right");
		dbgStrMap.put(chain[chain.length-1], "Left");
        
        Edge e = new PointedEdge(g, chain[0], grid[0][0], false);
        g.insertEdge(e);


        // add a secon chain
        chain = new Vertex[5];
        for (int i = 0; i < chain.length; i++) {
        	chain[i] = g.insertVertex();
        }
        for (int c = 0; c < chain.length - 1; c++) {
            Edge e2 = new PointedEdge(g, chain[c+1], chain[c], false);
            g.insertEdge(e2);
        }
		dbgStrMap.put(chain[0], "Right");
		dbgStrMap.put(chain[chain.length-1], "Left");
        
        Edge e2 = new PointedEdge(g, chain[0], grid[7][0], false);
        g.insertEdge(e2);
        
        
		dbgStrMap.put(grid[0][0], "00");
		dbgStrMap.put(grid[0][7], "07");
		dbgStrMap.put(grid[7][0], "70");
		dbgStrMap.put(grid[7][7], "77");
		
		
		return g;
    }
    
    @SuppressWarnings("unused")
	private Graph scenario4() {
        Graph g = new Graph();
		Vertex grid [] = new Vertex [8];
		for (int i = 0; i < 8; i++) {
			grid [i] = g.insertVertex ();
		}
		// horizontal edges
        for (int c = 0; c < 8 - 1; c++) {
            Edge e = new PointedEdge(g, grid[c], grid[c + 1], false);
            g.insertEdge(e);
        }

		dbgStrMap.put(grid[0], "left");
		dbgStrMap.put(grid[grid.length-1], "right");
		
        /*
        Vertex v9 = g.insertVertex ();
        Edge e;
        e = new PointedEdge (g, grid[0], v9, 0, true);
        g.insertEdge(e);
        e = new PointedEdge (g, v9, grid[7], 0, true);
        g.insertEdge(e);
        */

		return g;
    }

    @SuppressWarnings("unused")
	private Graph scenario5() {
        Graph g = new Graph();
		Vertex lrChain [] = new Vertex [2];
		for (int i = 0; i < lrChain.length; i++) {
            lrChain[i] = g.insertVertex();
        }

		Vertex tbChain [] = new Vertex [2];
		for (int i = 0; i < tbChain.length; i++) {
            tbChain[i] = g.insertVertex();
        }

		Vertex center = g.insertVertex ();

		
		// horizontal edges
        for (int c = 0; c < lrChain.length - 1; c++) {
            if (c==lrChain.length/2-1) {
                g.insertEdge(new PointedEdge(g, lrChain[c], center, false));
                g.insertEdge(new PointedEdge(g, center, lrChain[c + 1], false));
            } else {
                g.insertEdge(new PointedEdge(g, lrChain[c], lrChain[c + 1], false));
            }
        }
        
        // vert. egets
        for (int c = 0; c < tbChain.length - 1; c++) {
            if (c==tbChain.length/2-1) {
                g.insertEdge(new PointedEdge(g, tbChain[c], center, true));
                g.insertEdge(new PointedEdge(g, center, tbChain[c + 1], true));
            } else {
                g.insertEdge(new PointedEdge(g, tbChain[c], tbChain[c + 1], true));
            }
        }

		dbgStrMap.put(lrChain[0], "left");
		dbgStrMap.put(lrChain[lrChain.length-1], "right");
		dbgStrMap.put(tbChain[0], "top");
		dbgStrMap.put(tbChain[lrChain.length-1], "bottom");
		
		return g;
    }

	/**
	 * Sets graph and applies settings on the graph 
	 * @param g - if g is null, then settings are refreshed on the current graph
	 */
    public void setGraph(Graph g) {
        isIterating = false;
        while (iteratingNow)
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        if (g != null) {
            graph = g;
        }
        
        if (graph != null) {
            initSettings();
        }
    }
    
    public void initSettings() {
        SettingsDlg.Settings settings = settingsDlg.getSettings();
        
		int d = graph.getDimensions ();

		// initialize vertex width and heights
		for (int i = 0; i < graph.getNumberOfVertices(); i++) {
			double size [] = graph.vertices.get(i).getSize ();
			if (settings.useVertexSize) {
				size [0] = settings.vertexWidth;
				size [1] = settings.vertexHeight;
				if (d > 2) {
					double avg = (size [0] + size [1]) / 2;
					for (int j = 2; j < d; j++) size [j] = avg;
				}
			}
			else {
				for (int j = 0; j < d; j++) size [j] = 0;
			}
		}

		
		// let's override settings
        settings.forceModel = new ForceModel (graph);

        AbstractForceLaw attractionLaw = new QuadraticSpringLaw (graph, settings.prefEdgeLength);
    	settings.forceModel.addForceLaw (attractionLaw);
        dbgForceLaws.put(attractionLaw, Color.blue);

        AbstractForceLaw repulsionLaw = new InverseSquareVertexVertexRepulsionLaw (graph, settings.prefEdgeLength);
    	settings.forceModel.addForceLaw (repulsionLaw);
        dbgForceLaws.put(repulsionLaw, Color.green);

        ForceLaw pointedFrcLaw = new HardPointedForceLaw(graph,
				settings.prefEdgeLength*4, 
				attractionLaw,
				repulsionLaw);
    	settings.forceModel.addForceLaw(pointedFrcLaw);
        dbgForceLaws.put(pointedFrcLaw, Color.black);

        ForceLaw sgfl = new SubgraphForceLaw(graph, 
        		settings.prefEdgeLength,
        		attractionLaw, 
				repulsionLaw);
        settings.forceModel.addForceLaw(sgfl);
        dbgForceLaws.put(sgfl, Color.red);
        
        ForceLaw oafl = new OriginAttractionLaw(graph, 
        		settings.prefEdgeLength,
        		attractionLaw, 
				repulsionLaw);
        settings.forceModel.addForceLaw(oafl);
        dbgForceLaws.put(oafl, Color.black);
    	
		settings.optimizationProcedure = new ConjugateGradients(graph, settings.forceModel, 0.5, 0.2);

		
		optimizationProcedure = settings.optimizationProcedure;
    }

    
    void execDebug() {
    }

    void scramble() {
        int n = graph.getNumberOfVertices();
        double w = getSize().width, h = getSize().height;
        int d = graph.getDimensions();
        for (int i = 0; i < n; i++) {
            double coords[] = graph.vertices.get(i).getCoords();
            for (int j = 0; j < d; j++)
                coords[j] = Math.random() * w;
        }
        double sumX = 0, sumY = 0;
        for (int i = 0; i < n; i++) {
            double coords[] = graph.vertices.get(i).getCoords();
            sumX += coords[0];
            sumY += coords[1];
        }
        for (int i = 0; i < n; i++) {
            //Cell v = graph.vertices.get(i);
            double coords[] = graph.vertices.get(i).getCoords();
            coords[0] += (w / 2) - (sumX / n);
            coords[1] += (h / 2) - (sumY / n);
            
            graph.vertices.get(i).recomputeBoundaries();
        }
    }
    
    double[][] displayNegativeGradient = null;
    
    public void flushCache() {
        displayNegativeGradient = null;
    }
    
    public double largeGradient = 1E6;
    
    public boolean showStrings = true;
    public boolean showDbgForces = true;
    
    /**
     * Draws graphics such that:
     *  sub-graph children: will consist of two rectangles with a 2px offset.
     *  sub-graph coords & size: will consist of a reactangle with diagonals drawn in
     *  sub-graphs: are drawn with red lines
     */
    @Override
    public void update(Graphics g) {
        if (graph == null)
            return;
        
        if (displayNegativeGradient == null) {
            int n = graph.getNumberOfVertices();
            int d = graph.getDimensions();
            displayNegativeGradient = new double[n][d];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    displayNegativeGradient[i][j] = 0;
                }
                graph.vertices.get(i).gradientNdx = i;
            }
        }
        if (!isIterating) {
            initDbgForces(displayNegativeGradient);
        }

        int width = getSize().width, height = getSize().height;
        Graphics offScreenGraphics;
        if ((width != imageWidth) || (height != imageHeight) || offScreenImage == null) {
            imageWidth = width;
            imageHeight = height;
            offScreenImage = createImage((int) (width * zoomFactor), (int) (height * zoomFactor));
            offScreenGraphics = offScreenImage.getGraphics();
        } else {
            offScreenGraphics = offScreenImage.getGraphics();            
        }
        offScreenGraphics.setColor(Color.white);
        offScreenGraphics.fillRect(0, 0, (int) (width * zoomFactor), (int) (height * zoomFactor));

        offScreenGraphics.translate(userTranslateX, userTranslateY);

        Color vertexColor;
        if (isIterating) {
            vertexColor = Color.red;
        } else {
            vertexColor = Color.black;
        }

        // regular vertices
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            Cell v = graph.vertices.get(i);
            double coords[] = v.getCoords();
            int x = (int) coords[0];
            int y = (int) coords[1];
			double size [] = v.getSize ();
            int w = (int) size[0];
            int h = (int) size[1];
            if (displayNegativeGradient != null
                    && Math.abs(displayNegativeGradient[i][0]) + Math.abs(displayNegativeGradient[i][1]) > largeGradient) {
                offScreenGraphics.setColor(Color.blue);
            } else {
                offScreenGraphics.setColor(vertexColor);
            }
            offScreenGraphics.drawRect(x - w / 2, y - h / 2, w, h);
            
            if (dbgStrMap.containsKey(v) && showStrings) {
                Rectangle r = v.getBounds();
                offScreenGraphics.drawString("[" + i + "]" + dbgStrMap.get(v).toString(), r.x, r.y);
            } else if (v.data != null && showStrings) {
                Rectangle r = v.getBounds();
                offScreenGraphics.drawString("[" + i + "]" + v.data.toString(), r.x, r.y);
            }
        }
        // subgraphs vertices
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            if (!(graph.vertices.get(i) instanceof Subgraph)) {
                continue;
            }
            Subgraph sg = (Subgraph) graph.vertices.get(i);

            // draw the sub-graph center
            {
	            double coords[] = sg.getCoords();
	            int x = (int) coords[0];
	            int y = (int) coords[1];
				double size [] = sg.getSize ();
	            int w = (int) size[0];
	            int h = (int) size[1];
	            //offScreenGraphics.drawRect(x - w / 2, y - h / 2, w, h);
	            offScreenGraphics.drawLine(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
	            offScreenGraphics.drawLine(x + w / 2, y - h / 2, x - w / 2, y + h / 2);
            }
            
            // draw the sub-graph border
            {
                offScreenGraphics.setColor(Color.red);

                double coords[] = sg.getBorderCoords();
	            int x = (int) coords[0];
	            int y = (int) coords[1];
				double size [] = sg.getBorderSize ();
	            int w = (int) size[0];
	            int h = (int) size[1];
	            offScreenGraphics.drawRect(x - w / 2, y - h / 2, w, h);

	            offScreenGraphics.setColor(vertexColor);
            }

            for (int j=0; j<sg.getNumberOfVertices(); j++) {
                Cell v = sg.vertices.get(j);
                double coords[] = v.getCoords();
                int x = (int) coords[0];
                int y = (int) coords[1];
    			double size [] = v.getSize ();
                int w = (int) size[0];
                int h = (int) size[1];
                offScreenGraphics.drawRect(2+x - w / 2, 2+y - h / 2, w, h);
            }

        }
        // edges
        for (Edge e : graph.edges) {
            Vertex from = e.getFrom(), to = e.getTo();
            double f[] = from.getCoords(), t[] = to.getCoords();
            int x1 = (int) f[0], y1 = (int) f[1];
            int x2 = (int) t[0], y2 = (int) t[1];
            offScreenGraphics.drawLine(x1, y1, x2, y2);
        }
        
        if (!isIterating && showDbgForces) {
            displayDbgForces(offScreenGraphics, displayNegativeGradient);
        }

        // draw origin
    	offScreenGraphics.setColor(Color.white);
        offScreenGraphics.drawLine(-15, 1, 15, 1);
        offScreenGraphics.drawLine(-15, -1, 15, -1);
        offScreenGraphics.drawLine(1, -15, 1, 15);
        offScreenGraphics.drawLine(-1, -15, -1, 15);
    	offScreenGraphics.setColor(Color.black);
        offScreenGraphics.drawLine(-15, 0, 15, 0);
        offScreenGraphics.drawLine(0, -15, 0, 15);
        
        g.drawImage(offScreenImage, 0, 0, width, height, Color.white, this);
    }

    static double sqrt(double d) {
        return Math.sqrt(d);
    }

    static double square(double d) {
        return d * d;
    }

    static double cube(double d) {
        return d * d * d;
    }

    private void initDbgForces(double[][] negativeGradient) {
        // get cumulative gradient
        for (Iterator<ForceLaw> flit = dbgForceLaws.keySet().iterator(); flit.hasNext();) {
            flit.next().apply(negativeGradient);
        }
        
    }
    
    private void displayDbgForces(Graphics offScreenGraphics, double[][] negativeGradient) {
        int n = graph.getNumberOfVertices();
        int d = graph.getDimensions();

        // get norm
        double norm = Double.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            norm = Math.max(norm, square(negativeGradient[i][0]) + square(negativeGradient[i][1]));
        }
        
        //System.err.println(norm);
        norm = sqrt(norm) / 100;

        // global application
        offScreenGraphics.setColor(new Color(217,217,25));
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            Cell v = graph.vertices.get(i);
            double[] vCoordsFrom = v.getCoords();
            //double[] vGradient = negativeGradient[i];
            double[] vCoordsTo = new double[2];
            vCoordsTo[0] = vCoordsFrom[0] + negativeGradient[i][0] / norm;
            vCoordsTo[1] = vCoordsFrom[1] + negativeGradient[i][1] / norm;
            int x1 = (int) vCoordsFrom[0], y1 = (int) vCoordsFrom[1];
            int x2 = (int) vCoordsTo[0], y2 = (int) vCoordsTo[1];
            offScreenGraphics.drawLine(x1 - 2, y1 - 2, x2 - 2, y2 - 2);
            offScreenGraphics.drawLine(x1 - 1, y1 - 1, x2 - 1, y2 - 1);
            offScreenGraphics.drawLine(x1 + 0, y1 + 0, x2 + 0, y2 + 0);
            offScreenGraphics.drawLine(x1 + 1, y1 + 1, x2 + 1, y2 + 1);
            offScreenGraphics.drawLine(x1 + 2, y1 + 2, x2 + 2, y2 + 2);
        }

        // per law application
        for (Iterator flit = dbgForceLaws.keySet().iterator(); flit.hasNext();) {
            ForceLaw curFL = (ForceLaw) flit.next();
            offScreenGraphics.setColor((Color) dbgForceLaws.get(curFL));
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < d; j++) {
                    negativeGradient[i][j] = 0;
                }
            }
            curFL.apply(negativeGradient);
            
            for (int i = 0; i < graph.vertices.size(); i++) {
                if (!(graph.vertices.get(i) instanceof Vertex)) {
                    continue;
                }
                Vertex v = (Vertex) graph.vertices.get(i);
                double[] vCoordsFrom = v.getCoords();
                double[] vCoordsTo = new double[2];
                vCoordsTo[0] = vCoordsFrom[0] + negativeGradient[i][0] / norm;
                vCoordsTo[1] = vCoordsFrom[1] + negativeGradient[i][1] / norm;
                int x1 = (int) vCoordsFrom[0], y1 = (int) vCoordsFrom[1];
                int x2 = (int) vCoordsTo[0], y2 = (int) vCoordsTo[1];
                //System.err.println(curFL.getClass().getName() + ":" + x1 +
                // "," + y1 + "," + x2 + "," + y2);
                offScreenGraphics.drawLine(x1, y1, x2, y2);
            }
        }
    }

    public static void main(String[] args) {
        JiggleFrame jf = new JiggleFrame();
        jf.launchJiggle();
    }

    public void launchJiggle() {
        init();
        launch();
    }

    public void launch() {
        final JiggleFrame jf = this;
        Thread t = new Thread(new Runnable() {
            public void run() {
                jf.running = true;
                jf.run();
                jf.dispose();
            }});
        
        t.start();
    }

}
