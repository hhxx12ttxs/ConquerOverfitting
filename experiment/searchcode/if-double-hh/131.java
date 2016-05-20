/*
 *  Copyright (C) 2010 sylvie.huet
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

import au.com.bytecode.opencsv.CSVReader;
import fr.cemagref.prima.regionalmodel.Household;
import fr.cemagref.prima.regionalmodel.Individual;
import fr.cemagref.prima.regionalmodel.MunicipalitySet;
import fr.cemagref.prima.regionalmodel.dynamics.EmigrationUpdater;
import fr.cemagref.prima.regionalmodel.tools.BadDataException;
import fr.cemagref.prima.regionalmodel.tools.CSV;
import fr.cemagref.prima.regionalmodel.tools.ProcessingException;
import fr.cemagref.prima.regionalmodel.tools.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = EmigrationUpdater.class)
public class FranceEmigrationUpdater extends EmigrationUpdater {

    // These proba means "if you have a given profession, the probability you are a migrant out of the bunch is this one"
    private Map<Integer, double[]> emigrationsProbasByProfByYear=null;
    private String inputFilename="";
    private Character separator=';';
    private transient Map<Integer, Data> datas;
    private transient Data currentData;

    private static class Data {

      
        List<Integer> ageClassesSupBound = new ArrayList<Integer>();
        List<Double> probaAgeClass = new ArrayList<Double>();
    }

    @Override
    public void step(MunicipalitySet municipalitySet, int iter) throws ProcessingException {
        if (datas.get(iter) != null) {
            currentData = datas.get(iter);
        }
        // if no new data found, we reuse the last one
        if (currentData != null) {
            super.step(municipalitySet, iter);
        }
    }

    @Override
    public void init(MunicipalitySet region) throws BadDataException {
        super.init(region);
        datas = new TreeMap<Integer, Data>();
        // read input file
        try {
            CSVReader reader = CSV.getReader(region.getMyApplication().getFileFromRelativePath(inputFilename), separator);
            CSV.readYearlyData(reader, new CSV.ArrayParser() {

                @Override
                public void parse(int date, String[] line) throws BadDataException {
                    Data data = new Data();                 
                    int shift = 1;
                    int nbAgeClasses = Integer.parseInt(line[shift]);
                    Collections.addAll(data.ageClassesSupBound, Utils.parseIntegerArray(line, 1 + shift, 1 + shift + nbAgeClasses - 1 - 1));
                    shift += data.ageClassesSupBound.size();
                    data.ageClassesSupBound.add(Integer.MAX_VALUE);
                    Collections.addAll(data.probaAgeClass, Utils.parseDoubleArray(line, 1 + shift, 1 + shift + nbAgeClasses - 1));
                    datas.put(date, data);
                }
            });
            reader.close();
        } catch (IOException ex) {
            throw new BadDataException("", ex);
        }
    }

    @Override
    public void emigration(Household hh) {
        double probProf = 0.0;
        int nbWorkerAndUnemp = 0;
        // The emigration can be caused by the non satisfaction of the need of residence (or the need of job or ...)
        if (hh.isJustSuppressed() || hh.getResidence().isTransit()) { return;}
        
        double probAge = 0.0f;
        for (Individual indiv : hh) {
            for (int j = 0; j < currentData.ageClassesSupBound.size(); j++) {
                if (indiv.getAge() <= currentData.ageClassesSupBound.get(j)) {
                    probAge += currentData.probaAgeClass.get(j);
                    break;
                }
            }

            if (emigrationsProbasByProfByYear != null
                    && (indiv.getStatus() == Individual.Status.UNEMPLOYED
                    || indiv.getStatus() == Individual.Status.WORKER)) {
                if (indiv.getHousehold().getMyVillage().getMyRegion().getCurrentYear() <= 1999) {
                    probProf += emigrationsProbasByProfByYear.get(1999)[indiv.getProfession()];
                    //System.err.println("status "+indiv.getStatus()+" prof "+indiv.getProfession()+" prob "+probProf+" nbworkerune "+nbWorkerAndUnemp+" hsh "+hh.getId()+" lu "+probaMigrOutByProf1999[indiv.getProfession()]) ;
                } else {
                    if (indiv.getHousehold().getMyVillage().getMyRegion().getCurrentYear() <= 2006) {
                        probProf += emigrationsProbasByProfByYear.get(2006)[indiv.getProfession()];
                    }
                }
                nbWorkerAndUnemp++;
            }
        }
        probAge = probAge / hh.getSize();
        probProf = probProf / nbWorkerAndUnemp;
        probAge = (probAge + probProf) / 2;
        if (hh.getRandom().nextDouble() <= probAge) {
            hh.getMyVillage().suppressHousehold(hh,  true);
            // Statistics about the status regarding the activity of emigrants
            int[] r = {5, 15, 20, 30, 40, 50, 65, 180};
            for (Individual ind : hh.getCopyOfMembers()) {
                // Statistics ages
                for (int i = 0; i < r.length; i++) {
                    if (ind.getAge() <= r[i]) {
                        ind.getHousehold().getMyVillage().getMyRegion().emigrantAge[i]++;
                        i = r.length;
                    }
                }
                // Statistics about working status
                if (ind.getStatus() == Individual.Status.RETIRED) {
                    hh.getMyVillage().getMyRegion().emigrantStatus[6]++;
                } else {
                    if (ind.getStatus() == Individual.Status.INACTIVE) {
                        hh.getMyVillage().getMyRegion().emigrantStatus[7]++;
                    } else {
                        if (ind.getStatus() != Individual.Status.STUDENT) {
                            hh.getMyVillage().getMyRegion().emigrantStatus[ind.getProfession()]++;
                        }
                    }
                }
            }


        }
    }
}

