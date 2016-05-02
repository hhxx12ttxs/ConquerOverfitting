package com.safari.rhinodestruction;

public class RhinoSprite extends Sprite {
	private static double DURACION = 0.5;
	private static final int SIN_SALTO = 0;
	private static final int SUBIENDO = 1;
	private static final int BAJANDO = 2;
	private static final int DIST_MAX_SUBIENDO = 8;
	private int periodo;
	public boolean derecha, quieto, golpe, muerto;
	private int movVertical;
	private int distVertical;
	private int contSubiendo;
	private ManejadorDeLadrillos ladrillin;
	private int velocidad;
	private int xMundo, yMundo;
	public boolean golpe_edificio;

	public RhinoSprite(int w, int h, int velocidadLadrillo,
			ManejadorDeLadrillos ml, CargadorDeImg carImg, int p) {
		super(w / 2, h / 2, w, h, carImg, "stillderecha");
		velocidad = velocidadLadrillo;
		ladrillin = ml;
		periodo = p;
		setPaso(0, 0);
		derecha = true;
		quieto = true;
		golpe = false;
		muerto = false;
		posY = ladrillin.encontrarPiso(posX + getWidth() / 2) - getHeight();
		xMundo = posX;
		yMundo = posY;
		movVertical = SIN_SALTO;
		distVertical = ladrillin.getLadrilloHeight() / 2;
		contSubiendo = 0;
	}

	public void movIzquierda() {
		setImage("izquierda");
		loopImage(periodo, DURACION);
		derecha = false;
		quieto = false;
		golpe = false;
	}

	public void movDerecha() {
		setImage("derecha");
		loopImage(periodo, DURACION);
		derecha = true;
		quieto = false;
		golpe = false;
	}

	public void sinMovimiento() {
		if (derecha) {
			setImage("stillderecha");
			loopImage(periodo / 4, DURACION);
		} else {
			setImage("stillizquierda");
			loopImage(periodo / 4, DURACION);
		}
		quieto = true;
	}

	public void golpe() {
		if (derecha) {
			setImage("punchder");
			loopImage(periodo, DURACION);
		} else {
			setImage("punchizq");
			loopImage(periodo, DURACION);
		}
		if (!golpe)
			stopLooping();
		golpe = true;
	}

	public void golpebajo() {
		if (derecha) {
			setImage("downpunchder");
		} else {
			setImage("downpunchizq");
		}
	}

	public void salto() {
		if (movVertical == SIN_SALTO) {
			movVertical = SUBIENDO;
			contSubiendo = 0;
			if (quieto) {
				if (derecha)
					setImage("jumpder");
				else
					setImage("jumpizq");
			}
		}
		golpe = false;
	}

	public void muerto() {
		if (derecha) {
			setImage("muertoder");
		} else {
			setImage("muertoizq");
		}
		muerto = true;
	}

	public boolean willHitBrick() {
		if (quieto)
			return false;
		int testX;
		if (derecha)
			testX = xMundo + velocidad;
		else
			testX = xMundo - velocidad;
		int xMid = testX + getWidth() / 2;
		int yMid = yMundo + (int) (getHeight() * 0.85);
		return ladrillin.dentroDeLadrillo(xMid, yMid);
	}

	public void destruction() {
		int testX;
		int xMid, xMid2;
		int yMid, yMid2;
		if (derecha) {
			testX = xMundo + velocidad;
			xMid = testX + getWidth();
			yMid = yMundo + (int) (getHeight() * 0.4);
			xMid2 = testX + getWidth() / 2;
			yMid2 = yMundo + (int) (getHeight() * 0.4);
		} else {
			testX = xMundo - velocidad;
			xMid = testX;
			yMid = yMundo + (int) (getHeight() * 0.4);
			xMid2 = testX + getWidth() / 2;
			yMid2 = yMundo + (int) (getHeight() * 0.4);
		}
		ladrillin.destruirLadrillo(xMid2, yMid2);
		ladrillin.destruirLadrillo(xMid, yMid);
	}

	public void downdestruction() {
		updateCayendo();
		int xM = xMundo + getWidth() / 2;
		int yM = yMundo + getHeight();
		ladrillin.destruirLadrillo(xM, yM);
	}

	public void updateRhinoSprite() {
		if (!quieto) {
			if (derecha)
				xMundo += velocidad;
			else
				xMundo -= velocidad;
			if (movVertical == SIN_SALTO)
				checkSiCae();
		}
		if (movVertical == SUBIENDO)
			updateSubiendo();
		else if (movVertical == BAJANDO)
			updateCayendo();
		else if (muerto)
			updateCayendo();
		super.updateRhinoSprite();
	}

	private void checkSiCae() {
		int yTrans = ladrillin.checkTopeLadrillo(xMundo + (getWidth() / 2),
				yMundo + getHeight() + distVertical, distVertical);
		if (yTrans != 0)
			movVertical = BAJANDO;
	}

	private void updateSubiendo() {
		int yMid;
		if (contSubiendo == DIST_MAX_SUBIENDO) {
			movVertical = BAJANDO;
			contSubiendo = 0;
		} else {
			int yTrans = ladrillin.checkBaseLadrillo(xMundo + (getWidth() / 2),
					yMundo - distVertical, distVertical);
			ladrillin.destruirLadrillo(xMundo + (getWidth() / 2), yMundo
					- distVertical);
			yMid = yMundo + (int) (getHeight() * 0.4);
			ladrillin.destruirLadrillo(xMundo + (getWidth() / 2), yMid);
			if (yTrans == 0) {
				movVertical = BAJANDO;
				contSubiendo = 0;
			} else {
				trasladar(0, -yTrans);
				yMundo -= yTrans;
				contSubiendo++;
			}
		}
	}

	private void updateCayendo() {
		int yTrans = ladrillin.checkTopeLadrillo(xMundo + (getWidth() / 2),
				yMundo + getHeight() + distVertical, distVertical);
		if (yTrans == 0)
			finalizaSalto();
		else {
			trasladar(0, yTrans);
			yMundo += yTrans;
		}
	}

	private void finalizaSalto() {
		movVertical = SIN_SALTO;
		contSubiendo = 0;
		if (quieto) {
			if (derecha)
				setImage("derecha");
			else
				setImage("izquierda");
		}
	}
}
