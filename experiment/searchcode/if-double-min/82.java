package dovetaildb.dbservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;


import dovetaildb.api.ApiException;
import dovetaildb.bytes.ArrayBytes;
import dovetaildb.bytes.Bytes;
import dovetaildb.bytes.CompoundBytes;
import dovetaildb.dbservice.DbResultLiterals.DbResultAtom;
import dovetaildb.dbservice.DbServiceUtil.PatternVisitor;
import dovetaildb.iter.EmptyIter;
import dovetaildb.iter.Iter;
import dovetaildb.iter.LiteralIter;
import dovetaildb.iter.SlicedIter;
import dovetaildb.util.Util;

public class ScoringDbService extends WrappingDbService {

	private static final long serialVersionUID = 403813283478930L;
	
	public ScoringDbService(DbService subService) {
		super(subService);
	}

	public String toString() {
		return "ScoringDbService("+subService+")";
	}
	
	public static abstract class ScoreComponentFactory {
		public abstract ScoreComponent make(List<Object> scoringOperation, Bytes prefix);
	}
	public static abstract class ScoreComponent {
		public abstract Iter narrowScoreRange(double minScore, double maxScore, Iter iter);
		public abstract double scoreObject(Object result, double minScore, double maxScore);
		public double score(DbResult result, double minScore, double maxScore) {
			return scoreObject(result.simplify(), minScore, maxScore);
		}
		public abstract double lowerBound();
		public abstract double upperBound();
	}

	public static final ConcurrentHashMap<String, ScoreComponentFactory> directory = new ConcurrentHashMap<String, ScoreComponentFactory>();
	static {
		directory.put("NumericScore", new NumericScoreComponentFactory());
		directory.put("ReverseScore", new ReverseScoreComponentFactory());
		directory.put("MultipliedScore", new MultipliedScoreComponentFactory());
		directory.put("LookupTableScore", new LookupTableScoreComponentFactory());
		directory.put("LinearlyInterpolatedScore", new LinearlyInterpolatedScoreComponentFactory());
		directory.put("SumScore", new SumScoreFactory());
	}
	
	public static ScoreComponentFactory lookupScoreFactory(String opName) {
		ScoreComponentFactory factory = directory.get(opName);
		if (factory != null) return factory;
		synchronized(directory) {
			// retry with lock
			factory = directory.get(opName);
			if (factory != null) return factory;
			Class<? extends ScoreComponentFactory> clas;
			try {
				clas = Class.forName(opName).asSubclass(ScoreComponentFactory.class);
			} catch (ClassNotFoundException e) {
				throw new ApiException("UnknownScorerFactory", "This scorer factory class was not found: \""+opName+"\"");
			}
			try {
				factory = clas.newInstance();
			} catch (InstantiationException e) {
				throw new ApiException("InvalidScorerFactory", "This scorer factory class could not be instanciated: \""+opName+"\"", e);
			} catch (IllegalAccessException e) {
				throw new ApiException("InvalidScorerFactory", "This scorer factory class could not be instanciated: \""+opName+"\"", e);
			}
			directory.put(opName, factory);
		}
		return factory;
	}
	
	public static abstract class LeafScoreComponentFactory extends ScoreComponentFactory {

		@Override
		public ScoreComponent make(List<Object> scoringOperation, Bytes prefix) {
			LeafScoreComponent component = make(scoringOperation);
			component.initFromBytes(prefix);
			return component;
		}
		
		public abstract LeafScoreComponent make(List<Object> scoringOperation);
		
	}
	
	public static abstract class LeafScoreComponent extends ScoreComponent {
		Object pattern;
		List<Object> rangeInPattern;
		String[] derefs;
		double defaultScore = Double.NaN;
		
