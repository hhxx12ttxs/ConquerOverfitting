ap.setCollectionPointRecommendationFactor(getValue(collectionPointRecommendationFactorFactor, stddev, rng));
ap.setReputationShop(getValue(shopReputationFactor, stddev, rng));
if (rng.nextDouble() <= atHomeFactor) ap.setAlwaysAtHome(true);
ap.setReputationShop(rng.getDouble(0,1));
if (rng.nextDouble() <= atHomeFactor) ap.setAlwaysAtHome(true);
else ap.setAlwaysAtHome(false);

