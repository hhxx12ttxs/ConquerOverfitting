/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.evaluator;

import java.util.*;

import org.jpmml.manager.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.stat.descriptive.rank.*;
import org.apache.commons.math3.stat.descriptive.summary.*;

import org.dmg.pmml.*;

import com.google.common.collect.*;

import org.joda.time.*;

public class FunctionUtil {

	private FunctionUtil(){
	}

	static
	public Object evaluate(Apply apply, List<?> values, EvaluationContext context){
		String name = apply.getFunction();

		Function function = getFunction(name);
		if(function == null){
			DefineFunction defineFunction = context.resolveFunction(name);
			if(defineFunction == null){
				throw new UnsupportedFeatureException(apply);
			}

			return evaluate(defineFunction, values, context);
		}

		return function.evaluate(values);
	}

	static
	public Object evaluate(DefineFunction defineFunction, List<?> values, EvaluationContext context){
		List<ParameterField> parameterFields = defineFunction.getParameterFields();

		if(parameterFields.size() < 1){
			throw new InvalidFeatureException(defineFunction);
		} // End if

		if(parameterFields.size() != values.size()){
			throw new EvaluationException();
		}

		Map<FieldName, Object> arguments = Maps.newLinkedHashMap();

		for(int i = 0; i < parameterFields.size(); i++){
			ParameterField parameterField = parameterFields.get(i);

			Object value = values.get(i);

			DataType dataType = parameterField.getDataType();
			if(dataType != null){
				value = ParameterUtil.cast(dataType, value);
			}

			arguments.put(parameterField.getName(), value);
		}

		Expression expression = defineFunction.getExpression();
		if(expression == null){
			throw new InvalidFeatureException(defineFunction);
		}

		FunctionEvaluationContext functionContext = new FunctionEvaluationContext(context, arguments);

		Object result = ExpressionUtil.evaluate(expression, functionContext);

		DataType dataType = defineFunction.getDataType();
		if(dataType != null){
			result = ParameterUtil.cast(dataType, result);
		}

		return result;
	}

	static
	public Function getFunction(String name){
		return FunctionUtil.functions.get(name);
	}

	static
	public void putFunction(String name, Function function){
		FunctionUtil.functions.put(name, function);
	}

	static
	private Boolean asBoolean(Object value){

		if(value instanceof Boolean){
			return (Boolean)value;
		}

		throw new EvaluationException();
	}

	static
	private Number asNumber(Object value){

		if(value instanceof Number){
			return (Number)value;
		}

		throw new EvaluationException();
	}

	static
	private Integer asInteger(Object value){

		if(value instanceof Integer){
			return (Integer)value;
		}

		throw new EvaluationException();
	}

	static
	private String asString(Object value){

		if(value instanceof String){
			return (String)value;
		}

		throw new EvaluationException();
	}

	static
	private LocalDate asDate(Object value){

		if(value instanceof LocalDate){
			return (LocalDate)value;
		} else

		if(value instanceof LocalDateTime){
			LocalDateTime instant = (LocalDateTime)value;

			return instant.toLocalDate();
		}

		throw new EvaluationException();
	}

	static
	private LocalTime asTime(Object value){

		if(value instanceof LocalTime){
			return (LocalTime)value;
		} else

		if(value instanceof LocalDateTime){
			LocalDateTime instant = (LocalDateTime)value;

			return instant.toLocalTime();
		}

		throw new EvaluationException();
	}

	static
	private LocalDateTime asDateTime(Object value){

		if(value instanceof LocalDate){
			LocalDate instant = (LocalDate)value;

			return new LocalDateTime(instant.getYear(), instant.getMonthOfYear(), instant.getDayOfMonth(), 0, 0, 0);
		} else

		if(value instanceof LocalDateTime){
			return (LocalDateTime)value;
		}

		throw new EvaluationException();
	}

	static
	private Number cast(DataType dataType, Number number){

		switch(dataType){
			case INTEGER:
				if(number instanceof Integer){
					return number;
				}
				return Integer.valueOf(number.intValue());
			case FLOAT:
				if(number instanceof Float){
					return number;
				}
				return Float.valueOf(number.floatValue());
			case DOUBLE:
				if(number instanceof Double){
					return number;
				}
				return Double.valueOf(number.doubleValue());
			default:
				break;
		}

		throw new EvaluationException();
	}

