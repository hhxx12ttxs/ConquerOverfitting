return valor;
}

public static float[] rgb2hsl( int[] rgb, float[] result ) {
if( result == null )
result = new float[ rgb.length ];
result[0] = result[1] = result[2] = 0;
result[2] = (max + min) / 2;
float d = max - min;

if (d == 0)

