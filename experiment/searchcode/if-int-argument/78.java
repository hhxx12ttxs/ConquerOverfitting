public abstract class UnaryOperatorFormula extends OperatorFormula {

protected Formula argument;

public UnaryOperatorFormula(Formula argument) {
if (argument == null) {
if (other.argument != null)
return false;
} else if (!argument.equals(other.argument))

