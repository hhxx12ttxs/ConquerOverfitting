/* Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS.
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.google.ie.common.util;

import com.google.ie.business.dao.impl.DaoConstants;
import com.google.ie.common.constants.IdeaExchangeConstants;
import com.google.ie.common.search.analyzer.IdeaExchangeQueryAnalyzer;
import com.google.ie.dto.SearchResult;
import com.google.ie.web.controller.WebConstants;

import org.apache.log4j.Logger;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassHit;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassToken;
import org.compass.core.CompassQueryBuilder.CompassMultiPhraseQueryBuilder;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.support.search.CompassSearchCommand;
import org.compass.core.support.search.CompassSearchHelper;
import org.compass.core.support.search.CompassSearchResults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utility class to index and search different entities using compass search
 * engine.
 * 
 * @author adahiya
 * 
 */
public class SearchUtility {
    /** Logger for logging information */
    private static final Logger LOG = Logger.getLogger(SearchUtility.class.getName());
    /** Static compass instance */
    private final static Compass COMPASS;

    // Initialize the default configuration of compass for searching data.
    static {
        CompassConfiguration conf = new CompassConfiguration()
                        .setConnection("gae://index");
        conf.getSettings().setGroupSettings(
                        LuceneEnvironment.Analyzer.PREFIX,
                        LuceneEnvironment.Analyzer.DEFAULT_GROUP,
                        new String[] { LuceneEnvironment.Analyzer.TYPE },
                        new String[] { IdeaExchangeQueryAnalyzer.class.getName() });
        conf.getSettings().setGroupSettings(
                        LuceneEnvironment.Analyzer.PREFIX,
                        LuceneEnvironment.Analyzer.SEARCH_GROUP,
                        new String[] { LuceneEnvironment.Analyzer.TYPE },
                        new String[] { IdeaExchangeQueryAnalyzer.class.getName() });

        conf.getSettings().setSetting(
                        CompassEnvironment.ExecutorManager.EXECUTOR_MANAGER_TYPE,
                        "disabled");
        COMPASS = conf.addScan(IdeaExchangeConstants.PACKAGE_TO_INDEX).buildCompass();

    }

    /**
     * Index an entity based on configurations specified for the entity using
     * compass annotations.
     * 
     * @param entity the entity to be indexed
     * @return true if entity indexed successfully
     */
    public static boolean indexEntity(Serializable entity) {
        CompassIndexSession indexSession = null;
        try {
            indexSession = COMPASS.openIndexSession();

            if (COMPASS.getSearchEngineIndexManager()
                            .isLocked()) {
                COMPASS.getSearchEngineIndexManager()
                                .releaseLocks();
            }
            indexSession.save(entity);
            indexSession.flush();

            indexSession.commit();

            return true;

        } catch (CompassException e) {
            LOG.error("Compass exception caught.", e);
        } finally {
            if (indexSession != null && !indexSession.isClosed()) {
                indexSession.close();
            }
        }

        return false;

    }

    /**
     * Deletes an indexed entity from compass search index.
     * 
     * @param entity Serializable
     * @return boolean
     */
    public static boolean deleteEntityIndex(Serializable entity) {
        CompassIndexSession indexSession = null;
        try {
            indexSession = COMPASS.openIndexSession();

            if (COMPASS.getSearchEngineIndexManager()
                            .isLocked()) {
                COMPASS.getSearchEngineIndexManager()
                                .releaseLocks();
            }
            indexSession.delete(entity);
            indexSession.flush();

            indexSession.commit();

            return true;

        } catch (CompassException e) {
            LOG.error("Compass exception caught.", e);
        } finally {
            if (indexSession != null && !indexSession.isClosed()) {
                indexSession.close();
            }
        }

        return false;

    }

    /**
     * 
     * Search method for searching data on the basis of parameter passed.
     * 
     * @param query Keyword for searching data.
     * @param resourceProperty Column on which searching will be performed.
     * @param offset number of data in one page
     * @param pageSize page number for which data required.
     * @param alias alias names in which data will be searched
     * @return SearchResult
     */
    public static SearchResult search(String query,
                    String resourceProperty, int offset, int pageSize, String... alias) {
        // if (query == null || query.trim().length() == 0)
        // return null;
        return search(query, false, false, resourceProperty, offset, pageSize, alias);
    }

    /**
     * 
     * Search method for searching data on the basis of parameter passed.
     * 
     * @param query Keyword for searching data.
     * @param useAnalyzer use custom analyzer for searching.
     * @param resourceProperty Column on which searching will be performed.
     * @param offset number of data in one page
     * @param pageSize page number for which data required.
     * @param alias alias names in which data will be searched
     * @return SearchResult
     */
    public static SearchResult search(String query, boolean useAnalyzer,
                    String resourceProperty, int offset, int pageSize, String... alias) {
        if (query == null || query.trim().length() == 0)
            return null;
        return search(query, useAnalyzer, false, resourceProperty, offset, pageSize, alias);
    }

