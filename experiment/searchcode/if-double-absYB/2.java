getRelativeAccuracy() * FastMath.max(FastMath.abs(xA), FastMath.abs(xB));
if (((xB - xA) <= xTol) || (FastMath.max(absYA, absYB) < getFunctionValueAccuracy())) {
throw new MathInternalError(null);
}
}

// target for the next evaluation point
double targetY;
if (agingA >= MAXIMAL_AGING) {

