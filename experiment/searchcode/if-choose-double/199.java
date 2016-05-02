/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tp5sim.util;




/**
 *
 * @author sirius
 */
public class VectorEstado {

    private double matriz[][];

    public static final int INICIO = 0;
    public static final int PROXLLEGADA = 1;
    public static final int FINCOMPRA = 2;
    public static final int FINVENTA1 = 3;
    public static final int FINVENTA2 = 4;

    public static final int COMPRA = 5;
    public static final int VENTA = 6;

    public static final int LIBRE = 7;
    public static final int OCUPADO = 8;
    

    private double reloj;
    private double rnd[];
    private int posicionRND;

    private double proxllegada;
    private double finCompra;
    private double finVenta1;
    private double finVenta2;

    private double tiempoEntreLlegadas;
    private double demoraCompra;
    private double demoraVenta1;
    private double demoraVenta2;

    private double montoInicial;
    private double montoActual;

    private double media;

    private int colaCompra;
    private int colaVenta1;
    private int colaVenta2;

    private boolean estadoServCompra;
    private boolean estadoServVenta1;
    private boolean estadoServVenta2;

    private double ventas;
    private double compras;

    private double precioCompra;
    private double precioVenta;



    



    public VectorEstado(int cantidad, double rnd[], double media, double monto){

        matriz = new double[cantidad][30];
        this.rnd=rnd;
        this.media=media;

        estadoServCompra=false;
        estadoServVenta1=false;
        estadoServVenta2=false;

        proxllegada=0;
        finCompra=0;
        finVenta1=0;
        finVenta2=0;

        this.montoInicial=monto;
        montoActual=monto;

        colaCompra = 0;
        colaVenta1 = 0;
        colaVenta2 = 0;
        ventas=0;

        precioCompra=3.35;
        precioVenta=3.45;
        
        compras=montoInicial*3.40;

        demoraCompra=0;
        demoraVenta1=0;
        demoraVenta2=0;
        
    }

