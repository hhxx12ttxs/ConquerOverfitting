package intermediate.control;

import java.util.ArrayList;

import exceptions.MismatchException;
import exceptions.MissingResourceException;

import generator.NodeVisitor;
import intermediate.BaseNode;
import intermediate.expression.ExprNode;

public class If extends ControlNode{

	private ArrayList<BaseNode> elselist;
	
	public If(ExprNode n, ArrayList<BaseNode> b, ArrayList<BaseNode> elselist) {
		super(n, b);
		this.elselist = elselist;
	}

	public ArrayList<BaseNode> elseList(){
		return elselist;
	}
	
	public void accept(NodeVisitor visitor) throws MissingResourceException, MismatchException{
		visitor.visit(this);
	}
	
}