		public double scoreObject(Object result, double minScore, double maxScore) {
			for(int i=0; i<derefs.length; i++) {
				Object deref = derefs[i];
				if (deref == null) {
					if (!(result instanceof List)) return defaultScore;
					result = ((List)result).get(0);
				} else {
					if (!(result instanceof Map)) return defaultScore;
					result = ((Map)result).get(deref);
				}
				if (result == null) return defaultScore;
			}
			if (result instanceof List || result instanceof Map) return defaultScore;
			return scoreLeafDbResult(new DbResultAtom(result));
		}
		@Override
		public double score(DbResult result, double minScore, double maxScore) {
			for(int i=0; i<derefs.length; i++) {
				Object deref = derefs[i];
				if (deref == null) {
					result = result.derefByIndex(0);
				} else {
					result = result.derefByKey((String)deref);
				}
				if (result.isNull()) return defaultScore;
			}
			return scoreLeafDbResult(result);
		}
		private static final String[] EMPTY_STRING_ARRAY = new String[]{};
		public void initFromBytes(Bytes bytes) {
			ArrayList<String> derefList = new ArrayList<String>();
			pattern = initFromBytes(bytes, 0, derefList);
			derefs = derefList.toArray(EMPTY_STRING_ARRAY);
		}
		public Object initFromBytes(Bytes bytes, int i, ArrayList<String> derefList) {
			if (i == bytes.getLength()) {
				rangeInPattern = Util.literalList().a("[]").a(null).a(null);
				return rangeInPattern;
			}
			int val = bytes.get(i);
			Object patt;
			switch(val) {
			case DbServiceUtil.TYPE_CHAR_MAP:
				int j = ++i;
				for(;bytes.get(j)!=DbServiceUtil.CHAR_ENTRY_SEP; j++) {}
				String key = Util.encodeBytes(bytes.getBytes(i, j-i));
				derefList.add(key);
				patt = initFromBytes(bytes, j+1, derefList);
				return Util.literalSMap().p(key, patt);
			case DbServiceUtil.TYPE_CHAR_LIST:
				derefList.add(null);
				patt = initFromBytes(bytes, i+1, derefList);
				return Util.literalList().a(patt);
			default:
				throw new RuntimeException("Unexpected byte prefix: "+bytes.toString());
			}
			
		}
		public abstract double scoreLeafDbResult(DbResult result);
	}

	public static class NumericScoreComponentFactory extends LeafScoreComponentFactory {
		@Override
		public LeafScoreComponent make(List<Object> scoringOperation) {
			NumericScoreComponent component = new NumericScoreComponent();
			switch(scoringOperation.size()) {
			default:
				throw new ApiException("InvalidScorer", "Invalid number of parameters in this scorer: "+scoringOperation);
			case 4:
				component.ub = ((Number)scoringOperation.get(3)).doubleValue();
			case 3:
				component.lb = ((Number)scoringOperation.get(2)).doubleValue();
			case 2:
				Number defaultNumber = (Number)scoringOperation.get(1);
				if (defaultNumber == null) {
					component.defaultScore = Double.NaN;
				} else {
					component.defaultScore = ((Number)defaultNumber).doubleValue();
				}
			case 1:
			}
			return component;
		}
	}
	
	public static class NumericScoreComponent extends LeafScoreComponent {
		double lb = Double.NEGATIVE_INFINITY;
		double ub = Double.POSITIVE_INFINITY;
		@Override
		public Iter narrowScoreRange(double minScore, double maxScore, Iter iter) {
			rangeInPattern.set(1, new Double(minScore));
			rangeInPattern.set(2, new Double(maxScore));
			iter.specialize(pattern);
			return iter;
		}
		@Override
		public double scoreLeafDbResult(DbResult result) {
			if (result.isDouble()) {
				return result.getDouble();
			} else {
				return defaultScore;
			}
		}
		@Override
		public double lowerBound() { return lb; }
		@Override
		public double upperBound() { return ub; }
	}
	
	public static class LookupTableScoreComponentFactory extends LeafScoreComponentFactory {
		@Override
		public LeafScoreComponent make(List<Object> scoringOperation) {
			List<Object> points = (List<Object>) scoringOperation.get(1);
			double defaultScore = Double.NaN;
			switch(scoringOperation.size()) {
			default:
				throw new ApiException("InvalidScorer", "Invalid number of parameters in this scorer: "+scoringOperation);
			case 3:
				Number defaultNumber = (Number)scoringOperation.get(2);
				if (defaultNumber != null) {
					defaultScore = ((Number)scoringOperation.get(2)).doubleValue();
				}
			case 2:
			}
			return make(points, defaultScore);
		}
		public LeafScoreComponent make(List<Object> points, double defaultScore) {
			return new LookupTableScoreComponent(points, defaultScore);
		}
	}
	
