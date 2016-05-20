package VriNettbasert;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import java.io.Serializable;

import VriNettbasert.Kort.Kortfarge;

public class CopyOfSpillerPanel extends JPanel implements Serializable {
	static Icon [] kortene;
	static JButton [] kortLabel;
	static JLabel vinn;
	static JCheckBox ettKort;
	static Spiller sp;
	static int[] vk = {400};
	boolean ett 	= false;
	boolean Tett 	= false;
	boolean save 	= false;
	boolean load 	= false;
	boolean ferdig 	= false;
	
public CopyOfSpillerPanel(Spiller spiller) { //Constructor
		
		setLayout(new FlowLayout() ); //Setter layouten 
		sp = spiller;
		kortene = new Icon [52]; //Kortstokken
		for(int i=0;i<=51;i++){ //Legger inn bilde av hvert kort i et array.
			String path = new String(Integer.toString(i+1) + ".png");
			Icon k = new ImageIcon( getClass().getResource(path));
			kortene[i]=k;
		}
		kortLabel = new JButton[20]; //maks 20 kort paa hand
		for(int t=0;t<20;t++){
			JButton tom = new JButton("tom"); //lager 20 JButtons som er tomme
			kortLabel[t] = tom;
		}
		ButtonHandler handler = new ButtonHandler();
		
for(int i=0;i<sp.antKort();i++){ //dekoder kortene spilleren har p hand
	int faktor=1;
			if(sp.visKort(i).getFarge() == Kortfarge.Kloever) 
				faktor = sp.visKort(i).getVerdi();	
			if(sp.visKort(i).getFarge() == Kortfarge.Spar) 
				faktor = 13 + sp.visKort(i).getVerdi();
			if(sp.visKort(i).getFarge() == Kortfarge.Hjerter) 
				faktor = 26 + sp.visKort(i).getVerdi();
			if(sp.visKort(i).getFarge() == Kortfarge.Ruter) 
				faktor = 39 + sp.visKort(i).getVerdi();
			faktor--;
			//legger ut kortene som JButtons
			kortLabel[i] = new JButton(Integer.toString(i), kortene[faktor]);
			kortLabel[i].setBorderPainted(false);
			kortLabel[i].addActionListener(handler);
			kortLabel[i].setVerticalTextPosition(SwingConstants.BOTTOM);
			add(kortLabel[i]);
		} //end for ant kort	

	ettKort = new JCheckBox("Ett Kort Igjen?");
	CheckBoxHandler cbh = new CheckBoxHandler();
	ettKort.addItemListener(cbh);
	add(ettKort);
	JButton trekk = new JButton("Trekk ett kort");
	TrekkHandler th = new TrekkHandler();
	trekk.addActionListener(th);
	add(trekk);
	vinn = new JLabel("Noen vant - spillet er over");
	vinn.setVisible(false);
	
}
public void vinn(){
	vinn.setVisible(true);
}
public boolean ferdig () {
	
	return ferdig;
}
public int[] valgtKort(){
	//System.out.println("Du valgte kort nr :" + vk[0]);
	return vk;
}
public boolean ettkortigjen(){
	return ett;
}
public boolean trekkEtt(){
	return Tett;
}
public boolean save() {
	return save;
}
public boolean load() {
	return load;
}
public void resett(){
	ferdig 	= false;
	vk 		= new int [] {400};
	ett		= false;
	Tett	= false;
	save 	= false;
	load 	= false;
}

//Handlers

private class ButtonHandler implements ActionListener {
	public void actionPerformed(ActionEvent event){
		int tmp = Integer.parseInt(event.getActionCommand());
		System.out.println("Action Event: " +  tmp);
		VriKlient.lesValg(tmp);
		
		
		
		
		vk[0] = tmp;
		ferdig = true;
	}
} //end class ButtonHandler
private class CheckBoxHandler implements ItemListener {
	public void itemStateChanged( ItemEvent event){
		if(ettKort.isSelected()) ett = true;
		else ett= false;
	}
} //end class CheckBoxHandler
private class TrekkHandler implements ActionListener {
	public void actionPerformed(ActionEvent event) {
		System.out.println(event.getActionCommand());
		if(event.getActionCommand() == "Trekk ett kort"){
			VriKlient.trekkEttKort();
				//VriKlient.lesValg((String)event.getActionCommand());
			
		Tett 	= true;
		ferdig 	= true;
		}
		else if(event.getActionCommand() == "Save game") {
			//save game
			
			//	VriKlient.lesSpesialValg((String)event.getActionCommand());
			
			
			save 	= true;
			ferdig 	= true;
			
		}
		else if(event.getActionCommand() == "Load game") {
			//load game
			
			//	VriKlient.lesSpesialValg((String)event.getActionCommand());
			
			
			load 	= true;
			ferdig	= true;
			
		}
	}
}

	
}//end class TrekkHandler


