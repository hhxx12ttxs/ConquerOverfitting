package cl.alejo.jcsim.csim.circuit;

/**
 * 
 * jcsim
 * 
 * Created on Jul 17, 2004
 * 
 * This program is distributed under the terms of the GNU General Public License
 * The license is included in license.txt
 * 
 * @author: Alejandro Vera
 *  
 */
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cl.alejo.jcsim.csim.dom.Gate;

abstract public class Protoboard implements java.io.Serializable {

	protected Matrix _matrix = new Matrix();

	static final int EAST = 0;
	static final int NORTH = 1;
	static final int WEST = 2;
	static final int SOUTH = 3;

	public Protoboard() {
		super();
	}

	/**
	 * Este metodo agrega un pin a un proto, en la pocision X, Y
	 */
	public abstract void addPin(byte pinId, Gate gate, int x, int y);

	/**
	 * Devuelve en que direccion esta ctt1 con respecto a ctt2 Creation date:
	 * (07/09/96 01:10:18 a.m.)
	 * 
	 * @return int
	 * @param ctt1
	 *            newgui.Contact
	 * @param ctt2
	 *            newgui.Contact
	 */
	public int address(Contact contact1, Contact contact2) {
		if (contact2._x > contact1._x)
			return WEST;
		if (contact2._x < contact1._x)
			return EAST;
		if (contact2._y < contact1._y)
			return NORTH;
		return SOUTH;
	}

	/**
	 * Insert the method's description here. Creation date: (14/09/96 08:27:41
	 * p.m.)
	 * 
	 * @param ctt
	 *            newgui.Contact
	 * @param cttList
	 *            java.util.List
	 */
	abstract protected void attachContacts(Contact ctt, List listCtt);

	/**
	 * Este metodo es una mascara a metodo que conecta contactos... Creation
	 * date: (07/09/96 01:14:55 a.m.)
	 * 
	 * @param xi
	 *            int X inicial
	 * @param yi
	 *            int Y inicial
	 * @param xf
	 *            int X final
	 * @param yf
	 *            int Y final
	 */
	public void connect(int xi, int yi, int xf, int yf) {
		if (xi == xf && yi == yf)
			return;

		if (xi == xf)
			connectVertical(poke(xi, yi), poke(xf, yf));
		if (yi == yf)
			connectHorizontal(poke(xi, yi), poke(xf, yf));
	}

	/**
	 * Una mascara para conectar dos puntos en l Creation date: (07/09/96
	 * 01:14:11 a.m.)
	 * 
	 * @param xi
	 *            int
	 * @param yi
	 *            int
	 * @param xm
	 *            int
	 * @param ym
	 *            int
	 * @param xf
	 *            int
	 * @param yf
	 *            int
	 */
	public void connect(int xi, int yi, int xm, int ym, int xf, int yf) {
		connect(xi, yi, xm, ym);
		connect(xm, ym, xf, yf);
	}

	/**
	 * Este metodo conecta 2 contactos que se asume que existen Creation date:
	 * (07/09/96 01:14:55 a.m.)
	 * 
	 * @param ctt1
	 *            Contact es el primer contacto
	 * @param ctt2
	 *            Contact es el segundo contacto
	 */
	protected void connectHorizontal(Contact contact1, Contact contact2) {
		Contact contactA, contactB;

		if (contact1 == null || contact2 == null)
			return;

		if (contact1._x < contact2._x) {
			contactA = contact1;
			contactB = contact2;
		} else {
			contactA = contact2;
			contactB = contact1;
		}

		setConnect(contactA, EAST);
		setConnect(contactB, WEST);

		List testContactList = reconectInnerPinsHorizontaly(contactA, contactB);
		testContactList.add(contactA);
		testContactList.add(contactB);
		testList(testContactList);
	}

	private List reconectInnerPinsHorizontaly(Contact contactA, Contact contactB) {
		List testContactList = _matrix.getPointList(contactA, contactB);

		for (Iterator i = testContactList.iterator(); i.hasNext();) {
			Contact ctt = (Contact) i.next();
			setConnect(ctt, EAST);
			setConnect(ctt, WEST);
		}

		reconnect(findAttachedContacts(contactA));
		return testContactList;
	}

