/*
 * Copyright (c) 2006-2008 MiniMe. Code released under The MIT/X Window System
 * License. Full license text can be found in license.txt
 */
package minime.ui;

/**
 * The drawable component is used for the progress bar
 * @author chenhongbo
 */

import javax.microedition.lcdui.Graphics;

import minime.Drawable;
import minime.Image;
import minime.Observer;
import minime.Portability;
import minime.core.ResourceManager;
import minime.font.Font;
import minime.gen.FontRscId;

public class ProgressBar extends Drawable  implements Observer{
      
	private final static int DEFAULT_MAX_VALUE = 100;
	private final static int yOffsetFromCenter=5;
	private final static int YOffsetFromTop=2;
	private final static int XOffsetFromLeft=2;
	private final static int VIEW_GAP_BETWEEN_PROCESS_BAR_GRIDS=1;
	    
	//show the message of the process bar
	private Label progressMessageLabel;
	//show the current progress of the process bar
	//the currentProcess value is between 0-100
	private int  currentProcess=0;
	//store the max number of process bar grids 
	private int MAX_GRIDS_NUMBER;
	
	private ImagePanel processBarGrids[];
	private ImagePanel processBar;
	private Font textFont;
	
	/**
	 *  @param
	 *  longbar: label message of the process bar
	 *  item:    the item image of the process bar 
	 */ 
	public ProgressBar(int longbar,int item)
	{    
		 this(longbar,item,new Font(FontRscId.FONT_SYSTEM, FontRscId.FT_STYLE_PLAIN, FontRscId.FT_SIZE_MEDIUM));
	}
	public ProgressBar(int longbar,int item, Font textFont)
	{    
		 this.textFont=textFont;
		 progressMessageLabel=new Label("",textFont, Portability.C_BLACK);
		 processBar=new ImagePanel(new Image(longbar));
		 Image im_bar=new Image(longbar);
		 Image im_item=new Image(item);
		 MAX_GRIDS_NUMBER=(im_bar.getWidth()-2*XOffsetFromLeft+VIEW_GAP_BETWEEN_PROCESS_BAR_GRIDS)/(im_item.getWidth()+VIEW_GAP_BETWEEN_PROCESS_BAR_GRIDS);
		 processBarGrids=new ImagePanel[MAX_GRIDS_NUMBER];
	     for(int i=0; i<processBarGrids.length; i++)
	     {
	    		processBarGrids[i]=new ImagePanel(item);
	     }
	     setHeight(progressMessageLabel.getHeight()+yOffsetFromCenter+processBar.getHeight());
	     setWidth(Math.max(progressMessageLabel.getWidth(), processBar.getWidth()));
	     setNeedLayout() ;
	}
	
	
	public ProgressBar(int longbar,int item, int  maxGridsNumber,Font textFont)
	{    
		 this.textFont=textFont;
		 progressMessageLabel=new Label("",textFont, Portability.C_BLACK);
		 processBar=new ImagePanel(new Image(longbar));
		 MAX_GRIDS_NUMBER=maxGridsNumber;
		 processBarGrids=new ImagePanel[MAX_GRIDS_NUMBER];
	     for(int i=0; i<processBarGrids.length; i++)
	     {
	    		processBarGrids[i]=new ImagePanel(item);
	     }
	     setHeight(progressMessageLabel.getHeight()+yOffsetFromCenter+processBar.getHeight());
	     setWidth(Math.max(progressMessageLabel.getWidth(), processBar.getWidth()));
	     setNeedLayout() ;
	}
	

    public void increase()
    {
    	increase(1);
    }
    // the increased value between 0 -100
    public void increase(int delta)
    {
    	setCurrentProcess(currentProcess+delta);
    }
    
    
    /**
     * Only give the method body in the Observer interface
     */
    public void update(Object arg){
    	
    }
    
    /**  @param 
     *   args[0]: the process value between 0 -100
     *   args[1]: the process label message
     * (non-Javadoc)
     * @see util.ViewObserver#update(java.lang.Object[])
     */
	
	public void update(Object arg1,Object arg2) {
		int percentage=((Integer)arg1).intValue();
		String msg;
		if(arg2 instanceof String)
		     msg=(String)arg2;
		else
			 msg=ResourceManager.getString(((Integer)arg2).intValue());
		setBarMessage(msg);
		setCurrentProcess(percentage);
		minime.core.Runtime.getInstance().repaint();
	}

	public int getHeight() {
		setNeedLayout();
		return super.getHeight();
	}

	public int getWidth() {
		setNeedLayout();
		return super.getWidth();
	}

	
	public void renderImpl(Graphics gc) {
		int numberOfGrids = currentProcess*MAX_GRIDS_NUMBER/DEFAULT_MAX_VALUE;
		processBar.render(gc);
		for(int i=0;i<MAX_GRIDS_NUMBER&&i<numberOfGrids;i++)
		{
			processBarGrids[i].render(gc);
		}
		progressMessageLabel.render(gc);
		
	}
	
	
	protected void layoutImp() {
        // set position of process bar message and view 
		super.layoutImp();
		progressMessageLabel.layout();
		processBar.layout();
		
        int width=getWidth();
        
        progressMessageLabel.setPosition((width-progressMessageLabel.getWidth())/2,0);
        processBar.setPosition(0, progressMessageLabel.getHeight()+yOffsetFromCenter);
        //calculate the best offset from the x -side of the progress bar
        int processBarGridsX=(processBar.getWidth()-VIEW_GAP_BETWEEN_PROCESS_BAR_GRIDS*(MAX_GRIDS_NUMBER-1)-MAX_GRIDS_NUMBER*processBarGrids[0].getWidth())/2;
        int processBarGridsY=progressMessageLabel.getHeight()+yOffsetFromCenter+YOffsetFromTop;
        processBarGrids[0].setPosition(processBarGridsX, processBarGridsY);
        for(int i=1; i<processBarGrids.length; i++)
        	processBarGrids[i].setPosition(
        			processBarGrids[i-1].getLeft()+processBarGrids[i-1].getWidth()+VIEW_GAP_BETWEEN_PROCESS_BAR_GRIDS, 
        			processBarGridsY
        			);
		
	}

	public void setMessagePosition(int left, int top) {
		progressMessageLabel.setPosition(left, top);
	}

	public void setVisible(boolean visible) {
		progressMessageLabel.setVisible(visible);
	 	processBar.setVisible(visible);
    	for(int i=0; i<processBarGrids.length; i++)
    		processBarGrids[i].setVisible(visible);
	}
	
	public void setBarVisible(boolean visible)
	{
		processBar.setVisible(visible);
    	for(int i=0; i<processBarGrids.length; i++)
    		processBarGrids[i].setVisible(visible);
	}
	
	public void setMessageVisible(boolean visible){
		progressMessageLabel.setVisible(visible);
	}
	
	public void setBarMessage(int RscID){
		progressMessageLabel.setString(ResourceManager.getString(RscID));
		setNeedLayout();
	}
	
	public void setBarMessage(String message){
		progressMessageLabel.setString(message);
		setNeedLayout();
	}
   
	public int getCurrentProcess() {
		return currentProcess;
	}
    public void setCurrentProcess(int value)
    {
        if ( value<0 )
            this.currentProcess = 0;
        else if (value>DEFAULT_MAX_VALUE)
        	this.currentProcess=DEFAULT_MAX_VALUE;
        else
        	this.currentProcess=value;
    }
	
}

