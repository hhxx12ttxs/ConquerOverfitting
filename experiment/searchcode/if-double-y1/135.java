/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.*;

/**
 *
 * @author scheich
 */
public class Punkt3D {
 static Scanner keyboard = new Scanner(System.in);
    private double x,y,z;
    
     public Punkt3D() {
         double x1,y1,z1;      
         x1 = keyboard.nextDouble();
        y1 = keyboard.nextDouble();
        z1 = keyboard.nextDouble();
         this.x = x1;
        this.y = y1;
        this.z = z1;
    }
     public Punkt3D(double x1, double y1, double z1) {
         

        this.x = x1;
        this.y = y1;
        this.z = z1;
    }

     
    public void einlesen (){
        double x1,y1,z1;
        x1 = keyboard.nextDouble();
        y1 = keyboard.nextDouble();
        z1 = keyboard.nextDouble();
        set(x1,y1,z1);
    }
    
    public void ausgeben (){
        System.out.println(get("x"));
        System.out.println(get("y"));
        System.out.println(get("z"));
    }
    
    public void verschiebePunkt (double x2, double y2, double z2){
        double x1,y1,z1;
        x1=get("x")+x2;
        y1=get("y")+y2;
        z1=get("z")+z2;
        set(x1,y1,z1);
    }
    
    public void verschiebungEinlesen (){
        double x1,y1,z1;
        x1 = keyboard.nextDouble();
        y1 = keyboard.nextDouble();
        z1 = keyboard.nextDouble();   
        verschiebePunkt(x1,y1,z1);
    }
    
    public void set (double x1, double y1, double z1 ){
        x = x1;
        y = y1;
        z = z1;   
    }
    
    public double get (String vari){
        if (vari=="x") {return x;};
        if (vari=="y") {return y;};
        if (vari=="z") {return z;};
        return 42;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    Punkt3D punkt1= new Punkt3D();//neues Objekt punkt1 der 
                                  //Klasse Punkt3D mit 
                                  //leeren Koordinaten
        System.out.println("Gib die 3 Punkte ein!");
    punkt1.einlesen();
        System.out.println("Das sind sie!");
    punkt1.ausgeben();
        System.out.println("Gib die Verschiebung an!");
    punkt1.verschiebungEinlesen();
        System.out.println("Die neuen Punkte!");
    punkt1.ausgeben();        
    }
    
    
}

