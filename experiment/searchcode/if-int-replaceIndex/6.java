public static final String textReplace(final String originalText, final String...replaceTexts)
{
if( BasicUtils.isNullOrEmpty(originalText) || null == replaceTexts )
return StringConstants.EMPTY_STRING;
}

StringBuffer sb = new StringBuffer(originalText);

int replaceIndex = -1;

while( (replaceIndex = sb.lastIndexOf(StringConstants.SYMBOL_PERCENT)) >= 0 )

