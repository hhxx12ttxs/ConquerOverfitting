public class SimpleSubtractionEquationGenerator extends SimpleArithmeticEquationGenerator {

private static final int FIRST_ARGUMENT_UPPER_LIMIT = 18;
private int generateSecondArgument(int firstArgument) {
int secondArgumentLowerLimit = firstArgument - SECOND_ARGUMENT_UPPER_LIMIT;
if (secondArgumentLowerLimit < 0) {

