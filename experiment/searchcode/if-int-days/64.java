/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.smos.visat.export;

import com.bc.ceres.binio.*;
import org.esa.beam.dataio.smos.SmosConstants;
import org.esa.beam.smos.DateTimeUtils;
import org.esa.beam.smos.SmosUtils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

class EEExportGridPointHandler implements GridPointHandler {

    private final DataContext targetContext;
    private final GridPointFilter targetFilter;
    private final HashMap<Long, Date> snapshotIdTimeMap;
    private final TimeTracker timeTracker;
    private final GeometryTracker geometryTracker;
    private boolean level2;

    private long gridPointCount;
    private long gridPointDataPosition;
    private int latIndex;
    private int lonIndex;
    private static final int SEGMENT_SIZE = 16384;

    EEExportGridPointHandler(DataContext targetContext) {
        this(targetContext, new GridPointFilter() {
            @Override
            public boolean accept(int id, CompoundData gridPointData) throws IOException {
                return true;
            }
        });
    }

    EEExportGridPointHandler(DataContext targetContext, GridPointFilter targetFilter) {
        this.targetContext = targetContext;
        this.targetFilter = targetFilter;
        snapshotIdTimeMap = new HashMap<>();
        timeTracker = new TimeTracker();
        geometryTracker = new GeometryTracker();

        final String formatName = targetContext.getFormat().getName();
        level2 = SmosUtils.isL2Format(formatName);
    }

    @Override
    public void handleGridPoint(int id, CompoundData gridPointData) throws IOException {
        if (gridPointCount == 0) {
            init(gridPointData);
        }
        if (targetFilter.accept(id, gridPointData)) {
            trackSensingTime(gridPointData);
            trackGeometry(gridPointData);

            targetContext.getData().setLong(SmosConstants.GRID_POINT_COUNTER_NAME, ++gridPointCount);
            // ATTENTION: flush must occur <em>before</em> grid point data is written (rq-20091008)
            targetContext.getData().flush();

            gridPointData.resolveSize();
            final long size = gridPointData.getSize();
            final byte[] bytes = new byte[(int) size];

            get(gridPointData, bytes);
            put(targetContext, bytes, gridPointDataPosition);
            gridPointDataPosition += size;
        }
    }

    boolean hasValidPeriod() {
        return timeTracker.hasValidPeriod();
    }

    Date getSensingStart() {
        return timeTracker.getIntervalStart();
    }

    Date getSensingStop() {
        return timeTracker.getIntervalStop();
    }

    boolean hasValidArea() {
        return geometryTracker.hasValidArea();
    }

    Rectangle2D getArea() {
        return geometryTracker.getArea();
    }

    long getGridPointCount() {
        return gridPointCount;
    }

    static Date getL2MjdTimeStamp(CompoundData compoundData) throws IOException {
        int index = compoundData.getType().getMemberIndex("Mean_Acq_Time");
        if (index > 0) {
            return getOSUDPDate(compoundData, index);
        }
        final CompoundType type = compoundData.getType();
        index = type.getMemberIndex("Days");
        if (index >= 0) {
            return getSMUPDDate(compoundData);
        }

        return null;
    }

    private static Date getSMUPDDate(CompoundData compoundData) throws IOException {
        final int days = compoundData.getInt("Days");
        final long seconds = compoundData.getUInt("Seconds");
        final long microseconds = compoundData.getUInt("Microseconds");

        if ((days + seconds + microseconds) == 0) {
            return null;
        }

        return DateTimeUtils.cfiDateToUtc(days, seconds, microseconds);
    }

    private static Date getOSUDPDate(CompoundData compoundData, int index) throws IOException {
        final float floatDate = compoundData.getFloat(index);
        if (floatDate > 0.f) {
            return DateTimeUtils.mjdFloatDateToUtc(floatDate);
        }
        return null;
    }

