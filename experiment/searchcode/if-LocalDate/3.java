package com.metabroadcast.common.persistence.translator;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.Timestamp;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class TranslatorUtils {
	
    public static String toString(Object objectId) {
        String value = null;
        if (objectId != null) {
            value = objectId.toString();
        }
        return value;
    }
    
    public static Double toDouble(DBObject object, String name) {
        if (object.containsField(name)) {
            return (Double) object.get(name);
        }
        return null;
    }
    
    public static Float toFloat(DBObject object, String name) {
        if (object.containsField(name)) {
            return ((Double) object.get(name)).floatValue();
        }
        return null;
    }
    
    public static Boolean toBoolean(DBObject object, String name) {
        if (object.containsField(name)) {
            return (Boolean) object.get(name);
        }
        return null;
    }
    
    public static Integer toInteger(DBObject object, String name) {
        if (object.containsField(name)) {
            return (Integer) object.get(name);
        }
        return null;
    }
    
    public static Long toLong(DBObject object, String name) {
        if (object.containsField(name)) {
            return (Long) object.get(name);
        }
        return null;
    }

    
    public static String toString(DBObject object, String name) {
        if (object.containsField(name)) {
            return (String) object.get(name);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> toSet(DBObject object, String name) {
        if (object.containsField(name)) {
            List<String> dbValues = (List<String>) object.get(name);
            return Sets.newLinkedHashSet(dbValues);
        }
        return Sets.newLinkedHashSet();
    }

    @SuppressWarnings("unchecked")
    public static List<String> toList(DBObject object, String name) {
        if (object.containsField(name)) {
            return (List<String>) object.get(name);
        }
        return Lists.newArrayList();
    }
    
    @SuppressWarnings("unchecked")
    public static List<DateTime> toDateList(DBObject object, String name) {
        List<DateTime> dates = Lists.newArrayList();
        if (object.containsField(name)) {
            for (Object element: (List<Object>) object.get(name)) {
                Preconditions.checkArgument(element instanceof Date);
                dates.add(new DateTime((Date) element, DateTimeZones.UTC));
            }
        }
        return dates;
    }
    
    public static DBObject toDBObject(DBObject object, String name) {
        if (object.containsField(name)) {
            return (DBObject) object.get(name);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static List<DBObject> toDBObjectList(DBObject object, String name) {
        if (object.containsField(name)) {
            return (List<DBObject>) object.get(name);
        }
        return Lists.newArrayList();
    }

    public static DateTime toDateTime(DBObject dbObject, String name) {
        if (dbObject.containsField(name)) {
            return new DateTime((Date) dbObject.get(name), DateTimeZones.UTC);
        }
        return null;
    }
    
    public static LocalDate toLocalDate(DBObject dbObject, String name) {
        if (dbObject.containsField(name)) {
            return new LocalDate(dbObject.get(name));
        }
        return null;
    }

    public static void fromSet(DBObject dbObject, Set<String> set, String name) {
        if (!set.isEmpty()) {
            BasicDBList values = new BasicDBList();
            for (String value : set) {
                if (value != null) {
                    values.add(value);
                }
            }
            dbObject.put(name, values);
        }
    }
    
    public static void fromIterable(DBObject dbObject, Iterable<String> iterable, String name) {
        if (!Iterables.isEmpty(iterable)) {
            BasicDBList values = new BasicDBList();
            for (String value : iterable) {
                if (value != null) {
                    values.add(value);
                }
            }
            dbObject.put(name, values);
        }
    }

    public static void fromList(DBObject dbObject, Collection<String> list, String name) {
        if (!list.isEmpty()) {
            BasicDBList values = new BasicDBList();
            for (String value : list) {
                if (value != null) {
                    values.add(value);
                }
            }
            dbObject.put(name, values);
        }
    }
    
    public static void fromDateList(DBObject dbObject, Collection<DateTime> list, String name) {
        if (!list.isEmpty()) {
            BasicDBList values = new BasicDBList();
            for (DateTime value : list) {
                if (value != null) {
                    values.add(value.toDateTime(DateTimeZones.UTC).toDate());
                }
            }
            dbObject.put(name, values);
        }
    }

    public static void fromDateTime(DBObject dbObject, String name, DateTime dateTime) {
        if (dateTime != null) {
            dbObject.put(name, dateTime.toDateTime(DateTimeZones.UTC).toDate());
        }
    }
    
    public static void fromLocalDate(DBObject dbObject, String name, LocalDate localDate) {
        if (localDate != null) {
            dbObject.put(name, localDate.toString());
        }
    }

    public static void from(DBObject dbObject, String name, Object value) {
        if (value != null) {
            dbObject.put(name, value);
        }
    }
    
    public static DateTimeZone toTimeZone(DBObject dbo, String name) {
        if (dbo.containsField(name)) {
            return DateTimeZone.forID((String) dbo.get(name));
        }
        return null;
    }
    
    public static void fromTimeZone(DBObject dbo, String name, DateTimeZone value) {
        if (value != null) {
            dbo.put(name, value.getID());
        }
    }

	public static Locale toLocale(DBObject dbo, String name) {
		if (dbo.containsField(name)) {
			return new Locale((String) dbo.get(name));
		}
		return null;
	}
	
	public static void fromLocale(DBObject dbo, String name, Locale locale) {
		if (locale != null) {
			dbo.put(name, locale.getLanguage());
		}
	}

	public static void fromTimestamp(DBObject dbo, String name, Timestamp timestamp) {
		if (timestamp != null) {
			dbo.put(name, timestamp.millis());
		}
	}
	
	public static Timestamp toTimestamp(DBObject dbo, String name) {
		if (dbo.containsField(name)) {
			return Timestamp.of(toLong(dbo, name));
		}
		return null;
	}
}

