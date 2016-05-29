&amp;&amp; Inventory.isEmpty()) {
// The invy is empty so spawn herbs to clean.
HerbloreCleanerMethods.CloseLevelUpMenu();
if (HerbloreVariables.getHerb() != 199) {
// There is herbs to clean in the invy. Clean them.
HerbloreCleanerMethods.CleanHerbs();
}
if (Inventory.getCount(HerbloreVariables.getHerb()) == 0

