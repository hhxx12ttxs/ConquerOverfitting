package com.zczapran.orl.profile.rankedsemantic;

import com.zczapran.orl.document.ConceptDocument;
import com.zczapran.orl.ontology.Ontology;
import com.zczapran.orl.profile.Mergable;
import com.zczapran.orl.profile.Scorable;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Optimised implementation of algorithm described in paper 'A Semantic Approach
 * for News Recommendation' by F. Frasincar, W. IJntema, F. Goossen, F.
 * Hogenboom.
 *
 * @author Zbigniew Czapran <zczapran@gmail.com>
 */
public class RankedSemanticProfile
	implements Mergable<ConceptDocument, RankedSemanticProfile>, Scorable<ConceptDocument, RankedSemanticProfile> {

	final private Ontology ontology;
	final private TObjectDoubleHashMap innerProfile = new TObjectDoubleHashMap();
	protected int profileSize = 0;
	protected int size = 0;
	private boolean needsUpdate = true;
	private TObjectDoubleHashMap normalisedInnerProfile = new TObjectDoubleHashMap();

	/**
	 * Constructor that injects instance of Ontology interface.
	 *
	 * @param ontology
	 */
	public RankedSemanticProfile(Ontology ontology) {
		this.ontology = ontology;
	}

	/**
	 * Adds the document and does some precalculations of the profile in linear
	 * time.
	 *
	 * @param document
	 */
	@Override
	public void add(ConceptDocument document) {
		size++;
		for (String concept : document.getConcepts()) {
			profileSize++;
			if (!innerProfile.contains(concept)) {
				initialise(concept);
			}
			increase(concept, 0.95);

			for (String related : ontology.getRelated(concept)) {
				if (!innerProfile.contains(related)) {
					initialise(related);
				}
				increase(related, 0.8);
			}
			needsUpdate = true;
		}
	}

	/**
	 * Optimised merge operation.
	 *
	 * @param profile
	 */
	@Override
	public void merge(RankedSemanticProfile profile) {
		profileSize += profile.profileSize;
		size += profile.size();
		for (Object concept : profile.innerProfile.keys()) {
			if (!innerProfile.containsKey(concept)) {
				this.initialise((String)concept);
			}
			this.increase(concept.toString(), profile.innerProfile.get(concept));
			needsUpdate = true;
		}
	}

	/**
	 * Calculates score for a document against the profile.
	 *
	 * @param document
	 *
	 * @return Normalized score [0; 1.0]
	 */
	@Override
	public double score(ConceptDocument document) {
		TObjectDoubleHashMap profile = this.getInnerProfile(true);
		double sum = 0.0;
		for (double val : profile.values()) {
			sum += val;
		}

		Set<String> documentConcepts = new HashSet<>(document.getConcepts());
		for (String concept : document.getConcepts()) {
			documentConcepts.addAll(this.ontology.getRelated(concept));
		}

		double va = 0.0;
		if (profile.size() > 0) {
			for (String concept : documentConcepts) {
				va += profile.containsKey(concept) ? profile.get(concept) : 0.0;
			}
			return va / sum;
		} else {
			return 1.0;
		}
	}

	/**
	 * Compares to profiles with eath other.
	 *
	 * @param profile
	 *
	 * @return
	 */
	@Override
	public double score(RankedSemanticProfile profile) {
		TObjectDoubleHashMap innerProfile1 = this.getInnerProfile(true);
		TObjectDoubleHashMap innerProfile2 = profile.getInnerProfile(true);

		double sum = 0.0;
		for (double val : innerProfile1.values()) {
			sum += val;
		}
		for (double val : innerProfile2.values()) {
			sum += val;
		}

		double va = 0.0;

		Set<Object> set = new TreeSet(innerProfile2.keySet());
		set.retainAll(innerProfile1.keySet());

		for (Object concept : set) {
			va += innerProfile1.get(concept) + innerProfile2.get(concept);
		}

		return va / sum;
	}

	/**
	 * Returns inner profile (optionally normalized).
	 *
	 * @param normalize
	 * @return
	 */
	public TObjectDoubleHashMap getInnerProfile(boolean normalize) {
		if (normalize) {
			if (needsUpdate) {

				if (innerProfile.size() > 0) {
					Double min = null, max = null;
					for (double value : innerProfile.values()) {
						if (min == null || min > value) {
							min = value;
						}
						if (max == null || max < value) {
							max = value;
						}
					}
					normalisedInnerProfile = new TObjectDoubleHashMap(innerProfile.size() + 1, (float) 1.0);

					double scale = max - min;
					if (Math.abs(scale) < 0.01) {
						for (Object key : innerProfile.keySet()) {
							normalisedInnerProfile.put(key, 1.0);
						}
					} else {
						for (Object key : innerProfile.keySet()) {
							normalisedInnerProfile.put(key, (innerProfile.get(key) - min) / scale);
						}
					}
				} else {
					normalisedInnerProfile = new TObjectDoubleHashMap();
				}
			}
			return normalisedInnerProfile;
		} else {
			return innerProfile;
		}
	}

	/**
	 * Creates a formatted representation of the profile.
	 *
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		TObjectDoubleHashMap profile = this.getInnerProfile(true);

		str.append("[\n");
		for (Object key : profile.keySet()) {
			str.append(' ');
			str.append(key);
			str.append(": ");
			str.append(profile.get(key));
			str.append("\n");
		}
		str.append("]\n");

		return str.toString();
	}

	/**
	 * Increases a concept value in the profile.
	 *
	 * @param concept
	 * @param value
	 */
	private void increase(String concept, double value) {
		this.innerProfile.put(concept, this.innerProfile.get(concept) + value);
	}

	/**
	 * Initialises a concept in the profile.
	 *
	 * @param concept
	 */
	private void initialise(String concept) {
		this.innerProfile.put(concept, 0);
	}

	@Override
	public int size() {
		return size;
	}

}
