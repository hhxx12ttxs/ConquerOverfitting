System.out.println(powerOfTwo(3));
}

private static int powerOfTwo(int i) {
//System.out.println(i);
if(i == 0){
return 1;
}

if(i == 1){
return 2;
}

return 2 * powerOfTwo(i-1);
}

}

