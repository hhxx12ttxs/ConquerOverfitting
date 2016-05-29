File invf = new File(InventoryUtils.plugin.getDataFolder() + File.separator + &quot;wallets&quot;, player + &quot;.dat&quot;);
if(invf.exists()){
YamlConfiguration invY = new YamlConfiguration();
try {
invY.load(invf);
if (invY.isSet(Integer.toString(walletIndex))){
int size = invY.getInt(walletIndex + &quot;.size&quot;);

