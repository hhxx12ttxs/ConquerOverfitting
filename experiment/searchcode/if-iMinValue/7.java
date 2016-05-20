package edu.hm.dako.EchoApplication.TestAndBenchmarking;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.log4j.PropertyConfigurator;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.hm.dako.EchoApplication.TestAndBenchmarking.UserInterfaceInputParameters.ImplementationType;
import edu.hm.dako.EchoApplication.TestAndBenchmarking.UserInterfaceInputParameters.MeasurementType;

/**
 * Client GUI Programm mit dem die möglichen Parameter leicher eingegeben werden können.
 * Diese Klasse muss nicht angepasst werden.
 * 
 * @author Rottmüller
 *
 */
public class AutomaticClientGUI extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2103090247097271062L;
	
	
	private static JFrame jFrame; // Frame fuer Echo-Anwendugs-GUI
	private static JPanel jPanel;
	
	
	// GUI Komponenten
	private JFormattedTextField serverAddress;
	private JFormattedTextField serverPort;
	
	private JFormattedTextField durchlaeufe;
	private JFormattedTextField minValue;
	private JFormattedTextField maxValue;
	private JFormattedTextField difValue;
	
	private JFormattedTextField clientThinkTime;
	private JFormattedTextField maxNoOfMessages;
	private JFormattedTextField messageLenth;
	
	private JCheckBox tcpMultiThreadedServer;
	private JCheckBox udpMultiThreadedServer;
	private JCheckBox rmiMultiThreadedServer;
	private JCheckBox reliableMultiThreadedServer;
	
	private Button startButton;
	private Button newButton;
	private Button beendenButton;
	
	public AutomaticClientGUI() {
		super (new BorderLayout());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configureAndWatch("log4j.client.properties", 60 * 1000);
		
		try{
            //UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");
        } catch (Exception e) {
            // Likely PlasticXP is not in the class path; ignore.
		}
	
		jFrame = new JFrame("Automatic Client GUI");
		jFrame.setTitle("Automatic Client GUI");
		jFrame.add(new AutomaticClientGUI());
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JComponent panel = new AutomaticClientGUI().buildPanel();
        jFrame.getContentPane().add(panel);
        jFrame.pack();
        jFrame.setVisible(true);
	}

	private JComponent buildPanel() {
		initComponents();
		
		// Layout definieren
	    FormLayout layout = new FormLayout(
	                "left:pref, right:max(40dlu;pref), 3dlu, 70dlu, 7dlu, left:pref, right:max(40dlu;pref), 3dlu, 70dlu", // columns
	                "p, 9dlu, p, 3dlu, p, 6dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 6dlu, " +
	                "p, 3dlu, p, 3dlu, p, 9dlu, p, 3dlu, p");
	                
	    jPanel = new JPanel(layout);
	    jPanel.setBorder(Borders.DIALOG_BORDER);
	    
	    CellConstraints cc = new CellConstraints();
	    
	    jPanel.add(createSeparator("Eingabeparameter"),  cc.xyw(1, 1, 9));
	    
	    
	    
	    jPanel.add(createSeparator("Serverdaten"),  cc.xyw(2, 3, 8));
	    
	    jPanel.add(new JLabel("Serveradresse:"), cc.xy(2, 5));
	    jPanel.add(serverAddress, cc.xy(4, 5));
	    serverAddress.setText("localhost");
	    
	    jPanel.add(new JLabel("Serverport:"), cc.xy(7, 5));
	    jPanel.add(serverPort, cc.xy(9, 5));
	    serverPort.setText("50000");
	    
	    
	    
	    jPanel.add(createSeparator("Parameter"),  cc.xyw(2, 7, 8));
	    
	    jPanel.add(new JLabel("Min Value:"), cc.xy(2, 9));
	    jPanel.add(minValue, cc.xy(4, 9));
	    minValue.setText("100");
	    
	    jPanel.add(new JLabel("Durchläufe:"), cc.xy(7, 9));
	    jPanel.add(durchlaeufe, cc.xy(9, 9));
	    durchlaeufe.setText("5");
	    
	    
	    jPanel.add(new JLabel("Max Value:"), cc.xy(2, 11));
	    jPanel.add(maxValue, cc.xy(4, 11));
	    maxValue.setText("500");
	    
	    jPanel.add(new JLabel("Denkzeit [ms]:"), cc.xy(7, 11));
	    jPanel.add(clientThinkTime, cc.xy(9, 11));
	    clientThinkTime.setText("100");
	    
	    
	    jPanel.add(new JLabel("Schrittweite:"), cc.xy(2, 13));
	    jPanel.add(difValue, cc.xy(4, 13));
	    difValue.setText("100");
	    
	    
	    
	    jPanel.add(new JLabel("Nachrichtenlänge:"), cc.xy(2, 15));
	    jPanel.add(messageLenth, cc.xy(4, 15));
	    messageLenth.setText("50");
	    
	    jPanel.add(new JLabel("Nachrichtenanzahl:"), cc.xy(7, 15));
	    jPanel.add(maxNoOfMessages, cc.xy(9, 15));
	    maxNoOfMessages.setText("100");
	    
	    
	    
	    jPanel.add (createSeparator("zu startende Server"),  cc.xyw(2, 17, 8));
	    
	    jPanel.add(tcpMultiThreadedServer, cc.xyw(1, 19, 4));
	    tcpMultiThreadedServer.setSelected(true);
	    
	    jPanel.add(udpMultiThreadedServer, cc.xyw(6, 19, 4));
	    udpMultiThreadedServer.setSelected(true);
	    
	    jPanel.add(rmiMultiThreadedServer, cc.xyw(1, 21, 4));
	    rmiMultiThreadedServer.setSelected(true);
	    
	    jPanel.add(reliableMultiThreadedServer, cc.xyw(6, 21, 4));
	    reliableMultiThreadedServer.setSelected(true);
	    
	    
	    
	    jPanel.add(createSeparator("Aktionen"),cc.xyw(1, 23, 9));
		
	    jPanel.add (startButton, cc.xyw(3, 25, 2));   //Starten
	    jPanel.add (newButton, cc.xyw(5, 25, 3)); 	  //Loeschen
	    jPanel.add (beendenButton, cc.xyw(8, 25, 2)); //Abbrechen
		
		startButton.addActionListener(this);
		newButton.addActionListener(this);
		beendenButton.addActionListener(this);
		
	    return jPanel; 
	}

	private JComponent createSeparator(String text) {
		return DefaultComponentFactory.getInstance().createSeparator(text);
	}

	private void initComponents() {
		// Felder
		serverAddress = new JFormattedTextField();
		serverPort = new JFormattedTextField();
		
		durchlaeufe = new JFormattedTextField();
		minValue = new JFormattedTextField();
		maxValue = new JFormattedTextField();
		difValue = new JFormattedTextField();
		
		clientThinkTime = new JFormattedTextField();
		maxNoOfMessages = new JFormattedTextField();
		messageLenth = new JFormattedTextField();
		
		tcpMultiThreadedServer = new JCheckBox("TCP Multi Threaded Server", true);
		udpMultiThreadedServer = new JCheckBox("UDP Multi Threaded Server", true);
		rmiMultiThreadedServer = new JCheckBox("RMI Multi Threaded Server", true);
		reliableMultiThreadedServer = new JCheckBox("Reliable Multi Threaded Server", true);
		
		// Buttons
		startButton = new Button("Starten");
		newButton = new Button("Neu");
		newButton.setEnabled(false);
		beendenButton = new Button("Beenden"); 
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Starten")) {
			startButton.setEnabled(false);
			beendenButton.setEnabled(false);
			startAction(e);
			newButton.setEnabled(true);
			beendenButton.setEnabled(true);
		}
		
		if (e.getActionCommand().equals("Neu")) {
			newAction(e);
			startButton.setEnabled(true);
		}
		
		if (e.getActionCommand().equals("Beenden")) {
			finishAction(e);
		}
	}

	private void startAction(ActionEvent e) {
		// Input-Parameter aus GUI lesen
		AutomaticUserInterfaceInputParameters iParm = new AutomaticUserInterfaceInputParameters();
		
		// GUI sammmelt Eingabedaten 
			
		System.out.println("RemoteServerAdress: " + serverAddress.getText());
		iParm.setRemoteServerAddress(serverAddress.getText());
		
		Integer iServerPort = Integer.parseInt(serverPort.getText());
		System.out.println("Serverport: " + iServerPort);
		iParm.setRemoteServerPort(iServerPort);
			
		Integer iDurchlaeufe = Integer.parseInt(durchlaeufe.getText());
		System.out.println("Durchläufe: " + iDurchlaeufe);
		iParm.setDurchlaeufe(iDurchlaeufe); 
			
		Integer iThinkTime = Integer.parseInt(clientThinkTime.getText());
		System.out.println("Denkzeit: " + iThinkTime + " ms");
		iParm.setClientThinkTime(iThinkTime);
			
		Integer iMessageLength = Integer.parseInt(messageLenth.getText());
		System.out.println("Nachrichtenlaenge: " + iMessageLength + " Byte");
		iParm.setMessageLength(iMessageLength);
				
		Integer iMinValue = Integer.parseInt(minValue.getText());
		System.out.println("Min. Value: " + iMinValue);
		iParm.setMinValue(iMinValue);
		
		Integer iMaxValue = Integer.parseInt(maxValue.getText());
		System.out.println("Max. Value: " + iMaxValue);
		iParm.setMaxValue(iMaxValue);
		
		Integer iDiffValue = Integer.parseInt(difValue.getText());
		System.out.println("Diff. Value: " + iDiffValue);
		iParm.setDifValue(iDiffValue);
		
		Integer iMaxNoOfMessages = Integer.parseInt(maxNoOfMessages.getText());
		System.out.println("max. Nachrichten: " + iMaxNoOfMessages);
		iParm.setMaxNumberOfMessages(iMaxNoOfMessages);
		
		iParm.setMeasurementTypeList(getMeasurementTypeList());
		
		iParm.setImplementationTypeList(getImplementationTypeList());
		
		// Benchmarking-Client instanziieren und Benchmark starten
		AutomaticBenchmarkingGuiSimulation automaticClient = new AutomaticBenchmarkingGuiSimulation();
		automaticClient.start(iParm);
		
//		BenchmarkingClient benchClient = new BenchmarkingClient();
//		benchClient.executeTest(iParm, this);
	}

	private List<ImplementationType> getImplementationTypeList() {
		List<ImplementationType> resultList = new ArrayList<ImplementationType>();
		
		if (tcpMultiThreadedServer.isSelected()) {
			resultList.add(ImplementationType.TCPMultiThreaded);
		}
		
		if (udpMultiThreadedServer.isSelected()) {
			resultList.add(ImplementationType.UDPMultiThreaded);
		}
		
		if (rmiMultiThreadedServer.isSelected()) {
			resultList.add(ImplementationType.RmiMultiThreaded);
		}
		
		if (reliableMultiThreadedServer.isSelected()) {
			resultList.add(ImplementationType.ReliableUdpMultiThreaded);
		}
		
		return resultList;
	}

	private List<MeasurementType> getMeasurementTypeList() {
		List<MeasurementType> resultList = new ArrayList<MeasurementType>();
		
		resultList.add(MeasurementType.VarThreads);
		
		return resultList;
	}

	private void newAction(ActionEvent e) {
		serverAddress.setText("localhost");
		serverPort.setText("50000");
		durchlaeufe.setText("5");
		difValue.setText("100");
		minValue.setText("100");
		maxValue.setText("500");
		clientThinkTime.setText("100");
		maxNoOfMessages.setText("100");
		messageLenth.setText("50");
		
		tcpMultiThreadedServer.setSelected(true);
		udpMultiThreadedServer.setSelected(true);
		rmiMultiThreadedServer.setSelected(true);
		reliableMultiThreadedServer.setSelected(true);
	}

	private void finishAction(ActionEvent e) {
		// Programm beenden
		System.exit(0);
	}
	
	public void windowClosing(WindowEvent e) {
		System.exit(0);
	}
	
}

