package yarhar.map;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.LinkedList;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import org.json.*;
import pwnee.*;
import yarhar.*;
import yarhar.cmds.*;
import yarhar.fileio.YarharFile;
import yarhar.images.ImageLibrary;
import yarhar.dialogs.*;
import yarhar.utils.*;


/** The top level object for a map being manipulated with the YarHar UI. */
public class LevelMap extends Level implements ClipboardOwner {
    
    /** Same as the filename for this map (just the name, not the whole path or extension). */
    public String name = "New Map";
    
    /** Stores the last file path this map was saved/loaded to. */
    public String filePath = "";
    
    /** the background color displayed in the YarHar editor for this map. */
    public int bgColor = 0xFFFFFF;
    
    /** A reference to the editor's camera. */
    public Camera camera;
    
    /** This map's Sprite library. */
    public SpriteLibrary spriteLib;
    
    /** The list of this map's layers in order from top to bottom. */
    public LinkedList<Layer> layers = new LinkedList<Layer>();
    
    public Layer selectedLayer = null;
    
    /** a short description of this map's intended use. */
    public String desc = "";
    
    /** If true, a grid is displayed in the editor. */
    public boolean displayGrid = true;
    
    /** If true, the grid snapping for placing and moving sprites will be enabled. */
    public boolean snapToGrid = true;
    
    /** The width of the grid displayed under (or above) the map. */
    public int gridW = 640;
    
    /** The height of the grid displayed under (or above) the map. */
    public int gridH = 480;
    
    /** The distance between vertical lines in the grid. */
    public int gridSpaceX = 32;
    
    /** The distance between horizontal lines in the grid. */
    public int gridSpaceY = 32;
    
    /** The color of the grid's lines */
    public Color gridColor = new Color(0xFF0000);
    
    /** Flag to tell if the map has been modified. */
    public boolean isModified = false;
    
    /** Flag to tell if the map has been saved. */
    public boolean isSaved = false;
    
    /** A list of sprites currently selected. */  
    public LinkedList<SpriteInstance> selectedSprites = new LinkedList<SpriteInstance>();
    
    /** The currently selected sprite */
    public SpriteInstance selectedSprite = null;
    
    /** True if we are currently dragging the mouse to form a selection rectangle. */
    public boolean isSelRect = false;
    
    /** Color of the selection rectangle */
    public Color selRectColor = new Color(0x007777);
    
    
    /** The mouse's most recently recorded world coordinates */
    public Point mouseWorld = new Point(0,0);
    
    /** The starting world X of the mouse's last drag. */
    public int dragStartX = 0;
    
    /** The starting world Y of the mouse's last drag. */
    public int dragStartY = 0;
    
    /** True if sprites are currently being dragged. */
    public boolean isDrag = false;
    
    /** True if sprites are currently being cloned. */
    public boolean isCloning = false;
    
    /** True if performing a gesture-based rotate/scale */
    public boolean isGesturing = false;
    
    /** Point used to track the gesture's anchor point. */
    public Point2D gestureAnchor = new Point2D.Double(0,0);
    public Point2D gestureAnchor2 = new Point2D.Double(0,0);
    
    /** Stores the starting angle for a rotate gesture. */
    public double gestureStartAngle = 0;
    
    /** The sprite right-click menu */
    public SpriteRClickMenu spriteMenu;
    
    /** A reference to Yarhar's status footer. */
    public StatusFooterPanel footer;
    
    /** A string containing the status text that will be sent to the footer. */
    public String statusText = "";
    
    
    /** Creates a blank map With an unpopulated sprite library and just one layer. */
    public LevelMap(EditorPanel game) {
        this(game,null);
        isSaved = false;
    }
    
    
    /** Load the map from a ymap file */
    public LevelMap(EditorPanel game, String path) {
        super(game);
        footer = game.frame.footer;
        
        if(path == null) {
            spriteLib = new SpriteLibrary(this);
            addLayer(new Layer());
            filePath = FileUtils.getWorkingPath();
        }
        else if(path.startsWith("LOAD_OLD:")) {
          path = path.substring(9);
          
          try {
                FileReader fr = new FileReader(path);
                BufferedReader br = new BufferedReader(fr);
                
                // read the json text from the file.
                String jsonStr = "";
                String line = br.readLine();
                while(line != null) {
                    jsonStr += line;
                    line = br.readLine();
                }
                
                // convert the json text into a json object and then construct this map from it.
                JSONObject json = new JSONObject(jsonStr);
                JSONObject yarmap = json.getJSONObject("yarmap");
                loadJSON(yarmap);
            }
            catch(Exception e) {
                System.err.println("Error reading JSON for map.");
            }
        }
        else {
            try {
                YarharFile yhFile = YarharFile.loadCompressed(path);
                String jsonStr = yhFile.jsonStr;
                
                // convert the json text into a json object and then construct this map from it.
                JSONObject json = new JSONObject(jsonStr);
                JSONObject yarmap = json.getJSONObject("yarmap");
                loadJSON(yarmap);
                spriteLib.setImgLib(yhFile.imgLib);
            }
            catch(Exception e) {
                System.err.println("Error reading JSON for map.");
            }
        }

        game.frame.updateTitle(name);
        game.frame.layersPanel.setMap(this);
        game.frame.spriteLibPanel.setLibrary(this.spriteLib);
        
        camera = game.camera;
        
        isModified = false;
        isSaved = true;
        
        spriteMenu = new SpriteRClickMenu(this);
    }
    
    
    public void clean() {
    }
    
