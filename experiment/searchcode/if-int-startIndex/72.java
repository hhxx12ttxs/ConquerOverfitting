public static String cutText(String text, String indexText){

int startIndex = 0;
int endIndex = 0;

startIndex = text.indexOf(indexText);
if(startIndex >= 0){
endIndex = text.indexOf(&quot;\n&quot;, startIndex);
return text.substring(startIndex + indexText.length(), endIndex -1);
}
return &quot;&quot;;
}
}

