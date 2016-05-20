/*
 * InLab Software Hikari Framework
 *
 * Copyright (c) 2009, InLab Software, LLC. All rights reserved.
 * Use is subject to license terms.
 *
 * http://www.inlabsoft.com/products/hikari/license
 */
package com.inlabsoft.hikari.localizer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.inlabsoft.hikari.core.ServiceOrchestrator;
import com.inlabsoft.hikari.core.bean.AbstractObject;
import com.inlabsoft.hikari.core.bean.annotation.Instance;
import com.inlabsoft.hikari.core.util.Any;
import com.inlabsoft.hikari.core.util.Objects;
import com.inlabsoft.hikari.core.util.Strings;

/**
 * The <code>EnumeratedInstance</code> class is a skeleton for any managed enumeration, where
 * enumeration can be extended by any program and used by it's own. It is useful when describing
 * categories or types.
 *
 * @author  Andrey Ochirov
 * @version 1.0
 */
@Instance
public abstract class EnumeratedInstance<T extends EnumeratedInstance<T>>
    extends AbstractObject<T> implements Comparable<T>, Serializable {

    /**
     * The cache of constants.
     */
    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Set<EnumeratedInstance>> constantsCache =
        new HashMap<Class<?>, Set<EnumeratedInstance>>();

    /**
     * The class finger print that is set to indicate serialization compatibility with a previous
     * version of the class.
     */
    private static final long serialVersionUID = -6516307682350680528L;

    /**
     * Parse specified value to an enumerated instance.
     * <p>
     * The argument value may consist of either a name or an integer code.
     * <p>
     * For example:
     * <ul>
     * <li> "SYSTEM"
     * <li> "100"
     * </ul>
     *
     * @param   value value to be parsed.
     * @param   clazz an enumerated instance class.
     * @return  The parsed value. Passing an integer that corresponds to a known name (eg 100) will
     *          return the associated name (eg <CODE>SYSTEM</CODE>).
     *
     * @throws  IllegalArgumentException if the value is not valid. Valid values are integers
     *          between {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}, and all known names
     *          in the enumerated instance class.
     */
    @SuppressWarnings("unchecked")
    public static final <T extends EnumeratedInstance> T parse(Object value, Class<T> clazz)
            throws IllegalArgumentException {
        return parse(value, clazz, Locale.getDefault());
    }

    /**
     * Parse specified value to an enumerated instance.
     * <p>
     * The argument value may consist of either a name or an integer code.
     * <p>
     * For example:
     * <ul>
     * <li> "SYSTEM"
     * <li> "100"
     * </ul>
     *
     * @param   value value to be parsed.
     * @param   clazz an enumerated instance class.
     * @param   locale locale for localized name.
     * @return  The parsed value. Passing an integer that corresponds to a known name (eg 100) will
     *          return the associated name (eg <CODE>SYSTEM</CODE>).
     *
     * @throws  IllegalArgumentException if the value is not valid. Valid values are integers
     *          between {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}, and all known
     *          names in the enumerated instance class.
     */
    @SuppressWarnings("unchecked")
    public static final <T extends EnumeratedInstance> T parse(Object value, Class<T> clazz,
            Locale locale) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("Cannot look for a constants in null value");
        }
        // looking for the set of constants in the cache
        Set<EnumeratedInstance> instances = constantsCache.get(clazz);

        // if such enumerated instance class not cached we just creating set of instances for such
        // class but without adding it to the cache (it will be done in case of non-empty set of
        // instances)
        if (instances == null) {
            instances = new HashSet<EnumeratedInstance>();
        }
        // looking for all 'public static final' fields in specified class that are instances of
        // specified class
        for (Field field : clazz.getFields()) {
            if (clazz.equals(field.getType()) && Modifier.isPublic(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())) {
                try {
                    instances.add((T)field.get(null));
                } catch (IllegalAccessException e) {
                    // in case of unexpected exception we just going to the next field (this is very
                    // very seldom case that truly hard to predict)
                    continue;
                }
            }
        }

        if (instances.size() == 0) {
            throw new IllegalStateException("Specified class ["
                + clazz.getName()
                + "] is not an enumerated instance class (no any instances found)");
        }
        constantsCache.put(clazz, instances);
        // cause value has non-specified type we looking here for different attributes of enumerated
        // instance
        //
        // first, we starting from instance code
        try {
            int code = Any.asInt(value);

            for (EnumeratedInstance instance : instances) {
                if (instance.getCode() == code) {
                    return (T)instance;
                }
            }
        } catch (Exception e) {
            // not an integer, drop through
        }

        // second, we looking for non-localized name
        String name = Any.asString(value, null);

        for (EnumeratedInstance instance : instances) {
            if (instance.getName().equals(name)) {
                return (T)instance;
            }
        }
        // finally, look for a known enumerated instance with the given localized name, in specified
        // locale
        //
        // this is relatively expensive, but not excessively so
        for (EnumeratedInstance instance : instances) {
            if (Objects.equals(instance.getLocalizableName().toString(locale), name)) {
                return (T)instance;
            }
        }
        // in case of unknown code (instead return null - to prevent unexpected NPE)
        throw new IllegalArgumentException("Unknown value ["
            + value
            + "] was specified for enumeration");
    }

    /**
     * The integer code of the enumeration element.
     */
    @Instance.Attribute
    protected final int code;

    /**
     * The aligned string representation of the integer code.
     */
    protected final String codeName;

    /**
     * The non-localized name of the enumeration element.
     */
    protected final String name;

    /**
     * The key for localized name of the enumeration element.
     */
    protected final String nameKey;

    /**
     * The resource bundle name to be used in localizing the enumeration element name.
     */
    protected final String resourceBundleName;

    /**
     * Create a named enumeration element with a given integer code.
     * <p>
     * Note that this constructor is "protected" to allow subclassing.
     *
     * @param   code an integer code for the enumeration element.
     * @param   name the name of the enumeration element, for example "ONE".
     */
    protected EnumeratedInstance(int code, String name) {
        this(code, name, Strings.EMPTY_STRING);
    }

    /**
     * Create a named enumeration element with a given integer code and a given localization
     * resource name.
     * <p>
     * Note that this constructor is "protected" to allow subclassing.
     *
     * @param   code an integer code for the enumeration element.
     * @param   name the name of the enumeration element, for example "ONE".
     * @param   resourceBundleName name of a resource bundle to use in localizing the given name. If
     *          the resourceBundleName is <code>null</code> or an empty string, it is ignored.
     */
    protected EnumeratedInstance(int code, String name, String resourceBundleName) {
        this.code = code;
        this.codeName = Strings.leadLeft(Integer.toString(code), '0', 6);
        this.name = name;
        this.nameKey = (resourceBundleName == null
            ? Strings.EMPTY_STRING : resourceBundleName) + "#" + name;
        this.resourceBundleName = resourceBundleName;
    }

    /**
     * Compares specified <code>EnumeratedInstance</code> with this one.
     */
    public int compareTo(T o) {
        return (int)Math.signum(code - o.code);
    }

    /**
     * Returns the integer code for this enumeration element. This integer code can be used for
     * efficient ordering comparisons between enumeration elements.
     *
     * @return  the integer code for this enumeration element.
     */
    public final int getCode() {
        return code;
    }

    /**
     * Returns the localizable name of the enumeration element for custom localization in case when
     * system locale is not useful.
     *
     * @return  localizable name of the enumeration element.
     */
    public LocalizableMessage getLocalizableName() {
        return ServiceOrchestrator.lookupService(Localizer.class).getMessage(nameKey);
    }

    /**
     * Returns the localized string name of the enumeration element, for the current default locale.
     * <p>
     * If no localization information is available, the non-localized name is returned.
     *
     * @return  localized name of the enumeration element.
     */
    public String getLocalizedName() {
        try {
            return ServiceOrchestrator.lookupService(Localizer.class)
                .getMessage(nameKey).toString();
        } catch (Exception e) {
            return name;
        }
    }

    /**
     * Returns the non-localized string name of the enumeration element.
     *
     * @return  non-localized name of the enumeration element.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the enumeration element's localization resource bundle name, or <code>null</code> if
     * no localization bundle is defined.
     *
     * @return  localization resource bundle name.
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * Returns human-readable non-localized string representation of this enumerated instance.
     *
     * @return  the non-localized name of the Level, for example "INFO".
     */
    @Override
    public final String toString() {
        return "["
            + codeName
            + "] "
            + getLocalizedName();
    }

}