	/**
	 * Este metodo es una mascara a metodo que conecta contactos... Creation
	 * date: (07/09/96 01:14:55 a.m.)
	 * 
	 * @param ctt1
	 *            Contact es el primer contacto
	 * @param ctt2
	 *            Contact es el segundo contacto
	 */
	protected void connectVertical(Contact ctt1, Contact ctt2) {
		Contact ctta, cttb;

		if (ctt1 == null || ctt2 == null)
			return;

		if (ctt1._y < ctt2._y) {
			ctta = ctt1;
			cttb = ctt2;
		} else {
			ctta = ctt2;
			cttb = ctt1;
		}

		setConnect(ctta, NORTH);
		setConnect(cttb, SOUTH);

		List contactList = reconnectInnerPinsVerticaly(ctta, cttb);

		contactList.add(ctta);
		contactList.add(cttb);
		testList(contactList);
	}

	private void testList(List contactList) {
		for (Iterator i = contactList.iterator(); i.hasNext();) {
			Contact contact = (Contact) i.next();
			testContact(contact);
		}
	}

	private List reconnectInnerPinsVerticaly(Contact ctta, Contact cttb) {
		List contactList = _matrix.getPointList(ctta, cttb);

		for (Iterator i = contactList.iterator(); i.hasNext();) {
			Contact ctt = (Contact) i.next();
			setConnect(ctt, NORTH);
			setConnect(ctt, SOUTH);
		}

		List cttAttachedList = findAttachedContacts(ctta);
		reconnect(cttAttachedList);
		return contactList;
	}

	/**
	 * Borramos un contacto inutil de la matriz Creation date: (14/09/96
	 * 08:03:35 p.m.)
	 * 
	 * @param ctt
	 *            newgui.Contact
	 */
	protected void deleteContact(Contact ctt) {
		_matrix.remove(ctt);
	}

	/**
	 * Insert the method's description here. Creation date: (29/09/00 01:08:01
	 * a.m.)
	 * 
	 * @param ctt1
	 *            csimgui.Contact
	 * @param ctt2
	 *            csimgui.Contact
	 */
	public void disconnect(int x, int y) {

		int resultH = _matrix.findHorizontal(x, y);
		Contact hit = (Contact) _matrix.hit();
		Contact previous = (Contact) _matrix.previous();
		Contact next = (Contact) _matrix.next();

		switch (resultH) {
		case Matrix.EXIST:
			if (isConnected(hit, EAST) || isConnected(hit, WEST)) {
				disconnectHorizontal(previous, hit, next);
				return;
			}
			break;
		case Matrix.BETWEEN:
			if (isConnected(previous, EAST)) {
				disconnect(previous, next);
				return;
			}
		}

		int resultV = _matrix.findVertical(x, y);
		hit = (Contact) _matrix.hit();
		previous = (Contact) _matrix.previous();
		next = (Contact) _matrix.next();

		// Veamos si habia algo
		switch (resultV) {
		case Matrix.EXIST:
			if (isConnected(hit, NORTH) || isConnected(hit, SOUTH))
				disconnectV(previous, hit, next);
			return;
		case Matrix.BETWEEN:
			if (isConnected(previous, NORTH)) {
				disconnect(previous, next);
				return;
			}
		}
	}

	/**
	 * Desconecta dos contactos en la misma linea Creation date: (01/01/01
	 * 14:51:06)
	 * 
	 * @param ctt1
	 *            circuit.Contact
	 * @param ctt2
	 *            circuit.Contact
	 */
	public void disconnect(Contact contact1, Contact contact2) {

		int dir = address(contact1, contact2);
		int dirCtt1, dirCtt2;

		switch (dir) {
		case WEST:
			dirCtt1 = EAST;
			dirCtt2 = WEST;
			break;
		case NORTH:
			dirCtt1 = SOUTH;
			dirCtt2 = NORTH;
			break;
		case SOUTH:
			dirCtt1 = NORTH;
			dirCtt2 = SOUTH;
			break;
		default:
			dirCtt1 = WEST;
			dirCtt2 = EAST;
			break;
		}

		setDisconnect(contact1, dirCtt1);
		setDisconnect(contact2, dirCtt2);

		reconnect(contact1);
		reconnect(contact2);

		testContact(contact1);
		testContact(contact2);

		return;
	}

