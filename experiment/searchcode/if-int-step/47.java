public Step doStep(Step step, int n) throws IOException
{
if(playerNumber != step.getPlayerNumber())
return step;
step = choiceSquare(step, n);
return step;
}
protected Step choicePawn(Step step, int n) throws IOException
{

if(step.getNC(n) == step.getPlayerNumber())

