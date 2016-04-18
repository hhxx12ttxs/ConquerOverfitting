package com.gridpoint.energy.domainmodel;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author dhorlick
 */
public class CustomSchedules
{
    private Map<String, Set<CustomHours>> customHoursSetByType = new HashMap<String, Set<CustomHours>>();

    public Map<String, Set<CustomHours>> getCustomHoursSetByType()
    {
        return customHoursSetByType;
    }

    public void setCustomHoursSetByType(final Map<String, Set<CustomHours>> customHoursSetByType)
    {
        this.customHoursSetByType = customHoursSetByType;
    }

    /**
     * @param storeHourType typically "Main" or "Alternate"
     */
    public void store(final String storeHourType, final CustomHours customHours)
    {
        if (storeHourType==null || customHours==null)
            return;

        if (customHoursSetByType.containsKey(storeHourType))
        {
            final Set<CustomHours> customHoursSet = customHoursSetByType.get(storeHourType);

            final Iterator<CustomHours> existingCustomHoursIterator = customHoursSet.iterator();
            while (existingCustomHoursIterator.hasNext())
            {
                final CustomHours existingCustomHours = existingCustomHoursIterator.next();
                if (customHours.getUniqueName().equals(existingCustomHours.getUniqueName()))
                    existingCustomHoursIterator.remove();
            }

            customHoursSet.add(customHours);
        }
        else
        {
            final Set<CustomHours> set = new HashSet<CustomHours> ();
            set.add(customHours);
            customHoursSetByType.put(storeHourType, set);
        }
    }

    /**
     * @return The corresponding CustomHours, or null if none could be found.
     */
    public CustomHours findByUniqueName(final String storeHourType, final String uniqueCustomHoursName)
    {
        if (customHoursSetByType.containsKey(storeHourType))
        {
            final Set<CustomHours> customHoursSet = customHoursSetByType.get(storeHourType);
            for (final CustomHours customHours : customHoursSet)
            {
                if (uniqueCustomHoursName.equals(customHours.getUniqueName()))
                {
                    return customHours;
                }
            }
        }

        return null;
    }

    /**
     * @param storeHourType typically "Main" or "Alternate"
     * @param displayCustomHoursName example: "Christmas" or "Other"
     * @return the corresponding CustomHours, or null if none could be found.
     */
    public Set<CustomHours> findByDisplayName(final String storeHourType, final String displayCustomHoursName)
    {
        final Set<CustomHours> customHourses = new HashSet<CustomHours> ();

        if (customHoursSetByType.containsKey(storeHourType))
        {
            final Set<CustomHours> customHoursSet = customHoursSetByType.get(storeHourType);
            for (final CustomHours customHours : customHoursSet)
            {
                if (displayCustomHoursName.equals(customHours.getDisplayName()))
                {
                    customHourses.add(customHours);
                }
            }
        }

        return customHourses;
    }

    public void overlayFrom(CustomSchedules moreRecentCustomSchedules)
    {

        for (final Map.Entry<String, Set<CustomHours>> entry
                    : moreRecentCustomSchedules.getCustomHoursSetByType().entrySet())
        {
            final String storeHoursType = entry.getKey();

            if (!customHoursSetByType.containsKey(storeHoursType))
            {
                customHoursSetByType.put(storeHoursType, entry.getValue());
            }
            else
            {
                for (final CustomHours moreRecentCustomHours : entry.getValue())
                {
                    store(storeHoursType, moreRecentCustomHours);
                }
            }
        }
    }

    public Set<String> siteHoursTypes()
    {
        return customHoursSetByType.keySet();
    }

    public int countCustomHours()
    {
        int count = 0;

        for (final Map.Entry<String, Set<CustomHours>> entry : customHoursSetByType.entrySet())
        {
            count += entry.getValue().size();
        }

        return count;
    }

    /**
     * @return a two-element array, consisting of store hour type and unique custom name.
     */
    public static String[] firstStoreHourTypeAndUniqueNamePairFromXml(final Document doc)
    {
        try
        {
            final XPathExpression storeHourTypeExpr = XPathFactory.newInstance().newXPath().compile("//StoreHour[1]/@name");
            final String storeHourType = (String) storeHourTypeExpr.evaluate(doc, XPathConstants.STRING);

            final XPathExpression uniqueNameExpr = XPathFactory.newInstance().newXPath().compile("//Custom[1]/@name");
            final String uniqueName = (String) uniqueNameExpr.evaluate(doc, XPathConstants.STRING);

            return new String[] {storeHourType, uniqueName};
        }
        catch (XPathExpressionException e)
        {
            throw new IllegalStateException(e); // bad xpath?
        }
    }

    public boolean remove(final String storeHourType, final String uniqueCustomName)
    {
        if (customHoursSetByType.containsKey(storeHourType))
        {
            final Set<CustomHours> customHoursSet = customHoursSetByType.get(storeHourType);

            final Iterator<CustomHours> existingCustomHoursIterator = customHoursSet.iterator();
            while (existingCustomHoursIterator.hasNext())
            {
                final CustomHours existingCustomHours = existingCustomHoursIterator.next();
                if (uniqueCustomName.equals(existingCustomHours.getUniqueName()))
                {
                    existingCustomHoursIterator.remove();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.reflectionToString(this);
    }
}

