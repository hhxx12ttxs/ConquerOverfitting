/**
 * Copyright 2011 InfoAsset AG
 */
package de.infoasset.platform.services.domains;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.infoasset.imf.util.ImfUtilities;
import de.infoasset.platform.services.Extension;
import de.infoasset.platform.services.Loggers;
import de.infoasset.platform.services.Services;
import de.infoasset.platform.services.asset.DomainValueProperty;
import de.infoasset.platform.services.asset.PersistentEntity;
import de.infoasset.platform.services.asset.PersistentSchema;
import de.infoasset.platform.services.asset.Property;
import de.infoasset.platform.util.FieldFinder;
import de.infoasset.platform.util.FilterIterable;

public class Domain extends Extension {

    public void init() {
        allDomainValues.clear();
        try {
            if (key == null) {
                key = ImfUtilities.getShortClassName(this.getClass().getName());
            }
            for (Field f : FieldFinder.getFields(this, DomainValue.class)) {
                final DomainValue dv = (DomainValue) f.get(null);
                String valueKey = f.getName();
                initDomainValue(dv, valueKey);
            }
            for (DomainValueAsset dva : DomainValueAsset.getValuesByDomain(this)) {
                String valueKey = dva.key.get();
                if (getDomainValueByKey(valueKey) == null) {
                    DomainValue dv = new DomainValue(null);
                    initDomainValue(dv, valueKey);
                }
            }
        } catch (Exception ex) {
            Loggers.serviceLog.error("Failed to init domain", ex);
        }
    }

    private void initDomainValue(DomainValue dv, String valueKey) {
        dv.key = valueKey;
        dv.domain = this;
        dv.init();
        allDomainValues.add(dv);
    }

    private List<DomainValue> topDomainValues = new ArrayList<DomainValue>();

    private List<DomainValue> allDomainValues = new ArrayList<DomainValue>();

    String key;

    /**
     * Return the immutable name that identifies this domain in the set of
     * domains.
     */
    public String getKey() {
        return key;
    }

    public Iterator<DomainValue> getTopDomainValuesIterator() {
        return topDomainValues.iterator();
    }

    public Iterable<DomainValue> getTopDomainValues() {
        return topDomainValues;
    }

    public Iterator<DomainValue> getAllDomainValuesIterator() {
        return allDomainValues.iterator();
    }

    public Iterable<DomainValue> getAllDomainValues() {
        return allDomainValues;
    }

    public DomainValue getDomainValueByKey(String key) {
        for (DomainValue dv : allDomainValues) {
            if (dv.key.equals(key)) {
                return dv;
            }
        }
        return null;
    }

    public String getValueName(String domainValueKey) {
        DomainValue domainValue = getDomainValueByKey(domainValueKey);
        if (domainValue != null) {
            return domainValue.getName();
        } else {
            return null;
        }
    }

    public String getName() {
        return getKey();
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Domain other = (Domain) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    boolean hasField(String string) {
        for (Field f : FieldFinder.getFields(this, DomainValue.class)) {
            if (string.equals(f.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<DomainValueProperty> getAllProperties() {
        List<DomainValueProperty> result = new ArrayList<DomainValueProperty>();
        for (PersistentSchema<? extends PersistentEntity> as : Services.INSTANCE().getPersistentSchemas()) {
            for (Property p : new FilterIterable<Property>(as.prototype().getProperties()) {

                @Override
                public boolean filter(Property o) {
                    if (o instanceof DomainValueProperty) {
                        DomainValueProperty dvp = (DomainValueProperty) o;
                        if (dvp.getDomain().equals(Domain.this)) {
                            return true;
                        }
                    }
                    return false;
                }
            }) {
                result.add((DomainValueProperty) p);
            }
        }
        return result;
    }
}

