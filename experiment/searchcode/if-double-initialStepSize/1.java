import java.lang.reflect.Field;

public class NavConfig implements Serializable {

public static final double initialStepSize = 0.4;
private static final double initialVals = 0.4;

//before

public OptParam weight_health = new OptParam(0.4f, 0, 1, initialStepSize);

