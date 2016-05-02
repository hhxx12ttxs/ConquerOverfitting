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

package cl.alejo.jcsim.csim.circuit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import cl.alejo.jcsim.csim.dom.Gate;
import cl.alejo.jcsim.csim.dom.Pin;
import cl.alejo.jcsim.csim.gates.IconGate;
import cl.alejo.jcsim.csim.simulation.Agenda;
import cl.alejo.jcsim.window.Window;

public class Circuit implements java.io.Serializable, java.awt.event.ActionListener {

	private static final Color SELECTION_COLOR = new Color(250, 250, 0);

	private transient SelectionContainer _selectionTarget;

	private transient SelectionContainer _selectionBuffer;

	public static final short GRIDSIZE = 8;

	public static final short HALF_GRIDSIZE = GRIDSIZE / 2;

	public static final int NOTHING_STATE = 0;

	public static final int DRAGGING_GATE_STATE = 1;

	public static final int MOVING_CIRCUIT_STATE = 2;

	public static final int CONNECTING_STATE = 3;

	public static final int CLONNING_STATE = 4;

	public static final int SELECTING_STATE = 5;

	public static final int DRAGGING_VIEWPORT = 6;

	public int state = NOTHING_STATE;

	private Box _extent = new Box();

	transient private int _xConnectionStart, _yConnectionStart, _xConnectionEnd, _yConnectionEnd, _xConnectionMiddle,
		_yConnectionMiddle;

	transient private int _xSelectionStart, _ySelectionStart, _xSelectionEnd, _ySelectionEnd;

	transient private Box _selectionBox;

	public static final Color[] COLOR = { Color.BLACK, Color.GREEN, Color.LIGHT_GRAY, Color.CYAN, Color.WHITE,
			Color.BLUE, Color.MAGENTA, Color.YELLOW };

	private List _icons = new LinkedList();

	private Protoboard _protoboard;

	private Agenda _agenda = new Agenda();

	private transient IconGate _draggeableIconGate = null;

	private transient int _xDragging, _yDragging;

	private transient Image _gateImage = null;

	private transient Point _beginPoint, _endPoint;

	private transient Pin _shortCircuitPin = null;

	private String _name = null;

	private transient boolean _modified = false;

	public boolean _modificable = false;

	private static final int _timerTime = 100;

	private transient Timer _timer;

	private transient List _windows = new ArrayList();

	private double _lastRepaint = System.currentTimeMillis();

	private boolean _isRunning = true;

	private transient List _selections = new LinkedList();

	private transient int _colorCounter = 0;

	/**
	 * Insert the method's description here. Creation date: (11/12/00 17:58:37)
	 */
	public Circuit() {
		_protoboard = new ProtoboardPin();
		initTimer();
	}

	private void initTimer() {
		_timer = new Timer(_timerTime, this);
		_timer.setDelay(_timerTime);
	}

	public void actionPerformed(ActionEvent event) {
		repaintWindows();
		_timer.start();
	}

	/**
	 * Activa una nueva compuerta Creation date: (07/09/96 01:29:53 a.m.)
	 * 
	 * @return newgui.Point
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void activate(IconGate icon) {
		for (byte pinId = 0; pinId < icon.pinCount(); pinId++) {
			activePin(true, pinId, icon.transformedPoint(icon.getPointPin()[pinId]), icon.getGate());
		}
	}

	public void desactivate(IconGate icon) {
		for (byte pinId = 0; pinId < icon.pinCount(); pinId++) {
			activePin(false, pinId, icon.transformedPoint(icon.getPointPin()[pinId]), icon.getGate());
		}
	}

	private void activePin(boolean flag, byte pinId, Point p, Gate gate) {
		if (flag)
			_protoboard.addPin(pinId, gate, gridTrunc(p._x), gridTrunc(p._y));
		else
			_protoboard.removePin(pinId, gate, gridTrunc(p._x), gridTrunc(p._y));
	}

	/**
	 * Este metodo se invoca para activar todos los pines de las compuertas...
	 * Creation date: (05/04/01 16:55:55)
	 */
	public void activatePins() {
		for (Iterator iter = _icons.iterator(); iter.hasNext();) {
			IconGate icon = (IconGate) iter.next();
			desactivate(icon);
			activate(icon);
		}

		initTimerToOneTime();

		_lastRepaint = System.currentTimeMillis();
	}

