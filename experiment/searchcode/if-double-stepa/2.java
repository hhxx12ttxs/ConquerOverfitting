step = recHandler.getNextTimeStep(stepO, stepA);
if (step == null)
break;

/*
* test if transition occurs in this step
*/
if ((o == null || stepO.equals(o)) &amp;&amp;
(a == null || stepA.equals(a))) {

