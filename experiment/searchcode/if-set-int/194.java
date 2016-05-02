/**
 * Copyright (c) 2006-2008 MiniMe. Code released under The MIT/X Window System
 * License. Full license text can be found in license.txt
 */
package minime.ui;

import javax.microedition.lcdui.*;

import minime.Drawable;
import minime.alignment.AlignmentStrategy;
import minime.alignment.CenterAlignmentStrategy;
import minime.core.ResourceManager;
import minime.font.Font;
import minime.ui.style.StyleManager;

/**
 * The Label class is used to hold label,it can have frame.If not set text
 * position, string will be rendered at the middle of the Label.
 * 
 * @author Liang
 * 
 */
public class Label extends Drawable
{
    private String label;
    private Font font;
    private int textColor;
    
    private int labelX, labelY;

    boolean forcedWidth = false ;
    boolean forcedHeight = false ;
    
    private AlignmentStrategy alignmentStrategy;
    
    protected Label()
    {
    }
    /**
     * Creates an instance of label by given source Label instance,it will copy
     * the attributes from source Label instance.
     * 
     * @param l
     *            the source Label istance.
     */
    public Label(Label l)
    {
        this(l.label, l.font, l.textColor, l.frameThickness, l.frameColor);
        setRscId(l.getRscId()) ;
    }

    /**
     * Creates an instance of label by given text, font, text color, frame
     * thickness and frame color.
     * 
     * @param label
     *            the Label text
     * @param font
     *            the font for render label
     * @param textColor
     *            the color for label
     */
    public Label(String label, Font font, int textColor)
    {
        this(label, font, textColor, 0, 0);
    }

    /**
     * Creates an instance of label by given text, and text color.
     * 
     * @param label
     *            the Label text
     * @param textColor
     *            the color for label
     */
    public Label(String label, int textColor)
    {
        this(label, StyleManager.getStyleFont(StyleManager.LABEL_FONT), textColor);
    }

    /**
     * Creates an instance of label by given text, font, text color, frame
     * thickness and frame color.If font is null, font will be the default
     * font:FontManager.LABEL_FONT
     * 
     * @param label
     *            the Label text
     * @param font
     *            the font for render label
     * @param textColor
     *            the color for label
     * @param framethickness
     *            the frame thickness
     * @param frameColor
     *            the frame color
     */
    public Label(String label, Font font, int textColor, int framethickness, int frameColor)
    {
        this.label = label;
        if (font != null)
            this.font = font;
        else
            this.font = StyleManager.getStyleFont(StyleManager.LABEL_FONT);
        this.textColor = textColor;
        this.frameThickness = framethickness;
        // if frameSickness==0 means no frame
        if (framethickness > 0)
            needFrame = true;
        this.frameColor = frameColor;
        alignmentStrategy = new CenterAlignmentStrategy();
        setNeedLayout() ;
    }

    /**
     * Creates an Label instance with given text resource id, font, text color,
     * frame thickness and frame color. the text will be gotten from resource
     * file with text resource index id by ResourceManager.If want to set text
     * to null, set txRscId to ResourceManager.NONE_RESOURCE.
     * 
     * @param txRscId
     *            the text resource index id for getting text from resource
     *            file, if want to set text to null, txRscId should be
     *            ResourceManager.NONE_RESOURCE.
     * @param font
     *            the font for label text
     * @param textColor
     *            the text color
     * @param framethickness
     *            the frame thickness
     * @param frameColor
     *            the frame color
     */
    public Label(int txRscId, Font font, int textColor, int framethickness, int frameColor)
    {
        this(ResourceManager.getString(txRscId), font, textColor, framethickness, frameColor);
        setRscId(txRscId);
    }

    /**
     * Creates an Label instance with given text resource id, font, text
     * color.the text will be gotten from resource file with text resource index
     * id by ResourceManager.If want to set text to null, set txRscId to
     * ResourceManager.NONE_RESOURCE.
     * 
     * @param txRscId
     *            the text resource index id for getting text from resource
     *            file, if want to set text to null, txRscId should be
     *            ResourceManager.NONE_RESOURCE.
     * @param font
     *            the font for label text
     * @param textColor
     *            the text color
     */
    public Label(int txRscId, Font font, int textColor)
    {
        this(txRscId, font, textColor, 0, 0);
    }

    /**
     * Creates an Label instance with given text resource id, text color. The
     * font will be set to the default font.the text will be gotten from
     * resource file with text resource index id by ResourceManager.If want to
     * set text to null, set txRscId to ResourceManager.NONE_RESOURCE.
     * 
     * @param txRscId
     *            the text resource index id for getting text from resource
     *            file, if want to set text to null, txRscId should be
     *            ResourceManager.NONE_RESOURCE.
     * @param textColor
     *            the text color
     */
    public Label(int txRscId, int textColor)
    {
        this(txRscId, StyleManager.getStyleFont(StyleManager.LABEL_FONT), textColor);
    }
    
    /**
     * Set the AlignmentStrategy for Label
     * @param alignmentStrategy the new strategy
     */
	public void setAlignmentStrategy(AlignmentStrategy alignmentStrategy) {
		setNeedLayout();
		this.alignmentStrategy = alignmentStrategy;
	}

