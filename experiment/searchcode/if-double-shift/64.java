/*
 *  Copyright (C) 2010 Cemagref
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cemagref.prima.regionalmodel.auvergne;

import fr.cemagref.prima.regionalmodel.Household;
import fr.cemagref.prima.regionalmodel.Individual;
import fr.cemagref.prima.regionalmodel.Municipality;
import fr.cemagref.prima.regionalmodel.scenarios.Event;
import fr.cemagref.prima.regionalmodel.scenarios.ImmigrationUpdater;
import fr.cemagref.prima.regionalmodel.scenarios.ImmigrationEvent;
import fr.cemagref.prima.regionalmodel.tools.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Nicolas Dumoulin <nicolas.dumoulin@cemagref.fr>
 */
@ServiceProvider(service = ImmigrationUpdater.class)
public class FranceImmigrationUpdater extends ImmigrationUpdater {

    private SortedMap<Integer, double[]> cspProbasByYear=null;

    @Override
    protected Event<Municipality> buildEvent(String[] line, boolean useMigratoryBalance) {
        return new FranceImmigrationEvent(line, useMigratoryBalance);
    }

    private class FranceImmigrationEvent extends ImmigrationEvent {

        private List<Double> probaSizeHH;
        private List<Integer> ageClassesSupBound;
        private List<Double> probaAgeClass;

        public FranceImmigrationEvent(String[] line, boolean useMigratoryBalance) {
            super(line[0], useMigratoryBalance);
            int shift = 1;
            probaSizeHH = new ArrayList<Double>();
            for (String value : Arrays.copyOfRange(line, shift + 1, shift + 1 + Integer.parseInt(line[shift]))) {
                probaSizeHH.add(Double.parseDouble(value));
            }
            ageClassesSupBound = new ArrayList<Integer>();
            probaAgeClass = new ArrayList<Double>();
            shift += 1 + probaSizeHH.size();
            int nbAgeClasses = Integer.parseInt(line[shift]);
            // The last bound is
            for (String value : Arrays.copyOfRange(line, 1 + shift, 1 + shift + nbAgeClasses - 1)) {
                ageClassesSupBound.add(Integer.parseInt(value));
            }
            shift += ageClassesSupBound.size();
            ageClassesSupBound.add(Integer.MAX_VALUE);
            for (String value : Arrays.copyOfRange(line, 1 + shift, 1 + shift + nbAgeClasses)) {
                probaAgeClass.add(Double.parseDouble(value));
            }
        }

        /**
         * Method to decide if an household can represent an immigrant household S. Huet, 3.11.2010
         * That is overloaded to consider that a given existing household can represent an immigrant if it probably correspond to an immigrant
         * (respecting some size feature and age of individual probability)
         */
        @Override
        protected boolean evaluate(Household hh) {
            boolean good = super.evaluate(hh);
            if (good) {
                // Take the probability for the given size of household
                double probSize = probaSizeHH.get(Math.min(hh.getSize() - 1, probaSizeHH.size() - 1));
                // Compute the average probability for the age of the household member
                double probAge = 0.0f;
                for (Individual indiv : hh.getCopyOfMembers()) {
                    // look for the age range of the individual
                    for (int j = 0; j < ageClassesSupBound.size(); j++) {
                        if (indiv.getAge() <= ageClassesSupBound.get(j)) {
                            probAge = probAge + probaAgeClass.get(j);
                            break;
                        }
                    }
                }
                probAge = probAge / hh.getSize();
                if (hh.getRandom().nextDouble() > (double) ((probAge + probSize) / 2)) {
                    good = false;
                }
            }
            return good;
        }

        @Override
        protected void postprocessImmigrants(Household hh) {
            super.postprocessImmigrants(hh);
            int p = 0;
            // Adapt the profession and the activity status
            int[] r = {5, 15, 20, 30, 40, 50, 65, 180};
            for (Individual ind : hh) {
                // Attribution of a profession following the distribution of profession in the "real" data
                double[] probas = Utils.getValueFromUpperBound(cspProbasByYear, ind.getHousehold().getMyVillage().getMyRegion().getCurrentYear());
                p = ind.getHousehold().getRandom().nextIndexWithDistribution(probas);
                if (ind.getStatus() != Individual.Status.RETIRED) {
                    ind.setProfession(p);
                }
                // Statistics ages
                for (int i = 0; i < r.length; i++) {
                    if (ind.getAge() <= r[i]) {
                        ind.getHousehold().getMyVillage().getMyRegion().immigrantAge[i]++;
                        i = r.length;
                    }
                }
                // Statistics about the status regarding the activity of immigrants
                if (ind.getStatus() == Individual.Status.RETIRED) {
                    hh.getMyVillage().getMyRegion().immigrantStatus[6]++;
                } else {
                    if (ind.getStatus() == Individual.Status.INACTIVE) {
                        hh.getMyVillage().getMyRegion().immigrantStatus[7]++;
                    } else {
                        if (ind.getStatus() != Individual.Status.STUDENT) {
                            hh.getMyVillage().getMyRegion().immigrantStatus[ind.getProfession()]++;
                        }
                    }
                }
            }
        }
    }
}