    public void loadData() {
    }
    
    
    /** Produces a JSON string representing this map*/
    public String toJSON() {
        String result = "{";
        result += "\"name\":\"" + name + "\",";
        result += "\"bgColor\":" + bgColor + ",";
        result += "\"spriteLib\":" + spriteLib.toJSON() + ",";
        result += "\"layers\":[";
        boolean isFirst = true;
        for(Layer layer : layers) {
            if(!isFirst)
                result += ",";
            else
                isFirst = false;
            
            result += layer.toJSON();
        }
        result += "],";
        result += "\"desc\":\"" + desc + "\",";
        result += "\"gridSpaceX\":" + gridSpaceX + ",";
        result += "\"gridSpaceY\":" + gridSpaceY + ",";
        result += "\"gridW\":" + gridW + ",";
        result += "\"gridH\":" + gridH + ",";
        result += "\"gridColor\":" + gridColor.getRGB();
        result += "}";
        return result;
    }
    
    /** Loads this map from a json object. */
    public void loadJSON(JSONObject yarmap) {
        try {
            name = yarmap.getString("name");
            bgColor = yarmap.getInt("bgColor");
            spriteLib = new SpriteLibrary(this, yarmap.getJSONObject("spriteLib"));
            
            JSONArray layerListJ = yarmap.getJSONArray("layers");
            for(int i = 0; i < layerListJ.length(); i++) {
                JSONObject layerJ = layerListJ.getJSONObject(i);
                Layer layer = new Layer(layerJ, spriteLib);
                addLayer(layer);
            }
            
            desc = yarmap.getString("desc");
            gridSpaceX = yarmap.getInt("gridSpaceX");
            gridSpaceY = yarmap.getInt("gridSpaceY");
            gridW = yarmap.getInt("gridW");
            gridH = yarmap.getInt("gridH");
            gridColor = new Color(yarmap.getInt("gridColor"));
        }
        catch(Exception e) {
            System.err.println("Error reading JSON.");
        }
    }
    
    
    
