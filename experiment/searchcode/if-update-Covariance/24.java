this(true);
}

/**
* Create an empty {@link StorelessBivariateCovariance} instance.
*
* @param biasCorrection if <code>true</code> the covariance estimate is corrected
meanX = meanY = 0.0;
n = 0;
covarianceNumerator = 0.0;
biasCorrected = biasCorrection;
}

/**
* Update the covariance estimation with a pair of variables (x, y).

