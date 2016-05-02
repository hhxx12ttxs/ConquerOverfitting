/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.flymoore.yaengine.level;

import com.flymoore.yaengine.managers.ImageManager;
import com.flymoore.yaengine.objects.MapTileData;
import java.util.ArrayList;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;
import org.newdawn.slick.util.xml.SlickXMLException;
import org.newdawn.slick.util.xml.XMLElement;
import org.newdawn.slick.util.xml.XMLElementList;
import org.newdawn.slick.util.xml.XMLParser;

/**
 *
 * @author jlulian38
 */
public class LevelLoader {
    protected String file;
    protected MapData mapdata;

    public LevelLoader(String level_xml) throws SlickException {
        file = level_xml;
        mapdata = new MapData();
        parse();
    }

    protected void parse() throws SlickException {
        XMLElement level = new XMLParser().parse(file);

        XMLElementList leveldataf = level.getChildrenByName("leveldata");
        if (leveldataf.size() == 1) //there should only be one, freak out otherwise.
        {
            ArrayList<XMLElement> leveldatum_list = new ArrayList<XMLElement>();
            leveldataf.get(0).getChildren().addAllTo(leveldatum_list);

            for (XMLElement child: leveldatum_list)
            {
                if (child.getName().equals("tile"))
                {
                    float x = (float)child.getDoubleAttribute("x");
                    float y = (float)child.getDoubleAttribute("y");
                    int layer = child.getIntAttribute("layer", 0);
                    Image img = null;

                    XMLElementList imagedataf = child.getChildrenByName("image");
                    if (imagedataf.size() == 1) {
                        XMLElement imagedata = imagedataf.get(0);

                        String src = imagedata.getAttribute("src");
                        img = new Image(src);

                        try {
                            int ix = imagedata.getIntAttribute("x");
                            int iy = imagedata.getIntAttribute("y");
                            int iw = imagedata.getIntAttribute("w");
                            int ih = imagedata.getIntAttribute("h");
                            float iscale = (float)imagedata.getDoubleAttribute("scale",0);
                            img = img.getSubImage(ix,iy,iw,ih);
                            if (iscale != 0)
                                img = img.getScaledCopy(iscale);
                        } catch (SlickXMLException e) {
                            //do nothing this was expected
                        }
                    } else {
                        XMLElementList refdataf = child.getChildrenByName("ref");
                        if (refdataf.size() == 1)
                        {
                            XMLElement ref = refdataf.get(0);
                            img = ImageManager.getInstance().getImage(ref.getAttribute("name"));
                        } else {
                            throw new SlickXMLException("No image specified for <tile>@"+x+","+y);
                        }
                    }
                    mapdata.addNewTile(new MapTileData(layer,img, new Vector2f(x,y)));
                }
            }
        } else {
            throw new SlickXMLException("<leveldata> tag malformed in: "+file);
        }
    }

    public MapData getMapData() {
        return mapdata;
    }
}

