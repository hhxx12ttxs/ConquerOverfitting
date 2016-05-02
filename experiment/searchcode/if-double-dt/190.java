/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spaceballs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import maybach.object.Shape;

/**
 *
 * @author tkarpinski
 */
public class AI {
    private Color color;
    private HashMap<Color, ArrayList<Integer>> slownik = new HashMap(){};
    private int DT = 0;
    private Planeta planeta = null;
    private int DT1 = 0;
    private int DT2 = 0;
    
    public AI(Color color) {
        this.color = color;
        
        for ( Shape s : GameStarter.e.getLayer("planety").getShapes()) {
            if ((s!= null) && this.color == s.getColor()) {
                this.planeta = (Planeta)s;
            } 
        }
        
        for ( Shape s : GameStarter.e.getLayer("planety").getShapes()) {
            if ((s.getColor() != this.color) && (s.getColor() != Color.YELLOW)) {
                ArrayList<Integer> lista = new ArrayList<Integer>();
                lista.add(1000);//min odleglosc
                lista.add(0);//max odleglosc
                lista.add(1);//przybliza sie czy oddala? 1 || -1 
                lista.add(100);//poprzzednia odleglosc
                slownik.put(s.getColor(), lista);
            } 
        }
    }
    
    public void update(long dt) {
        this.DT += dt;
        this.DT1 += dt;
        this.DT2 += dt;
        if (this.DT > 1000) {
            for ( Shape s : GameStarter.e.getLayer("planety").getShapes()) {
                if ((s != null) && (this.planeta != null) && (s.getColor() != this.planeta.getColor()) && (s.getColor() != Color.YELLOW)) {
                    double odleglosc = (this.planeta).distance(s.getX(), s.getY());
                    ArrayList<Integer> list = slownik.get(s.getColor());
                    Integer min = (Integer) list.get(0);
                    Integer max = (Integer) list.get(1);
                    Integer poprzedniaOdleglosc = (Integer) list.get(3);
                    Integer przybliza = 1;
                    
                    if (odleglosc < min) min = (int)odleglosc;
                    if (odleglosc > max) max = (int)odleglosc;
                    if (odleglosc > poprzedniaOdleglosc) przybliza = -1;
                    
                    poprzedniaOdleglosc = (int)odleglosc;
                    
                    list.set(0, min);
                    list.set(1, max);
                    list.set(2, przybliza);
                    list.set(3, poprzedniaOdleglosc);
                    
                    //atakuj planete
                    if (przybliza == 1) {
                        Random r = new Random();
                        int losowa = r.nextInt(5);
                        if ((losowa == 0) && (odleglosc < max/3)) {
                            for ( Shape _s : GameStarter.e.getLayer("balls").getShapes()) {
                                if (_s != null) {
                                    Ball statek = (Ball) _s;

                                    //odleglosc statku od planety z ktorej wysylam
                                    double distance = this.planeta.distance(_s.getX(), _s.getY());

                                    if ((distance < this.planeta.getR()) && (statek != null) && (this.planeta!=null) && (statek.getColor() == this.planeta.getColor()) && (statek.getNumerStartku() <= 2)) {
                                        double planetX = s.getX();
                                        double planetY = s.getY();

                                        statek.setDest((int)planetX, (int)planetY);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.DT = 0;
        }
        if (this.DT1 > 100) {
            for ( Shape _s : GameStarter.e.getLayer("balls").getShapes()) {
                if ((_s != null) && (this.planeta != null) && (_s.getColor() != this.planeta.getColor())) {
                    //sprawdz czy jest blisko mojej planety
                    Ball wrogiStatek = (Ball) _s;
                    double bliskoscOdPlanety = this.planeta.distance(wrogiStatek.getX(), wrogiStatek.getY());
                    boolean czyWPlanecie = false;
                    
                    //sprawdzanie czy w swojej planecie
                    for ( Shape p : GameStarter.e.getLayer("planety").getShapes()) {
                        if ((p != null) && (p.getColor() == wrogiStatek.getColor())) {
                            Planeta wrogaPlaneta = (Planeta) p;
                            double odlegloscStatkuOdPlanety = wrogaPlaneta.distance(wrogiStatek.getX(), wrogiStatek.getY());
                            
                            if (odlegloscStatkuOdPlanety < wrogaPlaneta.getR()) czyWPlanecie = true;
                        }
                    }
                    //wyslij na statek lecacy na planete
                    if ((bliskoscOdPlanety < this.planeta.getR()*2) && (czyWPlanecie == false)) {
                        boolean ctn = true;
                        for ( Shape _dobryS : GameStarter.e.getLayer("balls").getShapes()) {
                            if ((_dobryS != null) && (((Ball)_dobryS).getNumerStartku() >= 3)&& (_dobryS.getColor() == this.planeta.getColor()) &&(ctn == true)) {
                                Ball dobryStatek = (Ball) _dobryS;
                                double odlegloscDobregoStatkuOdPlanety = this.planeta.distance(dobryStatek.getX(), dobryStatek.getY());
                                
                                if ((odlegloscDobregoStatkuOdPlanety < this.planeta.getR())) {
                                    dobryStatek.setDest((int)wrogiStatek.getX(), (int)wrogiStatek.getY());
                                    ctn = false;
                                }
                            }
                        }
                    }
                }
                //jezeli moj statek obok mojej planety
                if ((_s != null) && (this.planeta != null) && (_s.getColor() == this.planeta.getColor())) {
                    Ball mojStatek = (Ball) _s;
                    double odleglosc = this.planeta.distance(mojStatek.getX(), mojStatek.getY());
                    if ((odleglosc > this.planeta.getR()) && (odleglosc < this.planeta.getR()*3) && (mojStatek.getX() == mojStatek.getDestX()) && (mojStatek.getY() == mojStatek.getDestY())) {
                        mojStatek.setDest((int)this.planeta.getX(), (int)this.planeta.getY());
                    }
                }
            } 
        }
        if (this.DT2 > 4000) {
            //bedzie produkowal statki roznego poziomu
            int pierwszy = 0;
            int drugi = 0;
            int trzeci = 0;
            int czwarty = 0;
            for ( Shape s : GameStarter.e.getLayer("balls").getShapes()) {
                if ((s != null) && (this.planeta != null) && (s.getColor() == (this.planeta.getColor()))) {
                    if (((Ball)s).getNumerStartku() == 1) pierwszy++;
                    if (((Ball)s).getNumerStartku() == 2) drugi++;
                    if (((Ball)s).getNumerStartku() == 3) trzeci++;
                    if (((Ball)s).getNumerStartku() == 4) czwarty++;
                }
            }
            if (this.planeta != null) {
                Random r = new Random();
                if ((trzeci+czwarty)*3 > (pierwszy+drugi)) {
                    int ktory = r.nextInt(2)+1;
                    this.planeta.setKtoryStatekProdukuje(ktory);
                } else {
                    int ktory = r.nextInt(2)+3;
                    this.planeta.setKtoryStatekProdukuje(ktory);
                }
            }
            this.DT2 = 0;
        }
    }
}
