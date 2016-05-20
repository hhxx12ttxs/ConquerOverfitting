/*
 * Copyright (c) 2014. Knowledge Media Institute - The Open University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.open.kmi.iserve.discovery.api;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.iServeProperty;
import uk.ac.open.kmi.iserve.discovery.api.freetextsearch.FreeTextSearchPlugin;
import uk.ac.open.kmi.iserve.discovery.api.ranking.*;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.ReverseRanker;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.StandardRanker;
import uk.ac.open.kmi.iserve.discovery.util.CallbackEvent;
import uk.ac.open.kmi.iserve.discovery.util.Pair;
import uk.ac.open.kmi.iserve.discovery.util.StringToMatchTypeConverter;
import uk.ac.open.kmi.iserve.sal.events.OntologyEvent;
import uk.ac.open.kmi.iserve.sal.events.ServiceEvent;
import uk.ac.open.kmi.iserve.sal.exception.SalException;
import uk.ac.open.kmi.iserve.sal.manager.IntegratedComponent;
import uk.ac.open.kmi.iserve.sal.util.caching.Cache;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheException;
import uk.ac.open.kmi.iserve.sal.util.caching.CacheFactory;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Luca Panziera on 19/05/2014.
 */
public class DiscoveryEngineImpl extends IntegratedComponent implements DiscoveryEngine {

    private static Cache<String, Map<URI, Pair<Double, MatchResult>>> resultCache;
    private static Cache<String, String> callbackQueries;
    private OperationDiscoverer operationDiscoverer;
    private ServiceDiscoverer serviceDiscoverer;
    private FreeTextSearchPlugin freeTextSearchPlugin;
    private Set<Filter> filters;
    private Set<Scorer> scorers;
    private ScoreComposer scoreComposer;
    private Logger logger = LoggerFactory.getLogger(DiscoveryEngineImpl.class);
    private EventBus callbackBus = new AsyncEventBus("callbacks", Executors.newCachedThreadPool());
    @Inject
    private StringToMatchTypeConverter converter;

    @Inject
    public DiscoveryEngineImpl(EventBus eventBus,
                               @iServeProperty(ConfigurationProperty.ISERVE_URL) String iServeUri,
                           ServiceDiscoverer serviceDiscoverer,
                           OperationDiscoverer operationDiscoverer,
                           @Nullable FreeTextSearchPlugin freeTextSearchPlugin,
                           @Nullable Set<Filter> filters,
                           @Nullable Set<AtomicFilter> atomicFilters,
                           @Nullable Set<Scorer> scorers,
                           @Nullable Set<AtomicScorer> atomicScorers,
                           @Nullable ScoreComposer scoreComposer,
                           CacheFactory cacheFactory

    ) throws SalException {
        super(eventBus, iServeUri);

        this.operationDiscoverer = operationDiscoverer;
        this.serviceDiscoverer = serviceDiscoverer;
        this.freeTextSearchPlugin = freeTextSearchPlugin;
        this.filters = filters;
        this.scorers = scorers;
        this.scoreComposer = scoreComposer;

        if (atomicFilters != null) {
            if (this.filters == null) {
                this.filters = new HashSet<Filter>();
            } else {
                this.filters = new HashSet<Filter>(this.filters);
            }
            for (AtomicFilter atomicFilter : atomicFilters) {
                this.filters.add(new MolecularFilter(atomicFilter));
            }
        }

        if (atomicScorers != null) {
            if (this.scorers == null) {
                this.scorers = new HashSet<Scorer>();
            } else {
                this.scorers = new HashSet<Scorer>(this.scorers);
            }
            for (AtomicScorer atomicScorer : atomicScorers) {
                this.scorers.add(new MolecularScorer(atomicScorer));
            }
        }

        if (resultCache == null) {
            try {
                resultCache = cacheFactory.createPersistentCache("discovery-result");
            } catch (CacheException e) {
                resultCache = cacheFactory.createInMemoryCache("discovery-result");
            }

        }
        if (callbackQueries == null) {
            try {
                callbackQueries = cacheFactory.createPersistentCache("callback-queries");
            } catch (CacheException e) {
                callbackQueries = cacheFactory.createInMemoryCache("callback-queries");
            }

        }

    }

