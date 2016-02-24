package cn.edu.pku.sei.plde.conqueroverfitting.slice;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.jdt.JDTParse;

/**
 * Created by jiewang on 2016/2/23.
 */
public class StaticSlice {

	private String statements;
	private String expression;
	private String sliceStatements;
	
	public StaticSlice(String statements, String expression){
		this.statements = statements;
		this.expression = expression;
		
		slice();
	}
	
	private void slice(){
		sliceStatements = "";
		List<String> identifierInExpression = new JDTParse(expression, ASTParser.K_EXPRESSION).getIdentifierList();
		String[] statementsArray = statements.split("\n");
		for(String statement : statementsArray){
			List<String> identifierInStatement = new JDTParse(statement, ASTParser.K_STATEMENTS).getIdentifierList();
			identifierInStatement.retainAll(identifierInExpression);
			if(identifierInStatement.size() > 0){
				sliceStatements += statement;
				sliceStatements += "\n";
			}
		}
		
	}
	
	public String getSliceStatements(){
		return sliceStatements.toString();
	}
}
