package org.jote.util.dynaque;

import org.apache.commons.lang.StringUtils;
import org.jote.util.dynaque.annotation.Dynaquery;
import org.jote.util.dynaque.annotation.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jote
 * Date: 11/7/12
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class DynaqueEngine {
    private DynaqueParameter parameter;
    private Dynaquery query;
    private List<ParameterValue> values;
    private List<QueryParameter> rootQueryParameters = new ArrayList<QueryParameter>();
    private Map<String, Boolean> nullParameterInputValue = new HashMap<String, Boolean>();
    private String stringQuery = "";
    private String stringSortedQuery = "";
    private String stringCountQuery = "";
    private Query jpaQuery;

    private static String ROOT = "root";
    private static String WHERE = "where";


    public DynaqueEngine() {
    }

    public DynaqueEngine(Dynaquery query, DynaqueParameter parameter) {
        this.query = query;
        this.parameter = parameter;
    }

    private void extract(Dynaquery query) {
        // initialize roots
        rootQueryParameters = extractChild(ROOT);
    }

    private List<QueryParameter> extractChild(String parentName) {
        Assert.notNull(parentName, "Parent Query Parameter name should be specified already");
        List<QueryParameter> childs = new ArrayList<QueryParameter>();
        List<QueryParameter> queries = Arrays.asList(query.parameters());
        for (Iterator<QueryParameter> iterator = queries.iterator(); iterator.hasNext(); ) {
            QueryParameter qp = iterator.next();
            if (StringUtils.equals(parentName, qp.parent())) {
                childs.add(qp);
            }
        }
        return childs;
    }
//
//    private List<QuerySubParameter> extract(QueryParameter parameter) {
//        Assert.notNull(parameter, "Query Parameter is not ExistsF");
//        return Arrays.asList(parameter.subParameter());
//    }

    private void gather(DynaqueParameter parameter) {
        List<ParameterValue> values = new ArrayList<ParameterValue>();
        if (parameter.getDefaults() != null) {
            values.addAll(parameter.getDefaults().getValues());
        }
        if (parameter.getRegulars() != null) {
            List<ParameterValue> regulars = parameter.getRegulars().getValues();
            for (Iterator<ParameterValue> iterator = regulars.iterator(); iterator.hasNext(); ) {
                ParameterValue value = iterator.next();
                if (!isExistedInDefaultParameterValue(value)) {
                    values.add(value);
                }
            }
        }
        List<ParameterValue> toOverrides = new ArrayList<ParameterValue>();
        if (parameter.getOverrides() != null) {
            List<ParameterValue> overrides = parameter.getRegulars().getValues();
            for (Iterator<ParameterValue> iterator = overrides.iterator(); iterator.hasNext(); ) {
                ParameterValue value = iterator.next();
                if (!isOverridenParameterValue(value)) {
                    toOverrides.add(value);
                }
            }
            values.addAll(toOverrides);
        }
        this.values = values;
    }


    private void scanNullValue(List<ParameterValue> values) {
        for (Iterator<ParameterValue> iterator = values.iterator(); iterator.hasNext(); ) {
            ParameterValue value = iterator.next();
            boolean isNull = false;
            if ((value.getValue() == null) ||
                    (value.getValue() instanceof Iterable &&
                            !((Iterable) value.getValue()).iterator().hasNext()) ||
                    (value.getValue() instanceof String &&
                            ("".equals((String) value.getValue()) || "%".equals((String) value.getValue())))) {

                isNull = true;
            }
            if (isNull) {
                nullParameterInputValue.put(value.getName(), Boolean.TRUE);
            }
        }
    }

    public String buildQuery() {
        return buildQuery(query);
    }

    private String buildQuery(Dynaquery query) {
        extract(query);
        gather(parameter);
        scanNullValue(values);
        StringBuilder sb = new StringBuilder(query.base());
        boolean first = true;
        boolean containWhere = sb.toString().toLowerCase().contains("where");
        int c = 0;
        String sspar = "";
        for (Iterator<QueryParameter> iterator = rootQueryParameters.iterator(); iterator.hasNext(); ) {
            QueryParameter qp = iterator.next();
            String spar = buildQuery(qp, first);
            if (first) {
                if (!containWhere) {
                    sb.append(" " + WHERE);
                } else {
                    first = false;
                    spar = buildQuery(qp, first);
                }
                first = StringUtils.isEmpty(spar.trim());
            } else {

            }
            sb.append(spar);
            sspar += String.format("\n%s: %s %s", qp.name(), spar, c);
            c++;
        }
        stringQuery = sb.toString();
        logger.debug(sspar);
        return sb.toString();

    }

    Logger logger = LoggerFactory.getLogger(DynaqueEngine.class);

    private ParameterValue getValueByName(String name) {
        for (Iterator<ParameterValue> iterator = values.iterator(); iterator.hasNext(); ) {
            ParameterValue pv = iterator.next();
            if (StringUtils.equals(name, pv.getName())) {
                return pv;
            }
        }
        return null;
    }

    private QueryParameter getParameterByName(String name) {
        QueryParameter[] qps = query.parameters();
        for (int i = 0; i < qps.length; i++) {
            if (StringUtils.equals(name, qps[i].name())) {
                return qps[i];
            }
        }

        for (Iterator<QueryParameter> iterator = rootQueryParameters.iterator(); iterator.hasNext(); ) {
            QueryParameter qp = iterator.next();
            if (StringUtils.equals(name, qp.name())) {
                return qp;
            }
        }
        return null;
    }

    private String buildQuery(QueryParameter parameter, boolean first) {
        List<QueryParameter> subs = extractChild(parameter.name());
        if (subs != null && subs.size() > 0) {
            boolean firstSub = true;
            StringBuilder sb = new StringBuilder("");
            String sspar="";
            int c = 0;
            for (Iterator<QueryParameter> iterator = subs.iterator(); iterator.hasNext(); ) {
                QueryParameter sub = iterator.next();
                String ssub = buildQuery(sub, firstSub);
                firstSub = firstSub ? StringUtils.isEmpty(ssub.trim()) : false;
                sb.append(ssub);
                c++;
                sspar += String.format("\n%s.%s: %s ;firstsub:%s", c, sub.name(), ssub,firstSub);
            }
            logger.debug(sspar);
            String slogic = first ? "" : Dynautil.getLogic(parameter.logic());
            return StringUtils.isEmpty(sb.toString().trim()) ? "" : String.format(" %s (%s)", slogic, sb.toString());
        } else {
            return buildQuery(parameter.name(), parameter.field(), parameter.nullMeans(), parameter.logic(), first);
        }
    }

//    private String buildQuery(QuerySubParameter parameter, boolean first) {
//
//        return buildQuery(parameter.name(), parameter.field(), parameter.nullMeans(), parameter.logic(), first);
//    }

    private String buildQuery(String name,
                              String field,
                              NullMeans nullMeans, QueryParameterLogic logic, boolean first) {
        StringBuilder sb = new StringBuilder("");
        Operator operator = null;
        ParameterValue pv = getValueByName(name);
        if (pv == null) {
            return "";//ignore this Query Parameter;
        }
        operator = pv.getOperator();
        //Check is null value
        String operatorAndValue = String.format("%s :%s", Dynautil.getOperator(operator), name);
        if (isNullValue(name)) {
            if (NullMeans.IGNORE.equals(nullMeans)) {
                //Do nothing
                return "";
            } else if (NullMeans.IS.equals(nullMeans)) {
                if (isNullValue(name)) {
                    operator = Operator.IS;
                    operatorAndValue = String.format("%s %s", Dynautil.getOperator(operator), "null");
                }


            }
        }
        String slogic = first ? "" : Dynautil.getLogic(logic);
        sb.append(String.format(" %s %s %s  ", slogic, field, operatorAndValue));
        return sb.toString();
    }

    private Query createQuery(EntityManager em) {
        System.out.println("this is string sorted query: ___ " + stringSortedQuery);
        Query q = em.createQuery(stringSortedQuery);
        jpaQuery = q;
        return jpaQuery;
    }

    private Query createCountQuery(EntityManager em) {
        Query q = em.createQuery(stringCountQuery, Long.class);
        jpaQuery = q;
        return jpaQuery;
    }

    private Query applyPagination() {
        if (jpaQuery != null && parameter.getPageable() != null) {
            Pageable p = parameter.getPageable();
            jpaQuery.setFirstResult(p.getOffset());
            jpaQuery.setMaxResults(p.getPageSize());
        }
        return jpaQuery;
    }

    public Query count(EntityManager em) {
        buildQuery();
        applyCount();
        createCountQuery(em);
        bindParam();
        return jpaQuery;
    }

    public Query fetch(EntityManager em) {
        buildQuery();

        applySort();
        createQuery(em);
        applyPagination();
        bindParam();
        return jpaQuery;
    }

    public Query bindParam() {
        for (Iterator<ParameterValue> iterator = values.iterator(); iterator.hasNext(); ) {
            ParameterValue pv = iterator.next();
            QueryParameter qp = getParameterByName(pv.getName());
            boolean nullValue = isNullValue(pv.getName());
            if (!nullValue) {
                jpaQuery.setParameter(pv.getName(), pv.getValue());
            }
//            else if (nullValue && NullMeans.IS.equals(qp.nullMeans())) {
//                jpaQuery.setParameter(pv.getName(), null);
//            }
        }

        return jpaQuery;
    }

    private String applyCount() {
        stringCountQuery = QueryUtils.createCountQueryFor(stringQuery);
        return stringCountQuery;
    }

    private String applySort() {
        Sort sort = parameter.getSort();
        if (sort == null) {
            stringSortedQuery = stringQuery;
            return stringSortedQuery;
        }
        String alias = QueryUtils.detectAlias(stringQuery);
        stringSortedQuery = QueryUtils.applySorting(stringQuery, sort, alias);
        return stringSortedQuery;
    }

    private boolean isNullValue(String name) {
        Boolean val = nullParameterInputValue.get(name);
        return val != null && val.equals(Boolean.TRUE);
    }

    private boolean isExistedInDefaultParameterValue(ParameterValue value) {
        if (value == null) {
            return false;
        }
        if (parameter != null && parameter.getDefaults() != null) {
            List<ParameterValue> defaults = parameter.getDefaults().getValues();
            for (Iterator<ParameterValue> iterator = defaults.iterator(); iterator.hasNext(); ) {
                ParameterValue pv = iterator.next();
                boolean found = StringUtils.equalsIgnoreCase(value.getName(), pv.getName());
                if (found) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean isOverridenParameterValue(ParameterValue value) {
        if (value == null) {
            return false;
        }
        if (this.values != null) {
            for (Iterator<ParameterValue> iterator = this.values.iterator(); iterator.hasNext(); ) {
                ParameterValue pv = iterator.next();
                boolean found = StringUtils.equalsIgnoreCase(value.getName(), pv.getName());
                if (found) {
                    return true;
                }
            }

        }
        return false;
    }


}

