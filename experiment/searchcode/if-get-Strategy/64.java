private Strategy strategy;

public String replace(){
if(srcString == null) srcString = &quot;&quot;;
return strategy.replace(srcString);
this.srcString = srcString ;
this.strategy = strategy ;
}

public String getSrcString() {
return srcString;

