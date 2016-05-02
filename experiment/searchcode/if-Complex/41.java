<<<<<<< HEAD
package org.simplesql.parser.tree;

import java.util.ArrayList;
import java.util.List;

import org.simplesql.parser.tree.TERM.TYPE;

public class MULT {

	enum OP {
		PRODUCT("*"), DIVIDE("/"), MOD("mod");

		final String val;

		OP(String val) {
			this.val = val;
		}

		public String toString() {
			return val;
		}

	}

	TYPE type = TYPE.INTEGER;

	List<Object> children = new ArrayList<Object>();

	/**
	 * An expression is considered complex if it contains anything other than a
	 * simple constant or variable.
	 */
	boolean complex = false;

	/**
	 * Only used if complex == false. This means that only one UNARY exist.
	 */
	UNARY.TYPE unaryType = UNARY.TYPE.MIXED;
	Object unaryValue;

	String assignedName;

	public void unary(UNARY unary) {

		// get the highest order type
		type = unary.term.type.max(type);

		children.add(unary);

		// set to complex if the unary is a functions, expression or other than
		// a CONSTANT or Variable.
		unaryType = unary.getType();
		unaryValue = unary.term.getValue();

		if (!complex) {
			complex = unaryType == UNARY.TYPE.MIXED || children.size() > 1;

			if (unaryType.equals(UNARY.TYPE.VARIABLE))
				assignedName = unary.term.getAssignedName();

		}

	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public void mult() {
		children.add(OP.PRODUCT);
	}

	public void divide() {
		children.add(OP.DIVIDE);
	}

	public void mod() {
		children.add(OP.MOD);
	}

	public boolean isComplex() {
		return complex;
	}

	public void visit(Visitor visitor) {

		for (Object child : children) {
			if (child instanceof UNARY)
				visitor.unary((UNARY) child);
			else {
				OP op = (OP) child;
				if (op.equals(OP.PRODUCT))
					visitor.mult();
				else if (op.equals(OP.DIVIDE))
					visitor.divide();
				else
					visitor.mod();
			}
		}
	}

	public static interface Visitor {

		void unary(UNARY unary);

		void mult();

		void divide();

		void mod();

	}

	public String getAssignedName() {
		return assignedName;
	}

	public void setAssignedName(String assignedName) {
		this.assignedName = assignedName;
	}

}
=======
package com.ntobler.space;

public class Complex {

	public static final Complex ZERO = new Complex(0, 0);
	
	public final double x;
	public final double y;
	
	public Complex (double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Complex (Complex c) {
		this.x = c.x;
		this.y = c.y;
	}
	
	public static Complex normalFromAngle (double angle) {
		return new Complex(Math.sin(angle), Math.cos(angle));
	}
	
	public Complex plus(final Complex c) {
		return new Complex(x + c.x, y + c.y);
	}
	
	public Complex minus(final Complex c) {
		
		return new Complex(x - c.x, y - c.y);
	}
	
	public Complex scalarMultiply(double a) {
		
		return new Complex(x * a, y * a);
	}
	
	public Complex scalarDivide(double a) {
		
		return new Complex(x / a, y / a);
	}
	
	public Complex normalVector() {
		double abs = this.abs();
		if (abs == 0) {
			return new Complex(0,0);
		}
		else {
			return this.scalarDivide(abs);
		}
	}
	
	public double getAngle() {
		return Math.atan2(this.x, this.y);
	}
	
	public Complex addAngle(double angle) {
		
		Complex norm = normalFromAngle(angle + getAngle());
		
		return norm.scalarMultiply(this.abs()) ;
	}
	
	public Complex add90deg() {
		
		return new Complex(this.y, 0 - this.x);
	}
	
	public Complex sub90deg() {
		
		return new Complex(0 - this.y, this.x);
	}
	
	
	
	public double abs() {
		return  Math.sqrt((x*x) + (y*y));
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

