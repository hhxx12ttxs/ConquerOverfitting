public static void showUpdate(String v, int intVersion, String download, String buildType, boolean override) {

if (!UpdateActivity.open) {
intent.putExtra(&quot;intVersion&quot;, intVersion);
intent.putExtra(&quot;download&quot;, download);
intent.putExtra(&quot;buildType&quot;, buildType);

