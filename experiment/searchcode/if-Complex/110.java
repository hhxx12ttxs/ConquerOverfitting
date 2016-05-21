private static Pref cacheDisallowModificationComplexNodes = Pref.makeBooleanServerPref(\"DisallowModificationComplexNodes\", tool.prefs, false);
/**
 * Method to tell whether complex nodes can be modified.
 * Complex nodes are cell instances and advanced primitives (transistors, etc.)
 * The default is \"false\" (modifications are NOT disallowed).
 * @return true if the complex nodes cannot be modified.
 */
public static boolean isDisallowModificationComplexNodes() { return cacheDisallowModificationComplexNodes.getBoolean(); }
/**
 * Method to set whether complex nodes can be modified.
 * Complex nodes are cell instances and advanced primitives (transistors, etc.)
 * @param on true if complex nodes cannot be modified.
 */
public static void setDisallowModificationComplexNodes(boolean on) { cacheDisallowModificationComplexNodes.setBoolean(on); }
/**

