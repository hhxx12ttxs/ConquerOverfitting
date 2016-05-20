package aprs.mapbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import java.util.Map;

public class Data {
	private Map<Integer, MyNode> nodeSet = new HashMap<Integer, MyNode>();
	private Map<Integer, MyWay> waySet = new HashMap<Integer, MyWay>();
	private ArrayList<MyWay> wayList = new ArrayList<MyWay>();

	public MyNode getNode(int id) {
		return nodeSet.get(id);
	}

	public MyWay getWay(int id) {
		return waySet.get(id);
	}

	public Data() throws ParserConfigurationException, SAXException,
						 IOException {
		Converter.init_RenRou();
		read();
		System.err.println("data init finished");
	}

	private boolean equalNODE(char a1, char a2, char a3, char a4) {
		if (a1 == 'n' && a2 == 'o' && a3 == 'd' && a4 == 'e')
			return true;
		else
			return false;
	}

	private boolean equalWAY(char a1, char a2, char a3) {
		if (a1 == 'w' && a2 == 'a' && a3 == 'y')
			return true;
		else
			return false;
	}

	private boolean equalTAG(char a1, char a2, char a3) {
		if (a1 == 't' && a2 == 'a' && a3 == 'g')
			return true;
		else
			return false;
	}

	private boolean equalND(char a1, char a2) {
		if (a1 == 'n' && a2 == 'd')
			return true;
		else
			return false;
	}

	static int hh = 0, hh1 = 0;

	private void CHECK(String tmp) {
		++hh;
		// if (hh % 10000 == 0 || hh > 110000) System.out.println(hh + ":" + tmp
		// + ":");
	}

