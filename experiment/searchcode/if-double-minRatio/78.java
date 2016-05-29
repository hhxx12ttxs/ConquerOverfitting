public static Boolean checkCase(Player player, String chat) {

// Go back if staff have bypass on.
if( player.hasPermission(&quot;chatwarden.staff&quot;) &amp;&amp; mConfig.getConfig().getBoolean(&quot;case.bypass-staff&quot;) ) {
Integer minChars = mMain.getPlugin().getConfig().getInt(&quot;case.min-chars&quot;);
Double minRatio = mMain.getPlugin().getConfig().getDouble(&quot;case.min-ratio&quot;);

