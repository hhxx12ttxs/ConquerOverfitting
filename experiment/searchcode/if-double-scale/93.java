/* Copyright (c) 2010 ARt Project owners
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
package com.artgameweekend.projects.art.service;

/**
 *
 * @author pierre
 */
public class LayarParamsService
{
    private int distance = 100;
    private int type = 0;
    private int dimension = 2;
    private boolean rel =  true;
    private int angle = 0;
    private double scale = 1.0;

    private static LayarParamsService instance = new LayarParamsService();
    
    private LayarParamsService()
    {
        
    }
    
    public static LayarParamsService instance()
    {
        return instance;
    }

    /**
     * @return the distance
     */
    public int getDistance()
    {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(int distance)
    {
        this.distance = distance;
    }

    /**
     * @return the type
     */
    public int getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type)
    {
        this.type = type;
    }

    /**
     * @return the dimension
     */
    public int getDimension()
    {
        return dimension;
    }

    /**
     * @param dimension the dimension to set
     */
    public void setDimension(int dimension)
    {
        this.dimension = dimension;
    }

    /**
     * @return the rel
     */
    public boolean getRel()
    {
        return rel;
    }

    /**
     * @param rel the rel to set
     */
    public void setRel(boolean rel)
    {
        this.rel = rel;
    }

    /**
     * @return the angle
     */
    public int getAngle()
    {
        return angle;
    }

    /**
     * @param angle the angle to set
     */
    public void setAngle(int angle)
    {
        this.angle = angle;
    }

    /**
     * @return the scale
     */
    public double getScale()
    {
        return scale;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(double scale)
    {
        this.scale = scale;
    }
    
    

}

