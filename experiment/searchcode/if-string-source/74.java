package cn.mengmei.web.el;

public class StringUtils {

public static String subString(String source, Integer leng){
if(source.length()<=leng){
return source;
}else{
return source.substring(0, leng-1)+&quot;...&quot;;
}
}

}

