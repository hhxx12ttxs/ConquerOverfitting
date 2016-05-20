package lk.sliit.sep.qv2plugin.finalUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.Border;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import lk.sliit.sep.qv2plugin.executionEngine.HighlightAndScreenShot;
import lk.sliit.sep.qv2plugin.pdf.Pdf;
import lk.sliit.sep.qv2plugin.utilities.OSValidator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.Rotation;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

import de.javasoft.plaf.synthetica.SyntheticaBlueLightLookAndFeel;

import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ReportGUI extends javax.swing.JFrame {

	 private javax.swing.JPanel jPanel1;
	 private javax.swing.JScrollPane jScrollPane1;
	 private javax.swing.JScrollPane jScrollPane2;
	 private javax.swing.JTable jTable1;   
	 private javax.swing.JButton jButton1;
	 private javax.swing.JButton jButton2;
	 private javax.swing.JLabel jLabel1;
	 
	 private Vector <String> tableHeader;
	 private Vector <Vector<String>> tableContent;
	 private static HashMap hm;
	 private static WebDriver wb;
	 private CopyOnWriteArrayList<String> highlightedElements = new CopyOnWriteArrayList<String>();
	 private JPanel chartPanel;
	 private JPanel summaryPanel;
	 private JLabel lblElementsHavePassed;
	 private JLabel percentLabel;
	 private JScrollPane ChartScrollPane;
	 private JButton backButton;
	 
	 private ValidatorGUI validatorGui;
	 
	 public ReportGUI(HashMap hm, WebDriver wb, ValidatorGUI valiGui) {
		try {
			UIManager.setLookAndFeel(new SyntheticaBlueLightLookAndFeel());
		} catch (Exception e) {
			e.printStackTrace();
		}
		initComponents();
		tableHeader = new Vector<String>();
		tableContent = new Vector<Vector<String>>();
		this.wb = wb;
		
		this.validatorGui = valiGui;

		tableHeader.add("Rule Element Name");
		tableHeader.add("Xpath");
		tableHeader.add("Verification");
		tableHeader.add("Status");
		tableHeader.add("View");

		this.hm = hm;
		loadTable(hm);
	    }
	 
	 public ReportGUI() { //Created for testing purposes
			try {
				UIManager.setLookAndFeel(new SyntheticaBlueLightLookAndFeel());
			} catch (Exception e) {
				e.printStackTrace();
			}
			initComponents();
			tableHeader = new Vector<String>();
			tableContent = new Vector<Vector<String>>();
			
			validatorGui = null;
			
			//Test Driver Initialization
			String url = "http://www.google.com";
	        String driverPath = "";        
	        
	        //Validate OS
			try {
				if (OSValidator.isMac()) {
					driverPath = new File("./Drivers/OSXDriver/chromedriver")
							.getCanonicalPath(); // For OSX
				} else if (OSValidator.isWindows()) {
					driverPath = new File("./Drivers/WinDriver/chromedriver.exe")
							.getCanonicalPath(); // For Windows
				} else {
					JOptionPane.showMessageDialog(null,
							"Selenium Webdriver loading failed!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
			System.setProperty("webdriver.chrome.driver", driverPath);
	    	this.wb = new ChromeDriver();
	    	this.wb.get(url);
	    	
	    	//End of Test Driver Initialization

			tableHeader.add("Rule Element Name");
			tableHeader.add("Xpath");
			tableHeader.add("Verification");
			tableHeader.add("Status");
			tableHeader.add("View");

//			Test Data
			
			HashMap<String, String> resultMap = new HashMap<String, String>();
			
			String key1 = "Verify Background Attachment:/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/div[3]/div[1]/div[1]/div[1]/form[1]/fieldset[2]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[2]/div[1]/input[1]";
			String value1 = "Verification Unsuccessful: /html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/div[3]/div[1]/div[1]/div[1]/form[1]/fieldset[2]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[2]/div[1]/input[1]: rert";
			resultMap.put(key1, value1);
			String key2 = "Verify Background Color:/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/div[3]/div[1]/div[1]/div[1]/form[1]/fieldset[2]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[2]/div[1]/input[1]";
			String value2 = "Verification Successful: /html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/div[3]/div[1]/div[1]/div[1]/form[1]/fieldset[2]/div[1]/div[1]/div[1]/table[1]/tbody[1]/tr[1]/td[2]/div[1]/input[1]: bert";
			resultMap.put(key2, value2);
			
//			End of Test Data
			
			this.hm = resultMap;
			loadTable(this.hm);
		    }
	    
	    private void loadTable(HashMap<String, String> mp){
	                
	        Iterator it = mp.entrySet().iterator();
	        Vector<String> validationStrings = new Vector<String>();
	      
	        int i=0;
	        while (it.hasNext()){
	           Map.Entry pairs = (Map.Entry)it.next();
	            //Map        
	            String key = pairs.getKey().toString().split(":")[0];
	            String value = pairs.getValue().toString();
	            
	            String[] valArray = value.split(":");
	            
	            validationStrings.add(valArray[0]);
	            
	            //Test Prints
	            System.out.println("\n\n/******* Iterator Entry Start *******/\n");
	            System.out.println("Hashmap Key: " + pairs.getKey().toString());
	            System.out.println("Hashmap Value: " + value);
	            System.out.println("\n");
	            System.out.println("String Key(Verification): " + key);
	            
	            for(int j=0; j<valArray.length; j++){
	            	System.out.println("ValArray value["+j+"]: " + valArray[j]);
	            }
	            
	            System.out.println("\n");
	            System.out.println("\n\n/******* Iterator Entry End *******/");
	            //End of Test Prints
	            
	           // if (valArray[1].equals(""))
	            
	            
	           Vector<String> tblRow = new Vector<String>();
	            
	            tblRow.add(valArray[2]);		//elementname
	            tblRow.add(valArray[1]);		//xpath
	            tblRow.add(key);	            //verification 
	            tblRow.add(valArray[0]);		//status
	            tblRow.add("View Element");            
	            tableContent.add(tblRow);
	            
	        }
	        
	        jTable1.removeAll();
	        jTable1.setModel(new javax.swing.table.DefaultTableModel(tableContent, tableHeader));
	        jScrollPane2.setViewportView(jTable1);
	        
	        loadChart(validationStrings);
	    }
	    
	    /*
	     * Begin Chart
	     */
	    
	    private void loadChart(Vector<String> verification){
	    	int success = 0;
	    	int failure = 0;
	    	
	    	for (String ver : verification){
	    		if(ver.equalsIgnoreCase("Verification Successful")){
	    			success++;
	    		} else if (ver.equalsIgnoreCase("Verification Unsuccessful")){
	    			failure++;
	    		}
	    	}
	    	
	    	loadPercentLabel(failure, success);
	    	createPieChart("Validation Summary", success, failure);
	    }
	    
	    private void loadPercentLabel(int failure, int success){
	    	int total = success + failure;
	    	double fraction = ((double) success/(double) total);
	    	double percent = fraction*100;
//	    	int percentInt = (int) percent;
	    	String percentString = Double.toString(percent);
	    	percentLabel.setText(percentString+"%");
	    }
	    
	    private void createPieChart(String chartTitle, int successValue, int failureValue) {
	        PieDataset dataset = createDataset(successValue, failureValue);
	        JFreeChart chart = createChart(dataset, chartTitle);
	        ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(175, 100)); // default size
	        chartPanel.setBackground(new Color(47,91,130));
	        ChartScrollPane.setViewportView(chartPanel);
	        ChartScrollPane.setBackground(new Color(47,91,130));
	        this.chartPanel.setBackground(new Color(47,91,130));
	    }
	    
	    private  PieDataset createDataset(int successValue, int failureValue) {
	        DefaultPieDataset result = new DefaultPieDataset();
	        result.setValue("Failed", failureValue);
	        result.setValue("Successful", successValue);
	        return result;	        
	    }
	    
	    private JFreeChart createChart(PieDataset dataset, String title) {
	        
	        JFreeChart chart = ChartFactory.createPieChart3D(null,          // chart title
	            dataset,                // data
	            false,                   // include legend
	            true,
	            false);

	        PiePlot3D plot = (PiePlot3D) chart.getPlot();
	        plot.setStartAngle(90);
	        plot.setDirection(Rotation.CLOCKWISE);
	        plot.setForegroundAlpha(0.7f);
	        plot.setBackgroundPaint(/*new Color(191,212,231)*/Color.WHITE);
	        return chart;	        
	    }

	    /*
	     * End of Chart
	     */

	    @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
	    private void initComponents() {

	        jScrollPane1 = new javax.swing.JScrollPane();
	        jPanel1 = new javax.swing.JPanel();
	        jScrollPane2 = new javax.swing.JScrollPane();
	        jTable1 = new javax.swing.JTable();
	        jLabel1 = new javax.swing.JLabel();
	        jButton1 = new javax.swing.JButton();
	        jButton2 = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

	        jTable1.setModel(new javax.swing.table.DefaultTableModel(
	            new Object [][] {
	                {null, null, null, null},
	                {null, null, null, null},
	                {null, null, null, null},
	                {null, null, null, null}
	            },
	            new String [] {
	                "Verification", "Rule Element Name", "Status", "View"
	            }
	        ));
	        
	        jTable1.setRowHeight(60);
	        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                jTable1MouseClicked(evt);
	            }
	        });
	        jScrollPane2.setViewportView(jTable1);
	        
	        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
	        jLabel1.setText("Verification Report");

	        jButton1.setText("Save as PDF");
	        jButton1.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jButton1ActionPerformed(evt);
	            }
	        });

	        jButton2.setText("Finish");
	        jButton2.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jButton2ActionPerformed(evt);
	            }
	        });
	        
	        chartPanel = new JPanel();
	        
	        summaryPanel = new JPanel();
	        
	        backButton = new JButton("Back");
	        backButton.setVisible(false);
	        backButton.addActionListener(new ActionListener() {
	        	public void actionPerformed(ActionEvent e) {
//	        		backButtonActionPerformed(e);
	        	}
	        });

	        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
	        jPanel1Layout.setHorizontalGroup(
	        	jPanel1Layout.createParallelGroup(Alignment.LEADING)
	        		.addGroup(jPanel1Layout.createSequentialGroup()
	        			.addGap(23)
	        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
	        				.addGroup(jPanel1Layout.createSequentialGroup()
	        					.addGap(89)
	        					.addComponent(summaryPanel, GroupLayout.PREFERRED_SIZE, 386, GroupLayout.PREFERRED_SIZE)
	        					.addGap(32)
	        					.addComponent(chartPanel, GroupLayout.PREFERRED_SIZE, 272, GroupLayout.PREFERRED_SIZE))
	        				.addGroup(jPanel1Layout.createParallelGroup(Alignment.TRAILING, false)
	        					.addGroup(jPanel1Layout.createSequentialGroup()
	        						.addComponent(jButton1, GroupLayout.PREFERRED_SIZE, 132, GroupLayout.PREFERRED_SIZE)
	        						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	        						.addComponent(backButton, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)
	        						.addPreferredGap(ComponentPlacement.RELATED)
	        						.addComponent(jButton2, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE))
	        					.addComponent(jLabel1, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
	        					.addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 886, GroupLayout.PREFERRED_SIZE)))
	        			.addContainerGap(24, Short.MAX_VALUE))
	        );
	        jPanel1Layout.setVerticalGroup(
	        	jPanel1Layout.createParallelGroup(Alignment.TRAILING)
	        		.addGroup(jPanel1Layout.createSequentialGroup()
	        			.addContainerGap()
	        			.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
	        			.addPreferredGap(ComponentPlacement.RELATED)
	        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
	        				.addComponent(chartPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	        				.addComponent(summaryPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        			.addPreferredGap(ComponentPlacement.RELATED)
	        			.addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE)
	        			.addPreferredGap(ComponentPlacement.UNRELATED)
	        			.addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
	        				.addComponent(jButton2)
	        				.addComponent(jButton1)
	        				.addComponent(backButton))
	        			.addContainerGap())
	        );
	        
	        ChartScrollPane = new JScrollPane();
	        
	        GroupLayout gl_chartPanel = new GroupLayout(chartPanel);
	        gl_chartPanel.setHorizontalGroup(
	        	gl_chartPanel.createParallelGroup(Alignment.LEADING)
	        		.addGroup(Alignment.TRAILING, gl_chartPanel.createSequentialGroup()
	        			.addContainerGap()
	        			.addComponent(ChartScrollPane, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
	        			.addContainerGap())
	        );
	        gl_chartPanel.setVerticalGroup(
	        	gl_chartPanel.createParallelGroup(Alignment.LEADING)
	        		.addGroup(Alignment.TRAILING, gl_chartPanel.createSequentialGroup()
	        			.addContainerGap()
	        			.addComponent(ChartScrollPane, GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
	        			.addContainerGap())
	        );
	        chartPanel.setLayout(gl_chartPanel);
	        
	        JLabel lblSummary = new JLabel("Summary");
	        lblSummary.setFont(new java.awt.Font("Lucida Grande", 0, 14));
	        
	        percentLabel = new JLabel("0%");
	        percentLabel.setFont(new java.awt.Font("Lucida Grande", 0, 48));
	        percentLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        
	        lblElementsHavePassed = new JLabel("Elements have passed the test!");
	        lblElementsHavePassed.setFont(new java.awt.Font("Lucida Grande", 0, 14));
	        
	        GroupLayout gl_summaryPanel = new GroupLayout(summaryPanel);
	        gl_summaryPanel.setHorizontalGroup(
	        	gl_summaryPanel.createParallelGroup(Alignment.LEADING)
	        		.addGroup(gl_summaryPanel.createSequentialGroup()
	        			.addGap(89)
	        			.addComponent(lblElementsHavePassed)
	        			.addContainerGap(88, Short.MAX_VALUE))
	        		.addGroup(gl_summaryPanel.createSequentialGroup()
	        			.addGap(17)
	        			.addComponent(lblSummary)
	        			.addContainerGap(305, Short.MAX_VALUE))
	        		.addGroup(Alignment.TRAILING, gl_summaryPanel.createSequentialGroup()
	        			.addContainerGap(103, Short.MAX_VALUE)
	        			.addComponent(percentLabel, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE)
	        			.addGap(97))
	        );
	        gl_summaryPanel.setVerticalGroup(
	        	gl_summaryPanel.createParallelGroup(Alignment.TRAILING)
	        		.addGroup(gl_summaryPanel.createSequentialGroup()
	        			.addContainerGap()
	        			.addComponent(lblSummary)
	        			.addPreferredGap(ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
	        			.addComponent(percentLabel, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
	        			.addPreferredGap(ComponentPlacement.UNRELATED)
	        			.addComponent(lblElementsHavePassed)
	        			.addContainerGap())
	        );
	        summaryPanel.setLayout(gl_summaryPanel);
	        jPanel1.setLayout(jPanel1Layout);

	        jScrollPane1.setViewportView(jPanel1);

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jScrollPane1)
	                .addContainerGap())
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jScrollPane1)
	                .addContainerGap())
	        );

	        pack();
	    }// </editor-fold>                        

	    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {                                     
	        Integer i = jTable1.getSelectedRow();
	        String val = jTable1.getModel().getValueAt(i, 1).toString();
	        
	        HighlightAndScreenShot hs = new HighlightAndScreenShot();
	        
	        if (!highlightedElements.isEmpty()){
	        	Iterator<String> elemI = highlightedElements.iterator();
	        	while (elemI.hasNext()){
	        		String xpath = elemI.next();
	        		hs.removeHighlight(xpath, wb);
	        		highlightedElements.remove(xpath);
	        	}
	        }
	        
	        byte[] output = hs.highlightAndTakeScreenShot(val, wb);
	        highlightedElements.add(val);
	        
	        InputStream is = new ByteArrayInputStream(output);
	        
	    try {
	    	BufferedImage bImageFromConvert = ImageIO.read(is);
	        JFrame jf = new JFrame("Web Image");
	        JPanel jp = new JPanel();
	        jp.setPreferredSize(new Dimension(400, 800));
	        
	        ImageIcon imgIcon = new ImageIcon(bImageFromConvert);
	        JLabel jl = new JLabel();
	        jl.setIcon(imgIcon);
	        jp.add(jl);
	        jf.getContentPane().add(jp);
	        jf.pack();
	        jf.setVisible(true);
	    } catch (IOException ex) {
	        System.out.println(ex);
	    }
	    
	    	    
	    }
	    
	    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
	    	 JFileChooser jf = new JFileChooser();
	         int s = jf.showSaveDialog(this);
	         //ExtensionFileFilter e = new ExtensionFileFilter(SAVE_AS_IMAGE));

	         if (s == JFileChooser.APPROVE_OPTION) {
	             File f = jf.getSelectedFile();
	             String filePath = f.getAbsolutePath()+".pdf";
	             f = new File(filePath);
	             
	             try {
	                 Document document = new Document();
	                 Pdf fp = new Pdf();
	                 PdfWriter.getInstance(document, new FileOutputStream(f));
	                 document.open();

	                 //fp.addMetaData(document,jTextField1.toString(),jTextField2.toString());
	                 fp.addTitlePage(document, "Validation Results");
	                 fp.createReport(document, hm);

	                 document.close();

	                 JOptionPane.showMessageDialog(rootPane, "Succesfully Created");

	             } catch (Exception e) {
	                 e.printStackTrace();
	             }
	         }
	    }                                        

	    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                         
	        wb.close();
	        this.setVisible(false);
	        this.dispose();
	    }
	    
