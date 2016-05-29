if(normalizeScreenX >= 1) match.GetPlayer1().SetPosX(Float.parseFloat(posX) * normalizeScreenX);
else match.GetPlayer1().SetPosX(Float.parseFloat(posX) / normalizeScreenX);

if(normalizeScreenY >= 1) match.GetPlayer1().SetPosY(Float.parseFloat(posY) * normalizeScreenY);
match.GetPlayer1().SetIsUpdated(true);
}
else //Update P2
{
if(normalizeScreenX >= 1) match.GetPlayer2().SetPosX(Float.parseFloat(posX) / normalizeScreenX);

