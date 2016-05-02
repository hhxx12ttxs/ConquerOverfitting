/*
 * Copyright 2010-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mongodb.core.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.mongodb.InvalidMongoDbApiUsageException;
import org.springframework.data.mongodb.core.geo.Circle;
import org.springframework.data.mongodb.core.geo.Point;
import org.springframework.data.mongodb.core.geo.Shape;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Criteria implements CriteriaDefinition {

	/**
	 * Custom "not-null" object as we have to be able to work with {@literal null} values as well.
	 */
	private static final Object NOT_SET = new Object();

	private String key;

	private List<Criteria> criteriaChain;

	private LinkedHashMap<String, Object> criteria = new LinkedHashMap<String, Object>();

	private Object isValue = NOT_SET;

	public Criteria(String key) {
		this.criteriaChain = new ArrayList<Criteria>();
		this.criteriaChain.add(this);
		this.key = key;
	}

	protected Criteria(List<Criteria> criteriaChain, String key) {
		this.criteriaChain = criteriaChain;
		this.criteriaChain.add(this);
		this.key = key;
	}

	/**
	 * Static factory method to create a Criteria using the provided key
	 * 
	 * @param key
	 * @return
	 */
	public static Criteria where(String key) {
		return new Criteria(key);
	}

	/**
	 * Static factory method to create a Criteria using the provided key
	 * 
	 * @param key
	 * @return
	 */
	public Criteria and(String key) {
		return new Criteria(this.criteriaChain, key);
	}

	/**
	 * Creates a criterion using equality
	 * 
	 * @param o
	 * @return
	 */
	public Criteria is(Object o) {
		if (isValue != NOT_SET) {
			throw new InvalidMongoDbApiUsageException(
					"Multiple 'is' values declared. You need to use 'and' with multiple criteria");
		}
		if (this.criteria.size() > 0 && "$not".equals(this.criteria.keySet().toArray()[this.criteria.size() - 1])) {
			throw new InvalidMongoDbApiUsageException("Invalid query: 'not' can't be used with 'is' - use 'ne' instead.");
		}
		this.isValue = o;
		return this;
	}

	/**
	 * Creates a criterion using the $ne operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria ne(Object o) {
		criteria.put("$ne", o);
		return this;
	}

	/**
	 * Creates a criterion using the $lt operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria lt(Object o) {
		criteria.put("$lt", o);
		return this;
	}

	/**
	 * Creates a criterion using the $lte operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria lte(Object o) {
		criteria.put("$lte", o);
		return this;
	}

	/**
	 * Creates a criterion using the $gt operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria gt(Object o) {
		criteria.put("$gt", o);
		return this;
	}

	/**
	 * Creates a criterion using the $gte operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria gte(Object o) {
		criteria.put("$gte", o);
		return this;
	}

	/**
	 * Creates a criterion using the $in operator
	 * 
	 * @param o the values to match against
	 * @return
	 */
	public Criteria in(Object... o) {
		if (o.length > 1 && o[1] instanceof Collection) {
			throw new InvalidMongoDbApiUsageException("You can only pass in one argument of type "
					+ o[1].getClass().getName());
		}
		criteria.put("$in", o);
		return this;
	}

	/**
	 * Creates a criterion using the $in operator
	 * 
	 * @param c the collection containing the values to match against
	 * @return
	 */
	public Criteria in(Collection<?> c) {
		criteria.put("$in", c.toArray());
		return this;
	}

	/**
	 * Creates a criterion using the $nin operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria nin(Object... o) {
		criteria.put("$nin", o);
		return this;
	}

	/**
	 * Creates a criterion using the $mod operator
	 * 
	 * @param value
	 * @param remainder
	 * @return
	 */
	public Criteria mod(Number value, Number remainder) {
		List<Object> l = new ArrayList<Object>();
		l.add(value);
		l.add(remainder);
		criteria.put("$mod", l);
		return this;
	}

	/**
	 * Creates a criterion using the $all operator
	 * 
	 * @param o
	 * @return
	 */
	public Criteria all(Object... o) {
		criteria.put("$all", o);
		return this;
	}

	/**
	 * Creates a criterion using the $size operator
	 * 
	 * @param s
	 * @return
	 */
	public Criteria size(int s) {
		criteria.put("$size", s);
		return this;
	}

	/**
	 * Creates a criterion using the $exists operator
	 * 
	 * @param b
	 * @return
	 */
	public Criteria exists(boolean b) {
		criteria.put("$exists", b);
		return this;
	}

	/**
	 * Creates a criterion using the $type operator
	 * 
	 * @param t
	 * @return
	 */
	public Criteria type(int t) {
		criteria.put("$type", t);
		return this;
	}

	/**
	 * Creates a criterion using the $not meta operator which affects the clause directly following
	 * 
	 * @return
	 */
	public Criteria not() {
		criteria.put("$not", null);
		return this;
	}

	/**
	 * Creates a criterion using a $regex
	 * 
	 * @param re
	 * @return
	 */
	public Criteria regex(String re) {
		criteria.put("$regex", re);
		return this;
	}

	/**
	 * Creates a criterion using a $regex and $options
	 * 
	 * @param re
	 * @param options
	 * @return
	 */
	public Criteria regex(String re, String options) {
		criteria.put("$regex", re);
		if (StringUtils.hasText(options)) {
			criteria.put("$options", options);
		}
		return this;
	}

	/**
	 * Creates a geospatial criterion using a $within $center operation. This is only available for Mongo 1.7 and higher.
	 * 
	 * @param circle must not be {@literal null}
	 * @return
	 */
	public Criteria withinSphere(Circle circle) {
		Assert.notNull(circle);
		criteria.put("$within", new BasicDBObject("$centerSphere", circle.asList()));
		return this;
	}

	public Criteria within(Shape shape) {

		Assert.notNull(shape);
		criteria.put("$within", new BasicDBObject(shape.getCommand(), shape.asList()));
		return this;
	}

	/**
	 * Creates a geospatial criterion using a $near operation
	 * 
	 * @param point must not be {@literal null}
	 * @return
	 */
	public Criteria near(Point point) {
		Assert.notNull(point);
		criteria.put("$near", point.asList());
		return this;
	}

	/**
	 * Creates a geospatial criterion using a $nearSphere operation. This is only available for Mongo 1.7 and higher.
	 * 
	 * @param point must not be {@literal null}
	 * @return
	 */
	public Criteria nearSphere(Point point) {
		Assert.notNull(point);
		criteria.put("$nearSphere", point.asList());
		return this;
	}

	/**
	 * Creates a geospatical criterion using a $maxDistance operation, for use with $near
	 * 
	 * @param maxDistance
	 * @return
	 */
	public Criteria maxDistance(double maxDistance) {
		criteria.put("$maxDistance", maxDistance);
		return this;
	}

	/**
	 * Creates a criterion using the $elemMatch operator
	 * 
	 * @param c
	 * @return
	 */
	public Criteria elemMatch(Criteria c) {
		criteria.put("$elemMatch", c.getCriteriaObject());
		return this;
	}

	/**
	 * Creates an or query using the $or operator for all of the provided queries
	 * 
	 * @param queries
	 */
	public void or(List<Query> queries) {
		criteria.put("$or", queries);
	}

	public String getKey() {
		return this.key;
	}

	/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.datastore.document.mongodb.query.Criteria#
		 * getCriteriaObject(java.lang.String)
		 */
	public DBObject getCriteriaObject() {
		if (this.criteriaChain.size() == 1) {
			return criteriaChain.get(0).getSingleCriteriaObject();
		} else {
			DBObject criteriaObject = new BasicDBObject();
			for (Criteria c : this.criteriaChain) {
				criteriaObject.putAll(c.getSingleCriteriaObject());
			}
			return criteriaObject;
		}
	}

	protected DBObject getSingleCriteriaObject() {
		DBObject dbo = new BasicDBObject();
		boolean not = false;
		for (String k : this.criteria.keySet()) {
			if (not) {
				DBObject notDbo = new BasicDBObject();
				notDbo.put(k, this.criteria.get(k));
				dbo.put("$not", notDbo);
				not = false;
			} else {
				if ("$not".equals(k)) {
					not = true;
				} else {
					dbo.put(k, this.criteria.get(k));
				}
			}
		}
		DBObject queryCriteria = new BasicDBObject();
		if (isValue != NOT_SET) {
			queryCriteria.put(this.key, this.isValue);
			queryCriteria.putAll(dbo);
		} else {
			queryCriteria.put(this.key, dbo);
		}
		return queryCriteria;
	}

}

