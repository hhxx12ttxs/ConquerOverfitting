package hakd.game;

import hakd.Hakd;
import hakd.fxgui.FxGameGui;

import java.util.ArrayList;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import ai.pathfinder.Connector;
import ai.pathfinder.Node;
import ai.pathfinder.Pathfinder;

public class Map {
	private static ArrayList<Server>	servers		= new ArrayList<Server>();
	private static ArrayList<Node>		pathNodes	= new ArrayList<Node>();
	private static Server				flag1;
	private static Server				flag2;

	// creates connections between the servers
	@SuppressWarnings("unchecked")
	public static void createConnections() { // TODO make this reusable for awt
		ArrayList<Line> lines = new ArrayList<Line>();
		for (Server server : servers) { // for all servers in the game
			ArrayList<Server> connections = new ArrayList<Server>();
			int x1 = server.getxCoord();
			int y1 = server.getyCoord();

			for (Server s : servers) {
				int x2 = s.getxCoord();
				int y2 = s.getyCoord();
				if (!s.equals(server) && Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)) <= Hakd.getMaxr()) {
					connections.add(s);

					server.getNode().links.add(new Connector(s.getNode(), 1));

					if (!s.getConnections().contains(server)) {
						if (Hakd.isJava7()) {
							Line l = line(x1, y1, x2, y2);
							if (l != null) {
								l.setStrokeWidth(1.15);
								l.setStroke(Paint.valueOf("green"));
								lines.add(l);
							}
						} else {
							// awt stuff
						}
					}
				}
			}
			server.getConnections().addAll(0, connections);
		}
		if (Hakd.isJava7()) {
			FxGameGui.getNodes().addAll(lines);
			FxGameGui.update();
		} else {

		}
	}

	// return the best server from a servers connections
	public static Server pathFind(Server s1, Server s2) {
		Pathfinder p = new Pathfinder();
		p.addNodes(pathNodes);
		Node n = (Node) p.aStar(s1.getNode(), s2.getNode()).get(p.aStar(s1.getNode(), s2.getNode()).size() - 2); // search for the next best node
		for (Server s : s1.getConnections()) { // search for the server with that node
			if (s.getNode() == n) {
				return s;
			}
		}
		return null; // this /*will*/ should never happen
	}

	// finds the points on the out side of the circle instead of the center
	private static Line line(int x1, int y1, int x2, int y2) { // lack of a better name
		double r, a, b, c, xTrig, yTrig; // triangle>ABC
		r = Server.getRadius();

		a = 1; // line BC, point C is only (x2,y2+1)
		b = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 + 1 - y1) * (y2 + 1 - y1)); // line CA
		c = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)); // line AB

		Line line = new Line();
		if (x2 - x1 != 0) {
			xTrig = r * Math.sin(Math.acos((a * a - b * b + c * c) / (2 * a * c)));
			yTrig = r * Math.cos(Math.acos((a * a - b * b + c * c) / (2 * a * c)));
			if (x2 - x1 > 0) {
				line.setStartX(x1 + xTrig);
				line.setStartY(y1 - yTrig);
				line.setEndX(x2 - xTrig);
				line.setEndY(y2 + yTrig);
			} else if (x2 - x1 < 0) {
				line.setStartX(x1 - xTrig);
				line.setStartY(y1 - yTrig);
				line.setEndX(x2 + xTrig);
				line.setEndY(y2 + yTrig);
			}
		} else {
			if (y2 - y1 > 0) {
				line.setEndX(x2);
				line.setEndY(y2 + r);
			} else if (y2 - y1 < 0) {
				line.setEndX(x2);
				line.setEndY(y2 - r);
			} else {
				return null;
			}
			return null;
		}
		if (Double.isNaN(yTrig) || Double.isNaN(xTrig)) {
			return null;
		}
		return line;
		// this may be way too much work for such a little effect, although the lines do block the tool tip
		// plus I just copied this from Hak'd
	}

	public static ArrayList<Server> getServers() {
		return servers;
	}

	static public void setServers(ArrayList<Server> servers) {
		Map.servers = servers;
	}

	public static Server getFlag1() {
		return flag1;
	}

	public static void setFlag1(Server flag1) {
		Map.flag1 = flag1;
	}

	public static Server getFlag2() {
		return flag2;
	}

	public static void setFlag2(Server flag2) {
		Map.flag2 = flag2;
	}

	public static ArrayList<Node> getPathNodes() {
		return pathNodes;
	}

	public static void setPathNodes(ArrayList<Node> pathNodes) {
		Map.pathNodes = pathNodes;
	}
}

