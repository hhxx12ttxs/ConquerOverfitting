Tag.BASIC, 1), Bool = new Type(&quot;bool&quot;, Tag.BASIC, 1);

public static boolean numeric(Type p) {
if (p == Type.Char || p == Type.Int || p == Type.Float)
return Type.Float;
else if (p1 == Type.Int || p2 == Type.Int)
return Type.Int;
else
return Type.Char;
}
}

