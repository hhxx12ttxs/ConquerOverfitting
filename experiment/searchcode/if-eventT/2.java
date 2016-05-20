package engcomp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;


/**
 * Classe que implementa um manual de ajuda para o GraphTool.
 * Classe que extende JFrame e implementa KeyListener.
 * */
public class ManualAjuda extends JFrame implements KeyListener{
  public static final long serialVersionUID = 1;
  
  /** Tьtulos dos manuais. */
  private String TITLES[] =
  {"Manual","GraphTool","Skin","Remover componentes",
   "Caminhamento","Profundidade","Largura",
   "Kruskal","Prim Jarnik", "Dijkstra","Exemplo","Sobre"};//Total = 12
  
  /** Array com o objetivo de conter referЖncias para os textos. */
  private String MANUAIS[]; 
  
  /** ┴rea de exibiусo dos manuais. */
  private JTextArea texto;
  
  /** Barra para conter as ferramentas de aусo.*/
  private JToolBar barra;
  
  /** Campo de escrita das palavras-chave a serem buscadas. */
  private JTextField busca;
  
  /** Botсo de execuусo da busca. Utilizado quando a busca nсo ж dinРmica.*/
  private JButton OK;
  
  /** Lista de manuais que contжm a palavra-chave buscada. */
  private JComboBox select;
  
  /** Variрvel de estado que informa se a busca ж dinamica(<code>true</code>) ou nсo(<code>false</code>). */
  private boolean dinamico;
  
  /** Construtor sem argumentos. Chama o segundo construtor 
   * informando que a busca nсo ж dinamica (por default).
   * */
  public ManualAjuda(){
	this(false);
  }
  
  /** Segundo construtor. Recebe um booleano como argumento para 
   * especificar se a busca do manual ж dinamica ou nсo. 
   * @param din Argumento informando se o manual ж dinРmico(true) ou nсo(false).
   * */
  
