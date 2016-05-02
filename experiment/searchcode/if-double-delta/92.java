import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

/**
 * 
 */

/**
 * @author Fabio S Monteiro
 *
 */

abstract class Revestimento {
	private Anel anelPai;
	private final int id; /* Indice do Revestimento no Anel pai */
	private final double comprimentoAcima;
	private final double comprimentoAbaixo;
	private final double comprimentoLateral;
	private double temperatura;
	private double temperaturaAnterior;
	private Revestimento vizinhoAnterior;
	private Revestimento vizinhoPosterior;
	
	
	public Revestimento(Anel anelPai, int id, double comprimentoAcima, double comprimentoAbaixo, double comprimentoLateral, double temperaturaInicial) {
		this.anelPai = anelPai;
		this.id = id;
		this.comprimentoAcima = comprimentoAcima;   
		this.comprimentoAbaixo = comprimentoAbaixo;  
		this.comprimentoLateral = comprimentoLateral;
		this.temperaturaAnterior = temperaturaInicial;
	}
	
	public double getTemperaturaMediaAnterior() {
		if (anelPai.getAnelSuperior() == null) {
			//Calota
			return (anelPai.getAnelInferior().getTemperaturaMedia() + 
				       temperaturaAnterior) / 2;			
		}
		if (anelPai.getAnelInferior() == null) {
			//Ultimo anel
			return (anelPai.getAnelSuperior().getTemperaturaMedia() * comprimentoAcima) + 
				       ((vizinhoAnterior.temperaturaAnterior + vizinhoPosterior.temperaturaAnterior) * comprimentoLateral)
				       / (comprimentoAcima + 2 * comprimentoLateral);
		}
		return (anelPai.getAnelSuperior().getTemperaturaMedia() * comprimentoAcima) + 
			   (anelPai.getAnelInferior().getTemperaturaMedia() * comprimentoAbaixo) + 
		       ((vizinhoAnterior.temperaturaAnterior + vizinhoPosterior.temperaturaAnterior) * comprimentoLateral)
		       / (comprimentoAcima + comprimentoAbaixo + 2 * comprimentoLateral);
	}

	public void commitTimeStep() {
		temperaturaAnterior = temperatura;
	}

	public Revestimento getVizinhoAnterior() {
		return vizinhoAnterior;
	}

	public void setVizinhoAnterior(Revestimento vizinhoAnterior) {
		this.vizinhoAnterior = vizinhoAnterior;
	}

	public Revestimento getVizinhoPosterior() {
		return vizinhoPosterior;
	}

	public void setVizinhoPosterior(Revestimento vizinhoPosterior) {
		this.vizinhoPosterior = vizinhoPosterior;
	}

	public double getTemperatura() {
		return temperatura;
	}

	public void setTemperatura(double temperatura) {
		this.temperatura = temperatura;
	}

	public double getTemperaturaAnterior() {
		return temperaturaAnterior;
	}

	public void setTemperaturaAnterior(double temperaturaAnterior) {
		this.temperaturaAnterior = temperaturaAnterior;
	}

	public Anel getAnelPai() {
		return anelPai;
	}

	public int getId() {
		return id;
	}

	public double getComprimentoAcima() {
		return comprimentoAcima;
	}

	public double getComprimentoAbaixo() {
		return comprimentoAbaixo;
	}

	public double getComprimentoLateral() {
		return comprimentoLateral;
	}
	
	
	
}

class Rejunte extends Revestimento {

	public Rejunte(Anel anelPai, int id, double comprimentoAcima, double comprimentoAbaixo, double comprimentoLateral, double temperaturaInicial) {
		super(anelPai, id, comprimentoAcima, comprimentoAbaixo, comprimentoLateral, temperaturaInicial);
	}
	
	public Rejunte(Pastilha p) {
		super(p.getAnelPai(), p.getId(), p.getComprimentoAcima(), p.getComprimentoAbaixo(), p.getComprimentoLateral(), p.getTemperaturaAnterior());
		setTemperatura(p.getTemperatura());
		this.setVizinhoAnterior(p.getVizinhoAnterior());
		this.setVizinhoPosterior(p.getVizinhoPosterior());
	}	
}