    public void importLibrary(File file) {
        try {
            YarharFile yhFile = YarharFile.loadCompressed(file.getPath());
            
            // convert the json text into a json object and then construct this map from it.
            JSONObject json = new JSONObject(yhFile.jsonStr);
            JSONObject yarmap = json.getJSONObject("yarmap");
            ImageLibrary il = yhFile.imgLib;
            
            // import the library from the json.
            SpriteLibrary importLib = new SpriteLibrary(this, yarmap.getJSONObject("spriteLib"));
            new ImportLibraryEdit(spriteLib, importLib, il);
        }
        catch(Exception e) {
            System.err.println("Error reading JSON for map.");
        }
    }
    
    
    
    
    
    
    public void logic() {
        mouseWorld = getMouseWorld();
        
        // basic status text
        statusText = "Mouse: (" + mouseWorld.x + ", " + mouseWorld.y + ") ";
        
        statusText += "Layer: " + selectedLayer.name + " - ";
        
        if(selectedSprites.size() == 0) 
          statusText += "No sprites selected. ";
        else if(selectedSprites.size() == 1 && selectedSprite != null) {
          statusText += "1 sprite selected: " + selectedSprite.type.name + " - ";
          statusText += "position: (" + selectedSprite.x + "," + selectedSprite.y + ") ";
          statusText += "z-index: " + selectedSprite.zIndex + " ";
        }
        else
          statusText += selectedSprites.size() + " sprites selected. ";
        
        //// sprite interaction
        
        // When left click is released, resolve any operations associated with the mouse gesture.
        if(mouse.justLeftClicked) {
            // if we just finished making a selection rectangle, select all the sprites in the selection rectangle.
            if(isSelRect)
                selectionRectangle();
            
            // if we finished moving a set of sprites, finish moving them. 
            if(isDrag && !isCloning) {
                MoveSpriteEdit cmd = new MoveSpriteEdit(this);
            }
            
            // if we finished cloning a set of sprites, finish cloning them.
            if(isCloning) {
                CloneSpriteEdit cmd = new CloneSpriteEdit(this);
            }
            
            if(isGesturing && keyboard.isPressed(KeyEvent.VK_R)) {
                new RotateGestureEdit(this);
            }
            if(isGesturing && keyboard.isPressed(KeyEvent.VK_S)) {
                new ScaleGestureEdit(this);
            }
            if(isGesturing && keyboard.isPressed(KeyEvent.VK_T)) {
                new TileGestureEdit(this);
            }
            
            isDrag = false;
            isSelRect = false;
            isCloning = false;
            isGesturing = false;
        }
        
        // Have different manipulation modes for performing gestures.
        if(keyboard.isPressed(KeyEvent.VK_R)) {
            // Rotation gesture
            
            // Do initial calculations when mouse is just pressed.
            if(mouse.justLeftPressed) {
                isGesturing = true;
                
                // compute the sprite selection's center of mass.
                gestureAnchor = getSelectionCenter();
                
                // compute the start angle of the gesture relative to the anchor.
                gestureStartAngle = GameMath.angleTo(gestureAnchor.getX(), gestureAnchor.getY(), mouseWorld.x, mouseWorld.y);
                
                // save the original position and angles of all the sprites in the selection.
                for(SpriteInstance sprite : selectedSprites) {
                    sprite.startDragX = sprite.x;
                    sprite.startDragY = sprite.y;
                    sprite.startAngle = sprite.angle;
                }
            }
            
            // Do the rotation while the mouse is pressed.
            if(mouse.isLeftPressed) {
                double angleToMouse = GameMath.angleTo(gestureAnchor.getX(), gestureAnchor.getY(), mouseWorld.x, mouseWorld.y);
                rotateGesture(angleToMouse);
            }
            
            // status text
            statusText += "- Rotation ";
            if(selectedSprites.size() == 1) {
              statusText += selectedSprite.angle + " degrees ";
            }
            
        }
        else if(keyboard.isPressed(KeyEvent.VK_S)) {
            // Scale gesture
            
            if(mouse.justLeftPressed) {
                isGesturing = true;
                
                // save the drag start point
                gestureAnchor = getSelectionCenter();
                gestureAnchor2 = new Point(mouseWorld.x, mouseWorld.y);
                
                // save the original position and scales of all the sprites in the selection.
                for(SpriteInstance sprite : selectedSprites) {
                    sprite.startDragX = sprite.x;
                    sprite.startDragY = sprite.y;
                    sprite.startScaleX = sprite.scaleX;
                    sprite.startScaleY = sprite.scaleY;
                }
            }
            
            // Do the rotation while the mouse is pressed.
            if(mouse.isLeftPressed) {
                scaleGesture(mouseWorld);
            }
            
            // status text
            statusText += "- Scale ";
            if(selectedSprites.size() == 1) {
              statusText += "(" + selectedSprite.scaleX + ", " + selectedSprite.scaleY + ") ";
            }
        }
        else if(keyboard.isPressed(KeyEvent.VK_T)) {
            // Tile gesture
            
            if(mouse.justLeftPressed) {
                isGesturing = true;
                
                // save the drag start point
                gestureAnchor = getSelectionCenter();
                gestureAnchor2 = new Point(mouseWorld.x, mouseWorld.y);
                
                // save the original tile repeat values of all the sprites in the selection.
                for(SpriteInstance sprite : selectedSprites) {
                    sprite.startRepeatX = sprite.repeatX;
                    sprite.startRepeatY = sprite.repeatY;
                }
            }
            
            // Do the rotation while the mouse is pressed.
            if(mouse.isLeftPressed) {
                tileGesture(mouseWorld);
            }
            
            // status text
            statusText += "- Tile ";
            if(selectedSprites.size() == 1) {
              statusText += "(" + selectedSprite.repeatX + ", " + selectedSprite.repeatY + ") ";
            }
        }
        else if(!isGesturing) { 
            // No gestures being done. Do normal manipulation logic.
            
            // Click.
            if(mouse.justLeftPressed || mouse.justRightPressed) {
                dragStartX = mouseWorld.x;
                dragStartY = mouseWorld.y;
                
                if(!keyboard.isPressed(KeyEvent.VK_SPACE)) {
                    // Click a sprite.
                    selectedSprite = selectedLayer.tryClickSprite(mouse.position);
                    
                    // possibly unselect all sprites.
                    if(!selectedSprites.contains(selectedSprite) && (!keyboard.isPressed(KeyEvent.VK_SHIFT) || keyboard.isPressed(KeyEvent.VK_CONTROL)))
                        unselectAll();
                    
                    // if a sprite was clicked, select it!
                    if(selectedSprite != null) {
                        selectSprite(selectedSprite);
                        initDragSprites();
                    }
                }
                else
                    selectedSprite = null;
            }
            
            // Drag the selected sprite(s).
            if(mouse.isLeftPressed && selectedSprite != null) {
                if((mouseWorld.x != dragStartX || mouseWorld.y != dragStartY) && !isDrag) {
                    isDrag = true;
                    
                    // clone the sprite if we were holding CTRL and begin dragging the clone instead of the original. 
                    if(keyboard.isPressed(KeyEvent.VK_CONTROL)) {
                        cloneSelectedSprites();
                        isCloning = true;
                    }
                }
                
                if(isDrag) {
                    dragSelectedSprites(mouseWorld);
                }
            }
            
            
            // Create a selection rectangle if we drag after clicking in empty space.
            if(mouse.isLeftPressed && selectedSprite == null && keyboard.isPressed(KeyEvent.VK_SHIFT)) {
                if((mouseWorld.x != dragStartX || mouseWorld.y != dragStartY) && !isDrag) {
                    isSelRect = true; 
                    
                }
            }
            
            // Press Delete to delete the currently selected sprites.
            if(keyboard.justPressed(KeyEvent.VK_DELETE) && !selectedSprites.isEmpty()) {
                new DeleteSpriteEdit(this);
            }
            
            // Right clicking a selection of sprites pops up a menu.
            if(mouse.justRightPressed) {
                spriteMenu.show(this.game, mouse.x, mouse.y);
            }
            
            
            // Pressing the arrow keys will nudge the current sprite selection 1 pixel at a time.
            if(selectedSprites.size() > 0) {
                if(keyboard.justPressedRep(KeyEvent.VK_LEFT)) {
                    nudgeSelectedSprites(-1,0);
                }
                if(keyboard.justPressedRep(KeyEvent.VK_RIGHT)) {
                    nudgeSelectedSprites(1,0);
                }
                if(keyboard.justPressedRep(KeyEvent.VK_UP)) {
                    nudgeSelectedSprites(0,-1);
                }
                if(keyboard.justPressedRep(KeyEvent.VK_DOWN)) {
                    nudgeSelectedSprites(0,1);
                }
            }
            
            
            //// general camera controls
            
            // Pan the camera while the left mouse button is held (and shift is not held).
            if(mouse.isLeftPressed && !isSelRect && !isDrag && !isCloning) {
                camera.drag(mouse.position);
            }
               
            // Stop dragging the camera when we release the left or right mouse button.
            if(mouse.justLeftClicked)
               camera.endDrag();
                
            // Zoom in by scrolling the mouse wheel up.
            if(mouse.wheel < 0)
               camera.zoomAtScreen(1.25, mouse.position);
             
            // Zoom out by scrolling the mouse wheel down.
            if(mouse.wheel > 0)
               camera.zoomAtScreen(0.75, mouse.position);
            
        }
        
        footer.setText(statusText);
    }
    
    
    
    
    //// Coordinates
    
