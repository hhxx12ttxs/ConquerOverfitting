import com.dborisenko.math.optimization.linear.lexDualSimplexClasses.tableaus.SimplexTableau;
import com.dborisenko.math.optimization.linear.lexDualSimplexClasses.factories.SimplexTableauFactory;
protected transient Double maxArtificialValue = null;

protected transient SimplexTableau tableau;

private transient ZeroColumnsAction zeroColumnsAction = ZeroColumnsAction.ADD_CONSTRAINTS;

