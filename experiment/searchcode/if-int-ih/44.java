instructionHandleList.add(ih);
labels++;
} else if (instrName.equals(&quot;instanceof&quot;)) {
int arg = getClassConstRef(instrElems, cpg, labels);
ih = instructions.append(new INSTANCEOF(arg));
instructionHandleList.add(ih);
labels++;
} else if (instrName.equals(&quot;new&quot;)) {
int arg = getClassConstRef(instrElems, cpg, labels);

