package VriNettbasert;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import java.util.Scanner;






import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;



public class VriKlient extends JFrame implements Runnable{
	private static Kort bordet;
	public static Spiller hand;
	
	private static ObjectOutputStream output;
	private static ObjectInputStream input;
	private Socket client;
	private Scanner inputScanner;
	private SpillerPanel spillerPanel;
	private bordPanel bordPanel;
	private JTextArea displayArea;
	private String server;
	private static String navn;
	private static boolean vinn = false;
	private static boolean dintur = false;
	private static Color dinturColor;
	private static Color ikkedinturColor;
	private static Color bordColor;
	
	public VriKlient(String host){
		super(JOptionPane.showInputDialog("Hva heter du?"));
		navn = super.getTitle();
		System.out.println(navn);
		server = host;
		hand = new Spiller(navn);
		
		dinturColor 	= new Color(36, 200, 25);
		ikkedinturColor = new Color(26, 106, 15);
		bordColor		= new Color(36, 170, 25);
		
		spillerPanel = new SpillerPanel(hand);
		spillerPanel.setBackground(dinturColor);
		
		bordPanel = new bordPanel();
		bordPanel.setBackground(bordColor);
		
		
		displayArea = new JTextArea();
//		displayArea.setMaximumSize(new Dimension(600,1));
	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension d = new Dimension(600, 50);
		Dimension p = new Dimension(600,40);
		
		JScrollPane sc = new JScrollPane(displayArea);
		sc.setMaximumSize(p);
		sc.setPreferredSize(p);
		sc.setSize(p);
		
		sc.revalidate();
		/*
		JPanel j = new JPanel();
		j.add(sc, BorderLayout.WEST);
		j.setSize(d);
		j.setPreferredSize(d);
		j.setMaximumSize(d);
		j.revalidate();
		*/
		add(sc, BorderLayout.SOUTH);
		validate();
				
		
		add(bordPanel, BorderLayout.NORTH);
		
		add(spillerPanel, BorderLayout.CENTER);
		pack();
		setVisible(true);
		//spillerPanel.updateUI();
	} //end VriKlient constructor
	/**
	 * Kjżrer metodene som starter serveren, og er klar til  spille
	 * 
	 */
	public void run(){
		try{
			connectToServer();
			getStreams();
			processConnection();
		}//end try
		catch(EOFException eof){
			displayMessage("\nClient terminated connection");
		}
		catch (IOException ioe){
			displayMessage("Cannot connect to server.");
	//		ioe.printStackTrace();
		}
		
	} //end method runClient
	/**
	 * Kobler til serveren. 
	 * Viser status i displayArea
	 * @throws IOException Hvis den ikke fr kontakt med serveren som er angitt
	 */
	private void connectToServer() throws IOException {
		displayMessage("Attempting to connect... \n");
		client = new Socket(InetAddress.getByName(server), 12345);
		
		displayMessage("Connected to: " + client.getInetAddress().getHostName());	
	} //end method connectToServer
	/**
	 * Setter opp strżmmer inn og ut.
	 * @throws IOException Dersomd en ikke fr satt opp strżmmene.
	 */
	private void getStreams() throws IOException {
		output = new ObjectOutputStream(client.getOutputStream());
		output.flush();
		
		input = new ObjectInputStream(client.getInputStream());
		inputScanner = new Scanner(input);
		
		displayMessage("\nGot I/O streams\n");
	} //end method getStreams
	
