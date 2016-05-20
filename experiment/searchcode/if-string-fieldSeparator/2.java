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
package fr.cemagref.prima.regionalmodel.altmark.observations;

import fr.cemagref.prima.regionalmodel.*;
import fr.cemagref.prima.regionalmodel.parameters.Parameters;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Manages the output of data from the simulation.
 * This class holds additional functionality to the ObservablesHanlder
 * provided by package fr.cemagref.observation.kernel. 
 * 
 * Specifically, this class allows saving certain output data in a 
 * format that makes it comparable with the real data for the altmark.
 * 
 * @author Baqueiro
 * 26.07.2011
 */
public class AltmarkObservableManager {

   private MunicipalitySet municipalitySet;
   private String fieldSeparator = ";";
   private boolean writeOutput;
   
    
    Parameters params;
  
    private static class BinGroups {

        private BinGroups() {
        }
        ;
         public static final Integer[] AGE_DISTRIBUTION = {0, 6, 10, 18, 25, 45, 65};
        public static final Integer[] FAMILIES_CHILDREN = {0, 3, 6, 10, 15, 18};
        public static final Integer[] MUNICIPALITY_SIZES = {0, 2000, 5000, 10000, 50000, 100000, 200000};
        public static final Integer[] AGE_HEADS = {0, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
        public static final int[] EMPLOYMENT_AGES = {15,25,50,65};
    }

    public AltmarkObservableManager(MunicipalitySet munSet, Parameters param) {
        this.municipalitySet = munSet;
        this.params = param;
        if (param.getFieldSeparator()!=null){
            fieldSeparator = param.getFieldSeparator();
        }
        this.writeOutput=params.getWriteSimulationOutput();
        if (writeOutput){
            if ( params.getCreateHTMLReport()){
                copyReportFiles();
            };                           
        }
    }

    /**
     * Copy files used for the HTML reports
     */
    private void copyReportFiles(){
        // set new path
        String newPath =  params.getOutputDir();
        InputStream stream =this.getClass().getResourceAsStream("htmlreports.zip");        
        
        try{
        extractFolder(stream, newPath);        
        stream.close();
        } catch (IOException ex){
            Logger.getLogger(this.getClass().getCanonicalName())
                    .log(Level.WARNING, "Error writing HTML reports" );
            
        }
      
    }
    
    /**
     * Extracts contents of a zip file. From: 
     * http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
     * @param zipFile The input file to extract
     * @throws ZipException
     * @throws IOExceoption 
     */
    private void extractFolder(InputStream stream, String newPath) throws ZipException, IOException{
       int BUFFER = 2048;
       
       ZipInputStream zipInput = new ZipInputStream(stream);                            
       ZipEntry entry;
       
       while ((entry = zipInput.getNextEntry()) !=null){            
           String currentEntry = entry.getName();
           if (currentEntry.endsWith("html")){
               currentEntry = params.getRunPrefix()+"-"+currentEntry;
           }
           File destFile = new File(newPath, currentEntry);
           File destinationParent = destFile.getParentFile();
           
            
           destinationParent.mkdirs();
           
           if (entry.isDirectory()){
               continue;
           }
           
           int currentByte;
           byte data[] = new byte[BUFFER];
           
           // write file to disk
           FileOutputStream fos = new FileOutputStream(destFile);
           BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
           
           // read until last byte is found
           while ((currentByte= zipInput.read(data, 0, BUFFER))!=-1){
               dest.write(data,0,currentByte);
           }           
           dest.flush();
           dest.close();           
           
           // Replace template elements in HTML files with simulation run information.
           if (destFile.getName().endsWith("html")){
               FileInputStream readStream = new FileInputStream(destFile);
               // read file contents 
               String cont = convertStreamToString(readStream);
               readStream.close();
               cont= cont.replaceAll( "\\[RUN\\]" , String.valueOf(params.getRunPrefix()));                              
               FileOutputStream writeStream = new FileOutputStream(destFile);                              
               writeStream.write(cont.getBytes());
               writeStream.close();
           }
           
       }
    }
    /**
     * Writes the age distribution changes in the output file
     */
    private void writeAgeDistributionChanges() {
        BufferedWriter outFile;

        try {
            outFile = getFile(params.getAgeDistributionFileName(),"ageDistributionHeader.txt");                    
   
            
            int [] regionTotals = new int[BinGroups.AGE_DISTRIBUTION.length];
            for (Municipality m : municipalitySet.getMyMunicipalities()) {
                SortedMap<Integer, Integer> map =
                       getMunicipalityPopulationAge(BinGroups.AGE_DISTRIBUTION, m.getName());
                // write simulation year
                outFile.write(String.valueOf(municipalitySet.getCurrentYear()) + fieldSeparator);
                // write name of the municipality
                outFile.write(m.getName() + fieldSeparator);

                // write ages and calculate popoulation total 
                int population = 0;
                int i=0;
                for (Entry<Integer, Integer> entry : map.entrySet()) {
                    population += entry.getValue();
                    outFile.write(entry.getValue().toString() + fieldSeparator);
                    regionTotals[i++]+=entry.getValue();                    
                }
                // write total population
                outFile.write(population + fieldSeparator);
                outFile.newLine();
            }
            // write region totals
            
            outFile.close();
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getCanonicalName())
                    .log(Level.WARNING, "Error writing outputfile{0}", params.getAgeDistributionFileName());
            return;
        }
    }