	public static class LookupTableScoreComponent extends LeafScoreComponent {
		Object[] values;
		double[] scores;
		double lb;
		double ub;
		public LookupTableScoreComponent(List<Object> points, double defaultScore) {
			this.defaultScore = this.lb = this.ub = defaultScore;
			int count = points.size();
			values = new Object[count];
			scores = new double[count];
			int i=0;
			for(Object pointObj : points) {
				if (pointObj == null || !(pointObj instanceof List))
					throw new ApiException("InvalidScoringPoint", "Invalid score point: \""+pointObj+"\"; must instead be a list of size 2, containing X and Y coordinates");
				List<Object> pair = (List<Object>)pointObj;
				Object valObj = pair.get(0);
				Object scoreObj = pair.get(1);
				if (scoreObj == null || !(scoreObj instanceof Number))
					throw new ApiException("InvalidScoringPoint", "Invalid score point: \""+pair+"\"; Y coordinate must be a number");
				values[i] = valObj;
				DbResultAtom atom = new DbResultAtom(values[i]);
				char type = atom.type;
				if (type == DbServiceUtil.TYPE_CHAR_MAP || type == DbServiceUtil.TYPE_CHAR_LIST)
					throw new ApiException("InvalidScoringPoint", "Invalid score point: \""+pair+"\"; X coordinate must be an atomic type");
				if (i > 0) {
					if (atom.compareTo(values[i-1]) < 0) {
						throw new ApiException("InvalidScoringPoint", "X coordinates must be in increasing order, but '"+values[i-1]+"' > '"+values[i]+"'");
					}
				}
				double score = ((Number)scoreObj).doubleValue();
				scores[i] = score;
				if (score < lb) lb = score;
				if (score > ub) ub = score;
				i++;
			}
		}
		@Override
		public Iter narrowScoreRange(double minScore, double maxScore, Iter iter) {
			if (minScore > lb) lb = minScore;
			if (maxScore < ub) ub = maxScore;
			if (defaultScore >= minScore && defaultScore <= maxScore) return iter;
			Object minVal = null;
			Object maxVal = null;
			for(int i=0; i<values.length; i++) {
				double score = scores[i];
				if (score >= minScore && score <= maxScore) {
					minVal = values[i];
				}
			}
			for(int i=values.length-1; i>=0; i--) {
				double score = scores[i];
				if (score >= minScore && score <= maxScore) {
					maxVal = values[i];
				}
			}
			rangeInPattern.set(1, minVal);
			rangeInPattern.set(2, maxVal);
			iter.specialize(pattern);
			return iter;
		}
		@Override
		public double scoreLeafDbResult(DbResult result) {
			int min = 0;
			int max = values.length - 1;
			while ( min <= max) {
				int mid = (min+max)/2;
				int cmp = result.compareTo(values[mid]);
				if (cmp > 0) {
					min = mid + 1;
				} else if (cmp < 0) {
					max = mid - 1;
				} else {
					return scores[mid];
				}
			}
			return defaultScore;
		}
		@Override
		public double lowerBound() { return lb; }
		@Override
		public double upperBound() { return ub; }
	}
	
	public static class LinearlyInterpolatedScoreComponentFactory extends LookupTableScoreComponentFactory {
		@Override
		public LeafScoreComponent make(List<Object> points, double defaultScore) {
			return new LinearlyInterpolatedScoreComponent(points, defaultScore);
		}
	}
	
