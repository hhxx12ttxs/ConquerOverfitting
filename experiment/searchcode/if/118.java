package org.t2framework.recl.expression;

import org.t2framework.recl.ValidationContext;
import org.t2framework.recl.expression.validator.Validator;
import org.t2framework.recl.type.BooleanType;
import org.t2framework.recl.visitor.Visitor;

public class If<CTX extends ValidationContext> implements
		Function<CTX, ConditionResult<CTX>> {

	Validator<CTX> func;

	Function<CTX, BooleanType> successFunc;

	Function<CTX, BooleanType> failFunc;

	public If(Validator<CTX> func, Function<CTX, BooleanType> successFunc) {
		this(func, successFunc, null);
	}

	public If(Validator<CTX> func, Function<CTX, BooleanType> successFunc,
			Function<CTX, BooleanType> failFunc) {
		this.func = func;
		this.successFunc = successFunc;
		this.failFunc = failFunc;
	}

	@Override
	public void accept(Visitor<CTX> visitor, CTX context) {
		visitor.visit(this, context);
	}

	@Override
	public ConditionResult<CTX> apply(CTX context) {
		BooleanType ret = func.apply(context);
		if (ret == BooleanType.TRUE) {
			return new ConditionResult<CTX>(successFunc);
		} else {
			return new ConditionResult<CTX>(failFunc);
		}
	}

}