	static
	private DataType integerToDouble(DataType dataType){

		switch(dataType){
			case INTEGER:
				return DataType.DOUBLE;
			default:
				break;
		}

		return dataType;
	}

	private static final Map<String, Function> functions = Maps.newLinkedHashMap();

	public interface Function {

		Object evaluate(List<?> values);
	}

	static
	abstract
	public class ArithmeticFunction implements Function {

		abstract
		public Number evaluate(Number left, Number right);

		@Override
		public Number evaluate(List<?> values){

			if(values.size() != 2){
				throw new EvaluationException();
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if(left == null || right == null){
				return null;
			}

			DataType dataType = ParameterUtil.getResultDataType(left, right);

			Number result;

			try {
				result = evaluate(asNumber(left), asNumber(right));
			} catch(ArithmeticException ae){
				throw new InvalidResultException(null);
			}

			return cast(dataType, result);
		}
	}

	static {
		putFunction("+", new ArithmeticFunction(){

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() + right.doubleValue());
			}
		});

		putFunction("-", new ArithmeticFunction(){

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() - right.doubleValue());
			}
		});

		putFunction("*", new ArithmeticFunction(){

			@Override
			public Double evaluate(Number left, Number right){
				return Double.valueOf(left.doubleValue() * right.doubleValue());
			}
		});

		putFunction("/", new ArithmeticFunction(){

			@Override
			public Number evaluate(Number left, Number right){

				if(left instanceof Integer && right instanceof Integer){
					return Integer.valueOf(left.intValue() / right.intValue());
				}

				return Double.valueOf(left.doubleValue() / right.doubleValue());
			}
		});
	}

	static
	abstract
	public class AggregateFunction implements Function {

		abstract
		public StorelessUnivariateStatistic createStatistic();

		public DataType getResultType(DataType dataType){
			return dataType;
		}

		@Override
		public Number evaluate(List<?> values){
			StorelessUnivariateStatistic statistic = createStatistic();

			DataType dataType = null;

			for(Object value : values){

				if(value == null){
					continue;
				}

				statistic.increment(asNumber(value).doubleValue());

				if(dataType != null){
					dataType = ParameterUtil.getResultDataType(dataType, ParameterUtil.getDataType(value));
				} else

				{
					dataType = ParameterUtil.getDataType(value);
				}
			}

			if(statistic.getN() == 0){
				throw new MissingResultException(null);
			}

			return cast(getResultType(dataType), statistic.getResult());
		}
	}

	static {
		putFunction("min", new AggregateFunction(){

			@Override
			public Min createStatistic(){
				return new Min();
			}
		});

		putFunction("max", new AggregateFunction(){

			@Override
			public Max createStatistic(){
				return new Max();
			}
		});

		putFunction("avg", new AggregateFunction(){

			@Override
			public Mean createStatistic(){
				return new Mean();
			}

			@Override
			public DataType getResultType(DataType dataType){
				return integerToDouble(dataType);
			}
		});

		putFunction("sum", new AggregateFunction(){

			@Override
			public Sum createStatistic(){
				return new Sum();
			}
		});

		putFunction("product", new AggregateFunction(){

			@Override
			public Product createStatistic(){
				return new Product();
			}
		});
	}

	static
	abstract
	public class MathFunction implements Function {

		abstract
		public Double evaluate(Number value);

		public DataType getResultType(DataType dataType){
			return dataType;
		}

		@Override
		public Number evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			Object value = values.get(0);

			DataType dataType = ParameterUtil.getDataType(value);

			return cast(getResultType(dataType), evaluate(asNumber(value)));
		}
	}

	static
	abstract
	public class FpMathFunction extends MathFunction {

		@Override
		public DataType getResultType(DataType dataType){
			return integerToDouble(dataType);
		}
	}

	static {
		putFunction("log10", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.log10(value.doubleValue());
			}
		});

		putFunction("ln", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.log(value.doubleValue());
			}
		});

		putFunction("exp", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.exp(value.doubleValue());
			}
		});

		putFunction("sqrt", new FpMathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.sqrt(value.doubleValue());
			}
		});

		putFunction("abs", new MathFunction(){

			@Override
			public Double evaluate(Number value){
				return Math.abs(value.doubleValue());
			}
		});

		putFunction("pow", new Function(){

			@Override
			public Number evaluate(List<?> values){

				if(values.size() != 2){
					throw new EvaluationException();
				}

				Number left = asNumber(values.get(0));
				Number right = asNumber(values.get(1));

				DataType dataType = ParameterUtil.getResultDataType(left, right);

				Double result = Math.pow(left.doubleValue(), right.doubleValue());

				return cast(dataType, result);
			}
		});

		putFunction("threshold", new Function(){

			@Override
			public Number evaluate(List<?> values){

				if(values.size() != 2){
					throw new EvaluationException();
				}

				Number left = asNumber(values.get(0));
				Number right = asNumber(values.get(1));

				DataType dataType = ParameterUtil.getResultDataType(left, right);

				Integer result = (left.doubleValue() > right.doubleValue()) ? 1 : 0;

				return cast(dataType, result);
			}
		});

		putFunction("floor", new MathFunction(){

			@Override
			public Double evaluate(Number number){
				return Math.floor(number.doubleValue());
			}
		});

		putFunction("ceil", new MathFunction(){

			@Override
			public Double evaluate(Number number){
				return Math.ceil(number.doubleValue());
			}
		});

		putFunction("round", new MathFunction(){

			@Override
			public Double evaluate(Number number){
				return (double)Math.round(number.doubleValue());
			}
		});
	}

	static
	abstract
	public class ValueFunction implements Function {

		abstract
		public Boolean evaluate(Object value);

		@Override
		public Boolean evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			return evaluate(values.get(0));
		}
	}

	static {
		putFunction("isMissing", new ValueFunction(){

			@Override
			public Boolean evaluate(Object value){
				return Boolean.valueOf(value == null);
			}
		});

		putFunction("isNotMissing", new ValueFunction(){

			@Override
			public Boolean evaluate(Object value){
				return Boolean.valueOf(value != null);
			}
		});
	}

	static
	abstract
	public class ComparisonFunction implements Function {

		abstract
		public Boolean evaluate(int order);

		@Override
		public Boolean evaluate(List<?> values){

			if(values.size() != 2){
				throw new EvaluationException();
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if(left == null || right == null){
				throw new EvaluationException();
			}

			DataType dataType = ParameterUtil.getResultDataType(left, right);

			int order = ParameterUtil.compare(dataType, left, right);

			return evaluate(order);
		}
	}

	static {
		putFunction("equal", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int order){
				return Boolean.valueOf(order == 0);
			}
		});

		putFunction("notEqual", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int order){
				return Boolean.valueOf(order != 0);
			}
		});

		putFunction("lessThan", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int order){
				return Boolean.valueOf(order < 0);
			}
		});

		putFunction("lessOrEqual", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int order){
				return Boolean.valueOf(order <= 0);
			}
		});

		putFunction("greaterThan", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int order){
				return Boolean.valueOf(order > 0);
			}
		});

		putFunction("greaterOrEqual", new ComparisonFunction(){

			@Override
			public Boolean evaluate(int order){
				return Boolean.valueOf(order >= 0);
			}
		});
	}

	static
	abstract
	public class BinaryBooleanFunction implements Function {

		abstract
		public Boolean evaluate(Boolean left, Boolean right);

		@Override
		public Boolean evaluate(List<?> values){

			if(values.size() < 2){
				throw new EvaluationException();
			}

			Boolean result = asBoolean(values.get(0));

			for(int i = 1; i < values.size(); i++){
				result = evaluate(result, asBoolean(values.get(i)));
			}

			return result;
		}
	}

	static {
		putFunction("and", new BinaryBooleanFunction(){

			@Override
			public Boolean evaluate(Boolean left, Boolean right){
				return Boolean.valueOf(left.booleanValue() & right.booleanValue());
			}
		});

		putFunction("or", new BinaryBooleanFunction(){

			@Override
			public Boolean evaluate(Boolean left, Boolean right){
				return Boolean.valueOf(left.booleanValue() | right.booleanValue());
			}
		});
	}

	static
	abstract
	public class UnaryBooleanFunction implements Function {

		abstract
		public Boolean evaluate(Boolean value);

		@Override
		public Boolean evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			return evaluate(asBoolean(values.get(0)));
		}
	}

	static {
		putFunction("not", new UnaryBooleanFunction(){

			@Override
			public Boolean evaluate(Boolean value){
				return Boolean.valueOf(!value.booleanValue());
			}
		});
	}

	static
	abstract
	public class ValueListFunction implements Function {

		abstract
		public Boolean evaluate(Object value, List<?> values);

		@Override
		public Boolean evaluate(List<?> values){

			if(values.size() < 2){
				throw new EvaluationException();
			}

			return evaluate(values.get(0), values.subList(1, values.size()));
		}
	}

	static {
		putFunction("isIn", new ValueListFunction(){

			@Override
			public Boolean evaluate(Object value, List<?> values){
				return Boolean.valueOf(values.contains(value));
			}
		});

		putFunction("isNotIn", new ValueListFunction(){

			@Override
			public Boolean evaluate(Object value, List<?> values){
				return Boolean.valueOf(!values.contains(value));
			}
		});
	}

	static {
		putFunction("if", new Function(){

			@Override
			public Object evaluate(List<?> values){

				if(values.size() < 2 || values.size() > 3){
					throw new EvaluationException();
				}

				Boolean flag = asBoolean(values.get(0));

				if(flag.booleanValue()){
					return values.get(1);
				} else

				{
					if(values.size() > 2){
						return values.get(2);
					}

					// XXX
					return null;
				}
			}
		});
	}

	static
	abstract
	public class StringFunction implements Function {

		abstract
		public String evaluate(String value);

		@Override
		public String evaluate(List<?> values){

			if(values.size() != 1){
				throw new EvaluationException();
			}

			return evaluate(asString(values.get(0)));
		}
	}

	static {
		putFunction("uppercase", new StringFunction(){

			@Override
			public String evaluate(String value){
				return value.toUpperCase();
			}
		});

		putFunction("lowercase", new StringFunction(){

			@Override
			public String evaluate(String value){
				return value.toLowerCase();
			}
		});

		putFunction("substring", new Function(){

			@Override
			public String evaluate(List<?> values){

				if(values.size() != 3){
					throw new EvaluationException();
				}

				String value = asString(values.get(0));

				int position = asInteger(values.get(1));
				int length = asInteger(values.get(2));

				if(position <= 0 || length < 0){
					throw new EvaluationException();
				}

				return value.substring(position - 1, (position + length) - 1);
			}
		});

		putFunction("trimBlanks", new StringFunction(){

			@Override
			public String evaluate(String value){
				return value.trim();
			}
		});
	}

	static {
		putFunction("dateDaysSinceYear", new Function(){

			@Override
			public Integer evaluate(List<?> values){

				if(values.size() != 2){
					throw new EvaluationException();
				}

				LocalDate instant = asDate(values.get(0));

				int year = asInteger(values.get(1));

				DaysSinceDate period = new DaysSinceDate(year, instant);

				return period.intValue();
			}
		});

		putFunction("dateSecondsSinceMidnight", new Function(){

			@Override
			public Integer evaluate(List<?> values){

				if(values.size() != 1){
					throw new EvaluationException();
				}

				LocalTime instant = asTime(values.get(0));

				Seconds seconds = Seconds.seconds(instant.getHourOfDay() * 60 * 60 + instant.getMinuteOfHour() * 60 + instant.getSecondOfMinute());

				SecondsSinceMidnight period = new SecondsSinceMidnight(seconds);

				return period.intValue();
			}
		});

		putFunction("dateSecondsSinceYear", new Function(){

			@Override
			public Integer evaluate(List<?> values){

				if(values.size() != 2){
					throw new EvaluationException();
				}

				LocalDateTime instant = asDateTime(values.get(0));

				int year = asInteger(values.get(1));

				SecondsSinceDate period = new SecondsSinceDate(year, instant);

				return period.intValue();
			}
		});
	}
}