	/**
	 * Desconecto el contacto <code>contact</code> y reconectamos
	 * <code>contactNext</code> y <code>contactPrevious</code> Creation date:
	 * (19/01/01 15:29:16)
	 * 
	 * @param ctt
	 *            circuit.Contact
	 */
	public void disconnectHorizontal(Contact contactPrevious, Contact contact, Contact contactNext) {
		if (isConnected(contact, WEST)) {
			disconect(contactPrevious, contact, WEST, EAST);
		}
		if (isConnected(contact, EAST)) {
			disconect(contactNext, contact, EAST, WEST);
		}

		reconnect(contact);
		testContact(contact);

		if (isConnected(contact, WEST) && isConnected(contact, EAST))
			connectHorizontal(contactPrevious, contactNext);
	}

	/**
	 * Desconecto el contacto <code>contact</code> y reconectamos
	 * <code>contactNext</code> Creation date: (19/01/01 15:29:16)
	 * 
	 * @param ctt
	 *            circuit.Contact
	 */
	public void disconnectV(Contact contactPrevious, Contact contact, Contact contactNext) {
		if (isConnected(contact, SOUTH)) {
			disconect(contactPrevious, contact, SOUTH, NORTH);
		}
		if (isConnected(contact, NORTH)) {
			disconect(contactNext, contact, NORTH, SOUTH);
		}

		reconnect(contact);
		testContact(contact);

		if (isConnected(contact, SOUTH) && isConnected(contact, NORTH))
			connectVertical(contactPrevious, contactNext);
	}

	private void disconect(Contact contact, Contact sourceContact, int sfds, int dfgd) {
		setDisconnect(sourceContact, sfds);
		setDisconnect(contact, dfgd);
		reconnect(contact);
		testContact(contact);
	}

	/**
	 * Insert the method's description here. Creation date: (20/09/96 07:41:03
	 * p.m.)
	 */
	public Box extent() {
		return _matrix.extend();
	}

	/**
	 * Metodo que crea y devuelve una nueva lista con todos los contactos
	 * conectados al ctt. Creation date: (14/09/96 08:07:41 p.m.)
	 * 
	 * @return java.util.List
	 * @param ctt
	 *            newgui.Contact
	 */
	public List findAttachedContacts(Contact contact) {
		List listCtt = new ArrayList();
		attachContacts(contact, listCtt);
		return listCtt;

	}

	/**
	 * DEvuelve true si <code>contact</code> esta conectado en direccion addr
	 * Creation date: (07/09/96 01:39:40 a.m.)
	 * 
	 * @return boolean
	 * @param ctt
	 *            newgui.Contact
	 * @param dir
	 *            int
	 */
	public boolean isConnected(Contact contact, int addr) {
		return (contact._conectionMask & (1 << addr)) != 0;
	}

	/**
	 * Insert the method's description here. Creation date: (28/06/01 13:35:31)
	 * 
	 * @return boolean
	 */
	public boolean isTerminal(Contact ctt) {
		return countConnections(ctt) == 1;
	}

	public int countConnections(Contact ctt) {
		int i = 0;
		if (isConnected(ctt, EAST))
			i++;
		if (isConnected(ctt, NORTH))
			i++;
		if (isConnected(ctt, WEST))
			i++;
		if (isConnected(ctt, SOUTH))
			i++;
		return i;
	}

	/**
	 * Crea y devuelve un nuevo contacto Creation date: (06/09/96 12:03:54 a.m.)
	 * 
	 * @return newgui.Contact
	 */
	public abstract Contact makeContact(int x, int y);

	/**
	 * Devuelve el opuesto a una direccion dada. Creation date: (07/09/96
	 * 01:03:47 a.m.)
	 * 
	 * @return int
	 * @param addr
	 *            int
	 */
	public int opposite(int addr) {
		return (addr + 2) & 3;
	}

	/**
	 * Insert the method's description here. Creation date: (14/09/96 10:30:11
	 * p.m.)
	 * 
	 * @param Canvas
	 *            newgui.TextCanvas
	 */
	public abstract void paint(Graphics2D Canvas, Box boxViewport);

