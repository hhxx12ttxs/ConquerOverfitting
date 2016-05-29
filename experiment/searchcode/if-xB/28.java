public static void begin(HttpServletRequest request, HttpServletResponse response,  FilterConfig xbFilterConfig){
if(c_localInstance.get() != null){
throw new IllegalStateException(&quot;Already has begun.&quot;);
Pattern pattern = Pattern.compile(&quot;/system/xb\\w*/([^.]*?)(?:\\.|$)&quot;);
Matcher matcher = pattern.matcher(uri);
if(matcher.find()){

