for(int i = 0; i < A.length; i++) {
if(range >= A.length - 1) return true;
if(range < i) return false;
range = range >= curRange ? range : curRange;
}

if(range >= A.length - 1) return true;
else return false;
}
}

