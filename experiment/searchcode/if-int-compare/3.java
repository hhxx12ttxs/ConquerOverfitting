this.compare = compare;
}

@Override
public int compare(struct2 arg0, struct2 arg1)
{
if(compare.equals(&quot;Id&quot;))
return arg0.getId().compareTo(arg1.getId());

else if(compare.equals(&quot;Name&quot;))
return arg0.getName().compareTo(arg1.getName());

