public static double getCloseValue(MyTree2 tree, double value, double minDiff){

if(tree == null){
return minDiff;
}

System.out.println(tree.data);


double localDiff= Math.abs(tree.data-value);

