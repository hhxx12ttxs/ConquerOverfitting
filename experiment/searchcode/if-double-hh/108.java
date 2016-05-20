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
package fr.cemagref.prima.regionalmodel;

import fr.cemagref.prima.regionalmodel.Individual.Status;
import fr.cemagref.prima.regionalmodel.dynamics.Dynamics;
import fr.cemagref.prima.regionalmodel.parameters.MunicipalityParameters;
import fr.cemagref.prima.regionalmodel.parameters.OutsideParameters;
import fr.cemagref.prima.regionalmodel.scenarios.TableUpdater.UpdateMode;
import fr.cemagref.prima.regionalmodel.tools.BadDataException;
import fr.cemagref.prima.regionalmodel.tools.ProcessingException;
import fr.cemagref.prima.regionalmodel.tools.Random;
import fr.cemagref.prima.regionalmodel.tools.Updatable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Nicolas Dumoulin <nicolas.dumoulin@cemagref.fr>
 */
public class Municipality {
 // TODO-OMAR: Remove temporal logging
  /*  public int moveDueToJob=0;
   public int moveDueToCouple=0;*/
    private  final Logger myLogger;
    private String name;
    /**
     * Geographical coordinates
     */
    private double lon, lat;
    /**
     * It stores the municipalies considered as neighbour for each type of Service. The proximity for an hospital doesn't mean the same than for a bakery. It isn't used for
     * instance.
     */
    protected Map<Service, Municipality[]> proximityByService;
    protected List<Dynamics<Municipality>> dynamics;
    
    /** List of Individual working in this municipality. Must be updated by updateWorkersList() before accessing to guarantee correct state **/
    protected List<Individual> workers;
    /**
     * Total surface avaiable in the village which is dedicated for agriculture (farms).
     */
    private double totalSurface;
    /**
     * This vector shows the total number of residences, for each type of residence in the village.
     * For example, let's consider that were defined three diferent types of residences:
     * <ul><li>type 1: residences with 1 or 2 bedrooms
     * <li>type 2: residences with 3 or 4 bedrooms
     * <li>type 3: residences with more than 4 bedrooms</ul>
     * In this example, the vector has dimension 3, so:
     * <ul><li>residencesOffer[0]: number of residences of type 1;
     * <li>residencesOffer[1]: number of residences of type 2;
     * <li>residencesOffer[2]: number of residences of type 3.
     */
    private int[] residencesOffer;
    private List<Residence>[] freeResidences;
    private List<Residence>[] occupiedResidences;
    /**
     * Number of immigrants arriving in this municipality during a given iteration
     */
    public int nbImmigrants;
    /**
     * Number of individuals living in the village
     */
    private int populationSize;
    /**
     * Table containing the level of occupation activities of the activities for the village (constrained by the availability) It is affected by the fact individuals have patterns
     * of activity using or not the activities This corresponds only to the activities occupied by the external individuals to the village but at the same time living in the considered region
     */
    public SortedMap<Activity, Integer> occupiedActivitiesByOutside;
    /**
     * Table containing the level of occupation activities of the activities for the village (constrained by the availability) It is affected by the fact individuals have patterns
     * of activity using or not the activities This corresponds only to the activities occupied by the external individuals to the village but at the same time living in the considered region
     */
    private SortedMap<Activity, Integer> occupiedActivitiesByExt;
    /**
     * Table containing the level of occupation activities of the activities for the village (constrained by the availability) It is affected by the fact individuals have patterns
     * of activity using or not the activities This corresponds only to the activities occupied by the residents of the village
     */
    private SortedMap<Activity, Integer>  occupiedActivitiesByRes;
    /**
     * Table containing the offered (ex called availability) activities in the village. An activity is a job in the model. It corresponds to the potential of employments offered by
     * the village It is affected by the political and economical decision at the village and higher levels It does not include the employment available in the accessible villages
     * or cities
     */
    private SortedMap<Activity, Integer> totalOfferedJob;
    /**
     * Table containing the endogeneous offered (ex called availability) activities in the village. An activity is a job in the model. It corresponds to the potential of employments offered by
     * the village It is affected by the political and economical decision at the village and higher levels It does not include the employment available in the accessible villages
     * or cities. This part describes employments linked to the presence of people in a given place. It is updated endogeneously via a function proposed by Lenormand and al (2011).
     * See computeDynamicEmployServices() in MunicipalitySet
     */
    private SortedMap<Activity, Integer> endogeneousOfferedJob;
    /**
     * Table containing the exogeneous offered (ex called availability) activities in the village. An activity is a job in the model. It corresponds to the potential of employments offered by
     * the village It is affected by the political and economical decision at the village and higher levels It does not include the employment available in the accessible villages
     * or cities. This part is updated by the scenario.
     */
    private SortedMap<Activity, Integer> exogeneousOfferedJob;
      
    
    
    /**
     * Parameters for the function making dynamic the employment offer in the service sector: slope
     */
    private double slope;
    /**
     * Parameters for the function making dynamic the employment offer in the service sector: intercept
     */
    private double intercept;

    public double getIntercept() {
        return intercept;
    }

    public double getSlope() {
        return slope;
    }
    /**
     * Table of Villages containing instance of Villages accessible to this Municipality
     */
    private Municipality[] proximityJob;
    /**
     * Table containing the households living in the village
     */
    protected List<Household> myHouseholds;
    private Set<Individual> myWorkers;
    private MunicipalitySet myRegion;

    public Logger getLogger() {
        return myLogger;
    }

    public void init() throws BadDataException {
        for (Dynamics<Municipality> dyn : dynamics) {
            dyn.init(this);
        }
    }

    public void step(int iter) throws ProcessingException {
        for (Dynamics<Municipality> dyn : dynamics) {
            dyn.step(this, iter);
        }
    }

    public String getName() {
        return name;
    }

    public Iterable<Residence> getOccupiedResidences(int type) {
        return occupiedResidences[type];
    }

    public Residence pickAnOccupiedResidence(int type) {
        return occupiedResidences[type].get(getRandom().nextInt(0, occupiedResidences[type].size() - 1));
    }

    public int getOccupiedResidencesCount(int type) {
        return occupiedResidences[type].size();
    }

    /**
     * Find the closest municipality to the current one using the euclidien distance.
     * @param mun1
     * @param mun2
     * @return
     */
    public Municipality closest(Municipality mun1, Municipality mun2) {
        double dist1 = Math.pow(mun1.lon - this.lon, 2) + Math.pow(mun1.lat - this.lat, 2);
        double dist2 = Math.pow(mun2.lon - this.lon, 2) + Math.pow(mun2.lat - this.lat, 2);
        return dist1 < dist2 ? mun1 : mun2;
    }

    public void updatePopSize() {
        int pop = 0;
        for (Household hs : getMyHouseholds()) {
            pop = pop + hs.listOfMembers.size();
        }
        this.populationSize = pop;
    }

