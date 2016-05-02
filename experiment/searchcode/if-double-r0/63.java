/*********************************************************************************
 *
 *        SolarDrawImpl.java
 *
 *    Window for solar system drawing
 *
 *********************************************************************************
 *    Copyright 2003 Brown University -- Steven P. Reiss
 *********************************************************************************
 *  Copyright 2003, Brown University, Providence, RI.
 *
 *              All Rights Reserved
 *
 *  Permission to use, copy, modify, and distribute this software and its
 *  documentation for any purpose other than its incorporation into a
 *  commercial product is hereby granted without fee, provided that the
 *  above copyright notice appear in all copies and that both that
 *  copyright notice and this permission notice appear in supporting
 *  documentation, and that the name of Brown University not be used in
 *  advertising or publicity pertaining to distribution of the software
 *  without specific, written prior permission.
 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
 *  OF THIS SOFTWARE.
 *
 ********************************************************************************/
/* RCS: $Header$ */
/*********************************************************************************
 *
 * $Log$
 *
 ********************************************************************************/
package solardraw;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;
import java.awt.*;
import java.util.*;
class SolarDrawImpl extends JFrame implements SolarDraw, GLEventListener {
    /********************************************************************************/
    /*                                        */
    /* Private Storage */
    /*                                        */
    /********************************************************************************/
    private SolarDraw.Control client_control;
    private GLCanvas draw_area;
    private JLabel message_area;
    private DrawThread gl_thread;
    private int window_width;
    private int window_height;
    private boolean is_inited;
    private Collection<SolarDraw.Body> draw_objects;
    private double max_coord;
    private double max_mass;
    private double min_mass;
    private GLUquadric draw_quadric;
    private GLU glu_object;
    private SolarDrawCamera view_camera;
    private SolarDrawVector view_center;
    private SolarDrawVector new_center;
    private double rad_factor;
    private double max_radius;
    private double min_pixels;
    private double ups;
    private int frameCounter = 0;
    private UpdateCounter _updateCounter;
    private static final double MIN_PIXELS = 2;
    private static final int FRAMES_PER_SECOND = 60;
    private static final long serialVersionUID = 1L;
    /********************************************************************************/
    /*                                        */
    /* Constructors */
    /*                                        */
    /********************************************************************************/
    SolarDrawImpl(SolarDraw.Control ctl) {
        super("CS032 Solar System");
        client_control = ctl;
        gl_thread = null;
        draw_area = null;
        message_area = null;
        window_width = 0;
        window_height = 0;
        is_inited = false;
        draw_objects = new LinkedList<SolarDraw.Body>();
        max_coord = 0;
        max_mass = 0;
        min_mass = 0;
        max_radius = 0;
        glu_object = new GLU();
        draw_quadric = glu_object.gluNewQuadric();
        rad_factor = 0;
        min_pixels = MIN_PIXELS;
        setupWindow();
        view_camera = new SolarDrawCamera();
        new SolarDrawSpin(draw_area, view_camera, client_control);
        view_center = null;
        new_center = new SolarDrawVector();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        _updateCounter = new UpdateCounter();
    }
    /********************************************************************************/
    /*                                        */
    /* Action methods */
    /*                                        */
    /********************************************************************************/
    /**
     * Register an object to draw with the GUI.  Call this once for
     * each object that you want to be drawn.
     *
     * @param body - the body to register.
     */
    public void registerObject(SolarDraw.Body body) {
        synchronized (draw_objects) {
            draw_objects.add(body);
        }
        if (body.getX() > max_coord) {
            max_coord = body.getX();
        }
        if (body.getY() > max_coord) {
            max_coord = body.getY();
        }
        if (body.getMass() > max_mass) {
            max_mass = body.getMass();
        }
        if (min_mass == 0 || body.getMass() < min_mass) {
            min_mass = body.getMass();
        }
        if (body.getRadius() > max_radius) {
            max_radius = body.getRadius();
        }
    }
    /**
     * Unregister the object from the GUI to stop it from being drawn.
     *
     * @param body - the body to unregister.
     */
    public void unregisterObject(SolarDraw.Body body) {
        synchronized (draw_objects) {
            draw_objects.remove(body);
        }
    }
    public String getCombinedBodyName(String collider_name_1, String collider_name_2) {
        return collider_name_1.compareTo(collider_name_2) > 0 ? collider_name_1 : collider_name_2;
    }
    public void begin() {
        max_coord += 10 * max_radius;
        setVisible(true);
        gl_thread = new DrawThread();
        if (is_inited) {
            gl_thread.start();
        }
    }
    /********************************************************************************/
    /*                                        */
    /* Window setup methods */
    /*                                        */
    /********************************************************************************/
    private void setupWindow() {
        Dimension d = new Dimension(500, 500);
        JPanel pnl = new JPanel(new GridBagLayout());
        GLCapabilities caps = new GLCapabilities();
        draw_area = new GLCanvas(caps);
        draw_area.setSize(d);
        draw_area.addGLEventListener(this);
        addElement(pnl, draw_area, 0, 10);
        addElement(pnl, new JSeparator(), 1, 0);
        message_area = new JLabel("Hello", JLabel.CENTER);
        Font ft = message_area.getFont();
        ft = ft.deriveFont((float) 20.0);
        message_area.setFont(ft);
        addElement(pnl, message_area, 2, 0);
        Dimension sz = message_area.getSize();
        sz.width = 0;
        message_area.setMinimumSize(sz);
        setContentPane(pnl);
        pack();
    }
    private void addElement(JPanel pnl, Component c, int ypos, int ywt) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = ypos;
        gbc.gridwidth = 0;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = ywt;
        gbc.fill = GridBagConstraints.BOTH;
        pnl.add(c, gbc);
    }
    /********************************************************************************/
    /*                                        */
    /* Top level drawing methods */
    /*                                        */
    /********************************************************************************/
    public void display(GLAutoDrawable d) {
        GL gl = d.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        view_camera.draw(gl, glu_object);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        synchronized (draw_objects) {
            int nobj = 0;
            double tmass = 0;
            new_center.clear();
            for (Iterator<SolarDraw.Body> it = draw_objects.iterator(); it.hasNext();) {
                SolarDraw.Body sod = it.next();
                double m = sod.getMass();
                if (m <= 0d) {
                    System.err.println("Warning: object with name " + sod.getName() + " has mass " + m + ", and must have mass > 0 to be drawn!");
                    continue;
                }
                double x0 = sod.getX();
                double y0 = sod.getY();
                double z0 = sod.getZ();
                if (view_center != null && draw_objects.size() > 1000) {
                    double dsq = view_center.distance2(x0, y0, z0);
                    if (dsq > 10000 * max_coord * max_coord) {
                        it.remove();
                        continue;
                    }
                }
                double mfact = m * m;
                new_center.add(x0 * mfact, y0 * mfact, z0 * mfact);
                tmass += mfact;
                ++nobj;
            }
            if (nobj != 0) {
                new_center.scaleBy(1.0 / tmass);
            }
            if (view_center == null) {
                view_center = new SolarDrawVector(new_center);
            } else if (nobj > 1) {
                double diff = new_center.distance2(view_center);
                if (diff > max_coord * max_coord / 2) {
                    view_center.copyFrom(new_center);
                } else {
                    view_center.copyFrom(new_center);
                }
            } else {
                Iterator<SolarDraw.Body> it = draw_objects.iterator();

                if(it.hasNext())
                {
                    SolarDraw.Body last_body = it.next();
                    view_center.set(last_body.getX(), last_body.getY(), last_body.getZ());
                }
            }
            gl.glScaled(1 / max_coord, 1 / max_coord, 1 / max_coord);
            for (SolarDraw.Body sod : draw_objects) {
                gl.glPushAttrib(GL.GL_CURRENT_BIT | GL.GL_TRANSFORM_BIT);
                gl.glPushMatrix();
                drawObject(gl, sod);
                gl.glPopMatrix();
                gl.glPopAttrib();
            }
        }
        gl.glFlush();
        if (client_control != null) {
            double t = client_control.getTime();
            int yr = (int) (t / (60 * 60 * 24 * 365.25));
            t -= yr * 60 * 60 * 24 * 365.25;
            int day = (int) (t / (60 * 60 * 24));
            t -= day * 60 * 60 * 24;
            int hr = (int) (t / (60 * 60));
            t -= hr * 60 * 60;
            int mn = (int) (t / 60);
            t -= mn * 60;
            int sc = (int) (t);
            t -= sc;
            int ms = (int) (t * 1000);
            String s = String.format(
                    "%1$6d.%2$03d.%3$02d:%4$02d:%5$02d.%6$03d", yr, day, hr,
                    mn, sc, ms);
            s += String.format(" : %4.02f UPS", ups);
            if (frameCounter++ % 30 == 0) {
                ups = _updateCounter.getUPS();
                frameCounter = 1;
            }
            message_area.setText(s);
        }
    }
    public void displayChanged(GLAutoDrawable d, boolean _mode, boolean _dev) {
    }
    public void init(GLAutoDrawable d) {
        GL gl = d.getGL();
        gl.glClearColor(0f, 0f, 0f, 1f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        is_inited = true;
        if (gl_thread != null) {
            gl_thread.start();
        }
    }
    public void reshape(GLAutoDrawable d, int _x, int _y, int wd, int ht) {
        window_width = wd;
        window_height = ht;
        view_camera.setup(wd, ht);
        d.getGL().glViewport(0, 0, window_width, window_height);
    }
    private void drawObject(GL gl, SolarDraw.Body sod) {
        gl.glTranslated(sod.getX() - view_center.getX(), sod.getY()
                - view_center.getY(), sod.getZ() - view_center.getZ());
        float v0 = (float) sod.getMass();
        if (v0 == 0) {
            return;
        }
        if (v0 > max_mass) {
            max_mass = v0;
        }
        double x0 = (sod.getMass() - min_mass + 1) / (max_mass - min_mass + 1);
        v0 = (float) (Math.log(x0 + 1) / Math.log(2));
        // v0 = (float)(Math.log(v0-min_mass+1)/Math.log(max_mass-min_mass+1));
        // v0 =
        // (float)Math.sqrt((sod.getMass()-min_mass+1)/(max_mass-min_mass+1));
        // v0 = (float)(sod.getMass() / max_mass);
        /*
        if (v0 > 1) {
        max_mass = sod.getMass();
        v0 = 1;
        }
        v0 *= 0.8f;
        int c0 = Color.HSBtoRGB(v0, 1f, 1f);
        int r = (c0 >> 16) & 0xff;
        int g = (c0 >> 8) & 0xff;
        int b = (c0 & 0xff);
         *
         * */
        double rd = 1.0;
        double gd = (sod.getMass() / max_mass);
        double bd = 0.25;
        if (sod.getShape() == Shape.Cone) {
            rd = 0.0;
            gd = 1.0;
            bd = 0.0;
        }
        gl.glColor3d(rd, gd, bd);
        if (rad_factor == 0) {
            double r0 = sod.getRadius();
            double mr = 4.0 * max_coord / window_width;
            if (r0 >= mr) {
                rad_factor = 1;
            } else {
                rad_factor = mr / r0;
            }
        }
        // double rad = sod.getRadius() * 10;
        double rad = sod.getRadius() * Math.log(sod.getRadius());
        // double rad = sod.getRadius() * rad_factor;
        double minrad = min_pixels * max_coord / window_width;
        if (rad < minrad) {
            rad = minrad;
        }
        switch (sod.getShape()) {
            case Cone:
                gl.glRotated(90, sod.getX(), sod.getY(), sod.getZ());
                glu_object.gluCylinder(draw_quadric, rad, 1, rad * 5d, 10, 10);    // double base, double top, double height, int slices, int stacks)
                break;
            case Sphere:
                glu_object.gluSphere(draw_quadric, rad, 8, 8);
                break;
            default:
                glu_object.gluSphere(draw_quadric, rad, 8, 8);
                break;
        }
        // Draw a point in the center so we'll always see at least one pixel.
        // Before this, if you scrolled out too far the above drawing code wouldn't draw anything.
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex3f(0, 0, 0);
        gl.glEnd();
    }
    /**
     * This will update the UPS on the GUI.  Call this as you complete
     * each iteration of your simulation.
     */
    public void tick() {
        _updateCounter.update();
    }
    /**
     * Returns the number of ticks since the beginning of the simulation.
     *
     * @return: the number of ticks.
     */
    public long getTicks() {
        return _updateCounter.getTicks();
    }
    /********************************************************************************/
    /*                                        */
    /* Drawing thread for GL */
    /*                                        */
    /********************************************************************************/
    private class DrawThread extends Thread {
        private long last_draw;
        DrawThread() {
            super("Solar System Drawing Thread");
            last_draw = 0;
            setDaemon(true);
        }
        public void run() {
            long frametime = 1000 / FRAMES_PER_SECOND;
            long delay = 0;
            for (;;) {
                long t = System.currentTimeMillis();
                if (last_draw != 0) {
                    long dt = t - last_draw;
                    delay += frametime - dt;
                    if (delay > 50) {
                        try {
                            Thread.sleep(delay - 11, 500000);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                last_draw = t;
                draw_area.display();
            }
        }
    } // end of subclass DrawThread
} // end of class SolarDrawImpl
/* end of SolarDrawImpl.java */

