if (nbt != null) {
if (nbt.containsKey(&quot;MineItem&quot;)) {
mi.setDamage(nbt.getDouble(&quot;Damage&quot;));
nbt.put(&quot;MaxDur&quot;, mi.getMaxDur());
nbt.put(&quot;Lores&quot;, mi.getLores());
if (!mi.getGems().isEmpty()) {

