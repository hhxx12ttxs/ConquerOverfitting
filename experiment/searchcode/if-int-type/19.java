public int width = 0;
public Type(String s, int tag, int w) { super(s, tag); width = w; }
public static final Type
public static boolean numeric(Type p) {
if (p == Type.Char || p == Type.Int || p == Type.Float) return true;
else return false;

