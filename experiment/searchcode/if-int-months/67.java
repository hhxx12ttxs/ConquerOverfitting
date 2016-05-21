     */
    public abstract int getMinimumValue(long instant);
     * int v = ...
     * int age = getDifference(add(instant, v), instant);
     * <\/pre>
     * overflowing into larger fields if necessary.
     * 
     */
     * overflowing into larger fields if necessary.
     * <p>
    public abstract int getDifference(long minuendInstant, long subtrahendInstant);

