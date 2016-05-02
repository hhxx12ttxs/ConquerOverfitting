package gsender.src;

import com.sun.javafx.geom.Vec2f;
import com.sun.javafx.geom.Vec3f;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by 1 on 21.02.2015.
 */
public class PreRender {
    private JPanel root;
    private JPanel canvas;
    private JLabel time;
    private JLabel path;


    BufferedImage bi;

    private Vec3f pos=new Vec3f(0,0,0);
    private Vec2f movePos=new Vec2f();
    Point lastDarg;
    int rawScale;
    public PreRender() {
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                lastDarg=null;
            }

        });
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if(lastDarg!=null) {
                    movePos.x += e.getX() - lastDarg.x;
                    movePos.y += e.getY() - lastDarg.y;

                }
                lastDarg = e.getPoint();
                canvas.updateUI();
            }
        });
        canvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                rawScale+=e.getWheelRotation();

                scale=(float)Math.log(rawScale);
                if(!(scale>=1))
                    scale=1;
                drawOnCanvas(processor);
                canvas.updateUI();
            }
        });
    }
    GPreProcessor processor;
    float pathlength;
    public void drawOnCanvas(GPreProcessor proc){
        bi=new BufferedImage((int)(600*scale),(int)(600*scale),BufferedImage.TYPE_INT_RGB);
        processor=proc.copy();
       Graphics g= bi.getGraphics();
        g.setColor(Color.GREEN);

        while (proc.hasNext()){
            String line=proc.next();
            String[] split=line.split(" ");
            HashMap args=new HashMap();
            for(String s:split)
                args.put(s.charAt(0),s.substring(1));
            processGCode(g,args);
        }

        long millis=(long)(timeMins*60*1000);
        time.setText("Time to do: "+String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        ));
        path.setText("Path to do: "+pathlength+" mm");

    }

    private float pf(String s,float or){
        return s==null?or:Float.parseFloat(s);
    }

    public Vec3f getPos(HashMap<Character,String> args){
        return new Vec3f(pf(args.get('X'),pos.x),pf(args.get('Y'),pos.y),pf(args.get('Z'),pos.z));
    }

    float scale=1;

    private void line(Graphics g,float x,float y,float x1,float y1){
        x*=scale;
        y*=scale;
        x1*=scale;
        y1*=scale;
        g.drawLine((int) y, (int) x, (int) y1, (int) x1);

    }
    public float timeMins=0;
    public void countDelta(Vec3f p1,Vec3f p2){
        p1.sub(p2);
        pathlength+=p1.length();
        timeMins+=p1.length()/feedrate;
    }
    float feedrate=0;
    private void processGCode(Graphics g,HashMap<Character,String> args){
        timeMins=0;
        feedrate=pf(args.get('F'),100);
        switch (args.get('G')){
            case "00": {
                g.setColor(Color.GREEN);
                Vec3f np = getPos(args);
                line(g,pos.x,pos.y,np.x,np.y);
                countDelta(pos, np);
                pos = np;
                break;
            }
            case "01":{
                g.setColor(Color.RED);
                Vec3f np=getPos(args);
                line(g, pos.x, pos.y, np.x, np.y);
                countDelta(pos,np);
                pos=np;
                break;}
            case "02": {
                g.setColor(Color.RED);
                Vec3f end = getPos(args);
                Vec3f delta= new Vec3f(pf(args.get('I'),0),pf(args.get('J'),0),0);
                Vec3f center=new Vec3f(pos);
                center.add(delta);
                end.sub(center);
                double ang1=Math.atan2(-delta.y,-delta.x);
                double ang2=Math.atan2(end.y,end.x);
                if(ang2>=ang1)
                   ang2-=2*Math.PI;
                float rad=delta.length();
                Vec3f np=new Vec3f();
                for(double a=ang1;a>=ang2;a-=2f/rad){
                    np.x=(float)(Math.cos(a)*rad+center.x);
                    np.y=(float)(Math.sin(a)*rad+center.y);
                    line(g, pos.x, pos.y, np.x, np.y);
                    countDelta(pos,np);
                    pos.set(np);
                }
                end.add(center);
                line(g, pos.x, pos.y, end.x, end.y);
                countDelta(pos,end);
                pos.set(end);
                break;
            }
            case "03":{
                g.setColor(Color.RED);
                Vec3f end = getPos(args);
                Vec3f delta= new Vec3f(pf(args.get('I'),0),pf(args.get('J'),0),0);
                Vec3f center=new Vec3f(pos);
                center.add(delta);
                end.sub(center);
                double ang1=Math.atan2(-delta.y,-delta.x);
                double ang2=Math.atan2(end.y,end.x);
                if(ang2<=ang1)
                    ang2+=2*Math.PI;
                float rad=delta.length();
                Vec3f np=new Vec3f();
                for(double a=ang1;a<=ang2;a+=2f/rad){
                    np.x=(float)(Math.cos(a)*rad+center.x);
                    np.y=(float)(Math.sin(a)*rad+center.y);
                    line(g,(int)pos.x,(int)pos.y,(int)np.x,(int)np.y);
                    countDelta(pos,np);
                    pos.set(np);
                }
                end.add(center);
                line(g, pos.x, pos.y, end.x, end.y);
                countDelta(pos,end);
                pos.set(end);
            }
        }

    }
    public static void open(GPreProcessor proc) {
        PreRender pr=new PreRender();
        JFrame frame = new JFrame("PreRender");
        frame.setContentPane(pr.root);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();

        frame.setVisible(true);
        pr.drawOnCanvas(proc);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        canvas=new JPanel(){
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.translate((int)movePos.x,(int)movePos.y);
                g.drawImage(bi,(int)movePos.x,(int)movePos.y,null);

            }
        };

    }

    public static void main(String[] args){
        JFileChooser jfc = new JFileChooser();
        jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());
        jfc.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String fn = f.getName();
                if (fn.lastIndexOf('.') > 0) {
                    String n = fn.substring(fn.lastIndexOf('.') + 1);
                    return n.equalsIgnoreCase("gcode") || n.equalsIgnoreCase("gco");
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "G-Code";
            }
        });
        if(jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            BufferedReader br=new BufferedReader(new InputStreamReader(fis));
            PreRender.open(new GPreProcessor(br.lines()));
        }
    }
}

