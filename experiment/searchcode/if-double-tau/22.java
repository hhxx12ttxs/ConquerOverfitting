/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import beans.Aeropuerto;
import beans.Envio;
import beans.Vuelo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author miguelavg
 */
public class Recocido {
    private int kSA;                            // iteraciones por temperatura
    private double temperaturaInicial;          // temperatura inicial
    private double temperatura;                 // temperatura actual
    private double temperaturaFinal;            // temperatura final
    private double alfaSA;                      // coeficiente de reducción de temperatura
    private double alfaGrasp;                   // coeficiente de relajación del grasp construcción
    private double pParada;                     // porcentaje de malas iteraciones para parar
    private int intentos;                       // intentos de malos grasp
    private double vCostoAlmacen;
    private Envio envio;                        // envío a realizar
    private ArrayList<Vuelo> solucion;          // ruta solución
    private ArrayList<Vuelo> alterado;          // ruta alterada
    private ArrayList<VueloHist> vuelosLite;    // histórico

    public Recocido(int kSA, double temperaturaInicial, double temperaturaFinal, double alfaSA, double alfaGrasp, double pParada, int intentos, Envio envio, double vCostoAlmacen, ArrayList<VueloHist> vuelosLite) {
        this.kSA = kSA;
        this.temperaturaInicial = temperaturaInicial;
        this.temperatura = temperaturaInicial;
        this.temperaturaFinal = temperaturaFinal;
        this.alfaSA = alfaSA;
        this.alfaGrasp = alfaGrasp;
        this.pParada = pParada;
        this.intentos = intentos;
        this.envio = envio;
        this.vCostoAlmacen = vCostoAlmacen;
        this.vuelosLite = vuelosLite;
    }

    private double enfriamiento() {
        this.temperatura = this.alfaSA * this.temperatura;
        return this.temperatura;
    }