    @Override
    public Map<URI, Pair<Double, MatchResult>> discover(String request, URL callback) {
        JsonElement jsonRequest = new JsonParser().parse(request);
        callbackQueries.put(callback.toString(), jsonRequest.toString());
        return discover(request);
    }

    public Map<URI, Pair<Double, MatchResult>> discover(String request) {
        JsonElement jsonRequest = new JsonParser().parse(request);
        if (resultCache.containsKey(jsonRequest.toString())) {
            return resultCache.get(jsonRequest.toString());
        } else {
            Map<URI, Pair<Double, MatchResult>> result = discover(jsonRequest);
            resultCache.put(jsonRequest.toString(), result);
            return result;
        }

    }

    public Map<URI, Pair<Double, MatchResult>> discover(JsonElement request) {
        return discover(parse(request));
    }

    private DiscoveryRequest parse(JsonElement jsonRequest) {
        boolean filter = false;
        boolean rank = false;
        Map<Class, String> parametersMap = Maps.newHashMap();
        Set<Class> requestedModules = Sets.newHashSet();
        String rankingType = null;

        JsonObject requestObject = jsonRequest.getAsJsonObject();

        // Discovery parsing
        JsonObject discovery = requestObject.getAsJsonObject("discovery");
        DiscoveryFunction discoveryFunction = new DiscoveryFunction(discovery, serviceDiscoverer, operationDiscoverer, freeTextSearchPlugin, converter);

        try {
            // filtering parsing
            if (requestObject.has("filtering")) {
                filter = true;
                JsonArray filters = requestObject.getAsJsonArray("filtering");
                for (JsonElement filterElement : filters) {
                    JsonObject filterObject = filterElement.getAsJsonObject();
                    Class filterClass = Class.forName(filterObject.get("filterClass").getAsString());
                    requestedModules.add(filterClass);
                    if (filterObject.has("parameters")) {
                        parametersMap.put(filterClass, filterObject.get("parameters").toString());
                    }
                }

            }
            //scoring parsing
            if (requestObject.has("scoring")) {
                rank = true;
                JsonArray scorers = requestObject.getAsJsonArray("scoring");
                for (JsonElement scorerElement : scorers) {
                    JsonObject scorerObject = scorerElement.getAsJsonObject();
                    Class scorerClass = Class.forName(scorerObject.get("scorerClass").getAsString());
                    requestedModules.add(scorerClass);
                    if (scorerObject.has("parameters")) {
                        parametersMap.put(scorerClass, scorerObject.get("parameters").toString());
                    }
                }
                rankingType = "standard";

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // ranking parsing
        if (requestObject.has("ranking")) {
            rank = true;
            if (!requestObject.get("ranking").getAsString().equalsIgnoreCase("inverse")) {
                rankingType = "standard";
            } else {
                rankingType = requestObject.get("ranking").getAsString().toLowerCase();
            }
        }

        DiscoveryRequest discoveryRequest = new DiscoveryRequest(discoveryFunction, requestedModules, filter, rank, parametersMap, rankingType);
        return discoveryRequest;
    }

    public Map<URI, Pair<Double, MatchResult>> discover(DiscoveryRequest discoveryRequest) {

        logger.info("Functional discovery");
        DiscoveryFunction discoveryFunction = discoveryRequest.getDiscoveryFunction();
        ForkJoinPool pool = new ForkJoinPool();
        Map<URI, MatchResult> funcResults = pool.invoke(discoveryFunction);

        Set<URI> filteredResources = funcResults.keySet();
        if (discoveryRequest.filter()) {
            for (Filter filter : filters) {
                Class filterClass;
                if (filter instanceof MolecularFilter) {
                    filterClass = ((MolecularFilter) filter).getAtomicFilter().getClass();
                } else {
                    filterClass = filter.getClass();
                }
                if (discoveryRequest.getRequestedModules() != null && discoveryRequest.getRequestedModules().contains(filterClass)) {
                    logger.debug("Filtering results with {}", filter);
                    if (discoveryRequest.getModuleParametersMap().containsKey(filterClass)) {
                        filteredResources = filter.apply(filteredResources, discoveryRequest.getModuleParametersMap().get(filterClass));
                    } else {
                        filteredResources = filter.apply(filteredResources);
                    }
                }
            }
        }

        if (discoveryRequest.rank()) {
            // Rank if requested
            logger.debug("Preparing scoring");
            Map<Scorer, Map<URI, Double>> localScoresMap = Maps.newHashMap();

            for (Scorer scorer : scorers) {
                Class scorerClass;
                if (scorer instanceof MolecularScorer) {
                    scorerClass = ((MolecularScorer) scorer).getAtomicScorer().getClass();
                } else {
                    scorerClass = scorer.getClass();
                }
                // Find out which ones need to be applied
                if (discoveryRequest.getRequestedModules() != null && discoveryRequest.getRequestedModules().contains(scorerClass)) {
                    Map<URI, Double> localScores;
                    if (discoveryRequest.getModuleParametersMap().containsKey(scorerClass)) {
                        localScores = scorer.apply(filteredResources, discoveryRequest.getModuleParametersMap().get(scorerClass));
                    } else {
                        localScores = scorer.apply(filteredResources);
                    }
                    localScoresMap.put(scorer, localScores);
                }
            }

            logger.info("Calculating final ranking ...");
            Map<URI, Double> finalScores;
            if (!localScoresMap.isEmpty()) {
                // Scorers were provided
                logger.debug("Composing scores using {}", localScoresMap.keySet());
                finalScores = scoreComposer.compose(localScoresMap);
            } else {
                // Dummy scorer that assigns one by default
                logger.debug("Applying constant scoring to all results");
                finalScores = Maps.asMap(filteredResources, Functions.constant((double) 1));
            }

            Map<URI, Double> rankedURIs;
            if (discoveryRequest.getRankingType().equals("standard")) {
                logger.debug("Applying Standard Ranker");
                rankedURIs = new StandardRanker().rank(finalScores);
            } else {
                logger.debug("Applying Reverse Ranker");
                rankedURIs = new ReverseRanker().rank(finalScores);
            }

            ImmutableMap.Builder<URI, Pair<Double, MatchResult>> builder = ImmutableMap.builder();
            for (URI resource : rankedURIs.keySet()) {
                builder.put(resource, new Pair<Double, MatchResult>(rankedURIs.get(resource), funcResults.get(resource)));
            }

            return builder.build();
        }

        // Returns the filtered results otherwise
        logger.debug("Returning filtering results without ranking");
        ImmutableMap.Builder<URI, Pair<Double, MatchResult>> builder = ImmutableMap.builder();
        for (URI resource : filteredResources) {
            builder.put(resource, new Pair<Double, MatchResult>(new Double(0), funcResults.get(resource)));
        }
        return builder.build();
    }

    @Override
    public EventBus getCallbackBus() {
        return callbackBus;
    }

    // It returns discovery engine configuration
    public String toString() {
        StringBuilder descriptionBuilder = new StringBuilder();
        descriptionBuilder.append("Operation Discoverer: ").append(operationDiscoverer.getClass().getName()).append("\n")
                .append("Service Discoverer: ").append(serviceDiscoverer.getClass().getName()).append("\n");
        if (!filters.isEmpty()) {
            descriptionBuilder.append("Available Filters: { ");
            for (Filter filter : filters) {
                descriptionBuilder.append(filter.getClass().getName()).append(" ");
            }
            descriptionBuilder.append("}\n");
        }
        if (!scorers.isEmpty()) {
            descriptionBuilder.append("Available Scorers: { ");
            for (Scorer scorer : scorers) {
                descriptionBuilder.append(scorer.getClass().getName()).append(" ");
            }
            descriptionBuilder.append("}\n");
        }

        return descriptionBuilder.toString();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void rebuildCache(ServiceEvent e) {
        rebuildCache();
    }

    @Subscribe
    @AllowConcurrentEvents
    public void rebuildCache(OntologyEvent e) {
        rebuildCache();
    }

    private void rebuildCache() {
        logger.debug("Rebuilding discovery cache");
        resultCache.clear();
        for (String callback : callbackQueries.keySet()) {
            discover(callbackQueries.get(callback));
            //generate callback event
            try {
                callbackBus.post(new CallbackEvent(new URL(callback), new Date()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}

