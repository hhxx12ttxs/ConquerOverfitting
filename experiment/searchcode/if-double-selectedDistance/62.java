public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
{
if (((actor.getId() == OCTAVIS_LIGHT_FIRST) || (actor.getId() == OCTAVIS_HARD_FIRST)) &amp;&amp; (status == 1))
for (int[] outloc : OUTROOM_LOCATIONS)
{
if ((selectedLoc == null) || (selectedDistance > PositionUtils.calculateDistance(currentLoc.getX(), currentLoc.getY(), 0, outloc[0], outloc[1], 0, false)))

