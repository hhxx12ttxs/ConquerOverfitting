return true;
}
}
}

public static boolean isFileNameValid( String filename )
{
String invalidChars;
if ( OS.isWindows() )
{
invalidChars = &quot;\\/:*?\&quot;<>|&quot;;

