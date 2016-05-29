int position=0;
for(int k=0;k<player.getItems().size();k++)
{
if(player.getItem(k) instanceof ArrowItem)
Level.addEntity(arrow);
position=k;
break;
}
}
if(arrow!=null)
{
player.getItems().set(position,new InvyItemBlank(&quot;empty&quot;));
}
}
}

