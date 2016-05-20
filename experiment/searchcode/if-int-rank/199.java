package net.minekingdom.snaipe.MKManager.gui;

import java.util.ArrayList;
import java.util.List;

import net.minekingdom.snaipe.MKManager.MKManager;
import net.minekingdom.snaipe.MKManager.TextWrapper;
import net.minekingdom.snaipe.MKManager.world.Roll;
import net.minekingdom.snaipe.MKManager.world.Theme;

import org.getspout.spoutapi.gui.ContainerType;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.gui.WidgetAnchor;

public class CardDisplay extends GenericPopup {
        
    private MKManager plugin;
    
    private Message text;
    private Roll roll;
    
    public CardDisplay(Screen screen, Roll roll)
    {
        this.plugin = MKManager.getInstance();
        
        this.roll = roll;
        
        this.setWidth(screen.getWidth()).setHeight(screen.getHeight());
        this.setMargin(0, 40);
        this.setMarginTop(15);
        
        text = new Message(this, roll.getFirstCard().getAction().getName(), roll.getFullText());
        
        this.attachWidgets(plugin, text);
        this.setPriority(RenderPriority.Lowest);
    }
    
    private class Message extends GenericContainer {
        
        private List<Label> labels = new ArrayList<Label>();
        
        public Message(Screen screen, String title, String message)
        {
            this.setAnchor(WidgetAnchor.CENTER_CENTER);
            
            int width = screen.getWidth() - ( screen.getMarginLeft() + screen.getMarginRight() );
            
            Title titleContainer = new Title(title, width);
            
            this.addChild(titleContainer);
            
            String[] lines = TextWrapper.wrapText(message, width);
            
            int index = 0;
            for ( String line : lines )
            {
                int label_y = 10 * index + 30;
                
                Label label = new Label(line, label_y);
                labels.add(label);
                this.addChild(label);
                index++;
            }
            
            int height = 15*index + 25;
            
            int y = height + screen.getMarginTop();
            
            
            
            this.shiftYPos(-y);
            this.setWidth(width).setHeight(height);
            this.setPriority(RenderPriority.Highest);
        }
        
        private class Title extends GenericContainer {
            
            public Title(String title, int width)
            {
                this.setFixed(true);
                this.setLayout(ContainerType.HORIZONTAL);
                this.setHeight(15).setWidth(14 + 10 + TextWrapper.getStringLength(title));
                this.setAnchor(WidgetAnchor.TOP_CENTER);
                this.setX(0).setY(0);
                
                Symbol symbol = new Symbol();
                TitleText titleText = new TitleText(title);
                
                this.addChildren(titleText, symbol);
            }
            
            private class Symbol extends GenericContainer {

                public Symbol()
                {
                    this.setHeight(10).setWidth(14);
                    this.setLayout(ContainerType.HORIZONTAL);
                    this.setAnchor(WidgetAnchor.TOP_CENTER);
                    this.setX(0).setY(0);
                    this.setMargin(0, 5);
                    this.setFixed(true);
                    
                    Rank rank = new Rank();
                    this.addChild(rank);
                    
                    if ( !roll.getTheme().equals(Theme.SPECIAL) )
                    {
                        Color color = new Color();
                        this.addChild(color);
                    }
                }
                
                private class Rank extends GenericLabel {
                    
                    public Rank()
                    {
                        this.setHeight(8);
                        this.setFixed(true);
                        
                        if ( roll.getTheme().equals(Theme.SPECIAL) )
                        {
                            this.setText("JK");
                            
                            return;
                        }
                        
                        int rank = roll.getFirstCard().ordinal() % 13 + 1;
                        
                        if ( rank < 10 )
                        {
                            //this.setUrl("http://www.minekingdom.net/manager/" + (rank + 1) + ".png");
                            this.setText(String.valueOf(rank + 1));
                            this.setWidth(TextWrapper.getStringLength(String.valueOf(rank + 1)));
                        }
                        else if ( rank == 10 )
                        {
                            //this.setUrl("http://www.minekingdom.net/manager/V.png");
                            this.setText("V");
                            this.setWidth(TextWrapper.getStringLength("V"));
                        }
                        else if ( rank == 11 )
                        {
                            //this.setUrl("http://www.minekingdom.net/manager/Q.png");
                            this.setText("Q");
                            this.setWidth(TextWrapper.getStringLength("Q"));
                        }
                        else if ( rank == 12 )
                        {
                            //this.setUrl("http://www.minekingdom.net/manager/R.png");
                            this.setText("R");
                            this.setWidth(TextWrapper.getStringLength("R"));
                        }
                        else if ( rank == 13 )
                        {
                            //this.setUrl("http://www.minekingdom.net/manager/A.png");
                            this.setText("A");
                            this.setWidth(TextWrapper.getStringLength("A"));
                        }
                    }
                }
                
                private class Color extends GenericTexture {
                    
                    public Color()
                    {
                        this.setWidth(6).setHeight(6);
                        this.setFixed(true);
                        
                        int color = roll.getFirstCard().getTheme().ordinal();
                        
                        if ( color == 0 )
                        {
                            this.setUrl("http://www.minekingdom.net/manager/heart.png");
                        }
                        else if ( color == 1 )
                        {
                            this.setUrl("http://www.minekingdom.net/manager/spade.png");
                        }
                        else if ( color == 2 )
                        {
                            this.setUrl("http://www.minekingdom.net/manager/diamond.png");
                        }
                        else if ( color == 3 )
                        {
                            this.setUrl("http://www.minekingdom.net/manager/club.png");
                        }
                    }
                }
            }
        }
        
        private class TitleText extends GenericContainer {

            public TitleText(String text)
            {
                int width = TextWrapper.getStringLength(text);
                
                this.setAnchor(WidgetAnchor.TOP_CENTER);
                this.setX(-19).setY(0);
                this.setHeight(10).setWidth(width);
                this.setMargin(0);
                this.setFixed(true);
                
                Label title = new Label(text);
                
                this.addChild(title);
            }
            
            private class Label extends GenericLabel {
                
                public Label(String line)
                {
                    this.setHeight(10);
                    this.setAlign(WidgetAnchor.TOP_CENTER);
                    
                    this.setText(line);
                    this.setResize(false);
                }
            }
        }
        
        private class Label extends GenericLabel {
            
            public Label(String line, int y)
            {
                this.setHeight(10);
                this.setAlign(WidgetAnchor.TOP_CENTER);
                this.shiftYPos(y);
                
                this.setText(line);
                this.setResize(false);
            }
        }
    }
}