    /** Obtains the mouse's current world coordinates in integer form. */
    public Point getMouseWorld() {
        Point mouseScr = mouse.position;
        Point2D mouseWorld = camera.screenToWorld(mouseScr);
        int mx = (int) mouseWorld.getX();
        int my = (int) mouseWorld.getY();
        
        return new Point(mx,my);
    }
    
    
    /** Converts a world coordinate point to snapped world coordinates if grid snap is enabled. */
    public Point getSnappedCoords(Point worldPt) {
        if(snapToGrid) {
            int x = (worldPt.x/gridSpaceX)*gridSpaceX;
            int y = (worldPt.y/gridSpaceY)*gridSpaceY;
            return new Point(x,y);
        }   
        else
            return worldPt;
    }
    
    
    
    
    //// modify flag
    
    /** Flags this map as modified and puts an asterisk next to the map's name in the window title */
    public void flagModified() {
        isModified = true;
        
        EditorPanel editor = (EditorPanel) game;
        editor.frame.updateTitle(name + "*");
    }
    
    
    //// Layer operations
    
    /** Prepends a layer to this map (making the layer be on the top) and causes it to become selected. */
    public void addLayerFirst(Layer layer) {
        layers.addFirst(layer);
        selectedLayer = layer;
    }
    
    /** Appends a layer to this map (making the layer be on the bottom) and causes it to become selected. */
    public void addLayer(Layer layer) {
        layers.add(layer);
        selectedLayer = layer;
    }
    
    /** Moves a layer to another index */
    public void moveLayer(Layer layer, int destIndex) {
        if(!layers.contains(layer))
            return;
        
        int index = layers.indexOf(layer);

        layers.remove(layer);
        layers.add(destIndex, layer);
        selectedLayer = layer;
    }
    
    
    /** Switches to a different layer and unselects all sprites. */
    public void selectLayer(Layer layer) {
        if(!layers.contains(layer))
            return;
            
        selectedLayer = layer;
        unselectAll();
    }
    
    /** Deletes a layer. If it attempts to delete the only layer in the map, it fails and returns false. */
    public boolean deleteLayer(Layer layer) {
        if(!layers.contains(layer) || layers.size() == 1)
            return false;
        
        int index = layers.indexOf(layer);
        
        layers.remove(layer);
        
        if(index >= layers.size())
            index--;
        selectedLayer = layers.get(index);
        return true;
    }
    
    
    //// Drop sprites
    
    /** Drops a new sprite into the currently selected layer. */
    public SpriteInstance dropSpriteType(SpriteType spriteType, Point mouseWorld) {
    //    SpriteInstance sprite = selectedLayer.dropSpriteType(spriteType, getSnappedCoords(mouseWorld));
        DropSpriteEdit cmd = new DropSpriteEdit(this, spriteType, getSnappedCoords(mouseWorld));
        SpriteInstance sprite = cmd.sprite;

        return sprite;
    }
    
    
    
    
    
    
    //// Selecting sprites
    
    /** Selects a sprite and adds it to the list of currently selected sprites */
    public void selectSprite(SpriteInstance sprite) {
        if(sprite.isLocked)
          return;
        
        sprite.isSelected = true;
        
        // prevent duplicates
        if(selectedSprites.contains(sprite))
            return;

        // order the selectedSprites by their z-index in our selection list.
        int i;
        for(i = 0; i < selectedSprites.size(); i++) {
            SpriteInstance oSprite = selectedSprites.get(i);
            if(sprite.zIndex < oSprite.zIndex) {
                break;
            }
            
        }

        selectedSprites.add(i, sprite);
    }
    
    /** Selects all sprites in the current layer. */
    public void selectAll() {
        selectedSprites = new LinkedList<SpriteInstance>();
        selectedLayer.selectAll(true);
    }
    
    /** unselects all sprites in the current layer. */
    public void unselectAll() {
        selectedSprites = new LinkedList<SpriteInstance>();
        selectedLayer.selectAll(false);
    }
    
    /** Selects all sprites in the selection rectangle. */
    public void selectionRectangle() {
        int x = (int) Math.min(mouseWorld.x, dragStartX);
        int y = (int) Math.min(mouseWorld.y, dragStartY);
        int w = (int) Math.abs(mouseWorld.x - dragStartX);
        int h = (int) Math.abs(mouseWorld.y - dragStartY);
        
        for(SpriteInstance sprite : selectedLayer.sprites) {
            if(sprite.x >= x && sprite.x <= x+w && sprite.y >= y && sprite.y <= y+h) {
                selectSprite(sprite);
            }
        }
    }
    
    
    
    
    //// Moving sprites
    
