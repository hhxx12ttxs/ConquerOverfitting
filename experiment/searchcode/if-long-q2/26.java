q3 = s * (m.m31 + m.m13);
q0 = s * (m.m23 - m.m32);
} else if (m.m22 > m.m33) {
q2 = Math.sqrt(1.0 + m.m22 - m.m11 - m.m33) * 0.5;
return &quot;[&quot; + q0 + &quot; , &quot; + q1 + &quot; , &quot; + q2 + &quot; , &quot; + q3 + &quot;]&quot;;
}

@Override
public boolean equals(Object obj) {
if (obj instanceof Quaternion) {

