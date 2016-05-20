package org.gbif.ecatws.servlet;

import org.gbif.checklistbank.model.NameUsage;
import org.gbif.checklistbank.model.NameUsageFull;
import org.gbif.checklistbank.model.NameUsageSimple;
import org.gbif.checklistbank.model.VernacularNameFull;
import org.gbif.checklistbank.model.voc.ChecklistType;
import org.gbif.checklistbank.model.voc.SearchType;
import org.gbif.checklistbank.model.voc.SortOrder;
import org.gbif.checklistbank.service.ClassificationService;
import org.gbif.checklistbank.service.IdentifierService;
import org.gbif.checklistbank.service.ImageService;
import org.gbif.checklistbank.service.NameUsageService;
import org.gbif.checklistbank.service.VernacularService;
import org.gbif.ecat.voc.NomenclaturalCode;
import org.gbif.ecat.voc.Rank;
import org.gbif.ecat.voc.TaxonomicStatus;
import org.gbif.ecatws.servlet.result.Data;
import org.gbif.ecatws.servlet.result.ResultWriter;
import org.gbif.ecatws.servlet.util.UsageServletSupport;
import org.gbif.ecatws.servlet.util.WsException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static org.gbif.ecatws.servlet.util.Params.PARA_CODE;
import static org.gbif.ecatws.servlet.util.Params.PARA_COUNT;
import static org.gbif.ecatws.servlet.util.Params.PARA_ID;
import static org.gbif.ecatws.servlet.util.Params.PARA_NAME_ID;
import static org.gbif.ecatws.servlet.util.Params.PARA_NAME_RESOLVER;
import static org.gbif.ecatws.servlet.util.Params.PARA_NUB_ID;
import static org.gbif.ecatws.servlet.util.Params.PARA_QUERY;
import static org.gbif.ecatws.servlet.util.Params.PARA_SEARCH_TYPE;
import static org.gbif.ecatws.servlet.util.Params.PARA_SHOW_CLASSIFICATION;
import static org.gbif.ecatws.servlet.util.Params.PARA_SHOW_IDS;
import static org.gbif.ecatws.servlet.util.Params.PARA_SHOW_IMAGE;
import static org.gbif.ecatws.servlet.util.Params.PARA_STATUS;
import static org.gbif.ecatws.servlet.util.Params.PARA_YEAR;

@Singleton
public class NameUsageServlet extends ClbWebservice {
  private @Inject
  NameUsageService<NameUsageSimple> usageService;
  private @Inject
  ClassificationService classificationService;
  private @Inject
  NameUsageService<NameUsageFull> usageFullService;
  private @Inject
  UsageServletSupport usageSupport;
  private @Inject
  ImageService imageService;
  private @Inject
  IdentifierService identifierService;
  private @Inject
  VernacularService<VernacularNameFull> vernacularService;

  private static Set<Integer> getChecklistIds(Collection<NameUsageSimple> usages) {
    Set<Integer> ids = new HashSet<Integer>();
    for (NameUsageSimple u : usages) {
      ids.add(u.getChecklistId());
    }
    return ids;
  }

  @Override
  protected void execute(ResultWriter out, HttpServletRequest req) throws WsException {
    // COLLECT PARAMETERS
    Integer id = paraAsInt(req, PARA_ID, null);

    // different ACTIONS depending on parameters:
    if (id != null) {
      // get by usageID
      showUsageDetail(out, req, id);
    } else {
      if (paraAsBool(req, PARA_NAME_RESOLVER)) {
        resolveName(out, req);
      } else {
        searchUsages(out, req);
      }
    }
  }

  protected void resolveName(ResultWriter out, HttpServletRequest req) throws WsException {
    // first search for matching usages and then either show the single full usage or a short disambiguation list
    NomenclaturalCode code = NomenclaturalCode.fromString(para(req, PARA_CODE, null));;
    Integer year = paraAsInt(req, PARA_YEAR, null);
    Integer checklistId = requestedChecklistId(req, null);
    String nameToResolve = para(req, PARA_QUERY, null);
    String q = nameToResolve;
    if (q != null) {
      q = q.replaceAll("[_+\\s]+", " ") + "$";
    }
    int pagesize = pagesize(req, DEFAULT_PAGESIZE);
    List<NameUsageSimple> usages = usageService.search(null, null, q, null, checklistId, null, null,
        null, page(req, DEFAULT_PAGE), pagesize, null);
    if (usages.size() == 1) {
      // single usage, redirect (LinkedData rule) to full details
      out.redirect("/usage?id=" + usages.get(0).getId());
    } else if (usages.size() > 1) {
      Data data = new Data();
      data.usages = usages;
      out.success(data, "usages");
    } else {
      // no results
      out.notFound(NameUsage.class, nameToResolve);
    }
  }