    /** Snaps a sprite to the grid*/
    public void snapSprite(SpriteInstance sprite) {
        if(snapToGrid) {
            sprite.x = ((int)sprite.x/gridSpaceX)*gridSpaceX;
            sprite.y = ((int)sprite.y/gridSpaceY)*gridSpaceY;
        }
    }
    
    
    /** Initializes the currently selected sprites for dragging with the mouse */
    public void initDragSprites() {
        for(SpriteInstance sprite : selectedSprites) {
            sprite.startDragX = sprite.x;
            sprite.startDragY = sprite.y;
        }
    }
    
    /** Drags a sprite with the mouse */
    public void dragSprite(SpriteInstance sprite, Point mouseWorld) {
        int mdx = mouseWorld.x - dragStartX;
        int mdy = mouseWorld.y - dragStartY;
        
        sprite.x = sprite.startDragX + mdx;
        sprite.y = sprite.startDragY + mdy;
        
        snapSprite(sprite);
    }
    
    
    /** Drags all currently selected sprites with the mouse. */
    public void dragSelectedSprites(Point mouseWorld) {
        dragSprite(selectedSprite, mouseWorld);
        double mdx = selectedSprite.x - selectedSprite.startDragX;
        double mdy = selectedSprite.y - selectedSprite.startDragY;
            
        for(SpriteInstance sprite : selectedSprites) {
            sprite.x = sprite.startDragX + mdx;
            sprite.y = sprite.startDragY + mdy;
        }
    }
    
    
    /** Moves all currently selected sprites by some defined amount. */
    public void nudgeSelectedSprites(int x, int y) {
        for(SpriteInstance sprite : selectedSprites) {
            sprite.startDragX = sprite.x;
            sprite.startDragY = sprite.y;
            sprite.x += x;
            sprite.y += y;
        }
        new MoveSpriteEdit(this);
    }
    
    
    //// Rotating/scaling sprites
    
    /** Obtains the current sprite selection's center of mass. */
    public Point2D getSelectionCenter() {
        double cx = 0;
        double cy = 0;
        for(SpriteInstance sprite : selectedSprites) {
            cx += sprite.x/selectedSprites.size();
            cy += sprite.y/selectedSprites.size();
        }
        
        return new Point2D.Double(cx, cy);
    }
    
    /** Rotates the selected sprites relative to their current angle in degrees. */
    public void rotateSelectedSprites(double angle) {
        // compute the center of mass.
        Point2D center = getSelectionCenter();
        double cx = center.getX();
        double cy = center.getY();
        
        
        // rotate the sprites
        for(int i = 0; i < selectedSprites.size(); i++) {
            SpriteInstance sprite = selectedSprites.get(i);
            
            // rotate and move the sprites about their center of mass.
            double dx = sprite.x - cx;
            double dy = sprite.y - cy;
            AffineTransform rotation = AffineTransform.getRotateInstance(GameMath.d2r(0-angle));
            Point2D rotPt = rotation.transform(new Point2D.Double(dx,dy), null);
            
            sprite.x = cx + rotPt.getX();
            sprite.y = cy + rotPt.getY();
            sprite.rotate(sprite.angle + angle);
        }
    }
    
    /** Performs an iteration of the rotate mouse gesture */
    public void rotateGesture(double angleToMouse) {
        // determine the angle to set the sprite to
        double angle = angleToMouse - gestureStartAngle;
        
        // extract coordinates for convenience.
        double cx = gestureAnchor.getX();
        double cy = gestureAnchor.getY();
        
        for(SpriteInstance sprite : selectedSprites) {
            // rotate and move the sprites about their center of mass.
            double dx = sprite.startDragX - cx;
            double dy = sprite.startDragY - cy;
            AffineTransform rotation = AffineTransform.getRotateInstance(GameMath.d2r(0-angle));
            Point2D rotPt = rotation.transform(new Point2D.Double(dx,dy), null);
            
            sprite.x = cx + rotPt.getX();
            sprite.y = cy + rotPt.getY();
            sprite.rotate(sprite.startAngle + angle);
        }
    }
    
    /** Rotates the currently selected sprite to an absolute angle. */
    public void rotateSpriteAbs(double angle) {
        selectedSprite.rotate(angle);
    }
    
    /** scales the selected sprites relative to their current scale values. */
    public void scaleSelectedSprites(double uni, double x, double y) {
        try {
            // compute the center of mass.
            Point2D center = getSelectionCenter();
            double cx = center.getX();
            double cy = center.getY();
            
            // scale the sprites
            for(int i = 0; i < selectedSprites.size(); i++) {
                SpriteInstance sprite = selectedSprites.get(i);
                
                // scale and move the sprites about their center of mass.
                double dx = sprite.x - cx;
                double dy = sprite.y - cy;
                AffineTransform rot = AffineTransform.getRotateInstance(GameMath.d2r(0-sprite.angle));
                AffineTransform rotInv = rot.createInverse();
                AffineTransform scale = AffineTransform.getScaleInstance(uni*x,uni*y);
                
                AffineTransform catTrans = new AffineTransform();
                catTrans.concatenate(rot);
                catTrans.concatenate(scale);
                catTrans.concatenate(rotInv);
                
                Point2D scalePt = catTrans.transform(new Point2D.Double(dx,dy), null);
                
                sprite.x = cx + scalePt.getX();
                sprite.y = cy + scalePt.getY();
                
                sprite.scaleUni *= uni;
                sprite.scaleX *= x;
                sprite.scaleY *= y;
                sprite.transformChanged = true;
            }
        }
        catch (Exception e) { // Pokemon exception: Gotta catch 'em all!
            
        }
    }
    
