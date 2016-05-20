import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.lang.Integer;
import java.util.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
class GameCanvas extends Canvas implements Runnable
{
    double angle=60;
    double rads =57.29577866f;
    double velocity=3;
    double deltaTime = .098;
    Land ls;
    JComboBox weap;
    private Image dbImage;
    private Graphics dbg;
    double xV;
    double yV;
    double gravity = .098;
    Thread thr;
    JSlider Angle;
    JSlider Speed;
    boolean impact = true;
    boolean gameDone = false;
    boolean hitwall = false;
    public boolean efexcomp=true;
    double[] xShell=new double[2];
    double[] yShell=new double[2];
    Tank p1;
    Tank p2;
    int player=1;
    Projectile proj0=null;
    Projectile proj1=null;
    int radius;
    int[][] previous = new int[2][2];
    Barrel bar = new Barrel();
    Barrel bar1 = new Barrel();
    public GameCanvas(boolean a)
    {
        super();
        angle /= rads;
        ls = new Land(this);
        xShell[0]=(Math.sin(angle)*20)+40;
        yShell[0]=(Math.tan(angle)*20)+Land.map[40];
    }
    double th;
    int prevpos;
    public void hitOtherPlayer()
    {
        for(int a=0;a<180;a++)
        {
            for(int b=0;b<100;b++)
            {
                xV = (b/10)*Math.cos(Math.toRadians(a));
                yV = (b/10)*Math.sin(Math.toRadians(a));
                xShell[0] = (Math.cos(Math.toRadians(a))*20)+52;
                yShell[0] = ls.map[52]-(Math.sin(Math.toRadians(a))*20);
                impact=false;
                while(!impact)
                {
                    xShell[1] += xV;
                    yShell[1] -= yV;
                    yV -= gravity/2;
                    impact = AiImpact(xShell[1], yShell[1]);
                }
                if(xShell[0]>=495&&xShell[0]<=505)
                {
                    Angle.setValue(a);
                    Speed.setValue(b);
                    a=361;
                    b=101;
                }
            }
        }
    }
    boolean done=true;
    boolean fire=false;
    public void Fire(int tankx,int tanky)
    {
        xV = (Speed.getValue()/10)*Math.cos(Math.toRadians(Angle.getValue()));
        yV = (Speed.getValue()/10)*Math.sin(Math.toRadians(Angle.getValue()));
        xShell[0] = (Math.cos(Math.toRadians(Angle.getValue()))*20)+tankx+20;
        yShell[0] = (tanky+20)-(Math.sin(Math.toRadians(Angle.getValue()))*20);
        impact=false;
        if(player==1)
            p1.recoildone=false;
        if(player==2)
            p2.recoildone=false;
        while(!impact)
        {
            xShell[0] += xV;
            yShell[0] -= yV;
            yV -= gravity/2;
            impact = testForImpact(xShell[0], yShell[0]);
            repaint();
            try 
            {
                thr.sleep(20);
            }
            catch (InterruptedException e){}
        }
    }
    public void destroyLand()
    {
        efexcomp=false;
        repaint();
        ls.Efexcomp=true;
        int height;
        efe=false;
        int w=0;
        while(!efexcomp&&hitwall==false)
        {
            for(int i=(int)xShell[0]-radius;i<=(int)xShell[0]+radius;i++)
            {
                if(i<xShell[0])
                {
                    if((int)Math.pow(radius,2)>(int)Math.pow(xShell[0]-i,2))
                        th = Math.sqrt((int)Math.pow(radius,2)-(int)Math.pow(xShell[0]-i,2));
                    if((int)Math.pow(radius,2)<Math.pow(xShell[0]-i,2))
                        th = Math.sqrt((int)Math.pow(xShell[0]-i,2)-(int)Math.pow(radius,2));
                }
                if(i>xShell[0])
                {
                    if((int)Math.pow(radius,2)>Math.pow(i-xShell[0],2))
                        th = Math.sqrt((int)Math.pow(radius,2)-(int)Math.pow(i-xShell[0],2));
                    if((int)Math.pow(radius,2)<Math.pow(i-xShell[0],2))
                        th = Math.sqrt((int)Math.pow(i-xShell[0],2)-Math.pow(radius,2));
                }
                if(i==xShell[0])
                    th=radius;
                if(ls.map[i]<yShell[0]+th&&ls.map[i]>yShell[0]-th)
                {
                    if((int)(yShell[0]+th)>400)
                        ls.map[i]=400;
                else
                    ls.map[i]=(int)(yShell[0]+th);
                }
                if(ls.map[i]<yShell[0]-th)
                {
                    ls.left[w][2]=i;
                    ls.left[w][1]=(int)(yShell[0]-th);
                    ls.left[w][0]=ls.map[i];
                    if((int)(yShell[0]+th)>400)
                        ls.map[i]=400;
                    else
                        ls.map[i]=(int)(yShell[0]+th);
                    //ls.map[i]=(int)(ls.map[i]+(th*2));
                    w++;
                }
            }
            if(secondweap)
            {
                for(int i=(int)xShell[1]-radius;i<=(int)xShell[1]+radius;i++)
                {
                    if(i<xShell[1])
                    {
                        if((int)Math.pow(radius,2)>(int)Math.pow(xShell[1]-i,2))
                            th = Math.sqrt((int)Math.pow(radius,2)-(int)Math.pow(xShell[1]-i,2));
                        if((int)Math.pow(radius,2)<Math.pow(xShell[1]-i,2))
                            th = Math.sqrt((int)Math.pow(xShell[1]-i,2)-(int)Math.pow(radius,2));
                    }
                    if(i>xShell[1])
                    {
                        if((int)Math.pow(radius,2)>Math.pow(i-xShell[1],2))
                            th = Math.sqrt((int)Math.pow(radius,2)-(int)Math.pow(i-xShell[1],2));
                        if((int)Math.pow(radius,2)<Math.pow(i-xShell[1],2))
                            th = Math.sqrt((int)Math.pow(i-xShell[1],2)-Math.pow(radius,2));
                    }
                    if(i==xShell[1])
                        th=radius;
                    if(ls.map[i]<yShell[1]+th&&ls.map[i]>yShell[1]-th)
                    {
                        if((int)(yShell[1]+th)>400)
                            ls.map[i]=400;
                    else
                        ls.map[i]=(int)(yShell[1]+th);
                    }
                    if(ls.map[i]<yShell[1]-th)
                    {
                        ls.left[w][2]=i;
                        ls.left[w][1]=(int)(yShell[1]-th);
                        ls.left[w][0]=ls.map[i];
                        if((int)(yShell[1]+th)>400)
                            ls.map[i]=400;
                        else
                            ls.map[i]=(int)(yShell[1]+th);
                        //ls.map[i]=(int)(ls.map[i]+(th*2));
                        w++;
                    }
                }
            }
            efexcomp=true;
            efe=true;
            repaint();
            ls.indexs=w;
        }
        if(w>0)
        {
            if(ls.th==null)
                ls.dooo();
            else
                ls.done=false;
        }
        efexcomp=true;
        hitwall=false;
        p1.conformtoLand();
        p2.conformtoLand();
    }
    boolean secondweap=false;
    public void run()
    {
        p1= new Tank(Tank.PLAYER_ONE,ls,this,bar);
        p1.conformToLand();
        p2= new Tank(Tank.PLAYER_TWO,ls,this,bar1);
        p2.conformToLand();
        Frame f = new Frame();
        f.setLayout(new GridLayout(5,2));
        f.add(new Label("Angle"));
        Angle = new JSlider(0,360,45);
        Angle.setMajorTickSpacing( 40 );
        Angle.setMinorTickSpacing( 10 );
        Angle.setPaintTicks(true);
        Angle.setPaintLabels(true);
        Angle.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                if(player==1)
                {
                    p1.angle=(360-Angle.getValue());
                }
                if(player==2)
                {
                    p2.angle=(360-Angle.getValue());
                }
            }
        });
        String[] weapons = {"Single","Single Huge", "Double","Double Huge"};
        weap = new JComboBox(weapons);
        f.add(Angle);
        f.add(new Label("Power"));
        Speed = new JSlider(0,100,20);
        Speed.setMajorTickSpacing(40);
        Speed.setMinorTickSpacing(10);
        Speed.setPaintTicks(true);
        Speed.setPaintLabels(true);
        f.add(Speed);
        Button shoot = new Button("Shoot");
        shoot.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent r)
            {
                fire=true;
                if(player==1)
                {
                    previous[0][0]=Angle.getValue();
                    previous[0][1]=Speed.getValue();
                }
                if(player==2)
                {
                    previous[1][0]=Angle.getValue();
                    previous[1][1]=Speed.getValue();
                }
            }
        });
        f.add(shoot);
        f.add(weap);
        f.setSize(600,300);
        f.setVisible(true);
        while(!gameDone)
        {
            if(fire==true)
            {
                if(player==1)
                {
                    if(bar.th==null)
                        bar.th=new Thread(bar);
                    bar.th.start();
                    bar.gameon=true;
                    while(bar.gameon)
                    {
                        repaint();
                    }
                    if(weap.getSelectedIndex()==0||weap.getSelectedIndex()==2)
                        radius=30;
                    if(weap.getSelectedIndex()==1||weap.getSelectedIndex()==3)
                        radius=60;
                    if(weap.getSelectedIndex()==2||weap.getSelectedIndex()==3)
                    {
                        proj1=new Projectile(ls,p1,Speed.getValue(),Angle.getValue()+(new Random().nextInt(20)-10),this,2);
                        proj1.proj();
                        proj1.thr.start();
                        secondweap=true;
                    }
                    p1.recoildone=false;
                    proj0=new Projectile(ls,p1,Speed.getValue(),Angle.getValue(),this,1);
                    proj0.proj();
                    proj0.thr.start();
                    while(!proj0.impact&&((secondweap)?!proj1.impact:!proj0.impact))
                    {
                        try
                        {
                            thr.sleep(10);
                        }
                        catch(Exception e){}
                    }
                    xShell[0]=proj0.coord[0];
                    yShell[0]=proj0.coord[1];
                    if(secondweap)
                    {
                        xShell[1]=proj1.coord[0];
                        yShell[1]=proj1.coord[1];
                    }
                    destroyLand();
                    player=3;
                    Angle.setValue(previous[1][0]);
                    Speed.setValue(previous[1][1]);
                    player=1;
                }
                if(player==2)
                {
                    if(bar1.th==null)
                        bar1.th=new Thread(bar1);
                    bar1.th.start();
                    bar1.gameon=true;
                    while(bar1.gameon)
                    {
                        repaint();
                    }
                    p2.recoildone=false;
                    proj0=new Projectile(ls,p2,Speed.getValue(),Angle.getValue(),this,1);
                    proj0.proj();
                    proj0.thr.start();
                    while(!proj0.impact)
                    {
                        try
                        {
                            thr.sleep(10);
                        }
                        catch(Exception e){}
                    }
                    xShell[0]=proj0.coord[0];
                    yShell[0]=proj0.coord[1];
                    destroyLand();
                    player=3;
                    Angle.setValue(previous[0][0]);
                    Speed.setValue(previous[0][1]);
                    player=2;
                }
                player=((player==1)?2:1);
                fire=false;
                secondweap=false;
            }
            try 
            {
                thr.sleep(20);
            }
            catch (InterruptedException e){}
            repaint();
        }
    }
    public void startAnim()
    {
        thr = new Thread(this);
        thr.start();
    }
    public void update (Graphics g)
    {
        if (dbImage == null)
        {
            dbImage = createImage (800, 800);
            dbg = dbImage.getGraphics();
        }
        dbg.setColor (getBackground ());
        dbg.fillRect (0, 0, this.getSize().width, this.getSize().height);
        paint(dbg);
        dbg.setColor (Color.blue);
        dbg.drawImage(p1.TaNk(),p1.tankx,p1.tanky,this);
        dbg.drawImage(p2.TaNk(),p2.tankx,p2.tanky,this);
        g.drawImage (dbImage, 0, 0, this);
    }
    boolean efe=true;
    public void paint(Graphics g)
    {
        g.setColor(Color.black);
        if(proj0!=null)
        {
            g.drawImage(proj0.i,(int)proj0.coord[0],(int)proj0.coord[1],this);
            if(secondweap)
                g.drawImage(proj1.i,(int)proj1.coord[0],(int)proj1.coord[1],this);
        }
        ls.paintMap(g);
        if(efe==false)
        {
            g.fillOval((int)xShell[0]-10,(int)yShell[0]-10,20,20);
            try 
            {
                thr.sleep(1000);
            }
            catch (InterruptedException e){}
        }
    }
    public boolean AiImpact(double x,double y)
    {
        if(x>699||y>400)
        {
            return true;
        }
        if(x>=0&&y>=ls.map[(int)x])
        {
            return true;
        }
        if(x<=0)
        {
            return true;
        }
        else return false;
    }
    public boolean testForImpact(double x,double y)
    {
        if(x>699||y>400)
        {
            if(x>=700)
                hitwall=true;
            return true;
        }
        if(y>=ls.map[(int)x])
        {
            return true;
        }
        if(x<=0)
        {
            hitwall=true;
            return true;
        }
        else return false;
    }
}
