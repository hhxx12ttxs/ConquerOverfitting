private final float getJavaVersionAsFloat() {
if (JAVA_VERSION == null) {
return 0f;
}

String str = JAVA_VERSION.substring(0, 3);
public final boolean isJavaVersionAtLeast(float requiredVersion) {
return getVersionFloat() >= requiredVersion;

