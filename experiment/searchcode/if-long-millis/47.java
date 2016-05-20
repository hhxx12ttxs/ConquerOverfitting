/*
 * Created on 29.7.2004
 */
package jeliot.tracker;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jeliot.theater.Theater;
import jeliot.util.DebugUtil;

import org.syntax.jeliot_jedit.JEditTextArea;

/**
 * Most of the times the format for different tracker lines is
 * TIME:ID:NAME:X:Y:W:H but there are other possibilities as well.
 * The time is counted in milliseconds from the starting time.
 * 
 * @author Niko Myller
 */
public class Tracker {

    /**
     * Comment for <code>DATE_FORMAT</code>
     */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy.MM.dd");

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat(
            "HH.mm.ss.SSSS");

    private static final String columns = "Timestamp\tAction\tNumber Type\tX,Y\tWidth\tHeight\tAngle\tLife\tDescription";

    private static final String APPEAR_STRING = "Appear";

    private static final String DISAPPEAR_STRING = "Disappear";

    private static final String MODIFY_STRING = "Modify";

    private static final String MOUSEBUTTON_STRING = "MouseButton";

    private static final String BUTTON_STRING = "Button";

    private static final String KEYBOARD_STRING = "Keyboard";

    private static final String SCROLL_STRING = "Scroll";

    private static final String OTHER_STRING = "Other";

    public static final int APPEAR = 0;

    public static final int DISAPPEAR = 1;

    public static final int MODIFY = 2;

    public static final int MOUSEBUTTON = 3;

    public static final int BUTTON = 4;

    public static final int KEYBOARD = 5;

    public static final int SCROLL = 6;
    
    public static final int OTHER = 7;

    private static final String POLYGON_STRING = "Polygon";

    private static final String RECTANGLE_STRING = "Rectangle";

    private static final String OVAL_STRING = "Oval";

    private static final String LINE_STRING = "Line";

    public static final int POLYGON = 10;

    public static final int RECTANGLE = 11;

    public static final int OVAL = 12;

    public static final int LINE = 13;

    /**
     * The id counter
     */
    private static long idCounter = 0;

    /**
     * Comment for <code>out</code>
     */
    private static BufferedWriter out;

    /**
     * Comment for <code>theater</code>
     */
    private static Theater theater;

    /**
     * Comment for <code>track</code>
     */
    private static boolean track = false;

    /**
     * Comment for <code>nextId</code>
     */
    private static int nextId = 1;

    /**
     * @param t
     */
    public static void setTrack(boolean t) {
        track = t;
    }

    /**
     * @param t
     */
    public static void setTheater(Theater t) {
        theater = t;
    }

