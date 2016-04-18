/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.logongas.encuestas.persistencia.impl.dao.encuestas;

import es.logongas.encuestas.modelo.encuestas.Encuesta;
import es.logongas.encuestas.modelo.encuestas.Item;
import es.logongas.encuestas.modelo.encuestas.Pregunta;
import es.logongas.encuestas.modelo.encuestas.TipoItem;
import es.logongas.encuestas.modelo.encuestas.Valor;
import es.logongas.encuestas.modelo.resultados.EstadisticaDescriptiva;
import es.logongas.encuestas.modelo.resultados.InferenciaEstadistica;
import es.logongas.encuestas.modelo.resultados.Resultado;
import es.logongas.encuestas.modelo.resultados.Serie;
import es.logongas.encuestas.persistencia.services.dao.encuestas.EncuestaDAO;
import es.logongas.ix3.persistence.impl.hibernate.dao.GenericDAOImplHibernate;
import es.logongas.ix3.persistence.services.dao.NamedSearch;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 *
 * @author Lorenzo González
 */
public class EncuestaDAOImplHibernate extends GenericDAOImplHibernate<Encuesta, Integer> implements EncuestaDAO {

    private int numDecimals = 2;
    private BigDecimal nivelConfianza = new BigDecimal(0.95);

    @Override
    @NamedSearch(parameterNames = "item")
    public Resultado getResultadoItem(Item item) {
        return getResultadoItem(item,false);
    }
    private Resultado getResultadoItem(Item item,boolean mustCheck) {
        if (item==null) {
            throw new RuntimeException("El item es null");
        }

        Session session = sessionFactory.getCurrentSession();

        List<Object[]> resultados;
        {
            String whereCheck;
            if (mustCheck==true) {
                whereCheck=" AND ri.check=true";
            } else {
                whereCheck="";
            }

            String orderBy;
            {//Calcular como se ordenan los datos
                TipoItem tipoItem=item.getTipoItem();
                if (tipoItem==null) {
                    orderBy = " count(*) DESC ";
                } else {
                    switch (tipoItem) {
                        case Sino:
                            orderBy = " count(*) DESC ";
                            break;
                        case ListaValores:
                            if (item.getListaValores().isContieneValoresNumericos()==true) {
                                orderBy = " ri.valorNumerico ASC ";
                            } else {
                                orderBy = " count(*) DESC ";
                            }

                            break;
                        case Texto:
                            orderBy = " count(*) DESC ";
                            break;
                        case Fecha:
                            orderBy = " count(*) DESC ";
                            break;
                        case AreaTexto:
                            orderBy = " count(*) DESC ";
                            break;
                        default:
                            throw new RuntimeException("item.getTipoItem() no es válido:"+item.getTipoItem());
                    }
                }
            }
            String shql = "SELECT ri.valor,count(*) FROM RespuestaItem ri WHERE ri.item.idItem=? " + whereCheck + " GROUP BY ri.valor ORDER BY " + orderBy;
            Query query = session.createQuery(shql);
            query.setInteger(0, item.getIdItem());
            resultados = query.list();

            //Esto se hace pq cuando hay una lista de valores, se deben incluir tambien los valores que no tienen respuestas
            //Pero SOLO se hace para los valores numéricos pq así se mantiene la proporcion de los datos
            if ((item.getTipoItem()==TipoItem.ListaValores) && (item.getListaValores().isContieneValoresNumericos()==true)) {
                List<Object[]> realResultados=new ArrayList<Object[]>();

                //Recorremos todos los valores de "ListaValores" para añadir la frecuencia de cada uno de ellos
                for(Valor valor:item.getListaValores().getValores()){
                    String nombreValor=valor.getNombre();
                    Long frecuencia=0L; //Si no hay nada de la sqhl , entonces la frencuencia el 0

                    //Buscamos ese valor entre las frecuencias de la shql
                    for(Object[] resultado:resultados) {
                        if (resultado[0].equals(nombreValor)) {
                            frecuencia=(Long)resultado[1];
                            break;
                        }
                    }

                    Object[] singleRow={nombreValor,frecuencia};
                    realResultados.add(singleRow);
                }
                resultados=realResultados;
            }


        }

        Resultado resultado = new Resultado(item);
        Serie serie = new Serie(getCountRespuestasItems(resultados), item.getNombre());
        for (Object[] datos : resultados) {
            resultado.getLabels().add(getLabelFromValue((String) datos[0]));
            long rawData = ((Number) datos[1]).longValue();
            serie.getRawData().add(rawData);
            serie.getData().add(getDataFromRawData(rawData, serie.getNumRespuestas()));
        }

        //Calcular las estadísticasSolo si hay almenos 2 datos
        if ((item.getListaValores() != null) && (item.getListaValores().isContieneValoresNumericos()) && (serie.getNumRespuestas()>=2)) {


            //Añadir los datos
            String shql = "SELECT ri.valorNumerico FROM RespuestaItem ri WHERE ri.item.idItem=? AND ri.valorNumerico!=null ";
            Query query = session.createQuery(shql);
            query.setInteger(0, item.getIdItem());
            List<Double> datos = query.list();

            if (datos.size()>=2) {
                EstadisticaDescriptiva estadisticaDescriptiva=new EstadisticaDescriptiva(numDecimals);
                for(Double dato:datos) {
                    estadisticaDescriptiva.addData(dato);
                }


                InferenciaEstadistica inferenciaEstadistica=new InferenciaEstadistica(estadisticaDescriptiva, nivelConfianza, numDecimals);

                serie.setEstadisticaDescriptiva(estadisticaDescriptiva);
                serie.setInferenciaEstadistica(inferenciaEstadistica);
            }
        }


        resultado.getSeries().add(serie);

        return resultado;
    }

