List<Integer> result = new ArrayList<Integer>();
int tokenLen = words[0].length();
int tokenCount = words.length;
int len = tokenLen*tokenCount ;
for(int j=0;j<tokenCount;j++){
//读取每个单词
String token = str.substring(j*tokenLen,(j+1)*tokenLen);

