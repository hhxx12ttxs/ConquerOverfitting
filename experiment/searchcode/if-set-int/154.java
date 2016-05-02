package org.loon.framework.javase.game.action.map.tmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.loon.framework.javase.game.core.graphics.opengl.GL;
import org.loon.framework.javase.game.core.graphics.opengl.GLColor;
import org.loon.framework.javase.game.core.graphics.opengl.GLEx;
import org.loon.framework.javase.game.core.graphics.opengl.LTexture;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Copyright 2008 - 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email ceponline@yahoo.com.cn
 * @version 0.1.0
 */
public class TMXLayer {

	// ??????
	public static class Light {

		private float xpos;

		private float ypos;

		private float strength;

		private GLColor col;

		public Light(float x, float y, float str, GLColor col) {
			xpos = x;
			ypos = y;
			strength = str;
			this.col = col;
		}

		public void setLocation(float x, float y) {
			xpos = x;
			ypos = y;
		}

		public float[] getEffectAt(float x, float y, boolean colouredLights) {
			float dx = (x - xpos);
			float dy = (y - ypos);
			float distance2 = (dx * dx) + (dy * dy);
			float effect = 1 - (distance2 / (strength * strength));
			if (effect < 0) {
				effect = 0;
			}
			if (colouredLights) {
				return new float[] { col.r * effect, col.g * effect,
						col.b * effect };
			} else {
				return new float[] { effect, effect, effect };
			}
		}

	}

	private static byte[] base64;

	// ????
	private final TMXTiledMap tmxMap;

	// ????
	public int index;

	// XML???
	public String name;

	// ????
	public int[][][] data;

	// ????(TMX?????????/????)
	public int width;

	// ????(TMX?????????/????)
	public int height;

	// ????
	public TMXProperty props;

	// ?????????
	private boolean lightingOn;

	private boolean colouredLights;

	// ????
	private float[][][] lightValue;

	// ????
	private ArrayList<Light> lights = new ArrayList<Light>();

	// ???????
	private Light mainLight;

