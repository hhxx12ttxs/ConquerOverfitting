/*
 * Copyright 2011 Kim Lindhardt Madsen
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package dk.lindhardt.gwt.geie.client.evaluate;

import org.matheclipse.parser.client.ast.ASTNode;
import org.matheclipse.parser.client.ast.FunctionNode;
import org.matheclipse.parser.client.eval.IDouble0Function;

import java.io.Serializable;
import java.util.*;

/**
 * Evaluate math expressions to <code>double</code> numbers.
 */
public class GeieDoubleEvaluator {

   static Map<String, Double> SYMBOL_DOUBLE_MAP;
   static Map<String, Object> FUNCTION_DOUBLE_MAP;

   public static double EPSILON = 1.0e-15;

   static class NFunction implements IDouble1Function {
      public Double evaluate(Serializable arg1) {
         if (arg1 instanceof Boolean) {
            if (Boolean.TRUE.equals(arg1)) {
               return 1.0;
            } else if (Boolean.FALSE.equals(arg1)) {
               return 0.0;
            }
         } else if (arg1 instanceof Double) {
            return (Double) arg1;
         } else if (arg1 instanceof Date) {
            //TODO make correct serial number
            return (double) ((Date) arg1).getTime();
         }
         return 0.0;
      }
   }

   static class MaxFunction extends IDoubleDouble2Function implements IRangeFunction<Double> {

      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         double result = Double.MIN_VALUE;
         int end = function.size();
         for (int i = 1; i < end; i++) {
            Serializable value = engine.evaluateNode(function.getNode(i));
            if (value instanceof Range) {
               Range range = (Range) value;
               for (Integer row : range.keySet()) {
                  HashMap<Integer, Serializable> rowMap = range.get(row);
                  for (Integer column : rowMap.keySet()) {
                     result = updateMax(result, rowMap.get(column));
                  }
               }
            } else {
               result = updateMax(result, value);
            }
         }
         return result;
      }

      private Double updateMax(double result, Serializable value) {
         double temp;
         if (value instanceof Double) {
            temp = Math.max(result, (Double) value);
            if (temp > result) {
               result = temp;
            }
         }
         return result;
      }

      @Override
      public String getFunctionName() {
         return "max";
      }

