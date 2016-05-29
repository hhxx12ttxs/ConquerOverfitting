private static Settings	instance;

public static Settings getInstance() {
if (instance == null)
instance = new Settings();
private boolean normalizeWeight;

public Random getRandom() {
if (random == null)
random = new DefaultRandom();

