package uk.ac.lkl.common.util.restlet.server;

/**
 * The id of an entity.
 * 
 * Currently all indices within the restlet framework are integers. Rather than
 * use the type <code>int</code> or <code>Integer</code> in the signatures of
 * <code>EntityHandler</code>, instances of this class are used instead. This
 * essentially allows parameterised typing of an integer, e.g.
 * <code>EntityId&lt;User&gt;</code>.
 * 
 * @author $Author: darren.pearce $
 * @version $Revision: 4572 $
 * @version $Date: 2010-03-08 23:36:24 +0100 (Mon, 08 Mar 2010) $
 * 
 */
public class EntityId<O> {

    /**
     * The underlying integer value of this instance.
     * 
     */
    private int value;

    /**
     * Create a new instance using the given integer value.
     * 
     * @param value
     *            the integer value of this instance.
     * 
     */
    public EntityId(int value) {
	this.value = value;
    }

    /**
     * Get the integer value of this instance.
     * 
     * @return the integer value
     * 
     */
    public int getValue() {
	return value;
    }

    /**
     * Return a string representation of this instance.
     * 
     * @return the id as a string
     * 
     */
    public String toString() {
	return Integer.toString(value);
    }

    @Override
    public boolean equals(Object object) {
	if (!(object instanceof EntityId))
	    return false;
	EntityId<?> other = (EntityId<?>) object;
	return this.value == other.value;
    }

    @Override
    public int hashCode() {
	return value;
    }
}

