char[] c=new char[ch.length];
int i=0;
int index=0;

for(char character:ch){

if((i&amp;(1<<character-&#39;a&#39;-0))==0){
ch[index]=character;
index++;
i|=1<<character-&#39;a&#39;-0;
}
}
for(i=index;i<ch.length;i++)ch[i]=&#39;a&#39;-&#39;a&#39;-0;
return ch;

}

}

