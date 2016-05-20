package chamiloda.gui.toolset;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

/**
 * This panel shows all of its components as a single vertical list. It will
 * adapt to a viewport's width, but will force its height, to give a nice
 * scrolling list of width-adjusting components. If any of the internal
 * components resizes, it will resize itself to fit the new height.
 * 
 * A horizontal scroll bar will appear only if the viewport is smaller than the
 * preferred width of one of the internal components. 
 * 
 * Being a scrollable container meant to be put in a view port, it can not have
 * a PreferredSize by itself. The result from getPreferredSize() is completely
 * determined by its contents, so setPreferredSize has no effect.
 * 
 * The components inside the list will behave the same way, forcing their
 * preferred height onto the layout, but stretching beyond their preferred
 * width.
 * 
 * @author mmeu164
 */
public class VerticalJComponentContainer extends JPanel implements Scrollable
{
    // this class should contain a list of CollapsiblePanels.
    // It should have a method of monitoring them so it can adapt its
    // own PreferredSize whenever a panel closes or opens.
    
    private Boolean resizing=false;
    private int paddingX=0;
    private int paddingY=0;
    private int paddingYBetween=0;
    
    public VerticalJComponentContainer()
    {
        super(new SpringLayout(),true);
    }
    
    public VerticalJComponentContainer(Boolean isDoubleBuffered)
    {
        super(new SpringLayout(),isDoubleBuffered);
    }
    
    public VerticalJComponentContainer(JComponent[] elements)
    {
        super(new SpringLayout(),true);
        addContent(elements);
    }

    public VerticalJComponentContainer(Boolean isDoubleBuffered, Component[] elements)
    {
        this(isDoubleBuffered, elements, 0);
    }    

    public VerticalJComponentContainer(Boolean isDoubleBuffered, Component[] elements, int padding)
    {
        this(isDoubleBuffered, elements, padding, padding, padding);
    }

    public VerticalJComponentContainer(Boolean isDoubleBuffered, Component[] elements, int paddingX, int paddingY, int paddingYbetween)
    {
        super(new SpringLayout(),isDoubleBuffered);
        this.paddingX=paddingX;
        this.paddingY=paddingY;
        this.paddingYBetween=paddingYbetween;
        addContent(elements);
    }
    
    private void addContent(Component[] elements)
    {
        for (Component component : elements)
        {
            this.add(component);
        }
        //validate();
    }

    public void setPaddings(int padding)
    {
        this.paddingX = padding;
        this.paddingY = padding;
        this.paddingYBetween = padding;
    }
    
    public int getPaddingX()
    {
        return paddingX;
    }

    public void setPaddingX(int paddingX)
    {
        this.paddingX = paddingX;
    }

    public int getPaddingY() {
        return paddingY;
    }

    public void setPaddingY(int paddingY)
    {
        this.paddingY = paddingY;
    }

    public int getPaddingYBetween()
    {
        return paddingYBetween;
    }

    public void setPaddingYBetween(int paddingYBetween)
    {
        this.paddingYBetween = paddingYBetween;
    }
    
    public void resetContainerSize()
    {
        if (this.resizing)
            return;
        this.resizing=true;
        Dimension contentSize = getContentSize();
        contentSize.height = Math.max(contentSize.height, 20);

        this.setSize(this.getWidth(), contentSize.height);
        this.resizing=false;
        this.validate();
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index)
    {
        if (comp == null)
            return;
        int currentIndex = getComponentIndex(comp);
        if (currentIndex >= 0)
            return;
        boolean wasVisible = comp.isVisible();
        comp.setVisible(false);
        super.addImpl(comp, null, index);
        for (Component cmp : this.getComponents())
            setLayout(cmp);
        comp.setVisible(wasVisible);
        this.resetContainerSize();
    }
    
    private void setLayout(Component comp)
    {
        int index = getComponentIndex(comp);
        SpringLayout layout = (SpringLayout)getLayout();
        if (index == 0)
            layout.putConstraint(SpringLayout.NORTH, comp, paddingY, SpringLayout.NORTH, this);
        else
        {
            Component prevcomp = getComponent(index-1);
            layout.putConstraint(SpringLayout.NORTH, comp, paddingYBetween, SpringLayout.SOUTH, 
                    prevcomp);
        }
        layout.putConstraint(SpringLayout.WEST, comp, paddingX, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.EAST, comp, -paddingX, SpringLayout.EAST, this);
    }
    
