iW = (int)((width*3/4)/(Const.NW[Const.HEX])*3); //height
iH =  (int)Math.round (iW*Math.sqrt(3));
if ((Const.NH[Const.HEX]*2)*iH-iH/2>height) {
shifty = Const.SHIFTX;
iH= (int)((height-shifty)/(Const.NH[Const.HEX]*2-0.5f));

