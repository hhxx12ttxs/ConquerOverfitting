private Problem problem_;
private int maxEvaluations;

public MonoRandomSearch(Problem problem, int maxEvaluations)
{
this.problem_ = problem;
this.maxEvaluations = maxEvaluations;
}

public SolutionSet execute()

