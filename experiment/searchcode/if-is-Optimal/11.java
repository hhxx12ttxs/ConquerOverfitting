setFragmentationThreshold(fragmentationThreshold);
setOptimalSize(optimalBlockSize);
}

public boolean isFragment(final Block<T> block) {
private void setOptimalSize(long optimalSize) {
if (optimalSize < 1L) {
throw new IllegalArgumentException(&quot;optimalSize must be a positive value. Value passed was = &quot; + optimalSize);

