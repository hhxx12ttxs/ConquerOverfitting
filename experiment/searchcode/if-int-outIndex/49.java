public static String  encode ( byte [ ]  octetString )
//////////////////////////////////////////////////////////////////////
{
int  bits24;
int  bits6;
= new char [ ( ( octetString.length - 1 ) / 3 + 1 ) * 4 ];

int outIndex = 0;
int i        = 0;

while ( ( i + 3 ) <= octetString.length )

