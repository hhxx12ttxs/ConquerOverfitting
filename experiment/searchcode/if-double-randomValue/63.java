public void raceStep() {
int randomvalue = rnd.nextInt(100);

if(randomvalue < CHANCE_TO_HIDE &amp;&amp; !hidden) {
hide();
} else if(randomvalue < CHANCE_TO_SURFACE &amp;&amp; hidden) {
unhide();
}

forward(getNextStep());