  public ManualAjuda(boolean din){
	  super("Manual de ajuda do GraphTool");
	  setLayout(new BorderLayout());
	  MANUAIS = new String[TITLES.length];
	  dinamico = din;
	  
	  /*--INICIO::Textos dos manuais--*/
	  int pos = 0;

	  //"Manual"
	  MANUAIS[pos++] = new String (
	  "Manual de ajuda:\n\n" +
	  " Digite, no campo de busca, uma palavra chave para a sua dЩvida\n" +
	  "e clique em OK logo em seguida.\n" +
	  "Apзs isso, selecione na lista Я direita do botсo uma das opушes\n" +
	  "para verificar se consta a resposta para sua pergunta.\n");

	  //"GraphTool"
	  MANUAIS[pos++] = new String(
	  " O GraphTool se trata de um trabalho universitрrio com objetivos acadЖmicos" +
	  "para se trabalhar visualmente com grafos. Podendo-se construir grafos e \n" +
	  "trabalhar-se com a utilizaусo de diversos algoritmos.\n\n" +
	  "   - Sendo eles para: \n" +
	  "      + Caminhamento em grafos\n" +
	  "         * Caminhamento em profundidade\n" +
	  "         * Caminhamento em largura\n" +
	  "      + Construусo da рrvore mьnima\n" +
	  "         * Kruskal\n" +
	  "         * Prim-Jarnik\n" +
	  "      + Caminho mьnimo\n" +
	  "         * Dijkstra\n\n" +
	  " Esta ferramenta se trata de um trabalho universitрrio com\n" +
	  "tЖm objetivos acadЖmicos.\n");
	  
	  //"Skin"
	  MANUAIS[pos++] = new String(
	  " Os Skins sсo as aparЖncias que a interface do GraphTool pode tomar.\n" +
	  "As possibilidades podem mudar de mрquina para mрquina, portanto o GraphTool\n" +
	  "identifica automaticamente quais skins estсo instalados na mрquina em que\n" +
	  "estр sendo executado.\n" +
	  " Para optar dentre os Skins possьveis:  \n\n" +
	  "- Na barra superior, selecione \"GraphTool >> Skins >> [Opусo Desejada]\"\n");
	  
	  //"Remover componentes"
	  MANUAIS[pos++] = new String(
	  "Removendo componentes:\n" +
	  "╔ possьvel realizar a remoусo individual de componentes do grafo,\n" +
	  "todos os itens de uma mesma espжcie ou limpar todo o grafo de uma vez.\n\n" + 
 	  "Para remover um Щnico componente(uma aresta ou um vжrtice):\n" +
 	  "    - Estando na aba do \"Grafo Original\".\n" +
	  "	   - Na barra de ferramentas, clique no botсo \"Seleciona\" e,\n" + 
	  "	     em seguida, clique no componente que vocЖ deseja remover.\n" +
	  "    - Entсo pressione o botсo \"Remover\", tambжm da barra de ferramentas.\n\n" +
	  "Para remover todos os componentes de uma mesma\n" +
	  "espжcie(todas as arestas ou todos os vжrtices):\n" +
      "    - Na barra superior, selecione\n" +
      "      \"GraphTool >> Grafo Original >> Limpar [Espжcie a ser removida]\"\n\n" +
      "Para remover todos os componentes do grafo(todas as arestas e vжrtices):\n" +
      "    - Na barra superior, selecione \"GraphTool >> Grafo Original >> Limpar todo o Grafo\"\n");
	  
	  //"Caminhamento"
	  MANUAIS[pos++] = new String(
	  " Um caminhamento ж um procedimento sistemрtico para explorar\n" +
	  "um grafo examinando todos os seus vжrtices e arestas. Este ж\n" +
	  "considerado eficiente quando visita todos os vжrtices e     \n" +
	  "arestas em tempo proporcional a seu nЩmero, ou seja, em tem-\n" +
	  "po linear.\n"
	  );
	  
	  //"Profundidade"
	  MANUAIS[pos++] = new String(
	  " O caminhamento em profundidade pode ser utilizado em grafos\n" +
	  "nсo dirigidos para uma variedade de tarefas, incluindo      \n" +
	  "encontrar um caminho de um vжrtice Я outro.\n \n\n" +
	  " Para realizar o caminhamento em profundidade sobre um grafo\n" +
	  "existente, deve-se:\n" +
	  "  - Selecionar um vжrtice inicial na aba \"Grafo Original\".  \n" +
	  "  - Clicar em algum botсo de execuусo do caminhamento.        \n" +
	  "  - Observar o resultado na aba \"Grafo de saьda(gerado)\"    \n \n\n" +
	  "      Veja tambжm sobre: CAMINHAMENTO, LARGURA.\n"
	  );
	  
	  //"Largura"
	  MANUAIS[pos++] = new String(
	  " Para realizar o caminhamento em largura sobre um grafo\n" +
	  "existente, deve-se:\n" +
	  "  - Selecionar um vжrtice inicial na aba \"Grafo Original\".  \n" +
	  "  - Clicar em algum botсo de execuусo do caminhamento.        \n" +
	  "  - Observar o resultado na aba \"Grafo de saьda(gerado)\"    \n"
      );

	  //"Kruskal"
	  MANUAIS[pos++] = new String(
	  " Para construir a рrvore de cobertura mьnima utilizando-se o\n" +
	  "algoritmo de Kruskal:\n" +
	  "  - Clicar em algum botсo de execuусo do algoritmo.\n" +
	  "  - Observar o resultado na aba \"Grafo de saьda(gerado)\" \n"
	  );
	  
	  //"Prim Jarnik"
	  MANUAIS[pos++] = new String(
	  " Para construir a рrvore de cobertura mьnima utilizando-se o\n" +
	  "algoritmo de Prim-Jarnik:\n" +
	  "  - Selecionar um vжrtice inicial na aba \"Grafo Original\".  \n" +
	  "  - Clicar em algum botсo de execuусo do algoritmo.\n" +
	  "  - Observar o resultado na aba \"Grafo de saьda(gerado)\" \n"
	  );
	  
	  //"Dijkstra"
	  MANUAIS[pos++] = new String(
	  " Para construir a рrvore de cobertura mьnima utilizando-se o\n" +
	  "algoritmo de Dijkstra:\n" +
	  "  - Selecionar um vжrtice inicial na aba \"Grafo Original\".  \n" +
	  "  - Clicar em algum botсo de execuусo do algoritmo.\n" +
	  "  - Observar o resultado na aba \"Grafo de saьda(gerado)\" \n"
      );
	  
	  //"Exemplo"
	  MANUAIS[pos++] = new String(
	  "Carregando grafo de exemplo:\n\n" +
	  " Na aba \"Bem-vindo\", ж possьvel carregar um grafo de exemplo para que\n" +
	  "vocЖ possa interagir e ter um melhor entendimento sobre o funcionamento\n" +
	  "do GraphTool.\n");
	  
	  //"Sobre"
	  MANUAIS[pos++] = new String(
	  "##################################################\n" +
	  "        [ GraphTool :: DreamSoft :: UFRN ]        \n" +
	  "##################################################\n" +
	  "   + Algoritmos de caminhamento em grafos         \n" +
	  "       - Profundidade                             \n" +
	  "       - Largura                                  \n" +
	  "   + Construусo da рrvore mьnima                  \n" +
	  "       - Kruskal                                  \n" +
	  "       - Prim-Jarnik                              \n" +
	  "   + Caminho de custo mьnimo                      \n" +
	  "       - Dijkstra                                 \n" +
	  "##################################################\n" +
	  " UFRN - DIMAP - LAEDII - Prof. Demзstenes de Sena \n" +
      "   Estudantes:                                    \n" +
	  "              + Arthur Diego                      \n" +
	  "              + Hundson Thiago                    \n" +
	  "              + Tyago Medeiros                    \n" +
	  "   Endereco:                                      \n" +
	  "       http://www.lcc.ufrn.br/~tyago/GraphTool    \n" +
	  "##################################################" 		
	  );

	  
	  
	  /*--FIM::Texto dos Manuais--*/
	  
	  texto = new JTextArea(MANUAIS[0]);
	  texto.setEnabled(false);
	  texto.setBackground(Color.DARK_GRAY);
	  texto.setDisabledTextColor(Color.LIGHT_GRAY);
	  barra = new JToolBar();
	  
	  busca = new JTextField("Busca...");
	  select = new JComboBox(TITLES);
	  
	  OK = new JButton("OK");
	  
	  OK.addActionListener(
		new ActionListener(){
			public void actionPerformed(ActionEvent event){
				select.removeAllItems();
				for(int i=0;i<TITLES.length;i++){
				 if(MANUAIS[i].contains( busca.getText() )){
					select.addItem(TITLES[i]);
					select.repaint();
					barra.repaint();
				 }
				}
			}
		}	  
	  );
	  
	  
	  select.addItemListener(new ItemListener(){
		  public void itemStateChanged(ItemEvent iEvent){
			  if(iEvent.getStateChange() == ItemEvent.SELECTED ){
				  Object it = select.getSelectedItem();
				  texto.setText( manual(it) );
				  texto.repaint();
			  }
		  }
	  });
	  
	  
	  barra.add(busca);
	  barra.add(OK);
	  barra.addSeparator();
	  barra.add(select);
	  
	  //Adicionando eventos de teclado Я caixa de busca
	  busca.addKeyListener(this);
	  
	  add(barra,BorderLayout.NORTH);
	  add(texto,BorderLayout.CENTER);
	  
	  setSize(640,480); //= (80%) da (janela do GraphTool) 
	  setResizable(true);
	  setVisible(true);
  }	

