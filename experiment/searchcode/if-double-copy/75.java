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

package nl.cloudfarming.client.sensor.greenseeker;

import java.io.Serializable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import nl.cloudfarming.client.model.SensorData;
import nl.cloudfarming.client.model.SensorDataFile;


/**
 *
 * @author Gerben Feenstra
 */
@Entity
@DiscriminatorValue("greenseeker")
public class GreenseekerSensorData extends SensorData implements Serializable{

    private double objectId;
    private double heading;
    private double elevation;
    private double ndvi;
    private double vi2nd;
    private double red53;
    private double nir53;
    private double red54;
    private double nir54;
    private double red55;
    private double nir55;
    private double red56;
    private double nir56;
    private double red57;
    private double nir57;
    private double red58;
    private double nir58;
    private double sensor10;
    private double sensor19;
    private double sensor23;
    private double sensor24;
    private double sensor26;
    private double sensor73;

    /**
     * For JPA use only.
     */
    public GreenseekerSensorData(){
    }


     /**
     * Constructor for creating a Borne GreenseekerSensorDataBO
     */
    public GreenseekerSensorData(SensorDataFile dataFile, double objectId, double longitude, double latitude,
            double speed, double heading, double elevation, double ndvi, double vi2nd, double red53, double nir53, double red54, double nir54, double red55, double nir55,
            double red56, double nir56, double red57, double nir57, double red58, double nir58) {

        super(dataFile, longitude, latitude);
        super.setSpeed(speed);
        this.objectId = objectId;
        this.heading = heading;
        this.elevation = elevation;
        this.ndvi = ndvi;
        this.vi2nd = vi2nd;
        this.red53 = red53;
        this.nir53 = nir53;
        this.red54 = red54;
        this.nir54 = nir54;
        this.red55 = red55;
        this.nir55 = nir55;
        this.red56 = red56;
        this.nir56 = nir56;
        this.red57 = red57;
        this.nir57 = nir57;
        this.red58 = red58;
        this.nir58 = nir58;
    }

     /**
     * Constructor for creating a normal GreenseekerSensorDataBO
     */
    public GreenseekerSensorData(SensorDataFile dataFile, double objectId, double longitude, double latitude,
            double speed, double heading, double elevation, double ndvi, double vi2nd,
            double sensor10, double sensor19, double sensor23, double sensor24, double sensor26, double sensor73) {
        super(dataFile, longitude, latitude);
        setSpeed(speed);
        this.objectId = objectId;
        this.heading = heading;
        this.elevation = elevation;
        this.ndvi = ndvi;
        setCalculatedIndex1(ndvi);
        this.vi2nd = vi2nd;
        setCalculatedIndex2(vi2nd);
        this.sensor10 = sensor10;
        this.sensor19 = sensor19;
        this.sensor23 = sensor23;
        this.sensor24 = sensor24;
        this.sensor26 = sensor26;
        this.sensor73 = sensor73;
    }

    public double getObjectId() {
        return objectId;
    }

