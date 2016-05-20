package it.unina.gaeframework.geneticalgorithm.servlet;

import it.unina.gaeframework.geneticalgorithm.statistics.ChromosomeDataContainer;
import it.unina.gaeframework.geneticalgorithm.statistics.GeneticStatistics;
import it.unina.gaeframework.geneticalgorithm.stopcriteria.CheckStopCriteria;
import it.unina.gaeframework.geneticalgorithm.util.GAEUtils;
import it.unina.gaeframework.geneticalgorithm.util.GAKeyFactory;
import it.unina.tools.datastore.DatastoreLoadAndSave;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jsr107cache.Cache;

import com.google.appengine.api.datastore.Key;

@SuppressWarnings("serial")
public class MonitorServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	throws IOException {
		resp.setContentType("text/html");
		DatastoreLoadAndSave data = new DatastoreLoadAndSave();
		Cache cache = GAEUtils.getCache();
		
//		Map<String, Object> parameters = new HashMap<String, Object>();
//		parameters.put("",null);
		
		//recuperiamo l'oggetto geneticStatistics
		Key geneticStatisticskey = GAKeyFactory.getGAKeyFactoryInstance().getGenetisStatisticsKey();
		
		//proviamo a recuperarlo dalla cache
		GeneticStatistics geneticStatistics = (GeneticStatistics) cache.get(geneticStatisticskey);
		
		if (geneticStatistics == null){
			//se il caricamento dalla cache non ha successo lo carichiamo dal datastore
			geneticStatistics = data.loadObjectById(GeneticStatistics.class, geneticStatisticskey.getId());
		}
		
//		List<GeneticStatistics> l = (List<GeneticStatistics>) data.load(null, GeneticStatistics.class);
		if(geneticStatistics == null){ 
			resp.getWriter().write("Attendere prego...");
			resp.setHeader("Refresh", "5");			
		}else{
//			GeneticStatistics gs = l.get(0);
			CheckStopCriteria check = new CheckStopCriteria(geneticStatistics);
			if(!check.checkStopCriteria()){
				resp.getWriter().write("Iterazione attuale: "+geneticStatistics.getActualIteration());
				resp.getWriter().write(" Iterazioni massime: "+geneticStatistics.getMaxIterations());
				resp.setHeader("Refresh", "10");
				resp.getWriter().write("</br>Fitness Totale: "+geneticStatistics.getBestFitness()+"</br></br>");

				//TODO: Decidere se utilizzare la Cache anche per i ChromosomeDataContainer
				List<ChromosomeDataContainer> lChr = (List<ChromosomeDataContainer>) data.load(null, ChromosomeDataContainer.class);
				if(lChr.size()>0){
					Collections.sort(lChr);
					Long totalComputationTime = 0L;
					Long totalEvaluations= 0L;
					
					//Stampa dei risultati parziali del test
					for(ChromosomeDataContainer chr : lChr){
						resp.getWriter().write(chr.toString()+"</br>");
						totalComputationTime += chr.getComputationTimeInMillis();
						totalEvaluations += chr.getTotalEvaluationDone();
					}

					//Stampa dei totali
					DateFormat f = new SimpleDateFormat("H' hours, 'm' minutes, 's' seconds, 'SSS' milliseconds'");
					f.setTimeZone(TimeZone.getTimeZone("UTC"));
					String computationTime = "</br>Tempo di calcolo totale: "+ f.format(totalComputationTime)+"</br>";
					String evaluations= "</br>Numero totale di valutazioni: "+ totalEvaluations+"</br>";
					resp.getWriter().write(evaluations+computationTime);					
				}
			}else {
				resp.getWriter().write("Fitness Totale: "+geneticStatistics.getBestFitness()+"</br></br>");
				List<ChromosomeDataContainer> lChr = (List<ChromosomeDataContainer>) data.load(null, ChromosomeDataContainer.class);
				if(lChr.size()<=0){
					//TODO: gestione dell'errore
				}
				
				Collections.sort(lChr);
				Long totalComputationTime = 0L;
				Long totalEvaluations= 0L;
				
				//Stampa dei risultati finali del test
				for(ChromosomeDataContainer chr : lChr){
					resp.getWriter().write(chr.toString()+"</br>");
					totalEvaluations += chr.getTotalEvaluationDone();
					totalComputationTime += chr.getComputationTimeInMillis();
				}
				
				//Stampa dei totali
				DateFormat f = new SimpleDateFormat("H' hours, 'm' minutes, 's' seconds, 'SSS' milliseconds'");
				f.setTimeZone(TimeZone.getTimeZone("UTC"));
				String computationTime = "</br>Tempo di calcolo totale: "+ f.format(totalComputationTime)+"</br>";
				String evaluations= "</br>Numero totale di valutazioni: "+ totalEvaluations+"</br>";
				resp.getWriter().write(evaluations+computationTime);
				
			}
			
			//Stampa del link alla servlet di monitoraggio dei mapper
			resp.getWriter().write("</br><a href=\"/mapreduce/status\" target=\"_blank\"> Controlla map</a>");
		}
	}
}

