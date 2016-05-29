private PatternMatcherRegEx(boolean exclude, String pattern) {
super(exclude);
regPattern = Pattern.compile(pattern);
return new PatternMatcherRegEx(true, pattern);
}

@Override
public Boolean matches(String uri) {
if (regPattern.matcher(uri).matches())

