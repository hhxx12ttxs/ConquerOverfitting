int can = 0;
for( int i = 0; i < n; i++ ) if( seen[i] ) can++;
if( can<c ) return -1;
int ans = 0;
return ans;
}

void search( int i )
{
if( seen[i] ) return;
seen[i] = true;
for( int j = 0; j < n; j++ ) if( g[i].charAt(j)>&#39;0&#39; ) search(j);
}
}

