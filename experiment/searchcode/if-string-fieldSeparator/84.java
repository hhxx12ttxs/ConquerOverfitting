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
package fr.cemagref.prima.regionalmodel.parameters;

import fr.cemagref.observation.kernel.ObservableManager;
import fr.cemagref.observation.kernel.ObservablesHandler;
import fr.cemagref.observation.kernel.ObserverListener;
import fr.cemagref.ohoui.annotations.Description;
import fr.cemagref.prima.regionalmodel.Activity;
import fr.cemagref.prima.regionalmodel.Individual;
import fr.cemagref.prima.regionalmodel.MunicipalitySet;
import fr.cemagref.prima.regionalmodel.dynamics.DynamicServiceEmployment;
import fr.cemagref.prima.regionalmodel.dynamics.EmigrationUpdater;
import fr.cemagref.prima.regionalmodel.gui.ChartsPanel;
import fr.cemagref.prima.regionalmodel.tools.BadDataException;
import fr.cemagref.prima.regionalmodel.tools.ProcessingException;
import fr.cemagref.prima.regionalmodel.tools.Random;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicolas Dumoulin <nicolas.dumoulin@cemagref.fr>
 */
public class Parameters {

    private static transient final Logger LOGGER = Logger.getLogger(Parameters.class.getSimpleName());
    @Description(name = "Index of the seed", tooltip = "Index of the seed to use for initializing the pseudo-random numbers generator")
    private Integer seedIndex;
    @Description(name = "Simulation start", tooltip = "Indicates at which step the simulation will start")
    private int startStep;
    @Description(name = "Simulation duration", tooltip = "Number of steps that will be elapsed during the simulation")
    private int nbStep;
    @Description(name = "Step duration", tooltip = "")
    private int step;
    private int expIndex;
    private transient List<MunicipalityParameters> villagesParameters;
    private transient List<OutsideParameters> outsideParameters;
    private int nbSectorsActiv;
    private int nbTypesBySectorActiv;
    private transient int nbActivities;
    private int nbMaxOfActivityByPattern;
    private int nbSizeRes;
    private Value<Integer, Individual> ageToDie;
    private int ageMinHavingChild;
    private int ageMaxHavingChild;
    private transient double probaBirth;
    private double nbChild;
    private double avDifAgeCouple;
    private double stDifAgeCouple;
    private double probabilityToMakeCouple;    
    private Value<Integer, Individual> firstProfession;
    private int nbOfUnactiveSituations;
    private int nbOfUnemployedSituations;
    private Value<Integer, Individual> ageEnterLabourMarket;
    private Value<Integer, Individual> ageToRetire;
    private Value<Boolean, Individual> remainInactive;
    private Value<Boolean, Individual> isInactiveAfterUnemployment;
    private Value<Boolean, Individual> isInactiveAfterEmployment;
    private Value<Integer, Individual> profIfUnactive;
    private Value<Integer, Individual> profIfWorking;
    private Value<Boolean, Integer> filterFireOnActivity;
    private String municipalitiesDistances;
    private String jobResearchMunNetworkClasses;
    private String jobResearchMunNetworkProbas;
    private double splittingProba;
    private int nbJoinTrials;
    private int resSatisfactMargin;
    private double probToAcceptNewResidence;
    private String proxJobFile;
    private String populationFile;
    private String activityFile;
    private String scenarioFile;    
    @Description(name="Output prefix", tooltip="Filenames prefix for the output files that will be written. No data written if null.")
    private String outputDir;   
    private EmigrationUpdater emigration;
    private DynamicServiceEmployment dynamicServiceEmployment;
    private transient List<TemporalValue> temporalValuesCache;
    private List<ObserverListener> regionObservers;

    private Value<Integer, Individual> sectorIfWorking;
    /** Rule to define whether an individual is a residence owner */
    private Value<Boolean, Individual> isResidenceOwner;
    /** Maximum commuting time allowed before an individual look for another residence*/
    private float commutingTimeThreshold;
    /** Probability that a student of 18 years or older studies outside the study region */
    private float probabilityStudyOutside;
    