    @Override
    @NamedSearch(parameterNames = "pregunta")
    public Resultado getResultadoPregunta(Pregunta pregunta) {
        Session session = sessionFactory.getCurrentSession();

        Map<Item, Long> resultados = new TreeMap<Item, Long>();
        List<Object[]> resultadosTrue;
        {
            //Obtener las respuestas marcadas
            String shql = "SELECT ri.item,count(*) FROM RespuestaItem ri WHERE ri.item.pregunta.idPregunta=? and ri.check=true GROUP BY ri.item.nombre ";
            Query query = session.createQuery(shql);
            query.setInteger(0, pregunta.getIdPregunta());
            resultadosTrue = query.list();
            for (Object[] resultado : resultadosTrue) {
                Item item = (Item) resultado[0];
                long dataRaw = ((Number) resultado[1]).longValue();

                if (resultados.get(item) == null) {
                    resultados.put(item, dataRaw);
                }
            }
        }
        {
            //Obtener las respuestas que NO están marcadas
            List<Object[]> resultadosLabels;
            String shql = " SELECT i,0 FROM Item i WHERE i.pregunta.idPregunta=?";
            Query query = session.createQuery(shql);
            query.setInteger(0, pregunta.getIdPregunta());
            resultadosLabels = query.list();
            for (Object[] resultado : resultadosLabels) {
                Item item = (Item) resultado[0];
                long dataRaw = ((Number) resultado[1]).longValue();

                if (resultados.get(item) == null) {
                    resultados.put(item, dataRaw);
                }
            }
        }


        Resultado otros=null;
        if ((pregunta.isUltimoItemIncluyeOtros()==true) && (pregunta.getItems().size()>=1)) {
            Item ultimoItem=pregunta.getItems().get(pregunta.getItems().size()-1);
            otros=this.getResultadoItem(ultimoItem,true);
        }

        Resultado resultado = new Resultado(pregunta);
        Serie serie = new Serie(getCountRespuestasItems(resultadosTrue), pregunta.getPregunta());
        serie.setOtros(otros);
        for (Item item : resultados.keySet()) {
            resultado.getLabels().add(getLabelFromValue(item.getNombre()));
            long rawData = resultados.get(item);
            serie.getRawData().add(rawData);
            serie.getData().add(getDataFromRawData(rawData, serie.getNumRespuestas()));
        }
        resultado.getSeries().add(serie);

        return resultado;
    }

    private long getCountRespuestasItems(List<Object[]> respuestas) {
        long numRespuestas=0;
        for(int i=0;i<respuestas.size();i++) {
            Number frecuencia=(Number)respuestas.get(i)[1];

            numRespuestas = numRespuestas + frecuencia.longValue();
        }

        return numRespuestas;
    }

    @Override
    @NamedSearch(parameterNames = "encuesta")
    public long getNumRespuestas(Encuesta encuesta) {
        Session session = sessionFactory.getCurrentSession();

        long numRespuestas;

        String shql = "SELECT count(*) FROM RespuestaEncuesta re WHERE re.encuesta.idEncuesta=?";
        Query query = session.createQuery(shql);
        query.setInteger(0, encuesta.getIdEncuesta());
        numRespuestas = (Long) query.uniqueResult();

        return numRespuestas;
    }

    private String getLabelFromValue(String value) {
        String label;

        if ((value == null) || (value.trim().equals(""))) {
            label = "NS/NC";
        } else {
            label = value;
        }

        return label;
    }

    private BigDecimal getDataFromRawData(long rawData, long numRespuestas) {
        double doubleData;
        if (numRespuestas != 0) {
            doubleData = ((double) (rawData * 100)) / (double) numRespuestas;
        } else {
            doubleData = 0;
        }
        BigDecimal bigDecimalData = new BigDecimal(doubleData);
        BigDecimal data = bigDecimalData.setScale(numDecimals, RoundingMode.HALF_UP);

        return data;
    }
}