    public void Simular (){

        int colnumero=0;
        int colevento =1;
        int colreloj=2;

        int colRNDProxLLegada=3;
        int colTiempoEntreLLegada=4;
        int colProxLLegada=5;

        int colRNDObjetivo=6;
        int colObjetivo=7;

        int colEstadoV1=8;
        int colcolaVenta1=9;
        int colRNDdemoraVenta1=10;
        int coldemoraVenta1=11;
        int colFinVenta1=12;

        int colEstadoV2=13;
        int colcolaVenta2=14;
        int colRNDdemoraVenta2=15;
        int coldemoraVenta2=16;
        int colFinVenta2=17;

        int colEstadoCompra=18;
        int colcolaCompra=19;
        int colRNDdemoraCompra=20;
        int coldemoraCompra=21;
        int colFinCompra=22;
        
        int colMontoActual =23;
        int colPorcentaje =24;
        int colCompras=25;
        int colVentas=26;
        int colPrecio=27;

        int colDolVenta=28;
        int colDolCompra=29;


         for (int fila =0;fila<matriz.length;fila++)
        {
        
         if (fila==0)//fila==0
         {
            matriz [fila][colevento] = INICIO;
            matriz [fila][colRNDProxLLegada] = rnd[posicionRND];
            tiempoEntreLlegadas = matriz [fila][colTiempoEntreLLegada] = proxLlegada();
            proxllegada=reloj+tiempoEntreLlegadas;

         }
         else
         {
             
//            actualizarTiempo();

            switch(determinarEvento())
            {
                case(1) :  //proxima llegada
                 {

                     matriz [fila][colevento] = PROXLLEGADA;

                     matriz [fila][colRNDProxLLegada] = rnd[posicionRND];
                     tiempoEntreLlegadas=proxLlegada();
                     matriz [fila][colProxLLegada] = tiempoEntreLlegadas;
                     proxllegada=reloj+tiempoEntreLlegadas;
                     matriz [fila][colRNDObjetivo] = rnd[posicionRND];
                         matriz [fila][colObjetivo] = this.objetivo();
                     double serv=calcularServidor(matriz [fila][colObjetivo]);
                     boolean b = estaOcupado((int)serv);
                     
                     
                     if(serv == 1 && b == true)
                     {
                         colaCompra++;
                        
                     }
                     if(serv == 1 && b == false)
                     {
                         calcularPrecios();

                         matriz [fila][colRNDdemoraCompra] = rnd[posicionRND];
                         demoraCompra=demoraCompra();                         
                         matriz [fila][coldemoraCompra] = demoraCompra;
                         finCompra=demoraCompra+reloj;
                         matriz [fila][colFinCompra] = finCompra;
                         estadoServCompra=true;//se ocupa el servidor hasta q pase el tiempo de demora
                         
                         System.out.print("entro al servidor Compra");
                         
                     }
                     if(serv == 2 && b == true)
                     {
                         colaVenta1++;
                       
                     }
                     if(serv == 2 && b == false)
                     {
                         calcularPrecios();

                         
                         matriz [fila][colRNDdemoraVenta1] = rnd[posicionRND];
                         demoraVenta1=demoraVenta();
                         matriz [fila][coldemoraVenta1] = demoraVenta1;
                         finVenta1=demoraVenta1+reloj;
                         matriz [fila][colFinVenta1] = finVenta1;
                         estadoServVenta1=true;//se ocupa el servidor hasta q pase el tiempo de demora
                         
                         System.out.print("entro al servidor Venta1");
                     }
                     if(serv == 3 && b == true)
                     {
                         colaVenta2++;
                        
                     }
                     if(serv == 3 && b == false)
                     {
                         calcularPrecios();

                         
                         matriz [fila][colRNDdemoraVenta2] = rnd[posicionRND];
                         demoraVenta2=demoraVenta();
                         matriz [fila][coldemoraVenta2] = demoraVenta2;
                         finVenta2=demoraVenta2+reloj;
                         matriz[fila][colFinVenta2] = finVenta2;
                         estadoServVenta2=true;//se ocupa el servidor hasta q pase el tiempo de demora
                         
                         System.out.print("entro al servidor Venta2");
                     }

                     if(serv==4)
                     {
                         matriz [fila][colObjetivo]=3; //NO INGRESA
                     }
                     
                     break;
                 }
                case(2): //retiro Compra
                {
                    calcularPrecios();


                         //calculo cuanto van a vender los clientes
                         double ven = calcularventa();
                         compras+=(ven*precioCompra);

                         matriz [fila][colDolCompra] = ven;

                         montoActual+=ven;
                         
                         matriz [fila][colevento] = FINCOMPRA;
                        // matriz [fila][colCompras] = compras;
                         
                         estadoServCompra=false;//acutalizar el estado del servidor a false
                         
                         if (colaCompra >0)
                         {
                          colaCompra--;
                         }
                         finCompra=0;
                         System.out.print("salio del servidor COMPRA");
                    break;
                }
                case(3): //retiro venta 1
                {
                    calcularPrecios();

                        if(montoActual>=500){
                         double v=0;
                         v=(500*precioVenta);
                         matriz [fila][colDolVenta] = 500;
                         ventas+=v;
                         montoActual-=500;
                        }
                         matriz [fila][colevento] = FINVENTA1;
                         
                         
                         //matriz [fila][colVentas] = ventas;
                         
                         estadoServVenta1=false;//acutalizar el estado del servidor a false
                         finVenta1=0;
                         if (colaVenta1>0)
                         {
                           colaVenta1--;
                         }
                         System.out.print("Salio del servidor VENTA1");
                    break;
                }
                case(4): //retiro venta 2
                {
                    calcularPrecios();

                         //calcularPrecios();

                         if(montoActual>=500){
                         double v=0;
                         v=(500*precioVenta);
                         matriz [fila][colDolVenta] = 500;
                         ventas+=v;
                         montoActual-=500;
                         }
                         matriz [fila][colevento] = FINVENTA2;


                     

                         estadoServVenta2=false;//acutalizar el estado del servidor a false LIBRE
                         finVenta2=0;
                         if (colaVenta2 > 0)
                         {
                           colaVenta2--;
                         }

                         System.out.print("Salio del servidor VENTA1");
                    break;
                }
            }//fin switch
           
         }//fin else
         //
            //determinarEvento();
            matriz [fila][colnumero] = fila+1;
            //actualizarTiempo();
            matriz [fila][colreloj] = reloj;
            matriz [fila][colTiempoEntreLLegada] = tiempoEntreLlegadas;
            matriz [fila][colProxLLegada] = proxllegada;
            matriz [fila][colMontoActual] = montoActual;
            matriz [fila][colPorcentaje] = montoActual/montoInicial*100;
            matriz [fila][colCompras] = compras;
            matriz [fila][colVentas] = ventas;
            matriz [fila][colPrecio] = precioVenta;

            matriz [fila][colcolaVenta1] =colaVenta1;
            matriz [fila][colcolaVenta2] = colaVenta2;
            matriz [fila][colcolaCompra] = colaCompra;
            
            if(estadoServCompra==true)
                matriz[fila][colEstadoCompra]=OCUPADO;
            else
                matriz[fila][colEstadoCompra]=LIBRE;

            if(estadoServVenta1==true)
                matriz[fila][colEstadoV1]=OCUPADO;
            else
                matriz[fila][colEstadoV1]=LIBRE;

            if(estadoServVenta2==true)
                matriz[fila][colEstadoV2]=OCUPADO;
            else
                matriz[fila][colEstadoV2]=LIBRE;
            //reloj-=valor;
         //aumentar o disminuir precios
        }//fin for


    }