    private boolean writeSimulationOutput;
    private boolean createHTMLReport;
    
    
    /** Factors used to calibrate the SPC distributions for First selected SPC*/
    private double BetaFirstSPC1;
    private double BetaFirstSPC2;
    private double BetaFirstSPC3;
    private double BetaFirstSPC4;
    private double BetaFirstSPC5;
    private double BetaFirstSPC6;
    private double BetaFirstSPC7;
    private double BetaFirstSPC8;


    /** Factors used to calibrate the SPC distributions for selected SPC for Employed Individuals*/
    private double BetaNewSPC1Employ;
    private double BetaNewSPC2Employ;
    private double BetaNewSPC3Employ;
    private double BetaNewSPC4Employ;
    private double BetaNewSPC5Employ;
    private double BetaNewSPC6Employ;
    private double BetaNewSPC7Employ;
    private double BetaNewSPC8Employ;

    private double BetaNewSPC1Unemp;
    private double BetaNewSPC2Unemp;
    private double BetaNewSPC3Unemp;
    private double BetaNewSPC4Unemp;
    private double BetaNewSPC5Unemp;
    private double BetaNewSPC6Unemp;
    private double BetaNewSPC7Unemp;
    private double BetaNewSPC8Unemp;

    private String populationDistributionFileName;
    private String workingPlaceTableFileName;
    private String populationBirthsDeathsFileName;
    private String populationHouseholdStructureFileName;
    private String householdSizesFileName;
    private String economicStatusFileName;
    private String employmentTableFileName;
    private String soATableFileName;
    
    /** Rate of new jobs per year used to calculate amount of new jobs per year*/
    private double jobVacancyRate;
    
    private String fieldSeparator;
    private int runPrefix;
    /**
     * Probability that an individual will look for jobs in all municipalities
     * in the region after looking in his/her own municipality.
     */
    private double probLookingRegionalJobs;
    
    
    public Parameters() {
    }

    public void initRNG(Random random) throws ProcessingException {
        if (seedIndex == null) {
            LOGGER.log(Level.WARNING, "Random numbers generator will be initialized with the default seed : {0}", random.getSeedToString());
        } else {
            Scanner scanner = new Scanner(this.getClass().getResourceAsStream("status-period10000000000.txt"));
            scanner.useDelimiter(";");
            for (int i = seedIndex; i > 0; i--) {
                scanner.nextLine();
            }
            random.setSeed(new long[]{scanner.nextLong(), scanner.nextLong(), scanner.nextLong(),
                        scanner.nextLong(), scanner.nextLong(), scanner.nextLong()
                    });
            scanner.close();
            LOGGER.log(Level.INFO, "Random numbers generator has been initialized with the following seed : {0}", random.getSeedToString());
        }
    }

    public void loadVillages(File[] dirs, int outNb) throws BadDataException {
        villagesParameters = new ArrayList<MunicipalityParameters>(dirs.length - outNb);
        outsideParameters = new ArrayList<OutsideParameters>(outNb);
        Logger.getLogger(Parameters.class.getSimpleName()).log(Level.INFO, "{0} municipalities will be loaded", dirs.length - outNb);
        for (File municipalityDir : dirs) {
            Logger.getLogger(Parameters.class.getSimpleName()).log(Level.FINER, "Processing directory {0}", municipalityDir);
            try {
                if (municipalityDir.getName().toLowerCase().startsWith("outside")) {
                    outsideParameters.add(new OutsideParameters(this, municipalityDir));
                } else {
                    villagesParameters.add(new MunicipalityParameters(this, municipalityDir));
                }
            } catch (IOException ex) {
                Logger.getLogger(Parameters.class.getSimpleName()).log(Level.WARNING,
                        "This directory doesn't seem to contain municipalities file and has been skipped :" + municipalityDir + " (" + municipalityDir.getName() + ")",
                        ex);
            } catch (RuntimeException ex) {
                Logger.getLogger(Parameters.class.getSimpleName()).log(Level.SEVERE, "Error while trying to load municipality data in {0}", municipalityDir);
                throw ex;
            }
        }
    }

