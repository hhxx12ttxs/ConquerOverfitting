Za=alfa;


for(int i=0;i<periodo;i++)
{
total+=nps[i];
}
media_a=total/periodo;
Z0=(media_a-media)*Math.sqrt(periodo)/varianza;

if(Math.abs(Z0)<Za)