//	    private void backButtonActionPerformed(java.awt.event.ActionEvent evt){
//	    	int response = JOptionPane.showConfirmDialog(
//	    		    this,
//	    		    "Would you like to go back? All your current changes will be lost.",
//	    		    "Alert",
//	    		    JOptionPane.YES_NO_OPTION);
//	    	if (response == 0) {
//				validatorGui.setVisible(true);
//				this.dispose();
//			}
//	    }
	    
	    public static void main(String args[]) {
	        /* Set the Nimbus look and feel */
	        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
	        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
	         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
	         */
	        try {
	            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
	                if ("Nimbus".equals(info.getName())) {
	                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
	                    break;
	                }
	            }
	        } catch (ClassNotFoundException ex) {
	            java.util.logging.Logger.getLogger(ReportGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (InstantiationException ex) {
	            java.util.logging.Logger.getLogger(ReportGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (IllegalAccessException ex) {
	            java.util.logging.Logger.getLogger(ReportGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
	            java.util.logging.Logger.getLogger(ReportGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	        }
	        //</editor-fold>

	        /* Create and display the form */
	        java.awt.EventQueue.invokeLater(new Runnable() {
	            public void run() {
//	             new ReportGUI(hm, wb).setVisible(true);
	            	ReportGUI rgui = new ReportGUI();
	        		rgui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        		rgui.setLocationRelativeTo(null);
	        		rgui.setVisible(true);
	            }
	        });
	    }
}


