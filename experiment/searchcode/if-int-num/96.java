package edu.itszapopan.maestria.grafos;

public class Digrafo {

  private int numAristas;
  private int [][]grafo;
  private Arista []aristas;

  public Digrafo(int numAristas) {
	  
    this.numAristas = numAristas;

    grafo = new int[numAristas][numAristas];

    for (int i=0; i<numAristas; i++) {
      for (int j=0; j<numAristas; j++) {
        grafo[i][j] = 0;
      }
    }

    aristas = new Arista[numAristas];
  
  }

  public void setArista(int num, String nombre) {
    aristas[num] = new Arista(nombre);
  }

  public Arista getArista(int num) {
    return aristas[num];
  }

  public int getPosicion(String nombre) {
     int pos=-1;
     for(int i=0; i<numAristas; i++) {
        if (nombre.equalsIgnoreCase(aristas[i].getNombre())) {
	  pos = i;
	  break;
	}
     }
     return pos;
  }

  // Metodo sobrecargado
  public void setConexion(int desde, int hasta, int val) {
    grafo[desde][hasta] = val;
  }

  public void setConexion(String desde, String hasta, int val) {
    int from, to;

    from = getPosicion(desde);
    to   = getPosicion(hasta);

    if (from != -1 && to != -1) {
      grafo[from][to] = val;
    }
  }

  public int getConexion(int desde, int hasta) {
    return grafo[desde][hasta];
  }

  public void setAristaDistancia(int num, int dist) {
    aristas[num].setDistancia(dist);
  }

  public int getAristaDistancia(int num) {
    return aristas[num].getDistancia();
  }

  public void setAristaPrevio(int num, String previo) {
    aristas[num].setPrevio(previo);
  }

  public String getAristaPrevio(int num) {
    return aristas[num].getPrevio();
  }
}