    public void calcularPrecios(){
        double porcentaje=montoActual/montoInicial;
        boolean yaSeEjecuto=false;
        if(porcentaje<=0.5f && yaSeEjecuto==false)
        {
            precioVenta=3.55;

            colaVenta1=((int)colaVenta1/2);
            colaVenta2=((int)colaVenta2/2);

            yaSeEjecuto=true;
        }
        if(porcentaje>=1.25f)
        {
            precioVenta=3.45;
            yaSeEjecuto=false;
        }
    }

    public double proxLlegada()
    {
            //double vec[]=new double[2];
            proxllegada = this.formula(rnd[posicionRND]);
            //vec[0]=rnd[posicionRND];
            //double menor;
            //vec[1]=proxllegada;
            posicionRND++;
            return proxllegada;

    }

    public double formula(double valorRND){
        return Math.log(1-valorRND)/(-1/media);
    }

    public double objetivo()
    {
            //double vec[]=new double[2];
            if(((float)rnd[posicionRND])<(0.6f)) //puede haber problema comparacion tipo
            {
            //vec[0]=rnd[posicionRND];
            //vec[1]=1;

                if (((float)precioVenta)==3.55f &&((float) rnd[posicionRND])<0.3f )
                {
                    posicionRND++;
                    return 0;
                }
                posicionRND++;
                return COMPRA;//compra
            }
            else
            {
            //vec[0]=rnd[posicionRND];
            //vec[1]=2;
                posicionRND++;
                return VENTA;//venta
            }
            
            //return vec;

    }

    public int calcularServidor (double objetivo){    //Mostrar Random justo antes en la matriz!!!!!!!!!!!!!!!!!

        if(objetivo ==0)
        {
            return 4;
        }
        if (objetivo ==VENTA)
        {
            return 1;
        }
        else
        {
            if (rnd[posicionRND]<0.5f)
            {
                 posicionRND++;
                return 2;
            }
            else
            {
                posicionRND++;
                return 3;
            }
        }


    }

    public boolean estaOcupado (int servidor){
    if (servidor ==1)
    {
       if(estadoServCompra)
       {
          return true;
       }
    }

    if (servidor ==2)
    {
       if(estadoServVenta1)
       {
          return true;
       }
    }

    if (servidor ==3)
    {
       if(estadoServVenta2)
       {
          return true;
       }
    }
    return false;


    }

    public double[][] getMatriz(){
        return matriz;
    }


    public int determinarEvento() //por problemas borrar (reloj+)
    {
       double menor=0;
       int evento=0;
       if (proxllegada < finCompra || finCompra == 0)
       {
        menor = proxllegada;
        evento=1;
       }
       else
       {
        menor = finCompra;
        evento=2;
       }

       if (finVenta1 < menor && finVenta1 != 0)
       {
           menor = finVenta1;
           evento=3;
       }
       if (finVenta2 < menor && finVenta2 != 0)
        {
         menor =finVenta2;
         evento=4;
        }



        reloj=menor;//podria hber problemas
         return evento;
    }


    public double demoraCompra()
    {
        double demora;
        demora=(rnd[posicionRND]+1);
        posicionRND++;
        return demora;
    }

    public double demoraVenta()
    {
        double demora;
        demora=(rnd[posicionRND]+0.5f);
        posicionRND++;
        return demora;
    }

    public double calcularventa()   //del cliente
    {
        double venta;
        venta=(rnd[posicionRND]*500+500);
        posicionRND++;
        return venta;
    }


}

