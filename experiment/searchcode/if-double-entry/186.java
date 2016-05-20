package com.pb.aquavis.moves2010a.tables;

import com.pb.aquavis.AquavisDataProvider;
import com.pb.aquavis.AquavisUtils;
import com.pb.aquavis.moves2010a.*;
import com.pb.aquavis.moves2010a.types.*;
import com.pb.sawdust.tabledata.DataRow;
import com.pb.sawdust.tabledata.DataTable;
import com.pb.sawdust.tabledata.basic.RowDataTable;
import com.pb.sawdust.tabledata.metadata.DataType;
import com.pb.sawdust.tabledata.metadata.TableSchema;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code DayVmtFractionTable} is a table builder corresponding to the MOVES2010a "hourVMTFraction" table.
 *
 * @author crf
 *         Started 3/26/12 10:38 AM
 */
public class HourVmtFractionTable extends TableBuilder {
    /**
     * The name of the table used in the MOVES2010a software.
     */
    public static final String TABLE_NAME = "hourVMTFraction";

    private final RoadClassifier roadClassifier;
    private final SourceClassifier sourceClassifier;
    private final ZoneClassifier zoneClassifier;
    private final TimeDistribution timeDistribution;
    private final Map<SourceType,Map<RoadType,Double>> weekdayToWeekendFactors;

