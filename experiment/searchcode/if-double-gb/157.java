/*
 * DrawGraphPanel.java
 *
 * Created on 2006/11/05, 10:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package MyPanel;
/**
 *
 * @author Administrator
 */
import DrawFormat.*;
import dimension.PointD;
import dimension.Function2D;
import dimension.PolyLine;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.text.*;
public class DrawGraphPanel extends JPanel{
    static final int dim=2;
    
    static final DecimalFormat dform=new DecimalFormat("0.0E0");
    private static final NumberFormat form=NumberFormat.getInstance();
    private static final NumberFormat col=NumberFormat.getInstance();
    
    
    public static int lineN;
    public DrawGraphFormat dgf;
    
    public static int dt=15;
    public static int db=60;
    public static int dl=30;
    public static int dr=20;
    public static Dimension canvasSize=new Dimension(600,400);
    public static PointD frameSize=new PointD(canvasSize.width-dl-dr,canvasSize.height-dt-db);
    public static PointD minSize=new PointD(dl,canvasSize.height-db);
    public static PointD maxSize=new PointD(canvasSize.width-dr,dt);
    
    public TransPointD tr;
    
    public BasicStroke mainS;
    public BasicStroke subS;
    public BasicStroke frameS;
    public Color backCanvas;
    public Color backPanel;
    public Color fore;
    public Color mainC;
    public Color subC;
    public Color frameC;
    protected java.util.List<DrawLinePanel> lineList;
    public int count=0;
    public BufferedImage bufi;
    public Graphics2D gb;
    public static final AffineTransform afTrans=new AffineTransform();
    
    public DrawGraphPanel(DrawLinePanel[] dlp){
        DrawLineFormat[] dlf1=new DrawLineFormat[dlp.length];
        for(int i=0;i<dlp.length;i++)dlf1[i]=dlp[i].dlf;
        this.dgf=new DrawGraphFormat(dlf1);
        for(int i=0;i<dlp.length;i++)add(dlp[dlp.length-i-1]);
        lineList=new ArrayList<DrawLinePanel>();
        valueChanged();
    }
    public DrawGraphPanel(DrawLinePanel dlp){
        this.dgf=new DrawGraphFormat(dlp.dlf);
        add(dlp);
        valueChanged();
    }
    
    public void updata(Graphics g){
        paint(g);
    }
    
