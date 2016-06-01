public static NotifierFactory produce(DiffRepresentation diff){

if(diff.getPriority() == 1){
return new HighPriority(diff);
} else if(diff.getPriority() == 0) {
return new LowPriority(diff);