    /**
     * 
     * Search method for searching data on the basis of parameter passed.
     * 
     * @param query Keyword for searching data.
     * @param useAnalyzer use custom analyzer for searching.
     * @param useMultiPhrase multi-phrase search
     * @param resourceProperty Column on which searching will be performed.
     * @param offset number of data in one page
     * @param pageSize page number for which data required.
     * @return SearchResult
     */
    public static SearchResult search(String query, boolean useAnalyzer,
                    boolean useMultiPhrase,
                    String resourceProperty, int offset, int pageSize) {
        if (query == null || query.trim().length() > 0)
            return null;
        CompassSearchSession searchSession = null;

        SearchResult result = null;

        try {
            LOG.info("Searching started"
                            + GregorianCalendar.getInstance().getTime());
            searchSession = COMPASS
                            .openSearchSession();
            String analyzedQuery = query;
            // analyze the query with analyzer
            if (useAnalyzer) {
                analyzedQuery = getAnalyzedQuery(query, searchSession);
            }

            if (useMultiPhrase) {
                // create multiple phrase query and fetch data
                result = getMultiPhraseSearchData(resourceProperty, searchSession, analyzedQuery,
                                offset, pageSize);
            } else {
                analyzedQuery = analyzedQuery + WebConstants.ASTERISK;
                result = findData(query, resourceProperty, searchSession, offset, pageSize,
                                (String[]) null);
            }

        } catch (CompassException e) {
            LOG.error("An error occured during search", e);
        } finally {
            if (searchSession != null) {
                searchSession.close();
            }
        }
        return result;
    }

    /**
     * 
     * Search method for searching data on the basis of parameter passed.
     * 
     * @param query Keyword for searching data.
     * @param useAnalyzer use custom analyzer for searching.
     * @param useMultiPhrase multi-phrase search
     * @param resourceProperty Column on which searching will be performed.
     * @param offset number of data in one page
     * @param pageSize page number for which data required.
     * @return SearchResult
     */
    public static SearchResult search(String query, boolean useAnalyzer,
                    boolean useMultiPhrase,
                    String resourceProperty, int offset, int pageSize, String... alias) {
        // if (query == null || query.trim().length() == 0)
        // return null;
        CompassSearchSession searchSession = null;

        SearchResult result = null;

        try {
            LOG.info("Searching started"
                            + GregorianCalendar.getInstance().getTime());
            searchSession = COMPASS
                            .openSearchSession();
            String analyzedQuery = query;
            // analyze the query with analyzer
            if (useAnalyzer) {
                analyzedQuery = getAnalyzedQuery(query, searchSession);
            }

            if (useMultiPhrase) {
                // create multiple phrase query and fetch data
                result = getMultiPhraseSearchData(resourceProperty, searchSession, analyzedQuery,
                                offset, pageSize,
                                alias);
            } else {
                analyzedQuery = analyzedQuery + WebConstants.ASTERISK;
                result = findData(query, resourceProperty, searchSession, offset, pageSize, alias);
            }

        } catch (CompassException e) {
            LOG.error("An error occured during search", e);
        } finally {
            if (searchSession != null) {
                searchSession.close();
            }
        }
        return result;
    }

    /**
     * Convert compass hit into list.
     * 
     * @param hits CompassHit[]
     * @return List<Serializable>
     */
    private static List<Serializable> convertToList(CompassHit[] hits) {

        List<Serializable> result = new ArrayList<Serializable>();

        if (hits != null) {

            for (int counter = 0; counter < hits.length; counter++) {
                CompassHit hit = hits[counter];
                result.add((Serializable) hit.getData());
            }
        }
        return result;
    }

    /**
     * 
     * Search method for searching data on the basis of parameter passed.
     * 
     * @param query Keyword for searching data.
     * @param clazz Search data in particular class.
     * @param offset number of data in one page.
     * @param pageSize page number for which data required.
     * @return SearchResult
     */
    public static SearchResult search(String query,
                    Class<? extends Serializable> clazz, int offset, int pageSize) {
        if (query == null || query.trim().length() == 0)
            return null;
        CompassSearchSession searchSession = null;
        SearchResult result = null;
        try {
            LOG.info("Searching started"
                            + GregorianCalendar.getInstance().getTime());
            searchSession = COMPASS
                            .openSearchSession();

            result = findData(query, clazz, searchSession, offset, pageSize);

        } catch (CompassException e) {
            LOG.error("Some error occured during search at"
                            + GregorianCalendar.getInstance().getTime());
        } finally {
            if (searchSession != null) {
                searchSession.close();
            }
        }
        return result;
    }

    /**
     * This method just execute find query using compass.
     * 
     * @param query
     * @param searchIn
     * @param searchSession
     * @return CompassHits
     */
    private static SearchResult findData(String query, Class<? extends Serializable> clazz,
                    CompassSearchSession searchSession, int offset, int pageSize) {
        if (query == null || query.trim().length() == 0)
            return null;
        CompassQuery compassQuery = null;
        if (clazz != null) {
            compassQuery = getCompassSearchQuery(null,
                            searchSession, query, clazz.getSimpleName());

        } else
            compassQuery = getCompassSearchQuery(null,
                            searchSession, query);

        return getPaginatedData(compassQuery, offset, pageSize);
    }

