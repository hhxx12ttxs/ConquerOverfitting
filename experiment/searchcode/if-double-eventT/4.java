// restrict the interpolator to the first part of the step, up to the event
final double eventT = currentEvent.getEventTime();
interpolator.setInterpolatedTime(eventT);
final double[] eventY = interpolator.getInterpolatedState().clone();