    /** Performs an iteration for the scale mouse gesture. */
    public void scaleGesture(Point2D mouseWorld) {
        try {
            // compute the center of mass.
            double cx = gestureAnchor.getX();
            double cy = gestureAnchor.getY();
            
            // Compute the scale values based on the relative positions of the anchor point, 
            // the center of mass, and the mouse's current world position.
            double sx = (mouseWorld.getX() - cx)/(gestureAnchor2.getX() - cx);
            double sy = (mouseWorld.getY() - cy)/(gestureAnchor2.getY() - cy);
            
            for(SpriteInstance sprite : selectedSprites) {
                // scale and move the sprites about their center of mass.
                double dx = sprite.startDragX - cx;
                double dy = sprite.startDragY - cy;
                AffineTransform rot = AffineTransform.getRotateInstance(GameMath.d2r(0-sprite.angle));
                AffineTransform rotInv = rot.createInverse();
                AffineTransform scale = AffineTransform.getScaleInstance(sx,sy);
                
                AffineTransform catTrans = new AffineTransform();
                catTrans.concatenate(rot);
                catTrans.concatenate(scale);
                catTrans.concatenate(rotInv);
                
                Point2D scalePt = catTrans.transform(new Point2D.Double(dx,dy), null);
                
                sprite.x = cx + scalePt.getX();
                sprite.y = cy + scalePt.getY();
                
                sprite.scaleX = sprite.startScaleX * sx;
                sprite.scaleY = sprite.startScaleY * sy;
                sprite.transformChanged = true;
            }
        }
        catch (Exception e) { // Pokemon exception: Gotta catch 'em all!
            
        }
    }
    
    /** Scales the currently selected sprite to absolute values. */
    public void scaleSpriteAbs(double uni, double x, double y) {
        selectedSprite.scaleUni = uni;
        selectedSprite.scaleX = x;
        selectedSprite.scaleY = y;
        selectedSprite.transformChanged = true;
    }
    
    
    /** Tiles the selected sprites relative to their current tiling values. */
    public void tileSelectedSprites(double x, double y) {
        try {
            // tile the sprites
            for(int i = 0; i < selectedSprites.size(); i++) {
                SpriteInstance sprite = selectedSprites.get(i);
                
                sprite.repeatX *= x;
                sprite.repeatY *= y;
                sprite.transformChanged = true;
            }
        }
        catch (Exception e) { // Pokemon exception: Gotta catch 'em all!
            
        }
    }
    
    /** Performs an iteration for the tile mouse gesture. */
    public void tileGesture(Point2D mouseWorld) {
        try {
            // compute the center of mass.
            double cx = gestureAnchor.getX();
            double cy = gestureAnchor.getY();
            
            // Compute the scale values based on the relative positions of the anchor point, 
            // the center of mass, and the mouse's current world position.
            double sx = (mouseWorld.getX() - cx)/(gestureAnchor2.getX() - cx);
            double sy = (mouseWorld.getY() - cy)/(gestureAnchor2.getY() - cy);
            
            if(sx <= 0)
              sx = 0.1;
            if(sy <= 0)
              sy = 0.1;
            
            for(SpriteInstance sprite : selectedSprites) {
                sprite.repeatX = sprite.startRepeatX * sx;
                sprite.repeatY = sprite.startRepeatY * sy;
            }
            
            
        }
        catch (Exception e) { // Pokemon exception: Gotta catch 'em all!
            
        }
    }
    
    /** Tiles the currently selected sprite to absolute values. */
    public void tileSpriteAbs(double x, double y) {
        selectedSprite.repeatX = x;
        selectedSprite.repeatY = y;
    }
    
    //// Cloning sprites
    
    /** clones a sprite (not in the same sense as Object.clone(). */
    public SpriteInstance cloneSprite(SpriteInstance sprite) {
        SpriteInstance clone = sprite.makeClone();
        selectedLayer.addSprite(clone);
        return clone;
    }
    
    
    /** clones all currently selected sprites and sets the clones as the currently selected sprites. The original sprites become unselected. */
    public void cloneSelectedSprites() {
        LinkedList<SpriteInstance> newSelSprites = new LinkedList<SpriteInstance>();
        
        for(SpriteInstance sprite : selectedSprites) {
            SpriteInstance clone = cloneSprite(sprite);
            sprite.isSelected = false;
            clone.isSelected = true;
            
            newSelSprites.add(clone);
            selectedSprite = clone;
        }
        
        selectedSprites = newSelSprites;
    }
    
    //// Locking/Unlocking sprites
    
    /** Makes the selected sprites become unselected and unselectable. */
    public void lockSelectedSprites() {
      for(SpriteInstance sprite : selectedSprites) {
        sprite.isLocked = true;
      }
      unselectAll();
    }
    
    /** Unlocks all sprites in the currently selected layer. */
    public void unlockAll() {
      selectedLayer.unlockAll();
    }
    
    //// Deleting sprites
    
