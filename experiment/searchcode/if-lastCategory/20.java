package josekitmark;

import com.ioshq.util.BeanPropertyUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLDocument;

/**
 * @author Kit
 * @date Mar 31, 2010
 */
public class PanelRight extends JPanel implements ActionListener {

    private MassTownDataExplorer parent;
    private JPanel gridPanel;
    private HashMap<TownVariable, JCheckBox> checkboxes;
    private HashMap<TownVariable, JLabel> valueLabels;

    public PanelRight(MassTownDataExplorer parent) {
        // setup a box layout for this
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //this.setLayout(new FlowLayout());
        //this.setPreferredSize(new Dimension(WIDTH, HEIGHT / 3));
        this.parent = parent;

		JEditorPane instructionsPane = new JEditorPane();
        instructionsPane.setBackground(Color.white);
        instructionsPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        instructionsPane.setContentType("text/html");
		//Font font = UIManager.getFont("Label.font");
        Font font = new Font("sansserif", Font.PLAIN, 12);
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)instructionsPane.getDocument()).getStyleSheet().addRule(bodyRule);
        String instructionsText = "<html><b>Instructions</b><br>";
        instructionsText += "To display a variable on the map, use the drop-down menu above.  ";
        instructionsText += "Move your mouse pointer over towns on the map to view detailed data below.  ";
        instructionsText += "You may click up to 5 towns on the map you wish to compare.  ";
        instructionsText += "Check up to 4 variables below to use in comparing the selected towns.  ";
        instructionsText += "CTRL + click-and-drag to Zoom.  ";
        instructionsText += "SHIFT + click-and-drag to Pan.  ";
        instructionsText += "Clicking on the map will restore the original zoom level.  ";
        instructionsText += "</html>";
        instructionsPane.setText(instructionsText);
        this.add(instructionsPane);

        // Initialize our HashMaps for storing the value labels and checkboxes.
        checkboxes = new HashMap<TownVariable, JCheckBox>();
        valueLabels = new HashMap<TownVariable, JLabel>();

        initializeTownInfo();

		this.add(Box.createVerticalGlue());
        this.updateUI();
    }

    public void actionPerformed(ActionEvent e) {
		int numChecked = 0;
        for(JCheckBox c : checkboxes.values())
        {
            if (c.isSelected())
                numChecked++;
        }
        if (numChecked > (MassTownDataExplorer.chartXDim * MassTownDataExplorer.chartYDim))
        {
            JCheckBox clicked = (JCheckBox) e.getSource();
            clicked.setSelected(false);
        }
        else
        {
            parent.updateChart();
		}
    }

    public HashMap<TownVariable, JCheckBox> getCheckBoxes() {
        return checkboxes;
    }

    @Override
    public void paint(Graphics g) {
        // paint my contents first...
        // then, make sure lightweight children paint
        super.paint(g);
    }

    public void initializeTownInfo() {
        TownVariableCategory lastCategory = null;
        TownVariable[] sortedVariables = TownVariable.sortedValues();
        gridPanel = new JPanel();
        gridPanel.setBackground(Color.white);
        gridPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

		/*
        JLabel instructions = new JLabel("Select variables to use for displaying the charts.");
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        instructions.setFont(new Font("sansserif", Font.BOLD, 13));
        gridPanel.add(instructions, c);
        instructions = new JLabel("Mouse over towns to view data.");
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        gridPanel.add(instructions, c);
		 */

        double checkboxWidth = 0.0;
        double valueWidth = 1.0;
        double labelWidth = 0.0;
        int y = 2;

        for (int i = 0; i < sortedVariables.length; i++) {
            TownVariable tv = sortedVariables[i];
            if (tv.isDisplayed()) {
                // new category, display category name
                if (!tv.category().equals(lastCategory)) {
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.gridx = 1;
                    c.gridy = y;
                    c.gridwidth = 3;
                    JLabel emptyLabel = new JLabel(" ");
                    emptyLabel.setFont(new Font("sansserif", Font.PLAIN, 5));
                    //gridPanel.add(TextUtils.createRegularTextPane(" "), c);
                    gridPanel.add(emptyLabel, c);
                    y++;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.gridx = 1;
                    c.gridy = y;
                    c.gridwidth = 3;
                    JLabel titleLabel = new JLabel(tv.category().getCategoryName());
                    titleLabel.setFont(new Font("sansserif", Font.BOLD, 13));
                    gridPanel.add(titleLabel, c);
                    lastCategory = tv.category();
                    y++;
                }

                if (tv.isMeasure()) // Add a checkbox
                {
                    JCheckBox cb = new JCheckBox();
                    cb.setBackground(Color.white);
                    cb.addActionListener(this);
                    checkboxes.put(tv, cb);
                    c.weightx = checkboxWidth;
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.ipadx = 5;
                    c.gridx = 0;
                    c.gridy = y;
                    c.gridwidth = 1;
                    gridPanel.add(cb, c);
                }

                c.weightx = labelWidth;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 1;
                c.gridy = y;
                c.gridwidth = 1;
                c.ipadx = 10;
                JLabel displayName = new JLabel(tv.displayName() + ":");
                displayName.setFont(new Font("sansserif", Font.PLAIN, 12));
                gridPanel.add(displayName, c);

                JLabel valueLabel = new JLabel(" ");
                valueLabel.setFont(new Font("sansserif", Font.PLAIN, 12));
                valueLabels.put(tv, valueLabel);
                c.weightx = valueWidth;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = 2;
                c.gridy = y;
                c.ipadx = 0;
                c.gridwidth = 1;
                gridPanel.add(valueLabel, c);
                y++;
            }
        }
        this.add(gridPanel);
        //this.updateUI();
    }

    public void updateTownInfo(Town town) {
        TownVariable[] sortedVariables = TownVariable.sortedValues();
        for (TownVariable tv : sortedVariables) {
            if (tv.isDisplayed()) {
                if (tv.isMeasure()) {
                    Float val = town.getValue(tv);
                    if (val == null) {
                        valueLabels.get(tv).setText("No Data");
                    } else {
                        valueLabels.get(tv).setText(tv.formatVal(val));
                    }
                } else {
                    try {
                        String val = (String) BeanPropertyUtil.getProperty(tv.propertyName(), town);
                        if (val == null) {
                            valueLabels.get(tv).setText("No Data");
                        } else {
                            valueLabels.get(tv).setText(val);
                        }
                    } catch (ClassCastException e) {
                        valueLabels.get(tv).setText("ClassCastException");
                    }
                }
            }
        }
    }
}

