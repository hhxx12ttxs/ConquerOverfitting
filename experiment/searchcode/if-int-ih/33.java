ih = instructions.append(new GETSTATIC(arg));
instructionHandleList.add(ih);
labels++;
} else if (instrName.equals(&quot;getfield&quot;)) {
int arg = getFieldConstRef(instrElems, cpg, labels);
ih = instructions.append(new GETFIELD(arg));
instructionHandleList.add(ih);
labels++;
} else if (instrName.equals(&quot;putstatic&quot;)) {
int arg = getFieldConstRef(instrElems, cpg, labels);

