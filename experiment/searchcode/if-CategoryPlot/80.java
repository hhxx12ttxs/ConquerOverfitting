/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tema_barcharts;

import com.sun.jmx.snmp.BerDecoder;
import com.sun.org.apache.bcel.internal.generic.FCMPG;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.jfree.chart.*;
import org.jfree.*;
import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * 
 */
public class Tema_barcharts {
    public JFrame f;
    public JPanel p,p_val_axe;
    public JButton b,b_file_choser;
    public JButton b_cul_font_axe,b_cul_axe,b_cul_font_pe_barcharturi,b_cul_linii_val;
    public JButton b_bg_barchart;
    public JButton b_cul_bg;
    public JCheckBox cb_valori_axe,cb_valori_pe_barcharturi,cb_linii_axe;   
    public Color color_bg=(Color.WHITE);
    public ChartPanel barPanel;
    public JFreeChart barchart;
    public JTextField tf_val_axe;
    public JLabel lbl_val_axe;
    CategoryPlot cp ;
    ValueAxis axisr ;
    CategoryAxis axisc;
    Font font ;
        
    
   public Tema_barcharts() {
       frame_fc();
       //frame("populatie.xml");
   }
   
   public void frame_fc() {
       JFrame f;
       JPanel p=new JPanel(new GridLayout(1,1));
       String filename=null;
        f=new JFrame();
        
        
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(p); // or whatever...
        f.pack();
        f.setLocationRelativeTo(null);  // *** this will center your app ***
        f.setVisible(true);
        f.setSize(150, 150);
        
        
        JButton b_file_choser=new JButton("Alege fisier");
        b_file_choser.addActionListener(
            new ActionListener() {
                    
           @Override
           public void actionPerformed(ActionEvent ae) {
               String filename;
               JFileChooser chooser=new JFileChooser();
               chooser.showOpenDialog(null);
               File f = chooser.getSelectedFile();
               filename=f.getAbsolutePath();
               frame(filename);
               throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
           }
       });
        p.add(b_file_choser);
        //f.add(p);
       
   }
   
   
   public void frame(String filename) {
       f=new JFrame();
       p=new JPanel(new GridLayout(15,2));
       p_val_axe=new JPanel(new GridLayout(1,2));
       b= new JButton();
      // f.setBackground(Color.yellow);
//JPanel p=new JPanel();
       //f.add(p);
       f.setVisible(true);
       Toolkit tk = Toolkit.getDefaultToolkit();  
       int xSize = ((int) tk.getScreenSize().getWidth());  
       int ySize = ((int) tk.getScreenSize().getHeight());  
       f.setSize(xSize,ySize);  
       f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
       
             
       
       //loc in care vor fi inserate datele
       DefaultCategoryDataset barchartdata=new DefaultCategoryDataset();
       
       //datele vor fi inserate in tabel
       try {
 
	File fXmlFile = new File(filename);
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);
 
	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();
 
	//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
 
        /*iau toate elementele cu tagul tara*/
	NodeList nList = doc.getElementsByTagName("tara");
 
	//System.out.println("----------------------------");
 
         /*cat timp mai am elemente de citit din xml*/
	for (int temp = 0; temp < nList.getLength(); temp++) {
 
		Node nNode = nList.item(temp);
 
		//System.out.println("\nCurrent Element :" + nNode.getNodeName());
 
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
			Element eElement = (Element) nNode;
                        String nume_tara=new String(eElement.getAttribute("id"));
                        barchartdata.setValue(Long.parseLong(eElement.getElementsByTagName("populatie").item(0).getTextContent()),nume_tara,nume_tara);
                     
                        
                        //barchartdata.setValue(4000,"donatie3","miercuri");
                        
                        
			//System.out.println("Staff id : " + eElement.getAttribute("id"));
			//System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
			//System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
			//System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
			//System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
 
		}
	}
    } catch (Exception e) {
	e.printStackTrace();
    }

       
       
       barchart = ChartFactory.createBarChart("Populatia din lume", "Tari", "Populatie", barchartdata);
       barchart.setBackgroundPaint(Color.WHITE);
       
     
       CategoryPlot bar=new CategoryPlot();
       
      
       
       barPanel = new ChartPanel(barchart);
       barPanel.setVisible(true);
       
       
       /*setez butonul pentru alegere background pentru a afisa un JColorChooser atunci cand este apasat*/
       b=new JButton("Alege culoarea fundalului");
       b.addActionListener(
            new ActionListener() {
                    
           @Override
           public void actionPerformed(ActionEvent ae) {
               color_bg=JColorChooser.showDialog(null,"Pick Color",color_bg);
               if(color_bg==null)
                 color_bg=(Color)barchart.getBackgroundPaint();
               barchart.setBackgroundPaint(color_bg);    
               throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
           }
       });
       
       
       };
        
       
        
       
                           
                        
                    
        
       
        
       
       
   
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
   
        
         
        //creez un obiect de tipul Tema_barcharts
         new Tema_barcharts();
  }
        
    }
    


