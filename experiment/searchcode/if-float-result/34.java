return Float.compare( vec3.x, x ) == 0 &amp;&amp; Float.compare( vec3.y, y ) == 0 &amp;&amp; Float.compare( vec3.z, z ) == 0;

}

@Override
public int hashCode() {
int result = (x != +0.0f ? Float.floatToIntBits( x ) : 0);
result = 31 * result + (y != +0.0f ? Float.floatToIntBits( y ) : 0);
result = 31 * result + (z != +0.0f ? Float.floatToIntBits( z ) : 0);

