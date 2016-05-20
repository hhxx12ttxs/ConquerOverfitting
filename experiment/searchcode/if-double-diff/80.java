package vknob;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * VKnob is a swing component similar to
 * com.dreamfabric.DKnob, like a slider but round.
 * @see com.dreamfabric.DKnob
 */
public class VKnob extends JComponent {
    private final static float START = 225;
    private final static float LENGTH = 270;
    private final static float PI = 3.1415f;
    private final static float START_ANG = (START/360)*PI*2;
    private final static float LENGTH_ANG = (LENGTH/360)*PI*2;
    private final static float DRAG_RES = 0.01f;
    private final static float MULTIP = 180f / PI;

    private final static Color DEFAULT_FOCUS_COLOR = new Color(0xc0c0ff);
    private final static Color KNOB_COLOR = new Color(240, 240, 220);
                                         // new Color(0xe0e0c7)
    private final static Color KNOB_TOP_COLOR = new Color(250, 250, 230);
    private final static Color SHADOW_COLOR = Color.gray;
    private final static Color SHINE_COLOR = Color.white;
    private final static Color STRIPES_COLOR = new Color(192, 192, 192);
                                            // Color.gray

    private final int SHADOWX = 1;
    private final int SHADOWY = 1;
    private final float DRAG_SPEED = 0.01F;
    private final float CLICK_SPEED = 0.01F;
    //private int size;
    private int middle;

    // drag type (round or simple)
    public final static int SIMPLE = 1;
    public final static int ROUND  = 2;
    private int dragType = ROUND;

    private final static Dimension MIN_SIZE  = new Dimension(40, 40);
    private final static Dimension PREF_SIZE = new Dimension(80, 80);

    // Set the antialiasing to get the right look
    private final static RenderingHints AALIAS = 
        new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

    private ChangeEvent changeEvent = null;
    private EventListenerList listenerList = new EventListenerList();

    private Arc2D hitArc = new Arc2D.Float(Arc2D.PIE);

    private float val;           // internal value 0 <= val <= 1
    private VKnobModel model;    // for conversion val <--> value
    private Object value = null; // known as the value of the knob
    private VKnobSnapper snapper;

    private float ang = (float) START_ANG;
    private int dragpos = -1;
    private float startVal;
    private Color focusColor;
    private double lastAng;

    /**
     * A knob with null as the only value and no snapping.
     *
     * Call setModel(VKnobModel) and setSnapper(VKnobSnapper) to get
     * a more interesting knob.
     *
     * @see #setModel(VKnobModel)
     * @see #setSnapper(VKnobSnapper)
     */
    public VKnob() { this(null, new DefaultVKnobSnapper()); }

    /** A knob with user-defined values model and a default snapper. */
    public VKnob(VKnobModel model) { this(model, new DefaultVKnobSnapper()); }
    
    /** A knob with the given snapper but without values */
    public VKnob(VKnobSnapper snapper) { this(null, snapper); }

    /** A knob with the given model and snapper. */
    public VKnob(VKnobModel model, VKnobSnapper snapper) {
        setSnapper(snapper);
        setModel(model);

        //model = createDefaultVKnobModel();
        //setVal(0.0F); // this does some init

        focusColor = DEFAULT_FOCUS_COLOR;
        
        setPreferredSize(PREF_SIZE);
        hitArc.setAngleStart(235); // Degrees ??? Radians???
        addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    dragpos = me.getX() + me.getY();
                    startVal = val;

                    // Fix last angle
                    int xpos = middle - me.getX();
                    int ypos = middle - me.getY();
                    lastAng = Math.atan2(xpos, ypos);

                    requestFocus();
                }
                