    private void writeWorkingPlaceChanges(){
        
        // create file and write header
        BufferedWriter outFile = null;
        StringBuilder sb= new StringBuilder();
        try {            
            outFile = getFile(params.getWorkingPlaceTableFileName(),"WorkignPlaceHeaders.txt");            
            
            municipalitySet.updateMunicipalityWorkers();
            for (Municipality m : municipalitySet.getMyMunicipalities()) {
                sb.setLength(0);
                sb.append(String.valueOf(municipalitySet.getCurrentYear())).append(fieldSeparator);
                sb.append(m.getName()).append(fieldSeparator);                
                sb.append(String.valueOf(m.getTotalWorkers()));
                outFile.write(sb.toString());
                outFile.newLine();
            }            
            outFile.close();   
        }
                
        catch (IOException ex) {
              Logger.getLogger(this.getClass().getCanonicalName())
                    .log(Level.WARNING, "Error writing outputfile{0}", params.getWorkingPlaceTableFileName());
        }
     
    }
    private void writeSoAChanges (){
        
        // create file and write header
        BufferedWriter outFile = null;
        
        try {
            
            outFile = getFile(params.getSoATableFileName(),"SoAHeader.txt");                                   
            municipalitySet.updateMunicipalityWorkers();                                  
            // Write Sector of activity data for each muncipality
            for (Municipality m: municipalitySet.getMyMunicipalities()){
                StringBuilder sb = new StringBuilder();
                
                sb.append(String.valueOf(municipalitySet.getCurrentYear())).append(fieldSeparator);
                sb.append(m.getName()).append(fieldSeparator);                
                
                int[] soa = m.getSoAofResidents();
                                                
                /// write data from muicipality
                for (int i=0;i<soa.length;i++){
                    sb.append(soa[i]).append(fieldSeparator);
                          
                }
                
                outFile.write(sb.toString());
                outFile.newLine();
            }      
          
            outFile.close();
            
        } catch (IOException ex) {
             Logger.getLogger(this.getClass().getCanonicalName())
                    .log(Level.WARNING, "Error writing outputfile{0}", params.getAgeDistributionFileName());            
        }
    }
    
