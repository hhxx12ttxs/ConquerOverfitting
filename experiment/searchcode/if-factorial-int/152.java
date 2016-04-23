package cz.matfyz.minirisk.clients.isa.jiri.afp;

import java.lang.Math;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.matfyz.isa.jiri.jnash.SolutionSingle;
import cz.matfyz.isa.jiri.jnash.TwoPlayerConstSum;

public class Marvin {
	private Random rnd = null;
	private Map<Situation, Double> openingVal = new HashMap<Situation, Double>();
	private Map<Situation, Integer> openingCnt = new HashMap<Situation, Integer>();

	public Marvin() {
		this(new Random(42));
	}

	public Marvin(Random rnd) {
		this.rnd = rnd;
	}

	public static String getName() {
		return "Marvin";
	}

	public int advise(GameState state) {
		MixedStrategy ms = null;
		for(int opponent = 0; opponent < state.getNumberOfOpponents(); opponent++) {
			MixedStrategy s = this.advise(state, opponent);
			if((ms == null) || (s.getExpectedUtility() < ms.getExpectedUtility())) {
				ms = s;
			}
		}

		return ms.sample(this.rnd);
	}

	protected MixedStrategy advise(GameState state, int opponent) {
		Factor f = new Factor(state.restrict(opponent));
		//int[] myActions = state.myAvailableBids();
		//int[] herActions = state.othersAvailableBids()[opponent];
		int[] myActions = f.myAvailableBids();
		int[] herActions = f.herAvailableBids();
		int myScore = state.myScore();
		int herScore = state.othersScores()[opponent];
		double[][] payoffs = new double[myActions.length][herActions.length];
		int onTable = 0;

		for(int c : state.getDeck()) {
			onTable += c;
		}

		for(int i = 0; i < myActions.length; i++) {
			for(int j = 0; j < herActions.length; j++) {
				GameState nextState = (GameState) state.restrict(opponent);
				int[] gain = new int[2];
				int leftOver = 0;
				if(onTable > 0) {
					if(myActions[i] > herActions[j]) {
						gain[0] = onTable;
						gain[1] = 0;
					} else if(myActions[i] < herActions[j]) {
						gain[0] = 0;
						gain[1] = onTable;
					} else {
						leftOver = onTable;
					}
				} else if(onTable <= 0) {
					if(myActions[i] < herActions[j]) {
						gain[0] = onTable;
						gain[1] = 0;
					} else if(myActions[i] > herActions[j]) {
						gain[0] = 0;
						gain[1] = onTable;
					} else {
						leftOver = onTable;
					}
				}
				//nextState.update(new int[]{myActions[i], herActions[j]}, gain);
				int myUnfactoredAction = state.myAvailableBids()[myActions[i]/2];
				int id = 0;
				for(int k = 0; k < herActions[j]; k++) {
					id += f.getFactors()[k];
				}
				int herUnfactoredAction = state.othersAvailableBids()[opponent][id];
				nextState.update(new int[]{myUnfactoredAction, herUnfactoredAction}, gain);
				/* A current state. */
				payoffs[i][j] = nextState.myScore();
				/* A pessimistic expectation of the future. */
				payoffs[i][j] += this.expect(nextState, leftOver);
			}
		}

		MixedStrategy solution = solve(myActions, herActions, payoffs);
		solution.unfactor(state.myAvailableBids());
		return solution;
	}

