private final float mDoubleFactor;
public AcclerateInerpolator(){
mFactor = 1.0f;
mDoubleFactor =2*mFactor;
mFactor =factor;
mDoubleFactor = 2* mFactor;
}
@Override
public float getInterpolation(float v) {
if(mFactor == 1.0f){

