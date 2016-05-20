/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pokeman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;
import java.util.Scanner;
import javax.swing.JFrame;

/**
 *
 * @author Mark
 */
public class TextBox implements Serializable {
    private static final int widthFactor = 20, heightFactor = 5, curveFactor = 40, spaceFactor = 5;
    private Font f;
    
    private String[] str;
    private int x,y,width,height,textHeight;
    private int line,lines;
    private JFrame frame;
    private boolean over;
    private int index,finalIndex;
    private int arrowOff;
    private boolean hasKeyListener;    
    private KeyListener key;
    private Style style;
    private boolean centered;
    
    public TextBox(JFrame fr,String s,int x,int y,int width,int height,boolean scrolls,boolean centered,Style style){
        this.style = style;
        this.centered = centered;
        f = Window.FONT;
        f = f.deriveFont(Font.PLAIN, 30);

        
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        key = new Key();
        
        fr.addKeyListener(key);
        frame = fr;
        
        
        
        textHeight = (int)f.getStringBounds(s, new FontRenderContext(null,false,false)).getHeight();
        lines = (int)((height-heightFactor+15)/(textHeight+spaceFactor));
        
        str = new String[(int)(f.getStringBounds(s, new FontRenderContext(null,false,false)).getWidth()/(width-widthFactor))+1];
        
        Scanner string = new Scanner(s);
        String words="";
        int i = 0;
        while(string.hasNext()){ 
            String last = words;
            String next = string.next()+" ";
            words+=next;
            if((int)f.getStringBounds(words, new FontRenderContext(null,false,false)).getWidth()>width-widthFactor){
                str[i] = last;
                words = next;
                i++;
            }
        }
        System.out.println(words);
            str[i] = words;
        
        index = 0;
        finalIndex = 0;
        arrowOff = 0;
        for(int j=0;j<lines;j++)    
            if(j<str.length)
                finalIndex += str[j].length();
        if(!scrolls)
            index = finalIndex;
        hasKeyListener = true;
    }
    
    public void removeKeyListener(){
        hasKeyListener = false;
        frame.removeKeyListener(key);
    }
    
    public void addKeyListener(){
        hasKeyListener = true;
        frame.addKeyListener(key);        
    }
    
    public void draw(Graphics2D g2){
        if(!over){
            
            g2.setColor(style.getColor(false));
            
            if(style.getShape(false) == Style.ROUNDED_RECTANGLE)
                g2.fill(new RoundRectangle2D.Double(x,y,width,height,curveFactor,curveFactor));
            if(style.getShape(false) == Style.RECTANGLE)
                g2.fill(new Rectangle2D.Double(x,y,width,height));
            
            g2.setColor(style.getColor(true));
            
            if(style.getShape(true) == Style.ROUNDED_RECTANGLE)
                g2.fill(new RoundRectangle2D.Double(x+widthFactor/2,y+heightFactor/2,width-widthFactor,height-heightFactor,curveFactor,curveFactor));
            if(style.getShape(true) == Style.RECTANGLE)
                g2.fill(new Rectangle2D.Double(x+widthFactor/2,y+heightFactor/2,width-widthFactor,height-heightFactor));
                
            g2.setColor(Color.BLACK);



            g2.setFont(f);

            for(int k=0;k<3;k++){
                if(index<finalIndex)
                {                
                    index++;
                }
            }
            
            int i=-line;
            while(i<lines && i+line<str.length){ 

                if(i>=0){
                    int tempIndex=0;
                    for(int j=0;j<=i;j++)
                            tempIndex += str[j+line].length();
                    if(index>tempIndex)
                        g2.drawString(str[i+line], x+widthFactor, y+heightFactor+textHeight*(i+1)+spaceFactor*(i-1)-15); 
                    else
                    {
                        if(index-tempIndex+str[i+line].length()>0){
                            String string = str[i+line].substring(0,index-tempIndex+str[i+line].length());
                            if(centered)
                                g2.drawString(string, (int)(x+width/2-f.getStringBounds(string, new FontRenderContext(null,false,false)).getWidth()/2), y+heightFactor+textHeight*(i+1)+spaceFactor*(i-1)-15);
                            else
                                g2.drawString(string, x+widthFactor, y+heightFactor+textHeight*(i+1)+spaceFactor*(i-1)-15);
                        }
                    }
                    
                        
                }
                i++;

            }
            
            
            
            if(index == finalIndex && hasKeyListener){
                if(arrowOff>5)
                {
                    int[] xCoords = {x+width-widthFactor-25,x+width-widthFactor-5,x+width-widthFactor-15};
                    int[] yCoords = {y+height-heightFactor-20,y+height-heightFactor-20,y+height-heightFactor-5};
                    g2.setColor(Color.BLACK);  
                    g2.fill(new Polygon(xCoords,yCoords,3));
                    if(arrowOff>10)
                        arrowOff=0;
                }
                arrowOff++;    
            }
        }

    }

    public void destroy(){
        over = true;
    }
    
    public boolean isOver(){
        return over;
    }
    
    public class Key implements KeyListener{


        
        public void keyTyped(KeyEvent e) {
            if(e.getKeyChar()=='z' && index==finalIndex){
                
                line+=2;
                index = 0;
                finalIndex = 0;
                for(int j=lines;j<lines+2;j++)    
                    if(j<str.length)
                        finalIndex += str[j].length();
                if(line>=str.length){
                    frame.removeKeyListener(this);
                    over = true;
                }
            }
        }

        public void keyPressed(KeyEvent e) { 
            if(e.getKeyChar()=='x'){
                index = finalIndex;
            }
        }

        public void keyReleased(KeyEvent e) {
        }
    }
            
}

