// skip comment lines
if ( lastChar == commentCharacter )
{
do
{
lastChar = reader.read();
} while ( !isEof( lastChar ) &amp;&amp; lastChar != CR &amp;&amp; lastChar != LF );
return nextLine();
}

// handle pragma lines
if ( lastChar == pragmaCharacter )

