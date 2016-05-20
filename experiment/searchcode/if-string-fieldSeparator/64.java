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
import fr.cemagref.prima.regionalmodel.Individual;
import fr.cemagref.prima.regionalmodel.MunicipalitySet;
import fr.cemagref.prima.regionalmodel.Municipality;
import fr.cemagref.prima.regionalmodel.parameters.DecisionByAgeAndProbas;
import fr.cemagref.prima.regionalmodel.parameters.Value;
import fr.cemagref.prima.regionalmodel.scenarios.Event;
import fr.cemagref.prima.regionalmodel.scenarios.EventsWrapper;
import fr.cemagref.prima.regionalmodel.scenarios.Scenario;
import fr.cemagref.prima.regionalmodel.tools.BadDataException;
import fr.cemagref.prima.regionalmodel.tools.ProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.File;

/**
 *
 * @author Baqueiro
 */
@ServiceProvider(service = Scenario.class)
public class GermanyInactiveToInactiveTransition extends Scenario {

    /** Contains the list of events */
    List<EventsWrapper> eventsWrappers;
    String filename="";
    String fieldSeparator = ";";
    MunicipalitySet _region;

    @Override
    public List<? extends Event<MunicipalitySet>> getEvents() {
        return eventsWrappers;
    }

    @Override
    public final void init(final MunicipalitySet region) throws BadDataException {

        super.init(region);

        this._region = region;
        final int beginYear = region.getParameters().getStartStep();
        final int endYear = region.getParameters().getStartStep() + region.getParameters().getNbStep() + 1;
        eventsWrappers = new ArrayList<EventsWrapper>(endYear - beginYear + 1);
        for (int i = 0; i < endYear - beginYear + 1; i++) {
            eventsWrappers.add(new EventsWrapper());
        }

        if (region.getParameters().getFieldSeparator() != null) {
            fieldSeparator = region.getParameters().getFieldSeparator();
        }

        // add job municipality dynamics table event
        for (Municipality mun : municipalities) {
            // read dynamics for this municipality                 
            SortedMap<Integer, Value<Boolean, Individual>> dynamics = readDynamicsFile(mun);
            int i = beginYear;
            for (EventsWrapper ew : eventsWrappers) {
                if (dynamics.get(i) != null) {
                    ew.put(mun, buildEvent(dynamics.get(i)));
                }                 
            }

        }
    }

    private SortedMap<Integer, Value<Boolean, Individual>> readDynamicsFile(Municipality mun) throws BadDataException {
        SortedMap<Integer, Value<Boolean, Individual>> dynamics = new TreeMap<Integer, Value<Boolean, Individual>>();
        BufferedReader reader = null;
        try {
            MunicipalitySet region = mun.getMyRegion();
            File municipalityFilename = new File(mun.getMyRegion().getMyApplication().getMunicipalityDirectory(mun), filename);
            reader = new BufferedReader(new FileReader(municipalityFilename));
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.length() > 0) {
                    String[] fields = line.split(fieldSeparator);

                    if (fields.length < 2) {
                        throw new BadDataException("Each row of the dynamics events file must contain exactly 2 items: year,table_file");
                    }
                    String probaFile = mun.getName() + "\\" + fields[1];
                    Value<Boolean, Individual> dynTable = new DecisionByAgeAndProbas(probaFile);
                    dynTable.init(region.getMyApplication());

                    dynamics.put(new Integer(fields[0]), dynTable);
                }
            }
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(null).error("Error loading Job Vacancy Weights file", ex);
        }



        return dynamics;
    }

    /**
     * Clears the list of dynamics before adding the dynamics for the new step
     */
    @Override
    protected void preStep() {
        this._region.initInactiveInactiveScenarioDynamic();
    }

    private static class GermanyInactiveToInactiveDynamicsEvent implements Event<Municipality> {

        Value<Boolean, Individual> distribution;

        public GermanyInactiveToInactiveDynamicsEvent(Value<Boolean, Individual> newDistribution) {
            distribution = newDistribution;
        }

        @Override
        public void process(Municipality munic) throws ProcessingException {
            munic.getMyRegion().addInactiveInactiveScenarioDynamic(munic, distribution);
        }
    }

    private GermanyInactiveToInactiveDynamicsEvent buildEvent(Value<Boolean, Individual> newDistribution) {
        GermanyInactiveToInactiveDynamicsEvent event = new GermanyInactiveToInactiveDynamicsEvent(newDistribution);
        return event;

    }
}

