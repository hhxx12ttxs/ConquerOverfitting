private DiffUtil() {
}

public static final String getClass(int diff) {
if (diff > 0) {
return &quot;diff-pos&quot;;
} else if (diff < 0) {
return &quot;diff-neg&quot;;
} else {
return &quot;diff-zero&quot;;
}
}

}