    private void writeEmploymentChanges() {
        // create file and write header
        BufferedWriter outFile;
        try {
            outFile = getFile(params.getEmploymentTableFileName(),"employmentHeader.txt");
          // write the data for each municipality and calculate region sums
            for (Municipality m : municipalitySet.getMyMunicipalities()) {
                StringBuilder sb = new StringBuilder();

                int municEmployed = m.getTotalWithStatus(Individual.Status.WORKER);
                int municUnemployed = m.getTotalWithStatus(Individual.Status.UNEMPLOYED);
                sb.append(String.valueOf(municipalitySet.getCurrentYear())).append(fieldSeparator)
                        .append(m.getName()).append(fieldSeparator)                      
                        .append(municEmployed).append(fieldSeparator)
                        .append(municUnemployed).append(fieldSeparator);
           
                int[] employed = m.getGroupsInAgeRange(BinGroups.EMPLOYMENT_AGES, Individual.Status.WORKER);
                int[] unemployed = m.getGroupsInAgeRange(BinGroups.EMPLOYMENT_AGES, Individual.Status.UNEMPLOYED);

                // write the number of employed and unemployed in the required order
                for (int i = 0; i < employed.length; i++) {
                    sb.append(String.valueOf(employed[i])).append(fieldSeparator);
                    sb.append(String.valueOf(unemployed[i])).append(fieldSeparator);
            

                }
                
                // write output
                outFile.write(sb.toString());
                outFile.newLine();
            }                 
            outFile.close();
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Error writing outputfile{0}",
                    params.getEmploymentTableFileName());
        }
   
    }

    private void writeBirthsAndDeathsChanges() {
        try {
            BufferedWriter outFile = getFile(params.getPopulationBirthsDeathsFileName(),"birthsDeathsHeader.txt");
            for (Municipality m : municipalitySet.getMyMunicipalities()) {
                // write name of the municipality
                
                StringBuilder sb=new StringBuilder();
                
                sb.append(String.valueOf(municipalitySet.getCurrentYear())).append(fieldSeparator)
                        .append(m.getName()).append(fieldSeparator)
                        .append(m.getCounters().getInitialPopulation()).append(fieldSeparator)
                        .append(m.getCounters().getNumBirth()).append(fieldSeparator)
                        .append(m.getCounters().getNumDeath()).append(fieldSeparator)
                        .append(m.getCounters().getNbIndividualMoveIn()).append(fieldSeparator)
                        .append(m.getCounters().getNbIndividualMoveOut()).append(fieldSeparator)
                        .append(m.getPopulationSize()).append(fieldSeparator);                                
                outFile.write( sb.toString());
                outFile.newLine();
            }
            outFile.close();
        } catch (IOException ex) {
            Logger.getLogger(
                    this.getClass().getCanonicalName()).log(Level.WARNING, "Error writing outputfile{0}", params.getPopulationBirthsDeathsFileName());
            return;
        }
    }
    
  
    private BufferedWriter getFile(String name, String headerName) throws IOException {
        BufferedWriter outFile = null;
        boolean append=false;
        StringBuilder sb = new StringBuilder();
        sb.append(params.getOutputDir()).append("raw-data-tables/").append(params.getRunPrefix())
                .append("-").append(name);
        String fileName = sb.toString();
        File f = new File(fileName);
        
        if (f.exists()){
            append=true;
        }
        else{
            f.getParentFile().mkdirs();
        }
        outFile = new BufferedWriter(new FileWriter(fileName,append));
        
        if (!append) {
            // write headers
            outFile.write(
                    new StringBuilder().append("#Source:").append(fieldSeparator).append("PRIMA Simulator").toString());
            outFile.newLine();
            // read string from resources and write it to output file
            InputStream stream =this.getClass().getResourceAsStream(headerName);
            String headerStr =  convertStreamToString(stream);
            stream.close();
            // replace the tab with whatever field separator is 
            headerStr= headerStr.replace("\t", fieldSeparator);
            outFile.write(headerStr);                                                
            outFile.newLine();
            
        }
        return outFile;
    }

    private void writeHouseholdSizesChanges(){
        try{
                BufferedWriter outFile =
                    getFile(params.getHouseholdSizesFileName(),"hhSizesHeader.txt");
            int sizes[] = new int[5];
            Arrays.fill(sizes, 0);
            StringBuilder sb = new StringBuilder();
            for (Municipality mun: municipalitySet.getMyMunicipalities()){
                sb.setLength(0);
                for (Household hh: mun.getStableHouseholds()){
                    if (hh.getSize()<5){
                        sizes[hh.getSize()-1]++;
                    }
                    else{
                        sizes[4]++;
                    }
                    
                }
                  sb.append(String.valueOf(municipalitySet.getCurrentYear())).append(fieldSeparator)
                          .append(mun.getName()).append(fieldSeparator);                
                for (int i=0; i<sizes.length;i++){
                    sb.append(String.valueOf(sizes[i])).append(fieldSeparator);
                }
                outFile.write(sb.toString());
                outFile.newLine();
            }
            
            outFile.close();
        }  catch (IOException ex) {
            Logger.getLogger(
                    this.getClass().getCanonicalName()).log(Level.WARNING, "Error writing outputfile{0}",
                    params.getPopulationHouseholdStructureFileName());
        }

    }
    /**
     * Creates file with the household structure information for the
     * current state of the model.
     */
    private void writeHouseholdStructureChanges() {
        try {
            
            //TODO-OMAR: Implement household structure log which writes count of hh by types (single, couple, ...)
            BufferedWriter outFile =
                    getFile(params.getPopulationHouseholdStructureFileName(),"householdStructureHeader.txt");
            
                 
            outFile.newLine();
            outFile.close();
        } catch (IOException ex) {
            Logger.getLogger(
                    this.getClass().getCanonicalName()).log(Level.WARNING, "Error writing outputfile{0}",
                    params.getPopulationHouseholdStructureFileName());
            return;
        }
    }

    /**
     * Writes the distribution of economic activities among individuals for each municipality
     */
    public void writeEconomicStatus(){
          try {
            BufferedWriter outFile = getFile(params.getEconomicStatusFileName(),"economicStatusHeader.txt");          
            for (Municipality m : municipalitySet.getMyMunicipalities()) {
                // write name of the municipality
                
                StringBuilder sb=new StringBuilder();
                
                sb.append(String.valueOf(municipalitySet.getCurrentYear())).append(fieldSeparator)
                        .append(m.getName()).append(fieldSeparator);

                                // write total population
                sb.append(m.getPopulationSize()).append( fieldSeparator);
                // write total employed
                sb.append( m.getTotalWithStatus(Individual.Status.WORKER)).append(fieldSeparator);
                // write Unemployed
                sb.append( m.getTotalWithStatus(Individual.Status.UNEMPLOYED)).append(fieldSeparator);
                // write Retired
                sb.append( m.getTotalWithStatus(Individual.Status.RETIRED) ).append(fieldSeparator);
                // write Students
                sb.append(m.getTotalWithStatus(Individual.Status.STUDENT)).append( fieldSeparator);                
                // write Inactive
                sb.append(m.getTotalWithStatus(Individual.Status.INACTIVE) ).append( fieldSeparator);

                
                outFile.write(sb.toString());
                
                
                outFile.newLine();
            }
            outFile.close();
        } catch (IOException ex) {
            Logger.getLogger(
                    this.getClass().getCanonicalName()).log(Level.WARNING, "Error writing outputfile{0}", params.getPopulationBirthsDeathsFileName());
            return;
        }
    }
    
    /**
     * Records the output data into the corresponding output files
     */
    public void fireChanges() {              
        // skip  saving chagnes when indicated
        if (!this.writeOutput){
            return;
        }
        writeAgeDistributionChanges();
        writeBirthsAndDeathsChanges();
        writeHouseholdStructureChanges();
        writeHouseholdSizesChanges();
        writeEmploymentChanges();
        writeSoAChanges();
        writeWorkingPlaceChanges();
        writeEconomicStatus();
        
    }  
    
    

    /**
     * Generates a table containing the percentages of household sizes 
     * in the region grouped by the age of the head of household defined in the
     * input age array. Household sizes distributions are returned in the Double
     * array of the map as 1, 2, 3 and 4+ individuals.
     * 
     * Each number in the ages parameter corresponds to a "lower bound" of the
     * created groups. The <code>ages</code> is sorted from lowest to highest 
     * in order to return a sorted map. 
     *
     * The Integer values in the <code>ages</code> array are used as the keys
     * in the returned map.
     * 
     * @param ages  The list of ages to use as lower-bound of the table
     * @return Table with the percentage of household sizes
     * 
     */
    public SortedMap<Integer, Double[]> getRegionHouseholdSizesByAgeOfHeadPercentages(final Integer[] ages) {
        final SortedMap<Integer, Double[]> map = new TreeMap<Integer, Double[]>();
        final SortedMap<Integer, Integer> totalsMap = new TreeMap<Integer, Integer>();
        // popuplate map with keys initializing counts to 0
        for (Integer i : ages) {
            // bins representing households with 1, 2, 3 and 4+ individuals
            map.put(i, new Double[]{0d, 0d, 0d, 0d});
            totalsMap.put(i, 0); // used to calculate the averages
        }
        // traverse municipalities and households 
        for (Municipality m : municipalitySet.getMyMunicipalities()) {
            for (Household h : m.getStableHouseholds()) {
                // count household in the corresponding group based on the
                // age of the head of household
                final int ageHead = h.determineLeader().getAge();
                // determine the correct row based on the head age.           
                final Integer key = getAgeKeyFromMap(map.keySet(), ageHead);
                
                if (!map.containsKey(key)) {
                    continue;
                }
                Double[] row = map.get(key);
                totalsMap.put(key, totalsMap.get(key) + 1);
                // if the household size is within one of the size groups then 
                // count it in this group
                
                if (h.getSize()==0){continue;}
                
                if (h.getSize() < row.length ) {
                    row[h.getSize() - 1]++;
                } else { // if the household size is bigger, count it in the last
                    row[row.length - 1]++;
                }
            }
        }
        // calculate percentage
        for (Entry<Integer, Double[]> entry : map.entrySet()) {
            Double[] row = entry.getValue();
            final Integer key = entry.getKey();
            for (int i = 0; i < row.length; i++) {
                row[i] /= totalsMap.get(key);
            }
        }
        return map;
    }

    /**
     * Calculates the number of people in the defined municipality grouping
     * them in bins as defined by the <code>ages</code> parameter. 
     * 
     * Each number in the ages parameter corresponds to a "lower bound" of the
     * created groups. The <code>ages</code> is sorted from lowest to highest 
     * in order to return a sorted map. 
     * 
     * The Integer values in the <code>ages</code> array are used as the keys
     * in the returned map.
     * 
     * @param ages The list of ages to use as lower-bound of the table
     * @param munName the name of the municipality used to calculate the table
     * @return  The table with the population age grouped by ages
     */
    public SortedMap<Integer, Integer> getMunicipalityPopulationAge(final Integer ages[], final String munName)
            throws IllegalArgumentException {
        final SortedMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        final Municipality mun = municipalitySet.getMunicipality(munName);

        if (mun == null) {
               throw new IllegalArgumentException("Municpiality was not found");
        }
        
        // create Map bins with the provied ages.
        for (Integer i : ages) {
            map.put(i, 0);
        }
        // traverse individual's list and count them in the corresponding bin
        for (Household h : mun.getMyHouseholds()) {
            for (Individual i : h.getCopyOfMembers()) {
                final Integer age = Integer.valueOf(i.getAge());
                Integer key = null;
                // locate correct age bin
                for (Entry<Integer, Integer> entry : map.entrySet()) {
                    if (entry.getKey() > age) {
                        break;
                    }
                    key = entry.getKey();
                }
                if (key != null) {
                    map.put(key, map.get(key) + 1);
                }
            }
        }       

        if (map.isEmpty()) {
            throw new IllegalArgumentException("Municpiality was not found");
        }
        return map;
    }

     /**
     * Calculates the average number of individuals living on each residence of
     * different sizes.
     * @return Map containing the average number of individuals (value) living
     * on each distinct residence size (key)
     */
    public SortedMap<Integer, Double> getAverageIndividualsPerResidenceSize(){
        SortedMap<Integer,Double> map = new TreeMap<Integer, Double>();
        SortedMap<Integer,Integer> resCount = new TreeMap<Integer, Integer>();
        for (Municipality m : municipalitySet.getMyMunicipalities()) {
            for (int i = 0; i < municipalitySet.getParameters().getNbSizeRes(); i++) {                
                for (Residence r : m.getOccupiedResidences(i)) {
                    if (map.containsKey(i)) {
                        map.put(i, map.get(i) + ((r.getNbIndividualResidents()- map.get(i))/resCount.get(i)));
                        resCount.put(i, resCount.get(i) + 1);
                    } else {
                        map.put(i, (double)(r.getNbIndividualResidents()));
                        resCount.put(i, 1);
                    }
                }
            }
            System.out.println(map);
        }
        return map;
    }
    /**
     * Generates a table containing the number   of families that have children
     * of the age defined in the input age bins.  The data is separated by Single
     * and Couple families.
     * 
     * Because one family may be counted twice (for example, a family with one
     * children with 4 years and one children of 15 years) the sum of the percentages
     * does not equal to one.
     * 
     * Each number in the ages parameter corresponds to a "lower bound" of the
     * created groups. The <code>ages</code> is sorted from lowest to highest 
     * in order to return a sorted map. 
     *
     * The Integer values in the <code>ages</code> array are used as the keys
     * in the returned map.
     * 
     * @param ages The list of ages to use as lower-bound of the table
     * @return Table with the percentage of families by children's age
     */
    public SortedMap<Integer, Double[]> getRegionFamiliesGroupedByChildrenAgePercentage(final Municipality mun,final Integer ages[])
            throws IllegalArgumentException {
        final SortedMap<Integer, Double[]> map = new TreeMap<Integer, Double[]>();
        // create Map bins with the provied ages.
        for (Integer i : ages) {
            map.put(i, new Double[]{0d, 0d});
        }
        int totalHh = 0;
        // traverse individual's list and count them in the corresponding bin
        for (Household h : mun.getStableHouseholds()) {
            // count household if couple with children or Single with children
            if (h.getHshType() != Household.Type.COUPLE_W_CHILDREN && h.getHshType() != Household.Type.SINGLE_W_CHILDREN) {
                continue;
            }

            totalHh++;
            for (Individual ind : h.getChildren()) {
                // locate correct age bin
                final Integer key = getAgeKeyFromMap(map.keySet(), ind.getAge());
                if (!map.containsKey(key)) {
                    continue;
                }
                // increase the value for  corresponding household 
                Double[] row = map.get(key);
                if (h.getHshType() == Household.Type.COUPLE_W_CHILDREN) {
                    row[0]++; // couples
                } else {
                    row[1]++; // single
                }
            }

        }
                
        // Calculate percentage of each row in the table 
        for (Entry<Integer, Double[]> entry : map.entrySet()) {
            Double[] row = entry.getValue();
            row[0] /= totalHh;
            row[1] /= totalHh;       
        }
       
        return map;
    }
    /**
     * Returns the map key that corresponds to the specified age for a map where
     * the keyset correpsonds to the lower-bound ages
     * @param keylist A list containing the ordered 
     * @param age
     * @return  The highest age in the provided list which is lower or equal to age
     */
    private int getAgeKeyFromMap(final Set<Integer> keyset, final int age){
        int index=0;
        // locate correct age bin
        for (Integer entry : keyset) {
            if (entry > age) {
                break;
            }
            index = entry;
        }
        return index;
    }
    /**
     * Extracts the text form the input stream as a text string 
     * @param is The InputStream containing the text
     * @return The data read from the provided input stream as text.
     */
    private String convertStreamToString (InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();    
    }
}

