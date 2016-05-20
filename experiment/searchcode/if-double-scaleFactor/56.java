/*
 
 Image Operations

   1. The region of interest is defined by the bounding box of the source Image. This bounding box
      is specified in Image Space, which is the Image object's local coordinate system.
      
   2. If an AffineTransform is passed to drawImage(Image, AffineTransform, ImageObserver), 
   	  the AffineTransform is used to transform the bounding box from image space to user space. 
   	  If no AffineTransform is supplied, the bounding box is treated as if it is already in user 
   	  space.
   	  
   3. The bounding box of the source Image is transformed from user space into device space using
      the current Transform. Note that the result of transforming the bounding box does not 
      necessarily result in a rectangular region in device space.
      
   4. The Image object determines what colors to render, sampled according to the source to 
      destination coordinate mapping specified by the current Transform and the optional image 
      transform. 
*/


package jlogo.core.worksheet;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.event.*;
import java.awt.image.*;
import java.io.*;

import jlogo.WorkSheetModel;
import jlogo.config.CurrentLocale;
import jlogo.config.ImageUtils;
import jlogo.config.JTransparency;
import jlogo.gui.ReportDialog;

//1: vd metodoi 'checkImage()' dei pannelli Source e Dest
//2: usare il CurrentLocale
//3: cambiare il cursore nel SourcePanel con l'icona del 'pick color'

/*
 *  // update text field when the slider value changes
     s.addChangeListener(new ChangeListener()  { 
     	
     	public void stateChanged(ChangeEvent event)  {
     	
     	  	   JSlider source = (JSlider)event.getSource();
               textField.setText("" + source.getValue());
            }
        });

 * 
 */

/**
 *
 * @author  
 */
public class TransparencySelectionDialog extends JDialog  {
 
	private static final long serialVersionUID = 1L;

	/*  */
    private BufferedImage sourceImage;
    
    /*  */
    private Image destinationImage;
    
    private int image_x, image_y;
    private int image_width, image_height;
    private double scaleFactor;
    private boolean needScale; 
          
    /** range di trasparenza */
    private int range;
   
    /** */  
    private	Color color;
    /** */  
    private	Point bkgSourceImagePoint;
    	
    //TexturePanel
    private SourcePanel sourcePanel;
    private DestinationPanel destinationPanel;
    
    //PANNELLI
    private JPanel backgroundPanel; /* */
    private JPanel settingsPanel;
      
    //BOTTONI
    private JButton cancelBtn, okBtn;
    
    //ETICHETTE
    private JLabel rangeLabel;
    private JLabel colorLabel;

    //SLIDER
    private JSlider rangeSlider;
    
    //SPINNER
    private JSpinner rangeSpinner;
    
    //COLOR BUTTON
    private ColorButton colorButton;
    
    Container cp;
  
