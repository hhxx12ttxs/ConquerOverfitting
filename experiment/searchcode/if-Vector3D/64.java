package ianet.octaneraytrace.Flog;

public class Vector3D {
public double x;
public double y;
return new Vector3D(w.x + v.x, w.y + v.y, w.z + v.z);
}

public static Vector3D subtract(Vector3D v, Vector3D w) {
if (w == null || v == null) throw new RuntimeException(&quot;Vectors must be defined [&quot; + v + &quot;,&quot; + w + &quot;]&quot;);

