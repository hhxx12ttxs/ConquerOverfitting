BObjectInstant ins = (BObjectInstant) it.next();
if (ins.getInstant().equals(prev.getInstant())) {
// NOBObjectInstantExist for the instant to add :
// insert before an existing instant
if (getInstantCount() != 0) ((BObjectInstant) instants.first())

