
public class shape {
double x,y,height,width;
String name;

public shape(double x, double y, double width, double height, String name) {
public boolean contains(double mouseX, double mouseY) {
if(mouseX>=x&amp;&amp;mouseX<=x+width&amp;&amp;mouseY>=y&amp;&amp;mouseY<=y+height) {
return true;
}


return false;
}


}

