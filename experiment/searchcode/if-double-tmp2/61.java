private static Vector3 mTmp1;
private static Vector3 mTmp2;
public final Vector3 mNormal;
public double d = 0;

public enum PlaneSide {
Back, OnPlane,  Front
}

public Plane() {
mTmp1 = new Vector3();
mTmp2 = new Vector3();

