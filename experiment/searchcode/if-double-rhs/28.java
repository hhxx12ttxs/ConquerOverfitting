/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.evaluator.enums;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nasa.arc.mct.components.FeedProvider;
import gov.nasa.arc.mct.evaluator.api.Executor;
import gov.nasa.arc.mct.evaluator.expressions.MultiRuleExpression;
import gov.nasa.arc.mct.evaluator.spi.MultiProvider;

/**
 * The Multi-input evaluator implementation class.
 *
 */
public class MultiEvaluator  implements MultiProvider {

	/** Language string constant. */
	public static final String LANGUAGE_STRING = "multi";
	
	/** Not equals character. */
	public static final String NOT_EQUALS = "\u2260";
	private static final String ruleMatcher = 
			"(SINGLE_PARAMETER|MORE_THAN_ONE_PARAMETER|ALL_PARAMETERS)[\t]([\\S]*)[\t]([\\S]*)[\t]([\u2260<>=])[\t]([-]?[\\d]*[\\.]?[\\d]*)[\t]([^\t]*)[\t]([^\t]*)[|]";
	
	/** Multi-input rule expression pattern. */
	public static final Pattern multiExpressionPattern = Pattern.compile(ruleMatcher);
	private List<MultiExpression> expressions;
	
	@Override
	public Executor compile(String code) {
		Matcher m = multiExpressionPattern.matcher(code);
		expressions = new ArrayList<MultiExpression>();
		while (m.find()){
			assert m.groupCount() == 7 : "7 matching groups should be discovered: found " + m.groupCount();
			String singleOrAll = m.group(1);
			String puis = m.group(2);
			String[] puiList = puis.split(MultiRuleExpression.parameterDelimiter, 0);
			String pui = m.group(3);
			String relation = m.group(4);
			String value = m.group(5);
			String display = m.group(6);
			expressions.add(new MultiExpression(Enum.valueOf(MultiExpression.SetLogic.class, singleOrAll), puiList, pui, relation, value, display));
			
		}
		return new MultiExecutor(expressions);
	}
	
	
	@Override
	public String getLanguage(){
		return LANGUAGE_STRING;
	}
	
	/** A class for representing information contained in a rule based. 
	 * (possibly) on multiple parameter inputs, and evaluate the rule given inputs.
	 * @author dcberrio
	 *
	 */
	public static class MultiExpression {
		private final String display;
		private final double value;
		private String parameter = null;
		
		/** Operator enumeration.
		 * @author dcberrio
		 *
		 */
		public enum Operator {
			
			/** Pass through a parameter's value.
			 * @author dcberrio
			 *
			 */
			PASSTHROUGH {
				@Override
				public String evaluate (Map<String,Double> lhs, double rhs, String output,  
						SetLogic logic, String parameter) {
					if (lhs.get(parameter) == null) return null;
					return lhs.get(parameter).toString();
				}

				@Override
				public boolean matches(String operator) {
					// TODO Auto-generated method stub
					return "".equals(operator);
				}
			},
			