    public void loadObservers(boolean isGui) {
        ObservableManager.clear();
        if (regionObservers != null) {
            ObservablesHandler regionObservable = ObservableManager.addObservable(MunicipalitySet.class);
            for (ObserverListener observer : regionObservers) {
                if (!(observer instanceof ChartsPanel) || isGui) {
                    regionObservable.addObserverListener(observer);
                }
            }
        }
    }

    public Object readResolve() throws Exception {
        nbActivities = nbSectorsActiv * nbTypesBySectorActiv;
        this.probaBirth = (double) (nbChild) / (double) ((float) (ageMaxHavingChild - ageMinHavingChild + 1));        
                
        // fetch all instances of temporal value for putting in cache
        temporalValuesCache = new ArrayList<TemporalValue>();
        for (Field field : this.getClass().getDeclaredFields()) {
            Object o = field.get(this);
            if (o != null && TemporalValue.class.isAssignableFrom(o.getClass())) {
                temporalValuesCache.add((TemporalValue) o);
            }
        }
        return this;
    }

    /**
     * Update the temporal values parameters caches, see {@link TemporalValue#updateCurrentValue(int)}
     * @param time
     */
    public void updateTemporalValues(int time) {
        for (TemporalValue tv : temporalValuesCache) {
            tv.updateCurrentValue(time);
        }
    }

    public void setNbTypesBySectorActiv(int nbTypesBySectorActiv) {
        this.nbTypesBySectorActiv = nbTypesBySectorActiv;
    }

    public Integer getSeedIndex() {
        return seedIndex;
    }
    
    public int getExpIndex(){
        return this.expIndex;
    }

    public Value<Integer, Individual> getFirstProfession() {
        return firstProfession;
    }

    public Value<Boolean, Individual> getIsInactiveAfterEmployment() {
        return isInactiveAfterEmployment;
    }

    public Value<Boolean, Individual> getIsInactiveAfterUnemployment() {
        return isInactiveAfterUnemployment;
    }

    public int getNbMaxOfActivityByPattern() {
        return nbMaxOfActivityByPattern;
    }

    public Value<Integer, Individual> getProfIfUnactive() {
        return profIfUnactive;
    }

    public Value<Integer, Individual> getProfIfWorking() {
        return profIfWorking;
    }

    public Value<Integer, Individual> getSectorIfWorking(){
        return sectorIfWorking;
    }
    public Value<Boolean, Individual> getRemainInactive() {
        return remainInactive;
    }

    public int getResSatisfactMargin() {
        return resSatisfactMargin;
    }

    public List<MunicipalityParameters> getVillagesParameters() {
        return villagesParameters;
    }

    /**
     * Age max to have child
     */
    public int getAgeMaxHavingChild() {
        return ageMaxHavingChild;
    }

    /**
     * Age min to have child
     */
    public int getAgeMinHavingChild() {
        return ageMinHavingChild;
    }

    /**
     * Average difference of age in a couple
     */
    public double getAvDifAgeCouple() {
        return avDifAgeCouple;
    }

    /**
     * probability for a single to make a new a couple
     */
    public double getProbabilityToMakeCouple() {
        return probabilityToMakeCouple;
    }

    public double getProbaBirth() {
        return probaBirth;
    }

    public Value<Integer, Individual> getAgeEnterLabourMarket() {
        return ageEnterLabourMarket;
    }

    public int getAgeEnterLabourMarket(Individual ind) {
        return ageEnterLabourMarket.getValue(ind);
    }

    public Value<Integer, Individual> getAgeToRetire() {
        return ageToRetire;
    }

    public int getAgeToRetire(Individual ind) {
        return ageToRetire.getValue(ind);
    }

    public boolean isInactiveAfterEmployment(Individual ind) {
        return isInactiveAfterEmployment.getValue(ind);
    }

