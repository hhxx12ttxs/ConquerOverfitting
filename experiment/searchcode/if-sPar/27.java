package VriNettbasert;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import VriNettbasert.Kort.Kortfarge;

public class bordPanel extends JPanel{
	static Icon[] kortene;
	static JButton bordLabel;
		public bordPanel(){
			add(new JLabel("Venter p at spillet skal starte"));
			
		}

		public bordPanel(Kort bordet){
			setLayout(new FlowLayout() );
			kortene = new Icon[52];
			for(int i=0;i<=51;i++){ //Legger inn bilde av hvert kort i et array.
				String path = new String(Integer.toString(i+1) + ".png");
				Icon k = new ImageIcon( getClass().getResource(path));
				kortene[i]=k;
			}
			int faktor=0; //faktor angir hvilket bilde som skal brukes for hvilket kort
			if(bordet.getFarge() == Kortfarge.Kloever) 
				faktor = bordet.getVerdi();
			if(bordet.getFarge() == Kortfarge.Spar) 
				faktor = 13 + bordet.getVerdi();
			if(bordet.getFarge() == Kortfarge.Hjerter) 
				faktor = 26 + bordet.getVerdi();
			if(bordet.getFarge() == Kortfarge.Ruter) 
				faktor = 39 + bordet.getVerdi();
			faktor--;
			System.out.println("faktor = " + faktor);
			bordLabel = new JButton(kortene[faktor]);
			bordLabel.setBorderPainted(false);
			add(bordLabel);
		}
	
	
}