                public void mouseClicked(MouseEvent me) {
                    hitArc.setAngleExtent(-(LENGTH + 20));
                    if  (hitArc.contains(me.getX(), me.getY())) {           
                        hitArc.setAngleExtent(MULTIP * (ang-START_ANG)-10);
                        if  (hitArc.contains(me.getX(), me.getY())) {
                            decValue();
                        } else incValue();
                    }
                }
            });

        // Let the user control the knob with the mouse
        addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent me) {
                    if ( dragType == SIMPLE) {
                        float f = DRAG_SPEED * (float)
                            ((me.getX() + me.getY()) - dragpos);
                        setVal(startVal + f);
                    } else if ( dragType == ROUND) {
                        // Measure relative the middle of the button! 
                        int xpos = middle - me.getX();
                        int ypos = middle - me.getY();
                        double ang = Math.atan2(xpos, ypos);
                        double diff = lastAng - ang;
                        float newVal = (float) (val + (diff / LENGTH_ANG));
                        
                        // condition to avoid strange flip from max to min
                        if (! (val-newVal > 0.5 || newVal-val > 0.5))
                            setVal(newVal);

                        lastAng = ang;
                    }
                }
                
                public void mouseMoved(MouseEvent me) {
                }
            });

        // Let the user control the knob with the keyboard
        addKeyListener(new KeyListener() {
                
                public void keyTyped(KeyEvent e) {}
                public void keyReleased(KeyEvent e) {} 
                public void keyPressed(KeyEvent e) { 
                    int k = e.getKeyCode();
                    if (k == e.VK_RIGHT || k == e.VK_UP)
                        incValue();
                    else if (k == e.VK_LEFT || k == e.VK_DOWN)
                        decValue();
                }                
            });
        
        // Handle focus so that the knob gets the correct focus highlighting.
        addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    repaint();
                }
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
    }

    public static VKnobModel createDefaultModel() {
        return new VKnobModel() {
            public Object getValueAtKnobDirection(float x) {
                return new Float( (float)(Math.floor(x * 1000) / 100) );
            }
            public float getKnobDirectionOfValue(Object v) {
                return ((Float)v).floatValue() / 10;
            }
        };
    }

    public void setDragType(int type) {
        dragType = type;
    }
    public int getDragType() {
        return dragType;
    }
    
    //public boolean isFocusTraversable() {
    //    return true;
    //}
    
    protected void incValue() {
        setVal(val + CLICK_SPEED);
    }
    
    protected void decValue() {
        setVal(val - CLICK_SPEED);
    }
    
    /** Sets the internal value to between 0.0 and 1.0 */
    protected void setVal(float val) {
        if (val < 0) val = 0;
        if (val > 1) val = 1;
        this.val = val;
        float knobDir = snapper.getKnobDirection(val);
        if (ang != (ang = START_ANG - (float) LENGTH_ANG * knobDir)) {
            repaint();
            if (model != null) {
                Object newValue = model.getValueAtKnobDirection(knobDir);
                if (!newValue.equals(value)) {
                    value = newValue;
                    fireChangeEvent();
                }
            }
        }
    }

    /** Returns the value of the knob */
    public Object getValue() {
        return value;
    }

    /** Sets the value of the knob and adjusts the knob direction */
    public void setValue(Object value) {
        if ( !value.equals(this.value) ) {
            this.value = value;
            val = snapper.getDragValue(
                    model.getKnobDirectionOfValue(value));
            if      (val < 0) val = 0;
            else if (val > 1) val = 1;
            ang = START_ANG - (float) LENGTH_ANG * val;
            repaint();
        }
    }

    /**
     * Sets the model used to get the values out of the knob position.
     *
     * This does not repaint the knob. You may want to call setValue
     * after changing the model.
     * @see #setValue(Object)
     */
    public void setModel(VKnobModel model) {
        this.model = model;
    }
    
    public void setSnapper(VKnobSnapper snapper) {
        this.snapper = snapper;
    }

    public void addChangeListener(ChangeListener cl) {
        listenerList.add(ChangeListener.class, cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        listenerList.remove(ChangeListener.class, cl);                
    }

    public Dimension getMinimumSize() {
        return MIN_SIZE;
    }

    protected void fireChangeEvent() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }

    // Paint the VKnob
    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height) - 22;
        middle = 10 + size/2;
        int x1, x2, y1, y2; // used for misc temp values

        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setBackground(getParent().getBackground());
            g2d.addRenderingHints(AALIAS);
            
            // For the size of the "mouse click" area
            hitArc.setFrame(4, 4, size+12, size+12);
        }
        
        // Paint the "markers"
        float[] snaps = snapper.getSnapPositions();
        for (int i=0; i<snaps.length; i++)
            {
                float a2 = START_ANG - (float) LENGTH_ANG * snaps[i];
                x1 = middle + (int)((size/2 + 3) * Math.cos(a2));
                y1 = middle - (int)((size/2 + 3) * Math.sin(a2));
                x2 = middle + (int)((size/2 - 1) * Math.cos(a2));
                y2 = middle - (int)((size/2 - 1) * Math.sin(a2));
                g.drawLine(x1, y1, x2, y2);
            }
        
        // Set the position of the Zero
        //g.drawString("0", 2, size + 10);

        // Paint focus if in focus
        if (hasFocus()) {
          g.setColor(focusColor);
          g.fillOval(10+2, 10+2, size-4, size-4);
        }
        // else  .... Color.white;

        int bw = size/10; // border width (4)

        // shadow
        g.setColor(SHADOW_COLOR);
        g.fillOval(10 + bw + SHADOWX, 10 + bw + SHADOWY, size-2*bw, size-2*bw);
        
        g.setColor(Color.black);

        // the knob itself
        g.fillOval(10 + bw, 10 + bw, size-2*bw, size-2*bw);
        
        // tip of knob
        {
          g.setColor(Color.black);
          double w = size*2/5;
          int dx = (int)(w * Math.sin(ang));
          int dy = (int)(w * Math.cos(ang));
          int[] xs = new int[] {
                     middle + (int)((size/2 + 3) * Math.cos(ang)),
                     middle + dx,
                     middle - dx },
                ys = new int[] {
                     middle - (int)((size/2 + 3) * Math.sin(ang)),
                     middle + dy,
                     middle - dy };
          g.fillPolygon(xs, ys, 3);
        }

        // beige parts
        g.setColor(KNOB_COLOR);
        // beige tip of knob
        {
          double w = size*2/5 - 2;
          int dx = (int)(w * Math.sin(ang));
          int dy = (int)(w * Math.cos(ang));
          int[] xs = new int[] {
                     middle + (int)((size/2 + 1) * Math.cos(ang)),
                     middle + dx,
                     middle - dx },
                ys = new int[] {
                     middle - (int)((size/2 + 1) * Math.sin(ang)),
                     middle + dy,
                     middle - dy };
          g.fillPolygon(xs, ys, 3);
        }
       
        // the beige part of knob
        g.fillOval(10 + bw + 1, 10 + bw + 1, size - 2*bw - 2, size - 2*bw - 2);

        int s2 = (int) Math.max(size/4, bw + 2) - 2;

        // the top of knob
        x1 = y1 = middle - s2 + 1; // left, top
        x2 = y2 = 2 * s2      - 2; // width, height
        g.setColor(SHINE_COLOR);
        g.drawOval(x1-1, y1-1, x2, y2);
        g.setColor(STRIPES_COLOR);
        g.drawOval(x1, y1, x2, y2);
        

        // knob stripes, 20
        for (float angl = ang + .1f*PI;
             angl < ang + 2*PI - 0.2f*.1f*PI;
             angl += .1f*PI) {
          double sinus   = Math.sin(angl),
                 cosinus = Math.cos(angl);
          x1 = middle + (int)((size/2 - bw - 3) * cosinus);
          y1 = middle - (int)((size/2 - bw - 3) * sinus);
          x2 = middle + (int)((size/2 - s2 - 1) * cosinus);
          y2 = middle - (int)((size/2 - s2 - 1) * sinus);

          // light spot
          int dist = (int)(1.5 * Math.abs(cosinus + sinus)); // 0 <= dist <= 3
          g.setColor(SHINE_COLOR);
          g.drawLine(x1-dist, y1-dist, x2-dist, y2-dist);
          g.setColor(STRIPES_COLOR);
          g.drawLine(x1, y1, x2, y2);
        }
        
        // dash on tip
        {
          double sinus   = Math.sin(ang),
                 cosinus = Math.cos(ang);
          g.setColor(STRIPES_COLOR);
          x1 = middle + (int)((size/2 - 1) * cosinus); // -3
          y1 = middle - (int)((size/2 - 1) * sinus);
          x2 = middle + (int)((size/2 - s2 - 1) * cosinus);
          y2 = middle - (int)((size/2 - s2 - 1) * sinus);
          g.drawLine(x1, y1, x2, y2);
        }

        // Draw the value below the center
        if (value != null) {
        	g.setFont(new Font("Default", Font.PLAIN, 9));
            g.setColor(Color.black);
            g.drawString(value.toString(), size/4 + 8, size + 18);
        }
    }
}

