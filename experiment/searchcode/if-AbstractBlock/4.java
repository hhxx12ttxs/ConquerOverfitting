InterfaceComponent c = null;
AbstractBlock block = null;
while(it.hasNext()) {
if(this.isChildrenChange()) {
break;
}
c = it.next();
if(c instanceof AbstractBlock) {
block = (AbstractBlock)c;

