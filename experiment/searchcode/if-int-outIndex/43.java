= new char [ ( ( octetString.length - 1 ) / 3 + 1 ) * 4 ];

int outIndex = 0;
int i        = 0;

while ( ( i + 3 ) <= octetString.length )
out [ outIndex++ ] = alphabet [ bits6 ];

// padding
out [ outIndex++ ] = &#39;=&#39;;
}
else if ( octetString.length - i == 1 )

