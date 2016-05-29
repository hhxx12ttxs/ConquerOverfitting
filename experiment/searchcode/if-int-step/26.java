spin = s;
}

public void set(int i) {
step = i;
if(step > max || step < min)
step = 0;
if(spin != null)
if(step > max)
step = 0;
}

public void dec() {
set(step-1);
if(step < min)
step = 0;
}


public int get() {
return step;
}
}

