private PlotAPI plotAPI = null;

public void hook() {

if (Bukkit.getServer().getPluginManager().isPluginEnabled(&quot;PlotSquared&quot;)) {

plotAPI = new PlotAPI(plugin);

if (plotAPI != null) {

boolean hooked = PlaceholderAPI.registerPlaceholderHook(&quot;PlotSquared&quot;, new PlaceholderHook() {

