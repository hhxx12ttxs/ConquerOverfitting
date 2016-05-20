/*
 * Gasdroid 
 * Copyright (C) 2012  Andrea Antonello (www.hydrologis.com)
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
package org.gasdroide.database;

/**
 * Class that represents a dataset definition.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class Dataset {
    private long id;
    private String title;
    private long ts;
    private Double lat;
    private Double lon;
    private double[] p1;
    private double[] p2;

    public Dataset( long id, String title, long ts, Double lat, Double lon, double[] p1, double[] p2 ) {
        this.id = id;
        this.title = title;
        this.ts = ts;
        this.lat = lat;
        this.lon = lon;
        this.p1 = p1;
        this.p2 = p2;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getTs() {
        return ts;
    }

    public double getLat() {
        if (lat == null) {
            return Double.NaN;
        }
        return lat;
    }

    public double getLon() {
        if (lon == null) {
            return Double.NaN;
        }
        return lon;
    }

    public double[] getP1() {
        return p1;
    }

    public double[] getP2() {
        return p2;
    }
}