class Pastilha extends Revestimento {
	public static double tethaCritica;
	private double[] normal;
	
	
	public Pastilha(Anel anelPai, int id, double comprimentoD, double comprimentoL, double temperaturaInicial, double[] normal) {
		super(anelPai, id, comprimentoD, comprimentoD, comprimentoL, temperaturaInicial);
		this.normal = normal;
	}
	
	public double[] getNormal() {
		return normal;
	}

	@Override
	public void setTemperatura(double temperatura) {
		super.setTemperatura(temperatura);
		if (temperatura > Pastilha.tethaCritica) 
			explode();
	}
	
	protected void explode() {
		Rejunte r = new Rejunte(this);
		this.getVizinhoAnterior().setVizinhoPosterior(r);
		this.getVizinhoPosterior().setVizinhoAnterior(r);
		getAnelPai().getElementos()[getId()] = r;
	}
	
	
}

class Calota extends Pastilha {
	
	public Calota(Anel anelPai, double temperaturaInicial, double[] normal) {
		super(anelPai, 0, -1.0, -1.0, temperaturaInicial, normal);
	}
	
	@Override
	protected void explode() {
		Rejunte r = new Rejunte(this);
		getAnelPai().getElementos()[getId()] = r;
	}
	
}


class Anel {
	private final double z0;
	private final double z1;
	private Revestimento[] elementos;
	private double temperaturaMedia;
	private Anel anelSuperior = null;
	private Anel anelInferior = null;
	
	public Anel(double z0, double z1, Capsula capsula, double temperaturaInicial) {
		this.z0 = z0;
		this.z1 = z1;
		if (z0 > 0) {
			double circunferencia0 = (2 * Math.PI * Math.sqrt(z0 / capsula.getA()));
			double circunferencia1 = (2 * Math.PI * Math.sqrt(z1 / capsula.getA()));
			int numPastilhas = (int) (circunferencia0 / capsula.getD());
			double tamanhoLadoSuperiorRejuntes = (circunferencia0 - (capsula.getD()*numPastilhas)) / capsula.getD();
			double tamanhoLadoInferiorRejuntes = (circunferencia1 - (capsula.getD()*numPastilhas)) / capsula.getD();
			double tamanhoLadoLateralRejuntes = Math.sqrt(Math.pow(z1-z0, 2.0) + Math.pow(((tamanhoLadoInferiorRejuntes - tamanhoLadoSuperiorRejuntes)/2), 2.0));
		
	        // raios
	        double r0 = Math.sqrt(z0 / capsula.getA());
	        double r1 = Math.sqrt(z1 / capsula.getA());

	        // angulo de cada pastilha
	        double alpha0 = 2 * Math.asin((capsula.getD()/2) / r0 );
	        double alpha1 = 2 * Math.asin((capsula.getD()/2) / r1 );

	        // angulo da direfença entre cada pastilha (anel inferior e superior)
	        double beta0 = ((2 * Math.PI) - (numPastilhas * alpha0)) / numPastilhas;
	        double beta1 = ((2 * Math.PI) - (numPastilhas * alpha1)) / numPastilhas;

	        
			elementos = new Revestimento[2 * numPastilhas];
			for (int x = 0; x < elementos.length; x++) {
				if (x % 2 == 0) {
					//As células pares são pastilhas inicialmente, enquanto células impares são rejuntes
					int j = x/2; //Número da pastilha
		            double x0 = r0 * Math.cos( j * (alpha0 + beta0) );
		            double y0 = r0 * Math.sin( j * (alpha0 + beta0) );
	
		            double X0 = r0 * Math.cos( (j+1) * (alpha0 + beta0) );
		            double Y0 = r0 * Math.sin( (j+1) * (alpha0 + beta0) );
	
		            //double x1 = r1 * Math.cos( j * (alpha1 + beta1) );
		            //double y1 = r1 * Math.sin( j * (alpha1 + beta1) );
	
		            double X1 = r1 * Math.cos( (j+1) * (alpha1 + beta1) );
		            double Y1 = r1 * Math.sin( (j+1) * (alpha1 + beta1) );
		            double[] v1 = {x0, y0, z0};
		            double[] v2 = {X0, Y0, z0};
		            //double[] v3 = {x1, y1, z1};
		            double[] v4 = {X1, Y1, z1}; 
		            
		            double[] p = Util.produtoVetorial(v2, v1);
		            double[] q = Util.produtoVetorial(v4, v1);
		            
		            double [] normal = {p[1]*q[2] - p[2]*q[1], p[2]*q[0] - p[0]*q[2], p[0]*q[1] - p[1]*q[0]};
					
					elementos[x] = new Pastilha(this, x, capsula.getD(), z1-z0, temperaturaInicial, normal);
				} else
					elementos[x] = new Rejunte(this, x, tamanhoLadoSuperiorRejuntes, tamanhoLadoInferiorRejuntes, tamanhoLadoLateralRejuntes, temperaturaInicial);
				if (x > 0) {
					elementos[x].setVizinhoAnterior(elementos[x-1]);
					elementos[x-1].setVizinhoPosterior(elementos[x]);
				}
				if (x == (elementos.length -1)) {
					elementos[0].setVizinhoAnterior(elementos[x]);
					elementos[x].setVizinhoPosterior(elementos[0]);					
				}
				
			}
		} else {
			elementos = new Revestimento[1];
			double [] normal = {capsula.getP()[0], - capsula.getP()[1], capsula.getP()[2]};
			elementos[0] = new Calota(this, temperaturaInicial, normal);
		}
	}
	
