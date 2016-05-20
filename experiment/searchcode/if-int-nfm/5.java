/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package diagram;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import umlMain.UMLClass;

import umlMain.UMLClassManager;

/**
 * This class models a lollipop style UML interface and is distinct from the
 * view interface which uses the information from an instance of this class to
 * display information.
 * @author rjp148
 */
public class UMLInterface implements Serializable{
    
    private static long itfCount = 0;
    
    private long itfID;
    
    private String itfName;
    
    //The position of the point where the interface is attached to the class.
    private Point itfPosition;
    private InterfaceDiagram viewInterface;
    
    private UMLClass classAttachedTo;
    
    
    public UMLInterface(String name){
        
        itfID = itfCount;
        itfCount++;
        
        itfName = name;
    }
    
    public String toString(){
    	return itfName;
    }

    public UMLInterface(UMLInterface umlItf) {
        
        itfID = umlItf.itfID;
        itfName = umlItf.itfName;
        
        itfPosition = umlItf.itfPosition;
        viewInterface = umlItf.viewInterface;
        classAttachedTo = umlItf.classAttachedTo;
    }
    
    /**
     * Gets the unique Interface ID number as a long.
     * @return 
     */
    public long getInterfaceID(){
        return itfID;
    }
    
    /**
     * Sets the class that this lollipop interface is attached to
     * @param umlClass 
     */
    public void attachToClass(UMLClass umlClass){
        classAttachedTo = umlClass;
    }
    
    /**
     * Gets the UMLClass that this lollipop interface is attached to.
     * @return 
     */
    public UMLClass getClassAttachedTo(){
        return classAttachedTo;
    }
    
    /**
     * Get the name for this interface.
     * @return 
     */
    public String getInterfaceName(){
        return itfName;
    }
    
    /**
     * Set the name for this interface.
     * @param newName 
     */
    public void setInterfaceName(String newName){
        itfName = newName;
    }
    
    
    /**
     * Get the position of this interface as a Point.
     * @return 
     */
    public Point getPosition(){
        return itfPosition;
    }
    
    /**
     * Set the position of this interface to a Point.
     * @param pos 
     */
    public void setPosition(Point pos){
        itfPosition = pos;
    }
    
    /**
     * Set the position of this interface with input X and Y.
     * @param pos 
     */
    public void setPosition(int x, int y){
    	Point pos = new Point(x,y);
        itfPosition = pos;
    }
    
    /**
     * Sets the View Interface that will use the information from this model
     * Interface for displaying to the user.
     * @param viewItf 
     */
    public void setViewInterface(InterfaceDiagram viewItf){
        viewInterface = viewItf;
    }
    
    public void resetView(){
        UMLClassManager mgr = classAttachedTo.getClassManager();
        try{
            viewInterface.removeFromManager();
        }catch(Exception e){
            System.out.println("haha no view yet");
        }
        InterfaceDiagram itf = new InterfaceDiagram(this);
        mgr.add(itf);
        itf.setup();
        itf.validate();
    }
    
    /**
     * Get the view Interface that uses the information from this model interface.
     * @return 
     */
    public InterfaceDiagram getViewInterface(){
        return viewInterface;
    }
    
	//Reads Interface name to find dimensions needed for interface
	public Dimension InterfacesizebyLabel(){
		Dimension Lengthheight = new Dimension();
		JLabel nameLabel = new JLabel();
		nameLabel.setText(itfName);
		int Dheight = 10;
		int Dwidth = 20;
		FontMetrics NFM = nameLabel.getFontMetrics(nameLabel.getFont());
		int Nwidth = SwingUtilities.computeStringWidth(NFM, nameLabel.getText());
		int NHeight = NFM.getHeight();
		Dwidth += Nwidth;
		
		if (!(this.itfName.isEmpty())){
			Dheight += 12 + NHeight;
		}
		
		Lengthheight.height = Dheight;
		Lengthheight.width = Dwidth;
		return Lengthheight;
	}
}

