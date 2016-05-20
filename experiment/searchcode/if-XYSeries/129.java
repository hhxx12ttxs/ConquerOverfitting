package SYSC4005Gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TextArea;
import java.awt.TextField;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JMenuBar;
import java.awt.Button;
import java.awt.Color;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Checkbox;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JRadioButton;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYAnnotationEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class kFrame extends JFrame {

	private JPanel contentPane;
	private JTextField txtK;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;
	private JLabel lblIteration;
	private JTextField textField_8;
	private JTextField textField_9;
	private JTextField textField_10;
	private JLabel lblP;
	private JTextField textField_11;
	private JTextField textField_12;
	private JTextField textField_13;
	private JLabel lblMeanvalue;
	private JLabel lblUpperci;
	private JLabel lblLowerci;
	private JTextArea textField_14;
	private JTextArea textField_15;
	private JTextArea textField_16;
	public kFrame frameThis;
	private JLabel lblP_1;
	private JTextField textField_17;
	private JTextField textField_18;
	private JTextField textField_19;
	private JLabel lblP_2;
	private JTextField textField_20;
	private JTextField textField_21;
	private JTextField textField_22;
	private JLabel lblP_3;
	private JTextField textField_23;
	private JTextField textField_24;
	private JTextField textField_25;
	private JLabel lblP_4;
	private JTextField textField_26;
	private JTextField textField_27;
	private JTextField textField_28;
	private JSeparator separator_1;
	private JTextField textField_29;
	private JTextField textField_30;
	private JTextField textField_31;
	private JTextField textField_32;
	private JTextField textField_33;
	private JTextField textField_34;
	private JTextField textField_35;
	private JTextField textField_36;
	private JTextField textField_37;
	private JTextField textField_38;
	private JTextField textField_39;
	private JTextField textField_40;
	private JTextField textField_41;
	private JTextField textField_42;
	private JTextField textField_43;
	private JTextField textField_44;
	private JTextField textField_45;
	private JTextField textField_46;
	private Button button_5;
	private JTextField textField_47;
	private JTextField textField_48;
	private JTextField textField_49;
	private JTextField textField_50;
	private JTextField textField_51;
	private JTextField textField_52;
	private JTextField textField_53;
	private JTextField textField_54;
	private JTextField textField_55;
	private JSeparator separator_2;
	private JSeparator separator_3;
	private JSeparator separator_4;
    private Checkbox checkbox;
	private JSeparator separator;
	private JLabel lblTopology;
	private Button button_2;
	private JLabel lblSysc;
	private Button button;
	private Button button_1;
	private JLabel lblLambda;
	private JLabel lblIterations;
	private JLabel lblQueueLength;
	private JLabel lblSysc_1;
	private JLabel lblProjectProperties;
	private Button button_3;
	private Button button_4;
	private JLabel lblNewLabel;
	private JTextField textField_56;
	private JTextField textField_57;
	private Checkbox checkbox_1;
	private JProgressBar progressBar;
	private JTextField textField_58;
	private JButton btnPlotData;
	private JButton btnInfo;
	private Checkbox checkbox_2;
	
	
	private double lambds[];

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					kFrame frame = new kFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public kFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 867, 551);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblSysc = new JLabel("SYSC 4005 - Project Simulator");
		lblSysc.setFont(new Font("Calibri", Font.BOLD, 21));
		lblSysc.setBounds(311, 22, 277, 16);
		contentPane.add(lblSysc);
		frameThis = this;
		button = new Button("Topology1 - Round Robin");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 lblNewLabel.setText("Running");
			int timeSlots = Integer.parseInt(textField_2.getText());
			int N = Integer.parseInt(textField_5.getText());
			 double probability[] = new double[N];
			 int iterations= Integer.parseInt(textField_8.getText());
			 double lambda1 = Double.parseDouble(txtK.getText());
			 probability[0] = Double.parseDouble(textField_13.getText());
			 probability[1]	= Double.parseDouble(textField_17.getText());
			 probability[2] = Double.parseDouble(textField_20.getText());
			 probability[3]	= Double.parseDouble(textField_23.getText());
			 probability[4]	= Double.parseDouble(textField_26.getText());	 
			 
			 lambds = new double[10];
			 if(checkbox.getState())
			 {
				 for (int i = 1; i <= 10; i++) {
					 //probability[i] = Double.parseDouble(textField_11.getText());
					 lambds[i-1] = lambda1*i;
				 }
			 }
			 
			 if(checkbox.getState())
			 {
				 /*
				 double lambdas[] = new double[10]; 
				 for (int i = 0; i < N; i++) {
						//probability[i] = Double.parseDouble(textField_11.getText());
						lambdas[i] = lambda*(i+1);
					}
				Topology1 top = new Topology1(timeSlots, probability, lambdas, 1, iterations,frameThis);
				top.runAndPrintToFile("Topology3-LCQ.txt");
				*/
				 for(int j=0;j<10;j++)
				 {
					 double lambda = lambda1*(j+1);
					 double lambdas[] = new double[N];
					
					 
					 if(j==9)
					 {
						 lblNewLabel.setText("Done !!");
					 }

				
					 
					 for (int i = 0; i < N; i++) {
						 //probability[i] = Double.parseDouble(textField_11.getText());
						 lambdas[i] = lambda;
					 }
					 progressBar.setValue((j+1)*10);
					 if(checkbox_1.getState())
					 {
						 Topology1 top = new Topology1(true,Double.parseDouble(textField_56.getText()),Double.parseDouble(textField_57.getText()),timeSlots, probability, lambdas, 1, iterations,frameThis);
						 top.runAndPrintToFile("Topology3-LCQ.txt");
						 
					 }
					 else
					 {
					 Topology1 top = new Topology1(timeSlots, probability, lambdas, 1, iterations,frameThis);
					 top.runAndPrintToFile("Topology3-LCQ.txt");
					 }
				  }
			 }
			 else
			 {
				 double lambdas[] = new double[N];
				 for (int i = 0; i < N; i++) {
						//probability[i] = Double.parseDouble(textField_11.getText());
						lambdas[i] = lambda1;
					}
				 lblNewLabel.setText("Done !!");
				 if(checkbox_1.getState())
				 {
					 Topology1 top = new Topology1(true,Double.parseDouble(textField_56.getText()),Double.parseDouble(textField_57.getText()),timeSlots, probability, lambdas, 1, iterations,frameThis);
					 top.runAndPrintToFile("Topology3-LCQ.txt");
					 progressBar.setValue(100);
				 }
				 else
				 {
				 Topology1 top = new Topology1(timeSlots, probability, lambdas, 1, iterations,frameThis);
				 top.runAndPrintToFile("Topology3-LCQ.txt");
				 progressBar.setValue(100);
				 }
//				Topology1 top = new Topology1(timeSlots, probability, lambdas, 1, iterations,frameThis);
//				top.runAndPrintToFile("Topology3-LCQ.txt");
				
			 }
			}
		});
		button.setBounds(3, 112, 222, 29);
		contentPane.add(button);
		
		button_1 = new Button("Topology1 - Randomized");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 lblNewLabel.setText("Running...");
			int timeSlots = Integer.parseInt(textField_3.getText());
			int N = Integer.parseInt(textField_6.getText());
			 double probability[] = new double[N];
			 
			 int iterations= Integer.parseInt(textField_9.getText());
			 double lambda1 = Double.parseDouble(textField.getText());
			 probability[0] = Double.parseDouble(textField_12.getText());
			 probability[1]	= Double.parseDouble(textField_18.getText());
			 probability[2] = Double.parseDouble(textField_21.getText());
			 probability[3]	= Double.parseDouble(textField_24.getText());
			 probability[4]	= Double.parseDouble(textField_27.getText());	
			 lambds = new double[10];
			 if(checkbox.getState())
			 {
				 for (int i = 1; i <= 10; i++) {
					 //probability[i] = Double.parseDouble(textField_11.getText());
					 lambds[i-1] = lambda1*i;
				 }
			 }
			 if(checkbox.getState())
			 {
				 /*
				 double lambdas[] = new double[10]; 
				 for (int i = 0; i < N; i++) {
						//probability[i] = Double.parseDouble(textField_11.getText());
						lambdas[i] = lambda*(i+1);
					}
				 Topology1 top = new Topology1(timeSlots, probability, lambdas, 3, iterations,frameThis);
				top.runAndPrintToFile("Topology3-LCQ.txt");
				*/
				 for(int j=0;j<10;j++)
				 {

					 if(j==9)
					 {
						 lblNewLabel.setText("Done !!");
					 }
					
					 
					 double lambda = lambda1*(j+1);
					 System.out.println("lambda "+j+" "+lambda);
					 double lambdas[] = new double[N];
					 
					 for (int i = 0; i < N; i++) {
						 //probability[i] = Double.parseDouble(textField_11.getText());
						 lambdas[i] = lambda;
					 }
					 progressBar.setValue((j+1)*10);
					 
					 if(checkbox_1.getState())
					 {
						 Topology1 top = new Topology1(true,Double.parseDouble(textField_56.getText()),Double.parseDouble(textField_57.getText()),timeSlots, probability, lambdas, 3, iterations,frameThis);
						 top.runAndPrintToFile("Topology3-LCQ--1.txt");
					 }
					 else
					 {
					 Topology1 top = new Topology1(timeSlots, probability, lambdas, 3, iterations,frameThis);
					 top.runAndPrintToFile("Topology3-LCQ.txt");
					 
					 }
					 
				  }
				
				
			 }
			 else
			 {
				 double lambdas[] = new double[N];
				 for (int i = 0; i < N; i++) {
						//probability[i] = Double.parseDouble(textField_11.getText());
						lambdas[i] = lambda1;
					}
				 lblNewLabel.setText("Done !!");
				 if(checkbox_1.getState())
				 {
					 Topology1 top = new Topology1(true,Double.parseDouble(textField_56.getText()),Double.parseDouble(textField_57.getText()),timeSlots, probability, lambdas, 3, iterations,frameThis);
					 top.runAndPrintToFile("Topology3-LCQ.txt");
					 progressBar.setValue(100);
				 }
				 else
				 {
				 Topology1 top = new Topology1(timeSlots, probability, lambdas, 3, iterations,frameThis);
				 top.runAndPrintToFile("Topology3-LCQ.txt");
				 progressBar.setValue(100);
				 }
//				Topology1 top = new Topology1(timeSlots, probability, lambdas, 3, iterations,frameThis);
//				top.runAndPrintToFile("Topology3-LCQ.txt");
			 }
			}
			
		});
		button_1.setBounds(3, 147, 222, 29);
		contentPane.add(button_1);
		
		button_2 = new Button("Topology1 - LCQ");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progressBar.setValue(0);
				lblNewLabel.setText("Running...");
			int timeSlots = Integer.parseInt(textField_7.getText());
			int N = Integer.parseInt(textField_7.getText());
			 double probability[] = new double[N];

			 
			 int iterations= Integer.parseInt(textField_10.getText());
			 double lambda1 = Double.parseDouble(textField_1.getText());
			 probability[0] = Double.parseDouble(textField_11.getText());
			 probability[1]	= Double.parseDouble(textField_19.getText());
			 probability[2] = Double.parseDouble(textField_22.getText());
			 probability[3]	= Double.parseDouble(textField_25.getText());
			 probability[4]	= Double.parseDouble(textField_28.getText());	
			 lambds = new double[10];
			 if(checkbox.getState())
			 {
				 for (int i = 1; i <= 10; i++) {
					 //probability[i] = Double.parseDouble(textField_11.getText());
					 lambds[i-1] = lambda1*i;
				 }
			 }
			 
			 if(checkbox.getState())
			 {
				 /*
				 double lambdas[] = new double[10]; 
				 for (int i = 0; i < N; i++) {
						//probability[i] = Double.parseDouble(textField_11.getText());
						lambdas[i] = lambda*(i+1);
					}
				 Topology1 top = new Topology1(timeSlots, probability, lambdas, 2, iterations,frameThis);
				top.runAndPrintToFile("Topology3-LCQ.txt");
				*/
				 
				 for(int j=0;j<10;j++)
				 {
					 
					 double lambda = lambda1*(j+1);
					 double lambdas[] = new double[N];
					 
					 if(j==9)
					 {
						 lblNewLabel.setText("Done !!");
					 }
					 
				
					 for (int i = 0; i < N; i++) {
						 //probability[i] = Double.parseDouble(textField_11.getText());
						 lambdas[i] = lambda;
					 }
					 System.out.println("-------"+j);
					 progressBar.setValue((j+1)*10);
					 if(checkbox_1.getState())
					 {
						 Topology1 top = new Topology1(true,Double.parseDouble(textField_56.getText()),Double.parseDouble(textField_57.getText()),timeSlots, probability, lambdas, 2, iterations,frameThis);
						 top.runAndPrintToFile("Topology3-LCQ.txt");
					 }
					 else
					 {
					 Topology1 top = new Topology1(timeSlots, probability, lambdas, 2, iterations,frameThis);
					 top.runAndPrintToFile("Topology3-LCQ.txt");
					 }

				  }
			 }
			 else
			 {
				 double lambdas[] = new double[N];
				 for (int i = 0; i < N; i++) {
						//probability[i] = Double.parseDouble(textField_11.getText());
						lambdas[i] = lambda1;
					}
				 lblNewLabel.setText("Done !!");
				 if(checkbox_1.getState())
				 {
					 Topology1 top = new Topology1(true,Double.parseDouble(textField_56.getText()),Double.parseDouble(textField_57.getText()),timeSlots, probability, lambdas, 2, iterations,frameThis);
					 top.runAndPrintToFile("Topology3-LCQ.txt");
					 progressBar.setValue(100);
				 }
				 else
				 {
				 Topology1 top = new Topology1(timeSlots, probability, lambdas, 2, iterations,frameThis);
				 top.runAndPrintToFile("Topology3-LCQ.txt");
				 progressBar.setValue(100);
				 }
//				Topology1 top = new Topology1(timeSlots, probability, lambdas, 2, iterations,frameThis);
//				top.runAndPrintToFile("Topology3-LCQ.txt");
				
			 }
			 
			
			
			}
		});
		button_2.setBounds(3, 177, 222, 29);
		contentPane.add(button_2);
		
		lblTopology = new JLabel("Topology");
		lblTopology.setBounds(96, 89, 61, 16);
		contentPane.add(lblTopology);
		
		txtK = new JTextField();
		txtK.setText("0.2");
		txtK.setBounds(238, 117, 44, 28);
		contentPane.add(txtK);
		txtK.setColumns(10);
		
		lblLambda = new JLabel("Lambda");
		lblLambda.setBounds(238, 89, 61, 16);
		contentPane.add(lblLambda);
		
		textField = new JTextField();
		textField.setText("0.2");
		textField.setColumns(10);
		textField.setBounds(238, 148, 44, 28);
		contentPane.add(textField);
		
		textField_1 = new JTextField();
		textField_1.setText("0.2");
		textField_1.setColumns(10);
		textField_1.setBounds(238, 178, 44, 28);
		contentPane.add(textField_1);
		
		textField_2 = new JTextField();
		textField_2.setText("50000");
		textField_2.setColumns(10);
		textField_2.setBounds(311, 117, 61, 28);
		contentPane.add(textField_2);
		
		lblIterations = new JLabel("Time Slots");
		lblIterations.setBounds(311, 89, 73, 16);
		contentPane.add(lblIterations);
		
		textField_3 = new JTextField();
		textField_3.setText("50000");
		textField_3.setColumns(10);
		textField_3.setBounds(311, 148, 61, 28);
		contentPane.add(textField_3);
		
		textField_4 = new JTextField();
		textField_4.setText("50000");
		textField_4.setToolTipText("");
		textField_4.setColumns(10);
		textField_4.setBounds(311, 178, 61, 28);
		contentPane.add(textField_4);
		
		lblQueueLength = new JLabel("Queue Length");
		lblQueueLength.setBounds(396, 89, 87, 16);
		contentPane.add(lblQueueLength);
		
		textField_5 = new JTextField();
		textField_5.setText("5");
		textField_5.setColumns(10);
		textField_5.setBounds(396, 117, 61, 28);
		contentPane.add(textField_5);
		
		textField_6 = new JTextField();
		textField_6.setText("5");
		textField_6.setColumns(10);
		textField_6.setBounds(396, 148, 61, 28);
		contentPane.add(textField_6);
		
		textField_7 = new JTextField();
		textField_7.setToolTipText("");
		textField_7.setText("5");
		textField_7.setColumns(10);
		textField_7.setBounds(396, 178, 61, 28);
		contentPane.add(textField_7);
		
		lblIteration = new JLabel("Iteration");
		lblIteration.setBounds(495, 89, 61, 16);
		contentPane.add(lblIteration);
		
		textField_8 = new JTextField();
		textField_8.setText("20");
		textField_8.setColumns(10);
		textField_8.setBounds(495, 117, 44, 28);
		contentPane.add(textField_8);
		
		textField_9 = new JTextField();
		textField_9.setText("20");
		textField_9.setColumns(10);
		textField_9.setBounds(495, 148, 44, 28);
		contentPane.add(textField_9);
		
		textField_10 = new JTextField();
		textField_10.setText("20");
		textField_10.setColumns(10);
		textField_10.setBounds(495, 178, 44, 28);
		contentPane.add(textField_10);
		
		lblP = new JLabel("p1");
		lblP.setBounds(583, 89, 22, 16);
		contentPane.add(lblP);
		
		textField_11 = new JTextField();
		textField_11.setText("1");
		textField_11.setColumns(10);
		textField_11.setBounds(574, 178, 44, 28);
		contentPane.add(textField_11);
		
		textField_12 = new JTextField();
		textField_12.setText("1");
		textField_12.setColumns(10);
		textField_12.setBounds(574, 148, 44, 28);
		contentPane.add(textField_12);
		
		textField_13 = new JTextField();
		textField_13.setText("1");
		textField_13.setColumns(10);
		textField_13.setBounds(574, 117, 44, 28);
		contentPane.add(textField_13);
		
		separator = new JSeparator();
		separator.setBackground(Color.LIGHT_GRAY);
		separator.setBounds(3, 345, 851, 9);
		contentPane.add(separator);
		
		lblSysc_1 = new JLabel("Simulation Result");
		lblSysc_1.setFont(new Font("Calibri", Font.BOLD, 21));
		lblSysc_1.setBounds(144, 363, 155, 16);
		contentPane.add(lblSysc_1);
		
		lblMeanvalue = new JLabel("MeanValue");
		lblMeanvalue.setBounds(10, 406, 73, 16);
		contentPane.add(lblMeanvalue);
		
		lblUpperci = new JLabel("UpperCI");
		lblUpperci.setBounds(10, 440, 73, 16);
		contentPane.add(lblUpperci);
		
		lblLowerci = new JLabel("LowerCI");
		lblLowerci.setBounds(10, 468, 73, 16);
		contentPane.add(lblLowerci);
		
		textField_14 = new JTextArea();
		textField_14.setBounds(114, 398, 257, 28);
		contentPane.add(textField_14);
		textField_14.setColumns(10);

		textField_15 = new JTextArea();// new JTextField();
		textField_15.setColumns(10);
		textField_15.setBounds(114, 433, 257, 28);
		contentPane.add(textField_15);
		
		textField_16 = new JTextArea();//new JTextField();
		textField_16.setColumns(10);
		textField_16.setBounds(114, 468, 257, 28);
		contentPane.add(textField_16);
		
		lblP_1 = new JLabel("p2");
		lblP_1.setBounds(639, 89, 22, 16);
		contentPane.add(lblP_1);
		
		textField_17 = new JTextField();
		textField_17.setText("1");
		textField_17.setColumns(10);
		textField_17.setBounds(630, 117, 44, 28);
		contentPane.add(textField_17);
		
		textField_18 = new JTextField();
		textField_18.setText("1");
		textField_18.setColumns(10);
		textField_18.setBounds(630, 148, 44, 28);
		contentPane.add(textField_18);
		
		textField_19 = new JTextField();
		textField_19.setText("1");
		textField_19.setColumns(10);
		textField_19.setBounds(630, 178, 44, 28);
		contentPane.add(textField_19);
		
		lblP_2 = new JLabel("p3");
		lblP_2.setBounds(693, 89, 22, 16);
		contentPane.add(lblP_2);
		
		textField_20 = new JTextField();
		textField_20.setText("1");
		textField_20.setColumns(10);
		textField_20.setBounds(684, 117, 44, 28);
		contentPane.add(textField_20);
		
		textField_21 = new JTextField();
		textField_21.setText("1");
		textField_21.setColumns(10);
		textField_21.setBounds(684, 148, 44, 28);
		contentPane.add(textField_21);
		
		textField_22 = new JTextField();
		textField_22.setText("1");
		textField_22.setColumns(10);
		textField_22.setBounds(684, 178, 44, 28);
		contentPane.add(textField_22);
		
		lblP_3 = new JLabel("p4");
		lblP_3.setBounds(749, 89, 22, 16);
		contentPane.add(lblP_3);
		
		textField_23 = new JTextField();
		textField_23.setText("1");
		textField_23.setColumns(10);
		textField_23.setBounds(740, 117, 44, 28);
		contentPane.add(textField_23);
		
		textField_24 = new JTextField();
		textField_24.setText("1");
		textField_24.setColumns(10);
		textField_24.setBounds(740, 148, 44, 28);
		contentPane.add(textField_24);
		
		textField_25 = new JTextField();
		textField_25.setText("1");
		textField_25.setColumns(10);
		textField_25.setBounds(740, 178, 44, 28);
		contentPane.add(textField_25);
		
		lblP_4 = new JLabel("p5");
		lblP_4.setBounds(808, 89, 22, 16);
		contentPane.add(lblP_4);
		
		textField_26 = new JTextField();
		textField_26.setText("1");
		textField_26.setColumns(10);
		textField_26.setBounds(799, 117, 44, 28);
		contentPane.add(textField_26);
		
		textField_27 = new JTextField();
		textField_27.setText("1");
		textField_27.setColumns(10);
		textField_27.setBounds(799, 148, 44, 28);
		contentPane.add(textField_27);
		
		textField_28 = new JTextField();
		textField_28.setText("1");
		textField_28.setColumns(10);
		textField_28.setBounds(799, 178, 44, 28);
		contentPane.add(textField_28);
		
		checkbox = new Checkbox("Enable lambda run from 1 to 10 ");
		checkbox.setBounds(404, 438, 257, 23);
		contentPane.add(checkbox);
		
		separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(388, 364, 12, 142);
		contentPane.add(separator_1);
		
		lblProjectProperties = new JLabel("Simulation Properties");
		lblProjectProperties.setFont(new Font("Calibri", Font.BOLD, 21));
		lblProjectProperties.setBounds(463, 363, 198, 16);
		contentPane.add(lblProjectProperties);
		
		button_3 = new Button("Topology 2 - Randomized");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				
				lblNewLabel.setText("");
				lblNewLabel.setText("");
				int N = Integer.parseInt(textField_31.getText());//5;
				int K = Integer.parseInt(textField_58.getText());//3;
				int timeSlotCount = Integer.parseInt(textField_30.getText()); //50000;
				int iterations = Integer.parseInt(textField_32.getText());//20;
				double p = Double.parseDouble(textField_33.getText());
				double lambda = Double.parseDouble(textField_29.getText());//0.02;
				//double[] lambdas = new double[N];
				double probability[][] = new double[N][K];
				double lambdas[] = new double[N];
				 lambds = new double[10];
				
					 for (int i = 1; i <= 10; i++) {
						 //probability[i] = Double.parseDouble(textField_11.getText());
						 lambds[i-1] = lambda*i;
					 }
				 
			for (int i = 0; i < N; i++) {
				for (int k = 0; k < K; k++) {
					probability[i][k] = p;
				}
			}
			
			
		
			for (int i = 1; i <= 10; i++) {
				
				for (int n = 0; n < N; n++) {
					lambdas[n] = (n + 1) * lambda * i;
				}

				//progressBar.setValue(i*10);
				if(checkbox_1.getState())
				{
					double a = Double.parseDouble(textField_56.getText());
					double b = Double.parseDouble(textField_57.getText());
					Topology2 top = new Topology2(true, a, b, timeSlotCount, probability, lambdas, 3, iterations, K, frameThis);
					top.runAndPrintToFile("1_TOP_RAN",i*10);
				}
				else
				{
					Topology2 top = new Topology2(timeSlotCount, probability, lambdas, 3, iterations, K, frameThis);
					top.runAndPrintToFile("1_TOP_RAN",i*10);
				}
				
			}

			
			

			}
		});
		button_3.setActionCommand("");
		button_3.setBounds(3, 241, 222, 29);
		contentPane.add(button_3);
		
		textField_29 = new JTextField();
		textField_29.setText("0.2");
		textField_29.setColumns(10);
		textField_29.setBounds(238, 241, 44, 28);
		contentPane.add(textField_29);
		
		textField_30 = new JTextField();
		textField_30.setToolTipText("");
		textField_30.setText("50000");
		textField_30.setColumns(10);
		textField_30.setBounds(311, 241, 61, 28);
		contentPane.add(textField_30);
		
		textField_31 = new JTextField();
		textField_31.setToolTipText("");
		textField_31.setText("5");
		textField_31.setColumns(10);
		textField_31.setBounds(396, 241, 61, 28);
		contentPane.add(textField_31);
		
		textField_32 = new JTextField();
		textField_32.setText("20");
		textField_32.setColumns(10);
		textField_32.setBounds(495, 241, 44, 28);
		contentPane.add(textField_32);
		
		textField_33 = new JTextField();
		textField_33.setText("1");
		textField_33.setColumns(10);
		textField_33.setBounds(574, 241, 44, 28);
		contentPane.add(textField_33);
		
		textField_34 = new JTextField();
		textField_34.setText("-");
		textField_34.setColumns(10);
		textField_34.setBounds(630, 241, 44, 28);
		contentPane.add(textField_34);
		
		textField_35 = new JTextField();
		textField_35.setText("-");
		textField_35.setColumns(10);
		textField_35.setBounds(684, 241, 44, 28);
		contentPane.add(textField_35);
		
		textField_36 = new JTextField();
		textField_36.setText("-");
		textField_36.setColumns(10);
		textField_36.setBounds(740, 241, 44, 28);
		contentPane.add(textField_36);
		
		textField_37 = new JTextField();
		textField_37.setText("-");
		textField_37.setColumns(10);
		textField_37.setBounds(799, 241, 44, 28);
		contentPane.add(textField_37);
		
		button_4 = new Button("Topology 2 - AS/LCQ");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				lblNewLabel.setText("");
				lblNewLabel.setText("");
				int N = Integer.parseInt(textField_40.getText());//5;
				int K = Integer.parseInt(textField_58.getText());//3;
				int timeSlotCount = Integer.parseInt(textField_39.getText()); //50000;
				int iterations = Integer.parseInt(textField_41.getText());//20;
				double p = Double.parseDouble(textField_42.getText());
				double lambda = Double.parseDouble(textField_38.getText());//0.02;
				//double[] lambdas = new double[N];
				double probability[][] = new double[N][K];
				double lambdas[] = new double[N];
				 lambds = new double[10];
					
				 for (int i = 1; i <= 10; i++) {
					 //probability[i] = Double.parseDouble(textField_11.getText());
					 lambds[i-1] = lambda*i;
				 }
				 
			for (int i = 0; i < N; i++) {
				for (int k = 0; k < K; k++) {
					probability[i][k] = p;
				}
			}
			
			
		
			for (int i = 1; i <= 10; i++) {
				
				for (int n = 0; n < N; n++) {
					lambdas[n] = (n + 1) * lambda * i;
				}

				//progressBar.setValue(i*10);
				if(checkbox_1.getState())
				{
					double a = Double.parseDouble(textField_56.getText());
					double b = Double.parseDouble(textField_57.getText());
					Topology2 top = new Topology2(true, a, b, timeSlotCount, probability, lambdas, 1, iterations, K, frameThis);
					top.runAndPrintToFile("1_TOP_RAN",i*10);
				}
				else
				{
					Topology2 top = new Topology2(timeSlotCount, probability, lambdas, 1, iterations, K, frameThis);
					top.runAndPrintToFile("1_TOP_RAN",i*10);
				}
				
			}
				
			}
		});
		button_4.setActionCommand("");
		button_4.setBounds(3, 275, 222, 29);
		contentPane.add(button_4);
		
		textField_38 = new JTextField();
		textField_38.setText("0.2");
		textField_38.setColumns(10);
		textField_38.setBounds(238, 276, 44, 28);
		contentPane.add(textField_38);
		
		textField_39 = new JTextField();
		textField_39.setToolTipText("");
		textField_39.setText("50000");
		textField_39.setColumns(10);
		textField_39.setBounds(311, 276, 61, 28);
		contentPane.add(textField_39);
		
		textField_40 = new JTextField();
		textField_40.setToolTipText("");
		textField_40.setText("5");
		textField_40.setColumns(10);
		textField_40.setBounds(396, 276, 61, 28);
		contentPane.add(textField_40);
		
		textField_41 = new JTextField();
		textField_41.setText("20");
		textField_41.setColumns(10);
		textField_41.setBounds(495, 276, 44, 28);
		contentPane.add(textField_41);
		
		textField_42 = new JTextField();
		textField_42.setText("1");
		textField_42.setColumns(10);
		textField_42.setBounds(574, 276, 44, 28);
		contentPane.add(textField_42);
		
		textField_43 = new JTextField();
		textField_43.setText("-");
		textField_43.setColumns(10);
		textField_43.setBounds(630, 276, 44, 28);
		contentPane.add(textField_43);
		
		textField_44 = new JTextField();
		textField_44.setText("-");
		textField_44.setColumns(10);
		textField_44.setBounds(684, 276, 44, 28);
		contentPane.add(textField_44);
		
		textField_45 = new JTextField();
		textField_45.setText("-");
		textField_45.setColumns(10);
		textField_45.setBounds(740, 276, 44, 28);
		contentPane.add(textField_45);
		
		textField_46 = new JTextField();
		textField_46.setText("-");
		textField_46.setColumns(10);
		textField_46.setBounds(799, 276, 44, 28);
		contentPane.add(textField_46);
		
		button_5 = new Button("Topology 2 - LCSF/LCQ");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				lblNewLabel.setText("");
				lblNewLabel.setText("");
				int N = Integer.parseInt(textField_49.getText());//5;
				int K = Integer.parseInt(textField_58.getText());//3;
				int timeSlotCount = Integer.parseInt(textField_48.getText()); //50000;
				int iterations = Integer.parseInt(textField_50.getText());//20;
				double p = Double.parseDouble(textField_51.getText());
				double lambda = Double.parseDouble(textField_47.getText());//0.02;
				//double[] lambdas = new double[N];
				double probability[][] = new double[N][K];
				double lambdas[] = new double[N];
				
				 lambds = new double[10];
					
				 for (int i = 1; i <= 10; i++) {
					 //probability[i] = Double.parseDouble(textField_11.getText());
					 lambds[i-1] = lambda*i;
				 }
				
			for (int i = 0; i < N; i++) {
				for (int k = 0; k < K; k++) {
					probability[i][k] = p;
				}
			}
			
			
		
			for (int i = 1; i <= 10; i++) {
				
				for (int n = 0; n < N; n++) {
					lambdas[n] = (n + 1) * lambda * i;
				}

				//progressBar.setValue(i*10);
				if(checkbox_1.getState())
				{
					double a = Double.parseDouble(textField_56.getText());
					double b = Double.parseDouble(textField_57.getText());
					Topology2 top = new Topology2(true, a, b, timeSlotCount, probability, lambdas, 2, iterations, K, frameThis);
					top.runAndPrintToFile("1_TOP_RAN",i*10);
				}
				else
				{
					Topology2 top = new Topology2(timeSlotCount, probability, lambdas, 2, iterations, K, frameThis);
					top.runAndPrintToFile("1_TOP_RAN",i*10);
				}
				
			}
			}
		});
		button_5.setActionCommand("");
		button_5.setBounds(3, 307, 222, 29);
		contentPane.add(button_5);
		
		textField_47 = new JTextField();
		textField_47.setText("0.2");
		textField_47.setColumns(10);
		textField_47.setBounds(238, 311, 44, 28);
		contentPane.add(textField_47);
		
		textField_48 = new JTextField();
		textField_48.setToolTipText("");
		textField_48.setText("50000");
		textField_48.setColumns(10);
		textField_48.setBounds(311, 311, 61, 28);
		contentPane.add(textField_48);
		
		textField_49 = new JTextField();
		textField_49.setToolTipText("");
		textField_49.setText("5");
		textField_49.setColumns(10);
		textField_49.setBounds(396, 311, 61, 28);
		contentPane.add(textField_49);
		
		textField_50 = new JTextField();
		textField_50.setText("20");
		textField_50.setColumns(10);
		textField_50.setBounds(495, 311, 44, 28);
		contentPane.add(textField_50);
		
		textField_51 = new JTextField();
		textField_51.setText("1");
		textField_51.setColumns(10);
		textField_51.setBounds(574, 311, 44, 28);
		contentPane.add(textField_51);
		
		textField_52 = new JTextField();
		textField_52.setText("-");
		textField_52.setColumns(10);
		textField_52.setBounds(630, 311, 44, 28);
		contentPane.add(textField_52);
		
		textField_53 = new JTextField();
		textField_53.setText("-");
		textField_53.setColumns(10);
		textField_53.setBounds(684, 311, 44, 28);
		contentPane.add(textField_53);
		
		textField_54 = new JTextField();
		textField_54.setText("-");
		textField_54.setColumns(10);
		textField_54.setBounds(740, 311, 44, 28);
		contentPane.add(textField_54);
		
		textField_55 = new JTextField();
		textField_55.setText("-");
		textField_55.setColumns(10);
		textField_55.setBounds(799, 311, 44, 28);
		contentPane.add(textField_55);
		
		separator_2 = new JSeparator();
		separator_2.setBackground(Color.GRAY);
		separator_2.setBounds(15, 212, 846, 9);
		contentPane.add(separator_2);
		
		separator_3 = new JSeparator();
		separator_3.setOrientation(SwingConstants.VERTICAL);
		separator_3.setBounds(227, 89, 12, 122);
		contentPane.add(separator_3);
		
		separator_4 = new JSeparator();
		separator_4.setOrientation(SwingConstants.VERTICAL);
		separator_4.setBounds(227, 225, 12, 118);
		contentPane.add(separator_4);
		
		JButton btnClearall = new JButton("Clear Result");
		btnClearall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textField_14.setText("");
				textField_15.setText("");
				textField_16.setText("");
				lblNewLabel.setText("--");
				progressBar.setValue(0);
			}
		});
		btnClearall.setBounds(3, 360, 117, 29);
		contentPane.add(btnClearall);
		
		lblNewLabel = new JLabel("---");
		lblNewLabel.setBounds(589, 511, 65, 16);
		contentPane.add(lblNewLabel);
		
		JLabel lblA = new JLabel("a");
		lblA.setBounds(717, 370, 22, 16);
		contentPane.add(lblA);
		
		textField_56 = new JTextField();
		textField_56.setText("0.4");
		textField_56.setColumns(10);
		textField_56.setBounds(708, 398, 44, 28);
		contentPane.add(textField_56);
		
		JLabel lblB = new JLabel("b");
		lblB.setBounds(768, 370, 22, 16);
		contentPane.add(lblB);
		
		textField_57 = new JTextField();
		textField_57.setText("0.4");
		textField_57.setColumns(10);
		textField_57.setBounds(759, 398, 44, 28);
		contentPane.add(textField_57);
		
		JLabel lblStatus = new JLabel("Status :");
		lblStatus.setBounds(424, 510, 108, 16);
		contentPane.add(lblStatus);
		
		checkbox_1 = new Checkbox("Enable a-b | Tes");
		checkbox_1.setBounds(404, 403, 257, 23);
		contentPane.add(checkbox_1);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(708, 506, 146, 20);
		contentPane.add(progressBar);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
		
		JLabel lblServers = new JLabel("Servers");
		lblServers.setBounds(799, 370, 62, 16);
		contentPane.add(lblServers);
		
		textField_58 = new JTextField();
		textField_58.setText("3");
		textField_58.setColumns(10);
		textField_58.setBounds(808, 398, 44, 28);
		contentPane.add(textField_58);
		
		btnPlotData = new JButton("Plot Data");
		btnPlotData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//DrawSemiLogChart();
				 //final PlotGraph demo = new PlotGraph("Semi Log Chart", frameThis);
			    DrawSemiLogChart();
			}
		});
		btnPlotData.setBounds(726, 436, 117, 29);
		contentPane.add(btnPlotData);
		
		btnInfo = new JButton("info ?");
		btnInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frameThis,"" +"How to ?\n"+
						"Enter the data and run the simulation one by one.\n" +
						"It might take about 15-20 sec for a simulation to complete so please be patient.\n"+
						"Before clicking on plot please make sure there is some data in MeanValue Box\n" +
						"Always click on clear before starting new simulation.\n"+
						"Repo - https://bitbucket.org/nbhasin/sysc-4005-project-simulator"+
						"\n\nby Nishant Bhasin"+"\nVer. 3.0");
				//ShowDialogBox();
			}
		});
		btnInfo.setBounds(800, 19, 61, 29);
		contentPane.add(btnInfo);
		
		checkbox_2 = new Checkbox("Show / Hide Lines for Confidence Interval");
		checkbox_2.setBounds(404, 473, 311, 23);
		contentPane.add(checkbox_2);
		message();
	}
	
	
	public void message()
	{
//        PrintStream ps = new PrintStream(n );
//        System.setOut( ps );
//        System.setErr( ps );


	}
	
	public boolean checkboxTicked()
	{
		return checkbox.getState();
	}
	
	//mean
	public JTextArea getTF14()
	{
		return textField_14;
	}
	//Upper CI
	public JTextArea getTF15()
	{
		return textField_15;
	}
	//Lower CI
	public JTextArea getTF16()
	{
		return textField_16;
	}
	
	public void DrawChart()
	{
		// create a dataset...
		DefaultPieDataset data = new DefaultPieDataset();
		data.setValue("Category 1", 43.2);
		data.setValue("Category 2", 27.9);
		data.setValue("Category 3", 79.5);
		// create a chart...
		JFreeChart chart = ChartFactory.createPieChart(
		"Sample Pie Chart",
		data,
		true, // legend?
		true, // tooltips?
		false // URLs?
		);
		
		//JFreeChart chart2 = ChartFactory.create
		// create and display a frame...
		ChartFrame frame = new ChartFrame("First", chart);
		frame.pack();
		frame.setVisible(true);

	}
	
	public void DrawSemiLogChart()
	{
		  final XYSeries s1 = new XYSeries("Series 1");
		  final XYSeries s2 = new XYSeries("Upper");
		  final XYSeries s3 = new XYSeries("Lower");
		  
		  XYIntervalSeriesCollection errorsDataSet = new XYIntervalSeriesCollection();
		    XYIntervalSeries errors = new XYIntervalSeries("errors");
		    
		    Stroke stroke = new BasicStroke();
            Paint paint = Color.black;

            //XYLineAnnotation vertical;//= new XYLineAnnotation(x, y-dy, x, y+dy, stroke, paint);
		    
			String temp = getTF14().getText();
			String top = getTF15().getText();
			String bottom = getTF16().getText();
			
			String a[] = temp.split(",");
			System.out.println(Arrays.toString(a));

			String b[] = top.split(",");
			String c[] = bottom.split(",");
			
			for(int j=1;j<=a.length;j++)
			{
				//System.out.println(lambds[j-1]);
				s1.add(lambds[j-1],Double.parseDouble(a[j-1]));
				s2.add(lambds[j-1],Double.parseDouble(b[j-1]));//Up
				s3.add(lambds[j-1],Double.parseDouble(c[j-1]));//Low
				
				//s1.add(Math.exp(j / 5.0),Double.parseDouble(a[j-1]));
				//s2.add(Math.exp(j / 5.0),Double.parseDouble(b[j-1]));//Up
				//s3.add(Math.exp(j / 5.0),Double.parseDouble(c[j-1]));//Low
				
			}
			


	        final XYSeriesCollection dataset = new XYSeriesCollection();
	        dataset.addSeries(s1);
	        dataset.addSeries(s2);
	        dataset.addSeries(s3);

	        

	         JFreeChart chart = ChartFactory.createXYLineChart(
	            "Generated Graph",          // chart title
	            "Category",               // domain axis label
	            "Value",                  // range axis label
	            dataset,                  // data
	            PlotOrientation.VERTICAL,
	            true,                     // include legend
	            true,
	            false
	        );
	        


	        final XYPlot plot = chart.getXYPlot();
	        final NumberAxis domainAxis = new NumberAxis("x");
	        final NumberAxis rangeAxis = new LogarithmicAxis("Log(y)");
	        plot.setDomainAxis(domainAxis);
	        plot.setRangeAxis(rangeAxis);
	        chart.setBackgroundPaint(Color.white);
	        
	        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        if(checkbox_2.getState())
	        {
	        	renderer.setSeriesLinesVisible(1, true);
		        renderer.setSeriesLinesVisible(2, true);
	        }
	        else
	        {
	        	renderer.setSeriesLinesVisible(1, false);
		        renderer.setSeriesLinesVisible(2, false);
	        }
	        renderer.setSeriesShapesVisible(1, true);
	        plot.setOutlinePaint(Color.black);
	        plot.setRenderer(renderer);
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	        
	        

			ChartFrame frame = new ChartFrame("Graph Plot", chart);
			frame.pack();
			frame.setVisible(true);
	}
	
	public JProgressBar prog()
	{
		return progressBar;
	}
	
	  public void ShowDialogBox(){
		  final JFrame frame = new JFrame("Show Message Dialog");
		  JButton button = new JButton("Click Me");
		  button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(frame,"Roseindia.net");
				}
		  }
						 
				  );
		  frame.getContentPane().add(button);
		  frame.setSize(400, 400);
		  frame.setVisible(true);
		  frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		  }
}

