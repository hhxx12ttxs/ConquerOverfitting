package artemis.vide.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import util.ApplicationProperties;
import util.Constants;
import webservice.Attachment;
import webservice.Component;
import webservice.Connector;
import webservice.Diagram;
import webservice.Location;
import webservice.Service;
import artemis.vide.models.ArtemisDiagram;
import artemis.vide.models.ComponentModel;
import artemis.vide.models.ComponentService;
import artemis.vide.models.ConnectorModel;
import artemis.vide.models.RoleModel;
import artemis.vide.models.connectorModels.BasicConnectorModel;
import artemis.vide.models.connectorModels.ConnectorContentsModel;
import artemis.vide.models.connectorModels.ConnectorRoleModel;
import artemis.vide.models.connectorModels.VirtualParentConnectorModel;

import common.vide.model.BaseNode;
import common.vide.model.IEdge;

public class ArtemisHelper {

	public static boolean showOnce = false;

	public static Point caculateLocation(Point currentLoc, Dimension childSize,
			Point t_location, Dimension t_size) {
		Point location = new Point();
		int px = currentLoc.x;
		int py = currentLoc.y;
		int dx = px - t_location.x;
		int dy = py - t_location.y;
		location.x = px;
		location.y = py;
		double centerLocation_x = px + childSize.width / 2;
		double centerLocation_y = py + childSize.height / 2;
		int parentCenterLocation_x = t_location.x + t_size.width / 2;
		int parentCenterLocation_y = t_location.y + t_size.height / 2;
		double num = centerLocation_y - parentCenterLocation_y;
		double num1 = (childSize.width + t_size.width) / 2;
		if (dx < t_size.width / 2)
			location.x = (int) (parentCenterLocation_x
					- Math.sqrt(num1 * num1 - num * num) - childSize.width / 2);
		else
			location.x = (int) (parentCenterLocation_x
					+ Math.sqrt(num1 * num1 - num * num) - childSize.width / 2);
		return location;
	}

	public static int caculateDegree(int roleNum, int index,
			Dimension childSize, Dimension parentSize) {
		int offset = 0;
		double degree = Math.PI / roleNum * index;
		double centerDegree = Math.PI / 2;
		if (degree < centerDegree) {
			offset = (int) (parentSize.height / 2
					- (childSize.width + parentSize.width) / 2
					* Math.cos(degree) + childSize.width / 2);
		} else {
			offset = (int) (parentSize.height / 2
					+ (childSize.width + parentSize.width) / 2
					* Math.cos(Math.PI - degree) + childSize.width / 2);
		}
		return offset;
	}

	public static boolean canReduce(ConnectorContentsModel parent,
			Point oldLoc, Point newLoc, Dimension virSize) {
		if (parent != null && parent.getChildren() != null
				&& parent.getChildren().size() > 1) {
			List children = parent.getChildren();
			int dx = newLoc.x - oldLoc.x;
			int dy = newLoc.y - oldLoc.y;
			for (Object child : children) {
				if (child instanceof BasicConnectorModel) {
					if (!canDo(((BasicConnectorModel) child).getLocation()
							.getCopy().translate(dx, dy), newLoc, virSize))
						return false;
				}
			}
		}
		return true;
	}

	// ???BasicConnectorModel?????????????????
	public static boolean canDo(Point loc, Point ploc, Dimension psize) {

		int lenth1 = (new BasicConnectorModel()).getSize().width;
		int lenth2 = (new ConnectorRoleModel()).getSize().width;
		int lenth = lenth1 + lenth2;

		Point pCenterLoc = ploc.getCopy().translate(psize.width / 2,
				psize.height / 2);
		Point centerLoc = loc.getCopy().translate(lenth1 / 2, lenth1 / 2);

		int dx = Math.abs(centerLoc.x - pCenterLoc.x) + lenth2 / 2;
		int dy = Math.abs(centerLoc.y - pCenterLoc.y) + lenth2 / 2;
		int distance = (int) Math.sqrt(dx * dx + dy * dy);
		if (distance < psize.width / 2 - lenth1 / 2 - lenth2)
			return true;
		else
			return false;
	}

