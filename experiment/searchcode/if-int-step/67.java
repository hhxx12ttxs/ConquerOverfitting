addStep(step);
}


public void addAssertProgressDisplay(AssertProgressDisplayStep step) {
int listSize = getStepList().size();
if (listSize == 0) {
throw new GuiConfigurationException(

