interpolator.rescale(hNew);

}
}

// predict a first estimate of the state at step end
final double stepEnd = stepStart + stepSize;
nordsieck = nordsieckTmp;
interpolator.reinitialize(stepEnd, stepSize, scaled, nordsieck);

if (!isLastStep) {

