public class MonoHillClimbing extends Algorithm
{
private Problem problem;
private NeighborVisitor visitor;
private int maxEvaluations;
* Inicializa o hill climbing sem random restart
*/
public MonoHillClimbing(Problem problem, NeighborVisitor visitor, int maxEvaluations)

