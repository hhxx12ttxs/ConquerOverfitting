for (Group group : sw.getGroups()) {
if (first) {
}
} else if (selector instanceof ComplexArgument) {
ComplexArgument complex = ((ComplexArgument) selector);
if (complex.getArguments().size() != 0)
import org.eclipse.dltk.tcl.ast.ArgumentMatch;
import org.eclipse.dltk.tcl.ast.ComplexString;
import org.eclipse.dltk.tcl.ast.StringArgument;
return ((StringArgument) argument).getValue();
} else if (argument instanceof ComplexString) {
ComplexString complexString = (ComplexString) argument;
first = false;
out.append(inner);
} else if (argument instanceof ComplexArgument) {
ComplexArgument ca = (ComplexArgument) argument;
return DefinitionUtils.equalsArgumentIgnoreName(complex