	private void initTimerToOneTime() {
		if (_timer == null) {
			_timer = new Timer(_timerTime, this);
			_timer.setDelay(_timerTime);
			_timer.setRepeats(false);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (04/10/01 0:23:08)
	 */
	public void actualizeWindowsTitles() {

		if (_windows == null)
			return;

		for (Iterator iter = _windows.iterator(); iter.hasNext();) {
			Window window = (Window) iter.next();
			window.setTitle(_name);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (12/12/00 0:55:54)
	 * 
	 * @return int
	 * @param icon
	 *            csimgui.IconGate
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void addIconGate(IconGate icon, int x, int y) {

		_icons.add(icon);

		Dimension size = icon.getSize();

		int _xi = Circuit.gridTrunc(((int) (x - size.getWidth() / 2)));
		int _yi = Circuit.gridTrunc(((int) (y - size.getHeight() / 2)));
		int _xf = Circuit.gridTrunc(_xi + (int) size.getWidth());
		int _yf = Circuit.gridTrunc(_yi + (int) size.getHeight());

		icon.set(_xi, _yi, _xf, _yf);

		icon.moveTo(Circuit.gridTrunc(x), Circuit.gridTrunc(y));

		activate(icon);

		_extent.extend(icon);
	}

	/**
	 * Agregamos una ventana a la lista de ventanas de este circuito Creation
	 * date: (25/03/01 20:09:26)
	 * 
	 * @param window
	 *            jcsimwindow.JCSimWindow La ventana a agregar
	 */
	public void addWindow(Window window) {
		if (_windows == null)
			_windows = new ArrayList();

		if (!_windows.contains(window))
			_windows.add(window);
	}

	/**
	 * Se deberia ejecutar cuando se hace doble click en una compuerta Esto
	 * busca si existe un icono en esa posicion y luego ejecuta
	 * <code>apply</code> de ese icono.
	 * 
	 * @param x
	 *            int Posicion x
	 * @param y
	 *            int Posicion y
	 */
	public void apply(int x, int y) {
		IconGate icon = findIcon(x, y);
		if (icon != null)
			icon.apply(x, y);
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:27:06)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void beginConnect(int x, int y) {
		setMode(CONNECTING_STATE);
		_xConnectionStart = _xConnectionMiddle = _xConnectionEnd = Circuit.gridTrunc(x);
		_yConnectionStart = _yConnectionMiddle = _yConnectionEnd = Circuit.gridTrunc(y);
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:30:42)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void beginDragGate(int x, int y, IconGate icon) {
		// TODO: convertir esta porqueria en ESTADOS
		if (icon != null) {
			_draggeableIconGate = icon;
			_gateImage = icon.printImage();
			_xDragging = gridTrunc(x);
			_yDragging = gridTrunc(y);
			setMode(DRAGGING_GATE_STATE);
		}
	}

	public void beginDragSelection(int x, int y, SelectionContainer container) {
		// TODO: convertir esta porqueria en ESTADOS
		if (container != null) {
			_selectionTarget = container;
			// _selectionImage = container.getImage();
			_xDragging = gridTrunc(x);
			_yDragging = gridTrunc(y);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:30:42)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void beginDragViewport(int x, int y) {

		// El punto donde comienza el drag and drop de la compuerta
		_beginPoint = new Point(gridTrunc(x) + Circuit.HALF_GRIDSIZE, gridTrunc(y) + Circuit.HALF_GRIDSIZE);
		_endPoint = new Point(gridTrunc(x) + Circuit.HALF_GRIDSIZE, gridTrunc(y) + Circuit.HALF_GRIDSIZE);

		// El estado
		setMode(DRAGGING_VIEWPORT);
	}

	/**
	 * Empieza la seleccion Creation date: (03/04/01 17:26:48)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void beginSelect(int x, int y) {
		setMode(SELECTING_STATE);
		_xSelectionStart = _xSelectionEnd = Circuit.gridTrunc(x) + Circuit.HALF_GRIDSIZE;
		_ySelectionStart = _ySelectionEnd = Circuit.gridTrunc(y) + Circuit.HALF_GRIDSIZE;
	}

	/**
	 * Calculamos la extension total del circuito, incluyendo cables y
	 * Compuertas
	 * 
	 */
	public void computeExtension() {
		_extent.setEmpty();

		for (Iterator iter = _icons.iterator(); iter.hasNext();) {
			IconGate icon = (IconGate) iter.next();
			_extent.extend((IconGate) icon);
		}
		_extent.extend(_protoboard.extent());

	}

	/**
	 * Hace una conexion desde (xi,yi) a (xf,yf) (vertical+horizontal)
	 * 
	 * @param xi
	 *            int El comienzo en x
	 * @param yi
	 *            int El comienzo en y
	 * @param xf
	 *            int El fin en x
	 * @param yf
	 *            int El fin en y
	 */
	public void connect(int xi, int yi, int xf, int yf) {
		_modified = true;
		_protoboard.connect(gridTrunc(xi), gridTrunc(yi), gridTrunc(xf), gridTrunc(yf));
		_extent.extend(gridTrunc(xi), gridTrunc(yi));
		_extent.extend(gridTrunc(xf), gridTrunc(yf));
	}

	/**
	 * Hace una conexion desde (xi, yi) a (xf, yf) (vertical+horizontal) pasando
	 * por (xm, ym)
	 * 
	 * @param xi
	 *            int El comienzo en x
	 * @param yi
	 *            int El comienzo en y
	 * @param xm
	 *            int El medio en x
	 * @param ym
	 *            int El medio en y
	 * @param xf
	 *            int El fin en x
	 * @param yf
	 *            int El fin en y
	 */
	private void connect(int xi, int yi, int xm, int ym, int xf, int yf) {
		_modified = true;
		_protoboard.connect(gridTrunc(xi), gridTrunc(yi), gridTrunc(xm), gridTrunc(ym), gridTrunc(xf), gridTrunc(yf));
		_extent.extend(gridTrunc(xi), gridTrunc(yi));
		_extent.extend(gridTrunc(xm), gridTrunc(ym));
		_extent.extend(gridTrunc(xf), gridTrunc(yf));
	}

	/**
	 * Elimina icon de este circuito.
	 * 
	 * @param icon
	 *            IconGate El icono a borrar
	 */
	public void delete(IconGate icon) {
		_modified = true;

		desactivate(icon);
		_icons.remove(icon);
		if (icon.inBorder(_extent))
			computeExtension();

		icon.clean();
	}

	/**
	 * Desconecta un cable que pase por x,y Creation date: (01/01/01 15:15:40)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void disconnect(int x, int y) {
		_protoboard.disconnect(gridTrunc(x), gridTrunc(y));
		computeExtension();
		_modified = true;
	}

	/**
	 * Hacemos drag conectando un cable Creation date: (03/04/01 17:42:23)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void dragConnection(int x, int y) {
		_xConnectionEnd = gridTrunc(x);
		_yConnectionEnd = gridTrunc(y);

		if (getConnectionWidth() < getConnectionHeight()) {
			_xConnectionMiddle = _xConnectionStart;
			_yConnectionMiddle = _yConnectionEnd;
		} else {
			_xConnectionMiddle = _xConnectionEnd;
			_yConnectionMiddle = _yConnectionStart;
		}
	}

	private int getConnectionHeight() {
		return Math.abs(_yConnectionEnd - _yConnectionStart);
	}

	private int getConnectionWidth() {
		return Math.abs(_xConnectionEnd - _xConnectionStart);
	}

	public void dragGate(int x, int y) {
		if (_draggeableIconGate != null) {
			Dimension dim = _draggeableIconGate.getRotatedSize();
			_xDragging = gridTrunc(x) - gridTrunc(dim.width / 2);
			_yDragging = gridTrunc(y) - gridTrunc(dim.height / 2);
		}
	}

	public void dragSelect(int x, int y) {
		_xSelectionEnd = gridTrunc(x) + Circuit.HALF_GRIDSIZE;
		_ySelectionEnd = gridTrunc(y) + Circuit.HALF_GRIDSIZE;
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:40:36)
	 */
	public void dragViewport(int x, int y) {
		_endPoint._x = gridTrunc(x) + Circuit.HALF_GRIDSIZE;
		_endPoint._y = gridTrunc(y) + Circuit.HALF_GRIDSIZE;
	}

	/**
	 * Dibujamos el cable que estamos conectando. Creation date: (22/01/01
	 * 20:56:11)
	 * 
	 * @param gr
	 *            java.awt.Graphics2D
	 * @param boxViewport
	 *            circuit.Box
	 */
	public void drawConnect(Graphics2D gr) {

		gr.setColor(Color.blue);
		int _xi = Circuit.gridTrunc(_xConnectionStart) + Circuit.HALF_GRIDSIZE;
		int _yi = Circuit.gridTrunc(_yConnectionStart) + Circuit.HALF_GRIDSIZE;
		int _xm = Circuit.gridTrunc(_xConnectionMiddle) + Circuit.HALF_GRIDSIZE;
		int _ym = Circuit.gridTrunc(_yConnectionMiddle) + Circuit.HALF_GRIDSIZE;
		int _xf = Circuit.gridTrunc(_xConnectionEnd) + Circuit.HALF_GRIDSIZE;
		int _yf = Circuit.gridTrunc(_yConnectionEnd) + Circuit.HALF_GRIDSIZE;
		// Para no dibujar un pto.
		if (_xi != _xf || _yi != _yf) {
			gr.drawLine(_xi, _yi, _xm, _ym);
			gr.drawLine(_xm, _ym, _xf, _yf);
		}
	}

	private void drawPastedCircuits(Graphics2D gr) {

		if (_selections == null)
			return;
		gr.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, (float) 0.5));

		for (Iterator iterator = _selections.iterator(); iterator.hasNext();) {
			SelectionContainer container = (SelectionContainer) iterator.next();
			gr.drawImage(container.getImage(), container.getBox()._xi, container.getBox()._yi, null);
		}

		gr.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, (float) 1.0));
	}

	/**
	 * Insert the method's description here. Creation date: (20/04/01 15:57:30)
	 * 
	 * @param canvas
	 *            java.awt.Graphics2D
	 */
	private void drawDragged(Graphics2D gr) {
		setTransparence(gr, (float) 0.7);
		gr.drawImage(_gateImage, gridTrunc(_xDragging), gridTrunc(_yDragging), null);
	}

	private void setTransparence(Graphics2D gr, double alpha) {
		gr.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, (float) alpha));
	}

	/**
	 * 
	 * Creation date: (22/01/01 20:56:11)
	 * 
	 * @param gr
	 *            java.awt.Graphics2D
	 * @param boxViewport
	 *            circuit.Box
	 */
	public void drawDragViewport(Graphics2D gr) {

		gr.setColor(Color.yellow);
		gr.drawLine(_beginPoint._x, _beginPoint._y, _endPoint._x, _endPoint._y);
		drawCross(gr, _endPoint._x, _endPoint._y);
	}

	private void drawCross(Graphics2D gr, int x, int y) {
		gr.drawLine(x - 30, y, x + 30, y);
		gr.drawLine(x, y - 30, x, y + 30);
	}

	/**
	 * Dibuja el cuadrado de seleccion Creation date: (07/04/01 16:35:23)
	 */
	private void drawSelect(Graphics2D gr) {
		int _xi = gridTrunc(_xSelectionStart) + Circuit.HALF_GRIDSIZE;
		int _yi = gridTrunc(_ySelectionStart) + Circuit.HALF_GRIDSIZE;
		int _xf = gridTrunc(_xSelectionEnd) + Circuit.HALF_GRIDSIZE;
		int _yf = gridTrunc(_ySelectionEnd) + Circuit.HALF_GRIDSIZE;

		if (_xi > _xf) {
			int aux = _xi;
			_xi = _xf;
			_xf = aux;
		}
		if (_yi > _yf) {
			int aux = _yi;
			_yi = _yf;
			_yf = aux;
		}
		drawSelectionRectangle(gr, _xi, _yi, _xf, _yf);
	}

	private void drawSelectionRectangle(Graphics2D gr, int _xi, int _yi, int _xf, int _yf) {
		Color color = gr.getColor();
		gr.setColor(SELECTION_COLOR);
		gr.drawRect(_xi, _yi, _xf - _xi, _yf - _yi);
		setTransparence(gr, 0.5);
		gr.fillRect(_xi + 1, _yi + 1, _xf - _xi - 1, _yf - _yi - 1);
		setTransparence(gr, 1);
		gr.setColor(color);
	}

	/**
	 * Cuando terminamos de poner un cable Creation date: (15/01/01 1:04:26)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void endConnect() {

		setMode(NOTHING_STATE);
		connect(_xConnectionStart, _yConnectionStart, _xConnectionMiddle, _yConnectionMiddle, _xConnectionEnd,
			_yConnectionEnd);

		computeExtension();
		_modified = true;
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:34:08)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void endDragGate() {

		if (_draggeableIconGate != null) {
			_gateImage = null;
			computeExtension();
			setMode(NOTHING_STATE);
		}
		_modified = true;
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:34:32)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void endDragViewport() {
		setMode(NOTHING_STATE);
		_beginPoint = _endPoint = null;
	}

	/**
	 * Insert the method's description here. Creation date: (03/04/01 17:34:32)
	 * 
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public void endSelect() {
		setMode(NOTHING_STATE);
		_selectionBox = new Box(_xSelectionStart, _ySelectionStart, _xSelectionEnd, _ySelectionEnd);
		_modified = true;
	}

	private void copySelectionAreaToBuffer() {
		if (_selectionBox.getWidth() == 0 || _selectionBox.getHeight() == 0)
			return;

		Circuit circuit = newSelectedCircuit();

		if (_selections == null) {
			_selections = new LinkedList();
		}

		Box box = new Box(_selectionBox);
		box.moveTo(0, 0);
		_selectionBuffer = new SelectionContainer(circuit, createCopyImage(circuit), box);
	}

	private void copyBufferToSelectionList() {
		if (_selectionBuffer != null)
			_selections.add(_selectionBuffer.getClone());
	}

	public void paste() {
		copyBufferToSelectionList();
	}

	public void copy() {
		copySelectionAreaToBuffer();
	}

	private Circuit newSelectedCircuit() {
		Circuit circuit = new Circuit();
		for (Iterator iterator = _icons.iterator(); iterator.hasNext();) {
			IconGate icon = (IconGate) iterator.next();
			if (_selectionBox.contains(icon)) {
				addIconCloneToCircuit(circuit, icon, icon._xi - _selectionBox._xi, icon._yi - _selectionBox._yi);
			}
		}

		// TODO TErminar este metodo
		ContactPin previousContact = null;
		for (Iterator iterH = _protoboard._matrix.iteratorX(); iterH.hasNext();) {
			ContactPin currentContact = (ContactPin) iterH.next();

			if (previousContact != null && _protoboard.isConnected(previousContact, Protoboard.EAST)
				&& _selectionBox.isIntersected(previousContact, currentContact)) {

				int xi = Math.max(previousContact._x, _selectionBox._xi);
				int xf = Math.min(currentContact._x, _selectionBox._xf);
				circuit.connect(xi - _selectionBox._xi, currentContact._y - _selectionBox._yi, xf - _selectionBox._xi,
					currentContact._y - _selectionBox._yi);

			}
			previousContact = currentContact;

			if (currentContact._y > _selectionBox._yf)
				break;
		}

		previousContact = null;
		for (Iterator iterV = _protoboard._matrix.iteratorY(); iterV.hasNext();) {
			ContactPin currentContact = (ContactPin) iterV.next();

			if (previousContact != null && _protoboard.isConnected(previousContact, Protoboard.NORTH)
				&& _selectionBox.isIntersected(previousContact, currentContact)) {

				int yi = Math.max(previousContact._y, _selectionBox._yi);
				int yf = Math.min(currentContact._y, _selectionBox._yf);
				circuit.connect(yi - _selectionBox._yi, currentContact._x - _selectionBox._xi, yf - _selectionBox._yi,
					currentContact._x - _selectionBox._xi);

			}
			previousContact = currentContact;

			if (currentContact._x > _selectionBox._xf)
				break;
		}

		return circuit;
	}

	// TODO refactorizar con el anterior
	public Circuit getClone() {
		Circuit circuit = new Circuit();
		for (Iterator iterator = _icons.iterator(); iterator.hasNext();) {
			IconGate icon = (IconGate) iterator.next();
			addIconCloneToCircuit(circuit, icon, icon._xi, icon._yi);
		}

		ContactPin previousContact = null;
		// TODO refactorizar estos dos metodos
		for (Iterator iterH = _protoboard._matrix.iteratorX(); iterH.hasNext();) {
			ContactPin currentContact = (ContactPin) iterH.next();

			if (previousContact != null && _protoboard.isConnected(previousContact, Protoboard.EAST)) {
				circuit.connect(previousContact._x, currentContact._y, currentContact._x, currentContact._y);

			}
			previousContact = currentContact;
		}

		previousContact = null;
		for (Iterator iterV = _protoboard._matrix.iteratorY(); iterV.hasNext();) {
			ContactPin currentContact = (ContactPin) iterV.next();

			if (previousContact != null && _protoboard.isConnected(previousContact, Protoboard.NORTH)) {
				circuit.connect(currentContact._x, previousContact._y, currentContact._x, currentContact._y);

			}
			previousContact = currentContact;
		}

		return circuit;
	}

	private Image createCopyImage(Circuit circuit) {
		Image image = new BufferedImage(_selectionBox.getWidth(), _selectionBox.getHeight(),
			BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = (Graphics2D) image.getGraphics();
		Box box = new Box(_selectionBox);
		box.moveTo(0, 0);
		gr.setColor(Color.CYAN);
		gr.drawRect(1, 1, box.getWidth() - 2, box.getHeight() - 2);
		circuit.paint(gr, box);
		return image;
	}

	public Box getExtent() {
		return _extent;
	}

	public List findContacts(Box box) {
		List contactList = new ArrayList();

		for (Iterator iter = _protoboard._matrix.iteratorX(); iter.hasNext();) {
			ContactPin contact = (ContactPin) iter.next();
			if (box.contains(contact))
				contactList.add(contact);
		}
		return contactList;
	}

	public SelectionContainer findSelection(int x, int y) {
		if (_selections == null)
			return null;
		for (Iterator iterator = _selections.iterator(); iterator.hasNext();) {
			SelectionContainer container = (SelectionContainer) iterator.next();
			if (container.getBox().contains(x, y)) {
				return container;
			}
		}
		return null;
	}

	/**
	 * Busca todos los gates conectados a un gate y los agrega a la lista
	 * Creation date: (23/06/01 11:57:13)
	 * 
	 * @return java.util.List
	 * @param box
	 *            circuit.Box
	 */
	public static void findGates(Gate gate, List listGate) {
		if (listGate.contains(gate))
			return;

		if (gate.isNormal())
			listGate.add(gate);

		for (int i = 0; i < gate.pinCount(); i++) {
			addGatesAttachedToPin(gate, listGate, i);
		}
	}

	private static void addGatesAttachedToPin(Gate gate, List listGate, int i) {
		Pin pinFirst = gate.getPin(i);
		Pin pin = pinFirst;
		do {
			findGates(pin.getGate(), listGate);
			pin = (Pin) pin.next();
		} while (pin != pinFirst);
	}

	/**
	 * Insert the method's description here. Creation date: (23/06/01 11:57:13)
	 * 
	 * @return java.util.List
	 * @param box
	 *            circuit.Box
	 */
	public List findGates(List contacts) {

		// Una lista vacia de gates
		List gates = new ArrayList();

		for (Iterator iter = contacts.iterator(); iter.hasNext();) {
			ContactPin ctt = (ContactPin) iter.next();

			Pin pin = ctt.getGuidePin();

			if (pin != null)
				findGates(pin.getGate(), gates);
		}

		return gates;
	}

	/**
	 * Devuelve el icono que este en (x, y)
	 * 
	 * Creation date: (01/01/01 20:30:05)
	 * 
	 * @return icongate.IconGate
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public IconGate findIcon(int x, int y) {

		int _x = gridTrunc(x);
		int _y = gridTrunc(y);

		for (Iterator iterIcon = _icons.iterator(); iterIcon.hasNext();) {
			IconGate icon = (IconGate) iterIcon.next();
			if (icon.contains(_x, _y))
				return icon;
		}
		return null;
	}

	/**
	 * Devuelve el nombre del circuito Creation date: (25/03/01 20:28:02)
	 * 
	 * @return java.lang.String
	 */
	public String getName() {
		return _name;
	}

	/*
	 * Alinea hacia la derecha, arriba
	 */
	public static int gridCeil(int coord) {
		return coord | (GRIDSIZE - 1);
	}

	/**
	 * Alinea hacia la izquierda, abajo
	 */
	public static int gridTrunc(int coord) {
		return coord & (~(GRIDSIZE - 1));
	}

	/**
	 * True si <code>contact</code> es un contacto terminal Creation date:
	 * (28/06/01 13:38:10)
	 * 
	 * @return boolean
	 * @param ctt
	 *            circuit.Contact
	 */
	public boolean isTerminal(Contact contact) {
		return _protoboard.isTerminal(contact);
	}

	/**
	 * Carga un circuito de disco
	 * 
	 * @param filename
	 *            String El archivo que se debe cargar
	 */
	static public Circuit load(String filename) throws Exception {
		Circuit circuit = (Circuit) new ObjectInputStream(new FileInputStream(filename)).readObject();
		circuit.activatePins();
		return circuit;
	}

	/**
	 * Mueve <code>icon</code> hasta la posicion x, y
	 * 
	 * @param icon
	 *            IconGate El icono
	 * @param x
	 *            int EL x final
	 * @param y
	 *            int El y final
	 */
	public void moveTo(IconGate icon, int x, int y) {
		_modified = true;
		_extent.extend((Box) icon);
		icon.moveTo(gridTrunc(x), gridTrunc(y));
		icon.setTranslate(gridTrunc(x) * 2, gridTrunc(y) * 2);
		_extent.extend((Box) icon); // */
	}

	/**
	 * Redibuja las compuertas y los cables contenidos en <code>viewport</box>
	 * 
	 * @param viewport
	 *            Box El viewport
	 */
	public void paint(Graphics2D gr, Box viewport) {

		drawPastedCircuits(gr);
		drawGates(gr, viewport);
		_protoboard.paint(gr, viewport);
		drawMouseActions(gr);
		drawSelection(gr);
		drawPastedCircuits(gr);

	}

	private void drawSelection(Graphics2D gr) {
		if (_selectionBox != null && !_selectionBox.isEmpty()) {
			Color color = gr.getColor();
			gr.setColor(COLOR[_colorCounter]);
			_colorCounter = (_colorCounter + 1) % COLOR.length;
			gr.drawRect(_selectionBox._xi, _selectionBox._yi, _selectionBox.getWidth(), _selectionBox.getHeight());
			gr.setColor(color);
		}
	}

	private void drawMouseActions(Graphics2D gr) {
		switch (state) {
		case CONNECTING_STATE:
			drawConnect(gr);
			break;
		case MOVING_CIRCUIT_STATE:
			break;
		case SELECTING_STATE:
			drawSelect(gr);
			break;
		case DRAGGING_GATE_STATE:
		case CLONNING_STATE:
			drawDragged(gr);
			break;
		case DRAGGING_VIEWPORT:
			drawDragViewport(gr);
			break;
		}
	}

	private void drawGates(Graphics2D gr, Box boxViewport) {
		for (Iterator iter = _icons.iterator(); iter.hasNext();) {
			IconGate icon = (IconGate) iter.next();
			if (boxViewport.containsSomeCorner(icon))
				icon.paint(gr);
		}
	}

	// TODO usarlo
	private void add(Circuit circuit, int x, int y) {
		for (Iterator iterator = circuit.getIcons().iterator(); iterator.hasNext();) {
			IconGate icon = (IconGate) iterator.next();
			addIconCloneToCircuit(this, icon, icon._xi, icon._yi);
		}
	}

	private void addIconCloneToCircuit(Circuit circuit, IconGate icon, int x, int y) {
		IconGate iconClone = icon.make(circuit);
		circuit.addIconGate(iconClone, x, y);
	}

	/**
	 * Hacemos un peek en la protoboard Creation date: (03/04/01 17:57:43)
	 * 
	 * @return boolean
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean peek(int x, int y) {
		return _protoboard.peek(gridTrunc(x), gridTrunc(y));
	}

	/**
	 * Hacemos un peek en la protoboard Creation date: (03/04/01 17:57:43)
	 * 
	 * @return boolean
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean peekH(int x, int y) {
		return _protoboard.peekH(gridTrunc(x), gridTrunc(y));
	}

	/**
	 * Hacemos un peek en la protoboard Creation date: (03/04/01 17:57:43)
	 * 
	 * @return boolean
	 * @param x
	 *            int
	 * @param y
	 *            int
	 */
	public boolean peekV(int x, int y) {
		return _protoboard.peekV(gridTrunc(x), gridTrunc(y));
	}

	/**
	 * Refresca las barras de scroll asociadas a este circuito Creation date:
	 * (01/06/01 20:00:00)
	 */
	public void refreshScrollBars() {

		for (Iterator iterWin = _windows.iterator(); iterWin.hasNext();) {
			Window window = (Window) iterWin.next();
			window.refreshScrollbars();
		}
	}

	/**
	 * Quitamos la ventana de la lista de ventanas
	 * 
	 * @param window
	 *            jcsimwindow.JCSimWindow La ventana a remover
	 */
	public void removeWindow(Window window) {
		if (_windows == null)
			_windows = new ArrayList();

		if (_windows.contains(window))
			_windows.remove(window);
	}

	/**
	 * Insert the method's description here. Creation date: (19/04/01 1:48:05)
	 */
	public void repaintWindows() {
		double currTime = System.currentTimeMillis();
		double simTime = currTime - _lastRepaint;
		_lastRepaint = currTime;

		for (Iterator iterator = _windows.iterator(); iterator.hasNext();) {
			((Window) iterator.next()).getCanvas().repaint();
		}

		if (_isRunning)
			_agenda.runTime(simTime);
	}

	/**
	 * Serialize the circuit
	 * 
	 * @param out
	 *            java.io.ObjectOutputStream El stream para grabar
	 */
	public void save(String filename) throws IOException {
		ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filename));
		stream.writeObject(this);
		stream.flush();
	}

	/**
	 * Pone este pin como de cortocircuito Creation date: (03/04/01 20:24:31)
	 * 
	 * @param pin
	 *            csim.Pin
	 */
	private void setBadPin(Pin pin) {
		_shortCircuitPin = pin;
	}

	/**
	 * Sets mode
	 * 
	 * 
	 * @param mode
	 *            int
	 */
	public void setMode(int mode) {
		state = mode;
	}

	/**
	 * Changes circuit name
	 * 
	 * @param name
	 *            java.lang.String
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Hace andar el timer para el refresco de las ventanas Creation date:
	 * (05/06/01 18:20:30)
	 */
	public void startTimer() {
		_lastRepaint = System.currentTimeMillis();
		_timer.start();
	}

	/**
	 * Starts circuit simulation
	 */
	public void startSimulation() {
		_isRunning = true;
	}

	/**
	 * Detiene la simulacion del circuito
	 */
	public void stopSimulation() {
		_isRunning = false;
	}

	public Agenda getAgenda() {
		return _agenda;
	}

	public Protoboard getProtoboard() {
		return _protoboard;
	}

	public boolean isModified() {
		return _modified;
	}

	public List getIcons() {
		return _icons;
	}

	public void cleanSelection() {
		if (_selectionBox != null)
			_selectionBox.setEmpty();
	}

	public void setSelectionTarget(SelectionContainer container) {
		_selectionTarget = container;
	}

	public void dragSelection(int x, int y) {
		if (_selectionTarget != null) {
			_selectionTarget.getBox().moveTo(gridTrunc(x), gridTrunc(y));
		}
	}
}
