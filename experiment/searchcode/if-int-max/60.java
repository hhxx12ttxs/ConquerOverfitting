min_y = y;
}

public void add(int x, int y){
if ( x > max_x ) max_x = x;
if ( x < min_x ) min_x = x;
if ( y > max_y ) max_y = y;
if ( y < min_y ) min_y = y;
}

public void add( int x, int y, int w, int h){

