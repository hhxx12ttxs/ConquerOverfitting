package brainstechnology.predictors;

import brainstechnology.predictors.adaptive.EncogDirectionPredictor;
import fuck.Action;
import fuck.StateParameter;
import algebra.Vector;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static fuck.StateParameter.cosMyHeading;
import static fuck.StateParameter.sinMyHeading;
import static algebra.VectorAlgebra.getAngleBetween;
import static algebra.VectorAlgebra.rotate;

/**
 * Created by user50 on 08.11.2014.
 */
public class EncogDirectionPredictorTest {

    EncogDirectionPredictor predictor;

    @Before
    public void setUp() throws Exception {
        predictor = new EncogDirectionPredictor();
    }

    @Test
    public void testPredictAndAdapt() throws Exception {
        Vector vector = new Vector(1, 0);

        int sign = 0;
        int iterations = 400000;

        for (int i = 0; i < iterations; i++) {
            double rotateAngle = Math.random() * Math.PI - Math.PI/2;

            Map<StateParameter, Double> state = formState(vector);
            Map<Action, Double> action = formAction(rotateAngle);

            vector = rotate(vector, rotateAngle);

            Map<StateParameter, Double> nextState = formState(vector);

            predictor.adapt(state, action, nextState);

            Map<StateParameter, Double> predictedState = new HashMap<>();
            predictor.calculate(state, action, predictedState);

            double factRotate = getAngleBetween(rotate(vector, -rotateAngle), new Vector(predictedState.get(sinMyHeading), predictedState.get(cosMyHeading)));

//            System.out.println("fact: "+factRotate +" expected: "+rotateAngle);

            if (factRotate * rotateAngle > 0)
                sign++;
        }

        System.out.println((double)sign/(double)iterations);

    }

    private Map<Action, Double> formAction(double rotateAngle) {
        Map<Action, Double> action = new HashMap<>();
        action.put(Action.rotate, rotateAngle);

        return action;
    }

    private Map<StateParameter, Double> formState(Vector vector)
    {
        Map<StateParameter, Double> state = new HashMap<>();
        state.put(sinMyHeading, vector.getA());
        state.put(cosMyHeading, vector.getB());
        state.put(StateParameter.velocity, 0.0);

        return state;
    }
}