    /** Deletes all instances of a SpriteType in this map. Returns tuples of the deleted instances and the layers they were deleted from, for the purpose of undos. */
    public LinkedList<LayerInstTuple> deleteAllInstances(SpriteType type) {
        LinkedList<LayerInstTuple> delSprites = new LinkedList<LayerInstTuple>();
        
        
        for(Layer layer : layers) {
            LinkedList<SpriteInstance> sprites = new LinkedList<SpriteInstance>(layer.sprites);
            for(SpriteInstance inst : sprites) {
                if(inst.type == type) {
                    delSprites.add(new LayerInstTuple(layer, inst));
                    layer.removeSprite(inst);
                }
            }
        }
        
        return delSprites;
    }
    
    //// Reordering sprites
    /** Brings a selection of sprites to the top of this layer's z-ordering. */
    public void toFrontSelectedSprites() {
        selectedLayer.toFrontSelectedSprites();
    }
    
    public void fwdOneSelectedSprites() {
        selectedLayer.fwdOneSelectedSprites();
    }
    
    public void bwdOneSelectedSprites() {
        selectedLayer.bwdOneSelectedSprites();
    }
    
    /** Brings a selection of sprites to the bottom of this layer's z-ordering. */
    public void toBackSelectedSprites() {
        selectedLayer.toBackSelectedSprites();
    }
    
    
    
    
    //// Copy-Pasta
    
    /** Cuts the current sprite selection to the System clipboard. */
    public void cut() {
        copy();
        new DeleteSpriteEdit(this);
    }
    
    /** Copies the current sprite selection to the System clipboard. */
    public void copy() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        
        CopiedSprites copied = new CopiedSprites(selectedSprites);
        clipboard.setContents(copied, this);
    }
    
    /** Pastes the selection of sprites currently residing on the System clipboard. */
    public void paste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            if(clipboard.isDataFlavorAvailable(CopiedSprites.flavor)) {
                CopiedSprites contents = (CopiedSprites) clipboard.getData(CopiedSprites.flavor);
                
                // empty our selection
                unselectAll();
                
                // insert the sprites on the clipboard into this layer and select them.
                LinkedList<SpriteInstance> sprites = contents.getSprites(spriteLib);
                for(SpriteInstance sprite : sprites) {
                    selectedLayer.addSprite(sprite);
                    selectSprite(sprite);
                }
                
                // Create the undo/redo for this paste.
                new CloneSpriteEdit(this);
            }
        }
        catch(Exception e) {
            // Pokemon exception: gotta catch 'em all!
        }
    }
    
    
    public boolean copyEnabled() {
        return (selectedSprites.size() > 0);
    }
    
    
    public boolean pasteEnabled() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        return clipboard.isDataFlavorAvailable(CopiedSprites.flavor);
    }
    
    
    // required for ClipboardOwner interface.
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // do nothing.
    }
    
    
    
    //// Rendering
    
    /** Renders the map */
    public void render(Graphics2D g) {
        synchronized(this) {
            // render each of the layers in order of descending index. (The last layer is rendered on the bottom and the first layer is rendered on top)
            for(int i = layers.size() - 1; i >= 0; i--) {
                layers.get(i).render(g);
            }
            
            // render the grid with translucency
            Composite oldComp = g.getComposite();
            
            // Use an AlphaComposite to apply semi-transparency to the Sprite's image.
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) 0.25));
            renderGrid(g);
            renderSelectionRect(g);
            g.setComposite(oldComp);
        }
    }
    
    
    /** Renders the grid for this map */
    public void renderGrid(Graphics2D g) {
        if(!displayGrid)
            return;
        
        g.setColor(gridColor);
        for(int i = 0; i <= gridW; i += gridSpaceX) {
            g.drawLine(i, 0 , i, gridH);
        }
        for(int i = 0; i <= gridH; i += gridSpaceY) {
            g.drawLine(0,i,gridW,i);
        }
        
        g.drawRect(0,0,gridW,gridH);
    }
    
    /** Renders the selection rectangle for selecting multiple sprites. */
    public void renderSelectionRect(Graphics2D g) {
        if(!isSelRect)
            return;
        
        Stroke origStroke = g.getStroke();
        g.setStroke(new BasicStroke((float) (3/camera.zoom)));
        g.setColor(selRectColor);
        int x = (int) Math.min(mouseWorld.x, dragStartX);
        int y = (int) Math.min(mouseWorld.y, dragStartY);
        int w = (int) Math.abs(mouseWorld.x - dragStartX);
        int h = (int) Math.abs(mouseWorld.y - dragStartY);
        
        g.drawRect(x,y, w, h);
        
        g.setStroke(origStroke);
    }
    
}


/** Menu that appears when you right click a sprite selection.*/
class SpriteRClickMenu extends JPopupMenu implements ActionListener {
    LevelMap map;
    
    JMenuItem editTypeItem = new JMenuItem("Edit type");
    JMenuItem cutItem = new JMenuItem("Cut");
    JMenuItem copyItem = new JMenuItem("Copy");
    JMenuItem pasteItem = new JMenuItem("Paste");
    JMenu orderItems = new JMenu("Order");
        JMenuItem toFrontItem = new JMenuItem("Send to front");
        JMenuItem fwdOneItem = new JMenuItem("Forward one");
        JMenuItem bwdOneItem = new JMenuItem("Backward one");
        JMenuItem toBackItem = new JMenuItem("Send to back");
    JMenuItem rotateItem = new JMenuItem("Rotate");
    JMenuItem scaleItem = new JMenuItem("Scale");
    JMenuItem tileItem = new JMenuItem("Tile");
    JMenuItem opacityItem = new JMenuItem("Set opacity");
    JMenuItem lockItem = new JMenuItem("Lock selected");
    JMenuItem unlockItem = new JMenuItem("Unlock all");
    JMenuItem deleteItem = new JMenuItem("Delete");
    
