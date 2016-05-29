private static final Map<Integer, ParticleEffect> ID_MAP = new HashMap<Integer, ParticleEffect>();
private final String name;
private final int id;
private final int requiredVersion;
private ParticleEffect(String name, int id, int requiredVersion, boolean requiresData, boolean requiresWater) {
this.name = name;
this.id = id;

