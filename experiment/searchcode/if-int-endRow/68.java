
public class Block {
private int startrow;
private int endrow;
private int startcol;
private int endcol;
this.endrow = 3;
this.endcol = 3;
}

public boolean livetonextday(int num)
{
if (num==2||num==3)
return true;

