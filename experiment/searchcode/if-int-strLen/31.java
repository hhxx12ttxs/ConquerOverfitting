Scanner start = new Scanner(System.in);
int cases = start.nextInt();
for(int i = cases; i > 0; i--){
long strlen = start.nextLong();
if(strlen % 2 == 0){
System.out.println(strlen);
} else{
System.out.println(strlen - 1);
}
}
}
}

