/*
 * Copyright 2011 Blue Tang Studio. All rights reserved.
 * Date: 2011-06-21
 */

package com.bluetangstudio.searchcloud.client.model;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 * The metadata of query response.
 */
@JsonAutoDetect
@JsonPropertyOrder( { "searchQuery", "start", "count", "total" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultListMetadata {

    /**
     * The SearchQuery object contains queries, constraints and pagination
     * services for searching.
     */
    private final SearchQuery searchQuery;

    /**
     * The first index number of the returned data.
     */
    private final Integer start;

    /**
     * The number of documents to return from Search Cloud.
     */
    private final Integer count;

    /**
     * The total number of documents to return from Search Cloud.
     */
    private final Integer total;

    /**
     * Constructor.
     * 
     * @param searchQuery
     * @param total
     * @param start
     * @param count
     */
    @JsonCreator
    public ResultListMetadata(@JsonProperty("searchQuery") SearchQuery searchQuery,
            @JsonProperty("start") Integer start, @JsonProperty("count") Integer count,
            @JsonProperty("total") Integer total) {
        this.searchQuery = searchQuery;
        this.total = total;
        this.start = start;
        this.count = count;
    }

    /**
     * Returns the searchQuery.
     * 
     * @return the searchQuery
     */
    public final SearchQuery getSearchQuery() {
        return searchQuery;
    }

    /**
     * Returns the start.
     * 
     * @return the start
     */
    public final Integer getStart() {
        return start;
    }

    /**
     * Returns the count.
     * 
     * @return the count
     */
    public final Integer getCount() {
        return count;
    }

    /**
     * Return the total.
     * 
     * @return the total
     */
    public final Integer getTotal() {
        return total;
    }

    // CHECKSTYLE:OFF
    /**
     * Determines whether o is equal to this ResultListMetadata.
     * 
     * @param o
     *            The ResultListMetadata to compare equality with.
     * 
     * @return Returns true if equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ResultListMetadata))
            return false;

        ResultListMetadata that = (ResultListMetadata) o;

        if (count != null ? !count.equals(that.count) : that.count != null)
            return false;
        if (searchQuery != null ? !searchQuery.equals(that.searchQuery) : that.searchQuery != null)
            return false;
        if (start != null ? !start.equals(that.start) : that.start != null)
            return false;
        if (total != null ? !total.equals(that.total) : that.total != null)
            return false;

        return true;
    }

    /**
     * Computes the hash code of this ResultListMetadata using searchQuery,
     * start, count and total.
     * 
     * @return The hash code of this ResultListMetadata as an integer.
     */
    @Override
    public int hashCode() {
        int result = searchQuery != null ? searchQuery.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        return result;
    }
}

