// Position de l&#39;élément sur l&#39;axe des z.
private int zindex;

public Element (int z){
this.zindex=z;
}

public int getZindex (){
return this.zindex;
}

@Override
public int compareTo(Element o) {