    @Override
    public void remove(int index)
    {
        Component component = this.getComponent(index);
        boolean wasVisible = component.isVisible();
        component.setVisible(false);
        super.remove(index);
        for (Component cmp : this.getComponents())
            setLayout(cmp);
        component.setVisible(wasVisible);
        this.resetContainerSize();
    }
    
    @Override
    public void removeAll()
    {
        super.removeAll();
        this.resetContainerSize();
    }
    
    public void removeFirst()
    {
        if (this.getComponentCount() == 0)
            return;
        this.remove(0);
    }
    
    public void removeLast()
    {
        int compcount = this.getComponentCount();
        if (this.getComponentCount() == 0)
            return;
        remove(compcount-1);
    }
    
    public int getComponentIndex(Component comp)
    {
        int index = -1;
        for (int i = 0; i < getComponents().length; i++)
        {
            if (this.getComponent(i).equals(comp))
            {
                index=i;
                break;
            }
        }
        return index;
    }

    public void moveComponentDown(Component comp)
    {
        int index = getComponentIndex(comp);
        if (index == -1 || index == getComponentCount() -1)
            return;
        this.remove(index);
        this.add(comp, index+1);        
    }

    public void moveComponentUp(Component comp)
    {
        int index = getComponentIndex(comp);
        if (index <= 0)
            return;
        this.remove(index);
        this.add(comp, index-1);                
    }

    public int getComponentYPosition(Component comp)
    {
        int index = getComponentIndex(comp);
        if (index == -1)
            return 0;
        int ypos = paddingY;
        for (int i=0; i < index; i++)
        {
            Component component = this.getComponent(i);
            if (component.isVisible())
            {
                ypos+= this.getComponent(i).getPreferredSize().getHeight();
                ypos+=paddingYBetween;
            }
        }
        return ypos;
    }

    
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        if(true)
            return getPreferredSize();
        Dimension preferredSize = this.getPreferredSize();
        Container p = getParent();
        if (p instanceof JViewport)
        {
            JViewport vp = (JViewport)p;
            preferredSize = new Dimension(vp.getWidth(), (int) preferredSize.getHeight());
        }
        return preferredSize;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 10;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if (orientation == SwingConstants.VERTICAL)
            return 10;

        Component componentAt = getComponentAt(new Point(visibleRect.x, visibleRect.y));
        int index = this.getComponentIndex(componentAt);

        if (index == -1)
            return 100;
        
        if (direction < 0)
        {// scroll up
            if (index == 0)
                return 0;
            else
                return this.getComponent(index-1).getHeight();
        }
        else
        {//scroll down
            if (index == this.getComponentCount()-1)
                return 0;                
            else
                return this.getComponent(index+1).getHeight();
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

    @Override
    public Dimension getPreferredSize()
    {
        int currentWidth = 0;
        Container p = getParent();
        if (p instanceof JViewport || p instanceof VerticalJComponentContainer)
        {
            currentWidth = p.getWidth();
        }
        else
        {
            currentWidth = super.getPreferredSize().width;
        }
        Dimension contentSize = getContentSize();
        contentSize.width = Math.max(contentSize.width, currentWidth);
        contentSize.height = Math.max(contentSize.height, 1);
        if (this.getBorder()!=null)
        {
            Insets borderInsets = this.getBorder().getBorderInsets(this);
            if (borderInsets!=null)
            {
                contentSize.height+=borderInsets.top+borderInsets.bottom;
                contentSize.width+=borderInsets.left+borderInsets.right;
            }
        }
        return contentSize;
    }

    private Dimension getContentSize()
    {
        int currentHeight = paddingYBetween*(Math.max(this.getComponentCount()-1,0)) + paddingY*2;
        int padWidth = paddingX*2;
        int currentWidth = padWidth;
        for (Component component : this.getComponents())
        {
            if (component.isVisible())
            {
                Dimension compPrefSize = component.getPreferredSize();
                currentHeight+= compPrefSize.getHeight();
                currentWidth=Math.max(currentWidth, compPrefSize.width + padWidth);
            }
        }
        return new Dimension(currentWidth, currentHeight);
    }
    
}