	private void CHECK1(String tmp) {
		++hh1;
		// if (true || hh1 % 10000 == 0 || hh1 > 110000) System.out.println(hh1
		// + ":" + tmp + ":");
	}
	private double getLength(double x1, double y1, double x2, double y2) {
	    double ret = Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)));  
	    return ret;
	}

	@SuppressWarnings("unused")
	private void read() throws ParserConfigurationException, SAXException,
							   IOException {
		File file = new File("mp.osm");
		/*
		 * DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
		 * DocumentBuilder builder=factory.newDocumentBuilder(); Document
		 * document=builder.parse(file); Element root =
		 * document.getDocumentElement(); NodeList //nodes =
		 * root.getElementsByTagName("node"), ways =
		 * root.getElementsByTagName("way");
		 */
		FileInputStream filein = new FileInputStream(file);
		FileInputStream filein1 = new FileInputStream(file);

		Scanner in = new Scanner(filein);
		String tmp;

		tmp = in.nextLine();
		System.err.println(tmp);
		CHECK(tmp);
		while (in.hasNextLine()) {
			if (tmp.length() >= 6
					&& equalNODE(tmp.charAt(2), tmp.charAt(3), tmp.charAt(4),
							tmp.charAt(5))) {
				int posq = 0, posh = 0;
				while (tmp.charAt(posq) != '\"')
					++posq;
				posh = ++posq;
				while (tmp.charAt(posh) != '\"')
					++posh;
				int id = Integer.parseInt(tmp.substring(posq, posh));

				posq = posh + 1;
				while (tmp.charAt(posq) != '\"')
					++posq;
				posh = ++posq;
				while (tmp.charAt(posh) != '\"')
					++posh;
				double lat = Double.parseDouble(tmp.substring(posq, posh));

				posq = posh + 1;
				while (tmp.charAt(posq) != '\"')
					++posq;
				posh = ++posq;
				while (tmp.charAt(posh) != '\"')
					++posh;
				double lon = Double.parseDouble(tmp.substring(posq, posh));

				ArrayList<String> tags = new ArrayList<String>();
				tags.clear();
				for (int i = 0; i < Converter.MAX_Tag; ++i)
					tags.add("");

				tmp = in.nextLine();
				CHECK(tmp);
				int times = 0;
				while (tmp.length() >= 6
						&& equalTAG(tmp.charAt(3), tmp.charAt(4), tmp.charAt(5))) {
					++times;
					String _K, _V;
					posq = 0;
					posh = 0;

					posq = posh + 1;
					while (tmp.charAt(posq) != '\"')
						++posq;
					posh = ++posq;
					while (tmp.charAt(posh) != '\"')
						++posh;

					_K = tmp.substring(posq, posh);

					posq = posh + 1;
					while (tmp.charAt(posq) != '\"')
						++posq;
					posh = ++posq;
					while (tmp.charAt(posh) != '\"')
						++posh;
					_V = tmp.substring(posq, posh);

					int _id = Converter.getItemNode(_K);
					if (_id >= 0) {
						tags.set(_id, _V);
					}
					tmp = in.nextLine();
					CHECK(tmp);
				}
				if (times != 0) {
					tmp = in.nextLine();
					CHECK(tmp);
				}

				int type = 0;

				if (tags.get(1).equals("hospital"))
					type = 1;
				if (tags.get(1).equals("school"))
					type = 2;
				if (tags.get(1).equals("hotel"))
					type = 3;
				if (tags.get(1).equals("bank"))
					type = 5;
				if (tags.get(1).equals("police"))
					type = 6;
				//if (!tags.get(2).equals("")) System.out.println(tags.get(2));
				MyNode ins = new MyNode(id, lon, -lat, tags.get(2), type);
				nodeSet.put(id, ins);

			} else {
				tmp = in.nextLine();
				CHECK(tmp);
			}
		}

		Scanner in1 = new Scanner(filein1);
		// System.out.println(nodeSet.size());
		// in1
		int hh2 = 0;
		tmp = in1.nextLine();
		CHECK1(tmp);
		while (in1.hasNextLine()) {
			if (tmp.length() >= 5
					&& equalWAY(tmp.charAt(2), tmp.charAt(3), tmp.charAt(4))) {
				int posq = 0, posh = 0;
				while (tmp.charAt(posq) != '\"')
					++posq;
				posh = ++posq;
				while (tmp.charAt(posh) != '\"')
					++posh;
				int id = Integer.parseInt(tmp.substring(posq, posh));

				ArrayList<MyNode> nodes = new ArrayList<MyNode>();
				nodes.clear();
				
				tmp = in1.nextLine();
				CHECK1(tmp);

				String name = "";
				int type = 0;
				int times = 0;
				while (tmp.length() >= 5
						&& (equalND(tmp.charAt(3), tmp.charAt(4)) || equalTAG(
								tmp.charAt(3), tmp.charAt(4), tmp.charAt(5)))) {
					if (equalND(tmp.charAt(3), tmp.charAt(4))) {

						posq = 0;
						posh = 0;
						while (tmp.charAt(posq) != '\"')
							++posq;
						posh = ++posq;
						while (tmp.charAt(posh) != '\"')
							++posh;
						int _id = Integer.parseInt(tmp.substring(posq, posh));
						nodes.add(nodeSet.get(_id));

						tmp = in1.nextLine();
						CHECK1(tmp);
					} else {

						posq = 0;
						posh = 0;
						while (tmp.charAt(posq) != '\"')
							++posq;
						posh = ++posq;
						while (tmp.charAt(posh) != '\"')
							++posh;
						String _K = tmp.substring(posq, posh);

						++posh;
						posq = posh;

						while (tmp.charAt(posq) != '\"')
							++posq;
						posh = ++posq;
						while (tmp.charAt(posh) != '\"')
							++posh;
						String _V = tmp.substring(posq, posh);

						if (_K.equals("highway"))
							type = 0;
						if (_K.equals("cycleway"))
							type = 1;
						if (_K.equals("bridge"))
							type = 2;
						if (_K.equals("railway"))
							type = 3;
						if (_K.equals("waterway"))
							type = 4;
						if (_K.equals("foot"))
							type = 5;
						if (_K.equals("oneway"))
							type = 6;
						if (_K.equals("bicycle"))
							type = 7;
						if (_K.equals("name"))
							name = _V;

						tmp = in1.nextLine();
						CHECK1(tmp);
					}
				}
				MyWay ins = new MyWay(id, nodes, name, type);
				double calc = 0;
				for (int i = 1; i < nodes.size(); ++i) {
					calc += getLength( nodes.get(i - 1).getLatitude(),  nodes.get(i - 1).getLongitude(),
									   nodes.get(i).getLatitude(),  nodes.get(i).getLongitude());
				}
				
				ins.wayLength = calc;
				waySet.put(id, ins);
				wayList.add(ins);
				++hh2;

			} else {
				tmp = in1.nextLine();
				CHECK1(tmp);
			}
		}
		//System.out.println(hh2);
		/*
		 * for (int j = 0; j < nodes.getLength(); ++j) { // for (int j = 0; j <
		 * 200; ++j) { if (j%1000==0) System.err.println("done "+j); Node _node
		 * = nodes.item(j); if (Node.ELEMENT_NODE == _node.getNodeType()) {
		 * Element zh = (Element)_node;
		 * 
		 * NodeList tag = zh.getElementsByTagName("tag"); int id =
		 * Integer.parseInt(zh.getAttribute("id")); double lon =
		 * Double.parseDouble(zh.getAttribute("lon")); double lat =
		 * Double.parseDouble(zh.getAttribute("lat")); ArrayList<String> tags =
		 * new ArrayList<String>(); tags.clear(); for (int i = 0; i <
		 * Converter.MAX_Tag; ++i) tags.add("");
		 * 
		 * for (int k = 0; k < tag.getLength(); ++k) { Node _tag = tag.item(k);
		 * if (Node.ELEMENT_NODE == _tag.getNodeType()) { Element zh1 =
		 * (Element)_tag; String content=zh1.getAttribute("k"); int i =
		 * Converter.getItemNode(content); if (i != -1) { tags.set(i,
		 * zh1.getAttribute("v")); } } }
		 * 
		 * //System.err.println(ins.getName() + ":" + ins.getLatitude() + ":" +
		 * lat);
		 * 
		 * 
		 * } }
		 */
		/*
		 * for (int j = 0; j < ways.getLength(); ++j) { Node _node =
		 * ways.item(j); if (Node.ELEMENT_NODE == _node.getNodeType()) { Element
		 * zh = (Element)_node; int id =
		 * Integer.parseInt(zh.getAttribute("id")); NodeList nf =
		 * zh.getElementsByTagName("nd"); NodeList tag =
		 * zh.getElementsByTagName("tag"); ArrayList<MyNode> _nodes = new
		 * ArrayList<MyNode>(); _nodes.clear();
		 * 
		 * ArrayList<String> tags = new ArrayList<String>(); tags.clear(); for
		 * (int i = 0; i < Converter.MAX_Tag; ++i) tags.add("");
		 * 
		 * 
		 * for (int k = 0; k < tag.getLength(); ++k) { Node _tag = tag.item(k);
		 * if (Node.ELEMENT_NODE == _tag.getNodeType()) { Element zh1 =
		 * (Element)_tag; String content=zh1.getAttribute("k"); int i =
		 * Converter.getItemNode(content); if (i != -1) { tags.set(i,
		 * zh1.getAttribute("v")); } } } for (int k = 0; k < nf.getLength();
		 * ++k) { Node _tag = tag.item(k); if (Node.ELEMENT_NODE ==
		 * _tag.getNodeType()) { Element zh1 = (Element)_tag; String
		 * content=zh1.getAttribute("ref"); int t = Integer.parseInt(content);
		 * _nodes.add(nodeSet.get(t));
		 * 
		 * } } MyWay ins = new MyWay(id, _nodes, tags); waySet.put(id, ins);
		 * 
		 * } }
		 */
		// sort waynode
		Collections.sort(wayList, new Comparator<MyWay>() {

			@Override
			public int compare(MyWay o1, MyWay o2) {
				// -1
/*				if (o1.get_nodes().size() > o2.get_nodes().size())
					return -1;
				if (o1.get_nodes().size() < o2.get_nodes().size())
					return 1;*/
				
				if (o1.wayLength > o2.wayLength)
					return -1;
				if (o1.wayLength < o2.wayLength)
					return 1;
				return 0;
			}

		});

	}

	public ArrayList<MyNode> findNode(double maxlon, double minlon,
			double maxlat, double minlat) {
		ArrayList<MyNode> array = new ArrayList<MyNode>();
		array.clear();
		// nodeSet
		Collection<MyNode> tmp = nodeSet.values();
		Iterator<MyNode> iter = tmp.iterator();
		for (; iter.hasNext();) {
			MyNode opt = iter.next();
			if (opt.getLongitude() >= minlon && opt.getLongitude() <= maxlon
					&& opt.getLatitude() >= minlat
					&& opt.getLatitude() <= maxlat) {
				array.add(opt);
				if (!opt.getName().equals(""))
					System.err.println("check opt" + opt.getName());
			}
		}
		return array;
	}

	public ArrayList<MyNode> markNode(String search) {
		ArrayList<MyNode> array = new ArrayList<MyNode>();
		array.clear();
		// nodeSet
		Collection<MyNode> tmp = nodeSet.values();
		Iterator<MyNode> iter = tmp.iterator();
		for (; iter.hasNext();) {
			MyNode opt = iter.next();
			if (opt.getName().contains(search)) {
				array.add(opt);
			}
		}
		return array;
	}

	public ArrayList<MyNode> markWay(String search) {
		ArrayList<MyWay> array = new ArrayList<MyWay>();
		ArrayList<MyNode> ret = new ArrayList<MyNode>();  
		array.clear();
		ret.clear();
		// nodeSet
		Collection<MyWay> tmp = waySet.values();
		Iterator<MyWay> iter = tmp.iterator();
		for (; iter.hasNext();) {
			MyWay opt = iter.next();
			if (opt.getName().contains(search)) {
				array.add(opt);
			}
		}
		for (int j = 0; j < array.size(); ++j) {
			ret.add(array.get(j).get_nodes().get(0));
		}
		
		return ret;
	}
	
	
	private double dot(double x1, double y1, double x2, double y2) {
		double ret = (x1 * y2 - y1 * x2);
		return ret;
	}

	private int sgn(double x) {
		if (x > 0)
			return 1;
		if (x < 0)
			return -1;
		return 0;
	}

	private int decideSide(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		return (sgn(dot(x3 - x4, y3 - y4, x1 - x4, y1 - y4)) * sgn(dot(x3 - x4,
				y3 - y4, x2 - x4, y2 - y4)));
	}

	private boolean checkIntersect(double x1, double y1, double x2, double y2,
			double x3, double y3, double x4, double y4) {
		return (decideSide(x1, y1, x2, y2, x3, y3, x4, y4) < 0 && decideSide(
				x3, y3, x4, y4, x1, y1, x2, y2) < 0);

	}

	public ArrayList<MyWay> findWay(double maxlon, double minlon,
			double maxlat, double minlat) {
		ArrayList<MyWay> array = new ArrayList<MyWay>();

		array.clear();
		// nodeSet
		// Collection<MyWay> tmp = waySet.values();

		Iterator<MyWay> iter = wayList.iterator();

		for (; iter.hasNext();) {
			MyWay opt = (MyWay) iter.next();
			int flag = 0;

			for (int i = 0; i < opt.get_nodes().size() - 1; ++i) {
				MyNode st = opt.get_nodes().get(i);
				MyNode en = opt.get_nodes().get(i + 1);
				if (st.getLongitude() >= minlon && st.getLongitude() <= maxlon
						&& st.getLatitude() >= minlat
						&& st.getLatitude() <= maxlat) {
					flag = 1;
					break;
				}
				if (en.getLongitude() >= minlon && en.getLongitude() <= maxlon
						&& en.getLatitude() >= minlat
						&& en.getLatitude() <= maxlat) {
					flag = 1;
					break;
				}
				// (st.minlon, st.minlat)
				// (en.maxlon, en.maxlat)
				if (checkIntersect(minlon, minlat, minlon, maxlat, st
						.getLongitude(), st.getLatitude(), en.getLongitude(),
						en.getLatitude())
						|| checkIntersect(minlon, minlat, maxlon, minlat, st
								.getLongitude(), st.getLatitude(), en
								.getLongitude(), en.getLatitude())
						|| checkIntersect(maxlon, maxlat, minlon, maxlat, st
								.getLongitude(), st.getLatitude(), en
								.getLongitude(), en.getLatitude())
						|| checkIntersect(maxlon, maxlat, maxlon, minlat, st
								.getLongitude(), st.getLatitude(), en
								.getLongitude(), en.getLatitude())) {
					flag = 1;
					break;
				}
			}

			if (flag == 1)
				array.add(opt);

		}
		return array;

	}
}

