Integer y = chestLoc.getBlockY();
Integer z = chestLoc.getBlockZ();
for(String nameKey:plugin.chestConfig.getChests().getKeys(false)) {
for(String key:plugin.chestConfig.getChests().getConfigurationSection(nameKey).getKeys(false)) {
String world1 = plugin.chestConfig.getChests().getString(nameKey + &quot;.&quot; + key + &quot;.World&quot;);