    public SpriteRClickMenu(LevelMap map) {
        super();
        this.map = map;
    
        add(editTypeItem);
        editTypeItem.addActionListener(this);
        
        add(new JSeparator());
        
        add(cutItem);
        cutItem.addActionListener(this);
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        //cutItem.setEnabled(false);
        
        add(copyItem);
        copyItem.addActionListener(this);
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        //copyItem.setEnabled(false);
        
        add(pasteItem);
        pasteItem.addActionListener(this);
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        //pasteItem.setEnabled(false);
        
        add(new JSeparator());
        
        add(orderItems);
            orderItems.add(toFrontItem);
            toFrontItem.addActionListener(this);
            toFrontItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_QUOTE, ActionEvent.CTRL_MASK));
            
            orderItems.add(fwdOneItem);
            fwdOneItem.addActionListener(this);
            
            orderItems.add(bwdOneItem);
            bwdOneItem.addActionListener(this);
        
            orderItems.add(toBackItem);
            toBackItem.addActionListener(this);
            toBackItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, ActionEvent.CTRL_MASK));
        
        add(new JSeparator());
        
        add(rotateItem);
        rotateItem.addActionListener(this);
        
        add(scaleItem);
        scaleItem.addActionListener(this);
        
        add(tileItem);
        tileItem.addActionListener(this);
        
        add(opacityItem);
        opacityItem.addActionListener(this);
        
        add(new JSeparator());
        
        add(lockItem);
        lockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        lockItem.addActionListener(this);
        
        add(unlockItem);
        unlockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        unlockItem.addActionListener(this);
        
        add(new JSeparator()); 
        
        add(deleteItem);
        deleteItem.addActionListener(this);
    }
    
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        EditorPanel editor = (EditorPanel) map.game;
        
        if(source == editTypeItem) {
            SpriteType type = map.selectedSprite.type;
            new NewSpriteTypeDialog(editor.frame, map.spriteLib, null, type);
        }
        
        if(source == cutItem) {
            map.cut();
        }
        if(source == copyItem) {
            map.copy();
        }
        if(source == pasteItem) {
            map.paste();
        }
        
        if(source == toFrontItem) {
            new ToFrontEdit(map);
        }
        if(source == fwdOneItem) {
            new FwdOneEdit(map);
        }
        if(source == bwdOneItem) {
            new BwdOneEdit(map);
        }
        if(source == toBackItem) {
            new ToBackEdit(map);
        }
        
        if(source == rotateItem) {
            RotateDialog dialog = new RotateDialog(editor.frame, (map.selectedSprites.size() > 1));
            if(dialog.returnedOK) {
                new RotateSpriteEdit(map, dialog.angle, dialog.isRelative);
            }  
        }
        if(source == scaleItem) {
            SpriteInstance selSprite = null;
            if(map.selectedSprites.size() == 1)
                selSprite = map.selectedSprite;
            
            ScaleDialog dialog = new ScaleDialog(editor.frame, selSprite);
            if(dialog.returnedOK) {
                new ScaleSpriteEdit(map, dialog.scaleUni, dialog.scaleX, dialog.scaleY, dialog.isRelative);
            }
        }
        if(source == tileItem) {
            SpriteInstance selSprite = null;
            if(map.selectedSprites.size() == 1)
                selSprite = map.selectedSprite;
                
            TileDialog dialog = new TileDialog(editor.frame, selSprite);
            if(dialog.returnedOK) {
                new TileSpriteEdit(map, dialog.tileX, dialog.tileY, dialog.isRelative);
            }
        }
        if(source == opacityItem) {
            double initOpac = 1.0;
            if(map.selectedSprites.size() == 1)
                initOpac = map.selectedSprite.opacity;
            
            OpacityDialog dialog = new OpacityDialog(editor.frame, initOpac);
            if(dialog.returnedOK) {
                new OpacitySpriteEdit(map, dialog.opacity, dialog.isRelative);
            }
        }
        if(source == lockItem) {
          new LockSpriteEdit(map);
        }
        if(source == unlockItem) {
          new UnlockSpriteEdit(map);
        }
        
        if(source == deleteItem) {
            new DeleteSpriteEdit(map);
        }
    }
    
    
    /** Updates the enabled state of copy-pasta buttons, then displays the menu. */
    public void show(Component origin, int x, int y) {
        boolean copyEnabled = (map.copyEnabled() && map.selectedSprite != null);
        cutItem.setEnabled(copyEnabled);
        
        copyItem.setEnabled(copyEnabled);
        
        boolean pasteEnabled = map.pasteEnabled();
        pasteItem.setEnabled(pasteEnabled);
        
        rotateItem.setEnabled(copyEnabled);
        scaleItem.setEnabled(copyEnabled);
        opacityItem.setEnabled(copyEnabled);
        lockItem.setEnabled(copyEnabled);
        
        super.show(origin, x, y);
    }
}




