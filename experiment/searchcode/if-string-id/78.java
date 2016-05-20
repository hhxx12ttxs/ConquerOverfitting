/* Copyright (c) 2010-2012 ARTags project owners (see http://www.artags.org)
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
package org.artags.site.business;

import java.io.Serializable;

/**
 *
 * @author pierre
 */
public class Tag implements Serializable
{

    private String id;
    private String name;
    private String thumbnailId;
    private String rating;
    private String longitude;
    private String latitude;
    private String date;
    private String url;
    private String IconUrl;
    private long dateValue;
    private int ratingValue;
    private int ratingCount;

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the thumbnailId
     */
    public String getThumbnailId()
    {
        return thumbnailId;
    }

    /**
     * @param thumbnailId the thumbnailId to set
     */
    public void setThumbnailId(String thumbnailId)
    {
        this.thumbnailId = thumbnailId;
    }

    /**
     * @return the rating
     */
    public String getRating()
    {
        return rating;
    }

    /**
     * @param rating the rating to set
     */
    public void setRating(String rating)
    {
        this.rating = rating;
    }

    /**
     * @return the longitude
     */
    public String getLongitude()
    {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    /**
     * @return the latitude
     */
    public String getLatitude()
    {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    /**
     * @return the date
     */
    public String getDate()
    {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date)
    {
        this.date = date;
    }

    /**
     * @return the url
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return the IconUrl
     */
    public String getIconUrl()
    {
        return IconUrl;
    }

    /**
     * @param IconUrl the IconUrl to set
     */
    public void setIconUrl(String IconUrl)
    {
        this.IconUrl = IconUrl;
    }

    /**
     * @return the dateValue
     */
    public long getDateValue()
    {
        return dateValue;
    }

    /**
     * @param dateValue the dateValue to set
     */
    public void setDateValue(long dateValue)
    {
        this.dateValue = dateValue;
    }

    /**
     * @return the ratingValue
     */
    public int getRatingValue()
    {
        return ratingValue;
    }

    /**
     * @param ratingValue the ratingValue to set
     */
    public void setRatingValue(int ratingValue)
    {
        this.ratingValue = ratingValue;
    }

    /**
     * @return the ratingCount
     */
    public int getRatingCount()
    {
        return ratingCount;
    }

    /**
     * @param ratingCount the ratingCount to set
     */
    public void setRatingCount(int ratingCount)
    {
        this.ratingCount = ratingCount;
    }
}

