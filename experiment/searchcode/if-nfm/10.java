package diagram;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import umlMain.DependencyModel;


/**
 * This class stores the data side of the External Dependencies the ExternalDependecyModel
 * class uses this class to display each external dependency.
 */

public class ExternalDependency implements Serializable{
	  
    private static long ExDCount = 0;
    
    private long ExD_ID;
    
    private String ExDName;
    private String ExDFullName;
    
    private Point ExDPosition;
    private ExD_Diagram ExDview;
    private DependencyModel myDep;
    
    public ExternalDependency(String name){
        
        ExD_ID = ExDCount;
        ExDCount++;
        
        ExDFullName = name;
        if (ExDFullName.lastIndexOf('/')!=-1){
        	ExDName = ExDFullName.substring(ExDFullName.lastIndexOf('/')+1);
        }
        else{
        	ExDName = ExDFullName;
        }
    }

    public ExternalDependency(ExternalDependency ExtDep) {
        
        ExD_ID = ExtDep.ExD_ID;
        ExDFullName = ExtDep.ExDFullName;
        ExDName = ExtDep.ExDName;
        
        ExDPosition = ExtDep.ExDPosition;
        ExDview = ExtDep.ExDview;
    }
    
    /**
     * Gets the unique External Dependency ID number as a long.
     * @return 
     */
    public long getExtDepID(){
        return ExD_ID;
    }
    
    /**
     * Get the full name for this External Dependency.
     * @return 
     */
    public String getExDFullName(){
        return ExDFullName;
    }
    
    /**
     * Get the name for this External Dependency.
     * @return 
     */
    public String getExDName(){
        return ExDName;
    }
    
    /**
     * Set the full name for this External dependency.
     * this also sets the short name.
     * @param newName 
     */
    public void setExDFullName(String newFullName){
        ExDFullName = newFullName;
        if (ExDFullName.lastIndexOf('/')!=-1){
        	ExDName = ExDFullName.substring(ExDFullName.lastIndexOf('/')+1);
        }
        else{
        	ExDName = ExDFullName;
        }
    }
     
    /**
     * Get the position of this external dependency as a Point.
     * @return 
     */
    public Point getPosition(){
        return ExDPosition;
    }
    
    /**
     * Set the position of this external dependency to a Point.
     * @param pos 
     */
    public void setPosition(Point pos){
        ExDPosition = pos;
    }
    
    /**
     * Set the position of this external dependency with input X and Y.
     * @param pos 
     */
    public void setPosition(int x, int y){
    	Point pos = new Point(x,y);
        ExDPosition = pos;
    }
    
    /**
     * Sets the External dependency View that will use the information from this model for displaying to the user.
     * @param viewItf 
     */
    public void setExDView(ExD_Diagram exdview){
    	ExDview = exdview;
    }
    
    /**
     * Get the External dependency view that uses the information from this model.
     * @return 
     */
    public ExD_Diagram getExDView(){
        return ExDview;
    }
    
    /**Reads external dependency name to find dimensions needed for external dependency
     * @return
	 */
	public Dimension ExDsizebyLabel(){
		Dimension Lengthheight = new Dimension();
		JLabel nameLabel = new JLabel();
		nameLabel.setText(ExDName);
		int Dheight = 10;
		int Dwidth = 20;
		FontMetrics NFM = nameLabel.getFontMetrics(nameLabel.getFont());
		int Nwidth = SwingUtilities.computeStringWidth(NFM, nameLabel.getText());
		int NHeight = NFM.getHeight();
		Dwidth += Nwidth;
		
		if (!(this.ExDName.isEmpty())){
			Dheight += 12 + NHeight;
		}
		
		Lengthheight.height = Dheight;
		Lengthheight.width = Dwidth;
		return Lengthheight;
	}
	
	public void addDependency(DependencyModel dm) {
		myDep = dm;
	}
	
	public boolean hasDependency() {
		if (myDep != null) {
			return true;
		}
		return false;
	}
	
	public DependencyModel getDependency() {
		return myDep;
	}
}


