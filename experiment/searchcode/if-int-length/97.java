package org.ala.spatial.analysis.index;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import org.ala.spatial.analysis.cluster.Record;
import org.ala.spatial.util.Layer;
import org.ala.spatial.util.Layers;
import org.ala.spatial.util.OccurrencesFieldsUtil;
import org.ala.spatial.util.SimpleRegion;
import org.ala.spatial.util.SpatialLogger;
import org.ala.spatial.util.TabulationSettings;
import org.springframework.util.StringUtils;

/**
 * builder for occurrences index.
 *
 * expects occurrences in a csv containing at least species, longitude
 * and latitude.
 *
 * expects TabulationSettings fields for
 * 	<li>occurrences csv file location (has header)
 * 	<li>list of relevant hierarchy related columns in occurrences csv
 * 		the last three must relate to species, longitude and latitude
 *	<li>index directory for writing index files
 *	<li>
 *
 * creates and interfaces into:
 * <li><code>OCC_SORTED.csv</code>
 * 		csv with header row.
 *
 * 		records contain:
 * 			optional hierarchy fields, top down order, e.g. Family,
 * 			species name
 * 			longitude as decimal
 * 			latitude as decimal
 *
 * 	<li><code>OCC_SPECIES.csv</code>
 * 		csv with no header row.
 *
 * 		records contain:
 * 			species name
 * 			starting file position occurance in OCC_SORTED.csv
 * 			ending file position occurance in OCC_SORTED.csv
 * 			starting record number in OCC_SORTED.csv
 * 			ending record number in OCC_SORTED.csv
 *
 * @author adam
 *
 */
public class OccurrencesIndex {

    /**
     * sorted records filename
     * points (coordinates) records filename
     * sensitive coordinates filename
     *
     * excluded records filename
     * 
     * construction creates temporary files with '_0' to '_n' parts
     */
    static final String SORTED_FILENAME = "OCC_SORTED.csv";
    static final String POINTS_FILENAME = "OCC_POINTS.dat";
    static final String SENSITIVE_COORDINATES = "SENSITIVE_COORDINATES";
    static final String EXCLUDED_FILENAME = "OCC_EXCLUDED.csv";
    /**
     * list of file positions for SORTED_FILENAME for lines
     */
    static final String SORTED_LINE_STARTS = "OCC_SORTED_LINE_STARTS.dat";
    /**
     * sorted store of String[] * 2, first for ConceptId, 2nd for Names
     */
    static public final String SORTED_CONCEPTID_NAMES = "OCC_CONCEPTID_NAMES.dat";
    /**
     * points filename, in 0.5 degree grid, latitude then longitude, sort order
     */
    //static final String POINTS_FILENAME_05GRID = "OCC_POINTS_05GRID.dat";
    /**
     * index for 05grid points file
     */
    static final String POINTS_FILENAME_05GRID_IDX = "OCC_POINTS_05GRID_IDX.dat";
    /**
     * world grid of 05grid point index entry points
     *
     * for retrieving record numbers, and point longitude and latitudes,
     * from 05grid index
     */
    static final String POINTS_FILENAME_05GRID_KEY = "OCC_POINTS_05GRID_KEY.dat";
    /**
     * species index file, contains IndexRecord for each species
     */
    static final String SPECIES_IDX_FILENAME = "OCC_IDX_SPECIES0.dat";
    /**
     * species record array with family record index, to lookup family from
     * record number
     */
    //static final String SPECIES_TO_FAMILY = "OCC_SPECIES_TO_FAMILY.dat";
    //static final String SINGLEINDEX_TO_FAMILY = "OCC_SINGLEINDEX_TO_FAMILY.dat";
    /**
     * prefix for non-species index files
     */
    static final String OTHER_IDX_PREFIX = "OCC_IDX_";
    /**
     * postfix for non-species index files
     */
    static final String OTHER_IDX_POSTFIX = ".dat";
    /**
     * map of id's to record numbers
     */
    static final String ID_LOOKUP = "ID_LOOKUP.dat";
    /**
     * size of parts to be exported by splitter
     */
    static final int PART_SIZE_MAX = 1000000000; //move to TabulationSettings
    String occurrences_csv;
    String index_path;
    /**
     * instance of all indexed data for filtering
     */
    ArrayList<IndexedRecord[]> all_indexes = new ArrayList<IndexedRecord[]>(); //temporary
    /**
     * single instance of all indexed data for filtering
     */
    IndexedRecord[] single_index = null;
    /**
     * instance of all points records
     *
     * for frequent use
     */
    double[][] all_points = null;
    /**
     * mapping between conceptId's and paired searchable names
     */
    String[] occurrences_csv_field_pairs_ConceptId;
    String[] occurrences_csv_field_pairs_Name;
    int[] occurrences_csv_field_pairs_ToSingleIndex;
    int[] occurrences_csv_field_pairs_FirstFromSingleIndex;  //length == single_index.length
    IndexedRecord[] speciesSortByRecordNumber = null;
    int[] speciesSortByRecordNumberOrder = null;
    /**
     * index to actual csv field positions to named
     * fields
     */
    int[] column_positions;
    /**
     * index lookup for records points on sorted 0.5 degree grid
     */
    int[] grid_points_idx = null;
    /**
     * grid 360degree/0.5degree square, for entry position into grid_points
     */
    int[][] grid_key = null;
    /**
     * extra_indexes to record number mappings
     *
     * key: String
     * object: ArrayList<Integer> when building, int [] when built.
     */
    HashMap<String, Object>[] extra_indexes = null;
    /**
     * sorted list of common names and reference to single_index row in file_pos
     */
    //CommonNameRecord[] common_names_indexed;
    /**
     * all sensitive coordinates, or -1
     * [][0] = longitude
     * [][1] = latitude
     */
    double[][] sensitiveCoordinates;
    //public String[][] cluster_records = null;
    long[] occurrenceId = null;
    HashMap<String, Object> attributesMap = null;
    int[] speciesNumberInRecordsOrder = null;
    //int[] species_to_family = null;
    //static int[] singleindex_to_family = null;
    long[] sortedLineStarts = null;
    CountDownLatch updating = null;

    /*
     * SpeciesIndex add of single_index requires a translation between records
     */
    int[] speciesIndexLookup;
    /*
     * parent dataset
     */
    Dataset dataset = null;

    /**
     * constructor
     */
    OccurrencesIndex(Dataset d, String occurrencesFilename, String directoryName) {
        dataset = d;
        TabulationSettings.load();
        occurrences_csv = occurrencesFilename;
        index_path = directoryName;
    }

