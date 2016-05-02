
package clipping;
/*
 * @author sebastian garcia valencia
 */
import java.awt.*;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.*;
import javax.swing.*;

public class Clipping extends JPanel {
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;
    
    static int INSIDE = 0;
    static int LEFT = 1;
    static int RIGHT = 2;
    static int BOTTOM = 4;
    static int TOP = 8;
    
    
    int w;
    int h;
    Graphics2D g2d;
    
    //numero de lineas a pintar
    int numLineas = 10000;
    double [][] lineas = new double [4][numLineas];
    
    //para hacer el cambio visual con el timer
    boolean senial = false;
    
    public Clipping() 
    {
      
      ActionListener animate = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            senial = true;
            repaint();
        }
    };
    Timer timer = new Timer(1000,animate);
    timer.start();
    }

   public void llenarVectorLineas()
   {
       Random r = new Random();
       
       for (int i=0; i<numLineas; i++) {
          /*
          como hicimos el mapping de las coordenadas devemos usar esta formula para que
          los ramdom nos den numeros negativos tambien
          */
          double x1 = r.nextDouble() * (w/2+w/2)-w/2;
          double y1 = r.nextDouble() * (h/2+h/2)-h/2;
          double x2 = r.nextDouble() * (w/2+w/2)-w/2;
          double y2 = r.nextDouble() * (h/2+h/2)-h/2;
          
          /*double x1 = 0;
          double y1 = 0;
          double x2 = 128;
          double y2 = 200;*/
          
          x1 = mapearX(x1,w);
          x2 = mapearX(x2,w);
          y1 = mapearY(y1,h);
          y2 = mapearY(y2,h);
          

          //g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
          
          lineas[0][i] = x1;
          lineas[1][i] = y1;
          lineas[2][i] = x2;
          lineas[3][i] = y2;
       }
   }

    @Override
  public void paintComponent(Graphics g) {
      super.paintComponent(g);

      g2d = (Graphics2D) g;

      g2d.setColor(Color.BLUE);

      // size es el tamaÃ±o de la ventana.
      Dimension size = getSize();
      // Insets son los bordes y los tÃ­tulos de la ventana.
      Insets insets = getInsets();

      w =  (size.width - insets.left - insets.right)/2;//496
      h =  size.height - insets.top - insets.bottom;//474
      
      int tamaClipp = w/2;

      // Generador de nÃºmeros Random
      // Se va a utilizar nextInt, que devuelve un entero.

      g2d.drawLine(w, 0,w , h);
      
      g2d.drawRect(w/4, w/4, tamaClipp, tamaClipp);
      xMin = w/4;
      xMax = 3*w/4;
      yMin = w/4;
      yMax = 3*w/4;

      g2d.drawRect(w/4+w, w/4, tamaClipp, tamaClipp);
      
      
      if(!senial)
      {
        llenarVectorLineas();

        for (int i=0; i<numLineas; i++)  
        {
          drawLine(lineas[0][i], lineas[1][i], lineas[2][i], lineas[3][i],Color.BLACK); 
          drawLine(lineas[0][i]+w, lineas[1][i], lineas[2][i]+w, lineas[3][i],Color.BLACK); 
        }
      }
      else
      {
        //midiendo el tiempo de ejecucion
        long time_start, time_end;

        time_start = System.currentTimeMillis();

        for (int i=0; i<numLineas; i++)      
          liangBarskyDraw(lineas[0][i], lineas[1][i], lineas[2][i], lineas[3][i]);    


        time_end = System.currentTimeMillis();
        System.out.println("Liang.Barsky a tomado "+ ( time_end - time_start ) +" milisegundos");

        xMin = w/4+w;
        xMax = 3*w/4+w;
        yMin = w/4;
        yMax = 3*w/4;

        time_start = System.currentTimeMillis();

        for (int i=0; i<numLineas; i++)      
          cohenSutherlandDraw(lineas[0][i]+w, lineas[1][i], lineas[2][i]+w, lineas[3][i]);  

        time_end = System.currentTimeMillis();
        System.out.println("Cohen-Sutherland a tomado "+ ( time_end - time_start ) +" milisegundos");
      }
  }
  
  double mapearX(double x, int w)
  {
      return (x+w/2);
  }
  
  double mapearY(double y, int h)
  {
      return (h/2-y);
  }
  
    /**
     * Dibujar una lÃ­nea de un color determinado
     * @param x0 comienzo de la lÃ­nea
     * @param y0 comienzo de la lÃ­nea
     * @param x1 final de la lÃ­nea
     * @param y1 final de la lÃ­nea
     * @param color color del que se va a dibujar la lÃ­nea
     */
    private void drawLine(double x0, double y0, double x1, double y1,
            Color color) {          
        g2d.setColor(color);
        g2d.drawLine((int) x0, (int) y0, (int) x1, (int) y1);
    }
    
        /**
     * Calcular el codigo del punto
     * @param x coordenada x del punto
     * @param y coordenada y del punto
     * @return la codificaciÃ³n del punto
     */
    int computeOutCode(double x, double y) {
        int code = 0;

        if (x < xMin) {
            code |= LEFT;
        } else if (x > xMax) {
            code |= RIGHT;
        }
        if (y < yMin) {
            code |= BOTTOM;
        } else if (y > yMax) {
            code |= TOP;
        }

        return code;
    }

    /**
     * Dibujar una lÃ­nea que va de x0 y0 a x1 y1
     * Se dibuja en azulo lo que estÃ¡ dentro del Ã¡rea de clipping y en rojo
     * lo que estÃ¡ por fuera
     * @param x0 coordenada x del comienzo de la lÃ­nea
     * @param y0 coordenada y del comienzo de la lÃ­nea
     * @param x1 coordenada x del fin de la lÃ­nea
     * @param y1 coordenada y del fin de la lÃ­nea
     */
    void cohenSutherlandDraw(double x0, double y0, double x1, double y1) {
        int outCode0 = computeOutCode(x0, y0);
        int outCode1 = computeOutCode(x1, y1);
        boolean accept = false;

        while (true) {
            if ((outCode0 | outCode1) == 0) {
                accept = true;
                // pintar toda la linea verde
                drawLine(x0,y0,x1,y1,Color.green);
                break;
            } else if ((outCode0 & outCode1) != 0) {
                accept = false;
                // pintar toda la linea roja
                //drawLine(x0,y0,x1,y1,Color.red);
                break;
            } else {
                double x;
                double y;

                int outCodeOut;
                // Uno de los dos codigos es diferente de 0
                if (outCode0 != 0) {
                    outCodeOut = outCode0;
                } else {
                    outCodeOut = outCode1;
                }

                if ((outCodeOut & TOP) != 0) {
                    x = x0 + (x1 - x0) * (yMax - y0) / (y1 - y0);
                    y = yMax;
                    // Dibujar este segmento en rojo
                } else if ((outCodeOut & BOTTOM) != 0) {
                    x = x0 + (x1 - x0) * (yMin - y0) / (y1 - y0);
                    y = yMin;
                    // Dibujar este segmento en rojo
                } else if ((outCodeOut & RIGHT) != 0) {
                    y = y0 + (y1 - y0) * (xMax - x0) / (x1 - x0);
                    x = xMax;
                    // Dibujar este segmento en rojo
                } else /*if ((outCodeOut & LEFT) != 0)*/ {
                    y = y0 + (y1 - y0) * (xMin - x0) / (x1 - x0);
                    x = xMin;
                    // Dibujar este segmento en rojo
                }

                // Cambiar el punto externo
                if (outCodeOut == outCode0) {
                    // Pintar de rojo el tramo que se descarta
                    //drawLine(x0, y0, x, y, Color.red);
                    x0 = x;
                    y0 = y;
                    outCode0 = computeOutCode(x0, y0);
                } else {
                    // Pintar de rojo el tramo que se descarta
                    //drawLine(x1, y1, x, y, Color.red);
                    x1 = x;
                    y1 = y;
                    outCode1 = computeOutCode(x1, y1);
                }

            }

        }
    }

    
    /**
     * Algoritmo de Liang Barsky para recortado de lÃ­neas
     * @param x0 comienzo de la lÃ­nea
     * @param y0 comienzo de la lÃ­nea
     * @param x1 final de la lÃ­nea
     * @param y1 final de la lÃ­nea
     */
    public void liangBarskyDraw(double x0, double y0, double x1, double y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double [] p = {-dx, dx, -dy, dy};
        double [] q = {x0 - xMin, xMax - x0, y0 - yMin, yMax - y0};
        double [] entering = new double[3];
        double [] leaving = new double[3];
        double u;
        
        // Ponemos u = 0 en los que entran, de una vez
        entering[0] = 0d; 
        entering[1] = entering[2] = Double.NEGATIVE_INFINITY;
        // Ponemos u = 1 en los que salen, de una vez
        int enteringCount = 1;
        leaving[0] = 1d;
        leaving[1] = leaving[2] = Double.POSITIVE_INFINITY;
        int leavingCount = 1;
        boolean casoEspecial = false;
        for (int i = 0; i < 4; i++) {
            // Casos especiales
            if (p[i] == 0) {
                if (q[i] < 0) {
                    // el segmento esta por fuera
                    //drawLine(x0,y0,x1,y1,Color.red);
                    casoEspecial = true;
                    break;
                } else {
                    // No hacemos nada, porque la intersecciÃ³n con los
                    // lados del rectangulo a los cuales la linea no
                    // es paralela se hace bien.
                    // Como los entrantes se inicializan en -infinito
                    // y los salientes en +infinito, los cortes
                    // reales si setienen en cuenta.
                } 
            } else {
                u = q[i]/p[i];
                // entrando
                if (p[i] < 0) {
                    entering[enteringCount] = u;
                    enteringCount++;
                } else {
                    //saliendo
                    leaving[leavingCount] = u;
                    leavingCount++;
                }
            }            
        }
        
        // Linea paralela a un borde del rectangulo de cliping
        // Linea por fuera del rectangulo
        if(casoEspecial) {
            //drawLine(x0,y0,x1,y1,Color.red);
        }
        
        //drawLine(x0, y0, x1, y1, Color.red);
        double p0 = max(entering);
        double p1 = min(leaving);

        Point point0 = evaluteLineSegment(x0, y0, x1, y1, p0);
        Point point1 = evaluteLineSegment(x0, y0, x1, y1, p1);
        double startX = point0.x;
        double startY = point0.y;
        double endX = point1.x;
        double endY = point1.y;

        if (p1 > p0) {
            // segmento interno
            drawLine(startX, startY, endX, endY, Color.green);
            if (0 < p0) {
                //drawLine(x0, y0, startX, startY, Color.red);
            }
            if (p1 < 1) {
               // drawLine(endX, endY, x1, y1, Color.red);
            }
        } else {
            // la linea esta por fuera
            //drawLine(startX, startY, endX, endY, Color.red);
        }
    }
    
    public double min(double [] arr) {
        double min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < min)
                min = arr[i];
        }
        return min;
    }
    
    public double max(double [] arr) {
        double max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > max)
                max = arr[i];
        }
        return max;
    }
    /**
     * Dados los puntos de comienzo y fin de un segmento de recta, retorna
     * el punto, dobre el segmento, que corresponde al valor u del parÃ¡metro.
     * @param x0 comienzo del segmento
     * @param y0 comienzo del segmento
     * @param x1 fin del segmento
     * @param y1 fin del segmento
     * @param u parÃ¡meto
     * @return 
     */
    private Point evaluteLineSegment(double x0, double y0, double x1, double y1,
            double u) {
        double x = x0 + u * (x1 - x0);
        double y = y0 + u * (y1 - y0);
        Point p = new Point();
        p.x = x;
        p.y = y;
        return p;
    }
 
    
    /**
     * Programa principal
     * @param args 
     */
    public static void main(String[] args) {
        /*JFrame frame = new JFrame("LiangBarsky");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LiangBarsky lb = new LiangBarsky();
        frame.add(lb);
        frame.setSize(512+16+512, 512+38);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);*/
              // Crear un nuevo Frame
      JFrame frame = new JFrame("Lines");
      // Al cerrar el frame, termina la ejecuciÃ³n de este programa
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // Agregar un JPanel que se llama Points (esta clase)
      frame.add(new Clipping());
      // Asignarle tamaÃ±o
      frame.setSize(512+16+512, 512+38);
      // Poner el frame en el centro de la pantalla
      frame.setLocationRelativeTo(null);
      // Mostrar el frame
      frame.setVisible(true);
    }
    
    /** 
     * Clase interna para encapsular los valorex X e Y de un punto
     */
    private class Point {

        public double x;
        public double y;

        @Override
        public String toString() {
            String s = "<" + x + "," + y + ">";
            return s;
        }
    }
    

}
