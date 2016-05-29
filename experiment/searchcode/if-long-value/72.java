if(l != null) {
if (this.op.getOp() == Operation.LOGIC_NOT)
eval = Long.valueOf(l.longValue() > 0?0:1);
else if (this.op.getOp() == Operation.COMP)
eval = Long.valueOf((long)(~l.longValue()));

