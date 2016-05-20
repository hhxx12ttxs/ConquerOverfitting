/*
 * Copyright (C) 2011 Baqueiro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cemagref.prima.regionalmodel.altmark.scenarios;

import common.Logger;
import fr.cemagref.prima.regionalmodel.Activity;
import fr.cemagref.prima.regionalmodel.MunicipalitySet;
import fr.cemagref.prima.regionalmodel.Municipality;
import fr.cemagref.prima.regionalmodel.parameters.Parameters;
import fr.cemagref.prima.regionalmodel.scenarios.Event;
import fr.cemagref.prima.regionalmodel.scenarios.EventsWrapper;
import fr.cemagref.prima.regionalmodel.scenarios.Scenario;
import fr.cemagref.prima.regionalmodel.scenarios.TableUpdater.UpdateMode;
import fr.cemagref.prima.regionalmodel.tools.BadDataException;
import fr.cemagref.prima.regionalmodel.tools.ProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import fr.cemagref.prima.regionalmodel.tools.Files;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Baqueiro
 */
@ServiceProvider(service = Scenario.class)
public class GermanyJobsUpdater extends Scenario {

    /** Contains the list of events */
    List<EventsWrapper> eventsWrappers;
    String filename="";
    String fieldSeparator=";";

    @Override
    public List<? extends Event<MunicipalitySet>> getEvents() {
        return eventsWrappers;
    }

    @Override
    public final void init(final MunicipalitySet region) throws BadDataException {
        
        super.init(region);
                

        final int beginYear = region.getParameters().getStartStep();
        final int endYear = region.getParameters().getStartStep() + region.getParameters().getNbStep()+1;
        eventsWrappers = new ArrayList<EventsWrapper>(endYear - beginYear + 1);
        for (int i = 0; i < endYear - beginYear + 1; i++) {
            eventsWrappers.add(new EventsWrapper());
        }
        
        if ( region.getParameters().getFieldSeparator()!=null){
            fieldSeparator = region.getParameters().getFieldSeparator();
        }
        // read job vacancy weights
        SortedMap<Activity, Double> vacancyWeights = readVacancyWeightsFile(region);
        
        
        // Normalize vacancy weigths so that the sum of the table equals 1
        vacancyWeights = normalizeWeights(vacancyWeights);
        
        // add job vacancy events
        for (Municipality mun : municipalities) {
            for (EventsWrapper ew : eventsWrappers) {
                ew.put(mun, buildEvent(region.getParameters().getJobVacancyRate(), vacancyWeights));
            }
        }
    }

    private SortedMap<Activity, Double> readVacancyWeightsFile(final MunicipalitySet region) throws BadDataException {
        double[] weights =
                new double[region.getParameters().getNbSPC()*region.getParameters().getNbSoA()];

        SortedMap<Activity, Double> weightsTable = new TreeMap<Activity, Double>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(region.getMyApplication().getFileFromRelativePath(filename)));
          
            final String line = Files.readLine(reader);         
            if (line!=null && line.length()>0){
                final String[] fields = line.split(fieldSeparator);                                
                if (fields.length<weights.length){     
                    throw new BadDataException( 
                            String.format
                            ("The number of fields [%d] in the Job Vacancy"+
                            " weights input file is less than the total number of activities [%d]",fields.length, weights.length));
                }
                
                for (int i=0;i < weights.length;i++) {                    
                    weightsTable.put(region.getParameters().createActivityFromIndex(i),Double.parseDouble(fields[i]) );
                }
                
                
            }            
            
        } catch (IOException ex) {
            Logger.getLogger(null).error("Error loading Job Vacancy Weights file", ex);
        }
        finally{
           //Close the BufferedReader
            try {
                if (reader != null){reader.close();}
            } catch (IOException ex) {
                 Logger.getLogger(null).error("Error closing Job Vacancy Weights file", ex);
            }
        }
        
        
        
        return weightsTable;
    }
    /**
     * Normalize the values (so that they sum to 1)  of the weights in the specified weight table.
     * This function will modify the values of the Map passed in the parameters. 
     * @param weights Table with weights to be normalized
     * @return  reference to the weights map
     */
    private SortedMap<Activity, Double> normalizeWeights(SortedMap<Activity,Double> weights) {        
        // calculate total
        double total = 0;
        for (Double value: weights.values()){
            total += value;
        }                
        // divide each cell between the total
        for (Map.Entry<Activity,Double> entry: weights.entrySet()){        
            entry.setValue(entry.getValue()/total);
            
        }
        return weights;
    }

    private static class GermanyJobsEvent implements Event<Municipality> {

        SortedMap<Activity, Double> vacancyWeights;
        double rate;

        public GermanyJobsEvent(double jr, SortedMap<Activity, Double> vacancy) {
            vacancyWeights = vacancy;
            rate = jr;
        }

        @Override
        public void process(Municipality munic) throws ProcessingException {

            // get the total amount of people working in this municipality
            int totalJobs = munic.getTotalWorkers();
            Parameters params = munic.getMyRegion().getParameters();
            // get the total number of new workers
            int newJobs = (int) ((totalJobs * rate) / (1 - rate));
            SortedMap<Activity, Integer> jobsMap = new TreeMap<Activity, Integer>();
            // if there are new workers then create them according to vacancyWeights
            while (newJobs > 0) {
                double rnd = munic.getMyRegion().getRandom().nextDouble();
                double cum = 0;
                int index = 0;
                for (Double item : vacancyWeights.values()){
                    cum += item;
                    if (cum > rnd) {
                        break;
                    }
                    index++;
                    if (cum > rnd) {
                        break;
                    }
                }                
                Activity act =  params.createActivityFromIndex(index);
                if (jobsMap.get(act) == null) {
                    jobsMap.put( act, 1);
                } else {
                    jobsMap.put( act, jobsMap.get(act) + 1);
                }
                newJobs--;
            }
            for (Map.Entry<Activity, Integer> e : jobsMap.entrySet()) {
                munic.updateOfferedActivities(e.getKey(), e.getValue(), UpdateMode.DELTA);
            }
        }
    }

    private GermanyJobsEvent buildEvent(double jr, SortedMap<Activity, Double> vacancy) {
        GermanyJobsEvent event = new GermanyJobsEvent(jr, vacancy);

        return event;

    }
}