	/*Attempts to find the most pessimistic utility estimate available. */
	protected double expect(GameState nextState, int leftOver) {
		final int MIXER_CNT = 120; //150;
		Factor f = new Factor(nextState);
		int[] cardsLeft = nextState.getCards();
		//int[] myActions = nextState.myAvailableBids();
		//int[] herActions = nextState.othersAvailableBids()[0];
		int[] myActions = f.myAvailableBidsRepeated();
		int[] herActions = f.herAvailableBidsRepeated();
		int mixer_cnt = (int) Math.min(MIXER_CNT, 30*factorial(myActions.length));
		/* And I happen to know that the actions are sorted. */

		assert(myActions.length == herActions.length);

		/*
		if(myActions.length == 0) {
			return leftOver/2.0; //there is nothing left to gain [Marvin]
		}

		if(myActions.length == 1) {
			double result = evalTrace(leftOver, myActions, herActions, cardsLeft);

			GameTree t = new GameTree(f, cardsLeft, leftOver);
			System.err.println("Compare[1]: " + result + " " + t.getUtility());

			return result;
		}

		if(myActions.length == 2) {
			double result = 0.0;
			int[][] myStrategies = {(int[]) myActions.clone(), revert((int[]) myActions.clone())};
			int[][] herStrategies = {(int[]) herActions.clone(), revert((int[]) herActions.clone())};
			double[][] payoffs = new double[myStrategies.length][herStrategies.length];
			for(int k = 0; k < 2; k++) { //two possible orders of the two remaining deck cards
				for(int i = 0; i < myStrategies.length; i++) {
					for(int j = 0; j < herStrategies.length; j++) {
						payoffs[i][j] = evalTrace(leftOver, myStrategies[i], herStrategies[j], cardsLeft);
					}
				}
				result += TwoPlayerConstSum.solveSingle(payoffs).getUtility() / 2.0;
				revert(cardsLeft); //Prepare the other of the remaining two deck cards. The second reversion is ignored.
			}

			GameTree t = new GameTree(f, cardsLeft, leftOver);
			double result2 = 0.0;
			int[][] myStrategies2 = {(int[]) f.myAvailableBidsRepeated(), revert((int[]) f.myAvailableBidsRepeated())};
			int[][] herStrategies2 = {(int[]) f.herAvailableBidsRepeated(), revert((int[]) f.herAvailableBidsRepeated())};
			double[][] payoffs2 = new double[myStrategies2.length][herStrategies2.length];
			for(int k = 0; k < 2; k++) { //two possible orders of the two remaining deck cards
				for(int i = 0; i < myStrategies2.length; i++) {
					for(int j = 0; j < herStrategies2.length; j++) {
						payoffs2[i][j] = evalTrace(leftOver, myStrategies2[i], herStrategies2[j], cardsLeft);
					}
				}
				result2 += TwoPlayerConstSum.solveSingle(payoffs2).getUtility() / 2.0;
				revert(cardsLeft); //Prepare the other of the remaining two deck cards. The second reversion is ignored.
			}

			System.err.println("Compare[2]: " + result + " " + result2 + " " + t.getUtility() + " | " + Arrays.toString(cardsLeft) + " | " + leftOver);

			return result;
		}
		*/

		GameTree t = null;
		if(nextState.myAvailableBids().length <= 6) { //TODO boundary
			/*GameTree*/ t = new GameTree(f, cardsLeft, leftOver);
			return t.getUtility();
		}

		Situation situation = new Situation(f, cardsLeft, leftOver);
		Integer Cnt = openingCnt.get(situation);
		if((Cnt != null) && (Cnt > 8 /*50*/)) {
			//System.err.println("yup. " + openingVal.size() + " " + openingCnt.size());
			return openingVal.get(situation);
		}

		LinkedHashSet<Strategy> myStrategies = new LinkedHashSet<Strategy>();
		LinkedHashSet<Strategy> herStrategies = new LinkedHashSet<Strategy>();
		for(int k = 0; k < 2*myActions.length; rotate(myActions), rotate(herActions), k++) {
			if(k == myActions.length) {
				revert(myActions);
				revert(herActions);
			}
			myStrategies.add(new Strategy(myActions));
			herStrategies.add(new Strategy(herActions));
		}

		for(int k = 0; k < mixer_cnt; k++) {
			mix(myActions);
			mix(herActions);

			myStrategies.add(new Strategy(myActions));
			herStrategies.add(new Strategy(herActions));
		}

		double[][] payoffs = new double[myStrategies.size()][herStrategies.size()];
		int i = 0;
		for(Strategy myStrategy : myStrategies) {
			int j = 0;
			for(Strategy herStrategy : herStrategies) {
				payoffs[i][j] = eval(myStrategy.actions, herStrategy.actions, cardsLeft, leftOver);
				j++;
			}
			i++;
		}

		SolutionSingle solution = TwoPlayerConstSum.solveSingle(payoffs);
		/*
		if(t != null) {
			System.err.println("peek: " + t.getUtility() + " " + solution.getUtility() + " | " + Arrays.toString(cardsLeft) + " | " + leftOver);
		}
		*/

		if(nextState.myAvailableBids().length > 11) {
			if(Cnt == null) {
				openingVal.put(situation, solution.getUtility());
				openingCnt.put(situation, 1);
			} else {
				int cnt = 1 + Cnt;
				double v = openingVal.get(situation);
				double val = v * ((cnt - 1.0) / (double) cnt) + solution.getUtility() / (double) cnt;
				//double val = Math.min(v, solution.getUtility()); //Marvinistic expectation of the worst
				openingVal.put(situation, val);
				openingCnt.put(situation, cnt);
				return val;
			}
		}

		return solution.getUtility();
	}

