public static TreeNode arith(int op, TreeNode t1, TreeNode t2)
{
if (t1.mode == t2.mode)
return new TreeNode(op, t1.mode, t1, t2);
else
return new TreeNode(op, Ops.DOUBLE, cast(t1, Ops.DOUBLE), cast(t2, Ops.DOUBLE));

