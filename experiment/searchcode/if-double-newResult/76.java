private MonteCarloTreeSearchResult<GoMove> _createMonteCarloTreeSearchResult()
{
MonteCarloTreeSearchResult<GoMove> newResult;
if (mctsResultPool.isEmpty())
{
newResult = new MonteCarloTreeSearchResult<GoMove>(mctsResultPool);
MonteCarloHashMapResult newResult = mchmResultPool.testAndPop();
if (newResult==null)
newResult = new MonteCarloHashMapResult(mchmResultPool);

