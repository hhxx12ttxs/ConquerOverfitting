step = recHandler.getNextTimeStep(stepO, stepA);
if (step == null)
break;


ctThisInterv += getMetricForStep(step, metric);
if (metric.equals(Metric.MDP_REW)) {
double envRew = step.rew;
ctr += envRew;
}
if (metric.equals(Metric.POS_HREW)) {

