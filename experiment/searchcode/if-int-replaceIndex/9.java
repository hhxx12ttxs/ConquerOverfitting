memoryBlock = new LinkedList<>();
int maxDistIndex,willVisit,replaceIndex = -1;
int pageFaultCount = 0, pageReplaceCount = 0;
for (int i = 0; i < pageString.length; i++) {
if (memoryBlock.contains(pageString[i]))

