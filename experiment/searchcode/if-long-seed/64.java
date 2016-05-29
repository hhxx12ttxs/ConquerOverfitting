private static final String PROP_SCHEDULER_OFFSET_SEED = &quot;agent.scheduler.offset.seed&quot;;

private long offsetSeed;

public SchedulerOffsetManager(AgentStorageProvider storage) {
String offsetSeedString = storage.getValue(PROP_SCHEDULER_OFFSET_SEED);
long offsetSeed;
if (offsetSeedString != null) {
offsetSeed = Long.parseLong(offsetSeedString);

