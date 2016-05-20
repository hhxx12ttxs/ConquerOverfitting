
import java.util.Scanner;

public class av3b{
	public static void main(String[] args){
		
		//iniciação de variáveis
		Scanner sc = new Scanner(System.in);
		String alph = "abcdefghijklmnopqrstuvwxyz";
		int inic = 0;
		int serv = 0;
		int iter = 0;
		
		int instante_inicial;
		int instante=0;
		boolean controlo = true;

		
		//entrada de dados
				
		System.out.print("\nInserir o total de processos: ");
		iter = sc.nextInt();
		char processo[] = new char [iter];
		double matrizdados[][] = new double[5][iter];
		char tab[] = new char[iter];
		int tab2[][] = new int[2][iter];

		//preencher a matriz
		for(int i = 0; i<iter; i++){
			System.out.print("\nProcesso "+alph.charAt(i)+", instante inicial: ");
			inic = sc.nextInt();
			System.out.print("\nProcesso "+alph.charAt(i)+" tempo de serviço: ");
			serv = sc.nextInt();
			processo[i] = alph.charAt(i);
			matrizdados[0][i] = inic; // guardar tempo de inicio na 1 linha
			matrizdados[1][i] = serv; //guardar tempo de serviço na 2 linha
			matrizdados[2][i]=((matrizdados[0][i]+matrizdados[1][i])/matrizdados[1][i]); //R para t=0 //guardar racio na 3 linha
			
		}
		
		//disp da matriz para t=0
		System.out.print("\n\n================= Dados de Entrada =================\n");
		for(int k =0; k<iter; k++){
			System.out.print("Processo " +processo[k]+" : ");
			for(int linha = 0; linha<3;linha++){
				System.out.print(matrizdados[linha][k]+" | ");
			}
			System.out.print("\n");
		}
		
		System.out.print("\n\n================= Response Ratios =================");


		//encontrar o instante inicial
		//encontrar o primeiro processo a ser executado
		int ciclo=0;

		while(ciclo<iter){
			int perc = 1;
			int index = 0;

			while(perc < iter){
				if(matrizdados[2][perc]>matrizdados[2][index] || (matrizdados[2][perc] == matrizdados[2][index] && matrizdados[0][perc]<matrizdados[0][index])){
					index = perc;
				}				
				perc++;
			}

			matrizdados[3][index] = matrizdados[2][index]; //guardar racio na linha 4 (pode ser preciso mais tarde)
			matrizdados[2][index] = -1;
			tab[ciclo] = processo[index]; //guardar a ordem dos processos
			tab2[0][ciclo] = (int) matrizdados[1][index];
			tab2[1][ciclo] = (int) matrizdados[0][index];
			
			if(ciclo == 0){
				instante_inicial = (int) matrizdados[0][index];
				instante = instante_inicial;
			} //establece o instante como o instante inicial do primeiro processo
			else{
				instante = instante + (int) matrizdados[1][index];
			} //ao instante anterior, soma o tempo de serviço do processo seguinte

			System.out.print("\nInstante: "+ instante);
			//System.out.print("\nIndice com maior Response Ratio: "+index);
			System.out.print("\nR"+processo[index]+" : "+matrizdados[3][index]);
			ciclo++;
		}

		System.out.print("\n\n==================== Resultado ===================\n");
		for(int l = 0; l<tab.length; l++){
			for (int u =0; u< tab2[0][l]; u++){
				if( l ==0){
					if(tab2[1][1]>0 && controlo == true){
						for(int g =0;g<tab2[1][1];g++){
							System.out.print("-");
						}
					}
				}
				System.out.print(tab[l]);
				controlo = false;
			}
		}
	}
}