    public boolean isInactiveAfterUnemployment(Individual ind) {
        return isInactiveAfterUnemployment.getValue(ind);
    }

    public boolean remainInactive(Individual ind) {
        return remainInactive.getValue(ind);
    }

    public int getProfIfUnactive(Individual ind) {
        return profIfUnactive.getValue(ind);
    }

    public int getProfIfWorking(Individual ind) {
        return profIfWorking.getValue(ind);
    }

    public boolean getFilterFireOnActivity(int activity) {
        if (filterFireOnActivity == null) {
            return false;
        }
        return filterFireOnActivity.getValue(activity);
    }

    public boolean getFilterFireOnActivity(Activity act) {
        assert act!=null : "The activity must NOT be null";        
         if (filterFireOnActivity == null) {
            return false;
        }
        return filterFireOnActivity.getValue(createIndexFromActivity(act));        
    }
    public String getMunicipalitiesDistances() {
        return municipalitiesDistances;
    }

    public String getJobResearchMunNetworkClasses() {
        return jobResearchMunNetworkClasses;
    }

    public String getJobResearchMunNetworkProbas() {
        return jobResearchMunNetworkProbas;
    }

    public List<MunicipalityParameters> getMunicipalitiesParameters() {
        return villagesParameters;
    }

    public List<OutsideParameters> getOutsideParameters() {
        return outsideParameters;
    }

    public int getAgeToDie(Individual ind) {
        return ageToDie.getValue(ind);
    }

    public Value<Integer, Individual> getAgeToDie() {
        return ageToDie;
    }

    /**
     * Average number of children by individuals
     */
    public double getNbChild() {
        return nbChild;
    }

    public Integer getFirstProfession(Individual ind) {
        return firstProfession.getValue(ind);
    }

    /**
     * Give the number of line in the matrix of transition for the labour
     * situation (probaActivityPattern) corresponding to an unactive status
     */
    public int getNbOfUnactiveSituations() {
        return nbOfUnactiveSituations;
    }

    /**
     * Give the number of line in the matrix of transition for the labour
     * situation (probaActivityPattern) corresponding to an unemployed status
     */
    public int getNbOfUnemployedSituations() {
        return nbOfUnemployedSituations;
    }
   
    public int getNbSoA() {
        return nbSectorsActiv;
    }

    /**
     * Creates an activity with specific SPC and SoA depending on the provided
     * index. This function is used to consistently map the data from input
     * files containing SPC and SoA data. 
     * 
     * As input files provide such data in one row, then it is necessary to map
     * each entry in the row to a specific Activity (SoA and SPC). 
     * 
     * The current logic assumes that the order of the activities in the row is:
     * SPC1/SOA1 SPC2/SOA1 SPC3/SOA1 SPC4/SOA1 ... SPC1/SOA2 SPC2/SOA2 ... SPCn/SOAm
     * Where n is the total number of SPC and m is the total number of SoA.
     * 
     * So an index i would correspond to an activity with:
     *   SPC = i mod n 
     *  SOA = int(i / n)
     * @return A new activity corresponding to the provided index.
     */
    public Activity createActivityFromIndex(int index){
        assert index >= 0 : "Index must be greater or equal to 0 ";
        assert index <= getNbActivities() : "Index must be less to the total number of activities";        
        return new Activity ((int)index/getNbSPC(), index%getNbSPC());
    }
    
    /**
     * Returns the index equivalent to the defined activity based on the number of 
     * SPC and SoA in the parameters
     */
    public int createIndexFromActivity (Activity act)
    {
        assert act != null : "Activity must not be null";        
        return act.getSoA()*getNbSPC() + act.getSPC();
    }
    /**
     * Number of sizes considered for residences. The cardinal which corresponds
     * to nbSizesResidence is a "nbSizesResidence" and more size
     */
    public int getNbSizeRes() {
        return nbSizeRes;
    }

    /**
     * Step of the simulation start
     *
     * @return
     */
    public int getStartStep() {
        return startStep;
    }