      @Override
      Double evaluateDouble(double arg1, double arg2) {
         return Math.max(arg1, arg2);
      }
   }

   static class MinFunction extends IDoubleDouble2Function implements IRangeFunction<Double> {
      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         double result = Double.MAX_VALUE;
         int end = function.size();
         for (int i = 1; i < end; i++) {
            Serializable value = engine.evaluateNode(function.getNode(i));
            if (value instanceof Range) {
               Range range = (Range) value;
               for (Integer row : range.keySet()) {
                  HashMap<Integer, Serializable> rowMap = range.get(row);
                  for (Integer column : rowMap.keySet()) {
                     result = updateMin(result, rowMap.get(column));
                  }
               }
            } else {
               result = updateMin(result, value);
            }
         }
         return result;
      }

      private double updateMin(double result, Serializable value) {
         double temp;
         if (value instanceof Double) {
            temp = Math.min(result, (Double) value);
            if (temp < result) {
               result = temp;
            }
         }
         return result;
      }

      @Override
      public String getFunctionName() {
         return "min";
      }

      @Override
      public Double evaluateDouble(double arg1, double arg2) {
         return Math.min(arg1, arg2);
      }
   }

   static class PlusFunction extends IDoubleDouble2Function implements IRangeFunction {
      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         double result = 0.0;
         for (int i = 1; i < function.size(); i++) {
            Serializable value = engine.evaluateNode(function.getNode(i));
            if (value instanceof Double) {
               result += (Double) value;
            }
         }
         return result;
      }

      @Override
      public String getFunctionName() {
         return "plus";
      }

      @Override
      Double evaluateDouble(double arg1, double arg2) {
         return arg1 + arg2;
      }
   }

   static class TimesFunction extends IDoubleDouble2Function implements IRangeFunction<Double> {
      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         double result = 1.0;
         for (int i = 1; i < function.size(); i++) {
            Serializable value = engine.evaluateNode(function.getNode(i));
            if (value instanceof Range) {
               List<Serializable> values = ((Range) value).getValues();
               for (Serializable rangeValue : values) {
                  if (rangeValue instanceof Double) {
                     result *= (Double) rangeValue;
                  }
               }
            }
            if (value instanceof Double) {
               result *= (Double) value;
            }
         }
         return result;
      }

      @Override
      public String getFunctionName() {
         return "times";
      }

      @Override
      Double evaluateDouble(double arg1, double arg2) {
         return arg1 * arg2;
      }
   }

   static class CountFunction implements IRangeFunction<Double> {
      public String getFunctionName() {
         return "count";
      }

      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         int result = 0;
         for (int i = 1; i < function.size(); i++) {
            ASTNode node = function.get(i);
            if (node instanceof FunctionNode && "colon".equals(((FunctionNode) node).get(0).getString().toLowerCase())) {
               Serializable value = engine.evaluateNode(node);
               if (value instanceof Double) {
                  result += (Double) value;
               } else if (value instanceof Date) {
                  result++;
               }
            } else {
               Serializable value = engine.evaluateNode(node);
               if (value instanceof Double || value instanceof Date) {
                  result++;
               }
            }
         }
         return (double) result;
      }
   }

   static class SumFunction extends IDoubleDouble1And2Function implements IRangeFunction<Double> {
      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         double result = 0.0;
         for (int i = 1; i < function.size(); i++) {
            ASTNode node = function.getNode(i);
            Serializable value = engine.evaluateNode(node);
            if (value instanceof Range) {
               List<Serializable> values = ((Range) value).getValues();
               for (Serializable rangeValue : values) {
                  if (rangeValue instanceof Double) {
                     result += (Double) rangeValue;
                  }
               }
            }
            if (value instanceof Double) {
               result += (Double) value;
            }
         }
         return result;
      }

      @Override
      public String getFunctionName() {
         return "sum";
      }

      @Override
      Double evaluateDouble(double arg1) {
         return arg1;
      }

      @Override
      Double evaluateDouble(double arg1, double arg2) {
         return arg1 + arg2;
      }
   }

   static class LargeFunction implements IRangeFunction<Double> {

      public String getFunctionName() {
         return "large";
      }

      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         List<Double> list = new ArrayList<Double>();
         if (function.size() > 3) {
            throw new IllegalArgumentException(getFunctionName() + " only takes two arguments");
         }
         Serializable value1 = engine.evaluateNode(function.getNode(1));
         if (value1 instanceof Range) {
               List<Serializable> values = ((Range) value1).getValues();
               for (Serializable rangeValue : values) {
                  if (rangeValue instanceof Double) {
                     list.add((Double) rangeValue);
                  }
               }
         } else {
            throw new IllegalArgumentException(getFunctionName() + " argument 1 must be a range");
         }
         Collections.sort(list);
         Serializable value2 = engine.evaluateNode(function.getNode(2));
         if (value2 instanceof Double) {
            double dv = (Double) value2;
            int index = list.size() - (int) dv;
            if (index < 0) {
               throw new IllegalArgumentException(getFunctionName() + " argument 2 must be larger than 0 and smaller than the matrix size");
            }
            return list.get(index);
         } else {
            throw new IllegalArgumentException(getFunctionName() + " argument 2 must be an integer");
         }
      }
   }

   static class SmallFunction implements IRangeFunction<Double> {

      public String getFunctionName() {
         return "small";
      }

      public Double evaluate(GeieEvaluator engine, FunctionNode function) {
         List<Double> list = new ArrayList<Double>();
         if (function.size() > 3) {
            throw new IllegalArgumentException(getFunctionName() + " only takes two arguments");
         }
         Serializable value1 = engine.evaluateNode(function.getNode(1));
         if (value1 instanceof Range) {
               List<Serializable> values = ((Range) value1).getValues();
               for (Serializable rangeValue : values) {
                  if (rangeValue instanceof Double) {
                     list.add((Double) rangeValue);
                  }
               }
         } else {
            throw new IllegalArgumentException(getFunctionName() + " argument 1 must be a range");
         }
         Collections.sort(list);
         Collections.reverse(list);
         Serializable value2 = engine.evaluateNode(function.getNode(2));
         if (value2 instanceof Double) {
            double dv = (Double) value2;
            int index = list.size() - (int) dv;
            if (index < 0) {
               throw new IllegalArgumentException(getFunctionName() + " argument 2 must be larger than 0 and smaller than the matrix size");
            }
            return list.get(index);
         } else {
            throw new IllegalArgumentException(getFunctionName() + " argument 2 must be an integer");
         }
      }
   }

   static void addFunction(String name, IFunction function) {
      function.setFunctionName(name);
      FUNCTION_DOUBLE_MAP.put(name, function);
   }

   static {
      SYMBOL_DOUBLE_MAP = new HashMap<String, Double>();

      FUNCTION_DOUBLE_MAP = new HashMap<String, Object>();
      FUNCTION_DOUBLE_MAP.put("max", new MaxFunction());
      FUNCTION_DOUBLE_MAP.put("min", new MinFunction());
      FUNCTION_DOUBLE_MAP.put("plus", new PlusFunction());
      FUNCTION_DOUBLE_MAP.put("times", new TimesFunction());
      FUNCTION_DOUBLE_MAP.put("product", new TimesFunction());
      FUNCTION_DOUBLE_MAP.put("sum", new SumFunction());
      FUNCTION_DOUBLE_MAP.put("count", new CountFunction());
      FUNCTION_DOUBLE_MAP.put("n", new NFunction());
      FUNCTION_DOUBLE_MAP.put("large", new LargeFunction());
      FUNCTION_DOUBLE_MAP.put("small", new SmallFunction());
      //
      // Functions with 0 arguments
      //
      FUNCTION_DOUBLE_MAP.put(
            "pi", new IDouble0Function() {
               public double evaluate() {
                  return Math.PI;
               }
            });
      FUNCTION_DOUBLE_MAP.put(
            "rand", new IDouble0Function() {
               public double evaluate() {
                  return Math.random();
               }
            });

      //
      // Functions with 1 argument
      //
      addFunction(
            "percent", new IDoubleDouble1Function() {
               @Override
               Double evaluateDouble(double arg1) {
                  return arg1 / 100;
               }
            });
      FUNCTION_DOUBLE_MAP.put(
            "abs", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "abs";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.abs(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "acos", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "acos";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.acos(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "asin", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "asin";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.asin(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "int", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "int";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  int intValue = (int) arg1;
                  return (double) intValue;
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "cos", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "cos";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.cos(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "cosh", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "cosh";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.cosh(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "log", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "log";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.log(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "log10", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "log10";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.log10(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "sqrt", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "sqrt";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.sqrt(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "sin", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "sin";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.sin(arg1);
               }
            });

      FUNCTION_DOUBLE_MAP.put(
            "sinh", new IDoubleDouble1Function() {
               @Override
               public String getFunctionName() {
                  return "sinh";
               }

               @Override
               Double evaluateDouble(double arg1) {
                  return Math.sinh(arg1);
               }
            });
      addFunction(
            "atan", new IDoubleDouble1Function() {
               public Double evaluateDouble(double arg1) {
                  return Math.atan(arg1);
               }
            });
      addFunction(
            "tan", new IDoubleDouble1Function() {
               public Double evaluateDouble(double arg1) {
                  return Math.tan(arg1);
               }
            });
      addFunction(
            "tanh", new IDoubleDouble1Function() {
               public Double evaluateDouble(double arg1) {
                  return Math.tanh(arg1);
               }
            });
      addFunction(
            "exp", new IDoubleDouble1Function() {
               public Double evaluateDouble(double arg1) {
                  return Math.exp(arg1);
               }
            });
      addFunction(
            "sign", new IDoubleDouble1Function() {
               public Double evaluateDouble(double arg1) {
                  return Math.signum(arg1);
               }
            });
      addFunction(
            "fact", new IDoubleDouble1Function() {
               @Override
               Double evaluateDouble(double arg1) {
                  double rint = Math.rint(arg1);
                  if (Math.abs(rint - arg1) > EPSILON) {
                     throw new IllegalArgumentException(getFunctionName() + " only takes integer arguments");
                  } else {
                     double result = 1;
                     for (double i = arg1; i > EPSILON; i--) {
                        result *= i;
                     }
                     return result;
                  }
               }
            });
      addFunction(
            "year", new IDoubleDate1Function() {
               @Override
               Double evaluateDouble(Date arg1) {
                  return (double) arg1.getYear() + 1900;
               }
            });
      addFunction(
            "month", new IDoubleDate1Function() {
               @Override
               Double evaluateDouble(Date arg1) {
                  return (double) arg1.getMonth() + 1;
               }
            });
      addFunction(
            "day", new IDoubleDate1Function() {
               @Override
               Double evaluateDouble(Date arg1) {
                  return (double) arg1.getDate();
               }
            });
      addFunction(
            "hour", new IDoubleDate1Function() {
               @Override
               Double evaluateDouble(Date arg1) {
                  return (double) arg1.getHours();
               }
            });
      addFunction(
            "minute", new IDoubleDate1Function() {
               @Override
               Double evaluateDouble(Date arg1) {
                  return (double) arg1.getMinutes();
               }
            });
      addFunction(
            "second", new IDoubleDate1Function() {
               @Override
               Double evaluateDouble(Date arg1) {
                  return (double) arg1.getSeconds();
               }
            });
      FUNCTION_DOUBLE_MAP.put(
            "weekday", new IDouble1Function() {
               public Double evaluate(Serializable arg1) {
                  if (arg1 instanceof Date) {
                     return (double) ((Date) arg1).getDay() + 1;
                  } else if (arg1 instanceof Double) {
                     Double arg = (Double) arg1;
                     double rint = Math.rint(arg);
                     if (!(Math.abs(rint - arg) > EPSILON)) {
                        return (double) new Date((long) rint).getDay() + 1;
                     }
                  } else if (arg1 instanceof String) {
                     //TODO:
                  }
                  throw new IllegalArgumentException("weekday cannot take " + arg1.toString() + " as an argument");
               }
            });

      //
      // Functions with 2 arguments
      //
      addFunction(
            "power", new IDoubleDouble2Function() {
               public Double evaluateDouble(double arg1, double arg2) {
                  return Math.pow(arg1, arg2);
               }
            });
      addFunction(
            "mod", new IDoubleDouble2Function() {
               public Double evaluateDouble(double arg1, double arg2) {
                  return arg1 % arg2;
               }
            });
      addFunction(
            "arctan2", new IDoubleDouble2Function() {
               public Double evaluateDouble(double arg1, double arg2) {
                  return Math.atan2(arg2, arg1);
               }
            });
      addFunction(
            "round", new IDoubleDouble2Function() {
               public Double evaluateDouble(double arg1, double arg2) {
                  if (arg2 == 0) {
                     return (double) Math.round(arg1);
                  }
                  return Math.round(arg1 * (10.0 * arg2)) / (10.0 * arg2);
               }
            });
   }

   private GeieEvaluator geieEvaluator;

   public GeieDoubleEvaluator(GeieEvaluator geieEvaluator) {
      this.geieEvaluator = geieEvaluator;
   }

   /**
    * Evaluate an already parsed in <code>FunctionNode</code> into a
    * <code>double</code> number value.
    *
    * @param functionNode evaluate a function node
    * @return the evaluated value
    *
    * @throws ArithmeticException
    *           if the <code>functionNode</code> cannot be evaluated.
    */
   public Double evaluateFunction(final FunctionNode functionNode) {
      String symbol = functionNode.get(0).getString().toLowerCase();
      Object obj = FUNCTION_DOUBLE_MAP.get(symbol.toLowerCase());
      if (obj instanceof IRangeFunction) {
         return ((IRangeFunction<Double>) obj).evaluate(geieEvaluator, functionNode);
      }
      if (functionNode.size() == 1) {
         if (obj instanceof IDouble0Function) {
            return ((IDouble0Function) obj).evaluate();
         }
      } else if (functionNode.size() == 2) {
         if (obj instanceof IDouble1Function) {
            return ((IDouble1Function) obj).evaluate(
                  geieEvaluator.evaluateNode(functionNode.getNode(1)));
         }
         if (obj instanceof IDouble1And2Function) {
            return ((IDouble1And2Function) obj).evaluate(
                  geieEvaluator.evaluateNode(functionNode.getNode(1)));
         }
      } else if (functionNode.size() == 3) {
         if (obj instanceof IDouble2Function) {
            return ((IDouble2Function) obj).evaluate(
                  geieEvaluator.evaluateNode(functionNode.getNode(1)),
                  geieEvaluator.evaluateNode(functionNode.getNode(2)));
         }
         if (obj instanceof IDouble1And2Function) {
            return ((IDouble1And2Function) obj).evaluate(
                  geieEvaluator.evaluateNode(functionNode.getNode(1)),
                  geieEvaluator.evaluateNode(functionNode.getNode(2)));
         }
      }

      throw new ArithmeticException("Evaluating not possible for: " + functionNode.toString());
   }


}

