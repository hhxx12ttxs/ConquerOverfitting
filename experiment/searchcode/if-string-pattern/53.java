package org.gwt.formlayout.client.regexp;

public class Pattern extends PatternImpl {
	/**
     * Declares that regular expressions should be matched across line borders.
     */
    public final static int MULTILINE = PatternImpl.MULTILINE;

    /**
     * Declares that characters are matched reglardless of case.
     */
    public final static int CASE_INSENSITIVE = PatternImpl.CASE_INSENSITIVE;

    public static Pattern compile( String pattern )
    {
        return new Pattern( pattern );
    }

    public static Pattern compile( String pattern, int flags )
    {
        return new Pattern( pattern, flags );
    }

    public static boolean matches( String regex, String input )
    {
        return new Pattern( regex ).matches( input );
    }

    /**
     * Escape a provided string so that it will be interpreted as a literal in regular expressions. The current
     * implementation does escape each character even if not neccessary, generating verbose literals.
     *
     * @param input
     * @return
     */
    public static String quote( String input )
    {
        String output = "";
        for ( int i = 0; i < input.length(); i++ )
        {
            output += "\\" + input.charAt( i );
        }
        return output;
    }

    /**
     * Class constructor
     *
     * @param pattern Regular expression
     */
    public Pattern( String pattern )
    {
        this( pattern, 0 );
    }

    /**
     * Class constructor
     *
     * @param pattern Regular expression
     * @param flags
     */
    public Pattern( String pattern, int flags )
    {
        super( pattern, flags );
    }

    /**
     * Create a matcher for this pattern and a given input character sequence
     *
     * @param cs The input character sequence
     * @return A new matcher
     */
    public Matcher matcher( CharSequence cs )
    {
        return new Matcher( this, cs );
    }
}

