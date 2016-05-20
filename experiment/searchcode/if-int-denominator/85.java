package com.facebook.app.server.clustering;

import com.facebook.app.shared.clustering.Clusterable;

public class CosineSimilarityStrategy implements ClusterSimilarityStrategy {

	/**
	 * This clustering strategy implements the cosine similarity rule, viewing
	 * the characteristicClusterVector array of each Clusterable as a vector and
	 * using the cosine of the angle between two vectors as a measure of
	 * similarity.
	 */
	@Override
	public double computeSimilarity(Clusterable first, Clusterable second) {

		double[] firstCharacteristicClusterVector = first
				.getCharacteristicClusterVector();
		double[] secondCharacteristicClusterVector = second
				.getCharacteristicClusterVector();

		if (firstCharacteristicClusterVector.length == 0
				&& secondCharacteristicClusterVector.length == 0) {
			return 0;
		}

		if (firstCharacteristicClusterVector.length != secondCharacteristicClusterVector.length) {
			throw new IllegalArgumentException(
					"The lengths of the characteristicClusterVectors are different: "
							+ firstCharacteristicClusterVector.length
							+ " != "
							+ secondCharacteristicClusterVector.length);
		}

		double numerator = 0;
		for (int i = 0; i < firstCharacteristicClusterVector.length; i++) {
			numerator += firstCharacteristicClusterVector[i]
					* secondCharacteristicClusterVector[i];
		}

		double denominator = 0;
		double x1 = 0;
		double y1 = 0;
		for (int i = 0; i < firstCharacteristicClusterVector.length; i++) {
			x1 += firstCharacteristicClusterVector[i]
					* firstCharacteristicClusterVector[i];
			y1 += secondCharacteristicClusterVector[i]
					* secondCharacteristicClusterVector[i];
		}

		denominator = Math.sqrt(x1) + Math.sqrt(y1);
		double cosSim = numerator / denominator * 10;

		return cosSim;
	}

}