	public static class LinearlyInterpolatedScoreComponent extends LookupTableScoreComponent {
		public LinearlyInterpolatedScoreComponent(List<Object> points, double defaultScore) {
			super(points, defaultScore);
			for(Object val : values) {
				if (DbServiceUtil.typeOfObject(val) != DbServiceUtil.TYPE_CHAR_NUMBER)
					throw new ApiException("InvalidScoringPoint", "Numeric X coordinate required; found this: '"+val+"'");
			}
		}
		@Override
		public double scoreLeafDbResult(DbResult result) {
			int min = 0;
			int max = values.length - 1;
			if (! result.isDouble()) return defaultScore;
			if (result.compareTo(values[min]) <= 0)
				return scores[min];
			if (result.compareTo(values[max]) >= 0)
				return scores[max];
			while ( min <= max ) {
				int mid = (min+max)/2;
				int cmp = result.compareTo(values[mid]);
				if (cmp > 0) {
					min = mid + 1;
				} else if (cmp < 0) {
					max = mid - 1;
				} else {
					return scores[mid];
				}
			}
			double loY = scores[max];
			double hiY = scores[min];
			double loX = ((Number)values[max]).doubleValue();
			double hiX = ((Number)values[min]).doubleValue();
			double ourX = result.getDouble();
			return ((ourX-loX)*hiY + (hiX-ourX)*loY) / (hiX-loX);
		}
	}

	
	public static class MultipliedScoreComponentFactory extends ScoreComponentFactory {
		@Override
		public ScoreComponent make(List<Object> scoringOperation, Bytes prefix) {
			MultipliedScoreComponent component = new MultipliedScoreComponent();
			switch(scoringOperation.size()) {
			default:
				throw new ApiException("InvalidScorer", "Invalid number of parameters in this scorer: "+scoringOperation);
			case 3:
				component.multiplier = ((Number)scoringOperation.get(1)).doubleValue();
				if (component.multiplier < 0.0) 
					throw new ApiException("InvalidScorer", "Only positive multipliers are allowed: "+scoringOperation);
			}
			component.inner = parseScorePattern(scoringOperation.get(2), prefix);
			return component;
		}
	}
	
	public static class MultipliedScoreComponent extends ScoreComponent {
		ScoreComponent inner;
		double multiplier;
		@Override
		public Iter narrowScoreRange(double minScore, double maxScore, Iter iter) {
			return inner.narrowScoreRange(
					minScore/multiplier, 
					maxScore/multiplier, iter);
		}
		@Override
		public double scoreObject(Object result, double minScore, double maxScore) {
			return inner.scoreObject(result, minScore/multiplier, maxScore/multiplier) * multiplier;
		}
		@Override
		public double score(DbResult result, double minScore, double maxScore) {
			return inner.score(result, minScore/multiplier, maxScore/multiplier) * multiplier;
		}
		@Override
		public double lowerBound() { return inner.lowerBound()*multiplier; }
		@Override
		public double upperBound() { return inner.upperBound()*multiplier; }
	}
	
	public static class ReverseScoreComponentFactory extends ScoreComponentFactory {
		@Override
		public ScoreComponent make(List<Object> scoringOperation, Bytes prefix) {
			ReverseScoreComponent component = new ReverseScoreComponent();
			switch(scoringOperation.size()) {
			default:
				throw new ApiException("InvalidScorer", "Invalid number of parameters in this scorer: "+scoringOperation);
			case 2:
			}
			component.inner = parseScorePattern(scoringOperation.get(1), prefix);
			return component;
		}
	}
	
	public static class ReverseScoreComponent extends ScoreComponent {
		ScoreComponent inner;
		@Override
		public Iter narrowScoreRange(double minScore, double maxScore, Iter iter) {
			return inner.narrowScoreRange(-maxScore, -minScore, iter);
		}
		@Override
		public double scoreObject(Object result, double minScore, double maxScore) {
			return -inner.scoreObject(result, -maxScore, -minScore);
		}
		@Override
		public double score(DbResult result, double minScore, double maxScore) {
			return -inner.score(result, -maxScore, -minScore);
		}
		@Override
		public double lowerBound() { return -inner.upperBound(); }
		@Override
		public double upperBound() { return -inner.lowerBound(); }
	}

