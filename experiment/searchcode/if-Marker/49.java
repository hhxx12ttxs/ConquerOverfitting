* Created by ChunyueDu on 7/20/15.
*/
public class BuildMarker extends ProxyMarker implements DeleteMarkerInterface
public static BuildMarker get() {
if (mBuildMarker == null) {
mBuildMarker = new BuildMarker();
}
return mBuildMarker;
}
}

