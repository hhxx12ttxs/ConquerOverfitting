File invF = new File(getDataFolder() + File.separator + &quot;wallets&quot;, ownerUUID + &quot;.dat&quot;);
if(invF.exists()){
YamlConfiguration invY = new YamlConfiguration();
try {
invY.load(invF);
if (invY.isSet(Integer.toString(num))){

