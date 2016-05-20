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
package gov.nasa.arc.mct.evaluator.expressions;

/**
 * Multiple-input Expressions controller.
 */
public class MultiExpressionsController {

	private MultiViewManifestation multiViewManifestation;
	
	/** Create a controller for rule expressions.
	 * @param manifestation the manifestation.
	 */
	public MultiExpressionsController(MultiViewManifestation manifestation) {
		this.multiViewManifestation = manifestation;
	}
	
	/**
	 * Notifies that a new expression has been created.
	 * @param newExp the new expression to add.
	 * @param expList the expression list to add.
	 */
	public void notifyExpressionAdded(MultiRuleExpression newExp, MultiRuleExpressionList expList){
		if (expList != null) {
			expList.addExp(newExp);
			multiViewManifestation.refreshRuleExpressions();
			multiViewManifestation.setSelectedExpression(newExp);
			
		}
	}
	
	/**
	 * Notifies the an existing expression has been deleted.
	 * @param selectedExp the selected expression to delete.
	 * @param expList the expression list to delete.
	 */
	public void notifyExpressionDeleted(MultiRuleExpression selectedExp, MultiRuleExpressionList expList){
		if (selectedExp != null && expList != null){
			expList.deleteExp(selectedExp);
			multiViewManifestation.refreshRuleExpressions();
		}
	}
	
	/**
	 * Notifies that an existing expression has been added on top.
	 * @param newExp the new expression to add above.
	 * @param selectedExp the selected expression to add from.
	 * @param expList the expression list to add to.
	 */
	public void notifyExpressionAddedAbove(MultiRuleExpression newExp, MultiRuleExpression selectedExp, MultiRuleExpressionList expList){
		if (newExp != null && selectedExp != null && expList != null) {
			int index = expList.indexOf(selectedExp);
			expList.addExp(index, newExp);
		}
	}
	
	/**
	 * Notifies than an existing expression has been added below.
	 * @param newExp the new expression to add below.
	 * @param selectedExp the selected expression to add to.
	 * @param expList the expression list to add to.
	 */
	public void notifyExpressionAddedBelow(MultiRuleExpression newExp, MultiRuleExpression selectedExp, MultiRuleExpressionList expList){
		if (newExp != null && selectedExp != null && expList != null) {	
			int index = expList.indexOf(selectedExp);
			expList.addExp(index+1, newExp);
		}
	}
	
	/**
	 * Notifies to move expression up one.
	 * @param exp the expression to move up one.
	 * @param expList the expression list.
	 */
	public void notifyMovedUpOne(MultiRuleExpression exp, MultiRuleExpressionList expList){
		if (exp != null && expList != null){
			int index = expList.indexOf(exp);
			if (index > 0) {
				expList.move(index-1, exp);
				multiViewManifestation.refreshRuleExpressions();
			}
		}
	}
	
	/**
	 * Notifies to move expression down one.
	 * @param exp the expression to move down one.
	 * @param expList the expression list. 
	 */
	public void notifyMovedDownOne(MultiRuleExpression exp, MultiRuleExpressionList expList){
		if (exp != null && expList != null){
			int index = expList.indexOf(exp);
			if (index < expList.size()-1) {
				expList.move(index+1, exp);
				multiViewManifestation.refreshRuleExpressions();
			}
		}
		
	}
	
	/**
	 * Notifies to update a multi rule expression . 
	 * @param index the index of the rule expression to update.
	 * @param expList the list of expressions.
	 * @param exp the new rule expression
	 */
	public void notifyRuleUpdated(int index, MultiRuleExpressionList expList, 
			MultiRuleExpression exp){
		expList.updateExp(index, exp);
		multiViewManifestation.refreshRuleExpressions();
		multiViewManifestation.setSelectedExpression(exp);
	}
}