    /**
     * @param f
     */
    public static void openFile(File f) {
        if (out == null && track) {
            try {
                File file = null;
                do {
                    file = new File(f, "JeliotTracker"
                            + System.currentTimeMillis() + ".txt");
                } while (file.exists());
                file.createNewFile();
                out = new BufferedWriter(new FileWriter(file));
                Date time = TrackerClock.getInstance().getStartTime();
                out.write("Recording date: " + DATE_FORMAT.format(time) + "\n");
                out.write("Recording time: " + TIME_FORMAT.format(time)
                        + " (corresponds to time 0)\n\n\n");
                out.write(columns + "\n\n");

            } catch (Exception e) {
                if (DebugUtil.DEBUGGING) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 
     */
    public static void closeFile() {
        if (out != null && track) {
            try {
                out.flush();
                out.close();
                out = null;
            } catch (Exception e) {
                if (DebugUtil.DEBUGGING) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getActionDescription(int a) {
        switch (a) {
        case APPEAR:
            return APPEAR_STRING;
        case DISAPPEAR:
            return DISAPPEAR_STRING;
        case MODIFY:
            return MODIFY_STRING;
        case MOUSEBUTTON:
            return MOUSEBUTTON_STRING;
        case BUTTON:
            return BUTTON_STRING;
        case OTHER:
            return OTHER_STRING;
        case KEYBOARD:
            return KEYBOARD_STRING;
        case SCROLL:
            return SCROLL_STRING;
        default:
            return OTHER_STRING;
        }
    }

    public static String getTypeDescription(int t) {
        switch (t) {
        case POLYGON:
            return POLYGON_STRING;
        case RECTANGLE:
            return RECTANGLE_STRING;
        case OVAL:
            return OVAL_STRING;
        case LINE:
            return LINE_STRING;
        default:
            throw new RuntimeException("Illegal type specified in Tracking.");
        }
    }

    public static long getNewId() {
        return idCounter++;
    }

    public static synchronized void trackEvent(long millis, int action,
            int x, int y, int w, int h, String description) {
        if (out != null && track) {
            //Do the conversions and validation of the data
            long id = getNewId();
            String actionStr = getActionDescription(action);

            try {
                out.write("" + millis + "\t" + actionStr + "\t" + id + "\t"
                        + "\t" + x + "\t" + y + "\t" + w + "\t" + h
                        + "\t" + "\t" + "\t" + description
                        + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static synchronized void trackEvent(long millis, int action, int x, int y,
            String description) {
        if (out != null && track) {
            //Do the conversions and validation of the data
            long id = getNewId();
            String actionStr = getActionDescription(action);
            String locations = "";
            if (x >= 0 && y >= 0) {
                locations += x + "," + y;
            }
            try {
                out.write("" + millis + "\t" + actionStr + "\t" + id + "\t"
                        + "\t" + locations + "\t" + "\t" + "\t" + "\t" + "\t"
                        + description + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized long trackTheater(long millis, int action, long id, int type,
            int[] x, int[] y, int w, int h, double angle, long lifeTime,
            String description) {
        if (out != null && track && theater.isShowing()) {

            if (x.length <= 0 && x.length != y.length) {
                throw new RuntimeException(
                        "Different number of X and Y coordinates in Tracker!");
            }
            
            Point p = theater.getLocationOnScreen();
            Rectangle r = theater.getClipRect();
            
            String locations = "";
            for (int i = 0; i < x.length; i++) {
                int finalX = (p.x + x[i]);
                int finalY = (p.y + y[i]);
                if (type == RECTANGLE) {
                    
                }
                if (i == x.length - 1) {
                    locations +=  finalX + ","
                            + finalY;
                    break;
                }
                locations += finalX + "," + finalY
                        + ",";
            }

            return track(millis, action, id, type, locations, w, h, angle,
                    lifeTime, description);
        }
        return -1;
    }

    public static synchronized long trackCode(long millis, int action, long id, int type,
            int[] x, int[] y, int w, int h, double angle, long lifeTime,
            String description, JEditTextArea area) {
        if (out != null && track) {

            Point p = area.getLocationOnScreen();
            Rectangle r = area.getPainter().getClipRect();
            
            //int areaFirstY = Math.abs(area.lineToY(area.getFirstLine()));
            //int areaFirstX = Math.abs(area.getHorizontalOffset());
            //System.out.println("Code loc on screen");
            //System.out.println(p);
            //System.out.println("Code cliprect");
            //System.out.println(r);            
            //System.out.println("Code offset");
            //System.out.println("" + areaFirstX + " " + areaFirstY);
            //System.out.println("Code w and h");
            //System.out.println("" + w + " " + h);
            
            if (x.length <= 0 && x.length != y.length) {
                throw new RuntimeException(
                        "Different number of X and Y coordinates in Tracker!");
            }
            String locations = "";
            for (int i = 0; i < x.length; i++) {
                if (i == x.length - 1) {
                    locations += (p.x + x[i]) + ","
                            + (p.y + y[i]);
                    break;
                }
                locations += (p.x + x[i]) + ","
                        + (p.y + y[i]) + ",";
            }
            return track(millis, action, id, type, locations, w, h, angle,
                    lifeTime, description);
        }
        return -1;

    }

    public static synchronized long track(long millis, int action, long id, int type,
            String locations, int w, int h, double angle, long lifeTime,
            String description) {
        if (out != null && track) {

            //Do the conversions and validation of the data
            id = (id == -1) ? getNewId() : id;
            String actionStr = getActionDescription(action);
            String typeStr = getTypeDescription(type);

            try {
                out.write("" + millis + "\t" + actionStr + "\t" + id + "\t"
                        + typeStr + "\t" + locations + "\t" + w + "\t" + h
                        + "\t" + angle + "\t" + lifeTime + "\t" + description
                        + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return id;
        }
        return -1;
    }

    /**
     * @param name
     * @param x
     * @param y
     * @param w
     * @param h
     * @param millis
     */
    /*
     public static int writeToFile(String name, int x, int y, int w, int h,
     long millis, int id) {
     if (id < 0) {
     id = Tracker.nextId++;
     }
     if (out != null && track && theater != null) {
     try {
     Point p = theater.getLocationOnScreen();
     Rectangle r = theater.getClipRect();
     if (r != null) {
     out.write(millis + ":" + id + ":" + name + ":"
     + (p.x + x - r.x) + ":" + (p.y + y - r.y) + ":" + w
     + ":" + h);
     out.newLine();
     }
     } catch (Exception e) {
     if (DebugUtil.DEBUGGING) {
     e.printStackTrace();
     }
     }
     }
     return id;
     }
     */
    /**
     * @param name
     * @param x
     * @param y
     * @param w
     * @param h
     * @param millis
     */
    /*
     public static int writeIndexToFile(String name, int x, int y, int x2,
     int y2, long millis, int id) {
     if (id < 0) {
     id = Tracker.nextId++;
     }
     if (out != null && track && theater != null) {
     try {
     Point p = theater.getLocationOnScreen();
     Rectangle r = theater.getClipRect();
     if (r != null) {
     out.write(millis + ":" + id + ":" + name + ":"
     + (p.x + x - r.x) + ":" + (p.y + y - r.y) + ":"
     + (p.x + x2 - r.x) + ":" + (p.y + y2 - r.y));
     out.newLine();
     }
     } catch (Exception e) {
     if (DebugUtil.DEBUGGING) {
     e.printStackTrace();
     }
     }
     }
     return id;
     }
     */

    /**
     * @param name
     * @param x
     * @param y
     * @param w
     * @param h
     * @param millis
     */
    /*
     public static int writeToFileFromCodeView(String name, int x, int y, int w,
     int h, long millis, int id) {
     if (id < 0) {
     id = Tracker.nextId++;
     }

     if (out != null && track && codePane != null
     && codePane.getTextArea().isShowing()) {
     try {
     Point p = codePane.getTextArea().getLocationOnScreen();
     Rectangle r = codePane.getTextArea().getPainter().getClipRect();
     if (r != null) {
     p.x = p.x + 35; //35 comes from the width of the line
     // numbers showing component that is for
     // some reason not regonized correctly.
     out.write(millis
     + ":"
     + id
     + ":"
     + name
     + ":"
     + (p.x + x - r.x)
     + ":"
     + (p.y + y - r.y)
     + ":"
     + (((x + w - r.x) < (codePane.getWidth() - 35)) ? w
     : ((codePane.getWidth() - 35))) + ":" + h);
     out.newLine();
     }
     } catch (Exception e) {
     if (DebugUtil.DEBUGGING) {
     e.printStackTrace();
     }
     }
     }
     return id;
     }
     */
    /**
     * @param name
     * @param l
     * @param r
     * @param millis
     */
    /*
     public static int writeToFileFromCodeView(String name, int l, int r,
     long millis, int id) {
     if (id < 0) {
     id = Tracker.nextId++;
     }

     if (out != null && track && codePane != null
     && codePane.getTextArea().isShowing()) {
     try {
     out.write(millis + ":" + id + ":" + name + ":" + l + ":" + r);
     out.newLine();
     } catch (Exception e) {
     if (DebugUtil.DEBUGGING) {
     e.printStackTrace();
     }
     }
     }
     return id;
     }
     */
    /**
     * @param name
     * @param millis
     */
    /*
     public static int writeToFile(String name, long millis, int id) {
     if (id < 0) {
     id = Tracker.nextId++;
     }

     if (out != null && track) {
     try {
     out.write(millis + ":" + id + ":" + name);
     out.newLine();
     } catch (Exception e) {
     if (DebugUtil.DEBUGGING) {
     e.printStackTrace();
     }
     }
     }

     return id;
     }
     */

    /**
     * @param name
     * @param fileName
     * @param millis
     */
    /*
     public static int writeToFile(String name, String fileName, long millis,
     int id) {
     if (id < 0) {
     id = Tracker.nextId++;
     }

     if (out != null && track) {
     try {
     out.write(millis + ":" + id + ":" + name + ":" + fileName);
     out.newLine();
     } catch (Exception e) {
     if (DebugUtil.DEBUGGING) {
     e.printStackTrace();
     }
     }
     }
     return id;
     }
     */
}