			/** Equality.
			 * @author dcberrio
			 *
			 */
			EQUALS {
				@Override
				public boolean matches(String operator) {
					return "=".equals(operator);
				}
				
				@Override
				public String evaluate (Map<String,Double> lhs, double rhs, String output,  
						SetLogic logic, String parameter) {
					switch (logic) {
					case SINGLE_PARAMETER: 
						if (lhs.get(parameter) == null) return null;
						return lhs.get(parameter) == rhs ? output : null;
					case MORE_THAN_ONE_PARAMETER:
						boolean oneMatch = false;
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) != null) {
								if (lhs.get(feedId).doubleValue() == rhs) {
									if (oneMatch) {
										return output;
									} else {
										oneMatch = true;
									}
								}
							}
						}
						return null;
					case ALL_PARAMETERS: 
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) == null || lhs.get(feedId).doubleValue() != rhs) {
								return null;
							}
						}
						return output;
					default: return null;	
					}
				}
			},
			/** Less than.
			 * @author dcberrio
			 *
			 */
			LESS_THAN {
				@Override
				public boolean matches(String operator) {
					return "<".equals(operator);
				}
				
				@Override
				public String evaluate (Map<String,Double> lhs, double rhs, String output,  
						SetLogic logic, String parameter) {
					switch (logic) {
					case SINGLE_PARAMETER: 
						if (lhs.get(parameter) == null) return null;
						return lhs.get(parameter) < rhs ? output : null;
					case MORE_THAN_ONE_PARAMETER:
						boolean oneMatch = false;
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) != null) {
								if (lhs.get(feedId).doubleValue() < rhs) {
									if (oneMatch) {
										return output;
									} else {
										oneMatch = true;
									}
								}
							}
						}
						return null;
					case ALL_PARAMETERS: 
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) == null || lhs.get(feedId).doubleValue() >= rhs) {
								return null;
							}
						}
						return output;
					default: return null;	
					}
				}
			},
			
			/** Greater than.
			 * @author dcberrio
			 *
			 */
			GREATER_THAN {
				@Override
				public boolean matches(String operator) {
					return ">".equals(operator);
				}
				
				@Override
				public String evaluate (Map<String,Double> lhs, double rhs, String output,  
						SetLogic logic, String parameter) {
					switch (logic) {
					case SINGLE_PARAMETER: 
						if (lhs.get(parameter) == null) return null;
						return lhs.get(parameter) > rhs ? output : null;
					case MORE_THAN_ONE_PARAMETER:
						boolean oneMatch = false;
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) != null) {
								if (lhs.get(feedId).doubleValue() > rhs) {
									if (oneMatch) {
										return output;
									} else {
										oneMatch = true;
									}
								}
							}
						}
						return null;
					case ALL_PARAMETERS: 
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) == null || lhs.get(feedId).doubleValue() <= rhs) {
								return null;
							}
						}
						return output;
					default: return null;	
					}
				}
			},
			/** Not equal to.
			 * @author dcberrio
			 *
			 */
			NOT_EQUALS {
				@Override 
				public boolean matches(String operator) {
					return MultiEvaluator.NOT_EQUALS.equals(operator);
				}
				
				@Override
				public String evaluate (Map<String,Double> lhs, double rhs, String output,  
						SetLogic logic, String parameter) {
					switch (logic) {
					case SINGLE_PARAMETER: 
						if (lhs.get(parameter) == null) return null;
						return lhs.get(parameter) != rhs ? output : null;
					case MORE_THAN_ONE_PARAMETER:
						boolean oneMatch = false;
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) != null) {
								if (lhs.get(feedId).doubleValue() != rhs) {
									if (oneMatch) {
										return output;
									} else {
										oneMatch = true;
									}
								}
							}
						}
						return null;
					case ALL_PARAMETERS: 
						for (String feedId : lhs.keySet()) {
							if (lhs.get(feedId) == null || lhs.get(feedId).doubleValue() == rhs) {
								return null;
							}
						}
						return output;
					default: return null;	
					}	
				}
			};
			
			/** Match an operator to an input string.
			 * @param operator the operator string.
			 * @return boolean indicator of a match.
			 */
			public abstract boolean matches(String operator);
			
			/** Evaluate a rule based on multiple parameter inputs. 
			 * @param lhs a map of parameter values 
			 * @param rhs a value to compare one or more of the values to
			 * @param output the display value to return if the rule is true
			 * @param logic the logic used to combine multiple input values
			 * @param parameter the id of the single parameter to use in evaluating the rule
			 * when that logic is selected
			 * @return a string value that is either output value or null
			 */
			public abstract String evaluate(Map<String,Double> lhs, double rhs, String output,
					SetLogic logic, String parameter);
		}
		private final Operator operator;
		private final SetLogic logic;
		
		/** Enumeration for multi set logic.
		 * @author dcberrio
		 *
		 */
		public enum SetLogic {
			/** Single parameter.
			 * @author dcberrio
			 *
			 */
			SINGLE_PARAMETER {
				
				/** Test whether enum matches an input string.
				 * @param setLogic the set logic to match
				 * @return boolean indicating a match
				 */
				public boolean matches(String setLogic) {
					return "SINGLE_PARAMETER".equals(setLogic);
				}
				
			},
			
			/** More than 1 parameter.
			 * @author dcberrio
			 *
			 */
			MORE_THAN_ONE_PARAMETER {
				
				/** Test whether enum matches an input string.
				 * @param setLogic the set logic to match
				 * @return boolean indicating a match
				 */
				public boolean matches(String setLogic) {
					return "MORE_THAN_ONE_PARAMETER".equals(setLogic);
				}
			},
			
			/** All parameters.
			 * @author dcberrio
			 *
			 */
			ALL_PARAMETERS {
				
				/** Test whether enum matches an input string.
				 * @param setLogic the set logic to match
				 * @return boolean indicating a match
				 */
				public boolean matches(String setLogic) {
					return "ALL_PARAMETERS".equals(setLogic);
				}
			}
			
		}
		
		/**
		 * Multi expression contructor.
		 * @param setLogic logic to combine parameters
		 * @param puiList list of parameters for this rule
		 * @param pui single pui used by this rule
		 * @param op the operation performed.
		 * @param value the value.
		 * @param display the display name.
		 */
		public MultiExpression (SetLogic setLogic, String[] puiList, String pui, String op, String value, 
				String display) {
			this.value = Double.parseDouble(value);
			this.display = display;
			this.logic = setLogic;
			this.parameter = pui;
			
			Operator localOperator = null;
			for (Operator o:Operator.values()) {
				if (o.matches(op)) {
					localOperator = o;
					break;
				}
			}
			operator = localOperator;
			assert operator != null : "operator must not be null" ;
		}
		
		/**
		 * Executes the rule over a set of provided values.
		 * @param values A list of the required values.
		 * @return the execute string.
		 */
		public String execute(Map<String,Double> values) {
			if (values != null) {
				return operator.evaluate(values, value, display, logic, parameter);
			}
			return null;
		}
	}
	
	private static class MultiExecutor implements Executor {
		private final List <MultiExpression> expressions;
		
		public MultiExecutor(List<MultiExpression> expressionList) {
			expressions = expressionList;
		}
		
		/** Create a map of feedID and values
		 * @param providers
		 * @param data
		 * @return
		 */
		private Map<String,Double> getValuesFromFeed(List<FeedProvider> providers, Map<String, List<Map<String, String>>> data) {
			FeedProvider.RenderingInfo result = null;
			Map<String,Double> valuesMap = new ConcurrentHashMap<String,Double>();
			for (FeedProvider fp : providers) {
				String feedId = fp.getSubscriptionId();
				List<Map<String,String>> values = data.get(feedId);
				if (values != null && !values.isEmpty()) {
					result = fp.getRenderingInfo(values.get(values.size() - 1));
					if (result.getValueText() != null) {
						valuesMap.put(feedId,Double.valueOf(result.getValueText()));;
					}
				}
			}
			return valuesMap;
		}
		
		public FeedProvider.RenderingInfo getSingleFeedValue(List<FeedProvider> providers, 
				Map<String, List<Map<String, String>>> data, String paramId) {
			for (FeedProvider fp : providers) {
				if (fp.getSubscriptionId().equals(paramId)) {
					List<Map<String,String>> values = data.get(paramId);
					if (values != null) {
						return fp.getRenderingInfo(values.get(values.size() - 1));
					}
				}
			}
			return null;
		}
		
		/**
		 * Requires multiple inputs.
		 * @return false.
		 */
		public boolean requiresMultipleInputs() {
			return true;
		}
		
		/**
		 * Gets the feed provider from the set of specified feed providers.
		 * @param subId subscription id)
		 * @param providers list of providers to find the feed provider from
		 * @return an instance of FeedProvider that is for the given subscription Id 
		 */
		private FeedProvider getFeedProvider(String subId,List<FeedProvider> providers) {
			for (FeedProvider provider:providers) {
				if (subId.equals(provider.getSubscriptionId())) {
					return provider;
				}
			}
			return null;
		}
		
		private int getPriority(Map<String, List<Map<String, String>>> data, FeedProvider fp) {
			List<Map<String,String>> maps = data.get(fp.getSubscriptionId());
			if (maps != null && !maps.isEmpty()) {
				String priorityString = maps.get(maps.size()-1).get(FeedProvider.NORMALIZED_TELEMETRY_STATUS_CLASS_KEY);
				assert priorityString != null : "priority string should not be null";
				try {
					return Integer.parseInt(priorityString);
				} catch (NumberFormatException nfe) {
					// ignore the exception 
				}
			}
			
			return Integer.MAX_VALUE;
		}
		
		private FeedProvider.RenderingInfo getCombinedRenderingInfo(Map<String,Double> feedValues, 
				List<FeedProvider> feedProviders, Map<String, List<Map<String, String>>> data){
			FeedProvider.RenderingInfo highestPriorityInvalid = null,
					   highestPriorityAll = null;
			int highestPriorityInvalidClass = Integer.MAX_VALUE, 
					highestPriorityAllClass = Integer.MAX_VALUE;
			String lastOnValue = null;
			for (String feedId : feedValues.keySet()) {
				FeedProvider fp = getFeedProvider(feedId, feedProviders);
				FeedProvider.RenderingInfo feedValue = null;
				if (fp != null) {
					feedValue = getSingleFeedValue(feedProviders, data, feedId);
				}
				int currentPriority = getPriority(data,fp);
				
				if (feedValue != null) {
					if (highestPriorityAllClass > currentPriority) {
						highestPriorityAll = feedValue;
						highestPriorityAllClass = currentPriority;
					}
					
					if (!feedValue.isValid() && highestPriorityInvalidClass > currentPriority) {
						highestPriorityInvalid = feedValue;
						highestPriorityInvalidClass = currentPriority;
					}
				}
			}
			FeedProvider.RenderingInfo returnValue;
			if (highestPriorityInvalid != null) {
				returnValue = new FeedProvider.RenderingInfo(lastOnValue, highestPriorityInvalid.getValueColor(), 
						highestPriorityInvalid.getStatusText(), highestPriorityInvalid.getStatusColor(), highestPriorityInvalid.isValid());	
			} else if (highestPriorityAll != null) {
				returnValue = new FeedProvider.RenderingInfo(lastOnValue, highestPriorityAll.getValueColor(), 
						highestPriorityAll.getStatusText(), highestPriorityAll.getStatusColor(), highestPriorityAll.isValid());
			} else {
				returnValue = new FeedProvider.RenderingInfo("", Color.black, 
						"", Color.black, true);
			}
			
			return returnValue;
			
		}
		
		@Override
		public FeedProvider.RenderingInfo evaluate(Map<String, List<Map<String, String>>> data, List<FeedProvider> feedProviders) {
			Map<String,Double> feedValues = getValuesFromFeed(feedProviders,data);
			
				try {
					for (MultiExpression expression : expressions) {
						String expressionValue = expression.execute(feedValues);
						if (expressionValue != null) {
							
							FeedProvider.RenderingInfo ri = getCombinedRenderingInfo(feedValues, feedProviders, data);
							ri.setValueText(expressionValue);
							return ri;
						}
					}
				} catch (NumberFormatException nfe){
						
				}		
			return null;
		}
	}
}

