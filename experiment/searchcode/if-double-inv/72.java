/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import beans.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.hibernate.*;

/**
 *
 * @author jorge
 */
public class CVuelo {

    private static List ListaTipoEst;

    public static List<Parametro> llenarComboEstado() {
        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();
        try {
            Transaction tx = s.beginTransaction();
            Query q;

            q = s.getNamedQuery("ParametrosAeropuerto");
            q.setParameter("tipo", "ESTADO_VUELO");
            ListaTipoEst = q.list();



        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {

            s.close();
        }

        return ListaTipoEst;

    }

    public static String validar(
            Aeropuerto aeropuertoOrigen,
            Aeropuerto aeropuertoDestino,
            Date ini,
            Date fin,
            Parametro Estado,
            String capacidad,
            String monto) {
        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();
        String error_message = "";
        Parametro p;
        try {

            if (aeropuertoOrigen == null
                    || aeropuertoDestino == null
                    || ini == null
                    || fin == null
                    || Estado == null
                    || (capacidad == null || capacidad.compareTo("") == 0)
                    || (monto == null || monto.compareTo("") == 0)) {
                error_message = error_message + CValidator.buscarError("ERROR_FT001") + "\n";

            } else {
                if (ini.before(fin)) {
                    error_message = "La fecha de salida no puede ser menor a la fecha de llegada";
                }
                if (!CValidator.isInteger(capacidad)) {

                    error_message = "La capacidad máxima es inválida";

                } else {
                    if (Integer.parseInt(capacidad) == 0) {
                        error_message = "El capacidad máxima tiene que ser mayor que 0";
                    }
                }

                if (!CValidator.isDouble(monto)) {

                    error_message = "La capacidad máxima es inválida";

                } else {
                    if (Double.parseDouble(monto) == 0) {
                        error_message = "El capacidad máxima tiene que ser mayor que 0";
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }

        return error_message;
    }

    public static void cargarVuelo(Vuelo v) {

        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();


        try {

            Transaction tx = s.beginTransaction();

            int i = (Integer) s.save(v);

            tx.commit();
            CPista.guardarPista("Administración", "Vuelo", "Crear", v.aString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }

    }

    public static List<Vuelo> BuscarVuelo(String cod, Aeropuerto PaisOrigen, Aeropuerto PaisDestino, Calendar fechini, Calendar fechfinal, Parametro Estado) {

        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();
        List<Vuelo> ListaVuelos = null;
        Date ini = null;
        Date fin = null;
        String inii = null;

        try {

            if (fechini != null) {
                inii = CValidator.formatDate(fechini.getTime());
                ini = fechini.getTime();
            }

            if (fechfinal != null) {
                fin = fechfinal.getTime();
            }


            Query q = s.getNamedQuery("Volar");



//        if (isInteger(numEnvio)) {
//                Filter f_numEnvio = s.enableFilter("EnviosXNumEnvio");
//                f_numEnvio.setParameter("idEnvio", Integer.parseInt(numEnvio));
//            }
            if (cod != null && !cod.isEmpty() && CValidator.isInteger(cod)) {
                Filter f_id = s.enableFilter("VueloXId");
                f_id.setParameter("id", Integer.parseInt(cod));
            }

            if (PaisOrigen != null) {
                Filter f_origen = s.enableFilter("VueloXOrigen");
                f_origen.setParameter("idOrigen", PaisOrigen.getIdAeropuerto());
            }

            if (PaisDestino != null) {
                Filter f_destino = s.enableFilter("VueloXDestino");
                f_destino.setParameter("idDestino", PaisDestino.getIdAeropuerto());
            }

            if (fechini != null) {

//                ini = new Date();
//                ini.setDate(fechini.get(0));
//                ini.setMonth(fechini.get(1));
//                ini.setYear(fechini.get(2));
//                ini = formatoDelTexto.
                Filter f_inisalida = s.enableFilter("VueloXfechini");
                f_inisalida.setParameter("fechasalida", ini);
            }

            if (fechfinal != null) {
//                fin = new Date();
//                fin.setDate(fechfinal.get(0));
//                fin.setMonth(fechfinal.get(1));
//                fin.setYear(fechfinal.get(2));
                Filter f_finllega = s.enableFilter("VueloXfechfin");
                f_finllega.setParameter("fechallegada", fin);
            }

            if (Estado != null) {
                Filter f_estado = s.enableFilter("VueloXEstado");
                f_estado.setParameter("estado", Estado.getIdParametro());
            }

            ListaVuelos = q.list();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }

        return ListaVuelos;

    }

    public static void modificarVuelo(
            String idVuelo,
            Aeropuerto aeropuertoOrigen,
            Aeropuerto aeropuertoDestino,
            Date ini,
            Date fin,
            Parametro Estado,
            String capacidad,
            String monto) {


        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();


        try {
            Transaction tx = s.beginTransaction();
            Query q;
//            Parametro pTipoDoc;            
//            Parametro pCiudad;            
//            Parametro pPais;
            Vuelo objVuelo = new Vuelo();

            objVuelo.setIdVuelo(Integer.parseInt(idVuelo));
            objVuelo.setOrigen(aeropuertoOrigen);
            objVuelo.setDestino(aeropuertoDestino);
            objVuelo.setFechaLlegada(ini);
            objVuelo.setFechaSalida(fin);
            objVuelo.setEstado(Estado);
            objVuelo.setCapacidadMax(Integer.parseInt(capacidad));
            objVuelo.setAlquiler(Double.valueOf(monto));

//            objVuelo.setPais(Pais);
//            objVuelo.setIdAeropuerto(idAeropuerto);


            /*
             *  int idVuelo,
             Aeropuerto aeropuertoOrigen, 
             Aeropuerto aeropuertoDestino,
             Calendar ini,
             Calendar fin,
             Parametro Estado,
             String capacidad
             */
            
            CPista.guardarPista("Administración", "Vuelo", "Modificar", 
                                "ANTES: " + buscarVueloId(Integer.parseInt(idVuelo)).aString() + 
                                " DESPUES: " + objVuelo.aString());

            s.update(objVuelo);

            tx.commit();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }
//      

    }

    public static void agregarVuelo(
            Aeropuerto aeropuertoOrigen,
            Aeropuerto aeropuertoDestino,
            Date ini,
            Date fin,
            Parametro Estado,
            String capacidad,
            String monto) {



        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();


        try {
            Transaction tx = s.beginTransaction();
            Query q;
//            Parametro pTipoDoc;            
//            Parametro pCiudad;            
//            Parametro pPais;


            Vuelo objVuelo = new Vuelo();

            //       objVuelo.setIdVuelo(idVuelo); 
            objVuelo.setOrigen(aeropuertoOrigen);
            objVuelo.setDestino(aeropuertoDestino);
            objVuelo.setFechaLlegada(ini);
            objVuelo.setFechaSalida(fin);
            objVuelo.setEstado(Estado);
            objVuelo.setCapacidadMax(Integer.parseInt(capacidad));
            objVuelo.setAlquiler(Double.valueOf(monto));


            int i = (Integer) s.save(objVuelo);

            tx.commit();
            
            CPista.guardarPista("Administración", "Vuelo", "Crear", objVuelo.aString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }
    }

    public static Vuelo buscarVueloId(int id) {
        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();
        Vuelo v = null;

        try {

            Query q = s.getNamedQuery("VueloXIdVuelo").setMaxResults(1);
            q.setParameter("idVuelo", id);
            v = (Vuelo) q.uniqueResult();

            v.getIncidencias().size();

            for (Escala escala : v.getEscalas()) {
                for (int i = 0; i < escala.getEnvio().getEscalas().size(); i++) {
                    Escala escalaProfunda = escala.getEnvio().getEscalas().get(i);
                    //for(Escala escalaProfunda : escala.getEnvio().getEscalas()){
                    if (escalaProfunda.getIdEscala() == escala.getIdEscala()) {
                        escala.getEnvio().getEscalas().set(i, escala);
                    }
                    escalaProfunda.getVuelo().getIncidencias().size();
                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }

        return v;
    }

    public static void modificarVueloEstado(Vuelo objVuelo, String Estado) {

        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();

        try {
            Transaction tx = s.beginTransaction();

            if (objVuelo.getEstado().equals("CAN")) {
                return;
            }

            Parametro estadoVuelo = CParametro.buscarXValorUnicoyTipo("ESTADO_VUELO", Estado);
            objVuelo.setEstado(estadoVuelo);
            s.saveOrUpdate(objVuelo);
            tx.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }
    }

    private static void guardar(Vuelo v) {
        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();

        try {
            Transaction tx = s.beginTransaction();
            s.saveOrUpdate(v);
            tx.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }
    }

    public static Help modificarEscalas(Vuelo v) {
        Help h = new Help();

        if (v.getEscalas() != null) {
            for (Escala escala : v.getEscalas()) {
                ArrayList<Vuelo> vuelos = modificarEnvio(escala, h);
                for (Vuelo vuelo : vuelos) {
                    guardar(vuelo);
                }
                CEnvio.enviarCorreos(escala.getEnvio());
            }
        }

        return h;

    }

    public static ArrayList<Vuelo> modificarEnvio(Escala escala, Help h) {
        SessionFactory sf = Sesion.getSessionFactory();
        Session s = sf.openSession();
        ArrayList<Vuelo> v = new ArrayList<Vuelo>();

        try {

            Transaction tx;
            Parametro t;
            String estadoVuelo = escala.getVuelo().getEstado().getValorUnico();

            if (escala.getEstado().getValorUnico().equals("CAN")) {
                return null;
            }

            if (estadoVuelo.equals("FIN")) {
                // Si el vuelo llegó

                String valUnico;
                if (escala.getVuelo().getDestino().getIdAeropuerto() == escala.getEnvio().getDestino().getIdAeropuerto()) {
                    valUnico = "XREC";
                } else {
                    valUnico = "ESC";
                }

                //  Cambiarle el estado al envío
                t = CParametro.buscarXValorUnicoyTipo("ESTADO_ENVIO", valUnico);
                escala.getEnvio().setActual(escala.getVuelo().getDestino());
                escala.getEnvio().setEstado(t);

                //  Cambiarle el estado a la escala
                t = CParametro.buscarXValorUnicoyTipo("ESTADO_ESCALA", "FIN");
                escala.setEstado(t);

                //  Incrementar el almacén
                int cActual = escala.getEnvio().getActual().getCapacidadActual();
                escala.getEnvio().getActual().setCapacidadActual(cActual + escala.getEnvio().getNumPaquetes());
            }

            if (estadoVuelo.equals("VUE")) {
                // Si el vuelo despegó

                //  Cambiar de estado el envío
                t = CParametro.buscarXValorUnicoyTipo("ESTADO_ENVIO", "VUE");
                escala.getEnvio().setEstado(t);

                //  Cambiarle el estado a la escala
                t = CParametro.buscarXValorUnicoyTipo("ESTADO_ESCALA", "EFE");
                escala.setEstado(t);

                //  Decrementar el almacén
                int cActual = escala.getEnvio().getActual().getCapacidadActual();
                escala.getEnvio().getActual().setCapacidadActual(cActual - escala.getEnvio().getNumPaquetes());

                //  Poner nulo el aeropuerto actual
                escala.getEnvio().setActual(null);

            }

            if (estadoVuelo.equals("CAN")) {
                // Si el vuelo es cancelado:
                // - cambiarle el estado a las escalas que lo usan
                // - cambiarle el estado a las escalas programadas del mismo envío
                // - decrementar al capacidad del vuelo de dichas escalas programadas
                // - recalcular ruta
                // - si no hay ruta, poner estado Indefinido
                // - si hay ruta, agregar las escalas           

                t = CParametro.buscarXValorUnicoyTipo("ESTADO_ESCALA", "CAN");
                Date ahora = new Date();

                int numEscala = escala.getNumEscala();
                escala.setEstado(t);

                tx = s.beginTransaction();

                for (Escala escalaCancelada : escala.getEnvio().getEscalas()) {
                    if (escalaCancelada.getEstado().getValorUnico().equals("PROG")) {

                        if (escalaCancelada.getNumEscala() < numEscala) {
                            numEscala = escalaCancelada.getNumEscala();
                        }

                        escalaCancelada.setEstado(t);
                        int nVuelo = escalaCancelada.getVuelo().getCapacidadActual();
                        escalaCancelada.getVuelo().setCapacidadActual(nVuelo - escalaCancelada.getEnvio().getNumPaquetes());
                        s.saveOrUpdate(escalaCancelada.getVuelo());
                    }
                }

                tx.commit();

                CEnvio cenvio = new CEnvio();
                String error = cenvio.calcularRuta(escala.getEnvio(), ahora, numEscala);

                if (error != null && !error.isEmpty()) {
                    t = CParametro.buscarXValorUnicoyTipo("ESTADO_ENVIO", "IND");
                    h.sumaFallo();
                    escala.getEnvio().setEstado(t);
                } else {
                    h.sumaExito();
                }

            }


            tx = s.beginTransaction();
            s.saveOrUpdate(escala.getEnvio());
            tx.commit();

            for (Escala e : escala.getEnvio().getEscalas()) {
                v.add(e.getVuelo());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            s.close();
        }

        return v;
    }
}