    public void setObjectId(double objectId) {
        this.objectId = objectId;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getNdvi() {
        return ndvi;
    }

    public void setNdvi(double ndvi) {
        this.ndvi = ndvi;
    }

    public double getNir53() {
        return nir53;
    }

    public void setNir53(double nir53) {
        this.nir53 = nir53;
    }

    public double getNir54() {
        return nir54;
    }

    public void setNir54(double nir54) {
        this.nir54 = nir54;
    }

    public double getNir55() {
        return nir55;
    }

    public void setNir55(double nir55) {
        this.nir55 = nir55;
    }

    public double getNir56() {
        return nir56;
    }

    public void setNir56(double nir56) {
        this.nir56 = nir56;
    }

    public double getNir57() {
        return nir57;
    }

    public void setNir57(double nir57) {
        this.nir57 = nir57;
    }

    public double getNir58() {
        return nir58;
    }

    public void setNir58(double nir58) {
        this.nir58 = nir58;
    }

    public double getRed53() {
        return red53;
    }

    public void setRed53(double red53) {
        this.red53 = red53;
    }

    public double getRed54() {
        return red54;
    }

    public void setRed54(double red54) {
        this.red54 = red54;
    }

    public double getRed55() {
        return red55;
    }

    public void setRed55(double red55) {
        this.red55 = red55;
    }

    public double getRed56() {
        return red56;
    }

    public void setRed56(double red56) {
        this.red56 = red56;
    }

    public double getRed57() {
        return red57;
    }

    public void setRed57(double red57) {
        this.red57 = red57;
    }

    public double getRed58() {
        return red58;
    }

    public void setRed58(double red58) {
        this.red58 = red58;
    }

    public double getSensor10() {
        return sensor10;
    }

    public void setSensor10(double sensor10) {
        this.sensor10 = sensor10;
    }

    public double getSensor19() {
        return sensor19;
    }

    public void setSensor19(double sensor19) {
        this.sensor19 = sensor19;
    }

    public double getSensor23() {
        return sensor23;
    }

    public void setSensor23(double sensor23) {
        this.sensor23 = sensor23;
    }

    public double getSensor24() {
        return sensor24;
    }

    public void setSensor24(double sensor24) {
        this.sensor24 = sensor24;
    }

    public double getSensor26() {
        return sensor26;
    }

    public void setSensor26(double sensor26) {
        this.sensor26 = sensor26;
    }

    public double getSensor73() {
        return sensor73;
    }

    public void setSensor73(double sensor73) {
        this.sensor73 = sensor73;
    }

    public double getVi2nd() {
        return vi2nd;
    }

    public void setVi2nd(double vi2nd) {
        this.vi2nd = vi2nd;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GreenseekerSensorData other = (GreenseekerSensorData) obj;
        if (Double.doubleToLongBits(this.objectId) != Double.doubleToLongBits(other.objectId)) {
            return false;
        }
        if (Double.doubleToLongBits(this.heading) != Double.doubleToLongBits(other.heading)) {
            return false;
        }
        if (Double.doubleToLongBits(this.elevation) != Double.doubleToLongBits(other.elevation)) {
            return false;
        }
        if (Double.doubleToLongBits(this.ndvi) != Double.doubleToLongBits(other.ndvi)) {
            return false;
        }
        if (Double.doubleToLongBits(this.vi2nd) != Double.doubleToLongBits(other.vi2nd)) {
            return false;
        }
        if (Double.doubleToLongBits(this.red53) != Double.doubleToLongBits(other.red53)) {
            return false;
        }
        if (Double.doubleToLongBits(this.nir53) != Double.doubleToLongBits(other.nir53)) {
            return false;
        }
        if (Double.doubleToLongBits(this.red54) != Double.doubleToLongBits(other.red54)) {
            return false;
        }
        if (Double.doubleToLongBits(this.nir54) != Double.doubleToLongBits(other.nir54)) {
            return false;
        }
        if (Double.doubleToLongBits(this.red55) != Double.doubleToLongBits(other.red55)) {
            return false;
        }
        if (Double.doubleToLongBits(this.nir55) != Double.doubleToLongBits(other.nir55)) {
            return false;
        }
        if (Double.doubleToLongBits(this.red56) != Double.doubleToLongBits(other.red56)) {
            return false;
        }
        if (Double.doubleToLongBits(this.nir56) != Double.doubleToLongBits(other.nir56)) {
            return false;
        }
        if (Double.doubleToLongBits(this.red57) != Double.doubleToLongBits(other.red57)) {
            return false;
        }
        if (Double.doubleToLongBits(this.nir57) != Double.doubleToLongBits(other.nir57)) {
            return false;
        }
        if (Double.doubleToLongBits(this.red58) != Double.doubleToLongBits(other.red58)) {
            return false;
        }
        if (Double.doubleToLongBits(this.nir58) != Double.doubleToLongBits(other.nir58)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sensor10) != Double.doubleToLongBits(other.sensor10)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sensor19) != Double.doubleToLongBits(other.sensor19)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sensor23) != Double.doubleToLongBits(other.sensor23)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sensor24) != Double.doubleToLongBits(other.sensor24)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sensor26) != Double.doubleToLongBits(other.sensor26)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sensor73) != Double.doubleToLongBits(other.sensor73)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.objectId) ^ (Double.doubleToLongBits(this.objectId) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.heading) ^ (Double.doubleToLongBits(this.heading) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.elevation) ^ (Double.doubleToLongBits(this.elevation) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.ndvi) ^ (Double.doubleToLongBits(this.ndvi) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.vi2nd) ^ (Double.doubleToLongBits(this.vi2nd) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.red53) ^ (Double.doubleToLongBits(this.red53) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nir53) ^ (Double.doubleToLongBits(this.nir53) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.red54) ^ (Double.doubleToLongBits(this.red54) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nir54) ^ (Double.doubleToLongBits(this.nir54) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.red55) ^ (Double.doubleToLongBits(this.red55) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nir55) ^ (Double.doubleToLongBits(this.nir55) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.red56) ^ (Double.doubleToLongBits(this.red56) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nir56) ^ (Double.doubleToLongBits(this.nir56) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.red57) ^ (Double.doubleToLongBits(this.red57) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nir57) ^ (Double.doubleToLongBits(this.nir57) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.red58) ^ (Double.doubleToLongBits(this.red58) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nir58) ^ (Double.doubleToLongBits(this.nir58) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.sensor10) ^ (Double.doubleToLongBits(this.sensor10) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.sensor19) ^ (Double.doubleToLongBits(this.sensor19) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.sensor23) ^ (Double.doubleToLongBits(this.sensor23) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.sensor24) ^ (Double.doubleToLongBits(this.sensor24) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.sensor26) ^ (Double.doubleToLongBits(this.sensor26) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.sensor73) ^ (Double.doubleToLongBits(this.sensor73) >>> 32));
        return hash;
    }

    


}