	public Revestimento[] getElementos() {
		return elementos;
	}
	
	public void commitTimeStep() {
		double temp = 0.0;
		for (Revestimento r : elementos) {
			temp += r.getTemperatura();
			r.commitTimeStep();
		}
		temperaturaMedia = temp / elementos.length;
	}

	public Anel getAnelSuperior() {
		return anelSuperior;
	}

	public void setAnelSuperior(Anel anelSuperior) {
		this.anelSuperior = anelSuperior;
	}

	public Anel getAnelInferior() {
		return anelInferior;
	}

	public void setAnelInferior(Anel anelInferior) {
		this.anelInferior = anelInferior;
	}

	public double getTemperaturaMedia() {
		return temperaturaMedia;
	}

	public void setTemperaturaMedia(double temperaturaMedia) {
		this.temperaturaMedia = temperaturaMedia;
	}
	
}

class Capsula {
	private final double h;
	private final double a;
	private final double d;
	private final double[] v;
	private final double[] p;
	private Anel[] aneis;
	
	public Capsula(double h, double a, double d, double[] v, double[] p, double temperaturaInicial) {
		super();
		this.h = h;
		this.a = a;
		this.d = d;
		this.v = v;
		this.p = p;
		double l = Math.pow((6 * d) / (2 * Math.PI), 2.) * a;
		int quantidadeAneis = (int) (h / l);
		aneis = new Anel[quantidadeAneis];
		for (int x = 0; x < quantidadeAneis; x++) {
			aneis[x] = new Anel(x*l, (x+1)*l, this, temperaturaInicial);
			if (x > 0) {
				aneis[x].setAnelSuperior(aneis[x-1]);
				aneis[x-1].setAnelInferior(aneis[x]);
			}
		}
	}

	public void commitTimeStep() {
		for (Anel a : aneis) {
			a.commitTimeStep();
		}
	}	
	
	public double getH() {
		return h;
	}


	public double getA() {
		return a;
	}

	public double getD() {
		return d;
	}

	public double[] getV() {
		return v;
	}

	public double[] getP() {
		return p;
	}

	public Anel[] getAneis() {
		return aneis;
	}

	
	
}

class ControllerSimulacao {
	
	private final long iteracoes;
	private final Capsula capsula;
	private final double alfa;
	private final double delta;
	private final double t0;
	
	public ControllerSimulacao(double alfa, double delta, double t0, long iteracoes, Capsula capsula) {
		this.iteracoes = iteracoes;
		this.capsula = capsula;
		this.alfa = alfa;
		this.delta = delta;
		this.t0 = t0;
		calculaIteracoes();
	}
	
	private double atrito(long t, Pastilha pastilha) {
		double vn = Util.produtoEscalar(capsula.getV(), pastilha.getNormal());
		if (vn > 0) {
			return alfa * vn * Math.atan(Math.pow(t - t0, 2.0));
		} else
			return 0.0;
	}
	
