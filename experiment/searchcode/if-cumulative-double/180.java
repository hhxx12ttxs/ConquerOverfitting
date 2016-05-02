package jsl.modeling.elements.variable.nhpp;

public interface RateSegmentIfc {

    /** Returns a new instance of the rate segment
     * 
     * @return
     */
    RateSegmentIfc newInstance();
    
    /** Returns true if the supplied time is within this
     *  rate segments time interval
     * 
     * @param time
     * @return
     */
    boolean contains(double time);

    /** Returns the rate for the interval
     * 
     * @param time 
     * @return
     */
    double getRate(double time);

    /** The rate at the time that the time interval begins
     * 
     * @return
     */
    double getRateAtLowerTimeLimit();

    /** The rate at the time that the time interval ends
     * 
     * @return
     */
    double getRateAtUpperTimeLimit();

    /** The lower time limit
     * 
     * @return
     */
    double getLowerTimeLimit();

    /** The upper time limit
     * 
     * @return
     */
    double getUpperTimeLimit();

    /** The width of the interval
     * 
     * @return
     */
    double getTimeWidth();

    /** The lower limit on the cumulative rate axis
     * 
     * @return
     */
    double getCumulativeRateLowerLimit();

    /** The upper limit on the cumulative rate axis
     * 
     * @return
     */
    double getCumulativeRateUpperLimit();

    /** The cumulative rate interval width
     * 
     * @return
     */
    double getCumulativeRateIntervalWidth();

    /** Returns the value of the cumulative rate function for the interval
     * given a value of time within that interval 
     * 
     * @param time
     * @return
     */
    double getCumulativeRate(double time);

    /** Returns the inverse of the cumulative rate function given the interval
     *  and a cumulative rate value within that interval.  Returns a time
     * 
     * @param cumRate
     * @return
     */
    double getInverseCumulativeRate(double cumRate);
}