    /**
     * performs update of 'indexing' for new points data
     */
    public void occurrencesUpdate() {
        this.updating = new CountDownLatch(1);

        /* order is important */
        System.out.println("loading occurrences: " + occurrences_csv);

        //begin here
        makePartsThreaded();
        mergeParts();

        if (noRecords()) {
            return;
        }

        exportSortedGridPoints();

        //run these functions at the same time as other build_all requests
        class makingThread extends Thread {

            @Override
            public void run() {
                int recordcount = exportFieldIndexes();

                convertPropertyFieldCSVsToDATs(recordcount);

                updating.countDown();

                System.out.println("END of OCCURRENCES INDEX");
            }
        }
        ;
        makingThread mt = new makingThread();
        mt.start();
    }

    void mergeParts() {
        //count # of parts
        int numberOfParts = 0;
        while ((new File(
                index_path
                + SORTED_FILENAME + "_" + numberOfParts)).exists()) {
            numberOfParts++;
        }

        //merge sensitive coordinates parts
        int i;
        try {
            FileWriter fw = new FileWriter(
                    index_path
                    + SENSITIVE_COORDINATES + ".csv");

            for (i = 0; i < numberOfParts; i++) {
                BufferedReader r = new BufferedReader(new FileReader(
                        index_path
                        + SENSITIVE_COORDINATES
                        + i));
                String s;
                while ((s = r.readLine()) != null) {
                    fw.append(s).append("\n");
                }
                r.close();
            }

            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TODO: move existing joined output to *_(numberOfParts+1) for inclusion

        String[] lines = new String[numberOfParts];
        BufferedReader[] reader = new BufferedReader[numberOfParts];
        int[] positions = new int[numberOfParts];
        ArrayList<double[][]> points = new ArrayList<double[][]>(numberOfParts);
        int pointsCount = 0;

        try {
            //open output streams (points + sorted data)
            for (i = 0; i < numberOfParts; i++) {
                reader[i] = new BufferedReader(new FileReader(
                        index_path + SORTED_FILENAME
                        + "_" + i));
                lines[i] = reader[i].readLine();
                if (lines[i] != null) {
                    lines[i] = lines[i].trim();
                }
                //open points
                double[][] tmp = getPointsPairs(i);
                points.add(tmp);
                positions[i] = 0;
                pointsCount += tmp.length * 2;
            }

            FileWriter fw = new FileWriter(index_path
                    + SORTED_FILENAME);
            double[] aPoints = new double[pointsCount];
            int pointsPos = 0;

            //write to file
            int finishedParts = 0;
            String topline;
            while (finishedParts < numberOfParts) {
                //get top record
                topline = null;
                for (i = 0; i < numberOfParts; i++) {
                    if ((topline == null && lines[i] != null)
                            || (topline != null && lines[i] != null
                            && topline.compareTo(lines[i]) > 0)) {
                        topline = lines[i];
                    }
                }

                //write top records & increment
                for (i = 0; i < numberOfParts; i++) {
                    if (lines[i] != null && topline.equalsIgnoreCase(lines[i])) {
                        fw.append(lines[i]);
                        fw.append("\n");

                        //read up next line
                        lines[i] = reader[i].readLine();
                        if (lines[i] == null) {
                            finishedParts++;    //flag that one more is finsihed
                        } else {
                            lines[i] = lines[i].trim();
                        }

                        aPoints[pointsPos] = points.get(i)[positions[i]][0];
                        aPoints[pointsPos + 1] = points.get(i)[positions[i]][1];
                        positions[i]++;
                        pointsPos += 2;
                    }
                }
            }

            fw.close();

            /* export points */
            RandomAccessFile pointsfile = new RandomAccessFile(
                    index_path + POINTS_FILENAME,
                    "rw");
            byte[] b = new byte[pointsPos * 8];
            ByteBuffer bb = ByteBuffer.wrap(b);

            for (i = 0; i < pointsPos; i++) {
                bb.putDouble(aPoints[i]);
            }

            pointsfile.write(b);
            pointsfile.close();

            //delete parts
            //for (i = 0; i < numberOfParts; i++) {
            //    (new File(index_path
            //            + SORTED_FILENAME + "_" + i)).delete();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.gc();
    }

    /**
     * method to determine if the index is up to date, as a whole
     *
     * Compares dates on last written index file
     *      <code>index_path + SORTED_CONCEPTID_NAMES + "1.csv"</code>
     * to
     *      <code>occurrences_csv</code>
     *
     * @return true if index is up to date
     */
    public boolean isUpToDate() {
        //occurrences file 
        File occFile = new File(occurrences_csv);

        //last written index file
        File occLastFile = new File(index_path + SORTED_CONCEPTID_NAMES + "1.csv");

        if (occFile.exists() && occLastFile.exists()) {
            return occFile.lastModified() < occLastFile.lastModified();
        }

        return false;
    }

    /**
     * method to determine if the field indexes exist for:
     *      <code>index_path + TabulationSettings.geojson_property_names[i] + ".dat"</code>
     *
     * @return true if index is up to date
     */
    public boolean isUpToDateFieldIndexes() {
        for (int i = 0; i < TabulationSettings.geojson_property_names.length; i++) {
            if (!(new File(index_path + TabulationSettings.geojson_property_names[i] + ".dat")).exists()) {
                return false;
            }
        }

        return true;
    }

    /**
     * splits a csv line with " text qualifier
     *
     * states:
     * 	begin term: if " then set qualified to true
     * 	in term: qualified and ", then end qualified and end term
     * 	in term: not qualified and , then end term
     * 	in term: qualified and "" then "*
     *
     */
    static String[] split(String line) {
        ArrayList<String> al = new ArrayList<String>();
        int i;

        boolean qualified = false;
        boolean begin_term = true;
        boolean end_term = false;

        String word = "";
        for (i = 0; i < line.length(); i++) {
            if (begin_term && line.charAt(i) == '"') {
                qualified = true;
                begin_term = false;
            } else if (!qualified
                    && line.charAt(i) == ',') { //end term
                end_term = true;
            } else if (qualified
                    && line.charAt(i) == '"'
                    && line.length() > i + 1
                    && line.charAt(i + 1) == '"') {//one "
                i++;
                word += line.charAt(i + 1);
            } else if (qualified
                    && line.charAt(i) == '"'
                    && ((line.length() > i + 1
                    && line.charAt(i + 1) == ',')
                    || line.length() == i + 1)) {//end term
                end_term = true;
                qualified = false;
                i++;
            } else {
                word += line.charAt(i);
            }
            if (end_term) {
                begin_term = true;
                al.add(word);
                word = "";
                end_term = false;
            }
        }
        String[] string = new String[al.size()];
        for (i = 0; i < al.size(); i++) {
            string[i] = al.get(i);
        }
        return string;
    }

    /**
     * operates on OCC_SORTED.csv
     *
     * generates an index for each occurrences field (minus longitude
     * and latitude)
     */
    int exportFieldIndexes() {
        System.gc();

        System.out.println("doing exportFieldIndexes (after gc)");

        String[] columns = TabulationSettings.occurrences_csv_fields;

        OccurrencesFieldsUtil ofu = new OccurrencesFieldsUtil();
        ofu.load();

        int countOfIndexed = ofu.onetwoCount;

        /* first n columns are indexed, n=countOfIndexed */
        TreeMap<String, IndexedRecord>[] fw_maps = new TreeMap[countOfIndexed];

        /* build up id lookup for additional columns */
        extra_indexes = new HashMap[ofu.extraIndexes.length];

        int i;

        for (i = 0; i < extra_indexes.length; i++) {
            extra_indexes[i] = new HashMap<String, Object>();
        }

        //names vs concept ids
        TreeMap<String, String> idnames = new TreeMap<String, String>();
        int[] idnamesIdx = new int[TabulationSettings.occurrences_csv_field_pairs.length];
        for (int j = 0; j < TabulationSettings.occurrences_csv_field_pairs.length; j++) {
            for (i = 0; i < ofu.columnNames.length; i++) {
                if (TabulationSettings.occurrences_csv_field_pairs[j].equalsIgnoreCase(ofu.columnNames[i])) {
                    idnamesIdx[j] = i;
                    break;
                }
            }
            if (ofu.columnNames.length == i) {
                System.out.println("ERROR:" + TabulationSettings.occurrences_csv_field_pairs[j]);
            }
        }

        //init for sorted line starts
        getPointsPairs();   //ensures all_points is loaded
        sortedLineStarts = new long[all_points.length];

        //init for sensitive coordinates
        Vector<String[]> vsensitive_coordinates = getVSensitiveCoordinates(null);
        java.util.Collections.sort(vsensitive_coordinates, new Comparator<String[]>() {

            public int compare(String[] c1, String[] c2) {
                return c1[0].compareTo(c2[0]);
            }
        });
        SpatialLogger.log("SensitiveCoordinates: sorted");
        int recordCount = getPointsPairs().length;   //for number of records
        sensitiveCoordinates = new double[recordCount][2]; //[][0] long, [][1] lat
        //set to -1
        for (i = 0; i < sensitiveCoordinates.length; i++) {
            sensitiveCoordinates[i][0] = -1;
            sensitiveCoordinates[i][1] = -1;
        }
        int idColumn = (new OccurrencesFieldsUtil()).onetwoCount;
        String[] search_for = new String[3];
        int pos;

        long filepos = 0;
        int recordpos = 0;

        try {
            RandomAccessFile fw = new RandomAccessFile(index_path
                    + "OCCURRENCE_ID.dat", "rw");
            FileWriter[] attributes =
                    new FileWriter[TabulationSettings.geojson_property_names.length];
            for (i = 0; i < attributes.length; i++) {
                attributes[i] = new FileWriter(index_path
                        + TabulationSettings.geojson_property_names[i] + ".txt");
            }

            BufferedReader br = new BufferedReader(new FileReader(index_path + SORTED_FILENAME));
            String s;
            String[] sa;

            int progress = 0;

            for (i = 0; i < countOfIndexed; i++) {
                fw_maps[i] = new TreeMap<String, IndexedRecord>();
            }
            System.out.println("\r\nsorted columns: " + countOfIndexed);

            String[] last_value = new String[countOfIndexed];
            long[] last_position = new long[countOfIndexed];
            int[] last_record = new int[countOfIndexed];

            for (i = 0; i < countOfIndexed; i++) {
                last_value[i] = "";
                last_position[i] = 0;
                last_record[i] = 0;
            }

            ArrayList<Integer>[] obj = new ArrayList[extra_indexes.length];
            String[] prev_key = new String[extra_indexes.length];
            String[] current_key = new String[extra_indexes.length];

            while ((s = br.readLine()) != null) {
                if (progress % 100000 == 0) {
                    System.out.print("\rlines read: " + progress);
                }

                //for field indexes
                sa = s.split(",");
                progress++;
                //updated = false;
                if (sa.length >= countOfIndexed && sa.length > idColumn) {

                    //conceptid vs names
                    for (i = 0; i < idnamesIdx.length; i += 2) {
                        //valid id's only
                        if (sa[idnamesIdx[i]].length() > 0) {
                            //append 2 spaces for ordering
                            String joined = sa[idnamesIdx[i + 1]].toLowerCase() + "  |  " + sa[idnamesIdx[i]].toLowerCase();
                            idnames.put(joined, sa[idnamesIdx[i]].toLowerCase());
                        }
                    }

                    //add current record to extra_indexes
                    for (i = 0; i < extra_indexes.length; i++) {
                        //only non-empty values
                        if (sa[ofu.extraIndexes[i]].length() > 0) {
                            current_key[i] = sa[ofu.extraIndexes[i]];
                            if (current_key[i].equals(prev_key[i]) && obj[i] != null) {
                                obj[i].add(new Integer(recordpos));
                            } else {
                                prev_key[i] = current_key[i];

                                obj[i] = (ArrayList<Integer>) extra_indexes[i].get(current_key[i]);

                                if (obj[i] == null) {
                                    obj[i] = new ArrayList<Integer>();
                                    extra_indexes[i].put(current_key[i], obj[i]);
                                }
                                obj[i].add(new Integer(recordpos));
                            }
                        }
                    }

                    for (i = 0; i < countOfIndexed; i++) {
                        if (recordpos != 0 && !last_value[i].equalsIgnoreCase(sa[i])) {
                            fw_maps[i].put(last_value[i],
                                    new IndexedRecord(last_value[i].toLowerCase(),
                                    last_record[i],
                                    recordpos - 1, (byte) i));

                            last_position[i] = filepos;
                            last_record[i] = recordpos;

                            last_value[i] = sa[i];
                        } else if (recordpos == 0) {
                            //need to keep first string
                            last_value[i] = sa[i];
                        }
                    }
                }

                //for clustering records
                if (sa[TabulationSettings.geojson_id] != null) {
                    long l = 0;
                    try {
                        l = Long.parseLong(sa[TabulationSettings.geojson_id]);
                    } catch (Exception e) {
                    }
                    fw.writeLong(l);

                    for (int j = 0; j < TabulationSettings.geojson_property_names.length; j++) {
                        if (sa[TabulationSettings.geojson_property_fields[j]] == null) {
                            attributes[j].append("").append("\n");
                        } else {
                            attributes[j].append(sa[TabulationSettings.geojson_property_fields[j]]).append("\n");
                        }
                    }
                }

                //for sensitive coordinates
                search_for[0] = sa[idColumn];
                pos = java.util.Collections.binarySearch(vsensitive_coordinates,
                        search_for,
                        new Comparator<String[]>() {

                            public int compare(String[] c1, String[] c2) {
                                return c1[0].compareTo(c2[0]);
                            }
                        });
                if (pos >= 0 && pos < vsensitive_coordinates.size()) {
                    try {
                        sensitiveCoordinates[recordpos][0] = Double.parseDouble(
                                vsensitive_coordinates.get(pos)[0]);
                        sensitiveCoordinates[recordpos][1] = Double.parseDouble(
                                vsensitive_coordinates.get(pos)[1]);
                    } catch (Exception e) {
                    }
                }

                //for sorted line starts
                sortedLineStarts[recordpos] = filepos;

                recordpos++;
                filepos += s.length() + 1; 	//+1 for '\n'
            }

            //for clustering
            fw.close();
            for (i = 0; i < attributes.length; i++) {
                attributes[i].close();
            }

            //export sorted line starts
            try {
                FileOutputStream fos = new FileOutputStream(
                        index_path
                        + SORTED_LINE_STARTS);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(sortedLineStarts);
                oos.close();

                sortedLineStarts = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            //export sensitive coordinates
            try {
                RandomAccessFile points = new RandomAccessFile(
                        index_path
                        + SENSITIVE_COORDINATES
                        + "_raf.dat",
                        "rw");
                byte[] b = new byte[sensitiveCoordinates.length * 8 * 2];
                ByteBuffer bb = ByteBuffer.wrap(b);
                for (i = 0; i < sensitiveCoordinates.length; i++) {
                    bb.putDouble(sensitiveCoordinates[i][0]);
                    bb.putDouble(sensitiveCoordinates[i][1]);
                }
                points.write(b);
                points.close();

                sensitiveCoordinates = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            //for extra indexes
            for (i = 0; i < countOfIndexed; i++) {
                fw_maps[i].put(last_value[i],
                        new IndexedRecord(last_value[i].toLowerCase(),
                        last_record[i],
                        recordpos - 1, (byte) i));
            }

            br.close();
        } catch (Exception e) {
            SpatialLogger.log("exportFieldIndexes, read", e.toString());
            e.printStackTrace();
        }

        System.out.println("done read of indexes");

        /* write out extra_indexes */
        //transform ArrayList<Integer> to int[]
        for (i = 0; i < extra_indexes.length; i++) {
            int sz = 0;
            System.out.println("lookup name: " + TabulationSettings.occurrences_csv_fields_lookups[i]);

            for (Entry<String, Object> e : extra_indexes[i].entrySet()) {
                ArrayList<Integer> al = (ArrayList<Integer>) e.getValue();
                int[] new_obj = new int[al.size()];
                int z = 0;
                for (Integer q : al) {
                    new_obj[z++] = q.intValue();
                }
                if (sz < 10) {
                    System.out.println(e.getKey() + " > " + al.size());

                }
                sz++;
                e.setValue(new_obj);
            }
            System.out.println("total: " + sz);
        }
        try {
            FileOutputStream fos = new FileOutputStream(
                    index_path
                    + ID_LOOKUP);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(extra_indexes);
            oos.close();

            extra_indexes = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* write as array objects for faster reads later*/
        try {
            for (i = 0; i < fw_maps.length; i++) {
                Set<Map.Entry<String, IndexedRecord>> set = fw_maps[i].entrySet();
                Iterator<Map.Entry<String, IndexedRecord>> iset = set.iterator();

                IndexedRecord[] ir = new IndexedRecord[set.size()];

                int j = 0;
                while (iset.hasNext()) {
                    /* export the records */
                    ir[j++] = iset.next().getValue();
                }

                String filename = index_path
                        + OTHER_IDX_PREFIX + columns[ofu.onestwos[i]] + OTHER_IDX_POSTFIX;

                /* rename the species file, (last indexed column = species column) */
                if (i == countOfIndexed - 1) {
                    filename = index_path
                            + SPECIES_IDX_FILENAME;
                }

                FileOutputStream fos = new FileOutputStream(filename);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(ir);
                oos.close();

                /* also write as csv for debugging */
                FileWriter fw = new FileWriter(filename + ".csv");
                for (IndexedRecord r : ir) {
                    fw.append(r.name);
                    fw.append(",");
                    fw.append(String.valueOf(r.record_start));
                    fw.append(",");
                    fw.append(String.valueOf(r.record_end));
                    fw.append("\r\n");
                }
                fw.close();
            }
            fw_maps = null;
            SpatialLogger.log("exportFieldIndexes done");
        } catch (Exception e) {
            SpatialLogger.log("exportFieldIndexes, write", e.toString());
        }

        //this will fail on the occurrences_csv_fields_pairs... loads
        try {
            //System.out.println("!!!!!!!!!!!Exceptions expected here!!!!!!!!!!!!!!");
            //loadIndexes();
            loadSingleIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //write out conceptid vs names
            occurrences_csv_field_pairs_ConceptId = new String[idnames.size()];
            occurrences_csv_field_pairs_Name = new String[idnames.size()];
            i = 0;
            for (Entry<String, String> et : idnames.entrySet()) {
                occurrences_csv_field_pairs_ConceptId[i] = et.getValue();
                occurrences_csv_field_pairs_Name[i] = et.getKey().substring(0, et.getKey().indexOf('|')).trim(); //first value is name
                i++;
            }
            //load single_index
            occurrences_csv_field_pairs_ToSingleIndex = new int[occurrences_csv_field_pairs_ConceptId.length];
            IndexedRecord lookfor = new IndexedRecord("", 0, 0, (byte) -1);
            for (i = 0; i < occurrences_csv_field_pairs_ConceptId.length; i++) {
                lookfor.name = occurrences_csv_field_pairs_ConceptId[i];
                occurrences_csv_field_pairs_ToSingleIndex[i] = java.util.Arrays.binarySearch(single_index,
                        lookfor,
                        new Comparator<IndexedRecord>() {

                            public int compare(IndexedRecord r1, IndexedRecord r2) {
                                return r1.name.compareTo(r2.name);
                            }
                        });
                if (occurrences_csv_field_pairs_ToSingleIndex[i] < 0) {
                    System.out.println("ERROR2: " + occurrences_csv_field_pairs_ConceptId[i] + " : " + occurrences_csv_field_pairs_Name[i]);
                } else {
                    //duplicates exist in single_index
                    while (i > 0 && single_index[occurrences_csv_field_pairs_ToSingleIndex[i - 1]].equals(occurrences_csv_field_pairs_ConceptId[i])) {
                        i--;
                    }
                }
            }
            occurrences_csv_field_pairs_FirstFromSingleIndex = new int[single_index.length];
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            for (i = 0; i < occurrences_csv_field_pairs_ConceptId.length; i++) {
                hm.put(occurrences_csv_field_pairs_ConceptId[i], i);
            }
            for (i = 0; i < single_index.length; i++) {
                Integer in = hm.get(single_index[i].name);
                if (in == null) {
                    System.out.println("ERROR3: " + single_index[i].name);
                    occurrences_csv_field_pairs_FirstFromSingleIndex[i] = -1;
                } else {
                    occurrences_csv_field_pairs_FirstFromSingleIndex[i] = in.intValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream fos = new FileOutputStream(
                    index_path
                    + SORTED_CONCEPTID_NAMES);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this.occurrences_csv_field_pairs_ConceptId);
            oos.writeObject(this.occurrences_csv_field_pairs_Name);
            oos.writeObject(this.occurrences_csv_field_pairs_FirstFromSingleIndex);
            oos.writeObject(this.occurrences_csv_field_pairs_ToSingleIndex);
            oos.close();
            FileWriter fw = new FileWriter(
                    index_path
                    + SORTED_CONCEPTID_NAMES + "0.csv");
            for (i = 0; i < occurrences_csv_field_pairs_ConceptId.length; i++) {
                fw.append(occurrences_csv_field_pairs_ConceptId[i]).append("\r\n");
            }
            fw.close();
            fw = new FileWriter(
                    index_path
                    + SORTED_CONCEPTID_NAMES + "1.csv");
            for (i = 0; i < occurrences_csv_field_pairs_Name.length; i++) {
                fw.append(occurrences_csv_field_pairs_Name[i]).append("\r\n");
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        fw_maps = null;

        System.gc();

        return recordpos;
    }

    void convertPropertyFieldCSVsToDATs(int numberOfRecords) {
        //convert attribute column txt files into arrays, then save
        for (int i = 0; i < TabulationSettings.geojson_property_names.length; i++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(index_path
                        + TabulationSettings.geojson_property_names[i] + ".txt"));
                String s;

                int j = 0;

                Object array = null;

                switch (TabulationSettings.geojson_property_types[i]) {
                    case 0: //double
                        double[] d = new double[numberOfRecords];
                        while ((s = br.readLine()) != null) {
                            d[j] = ValueCorrection.correctToDouble(TabulationSettings.geojson_property_units[i], TabulationSettings.geojson_property_catagory[i], s);
                            j++;
                        }
                        array = d;
                        break;
                    case 1: //int
                        int[] ia = new int[numberOfRecords];
                        while ((s = br.readLine()) != null) {
                            ia[j] = ValueCorrection.correctToInt(TabulationSettings.geojson_property_units[i], TabulationSettings.geojson_property_catagory[i], s);
                            j++;
                        }
                        array = ia;
                        break;
                    case 2: //boolean
                        boolean[] ib = new boolean[numberOfRecords];
                        while ((s = br.readLine()) != null) {
                            try {
                                if (s.length() == 0 || s.equals("0") || s.equalsIgnoreCase("n")
                                        || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("no")) {
                                    ib[j] = false;
                                } else {
                                    ib[j] = true;
                                }
                            } catch (Exception e) {
                            }
                            j++;
                        }
                        array = ib;
                        break;
                    case 3: //string
                        String[] is = new String[numberOfRecords];
                        while ((s = br.readLine()) != null) {
                            is[j] = s;
                            j++;
                        }
                        array = is;
                        break;
                }

                SpeciesColourOption.saveData(index_path, i, array);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * returns a list of (species names / type / count) for valid
     * .equalsIgnoreCase matches
     *
     * if input is like "species_name / type" match lookup column name
     * with 'type', e.g. "genus" or "family" or "species".
     *
     * @param filter begins with text to search for (LSID)
     * @return species matches as IndexedRecord[]
     */
    public IndexedRecord filterSpeciesRecords(String filter) {
        filter = filter.toLowerCase();

        loadIndexes();

        IndexedRecord searchfor = new IndexedRecord(filter, 0, 0, (byte) 0);
        int pos = java.util.Arrays.binarySearch(single_index, searchfor,
                new Comparator<IndexedRecord>() {

                    public int compare(IndexedRecord r1, IndexedRecord r2) {
                        return r1.name.compareTo(r2.name);
                    }
                });

        if (pos >= 0) {
            return single_index[pos];
        }

        return null;
    }

    public void loadSpeciesNumberInRecordsOrderOnly() {
        int i;
        double[][] points = getPointsPairs();
        try {
            speciesNumberInRecordsOrder = new int[points.length];
            for (i = 0; i < points.length; i++) {
                speciesNumberInRecordsOrder[i] = -1;
            }
            int countFindError = 0;
            for (int k = 0; k < all_indexes.size(); k++) {
                IndexedRecord[] species = all_indexes.get(k); //(IndexedRecord[]) ois.readObject();

                for (i = 0; i < species.length; i++) {
                    //identify the corresponding entry in single_index
                    int pos = java.util.Arrays.binarySearch(single_index, species[i],
                            new Comparator<IndexedRecord>() {

                                public int compare(IndexedRecord r1, IndexedRecord r2) {
                                    return r1.name.compareTo(r2.name);
                                }
                            });

                    //everything is in single_index, better check
                    if (pos < 0) {
                        countFindError++;
                    }

                    for (int j = species[i].record_start; j <= species[i].record_end; j++) {
                        speciesNumberInRecordsOrder[j] = pos;
                    }
                }
            }
            //error checking, likely hierarchy problems
            int countmissing = 0;
            for (i = 0; i < points.length; i++) {
                if (speciesNumberInRecordsOrder[i] == -1) {
                    countmissing++;
                }
            }
            //System.out.println("******* find error: " + countFindError);
            System.out.println("******* missing: " + countmissing);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * loads all OccurrencesIndex files for quicker response times
     *
     * excludes points indexes
     */
    public void loadIndexes() {
        // System.gc();
        TabulationSettings.load();

        if (single_index != null) {
            return;
        }

        getPointsPairs();

        loadSortedLineStarts();

        OccurrencesFieldsUtil.load();

        loadSingleIndex();

        //build speciesNumberInRecordsOrder
        getPointsPairsGrid(); //load up gridded points
        loadSpeciesNumberInRecordsOrderOnly();

        loadIdLookup();

        loadSensitiveCoordinates();

        loadClusterRecords();

        makeSpeciesSortByRecordNumber();

        System.out.println("INDEXES LOADED");

    }

    /**
     * loads id lookup and conceptid vs name
     */
    void loadIdLookup() {
        try {
            FileInputStream fis = new FileInputStream(
                    index_path
                    + ID_LOOKUP);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            extra_indexes = (HashMap<String, Object>[]) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            FileInputStream fis = new FileInputStream(
                    index_path
                    + SORTED_CONCEPTID_NAMES);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            occurrences_csv_field_pairs_ConceptId = (String[]) ois.readObject();
            occurrences_csv_field_pairs_Name = (String[]) ois.readObject();
            occurrences_csv_field_pairs_FirstFromSingleIndex = (int[]) ois.readObject();
            occurrences_csv_field_pairs_ToSingleIndex = (int[]) ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * list available extra_indexes
     */
    String[] listLookups() {
        OccurrencesFieldsUtil ofu = new OccurrencesFieldsUtil();
        ofu.load();

        String[] list = new String[TabulationSettings.occurrences_csv_fields_lookups.length
                + ofu.twoCount];

        int pos = 0;
        int i;

        for (i = 0; i < TabulationSettings.occurrences_csv_fields_lookups.length; i++) {
            list[pos++] = TabulationSettings.occurrences_csv_fields_lookups[i];
        }

        for (i = 0; i < ofu.twoCount; i++) {
            list[pos++] = ofu.columnNames[ofu.twos[i]];
        }

        return list;
    }

    /**
     * return int[] of records for listLookup match
     *
     * @param lookup_idx index to lookup as int
     * @param key index value to lookup as String
     * @return records found as int [] or null for none
     */
    public int[] lookup(int lookup_idx, String key) {
        loadIndexes();
        if (lookup_idx < extra_indexes.length) {
            return (int[]) extra_indexes[lookup_idx].get(key);
        } else {
            return lookupRegular(key);
        }
    }

    /**
     * return int[] of records for listLookup match
     *
     * @param lookupName name of index to lookup as string
     * @param key index value to lookup as String
     * @return records found as int [] or null for none
     */
    public int[] lookup(String lookupName, String key) {
        loadIndexes();
        String[] lookups = listLookups();
        for (int i = 0; i < lookups.length; i++) {
            if (lookupName.equalsIgnoreCase(lookups[i])) {
                return (int[]) extra_indexes[i].get(key);
            }
        }

        return lookupRegular(key);
    }

    /**
     * return int [] of records for indexed match on key
     * @param key lookup value as String, e.g. species name
     * @return records found as int [] or null for none
     */
    int[] lookupRegular(String key) {
        //check against regular index
        IndexedRecord ir = filterSpeciesRecords(key);
        if (ir != null) {
            int[] output = new int[ir.record_end - ir.record_start + 1];
            for (int i = 0; i < output.length; i++) {
                output[i] = i + ir.record_start;
            }
            return output;
        }

        return null;
    }

    void loadSortedLineStarts() {
        if (sortedLineStarts == null) {
            try {
                FileInputStream fis = new FileInputStream(
                        index_path
                        + SORTED_LINE_STARTS);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
                sortedLineStarts = (long[]) ois.readObject();
                ois.close();
            } catch (Exception e) {
                SpatialLogger.log("getSortedRecords", e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * gets sorted records strings for input of sorted record indexes
     *
     * TODO: make use of class variable speciesSortByRecordNumber
     * @param records array of record indexes to return as int []
     * @return sorted records as String []
     */
    public String[] getSortedRecords(int[] records) {
        int i;

        /* enforce sort */
        for (i = 1; i < records.length; i++) {
            if (records[i - 1] > records[i]) {
                java.util.Arrays.sort(records);
                break;
            }
        }

        String[] lines = new String[records.length];

        long start = System.currentTimeMillis();

        /* iterate through sorted records file and extract only required records */
        try {
            //max line len 100000 characters
            byte[] data = new byte[(int) (100000)];
            FileInputStream fis = new FileInputStream(
                    index_path
                    + SORTED_FILENAME);

            long filepos = 0;
            int idxpos = 0;

            //skip to record, update filepos, read line, increment
            do {
                fis.skip(sortedLineStarts[records[idxpos]] - filepos);
                filepos = sortedLineStarts[records[idxpos] + 1];
                int len = (int) (sortedLineStarts[records[idxpos] + 1] - sortedLineStarts[records[idxpos]]);
                fis.read(data, 0, len);
                lines[idxpos] = (new String(Arrays.copyOfRange(data, 0, len))).trim();
                idxpos++;
            } while (idxpos < lines.length && idxpos < records.length
                    && lines[idxpos - 1] != null);

            fis.close();

            System.out.println("getSortedRecords: count=" + lines.length + " in " + (System.currentTimeMillis() - start) + "ms");

            return lines;
        } catch (Exception e) {
            SpatialLogger.log("getSortedRecords", e.toString());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * gets all points corresponding to sorted records
     *
     * @return all points as double[n][2]
     * where
     *  n is number of points
     *  [][0] is longitude
     *  [][1] is latitude
     */
    public double[][] getPointsPairs() {
        /* if already loaded, return existing data */
        if (all_points == null) {
            long start = System.currentTimeMillis();

            /* store for next time */
            all_points = getPointsPairs(-1);

            System.out.println("getpointspairs: " + (System.currentTimeMillis() - start) + "ms");
        }

        return all_points;
    }

    /**
     * for a PART
     *
     * gets all points corresponding to sorted records
     *
     * @param partNumber part to open as int
     * @return all points as double[n][2]
     * where
     *  n is number of points
     *  [][0] is longitude
     *  [][1] is latitude
     */
    public double[][] getPointsPairs(int partNumber) {
        double[][] d = null;

        try {
            /* load all points */
            RandomAccessFile points;
            if (partNumber >= 0) {
                points = new RandomAccessFile(
                        index_path + POINTS_FILENAME + "_" + partNumber,
                        "r");
            } else {
                points = new RandomAccessFile(
                        index_path + POINTS_FILENAME,
                        "r");
            }
            int number_of_points = ((int) points.length()) / 8;
            int number_of_records = number_of_points / 2;
            byte[] b = new byte[number_of_points * 8];
            points.read(b);
            ByteBuffer bb = ByteBuffer.wrap(b);
            points.close();

            int i;

            /* read doubles into data structure*/
            d = new double[number_of_records][2];
            for (i = 0; i < number_of_records; i++) {
                d[i][0] = bb.getDouble();
                d[i][1] = bb.getDouble();
            }
        } catch (Exception e) {
            SpatialLogger.log("getPointsPairs", e.toString());
        }

        return d;
    }

    /**
     * operates on previously OCC_POINTS.dat
     *
     * exports points in sorted on latitude then longitude
     * of 0.5 degree grid cells
     *
     * exports as POINTS_FILENAME_GEO for points data
     * exports as POINTS_FILENAME_GEO_IDX for sorted records indexes
     */
    void exportSortedGridPoints() {
        /* load OCC_POINTS.dat */
        double[][] points = getPointsPairs();

        /* load Points object */
        Point[] pa = new Point[points.length];
        int i;
        double longitude, latitude;

        for (i = 0; i < points.length; i++) {
            longitude = points[i][0];
            latitude = points[i][1];

            pa[i] = new Point(longitude, latitude, i);
        }

        /* sort on 0.5degree grid by latitude then longitude */
        java.util.Arrays.sort(pa,
                new Comparator<Point>() {

                    public int compare(Point r1, Point r2) {
                        double result = (int) (4 * r1.latitude + 360) - (int) (4 * r2.latitude + 360);
                        if (result == 0) {
                            result = (int) (2 * r1.longitude + 360) - (int) (2 * r2.longitude + 360);
                        }
                        return (int) result;
                    }
                });

        //export lookup for idx/record reference
        try {
            RandomAccessFile raf = new RandomAccessFile(
                    index_path + POINTS_FILENAME_05GRID_IDX,
                    "rw");
            byte[] b = new byte[pa.length * 4];
            ByteBuffer bb = ByteBuffer.wrap(b);

            /* forward */
            b = new byte[pa.length * 4];
            bb = ByteBuffer.wrap(b);
            for (i = 0; i < pa.length; i++) {
                bb.putInt(pa[i].idx);
            }
            raf.write(b);

            raf.close();
        } catch (Exception e) {
            SpatialLogger.log("exportSortedGridPoints", e.toString());
        }

        //export lookup for idx/record reference

        /* 720 constant is 360 EW & 180 NS degrees/0.5/.25 degree grid */

        try {
            RandomAccessFile raf = new RandomAccessFile(
                    index_path + POINTS_FILENAME_05GRID_KEY,
                    "rw");
            byte[] b = new byte[(360 * 2 * 360 * 2) * 4];
            ByteBuffer bb = ByteBuffer.wrap(b);

            /* fill grid cells positions/key */
            int[][] list = new int[720][720];
            int j;
            for (i = 0; i < 720; i++) {
                for (j = 0; j < 720; j++) {
                    list[i][j] = -1;
                }
            }
            int lastx = 0;
            int lasty = 0;
            for (i = 0; i < pa.length; i++) {
                int x = (int) (pa[i].longitude * 2 + 360);          //longitude is -180 to 179.999...
                int y = (int) (pa[i].latitude * 4 + 360);		//latitude is -90 to 89.999...

                if (list[y][x] == -1
                        || list[y][x] > i) {
                    list[y][x] = i;
                }

                if (y < lasty || (y == lasty && x < lastx)) {
                    System.out.print("," + x + " " + y);

                }
                lastx = x;
                lasty = y;
            }

            /* populate blanks and validate order */
            int last_cell = pa.length;
            for (i = (720 - 1); i >= 0; i--) {
                for (j = (720 - 1); j >= 0; j--) {
                    if (list[i][j] == -1) {
                        list[i][j] = last_cell;
                    } else if (last_cell < list[i][j]) {
                        SpatialLogger.log("exportSortedGridPoints, order err");
                    }
                    last_cell = list[i][j];
                }
            }

            /* write */
            for (i = 0; i < 720; i++) {
                for (j = 0; j < 720; j++) {
                    bb.putInt(list[i][j]);
                }
            }

            raf.write(b);
            raf.close();
        } catch (Exception e) {
            SpatialLogger.log("exportSortedGridPoints", e.toString());
        }
    }

    /**
     * gets all points corresponding to sorted records
     * on 0.5 grid using latitude then longitude sort order
     *
     * @return all points as double[n][2]
     * where
     *  n is number of points
     *  [][0] is longitude
     *  [][1] is latitude
     */
    public void getPointsPairsGrid() {
        /* return existing load if available */
        if (grid_points_idx != null) {
            return;
        }

        //load idx as well
        getPointsPairsGrididx();
        getPointsPairsGridKey();
    }

    /**
     * gets index for all points corresponding to sorted records
     * on 0.5 grid using latitude then longitude sort order
     *
     * @return records index of latitude then longitude sorted points
     * as records index positions against this method sorted points
     */
    public int[] getPointsPairsGrididx() {
        /* return existing load if available */
        if (grid_points_idx != null) {
            return grid_points_idx;
        }

        //int[] d1 = null;
        int[] d2 = null;
        int i;
        try {
            /* load all */
            RandomAccessFile points = new RandomAccessFile(
                    index_path + POINTS_FILENAME_05GRID_IDX,
                    "r");
            int number_of_points = ((int) points.length()) / 4;
            int number_of_records = number_of_points;
            byte[] b = new byte[number_of_points * 4];

            /* read reverse order idx as int */
            points.read(b);
            ByteBuffer bb = ByteBuffer.wrap(b);

            /* read forward order idx as int */
            d2 = new int[number_of_records];
            for (i = 0; i < number_of_records; i++) {
                d2[i] = bb.getInt();
            }

            System.out.println("num records in key:" + number_of_records);

            points.close();
        } catch (Exception e) {
            SpatialLogger.log("getPointsPairsGrididx", e.toString());
        }

        //grid_points_idx_rev = d1;
        grid_points_idx = d2;
        return d2;
    }

    /**
     * gets key of all points corresponding to sorted records
     * on 0.5 grid using latitude then longitude sort order
     *
     * @return records key grid of latitude then longitude sorted order
     * as method sorted points index for each cell
     */
    public int[][] getPointsPairsGridKey() {
        /* use previously loaded if available */
        if (grid_key != null) {
            return grid_key;
        }

        int[][] d = null;
        int i;

        try {
            /* load all */
            RandomAccessFile points = new RandomAccessFile(
                    index_path + POINTS_FILENAME_05GRID_KEY,
                    "r");
            int number_of_points = ((int) points.length()) / 4;
            byte[] b = new byte[number_of_points * 4];
            points.read(b);
            ByteBuffer bb = ByteBuffer.wrap(b);

            /* read as int into grid */
            d = new int[720][720];
            int j;
            for (i = 0; i < 720; i++) {
                for (j = 0; j < 720; j++) {
                    d[i][j] = bb.getInt();
                }
            }
            points.close();

        } catch (Exception e) {
            SpatialLogger.log("getPointsPairsGridKey", e.toString());
        }

        grid_key = d;
        return d;
    }

    void makeSpeciesSortByRecordNumber() {
        if (speciesSortByRecordNumber == null) {

            speciesSortByRecordNumber = single_index.clone();

            //preserve original order in a secondary list, to be returned
            int[] tmp = new int[speciesSortByRecordNumber.length];
            int i;
            for (i = 0; i < speciesSortByRecordNumber.length; i++) {
                tmp[i] = speciesSortByRecordNumber[i].record_end;
                speciesSortByRecordNumber[i].record_end = i;
            }

            java.util.Arrays.sort(speciesSortByRecordNumber,
                    new Comparator<IndexedRecord>() {

                        public int compare(IndexedRecord r1, IndexedRecord r2) {
                            if (r1.record_start == r2.record_start) {
                                return r1.type - r2.type;
                            }
                            return r1.record_start - r2.record_start;
                        }
                    });

            //return value to borrowed variable
            int t;
            speciesSortByRecordNumberOrder = new int[speciesSortByRecordNumber.length];
            for (i = 0; i < speciesSortByRecordNumber.length; i++) {
                t = speciesSortByRecordNumber[i].record_end;
                speciesSortByRecordNumber[i].record_end = tmp[t];

                speciesSortByRecordNumberOrder[i] = t;
            }
        }
    }

    void loadClusterRecords() {
        if (occurrenceId != null) {
            return;
        }

        //occurrence id's
        try {
            RandomAccessFile raf = new RandomAccessFile(index_path
                    + "OCCURRENCE_ID.dat", "r");
            int records = (int) raf.length() / 8; //long's
            byte[] b = new byte[(int) raf.length()];
            raf.read(b);
            raf.close();
            ByteBuffer bb = ByteBuffer.wrap(b);
            occurrenceId = new long[records];
            for (int i = 0; i < records; i++) {
                occurrenceId[i] = bb.getLong();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (attributesMap == null) {
            attributesMap = new HashMap<String, Object>();
        }

        //load attributes
        for (int j = 0; j < TabulationSettings.geojson_property_names.length; j++) {
            try {
                Object data = SpeciesColourOption.loadData(index_path, j);
                attributesMap.put(TabulationSettings.geojson_property_names[j], data);

                //lookup uid to name, instituion and dataResource
                if(TabulationSettings.institution_property == TabulationSettings.geojson_property_fields[j]) {
                    Object [] d = (Object []) data;
                    String [] s = (String []) d[0];
                    for(int k=0;k<s.length;k++) {
                        String value = TabulationSettings.institutions.get(s[k]);
                        if(value != null) {
                            s[k] = value.replace(",","");
                        } else {
                            s[k] = "";
                        }
                    }
                }
                if(TabulationSettings.dataresource_property == TabulationSettings.geojson_property_fields[j]) {
                    Object [] d = (Object []) data;
                    String [] s = (String []) d[0];
                    for(int k=0;k<s.length;k++) {
                        String value = TabulationSettings.dataResources.get(s[k]);
                        if(value != null) {
                            s[k] = value.replace(",","");
                        } else {
                            s[k] = "";
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * gets parts/positions in occurrences file for processing
     *
     * String[0] is part number.
     *  implies a seek to Long.parseLong(String[0])*PART_SIZE_MAX
     * String[1] is the starting string
     *  Unless String[0] == "0";
     *  After seek, read lines.  The first line == String[1] is the start.
     * String[2] is the ending string
     *  when reading, terminate when at end of file or 'line read' == String[2]
     * @return
     */
    LinkedBlockingQueue<String[]> getPartsPositions() {
        LinkedBlockingQueue<String[]> lbq = new LinkedBlockingQueue<String[]>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(occurrences_csv));
            long part = 0;

            //determine end
            String[] next;
            String line = null;
            while ((br.skip(OccurrencesIndex.PART_SIZE_MAX)) > 0 || part == 0) {
                System.out.println("skipped into part: " + part);

                //apply limit for dev
                if (part * OccurrencesIndex.PART_SIZE_MAX > TabulationSettings.occurrences_csv_max_records && TabulationSettings.occurrences_csv_max_records > 0) {
                    break;
                }

                next = new String[3];
                next[0] = String.valueOf(part);
                part++;

                next[1] = line; //previous part's end line

                br.mark(2000000);//max size of 2 lines

                line = br.readLine();  //skip this line, might be partial

                //check if this is a continuation line
                while (line != null && line.length() > 0 && line.charAt(line.length() - 1) == '\\') {
                    String spart = br.readLine();
                    if (spart == null) {  //same as whole line is null
                        break;
                    } else {
                        line.replace('\\', ' ');   //new line is same as 'space'
                        line += spart;
                    }
                }//repeat as necessary

                line = br.readLine(); //this is the end line
                while (line != null && line.length() > 0 && line.charAt(line.length() - 1) == '\\') {
                    String spart = br.readLine();
                    if (spart == null) {  //same as whole line is null
                        break;
                    } else {
                        line.replace('\\', ' ');   //new line is same as 'space'
                        line += spart;
                    }
                }//repeat as necessary

                br.reset();
                next[2] = line; //record end line

                lbq.put(next);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lbq;
    }
    int[] sensitive_column_positions;

    /**
     * read header of occurrences file and setup for column positions
     */
    void initColumnPositions() {
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader(occurrences_csv));

            String s = br.readLine();

            //check for continuation line
            while (s != null && s.length() > 0 && s.charAt(s.length() - 1) == '\\') {
                String spart = br.readLine();
                if (spart == null) {  //same as whole line is null
                    break;

                } else {
                    s.replace('\\', ' ');   //new line is same as 'space'
                    s += spart;
                }
            }//repeat as necessary

            String[] sa = s.split(",");

            /* remove quotes and commas form terms */
            for (int i = 0; i < sa.length; i++) {
                if (sa[i].length() > 0) {
                    sa[i] = sa[i].replace("\"", "");
                    sa[i] = sa[i].replace(",", " ");
                }
            }

            column_positions = OccurrencesFieldsUtil.getColumnPositions(sa);

            sensitive_column_positions = OccurrencesFieldsUtil.getSensitiveColumnPositions(sa);

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void makePartsThreaded() {
        SpatialLogger.log("MAKE PARTS THREADED: START");

        LinkedBlockingQueue<String[]> lbq = getPartsPositions();
        CountDownLatch cdl = new CountDownLatch(lbq.size());

        initColumnPositions();

        MakeOccurrenceParts[] threads = new MakeOccurrenceParts[TabulationSettings.analysis_threads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new MakeOccurrenceParts(lbq, cdl, column_positions, sensitive_column_positions, occurrences_csv, index_path);
            threads[i].start();
        }
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].interrupt();
            } catch (Exception e) {
            }
        }

        SpatialLogger.log("MAKE PARTS THREADED: FINISHED");
    }

    private Vector<String[]> getVSensitiveCoordinates(CountDownLatch cdl) {
        Vector<String[]> vsensitive_coordinates = new Vector<String[]>(1000000);

        SpatialLogger.log("SensitiveCoordinates: start");
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    index_path
                    + SENSITIVE_COORDINATES + ".csv"));
            String s;

            while ((s = br.readLine()) != null) {
          