    public void renderImpl(Graphics gc)
    {
        if (label != null)
        {
        	drawLabel(gc);
        }
    }

    private void drawLabel(Graphics gc)
    {
    	gc.translate(labelX, labelY) ;
       	alignmentStrategy.render(gc);
    	gc.translate(-labelX, -labelY) ;
    }
    
    public void layoutImp()
    {
        // if no font or no label, nothing to do ..
    	if (font==null || label==null)
    	{
    		if(!forcedWidth)
    			super.setWidth(0);
    		if(!forcedHeight)
    			super.setHeight(0);
    		return ;
    	}

    	if (!forcedWidth)
    	{    	
    		// align with no width
        	int textWidth = alignmentStrategy.align(font, textColor, label) ;
	    	int w = textWidth + (frameThickness * 2) + 2  ;  // leave 1 dot space between frame and left & right of the string
	    	super.setWidth(w);   
    	}
    	else
    	{
    		int w = Math.max(0, getWidth() - (frameThickness * 2) - 2) ; // leave 1 dot space between frame and left & right of the string
    		// align within the width
    		alignmentStrategy.align(font, textColor, label, w) ;
    	}
    	
    	// text already align (on X), set the label height if needed 
    	if (!forcedHeight)
    	{
    		int h=alignmentStrategy.getHeight() + (frameThickness * 2) + 2 ;
	        super.setHeight(h) ;
    	}
    	
    	int alignMaxWidth = alignmentStrategy.getMaxWidth() ;
    	int alignHeight = alignmentStrategy.getHeight() ;
    	// we center the text. labelX & Y can be < 0 but no pb as label is cliped
		labelX = (getWidth() - alignMaxWidth) / 2 ;
		labelY = (getHeight() - alignHeight) / 2 ;
    }
    

    /**
	 * set text for editfieldTitleLabel, and size calculated by layout
	 * 
	 * @param text
	 *            string for editfieldTitleLabel
	 */
    public void setString(String text)
    {
        this.label = text;
        setNeedLayout();
    }

    /**
	 * set text for editfieldTitleLabel, and size calculated by layout, the text
	 * will be drawed from text resource file based on text resource id.If want
	 * to set text to null, set txRscId to ResourceManager.NONE_RESOURCE.
	 * 
	 * @param txRscId
	 *            the text resource index id for getting text from resource
	 *            file, if want to set text to null, txRscId should be
	 *            ResourceManager.NONE_RESOURCE.
	 */
    public void setString(int txRscId)
    {
        setString(ResourceManager.getString(txRscId));
        setRscId(txRscId) ;
    }

    /**
     * Add a string to label.
     * 
     * @param content
     *            the new added string
     */
    public void append(String content)
    {
        if (content != null)
        {
            if (label != null)
                label += content;
            else
                label = content;

            setString(label);
        }
    }

    /**
     * Add a string to label.This added string is gotten from resource file
     * based on given text resource id,If want to append nothing, set txRscId to
     * ResourceManager.NONE_RESOURCE.
     * 
     * @param txRscId
     *            the text resource id.If want to append nothing, set txRscId to
     *            ResourceManager.NONE_RESOURCE.
     */
    public void append(int txRscId)
    {
        append(ResourceManager.getString(txRscId));
    }

    /**
     * @deprecated
     * should use setText(); layout() ;
     * set text for editfieldTitleLabel, and set string width as Label width
     * 
     * @param text
     *            string for editfieldTitleLabel
     */
    
    public void setStrWithStrWidth(String text)
    {
        this.label = text;
        setWidth(font.getStringWidth(label));
        setNeedLayout() ;
    }

    /**
     * @deprecated
     * should use setText(); layout() ;
     * 		
     * set text for editfieldTitleLabel, and set string width as Label width,
     * the text will be drawn from text resource file based on text resource
     * id.If want to set text to null, set txRscId to
     * ResourceManager.NONE_RESOURCE.
     * 
     * @param txRscId
     *            the text resource index id for getting text from resource
     *            file, if want to set text to null, txRscId should be
     *            ResourceManager.NONE_RESOURCE.
     */
    public void setStrWithStrWidth(int txRscId)
    {
        setStrWithStrWidth(ResourceManager.getString(txRscId));
        setRscId(txRscId) ;
    }

    /**
     * Gets the label text
     * 
     * @return the label text
     */
    public String getString()
    {
        return label;
    }

    /**
     * Returns the font.
     * 
     * @return font instance.
     */
    public Font getFont()
    {
        return font;
    }

    /**
     * Sets the color for rendering label text.
     * 
     * @param textColor
     *            the color for label text
     */
    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
    }

    public void setWidth(int w)
    {
    	forcedWidth = (w != 0);
    	super.setWidth(w) ;
    }

    public void setHeight(int h)
    {
    	forcedHeight = (h != 0) ;
    	super.setHeight(h) ;
    }
	public int getTextColor() {
		return textColor;
	}
	
	public void setFont(Font font) 
	{
		this.font = font;
		setNeedLayout();
	}
}