    /**
     * Get paginated data by using search helper.
     * 
     * @param compassQuery
     * @param offset
     * @param pageSize
     * @return SearchResult
     */
    private static SearchResult getPaginatedData(CompassQuery compassQuery, int offset, int pageSize) {
        if (pageSize < IdeaExchangeConstants.ONE)
            pageSize = IdeaExchangeConstants.DEFAULT_PAGE_SIZE;
        if (offset < IdeaExchangeConstants.ZERO)
            offset = IdeaExchangeConstants.DEFAULT_OFFSET;
        CompassSearchHelper compassSearchHelper = new CompassSearchHelper(COMPASS, pageSize);

        CompassSearchCommand compassSearchCommand = new
                        CompassSearchCommand(compassQuery);
        compassSearchCommand.setPage(offset);
        CompassSearchResults compassSearchResults = compassSearchHelper
                        .search(compassSearchCommand);
        CompassHit[] hits = compassSearchResults.getHits();
        SearchResult searchResult = new SearchResult();
        searchResult.setTotalCount(compassSearchResults.getTotalHits());
        searchResult.setData(convertToList(hits));
        return searchResult;

    }

    /**
     * find data search data and returned paginated data.
     * 
     * @param query
     * @param resourceProperty
     * @param searchSession
     * @param offset
     * @param pageSize
     * @param alias
     * @return SearchResult
     */
    private static SearchResult findData(String query, String resourceProperty,
                    CompassSearchSession searchSession, int offset, int pageSize, String... alias) {
        if (query == null || query.trim().length() == 0)
            return null;
        CompassQuery compassQuery = getCompassSearchQuery(resourceProperty,
                        searchSession, query, alias);

        return getPaginatedData(compassQuery, offset, pageSize);

    }

    /**
     * get Compass Search Query.
     * 
     * @param resourceProperty
     * @param searchSession
     * @param query
     * @param alias
     * @return CompassQuery
     */
    private static CompassQuery getCompassSearchQuery(String resourceProperty,
                    CompassSearchSession searchSession, String query, String... alias) {
        CompassQuery compassQuery = null;
        if (isResourcePropertyNotNull(resourceProperty)) {
            compassQuery = searchSession.queryBuilder().queryString(
                            resourceProperty +
                            IdeaExchangeConstants.COLON +
                            query
                            ).toQuery();

        } else {
            compassQuery = searchSession.queryBuilder().queryString(
                            query).toQuery();
        }
        compassQuery.setAliases(alias);
        return compassQuery;
    }

    /**
     * is resource property not null.
     * 
     * @param resourceProperty
     * @return
     */
    private static boolean isResourcePropertyNotNull(String resourceProperty) {
        return resourceProperty != null && resourceProperty.trim().length() > 0;
    }

    /**
     * Get multiple phrase search data for objectionable content search.
     * 
     * @param resourceProperty
     * @param searchSession
     * @param analyzedQuery
     * @param offset
     * @param pageSize
     * @param aliases
     * @return SearchResult
     */
    private static SearchResult getMultiPhraseSearchData(String resourceProperty,
                    CompassSearchSession searchSession,
                    String analyzedQuery, int offset, int pageSize, String... aliases) {
        CompassQuery compassQuery = null;
        if (isResourcePropertyNotNull(resourceProperty)) {
            String[] data = analyzedQuery.toString().split(IdeaExchangeConstants.SPACE);
            CompassMultiPhraseQueryBuilder c = null;
            c = searchSession.queryBuilder().multiPhrase(resourceProperty);
            c.setSlop(1);
            c.add(data);

            compassQuery = c.toQuery().setAliases(aliases);
        } else {
            compassQuery = searchSession.queryBuilder().queryString(analyzedQuery).toQuery()
                            .setAliases(
                            aliases);
        }
        return getPaginatedData(compassQuery, offset, pageSize);
    }

    /**
     * Use analyzer to analyze query and return modified query..
     * 
     * @param query String
     * @param searchSession CompassSearchSession
     * @return String
     */
    private static String getAnalyzedQuery(String query, CompassSearchSession searchSession) {
        CompassToken[] compassTokens = searchSession.analyzerHelper()
                        .analyze(query);
        StringBuilder querey = new StringBuilder();
        if (compassTokens != null && compassTokens.length > DaoConstants.ZERO) {
            for (CompassToken compassToken : compassTokens) {
                querey.append(compassToken.getTermText() + IdeaExchangeConstants.SPACE);
            }
        }
        return querey.toString();
    }

    /**
     * Getter method for getting compass instance.
     * 
     * @return
     */
    public static Compass getCompass() {
        return COMPASS;
    }

}


