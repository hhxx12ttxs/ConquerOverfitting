import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
class Graph extends JPanel {
	int y[]; // y-axis (inputted)
	int x[]; // x-axis (computed)
	public Graph(int y[]) {
		this.y = y;
		x = new int[y.length];
		setBackground(Color.white);
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int n = 0;
		for (int j = 0;j < x.length;j++) {
			x[j] = n;
			n+=(getWidth()/x.length);
		}
		g.setColor(Color.blue);
		g.drawPolyline(x,y,x.length);
	}
}
public class Example extends JFrame implements ActionListener {
	JTextField text[]; // input fields
	JButton button;
	JLabel label[];
	public Example() {
		super("Example");
		Container c = getContentPane();
		c.setLayout(new GridLayout(0,2));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		text = new JTextField[10];
		label = new JLabel[text.length];
		// below is default GUI setup
		for (int j = 0;j < text.length;j++) {
			label[j] = new JLabel("Y value #"+(j+1)+": ");
			text[j] = new JTextField();
			c.add(label[j]);
			c.add(text[j]);
		}
		button = new JButton("Graph!");
		c.add(button);
		button.addActionListener(this);
		pack();
		setSize(new Dimension(400,getHeight()));
		show();
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button) {
			int j;
			int y[] = new int[text.length];
			for (j = 0;j < y.length;j++) {
				try {
					y[j] = Integer.parseInt(text[j].getText());
				}
				catch (NumberFormatException ne) {
					// one of the fields isnt an
					// acceptable number, so forget about it
					return;
				}
			}
			// here the GUI needs to be rebuilt
			Container c = getContentPane();
			for (j = 0;j < text.length;j++) {
				c.remove(text[j]);
				c.remove(label[j]);
			}
			c.remove(button);
			c.setLayout(new BorderLayout());
			c.add(new Graph(y),BorderLayout.CENTER);
			c.validate(); // <-- that is the key method to update the GUI
		}
	}
	public static void main(String args[]) {
		new Example();
	}
}