	/**
	 * ??TMX??????????
	 * 
	 * @param map
	 * @param element
	 * @throws RuntimeException
	 */
	public TMXLayer(TMXTiledMap map, Element element) throws RuntimeException {
		if (base64 == null) {
			base64 = new byte[256];
			for (int i = 0; i < 256; i++) {
				base64[i] = -1;
			}
			for (int i = 'A'; i <= 'Z'; i++) {
				base64[i] = (byte) (i - 'A');
			}
			for (int i = 'a'; i <= 'z'; i++) {
				base64[i] = (byte) (26 + i - 'a');
			}
			for (int i = '0'; i <= '9'; i++) {
				base64[i] = (byte) (52 + i - '0');
			}
			base64['+'] = 62;
			base64['/'] = 63;

		}
		this.tmxMap = map;
		name = element.getAttribute("name");
		width = Integer.parseInt(element.getAttribute("width"));
		height = Integer.parseInt(element.getAttribute("height"));
		data = new int[width][height][3];

		// ????????
		Element propsElement = (Element) element.getElementsByTagName(
				"properties").item(0);
		if (propsElement != null) {
			NodeList properties = propsElement.getElementsByTagName("property");
			if (properties != null) {
				props = new TMXProperty();
				for (int p = 0; p < properties.getLength(); p++) {
					Element propElement = (Element) properties.item(p);

					String name = propElement.getAttribute("name");
					String value = propElement.getAttribute("value");
					props.setProperty(name, value);
				}
			}
		}

		Element dataNode = (Element) element.getElementsByTagName("data").item(
				0);
		String encoding = dataNode.getAttribute("encoding");
		String compression = dataNode.getAttribute("compression");

		// ??base64?????
		if ("base64".equals(encoding) && "gzip".equals(compression)) {
			try {
				Node cdata = dataNode.getFirstChild();
				char[] enc = cdata.getNodeValue().trim().toCharArray();
				byte[] dec = decodeBase64(enc);
				GZIPInputStream is = new GZIPInputStream(
						new ByteArrayInputStream(dec));

				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						int tileId = 0;
						tileId |= is.read();
						tileId |= is.read() << 8;
						tileId |= is.read() << 16;
						tileId |= is.read() << 24;

						if (tileId == 0) {
							data[x][y][0] = -1;
							data[x][y][1] = 0;
							data[x][y][2] = 0;
						} else {
							TMXTileSet set = map.findTileSet(tileId);

							if (set != null) {
								data[x][y][0] = set.index;
								data[x][y][1] = tileId - set.firstGID;
							}
							data[x][y][2] = tileId;
						}
					}
				}
			} catch (IOException e) {
				throw new RuntimeException("Unable to decode base64 !");
			}
		} else {
			throw new RuntimeException("Unsupport tiled map type " + encoding
					+ "," + compression + " only gzip base64 Support !");
		}
	}

	private void createLight() {
		if (lightValue == null) {
			lightValue = new float[width + 1][height + 1][3];
		}
		lights.clear();
		if (mainLight != null) {
			lights.add(mainLight);
		}
		updateLight();
	}

	public void setMainLight(Light l) {
		mainLight = l;
	}

	public Light getMainLight() {
		return mainLight;
	}

	public void addLight(Light l) {
		if (l != null) {
			lights.add(l);
		}
	}

	public void removeLight(Light l) {
		if (l != null) {
			lights.remove(l);
		}
	}

	public void setLight(boolean l) {
		if (lightValue == null && l) {
			createLight();
		}
		this.lightingOn = l;
	}

	public boolean isLight() {
		return lightingOn;
	}

	public void clearLight() {
		if (lights != null) {
			lights.clear();
		}
	}

	public void updateLight() {
		if (mainLight == null) {
			throw new RuntimeException("the main light is null !");
		}
		for (int y = 0; y < height + 1; y++) {
			for (int x = 0; x < width + 1; x++) {
				for (int component = 0; component < 3; component++) {
					lightValue[x][y][component] = 0;
				}
				for (int i = 0; i < lights.size(); i++) {
					float[] effect = ((Light) lights.get(i)).getEffectAt(x, y,
							colouredLights);
					for (int component = 0; component < 3; component++) {
						lightValue[x][y][component] += effect[component];
					}
				}
				for (int component = 0; component < 3; component++) {
					if (lightValue[x][y][component] > 1) {
						lightValue[x][y][component] = 1;
					}
				}
			}
		}
	}

	/**
	 * ?????????ID
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public int getTileID(int x, int y) {
		return data[x][y][2];
	}

	/**
	 * ?????????ID
	 * 
	 * @param x
	 * @param y
	 * @param tile
	 */
	public void setTileID(int x, int y, int tile) {
		if (tile == 0) {
			data[x][y][0] = -1;
			data[x][y][1] = 0;
			data[x][y][2] = 0;
		} else {
			TMXTileSet set = tmxMap.findTileSet(tile);

			data[x][y][0] = set.index;
			data[x][y][1] = tile - set.firstGID;
			data[x][y][2] = tile;
		}
	}

	/**
	 * ????????LGraphics??
	 * 
	 * @param g
	 * @param x
	 * @param y
	 * @param sx
	 * @param sy
	 * @param width
	 * @param ty
	 * @param isLine
	 * @param mapTileWidth
	 * @param mapTileHeight
	 */
	public void draw(GLEx g, int x, int y, int sx, int sy, int width, int ty,
			boolean isLine, int mapTileWidth, int mapTileHeight) {

		int tileCount = tmxMap.getTileSetCount();

		int nx, ny, sheetX, sheetY, tileOffsetY;

		int cx, cy;

		g.glBegin(GL.GL_TRIANGLE_STRIP, false);

		for (int tileset = 0; tileset < tileCount; tileset++) {

			TMXTileSet set = null;

			for (int tx = 0; tx < width; tx++) {

				nx = sx + tx;
				ny = sy + ty;

				if ((nx < 0) || (ny < 0)) {
					continue;
				}
				if ((nx >= this.width) || (ny >= this.height)) {
					continue;
				}

				if (data[nx][ny][0] == tileset) {
					if (set == null) {
						set = tmxMap.getTileSet(tileset);
					}

					sheetX = set.getTileX(data[nx][ny][1]);
					sheetY = set.getTileY(data[nx][ny][1]);

					tileOffsetY = set.tileHeight - mapTileHeight;

					LTexture tex2d = set.tiles.getSubImage(sheetX, sheetY);

					cx = x + (tx * mapTileWidth);
					cy = y + (ty * mapTileHeight) - tileOffsetY;

					if (lightingOn) {
						setLightColor(tex2d, cx / mapTileWidth, cy
								/ mapTileHeight);
					}

					tex2d.draw(cx, cy, mapTileWidth, mapTileHeight);

				}
			}

			if (isLine) {
				if (set != null) {
					set = null;
				}
				tmxMap.rendered(ty, ty + sy, index);
			}
		}
		g.glEnd();
	}

	/**
	 * ????????
	 * 
	 * @param image
	 * @param x
	 * @param y
	 */
	private void setLightColor(LTexture image, int x, int y) {
		if (x < width && y < height) {
			image.setColor(LTexture.TOP_LEFT, lightValue[x][y][0],
					lightValue[x][y][1], lightValue[x][y][2], 1);
			image.setColor(LTexture.TOP_RIGHT, lightValue[x + 1][y][0],
					lightValue[x + 1][y][1], lightValue[x + 1][y][2], 1);
			image
					.setColor(LTexture.BOTTOM_RIGHT,
							lightValue[x + 1][y + 1][0],
							lightValue[x + 1][y + 1][1],
							lightValue[x + 1][y + 1][2], 1);
			image.setColor(LTexture.BOTTOM_LEFT, lightValue[x][y + 1][0],
					lightValue[x][y + 1][1], lightValue[x][y + 1][2], 1);
		}
	}

	/**
	 * ??base64??????????????
	 * 
	 * @param data
	 * @return
	 */
	private byte[] decodeBase64(char[] data) {
		int temp = data.length;
		for (int ix = 0; ix < data.length; ix++) {
			if ((data[ix] > 255) || base64[data[ix]] < 0) {
				--temp;
			}
		}

		int len = (temp / 4) * 3;
		if ((temp % 4) == 3) {
			len += 2;
		}
		if ((temp % 4) == 2) {
			len += 1;
		}
		byte[] out = new byte[len];

		int shift = 0;
		int accum = 0;
		int index = 0;

		for (int ix = 0; ix < data.length; ix++) {
			int value = (data[ix] > 255) ? -1 : base64[data[ix]];

			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}

		if (index != out.length) {
			throw new RuntimeException("index != " + out.length);
		}

		return out;
	}
}
