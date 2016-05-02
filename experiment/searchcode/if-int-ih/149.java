/**
 * Copyright (C) 2010 Cloudfarming <info@cloudfarming.nl>
 *
 * Licensed under the Eclipse Public License - v 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.cloudfarming.client.sensor;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.List;
import nl.cloudfarming.client.model.SensorData;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.BoundingBox;

/**
 *
 * @author Timon Veenstra
 */
public class RasterFactory {

    private RasterFactory(){}
    
    /**
     * create a raster from sensor data
     *
     *
     * @param sensorData
     * @return raster
     */
    public static GridCoverage2D createRaster(List<SensorData> sensorData) {

        if (sensorData.isEmpty()) {
            return null;
        }
        double minx = sensorData.get(0).getLongitude();
        double maxx = sensorData.get(0).getLongitude();
        double miny = sensorData.get(0).getLatitude();
        double maxy = sensorData.get(0).getLatitude();

        for (SensorData sd : sensorData) {
            minx = (minx < sd.getLongitude()) ? minx : sd.getLongitude();
            miny = (miny < sd.getLatitude()) ? miny : sd.getLatitude();
            maxx = (maxx > sd.getLongitude()) ? maxx : sd.getLongitude();
            maxy = (maxy > sd.getLatitude()) ? maxy : sd.getLatitude();
        }

        // width height and ration in coordiantes
        double diffx = maxx - minx;
        double diffy = maxy - miny;
        double ratio = diffy / diffx;


        // in pixels
        Double width = Math.ceil(Math.sqrt(sensorData.size() / ratio));
        Double height = Math.ceil(Math.sqrt(sensorData.size() / ratio));

        int w = width.intValue();
        int h = height.intValue();
        int bands = 1;
        Point location = new Point(0, 0);
        WritableRaster raster = WritableRaster.createBandedRaster(DataBuffer.TYPE_INT, w, h, bands, location);

        for (int iw = 0; iw < w; iw++) {
            for (int ih = 0; ih < h; ih++) {
                raster.setSample(iw,ih,0,Integer.MIN_VALUE);
            }
        }

        for (SensorData sd : sensorData) {
            Double x = Math.floor((sd.getLongitude() - minx) / (diffx / (width - 1)));
            Double y = Math.floor((sd.getLatitude() - miny) / (diffy / (height - 1)));

            //TODO why is y-axis flipped????? flipback here shouldnt be needed
            y = Math.abs(y + 1 - height);

            //TODO sample data gets casted into an int, so multuiply with 1000 for example
            raster.setSample(x.intValue(), y.intValue(), 0, sd.getCalculatedIndex1() * SensorPalette.SENSOR_DATA_MULTIPLIER);
//            raster.setSample(x.intValue(), y.intValue(), 1, sd.getCalculatedIndex2() * 1000);
//            raster.setSample(x.intValue(), y.intValue(), 2, sd.getReading1());
//            raster.setSample(x.intValue(), y.intValue(), 3, sd.getReading2());
//            raster.setSample(x.intValue(), y.intValue(), 4, sd.getReading3());
            
        }

        BoundingBox boundingBox = new ReferencedEnvelope(minx, maxx, miny, maxy, DefaultGeographicCRS.WGS84);


        GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(null);
        return factory.create("name", raster, boundingBox);
    }
}

