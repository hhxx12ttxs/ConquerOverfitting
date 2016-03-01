/***************************************************************************
 * Copyright 2010 Global Biodiversity Information Facility Secretariat
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.checklistbank.nub;

import org.gbif.checklistbank.Constants;
import org.gbif.checklistbank.cache.CacheUtils;
import org.gbif.checklistbank.cache.NameStringCache;
import org.gbif.checklistbank.cache.NameUsageCache;
import org.gbif.checklistbank.lookup.ClassificationMatcher;
import org.gbif.checklistbank.lookup.ClassifiedName;
import org.gbif.checklistbank.model.lite.NameStringLite;
import org.gbif.checklistbank.model.lite.NameUsageLite;
import org.gbif.checklistbank.service.TermService;
import org.gbif.checklistbank.utils.RankUtil;
import org.gbif.ecat.voc.NameType;
import org.gbif.ecat.voc.Rank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds all homonyms and their reference usage (with classification, rank & status)
 * to allow matching of homonym names with classification to their correct concept.
 *
 * @author markus
 */

/**
 * @author markus
 */
public class HomonymInformer {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private final NameUsageCache nuCache;
  private final NameStringCache nsCache;
  private final TermService termService;
  private final ClassificationMatcher classificationMatcher;
  // keyed on lower case canonical name
  private Map<String, List<NameUsageLite>> byNormedCanon = Maps.newHashMap();
  // set to null for no debugging or a canonical name to log its details
  private final String DEBUG_TRACE_NAME = null;

  /**
   * @param termService
   * @param nuCache
   * @param nsCache
   * @param matcher
   * @param useColHigherTaxa if true load homonyms also from higher catalogue of life taxa
   * @param homonymChecklistId list of checklist ids to be used as homonym sources
   */
  public HomonymInformer(TermService termService, NameUsageCache nuCache, NameStringCache nsCache,
    ClassificationMatcher matcher, boolean useColHigherTaxa, int... homonymChecklistId) {
    log.debug("Instantiating homonym informer from " + homonymChecklistId.length + " checklists");
    this.termService = termService;
    this.nsCache = nsCache;
    this.nuCache = nuCache;
    this.classificationMatcher = matcher;

    // load higher taxa homonyms from CoL?
    if (useColHigherTaxa){
      loadColHomonyms();
    }

    // now load homonym for each checklist from cache
    for (int c : homonymChecklistId) {
      loadHomonyms(c);
    }

    log.debug("Created homonym informer with " + byNormedCanon.size() + " normalised canonical names");
  }

  private void addUsage(NameUsageLite u) {
    if (u == null) {
      return;
    }

    // use preferred rank only
    NameStringLite name = nsCache.get(u.nameFk);
    if (NameType.blacklisted == name.type) {
      log.debug("Ignoring blacklisted name {}", name.scientificName);
      return;
    }

    String cn = name.scientificName;
    if (NameType.hybrid == name.type || NameType.virus == name.type) {
      log.debug("Adding unparsable name {} of type {} to informer", name.scientificName, name.type);
    } else if (name.canonicalNameFk < 0) {
      log.debug("No canonical name for {}", name.scientificName);
    } else {
      cn = nsCache.get(name.canonicalNameFk).scientificName;
    }

    // key on normalised canonical name
    cn = normalize(cn);
    if (!this.byNormedCanon.containsKey(cn)) {
      this.byNormedCanon.put(cn, new ArrayList<NameUsageLite>());
    }
    this.byNormedCanon.get(cn).add(u);
  }

  /**
   * Finds a matching homonym for a standalone, verbatim name usage.
   * Assumes a canonical name being passed in already and doesnt parse the name further.
   *
   * @param usage the usage with a canonical name and optional classification to query for
   *
   * @return the homonym lookup result
   */
  public HomonymLookup findReferenceUsage(ClassifiedName usage) {
    String canonName = normalize(usage.getScientificName());
    return findReferenceUsage(canonName, usage);
  }

  /**
   * Main method to look for homonyms when the usage is known to be part of the homonym cache!
   *
   * @return the homonym lookup result
   */
  public HomonymLookup findReferenceUsage(NameUsageLite usage) {
    NameStringLite name = nsCache.get(usage.nameFk);
    String canonName;
    if (name.canonicalNameFk == usage.nameFk || name.canonicalNameFk < 0) {
      canonName = normalize(name.scientificName);
    } else {
      canonName = normalize(nsCache.get(name.canonicalNameFk).scientificName);
    }
    return findReferenceUsage(canonName, usage2classifiedName(usage));
  }