	// ??????role?????
	public static void locateRoles(BaseNode connector) {
		Dimension virtualSize = connector.getSize();
		// ??????????
		Point circleCenterLoc = new Point(connector.getLocation().x
				+ virtualSize.width / 2, connector.getLocation().y
				+ virtualSize.width / 2);
		Dimension roleSize = new Dimension(15, 15);
		List<ConnectorRoleModel> connRoles = null;
		if (connector instanceof VirtualParentConnectorModel)
			connRoles = ((VirtualParentConnectorModel) connector).getRoles();
		else if (connector instanceof BasicConnectorModel)
			connRoles = ((BasicConnectorModel) connector).getRoles();
		if (connRoles.size() < 1)
			return;

		// System.out.println(circleCenterLoc);
		int calleeNum = connRoles.size() - 1;
		int calleeCount = 0;
		// ??????caller????callee
		if (calleeNum == 0) {
			ConnectorRoleModel connRole = connRoles.get(0);
			if (connRole.isCaller()) {
				Point location = new Point(circleCenterLoc.x
						- virtualSize.width / 2 - roleSize.width,
						circleCenterLoc.y - roleSize.height / 2);
				connRole.setLocation(location);
			} else {
				Point location = new Point(circleCenterLoc.x
						+ virtualSize.width / 2 - roleSize.width,
						circleCenterLoc.y - roleSize.height / 2);
				connRole.setLocation(location);
			}
			return;
		}
		int averageAngle = 60 / calleeNum;
		int startAngle = 0;

		if (calleeNum % 2 == 0)
			startAngle = 60;
		else
			startAngle = 90 - averageAngle * ((calleeNum - 1) / 2);
		for (ConnectorRoleModel connRole : connRoles) {

			if (connRole.isCaller()) {

				Point location = new Point(circleCenterLoc.x
						- virtualSize.width / 2 - roleSize.width,
						circleCenterLoc.y - roleSize.height / 2);
				connRole.setLocation(location);

			} else {

				double currentRadian = Math.PI
						* (startAngle + averageAngle * calleeCount) / 180;
				Point location = new Point();
				int length = virtualSize.width / 2 + roleSize.width / 2;

				location.x = circleCenterLoc.x
						+ (int) (length * Math.sin(currentRadian))
						- roleSize.width / 2;
				location.y = circleCenterLoc.y
						- (int) (length * Math.cos(currentRadian))
						- roleSize.width / 2;
				connRole.setLocation(location);

				if (calleeNum % 2 == 0 && calleeCount == (calleeNum / 2 - 1))
					calleeCount += 2;
				else
					calleeCount++;

			}

		}
	}

	// yield diagram model for webservice
	public static Diagram transformModel(ArtemisDiagram contents) {
		Diagram diagram = new Diagram();
		List<BaseNode> nodes = contents.getChildren();
		List<RoleModel> roles = contents.getRoles();
		ArrayList<Connector> connList = new ArrayList<Connector>();
		ArrayList<Component> compList = new ArrayList<Component>();
		ArrayList<Attachment> attachList = new ArrayList<Attachment>();
		HashMap<BaseNode, Object> map = new HashMap<BaseNode, Object>();
		for (BaseNode model : nodes) {
			if (model instanceof ConnectorModel) {
				ConnectorModel connModel = (ConnectorModel) model;
				Connector conn = new Connector(connModel.getName());
				conn.setLocation(new Location(connModel.getLocation().x,
						connModel.getLocation().y));
				connList.add(conn);
				map.put(connModel, conn);
			} else if (model instanceof ComponentModel) {
				ComponentModel compModel = (ComponentModel) model;
				Component comp = new Component(compModel.getName());
				comp.setLocation(new Location(compModel.getLocation().x,
						compModel.getLocation().y));
				if (compModel.isCompound()) {
					comp.setStyle("compound");

					Diagram compDiagram = transformModel(compModel);
					comp.setDiagram(compDiagram);
				} else
					comp.setStyle("atomic");

				if (compModel.getBinding() == null) {
					comp.setState("unbind");
				} else if (compModel.getBinding() != null) {
					comp.setState("binded");
					ComponentService csModel = compModel.getBinding();

					Service service = new Service((String) csModel
							.getArtemisProperty().get("comp_name"), csModel
							.getId());
					comp.setService(service);
				}
				compList.add(comp);
				map.put(compModel, comp);
			}

		}
		// ????l????
		for (RoleModel role : roles) {
			Attachment attachment = new Attachment();
			attachment.setSource((Component) map.get(role.getTarget()
					.getComponent()));
			attachment.setTarget((Connector) map.get(role.getSource()));
			attachList.add(attachment);

		}
		Component comps[] = new Component[compList.size()];
		compList.toArray(comps);
		diagram.setComps(comps);
		Connector conns[] = new Connector[connList.size()];
		connList.toArray(conns);
		diagram.setConns(conns);
		Attachment[] attachs = new Attachment[attachList.size()];
		attachList.toArray(attachs);
		diagram.setAttachs(attachs);
		compList = null;
		connList = null;
		attachList = null;
		return diagram;
	}

	public static void replaceContents(ArtemisDiagram contents,
			ArtemisDiagram newContents) {
		List<BaseNode> children = contents.getChildren();
		List<BaseNode> newChildren = newContents.getChildren();

		List<BaseNode> tempNodes = new ArrayList<BaseNode>();
		tempNodes.addAll(children);
		// remove no-exist nodes in new contents
		for (BaseNode node : tempNodes) {
			if (!newChildren.contains(node))
				contents.removeNode(node);
		}
		for (BaseNode node : newChildren) {
			node.setParent(contents);
			contents.addNode(node);
		}

		tempNodes.clear();
		List<IEdge> edges = contents.getEdges();
		List<IEdge> newEdges = newContents.getEdges();

		List<IEdge> tempEdges = new ArrayList<IEdge>();
		tempEdges.addAll(edges);
		// remove no-exist edges in new contents
		for (IEdge edge : tempEdges) {
			if (!newEdges.contains(edge)) {
				edge.detachSource();
				edge.detachTarget();
				contents.removeEdge(edge);
			}
		}
		tempEdges.clear();
		for (IEdge edge : newEdges) {
			if (!edges.contains(edge)) {
				edge.attachSource();
				edge.attachTarget();
				contents.addEdge(edge);
			}
		}

	}
}