    /**
     * 
     *  COSTRUTTORE
     */
	public TransparencySelectionDialog() {
		super();
              
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
      
        // posiziona la Dialog 
        setLocation(400,400);
		
        
        
        needScale = false;
        range = 50;
        bkgSourceImagePoint = new Point(0,0);
                
        //CARICA LE IMMAGINI
        caricaImmagini();
             
        color = new Color(sourceImage.getRGB(0,0));
        
        //DEBUG
        	System.out.println("(1) Colore: " + color);
        
       
      
        //rangeSlider
        rangeSlider = new JSlider(JSlider.HORIZONTAL,0,100,0);
    	rangeSlider.setPaintTicks(true);
		rangeSlider.setSnapToTicks(true);
		rangeSlider.setPaintTrack(true);
		rangeSlider.setPaintLabels(true);
		rangeSlider.setMajorTickSpacing(25);
		rangeSlider.setMinorTickSpacing(5);
		rangeSlider.setToolTipText("Scegli un valore per il RANGE di transparenza");
		
        rangeSlider.setValue(range);
        
        // LISTENER per la slider
        rangeSlider.addChangeListener(new ChangeListener(){
          
            public void stateChanged(ChangeEvent e) {
            	            	
                if(!rangeSlider.getValueIsAdjusting()) {
                	                  	  
                	range = rangeSlider.getValue();	
                    destinationImage = JTransparency.makeColorTransparent(sourceImage,color,range);
                    destinationPanel.repaint();
                }
            }
        });
        
        //SPINNER
        rangeSpinner = new JSpinner();
        //rangeSpinner.setModel(rangeSlider.getModel()); // Share model
      
        // BOTTONI 'ok' e 'cancel'
        cancelBtn = new JButton(CurrentLocale.getString("Cancel"));
        okBtn = new JButton(CurrentLocale.getString("Accept"));
        
        //LISTENER sul bottone 'cancel'
        cancelBtn.addActionListener(new ActionListener(){

              public void actionPerformed(ActionEvent e){
            	  
            	  dispose();
              }
        });

        // LISTENER sul bottone 'ok'
        okBtn.addActionListener(new ActionListener(){
    
            public void actionPerformed(ActionEvent e){
              
            	((ImageLayer)WorkSheetModel.selected).setImage(destinationImage);
         
            	dispose();
            }
        });
        
//      LISTENER sul bottone 'colorButton'
        colorButton = new ColorButton(color);
        colorButton.addActionListener(new ActionListener() {

        	public void actionPerformed(ActionEvent e) {
        		        	
        		final JDialog dialog =new JDialog();
        		final ColorPicker colorPicker = new ColorPicker();

        		dialog.setLayout(new BorderLayout());
        		dialog.setBounds(( (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth() ) / 2, 
        				((int)Toolkit.getDefaultToolkit().getScreenSize().getHeight() ) / 2,
        				160,
        				140
        		);
        		
        		colorPicker.setBounds(0,0,160,40);
        		dialog.add(BorderLayout.CENTER, colorPicker);
        		
        		JButton acceptButton = new JButton(CurrentLocale.getString("Accept"));
        		acceptButton.addActionListener(new ActionListener(){
        			public void actionPerformed(ActionEvent e){
        				dialog.dispose();
        			}
        		});
        		acceptButton.setBounds(0,40,160,30);
        		
        		dialog.add(BorderLayout.SOUTH,acceptButton);
        		dialog.addWindowListener(new WindowAdapter(){
        			
        			public void windowClosed(WindowEvent e){
        				//cosě creo un oggetto da capo --> evitare!
        				//color = new Color(colorPicker.getColor());
        				color = colorPicker.getColor();
        				colorButton.setBackground(color);			
        			}
        		});
        		dialog.setVisible(true);
        		
        	}
        });	
       
        //crea la GUI
        createGUI();
        
        //PLACE IMAGE
        posizionaImmagine();
                           
        System.out.println("(1) color:" + color);
    }
	
	/*
	 *  posiziona l'immagine nel SourcePanel e DestinationPanel
	 *  restringendola se necessario
	 */
    public void posizionaImmagine(){
    	
    	 int panel_width = sourcePanel.getWidth();
         int panel_height = sourcePanel.getHeight();
         /*
         double x,y;
         
         System.out.println("\n\n panel_width: " + panel_width + "  panel_height: "+ panel_height);
         
         boolean jump = false;
            	
    	 if(image_width > panel_width) {
    		 
    		 System.out.println("\n ramo 1");
    		 
    		 needScale = true;
    		 
    		 x = (image_width * 100)/ panel_width;
    		 y = x-100;
    		 scaleFactor = 1 - (y/100);     	
    		 
    		 //scaleFactor = ((((image_width * 100)/ panel_width)-100)/100);
    		 
    		 jump = true;
    		 
    		 //System.out.println("\n X: =" + x); 
    		 //System.out.println("\n Y =" + y); 
    		 System.out.println("\n Fattore di scale =" + scaleFactor);    		    		
    	 }
    	 else if(image_height > panel_height && !jump ) {
    		 
    		 System.out.println("\n ramo 2");
    		 
    		 needScale = true;
    		 
    		 x = (image_height * 100)/ panel_height;
    		 y = x-100;
    		 scaleFactor = 1 - (y/100);   
    		 
    		 //scaleFactor = ((((image_height * 100)/ panel_height)-100)/100);    		 
    		 
    		 System.out.println("\n Fattore di scale 2 =" + scaleFactor);	     		 
    	 }
    	 
    	 jump = false;
    	 */    	 
    	 // centra l'immagine nel Pannello 
    	 image_x = panel_width/2 - image_width/2;
         image_y = panel_height/2 - image_height/2;
    }
    
    // CaricaImmagini()
    public void caricaImmagini() {
        
        destinationImage = ((ImageLayer)WorkSheetModel.selected).getOriginalImage();
        
        if(destinationImage==null) 
        	new ReportDialog(null,new IOException(),"Errore caricamento immagine dal Layer");
        
        sourceImage = ImageUtils.toBufferedImage(destinationImage);
        
        image_width = sourceImage.getWidth();
        image_height = sourceImage.getHeight();
        
        System.out.println("\n image_width: " + image_width + "  image_height: "+ image_height);
        
        
    	/*  try {           
            destinationImage = (Image)ImageIO.read(JLogoApplication.class.getResource("icone/logo.jpg"));
        }
        catch(IOException e) {        
            System.out.println(e);
        }
        MediaTracker media = new MediaTracker(this);
        media.addImage(destinationImage,0);
    
        try {
          
        	media.waitForID(0);
            sourceImage = ImageUtils.toBufferedImage(destinationImage);
            image_width = sourceImage.getWidth();
            image_height = sourceImage.getHeight();
        } 
        catch(InterruptedException e) { 
        	System.out.println(e);
        }
        */      
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new TransparencySelectionDialog(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }
    
    /**
     * crea la GUI
     */
    public void createGUI(){
    	
    	rangeLabel = new JLabel("Range");
        colorLabel = new JLabel("Colore");
        sourcePanel = new SourcePanel();
        destinationPanel = new DestinationPanel();
        backgroundPanel = new JPanel(); 
        settingsPanel = new JPanel();
               
        //LAYOUT
        backgroundPanel.setLayout(null);
        settingsPanel.setLayout(null);
        
        //SIZE
        backgroundPanel.setPreferredSize(new java.awt.Dimension(486, 500));

        //BORDI
        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        sourcePanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        destinationPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        
        // POSIZIONI
        sourcePanel.setBounds(20, 20, 208, 200);
        destinationPanel.setBounds(258, 20, 208, 200);
        settingsPanel.setBounds(20, 240, 440, 140);
         
        destinationPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        
        //nel SettingsPanel
        rangeLabel.setBounds(90, 30, 51, 20);
        rangeSpinner.setBounds(170, 30, 40, 20);
        rangeSlider.setBounds(240, 30, 150, 50);
        colorLabel.setBounds(90, 100, 40, 14);
        colorButton.setBounds(180,100,20,20);
        
        okBtn.setBounds(290, 450, 80, 25);
        cancelBtn.setBounds(380, 450, 80, 25);
        
        //aggiunge i COMPONENTI
        settingsPanel.add(rangeLabel);
        settingsPanel.add(colorLabel);
        settingsPanel.add(rangeSlider);   
        settingsPanel.add(rangeSpinner);
        settingsPanel.add(colorButton);
   
        backgroundPanel.add(sourcePanel);
        backgroundPanel.add(destinationPanel);
        backgroundPanel.add(settingsPanel);
        backgroundPanel.add(okBtn);
        backgroundPanel.add(cancelBtn);

        cp = getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(backgroundPanel,BorderLayout.CENTER);
        pack();
      
    }
    
    /**
     *  mostra la dialog
     * (chiamato da WSCP)
     */
    public void showDialog() {
    	setVisible(true);    	
    }

/**
 * INNER CLASS 1
 */
	class SourcePanel extends TexturePanel implements MouseListener{
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		public SourcePanel() {
			addMouseListener(this);
			
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			AffineTransform at = new AffineTransform();
			
			if (sourceImage != null ) {
				
				if(needScale) {
									
					at.translate(image_x, image_y);
					at.scale(scaleFactor,scaleFactor);
					at.translate(-image_x, -image_y);
					
					g2.drawImage(sourceImage, at, null);
				}
				else {
					g2.drawImage(sourceImage, image_x, image_y, null);
					//System.out.println("\n SOURCE IMAGE  disegnata su sourcePanel");
				}
			}
		}

		// MOUSE PRESSED
		public void mousePressed(MouseEvent e) {
			
			bkgSourceImagePoint.setLocation(e.getX() - image_x,e.getY() - image_y);

			try {
				//cosě creo un oggetto da capo --> evitare!
				color = new Color(sourceImage.getRGB(
						(int) bkgSourceImagePoint.getX(), (int) bkgSourceImagePoint.getY()));
				
				colorButton.setBackground(color);
				
				//System.out.println("\nuovo colore: " + color + "alla posizione \n");
				//System.out.println("X: " + ((int)bkgSourceImagePoint.getX()) + " Y: " + ((int)bkgSourceImagePoint.getY()));

			}
			catch (ArrayIndexOutOfBoundsException ex) {
				
				System.out.println("\nERRORE");
				System.out.println("X: " + ((int)bkgSourceImagePoint.getX()) + " Y: " + ((int)bkgSourceImagePoint.getY()));
			}
						
			destinationImage = JTransparency.makeColorTransparent(sourceImage, color, range);
			destinationPanel.repaint();
			
		}
		 
		public void mouseDragged(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseClicked(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}

	}

/**
 * 
 * INNER CLASS 2
 * 
 */
class DestinationPanel extends TexturePanel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * COSTRUTTORE 
	 *
	 */
	public DestinationPanel() {	
		
		
		
	}

	/**
	 * DISEGNO
	 */
	public void paintComponent(Graphics g) {
		
		//senza questa chiamata la Texture non compare sullo sfondo
		super.paintComponent(g);
		Graphics2D g2=(Graphics2D)g;
		
		AffineTransform at = new AffineTransform();
		
		if(destinationImage != null ) {
			
			if(needScale) {
				at.scale(scaleFactor,scaleFactor);
				//at.translate(image_x - image_width/2, image_y - image_height/2);
				g2.drawImage(sourceImage, at, null);
			}
			else {
				
				g2.drawImage(destinationImage,image_x,image_y,null);
				//System.out.println("\n DESTINATION IMAGE  disegnata su DestinationPanel");
			}
			
		}
		else {
			System.out.println("\n DESTINATION IMAGE null");
		}
	}

}

public void mouseClicked(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

public void mouseEntered(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

public void mouseExited(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

public void mouseReleased(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

/**
 * 
 * INNER CLASS 3
 */
class ColorButton extends JButton {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ColorButton(Color color) {
		setBackground(color);	
	}
}

}//chiude PUBLIC CLASS

  
