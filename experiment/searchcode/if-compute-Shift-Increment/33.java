private static final Logger myLogger = Logger.getLogger(EndelmanDistanceMatrix.class);

private static final int DEFAULT_MAX_ALLELES = 2;

/**
* Compute Endelman kinship for all pairs of taxa. Missing sites are
* ignored. http://www.g3journal.org/content/2/11/1405.full.pdf Equation-13
*
* @param genotype Genotype Table used to compute kinship

