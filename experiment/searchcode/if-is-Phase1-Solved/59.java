* a GUI and a TextUI on two different runners. If you are interested,
* this is probably best solved with Observers and messages:
*    1) make some messages: https://github.com/zombiecalypse/Ursuppe-Sample/tree/master/src/com/acme/ursuppe/events
int number = decisions.roll(1, 2);
if (number == 1) {
game.phase1(playersQueue.get(i));

