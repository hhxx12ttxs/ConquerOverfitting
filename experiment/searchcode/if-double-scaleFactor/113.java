package pages.contents;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import main.MainPanel;

import utilities.Settings;

import dataBase.ContentsObject;

@SuppressWarnings("serial")
public class ItemPanel extends JPanel {
    private ActionListener al;
    private MainPanel mainPanel;
    private ContentsObject item;
    private JPanel header;
    private JPanel main;
    private JPanel imagePanel;
    private static final int IMAGE_X = 200;
    private static final int IMAGE_Y = 150;
    private JButton addToListBtn;
    private ActionListener addToListBtnListener;
    private JButton locateBtn;
    private JLabel name;
    private JLabel brand;
    private JLabel purchased;
    private JLabel expires;
    private JLabel remaining;
    private JTextPane ingredients;
    private Font littleFont;
    private Font bigFont;
    
    public ItemPanel(MainPanel mainPanel, ActionListener addToShoppingListListener) {
        super();
        this.mainPanel = mainPanel;
        addToListBtnListener = addToShoppingListListener;
        initialize();
    }
    
    private void initialize() {
        setLayout(new BorderLayout());
        setBackground(Settings.instance().background);
        
        al = new ActionListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void actionPerformed(ActionEvent e) {
                setItem((ContentsObject) ((JList<Object>)e.getSource()).getSelectedValue());
            }
        };
        
        littleFont = new Font("Roboto", Font.PLAIN, 20);
        bigFont = new Font("Roboto", Font.PLAIN, 25);
        
