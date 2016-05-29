public String id;
public static String KEY_TEXT = &quot;text&quot;;
public static String KEY_ID = &quot;id&quot;;

public Tweet(String text, String id){
this.text = text;
this.id = id;
}


@Override
public String get(Object k){

