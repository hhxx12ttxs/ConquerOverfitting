package Implementacion;

import Entidades.Regalo;
import Implementaciones.ColaPrioridad;
import Implementaciones.Conjunto;
import Implementaciones.Matriz;
import Implementaciones.Vector;
import Interfaces.CanjearPuntosInterface;
import TDA.ConjuntoTDA;
import TDA.MatrizTDA;
import TDA.VectorTDA;

public class CanjearPuntosInterfaceImp implements CanjearPuntosInterface {

	private int maxDim = 2;
	private int cantidadRegalos;

	@Override
	public int maximizarRegalosARetirar(ConjuntoTDA<Regalo> catalogo,
			ConjuntoTDA<Regalo> maximosRegalosResultado, int puntosAcumulados) {
		// TODO Auto-generated method stub
		Matriz<Integer> m = new Matriz<Integer>();
		m.inicializarMatriz(puntosAcumulados + 1);
		Vector<Regalo> regalos = new Vector<Regalo>();
		ConjuntoTDA<Regalo> catalogoAux = new Conjunto<Regalo>();
		catalogoAux.inicializarConjunto();
		int cantidad = 0;
		while (!catalogo.conjuntoVacio()) {
			cantidad++;
			Regalo r = catalogo.elegir();
			catalogo.sacar(r);
			catalogoAux.agregar(r);
		}
		catalogo = catalogoAux;
		cantidadRegalos = cantidad;
		regalos.inicializarVector(cantidad + 1);

		// Primer elemento de regalos tiene que ser null
		try {
			regalos.agregarElemento(0, null);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generar una ordenacion con los regalos del catalogo

		catalogoAux = new Conjunto<Regalo>();
		catalogoAux.inicializarConjunto();
		ColaPrioridad<Regalo> regalosOrdenados = new ColaPrioridad<Regalo>();
		regalosOrdenados.InicializarCola();

		while (!catalogo.conjuntoVacio()) {

			Regalo r = catalogo.elegir();
			regalosOrdenados.AgregarElemento(r, r.obtenerPrecio());
			catalogo.sacar(r);
			catalogoAux.agregar(r);

		}
		catalogo = catalogoAux;
		for (int i = 1; i < regalos.capacidadVector(); i++)
			try {
				Regalo recuperado = regalosOrdenados.RecuperarMinElemento();
				// System.out.println("Regalo recuperado minimo: "
				// + recuperado.obtenerNombre());
				regalos.agregarElemento(i, recuperado);
				regalosOrdenados.EliminarMinPrioridad();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		// Rellena ceros en primera columna matriz
		for (int i = 0; i < regalos.capacidadVector(); i++) {
			m.setearValor(i, 0, 0);
		}

		// Rellena ceros en primera columna matriz
		for (int j = 0; j < m.obtenerDimension(); j++) {
			m.setearValor(0, j, j);
			// System.out.println("Posicion matriz: [" + 0 + "," + j + "]");
			// System.out.println("Vale => [" + j + "]");
		}

		// Rellena matriz
		int val = 0;
		for (int i = 1; i < regalos.capacidadVector(); i++) {
			for (int j = 1; j < m.obtenerDimension(); j++) {
				//System.out.println("Posicion matriz: [" + i + "," + j + "]");
				try {
					boolean loLlevo = this.obtenerMinimo(m, i, j, regalos);
					if (j == m.obtenerDimension() - 1 && loLlevo) {
						maximosRegalosResultado.agregar(regalos
								.recuperarElemento(i));
						val = val
								+ regalos.recuperarElemento(i).obtenerPuntos();
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return val;
	}

	private ColaPrioridad<Regalo> ordenarRegalos(ConjuntoTDA<Regalo> catalogo) {

		ConjuntoTDA<Regalo> catalogoAux = new Conjunto<Regalo>();
		ColaPrioridad<Regalo> cola = new ColaPrioridad<Regalo>();
		cola.InicializarCola();

		while (!catalogo.conjuntoVacio()) {

			Regalo r = catalogo.elegir();
			cola.AgregarElemento(r, r.obtenerPrecio());
			catalogo.sacar(r);
			catalogoAux.agregar(r);

		}
		catalogo = catalogoAux;
		return cola;

	}

	private Regalo elegirRegaloMenorPrecio(ConjuntoTDA<Regalo> catalogoAux) {
		Regalo regaloMin = new Regalo();
		regaloMin.setearPrecio(0);
		while (!catalogoAux.conjuntoVacio()) {
			Regalo regaloAux = catalogoAux.elegir();
			if (regaloAux.obtenerPrecio() < regaloMin.obtenerPrecio()) {
				regaloMin = regaloAux;
			}
			catalogoAux.sacar(regaloAux);
		}
		return regaloMin;
	}

	private int cantidadRegalos(ConjuntoTDA<Regalo> catalogo) {
		ConjuntoTDA<Regalo> catalogoAux = new Conjunto<Regalo>();
		catalogoAux.inicializarConjunto();
		int cantidad = 0;
		while (!catalogo.conjuntoVacio()) {
			cantidad++;
			Regalo r = catalogo.elegir();
			catalogo.sacar(r);
			catalogoAux.agregar(r);
		}
		catalogo = catalogoAux;
		return cantidad;

	}

	private boolean obtenerMinimo(Matriz<Integer> m, Integer i, Integer j,
			Vector<Regalo> regalos) throws Exception {
		int minimo;
		int sumaregalos = 0;
		for (int k = 1; k <= i; k++) {
			sumaregalos = sumaregalos
					+ regalos.recuperarElemento(k).obtenerPuntos();
		}
		int p1 = j - sumaregalos;
		int loLlevo;
		if (p1 < 0)
			loLlevo = 99999;
		else
			loLlevo = m.obtenerValor(i - 1, j - sumaregalos);
		int noLoLlevo = m.obtenerValor(i - 1, j);
		if (loLlevo < noLoLlevo)
			minimo = loLlevo;
		else
			minimo = noLoLlevo;

		m.setearValor(i, j, minimo);
		// System.out.println("Vale (else) => [" + minimo + "]");

		return (loLlevo < noLoLlevo);

	}

	private int obtenerPosicionVector(Vector<Integer> puntos,
			Integer cantPuntos, Matriz<Integer> m) throws Exception {
		int posicion = 0;
		for (int i = 0; i < m.obtenerDimension(); i++) {
			if (puntos.recuperarElemento(i).equals(cantPuntos))
				posicion = i;
		}
		return posicion;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * Interfaces.CanjearPuntosInterface#obtenerTableroMultirubroBalanceado(
	 * TDA.ConjuntoTDA) PROBLEMA 2
	 */

	@Override
	public MatrizTDA<Regalo> obtenerTableroMultirubroBalanceado(
			ConjuntoTDA<Regalo> regalosARetirar) {

		MatrizTDA<Regalo> matriz = new Matriz<Regalo>();
		matriz.inicializarMatriz(2);

		MatrizTDA<Regalo> resultado = new Matriz<Regalo>();
		Vector<Regalo> regalos = new Vector<Regalo>();
		regalos.inicializarVector(cantidadRegalos);

		int i = 0;
		while (!regalosARetirar.conjuntoVacio()) {
			Regalo r = regalosARetirar.elegir();
			try {
				regalos.agregarElemento(i, r);
				regalosARetirar.sacar(r);
				i++;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		resultado = this.llamadaBacktracking(null, 0, regalos);

		if (resultado != null) {
			return resultado;
		} else {
			return null;
		}

	}

	private MatrizTDA<Regalo> llamadaBacktracking(MatrizTDA<Regalo> matriz,
			int dim, Vector<Regalo> regalos) {

		try {
			if (esSolucion(matriz)) {
				//System.out.println("Saliendo por solucion");
				return matriz;
			} else {
				if (dim < this.maxDim) {
					VectorTDA<MatrizTDA<Regalo>> compleciones = generarCompleciones(
							matriz, dim, regalos);
					for (int index = 0; index < compleciones.capacidadVector(); index++) {
						MatrizTDA<Regalo> ensayo;

						ensayo = compleciones.recuperarElemento(index);

						matriz = llamadaBacktracking(ensayo, dim + 1, regalos);
						if(matriz != null){
							return matriz;
						}
						else
						{
							
						}
						
					}
				} else {
					// System.out.println("Saliendo por dimension mayor a maxDim");
					return null;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("Saliendo");
		return matriz;

	}

	private VectorTDA<MatrizTDA<Regalo>> generarCompleciones(
			MatrizTDA<Regalo> matriz, int dim, Vector<Regalo> regalos) {

		VectorTDA<MatrizTDA<Regalo>> res = new Vector<MatrizTDA<Regalo>>();

		try {

			// Dimension cero, genera una matriz de 1x1 para cada regalo
			if (dim == 0) {

				res.inicializarVector(regalos.capacidadVector());

				for (int i = 0; i < regalos.capacidadVector(); i++) {
					Matriz<Regalo> m = new Matriz<Regalo>();
					m.inicializarMatriz(1);

					m.setearValor(0, 0, regalos.recuperarElemento(i));

					res.agregarElemento(i, m);

				}
			} else {
				// Generar conjunto matrices de la siguiente dimension
				// Se van a generar !(2*dim+1) nuevas matrices
				int cantidadEjemplares = factorial(2 * dim + 1);
				int cantidad = 0;
				res.inicializarVector(cantidadEjemplares);

				while (cantidad < cantidadEjemplares) {
					Matriz<Regalo> m = new Matriz<Regalo>();
					m.inicializarMatriz(dim + 1);

					// Genero un conjunto con los regalos del vector para
					// aprovechar la aleatoriedad del TDA
					ConjuntoTDA<Regalo> conjAux = new Conjunto<Regalo>();
					conjAux.inicializarConjunto();

					for (int index = 0; index < regalos.capacidadVector(); index++) {
						conjAux.agregar(regalos.recuperarElemento(index));
					}

					// Se copian los valores de la matriz anterior
					for (int j = 0; j < dim; j++) {
						for (int k = 0; k < dim; k++) {
							Regalo r = matriz.obtenerValor(j, k);
							m.setearValor(j, k, r);
							conjAux.sacar(r);
						}
					}

					// Se rellenan las posiciones restantes con los regalos

					// Primero la ultima fila
					int uf = dim;
					for (int index = 0; index < dim + 1; index++) {
						Regalo r = conjAux.elegir();
						conjAux.sacar(r);
						m.setearValor(uf, index, r);
					}

					// Ahora la ultima columna
					int uc = dim;
					for (int index = 0; index < dim; index++) {
						Regalo r = conjAux.elegir();
						conjAux.sacar(r);
						m.setearValor(index, uc, r);
					}

					if (!enVector(res, m)) {
						res.agregarElemento(cantidad, m);
						cantidad++;
					}

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	private boolean enVector(VectorTDA<MatrizTDA<Regalo>> res, Matriz<Regalo> m)
			throws Exception {

		Outer: for (int index = 0; index < res.capacidadVector(); index++) {

			MatrizTDA<Regalo> mEnVector = new Matriz<Regalo>();
			try {
				mEnVector = res.recuperarElemento(index);
			} catch (Exception e) {
				// El vector no tiene elementos en esa posicion
				// La matriz no esta en el
				return false;
			}

			boolean sonIguales = true;

			for (int i = 0; i < mEnVector.obtenerDimension(); i++) {
				for (int j = 0; j < mEnVector.obtenerDimension(); j++) {

					// Definimos condicion de igualdad solo sobre el nombre
					// del regalo

					Regalo r1 = mEnVector.obtenerValor(i, j);
					Regalo r2 = m.obtenerValor(i, j);

					if (r1.obtenerNombre() != r2.obtenerNombre()) {
						// son diferentes, siguiente
						sonIguales = false;
						continue Outer;
					}
				}
			}

			if (sonIguales)
				return true;
		}

		return false;
	}

	private boolean esSolucion(MatrizTDA<Regalo> matriz) {
		if (matriz == null || matriz.obtenerDimension() < this.maxDim)
			return false;
		if (regalosDiferentesOk(matriz)) {
			if (sumasOk(matriz)) {
				if (rubrosOk(matriz)) {
					//System.out.println("MATRIZ VALIDA");
					return true;
				} else {
					//System.out.println("La matriz no verifica RUBROS");
				}
			} else {
				//System.out.println("La matriz no verifica SUMAS");

			}
		} else {
			//System.out.println("La matriz no verifica DIFERENTES");

		}

		return false;
	}

	private boolean rubrosOk2(MatrizTDA<Regalo> matriz) {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean rubrosOk(MatrizTDA<Regalo> matriz) {

		boolean rubrosOk = true;
		for (int i = 0; i < matriz.obtenerDimension(); i++) {
			for (int j = 0; j < matriz.obtenerDimension() - 1; j++) {
				for (int k = 1; k < matriz.obtenerDimension(); k++) {
					if (matriz.obtenerValor(i, j).obtenerNroRubro() == matriz
							.obtenerValor(i, k).obtenerNroRubro())
						return false;
				}
			}
		}
		// Verifica rubros en columnas
		for (int j = 0; j < matriz.obtenerDimension(); j++) {
			for (int i = 0; i < matriz.obtenerDimension() - 1; i++) {
				int k = 0;
				while (j + k < matriz.obtenerDimension() - 1
						&& i + k < matriz.obtenerDimension() - 1) {
					if (matriz.obtenerValor(i, j).obtenerNroRubro() == matriz
							.obtenerValor(i + k + 1, j + k + 1)
							.obtenerNroRubro())
						return false;
					k++;
				}
			}
		}
		// Verifica diagonal izquierda
		for (int i = 0; i < matriz.obtenerDimension() - 1; i++) {
			for (int j = 1; j < matriz.obtenerDimension(); j++) {
				int k = 0;
				while (j - k > 0 && i + k < matriz.obtenerDimension()) {
					if (matriz.obtenerValor(i, j).obtenerNroRubro() == matriz
							.obtenerValor(i + k + 1, j - k - 1)
							.obtenerNroRubro())
						return false;
					k++;
				}
			}
		}

		// Verifica diagonal derecha
		for (int i = 0; i < matriz.obtenerDimension() - 1; i++) {
			for (int j = 0; j < matriz.obtenerDimension() - 1; j++) {
				int k = 0;
				while (j + k < matriz.obtenerDimension() - 1
						&& i + k < matriz.obtenerDimension() - 1) {
					if (matriz.obtenerValor(i, j).obtenerNroRubro() == matriz
							.obtenerValor(i + k + 1, j + k + 1)
							.obtenerNroRubro())
						return false;
					k++;
				}
			}
		}

		return rubrosOk;
	}

	private boolean sumasOk(MatrizTDA<Regalo> matriz) {

		boolean sumasOk = true;

		int sumaPrimerFila = 0;

		for (int j = 0; j < matriz.obtenerDimension(); j++) {
			sumaPrimerFila = sumaPrimerFila
					+ matriz.obtenerValor(0, j).obtenerPrecio();
		}

		//System.out.println("Suma primera fila: " + sumaPrimerFila);

		for (int i = 1; i < matriz.obtenerDimension(); i++) {
			int sumaFila = 0;
			for (int j = 0; j < matriz.obtenerDimension(); j++) {
				sumaFila = sumaFila + matriz.obtenerValor(i, j).obtenerPrecio();
			}
			//System.out.println("Suma fila: " + sumaFila);
			if (sumaFila != sumaPrimerFila)
				return false;
		}

		int sumaPrimerColumna = 0;
		for (int i = 0; i < matriz.obtenerDimension(); i++) {
			sumaPrimerColumna = sumaPrimerColumna
					+ matriz.obtenerValor(i, 0).obtenerPrecio();
		}

		//System.out.println("Suma primera columna: " + sumaPrimerColumna);

		for (int j = 1; j < matriz.obtenerDimension(); j++) {
			int sumaColumna = 0;
			for (int i = 0; i < matriz.obtenerDimension(); i++) {
				sumaColumna = sumaColumna
						+ matriz.obtenerValor(i, j).obtenerPrecio();
			}
			//System.out.println("Suma columna: " + sumaColumna);

			if (sumaColumna != sumaPrimerColumna)
				return false;
		}
		
		int sumaPrimerDiagonal =0;
		for (int i=0;i<matriz.obtenerDimension();i++)
			sumaPrimerDiagonal= sumaPrimerDiagonal + matriz.obtenerValor(i, i).obtenerPrecio();
		
		//System.out.println("Suma primera diagonal: " + sumaPrimerDiagonal);

		
		int sumaSegundaDiagonal =0;
		for (int i=0;i<matriz.obtenerDimension();i++)
			sumaSegundaDiagonal= sumaSegundaDiagonal + matriz.obtenerValor(i, matriz.obtenerDimension()-1-i).obtenerPrecio();
		
		//System.out.println("Suma primera diagonal: " + sumaPrimerDiagonal);
		
		if (sumaPrimerFila!=sumaPrimerColumna | sumaPrimerFila != sumaPrimerDiagonal | sumaPrimerFila != sumaSegundaDiagonal)
			return false;
		
		return sumasOk;
	}

	private boolean regalosDiferentesOk(MatrizTDA<Regalo> matriz) {
		// TODO Auto-generated method stub
		return true;
	}

	private int factorial(int n) {
		int fact = 1;
		for (int i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
	}

}

