package com.metabroadcast.common.persistence.mongo;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ELEM_MATCH;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.GREATER_THAN;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.GREATER_THAN_OR_EQUAL_TO;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.IN;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.NOT_IN;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.LESS_THAN;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.LESS_THAN_OR_EQUAL_TO;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.OR;

import java.util.Arrays;
import java.util.Collections;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoQueryBuilder {

	private static final Function<MongoQueryBuilder, DBObject> BUILD = new Function<MongoQueryBuilder, DBObject>() {

		@Override
		public DBObject apply(MongoQueryBuilder builder) {
			return builder.build();
		}
	};
	
	private DBObject query = new BasicDBObject();
    private DBObject select= new BasicDBObject();
    
	public MongoQueryBuilder or(MongoQueryBuilder... disjuncts) {
		query.put(OR, toMongoList(Iterables.transform(Arrays.asList(disjuncts), BUILD)));
		return this;
	}
	
	public MongoQueryBuilder idEquals(String value) {
		return fieldEquals(ID, value);
	}
	
	public MongoQueryBuilder fieldEquals(String field, String value) {
		query.put(field, value);
		return this;
	}
	
	public MongoQueryBuilder fieldEquals(String field, boolean value) {
		query.put(field, value);
		return this;
	}
	
	public MongoQueryBuilder fieldEquals(String field, int value) {
		query.put(field, value);
		return this;
	}
	
	public MongoQueryBuilder idIn(Iterable<String> ids) {
		return fieldIn(ID, ids);
	}
	
	public MongoQueryBuilder fieldNotIn(String field, Iterable<String> ids) {
        query.put(field, new BasicDBObject(NOT_IN, toMongoList(ids)));
        return this;
    }
	
	public MongoQueryBuilder fieldIn(String field, Iterable<String> ids) {
		query.put(field, new BasicDBObject(IN, toMongoList(ids)));
		return this;
	}
	
	public MongoQueryBuilder intFieldIn(String field, Iterable<Integer> ids) {
		query.put(field, new BasicDBObject(IN, toMongoList(ids)));
		return this;
	}
	
	public MongoQueryBuilder exists(String field) {
        query.put(field, new BasicDBObject(MongoConstants.EXISTS, true));
        return this;
    }
	
	public MongoQueryBuilder doesNotExist(String field) {
	    query.put(field, new BasicDBObject(MongoConstants.EXISTS, false));
	    return this;
	}
	
	public MongoQueryBuilder fieldLessThanOrEqualTo(String path, int value) {
		query.put(path, new BasicDBObject(MongoConstants.LESS_THAN_OR_EQUAL_TO, value));
		return this;
	}
	
	public MongoQueryBuilder fieldGreaterThanOrEqualTo(String path, int value) {
		query.put(path, new BasicDBObject(MongoConstants.GREATER_THAN_OR_EQUAL_TO, value));
		return this;
	}
	
	public MongoQueryBuilder fieldGreaterThan(String path, Object value) {
        query.put(path, new BasicDBObject(MongoConstants.GREATER_THAN, value));
        return this;
    }
	
	public MongoQueryBuilder fieldLessThan(String path, Object value) {
        query.put(path, new BasicDBObject(MongoConstants.LESS_THAN, value));
        return this;
    }
	
	public MongoQueryBuilder fieldAfterOrAt(String field, DateTime when) {
		return dateTimeConstraint(field, GREATER_THAN_OR_EQUAL_TO, when);
	}
	
	public MongoQueryBuilder fieldAfter(String field, DateTime when) {
		return dateTimeConstraint(field, GREATER_THAN, when);
	}
	
	public MongoQueryBuilder fieldBefore(String field, DateTime when) {
		return dateTimeConstraint(field, LESS_THAN, when);
	}
	
	public MongoQueryBuilder fieldBeforeOrAt(String field, DateTime when) {
		return dateTimeConstraint(field, LESS_THAN_OR_EQUAL_TO, when);
	}

	private MongoQueryBuilder dateTimeConstraint(String field, String op, DateTime when) {
		query.put(field, new BasicDBObject(op, when.toDate()));
		return this;
	}
	
	public MongoQueryBuilder elemMatch(String field, MongoQueryBuilder matches) {
		query.put(field, new BasicDBObject(ELEM_MATCH, matches.build()));
		return this;
	}
	
	public DBObject build() {
		return query;
	}
	
	private static BasicDBList toMongoList(Iterable<?> values) {
		BasicDBList list = new BasicDBList();
		list.addAll(Sets.newHashSet(values));
		return list;
	}
	
	public MongoQueryBuilder selecting(MongoSelectBuilder select) {
	    this.select = select.build();
	    return this;
	}
	
	public Iterable<DBObject> find(DBCollection collection) {
		DBCursor cur = collection.find(build(), select);
		if (cur == null) {
            return Collections.emptyList();
        }
		return cur;
	}
	
	public Iterable<DBObject> find(DBCollection collection, MongoSortBuilder sort) {
		Iterable<DBObject> cur = collection.find(query, select).sort(sort.build());
		if (cur == null) {
            return Collections.emptyList();
        }
		return cur;
	}
	
	public Iterable<DBObject> find(DBCollection collection, MongoSortBuilder sort, int limit) {
	    Iterable<DBObject> cur = collection.find(query, select).sort(sort.build()).limit(limit);
        if (cur == null) {
            return Collections.emptyList();
        }
        return cur;
	}
	
	public Iterable<DBObject> find(DBCollection collection, MongoSortBuilder sort, int limit, int page) {
	    int skip = (page > 0 ? (page-1)*limit : 0);
        Iterable<DBObject> cur = collection.find(query, select).sort(sort.build()).skip(skip).limit(limit);
        if (cur == null) {
            return Collections.emptyList();
        }
        return cur;
    }

	public MongoQueryBuilder mergeIn(MongoQueryBuilder builder) {
		query.putAll(builder.build());
		return this;
	}
	
	public MongoQueryBuilder inclusiveRange(String field, DateTime lower, DateTime upper) {
		return inclusiveRangeObjects(field, lower.toDate(), upper.toDate());
	}
		
	public MongoQueryBuilder inclusiveRange(String field, int lower, int upper) {
		return inclusiveRangeObjects(field, lower, upper);
	}

	private MongoQueryBuilder inclusiveRangeObjects(String field, Object lower, Object upper) {
		DBObject range = new BasicDBObject();
		range.put(MongoConstants.GREATER_THAN_OR_EQUAL_TO, lower);
		range.put(MongoConstants.LESS_THAN_OR_EQUAL_TO, upper);
		query.put(field, range);
		return this;
	}
}