    private double buscarHistorico(int idOrigen, int idDestino) {

        int lo = 0;
        int hi = vuelosLite.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            VueloHist v = vuelosLite.get(mid);
            if ((idOrigen < v.getOrigen()) || (idOrigen == v.getOrigen() && idDestino < v.getDestino())) {
                hi = mid - 1;
            } else if ((idOrigen > v.getOrigen()) || (idOrigen == v.getOrigen() && idDestino > v.getDestino())) {
                lo = mid + 1;
            } else {
                return vuelosLite.get(mid).getpLleno();
            }
        }
        return 0;
    }

    private int estadoEnergia(ArrayList<Vuelo> vuelos, Date llegada) {
        try {
            Vuelo vuelo;
            long milisec;
            double iCostoAlmacen;
            double iCostoEnvio;
            double costoAlmacen = 0;
            double costoEnvio = 0;
            double costo;
            double pLleno;
            double pCapacidad;

            for (int i = 0; i < vuelos.size(); i++) {
                vuelo = vuelos.get(i);
                milisec = vuelo.getFechaSalida().getTime() - llegada.getTime();

                iCostoAlmacen = this.vCostoAlmacen * (double) milisec / (60 * 60000);
                costoAlmacen = costoAlmacen + iCostoAlmacen;

                pLleno = buscarHistorico(envio.getActual().getIdAeropuerto(), envio.getDestino().getIdAeropuerto());

                pCapacidad = Math.max(pLleno * vuelo.getCapacidadMax(), vuelo.getCapacidadActual() + 1);

                iCostoEnvio = (double) vuelo.getAlquiler() / pCapacidad;
                costoEnvio = costoEnvio + iCostoEnvio;
            }

            costo = costoEnvio + costoAlmacen;
            return (int) costo;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    private double boltzmann(double dEnergia, double temperatura) {
        return Math.exp(-1 * (dEnergia / temperatura));
    }

    public class CustomComparator implements Comparator<MovimientoAlmacen> {
        @Override
        public int compare(MovimientoAlmacen m1, MovimientoAlmacen m2) {
            if (m1.getFecha().before(m2.getFecha())) {
                return -1;
            }
            if (m1.getFecha().after(m2.getFecha())) {
                return 1;
            }
            return 0;
        }
    }

    private int getMax(Aeropuerto a, Date in, Date out) {
        int actual = a.getCapacidadActual();
        int max = actual;

        ArrayList<MovimientoAlmacen> moves = new ArrayList<MovimientoAlmacen>();
        for (Vuelo v : a.getVuelosSalida()) {
            moves.add(new MovimientoAlmacen(v.getFechaSalida(), "O", v.getCapacidadActual()));
        }
        for (Vuelo v : a.getVuelosLlegada()) {
            moves.add(new MovimientoAlmacen(v.getFechaLlegada(), "I", v.getCapacidadActual()));
        }
        Collections.sort(moves, new CustomComparator());

        for (MovimientoAlmacen m : moves) {
            if (m.getTipo().equals("I") && m.getFecha().before(out) && m.getFecha().after(in)) {
                actual = actual + m.getCantidad();
            }
            if (m.getTipo().equals("O") && m.getFecha().before(out) && m.getFecha().after(in)) {
                actual = actual - m.getCantidad();
            }
            if (max < actual) {
                max = actual;
            }
        }

        return max;
    }

    private ArrayList<Vuelo> liteGrasp(Aeropuerto aOrigen, Aeropuerto aDestino, Date fecha, double alfa, int numPaquetes) {
        try {
            Aeropuerto aActual = aOrigen;
            Random rnd = new Random();

            Date dActual = fecha;
            int iActual = aActual.getIdAeropuerto();
            int iFinal = aDestino.getIdAeropuerto();

            ArrayList<Vuelo> posibles;
            ArrayList<Vuelo> construccion = new ArrayList<Vuelo>();
            ArrayList<Vuelo> rcl;
            Vuelo aleatorio;

            int beta = Integer.MAX_VALUE;
            int tau = 0;
            int e;

            // Mientras no hayamos llegado al final...

            while (iActual != iFinal && aActual.getCapacidadMax() >= aActual.getCapacidadActual() + numPaquetes) {
                posibles = new ArrayList<Vuelo>();

                // Calcular los vuelos posibles, el beta y el tau

                for (Vuelo vuelo : aActual.getVuelosSalida()) {

                    if (vuelo.getCapacidadMax() >= vuelo.getCapacidadActual() + numPaquetes
                            && getMax(aOrigen, dActual, vuelo.getFechaSalida()) + numPaquetes <= aOrigen.getCapacidadMax()
                            && dActual.before(vuelo.getFechaSalida())) {
                        posibles.add(vuelo);
                        ArrayList<Vuelo> wrap = new ArrayList<Vuelo>();
                        wrap.add(vuelo);
                        e = estadoEnergia(wrap, dActual);

                        if (e < beta && e >= 0) {
                            beta = e;
                        }
                        if (e > tau && e >= 0) {
                            tau = e;
                        }

                    }

                }

                rcl = new ArrayList<Vuelo>();

                for (Vuelo vuelo : posibles) {
                    ArrayList<Vuelo> wrap = new ArrayList<Vuelo>();
                    wrap.add(vuelo);
                    e = estadoEnergia(wrap, dActual);

                    if (beta <= e && e <= beta + alfa * (tau - beta)) {
                        rcl.add(vuelo);
                    }
                }

                if (rcl.isEmpty()) {
                    return null;
                }

                aleatorio = rcl.get(rnd.nextInt(rcl.size()));
                construccion.add(aleatorio);

                aActual = aleatorio.getDestino();
                iActual = aActual.getIdAeropuerto();
                dActual = aleatorio.getFechaLlegada();
                beta = Integer.MAX_VALUE;
                tau = 0;

            }

            if (iActual != iFinal) {
                return null;
            }

            return construccion;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private ArrayList<Vuelo> alteracionMolecular() {
        try {
            Random rnd = new Random();
            Date fecha;

            this.alterado = new ArrayList<Vuelo>();
            int iAleatorio = rnd.nextInt(this.solucion.size());
            Vuelo aleatorio = this.solucion.get(iAleatorio);
            Aeropuerto pivote = aleatorio.getOrigen();

            for (int i = 0; i < iAleatorio; i++) {
                alterado.add(this.solucion.get(i));
            }

            if (iAleatorio > 0) {
                fecha = solucion.get(iAleatorio - 1).getFechaLlegada();
            } else {
                fecha = envio.getFechaRegistro();
            }

            ArrayList<Vuelo> construccion = liteGrasp(pivote, envio.getDestino(), fecha, this.alfaGrasp, envio.getNumPaquetes());

            if (construccion == null) {
                this.alterado = null;
                return null;
            }

            for (int i = 0; i < construccion.size(); i++) {
                this.alterado.add(construccion.get(i));
            }

            return alterado;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ArrayList<Vuelo> simular() {
        try {
            Random rnd = new Random();
            int dEnergia;
            double b, p;

            int iteraciones = this.kSA * (int) (Math.log(this.temperaturaFinal / this.temperaturaInicial) / Math.log(this.alfaSA));
            int outIt = 0;

            for (int i = 0; i < this.intentos; i++) {
                this.solucion = liteGrasp(envio.getActual(), envio.getDestino(), envio.getFechaRegistro(), this.alfaGrasp, envio.getNumPaquetes());
                if (this.solucion != null) {
                    break;
                }
            }

            if (this.solucion == null) {
                // System.out.println("¡No hay solución inicial!");
                return null;
            }

            while (this.temperatura > this.temperaturaFinal) {
                for (int k = 0; k < this.kSA; k++) {
                    for (int i = 0; i < this.intentos; i++) {
                        this.alteracionMolecular();
                        if (this.alterado != null) {
                            break;
                        }
                    }

                    if (this.alterado == null) {
                        outIt++;

                        if (outIt >= iteraciones * this.pParada) {
                            // System.out.println("¡Fin por optimalidad!\n");
                            return this.solucion;
                        }
                        continue;
                    }

                    dEnergia = estadoEnergia(this.alterado, this.envio.getFechaRegistro()) - estadoEnergia(this.solucion, this.envio.getFechaRegistro());

                    if (dEnergia >= 0) {
                        outIt++;
                        b = boltzmann(dEnergia, temperatura);
                        p = rnd.nextDouble();
                        if (p <= b) {
                            this.solucion = this.alterado;
                            //System.out.println("¡Alteración elegida por Boltzmann!");
                        }
                    } else {
                        outIt = 0;
                        this.solucion = this.alterado;
                        //System.out.println("¡Alteración elegida por mejora!");
                    }

                    if (outIt >= iteraciones * this.pParada) {
                        // System.out.println("¡Fin por optimalidad!\n");
                        return solucion;
                    }

                }

                this.enfriamiento();
            }
            return solucion;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