    public void paint(Graphics g){
        Graphics2D g2=(Graphics2D)g;
        g2.drawImage(bufi,new AffineTransformOp(afTrans,AffineTransformOp.TYPE_BICUBIC),0,0);
    }
    public void refresh(){
        count++;//count_UpLoaD
//        System.out.println(count);
        setBackground(backCanvas);
        setForeground(fore);
        bufi=new BufferedImage(canvasSize.width,canvasSize.height,BufferedImage.TYPE_INT_RGB);
        gb=bufi.createGraphics();
        gb.setColor(new Color(240,240,255));
        gb.fillRect(0,0,canvasSize.width,canvasSize.height);
        gb.setColor(backCanvas);
        gb.fill(new Rectangle.Float((float)minSize.p[PointD.X],(float)maxSize.p[PointD.Y],(float)frameSize.p[PointD.X],(float)frameSize.p[PointD.Y]));
        tr=new TransPointD(dgf);
        
        if(dgf.scaleType.equals("LinerLiner")){
            drawLinerLiner();
        } else if(dgf.scaleType.equals("LinerLog")){
            drawLinerLog();
        } else if(dgf.scaleType.equals("LogLiner")){
            drawLogLiner();
        } else if(dgf.scaleType.equals("LogLog")){
            drawLogLog();
        }
        drawLine();
        //     drawNumber();
        drawFrame();
        
    }
    private void drawLinerLiner(){
        drawGridLinerX();
        drawGridLinerY();
    }
    private void drawLinerLog(){
        drawGridLinerX();
        drawGridLogY();
    }
    private void drawLogLiner(){
        drawGridLogX();
        drawGridLinerY();
    }
    private void drawLogLog(){
        drawGridLogX();
        drawGridLogY();
    }
    private void drawGridLinerX(){
        gb.setStroke(mainS);
        gb.setColor(mainC);
        double dx=dgf.delMain.p[PointD.X]*tr.nrm.p[PointD.X];
        for(int j=0;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.X]<=j*dx && j*dx<=tr.max.p[PointD.X])gb.draw(tr.transLine(j*dx,tr.min.p[PointD.Y],j*dx,tr.max.p[PointD.Y]));
            if(tr.min.p[PointD.X]<=-j*dx && -j*dx<=tr.max.p[PointD.X])gb.draw(tr.transLine(-j*dx,tr.min.p[PointD.Y],-j*dx,tr.max.p[PointD.Y]));
        }
        gb.setStroke(subS);
        gb.setColor(subC);
        dx=dgf.delSub.p[PointD.X]*tr.nrm.p[PointD.X];
        for(int j=0;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.X]<=j*dx && j*dx<=tr.max.p[PointD.X])gb.draw(tr.transLine(j*dx,tr.min.p[PointD.Y],j*dx,tr.max.p[PointD.Y]));
            if(tr.min.p[PointD.X]<=-j*dx && -j*dx<=tr.max.p[PointD.X])gb.draw(tr.transLine(-j*dx,tr.min.p[PointD.Y],-j*dx,tr.max.p[PointD.Y]));
        }
    }
    private void drawGridLinerY(){
        gb.setStroke(mainS);
        gb.setColor(mainC);
        double dy=dgf.delMain.p[PointD.Y]*tr.nrm.p[PointD.Y];
        for(int j=0;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.Y]<=j*dy && j*dy<=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],j*dy,tr.max.p[PointD.X],j*dy));
            if(tr.min.p[PointD.Y]<=-j*dy && -j*dy<=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],-j*dy,tr.max.p[PointD.X],-j*dy));
        }
        gb.setStroke(subS);
        gb.setColor(subC);
        dy=dgf.delSub.p[PointD.Y]*tr.nrm.p[PointD.Y];
        for(int j=0;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.Y]<=j*dy && j*dy<=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],j*dy,tr.max.p[PointD.X],j*dy));
            if(tr.min.p[PointD.Y]<=-j*dy && -j*dy<=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],-j*dy,tr.max.p[PointD.X],-j*dy));
        }
    }
    private void drawGridLogX(){
        gb.setStroke(mainS);
        gb.setColor(mainC);
        double dx=Math.log10(dgf.delMain.p[PointD.X])*tr.nrm.p[PointD.X];
        for(int j=0;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.X]<=j*dx && j*dx<=tr.max.p[PointD.X])gb.draw(tr.transLine(j*dx,tr.min.p[PointD.Y],j*dx,tr.max.p[PointD.Y]));
            if(tr.min.p[PointD.X]<=-j*dx && -j*dx<=tr.max.p[PointD.X])gb.draw(tr.transLine(-j*dx,tr.min.p[PointD.Y],-j*dx,tr.max.p[PointD.Y]));
        }
        gb.setStroke(subS);
        gb.setColor(subC);
        double dxP=0;
        double dxM=0;
        for(int j=1;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.X]<= dxP && dxP <=tr.max.p[PointD.X])gb.draw(tr.transLine(dxP,tr.min.p[PointD.Y],dxP,tr.max.p[PointD.Y]));
            if(tr.min.p[PointD.X]<= dxM && dxM <=tr.max.p[PointD.X])gb.draw(tr.transLine(dxM,tr.min.p[PointD.Y],dxM,tr.max.p[PointD.Y]));
            dxP+=Math.log10((double)((j-1)%9+2)/(double)((j-1)%9+1))*tr.nrm.p[PointD.X];
            dxM-=Math.log10((double)((9-j)%9+2)/(double)((9-j)%9+1))*tr.nrm.p[PointD.X];
        }
    }
    private void drawGridLogY(){
        double dy=dgf.delMain.p[PointD.Y]*tr.nrm.p[PointD.Y];
        for(int j=0;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.Y]<=j*dy && j*dy<=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],j*dy,tr.max.p[PointD.X],j*dy));
            if(tr.min.p[PointD.Y]<=-j*dy && -j*dy<=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],-j*dy,tr.max.p[PointD.X],-j*dy));
        }
        gb.setStroke(subS);
        gb.setColor(subC);
        double dyP=0;
        double dyM=0;
        for(int j=1;j<DrawGraphFormat.gridMax;j++){
            if(tr.min.p[PointD.Y]<= dyP && dyP <=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],dyP,tr.max.p[PointD.X],dyP));
            if(tr.min.p[PointD.Y]<= dyM && dyM <=tr.max.p[PointD.Y])gb.draw(tr.transLine(tr.min.p[PointD.X],dyM,tr.max.p[PointD.X],dyM));
            dyP+=Math.log10((double)((j-1)%9+2)/(double)((j-1)%9+1))*tr.nrm.p[PointD.X];
            dyM-=Math.log10((double)((9-j)%9+2)/(double)((9-j)%9+1))*tr.nrm.p[PointD.X];
        }
    }
    
    private void drawLine(){
        Component[] dlp=(Component[])getComponents();
        for(int i=0;i<dlp.length;i++){
            DrawLinePanel a=(DrawLinePanel)dlp[i];
            a.drawNewLine(gb,dgf);
        }
    }
    
    private void drawNumber(){
    }
    private void drawNumberLiner(){
        for(int j=0;j<=dgf.nMain[PointD.X];j++){
        }
        for(int j=0;j<=dgf.nMain[PointD.Y];j++){
        }
    }
    private void drawNumberLog(){
        for(int j=0;j<dgf.nMain[PointD.X];j++){
        }
        for(int j=0;j<dgf.nMain[PointD.Y];j++){
        }
    }
    
    private void drawFrame(){
        gb.setStroke(frameS);
        gb.setColor(frameC);
        gb.draw(new Rectangle.Float((float)minSize.p[PointD.X],(float)maxSize.p[PointD.Y],(float)frameSize.p[PointD.X],(float)frameSize.p[PointD.Y]));
    }
    
    
    public void valueChanged(){
        mainS = new BasicStroke((float)DrawGraphFormat.mainWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
        subS = new BasicStroke((float)DrawGraphFormat.subWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
        frameS = new BasicStroke((float)DrawGraphFormat.frameWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
        backCanvas=Color.white;
        backPanel=Color.CYAN;
        fore=Color.BLACK;
        mainC=DrawGraphFormat.mainColor;
        subC=DrawGraphFormat.subColor;
        frameC=DrawGraphFormat.frameColor;
        refresh();
    }
    static{
        col.setGroupingUsed(false);
        form.setGroupingUsed(false);
        col.setMaximumFractionDigits(2);
        form.setMinimumFractionDigits(0);
        form.setMaximumFractionDigits(2);
        form.setMinimumIntegerDigits(1);
        form.setMaximumIntegerDigits(5);
    }
    
    /** Creates a new instance of DrawGraphPanel */
    public String toString(){
        return dgf.graphTitle;
    }
}

