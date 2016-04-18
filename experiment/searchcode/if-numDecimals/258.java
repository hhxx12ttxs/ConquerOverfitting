/**
 * 
 */
package ArgumentParser;

/**
 * Parser will take an array of strings containing the values of the 
 * flags utilized in a command line environment.
 * 
 * @author Pablo Gallastegui and Dan Dodson
 * @version 2
 *
 */
public class ProgramArgs {

    // Error messages
    public static final String EXCEPTION_EMPTY_PARAMETER_ARG = "Parameter %s is empty.";
    public static final String EXCEPTION_NON_INTEGER_PARAMETER = "Parameter %s is not an integer.";
    public static final String EXCEPTION_OUT_OF_RANGE = "Parameter %s is out of range.";
    public static final String EXCEPTION_INVALID_OPTION = "%s is not a valid option.";
    public static final String EXCEPTION_EXTRA_ARGUMENT = "'%s' is not a valid argument.";

    // Command line options
	private int bufferLength = 1;
    // a number of decimal places; default should be the number storable in a float
    // on a 64 bit machine.
    private int storageNumericPrecision = maxFloatDecimals();
    // percentage of grid cells to persist in the database
    private int storageGeographicPrecision = 100;
    // percentage of temperature frames in time to persist
    private int storageTimePrecision = 100;

	/**
	 * @return the buffer length
	 */
	public int getBufferLength() {
		return bufferLength;
	}

    public int getStorageNumericPrecision() {
        return storageNumericPrecision;
    }

    public int getStorageGeographicPrecision() {
        return storageGeographicPrecision;
    }

    public int getStorageTimePrecision() {
        return storageTimePrecision;
    }

	/**
	 * <CTOR>
	 */
	public ProgramArgs() {
	}

    /**
     * From an option and the argument following it, return the value of the option if given.
     * Works only for a short option (one dash followed by one letter).
     * @param opt       the known option flag
     * @param next      the unknown argument following the known option flag
     * @param min       the minimum integer value of the option argument
     * @param max       the maximum integer value of the option argument
     * @throws Exception
     */
    private int getOpt(String opt, String next, int min, int max) throws Exception {
        // Check to see if the user put a space in between the named parameter and
        // the value for it or not. Accept it either way.
        String optArg;
        int intOptArg;

        if (opt.length() == 2) {
            if (next == null) {
                throw new Exception(String.format(ProgramArgs.EXCEPTION_EMPTY_PARAMETER_ARG, opt));
            } else {
                optArg = next;
            }
        } else {
            optArg = opt.substring(2);
        }

        try {
            intOptArg = Integer.parseInt(optArg);
        } catch (NumberFormatException e) {
            throw new Exception(String.format(ProgramArgs.EXCEPTION_NON_INTEGER_PARAMETER, optArg));
        }
        if (intOptArg < min || intOptArg > max) {
            System.err.println(opt + " must be between " + min + " and " + max + ".");
            throw new Exception(String.format(ProgramArgs.EXCEPTION_OUT_OF_RANGE, optArg));
        }

        return intOptArg;
    }

    /**
     * @return The maximum number of decimals storable in a double (minus 2)
     */
    private int maxDoubleDecimals() {
        String strDouble = String.valueOf(Double.MAX_VALUE);
        // Assume the string representation of a number will be in scientific notation
        // with a Capital E representing the power of 10, and nothing but digits after
        // the E.
        int numDecimals = Integer.valueOf(strDouble.substring(strDouble.indexOf("E") + 1));
        // Assume temperatures will use three digits before the decimal place -
        // Kelvin temperatures of the planet will be above 100 and below 1000.
        // This is not a great assumption, but shouldn't cause problems as long as the
        // storage medium doesn't depend on its accuracy.
        return numDecimals - 2;
    }

    /**
     * @return The maximum number of decimals storable in a double (minus 2)
     */
    private int maxFloatDecimals() {
        String floatStr = String.valueOf(Float.MAX_VALUE);
        // Assume the string representation of a number will be in scientific notation
        // with a Capital E representing the power of 10, and nothing but digits after
        // the E.
        int numDecimals = Integer.valueOf(floatStr.substring(floatStr.indexOf("E") + 1));
        // Assume temperatures will use three digits before the decimal place -
        // Kelvin temperatures of the planet will be above 100 and below 1000.
        // This is not a great assumption, but shouldn't cause problems as long as the
        // storage medium doesn't depend on its accuracy.
        return numDecimals - 2;
    }


	/**
	 * It will parse the strings array provided and will populate the private variables.
     * If no value is provided for the options, default values will be used.
	 *
	 * @param 	args		Array containing the argument strings
	 * @throws	Exception	when invalid parameters are provided
	 */
	public void parse(String[] args) throws Exception {

        String opt;
        String next;
        for (int i=0; i < args.length; i++) {
            opt = args[i];
            if (i == args.length - 1) {
                next = null;
            } else {
                next = args[i + 1];
            }

            if (opt.startsWith("-b")) {
                bufferLength = getOpt(opt, next, 1, Integer.MAX_VALUE);
            } else if (opt.startsWith("-p")) {
                storageNumericPrecision = getOpt(opt, next, 0, maxDoubleDecimals());
            } else if (opt.startsWith("-g")) {
                storageGeographicPrecision = getOpt(opt, next, 0, 100);
            } else if (opt.startsWith("-t")) {
                storageTimePrecision = getOpt(opt, next, 0, 100);
            } else if (opt.startsWith("-")) {
                throw new Exception(String.format(ProgramArgs.EXCEPTION_INVALID_OPTION, opt));
            } else if (next != null) {
                if (!opt.startsWith("-") && !next.startsWith("-")) {
                    throw new Exception(String.format(ProgramArgs.EXCEPTION_EXTRA_ARGUMENT, opt));
                }
            } else if (args.length == 1 && !opt.startsWith("-")) {
                // there's a single invalid argument
                throw new Exception(String.format(ProgramArgs.EXCEPTION_EXTRA_ARGUMENT, opt));
            }
        }

    }

}