  /**
     * main method to look for homonyms. This method will compare a given usage
     * with its known canonical name homonyms and return the corresponding usage id of the
     * best matching reference homonym if it can be found. For clear non homonyms a -1 will be
     * returned. If a yet unknown species is given with a genus known to be a
     * homonym, the species will be added to the informers lookup and its
     * usageId will be returned.
     *
     * @return the homonym reference usage id, potentially of the usage input if its a new one, or -1 if its no homonym
     */
  private HomonymLookup findReferenceUsage(String canonName, ClassifiedName cname) {
    // check for matches in the following order:
    // 1) match on canonicalNameID
    // 2) if nothing, try to match the canonical species or genus alone
    // => species & genus matches potentially add new usages to the informer

    // matching by full canonical name?
    if (byNormedCanon.containsKey(canonName)) {
      ClassifiedName best = findUsageByClassificationComp(cname, byNormedCanon.get(canonName));
      if (best == null) {
        return HomonymLookup.undecidableHomonym(cname.getUsageId());
      } else {
        return HomonymLookup.homonym(cname.getUsageId(), best.getUsageId());
      }

    } else {
      // try with partial species or genus match if whitespace exists
      if (StringUtils.countMatches(canonName, " ") > 0) {
        // its a species or infraspecies, try with genus alone
        String genus = StringUtils.substringBefore(canonName, " ");
        if (byNormedCanon.containsKey(genus)) {
          // only match to genera now, avoid inter rank homonyms
          List<NameUsageLite> refHomonyms = Lists.newArrayList();
          for (NameUsageLite nu : byNormedCanon.get(genus)){
            Rank rank = termService.preferredRank(nu.rankFk);
            if (rank != null && rank == Rank.GENUS){
              refHomonyms.add(nu);
            }
          }
          // only if there are at least 2 homonym genera flag this name as a genus homonym
          if (refHomonyms.size() > 1){
            ClassifiedName best = findUsageByClassificationComp(cname, refHomonyms);
            if (best == null) {
              return HomonymLookup.undecidableGenusHomonym(cname.getUsageId());
            } else {
              return HomonymLookup.genusHomonym(cname.getUsageId(), best.getUsageId());
            }
          }
        }
      }
    }
    return HomonymLookup.noHomonym(cname.getUsageId());
  }

  private ClassifiedName findUsageByClassificationComp(ClassifiedName usage, List<NameUsageLite> refHomonyms) {
    // convert NameUsageLite to ClassifiedName so we can compare verbatim classifications
    List<ClassifiedName> references = Lists.newArrayList();
    for (NameUsageLite u : refHomonyms) {
      references.add(usage2classifiedName(u));
    }
    return classificationMatcher.matchBestClassification(usage, references, -4);
  }

  /**
   * Lists all known authorative homonyms for a given canonical name.
   * This does NOT include potential new (infra) species homonyms found during reference usage look up:
   * @see #findReferenceUsage(org.gbif.checklistbank.model.lite.NameUsageLite)
   *
   * @return the list of reference usages for a given canonical name
   */
  public List<ClassifiedName> listReferenceUsages(String canonicalName) {
    List<ClassifiedName> result = new ArrayList<ClassifiedName>();
    String canonName = normalize(canonicalName);
    if (byNormedCanon.containsKey(canonName)) {
      for (NameUsageLite u : byNormedCanon.get(canonName)) {
        result.add(usage2classifiedName(u));
      }
    }
    return result;
  }

  /**
   * Lists all known homonyms for a given namestring object.
   * Different from the overloaded string based method the name doesnt have to be a canonical.
   *
   * @return the list of reference usages for a given name
   */
  public List<ClassifiedName> listReferenceUsages(NameStringLite name) {
    if (name == null){
      return new ArrayList<ClassifiedName>();
    }
    NameStringLite canonical = nsCache.getCanonical(name.id);
    return listReferenceUsages(normalize(canonical.scientificName));
  }


  /**
   * Adds all homonyms from a certain checklist off the internal usage/name cache.
   * Only canonical names that have at least 2 usages will be loaded - otherwise it isnt a homonym.
   * @param homonymChecklistId
   */
  public void loadHomonyms(int homonymChecklistId) {
    log.debug("Adding homonym usages to informer from checklist " + homonymChecklistId + " ...");
    // if we wanna load homonyms, we need to filter out the homonyms first
    TIntObjectHashMap<TIntHashSet> canonMap = new TIntObjectHashMap<TIntHashSet>();

    // iterate over all usages for checklist
    Iterator<NameUsageLite> iter = nuCache.checklistIterator(homonymChecklistId);
    while (iter.hasNext()){
      NameUsageLite u = iter.next();
      NameStringLite n = nsCache.get(u.nameFk);
      if (n == null) continue;

      if (n.canonicalNameFk < 0) {
        log.warn("Ignoring name without canonical name: " + n.scientificName);
      } else {
        if (!canonMap.containsKey(n.canonicalNameFk)) {
          canonMap.put(n.canonicalNameFk, new TIntHashSet());
        }
        canonMap.get(n.canonicalNameFk).add(u.id);
      }
    }

    addCanonicalMap(homonymChecklistId, canonMap);
  }