    /**
     * Constructor specifying information and data needed to build the table.
     *
     * @param year
     *        The model run year.
     *
     * @param county
     *        The county.
     *
     * @param roadClassifier
     *        The road type classifier.
     *
     * @param sourceClassifier
     *        The source type classifier.
     *
     * @param zoneClassifier
     *        The zone classifier.
     *
     * @param timeDistribution
     *        The time distribution specification.
     *
     * @param vmtTransformer
     *        The VMT transformer.
     */
    public HourVmtFractionTable(int year, String county, RoadClassifier roadClassifier, SourceClassifier sourceClassifier, ZoneClassifier zoneClassifier, TimeDistribution timeDistribution, VmtTransformer vmtTransformer) {
        super(year,county);
        this.roadClassifier = roadClassifier;
        this.sourceClassifier = sourceClassifier;
        this.zoneClassifier = zoneClassifier;
        this.timeDistribution = timeDistribution;

        weekdayToWeekendFactors = new HashMap<>();
        int monthCount = Month.values().length;
        for (SourceType sourceType : SourceType.values()) {
            Map<RoadType,Double> roadTypeMap = new HashMap<>();
            for (RoadType roadType : RoadType.values()) {
                double factor = 0.0;
                for (Month month : Month.values()) {
                    Map<DayOfTheWeek,Double> vmtFactors = vmtTransformer.getDayVmtFractions(sourceType,month,roadType);
                    factor += vmtFactors.get(DayOfTheWeek.WEEKEND) / vmtFactors.get(DayOfTheWeek.WEEKDAY) * (5.0/2.0) / monthCount;
                }
                roadTypeMap.put(roadType,factor);
            }
            weekdayToWeekendFactors.put(sourceType,roadTypeMap);
        }
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public DataTable buildTable(AquavisDataProvider dataProvider) {
        DataTable network = dataProvider.getAquavisNetwork();

        TableSchema schema = new TableSchema(getTableName(),
                                              new String[] {"sourceTypeID","roadTypeID","dayID","hourID","hourVMTFraction"},
                                              new DataType[] {DataType.INT,DataType.INT,DataType.INT,DataType.INT,DataType.DOUBLE});
        DataTable avgSpeedDistributionTable = new RowDataTable(schema);

        for (DayOfTheWeek dayOfTheWeek : DayOfTheWeek.values()) {
            Map<SourceType,Map<RoadType,Map<Hour,Double>>> fractions = new EnumMap<>(SourceType.class);
            for (SourceType sourceType : SourceType.values()) {
                Map<RoadType,Map<Hour,Double>> roadMap = new EnumMap<>(RoadType.class);
                for (RoadType roadType : RoadType.values()) {
                    Map<Hour,Double> hourMap = new EnumMap<>(Hour.class);
                    for (Hour hour : Hour.values())
                        hourMap.put(hour,0.0);
                    roadMap.put(roadType,hourMap);
                }
                fractions.put(sourceType,roadMap);
            }

            for (DataRow row : network) {
                double volume = row.getCellAsDouble(AquavisUtils.AQUAVIS_NETWORK_VOLUME_FIELD);
                Map<SourceType,Double> sourceMap = sourceClassifier.getSourceTypes(row.getCellAsString(AquavisUtils.AQUAVIS_NETWORK_VEHICLE_CLASS_FIELD));
                RoadType roadType = roadClassifier.getRoadType(row.getCellAsInt(AquavisUtils.AQUAVIS_NETWORK_LINK_CLASS_FIELD));
                double vmt = row.getCellAsDouble(AquavisUtils.AQUAVIS_NETWORK_LENGTH_FIELD)*volume;
                String skimPeriod = row.getCellAsString(AquavisUtils.AQUAVIS_NETWORK_TIME_PERIOD_FIELD);
                for (SourceType sourceType : sourceMap.keySet()) {
                    double shiftedVmt = dayOfTheWeek == DayOfTheWeek.WEEKEND ? weekdayToWeekendFactors.get(sourceType).get(roadType) * vmt : vmt;
                    Map<Hour,Double> timeSplit = timeDistribution.distribution(sourceType,skimPeriod);
                    Map<Hour,Double> hourMap = fractions.get(sourceType).get(roadType);
                    for (Hour hour : timeSplit.keySet())
                        hourMap.put(hour,hourMap.get(hour) + shiftedVmt*sourceMap.get(sourceType)*timeSplit.get(hour));
                }
            }

            //get intrazonal road type
            Map<Integer,RoadType> intrazonalRoadMap = new HashMap<>();
            Map<Integer,Double> intrazonalVmtMap = new HashMap<>();
            for (DataRow row : dataProvider.getAquavisIntrazonal()) {
                int zone = row.getCellAsInt(AquavisUtils.AQUAVIS_INTRAZONAL_ZONE_FIELD);
                intrazonalRoadMap.put(zone,zoneClassifier.isRural(row.getCellAsInt(AquavisUtils.AQUAVIS_INTRAZONAL_AREA_TYPE_FIELD)) ? RoadType.RURAL_UNRESTRICTED_ACCESS : RoadType.URBAN_UNRESTRICTED_ACCESS);
                intrazonalVmtMap.put(zone,row.getCellAsDouble(AquavisUtils.AQUAVIS_INTRAZONAL_LENGTH_FIELD));
            }

            //now use road type and put intrazonals in it
            for (DataRow row : dataProvider.getAquavisTrips()) {
                int origin = row.getCellAsInt(AquavisUtils.AQUAVIS_TRIPS_ORIGIN_ZONE_FIELD);
                int destination = row.getCellAsInt(AquavisUtils.AQUAVIS_TRIPS_DESTINATION_ZONE_FIELD);
                if (origin == destination) {
                    Double intrazonalVmt = intrazonalVmtMap.get(origin);
                    if (intrazonalVmt != null) {
                        double vmt = intrazonalVmt*row.getCellAsDouble(AquavisUtils.AQUAVIS_TRIPS_TRIPS_FIELD);
                        RoadType roadType = intrazonalRoadMap.get(origin);
                        Hour hour = timeDistribution.getHour(row.getCellAsInt(AquavisUtils.AQUAVIS_TRIPS_HOUR_FIELD)); //have hour info explicitly, so no need to split on time period field
                        Map<SourceType,Double> sourceTypeMap = sourceClassifier.getSourceTypes(row.getCellAsString(AquavisUtils.AQUAVIS_TRIPS_VEHICLE_CLASS_FIELD));
                        for (SourceType sourceType : sourceTypeMap.keySet()) {
                            double shiftedVmt = dayOfTheWeek == DayOfTheWeek.WEEKEND ? weekdayToWeekendFactors.get(sourceType).get(roadType) * vmt : vmt;
                            Map<Hour,Double> hourMap = fractions.get(sourceType).get(roadType);
                            hourMap.put(hour,hourMap.get(hour)+shiftedVmt*sourceTypeMap.get(sourceType));
                        }
                    }
                }
            }


            //sums to one for each source/road/day combination
            for (SourceType sourceType : fractions.keySet()) {
                Map<RoadType,Map<Hour,Double>> roadMap = fractions.get(sourceType);
                for (RoadType roadType : roadMap.keySet()) {
                    Map<Hour,Double> hourMap = roadMap.get(roadType);
                    double sum = 0.0;
                    for (double d : hourMap.values())
                        sum += d;
                    if (sum > 0.0 ) {
                        for (Map.Entry<Hour,Double> entry : hourMap.entrySet())
                            entry.setValue(entry.getValue() / sum);
                    } else {
                        hourMap.entrySet().iterator().next().setValue(1.0); //set first entry to 1.0 as a placeholder
                    }
                }
            }


            //"sourceTypeID","roadTypeID","dayID","hourID","hourVMTFraction"
            for (SourceType sourceType : SourceType.values())
                for (RoadType roadType : RoadType.values())
                    for (Hour hour : Hour.values())
                        avgSpeedDistributionTable.addRow(sourceType.getId(),roadType.getId(),dayOfTheWeek.getId(),hour.getId(),fractions.get(sourceType).get(roadType).get(hour));
        }
        return avgSpeedDistributionTable;
    }
}

