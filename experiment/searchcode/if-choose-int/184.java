package fe.s100502030;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;


public class FE11 extends JApplet {
	private ImageIcon[] cats = new ImageIcon[3];
	private ImageIcon[] dogs = new ImageIcon[3];
	private ImageIcon[] mouses = new ImageIcon[3];

	private JLabel imageLabel = new JLabel();

	private JButton previous = new JButton("Previous");
	private JButton next = new JButton("Next");

	private int choose = 0;
	private int index = 0;
	
	private JFrame  f = new JFrame();
	JLabel message = new JLabel();
	JButton button = new JButton("??");
	// private int index2 = 0;
	// private int index3 = 0;

	public FE11() {
		// set Action
		Action catAction = new MyAction("cat", new ImageIcon(getClass()
				.getResource("image/cat_icon.jpg")));
		Action dogAction = new MyAction("dog", new ImageIcon(getClass()
				.getResource("image/dog_icon.jpg")));
		Action mouseAction = new MyAction("mouse", new ImageIcon(getClass()
				.getResource("image/mouse_icon.jpg")));

		for (int i = 0; i < 3; i++) {
			cats[i] = new ImageIcon(getClass().getResource(
					"image/cat" + (i + 1) + ".jpg"));
			dogs[i] = new ImageIcon(getClass().getResource(
					"image/dog" + (i + 1) + ".jpg"));
			mouses[i] = new ImageIcon(getClass().getResource(
					"image/mouse" + (i + 1) + ".jpg"));
		}

		JMenuBar jmb = new JMenuBar();
		setJMenuBar(jmb);
		JMenu menu = new JMenu("Animals Gallery");
		jmb.add(menu);
		// additem to menu
		menu.add(catAction);
		menu.add(dogAction);
		menu.add(mouseAction);

		JToolBar jtb = new JToolBar();
		// additem to toolbar
		jtb.add(catAction);
		jtb.add(dogAction);
		jtb.add(mouseAction);

		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(imageLabel, BorderLayout.CENTER);
		p1.add(jtb, BorderLayout.NORTH);

		JPanel p2 = new JPanel();
		p2.add(previous);
		p2.add(next);

		setLayout(new BorderLayout());
		add(p1, BorderLayout.CENTER);
		add(p2, BorderLayout.SOUTH);
		
		//alert frame
		f.setSize(100,100);
		f.setLayout(new GridLayout(2,1));
		f.setTitle("??");
		f.setLocationRelativeTo(null);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(false);
		
		
		
		JPanel p3 = new JPanel();
		p3.add(button);
		f.add(message);
		f.add(p3);

		previous.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				index--;
				if (index < 0) {
					index = 0;
					message.setText("??????");
					f.setVisible(true);
				}
				// previous image
				if (choose == 1)
					imageLabel.setIcon(cats[index]);
				else if (choose == 2) {
					imageLabel.setIcon(dogs[index]);
				} else if(choose == 3)
					imageLabel.setIcon(mouses[index]);
				
				
			}
		});
		
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				index++;
				if (index >= 3) {
					index = 2;
					message.setText("??????");
					f.setVisible(true);
				}
				// next image
				if (choose == 1)
					imageLabel.setIcon(cats[index]);
				else if (choose == 2) {
					imageLabel.setIcon(dogs[index]);
				} else if(choose==3)
					imageLabel.setIcon(mouses[index]);
			}
		});
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				f.setVisible(false);
			}
		});
	}

	private class MyAction extends AbstractAction {
		String name;

		public MyAction(String name, Icon icon) {
			super(name, icon);
			putValue(Action.SHORT_DESCRIPTION, name + "");
			this.name = name;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			// choose gallery
			index = 0;
			if (name.equals("cat")) {
				choose = 1;
				imageLabel.setIcon(cats[index]);
			} else if (name.equals("dog")) {
				choose = 2;
				imageLabel.setIcon(dogs[0]);
			} else if (name.equals("mouse")) {
				choose = 3;
				imageLabel.setIcon(mouses[0]);
			}

		}
	}

}