	private double dissipacao(Pastilha r) {
		return delta * Util.produtoEscalar(capsula.getV(), r.getNormal());
	}
	
	private void calculaIteracoes() {
		for (long i = 1; i <= iteracoes; i++) {
			
			for (Anel anel: capsula.getAneis()) {
				for (Revestimento r: anel.getElementos()) {
					if (r instanceof Rejunte) {
						r.setTemperatura(r.getTemperaturaMediaAnterior());
					} else {
						r.setTemperatura(
								r.getTemperaturaMediaAnterior() + atrito(i, (Pastilha)r) - dissipacao((Pastilha)r)
								);
					}					
				}
			}
			capsula.commitTimeStep();
		}

	}	
	
	
}


class Util {

	public static double produtoEscalar(double V1[], double V2[]) {
		if (V1.length != V2.length)
			return 0.0;
	    double res = 0;
	    for (int i=0; i < V1.length; i++)
	        res = res + V1[i] * V2[i];
	    return res;
	}

	public static double[] produtoVetorial(double A[], double B[]) {
	    int x = 0, y = 1, z = 2;
	    double[] res = new double[3];
	    res[x] = A[y]*B[z] - A[z]*B[y];
	    res[y] = A[z]*B[x] - A[x]*B[z];
	    res[z] = A[x]*B[y] - A[y]*B[x];
	    return res;
	}	
}

public class Ep1 {
	private static double h; /* Altura da base até o vértice */
	private static double a; /* Fator de forma da cápsula */
	private static double alfa; /* Parâmetro da função de atrito */
	private static double t0; /* Parâmetro da função de atrito */
	private static double delta; /* Parametro da funcao de atrito */
	private static double tethaCritica; /* Temperatura em que uma pastilha se desintegra */
	private static double temperaturaInicial; /* Temperatura inicial das pastilhas */
	private static double d; /* Lado da pastilha */
	private static double[] p = new double[3]; /*vetor p*/
	private static double[] v = new double[3]; /*vetor v*/

	private static long iteracoes; /* Nr de iterações */

