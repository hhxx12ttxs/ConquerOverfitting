/**
 * 
 */
package uk.ac.ucl.cs.pddlgen.ebnf;

/**
 * @author rax
 *
 */
public class PrimitiveType extends Streamable {

	Name name;

	private PrimitiveType(Name name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ucl.cs.pddlgen.ebnf.Streamable#printInternal()
	 */
	@Override
	protected void printInternal() {
		writeInto(pw, name);
	}

	public static PrimitiveType create(String name) {
		return create(Name.create(name));
	}
	
	public static PrimitiveType create(Name name) {
		if (name == null) {
			throw new IllegalArgumentException("Statement <primitive-type> must have a <name>.");
		}
		return new PrimitiveType(name);
	}

}