        header = new JPanel();
        header.setBackground(Settings.instance().background);
        GridBagLayout gbl1 = new GridBagLayout();
        gbl1.columnWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0};
        gbl1.columnWidths = new int[] {20, 80, 20, 20, 20};
        gbl1.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
        gbl1.rowHeights = new int[] {20, 0, 20, 0, 0, 20};
        header.setLayout(gbl1);
        GridBagConstraints c = new GridBagConstraints();
        imagePanel = new JPanel();
        imagePanel.setPreferredSize(new Dimension(IMAGE_X, IMAGE_Y));
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setBackground(Settings.instance().background);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 4;
        header.add(imagePanel, c);
        addToListBtn = new JButton("<html><center>" + "Add to" + "<br>" + "Shopping List" + "</html>");
        addToListBtn.setPreferredSize(new Dimension(150, 50));
        addToListBtn.addActionListener(new AddToListener());
        addToListBtn.setFont(new Font("Roboto", Font.PLAIN, 17));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        header.add(addToListBtn, c);
        locateBtn = new JButton("Locate");
        locateBtn.setPreferredSize(new Dimension(150, 50));
        locateBtn.setFont(new Font("Roboto", Font.PLAIN, 17));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        header.add(locateBtn, c);
        add(header, BorderLayout.NORTH);
        
        main = new JPanel();
        main.setBackground(Settings.instance().background);
        GridBagLayout gbl2 = new GridBagLayout();
        gbl2.columnWeights = new double[] {0.0, 1.0, 0.0, 1.0, 0.0};
        gbl2.columnWidths = new int[] {20, 0, 10, 0, 20};
        gbl2.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        gbl2.rowHeights = new int[] {20, 25, 25, 25, 25, 25, 25, 25, 25, 25};
        main.setLayout(gbl2);
        JLabel n = new JLabel("Item:");
        n.setHorizontalAlignment(JLabel.RIGHT);
        n.setVerticalAlignment(JLabel.BOTTOM);
        n.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(n, c);
        name = new JLabel();
        name.setVerticalAlignment(JLabel.BOTTOM);
        name.setFont(bigFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(name, c);
        JLabel b = new JLabel("Brand:");
        b.setHorizontalAlignment(JLabel.RIGHT);
        b.setVerticalAlignment(JLabel.BOTTOM);
        b.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(b, c);
        brand = new JLabel();
        brand.setVerticalAlignment(JLabel.BOTTOM);
        brand.setFont(bigFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(brand, c);
        JLabel p = new JLabel("Purchase Date:");
        p.setHorizontalAlignment(JLabel.RIGHT);
        p.setVerticalAlignment(JLabel.BOTTOM);
        p.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(p, c);
        purchased = new JLabel();
        purchased.setVerticalAlignment(JLabel.BOTTOM);
        purchased.setFont(bigFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(purchased, c);
        JLabel e = new JLabel("Expiration Date:");
        e.setHorizontalAlignment(JLabel.RIGHT);
        e.setVerticalAlignment(JLabel.BOTTOM);
        e.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(e, c);
        expires = new JLabel();
        expires.setVerticalAlignment(JLabel.BOTTOM);
        expires.setFont(bigFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(expires, c);
        JLabel r = new JLabel("Amount Left:");
        r.setHorizontalAlignment(JLabel.RIGHT);
        r.setVerticalAlignment(JLabel.BOTTOM);
        r.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(r, c);
        remaining = new JLabel();
        remaining.setVerticalAlignment(JLabel.BOTTOM);
        remaining.setFont(bigFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(remaining, c);
        JLabel i = new JLabel("Ingredients:");
        i.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 1;
        c.gridheight = 1;
        main.add(i, c);
        ingredients = new JTextPane();
        ingredients.setEditable(false);
        ingredients.setOpaque(false);
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setAlignment( sas, StyleConstants.ALIGN_JUSTIFIED);
        ingredients.setParagraphAttributes( sas, false);
        ingredients.setFont(littleFont);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 3;
        c.gridheight = 1;
        main.add(ingredients, c);
        
        add(main, BorderLayout.CENTER);
        
        this.setVisible(false);
        
    }
    
    public void setItem(ContentsObject item) {
        this.item = item;
        imagePanel.removeAll();
        
        BufferedImage affter = scaleImage(item.getImage(), IMAGE_X, IMAGE_Y);
        ImageIcon newIcon = new ImageIcon(affter);
        JLabel myImage = new JLabel(newIcon);
        imagePanel.add(myImage, BorderLayout.CENTER);
        
        name.setText(item.getName());
        brand.setText(item.getBrand());
        purchased.setText(item.getPurchased().toString());
        expires.setText(item.getPurchased().toString());
        remaining.setText(item.getAmountRemaining());
        ingredients.setText(item.getIngredients());
        
        this.setVisible(true);
    }
    
    public BufferedImage scaleImage(BufferedImage image, int maxWidth, int maxHeight) {
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();
        double scaleFactor = 0.0;
        double scaleFactorX = 0.0;
        double scaleFactorY = 0.0;
        if (originalWidth > maxWidth) {
            scaleFactorX = ((double) maxWidth / originalWidth);
        }
        if (originalHeight > maxHeight) {
            scaleFactorY = ((double) maxHeight / originalHeight);
        }
        scaleFactor = (scaleFactorX < scaleFactorY)?scaleFactorX:scaleFactorY;
        if (scaleFactor != 0) {
            // create smaller image
            BufferedImage img = new BufferedImage((int) (originalWidth * scaleFactor), (int) (originalHeight * scaleFactor), BufferedImage.TYPE_INT_ARGB);
            // fast scale (Java 1.4 & 1.5)
            Graphics g = img.getGraphics();
            g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
            return img;
        }
        return image;
    }

    public ActionListener getActionListener() {
        return al;
    }
    
    private class AddToListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (addToListBtnListener != null) {
                mainPanel.setPage(MainPanel.SHOPPINGLISTPAGE);
                new java.util.Timer().schedule( 
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                addToListBtnListener.actionPerformed(new ActionEvent(item,
                                        ActionEvent.ACTION_PERFORMED,
                                        item.getName()));
                            }
                        }, 
                        200 
                );
                
            }
        }
        
    }
}

