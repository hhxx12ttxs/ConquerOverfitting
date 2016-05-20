package calculator.finiteStateMachineImpl;

import calculator.api.SolvingException;
import calculator.api.Solver;
import calculator.finiteStateMachineImpl.parsing.ParserFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static calculator.finiteStateMachineImpl.State.*;
import static java.util.EnumSet.of;

/**
 * Author: Vladislav Lubenskiy, vlad.lubenskiy@gmail.com
 */
public class FiniteStateMachineSolver implements Solver {
    final Map<State, Set<State>> transitions = new HashMap<State, Set<State>>() {
        {
            put(START, of(NUMBER));
            put(NUMBER, of(OPERATOR));
            put(OPERATOR, of(NUMBER));
        }
    };

    @Override
    public double solve(String expression) throws SolvingException {
        State currentState = START;
        ParsingContext context = new ParsingContext(expression);
        while (!context.isParsed()) {
            currentState = moveForward(currentState, context);
            if (currentState == null) throw new SolvingException("Wrong format", context.getCursor());
        }
        finishState(context);
        return context.getArgumentsStack().pop();
    }

    private void finishState(ParsingContext context) {
        while (!context.getOperatorsStack().isEmpty()) {
            double result = context.getOperatorsStack().pop().exec(context.getArgumentsStack());
            context.getArgumentsStack().push(result);
        }
    }

    private State moveForward(State state, ParsingContext context) {
        for (State possibleState : transitions.get(state)) {
            if (parserFactory.get(possibleState).parse(context)) {
                return possibleState;
            }
        }
        return null;
    }

    ParserFactory parserFactory = new ParserFactory();

}