  /** Funусo auxiliar para identificar um manual atravжs do seu tьtulo.
   * @param title Referencia para um dos titulos de manuais.
   * @return Referencia para o manual correspondente ao titulo.
   * */
  private String manual(Object title){
	  for(int i=0;i<TITLES.length;i++){
		  if(title ==  TITLES[i] ){ 
			  return MANUAIS[i]; 
		  }
	  }
	  return (new String("Manual inexistente"));
  }
   /*eventos de teclado*/
  
  
  	/** Metodo invocado automaticamente quando o teclado ж pressionado
  	 * @param eventT Evento do teclado.
  	 * */
	  public void keyPressed(KeyEvent eventT){
		  if(dinamico || eventT.getKeyCode() == KeyEvent.VK_ENTER){
				select.removeAllItems();
				for(int i=0;i<TITLES.length;i++){
				 if(MANUAIS[i].contains( busca.getText() )){
					select.addItem(TITLES[i]);
					select.repaint();
					barra.repaint();
				 }
				}
		  }
	  }	

	  /** Metodo invocado automaticamente quando a tecla pressionada ж solta.
	   * Nсo utilizado. Estр aqui porque ж obrigatзrio.
	   * @param eventT Evento do teclado.
	   * */
	  public void keyReleased(KeyEvent eventT){ }

	  /** Mжtodo invocado automaticamente quando alguma tecla de aусo ж pressionada.
	   * @param eventT Evento do teclado.
	   * */
	  public void keyTyped(KeyEvent eventT){
		  keyPressed(eventT);
	  }

  
}

