final String ext = getInputFileExtension();
if(ext != null){
final FileInputStream fileInputStream = new FileInputStream(getTestDataPath() + getTestName() + ext);
result = newResult;
}

return result;
}

private static double getJavaVersion() {
final String version = System.getProperty(&quot;java.version&quot;);

