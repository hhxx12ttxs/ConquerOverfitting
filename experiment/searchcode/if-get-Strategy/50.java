super(source);
if (newStrategy == null) {
throw new NullPointerException();
}
this.newStrategy = newStrategy;
}



public AllocationStrategy getNewStrategy() {
return this.newStrategy;
}
}

