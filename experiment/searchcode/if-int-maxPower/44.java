public static Armor createNewArmor(boolean boss, int baseHealth) {
int maxPower = (boss) ? Configuration.bossArmorPower : Configuration.minionArmorPower;
armor.leggings = buildLeggings(maxPower);
armor.boots = buildBoots(maxPower);

return armor;
}

public static ItemStack buildHelmet(int maxPower) {

