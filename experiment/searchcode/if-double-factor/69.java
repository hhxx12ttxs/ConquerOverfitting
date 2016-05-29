this.factorEvaluator = factorEvaluator;
}

@Override
public int compare(Node n1, Node n2) {
if (factorEvaluator.hasNaturalOrder())
return Double.compare(factorEvaluator.evaluateFactorForNode(n1), factorEvaluator.evaluateFactorForNode(n2));

