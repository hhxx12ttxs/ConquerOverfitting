package ru.cos.sim.visualizer.traffic.graphs.converter;

import ru.cos.sim.communication.dto.MeterDTO;
import ru.cos.sim.meters.*;
import ru.cos.sim.meters.framework.MeasuredData;
import ru.cos.sim.meters.framework.Measurement;
import ru.cos.sim.meters.framework.MeasurementHistory;
import ru.cos.sim.meters.framework.PeriodData;
import ru.cos.sim.meters.framework.ScheduledData;
import ru.cos.sim.meters.framework.TimePeriod;
import ru.cos.sim.visualizer.traffic.graphs.classes.GraphData;
import ru.cos.sim.visualizer.traffic.graphs.classes.IntervalData;
import ru.cos.sim.visualizer.traffic.graphs.classes.Point;
import ru.cos.sim.visualizer.traffic.graphs.classes.PointCollection;

import java.util.List;

public abstract class AbstractConverter<B extends PointCollection, S extends GraphData,
         MD extends MeasuredData<MD>> implements DataConverter<S , MD>{

    public float[][] getHistoryData(MeterDTO<MD> dto){
        return convertHistoryData(dto.getHistory(), 0);
    }

    public List<float[][]> getScheduledHistoryData(MeterDTO<MD> dto){
        List<float[][]> scheduled = new java.util.ArrayList<float[][]>();
        if(dto.getScheduledHistoryData() == null) return scheduled;
        for(PeriodData<MeasurementHistory<MD>> p : dto.getScheduledHistoryData().getPeriodMeasuredDataSet()){
            float[][] data = convertHistoryData(p.getMeasuredData(), 0);
            scheduled.add(data);
        }
        return scheduled;
    }

    public float[][] getScheduledHistoryDataByPeriod(MeterDTO<MD> dto, TimePeriod period){
        ScheduledData<MeasurementHistory<MD>> scheduled = dto.getScheduledHistoryData();
        if(scheduled == null) return new float[0][];
        for(PeriodData<MeasurementHistory<MD>> p : scheduled.getPeriodMeasuredDataSet()){
            TimePeriod per = p.getTimePeriod();
            if(Float.compare(per.getTimeFrom(), period.getTimeFrom()) == 0
                    && Float.compare(per.getTimeTo(), period.getTimeTo()) == 0){
                MeasurementHistory<MD> hist = p.getMeasuredData();
                return convertHistoryData(hist, 0);
            }
        }
        return new float[0][];
    }

    public List<TimePeriod> getScheduledHistoryPeriods(MeterDTO<MD> dto){
        List<TimePeriod> periods = new java.util.ArrayList<TimePeriod>();
        if(dto.getScheduledHistoryData() == null) return periods;
        for(PeriodData<MeasurementHistory<MD>> p : dto.getScheduledHistoryData().getPeriodMeasuredDataSet()){
            periods.add(p.getTimePeriod());
        }
        return periods;
    }

    private float[][] convertHistoryData(MeasurementHistory<MD> history, float timeFrom){
        if(history == null) return new float[0][];
        int historySize = history.getHistory().size();
        float[][] data = new float[historySize][];
        for(int i = 0; i < historySize; i++){
            Measurement<MD> md = history.getHistory().get(i);
            float[] datum = getValue(md.getMeasuredData());
            datum[0] = md.getMeasurementTime() + timeFrom;

            data[i] = datum;
        }
        return data;
    }

    public float[][] getInstantData(MeterDTO<MD> dto){
        return new float[0][];
    }

    public S convertHistory(MeterDTO<MD> dto){
        S graphData = getGraphDataInstance();

        convertScheduledAverage(dto, graphData);

        MeasurementHistory<MD> history = dto.getHistory();
        if(history == null) return graphData;

        PointCollection pointCollection = convertMeasurementHistory(history, 0);
        pointCollection.setDescription(dto.getName());
        addDatasetToGraphData(graphData, pointCollection);
        return graphData;
    }

    public S convertScheduledHistory(MeterDTO<MD> dto){
        S graphData = getGraphDataInstance();

        convertScheduledAverage(dto, graphData);

        ScheduledData<MeasurementHistory<MD>> scheduled = dto.getScheduledHistoryData();
        if(scheduled == null) return graphData;

        for(PeriodData<MeasurementHistory<MD>> p : scheduled.getPeriodMeasuredDataSet()){
            TimePeriod period = p.getTimePeriod();
            MeasurementHistory<MD> hist = p.getMeasuredData();
            PointCollection pointCollection = convertMeasurementHistory(hist, 0);
            pointCollection.setDescription(Float.toString(period.getTimeFrom()) + "-" + Float.toString(period.getTimeTo()));
            addDatasetToGraphData(graphData, pointCollection);
        }
        return graphData;
    }

    public S convertScheduledHistoryByPeriod(MeterDTO<MD> dto, TimePeriod period){
        S graphData = getGraphDataInstance();

        ScheduledData<MeasurementHistory<MD>> scheduled = dto.getScheduledHistoryData();
        if(scheduled == null) return graphData;
        for(PeriodData<MeasurementHistory<MD>> p : scheduled.getPeriodMeasuredDataSet()){
            TimePeriod per = p.getTimePeriod();
            if(Float.compare(per.getTimeFrom(), period.getTimeFrom()) == 0
                    && Float.compare(per.getTimeTo(), period.getTimeTo()) == 0){
                MeasurementHistory<MD> hist = p.getMeasuredData();
                PointCollection pointCollection = convertMeasurementHistory(hist, 0);
                pointCollection.setDescription(Float.toString(period.getTimeFrom()) + "-" + Float.toString(period.getTimeTo()));
                addDatasetToGraphData(graphData, pointCollection);
            }
        }
        return graphData;
    }

    public abstract float[] getValue(MD measuredData);
    protected abstract S getGraphDataInstance();
    protected abstract B getPointCollection();
    protected abstract Point convertMeasuredData(float time, MD md);
    protected abstract void addDatasetToGraphData(S graphData, PointCollection pointCollection);

    private PointCollection convertMeasurementHistory(MeasurementHistory<MD> history, float timeCorrection){
        PointCollection collection = getPointCollection();
        if(history == null) return collection;
        List<Measurement<MD>> measurements = history.getHistory();
        for(Measurement<MD> m : measurements){
            Point p = convertMeasuredData(m.getMeasurementTime() + timeCorrection, m.getMeasuredData());
            collection.getPoints().add(p);
        }
        return collection;
    }

    private void convertScheduledAverage(MeterDTO<MD> meterDTO, S graphData){
        ScheduledData<MD> scheduledData = meterDTO.getScheduledAverageData();
        if(scheduledData == null) return;

        for(PeriodData<MD> p : scheduledData.getPeriodMeasuredDataSet()){
            TimePeriod timePeriod = p.getTimePeriod();
            MD data = p.getMeasuredData();

            IntervalData intervalData = new IntervalData();
            intervalData.setPeriodStart(timePeriod.getTimeFrom());
            intervalData.setPeriodEnd(timePeriod.getTimeTo());
            intervalData.setValue(buildValueString(data));

            graphData.getIntervalCollection().add(intervalData);
        }
    }

    public String buildValueString(MD md){
        float[] data = getValue(md);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < data.length - 1; i++){
            builder.append(data[i+1]);
            if((i + 1) != (data.length - 1)) builder.append("/");
        }
        return builder.toString();
    }
}

