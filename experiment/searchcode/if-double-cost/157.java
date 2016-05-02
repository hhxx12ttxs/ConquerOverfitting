package tinydb.jointree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tinydb.jointree.transformation.JoinTreeTransformation;
import tinydb.querygraph.QueryGraph;


public class IterativeImprovement implements JoinOrderAlgorithm {
	
	private final List<JoinTreeTransformation> ruleSet = new ArrayList<JoinTreeTransformation>();
	private final int timeLimitMillis;
	
	public IterativeImprovement(Collection<JoinTreeTransformation> joinTreeTransformations, int timeLimitMillis) {
		this.timeLimitMillis = timeLimitMillis;
		for (JoinTreeTransformation transformationRule : joinTreeTransformations) {
			ruleSet.add(transformationRule);
		}
	}

	@Override
	public JoinTree computeJoinTree(List<QueryGraph> queryGraphs) {
		// use QuickPick to obtain random join trees
		final QuickPick quickPick = new QuickPick(1);

		final long startTime = System.currentTimeMillis();

		JoinTree bestJoinTree = null;
		double bestJoinTreeCost = Double.MAX_VALUE;
		int iterations = 0;
		int improvementsFound = 0;
		do {
			final JoinTree randomJoinTree = quickPick.computeJoinTree(queryGraphs);
			if (bestJoinTree == null) {
				bestJoinTree = randomJoinTree;
				bestJoinTreeCost = cost(bestJoinTree);
			}
			
			final JoinTree transformedJoinTree = iterativeImprovement(randomJoinTree);
			final double transformedJoinTreeCost = cost(transformedJoinTree);
			if (transformedJoinTreeCost < bestJoinTreeCost) {
				bestJoinTree = transformedJoinTree;
				bestJoinTreeCost = transformedJoinTreeCost;
				improvementsFound++;
			}
			iterations++;
		} while (System.currentTimeMillis() - startTime <= timeLimitMillis);
		System.out.println("Iterations: " + iterations + ", improvements: " + improvementsFound);
		return bestJoinTree;
	}

	public JoinTree iterativeImprovement(JoinTree joinTree) {
		if (joinTree.isLeafNode()) {
			System.out.println(joinTree + " is a leaf node. No rules applicable.");
			return joinTree;
		}
		
		System.out.println(joinTree);
		final Cout cout = new Cout();
		JoinTreeUtils.printJoinTree(joinTree, cout);
		while (true) {
			
			JoinTreeTransformation bestRule = null;
			JoinTree bestTree = joinTree;
			double bestTreeCost = cout.computeCost(bestTree);
			boolean improved = false;
			
			for (JoinTreeTransformation rule : ruleSet) {
				final JoinTree transformedTree = rule.applyTransformation(joinTree);
				final double transformedTreeCost = cout.computeCost(transformedTree);
				if (transformedTreeCost < bestTreeCost) {
					bestTree = transformedTree;
					bestTreeCost = transformedTreeCost;
					bestRule = rule;
					improved = true;
					System.out.println("+ Rule '" + rule + "' leads to a cheaper tree. " 
							+ transformedTree + ", cost = " + cout.computeCost(transformedTree));
				} else {
					System.out.println("- Rule '" + rule + "' leads NOT to a cheaper tree. " 
							+ transformedTree + ", cost = " + cout.computeCost(transformedTree));

				}
					
			}
			
			if (improved) {
				
				System.out.println(" --" + bestRule + " -> ");
				// the transformed tree is cheaper. use it for further improvements
				joinTree = bestTree;
				System.out.println(joinTree);
				JoinTreeUtils.printJoinTree(joinTree, cout);
				continue;
				
			} else {
		
				System.out.println("Improve subtrees...");
				
				// improve subtrees
				final JoinTree leftSubTree = joinTree.getLeftSubTree();
				final JoinTree transformedLeftSubTree = iterativeImprovement(leftSubTree);
				final JoinTree rightSubTree = joinTree.getRightSubTree();
				final JoinTree transformedRightSubTree = iterativeImprovement(rightSubTree);

				List<JoinTree> treeCombinations = new ArrayList<JoinTree>();
				if (cost(transformedLeftSubTree) < cost(leftSubTree)) {
					treeCombinations.add(new JoinTree(transformedLeftSubTree, rightSubTree));
					treeCombinations.add(new JoinTree(transformedLeftSubTree, transformedRightSubTree));
					
				}
				if (cost(transformedRightSubTree) < cost(rightSubTree)) {
					treeCombinations.add(new JoinTree(leftSubTree, transformedRightSubTree));
					treeCombinations.add(new JoinTree(transformedLeftSubTree, transformedRightSubTree));
					
				}
				Collections.sort(treeCombinations, new Comparator<JoinTree>() {

					@Override
					public int compare(JoinTree o1, JoinTree o2) {
						double costT1 = cost(o1); 
						double costT2 = cost(o2);
						if (costT1 == costT2) {
							return 0;
						}
						return Double.compare(costT1, costT2);
					}
					
				});
				System.out.println("tree combinations: " + treeCombinations);
				
				if (!treeCombinations.isEmpty()) {
					if (cost(treeCombinations.get(0)) < cost(joinTree)) {
						joinTree = treeCombinations.get(0);

						System.out.println(joinTree);
						JoinTreeUtils.printJoinTree(joinTree, cout);

					}
				} else {
					break;
				}
			}
		}
		
		// local minimum has been reached. - no rule improved the current join tree
		return joinTree;
	}

	private double cost(JoinTree joinTree) {
		return Utils.costCout(joinTree);
	}


	
}

