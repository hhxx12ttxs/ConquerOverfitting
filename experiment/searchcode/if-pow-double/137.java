package simphony.datastruct;


public class Phoneme implements java.io.Serializable {
	String ipa;
	PhonemeFeatures features;

	// weights for distance measure
	private static final double WEIGHT_PLACE		= 0.299d;
	private static final double WEIGHT_MANNER		= 0.5d;
	private static final double WEIGHT_SECONDARY	= 0.001d;
	private static final double WEIGHT_FLAGS		= 0.2d;
	private static final int NUM_FLAGS				= 8;

	public Phoneme(String basicPhoneme, String modifiers) {
		this.ipa = basicPhoneme + modifiers;
		this.features = PhonemeFeatures.getFeatures(basicPhoneme, modifiers);
	}

	public PhonemeFeatures getFeatures() {
		return this.features;
	}

	public double abs() {
		return this.features.manner * 0.5 + 0.5;
	}

	public double dist(Phoneme otherPhoneme) {
		double dist = 0.0d, maxDist = 0.0d;
		PhonemeFeatures otherFeatures = otherPhoneme.getFeatures();

		// place of articulation
		//dist += WEIGHT_PLACE * Math.abs(this.features.place - otherFeatures.place);
		dist += WEIGHT_PLACE * Math.pow(this.features.place - otherFeatures.place, 4);
		maxDist += WEIGHT_PLACE * 1.0d;

		// manner of articulation
		//dist += WEIGHT_MANNER * Math.abs(this.features.manner - otherFeatures.manner);
		dist += WEIGHT_MANNER * Math.pow(this.features.manner - otherFeatures.manner, 4);
		maxDist += WEIGHT_MANNER * 1.0d;

		// (place of) secondary articulation
		//dist += WEIGHT_SECONDARY * Math.abs(this.features.secondary - otherFeatures.secondary);
		dist += WEIGHT_SECONDARY * Math.pow(this.features.secondary - otherFeatures.secondary, 4);
		maxDist += WEIGHT_SECONDARY * 1.0d;

		// flags -- Hamming distance
		int mask = 1, flagsDist = 0;
		int difference = this.features.flags ^ otherFeatures.flags;
		for (int i = 0; i < NUM_FLAGS; i++) {
			if ((difference & mask) > 0)
				flagsDist += 1;
			mask *= 2;
		}
		dist += WEIGHT_FLAGS * Math.pow(((double)flagsDist) / NUM_FLAGS, 4);
		maxDist += WEIGHT_FLAGS * 1.0d;

		//System.out.println("d("+this.toString()+", " + otherPhoneme.toString() + ") = " + dist/maxDist);

		return Math.pow(dist / maxDist, 0.25);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Phoneme)) return false;
		return this.ipa.equals(other.toString());
	}

	public int hashCode() {
		return this.ipa.hashCode();
	}

	public String toString() {
		return this.ipa;
	}

}


