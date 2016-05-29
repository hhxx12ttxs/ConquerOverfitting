*   <code>m = m + (new value - m) / (number of observations)</code></li>
* </ol>
* If {@link #evaluate(double[])} is used to compute the mean of an array
* of stored values, a two-pass, corrected algorithm is used, starting with
* Statistical Association, Vol. 69, No. 348 (Dec., 1974), pp. 859-866.
*
* Returns <code>Double.NaN</code> if the dataset is empty.

