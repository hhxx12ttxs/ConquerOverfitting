public String parseName( String name ){
if( name.contains(&quot;@&quot;) ){
name = name.replace(&quot;@&quot;, &quot;$&quot;);
}
return name;
}

public String showName(String name){
if( name.contains(&quot;$&quot;) ){
name = name.replace(&quot;$&quot;, &quot;@&quot;);
}
return name;
}
}

