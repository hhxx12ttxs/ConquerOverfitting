/**
 *  Licensed under GPL. For more information, see
 *    http://jaxodraw.sourceforge.net/license.html
 *  or the LICENSE file in the jaxodraw distribution.
 */
package net.sf.jaxodraw.object.arrow;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.jaxodraw.gui.panel.edit.JaxoEditPanel;
import net.sf.jaxodraw.gui.panel.edit.JaxoEditPanelListener;
import net.sf.jaxodraw.gui.swing.spinner.JaxoFixedJSpinner;
import net.sf.jaxodraw.util.JaxoLanguage;


/** A panel to change the parameters of an arrow.
 * @since 2.0
 */
public class JaxoDefaultArrowEditPanel extends JaxoEditPanel {
    private static final long serialVersionUID = 7526471155622776147L;
    private transient JSpinner splength;
    private transient JSpinner spwidth;
    private transient JSpinner spinset;
    private transient JCheckBox lockcb;
    private transient float lwRatio;
    private transient boolean adjusting;
    private final transient GridLayout layout = new GridLayout(0, 2);
    private final transient JaxoDefaultArrow theArrow;

    /**
     * Constructor: returns an edit panel for the given arrow.
     *
     * @param arrow the arrow to edit.
     * @param lockar Default value for the lock sub-panel.
     */
    public JaxoDefaultArrowEditPanel(JaxoDefaultArrow arrow, boolean lockar) {
        this.theArrow = arrow;
        init(lockar);
    }

    private void init(boolean lockar) {
        float length = theArrow.getArrowLength();
        float width = theArrow.getArrowWidth();
        float inset = theArrow.getArrowInset();
        this.lwRatio = length / width;

        Dimension size = new Dimension(120, 20);

        JPanel plength = getLengthPanel(length);

        JPanel pwidth = getWidthPanel(width);

        JPanel pinset = getInsetPanel(inset);

        JPanel plockar = getLockArrowPanel(lockar);

        JPanel arrEPanel = new JPanel();
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints sc = new GridBagConstraints();
        arrEPanel.setLayout(gb);

        sc.gridx = 0;
        sc.gridy = 0;
        plength.setPreferredSize(size);
        arrEPanel.add(plength, sc);

        sc.gridx = 0;
        sc.gridy = 1;
        sc.insets = new Insets(5, 0, 5, 0);
        pwidth.setPreferredSize(size);
        arrEPanel.add(pwidth, sc);

        sc.gridx = 0;
        sc.gridy = 2;
        sc.insets = new Insets(0, 0, 5, 0);
        pinset.setPreferredSize(size);
        arrEPanel.add(pinset, sc);

        sc.gridx = 1;
        sc.gridy = 0;
        sc.gridheight = 2;
        sc.insets = new Insets(0, 10, 5, 0);
        arrEPanel.add(plockar, sc);

        setLineBoxLayout();

        TitledBorder arrETitle =
            createI18NBorder("JaxoArrowOptionsEditPanel.title");
        this.setBorder(arrETitle);
        this.add(arrEPanel);
    }

    /** {@inheritDoc} */
    public void addEditPanelListener(JaxoEditPanelListener listener) {
        if (listener != null) {
            splength.addChangeListener(listener);
            spwidth.addChangeListener(listener);
            spinset.addChangeListener(listener);
        }
    }

      //
     // private methods
    //

    private JPanel getLengthPanel(float length) {
        JPanel plength = new JPanel();
        plength.setLayout(layout);

        JLabel jlength = createI18NLabel("JaxoArrowOptionsEditPanel.length");
        plength.add(jlength);

        SpinnerNumberModel modelLength =
            new SpinnerNumberModel(
                new Float(length), new Float(0.f), null, new Float(1.f));

        this.splength = new JaxoFixedJSpinner(modelLength);
        splength.setName("splen");
        splength.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (adjusting) {
                        return;
                    }
                    if (lockcb.isSelected()) {
                        adjusting = true;
                        try {
                            float len =
                                ((Number) splength.getValue()).floatValue();
                            float wid = len / lwRatio;
                            spwidth.setValue(new Double(wid));
                            theArrow.setArrowLength(len);
                            theArrow.setArrowWidth(wid);
                        } finally {
                            adjusting = false;
                        }
                    } else {
                        float len = ((Number) splength.getValue()).floatValue();
                        theArrow.setArrowLength(len);
                    }
                }
            });
        plength.add(splength);

        return plength;
    }

    private JPanel getWidthPanel(float width) {
        JPanel pwidth = new JPanel();
        pwidth.setLayout(layout);

        JLabel jwidth = createI18NLabel("JaxoArrowOptionsEditPanel.width");
        pwidth.add(jwidth);

        SpinnerNumberModel modelWidth =
            new SpinnerNumberModel(
                new Float(width), new Float(0.f), null, new Float(1.f));

        this.spwidth = new JaxoFixedJSpinner(modelWidth);
        spwidth.setName("spwid");
        spwidth.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (adjusting) {
                        return;
                    }
                    if (lockcb.isSelected()) {
                        adjusting = true;
                        try {
                            float wid =
                                ((Number) spwidth.getValue()).floatValue();
                            float len = lwRatio * wid;
                            splength.setValue(new Double(len));
                            theArrow.setArrowLength(len);
                            theArrow.setArrowWidth(wid);
                        } finally {
                            adjusting = false;
                        }
                    } else {
                        float wid = ((Number) spwidth.getValue()).floatValue();
                        theArrow.setArrowWidth(wid);
                    }
                }
            });
        pwidth.add(spwidth);

        return pwidth;
    }

    private JPanel getInsetPanel(float inset) {
        JPanel pinset = new JPanel();
        pinset.setLayout(layout);

        JLabel jinset = createI18NLabel("JaxoArrowOptionsEditPanel.inset");
        pinset.add(jinset);

        SpinnerNumberModel modelinset =
            new SpinnerNumberModel(new Float(inset),
                new Float(0.f), new Float(1.f), new Float(0.05f));

        this.spinset = new JaxoFixedJSpinner(modelinset);
        spinset.setName("spinset");
        spinset.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    float ins = ((Number) spinset.getValue()).floatValue();
                    theArrow.setArrowInset(ins);
                }
            });
        pinset.add(spinset);

        return pinset;
    }

    private JPanel getLockArrowPanel(boolean lockar) {
        JPanel plockar = new JPanel();
        plockar.setLayout(layout);

        lockcb =
            new JCheckBox(JaxoLanguage.translate("JaxoArrowOptionsEditPanel.lockar"));
        lockcb.setName("lockar");
        lockcb.setEnabled(true);
        lockcb.setSelected(lockar);
        lockcb.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (lockcb.isSelected()) {
                        float len =
                            ((Number) splength.getValue()).floatValue();
                        float wid = ((Number) spwidth.getValue()).floatValue();
                        lwRatio = len / wid;
                    }
                }
            });
        plockar.add(lockcb);

        return plockar;
    }

}

