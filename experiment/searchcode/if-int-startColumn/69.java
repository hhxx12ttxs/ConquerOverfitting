int startline = t.getLine(), startcolumn = t.getColumn();
if (NodeVariableDec.canStart(t)) return setPos(NodeVariableDec.parse(t), startline, startcolumn);
if (NodeFunctionDec.canStart(t)) return setPos(NodeFunctionDec.parse(t), startline, startcolumn);

