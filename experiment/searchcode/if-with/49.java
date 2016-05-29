// &quot;Surround with &#39;if (i != null)&#39;&quot; &quot;true&quot;
import org.jetbrains.annotations.Nullable;

class A {
void foo(@Nullable String i) {
if (i.le<caret>ngth() > 0) {
if (i != &quot;a&quot;) {
}
}
}
}

