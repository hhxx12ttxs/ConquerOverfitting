public static CompressStrategy getCompressStrategy(String strategy) {
CompressStrategy cs = new NullCompressStrategy();

if (&quot;rar&quot;.equals(strategy)) {
cs = new RarCompressStrategy();

} else if (&quot;zip&quot;.equals(strategy)) {