    public void setStartStep(int startStep) {
        this.startStep = startStep;
    }

    /**
     * Nb of iteration to do
     */
    public int getNbStep() {
        return nbStep;
    }

    public int getNbSPC() {
        return nbTypesBySectorActiv;
    }

    public int getNbActivities() {
        return nbActivities;
    }

    /**
     * Probability to divorce, to quit each other inside a couple
     */
    public double getSplittingProba() {
        return splittingProba;
    }

    public double getStDifAgeCouple() {
        return stDifAgeCouple;
    }

    /**
     * Nb of years in one iteration, particularly used to increase age of people
     */
    public int getStep() {
        return step;
    }

/**
 * Probability that an individual will look for jobs within her region.
 * Meant to be used after an individual looks for a job within her muncipality and fails
 * to find a suitable job. The individual will then try to find a job in "other" municipalities.
 *
 * @return Probability to look for a job in all the municipalities of the region. Wihtin the range of
 * 0 to 1, 0 and inclusive. 
 */
    public double  getProbLookingRegionalJobs () {
        return probLookingRegionalJobs;
    }
    /**
     * Number of times by iteration an individual tries to find an individual respecting the conditions to become its partner (join method)
     */
    public int getNbJoinTrials() {
        return nbJoinTrials;
    }

    public double getProbToAcceptNewResidence() {
        return probToAcceptNewResidence;
    }

    public double getResSatisfacMargin() {
        return resSatisfactMargin;
    }

    /**
     * File containing the list of municipality considered as close from finding a job point of view
     */
    public String getProxJobFile() {
        return proxJobFile;
    }

    /**
     * File containing the list of individuals with their activities in a municipality
     */
    public String getActivityFile() {
        return activityFile;
    }

    /**
     * File containing the households of a municipality
     */
    public String getPopulationFile() {
        return populationFile;
    }

    /**
     * File containing the scenarios
     */
    public String getScenarioFile() {
        return scenarioFile;
    }

    public String getOutputDir() {
        return outputDir;
    }
    
    public String getAgeDistributionFileName(){
        return populationDistributionFileName;
    }
    public String getWorkingPlaceTableFileName(){
       return workingPlaceTableFileName;
    }
    
    public String getPopulationHouseholdStructureFileName(){
        return populationHouseholdStructureFileName;
    }
    
    public String getHouseholdSizesFileName(){
        if (householdSizesFileName==null){
            householdSizesFileName = "HouseholdSizes.csv";
        }
        return householdSizesFileName;
    }
    public String getPopulationBirthsDeathsFileName(){
        return populationBirthsDeathsFileName;
    }
    
    public String getEconomicStatusFileName(){
        return economicStatusFileName;
    }
    
    public String getEmploymentTableFileName(){
        return employmentTableFileName;
    }
    public String getSoATableFileName(){
        return soATableFileName;
    }
    public String getFieldSeparator(){
        return fieldSeparator;
    }
  
    public EmigrationUpdater getEmigration() {
        return emigration;
    }

    public DynamicServiceEmployment getDynamicServiceEmployment() {
        return dynamicServiceEmployment;
    }

