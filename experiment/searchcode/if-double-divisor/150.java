package protogoose.screens;

import deadmarslib.Entities.Entity;
import deadmarslib.Entities.EntityCollision;
import deadmarslib.Entities.EntityManager;
import deadmarslib.Game.GameCamera;
import deadmarslib.Game.GameInput;
import deadmarslib.Game.GameTime;
import deadmarslib.ScreenManager.Screen;
import deadmarslib.ScreenManager.Screens.LoadingScreen;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import protogoose.ProtoGoose;
import protogoose.assets.Assets;
import protogoose.entities.Block;
import protogoose.entities.Box;
import protogoose.entities.LeftGrassyBlock;
import protogoose.entities.NightTreesBackground;
import protogoose.entities.RightGrassyBlock;
import protogoose.entities.TacoShack;
import protogoose.entities.TopGrassyBlock;
import protogoose.entities.Zombie;

public class ArenaScreen extends Screen {
    
    Rectangle treeArea = new Rectangle(0,0,2400,1200);
    EntityManager entMan = new EntityManager(treeArea, 5, false);
    
    Polygon viewArea = new Polygon();
    GameCamera gameCam = new GameCamera();

    public Zombie player = new Zombie(new Rectangle(0,0, 32, 80), gameCam);
    
    boolean mouseGrabbing = false;

    @Override
    public void handleInput(GameInput input) {
        super.handleInput(input);
        player.handleInput(input);
        
        if (input.isKeyDown(KeyEvent.VK_ESCAPE)) {
            input.removeKeyDown(KeyEvent.VK_ESCAPE);
            LoadingScreen.load(this.getScreenManager(), true, new Screen[]{new MenuScreen()});
        }
        
        if(!input.isMouseDown(MouseEvent.BUTTON1)) {
            mouseGrabbing = false;
        }
    }

    @Override
    public void loadContent() {
        super.loadContent();

        loadMap(map, 100);
        
        entMan.addEntity(player);
    }

    @Override
    public void update(GameTime gameTime, boolean otherScreenHasFocus, boolean coveredByOtherScreen) {
        super.update(gameTime, otherScreenHasFocus, coveredByOtherScreen);

        entMan.updateEntities(gameTime);
        
        int camX = (int)player.getX() - ProtoGoose.resolution.width/2+12;
        int camY = (int)player.getY() - ProtoGoose.resolution.height/2+12;
        int camW = this.getScreenManager().game.getResolution().width;
        int camH = this.getScreenManager().game.getResolution().height;
        
        //camX = ((int)(((int)player.getX()+12) / ProtoGoose.resolution.width)) * ProtoGoose.resolution.width;
        //camY = ((int)(((int)player.getY() + 50) / ProtoGoose.resolution.height)) * ProtoGoose.resolution.height;
        
        gameCam.setCamera(camX, camY, camW, camH);
        gameCam.lockTo(treeArea);
    }

    @Override
    public void render(GameTime gameTime, Graphics g) {
        super.render(gameTime, g);
        
        g.setColor(Assets.blackSky);
        g.fillRect(0, 0, this.getScreenManager().game.getResolution().width, this.getScreenManager().game.getResolution().height);
        entMan.renderEntities(new Class[]{Zombie.class, Box.class, TacoShack.class}, gameCam.getCamera(0.5, 1.0), gameTime, g);
        entMan.renderEntities(new Class[]{Zombie.class, Box.class, NightTreesBackground.class}, gameCam.getCamera(), gameTime, g);
        entMan.renderEntities(new Class[]{Zombie.class, NightTreesBackground.class}, gameCam.getCamera(), gameTime, g);
        player.render(gameTime, g);
        
        final GameInput input = this.getScreenManager().getInput();
        
        final double divisor = (double)this.getScreenManager().game.getWidth() / (double)this.getScreenManager().game.getResolution().width;
        
        int mx = (int) (input.getMouseX() / divisor);
        int my = (int) (input.getMouseY() / divisor);
        
        final Graphics thisG = g;
        final GameCamera thisCam = gameCam;
        
        EntityCollision boxCol = new EntityCollision(){

            @Override
            public void onCollision(Entity ent1, Entity ent2) {
                if(input.isMouseDown(MouseEvent.BUTTON1) && !mouseGrabbing) {
                    mouseGrabbing = true;
                    ((Box)ent2).grabbed = true;
                    ((Box)ent2).grabPoint.x = (int) ((input.getMouseX()/divisor+thisCam.getX()) - (int)ent2.getX());
                    ((Box)ent2).grabPoint.y = (int) ((input.getMouseY()/divisor+thisCam.getY()) - (int)ent2.getY());
                }
                thisG.setColor(Color.red);
                thisG.drawRect((int)ent2.getX() - thisCam.getX()-1, (int)ent2.getY()-thisCam.getY()-1, (int)ent2.getWidth()+1, (int)ent2.getHeight()+1);
            }
        };
        
        entMan.externalCollision(new Point(mx +gameCam.getX(), my+gameCam.getY()), new Class[]{Box.class}, new EntityCollision[]{boxCol});
    }
    
    public void loadMap(String Map, int x) {
        GameInput input = this.getScreenManager().getInput();
        
        //Read Map and Create Objects
        int count = 0;
        int row = 0;
        int col = 0;
        int ghost = 0;
        while (count < map.length()) {
            int entX = col*24;
            int entY = row*24;
            if(map.charAt(count) == 'B') {
                entMan.addEntity(new Block(new Rectangle(entX,entY, 24, 24), gameCam, input));
            } else if(map.charAt(count) == 'T') {
                entMan.addEntity(new TopGrassyBlock(new Rectangle(entX,entY, 24, 24), gameCam, input));
            } else if(map.charAt(count) == 'C') {
                entMan.addEntity(new Box(new Rectangle(entX,entY, 24, 24), gameCam, input));
            } else if(map.charAt(count) == 'S') {
                entMan.addEntity(new TacoShack(new Rectangle(entX, entY, 352, 352), gameCam));
            } else if(map.charAt(count) == 'N') {
                entMan.addEntity(new NightTreesBackground(new Rectangle(entX, entY, 504, 240), gameCam));
            } else if(map.charAt(count) == 'L') {
                entMan.addEntity(new LeftGrassyBlock(new Rectangle(entX, entY, 504, 240), gameCam, input));
            } else if(map.charAt(count) == 'R') {
                entMan.addEntity(new RightGrassyBlock(new Rectangle(entX, entY, 504, 240), gameCam, input));
            } else if(map.charAt(count) == 'P') {
                player.setPosition(entX, entY);
            }

            col++;
            if(col >= x)
            {
                row++;
                col = 0;
            }
            count++;
        }
    }
    
    String map = ""+
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                  S                        BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "BN                   N                    N BBBBBBBBBBBBBBB                       N                B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B                                           BBBBBBBBBBBBBBB                                        B" + 
            "B    P                                      BBBBBBBBBBBBBBB                                        B" +
            "B                                           BBBBBBBBBBBBBBB                                        B" +
            "B                                                                                                  B" +
            "B                                                                                                  B" +
            "B    CC                                                                                            B" +
            "B    CCC                                                                                           B" +
            "B   CCCCC                                                                                          B" + 
            "BTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB  BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB  BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB  BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB  BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB    BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB    BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB    BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "B                                                                                                  B" + 
            "B                                                                                                  B" + 
            "B                                                                                                  B" + 
            "B                                                                                                  B" + 
            "BTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" + 
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    
}