  protected void searchUsages(ResultWriter out, HttpServletRequest req) throws WsException {
    // if no usage detail we will get a pageable result list
    Data data = new Data();

    // first get general search modifier and params valid for all searches
    SearchType searchType = SearchType.fromString(para(req, PARA_SEARCH_TYPE, null));;
    Integer checklistId = requestedChecklistId(req, null);
    // increase resource hits?
    if (checklistId != null) {
      hitsService.addHit(checklistId);
    }
    TaxonomicStatus status = TaxonomicStatus.fromString(para(req, PARA_STATUS, null));
    Rank rankFilter = usageSupport.requestedRank(req);
    Set<ChecklistType> ctypes = ChecklistServlet.requestedChecklistTypes(req);
    int pagesize = pagesize(req, DEFAULT_PAGESIZE);

    // is a nameID requested?
    Integer nid = paraAsInt(req, PARA_NAME_ID, null);
    Integer nubId = paraAsInt(req, PARA_NUB_ID, null);
    String q = para(req, PARA_QUERY, null);
    // sorting
    SortOrder order = UsageServletSupport.requestedSortOrder(req);
    if (nid != null || nubId != null || q != null) {
      data.usages = usageService.search(nid, nubId, q, searchType, checklistId, ctypes, status, rankFilter, page(req, DEFAULT_PAGE), pagesize, order);
      // count total hits?
      boolean doCount = paraAsBool(req, PARA_COUNT);
      if (doCount) {
        if (data.usages.size() >= pagesize) {
          data.totalHits = usageService.searchCount(nid, nubId, q, searchType, checklistId, ctypes, status, rankFilter);
        } else {
          data.totalHits = data.usages.size();
        }
      }
      if (data.usages != null && !data.usages.isEmpty()) {
        // optional additional response data requested?
        boolean showImage = paraAsBool(req, PARA_SHOW_IMAGE);
        boolean showIds = paraAsBool(req, PARA_SHOW_IDS);

        Set<String> requestedVernacularLanguages = paraVernacularLanguages(req);
        data.higherRanks = UsageServletSupport.requestedHigherRanks(req);

//				log.debug("Additional data requested: showImage="+showImage+", showIds="+showIds+", showRanks="+StringUtils.join(data.higherRanks,"|")+", vernaculars="+StringUtils.join(requestedVernacularLanguages,"|"));

        // if requested also retrieve classification
        if (data.higherRanks.size() > 0) {
          classificationService.addClbVerbatimClassifications(data.usages,
              data.higherRanks.toArray(new Rank[data.higherRanks.size() - 1]));
        }
        // optionally include additional data
        if (showImage) {
          data.usage2Images = usageSupport.usageImages(data.usages);
        }
        if (showIds) {
          data.usage2Identifier = usageSupport.usageIdentifier(data.usages);
        }
        if (!requestedVernacularLanguages.isEmpty()) {
          data.usage2Vernaculars = usageSupport.usageVernaculars(data.usages, requestedVernacularLanguages);
        }
        // add checklist metadata
        data.checklistMap = checklistService.get(getChecklistIds(data.usages));
        // increase resource hits
        for (Integer cid : data.checklistMap.keySet()) {
          hitsService.addHit(cid);
        }

      }
      out.success(data);

    } else {
      // no query
      out.badRequest("Request parameter usage id ID, query search parameter Q, name id NID or lexical group LEXID is required");
    }
  }

  protected void showUsageDetail(ResultWriter out, HttpServletRequest req, Integer usageId) throws WsException {
    // optional additional response data requested?
    boolean showHigherClassification = false;
    if (para(req, PARA_SHOW_CLASSIFICATION, "").contains("x")) {
      showHigherClassification = true;
    }

    Data data = new Data();
    data.usage = usageFullService.get(usageId);
    if (data.usage == null) {
      out.notFound(NameUsage.class, usageId);
      return;
    } else {
      data.checklist = checklistService.get(data.usage.getChecklistId());
      // increase resource hits
      hitsService.addHit(data.checklist.getId());
      data.images = imageService.getByUsage(usageId);
      data.vernaculars = vernacularService.getByUsage(usageId);
      data.synonyms = usageService.getSynonyms(usageId, 1, 100, SortOrder.alpha, null);
      data.identifier = identifierService.getByUsage(usageId);
      data.similar = usageService.listByNubId(data.usage.getNubId());
      data.children = new ArrayList<NameUsageSimple>();
      // remove self from list
      Integer selfIdx = null;
      for (int i = 0; i < data.similar.size(); i++) {
        if (data.similar.get(i) != null && data.similar.get(i).getId().equals(usageId)) {
          selfIdx = i;
          break;
        }
      }
      if (selfIdx != null) {
        data.similar.remove((int) selfIdx);
      }
      data.classification = classificationService.getClbVerbatimClassification(usageId);
      // too slow currently. takes 1-2 seconds to retrieve, so make it optional
      if (showHigherClassification) {
        data.fullClassification = classificationService.getClassification(usageId);
      }

      // render response
      out.success(data, "usage");
    }
  }

}

