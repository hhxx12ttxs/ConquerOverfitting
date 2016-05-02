package com.intelerad.web.lib.gwt.model;

import java.util.Set;

/**
 * 
 * Utility class to offload the validation logic into one place for use by both 
 * the GUI and the model
 *
 */
public class Validator
{
    private static final char[] invalidChars = new char[]{'\\'};
    
    public static boolean isValid( String input, boolean isRequired )
    {
        if ( isRequired && isBlank( input ) )
        {
            return false;
        }
        else
        {
            for ( char ch : invalidChars )
            {
                if ( input.indexOf( ch ) != -1 )
                {
                    return false;
                }
            }
            return true;
        }
    }
    
    public static boolean isValid( Set<String> set, boolean isRequired )
    {
        for( String e: set )
        {
            if ( e != null && !Validator.isValid( e, isRequired ) )
                return false;
        }
        return true;
    }
    
    private static boolean isBlank( String input )
    {
        return input.trim().length() == 0;
    }
}

