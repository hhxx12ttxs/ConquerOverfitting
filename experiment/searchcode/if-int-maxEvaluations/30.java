private NeighborVisitor visitor;
private int maxEvaluations;
private boolean randomRestart;
private int restarts;
private Notifier notifier;
public MonoHillClimbing(Problem problem, NeighborVisitor visitor, int maxEvaluations, Notifier notifier)
{
this(problem, visitor, maxEvaluations, false, notifier);

