/*
 * Copyright (C) 2010 Aday Talavera Hierro <aday.talavera@gmail.com>
 *
 * This file is part of JASEIMOV.
 *
 * JASEIMOV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JASEIMOV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JASEIMOV.  If not, see <http://www.gnu.org/licenses/>.
 */
package jaseimov.client.controlcar;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * JPanel with a moveable red button along two axis that allow to control motor velocity and servo position.
 * @author Aday Talavera Hierro <aday.talavera@gmail.com>
 */
class VisualControlPanel 
        extends JPanel
        implements MouseListener, MouseMotionListener
{
    // Motor & Servo Managaer
    private ControlCarFrame.MotorModel motorManager;
    private ControlCarFrame.ServoModel servoManager;

    // Mouse coordinates
    private int x;
    private int y;

    // Center of the panel
    private int x0;
    private int y0;
    
    public VisualControlPanel(ControlCarFrame.MotorModel m, ControlCarFrame.ServoModel s)
    {
        motorManager = m;
        servoManager = s;
        
        this.setLayout(new java.awt.BorderLayout());

        setPreferredSize(new java.awt.Dimension(300, 300));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        
        this.addComponentListener(
            new java.awt.event.ComponentAdapter()
            {
                @Override
                public void componentResized(java.awt.event.ComponentEvent evt)
                {                              
                    resetCenter();
                }
            });
        resetCenter();
    }
         
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        final int width = getWidth();
        final int height = getHeight();        

        //Background
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        //Axes
        g.setColor(Color.black);
        g.drawLine(0, y0, width, y0);
        g.drawLine(y0, 0, y0, height);
        g.drawArc(0, 0, width-1, height-1, 0, 360);        

        //Circle
        g.setColor(Color.red);                
        g.fillOval(x-10, y-10, 20, 20);       
        //Line from center to the circle
        g.drawLine(x, y, x0, y0);
        //Border of the circle
        g.setColor(Color.black);
        g.drawOval(x-10, y-10, 20, 20);
    }
    
    private void resetCenter()
    {                
        x0 = getWidth()/2;
        y0 = getHeight()/2;
        x = x0;
        y = y0;
    }    

    public int getVelocity()
    {        
        // Calculate velocity with last value of y
        return -(int)(((double)motorManager.maxVelocity/(double)y0)*((double)y-(double)y0));
    }

    public void setVelocity(int v)
    {
        y = (int)(((double)y0/(double)motorManager.maxVelocity)*(double)(-v)+(double)y0);
        repaint();
    }

    public int getPosition()
    {
        // Calculate position with last value of x
        return (int)(((double)(servoManager.maxPosition-servoManager.startPosition)/(double)x0)*((double)x-(double)x0)+(double)servoManager.startPosition);
    }

    public void setPosition(int p)
    {
        x = (int)(((double)x0/(double)(servoManager.maxPosition-servoManager.startPosition))*((double)p-servoManager.startPosition)+(double)x0);
        repaint();
    }

    ChangeListener velocityListener;
    ChangeListener positionListener;

    private void notifyChanges()
    {
        ChangeEvent e = new ChangeEvent(this);
        velocityListener.stateChanged(e);
        positionListener.stateChanged(e);  
    }        

    public void setVelocityListener(ChangeListener l)
    {
        velocityListener = l;
    }    

    public void setPositionListener(ChangeListener l)
    {
        positionListener = l;
    }

    // Don't allow to pass this panel limits dragging the mouse
    // This prevents sending out of range values of velocity or position
    boolean avalaible = false;

    public void mouseEntered(MouseEvent e)
    {
        avalaible = true;
    }

    public void mouseExited(MouseEvent e)
    {
        avalaible = false;
    }

    public void mouseDragged(MouseEvent e)
    {
        if(avalaible)
        {
            x = e.getX();
            y = e.getY();
            repaint();
            notifyChanges();
        }
    }

    public void mouseReleased(MouseEvent e) 
    {
        if(avalaible)
            if(e.getButton() == MouseEvent.BUTTON3)
            {
                resetCenter();
                repaint();
                notifyChanges();
            }
    }

    public void mouseMoved(MouseEvent e) {}    
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}   
}

