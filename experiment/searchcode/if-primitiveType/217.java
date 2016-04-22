package ast;

import java.util.List;

public class BinExp extends Expression{
    Expression expression1;
    Operator operator;
    Expression expression2;

    public BinExp(Expression expression1, Operator op, Expression expression2) {
        this.expression1 = expression1;
        this.operator = op;
        this.expression2 = expression2;
    }

    @Override
    public String gen(int padding) throws UncompatibleTypeException {
        return this.getSpaceFromPadding(padding) + expression1.gen(0) + operator.toString() + expression2.gen(0);
    }

    @Override
    public PrimitiveType getType(List<Definition> definitions) throws UncompatibleTypeException {
        PrimitiveType expr2Type = expression2.getType(definitions);

        if (Operator.getOnlyArithmeticOperators().contains(operator) && expr2Type == PrimitiveType.INT)
        {
            return PrimitiveType.INT;
        }

        return PrimitiveType.BOOL;
    }

    @Override
    protected void checkErrors(List<Definition> definitions) throws UncompatibleTypeException {

        expression1.checkErrors(definitions);
        expression2.checkErrors(definitions);

        PrimitiveType expr1Type = expression1.getType(definitions);
        PrimitiveType expr2Type = expression2.getType(definitions);

        if (expr1Type != expr2Type)
        {
            throw new UncompatibleTypeException("You must use same type for a binary operator");
        }

        boolean isBooleanReturn = (Operator.getCommonOperators().contains(operator)
                || Operator.getOnlyBoolOperators().contains(operator) && expr1Type == PrimitiveType.BOOL
                || Operator.getArithmeticComparisonOperator().contains(operator) && expr1Type == PrimitiveType.INT
        );

        boolean isIntegerReturn = (Operator.getOnlyArithmeticOperators().contains(operator)
                && expr2Type == PrimitiveType.INT
        );

        if (!isBooleanReturn && !isIntegerReturn)
        {
            throw new UncompatibleTypeException("You not associated well operator and expression type");
        }


        if (isIntegerReturn)
        {
            if (expression2 instanceof IntLit && operator == Operator.DIVIDE)
            {
                if (((IntLit) expression2).value == 0)
                {
                    throw new RuntimeException("You can't divide by 0");
                }
            }
        }

    }
}

