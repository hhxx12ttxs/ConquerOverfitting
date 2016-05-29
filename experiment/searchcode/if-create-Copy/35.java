return new Itemset();
}

public Value createValue() {
return new Value();
}

public Copy createCopy() {
else if(tagName.equals(&quot;value&quot;))
return createValue();
else if(tagName.equals(&quot;copy&quot;))
return createCopy();
else if(tagName.equals(&quot;choices&quot;))