	public static final class ScoreExtractor extends PatternVisitor {
		ArrayList<ScoreComponent> scoreComponents = new ArrayList<ScoreComponent>();
		Bytes globalPrefix;
		public ScoreExtractor(Bytes globalPrefix) {
			this.globalPrefix = globalPrefix;
		}
		@Override
		protected boolean isOperationLead(String op) {
			return true;
		}
		@Override
		public void handleAtomic(Bytes prefix, Bytes suffix, Object val) {
			throw new ApiException("InvalidScorePattern", "Atomic value not allowed in scoring pattern: \""+val+"\"");
		}
		@Override
		public void handleOperation(Bytes prefix, Bytes suffix, String opName, List<Object> operation) {
			ScoreComponentFactory factory = lookupScoreFactory(opName);
			ScoreComponent component = factory.make(operation, CompoundBytes.make(globalPrefix,prefix));
			scoreComponents.add(component);
		}
		public ScoreComponent getScoreComponent() {
			int sz = scoreComponents.size(); 
			if (sz > 1) {
				return new SumScoreComponent(scoreComponents);
			} else if (sz == 1) {
				return scoreComponents.get(0);
			} else {
				throw new ApiException("InvalidScorePattern", "No scorer found in (sub)pattern");
			}
		}
	}
	public static final class SumScoreFactory extends ScoreComponentFactory {
		@Override
		public ScoreComponent make(List<Object> scoringOperation, Bytes prefix) {
			return parseScorePattern(scoringOperation.get(1), prefix);
		}
	}
	public static final class SumScoreComponent extends ScoreComponent {
		double lb, ub;
		private static class SumScoreEntry {
			ScoreComponent component;
			double lb, ub;
			double subsequentMax, subsequentMin;
		}
		SumScoreEntry[] entries;
		public SumScoreComponent(ArrayList<ScoreComponent> components) {
			entries = new SumScoreEntry[components.size()];
			int i=0;
			for(ScoreComponent component : components) {
				SumScoreEntry entry = new SumScoreEntry();
				entry.component = component;
				entry.lb = component.lowerBound();
				entry.ub = component.upperBound();
				entries[i++] = entry;
			}
			for(i=entries.length-2; i>=0; i--) {
				SumScoreEntry cur  = entries[i];
				SumScoreEntry above = entries[i+1];
				cur.subsequentMax = above.ub + above.subsequentMax;
				cur.subsequentMin = above.lb + above.subsequentMin;
			}
		}

		@Override
		public Iter narrowScoreRange(double minScore, double maxScore, Iter iter) {
			for(SumScoreEntry entry : entries) {
				double minScoreWithoutMe = lb - entry.lb;
				double maxScoreWithoutMe = ub - entry.ub;
				double myMinScore = entry.lb + minScore - maxScoreWithoutMe;
				double myMaxScore =            maxScore - minScoreWithoutMe;
				if (myMinScore > entry.lb || myMaxScore < entry.ub) {
					if (myMinScore > entry.lb) entry.lb = myMinScore;
					if (myMaxScore < entry.ub) entry.ub = myMaxScore;
					iter = entry.component.narrowScoreRange(myMinScore, myMaxScore, iter);
				}
			}
			return iter;
		}
		@Override
		public double scoreObject(Object result, double minScore, double maxScore) {
			double total = 0.0;
			for(SumScoreEntry entry : entries) {
				double score = entry.component.scoreObject(result, entry.lb+minScore, entry.ub+maxScore);
				if (Double.isNaN(score)) return Double.NaN;
				total += score;
				if (total + entry.subsequentMin > maxScore) return Double.NaN;
				if (total + entry.subsequentMax < minScore) return Double.NaN;
				
			}
			return total;
		}
		@Override
		public double score(DbResult result, double minScore, double maxScore) {
			double total = 0.0;
			for(SumScoreEntry entry : entries) {
				double score = entry.component.score(result, entry.lb+minScore, entry.ub+maxScore);
				if (Double.isNaN(score)) return Double.NaN;
				total += score;
				if (total + entry.subsequentMin > maxScore) return Double.NaN;
				if (total + entry.subsequentMax < minScore) return Double.NaN;
				
			}
			return total;
		}
		@Override
		public double lowerBound() { return lb; }
		@Override
		public double upperBound() { return ub; }
	}
	
	protected static ScoreComponent parseScorePattern(Object scorePattern, Bytes prefix) {
		ScoreExtractor extractor = new ScoreExtractor(prefix);
		extractor.apply(scorePattern);
		return extractor.getScoreComponent();
	}


