private final int requiredVersion;
private final boolean requiresData;
private final boolean requiresWater;

// Initialize map for quick name and id lookup
public boolean getRequiresWater() {
return requiresWater;
}

public boolean isSupported() {
if (requiredVersion == -1) {

