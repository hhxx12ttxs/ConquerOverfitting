package cz.muni.fi.pb162.calculator;

/**
 * @author jcechace
 */
public interface Result {

    /**
     * Method used to determine whether result is successful
     * @return true if this result contains value
     */
    boolean isSuccess();

    /**
     * Method used to determine whether result contains alphanumeric value
     * @return true if this result has alphanumeric value
     */
    boolean isAlphanumeric();

    /**
     * Method used to determine whether result contains numeric value
     * @return true if this result has numeric value
     */
    boolean isNumeric();

    /**
     * If result contains numeric value, this method should be used to retrieve it
     * @return value if successful and numeric,  Double.NaN otherwise;
     */
    double getNumericValue();

    /**
     * If result contains alphanumeric value or error message, this method should be used to retrieve it
     * @return value if successful and alphanumeric, error message if not successful, null otherwise;
     */
    String getAlphanumericValue();
}

