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
package nl.cloudfarming.client.model;

import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.Index;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * abstract base class for sensor data
 *
 * @author Merijn Zengers
 */
@NamedQueries(value = {
    @NamedQuery(name = SensorData.Q_FIND_DATA_FOR_RUN, query = "select d from SensorData d where d.sensorRun = :"+SensorData.P_FIND_DATA_FOR_RUN)
})
@Entity
@Table(name = "SENSOR_DATA")
//TODO implement property change events
public class SensorData extends Bean implements Serializable, Persistable {

    public static final String Q_FIND_DATA_FOR_RUN = "findDataForRun";
    public static final String P_FIND_DATA_FOR_RUN = "sensorData";
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SensorDataGen")
    @TableGenerator(name = "SensorDataGen", table = "SENSORDATA_GEN", pkColumnName = "PK", valueColumnName = "AID")
    @Column(name = "ID")
    private long id;
    @ManyToOne(targetEntity = SensorDataFile.class)
    @JoinColumn(name = "SENSOR_DATA_FILE_ID")
    private SensorDataFile sensorDataFile;
    @ManyToOne(targetEntity = SensorRun.class)
    @JoinColumn(name = "SENSOR_RUN_ID")
    private SensorRun sensorRun;
    /**
     * X
     */
    @Index
    @Column
    private double longitude;
    /**
     * Y
     */
    @Index
    @Column
    private double latitude;
    @Column
    private Double calculatedIndex1;
    @Column
    private Double calculatedIndex2;
    @Column
    private Double reading1;
    @Column
    private Double reading2;
    @Column
    private Double reading3;
    @Column(name = "UTC_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date utcTime;
    @Column
    private Double speed;
    @Column
    private Double course;

    public SensorData() {
    }

    public SensorData(SensorDataFile sensorDataFile, double longitude, double latitude) {
        this.sensorDataFile = sensorDataFile;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SensorDataFile getDataSensorFile() {
        return sensorDataFile;
    }

    public void setDataSensorFile(SensorDataFile geoDataSensorFile) {
        this.sensorDataFile = geoDataSensorFile;
    }

    public Double getCalculatedIndex1() {
        return calculatedIndex1;
    }

    public void setCalculatedIndex1(Double calculatedIndex1) {
        this.calculatedIndex1 = calculatedIndex1;
    }

    public Double getCalculatedIndex2() {
        return calculatedIndex2;
    }

    public void setCalculatedIndex2(Double calculatedIndex2) {
        this.calculatedIndex2 = calculatedIndex2;
    }

    public Double getCourse() {
        return course;
    }

    public void setCourse(Double course) {
        this.course = course;
    }

    @Override
    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Double getReading1() {
        return reading1;
    }

    public void setReading1(Double reading1) {
        this.reading1 = reading1;
    }

    public Double getReading2() {
        return reading2;
    }

    public void setReading2(Double reading2) {
        this.reading2 = reading2;
    }

    public Double getReading3() {
        return reading3;
    }

    public void setReading3(Double reading3) {
        this.reading3 = reading3;
    }

    public SensorDataFile getSensorDataFile() {
        return sensorDataFile;
    }

    public void setSensorDataFile(SensorDataFile sensorDataFile) {
        this.sensorDataFile = sensorDataFile;
    }

    public SensorRun getSensorRun() {
        return sensorRun;
    }

    public void setSensorRun(SensorRun sensorRun) {
        this.sensorRun = sensorRun;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Date getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(Date utcTime) {
        this.utcTime = utcTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SensorData other = (SensorData) obj;
        if (this.id != other.id) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (this.calculatedIndex1 != other.calculatedIndex1 && (this.calculatedIndex1 == null || !this.calculatedIndex1.equals(other.calculatedIndex1))) {
            return false;
        }
        if (this.calculatedIndex2 != other.calculatedIndex2 && (this.calculatedIndex2 == null || !this.calculatedIndex2.equals(other.calculatedIndex2))) {
            return false;
        }
        if (this.reading1 != other.reading1 && (this.reading1 == null || !this.reading1.equals(other.reading1))) {
            return false;
        }
        if (this.reading2 != other.reading2 && (this.reading2 == null || !this.reading2.equals(other.reading2))) {
            return false;
        }
        if (this.reading3 != other.reading3 && (this.reading3 == null || !this.reading3.equals(other.reading3))) {
            return false;
        }
        if (this.utcTime != other.utcTime && (this.utcTime == null || !this.utcTime.equals(other.utcTime))) {
            return false;
        }
        if (this.speed != other.speed && (this.speed == null || !this.speed.equals(other.speed))) {
            return false;
        }
        if (this.course != other.course && (this.course == null || !this.course.equals(other.course))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 47 * hash + (this.calculatedIndex1 != null ? this.calculatedIndex1.hashCode() : 0);
        hash = 47 * hash + (this.calculatedIndex2 != null ? this.calculatedIndex2.hashCode() : 0);
        hash = 47 * hash + (this.reading1 != null ? this.reading1.hashCode() : 0);
        hash = 47 * hash + (this.reading2 != null ? this.reading2.hashCode() : 0);
        hash = 47 * hash + (this.reading3 != null ? this.reading3.hashCode() : 0);
        hash = 47 * hash + (this.utcTime != null ? this.utcTime.hashCode() : 0);
        hash = 47 * hash + (this.speed != null ? this.speed.hashCode() : 0);
        hash = 47 * hash + (this.course != null ? this.course.hashCode() : 0);
        return hash;
    }
}

