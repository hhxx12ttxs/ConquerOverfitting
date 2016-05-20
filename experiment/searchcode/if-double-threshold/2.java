package lrg.insider.plugins.core.operators.conformityOperators;

import lrg.common.abstractions.entities.ResultEntity;
import lrg.common.abstractions.plugins.operators.ConformityOperatorWithThresholds;

/**
 * Created by Horia Radu.
 * User: horia
 * Date: 03.05.2004
 * Time: 12:52:02
 * To change this template use File | Settings | File Templates.
 */
public class HigherThanOperator extends ConformityOperatorWithThresholds
{
    public HigherThanOperator()
    {
        super(">", "numerical");
    }

    public Double apply(ResultEntity theResult, Object threshold)
    {
        if (theResult.getValue() instanceof Double == false) return new Double(0);
        if (threshold instanceof Double == false) return new Double(0);

        if ((Double) threshold == 0)
        	return 100.00;
        
        double result = ((Double) theResult.getValue()) / ((Double) threshold) * 100;
        return result;
    }
}

