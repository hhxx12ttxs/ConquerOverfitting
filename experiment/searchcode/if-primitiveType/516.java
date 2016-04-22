package ast;

import java.util.List;

public class UnaryExp extends Expression {
    UnaryOperator unaryop;
    Expression expression;


    public UnaryExp(UnaryOperator operator, Expression expression) {
        this.expression = expression;
        this.unaryop = operator;
    }

    @Override
    public String gen(int padding) throws UncompatibleTypeException {
        return this.getSpaceFromPadding(padding) + unaryop.toString() + " " + expression.gen(0);
    }

    @Override
    public PrimitiveType getType(List<Definition> definitions) throws UncompatibleTypeException {
        PrimitiveType exprType = expression.getType(definitions);
        boolean isBooleanReturn = (exprType == PrimitiveType.BOOL && unaryop == UnaryOperator.NOT);

        return isBooleanReturn ? PrimitiveType.BOOL : PrimitiveType.INT;

    }

    @Override
    protected void checkErrors(List<Definition> definitions) throws UncompatibleTypeException {
        this.expression.checkErrors(definitions);
        PrimitiveType exprType = expression.getType(definitions);
        boolean isBooleanReturn = (exprType == PrimitiveType.BOOL && unaryop == UnaryOperator.NOT);
        boolean isIntegerReturn = (exprType == PrimitiveType.INT && unaryop == UnaryOperator.MINUS);

        if (!isBooleanReturn && !isIntegerReturn)
        {
            throw new UncompatibleTypeException("UncompatibleType : ! used only with Bool and '-' only with int");
        }
    }
}

