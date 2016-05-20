package javacommon.algorithm.astar.demo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
/** log4j-1.2.15.jar */
import org.apache.log4j.Logger;
/** swt-3.4.2-win32-win32-x86.zip */
import org.eclipse.swt.graphics.Point;

import javacommon.algorithm.astar.demo.Map;
import javacommon.algorithm.astar.demo.struct.PriorityBuffer;

/**
 * A*
 * 
 * @author zhaoming23@gmail.com <br/> 2010-1-1 ??11:08:30
 */
public class AStarFinder {

	private static final Logger LOG = Logger.getLogger(AStarFinder.class);

	private/* OpenList */PriorityBuffer _openList;

	private/* LinkedList */PriorityBuffer _closeList;

	/** ?????????? */
	private int[][] _map;

	/** ?????????? */
	private int[] _limit;

	/** ???? */
	public int index;

	/** ???? */
	public static long time;
	
	private Map map;

	public AStarFinder(int[][] map, int[] limit, Map map1) {
		_map = map;
		this.map = map1;
		_limit = limit;
		_openList = new PriorityBuffer() /* OpenList() */;
		_closeList = new PriorityBuffer() /* LinkedList() */;
	}

	/** ????????????? */
	private java.util.Map<Node, Integer> nodeValueMap = new HashMap<Node, Integer>();

	/** A* find path function */
	public List<Node> searchPath(Point startPos, Point objectPos, boolean is8Direction,
			Map map) {
		index = 0;
		Node startNode = new Node(startPos);
		Node objectNode = new Node(objectPos);

		// ????????
		startNode._costFromStart = 0;
		startNode._costToObject = startNode.getCost(objectNode);
		startNode._parentNode = null;

		// ??????
		_openList.add(startNode);

		// ?????????????,??List????
		long startTime = System.nanoTime();
		while (_openList.isEmpty() == false) {
			// ?????????
			Node firstNode = (Node) _openList./* removeFirst() */remove();
			if (firstNode.equals(objectNode)) {
				// ????????
				LOG.info("A* cipher count:" + index);
				long endTime = System.nanoTime();
				time = endTime - startTime;
				LOG.info("A* spent nanoTime:" + time);
				return createPath(firstNode);
			} else {
				_closeList.add(firstNode);
				LinkedList _limit = firstNode.getLimit(is8Direction, map);

				int limitSize = _limit.size();
				for (int i = 0; i < limitSize; i++) {
					Node neighbourNode = (Node) _limit.get(i);
					boolean isOpen = _openList.contains(neighbourNode);
					boolean isClosed = _closeList.contains(neighbourNode);
					boolean isHit = isHit(neighbourNode);
					if (isOpen == false && isClosed == false && isHit == false) {
						neighbourNode._costFromStart = firstNode._costFromStart
								+ Node.calcCost(firstNode, neighbourNode);
						neighbourNode._costToObject = neighbourNode
								.getCost(objectNode);
						neighbourNode._parentNode = firstNode;
						_openList.add(neighbourNode);
						nodeValueMap.put(neighbourNode, neighbourNode._costFromStart);
					}
				}
			}
			index++;
		}
		_closeList.clear();
		return null;
	}

	/**
	 * ??????????
	 * 
	 * @param node
	 *            ????
	 * @return ???????????
	 */
	private List<Node> createPath(Node node) {
		LinkedList<Node> path = new LinkedList<Node>();
		while (node._parentNode != null) {
			path.addFirst(node);
			node = node._parentNode;
		}
		path.addFirst(node);
		// ????????, ????????????
		path.removeLast();
		path.removeFirst();
		return path;
	}

	public java.util.Map<Node, Integer> getNodeValueMap() {
		return nodeValueMap;
	}
	
	/**
	 * ???????
	 * @param list ???????????
	 * @return ??????????
	 */
	public List<Node> getSmoothPath(List<Node> list) {
		List<Node> smoothList = new ArrayList<Node>();
		if(null == list){
			return null;
		}
		int loopSize = list.size();
		for (int i = 1; i < loopSize - 1; i += 2) {
			int tempIndex = i;
			Node node1 = list.get(i - 1);
			Node node2 = list.get(tempIndex);
			Node node3 = list.get(++tempIndex);
			
			if(node2 != null && node3 != null){
				LinkedList limit = node1.getLimit(true, map);
				if(limit.contains(node2)
					&& limit.contains(node3)){
					
					smoothList.add(node1);
					smoothList.add(node3);
					continue;
				}
			}
			smoothList.add(node1);
			smoothList.add(node2);
			smoothList.add(node3);
			//???????????????
			smoothList.add(list.get(loopSize - 1));
		}
		return smoothList;
	}

	/**
	 * ??????(?????)
	 * 
	 * @param node
	 *            ??
	 * @return
	 */
	private boolean isHit(Node node) {
		if (_limit != null) {
			for (int i = 0; i < _limit.length; i++) {
				if (_map[node._pos.x][node._pos.y] == _limit[i]) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ????, ??????????????? PriorityBuffer(2008-04-10) ? LinkedList ??
	 * 
	 * @author zhaoming23@gmail.com 2010-1-4 - ??02:09:27
	 */
	/*
	 * private class OpenList extends LinkedList {
	 * 
	 *//**
		 * serialVersionUID
		 */
	/*
	 * private static final long serialVersionUID = -5947307586912421545L;
	 * 
	 *//**
		 * ??????
		 * 
		 * @param node
		 */
	/*
	 * public void add(Node node) { int size = size(); for (int i = 0; i < size;
	 * i++) { // ??node#compareTo function <= ??? (???????) if
	 * (node.compareTo(get(i)) <= 0) { add(i, node); return; } } //????????????
	 * addLast(node); } }
	 */
}

