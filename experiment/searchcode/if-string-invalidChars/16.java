public boolean isValid(String filename) {
String[] invalidChars = {&quot; &quot;, &quot;\\&quot;, &quot;?&quot;, &quot;%&quot;, &quot;*&quot;, &quot;|&quot;, &quot;\&quot;&quot;, &quot;<&quot;, &quot;>&quot;};
for (int i = 0; i < invalidChars.length; i++) {
if (filename.contains(invalidChars[i])) {
return false;
}
}
return true;
}
}

