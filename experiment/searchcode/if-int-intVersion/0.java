final class IntVersion implements Version {
private static final Interner<IntVersion> interner = Interners.newWeakInterner();
@Override
public boolean atMost(Version other) {
if (!(other instanceof IntVersion)) {
return false;

