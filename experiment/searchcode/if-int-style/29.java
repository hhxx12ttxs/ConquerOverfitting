private static int mCounter = 0;

private KmlBaloonStyle mBaloonStyle = null;
private KmlPolyStyle mPolyStyle = null;
public void setSubStyle(KmlSubStyle subStyle){

if(subStyle instanceof KmlBaloonStyle){
mBaloonStyle = (KmlBaloonStyle) subStyle;

