Iterable<AttributeCombination>
{
private final int nVars;
private boolean hasNext;
private final List<Integer> combo;
private final List<String> labels;

public CombinationGenerator(final int nVars, final int nCombo,

