package calculator.finiteStateMachineImpl;

import calculator.api.Solver;
import calculator.api.SolvingException;
import calculator.finiteStateMachineImpl.recognizers.RecognizerFactory;
import calculator.finiteStateMachineImpl.recognizers.commands.InterpretingCommand;
import calculator.functions.FunctionFactoriesHolder;
import calculator.functions.standardFunctions.MaxFunctionFactory;
import calculator.functions.standardFunctions.MinFunctionFactory;
import calculator.functions.standardFunctions.PiFunctionFactory;
import calculator.functions.standardFunctions.SumFunctionFactory;
import calculator.operators.BinaryOperatorFactoriesHolder;
import calculator.operators.standardOperators.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static calculator.finiteStateMachineImpl.State.*;
import static java.util.EnumSet.of;

/**
 * Author: Vladislav Lubenskiy, vlad.lubenskiy@gmail.com
 */
public class FiniteStateMachineSolver implements Solver {
    FunctionFactoriesHolder functionFactoriesHolder = new FunctionFactoriesHolder();
    BinaryOperatorFactoriesHolder operatorFactoriesHolder = new BinaryOperatorFactoriesHolder();
    RecognizerFactory recognizerFactory = new RecognizerFactory(functionFactoriesHolder, operatorFactoriesHolder);

    final Map<State, Set<State>> transitions = new HashMap<State, Set<State>>() {
        {
            put(START, of(NUMBER, OPEN_BRACKET, FUNCTION));
            put(NUMBER, of(OPERATOR, FINISH, CLOSE_BRACKET, ARGUMENTS_DELIMITER));
            put(OPERATOR, of(NUMBER, OPEN_BRACKET, FUNCTION));
            put(CLOSE_BRACKET, of(OPERATOR, FINISH, CLOSE_BRACKET, ARGUMENTS_DELIMITER));
            put(OPEN_BRACKET, of(NUMBER, OPEN_BRACKET, FUNCTION, CLOSE_BRACKET));
            put(FUNCTION, of(OPEN_BRACKET));
            put(ARGUMENTS_DELIMITER, of(NUMBER, FUNCTION, OPEN_BRACKET));
        }
    };

    public FiniteStateMachineSolver() {
        functionFactoriesHolder.add("sum", new SumFunctionFactory());
        functionFactoriesHolder.add("pi", new PiFunctionFactory());
        functionFactoriesHolder.add("max", new MaxFunctionFactory());
        functionFactoriesHolder.add("min", new MinFunctionFactory());

        operatorFactoriesHolder.add("+", new PlusOperatorFactory());
        operatorFactoriesHolder.add("/", new DivideOperatorFactory());
        operatorFactoriesHolder.add("-", new MinusOperatorFactory());
        operatorFactoriesHolder.add("*", new MultiplyOperatorFactory());
        operatorFactoriesHolder.add("^", new PowerOperatorFactory());
    }

    @Override
    public double solve(String expression) throws SolvingException {
        State currentState = START;

        InterpretingContext context = new InterpretingContext(expression);

        while (currentState != FINISH) {

            try {
                currentState = moveForward(currentState, context);
            } catch (Exception e) {
                throw new SolvingException(e.getMessage(), context.getParsingContext().getCursor());
            }

            if (currentState == null)
                throw new SolvingException("Wrong format", context.getParsingContext().getCursor());

        }
        return context.getEvaluatingContext().getArgumentsStack().pop();
    }

    private State moveForward(State state, InterpretingContext context) throws Exception {
        for (State possibleState : transitions.get(state)) {
            InterpretingCommand command = recognizerFactory.get(possibleState).getCommand(context.getParsingContext());
            if (command != null) {
                command.execute(context.getEvaluatingContext());
                return possibleState;
            }
        }
        return null;
    }
}