    public int getCurrentPopSize() {
        int pop=0;
        for (Household hs : getMyHouseholds()) {
            if (!hs.isJustSuppressed()) {
                pop+=hs.listOfMembers.size();
            }
        }
        return pop;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public SortedMap<Activity, Integer>getOccupiedActivitiesByOutside() {
        return occupiedActivitiesByOutside;
    }

    public int getOccupiedActivitiesByOutside(Activity act) {
        assert act!=null : "Activity parameter must NOT be null";
        if (occupiedActivitiesByOutside==null){
            occupiedActivitiesByOutside = new TreeMap<Activity, Integer>();        
        }
        
        if (!occupiedActivitiesByOutside.containsKey(act)){
                occupiedActivitiesByOutside.put(act, 0);
        }
        
        
        return occupiedActivitiesByOutside.get(act);
    }

    public void setOccupiedActivitiesByOutside( Activity act, int newValue){
        this.occupiedActivitiesByOutside.put(act, newValue);
    }
    
    public int getOccupiedActivitiesByExt(final Activity activ) {
        if (occupiedActivitiesByExt==null ) {
            occupiedActivitiesByExt = new TreeMap<Activity, Integer>();
            
        }
        
        if (!occupiedActivitiesByExt.containsKey(activ)){
            occupiedActivitiesByExt.put(activ,0);
        }
        return this.occupiedActivitiesByExt.get(activ);

    }        

    public int getOccupiedActivitiesByRes(final Activity activ) {
        if (occupiedActivitiesByRes == null) {
            occupiedActivitiesByRes = new TreeMap<Activity, Integer>();

        }
        if (!occupiedActivitiesByRes.containsKey(activ)) {
            occupiedActivitiesByRes.put(activ, 0);
        }
        return this.occupiedActivitiesByRes.get(activ);
    }
   
    public int getOfferedActivities(final Activity activ) {
        assert activ!=null:
                String.format("The selected activity must NOT be null");
        if (totalOfferedJob==null){
            totalOfferedJob = new TreeMap<Activity, Integer>();
        }
        if (!totalOfferedJob.containsKey(activ)){
            totalOfferedJob.put(activ, 0);
        }
        return totalOfferedJob.get(activ);
    }

    public void setOfferedActivities(final SortedMap<Activity, Integer> offeredActivities) {
        this.totalOfferedJob = offeredActivities;
    }

    public void setOfferedActivities(final Activity act, final int offeredActivities) {
        this.totalOfferedJob.put(act, offeredActivities);
    }


    @Updatable
    public void updateOfferedActivities(Activity activity, Integer value, UpdateMode mode) {
        int toCreateOutside = 0;

        int tot = 0;
        int curJobs = getExogenousOfferedJob(activity);
        int delta = UpdateMode.computeDelta(curJobs, value, mode);
        if (curJobs + delta < 0) {
            throw new UnsupportedOperationException("Error in activities scenario: delta can be "
                    + "greatest than the current value (category=" + activity + ", value="
                    + curJobs + ", delta=" + delta + ")");
        } else {
            if (delta > 0) {
                // then employment creation to share between occupation by people from outside and potential occupation (offer) by insiders
                tot = getOccupiedActivitiesByOutside(activity) + getOccupiedActivitiesByRes(activity) + getOccupiedActivitiesByExt(activity);
                if (tot == 0 && getOccupiedActivitiesByOutside(activity) == 0) {
                    if (delta == 1) { // try to avoid the bias consisting in putting all the job offer occupied by outside
                       toCreateOutside =  getRandom().nextDouble() > 0.5 ? 0 : 1;                        
                    } else {
                        toCreateOutside = Math.round(delta * 0.5f);
                    }
                } else {
                    if ((tot == getOccupiedActivitiesByOutside(activity) * 2) && delta == 1) {
                        toCreateOutside = getRandom().nextDouble() > 0.5 ? 0 : 1;
                        
                    } else {
                        toCreateOutside = Math.round(delta * ((float) getOccupiedActivitiesByOutside(activity) / tot));
                    }
                }
                setOccupiedActivitiesByOutside(activity,getOccupiedActivitiesByOutside(activity) + toCreateOutside);                
            }
            
            setExogeneousOfferedJob(activity, curJobs+delta);            
            totalOfferedJob.put(activity, getExogenousOfferedJob(activity) + getEndogenousOfferedJob(activity));
        }
    }
    
    private Random getRandom() {
        return getMyRegion().getMyApplication().getRandom();
    }

    public Municipality[] getProximityJob() {
        return proximityJob;
    }

    public void setProximityJob(Municipality... proximityJob) {
        this.proximityJob = proximityJob;
    }
    public void setProximityJob(List<Municipality> munic){
        assert munic!=null: "List of proximity jobs must NOT be null. Found null";
        this.proximityJob = munic.toArray(new Municipality[0]);
    }

    public List<Household> getMyHouseholds() {
        return myHouseholds;
    }
    
    /**
     * Returns a copy of the households in this municipality.
     * Useful when iterating over households to perform operations that may
     * modify the household list
     * 
     * @return A copy of the list of households in this Municipality
     */
    public List<Household> getCopyOfHouseholds() {
        List<Household> members = new ArrayList<Household>(myHouseholds.size());
        members.addAll(myHouseholds);
        return members;
    }

    /**
     * Return a list of households which are not in transit and not suppressed.
     * @return List of 'effective' households in the municipality. 
     */
    public List<Household> getStableHouseholds(){
        final List<Household> newList=new ArrayList<Household>();
        for (Household house : this.myHouseholds){
            if (house.isJustSuppressed() || house.getResidence().isTransit()){
                continue;
            }
            
            newList.add(house);
        }
        
        return newList;
    }
    public Household findHouseholdById(long id) {
        for (Household hh : myHouseholds) {
            if (hh.getId() == id) {
                return hh;
            }
        }
        return null;
    }

    public void removeWorker(Individual ind) {

        assert getMyWorkers().contains(ind):
                String.format("The municipality %s does not contain individual %s as a worker",
                        this.toString(), ind.toString());      
        getMyWorkers().remove(ind);   
        if (ind.getHousehold().getMyVillage().equals(this)){
            this.increaseOccupiedActivityRes(ind.getCurrentActivity(), -1);
            return;
        }
        if (!getMyRegion().getOutsides().contains(ind.getHousehold().getMyVillage())){
            this.increaseOccupiedActivityExt(ind.getCurrentActivity(), -1);
            return;
        }
    }

    public void addWorker(Individual ind) {
        int age = ind.getAgeToEnterOnTheLabourMarket();
        assert age>=0: "Age to enter labour market must be >=0. Found:"+age;        
        assert !getMyWorkers().contains(ind) : 
                String.format("Individual already in the worker list for %s at %s",
                ind, this);
         getMyWorkers().add(ind);  
            if (ind.getHousehold().getMyVillage().equals(this)){
            this.increaseOccupiedActivityRes(ind.getCurrentActivity(), 1);
            return;
        }
        if (!ind.getHousehold().getMyVillage().isOutside()){
            this.increaseOccupiedActivityExt(ind.getCurrentActivity(), 1);
            return;
        }
    }

    public MunicipalitySet getMyRegion() {
        return myRegion;
    }

    public void setMyRegion(MunicipalitySet myRegion) {
        this.myRegion = myRegion;
    }
    private MunicipalityCounters counters = new MunicipalityCounters();

    public void resetCounters() {
        nbImmigrants = 0;
        counters.resetCounters();
        counters.setInitialPopulation(this.getCurrentPopSize());
    }
    
    

    public MunicipalityCounters getCounters() {
        return counters;
    }   
    private Municipality(String name) {
        this.name = name;
        this.myLogger = Logger.getLogger(Municipality.class.getSimpleName() + "." + name);
        this.endogeneousOfferedJob = new TreeMap<Activity, Integer>();
        this.exogeneousOfferedJob = new TreeMap<Activity, Integer>();
    }

    /**
     * The constructor for the village where we temporarily stocked people who want to probabilisticMove
     */
    public Municipality(MunicipalitySet myReg, String name) {
        this(name);
        this.myRegion = myReg;
        // first index is for unemployed people
        this.totalOfferedJob = new TreeMap<Activity, Integer>();
        // obtain activities from MunicipalityiSet
        for (Activity act: myReg.getAllActivities()){
            totalOfferedJob.put(act, 0);            
        }
        
        this.myWorkers = new HashSet<Individual>();
        initOccupiedActivitiesCounters(totalOfferedJob);
    }

    /**
     * Constructor for tests
     * @param name
     * @param availabActiv
     */
    public Municipality(final String name, final SortedMap<Activity, Integer> availabActiv) {
        this(name);
        this.totalOfferedJob = availabActiv;
        initOccupiedActivitiesCounters(totalOfferedJob);
    }

    private void initOccupiedActivitiesCounters(Map<Activity, Integer> offeredJobs) {
                
        assert offeredJobs!=null : "Offered jobs must not be NULL";
        assert !offeredJobs.isEmpty() : "Offered jobs must not be empty ";
        
        occupiedActivitiesByExt = new TreeMap<Activity, Integer>();
        occupiedActivitiesByRes = new TreeMap<Activity, Integer>();
        occupiedActivitiesByOutside = new TreeMap<Activity, Integer>();
        
        for (Activity act : offeredJobs.keySet()){
            occupiedActivitiesByExt.put(act, 0);
            occupiedActivitiesByRes.put(act,0);
            occupiedActivitiesByOutside.put(act,0);
            
        }
    }

    public int getResidencesTypeCount() {
        return residencesOffer.length;
    }

    public int getResidenceOffer(int i) {
        return residencesOffer[i];
    }

    @Updatable
    protected void updateAvailableResidence(Integer category, Integer value, UpdateMode mode) {
        int delta = UpdateMode.computeDelta(residencesOffer[category], value, mode);
        if (residencesOffer[category] + delta < 0) {
            throw new UnsupportedOperationException("Error in residence scenario: delta can be "
                    + "greatest than the current value (category=" + category + ", value="
                    + residencesOffer[category] + ", delta=" + delta + ")");
        } else {
            residencesOffer[category] += delta;
            if (delta > 0) {
                // add residences
                for (int i = 0; i < delta; i++) {
                    freeResidences[category].add(new Residence(category));
                }
            } else if (delta < 0) {
                int nb = Math.abs(delta);
                if (nb > freeResidences[category].size()) {
                    // remove residences
                    freeResidences[category].clear();
                } else {
                    for (int i = 0; i < nb; i++) {
                        // remove residences
                        freeResidences[category].remove(0);
                    }
                }
            }
        }
    }

    @Updatable
    protected void updateAvailableResidence() {
        List<Residence> redundantHouses = new ArrayList<Residence>();
        for (int cat = 0; cat < getResidencesTypeCount(); cat++) {
            int redundantCount = occupiedResidences[cat].size() - residencesOffer[cat];
            if (redundantCount > 0) {
                redundantHouses.addAll(getRandom().pickElements(occupiedResidences[cat], redundantCount));
            }
        }
        int freeCat = 0;
        // Now we will try to find a new house for hh before to expulse them definitively
        List<Household> expulsed = new ArrayList<Household>();
        for (Residence redundantHouse : redundantHouses) {
            for (Household hh : redundantHouse) {
                while (freeCat < getResidencesTypeCount() && (freeResidences[freeCat].isEmpty())) {
                    freeCat++;
                }
                if (freeCat == getResidencesTypeCount()) {
                    // no more residence available in the municipality
                    // the household is expulsed
                    getLogger().log(Level.FINER, "Expulsion of HH {0}", hh.getId());
                    occupiedResidences[hh.getResidence().getType()].remove(hh.getResidence());
                    expulsed.add(hh);
                    hh.setJustSuppressed(true);
                    for (Individual ind : hh) {
                        if (ind.getCurrentActivity() != null) {
                            ind.leaveActivity();
                        }
                    }
                    counters.incNbHouseholdExpulsed(1);
                } else {
                    // we move the household in a free residence
                    hh.setNextSizeOfResidence(freeCat);
                    occupiedResidences[hh.getResidence().getType()].remove(hh.getResidence());
                    takeResidence(hh);
                }
            }
        }
        // suppression at the end for avoiding concurrent modification exceptions
        for (Household hh : expulsed) {
            // we should remove them from the residence for avoiding their processing
            // in the suppresion of households at the end of the iteration, for
            // controlling entirely the removing of the residence
            hh.getResidence().remove(hh);
        }
    }

    public List<Residence> getFreeResidences(int type) {
        return freeResidences[type];
    }

    public boolean hasFreeResidences(int type) {
        assert type < freeResidences.length : "Input residence index is greater than total number of residences";
        return !freeResidences[type].isEmpty();
    }

    public int getFreeResidencesCount(int type) {
        return freeResidences[type].size();
    }

    public double getTotalSurface() {
        return totalSurface;
    }

    public void setTotalSurface(double val) {
        this.totalSurface = val;
    }
    public int[] patternOccupancy;

    public void setPatternOccupancy(int[] patternOccupancy) {
        this.patternOccupancy = patternOccupancy.clone();
    }

    public void setPatternOccupancy(int i, int val) {
        this.patternOccupancy[i] = val;
    }

    public void incPatternOccupancy(Integer i, Integer delta) {
        this.patternOccupancy[i] += delta;
    }

    @Updatable
    public void updatePatternOccupancy(Integer i, Integer value, UpdateMode mode) {
        this.patternOccupancy[i] += UpdateMode.computeDelta(this.patternOccupancy[i], value, mode);
    }

    public int[] getPatternOccupancy() {
        return patternOccupancy.clone();
    }

    /**
     * This function allows to represent specific rural policies for the village. For example, by this function it could be redefined the total surface for agriculture available in
     * the village. Further others rules can be manage by this function. So, it represents the dynamics of changing the available land for a specific use.
     */
    public void changeLandUse() {
    }

    /**
     * This method manage with the eventual fire after the application of the scenario which can have been reduced the number of job offered for an activity)     
     * @author S. Huet - 16.03.2010
     */
    public void fire(boolean init) {
        SortedMap<Activity, Integer> nbToFire = computeNbOfIndivToFire();
        float probaFireOutside;
       
        int toFireOutside;
        for (Activity act : getMyRegion().getAllActivities()) {
            if (nbToFire.get(act) == 0) {
                continue;
            }         
            probaFireOutside = (float) getOccupiedActivitiesByOutside(act) / 
                    (float) (getOccupiedActivitiesByOutside(act)+
                    getOccupiedActivitiesByRes(act) + getOccupiedActivitiesByExt(act));
            
            toFireOutside = Math.round(nbToFire.get(act) * probaFireOutside);
            setOccupiedActivitiesByOutside(act, getOccupiedActivitiesByOutside(act) - toFireOutside);
            
            nbToFire.put(act, nbToFire.get(act)- toFireOutside);
            
            List<Individual> indiv = possibleIndivToFire(act);
            
            
            for (int i = 0 ; i < nbToFire.get(act);i++){
                // fire a randomly chosen individual
                if(indiv.isEmpty()){
                    break;
                }
                int nb = getRandom().nextInt(0, indiv.size() - 1);
                Individual selectedIndividual = indiv.get(nb);
                selectedIndividual.setLastFiringMun(this);
                selectedIndividual.setLastFiringYear(myRegion.getCurrentYear());               
                selectedIndividual.leaveActivity();
                selectedIndividual.becomeUnemployed(selectedIndividual.getHousehold().getMyVillage());
                if (init) { 
                    // The fire is done to solve the fact that the individual has at the beginning a 
                    // profession in a place and a sector where this profession is not available
                    selectedIndividual.setLookForJob(true);
                    selectedIndividual.setSearchedProf(selectedIndividual.getProfession());
                }
                indiv.remove(nb);                        
            }

        }   
    }

    /**
     * This method compute, for each offered activities, the number of individuals
     * it is necessary to fire after the application of the scenario at a given
     * iteration (corresponds to a change of municipal policy regarding employment)
     *
     * @author S. Huet - 16.03.2010
     * @return A map with the number of individuals to fire in one activity
     */
    public SortedMap<Activity, Integer> computeNbOfIndivToFire() {    
        SortedMap<Activity, Integer> nbToFire = new TreeMap<Activity,Integer>();
        
        for (Activity act : getMyRegion().getAllActivities()){
            if (!getMyRegion().getParameters().getFilterFireOnActivity(act)) {
                int toFire = Math.min(0,getOfferedActivities(act) 
                        - getOccupiedActivitiesByExt(act) 
                        - getOccupiedActivitiesByRes(act) 
                        - getOccupiedActivitiesByOutside(act));
                nbToFire.put(act,Math.abs(toFire)) ; // Ensure toFire value is
                
            }
        }        
        return nbToFire;
    }

    /**
     * Return the list of municipalities that have this municipality as proximityJob.
     * 
     * @return
     */
    public List<Municipality> getProximityJobOrigin() {
        List<Municipality> muns = new ArrayList<Municipality>(proximityJob.length);
        for (Municipality mun : myRegion.getMyMunicipalities()) {
            for (Municipality proxMun : mun.getProximityJob()) {
                if (proxMun == this) {
                    muns.add(mun);
                    break;
                }
            }
        }
        for (Municipality mun : myRegion.getOutsides()) {
            for (Municipality proxMun : mun.getProximityJob()) {
                if (proxMun == this) {
                    muns.add(mun);
                    break;
                }
            }
        }
        return muns;
    }

    /**
     * This method compute the list of individuals susceptible to be fired because they
     * have an activity which has been suppressed by the scenario and they do it in the present
     * municipality
     *
     * @param activ
     *            integer representing the activity position in the offered activity vector
     * @author S. Huet - 16.03.2010
     */
    private List<Individual> possibleIndivToFire(Activity activ) {
        List<Individual> indiv = new ArrayList<Individual>();

        for (Individual individual : getMyWorkers()) {
            if (individual.getCurrentActivity().equals(activ)) {
                indiv.add(individual);
            }

        }    
        return indiv;
    }

    /**
     * Method initHouseholds with a residential status The parameters to init households are: First version: S. Huet, 28.07.2009
     */
    public void initHouseholds(int[][] myHsh) {
        // Initialize household
        myHouseholds = new ArrayList<Household>(myHsh.length);
        for (int[] hh : myHsh) {
            myHouseholds.add(new Household(this, hh));
        }
        for (int index : getRandom().randomList(myHsh.length)) {
            myHouseholds.get(index).initResidence(); // attribution of a residence since the households are initialised without a residence (status = -1)
            // if the residence have not been found then add attach the household to an existing residence
            if (myHouseholds.get(index).getResidence() == null) {
                myHouseholds.get(index).attachToExistingResidence();
        }       
    }
    }
    /**
     * Split complex households with more than two adults into new household.     
     * This must be performed to comply with the assumption that a household contains at most two adults (partners). 
     * 
     * The function moves individuals out of the complex households into new households.
     * Each single extra individual is moved to a new household within the same residence
     * as the complex household.
     */
    void processComplexHouseholds(){      
        for (Household house : getCopyOfHouseholds()){
            // skip non-complex households
            if (house.getHshType() != Household.Type.COMPLEX){
                continue;
            }   
            // remove households without adults
            if (house.getAdults().isEmpty()){
                this.suppressHousehold(house,  true);
                 myHouseholds.remove(house);
                 continue;
            }
             while (house.getAdults().size()>2){                        
                // we choose the leaving individual randomly
                int index = getRandom().nextInt(0, house.getAdults().size()-1);
                Individual leavingInd = house.getAdults().get(index);
                house.suppressMember(leavingInd, Household.MemberSuppressionReason.BECOME_ADULT);                
                List<Individual> newHouseInd = new ArrayList<Individual>();
                newHouseInd.add(leavingInd);                
                Household h = new Household(this, newHouseInd, house.getResidence());               
                this.myHouseholds.add(h); // add household to the list of households                
                
                assert (leavingInd.getStatus()!=Status.STUDENT)||
                         leavingInd.isAdult()==false:
                        "Student mismatch: Cannot have adult students. Found:"+leavingInd.toString()                ;
            }   
        }                
    }

    public int getNbOfUnsatisfiedByRes() {
        int nbOfUnsatisfiedByRes = 0;
        for (Household hh : myHouseholds) {
            if (!hh.isJustSuppressed() && hh.needToChangeRes(appropriatedSizeOfRes(hh.size()))) {
                    nbOfUnsatisfiedByRes++;                
            }
        }
        return nbOfUnsatisfiedByRes;
    }

    /**
     * Method updating the households age Last version: S. Huet, 12.03.2010
     */
    public void updateHouseholdAges() {
        for (Household hh : new ArrayList<Household>(myHouseholds)) {
            // For each individual in the Household
            if (!hh.getResidence().isTransit()) { // Potential migrants don't age
                for (Individual ind : hh.getCopyOfMembers()) {                
                    ind.update();
                }
            }
        }
    }

    /**
     * Method which gave the appropriated size of flat (in term of index in the matrix of available residence for a given size of household For France: we consider the average rule
     * of ideal flat for a given household is (1 room by individual) + 1 room: for example: an household of 3 individuals needs an ideal flat of (3*1)+1 = 4 rooms (NB : the INSEE
     * counts all the rooms of the flat (i.e. the kitchen is a room if it has a surface of at least 12 m2) First version: S. Huet, 11.04.2009
     */
    public int appropriatedSizeOfRes(int sizeHsh) {
        int index = getMyRegion().getCapacityOfAvailRes().length - 1;
        // Consider the number of class of size of household divised by the
        // number of availability
        for (int i = 0; i < getMyRegion().getCapacityOfAvailRes().length; i++) {
            if (sizeHsh <= getMyRegion().getCapacityOfAvailRes(i)) {
                index = i;
                i = getMyRegion().getCapacityOfAvailRes().length;
            }
        }
        return index;
    }

    /**
     * Method init the village First version: S. Huet, 28.07.2009 Changed: S. Huet 12.10.09
     */
    public void initVillage(MunicipalityParameters parameters, int popSize,
            int[][] myHsh, String[][] myIndivActiv, String[] proxJob) {
        myLogger.log(Level.FINEST, "Init municipality {0}", name);
        setTotalSurface(parameters.getTotalSurface());
        this.populationSize = popSize;
        this.lon = parameters.getLon();
        this.lat = parameters.getLat();
        this.slope = parameters.getSlope();
        this.intercept = parameters.getIntercept();
    
        initVillageResidence(parameters.getResidenceOffer());
        initJobProximity(proxJob);
        initHouseholds(myHsh);
        initIndividualActivity(myIndivActiv);
        
        
                
        initResidenceOwners();
        processComplexHouseholds();
    }

    private void initResidenceOwners(){
         for (Household hh : myHouseholds){
             hh.updateOwnsResidence();
         }
    }
    /**
     * Creates the number of jobs needed for people working in this municipality and living within the MunicipalitySet (i.e. 
     * not in the outside).
     * 
     * This method must be used at the initialization of the Municipality after the method finishToInitMunicipality();
     * has been successfully called. 
     * 
     * This method is used to ensure that there are enough jobs in the Municipality to accommodate all the workers that
     * are accounted for in in the model at the beginning. 
     * 
     * It should be used only when the exact number of jobs for
     * each Municipality is unknown. By using this method we implement the
     * assumption that at the beginning of the simulation the number of jobs is equal to the number of people employed (as
     * created by the PopulationGeneration and ActivityGeneration algorithms.
     *
     * Omar Baqueiro Espinosa 08.05.2011 baqueiro@iamo.de 
     */
    public void createJobsForRegionWorkers(){

        for (Activity act :getMyRegion().getAllActivities()){
            // this calculates the number of offers "missing" from the total offered activities (when workers <0)
            int workers = getOfferedActivities(act) - getOccupiedActivitiesByExt(act) - getOccupiedActivitiesByRes(act);
            if (workers < 0){
                workers = Math.abs(workers);
                // if there are some offered activities we convert 
                increaseOfferedActivities(act, workers);
                assert getOfferedActivities(act)>=0: "An offered activity must be >=0. Found: "+ getOfferedActivities(act);
                
            }            
        }
    }
    /**
     * Method to finish the initialization of the individuals after each individual has one or two parents identified - 24.11.2011
     */
    public void finishToInitMunicipality() {
        for (Household hsh : getMyHouseholds()) {
            for (Individual ind : hsh) {
                //ind.initIndividual();
                if (ind.getAgeToEnterOnTheLabourMarket() == -1) {
                    ind.initAgeToEnterOnLabourMarket();
                }
                ind.initAgeToDie();
                if (ind.getStatus() == Status.STUDENT && ind.getAge() >= ind.getAgeToEnterOnTheLabourMarket()) {
                        ind.updateAgeLabourMarket();
                    }                
            }
            // The affectation of a first profession to those who has not already got a profession has to be done
            // since every individual have already an age of entering on the labour market and most of them has a profession
            for (Individual ind : hsh) {
                if ((ind.getStatus() == Status.WORKER || ind.getStatus() == Status.UNEMPLOYED) &&
                        ind.getProfession() == -1) {
                    ind.affectAFirstProfession();
                }
                if (ind.getAgeToGoOnRetirement() == -1) {
                    ind.initAgeToGoOnRetirement();
                }
                if (ind.getStatus() == Status.RETIRED) {
                    ind.setAgeToGoOnRetirement(ind.getAge() - 1);                                        
                } else {
                    if (ind.getAge() > ind.getAgeToGoOnRetirement()) {
                        ind.updateAgeRetirement();
                    }
                }
            }

        }
    }

    /**
     * Method to init the village representing the outside of the rural part of the region and the outside of the region 
     * First version: S. Huet, 07.10.2009
     */
    public void initOutside(String livingHsh[][], int[] availRes, SortedMap<Activity,Integer> jobOffers) {
        myHouseholds = new ArrayList<Household>();
        initVillageResidence(availRes);
        setOfferedActivities(jobOffers);
        endogeneousOfferedJob = new TreeMap<Activity, Integer>();                
        exogeneousOfferedJob = new TreeMap<Activity, Integer>();                

    }
 
    /**
     * Method to init the list of municipality which are close to this one regarding the job purpose
     * S. HUET 19.05.2010
     */
    private void initJobProximity(String[] jobProx) {
        //proximityJob = new Municipality[jobProx.length];
        ArrayList<Municipality> proxList = new ArrayList<Municipality>();
        for (int i = 0; i < jobProx.length; i++) {
            if (!this.getName().equals(jobProx[i])) {
                Municipality mun = getMyRegion().getMunicipality(jobProx[i]);
                if (mun != null) {
                    proxList.add(mun);
                }
            }

        }
        proximityJob = proxList.toArray(new Municipality[0]);
    }

    /**
     * Associate each Employed or Unemployed individual with an Activity
     * The format of the file is described in a doc file
     * "Instructions for the activity file.doc" S. HUET 8.10.2009
     */
    private void initIndividualActivity(final String[][]  myIndivActiv) {
        
        int curActiv=0;
        for (Household household : getCopyOfHouseholds()) {
            
            for (Individual ind : household.getCopyOfMembers()) {
                ind.initIndividualActivities(myIndivActiv[curActiv]);
                
                curActiv++;
                
                assert  (ind.getStatus()!=Status.WORKER) 
                || (ind.getCurrentJobLocation().getMyWorkers().contains(ind)): 
                        String.format("Worker %s was not added in the workers array of work location [%s] for %s",
                        ind, ind.getCurrentJobLocation(), ind.getHousehold());
                
                assert ind.getStatus()!=Status.STUDENT || ind.isAdult()==false:
                        "Student mismatch: Cannot have adult students. Found:"+household.toString();
                
                assert !ind.isAdult() || ind.getAge()>14:
                        String.format("Adults must be older than 14. Found:%s in %s",
                        ind.getAge(),household.toString() );
                
                assert ind.getStatus()!=Status.WORKER || ind.getProfession()>=0: 
                        String.format("Worker must have valid SPC. Found:%s in %s. Activities:%s",
                        ind.getProfession(), household,Arrays.deepToString(myIndivActiv[curActiv]));
                assert ind.getStatus()!=Status.UNEMPLOYED || ind.getProfession()>=0
                        :String.format("Unemployed must have valid SPC. Found:%s in %s. Activities:%s",
                        ind.getProfession(), household,Arrays.deepToString(myIndivActiv[curActiv]));
            }                                   
        }        

    }

    /**
     * Initialisation of the village residence offer
     */
    public void initVillageResidence(int[] residenceOffer) {
        residencesOffer = new int[residenceOffer.length];
        freeResidences = new List[residenceOffer.length];
        occupiedResidences = new List[residenceOffer.length];
        for (int i = 0; i < residenceOffer.length; i++) {
            int val = residenceOffer[i];
            if ((myRegion != null)) {
                freeResidences[i] = new ArrayList<Residence>();
                occupiedResidences[i] = new ArrayList<Residence>();
                for (int j = 0; j < val; j++) {
                    freeResidences[i].add(new Residence(i));
                }
            }
            this.residencesOffer[i] = val;
        }
    }

    /**
     * Initialization of the village pattern occupancy
     */
    private void initPatternOccupancy() {
        patternOccupancy = new int[getMyRegion().getAllActivities().length];
        for (int i = 0; i < getMyRegion().getAllActivities().length; i++) {
            setPatternOccupancy(i, 0);
        }
    }

    /**
     * Method to print out the state of the households composing the village
     */
    public String printHouseHoldsComposition() {
        StringBuilder buf = new StringBuilder(getMyHouseholds().size() * 100);
        buf.append(name).append("\n");
        for (Household hh : myHouseholds) {
            buf.append(hh.getResidence().getType()).append("\n");
            buf.append(hh);
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isOutside() {        
        return name.toLowerCase().startsWith("outside");
    }

    /**
     * Method to suppress an household which will be concretely suppressed at the end of the current iteration
     */
    public void suppressHousehold(Household hsehold,boolean die) {
        if (die){
            for (Individual ind : hsehold) {
                ind.leaveActivity();
            }
        }

        hsehold.getMyVillage().releaseResidence(hsehold);
        hsehold.setJustSuppressed(true);
        counters.incNbHouseholdSuppressed(1);
    }

    /**
     * Method to increase or decrease the occupation level of an activity occupied by a resident S. Huet, 06.10.2009
     * @param activ The activity whose availability will be changed
     * @param increment The value to add (or subtract if negative) to the current activity
     */
    public void increaseOccupiedActivityRes(Activity activ, int increment) {   
        if (occupiedActivitiesByRes==null){
            occupiedActivitiesByRes = new TreeMap<Activity, Integer>();
        }
        if (!occupiedActivitiesByRes.containsKey(activ)){
            occupiedActivitiesByRes.put(activ, 0);
        }
        occupiedActivitiesByRes.put(activ, occupiedActivitiesByRes.get(activ)+ increment);
    }
   
  /**
     * Method to increase or decrease the occupation level of an activity occupied by a resident S. Huet, 06.10.2009
     * @param activ The activity whose availability will be changed
     * @param increment The value to add (or subtract if negative) to the current activity
     */
    public void increaseOccupiedActivityOutside(Activity activ, int increment) {   
        if (occupiedActivitiesByOutside==null){
            occupiedActivitiesByOutside = new TreeMap<Activity, Integer>();
        }
        if (!occupiedActivitiesByOutside.containsKey(activ)){
            occupiedActivitiesByOutside.put(activ,0);
        }
        
        occupiedActivitiesByOutside.put(activ, occupiedActivitiesByOutside.get(activ)+ increment);
    }
   
    /**
     * Method to increase/decrease the occupation level of an activity occupied by a external individual S. Huet, 0.10.2009
     * @param activ The activity whose availability will be changed
     * @param increment The value to add (or subtract if negative) to the current activity
     */
    public void increaseOccupiedActivityExt(Activity activ, int increment) {        
        if (occupiedActivitiesByExt == null){
            occupiedActivitiesByExt = new TreeMap<Activity, Integer>();
        }
        if (!occupiedActivitiesByExt.containsKey(activ)){
            occupiedActivitiesByExt.put(activ, 0);
        }
        occupiedActivitiesByExt.put(activ, occupiedActivitiesByExt.get(activ)+ increment);
    }

    /**
     * Method to increase/decrease the number of total offered activities
     * @param activ The activity whose availability will be changed
     * @param increment The value to add (or subtract if negative) to the current activity
     */
    private void increaseOfferedActivities(Activity activ, int value){
        setOfferedActivities(activ, getOfferedActivities(activ)+value);
    }
    

    public float getSumFreeActivities() {
        float freeact=0;
        for (Activity act :getMyRegion().getAllActivities()){
            freeact+= getOfferedActivities(act);
            Integer numAct = getOccupiedActivitiesByExt(act);
            freeact-= (numAct==null)?0:numAct;
            numAct = getOccupiedActivitiesByRes(act);
            freeact-= (numAct==null)?0:numAct;
            numAct = getOccupiedActivitiesByOutside(act);            
            freeact-= (numAct==null)?0:numAct;
        }        
        return freeact;
    }
    /**
     * Method to indicate at a given moment the number of free activities in the village S. Huet, 07.10.2009
     * @param activ The index of the activity
     */
    public float getFreeActivities(Activity activ) {
        float free;
        if (getOccupiedActivitiesByOutside(activ) >= 0) {
            free = getOfferedActivities(activ) - (getOccupiedActivitiesByExt(activ) + getOccupiedActivitiesByRes(activ) + getOccupiedActivitiesByOutside(activ));
        } else {
            free = getOfferedActivities(activ)- (getOccupiedActivitiesByExt(activ) + getOccupiedActivitiesByRes(activ));
        }
        return free;
    }

    /**
     * Method to indicate at a given moment the number of free activities in the village
     */
    public float[] getFreeActivities() {
        float[] free = new float[myRegion.getNbActivities()];
        int i=0;
        for (Integer numAct: totalOfferedJob.values()){
            free[i++] = numAct;
        }
        return free;
    }

    public float[] getOccupiedActivities() {
        float[] occupied = new float[myRegion.getNbActivities()];
        int i=0;
        for (Activity act:totalOfferedJob.keySet()){
            occupied[i] = getOccupiedActivitiesByExt(act) + getOccupiedActivitiesByOutside(act) + getOccupiedActivitiesByRes(act);
        }
        return occupied;
    }

    public void takeResidence(Household myHouseH) {
        Residence residence = this.freeResidences[myHouseH.getNextSizeOfResidence()].remove(0);
        myHouseH.setResidence(residence);
        occupiedResidences[myHouseH.getNextSizeOfResidence()].add(residence);
    }

    public void releaseResidence(Household myHouseH) {
        if (myHouseH.getResidence().isTransit()) {
            nbImmigrants++;
        }
        releaseResidence(myHouseH, null);
    }

    /**
     * Release properly the residence in the municipality.
     * @param myHouseH hh that quits his residence.
     * @param it iterator for safe removing. If null, the hh is removed directly from the list.
     */
    public void releaseResidence(Household myHouseH, Iterator<Household> it) {
        boolean alreadySup = true;
        for (Household hh : myHouseH.getResidence()) {
            if (hh.getId() == myHouseH.getId()) {
                alreadySup = false;
                break;
            }
        }
        if (!alreadySup) {
            if (it == null) {
                myHouseH.getResidence().remove(myHouseH);
            } else {
                it.remove();
            }
            if (myHouseH.getResidence().getNbHhResidents() == 0 && myHouseH.getResidence().getType() >= 0) { 
                    myHouseH.getMyVillage().freeResidences[myHouseH.getResidence().getType()].add(myHouseH.getResidence());
                    myHouseH.getMyVillage().occupiedResidences[myHouseH.getResidence().getType()].remove(myHouseH.getResidence());
                
            }
        }
    }

     /**
      * Executes the change of residence for one household when they are changing
      * to a municipality other than the current one.
      * @param myHouseH The household changing municipality
      * @param fireOtherWorkers  Whether other Household workers will be fired
      */
    private void changeMunicipality(Household myHouseH) {
    assert myHouseH.getAdults().size()<=2 : 
                    String.format("A household must have 2 or less adults. Found:%s at %s",
                    myHouseH.getAdults().size(), myHouseH.toString());
        
        Municipality nextResMun =   myHouseH.getNextResMunicipality();
        assert nextResMun !=null:"Next residence municipalty must NOT be nul at this point. Found null";
        
        myHouseH.getMyVillage().getCounters().incNbHouseholdMoveOut(1);
        myHouseH.getMyVillage().getCounters().incNbIndividualMoveOut(myHouseH.size());
        
        myHouseH.setJustSuppressed(true);
        // if Municipality is moving to the outside log the move
        if (getMyRegion().getOutsides().contains(myHouseH.getNextResMunicipality())) {
           getCounters().incNbIndividualMoveOutside(myHouseH.getSize());                  
        }        

        // for unemployed member of household, the "unemployed" pattern is now counted in the new municipality, 
        //firstly we suppress him from the counter in the departure
        // municipality
        nextResMun.getCounters().incNbHouseholdMoveIn(1);
        nextResMun.getCounters().incNbIndividualMoveIn(myHouseH.size());    

        for (Individual individual : myHouseH) {
            switch (individual.getStatus()) {
                case UNEMPLOYED:
                    // change status of working municipality (by definition unemployed people
                    // are said to work in the same municipality they live
                    individual.setCurrentJobLocation(myHouseH.getNextResMunicipality());
                    break;
                case WORKER:
                    // if the worker is currently worker in the village she is leaving
                    if (individual.getCurrentJobLocation().equals(myHouseH.getMyVillage())){
                       myHouseH.getMyVillage().increaseOccupiedActivityExt(individual.getCurrentActivity(), 1); 
                       myHouseH.getMyVillage().increaseOccupiedActivityRes(individual.getCurrentActivity(), -1); 
                       break;
                    }
                    // if new individual residence is in the job location
                    if (individual.getCurrentJobLocation().equals(nextResMun)){
                        nextResMun.increaseOccupiedActivityRes(individual.getCurrentActivity(), 1);
                        nextResMun.increaseOccupiedActivityExt(individual.getCurrentActivity(), -1);
                    }
                    break;
                    
                    
            }
            
        }
        
        Household newHsh = new Household(myHouseH, false);
        newHsh.setMyVillage(myHouseH.getNextResMunicipality());
        myHouseH.getNextResMunicipality().getMyHouseholds().add(newHsh);
        newHsh.getNextResMunicipality().takeResidence(newHsh);
    
   
    }
   
    public void move(Household myHouseH) {
        myHouseH.resetLeader();
        myHouseH.setNeedsResidence(false);
        for (Individual ind : myHouseH) {
            ind.setLastMovingYear( myRegion.getCurrentYear());
        }
        // update the residence counters of each village, the quitted and the new one
        
        releaseResidence(myHouseH);
        if (myHouseH.getNextResMunicipality() == myHouseH.getMyVillage()) { // the household only change of residence without changing municipality
            myHouseH.getMyVillage().takeResidence(myHouseH);
            myHouseH.setNextSizeOfResidence(-1);
            myHouseH.setNextResMunicipality(null);
        } else {
            
            // the household change of residence and municipality
            changeMunicipality(myHouseH);
           
        }
        
    }

    /**
     * Return the number of actives
     */
    public int getTotalActives() {
        int numActives = 0;
        for (Household hh : myHouseholds) {
            if (!hh.isJustSuppressed()) {
                for (Individual individual : hh) {
                    if (individual.getStatus() == Status.UNEMPLOYED || individual.getStatus() == Status.WORKER) {
                        numActives++;
                    }
                }
            }
        }
        return numActives;
    }
    

    /**
     * Returns the total number of individuals that have the Economical Status
     * defined by the input parameter
     * @param stat Only individuals with this status counted 
     * @return The sum of all individuals living in the municipality with the s
     * specified status.
     */
    public int getTotalWithStatus(Status stat){
        int sum = 0;
           for (Household hh : myHouseholds) {
            if (!hh.isJustSuppressed() ) {
                for (Individual individual : hh) {
                    if (individual.getStatus() == stat) {
                        sum++;
                    }
                }
            }
        }
        return sum;    
    }
  
    /**
     * Get the number of individuals grouped by the ages defined
     * in the input parameter. Each value of the input parameter is used as
     * the lower bound of the age group. The upper bound of each group is 
     * defined as 1- the next integer in the list.
     * 
     * Only the individuals whose status is equal to the desiredStatus parameter
     * will be counted
     * 
     * @param ageRange Array of lower bounds to use for grouping the age bins
     *        this array will be sorted from low to high before creating the 
     *        bounds
     * @param desiredStatus The status that each individual must have to be 
     *        counted in the groups
     * @return An array containing the number of individuals falling within each
     *         category of the defined groups. Note that the values correspond
     *         to the groups formed after sorting the input array.
     *         
     */
    public int[] getGroupsInAgeRange(int[] ageRange, Status desiredStatus) {
        int[] groups = new int[ageRange.length];
        Arrays.sort(ageRange);

        for (Household hh : myHouseholds) {
            if (!hh.isJustSuppressed()) {
                for (Individual individual : hh) {
                    if (individual.getStatus() == desiredStatus) {
                        // put individual in correct bin
                        int bin=-1;
                        for (int i=0;i<ageRange.length;i++){
                            if (ageRange[i]>individual.getAge()){
                                break;
                            }
                            bin = i;
                        }
                        if (bin==-1) continue;
                        
                        groups[bin]++;                                                    
                    }
                }
            }
        }

        return groups;


    }


    /**
     * Method printing the vector available residence First version: S. Huet, 28.07.2009
     */
    public String editAvailableRes() {
        StringBuilder buff = new StringBuilder("Residences Offer");
        buff.append(Arrays.toString(residencesOffer));
        buff.append(" - Free residences ");
        for (int i = 0; i < getResidencesTypeCount(); i++) {
            buff.append(" ").append(getFreeResidencesCount(i));
        }
        buff.append(" - Nb of households of the village without lodging: ");
        buff.append(counters.getNbOfDwellingLacking());
        buff.append(" nb of unsatisfied household by the residence: ");
        buff.append(getNbOfUnsatisfiedByRes());
        return buff.toString();
    }

    /**
     * Method to edit the demography S. Huet, 11.08.2009
     */
    public String editDemography(int time) {
        int numActives = getTotalActives();
        StringBuilder buff = new StringBuilder("Demography");
        buff.append(" size=").append(populationSize);
        buff.append(" actives=").append(numActives);
        buff.append(" death=").append(counters.getNumDeath());
        buff.append(" birth=").append(counters.getNumBirth()); 
        buff.append(" mov out=").append(counters.getNbIndividualMoveOut());
        buff.append(" mov in=").append(counters.getNbIndividualMoveIn());
        if (time != 0) {
            buff.append(" mov inside=").append(counters.getNbIndividualMoveInside());
        }
        buff.append(" HHout=").append(counters.getNbHouseholdMoveOut());
        buff.append(" HHin=").append(counters.getNbHouseholdMoveIn());
        if (time != 0) {
            buff.append(" HHinside=").append(counters.getNbHouseholdMoveInside());
            buff.append(" HHnew=").append(counters.getNewHouseholdNb());
        }
        buff.append(" HHsuppressed=").append(counters.getNbHouseholdSuppressed());
        buff.append(" HHexpulsed=").append(counters.getNbHouseholdExpulsed());
        buff.append(" HHtotal=").append(myHouseholds.size());
        return buff.toString();
    }

    /**
     * Method to edit pattern occupancy S. Huet, 11.08.2009
     */
    public String editPatternOccupancy() {
        int total = 0;
        StringBuilder buff = new StringBuilder("Pattern occupancy: ");
        for (int i = 0; i < patternOccupancy.length; i++) {
            buff.append(patternOccupancy[i]).append(" ");
            total = total + patternOccupancy[i];
        }
        buff.append("total: ").append(total);
        return buff.toString();
    }

    public int getTotalPatternOccupancy() {
        int total = 0;
        for (int i = 0; i < patternOccupancy.length; i++) {
            total = total + patternOccupancy[i];
        }
        return total;
    }
    /**
     * Method to edit various statistic on households: households details about residence S. Huet, 12.08.2009
     */
    public void editHouseholdDetailResidence() {
        int i = 0;
        for (i = 0; i < getMyHouseholds().size(); i++) {
            System.err.println(" size flat " + getMyRegion().getCapacityOfAvailRes(getMyHouseholds().get(i).getResidence().getType()) + " size household "
                    + getMyHouseholds().get(i).size());
        }
    }

    /**
     * Method to edit various statistic on households: distribution of households on household types Household types: correspond to the code used by StartPopulation when it
     * generates a population 0: single 1: monoparenthal family 2: couple without children 3: couple with children 4: complex household (other households) S. Huet, 13.08.2009
     */
    public String editHouseholdDetailTypes() {
        int i = 0;        
        SortedMap<Household.Type, Integer> hhTypeCount= new TreeMap<Household.Type, Integer> ();      
        for (i = 0; i < getMyHouseholds().size(); i++) {
            Household.Type curType = getMyHouseholds().get(i).getHshType();                
                if (hhTypeCount.containsKey(curType)) {
                    hhTypeCount.put(curType, hhTypeCount.get(curType)+1);
                }
                else {
                    hhTypeCount.put(curType,1);
                }                
            }        
        StringBuilder buff = new StringBuilder("Distribution of hsh on hsh types\t");
        for (Integer typeCount:hhTypeCount.values()) {
            buff.append(typeCount).append("\t");
        }
        return buff.toString();
    }

    /**
     * Method to edit various statistic on households: households size distribution S. Huet, 12.08.2009
     */
    public String editHouseholdDetailSizes() {
        int i = 0;
        int j = 0;
        int maxBound = 30;
        int sizeMax = 0;
        int[] sizeTemp = new int[maxBound];
        Arrays.fill(sizeTemp, 0);
        for (i = 0; i < getMyHouseholds().size(); i++) {
            for (j = 0; j < maxBound; j++) {
                if (getMyHouseholds().get(i).size() == j + 1) {
                    sizeTemp[j]++;
                }
                if (getMyHouseholds().get(i).size() > sizeMax) {
                    sizeMax = getMyHouseholds().get(i).size();
                }
            }
        }
        int[] size = new int[sizeMax];
        StringBuilder buff = new StringBuilder("Distribution of household sizes:\t");
        for (i = 0; i < sizeMax; i++) {
            size[i] = sizeTemp[i];
            buff.append(size[i]).append("\t");
        }
        return buff.toString();
    }

    /**
     * Method to edit various statistic on individuals: individual age distribution by step of 5 years from 0 to 90 and more S. Huet, 12.08.2009
     */
    public String editIndividualDetailAges(String shift) {
        int j = 0;
        int step = 5; // step of 5 years
        int max = 90; // age max considered for the last - 1 range; the last one
        // is higher than max
        int nbRange = (int) max / step + 1; // better if you are sure that the
        // result is already an int
        int[] ageDist = new int[nbRange];
        int[] indivStatus = new int[5];
        Arrays.fill(indivStatus, 0);
        int theOldest = 0;
        int bound = 0;
        for (Household hh : myHouseholds) {
            for (Individual ind : hh) {
                indivStatus[ind.getStatus().ordinal()]++;
                bound = 0;
                for (j = 0; j < nbRange; j++) {
                    bound = bound + step;
                    if (ind.getAge() < bound) {
                        ageDist[j]++;
                        j = nbRange;
                    } else {
                        if (j == nbRange - 1) {
                            ageDist[j]++;
                        }
                    }
                    if (ind.getAge() > theOldest) {
                        theOldest = ind.getAge();
                    }
                }
            }
        }
        StringBuilder buff = new StringBuilder(shift);
        buff.append("Distribution of individual ages by step: ");
        buff.append(step).append(" years\t");
        for (int i = 0; i < nbRange; i++) {
            buff.append(ageDist[i]).append("\t");
        }
        buff.append("The oldest is ").append(theOldest).append("\n");
        int totStat = 0;
        buff.append(shift).append("Distribution of individual status (in order student, unemployed, inactive, worker, retired:\t");
        for (int i = 0; i < indivStatus.length; i++) {
            buff.append(indivStatus[i]).append("\t");
            totStat = totStat + indivStatus[i];
        }
        buff.append("total ").append(totStat);
        return buff.toString();
    }

 

    /**
     * Method to create by duplication the potentially migrant households starting from a list of randomly chosen households in the population, 3.11.2010
     * All the migrants are initialised with an unemployed status if they are workers in the duplicate household
     */
    public void addTheMigrants(Household[] toDuplicate) {
        for (Household duplic : toDuplicate) {
            Household newHsh = new Household(duplic, true);
            newHsh.setMyVillage(this);
            newHsh.createMigrants(duplic);
            newHsh.setNeedsResidence(true);
            getMyHouseholds().add(newHsh);
        }
    }

    /**
     * Gets the number individuals that work in this municipality grouped by
     * Sector of Activity. 
     * Note that this method considers also individuals who reside in other
     * municipalities and work in the current municipality.
     *
     * To guarantee that the count of individuals is accurate the method
     * MuniciaplitySet.updateMunicipalityWorkers() must be run before this
     * method.
     * @return  array containing the number of individuals working in the this 
     * municipality on the different Sectors of Activity.
     */
    public int[] getSoAofWorkers(){
        int[] soa = new int[getMyRegion().getParameters().getNbSoA()];
        Arrays.fill(soa, 0);
        // if there workers list has not been initialized return zeroed array
        if (workers==null)
        {
            return soa;
        }
        for (Individual ind : workers){            
            soa[ind.getSectorOfActivity()]++;                        
        }        
        return soa;
    }

    /**
     * Gets the number of employed individuals that live in this municipality
     * grouped by Sector of Activity.
     * @return array containing the number of employed individuals living in this
     * municipality for each SoA.
     */
    public int[] getSoAofResidents()
    {
        int[] soa = new int[getMyRegion().getParameters().getNbSoA()];
        Arrays.fill(soa, 0);
        for (Household h:getMyHouseholds()){
            for (Individual ind:h.listOfMembers){
                if (ind.getStatus() == Status.WORKER){
                    soa[ind.getSectorOfActivity()]++;
                }
            }
        }        
        return soa;
    }
    /**
     *  Updates the list of workers working in this municipality
     * this is must be a list of individuals whose work location
     * is the current municipality
     * 
     */
    public void setWorkers(List<Individual> w) {
        workers = w;
    }
    
    
      public void setEndogeneousOfferedJob(Activity act, Integer newValue){
        assert endogeneousOfferedJob != null : "Endogenous offered jobs must NOT be null at this point";
        endogeneousOfferedJob.put(act, newValue);
    }
    public void setExogeneousOfferedJob(Activity act, Integer newValue){
        assert exogeneousOfferedJob != null : "Exogenous offered jobs must NOT be null at this point";
        exogeneousOfferedJob.put(act, newValue);
    }
    
    /**
     * Gets the number of jobs offered endogenously by the model or the given activity. If the activity
     * was not in the table endogenous jobs table then it is added and the value set to 0
     * @param act The activity 
     * @return Number of jobs provided endogenously by the model for the given activity
     */
    public int getEndogenousOfferedJob(Activity act){
        assert endogeneousOfferedJob != null : "Endogenous offered jobs must NOT be null at this point";
        
        
        if (!endogeneousOfferedJob.containsKey(act)){
            endogeneousOfferedJob.put(act, 0);
        }
        return endogeneousOfferedJob.get(act);
    }
    
    public SortedMap<Activity, Integer> getEndogenousOfferedJob(){
        assert endogeneousOfferedJob != null : "Endogenous offered jobs must NOT be null at this point";        
        return endogeneousOfferedJob;
    }
    /**
     * Gets the number of jobs offered exogenously by the model or the given activity. If the activity
     * was not in the table endogenous jobs table then it is added and the value set to 0
     * @param act The activity for which the availability is looked for.
     * @return Number of jobs provided exogenously by the model for the given activity
     */
    public Integer getExogenousOfferedJob(Activity act){
        assert exogeneousOfferedJob != null : "Exogenous offered jobs must NOT be null at this point";
        if (!exogeneousOfferedJob.containsKey(act)){
            exogeneousOfferedJob.put(act, 0);
        }
        return exogeneousOfferedJob.get(act);
    }
    
    /**
     * Gets the total number of individuals that have this municipality
     * as a work location. 
     * @return Number of individuals working in this work location.
     */
    public int getTotalWorkers(){
        if (workers==null){
            return 0;
        }
        return workers.size();
    }

    /**
     * Sets the list of occupied activities by outside to 0
     */
    void clearOccupiedActivititesByOutside() {
       occupiedActivitiesByOutside.clear();
    }
    
    public Set<Individual> getMyWorkers(){
        if (myWorkers==null){
            myWorkers = new HashSet<Individual>();
        }
        return this.myWorkers;
    }
}