	/**
	 * Metodo que devuelve verdadero si existe un contacto en la posicion x,y
	 * Creation date: (19/10/00 08:04:23 p.m.)
	 * 
	 * @return boolean
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean peek(int x, int y) {
		if (_matrix.findVertical(x, y) == Matrix.EXIST)
			return true;

		if (_matrix.findHorizontal(x, y) == Matrix.EXIST)
			return true;

		return false;
	}

	/**
	 * Este metodo devuelve verdader true si pasa un cable horizontal por x,y
	 * Creation date: (19/10/00 08:03:10 p.m.)
	 * 
	 * @return boolean
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean peekH(int x, int y) {
		int existH = _matrix.findHorizontal(x, y);

		if (existH == Matrix.EXIST)
			return true;

		if (existH == Matrix.BETWEEN) {
			Contact contact = (Contact) _matrix.previous();
			if (isConnected(contact, EAST))
				return true;
		}
		return false;
	}

	/**
	 * Devuelve true si por x, y pasa un cable vertical Creation date: (19/10/00
	 * 08:02:43 p.m.)
	 * 
	 * @return boolean
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean peekV(int x, int y) {
		int existV = _matrix.findVertical(x, y);

		if (existV == Matrix.EXIST)
			return true;

		if (existV == Matrix.BETWEEN) {
			Contact contact = (Contact) _matrix.previous();
			if (isConnected(contact, NORTH))
				return true;
		}
		return false;
	}

	/**
	 * Crea un nuevo contacto en la matriz y si pasan cables por ella, los une a
	 * este contacto Creation date: (07/09/96 01:20:52 a.m.)
	 * 
	 * @return newgui.Contact
	 * @param x
	 *            int
	 * @param i
	 *            int
	 */
	public Contact poke(int x, int y) {

		int Hconnected = _matrix.findHorizontal(x, y);

		if (Hconnected == Matrix.EXIST)
			return (Contact) _matrix.hit();

		Contact contact = makeContact(x, y);

		connectToSorroundingContacts(contact, x, y);

		_matrix.add(contact);

		return contact;
	}

	private Contact connectToSorroundingContacts(Contact contact, int x, int y) {
		_matrix.findHorizontal(x, y);
		Contact cttHprevious = (Contact) _matrix.previous();
		Contact cttHnext = (Contact) _matrix.next();

		_matrix.findVertical(x, y);
		Contact cttVprevious = (Contact) _matrix.previous();
		Contact cttVnext = (Contact) _matrix.next();

		if (cttHprevious != null && isConnected(cttHprevious, EAST))
			setConnect(contact, WEST);
		if (cttHnext != null && isConnected(cttHnext, WEST))
			setConnect(contact, EAST);
		if (cttVprevious != null && isConnected(cttVprevious, NORTH))
			setConnect(contact, SOUTH);
		if (cttVnext != null && isConnected(cttVnext, SOUTH))
			setConnect(contact, NORTH);
		return contact;
	}

	/**
	 * Reconecta todos los contactos que estan logicamente conectados a Ctt
	 * Creation date: (01/01/01 14:54:25)
	 * 
	 * @param ctt
	 *            circuit.Contact el contacto a revisar y reconectar
	 */
	public void reconnect(Contact contact) {
		List attachedContacts = findAttachedContacts(contact);
		reconnect(attachedContacts);
	}

	/**
	 * Reconecta los pines que existen en una lista de contactos Creation date:
	 * (05/09/96 11:59:40 p.m.)
	 * 
	 * @param cttList
	 *            java.util.List
	 */

	public abstract void reconnect(List contactsList);

	/**
	 * Insert the method's description here. Creation date: (01/01/01 20:52:53)
	 * 
	 * @param pin
	 *            csim.Pin
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */

	public abstract void removePin(byte pinId, Gate gate, int x, int y);

	/**
	 * Setea una conexion en la direccion especificada Creation date: (07/09/96
	 * 01:38:51 a.m.)
	 * 
	 * @param ctt
	 *            newgui.Contact
	 * @param dir
	 *            int
	 */
	protected void setConnect(Contact contact, int dir) {
		contact._conectionMask = (byte) (contact._conectionMask | (1 << dir));
	}

	/**
	 * Desconecta un contacto en alguna direccion Creation date: (07/09/96
	 * 01:41:30 a.m.)
	 * 
	 * @param ctt
	 *            newgui.Contact
	 * @param addr
	 *            int
	 */
	protected void setDisconnect(Contact contact, int dir) {
		contact._conectionMask = (byte) (contact._conectionMask & ~(1 << dir));
	}

	/**
	 * Verifica si un contacto es necesario Creation date: (14/09/96 07:59:57
	 * p.m.)
	 * 
	 * @param ctt
	 *            newgui.Contact
	 */
	public abstract void testContact(Contact ctt);
}

