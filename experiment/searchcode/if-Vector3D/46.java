package point;

public class Vector3D extends AbstractVector{

public Vector3D(float x, float y, float z) {
return super.hashCode() + 17;
}

@Override
public boolean equals(Object o) {
if(! (o instanceof Vector3D))
return false;

