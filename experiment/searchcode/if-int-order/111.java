package javara.world;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javara.world.environment.Celestial;
import javara.world.environment.Sky;
import javara.world.goodies.GrenadeGoody;
import javara.world.physical.Block;
import javara.world.physical.Domeoid;
import javara.world.physical.Ground;
import javara.world.physical.Ramp;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class WorldLoader {
	public static World load(String fileName, Node rootNode, PhysicsSpace physicsSpace, AssetManager assets) throws IOException {
		InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
		InputSource src = new InputSource(is);
		MapParser parser = new MapParser(rootNode, physicsSpace, assets);
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(parser);
			reader.parse(src);
			return parser.get();
		}
		catch (SAXException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	protected static class MapParser extends DefaultHandler {
		protected World world;
		protected StringBuilder chars;
		protected int numLights = 0;

		public MapParser(Node root, PhysicsSpace space, AssetManager assets) {
			world = new World(root, space, assets);
			chars = new StringBuilder();
		}

		public World get() {
			return world;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equalsIgnoreCase("map")) {
				world.setName(attrs.getValue("name"));
				world.setAuthor(attrs.getValue("author"));
			}
			else if (localName.equalsIgnoreCase("sky")) {
				ColorRGBA skyColor, horizonColor, ambientColor;
				float horizonScale = -1.0f;

				skyColor = (attrs.getValue("color") != null) ? parseColor(attrs.getValue("color")) : null;
				horizonColor = (attrs.getValue("horizon") != null) ? parseColor(attrs.getValue("horizon")) : null;
				ambientColor = (attrs.getValue("ambient") != null) ? parseColor(attrs.getValue("ambient")) : null;
				if (attrs.getValue("horizonScale") != null)
					horizonScale = Float.parseFloat(attrs.getValue("horizonScale"));

				if (ambientColor != null)
					world.setAmbientColor(ambientColor);

				Sky sky = world.getSky();
				if (skyColor != null)
					sky.setColor(skyColor);
				if (horizonColor != null)
					sky.setHorizonColor(horizonColor);
				if (horizonScale >= 0)
					sky.setHorizonScale(horizonScale);
			}
			else if (localName.equalsIgnoreCase("ground")) {
				ColorRGBA groundColor = (attrs.getValue("color") != null) ? parseColor(attrs.getValue("color")) : null;
				Ground ground = world.getGround();
				Sky sky = world.getSky();

				if (groundColor != null) {
					ground.setColor(groundColor);
					sky.setGroundColor(groundColor);
				}
			}
			else if (localName.equalsIgnoreCase("celestial")) {
				ColorRGBA color;
				float azimuth = 90.0f, elevation = 45.0f, intensity = 0.4f, size = 1.0f;
				boolean visible = false;

				color = (attrs.getValue("color") != null) ? parseColor(attrs.getValue("color")) : new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f);

				if (attrs.getValue("azimuth") != null)
					azimuth = World.compassToRadians(Float.parseFloat(attrs.getValue("azimuth")));
				if (attrs.getValue("elevation") != null)
					elevation = Float.parseFloat(attrs.getValue("elevation")) * FastMath.DEG_TO_RAD;
				if (attrs.getValue("intensity") != null)
					intensity = Float.parseFloat(attrs.getValue("intensity"));
				if (attrs.getValue("size") != null)
					size = Float.parseFloat(attrs.getValue("size"));
				if (attrs.getValue("visible") != null)
					visible = parseBoolean(attrs.getValue("visible"));

				Celestial cel = new Celestial(world, color, intensity, azimuth, elevation, size, visible);
				world.addWorldObject(cel);

				if (intensity > 0)
					numLights++;
			}
			else if (localName.equalsIgnoreCase("starfield")) {
				long seed = world.getName().hashCode();
				int count = 500;
				boolean monochrome = false;
				ColorRGBA minColor = ColorRGBA.White;
				ColorRGBA maxColor = ColorRGBA.White;
				float minSize = 0.025f;
				float maxSize = 0.025f;

				if (attrs.getValue("seed") != null)
					seed = Long.parseLong(attrs.getValue("seed"));
				if (attrs.getValue("count") != null)
					count = Integer.parseInt(attrs.getValue("count"));
				if (attrs.getValue("monochrome") != null)
					monochrome = parseBoolean(attrs.getValue("monochrome"));
				if (attrs.getValue("minColor") != null)
					minColor = parseColor(attrs.getValue("minColor"));
				if (attrs.getValue("maxColor") != null)
					maxColor = parseColor(attrs.getValue("maxColor"));
				if (attrs.getValue("minSize") != null)
					minSize = Float.parseFloat(attrs.getValue("minSize"));
				if (attrs.getValue("maxSize") != null)
					minSize = Float.parseFloat(attrs.getValue("maxSize"));

				Random rand = new Random(seed);

				float minR = minColor.r;
				float deltaR = maxColor.r - minR;
				float minG = minColor.g;
				float deltaG = maxColor.g - minG;
				float minB = minColor.b;
				float deltaB = maxColor.b - minB;

				for (int i = 0; i < count; i++) {
					// Note: in order to end up with an even distribution of stars, we can't just
					// use two raw random numbers.
					// We need to scale them such that tere are fewer stars near the poles than near
					// the equator.
					// The following lines should result in an even distribution.
					float theta = FastMath.TWO_PI * rand.nextFloat();
					float phi = FastMath.abs(FastMath.HALF_PI - FastMath.acos(rand.nextFloat()));
					float r, g, b;

					if (monochrome) {
						float dice = rand.nextFloat();
						r = minR + dice * deltaR;
						g = minG + dice * deltaG;
						b = minB + dice * deltaB;
					}
					else {
						r = minR + rand.nextFloat() * deltaR;
						g = minG + rand.nextFloat() * deltaG;
						b = minB + rand.nextFloat() * deltaB;
					}
					ColorRGBA color = new ColorRGBA(r, g, b, (phi / FastMath.HALF_PI));
					float size = minSize + rand.nextFloat() * (maxSize - minSize);

					Celestial cel = new Celestial(world, color, 0.0f, theta, phi, size, true);
					world.addWorldObject(cel);
				}
			}
			else if (localName.equalsIgnoreCase("incarnator")) {
				Vector3f location;
				float angle = 0.0f;
				int order = 0;

				location = (attrs.getValue("location") != null) ? parseVector(attrs.getValue("location")) : new Vector3f(0.0f, 30.0f, 0.0f);

				if (attrs.getValue("angle") != null)
					angle = Float.parseFloat(attrs.getValue("angle"));
				if (attrs.getValue("order") != null)
					order = Integer.parseInt(attrs.getValue("order"));

				world.addIncarnator(location, angle, order);
			}
			else if (localName.equalsIgnoreCase("block")) {
				Vector3f center, size;
				ColorRGBA color;
				float yaw = Block.DEFAULT_YAW, pitch = Block.DEFAULT_PITCH, roll = Block.DEFAULT_ROLL, mass = 0.0f;
				boolean isHologram = false;

				center = (attrs.getValue("center") != null) ? parseVector(attrs.getValue("center")) : new Vector3f(0.0f, 0.0f, 0.0f);
				size = (attrs.getValue("size") != null) ? parseVector(attrs.getValue("size")) : Block.DEFAULT_SIZE;
				color = (attrs.getValue("color") != null) ? parseColor(attrs.getValue("color")) : new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f);

				if (attrs.getValue("opacity") != null)
					color.set(color.r, color.g, color.b, Float.parseFloat(attrs.getValue("opacity")));
				if (attrs.getValue("yaw") != null)
					yaw = World.compassToRadians(Float.parseFloat(attrs.getValue("yaw")));
				if (attrs.getValue("pitch") != null)
					pitch = Float.parseFloat(attrs.getValue("pitch")) * FastMath.DEG_TO_RAD;
				if (attrs.getValue("roll") != null)
					roll = Float.parseFloat(attrs.getValue("roll")) * FastMath.DEG_TO_RAD;
				if (attrs.getValue("mass") != null)
					mass = Float.parseFloat(attrs.getValue("mass"));
				if (attrs.getValue("hologram") != null)
					isHologram = parseBoolean(attrs.getValue("hologram"));

				Block block = new Block(world, center, size, color, yaw, pitch, roll, mass, isHologram);
				world.addWorldObject(block);
			}
			else if (localName.equalsIgnoreCase("ramp")) {
				Vector3f base, top;
				ColorRGBA color;
				float width = Ramp.DEFAULT_WIDTH;
				float thickness = Ramp.DEFAULT_THICKNESS;
				boolean isHologram = false;

				base = (attrs.getValue("base") != null) ? parseVector(attrs.getValue("base")) : new Vector3f(-2.0f, 0.0f, 0.0f);
				top = (attrs.getValue("top") != null) ? parseVector(attrs.getValue("top")) : new Vector3f(2.0f, 4.0f, 0.0f);
				color = (attrs.getValue("color") != null) ? parseColor(attrs.getValue("color")) : new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f);

				if (attrs.getValue("opacity") != null)
					color.set(color.r, color.g, color.b, Float.parseFloat(attrs.getValue("opacity")));
				if (attrs.getValue("width") != null)
					width = Float.parseFloat(attrs.getValue("width"));
				if (attrs.getValue("thickness") != null)
					thickness = Float.parseFloat(attrs.getValue("thickness"));
				if (attrs.getValue("hologram") != null)
					isHologram = parseBoolean(attrs.getValue("hologram"));

				Ramp ramp = new Ramp(world, base, top, width, thickness, color, isHologram);
				world.addWorldObject(ramp);
			}
			else if (localName.equalsIgnoreCase("dome")) {
				Vector3f center;
				ColorRGBA color;
				int planes = Domeoid.DEFAULT_PLANES, radialSamples = Domeoid.DEFAULT_RADIAL_SAMPLES;
				float radius = Domeoid.DEFAULT_RADIUS, yaw = Domeoid.DEFAULT_YAW, pitch = Domeoid.DEFAULT_PITCH, roll = Domeoid.DEFAULT_ROLL, mass = 0.0f;
				boolean outsideView = true, isHologram = false;

				center = (attrs.getValue("center") != null) ? parseVector(attrs.getValue("center")) : new Vector3f(0.0f, 0.0f, 0.0f);
				color = (attrs.getValue("color") != null) ? parseColor(attrs.getValue("color")) : new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f);

				if (attrs.getValue("opacity") != null)
					color.set(color.r, color.g, color.b, Float.parseFloat(attrs.getValue("opacity")));
				if (attrs.getValue("planes") != null)
					planes = Integer.parseInt(attrs.getValue("planes"));
				if (attrs.getValue("radialSamples") != null)
					radialSamples = Integer.parseInt(attrs.getValue("radialSamples"));
				if (attrs.getValue("radius") != null)
					radius = Float.parseFloat(attrs.getValue("radius"));
				if (attrs.getValue("outsideView") != null)
					outsideView = parseBoolean(attrs.getValue("outsideView"));
				if (attrs.getValue("yaw") != null)
					yaw = World.compassToRadians(Float.parseFloat(attrs.getValue("yaw")));
				if (attrs.getValue("pitch") != null)
					pitch = Float.parseFloat(attrs.getValue("pitch")) * FastMath.DEG_TO_RAD;
				if (attrs.getValue("roll") != null)
					roll = Float.parseFloat(attrs.getValue("roll")) * FastMath.DEG_TO_RAD;
				if (attrs.getValue("mass") != null)
					mass = Float.parseFloat(attrs.getValue("mass"));
				if (attrs.getValue("hologram") != null)
					isHologram = parseBoolean(attrs.getValue("hologram"));

				Domeoid dome = new Domeoid(world, center, planes, radialSamples, radius, outsideView, color, yaw, pitch, roll, mass, isHologram);
				world.addWorldObject(dome);
			}
			else if (localName.equalsIgnoreCase("goody")) {
				String type = attrs.getValue("type");
				Vector3f center = (attrs.getValue("center") != null) ? parseVector(attrs.getValue("center")) : new Vector3f(0.0f, 0.0f, 0.0f);
				float respawn = attrs.getValue("respawn") != null ? Float.parseFloat(attrs.getValue("respawn")) : 10.0f;

				if (type.equalsIgnoreCase("grenade")) {
					GrenadeGoody g = new GrenadeGoody(world, center, respawn);
					world.addWorldObject(g);
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			chars.append(ch, start, length);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			// If the map has no celestials, add the defaults.
			if (localName.equalsIgnoreCase("map")) {
				if (numLights == 0) {
					Celestial c1 = new Celestial(world, new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f), 0.4f, World.compassToRadians(20), 45 * FastMath.DEG_TO_RAD,
							1.0f, false);
					Celestial c2 = new Celestial(world, new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f), 0.3f, World.compassToRadians(200), 20 * FastMath.DEG_TO_RAD,
							1.0f, false);
					world.addWorldObject(c1);
					world.addWorldObject(c2);
				}
			}
			chars = new StringBuilder();
		}

		public static Vector3f parseVector(String s) {
			String parts[] = s.split(",");
			float x = Float.parseFloat(parts[0]);
			float y = Float.parseFloat(parts[1]);
			float z = Float.parseFloat(parts[2]);
			return new Vector3f(x, y, z);
		}

		public static ColorRGBA parseColor(String s) {
			String parts[] = s.split(",");
			float r = Float.parseFloat(parts[0]);
			float g = Float.parseFloat(parts[1]);
			float b = Float.parseFloat(parts[2]);
			return new ColorRGBA(r, g, b, 1);
		}

		public static boolean parseBoolean(String s) {
			if (s.equalsIgnoreCase("true") || s.equals("1") || s.equals("yes") || s.equalsIgnoreCase("t") || s.equals("y"))
				return true;
			if (s.equalsIgnoreCase("false") || s.equals("0") || s.equals("no") || s.equalsIgnoreCase("f") || s.equals("n"))
				return false;
			else
				return false;
		}
	}
}

