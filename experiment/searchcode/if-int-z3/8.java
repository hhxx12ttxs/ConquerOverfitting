Z3_APP_AST (1);

private final int intValue;

Z3_ast_kind(int v) {
this.intValue = v;
}

public static final Z3_ast_kind fromInt(int v) {
for (Z3_ast_kind k: values())
if (k.intValue == v) return k;

