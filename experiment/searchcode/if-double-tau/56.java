class Link {
    
    	private final double ALPHA   = 1.0;
	private final double BETA    = 2.0;
	private final double INITTAU = 0.1; // is this a good value?
	private final double RHO     = 0.5;
	private final double MINLEN  = 0.01;

	private final Node nodeS;
	private final Node nodeT;
	private double     tau;
	private double     deltaTau;

	public Link(Node nodeS, Node nodeT) {
                // Verifica si el nodo inicial y final del enlace no son nulos
		if (nodeS == null) throw new NullPointerException("nodeS is null");
		if (nodeT == null) throw new NullPointerException("nodeT is null");
                // Verifica si ambos nodos pertenecer a la misma red
		Contract.require("same Network", nodeS.getNet() == nodeT.getNet());
                // Verifica si el "id" del nodo inicial es menor al del nodo final del enlace
		Contract.require("nodeIdS < nodeIdT", nodeS.getId() < nodeT.getId());
		this.nodeS  = nodeS;
		this.nodeT  = nodeT;
		tau         = INITTAU;
		deltaTau    = 0.0;
	}
        // Retorna el nodo inicial del enlace (objeto)
	public final Node getNodeS() {
		return(nodeS);
	}
        // Retorna el nodo final del enlace (objeto)
	public final Node getNodeT() {
		return(nodeT);
	}
        // Calcula y retorna la distancia euclidiana entre en nodo inicial y de destino
	public double length() {
                // Obtiene y almacena las coordenadas de dichos nodos, inicial y final
		double x1  = nodeS.getX();
		double y1  = nodeS.getY();
		double x2  = nodeT.getX();
		double y2  = nodeT.getY();
		double len = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
                // Checkea que el largo (distancia) obtenida sea mayor o igual a 0.0
		Contract.ensure("len >= 0.0", len >= 0.0);
		return(len);
	}
        // Retorna el valor de la variable "tau"
	public synchronized final double getTau() {
                // Checkea que esta constante sea mayor o igual a 0.0
		Contract.ensure("tau >= 0.0", tau >= 0.0);
		return(tau);
	}
        // Reinicializa el valor de la variable "tau" con el valor constante "INITTAU"
        // definido para el algoritmo
	public synchronized final void resetTau() {
		tau = INITTAU;
	}

	public void update() {
		tau      = RHO * getTau() + deltaTau;
		deltaTau = 0.0;
	}

	public void deposit(double d) {
		Contract.require("d > 0.0", d > 0.0);
		deltaTau += d;
	}

	public double weight() {
		double len = length();
		len = (len >= MINLEN) ? len : MINLEN;
		return(Math.pow(tau, ALPHA) * Math.pow((1.0/len), BETA));
	}

        public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Link(").append(nodeS.getId());
		buf.append(",").append(nodeT.getId());
		buf.append(";").append(length());
		buf.append(",").append(getTau());
		buf.append(",").append(deltaTau);
		buf.append(")");
		return(buf.toString());
	}
}