	/* Beware of wolfs. This functions operates in place. A handle to its parameter is returned for convenience.*/
	public static int[] rotate(int[] values) {
		int first = values[0];

		for(int i = 0; i < values.length - 1; i++) {
			values[i] = values[i+1];
		}

		values[values.length-1] = first;
		return values;
	}

	/* Beware of wolfs. This functions operates in place. A handle to its parameter is returned for convenience.*/
	public static int[] revert(int[] values) {
		for(int i = 0; i < values.length / 2; i++) {
			int tail = values.length - 1 - i;
			int tmp = values[i];
			values[i] = values[tail];
			values[tail] = tmp;
		}

		return values;
	}

	protected void mix(int[] values) {
		for(int i = values.length - 1; i > 0; i--) {
			int id = this.rnd.nextInt(i);
			int tmp = values[i];
			values[i] = values[id];
			values[id] = tmp;
		}
	}

	protected long product(int from, int to) {
		long result = 1;

		for(int i = from; i <= to; i++) {
			result *= i;
		}

		return result;
	}

	protected long factorial(int n) {
		return product(2, n);
	}


	protected double eval(int[] myStrategy, int[] herStrategy, int[] cards, int leftOver) {
		assert(myStrategy.length == herStrategy.length);
		assert(myStrategy.length == cards.length);

		int val = 0;
		for(int i = 0; i < cards.length; i++) {
			if((cards[i] > 0) && (myStrategy[i] > herStrategy[i])) { // "<" by Marvin
				val += cards[i];
			} else if((cards[i] < 0) && (myStrategy[i] <= herStrategy[i])) { // "<=" by Marvin
				val += cards[i];
			}
		}

		return val;
	}

	public static double evalTrace(int leftOver, int[] myActions, int[] herActions, int[] cards) {
		double result = 0.0;

		for(int i = 0; i < myActions.length; i++) {
			if(cards[i] + leftOver > 0) {
				if(myActions[i] > herActions[i]) {
					result += cards[i] + leftOver;
					leftOver = 0;
				} else if(myActions[i] == herActions[i]) {
					leftOver += cards[i];
				} else {
					leftOver = 0;
				}
			} else {
				if(myActions[i] < herActions[i]) {
					result += cards[i] + leftOver;
					leftOver = 0;
				} else if(myActions[i] == herActions[i]) {
					leftOver += cards[i];
				} else {
					leftOver = 0;
				}
			}
		}

		result += leftOver / 2.0;
		return result;
	}

	protected MixedStrategy solve(int[] myActions, int[] herActions, double[][] payoffs) {
		SolutionSingle solution = TwoPlayerConstSum.solveSingle(payoffs);
		/*
		for(int i = 0; i < payoffs.length; i++) {
			System.err.println("P: " + Arrays.toString(payoffs[i]));
		}
		*/
		double[][] actionDistribution = solution.getActionDistribution();
		/*
		for(double[] p : payoffs) {
			System.err.println("p: " + Arrays.toString(p));
		}
		*/
		//System.err.println("A: " + Arrays.toString(actionDistribution[0]));
		//System.err.println("B: " + Arrays.toString(actionDistribution[1]));
		/*
		double[] ut = new double[myActions.length];
		for(int i = 0; i < ut.length; i++) {
			for(int j = 0; j < herActions.length; j++) {
				ut[i] += actionDistribution[0][i] * actionDistribution[1][j] * payoffs[i][j];
			}
		}
		System.err.println("u: " + Arrays.toString(ut));
		*/
		return new MixedStrategy(myActions, solution.getActionDistribution()[0], solution.getUtility());
	}
}

