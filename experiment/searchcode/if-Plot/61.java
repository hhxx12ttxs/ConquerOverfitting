public static HashSet<UUID> getOwners(Plot plot) {
if (plot.owner == null) {
return new HashSet<UUID>();
}
if (plot.settings.isMerged()) {
UUID owner = MainUtil.getPlot(plot.world, id).owner;
if (owner != null) {
owners.add(owner);

