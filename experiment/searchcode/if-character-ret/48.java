package com.trumpetx.minerstatus.beans;

import com.trumpetx.minerstatus.util.Deserializer;
import com.trumpetx.minerstatus.util.ServiceFactory;

public abstract class StatusMetadataBase
    implements StatusMetadata
{
    ServiceFactory _factory;

    @Override
    public void setFactory( ServiceFactory factory )
    {
        _factory = factory;
    }

    @Override
    public ServiceFactory getFactory()
    {
        if ( _factory != null )
            return _factory;
        else
            return ServiceFactory.getDefaultInstance();
    }

    protected String getCommonDirections( String poolName, String youCanGetYourAPIKey )
    {
        // @formatter:off
        return poolName + " provides an API key which you can use to access your data semi-privately (security through obscurity).  "
            + "You can get your API key " + youCanGetYourAPIKey;
        // @formatter:on
    }

    public String getAPIKeyLabel()
    {
        return "API Key";
    }

    public String cleanAPIKey( String apiKey )
    {
        StringBuilder ret = null;

        for ( int i = 0; i < apiKey.length(); i++ )
        {
            char ch = apiKey.charAt( i );

            if ( Character.isWhitespace( ch ) )
            {
                // If we've never seen a whitespace character before this one, then we know that all
                // the chars prior to this are "keepers", so we can start the StringBuilder up with
                // that subsequence. If the StringBuilder already exists, then this is one of the
                // characters we *dont't* want in it.
                if ( ret == null )
                    ret = new StringBuilder( apiKey.subSequence( 0, i ) );
            }
            else
            {
                // This is not a whitespace character; if we're in StringBuilder mode, it's one we
                // want to keep. If the StringBuilder hasn't yet been created, then we're still in
                // the state where every character we've seen is valid. If we're lucky, we'll get
                // out of the loop without having created the StringBuilder, and then every character
                // is valid, and we can just return the original String. :-)
                if ( ret != null )
                    ret.append( ch );
            }
        }

        if ( ret == null )
            return apiKey;
        else
            return ret.toString();
    }

    protected final String DIGITS = "0123456789";

    protected final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    protected final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";

    protected final String LETTERS = UPPERCASE_LETTERS + LOWERCASE_LETTERS;

    protected boolean validateAPIKey( String apiKey, String validCharacters )
    {
        if ( apiKey.length() == 0 )
            return false;

        for ( int i = 0; i < apiKey.length(); i++ )
            if ( validCharacters.indexOf( apiKey.charAt( i ) ) < 0 )
                return false;

        return true;
    }

    public boolean validateAPIKey( String apiKey )
    {
        return validateAPIKey( apiKey, DIGITS + LETTERS );
    }

    protected abstract StatusDataAdapter getDataAdapterImpl();

    @Override
    public final StatusDataAdapter getDataAdapter()
    {
        StatusDataAdapter dataAdapter = getDataAdapterImpl();

        dataAdapter.setFactory( _factory );

        return dataAdapter;
    }

    protected abstract Deserializer<? extends Status> getDeserializerImpl();

    @Override
    public final Deserializer<? extends Status> getDeserializer()
    {
        Deserializer<? extends Status> deserializer = getDeserializerImpl();

        deserializer.setFactory( _factory );

        return deserializer;
    }
}

