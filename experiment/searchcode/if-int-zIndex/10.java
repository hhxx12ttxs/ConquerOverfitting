private static final long serialVersionUID = -2058648426872439077L;
protected int zIndex = 0;

@Override
public int getZIndex() {
@Override
public int compareTo(Drawable o) {
int res = this.zIndex - o.getZIndex();
if(res == 0 &amp;&amp; (o instanceof Unit))

