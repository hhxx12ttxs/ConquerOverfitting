package GUI;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;

public class Choices extends JPanel implements ActionListener {

	private JButton level1, tuturial, highScore;
	
	private Game gam;
	private Tutorial tut;
	private GreetingWindow gW;
	private Lionheart.highScore hS = new Lionheart.highScore();
	
	public Choices(GreetingWindow greetingWindow){
		
		this.gW = greetingWindow;
	
		this.setLayout(new GridLayout(3, 1));
		
		level1 = new JButton("Level 1");
		this.add(level1);
		level1.addActionListener(this);
		
		 tuturial = new JButton("Tutorial");
		 this.add( tuturial);
		 tuturial.addActionListener(this);
		
		highScore = new JButton("Highscore");
		this.add(highScore);
		highScore.addActionListener(this);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String aC = e.getActionCommand();

		if(aC.equals("Level 1")){
			gam = new Game(aC);
			gW.setVisible(false);
		}
		else if(aC.equals("Tutorial")){
			tut = new Tutorial();
		}
		else if(aC.equals("Highscore")){
			try {
				hS.load("highscore.txt");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			hS.outTxtFile();
		}
	}
	

	
	
}

