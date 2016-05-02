import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalButtonUI;

public class filterDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Color[] colortable;
	private DecimalFormat data = new DecimalFormat("#0.00");
	private double upperDataTemp;
	private double lowerDataTemp;
	
	public double upperData;
	public double lowerData;
	
	// Global components
	private JSlider upperSlider = new JSlider();
	private JSlider lowerSlider = new JSlider();
	private JLabel upperNum = new JLabel(data.format(upperData));
	private JLabel lowerNum = new JLabel(data.format(lowerData));
	private JButton upperBar = new JButton();
	private JButton lowerBar = new JButton();
	
	@SuppressWarnings("static-access")
	public filterDialog(Color[] colors, double upper, double lower) {
		setIconImage(new ImageIcon(new MainClass().getClass().getResource("App1.png")).getImage());
		setTitle("Color Filter");
		
		this.colortable = colors;
		this.upperData = upper;
		this.lowerData = lower;
	}
	
	public void showFilterDialog(Component parent){
		// contentpane
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
		// page_start text area
		JTextArea textbox = new JTextArea();
		textbox.setBorder(BorderFactory.createLoweredBevelBorder());
		textbox.setEditable(false);
		textbox.setLineWrap(true);
		textbox.setWrapStyleWord(true);
		String text = 	"Using the slinding scale below you can " +
						"set an upper and/or lower threshold for " +
						"the color scale of an s-plot. All windows having " +
						"a Pearson correlation value above the upper threshold " + 
						"will be drawn in the color specified in the upper " + 
						"threshold color block. Likewise, all windows having " +
						"a Pearson correlation value below the lower threshold " +
						"will be drawn in the color block. The upper and lower " +
						"threshold colors can be changed by clicking on the " +
						"corresponding color blocks.";
		textbox.setText(text);
		textbox.setMinimumSize(new Dimension(309,72));
		textbox.setCaretPosition(0);
		JScrollPane scrollPane = new JScrollPane(textbox);
		scrollPane.setPreferredSize(new Dimension(309,72));
		scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// setting bar
		JPanel settingspane = new JPanel(new GridLayout(0,1));
		
		JPanel upperThres = new JPanel(new GridLayout(0,1));
		TitledBorder upperTitle;
		upperTitle = BorderFactory.createTitledBorder("Upper Threshold");
		upperTitle.setTitleJustification(TitledBorder.LEFT);
		upperThres.setBorder(upperTitle);
		JPanel upperA = new JPanel(new GridBagLayout());
		JLabel upperValue = new JLabel("Value");
		upperValue.setBorder(BorderFactory.createLoweredBevelBorder());
		upperSlider.setOrientation(JSlider.HORIZONTAL);
		upperSlider.setMinimum((int)(frame.lowerDataDefault*100.0));
		upperSlider.setMaximum((int)(frame.upperDataDefault*100.0));
		upperSlider.setValue((int)(upperData*100.0));
		upperSlider.setMajorTickSpacing(10);
		upperSlider.setMinorTickSpacing(0);
		upperSlider.setPaintTicks(true);
		upperNum.setText(data.format(upperData));
		upperNum.setBorder(BorderFactory.createLoweredBevelBorder());
		upperNum.setMinimumSize(new Dimension(41,18));
		upperNum.setMaximumSize(new Dimension(41,18));
		upperNum.setPreferredSize(new Dimension(41,18));
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(2,2,2,2);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_START;
			c.gridx = 0;
			c.gridy = 0;
			upperA.add(upperValue,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 1.0;
		    //c.gridwidth = 4;
			c.gridx = 1;
			c.gridy = 0;
			upperA.add(upperSlider,c);
			c.weightx = 0;
			//c.gridwidth = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_END;
			c.gridx = 2;
		    c.gridy = 0;
		    upperA.add(upperNum,c);
		JPanel upperB = new JPanel(new GridBagLayout());
		JLabel upperColor = new JLabel("Color");
		upperColor.setBorder(BorderFactory.createLoweredBevelBorder());
		upperBar.setUI(new MetalButtonUI());
		upperBar.setBackground(colortable[0]);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_START;
			c.gridx = 0;
			c.gridy = 1;
			upperB.add(upperColor,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 1.0;
		    //c.gridwidth = 5;
			c.gridx = 1;
			c.gridy = 1;
			upperB.add(upperBar,c);
			c.weightx = 0;
			c.gridwidth = 0;
		upperThres.add(upperA);
		upperThres.add(upperB);
		
		JPanel lowerThres = new JPanel(new GridLayout(0,1));
		TitledBorder lowerTitle;
		lowerTitle = BorderFactory.createTitledBorder("Lower Threshold");
		lowerTitle.setTitleJustification(TitledBorder.LEFT);
		lowerThres.setBorder(lowerTitle);
		JPanel lowerA = new JPanel(new GridBagLayout());
		JLabel lowerValue = new JLabel("Value");
		lowerValue.setBorder(BorderFactory.createLoweredBevelBorder());
		lowerSlider.setOrientation(JSlider.HORIZONTAL);
		lowerSlider.setMaximum((int)(frame.upperDataDefault*100.0));
		lowerSlider.setMinimum((int)(frame.lowerDataDefault*100.0));
		lowerSlider.setValue((int)(lowerData*100.0));
		lowerSlider.setMajorTickSpacing(10);
		lowerSlider.setMinorTickSpacing(0);
		lowerSlider.setPaintTicks(true);
		lowerNum.setText(data.format(lowerData));
		lowerNum.setBorder(BorderFactory.createLoweredBevelBorder());
		lowerNum.setMinimumSize(new Dimension(41,18));
		lowerNum.setMaximumSize(new Dimension(41,18));
		lowerNum.setPreferredSize(new Dimension(41,18));
			c = new GridBagConstraints();
			c.insets = new Insets(2,2,2,2);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_START;
			c.gridx = 0;
			c.gridy = 0;
			lowerA.add(lowerValue,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.CENTER;
			c.weightx = 1.0;
		    //c.gridwidth = 4;
			c.gridx = 1;
			c.gridy = 0;
			lowerA.add(lowerSlider,c);
			c.weightx = 0;
			//c.gridwidth = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_END;
			c.gridx = 2;
		    c.gridy = 0;
		    lowerA.add(lowerNum,c);
		JPanel lowerB = new JPanel(new GridBagLayout());
		JLabel lowerColor = new JLabel("Color");
		lowerColor.setBorder(BorderFactory.createLoweredBevelBorder());
		lowerBar.setUI(new MetalButtonUI());
		lowerBar.setBackground(colortable[5]);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_START;
			c.gridx = 0;
			c.gridy = 1;
			lowerB.add(lowerColor,c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.anchor = GridBagConstraints.LINE_END;
			c.weightx = 1.0;
		    //c.gridwidth = 5;
			c.gridx = 1;
			c.gridy = 1;
			lowerB.add(lowerBar,c);
			c.weightx = 0;
			c.gridwidth = 0;
		lowerThres.add(lowerA);
		lowerThres.add(lowerB);
		
		settingspane.add(upperThres);
		settingspane.add(lowerThres);
		
		// buttons
		JPanel buttonpane = new JPanel();
		buttonpane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonpane.setLayout(new BoxLayout(buttonpane,BoxLayout.LINE_AXIS));
		JButton defaultColor = new JButton("Default Value");
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		buttonpane.add(defaultColor);
		buttonpane.add(Box.createHorizontalGlue());
		buttonpane.add(ok);
		buttonpane.add(Box.createHorizontalGlue());
		buttonpane.add(cancel);
		
		// add everything
		contentPane.add(scrollPane,BorderLayout.PAGE_START);
		contentPane.add(settingspane,BorderLayout.CENTER);
		contentPane.add(buttonpane,BorderLayout.PAGE_END);
		
		// set dialog frame
		this.setContentPane(contentPane);
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(parent);
		
		// set datatemp
		upperDataTemp = upperData;
		lowerDataTemp = lowerData;
		
		// event listeners
		upperSlider.addChangeListener(this.new SliderListener());
		lowerSlider.addChangeListener(this.new SliderListener());
		upperBar.addActionListener(this.new ButtonListener());
		lowerBar.addActionListener(this.new ButtonListener());
		ok.addActionListener(this.new ButtonOK());
		cancel.addActionListener(this.new ButtonCancel());
		defaultColor.addActionListener(this.new ButtonDefault());
	}
	
	// ok cancel default buttons
	class ButtonOK implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			frame.upperData = upperDataTemp;
			frame.lowerData = lowerDataTemp;
			
			firePropertyChange("filter_update", null, null);
			
			dispose();
		}
	}
	class ButtonCancel implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	class ButtonDefault implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			upperDataTemp = frame.upperDataDefault;
			lowerDataTemp = frame.lowerDataDefault;
			upperSlider.setValue((int)(upperDataTemp*100.0));
			upperNum.setText(data.format(upperDataTemp));
			lowerSlider.setValue((int)(lowerDataTemp*100.0));
			lowerNum.setText(data.format(lowerDataTemp));
		}
	}
	class SliderListener implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        if (!source.getValueIsAdjusting()) {
	        	if(source.equals(upperSlider)){
	        		upperDataTemp = ((double)source.getValue()) / 100.00;
	        		upperNum.setText(data.format(upperDataTemp));
	        	}
	        	if(source.equals(lowerSlider)){
	        		lowerDataTemp = ((double)source.getValue()) / 100.00;
	        		lowerNum.setText(data.format(lowerDataTemp));
	        	}
	        }
	    }
	}
	class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton)e.getSource();
			if (source.equals(upperBar)){
				Color newColor = JColorChooser.showDialog(
                        upperBar.getParent(),
                        "Choose Upper Threshold Color",
                        colortable[0]);
				if (newColor != null) {
					colortable[0] = newColor;
					upperBar.setBackground(newColor);
				}
			}
			if (source.equals(lowerBar)){
				Color newColor = JColorChooser.showDialog(
                        lowerBar.getParent(),
                        "Choose Lower Threshold Color",
                        colortable[5]);
				if (newColor != null) {
					colortable[5] = newColor;
					lowerBar.setBackground(newColor);
				}
			}
		}
    }
}

