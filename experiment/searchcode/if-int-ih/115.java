package header;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.Timer;

import utilities.ToggleButton;

@SuppressWarnings("serial")
public class Header extends JPanel {

	private JLabel clockLabel;
	private ToggleButton voiceButton;
    private Image image;

	public Header() {
		super();
		initialize();
	}

	/**
     * 
     */
	private void initialize() {
		ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("Icons/ice-cubes-dark.jpg"));
        image = icon.getImage();
        

		clockLabel = new JLabel();
		clockLabel.setFont(new Font("Roboto", Font.BOLD, 80));
		clockLabel.setForeground(Color.BLACK);
		clockLabel.setHorizontalTextPosition(JLabel.CENTER);
		clockLabel.setVerticalTextPosition(JLabel.CENTER);

		DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
		String time = timeFormat.format(new Date());
		clockLabel.setText(time);

		new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DateFormat timeFormat = DateFormat
						.getTimeInstance(DateFormat.SHORT);
				String time = timeFormat.format(new Date());
				clockLabel.setText(time);
			}
		}).start();

		SpringLayout layout = new SpringLayout();
		setLayout(layout);
		add(clockLabel);
		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, clockLabel, 0,
				SpringLayout.HORIZONTAL_CENTER, this);

        layout.putConstraint(SpringLayout.VERTICAL_CENTER, clockLabel, 0,
                SpringLayout.VERTICAL_CENTER, this);
		
		ImageIcon icon1 = new ImageIcon(getClass().getClassLoader().getResource("Icons/mic_off.gif"));
		ImageIcon icon2 = new ImageIcon(getClass().getClassLoader().getResource("Icons/mic_on.gif"));
            voiceButton = new ToggleButton(icon1.getImage(), icon2.getImage());
		voiceButton.setPreferredSize(new Dimension(100, 100));
		
		add(voiceButton);
        layout.putConstraint(SpringLayout.WEST, voiceButton, 0,
                SpringLayout.WEST, this);

        layout.putConstraint(SpringLayout.VERTICAL_CENTER, voiceButton, 0,
                SpringLayout.VERTICAL_CENTER, this);

	}
	
    /**
     * Tile the background with the image.
     * If the image is not null, the image is painted over the background.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int iw = image.getWidth(this);
            int ih = image.getHeight(this);
            if (iw > 0 && ih > 0) {
                for (int x = 0; x < getWidth(); x += iw) {
                    for (int y = 0; y < getHeight(); y += ih) {
                        g.drawImage(image, x, y, iw, ih, this);
                    }
                }
            }
        }
    }
}

