private int maxEvaluations;

public MonoRandomSearch(Problem problem, int maxEvaluations)
{
this.problem_ = problem;
problem_.evaluate(best);
int evaluations = 1;

while (evaluations < maxEvaluations)
{
Solution current = new Solution(problem_);

