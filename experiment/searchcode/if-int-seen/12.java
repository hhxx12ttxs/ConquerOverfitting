final int EQUAL = 0;
final int AFTER = 1;


if(this.getLast_seen() == another.getLast_seen()){
return EQUAL;
}

if(this.getLast_seen() > another.getLast_seen()){
return BEFORE;
}

if(this.getLast_seen() < another.getLast_seen()){