	private static String nomeArquivoEntrada; /* Nome do arquivo de entrada */
	private static String nomeArquivoSaida; /* Nome do arquivo de saída */
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) { /* Não foi passado nenhum parâmetro */
			imprimeUso();
			System.exit(0);
		}
		if ((args.length == 1) && (args[0].equals("--help"))) { /* passado apenas parâmetro --help (ajuda)*/
			imprimeUso();
			System.exit(0);
		}
		if (verificaParametros(args) < 0) {
			/* Encontrados erros nos parâmetros recebidos.
			 * Mensagens de erro emitidas por verificaParametros().
			 * Saindo.
			 */
			System.exit(-1);
		}
		
		Pastilha.tethaCritica = tethaCritica;
		Capsula c = new Capsula(h, a, d, v, p, temperaturaInicial);
		new ControllerSimulacao(alfa, delta, t0, iteracoes, c);
		gravarArquivo(c);
	}


	public static void imprimeUso() {
		System.out.println("Uso: capsula [opções] -i <nome_arquivo_entrada> | todos os parâmetros  [-o <nome_arquivo_saida>]");
		System.out.println("");
		System.out.println("   Deve ser fornecido um nome de arquivo de entrada contendo os parâmetros ou então:");
		System.out.println("     -h Altura da capsula");
		System.out.println("     -a Fator de forma da capsula");
		System.out.println("     -d Medida lateral das pastilhas");
		System.out.println("     -A parâmetro alfa da função de atrito");
		System.out.println("     -t parâmetro t0 da função de atrito");
		System.out.println("     -D parâmetro da função de dissipação");
		System.out.println("     -M temperatura na qual a pastilha desintegra");
		System.out.println("     -P Vetor tridimensional de posição");
		System.out.println("     -V Vetor tridimensional de velocidade");
		System.out.println("     -r Número de iterações");
		System.out.println("");
		System.out.println("   Opções disponíveis:");
		System.out.println("     --help  Imprime esta ajuda e termina o programa");
		System.out.println("");
		System.out.println("   Observações:");
		System.out.print("      Caso seja fornecido um arquivo de entrada e também alguns outros parâmetros, ");
		System.out.println("os valores passados na linha de comando terão precedência sobre os do arquivo");
		System.out.println("");
		System.out.println("Universidade de São Paulo - Instituto de Matemática e Estatística");
		System.out.println("Exercício de Programação 1 / 2011 - MAC 5742 Programação Paralela");
		System.out.println("Professor Gubi ");
		System.out.println("Autores: Fábio de Salles Monteiro ");
		System.out.println("-----------------------------------------------------------------------------");
	} /* Fim imprimeUso() */

	private static int verificaParametros(String params[]) {
		short j, i = 0;
		int numParams = params.length;
		while (i < numParams) {
			j = i;

			if (params[i].equals("-i")) {
				nomeArquivoEntrada = params[++i];
				if (lerArquivoEntrada() < 0 ) {
					System.out.println("Ocorreu um erro ao ler o arquivo de entrada. \nVerifique se você possui os direitos apropriados e se o arquivo está no formato correto.");
					return -1;
				}
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-h"))) {
				h = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-a"))) {
				a = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-d"))) {
				d = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-A"))) {
				alfa = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-t"))) {
				t0 = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-D"))) {
				delta = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-M"))) {
				tethaCritica = Double.parseDouble(params[++i]);
				i++;
			}
			if ((i < (numParams - 1)) && (params[i].equals("-r"))) {
				iteracoes = Long.parseLong(params[++i]);
				i++;
			}
			/* Falta p e v - vetores tridimensionais */

			if ((i < (numParams - 1)) && (params[i].equals("-o"))) {
				nomeArquivoSaida = params[++i];
				i++;
			}
			if ((i < numParams) && (params[i].equals("--help"))) {
				imprimeUso();
				i++;
			}
			if (i == j) {
				System.out
						.println("O parâmetro "
								+ params[i]
								+ " não foi reconhecido como válido.\nUtilize a opção --help para ajuda");
				return -1;
			}
		}

		return 0;
	} /* Fim verificaParametros */


	private static int lerArquivoEntrada() {

		try {
			FileReader fr = new FileReader(nomeArquivoEntrada);
			StreamTokenizer st = new StreamTokenizer(fr);
			st.nextToken();
			h = st.nval;
			st.nextToken();
			a = st.nval;
			st.nextToken();
			d = st.nval;
			st.nextToken();
			alfa = st.nval;
			st.nextToken();
			t0 = st.nval;
			st.nextToken();
			delta = st.nval;
			st.nextToken();
			tethaCritica = st.nval;
			st.nextToken();
			temperaturaInicial = st.nval;
			st.nextToken();
			p[0] = st.nval;
			st.nextToken();
			p[1] = st.nval;
			st.nextToken();
			p[2] = st.nval;
			st.nextToken();
			v[0] = st.nval;
			st.nextToken();
			v[1] = st.nval;
			st.nextToken();
			v[2] = st.nval;
			st.nextToken();
			iteracoes = (long) st.nval;

		} catch (Exception e) {
			//e.printStackTrace();
			return -1;
		}
		return 0;
	}	
	
	private static int gravarArquivo(Capsula capsula) {
		try {
			PrintWriter pwArqSaida = new PrintWriter(new FileWriter(nomeArquivoSaida));
			pwArqSaida.print(a);
			pwArqSaida.print(" ");
			pwArqSaida.print(h);
			pwArqSaida.print(" ");
			pwArqSaida.println(d);
			pwArqSaida.println(capsula.getAneis()[0].getElementos()[0].getTemperatura()); // Aqui vai a temperatura da calota ou do rejunte se ela estourou
			pwArqSaida.println(capsula.getAneis().length - 1); //O primeiro é a calota
			for (int x = 1; x < capsula.getAneis().length; x++) {
				pwArqSaida.print(capsula.getAneis()[x].getElementos().length / 2);
				pwArqSaida.print(" ");
				for (int j = 0; j < capsula.getAneis()[x].getElementos().length; j+=2) {
					pwArqSaida.printf("%.4f", capsula.getAneis()[x].getElementos()[j].getTemperatura()); 
					pwArqSaida.print(" ");
				}
				pwArqSaida.println();
			}
			pwArqSaida.close();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}	
	
}

