package romberg;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        /************************************************************************
         *                           Scanner                                   *
         ***********************************************************************/
        Scanner Lee = new Scanner(System.in);


        /************************************************************************
         *                   Declaracion de Variables                           *
         ***********************************************************************/
        int grado = 0;
        double a = 0;
        double b = 0;
        double c = 0.0;
        int n = 0;
        double h = 0.0;
        boolean Repetir = true;

        do{

            System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        /************************************************************************
         *                           Funcion                                   *
         ***********************************************************************/
        System.out.print("\nDe que grado es la f(x) ?\n\t=> ");
        grado = Lee.nextInt();
        System.out.println();
        double Fx[] = new double[(grado + 1)];

            for (int x = 0; x <= grado; x++) {
                System.out.print("\nIngrese el Valor de X^" + x + " =>");
                Fx[x] = Lee.nextDouble();
            }




        /************************************************************************
         *                       Cuantas Particiones                           *
         ***********************************************************************/
        do {
            System.out.print("\n\n\n\n\n\n\n\n\nCual es el valor de 'n'(NOTA: n debe de ser PAR) ?\n\t=> ");
            n = Lee.nextInt();
        } while (n % 2 != 0);
        double Romberg[] = new double[3];


        /************************************************************************
         *                       Valor de A y B                                 *
         ***********************************************************************/
        System.out.print("\n\n\n\nCual es la cota a ?\n\t=> ");
        a = Lee.nextDouble();

        System.out.print("\n\n\n\nCual es la cota b ?\n\t=> ");
        b = Lee.nextDouble();


        /************************************************************************
         *                       Verificar F(a) y F(b)                          *
         ***********************************************************************/
        System.out.print("\n\n\n\n\n\n\n\n\n\n\n La funcion a evaluar es:\n\t");
        if(Fx[0] < 0){
            System.out.print("- " + (-1 * Fx[0]));
        }
        else{
            System.out.print(Fx[0]);
        }
        for(int i = 1 ; i <= grado ; i++){
            if(Fx[i] < 0){
                System.out.print(" - " + (-1 * Fx[i]) + "X^" + i);
            }
            else{
                System.out.print(" + " + Fx[i] + "X^" + i);
            }
        }


        /************************************************************************
         *                       Empieza el Algoritmo                          *
         ***********************************************************************/
        for (int i = 0; i < 2; i++) {
            h = (b - a)/n;

            Romberg[i] = 0.0;


            for (int k = 0; k <= n; k++) {
                c = a + (k * h);
                Romberg[i] = Romberg[i] + Evaluar(c, grado, Fx);
            }
            
            Romberg[i] = Romberg[i] * h/2;

            n = n / 2;
           
        }

        Romberg[2] = Romberg[0] + (Romberg[1] - Romberg[0])/3;


        System.out.print("\n\n\n\n\n\n\n\n\n\n");
        
        System.out.print("I1 = " + Romberg[1]);
        System.out.print("\nI2 = " + Romberg[0]);
        System.out.print("\nI = I1 + (I2 - I1)/3 \nI = (" + Romberg[1] + ") + ((" + Romberg[0] + ") - (" + Romberg[1] + "))/3\n");
        System.out.print("I = " + Romberg[2]);

        do{
            System.out.print("\n\n\n\nDesea Ingresar otra funcion ? (1 = si o 2 = no))\n");
            n = Lee.nextInt();
        }while((n != 1) && (n != 2));

        if(n == 2){
            Repetir = false;
        }

        }while(Repetir);

        System.out.println("\n\n\n\n\n\n\n\n\n\n\t\t\t\tCopyrigth Carlos Gamez Sanchez\n\n\n\n\n\t\t\t\t");
    }

    /************************************************************************
     *                         Evaluar F(x)                                *
     ***********************************************************************/
    public static double Evaluar(double x, int grado, double Fx[]) {
        double Respuesta = 0.0;

        for (int a = 0; a <= grado; a++) {
            Respuesta = Respuesta + (Math.pow(x, a) * Fx[a]);
        }

        return Respuesta;
    }
}

