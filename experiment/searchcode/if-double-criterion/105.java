package com.oc.dao.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.ObjectUtils;

import com.oc.common.util.Detect;
import com.oc.common.util.JsonUtil;
import com.oc.model.criteria.BetweenParameter;
import com.oc.model.criteria.Criteria;
import com.oc.model.criteria.CriteriaParameter;
import com.oc.model.criteria.CriteriaParameterItem;
import com.oc.model.criteria.CriteriaType;
import com.oc.model.criterion.Criterion;
import com.oc.model.criterion.QueryCriteria;
import com.oc.model.criterion.Restrictions;
import com.oc.common.vo.MapParameter;

/**
 *
 * @author nathanleewei
 *
 */
public abstract class CriteriaParameterUtil extends Detect {

    public static final String STATEMENT_PARAMETER_PLACEHOLDER = "?";

    public static QueryCriteria toQueryCriteria(String json) {
        List<CriteriaParameterItem> parameterItems = JsonUtil.unmarshalList(json, CriteriaParameterItem.class);

        return toQueryCriteria(parameterItems);
    }

    public static QueryCriteria toQueryCriteria(List<CriteriaParameterItem> parameterItems) {
        QueryCriteria queryCriteria = new QueryCriteria();
        if (notEmpty(parameterItems)) {
            for (CriteriaParameterItem item : parameterItems) {
                if (null != item.getValue()) {
                    if (item.getCriteriaType().equals(CriteriaType.NativeSql))
                        queryCriteria.add(assembleCriterion(item.getAttribute(), item.getCriteriaType(), null, item.getValue().toString()));
                    else
                        queryCriteria.add(assembleCriterion(item.getAttribute(), item.getCriteriaType(), item.getValue(), null));
                }
            }
        }
        return queryCriteria;
    }

    @SuppressWarnings("unchecked")
    private static Criterion assembleCriterion(String columnName, CriteriaType criteriaType, Object value, String nativeSql) {
        Criterion criterion = null;
        switch (criteriaType) {
        case Equal:
            criterion = Restrictions.eq(columnName, value);
            break;
        case NotEqual:
            criterion = Restrictions.ne(columnName, value);
            break;
        case Like:
            criterion = Restrictions.like(columnName, value.toString());
            break;
        case Greaterthan:
            criterion = Restrictions.gt(columnName, value);
            break;
        case Lessthan:
            criterion = Restrictions.lt(columnName, value);
            break;
        case LessthanOrEqual:
            criterion = (Restrictions.le(columnName, value));
            break;
        case In:
            if (ObjectUtils.isArray(value)) {
                Object[] values = ObjectUtils.toObjectArray(value);
                criterion = Restrictions.in(columnName, values);
            } else if (value instanceof Collection) {
                Collection values = (Collection) value;
                criterion = Restrictions.in(columnName, values);
            }
            break;
        case IsNull:
            criterion = Restrictions.isNull(columnName);
            break;
        case IsNotNull:
            criterion = Restrictions.isNotNull(columnName);
            break;
        case Between:
            if (value instanceof BetweenParameter) {
                BetweenParameter betweenParamaeter = (BetweenParameter) value;
                criterion = Restrictions.between(columnName, betweenParamaeter.getLoValue(), betweenParamaeter.getHiValue());
            } else if (ObjectUtils.isArray(value)) {
                Object[] values = ObjectUtils.toObjectArray(value);
                if (values.length != 2)
                    throw new RuntimeException("Parameter for between is illagal");
                criterion = Restrictions.between(columnName, values[0], values[1]);
            } else if (value instanceof Collection) {
                Collection values = (Collection) value;
                criterion = Restrictions.between(columnName, values.toArray()[0], values.toArray()[1]);
            }
            break;
        case NativeSql:
            // String nativeSql=value.toString();
            if (null != nativeSql) {
                int palceHolderCount = StringUtils.countMatches(nativeSql, STATEMENT_PARAMETER_PLACEHOLDER);
                if (palceHolderCount > 0) {
                    String[] names = new String[palceHolderCount];
                    Object[] values = new Object[palceHolderCount];
                    for (int index = 0; index < palceHolderCount; index++) {
                        names[index] = columnName;
                        values[index] = value;
                    }
                    criterion = Restrictions.sqlRestriction(nativeSql, names, values);
                } else {
                    criterion = Restrictions.sqlRestriction(nativeSql);
                }
            }
            break;
        case Ignore:
            break;
        }

        return criterion;
    }

