public int mostItems(int boxX, int boxY, int boxZ, int itemX, int itemY, int itemZ) {
int[] item={itemX,itemY,itemZ};
int[] oriented=new int[6];
if(itemZ>boxZ||itemY>boxY||itemZ>boxZ)return 0;
int numOfItem=0;
for(int i=0;i<oriented.length;i++){
if(numOfItem<oriented[i])numOfItem=oriented[i];
}
return numOfItem;
}

}