    /**
     * Creates a new array and fills it with the values of the Beta parameters
     * for the FirstSCPAttribution method (assign a SPC to an individual going from
     * Student to Unemployed/Worker
     * @return Array with ordered beta values ([0]=>SPC1, [1]=>SPC2, [2]=>SPC3, etc.)
     */
    public Double[] getFirstSPCBetas() {
        ArrayList<Double> betas = new ArrayList<Double>();
        betas.add(BetaFirstSPC1);
        betas.add(BetaFirstSPC2);
        betas.add(BetaFirstSPC3);
        betas.add(BetaFirstSPC4);
        betas.add(BetaFirstSPC5);
        betas.add(BetaFirstSPC6);
        betas.add(BetaFirstSPC7);
        betas.add(BetaFirstSPC8);

        // Ensure that the Beta value are not 0 (if they are 0 then set to 1
        for (int i=0;i<betas.size();i++){
            if (betas.get(i)==0){
                betas.set(i, new Double(1));
            }
        }

        return betas.toArray(new Double[1]);

    }
/**
     * Creates a new array and fills it with the values of the Beta parameters
     * for the firstAssginedSPC table (select a potential SPC to an EMPLOYED individual who is
     * looking for a new employment
     * @return Array with ordered beta values ([0]=>SPC1, [1]=>SPC2, [2]=>SPC3, etc.)
     */
    public Double[] getNewSPCEmployBetas() {
         ArrayList<Double> betas = new ArrayList<Double>();
        betas.add(BetaNewSPC1Employ);
        betas.add(BetaNewSPC2Employ);
        betas.add(BetaNewSPC3Employ);
        betas.add(BetaNewSPC4Employ);
        betas.add(BetaNewSPC5Employ);
        betas.add(BetaNewSPC6Employ);

         // Ensure that the Beta value are not 0 (if they are 0 then set to 1
        for (int i=0;i<betas.size();i++){
            if (betas.get(i)==0){
                betas.set(i, new Double(1));
            }
        }
        return betas.toArray(new Double[1]);
    }

    /**
     * Creates a new array and fills it with the values of the Beta parameters
     * for the firstAssginedSPC table (assign a SPC to an individual going from
     * Student to Unemployed/Worker
     * @return Array with ordered beta values ([0]=>SPC1, [1]=>SPC2, [2]=>SPC3, etc.)
     */
    public Double[] getNewSPCUnempBetas() {
        ArrayList<Double> betas = new ArrayList<Double>();
        betas.add(BetaNewSPC1Unemp);
        betas.add(BetaNewSPC2Unemp);
        betas.add(BetaNewSPC3Unemp);
        betas.add(BetaNewSPC4Unemp);
        betas.add(BetaNewSPC5Unemp);
        betas.add(BetaNewSPC6Unemp);
         // Ensure that the Beta value are not 0 (if they are 0 then set to 1
        for (int i=0;i<betas.size();i++){
            if (betas.get(i)==0){
                betas.set(i, new Double(1));
            }
        }
        return betas.toArray(new Double[1]);
    }
    
    /**
     * Indicates the number of run when doing an exploration if it is not
     * an exploration then this function returns NULL
     * 
     * @return The number of the exploration run being currently run or NULL
     * if the program was not run from an exploration
     */
    public int getRunPrefix(){
        return this.runPrefix;
    }

    /**
     * Returns the parameter jobbVacancyRate. This parameter is used to calculate
     * the number of new jobs generated each year. It is used as a calibration
     * parameter to get the additional number yearly jobs available on each 
     * municipality. 
     * 
     * @return the jobVacancyRate
     */
    public double getJobVacancyRate() {
        return jobVacancyRate;
    }

    public boolean isResidenceOwner(Individual ind){
        return isResidenceOwner.getValue(ind);
    }

    /**
     * @return the commutingTimeThreshold
     */
    public float getCommutingTimeThreshold() {
        return commutingTimeThreshold;
    }

       /**
     * @return the probabilityStudyOutside
     */
    public float getProbabilityStudyOutside() {
        return probabilityStudyOutside;
    }

    /**
     * @return the writeSimulationOutput
     */
    public boolean getWriteSimulationOutput() {
        return writeSimulationOutput;
    }

    /**
     * @param writeSimulationOutput the writeSimulationOutput to set
     */
    public void setWriteSimulationOutput(boolean writeSimulationOutput) {
        this.writeSimulationOutput = writeSimulationOutput;
    }

    /**
     * @return the createHTMLReport
     */
    public boolean getCreateHTMLReport() {        
        return createHTMLReport;
    }

    /**
     * @param createHTMLReport the createHTMLReport to set
     */
    public void setCreateHTMLReport(boolean createHTMLReport) {
        this.createHTMLReport = createHTMLReport;
    }

    
}

