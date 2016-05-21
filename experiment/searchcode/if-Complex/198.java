     *
     * @return The supercomplexes, or an empty array if none.
     *
     */
    @UML(identifier=\"superComplex\", obligation=MANDATORY, specification=ISO_19107)
    Complex[] getSuperComplexes();
 */
@UML(identifier=\"GM_Complex\", specification=ISO_19107)
public interface Complex extends Geometry {
    /**
     * Returns {@code true} if and only if this {@code Complex} is maximal.
     * A complex is maximal if it is a subcomplex of no larger complex.
     *
     * @return {@code true} if this complex is maximal.
     */

