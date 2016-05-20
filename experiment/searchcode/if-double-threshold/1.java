package lrg.insider.plugins.core.operators.conformityOperators;

import lrg.common.abstractions.entities.ResultEntity;
import lrg.common.abstractions.plugins.operators.ConformityOperatorWithThresholds;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: 04.02.2005
 * Time: 18:48:41
 * To change this template use File | Settings | File Templates.
 */
public class Equal extends ConformityOperatorWithThresholds {
    public Equal() {
        super("=", "numerical");
    }

    public Double apply(ResultEntity theResult, Object threshold) {
    	if (theResult.getValue() instanceof Double == false) return new Double(0);
        if (threshold instanceof Double == false) return new Double(0);

        if ((Double) threshold == 0)
        	return 100.00;
        
        return ((Double) theResult.getValue()) / ((Double) threshold) * 100;
    }
}
