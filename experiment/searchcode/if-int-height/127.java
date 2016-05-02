package com.bwj.tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.bwj.graphics.InputState;

public class Map {

	private int width, height;

	String name;

	HashMap<Integer, Layer> layers;

	int maxDepth = 1;

	private Tileset tileset;
	
	private boolean changed = true;
	
	private BufferedImage renderedMap;

	public Tileset getTileset() {
		return tileset;
	}

	public Map(int width, int height, String tilesetname) {
		this.width = width;
		this.height = height;
		layers = new HashMap<Integer, Layer>();
		for(int i = 1; i <= 3; i++) {
			addLayer(i);
		}
		maxDepth = 3;
		setTileset(tilesetname);
	}

	public Map(InputStream stream) {
		layers = new HashMap<Integer, Layer>();
		load(stream);
		//load();
	}

	public void addLayer(int layer) {
		layers.put(layer, new Layer(width, height));
	}


	public void setTileset(String name) {
		tileset = new Tileset(name);
	}

	public boolean update(long lastLoopLength, InputState input) {
		// TODO Auto-generated method stub
		return false;
	}

	public void redraw(Graphics2D graphics, int startx, int starty, int screenwidth, int screenheight) {
		if(screenwidth > width * 32)
			screenwidth = width * 32;
		if(screenheight > height * 32)
			screenheight = height * 32;
		if(changed) {
			renderedMap = new BufferedImage(width*32, height*32, BufferedImage.TYPE_INT_ARGB);
			Graphics2D imageGraphics = renderedMap.createGraphics();
			imageGraphics.setColor(Color.BLACK);
			imageGraphics.fillRect(0, 0, screenwidth*32, screenheight*32);
			for(int i = maxDepth; i > 0; i--) {
				if(layers.get(i) != null) {
					layers.get(i).redraw(imageGraphics);
				}
			}
			changed = false;
		}
		graphics.drawImage(renderedMap.getSubimage(startx, starty, screenwidth, screenheight), 0, 0, null);
	}
	public void redraw(Graphics2D graphics) {
		redraw(graphics, 0, 0, width * 32, height * 32);
	}

	public void load(InputStream stream) {
		//URL mapURL = this.getClass().getClassLoader().getResource("resources/maps/" + name + ".xml");
		try {

			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(stream);

			Element root = doc.getRootElement();
			width = Integer.parseInt(root.getAttributeValue("width"));
			height = Integer.parseInt(root.getAttributeValue("height"));
			String tilesetName = root.getAttributeValue("tileset");
			setTileset(tilesetName);

			List layers = root.getChildren("layer");
			for(Object elem : layers) {
				Element layerElem = (Element)elem;
				int depth = Integer.parseInt(layerElem.getAttributeValue("depth"));
				String tileData = layerElem.getText().trim();

				String[] tileIds = tileData.split("\\s+");

				Layer newLayer = new Layer(width, height);
				for(int j = 0; j < width * height; j++) {
					int x = j % width;
					int y = j / width;
					int tileId = Integer.parseInt(tileIds[j]);
					if(tileId == 0) {
						newLayer.setTile(x, y, null);
					}
					else {
						newLayer.setTile(x, y, tileset.getTile(tileId));
					}
				}
				this.layers.put(depth, newLayer);
				if(depth > maxDepth) {
					maxDepth = depth;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JDOMException e) {
			e.printStackTrace();
		}
	}

	public void save(File saveTo) {
		Element root = new Element("map");
		root.setAttribute("tileset", tileset.getName());
		root.setAttribute("width", Integer.toString(width));
		root.setAttribute("height", Integer.toString(height));


		for(Integer key : layers.keySet()) {
			Layer layer = layers.get(key);
			Element layerElem = new Element("layer");
			layerElem.setAttribute("depth", key.toString());
			layerElem.setText(layer.getTileData());
			root.addContent(layerElem);
		}

		Document doc = new Document(root);
		Format format = Format.getPrettyFormat();

		XMLOutputter output = new XMLOutputter(format);
		try {
			output.output(doc, new FileOutputStream(saveTo));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setTile(int layer, int x, int y, int tile) {
		layers.get(layer).setTile(x, y, tileset.getTile(tile));
		changed = true;
	}
	
	public int getTile(int layer, int x, int y) {
		return tileset.getID(layers.get(layer).getTile(x, y));
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}