	public static final class HitRec implements Comparable<HitRec> {
		double score;
		Object result;
		public HitRec(double score, Object result) {
			this.score = score;
			this.result = result;
		}
		public int compareTo(HitRec o) {
			double diff = score - o.score;
			if (diff > 0) return 1;
			else if (diff < 0) return -1;
			else return 0;
		}
		
	}

	@Override
	public Iter query(String bag, long txnId, Object query, Map<String, Object> options) {
		int offset = 0;
		Object offsetObj = options.get("offset");
		if (offsetObj != null) {
			offset = ((Number)offsetObj).intValue();
			options.remove("offset");
		}
		long limit = Long.MAX_VALUE;
		Object limitObj = options.get("limit");
		if (limitObj != null) {
			limit = ((Number)limitObj).longValue();
			options.remove("limit");
			limit += offset;
		}
		final PriorityQueue<HitRec> hits = new PriorityQueue<HitRec>();
		String bookmark = (String)options.get("bookmark");
		double minScore = Double.NEGATIVE_INFINITY;
		double maxScore = Double.POSITIVE_INFINITY;
		String secondaryField = "id";
		Object secondaryValue = null;
		if (bookmark != null) {
			String[] bookmarkParts = bookmark.split(",");
			if (bookmarkParts.length != 3) throw new ApiException("InvalidBookmark","Bookmark must have three elements, seperated by commas, instead we found: \""+bookmark+"\"");
			String maxScoreString = bookmarkParts[0];
			if (maxScoreString.length() > 0) {
				try {
					maxScore = Double.parseDouble(maxScoreString);
				} catch(NumberFormatException e) {
					throw new ApiException("InvalidBookmark","Uparsable max score in \""+bookmark+"\"");
				}
			}
			secondaryField = bookmarkParts[1];
			try {
				secondaryValue = Util.jsonDecode(bookmarkParts[2]);
			} catch(Util.JsonParseRuntimeException e) {
				throw new ApiException("InvalidBookmark", "Invalid compare JSON value in bookmark: "+e.getMessage());
			}
		}
		Object scorePattern = options.get("score");
		Iter iter = subService.query(bag, txnId, query, options);
		if (scorePattern == null) {
			return new SlicedIter(iter, offset, limit);
		}
		ScoreComponent scorer = parseScorePattern(scorePattern, ArrayBytes.EMPTY_BYTES);
		while(iter.hasNext()) {
			Object resultObject = iter.next();
			double score;
			DbResult dbResult = null;
			if (resultObject instanceof DbResultMapView) {
				dbResult = ((DbResultMapView)resultObject).getDbResult();
				score = scorer.score(dbResult, minScore, maxScore);
			} else {
				score = scorer.scoreObject(resultObject, minScore, maxScore);
			}
			if (Double.isNaN(score)) continue;
			if (score < minScore) continue;
			if (score > maxScore) continue;
			if (score == maxScore) {
				if (dbResult.derefByKey(secondaryField).compareTo(secondaryValue) <= 0) continue;
			}
			
			// copy in an appropriate fashion
			if (dbResult != null && dbResult instanceof QueryNodeDbResult) {
				resultObject = new QueryNodeDbResultMarker((QueryNodeDbResult)dbResult);
//				resultObject = dbResult.deepCopy();
			} else {
				resultObject = Util.jsonDecode(Util.jsonEncode(resultObject));
			}
			hits.offer(new HitRec(score, resultObject));
			
			if (hits.size() > limit) {
				hits.remove();
				double newMinScore = hits.peek().score;
				if (newMinScore > minScore) {
					minScore = newMinScore;
					Iter newIter = scorer.narrowScoreRange(minScore, maxScore, iter);
					iter = newIter;
				}
			}
		}
		final int numHits = hits.size();
		if (numHits <= offset) {
			return EmptyIter.EMPTY_ITER;
		}
		Object[] resultBuf = new Object[numHits-offset];
		for(int i=numHits-offset-1; i>=0; i--) {
			HitRec rec = hits.remove();
			resultBuf[i] = rec.result;
		}
		return new LiteralIter(resultBuf);
	}


}