  private void loadColHomonyms() {
    // we want to load higher homonyms of the same rank only...
    for (Rank r : RankUtil.HIGHER_CLB_RANKS) {
      log.debug("Adding "+r+" homonyms to informer from Catalogue Of Life ...");
      TIntObjectHashMap<TIntHashSet> canonMap = new TIntObjectHashMap<TIntHashSet>();
      int counter = 0;
      // iterate over all usages and filter by checklist & rank
      // iterate over all usages for checklist
      Iterator<NameUsageLite> iter = nuCache.checklistIterator(Constants.COL_CHECKLIST_ID);
      while (iter.hasNext()) {
        NameUsageLite u = iter.next();
        if (u.rankFk == r.termID()){
          counter++;
          NameStringLite n = nsCache.get(u.nameFk);
          if (n == null) continue;

          if (n.canonicalNameFk < 0) {
            log.warn("Ignoring name without canonical name: " + n.scientificName);
          } else {
            if (!canonMap.containsKey(n.canonicalNameFk)) {
              canonMap.put(n.canonicalNameFk, new TIntHashSet());
            }
            canonMap.get(n.canonicalNameFk).add(u.id);
          }
        }
      }

      log.debug("{} usages for rank {} found in CoL", counter, r);
      addCanonicalMap(Constants.COL_CHECKLIST_ID, canonMap);
    }
  }

  private void addCanonicalMap(int homonymChecklistId, TIntObjectHashMap<TIntHashSet> canonMap){
    // see how many canonical homonyms we really have...
    int canonCount = 0;
    int usageCount = 0;
    TIntObjectIterator<TIntHashSet> canoniter = canonMap.iterator();
    for (int i = canonMap.size(); i-- > 0; ) {
      canoniter.advance();
      int canonNameId = canoniter.key();
      TIntHashSet canonUsages = canoniter.value();
      String canonName = normalize(nsCache.getCanonicalName(canonNameId));
      final boolean isTraced = DEBUG_TRACE_NAME != null && canonName.startsWith(normalize(DEBUG_TRACE_NAME));
      // only add "homonyms" with at least 2 usages
      if (canonUsages.size() > 1) {
        // ok, only true homonyms across all ranks left. Add them to informer
        canonCount++;
        if (isTraced){
          log.debug("TRACING NAME {}: {} homonyms in checklist {}", new Object[]{canonName, canonUsages.size(), homonymChecklistId});
        }
        // now check if we have already homonyms for this name - we need to merge them to remove duplicates!
        if (byNormedCanon.containsKey(canonName)){
          // Merge existing with new homonyms for the same canonical name - keep the bigger set only!
          if (byNormedCanon.get(canonName).size() < canonUsages.size() ){
            log.warn("Homonym name {} treated in multiple sources, replace with {} names from checklist {}", new Object[]{canonName, canonUsages.size(), homonymChecklistId});
            usageCount -= byNormedCanon.get(canonName).size();
            byNormedCanon.get(canonName).clear();
          } else {
            log.warn("Homonym name {} treated in multiple sources, keep existing {} homonyms", canonName, byNormedCanon.get(canonName).size());
            canonUsages.clear();
          }
        }

        // add all that are left if classification is different
        for (int usageId : canonUsages.toArray()) {
          NameUsageLite u = nuCache.get(usageId);
          // ignore homonyms with the exact same classification as a previous one
          if (containsSameClassification(u, byNormedCanon.get(canonName))){
            log.warn("\"{}\" homonym usage {} with exact same classification ignored in homonym informer", canonName, usageId);
            continue;
          }
          addUsage(u);
          usageCount++;
        }

        //remove canonical if just one is left
        if (byNormedCanon.containsKey(canonName) && byNormedCanon.get(canonName).size() < 2){
          byNormedCanon.remove(canonName);
        }

      } else if (isTraced){
        log.debug("TRACING NAME {}: no homonym in checklist {}", canonName, homonymChecklistId);
      }
    }

    log.debug("Added " + usageCount + " usages from checklist " + homonymChecklistId + " representing " + canonCount
              + " canonical homonyms to informer");
  }

  /**
   * @param u the usage to compare
   * @param other usages to compare u with
   * @return true if there is at least one usage that does not differ in its classification
   */
  private boolean containsSameClassification(NameUsageLite u, @Nullable List<NameUsageLite> other){
    if (other == null){
      return false;
    }
    ClassifiedName n1 = usage2classifiedName(u);
    for (NameUsageLite u2 : other){
      ClassifiedName n2 = usage2classifiedName(u2);
      // if any of the other names does not contradict in its classification, return true
      if (!classificationMatcher.contradicts(n1, n2)){
        return true;
      }
    }
    return false;
  }

  private String normalize(String cname) {
    if (cname == null) {
      return null;
    }
    return cname.toLowerCase().trim().replaceAll("\\s+", " ");
  }

  /**
   * @return the number of canonical homonym names aware of
   */
  public int size() {
    return byNormedCanon.size();
  }

  /**
   * @param usage     classification of unmatched name with canonical names in higher ranks
   *
   * @return the matching rate/confidence. The higher the better the match
   */
  private ClassifiedName usage2classifiedName(NameUsageLite usage) {
    return CacheUtils.usage2classifiedName(usage.id,nuCache,nsCache,termService);
  }
}