    private void trackSensingTime(CompoundData gridPointData) throws IOException {
        final CompoundType type = gridPointData.getType();
        final String typeName = type.getName();
        if (typeName.contains("ECMWF")) {
            return; // no sensing time information in ECMWF auxiliary files
        }
        if (level2) {
            final Date timeStamp = getL2MjdTimeStamp(gridPointData);
            if (timeStamp != null) {
                timeTracker.track(timeStamp);
            }
        } else {
            int index = type.getMemberIndex(SmosConstants.BT_DATA_LIST_NAME);
            final SequenceData btDataList = gridPointData.getSequence(index);
            CompoundData btData = btDataList.getCompound(0);
            trackTime(btData);

            final int elementCount = btDataList.getElementCount();
            btData = btDataList.getCompound(elementCount - 1);
            trackTime(btData);
        }
    }

    private void trackTime(CompoundData btData) throws IOException {
        int index;
        index = btData.getType().getMemberIndex(SmosConstants.BT_SNAPSHOT_ID_OF_PIXEL_NAME);
        if (index >= 0) {
            final long snapShotId = btData.getUInt(index);
            timeTracker.track(snapshotIdTimeMap.get(snapShotId));
        }
    }

    private void trackGeometry(CompoundData gridPointData) throws IOException {
        double lat = gridPointData.getDouble(latIndex);
        double lon = gridPointData.getDouble(lonIndex);
        // normalisation to [-180, 180] necessary for some L1c test products
        if (lon > 180.0) {
            lon = lon - 360.0;
        }
        geometryTracker.add(new Point2D.Double(lon, lat));
    }

    private void init(CompoundData gridPointData) throws IOException {
        final CompoundType gridPointType = gridPointData.getType();
        latIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_LAT_NAME);
        lonIndex = gridPointType.getMemberIndex(SmosConstants.GRID_POINT_LON_NAME);

        final CollectionData parent = gridPointData.getParent();
        final long parentPosition = parent.getPosition();
        copySnapshotData(parent, parentPosition);

        createSnapshotIdMap(parent);

        targetContext.getData().setLong(SmosConstants.GRID_POINT_COUNTER_NAME, 0);
        targetContext.getData().flush();

        gridPointDataPosition = parentPosition;
    }

    private void createSnapshotIdMap(CollectionData parent) throws IOException {
        final DataContext context = parent.getContext();
        final int snapshotListIndex = context.getData().getMemberIndex(SmosConstants.SNAPSHOT_LIST_NAME);
        if (snapshotListIndex == -1) {
            return; // we have a browse product
        }
        final SequenceData snapshotData = context.getData().getSequence(snapshotListIndex);
        final int snapshotCount = snapshotData.getElementCount();
        for (int i = 0; i < snapshotCount; i++) {
            final CompoundData snapshot = snapshotData.getCompound(i);
            final Date snapshotTime = DateTimeUtils.cfiDateToUtc(snapshot);
            final long snapshotId = snapshot.getUInt(1);
            snapshotIdTimeMap.put(snapshotId, snapshotTime);
        }
    }

    private void copySnapshotData(CollectionData parent, long parentPosition) throws IOException {
        copyBytesTo(parent.getContext(), targetContext, parentPosition);
    }

    private static void copyBytesTo(DataContext sourceContext,
                                    DataContext targetContext, long to) throws IOException {
        byte[] bytes = new byte[SEGMENT_SIZE];

        for (long pos = 0; pos < to; pos += SEGMENT_SIZE) {
            final long remainderSize = to - pos;
            if (remainderSize < SEGMENT_SIZE) {
                bytes = new byte[(int) remainderSize];
            }

            get(sourceContext, bytes, pos);
            put(targetContext, bytes, pos);
        }
    }

    private static void get(CompoundData compoundData, byte[] bytes) throws IOException {
        final DataContext context = compoundData.getContext();
        final long position = compoundData.getPosition();
        context.getHandler().read(context, bytes, position);
    }

    private static void get(DataContext sourceContext, byte[] bytes, long position) throws IOException {
        sourceContext.getHandler().read(sourceContext, bytes, position);
    }

    private static void put(DataContext targetContext, byte[] bytes, long position) throws IOException {
        targetContext.getHandler().write(targetContext, bytes, position);
    }
}