	/**
	 * Kjżrer spillet. Venter p tilbakemelding fra serveren helt til noen vinner.
	 * Tolker inkommende meldinger fra serveren og kjżrer de nżdvendige metoder.
	 * @throws IOException Dersom forbindelsen blir brutt.
	 */
	private void processConnection() throws IOException {
		
		do{
			oppdaterVindu();
			hand.kortene();
		try {
			Object tmp = input.readObject();
//			displayMessage("Received object \n");
			
			pakke m = (pakke)tmp;
			if(m.meldingLik("Lovlig")){
//				displayMessage("trekket er lovlig\n");
				dintur = false;
			}
			if(m.meldingLik("ikkeLovlig")){
				displayMessage("Ulovlig trekk\n");
				hand.leggTilKort(m.kort);
			}
			if(m.meldingLik("trukketForMange")){
				displayMessage("Du har trukket mer enn tre kort.\n");
				dintur = false;
			}
			
			if(m.meldingLik("DuErMed")){
				displayMessage("Du er med\n"); //bekreftelse fra serveren p at spilleren er med
				pakke b = new pakke("bekreft", navn);
				try{
					output.writeObject(b);
					output.flush();
				}
				catch(IOException ioe){
					
				}
			}
			else if(m.meldingLik("dintur")){
//				displayMessage("Det er din tur.\n");
				dintur=true;
				oppdaterVindu();
			}
			else if(m.meldingLik("noenVant")){
				noenVant(m.navn);
			}
			else if(m.meldingLik("ettKort")){
				displayMessage("\n" + m.navn + "har kun ett kort paa handen.\n");
			}
			else if(m.meldingLik("nyttBord")){
				bordet = m.kort;
			}
			else if(m.meldingLik("nyttHand")){
							hand.leggTilKort(m.getKort());
//							System.out.println("Kort gjenkjent\n");
//							System.out.println(hand.antKort());
						
			} //end else if
		}
		catch(ClassNotFoundException cnf){
			displayMessage("Objekt av ukjent klasse motatt");
		}
		oppdaterVindu();
		}while (!vinn);
		
		
	} //end method processConnection
	
	public void closeConnection(){
		displayMessage("\n Closing connection");
		try{
			output.close();
			input.close();
			client.close();
		} 
		catch(IOException ioe){
			
		} //end catch
	} //end closeConnection
	
	public static void lesValg(int k) {
		if(dintur){
		leggKort(hand.trekkKort(k));
		}
		else{
			System.out.println("Det er ikke din tur");
			
		}
		
	}
	public static void trekkEttKort(){
		if(dintur){
			pakke s = new pakke("trekkEtt", navn);
		try{
			output.writeObject(s);
			output.flush();
		} //end try
		catch(IOException ioe){
			
		}//end catch
		} //end if
		else {
			System.out.println("Det er ikke din tur");
				} //end else
	} //End method leggKort
	private static Kort vri(Kort kort){
		boolean goodInp = false;
		do{
			try{
				Object[] possibleValues = {"Kloever", "Hjerter", "Ruter", "Spar"};
				Object selectedValue = JOptionPane.showInputDialog(null,
						"Hva vil du vri til?", "Vri", 
						JOptionPane.INFORMATION_MESSAGE, null, possibleValues,
						possibleValues[0]);
				if(selectedValue.equals("Kloever"))
					kort.setFarge(1);
				else if(selectedValue.equals("Hjerter"))
					kort.setFarge(2);
				else if(selectedValue.equals("Ruter"))
					kort.setFarge(3);
				else if(selectedValue.equals("Spar"))
					kort.setFarge(4);
				goodInp = true;
			}
			catch(Exception NPE){
				System.out.println("Det er ikke din tur");
					}
			
		}while(!goodInp);
		return kort;
	} //end method vri
	private static void leggKort(Kort kort){
		if(dintur){
			pakke s;
			String m = "legg";
			if(hand.antKort()==0)
				m = "siste";
			
			if(hand.antKort()==2){
				m = "ett";
			}
			
			if(kort.getVerdi()==8){
				s = new pakke(m, vri(kort), navn);
			}//end if 
			else{
				s = new pakke(m, kort, navn);
			} //end else
			
		try{
			output.writeObject(s);
			output.flush();
		} //end try
		catch(IOException ioe){
			System.out.println("---------IOE");
		}//end catch
		
		} //end if dintur
		else {
			System.out.println("Det er ikke din tur");
		} //end else
	} //End method leggKort
	
	private void noenVant(String vinner){
		displayMessage("Spillet er over " + vinner + " vant!");
		spillerPanel.vinn();
	}
	
	private void displayMessage(final String messageToDisplay){
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run() {
						displayArea.append(messageToDisplay);
					}// end method run
				} //end anonymous inner class
				); //End call to invokeLater
	}// end method displayMessage
	
	public void oppdaterVindu(){
		
		if(bordet != null){
		remove(bordPanel);
		bordPanel = new bordPanel(bordet);
		bordPanel.setBackground(bordColor);
		add(bordPanel, BorderLayout.NORTH);
		}
		
		remove(spillerPanel);
		spillerPanel = new SpillerPanel(hand);
		if(dintur)
			spillerPanel.setBackground(dinturColor);
		else
			spillerPanel.setBackground(ikkedinturColor);
		add(spillerPanel, BorderLayout.CENTER);
		pack();
//		spillerPanel.updateUI();
//		setVisible(true);
	} //end method oppdater Vindu

}

