System.out.println( &quot;Hn (n=&quot; + N + &quot;) = &quot; + calculateHarmonicNumber( N ) );
}

public static double calculateHarmonicNumber( double N ) {
double Hn = 1.0;
for( double base = 2.0; base <= N; base++ )
Hn += 1.0 / base;
return Hn;
}

}