    @SuppressWarnings("unchecked")
    public static QueryCriteria toQueryCriteria(CriteriaParameter criteriaParameter) {
        QueryCriteria queryCriteria = new QueryCriteria();

        Class<?> clazz = criteriaParameter.getClass();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                if (shouldExcludeProperty(clazz, prop)) {
                    continue;
                }
                String name = prop.getName();
                Method method = prop.getReadMethod();

                if (null != method) {
                    Object value = method.invoke(criteriaParameter, new Object[0]);
                    if (null != value) {

                        // TODO: Just use for test,this code fragment will be
                        // dropped when all the annotaions have been setted
                        if (NumberUtils.isNumber(value.toString())) {
                            double doubleValue = Double.parseDouble(String.valueOf(value));
                            if (doubleValue < 0.000001)
                                continue;
                        }

                        if (value instanceof Boolean) {
                            boolean b = (Boolean) value;
                            if (b) {
                                value = 1;
                            } else {
                                value = 0;
                            }
                        }

                        Criteria criteria = method.getAnnotation(Criteria.class);
                        if (null != criteria) {
                            CriteriaType criteriaType = criteria.type();
                            String columnName = criteria.columnName();
                            if (null != columnName && columnName.equals(Criteria.BY_DEFAULT_RULE))
                                columnName = name;
                            switch (criteriaType) {
                            case Equal:
                                queryCriteria.add(Restrictions.eq(columnName, value));
                                break;
                            case NotEqual:
                                queryCriteria.add(Restrictions.ne(columnName, value));
                                break;
                            case Like:
                                queryCriteria.add(Restrictions.like(columnName, value.toString()));
                                break;
                            case Greaterthan:
                                queryCriteria.add(Restrictions.gt(columnName, value));
                                break;
                            case Lessthan:
                                queryCriteria.add(Restrictions.lt(columnName, value));
                                break;
                            case LessthanOrEqual:
                                queryCriteria.add(Restrictions.le(columnName, value));
                                break;
                            case In:
                                if (ObjectUtils.isArray(value)) {
                                    Object[] values = ObjectUtils.toObjectArray(value);
                                    queryCriteria.add(Restrictions.in(columnName, values));
                                } else if (value instanceof Collection) {
                                    Collection values = (Collection) value;
                                    queryCriteria.add(Restrictions.in(columnName, values));
                                }
                                break;
                            case IsNull:
                                queryCriteria.add(Restrictions.isNull(columnName));
                                break;
                            case IsNotNull:
                                queryCriteria.add(Restrictions.isNotNull(columnName));
                                break;
                            case Between:
                                if (value instanceof BetweenParameter) {
                                    BetweenParameter betweenParamaeter = (BetweenParameter) value;
                                    queryCriteria.add(Restrictions.between(columnName, betweenParamaeter.getLoValue(), betweenParamaeter.getHiValue()));
                                } else if (ObjectUtils.isArray(value)) {
                                    Object[] values = ObjectUtils.toObjectArray(value);
                                    if (values.length != 2)
                                        throw new RuntimeException("Parameter for between is illagal");
                                    queryCriteria.add(Restrictions.between(columnName, values[0], values[1]));
                                } else if (value instanceof Collection) {
                                    Collection values = (Collection) value;
                                    queryCriteria.add(Restrictions.between(columnName, values.toArray()[0], values.toArray()[1]));
                                }
                                break;
                            case NativeSql:
                                String nativeSql = criteria.value();
                                if (null != nativeSql) {
                                    int palceHolderCount = StringUtils.countMatches(nativeSql, STATEMENT_PARAMETER_PLACEHOLDER);
                                    if (palceHolderCount > 0) {
                                        String[] names = new String[palceHolderCount];
                                        Object[] values = new Object[palceHolderCount];
                                        for (int index = 0; index < palceHolderCount; index++) {
                                            names[index] = columnName;
                                            values[index] = value;
                                        }
                                        queryCriteria.add(Restrictions.sqlRestriction(nativeSql, names, values));
                                    } else {
                                        queryCriteria.add(Restrictions.sqlRestriction(nativeSql));
                                    }
                                }
                                break;
                            case Ignore:
                                break;
                            }

                        } else {
                            queryCriteria.add(Restrictions.eq(name, value));
                        }

                    }

                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return queryCriteria;
    }

    public static MapParameter toMapParameter(CriteriaParameter criteriaParameter) {
        MapParameter mapParameter = new MapParameter();
        //
        Class<?> clazz = criteriaParameter.getClass();
        try {

            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();

            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];

                if (shouldExcludeProperty(clazz, prop)) {
                    continue;
                }

                String name = prop.getName();
                Method method = prop.getReadMethod();
                if (null != method) {

                    Object value = method.invoke(criteriaParameter, new Object[0]);

                    if (null != value) {

                        String stringValue = asString(value);

                        // TODO: Just use for test,this code fragment will be
                        // dropped when all the annotaion has been setted

                        if (notEmpty(stringValue)) {
                            // Parameter parameter =
                            // method.getAnnotation(Parameter.class);
                            Criteria criteria = method.getAnnotation(Criteria.class);
                            if (null != criteria) {
                                CriteriaType criteriaType = criteria.type();
                                if (null != criteriaType && CriteriaType.Ignore.equals(criteriaType))
                                    continue;
                            }

                            if (prop.getPropertyType().equals(String.class)) {
                                mapParameter.put(name, value);
                            } else {
                                if (NumberUtils.isNumber(stringValue)) {
                                    double doubleValue = Double.parseDouble(stringValue);

                                    if (doubleValue < 0.000001) {
                                        continue;
                                    } else {
                                        mapParameter.put(name, value);
                                    }
                                } else {
                                    mapParameter.put(name, value);
                                }
                            }

                        }

                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mapParameter;
    }

    public static Object[] parameters() {
        return new Object[0];
    }

    // protected static MapParameter oldToMapParameter(CriteriaParameter
    // criteriaParameter) {
    //
    // MapParameter mapParameter = new MapParameter();
    //
    // try {
    // Method[] methods = criteriaParameter.getClass().getMethods();
    //
    // if (null != methods) {
    // // Map<String, String> fieldNames = new HashMap<String,
    // // String>();
    // // String fieldName = null;
    // for (Method method : methods) {
    //
    // String fieldName = null;
    //
    // String methodName = method.getName();
    // if (methodName.startsWith("get")) {
    // fieldName = methodName.substring(3);
    // } else if (methodName.startsWith("is")) {
    // fieldName = methodName.substring(2);
    // }
    // if (null != fieldName) {
    // fieldName = (char) (fieldName.charAt(0) + 32) + fieldName.substring(1);
    // // fieldNames.put(fieldName.toUpperCase(), fieldName);
    //
    // if (shouldExcludeProperty(fieldName)) {
    // continue;
    // }
    //
    // Object value = method.invoke(criteriaParameter, parameters());
    // if (null != value) {
    //
    // Parameter parameter = method.getAnnotation(Parameter.class);
    // if (null != parameter) {
    // ParameterType parameterType = parameter.value();
    // if (ParameterType.Positive.equals(parameterType)) {
    // double doubleValue = Double.parseDouble(String.valueOf(value));
    // if (doubleValue > 0) {
    // mapParameter.put(fieldName, value);
    // }
    // }
    // } else {
    // mapParameter.put(fieldName, value);
    // }
    // }
    // }
    // }
    // }
    // } catch (Exception e) {
    // // log.error("--class not found: " + e.getMessage());// throw new
    // // NestedException(e);
    // }
    // return mapParameter;
    // }

    // private static boolean shouldExcludeProperty(String fieldName) {
    //
    // if (fieldName.equals("class") || fieldName.equals("declaringClass") ||
    // fieldName.equals("cachedSuperClass") ||
    // fieldName.equals("metaClass")) {
    // return true;
    // }
    //
    // return false;
    // }

    private static boolean shouldExcludeProperty(Class<?> clazz, PropertyDescriptor prop) {
        String name = prop.getName();

        if (name.equals("class") || name.equals("declaringClass") || name.equals("cachedSuperClass") || name.equals("metaClass")) {
            return true;
        }

        return false;
    }

}

