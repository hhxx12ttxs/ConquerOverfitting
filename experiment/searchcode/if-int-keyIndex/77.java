// TODO Auto-generated method stub
int id = v.getId();
int idx=0, i;
for(i = 0; i < ControllerAct.keyIndex.length; i++)
if(ControllerAct.keyIndex[i] == -1)
for(i = 0; i < ControllerAct.charIdList.length; i++)
if(id == ControllerAct.charIdList[i])
{
ControllerAct.keyIndex[idx] = i;
ControllerAct.dial.dismiss();

